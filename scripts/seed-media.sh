#!/usr/bin/env bash
# Seeds the `media` table by POSTing the real original images in
# scripts/images/ to media-service's own upload endpoint
# (MediaController.upload, POST /api/v1/media/{ownerType}/{ownerId}) --
# the same endpoint gateway's MediaFeignClient.upload() calls for real
# uploads. No auth is required: ownership checks are the Gateway's job
# (see MediaService kdoc), so calling media-service directly here is
# consistent with how the rest of populate-db.sh already talks straight to
# each service's own Postgres container, bypassing Gateway/JWT entirely.
#
# Going through the real endpoint (rather than inserting rows via SQL and
# copying files into storage directly, which is what this script replaces)
# means media-service's own StorageService/ImageResizer pipeline actually
# runs: the original gets stored as-is, real 1024/512/248px variants get
# generated from it, and whichever storage backend the media-service
# container is configured for (local disk or S3, via its own STORAGE_TYPE/
# AWS_* env in docker-compose.yml) is used transparently -- this script
# doesn't need to know or care which one is active.
#
# Reads scripts/sql/media-seed.tsv, which encodes which photo goes on which
# stay/room, with what caption/isPrimary/displayOrder (ported from the old
# scripts/sql/media.sql INSERT rows). Each row's imageHash is the source
# photo's id with any file extension and trailing `_<size>` suffix
# stripped -- resolved against scripts/images/ by glob so it works even
# where the original and the (now-unused-for-seeding) compressed .webp
# don't share an exact filename.
#
# Safe to run multiple times: before uploading a row, that owner's existing
# pictures are fetched (GET /api/v1/media/{ownerType}/{ownerId}) and the row
# is skipped if a picture with that exact caption already exists.
#
# Rows are processed by up to SEED_MEDIA_CONCURRENCY (default 3) concurrent
# background uploads -- each is a real multipart upload plus media-service's
# own resize/WebP/S3-or-disk pipeline, so doing this fully sequentially (as
# this script used to) made a full reseed take 10+ minutes. Concurrency is
# per-row rather than per-owner, so each background row does its own
# existing-captions GET rather than sharing one cached lookup per owner (as
# the old sequential version did) -- a few extra cheap GETs, traded for not
# needing cross-process shared state. See feedback_resource_constrained_dev_box:
# keep this modest, this box has 7.4GB RAM.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

if [ -f .env ]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
fi

IMAGES_DIR="scripts/images"
MANIFEST="scripts/sql/media-seed.tsv"
CONTAINER="media-service"
MEDIA_SERVICE_URL="${MEDIA_SERVICE_URL:-http://localhost:8085}"
CONCURRENCY="${SEED_MEDIA_CONCURRENCY:-1}"

if [ ! -d "$IMAGES_DIR" ]; then
    echo "Error: $IMAGES_DIR not found" >&2
    exit 1
fi

if [ ! -f "$MANIFEST" ]; then
    echo "Error: $MANIFEST not found" >&2
    exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
    echo "Error: container '${CONTAINER}' is not running. Start it with 'scripts/lift-stack.sh' or 'docker compose up -d' first." >&2
    exit 1
fi

RESULTS_DIR="$(mktemp -d)"
trap 'rm -rf "$RESULTS_DIR"' EXIT

owner_has_caption() {
    local owner_type="$1" owner_id="$2" caption="$3"
    local existing
    existing="$(
        curl -sf "$MEDIA_SERVICE_URL/api/v1/media/${owner_type}/${owner_id}" 2>/dev/null | python3 -c '
import json, sys
try:
    data = json.load(sys.stdin)
    if isinstance(data, list):
        for m in data:
            print(m.get("caption") or "")
except Exception:
    pass
'
    )" || existing=""
    grep -qxF "$caption" <<<"$existing"
}

resolve_source_file() {
    local image_hash="$1"
    local matches=("$IMAGES_DIR/${image_hash}"*.jpeg)

    if [ ! -e "${matches[0]}" ]; then
        echo "Error: no original image found for hash '${image_hash}' in $IMAGES_DIR" >&2
        return 1
    fi
    if [ "${#matches[@]}" -ne 1 ]; then
        echo "Error: ambiguous match for hash '${image_hash}': ${matches[*]}" >&2
        return 1
    fi
    printf '%s' "${matches[0]}"
}

# Runs in a background subshell -- reports its outcome via a per-row file in
# RESULTS_DIR rather than mutating shared counters, since counter writes
# from a background job wouldn't be visible to the parent shell.
process_row() {
    local owner_type="$1" owner_id="$2" image_hash="$3" caption="$4" is_primary="$5" display_order="$6" result_id="$7"

    if owner_has_caption "$owner_type" "$owner_id" "$caption"; then
        echo "Skipping ${owner_type} ${owner_id} '${caption}' (already seeded)"
        echo "SKIPPED" >"$RESULTS_DIR/$result_id"
        return 0
    fi

    local src
    src="$(resolve_source_file "$image_hash")" || {
        echo "ERROR" >"$RESULTS_DIR/$result_id"
        return 1
    }
    echo "Uploading ${src} -> ${owner_type} ${owner_id} '${caption}'..."

    local response http_code body attempt=1 max_attempts=3
    while [ "$attempt" -le "$max_attempts" ]; do
        response="$(curl -s -w '\n%{http_code}' -X POST "$MEDIA_SERVICE_URL/api/v1/media/${owner_type}/${owner_id}" \
            -F "file=@${src};type=image/jpeg" \
            -F "caption=${caption}" \
            -F "isPrimary=${is_primary}" \
            -F "displayOrder=${display_order}")" || response=""
        http_code="${response##*$'\n'}"
        body="${response%$'\n'*}"
        if ! [[ "$http_code" =~ ^[0-9]+$ ]]; then
            http_code="000"
        fi

        if [ "$http_code" -ge 200 ] && [ "$http_code" -lt 300 ]; then
            echo "UPLOADED" >"$RESULTS_DIR/$result_id"
            return 0
        fi

        echo "Warning: upload attempt $attempt failed (HTTP ${http_code}) for ${owner_type} ${owner_id} '${caption}': ${body}. Retrying in 2s..." >&2
        attempt=$((attempt + 1))
        sleep 2
    done

    echo "Error: upload failed after $max_attempts attempts (HTTP ${http_code}) for ${owner_type} ${owner_id} '${caption}': ${body}" >&2
    echo "ERROR" >"$RESULTS_DIR/$result_id"
    return 1
}

row_id=0
pids=()

while IFS=$'\t' read -r owner_type owner_id image_hash caption is_primary display_order; do
    [[ "$owner_type" == \#* || -z "$owner_type" ]] && continue

    while [ "$(jobs -rp | wc -l)" -ge "$CONCURRENCY" ]; do
        wait -n
    done

    row_id=$((row_id + 1))
    process_row "$owner_type" "$owner_id" "$image_hash" "$caption" "$is_primary" "$display_order" "$row_id" &
    pids+=("$!")
done < <(grep -v '^#' "$MANIFEST")

failed=0
for pid in "${pids[@]}"; do
    wait "$pid" || failed=1
done

uploaded=0
skipped=0
for f in "$RESULTS_DIR"/*; do
    case "$(cat "$f")" in
        UPLOADED) uploaded=$((uploaded + 1)) ;;
        SKIPPED) skipped=$((skipped + 1)) ;;
    esac
done

echo "Done seeding media: ${uploaded} uploaded, ${skipped} skipped (already present)."

if [ "$failed" -ne 0 ]; then
    echo "Error: one or more uploads failed (see above)." >&2
    exit 1
fi

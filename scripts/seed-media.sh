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
# Safe to run multiple times: before uploading a row, the owner's existing
# pictures are fetched once (GET /api/v1/media/{ownerType}/{ownerId}) and
# the row is skipped if a picture with that exact caption already exists.
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

declare -A caption_cache

owner_has_caption() {
    local owner_type="$1" owner_id="$2" caption="$3"
    local key="${owner_type}:${owner_id}"

    if [ -z "${caption_cache[$key]+set}" ]; then
        caption_cache[$key]="$(
            curl -sf "$MEDIA_SERVICE_URL/api/v1/media/${owner_type}/${owner_id}" | python3 -c '
import json, sys
for m in json.load(sys.stdin):
    print(m.get("caption") or "")
'
        )" || caption_cache[$key]=""
    fi

    grep -qxF "$caption" <<<"${caption_cache[$key]}"
}

resolve_source_file() {
    local image_hash="$1"
    local matches=("$IMAGES_DIR/${image_hash}"*.jpeg)

    if [ ! -e "${matches[0]}" ]; then
        echo "Error: no original image found for hash '${image_hash}' in $IMAGES_DIR" >&2
        exit 1
    fi
    if [ "${#matches[@]}" -ne 1 ]; then
        echo "Error: ambiguous match for hash '${image_hash}': ${matches[*]}" >&2
        exit 1
    fi
    printf '%s' "${matches[0]}"
}

uploaded=0
skipped=0

while IFS=$'\t' read -r owner_type owner_id image_hash caption is_primary display_order; do
    [[ "$owner_type" == \#* || -z "$owner_type" ]] && continue

    if owner_has_caption "$owner_type" "$owner_id" "$caption"; then
        echo "Skipping ${owner_type} ${owner_id} '${caption}' (already seeded)"
        skipped=$((skipped + 1))
        continue
    fi

    src="$(resolve_source_file "$image_hash")"
    echo "Uploading ${src} -> ${owner_type} ${owner_id} '${caption}'..."

    response="$(curl -s -w '\n%{http_code}' -X POST "$MEDIA_SERVICE_URL/api/v1/media/${owner_type}/${owner_id}" \
        -F "file=@${src};type=image/jpeg" \
        -F "caption=${caption}" \
        -F "isPrimary=${is_primary}" \
        -F "displayOrder=${display_order}")"
    http_code="${response##*$'\n'}"
    body="${response%$'\n'*}"

    if [ "$http_code" -lt 200 ] || [ "$http_code" -ge 300 ]; then
        echo "Error: upload failed (HTTP ${http_code}) for ${owner_type} ${owner_id} '${caption}': ${body}" >&2
        exit 1
    fi

    # Keep the per-owner caption cache in sync so later rows for the same
    # owner in this run see the picture that was just uploaded.
    caption_cache["${owner_type}:${owner_id}"]+=$'\n'"${caption}"
    uploaded=$((uploaded + 1))
done < <(grep -v '^#' "$MANIFEST")

echo "Done seeding media: ${uploaded} uploaded, ${skipped} skipped (already present)."

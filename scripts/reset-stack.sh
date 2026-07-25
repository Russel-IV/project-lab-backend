#!/usr/bin/env bash
# Wipes every Postgres volume + the shared uploads volume (and, when
# STORAGE_TYPE=s3, the stays/ and rooms/ prefixes in the real S3 bucket) and
# brings the stack back up from nothing: lift-stack.sh (build + staggered
# start) then populate-db.sh (which seeds all 5 per-service databases,
# including media rows via scripts/seed-media.sh -- real uploads through
# media-service's own endpoint, not a raw file copy).
#
# Use this instead of typing `docker compose down -v` by hand -- see
# feedback_resource_constrained_dev_box: this box has 7.4GB RAM, so bringing
# the stack back up must go through lift-stack.sh's staggered start, never
# an ad-hoc `docker compose up -d`.
#
# Usage: scripts/reset-stack.sh [--no-build] [-y]
#   --no-build   passed through to lift-stack.sh (skip sequential image builds)
#   -y           skip the confirmation prompt (deletes volumes immediately)
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

if [ -f .env ]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
fi

NO_BUILD=false
ASSUME_YES=false
for arg in "$@"; do
    case "$arg" in
        --no-build) NO_BUILD=true ;;
        -y) ASSUME_YES=true ;;
        *)
            echo "Error: unknown argument '$arg'" >&2
            exit 1
            ;;
    esac
done

if [ "$ASSUME_YES" = false ]; then
    confirm_msg="This deletes ALL data in every Postgres volume and the uploads volume."
    if [ "${STORAGE_TYPE:-local}" = "s3" ] && [ -n "${AWS_S3_BUCKET:-}" ]; then
        confirm_msg="${confirm_msg} It also deletes every object under s3://${AWS_S3_BUCKET}/{stays,rooms}."
    fi
    read -r -p "${confirm_msg} Continue? [y/N] " reply
    case "$reply" in
        [yY]|[yY][eE][sS]) ;;
        *)
            echo "Aborted."
            exit 1
            ;;
    esac
fi

echo "== Tearing down containers + volumes =="
docker compose down -v

# populate-db.sh's idempotency check (skip a row if that owner already has a
# picture with that caption) is DB-based, but the media-service DB was just
# wiped above -- so without this, every reset re-uploads the whole manifest
# under fresh UUIDv7 keys and orphans the previous run's objects in S3
# forever. Wiping the prefixes media-service actually writes to (see
# StorageService.save()/saveVariants() -- keys are always "$folder/...",
# and $folder is "stays/{id}" or "rooms/{id}") keeps S3 in step with the
# Postgres/uploads-volume wipe above.
if [ "${STORAGE_TYPE:-local}" = "s3" ] && [ -n "${AWS_S3_BUCKET:-}" ]; then
    echo "== Emptying s3://${AWS_S3_BUCKET}/{stays,rooms} =="
    for prefix in stays rooms; do
        docker run --rm \
            -e AWS_ACCESS_KEY_ID="${AWS_ACCESS_KEY_ID:-}" \
            -e AWS_SECRET_ACCESS_KEY="${AWS_SECRET_ACCESS_KEY:-}" \
            -e AWS_DEFAULT_REGION="${AWS_REGION:-us-east-1}" \
            amazon/aws-cli s3 rm "s3://${AWS_S3_BUCKET}/${prefix}" --recursive
    done
fi

echo "== Bringing the stack back up =="
if [ "$NO_BUILD" = true ]; then
    scripts/lift-stack.sh --no-build
else
    scripts/lift-stack.sh
fi

echo "== Seeding databases + images =="
scripts/populate-db.sh

echo "Done. Stack reset and reseeded."

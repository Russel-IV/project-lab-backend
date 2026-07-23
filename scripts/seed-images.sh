#!/usr/bin/env bash
# Copies/uploads the real images in scripts/images/ into either:
# 1. AWS S3 bucket (when STORAGE_TYPE=s3 or AWS_S3_BUCKET is set), OR
# 2. The shared `uploads` docker volume in media-service container (when STORAGE_TYPE=local).
#
# Copies at the exact keys scripts/sql/media.sql's rows reference
# (`stays/{stayId}/{filename}` / `rooms/{roomId}/{filename}`) -- matching what
# StorageService.save() produces for uploads. Run this before loading scripts/sql/media.sql
# (scripts/populate-db.sh does this automatically).
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

if [ -f .env ]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
fi

IMAGES_DIR="scripts/images"
MEDIA_SQL="scripts/sql/media.sql"
CONTAINER="media-service"
STORAGE_TYPE="${STORAGE_TYPE:-local}"

if [ ! -d "$IMAGES_DIR" ]; then
    echo "Error: $IMAGES_DIR not found" >&2
    exit 1
fi

if [ ! -f "$MEDIA_SQL" ]; then
    echo "Error: $MEDIA_SQL not found" >&2
    exit 1
fi

keys="$(python3 -c "
import re
with open('$MEDIA_SQL') as f:
    content = f.read()
for m in re.finditer(r\"'((?:stays|rooms)/[0-9]+/[^']+)'\", content):
    print(m.group(1))
")"

count="$(echo "$keys" | grep -c . || true)"

if [ "$STORAGE_TYPE" = "s3" ] || [ -n "${AWS_S3_BUCKET:-}" ]; then
    BUCKET="${AWS_S3_BUCKET:-frui-media-bucket}"
    echo "Uploading ${count} seed images to S3 bucket '${BUCKET}'..."

    if ! command -v aws &> /dev/null; then
        echo "Error: AWS CLI ('aws') is not installed or not in PATH." >&2
        exit 1
    fi

    while IFS= read -r key; do
        [ -z "$key" ] && continue
        filename="$(basename "$key")"
        src="${IMAGES_DIR}/${filename}"
        if [ ! -f "$src" ]; then
            echo "Error: ${src} not found (referenced by ${MEDIA_SQL} as '${key}')" >&2
            exit 1
        fi
        echo "Uploading ${src} to s3://${BUCKET}/${key}..."
        aws s3 cp "$src" "s3://${BUCKET}/${key}"
    done <<< "$keys"

else
    if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
        echo "Error: container '${CONTAINER}' is not running. Start it with 'scripts/lift-stack.sh' or 'docker compose up -d' first." >&2
        exit 1
    fi

    echo "Copying ${count} seed images into ${CONTAINER}'s uploads volume..."

    while IFS= read -r key; do
        [ -z "$key" ] && continue
        filename="$(basename "$key")"
        folder="$(dirname "$key")"
        src="${IMAGES_DIR}/${filename}"
        if [ ! -f "$src" ]; then
            echo "Error: ${src} not found (referenced by ${MEDIA_SQL} as '${key}')" >&2
            exit 1
        fi
        docker exec "$CONTAINER" mkdir -p "/app/uploads/${folder}"
        docker cp "$src" "${CONTAINER}:/app/uploads/${key}"
    done <<< "$keys"
fi

echo "Done seeding images."

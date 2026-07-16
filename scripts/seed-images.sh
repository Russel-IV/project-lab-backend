#!/usr/bin/env bash
# Copies the real images in scripts/images/ into the shared `uploads` docker
# volume, at the exact keys scripts/sql/media.sql's rows reference
# (`stays/{stayId}/{filename}` / `rooms/{roomId}/{filename}`) -- matching what
# LocalStorageService.save() would have produced for a real upload. Run this
# before loading scripts/sql/media.sql (scripts/populate-db.sh does this
# automatically) -- the DB rows are meaningless without the actual files
# existing on disk for the Gateway's /uploads/** static handler to serve.
#
# Rather than hardcoding the image-to-owner mapping a second time here, this
# script parses media.sql itself for every `'stays/N/...'` / `'rooms/N/...'`
# value, so the two files can't drift out of sync.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

IMAGES_DIR="scripts/images"
MEDIA_SQL="scripts/sql/media.sql"
CONTAINER="media-service"

if [ ! -d "$IMAGES_DIR" ]; then
    echo "Error: $IMAGES_DIR not found" >&2
    exit 1
fi

if [ ! -f "$MEDIA_SQL" ]; then
    echo "Error: $MEDIA_SQL not found" >&2
    exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER}$"; then
    echo "Error: container '${CONTAINER}' is not running. Start it with 'scripts/lift-stack.sh' or 'docker compose up -d' first." >&2
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

echo "Done."

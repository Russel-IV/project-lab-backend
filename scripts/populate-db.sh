#!/usr/bin/env bash
# Loads dev seed data into the running per-service Postgres containers.
#
# Post-migration (docs/adr/0001 and on), there is no single monolithic
# database anymore — the old db/population_script.sql/populate-db.sh
# targeted just "project-lab-database", which now owns zero domain
# tables (everything was extracted into per-service databases and
# dropped from gateway's own Flyway history, see gateway's
# V18-V22__drop_*.sql). This script replaces that pair: it loads
# scripts/sql/{identity,inventory,booking,review}.sql into their respective
# containers, in the dependency order the seed data's ids actually require
# (see each fragment's own header comment), plus scripts/seed-media.sh (see
# below) in place of a media.sql fragment.
#
# There is no `user_favorite` fragment: that table was dropped outright
# in gateway's V20__drop_identity_tables.sql and never recreated by any
# service (it was already an orphaned/unmapped table before the split —
# no JPA entity referenced it even in the monolith). Nothing ports it.
#
# There is no media.sql fragment: media rows are seeded by
# scripts/seed-media.sh instead, which POSTs the original images in
# scripts/images/ straight to media-service's own upload endpoint (see that
# script's header) so its real StorageService/ImageResizer pipeline runs —
# storing the original, generating resized variants, and writing to
# whichever storage backend (local disk or S3) the container is configured
# for. Run in the same slot media.sql used to occupy, once identity/
# inventory ids exist.
#
# Safe to run multiple times: every SQL fragment uses explicit ids with
# ON CONFLICT DO NOTHING and resets its own sequences; scripts/seed-media.sh
# has its own re-run guard (see that script's header) since real uploads get
# server-generated ids rather than the fixed ones a SQL fragment could rely
# on ON CONFLICT DO NOTHING for.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

if [ -f .env ]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
fi

POSTGRES_USER="${POSTGRES_USER:-postgres}"
POSTGRES_DB="${POSTGRES_DB:-postgres}"

# service name -> container name -> sql fragment, in dependency order
# (identity/inventory seed the ids booking/review/media reference).
FRAGMENTS_BEFORE_MEDIA=(
    "identity-database:scripts/sql/identity.sql"
    "inventory-database:scripts/sql/inventory.sql"
)
FRAGMENTS_AFTER_MEDIA=(
    "booking-database:scripts/sql/booking.sql"
    "review-database:scripts/sql/review.sql"
)

load_fragment() {
    local entry="$1" container sql_file
    container="${entry%%:*}"
    sql_file="${entry#*:}"

    if [ ! -f "$sql_file" ]; then
        echo "Error: $sql_file not found" >&2
        exit 1
    fi

    if ! docker ps --format '{{.Names}}' | grep -q "^${container}$"; then
        echo "Error: container '${container}' is not running. Start it with 'scripts/lift-stack.sh' or 'docker compose up -d' first." >&2
        exit 1
    fi

    echo "Loading ${sql_file} into ${container} (db: ${POSTGRES_DB}, user: ${POSTGRES_USER})..."
    docker exec -i "$container" psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < "$sql_file"
}

for entry in "${FRAGMENTS_BEFORE_MEDIA[@]}"; do
    load_fragment "$entry"
done

./scripts/seed-media.sh

for entry in "${FRAGMENTS_AFTER_MEDIA[@]}"; do
    load_fragment "$entry"
done

echo "Triggering chatbot knowledge ingestion..."
CHATBOT_URL="${CHATBOT_URL:-http://localhost:8086}"
if curl -sf -X POST "${CHATBOT_URL}/internal/chat/ingest" >/dev/null 2>&1; then
    echo "Chatbot static knowledge successfully ingested."
else
    echo "Warning: Chatbot ingestion failed or chatbot-service is not responding." >&2
fi

echo "Done."

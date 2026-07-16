#!/usr/bin/env bash
# Loads dev seed data into the running per-service Postgres containers.
#
# Post-migration (docs/adr/0001 and on), there is no single monolithic
# database anymore — the old db/population_script.sql/populate-db.sh
# targeted just "project-lab-database", which now owns zero domain
# tables (everything was extracted into per-service databases and
# dropped from gateway's own Flyway history, see gateway's
# V18-V22__drop_*.sql). This script replaces that pair: it loads
# scripts/sql/{identity,inventory,media,booking,review}.sql into their
# respective containers, in the dependency order the seed data's ids
# actually require (see each fragment's own header comment).
#
# There is no `user_favorite` fragment: that table was dropped outright
# in gateway's V20__drop_identity_tables.sql and never recreated by any
# service (it was already an orphaned/unmapped table before the split —
# no JPA entity referenced it even in the monolith). Nothing ports it.
#
# Before loading media.sql, this also runs scripts/seed-images.sh, which
# copies the real images in scripts/images/ into the shared uploads volume
# at the exact keys media.sql's rows reference — the DB rows are meaningless
# without the actual files existing for the Gateway's /uploads/** handler to
# serve.
#
# Safe to run multiple times: every fragment uses explicit ids with
# ON CONFLICT DO NOTHING and resets its own sequences.
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
FRAGMENTS=(
    "identity-database:scripts/sql/identity.sql"
    "inventory-database:scripts/sql/inventory.sql"
    "media-database:scripts/sql/media.sql"
    "booking-database:scripts/sql/booking.sql"
    "review-database:scripts/sql/review.sql"
)

for entry in "${FRAGMENTS[@]}"; do
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

    if [ "$sql_file" = "scripts/sql/media.sql" ]; then
        ./scripts/seed-images.sh
    fi

    echo "Loading ${sql_file} into ${container} (db: ${POSTGRES_DB}, user: ${POSTGRES_USER})..."
    docker exec -i "$container" psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < "$sql_file"
done

echo "Done."

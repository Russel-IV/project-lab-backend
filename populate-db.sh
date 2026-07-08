#!/usr/bin/env bash
# Loads db/population_script.sql into the running project-lab-database container.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

CONTAINER_NAME="project-lab-database"
SQL_FILE="db/population_script.sql"

if [ -f .env ]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
fi

POSTGRES_USER="${POSTGRES_USER:-postgres}"
POSTGRES_DB="${POSTGRES_DB:-postgres}"

if [ ! -f "$SQL_FILE" ]; then
    echo "Error: $SQL_FILE not found" >&2
    exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "Error: container '${CONTAINER_NAME}' is not running. Start it with 'docker compose up -d' first." >&2
    exit 1
fi

echo "Loading ${SQL_FILE} into ${CONTAINER_NAME} (db: ${POSTGRES_DB}, user: ${POSTGRES_USER})..."
docker exec -i "$CONTAINER_NAME" psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" < "$SQL_FILE"

echo "Done."

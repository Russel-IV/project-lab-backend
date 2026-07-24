#!/usr/bin/env bash
# Wipes every Postgres volume + the shared uploads volume and brings the
# stack back up from nothing: lift-stack.sh (build + staggered start) then
# populate-db.sh (which seeds all 5 per-service databases, including media
# rows via scripts/seed-media.sh -- real uploads through media-service's own
# endpoint, not a raw file copy).
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
    read -r -p "This deletes ALL data in every Postgres volume and the uploads volume. Continue? [y/N] " reply
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

echo "== Bringing the stack back up =="
if [ "$NO_BUILD" = true ]; then
    scripts/lift-stack.sh --no-build
else
    scripts/lift-stack.sh
fi

echo "== Seeding databases + images =="
scripts/populate-db.sh

echo "Done. Stack reset and reseeded."

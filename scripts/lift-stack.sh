#!/usr/bin/env bash
# Brings up the full docker-compose stack (7 JVM services + 6 Postgres +
# zipkin) without pegging every core on this dev box (see
# feedback_resource_constrained_dev_box: 7.4GB RAM total).
#
# `docker compose up --build -d` builds every "dirty" image concurrently by
# default — up to 7 simultaneous Maven+Kotlin multi-stage builds, each its
# own JVM doing dependency resolution and compilation. That reliably
# saturates every core and can hang the whole VM on a box this size. This
# script instead:
#   1. Builds each app image one at a time (peak concurrent compiles: 1).
#   2. Starts the lightweight infra (6 Postgres containers + zipkin)
#      together, since Postgres startup is cheap.
#   3. Starts eureka-server and waits for it to report healthy.
#   4. Starts the 6 app services ONE AT A TIME, waiting for each to answer
#      its own /actuator/health before starting the next and pausing a few
#      seconds after that — 6 Spring Boot apps doing Flyway migrations +
#      context startup + Eureka registration simultaneously is exactly the
#      kind of burst this script exists to avoid.
#
# Usage: scripts/lift-stack.sh [--no-build]
#   --no-build   skip the sequential image build step (use existing images)
#
# Tuning via env vars:
#   LIFT_STAGGER_SECONDS        pause between starting each app service (default 15)
#   LIFT_HEALTH_TIMEOUT_SECONDS max wait per service before giving up (default 180)
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/.."

if [ -f .env ]; then
    set -a
    # shellcheck disable=SC1091
    source .env
    set +a
fi

SKIP_BUILD=false
if [ "${1:-}" = "--no-build" ]; then
    SKIP_BUILD=true
fi

STAGGER_SECONDS="${LIFT_STAGGER_SECONDS:-15}"
HEALTH_TIMEOUT_SECONDS="${LIFT_HEALTH_TIMEOUT_SECONDS:-180}"

INFRA_SERVICES=(project-lab-database review-database media-database identity-database inventory-database booking-database zipkin)
DB_SERVICES=(project-lab-database review-database media-database identity-database inventory-database booking-database)
APP_SERVICES=(project-lab-backend identity-service inventory-service booking-service review-service media-service)

# Host port each app service's own /actuator/health is reachable on.
declare -A HEALTH_PORT=(
    [project-lab-backend]="${SPRING_PORT:-8080}"
    [identity-service]=8081
    [inventory-service]=8082
    [booking-service]=8083
    [review-service]=8084
    [media-service]=8085
)

# Polls `docker inspect`'s own healthcheck status — used for containers that
# already declare a healthcheck in docker-compose.yml (the 6 Postgres
# containers, eureka-server).
wait_for_container_healthy() {
    local container="$1"
    local waited=0
    echo "Waiting for ${container} to report healthy..."
    while true; do
        status="$(docker inspect --format='{{.State.Health.Status}}' "$container" 2>/dev/null || echo "starting")"
        if [ "$status" = "healthy" ]; then
            echo "${container} is healthy."
            return 0
        fi
        if [ "$waited" -ge "$HEALTH_TIMEOUT_SECONDS" ]; then
            echo "Error: ${container} did not become healthy within ${HEALTH_TIMEOUT_SECONDS}s (last status: ${status})" >&2
            exit 1
        fi
        sleep 3
        waited=$((waited + 3))
    done
}

# Polls a Spring Boot app service's own actuator endpoint directly — these
# services have no compose-level healthcheck defined today.
wait_for_actuator_health() {
    local service="$1"
    local port="$2"
    local waited=0
    echo "Waiting for ${service} to answer http://localhost:${port}/actuator/health..."
    while true; do
        if curl -sf "http://localhost:${port}/actuator/health" >/dev/null 2>&1; then
            echo "${service} is up."
            return 0
        fi
        if [ "$waited" -ge "$HEALTH_TIMEOUT_SECONDS" ]; then
            echo "Error: ${service} did not answer /actuator/health within ${HEALTH_TIMEOUT_SECONDS}s" >&2
            exit 1
        fi
        sleep 3
        waited=$((waited + 3))
    done
}

if [ "$SKIP_BUILD" = false ]; then
    echo "== Building app images sequentially (one Maven/Kotlin compile at a time) =="
    for svc in eureka-server "${APP_SERVICES[@]}"; do
        echo "--- building ${svc} ---"
        docker compose build "$svc"
    done
else
    echo "== Skipping build (--no-build) =="
fi

echo "== Starting databases + zipkin =="
docker compose up -d "${INFRA_SERVICES[@]}"
for svc in "${DB_SERVICES[@]}"; do
    wait_for_container_healthy "$svc"
done

echo "== Starting eureka-server =="
docker compose up -d eureka-server
wait_for_container_healthy eureka-server

echo "== Starting app services one at a time, staggered ${STAGGER_SECONDS}s apart =="
for svc in "${APP_SERVICES[@]}"; do
    echo "--- starting ${svc} ---"
    docker compose up -d "$svc"
    wait_for_actuator_health "$svc" "${HEALTH_PORT[$svc]}"
    echo "Pausing ${STAGGER_SECONDS}s before starting the next service..."
    sleep "$STAGGER_SECONDS"
done

echo "Stack is up. Gateway: http://localhost:${SPRING_PORT:-8080}  Eureka: http://localhost:8761  Zipkin: http://localhost:9411"

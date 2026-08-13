# 15. Switch container base images to Alpine where arm64 support exists

Date: 2026-07-16

## Status

Accepted

## Context

None of the 7 service Dockerfiles or the Postgres-family images used Alpine
variants, despite Alpine being the standard way to shrink JVM images and reduce
attack surface. All 7 shared: build stage `maven:3.9.9-eclipse-temurin-24`
(Debian), runtime `eclipse-temurin:24-jre` (Debian). Databases were `postgres:16`
and `postgis/postgis:16-3.4`; Zipkin was `openzipkin/zipkin:latest`.

Since [ADR-0014](0014-deployment-topology-oracle-cloud.md), prod deploys to an
Oracle Ampere A1 (**arm64**), cross-built via QEMU/buildx. Every Alpine tag had to
be verified multi-arch first — an amd64-only tag would build fine locally and in
CI, then silently break at deploy time:

| Image | Alpine tag | arm64/v8? |
|---|---|---|
| `maven:3.9.9-eclipse-temurin-24` | `-alpine` | yes |
| `eclipse-temurin:24-jre` | `24-jre-alpine` | yes |
| `postgres:16` | `16-alpine` | yes |
| `postgis/postgis:16-3.4` | only `16-3.5-alpine` exists | **amd64-only** |
| `openzipkin/zipkin:latest` | `zipkin-slim` | yes |

`postgis/postgis` publishes no arm64 Alpine build, so `project-lab-database` and
`inventory-database` stay on the Debian image. Separately: the eureka-server
healthcheck uses `bash -c 'exec 3<>/dev/tcp/...'`, but Alpine ships only busybox
`ash`, which doesn't support `/dev/tcp`.

## Decision

- All 7 Dockerfiles: `-alpine` / `24-jre-alpine`, plus `RUN apk add --no-cache
  bash` in the runtime stage to keep the `/dev/tcp` healthcheck working.
- `postgres:16` → `postgres:16-alpine` for review/media/identity/booking
  databases.
- `postgis/postgis:16-3.4` unchanged (no arm64 Alpine build exists).
- `openzipkin/zipkin:latest` → `openzipkin/zipkin-slim:latest` (safe — this
  project only uses Zipkin's default in-memory storage).

Validated by rebuilding the full stack locally (`scripts/lift-stack.sh`) — all 14
containers healthy, eureka's healthcheck passed with `bash` installed, Gateway
showed all 6 services registered with DB connectivity up. arm64 correctness itself
is left to `deploy.yml`'s buildx cross-build.

Measured image size:

| Image | Before | After |
|---|---|---|
| App services (7x, avg) | ~604–642MB | ~452–531MB |
| `postgres:16` → `-alpine` | 642MB | 396MB |
| `zipkin` → `zipkin-slim` | 393MB | 311MB |

## Consequences

- Smaller images, faster pulls, less attack surface — complementary to (not
  overlapping with) ADR-0014's per-container memory budget.
- `postgis/postgis` stays on Debian indefinitely unless a custom Alpine+PostGIS
  build is worth maintaining later.
- Every runtime stage now carries `apk add --no-cache bash`; new healthchecks or
  Dockerfiles should follow this same pattern unless a dependency (like PostGIS)
  blocks it.

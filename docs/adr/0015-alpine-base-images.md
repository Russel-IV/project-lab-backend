# 15. Switch container base images to Alpine where arm64 support exists

Date: 2026-07-16

## Status

Accepted

## Context

None of the 7 service Dockerfiles (`gateway`, `eureka-server`, `identity-service`,
`inventory-service`, `booking-service`, `review-service`, `media-service`) or the
Postgres-family images in `docker-compose.yml`/`docker-compose.prod.yml` used
Alpine variants, despite Alpine being the standard way to shrink JVM container
images and reduce attack surface (fewer packages than a full Debian/Ubuntu
userland). All 7 Dockerfiles shared the same pattern:

- Build stage: `maven:3.9.9-eclipse-temurin-24` (Debian-based)
- Runtime stage: `eclipse-temurin:24-jre` (Debian-based)

Databases were `postgres:16` (review, media, identity, booking) and
`postgis/postgis:16-3.4` (project-lab, inventory); Zipkin was
`openzipkin/zipkin:latest`.

Since [ADR-0014](0014-deployment-topology-oracle-cloud.md), production deploys to
an Oracle Ampere A1 instance — **arm64**, not amd64 — with images cross-built via
QEMU/buildx in `.github/workflows/deploy.yml`. Any Alpine tag picked here had to
be verified multi-arch first, since an amd64-only tag would build fine on a local
x86 dev box and this repo's amd64 GitHub Actions runner, then fail or silently
produce the wrong architecture at deploy time. Checked against Docker Hub
manifests before changing anything:

| Image | Alpine tag | arm64/v8? |
|---|---|---|
| `maven:3.9.9-eclipse-temurin-24` | `-alpine` | yes |
| `eclipse-temurin:24-jre` | `24-jre-alpine` | yes |
| `postgres:16` | `16-alpine` | yes |
| `postgis/postgis:16-3.4` | only `16-3.5-alpine` exists | **amd64-only** |
| `openzipkin/zipkin:latest` | `zipkin-slim` | yes |

`postgis/postgis` publishes no arm64 Alpine build at all — switching
`project-lab-database` or `inventory-database` to it would have broken production
silently (or via a confusing manifest-mismatch failure at deploy time), so those
two stay on the Debian-based image.

A second compatibility gap surfaced once running: `docker-compose.yml`'s
eureka-server healthcheck uses `bash -c 'exec 3<>/dev/tcp/127.0.0.1/8761'` (chosen
originally because the Debian-based JRE image has no curl/wget either). Alpine
images ship only busybox `ash`, which doesn't support `/dev/tcp` — the healthcheck
would silently break without `bash` present.

## Decision

- All 7 Dockerfiles: `maven:3.9.9-eclipse-temurin-24` → `-alpine`, and
  `eclipse-temurin:24-jre` → `24-jre-alpine`, with `RUN apk add --no-cache bash`
  added to the runtime stage of each to keep the existing `/dev/tcp` healthcheck
  working unchanged.
- `postgres:16` → `postgres:16-alpine` for review-database, media-database,
  identity-database, and booking-database, in both compose files.
- `postgis/postgis:16-3.4` left unchanged for project-lab-database and
  inventory-database — no arm64 Alpine build exists upstream.
- `openzipkin/zipkin:latest` → `openzipkin/zipkin-slim:latest` — safe since this
  project only uses Zipkin's default in-memory storage (no
  Cassandra/Elasticsearch/Kafka collector, which is what `-slim` drops).

Validated by rebuilding the full stack locally with `scripts/lift-stack.sh`
(sequential builds, staggered startup — see
[feedback_resource_constrained_dev_box]) and confirming all 14 containers
reported healthy, the eureka-server `/dev/tcp` healthcheck passed with the new
`bash` package, and the gateway's `/actuator/health` showed all 6 services
registered in Eureka with DB connectivity up. arm64 correctness itself is left to
`deploy.yml`'s existing buildx cross-build, not this local validation.

Measured image size changes on the dev box:

| Image | Before | After |
|---|---|---|
| App services (7x, avg) | ~604–642MB | ~452–531MB |
| `postgres:16` → `-alpine` | 642MB | 396MB |
| `zipkin` → `zipkin-slim` | 393MB | 311MB |

## Consequences

- Smaller images pull faster and reduce attack surface on both the 7.4GB dev box
  and the 12GB Ampere A1 prod box from ADR-0014 — image size wasn't the
  bottleneck ADR-0014's per-container `mem_limit`/`MaxRAMPercentage` budget was
  solving (that's JVM heap, independent of base OS), so this is a complementary,
  not overlapping, optimization.
- `postgis/postgis` stays on its heavier Debian base indefinitely unless a future
  need justifies maintaining a custom Alpine+PostGIS image build — real ongoing
  maintenance burden not currently worth taking on for a lab project.
- Every Dockerfile's runtime stage now has an extra `apk add --no-cache bash`
  layer; any new compose healthcheck added later should use this same `bash`
  rather than assuming Debian-style `/bin/sh` behavior, and any *new* service
  Dockerfile added going forward should follow this same Alpine-first pattern
  unless its dependencies (like PostGIS) don't support it.

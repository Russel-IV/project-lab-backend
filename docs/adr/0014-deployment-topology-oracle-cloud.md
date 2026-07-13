# 14. Deployment topology: migrate to Oracle Cloud Free Tier

Date: 2026-07-02

## Status

Accepted

## Context

Production currently runs on AWS, documented in `docs/aws-deployment.md`:

- EC2 `t3.micro` — **1GB RAM**, 2 vCPUs — with a 2GB swapfile and
  `-XX:MaxRAMPercentage=70.0` specifically configured to keep *one* Spring Boot
  JVM from OOM-killing itself.
- RDS `db.t3.micro` — 1GB RAM, free-tier eligible, separate from the app instance.

This box was sized and tuned for exactly one constrained JVM. A microservices
topology per [ADR-0001](0001-adopt-microservices-with-gateway.md) through
[ADR-0013](0013-observability-and-tracing.md) requires, at minimum: Gateway,
Identity, Inventory, Booking, Review, Media, and Eureka — **7 concurrent JVMs**,
plus optionally Zipkin. This cannot run on a 1GB box regardless of code changes;
attempting it would mean constant swap-thrashing, GC pauses, and OOM kills, not a
usable deployment.

AWS's free tier for anything larger than `t2/t3.micro` is time-limited (12
months) or credit-based, not free indefinitely — it does not solve this problem
within the "as close to zero cost as possible, indefinitely" constraint this
project operates under.

## Decision

Migrate hosting from AWS EC2 + RDS to **Oracle Cloud Infrastructure's Always Free
tier** — specifically the Ampere A1 compute shape. As of a June 2026 change to
Oracle's free tier, this allocation is **2 OCPUs + 12GB RAM total** (reduced from
the previous 4 OCPUs/24GB), free indefinitely, not a trial — usable as one
instance or split across two. Oracle also includes, separately, up to 2 small
AMD x86 micro instances (1/8 OCPU + 1GB RAM each) at no cost, which are too weak
for another JVM but usable for something trivial if needed. 200GB of Always Free
block storage is included, comfortably covering Postgres data and uploaded media.

Run all services, Eureka, and a self-hosted Postgres container (replacing RDS) via
Docker Compose on the Ampere A1 instance — the same deployment model already in
use locally (`docker-compose up --build`), just more containers on a larger box.
Each service's JVM is memory-capped (`-XX:MaxRAMPercentage`, the same technique
already used on the current EC2 box) so the fixed set of services fits within the
12GB budget with headroom. CI/CD adapts the existing GitHub Actions pipeline
(currently: test → build → push to ECR → SSH-deploy to EC2) to push images to a
registry reachable from Oracle Cloud (e.g. GitHub Container Registry) and
SSH-deploy to the new host in place of EC2.

## Consequences

- Resolves the RAM constraint that would otherwise block this entire migration —
  without it, [ADR-0006](0006-service-discovery-eureka.md) (Eureka) and the
  service count in [ADR-0002](0002-service-boundaries-and-decomposition.md) would
  both need to be cut down to fit 1GB instead. 12GB is tighter than the 24GB
  originally assumed, but still comfortably fits the 7-JVM topology (Gateway +
  5 services + Eureka) at roughly 400-500MB per service plus one shared Postgres
  container, provided each service enforces a memory cap rather than running
  with JVM defaults.
- `docs/aws-deployment.md` becomes outdated once this is executed; it should be
  superseded by a new `docs/oracle-cloud-deployment.md` documenting the equivalent
  setup (compute instance provisioning, security lists, Docker Compose deployment,
  updated GitHub Actions secrets) — that document is follow-up work, not part of
  this ADR.
- The database is no longer a managed service (RDS) — backups, upgrades, and
  patching become the team's responsibility instead of AWS's. Accepted trade-off
  for zero cost at this project's scale; revisit if data durability requirements
  grow beyond what a self-managed container can reasonably guarantee.
- Loses the AWS-specific IAM-role-based ECR pull convenience; replaced with
  whatever credential mechanism the chosen registry requires.
- Everything in ADRs 0001–0013 is written assuming this hosting target exists; if
  this decision is reversed, several of those ADRs (0002 service count, 0006
  discovery) need to be revisited, not just this one.

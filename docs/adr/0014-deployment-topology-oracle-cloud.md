# 14. Deployment topology: migrate to Oracle Cloud Free Tier

Date: 2026-07-02

## Status

Accepted

## Context

Production runs on AWS (`docs/aws-deployment.md`): EC2 `t3.micro` (1GB RAM,
2 vCPU, 2GB swap, `-XX:MaxRAMPercentage=70.0`) + RDS `db.t3.micro` (1GB RAM) —
sized for exactly one constrained JVM. The microservices topology (ADR-0001–0013)
needs at least 7 concurrent JVMs (Gateway, 5 services, Eureka), plus optionally
Zipkin — not viable on a 1GB box. AWS's free tier beyond `t2/t3.micro` is time- or
credit-limited, not free indefinitely.

## Decision

Migrate to **Oracle Cloud's Always Free tier** — Ampere A1, 2 OCPUs + 12GB RAM
total (reduced from a previous 4 OCPU/24GB, as of June 2026), free indefinitely,
plus 200GB Always Free block storage.

Run all services, Eureka, and a self-hosted Postgres container (replacing RDS) via
Docker Compose — the same model already used locally, just more containers. Each
JVM is memory-capped (`-XX:MaxRAMPercentage`, same technique as the current EC2
box) to fit the 12GB budget. CI/CD adapts the existing GitHub Actions pipeline to
push to a registry reachable from Oracle Cloud and SSH-deploy there instead of EC2.

## Consequences

- Resolves the RAM constraint blocking this whole migration. 12GB is tighter than
  the 24GB originally assumed but still fits ~400–500MB/service + one Postgres
  container, provided each service caps its own memory.
- `docs/aws-deployment.md` becomes outdated; a new `docs/oracle-cloud-deployment.md`
  is follow-up work, not part of this ADR.
- The database is self-managed now, not RDS — backups/upgrades become the team's
  job. Accepted for zero cost; revisit if durability needs grow.
- Loses AWS IAM-based ECR pull convenience.
- ADRs 0001–0013 assume this hosting target; reversing it means revisiting several
  of them (service count, discovery), not just this one.

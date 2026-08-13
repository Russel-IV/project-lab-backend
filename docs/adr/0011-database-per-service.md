# 11. Database-per-service

Date: 2026-07-02

## Status

Accepted

## Context

All data lives in one Postgres DB today, one Flyway sequence (`V1`–`V10`), with
Postgres enum types and cross-table FKs enforced at the DB level.

## Decision

Each service (Identity, Inventory, Booking, Review, Media) gets its own Postgres
database, self-hosted ([ADR-0014](0014-deployment-topology-oracle-cloud.md)).
Flyway migrations are partitioned per service; enum types are duplicated wherever
needed (e.g. `booking_status` now lives only in Booking's DB).

Cross-service FKs are **dropped at the DB level**, enforced at the app layer
instead:
- `booking.user_id` — no FK check; a valid JWT already implies a real user.
- `stay.host_id` — validated via Feign to Identity (or the host's own JWT claims at
  creation time), not a DB constraint.

## Consequences

- Loses cross-domain referential integrity at the DB level — a standard
  microservices trade-off; a bad ID now surfaces as an app-level validation
  failure or a 404, not a constraint violation.
- Each service migrates/evolves its schema independently.
- No net increase in migration files, just partitioned by service.
- `V10`-style migrations become Media-service-only once Media is extracted.

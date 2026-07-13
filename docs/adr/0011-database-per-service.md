# 11. Database-per-service

Date: 2026-07-02

## Status

Accepted

## Context

All data currently lives in one Postgres database, managed by one set of Flyway
migrations (`V1`–`V10`), with Postgres-native enum types (`property_type`,
`booking_status`, `amenity_type`) and cross-table foreign keys (e.g.
`booking.user_id → user.id`, `stay.host_id → host.id`) enforced at the DB level.

## Decision

Each service (Identity, Inventory, Booking, Review, Media) gets its own Postgres
database/schema, self-hosted via container(s) on the new hosting target
([ADR-0014](0014-deployment-topology-oracle-cloud.md)). Flyway migration files are
partitioned by service — each service owns only the migrations for its own
tables. Enum types are duplicated into whichever schemas need them (e.g.
`booking_status` exists only in Booking's DB now, not globally).

Cross-service foreign keys are **dropped at the database level** and enforced at
the application layer instead:

- `booking.user_id`: no longer FK-checked against `user`; the user's existence is
  implied by a valid JWT (issued only for real users), so no check is needed at
  all.
- `stay.host_id`: validated via a Feign call to Identity (or trusted from the
  authenticated host's own JWT claims at Stay-creation time), not a DB constraint.

## Consequences

- Loses cross-domain referential integrity at the database level — a standard,
  accepted microservices trade-off. Data corruption from a bad ID now surfaces as
  an application-level validation failure or a Feign call returning 404, not a DB
  constraint violation.
- Each service migrates and evolves its schema independently — no more
  coordinating one shared `V{n}` migration sequence across unrelated domains.
- No net increase in total migration files, just partitioned by service instead of
  centralized.
- `V10__normalize_picture_urls.sql`-style migrations become Media-service-only
  concerns once Media is extracted.

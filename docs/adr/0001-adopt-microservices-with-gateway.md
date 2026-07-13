# 1. Adopt microservices architecture with a gateway

Date: 2026-07-02

## Status

Accepted

## Context

`project-lab-backend` is currently a single Spring Boot application: one GraphQL
schema, one set of REST controllers, one Postgres database, one deployable JAR.
Anticipated non-functional requirements (independent scaling, fault isolation
between domains, independent deployability) may require moving away from this
single-deployable model.

The system is small (5 bounded contexts, single Maven module, single DB) and is a
course project that will nonetheless be deployed to production, so the migration
needs to be real, not just a diagram exercise, while staying within a zero/near-zero
cost budget (see [ADR-0014](0014-deployment-topology-oracle-cloud.md)).

Doing this as a single "big bang" rewrite is high risk: entity relationships
currently span what would become service boundaries (e.g. `Booking` holds live JPA
references to `User` and `Room`), and one query (`StayService.buildSpec()`'s
availability search) does a live SQL join across what would become two separate
databases.

## Decision

Adopt a microservices architecture decomposed by bounded context (see
[ADR-0002](0002-service-boundaries-and-decomposition.md)), fronted by a single
Gateway that preserves the current REST and GraphQL contract for the frontend (see
[ADR-0004](0004-gateway-technology-and-endpoint-preservation.md)).

Migrate using the strangler fig pattern, in this order:

1. **Modularize in place** — reorganize the monolith's packages by bounded context
   and enforce module boundaries with Spring Modulith
   ([ADR-0012](0012-module-boundary-enforcement-spring-modulith.md)) before any
   physical extraction. This is a zero-deployment-risk refactor that surfaces every
   illegal cross-domain reference ahead of time.
2. **Stand up the Gateway** in front of the still-monolithic app, so routing, auth
   validation, and endpoint shape are proven before any service is actually split
   out.
3. **Extract services in order of increasing coupling**: Review and Media first
   (already decoupled or made generic), then Identity (stateless JWT makes this
   low-risk), then Inventory, then Booking last, since it depends on both Identity
   and Inventory and has the hardest cross-domain query to resolve
   ([ADR-0010](0010-booking-inventory-consistency.md)).

## Consequences

- More operational moving parts: multiple JVMs, multiple databases, network calls
  where there were previously in-process method calls.
- Requires a host capable of running several concurrent services
  ([ADR-0014](0014-deployment-topology-oracle-cloud.md)) — the current production
  box cannot do this regardless of code changes.
- In exchange: independent scaling and deployment per service, and fault isolation
  (e.g. Media/upload load no longer affects Booking/GraphQL latency).
- The frontend/client contract is unaffected — this is a hosting/backend
  reorganization, not a client-facing change.
- Each extraction phase is independently testable and revertible, which matters
  more for risk management than raw implementation speed.

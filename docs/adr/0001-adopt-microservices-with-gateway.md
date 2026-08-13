# 1. Adopt microservices architecture with a gateway

Date: 2026-07-02

## Status

Accepted

## Context

`project-lab-backend` is one Spring Boot app: one schema, one DB, one JAR.
Independent scaling/deployability and fault isolation between domains motivate
splitting it up, within a near-zero cost budget ([ADR-0014](0014-deployment-topology-oracle-cloud.md))
and without a "big bang" rewrite — entities already cross future service boundaries
(e.g. `Booking` holds live JPA refs to `User` and `Room`; `StayService.buildSpec()`
joins across what would become two databases).

## Decision

Decompose by bounded context ([ADR-0002](0002-service-boundaries-and-decomposition.md))
behind a single Gateway that preserves the current REST/GraphQL contract
([ADR-0004](0004-gateway-technology-and-endpoint-preservation.md)). Migrate via
strangler fig:

1. **Modularize in place** — reorganize by bounded context, enforce boundaries with
   Spring Modulith ([ADR-0012](0012-module-boundary-enforcement-spring-modulith.md)),
   before any physical extraction.
2. **Stand up the Gateway** in front of the still-monolithic app first, proving
   routing/auth/endpoint shape before any service splits out.
3. **Extract in order of increasing coupling**: Review and Media first (already
   decoupled or generic), then Identity (stateless JWT, low-risk), then Inventory,
   then Booking last (depends on both, plus the hardest cross-domain query —
   [ADR-0010](0010-booking-inventory-consistency.md)).

## Consequences

- More moving parts: multiple JVMs/DBs, network calls replacing in-process ones.
- Needs a host that can run several services concurrently ([ADR-0014](0014-deployment-topology-oracle-cloud.md)).
- Gains independent scaling/deployment and fault isolation per service.
- No frontend/client contract change.
- Each phase is independently testable and revertible.

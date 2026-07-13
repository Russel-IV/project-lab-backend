# 8. Inter-service communication

Date: 2026-07-02

## Status

Accepted

## Context

Once split, services need to call each other synchronously: Gateway → every
domain service (GraphQL resolution and REST proxying), and Inventory → Booking
for availability checks ([ADR-0010](0010-booking-inventory-consistency.md)).
Candidates were hand-rolled `RestTemplate`/`WebClient` calls versus a declarative
client library.

## Decision

Use **Spring Cloud OpenFeign** (`spring-cloud-starter-openfeign`) for all
inter-service calls. Feign clients resolve target services via Eureka
([ADR-0006](0006-service-discovery-eureka.md)) and Spring Cloud LoadBalancer.
Wrap calls with **Resilience4j**
(`spring-cloud-starter-circuitbreaker-resilience4j`) for timeouts, circuit
breaking, and fallbacks — most critically on the Inventory → Booking call in the
availability-search path.

## Consequences

- Declarative clients (`interface` + annotations) instead of manual HTTP plumbing,
  consistent with the "prefer stock Spring behavior over hand-rolled code"
  direction of this migration.
- Every cross-service call is now a network call that can fail independently of
  the caller — Resilience4j configuration (timeout/fallback per client) is not
  optional, it is required wherever a Feign call sits on a request path the
  frontend depends on synchronously.
- Circuit breaker fallbacks need an explicit, documented behavior per call site,
  not just a default — see [ADR-0010](0010-booking-inventory-consistency.md) for
  the one that matters most.

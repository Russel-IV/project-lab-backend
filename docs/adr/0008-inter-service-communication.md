# 8. Inter-service communication

Date: 2026-07-02

## Status

Accepted, with a scoped exception for the Gateway's outbound calls
([ADR-0025](0025-reactive-gateway-webflux-migration.md) — Feign → reactive
`WebClient`). Everything below still applies to every other service, and to the
Gateway's own inbound side.

## Context

Services need to call each other synchronously: Gateway → every domain service, and
Inventory → Booking for availability checks
([ADR-0010](0010-booking-inventory-consistency.md)). Considered hand-rolled
`RestTemplate`/`WebClient` calls versus a declarative client library.

## Decision

Use **Spring Cloud OpenFeign** for all inter-service calls, resolving targets via
Eureka ([ADR-0006](0006-service-discovery-eureka.md)) and Spring Cloud
LoadBalancer. Wrap calls with **Resilience4j** for timeouts, circuit breaking, and
fallbacks — most critically on Inventory → Booking in the availability-search path.

## Consequences

- Declarative clients instead of manual HTTP plumbing.
- Every cross-service call can now fail independently — Resilience4j
  timeout/fallback config is required, not optional, on any call the frontend
  depends on synchronously.
- Circuit breaker fallbacks need an explicit, documented behavior per call site —
  see [ADR-0010](0010-booking-inventory-consistency.md) for the one that matters
  most.

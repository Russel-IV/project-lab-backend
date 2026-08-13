# 10. Booking/Inventory consistency for availability search

Date: 2026-07-02

## Status

Accepted

## Context

`StayService.buildSpec()` runs a correlated SQL subquery joining `room` and
`booking` to exclude stays with no room available for a date range. Once Booking
and Inventory have separate databases, this can't run as-is. Considered:

1. **Synchronous call + circuit breaker** — Inventory calls a Booking
   conflict-check endpoint via Feign, wrapped in Resilience4j.
2. **Event-driven CQRS read model** — Booking publishes events, Inventory
   maintains its own local availability table. Needs a message broker.

## Decision

Synchronous call + circuit breaker. On Booking unavailability/timeout, the
availability filter is **skipped** rather than failing the search — rooms show as
potentially available, and the authoritative check still happens at actual
booking-creation time (already correctness-critical, unaffected by this decision).
A degraded, over-inclusive result beats a failed search.

## Consequences

- Simpler to build/test: no broker, no eventual-consistency edge cases.
- Search availability now depends on Booking's uptime/latency, mitigated by the
  circuit breaker fallback.
- The one place this split changes user-visible behavior under failure: search may
  show rooms that turn out booked (resolved at booking time, not silently wrong).
- Revisit in favor of the CQRS read model if Inventory's search path needs to scale
  or fail independently of Booking — supersede this ADR, don't edit it.

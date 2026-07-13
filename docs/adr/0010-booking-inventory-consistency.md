# 10. Booking/Inventory consistency for availability search

Date: 2026-07-02

## Status

Accepted

## Context

`StayService.buildSpec()` (search filter construction) currently runs a
correlated SQL subquery joining `room` and `booking` in one statement, to exclude
stays with no room available for the requested date range. Once Booking and
Inventory have separate databases, this SQL cannot run as-is.

Two approaches were considered:

1. **Synchronous call + circuit breaker** — Inventory calls a Booking endpoint at
   search time (e.g. `POST /internal/bookings/conflicts` with room IDs + date
   range) via Feign, wrapped in Resilience4j (timeout, circuit breaker, fallback).
2. **Event-driven CQRS read model** — Booking publishes `BookingCreated`/
   `BookingCancelled` events; Inventory (or a dedicated read-model) subscribes and
   maintains its own local availability table, queried natively with no
   cross-service call at request time. Requires a message broker (e.g. RabbitMQ).

## Decision

Synchronous call + circuit breaker. Inventory calls Booking's conflict-check
endpoint at search time via Feign, wrapped in Resilience4j with an explicit
timeout and a defined fallback.

Fallback behavior: on Booking service unavailability or timeout, the availability
filter is **skipped** rather than failing the whole search — rooms are shown as
potentially available, and the authoritative conflict check still happens at
actual booking-creation time (already the correctness-critical point in the
current code, unaffected by this decision). A degraded (over-inclusive) search
result is preferred over a failed search.

## Consequences

- Simpler to build and test within course scope: no message broker, no
  eventual-consistency edge cases to reason about or grade.
- Search availability now depends on Booking service's uptime/latency, mitigated
  but not eliminated by the circuit breaker fallback.
- This is the one place the microservices split changes user-visible behavior
  under failure: search may show rooms that turn out to be booked (resolved at
  booking time, not silently incorrect).
- Revisit in favor of the event-driven CQRS read model (rejected here, not
  discarded) if Inventory's search path needs to scale or fail independently of
  Booking's availability — this ADR should be superseded, not edited, if that
  happens.

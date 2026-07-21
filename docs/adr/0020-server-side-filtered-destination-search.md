# 20. Server-side filtered destination search

Date: 2026-07-20

## Status

Accepted

## Context

`destinations: [Destination!]!` (no args) returns every distinct destination
in one call — correct at 15 rows, but doesn't scale: the frontend would have
to hold and client-filter an ever-growing full list.

## Decision

Add `destinations(search: String, limit: Int = 20): [Destination!]!` as an
additive, non-breaking extension (existing no-arg calls unaffected). Backed
by ADR-0019's trigram index. The existing unfiltered path stays available for
small/cacheable call sites (e.g. `popularDestinations`, ADR-0022).

## Consequences

- Frontend moves from fetch-once-and-filter to debounced (~200-300ms)
  per-keystroke queries with in-flight request cancellation — a frontend
  concern, not tracked in this backend's plan.
- New load pattern (many small queries instead of one) — worth watching via
  existing tracing/metrics (ADR-0013).
- Sets up the query shape ADR-0021 (ranking) and ADR-0023 (fuzzy matching)
  extend.

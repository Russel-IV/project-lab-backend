# 24. Geo-sharding for large-scale geo-radius search

Date: 2026-07-20

## Status

Proposed — not scheduled, no implementation planned

## Context

`Stay.location` is `GEOGRAPHY(POINT, 4326)` with a GIST index (V9), laying
groundwork for a future "search stays near a point / map viewport" feature —
not yet exposed via any query. At real OTA scale, geo search is eventually
partitioned geographically (geohash-prefix sharding, Google S2 cells, Uber
H3, or coarse region/continent sharding — the last often driven as much by
data-residency law, e.g. GDPR, as by performance) so a geo query scans only
the relevant shard(s) instead of one global index.

Note this is orthogonal to destination-name search (ADR-0018 through 0023) —
that's text search over names; this is spatial/proximity search over
coordinates.

## Decision

Do not implement geo-sharding now. This ADR records the analysis and the
trigger conditions for revisiting it:

- (a) a geo-radius/map-bounds feature is actually built — until then,
  PostGIS's existing GIST index handles it natively up to roughly
  single-digit-million-row scale with zero sharding.
- (b) query latency/throughput on that feature demonstrably exceeds what one
  Postgres instance — or a dedicated search engine (Elasticsearch/OpenSearch
  with its own internal geo_point sharding, a lower-effort intermediate step)
  — can serve.
- (c) multi-region/data-residency requirements force physical data
  partitioning independent of query performance.

## Consequences

- None from inaction — a deliberate no-op, documented so the option isn't
  silently forgotten or reinvented without this analysis.
- If triggered, the escalation path is: PostGIS as-is → dedicated search
  engine with built-in sharding → hand-rolled H3/S2/geohash partitioning, in
  that order. Skipping straight to hand-rolled sharding is not recommended.
- Promote to Accepted (or supersede) only once a trigger condition above is
  actually met.

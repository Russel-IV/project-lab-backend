# 19. Trigram index for city search

Date: 2026-07-20

## Status

Accepted

## Context

`city` filtering uses `LIKE '%x%'`. The plain btree `idx_address_city` index
(added alongside the `destinations` query this session) does not accelerate
arbitrary-substring `LIKE` — btree only helps equality, prefix (`LIKE 'x%'`),
and range predicates. Invisible at 15 rows; becomes a full scan as inventory
grows.

## Decision

Enable `pg_trgm`. Replace `idx_address_city` with a GIN trigram index:

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE INDEX idx_address_city_trgm ON address USING GIN (city gin_trgm_ops);
```

This accelerates both `LIKE '%x%'` and `similarity()`-based matching, which
ADR-0021 (ranking) and ADR-0023 (typo tolerance) build on directly.

## Consequences

- Needs `CREATE EXTENSION IF NOT EXISTS pg_trgm` — same pattern already used
  for PostGIS in V9.
- GIN trigram indexes are larger and costlier to update on write than btree;
  acceptable since address writes (new listings) are far rarer than reads.
- Whether the old btree index is worth keeping alongside (for exact-match/
  `ORDER BY city`) is an implementation call, not a decision this ADR needs
  to make — benchmark before dropping it.

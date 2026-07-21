# 21. Destination relevance ranking

Date: 2026-07-20

## Status

Accepted

## Context

`destinations` results are ordered `ORDER BY a.city` — alphabetical, not
relevance. A real ranking should favor prefix matches and popular
destinations over incidental substring hits.

## Decision

Order `destinations(search:)` by: (1) prefix match before mid-string match,
(2) descending `stay_count` per region (a `COUNT` join against `region_id` —
cheap at near-term scale, no separate analytics pipeline needed yet), (3)
alphabetical as tiebreaker.

Explicitly out of scope: per-user personalization and geo-proximity ranking
(needs the requester's location — a separate feature/ADR).

## Consequences

- Query becomes join + count + sort instead of flat `DISTINCT` — still cheap
  at realistic near-term scale (low thousands of regions).
- Ranking logic lives in `DestinationService`, unit-testable independent of
  future ranking sophistication.
- If `stay_count` joins become expensive at real scale, revisit as a
  denormalized counter maintained on stay create/delete — not needed now.

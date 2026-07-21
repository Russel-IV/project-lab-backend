# 22. Curated/popular destinations for the empty-query state

Date: 2026-07-20

## Status

Accepted

## Context

With no typed query, there's nothing to show unless something is picked. No
curation mechanism exists today.

## Decision

Add a nullable `curated_rank INT` column on `region` (no new join table —
simplest shape for this). Add `popularDestinations(limit: Int = 8): [Destination!]!`:
curated rows first (by `curated_rank`), padded with top-`stay_count` regions
(ADR-0021) if fewer than `limit` are explicitly curated, so the feature never
returns an empty/short list.

No ML/personalization — deliberately the simplest version that unblocks a
real empty state.

## Consequences

- Needs an admin/host-facing way to set `curated_rank` — out of scope here,
  a follow-up.
- Frontend swaps its "show everything unfiltered" fallback for this query.
- Degrades gracefully with zero curated rows: falls back entirely to
  popularity ranking.

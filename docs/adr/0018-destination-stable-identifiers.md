# 18. Stable destination identifiers instead of free-text city matching

Date: 2026-07-20

## Status

Accepted

## Context

`StayFilterInput.city` matches via `cb.like(cb.lower(address.get("city")), "%${it.lowercase()}%")`
in `StayService.buildSpec()`. `Address.city` is a plain `TEXT` column, no FK,
no dedup. Two stays sharing a city name in different countries/states would
collide. Current 15-row seed data has no duplicates, masking the bug rather
than proving it doesn't exist.

Options: (a) enforce city-name uniqueness app-side — breaks on first real
duplicate; (b) normalize into a `region` table with surrogate PK, FK from
`Address`; (c) composite natural key (`city|countryCode` string) — still
ambiguous across state/province, not a real FK.

## Decision

(b). Add `region(id, city, country_code, state_province NULL)`,
`Address.region_id` FK. Expose `regionId: Int!` on `Destination`/`Address`.
Add `regionId: Int` to `StayFilterInput` as the preferred filter. Backfill one
`region` row per distinct `(city, country_code)` — same set
`AddressRepository.findDistinctCityCountryPairs()` already computes.

Keep `city`/`countryCode` on `StayFilterInput`, marked `@deprecated`, per this
project's endpoint-preservation practice (ADR-0004).

## Consequences

- Closes the collision bug at the schema level instead of by seed-data luck.
- One extra join (`address` → `region`) when `regionId` is used — negligible
  at foreseeable scale.
- Frontend must switch its committed search value from `destination.city`
  (string) to `destination.regionId` (int) — a frontend-repo change, not
  tracked in this backend's plan, but a direct consequence of this decision.
- `city`/`countryCode` filtering stays available (deprecated) until the
  frontend fully migrates.
- Ranking, fuzzy matching, and curation (ADR-0019, 0021, 0022, 0023) all
  assume `region` exists as a first-class row, not the string it replaces.

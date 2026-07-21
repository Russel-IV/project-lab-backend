# 23. Typo-tolerant fuzzy destination matching

Date: 2026-07-20

## Status

Accepted

## Context

`LIKE '%x%'` returns nothing for "Pariss" or "Zurich" (vs. "Zürich").
ADR-0019's trigram index makes `similarity()` available but doesn't decide
how fuzzy matching should behave.

## Decision

Extend `destinations(search:)` to match on:
`unaccent(lower(city)) ILIKE '%'||unaccent(lower(:search))||'%' OR similarity(city, :search) > 0.3`.
Enable `unaccent` alongside `pg_trgm` — cheaply solves diacritics
("Zurich" → "Zürich") without custom logic. `0.3` is Postgres's commonly-cited
default balance point for `pg_trgm` similarity, tunable without a schema
change.

Explicitly not doing: phonetic matching (Soundex/Metaphone) or
transliteration beyond `unaccent`.

## Consequences

- Needs `CREATE EXTENSION IF NOT EXISTS unaccent`.
- Similarity threshold is a constant, not user-configurable — revisit once
  there's real false-positive/negative data.
- Adds `similarity()` cost per query, acceptable given the trigram index
  (ADR-0019) backing it.

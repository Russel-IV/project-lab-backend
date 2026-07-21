-- Indexed on lower(city), not city, to match StayService.buildSpec()'s actual
-- predicate (`lower(city) LIKE lower(:x)`, i.e. cb.like(cb.lower(...))) — a
-- trigram index only backs a LIKE/ILIKE query whose expression it matches
-- exactly, so indexing plain `city` here would silently never be used.
CREATE INDEX idx_address_city_trgm ON address USING GIN (lower(city) gin_trgm_ops);

-- Benchmarked with EXPLAIN ANALYZE against a 200k-row synthetic address_bench
-- table and against this table with enable_seqscan off (docs/adr/0019):
-- idx_address_city (plain btree) cannot accelerate a `%x%` substring pattern
-- at all — the planner falls back to a seq scan regardless of its presence —
-- while the GIN trigram index above produces a bitmap index scan (~10x
-- faster at 200k rows). No query in this codebase does an equality or prefix
-- match on city that a btree would help, so it's dropped here rather than
-- kept alongside.
DROP INDEX idx_address_city;

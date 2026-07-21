CREATE TABLE region (
    id SERIAL PRIMARY KEY,
    city TEXT NOT NULL,
    country_code CHAR(2) NOT NULL,
    state_province TEXT
);

-- Enforces the dedup invariant ADR-0018 relies on (one region per distinct
-- city/country pair) so concurrent findOrCreate lookups from StayService
-- can't race into duplicate rows the way the old unindexed address.city
-- string comparison could.
CREATE UNIQUE INDEX idx_region_city_country ON region(lower(city), country_code);

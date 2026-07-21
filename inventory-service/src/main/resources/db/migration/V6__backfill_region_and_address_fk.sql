-- One region row per distinct (city, country_code) currently in use — the
-- same set AddressRepository.findDistinctCityCountryPairs() used to compute
-- before DestinationService switched to querying `region` directly.
INSERT INTO region (city, country_code, state_province)
SELECT DISTINCT ON (lower(city), country_code) city, country_code, state_province
FROM address
ORDER BY lower(city), country_code;

ALTER TABLE address ADD COLUMN region_id INT REFERENCES region(id);

UPDATE address a
SET region_id = r.id
FROM region r
WHERE lower(a.city) = lower(r.city) AND a.country_code = r.country_code;

ALTER TABLE address ALTER COLUMN region_id SET NOT NULL;

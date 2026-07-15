-- Review moved to review-service (docs/adr/0002, Phase 2 of the migration plan) with its
-- own database (V1__review_table.sql there). This is a dev/lab environment with no
-- production data to preserve, so this is a straight drop rather than a data-copy
-- migration — a real production cutover would need to backfill review-service's DB
-- from this table before running this migration, not just drop it.
DROP TABLE IF EXISTS review;

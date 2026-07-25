-- Same rationale as identity-service's V3__user_public_id_default.sql: raw-SQL
-- seed inserts (scripts/sql/inventory.sql) omit public_id, so it needs a
-- DB-level default rather than relying solely on app-side Uuid7 generation.
ALTER TABLE stay ALTER COLUMN public_id SET DEFAULT gen_random_uuid();

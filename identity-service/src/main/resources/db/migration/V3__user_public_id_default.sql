-- V2 left public_id with no default: fine for app inserts (Uuid7 always supplies
-- one explicitly), but any raw-SQL insert that omits the column — e.g.
-- scripts/sql/identity.sql — hits the NOT NULL constraint. A DB-level default
-- makes those paths work without threading a value through every INSERT.
ALTER TABLE "user" ALTER COLUMN public_id SET DEFAULT gen_random_uuid();

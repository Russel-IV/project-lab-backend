-- User/Host/Language/PaymentMethod moved to identity-service (docs/adr/0002,
-- docs/adr/0011, Phase 4 of the migration plan). Unlike prior extractions, tables
-- that STAY here (stay, booking) have FK constraints pointing INTO the tables being
-- dropped, so those constraints must go first, then the tables in dependency order
-- (leaf tables before the ones they reference). Same pragmatic dev/lab cutover as
-- V18/V19: no production data to preserve, so straight drops rather than a
-- data-copy migration.
ALTER TABLE stay DROP CONSTRAINT fk_stay_host;
ALTER TABLE booking DROP CONSTRAINT fk_booking_user;

-- Orphaned/unmapped table (no JPA entity anywhere referenced it, even before this
-- phase) with FKs into both "user" and stay — must go before "user" can be dropped.
DROP TABLE IF EXISTS user_favorite;

DROP TABLE IF EXISTS payment_method;
DROP TABLE IF EXISTS host_language;
DROP TABLE IF EXISTS host;
DROP TABLE IF EXISTS language;
DROP TABLE IF EXISTS "user";

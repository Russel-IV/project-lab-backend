-- Booking and booking_room moved to booking-service (docs/adr/0002, docs/adr/0010,
-- docs/adr/0011, Phase 6 of the migration plan). booking_room's FK into room was
-- already dropped in V21 (Phase 5); its remaining FK is into booking itself, which
-- goes away when the table is dropped (ON DELETE CASCADE), so no separate
-- "constraints before tables" step is needed here, unlike V20/V21. Same pragmatic
-- dev/lab cutover as V18/V19/V20/V21: no production data to preserve, so straight
-- drops rather than a data-copy migration.
DROP TABLE IF EXISTS booking_room;
DROP TABLE IF EXISTS booking;

-- Stay/Room/Address/PropertyBrand and the six lookup tables moved to
-- inventory-service (docs/adr/0002, docs/adr/0010, Phase 5 of the migration plan).
-- booking_room (which stays here — Booking isn't extracted until Phase 6) has an FK
-- into room that must go first, same "constraints before tables" pattern V20 used for
-- stay/booking's FKs into the identity tables it dropped. Same pragmatic dev/lab
-- cutover as V18/V19/V20: no production data to preserve, so straight drops rather
-- than a data-copy migration.
ALTER TABLE booking_room DROP CONSTRAINT fk_br_room;

DROP TABLE IF EXISTS stay_view;
DROP TABLE IF EXISTS stay_amenity;
DROP TABLE IF EXISTS stay_accessibility;
DROP TABLE IF EXISTS stay_meal_plan;
DROP TABLE IF EXISTS stay_payment_type;
DROP TABLE IF EXISTS stay_traveler_experience;
DROP TABLE IF EXISTS room;
DROP TABLE IF EXISTS stay;
DROP TABLE IF EXISTS address;
DROP TABLE IF EXISTS view;
DROP TABLE IF EXISTS amenity;
DROP TABLE IF EXISTS accessibility;
DROP TABLE IF EXISTS meal_plan;
DROP TABLE IF EXISTS payment_type;
DROP TABLE IF EXISTS property_brand;
DROP TABLE IF EXISTS traveler_experience;

-- ============================================================
-- INVENTORY-SERVICE SEED DATA (inventory-database)
-- ============================================================
-- Tables: view, amenity, accessibility, meal_plan, payment_type,
-- property_brand, traveler_experience, region, address, stay, room, plus
-- the six stay-attribute bridge tables and room_amenity (room-level, not a
-- stay attribute, but seeded here right after each ROOMS section since it
-- references room ids from that section).
-- Safe to run multiple times: all inserts use explicit IDs with
-- ON CONFLICT DO NOTHING. Sequences are reset after each table.
--
-- 48 stays across 6 continents, 75 rooms. stay.host_id values (1, 3, 4,
-- 8, 10, 12, 15, 18) reference identity-service's seeded host ids —
-- there is no database-level FK across services anymore, so
-- scripts/sql/identity.sql MUST run before this fragment.
--
-- Free rooms with no bookings at all (see booking.sql): 3 (Deluxe Suite),
-- 7 (Superior Suite).
--
-- Section 5 adds 20 more stays (ids 16-35, rooms 27-57) as a second seed
-- batch — same reused host ids, lookup rows, and scripts/images/ photos as
-- above, but deliberately with no matching rows in booking.sql/review.sql
-- (those two stay purely additive against stays 1-15).
--
-- Section 8 adds 13 more stays (ids 36-48, rooms 58-75) as a third batch,
-- reusing existing region rows from sections 2 and 5 (same city/country)
-- instead of introducing new cities, to give the city distribution a
-- realistic density skew — most cities host exactly one stay, but a
-- handful host several. Each stay still needs its own address row (a
-- distinct street address, new id) since stay.address_id is UNIQUE — one
-- stay per address is enforced at the schema level (V1__inventory_tables.sql),
-- so unlike section 5 this batch breaks section 2's "address id N == region
-- id N" 1:1 assumption on purpose: several new address rows point at the
-- same region_id. Distribution after this batch: Valparaíso (region 3) has
-- 5 stays, Tokyo (region 2) and Barcelona (region 11) have 4 each, Paris
-- (region 4) has 3, Amsterdam (region 15) has 2, every other city still
-- has 1. Like section 5, these are additive-only — no matching rows in
-- booking.sql/review.sql.
-- ============================================================


-- ============================================================
-- 1. LOOKUP TABLES
-- ============================================================

INSERT INTO view (id, view_type) VALUES
(1, 'Ocean View'),
(2, 'Mountain View'),
(3, 'City Skyline'),
(4, 'Garden View'),
(5, 'Pool View'),
(6, 'Forest View'),
(7, 'Lake View'),
(8, 'Vineyard View'),
(9, 'Desert View')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('view', 'id'), COALESCE(MAX(id), 1)) FROM view;

INSERT INTO amenity (id, name, type) VALUES
(1,  'High-Speed Wi-Fi',        'PROPERTY_AMENITY'),
(2,  'Air Conditioning',        'ROOM_AMENITY'),
(3,  'Private Pool',            'PROPERTY_AMENITY'),
(4,  'Fully Equipped Kitchen',  'ROOM_AMENITY'),
(5,  'Washing Machine',         'PROPERTY_AMENITY'),
(6,  'Gym Access',              'PROPERTY_AMENITY'),
(7,  'Balcony',                 'ROOM_AMENITY'),
(8,  'Hot Tub',                 'PROPERTY_AMENITY'),
(9,  'Fireplace',               'ROOM_AMENITY'),
(10, 'Breakfast Bar',           'PROPERTY_AMENITY'),
(11, 'EV Charger',              'PROPERTY_AMENITY'),
(12, 'Sauna',                   'ROOM_AMENITY'),
(13, 'Rooftop Terrace',         'PROPERTY_AMENITY'),
(14, 'Soundproofing',           'ROOM_AMENITY'),
(15, 'Ski Storage',             'PROPERTY_AMENITY')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('amenity', 'id'), COALESCE(MAX(id), 1)) FROM amenity;

INSERT INTO accessibility (id, accessibility_type) VALUES
(1, 'Wheelchair Accessible Path'),
(2, 'Step-Free Bedroom'),
(3, 'Elevator Available'),
(4, 'Accessible Parking'),
(5, 'Wide Doorways')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('accessibility', 'id'), COALESCE(MAX(id), 1)) FROM accessibility;

INSERT INTO meal_plan (id, meal_plan_type) VALUES
(1, 'Room Only'),
(2, 'Breakfast Included'),
(3, 'Half Board'),
(4, 'All Inclusive'),
(5, 'Full Board')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('meal_plan', 'id'), COALESCE(MAX(id), 1)) FROM meal_plan;

INSERT INTO payment_type (id, payment_type) VALUES
(1, 'Credit Card'),
(2, 'PayPal'),
(3, 'Cryptocurrency'),
(4, 'Bank Transfer'),
(5, 'Cash')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('payment_type', 'id'), COALESCE(MAX(id), 1)) FROM payment_type;

INSERT INTO property_brand (id, brand_name) VALUES
(1, 'Independent'),
(2, 'Hilton Hotels'),
(3, 'Marriott International'),
(4, 'Hyatt Regency'),
(5, 'Four Seasons'),
(6, 'Accor Hotels'),
(7, 'Best Western'),
(8, 'Soho House')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('property_brand', 'id'), COALESCE(MAX(id), 1)) FROM property_brand;

INSERT INTO traveler_experience (id, traveler_experience_type) VALUES
(1, 'Family Friendly'),
(2, 'Romantic Getaway'),
(3, 'Business Travel'),
(4, 'Adventure & Nature'),
(5, 'Backpacker Approved'),
(6, 'Wellness & Spa'),
(7, 'Solo Traveler'),
(8, 'Pet Friendly')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('traveler_experience', 'id'), COALESCE(MAX(id), 1)) FROM traveler_experience;


-- ============================================================
-- 2. REGIONS, ADDRESSES & STAYS
-- ============================================================

-- One row per distinct (city, country_code) below (docs/adr/0018) — id N here
-- always matches address id N, since this seed set has no two addresses
-- sharing a city/country pair.
INSERT INTO region (id, city, country_code, state_province) VALUES
(1,  'Miami',         'US', 'Florida'),
(2,  'Tokyo',         'JP', 'Tokyo'),
(3,  'Valparaíso',    'CL', 'Valparaíso'),
(4,  'Paris',         'FR', 'Île-de-France'),
(5,  'Ubud',          'ID', 'Bali'),
(6,  'Oia',           'GR', 'South Aegean'),
(7,  'New York',      'US', 'New York'),
(8,  'Isle of Skye',  'GB', 'Highland'),
(9,  'Marrakech',     'MA', 'Marrakech-Safi'),
(10, 'Zermatt',       'CH', 'Valais'),
(11, 'Barcelona',     'ES', 'Catalonia'),
(12, 'Cape Town',     'ZA', 'Western Cape'),
(13, 'Sydney',        'AU', 'New South Wales'),
(14, 'Reykjavik',     'IS', 'Capital Region'),
(15, 'Amsterdam',     'NL', 'North Holland')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('region', 'id'), COALESCE(MAX(id), 1)) FROM region;

INSERT INTO address (id, street_address, extended_address, city, state_province, postal_code, country_code, region_id) VALUES
(1,  '123 Ocean Drive',       'Apt 4B',      'Miami',         'Florida',         '33139',    'US', 1),
(2,  '4-56 Shinjuku',         'Floor 32',    'Tokyo',         'Tokyo',           '160-0022', 'JP', 2),
(3,  '789 Alpine Way',        NULL,          'Valparaíso',    'Valparaíso',      '2340000',  'CL', 3),
(4,  '12 Rue de Rivoli',      '3ème étage',  'Paris',         'Île-de-France',   '75001',    'FR', 4),
(5,  'Jl. Monkey Forest 88',  NULL,          'Ubud',          'Bali',            '80571',    'ID', 5),
(6,  'Oia Cliffside Path',    NULL,          'Oia',           'South Aegean',    '84702',    'GR', 6),
(7,  '350 5th Avenue',        'Suite 2100',  'New York',      'New York',        '10118',    'US', 7),
(8,  'Uig Road',              NULL,          'Isle of Skye',  'Highland',        'IV51 9XY', 'GB', 8),
(9,  'Derb El Hammam 12',     NULL,          'Marrakech',     'Marrakech-Safi',  '40000',    'MA', 9),
(10, 'Bahnhofstrasse 25',     NULL,          'Zermatt',       'Valais',          '3920',     'CH', 10),
(11, 'Passeig de Gràcia 45',  '2º 1ª',       'Barcelona',     'Catalonia',       '08007',    'ES', 11),
(12, 'Victoria Road',         NULL,          'Cape Town',     'Western Cape',    '8005',     'ZA', 12),
(13, '61 Macquarie Street',   NULL,          'Sydney',        'New South Wales', '2000',     'AU', 13),
(14, 'Laugavegur 7',          NULL,          'Reykjavik',     'Capital Region',  '101',      'IS', 14),
(15, 'Prinsengracht 263',     NULL,          'Amsterdam',     'North Holland',   '1016 GV',  'NL', 15)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('address', 'id'), COALESCE(MAX(id), 1)) FROM address;

INSERT INTO stay (
    id, name, about, property_type, is_refundable,
    star_rating, days_from_booking_cancellation_deadline,
    policies_text, important_information,
    host_id, address_id, property_brand_id, location
) VALUES
(1,
    'Cozy Beachfront House',
    'Beautiful house right on the shore. Perfect for families and couples.',
    'HOME', true, 4.5, 5,
    'No pets. Quiet hours 10 PM–8 AM.',
    'Check-in after 3 PM. Beach towels provided.',
    1, 1, 1,
    ST_GeogFromText('SRID=4326;POINT(-80.1918 25.7617)')),   -- Miami, FL
(2,
    'Luxury Tokyo Sky Hotel',
    'High-rise hotel overlooking the city lights of Shinjuku.',
    'HOTEL', false, 5.0, 2,
    'No smoking. No parties.',
    'Passport required at check-in. Concierge available 24/7.',
    3, 2, 3,
    ST_GeogFromText('SRID=4326;POINT(139.7000 35.6942)')),   -- Tokyo, JP
(3,
    'Charming Mountain Cabin',
    'A quiet retreat surrounded by pine forests. Ideal for nature lovers.',
    'HOME', true, 4.0, 7,
    'No loud music after 10 PM.',
    'Bring warm clothes. Firewood is provided.',
    4, 3, 1,
    ST_GeogFromText('SRID=4326;POINT(-71.6127 -33.0472)')),  -- Valparaíso, CL
(4,
    'Parisian Boutique Hotel',
    'Elegant 19th-century building steps from the Louvre.',
    'HOTEL', true, 4.8, 3,
    'No smoking. No pets.',
    'Breakfast served 7–10 AM. Late checkout upon request.',
    1, 4, 2,
    ST_GeogFromText('SRID=4326;POINT(2.3522 48.8566)')),     -- Paris, FR
(5,
    'Bali Jungle Retreat',
    'Private villa surrounded by rice paddies and tropical jungle.',
    'HOME', false, 4.3, 14,
    'Respect local customs. No loud music.',
    'Airport transfer available. Pool heated on request.',
    3, 5, 1,
    ST_GeogFromText('SRID=4326;POINT(115.2625 -8.5069)')),   -- Ubud, Bali, ID
(6,
    'Santorini Cliffside Villa',
    'Whitewashed villa perched above the caldera with unforgettable sunset views.',
    'HOME', true, 4.7, 10,
    'No parties. No smoking indoors.',
    'Steep steps to the entrance, not suitable for limited mobility. Sunset terrace included.',
    8, 6, 1,
    ST_GeogFromText('SRID=4326;POINT(25.3760 36.4614)')),    -- Oia, Santorini, GR
(7,
    'Manhattan Skyline Hotel',
    'Modern high-rise hotel in the heart of Midtown, steps from the Empire State Building.',
    'HOTEL', false, 4.6, 2,
    'No smoking. No pets.',
    'Valet parking available. Rooftop bar open until 1 AM.',
    10, 7, 3,
    ST_GeogFromText('SRID=4326;POINT(-73.9857 40.7484)')),   -- New York, US
(8,
    'Scottish Highlands Cottage',
    'Stone cottage on the Isle of Skye with panoramic views of the Cuillin mountains.',
    'HOME', true, 4.4, 7,
    'No loud music after 9 PM.',
    'Wellies and rain jackets provided. Nearest shop is a 10-minute drive.',
    4, 8, 1,
    ST_GeogFromText('SRID=4326;POINT(-6.2489 57.2720)')),    -- Isle of Skye, GB
(9,
    'Marrakech Riad Retreat',
    'Traditional riad in the medina with a central courtyard, plunge pool, and rooftop terrace.',
    'HOME', false, 4.5, 5,
    'No alcohol on premises. Respect quiet hours.',
    'Airport pickup included. Staff on-site during the day.',
    12, 9, 1,
    ST_GeogFromText('SRID=4326;POINT(-7.9811 31.6295)')),    -- Marrakech, MA
(10,
    'Swiss Alpine Chalet',
    'Timber chalet with direct ski-in/ski-out access to the Matterhorn glacier paradise.',
    'HOME', true, 4.9, 14,
    'No pets. Ski equipment must be stored in the boot room.',
    'Heated boot room provided. Private chef available on request.',
    15, 10, 1,
    ST_GeogFromText('SRID=4326;POINT(7.7491 46.0207)')),     -- Zermatt, CH
(11,
    'Barcelona Boutique Hotel',
    'Modernist building on Passeig de Gràcia, blending Art Nouveau charm with modern comfort.',
    'HOTEL', true, 4.5, 3,
    'No smoking. No parties.',
    'Breakfast served 7–11 AM. Rooftop pool open seasonally.',
    18, 11, 8,
    ST_GeogFromText('SRID=4326;POINT(2.1734 41.3851)')),     -- Barcelona, ES
(12,
    'Cape Town Ocean Lodge',
    'Lodge on the Atlantic Seaboard with direct beach access and Table Mountain views.',
    'HOME', false, 4.3, 7,
    'No loud music after 10 PM. No smoking indoors.',
    'Backup generator on site. Beach gear provided.',
    8, 12, 1,
    ST_GeogFromText('SRID=4326;POINT(18.4241 -33.9249)')),   -- Cape Town, ZA
(13,
    'Sydney Harbour Hotel',
    'Waterfront hotel with sweeping views of the Opera House and Harbour Bridge.',
    'HOTEL', false, 4.7, 2,
    'No smoking. No parties.',
    'Ferry terminal 5 minutes on foot. Harbour view rooms subject to availability.',
    10, 13, 4,
    ST_GeogFromText('SRID=4326;POINT(151.2093 -33.8688)')),  -- Sydney, AU
(14,
    'Reykjavik Northern Lights Cabin',
    'Glass-roofed cabin outside the city, built for aurora viewing on clear winter nights.',
    'HOME', true, 4.6, 5,
    'No smoking indoors. Respect quiet hours after 11 PM.',
    'Thermal hot tub on the deck. Aurora wake-up call available on request.',
    12, 14, 1,
    ST_GeogFromText('SRID=4326;POINT(-21.9426 64.1466)')),   -- Reykjavik, IS
(15,
    'Amsterdam Canal House Hotel',
    '17th-century canal house converted into a boutique hotel overlooking the Prinsengracht.',
    'HOTEL', true, 4.4, 3,
    'No smoking. Narrow stairs, limited elevator access.',
    'Bicycle rental available at reception. Breakfast included.',
    15, 15, 6,
    ST_GeogFromText('SRID=4326;POINT(4.9041 52.3676)'))      -- Amsterdam, NL
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('stay', 'id'), COALESCE(MAX(id), 1)) FROM stay;


-- ============================================================
-- 3. ROOMS
-- ============================================================

INSERT INTO room (id, stay_id, name, price, sleeps, bedroom_amount, bathrooms, size) VALUES
-- Stay 1 — HOME: one room represents the whole property
(1,  1,  'Beachfront Suite',           120.50, 4, 2, 1.5,  85.0),
-- Stay 2 — HOTEL: three independently bookable rooms
(2,  2,  'Standard King',              350.00, 2, 1, 1.0,  45.5),
(3,  2,  'Deluxe Suite',               550.00, 4, 2, 2.0,  75.0),   -- never booked (free for availableRooms tests)
(4,  2,  'Executive Penthouse',       1200.00, 2, 1, 2.0,  90.0),
-- Stay 3 — HOME
(5,  3,  'Mountain Loft',               85.00, 6, 3, 2.5, 120.0),
-- Stay 4 — HOTEL: two rooms
(6,  4,  'Classic Double',             220.00, 2, 1, 1.0,  30.0),
(7,  4,  'Superior Suite',             480.00, 3, 2, 2.0,  60.0),   -- never booked (free for availableRooms tests)
-- Stay 5 — HOME
(8,  5,  'Jungle Pool Villa',          175.00, 2, 1, 1.0,  55.0),
-- Stay 6 — HOME
(9,  6,  'Cliffside Suite',            310.00, 4, 2, 2.0,  70.0),
-- Stay 7 — HOTEL: three rooms
(10, 7,  'City View Queen',            280.00, 2, 1, 1.0,  32.0),
(11, 7,  'Skyline King',               420.00, 2, 1, 1.5,  42.0),
(12, 7,  'Penthouse Loft',             950.00, 4, 2, 2.5,  95.0),
-- Stay 8 — HOME
(13, 8,  'Highland Cottage',           150.00, 5, 3, 2.0, 100.0),
-- Stay 9 — HOME: two suites
(14, 9,  'Riad Suite',                 140.00, 3, 1, 1.0,  40.0),
(15, 9,  'Rooftop Suite',              210.00, 4, 2, 2.0,  55.0),
-- Stay 10 — HOME
(16, 10, 'Alpine Chalet',              480.00, 8, 4, 3.0, 150.0),
-- Stay 11 — HOTEL: three rooms
(17, 11, 'Gothic Quarter Double',      190.00, 2, 1, 1.0,  28.0),
(18, 11, 'Passeig de Gràcia Suite',    260.00, 3, 1, 1.5,  38.0),
(19, 11, 'Rooftop Terrace Room',       300.00, 2, 1, 1.0,  35.0),
-- Stay 12 — HOME
(20, 12, 'Ocean Lodge Room',           165.00, 4, 2, 1.5,  60.0),
-- Stay 13 — HOTEL: three rooms
(21, 13, 'Harbour View Twin',          220.00, 2, 1, 1.0,  30.0),
(22, 13, 'Opera House King',           340.00, 2, 1, 1.5,  38.0),
(23, 13, 'Executive Suite',            600.00, 4, 2, 2.0,  65.0),
-- Stay 14 — HOME
(24, 14, 'Aurora Cabin',               210.00, 4, 2, 1.5,  65.0),
-- Stay 15 — HOTEL: two rooms
(25, 15, 'Canal View Double',          240.00, 2, 1, 1.0,  25.0),
(26, 15, 'Canal House Suite',          380.00, 3, 1, 1.5,  40.0)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('room', 'id'), COALESCE(MAX(id), 1)) FROM room;

-- Room-level amenities: the ROOM_AMENITY-typed subset of each stay's
-- stay_amenity rows below, attached to one of that stay's actual rooms
-- (for a multi-room stay, split across rooms rather than all on one) so
-- StayFilterInput.roomAmenityIds' "somewhere on the property" semantics
-- (StayService.hasAllRoomAmenities) has something real to aggregate over.
INSERT INTO room_amenity (room_id, amenity_id) VALUES
(1, 2), (1, 4),   -- Stay 1
(2, 2),           -- Stay 2
(5, 4), (5, 9),   -- Stay 3
(6, 2),           -- Stay 4
(8, 4),           -- Stay 5
(10, 2), (11, 14),-- Stay 7 (split across rooms)
(13, 4), (13, 9), -- Stay 8
(16, 4), (16, 9), (16, 12), -- Stay 10
(17, 2),          -- Stay 11
(20, 7),          -- Stay 12
(21, 2),          -- Stay 13
(24, 9),          -- Stay 14
(25, 2)           -- Stay 15
ON CONFLICT DO NOTHING;


-- ============================================================
-- 4. STAY ATTRIBUTES  (bridge tables)
-- ============================================================

INSERT INTO stay_view (stay_id, view_id) VALUES
(1, 1),           -- Beachfront: Ocean View
(2, 3),           -- Tokyo hotel: City Skyline
(3, 2),           -- Cabin: Mountain View
(4, 4),           -- Paris: Garden View
(5, 5), (5, 6),   -- Bali: Pool View + Forest View
(6, 1), (6, 8),   -- Santorini: Ocean View + Vineyard View
(7, 3),           -- Manhattan: City Skyline
(8, 2), (8, 6),   -- Skye: Mountain View + Forest View
(9, 4),           -- Marrakech: Garden View
(10, 2),          -- Zermatt: Mountain View
(11, 3),          -- Barcelona: City Skyline
(12, 1),          -- Cape Town: Ocean View
(13, 1),          -- Sydney: Ocean View (harbour)
(14, 2),          -- Reykjavik: Mountain View
(15, 7)           -- Amsterdam: Lake View (canal)
ON CONFLICT DO NOTHING;

INSERT INTO stay_amenity (stay_id, amenity_id) VALUES
(1, 1), (1, 2), (1, 4),
(2, 1), (2, 2), (2, 6), (2, 8),
(3, 1), (3, 4), (3, 5), (3, 9),
(4, 1), (4, 2), (4, 10),
(5, 1), (5, 3), (5, 4), (5, 8),
(6, 1), (6, 3), (6, 8), (6, 13),
(7, 1), (7, 2), (7, 6), (7, 13), (7, 14),
(8, 4), (8, 9),
(9, 1), (9, 3), (9, 10), (9, 13),
(10, 4), (10, 9), (10, 12), (10, 15),
(11, 1), (11, 2), (11, 3), (11, 13),
(12, 1), (12, 7), (12, 11),
(13, 1), (13, 2), (13, 6), (13, 13),
(14, 8), (14, 9),
(15, 1), (15, 2), (15, 10)
ON CONFLICT DO NOTHING;

INSERT INTO stay_accessibility (stay_id, accessibility_id) VALUES
(1, 1),
(2, 1), (2, 3), (2, 4),
(4, 1), (4, 3),
(5, 1),
(7, 1), (7, 3), (7, 4),
(11, 1), (11, 3),
(12, 4),
(13, 1), (13, 3), (13, 4)
ON CONFLICT DO NOTHING;

INSERT INTO stay_meal_plan (stay_id, meal_plan_id) VALUES
(1, 1),
(2, 1), (2, 2), (2, 3),
(3, 1),
(4, 1), (4, 2),
(5, 1), (5, 4),
(6, 1), (6, 2),
(7, 1), (7, 2), (7, 3),
(8, 1),
(9, 2), (9, 4),
(10, 1), (10, 5),
(11, 1), (11, 2),
(12, 1), (12, 2),
(13, 1), (13, 2), (13, 3),
(14, 1),
(15, 1), (15, 2)
ON CONFLICT DO NOTHING;

INSERT INTO stay_payment_type (stay_id, payment_type_id) VALUES
(1, 1), (1, 2),
(2, 1), (2, 4),
(3, 1), (3, 3),
(4, 1), (4, 2), (4, 4),
(5, 1), (5, 5),
(6, 1), (6, 2),
(7, 1), (7, 2), (7, 4),
(8, 1), (8, 5),
(9, 1), (9, 5),
(10, 1), (10, 4),
(11, 1), (11, 2), (11, 4),
(12, 1), (12, 5),
(13, 1), (13, 2), (13, 4),
(14, 1), (14, 5),
(15, 1), (15, 2)
ON CONFLICT DO NOTHING;

INSERT INTO stay_traveler_experience (stay_id, traveler_experience_id) VALUES
(1, 1), (1, 2),
(2, 3),
(3, 4), (3, 5),
(4, 2), (4, 3),
(5, 2), (5, 4), (5, 6),
(6, 2), (6, 6),
(7, 3), (7, 7),
(8, 4), (8, 5),
(9, 2), (9, 6),
(10, 1), (10, 4),
(11, 2), (11, 3),
(12, 1), (12, 4),
(13, 2), (13, 3),
(14, 2), (14, 4),
(15, 2), (15, 7)
ON CONFLICT DO NOTHING;


-- ============================================================
-- 5. ADDITIONAL STAYS — SEED BATCH 2 (20 more stays, ids 16-35)
-- ============================================================
-- Same shape as section 2-4 above: reuses the lookup rows from section 1
-- and the host ids from identity.sql, no new bookings/reviews (those
-- fragments only reference stays 1-15).

INSERT INTO region (id, city, country_code, state_province) VALUES
(16, 'Lisbon',         'PT', 'Lisbon'),
(17, 'Kyoto',          'JP', 'Kyoto'),
(18, 'Buenos Aires',   'AR', 'Buenos Aires'),
(19, 'Vienna',         'AT', 'Vienna'),
(20, 'Dubai',          'AE', 'Dubai'),
(21, 'Singapore',      'SG', 'Singapore'),
(22, 'Cusco',          'PE', 'Cusco'),
(23, 'Prague',         'CZ', 'Prague'),
(24, 'Bangkok',        'TH', 'Bangkok'),
(25, 'Copenhagen',     'DK', 'Capital Region'),
(26, 'Nairobi',        'KE', 'Nairobi'),
(27, 'Rio de Janeiro', 'BR', 'Rio de Janeiro'),
(28, 'Seoul',          'KR', 'Seoul'),
(29, 'Vancouver',      'CA', 'British Columbia'),
(30, 'Edinburgh',      'GB', 'Scotland'),
(31, 'Hanoi',          'VN', 'Hanoi'),
(32, 'Budapest',       'HU', 'Budapest'),
(33, 'Queenstown',     'NZ', 'Otago'),
(34, 'Havana',         'CU', 'Havana'),
(35, 'Kraków',         'PL', 'Lesser Poland')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('region', 'id'), COALESCE(MAX(id), 1)) FROM region;

INSERT INTO address (id, street_address, extended_address, city, state_province, postal_code, country_code, region_id) VALUES
(16, 'Rua de São Miguel 12',      NULL,          'Lisbon',         'Lisbon',            '1100-542',   'PT', 16),
(17, '4-2 Gion-machi',            NULL,          'Kyoto',          'Kyoto',             '605-0074',   'JP', 17),
(18, 'Honduras 4750',             'Piso 2',      'Buenos Aires',   'Buenos Aires',      'C1414',      'AR', 18),
(19, 'Kärntner Ring 8',           NULL,          'Vienna',         'Vienna',            '1010',       'AT', 19),
(20, 'Crescent Road, Palm Jumeirah', NULL,       'Dubai',          'Dubai',             '00000',      'AE', 20),
(21, '10 Bayfront Avenue',        NULL,          'Singapore',      'Singapore',         '018956',     'SG', 21),
(22, 'Calle Choquechaca 220',     NULL,          'Cusco',          'Cusco',             '08002',      'PE', 22),
(23, 'Staroměstské náměstí 5',    NULL,          'Prague',         'Prague',            '110 00',     'CZ', 23),
(24, '99 Soi Charoen Krung',      NULL,          'Bangkok',        'Bangkok',           '10500',      'TH', 24),
(25, 'Nyhavn 18',                 NULL,          'Copenhagen',     'Capital Region',    '1051',       'DK', 25),
(26, 'Marula Lane, Karen',        NULL,          'Nairobi',        'Nairobi',           '00502',      'KE', 26),
(27, 'Avenida Atlântica 1702',    'Apt 501',     'Rio de Janeiro', 'Rio de Janeiro',    '22021-001',  'BR', 27),
(28, '429 Gangnam-daero',         NULL,          'Seoul',          'Seoul',             '06120',      'KR', 28),
(29, '1750 Beach Avenue',         NULL,          'Vancouver',      'British Columbia',  'V6G 1Z9',    'CA', 29),
(30, '145 Royal Mile',            NULL,          'Edinburgh',      'Scotland',          'EH1 1SG',    'GB', 30),
(31, '18 Hàng Bạc',               NULL,          'Hanoi',          'Hanoi',             '100000',     'VN', 31),
(32, 'Váci utca 22',              NULL,          'Budapest',       'Budapest',          '1052',       'HU', 32),
(33, '88 Lake Esplanade',         NULL,          'Queenstown',     'Otago',             '9300',       'NZ', 33),
(34, 'Calle Obispo 154',          NULL,          'Havana',         'Havana',            '10100',      'CU', 34),
(35, 'Rynek Główny 9',            NULL,          'Kraków',         'Lesser Poland',     '31-042',     'PL', 35)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('address', 'id'), COALESCE(MAX(id), 1)) FROM address;

INSERT INTO stay (
    id, name, about, property_type, is_refundable,
    star_rating, days_from_booking_cancellation_deadline,
    policies_text, important_information,
    host_id, address_id, property_brand_id, location
) VALUES
(16,
    'Alfama Hillside House',
    'Traditional tiled house tucked into the narrow lanes of Alfama, with a private terrace overlooking the Tagus.',
    'HOME', true, 4.4, 5,
    'No smoking indoors. Quiet hours 11 PM–8 AM.',
    'Steep cobblestone streets nearby; not ideal for heavy luggage. Fado bar recommendations from the host.',
    1, 16, 1,
    ST_GeogFromText('SRID=4326;POINT(-9.1393 38.7223)')),     -- Lisbon, PT
(17,
    'Kyoto Machiya Hotel',
    'Restored machiya townhouse hotel steps from Gion, blending traditional wood interiors with modern comfort.',
    'HOTEL', false, 4.6, 3,
    'No smoking. Indoor slippers required.',
    'Front desk staffed 7 AM–11 PM. Kimono rental available on-site.',
    3, 17, 2,
    ST_GeogFromText('SRID=4326;POINT(135.7681 35.0116)')),    -- Kyoto, JP
(18,
    'Palermo Soho Loft',
    'Airy loft in the heart of Palermo Soho, surrounded by street art, cafés, and tango bars.',
    'HOME', true, 4.2, 7,
    'No pets. No parties.',
    'Building has no elevator (3rd floor walk-up). Weekly tango lesson recommendations from host.',
    4, 18, 1,
    ST_GeogFromText('SRID=4326;POINT(-58.3816 -34.6037)')),   -- Buenos Aires, AR
(19,
    'Vienna Ringstrasse Hotel',
    'Grand 19th-century hotel on the Ringstrasse, moments from the State Opera and Hofburg Palace.',
    'HOTEL', true, 4.7, 3,
    'No smoking. No pets.',
    'Breakfast served 6:30–10:30 AM. Formal dress requested in the restaurant after 6 PM.',
    8, 19, 3,
    ST_GeogFromText('SRID=4326;POINT(16.3738 48.2082)')),     -- Vienna, AT
(20,
    'Palm Jumeirah Sky Hotel',
    'Ultra-modern tower on the Palm with private beach access and floor-to-ceiling views of the Gulf.',
    'HOTEL', false, 4.9, 2,
    'No smoking. No outside alcohol.',
    'Valet parking included. Beach club open until midnight.',
    10, 20, 5,
    ST_GeogFromText('SRID=4326;POINT(55.2708 25.2048)')),     -- Dubai, AE
(21,
    'Marina Bay Skyline Hotel',
    'High-rise hotel with an infinity pool overlooking Marina Bay and the Gardens by the Bay.',
    'HOTEL', true, 4.8, 3,
    'No smoking. No pets.',
    'Rooftop pool open 6 AM–10 PM. Airport shuttle available on request.',
    12, 21, 4,
    ST_GeogFromText('SRID=4326;POINT(103.8198 1.3521)')),     -- Singapore, SG
(22,
    'Andean Courtyard House',
    'Colonial courtyard house near the Plaza de Armas, a short walk from the trailhead to Sacsayhuamán.',
    'HOME', true, 4.3, 7,
    'No smoking indoors. Respect quiet hours after 10 PM.',
    'High altitude — coca tea provided on arrival. Oxygen tank available on request.',
    15, 22, 1,
    ST_GeogFromText('SRID=4326;POINT(-71.9675 -13.5319)')),   -- Cusco, PE
(23,
    'Old Town Boutique Hotel',
    'Boutique hotel in a restored Baroque building steps from the Astronomical Clock.',
    'HOTEL', true, 4.5, 3,
    'No smoking. No parties.',
    'Breakfast served 7–10 AM. Cobblestone entrance, not stroller-friendly.',
    18, 23, 7,
    ST_GeogFromText('SRID=4326;POINT(14.4378 50.0755)')),     -- Prague, CZ
(24,
    'Chao Phraya Riverside House',
    'Teak house on stilts along the Chao Phraya river, with a private longtail boat dock.',
    'HOME', false, 4.1, 5,
    'No pets. Remove shoes indoors.',
    'Boat transfer to nearest pier included. Air conditioning in bedroom only.',
    1, 24, 1,
    ST_GeogFromText('SRID=4326;POINT(100.5018 13.7563)')),    -- Bangkok, TH
(25,
    'Nyhavn Canal House',
    'Colorful 17th-century townhouse right on Nyhavn canal, walking distance to Tivoli Gardens.',
    'HOME', true, 4.6, 5,
    'No smoking. No pets.',
    'Narrow staircase, no elevator. Bicycles provided for guest use.',
    3, 25, 1,
    ST_GeogFromText('SRID=4326;POINT(12.5683 55.6761)')),     -- Copenhagen, DK
(26,
    'Karen Garden Cottage',
    'Cottage set in a lush garden in the Karen suburb, close to the Giraffe Centre and Karen Blixen Museum.',
    'HOME', true, 4.4, 7,
    'No smoking indoors. Gate locked after 10 PM.',
    'Airport transfer available on request. Askari (security guard) on-site overnight.',
    4, 26, 1,
    ST_GeogFromText('SRID=4326;POINT(36.8219 -1.2921)')),     -- Nairobi, KE
(27,
    'Copacabana Beachfront Flat',
    'Beachfront apartment with a wraparound balcony overlooking Copacabana Beach.',
    'HOME', false, 4.2, 3,
    'No parties. No smoking on balcony.',
    'Doorman on duty 24/7. Carnival season pricing may apply.',
    8, 27, 1,
    ST_GeogFromText('SRID=4326;POINT(-43.1729 -22.9068)')),   -- Rio de Janeiro, BR
(28,
    'Gangnam Tower Hotel',
    'Sleek high-rise hotel in Gangnam with a rooftop bar and easy subway access to Myeongdong.',
    'HOTEL', true, 4.7, 2,
    'No smoking. No pets.',
    'Late checkout available for a fee. K-beauty amenities in every room.',
    10, 28, 8,
    ST_GeogFromText('SRID=4326;POINT(126.9780 37.5665)')),    -- Seoul, KR
(29,
    'Stanley Park Cabin',
    'Cedar cabin backing onto Stanley Park, with mountain and harbor views from the deck.',
    'HOME', true, 4.5, 5,
    'No smoking. Pets allowed with prior approval.',
    'Rain gear provided. Seawall bike path 2 minutes away.',
    12, 29, 1,
    ST_GeogFromText('SRID=4326;POINT(-123.1207 49.2827)')),   -- Vancouver, CA
(30,
    'Old Town Stone Flat',
    'Historic stone flat on the Royal Mile, steps from Edinburgh Castle.',
    'HOME', true, 4.4, 5,
    'No smoking indoors. Quiet hours after 10 PM.',
    'Steep spiral staircase, not lift accessible. Festival season books up fast.',
    15, 30, 1,
    ST_GeogFromText('SRID=4326;POINT(-3.1883 55.9533)')),     -- Edinburgh, GB
(31,
    'Old Quarter Courtyard House',
    'Narrow tube house in the Old Quarter with a hidden interior courtyard, minutes from Hoan Kiem Lake.',
    'HOME', false, 4.0, 3,
    'No smoking indoors. Remove shoes at entrance.',
    'Street noise from motorbikes until late evening. Egg coffee tasting kit provided.',
    18, 31, 1,
    ST_GeogFromText('SRID=4326;POINT(105.8342 21.0278)')),    -- Hanoi, VN
(32,
    'Danube Bank Hotel',
    'Grand hotel on the Pest bank of the Danube with direct views of Buda Castle and the Chain Bridge.',
    'HOTEL', true, 4.6, 3,
    'No smoking. No parties.',
    'Thermal spa on-site. Breakfast included, served 7–10:30 AM.',
    1, 32, 6,
    ST_GeogFromText('SRID=4326;POINT(19.0402 47.4979)')),     -- Budapest, HU
(33,
    'Lake Wakatipu Lodge',
    'Timber lodge on the shores of Lake Wakatipu with panoramic views of the Remarkables.',
    'HOME', true, 4.8, 7,
    'No smoking indoors. Ski/bike gear must be stored in the garage.',
    'Free shuttle to the gondola. Hot tub on the lakeside deck.',
    3, 33, 1,
    ST_GeogFromText('SRID=4326;POINT(168.6626 -45.0312)')),   -- Queenstown, NZ
(34,
    'Habana Vieja Colonial House',
    'Restored colonial house with a plant-filled interior patio in the heart of Habana Vieja.',
    'HOME', false, 4.1, 5,
    'No smoking indoors. Cash preferred for extras.',
    'Intermittent hot water — advise host of preferred shower times. Classic car tours arranged on request.',
    4, 34, 1,
    ST_GeogFromText('SRID=4326;POINT(-82.3666 23.1136)')),    -- Havana, CU
(35,
    'Old Town Market Square Hotel',
    'Boutique hotel steps from the Main Market Square and St. Mary''s Basilica.',
    'HOTEL', true, 4.5, 3,
    'No smoking. No pets.',
    'Breakfast served 7–10 AM. Ground floor rooms available on request.',
    8, 35, 2,
    ST_GeogFromText('SRID=4326;POINT(19.9450 50.0647)'))      -- Kraków, PL
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('stay', 'id'), COALESCE(MAX(id), 1)) FROM stay;


-- ============================================================
-- 6. ADDITIONAL ROOMS  (ids 27-57, for stays 16-35)
-- ============================================================

INSERT INTO room (id, stay_id, name, price, sleeps, bedroom_amount, bathrooms, size) VALUES
-- Stay 16 — HOME
(27, 16, 'Tagus View Room',           95.00, 3, 1, 1.0,  40.0),
-- Stay 17 — HOTEL: three rooms
(28, 17, 'Tatami Twin',              210.00, 2, 1, 1.0,  28.0),
(29, 17, 'Zen Garden Suite',         340.00, 3, 1, 1.5,  42.0),
(30, 17, 'Machiya Penthouse',        620.00, 4, 2, 2.0,  70.0),
-- Stay 18 — HOME
(31, 18, 'Palermo Loft Room',         78.00, 3, 1, 1.0,  50.0),
-- Stay 19 — HOTEL: two rooms
(32, 19, 'Opera View Double',        260.00, 2, 1, 1.0,  32.0),
(33, 19, 'Ringstrasse Suite',        480.00, 3, 2, 2.0,  58.0),
-- Stay 20 — HOTEL: three rooms
(34, 20, 'Marina View King',         480.00, 2, 1, 1.0,  45.0),
(35, 20, 'Palm Suite',               890.00, 3, 2, 2.0,  75.0),
(36, 20, 'Royal Penthouse',         2200.00, 4, 2, 3.0, 140.0),
-- Stay 21 — HOTEL: two rooms
(37, 21, 'Bay View Queen',           320.00, 2, 1, 1.0,  34.0),
(38, 21, 'Gardens Suite',            560.00, 3, 2, 2.0,  62.0),
-- Stay 22 — HOME
(39, 22, 'Courtyard Room',            68.00, 3, 1, 1.0,  35.0),
-- Stay 23 — HOTEL: two rooms
(40, 23, 'Old Town Double',          150.00, 2, 1, 1.0,  26.0),
(41, 23, 'Astronomical Suite',       260.00, 3, 1, 1.5,  40.0),
-- Stay 24 — HOME
(42, 24, 'Riverside Room',            60.00, 2, 1, 1.0,  30.0),
-- Stay 25 — HOME
(43, 25, 'Canal View Room',          155.00, 2, 1, 1.0,  32.0),
-- Stay 26 — HOME
(44, 26, 'Garden Cottage Room',       72.00, 3, 1, 1.0,  38.0),
-- Stay 27 — HOME
(45, 27, 'Beachfront Balcony Room',  130.00, 4, 2, 1.0,  55.0),
-- Stay 28 — HOTEL: three rooms
(46, 28, 'Gangnam Standard',         190.00, 2, 1, 1.0,  26.0),
(47, 28, 'Rooftop King',             340.00, 2, 1, 1.5,  36.0),
(48, 28, 'Executive Suite',          620.00, 4, 2, 2.0,  68.0),
-- Stay 29 — HOME
(49, 29, 'Park View Room',           145.00, 4, 2, 1.5,  62.0),
-- Stay 30 — HOME
(50, 30, 'Royal Mile Room',          135.00, 3, 1, 1.0,  34.0),
-- Stay 31 — HOME
(51, 31, 'Courtyard Suite',           45.00, 2, 1, 1.0,  28.0),
-- Stay 32 — HOTEL: two rooms
(52, 32, 'Danube View Double',       165.00, 2, 1, 1.0,  30.0),
(53, 32, 'Castle View Suite',        290.00, 3, 2, 1.5,  50.0),
-- Stay 33 — HOME
(54, 33, 'Remarkables View Room',    195.00, 4, 2, 1.5,  68.0),
-- Stay 34 — HOME
(55, 34, 'Patio Room',                55.00, 2, 1, 1.0,  26.0),
-- Stay 35 — HOTEL: two rooms
(56, 35, 'Market Square Double',     120.00, 2, 1, 1.0,  27.0),
(57, 35, 'Basilica View Suite',      210.00, 3, 1, 1.5,  42.0)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('room', 'id'), COALESCE(MAX(id), 1)) FROM room;

-- Room-level amenities, same rationale as section 3's room_amenity block.
INSERT INTO room_amenity (room_id, amenity_id) VALUES
(27, 4), (27, 7), -- Stay 16
(28, 2), (29, 14),-- Stay 17 (split across rooms)
(31, 4),          -- Stay 18
(32, 2),          -- Stay 19
(34, 2),          -- Stay 20
(37, 2),          -- Stay 21
(39, 9),          -- Stay 22
(40, 2),          -- Stay 23
(42, 4),          -- Stay 24
(43, 4),          -- Stay 25
(44, 4), (44, 7), -- Stay 26
(45, 7),          -- Stay 27
(46, 2),          -- Stay 28
(49, 4), (49, 9), -- Stay 29
(50, 9),          -- Stay 30
(51, 4),          -- Stay 31
(52, 2),          -- Stay 32
(54, 9),          -- Stay 33
(55, 4),          -- Stay 34
(56, 2)           -- Stay 35
ON CONFLICT DO NOTHING;


-- ============================================================
-- 7. ADDITIONAL STAY ATTRIBUTES  (bridge tables, stays 16-35)
-- ============================================================

INSERT INTO stay_view (stay_id, view_id) VALUES
(16, 1),          -- Lisbon: Ocean View (Tagus estuary)
(17, 4),          -- Kyoto: Garden View
(18, 3),          -- Buenos Aires: City Skyline
(19, 3),          -- Vienna: City Skyline
(20, 1), (20, 5), -- Dubai: Ocean View + Pool View
(21, 3), (21, 4), -- Singapore: City Skyline + Garden View
(22, 2),          -- Cusco: Mountain View
(23, 3),          -- Prague: City Skyline
(24, 7),          -- Bangkok: Lake View (river)
(25, 7),          -- Copenhagen: Lake View (canal)
(26, 4),          -- Nairobi: Garden View
(27, 1),          -- Rio: Ocean View
(28, 3),          -- Seoul: City Skyline
(29, 6), (29, 2), -- Vancouver: Forest View + Mountain View
(30, 3),          -- Edinburgh: City Skyline
(31, 7),          -- Hanoi: Lake View
(32, 3),          -- Budapest: City Skyline
(33, 2), (33, 7), -- Queenstown: Mountain View + Lake View
(34, 3),          -- Havana: City Skyline
(35, 3)           -- Kraków: City Skyline
ON CONFLICT DO NOTHING;

INSERT INTO stay_amenity (stay_id, amenity_id) VALUES
(16, 1), (16, 4), (16, 7),
(17, 1), (17, 2), (17, 14),
(18, 1), (18, 4),
(19, 1), (19, 2), (19, 10),
(20, 1), (20, 2), (20, 3), (20, 6),
(21, 1), (21, 2), (21, 3), (21, 6),
(22, 1), (22, 9),
(23, 1), (23, 2), (23, 10),
(24, 1), (24, 4),
(25, 1), (25, 4), (25, 5),
(26, 1), (26, 4), (26, 7),
(27, 1), (27, 7),
(28, 1), (28, 2), (28, 6), (28, 13),
(29, 1), (29, 4), (29, 9),
(30, 1), (30, 9),
(31, 1), (31, 4),
(32, 1), (32, 2), (32, 8), (32, 10),
(33, 1), (33, 8), (33, 9), (33, 15),
(34, 1), (34, 4),
(35, 1), (35, 2), (35, 10)
ON CONFLICT DO NOTHING;

INSERT INTO stay_accessibility (stay_id, accessibility_id) VALUES
(17, 3),
(19, 1), (19, 3), (19, 4),
(20, 1), (20, 3), (20, 4), (20, 5),
(21, 1), (21, 3), (21, 4),
(23, 3),
(28, 1), (28, 3),
(32, 1), (32, 3),
(35, 3)
ON CONFLICT DO NOTHING;

INSERT INTO stay_meal_plan (stay_id, meal_plan_id) VALUES
(16, 1),
(17, 1), (17, 2),
(18, 1),
(19, 1), (19, 2),
(20, 1), (20, 2), (20, 4),
(21, 1), (21, 2),
(22, 1),
(23, 1), (23, 2),
(24, 1),
(25, 1),
(26, 1),
(27, 1),
(28, 1), (28, 2),
(29, 1),
(30, 1),
(31, 1),
(32, 1), (32, 2),
(33, 1), (33, 5),
(34, 1),
(35, 1), (35, 2)
ON CONFLICT DO NOTHING;

INSERT INTO stay_payment_type (stay_id, payment_type_id) VALUES
(16, 1), (16, 2),
(17, 1), (17, 4),
(18, 1), (18, 5),
(19, 1), (19, 2), (19, 4),
(20, 1), (20, 2), (20, 4),
(21, 1), (21, 2), (21, 4),
(22, 1), (22, 5),
(23, 1), (23, 2),
(24, 1), (24, 5),
(25, 1), (25, 4),
(26, 1), (26, 5),
(27, 1), (27, 2),
(28, 1), (28, 2), (28, 4),
(29, 1), (29, 2),
(30, 1), (30, 2),
(31, 1), (31, 5),
(32, 1), (32, 2), (32, 4),
(33, 1), (33, 2),
(34, 1), (34, 5),
(35, 1), (35, 2)
ON CONFLICT DO NOTHING;

INSERT INTO stay_traveler_experience (stay_id, traveler_experience_id) VALUES
(16, 2), (16, 7),
(17, 2), (17, 3),
(18, 5), (18, 7),
(19, 2), (19, 3),
(20, 2), (20, 6),
(21, 1), (21, 3),
(22, 4), (22, 7),
(23, 2), (23, 3),
(24, 5), (24, 7),
(25, 1), (25, 2),
(26, 4), (26, 8),
(27, 1), (27, 2),
(28, 3), (28, 7),
(29, 4), (29, 8),
(30, 2), (30, 3),
(31, 5), (31, 7),
(32, 2), (32, 6),
(33, 4), (33, 6),
(34, 2), (34, 7),
(35, 2), (35, 3)
ON CONFLICT DO NOTHING;


-- ============================================================
-- 8. ADDITIONAL STAYS — SEED BATCH 3 (ids 36-48, reused addresses)
-- ============================================================
-- No new region rows here — every address below points at a region_id
-- already inserted in section 2 or 5, so several cities end up hosting
-- multiple stays (see the header comment for the exact counts). Each
-- address does still need its own row and its own street address, since
-- stay.address_id is UNIQUE (schema-enforced one-stay-per-address).

INSERT INTO address (id, street_address, extended_address, city, state_province, postal_code, country_code, region_id) VALUES
(36, 'Calle Cerro Alegre 45',     NULL,          'Valparaíso',    'Valparaíso',      '2340001',    'CL', 3),
(37, 'Paseo Gervasoni 12',        NULL,          'Valparaíso',    'Valparaíso',      '2340002',    'CL', 3),
(38, 'Avenida Errázuriz 908',     NULL,          'Valparaíso',    'Valparaíso',      '2340003',    'CL', 3),
(39, 'Calle Templeman 220',       NULL,          'Valparaíso',    'Valparaíso',      '2340004',    'CL', 3),
(40, 'Shibuya 2-21-1',            NULL,          'Tokyo',         'Tokyo',           '150-0002',   'JP', 2),
(41, 'Asakusa 1-36-3',            NULL,          'Tokyo',         'Tokyo',           '111-0032',   'JP', 2),
(42, 'Jingumae 6-12-8',           NULL,          'Tokyo',         'Tokyo',           '150-0001',   'JP', 2),
(43, 'Carrer del Bisbe 9',        NULL,          'Barcelona',     'Catalonia',       '08002',      'ES', 11),
(44, 'Carrer de Mallorca 401',    NULL,          'Barcelona',     'Catalonia',       '08013',      'ES', 11),
(45, 'Passeig de Joan de Borbó 62', NULL,        'Barcelona',     'Catalonia',       '08003',      'ES', 11),
(46, 'Rue Lepic 22',              NULL,          'Paris',         'Île-de-France',   '75018',      'FR', 4),
(47, 'Rue des Rosiers 14',        NULL,          'Paris',         'Île-de-France',   '75004',      'FR', 4),
(48, 'Egelantiersgracht 8',       NULL,          'Amsterdam',     'North Holland',   '1015 CE',    'NL', 15)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('address', 'id'), COALESCE(MAX(id), 1)) FROM address;

INSERT INTO stay (
    id, name, about, property_type, is_refundable,
    star_rating, days_from_booking_cancellation_deadline,
    policies_text, important_information,
    host_id, address_id, property_brand_id, location
) VALUES
(36,
    'Cerro Alegre Hillside Apartment',
    'Bright apartment perched on Cerro Alegre with sweeping views over Valparaíso''s colorful rooftops and the harbor beyond.',
    'HOME', true, 4.2, 5,
    'No smoking indoors. Quiet hours 10 PM–8 AM.',
    'Funicular station 3 minutes on foot. Steep streets nearby, comfortable shoes recommended.',
    12, 36, 1,
    ST_GeogFromText('SRID=4326;POINT(-71.6089 -33.0431)')),   -- Valparaíso, CL
(37,
    'Cerro Concepción Boutique Hotel',
    'Restored heritage building turned boutique hotel amid the street art and cafés of Cerro Concepción.',
    'HOTEL', true, 4.5, 3,
    'No smoking. No parties.',
    'Breakfast served 7–10 AM. Funicular access included in stay.',
    15, 37, 1,
    ST_GeogFromText('SRID=4326;POINT(-71.6210 -33.0455)')),   -- Valparaíso, CL
(38,
    'Valparaíso Portside Loft',
    'Industrial-chic loft near the harbor, walking distance to the naval museum and dockside seafood market.',
    'HOME', false, 4.0, 7,
    'No pets. No loud music.',
    'Port noise possible in early morning. Market stalls open from 6 AM.',
    18, 38, 1,
    ST_GeogFromText('SRID=4326;POINT(-71.6265 -33.0398)')),   -- Valparaíso, CL
(39,
    'Bohemian Artist House',
    'Eclectic house filled with local art in the winding hills, a favorite among backpackers and creatives.',
    'HOME', true, 4.1, 5,
    'Respect quiet hours after 10 PM. No smoking indoors.',
    'Shared kitchen with other guests. Weekly graffiti-tour recommendations from the host.',
    1, 39, 1,
    ST_GeogFromText('SRID=4326;POINT(-71.6142 -33.0512)')),   -- Valparaíso, CL
(40,
    'Shibuya Capsule Hotel',
    'Compact, efficient capsule hotel steps from Shibuya Crossing, built for solo travelers on the move.',
    'HOTEL', false, 4.0, 1,
    'No smoking. Shared bathroom facilities.',
    'Luggage storage available. Check-in from 3 PM, capsules locked 24/7 for security.',
    3, 40, 1,
    ST_GeogFromText('SRID=4326;POINT(139.7016 35.6595)')),    -- Tokyo, JP
(41,
    'Asakusa Traditional Inn',
    'Family-run ryokan-style inn near Senso-ji Temple, with tatami floors and futon bedding.',
    'HOME', true, 4.4, 5,
    'Remove shoes indoors. Quiet hours after 9 PM.',
    'Yukata robes provided. Temple market 5 minutes on foot.',
    4, 41, 1,
    ST_GeogFromText('SRID=4326;POINT(139.7967 35.7118)')),    -- Tokyo, JP
(42,
    'Harajuku Design Hotel',
    'Concept hotel wrapped in rotating local art installations, moments from Takeshita Street.',
    'HOTEL', true, 4.6, 2,
    'No smoking. No outside food in rooms.',
    'Late checkout available for a fee. Rooftop gallery open to guests.',
    8, 42, 8,
    ST_GeogFromText('SRID=4326;POINT(139.7028 35.6702)')),    -- Tokyo, JP
(43,
    'Gothic Quarter Apartment',
    'Stone-walled apartment tucked into the medieval lanes of the Barri Gòtic, steps from the cathedral.',
    'HOME', true, 4.3, 5,
    'No smoking indoors. Quiet hours 11 PM–8 AM.',
    'Narrow street, no vehicle access. Cathedral bells audible in the morning.',
    10, 43, 1,
    ST_GeogFromText('SRID=4326;POINT(2.1749 41.3833)')),      -- Barcelona, ES
(44,
    'Sagrada Família View Hotel',
    'Modern hotel with rooftop rooms facing Gaudí''s unfinished basilica.',
    'HOTEL', true, 4.7, 3,
    'No smoking. No parties.',
    'Breakfast served 7–10:30 AM. Basilica tickets bookable at the front desk.',
    12, 44, 4,
    ST_GeogFromText('SRID=4326;POINT(2.1743 41.4036)')),      -- Barcelona, ES
(45,
    'Barceloneta Beach House',
    'Relaxed beach house a block from the sand, with a rooftop terrace for sunset views over the Mediterranean.',
    'HOME', false, 4.2, 3,
    'No parties. No smoking on terrace.',
    'Beach gear provided. Seafood restaurants line the block.',
    15, 45, 1,
    ST_GeogFromText('SRID=4326;POINT(2.1925 41.3784)')),      -- Barcelona, ES
(46,
    'Montmartre Artist Studio',
    'Small studio near Place du Tertre, in the same streets once walked by Picasso and Renoir.',
    'HOME', true, 4.3, 5,
    'No smoking indoors. Quiet hours after 10 PM.',
    'Steep hill up to Sacré-Cœur. Funicular available nearby.',
    18, 46, 1,
    ST_GeogFromText('SRID=4326;POINT(2.3431 48.8867)')),      -- Paris, FR
(47,
    'Le Marais Boutique Hotel',
    'Intimate hotel in a converted 17th-century hôtel particulier, in the heart of Le Marais.',
    'HOTEL', true, 4.6, 3,
    'No smoking. No pets.',
    'Breakfast served 7–10 AM. Concierge can arrange gallery tours.',
    1, 47, 6,
    ST_GeogFromText('SRID=4326;POINT(2.3620 48.8586)')),      -- Paris, FR
(48,
    'Jordaan Canal Apartment',
    'Cozy apartment on a quiet Jordaan canal, close to local markets and independent boutiques.',
    'HOME', true, 4.5, 5,
    'No smoking. Narrow staircase, limited storage for large luggage.',
    'Bicycle available for guest use. Market held nearby on Mondays and Saturdays.',
    3, 48, 1,
    ST_GeogFromText('SRID=4326;POINT(4.8838 52.3745)'))       -- Amsterdam, NL
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('stay', 'id'), COALESCE(MAX(id), 1)) FROM stay;


-- ============================================================
-- 9. ADDITIONAL ROOMS  (ids 58-75, for stays 36-48)
-- ============================================================

INSERT INTO room (id, stay_id, name, price, sleeps, bedroom_amount, bathrooms, size) VALUES
-- Stay 36 — HOME
(58, 36, 'Hillside Studio',           78.00, 2, 1, 1.0,  32.0),
-- Stay 37 — HOTEL: two rooms
(59, 37, 'Colorful Facade Double',   130.00, 2, 1, 1.0,  28.0),
(60, 37, 'Cerro View Suite',         195.00, 3, 1, 1.5,  38.0),
-- Stay 38 — HOME
(61, 38, 'Portside Loft Room',        92.00, 2, 1, 1.0,  45.0),
-- Stay 39 — HOME
(62, 39, 'Artist Studio Room',        68.00, 2, 1, 1.0,  30.0),
-- Stay 40 — HOTEL: two rooms
(63, 40, 'Capsule Pod',               45.00, 1, 1, 0.5,   8.0),
(64, 40, 'Deluxe Capsule Suite',      95.00, 2, 1, 1.0,  15.0),
-- Stay 41 — HOME
(65, 41, 'Tatami Room',               88.00, 2, 1, 1.0,  25.0),
-- Stay 42 — HOTEL: two rooms
(66, 42, 'Pop Art Double',           175.00, 2, 1, 1.0,  26.0),
(67, 42, 'Design Loft Suite',        310.00, 3, 2, 1.5,  48.0),
-- Stay 43 — HOME
(68, 43, 'Gothic Quarter Room',      105.00, 3, 1, 1.0,  42.0),
-- Stay 44 — HOTEL: two rooms
(69, 44, 'Basilica View Double',     210.00, 2, 1, 1.0,  30.0),
(70, 44, 'Gaudí Suite',              350.00, 3, 1, 1.5,  45.0),
-- Stay 45 — HOME
(71, 45, 'Beach House Room',         145.00, 4, 2, 1.5,  58.0),
-- Stay 46 — HOME
(72, 46, 'Artist Studio',             82.00, 2, 1, 1.0,  28.0),
-- Stay 47 — HOTEL: two rooms
(73, 47, 'Marais Classic Double',    240.00, 2, 1, 1.0,  26.0),
(74, 47, 'Marais Suite',             420.00, 3, 1, 1.5,  42.0),
-- Stay 48 — HOME
(75, 48, 'Canal View Studio',        165.00, 2, 1, 1.0,  32.0)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('room', 'id'), COALESCE(MAX(id), 1)) FROM room;

-- Room-level amenities, same rationale as section 3's room_amenity block.
INSERT INTO room_amenity (room_id, amenity_id) VALUES
(58, 4),          -- Stay 36
(59, 2), (60, 14),-- Stay 37 (split across rooms)
(61, 4),          -- Stay 38
(64, 2),          -- Stay 40
(66, 2),          -- Stay 42
(68, 4),          -- Stay 43
(69, 2),          -- Stay 44
(71, 7),          -- Stay 45
(72, 4),          -- Stay 46
(73, 2),          -- Stay 47
(75, 4)           -- Stay 48
ON CONFLICT DO NOTHING;


-- ============================================================
-- 10. ADDITIONAL STAY ATTRIBUTES  (bridge tables, stays 36-48)
-- ============================================================

INSERT INTO stay_view (stay_id, view_id) VALUES
(36, 1),          -- Valparaíso hillside apartment: Ocean View
(37, 1),          -- Valparaíso boutique hotel: Ocean View
(38, 1),          -- Valparaíso portside loft: Ocean View
(39, 3),          -- Valparaíso artist house: City Skyline
(40, 3),          -- Tokyo capsule hotel: City Skyline
(41, 4),          -- Tokyo traditional inn: Garden View
(42, 3),          -- Tokyo design hotel: City Skyline
(43, 3),          -- Barcelona Gothic Quarter: City Skyline
(44, 3),          -- Barcelona Sagrada Família hotel: City Skyline
(45, 1),          -- Barcelona beach house: Ocean View
(46, 3),          -- Paris Montmartre studio: City Skyline
(47, 3),          -- Paris Le Marais hotel: City Skyline
(48, 7)           -- Amsterdam Jordaan apartment: Lake View (canal)
ON CONFLICT DO NOTHING;

INSERT INTO stay_amenity (stay_id, amenity_id) VALUES
(36, 1), (36, 4),
(37, 1), (37, 2), (37, 13),
(38, 1), (38, 4),
(39, 1), (39, 9),
(40, 1), (40, 2),
(41, 1), (41, 4),
(42, 1), (42, 2), (42, 13),
(43, 1), (43, 4),
(44, 1), (44, 2), (44, 10),
(45, 1), (45, 3), (45, 7),
(46, 1), (46, 4),
(47, 1), (47, 2), (47, 10),
(48, 1), (48, 4)
ON CONFLICT DO NOTHING;

INSERT INTO stay_accessibility (stay_id, accessibility_id) VALUES
(37, 3),
(40, 3),
(44, 1), (44, 3)
ON CONFLICT DO NOTHING;

INSERT INTO stay_meal_plan (stay_id, meal_plan_id) VALUES
(36, 1),
(37, 1), (37, 2),
(38, 1),
(39, 1),
(40, 1),
(41, 1), (41, 2),
(42, 1), (42, 2),
(43, 1),
(44, 1), (44, 2),
(45, 1),
(46, 1),
(47, 1), (47, 2),
(48, 1)
ON CONFLICT DO NOTHING;

INSERT INTO stay_payment_type (stay_id, payment_type_id) VALUES
(36, 1), (36, 2),
(37, 1), (37, 2), (37, 4),
(38, 1),
(39, 1), (39, 5),
(40, 1), (40, 4),
(41, 1),
(42, 1), (42, 2), (42, 4),
(43, 1), (43, 2),
(44, 1), (44, 2), (44, 4),
(45, 1), (45, 2),
(46, 1),
(47, 1), (47, 2), (47, 4),
(48, 1), (48, 2)
ON CONFLICT DO NOTHING;

INSERT INTO stay_traveler_experience (stay_id, traveler_experience_id) VALUES
(36, 2), (36, 7),
(37, 2), (37, 3),
(38, 5), (38, 7),
(39, 5), (39, 7),
(40, 5), (40, 7),
(41, 2), (41, 4),
(42, 3), (42, 7),
(43, 2), (43, 5),
(44, 2), (44, 3),
(45, 1), (45, 2),
(46, 2), (46, 7),
(47, 2), (47, 3),
(48, 2), (48, 7)
ON CONFLICT DO NOTHING;

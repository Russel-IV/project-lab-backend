-- ============================================================
-- INVENTORY-SERVICE SEED DATA (inventory-database)
-- ============================================================
-- Tables: view, amenity, accessibility, meal_plan, payment_type,
-- property_brand, traveler_experience, region, address, stay, room, plus
-- the six stay-attribute bridge tables.
-- Safe to run multiple times: all inserts use explicit IDs with
-- ON CONFLICT DO NOTHING. Sequences are reset after each table.
--
-- 15 stays across 5 continents, 26 rooms. stay.host_id values (1, 3, 4,
-- 8, 10, 12, 15, 18) reference identity-service's seeded host ids —
-- there is no database-level FK across services anymore, so
-- scripts/sql/identity.sql MUST run before this fragment.
--
-- Free rooms with no bookings at all (see booking.sql): 3 (Deluxe Suite),
-- 7 (Superior Suite).
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

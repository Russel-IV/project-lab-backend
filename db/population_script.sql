-- ============================================================
-- POPULATION SCRIPT — TEST-READY SEED DATA
-- ============================================================
-- Safe to run multiple times: all inserts use explicit IDs with
-- ON CONFLICT DO NOTHING. Sequences are reset after each table.
--
-- Test credentials (BCrypt, cost 10):
--   plain-text password → "password"
--   hash               → $2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS
--
-- Scale: 20 users (8 hosts / 12 guests), 15 stays across 5 continents,
-- 26 rooms, 30 "narrative" bookings (all 4 statuses represented) plus
-- 64 synthetic COMPLETED bookings (section 7b) backing review
-- eligibility and 15 more (section 7c, one per stay) for a guest who
-- is eligible but hasn't reviewed yet, and 71 reviews (2–10 per stay,
-- ratings 1–5).
--
-- Booking status coverage (see section 7 for the full table):
--   CONFIRMED, PENDING, CANCELLED and COMPLETED all appear on
--   multiple stays, including multi-room bookings.
--
-- Every review's (user_id, stay_id) pair has a matching COMPLETED
-- booking (see section 7b), consistent with createReview()'s
-- eligibility check and its unique-per-(user, stay) constraint (V17).
-- Section 7c seeds one additional eligible-but-unreviewed guest per
-- stay for exercising the createReview happy path.
--
-- Free rooms with no bookings at all: 3 (Deluxe Suite), 7 (Superior Suite)
-- ============================================================


-- ============================================================
-- 1. LOOKUP TABLES
-- ============================================================

INSERT INTO language (id, language_name) VALUES
(1, 'English'),
(2, 'Spanish'),
(3, 'Japanese'),
(4, 'German'),
(5, 'French'),
(6, 'Portuguese'),
(7, 'Mandarin'),
(8, 'Italian'),
(9, 'Korean'),
(10, 'Arabic')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('language', 'id'), COALESCE(MAX(id), 1)) FROM language;

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
-- 2. USERS
-- ============================================================
-- IDs 1, 3, 4, 8, 10, 12, 15, 18 are hosts; the rest are guests.
-- All passwords: "password"

INSERT INTO "user" (id, name, email, password_hash) VALUES
(1,  'Alice Johnson',     'alice@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(2,  'Bob Smith',         'bob@test.com',     '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(3,  'Takashi Murakami',  'takashi@test.com', '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(4,  'Clara Oswald',      'clara@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(5,  'David Kim',         'david@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(6,  'Emma García',       'emma@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(7,  'Frank Lee',         'frank@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(8,  'Priya Patel',       'priya@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(9,  'Liam O''Connor',    'liam@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(10, 'Sofia Rossi',       'sofia@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(11, 'Noah Andersen',     'noah@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(12, 'Amara Okafor',      'amara@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(13, 'Mateo Fernández',   'mateo@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(14, 'Yuki Tanaka',       'yuki@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(15, 'Isabel Santos',     'isabel@test.com',  '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(16, 'Ethan Walker',      'ethan@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(17, 'Zara Ahmed',        'zara@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(18, 'Lucas Müller',      'lucas@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(19, 'Chloe Martin',      'chloe@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(20, 'Omar Hassan',       'omar@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('"user"', 'id'), COALESCE(MAX(id), 1)) FROM "user";


-- ============================================================
-- 3. HOSTS
-- ============================================================

INSERT INTO host (id, communication_rating, checkin_process_rating, cancellation_rate) VALUES
(1,  98.5,  95.0, 2.1),   -- Alice:  top-rated
(3,  100.0, 98.0, 0.0),   -- Takashi: perfect score
(4,  90.0,  88.5, 5.4),   -- Clara:  good but not perfect
(8,  96.0,  94.0, 1.5),   -- Priya:  very responsive
(10, 92.5,  90.0, 3.2),   -- Sofia:  reliable mid-tier
(12, 88.0,  85.0, 6.8),   -- Amara:  solid but occasional cancellations
(15, 99.0,  97.5, 0.5),   -- Isabel: near-perfect
(18, 85.5,  82.0, 8.0)    -- Lucas:  newer host, still building reputation
ON CONFLICT (id) DO NOTHING;

INSERT INTO host_language (host_id, language_id) VALUES
(1, 1), (1, 2),            -- Alice:   English, Spanish
(3, 1), (3, 3),            -- Takashi: English, Japanese
(4, 1), (4, 4), (4, 5),   -- Clara:   English, German, French
(8, 1), (8, 5),            -- Priya:   English, French
(10, 1), (10, 8),          -- Sofia:   English, Italian
(12, 1), (12, 10),         -- Amara:   English, Arabic
(15, 1), (15, 2), (15, 6),-- Isabel:  English, Spanish, Portuguese
(18, 1), (18, 4)           -- Lucas:   English, German
ON CONFLICT DO NOTHING;


-- ============================================================
-- 4. ADDRESSES & STAYS
-- ============================================================

INSERT INTO address (id, street_address, extended_address, city, state_province, postal_code, country_code) VALUES
(1,  '123 Ocean Drive',       'Apt 4B',      'Miami',         'Florida',         '33139',    'US'),
(2,  '4-56 Shinjuku',         'Floor 32',    'Tokyo',         'Tokyo',           '160-0022', 'JP'),
(3,  '789 Alpine Way',        NULL,          'Valparaíso',    'Valparaíso',      '2340000',  'CL'),
(4,  '12 Rue de Rivoli',      '3ème étage',  'Paris',         'Île-de-France',   '75001',    'FR'),
(5,  'Jl. Monkey Forest 88',  NULL,          'Ubud',          'Bali',            '80571',    'ID'),
(6,  'Oia Cliffside Path',    NULL,          'Oia',           'South Aegean',    '84702',    'GR'),
(7,  '350 5th Avenue',        'Suite 2100',  'New York',      'New York',        '10118',    'US'),
(8,  'Uig Road',              NULL,          'Isle of Skye',  'Highland',        'IV51 9XY', 'GB'),
(9,  'Derb El Hammam 12',     NULL,          'Marrakech',     'Marrakech-Safi',  '40000',    'MA'),
(10, 'Bahnhofstrasse 25',     NULL,          'Zermatt',       'Valais',          '3920',     'CH'),
(11, 'Passeig de Gràcia 45',  '2º 1ª',       'Barcelona',     'Catalonia',       '08007',    'ES'),
(12, 'Victoria Road',         NULL,          'Cape Town',     'Western Cape',    '8005',     'ZA'),
(13, '61 Macquarie Street',   NULL,          'Sydney',        'New South Wales', '2000',     'AU'),
(14, 'Laugavegur 7',          NULL,          'Reykjavik',     'Capital Region',  '101',      'IS'),
(15, 'Prinsengracht 263',     NULL,          'Amsterdam',     'North Holland',   '1016 GV',  'NL')
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
-- 5. ROOMS
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
-- 6. STAY PICTURES
-- ============================================================
-- Stays 1–5 point to real files in the project S3 bucket.
-- Stays 6–15 use picsum.photos so images actually render in the UI.

INSERT INTO stay_picture (id, stay_id, url, caption, is_primary, display_order) VALUES
-- Stay 1
(1,  1, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/beach-exterior.png',  'Ocean-facing exterior',       true,  0),
(2,  1, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/living-room.png',     'Open-plan living area',       false, 1),
(3,  1, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/beach-bedroom.png',   'Master bedroom',              false, 2),
-- Stay 2
(4,  2, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/lobby.png',           'Hotel lobby',                 true,  0),
(5,  2, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/standard-king.png',   'Standard King room',          false, 1),
(6,  2, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/deluxe-suite.png',    'Deluxe Suite living area',    false, 2),
(7,  2, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/penthouse.png',       'Executive Penthouse view',    false, 3),
-- Stay 3
(8,  3, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/cabin-exterior.png',  'Cabin surrounded by pines',   true,  0),
(9,  3, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/cabin-interior.png',  'Interior with fireplace',     false, 1),
-- Stay 4
(10, 4, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/paris-facade.png',    'Building facade',             true,  0),
(11, 4, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/paris-room.png',      'Classic Double room',         false, 1),
-- Stay 5
(12, 5, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/bali-villa.png',      'Pool villa from the garden',  true,  0),
(13, 5, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/bali-pool.png',       'Private infinity pool',       false, 1),
-- Stay 6
(14, 6,  'https://picsum.photos/seed/santorini-villa-ext/1200/800',       'Cliffside villa at sunset',              true,  0),
(15, 6,  'https://picsum.photos/seed/santorini-villa-pool/1200/800',      'Infinity pool over the caldera',         false, 1),
(16, 6,  'https://picsum.photos/seed/santorini-villa-bedroom/1200/800',   'Whitewashed bedroom suite',              false, 2),
-- Stay 7
(17, 7,  'https://picsum.photos/seed/manhattan-hotel-lobby/1200/800',     'Midtown hotel lobby',                    true,  0),
(18, 7,  'https://picsum.photos/seed/manhattan-hotel-king/1200/800',      'Skyline King room',                      false, 1),
(19, 7,  'https://picsum.photos/seed/manhattan-hotel-rooftop/1200/800',   'Rooftop bar at night',                   false, 2),
-- Stay 8
(20, 8,  'https://picsum.photos/seed/skye-cottage-ext/1200/800',          'Stone cottage exterior',                 true,  0),
(21, 8,  'https://picsum.photos/seed/skye-cottage-view/1200/800',         'View of the Cuillin mountains',          false, 1),
-- Stay 9
(22, 9,  'https://picsum.photos/seed/marrakech-riad-courtyard/1200/800',  'Central courtyard with plunge pool',     true,  0),
(23, 9,  'https://picsum.photos/seed/marrakech-riad-rooftop/1200/800',    'Rooftop terrace at dusk',                false, 1),
(24, 9,  'https://picsum.photos/seed/marrakech-riad-suite/1200/800',      'Riad Suite interior',                    false, 2),
-- Stay 10
(25, 10, 'https://picsum.photos/seed/zermatt-chalet-ext/1200/800',        'Timber chalet with Matterhorn backdrop', true,  0),
(26, 10, 'https://picsum.photos/seed/zermatt-chalet-interior/1200/800',   'Cozy alpine living room',                false, 1),
(27, 10, 'https://picsum.photos/seed/zermatt-chalet-bootroom/1200/800',   'Heated ski boot room',                   false, 2),
-- Stay 11
(28, 11, 'https://picsum.photos/seed/barcelona-hotel-facade/1200/800',    'Modernist facade on Passeig de Gràcia',  true,  0),
(29, 11, 'https://picsum.photos/seed/barcelona-hotel-suite/1200/800',     'Passeig de Gràcia Suite',                false, 1),
(30, 11, 'https://picsum.photos/seed/barcelona-hotel-pool/1200/800',      'Seasonal rooftop pool',                  false, 2),
-- Stay 12
(31, 12, 'https://picsum.photos/seed/capetown-lodge-beach/1200/800',      'Direct beach access',                    true,  0),
(32, 12, 'https://picsum.photos/seed/capetown-lodge-view/1200/800',       'Table Mountain view from the deck',      false, 1),
-- Stay 13
(33, 13, 'https://picsum.photos/seed/sydney-hotel-harbour/1200/800',      'Harbour view from the lobby',            true,  0),
(34, 13, 'https://picsum.photos/seed/sydney-hotel-opera/1200/800',        'Opera House King room',                  false, 1),
(35, 13, 'https://picsum.photos/seed/sydney-hotel-suite/1200/800',        'Executive Suite living area',            false, 2),
-- Stay 14
(36, 14, 'https://picsum.photos/seed/reykjavik-cabin-ext/1200/800',       'Glass-roofed cabin exterior',            true,  0),
(37, 14, 'https://picsum.photos/seed/reykjavik-cabin-auroras/1200/800',   'Northern lights above the cabin',        false, 1),
-- Stay 15
(38, 15, 'https://picsum.photos/seed/amsterdam-hotel-canal/1200/800',     'Canal house facade at golden hour',      true,  0),
(39, 15, 'https://picsum.photos/seed/amsterdam-hotel-double/1200/800',    'Canal View Double room',                 false, 1),
(40, 15, 'https://picsum.photos/seed/amsterdam-hotel-breakfast/1200/800', 'Breakfast room overlooking the canal',   false, 2)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('stay_picture', 'id'), COALESCE(MAX(id), 1)) FROM stay_picture;


-- ============================================================
-- 7. BOOKINGS  (all four statuses covered across every stay)
-- ============================================================

-- total_price = sum(room prices) × nights
INSERT INTO booking (id, user_id, check_in_date, check_out_date, status, guests_count, created_at, total_price) VALUES
(1,  2,  '2027-01-15', '2027-01-20', 'CONFIRMED', 2, '2026-06-01 10:00:00',  602.50),  -- Bob   → Beachfront Suite
(2,  5,  '2027-02-10', '2027-02-14', 'CONFIRMED', 1, '2026-06-05 14:30:00', 1400.00),  -- David  → Standard King
(3,  2,  '2027-03-01', '2027-03-05', 'PENDING',   3, '2026-06-10 09:00:00', 2800.00),  -- Bob    → Classic Double + Superior Suite
(4,  6,  '2026-08-01', '2026-08-07', 'CANCELLED', 4, '2026-05-20 11:00:00',  510.00),  -- Emma   → Mountain Loft
(5,  5,  '2026-04-05', '2026-04-12', 'COMPLETED', 2, '2026-03-01 16:00:00', 1225.00),  -- David  → Jungle Pool Villa
(6,  7,  '2027-04-20', '2027-04-25', 'CONFIRMED', 1, '2026-06-15 08:00:00', 6000.00),  -- Frank  → Executive Penthouse
(7,  9,  '2027-05-10', '2027-05-15', 'CONFIRMED', 2, '2026-11-01 09:15:00', 1550.00),  -- Liam   → Cliffside Suite
(8,  16, '2026-05-01', '2026-05-04', 'COMPLETED', 2, '2026-03-15 12:00:00',  930.00),  -- Ethan  → Cliffside Suite
(9,  11, '2027-06-01', '2027-06-05', 'CONFIRMED', 2, '2026-12-01 10:00:00', 1120.00),  -- Noah   → City View Queen
(10, 13, '2027-07-10', '2027-07-13', 'PENDING',   2, '2026-12-20 14:00:00', 1260.00),  -- Mateo  → Skyline King
(11, 17, '2026-05-01', '2026-05-04', 'CANCELLED', 4, '2026-02-01 08:00:00', 4110.00),  -- Zara   → Skyline King + Penthouse Loft
(12, 14, '2027-08-05', '2027-08-10', 'CONFIRMED', 3, '2027-01-10 09:00:00',  750.00),  -- Yuki   → Highland Cottage
(13, 19, '2027-03-15', '2027-03-20', 'CONFIRMED', 2, '2026-10-01 11:00:00',  700.00),  -- Chloe  → Riad Suite
(14, 20, '2026-02-01', '2026-02-05', 'COMPLETED', 2, '2025-12-05 09:30:00',  840.00),  -- Omar   → Rooftop Suite
(15, 9,  '2027-09-01', '2027-09-04', 'PENDING',   4, '2027-02-01 10:00:00', 1050.00),  -- Liam   → Riad Suite + Rooftop Suite
(16, 2,  '2027-01-05', '2027-01-12', 'CONFIRMED', 4, '2026-08-01 09:00:00', 3360.00),  -- Bob    → Alpine Chalet
(17, 6,  '2026-03-01', '2026-03-05', 'CANCELLED', 2, '2026-01-05 09:00:00', 1920.00),  -- Emma   → Alpine Chalet
(18, 5,  '2027-04-10', '2027-04-13', 'CONFIRMED', 2, '2026-11-15 09:00:00',  570.00),  -- David  → Gothic Quarter Double
(19, 7,  '2027-02-20', '2027-02-24', 'PENDING',   2, '2026-12-10 09:00:00', 1040.00),  -- Frank  → Passeig de Gràcia Suite
(20, 13, '2026-06-01', '2026-06-05', 'COMPLETED', 2, '2026-04-01 09:00:00', 1200.00),  -- Mateo  → Rooftop Terrace Room
(21, 16, '2027-11-01', '2027-11-08', 'CONFIRMED', 2, '2027-05-01 09:00:00', 1155.00),  -- Ethan  → Ocean Lodge Room
(22, 17, '2026-04-01', '2026-04-04', 'COMPLETED', 2, '2026-02-01 09:00:00',  495.00),  -- Zara   → Ocean Lodge Room
(23, 9,  '2027-12-01', '2027-12-05', 'CONFIRMED', 2, '2027-06-01 09:00:00',  880.00),  -- Liam   → Harbour View Twin
(24, 11, '2027-01-20', '2027-01-24', 'PENDING',   3, '2026-09-01 09:00:00', 1360.00),  -- Noah   → Opera House King
(25, 19, '2026-07-01', '2026-07-07', 'COMPLETED', 2, '2026-04-15 09:00:00', 3600.00),  -- Chloe  → Executive Suite
(26, 14, '2027-02-01', '2027-02-05', 'CONFIRMED', 2, '2026-10-01 09:00:00',  840.00),  -- Yuki   → Aurora Cabin
(27, 20, '2026-01-10', '2026-01-13', 'CANCELLED', 2, '2025-11-01 09:00:00',  630.00),  -- Omar   → Aurora Cabin
(28, 2,  '2027-05-01', '2027-05-04', 'CONFIRMED', 2, '2026-12-01 09:00:00',  720.00),  -- Bob    → Canal View Double
(29, 6,  '2026-03-15', '2026-03-19', 'COMPLETED', 2, '2026-01-20 09:00:00', 1520.00),  -- Emma   → Canal House Suite
(30, 13, '2027-10-10', '2027-10-13', 'PENDING',   2, '2027-04-01 09:00:00',  720.00)   -- Mateo  → Canal View Double
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('booking', 'id'), COALESCE(MAX(id), 1)) FROM booking;

INSERT INTO booking_room (booking_id, room_id) VALUES
(1, 1),
(2, 2),
(3, 6), (3, 7),
(4, 5),
(5, 8),
(6, 4),
(7, 9),
(8, 9),
(9, 10),
(10, 11),
(11, 11), (11, 12),
(12, 13),
(13, 14),
(14, 15),
(15, 14), (15, 15),
(16, 16),
(17, 16),
(18, 17),
(19, 18),
(20, 19),
(21, 20),
(22, 20),
(23, 21),
(24, 22),
(25, 23),
(26, 24),
(27, 24),
(28, 25),
(29, 26),
(30, 25)
ON CONFLICT DO NOTHING;


-- ------------------------------------------------------------
-- 7b. REVIEW-ELIGIBILITY BOOKINGS
-- ------------------------------------------------------------
-- createReview() now requires the reviewer to have a COMPLETED booking
-- for the stay being reviewed, and rejects a second review for the same
-- stay (unique constraint on review(user_id, stay_id), see V17). The
-- synthetic bookings below backfill a COMPLETED booking for every
-- (user, stay) pair reviewed in section 8, so this seed data stays
-- internally consistent with that rule and is usable for exercising
-- createReview / myBookingStatusForStay / myReviewForStay end-to-end.

INSERT INTO booking (id, user_id, check_in_date, check_out_date, status, guests_count, created_at, total_price) VALUES
(31, 2, '2025-01-05', '2025-01-08', 'COMPLETED', 1, '2024-11-06 09:00:00', 361.50),  -- user 2 -> stay 1 (review eligibility)
(32, 6, '2025-01-10', '2025-01-13', 'COMPLETED', 1, '2024-11-11 09:00:00', 361.50),  -- user 6 -> stay 1 (review eligibility)
(33, 9, '2025-01-15', '2025-01-18', 'COMPLETED', 1, '2024-11-16 09:00:00', 361.50),  -- user 9 -> stay 1 (review eligibility)
(34, 16, '2025-01-20', '2025-01-23', 'COMPLETED', 1, '2024-11-21 09:00:00', 361.50),  -- user 16 -> stay 1 (review eligibility)
(35, 7, '2025-01-25', '2025-01-28', 'COMPLETED', 1, '2024-11-26 09:00:00', 1050.00),  -- user 7 -> stay 2 (review eligibility)
(36, 5, '2025-01-30', '2025-02-02', 'COMPLETED', 1, '2024-12-01 09:00:00', 1050.00),  -- user 5 -> stay 2 (review eligibility)
(37, 11, '2025-02-04', '2025-02-07', 'COMPLETED', 1, '2024-12-06 09:00:00', 1050.00),  -- user 11 -> stay 2 (review eligibility)
(38, 17, '2025-02-09', '2025-02-12', 'COMPLETED', 1, '2024-12-11 09:00:00', 1050.00),  -- user 17 -> stay 2 (review eligibility)
(39, 13, '2025-02-14', '2025-02-17', 'COMPLETED', 1, '2024-12-16 09:00:00', 1050.00),  -- user 13 -> stay 2 (review eligibility)
(40, 20, '2025-02-19', '2025-02-22', 'COMPLETED', 1, '2024-12-21 09:00:00', 1050.00),  -- user 20 -> stay 2 (review eligibility)
(41, 2, '2025-02-24', '2025-02-27', 'COMPLETED', 1, '2024-12-26 09:00:00', 255.00),  -- user 2 -> stay 3 (review eligibility)
(42, 7, '2025-03-01', '2025-03-04', 'COMPLETED', 1, '2024-12-31 09:00:00', 255.00),  -- user 7 -> stay 3 (review eligibility)
(43, 14, '2025-03-06', '2025-03-09', 'COMPLETED', 1, '2025-01-05 09:00:00', 255.00),  -- user 14 -> stay 3 (review eligibility)
(44, 6, '2025-03-11', '2025-03-14', 'COMPLETED', 1, '2025-01-10 09:00:00', 660.00),  -- user 6 -> stay 4 (review eligibility)
(45, 9, '2025-03-16', '2025-03-19', 'COMPLETED', 1, '2025-01-15 09:00:00', 660.00),  -- user 9 -> stay 4 (review eligibility)
(46, 19, '2025-03-21', '2025-03-24', 'COMPLETED', 1, '2025-01-20 09:00:00', 660.00),  -- user 19 -> stay 4 (review eligibility)
(47, 2, '2025-03-26', '2025-03-29', 'COMPLETED', 1, '2025-01-25 09:00:00', 525.00),  -- user 2 -> stay 5 (review eligibility)
(48, 11, '2025-03-31', '2025-04-03', 'COMPLETED', 1, '2025-01-30 09:00:00', 525.00),  -- user 11 -> stay 5 (review eligibility)
(49, 17, '2025-04-05', '2025-04-08', 'COMPLETED', 1, '2025-02-04 09:00:00', 525.00),  -- user 17 -> stay 5 (review eligibility)
(50, 20, '2025-04-10', '2025-04-13', 'COMPLETED', 1, '2025-02-09 09:00:00', 525.00),  -- user 20 -> stay 5 (review eligibility)
(51, 9, '2025-04-15', '2025-04-18', 'COMPLETED', 1, '2025-02-14 09:00:00', 930.00),  -- user 9 -> stay 6 (review eligibility)
(52, 13, '2025-04-20', '2025-04-23', 'COMPLETED', 1, '2025-02-19 09:00:00', 930.00),  -- user 13 -> stay 6 (review eligibility)
(53, 6, '2025-04-25', '2025-04-28', 'COMPLETED', 1, '2025-02-24 09:00:00', 930.00),  -- user 6 -> stay 6 (review eligibility)
(54, 5, '2025-04-30', '2025-05-03', 'COMPLETED', 1, '2025-03-01 09:00:00', 840.00),  -- user 5 -> stay 7 (review eligibility)
(55, 14, '2025-05-05', '2025-05-08', 'COMPLETED', 1, '2025-03-06 09:00:00', 840.00),  -- user 14 -> stay 7 (review eligibility)
(56, 19, '2025-05-10', '2025-05-13', 'COMPLETED', 1, '2025-03-11 09:00:00', 840.00),  -- user 19 -> stay 7 (review eligibility)
(57, 7, '2025-05-15', '2025-05-18', 'COMPLETED', 1, '2025-03-16 09:00:00', 840.00),  -- user 7 -> stay 7 (review eligibility)
(58, 20, '2025-05-20', '2025-05-23', 'COMPLETED', 1, '2025-03-21 09:00:00', 840.00),  -- user 20 -> stay 7 (review eligibility)
(59, 2, '2025-05-25', '2025-05-28', 'COMPLETED', 1, '2025-03-26 09:00:00', 840.00),  -- user 2 -> stay 7 (review eligibility)
(60, 17, '2025-05-30', '2025-06-02', 'COMPLETED', 1, '2025-03-31 09:00:00', 840.00),  -- user 17 -> stay 7 (review eligibility)
(61, 11, '2025-06-04', '2025-06-07', 'COMPLETED', 1, '2025-04-05 09:00:00', 840.00),  -- user 11 -> stay 7 (review eligibility)
(62, 13, '2025-06-09', '2025-06-12', 'COMPLETED', 1, '2025-04-10 09:00:00', 450.00),  -- user 13 -> stay 8 (review eligibility)
(63, 9, '2025-06-14', '2025-06-17', 'COMPLETED', 1, '2025-04-15 09:00:00', 450.00),  -- user 9 -> stay 8 (review eligibility)
(64, 6, '2025-06-19', '2025-06-22', 'COMPLETED', 1, '2025-04-20 09:00:00', 420.00),  -- user 6 -> stay 9 (review eligibility)
(65, 16, '2025-06-24', '2025-06-27', 'COMPLETED', 1, '2025-04-25 09:00:00', 420.00),  -- user 16 -> stay 9 (review eligibility)
(66, 14, '2025-06-29', '2025-07-02', 'COMPLETED', 1, '2025-04-30 09:00:00', 420.00),  -- user 14 -> stay 9 (review eligibility)
(67, 2, '2025-07-04', '2025-07-07', 'COMPLETED', 1, '2025-05-05 09:00:00', 420.00),  -- user 2 -> stay 9 (review eligibility)
(68, 19, '2025-07-09', '2025-07-12', 'COMPLETED', 1, '2025-05-10 09:00:00', 420.00),  -- user 19 -> stay 9 (review eligibility)
(69, 7, '2025-07-14', '2025-07-17', 'COMPLETED', 1, '2025-05-15 09:00:00', 1440.00),  -- user 7 -> stay 10 (review eligibility)
(70, 11, '2025-07-19', '2025-07-22', 'COMPLETED', 1, '2025-05-20 09:00:00', 1440.00),  -- user 11 -> stay 10 (review eligibility)
(71, 17, '2025-07-24', '2025-07-27', 'COMPLETED', 1, '2025-05-25 09:00:00', 1440.00),  -- user 17 -> stay 10 (review eligibility)
(72, 5, '2025-07-29', '2025-08-01', 'COMPLETED', 1, '2025-05-30 09:00:00', 570.00),  -- user 5 -> stay 11 (review eligibility)
(73, 9, '2025-08-03', '2025-08-06', 'COMPLETED', 1, '2025-06-04 09:00:00', 570.00),  -- user 9 -> stay 11 (review eligibility)
(74, 20, '2025-08-08', '2025-08-11', 'COMPLETED', 1, '2025-06-09 09:00:00', 570.00),  -- user 20 -> stay 11 (review eligibility)
(75, 16, '2025-08-13', '2025-08-16', 'COMPLETED', 1, '2025-06-14 09:00:00', 570.00),  -- user 16 -> stay 11 (review eligibility)
(76, 6, '2025-08-18', '2025-08-21', 'COMPLETED', 1, '2025-06-19 09:00:00', 570.00),  -- user 6 -> stay 11 (review eligibility)
(77, 14, '2025-08-23', '2025-08-26', 'COMPLETED', 1, '2025-06-24 09:00:00', 570.00),  -- user 14 -> stay 11 (review eligibility)
(78, 19, '2025-08-28', '2025-08-31', 'COMPLETED', 1, '2025-06-29 09:00:00', 495.00),  -- user 19 -> stay 12 (review eligibility)
(79, 11, '2025-09-02', '2025-09-05', 'COMPLETED', 1, '2025-07-04 09:00:00', 495.00),  -- user 11 -> stay 12 (review eligibility)
(80, 2, '2025-09-07', '2025-09-10', 'COMPLETED', 1, '2025-07-09 09:00:00', 495.00),  -- user 2 -> stay 12 (review eligibility)
(81, 6, '2025-09-12', '2025-09-15', 'COMPLETED', 1, '2025-07-14 09:00:00', 660.00),  -- user 6 -> stay 13 (review eligibility)
(82, 14, '2025-09-17', '2025-09-20', 'COMPLETED', 1, '2025-07-19 09:00:00', 660.00),  -- user 14 -> stay 13 (review eligibility)
(83, 20, '2025-09-22', '2025-09-25', 'COMPLETED', 1, '2025-07-24 09:00:00', 660.00),  -- user 20 -> stay 13 (review eligibility)
(84, 7, '2025-09-27', '2025-09-30', 'COMPLETED', 1, '2025-07-29 09:00:00', 660.00),  -- user 7 -> stay 13 (review eligibility)
(85, 9, '2025-10-02', '2025-10-05', 'COMPLETED', 1, '2025-08-03 09:00:00', 660.00),  -- user 9 -> stay 13 (review eligibility)
(86, 13, '2025-10-07', '2025-10-10', 'COMPLETED', 1, '2025-08-08 09:00:00', 660.00),  -- user 13 -> stay 13 (review eligibility)
(87, 16, '2025-10-12', '2025-10-15', 'COMPLETED', 1, '2025-08-13 09:00:00', 660.00),  -- user 16 -> stay 13 (review eligibility)
(88, 5, '2025-10-17', '2025-10-20', 'COMPLETED', 1, '2025-08-18 09:00:00', 660.00),  -- user 5 -> stay 13 (review eligibility)
(89, 11, '2025-10-22', '2025-10-25', 'COMPLETED', 1, '2025-08-23 09:00:00', 630.00),  -- user 11 -> stay 14 (review eligibility)
(90, 17, '2025-10-27', '2025-10-30', 'COMPLETED', 1, '2025-08-28 09:00:00', 630.00),  -- user 17 -> stay 14 (review eligibility)
(91, 13, '2025-11-01', '2025-11-04', 'COMPLETED', 1, '2025-09-02 09:00:00', 720.00),  -- user 13 -> stay 15 (review eligibility)
(92, 9, '2025-11-06', '2025-11-09', 'COMPLETED', 1, '2025-09-07 09:00:00', 720.00),  -- user 9 -> stay 15 (review eligibility)
(93, 20, '2025-11-11', '2025-11-14', 'COMPLETED', 1, '2025-09-12 09:00:00', 720.00),  -- user 20 -> stay 15 (review eligibility)
(94, 16, '2025-11-16', '2025-11-19', 'COMPLETED', 1, '2025-09-17 09:00:00', 720.00)  -- user 16 -> stay 15 (review eligibility)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('booking', 'id'), COALESCE(MAX(id), 1)) FROM booking;

INSERT INTO booking_room (booking_id, room_id) VALUES
(31, 1),
(32, 1),
(33, 1),
(34, 1),
(35, 2),
(36, 2),
(37, 2),
(38, 2),
(39, 2),
(40, 2),
(41, 5),
(42, 5),
(43, 5),
(44, 6),
(45, 6),
(46, 6),
(47, 8),
(48, 8),
(49, 8),
(50, 8),
(51, 9),
(52, 9),
(53, 9),
(54, 10),
(55, 10),
(56, 10),
(57, 10),
(58, 10),
(59, 10),
(60, 10),
(61, 10),
(62, 13),
(63, 13),
(64, 14),
(65, 14),
(66, 14),
(67, 14),
(68, 14),
(69, 16),
(70, 16),
(71, 16),
(72, 17),
(73, 17),
(74, 17),
(75, 17),
(76, 17),
(77, 17),
(78, 20),
(79, 20),
(80, 20),
(81, 21),
(82, 21),
(83, 21),
(84, 21),
(85, 21),
(86, 21),
(87, 21),
(88, 21),
(89, 24),
(90, 24),
(91, 25),
(92, 25),
(93, 25),
(94, 25)
ON CONFLICT DO NOTHING;


-- ------------------------------------------------------------
-- 7c. UNREVIEWED COMPLETED BOOKINGS
-- ------------------------------------------------------------
-- One extra COMPLETED booking per stay, for a guest who has *not* left
-- a review for it. Section 7b makes every existing review eligible,
-- which leaves no seed user in the "eligible, hasn't reviewed yet"
-- state — the actual createReview happy path. These fill that gap so
-- it can be exercised end-to-end (log in as the noted user, call
-- myBookingStatusForStay for the stay, then createReview).

INSERT INTO booking (id, user_id, check_in_date, check_out_date, status, guests_count, created_at, total_price) VALUES
(95, 5, '2025-12-01', '2025-12-04', 'COMPLETED', 1, '2025-10-17 09:00:00', 361.50),  -- user 5 -> stay 1 (eligible, not yet reviewed)
(96, 6, '2025-12-07', '2025-12-10', 'COMPLETED', 1, '2025-10-23 09:00:00', 1050.00),  -- user 6 -> stay 2 (eligible, not yet reviewed)
(97, 9, '2025-12-13', '2025-12-16', 'COMPLETED', 1, '2025-10-29 09:00:00', 255.00),  -- user 9 -> stay 3 (eligible, not yet reviewed)
(98, 11, '2025-12-19', '2025-12-22', 'COMPLETED', 1, '2025-11-04 09:00:00', 660.00),  -- user 11 -> stay 4 (eligible, not yet reviewed)
(99, 13, '2025-12-25', '2025-12-28', 'COMPLETED', 1, '2025-11-10 09:00:00', 525.00),  -- user 13 -> stay 5 (eligible, not yet reviewed)
(100, 14, '2025-12-31', '2026-01-03', 'COMPLETED', 1, '2025-11-16 09:00:00', 930.00),  -- user 14 -> stay 6 (eligible, not yet reviewed)
(101, 16, '2026-01-06', '2026-01-09', 'COMPLETED', 1, '2025-11-22 09:00:00', 840.00),  -- user 16 -> stay 7 (eligible, not yet reviewed)
(102, 17, '2026-01-12', '2026-01-15', 'COMPLETED', 1, '2025-11-28 09:00:00', 450.00),  -- user 17 -> stay 8 (eligible, not yet reviewed)
(103, 5, '2026-01-18', '2026-01-21', 'COMPLETED', 1, '2025-12-04 09:00:00', 420.00),  -- user 5 -> stay 9 (eligible, not yet reviewed)
(104, 6, '2026-01-24', '2026-01-27', 'COMPLETED', 1, '2025-12-10 09:00:00', 1440.00),  -- user 6 -> stay 10 (eligible, not yet reviewed)
(105, 7, '2026-01-30', '2026-02-02', 'COMPLETED', 1, '2025-12-16 09:00:00', 570.00),  -- user 7 -> stay 11 (eligible, not yet reviewed)
(106, 9, '2026-02-05', '2026-02-08', 'COMPLETED', 1, '2025-12-22 09:00:00', 495.00),  -- user 9 -> stay 12 (eligible, not yet reviewed)
(107, 11, '2026-02-11', '2026-02-14', 'COMPLETED', 1, '2025-12-28 09:00:00', 660.00),  -- user 11 -> stay 13 (eligible, not yet reviewed)
(108, 13, '2026-02-17', '2026-02-20', 'COMPLETED', 1, '2026-01-03 09:00:00', 630.00),  -- user 13 -> stay 14 (eligible, not yet reviewed)
(109, 14, '2026-02-23', '2026-02-26', 'COMPLETED', 1, '2026-01-09 09:00:00', 720.00)  -- user 14 -> stay 15 (eligible, not yet reviewed)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('booking', 'id'), COALESCE(MAX(id), 1)) FROM booking;

INSERT INTO booking_room (booking_id, room_id) VALUES
(95, 1),
(96, 2),
(97, 5),
(98, 6),
(99, 8),
(100, 9),
(101, 10),
(102, 13),
(103, 14),
(104, 16),
(105, 17),
(106, 20),
(107, 21),
(108, 24),
(109, 25)
ON CONFLICT DO NOTHING;


-- ============================================================
-- 8. REVIEWS  (2–10 per stay across all 15 stays)
-- ============================================================

INSERT INTO review (id, text, user_id, stay_id, rating) VALUES
-- Stay 1 — Cozy Beachfront House (4 reviews)
(1, 'Amazing stay! The ocean view was stunning and the host was incredibly welcoming.', 2, 1, 5),
(7, 'The kids loved being steps from the sand, and the kitchen was fully stocked for a week of cooking.', 6, 1, 5),
(8, 'Great location and a beautiful deck, though the wifi dropped a few times during our stay.', 9, 1, 4),
(9, 'Alice was so responsive and the house was even nicer than the photos.', 16, 1, 5),
-- Stay 2 — Luxury Tokyo Sky Hotel (6 reviews)
(2, 'Incredible service and breathtaking views. The penthouse is worth every penny.', 7, 2, 5),
(3, 'Standard King was spotless. Tokyo from the 32nd floor at night is pure magic.', 5, 2, 4),
(10, 'Front desk upgraded us on arrival and the view of Shinjuku at night was unbeatable.', 11, 2, 5),
(11, 'Rooms are small for the price, but the location makes up for it.', 17, 2, 3),
(12, 'Best hotel breakfast we''ve had in Tokyo, and the staff speak excellent English.', 13, 2, 5),
(13, 'Quiet, immaculate, and a five-minute walk from the station.', 20, 2, 4),
-- Stay 3 — Charming Mountain Cabin (3 reviews)
(4, 'Cold during winter but the cabin fireplace kept us warm. Absolutely beautiful.', 2, 3, 4),
(14, 'Waking up to fog rolling over the pines was worth the winding drive up.', 7, 3, 5),
(15, 'Charming but the heating struggled on the coldest night.', 14, 3, 3),
-- Stay 4 — Parisian Boutique Hotel (3 reviews)
(6, 'Paris was magical. Hotel perfectly located and the staff are wonderfully attentive.', 6, 4, 5),
(16, 'Steps from the Louvre and the breakfast spread was excellent.', 9, 4, 4),
(17, 'Elegant rooms, and the late checkout saved our last day in Paris.', 19, 4, 5),
-- Stay 5 — Bali Jungle Retreat (5 reviews)
(5, 'Bali exceeded all expectations. The pool villa is a dream. Will definitely be back!', 5, 5, 5),
(18, 'The rice paddy views from the pool are something we''ll never forget.', 2, 5, 5),
(19, 'Airport transfer was seamless, villa is even better in person.', 11, 5, 4),
(20, 'Peaceful and private, though the road up gets muddy after rain.', 17, 5, 4),
(21, 'Staff heated the pool for us without even asking twice. Incredible hospitality.', 20, 5, 5),
-- Stay 6 — Santorini Cliffside Villa (4 reviews)
(22, 'Sunset from the terrace is exactly like the postcards. Unreal.', 9, 6, 5),
(23, 'Steep steps but the caldera view makes every one of them worth it.', 16, 6, 5),
(24, 'Beautiful villa, but book a rental car — the walk into town is longer than it looks.', 13, 6, 3),
(25, 'Priya left a bottle of local wine waiting for us. Lovely touch.', 6, 6, 4),
-- Stay 7 — Manhattan Skyline Hotel (8 reviews)
(26, 'Rooftop bar views of the Empire State Building at night, can''t beat it.', 5, 7, 5),
(27, 'Rooms are compact like most Midtown hotels but spotless and well soundproofed.', 14, 7, 4),
(28, 'Valet was fast even during rush hour, and the penthouse suite was stunning.', 19, 7, 5),
(29, 'Great location for Broadway, walkable to almost everything.', 7, 7, 4),
(30, 'Nice hotel but street noise carried through the window at night.', 20, 7, 3),
(31, 'Front desk handled a last-minute date change without any fuss.', 2, 7, 5),
(32, 'Skyline King room lived up to its name — incredible view of the city lights.', 17, 7, 4),
(33, 'Perfect base for a work trip, fast wifi and a great gym.', 11, 7, 5),
-- Stay 8 — Scottish Highlands Cottage (2 reviews)
(34, 'The Cuillin views from the kitchen window alone are worth the trip.', 13, 8, 5),
(35, 'Cozy and remote — bring groceries, the nearest shop is a proper drive.', 9, 8, 4),
-- Stay 9 — Marrakech Riad Retreat (6 reviews)
(36, 'The courtyard plunge pool was the perfect escape from the medina heat.', 6, 9, 5),
(37, 'Airport pickup made navigating the medina so much easier on arrival.', 16, 9, 5),
(38, 'Rooftop breakfast overlooking the rooftops of the medina was magical.', 14, 9, 4),
(39, 'Beautiful riad, but the walls are thin — you''ll hear the call to prayer clearly.', 20, 9, 3),
(40, 'Amara''s staff anticipated everything we needed before we asked.', 2, 9, 5),
(41, 'Authentic and gorgeously decorated, just book a guide for the first walk into the souks.', 19, 9, 4),
-- Stay 10 — Swiss Alpine Chalet (3 reviews)
(42, 'Ski-in/ski-out access to the Matterhorn glacier is genuinely as advertised.', 7, 10, 5),
(43, 'The private chef dinner was the highlight of our whole trip.', 11, 10, 5),
(44, 'Gorgeous chalet, just know the boot room fills up fast with a big group.', 17, 10, 4),
-- Stay 11 — Barcelona Boutique Hotel (7 reviews)
(45, 'Passeig de Gràcia location means Gaudí''s buildings are right outside the door.', 5, 11, 5),
(46, 'Rooftop pool was a great way to cool off after a day of sightseeing.', 13, 11, 4),
(47, 'Art Nouveau details everywhere, felt like staying inside a piece of history.', 9, 11, 5),
(48, 'Lovely hotel but the elevator queue got long during peak checkout hours.', 20, 11, 3),
(49, 'Breakfast spread was generous and the staff were endlessly patient with our Spanish.', 16, 11, 4),
(50, 'Rooftop terrace room had the best light in the evening. Would book again.', 6, 11, 5),
(51, 'Central without being noisy, exactly what we wanted for the trip.', 14, 11, 4),
-- Stay 12 — Cape Town Ocean Lodge (4 reviews)
(52, 'Woke up to Table Mountain every morning and the beach was a two-minute walk.', 19, 12, 5),
(53, 'Loved the beach gear they left for us, saved us renting our own.', 11, 12, 4),
(54, 'Gorgeous location, just check the load-shedding schedule before you arrive.', 17, 12, 4),
(55, 'One of the best ocean views we''ve had anywhere, and incredible value.', 2, 12, 5),
-- Stay 13 — Sydney Harbour Hotel (9 reviews)
(56, 'Woke up to the Opera House out our window every single morning.', 6, 13, 5),
(57, 'Five-minute walk to the ferry terminal made getting around the harbour effortless.', 14, 13, 5),
(58, 'Executive Suite was spacious and the harbour view rooms are worth requesting.', 20, 13, 4),
(59, 'Great location but pricier than similar hotels a few blocks back.', 7, 13, 3),
(60, 'Staff arranged a surprise anniversary setup in our room without being asked twice.', 9, 13, 5),
(61, 'Opera House King room lived up to the name, incredible views at sunset.', 13, 13, 4),
(62, 'Best harbour views in Sydney, hands down. Worth every dollar.', 16, 13, 5),
(63, 'Comfortable beds and a great breakfast buffet overlooking the water.', 19, 13, 4),
(64, 'Checked in early with no fuss and the room was already spotless.', 5, 13, 5),
-- Stay 14 — Reykjavik Northern Lights Cabin (2 reviews)
(65, 'We saw the aurora directly through the glass roof from bed. Unreal experience.', 11, 14, 5),
(66, 'Hot tub on the deck under the stars was the highlight of our Iceland trip.', 17, 14, 4),
-- Stay 15 — Amsterdam Canal House Hotel (5 reviews)
(67, 'Waking up to the canal view every morning made the whole trip feel like a postcard.', 13, 15, 5),
(68, 'Historic building with narrow stairs, but the bike rental made getting around effortless.', 9, 15, 4),
(69, 'Breakfast included and the canal house suite was beautifully restored.', 20, 15, 5),
(70, 'Charming hotel, just be ready for a lot of stairs if you''re not near the elevator.', 6, 15, 3),
(71, 'Best located hotel for exploring the canal ring on foot or by bike.', 16, 15, 5)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('review', 'id'), COALESCE(MAX(id), 1)) FROM review;


-- ============================================================
-- 9. USER FAVOURITES
-- ============================================================

INSERT INTO user_favorite (user_id, stay_id) VALUES
(2, 1), (2, 3), (2, 5), (2, 10),
(5, 2), (5, 5),
(6, 4), (6, 15),
(7, 2), (7, 13),
(9, 6), (9, 9),
(11, 7), (11, 10),
(13, 7), (13, 11),
(14, 8), (14, 14),
(16, 6), (16, 12),
(17, 9), (17, 11),
(19, 9), (19, 15),
(20, 13), (20, 14)
ON CONFLICT DO NOTHING;


-- ============================================================
-- 10. STAY ATTRIBUTES  (bridge tables)
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

-- ============================================================
-- POPULATION SCRIPT — TEST-READY SEED DATA
-- ============================================================
-- Safe to run multiple times: all inserts use explicit IDs with
-- ON CONFLICT DO NOTHING. Sequences are reset after each table.
--
-- Test credentials (BCrypt, cost 10):
--   plain-text password → "password"
--   hash               → $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
--
-- Booking status coverage:
--   Booking 1 → CONFIRMED  (upcoming, single room)
--   Booking 2 → CONFIRMED  (upcoming, single room)
--   Booking 3 → PENDING    (upcoming, multi-room)
--   Booking 4 → CANCELLED  (past)
--   Booking 5 → COMPLETED  (past)
--   Booking 6 → CONFIRMED  (upcoming, single room)
--
-- Free rooms with no active bookings: 3 (Deluxe Suite), 7 (Superior Suite)
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
(7, 'Mandarin')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('language', 'id'), COALESCE(MAX(id), 1)) FROM language;

INSERT INTO view (id, view_type) VALUES
(1, 'Ocean View'),
(2, 'Mountain View'),
(3, 'City Skyline'),
(4, 'Garden View'),
(5, 'Pool View'),
(6, 'Forest View')
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
(10, 'Breakfast Bar',           'PROPERTY_AMENITY')
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
(5, 'Four Seasons')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('property_brand', 'id'), COALESCE(MAX(id), 1)) FROM property_brand;

INSERT INTO traveler_experience (id, traveler_experience_type) VALUES
(1, 'Family Friendly'),
(2, 'Romantic Getaway'),
(3, 'Business Travel'),
(4, 'Adventure & Nature'),
(5, 'Backpacker Approved'),
(6, 'Wellness & Spa')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('traveler_experience', 'id'), COALESCE(MAX(id), 1)) FROM traveler_experience;


-- ============================================================
-- 2. USERS
-- ============================================================
-- IDs 1, 3, 4 are hosts; 2, 5, 6, 7 are guests.
-- All passwords: "password"

INSERT INTO "user" (id, name, email, password_hash) VALUES
(1, 'Alice Johnson',    'alice@test.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(2, 'Bob Smith',        'bob@test.com',     '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(3, 'Takashi Murakami', 'takashi@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(4, 'Clara Oswald',     'clara@test.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(5, 'David Kim',        'david@test.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(6, 'Emma García',      'emma@test.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'),
(7, 'Frank Lee',        'frank@test.com',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('"user"', 'id'), COALESCE(MAX(id), 1)) FROM "user";


-- ============================================================
-- 3. HOSTS
-- ============================================================

INSERT INTO host (id, communication_rating, checkin_process_rating, cancellation_rate) VALUES
(1, 98.5,  95.0, 2.1),   -- Alice:   top-rated
(3, 100.0, 98.0, 0.0),   -- Takashi: perfect score
(4, 90.0,  88.5, 5.4)    -- Clara:   good but not perfect
ON CONFLICT (id) DO NOTHING;

INSERT INTO host_language (host_id, language_id) VALUES
(1, 1), (1, 2),           -- Alice:   English, Spanish
(3, 1), (3, 3),           -- Takashi: English, Japanese
(4, 1), (4, 4), (4, 5)   -- Clara:   English, German, French
ON CONFLICT DO NOTHING;


-- ============================================================
-- 4. ADDRESSES & STAYS
-- ============================================================

INSERT INTO address (id, street_address, extended_address, city, state_province, postal_code, country_code) VALUES
(1, '123 Ocean Drive',       'Apt 4B',      'Miami',      'Florida',       '33139',    'US'),
(2, '4-56 Shinjuku',         'Floor 32',    'Tokyo',      'Tokyo',         '160-0022', 'JP'),
(3, '789 Alpine Way',        NULL,          'Valparaíso', 'Valparaíso',    '2340000',  'CL'),
(4, '12 Rue de Rivoli',      '3ème étage',  'Paris',      'Île-de-France', '75001',    'FR'),
(5, 'Jl. Monkey Forest 88',  NULL,          'Ubud',       'Bali',          '80571',    'ID')
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
    ST_GeogFromText('SRID=4326;POINT(115.2625 -8.5069)'))    -- Ubud, Bali, ID
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('stay', 'id'), COALESCE(MAX(id), 1)) FROM stay;


-- ============================================================
-- 5. ROOMS
-- ============================================================

INSERT INTO room (id, stay_id, name, price, sleeps, bedroom_amount, bathrooms, size) VALUES
-- Stay 1 — HOME: one room represents the whole property
(1, 1, 'Beachfront Suite',     120.50, 4, 2, 1.5,  85.0),
-- Stay 2 — HOTEL: three independently bookable rooms
(2, 2, 'Standard King',        350.00, 2, 1, 1.0,  45.5),
(3, 2, 'Deluxe Suite',         550.00, 4, 2, 2.0,  75.0),   -- never booked (free for availableRooms tests)
(4, 2, 'Executive Penthouse', 1200.00, 2, 1, 2.0,  90.0),
-- Stay 3 — HOME
(5, 3, 'Mountain Loft',         85.00, 6, 3, 2.5, 120.0),
-- Stay 4 — HOTEL: two rooms
(6, 4, 'Classic Double',       220.00, 2, 1, 1.0,  30.0),
(7, 4, 'Superior Suite',       480.00, 3, 2, 2.0,  60.0),   -- never booked (free for availableRooms tests)
-- Stay 5 — HOME
(8, 5, 'Jungle Pool Villa',    175.00, 2, 1, 1.0,  55.0)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('room', 'id'), COALESCE(MAX(id), 1)) FROM room;


-- ============================================================
-- 6. STAY PICTURES
-- ============================================================

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
(13, 5, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/bali-pool.png',       'Private infinity pool',       false, 1)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('stay_picture', 'id'), COALESCE(MAX(id), 1)) FROM stay_picture;


-- ============================================================
-- 7. BOOKINGS  (all four statuses covered)
-- ============================================================

-- total_price = sum(room prices) × nights
INSERT INTO booking (id, user_id, check_in_date, check_out_date, status, guests_count, created_at, total_price) VALUES
(1, 2, '2027-01-15', '2027-01-20', 'CONFIRMED', 2, '2026-06-01 10:00:00',  602.50),  -- Bob  → Beachfront Suite $120.50 × 5 nights
(2, 5, '2027-02-10', '2027-02-14', 'CONFIRMED', 1, '2026-06-05 14:30:00', 1400.00),  -- David → Standard King $350.00 × 4 nights
(3, 2, '2027-03-01', '2027-03-05', 'PENDING',   3, '2026-06-10 09:00:00', 2800.00),  -- Bob  → Classic Double $220 + Superior Suite $480 = $700 × 4 nights
(4, 6, '2026-08-01', '2026-08-07', 'CANCELLED', 4, '2026-05-20 11:00:00',  510.00),  -- Emma → Mountain Loft $85.00 × 6 nights
(5, 5, '2026-04-05', '2026-04-12', 'COMPLETED', 2, '2026-03-01 16:00:00', 1225.00),  -- David → Jungle Pool Villa $175.00 × 7 nights
(6, 7, '2027-04-20', '2027-04-25', 'CONFIRMED', 1, '2026-06-15 08:00:00', 6000.00)   -- Frank → Executive Penthouse $1200.00 × 5 nights
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('booking', 'id'), COALESCE(MAX(id), 1)) FROM booking;

INSERT INTO booking_room (booking_id, room_id) VALUES
(1, 1),        -- Bob → Beachfront Suite
(2, 2),        -- David → Standard King
(3, 6), (3, 7),-- Bob → Classic Double + Superior Suite (Paris, multi-room)
(4, 5),        -- Emma → Mountain Loft (cancelled)
(5, 8),        -- David → Jungle Pool Villa (completed)
(6, 4)         -- Frank → Executive Penthouse
ON CONFLICT DO NOTHING;


-- ============================================================
-- 8. REVIEWS
-- ============================================================

INSERT INTO review (id, text, user_id, stay_id, rating) VALUES
(1, 'Amazing stay! The ocean view was stunning and the host was incredibly welcoming.',      2, 1, 5),
(2, 'Incredible service and breathtaking views. The penthouse is worth every penny.',       7, 2, 5),
(3, 'Standard King was spotless. Tokyo from the 32nd floor at night is pure magic.',        5, 2, 4),
(4, 'Cold during winter but the cabin fireplace kept us warm. Absolutely beautiful.',        2, 3, 4),
(5, 'Bali exceeded all expectations. The pool villa is a dream. Will definitely be back!',  5, 5, 5),
(6, 'Paris was magical. Hotel perfectly located and the staff are wonderfully attentive.',   6, 4, 5)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('review', 'id'), COALESCE(MAX(id), 1)) FROM review;


-- ============================================================
-- 9. USER FAVOURITES
-- ============================================================

INSERT INTO user_favorite (user_id, stay_id) VALUES
(2, 1), (2, 3), (2, 5),
(5, 2), (5, 5),
(6, 4),
(7, 2)
ON CONFLICT DO NOTHING;


-- ============================================================
-- 10. STAY ATTRIBUTES  (bridge tables)
-- ============================================================

INSERT INTO stay_view (stay_id, view_id) VALUES
(1, 1),           -- Beachfront: Ocean View
(2, 3),           -- Tokyo hotel: City Skyline
(3, 2),           -- Cabin: Mountain View
(4, 4),           -- Paris: Garden View
(5, 5), (5, 6)    -- Bali: Pool View + Forest View
ON CONFLICT DO NOTHING;

INSERT INTO stay_amenity (stay_id, amenity_id) VALUES
(1, 1), (1, 2), (1, 4),
(2, 1), (2, 2), (2, 6), (2, 8),
(3, 1), (3, 4), (3, 5), (3, 9),
(4, 1), (4, 2), (4, 10),
(5, 1), (5, 3), (5, 4), (5, 8)
ON CONFLICT DO NOTHING;

INSERT INTO stay_accessibility (stay_id, accessibility_id) VALUES
(1, 1),
(2, 1), (2, 3), (2, 4),
(4, 1), (4, 3),
(5, 1)
ON CONFLICT DO NOTHING;

INSERT INTO stay_meal_plan (stay_id, meal_plan_id) VALUES
(1, 1),
(2, 1), (2, 2), (2, 3),
(3, 1),
(4, 1), (4, 2),
(5, 1), (5, 4)
ON CONFLICT DO NOTHING;

INSERT INTO stay_payment_type (stay_id, payment_type_id) VALUES
(1, 1), (1, 2),
(2, 1), (2, 4),
(3, 1), (3, 3),
(4, 1), (4, 2), (4, 4),
(5, 1), (5, 5)
ON CONFLICT DO NOTHING;

INSERT INTO stay_traveler_experience (stay_id, traveler_experience_id) VALUES
(1, 1), (1, 2),
(2, 3),
(3, 4), (3, 5),
(4, 2), (4, 3),
(5, 2), (5, 4), (5, 6)
ON CONFLICT DO NOTHING;

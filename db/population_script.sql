-- ==========================================
-- 1. POBLAR TABLAS DE CATÁLOGOS Y DICCIONARIOS
-- ==========================================

INSERT INTO language (language_name) VALUES
('English'), ('Spanish'), ('Japanese'), ('German'), ('French')
ON CONFLICT DO NOTHING;

INSERT INTO view (view_type) VALUES
('Ocean View'), ('Mountain View'), ('City Skyline'), ('Garden View'), ('Pool View')
ON CONFLICT DO NOTHING;

INSERT INTO amenity (name, type) VALUES
('High-Speed Wi-Fi', 'PROPERTY_AMENITY'),
('Air Conditioning', 'ROOM_AMENITY'),
('Private Pool', 'PROPERTY_AMENITY'),
('Fully Equipped Kitchen', 'ROOM_AMENITY'),
('Washing Machine', 'PROPERTY_AMENITY'),
('Gym Access', 'PROPERTY_AMENITY'),
('Balcony', 'ROOM_AMENITY')
ON CONFLICT DO NOTHING;

INSERT INTO accessibility (accessibility_type) VALUES
('Wheelchair Accessible Path'), ('Step-Free Bedroom'), ('Elevator Available'), ('Accessible Parking')
ON CONFLICT DO NOTHING;

INSERT INTO meal_plan (meal_plan_type) VALUES
('Room Only'), ('Breakfast Included'), ('Half Board'), ('All Inclusive')
ON CONFLICT DO NOTHING;

INSERT INTO payment_type (payment_type) VALUES
('Credit Card'), ('PayPal'), ('Cryptocurrency'), ('Bank Transfer')
ON CONFLICT DO NOTHING;

INSERT INTO property_brand (brand_name) VALUES
('Independent'), ('Hilton Hotels'), ('Marriott International'), ('Hyatt Regency')
ON CONFLICT DO NOTHING;

INSERT INTO traveler_experience (traveler_experience_type) VALUES
('Family Friendly'), ('Romantic Getaway'), ('Business Travel'), ('Adventure & Nature'), ('Backpacker Approved')
ON CONFLICT DO NOTHING;

-- ==========================================
-- 2. POBLAR USUARIOS Y ANFITRIONES (HOSTS)
-- ==========================================

INSERT INTO "user" (id, name) VALUES
(1, 'Alice Johnson'),
(2, 'Bob Smith'),
(3, 'Takashi Murakami'),
(4, 'Clara Oswald'),
(5, 'David Kim')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('"user"', 'id'), COALESCE(MAX(id), 1)) FROM "user";

INSERT INTO host (id, communication_rating, checkin_process_rating, cancellation_rate) VALUES
(1, 98.5, 95.0, 2.1),
(3, 100.0, 98.0, 0.0),
(4, 90.0, 88.5, 5.4)
ON CONFLICT (id) DO NOTHING;

-- ==========================================
-- 3. POBLAR DIRECCIONES Y PROPIEDADES
-- ==========================================

INSERT INTO address (id, street_address, extended_address, city, state_province, postal_code, country_code) VALUES
(1, '123 Ocean Drive', 'Apt 4B',   'Miami',       'Florida',     '33139',   'US'),
(2, '4-56 Shinjuku',  'Floor 32', 'Tokyo',        'Tokyo',       '160-0022','JP'),
(3, '789 Alpine Way', NULL,        'Valparaíso',  'Valparaíso',  '2340000', 'CL')
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('address', 'id'), COALESCE(MAX(id), 1)) FROM address;

INSERT INTO stay (
    id, name, about, property_type, is_refundable,
    star_rating, days_from_booking_cancellation_deadline,
    policies_text, important_information,
    host_id, address_id, property_brand_id
) VALUES
(
    1, 'Cozy Beachfront House',
    'Beautiful house right next to the shore.',
    'HOME', true, 4.5, 5,
    'No pets allowed.', 'Check-in after 3 PM.',
    1, 1, 1
),
(
    2, 'Luxury Tokyo Sky Hotel',
    'High-rise hotel overlooking the city lights.',
    'HOTEL', false, 5.0, 2,
    'No smoking inside.', 'Passport required at check-in.',
    3, 2, 3
),
(
    3, 'Charming Mountain Cabin',
    'A quiet retreat surrounded by nature.',
    'HOME', true, 4.0, 7,
    'Keep noise down after 10 PM.', 'Bring warm clothes.',
    4, 3, 1
)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('stay', 'id'), COALESCE(MAX(id), 1)) FROM stay;

-- ==========================================
-- 4. POBLAR HABITACIONES
-- ==========================================

-- HOME stays get one room representing the whole property.
-- HOTEL stays get multiple independently bookable rooms.
INSERT INTO room (id, stay_id, name, price, sleeps, bedroom_amount, bathrooms, size) VALUES
(1, 1, 'Beachfront Suite',  120.50, 4, 2, 1.5, 85.0),
(2, 2, 'Standard King',     350.00, 2, 1, 1.0, 45.5),
(3, 2, 'Deluxe Suite',      550.00, 4, 2, 2.0, 75.0),
(4, 3, 'Mountain Loft',      85.00, 6, 3, 2.5, 120.0)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('room', 'id'), COALESCE(MAX(id), 1)) FROM room;

-- ==========================================
-- 5. POBLAR FOTOS DE PROPIEDADES
-- ==========================================

INSERT INTO stay_picture (id, stay_id, url, caption, is_primary, display_order) VALUES
(1, 1, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/beach-exterior.png',  'Ocean-facing exterior',      true,  0),
(2, 1, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/living-room.png',     'Open-plan living area',      false, 1),
(3, 2, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/lobby.png',           'Hotel lobby',                true,  0),
(4, 2, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/standard-king.png',   'Standard King room',         false, 1),
(5, 2, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/deluxe-suite.png',    'Deluxe Suite living area',   false, 2),
(6, 3, 'https://softserve-labpro-team1-store.s3.us-east-2.amazonaws.com/images/cabin-exterior.png',  'Cabin surrounded by pines',  true,  0)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('stay_picture', 'id'), COALESCE(MAX(id), 1)) FROM stay_picture;

-- ==========================================
-- 6. POBLAR RESERVAS
-- ==========================================

INSERT INTO booking (id, user_id, check_in_date, check_out_date, status, guests_count) VALUES
(1, 2, '2026-07-15', '2026-07-20', 'CONFIRMED', 2),
(2, 5, '2026-08-01', '2026-08-04', 'CONFIRMED', 1),
(3, 2, '2026-09-10', '2026-09-14', 'PENDING',   6)
ON CONFLICT (id) DO NOTHING;

SELECT setval(pg_get_serial_sequence('booking', 'id'), COALESCE(MAX(id), 1)) FROM booking;

-- Which rooms each booking covers
INSERT INTO booking_room (booking_id, room_id) VALUES
(1, 1),       -- Bob's booking: Beachfront Suite (HOME, single room)
(2, 2),       -- David's booking: Standard King (Tokyo hotel)
(3, 2), (3, 3) -- Bob's pending booking: Standard King + Deluxe Suite (multi-room, same hotel)
ON CONFLICT DO NOTHING;

-- ==========================================
-- 7. POBLAR RESEÑAS
-- ==========================================

INSERT INTO review (text, user_id, stay_id) VALUES
('Amazing stay! The ocean view was stunning.', 2, 1),
('Incredible service and view, highly recommend.', 5, 2),
('A bit cold during winter, but the cabin is beautiful.', 2, 3)
ON CONFLICT DO NOTHING;

-- ==========================================
-- 8. POBLAR RELACIONES MUCHOS A MUCHOS (BRIDGE TABLES)
-- ==========================================

INSERT INTO user_favorite (user_id, stay_id) VALUES
(2, 1), (2, 3), (5, 2)
ON CONFLICT DO NOTHING;

INSERT INTO host_language (host_id, language_id) VALUES
(1, 1), (1, 2),
(3, 1), (3, 3),
(4, 1), (4, 4)
ON CONFLICT DO NOTHING;

INSERT INTO stay_view (stay_id, view_id) VALUES
(1, 1),
(2, 3),
(3, 2)
ON CONFLICT DO NOTHING;

INSERT INTO stay_amenity (stay_id, amenity_id) VALUES
(1, 1), (1, 2), (1, 4),
(2, 1), (2, 2), (2, 6),
(3, 4), (3, 5), (3, 7)
ON CONFLICT DO NOTHING;

INSERT INTO stay_accessibility (stay_id, accessibility_id) VALUES
(1, 1),
(2, 1), (2, 3)
ON CONFLICT DO NOTHING;

INSERT INTO stay_meal_plan (stay_id, meal_plan_id) VALUES
(1, 1),
(2, 1), (2, 2)
ON CONFLICT DO NOTHING;

INSERT INTO stay_payment_type (stay_id, payment_type_id) VALUES
(1, 1), (1, 2),
(2, 1), (2, 4),
(3, 1), (3, 3)
ON CONFLICT DO NOTHING;

INSERT INTO stay_traveler_experience (stay_id, traveler_experience_id) VALUES
(1, 1),
(2, 3),
(3, 4), (3, 5)
ON CONFLICT DO NOTHING;

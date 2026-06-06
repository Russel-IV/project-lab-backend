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

-- Insertar usuarios base
INSERT INTO "user" (id, name) VALUES
(1, 'Alice Johnson'),
(2, 'Bob Smith'),
(3, 'Takashi Murakami'),
(4, 'Clara Oswald'),
(5, 'David Kim')
ON CONFLICT (id) DO NOTHING;

-- Sincronizar la secuencia de IDs de user tras inserciones explícitas
SELECT setval(pg_get_serial_sequence('"user"', 'id'), COALESCE(MAX(id), 1)) FROM "user";

-- Registrar anfitriones vinculados a sus respectivos usuarios
INSERT INTO host (id, communication_rating, checkin_process_rating, cancellation_rate) VALUES
(1, 98.5, 95.0, 2.1), -- Alice es Host
(3, 100.0, 98.0, 0.0), -- Takashi es Host
(4, 90.0, 88.5, 5.4)  -- Clara es Host
ON CONFLICT (id) DO NOTHING;

-- ==========================================
-- 3. POBLAR TABLAS OPERACIONALES CORE
-- ==========================================

-- Insertar propiedades de alojamiento (Stays)
INSERT INTO stay (
    id, price, name, about, property_type, street_address, extended_address,
    city, state_province, postal_code, country_code, is_available, is_refundable,
    star_rating, sleeps, bedroom_amount, bathrooms, size, days_from_booking_cancellation_deadline,
    policies_text, important_information, host_id, property_brand_id
) VALUES
(
    1, 120.50, 'Cozy Beachfront House', 'Beautiful house right next to the shore.', 'HOME',
    '123 Ocean Drive', 'Apt 4B', 'Miami', 'Florida', '33139', 'US', true, true,
    4.5, 4, 2, 1.5, 85.0, 5, 'No pets allowed.', 'Check-in after 3 PM.', 1, 1
),
(
    2, 350.00, 'Luxury Tokyo Sky Hotel Room', 'High-rise room overlooking the city lights.', 'HOTEL',
    '4-56 Shinjuku', 'Floor 32', 'Tokyo', 'Tokyo', '160-0022', 'JP', true, false,
    5.0, 2, 1, 1.0, 45.5, 2, 'No smoking inside.', 'Passport required at check-in.', 3, 3
),
(
    3, 85.00, 'Charming Mountain Cabin', 'A quiet retreat surrounded by nature.', 'HOME',
    '789 Alpine Way', NULL, 'Valparaíso', 'Valparaíso', '2340000', 'CL', true, true,
    4.0, 6, 3, 2.5, 120.0, 7, 'Keep noise down after 10 PM.', 'Bring warm clothes.', 4, 1
)
ON CONFLICT (id) DO NOTHING;

-- Sincronizar la secuencia de IDs de stay
SELECT setval(pg_get_serial_sequence('stay', 'id'), COALESCE(MAX(id), 1)) FROM stay;

-- Insertar reseñas escritas por usuarios a propiedades
INSERT INTO review (text, user_id, stay_id) VALUES
('Amazing stay! The ocean view was stunning.', 2, 1), -- Bob evalúa Cozy Beachfront
('Incredible service and view, highly recommend.', 5, 2), -- David evalúa Tokyo Sky Hotel
('A bit cold during winter, but the cabin is beautiful.', 2, 3) -- Bob evalúa Mountain Cabin
ON CONFLICT DO NOTHING;

-- ==========================================
-- 4. POBLAR RELACIONES MUCHOS A MUCHOS (BRIDGE TABLES)
-- ==========================================

-- Favoritos de usuarios
INSERT INTO user_favorite (user_id, stay_id) VALUES
(2, 1), (2, 3), (5, 2)
ON CONFLICT DO NOTHING;

-- Idiomas hablados por los anfitriones
INSERT INTO host_language (host_id, language_id) VALUES
(1, 1), (1, 2), -- Alice habla inglés y español
(3, 1), (3, 3), -- Takashi habla inglés y japonés
(4, 1), (4, 4)  -- Clara habla inglés y alemán
ON CONFLICT DO NOTHING;

-- Vistas de las propiedades
INSERT INTO stay_view (stay_id, view_id) VALUES
(1, 1), -- Cozy House: Ocean View
(2, 3), -- Tokyo Hotel: City Skyline
(3, 2)  -- Mountain Cabin: Mountain View
ON CONFLICT DO NOTHING;

-- Amenidades por propiedad
INSERT INTO stay_amenity (stay_id, amenity_id) VALUES
(1, 1), (1, 2), (1, 4), -- Cozy House: Wi-Fi, AC, Kitchen
(2, 1), (2, 2), (2, 6), -- Tokyo Hotel: Wi-Fi, AC, Gym
(3, 4), (3, 5), (3, 7)  -- Mountain Cabin: Kitchen, Washing Machine, Balcony
ON CONFLICT DO NOTHING;

-- Accesibilidad por propiedad
INSERT INTO stay_accessibility (stay_id, accessibility_id) VALUES
(1, 1), -- Cozy House: Wheelchair Path
(2, 1), (2, 3) -- Tokyo Hotel: Wheelchair Path, Elevator
ON CONFLICT DO NOTHING;

-- Planes de comida disponibles por propiedad
INSERT INTO stay_meal_plan (stay_id, meal_plan_id) VALUES
(1, 1), -- Cozy House: Room Only
(2, 1), (2, 2) -- Tokyo Hotel: Room Only & Breakfast
ON CONFLICT DO NOTHING;

-- Métodos de pago aceptados por propiedad
INSERT INTO stay_payment_type (stay_id, payment_type_id) VALUES
(1, 1), (1, 2), -- Cozy House: Credit Card, PayPal
(2, 1), (2, 4), -- Tokyo Hotel: Credit Card, Bank Transfer
(3, 1), (3, 3)  -- Mountain Cabin: Credit Card, Crypto
ON CONFLICT DO NOTHING;

-- Experiencias de viajero asociadas a la propiedad
INSERT INTO stay_traveler_experience (stay_id, traveler_experience_id) VALUES
(1, 1), -- Cozy House: Family Friendly
(2, 3), -- Tokyo Hotel: Business Travel
(3, 4), (3, 5) -- Mountain Cabin: Adventure, Backpacker Approved
ON CONFLICT DO NOTHING;

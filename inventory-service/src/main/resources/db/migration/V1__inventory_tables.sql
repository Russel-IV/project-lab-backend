-- Consolidates gateway's V1 (property_type/amenity_type enums), V2 (lookup tables,
-- minus host/language/user which stayed with Identity), V3 (address/stay/room), V4
-- (stay_* bridge tables), V5 (indexes), V9 (PostGIS location) into this service's own
-- history (docs/adr/0002, docs/adr/0010, Phase 5 of the migration plan). No FK to
-- host/user (Identity, Phase 4) or to booking/booking_room (Booking stays in gateway's
-- database until Phase 6) — existence there is trusted from the caller or checked via
-- Feign, per docs/adr/0011.
CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TYPE property_type AS ENUM ('HOME', 'HOTEL');
CREATE TYPE amenity_type AS ENUM ('ROOM_AMENITY', 'PROPERTY_AMENITY');

CREATE TABLE view (
    id SERIAL PRIMARY KEY,
    view_type VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE amenity (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL
);

CREATE TABLE accessibility (
    id SERIAL PRIMARY KEY,
    accessibility_type VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE meal_plan (
    id SERIAL PRIMARY KEY,
    meal_plan_type VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE payment_type (
    id SERIAL PRIMARY KEY,
    payment_type VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE property_brand (
    id SERIAL PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE traveler_experience (
    id SERIAL PRIMARY KEY,
    traveler_experience_type VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE address (
    id SERIAL PRIMARY KEY,
    street_address TEXT NOT NULL,
    extended_address TEXT,
    city TEXT NOT NULL,
    state_province TEXT,
    postal_code TEXT,
    country_code CHAR(2) NOT NULL
);

CREATE TABLE stay (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    about TEXT,
    property_type property_type NOT NULL,
    is_refundable BOOLEAN NOT NULL DEFAULT FALSE,
    star_rating DECIMAL(2, 1),
    days_from_booking_cancellation_deadline INT,
    policies_text TEXT,
    important_information TEXT,
    host_id INT NOT NULL,
    address_id INT NOT NULL,
    property_brand_id INT,
    location GEOGRAPHY(POINT, 4326),

    CONSTRAINT fk_stay_address
        FOREIGN KEY (address_id)
        REFERENCES address(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_stay_brand
        FOREIGN KEY (property_brand_id)
        REFERENCES property_brand(id)
        ON DELETE SET NULL,

    CONSTRAINT check_star_rating
        CHECK (star_rating >= 0.0 AND star_rating <= 5.0),

    CONSTRAINT check_days_from_booking_cancellation_deadline
        CHECK (days_from_booking_cancellation_deadline >= 0),

    UNIQUE (address_id)
);

CREATE INDEX idx_stay_host_id ON stay(host_id);
CREATE INDEX idx_stay_address_id ON stay(address_id);
CREATE INDEX idx_stay_brand_id ON stay(property_brand_id);
CREATE INDEX idx_stay_location ON stay USING GIST(location);

CREATE TABLE room (
    id SERIAL PRIMARY KEY,
    stay_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    sleeps INT NOT NULL,
    bedroom_amount INT NOT NULL,
    bathrooms DECIMAL(3, 1) NOT NULL,
    size DECIMAL(10, 1),

    CONSTRAINT fk_room_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE,

    CONSTRAINT check_room_price
        CHECK (price >= 0.00),

    CONSTRAINT check_room_bathrooms
        CHECK (bathrooms >= 0.0),

    CONSTRAINT check_room_bedroom_amount
        CHECK (bedroom_amount >= 0),

    CONSTRAINT check_room_sleeps
        CHECK (sleeps > 0),

    CONSTRAINT check_room_size
        CHECK (size >= 0.0)
);

CREATE INDEX idx_room_stay_id ON room(stay_id);

CREATE TABLE stay_view (
    stay_id INT NOT NULL REFERENCES stay(id) ON DELETE CASCADE,
    view_id INT NOT NULL REFERENCES view(id) ON DELETE CASCADE,
    PRIMARY KEY (stay_id, view_id)
);
CREATE INDEX idx_fk_sv_view ON stay_view(view_id);

CREATE TABLE stay_amenity (
    stay_id INT NOT NULL REFERENCES stay(id) ON DELETE CASCADE,
    amenity_id INT NOT NULL REFERENCES amenity(id) ON DELETE CASCADE,
    PRIMARY KEY (stay_id, amenity_id)
);
CREATE INDEX idx_fk_sa_amenity ON stay_amenity(amenity_id);

CREATE TABLE stay_accessibility (
    stay_id INT NOT NULL REFERENCES stay(id) ON DELETE CASCADE,
    accessibility_id INT NOT NULL REFERENCES accessibility(id) ON DELETE CASCADE,
    PRIMARY KEY (stay_id, accessibility_id)
);
CREATE INDEX idx_fk_sc_accessibility ON stay_accessibility(accessibility_id);

CREATE TABLE stay_meal_plan (
    stay_id INT NOT NULL REFERENCES stay(id) ON DELETE CASCADE,
    meal_plan_id INT NOT NULL REFERENCES meal_plan(id) ON DELETE CASCADE,
    PRIMARY KEY (stay_id, meal_plan_id)
);
CREATE INDEX idx_fk_sm_meal_plan ON stay_meal_plan(meal_plan_id);

CREATE TABLE stay_payment_type (
    stay_id INT NOT NULL REFERENCES stay(id) ON DELETE CASCADE,
    payment_type_id INT NOT NULL REFERENCES payment_type(id) ON DELETE CASCADE,
    PRIMARY KEY (stay_id, payment_type_id)
);
CREATE INDEX idx_fk_sp_payment ON stay_payment_type(payment_type_id);

CREATE TABLE stay_traveler_experience (
    stay_id INT NOT NULL REFERENCES stay(id) ON DELETE CASCADE,
    traveler_experience_id INT NOT NULL REFERENCES traveler_experience(id) ON DELETE CASCADE,
    PRIMARY KEY (stay_id, traveler_experience_id)
);
CREATE INDEX idx_fk_se_experience ON stay_traveler_experience(traveler_experience_id);

CREATE TABLE IF NOT EXISTS "user" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS host (
    id INT PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE,
    communication_rating NUMERIC(4, 1),
    checkin_process_rating NUMERIC(4, 1),
    cancellation_rate NUMERIC(4, 1),

    CONSTRAINT "check_communication_rating"
        CHECK (communication_rating >= 0.0 AND communication_rating <= 100.0),

    CONSTRAINT "check_checkin_process_rating"
        CHECK (checkin_process_rating >= 0.0 AND checkin_process_rating <= 100.0),

    CONSTRAINT "check_cancellation_rate"
        CHECK (cancellation_rate >= 0.0 AND cancellation_rate <= 100.0)
);

CREATE TABLE IF NOT EXISTS language (
    id SERIAL PRIMARY KEY,
    language_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS view (
    id SERIAL PRIMARY KEY,
    view_type VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS amenity (
    id SERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS accessibility (
    id SERIAL PRIMARY KEY,
    accessibility_type VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS meal_plan (
    id SERIAL PRIMARY KEY,
    meal_plan_type VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS payment_type (
    id SERIAL PRIMARY KEY,
    payment_type VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS property_brand (
    id SERIAL PRIMARY KEY,
    brand_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS traveler_experience (
    id SERIAL PRIMARY KEY,
    traveler_experience_type VARCHAR(100) NOT NULL UNIQUE
);

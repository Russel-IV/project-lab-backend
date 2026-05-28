-- ==========================================
-- 1. LOOKUP & DICTIONARY TABLES
-- ==========================================

CREATE TABLE IF NOT EXISTS "USER" (
    "id" SERIAL PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS "HOST" (
    "id" INT PRIMARY KEY REFERENCES "USER"("id") ON DELETE CASCADE,
    "communication_rating" NUMERIC(4, 1),
    "checkin_process_rating" NUMERIC(4, 1),
    "cancellation_rate" NUMERIC(4, 1),

    CONSTRAINT "check_communication_rating"
        CHECK (communication_rating >= 0.0 AND communication_rating <= 100.0),

    CONSTRAINT "check_checkin_process_rating"
        CHECK (checkin_process_rating >= 0.0 AND checkin_process_rating <= 100.0),

    CONSTRAINT "check_cancellation_rate"
        CHECK (cancellation_rate >= 0.0 AND cancellation_rate <= 100.0)
);

CREATE TABLE IF NOT EXISTS "LANGUAGE" (
    "id" SERIAL PRIMARY KEY,
    "language_name" VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "VIEW" (
    "id" SERIAL PRIMARY KEY,
    "view_type" VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "AMENITY" (
    "id" SERIAL PRIMARY KEY,
    "name" VARCHAR(150) NOT NULL UNIQUE,
    "type" amenity_type NOT NULL
);

CREATE TABLE IF NOT EXISTS "ACCESSIBILITY" (
    "id" SERIAL PRIMARY KEY,
    "accessibility_type" VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "MEAL_PLAN" (
    "id" SERIAL PRIMARY KEY,
    "meal_plan_type" VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "PAYMENT_TYPE" (
    "id" SERIAL PRIMARY KEY,
    "payment_type" VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "PROPERTY_BRAND" (
    "id" SERIAL PRIMARY KEY,
    "brand_name" VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "TRAVELER_EXPERIENCE" (
    "id" SERIAL PRIMARY KEY,
    "traveler_experience_type" VARCHAR(100) NOT NULL UNIQUE
);

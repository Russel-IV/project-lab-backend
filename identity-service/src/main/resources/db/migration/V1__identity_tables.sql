-- Consolidates gateway's V2 (user/host/language), V4 (host_language), V6 (auth
-- columns), V12 (phone), V13 (payment_method), V14 (profile_picture_url), V15 (soft
-- delete), V16 (payment_method default) into this service's own history (docs/adr/0002,
-- Phase 4 of the migration plan). Host->User and PaymentMethod->User FKs stay — both
-- tables live in this same database now. No FK to stay/booking (those never existed on
-- this side; the FK direction was always stay.host_id/booking.user_id -> here).
CREATE TABLE "user" (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(320),
    password_hash TEXT,
    phone VARCHAR(32),
    profile_picture_url TEXT,
    deleted_at TIMESTAMP
);

CREATE UNIQUE INDEX uq_user_email ON "user"(email) WHERE email IS NOT NULL AND deleted_at IS NULL;

CREATE TABLE host (
    id INT PRIMARY KEY REFERENCES "user"(id) ON DELETE CASCADE,
    communication_rating NUMERIC(4,1) CHECK (communication_rating BETWEEN 0 AND 100),
    checkin_process_rating NUMERIC(4,1) CHECK (checkin_process_rating BETWEEN 0 AND 100),
    cancellation_rate NUMERIC(4,1) CHECK (cancellation_rate BETWEEN 0 AND 100)
);

CREATE TABLE language (
    id SERIAL PRIMARY KEY,
    language_name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE host_language (
    host_id INT NOT NULL REFERENCES host(id) ON DELETE CASCADE,
    language_id INT NOT NULL REFERENCES language(id) ON DELETE CASCADE,
    PRIMARY KEY (host_id, language_id)
);

CREATE TABLE payment_method (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    stripe_payment_method_id TEXT NOT NULL,
    brand VARCHAR(32) NOT NULL,
    last_four VARCHAR(4) NOT NULL,
    type VARCHAR(32) NOT NULL,
    expiry_month INT NOT NULL,
    expiry_year INT NOT NULL,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_payment_method_user_id ON payment_method(user_id);

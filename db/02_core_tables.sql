-- ==========================================
-- 2. CORE OPERATIONAL TABLES
-- ==========================================

CREATE TABLE IF NOT EXISTS stay (
    id SERIAL PRIMARY KEY,
    price DECIMAL(10, 2) NOT NULL,
    name TEXT NOT NULL,
    about TEXT,
    property_type property_type,
    street_address TEXT NOT NULL,
    extended_address TEXT,
    city TEXT NOT NULL,
    state_province TEXT,
    postal_code TEXT,
    country_code TEXT,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    is_refundable BOOLEAN NOT NULL DEFAULT FALSE,
    star_rating DECIMAL(1, 1),
    sleeps INT NOT NULL,
    bedroom_amount INT NOT NULL,
    bathrooms DECIMAL(3, 1) NOT NULL, -- e.g., 2.5 bathrooms
    size DECIMAL(10, 1),
    days_from_booking_cancellation_deadline INT,
    policies_text TEXT,
    important_information TEXT,
    host_id INT NOT NULL,
    property_brand_id INT,

    CONSTRAINT fk_stay_host
        FOREIGN KEY (host_id)
        REFERENCES host(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_stay_brand
        FOREIGN KEY (property_brand_id)
        REFERENCES property_brand(id)
        ON DELETE SET NULL,

    CONSTRAINT check_stay_price
        CHECK (price >= 0.00),

    CONSTRAINT check_star_rating
        CHECK (star_rating >= 0.0 AND star_rating <= 5.0),

    CONSTRAINT check_bathrooms
        CHECK (bathrooms >= 0.0),

    CONSTRAINT check_bedroom_amount
        CHECK (bedroom_amount >= 0),

    CONSTRAINT check_sleeps
        CHECK (sleeps > 0),

    CONSTRAINT check_size
        CHECK (size >= 0.0),

    CONSTRAINT check_days_from_booking_cancellation_deadline
        CHECK (days_from_booking_cancellation_deadline >= 0)
);

CREATE TABLE IF NOT EXISTS review (
    id SERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    user_id INT NOT NULL,
    stay_id INT NOT NULL,

    CONSTRAINT fk_review_user
        FOREIGN KEY (user_id)
        REFERENCES "user"(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_review_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE
);

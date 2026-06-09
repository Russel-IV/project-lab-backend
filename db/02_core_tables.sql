-- ==========================================
-- 2. CORE OPERATIONAL TABLES
-- ==========================================

CREATE TABLE IF NOT EXISTS address (
    id SERIAL PRIMARY KEY,
    street_address TEXT NOT NULL,
    extended_address TEXT,
    city TEXT NOT NULL,
    state_province TEXT,
    postal_code TEXT,
    country_code CHAR(2) NOT NULL
);

CREATE TABLE IF NOT EXISTS stay (
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

    CONSTRAINT fk_stay_host
        FOREIGN KEY (host_id)
        REFERENCES host(id)
        ON DELETE RESTRICT,

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

CREATE TABLE IF NOT EXISTS room (
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

CREATE TABLE IF NOT EXISTS stay_picture (
    id SERIAL PRIMARY KEY,
    stay_id INT NOT NULL,
    url TEXT NOT NULL,
    caption TEXT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_picture_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE,

    CONSTRAINT check_display_order
        CHECK (display_order >= 0)
);

CREATE TABLE IF NOT EXISTS booking (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status booking_status NOT NULL DEFAULT 'PENDING',
    guests_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id)
        REFERENCES "user"(id)
        ON DELETE RESTRICT,

    CONSTRAINT check_booking_dates
        CHECK (check_out_date > check_in_date),

    CONSTRAINT check_booking_guests
        CHECK (guests_count > 0)
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

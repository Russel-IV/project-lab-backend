-- Mocked-Stripe payment intents, same spirit as identity-service's payment_method
-- table. Lives in booking-service so createBooking can verify one against fresh
-- room data in the same transaction/database as the booking it produces.

CREATE TABLE IF NOT EXISTS payment_intent (
    id SERIAL PRIMARY KEY,
    payment_intent_id VARCHAR(64) NOT NULL UNIQUE,
    idempotency_key VARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guests_count INT NOT NULL,
    -- Smallest currency unit (e.g. cents), Stripe-style.
    amount INT NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'usd',
    client_secret VARCHAR(128) NOT NULL,
    -- Set once consumed by createBooking; doubles as its idempotency marker.
    booking_id INT REFERENCES booking(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT check_payment_intent_dates
        CHECK (check_out_date > check_in_date),

    CONSTRAINT check_payment_intent_guests
        CHECK (guests_count > 0),

    CONSTRAINT uq_payment_intent_user_idempotency
        UNIQUE (user_id, idempotency_key)
);

CREATE TABLE IF NOT EXISTS payment_intent_room (
    payment_intent_id INT NOT NULL,
    room_id INT NOT NULL,

    CONSTRAINT fk_pir_payment_intent
        FOREIGN KEY (payment_intent_id)
        REFERENCES payment_intent(id)
        ON DELETE CASCADE,

    PRIMARY KEY (payment_intent_id, room_id)
);

CREATE INDEX idx_payment_intent_user ON payment_intent(user_id);

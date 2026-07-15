-- Fresh database (docs/adr/0011) for booking-service, extracted per Phase 6 of the
-- migration plan. No FK to "user" or "room": both live in other services now
-- (identity-service, inventory-service) — existence is trusted from the caller
-- (userId implied by a valid JWT, per docs/adr/0011) or Feign-validated at
-- creation time (roomIds, via booking-service's own RoomFeignClient), never a DB-level
-- constraint across a service boundary.

CREATE TYPE booking_status AS ENUM ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED');

CREATE TABLE IF NOT EXISTS booking (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    status booking_status NOT NULL DEFAULT 'PENDING',
    guests_count INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,

    CONSTRAINT check_booking_dates
        CHECK (check_out_date > check_in_date),

    CONSTRAINT check_booking_guests
        CHECK (guests_count > 0)
);

CREATE TABLE IF NOT EXISTS booking_room (
    booking_id INT NOT NULL,
    room_id INT NOT NULL,

    CONSTRAINT fk_br_booking
        FOREIGN KEY (booking_id)
        REFERENCES booking(id)
        ON DELETE CASCADE,

    PRIMARY KEY (booking_id, room_id)
);

CREATE INDEX idx_booking_user ON booking(user_id);

-- ==========================================
-- 3. MANY-TO-MANY BRIDGE TABLES
-- ==========================================

CREATE TABLE IF NOT EXISTS user_favorite (
    user_id INT NOT NULL,
    stay_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_favorite_user
        FOREIGN KEY (user_id)
        REFERENCES "user"(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_favorite_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE,

    PRIMARY KEY (user_id, stay_id)
);

CREATE TABLE IF NOT EXISTS host_language (
    host_id INT NOT NULL,
    language_id INT NOT NULL,

    CONSTRAINT fk_hl_host
        FOREIGN KEY (host_id)
        REFERENCES host(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_hl_language
        FOREIGN KEY (language_id)
        REFERENCES language(id)
        ON DELETE CASCADE,

    PRIMARY KEY (host_id, language_id)
);

CREATE TABLE IF NOT EXISTS stay_view (
    stay_id INT NOT NULL,
    view_id INT NOT NULL,

    CONSTRAINT fk_sv_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sv_view
        FOREIGN KEY (view_id)
        REFERENCES view(id)
        ON DELETE CASCADE,

    PRIMARY KEY (stay_id, view_id)
);

CREATE TABLE IF NOT EXISTS stay_amenity (
    stay_id INT NOT NULL,
    amenity_id INT NOT NULL,

    CONSTRAINT fk_sa_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sa_amenity
        FOREIGN KEY (amenity_id)
        REFERENCES amenity(id)
        ON DELETE CASCADE,

    PRIMARY KEY (stay_id, amenity_id)
);

CREATE TABLE IF NOT EXISTS stay_accessibility (
    stay_id INT NOT NULL,
    accessibility_id INT NOT NULL,

    CONSTRAINT fk_sc_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sc_accessibility
        FOREIGN KEY (accessibility_id)
        REFERENCES accessibility(id)
        ON DELETE CASCADE,

    PRIMARY KEY (stay_id, accessibility_id)
);

CREATE TABLE IF NOT EXISTS stay_meal_plan (
    stay_id INT NOT NULL,
    meal_plan_id INT NOT NULL,

    CONSTRAINT fk_sm_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sm_meal_plan
        FOREIGN KEY (meal_plan_id)
        REFERENCES meal_plan(id)
        ON DELETE CASCADE,

    PRIMARY KEY (stay_id, meal_plan_id)
);

CREATE TABLE IF NOT EXISTS stay_payment_type (
    stay_id INT NOT NULL,
    payment_type_id INT NOT NULL,

    CONSTRAINT fk_sp_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sp_payment
        FOREIGN KEY (payment_type_id)
        REFERENCES payment_type(id)
        ON DELETE CASCADE,

    PRIMARY KEY (stay_id, payment_type_id)
);

CREATE TABLE IF NOT EXISTS stay_traveler_experience (
    stay_id INT NOT NULL,
    traveler_experience_id INT NOT NULL,

    CONSTRAINT fk_se_stay
        FOREIGN KEY (stay_id)
        REFERENCES stay(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_se_experience
        FOREIGN KEY (traveler_experience_id)
        REFERENCES traveler_experience(id)
        ON DELETE CASCADE,

    PRIMARY KEY (stay_id, traveler_experience_id)
);

CREATE TABLE IF NOT EXISTS booking_room (
    booking_id INT NOT NULL,
    room_id INT NOT NULL,

    CONSTRAINT fk_br_booking
        FOREIGN KEY (booking_id)
        REFERENCES booking(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_br_room
        FOREIGN KEY (room_id)
        REFERENCES room(id)
        ON DELETE RESTRICT,

    PRIMARY KEY (booking_id, room_id)
);

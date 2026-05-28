-- ==========================================
-- 3. MANY-TO-MANY BRIDGE TABLES
-- ==========================================

CREATE TABLE IF NOT EXISTS "USER_FAVORITE" (
    "user_id" INT NOT NULL,
    "stay_id" INT NOT NULL,
    "created_at" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "fk_favorite_user"
        FOREIGN KEY ("user_id")
        REFERENCES "USER"("id")
        ON DELETE CASCADE,

    CONSTRAINT "fk_favorite_stay"
        FOREIGN KEY ("stay_id")
        REFERENCES "STAY"("id")
        ON DELETE CASCADE,

    PRIMARY KEY ("user_id", "stay_id")
);

CREATE TABLE IF NOT EXISTS "HOST_LANGUAGE" (
    "host_id" INT NOT NULL,
    "language_id" INT NOT NULL,

    CONSTRAINT "fk_hl_host"
        FOREIGN KEY ("host_id")
        REFERENCES "HOST"("id")
        ON DELETE CASCADE,

    CONSTRAINT "fk_hl_language"
        FOREIGN KEY ("language_id")
        REFERENCES "LANGUAGE"("id")
        ON DELETE CASCADE,

    PRIMARY KEY ("host_id", "language_id")
);

CREATE TABLE IF NOT EXISTS "STAY_VIEW" (
    "stay_id" INT NOT NULL,
    "view_id" INT NOT NULL,

    CONSTRAINT "fk_sv_stay"
        FOREIGN KEY ("stay_id")
        REFERENCES "STAY"("id")
        ON DELETE CASCADE,

    CONSTRAINT "fk_sv_view"
        FOREIGN KEY ("view_id")
        REFERENCES "VIEW"("id")
        ON DELETE CASCADE,

    PRIMARY KEY ("stay_id", "view_id")
);

CREATE TABLE IF NOT EXISTS "STAY_AMENITY" (
    "stay_id" INT NOT NULL,
    "amenity_id" INT NOT NULL,

    CONSTRAINT "fk_sa_stay"
        FOREIGN KEY ("stay_id")
        REFERENCES "STAY"("id")
        ON DELETE CASCADE,

    CONSTRAINT "fk_sa_amenity"
        FOREIGN KEY ("amenity_id")
        REFERENCES "AMENITY"("id")
        ON DELETE CASCADE,

    PRIMARY KEY ("stay_id", "amenity_id")
);

CREATE TABLE IF NOT EXISTS "STAY_ACCESSIBILITY" (
    "stay_id" INT NOT NULL,
    "accessibility_id" INT NOT NULL,

    CONSTRAINT "fk_sc_stay"
        FOREIGN KEY ("stay_id")
        REFERENCES "STAY"("id")
        ON DELETE CASCADE,

    CONSTRAINT "fk_sc_accessibility"
        FOREIGN KEY ("accessibility_id")
        REFERENCES "ACCESSIBILITY"("id")
        ON DELETE CASCADE,

    PRIMARY KEY ("stay_id", "accessibility_id")
);

CREATE TABLE IF NOT EXISTS "STAY_MEAL_PLAN" (
    "stay_id" INT NOT NULL,
    "meal_plan_id" INT NOT NULL,

    CONSTRAINT "fk_sm_stay"
        FOREIGN KEY ("stay_id")
        REFERENCES "STAY"("id")
        ON DELETE CASCADE,

    CONSTRAINT "fk_sm_meal_plan"
        FOREIGN KEY ("meal_plan_id")
        REFERENCES "MEAL_PLAN"("id")
        ON DELETE CASCADE,

    PRIMARY KEY ("stay_id", "meal_plan_id")
);

CREATE TABLE IF NOT EXISTS "STAY_PAYMENT_TYPE" (
    "stay_id" INT NOT NULL,
    "payment_type_id" INT NOT NULL,

    CONSTRAINT "fk_sp_stay"
        FOREIGN KEY ("stay_id")
        REFERENCES "STAY"("id")
        ON DELETE CASCADE,

    CONSTRAINT "fk_sp_payment"
        FOREIGN KEY ("payment_type_id")
        REFERENCES "PAYMENT_TYPE"("id")
        ON DELETE CASCADE,

    PRIMARY KEY ("stay_id", "payment_type_id")
);

CREATE TABLE IF NOT EXISTS "STAY_TRAVELER_EXPERIENCE" (
    "stay_id" INT NOT NULL,
    "traveler_experience_id" INT NOT NULL,

    CONSTRAINT "fk_se_stay"
        FOREIGN KEY ("stay_id")
        REFERENCES "STAY"("id")
        ON DELETE CASCADE,

    CONSTRAINT "fk_se_experience"
        FOREIGN KEY ("traveler_experience_id")
        REFERENCES "TRAVELER_EXPERIENCE"("id")
        ON DELETE CASCADE,

    PRIMARY KEY ("stay_id", "traveler_experience_id")
);

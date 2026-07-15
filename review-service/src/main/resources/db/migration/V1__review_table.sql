-- Mirrors the review table shape from the monolith's V3__core_tables.sql, V7__review_rating.sql,
-- and V17__review_unique_per_user_stay.sql, minus the FKs to "user"/"stay" — dropped per
-- docs/adr/0011 (database-per-service): existence is now an application-layer concern,
-- not a DB constraint this service can enforce against tables it no longer owns.
CREATE TABLE IF NOT EXISTS review (
    id SERIAL PRIMARY KEY,
    text TEXT NOT NULL,
    user_id INT NOT NULL,
    stay_id INT NOT NULL,
    rating SMALLINT NOT NULL DEFAULT 3,

    CONSTRAINT check_review_rating CHECK (rating >= 1 AND rating <= 5),
    CONSTRAINT uq_review_user_stay UNIQUE (user_id, stay_id)
);

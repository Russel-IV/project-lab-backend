-- Favorite (docs/adr/0011: database-per-service) — no FK to "user"/"stay", mirrors
-- V1__review_table.sql: existence is an application-layer concern the owning
-- services enforce, not something this service's DB can constrain against
-- tables it doesn't own.
CREATE TABLE IF NOT EXISTS favorite (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    stay_id INT NOT NULL,

    CONSTRAINT uq_favorite_user_stay UNIQUE (user_id, stay_id)
);

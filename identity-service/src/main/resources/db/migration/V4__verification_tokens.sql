-- Generic verification-token table (mirrors media-service's owner-polymorphic Media
-- table: one table, a `type` discriminator, instead of separate
-- password_reset_token / email_confirmation_token tables) backing both the
-- password-reset and account-confirmation flows.
CREATE TABLE verification_token (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    token VARCHAR(128) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('PASSWORD_RESET', 'EMAIL_CONFIRMATION')),
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_verification_token_token ON verification_token(token);
CREATE INDEX idx_verification_token_user_type ON verification_token(user_id, type);

-- A timestamp, not a boolean: records *when* confirmed. Nullable = unconfirmed.
-- Account confirmation is non-blocking — login is never gated on this being set.
ALTER TABLE "user" ADD COLUMN email_confirmed_at TIMESTAMP;

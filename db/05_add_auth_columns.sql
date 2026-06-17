-- Add authentication columns to user table
-- These are nullable to preserve compatibility with existing rows.
-- The signup mutation always populates both fields; rows without credentials cannot log in.
ALTER TABLE "user"
    ADD COLUMN IF NOT EXISTS email VARCHAR(320),
    ADD COLUMN IF NOT EXISTS password_hash TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS uq_user_email ON "user" (email) WHERE email IS NOT NULL;

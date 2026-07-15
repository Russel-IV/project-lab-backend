ALTER TABLE "user"
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

DROP INDEX IF EXISTS uq_user_email;
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_email ON "user" (email) WHERE email IS NOT NULL AND deleted_at IS NULL;

ALTER TABLE "user" ADD COLUMN public_id UUID;

-- v4 here, not the app's Uuid7: these are pre-existing rows, so insertion-order
-- locality (the only reason to prefer v7) doesn't apply retroactively. New rows
-- get an app-generated Uuid7 value going forward.
UPDATE "user" SET public_id = gen_random_uuid() WHERE public_id IS NULL;

ALTER TABLE "user" ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE "user" ADD CONSTRAINT uq_user_public_id UNIQUE (public_id);

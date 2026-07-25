ALTER TABLE stay ADD COLUMN public_id UUID;

UPDATE stay SET public_id = gen_random_uuid() WHERE public_id IS NULL;

ALTER TABLE stay ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE stay ADD CONSTRAINT uq_stay_public_id UNIQUE (public_id);

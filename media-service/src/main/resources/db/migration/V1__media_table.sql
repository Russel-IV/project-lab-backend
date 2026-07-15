-- Generalizes stay_picture/room_picture (gateway V3/V11) into a single owner-polymorphic
-- table per docs/adr/0003. No FK to stay/room/user (docs/adr/0011) — existence is trusted
-- from the Gateway, which still owns Stay/Room/User until their own extraction phases.
CREATE TABLE media (
    id SERIAL PRIMARY KEY,
    owner_type VARCHAR(10) NOT NULL CHECK (owner_type IN ('STAY', 'ROOM', 'USER')),
    owner_id INT NOT NULL,
    url TEXT NOT NULL,
    caption TEXT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,

    CONSTRAINT check_media_display_order CHECK (display_order >= 0)
);

CREATE INDEX idx_media_owner ON media(owner_type, owner_id);
CREATE UNIQUE INDEX idx_media_primary ON media(owner_type, owner_id) WHERE is_primary = TRUE;

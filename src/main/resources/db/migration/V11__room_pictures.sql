CREATE TABLE IF NOT EXISTS room_picture (
    id SERIAL PRIMARY KEY,
    room_id INT NOT NULL,
    url TEXT NOT NULL,
    caption TEXT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_room_picture_room
        FOREIGN KEY (room_id)
        REFERENCES room(id)
        ON DELETE CASCADE,

    CONSTRAINT check_room_picture_display_order
        CHECK (display_order >= 0)
);

CREATE INDEX idx_room_picture_room_id ON room_picture(room_id);
CREATE UNIQUE INDEX idx_room_picture_primary ON room_picture(room_id) WHERE is_primary = TRUE;

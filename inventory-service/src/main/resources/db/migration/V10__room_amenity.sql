CREATE TABLE room_amenity (
    room_id INT NOT NULL REFERENCES room(id) ON DELETE CASCADE,
    amenity_id INT NOT NULL REFERENCES amenity(id) ON DELETE CASCADE,
    PRIMARY KEY (room_id, amenity_id)
);
CREATE INDEX idx_fk_ra_amenity ON room_amenity(amenity_id);

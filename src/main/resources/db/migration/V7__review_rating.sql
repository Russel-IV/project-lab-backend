ALTER TABLE review ADD COLUMN rating SMALLINT NOT NULL DEFAULT 3;
ALTER TABLE review ADD CONSTRAINT check_review_rating CHECK (rating >= 1 AND rating <= 5);

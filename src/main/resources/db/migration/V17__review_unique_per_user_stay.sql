ALTER TABLE review ADD CONSTRAINT uq_review_user_stay UNIQUE (user_id, stay_id);

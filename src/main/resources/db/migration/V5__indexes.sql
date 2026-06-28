CREATE INDEX idx_stay_host_id ON stay(host_id);
CREATE INDEX idx_stay_address_id ON stay(address_id);
CREATE INDEX idx_stay_brand_id ON stay(property_brand_id);
CREATE INDEX idx_review_stay_id ON review(stay_id);
CREATE INDEX idx_review_user_id ON review(user_id);

CREATE INDEX idx_room_stay_id ON room(stay_id);

CREATE INDEX idx_booking_user_id ON booking(user_id);
CREATE INDEX idx_booking_dates ON booking(check_in_date, check_out_date);

CREATE INDEX idx_br_room_id ON booking_room(room_id);

CREATE INDEX idx_stay_picture_stay_id ON stay_picture(stay_id);
CREATE UNIQUE INDEX idx_stay_picture_primary ON stay_picture(stay_id) WHERE is_primary = TRUE;

CREATE INDEX idx_fk_favorite_stay ON user_favorite(stay_id);
CREATE INDEX idx_fk_hl_language ON host_language(language_id);
CREATE INDEX idx_fk_sv_view ON stay_view(view_id);
CREATE INDEX idx_fk_sa_amenity ON stay_amenity(amenity_id);
CREATE INDEX idx_fk_sc_accessibility ON stay_accessibility(accessibility_id);
CREATE INDEX idx_fk_sm_meal_plan ON stay_meal_plan(meal_plan_id);
CREATE INDEX idx_fk_sp_payment ON stay_payment_type(payment_type_id);
CREATE INDEX idx_fk_se_experience ON stay_traveler_experience(traveler_experience_id);

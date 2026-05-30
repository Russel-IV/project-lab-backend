-- ==========================================
-- 4. PERFORMANCE & JOIN INDEXES
-- ==========================================

CREATE INDEX "idx_stay_host_id" ON stay("host_id");
CREATE INDEX "idx_stay_brand_id" ON stay("property_brand_id");
CREATE INDEX "idx_review_stay_id" ON review("stay_id");
CREATE INDEX "idx_review_user_id" ON review("user_id");

-- Junction trailing-edge indexes for fast backward lookups
CREATE INDEX "idx_fk_favorite_stay" ON user_favorite("stay_id");
CREATE INDEX "idx_fk_hl_language" ON host_language("language_id");
CREATE INDEX "idx_fk_sv_view" ON stay_view("view_id");
CREATE INDEX "idx_fk_sa_amenity" ON stay_amenity("amenity_id");
CREATE INDEX "idx_fk_sc_accessibility" ON stay_accessibility("accessibility_id");
CREATE INDEX "idx_fk_sm_meal_plan" ON stay_meal_plan("meal_plan_id");
CREATE INDEX "idx_fk_sp_payment" ON stay_payment_type("payment_type_id");
CREATE INDEX "idx_fk_se_experience" ON stay_traveler_experience("traveler_experience_id");

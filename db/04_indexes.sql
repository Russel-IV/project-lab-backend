-- ==========================================
-- 4. PERFORMANCE & JOIN INDEXES
-- ==========================================

CREATE INDEX "idx_stay_host_id" ON "STAY"("host_id");
CREATE INDEX "idx_stay_brand_id" ON "STAY"("property_brand_id");
CREATE INDEX "idx_review_stay_id" ON "REVIEW"("stay_id");
CREATE INDEX "idx_review_user_id" ON "REVIEW"("user_id");

-- Junction trailing-edge indexes for fast backward lookups
CREATE INDEX "idx_fk_favorite_stay" ON "USER_FAVORITE"("stay_id");
CREATE INDEX "idx_fk_hl_language" ON "HOST_LANGUAGE"("language_id");
CREATE INDEX "idx_fk_sv_view" ON "STAY_VIEW"("view_id");
CREATE INDEX "idx_fk_sa_amenity" ON "STAY_AMENITY"("amenity_id");
CREATE INDEX "idx_fk_sc_accessibility" ON "STAY_ACCESSIBILITY"("accessibility_id");
CREATE INDEX "idx_fk_sm_meal_plan" ON "STAY_MEAL_PLAN"("meal_plan_id");
CREATE INDEX "idx_fk_sp_payment" ON "STAY_PAYMENT_TYPE"("payment_type_id");
CREATE INDEX "idx_fk_se_experience" ON "STAY_TRAVELER_EXPERIENCE"("traveler_experience_id");

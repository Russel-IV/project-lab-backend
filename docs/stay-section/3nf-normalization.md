# No normalization
**STAY_SYSTEM** (stay_price, stay_location, stay_property_amenities, stay_room_amenities, stay_room_views, stay_payment_type, stay_guest_rating, stay_star_rating, stay_property_type, stay_property_brand, stay_traveler_experience, stay_bookings, stay_accessibility, stay_meal_plans_available, stay_beach_access, stay_name, stay_host_name, stay_host_languages, stay_host_communication_rating, stay_host_checkin_process_rating, stay_host_cancellation_rate, stay_is_refundable, stay_is_favorite, stay_reviews, stay_policies_text, stay_cancellation_deadline, stay_about, stay_important_information, stay_pictures, room_name, room_price, room_sleeps, room_bedroom_amount, room_bathrooms, room_size)

# First Normal Form
**STAY_SYSTEM** (*stay_id*, stay_street_address, stay_extended_address, stay_city, stay_state_province, stay_postal_code, stay_country_code, stay_guest_rating, stay_star_rating, stay_property_type, stay_beach_access, stay_name, stay_is_refundable, stay_policies_text, stay_cancellation_deadline, stay_about, stay_important_information, *host_id*, host_name, host_communication_rating, host_checkin_process_rating, host_cancellation_rate, *review_id*, review_text, *review_user_id*, user_name)
**ROOM** (*stay_id*, *room_id*, room_name, room_price, room_sleeps, room_bedroom_amount, room_bathrooms, room_size)
**BOOKING** (*booking_id*, *user_id*, check_in_date, check_out_date, status, guests_count, created_at)
**BOOKING_ROOM** (*booking_id*, *room_id*)
**STAY_PROPERTY_BRAND** (*stay_id*, brand_name)
**STAY_TRAVELER_EXPERIENCE** (*stay_id*, traveler_experience_type)
**STAY_PROPERTY_AMENITIES** (*stay_id*, amenity_name)
**STAY_ROOM_AMENITIES** (*stay_id*, amenity_name)
**STAY_ROOM_VIEWS** (*stay_id*, view_type)
**STAY_ACCESSIBILITY** (*stay_id*, accessibility_type)
**STAY_MEAL_PLAN** (*stay_id*, meal_plan_type)
**STAY_PAYMENT_TYPE** (*stay_id*, payment_type)
**STAY_PICTURE** (*stay_id*, picture_url, caption, is_primary, display_order)
**HOST_LANGUAGES** (*host_id*, language)

# Second Normal Form
**STAY** (*id*, street_address, extended_address, city, state_province, postal_code, country_code, guest_rating, star_rating, property_type, beach_access, name, is_refundable, policies_text, cancellation_deadline, about, important_information, *host_id*)
**ROOM** (*id*, *stay_id*, name, price, sleeps, bedroom_amount, bathrooms, size)
**HOST** (*id*, name, communication_rating, checkin_process_rating, cancellation_rate)
**REVIEW** (*id*, text, *user_id*, user_name, *stay_id*)
**BOOKING** (*id*, *user_id*, check_in_date, check_out_date, status, guests_count, created_at)
**BOOKING_ROOM** (*booking_id*, *room_id*)
**STAY_PROPERTY_BRAND** (*stay_id*, brand_name)
**STAY_TRAVELER_EXPERIENCE** (*stay_id*, traveler_experience_type)
**STAY_PROPERTY_AMENITIES** (*stay_id*, amenity_name)
**STAY_ROOM_AMENITIES** (*stay_id*, amenity_name)
**STAY_ROOM_VIEWS** (*stay_id*, view_type)
**STAY_ACCESSIBILITY** (*stay_id*, accessibility_type)
**STAY_MEAL_PLAN** (*stay_id*, meal_plan_type)
**STAY_PAYMENT_TYPE** (*stay_id*, payment_type)
**STAY_PICTURE** (*id*, *stay_id*, picture_url, caption, is_primary, display_order)
**HOST_LANGUAGES** (*host_id*, language)
**USER_FAVORITE** (*user_id*, *stay_id*, created_at)

# Third Normal Form
**STAY** (*id*, name, about, property_type, is_refundable, star_rating, cancellation_deadline, policies_text, important_information, *host_id*, *address_id*)
- _guest_rating can be derived_
- _beach_access can be derived_
- _property_type is an enum: HOME, HOTEL_
**ADDRESS** (*id*, street_address, extended_address, city, state_province, postal_code, country_code)
- _address fields extracted from STAY to isolate location responsibility and enable reuse_
**ROOM** (*id*, *stay_id*, name, price, sleeps, bedroom_amount, bathrooms, size)
- _every STAY has at least one ROOM; HOME stays have exactly one, HOTEL stays have one per bookable unit_
- _price, sleeps, and physical dimensions belong to the room, not the property_
**BOOKING** (*id*, *user_id*, check_in_date, check_out_date, status, guests_count, created_at)
- _status is an enum: PENDING, CONFIRMED, CANCELLED, COMPLETED_
- _the 6-month lookahead window and min/max duration are enforced at the service layer, not in the schema_
- _guests_count is validated against the total sleeps of all booked rooms at booking time_
**BOOKING_ROOM** (*booking_id*, *room_id*)
- _a booking includes one or more rooms; each room is independently available_
- _availability is derived: a room is free for [A, B] when it does not appear in BOOKING_ROOM joined with a PENDING or CONFIRMED BOOKING where check_in_date < B AND check_out_date > A_
- _for HOME stays this means at most one active booking at a time; for HOTEL stays each room is checked independently_
**STAY_PICTURE** (*id*, *stay_id*, url, caption, is_primary, display_order)
- _url is a server-assigned relative path (e.g. /uploads/stays/1/uuid.jpg); pictures are uploaded as image files, not external URLs_
- _at most one picture per stay may have is_primary = true; enforced at the service layer and via a partial unique index_
**AMENITY** (*id*, name, type)
- _type is an enum "room_amenity" or "property_amenity"_
**REVIEW** (*id*, text, *user_id*, *stay_id*)
**USER** (*id*, name)
**HOST** (*id*, name, communication_rating, checkin_process_rating, cancellation_rate)
**LANGUAGE** (*id*, language_name)
**VIEW** (*id*, view_type)
**ACCESSIBILITY** (*id*, accessibility_type)
**MEAL_PLAN** (*id*, meal_plan_type)
**PAYMENT_TYPE** (*id*, payment_type)
**PROPERTY_BRAND** (*id*, brand_name)
**TRAVELER_EXPERIENCE** (*id*, traveler_experience_type)
**USER_FAVORITE** (*user_id*, *stay_id*, created_at)
**STAY_VIEW** (*stay_id*, *view_id*)
**HOST_LANGUAGE** (*host_id*, *language_id*)
**STAY_PROPERTY_BRAND** (*stay_id*, *brand_id*)
**STAY_TRAVELER_EXPERIENCE** (*stay_id*, *traveler_experience_id*)
**STAY_PAYMENT_TYPE** (*stay_id*, *payment_type_id*)
**STAY_MEAL_PLAN** (*stay_id*, *meal_plan_id*)
**STAY_AMENITY** (*stay_id*, *amenity_id*)
**STAY_ACCESSIBILITY** (*stay_id*, *accessibility_id*)

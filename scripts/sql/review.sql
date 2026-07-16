-- ============================================================
-- REVIEW-SERVICE SEED DATA (review-database)
-- ============================================================
-- Table: review.
-- Safe to run multiple times: all inserts use explicit IDs with
-- ON CONFLICT DO NOTHING. Sequence is reset after the insert.
--
-- review.user_id references identity-service's seeded user ids, and
-- review.stay_id references inventory-service's seeded stay ids —
-- there is no database-level FK across services anymore, so
-- scripts/sql/identity.sql and scripts/sql/inventory.sql should run
-- before this fragment.
--
-- 71 reviews (2-10 per stay across all 15 stays, ratings 1-5). Every
-- review's (user_id, stay_id) pair has a matching COMPLETED booking in
-- booking.sql's section 2 (review-eligibility bookings) — this is now
-- an application-layer invariant spanning three separate databases
-- (identity/inventory/booking/review), not enforced by Postgres, so
-- don't renumber user/stay ids here without keeping booking.sql in
-- sync.
-- ============================================================

INSERT INTO review (id, text, user_id, stay_id, rating) VALUES
-- Stay 1 — Cozy Beachfront House (4 reviews)
(1, 'Amazing stay! The ocean view was stunning and the host was incredibly welcoming.', 2, 1, 5),
(7, 'The kids loved being steps from the sand, and the kitchen was fully stocked for a week of cooking.', 6, 1, 5),
(8, 'Great location and a beautiful deck, though the wifi dropped a few times during our stay.', 9, 1, 4),
(9, 'Alice was so responsive and the house was even nicer than the photos.', 16, 1, 5),
-- Stay 2 — Luxury Tokyo Sky Hotel (6 reviews)
(2, 'Incredible service and breathtaking views. The penthouse is worth every penny.', 7, 2, 5),
(3, 'Standard King was spotless. Tokyo from the 32nd floor at night is pure magic.', 5, 2, 4),
(10, 'Front desk upgraded us on arrival and the view of Shinjuku at night was unbeatable.', 11, 2, 5),
(11, 'Rooms are small for the price, but the location makes up for it.', 17, 2, 3),
(12, 'Best hotel breakfast we''ve had in Tokyo, and the staff speak excellent English.', 13, 2, 5),
(13, 'Quiet, immaculate, and a five-minute walk from the station.', 20, 2, 4),
-- Stay 3 — Charming Mountain Cabin (3 reviews)
(4, 'Cold during winter but the cabin fireplace kept us warm. Absolutely beautiful.', 2, 3, 4),
(14, 'Waking up to fog rolling over the pines was worth the winding drive up.', 7, 3, 5),
(15, 'Charming but the heating struggled on the coldest night.', 14, 3, 3),
-- Stay 4 — Parisian Boutique Hotel (3 reviews)
(6, 'Paris was magical. Hotel perfectly located and the staff are wonderfully attentive.', 6, 4, 5),
(16, 'Steps from the Louvre and the breakfast spread was excellent.', 9, 4, 4),
(17, 'Elegant rooms, and the late checkout saved our last day in Paris.', 19, 4, 5),
-- Stay 5 — Bali Jungle Retreat (5 reviews)
(5, 'Bali exceeded all expectations. The pool villa is a dream. Will definitely be back!', 5, 5, 5),
(18, 'The rice paddy views from the pool are something we''ll never forget.', 2, 5, 5),
(19, 'Airport transfer was seamless, villa is even better in person.', 11, 5, 4),
(20, 'Peaceful and private, though the road up gets muddy after rain.', 17, 5, 4),
(21, 'Staff heated the pool for us without even asking twice. Incredible hospitality.', 20, 5, 5),
-- Stay 6 — Santorini Cliffside Villa (4 reviews)
(22, 'Sunset from the terrace is exactly like the postcards. Unreal.', 9, 6, 5),
(23, 'Steep steps but the caldera view makes every one of them worth it.', 16, 6, 5),
(24, 'Beautiful villa, but book a rental car — the walk into town is longer than it looks.', 13, 6, 3),
(25, 'Priya left a bottle of local wine waiting for us. Lovely touch.', 6, 6, 4),
-- Stay 7 — Manhattan Skyline Hotel (8 reviews)
(26, 'Rooftop bar views of the Empire State Building at night, can''t beat it.', 5, 7, 5),
(27, 'Rooms are compact like most Midtown hotels but spotless and well soundproofed.', 14, 7, 4),
(28, 'Valet was fast even during rush hour, and the penthouse suite was stunning.', 19, 7, 5),
(29, 'Great location for Broadway, walkable to almost everything.', 7, 7, 4),
(30, 'Nice hotel but street noise carried through the window at night.', 20, 7, 3),
(31, 'Front desk handled a last-minute date change without any fuss.', 2, 7, 5),
(32, 'Skyline King room lived up to its name — incredible view of the city lights.', 17, 7, 4),
(33, 'Perfect base for a work trip, fast wifi and a great gym.', 11, 7, 5),
-- Stay 8 — Scottish Highlands Cottage (2 reviews)
(34, 'The Cuillin views from the kitchen window alone are worth the trip.', 13, 8, 5),
(35, 'Cozy and remote — bring groceries, the nearest shop is a proper drive.', 9, 8, 4),
-- Stay 9 — Marrakech Riad Retreat (6 reviews)
(36, 'The courtyard plunge pool was the perfect escape from the medina heat.', 6, 9, 5),
(37, 'Airport pickup made navigating the medina so much easier on arrival.', 16, 9, 5),
(38, 'Rooftop breakfast overlooking the rooftops of the medina was magical.', 14, 9, 4),
(39, 'Beautiful riad, but the walls are thin — you''ll hear the call to prayer clearly.', 20, 9, 3),
(40, 'Amara''s staff anticipated everything we needed before we asked.', 2, 9, 5),
(41, 'Authentic and gorgeously decorated, just book a guide for the first walk into the souks.', 19, 9, 4),
-- Stay 10 — Swiss Alpine Chalet (3 reviews)
(42, 'Ski-in/ski-out access to the Matterhorn glacier is genuinely as advertised.', 7, 10, 5),
(43, 'The private chef dinner was the highlight of our whole trip.', 11, 10, 5),
(44, 'Gorgeous chalet, just know the boot room fills up fast with a big group.', 17, 10, 4),
-- Stay 11 — Barcelona Boutique Hotel (7 reviews)
(45, 'Passeig de Gràcia location means Gaudí''s buildings are right outside the door.', 5, 11, 5),
(46, 'Rooftop pool was a great way to cool off after a day of sightseeing.', 13, 11, 4),
(47, 'Art Nouveau details everywhere, felt like staying inside a piece of history.', 9, 11, 5),
(48, 'Lovely hotel but the elevator queue got long during peak checkout hours.', 20, 11, 3),
(49, 'Breakfast spread was generous and the staff were endlessly patient with our Spanish.', 16, 11, 4),
(50, 'Rooftop terrace room had the best light in the evening. Would book again.', 6, 11, 5),
(51, 'Central without being noisy, exactly what we wanted for the trip.', 14, 11, 4),
-- Stay 12 — Cape Town Ocean Lodge (4 reviews)
(52, 'Woke up to Table Mountain every morning and the beach was a two-minute walk.', 19, 12, 5),
(53, 'Loved the beach gear they left for us, saved us renting our own.', 11, 12, 4),
(54, 'Gorgeous location, just check the load-shedding schedule before you arrive.', 17, 12, 4),
(55, 'One of the best ocean views we''ve had anywhere, and incredible value.', 2, 12, 5),
-- Stay 13 — Sydney Harbour Hotel (9 reviews)
(56, 'Woke up to the Opera House out our window every single morning.', 6, 13, 5),
(57, 'Five-minute walk to the ferry terminal made getting around the harbour effortless.', 14, 13, 5),
(58, 'Executive Suite was spacious and the harbour view rooms are worth requesting.', 20, 13, 4),
(59, 'Great location but pricier than similar hotels a few blocks back.', 7, 13, 3),
(60, 'Staff arranged a surprise anniversary setup in our room without being asked twice.', 9, 13, 5),
(61, 'Opera House King room lived up to the name, incredible views at sunset.', 13, 13, 4),
(62, 'Best harbour views in Sydney, hands down. Worth every dollar.', 16, 13, 5),
(63, 'Comfortable beds and a great breakfast buffet overlooking the water.', 19, 13, 4),
(64, 'Checked in early with no fuss and the room was already spotless.', 5, 13, 5),
-- Stay 14 — Reykjavik Northern Lights Cabin (2 reviews)
(65, 'We saw the aurora directly through the glass roof from bed. Unreal experience.', 11, 14, 5),
(66, 'Hot tub on the deck under the stars was the highlight of our Iceland trip.', 17, 14, 4),
-- Stay 15 — Amsterdam Canal House Hotel (5 reviews)
(67, 'Waking up to the canal view every morning made the whole trip feel like a postcard.', 13, 15, 5),
(68, 'Historic building with narrow stairs, but the bike rental made getting around effortless.', 9, 15, 4),
(69, 'Breakfast included and the canal house suite was beautifully restored.', 20, 15, 5),
(70, 'Charming hotel, just be ready for a lot of stairs if you''re not near the elevator.', 6, 15, 3),
(71, 'Best located hotel for exploring the canal ring on foot or by bike.', 16, 15, 5)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('review', 'id'), COALESCE(MAX(id), 1)) FROM review;

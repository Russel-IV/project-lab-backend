-- ============================================================
-- BOOKING-SERVICE SEED DATA (booking-database)
-- ============================================================
-- Tables: booking, booking_room.
-- Safe to run multiple times: all inserts use explicit IDs with
-- ON CONFLICT DO NOTHING. Sequences are reset after each table.
--
-- booking.user_id references identity-service's seeded user ids, and
-- booking_room.room_id references inventory-service's seeded room ids —
-- there is no database-level FK across services anymore, so
-- scripts/sql/identity.sql and scripts/sql/inventory.sql MUST run before
-- this fragment (booking_room.booking_id still has a real intra-DB FK
-- to booking, so this file's own booking INSERTs must run before its
-- own booking_room INSERTs, same as the original monolith script).
--
-- 30 "narrative" bookings (all 4 statuses represented, section 7) plus
-- 64 synthetic COMPLETED bookings (section 7b) backing review
-- eligibility and 15 more (section 7c, one per stay) for a guest who is
-- eligible but hasn't reviewed yet (see review.sql's header for how
-- these three databases' seed data stays cross-consistent).
--
-- Booking status coverage: CONFIRMED, PENDING, CANCELLED and COMPLETED
-- all appear on multiple stays, including multi-room bookings.
-- ============================================================


-- ============================================================
-- 1. BOOKINGS  (all four statuses covered across every stay)
-- ============================================================

-- total_price = sum(room prices) × nights
INSERT INTO booking (id, user_id, check_in_date, check_out_date, status, guests_count, created_at, total_price) VALUES
(1,  2,  '2027-01-15', '2027-01-20', 'CONFIRMED', 2, '2026-06-01 10:00:00',  602.50),  -- Bob   → Beachfront Suite
(2,  5,  '2027-02-10', '2027-02-14', 'CONFIRMED', 1, '2026-06-05 14:30:00', 1400.00),  -- David  → Standard King
(3,  2,  '2027-03-01', '2027-03-05', 'PENDING',   3, '2026-06-10 09:00:00', 2800.00),  -- Bob    → Classic Double + Superior Suite
(4,  6,  '2026-08-01', '2026-08-07', 'CANCELLED', 4, '2026-05-20 11:00:00',  510.00),  -- Emma   → Mountain Loft
(5,  5,  '2026-04-05', '2026-04-12', 'COMPLETED', 2, '2026-03-01 16:00:00', 1225.00),  -- David  → Jungle Pool Villa
(6,  7,  '2027-04-20', '2027-04-25', 'CONFIRMED', 1, '2026-06-15 08:00:00', 6000.00),  -- Frank  → Executive Penthouse
(7,  9,  '2027-05-10', '2027-05-15', 'CONFIRMED', 2, '2026-11-01 09:15:00', 1550.00),  -- Liam   → Cliffside Suite
(8,  16, '2026-05-01', '2026-05-04', 'COMPLETED', 2, '2026-03-15 12:00:00',  930.00),  -- Ethan  → Cliffside Suite
(9,  11, '2027-06-01', '2027-06-05', 'CONFIRMED', 2, '2026-12-01 10:00:00', 1120.00),  -- Noah   → City View Queen
(10, 13, '2027-07-10', '2027-07-13', 'PENDING',   2, '2026-12-20 14:00:00', 1260.00),  -- Mateo  → Skyline King
(11, 17, '2026-05-01', '2026-05-04', 'CANCELLED', 4, '2026-02-01 08:00:00', 4110.00),  -- Zara   → Skyline King + Penthouse Loft
(12, 14, '2027-08-05', '2027-08-10', 'CONFIRMED', 3, '2027-01-10 09:00:00',  750.00),  -- Yuki   → Highland Cottage
(13, 19, '2027-03-15', '2027-03-20', 'CONFIRMED', 2, '2026-10-01 11:00:00',  700.00),  -- Chloe  → Riad Suite
(14, 20, '2026-02-01', '2026-02-05', 'COMPLETED', 2, '2025-12-05 09:30:00',  840.00),  -- Omar   → Rooftop Suite
(15, 9,  '2027-09-01', '2027-09-04', 'PENDING',   4, '2027-02-01 10:00:00', 1050.00),  -- Liam   → Riad Suite + Rooftop Suite
(16, 2,  '2027-01-05', '2027-01-12', 'CONFIRMED', 4, '2026-08-01 09:00:00', 3360.00),  -- Bob    → Alpine Chalet
(17, 6,  '2026-03-01', '2026-03-05', 'CANCELLED', 2, '2026-01-05 09:00:00', 1920.00),  -- Emma   → Alpine Chalet
(18, 5,  '2027-04-10', '2027-04-13', 'CONFIRMED', 2, '2026-11-15 09:00:00',  570.00),  -- David  → Gothic Quarter Double
(19, 7,  '2027-02-20', '2027-02-24', 'PENDING',   2, '2026-12-10 09:00:00', 1040.00),  -- Frank  → Passeig de Gràcia Suite
(20, 13, '2026-06-01', '2026-06-05', 'COMPLETED', 2, '2026-04-01 09:00:00', 1200.00),  -- Mateo  → Rooftop Terrace Room
(21, 16, '2027-11-01', '2027-11-08', 'CONFIRMED', 2, '2027-05-01 09:00:00', 1155.00),  -- Ethan  → Ocean Lodge Room
(22, 17, '2026-04-01', '2026-04-04', 'COMPLETED', 2, '2026-02-01 09:00:00',  495.00),  -- Zara   → Ocean Lodge Room
(23, 9,  '2027-12-01', '2027-12-05', 'CONFIRMED', 2, '2027-06-01 09:00:00',  880.00),  -- Liam   → Harbour View Twin
(24, 11, '2027-01-20', '2027-01-24', 'PENDING',   3, '2026-09-01 09:00:00', 1360.00),  -- Noah   → Opera House King
(25, 19, '2026-07-01', '2026-07-07', 'COMPLETED', 2, '2026-04-15 09:00:00', 3600.00),  -- Chloe  → Executive Suite
(26, 14, '2027-02-01', '2027-02-05', 'CONFIRMED', 2, '2026-10-01 09:00:00',  840.00),  -- Yuki   → Aurora Cabin
(27, 20, '2026-01-10', '2026-01-13', 'CANCELLED', 2, '2025-11-01 09:00:00',  630.00),  -- Omar   → Aurora Cabin
(28, 2,  '2027-05-01', '2027-05-04', 'CONFIRMED', 2, '2026-12-01 09:00:00',  720.00),  -- Bob    → Canal View Double
(29, 6,  '2026-03-15', '2026-03-19', 'COMPLETED', 2, '2026-01-20 09:00:00', 1520.00),  -- Emma   → Canal House Suite
(30, 13, '2027-10-10', '2027-10-13', 'PENDING',   2, '2027-04-01 09:00:00',  720.00)   -- Mateo  → Canal View Double
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('booking', 'id'), COALESCE(MAX(id), 1)) FROM booking;

INSERT INTO booking_room (booking_id, room_id) VALUES
(1, 1),
(2, 2),
(3, 6), (3, 7),
(4, 5),
(5, 8),
(6, 4),
(7, 9),
(8, 9),
(9, 10),
(10, 11),
(11, 11), (11, 12),
(12, 13),
(13, 14),
(14, 15),
(15, 14), (15, 15),
(16, 16),
(17, 16),
(18, 17),
(19, 18),
(20, 19),
(21, 20),
(22, 20),
(23, 21),
(24, 22),
(25, 23),
(26, 24),
(27, 24),
(28, 25),
(29, 26),
(30, 25)
ON CONFLICT DO NOTHING;


-- ------------------------------------------------------------
-- 2. REVIEW-ELIGIBILITY BOOKINGS
-- ------------------------------------------------------------
-- createReview() requires the reviewer to have a COMPLETED booking for
-- the stay being reviewed, and rejects a second review for the same
-- stay (unique constraint on review(user_id, stay_id), review-service's
-- V7). The synthetic bookings below backfill a COMPLETED booking for
-- every (user, stay) pair reviewed in review.sql, so this seed data
-- stays internally consistent with that rule and is usable for
-- exercising createReview / myBookingStatusForStay / myReviewForStay
-- end-to-end across the three now-separate databases.

INSERT INTO booking (id, user_id, check_in_date, check_out_date, status, guests_count, created_at, total_price) VALUES
(31, 2, '2025-01-05', '2025-01-08', 'COMPLETED', 1, '2024-11-06 09:00:00', 361.50),  -- user 2 -> stay 1 (review eligibility)
(32, 6, '2025-01-10', '2025-01-13', 'COMPLETED', 1, '2024-11-11 09:00:00', 361.50),  -- user 6 -> stay 1 (review eligibility)
(33, 9, '2025-01-15', '2025-01-18', 'COMPLETED', 1, '2024-11-16 09:00:00', 361.50),  -- user 9 -> stay 1 (review eligibility)
(34, 16, '2025-01-20', '2025-01-23', 'COMPLETED', 1, '2024-11-21 09:00:00', 361.50),  -- user 16 -> stay 1 (review eligibility)
(35, 7, '2025-01-25', '2025-01-28', 'COMPLETED', 1, '2024-11-26 09:00:00', 1050.00),  -- user 7 -> stay 2 (review eligibility)
(36, 5, '2025-01-30', '2025-02-02', 'COMPLETED', 1, '2024-12-01 09:00:00', 1050.00),  -- user 5 -> stay 2 (review eligibility)
(37, 11, '2025-02-04', '2025-02-07', 'COMPLETED', 1, '2024-12-06 09:00:00', 1050.00),  -- user 11 -> stay 2 (review eligibility)
(38, 17, '2025-02-09', '2025-02-12', 'COMPLETED', 1, '2024-12-11 09:00:00', 1050.00),  -- user 17 -> stay 2 (review eligibility)
(39, 13, '2025-02-14', '2025-02-17', 'COMPLETED', 1, '2024-12-16 09:00:00', 1050.00),  -- user 13 -> stay 2 (review eligibility)
(40, 20, '2025-02-19', '2025-02-22', 'COMPLETED', 1, '2024-12-21 09:00:00', 1050.00),  -- user 20 -> stay 2 (review eligibility)
(41, 2, '2025-02-24', '2025-02-27', 'COMPLETED', 1, '2024-12-26 09:00:00', 255.00),  -- user 2 -> stay 3 (review eligibility)
(42, 7, '2025-03-01', '2025-03-04', 'COMPLETED', 1, '2024-12-31 09:00:00', 255.00),  -- user 7 -> stay 3 (review eligibility)
(43, 14, '2025-03-06', '2025-03-09', 'COMPLETED', 1, '2025-01-05 09:00:00', 255.00),  -- user 14 -> stay 3 (review eligibility)
(44, 6, '2025-03-11', '2025-03-14', 'COMPLETED', 1, '2025-01-10 09:00:00', 660.00),  -- user 6 -> stay 4 (review eligibility)
(45, 9, '2025-03-16', '2025-03-19', 'COMPLETED', 1, '2025-01-15 09:00:00', 660.00),  -- user 9 -> stay 4 (review eligibility)
(46, 19, '2025-03-21', '2025-03-24', 'COMPLETED', 1, '2025-01-20 09:00:00', 660.00),  -- user 19 -> stay 4 (review eligibility)
(47, 2, '2025-03-26', '2025-03-29', 'COMPLETED', 1, '2025-01-25 09:00:00', 525.00),  -- user 2 -> stay 5 (review eligibility)
(48, 11, '2025-03-31', '2025-04-03', 'COMPLETED', 1, '2025-01-30 09:00:00', 525.00),  -- user 11 -> stay 5 (review eligibility)
(49, 17, '2025-04-05', '2025-04-08', 'COMPLETED', 1, '2025-02-04 09:00:00', 525.00),  -- user 17 -> stay 5 (review eligibility)
(50, 20, '2025-04-10', '2025-04-13', 'COMPLETED', 1, '2025-02-09 09:00:00', 525.00),  -- user 20 -> stay 5 (review eligibility)
(51, 9, '2025-04-15', '2025-04-18', 'COMPLETED', 1, '2025-02-14 09:00:00', 930.00),  -- user 9 -> stay 6 (review eligibility)
(52, 13, '2025-04-20', '2025-04-23', 'COMPLETED', 1, '2025-02-19 09:00:00', 930.00),  -- user 13 -> stay 6 (review eligibility)
(53, 6, '2025-04-25', '2025-04-28', 'COMPLETED', 1, '2025-02-24 09:00:00', 930.00),  -- user 6 -> stay 6 (review eligibility)
(54, 5, '2025-04-30', '2025-05-03', 'COMPLETED', 1, '2025-03-01 09:00:00', 840.00),  -- user 5 -> stay 7 (review eligibility)
(55, 14, '2025-05-05', '2025-05-08', 'COMPLETED', 1, '2025-03-06 09:00:00', 840.00),  -- user 14 -> stay 7 (review eligibility)
(56, 19, '2025-05-10', '2025-05-13', 'COMPLETED', 1, '2025-03-11 09:00:00', 840.00),  -- user 19 -> stay 7 (review eligibility)
(57, 7, '2025-05-15', '2025-05-18', 'COMPLETED', 1, '2025-03-16 09:00:00', 840.00),  -- user 7 -> stay 7 (review eligibility)
(58, 20, '2025-05-20', '2025-05-23', 'COMPLETED', 1, '2025-03-21 09:00:00', 840.00),  -- user 20 -> stay 7 (review eligibility)
(59, 2, '2025-05-25', '2025-05-28', 'COMPLETED', 1, '2025-03-26 09:00:00', 840.00),  -- user 2 -> stay 7 (review eligibility)
(60, 17, '2025-05-30', '2025-06-02', 'COMPLETED', 1, '2025-03-31 09:00:00', 840.00),  -- user 17 -> stay 7 (review eligibility)
(61, 11, '2025-06-04', '2025-06-07', 'COMPLETED', 1, '2025-04-05 09:00:00', 840.00),  -- user 11 -> stay 7 (review eligibility)
(62, 13, '2025-06-09', '2025-06-12', 'COMPLETED', 1, '2025-04-10 09:00:00', 450.00),  -- user 13 -> stay 8 (review eligibility)
(63, 9, '2025-06-14', '2025-06-17', 'COMPLETED', 1, '2025-04-15 09:00:00', 450.00),  -- user 9 -> stay 8 (review eligibility)
(64, 6, '2025-06-19', '2025-06-22', 'COMPLETED', 1, '2025-04-20 09:00:00', 420.00),  -- user 6 -> stay 9 (review eligibility)
(65, 16, '2025-06-24', '2025-06-27', 'COMPLETED', 1, '2025-04-25 09:00:00', 420.00),  -- user 16 -> stay 9 (review eligibility)
(66, 14, '2025-06-29', '2025-07-02', 'COMPLETED', 1, '2025-04-30 09:00:00', 420.00),  -- user 14 -> stay 9 (review eligibility)
(67, 2, '2025-07-04', '2025-07-07', 'COMPLETED', 1, '2025-05-05 09:00:00', 420.00),  -- user 2 -> stay 9 (review eligibility)
(68, 19, '2025-07-09', '2025-07-12', 'COMPLETED', 1, '2025-05-10 09:00:00', 420.00),  -- user 19 -> stay 9 (review eligibility)
(69, 7, '2025-07-14', '2025-07-17', 'COMPLETED', 1, '2025-05-15 09:00:00', 1440.00),  -- user 7 -> stay 10 (review eligibility)
(70, 11, '2025-07-19', '2025-07-22', 'COMPLETED', 1, '2025-05-20 09:00:00', 1440.00),  -- user 11 -> stay 10 (review eligibility)
(71, 17, '2025-07-24', '2025-07-27', 'COMPLETED', 1, '2025-05-25 09:00:00', 1440.00),  -- user 17 -> stay 10 (review eligibility)
(72, 5, '2025-07-29', '2025-08-01', 'COMPLETED', 1, '2025-05-30 09:00:00', 570.00),  -- user 5 -> stay 11 (review eligibility)
(73, 9, '2025-08-03', '2025-08-06', 'COMPLETED', 1, '2025-06-04 09:00:00', 570.00),  -- user 9 -> stay 11 (review eligibility)
(74, 20, '2025-08-08', '2025-08-11', 'COMPLETED', 1, '2025-06-09 09:00:00', 570.00),  -- user 20 -> stay 11 (review eligibility)
(75, 16, '2025-08-13', '2025-08-16', 'COMPLETED', 1, '2025-06-14 09:00:00', 570.00),  -- user 16 -> stay 11 (review eligibility)
(76, 6, '2025-08-18', '2025-08-21', 'COMPLETED', 1, '2025-06-19 09:00:00', 570.00),  -- user 6 -> stay 11 (review eligibility)
(77, 14, '2025-08-23', '2025-08-26', 'COMPLETED', 1, '2025-06-24 09:00:00', 570.00),  -- user 14 -> stay 11 (review eligibility)
(78, 19, '2025-08-28', '2025-08-31', 'COMPLETED', 1, '2025-06-29 09:00:00', 495.00),  -- user 19 -> stay 12 (review eligibility)
(79, 11, '2025-09-02', '2025-09-05', 'COMPLETED', 1, '2025-07-04 09:00:00', 495.00),  -- user 11 -> stay 12 (review eligibility)
(80, 2, '2025-09-07', '2025-09-10', 'COMPLETED', 1, '2025-07-09 09:00:00', 495.00),  -- user 2 -> stay 12 (review eligibility)
(81, 6, '2025-09-12', '2025-09-15', 'COMPLETED', 1, '2025-07-14 09:00:00', 660.00),  -- user 6 -> stay 13 (review eligibility)
(82, 14, '2025-09-17', '2025-09-20', 'COMPLETED', 1, '2025-07-19 09:00:00', 660.00),  -- user 14 -> stay 13 (review eligibility)
(83, 20, '2025-09-22', '2025-09-25', 'COMPLETED', 1, '2025-07-24 09:00:00', 660.00),  -- user 20 -> stay 13 (review eligibility)
(84, 7, '2025-09-27', '2025-09-30', 'COMPLETED', 1, '2025-07-29 09:00:00', 660.00),  -- user 7 -> stay 13 (review eligibility)
(85, 9, '2025-10-02', '2025-10-05', 'COMPLETED', 1, '2025-08-03 09:00:00', 660.00),  -- user 9 -> stay 13 (review eligibility)
(86, 13, '2025-10-07', '2025-10-10', 'COMPLETED', 1, '2025-08-08 09:00:00', 660.00),  -- user 13 -> stay 13 (review eligibility)
(87, 16, '2025-10-12', '2025-10-15', 'COMPLETED', 1, '2025-08-13 09:00:00', 660.00),  -- user 16 -> stay 13 (review eligibility)
(88, 5, '2025-10-17', '2025-10-20', 'COMPLETED', 1, '2025-08-18 09:00:00', 660.00),  -- user 5 -> stay 13 (review eligibility)
(89, 11, '2025-10-22', '2025-10-25', 'COMPLETED', 1, '2025-08-23 09:00:00', 630.00),  -- user 11 -> stay 14 (review eligibility)
(90, 17, '2025-10-27', '2025-10-30', 'COMPLETED', 1, '2025-08-28 09:00:00', 630.00),  -- user 17 -> stay 14 (review eligibility)
(91, 13, '2025-11-01', '2025-11-04', 'COMPLETED', 1, '2025-09-02 09:00:00', 720.00),  -- user 13 -> stay 15 (review eligibility)
(92, 9, '2025-11-06', '2025-11-09', 'COMPLETED', 1, '2025-09-07 09:00:00', 720.00),  -- user 9 -> stay 15 (review eligibility)
(93, 20, '2025-11-11', '2025-11-14', 'COMPLETED', 1, '2025-09-12 09:00:00', 720.00),  -- user 20 -> stay 15 (review eligibility)
(94, 16, '2025-11-16', '2025-11-19', 'COMPLETED', 1, '2025-09-17 09:00:00', 720.00)  -- user 16 -> stay 15 (review eligibility)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('booking', 'id'), COALESCE(MAX(id), 1)) FROM booking;

INSERT INTO booking_room (booking_id, room_id) VALUES
(31, 1),
(32, 1),
(33, 1),
(34, 1),
(35, 2),
(36, 2),
(37, 2),
(38, 2),
(39, 2),
(40, 2),
(41, 5),
(42, 5),
(43, 5),
(44, 6),
(45, 6),
(46, 6),
(47, 8),
(48, 8),
(49, 8),
(50, 8),
(51, 9),
(52, 9),
(53, 9),
(54, 10),
(55, 10),
(56, 10),
(57, 10),
(58, 10),
(59, 10),
(60, 10),
(61, 10),
(62, 13),
(63, 13),
(64, 14),
(65, 14),
(66, 14),
(67, 14),
(68, 14),
(69, 16),
(70, 16),
(71, 16),
(72, 17),
(73, 17),
(74, 17),
(75, 17),
(76, 17),
(77, 17),
(78, 20),
(79, 20),
(80, 20),
(81, 21),
(82, 21),
(83, 21),
(84, 21),
(85, 21),
(86, 21),
(87, 21),
(88, 21),
(89, 24),
(90, 24),
(91, 25),
(92, 25),
(93, 25),
(94, 25)
ON CONFLICT DO NOTHING;


-- ------------------------------------------------------------
-- 3. UNREVIEWED COMPLETED BOOKINGS
-- ------------------------------------------------------------
-- One extra COMPLETED booking per stay, for a guest who has *not* left
-- a review for it. Section 2 above makes every existing review
-- eligible, which leaves no seed user in the "eligible, hasn't reviewed
-- yet" state — the actual createReview happy path. These fill that gap
-- so it can be exercised end-to-end (log in as the noted user, call
-- myBookingStatusForStay for the stay, then createReview).

INSERT INTO booking (id, user_id, check_in_date, check_out_date, status, guests_count, created_at, total_price) VALUES
(95, 5, '2025-12-01', '2025-12-04', 'COMPLETED', 1, '2025-10-17 09:00:00', 361.50),  -- user 5 -> stay 1 (eligible, not yet reviewed)
(96, 6, '2025-12-07', '2025-12-10', 'COMPLETED', 1, '2025-10-23 09:00:00', 1050.00),  -- user 6 -> stay 2 (eligible, not yet reviewed)
(97, 9, '2025-12-13', '2025-12-16', 'COMPLETED', 1, '2025-10-29 09:00:00', 255.00),  -- user 9 -> stay 3 (eligible, not yet reviewed)
(98, 11, '2025-12-19', '2025-12-22', 'COMPLETED', 1, '2025-11-04 09:00:00', 660.00),  -- user 11 -> stay 4 (eligible, not yet reviewed)
(99, 13, '2025-12-25', '2025-12-28', 'COMPLETED', 1, '2025-11-10 09:00:00', 525.00),  -- user 13 -> stay 5 (eligible, not yet reviewed)
(100, 14, '2025-12-31', '2026-01-03', 'COMPLETED', 1, '2025-11-16 09:00:00', 930.00),  -- user 14 -> stay 6 (eligible, not yet reviewed)
(101, 16, '2026-01-06', '2026-01-09', 'COMPLETED', 1, '2025-11-22 09:00:00', 840.00),  -- user 16 -> stay 7 (eligible, not yet reviewed)
(102, 17, '2026-01-12', '2026-01-15', 'COMPLETED', 1, '2025-11-28 09:00:00', 450.00),  -- user 17 -> stay 8 (eligible, not yet reviewed)
(103, 5, '2026-01-18', '2026-01-21', 'COMPLETED', 1, '2025-12-04 09:00:00', 420.00),  -- user 5 -> stay 9 (eligible, not yet reviewed)
(104, 6, '2026-01-24', '2026-01-27', 'COMPLETED', 1, '2025-12-10 09:00:00', 1440.00),  -- user 6 -> stay 10 (eligible, not yet reviewed)
(105, 7, '2026-01-30', '2026-02-02', 'COMPLETED', 1, '2025-12-16 09:00:00', 570.00),  -- user 7 -> stay 11 (eligible, not yet reviewed)
(106, 9, '2026-02-05', '2026-02-08', 'COMPLETED', 1, '2025-12-22 09:00:00', 495.00),  -- user 9 -> stay 12 (eligible, not yet reviewed)
(107, 11, '2026-02-11', '2026-02-14', 'COMPLETED', 1, '2025-12-28 09:00:00', 660.00),  -- user 11 -> stay 13 (eligible, not yet reviewed)
(108, 13, '2026-02-17', '2026-02-20', 'COMPLETED', 1, '2026-01-03 09:00:00', 630.00),  -- user 13 -> stay 14 (eligible, not yet reviewed)
(109, 14, '2026-02-23', '2026-02-26', 'COMPLETED', 1, '2026-01-09 09:00:00', 720.00)  -- user 14 -> stay 15 (eligible, not yet reviewed)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('booking', 'id'), COALESCE(MAX(id), 1)) FROM booking;

INSERT INTO booking_room (booking_id, room_id) VALUES
(95, 1),
(96, 2),
(97, 5),
(98, 6),
(99, 8),
(100, 9),
(101, 10),
(102, 13),
(103, 14),
(104, 16),
(105, 17),
(106, 20),
(107, 21),
(108, 24),
(109, 25)
ON CONFLICT DO NOTHING;

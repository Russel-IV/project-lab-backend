-- ============================================================
-- IDENTITY-SERVICE SEED DATA (identity-database)
-- ============================================================
-- Tables: language, "user", host, host_language.
-- Safe to run multiple times: all inserts use explicit IDs with
-- ON CONFLICT DO NOTHING. Sequences are reset after each table.
--
-- Test credentials (BCrypt, cost 10):
--   plain-text password -> "password"
--   hash                -> $2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS
--
-- 20 users (8 hosts / 12 guests). IDs 1, 3, 4, 8, 10, 12, 15, 18 are hosts;
-- the rest are guests. inventory-service's seeded stays reference these
-- exact host ids as stay.host_id, and booking-service's/review-service's
-- seeded rows reference these exact user ids as user_id — there is no
-- database-level FK across services anymore, so this script MUST run
-- before inventory/booking/review's fragments, and these ids must not
-- be renumbered without updating the other fragments too.
-- ============================================================

INSERT INTO language (id, language_name) VALUES
(1, 'English'),
(2, 'Spanish'),
(3, 'Japanese'),
(4, 'German'),
(5, 'French'),
(6, 'Portuguese'),
(7, 'Mandarin'),
(8, 'Italian'),
(9, 'Korean'),
(10, 'Arabic')
ON CONFLICT DO NOTHING;
SELECT setval(pg_get_serial_sequence('language', 'id'), COALESCE(MAX(id), 1)) FROM language;

INSERT INTO "user" (id, name, email, password_hash) VALUES
(1,  'Alice Johnson',     'alice@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(2,  'Bob Smith',         'bob@test.com',     '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(3,  'Takashi Murakami',  'takashi@test.com', '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(4,  'Clara Oswald',      'clara@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(5,  'David Kim',         'david@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(6,  'Emma García',       'emma@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(7,  'Frank Lee',         'frank@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(8,  'Priya Patel',       'priya@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(9,  'Liam O''Connor',    'liam@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(10, 'Sofia Rossi',       'sofia@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(11, 'Noah Andersen',     'noah@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(12, 'Amara Okafor',      'amara@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(13, 'Mateo Fernández',   'mateo@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(14, 'Yuki Tanaka',       'yuki@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(15, 'Isabel Santos',     'isabel@test.com',  '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(16, 'Ethan Walker',      'ethan@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(17, 'Zara Ahmed',        'zara@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(18, 'Lucas Müller',      'lucas@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(19, 'Chloe Martin',      'chloe@test.com',   '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS'),
(20, 'Omar Hassan',       'omar@test.com',    '$2b$10$Qr9OTs9LDghn16/QbKviZ.3w2EVu1CsRfe/s1l642Q.oK9rWhycLS')
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('"user"', 'id'), COALESCE(MAX(id), 1)) FROM "user";

INSERT INTO host (id, communication_rating, checkin_process_rating, cancellation_rate) VALUES
(1,  98.5,  95.0, 2.1),   -- Alice:  top-rated
(3,  100.0, 98.0, 0.0),   -- Takashi: perfect score
(4,  90.0,  88.5, 5.4),   -- Clara:  good but not perfect
(8,  96.0,  94.0, 1.5),   -- Priya:  very responsive
(10, 92.5,  90.0, 3.2),   -- Sofia:  reliable mid-tier
(12, 88.0,  85.0, 6.8),   -- Amara:  solid but occasional cancellations
(15, 99.0,  97.5, 0.5),   -- Isabel: near-perfect
(18, 85.5,  82.0, 8.0)    -- Lucas:  newer host, still building reputation
ON CONFLICT (id) DO NOTHING;

INSERT INTO host_language (host_id, language_id) VALUES
(1, 1), (1, 2),            -- Alice:   English, Spanish
(3, 1), (3, 3),            -- Takashi: English, Japanese
(4, 1), (4, 4), (4, 5),   -- Clara:   English, German, French
(8, 1), (8, 5),            -- Priya:   English, French
(10, 1), (10, 8),          -- Sofia:   English, Italian
(12, 1), (12, 10),         -- Amara:   English, Arabic
(15, 1), (15, 2), (15, 6),-- Isabel:  English, Spanish, Portuguese
(18, 1), (18, 4)           -- Lucas:   English, German
ON CONFLICT DO NOTHING;

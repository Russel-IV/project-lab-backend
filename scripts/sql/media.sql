-- ============================================================
-- MEDIA-SERVICE SEED DATA (media-database)
-- ============================================================
-- Table: media (owner_type/owner_id polymorphic -- docs/adr/0003).
-- Uses real local image files from scripts/images/ (copied into the shared
-- `uploads` docker volume by scripts/seed-images.sh, which populate-db.sh
-- runs before loading this fragment) -- reused across many stays/rooms since
-- there are only 20 source images. `url` stores the PORTABLE KEY
-- (`stays/{stayId}/{filename}` / `rooms/{roomId}/{filename}`), matching exactly
-- what LocalStorageService.save() would have produced for a real upload --
-- media-service's MediaService.toResponse() calls storageService.toUrl(key) to
-- build the full URL at read time (docs/adr/0003, LocalStorageService kdoc).
-- Storing a full external URL here (the previous approach, ported unmodified
-- from the old monolith's stay_picture table) is wrong against this contract:
-- toUrl() would prepend the public base URL to an already-absolute URL,
-- producing a broken address -- confirmed live, not just theoretical.
--
-- media.owner_id (stay ids 1-15, room ids 1-26) references inventory-service's
-- seeded stays/rooms -- there is no database-level FK across services anymore,
-- so scripts/sql/inventory.sql should run before this fragment.
--
-- Stay pictures: same captions/primary/display_order structure as before (2-4
-- per stay, 40 total). Room pictures: one per room (26 total, ids 41-66),
-- captioned with the room's own name.
--
-- Section 2 below adds pictures for inventory.sql's second seed batch (stays
-- 16-35, rooms 27-57): 2 stay pictures each (ids 67-106, 40 total) and 1 room
-- picture each (ids 107-137, 31 total), reusing the same 20 files in
-- scripts/images/ on a repeating cycle -- same approach as section 1.
-- Safe to run multiple times: ON CONFLICT DO NOTHING, sequence reset.
-- ============================================================

INSERT INTO media (id, owner_type, owner_id, url, caption, is_primary, display_order) VALUES
(1, 'STAY', 1, 'stays/1/photo-1445019980597-93fa8acb246c_3.webp', 'Ocean-facing exterior', true, 0),
(2, 'STAY', 1, 'stays/1/photo-1455587734955-081b22074882_4.webp', 'Open-plan living area', false, 1),
(3, 'STAY', 1, 'stays/1/photo-1495365200479-c4ed1d35e1aa.webp', 'Master bedroom', false, 2),
(4, 'STAY', 2, 'stays/2/photo-1496417263034-38ec4f0b665a_3.webp', 'Hotel lobby', true, 0),
(5, 'STAY', 2, 'stays/2/photo-1520250497591-112f2f40a3f4_3.webp', 'Standard King room', false, 1),
(6, 'STAY', 2, 'stays/2/photo-1535827841776-24afc1e255ac_6.webp', 'Deluxe Suite living area', false, 2),
(7, 'STAY', 2, 'stays/2/photo-1542314831-068cd1dbfeeb_3.webp', 'Executive Penthouse view', false, 3),
(8, 'STAY', 3, 'stays/3/photo-1549294413-26f195200c16_4.webp', 'Cabin surrounded by pines', true, 0),
(9, 'STAY', 3, 'stays/3/photo-1551882547-ff40c63fe5fa_4.webp', 'Interior with fireplace', false, 1),
(10, 'STAY', 4, 'stays/4/photo-1566073771259-6a8506099945_4.webp', 'Building facade', true, 0),
(11, 'STAY', 4, 'stays/4/photo-1584132967334-10e028bd69f7_3.webp', 'Classic Double room', false, 1),
(12, 'STAY', 5, 'stays/5/photo-1611892440504-42a792e24d32_3.webp', 'Pool villa from the garden', true, 0),
(13, 'STAY', 5, 'stays/5/photo-1618773928121-c32242e63f39_4.webp', 'Private infinity pool', false, 1),
(14, 'STAY', 6, 'stays/6/photo-1629140727571-9b5c6f6267b4_7.webp', 'Cliffside villa at sunset', true, 0),
(15, 'STAY', 6, 'stays/6/photo-1631049307264-da0ec9d70304_4.webp', 'Infinity pool over the caldera', false, 1),
(16, 'STAY', 6, 'stays/6/premium_photo-1661929519129-7a76946c1d38_3.webp', 'Whitewashed bedroom suite', false, 2),
(17, 'STAY', 7, 'stays/7/premium_photo-1661964071015-d97428970584_2.webp', 'Midtown hotel lobby', true, 0),
(18, 'STAY', 7, 'stays/7/premium_photo-1661964402307-02267d1423f5_4.webp', 'Skyline King room', false, 1),
(19, 'STAY', 7, 'stays/7/premium_photo-1675745329954-9639d3b74bbf_6.webp', 'Rooftop bar at night', false, 2),
(20, 'STAY', 8, 'stays/8/premium_photo-1687960116497-0dc41e1808a2_3.webp', 'Stone cottage exterior', true, 0),
(21, 'STAY', 8, 'stays/8/photo-1445019980597-93fa8acb246c_3.webp', 'View of the Cuillin mountains', false, 1),
(22, 'STAY', 9, 'stays/9/photo-1455587734955-081b22074882_4.webp', 'Central courtyard with plunge pool', true, 0),
(23, 'STAY', 9, 'stays/9/photo-1495365200479-c4ed1d35e1aa.webp', 'Rooftop terrace at dusk', false, 1),
(24, 'STAY', 9, 'stays/9/photo-1496417263034-38ec4f0b665a_3.webp', 'Riad Suite interior', false, 2),
(25, 'STAY', 10, 'stays/10/photo-1520250497591-112f2f40a3f4_3.webp', 'Timber chalet with Matterhorn backdrop', true, 0),
(26, 'STAY', 10, 'stays/10/photo-1535827841776-24afc1e255ac_6.webp', 'Cozy alpine living room', false, 1),
(27, 'STAY', 10, 'stays/10/photo-1542314831-068cd1dbfeeb_3.webp', 'Heated ski boot room', false, 2),
(28, 'STAY', 11, 'stays/11/photo-1549294413-26f195200c16_4.webp', 'Modernist facade on Passeig de Gràcia', true, 0),
(29, 'STAY', 11, 'stays/11/photo-1551882547-ff40c63fe5fa_4.webp', 'Passeig de Gràcia Suite', false, 1),
(30, 'STAY', 11, 'stays/11/photo-1566073771259-6a8506099945_4.webp', 'Seasonal rooftop pool', false, 2),
(31, 'STAY', 12, 'stays/12/photo-1584132967334-10e028bd69f7_3.webp', 'Direct beach access', true, 0),
(32, 'STAY', 12, 'stays/12/photo-1611892440504-42a792e24d32_3.webp', 'Table Mountain view from the deck', false, 1),
(33, 'STAY', 13, 'stays/13/photo-1618773928121-c32242e63f39_4.webp', 'Harbour view from the lobby', true, 0),
(34, 'STAY', 13, 'stays/13/photo-1629140727571-9b5c6f6267b4_7.webp', 'Opera House King room', false, 1),
(35, 'STAY', 13, 'stays/13/photo-1631049307264-da0ec9d70304_4.webp', 'Executive Suite living area', false, 2),
(36, 'STAY', 14, 'stays/14/premium_photo-1661929519129-7a76946c1d38_3.webp', 'Glass-roofed cabin exterior', true, 0),
(37, 'STAY', 14, 'stays/14/premium_photo-1661964071015-d97428970584_2.webp', 'Northern lights above the cabin', false, 1),
(38, 'STAY', 15, 'stays/15/premium_photo-1661964402307-02267d1423f5_4.webp', 'Canal house facade at golden hour', true, 0),
(39, 'STAY', 15, 'stays/15/premium_photo-1675745329954-9639d3b74bbf_6.webp', 'Canal View Double room', false, 1),
(40, 'STAY', 15, 'stays/15/premium_photo-1687960116497-0dc41e1808a2_3.webp', 'Breakfast room overlooking the canal', false, 2),
(41, 'ROOM', 1, 'rooms/1/photo-1445019980597-93fa8acb246c_3.webp', 'Beachfront Suite', true, 0),
(42, 'ROOM', 2, 'rooms/2/photo-1455587734955-081b22074882_4.webp', 'Standard King', true, 0),
(43, 'ROOM', 3, 'rooms/3/photo-1495365200479-c4ed1d35e1aa.webp', 'Deluxe Suite', true, 0),
(44, 'ROOM', 4, 'rooms/4/photo-1496417263034-38ec4f0b665a_3.webp', 'Executive Penthouse', true, 0),
(45, 'ROOM', 5, 'rooms/5/photo-1520250497591-112f2f40a3f4_3.webp', 'Mountain Loft', true, 0),
(46, 'ROOM', 6, 'rooms/6/photo-1535827841776-24afc1e255ac_6.webp', 'Classic Double', true, 0),
(47, 'ROOM', 7, 'rooms/7/photo-1542314831-068cd1dbfeeb_3.webp', 'Superior Suite', true, 0),
(48, 'ROOM', 8, 'rooms/8/photo-1549294413-26f195200c16_4.webp', 'Jungle Pool Villa', true, 0),
(49, 'ROOM', 9, 'rooms/9/photo-1551882547-ff40c63fe5fa_4.webp', 'Cliffside Suite', true, 0),
(50, 'ROOM', 10, 'rooms/10/photo-1566073771259-6a8506099945_4.webp', 'City View Queen', true, 0),
(51, 'ROOM', 11, 'rooms/11/photo-1584132967334-10e028bd69f7_3.webp', 'Skyline King', true, 0),
(52, 'ROOM', 12, 'rooms/12/photo-1611892440504-42a792e24d32_3.webp', 'Penthouse Loft', true, 0),
(53, 'ROOM', 13, 'rooms/13/photo-1618773928121-c32242e63f39_4.webp', 'Highland Cottage', true, 0),
(54, 'ROOM', 14, 'rooms/14/photo-1629140727571-9b5c6f6267b4_7.webp', 'Riad Suite', true, 0),
(55, 'ROOM', 15, 'rooms/15/photo-1631049307264-da0ec9d70304_4.webp', 'Rooftop Suite', true, 0),
(56, 'ROOM', 16, 'rooms/16/premium_photo-1661929519129-7a76946c1d38_3.webp', 'Alpine Chalet', true, 0),
(57, 'ROOM', 17, 'rooms/17/premium_photo-1661964071015-d97428970584_2.webp', 'Gothic Quarter Double', true, 0),
(58, 'ROOM', 18, 'rooms/18/premium_photo-1661964402307-02267d1423f5_4.webp', 'Passeig de Gràcia Suite', true, 0),
(59, 'ROOM', 19, 'rooms/19/premium_photo-1675745329954-9639d3b74bbf_6.webp', 'Rooftop Terrace Room', true, 0),
(60, 'ROOM', 20, 'rooms/20/premium_photo-1687960116497-0dc41e1808a2_3.webp', 'Ocean Lodge Room', true, 0),
(61, 'ROOM', 21, 'rooms/21/photo-1445019980597-93fa8acb246c_3.webp', 'Harbour View Twin', true, 0),
(62, 'ROOM', 22, 'rooms/22/photo-1455587734955-081b22074882_4.webp', 'Opera House King', true, 0),
(63, 'ROOM', 23, 'rooms/23/photo-1495365200479-c4ed1d35e1aa.webp', 'Executive Suite', true, 0),
(64, 'ROOM', 24, 'rooms/24/photo-1496417263034-38ec4f0b665a_3.webp', 'Aurora Cabin', true, 0),
(65, 'ROOM', 25, 'rooms/25/photo-1520250497591-112f2f40a3f4_3.webp', 'Canal View Double', true, 0),
(66, 'ROOM', 26, 'rooms/26/photo-1535827841776-24afc1e255ac_6.webp', 'Canal House Suite', true, 0)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('media', 'id'), COALESCE(MAX(id), 1)) FROM media;


-- ============================================================
-- 2. ADDITIONAL PICTURES — SEED BATCH 2 (stays 16-35, rooms 27-57)
-- ============================================================

INSERT INTO media (id, owner_type, owner_id, url, caption, is_primary, display_order) VALUES
(67, 'STAY', 16, 'stays/16/photo-1445019980597-93fa8acb246c_3.webp', 'Tiled house exterior in Alfama', true, 0),
(68, 'STAY', 16, 'stays/16/photo-1455587734955-081b22074882_4.webp', 'Private terrace over the Tagus', false, 1),
(69, 'STAY', 17, 'stays/17/photo-1495365200479-c4ed1d35e1aa.webp', 'Machiya hotel entrance', true, 0),
(70, 'STAY', 17, 'stays/17/photo-1496417263034-38ec4f0b665a_3.webp', 'Tatami interior with garden view', false, 1),
(71, 'STAY', 18, 'stays/18/photo-1520250497591-112f2f40a3f4_3.webp', 'Palermo Soho loft building', true, 0),
(72, 'STAY', 18, 'stays/18/photo-1535827841776-24afc1e255ac_6.webp', 'Open-plan living space', false, 1),
(73, 'STAY', 19, 'stays/19/photo-1542314831-068cd1dbfeeb_3.webp', 'Ringstrasse hotel facade', true, 0),
(74, 'STAY', 19, 'stays/19/photo-1549294413-26f195200c16_4.webp', 'Opera View Double room', false, 1),
(75, 'STAY', 20, 'stays/20/photo-1551882547-ff40c63fe5fa_4.webp', 'Palm Jumeirah tower at dusk', true, 0),
(76, 'STAY', 20, 'stays/20/photo-1566073771259-6a8506099945_4.webp', 'Private beach club', false, 1),
(77, 'STAY', 21, 'stays/21/photo-1584132967334-10e028bd69f7_3.webp', 'Marina Bay infinity pool', true, 0),
(78, 'STAY', 21, 'stays/21/photo-1611892440504-42a792e24d32_3.webp', 'Gardens by the Bay view', false, 1),
(79, 'STAY', 22, 'stays/22/photo-1618773928121-c32242e63f39_4.webp', 'Colonial courtyard entrance', true, 0),
(80, 'STAY', 22, 'stays/22/photo-1629140727571-9b5c6f6267b4_7.webp', 'View toward Sacsayhuamán', false, 1),
(81, 'STAY', 23, 'stays/23/photo-1631049307264-da0ec9d70304_4.webp', 'Baroque building facade', true, 0),
(82, 'STAY', 23, 'stays/23/premium_photo-1661929519129-7a76946c1d38_3.webp', 'Astronomical Clock view', false, 1),
(83, 'STAY', 24, 'stays/24/premium_photo-1661964071015-d97428970584_2.webp', 'Teak house on the river', true, 0),
(84, 'STAY', 24, 'stays/24/premium_photo-1661964402307-02267d1423f5_4.webp', 'Private longtail boat dock', false, 1),
(85, 'STAY', 25, 'stays/25/premium_photo-1675745329954-9639d3b74bbf_6.webp', 'Nyhavn canal house facade', true, 0),
(86, 'STAY', 25, 'stays/25/premium_photo-1687960116497-0dc41e1808a2_3.webp', 'Canal view from the window', false, 1),
(87, 'STAY', 26, 'stays/26/photo-1445019980597-93fa8acb246c_3.webp', 'Garden cottage exterior', true, 0),
(88, 'STAY', 26, 'stays/26/photo-1455587734955-081b22074882_4.webp', 'Lush Karen garden', false, 1),
(89, 'STAY', 27, 'stays/27/photo-1495365200479-c4ed1d35e1aa.webp', 'Copacabana beachfront balcony', true, 0),
(90, 'STAY', 27, 'stays/27/photo-1496417263034-38ec4f0b665a_3.webp', 'Wraparound balcony view', false, 1),
(91, 'STAY', 28, 'stays/28/photo-1520250497591-112f2f40a3f4_3.webp', 'Gangnam tower exterior', true, 0),
(92, 'STAY', 28, 'stays/28/photo-1535827841776-24afc1e255ac_6.webp', 'Rooftop bar at night', false, 1),
(93, 'STAY', 29, 'stays/29/photo-1542314831-068cd1dbfeeb_3.webp', 'Cedar cabin exterior', true, 0),
(94, 'STAY', 29, 'stays/29/photo-1549294413-26f195200c16_4.webp', 'Deck with harbor views', false, 1),
(95, 'STAY', 30, 'stays/30/photo-1551882547-ff40c63fe5fa_4.webp', 'Stone flat on the Royal Mile', true, 0),
(96, 'STAY', 30, 'stays/30/photo-1566073771259-6a8506099945_4.webp', 'Living room with castle view', false, 1),
(97, 'STAY', 31, 'stays/31/photo-1584132967334-10e028bd69f7_3.webp', 'Old Quarter tube house facade', true, 0),
(98, 'STAY', 31, 'stays/31/photo-1611892440504-42a792e24d32_3.webp', 'Hidden interior courtyard', false, 1),
(99, 'STAY', 32, 'stays/32/photo-1618773928121-c32242e63f39_4.webp', 'Danube-bank hotel facade', true, 0),
(100, 'STAY', 32, 'stays/32/photo-1629140727571-9b5c6f6267b4_7.webp', 'Buda Castle view', false, 1),
(101, 'STAY', 33, 'stays/33/photo-1631049307264-da0ec9d70304_4.webp', 'Timber lodge on Lake Wakatipu', true, 0),
(102, 'STAY', 33, 'stays/33/premium_photo-1661929519129-7a76946c1d38_3.webp', 'View of the Remarkables', false, 1),
(103, 'STAY', 34, 'stays/34/premium_photo-1661964071015-d97428970584_2.webp', 'Colonial house facade in Habana Vieja', true, 0),
(104, 'STAY', 34, 'stays/34/premium_photo-1661964402307-02267d1423f5_4.webp', 'Plant-filled interior patio', false, 1),
(105, 'STAY', 35, 'stays/35/premium_photo-1675745329954-9639d3b74bbf_6.webp', 'Market Square hotel facade', true, 0),
(106, 'STAY', 35, 'stays/35/premium_photo-1687960116497-0dc41e1808a2_3.webp', 'View of St. Mary''s Basilica', false, 1),
(107, 'ROOM', 27, 'rooms/27/photo-1445019980597-93fa8acb246c_3.webp', 'Tagus View Room', true, 0),
(108, 'ROOM', 28, 'rooms/28/photo-1455587734955-081b22074882_4.webp', 'Tatami Twin', true, 0),
(109, 'ROOM', 29, 'rooms/29/photo-1495365200479-c4ed1d35e1aa.webp', 'Zen Garden Suite', true, 0),
(110, 'ROOM', 30, 'rooms/30/photo-1496417263034-38ec4f0b665a_3.webp', 'Machiya Penthouse', true, 0),
(111, 'ROOM', 31, 'rooms/31/photo-1520250497591-112f2f40a3f4_3.webp', 'Palermo Loft Room', true, 0),
(112, 'ROOM', 32, 'rooms/32/photo-1535827841776-24afc1e255ac_6.webp', 'Opera View Double', true, 0),
(113, 'ROOM', 33, 'rooms/33/photo-1542314831-068cd1dbfeeb_3.webp', 'Ringstrasse Suite', true, 0),
(114, 'ROOM', 34, 'rooms/34/photo-1549294413-26f195200c16_4.webp', 'Marina View King', true, 0),
(115, 'ROOM', 35, 'rooms/35/photo-1551882547-ff40c63fe5fa_4.webp', 'Palm Suite', true, 0),
(116, 'ROOM', 36, 'rooms/36/photo-1566073771259-6a8506099945_4.webp', 'Royal Penthouse', true, 0),
(117, 'ROOM', 37, 'rooms/37/photo-1584132967334-10e028bd69f7_3.webp', 'Bay View Queen', true, 0),
(118, 'ROOM', 38, 'rooms/38/photo-1611892440504-42a792e24d32_3.webp', 'Gardens Suite', true, 0),
(119, 'ROOM', 39, 'rooms/39/photo-1618773928121-c32242e63f39_4.webp', 'Courtyard Room', true, 0),
(120, 'ROOM', 40, 'rooms/40/photo-1629140727571-9b5c6f6267b4_7.webp', 'Old Town Double', true, 0),
(121, 'ROOM', 41, 'rooms/41/photo-1631049307264-da0ec9d70304_4.webp', 'Astronomical Suite', true, 0),
(122, 'ROOM', 42, 'rooms/42/premium_photo-1661929519129-7a76946c1d38_3.webp', 'Riverside Room', true, 0),
(123, 'ROOM', 43, 'rooms/43/premium_photo-1661964071015-d97428970584_2.webp', 'Canal View Room', true, 0),
(124, 'ROOM', 44, 'rooms/44/premium_photo-1661964402307-02267d1423f5_4.webp', 'Garden Cottage Room', true, 0),
(125, 'ROOM', 45, 'rooms/45/premium_photo-1675745329954-9639d3b74bbf_6.webp', 'Beachfront Balcony Room', true, 0),
(126, 'ROOM', 46, 'rooms/46/premium_photo-1687960116497-0dc41e1808a2_3.webp', 'Gangnam Standard', true, 0),
(127, 'ROOM', 47, 'rooms/47/photo-1445019980597-93fa8acb246c_3.webp', 'Rooftop King', true, 0),
(128, 'ROOM', 48, 'rooms/48/photo-1455587734955-081b22074882_4.webp', 'Executive Suite', true, 0),
(129, 'ROOM', 49, 'rooms/49/photo-1495365200479-c4ed1d35e1aa.webp', 'Park View Room', true, 0),
(130, 'ROOM', 50, 'rooms/50/photo-1496417263034-38ec4f0b665a_3.webp', 'Royal Mile Room', true, 0),
(131, 'ROOM', 51, 'rooms/51/photo-1520250497591-112f2f40a3f4_3.webp', 'Courtyard Suite', true, 0),
(132, 'ROOM', 52, 'rooms/52/photo-1535827841776-24afc1e255ac_6.webp', 'Danube View Double', true, 0),
(133, 'ROOM', 53, 'rooms/53/photo-1542314831-068cd1dbfeeb_3.webp', 'Castle View Suite', true, 0),
(134, 'ROOM', 54, 'rooms/54/photo-1549294413-26f195200c16_4.webp', 'Remarkables View Room', true, 0),
(135, 'ROOM', 55, 'rooms/55/photo-1551882547-ff40c63fe5fa_4.webp', 'Patio Room', true, 0),
(136, 'ROOM', 56, 'rooms/56/photo-1566073771259-6a8506099945_4.webp', 'Market Square Double', true, 0),
(137, 'ROOM', 57, 'rooms/57/photo-1584132967334-10e028bd69f7_3.webp', 'Basilica View Suite', true, 0)
ON CONFLICT (id) DO NOTHING;
SELECT setval(pg_get_serial_sequence('media', 'id'), COALESCE(MAX(id), 1)) FROM media;

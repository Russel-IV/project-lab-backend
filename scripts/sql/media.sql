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
-- per stay, 40 total). Room pictures: new -- one per room (26 total, ids 41-66),
-- captioned with the room's own name, previously never seeded at all.
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

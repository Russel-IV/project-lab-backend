-- StayPicture/RoomPicture generalized into media-service's Media table (docs/adr/0003,
-- Phase 3 of the migration plan). Same pragmatic dev/lab cutover as V18: no production
-- data to preserve, so a straight drop rather than a data-copy migration.
DROP TABLE IF EXISTS stay_picture;
DROP TABLE IF EXISTS room_picture;

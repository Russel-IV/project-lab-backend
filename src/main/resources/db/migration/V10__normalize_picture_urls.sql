-- Strip the /uploads/ prefix stored by the pre-StorageService code.
-- New code stores bare keys (stays/{id}/{uuid}.ext); LocalStorageService.toUrl() adds the prefix at read time.
UPDATE stay_picture
SET url = REGEXP_REPLACE(url, '^/uploads/', '')
WHERE url LIKE '/uploads/%';

-- No urls here: storage_key is the object's key in the bucket, and the url a browser fetches is composed from
-- it and the configured CDN base when the asset is read. The seed used to carry a second, absolute copy, which
-- meant every environment came up serving the demo library from one developer's MinIO.

-- The store's appearance, which merchant used to hold.
--
-- The logo and the slider images live in the media library now; the page layout seed's hero slides
-- reference the slider assets directly. Social links are part of the site settings record. The objects are
-- already in MinIO under merchant's old key layout, so the seed registers them rather than moving bytes.
-- Negative ids are seed-only: real ids come from sequences that only grow upward.

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-390001, '65f023632bc46470c104b75f', 'logo.jpeg', 'logo.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-390001', 'files/65f023632bc46470c104b75f/LOGO/logo.jpeg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-390002, '65f023632bc46470c104b75f', 'banner.jpeg', 'banner.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-390002', 'files/65f023632bc46470c104b75f/BANNER/banner.jpeg', now())
on conflict (id) do nothing;
-- The site settings row is created by 01-store.sql; this fills in what merchant used to own.
UPDATE content.site_settings SET logo_media_id = -390001, social_links = '[{"provider": "FACEBOOK", "url": "https://facebook.com/usaelectronics"}, {"provider": "X", "url": "https://x.com/usaelectronics"}, {"provider": "INSTAGRAM", "url": "https://instagram.com/usaelectronics"}, {"provider": "TIKTOK", "url": "https://tiktok.com/@usaelectronics"}]'::jsonb
 WHERE store_merchant_id = '65f023632bc46470c104b75f';
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-390003, '65f023632bc46470c104b75f', 'slide-1.jpeg', 'slide-1.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-390003', 'files/65f023632bc46470c104b75f/SLIDER/slide-1.jpeg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-390004, '65f023632bc46470c104b75f', 'slide-2.jpeg', 'slide-2.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-390004', 'files/65f023632bc46470c104b75f/SLIDER/slide-2.jpeg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-390005, '65f023632bc46470c104b75f', 'slide-3.jpeg', 'slide-3.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-390005', 'files/65f023632bc46470c104b75f/SLIDER/slide-3.jpeg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-390006, '65f023632bc46470c104b75f', 'slide-4.jpeg', 'slide-4.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-390006', 'files/65f023632bc46470c104b75f/SLIDER/slide-4.jpeg', now())
on conflict (id) do nothing;
INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-390007, '65f023632bc46470c104b75f', 'slide-5.jpeg', 'slide-5.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-390007', 'files/65f023632bc46470c104b75f/SLIDER/slide-5.jpeg', now())
on conflict (id) do nothing;
-- The quota row is what the console's summary reads for "N files, X of 5 GB"; uploads and deletes keep it in
-- step transactionally. These seeds insert assets directly, so they have to maintain it too — without this the
-- library listed every file while every counter beside it read zero.
INSERT INTO content.media_quota (store_merchant_id, bytes_used, file_count)
SELECT store_merchant_id, coalesce(sum(bytes), 0), count(*)
  FROM content.media_asset
 WHERE store_merchant_id = '65f023632bc46470c104b75f'
 GROUP BY store_merchant_id
ON CONFLICT (store_merchant_id) DO UPDATE
   SET bytes_used = excluded.bytes_used, file_count = excluded.file_count;
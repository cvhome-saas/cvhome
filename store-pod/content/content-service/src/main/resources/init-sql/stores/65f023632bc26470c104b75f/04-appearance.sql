-- The store's appearance, which merchant used to hold.
--
-- The logo and the slider live in the media library now; the slides became CAROUSEL banners, which is
-- what the storefront's hero reads. Social links are part of the site settings record. The objects are
-- already in MinIO under merchant's old key layout, so the seed registers them rather than moving bytes.
-- Negative ids are seed-only: real ids come from sequences that only grow upward.

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-290001, '65f023632bc26470c104b75f', 'logo.jpeg', 'logo.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-290001', 'files/65f023632bc26470c104b75f/LOGO/logo.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc26470c104b75f/LOGO/logo.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-290002, '65f023632bc26470c104b75f', 'banner.jpeg', 'banner.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-290002', 'files/65f023632bc26470c104b75f/BANNER/banner.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc26470c104b75f/BANNER/banner.jpeg', now())
on conflict (id) do nothing;

-- The site settings row is created by 01-store.sql; this fills in what merchant used to own.
UPDATE content.site_settings SET logo_media_id = -290001, social_links = '[{"provider": "FACEBOOK", "url": "https://facebook.com/egyptcarsales"}, {"provider": "X", "url": "https://x.com/egyptcarsales"}, {"provider": "INSTAGRAM", "url": "https://instagram.com/egyptcarsales"}, {"provider": "TIKTOK", "url": "https://tiktok.com/@egyptcarsales"}]'::jsonb
 WHERE store_merchant_id = '65f023632bc26470c104b75f';

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-290003, '65f023632bc26470c104b75f', 'slide-1.jpeg', 'slide-1.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-290003', 'files/65f023632bc26470c104b75f/SLIDER/slide-1.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc26470c104b75f/SLIDER/slide-1.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-2100, 'slide-1', 'BANNER', 0, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -290003, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-2200, now(), now(), '', 'Slide 1', 'Slide 1',
        '', '', '', '', -2100, 'en', 'Storefront slide 1')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-290004, '65f023632bc26470c104b75f', 'slide-2.jpeg', 'slide-2.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-290004', 'files/65f023632bc26470c104b75f/SLIDER/slide-2.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc26470c104b75f/SLIDER/slide-2.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-2101, 'slide-2', 'BANNER', 1, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -290004, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-2201, now(), now(), '', 'Slide 2', 'Slide 2',
        '', '', '', '', -2101, 'en', 'Storefront slide 2')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-290005, '65f023632bc26470c104b75f', 'slide-3.jpeg', 'slide-3.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-290005', 'files/65f023632bc26470c104b75f/SLIDER/slide-3.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc26470c104b75f/SLIDER/slide-3.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-2102, 'slide-3', 'BANNER', 2, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -290005, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-2202, now(), now(), '', 'Slide 3', 'Slide 3',
        '', '', '', '', -2102, 'en', 'Storefront slide 3')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-290006, '65f023632bc26470c104b75f', 'slide-4.jpeg', 'slide-4.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-290006', 'files/65f023632bc26470c104b75f/SLIDER/slide-4.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc26470c104b75f/SLIDER/slide-4.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-2103, 'slide-4', 'BANNER', 3, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -290006, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-2203, now(), now(), '', 'Slide 4', 'Slide 4',
        '', '', '', '', -2103, 'en', 'Storefront slide 4')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-290007, '65f023632bc26470c104b75f', 'slide-5.jpeg', 'slide-5.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-290007', 'files/65f023632bc26470c104b75f/SLIDER/slide-5.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc26470c104b75f/SLIDER/slide-5.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-2104, 'slide-5', 'BANNER', 4, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -290007, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-2204, now(), now(), '', 'Slide 5', 'Slide 5',
        '', '', '', '', -2104, 'en', 'Storefront slide 5')
on conflict (description_id) do nothing;

-- The quota row is what the console's summary reads for "N files, X of 5 GB"; uploads and deletes keep it in
-- step transactionally. These seeds insert assets directly, so they have to maintain it too — without this the
-- library listed every file while every counter beside it read zero.
INSERT INTO content.media_quota (store_merchant_id, bytes_used, file_count)
SELECT store_merchant_id, coalesce(sum(bytes), 0), count(*)
  FROM content.media_asset
 WHERE store_merchant_id = '65f023632bc26470c104b75f'
 GROUP BY store_merchant_id
ON CONFLICT (store_merchant_id) DO UPDATE
   SET bytes_used = excluded.bytes_used, file_count = excluded.file_count;

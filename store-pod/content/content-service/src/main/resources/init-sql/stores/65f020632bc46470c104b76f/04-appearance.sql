-- No urls here: storage_key is the object's key in the bucket, and the url a browser fetches is composed from
-- it and the configured CDN base when the asset is read. The seed used to carry a second, absolute copy, which
-- meant every environment came up serving the demo library from one developer's MinIO.

-- The store's appearance, which merchant used to hold.
--
-- The logo and the slider live in the media library now; the slides became CAROUSEL banners, which is
-- what the storefront's hero reads. Social links are part of the site settings record. The objects are
-- already in MinIO under merchant's old key layout, so the seed registers them rather than moving bytes.
-- Negative ids are seed-only: real ids come from sequences that only grow upward.

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-190001, '65f020632bc46470c104b76f', 'logo.jpeg', 'logo.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-190001', 'files/65f020632bc46470c104b76f/LOGO/logo.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-190002, '65f020632bc46470c104b76f', 'banner.jpeg', 'banner.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-190002', 'files/65f020632bc46470c104b76f/BANNER/banner.jpeg', now())
on conflict (id) do nothing;

-- The site settings row is created by 01-store.sql; this fills in what merchant used to own.
UPDATE content.site_settings SET logo_media_id = -190001, social_links = '[{"provider": "FACEBOOK", "url": "https://www.facebook.com/beauteeleganteparis"}, {"provider": "X", "url": "https://www.twitter.com/beauteelegante"}, {"provider": "INSTAGRAM", "url": "https://www.instagram.com/beaute.elegante.paris"}, {"provider": "TIKTOK", "url": "https://www.tiktok.com/@beauteelegante"}]'::jsonb
 WHERE store_merchant_id = '65f020632bc46470c104b76f';

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-190003, '65f020632bc46470c104b76f', 'slide-1.jpeg', 'slide-1.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-190003', 'files/65f020632bc46470c104b76f/SLIDER/slide-1.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-1100, 'slide-1', 'BANNER', 0, true, '65f020632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -190003, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-1200, now(), now(), '', 'Slide 1', 'Slide 1',
        '', '', '', '', -1100, 'en', 'Storefront slide 1')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-190004, '65f020632bc46470c104b76f', 'slide-2.jpeg', 'slide-2.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-190004', 'files/65f020632bc46470c104b76f/SLIDER/slide-2.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-1101, 'slide-2', 'BANNER', 1, true, '65f020632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -190004, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-1201, now(), now(), '', 'Slide 2', 'Slide 2',
        '', '', '', '', -1101, 'en', 'Storefront slide 2')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-190005, '65f020632bc46470c104b76f', 'slide-3.jpeg', 'slide-3.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-190005', 'files/65f020632bc46470c104b76f/SLIDER/slide-3.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-1102, 'slide-3', 'BANNER', 2, true, '65f020632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -190005, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-1202, now(), now(), '', 'Slide 3', 'Slide 3',
        '', '', '', '', -1102, 'en', 'Storefront slide 3')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-190006, '65f020632bc46470c104b76f', 'slide-4.jpeg', 'slide-4.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-190006', 'files/65f020632bc46470c104b76f/SLIDER/slide-4.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-1103, 'slide-4', 'BANNER', 3, true, '65f020632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -190006, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-1203, now(), now(), '', 'Slide 4', 'Slide 4',
        '', '', '', '', -1103, 'en', 'Storefront slide 4')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, uploaded_at)
VALUES (-190007, '65f020632bc46470c104b76f', 'slide-5.jpeg', 'slide-5.jpeg', 'image/jpeg', 'IMAGE',
        320000, 'seed-190007', 'files/65f020632bc46470c104b76f/SLIDER/slide-5.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-1104, 'slide-5', 'BANNER', 4, true, '65f020632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -190007, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-1204, now(), now(), '', 'Slide 5', 'Slide 5',
        '', '', '', '', -1104, 'en', 'Storefront slide 5')
on conflict (description_id) do nothing;

-- The quota row is what the console's summary reads for "N files, X of 5 GB"; uploads and deletes keep it in
-- step transactionally. These seeds insert assets directly, so they have to maintain it too — without this the
-- library listed every file while every counter beside it read zero.
INSERT INTO content.media_quota (store_merchant_id, bytes_used, file_count)
SELECT store_merchant_id, coalesce(sum(bytes), 0), count(*)
  FROM content.media_asset
 WHERE store_merchant_id = '65f020632bc46470c104b76f'
 GROUP BY store_merchant_id
ON CONFLICT (store_merchant_id) DO UPDATE
   SET bytes_used = excluded.bytes_used, file_count = excluded.file_count;

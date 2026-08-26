-- The store's appearance, which merchant used to hold.
--
-- The logo and the slider live in the media library now; the slides became CAROUSEL banners, which is
-- what the storefront's hero reads. Social links are part of the site settings record. The objects are
-- already in MinIO under merchant's old key layout, so the seed registers them rather than moving bytes.
-- Negative ids are seed-only: real ids come from sequences that only grow upward.

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-490001, '65f023632bc46470c104b76f', 'logo.jpeg', 'logo.jpeg', 'image/jpeg', 'IMAGE',
        0, 'seed-490001', 'files/65f023632bc46470c104b76f/LOGO/logo.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc46470c104b76f/LOGO/logo.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-490002, '65f023632bc46470c104b76f', 'banner.jpeg', 'banner.jpeg', 'image/jpeg', 'IMAGE',
        0, 'seed-490002', 'files/65f023632bc46470c104b76f/BANNER/banner.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc46470c104b76f/BANNER/banner.jpeg', now())
on conflict (id) do nothing;

-- The site settings row is created by 01-store.sql; this fills in what merchant used to own.
UPDATE content.site_settings SET logo_media_id = -490001, social_links = '[{"provider": "FACEBOOK", "url": "https://facebook.com/riyadhfashionhub"}, {"provider": "X", "url": "https://x.com/riyadhfashionhub"}, {"provider": "INSTAGRAM", "url": "https://instagram.com/riyadhfashionhub"}, {"provider": "TIKTOK", "url": "https://tiktok.com/@riyadhfashionhub"}]'::jsonb
 WHERE store_merchant_id = '65f023632bc46470c104b76f';

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-490003, '65f023632bc46470c104b76f', 'slide-1.jpeg', 'slide-1.jpeg', 'image/jpeg', 'IMAGE',
        0, 'seed-490003', 'files/65f023632bc46470c104b76f/SLIDER/slide-1.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc46470c104b76f/SLIDER/slide-1.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-4100, 'slide-1', 'BANNER', 0, true, '65f023632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -490003, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-4200, now(), now(), '', 'Slide 1', 'Slide 1',
        '', '', '', '', -4100, 'en', 'Storefront slide 1')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-490004, '65f023632bc46470c104b76f', 'slide-2.jpeg', 'slide-2.jpeg', 'image/jpeg', 'IMAGE',
        0, 'seed-490004', 'files/65f023632bc46470c104b76f/SLIDER/slide-2.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc46470c104b76f/SLIDER/slide-2.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-4101, 'slide-2', 'BANNER', 1, true, '65f023632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -490004, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-4201, now(), now(), '', 'Slide 2', 'Slide 2',
        '', '', '', '', -4101, 'en', 'Storefront slide 2')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-490005, '65f023632bc46470c104b76f', 'slide-3.jpeg', 'slide-3.jpeg', 'image/jpeg', 'IMAGE',
        0, 'seed-490005', 'files/65f023632bc46470c104b76f/SLIDER/slide-3.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc46470c104b76f/SLIDER/slide-3.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-4102, 'slide-3', 'BANNER', 2, true, '65f023632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -490005, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-4202, now(), now(), '', 'Slide 3', 'Slide 3',
        '', '', '', '', -4102, 'en', 'Storefront slide 3')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-490006, '65f023632bc46470c104b76f', 'slide-4.jpeg', 'slide-4.jpeg', 'image/jpeg', 'IMAGE',
        0, 'seed-490006', 'files/65f023632bc46470c104b76f/SLIDER/slide-4.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc46470c104b76f/SLIDER/slide-4.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-4103, 'slide-4', 'BANNER', 3, true, '65f023632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -490006, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-4203, now(), now(), '', 'Slide 4', 'Slide 4',
        '', '', '', '', -4103, 'en', 'Storefront slide 4')
on conflict (description_id) do nothing;

INSERT INTO content.media_asset (id, store_merchant_id, filename, original_filename, mime_type, kind,
                                 bytes, checksum, storage_key, public_url, uploaded_at)
VALUES (-490007, '65f023632bc46470c104b76f', 'slide-5.jpeg', 'slide-5.jpeg', 'image/jpeg', 'IMAGE',
        0, 'seed-490007', 'files/65f023632bc46470c104b76f/SLIDER/slide-5.jpeg', 'http://localhost:9000/d0dd4299-963a-4458-b31f-8efe31c35e8e/files/65f023632bc46470c104b76f/SLIDER/slide-5.jpeg', now())
on conflict (id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-4104, 'slide-5', 'BANNER', 4, true, '65f023632bc46470c104b76f', 'PUBLISHED', 1,
        'CAROUSEL', '{"target": null, "artwork": {"desktopMediaId": -490007, "mobileMediaId": null, "mobileCrop": null}, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name,
                                          title, meta_description, meta_keywords, meta_title, sef_url,
                                          content_id, language_code, alt_text)
VALUES (-4204, now(), now(), '', 'Slide 5', 'Slide 5',
        '', '', '', '', -4104, 'en', 'Storefront slide 5')
on conflict (description_id) do nothing;


-- Banners: the hero, the collection promos and a windowed slide.
--
-- Demo content for the test store 65f023632bc46470c104b76f (Riyadh Fashion Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- banner: hero-linen-season (HERO)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40033, now(), now(), 'seed', 'hero-linen-season', 'BANNER', 0, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'HERO', null, null, null, '{"target": {"kind": "PAGE", "value": "fabric-and-care"}, "artwork": {"desktopMediaId": -490004, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 35, "alignment": "left"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400065, now(), now(), 'seed', null, 'موسم الكتان', 'موسم الكتان', null, null, null, null, 'ar', -40033, 'TRANSLATED', null, 'قمصان كتان معلّقة على رف خشبي', 'تعرّف على أقمشتنا', 'أقمشة تسمح بمرور الهواء، وقَصّات لا تلتصق.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400066, now(), now(), 'seed', null, 'Linen season', 'Linen season', null, null, null, null, 'en', -40033, 'TRANSLATED', null, 'Linen shirts hanging on a wooden rail', 'About our fabric', 'Weaves that let air through, cuts that do not cling.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-4007, -490004, 'CONTENT', '-40033', 'موسم الكتان', -40033, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: collection-women (COLLECTION)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40034, now(), now(), 'seed', 'collection-women', 'BANNER', 1, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'COLLECTION', null, null, null, '{"target": {"kind": "COLLECTION", "value": "women"}, "artwork": {"desktopMediaId": -490005, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 40, "alignment": "center"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400067, now(), now(), 'seed', null, 'مجموعة النساء', 'مجموعة النساء', null, null, null, null, 'ar', -40034, 'TRANSLATED', null, 'إطلالة نسائية بألوان محايدة', 'تسوّقي المجموعة', 'قَصّات مستقيمة وألوان محايدة.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400068, now(), now(), 'seed', null, 'The women''s collection', 'The women''s collection', null, null, null, null, 'en', -40034, 'TRANSLATED', null, 'A women''s look in neutral tones', 'Shop the collection', 'Straight lines and neutral colour.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-4008, -490005, 'CONTENT', '-40034', 'مجموعة النساء', -40034, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: free-alterations (CAROUSEL)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40035, now(), now(), 'seed', 'free-alterations', 'BANNER', 5, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'CAROUSEL', null, null, null, '{"target": {"kind": "PAGE", "value": "our-stores"}, "artwork": {"desktopMediaId": -490006, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 30, "alignment": "left"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400069, now(), now(), 'seed', null, 'التعديل مجانًا', 'التعديل مجانًا', null, null, null, null, 'ar', -40035, 'TRANSLATED', null, 'خياط يعدّل قميصًا', 'مواعيد المعارض', 'على أي قطعة من المعرض، خلال ثلاثة أيام.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400070, now(), now(), 'seed', null, 'Alterations, free', 'Alterations, free', null, null, null, null, 'en', -40035, 'TRANSLATED', null, 'A tailor altering a shirt', 'Store hours', 'On anything bought in store, ready in three days.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-4009, -490006, 'CONTENT', '-40035', 'التعديل مجانًا', -40035, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: ramadan-teaser (CAROUSEL)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40036, now(), now(), 'seed', 'ramadan-teaser', 'BANNER', 6, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'CAROUSEL', now() + interval '14 days', now() + interval '35 days', null, '{"target": {"kind": "PAGE", "value": "ramadan-collection"}, "artwork": {"desktopMediaId": -490007, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 50, "alignment": "center"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400071, now(), now(), 'seed', null, 'مجموعة رمضان (مجدولة)', 'مجموعة رمضان (مجدولة)', null, null, null, null, 'ar', -40036, 'TRANSLATED', null, 'تصميم مجموعة رمضان', 'قريبًا', 'بانر بنافذة زمنية: يظهر بعد أسبوعين فقط.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400072, now(), now(), 'seed', null, 'Ramadan collection (scheduled)', 'Ramadan collection (scheduled)', null, null, null, null, 'en', -40036, 'TRANSLATED', null, 'Ramadan collection artwork', 'Coming soon', 'A windowed banner: it only goes live in two weeks.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-4010, -490007, 'CONTENT', '-40036', 'مجموعة رمضان (مجدولة)', -40036, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;


-- the five slides the store shipped with, given real copy in both of its languages
UPDATE content.content_description
   SET language_code = 'ar', name = 'موسم الكتان', title = 'موسم الكتان', subtitle = 'أقمشة تسمح بمرور الهواء، وقَصّات لا تلتصق.', cta_label = 'تعرّف على أقمشتنا',
       alt_text = 'قمصان كتان معلّقة على رف خشبي', date_modified = now()
 WHERE description_id = -4200;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400073, now(), now(), 'seed', null, 'Linen season', 'Linen season', null, null, null, null, 'en', -4100, 'TRANSLATED', null, 'Linen shirts hanging on a wooden rail', 'About our fabric', 'Weaves that let air through, cuts that do not cling.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'ar', name = 'التعديل مجانًا', title = 'التعديل مجانًا', subtitle = 'على أي قطعة من المعرض، جاهزة خلال ثلاثة أيام.', cta_label = 'مواعيد المعارض',
       alt_text = 'خياط يعدّل قميصًا في غرفة التعديل', date_modified = now()
 WHERE description_id = -4201;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400074, now(), now(), 'seed', null, 'Alterations, free', 'Alterations, free', null, null, null, null, 'en', -4101, 'TRANSLATED', null, 'A tailor altering a shirt in the workroom', 'Store hours', 'On anything bought in store, ready within three days.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'ar', name = 'مجموعة النساء', title = 'مجموعة النساء', subtitle = 'قَصّات مستقيمة وألوان محايدة.', cta_label = 'تسوّقي المجموعة',
       alt_text = 'إطلالة نسائية بألوان محايدة', date_modified = now()
 WHERE description_id = -4202;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400075, now(), now(), 'seed', null, 'The women’s collection', 'The women’s collection', null, null, null, null, 'en', -4102, 'TRANSLATED', null, 'A women’s look in neutral tones', 'Shop the collection', 'Straight lines and neutral colour.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'ar', name = 'المقاس الصحيح من أول مرة', title = 'المقاس الصحيح من أول مرة', subtitle = 'جداول بالسنتيمتر، وطريقة القياس بالتفصيل.', cta_label = 'دليل المقاسات',
       alt_text = 'شريط قياس على طاولة قص', date_modified = now()
 WHERE description_id = -4203;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400076, now(), now(), 'seed', null, 'The right size, first time', 'The right size, first time', null, null, null, null, 'en', -4103, 'TRANSLATED', null, 'A tape measure on a cutting table', 'Size guide', 'Charts in centimetres, and how to measure properly.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'ar', name = 'توصيل خلال يوم داخل الرياض', title = 'توصيل خلال يوم داخل الرياض', subtitle = 'مجانًا للطلبات فوق 200 ريال.', cta_label = 'سياسة الشحن',
       alt_text = 'مندوب توصيل يحمل طردًا', date_modified = now()
 WHERE description_id = -4204;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400077, now(), now(), 'seed', null, 'Next-day delivery in Riyadh', 'Next-day delivery in Riyadh', null, null, null, null, 'en', -4104, 'TRANSLATED', null, 'A courier carrying a parcel', 'Shipping policy', 'Free on orders over SAR 200.')
on conflict (description_id) do nothing;

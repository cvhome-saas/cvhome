-- Home-page sections, one of every kind.
--
-- Demo content for the test store 65f023632bc26470c104b75f (Egypt Car Sales), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- section: home-featured-banner (BANNER_REF)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20037, now(), now(), 'seed', 'home-featured-banner', 'SECTION', 0, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "BANNER_REF", "targetValue": "trade-in-promo", "mediaId": null, "itemLimit": null, "layout": null, "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200078, now(), now(), 'seed', null, 'عرض الأسبوع', 'عرض الأسبوع', null, null, null, null, 'ar', -20037, 'TRANSLATED', null, null, null, 'البانر المعروض حاليًا.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200079, now(), now(), 'seed', null, 'L''offre de la semaine', 'L''offre de la semaine', null, null, null, null, 'fr', -20037, 'TRANSLATED', null, null, null, 'La bannière actuellement en ligne.')
on conflict (description_id) do nothing;

-- section: home-featured-cars (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20038, now(), now(), 'seed', 'home-featured-cars', 'SECTION', 1, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "FEATURED_ITEMS", "mediaId": null, "itemLimit": 8, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200080, now(), now(), 'seed', null, 'سيارات مختارة', 'سيارات مختارة', null, null, null, null, 'ar', -20038, 'TRANSLATED', null, null, null, 'اختيار فريق المعرض هذا الأسبوع.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200081, now(), now(), 'seed', null, 'Notre sélection', 'Notre sélection', null, null, null, null, 'fr', -20038, 'TRANSLATED', null, null, null, 'Le choix de l''équipe cette semaine.')
on conflict (description_id) do nothing;

-- section: home-categories (CATEGORY_GRID)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20039, now(), now(), 'seed', 'home-categories', 'SECTION', 2, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "CATEGORY_GRID", "targetValue": "ACCESSORIES", "mediaId": null, "itemLimit": 6, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200082, now(), now(), 'seed', null, 'قطع غيار وإكسسوارات', 'قطع غيار وإكسسوارات', null, null, null, null, 'ar', -20039, 'TRANSLATED', null, null, null, 'كل ما تحتاجه بعد الشراء.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200083, now(), now(), 'seed', null, 'Pièces et accessoires', 'Pièces et accessoires', null, null, null, null, 'fr', -20039, 'TRANSLATED', null, null, null, 'Tout ce qui vient après l''achat.')
on conflict (description_id) do nothing;

-- section: home-new-arrivals (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20040, now(), now(), 'seed', 'home-new-arrivals', 'SECTION', 3, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "NEWLY_ADDED", "mediaId": null, "itemLimit": 8, "layout": "carousel", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200084, now(), now(), 'seed', null, 'وصل حديثًا', 'وصل حديثًا', null, null, null, null, 'ar', -20040, 'TRANSLATED', null, null, null, 'آخر ما دخل المعرض.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200085, now(), now(), 'seed', null, 'Dernières arrivées', 'Dernières arrivées', null, null, null, null, 'fr', -20040, 'TRANSLATED', null, null, null, 'Ce qui vient d''entrer au showroom.')
on conflict (description_id) do nothing;

-- section: home-promise (RICH_TEXT)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20041, now(), now(), 'seed', 'home-promise', 'SECTION', 4, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "RICH_TEXT", "targetValue": null, "mediaId": null, "itemLimit": null, "layout": null, "cta": {"kind": "PAGE", "value": "inspection-report", "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200086, now(), now(), 'seed', '<p>خمس إلى ثماني سيارات تُرفض كل أسبوع في ورشة المعادي. هذا هو السبب الوحيد الذي يجعل التقرير المرفق يعني شيئًا.</p>', 'لا نبيع ما لا نضمنه', 'لا نبيع ما لا نضمنه', null, null, null, null, 'ar', -20041, 'TRANSLATED', null, null, 'اقرأ تقرير الفحص', 'تعهّدنا في سطر واحد.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200087, now(), now(), 'seed', '<p>Cinq à huit véhicules sont refusés chaque semaine à l''atelier de Maadi. C''est la seule raison pour laquelle le rapport joint veut dire quelque chose.</p>', 'Nous ne vendons pas ce que nous ne garantissons pas', 'Nous ne vendons pas ce que nous ne garantissons pas', null, null, null, null, 'fr', -20041, 'TRANSLATED', null, null, 'Lire le rapport d''inspection', 'Notre engagement en une ligne.')
on conflict (description_id) do nothing;

-- section: home-full-catalogue (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20042, now(), now(), 'seed', 'home-full-catalogue', 'SECTION', 5, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "HOME_PAGE", "mediaId": null, "itemLimit": 24, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200088, now(), now(), 'seed', null, 'كل المعروض', 'كل المعروض', null, null, null, null, 'ar', -20042, 'TRANSLATED', null, null, null, 'كل سيارة في المعرض، بتقريرها.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200089, now(), now(), 'seed', null, 'Tout le stock', 'Tout le stock', null, null, null, null, 'fr', -20042, 'TRANSLATED', null, null, null, 'Chaque véhicule du showroom, avec son rapport.')
on conflict (description_id) do nothing;

-- section: home-image-showroom (IMAGE)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20043, now(), now(), 'seed', 'home-image-showroom', 'SECTION', 6, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "IMAGE", "targetValue": null, "mediaId": -290005, "itemLimit": null, "layout": null, "cta": {"kind": "PAGE", "value": "showrooms", "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200090, now(), now(), 'seed', null, 'ثلاثة معارض في القاهرة', 'ثلاثة معارض في القاهرة', null, null, null, null, 'ar', -20043, 'TRANSLATED', null, null, 'مواعيد الفروع', 'مدينة نصر، المعادي، الشيخ زايد.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200091, now(), now(), 'seed', null, 'Trois showrooms au Caire', 'Trois showrooms au Caire', null, null, null, null, 'fr', -20043, 'TRANSLATED', null, null, 'Horaires des showrooms', 'Nasr City, Maadi, Sheikh Zayed.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-2011, -290005, 'CONTENT', '-20043', 'ثلاثة معارض في القاهرة', -20043, 'SECTION', 'image')
on conflict (id) do nothing;

-- section: home-recommended (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20044, now(), now(), 'seed', 'home-recommended', 'SECTION', 7, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "RECOMMENDED", "mediaId": null, "itemLimit": 12, "layout": "carousel", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200092, now(), now(), 'seed', null, 'مقترحة لك', 'مقترحة لك', null, null, null, null, 'ar', -20044, 'TRANSLATED', null, null, null, 'بناءً على أكثر ما يسأل عنه العملاء.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200093, now(), now(), 'seed', null, 'Recommandé pour vous', 'Recommandé pour vous', null, null, null, null, 'fr', -20044, 'TRANSLATED', null, null, null, 'D''après ce que les clients demandent le plus.')
on conflict (description_id) do nothing;

-- section: home-blog (POST_FEED)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20045, now(), now(), 'seed', 'home-blog', 'SECTION', 8, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "POST_FEED", "targetValue": null, "mediaId": null, "itemLimit": 3, "layout": null, "cta": {"kind": "BLOG_INDEX", "value": null, "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200094, now(), now(), 'seed', null, 'من المدونة', 'من المدونة', null, null, null, null, 'ar', -20045, 'TRANSLATED', null, null, 'كل المقالات', 'أدلة الشراء والصيانة والكهربائي.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200095, now(), now(), 'seed', null, 'Sur le blog', 'Sur le blog', null, null, null, null, 'fr', -20045, 'TRANSLATED', null, null, 'Tous les articles', 'Guides d''achat, entretien et électrique.')
on conflict (description_id) do nothing;

-- section: home-faq (FAQ_REF)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20046, now(), now(), 'seed', 'home-faq', 'SECTION', 9, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "FAQ_REF", "targetValue": "ordering", "mediaId": null, "itemLimit": null, "layout": null, "cta": {"kind": "FAQ_INDEX", "value": null, "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200096, now(), now(), 'seed', null, 'الشراء والتمويل باختصار', 'الشراء والتمويل باختصار', null, null, null, null, 'ar', -20046, 'TRANSLATED', null, null, 'كل المساعدة', 'أكثر ثلاثة أسئلة تكرارًا.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200097, now(), now(), 'seed', null, 'Achat et financement, en bref', 'Achat et financement, en bref', null, null, null, null, 'fr', -20046, 'TRANSLATED', null, null, 'Toute l''aide', 'Les trois questions les plus fréquentes.')
on conflict (description_id) do nothing;


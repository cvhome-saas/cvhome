-- Home-page sections, one of every kind.
--
-- Demo content for the test store 65f023632bc46470c104b75f (USA Electronics Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- section: home-banner-ref (BANNER_REF)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30037, now(), now(), 'seed', 'home-banner-ref', 'SECTION', 0, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "BANNER_REF", "targetValue": "trade-in-banner", "mediaId": null, "itemLimit": null, "layout": null, "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300078, now(), now(), 'seed', null, 'This week', 'This week', null, null, null, null, 'en', -30037, 'TRANSLATED', null, null, null, 'The banner currently live.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300079, now(), now(), 'seed', null, 'Cette semaine', 'Cette semaine', null, null, null, null, 'fr', -30037, 'TRANSLATED', null, null, null, 'La bannière actuellement en ligne.')
on conflict (description_id) do nothing;

-- section: home-featured (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30038, now(), now(), 'seed', 'home-featured', 'SECTION', 1, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "FEATURED_ITEMS", "mediaId": null, "itemLimit": 8, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300080, now(), now(), 'seed', null, 'Bench-tested picks', 'Bench-tested picks', null, null, null, null, 'en', -30038, 'TRANSLATED', null, null, null, 'What passed, and why it stayed.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300081, now(), now(), 'seed', null, 'Sélection du banc d''essai', 'Sélection du banc d''essai', null, null, null, null, 'fr', -30038, 'TRANSLATED', null, null, null, 'Ce qui a passé les tests, et pourquoi.')
on conflict (description_id) do nothing;

-- section: home-categories (CATEGORY_GRID)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30039, now(), now(), 'seed', 'home-categories', 'SECTION', 2, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "CATEGORY_GRID", "targetValue": "GAMING", "mediaId": null, "itemLimit": 6, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300082, now(), now(), 'seed', null, 'Browse gaming', 'Browse gaming', null, null, null, null, 'en', -30039, 'TRANSLATED', null, null, null, 'Consoles, displays and peripherals.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300083, now(), now(), 'seed', null, 'Rayon gaming', 'Rayon gaming', null, null, null, null, 'fr', -30039, 'TRANSLATED', null, null, null, 'Consoles, écrans et périphériques.')
on conflict (description_id) do nothing;

-- section: home-new (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30040, now(), now(), 'seed', 'home-new', 'SECTION', 3, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "NEWLY_ADDED", "mediaId": null, "itemLimit": 8, "layout": "carousel", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300084, now(), now(), 'seed', null, 'Just added', 'Just added', null, null, null, null, 'en', -30040, 'TRANSLATED', null, null, null, 'Straight off the bench.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300085, now(), now(), 'seed', null, 'Nouveautés', 'Nouveautés', null, null, null, null, 'fr', -30040, 'TRANSLATED', null, null, null, 'Tout juste sortis du banc.')
on conflict (description_id) do nothing;

-- section: home-standard (RICH_TEXT)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30041, now(), now(), 'seed', 'home-standard', 'SECTION', 4, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "RICH_TEXT", "targetValue": null, "mediaId": null, "itemLimit": null, "layout": null, "cta": {"kind": "PAGE", "value": "buying-guides", "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300086, now(), now(), 'seed', '<p>We are offered roughly three thousand products a year and list about four hundred. The difference is a week on the bench, and a willingness to say no to a good margin.</p>', 'Four hundred products out of three thousand', 'Four hundred products out of three thousand', null, null, null, null, 'en', -30041, 'TRANSLATED', null, null, 'How we choose', 'The rest did not pass.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300087, now(), now(), 'seed', '<p>On nous propose environ trois mille produits par an ; nous en référençons quatre cents. L''écart, c''est une semaine de banc d''essai et la volonté de refuser une bonne marge.</p>', 'Quatre cents produits sur trois mille', 'Quatre cents produits sur trois mille', null, null, null, null, 'fr', -30041, 'TRANSLATED', null, null, 'Comment nous choisissons', 'Les autres n''ont pas passé le banc.')
on conflict (description_id) do nothing;

-- section: home-full-catalogue (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30042, now(), now(), 'seed', 'home-full-catalogue', 'SECTION', 5, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "HOME_PAGE", "mediaId": null, "itemLimit": 24, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300088, now(), now(), 'seed', null, 'The whole catalogue', 'The whole catalogue', null, null, null, null, 'en', -30042, 'TRANSLATED', null, null, null, 'Four hundred products, all bench-tested.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300089, now(), now(), 'seed', null, 'Tout le catalogue', 'Tout le catalogue', null, null, null, null, 'fr', -30042, 'TRANSLATED', null, null, null, 'Quatre cents produits, tous passés au banc.')
on conflict (description_id) do nothing;

-- section: home-image-bench (IMAGE)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30043, now(), now(), 'seed', 'home-image-bench', 'SECTION', 6, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "IMAGE", "targetValue": null, "mediaId": -390005, "itemLimit": null, "layout": null, "cta": {"kind": "URL", "value": "/blog?category=workshop", "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300090, now(), now(), 'seed', null, 'From the bench', 'From the bench', null, null, null, null, 'en', -30043, 'TRANSLATED', null, null, 'Read the measurements', 'Thermals, latency, battery capacity — measured, then written up.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300091, now(), now(), 'seed', null, 'Au banc d''essai', 'Au banc d''essai', null, null, null, null, 'fr', -30043, 'TRANSLATED', null, null, 'Lire les mesures', 'Thermique, latence, capacité de batterie — mesurées, puis publiées.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-3011, -390005, 'CONTENT', '-30043', 'From the bench', -30043, 'SECTION', 'image')
on conflict (id) do nothing;

-- section: home-recommended (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30044, now(), now(), 'seed', 'home-recommended', 'SECTION', 7, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "RECOMMENDED", "mediaId": null, "itemLimit": 12, "layout": "carousel", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300092, now(), now(), 'seed', null, 'Recommended for you', 'Recommended for you', null, null, null, null, 'en', -30044, 'TRANSLATED', null, null, null, 'What the bench notes point to most often.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300093, now(), now(), 'seed', null, 'Recommandé pour vous', 'Recommandé pour vous', null, null, null, null, 'fr', -30044, 'TRANSLATED', null, null, null, 'Ce vers quoi les notes de banc pointent le plus.')
on conflict (description_id) do nothing;

-- section: home-posts (POST_FEED)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30045, now(), now(), 'seed', 'home-posts', 'SECTION', 8, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "POST_FEED", "targetValue": null, "mediaId": null, "itemLimit": 3, "layout": null, "cta": {"kind": "BLOG_INDEX", "value": null, "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300094, now(), now(), 'seed', null, 'Latest writing', 'Latest writing', null, null, null, null, 'en', -30045, 'TRANSLATED', null, null, 'All articles', 'Reviews, how-tos and bench notes.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300095, now(), now(), 'seed', null, 'Derniers articles', 'Derniers articles', null, null, null, null, 'fr', -30045, 'TRANSLATED', null, null, 'Tous les articles', 'Tests, tutoriels et notes de banc.')
on conflict (description_id) do nothing;

-- section: home-faq (FAQ_REF)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30046, now(), now(), 'seed', 'home-faq', 'SECTION', 9, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "FAQ_REF", "targetValue": "returns", "mediaId": null, "itemLimit": null, "layout": null, "cta": {"kind": "FAQ_INDEX", "value": null, "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300096, now(), now(), 'seed', null, 'Returns & warranty, briefly', 'Returns & warranty, briefly', null, null, null, null, 'en', -30046, 'TRANSLATED', null, null, 'All help', 'The three questions we get most.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300097, now(), now(), 'seed', null, 'Retours et garantie, en bref', 'Retours et garantie, en bref', null, null, null, null, 'fr', -30046, 'TRANSLATED', null, null, 'Toute l''aide', 'Les trois questions les plus fréquentes.')
on conflict (description_id) do nothing;


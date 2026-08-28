-- Home-page sections, one of every kind.
--
-- Demo content for the test store 65f020632bc46470c104b76f (Beauté Élégante Paris), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- section: accueil-carousel (BANNER_REF)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10037, now(), now(), 'seed', 'accueil-carousel', 'SECTION', 0, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "BANNER_REF", "targetValue": "edit-printemps", "mediaId": null, "itemLimit": null, "layout": null, "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100078, now(), now(), 'seed', null, 'À la une', 'À la une', null, null, null, null, 'fr', -10037, 'TRANSLATED', null, null, null, 'La bannière du moment.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100079, now(), now(), 'seed', null, 'Featured', 'Featured', null, null, null, null, 'en', -10037, 'TRANSLATED', null, null, null, 'The banner of the moment.')
on conflict (description_id) do nothing;

-- section: accueil-selection (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10038, now(), now(), 'seed', 'accueil-selection', 'SECTION', 1, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "FEATURED_ITEMS", "mediaId": null, "itemLimit": 8, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100080, now(), now(), 'seed', null, 'Notre sélection', 'Notre sélection', null, null, null, null, 'fr', -10038, 'TRANSLATED', null, null, null, 'Choisie chaque semaine par l''équipe boutique.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100081, now(), now(), 'seed', null, 'Our selection', 'Our selection', null, null, null, null, 'en', -10038, 'TRANSLATED', null, null, null, 'Chosen each week by the boutique team.')
on conflict (description_id) do nothing;

-- section: accueil-categories (CATEGORY_GRID)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10039, now(), now(), 'seed', 'accueil-categories', 'SECTION', 2, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "CATEGORY_GRID", "targetValue": "SKINCARE", "mediaId": null, "itemLimit": 6, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100082, now(), now(), 'seed', null, 'Par besoin de peau', 'Par besoin de peau', null, null, null, null, 'fr', -10039, 'TRANSLATED', null, null, null, 'Nettoyer, hydrater, traiter.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100083, now(), now(), 'seed', null, 'By skin need', 'By skin need', null, null, null, null, 'en', -10039, 'TRANSLATED', null, null, null, 'Cleanse, hydrate, treat.')
on conflict (description_id) do nothing;

-- section: accueil-nouveautes (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10040, now(), now(), 'seed', 'accueil-nouveautes', 'SECTION', 3, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "NEWLY_ADDED", "mediaId": null, "itemLimit": 8, "layout": "carousel", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100084, now(), now(), 'seed', null, 'Nouveautés', 'Nouveautés', null, null, null, null, 'fr', -10040, 'TRANSLATED', null, null, null, 'Les derniers arrivages de l''atelier.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100085, now(), now(), 'seed', null, 'New arrivals', 'New arrivals', null, null, null, null, 'en', -10040, 'TRANSLATED', null, null, null, 'The latest out of the workshop.')
on conflict (description_id) do nothing;

-- section: accueil-manifeste (RICH_TEXT)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10041, now(), now(), 'seed', 'accueil-manifeste', 'SECTION', 4, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "RICH_TEXT", "targetValue": null, "mediaId": null, "itemLimit": null, "layout": null, "cta": {"kind": "PAGE", "value": "nos-ingredients", "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100086, now(), now(), 'seed', '<p>Nous ne sortons un produit que lorsqu''il fait quelque chose que la gamme ne faisait pas déjà. C''est pour cela qu''il y a vingt-deux références, et pas deux cents.</p>', 'Formulé court, dosé juste', 'Formulé court, dosé juste', null, null, null, null, 'fr', -10041, 'TRANSLATED', null, null, 'Lire notre charte', 'Notre charte en une phrase.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100087, now(), now(), 'seed', '<p>We only release a product when it does something the range did not already do. Which is why there are twenty-two references, not two hundred.</p>', 'Short formulas, honest doses', 'Short formulas, honest doses', null, null, null, null, 'en', -10041, 'TRANSLATED', null, null, 'Read our charter', 'Our charter in one line.')
on conflict (description_id) do nothing;

-- section: accueil-catalogue (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10042, now(), now(), 'seed', 'accueil-catalogue', 'SECTION', 5, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "HOME_PAGE", "mediaId": null, "itemLimit": 24, "layout": "grid", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100088, now(), now(), 'seed', null, 'Toute la gamme', 'Toute la gamme', null, null, null, null, 'fr', -10042, 'TRANSLATED', null, null, null, 'Vingt-deux références, pas deux cents.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100089, now(), now(), 'seed', null, 'The whole range', 'The whole range', null, null, null, null, 'en', -10042, 'TRANSLATED', null, null, null, 'Twenty-two references, not two hundred.')
on conflict (description_id) do nothing;

-- section: accueil-image-atelier (IMAGE)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10043, now(), now(), 'seed', 'accueil-image-atelier', 'SECTION', 6, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "IMAGE", "targetValue": null, "mediaId": -190005, "itemLimit": null, "layout": null, "cta": {"kind": "URL", "value": "/blog/dans-latelier-de-nantes", "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100090, now(), now(), 'seed', null, 'L''atelier de Nantes', 'L''atelier de Nantes', null, null, null, null, 'fr', -10043, 'TRANSLATED', null, null, 'Visiter l''atelier', 'Six cents litres par semaine, trois cuves, une chimiste.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100091, now(), now(), 'seed', null, 'The Nantes workshop', 'The Nantes workshop', null, null, null, null, 'en', -10043, 'TRANSLATED', null, null, 'Visit the workshop', 'Six hundred litres a week, three vessels, one chemist.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-1011, -190005, 'CONTENT', '-10043', 'L''atelier de Nantes', -10043, 'SECTION', 'image')
on conflict (id) do nothing;

-- section: accueil-recommande (PRODUCT_GROUP)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10044, now(), now(), 'seed', 'accueil-recommande', 'SECTION', 7, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "PRODUCT_GROUP", "targetValue": "RECOMMENDED", "mediaId": null, "itemLimit": 12, "layout": "carousel", "cta": null}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100092, now(), now(), 'seed', null, 'Recommandé pour vous', 'Recommandé pour vous', null, null, null, null, 'fr', -10044, 'TRANSLATED', null, null, null, 'D''après ce que la boutique conseille le plus.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100093, now(), now(), 'seed', null, 'Recommended for you', 'Recommended for you', null, null, null, null, 'en', -10044, 'TRANSLATED', null, null, null, 'Based on what the boutique advises most.')
on conflict (description_id) do nothing;

-- section: accueil-journal (POST_FEED)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10045, now(), now(), 'seed', 'accueil-journal', 'SECTION', 8, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "POST_FEED", "targetValue": null, "mediaId": null, "itemLimit": 3, "layout": null, "cta": {"kind": "BLOG_INDEX", "value": null, "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100094, now(), now(), 'seed', null, 'Le Journal', 'Le Journal', null, null, null, null, 'fr', -10045, 'TRANSLATED', null, null, 'Tous les articles', 'Rituels, ingrédients et coulisses.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100095, now(), now(), 'seed', null, 'The Journal', 'The Journal', null, null, null, null, 'en', -10045, 'TRANSLATED', null, null, 'All articles', 'Rituals, ingredients and behind the scenes.')
on conflict (description_id) do nothing;

-- section: accueil-faq (FAQ_REF)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10046, now(), now(), 'seed', 'accueil-faq', 'SECTION', 9, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, null, null, null, null, '{"kind": "FAQ_REF", "targetValue": "shipping", "mediaId": null, "itemLimit": null, "layout": null, "cta": {"kind": "FAQ_INDEX", "value": null, "broken": null}}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100096, now(), now(), 'seed', null, 'Livraison, en bref', 'Livraison, en bref', null, null, null, null, 'fr', -10046, 'TRANSLATED', null, null, 'Toute l''aide', 'Les trois questions qui reviennent.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100097, now(), now(), 'seed', null, 'Delivery, briefly', 'Delivery, briefly', null, null, null, null, 'en', -10046, 'TRANSLATED', null, null, 'All help', 'The three questions we get most.')
on conflict (description_id) do nothing;


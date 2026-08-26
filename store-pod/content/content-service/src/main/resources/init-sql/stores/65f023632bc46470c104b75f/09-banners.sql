-- Banners: the hero, the collection promos and a windowed slide.
--
-- Demo content for the test store 65f023632bc46470c104b75f (USA Electronics Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- banner: hero-two-year-warranty (HERO)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30033, now(), now(), 'seed', 'hero-two-year-warranty', 'BANNER', 0, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'HERO', null, null, null, '{"target": {"kind": "PAGE", "value": "warranty"}, "artwork": {"desktopMediaId": -390004, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 40, "alignment": "left"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300065, now(), now(), 'seed', null, 'Two years, on everything', 'Two years, on everything', null, null, null, null, 'en', -30033, 'TRANSLATED', null, 'A repair bench with a laptop open on it', 'Read the warranty', 'A year longer than the manufacturer, honoured at our own bench.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300066, now(), now(), 'seed', null, 'Deux ans, sur tout', 'Deux ans, sur tout', null, null, null, null, 'fr', -30033, 'TRANSLATED', null, 'Un établi de réparation avec un portable ouvert', 'Lire la garantie', 'Un an de plus que le constructeur, assuré dans notre atelier.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-3007, -390004, 'CONTENT', '-30033', 'Two years, on everything', -30033, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: collection-gaming (COLLECTION)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30034, now(), now(), 'seed', 'collection-gaming', 'BANNER', 1, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'COLLECTION', null, null, null, '{"target": {"kind": "COLLECTION", "value": "gaming"}, "artwork": {"desktopMediaId": -390005, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 35, "alignment": "center"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300067, now(), now(), 'seed', null, 'Gaming, latency measured', 'Gaming, latency measured', null, null, null, null, 'en', -30034, 'TRANSLATED', null, 'A gaming setup lit in blue', 'Shop gaming', 'Every display tested in game mode before it ships.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300068, now(), now(), 'seed', null, 'Gaming, latence mesurée', 'Gaming, latence mesurée', null, null, null, null, 'fr', -30034, 'TRANSLATED', null, 'Un poste de jeu éclairé en bleu', 'Voir le gaming', 'Chaque écran testé en mode jeu avant expédition.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-3008, -390005, 'CONTENT', '-30034', 'Gaming, latency measured', -30034, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: trade-in-banner (CAROUSEL)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30035, now(), now(), 'seed', 'trade-in-banner', 'BANNER', 5, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'CAROUSEL', null, null, null, '{"target": {"kind": "PAGE", "value": "trade-in-program"}, "artwork": {"desktopMediaId": -390006, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 30, "alignment": "left"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300069, now(), now(), 'seed', null, 'Trade in, get credit in three days', 'Trade in, get credit in three days', null, null, null, null, 'en', -30035, 'TRANSLATED', null, 'Devices packed for trade-in', 'Get a quote', 'Prepaid box, certified recycling, no haggling.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300070, now(), now(), 'seed', null, 'Reprise créditée en trois jours', 'Reprise créditée en trois jours', null, null, null, null, 'fr', -30035, 'TRANSLATED', null, 'Appareils emballés pour reprise', 'Obtenir une estimation', 'Carton prépayé, recyclage certifié, sans marchandage.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-3009, -390006, 'CONTENT', '-30035', 'Trade in, get credit in three days', -30035, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: black-friday-teaser (CAROUSEL)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30036, now(), now(), 'seed', 'black-friday-teaser', 'BANNER', 6, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'CAROUSEL', now() + interval '14 days', now() + interval '32 days', null, '{"target": {"kind": "PAGE", "value": "black-friday"}, "artwork": {"desktopMediaId": -390007, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 55, "alignment": "center"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300071, now(), now(), 'seed', null, 'Black Friday (scheduled)', 'Black Friday (scheduled)', null, null, null, null, 'en', -30036, 'TRANSLATED', null, 'Black Friday artwork', 'Coming soon', 'A windowed banner: it only goes live in two weeks.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300072, now(), now(), 'seed', null, 'Black Friday (programmée)', 'Black Friday (programmée)', null, null, null, null, 'fr', -30036, 'TRANSLATED', null, 'Visuel Black Friday', 'Bientôt', 'Bannière fenêtrée : visible dans deux semaines.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-3010, -390007, 'CONTENT', '-30036', 'Black Friday (scheduled)', -30036, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;


-- the five slides the store shipped with, given real copy in both of its languages
UPDATE content.content_description
   SET language_code = 'en', name = 'Two years on everything', title = 'Two years on everything', subtitle = 'A year longer than the manufacturer, honoured at our own bench.', cta_label = 'Read the warranty',
       alt_text = 'A repair bench with a laptop open on it', date_modified = now()
 WHERE description_id = -3200;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300073, now(), now(), 'seed', null, 'Deux ans sur tout', 'Deux ans sur tout', null, null, null, null, 'fr', -3100, 'TRANSLATED', null, 'Un établi de réparation avec un portable ouvert', 'Lire la garantie', 'Un an de plus que le constructeur, assuré dans notre atelier.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'en', name = 'Tested before it ships', title = 'Tested before it ships', subtitle = 'Thirty minutes of sustained load, not a thirty-second benchmark.', cta_label = 'How we choose',
       alt_text = 'Thermal probes attached to a laptop chassis', date_modified = now()
 WHERE description_id = -3201;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300074, now(), now(), 'seed', null, 'Testé avant expédition', 'Testé avant expédition', null, null, null, null, 'fr', -3101, 'TRANSLATED', null, 'Sondes thermiques fixées sur un châssis de portable', 'Comment nous choisissons', 'Trente minutes de charge continue, pas trente secondes de benchmark.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'en', name = 'Trade in, credited in three days', title = 'Trade in, credited in three days', subtitle = 'Prepaid box, certified recycling, no haggling.', cta_label = 'Get a quote',
       alt_text = 'Devices packed in a padded trade-in box', date_modified = now()
 WHERE description_id = -3202;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300075, now(), now(), 'seed', null, 'Reprise créditée en trois jours', 'Reprise créditée en trois jours', null, null, null, null, 'fr', -3102, 'TRANSLATED', null, 'Appareils emballés dans un carton de reprise', 'Obtenir une estimation', 'Carton prépayé, recyclage certifié, sans marchandage.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'en', name = 'Gaming, latency measured', title = 'Gaming, latency measured', subtitle = 'Every display checked in game mode before it leaves.', cta_label = 'Shop gaming',
       alt_text = 'A gaming setup lit in blue', date_modified = now()
 WHERE description_id = -3203;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300076, now(), now(), 'seed', null, 'Gaming, latence mesurée', 'Gaming, latence mesurée', null, null, null, null, 'fr', -3103, 'TRANSLATED', null, 'Un poste de jeu éclairé en bleu', 'Voir le gaming', 'Chaque écran vérifié en mode jeu avant de partir.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'en', name = 'Free shipping over $75', title = 'Free shipping over $75', subtitle = 'Same-day dispatch on orders before 3pm Eastern.', cta_label = 'See the rates',
       alt_text = 'Parcels on a warehouse conveyor', date_modified = now()
 WHERE description_id = -3204;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300077, now(), now(), 'seed', null, 'Livraison offerte dès 75 $', 'Livraison offerte dès 75 $', null, null, null, null, 'fr', -3104, 'TRANSLATED', null, 'Colis sur un convoyeur d’entrepôt', 'Voir les tarifs', 'Expédition le jour même avant 15h heure de l’Est.')
on conflict (description_id) do nothing;

-- Banners: the hero, the collection promos and a windowed slide.
--
-- Demo content for the test store 65f020632bc46470c104b76f (Beauté Élégante Paris), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- banner: hero-eclat-quotidien (HERO)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10033, now(), now(), 'seed', 'hero-eclat-quotidien', 'BANNER', 0, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'HERO', null, null, null, '{"target": {"kind": "COLLECTION", "value": "soins-peau"}, "artwork": {"desktopMediaId": -190004, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#1b1b1b", "overlayOpacity": 25, "alignment": "left"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100065, now(), now(), 'seed', null, 'L''éclat, tous les jours', 'L''éclat, tous les jours', null, null, null, null, 'fr', -10033, 'TRANSLATED', null, 'Flacons de sérum posés sur un plateau de marbre', 'Découvrir les soins', 'La routine soin en trois gestes, formulée à Nantes.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100066, now(), now(), 'seed', null, 'Radiance, every day', 'Radiance, every day', null, null, null, null, 'en', -10033, 'TRANSLATED', null, 'Serum bottles arranged on a marble tray', 'Explore skincare', 'A three-step skincare routine, formulated in Nantes.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-1007, -190004, 'CONTENT', '-10033', 'L''éclat, tous les jours', -10033, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: collection-parfums (COLLECTION)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10034, now(), now(), 'seed', 'collection-parfums', 'BANNER', 1, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'COLLECTION', null, null, null, '{"target": {"kind": "COLLECTION", "value": "parfums"}, "artwork": {"desktopMediaId": -190005, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 40, "alignment": "center"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100067, now(), now(), 'seed', null, 'Parfums de niche', 'Parfums de niche', null, null, null, null, 'fr', -10034, 'TRANSLATED', null, 'Flacon de parfum sur fond sombre', 'Voir les parfums', 'Douze compositions, aucune tête d''affiche.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100068, now(), now(), 'seed', null, 'Niche fragrance', 'Niche fragrance', null, null, null, null, 'en', -10034, 'TRANSLATED', null, 'A perfume bottle against a dark background', 'See fragrances', 'Twelve compositions, no headline act.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-1008, -190005, 'CONTENT', '-10034', 'Parfums de niche', -10034, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: edit-printemps (CAROUSEL)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10035, now(), now(), 'seed', 'edit-printemps', 'BANNER', 5, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'CAROUSEL', null, null, null, '{"target": {"kind": "PAGE", "value": "nos-ingredients"}, "artwork": {"desktopMediaId": -190006, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 30, "alignment": "left"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100069, now(), now(), 'seed', null, 'L''édit du printemps', 'L''édit du printemps', null, null, null, null, 'fr', -10035, 'TRANSLATED', null, 'Composition de produits de soin printaniers', 'Lire la charte', 'Textures légères et fleurs blanches.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100070, now(), now(), 'seed', null, 'The spring edit', 'The spring edit', null, null, null, null, 'en', -10035, 'TRANSLATED', null, 'A spring arrangement of skincare products', 'Read the charter', 'Lighter textures and white florals.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-1009, -190006, 'CONTENT', '-10035', 'L''édit du printemps', -10035, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: ventes-privees-teaser (CAROUSEL)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10036, now(), now(), 'seed', 'ventes-privees-teaser', 'BANNER', 6, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'CAROUSEL', now() + interval '14 days', now() + interval '28 days', null, '{"target": {"kind": "PAGE", "value": "soldes-privees"}, "artwork": {"desktopMediaId": -190007, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 45, "alignment": "center"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100071, now(), now(), 'seed', null, 'Ventes privées (programmée)', 'Ventes privées (programmée)', null, null, null, null, 'fr', -10036, 'TRANSLATED', null, 'Visuel des ventes privées', 'Bientôt', 'Bannière fenêtrée : visible seulement dans deux semaines.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100072, now(), now(), 'seed', null, 'Private sale (scheduled)', 'Private sale (scheduled)', null, null, null, null, 'en', -10036, 'TRANSLATED', null, 'Private sale artwork', 'Coming soon', 'A windowed banner: it only goes live in two weeks.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-1010, -190007, 'CONTENT', '-10036', 'Ventes privées (programmée)', -10036, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;


-- the five slides the store shipped with, given real copy in both of its languages
UPDATE content.content_description
   SET language_code = 'fr', name = 'Nouveau : sérum rétinal 0,05 %', title = 'Nouveau : sérum rétinal 0,05 %', subtitle = 'Le geste du soir, dosé pour être tenu.', cta_label = 'Découvrir',
       alt_text = 'Flacon de sérum rétinal sur fond crème', date_modified = now()
 WHERE description_id = -1200;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100073, now(), now(), 'seed', null, 'New: 0.05% retinal serum', 'New: 0.05% retinal serum', null, null, null, null, 'en', -1100, 'TRANSLATED', null, 'A retinal serum bottle on a cream background', 'Discover', 'The evening step, dosed to be kept up.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'fr', name = 'Recharges en verre consigné', title = 'Recharges en verre consigné', subtitle = '3 € rendus en boutique pour chaque flacon rapporté.', cta_label = 'Comment ça marche',
       alt_text = 'Flacons de recharge alignés sur une étagère', date_modified = now()
 WHERE description_id = -1201;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100074, now(), now(), 'seed', null, 'Returnable glass refills', 'Returnable glass refills', null, null, null, null, 'en', -1101, 'TRANSLATED', null, 'Refill bottles lined up on a shelf', 'How it works', '€3 back in the boutique for every bottle returned.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'fr', name = 'Diagnostic de peau offert', title = 'Diagnostic de peau offert', subtitle = 'Vingt minutes en boutique, sans achat obligatoire.', cta_label = 'Prendre rendez-vous',
       alt_text = 'Conseillère réalisant un diagnostic de peau', date_modified = now()
 WHERE description_id = -1202;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100075, now(), now(), 'seed', null, 'Free skin diagnosis', 'Free skin diagnosis', null, null, null, null, 'en', -1102, 'TRANSLATED', null, 'An advisor carrying out a skin diagnosis', 'Book a slot', 'Twenty minutes in the boutique, with no obligation to buy.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'fr', name = 'Parfums de niche', title = 'Parfums de niche', subtitle = 'Douze compositions, aucune tête d’affiche.', cta_label = 'Voir les parfums',
       alt_text = 'Flacons de parfum sur un plateau de marbre', date_modified = now()
 WHERE description_id = -1203;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100076, now(), now(), 'seed', null, 'Niche fragrance', 'Niche fragrance', null, null, null, null, 'en', -1103, 'TRANSLATED', null, 'Perfume bottles on a marble tray', 'See fragrances', 'Twelve compositions, no headline act.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'fr', name = 'Livraison offerte dès 60 €', title = 'Livraison offerte dès 60 €', subtitle = 'Préparée le jour même, expédiée de Paris.', cta_label = 'Voir les conditions',
       alt_text = 'Colis Beauté Élégante prêt à l’expédition', date_modified = now()
 WHERE description_id = -1204;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100077, now(), now(), 'seed', null, 'Free delivery over €60', 'Free delivery over €60', null, null, null, null, 'en', -1104, 'TRANSLATED', null, 'A Beauté Élégante parcel ready to ship', 'See the terms', 'Packed the same day, shipped from Paris.')
on conflict (description_id) do nothing;

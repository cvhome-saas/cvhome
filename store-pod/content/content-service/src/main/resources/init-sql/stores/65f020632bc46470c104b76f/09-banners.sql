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

-- the five slides the store shipped with, given real copy in both of its languages
UPDATE content.content_description
   SET language_code = 'fr', name = 'Nouveau : sérum rétinal 0,05 %', title = 'Nouveau : sérum rétinal 0,05 %', subtitle = 'Le geste du soir, dosé pour être tenu.', cta_label = 'Découvrir',
       alt_text = 'Flacon de sérum rétinal sur fond crème', date_modified = now()
 WHERE description_id = -1200;UPDATE content.content_description
   SET language_code = 'fr', name = 'Recharges en verre consigné', title = 'Recharges en verre consigné', subtitle = '3 € rendus en boutique pour chaque flacon rapporté.', cta_label = 'Comment ça marche',
       alt_text = 'Flacons de recharge alignés sur une étagère', date_modified = now()
 WHERE description_id = -1201;UPDATE content.content_description
   SET language_code = 'fr', name = 'Diagnostic de peau offert', title = 'Diagnostic de peau offert', subtitle = 'Vingt minutes en boutique, sans achat obligatoire.', cta_label = 'Prendre rendez-vous',
       alt_text = 'Conseillère réalisant un diagnostic de peau', date_modified = now()
 WHERE description_id = -1202;UPDATE content.content_description
   SET language_code = 'fr', name = 'Parfums de niche', title = 'Parfums de niche', subtitle = 'Douze compositions, aucune tête d’affiche.', cta_label = 'Voir les parfums',
       alt_text = 'Flacons de parfum sur un plateau de marbre', date_modified = now()
 WHERE description_id = -1203;UPDATE content.content_description
   SET language_code = 'fr', name = 'Livraison offerte dès 60 €', title = 'Livraison offerte dès 60 €', subtitle = 'Préparée le jour même, expédiée de Paris.', cta_label = 'Voir les conditions',
       alt_text = 'Colis Beauté Élégante prêt à l’expédition', date_modified = now()
 WHERE description_id = -1204;
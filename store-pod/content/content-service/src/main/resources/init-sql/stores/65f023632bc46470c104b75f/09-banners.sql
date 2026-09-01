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

-- the five slides the store shipped with, given real copy in both of its languages
UPDATE content.content_description
   SET language_code = 'en', name = 'Two years on everything', title = 'Two years on everything', subtitle = 'A year longer than the manufacturer, honoured at our own bench.', cta_label = 'Read the warranty',
       alt_text = 'A repair bench with a laptop open on it', date_modified = now()
 WHERE description_id = -3200;UPDATE content.content_description
   SET language_code = 'en', name = 'Tested before it ships', title = 'Tested before it ships', subtitle = 'Thirty minutes of sustained load, not a thirty-second benchmark.', cta_label = 'How we choose',
       alt_text = 'Thermal probes attached to a laptop chassis', date_modified = now()
 WHERE description_id = -3201;UPDATE content.content_description
   SET language_code = 'en', name = 'Trade in, credited in three days', title = 'Trade in, credited in three days', subtitle = 'Prepaid box, certified recycling, no haggling.', cta_label = 'Get a quote',
       alt_text = 'Devices packed in a padded trade-in box', date_modified = now()
 WHERE description_id = -3202;UPDATE content.content_description
   SET language_code = 'en', name = 'Gaming, latency measured', title = 'Gaming, latency measured', subtitle = 'Every display checked in game mode before it leaves.', cta_label = 'Shop gaming',
       alt_text = 'A gaming setup lit in blue', date_modified = now()
 WHERE description_id = -3203;UPDATE content.content_description
   SET language_code = 'en', name = 'Free shipping over $75', title = 'Free shipping over $75', subtitle = 'Same-day dispatch on orders before 3pm Eastern.', cta_label = 'See the rates',
       alt_text = 'Parcels on a warehouse conveyor', date_modified = now()
 WHERE description_id = -3204;
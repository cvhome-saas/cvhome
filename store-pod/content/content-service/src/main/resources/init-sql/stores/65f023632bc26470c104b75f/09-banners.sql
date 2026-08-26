-- Banners: the hero, the collection promos and a windowed slide.
--
-- Demo content for the test store 65f023632bc26470c104b75f (Egypt Car Sales), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

-- banner: hero-inspected-and-ready (HERO)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20033, now(), now(), 'seed', 'hero-inspected-and-ready', 'BANNER', 0, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'HERO', null, null, null, '{"target": {"kind": "PAGE", "value": "inspection-report"}, "artwork": {"desktopMediaId": -290004, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 45, "alignment": "left"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200065, now(), now(), 'seed', null, 'مفحوصة على 150 نقطة', 'مفحوصة على 150 نقطة', null, null, null, null, 'ar', -20033, 'TRANSLATED', null, 'سيارة في ورشة الفحص', 'اقرأ التقرير', 'كل سيارة مستعملة، ومعها تقريرها كاملًا.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200066, now(), now(), 'seed', null, 'Inspectée en 150 points', 'Inspectée en 150 points', null, null, null, null, 'fr', -20033, 'TRANSLATED', null, 'Un véhicule sur le pont d''inspection', 'Lire le rapport', 'Chaque occasion, avec son rapport complet.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-2007, -290004, 'CONTENT', '-20033', 'مفحوصة على 150 نقطة', -20033, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: collection-electric (COLLECTION)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20034, now(), now(), 'seed', 'collection-electric', 'BANNER', 1, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'COLLECTION', null, null, null, '{"target": {"kind": "COLLECTION", "value": "voitures-electriques"}, "artwork": {"desktopMediaId": -290005, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 35, "alignment": "center"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200067, now(), now(), 'seed', null, 'كهربائية بالكامل', 'كهربائية بالكامل', null, null, null, null, 'ar', -20034, 'TRANSLATED', null, 'سيارة كهربائية أثناء الشحن', 'تصفّح الكهربائية', 'شاحن منزلي مركّب مجانًا.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200068, now(), now(), 'seed', null, '100 % électrique', '100 % électrique', null, null, null, null, 'fr', -20034, 'TRANSLATED', null, 'Un véhicule électrique en charge', 'Voir les électriques', 'Borne domestique installée gratuitement.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-2008, -290005, 'CONTENT', '-20034', 'كهربائية بالكامل', -20034, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: trade-in-promo (CAROUSEL)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20035, now(), now(), 'seed', 'trade-in-promo', 'BANNER', 5, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'CAROUSEL', null, null, null, '{"target": {"kind": "PAGE", "value": "trade-in"}, "artwork": {"desktopMediaId": -290006, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 40, "alignment": "left"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200069, now(), now(), 'seed', null, 'استبدل سيارتك اليوم', 'استبدل سيارتك اليوم', null, null, null, null, 'ar', -20035, 'TRANSLATED', null, 'تقييم سيارة في المعرض', 'احجز تقييمًا', 'تقييم مجاني خلال ساعة، وعرض ساري أسبوعًا.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200070, now(), now(), 'seed', null, 'Reprenez votre véhicule aujourd''hui', 'Reprenez votre véhicule aujourd''hui', null, null, null, null, 'fr', -20035, 'TRANSLATED', null, 'Estimation d''un véhicule au showroom', 'Réserver une estimation', 'Estimation gratuite en une heure, offre valable une semaine.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-2009, -290006, 'CONTENT', '-20035', 'استبدل سيارتك اليوم', -20035, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;

-- banner: summer-offers-teaser (CAROUSEL)
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20036, now(), now(), 'seed', 'summer-offers-teaser', 'BANNER', 6, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', null, false, null, null, false, 'CAROUSEL', now() + interval '14 days', now() + interval '30 days', null, '{"target": {"kind": "PAGE", "value": "summer-offers"}, "artwork": {"desktopMediaId": -290007, "mobileMediaId": null, "mobileCrop": null}, "theme": {"textColor": "#ffffff", "overlayOpacity": 50, "alignment": "center"}, "loggedInOnly": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200071, now(), now(), 'seed', null, 'عروض الصيف (مجدولة)', 'عروض الصيف (مجدولة)', null, null, null, null, 'ar', -20036, 'TRANSLATED', null, 'تصميم عروض الصيف', 'قريبًا', 'بانر بنافذة زمنية: يظهر بعد أسبوعين فقط.')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200072, now(), now(), 'seed', null, 'Offres d''été (programmée)', 'Offres d''été (programmée)', null, null, null, null, 'fr', -20036, 'TRANSLATED', null, 'Visuel des offres d''été', 'Bientôt', 'Bannière fenêtrée : visible dans deux semaines.')
on conflict (description_id) do nothing;
INSERT INTO content.media_usage (id, asset_id, owner_kind, owner_ref, owner_title, content_id, content_type, field)
VALUES (-2010, -290007, 'CONTENT', '-20036', 'عروض الصيف (مجدولة)', -20036, 'BANNER', 'artwork.desktop')
on conflict (id) do nothing;


-- the five slides the store shipped with, given real copy in both of its languages
UPDATE content.content_description
   SET language_code = 'ar', name = 'مفحوصة على 150 نقطة', title = 'مفحوصة على 150 نقطة', subtitle = 'كل سيارة مستعملة، ومعها تقريرها كاملًا.', cta_label = 'اقرأ التقرير',
       alt_text = 'سيارة على رافعة الفحص', date_modified = now()
 WHERE description_id = -2200;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200073, now(), now(), 'seed', null, 'Inspectée en 150 points', 'Inspectée en 150 points', null, null, null, null, 'fr', -2100, 'TRANSLATED', null, 'Un véhicule sur le pont d’inspection', 'Lire le rapport', 'Chaque occasion, avec son rapport complet.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'ar', name = 'تقسيط حتى 60 شهرًا', title = 'تقسيط حتى 60 شهرًا', subtitle = 'مقدم من 20%، وموافقة مبدئية خلال 48 ساعة.', cta_label = 'احسب قسطك',
       alt_text = 'توقيع عقد تمويل في المعرض', date_modified = now()
 WHERE description_id = -2201;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200074, now(), now(), 'seed', null, 'Financement jusqu’à 60 mois', 'Financement jusqu’à 60 mois', null, null, null, null, 'fr', -2101, 'TRANSLATED', null, 'Signature d’un contrat de financement au showroom', 'Calculer une mensualité', 'Apport dès 20 %, accord de principe sous 48 heures.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'ar', name = 'كهربائية بالكامل', title = 'كهربائية بالكامل', subtitle = 'شاحن منزلي 7 كيلوواط مركّب مجانًا.', cta_label = 'تصفّح الكهربائية',
       alt_text = 'سيارة كهربائية أثناء الشحن', date_modified = now()
 WHERE description_id = -2202;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200075, now(), now(), 'seed', null, '100 % électrique', '100 % électrique', null, null, null, null, 'fr', -2102, 'TRANSLATED', null, 'Un véhicule électrique en charge', 'Voir les électriques', 'Borne domestique 7 kW installée gratuitement.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'ar', name = 'استبدل سيارتك اليوم', title = 'استبدل سيارتك اليوم', subtitle = 'تقييم مجاني خلال ساعة، وعرض ساري أسبوعًا.', cta_label = 'احجز تقييمًا',
       alt_text = 'تقييم سيارة أمام المعرض', date_modified = now()
 WHERE description_id = -2203;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200076, now(), now(), 'seed', null, 'Reprenez votre véhicule', 'Reprenez votre véhicule', null, null, null, null, 'fr', -2103, 'TRANSLATED', null, 'Estimation d’un véhicule devant le showroom', 'Réserver une estimation', 'Estimation gratuite en une heure, offre valable une semaine.')
on conflict (description_id) do nothing;
UPDATE content.content_description
   SET language_code = 'ar', name = 'الترخيص علينا', title = 'الترخيص علينا', subtitle = 'رسوم المرور والفحص مشمولة في السعر المعلن.', cta_label = 'التفاصيل',
       alt_text = 'لوحات معدنية جديدة على مكتب', date_modified = now()
 WHERE description_id = -2204;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200077, now(), now(), 'seed', null, 'L’immatriculation est à notre charge', 'L’immatriculation est à notre charge', null, null, null, null, 'fr', -2104, 'TRANSLATED', null, 'Des plaques neuves posées sur un bureau', 'En savoir plus', 'Frais de police et de contrôle inclus dans le prix affiché.')
on conflict (description_id) do nothing;

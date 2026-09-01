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

-- the five slides the store shipped with, given real copy in both of its languages
UPDATE content.content_description
   SET language_code = 'ar', name = 'مفحوصة على 150 نقطة', title = 'مفحوصة على 150 نقطة', subtitle = 'كل سيارة مستعملة، ومعها تقريرها كاملًا.', cta_label = 'اقرأ التقرير',
       alt_text = 'سيارة على رافعة الفحص', date_modified = now()
 WHERE description_id = -2200;UPDATE content.content_description
   SET language_code = 'ar', name = 'تقسيط حتى 60 شهرًا', title = 'تقسيط حتى 60 شهرًا', subtitle = 'مقدم من 20%، وموافقة مبدئية خلال 48 ساعة.', cta_label = 'احسب قسطك',
       alt_text = 'توقيع عقد تمويل في المعرض', date_modified = now()
 WHERE description_id = -2201;UPDATE content.content_description
   SET language_code = 'ar', name = 'كهربائية بالكامل', title = 'كهربائية بالكامل', subtitle = 'شاحن منزلي 7 كيلوواط مركّب مجانًا.', cta_label = 'تصفّح الكهربائية',
       alt_text = 'سيارة كهربائية أثناء الشحن', date_modified = now()
 WHERE description_id = -2202;UPDATE content.content_description
   SET language_code = 'ar', name = 'استبدل سيارتك اليوم', title = 'استبدل سيارتك اليوم', subtitle = 'تقييم مجاني خلال ساعة، وعرض ساري أسبوعًا.', cta_label = 'احجز تقييمًا',
       alt_text = 'تقييم سيارة أمام المعرض', date_modified = now()
 WHERE description_id = -2203;UPDATE content.content_description
   SET language_code = 'ar', name = 'الترخيص علينا', title = 'الترخيص علينا', subtitle = 'رسوم المرور والفحص مشمولة في السعر المعلن.', cta_label = 'التفاصيل',
       alt_text = 'لوحات معدنية جديدة على مكتب', date_modified = now()
 WHERE description_id = -2204;
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

-- the five slides the store shipped with, given real copy in both of its languages
UPDATE content.content_description
   SET language_code = 'ar', name = 'موسم الكتان', title = 'موسم الكتان', subtitle = 'أقمشة تسمح بمرور الهواء، وقَصّات لا تلتصق.', cta_label = 'تعرّف على أقمشتنا',
       alt_text = 'قمصان كتان معلّقة على رف خشبي', date_modified = now()
 WHERE description_id = -4200;UPDATE content.content_description
   SET language_code = 'ar', name = 'التعديل مجانًا', title = 'التعديل مجانًا', subtitle = 'على أي قطعة من المعرض، جاهزة خلال ثلاثة أيام.', cta_label = 'مواعيد المعارض',
       alt_text = 'خياط يعدّل قميصًا في غرفة التعديل', date_modified = now()
 WHERE description_id = -4201;UPDATE content.content_description
   SET language_code = 'ar', name = 'مجموعة النساء', title = 'مجموعة النساء', subtitle = 'قَصّات مستقيمة وألوان محايدة.', cta_label = 'تسوّقي المجموعة',
       alt_text = 'إطلالة نسائية بألوان محايدة', date_modified = now()
 WHERE description_id = -4202;UPDATE content.content_description
   SET language_code = 'ar', name = 'المقاس الصحيح من أول مرة', title = 'المقاس الصحيح من أول مرة', subtitle = 'جداول بالسنتيمتر، وطريقة القياس بالتفصيل.', cta_label = 'دليل المقاسات',
       alt_text = 'شريط قياس على طاولة قص', date_modified = now()
 WHERE description_id = -4203;UPDATE content.content_description
   SET language_code = 'ar', name = 'توصيل خلال يوم داخل الرياض', title = 'توصيل خلال يوم داخل الرياض', subtitle = 'مجانًا للطلبات فوق 200 ريال.', cta_label = 'سياسة الشحن',
       alt_text = 'مندوب توصيل يحمل طردًا', date_modified = now()
 WHERE description_id = -4204;
-- FAQ groups and entries.
--
-- Demo content for the test store 65f023632bc46470c104b76f (Riyadh Fashion Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-401, '65f023632bc46470c104b76f', 'general', 0, '{"ar": "أسئلة عامة", "en": "General"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-402, '65f023632bc46470c104b76f', 'ordering', 1, '{"ar": "الطلب والدفع", "en": "Ordering & payment"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-403, '65f023632bc46470c104b76f', 'shipping', 2, '{"ar": "الشحن والتوصيل", "en": "Shipping & delivery"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-404, '65f023632bc46470c104b76f', 'returns', 3, '{"ar": "الاستبدال والاسترجاع", "en": "Returns & exchanges"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;

-- faq: general / find-my-size
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40014, now(), now(), 'seed', 'find-my-size', 'FAQ', 0, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["كيف", "أعرف", "مقاسي", "الصحيح؟"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400027, now(), now(), 'seed', '<p>قِس صدرك وخصرك وأردافك وقارنها بجدول المقاسات، لا بالمقاس الذي اعتدته من علامة أخرى. كل صفحة منتج تذكر أيضًا إن كان القَصّ ضيقًا أو واسعًا.</p>', 'كيف أعرف مقاسي الصحيح؟', 'كيف أعرف مقاسي الصحيح؟', null, null, null, null, 'ar', -40014, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400028, now(), now(), 'seed', '<p>Measure bust, waist and hips and compare with the size chart rather than with the size you take at another brand. Each product page also states whether the cut runs slim or relaxed.</p>', 'How do I find my size?', 'How do I find my size?', null, null, null, null, 'en', -40014, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: general / alterations
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40015, now(), now(), 'seed', 'alterations', 'FAQ', 1, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["هل", "تقدّمون", "خدمة", "التعديل؟"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400029, now(), now(), 'seed', '<p>نعم، ومجانًا على أي قطعة تُشترى من المعرض: تقصير البناطيل وتضييق الأكتاف، والتسليم خلال ثلاثة أيام.</p>', 'هل تقدّمون خدمة التعديل؟', 'هل تقدّمون خدمة التعديل؟', null, null, null, null, 'ar', -40015, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400030, now(), now(), 'seed', '<p>Yes, free on anything bought in store: hems and shoulder taper, ready within three days.</p>', 'Do you offer alterations?', 'Do you offer alterations?', null, null, null, null, 'en', -40015, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: general / restock
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40016, now(), now(), 'seed', 'restock', 'FAQ', 2, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["هل", "تعود", "القطع", "النافدة؟"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400031, now(), now(), 'seed', '<p>القطع الأساسية نعم، خلال ثلاثة إلى ستة أسابيع. أما قطع المجموعات الموسمية فلا تُعاد طباعتها. اضغط « أبلغني » على صفحة المنتج.</p>', 'هل تعود القطع النافدة؟', 'هل تعود القطع النافدة؟', null, null, null, null, 'ar', -40016, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400032, now(), now(), 'seed', '<p>Core pieces do, within three to six weeks. Seasonal collection pieces are not reprinted. Use “Notify me” on the product page.</p>', 'Do sold-out pieces come back?', 'Do sold-out pieces come back?', null, null, null, null, 'en', -40016, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / payment-methods
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40017, now(), now(), 'seed', 'payment-methods', 'FAQ', 0, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["ما", "وسائل", "الدفع", "المتاحة؟"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400033, now(), now(), 'seed', '<p>مدى وفيزا وماستركارد وApple Pay وتابي وتمارا للتقسيط. الدفع يُعالَج عبر Stripe، ولا نحفظ أرقام البطاقات لدينا.</p>', 'ما وسائل الدفع المتاحة؟', 'ما وسائل الدفع المتاحة؟', null, null, null, null, 'ar', -40017, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400034, now(), now(), 'seed', '<p>Mada, Visa, Mastercard, Apple Pay, and Tabby or Tamara for instalments. Payment is processed by Stripe; we never store card numbers.</p>', 'Which payment methods can I use?', 'Which payment methods can I use?', null, null, null, null, 'en', -40017, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / cash-on-delivery
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40018, now(), now(), 'seed', 'cash-on-delivery', 'FAQ', 1, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["هل", "الدفع", "عند", "الاستلام"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400035, now(), now(), 'seed', '<p>نعم داخل المملكة، برسوم 15 ريالًا، وبحد أقصى 2000 ريال للطلب الواحد.</p>', 'هل الدفع عند الاستلام متاح؟', 'هل الدفع عند الاستلام متاح؟', null, null, null, null, 'ar', -40018, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400036, now(), now(), 'seed', '<p>Yes inside the Kingdom, for a SAR 15 fee, up to SAR 2,000 per order.</p>', 'Is cash on delivery available?', 'Is cash on delivery available?', null, null, null, null, 'en', -40018, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / modify-order
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40019, now(), now(), 'seed', 'modify-order', 'FAQ', 2, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["هل", "أستطيع", "تعديل", "طلبي"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400037, now(), now(), 'seed', '<p>ما دام الطلب لم يدخل التجهيز — عادة خلال ساعتين — يمكننا تغيير المقاس أو العنوان. بعدها يلزم الاستلام ثم الاسترجاع المجاني.</p>', 'هل أستطيع تعديل طلبي بعد تأكيده؟', 'هل أستطيع تعديل طلبي بعد تأكيده؟', null, null, null, null, 'ar', -40019, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400038, now(), now(), 'seed', '<p>As long as it has not entered packing — usually within two hours — we can change a size or an address. After that, it has to arrive and go back as a free return.</p>', 'Can I change my order after confirming?', 'Can I change my order after confirming?', null, null, null, null, 'en', -40019, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / delivery-times
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40020, now(), now(), 'seed', 'delivery-times', 'FAQ', 0, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["كم", "يستغرق", "التوصيل؟"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400039, now(), now(), 'seed', '<p>يوم واحد داخل الرياض، ويومان إلى ثلاثة لجدة والدمام ومكة، وثلاثة إلى خمسة لبقية المناطق.</p>', 'كم يستغرق التوصيل؟', 'كم يستغرق التوصيل؟', null, null, null, null, 'ar', -40020, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400040, now(), now(), 'seed', '<p>One day inside Riyadh, two to three days for Jeddah, Dammam and Makkah, three to five for the rest of the Kingdom.</p>', 'How long does delivery take?', 'How long does delivery take?', null, null, null, null, 'en', -40020, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / gcc-shipping
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40021, now(), now(), 'seed', 'gcc-shipping', 'FAQ', 1, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["هل", "تشحنون", "خارج", "المملكة؟"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400041, now(), now(), 'seed', '<p>إلى دول مجلس التعاون الخليجي فقط، خلال خمسة إلى ثمانية أيام. الرسوم الجمركية على المستلم.</p>', 'هل تشحنون خارج المملكة؟', 'هل تشحنون خارج المملكة؟', null, null, null, null, 'ar', -40021, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400042, now(), now(), 'seed', '<p>To the GCC states only, in five to eight days. Customs duties are payable by the recipient.</p>', 'Do you ship outside Saudi Arabia?', 'Do you ship outside Saudi Arabia?', null, null, null, null, 'en', -40021, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / same-day-riyadh
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40022, now(), now(), 'seed', 'same-day-riyadh', 'FAQ', 2, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["هل", "يوجد", "توصيل", "في"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400043, now(), now(), 'seed', '<p>نعم داخل الرياض للطلبات المؤكدة قبل الظهر، مقابل 35 ريالًا.</p>', 'هل يوجد توصيل في نفس اليوم؟', 'هل يوجد توصيل في نفس اليوم؟', null, null, null, null, 'ar', -40022, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400044, now(), now(), 'seed', '<p>Yes inside Riyadh, for orders confirmed before noon, at SAR 35.</p>', 'Is same-day delivery available?', 'Is same-day delivery available?', null, null, null, null, 'en', -40022, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / return-window
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40023, now(), now(), 'seed', 'return-window', 'FAQ', 0, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["كم", "مدة", "الاسترجاع؟"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400045, now(), now(), 'seed', '<p>ثلاثون يومًا من الاستلام، والقطعة غير مستعملة وبطاقتها مثبّتة. الاسترجاع مجاني.</p>', 'كم مدة الاسترجاع؟', 'كم مدة الاسترجاع؟', null, null, null, null, 'ar', -40023, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400046, now(), now(), 'seed', '<p>Thirty days from delivery, unworn and with the tag attached. Returns are free.</p>', 'How long is the return window?', 'How long is the return window?', null, null, null, null, 'en', -40023, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / exchange-size
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40024, now(), now(), 'seed', 'exchange-size', 'FAQ', 1, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["كيف", "أستبدل", "مقاسًا؟"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400047, now(), now(), 'seed', '<p>اطلب استبدالًا من حسابك؛ نحجز المقاس الجديد فورًا ونشحنه بمجرد استلام القطعة الأولى في المستودع.</p>', 'كيف أستبدل مقاسًا؟', 'كيف أستبدل مقاسًا؟', null, null, null, null, 'ar', -40024, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400048, now(), now(), 'seed', '<p>Request an exchange from your account; we hold the new size immediately and ship it as soon as the first piece reaches the warehouse.</p>', 'How do I exchange for another size?', 'How do I exchange for another size?', null, null, null, null, 'en', -40024, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / refund-timing
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-40025, now(), now(), 'seed', 'refund-timing', 'FAQ', 2, true, '65f023632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b76f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["متى", "يصل", "الاسترداد؟"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400049, now(), now(), 'seed', '<p>خلال خمسة أيام عمل من وصول القطعة، بنفس وسيلة الدفع. ظهوره في كشف الحساب يعتمد على بنكك.</p>', 'متى يصل الاسترداد؟', 'متى يصل الاسترداد؟', null, null, null, null, 'ar', -40025, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-400050, now(), now(), 'seed', '<p>Within five working days of the item arriving, to the original payment method. How quickly it appears on your statement depends on your bank.</p>', 'When does the refund arrive?', 'When does the refund arrive?', null, null, null, null, 'en', -40025, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;


-- FAQ groups and entries.
--
-- Demo content for the test store 65f023632bc26470c104b75f (Egypt Car Sales), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-201, '65f023632bc26470c104b75f', 'general', 0, '{"ar": "أسئلة عامة", "fr": "Questions générales"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-202, '65f023632bc26470c104b75f', 'ordering', 1, '{"ar": "الشراء والتمويل", "fr": "Achat et financement"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-203, '65f023632bc26470c104b75f', 'shipping', 2, '{"ar": "التسليم والترخيص", "fr": "Livraison et immatriculation"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-204, '65f023632bc26470c104b75f', 'returns', 3, '{"ar": "الضمان والإرجاع", "fr": "Garantie et retour"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;

-- faq: general / test-drive
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20014, now(), now(), 'seed', 'test-drive', 'FAQ', 0, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["هل", "يمكنني", "تجربة", "السيارة"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200027, now(), now(), 'seed', '<p>نعم، ومجانًا. احجز موعدًا عبر الموقع أو بالهاتف، وأحضر رخصة قيادة سارية. مدة التجربة عشرون دقيقة على مسار يشمل طريقًا سريعًا.</p>', 'هل يمكنني تجربة السيارة قبل الشراء؟', 'هل يمكنني تجربة السيارة قبل الشراء؟', null, null, null, null, 'ar', -20014, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200028, now(), now(), 'seed', '<p>Oui, et c''est gratuit. Réservez un créneau en ligne ou par téléphone et présentez un permis en cours de validité. L''essai dure vingt minutes sur un parcours incluant de la voie rapide.</p>', 'Puis-je essayer la voiture avant d''acheter ?', 'Puis-je essayer la voiture avant d''acheter ?', null, null, null, null, 'fr', -20014, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: general / inspection-independent
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20015, now(), now(), 'seed', 'inspection-independent', 'FAQ', 1, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["هل", "أستطيع", "فحص", "السيارة"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200029, now(), now(), 'seed', '<p>بالتأكيد. يمكنك اصطحاب فني تثق به إلى المعرض، أو أخذ السيارة إلى مركز فحص خارجي بعد توقيع إقرار بسيط.</p>', 'هل أستطيع فحص السيارة عند فني من عندي؟', 'هل أستطيع فحص السيارة عند فني من عندي؟', null, null, null, null, 'ar', -20015, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200030, now(), now(), 'seed', '<p>Bien sûr. Vous pouvez venir accompagné au showroom, ou emmener le véhicule dans un centre de contrôle indépendant après signature d''une simple décharge.</p>', 'Puis-je faire expertiser la voiture par mon propre mécanicien ?', 'Puis-je faire expertiser la voiture par mon propre mécanicien ?', null, null, null, null, 'fr', -20015, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: general / car-history
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20016, now(), now(), 'seed', 'car-history', 'FAQ', 2, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["كيف", "أعرف", "تاريخ", "السيارة"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200031, now(), now(), 'seed', '<p>كل سيارة مستعملة معها تقرير فحص من 150 نقطة وسجل الملكيات السابقة. نعرض الاثنين قبل التوقيع، لا بعده.</p>', 'كيف أعرف تاريخ السيارة المستعملة؟', 'كيف أعرف تاريخ السيارة المستعملة؟', null, null, null, null, 'ar', -20016, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200032, now(), now(), 'seed', '<p>Chaque véhicule d''occasion est accompagné du rapport en 150 points et de l''historique des propriétaires. Les deux sont présentés avant la signature, pas après.</p>', 'Comment connaître l''historique d''une occasion ?', 'Comment connaître l''historique d''une occasion ?', null, null, null, null, 'fr', -20016, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / payment-methods
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20017, now(), now(), 'seed', 'payment-methods', 'FAQ', 0, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["ما", "وسائل", "الدفع", "المقبولة؟"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200033, now(), now(), 'seed', '<p>تحويل بنكي، شيك مقبول الدفع، بطاقة ائتمان حتى 200 ألف جنيه، أو تمويل عبر أحد بنوكنا الشريكة. لا نقبل مبالغ نقدية تتجاوز الحد القانوني.</p>', 'ما وسائل الدفع المقبولة؟', 'ما وسائل الدفع المقبولة؟', null, null, null, null, 'ar', -20017, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200034, now(), now(), 'seed', '<p>Virement bancaire, chèque certifié, carte de crédit jusqu''à 200 000 EGP, ou financement via l''une de nos banques partenaires. Nous n''acceptons pas d''espèces au-delà du plafond légal.</p>', 'Quels moyens de paiement acceptez-vous ?', 'Quels moyens de paiement acceptez-vous ?', null, null, null, null, 'fr', -20017, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / reserve-car
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20018, now(), now(), 'seed', 'reserve-car', 'FAQ', 1, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["كيف", "أحجز", "سيارة", "معروضة؟"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200035, now(), now(), 'seed', '<p>بمقدم 10% يُرد بالكامل خلال 72 ساعة إن غيّرت رأيك. الحجز يوقف عرض السيارة لأسبوع.</p>', 'كيف أحجز سيارة معروضة؟', 'كيف أحجز سيارة معروضة؟', null, null, null, null, 'ar', -20018, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200036, now(), now(), 'seed', '<p>Avec un acompte de 10 %, intégralement remboursable sous 72 heures si vous changez d''avis. La réservation retire le véhicule de la vente pendant une semaine.</p>', 'Comment réserver un véhicule ?', 'Comment réserver un véhicule ?', null, null, null, null, 'fr', -20018, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / financing-approval
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20019, now(), now(), 'seed', 'financing-approval', 'FAQ', 2, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["كم", "يستغرق", "قبول", "التمويل؟"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200037, now(), now(), 'seed', '<p>الموافقة المبدئية خلال 48 ساعة عمل بعد استلام المستندات كاملة، والنهائية بعد معاينة البنك.</p>', 'كم يستغرق قبول التمويل؟', 'كم يستغرق قبول التمويل؟', null, null, null, null, 'ar', -20019, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200038, now(), now(), 'seed', '<p>Un accord de principe sous 48 heures ouvrées une fois le dossier complet, l''accord définitif après vérification bancaire.</p>', 'Combien de temps pour un accord de financement ?', 'Combien de temps pour un accord de financement ?', null, null, null, null, 'fr', -20019, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / registration
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20020, now(), now(), 'seed', 'registration', 'FAQ', 0, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["من", "يتولى", "إجراءات", "الترخيص؟"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200039, now(), now(), 'seed', '<p>نحن. رسوم المرور والفحص وشهادة البيانات مشمولة في السعر المعلن، ويستغرق الأمر من ثلاثة إلى خمسة أيام عمل.</p>', 'من يتولى إجراءات الترخيص؟', 'من يتولى إجراءات الترخيص؟', null, null, null, null, 'ar', -20020, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200040, now(), now(), 'seed', '<p>Nous. Les frais de police, de contrôle et de certificat sont inclus dans le prix affiché ; comptez trois à cinq jours ouvrés.</p>', 'Qui s''occupe de l''immatriculation ?', 'Qui s''occupe de l''immatriculation ?', null, null, null, null, 'fr', -20020, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / delivery-outside-cairo
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20021, now(), now(), 'seed', 'delivery-outside-cairo', 'FAQ', 1, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["هل", "تسلّمون", "خارج", "القاهرة؟"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200041, now(), now(), 'seed', '<p>نعم، إلى كل المحافظات. التسليم داخل القاهرة الكبرى مجاني، وخارجها برسوم تبدأ من 1500 جنيه.</p>', 'هل تسلّمون خارج القاهرة؟', 'هل تسلّمون خارج القاهرة؟', null, null, null, null, 'ar', -20021, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200042, now(), now(), 'seed', '<p>Oui, dans tous les gouvernorats. La livraison est offerte dans le Grand Caire ; ailleurs, elle démarre à 1 500 EGP.</p>', 'Livrez-vous en dehors du Caire ?', 'Livrez-vous en dehors du Caire ?', null, null, null, null, 'fr', -20021, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / plates-timing
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20022, now(), now(), 'seed', 'plates-timing', 'FAQ', 2, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["متى", "أستلم", "اللوحات", "المعدنية؟"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200043, now(), now(), 'seed', '<p>مع السيارة نفسها. لا نسلّم أي سيارة برخصة مؤقتة أو بدون لوحات.</p>', 'متى أستلم اللوحات المعدنية؟', 'متى أستلم اللوحات المعدنية؟', null, null, null, null, 'ar', -20022, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200044, now(), now(), 'seed', '<p>Avec le véhicule. Nous ne livrons jamais une voiture sous carte provisoire ou sans plaques.</p>', 'Quand reçois-je les plaques ?', 'Quand reçois-je les plaques ?', null, null, null, null, 'fr', -20022, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / used-car-warranty
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20023, now(), now(), 'seed', 'used-car-warranty', 'FAQ', 0, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["ما", "ضمان", "السيارة", "المستعملة؟"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200045, now(), now(), 'seed', '<p>ستة أشهر على المحرك وناقل الحركة، وسبعة أيام أو 300 كيلومتر للإرجاع الكامل.</p>', 'ما ضمان السيارة المستعملة؟', 'ما ضمان السيارة المستعملة؟', null, null, null, null, 'ar', -20023, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200046, now(), now(), 'seed', '<p>Six mois sur le moteur et la boîte, et sept jours ou 300 kilomètres pour un retour intégral.</p>', 'Quelle garantie sur une occasion ?', 'Quelle garantie sur une occasion ?', null, null, null, null, 'fr', -20023, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / defect-after-delivery
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20024, now(), now(), 'seed', 'defect-after-delivery', 'FAQ', 1, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["ظهر", "عيب", "بعد", "التسليم،"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200047, now(), now(), 'seed', '<p>اتصل بمركز خدمة المعادي في نفس اليوم. إن كان العيب مخالفًا لتقرير الفحص، الإصلاح على حسابنا بالكامل.</p>', 'ظهر عيب بعد التسليم، ماذا أفعل؟', 'ظهر عيب بعد التسليم، ماذا أفعل؟', null, null, null, null, 'ar', -20024, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200048, now(), now(), 'seed', '<p>Appelez le centre de service de Maadi le jour même. Si le défaut contredit le rapport d''inspection, la réparation est intégralement à notre charge.</p>', 'Un défaut apparaît après la livraison — que faire ?', 'Un défaut apparaît après la livraison — que faire ?', null, null, null, null, 'fr', -20024, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / refund-timing
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-20025, now(), now(), 'seed', 'refund-timing', 'FAQ', 2, true, '65f023632bc26470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc26470c104b75f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["متى", "يُرد", "المقدم؟"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200049, now(), now(), 'seed', '<p>خلال خمسة أيام عمل من طلب الإلغاء، بنفس وسيلة الدفع.</p>', 'متى يُرد المقدم؟', 'متى يُرد المقدم؟', null, null, null, null, 'ar', -20025, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-200050, now(), now(), 'seed', '<p>Sous cinq jours ouvrés à compter de la demande d''annulation, sur le moyen de paiement d''origine.</p>', 'Quand l''acompte est-il remboursé ?', 'Quand l''acompte est-il remboursé ?', null, null, null, null, 'fr', -20025, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;


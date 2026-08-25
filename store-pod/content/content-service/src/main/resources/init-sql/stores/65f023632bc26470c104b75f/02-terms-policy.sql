-- The store's TERMS policy: the checkout agreement reads GET /storefront/policies/TERMS, which
-- Module 13 made the only source (the legacy `agreement` box fallback is gone). Negative ids are
-- deliberate — content ids come from SM_SEQUENCER and only ever grow upward, so seed-only rows
-- below zero can never collide with rows the running service creates.

INSERT INTO content.content (content_id, code, content_type, link_to_menu, sort_order, visible,
                             store_merchant_id, status, version, policy_type, meta)
VALUES (-2, 'terms-of-service', 'POLICY', false, 99, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1, 'TERMS',
        '{"jurisdiction": null, "requiresAcceptance": true, "notifyCustomers": false,
           "displayAt": {"footer": true, "checkout": true, "signup": false}}')
on conflict (content_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description,
                                          name, title, meta_description, meta_keywords, meta_title,
                                          sef_url, content_id, language_code)
VALUES (-3, now(), now(), '<h2>شروط الخدمة</h2><p>بإتمام الطلب في هذا المتجر فإنك توافق على هذه الشروط. يتم تأكيد الطلبات بعد استلام الدفع، وتشمل الأسعار الضرائب المطبقة ما لم يُذكر خلاف ذلك. تُقبل الإرجاعات خلال 14 يومًا للمنتجات غير المستخدمة في عبوتها الأصلية.</p>', 'شروط الخدمة', 'شروط الخدمة', '', '', '', '', -2, 'ar')
on conflict (description_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description,
                                          name, title, meta_description, meta_keywords, meta_title,
                                          sef_url, content_id, language_code)
VALUES (-4, now(), now(), '<h2>Conditions d''utilisation</h2><p>En passant commande sur cette boutique, vous acceptez les présentes conditions. Les commandes sont confirmées à réception du paiement ; les prix incluent les taxes applicables sauf mention contraire. Les retours sont acceptés sous 14 jours pour les articles non utilisés dans leur emballage d''origine.</p>', 'Conditions d''utilisation', 'Conditions d''utilisation', '', '', '', '', -2, 'fr')
on conflict (description_id) do nothing;

INSERT INTO content.policy_version (id, store_merchant_id, content_id, version, status,
                                    effective_from, note, translations, published_at, published_by)
VALUES (-2, '65f023632bc26470c104b75f', -2, 1, 'LIVE', now(), null, '[{"language": "ar", "title": "شروط الخدمة", "body": "<h2>شروط الخدمة</h2><p>بإتمام الطلب في هذا المتجر فإنك توافق على هذه الشروط. يتم تأكيد الطلبات بعد استلام الدفع، وتشمل الأسعار الضرائب المطبقة ما لم يُذكر خلاف ذلك. تُقبل الإرجاعات خلال 14 يومًا للمنتجات غير المستخدمة في عبوتها الأصلية.</p>"}, {"language": "fr", "title": "Conditions d''utilisation", "body": "<h2>Conditions d''utilisation</h2><p>En passant commande sur cette boutique, vous acceptez les présentes conditions. Les commandes sont confirmées à réception du paiement ; les prix incluent les taxes applicables sauf mention contraire. Les retours sont acceptés sous 14 jours pour les articles non utilisés dans leur emballage d''origine.</p>"}]', now(), 'seed')
on conflict (id) do nothing;

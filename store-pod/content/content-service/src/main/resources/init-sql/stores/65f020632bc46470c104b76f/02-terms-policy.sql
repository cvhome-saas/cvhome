-- The store's TERMS policy: the checkout agreement reads GET /storefront/policies/TERMS, which
-- is now the only source (the legacy `agreement` box is gone). Negative ids are
-- deliberate — content ids come from SM_SEQUENCER and only ever grow upward, so seed-only rows
-- below zero can never collide with rows the running service creates.

INSERT INTO content.content (content_id, code, content_type, sort_order, visible,
                             store_merchant_id, status, version, policy_type, meta)
VALUES (-1, 'terms-of-service', 'POLICY', 99, true, '65f020632bc46470c104b76f', 'PUBLISHED', 1, 'TERMS',
        '{"jurisdiction": null, "requiresAcceptance": true, "notifyCustomers": false,
           "displayAt": {"footer": true, "checkout": true, "signup": false}}')
on conflict (content_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description,
                                          name, title, meta_description, meta_keywords, meta_title,
                                          sef_url, content_id, language_code)
VALUES (-1, now(), now(), '<h2>Terms of Service</h2><p>By placing an order on this store you agree to these terms. Orders are confirmed once payment is received; prices include applicable taxes unless stated otherwise. Returns are accepted within 14 days for unused items in their original packaging.</p>', 'Terms of Service', 'Terms of Service', '', '', '', '', -1, 'en')
on conflict (description_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description,
                                          name, title, meta_description, meta_keywords, meta_title,
                                          sef_url, content_id, language_code)
VALUES (-2, now(), now(), '<h2>Conditions d''utilisation</h2><p>En passant commande sur cette boutique, vous acceptez les présentes conditions. Les commandes sont confirmées à réception du paiement ; les prix incluent les taxes applicables sauf mention contraire. Les retours sont acceptés sous 14 jours pour les articles non utilisés dans leur emballage d''origine.</p>', 'Conditions d''utilisation', 'Conditions d''utilisation', '', '', '', '', -1, 'fr')
on conflict (description_id) do nothing;

INSERT INTO content.policy_version (id, store_merchant_id, content_id, version, status,
                                    effective_from, note, translations, published_at, published_by)
VALUES (-1, '65f020632bc46470c104b76f', -1, 1, 'LIVE', now(), null, '[{"language": "en", "title": "Terms of Service", "body": "<h2>Terms of Service</h2><p>By placing an order on this store you agree to these terms. Orders are confirmed once payment is received; prices include applicable taxes unless stated otherwise. Returns are accepted within 14 days for unused items in their original packaging.</p>"}, {"language": "fr", "title": "Conditions d''utilisation", "body": "<h2>Conditions d''utilisation</h2><p>En passant commande sur cette boutique, vous acceptez les présentes conditions. Les commandes sont confirmées à réception du paiement ; les prix incluent les taxes applicables sauf mention contraire. Les retours sont acceptés sous 14 jours pour les articles non utilisés dans leur emballage d''origine.</p>"}]', now(), 'seed')
on conflict (id) do nothing;

-- FAQ groups and entries.
--
-- Demo content for the test store 65f023632bc46470c104b75f (USA Electronics Hub), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-301, '65f023632bc46470c104b75f', 'general', 0, '{"en": "General", "fr": "Questions générales"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-302, '65f023632bc46470c104b75f', 'ordering', 1, '{"en": "Ordering & payment", "fr": "Commande et paiement"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-303, '65f023632bc46470c104b75f', 'shipping', 2, '{"en": "Shipping & delivery", "fr": "Livraison"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-304, '65f023632bc46470c104b75f', 'returns', 3, '{"en": "Returns & warranty", "fr": "Retours et garantie"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;

-- faq: general / price-match
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30014, now(), now(), 'seed', 'price-match', 'FAQ', 0, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["do", "you", "price", "match?"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300027, now(), now(), 'seed', '<p>Yes, against any authorised US retailer, within fourteen days of purchase. Marketplace listings and grey imports do not count — we cannot honour a warranty on stock we did not source.</p>', 'Do you price match?', 'Do you price match?', null, null, null, null, 'en', -30014, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300028, now(), now(), 'seed', '<p>Oui, sur tout revendeur agréé aux États-Unis, dans les quatorze jours suivant l''achat. Les places de marché et les imports parallèles sont exclus : nous ne garantissons pas un produit que nous n''avons pas approvisionné.</p>', 'Alignez-vous vos prix ?', 'Alignez-vous vos prix ?', null, null, null, null, 'fr', -30014, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: general / open-box
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30015, now(), now(), 'seed', 'open-box', 'FAQ', 1, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["what", "does", "“open", "box”"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300029, now(), now(), 'seed', '<p>A returned unit that passed the full bench test. It is sold at a discount with the same two-year warranty as new stock, and the listing states exactly what is cosmetically imperfect.</p>', 'What does “open box” mean here?', 'What does “open box” mean here?', null, null, null, null, 'en', -30015, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300030, now(), now(), 'seed', '<p>Un produit retourné qui a repassé le banc de test complet. Vendu remisé, avec la même garantie de deux ans que le neuf, et la fiche indique précisément ce qui est esthétiquement imparfait.</p>', 'Que signifie « open box » ?', 'Que signifie « open box » ?', null, null, null, null, 'fr', -30015, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: general / in-store-stock
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30016, now(), now(), 'seed', 'in-store-stock', 'FAQ', 2, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["is", "everything", "online", "also"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300031, now(), now(), 'seed', '<p>No — the store carries about a third of the catalogue. The product page shows live in-store stock for the 28th Street location.</p>', 'Is everything online also in the store?', 'Is everything online also in the store?', null, null, null, null, 'en', -30016, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300032, now(), now(), 'seed', '<p>Non : la boutique tient environ un tiers du catalogue. La fiche produit affiche le stock réel du magasin de la 28e rue.</p>', 'Tout ce qui est en ligne est-il en boutique ?', 'Tout ce qui est en ligne est-il en boutique ?', null, null, null, null, 'fr', -30016, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / payment-methods
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30017, now(), now(), 'seed', 'payment-methods', 'FAQ', 0, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["which", "payment", "methods", "do"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300033, now(), now(), 'seed', '<p>All major cards, Apple Pay, Google Pay, PayPal, and Affirm for orders above $250. Payment is processed by Stripe; card numbers never touch our servers.</p>', 'Which payment methods do you accept?', 'Which payment methods do you accept?', null, null, null, null, 'en', -30017, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300034, now(), now(), 'seed', '<p>Toutes les cartes principales, Apple Pay, Google Pay, PayPal, et Affirm au-delà de 250 $. Le paiement est traité par Stripe ; les numéros de carte ne passent jamais par nos serveurs.</p>', 'Quels moyens de paiement acceptez-vous ?', 'Quels moyens de paiement acceptez-vous ?', null, null, null, null, 'fr', -30017, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / cancel-order
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30018, now(), now(), 'seed', 'cancel-order', 'FAQ', 1, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["can", "i", "cancel", "after"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300035, now(), now(), 'seed', '<p>Until the label prints, which is usually within two hours on weekdays. After that the parcel has to arrive and go back as a free return.</p>', 'Can I cancel after ordering?', 'Can I cancel after ordering?', null, null, null, null, 'en', -30018, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300036, now(), now(), 'seed', '<p>Jusqu''à l''impression de l''étiquette, soit généralement dans les deux heures en semaine. Ensuite, le colis doit arriver et repartir en retour gratuit.</p>', 'Puis-je annuler après commande ?', 'Puis-je annuler après commande ?', null, null, null, null, 'fr', -30018, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / sales-tax
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30019, now(), now(), 'seed', 'sales-tax', 'FAQ', 2, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["is", "sales", "tax", "included"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300037, now(), now(), 'seed', '<p>No. Prices are shown pre-tax; the applicable state and local rate is calculated at checkout from your shipping address.</p>', 'Is sales tax included in the price?', 'Is sales tax included in the price?', null, null, null, null, 'en', -30019, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300038, now(), now(), 'seed', '<p>Non. Les prix sont affichés hors taxe ; le taux applicable est calculé au paiement à partir de votre adresse de livraison.</p>', 'La taxe est-elle incluse dans le prix ?', 'La taxe est-elle incluse dans le prix ?', null, null, null, null, 'fr', -30019, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / delivery-times
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30020, now(), now(), 'seed', 'delivery-times', 'FAQ', 0, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["how", "fast", "is", "delivery?"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300039, now(), now(), 'seed', '<p>Three to five business days standard in the contiguous US, free over $75. Two-day and overnight are available at checkout.</p>', 'How fast is delivery?', 'How fast is delivery?', null, null, null, null, 'en', -30020, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300040, now(), now(), 'seed', '<p>Trois à cinq jours ouvrés en standard sur les États contigus, offert dès 75 $. Les options deux jours et lendemain sont proposées au paiement.</p>', 'Quels sont les délais de livraison ?', 'Quels sont les délais de livraison ?', null, null, null, null, 'fr', -30020, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / international
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30021, now(), now(), 'seed', 'international', 'FAQ', 1, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["do", "you", "ship", "internationally?"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300041, now(), now(), 'seed', '<p>To Canada and Mexico only. Duties and brokerage are collected by the carrier at delivery and are not included in the price.</p>', 'Do you ship internationally?', 'Do you ship internationally?', null, null, null, null, 'en', -30021, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300042, now(), now(), 'seed', '<p>Uniquement au Canada et au Mexique. Droits et frais de courtage sont perçus par le transporteur à la livraison et ne sont pas inclus.</p>', 'Livrez-vous à l''international ?', 'Livrez-vous à l''international ?', null, null, null, null, 'fr', -30021, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / store-pickup
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30022, now(), now(), 'seed', 'store-pickup', 'FAQ', 2, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["can", "i", "pick", "up"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300043, now(), now(), 'seed', '<p>Yes, free, usually within two hours during opening times. You will get an email when it is at the counter — do not travel before that.</p>', 'Can I pick up in store?', 'Can I pick up in store?', null, null, null, null, 'en', -30022, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300044, now(), now(), 'seed', '<p>Oui, gratuitement, généralement en deux heures pendant les horaires d''ouverture. Un e-mail vous prévient quand la commande est au comptoir — ne vous déplacez pas avant.</p>', 'Le retrait en boutique est-il possible ?', 'Le retrait en boutique est-il possible ?', null, null, null, null, 'fr', -30022, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / return-window
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30023, now(), now(), 'seed', 'return-window', 'FAQ', 0, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["how", "long", "do", "i"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300045, now(), now(), 'seed', '<p>Forty-five days unopened, thirty days opened. Return shipping is free and there is no restocking fee.</p>', 'How long do I have to return something?', 'How long do I have to return something?', null, null, null, null, 'en', -30023, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300046, now(), now(), 'seed', '<p>Quarante-cinq jours non ouvert, trente jours ouvert. Le retour est gratuit et sans frais de remise en stock.</p>', 'Combien de temps pour retourner un produit ?', 'Combien de temps pour retourner un produit ?', null, null, null, null, 'fr', -30023, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / warranty-claim
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30024, now(), now(), 'seed', 'warranty-claim', 'FAQ', 1, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["how", "do", "i", "make"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300047, now(), now(), 'seed', '<p>Open a claim from your account with the serial number. Median repair turnaround is six business days; past fourteen we ship a replacement instead.</p>', 'How do I make a warranty claim?', 'How do I make a warranty claim?', null, null, null, null, 'en', -30024, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300048, now(), now(), 'seed', '<p>Ouvrez un dossier depuis votre compte avec le numéro de série. Le délai médian de réparation est de six jours ouvrés ; au-delà de quatorze, nous expédions un remplacement.</p>', 'Comment faire jouer la garantie ?', 'Comment faire jouer la garantie ?', null, null, null, null, 'fr', -30024, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / dead-pixels
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-30025, now(), now(), 'seed', 'dead-pixels', 'FAQ', 2, true, '65f023632bc46470c104b75f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f023632bc46470c104b75f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["are", "dead", "pixels", "covered?"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300049, now(), now(), 'seed', '<p>Above the manufacturer''s own stated threshold, yes — and we will tell you what that threshold is before you buy, which most retailers will not.</p>', 'Are dead pixels covered?', 'Are dead pixels covered?', null, null, null, null, 'en', -30025, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-300050, now(), now(), 'seed', '<p>Au-delà du seuil annoncé par le constructeur, oui — et nous vous indiquons ce seuil avant l''achat, ce que la plupart des revendeurs évitent de faire.</p>', 'Les pixels morts sont-ils couverts ?', 'Les pixels morts sont-ils couverts ?', null, null, null, null, 'fr', -30025, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;


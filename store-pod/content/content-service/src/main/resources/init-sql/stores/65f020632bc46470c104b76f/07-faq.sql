-- FAQ groups and entries.
--
-- Demo content for the test store 65f020632bc46470c104b76f (Beauté Élégante Paris), in every language the store sells in.
-- Generated seed: ids are negative on purpose — content, media and menu ids all come from sequences that
-- only grow upward, so a seed row below zero can never collide with one the running service creates.

INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-101, '65f020632bc46470c104b76f', 'general', 0, '{"fr": "Questions générales", "en": "General"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-102, '65f020632bc46470c104b76f', 'ordering', 1, '{"fr": "Commande et paiement", "en": "Ordering & payment"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-103, '65f020632bc46470c104b76f', 'shipping', 2, '{"fr": "Livraison", "en": "Shipping & delivery"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;
INSERT INTO content.faq_group (id, store_merchant_id, group_key, position, names)
VALUES (-104, '65f020632bc46470c104b76f', 'returns', 3, '{"fr": "Retours et remboursements", "en": "Returns & refunds"}')
on conflict (store_merchant_id, group_key) do update
        set names = excluded.names, position = excluded.position;

-- faq: general / ou-est-la-boutique
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10014, now(), now(), 'seed', 'ou-est-la-boutique', 'FAQ', 0, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["où", "se", "trouve", "votre"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100027, now(), now(), 'seed', '<p>Au 15 rue de la Paix, Paris 2e, du mardi au samedi de 10h à 19h. Le diagnostic de peau y est gratuit et dure vingt minutes, sans achat obligatoire.</p>', 'Où se trouve votre boutique ?', 'Où se trouve votre boutique ?', null, null, null, null, 'fr', -10014, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100028, now(), now(), 'seed', '<p>At 15 rue de la Paix, Paris 2e, Tuesday to Saturday, 10am to 7pm. The skin diagnosis there is free, takes twenty minutes and comes with no obligation to buy.</p>', 'Where is your boutique?', 'Where is your boutique?', null, null, null, null, 'en', -10014, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: general / produits-testes-animaux
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10015, now(), now(), 'seed', 'produits-testes-animaux', 'FAQ', 1, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["vos", "produits", "sont-ils", "testés"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100029, now(), now(), 'seed', '<p>Non. Les tests sur animaux pour les cosmétiques sont interdits dans l''Union européenne depuis 2013, et nous ne commercialisons dans aucun pays qui les exigerait.</p>', 'Vos produits sont-ils testés sur les animaux ?', 'Vos produits sont-ils testés sur les animaux ?', null, null, null, null, 'fr', -10015, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100030, now(), now(), 'seed', '<p>No. Animal testing for cosmetics has been banned in the European Union since 2013, and we do not sell in any market that would require it.</p>', 'Are your products tested on animals?', 'Are your products tested on animals?', null, null, null, null, 'en', -10015, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: general / echantillons
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10016, now(), now(), 'seed', 'echantillons', 'FAQ', 2, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'general'), false, null, null, false, null, null, null, null, '{"keywords": ["proposez-vous", "des", "échantillons", "?"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100031, now(), now(), 'seed', '<p>Deux échantillons de votre choix accompagnent chaque commande. Vous les sélectionnez à l''étape du panier, juste avant le paiement.</p>', 'Proposez-vous des échantillons ?', 'Proposez-vous des échantillons ?', null, null, null, null, 'fr', -10016, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100032, now(), now(), 'seed', '<p>Two samples of your choosing come with every order. You pick them in the cart, just before payment.</p>', 'Do you offer samples?', 'Do you offer samples?', null, null, null, null, 'en', -10016, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / moyens-de-paiement
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10017, now(), now(), 'seed', 'moyens-de-paiement', 'FAQ', 0, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["quels", "moyens", "de", "paiement"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100033, now(), now(), 'seed', '<p>Carte bancaire (Visa, Mastercard, CB), Apple Pay, Google Pay et virement SEPA pour les commandes au-dessus de 500 €. Le paiement est traité par Stripe ; nous ne stockons jamais votre numéro de carte.</p>', 'Quels moyens de paiement acceptez-vous ?', 'Quels moyens de paiement acceptez-vous ?', null, null, null, null, 'fr', -10017, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100034, now(), now(), 'seed', '<p>Cards (Visa, Mastercard, CB), Apple Pay, Google Pay, and SEPA transfer for orders above €500. Payment is handled by Stripe; we never store your card number.</p>', 'Which payment methods do you accept?', 'Which payment methods do you accept?', null, null, null, null, 'en', -10017, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / modifier-commande
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10018, now(), now(), 'seed', 'modifier-commande', 'FAQ', 1, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["puis-je", "modifier", "ma", "commande"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100035, now(), now(), 'seed', '<p>Tant que la commande n''est pas passée en préparation, écrivez-nous : nous pouvons ajouter un article ou corriger une adresse. Passé ce stade, il faut attendre la livraison et faire un retour.</p>', 'Puis-je modifier ma commande après validation ?', 'Puis-je modifier ma commande après validation ?', null, null, null, null, 'fr', -10018, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100036, now(), now(), 'seed', '<p>As long as the order has not moved into packing, write to us: we can add an item or fix an address. After that, the parcel has to arrive and go back as a return.</p>', 'Can I change my order after checkout?', 'Can I change my order after checkout?', null, null, null, null, 'en', -10018, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: ordering / facture-tva
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10019, now(), now(), 'seed', 'facture-tva', 'FAQ', 2, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'ordering'), false, null, null, false, null, null, null, null, '{"keywords": ["comment", "obtenir", "une", "facture"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100037, now(), now(), 'seed', '<p>La facture est jointe au courriel de confirmation d''expédition et reste téléchargeable dans votre espace client, rubrique « Mes commandes ».</p>', 'Comment obtenir une facture avec TVA ?', 'Comment obtenir une facture avec TVA ?', null, null, null, null, 'fr', -10019, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100038, now(), now(), 'seed', '<p>The invoice is attached to the shipping confirmation email and stays downloadable from your account, under “My orders”.</p>', 'How do I get a VAT invoice?', 'How do I get a VAT invoice?', null, null, null, null, 'en', -10019, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / delais-livraison
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10020, now(), now(), 'seed', 'delais-livraison', 'FAQ', 0, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["quels", "sont", "les", "délais"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100039, now(), now(), 'seed', '<p>Deux à trois jours ouvrés en France métropolitaine, trois à six jours dans l''Union européenne. Un numéro de suivi vous est envoyé dès la remise au transporteur.</p>', 'Quels sont les délais de livraison ?', 'Quels sont les délais de livraison ?', null, null, null, null, 'fr', -10020, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100040, now(), now(), 'seed', '<p>Two to three working days in mainland France, three to six across the European Union. A tracking number is sent as soon as the parcel reaches the carrier.</p>', 'How long does delivery take?', 'How long does delivery take?', null, null, null, null, 'en', -10020, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / livraison-internationale
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10021, now(), now(), 'seed', 'livraison-internationale', 'FAQ', 1, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["livrez-vous", "hors", "d''europe", "?"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100041, now(), now(), 'seed', '<p>Pas encore. Nous livrons l''Union européenne, la Suisse et le Royaume-Uni ; les autres destinations arriveront courant 2026.</p>', 'Livrez-vous hors d''Europe ?', 'Livrez-vous hors d''Europe ?', null, null, null, null, 'fr', -10021, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100042, now(), now(), 'seed', '<p>Not yet. We deliver to the European Union, Switzerland and the United Kingdom; other destinations are planned for 2026.</p>', 'Do you ship outside Europe?', 'Do you ship outside Europe?', null, null, null, null, 'en', -10021, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: shipping / retrait-boutique
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10022, now(), now(), 'seed', 'retrait-boutique', 'FAQ', 2, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'shipping'), false, null, null, false, null, null, null, null, '{"keywords": ["le", "retrait", "en", "boutique"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100043, now(), now(), 'seed', '<p>Oui, et il est gratuit. Choisissez « Retrait boutique » au paiement : la commande est prête sous deux heures pendant les horaires d''ouverture.</p>', 'Le retrait en boutique est-il possible ?', 'Le retrait en boutique est-il possible ?', null, null, null, null, 'fr', -10022, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100044, now(), now(), 'seed', '<p>Yes, and it is free. Choose “Boutique pickup” at checkout: the order is ready within two hours during opening times.</p>', 'Can I collect in the boutique?', 'Can I collect in the boutique?', null, null, null, null, 'en', -10022, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / delai-retour
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10023, now(), now(), 'seed', 'delai-retour', 'FAQ', 0, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["combien", "de", "temps", "pour"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100045, now(), now(), 'seed', '<p>Trente jours après réception, à condition que le produit soit non ouvert et son scellé d''hygiène intact.</p>', 'Combien de temps pour retourner un produit ?', 'Combien de temps pour retourner un produit ?', null, null, null, null, 'fr', -10023, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100046, now(), now(), 'seed', '<p>Thirty days from delivery, provided the item is unopened and its hygiene seal is intact.</p>', 'How long do I have to return something?', 'How long do I have to return something?', null, null, null, null, 'en', -10023, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / produit-abime
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10024, now(), now(), 'seed', 'produit-abime', 'FAQ', 1, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["mon", "produit", "est", "arrivé"], "showInCheckoutHelp": true}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100047, now(), now(), 'seed', '<p>Photographiez le colis et le produit, puis écrivez-nous sous 48 heures. Nous réexpédions immédiatement, sans attendre le retour de l''article.</p>', 'Mon produit est arrivé abîmé, que faire ?', 'Mon produit est arrivé abîmé, que faire ?', null, null, null, null, 'fr', -10024, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100048, now(), now(), 'seed', '<p>Photograph the parcel and the item, then write to us within 48 hours. We ship a replacement immediately, without waiting for the original to come back.</p>', 'My item arrived damaged — what now?', 'My item arrived damaged — what now?', null, null, null, null, 'en', -10024, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;

-- faq: returns / delai-remboursement
INSERT INTO content.content (content_id, date_created, date_modified, updt_id, code, content_type, sort_order, visible, store_merchant_id, status, publish_at, unpublish_at, version, created_by, updated_by, parent_id, noindex, canonical_url, og_media_id, show_in_footer, placement, starts_at, ends_at, policy_type, meta)
VALUES (-10025, now(), now(), 'seed', 'delai-remboursement', 'FAQ', 2, true, '65f020632bc46470c104b76f', 'PUBLISHED', null, null, 1, 'seed', 'seed', (select id from content.faq_group where store_merchant_id = '65f020632bc46470c104b76f' and group_key = 'returns'), false, null, null, false, null, null, null, null, '{"keywords": ["quand", "suis-je", "remboursé", "?"], "showInCheckoutHelp": false}')
on conflict (content_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100049, now(), now(), 'seed', '<p>Sous cinq jours ouvrés après réception du retour à l''atelier. Le délai d''apparition sur votre relevé dépend ensuite de votre banque.</p>', 'Quand suis-je remboursé ?', 'Quand suis-je remboursé ?', null, null, null, null, 'fr', -10025, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, updt_id, description, name, title, meta_description, meta_keywords, meta_title, sef_url, language_code, content_id, state, excerpt, alt_text, cta_label, subtitle)
VALUES (-100050, now(), now(), 'seed', '<p>Within five working days of the return reaching the workshop. How quickly it shows on your statement is then down to your bank.</p>', 'When am I refunded?', 'When am I refunded?', null, null, null, null, 'en', -10025, 'TRANSLATED', null, null, null, null)
on conflict (description_id) do nothing;


/*
Generated SQL inserts similar to the provided template
where languages=['fr','en'] and store_id='65f020632bc46470c104b76f' and country_id='FR'
domain for this store is beauté
*/

-- Insert into merchant.merchant_store`
INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme,
                                     color_theme, seizeunitcode, store_email, store_logo, store_banner, store_address,
                                     store_city, store_name, store_phone, store_postal_code, store_state_prov,
                                     use_cache, require_login_for_order_placement, weightunitcode, country_id,
                                     currency_id, language_code)
VALUES ('65f020632bc46470c104b76f', false, '2024-03-31', '352023632b046970c104b76f', 'JEWELERY', 'LIGHT',
        'CM', -- Changed seizeunitcode to CM
        'contact@beaute-elegante.fr', 'logo.jpeg', 'banner.jpeg', '15 Rue de la Paix', 'Paris', 'Beauté-Élégante-Paris',
        '+33 1 23 45 67 89',
        '75002', 'Île-de-France', false, true, 'KG', 'FR', 'EUR',
        'fr') -- Changed weightunitcode to KG, set currency to EUR, first language to fr
on conflict (store_merchant_id) do nothing;

-- Insert into merchant.merchant_language for 'fr' and 'en'
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f020632bc46470c104b76f', 'fr')
on conflict (store_merchant_id, language_code) do nothing;

INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f020632bc46470c104b76f', 'en')
on conflict (store_merchant_id, language_code) do nothing;

INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f020632bc46470c104b76f', 0, 'slide-1.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f020632bc46470c104b76f', 1, 'slide-2.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f020632bc46470c104b76f', 2, 'slide-3.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f020632bc46470c104b76f', 3, 'slide-4.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f020632bc46470c104b76f', 4, 'slide-5.jpeg');


-- Insert into merchant.social_links for 'FACEBOOK', 'X', 'INSTAGRAM', 'TIKTOK'
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f020632bc46470c104b76f', 'FACEBOOK', 'https://www.facebook.com/beauteeleganteparis');

INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f020632bc46470c104b76f', 'X', 'https://www.twitter.com/beauteelegante');

INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f020632bc46470c104b76f', 'INSTAGRAM', 'https://www.instagram.com/beaute.elegante.paris');

INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f020632bc46470c104b76f', 'TIKTOK', 'https://www.tiktok.com/@beauteelegante');



INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org2-store1.asrevo.com', 'CUSTOM_DOMAIN', '65f020632bc46470c104b76f')
ON CONFLICT DO NOTHING;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org2-store1', 'SUB_DOMAIN', '65f020632bc46470c104b76f')
ON CONFLICT DO NOTHING;



/*
Generated content for store_id='65f020632bc46470c104b76f' (beauté Domain) store name Beauté Élégante Paris
Pages: ['about-us', 'contact-us', 'terms', 'privacy', 'location', 'faq']
Boxes: ['header-message', 'agreement','meta-title','meta-description']
Languages: ['fr', 'en']
Starting content_id: 11
Starting description_id: 21
Starting sort_order: 1
*/

-- Page: about-us
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (11, 'about-us', 'PAGE', true, 1, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (21, now(), now(),
        '<h1>À Propos de Beauté Élégante Paris</h1><p>Bienvenue chez Beauté Élégante Paris, votre sanctuaire de la beauté de luxe au cœur de Paris. Découvrez notre sélection exquise de soins, maquillages et parfums des plus grandes marques.</p><h2>Notre Philosophie</h2><p>Nous croyons en l''élégance intemporelle et le pouvoir de la beauté pour révéler la confiance en soi. Chaque produit est choisi pour sa qualité exceptionnelle et son efficacité.</p>',
        'À Propos', 'À Propos de Beauté Élégante Paris',
        'Découvrez Beauté Élégante Paris, la destination de luxe pour les soins, le maquillage et les parfums à Paris.',
        'beauté, luxe, paris, soins, maquillage, parfum, élégance', 'À Propos | Beauté Élégante Paris',
        'a-propos', 11, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (22, now(), now(),
        '<h1>About Beauté Élégante Paris</h1><p>Welcome to Beauté Élégante Paris, your sanctuary for luxury beauty in the heart of Paris. Discover our exquisite selection of skincare, makeup, and fragrances from the finest brands.</p><h2>Our Philosophy</h2><p>We believe in timeless elegance and the power of beauty to reveal self-confidence. Each product is chosen for its exceptional quality and effectiveness.</p>',
        'About Us', 'About Beauté Élégante Paris',
        'Discover Beauté Élégante Paris, the luxury destination for skincare, makeup, and perfumes in Paris.',
        'beauty, luxury, paris, skincare, makeup, fragrance, elegance', 'About Us | Beauté Élégante Paris',
        'about-us', 11, 'en')
on conflict (description_id) do nothing;

-- Page: contact-us
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (12, 'contact-us', 'PAGE', true, 2, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (23, now(), now(),
        '<h1>Contactez Beauté Élégante Paris</h1><p>Pour toute question ou conseil personnalisé, notre équipe d''experts beauté est à votre écoute.</p><h2>Coordonnées :</h2><ul><li>Adresse : 25 Rue du Faubourg Saint-Honoré, 75008 Paris, France</li><li>Téléphone : +33 1 40 00 00 00</li><li>Email : contact@beauteelegante.paris</li></ul><!-- Contact Form Placeholder -->',
        'Contactez-Nous', 'Contactez Beauté Élégante Paris',
        'Contactez les experts beauté de Beauté Élégante Paris pour des conseils personnalisés ou des questions.',
        'contact, beauté, paris, service client, conseil beauté', 'Contactez-Nous | Beauté Élégante Paris',
        'contactez-nous', 12, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (24, now(), now(),
        '<h1>Contact Beauté Élégante Paris</h1><p>For any questions or personalized advice, our team of beauty experts is here to listen.</p><h2>Contact Details:</h2><ul><li>Address: 25 Rue du Faubourg Saint-Honoré, 75008 Paris, France</li><li>Phone: +33 1 40 00 00 00</li><li>Email: contact@beauteelegante.paris</li></ul><!-- Contact Form Placeholder -->',
        'Contact Us', 'Contact Beauté Élégante Paris',
        'Contact the beauty experts at Beauté Élégante Paris for personalized advice or inquiries.',
        'contact, beauty, paris, customer service, beauty advice', 'Contact Us | Beauté Élégante Paris',
        'contact-us', 12, 'en')
on conflict (description_id) do nothing;

-- Page: terms
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (13, 'terms', 'PAGE', false, 3, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (25, now(), now(),
        '<h1>Conditions Générales de Vente</h1><p>Veuillez lire attentivement nos conditions générales de vente avant d''effectuer un achat sur Beauté Élégante Paris.</p><h2>Article 1 : Produits</h2><p>Les produits proposés sont conformes à la législation française en vigueur.</p><h2>Article 2 : Prix</h2><p>Les prix sont indiqués en Euros (€) toutes taxes comprises (TTC).</p><h2>Article 3 : Commande et Paiement</h2><p>Le paiement est exigible immédiatement à la commande.</p>',
        'Conditions Générales', 'Conditions Générales | Beauté Élégante Paris',
        'Consultez les conditions générales de vente de la boutique en ligne Beauté Élégante Paris.',
        'conditions, termes, vente, légal, beauté, paris', 'Conditions Générales | Beauté Élégante Paris',
        'conditions-generales', 13, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (26, now(), now(),
        '<h1>Terms and Conditions</h1><p>Please carefully read our terms and conditions before making a purchase on Beauté Élégante Paris.</p><h2>Article 1: Products</h2><p>The products offered comply with current French legislation.</p><h2>Article 2: Prices</h2><p>Prices are indicated in Euros (€) including all taxes (VAT included).</p><h2>Article 3: Order and Payment</h2><p>Payment is due immediately upon ordering.</p>',
        'Terms & Conditions', 'Terms & Conditions | Beauté Élégante Paris',
        'Review the terms and conditions for the Beauté Élégante Paris online boutique.',
        'terms, conditions, sale, legal, beauty, paris', 'Terms & Conditions | Beauté Élégante Paris',
        'terms', 13, 'en')
on conflict (description_id) do nothing;

-- Page: privacy
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (14, 'privacy', 'PAGE', false, 4, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (27, now(), now(),
        '<h1>Politique de Confidentialité</h1><p>Beauté Élégante Paris s''engage à protéger la confidentialité de vos informations personnelles.</p><h2>Collecte des informations</h2><p>Nous collectons les informations nécessaires au traitement de votre commande et à l''amélioration de nos services.</p><h2>Utilisation des informations</h2><p>Vos données sont utilisées exclusivement par Beauté Élégante Paris et ne sont pas partagées avec des tiers sans votre consentement.</p>',
        'Politique de Confidentialité', 'Politique de Confidentialité | Beauté Élégante Paris',
        'Notre politique concernant la collecte et l''utilisation de vos données personnelles sur Beauté Élégante Paris.',
        'confidentialité, vie privée, données personnelles, beauté, paris',
        'Politique de Confidentialité | Beauté Élégante Paris',
        'politique-confidentialite', 14, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (28, now(), now(),
        '<h1>Privacy Policy</h1><p>Beauté Élégante Paris is committed to protecting the privacy of your personal information.</p><h2>Information Collection</h2><p>We collect the information necessary to process your order and improve our services.</p><h2>Use of Information</h2><p>Your data is used exclusively by Beauté Élégante Paris and is not shared with third parties without your consent.</p>',
        'Privacy Policy', 'Privacy Policy | Beauté Élégante Paris',
        'Our policy regarding the collection and use of your personal data on Beauté Élégante Paris.',
        'privacy, policy, personal data, beauty, paris', 'Privacy Policy | Beauté Élégante Paris',
        'privacy', 14, 'en')
on conflict (description_id) do nothing;

-- Page: location
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (15, 'location', 'PAGE', false, 5, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (29, now(), now(),
        '<h1>Notre Boutique</h1><p>Retrouvez l''univers Beauté Élégante Paris dans notre boutique phare.</p><h2>Adresse</h2><p>25 Rue du Faubourg Saint-Honoré<br>75008 Paris, France</p><h2>Horaires d''Ouverture</h2><p>Lundi - Samedi : 10h00 - 19h00<br>Dimanche : Fermé</p><!-- Map Placeholder -->',
        'Notre Boutique', 'Trouver Notre Boutique | Beauté Élégante Paris',
        'Localisez notre boutique Beauté Élégante Paris et consultez nos horaires d''ouverture.',
        'boutique, magasin, adresse, paris, beauté, emplacement', 'Notre Boutique | Beauté Élégante Paris',
        'notre-boutique', 15, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (30, now(), now(),
        '<h1>Our Boutique</h1><p>Discover the world of Beauté Élégante Paris in our flagship boutique.</p><h2>Address</h2><p>25 Rue du Faubourg Saint-Honoré<br>75008 Paris, France</p><h2>Opening Hours</h2><p>Monday - Saturday: 10:00 AM - 7:00 PM<br>Sunday: Closed</p><!-- Map Placeholder -->',
        'Our Boutique', 'Find Our Boutique | Beauté Élégante Paris',
        'Locate our Beauté Élégante Paris boutique and check our opening hours.',
        'boutique, store, address, paris, beauty, location', 'Our Boutique | Beauté Élégante Paris',
        'location', 15, 'en')
on conflict (description_id) do nothing;

-- Page: faq
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (16, 'faq', 'PAGE', false, 6, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (31, now(), now(),
        '<h1>Questions Fréquemment Posées</h1><p>Trouvez des réponses à vos questions sur nos produits, votre commande ou nos services.</p><h2>Produits</h2><p><strong>Q : Vos produits sont-ils testés sur les animaux ?</strong><br>R : Non, Beauté Élégante Paris s''engage contre les tests sur les animaux.</p><h2>Commande</h2><p><strong>Q : Comment suivre ma commande ?</strong><br>R : Un lien de suivi vous sera envoyé par email lors de l''expédition.</p>',
        'FAQ', 'FAQ | Beauté Élégante Paris',
        'Réponses aux questions fréquentes sur les produits et services de Beauté Élégante Paris.',
        'faq, questions, aide, beauté, paris, commande, livraison', 'FAQ | Beauté Élégante Paris',
        'faq', 16, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (32, now(), now(),
        '<h1>Frequently Asked Questions</h1><p>Find answers to your questions about our products, your order, or our services.</p><h2>Products</h2><p><strong>Q: Are your products tested on animals?</strong><br>A: No, Beauté Élégante Paris is committed against animal testing.</p><h2>Order</h2><p><strong>Q: How can I track my order?</strong><br>A: A tracking link will be sent to you by email upon shipment.</p>',
        'FAQ', 'FAQ | Beauté Élégante Paris',
        'Answers to frequently asked questions about Beauté Élégante Paris products and services.',
        'faq, questions, help, beauty, paris, order, shipping', 'FAQ | Beauté Élégante Paris',
        'faq', 16, 'en')
on conflict (description_id) do nothing;

-- Box: header-message
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (17, 'header-message', 'BOX', false, 7, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (33, now(), now(),
        'Livraison offerte en France métropolitaine dès 75€ d''achat.',
        'Message d''En-tête', 'Message d''En-tête',
        '', '', '', '', 17, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (34, now(), now(),
        'Free shipping in mainland France on orders over €75.',
        'Header Message', 'Header Message',
        '', '', '', '', 17, 'en')
on conflict (description_id) do nothing;

-- Box: agreement
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (18, 'agreement', 'BOX', false, 8, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (35, now(), now(),
        '<p style="text-align:center; font-size:0.9em; padding:5px;">En naviguant sur ce site, vous acceptez nos <a href="/conditions-generales">Conditions Générales</a>.</p>',
        'Accord Utilisateur', 'Accord Utilisateur',
        '', '', '', '', 18, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (36, now(), now(),
        '<p style="text-align:center; font-size:0.9em; padding:5px;">By browsing this site, you agree to our <a href="/terms">Terms & Conditions</a>.</p>',
        'User Agreement', 'User Agreement',
        '', '', '', '', 18, 'en')
on conflict (description_id) do nothing;

-- NEW BOX: meta-title
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (19, 'meta-title', 'BOX', false, 9, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (37, now(), now(),
        'Beauté Élégante Paris | Soins de Luxe, Maquillage & Parfums d''Exception',
        '', '', '', '', '', '', 19, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (38, now(), now(),
        'Beauté Élégante Paris | Luxury Skincare, Makeup & Exquisite Perfumes',
        '', '', '', '', '', '', 19, 'en')
on conflict (description_id) do nothing;

-- NEW BOX: meta-description
INSERT INTO merchant.content (content_id, code, content_type,
                              link_to_menu, sort_order, visible, store_merchant_id)
VALUES (20, 'meta-description', 'BOX', false, 10, true, '65f020632bc46470c104b76f')
on conflict (content_id) do nothing;

-- Language: fr
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (39, now(), now(),
        'Découvrez l''univers de Beauté Élégante Paris. Votre destination pour les soins de la peau haut de gamme, le maquillage de créateur et les parfums rares au cœur de Paris. Élégance et savoir-faire français.',
        '', '', '', '', '', '', 20, 'fr')
on conflict (description_id) do nothing;
-- Language: en
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (40, now(), now(),
        'Explore the world of Beauté Élégante Paris. Your destination for premium skincare, designer makeup, and rare fragrances in the heart of Paris. French elegance and expertise.',
        '', '', '', '', '', '', 20, 'en')
on conflict (description_id) do nothing;
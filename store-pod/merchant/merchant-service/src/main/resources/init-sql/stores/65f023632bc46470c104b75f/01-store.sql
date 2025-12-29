/*
Generated SQL inserts for a new store based on the original template.
Store ID: '65f023632bc46470c104b75f'
Languages: ['en', 'fr']
Country: 'USA'
Domain: electronics
*/

-- Define parameters for clarity (These would typically be replaced by actual values or generator logic)
-- Let's assume:
-- $paramter.store_id = '65f023632bc46470c104b75f'
-- $paramter.country_id = 'USA'
-- $parameter.languages = ['en', 'fr']
-- $generator.currencyForCountry('USA') resolves to the currency_id for USD (e.g., 1 - assuming this ID exists)
-- New Org ID generated: '41a034a43cd77581d105c87b'

INSERT INTO merchant.merchant_store (store_merchant_id, currency_format_national, in_business_since, org, theme,
                                     color_theme, seizeunitcode, store_email, store_logo, store_banner, store_address,
                                     store_city, store_name, store_phone, store_postal_code, store_state_prov,
                                     use_cache, weightunitcode, country_id, currency_id, language_code)
VALUES ('65f023632bc46470c104b75f', false, '2024-04-01', '21f023932bc66470c104b76f', 'MODERN', 'LIGHT', 'IN',
        'info@usaelectronics.com', 'logo.jpeg', 'banner.jpeg', '456 Tech Avenue',
        'New York', 'USA-Electronics-Hub', '+1-212-555-0123',
        '10001', 'NY', false, 'LB', 'USA', 'USD',
        'en') -- Using 'en' as the first language
on conflict (store_merchant_id) do nothing;
-- Specify conflict target for clarity

-- Loop over languages: 'en', 'fr'
INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b75f', 'en')
on conflict (store_merchant_id, language_code) do nothing; -- Specify conflict target

INSERT INTO merchant.merchant_language (store_merchant_id, language_code)
VALUES ('65f023632bc46470c104b75f', 'fr')
on conflict (store_merchant_id, language_code) do nothing;
-- Specify conflict target

-- Loop for slider images (0 to 4)
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b75f', 0, 'slide-1.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b75f', 1, 'slide-2.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b75f', 2, 'slide-3.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b75f', 3, 'slide-4.jpeg');
INSERT INTO merchant.merchant_slider_images (store_merchant_id, priority, name)
VALUES ('65f023632bc46470c104b75f', 4, 'slide-5.jpeg');


-- Loop for social links ('FACEBOOK','X','INSTAGRAM','TIKTOK')
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b75f', 'FACEBOOK', 'https://facebook.com/usaelectronics');
INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b75f', 'X', 'https://x.com/usaelectronics');

INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b75f', 'INSTAGRAM', 'https://instagram.com/usaelectronics');

INSERT INTO merchant.social_links (store_merchant_id, provider, url)
VALUES ('65f023632bc46470c104b75f', 'TIKTOK', 'https://tiktok.com/@usaelectronics');




INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org1-store2.asrevo.com', 'CUSTOM_DOMAIN', '65f023632bc46470c104b75f')
ON CONFLICT DO NOTHING;

INSERT INTO merchant.store_domains(domain, domain_type, store_merchant_id)
VALUES ('org1-store2', 'SUB_DOMAIN', '65f023632bc46470c104b75f')
ON CONFLICT DO NOTHING;


/*
Generated content for store_id='65f023632bc46470c104b75f' (electronics Domain) store name USA Electronics Hub
Pages: ['about-us', 'contact-us', 'terms', 'privacy', 'location', 'faq']
Boxes: ['header-message', 'agreement','meta-title','meta-description']
Languages: ['en', 'fr']
Starting content_id: 31
Starting description_id: 61
Starting sort_order: 1
*/

-- Page: about-us
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (31, 'about-us', 'PAGE', true, 1, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (61, now(), now(),
        '<h1>About USA Electronics Hub</h1><p>Welcome to USA Electronics Hub, your trusted American source for the latest in consumer electronics. Based right here in the USA, we offer a vast selection of top brands in smartphones, computers, TVs, audio, and more.</p><h2>Our Mission</h2><p>To provide American consumers with cutting-edge technology, competitive prices, fast shipping from within the US, and outstanding customer support.</p><h2>Why Choose Us?</h2><p>USA-based operations, wide product range, knowledgeable staff, and a commitment to customer satisfaction.</p>',
        'About Us', 'About USA Electronics Hub',
        'Learn about USA Electronics Hub, your American destination for the latest electronics and tech gadgets.',
        'electronics, USA, about us, tech hub, gadgets, computers, TVs, American retailer',
        'About Us | USA Electronics Hub',
        'about-us', 31, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (62, now(), now(),
        '<h1>À Propos de USA Electronics Hub</h1><p>Bienvenue chez USA Electronics Hub, votre source américaine de confiance pour les dernières nouveautés en électronique grand public. Basés ici aux États-Unis, nous proposons une vaste sélection des meilleures marques de smartphones, ordinateurs, téléviseurs, audio, et plus encore.</p><h2>Notre Mission</h2><p>Fournir aux consommateurs américains une technologie de pointe, des prix compétitifs, une expédition rapide depuis les États-Unis et un support client exceptionnel.</p><h2>Pourquoi Nous Choisir ?</h2><p>Opérations basées aux États-Unis, large gamme de produits, personnel compétent et engagement envers la satisfaction client.</p>',
        'À Propos', 'À Propos de USA Electronics Hub',
        'Découvrez USA Electronics Hub, votre destination américaine pour les derniers appareils électroniques et gadgets technologiques.',
        'électronique, USA, à propos, tech hub, gadgets, ordinateurs, téléviseurs, détaillant américain',
        'À Propos | USA Electronics Hub',
        'a-propos', 31, 'fr')
on conflict (description_id) do nothing;

-- Page: contact-us
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (32, 'contact-us', 'PAGE', true, 2, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (63, now(), now(),
        '<h1>Contact USA Electronics Hub</h1><p>Have questions or need assistance? Our US-based customer support team is ready to help!</p><h2>Contact Information:</h2><ul><li>Email: support@usaelectronicshub.com</li><li>Phone: 1-800-USA-ELEC (1-800-872-3532)</li><li>Address: 1 Technology Plaza, Silicon Valley, CA 94000, USA</li><li>Hours: Mon-Fri, 9 AM - 5 PM PST</li></ul><!-- Contact Form Placeholder -->',
        'Contact Us', 'Contact USA Electronics Hub',
        'Get in touch with the USA Electronics Hub customer support team for product inquiries, order support, or technical help.',
        'contact, USA Electronics Hub, support, customer service, electronics help, phone, email, USA',
        'Contact Us | USA Electronics Hub',
        'contact-us', 32, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (64, now(), now(),
        '<h1>Contactez USA Electronics Hub</h1><p>Vous avez des questions ou besoin d''aide ? Notre équipe de support client basée aux États-Unis est prête à vous aider !</p><h2>Coordonnées :</h2><ul><li>Email : support@usaelectronicshub.com</li><li>Téléphone : 1-800-USA-ELEC (1-800-872-3532)</li><li>Adresse : 1 Technology Plaza, Silicon Valley, CA 94000, USA</li><li>Horaires : Lun-Ven, 9h - 17h PST</li></ul><!-- Placeholder Formulaire de Contact -->',
        'Contactez-Nous', 'Contactez USA Electronics Hub',
        'Contactez l''équipe de support client de USA Electronics Hub pour des questions sur les produits, le support de commande ou l''aide technique.',
        'contact, USA Electronics Hub, support, service client, aide électronique, téléphone, email, USA',
        'Contactez-Nous | USA Electronics Hub',
        'contactez-nous', 32, 'fr')
on conflict (description_id) do nothing;

-- Page: terms
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (33, 'terms', 'PAGE', false, 3, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (65, now(), now(),
        '<h1>Terms and Conditions</h1><p>Welcome to USA Electronics Hub. These terms govern your use of our website and the purchase of products from us.</p><h2>1. Use of Website</h2><p>By accessing usaelectronicshub.com, you agree to comply with these terms.</p><h2>2. Product Information and Pricing</h2><p>We strive for accuracy, but errors may occur. Prices and availability are subject to change without notice. All prices are in USD.</p><h2>3. Shipping and Returns</h2><p>We ship within the United States. Please review our Shipping & Returns policy for details on delivery times, costs, and our return process (e.g., 30-day return window).</p><h2>4. Governing Law</h2><p>These terms are governed by the laws of the State of California and the United States.</p>',
        'Terms & Conditions', 'Terms & Conditions | USA Electronics Hub',
        'Read the terms and conditions for using the USA Electronics Hub website and purchasing electronics within the US.',
        'terms, conditions, USA Electronics Hub, legal, electronics policy, shipping, returns, USA',
        'Terms & Conditions | USA Electronics Hub',
        'terms', 33, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (66, now(), now(),
        '<h1>Conditions Générales</h1><p>Bienvenue chez USA Electronics Hub. Ces conditions régissent votre utilisation de notre site Web et l''achat de produits chez nous.</p><h2>1. Utilisation du Site Web</h2><p>En accédant à usaelectronicshub.com, vous acceptez de vous conformer à ces conditions.</p><h2>2. Informations sur les Produits et Tarification</h2><p>Nous nous efforçons d''être précis, mais des erreurs peuvent survenir. Les prix et la disponibilité sont sujets à changement sans préavis. Tous les prix sont en USD.</p><h2>3. Expédition et Retours</h2><p>Nous expédions aux États-Unis. Veuillez consulter notre politique d''Expédition et Retours pour les détails sur les délais de livraison, les coûts et notre processus de retour (par ex., fenêtre de retour de 30 jours).</p><h2>4. Droit Applicable</h2><p>Ces conditions sont régies par les lois de l''État de Californie et des États-Unis.</p>',
        'Conditions Générales', 'Conditions Générales | USA Electronics Hub',
        'Lisez les conditions générales d''utilisation du site Web de USA Electronics Hub et d''achat d''électronique aux États-Unis.',
        'conditions, termes, USA Electronics Hub, légal, politique électronique, expédition, retours, USA',
        'Conditions Générales | USA Electronics Hub',
        'conditions-generales', 33, 'fr')
on conflict (description_id) do nothing;

-- Page: privacy
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (34, 'privacy', 'PAGE', false, 4, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (67, now(), now(),
        '<h1>Privacy Policy</h1><p>USA Electronics Hub values your privacy. This policy details how we collect, use, and protect your personal information in compliance with US regulations.</p><h2>Information We Collect</h2><p>We collect information you provide (name, address, email, payment info) and data collected automatically (IP address, browsing activity).</p><h2>How We Use Information</h2><p>To process orders, improve your experience, communicate with you, and for marketing purposes (with opt-out options).</p><h2>Data Security</h2><p>We implement robust security measures to protect your data.</p><h2>Your Rights</h2><p>You may have rights regarding your personal data under laws like the CCPA. Contact us to exercise these rights.</p>',
        'Privacy Policy', 'Privacy Policy | USA Electronics Hub',
        'Understand how USA Electronics Hub collects, uses, and protects your personal data in the USA.',
        'privacy, policy, USA Electronics Hub, data protection, security, personal information, CCPA, USA',
        'Privacy Policy | USA Electronics Hub',
        'privacy', 34, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (68, now(), now(),
        '<h1>Politique de Confidentialité</h1><p>USA Electronics Hub respecte votre vie privée. Cette politique détaille comment nous collectons, utilisons et protégeons vos informations personnelles conformément aux réglementations américaines.</p><h2>Informations que nous collectons</h2><p>Nous collectons les informations que vous fournissez (nom, adresse, email, informations de paiement) et les données collectées automatiquement (adresse IP, activité de navigation).</p><h2>Comment nous utilisons les informations</h2><p>Pour traiter les commandes, améliorer votre expérience, communiquer avec vous et à des fins de marketing (avec options de désinscription).</p><h2>Sécurité des données</h2><p>Nous mettons en œuvre des mesures de sécurité robustes pour protéger vos données.</p><h2>Vos droits</h2><p>Vous pouvez avoir des droits concernant vos données personnelles en vertu de lois telles que le CCPA. Contactez-nous pour exercer ces droits.</p>',
        'Politique de Confidentialité', 'Politique de Confidentialité | USA Electronics Hub',
        'Comprenez comment USA Electronics Hub collecte, utilise et protège vos données personnelles aux États-Unis.',
        'confidentialité, politique, USA Electronics Hub, protection données, sécurité, informations personnelles, CCPA, USA',
        'Politique de Confidentialité | USA Electronics Hub',
        'politique-confidentialite', 34, 'fr')
on conflict (description_id) do nothing;

-- Page: location
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (35, 'location', 'PAGE', false, 5, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (69, now(), now(),
        '<h1>Our Location</h1><p>USA Electronics Hub operates primarily online, shipping from fulfillment centers across the United States to ensure fast delivery.</p><h2>Headquarters</h2><p>1 Technology Plaza<br>Silicon Valley, CA 94000, USA</p><p><i>Please note: Our headquarters is not a retail location. All orders must be placed online.</i></p><!-- Map Placeholder showing USA -->',
        'Our Location', 'USA Electronics Hub Location',
        'Find information about USA Electronics Hub''s headquarters and operational base in the USA.',
        'location, USA Electronics Hub, address, headquarters, USA, online retailer',
        'Our Location | USA Electronics Hub',
        'location', 35, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (70, now(), now(),
        '<h1>Notre Emplacement</h1><p>USA Electronics Hub opère principalement en ligne, expédiant depuis des centres de distribution à travers les États-Unis pour assurer une livraison rapide.</p><h2>Siège Social</h2><p>1 Technology Plaza<br>Silicon Valley, CA 94000, USA</p><p><i>Veuillez noter : Notre siège social n''est pas un point de vente au détail. Toutes les commandes doivent être passées en ligne.</i></p><!-- Placeholder Carte montrant les USA -->',
        'Notre Emplacement', 'Emplacement USA Electronics Hub',
        'Trouvez des informations sur le siège social et la base opérationnelle de USA Electronics Hub aux États-Unis.',
        'emplacement, USA Electronics Hub, adresse, siège social, USA, détaillant en ligne',
        'Notre Emplacement | USA Electronics Hub',
        'notre-emplacement', 35, 'fr')
on conflict (description_id) do nothing;

-- Page: faq
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (36, 'faq', 'PAGE', false, 6, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (71, now(), now(),
        '<h1>Frequently Asked Questions (FAQ)</h1><p>Find answers to common questions about shopping with USA Electronics Hub.</p><h2>Shipping</h2><p><strong>Q: Where do you ship from?</strong><br>A: We ship from various fulfillment centers within the United States.</p><p><strong>Q: How long does shipping take?</strong><br>A: Standard shipping usually takes 3-7 business days within the continental US.</p><h2>Warranty & Returns</h2><p><strong>Q: What is your return policy?</strong><br>A: We offer a 30-day return policy for most items. See our <a href="/terms">Terms & Conditions</a> for details.</p><p><strong>Q: How do I claim a warranty?</strong><br>A: Please contact our customer support with your order details and issue description.</p><h2>Payment</h2><p><strong>Q: What payment methods do you accept?</strong><br>A: We accept major US credit cards (Visa, MasterCard, Amex, Discover), PayPal, and other methods indicated at checkout.</p>',
        'FAQ', 'FAQ | USA Electronics Hub',
        'Answers to common questions about shipping, returns, warranty, and payments at USA Electronics Hub.',
        'faq, USA Electronics Hub, help, support, questions, electronics, shipping, returns, warranty, payment, USA',
        'FAQ | USA Electronics Hub',
        'faq', 36, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (72, now(), now(),
        '<h1>Questions Fréquemment Posées (FAQ)</h1><p>Trouvez ici les réponses aux questions courantes concernant les achats chez USA Electronics Hub.</p><h2>Expédition</h2><p><strong>Q : D''où expédiez-vous ?</strong><br>R : Nous expédions depuis divers centres de distribution aux États-Unis.</p><p><strong>Q : Combien de temps prend l''expédition ?</strong><br>R : L''expédition standard prend généralement 3 à 7 jours ouvrables aux États-Unis continentaux.</p><h2>Garantie & Retours</h2><p><strong>Q : Quelle est votre politique de retour ?</strong><br>R : Nous offrons une politique de retour de 30 jours pour la plupart des articles. Consultez nos <a href="/conditions-generales">Conditions Générales</a> pour plus de détails.</p><p><strong>Q : Comment faire une réclamation de garantie ?</strong><br>R : Veuillez contacter notre support client avec les détails de votre commande et la description du problème.</p><h2>Paiement</h2><p><strong>Q : Quels modes de paiement acceptez-vous ?</strong><br>R : Nous acceptons les principales cartes de crédit américaines (Visa, MasterCard, Amex, Discover), PayPal et d''autres méthodes indiquées lors du paiement.</p>',
        'FAQ', 'FAQ | USA Electronics Hub',
        'Réponses aux questions courantes sur l''expédition, les retours, la garantie et les paiements chez USA Electronics Hub.',
        'faq, USA Electronics Hub, aide, support, questions, électronique, expédition, retours, garantie, paiement, USA',
        'FAQ | USA Electronics Hub',
        'faq', 36, 'fr')
on conflict (description_id) do nothing;

-- Box: header-message
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (37, 'header-message', 'BOX', false, 7, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (73, now(), now(),
        'Proudly American! Free Standard US Shipping on orders over $50!',
        'Header Message', 'Header Message',
        '', '', '', '', 37, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (74, now(), now(),
        'Fièrement Américain !Livraison Standard Gratuite aux USA dès 50$ d''achat !',
        'Message d''En-tête', 'Message d''En-tête',
        '', '', '', '', 37, 'fr')
on conflict (description_id) do nothing;

-- Box: agreement
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (38, 'agreement', 'BOX', false, 8, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (75, now(), now(),
        '<p style="text-align:center; font-size:0.9em; padding:5px;">By using this site, you agree to our <a href="/terms">Terms & Conditions</a>.</p>',
        'User Agreement', 'User Agreement',
        '', '', '', '', 38, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (76, now(), now(),
        '<p style="text-align:center; font-size:0.9em; padding:5px;">En utilisant ce site, vous acceptez nos <a href="/conditions-generales">Conditions Générales</a>.</p>',
        'Accord Utilisateur', 'Accord Utilisateur',
        '', '', '', '', 38, 'fr')
on conflict (description_id) do nothing;

-- Box: meta-title
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (39, 'meta-title', 'BOX', false, 9, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (77, now(), now(),
        'USA Electronics Hub | Latest Tech & Gadgets in America',
        '', '', '', '', '', '', 39, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (78, now(), now(),
        'USA Electronics Hub | Tech & Gadgets de Pointe en Amérique',
        '', '', '', '', '', '', 39, 'fr')
on conflict (description_id) do nothing;

-- Box: meta-description
INSERT INTO merchant.content (content_id, code, content_type,
                             link_to_menu, sort_order, visible, store_merchant_id)
VALUES (40, 'meta-description', 'BOX', false, 10, true, '65f023632bc46470c104b75f')
on conflict (content_id) do nothing;

INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (79, now(), now(),
        'Shop the latest electronics, computers, TVs, and tech gadgets at USA Electronics Hub. Your trusted American source for top brands and fast US shipping.',
        '', '', '', '', '', '', 40, 'en')
on conflict (description_id) do nothing;
INSERT INTO merchant.content_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, sef_url, content_id,
                                         language_code)
VALUES (80, now(), now(),
        'Achetez les derniers appareils électroniques, ordinateurs, téléviseurs et gadgets technologiques chez USA Electronics Hub. Votre source américaine de confiance pour les grandes marques et une expédition rapide aux États-Unis.',
        '', '', '', '', '', '', 40, 'fr')
on conflict (description_id) do nothing;

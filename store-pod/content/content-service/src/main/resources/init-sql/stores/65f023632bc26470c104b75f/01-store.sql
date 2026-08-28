/*
Generated SQL inserts for a new store based on the original template.
Store ID: '65f023632bc26470c104b75f'
Languages: ['ar', 'fr']
Country: 'EG'
Domain: cars
*/

-- Define parameters for clarity (These would typically be replaced by actual values or generator logic)
-- Let's assume:
-- $paramter.store_id = '65f023632bc26470c104b75f'
-- $paramter.country_id = 'EG'
-- $parameter.languages = ['ar', 'fr']
-- $generator.currencyForCountry('EG') resolves to the currency_id for EGP (e.g., 10 - assuming this ID exists)
-- New Org ID generated: '32a034a43cd77581d105c87a'

-- Specify conflict target for clarity

-- Loop over languages: 'ar', 'fr'
-- Specify conflict target

-- Specify conflict target

-- Loop for slider images (0 to 4)
-- Loop for social links ('FACEBOOK','X','INSTAGRAM','TIKTOK')
/*
Generated content for store_id='65f023632bc26470c104b75f' (Cars Domain) store name Egypt Car Sales
Pages: ['about-us', 'contact-us', 'terms', 'privacy', 'location', 'faq']
Boxes: ['header-message', 'agreement','meta-title','meta-description']
Languages: ['ar', 'fr']
Starting content_id: 21
Starting description_id: 41
Starting sort_order: 1
*/

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id, status, version)
VALUES (21, 'about-us', 'PAGE', 1, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1)
on conflict (content_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (41, now(), now(),
        '<h1>عن إيجيبت كار سيلز</h1><p>مرحبًا بكم في إيجيبت كار سيلز، وجهتكم الموثوقة لشراء وبيع السيارات في مصر. نقدم مجموعة واسعة من السيارات الجديدة والمستعملة عالية الجودة لتلبية جميع احتياجاتكم.</p><h2>مهمتنا</h2><p>توفير تجربة شفافة ومريحة لعملائنا في سوق السيارات المصري، مع التركيز على الجودة والأسعار التنافسية.</p><h2>لماذا تختارنا؟</h2><p>خبرة واسعة في السوق المحلي، تشكيلة متنوعة، خدمة عملاء متميزة.</p>',
        'عن الشركة', 'عن إيجيبت كار سيلز',
        'تعرف على إيجيبت كار سيلز، خبرتنا في سوق السيارات المصري ومهمتنا لخدمة عملائنا.',
        'سيارات, مصر, إيجيبت كار سيلز, عن الشركة, شراء سيارات, سيارات مستعملة, سيارات جديدة',
        'عن الشركة | إيجيبت كار سيلز',
        'about-us', 21, 'ar')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (42, now(), now(),
        '<h1>À Propos d''Egypt Car Sales</h1><p>Bienvenue chez Egypt Car Sales, votre destination de confiance pour l''achat et la vente de voitures en Égypte. Nous proposons une large sélection de véhicules neufs et d''occasion de haute qualité pour répondre à tous vos besoins.</p><h2>Notre Mission</h2><p>Offrir une expérience transparente et pratique à nos clients sur le marché automobile égyptien, en mettant l''accent sur la qualité et des prix compétitifs.</p><h2>Pourquoi Nous Choisir ?</h2><p>Vaste expérience du marché local, sélection variée, service client exceptionnel.</p>',
        'À Propos', 'À Propos d''Egypt Car Sales',
        'Découvrez Egypt Car Sales, notre expertise sur le marché automobile égyptien et notre mission au service de nos clients.',
        'voitures, égypte, egypt car sales, à propos, achat voiture, voitures occasion, voitures neuves',
        'À Propos | Egypt Car Sales',
        'a-propos', 21, 'fr')
on conflict (description_id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id, status, version)
VALUES (22, 'contact-us', 'PAGE', 2, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1)
on conflict (content_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (43, now(), now(),
        '<h1>اتصل بنا</h1><p>هل لديك سؤال حول سيارة معينة، أو تحتاج إلى مساعدة في عملية الشراء أو البيع؟ فريق إيجيبت كار سيلز جاهز للمساعدة.</p><h2>معلومات الاتصال:</h2><ul><li>البريد الإلكتروني: info@egyptcarsales.eg</li><li>الهاتف: 01xxxxxxxxx (مصر)</li><li>العنوان: [أضف عنوانًا في مصر، مثال: شارع التسعين، التجمع الخامس، القاهرة]</li><li>ساعات العمل: السبت - الخميس، 10 صباحًا - 6 مساءً</li></ul><!-- Contact Form Placeholder -->',
        'اتصل بنا', 'اتصل بإيجيبت كار سيلز',
        'تواصل مع فريق إيجيبت كار سيلز للاستفسارات حول السيارات، المبيعات، أو الخدمة في مصر.',
        'اتصل بنا, إيجيبت كار سيلز, خدمة العملاء, مساعدة, استفسار سيارات, مصر', 'اتصل بنا | إيجيبت كار سيلز',
        'contact-us', 22, 'ar')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (44, now(), now(),
        '<h1>Contactez-Nous</h1><p>Vous avez une question sur une voiture spécifique, ou besoin d''aide pour l''achat ou la vente ? L''équipe d''Egypt Car Sales est prête à vous aider.</p><h2>Coordonnées :</h2><ul><li>Email : info@egyptcarsales.eg</li><li>Téléphone : 01xxxxxxxxx (Égypte)</li><li>Adresse : [Ajouter une adresse en Égypte, ex: 90 Street, Fifth Settlement, Le Caire]</li><li>Heures d''ouverture : Samedi - Jeudi, 10h - 18h</li></ul><!-- Placeholder Formulaire de Contact -->',
        'Contactez-Nous', 'Contactez Egypt Car Sales',
        'Contactez l''équipe d''Egypt Car Sales pour toute question concernant les voitures, les ventes ou le service en Égypte.',
        'contact, egypt car sales, service client, aide, question voiture, égypte', 'Contactez-Nous | Egypt Car Sales',
        'contactez-nous', 22, 'fr')
on conflict (description_id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id, status, version)
VALUES (23, 'terms', 'PAGE', 3, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1)
on conflict (content_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (45, now(), now(),
        '<h1>الشروط والأحكام</h1><p>يرجى قراءة الشروط والأحكام بعناية قبل استخدام موقع إيجيبت كار سيلز أو إجراء أي معاملة.</p><h2>1. استخدام الموقع</h2><p>استخدامك لهذا الموقع يعني موافقتك على هذه الشروط.</p><h2>2. عرض وشراء المركبات</h2><p>المعلومات المعروضة عن السيارات هي لأغراض إعلامية وقد تخضع للتغيير. تخضع عمليات الشراء لعقود منفصلة.</p><h2>3. مسؤولية المستخدم</h2><p>المستخدم مسؤول عن صحة المعلومات المقدمة وعن أي استخدام للموقع.</p>',
        'الشروط والأحكام', 'الشروط والأحكام | إيجيبت كار سيلز',
        'اقرأ الشروط والأحكام الخاصة باستخدام موقع وخدمات إيجيبت كار سيلز في مصر.',
        'شروط, أحكام, إيجيبت كار سيلز, قانوني, اتفاقية, شراء سيارة, مصر', 'الشروط والأحكام | إيجيبت كار سيلز',
        'terms', 23, 'ar')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (46, now(), now(),
        '<h1>Conditions Générales</h1><p>Veuillez lire attentivement les conditions générales avant d''utiliser le site web d''Egypt Car Sales ou d''effectuer toute transaction.</p><h2>1. Utilisation du Site</h2><p>Votre utilisation de ce site implique votre acceptation de ces conditions.</p><h2>2. Affichage et Achat de Véhicules</h2><p>Les informations affichées sur les voitures sont à titre informatif et peuvent être sujettes à modification. Les achats sont régis par des contrats distincts.</p><h2>3. Responsabilité de l''Utilisateur</h2><p>L''utilisateur est responsable de l''exactitude des informations fournies et de toute utilisation du site.</p>',
        'Conditions Générales', 'Conditions Générales | Egypt Car Sales',
        'Lisez les conditions générales d''utilisation du site et des services d''Egypt Car Sales en Égypte.',
        'conditions, termes, egypt car sales, légal, accord, achat voiture, égypte',
        'Conditions Générales | Egypt Car Sales',
        'conditions-generales', 23, 'fr')
on conflict (description_id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id, status, version)
VALUES (24, 'privacy', 'PAGE', 4, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1)
on conflict (content_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (47, now(), now(),
        '<h1>سياسة الخصوصية</h1><p>في إيجيبت كار سيلز، نحترم خصوصيتك ونلتزم بحماية بياناتك الشخصية. توضح هذه السياسة كيفية تعاملنا مع معلوماتك.</p><h2>جمع المعلومات</h2><p>نقوم بجمع المعلومات التي تقدمها لنا (مثل الاسم، رقم الهاتف، البريد الإلكتروني) والمعلومات التي يتم جمعها تلقائيًا عند استخدامك للموقع.</p><h2>استخدام المعلومات</h2><p>نستخدم معلوماتك لتسهيل عمليات البيع والشراء، التواصل معك، وتحسين خدماتنا.</p><h2>مشاركة المعلومات</h2><p>لا نشارك معلوماتك الشخصية مع أطراف ثالثة إلا بموافقتك أو عند الضرورة القانونية.</p>',
        'سياسة الخصوصية', 'سياسة الخصوصية | إيجيبت كار سيلز',
        'تعرف على كيفية قيام إيجيبت كار سيلز بجمع واستخدام وحماية معلوماتك الشخصية في مصر.',
        'خصوصية, إيجيبت كار سيلز, بيانات شخصية, حماية البيانات, أمان, مصر', 'سياسة الخصوصية | إيجيبت كار سيلز',
        'privacy', 24, 'ar')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (48, now(), now(),
        '<h1>Politique de Confidentialité</h1><p>Chez Egypt Car Sales, nous respectons votre vie privée et nous nous engageons à protéger vos données personnelles. Cette politique explique comment nous traitons vos informations.</p><h2>Collecte d''Informations</h2><p>Nous collectons les informations que vous nous fournissez (telles que nom, numéro de téléphone, email) et les informations collectées automatiquement lors de votre utilisation du site.</p><h2>Utilisation des Informations</h2><p>Nous utilisons vos informations pour faciliter les processus de vente et d''achat, communiquer avec vous et améliorer nos services.</p><h2>Partage des Informations</h2><p>Nous ne partageons pas vos informations personnelles avec des tiers sans votre consentement ou lorsque la loi l''exige.</p>',
        'Politique de Confidentialité', 'Politique de Confidentialité | Egypt Car Sales',
        'Comprenez comment Egypt Car Sales collecte, utilise et protège vos informations personnelles en Égypte.',
        'confidentialité, egypt car sales, données personnelles, protection données, sécurité, égypte',
        'Politique de Confidentialité | Egypt Car Sales',
        'politique-confidentialite', 24, 'fr')
on conflict (description_id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id, status, version)
VALUES (25, 'location', 'PAGE', 5, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1)
on conflict (content_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (49, now(), now(),
        '<h1>موقعنا</h1><p>تفضل بزيارة معرضنا الرئيسي في القاهرة للاطلاع على تشكيلتنا المختارة من السيارات.</p><h2>معرض القاهرة</h2><p>[أضف عنوانًا في مصر، مثال: شارع التسعين، التجمع الخامس، القاهرة]<br>ساعات العمل: السبت - الخميس، 10 صباحًا - 6 مساءً</p><p><i>يمكنك أيضًا تصفح وشراء سياراتنا عبر الإنترنت.</i></p><!-- Map Placeholder -->',
        'موقعنا', 'موقع معرض إيجيبت كار سيلز',
        'اعثر على موقع معرض إيجيبت كار سيلز الرئيسي في القاهرة وساعات العمل.',
        'موقع, معرض, إيجيبت كار سيلز, عنوان, ساعات العمل, القاهرة, مصر', 'موقعنا | إيجيبت كار سيلز',
        'location', 25, 'ar')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (50, now(), now(),
        '<h1>Notre Emplacement</h1><p>Visitez notre showroom principal au Caire pour découvrir notre sélection de voitures.</p><h2>Showroom Le Caire</h2><p>[Ajouter une adresse en Égypte, ex: 90 Street, Fifth Settlement, Le Caire]<br>Heures d''ouverture : Samedi - Jeudi, 10h - 18h</p><p><i>Vous pouvez également parcourir et acheter nos voitures en ligne.</i></p><!-- Placeholder Carte -->',
        'Notre Emplacement', 'Emplacement Showroom Egypt Car Sales',
        'Trouvez l''emplacement et les heures d''ouverture du showroom principal d''Egypt Car Sales au Caire.',
        'emplacement, showroom, egypt car sales, adresse, heures ouverture, Le Caire, égypte',
        'Notre Emplacement | Egypt Car Sales',
        'notre-emplacement', 25, 'fr')
on conflict (description_id) do nothing;

INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id, status, version)
VALUES (26, 'faq', 'PAGE', 6, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1)
on conflict (content_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (51, now(), now(),
        '<h1>الأسئلة الشائعة</h1><p>تجد هنا إجابات للأسئلة الأكثر شيوعًا حول شراء وبيع السيارات من خلال إيجيبت كار سيلز.</p><h2>الشراء</h2><p><strong>س: ما هي المستندات المطلوبة لشراء سيارة مستعملة؟</strong><br>ج: بطاقة الرقم القومي سارية المفعول، وقد تطلب مستندات إضافية حسب حالة السيارة ونظام الدفع.</p><h2>البيع</h2><p><strong>س: كيف يمكنني عرض سيارتي للبيع؟</strong><br>ج: يمكنك التواصل معنا لتقييم سيارتك وعرضها من خلال منصتنا.</p><h2>التمويل</h2><p><strong>س: هل تساعدون في إجراءات التمويل البنكي؟</strong><br>ج: يمكننا تقديم المشورة وتوجيهك للجهات المختصة بالتمويل.</p>',
        'الأسئلة الشائعة', 'الأسئلة الشائعة | إيجيبت كار سيلز',
        'إجابات للأسئلة الشائعة حول شراء وبيع وتمويل السيارات عبر إيجيبت كار سيلز في مصر.',
        'أسئلة شائعة, إيجيبت كار سيلز, مساعدة, شراء سيارة, بيع سيارة, تمويل, مصر', 'الأسئلة الشائعة | إيجيبت كار سيلز',
        'faq', 26, 'ar')
on conflict (description_id) do nothing;
INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (52, now(), now(),
        '<h1>Questions Fréquemment Posées (FAQ)</h1><p>Trouvez ici les réponses aux questions les plus courantes concernant l''achat et la vente de voitures via Egypt Car Sales.</p><h2>Achat</h2><p><strong>Q : Quels documents sont nécessaires pour acheter une voiture d''occasion ?</strong><br>R : Une carte d''identité nationale valide. Des documents supplémentaires peuvent être requis selon l''état de la voiture et le mode de paiement.</p><h2>Vente</h2><p><strong>Q : Comment puis-je proposer ma voiture à la vente ?</strong><br>R : Vous pouvez nous contacter pour évaluer votre voiture et la proposer via notre plateforme.</p><h2>Financement</h2><p><strong>Q : Aidez-vous avec les procédures de financement bancaire ?</strong><br>R : Nous pouvons vous conseiller et vous orienter vers les entités de financement compétentes.</p>',
        'FAQ', 'FAQ | Egypt Car Sales',
        'Réponses aux questions fréquemment posées sur l''achat, la vente et le financement de voitures via Egypt Car Sales en Égypte.',
        'faq, egypt car sales, aide, achat voiture, vente voiture, financement, égypte', 'FAQ | Egypt Car Sales',
        'faq', 26, 'fr')
on conflict (description_id) do nothing;

-- The store's appearance record. metaTitle/metaDescription were the `meta-title` and `meta-description` BOX rows;
-- they are per-locale maps here. Logo and favicon stay empty until the seller uploads one.
INSERT INTO content.site_settings (store_merchant_id, seo, social_links, updated_at)
VALUES ('65f023632bc26470c104b75f', '{"metaTitle": {"ar": "إيجيبت كار سيلز | شراء وبيع السيارات الجديدة والمستعملة في مصر", "fr": "Egypt Car Sales | Achat et Vente de Voitures Neuves et d''Occasion en Égypte"}, "metaDescription": {"ar": "منصة موثوقة لشراء وبيع السيارات في مصر. تصفح تشكيلة واسعة من السيارات الجديدة والمستعملة بأسعار تنافسية وجودة مضمونة مع إيجيبت كار سيلز.", "fr": "Plateforme de confiance pour l''achat et la vente de voitures en Égypte. Découvrez une large sélection de véhicules neufs et d''occasion à des prix compétitifs et une qualité garantie avec Egypt Car Sales."}}'::jsonb, '[]'::jsonb, now())
on conflict (store_merchant_id) do nothing;

-- The announcement strip, which the `header-message` BOX used to carry. Negative ids are deliberate: content ids
-- come from SM_SEQUENCER and only ever grow upward, so seed-only rows below zero can never collide.
-- The message sits in `description`, not `name`: a STRIP banner shows no artwork and no headline, so its body is
-- what the shopper reads (rich, so it can link) and `name` is only the label the console list shows.
INSERT INTO content.content (content_id, code, content_type, sort_order, visible, store_merchant_id,
                             status, version, placement, meta)
VALUES (-12, 'announcement', 'BANNER', 0, true, '65f023632bc26470c104b75f', 'PUBLISHED', 1, 'STRIP',
        '{"target": null, "artwork": null, "theme": null, "loggedInOnly": false}')
on conflict (content_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (-28, now(), now(), 'عروض حصرية! تصفح أحدث السيارات المستعملة بحالة ممتازة وأسعار لا تقبل المنافسة.', 'Announcement', 'Announcement',
        '', '', '', '', -12, 'ar')
on conflict (description_id) do nothing;

INSERT INTO content.content_description (description_id, date_created, date_modified, description, name, title,
                                          meta_description, meta_keywords, meta_title, sef_url, content_id,
                                          language_code)
VALUES (-27, now(), now(), 'Offres Exclusives! Découvrez nos dernières voitures d''occasion en excellent état à des prix imbattables.', 'Announcement', 'Announcement',
        '', '', '', '', -12, 'fr')
on conflict (description_id) do nothing;

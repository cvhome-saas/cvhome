/*
Generated SQL inserts for catalog.category and catalog.category_description
Store ID: '65f023632bc26470c104b75f'
Languages: ['ar','fr']
Starting category_id: 25
Starting description_id: 49
Number of categories: 12
Domain: Cars
*/

-- Categories (12 total, IDs 25-36)
INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (25, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'sedans.jpg', true, 'SEDANS', 0,
        true, '/25/', 0, true, '65f023632bc26470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (26, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'suvs.jpg', true, 'SUVS', 0,
        true, '/26/', 1, true, '65f023632bc26470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (27, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'electric.jpg', true, 'ELECTRIC_CARS', 0,
        true, '/27/', 2, true, '65f023632bc26470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (28, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'used.jpg', true, 'USED_CARS', 0,
        false, '/28/', 3, true, '65f023632bc26470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (29, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'parts_accessories.jpg', true,
        'PARTS_ACCESSORIES', 0,
        false, '/29/', 4, true, '65f023632bc26470c104b75f', null) -- Parent Category
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (30, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'parts.jpg', true, 'SPARE_PARTS', 1,
        false, '/29/30/', 0, true, '65f023632bc26470c104b75f', 29) -- Child of 29
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (31, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'accessories.jpg', true, 'ACCESSORIES', 1,
        false, '/29/31/', 1, true, '65f023632bc26470c104b75f', 29) -- Child of 29, Parent for 32, 33
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (32, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'interior.jpg', true, 'INTERIOR_ACCESSORIES', 2,
        false, '/29/31/32/', 0, true, '65f023632bc26470c104b75f', 31) -- Child of 31
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (33, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'exterior.jpg', true, 'EXTERIOR_ACCESSORIES', 2,
        false, '/29/31/33/', 1, true, '65f023632bc26470c104b75f', 31) -- Child of 31
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (34, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'tires.jpg', true, 'TIRES_WHEELS', 1,
        false, '/29/34/', 2, true, '65f023632bc26470c104b75f', 29) -- Child of 29
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (35, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'maintenance.jpg', true, 'MAINTENANCE', 0,
        false, '/35/', 5, true, '65f023632bc26470c104b75f', null)
on conflict (category_id) do nothing;

INSERT INTO catalog.category (category_id, date_created, date_modified, category_image, category_status, code,
                              depth, featured, lineage, sort_order, visible, store_merchant_id, parent_id)
VALUES (36, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'luxury.jpg', true, 'LUXURY_CARS', 0,
        false, '/36/', 6, true, '65f023632bc26470c104b75f', null)
on conflict (category_id) do nothing;


-- Category Descriptions (12 categories * 2 languages = 24 descriptions, IDs 49-72)

-- Category 25: SEDANS
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (49, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'تصفح مجموعتنا الواسعة من سيارات السيدان.',
        'سيارات سيدان', 'سيارات سيدان للبيع', 'أفضل العروض على السيدان', 'ابحث عن سيارة السيدان المثالية لك.',
        'سيدان, سيارات عائلية, سيارات اقتصادية', 'سيارات سيدان جديدة ومستعملة',
        'سيارات-سيدان', 'ar', 25)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (50, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'Parcourez notre large sélection de berlines.',
        'Berlines', 'Berlines à vendre', 'Meilleures offres sur les berlines', 'Trouvez la berline parfaite pour vous.',
        'berline, voitures familiales, voitures économiques', 'Berlines neuves et d''occasion',
        'berlines', 'fr', 25)
on conflict (description_id) do nothing;

-- Category 26: SUVS
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (51, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'اكتشف سيارات الدفع الرباعي والكروس أوفر.',
        'سيارات دفع رباعي', 'سيارات دفع رباعي للبيع', 'مساحة وراحة', 'سيارات دفع رباعي لجميع التضاريس.',
        'دفع رباعي, كروس أوفر, سيارات كبيرة', 'سيارات دفع رباعي جديدة ومستعملة',
        'سيارات-دفع-رباعي', 'ar', 26)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (52, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'Découvrez nos VUS et crossovers.', 'VUS',
        'VUS à vendre', 'Espace et confort', 'VUS pour tous les terrains.', 'VUS, crossover, grandes voitures',
        'VUS neufs et d''occasion',
        'vus', 'fr', 26)
on conflict (description_id) do nothing;

-- Category 27: ELECTRIC_CARS
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (53, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'انضم إلى مستقبل القيادة مع السيارات الكهربائية.', 'سيارات كهربائية', 'سيارات كهربائية للبيع', 'صديقة للبيئة',
        'سيارات كهربائية وهجينة.', 'سيارات كهربائية, EV, هجين', 'سيارات كهربائية جديدة',
        'سيارات-كهربائية', 'ar', 27)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (54, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Rejoignez l''avenir de la conduite avec les voitures électriques.', 'Voitures Électriques',
        'Voitures électriques à vendre', 'Écologique', 'Voitures électriques et hybrides.',
        'voitures électriques, EV, hybride', 'Voitures électriques neuves',
        'voitures-electriques', 'fr', 27)
on conflict (description_id) do nothing;

-- Category 28: USED_CARS
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (55, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'سيارات مستعملة معتمدة بجودة عالية وأسعار رائعة.', 'سيارات مستعملة', 'سيارات مستعملة للبيع', 'صفقات رائعة',
        'ابحث عن سيارات مستعملة موثوقة.', 'سيارات مستعملة, سيارات رخيصة', 'سيارات مستعملة معتمدة',
        'سيارات-مستعملة', 'ar', 28)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (56, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Voitures d''occasion certifiées de haute qualité à des prix avantageux.', 'Voitures d''Occasion',
        'Voitures d''occasion à vendre', 'Bonnes affaires', 'Trouvez des voitures d''occasion fiables.',
        'voitures occasion, voitures pas chères', 'Voitures d''occasion certifiées',
        'voitures-occasion', 'fr', 28)
on conflict (description_id) do nothing;

-- Category 29: PARTS_ACCESSORIES
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (57, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'كل ما تحتاجه لسيارتك من قطع غيار وإكسسوارات.',
        'قطع غيار وإكسسوارات', 'قطع غيار وإكسسوارات السيارات', null, 'قطع غيار وإكسسوارات لجميع أنواع السيارات.',
        'قطع غيار, إكسسوارات, سيارات', 'قطع غيار وإكسسوارات السيارات',
        'قطع-غيار-واكسسوارات', 'ar', 29)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (58, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Tout ce dont vous avez besoin pour votre voiture en pièces et accessoires.', 'Pièces & Accessoires',
        'Pièces et accessoires auto', null, 'Pièces et accessoires pour tous types de voitures.',
        'pièces détachées, accessoires, auto', 'Pièces et accessoires automobiles',
        'pieces-accessoires', 'fr', 29)
on conflict (description_id) do nothing;

-- Category 30: SPARE_PARTS
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (59, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'قطع غيار أصلية وموثوقة لسيارتك.', 'قطع غيار',
        'قطع غيار سيارات', null, 'ابحث عن قطع الغيار المناسبة لسيارتك.', 'قطع غيار, محركات, فرامل',
        'قطع غيار سيارات أصلية',
        'قطع-غيار', 'ar', 30)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (60, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Pièces détachées d''origine et fiables pour votre voiture.', 'Pièces Détachées', 'Pièces détachées auto', null,
        'Trouvez les bonnes pièces pour votre voiture.', 'pièces détachées, moteurs, freins',
        'Pièces détachées auto d''origine',
        'pieces-detachees', 'fr', 30)
on conflict (description_id) do nothing;

-- Category 31: ACCESSORIES
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (61, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'إكسسوارات لتحسين مظهر وأداء سيارتك.',
        'إكسسوارات', 'إكسسوارات سيارات', null, 'إكسسوارات داخلية وخارجية.', 'إكسسوارات, سيارات, تزيين',
        'إكسسوارات سيارات',
        'اكسسوارات-سيارات', 'ar', 31)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (62, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Accessoires pour améliorer l''apparence et les performances de votre voiture.', 'Accessoires',
        'Accessoires auto', null, 'Accessoires intérieurs et extérieurs.', 'accessoires, auto, tuning',
        'Accessoires automobiles',
        'accessoires-auto', 'fr', 31)
on conflict (description_id) do nothing;

-- Category 32: INTERIOR_ACCESSORIES
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (63, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'إكسسوارات داخلية لراحة وأناقة مقصورة سيارتك.',
        'إكسسوارات داخلية', 'إكسسوارات سيارات داخلية', null, 'أغطية مقاعد, دواسات, أنظمة صوت.',
        'إكسسوارات داخلية, مقاعد, صوت', 'إكسسوارات داخلية للسيارات',
        'اكسسوارات-داخلية', 'ar', 32)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (64, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Accessoires intérieurs pour le confort et le style de votre habitacle.', 'Accessoires Intérieurs',
        'Accessoires auto intérieurs', null, 'Housses de siège, tapis, systèmes audio.',
        'accessoires intérieurs, sièges, audio', 'Accessoires intérieurs auto',
        'accessoires-interieurs', 'fr', 32)
on conflict (description_id) do nothing;

-- Category 33: EXTERIOR_ACCESSORIES
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (65, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'إكسسوارات خارجية لحماية وتحسين مظهر سيارتك.',
        'إكسسوارات خارجية', 'إكسسوارات سيارات خارجية', null, 'أغطية سيارات, مصابيح, جنوط.',
        'إكسسوارات خارجية, حماية, جنوط', 'إكسسوارات خارجية للسيارات',
        'اكسسوارات-خارجية', 'ar', 33)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (66, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Accessoires extérieurs pour protéger et améliorer l''apparence de votre voiture.', 'Accessoires Extérieurs',
        'Accessoires auto extérieurs', null, 'Housses de voiture, phares, jantes.',
        'accessoires extérieurs, protection, jantes', 'Accessoires extérieurs auto',
        'accessoires-exterieurs', 'fr', 33)
on conflict (description_id) do nothing;

-- Category 34: TIRES_WHEELS
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (67, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'مجموعة واسعة من الإطارات والجنوط لجميع أنواع السيارات.', 'إطارات وجنوط', 'إطارات وجنوط سيارات', null,
        'إطارات صيفية, شتوية, لجميع الفصول.', 'إطارات, جنوط, عجلات', 'إطارات وجنوط سيارات',
        'اطارات-وجنوط', 'ar', 34)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (68, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Large sélection de pneus et jantes pour tous types de voitures.', 'Pneus & Jantes', 'Pneus et jantes auto',
        null, 'Pneus été, hiver, toutes saisons.', 'pneus, jantes, roues', 'Pneus et jantes auto',
        'pneus-jantes', 'fr', 34)
on conflict (description_id) do nothing;

-- Category 35: MAINTENANCE
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (69, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000', 'خدمات صيانة للحفاظ على سيارتك في أفضل حالة.',
        'صيانة سيارات', 'خدمات صيانة سيارات', null, 'تغيير زيت, فحص فرامل, والمزيد.', 'صيانة, خدمة, سيارات',
        'خدمات صيانة سيارات',
        'صيانة-سيارات', 'ar', 35)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (70, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Services d''entretien pour maintenir votre voiture en parfait état.', 'Entretien Auto',
        'Services d''entretien auto', null, 'Vidange, contrôle des freins, et plus.', 'entretien, service, auto',
        'Services d''entretien automobile',
        'entretien-auto', 'fr', 35)
on conflict (description_id) do nothing;

-- Category 36: LUXURY_CARS
INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (71, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'مجموعة مختارة من السيارات الفاخرة لأصحاب الذوق الرفيع.', 'سيارات فاخرة', 'سيارات فاخرة للبيع', 'فخامة وأداء',
        'سيارات فاخرة من أفضل العلامات التجارية.', 'سيارات فاخرة, سيارات رياضية, فخامة', 'سيارات فاخرة جديدة ومستعملة',
        'سيارات-فاخرة', 'ar', 36)
on conflict (description_id) do nothing;

INSERT INTO catalog.category_description (description_id, date_created, date_modified, description, name, title,
                                          category_highlight, meta_description, meta_keywords, meta_title, sef_url,
                                          language_code, category_id)
VALUES (72, '2024-04-02 12:00:00.000000', '2024-04-02 12:00:00.000000',
        'Une sélection de voitures de luxe pour les connaisseurs.', 'Voitures de Luxe', 'Voitures de luxe à vendre',
        'Luxe et performance', 'Voitures de luxe des meilleures marques.', 'voitures de luxe, voitures de sport, luxe',
        'Voitures de luxe neuves et d''occasion',
        'voitures-luxe', 'fr', 36)
on conflict (description_id) do nothing;
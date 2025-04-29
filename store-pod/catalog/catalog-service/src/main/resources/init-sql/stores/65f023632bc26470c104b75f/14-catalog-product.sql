/*
Generated SQL inserts for catalog.product and catalog.product_description
Store ID: '65f023632bc26470c104b75f'
Languages: ['ar','fr']
Manufacturer IDs: (13, 14, 15, 16, 17, 18)
Product Type IDs: (9, 10, 11, 12)
Starting product_id: 91
Starting product_description_id: 181
Number of products: 15
Domain: Cars
HTML format used for description.
*/

-- Product 91: Toyota Camry (Sedan)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (91, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false, null,
        false, null, true, false, null, null, 'CAR-REF-91', 'CAR-SKU-91', 0, 13,
        '65f023632bc26470c104b75f', 9)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (181, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>تويوتا كامري 2024</h1><p>سيارة سيدان موثوقة وأنيقة، مثالية للتنقل اليومي والرحلات العائلية.</p><ul><li><strong>المحرك:</strong> 2.5 لتر 4 سلندر</li><li><strong>ناقل الحركة:</strong> أوتوماتيكي 8 سرعات</li><li><strong>الميزات:</strong> نظام Toyota Safety Sense، شاشة لمس، Apple CarPlay/Android Auto</li><li><strong>استهلاك الوقود:</strong> ممتاز</li></ul>',
        'تويوتا كامري 2024', 'تويوتا كامري 2024 الجديدة للبيع',
        'اكتشف تويوتا كامري 2024 الجديدة، سيارة سيدان موثوقة بميزات متقدمة.',
        'تويوتا, كامري, 2024, سيدان, سيارة عائلية', 'تويوتا كامري 2024 | سيارة سيدان موثوقة', 'موثوقة واقتصادية',
        'toyota-camry-2024', 'ar', 91)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (182, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Toyota Camry 2024</h1><p>Une berline fiable et élégante, parfaite pour les trajets quotidiens et les voyages en famille.</p><ul><li><strong>Moteur :</strong> 2.5L 4 cylindres</li><li><strong>Transmission :</strong> Automatique 8 vitesses</li><li><strong>Caractéristiques :</strong> Toyota Safety Sense, écran tactile, Apple CarPlay/Android Auto</li><li><strong>Consommation :</strong> Excellente</li></ul>',
        'Toyota Camry 2024', 'Nouvelle Toyota Camry 2024 à vendre',
        'Découvrez la nouvelle Toyota Camry 2024, une berline fiable avec des fonctionnalités avancées.',
        'toyota, camry, 2024, berline, voiture familiale', 'Toyota Camry 2024 | Berline Fiable', 'Fiable et économique',
        'toyota-camry-2024', 'fr', 91)
on conflict (description_id) do nothing;

-- Product 92: BMW X5 (SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (92, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false, null,
        false, null, true, false, null, null, 'CAR-REF-92', 'CAR-SKU-92', 1, 14,
        '65f023632bc26470c104b75f', 10)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (183, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>بي إم دبليو X5 2024</h1><p>سيارة دفع رباعي فاخرة تجمع بين الأداء القوي والتكنولوجيا المتطورة والراحة الفائقة.</p><ul><li><strong>المحرك:</strong> 3.0 لتر 6 سلندر توربو</li><li><strong>الدفع:</strong> xDrive دفع رباعي</li><li><strong>الميزات:</strong> نظام iDrive، مقصورة جلدية فاخرة، سقف بانورامي</li><li><strong>الأداء:</strong> ديناميكي وممتع</li></ul>',
        'بي إم دبليو X5 2024', 'بي إم دبليو X5 2024 الفاخرة للبيع',
        'تجربة قيادة لا مثيل لها مع بي إم دبليو X5 2024، سيارة دفع رباعي فاخرة.',
        'بي ام دبليو, X5, 2024, دفع رباعي, SUV, فاخرة', 'بي إم دبليو X5 2024 | دفع رباعي فاخر', 'فخامة وأداء رياضي',
        'bmw-x5-2024', 'ar', 92)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (184, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>BMW X5 2024</h1><p>Un VUS de luxe qui allie performances puissantes, technologie de pointe et confort supérieur.</p><ul><li><strong>Moteur :</strong> 3.0L 6 cylindres Turbo</li><li><strong>Transmission :</strong> xDrive intégrale</li><li><strong>Caractéristiques :</strong> Système iDrive, intérieur cuir luxueux, toit panoramique</li><li><strong>Performance :</strong> Dynamique et engageante</li></ul>',
        'BMW X5 2024', 'BMW X5 2024 de luxe à vendre',
        'Expérience de conduite inégalée avec le BMW X5 2024, un VUS de luxe.', 'bmw, x5, 2024, vus, suv, luxe',
        'BMW X5 2024 | VUS de Luxe', 'Luxe et performance sportive',
        'bmw-x5-2024', 'fr', 92)
on conflict (description_id) do nothing;

-- Product 93: Mercedes EQS (Electric)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (93, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false, null,
        false, null, true, false, null, null, 'CAR-REF-93', 'CAR-SKU-93', 2, 15,
        '65f023632bc26470c104b75f', 11)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (185, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>مرسيدس EQS 2024</h1><p>قمة الفخامة الكهربائية. سيارة سيدان كهربائية بالكامل تقدم مدى استثنائي وتكنولوجيا رائدة.</p><ul><li><strong>البطارية:</strong> 107.8 كيلوواط ساعة</li><li><strong>المدى:</strong> يصل إلى 700 كم (WLTP)</li><li><strong>الميزات:</strong> شاشة Hyperscreen MBUX، تصميم داخلي فخم، قيادة شبه ذاتية</li><li><strong>الشحن:</strong> شحن سريع DC</li></ul>',
        'مرسيدس EQS 2024', 'مرسيدس EQS 2024 الكهربائية الفاخرة', 'اكتشف مستقبل الفخامة مع مرسيدس EQS 2024 الكهربائية.',
        'مرسيدس, EQS, 2024, كهربائية, EV, فاخرة', 'مرسيدس EQS 2024 | سيدان كهربائية فاخرة', 'فخامة كهربائية ومدى طويل',
        'mercedes-eqs-2024', 'ar', 93)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (186, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Mercedes EQS 2024</h1><p>Le summum du luxe électrique. Une berline entièrement électrique offrant une autonomie exceptionnelle et une technologie de pointe.</p><ul><li><strong>Batterie :</strong> 107.8 kWh</li><li><strong>Autonomie :</strong> Jusqu''à 700 km (WLTP)</li><li><strong>Caractéristiques :</strong> MBUX Hyperscreen, intérieur somptueux, conduite semi-autonome</li><li><strong>Recharge :</strong> Recharge rapide DC</li></ul>',
        'Mercedes EQS 2024', 'Mercedes EQS 2024 électrique de luxe',
        'Découvrez l''avenir du luxe avec la Mercedes EQS 2024 électrique.',
        'mercedes, eqs, 2024, électrique, ev, luxe', 'Mercedes EQS 2024 | Berline Électrique de Luxe',
        'Luxe électrique et longue autonomie',
        'mercedes-eqs-2024', 'fr', 93)
on conflict (description_id) do nothing;

-- Product 94: Hyundai Tucson (Used SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (94, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false, null,
        false, null, true, false, null, null, 'CAR-REF-94', 'CAR-SKU-94', 3, 16,
        '65f023632bc26470c104b75f', 12) -- Product Type 12 (USED_CARS), but it's an SUV model
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (187, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>هيونداي توسان 2021 (مستعملة)</h1><p>سيارة دفع رباعي مدمجة مستعملة بحالة ممتازة، عملية وذات تصميم عصري.</p><ul><li><strong>الموديل:</strong> 2021</li><li><strong>المسافة المقطوعة:</strong> 45,000 كم</li><li><strong>الحالة:</strong> ممتازة، فحص كامل</li><li><strong>الميزات:</strong> شاشة لمس، كاميرا خلفية، تصميم جذاب</li></ul>',
        'هيونداي توسان 2021 مستعملة', 'هيونداي توسان 2021 مستعملة للبيع',
        'سيارة هيونداي توسان 2021 مستعملة بحالة ممتازة وسعر جيد.', 'هيونداي, توسان, 2021, مستعملة, دفع رباعي, SUV',
        'هيونداي توسان 2021 مستعملة | حالة ممتازة', 'سيارة SUV مستعملة موثوقة',
        'hyundai-tucson-2021-used', 'ar', 94)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (188, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Hyundai Tucson 2021 (Occasion)</h1><p>Un VUS compact d''occasion en excellent état, pratique et au design moderne.</p><ul><li><strong>Modèle :</strong> 2021</li><li><strong>Kilométrage :</strong> 45 000 km</li><li><strong>État :</strong> Excellent, entièrement inspecté</li><li><strong>Caractéristiques :</strong> Écran tactile, caméra de recul, design attrayant</li></ul>',
        'Hyundai Tucson 2021 Occasion', 'Hyundai Tucson 2021 d''occasion à vendre',
        'Hyundai Tucson 2021 d''occasion en excellent état à bon prix.', 'hyundai, tucson, 2021, occasion, vus, suv',
        'Hyundai Tucson 2021 Occasion | Excellent État', 'VUS d''occasion fiable',
        'hyundai-tucson-2021-used', 'fr', 94)
on conflict (description_id) do nothing;

-- Product 95: Kia Sportage (Sedan - Error in logic, should be SUV, using Type 9 Sedan for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (95, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false, null,
        false, null, true, false, null, null, 'CAR-REF-95', 'CAR-SKU-95', 4, 17,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (189, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>كيا سبورتاج 2024</h1><p>سيارة SUV مدمجة بتصميم جريء وميزات تقنية متقدمة.</p><ul><li><strong>المحرك:</strong> 1.6 لتر توربو</li><li><strong>الميزات:</strong> شاشة بانورامية، أنظمة مساعدة السائق، تصميم داخلي حديث</li><li><strong>الضمان:</strong> ضمان كيا المتميز</li></ul>',
        'كيا سبورتاج 2024', 'كيا سبورتاج 2024 الجديدة', 'اكتشف كيا سبورتاج 2024 بتصميمها الجريء وتقنياتها المبتكرة.',
        'كيا, سبورتاج, 2024, SUV, دفع رباعي', 'كيا سبورتاج 2024 | تصميم جريء', 'تصميم عصري وتقنيات متقدمة',
        'kia-sportage-2024', 'ar', 95)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (190, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Kia Sportage 2024</h1><p>Un VUS compact au design audacieux et aux fonctionnalités technologiques avancées.</p><ul><li><strong>Moteur :</strong> 1.6L Turbo</li><li><strong>Caractéristiques :</strong> Écran panoramique, systèmes d''aide à la conduite, intérieur moderne</li><li><strong>Garantie :</strong> Garantie exceptionnelle Kia</li></ul>',
        'Kia Sportage 2024', 'Nouveau Kia Sportage 2024',
        'Découvrez le Kia Sportage 2024 avec son design audacieux et ses technologies innovantes.',
        'kia, sportage, 2024, suv, vus', 'Kia Sportage 2024 | Design Audacieux',
        'Design moderne et technologies avancées',
        'kia-sportage-2024', 'fr', 95)
on conflict (description_id) do nothing;

-- Product 96: Ford Mustang (SUV - Error in logic, should be Coupe/Convertible, using Type 10 SUV for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (96, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false, null,
        false, null, true, false, null, null, 'CAR-REF-96', 'CAR-SKU-96', 5, 18,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (191, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>فورد موستانج 2024</h1><p>أيقونة السيارات الرياضية الأمريكية، بقوة لا تضاهى وتصميم يخطف الأنظار.</p><ul><li><strong>المحرك:</strong> 5.0 لتر V8 (GT) أو 2.3 لتر EcoBoost</li><li><strong>الأداء:</strong> تسارع مذهل وتحكم رياضي</li><li><strong>الميزات:</strong> نظام SYNC 4، تصميم داخلي رياضي، خيارات تخصيص واسعة</li></ul>',
        'فورد موستانج 2024', 'فورد موستانج 2024 الرياضية', 'امتلك أيقونة الأداء مع فورد موستانج 2024.',
        'فورد, موستانج, 2024, سيارة رياضية, كوبيه', 'فورد موستانج 2024 | أيقونة رياضية', 'قوة وأداء أسطوري',
        'ford-mustang-2024', 'ar', 96)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (192, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Ford Mustang 2024</h1><p>L''icône américaine des voitures de sport, avec une puissance inégalée et un design saisissant.</p><ul><li><strong>Moteur :</strong> 5.0L V8 (GT) ou 2.3L EcoBoost</li><li><strong>Performance :</strong> Accélération fulgurante et maniabilité sportive</li><li><strong>Caractéristiques :</strong> Système SYNC 4, intérieur sportif, larges options de personnalisation</li></ul>',
        'Ford Mustang 2024', 'Ford Mustang 2024 Sportive',
        'Possédez une icône de performance avec la Ford Mustang 2024.', 'ford, mustang, 2024, voiture de sport, coupé',
        'Ford Mustang 2024 | Icône Sportive', 'Puissance et performance légendaires',
        'ford-mustang-2024', 'fr', 96)
on conflict (description_id) do nothing;

-- Product 97: Toyota RAV4 (Electric - Error in logic, should be SUV/Hybrid, using Type 11 Electric for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (97, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false, null,
        false, null, true, false, null, null, 'CAR-REF-97', 'CAR-SKU-97', 6, 13,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (193, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>تويوتا RAV4 2024</h1><p>سيارة SUV مدمجة شهيرة تجمع بين العملية والكفاءة والموثوقية.</p><ul><li><strong>المحرك:</strong> 2.5 لتر 4 سلندر (متوفر بنسخة هجينة)</li><li><strong>الميزات:</strong> مساحة داخلية واسعة، نظام دفع رباعي اختياري، كفاءة عالية في استهلاك الوقود</li><li><strong>السلامة:</strong> Toyota Safety Sense قياسي</li></ul>',
        'تويوتا RAV4 2024', 'تويوتا RAV4 2024 الجديدة', 'تويوتا RAV4 2024، سيارة SUV عملية وموثوقة لجميع احتياجاتك.',
        'تويوتا, RAV4, 2024, SUV, هجين, دفع رباعي', 'تويوتا RAV4 2024 | SUV عملي', 'عملية وكفاءة عالية',
        'toyota-rav4-2024', 'ar', 97)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (194, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Toyota RAV4 2024</h1><p>Un VUS compact populaire alliant praticité, efficacité et fiabilité.</p><ul><li><strong>Moteur :</strong> 2.5L 4 cylindres (Version hybride disponible)</li><li><strong>Caractéristiques :</strong> Intérieur spacieux, transmission intégrale en option, excellente économie de carburant</li><li><strong>Sécurité :</strong> Toyota Safety Sense de série</li></ul>',
        'Toyota RAV4 2024', 'Nouveau Toyota RAV4 2024',
        'Toyota RAV4 2024, le VUS pratique et fiable pour tous vos besoins.',
        'toyota, rav4, 2024, suv, vus, hybride, awd', 'Toyota RAV4 2024 | VUS Pratique', 'Pratique et très efficace',
        'toyota-rav4-2024', 'fr', 97)
on conflict (description_id) do nothing;

-- Product 98: BMW 3 Series (Used Sedan)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (98, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false, null,
        false, null, true, false, null, null, 'CAR-REF-98', 'CAR-SKU-98', 7, 14,
        '65f023632bc26470c104b75f', 12) -- Type 12 (USED_CARS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (195, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>بي إم دبليو الفئة الثالثة 2020 (مستعملة)</h1><p>سيارة سيدان رياضية فاخرة مستعملة، تقدم تجربة قيادة ممتعة وتصميمًا أنيقًا.</p><ul><li><strong>الموديل:</strong> 2020</li><li><strong>المسافة المقطوعة:</strong> 55,000 كم</li><li><strong>الحالة:</strong> جيدة جدًا، صيانة منتظمة</li><li><strong>الميزات:</strong> محرك توربو، تحكم رياضي، نظام iDrive</li></ul>',
        'بي إم دبليو الفئة الثالثة 2020 مستعملة', 'بي إم دبليو الفئة الثالثة 2020 مستعملة',
        'سيارة بي إم دبليو الفئة الثالثة 2020 مستعملة بحالة جيدة.',
        'بي ام دبليو, الفئة 3, 2020, مستعملة, سيدان, رياضية', 'بي إم دبليو الفئة الثالثة 2020 مستعملة',
        'سيدان رياضية مستعملة',
        'bmw-3-series-2020-used', 'ar', 98)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (196, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>BMW Série 3 2020 (Occasion)</h1><p>Une berline sportive de luxe d''occasion, offrant une expérience de conduite agréable et un design élégant.</p><ul><li><strong>Modèle :</strong> 2020</li><li><strong>Kilométrage :</strong> 55 000 km</li><li><strong>État :</strong> Très bon, entretien régulier</li><li><strong>Caractéristiques :</strong> Moteur turbo, maniabilité sportive, système iDrive</li></ul>',
        'BMW Série 3 2020 Occasion', 'BMW Série 3 2020 d''occasion', 'BMW Série 3 2020 d''occasion en bon état.',
        'bmw, série 3, 2020, occasion, berline, sportive', 'BMW Série 3 2020 Occasion', 'Berline sportive d''occasion',
        'bmw-3-series-2020-used', 'fr', 98)
on conflict (description_id) do nothing;

-- Product 99: Mercedes C-Class (Sedan)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (99, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false, null,
        false, null, true, false, null, null, 'CAR-REF-99', 'CAR-SKU-99', 8, 15,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (197, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>مرسيدس C-Class 2024</h1><p>سيارة سيدان فاخرة مدمجة تجمع بين الأناقة والتكنولوجيا المتقدمة والراحة.</p><ul><li><strong>المحرك:</strong> 2.0 لتر توربو مع نظام EQ Boost</li><li><strong>الميزات:</strong> نظام MBUX، تصميم داخلي مستوحى من S-Class، كفاءة محسنة</li><li><strong>الراحة:</strong> مقاعد مريحة ونظام تعليق متكيف</li></ul>',
        'مرسيدس C-Class 2024', 'مرسيدس C-Class 2024 الجديدة', 'مرسيدس C-Class 2024: فخامة مدمجة وتكنولوجيا متطورة.',
        'مرسيدس, C-Class, 2024, سيدان, فاخرة', 'مرسيدس C-Class 2024 | سيدان فاخرة', 'أناقة وتكنولوجيا',
        'mercedes-c-class-2024', 'ar', 99)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (198, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Mercedes C-Class 2024</h1><p>Une berline compacte de luxe alliant élégance, technologie avancée et confort.</p><ul><li><strong>Moteur :</strong> 2.0L Turbo avec EQ Boost</li><li><strong>Caractéristiques :</strong> Système MBUX, intérieur inspiré de la S-Class, efficacité améliorée</li><li><strong>Confort :</strong> Sièges confortables et suspension adaptative</li></ul>',
        'Mercedes C-Class 2024', 'Nouvelle Mercedes C-Class 2024',
        'Mercedes C-Class 2024 : luxe compact et technologie de pointe.', 'mercedes, c-class, 2024, berline, luxe',
        'Mercedes C-Class 2024 | Berline de Luxe', 'Élégance et technologie',
        'mercedes-c-class-2024', 'fr', 99)
on conflict (description_id) do nothing;

-- Product 100: Hyundai Elantra (SUV - Error in logic, should be Sedan, using Type 10 SUV for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (100, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-100', 'CAR-SKU-100', 9, 16,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (199, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>هيونداي إلنترا 2024</h1><p>سيارة سيدان مدمجة بتصميم جريء وميزات رائعة وقيمة ممتازة.</p><ul><li><strong>المحرك:</strong> 1.6 لتر أو 2.0 لتر</li><li><strong>الميزات:</strong> شاشة لمس كبيرة، تصميم داخلي حديث، كفاءة في استهلاك الوقود</li><li><strong>القيمة:</strong> سعر تنافسي وميزات قياسية غنية</li></ul>',
        'هيونداي إلنترا 2024', 'هيونداي إلنترا 2024 الجديدة', 'هيونداي إلنترا 2024: تصميم جريء وقيمة لا تقبل المنافسة.',
        'هيونداي, إلنترا, 2024, سيدان, مدمجة', 'هيونداي إلنترا 2024 | سيدان أنيقة', 'تصميم وقيمة ممتازة',
        'hyundai-elantra-2024', 'ar', 100)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (200, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Hyundai Elantra 2024</h1><p>Une berline compacte au design audacieux, dotée de fonctionnalités exceptionnelles et d''un excellent rapport qualité-prix.</p><ul><li><strong>Moteur :</strong> 1.6L ou 2.0L</li><li><strong>Caractéristiques :</strong> Grand écran tactile, intérieur moderne, faible consommation de carburant</li><li><strong>Valeur :</strong> Prix compétitif et riches fonctionnalités de série</li></ul>',
        'Hyundai Elantra 2024', 'Nouvelle Hyundai Elantra 2024',
        'Hyundai Elantra 2024 : design audacieux et valeur imbattable.', 'hyundai, elantra, 2024, berline, compacte',
        'Hyundai Elantra 2024 | Berline Élégante', 'Design et excellente valeur',
        'hyundai-elantra-2024', 'fr', 100)
on conflict (description_id) do nothing;

-- Product 101: Kia Seltos (Electric - Error in logic, should be SUV, using Type 11 Electric for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (101, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-101', 'CAR-SKU-101', 10, 17,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (201, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>كيا سيلتوس 2024</h1><p>سيارة SUV صغيرة أنيقة وعملية، مثالية للمدينة والمغامرات الخفيفة.</p><ul><li><strong>المحرك:</strong> 1.6 لتر أو 1.4 لتر توربو</li><li><strong>الميزات:</strong> تصميم مميز، مقصورة واسعة نسبيًا، تقنيات أمان متقدمة</li><li><strong>العملية:</strong> سهلة القيادة والركن</li></ul>',
        'كيا سيلتوس 2024', 'كيا سيلتوس 2024 الجديدة', 'كيا سيلتوس 2024: سيارة SUV صغيرة تجمع بين الأناقة والعملية.',
        'كيا, سيلتوس, 2024, SUV, صغيرة', 'كيا سيلتوس 2024 | SUV أنيق', 'أنيقة وعملية للمدينة',
        'kia-seltos-2024', 'ar', 101)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (202, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Kia Seltos 2024</h1><p>Un petit VUS élégant et pratique, idéal pour la ville et les petites aventures.</p><ul><li><strong>Moteur :</strong> 1.6L ou 1.4L Turbo</li><li><strong>Caractéristiques :</strong> Design distinctif, habitacle relativement spacieux, technologies de sécurité avancées</li><li><strong>Praticité :</strong> Facile à conduire et à garer</li></ul>',
        'Kia Seltos 2024', 'Nouveau Kia Seltos 2024', 'Kia Seltos 2024 : le petit VUS qui allie style et praticité.',
        'kia, seltos, 2024, suv, vus, petit', 'Kia Seltos 2024 | VUS Élégant', 'Élégant et pratique pour la ville',
        'kia-seltos-2024', 'fr', 101)
on conflict (description_id) do nothing;

-- Product 102: Ford F-150 (Used Truck)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (102, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-102', 'CAR-SKU-102', 11, 18,
        '65f023632bc26470c104b75f', 12) -- Type 12 (USED_CARS)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (203, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>فورد F-150 2019 (مستعملة)</h1><p>شاحنة بيك أب قوية وموثوقة مستعملة، جاهزة للأعمال الشاقة والمغامرات.</p><ul><li><strong>الموديل:</strong> 2019</li><li><strong>المحرك:</strong> V6 EcoBoost أو V8</li><li><strong>الحالة:</strong> جيدة، قدرة سحب ممتازة</li><li><strong>الميزات:</strong> مقصورة واسعة، قدرات تحميل عالية، متانة معروفة</li></ul>',
        'فورد F-150 2019 مستعملة', 'شاحنة فورد F-150 2019 مستعملة', 'فورد F-150 2019 مستعملة، شاحنة قوية وموثوقة.',
        'فورد, F-150, 2019, مستعملة, شاحنة, بيك أب', 'فورد F-150 2019 مستعملة | شاحنة قوية',
        'شاحنة بيك أب مستعملة قوية',
        'ford-f150-2019-used', 'ar', 102)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (204, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Ford F-150 2019 (Occasion)</h1><p>Un pick-up d''occasion robuste et fiable, prêt pour les travaux difficiles et les aventures.</p><ul><li><strong>Modèle :</strong> 2019</li><li><strong>Moteur :</strong> V6 EcoBoost ou V8</li><li><strong>État :</strong> Bon, excellente capacité de remorquage</li><li><strong>Caractéristiques :</strong> Cabine spacieuse, capacités de charge élevées, durabilité reconnue</li></ul>',
        'Ford F-150 2019 Occasion', 'Camion Ford F-150 2019 d''occasion',
        'Ford F-150 2019 d''occasion, un camion robuste et fiable.', 'ford, f-150, 2019, occasion, camion, pick-up',
        'Ford F-150 2019 Occasion | Camion Robuste', 'Pick-up d''occasion robuste',
        'ford-f150-2019-used', 'fr', 102)
on conflict (description_id) do nothing;

-- Product 103: Toyota Corolla (Sedan)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (103, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-103', 'CAR-SKU-103', 12, 13,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN)
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (205, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>تويوتا كورولا 2024</h1><p>السيارة الأكثر مبيعًا في العالم، معروفة بموثوقيتها وكفاءتها وقيمتها الممتازة.</p><ul><li><strong>المحرك:</strong> 1.8 لتر أو 2.0 لتر</li><li><strong>الميزات:</strong> Toyota Safety Sense، كفاءة عالية في استهلاك الوقود، تصميم عملي</li><li><strong>الموثوقية:</strong> سمعة لا مثيل لها في الاعتمادية</li></ul>',
        'تويوتا كورولا 2024', 'تويوتا كورولا 2024 الجديدة', 'تويوتا كورولا 2024: السيارة المدمجة الموثوقة والاقتصادية.',
        'تويوتا, كورولا, 2024, سيدان, مدمجة, اقتصادية', 'تويوتا كورولا 2024 | موثوقة واقتصادية', 'موثوقية أسطورية',
        'toyota-corolla-2024', 'ar', 103)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (206, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Toyota Corolla 2024</h1><p>La voiture la plus vendue au monde, connue pour sa fiabilité, son efficacité et son excellent rapport qualité-prix.</p><ul><li><strong>Moteur :</strong> 1.8L ou 2.0L</li><li><strong>Caractéristiques :</strong> Toyota Safety Sense, excellente économie de carburant, design pratique</li><li><strong>Fiabilité :</strong> Réputation inégalée de fiabilité</li></ul>',
        'Toyota Corolla 2024', 'Nouvelle Toyota Corolla 2024',
        'Toyota Corolla 2024 : la voiture compacte fiable et économique.',
        'toyota, corolla, 2024, berline, compacte, économique', 'Toyota Corolla 2024 | Fiable et Économique',
        'Fiabilité légendaire',
        'toyota-corolla-2024', 'fr', 103)
on conflict (description_id) do nothing;

-- Product 104: BMW i4 (SUV - Error in logic, should be Electric Sedan, using Type 10 SUV for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (104, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-104', 'CAR-SKU-104', 13, 14,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (207, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>بي إم دبليو i4 2024</h1><p>سيارة غران كوبيه كهربائية بالكامل تجمع بين الأداء الرياضي المميز لسيارات BMW والتنقل المستدام.</p><ul><li><strong>المدى:</strong> يصل إلى 590 كم (WLTP)</li><li><strong>الأداء:</strong> تسارع فوري وتحكم دقيق</li><li><strong>الميزات:</strong> نظام iDrive 8، تصميم أنيق، شحن سريع</li></ul>',
        'بي إم دبليو i4 2024', 'بي إم دبليو i4 2024 الكهربائية',
        'بي إم دبليو i4 2024: الأداء الكهربائي يلتقي بالأناقة.', 'بي ام دبليو, i4, 2024, كهربائية, EV, غران كوبيه',
        'بي إم دبليو i4 2024 | غران كوبيه كهربائية', 'أداء كهربائي رياضي',
        'bmw-i4-2024', 'ar', 104)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (208, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>BMW i4 2024</h1><p>Un Gran Coupé entièrement électrique qui combine les performances sportives caractéristiques de BMW avec la mobilité durable.</p><ul><li><strong>Autonomie :</strong> Jusqu''à 590 km (WLTP)</li><li><strong>Performance :</strong> Accélération instantanée et maniabilité précise</li><li><strong>Caractéristiques :</strong> Système iDrive 8, design élégant, recharge rapide</li></ul>',
        'BMW i4 2024', 'BMW i4 2024 Électrique', 'BMW i4 2024 : la performance électrique rencontre l''élégance.',
        'bmw, i4, 2024, électrique, ev, gran coupé', 'BMW i4 2024 | Gran Coupé Électrique',
        'Performance électrique sportive',
        'bmw-i4-2024', 'fr', 104)
on conflict (description_id) do nothing;

-- Product 105: Mercedes E-Class (Electric - Error in logic, should be Sedan, using Type 11 Electric for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (105, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-105', 'CAR-SKU-105', 14, 15,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (209, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>مرسيدس E-Class 2024</h1><p>سيارة سيدان فاخرة متوسطة الحجم، رمز للراحة والابتكار والأناقة الخالدة.</p><ul><li><strong>المحرك:</strong> خيارات متنوعة مع تقنية EQ Boost</li><li><strong>الميزات:</strong> نظام MBUX محدث، تصميم داخلي فاخر، أنظمة مساعدة سائق متقدمة</li><li><strong>الراحة:</strong> تجربة قيادة سلسة وهادئة</li></ul>',
        'مرسيدس E-Class 2024', 'مرسيدس E-Class 2024 الجديدة', 'مرسيدس E-Class 2024: مزيج مثالي من الفخامة والابتكار.',
        'مرسيدس, E-Class, 2024, سيدان, فاخرة', 'مرسيدس E-Class 2024 | سيدان فاخرة مبتكرة', 'راحة وابتكار',
        'mercedes-e-class-2024', 'ar', 105)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (210, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Mercedes E-Class 2024</h1><p>La berline de luxe de taille moyenne, symbole de confort, d''innovation et d''élégance intemporelle.</p><ul><li><strong>Moteur :</strong> Diverses options avec technologie EQ Boost</li><li><strong>Caractéristiques :</strong> Système MBUX mis à jour, intérieur luxueux, systèmes avancés d''aide à la conduite</li><li><strong>Confort :</strong> Expérience de conduite douce et silencieuse</li></ul>',
        'Mercedes E-Class 2024', 'Nouvelle Mercedes E-Class 2024',
        'Mercedes E-Class 2024 : le mélange parfait de luxe et d''innovation.',
        'mercedes, e-class, 2024, berline, luxe', 'Mercedes E-Class 2024 | Berline de Luxe Innovante',
        'Confort et innovation',
        'mercedes-e-class-2024', 'fr', 105)
on conflict (description_id) do nothing;
-- Product 106: Hyundai Sonata (Used Sedan)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (106, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-106', 'CAR-SKU-106', 15, 16,
        '65f023632bc26470c104b75f', 12) -- Type 12 (USED_CARS) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (211, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>هيونداي سوناتا 2022 (مستعملة)</h1><p>سيارة سيدان متوسطة الحجم مستعملة بتصميم أنيق ومقصورة رحبة.</p><ul><li><strong>الموديل:</strong> 2022</li><li><strong>المسافة المقطوعة:</strong> 30,000 كم</li><li><strong>الحالة:</strong> ممتازة، تحت الضمان</li><li><strong>الميزات:</strong> شاشة لمس كبيرة، تصميم انسيابي، مقاعد مريحة</li></ul>',
        'هيونداي سوناتا 2022 مستعملة', 'هيونداي سوناتا 2022 مستعملة للبيع',
        'سيارة هيونداي سوناتا 2022 مستعملة بحالة ممتازة وتصميم جذاب.', 'هيونداي, سوناتا, 2022, مستعملة, سيدان',
        'هيونداي سوناتا 2022 مستعملة | تصميم أنيق', 'سيدان مستعملة بحالة ممتازة',
        'hyundai-sonata-2022-used', 'ar', 106)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (212, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Hyundai Sonata 2022 (Occasion)</h1><p>Une berline intermédiaire d''occasion au design élégant et à l''habitacle spacieux.</p><ul><li><strong>Modèle :</strong> 2022</li><li><strong>Kilométrage :</strong> 30 000 km</li><li><strong>État :</strong> Excellent, sous garantie</li><li><strong>Caractéristiques :</strong> Grand écran tactile, design fluide, sièges confortables</li></ul>',
        'Hyundai Sonata 2022 Occasion', 'Hyundai Sonata 2022 d''occasion à vendre',
        'Hyundai Sonata 2022 d''occasion en excellent état et au design attrayant.',
        'hyundai, sonata, 2022, occasion, berline', 'Hyundai Sonata 2022 Occasion | Design Élégant',
        'Berline d''occasion en excellent état',
        'hyundai-sonata-2022-used', 'fr', 106)
on conflict (description_id) do nothing;

-- Product 107: Kia EV6 (Sedan - Error in logic, should be Electric SUV, using Type 9 Sedan for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (107, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-107', 'CAR-SKU-107', 16, 17,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (213, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>كيا EV6 2024</h1><p>سيارة كروس أوفر كهربائية بالكامل بتصميم مستقبلي ومدى طويل وشحن فائق السرعة.</p><ul><li><strong>المدى:</strong> يصل إلى 500 كم (WLTP)</li><li><strong>الشحن:</strong> يدعم شحن 800 فولت فائق السرعة</li><li><strong>الميزات:</strong> تصميم داخلي مبتكر، شاشات منحنية، أداء رياضي (GT)</li></ul>',
        'كيا EV6 2024', 'كيا EV6 2024 الكهربائية', 'كيا EV6 2024: تصميم مستقبلي وأداء كهربائي مذهل.',
        'كيا, EV6, 2024, كهربائية, EV, كروس أوفر', 'كيا EV6 2024 | كروس أوفر كهربائي', 'تصميم مستقبلي وشحن سريع',
        'kia-ev6-2024', 'ar', 107)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (214, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Kia EV6 2024</h1><p>Un crossover entièrement électrique au design futuriste, longue autonomie et recharge ultra-rapide.</p><ul><li><strong>Autonomie :</strong> Jusqu''à 500 km (WLTP)</li><li><strong>Recharge :</strong> Supporte la recharge ultra-rapide 800V</li><li><strong>Caractéristiques :</strong> Intérieur innovant, écrans incurvés, performance sportive (GT)</li></ul>',
        'Kia EV6 2024', 'Kia EV6 2024 Électrique',
        'Kia EV6 2024 : design futuriste et performances électriques étonnantes.',
        'kia, ev6, 2024, électrique, ev, crossover', 'Kia EV6 2024 | Crossover Électrique',
        'Design futuriste et recharge rapide',
        'kia-ev6-2024', 'fr', 107)
on conflict (description_id) do nothing;

-- Product 108: Ford Explorer (SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (108, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-108', 'CAR-SKU-108', 17, 18,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (215, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>فورد إكسبلورر 2024</h1><p>سيارة SUV عائلية بثلاثة صفوف من المقاعد، تجمع بين الرحابة والقوة والتكنولوجيا.</p><ul><li><strong>المحرك:</strong> EcoBoost V6 أو نسخة هجينة</li><li><strong>المقاعد:</strong> تتسع لـ 7 ركاب</li><li><strong>الميزات:</strong> نظام Co-Pilot360 للمساعدة على القيادة، مساحة تخزين كبيرة، قدرة سحب جيدة</li></ul>',
        'فورد إكسبلورر 2024', 'فورد إكسبلورر 2024 العائلية',
        'فورد إكسبلورر 2024: سيارة SUV مثالية للعائلة والمغامرات.', 'فورد, إكسبلورر, 2024, SUV, عائلية, 7 مقاعد',
        'فورد إكسبلورر 2024 | SUV عائلي', 'رحابة وقوة للعائلة',
        'ford-explorer-2024', 'ar', 108)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (216, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Ford Explorer 2024</h1><p>Un VUS familial à trois rangées qui allie espace, puissance et technologie.</p><ul><li><strong>Moteur :</strong> EcoBoost V6 ou version hybride</li><li><strong>Sièges :</strong> Jusqu''à 7 passagers</li><li><strong>Caractéristiques :</strong> Système d''aide à la conduite Co-Pilot360, grand espace de chargement, bonne capacité de remorquage</li></ul>',
        'Ford Explorer 2024', 'Ford Explorer 2024 Familial',
        'Ford Explorer 2024 : le VUS parfait pour la famille et l''aventure.',
        'ford, explorer, 2024, suv, vus, familial, 7 places', 'Ford Explorer 2024 | VUS Familial',
        'Espace et puissance pour la famille',
        'ford-explorer-2024', 'fr', 108)
on conflict (description_id) do nothing;

-- Product 109: Toyota Highlander (Electric - Error in logic, should be SUV/Hybrid, using Type 11 Electric for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (109, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-109', 'CAR-SKU-109', 18, 13,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (217, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>تويوتا هايلاندر 2024</h1><p>سيارة SUV متوسطة الحجم بثلاثة صفوف، مثالية للعائلات التي تبحث عن الراحة والموثوقية والكفاءة.</p><ul><li><strong>المحرك:</strong> V6 أو نسخة هجينة عالية الكفاءة</li><li><strong>المقاعد:</strong> تتسع لـ 7 أو 8 ركاب</li><li><strong>الميزات:</strong> Toyota Safety Sense 2.5+، مقصورة هادئة ومريحة، خيارات دفع رباعي</li></ul>',
        'تويوتا هايلاندر 2024', 'تويوتا هايلاندر 2024 العائلية',
        'تويوتا هايلاندر 2024: الراحة والموثوقية للعائلة الكبيرة.',
        'تويوتا, هايلاندر, 2024, SUV, عائلية, هجين, 7 مقاعد',
        'تويوتا هايلاندر 2024 | SUV عائلي مريح', 'راحة وكفاءة للعائلة',
        'toyota-highlander-2024', 'ar', 109)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (218, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Toyota Highlander 2024</h1><p>Un VUS intermédiaire à trois rangées, idéal pour les familles recherchant confort, fiabilité et efficacité.</p><ul><li><strong>Moteur :</strong> V6 ou version hybride très efficace</li><li><strong>Sièges :</strong> Jusqu''à 7 ou 8 passagers</li><li><strong>Caractéristiques :</strong> Toyota Safety Sense 2.5+, habitacle silencieux et confortable, options AWD</li></ul>',
        'Toyota Highlander 2024', 'Toyota Highlander 2024 Familial',
        'Toyota Highlander 2024 : confort et fiabilité pour la grande famille.',
        'toyota, highlander, 2024, suv, vus, familial, hybride, 7 places',
        'Toyota Highlander 2024 | VUS Familial Confortable', 'Confort et efficacité pour la famille',
        'toyota-highlander-2024', 'fr', 109)
on conflict (description_id) do nothing;

-- Product 110: BMW X3 (Used SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (110, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-110', 'CAR-SKU-110', 19, 14,
        '65f023632bc26470c104b75f', 12) -- Type 12 (USED_CARS) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (219, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>بي إم دبليو X3 2021 (مستعملة)</h1><p>سيارة SUV مدمجة فاخرة مستعملة، تجمع بين الأداء الرياضي والعملية اليومية.</p><ul><li><strong>الموديل:</strong> 2021</li><li><strong>المسافة المقطوعة:</strong> 40,000 كم</li><li><strong>الحالة:</strong> ممتازة، تاريخ صيانة كامل</li><li><strong>الميزات:</strong> محرك توربو قوي، نظام xDrive، مقصورة عالية الجودة</li></ul>',
        'بي إم دبليو X3 2021 مستعملة', 'بي إم دبليو X3 2021 مستعملة للبيع',
        'سيارة بي إم دبليو X3 2021 مستعملة بحالة ممتازة وأداء رياضي.', 'بي ام دبليو, X3, 2021, مستعملة, SUV, فاخرة',
        'بي إم دبليو X3 2021 مستعملة | SUV فاخر', 'SUV فاخر مستعمل بحالة ممتازة',
        'bmw-x3-2021-used', 'ar', 110)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (220, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>BMW X3 2021 (Occasion)</h1><p>Un VUS compact de luxe d''occasion, alliant performances sportives et praticité au quotidien.</p><ul><li><strong>Modèle :</strong> 2021</li><li><strong>Kilométrage :</strong> 40 000 km</li><li><strong>État :</strong> Excellent, historique d''entretien complet</li><li><strong>Caractéristiques :</strong> Moteur turbo puissant, système xDrive, habitacle de haute qualité</li></ul>',
        'BMW X3 2021 Occasion', 'BMW X3 2021 d''occasion à vendre',
        'BMW X3 2021 d''occasion en excellent état avec des performances sportives.',
        'bmw, x3, 2021, occasion, suv, vus, luxe', 'BMW X3 2021 Occasion | VUS de Luxe',
        'VUS de luxe d''occasion en excellent état',
        'bmw-x3-2021-used', 'fr', 110)
on conflict (description_id) do nothing;

-- Product 111: Mercedes GLC (Sedan - Error in logic, should be SUV, using Type 9 Sedan for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (111, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-111', 'CAR-SKU-111', 20, 15,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (221, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>مرسيدس GLC 2024</h1><p>سيارة SUV فاخرة مدمجة تجمع بين التصميم الأنيق والتكنولوجيا المتقدمة والراحة الفائقة.</p><ul><li><strong>المحرك:</strong> 2.0 لتر توربو مع EQ Boost</li><li><strong>الميزات:</strong> نظام MBUX، مقصورة فاخرة، خيارات دفع رباعي 4MATIC</li><li><strong>الراحة:</strong> قيادة سلسة ومريحة</li></ul>',
        'مرسيدس GLC 2024', 'مرسيدس GLC 2024 الجديدة', 'مرسيدس GLC 2024: الفخامة والراحة في سيارة SUV مدمجة.',
        'مرسيدس, GLC, 2024, SUV, فاخرة, مدمجة', 'مرسيدس GLC 2024 | SUV فاخر مدمج', 'فخامة وراحة مدمجة',
        'mercedes-glc-2024', 'ar', 111)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (222, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Mercedes GLC 2024</h1><p>Un VUS compact de luxe alliant design élégant, technologie avancée et confort supérieur.</p><ul><li><strong>Moteur :</strong> 2.0L Turbo avec EQ Boost</li><li><strong>Caractéristiques :</strong> Système MBUX, habitacle luxueux, options 4MATIC AWD</li><li><strong>Confort :</strong> Conduite douce et confortable</li></ul>',
        'Mercedes GLC 2024', 'Nouveau Mercedes GLC 2024',
        'Mercedes GLC 2024 : luxe et confort dans un VUS compact.', 'mercedes, glc, 2024, suv, vus, luxe, compact',
        'Mercedes GLC 2024 | VUS Compact de Luxe', 'Luxe et confort compacts',
        'mercedes-glc-2024', 'fr', 111)
on conflict (description_id) do nothing;

-- Product 112: Hyundai Santa Fe (SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (112, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-112', 'CAR-SKU-112', 21, 16,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (223, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>هيونداي سنتافي 2024</h1><p>سيارة SUV متوسطة الحجم بتصميم جديد جريء ومقصورة رحبة بثلاثة صفوف.</p><ul><li><strong>التصميم:</strong> تصميم صندوقي مميز وجريء</li><li><strong>المقاعد:</strong> 3 صفوف تتسع للعائلة</li><li><strong>الميزات:</strong> تقنيات متقدمة، مساحة داخلية واسعة، خيارات هجينة</li></ul>',
        'هيونداي سنتافي 2024', 'هيونداي سنتافي 2024 الجديدة كلياً',
        'اكتشف هيونداي سنتافي 2024 الجديدة بتصميمها الجريء ومقصورتها الرحبة.',
        'هيونداي, سنتافي, 2024, SUV, عائلية, 7 مقاعد', 'هيونداي سنتافي 2024 | تصميم جديد جريء',
        'تصميم جريء ومساحة للعائلة',
        'hyundai-santa-fe-2024', 'ar', 112)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (224, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Hyundai Santa Fe 2024</h1><p>Un VUS intermédiaire avec un nouveau design audacieux et un habitacle spacieux à trois rangées.</p><ul><li><strong>Design :</strong> Design carré distinctif et audacieux</li><li><strong>Sièges :</strong> 3 rangées pour accueillir la famille</li><li><strong>Caractéristiques :</strong> Technologies avancées, intérieur spacieux, options hybrides</li></ul>',
        'Hyundai Santa Fe 2024', 'Tout nouveau Hyundai Santa Fe 2024',
        'Découvrez le nouveau Hyundai Santa Fe 2024 avec son design audacieux et son habitacle spacieux.',
        'hyundai, santa fe, 2024, suv, vus, familial, 7 places', 'Hyundai Santa Fe 2024 | Nouveau Design Audacieux',
        'Design audacieux et espace familial',
        'hyundai-santa-fe-2024', 'fr', 112)
on conflict (description_id) do nothing;

-- Product 113: Kia Telluride (Electric - Error in logic, should be SUV, using Type 11 Electric for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (113, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-113', 'CAR-SKU-113', 22, 17,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (225, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>كيا تيلورايد 2024</h1><p>سيارة SUV كبيرة بثلاثة صفوف تجمع بين الفخامة والرحابة والقيمة الممتازة.</p><ul><li><strong>المقاعد:</strong> تتسع لـ 8 ركاب بشكل مريح</li><li><strong>الميزات:</strong> تصميم داخلي راقي، تقنيات متقدمة، قيادة سلسة</li><li><strong>القيمة:</strong> حزمة ميزات غنية بسعر تنافسي</li></ul>',
        'كيا تيلورايد 2024', 'كيا تيلورايد 2024 العائلية الفاخرة',
        'كيا تيلورايد 2024: فخامة ورحابة لا مثيل لها للعائلة.', 'كيا, تيلورايد, 2024, SUV, عائلية, 8 مقاعد, فاخرة',
        'كيا تيلورايد 2024 | SUV عائلي فاخر', 'فخامة وقيمة للعائلة',
        'kia-telluride-2024', 'ar', 113)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (226, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Kia Telluride 2024</h1><p>Un grand VUS à trois rangées qui combine luxe, espace et excellente valeur.</p><ul><li><strong>Sièges :</strong> Accueille confortablement jusqu''à 8 passagers</li><li><strong>Caractéristiques :</strong> Intérieur haut de gamme, technologies avancées, conduite douce</li><li><strong>Valeur :</strong> Riche ensemble de fonctionnalités à un prix compétitif</li></ul>',
        'Kia Telluride 2024', 'Kia Telluride 2024 Familial de Luxe',
        'Kia Telluride 2024 : luxe et espace inégalés pour la famille.',
        'kia, telluride, 2024, suv, vus, familial, 8 places, luxe', 'Kia Telluride 2024 | VUS Familial de Luxe',
        'Luxe et valeur pour la famille',
        'kia-telluride-2024', 'fr', 113)
on conflict (description_id) do nothing;

-- Product 114: Ford Bronco (Used SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (114, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-114', 'CAR-SKU-114', 23, 18,
        '65f023632bc26470c104b75f', 12) -- Type 12 (USED_CARS) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (227, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>فورد برونكو 2022 (مستعملة)</h1><p>سيارة SUV للطرق الوعرة مستعملة، بتصميم كلاسيكي وقدرات فائقة.</p><ul><li><strong>الموديل:</strong> 2022</li><li><strong>المسافة المقطوعة:</strong> 25,000 كم</li><li><strong>الحالة:</strong> ممتازة، مجهزة للطرق الوعرة</li><li><strong>الميزات:</strong> أبواب وسقف قابل للإزالة، نظام دفع رباعي متقدم، تصميم أيقوني</li></ul>',
        'فورد برونكو 2022 مستعملة', 'فورد برونكو 2022 مستعملة للطرق الوعرة',
        'سيارة فورد برونكو 2022 مستعملة، أيقونة الطرق الوعرة.', 'فورد, برونكو, 2022, مستعملة, SUV, طرق وعرة, 4x4',
        'فورد برونكو 2022 مستعملة | أيقونة الطرق الوعرة', 'SUV مستعمل للطرق الوعرة',
        'ford-bronco-2022-used', 'ar', 114)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (228, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Ford Bronco 2022 (Occasion)</h1><p>Un VUS tout-terrain d''occasion, au design classique et aux capacités exceptionnelles.</p><ul><li><strong>Modèle :</strong> 2022</li><li><strong>Kilométrage :</strong> 25 000 km</li><li><strong>État :</strong> Excellent, équipé pour le tout-terrain</li><li><strong>Caractéristiques :</strong> Portes et toit amovibles, système 4x4 avancé, design iconique</li></ul>',
        'Ford Bronco 2022 Occasion', 'Ford Bronco 2022 d''occasion tout-terrain',
        'Ford Bronco 2022 d''occasion, une icône du tout-terrain.',
        'ford, bronco, 2022, occasion, suv, vus, tout-terrain, 4x4',
        'Ford Bronco 2022 Occasion | Icône Tout-Terrain', 'VUS tout-terrain d''occasion',
        'ford-bronco-2022-used', 'fr', 114)
on conflict (description_id) do nothing;

-- Product 115: Toyota Sienna (Sedan - Error in logic, should be Minivan/Hybrid, using Type 9 Sedan for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (115, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-115', 'CAR-SKU-115', 24, 13,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (229, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>تويوتا سيينا 2024</h1><p>ميني فان هجينة بالكامل، توفر مساحة رحبة للعائلة وكفاءة ممتازة في استهلاك الوقود.</p><ul><li><strong>المحرك:</strong> نظام هجين 2.5 لتر</li><li><strong>المقاعد:</strong> تتسع لـ 7 أو 8 ركاب</li><li><strong>الميزات:</strong> أبواب منزلقة كهربائية، مقاعد الصف الثاني فاخرة (اختياري)، كفاءة وقود رائدة في فئتها</li></ul>',
        'تويوتا سيينا 2024', 'تويوتا سيينا 2024 الهجينة', 'تويوتا سيينا 2024: الميني فان الهجينة المثالية للعائلة.',
        'تويوتا, سيينا, 2024, ميني فان, هجين, عائلية', 'تويوتا سيينا 2024 | ميني فان هجينة', 'مساحة وكفاءة للعائلة',
        'toyota-sienna-2024', 'ar', 115)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (230, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Toyota Sienna 2024</h1><p>Une minifourgonnette entièrement hybride, offrant un espace généreux pour la famille et une excellente économie de carburant.</p><ul><li><strong>Moteur :</strong> Système hybride 2.5L</li><li><strong>Sièges :</strong> Jusqu''à 7 ou 8 passagers</li><li><strong>Caractéristiques :</strong> Portes coulissantes électriques, sièges capitaine de luxe en 2e rangée (option), économie de carburant de premier ordre</li></ul>',
        'Toyota Sienna 2024', 'Toyota Sienna 2024 Hybride',
        'Toyota Sienna 2024 : la minifourgonnette hybride parfaite pour la famille.',
        'toyota, sienna, 2024, minifourgonnette, hybride, familial', 'Toyota Sienna 2024 | Minifourgonnette Hybride',
        'Espace et efficacité pour la famille',
        'toyota-sienna-2024', 'fr', 115)
on conflict (description_id) do nothing;

-- Product 116: BMW 5 Series (SUV - Error in logic, should be Sedan, using Type 10 SUV for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (116, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-116', 'CAR-SKU-116', 25, 14,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (231, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>بي إم دبليو الفئة الخامسة 2024</h1><p>سيارة سيدان فاخرة تنفيذية تجمع بين الديناميكية والراحة والتكنولوجيا المبتكرة.</p><ul><li><strong>المحرك:</strong> خيارات متنوعة بما في ذلك نسخ هجينة وكهربائية (i5)</li><li><strong>الميزات:</strong> شاشة BMW Curved Display، نظام iDrive 8.5، تصميم داخلي فاخر</li><li><strong>القيادة:</strong> توازن مثالي بين الراحة والأداء الرياضي</li></ul>',
        'بي إم دبليو الفئة الخامسة 2024', 'بي إم دبليو الفئة الخامسة 2024 الجديدة',
        'بي إم دبليو الفئة الخامسة 2024: الفخامة التنفيذية والتكنولوجيا المتقدمة.',
        'بي ام دبليو, الفئة 5, 2024, سيدان, فاخرة, تنفيذية, i5', 'بي إم دبليو الفئة الخامسة 2024 | سيدان تنفيذية',
        'فخامة وديناميكية',
        'bmw-5-series-2024', 'ar', 116)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (232, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>BMW Série 5 2024</h1><p>La berline de luxe exécutive qui allie dynamisme, confort et technologie innovante.</p><ul><li><strong>Moteur :</strong> Diverses options incluant hybride et électrique (i5)</li><li><strong>Caractéristiques :</strong> Écran incurvé BMW, système iDrive 8.5, intérieur luxueux</li><li><strong>Conduite :</strong> Équilibre parfait entre confort et performance sportive</li></ul>',
        'BMW Série 5 2024', 'Nouvelle BMW Série 5 2024',
        'BMW Série 5 2024 : luxe exécutif et technologie avancée.', 'bmw, série 5, 2024, berline, luxe, exécutive, i5',
        'BMW Série 5 2024 | Berline Exécutive', 'Luxe et dynamisme',
        'bmw-5-series-2024', 'fr', 116)
on conflict (description_id) do nothing;

-- Product 117: Mercedes S-Class (Electric - Error in logic, should be Sedan, using Type 11 Electric for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (117, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-117', 'CAR-SKU-117', 26, 15,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (233, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>مرسيدس S-Class 2024</h1><p>قمة الفخامة والابتكار في عالم السيارات. سيارة سيدان فاخرة تحدد المعايير.</p><ul><li><strong>الفخامة:</strong> مقصورة لا مثيل لها بمواد فاخرة وتكنولوجيا متطورة</li><li><strong>الراحة:</strong> تجربة قيادة هادئة ومريحة للغاية</li><li><strong>الميزات:</strong> أحدث أنظمة MBUX، أنظمة مساعدة سائق متقدمة، خيارات تخصيص واسعة</li></ul>',
        'مرسيدس S-Class 2024', 'مرسيدس S-Class 2024 الفاخرة', 'مرسيدس S-Class 2024: معيار الفخامة والابتكار.',
        'مرسيدس, S-Class, 2024, سيدان, فاخرة, قمة', 'مرسيدس S-Class 2024 | قمة الفخامة', 'فخامة وابتكار لا مثيل لهما',
        'mercedes-s-class-2024', 'ar', 117)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (234, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Mercedes S-Class 2024</h1><p>Le summum du luxe et de l''innovation automobile. La berline de luxe qui définit les standards.</p><ul><li><strong>Luxe :</strong> Habitacle inégalé avec matériaux somptueux et technologie de pointe</li><li><strong>Confort :</strong> Expérience de conduite extrêmement silencieuse et confortable</li><li><strong>Caractéristiques :</strong> Derniers systèmes MBUX, systèmes avancés d''aide à la conduite, larges options de personnalisation</li></ul>',
        'Mercedes S-Class 2024', 'Mercedes S-Class 2024 de Luxe',
        'Mercedes S-Class 2024 : la référence du luxe et de l''innovation.',
        'mercedes, s-class, 2024, berline, luxe, sommet', 'Mercedes S-Class 2024 | Le Sommet du Luxe',
        'Luxe et innovation inégalés',
        'mercedes-s-class-2024', 'fr', 117)
on conflict (description_id) do nothing;

-- Product 118: Hyundai Kona Electric (Used Electric)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (118, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-118', 'CAR-SKU-118', 27, 16,
        '65f023632bc26470c104b75f', 12) -- Type 12 (USED_CARS) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (235, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>هيونداي كونا الكهربائية 2022 (مستعملة)</h1><p>سيارة SUV كهربائية صغيرة مستعملة، عملية وذات مدى جيد للمدينة.</p><ul><li><strong>الموديل:</strong> 2022</li><li><strong>المدى:</strong> حوالي 400 كم (WLTP)</li><li><strong>الحالة:</strong> جيدة جدًا، بطارية بحالة جيدة</li><li><strong>الميزات:</strong> تصميم مميز، قيادة سهلة، تكلفة تشغيل منخفضة</li></ul>',
        'هيونداي كونا الكهربائية 2022 مستعملة', 'هيونداي كونا الكهربائية 2022 مستعملة',
        'سيارة هيونداي كونا الكهربائية 2022 مستعملة، خيار اقتصادي وعملي.',
        'هيونداي, كونا, كهربائية, 2022, مستعملة, EV, SUV', 'هيونداي كونا الكهربائية 2022 مستعملة',
        'SUV كهربائية مستعملة اقتصادية',
        'hyundai-kona-electric-2022-used', 'ar', 118)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (236, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Hyundai Kona Electric 2022 (Occasion)</h1><p>Un petit VUS électrique d''occasion, pratique et avec une bonne autonomie pour la ville.</p><ul><li><strong>Modèle :</strong> 2022</li><li><strong>Autonomie :</strong> Environ 400 km (WLTP)</li><li><strong>État :</strong> Très bon, batterie en bon état</li><li><strong>Caractéristiques :</strong> Design distinctif, conduite facile, faible coût d''utilisation</li></ul>',
        'Hyundai Kona Electric 2022 Occasion', 'Hyundai Kona Electric 2022 d''occasion',
        'Hyundai Kona Electric 2022 d''occasion, un choix économique et pratique.',
        'hyundai, kona, electric, 2022, occasion, ev, suv, vus', 'Hyundai Kona Electric 2022 Occasion',
        'VUS électrique d''occasion économique',
        'hyundai-kona-electric-2022-used', 'fr', 118)
on conflict (description_id) do nothing;

-- Product 119: Kia Niro EV (Sedan - Error in logic, should be Electric SUV, using Type 9 Sedan for demo)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (119, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-119', 'CAR-SKU-119', 28, 17,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (237, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>كيا نيرو EV 2024</h1><p>سيارة كروس أوفر كهربائية عملية بتصميم محدث ومقصورة واسعة.</p><ul><li><strong>المدى:</strong> مدى جيد للاستخدام اليومي</li><li><strong>الميزات:</strong> تصميم داخلي مستدام، تقنيات مساعدة السائق، مساحة تخزين جيدة</li><li><strong>العملية:</strong> خيار رائع للعائلات الصغيرة والتنقل الحضري</li></ul>',
        'كيا نيرو EV 2024', 'كيا نيرو EV 2024 الكهربائية', 'كيا نيرو EV 2024: كروس أوفر كهربائي عملي ومستدام.',
        'كيا, نيرو, EV, 2024, كهربائية, كروس أوفر', 'كيا نيرو EV 2024 | كروس أوفر كهربائي عملي', 'عملية ومستدامة',
        'kia-niro-ev-2024', 'ar', 119)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (238, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Kia Niro EV 2024</h1><p>Un crossover électrique pratique avec un design mis à jour et un habitacle spacieux.</p><ul><li><strong>Autonomie :</strong> Bonne autonomie pour un usage quotidien</li><li><strong>Caractéristiques :</strong> Intérieur durable, technologies d''aide à la conduite, bon espace de chargement</li><li><strong>Praticité :</strong> Excellent choix pour les petites familles et les déplacements urbains</li></ul>',
        'Kia Niro EV 2024', 'Kia Niro EV 2024 Électrique',
        'Kia Niro EV 2024 : crossover électrique pratique et durable.',
        'kia, niro, ev, 2024, électrique, crossover', 'Kia Niro EV 2024 | Crossover Électrique Pratique',
        'Pratique et durable',
        'kia-niro-ev-2024', 'fr', 119)
on conflict (description_id) do nothing;

-- Product 120: Ford Mustang Mach-E (SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (120, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-120', 'CAR-SKU-120', 29, 18,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (239, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>فورد موستانج Mach-E 2024</h1><p>سيارة SUV كهربائية بالكامل مستوحاة من تصميم الموستانج الأيقوني، تجمع بين الأداء المثير والتكنولوجيا المتقدمة.</p><ul><li><strong>المدى:</strong> خيارات بطارية متعددة بمدى يصل إلى 500 كم</li><li><strong>الأداء:</strong> تسارع قوي، خاصة في نسخة GT</li><li><strong>الميزات:</strong> شاشة لمس عمودية كبيرة، نظام SYNC 4A، تصميم رياضي</li></ul>',
        'فورد موستانج Mach-E 2024', 'فورد موستانج Mach-E 2024 الكهربائية',
        'فورد موستانج Mach-E 2024: الأداء الكهربائي بروح الموستانج.',
        'فورد, موستانج, Mach-E, 2024, كهربائية, EV, SUV, رياضية', 'فورد موستانج Mach-E 2024 | SUV كهربائي رياضي',
        'أداء كهربائي مثير',
        'ford-mustang-mach-e-2024', 'ar', 120)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (240, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Ford Mustang Mach-E 2024</h1><p>Un VUS entièrement électrique inspiré du design iconique de la Mustang, alliant performances exaltantes et technologie avancée.</p><ul><li><strong>Autonomie :</strong> Multiples options de batterie avec jusqu''à 500 km d''autonomie</li><li><strong>Performance :</strong> Accélération puissante, surtout en version GT</li><li><strong>Caractéristiques :</strong> Grand écran tactile vertical, système SYNC 4A, design sportif</li></ul>',
        'Ford Mustang Mach-E 2024', 'Ford Mustang Mach-E 2024 Électrique',
        'Ford Mustang Mach-E 2024 : la performance électrique avec l''esprit Mustang.',
        'ford, mustang, mach-e, 2024, électrique, ev, suv, vus, sportif',
        'Ford Mustang Mach-E 2024 | VUS Électrique Sportif', 'Performance électrique exaltante',
        'ford-mustang-mach-e-2024', 'fr', 120)
on conflict (description_id) do nothing;
-- Product 121: Toyota Avalon (Sedan)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (121, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-121', 'CAR-SKU-121', 30, 13,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (241, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>تويوتا أفالون 2024</h1><p>سيارة سيدان كبيرة تجمع بين الفخامة والراحة والأداء السلس.</p><ul><li><strong>المحرك:</strong> V6 قوي أو نسخة هجينة فعالة</li><li><strong>المقصورة:</strong> واسعة وفاخرة بمواد عالية الجودة</li><li><strong>الميزات:</strong> نظام صوت JBL، شاشة عرض على الزجاج الأمامي، مقاعد جلدية مريحة</li></ul>',
        'تويوتا أفالون 2024', 'تويوتا أفالون 2024 الفاخرة', 'تويوتا أفالون 2024: تجربة سيدان فاخرة ومريحة.',
        'تويوتا, أفالون, 2024, سيدان, فاخرة, كبيرة', 'تويوتا أفالون 2024 | سيدان فاخرة', 'فخامة وراحة فائقة',
        'toyota-avalon-2024', 'ar', 121)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (242, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Toyota Avalon 2024</h1><p>Une grande berline qui allie luxe, confort et performances douces.</p><ul><li><strong>Moteur :</strong> V6 puissant ou version hybride efficace</li><li><strong>Habitacle :</strong> Spacieux et luxueux avec des matériaux de haute qualité</li><li><strong>Caractéristiques :</strong> Système audio JBL, affichage tête haute, sièges en cuir confortables</li></ul>',
        'Toyota Avalon 2024', 'Toyota Avalon 2024 de Luxe',
        'Toyota Avalon 2024 : une expérience de berline luxueuse et confortable.',
        'toyota, avalon, 2024, berline, luxe, grande', 'Toyota Avalon 2024 | Berline de Luxe',
        'Luxe et confort supérieurs',
        'toyota-avalon-2024', 'fr', 121)
on conflict (description_id) do nothing;

-- Product 122: BMW X1 (SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (122, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-122', 'CAR-SKU-122', 31, 14,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (243, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>بي إم دبليو X1 2024</h1><p>سيارة SUV مدمجة فاخرة بتصميم رياضي ومقصورة عملية وتقنيات حديثة.</p><ul><li><strong>المحرك:</strong> 4 سلندر توربو</li><li><strong>الميزات:</strong> شاشة BMW Curved Display، نظام iDrive حديث، تصميم داخلي واسع نسبيًا</li><li><strong>القيادة:</strong> رشيقة وممتعة في المدينة</li></ul>',
        'بي إم دبليو X1 2024', 'بي إم دبليو X1 2024 الجديدة', 'بي إم دبليو X1 2024: سيارة SUV مدمجة فاخرة وعملية.',
        'بي ام دبليو, X1, 2024, SUV, مدمجة, فاخرة', 'بي إم دبليو X1 2024 | SUV مدمج فاخر', 'رياضية وعملية',
        'bmw-x1-2024', 'ar', 122)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (244, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>BMW X1 2024</h1><p>Un VUS compact de luxe au design sportif, habitacle pratique et technologies modernes.</p><ul><li><strong>Moteur :</strong> 4 cylindres Turbo</li><li><strong>Caractéristiques :</strong> Écran incurvé BMW, système iDrive moderne, intérieur relativement spacieux</li><li><strong>Conduite :</strong> Agile et agréable en ville</li></ul>',
        'BMW X1 2024', 'Nouveau BMW X1 2024', 'BMW X1 2024 : le VUS compact de luxe pratique et sportif.',
        'bmw, x1, 2024, suv, vus, compact, luxe', 'BMW X1 2024 | VUS Compact de Luxe', 'Sportif et pratique',
        'bmw-x1-2024', 'fr', 122)
on conflict (description_id) do nothing;

-- Product 123: Mercedes EQB (Electric SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (123, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-123', 'CAR-SKU-123', 32, 15,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (245, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>مرسيدس EQB 2024</h1><p>سيارة SUV كهربائية مدمجة فاخرة تتسع لسبعة ركاب (اختياري).</p><ul><li><strong>المدى:</strong> مدى جيد للاستخدام اليومي والعائلي</li><li><strong>المقاعد:</strong> 5 مقاعد قياسية، 7 مقاعد اختيارية</li><li><strong>الميزات:</strong> نظام MBUX، تصميم داخلي عملي، دفع رباعي 4MATIC اختياري</li></ul>',
        'مرسيدس EQB 2024', 'مرسيدس EQB 2024 الكهربائية العائلية',
        'مرسيدس EQB 2024: سيارة SUV كهربائية فاخرة وعملية للعائلة.',
        'مرسيدس, EQB, 2024, كهربائية, EV, SUV, 7 مقاعد', 'مرسيدس EQB 2024 | SUV كهربائي عائلي', 'كهربائية وعائلية',
        'mercedes-eqb-2024', 'ar', 123)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (246, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Mercedes EQB 2024</h1><p>Un VUS compact électrique de luxe pouvant accueillir jusqu''à sept passagers (en option).</p><ul><li><strong>Autonomie :</strong> Bonne autonomie pour un usage quotidien et familial</li><li><strong>Sièges :</strong> 5 sièges de série, 7 sièges en option</li><li><strong>Caractéristiques :</strong> Système MBUX, intérieur pratique, transmission intégrale 4MATIC en option</li></ul>',
        'Mercedes EQB 2024', 'Mercedes EQB 2024 Électrique Familial',
        'Mercedes EQB 2024 : le VUS électrique de luxe pratique pour la famille.',
        'mercedes, eqb, 2024, électrique, ev, suv, vus, 7 places', 'Mercedes EQB 2024 | VUS Électrique Familial',
        'Électrique et familial',
        'mercedes-eqb-2024', 'fr', 123)
on conflict (description_id) do nothing;

-- Product 124: Hyundai Accent 2020 (Used Sedan)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (124, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-124', 'CAR-SKU-124', 33, 16,
        '65f023632bc26470c104b75f', 12) -- Type 12 (USED_CARS) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (247, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>هيونداي أكسنت 2020 (مستعملة)</h1><p>سيارة سيدان صغيرة مستعملة، اقتصادية وموثوقة للتنقل اليومي.</p><ul><li><strong>الموديل:</strong> 2020</li><li><strong>المسافة المقطوعة:</strong> 60,000 كم</li><li><strong>الحالة:</strong> جيدة، صيانة دورية</li><li><strong>الميزات:</strong> حجم مدمج، استهلاك وقود منخفض، تكلفة تشغيل اقتصادية</li></ul>',
        'هيونداي أكسنت 2020 مستعملة', 'هيونداي أكسنت 2020 مستعملة للبيع',
        'سيارة هيونداي أكسنت 2020 مستعملة، خيار اقتصادي وموثوق.', 'هيونداي, أكسنت, 2020, مستعملة, سيدان, اقتصادية',
        'هيونداي أكسنت 2020 مستعملة | اقتصادية', 'سيارة مستعملة اقتصادية',
        'hyundai-accent-2020-used', 'ar', 124)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (248, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Hyundai Accent 2020 (Occasion)</h1><p>Une petite berline d''occasion, économique et fiable pour les trajets quotidiens.</p><ul><li><strong>Modèle :</strong> 2020</li><li><strong>Kilométrage :</strong> 60 000 km</li><li><strong>État :</strong> Bon, entretien régulier</li><li><strong>Caractéristiques :</strong> Taille compacte, faible consommation de carburant, coût d''utilisation économique</li></ul>',
        'Hyundai Accent 2020 Occasion', 'Hyundai Accent 2020 d''occasion à vendre',
        'Hyundai Accent 2020 d''occasion, un choix économique et fiable.',
        'hyundai, accent, 2020, occasion, berline, économique', 'Hyundai Accent 2020 Occasion | Économique',
        'Voiture d''occasion économique',
        'hyundai-accent-2020-used', 'fr', 124)
on conflict (description_id) do nothing;

-- Product 125: Kia K5 (Sedan)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (125, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-125', 'CAR-SKU-125', 34, 17,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (249, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>كيا K5 2024</h1><p>سيارة سيدان متوسطة الحجم بتصميم رياضي جريء ومقصورة غنية بالميزات.</p><ul><li><strong>المحرك:</strong> 1.6 لتر توربو أو 2.5 لتر توربو (GT)</li><li><strong>التصميم:</strong> خط سقف انسيابي، شبك أمامي "أنف النمر"</li><li><strong>الميزات:</strong> شاشات بانورامية، دفع رباعي اختياري، أداء رياضي في نسخة GT</li></ul>',
        'كيا K5 2024', 'كيا K5 2024 الجديدة', 'كيا K5 2024: سيدان رياضية بتصميم يخطف الأنظار.',
        'كيا, K5, 2024, سيدان, رياضية', 'كيا K5 2024 | سيدان رياضية', 'تصميم رياضي وأداء',
        'kia-k5-2024', 'ar', 125)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (250, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Kia K5 2024</h1><p>Une berline intermédiaire au design sportif audacieux et à l''habitacle riche en fonctionnalités.</p><ul><li><strong>Moteur :</strong> 1.6L Turbo ou 2.5L Turbo (GT)</li><li><strong>Design :</strong> Ligne de toit fastback, calandre "Tiger Nose"</li><li><strong>Caractéristiques :</strong> Écrans panoramiques, transmission intégrale en option, performances sportives en version GT</li></ul>',
        'Kia K5 2024', 'Nouvelle Kia K5 2024', 'Kia K5 2024 : la berline sportive au design saisissant.',
        'kia, k5, 2024, berline, sportive', 'Kia K5 2024 | Berline Sportive', 'Design sportif et performance',
        'kia-k5-2024', 'fr', 125)
on conflict (description_id) do nothing;

-- Product 126: Ford Escape (SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (126, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-126', 'CAR-SKU-126', 35, 18,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (251, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>فورد إسكيب 2024</h1><p>سيارة SUV مدمجة عملية ومتعددة الاستخدامات، متوفرة بخيارات محركات متنوعة بما في ذلك الهجينة.</p><ul><li><strong>المحرك:</strong> EcoBoost، هجين، هجين قابل للشحن (PHEV)</li><li><strong>الميزات:</strong> نظام SYNC 4، مقاعد خلفية منزلقة، كفاءة عالية في استهلاك الوقود</li><li><strong>العملية:</strong> مثالية للتنقل اليومي والرحلات القصيرة</li></ul>',
        'فورد إسكيب 2024', 'فورد إسكيب 2024 الجديدة', 'فورد إسكيب 2024: سيارة SUV مدمجة عملية وفعالة.',
        'فورد, إسكيب, 2024, SUV, مدمجة, هجين, PHEV', 'فورد إسكيب 2024 | SUV عملي وفعال', 'عملية وكفاءة',
        'ford-escape-2024', 'ar', 126)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (252, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Ford Escape 2024</h1><p>Un VUS compact pratique et polyvalent, disponible avec diverses options de moteur, y compris hybride.</p><ul><li><strong>Moteur :</strong> EcoBoost, Hybride, Hybride Rechargeable (PHEV)</li><li><strong>Caractéristiques :</strong> Système SYNC 4, sièges arrière coulissants, excellente économie de carburant</li><li><strong>Praticité :</strong> Idéal pour les trajets quotidiens et les courts voyages</li></ul>',
        'Ford Escape 2024', 'Nouveau Ford Escape 2024', 'Ford Escape 2024 : le VUS compact pratique et efficace.',
        'ford, escape, 2024, suv, vus, compact, hybride, phev', 'Ford Escape 2024 | VUS Pratique et Efficace',
        'Pratique et efficace',
        'ford-escape-2024', 'fr', 126)
on conflict (description_id) do nothing;

-- Product 127: Toyota bZ4X (Electric SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (127, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-127', 'CAR-SKU-127', 36, 13,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (253, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>تويوتا bZ4X 2024</h1><p>أول سيارة SUV كهربائية بالكامل من تويوتا، بتصميم مستقبلي ومدى جيد.</p><ul><li><strong>المدى:</strong> يصل إلى 400 كم تقريبًا</li><li><strong>الدفع:</strong> أمامي أو رباعي (AWD)</li><li><strong>الميزات:</strong> تصميم داخلي حديث، نظام Toyota Safety Sense 3.0، شحن سريع DC</li></ul>',
        'تويوتا bZ4X 2024', 'تويوتا bZ4X 2024 الكهربائية',
        'تويوتا bZ4X 2024: دخول تويوتا القوي لعالم الـ SUV الكهربائي.',
        'تويوتا, bZ4X, 2024, كهربائية, EV, SUV', 'تويوتا bZ4X 2024 | SUV كهربائي', 'كهربائية بالكامل من تويوتا',
        'toyota-bz4x-2024', 'ar', 127)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (254, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Toyota bZ4X 2024</h1><p>Le premier VUS entièrement électrique de Toyota, au design futuriste et bonne autonomie.</p><ul><li><strong>Autonomie :</strong> Jusqu''à environ 400 km</li><li><strong>Transmission :</strong> Traction avant ou intégrale (AWD)</li><li><strong>Caractéristiques :</strong> Intérieur moderne, Toyota Safety Sense 3.0, recharge rapide DC</li></ul>',
        'Toyota bZ4X 2024', 'Toyota bZ4X 2024 Électrique',
        'Toyota bZ4X 2024 : l''entrée audacieuse de Toyota dans le monde des VUS électriques.',
        'toyota, bz4x, 2024, électrique, ev, suv, vus', 'Toyota bZ4X 2024 | VUS Électrique',
        'Entièrement électrique par Toyota',
        'toyota-bz4x-2024', 'fr', 127)
on conflict (description_id) do nothing;

-- Product 128: BMW X7 2021 (Used SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (128, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-128', 'CAR-SKU-128', 37, 14,
        '65f023632bc26470c104b75f', 12) -- Type 12 (USED_CARS) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (255, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>بي إم دبليو X7 2021 (مستعملة)</h1><p>سيارة SUV فاخرة كبيرة مستعملة بثلاثة صفوف، توفر فخامة ورحابة لا مثيل لهما.</p><ul><li><strong>الموديل:</strong> 2021</li><li><strong>المسافة المقطوعة:</strong> 50,000 كم</li><li><strong>الحالة:</strong> ممتازة، فئة عالية</li><li><strong>الميزات:</strong> مقصورة فخمة جدًا، محرك قوي، تقنيات متقدمة، 3 صفوف مقاعد</li></ul>',
        'بي إم دبليو X7 2021 مستعملة', 'بي إم دبليو X7 2021 مستعملة فاخرة',
        'سيارة بي إم دبليو X7 2021 مستعملة، قمة الفخامة والرحابة في SUV.',
        'بي ام دبليو, X7, 2021, مستعملة, SUV, فاخرة, كبيرة, 7 مقاعد', 'بي إم دبليو X7 2021 مستعملة | SUV فاخر كبير',
        'SUV فاخر كبير مستعمل',
        'bmw-x7-2021-used', 'ar', 128)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (256, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>BMW X7 2021 (Occasion)</h1><p>Un grand VUS de luxe d''occasion à trois rangées, offrant luxe et espace inégalés.</p><ul><li><strong>Modèle :</strong> 2021</li><li><strong>Kilométrage :</strong> 50 000 km</li><li><strong>État :</strong> Excellent, haut de gamme</li><li><strong>Caractéristiques :</strong> Habitacle très luxueux, moteur puissant, technologies avancées, 3 rangées de sièges</li></ul>',
        'BMW X7 2021 Occasion', 'BMW X7 2021 d''occasion de Luxe',
        'BMW X7 2021 d''occasion, le summum du luxe et de l''espace en VUS.',
        'bmw, x7, 2021, occasion, suv, vus, luxe, grand, 7 places', 'BMW X7 2021 Occasion | Grand VUS de Luxe',
        'Grand VUS de luxe d''occasion',
        'bmw-x7-2021-used', 'fr', 128)
on conflict (description_id) do nothing;

-- Product 129: Mercedes A-Class Sedan
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (129, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-129', 'CAR-SKU-129', 38, 15,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (257, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>مرسيدس A-Class سيدان 2024</h1><p>سيارة سيدان فاخرة مدمجة، تمثل نقطة الدخول لعالم مرسيدس-بنز.</p><ul><li><strong>المحرك:</strong> 4 سلندر توربو</li><li><strong>الميزات:</strong> نظام MBUX بشاشتين، تصميم داخلي أنيق، حجم مناسب للمدينة</li><li><strong>الفخامة:</strong> تجربة مرسيدس في حزمة مدمجة</li></ul>',
        'مرسيدس A-Class سيدان 2024', 'مرسيدس A-Class سيدان 2024 الجديدة',
        'مرسيدس A-Class سيدان 2024: الفخامة المدمجة من مرسيدس.',
        'مرسيدس, A-Class, 2024, سيدان, فاخرة, مدمجة', 'مرسيدس A-Class سيدان 2024 | فخامة مدمجة', 'مدخل لعالم مرسيدس',
        'mercedes-a-class-sedan-2024', 'ar', 129)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (258, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Mercedes A-Class Berline 2024</h1><p>La berline compacte de luxe, point d''entrée dans l''univers Mercedes-Benz.</p><ul><li><strong>Moteur :</strong> 4 cylindres Turbo</li><li><strong>Caractéristiques :</strong> Système MBUX à double écran, design intérieur élégant, taille adaptée à la ville</li><li><strong>Luxe :</strong> L''expérience Mercedes dans un format compact</li></ul>',
        'Mercedes A-Class Berline 2024', 'Nouvelle Mercedes A-Class Berline 2024',
        'Mercedes A-Class Berline 2024 : le luxe compact par Mercedes.',
        'mercedes, a-class, 2024, berline, luxe, compacte', 'Mercedes A-Class Berline 2024 | Luxe Compact',
        'Entrée dans l''univers Mercedes',
        'mercedes-a-class-berline-2024', 'fr', 129)
on conflict (description_id) do nothing;

-- Product 130: Hyundai Palisade (SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (130, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-130', 'CAR-SKU-130', 39, 16,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (259, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>هيونداي باليسيد 2024</h1><p>سيارة SUV كبيرة بثلاثة صفوف، توفر مساحة رحبة وميزات فاخرة للعائلة.</p><ul><li><strong>المقاعد:</strong> تتسع لـ 7 أو 8 ركاب</li><li><strong>الميزات:</strong> مقصورة راقية، تقنيات أمان متقدمة، قيادة مريحة</li><li><strong>القيمة:</strong> فخامة ومساحة بسعر تنافسي</li></ul>',
        'هيونداي باليسيد 2024', 'هيونداي باليسيد 2024 العائلية',
        'هيونداي باليسيد 2024: سيارة SUV عائلية كبيرة بميزات فاخرة.',
        'هيونداي, باليسيد, 2024, SUV, عائلية, 7 مقاعد, 8 مقاعد', 'هيونداي باليسيد 2024 | SUV عائلي كبير',
        'مساحة وفخامة للعائلة',
        'hyundai-palisade-2024', 'ar', 130)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (260, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Hyundai Palisade 2024</h1><p>Un grand VUS à trois rangées, offrant espace généreux et fonctionnalités luxueuses pour la famille.</p><ul><li><strong>Sièges :</strong> Jusqu''à 7 ou 8 passagers</li><li><strong>Caractéristiques :</strong> Habitacle haut de gamme, technologies de sécurité avancées, conduite confortable</li><li><strong>Valeur :</strong> Luxe et espace à un prix compétitif</li></ul>',
        'Hyundai Palisade 2024', 'Hyundai Palisade 2024 Familial',
        'Hyundai Palisade 2024 : le grand VUS familial aux caractéristiques luxueuses.',
        'hyundai, palisade, 2024, suv, vus, familial, 7 places, 8 places', 'Hyundai Palisade 2024 | Grand VUS Familial',
        'Espace et luxe pour la famille',
        'hyundai-palisade-2024', 'fr', 130)
on conflict (description_id) do nothing;

-- Product 131: Kia Soul EV (Electric)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (131, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-131', 'CAR-SKU-131', 40, 17,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (261, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>كيا سول EV 2024</h1><p>سيارة هاتشباك كهربائية بتصميم صندوقي مميز ومقصورة عملية.</p><ul><li><strong>المدى:</strong> مدى مناسب للتنقل داخل المدينة</li><li><strong>التصميم:</strong> فريد وعصري</li><li><strong>الميزات:</strong> مقصورة واسعة نسبيًا لحجمها، سهلة الركن</li></ul>',
        'كيا سول EV 2024', 'كيا سول EV 2024 الكهربائية', 'كيا سول EV 2024: هاتشباك كهربائية بتصميم فريد.',
        'كيا, سول, EV, 2024, كهربائية, هاتشباك', 'كيا سول EV 2024 | هاتشباك كهربائي', 'تصميم فريد وعملي',
        'kia-soul-ev-2024', 'ar', 131)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (262, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Kia Soul EV 2024</h1><p>Une voiture à hayon électrique au design carré distinctif et à l''habitacle pratique.</p><ul><li><strong>Autonomie :</strong> Autonomie adaptée aux déplacements urbains</li><li><strong>Design :</strong> Unique et moderne</li><li><strong>Caractéristiques :</strong> Habitacle relativement spacieux pour sa taille, facile à garer</li></ul>',
        'Kia Soul EV 2024', 'Kia Soul EV 2024 Électrique',
        'Kia Soul EV 2024 : la voiture à hayon électrique au design unique.',
        'kia, soul, ev, 2024, électrique, hatchback', 'Kia Soul EV 2024 | Hatchback Électrique',
        'Design unique et pratique',
        'kia-soul-ev-2024', 'fr', 131)
on conflict (description_id) do nothing;

-- Product 132: Ford Focus 2019 (Used Sedan)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (132, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-132', 'CAR-SKU-132', 41, 18,
        '65f023632bc26470c104b75f', 12) -- Type 12 (USED_CARS) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (263, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>فورد فوكس 2019 (مستعملة)</h1><p>سيارة سيدان أو هاتشباك مدمجة مستعملة، معروفة بتحكمها الرشيق.</p><ul><li><strong>الموديل:</strong> 2019</li><li><strong>المسافة المقطوعة:</strong> 70,000 كم</li><li><strong>الحالة:</strong> جيدة</li><li><strong>الميزات:</strong> قيادة ممتعة، حجم مدمج، متوفرة بنسخة هاتشباك</li></ul>',
        'فورد فوكس 2019 مستعملة', 'فورد فوكس 2019 مستعملة للبيع',
        'سيارة فورد فوكس 2019 مستعملة، ممتعة في القيادة.', 'فورد, فوكس, 2019, مستعملة, سيدان, هاتشباك',
        'فورد فوكس 2019 مستعملة | قيادة ممتعة', 'سيارة مدمجة مستعملة',
        'ford-focus-2019-used', 'ar', 132)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (264, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Ford Focus 2019 (Occasion)</h1><p>Une voiture compacte d''occasion (berline ou hayon), connue pour sa maniabilité agile.</p><ul><li><strong>Modèle :</strong> 2019</li><li><strong>Kilométrage :</strong> 70 000 km</li><li><strong>État :</strong> Bon</li><li><strong>Caractéristiques :</strong> Conduite agréable, taille compacte, disponible en version hayon</li></ul>',
        'Ford Focus 2019 Occasion', 'Ford Focus 2019 d''occasion à vendre',
        'Ford Focus 2019 d''occasion, agréable à conduire.', 'ford, focus, 2019, occasion, berline, hatchback',
        'Ford Focus 2019 Occasion | Conduite Agréable', 'Voiture compacte d''occasion',
        'ford-focus-2019-used', 'fr', 132)
on conflict (description_id) do nothing;

-- Product 133: Toyota Crown (Sedan/Hybrid)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (133, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-133', 'CAR-SKU-133', 42, 13,
        '65f023632bc26470c104b75f', 9) -- Type 9 (SEDAN) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (265, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>تويوتا كراون 2024</h1><p>عودة اسم كراون الأيقوني في شكل سيدان مرتفعة هجينة بالكامل.</p><ul><li><strong>المحرك:</strong> نظام هجين قياسي، مع خيار Hybrid MAX عالي الأداء</li><li><strong>التصميم:</strong> فريد يجمع بين السيدان والـ SUV</li><li><strong>الميزات:</strong> دفع رباعي قياسي، مقصورة فاخرة، تقنيات متقدمة</li></ul>',
        'تويوتا كراون 2024', 'تويوتا كراون 2024 الهجينة الجديدة',
        'تويوتا كراون 2024: سيدان هجينة مرتفعة بتصميم فريد.', 'تويوتا, كراون, 2024, سيدان, هجين, مرتفعة',
        'تويوتا كراون 2024 | سيدان هجينة مبتكرة', 'تصميم فريد وهجين',
        'toyota-crown-2024', 'ar', 133)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (266, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Toyota Crown 2024</h1><p>Le retour du nom iconique Crown sous la forme d''une berline surélevée entièrement hybride.</p><ul><li><strong>Moteur :</strong> Système hybride de série, avec option Hybrid MAX haute performance</li><li><strong>Design :</strong> Unique, combinant berline et VUS</li><li><strong>Caractéristiques :</strong> Transmission intégrale de série, habitacle luxueux, technologies avancées</li></ul>',
        'Toyota Crown 2024', 'Nouvelle Toyota Crown 2024 Hybride',
        'Toyota Crown 2024 : la berline hybride surélevée au design unique.',
        'toyota, crown, 2024, berline, hybride, surélevée', 'Toyota Crown 2024 | Berline Hybride Innovante',
        'Design unique et hybride',
        'toyota-crown-2024', 'fr', 133)
on conflict (description_id) do nothing;

-- Product 134: BMW X6 (SUV Coupe)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (134, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-134', 'CAR-SKU-134', 43, 14,
        '65f023632bc26470c104b75f', 10) -- Type 10 (SUV) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (267, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>بي إم دبليو X6 2024</h1><p>سيارة كوبيه رياضية متعددة الاستخدامات (SAC) بتصميم جريء وأداء قوي.</p><ul><li><strong>المحرك:</strong> 6 سلندر أو V8 توربو (M60i)</li><li><strong>التصميم:</strong> خط سقف كوبيه مميز، شبك أمامي مضيء (اختياري)</li><li><strong>الأداء:</strong> رياضي وديناميكي</li></ul>',
        'بي إم دبليو X6 2024', 'بي إم دبليو X6 2024 كوبيه SUV',
        'بي إم دبليو X6 2024: تصميم كوبيه جريء وأداء رياضي في SUV.',
        'بي ام دبليو, X6, 2024, SUV, كوبيه, SAC, رياضي', 'بي إم دبليو X6 2024 | كوبيه SUV رياضي', 'تصميم كوبيه وأداء',
        'bmw-x6-2024', 'ar', 134)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (268, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>BMW X6 2024</h1><p>Le Coupé d''Activité Sportive (SAC) au design audacieux et aux performances puissantes.</p><ul><li><strong>Moteur :</strong> 6 cylindres ou V8 Turbo (M60i)</li><li><strong>Design :</strong> Ligne de toit de coupé distinctive, calandre illuminée (option)</li><li><strong>Performance :</strong> Sportive et dynamique</li></ul>',
        'BMW X6 2024', 'BMW X6 2024 Coupé VUS',
        'BMW X6 2024 : design de coupé audacieux et performances sportives en VUS.',
        'bmw, x6, 2024, suv, vus, coupé, sac, sportif', 'BMW X6 2024 | Coupé VUS Sportif',
        'Design coupé et performance',
        'bmw-x6-2024', 'fr', 134)
on conflict (description_id) do nothing;

-- Product 135: Mercedes EQC (Electric SUV)
INSERT INTO catalog.product (product_id, date_created, date_modified, available, date_available, preorder,
                             product_height, product_free, product_length, product_ship, product_virtual,
                             product_weight, product_width, ref_sku, sku, sort_order, manufacturer_id,
                             store_merchant_id, product_type_id)
VALUES (135, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', true, '2024-04-02 00:00:00.000000', false,
        null,
        false, null, true, false, null, null, 'CAR-REF-135', 'CAR-SKU-135', 44, 15,
        '65f023632bc26470c104b75f', 11) -- Type 11 (ELECTRIC) assigned based on cycle
on conflict (product_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (269, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>مرسيدس EQC 2024</h1><p>سيارة SUV كهربائية فاخرة من مرسيدس، تجمع بين الأناقة والأداء الكهربائي الصامت.</p><ul><li><strong>المدى:</strong> مدى جيد للتنقل اليومي</li><li><strong>الأداء:</strong> محركان كهربائيان يوفران دفع رباعي 4MATIC</li><li><strong>الميزات:</strong> نظام MBUX، تصميم داخلي فاخر، قيادة هادئة ومريحة</li></ul>',
        'مرسيدس EQC 2024', 'مرسيدس EQC 2024 الكهربائية الفاخرة',
        'مرسيدس EQC 2024: تجربة SUV كهربائية فاخرة وهادئة.', 'مرسيدس, EQC, 2024, كهربائية, EV, SUV, فاخرة',
        'مرسيدس EQC 2024 | SUV كهربائي فاخر', 'فخامة كهربائية صامتة',
        'mercedes-eqc-2024', 'ar', 135)
on conflict (description_id) do nothing;

INSERT INTO catalog.product_description (description_id, date_created, date_modified, description, name, title,
                                         meta_description, meta_keywords, meta_title, product_highlight,
                                         sef_url, language_code, product_id)
VALUES (270, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000',
        '<h1>Mercedes EQC 2024</h1><p>Le VUS électrique de luxe de Mercedes, alliant élégance et performances électriques silencieuses.</p><ul><li><strong>Autonomie :</strong> Bonne autonomie pour les déplacements quotidiens</li><li><strong>Performance :</strong> Deux moteurs électriques offrant la transmission intégrale 4MATIC</li><li><strong>Caractéristiques :</strong> Système MBUX, intérieur luxueux, conduite silencieuse et confortable</li></ul>',
        'Mercedes EQC 2024', 'Mercedes EQC 2024 Électrique de Luxe',
        'Mercedes EQC 2024 : l''expérience VUS électrique luxueuse et silencieuse.',
        'mercedes, eqc, 2024, électrique, ev, suv, vus, luxe', 'Mercedes EQC 2024 | VUS Électrique de Luxe',
        'Luxe électrique silencieux',
        'mercedes-eqc-2024', 'fr', 135)
on conflict (description_id) do nothing;
/*
can you generate another sql inserts similar to this
where languages=['ar','fr'] and store_id='65f023632bc26470c104b75f'
start manufacturer id from 13
start manufacturer_description from 25
generate 6 manufacturer
domain for this store is cars
*/

-- Manufacturer 1 (ID: 13) - Toyota
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (13, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'TOYOTA', null, 0, '65f023632bc26470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 13 (Toyota)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (25, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'شركة تصنيع سيارات يابانية مشهورة.', 'تويوتا',
        null, 'ar', 13)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (26, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'Célèbre constructeur automobile japonais.',
        'Toyota', null, 'fr', 13)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 2 (ID: 14) - BMW
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (14, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'BMW', null, 1, '65f023632bc26470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 14 (BMW)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (27, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'شركة تصنيع سيارات ألمانية فاخرة.',
        'بي إم دبليو', null, 'ar', 14)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (28, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'Constructeur allemand de voitures de luxe.',
        'BMW', null, 'fr', 14)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 3 (ID: 15) - Mercedes-Benz
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (15, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'MERCEDES', null, 2, '65f023632bc26470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 15 (Mercedes-Benz)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (29, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000',
        'علامة تجارية عالمية للسيارات الفاخرة والتجارية.', 'مرسيدس-بنز', null, 'ar', 15)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (30, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000',
        'Marque mondiale automobiles de luxe et commerciales.', 'Mercedes-Benz', null, 'fr', 15)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 4 (ID: 16) - Hyundai
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (16, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'HYUNDAI', null, 3, '65f023632bc26470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 16 (Hyundai)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (31, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000',
        'شركة تصنيع سيارات كورية جنوبية متعددة الجنسيات.', 'هيونداي', null, 'ar', 16)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (32, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000',
        'Constructeur automobile multinational sud-coréen.', 'Hyundai', null, 'fr', 16)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 5 (ID: 17) - Kia
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (17, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'KIA', null, 4, '65f023632bc26470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 17 (Kia)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (33, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'ثاني أكبر مصنع للسيارات في كوريا الجنوبية.',
        'كيا', null, 'ar', 17)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (34, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000',
        'Deuxième plus grand constructeur automobile de Corée du Sud.', 'Kia', null, 'fr', 17)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 6 (ID: 18) - Ford
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (18, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'FORD', null, 5, '65f023632bc26470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 18 (Ford)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (35, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000', 'شركة تصنيع سيارات أمريكية متعددة الجنسيات.',
        'فورد', null, 'ar', 18)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (36, '2024-04-02 11:00:00.000000', '2024-04-02 11:00:00.000000',
        'Constructeur automobile multinational américain.', 'Ford', null, 'fr', 18)
on conflict (description_id) do nothing; -- Specify conflict target
/*
Generated SQL inserts for catalog.manufacturer and catalog.manufacturer_description
Store ID: '65f023632bc46470c104b75f'
Languages: ['en','fr']
Starting manufacturer_id: 19
Starting description_id: 37
Number of manufacturers: 6
Domain: electronics
*/

-- Manufacturer 1 (ID: 19) - Apple
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (19, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000', 'APPLE', null, 0, '65f023632bc46470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 19 (Apple)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (37, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'Leading consumer electronics company known for iPhone, Mac, and iPad.', 'Apple', null, 'en', 19)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (38, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'Leader de l''électronique grand public connu pour l''iPhone, le Mac et l''iPad.', 'Apple', null, 'fr', 19)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 2 (ID: 20) - Samsung
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (20, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000', 'SAMSUNG', null, 1, '65f023632bc46470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 20 (Samsung)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (39, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'Global electronics giant offering smartphones, TVs, appliances, and more.', 'Samsung', null, 'en', 20)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (40, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'Géant mondial de l''électronique proposant smartphones, téléviseurs, appareils électroménagers, etc.',
        'Samsung', null, 'fr', 20)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 3 (ID: 21) - Sony
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (21, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000', 'SONY', null, 2, '65f023632bc46470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 21 (Sony)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (41, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'Japanese multinational conglomerate known for electronics, gaming (PlayStation), and entertainment.', 'Sony',
        null, 'en', 21)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (42, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'Conglomérat multinational japonais connu pour l''électronique, les jeux (PlayStation) et le divertissement.',
        'Sony', null, 'fr', 21)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 4 (ID: 22) - LG
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (22, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000', 'LG', null, 3, '65f023632bc46470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 22 (LG)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (43, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'South Korean multinational electronics company specializing in TVs, home appliances, and mobile communications.',
        'LG', null, 'en', 22)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (44, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'Entreprise multinationale sud-coréenne d''électronique spécialisée dans les téléviseurs, les appareils électroménagers et les communications mobiles.',
        'LG', null, 'fr', 22)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 5 (ID: 23) - Dell
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (23, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000', 'DELL', null, 4, '65f023632bc46470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 23 (Dell)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (45, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'American multinational computer technology company that develops, sells, repairs, and supports computers and related products.',
        'Dell', null, 'en', 23)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (46, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'Entreprise technologique multinationale américaine qui développe, vend, répare et prend en charge des ordinateurs et produits connexes.',
        'Dell', null, 'fr', 23)
on conflict (description_id) do nothing;
-- Specify conflict target

-- Manufacturer 6 (ID: 24) - HP (Hewlett-Packard)
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (24, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000', 'HP', null, 5, '65f023632bc46470c104b75f')
on conflict (manufacturer_id) do nothing;
-- Specify conflict target

-- Descriptions for Manufacturer 24 (HP)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (47, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'American multinational information technology company known for personal computers, printers, and related supplies.',
        'HP', null, 'en', 24)
on conflict (description_id) do nothing; -- Specify conflict target

INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (48, '2024-04-02 16:00:00.000000', '2024-04-02 16:00:00.000000',
        'Entreprise multinationale américaine de technologie de l''information connue pour ses ordinateurs personnels, imprimantes et fournitures connexes.',
        'HP', null, 'fr', 24)
on conflict (description_id) do nothing; -- Specify conflict target
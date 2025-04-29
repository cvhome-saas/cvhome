/*
Generated SQL inserts similar to the provided template
where languages=['fr','en'] and store_id='65f020632bc46470c104b76f'
start manufacturer id from 7
start manufacturer_description from 13
generate 6 manufacturer
domain for this store is beauté
Timestamps replaced with NOW()
Realistic beauty brand names and codes generated.
*/

-- Manufacturer 7: Yves Saint Laurent Beauté
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (7, NOW(), NOW(), 'YSL', null, 6, '65f020632bc46470c104b76f')
on conflict (manufacturer_id) do nothing;

-- French Description (ID: 13)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (13, NOW(), NOW(),
        'Yves Saint Laurent Beauté propose des produits de maquillage audacieux, des parfums iconiques et des soins de luxe.',
        'Yves Saint Laurent Beauté', null, 'fr', 7)
on conflict (description_id) do nothing;
-- English Description (ID: 14)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (14, NOW(), NOW(),
        'Yves Saint Laurent Beauté offers bold makeup products, iconic fragrances, and luxury skincare.',
        'Yves Saint Laurent Beauty', null, 'en', 7)
on conflict (description_id) do nothing;

-- Manufacturer 8: Guerlain
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (8, NOW(), NOW(), 'GUERLAIN', null, 7, '65f020632bc46470c104b76f')
on conflict (manufacturer_id) do nothing;

-- French Description (ID: 15)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (15, NOW(), NOW(),
        'Guerlain, maison parisienne depuis 1828, excelle dans l''art du parfum, du soin et du maquillage.', 'Guerlain',
        null, 'fr', 8)
on conflict (description_id) do nothing;
-- English Description (ID: 16)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (16, NOW(), NOW(),
        'Guerlain, a Parisian house since 1828, excels in the art of fragrance, skincare, and makeup.', 'Guerlain',
        null, 'en', 8)
on conflict (description_id) do nothing;

-- Manufacturer 9: Shiseido
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (9, NOW(), NOW(), 'SHISEIDO', null, 8, '65f020632bc46470c104b76f')
on conflict (manufacturer_id) do nothing;

-- French Description (ID: 17)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (17, NOW(), NOW(),
        'Shiseido combine la science et l''art japonais pour créer des soins de la peau et du maquillage de haute qualité.',
        'Shiseido', null, 'fr', 9)
on conflict (description_id) do nothing;
-- English Description (ID: 18)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (18, NOW(), NOW(), 'Shiseido combines Japanese science and art to create high-quality skincare and makeup.',
        'Shiseido', null, 'en', 9)
on conflict (description_id) do nothing;

-- Manufacturer 10: NARS Cosmetics
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (10, NOW(), NOW(), 'NARS', null, 9, '65f020632bc46470c104b76f')
on conflict (manufacturer_id) do nothing;

-- French Description (ID: 19)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (19, NOW(), NOW(),
        'NARS Cosmetics est célèbre pour ses couleurs riches, ses produits de maquillage iconiques et son approche moderne de la beauté.',
        'NARS Cosmetics', null, 'fr', 10)
on conflict (description_id) do nothing;
-- English Description (ID: 20)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (20, NOW(), NOW(),
        'NARS Cosmetics is famous for its rich colors, iconic makeup products, and modern approach to beauty.',
        'NARS Cosmetics', null, 'en', 10)
on conflict (description_id) do nothing;

-- Manufacturer 11: La Roche-Posay
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (11, NOW(), NOW(), 'LAROCHE', null, 10, '65f020632bc46470c104b76f')
on conflict (manufacturer_id) do nothing;

-- French Description (ID: 21)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (21, NOW(), NOW(),
        'La Roche-Posay est recommandée par les dermatologues pour ses soins formulés pour les peaux sensibles.',
        'La Roche-Posay', null, 'fr', 11)
on conflict (description_id) do nothing;
-- English Description (ID: 22)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (22, NOW(), NOW(),
        'La Roche-Posay is recommended by dermatologists for its skincare formulated for sensitive skin.',
        'La Roche-Posay', null, 'en', 11)
on conflict (description_id) do nothing;

-- Manufacturer 12: Kérastase
INSERT INTO catalog.manufacturer (manufacturer_id, date_created, date_modified, code, manufacturer_image, sort_order,
                                  store_merchant_id)
VALUES (12, NOW(), NOW(), 'KERASTASE', null, 11, '65f020632bc46470c104b76f')
on conflict (manufacturer_id) do nothing;

-- French Description (ID: 23)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (23, NOW(), NOW(), 'Kérastase offre des soins capillaires professionnels de luxe pour transformer les cheveux.',
        'Kérastase', null, 'fr', 12)
on conflict (description_id) do nothing;
-- English Description (ID: 24)
INSERT INTO catalog.manufacturer_description (description_id, date_created, date_modified, description, name,
                                              manufacturers_url, language_code, manufacturer_id)
VALUES (24, NOW(), NOW(), 'Kérastase offers luxury professional haircare to transform hair.', 'Kérastase', null, 'en',
        12)
on conflict (description_id) do nothing;
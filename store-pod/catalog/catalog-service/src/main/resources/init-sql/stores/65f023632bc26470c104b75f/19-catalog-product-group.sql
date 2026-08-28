/*
Generated SQL inserts for product groups based on product relationship file.
- Store ID: '65f023632bc26470c104b75f'
- Group Codes: 'HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS'
*/

-- Product Groups
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (9, true, 'HOME_PAGE', '65f023632bc26470c104b75f', null, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (10, true, 'RECOMMENDED', '65f023632bc26470c104b75f', null, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (11, true, 'NEWLY_ADDED', '65f023632bc26470c104b75f', null, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (12, true, 'FEATURED_ITEMS', '65f023632bc26470c104b75f', null, NOW(), NOW())
on conflict do nothing;

-- Group Descriptions (English & Arabic)
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (17, 'Home Page', 'en', 9, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (18, 'الصفحة الرئيسية', 'ar', 9, NOW(), NOW())
on conflict do nothing;

-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (19, 'Recommended', 'en', 10, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (20, 'مقترح لك', 'ar', 10, NOW(), NOW())
on conflict do nothing;

-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (21, 'Newly Added', 'en', 11, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (22, 'أضيف حديثا', 'ar', 11, NOW(), NOW())
on conflict do nothing;

-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (23, 'Featured Items', 'en', 12, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (24, 'منتجات مميزة', 'ar', 12, NOW(), NOW())
on conflict do nothing;

-- Product Group Memberships
INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (9, 91),
       (9, 92),
       (9, 93),
       (9, 94),
       (9, 95),
       (9, 96),
       (9, 97),
       (9, 98),
       (9, 99),
       (9, 100),
       (9, 101),
       (9, 102),
       (9, 103),
       (9, 104),
       (9, 105),
       (9, 106),
       (9, 107),
       (9, 108),
       (9, 109),
       (9, 110),
       (9, 111),
       (9, 112),
       (9, 113),
       (9, 114)
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (10, 115),
       (10, 117),
       (10, 119),
       (10, 121),
       (10, 123),
       (10, 125),
       (10, 127),
       (10, 129),
       (10, 131),
       (10, 133)
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (11, 135),
       (11, 134),
       (11, 132),
       (11, 130),
       (11, 128),
       (11, 126),
       (11, 124),
       (11, 122),
       (11, 120),
       (11, 118),
       (11, 116),
       (11, 114)
ON CONFLICT DO NOTHING;
INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (12, 91),
       (12, 95),
       (12, 99),
       (12, 103),
       (12, 107),
       (12, 111),
       (12, 115),
       (12, 119)
ON CONFLICT DO NOTHING;
-- French group names. The seed above carries English and Arabic only, so on the three storefronts that sell in
-- French every product rail dropped out of the home page: the storefront skips a group whose description does
-- not resolve in the requested locale. Ids are negative — description ids come from a sequence that only grows.
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-9, 'Page d''accueil', 'fr', 9, NOW(), NOW())
on conflict do nothing;
-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-10, 'Recommandé pour vous', 'fr', 10, NOW(), NOW())
on conflict do nothing;
-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-11, 'Nouveautés', 'fr', 11, NOW(), NOW())
on conflict do nothing;
-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-12, 'Notre sélection', 'fr', 12, NOW(), NOW())
on conflict do nothing;

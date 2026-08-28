/*
Generated SQL inserts for product groups based on product relationship file.
- Store ID: '65f020632bc46470c104b76f'
- Group Codes: 'HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS'
*/

-- Product Groups
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (13, true, 'HOME_PAGE', '65f020632bc46470c104b76f', null, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (14, true, 'RECOMMENDED', '65f020632bc46470c104b76f', null, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (15, true, 'NEWLY_ADDED', '65f020632bc46470c104b76f', null, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created,
                                   date_modified)
VALUES (16, true, 'FEATURED_ITEMS', '65f020632bc46470c104b76f', null, NOW(), NOW())
on conflict do nothing;

-- Group Descriptions (English & Arabic)
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (25, 'Home Page', 'en', 13, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (26, 'الصفحة الرئيسية', 'ar', 13, NOW(), NOW())
on conflict do nothing;

-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (27, 'Recommended', 'en', 14, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (28, 'مقترح لك', 'ar', 14, NOW(), NOW())
on conflict do nothing;

-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (29, 'Newly Added', 'en', 15, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (30, 'أضيف حديثا', 'ar', 15, NOW(), NOW())
on conflict do nothing;

-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (31, 'Featured Items', 'en', 16, NOW(), NOW())
on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created,
                                               date_modified)
VALUES (32, 'منتجات مميزة', 'ar', 16, NOW(), NOW())
on conflict do nothing;

-- Product Group Memberships
INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (13, 46),
       (13, 47),
       (13, 48),
       (13, 49),
       (13, 50),
       (13, 51),
       (13, 52),
       (13, 53),
       (13, 54),
       (13, 55),
       (13, 56),
       (13, 57),
       (13, 58),
       (13, 59),
       (13, 60),
       (13, 61),
       (13, 62),
       (13, 63),
       (13, 64),
       (13, 65),
       (13, 66),
       (13, 67),
       (13, 68),
       (13, 69)
ON CONFLICT DO NOTHING;

INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (14, 70),
       (14, 71),
       (14, 72),
       (14, 73),
       (14, 74),
       (14, 75),
       (14, 76),
       (14, 77),
       (14, 78),
       (14, 79),
       (14, 80),
       (14, 81)
ON CONFLICT DO NOTHING;

INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (15, 82),
       (15, 83),
       (15, 84),
       (15, 85),
       (15, 86),
       (15, 87),
       (15, 88),
       (15, 89),
       (15, 90),
       (15, 46)
ON CONFLICT DO NOTHING;

INSERT INTO catalog.product_group_product (product_group_id, product_id)
VALUES (16, 47),
       (16, 48),
       (16, 49),
       (16, 50),
       (16, 51),
       (16, 52),
       (16, 53),
       (16, 54)
ON CONFLICT DO NOTHING;
-- French group names. The seed above carries English and Arabic only, so on the three storefronts that sell in
-- French every product rail dropped out of the home page: the storefront skips a group whose description does
-- not resolve in the requested locale. Ids are negative — description ids come from a sequence that only grows.
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-13, 'Page d''accueil', 'fr', 13, NOW(), NOW())
on conflict do nothing;
-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-14, 'Recommandé pour vous', 'fr', 14, NOW(), NOW())
on conflict do nothing;
-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-15, 'Nouveautés', 'fr', 15, NOW(), NOW())
on conflict do nothing;
-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id,
                                               date_created, date_modified)
VALUES (-16, 'Notre sélection', 'fr', 16, NOW(), NOW())
on conflict do nothing;

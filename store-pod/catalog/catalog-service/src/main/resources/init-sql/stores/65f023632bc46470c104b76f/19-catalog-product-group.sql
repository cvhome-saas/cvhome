/*
Generated SQL inserts for product groups based on product relationship file.
- Store ID: '65f023632bc46470c104b76f'
- Group Codes: 'HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS'
*/

-- Product Groups
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (1, true, 'HOME_PAGE', '65f023632bc46470c104b76f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (2, true, 'RECOMMENDED', '65f023632bc46470c104b76f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (3, true, 'NEWLY_ADDED', '65f023632bc46470c104b76f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (4, true, 'FEATURED_ITEMS', '65f023632bc46470c104b76f', null, NOW(), NOW()) on conflict do nothing;

-- Group Descriptions (English & Arabic)
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (1, 'Home Page', 'en', 1, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (2, 'الصفحة الرئيسية', 'ar', 1, NOW(), NOW()) on conflict do nothing;

-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (3, 'Recommended', 'en', 2, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (4, 'مقترح لك', 'ar', 2, NOW(), NOW()) on conflict do nothing;

-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (5, 'Newly Added', 'en', 3, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (6, 'أضيف حديثا', 'ar', 3, NOW(), NOW()) on conflict do nothing;

-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (7, 'Featured Items', 'en', 4, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (8, 'منتجات مميزة', 'ar', 4, NOW(), NOW()) on conflict do nothing;

-- Product Group Memberships (Mirroring Relationship IDs 5-65)
-- HOME_PAGE (Products 1-24)
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20), (1, 21), (1, 22), (1, 23), (1, 24) on conflict do nothing;
-- RECOMMENDED (Products 16-27)
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (2, 16), (2, 17), (2, 18), (2, 19), (2, 20), (2, 21), (2, 22), (2, 23), (2, 24), (2, 25), (2, 26), (2, 27) on conflict do nothing;
-- NEWLY_ADDED (Products 31-45)
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (3, 31), (3, 32), (3, 33), (3, 34), (3, 35), (3, 36), (3, 37), (3, 38), (3, 39), (3, 40), (3, 41), (3, 42), (3, 43), (3, 44), (3, 45) on conflict do nothing;
-- FEATURED_ITEMS (Specific Luxury Products)
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (4, 5), (4, 6), (4, 11), (4, 12), (4, 17), (4, 18), (4, 23), (4, 24), (4, 29), (4, 30) on conflict do nothing;

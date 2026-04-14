/*
Generated SQL inserts for product groups based on product relationship file.
- Store ID: '65f023632bc26470c104b75f'
- Group Codes: 'HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS'
*/

-- Product Groups
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (201, true, 'HOME_PAGE', '65f023632bc26470c104b75f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (202, true, 'RECOMMENDED', '65f023632bc26470c104b75f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (203, true, 'NEWLY_ADDED', '65f023632bc26470c104b75f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (204, true, 'FEATURED_ITEMS', '65f023632bc26470c104b75f', null, NOW(), NOW()) on conflict do nothing;

-- Group Descriptions (English & Arabic)
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (201, 'Home Page', 'en', 201, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (202, 'الصفحة الرئيسية', 'ar', 201, NOW(), NOW()) on conflict do nothing;

-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (203, 'Recommended', 'en', 202, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (204, 'مقترح لك', 'ar', 202, NOW(), NOW()) on conflict do nothing;

-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (205, 'Newly Added', 'en', 203, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (206, 'أضيف حديثا', 'ar', 203, NOW(), NOW()) on conflict do nothing;

-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (207, 'Featured Items', 'en', 204, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (208, 'منتجات مميزة', 'ar', 204, NOW(), NOW()) on conflict do nothing;

-- Product Group Memberships
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (201, 1), (201, 2), (201, 3), (201, 4), (201, 5), (201, 6), (201, 7), (201, 8), (201, 9), (201, 10), (201, 11), (201, 12), (201, 13), (201, 14), (201, 15), (201, 16), (201, 17), (201, 18), (201, 19), (201, 20), (201, 21), (201, 22), (201, 23), (201, 24) on conflict do nothing;
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (202, 16), (202, 17), (202, 18), (202, 19), (202, 20), (202, 21), (202, 22), (202, 23), (202, 24), (202, 25), (202, 26), (202, 27) on conflict do nothing;
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (203, 31), (203, 32), (203, 33), (203, 34), (203, 35), (203, 36), (203, 37), (203, 38), (203, 39), (203, 40), (203, 41), (203, 42), (203, 43), (203, 44), (203, 45) on conflict do nothing;
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (204, 5), (204, 6), (204, 11), (204, 12), (204, 17), (204, 18), (204, 23), (204, 24), (204, 29), (204, 30) on conflict do nothing;

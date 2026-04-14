/*
Generated SQL inserts for product groups based on product relationship file.
- Store ID: '65f023632bc46470c104b75f'
- Group Codes: 'HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS'
*/

-- Product Groups
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (101, true, 'HOME_PAGE', '65f023632bc46470c104b75f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (102, true, 'RECOMMENDED', '65f023632bc46470c104b75f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (103, true, 'NEWLY_ADDED', '65f023632bc46470c104b75f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (104, true, 'FEATURED_ITEMS', '65f023632bc46470c104b75f', null, NOW(), NOW()) on conflict do nothing;

-- Group Descriptions (English & Arabic)
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (101, 'Home Page', 'en', 101, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (102, 'الصفحة الرئيسية', 'ar', 101, NOW(), NOW()) on conflict do nothing;

-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (103, 'Recommended', 'en', 102, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (104, 'مقترح لك', 'ar', 102, NOW(), NOW()) on conflict do nothing;

-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (105, 'Newly Added', 'en', 103, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (106, 'أضيف حديثا', 'ar', 103, NOW(), NOW()) on conflict do nothing;

-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (107, 'Featured Items', 'en', 104, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (108, 'منتجات مميزة', 'ar', 104, NOW(), NOW()) on conflict do nothing;

-- Product Group Memberships (Assuming Product IDs match relationship file)
-- IDs based on relationship file range (simplified to match IDs 1-45 logic)
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (101, 1), (101, 2), (101, 3), (101, 4), (101, 5), (101, 6), (101, 7), (101, 8), (101, 9), (101, 10), (101, 11), (101, 12), (101, 13), (101, 14), (101, 15), (101, 16), (101, 17), (101, 18), (101, 19), (101, 20), (101, 21), (101, 22), (101, 23), (101, 24) on conflict do nothing;
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (102, 16), (102, 17), (102, 18), (102, 19), (102, 20), (102, 21), (102, 22), (102, 23), (102, 24), (102, 25), (102, 26), (102, 27) on conflict do nothing;
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (103, 31), (103, 32), (103, 33), (103, 34), (103, 35), (103, 36), (103, 37), (103, 38), (103, 39), (103, 40), (103, 41), (103, 42), (103, 43), (103, 44), (103, 45) on conflict do nothing;
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (104, 5), (104, 6), (104, 11), (104, 12), (104, 17), (104, 18), (104, 23), (104, 24), (104, 29), (104, 30) on conflict do nothing;

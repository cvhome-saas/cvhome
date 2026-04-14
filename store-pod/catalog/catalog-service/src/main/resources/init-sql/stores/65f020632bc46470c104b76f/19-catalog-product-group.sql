/*
Generated SQL inserts for product groups based on product relationship file.
- Store ID: '65f020632bc46470c104b76f'
- Group Codes: 'HOME_PAGE', 'RECOMMENDED', 'NEWLY_ADDED', 'FEATURED_ITEMS'
*/

-- Product Groups
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (301, true, 'HOME_PAGE', '65f020632bc46470c104b76f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (302, true, 'RECOMMENDED', '65f020632bc46470c104b76f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (303, true, 'NEWLY_ADDED', '65f020632bc46470c104b76f', null, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group (product_group_id, active, code, store_merchant_id, parent_product_id, date_created, date_modified)
VALUES (304, true, 'FEATURED_ITEMS', '65f020632bc46470c104b76f', null, NOW(), NOW()) on conflict do nothing;

-- Group Descriptions (English & Arabic)
-- HOME_PAGE
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (301, 'Home Page', 'en', 301, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (302, 'الصفحة الرئيسية', 'ar', 301, NOW(), NOW()) on conflict do nothing;

-- RECOMMENDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (303, 'Recommended', 'en', 302, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (304, 'مقترح لك', 'ar', 302, NOW(), NOW()) on conflict do nothing;

-- NEWLY_ADDED
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (305, 'Newly Added', 'en', 303, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (306, 'أضيف حديثا', 'ar', 303, NOW(), NOW()) on conflict do nothing;

-- FEATURED_ITEMS
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (307, 'Featured Items', 'en', 304, NOW(), NOW()) on conflict do nothing;
INSERT INTO catalog.product_group_description (description_id, name, language_code, product_group_id, date_created, date_modified)
VALUES (308, 'منتجات مميزة', 'ar', 304, NOW(), NOW()) on conflict do nothing;

-- Product Group Memberships
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (301, 1), (301, 2), (301, 3), (301, 4), (301, 5), (301, 6), (301, 7), (301, 8), (301, 9), (301, 10), (301, 11), (301, 12), (301, 13), (301, 14), (301, 15), (301, 16), (301, 17), (301, 18), (301, 19), (301, 20), (301, 21), (301, 22), (301, 23), (301, 24) on conflict do nothing;
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (302, 16), (302, 17), (302, 18), (302, 19), (302, 20), (302, 21), (302, 22), (302, 23), (302, 24), (302, 25), (302, 26), (302, 27) on conflict do nothing;
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (303, 31), (303, 32), (303, 33), (303, 34), (303, 35), (303, 36), (303, 37), (303, 38), (303, 39), (303, 40), (303, 41), (303, 42), (303, 43), (303, 44), (303, 45) on conflict do nothing;
INSERT INTO catalog.product_group_product (product_group_id, product_id) VALUES (304, 5), (304, 6), (304, 11), (304, 12), (304, 17), (304, 18), (304, 23), (304, 24), (304, 29), (304, 30) on conflict do nothing;

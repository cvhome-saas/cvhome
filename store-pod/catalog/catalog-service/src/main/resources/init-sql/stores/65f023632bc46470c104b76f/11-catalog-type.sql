/*
can you generate another sql inserts similar to this
where languages=['ar','en'] and store_id='65f023632bc46470c104b76f'
start product_type_id id from 1
generate 4 product_type
domain for this store is fashion
*/

-- Product Type 1: Clothing
INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (1, NOW(), NOW(), 'CLOTHING', '65f023632bc46470c104b76f')
on conflict do nothing;

-- Product Type 2: Shoes
INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (2, NOW(), NOW(), 'SHOES', '65f023632bc46470c104b76f')
on conflict do nothing;

-- Product Type 3: Accessories
INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (3, NOW(), NOW(), 'ACCESSORIES', '65f023632bc46470c104b76f')
on conflict do nothing;

-- Product Type 4: Bags
INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (4, NOW(), NOW(), 'BAGS', '65f023632bc46470c104b76f')
on conflict do nothing;
/*
Generated SQL inserts similar to the provided template
where languages=['fr','en'] and store_id='65f020632bc46470c104b76f'
start product_type_id id from 5
generate 4 product_type
domain for this store is beauté
*/

-- Product Type 5: Skincare
INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (5, NOW(), NOW(), 'SKINCARE', '65f020632bc46470c104b76f')
on conflict (product_type_id) do nothing;

-- Product Type 6: Makeup
INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (6, NOW(), NOW(), 'MAKEUP', '65f020632bc46470c104b76f')
on conflict (product_type_id) do nothing;

-- Product Type 7: Fragrance
INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (7, NOW(), NOW(), 'FRAGRANCE', '65f020632bc46470c104b76f')
on conflict (product_type_id) do nothing;

-- Product Type 8: Haircare
INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (8, NOW(), NOW(), 'HAIRCARE', '65f020632bc46470c104b76f')
on conflict (product_type_id) do nothing;
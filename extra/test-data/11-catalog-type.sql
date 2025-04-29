/*
can you generate another sql inserts similar to this
where languages=['en','fr'] and store_id='65f023632bc46470c104b75f'
start product_type_id id from 13
generate 4 product_type
domain for this store is electronics
*/
INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (1, '2024-03-31 08:45:38.000000', '2024-03-31 08:45:38.000000', $generator.product_type_code(), $paramter.store_merchant_id)
on conflict do nothing;

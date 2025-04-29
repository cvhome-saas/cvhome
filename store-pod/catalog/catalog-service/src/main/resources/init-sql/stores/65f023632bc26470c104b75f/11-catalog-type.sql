/*
can you generate another sql inserts similar to this
where languages=['ar','fr'] and store_id='65f023632bc26470c104b75f'
start product_type_id id from 9
generate 4 product_type
domain for this store is cars
*/

-- Original Insert (for context)
-- INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
-- VALUES (1, '2024-03-31 08:45:38.000000', '2024-03-31 08:45:38.000000', $generator.product_type_code(), $paramter.store_merchant_id)
-- on conflict do nothing;

-- Generated Inserts for store '65f023632bc26470c104b75f' (Cars Domain)

INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (9, '2024-04-02 10:00:00.000000', '2024-04-02 10:00:00.000000', 'SEDAN', '65f023632bc26470c104b75f')
on conflict (product_type_id) do nothing; -- Specify conflict target

INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (10, '2024-04-02 10:00:00.000000', '2024-04-02 10:00:00.000000', 'SUV', '65f023632bc26470c104b75f')
on conflict (product_type_id) do nothing; -- Specify conflict target

INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (11, '2024-04-02 10:00:00.000000', '2024-04-02 10:00:00.000000', 'ELECTRIC', '65f023632bc26470c104b75f')
on conflict (product_type_id) do nothing; -- Specify conflict target

INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (12, '2024-04-02 10:00:00.000000', '2024-04-02 10:00:00.000000', 'USED_CARS', '65f023632bc26470c104b75f')
on conflict (product_type_id) do nothing; -- Specify conflict target
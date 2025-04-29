/*
Generated SQL inserts for catalog.product_type based on the original template.
Store ID: '65f023632bc46470c104b75f'
Languages: ['en', 'fr']
Starting product_type_id: 13
Number of product types: 4
Domain: electronics
*/

-- Original Insert (for context)
-- INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
-- VALUES (1, '2024-03-31 08:45:38.000000', '2024-03-31 08:45:38.000000', $generator.product_type_code(), $paramter.store_merchant_id)
-- on conflict do nothing;

-- Generated Inserts for store '65f023632bc46470c104b75f' (Electronics Domain)

INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (13, '2024-04-02 15:00:00.000000', '2024-04-02 15:00:00.000000', 'SMARTPHONES', '65f023632bc46470c104b75f')
on conflict (product_type_id) do nothing; -- Specify conflict target

INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (14, '2024-04-02 15:00:00.000000', '2024-04-02 15:00:00.000000', 'LAPTOPS', '65f023632bc46470c104b75f')
on conflict (product_type_id) do nothing; -- Specify conflict target

INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (15, '2024-04-02 15:00:00.000000', '2024-04-02 15:00:00.000000', 'HEADPHONES', '65f023632bc46470c104b75f')
on conflict (product_type_id) do nothing; -- Specify conflict target

INSERT INTO catalog.product_type (product_type_id, date_created, date_modified, prd_type_code, store_merchant_id)
VALUES (16, '2024-04-02 15:00:00.000000', '2024-04-02 15:00:00.000000', 'TELEVISIONS', '65f023632bc46470c104b75f')
on conflict (product_type_id) do nothing; -- Specify conflict target
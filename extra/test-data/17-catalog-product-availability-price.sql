/*
generate more product_availability  based on product file  and add product_price for every product_availability using product_avail_id column as foreign key
start product_availability=136 product_price=136 product_price_description=271
where languages=['en','fr']
*/
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (1, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f023632bc46470c104b75f', 1)
on conflict do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (1, 'base', true, 200.00, 'ONE_TIME', 1)
on conflict do nothing;

-- <<Begin Loop on $parameter.languages l
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (1, '2024-03-31 15:26:49.000000', '2024-03-31 15:26:49.000000', '', 'DEFAULT', '', $each.l, 1)
on conflict do nothing;
-- End Loop>>

/*
Generated SQL inserts for product availability and pricing based on products 1-15.
- Starts product_avail_id from 1.
- Starts product_price_id from 1.
- Starts product_price_description description_id from 1.
- Sets reasonable quantities and prices based on product type/brand.
- Uses store_id = '65f023632bc46470c104b76f'.
- Includes descriptions for languages 'en' and 'ar'.
- Uses product details (weight, dimensions) from the product file where available.
*/

-- Product 1: Nike Running Shoes
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (1, true, 12.0, 30.0, 0.8, 10.0, false, 25, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 1)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (1, 'base', true, 750.00, 'ONE_TIME', 1)
on conflict (product_price_id) do nothing;

-- English Description (ID: 1)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (1, NOW(), NOW(), '', 'DEFAULT', '', 'en', 1)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 2)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (2, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 1)
on conflict (description_id) do nothing;


-- Product 2: Zara Women's Midi Dress
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (2, true, null, null, 0.5, null, false, 40, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 2)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (2, 'base', true, 350.00, 'ONE_TIME', 2)
on conflict (product_price_id) do nothing;

-- English Description (ID: 3)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (3, NOW(), NOW(), '', 'DEFAULT', '', 'en', 2)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 4)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (4, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 2)
on conflict (description_id) do nothing;


-- Product 3: Adidas Men's Track Pants
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (3, true, null, null, 0.5, null, false, 35, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 3)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (3, 'base', true, 320.00, 'ONE_TIME', 3)
on conflict (product_price_id) do nothing;

-- English Description (ID: 5)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (5, NOW(), NOW(), '', 'DEFAULT', '', 'en', 3)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 6)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (6, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 3)
on conflict (description_id) do nothing;


-- Product 4: H&M Women's Knit Sweater
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (4, true, null, null, 0.6, null, false, 50, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 4)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (4, 'base', true, 280.00, 'ONE_TIME', 4)
on conflict (product_price_id) do nothing;

-- English Description (ID: 7)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (7, NOW(), NOW(), '', 'DEFAULT', '', 'en', 4)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 8)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (8, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 4)
on conflict (description_id) do nothing;


-- Product 5: Gucci Marmont Shoulder Bag
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (5, true, 15.0, 26.0, 0.7, 7.0, false, 5, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 5)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (5, 'base', true, 8500.00, 'ONE_TIME', 5) -- Higher price for Gucci
on conflict (product_price_id) do nothing;

-- English Description (ID: 9)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (9, NOW(), NOW(), '', 'DEFAULT', '', 'en', 5)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 10)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (10, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 5)
on conflict (description_id) do nothing;


-- Product 6: Chanel Sunglasses
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (6, true, null, null, 0.1, null, false, 8, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 6)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (6, 'base', true, 2100.00, 'ONE_TIME', 6) -- Higher price for Chanel
on conflict (product_price_id) do nothing;

-- English Description (ID: 11)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (11, NOW(), NOW(), '', 'DEFAULT', '', 'en', 6)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 12)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (12, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 6)
on conflict (description_id) do nothing;


-- Product 7: Nike Kids' Hoodie
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (7, true, null, null, 0.4, null, false, 30, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 7)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (7, 'base', true, 300.00, 'ONE_TIME', 7)
on conflict (product_price_id) do nothing;

-- English Description (ID: 13)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (13, NOW(), NOW(), '', 'DEFAULT', '', 'en', 7)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 14)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (14, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 7)
on conflict (description_id) do nothing;


-- Product 8: Zara Men's Sneakers
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (8, true, null, null, 0.9, null, false, 22, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 8)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (8, 'base', true, 420.00, 'ONE_TIME', 8)
on conflict (product_price_id) do nothing;

-- English Description (ID: 15)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (15, NOW(), NOW(), '', 'DEFAULT', '', 'en', 8)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 16)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (16, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 8)
on conflict (description_id) do nothing;


-- Product 9: Adidas Women's Backpack
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (9, true, 45.0, 30.0, 0.5, 15.0, false, 18, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 9)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (9, 'base', true, 250.00, 'ONE_TIME', 9)
on conflict (product_price_id) do nothing;

-- English Description (ID: 17)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (17, NOW(), NOW(), '', 'DEFAULT', '', 'en', 9)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 18)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (18, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 9)
on conflict (description_id) do nothing;


-- Product 10: H&M Men's Belt
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (10, true, null, null, 0.2, 3.5, false, 45, 3, 1, true, '*',
        '65f023632bc46470c104b76f', 10)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (10, 'base', true, 120.00, 'ONE_TIME', 10)
on conflict (product_price_id) do nothing;

-- English Description (ID: 19)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (19, NOW(), NOW(), '', 'DEFAULT', '', 'en', 10)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 20)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (20, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 10)
on conflict (description_id) do nothing;


-- Product 11: Gucci Women's Loafers
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (11, true, null, null, 0.7, null, false, 7, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 11)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (11, 'base', true, 3900.00, 'ONE_TIME', 11) -- Higher price for Gucci
on conflict (product_price_id) do nothing;

-- English Description (ID: 21)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (21, NOW(), NOW(), '', 'DEFAULT', '', 'en', 11)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 22)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (22, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 11)
on conflict (description_id) do nothing;


-- Product 12: Chanel Card Holder
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (12, true, 7.5, 11.2, 0.1, 0.5, false, 10, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 12)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (12, 'base', true, 2400.00, 'ONE_TIME', 12) -- Higher price for Chanel
on conflict (product_price_id) do nothing;

-- English Description (ID: 23)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (23, NOW(), NOW(), '', 'DEFAULT', '', 'en', 12)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 24)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (24, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 12)
on conflict (description_id) do nothing;


-- Product 13: Nike Women's Leggings
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (13, true, null, null, 0.3, null, false, 28, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 13)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (13, 'base', true, 380.00, 'ONE_TIME', 13)
on conflict (product_price_id) do nothing;

-- English Description (ID: 25)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (25, NOW(), NOW(), '', 'DEFAULT', '', 'en', 13)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 26)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (26, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 13)
on conflict (description_id) do nothing;


-- Product 14: Zara Men's Polo Shirt
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (14, true, null, null, 0.3, null, false, 33, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 14)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (14, 'base', true, 190.00, 'ONE_TIME', 14)
on conflict (product_price_id) do nothing;

-- English Description (ID: 27)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (27, NOW(), NOW(), '', 'DEFAULT', '', 'en', 14)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 28)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (28, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 14)
on conflict (description_id) do nothing;


-- Product 15: Adidas Kids' Sandals
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (15, true, null, null, 0.3, null, false, 26, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 15)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (15, 'base', true, 150.00, 'ONE_TIME', 15)
on conflict (product_price_id) do nothing;

-- English Description (ID: 29)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (29, NOW(), NOW(), '', 'DEFAULT', '', 'en', 15)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 30)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (30, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 15)
on conflict (description_id) do nothing;
/*
Generated SQL inserts for product availability and pricing based on products 16-45.
- Continues product_avail_id from 16.
- Continues product_price_id from 16.
- Continues product_price_description description_id from 31.
- Sets reasonable quantities and prices based on product type/brand.
- Uses store_id = '65f023632bc46470c104b76f'.
- Includes descriptions for languages 'en' and 'ar'.
*/

-- Product 16: H&M Women's Wrap Dress
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (16, true, null, null, 0.4, null, false, 15, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 16)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (16, 'base', true, 320.00, 'ONE_TIME', 16)
on conflict (product_price_id) do nothing;

-- English Description (ID: 31)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (31, NOW(), NOW(), '', 'DEFAULT', '', 'en', 16)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 32)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (32, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 16)
on conflict (description_id) do nothing;


-- Product 17: Gucci Men's Wallet
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (17, true, 9.0, 11.0, 0.1, 1.5, false, 5, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 17)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (17, 'base', true, 1800.00, 'ONE_TIME', 17) -- Higher price for Gucci
on conflict (product_price_id) do nothing;

-- English Description (ID: 33)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (33, NOW(), NOW(), '', 'DEFAULT', '', 'en', 17)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 34)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (34, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 17)
on conflict (description_id) do nothing;


-- Product 18: Chanel Brooch
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (18, true, null, null, 0.05, null, false, 3, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 18)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (18, 'base', true, 2500.00, 'ONE_TIME', 18) -- Higher price for Chanel
on conflict (product_price_id) do nothing;

-- English Description (ID: 35)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (35, NOW(), NOW(), '', 'DEFAULT', '', 'en', 18)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 36)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (36, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 18)
on conflict (description_id) do nothing;


-- Product 19: Nike Men's Basketball Shorts
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (19, true, null, null, 0.3, null, false, 25, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 19)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (19, 'base', true, 280.00, 'ONE_TIME', 19)
on conflict (product_price_id) do nothing;

-- English Description (ID: 37)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (37, NOW(), NOW(), '', 'DEFAULT', '', 'en', 19)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 38)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (38, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 19)
on conflict (description_id) do nothing;


-- Product 20: Zara Women's Sandals
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (20, true, null, null, 0.5, null, false, 18, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 20)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (20, 'base', true, 290.00, 'ONE_TIME', 20)
on conflict (product_price_id) do nothing;

-- English Description (ID: 39)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (39, NOW(), NOW(), '', 'DEFAULT', '', 'en', 20)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 40)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (40, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 20)
on conflict (description_id) do nothing;


-- Product 21: Adidas Men's Hoodie
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (21, true, null, null, 0.7, null, false, 12, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 21)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (21, 'base', true, 450.00, 'ONE_TIME', 21)
on conflict (product_price_id) do nothing;

-- English Description (ID: 41)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (41, NOW(), NOW(), '', 'DEFAULT', '', 'en', 21)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 42)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (42, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 21)
on conflict (description_id) do nothing;


-- Product 22: H&M Kids' T-Shirt Pack
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (22, true, null, null, 0.4, null, false, 30, 3, 1, true, '*',
        '65f023632bc46470c104b76f', 22)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (22, 'base', true, 250.00, 'ONE_TIME', 22) -- Price for the pack
on conflict (product_price_id) do nothing;

-- English Description (ID: 43)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (43, NOW(), NOW(), '', 'DEFAULT', '', 'en', 22)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 44)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (44, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 22)
on conflict (description_id) do nothing;


-- Product 23: Gucci Men's Sneakers
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (23, true, null, null, 1.0, null, false, 8, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 23)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (23, 'base', true, 3200.00, 'ONE_TIME', 23) -- Higher price for Gucci
on conflict (product_price_id) do nothing;

-- English Description (ID: 45)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (45, NOW(), NOW(), '', 'DEFAULT', '', 'en', 23)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 46)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (46, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 23)
on conflict (description_id) do nothing;


-- Product 24: Chanel Ballet Flats
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (24, true, null, null, 0.5, null, false, 6, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 24)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (24, 'base', true, 3500.00, 'ONE_TIME', 24) -- Higher price for Chanel
on conflict (product_price_id) do nothing;

-- English Description (ID: 47)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (47, NOW(), NOW(), '', 'DEFAULT', '', 'en', 24)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 48)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (48, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 24)
on conflict (description_id) do nothing;


-- Product 25: Nike Women's Tank Top
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (25, true, null, null, 0.2, null, false, 20, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 25)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (25, 'base', true, 180.00, 'ONE_TIME', 25)
on conflict (product_price_id) do nothing;

-- English Description (ID: 49)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (49, NOW(), NOW(), '', 'DEFAULT', '', 'en', 25)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 50)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (50, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 25)
on conflict (description_id) do nothing;


-- Product 26: Zara Men's Chino Trousers
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (26, true, null, null, 0.5, null, false, 14, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 26)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (26, 'base', true, 350.00, 'ONE_TIME', 26)
on conflict (product_price_id) do nothing;

-- English Description (ID: 51)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (51, NOW(), NOW(), '', 'DEFAULT', '', 'en', 26)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 52)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (52, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 26)
on conflict (description_id) do nothing;


-- Product 27: Adidas Kids' Tracksuit
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (27, true, null, null, 0.6, null, false, 10, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 27)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (27, 'base', true, 480.00, 'ONE_TIME', 27)
on conflict (product_price_id) do nothing;

-- English Description (ID: 53)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (53, NOW(), NOW(), '', 'DEFAULT', '', 'en', 27)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 54)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (54, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 27)
on conflict (description_id) do nothing;


-- Product 28: H&M Scarf
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (28, true, null, 180.0, 0.2, 70.0, false, 22, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 28)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (28, 'base', true, 150.00, 'ONE_TIME', 28)
on conflict (product_price_id) do nothing;

-- English Description (ID: 55)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (55, NOW(), NOW(), '', 'DEFAULT', '', 'en', 28)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 56)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (56, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 28)
on conflict (description_id) do nothing;


-- Product 29: Gucci Belt Bag
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (29, true, 14.0, 24.0, 0.5, 5.5, false, 4, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 29)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (29, 'base', true, 4500.00, 'ONE_TIME', 29) -- Higher price for Gucci
on conflict (product_price_id) do nothing;

-- English Description (ID: 57)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (57, NOW(), NOW(), '', 'DEFAULT', '', 'en', 29)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 58)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (58, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 29)
on conflict (description_id) do nothing;


-- Product 30: Chanel Sneakers
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (30, true, null, null, 0.9, null, false, 5, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 30)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (30, 'base', true, 5500.00, 'ONE_TIME', 30) -- Higher price for Chanel
on conflict (product_price_id) do nothing;

-- English Description (ID: 59)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (59, NOW(), NOW(), '', 'DEFAULT', '', 'en', 30)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 60)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (60, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 30)
on conflict (description_id) do nothing;


-- Product 31: Nike Men's Windrunner Jacket
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (31, true, null, null, 0.5, null, false, 9, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 31)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (31, 'base', true, 550.00, 'ONE_TIME', 31)
on conflict (product_price_id) do nothing;

-- English Description (ID: 61)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (61, NOW(), NOW(), '', 'DEFAULT', '', 'en', 31)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 62)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (62, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 31)
on conflict (description_id) do nothing;


-- Product 32: Zara Women's Jeans
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (32, true, null, null, 0.6, null, false, 17, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 32)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (32, 'base', true, 380.00, 'ONE_TIME', 32)
on conflict (product_price_id) do nothing;

-- English Description (ID: 63)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (63, NOW(), NOW(), '', 'DEFAULT', '', 'en', 32)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 64)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (64, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 32)
on conflict (description_id) do nothing;


-- Product 33: Adidas Cap
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (33, true, null, null, 0.1, null, false, 25, 3, 1, true, '*',
        '65f023632bc46470c104b76f', 33)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (33, 'base', true, 120.00, 'ONE_TIME', 33)
on conflict (product_price_id) do nothing;

-- English Description (ID: 65)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (65, NOW(), NOW(), '', 'DEFAULT', '', 'en', 33)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 66)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (66, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 33)
on conflict (description_id) do nothing;


-- Product 34: H&M Kids' Rain Jacket
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (34, true, null, null, 0.4, null, false, 11, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 34)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (34, 'base', true, 300.00, 'ONE_TIME', 34)
on conflict (product_price_id) do nothing;

-- English Description (ID: 67)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (67, NOW(), NOW(), '', 'DEFAULT', '', 'en', 34)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 68)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (68, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 34)
on conflict (description_id) do nothing;


-- Product 35: Gucci Scarf
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (35, true, null, 140.0, 0.2, 140.0, false, 7, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 35)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (35, 'base', true, 2200.00, 'ONE_TIME', 35) -- Higher price for Gucci
on conflict (product_price_id) do nothing;

-- English Description (ID: 69)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (69, NOW(), NOW(), '', 'DEFAULT', '', 'en', 35)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 70)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (70, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 35)
on conflict (description_id) do nothing;


-- Product 36: Chanel Wallet on Chain (WOC)
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (36, true, 12.3, 19.2, 0.4, 3.5, false, 3, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 36)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (36, 'base', true, 11000.00, 'ONE_TIME', 36) -- Higher price for Chanel WOC
on conflict (product_price_id) do nothing;

-- English Description (ID: 71)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (71, NOW(), NOW(), '', 'DEFAULT', '', 'en', 36)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 72)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (72, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 36)
on conflict (description_id) do nothing;


-- Product 37: Nike Women's Running Shorts
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (37, true, null, null, 0.2, null, false, 19, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 37)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (37, 'base', true, 200.00, 'ONE_TIME', 37)
on conflict (product_price_id) do nothing;

-- English Description (ID: 73)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (73, NOW(), NOW(), '', 'DEFAULT', '', 'en', 37)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 74)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (74, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 37)
on conflict (description_id) do nothing;


-- Product 38: Zara Kids' Sweater
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (38, true, null, null, 0.3, null, false, 16, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 38)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (38, 'base', true, 220.00, 'ONE_TIME', 38)
on conflict (product_price_id) do nothing;

-- English Description (ID: 75)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (75, NOW(), NOW(), '', 'DEFAULT', '', 'en', 38)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 76)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (76, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 38)
on conflict (description_id) do nothing;


-- Product 39: Adidas Socks (3-Pack)
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (39, true, null, null, 0.15, null, false, 40, 5, 1, true, '*',
        '65f023632bc46470c104b76f', 39)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (39, 'base', true, 80.00, 'ONE_TIME', 39) -- Price for the pack
on conflict (product_price_id) do nothing;

-- English Description (ID: 77)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (77, NOW(), NOW(), '', 'DEFAULT', '', 'en', 39)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 78)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (78, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 39)
on conflict (description_id) do nothing;


-- Product 40: H&M Men's Swim Shorts
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (40, true, null, null, 0.2, null, false, 20, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 40)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (40, 'base', true, 190.00, 'ONE_TIME', 40)
on conflict (product_price_id) do nothing;

-- English Description (ID: 79)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (79, NOW(), NOW(), '', 'DEFAULT', '', 'en', 40)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 80)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (80, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 40)
on conflict (description_id) do nothing;


-- Product 41: Gucci Horsebit Loafers
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (41, true, null, null, 0.8, null, false, 6, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 41)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (41, 'base', true, 3800.00, 'ONE_TIME', 41) -- Higher price for Gucci
on conflict (product_price_id) do nothing;

-- English Description (ID: 81)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (81, NOW(), NOW(), '', 'DEFAULT', '', 'en', 41)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 82)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (82, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 41)
on conflict (description_id) do nothing;


-- Product 42: Chanel Earrings
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (42, true, null, null, 0.05, null, false, 4, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 42)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (42, 'base', true, 2800.00, 'ONE_TIME', 42) -- Higher price for Chanel
on conflict (product_price_id) do nothing;

-- English Description (ID: 83)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (83, NOW(), NOW(), '', 'DEFAULT', '', 'en', 42)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 84)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (84, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 42)
on conflict (description_id) do nothing;


-- Product 43: Nike Duffel Bag
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (43, true, 28.0, 55.0, 0.6, 25.0, false, 10, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 43)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (43, 'base', true, 350.00, 'ONE_TIME', 43)
on conflict (product_price_id) do nothing;

-- English Description (ID: 85)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (85, NOW(), NOW(), '', 'DEFAULT', '', 'en', 43)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 86)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (86, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 43)
on conflict (description_id) do nothing;


-- Product 44: Zara Women's Blouse
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (44, true, null, null, 0.3, null, false, 13, 1, 1, true, '*',
        '65f023632bc46470c104b76f', 44)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (44, 'base', true, 260.00, 'ONE_TIME', 44)
on conflict (product_price_id) do nothing;

-- English Description (ID: 87)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (87, NOW(), NOW(), '', 'DEFAULT', '', 'en', 44)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 88)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (88, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 44)
on conflict (description_id) do nothing;


-- Product 45: Adidas Slides
INSERT INTO catalog.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (45, true, null, null, 0.4, null, false, 28, 2, 1, true, '*',
        '65f023632bc46470c104b76f', 45)
on conflict (product_avail_id) do nothing;

INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id)
VALUES (45, 'base', true, 150.00, 'ONE_TIME', 45)
on conflict (product_price_id) do nothing;

-- English Description (ID: 89)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (89, NOW(), NOW(), '', 'DEFAULT', '', 'en', 45)
on conflict (description_id) do nothing;
-- Arabic Description (ID: 90)
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (90, NOW(), NOW(), '', 'DEFAULT', '', 'ar', 45)
on conflict (description_id) do nothing;
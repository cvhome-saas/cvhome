/*
Generated SQL inserts for catalog.product_availability, catalog.product_price,
and catalog.product_price_description based on product data (IDs 136-180).
Store ID: '65f023632bc46470c104b75f'
Languages: ['en','fr']
Starting product_availability_id: 136
Starting product_price_id: 136
Starting product_price_description_id: 271
*/

-- Product 136: Apple iPhone 15 Pro
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (136, true, null, null, null, null, false, 100, 10, 1, true, '*', '65f023632bc46470c104b75f', 136)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (136, 'base', true, 999.00, 'ONE_TIME', 136)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (271, now(), now(), '', 'DEFAULT', '', 'en', 136)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (272, now(), now(), '', 'DEFAULT', '', 'fr', 136)
on conflict (description_id) do nothing;

-- Product 137: Samsung Galaxy S24 Ultra
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (137, true, null, null, null, null, false, 100, 10, 1, true, '*', '65f023632bc46470c104b75f', 137)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (137, 'base', true, 1299.00, 'ONE_TIME', 137)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (273, now(), now(), '', 'DEFAULT', '', 'en', 137)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (274, now(), now(), '', 'DEFAULT', '', 'fr', 137)
on conflict (description_id) do nothing;

-- Product 138: Dell XPS 15 Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (138, true, null, null, null, null, false, 100, 5, 1, true, '*', '65f023632bc46470c104b75f', 138)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (138, 'base', true, 1599.00, 'ONE_TIME', 138)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (275, now(), now(), '', 'DEFAULT', '', 'en', 138)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (276, now(), now(), '', 'DEFAULT', '', 'fr', 138)
on conflict (description_id) do nothing;

-- Product 139: Sony WH-1000XM5 Headphones
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (139, true, null, null, null, null, false, 100, 10, 1, true, '*', '65f023632bc46470c104b75f', 139)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (139, 'base', true, 399.99, 'ONE_TIME', 139)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (277, now(), now(), '', 'DEFAULT', '', 'en', 139)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (278, now(), now(), '', 'DEFAULT', '', 'fr', 139)
on conflict (description_id) do nothing;

-- Product 140: LG C3 65-inch OLED TV
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (140, true, null, null, null, null, false, 50, 2, 1, true, '*', '65f023632bc46470c104b75f', 140)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (140, 'base', true, 1499.99, 'ONE_TIME', 140)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (279, now(), now(), '', 'DEFAULT', '', 'en', 140)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (280, now(), now(), '', 'DEFAULT', '', 'fr', 140)
on conflict (description_id) do nothing;

-- Product 141: Apple MacBook Air M3
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (141, true, null, null, null, null, false, 100, 5, 1, true, '*', '65f023632bc46470c104b75f', 141)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (141, 'base', true, 1099.00, 'ONE_TIME', 141)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (281, now(), now(), '', 'DEFAULT', '', 'en', 141)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (282, now(), now(), '', 'DEFAULT', '', 'fr', 141)
on conflict (description_id) do nothing;

-- Product 142: Samsung QN90C 55-inch QLED TV
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (142, true, null, null, null, null, false, 60, 2, 1, true, '*', '65f023632bc46470c104b75f', 142)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (142, 'base', true, 1199.99, 'ONE_TIME', 142)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (283, now(), now(), '', 'DEFAULT', '', 'en', 142)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (284, now(), now(), '', 'DEFAULT', '', 'fr', 142)
on conflict (description_id) do nothing;

-- Product 143: HP Spectre x360 14
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (143, true, null, null, null, null, false, 80, 5, 1, true, '*', '65f023632bc46470c104b75f', 143)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (143, 'base', true, 1399.99, 'ONE_TIME', 143)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (285, now(), now(), '', 'DEFAULT', '', 'en', 143)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (286, now(), now(), '', 'DEFAULT', '', 'fr', 143)
on conflict (description_id) do nothing;

-- Product 144: Apple AirPods Pro (2nd Gen)
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (144, true, null, null, null, null, false, 150, 10, 1, true, '*', '65f023632bc46470c104b75f', 144)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (144, 'base', true, 249.00, 'ONE_TIME', 144)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (287, now(), now(), '', 'DEFAULT', '', 'en', 144)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (288, now(), now(), '', 'DEFAULT', '', 'fr', 144)
on conflict (description_id) do nothing;

-- Product 145: Samsung Galaxy Buds2 Pro
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (145, true, null, null, null, null, false, 120, 10, 1, true, '*', '65f023632bc46470c104b75f', 145)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (145, 'base', true, 229.99, 'ONE_TIME', 145)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (289, now(), now(), '', 'DEFAULT', '', 'en', 145)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (290, now(), now(), '', 'DEFAULT', '', 'fr', 145)
on conflict (description_id) do nothing;

-- Product 146: Sony Bravia X90L 65-inch TV
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (146, true, null, null, null, null, false, 70, 2, 1, true, '*', '65f023632bc46470c104b75f', 146)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (146, 'base', true, 1299.99, 'ONE_TIME', 146)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (291, now(), now(), '', 'DEFAULT', '', 'en', 146)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (292, now(), now(), '', 'DEFAULT', '', 'fr', 146)
on conflict (description_id) do nothing;

-- Product 147: Samsung Galaxy A54
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (147, true, null, null, null, null, false, 150, 10, 1, true, '*', '65f023632bc46470c104b75f', 147)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (147, 'base', true, 449.99, 'ONE_TIME', 147)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (293, now(), now(), '', 'DEFAULT', '', 'en', 147)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (294, now(), now(), '', 'DEFAULT', '', 'fr', 147)
on conflict (description_id) do nothing;

-- Product 148: Sony LinkBuds S
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (148, true, null, null, null, null, false, 130, 10, 1, true, '*', '65f023632bc46470c104b75f', 148)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (148, 'base', true, 199.99, 'ONE_TIME', 148)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (295, now(), now(), '', 'DEFAULT', '', 'en', 148)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (296, now(), now(), '', 'DEFAULT', '', 'fr', 148)
on conflict (description_id) do nothing;

-- Product 149: Dell Inspiron 16 Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (149, true, null, null, null, null, false, 90, 5, 1, true, '*', '65f023632bc46470c104b75f', 149)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (149, 'base', true, 799.99, 'ONE_TIME', 149)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (297, now(), now(), '', 'DEFAULT', '', 'en', 149)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (298, now(), now(), '', 'DEFAULT', '', 'fr', 149)
on conflict (description_id) do nothing;

-- Product 150: LG Gram 17 Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (150, true, null, null, null, null, false, 75, 5, 1, true, '*', '65f023632bc46470c104b75f', 150)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (150, 'base', true, 1699.99, 'ONE_TIME', 150)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (299, now(), now(), '', 'DEFAULT', '', 'en', 150)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (300, now(), now(), '', 'DEFAULT', '', 'fr', 150)
on conflict (description_id) do nothing;

-- Product 151: HP Envy x360 16 Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (151, true, null, null, null, null, false, 85, 5, 1, true, '*', '65f023632bc46470c104b75f', 151)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (151, 'base', true, 999.99, 'ONE_TIME', 151)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (301, now(), now(), '', 'DEFAULT', '', 'en', 151)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (302, now(), now(), '', 'DEFAULT', '', 'fr', 151)
on conflict (description_id) do nothing;

-- Product 152: Apple TV 4K (2022)
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (152, true, null, null, null, null, false, 200, 10, 1, true, '*', '65f023632bc46470c104b75f', 152)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (152, 'base', true, 179.00, 'ONE_TIME', 152)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (303, now(), now(), '', 'DEFAULT', '', 'en', 152)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (304, now(), now(), '', 'DEFAULT', '', 'fr', 152)
on conflict (description_id) do nothing;

-- Product 153: Samsung The Frame TV (55-inch)
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (153, true, null, null, null, null, false, 40, 2, 1, true, '*', '65f023632bc46470c104b75f', 153)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (153, 'base', true, 1499.99, 'ONE_TIME', 153)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (305, now(), now(), '', 'DEFAULT', '', 'en', 153)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (306, now(), now(), '', 'DEFAULT', '', 'fr', 153)
on conflict (description_id) do nothing;

-- Product 154: Sony INZONE H9 Wireless Headset
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (154, true, null, null, null, null, false, 90, 10, 1, true, '*', '65f023632bc46470c104b75f', 154)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (154, 'base', true, 299.99, 'ONE_TIME', 154)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (307, now(), now(), '', 'DEFAULT', '', 'en', 154)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (308, now(), now(), '', 'DEFAULT', '', 'fr', 154)
on conflict (description_id) do nothing;

-- Product 155: LG UltraGear 27" OLED Monitor
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (155, true, null, null, null, null, false, 65, 3, 1, true, '*', '65f023632bc46470c104b75f', 155)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (155, 'base', true, 999.99, 'ONE_TIME', 155)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (309, now(), now(), '', 'DEFAULT', '', 'en', 155)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (310, now(), now(), '', 'DEFAULT', '', 'fr', 155)
on conflict (description_id) do nothing;

-- Product 156: Dell Alienware m16 Gaming Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (156, true, null, null, null, null, false, 55, 2, 1, true, '*', '65f023632bc46470c104b75f', 156)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (156, 'base', true, 1899.99, 'ONE_TIME', 156)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (311, now(), now(), '', 'DEFAULT', '', 'en', 156)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (312, now(), now(), '', 'DEFAULT', '', 'fr', 156)
on conflict (description_id) do nothing;

-- Product 157: HP OMEN 16 Gaming Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (157, true, null, null, null, null, false, 60, 3, 1, true, '*', '65f023632bc46470c104b75f', 157)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (157, 'base', true, 1499.99, 'ONE_TIME', 157)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (313, now(), now(), '', 'DEFAULT', '', 'en', 157)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (314, now(), now(), '', 'DEFAULT', '', 'fr', 157)
on conflict (description_id) do nothing;

-- Product 158: Apple Studio Display
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (158, true, null, null, null, null, false, 45, 2, 1, true, '*', '65f023632bc46470c104b75f', 158)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (158, 'base', true, 1599.00, 'ONE_TIME', 158)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (315, now(), now(), '', 'DEFAULT', '', 'en', 158)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (316, now(), now(), '', 'DEFAULT', '', 'fr', 158)
on conflict (description_id) do nothing;

-- Product 159: Samsung Galaxy Book4 Pro
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (159, true, null, null, null, null, false, 70, 5, 1, true, '*', '65f023632bc46470c104b75f', 159)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (159, 'base', true, 1449.99, 'ONE_TIME', 159)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (317, now(), now(), '', 'DEFAULT', '', 'en', 159)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (318, now(), now(), '', 'DEFAULT', '', 'fr', 159)
on conflict (description_id) do nothing;

-- Product 160: Sony HT-A5000 Soundbar
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (160, true, null, null, null, null, false, 80, 3, 1, true, '*', '65f023632bc46470c104b75f', 160)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (160, 'base', true, 999.99, 'ONE_TIME', 160)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (319, now(), now(), '', 'DEFAULT', '', 'en', 160)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (320, now(), now(), '', 'DEFAULT', '', 'fr', 160)
on conflict (description_id) do nothing;

-- Product 161: LG Tone Free FP9 Earbuds
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (161, true, null, null, null, null, false, 110, 10, 1, true, '*', '65f023632bc46470c104b75f', 161)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (161, 'base', true, 149.99, 'ONE_TIME', 161)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (321, now(), now(), '', 'DEFAULT', '', 'en', 161)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (322, now(), now(), '', 'DEFAULT', '', 'fr', 161)
on conflict (description_id) do nothing;

-- Product 162: Dell G15 Gaming Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (162, true, null, null, null, null, false, 75, 3, 1, true, '*', '65f023632bc46470c104b75f', 162)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (162, 'base', true, 899.99, 'ONE_TIME', 162)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (323, now(), now(), '', 'DEFAULT', '', 'en', 162)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (324, now(), now(), '', 'DEFAULT', '', 'fr', 162)
on conflict (description_id) do nothing;

-- Product 163: HP Pavilion Plus 14 Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (163, true, null, null, null, null, false, 95, 5, 1, true, '*', '65f023632bc46470c104b75f', 163)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (163, 'base', true, 849.99, 'ONE_TIME', 163)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (325, now(), now(), '', 'DEFAULT', '', 'en', 163)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (326, now(), now(), '', 'DEFAULT', '', 'fr', 163)
on conflict (description_id) do nothing;

-- Product 164: Apple Beats Studio Pro Headphones
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (164, true, null, null, null, null, false, 100, 10, 1, true, '*', '65f023632bc46470c104b75f', 164)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (164, 'base', true, 349.99, 'ONE_TIME', 164)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (327, now(), now(), '', 'DEFAULT', '', 'en', 164)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (328, now(), now(), '', 'DEFAULT', '', 'fr', 164)
on conflict (description_id) do nothing;

-- Product 165: Samsung Galaxy S23 FE
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (165, true, null, null, null, null, false, 140, 10, 1, true, '*', '65f023632bc46470c104b75f', 165)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (165, 'base', true, 599.99, 'ONE_TIME', 165)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (329, now(), now(), '', 'DEFAULT', '', 'en', 165)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (330, now(), now(), '', 'DEFAULT', '', 'fr', 165)
on conflict (description_id) do nothing;

-- Product 166: LG OLED evo C3 55-inch TV
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (166, true, null, null, null, null, false, 50, 2, 1, true, '*', '65f023632bc46470c104b75f', 166)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (166, 'base', true, 1499.99, 'ONE_TIME', 166) -- Same price as 65-inch C3 (product 140)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (331, now(), now(), '', 'DEFAULT', '', 'en', 166)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (332, now(), now(), '', 'DEFAULT', '', 'fr', 166)
on conflict (description_id) do nothing;

-- Product 167: Dell UltraSharp 27 Monitor - U2723QE
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (167, true, null, null, null, null, false, 80, 4, 1, true, '*', '65f023632bc46470c104b75f', 167)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (167, 'base', true, 649.99, 'ONE_TIME', 167)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (333, now(), now(), '', 'DEFAULT', '', 'en', 167)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (334, now(), now(), '', 'DEFAULT', '', 'fr', 167)
on conflict (description_id) do nothing;

-- Product 168: Sony WF-1000XM5 Earbuds
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (168, true, null, null, null, null, false, 160, 10, 1, true, '*', '65f023632bc46470c104b75f', 168)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (168, 'base', true, 299.99, 'ONE_TIME', 168)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (335, now(), now(), '', 'DEFAULT', '', 'en', 168)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (336, now(), now(), '', 'DEFAULT', '', 'fr', 168)
on conflict (description_id) do nothing;

-- Product 169: Apple Watch Series 9
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (169, true, null, null, null, null, false, 180, 10, 1, true, '*', '65f023632bc46470c104b75f', 169)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (169, 'base', true, 399.00, 'ONE_TIME', 169)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (337, now(), now(), '', 'DEFAULT', '', 'en', 169)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (338, now(), now(), '', 'DEFAULT', '', 'fr', 169)
on conflict (description_id) do nothing;

-- Product 170: Samsung Galaxy Tab S9
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (170, true, null, null, null, null, false, 110, 5, 1, true, '*', '65f023632bc46470c104b75f', 170)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (170, 'base', true, 799.99, 'ONE_TIME', 170)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (339, now(), now(), '', 'DEFAULT', '', 'en', 170)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (340, now(), now(), '', 'DEFAULT', '', 'fr', 170)
on conflict (description_id) do nothing;

-- Product 171: HP Chromebook x360 14c
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (171, true, null, null, null, null, false, 100, 5, 1, true, '*', '65f023632bc46470c104b75f', 171)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (171, 'base', true, 499.99, 'ONE_TIME', 171)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (341, now(), now(), '', 'DEFAULT', '', 'en', 171)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (342, now(), now(), '', 'DEFAULT', '', 'fr', 171)
on conflict (description_id) do nothing;

-- Product 172: Samsung Galaxy Z Fold5
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (172, true, null, null, null, null, false, 80, 5, 1, true, '*', '65f023632bc46470c104b75f', 172)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (172, 'base', true, 1799.99, 'ONE_TIME', 172)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (343, now(), now(), '', 'DEFAULT', '', 'en', 172)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (344, now(), now(), '', 'DEFAULT', '', 'fr', 172)
on conflict (description_id) do nothing;

-- Product 173: LG UltraFine 32" 4K Monitor
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (173, true, null, null, null, null, false, 70, 3, 1, true, '*', '65f023632bc46470c104b75f', 173)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (173, 'base', true, 1299.99, 'ONE_TIME', 173)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (345, now(), now(), '', 'DEFAULT', '', 'en', 173)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (346, now(), now(), '', 'DEFAULT', '', 'fr', 173)
on conflict (description_id) do nothing;

-- Product 174: Apple iPad Air (M2)
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (174, true, null, null, null, null, false, 120, 5, 1, true, '*', '65f023632bc46470c104b75f', 174)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (174, 'base', true, 599.00, 'ONE_TIME', 174)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (347, now(), now(), '', 'DEFAULT', '', 'en', 174)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (348, now(), now(), '', 'DEFAULT', '', 'fr', 174)
on conflict (description_id) do nothing;

-- Product 175: Sony Bravia 7 QLED TV (65-inch)
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (175, true, null, null, null, null, false, 40, 2, 1, true, '*', '65f023632bc46470c104b75f', 175)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (175, 'base', true, 1899.99, 'ONE_TIME', 175)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (349, now(), now(), '', 'DEFAULT', '', 'en', 175)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (350, now(), now(), '', 'DEFAULT', '', 'fr', 175)
on conflict (description_id) do nothing;

-- Product 176: Dell Latitude 7440 Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (176, true, null, null, null, null, false, 85, 5, 1, true, '*', '65f023632bc46470c104b75f', 176)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (176, 'base', true, 1499.99, 'ONE_TIME', 176)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (351, now(), now(), '', 'DEFAULT', '', 'en', 176)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (352, now(), now(), '', 'DEFAULT', '', 'fr', 176)
on conflict (description_id) do nothing;

-- Product 177: Samsung Galaxy Watch6
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (177, true, null, null, null, null, false, 190, 10, 1, true, '*', '65f023632bc46470c104b75f', 177)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (177, 'base', true, 299.99, 'ONE_TIME', 177)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (353, now(), now(), '', 'DEFAULT', '', 'en', 177)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (354, now(), now(), '', 'DEFAULT', '', 'fr', 177)
on conflict (description_id) do nothing;

-- Product 178: Beats Fit Pro Earbuds
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (178, true, null, null, null, null, false, 140, 10, 1, true, '*', '65f023632bc46470c104b75f', 178)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (178, 'base', true, 199.99, 'ONE_TIME', 178)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (355, now(), now(), '', 'DEFAULT', '', 'en', 178)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (356, now(), now(), '', 'DEFAULT', '', 'fr', 178)
on conflict (description_id) do nothing;

-- Product 179: HP EliteBook 840 G10 Laptop
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (179, true, null, null, null, null, false, 70, 5, 1, true, '*', '65f023632bc46470c104b75f', 179)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (179, 'base', true, 1599.99, 'ONE_TIME', 179)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (357, now(), now(), '', 'DEFAULT', '', 'en', 179)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (358, now(), now(), '', 'DEFAULT', '', 'fr', 179)
on conflict (description_id) do nothing;

-- Product 180: LG S90QY Soundbar
INSERT INTO catalog.product_availability (product_avail_id, available, height, length, weight, width, free_shipping,
                                          quantity, quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id)
VALUES (180, true, null, null, null, null, false, 90, 3, 1, true, '*', '65f023632bc46470c104b75f', 180)
on conflict (product_avail_id) do nothing;
INSERT INTO catalog.product_price (product_price_id, product_price_code, default_price, product_price_amount,
                                   product_price_type, product_avail_id)
VALUES (180, 'base', true, 799.99, 'ONE_TIME', 180)
on conflict (product_price_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (359, now(), now(), '', 'DEFAULT', '', 'en', 180)
on conflict (description_id) do nothing;
INSERT INTO catalog.product_price_description (description_id, date_created, date_modified, description, name, title,
                                               language_code, product_price_id)
VALUES (360, now(), now(), '', 'DEFAULT', '', 'fr', 180)
on conflict (description_id) do nothing;
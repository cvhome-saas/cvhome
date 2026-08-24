/*
generate more product_availability  based on product file  and add product_price for every product_availability using product_avail_id column as foreign key
start product_availability=46 product_price=46 product_price_description=91
where languages=['fr','en']
Timestamps replaced with NOW()
Applies to products 46-90
*/

-- Product 46
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (46, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (46, 'base', true, 20.00, 'ONE_TIME', 46, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (91, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 46)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (92, NOW(), NOW(), '', 'DEFAULT', '', 'en', 46)
on conflict (description_id) do nothing;

-- Product 47
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (47, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 47, 'SKU-GUER-SKIN-ARWOIL47')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (47, 'base', true, 21.50, 'ONE_TIME', 47, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (93, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 47)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (94, NOW(), NOW(), '', 'DEFAULT', '', 'en', 47)
on conflict (description_id) do nothing;

-- Product 48
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (48, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 48, 'SKU-SHIS-SKIN-ULTIMUNE48')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (48, 'base', true, 23.00, 'ONE_TIME', 48, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (95, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 48)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (96, NOW(), NOW(), '', 'DEFAULT', '', 'en', 48)
on conflict (description_id) do nothing;

-- Product 49
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (49, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 49, 'SKU-NARS-MAKE-RCCONC49')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (49, 'base', true, 24.50, 'ONE_TIME', 49, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (97, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 49)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (98, NOW(), NOW(), '', 'DEFAULT', '', 'en', 49)
on conflict (description_id) do nothing;

-- Product 50
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (50, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 50, 'SKU-LRP-SKIN-ANTHUV50')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (50, 'base', true, 26.00, 'ONE_TIME', 50, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (99, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 50)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (100, NOW(), NOW(), '', 'DEFAULT', '', 'en', 50)
on conflict (description_id) do nothing;

-- Product 51
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (51, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 51, 'SKU-KERA-HAIR-GENSHMP51')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (51, 'base', true, 27.50, 'ONE_TIME', 51, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (101, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 51)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (102, NOW(), NOW(), '', 'DEFAULT', '', 'en', 51)
on conflict (description_id) do nothing;

-- Product 52
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (52, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 52, 'SKU-YSL-FRAG-BLACKOPIUM52')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (52, 'base', true, 29.00, 'ONE_TIME', 52, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (103, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 52)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (104, NOW(), NOW(), '', 'DEFAULT', '', 'en', 52)
on conflict (description_id) do nothing;

-- Product 53
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (53, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 53, 'SKU-GUER-MAKE-TERRACOTTA53')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (53, 'base', true, 30.50, 'ONE_TIME', 53, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (105, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 53)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (106, NOW(), NOW(), '', 'DEFAULT', '', 'en', 53)
on conflict (description_id) do nothing;

-- Product 54
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (54, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 54, 'SKU-SHIS-SKIN-BENEFCR54')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (54, 'base', true, 32.00, 'ONE_TIME', 54, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (107, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 54)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (108, NOW(), NOW(), '', 'DEFAULT', '', 'en', 54)
on conflict (description_id) do nothing;

-- Product 55
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (55, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 55, 'SKU-NARS-MAKE-ORGBLUSH55')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (55, 'base', true, 33.50, 'ONE_TIME', 55, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (109, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 55)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (110, NOW(), NOW(), '', 'DEFAULT', '', 'en', 55)
on conflict (description_id) do nothing;

-- Product 56
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (56, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 56, 'SKU-LRP-SKIN-CICAB5-56')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (56, 'base', true, 35.00, 'ONE_TIME', 56, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (111, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 56)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (112, NOW(), NOW(), '', 'DEFAULT', '', 'en', 56)
on conflict (description_id) do nothing;

-- Product 57
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (57, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 57, 'SKU-KERA-HAIR-ELIXIR57')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (57, 'base', true, 36.50, 'ONE_TIME', 57, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (113, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 57)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (114, NOW(), NOW(), '', 'DEFAULT', '', 'en', 57)
on conflict (description_id) do nothing;

-- Product 58
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (58, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 58, 'SKU-YSL-FRAG-LIBREEDP58')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (58, 'base', true, 38.00, 'ONE_TIME', 58, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (115, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 58)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (116, NOW(), NOW(), '', 'DEFAULT', '', 'en', 58)
on conflict (description_id) do nothing;

-- Product 59
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (59, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (59, 'base', true, 39.50, 'ONE_TIME', 59, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (117, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 59)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (118, NOW(), NOW(), '', 'DEFAULT', '', 'en', 59)
on conflict (description_id) do nothing;

-- Product 60
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (60, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 60, 'SKU-SHIS-MAKE-SYNCSKIN60')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (60, 'base', true, 41.00, 'ONE_TIME', 60, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (119, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 60)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (120, NOW(), NOW(), '', 'DEFAULT', '', 'en', 60)
on conflict (description_id) do nothing;

-- Product 61
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (61, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 61, 'SKU-NARS-MAKE-LAGUNABRZ61')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (61, 'base', true, 42.50, 'ONE_TIME', 61, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (121, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 61)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (122, NOW(), NOW(), '', 'DEFAULT', '', 'en', 61)
on conflict (description_id) do nothing;

-- Product 62
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (62, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 62, 'SKU-LRP-SKIN-HYALUB5-62')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (62, 'base', true, 44.00, 'ONE_TIME', 62, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (123, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 62)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (124, NOW(), NOW(), '', 'DEFAULT', '', 'en', 62)
on conflict (description_id) do nothing;

-- Product 63
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (63, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 63, 'SKU-KERA-HAIR-BACICAFLASH63')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (63, 'base', true, 45.50, 'ONE_TIME', 63, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (125, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 63)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (126, NOW(), NOW(), '', 'DEFAULT', '', 'en', 63)
on conflict (description_id) do nothing;

-- Product 64
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (64, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (64, 'base', true, 47.00, 'ONE_TIME', 64, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (127, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 64)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (128, NOW(), NOW(), '', 'DEFAULT', '', 'en', 64)
on conflict (description_id) do nothing;

-- Product 65
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (65, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 65, 'SKU-GUER-FRAG-MONGUERLAIN65')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (65, 'base', true, 48.50, 'ONE_TIME', 65, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (129, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 65)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (130, NOW(), NOW(), '', 'DEFAULT', '', 'en', 65)
on conflict (description_id) do nothing;

-- Product 66
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (66, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 66, 'SKU-SHIS-SKIN-VPUFCR66')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (66, 'base', true, 50.00, 'ONE_TIME', 66, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (131, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 66)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (132, NOW(), NOW(), '', 'DEFAULT', '', 'en', 66)
on conflict (description_id) do nothing;

-- Product 67
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (67, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 67, 'SKU-NARS-MAKE-SHEERGLOW67')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (67, 'base', true, 51.50, 'ONE_TIME', 67, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (133, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 67)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (134, NOW(), NOW(), '', 'DEFAULT', '', 'en', 67)
on conflict (description_id) do nothing;

-- Product 68
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (68, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 68, 'SKU-LRP-SKIN-EFFADUO68')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (68, 'base', true, 53.00, 'ONE_TIME', 68, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (135, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 68)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (136, NOW(), NOW(), '', 'DEFAULT', '', 'en', 68)
on conflict (description_id) do nothing;

-- Product 69
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (69, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 69, 'SKU-KERA-HAIR-CHRONOHUILE69')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (69, 'base', true, 54.50, 'ONE_TIME', 69, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (137, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 69)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (138, NOW(), NOW(), '', 'DEFAULT', '', 'en', 69)
on conflict (description_id) do nothing;

-- Product 70
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (70, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 70, 'SKU-YSL-FRAG-MONPARIS70')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (70, 'base', true, 56.00, 'ONE_TIME', 70, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (139, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 70)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (140, NOW(), NOW(), '', 'DEFAULT', '', 'en', 70)
on conflict (description_id) do nothing;

-- Product 71
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (71, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 71, 'SKU-GUER-SKIN-OICREAM71')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (71, 'base', true, 57.50, 'ONE_TIME', 71, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (141, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 71)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (142, NOW(), NOW(), '', 'DEFAULT', '', 'en', 71)
on conflict (description_id) do nothing;

-- Product 72
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (72, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 72, 'SKU-SHIS-MAKE-MINBLUSH72')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (72, 'base', true, 59.00, 'ONE_TIME', 72, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (143, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 72)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (144, NOW(), NOW(), '', 'DEFAULT', '', 'en', 72)
on conflict (description_id) do nothing;

-- Product 73
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (73, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 73, 'SKU-NARS-MAKE-CLIMAXMASC73')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (73, 'base', true, 60.50, 'ONE_TIME', 73, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (145, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 73)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (146, NOW(), NOW(), '', 'DEFAULT', '', 'en', 73)
on conflict (description_id) do nothing;

-- Product 74
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (74, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 74, 'SKU-LRP-SKIN-TOLSENSCR74')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (74, 'base', true, 62.00, 'ONE_TIME', 74, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (147, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 74)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (148, NOW(), NOW(), '', 'DEFAULT', '', 'en', 74)
on conflict (description_id) do nothing;

-- Product 75
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (75, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 75, 'SKU-KERA-HAIR-CIMENTHERM75')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (75, 'base', true, 63.50, 'ONE_TIME', 75, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (149, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 75)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (150, NOW(), NOW(), '', 'DEFAULT', '', 'en', 75)
on conflict (description_id) do nothing;

-- Product 76
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (76, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 76, 'SKU-YSL-SKIN-PSNIGHT76')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (76, 'base', true, 65.00, 'ONE_TIME', 76, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (151, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 76)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (152, NOW(), NOW(), '', 'DEFAULT', '', 'en', 76)
on conflict (description_id) do nothing;

-- Product 77
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (77, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 77, 'SKU-GUER-FRAG-AAMANDBAS77')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (77, 'base', true, 66.50, 'ONE_TIME', 77, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (153, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 77)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (154, NOW(), NOW(), '', 'DEFAULT', '', 'en', 77)
on conflict (description_id) do nothing;

-- Product 78
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (78, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 78, 'SKU-SHIS-HAIR-TSUBAKIMASK78')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (78, 'base', true, 68.00, 'ONE_TIME', 78, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (155, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 78)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (156, NOW(), NOW(), '', 'DEFAULT', '', 'en', 78)
on conflict (description_id) do nothing;

-- Product 79
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (79, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (79, 'base', true, 69.50, 'ONE_TIME', 79, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (157, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 79)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (158, NOW(), NOW(), '', 'DEFAULT', '', 'en', 79)
on conflict (description_id) do nothing;

-- Product 80
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (80, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 80, 'SKU-LRP-SKIN-LIPIAPM80')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (80, 'base', true, 71.00, 'ONE_TIME', 80, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (159, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 80)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (160, NOW(), NOW(), '', 'DEFAULT', '', 'en', 80)
on conflict (description_id) do nothing;

-- Product 81
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (81, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 81, 'SKU-KERA-HAIR-NUTRI8HSERUM81')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (81, 'base', true, 72.50, 'ONE_TIME', 81, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (161, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 81)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (162, NOW(), NOW(), '', 'DEFAULT', '', 'en', 81)
on conflict (description_id) do nothing;

-- Product 82
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (82, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 82, 'SKU-YSL-MAKE-ALLHOURSFND82')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (82, 'base', true, 74.00, 'ONE_TIME', 82, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (163, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 82)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (164, NOW(), NOW(), '', 'DEFAULT', '', 'en', 82)
on conflict (description_id) do nothing;

-- Product 83
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (83, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 83, 'SKU-GUER-SKIN-SUPERAQUA83')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (83, 'base', true, 75.50, 'ONE_TIME', 83, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (165, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 83)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (166, NOW(), NOW(), '', 'DEFAULT', '', 'en', 83)
on conflict (description_id) do nothing;

-- Product 84
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (84, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 84, 'SKU-SHIS-FRAG-GINZAEDP84')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (84, 'base', true, 77.00, 'ONE_TIME', 84, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (167, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 84)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (168, NOW(), NOW(), '', 'DEFAULT', '', 'en', 84)
on conflict (description_id) do nothing;

-- Product 85
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (85, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (85, 'base', true, 78.50, 'ONE_TIME', 85, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (169, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 85)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (170, NOW(), NOW(), '', 'DEFAULT', '', 'en', 85)
on conflict (description_id) do nothing;

-- Product 86
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (86, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 86, 'SKU-LRP-SKIN-VITC10-86')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (86, 'base', true, 80.00, 'ONE_TIME', 86, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (171, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 86)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (172, NOW(), NOW(), '', 'DEFAULT', '', 'en', 86)
on conflict (description_id) do nothing;

-- Product 87
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (87, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 87, 'SKU-KERA-HAIR-DISCFLUID87')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (87, 'base', true, 81.50, 'ONE_TIME', 87, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (173, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 87)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (174, NOW(), NOW(), '', 'DEFAULT', '', 'en', 87)
on conflict (description_id) do nothing;

-- Product 88
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (88, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 88, 'SKU-YSL-FRAG-YEDP88')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (88, 'base', true, 83.00, 'ONE_TIME', 88, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (175, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 88)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (176, NOW(), NOW(), '', 'DEFAULT', '', 'en', 88)
on conflict (description_id) do nothing;

-- Product 89
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (89, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 89, 'SKU-GUER-MAKE-LESSENTIEL89')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (89, 'base', true, 84.50, 'ONE_TIME', 89, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (177, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 89)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (178, NOW(), NOW(), '', 'DEFAULT', '', 'en', 89)
on conflict (description_id) do nothing;

-- Product 90
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (90, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f020632bc46470c104b76f', 90, 'SKU-SHIS-SKIN-WASOCLEAN90')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (90, 'base', true, 86.00, 'ONE_TIME', 90, '65f020632bc46470c104b76f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (179, NOW(), NOW(), '', 'DEFAULT', '', 'fr', 90)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (180, NOW(), NOW(), '', 'DEFAULT', '', 'en', 90)
on conflict (description_id) do nothing;
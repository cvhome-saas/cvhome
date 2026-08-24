/*
generate more product_availability  based on product file  and add product_price for every product_availability using product_avail_id column as foreign key
start product_availability=91 product_price=91 product_price_description=181
where languages=['ar','fr']
*/

-- Product 91: Toyota Camry
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (91, true, null, null, null, null, false, 15, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 91, 'CAR-SKU-91')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (91, 'base', true, 25000.00, 'ONE_TIME', 91, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (181, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 91)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (182, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 91)
on conflict (description_id) do nothing;

-- Product 92: BMW X5
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (92, true, null, null, null, null, false, 8, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 92, 'CAR-SKU-92')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (92, 'base', true, 65000.00, 'ONE_TIME', 92, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (183, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 92)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (184, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 92)
on conflict (description_id) do nothing;

-- Product 93: Mercedes EQS
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (93, true, null, null, null, null, false, 5, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 93, 'CAR-SKU-93')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (93, 'base', true, 105000.00, 'ONE_TIME', 93, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (185, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 93)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (186, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 93)
on conflict (description_id) do nothing;

-- Product 94: Hyundai Tucson (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (94, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 94, 'CAR-SKU-94')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (94, 'base', true, 22000.00, 'ONE_TIME', 94, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (187, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 94)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (188, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 94)
on conflict (description_id) do nothing;

-- Product 95: Kia Sportage
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (95, true, null, null, null, null, false, 12, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 95, 'CAR-SKU-95')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (95, 'base', true, 28000.00, 'ONE_TIME', 95, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (189, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 95)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (190, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 95)
on conflict (description_id) do nothing;

-- Product 96: Ford Mustang
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (96, true, null, null, null, null, false, 7, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 96, 'CAR-SKU-96')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (96, 'base', true, 40000.00, 'ONE_TIME', 96, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (191, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 96)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (192, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 96)
on conflict (description_id) do nothing;

-- Product 97: Toyota RAV4
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (97, true, null, null, null, null, false, 18, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 97, 'CAR-SKU-97')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (97, 'base', true, 30000.00, 'ONE_TIME', 97, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (193, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 97)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (194, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 97)
on conflict (description_id) do nothing;

-- Product 98: BMW 3 Series (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (98, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 98, 'CAR-SKU-98')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (98, 'base', true, 35000.00, 'ONE_TIME', 98, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (195, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 98)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (196, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 98)
on conflict (description_id) do nothing;

-- Product 99: Mercedes C-Class
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (99, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 99, 'CAR-SKU-99')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (99, 'base', true, 48000.00, 'ONE_TIME', 99, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (197, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 99)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (198, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 99)
on conflict (description_id) do nothing;

-- Product 100: Hyundai Elantra
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (100, true, null, null, null, null, false, 20, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 100, 'CAR-SKU-100')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (100, 'base', true, 22000.00, 'ONE_TIME', 100, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (199, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 100)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (200, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 100)
on conflict (description_id) do nothing;

-- Product 101: Kia Seltos
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (101, true, null, null, null, null, false, 14, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 101, 'CAR-SKU-101')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (101, 'base', true, 24000.00, 'ONE_TIME', 101, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (201, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 101)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (202, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 101)
on conflict (description_id) do nothing;

-- Product 102: Ford F-150 (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (102, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 102, 'CAR-SKU-102')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (102, 'base', true, 38000.00, 'ONE_TIME', 102, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (203, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 102)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (204, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 102)
on conflict (description_id) do nothing;

-- Product 103: Toyota Corolla
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (103, true, null, null, null, null, false, 25, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 103, 'CAR-SKU-103')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (103, 'base', true, 21000.00, 'ONE_TIME', 103, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (205, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 103)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (206, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 103)
on conflict (description_id) do nothing;

-- Product 104: BMW i4
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (104, true, null, null, null, null, false, 6, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 104, 'CAR-SKU-104')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (104, 'base', true, 58000.00, 'ONE_TIME', 104, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (207, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 104)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (208, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 104)
on conflict (description_id) do nothing;

-- Product 105: Mercedes E-Class
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (105, true, null, null, null, null, false, 7, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 105, 'CAR-SKU-105')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (105, 'base', true, 60000.00, 'ONE_TIME', 105, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (209, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 105)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (210, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 105)
on conflict (description_id) do nothing;

-- Product 106: Hyundai Sonata (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (106, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 106, 'CAR-SKU-106')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (106, 'base', true, 26000.00, 'ONE_TIME', 106, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (211, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 106)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (212, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 106)
on conflict (description_id) do nothing;

-- Product 107: Kia EV6
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (107, true, null, null, null, null, false, 10, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 107, 'CAR-SKU-107')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (107, 'base', true, 45000.00, 'ONE_TIME', 107, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (213, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 107)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (214, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 107)
on conflict (description_id) do nothing;

-- Product 108: Ford Explorer
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (108, true, null, null, null, null, false, 11, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 108, 'CAR-SKU-108')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (108, 'base', true, 42000.00, 'ONE_TIME', 108, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (215, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 108)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (216, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 108)
on conflict (description_id) do nothing;

-- Product 109: Toyota Highlander
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (109, true, null, null, null, null, false, 13, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 109, 'CAR-SKU-109')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (109, 'base', true, 40000.00, 'ONE_TIME', 109, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (217, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 109)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (218, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 109)
on conflict (description_id) do nothing;

-- Product 110: BMW X3 (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (110, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 110, 'CAR-SKU-110')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (110, 'base', true, 42000.00, 'ONE_TIME', 110, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (219, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 110)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (220, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 110)
on conflict (description_id) do nothing;

-- Product 111: Mercedes GLC
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (111, true, null, null, null, null, false, 10, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 111, 'CAR-SKU-111')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (111, 'base', true, 50000.00, 'ONE_TIME', 111, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (221, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 111)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (222, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 111)
on conflict (description_id) do nothing;

-- Product 112: Hyundai Santa Fe
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (112, true, null, null, null, null, false, 16, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 112, 'CAR-SKU-112')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (112, 'base', true, 35000.00, 'ONE_TIME', 112, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (223, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 112)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (224, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 112)
on conflict (description_id) do nothing;

-- Product 113: Kia Telluride
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (113, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 113, 'CAR-SKU-113')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (113, 'base', true, 43000.00, 'ONE_TIME', 113, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (225, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 113)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (226, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 113)
on conflict (description_id) do nothing;

-- Product 114: Ford Bronco (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (114, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 114, 'CAR-SKU-114')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (114, 'base', true, 48000.00, 'ONE_TIME', 114, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (227, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 114)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (228, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 114)
on conflict (description_id) do nothing;

-- Product 115: Toyota Sienna
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (115, true, null, null, null, null, false, 10, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 115, 'CAR-SKU-115')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (115, 'base', true, 38000.00, 'ONE_TIME', 115, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (229, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 115)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (230, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 115)
on conflict (description_id) do nothing;

-- Product 116: BMW 5 Series
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (116, true, null, null, null, null, false, 8, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 116, 'CAR-SKU-116')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (116, 'base', true, 58000.00, 'ONE_TIME', 116, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (231, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 116)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (232, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 116)
on conflict (description_id) do nothing;

-- Product 117: Mercedes S-Class
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (117, true, null, null, null, null, false, 4, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 117, 'CAR-SKU-117')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (117, 'base', true, 115000.00, 'ONE_TIME', 117, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (233, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 117)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (234, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 117)
on conflict (description_id) do nothing;

-- Product 118: Hyundai Kona Electric (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (118, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 118, 'CAR-SKU-118')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (118, 'base', true, 30000.00, 'ONE_TIME', 118, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (235, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 118)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (236, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 118)
on conflict (description_id) do nothing;

-- Product 119: Kia Niro EV
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (119, true, null, null, null, null, false, 12, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 119, 'CAR-SKU-119')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (119, 'base', true, 40000.00, 'ONE_TIME', 119, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (237, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 119)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (238, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 119)
on conflict (description_id) do nothing;

-- Product 120: Ford Mustang Mach-E
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (120, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 120, 'CAR-SKU-120')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (120, 'base', true, 48000.00, 'ONE_TIME', 120, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (239, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 120)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (240, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 120)
on conflict (description_id) do nothing;

-- Product 121: Toyota Avalon
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (121, true, null, null, null, null, false, 7, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 121, 'CAR-SKU-121')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (121, 'base', true, 38000.00, 'ONE_TIME', 121, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (241, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 121)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (242, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 121)
on conflict (description_id) do nothing;

-- Product 122: BMW X1
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (122, true, null, null, null, null, false, 11, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 122, 'CAR-SKU-122')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (122, 'base', true, 40000.00, 'ONE_TIME', 122, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (243, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 122)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (244, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 122)
on conflict (description_id) do nothing;

-- Product 123: Mercedes EQB
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (123, true, null, null, null, null, false, 6, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 123, 'CAR-SKU-123')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (123, 'base', true, 55000.00, 'ONE_TIME', 123, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (245, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 123)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (246, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 123)
on conflict (description_id) do nothing;

-- Product 124: Hyundai Accent (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (124, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 124, 'CAR-SKU-124')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (124, 'base', true, 15000.00, 'ONE_TIME', 124, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (247, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 124)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (248, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 124)
on conflict (description_id) do nothing;

-- Product 125: Kia K5
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (125, true, null, null, null, null, false, 14, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 125, 'CAR-SKU-125')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (125, 'base', true, 27000.00, 'ONE_TIME', 125, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (249, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 125)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (250, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 125)
on conflict (description_id) do nothing;

-- Product 126: Ford Escape
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (126, true, null, null, null, null, false, 17, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 126, 'CAR-SKU-126')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (126, 'base', true, 29000.00, 'ONE_TIME', 126, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (251, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 126)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (252, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 126)
on conflict (description_id) do nothing;

-- Product 127: Toyota bZ4X
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (127, true, null, null, null, null, false, 9, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 127, 'CAR-SKU-127')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (127, 'base', true, 43000.00, 'ONE_TIME', 127, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (253, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 127)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (254, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 127)
on conflict (description_id) do nothing;

-- Product 128: BMW X7 (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (128, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 128, 'CAR-SKU-128')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (128, 'base', true, 75000.00, 'ONE_TIME', 128, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (255, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 128)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (256, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 128)
on conflict (description_id) do nothing;

-- Product 129: Mercedes A-Class Sedan
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (129, true, null, null, null, null, false, 10, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 129, 'CAR-SKU-129')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (129, 'base', true, 36000.00, 'ONE_TIME', 129, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (257, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 129)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (258, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 129)
on conflict (description_id) do nothing;

-- Product 130: Hyundai Palisade
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (130, true, null, null, null, null, false, 12, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 130, 'CAR-SKU-130')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (130, 'base', true, 39000.00, 'ONE_TIME', 130, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (259, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 130)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (260, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 130)
on conflict (description_id) do nothing;

-- Product 131: Kia Soul EV
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (131, true, null, null, null, null, false, 8, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 131, 'CAR-SKU-131')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (131, 'base', true, 35000.00, 'ONE_TIME', 131, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (261, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 131)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (262, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 131)
on conflict (description_id) do nothing;

-- Product 132: Ford Focus (Used)
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (132, true, null, null, null, null, false, 1, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 132, 'CAR-SKU-132')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (132, 'base', true, 18000.00, 'ONE_TIME', 132, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (263, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 132)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (264, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 132)
on conflict (description_id) do nothing;

-- Product 133: Toyota Crown
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (133, true, null, null, null, null, false, 7, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 133, 'CAR-SKU-133')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (133, 'base', true, 41000.00, 'ONE_TIME', 133, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (265, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 133)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (266, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 133)
on conflict (description_id) do nothing;

-- Product 134: BMW X6
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (134, true, null, null, null, null, false, 5, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 134, 'CAR-SKU-134')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (134, 'base', true, 75000.00, 'ONE_TIME', 134, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (267, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 134)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (268, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 134)
on conflict (description_id) do nothing;

-- Product 135: Mercedes EQC
INSERT INTO inventory.product_availability (product_avail_id, available, height,
                                          length, weight, width, free_shipping, quantity,
                                          quantity_ord_max, quantity_ord_min, status, region,
                                          store_merchant_id, product_id, sku)
VALUES (135, true, null, null, null, null, false, 6, 1, 1, true, '*',
        '65f023632bc26470c104b75f', 135, 'CAR-SKU-135')
on conflict (product_avail_id) do nothing;

INSERT INTO inventory.product_price (product_price_id, product_price_code, default_price,
                                   product_price_amount, product_price_type, product_avail_id, store_merchant_id)
VALUES (135, 'base', true, 70000.00, 'ONE_TIME', 135, '65f023632bc26470c104b75f')
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (269, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'السعر الأساسي', '', 'ar', 135)
on conflict (description_id) do nothing;
INSERT INTO inventory.product_price_description (description_id, date_created, date_modified, description, name,
                                               title, language_code, product_price_id)
VALUES (270, '2024-04-02 14:00:00.000000', '2024-04-02 14:00:00.000000', '', 'Prix de base', '', 'fr', 135)
on conflict (description_id) do nothing;
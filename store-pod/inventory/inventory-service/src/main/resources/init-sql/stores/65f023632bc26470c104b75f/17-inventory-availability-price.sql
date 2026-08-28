-- Demo stock and price, one row of each per catalog product (matched by sku / product_id).
-- Only the columns the inventory service reads; the rest of the table stays at its defaults.

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (91, '65f023632bc26470c104b75f', 91, 'CAR-SKU-91', 15, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (91, '65f023632bc26470c104b75f', 91, 'base', true, 25000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (92, '65f023632bc26470c104b75f', 92, 'CAR-SKU-92', 8, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (92, '65f023632bc26470c104b75f', 92, 'base', true, 65000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (93, '65f023632bc26470c104b75f', 93, 'CAR-SKU-93', 5, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (93, '65f023632bc26470c104b75f', 93, 'base', true, 105000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (94, '65f023632bc26470c104b75f', 94, 'CAR-SKU-94', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (94, '65f023632bc26470c104b75f', 94, 'base', true, 22000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (95, '65f023632bc26470c104b75f', 95, 'CAR-SKU-95', 12, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (95, '65f023632bc26470c104b75f', 95, 'base', true, 28000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (96, '65f023632bc26470c104b75f', 96, 'CAR-SKU-96', 7, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (96, '65f023632bc26470c104b75f', 96, 'base', true, 40000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (97, '65f023632bc26470c104b75f', 97, 'CAR-SKU-97', 18, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (97, '65f023632bc26470c104b75f', 97, 'base', true, 30000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (98, '65f023632bc26470c104b75f', 98, 'CAR-SKU-98', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (98, '65f023632bc26470c104b75f', 98, 'base', true, 35000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (99, '65f023632bc26470c104b75f', 99, 'CAR-SKU-99', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (99, '65f023632bc26470c104b75f', 99, 'base', true, 48000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (100, '65f023632bc26470c104b75f', 100, 'CAR-SKU-100', 20, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (100, '65f023632bc26470c104b75f', 100, 'base', true, 22000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (101, '65f023632bc26470c104b75f', 101, 'CAR-SKU-101', 14, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (101, '65f023632bc26470c104b75f', 101, 'base', true, 24000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (102, '65f023632bc26470c104b75f', 102, 'CAR-SKU-102', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (102, '65f023632bc26470c104b75f', 102, 'base', true, 38000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (103, '65f023632bc26470c104b75f', 103, 'CAR-SKU-103', 25, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (103, '65f023632bc26470c104b75f', 103, 'base', true, 21000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (104, '65f023632bc26470c104b75f', 104, 'CAR-SKU-104', 6, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (104, '65f023632bc26470c104b75f', 104, 'base', true, 58000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (105, '65f023632bc26470c104b75f', 105, 'CAR-SKU-105', 7, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (105, '65f023632bc26470c104b75f', 105, 'base', true, 60000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (106, '65f023632bc26470c104b75f', 106, 'CAR-SKU-106', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (106, '65f023632bc26470c104b75f', 106, 'base', true, 26000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (107, '65f023632bc26470c104b75f', 107, 'CAR-SKU-107', 10, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (107, '65f023632bc26470c104b75f', 107, 'base', true, 45000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (108, '65f023632bc26470c104b75f', 108, 'CAR-SKU-108', 11, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (108, '65f023632bc26470c104b75f', 108, 'base', true, 42000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (109, '65f023632bc26470c104b75f', 109, 'CAR-SKU-109', 13, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (109, '65f023632bc26470c104b75f', 109, 'base', true, 40000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (110, '65f023632bc26470c104b75f', 110, 'CAR-SKU-110', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (110, '65f023632bc26470c104b75f', 110, 'base', true, 42000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (111, '65f023632bc26470c104b75f', 111, 'CAR-SKU-111', 10, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (111, '65f023632bc26470c104b75f', 111, 'base', true, 50000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (112, '65f023632bc26470c104b75f', 112, 'CAR-SKU-112', 16, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (112, '65f023632bc26470c104b75f', 112, 'base', true, 35000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (113, '65f023632bc26470c104b75f', 113, 'CAR-SKU-113', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (113, '65f023632bc26470c104b75f', 113, 'base', true, 43000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (114, '65f023632bc26470c104b75f', 114, 'CAR-SKU-114', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (114, '65f023632bc26470c104b75f', 114, 'base', true, 48000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (115, '65f023632bc26470c104b75f', 115, 'CAR-SKU-115', 10, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (115, '65f023632bc26470c104b75f', 115, 'base', true, 38000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (116, '65f023632bc26470c104b75f', 116, 'CAR-SKU-116', 8, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (116, '65f023632bc26470c104b75f', 116, 'base', true, 58000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (117, '65f023632bc26470c104b75f', 117, 'CAR-SKU-117', 4, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (117, '65f023632bc26470c104b75f', 117, 'base', true, 115000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (118, '65f023632bc26470c104b75f', 118, 'CAR-SKU-118', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (118, '65f023632bc26470c104b75f', 118, 'base', true, 30000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (119, '65f023632bc26470c104b75f', 119, 'CAR-SKU-119', 12, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (119, '65f023632bc26470c104b75f', 119, 'base', true, 40000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (120, '65f023632bc26470c104b75f', 120, 'CAR-SKU-120', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (120, '65f023632bc26470c104b75f', 120, 'base', true, 48000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (121, '65f023632bc26470c104b75f', 121, 'CAR-SKU-121', 7, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (121, '65f023632bc26470c104b75f', 121, 'base', true, 38000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (122, '65f023632bc26470c104b75f', 122, 'CAR-SKU-122', 11, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (122, '65f023632bc26470c104b75f', 122, 'base', true, 40000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (123, '65f023632bc26470c104b75f', 123, 'CAR-SKU-123', 6, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (123, '65f023632bc26470c104b75f', 123, 'base', true, 55000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (124, '65f023632bc26470c104b75f', 124, 'CAR-SKU-124', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (124, '65f023632bc26470c104b75f', 124, 'base', true, 15000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (125, '65f023632bc26470c104b75f', 125, 'CAR-SKU-125', 14, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (125, '65f023632bc26470c104b75f', 125, 'base', true, 27000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (126, '65f023632bc26470c104b75f', 126, 'CAR-SKU-126', 17, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (126, '65f023632bc26470c104b75f', 126, 'base', true, 29000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (127, '65f023632bc26470c104b75f', 127, 'CAR-SKU-127', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (127, '65f023632bc26470c104b75f', 127, 'base', true, 43000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (128, '65f023632bc26470c104b75f', 128, 'CAR-SKU-128', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (128, '65f023632bc26470c104b75f', 128, 'base', true, 75000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (129, '65f023632bc26470c104b75f', 129, 'CAR-SKU-129', 10, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (129, '65f023632bc26470c104b75f', 129, 'base', true, 36000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (130, '65f023632bc26470c104b75f', 130, 'CAR-SKU-130', 12, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (130, '65f023632bc26470c104b75f', 130, 'base', true, 39000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (131, '65f023632bc26470c104b75f', 131, 'CAR-SKU-131', 8, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (131, '65f023632bc26470c104b75f', 131, 'base', true, 35000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (132, '65f023632bc26470c104b75f', 132, 'CAR-SKU-132', 1, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (132, '65f023632bc26470c104b75f', 132, 'base', true, 18000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (133, '65f023632bc26470c104b75f', 133, 'CAR-SKU-133', 7, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (133, '65f023632bc26470c104b75f', 133, 'base', true, 41000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (134, '65f023632bc26470c104b75f', 134, 'CAR-SKU-134', 5, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (134, '65f023632bc26470c104b75f', 134, 'base', true, 75000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (135, '65f023632bc26470c104b75f', 135, 'CAR-SKU-135', 6, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (135, '65f023632bc26470c104b75f', 135, 'base', true, 70000.00)
on conflict (product_price_id) do nothing;

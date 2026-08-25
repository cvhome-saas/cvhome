-- Demo stock and price, one row of each per catalog product (matched by sku / product_id).
-- Only the columns the inventory service reads; the rest of the table stays at its defaults.

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (1, '65f023632bc46470c104b76f', 1, 'SKU-NK-RUN-001', 25, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (1, '65f023632bc46470c104b76f', 1, 'base', true, 750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2, '65f023632bc46470c104b76f', 2, 'SKU-ZR-CL-DRS02', 40, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2, '65f023632bc46470c104b76f', 2, 'base', true, 350.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3, '65f023632bc46470c104b76f', 3, 'SKU-AD-CL-TPT03', 35, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3, '65f023632bc46470c104b76f', 3, 'base', true, 320.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4, '65f023632bc46470c104b76f', 4, 'SKU-HM-CL-SWT04', 50, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4, '65f023632bc46470c104b76f', 4, 'base', true, 280.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (5, '65f023632bc46470c104b76f', 5, 'SKU-GU-BG-MAR05', 5, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (5, '65f023632bc46470c104b76f', 5, 'base', true, 8500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (6, '65f023632bc46470c104b76f', 6, 'SKU-CH-AC-SUN06', 8, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (6, '65f023632bc46470c104b76f', 6, 'base', true, 2100.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (7, '65f023632bc46470c104b76f', 7, 'SKU-NK-CL-KHD07', 30, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (7, '65f023632bc46470c104b76f', 7, 'base', true, 300.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (8, '65f023632bc46470c104b76f', 8, 'SKU-ZR-SH-SNK08', 22, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (8, '65f023632bc46470c104b76f', 8, 'base', true, 420.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (9, '65f023632bc46470c104b76f', 9, 'SKU-AD-BG-BPK09', 18, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (9, '65f023632bc46470c104b76f', 9, 'base', true, 250.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (10, '65f023632bc46470c104b76f', 10, 'SKU-HM-AC-BLT10', 45, true, 1, 3)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (10, '65f023632bc46470c104b76f', 10, 'base', true, 120.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (11, '65f023632bc46470c104b76f', 11, 'SKU-GU-SH-LOF11', 7, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (11, '65f023632bc46470c104b76f', 11, 'base', true, 3900.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (12, '65f023632bc46470c104b76f', 12, 'SKU-CH-AC-CRD12', 10, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (12, '65f023632bc46470c104b76f', 12, 'base', true, 2400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (13, '65f023632bc46470c104b76f', 13, 'SKU-NK-CL-LEG13', 28, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (13, '65f023632bc46470c104b76f', 13, 'base', true, 380.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (14, '65f023632bc46470c104b76f', 14, 'SKU-ZR-CL-POL14', 33, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (14, '65f023632bc46470c104b76f', 14, 'base', true, 190.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (15, '65f023632bc46470c104b76f', 15, 'SKU-AD-SH-SND15', 26, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (15, '65f023632bc46470c104b76f', 15, 'base', true, 150.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (16, '65f023632bc46470c104b76f', 16, 'SKU-HM-CL-DRS16', 15, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (16, '65f023632bc46470c104b76f', 16, 'base', true, 320.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (17, '65f023632bc46470c104b76f', 17, 'SKU-GU-AC-WAL17', 5, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (17, '65f023632bc46470c104b76f', 17, 'base', true, 1800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (18, '65f023632bc46470c104b76f', 18, 'SKU-CH-AC-BRH18', 3, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (18, '65f023632bc46470c104b76f', 18, 'base', true, 2500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (19, '65f023632bc46470c104b76f', 19, 'SKU-NK-CL-BBS19', 25, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (19, '65f023632bc46470c104b76f', 19, 'base', true, 280.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (20, '65f023632bc46470c104b76f', 20, 'SKU-ZR-SH-SND20', 18, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (20, '65f023632bc46470c104b76f', 20, 'base', true, 290.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (21, '65f023632bc46470c104b76f', 21, 'SKU-AD-CL-HOD21', 12, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (21, '65f023632bc46470c104b76f', 21, 'base', true, 450.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (22, '65f023632bc46470c104b76f', 22, 'SKU-HM-CL-KTP22', 30, true, 1, 3)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (22, '65f023632bc46470c104b76f', 22, 'base', true, 250.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (23, '65f023632bc46470c104b76f', 23, 'SKU-GU-SH-SNK23', 8, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (23, '65f023632bc46470c104b76f', 23, 'base', true, 3200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (24, '65f023632bc46470c104b76f', 24, 'SKU-CH-SH-BAL24', 6, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (24, '65f023632bc46470c104b76f', 24, 'base', true, 3500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (25, '65f023632bc46470c104b76f', 25, 'SKU-NK-CL-TNK25', 20, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (25, '65f023632bc46470c104b76f', 25, 'base', true, 180.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (26, '65f023632bc46470c104b76f', 26, 'SKU-ZR-CL-TRS26', 14, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (26, '65f023632bc46470c104b76f', 26, 'base', true, 350.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (27, '65f023632bc46470c104b76f', 27, 'SKU-AD-CL-KTS27', 10, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (27, '65f023632bc46470c104b76f', 27, 'base', true, 480.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (28, '65f023632bc46470c104b76f', 28, 'SKU-HM-AC-SCF28', 22, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (28, '65f023632bc46470c104b76f', 28, 'base', true, 150.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (29, '65f023632bc46470c104b76f', 29, 'SKU-GU-BG-BBG29', 4, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (29, '65f023632bc46470c104b76f', 29, 'base', true, 4500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (30, '65f023632bc46470c104b76f', 30, 'SKU-CH-SH-SNK30', 5, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (30, '65f023632bc46470c104b76f', 30, 'base', true, 5500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (31, '65f023632bc46470c104b76f', 31, 'SKU-NK-CL-JKT31', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (31, '65f023632bc46470c104b76f', 31, 'base', true, 550.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (32, '65f023632bc46470c104b76f', 32, 'SKU-ZR-CL-JNS32', 17, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (32, '65f023632bc46470c104b76f', 32, 'base', true, 380.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (33, '65f023632bc46470c104b76f', 33, 'SKU-AD-AC-CAP33', 25, true, 1, 3)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (33, '65f023632bc46470c104b76f', 33, 'base', true, 120.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (34, '65f023632bc46470c104b76f', 34, 'SKU-HM-CL-KRJ34', 11, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (34, '65f023632bc46470c104b76f', 34, 'base', true, 300.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (35, '65f023632bc46470c104b76f', 35, 'SKU-GU-AC-SCF35', 7, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (35, '65f023632bc46470c104b76f', 35, 'base', true, 2200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (36, '65f023632bc46470c104b76f', 36, 'SKU-CH-BG-WOC36', 3, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (36, '65f023632bc46470c104b76f', 36, 'base', true, 11000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (37, '65f023632bc46470c104b76f', 37, 'SKU-NK-CL-WRS37', 19, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (37, '65f023632bc46470c104b76f', 37, 'base', true, 200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (38, '65f023632bc46470c104b76f', 38, 'SKU-ZR-CL-KSW38', 16, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (38, '65f023632bc46470c104b76f', 38, 'base', true, 220.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (39, '65f023632bc46470c104b76f', 39, 'SKU-AD-AC-SCK39', 40, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (39, '65f023632bc46470c104b76f', 39, 'base', true, 80.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (40, '65f023632bc46470c104b76f', 40, 'SKU-HM-CL-SWM40', 20, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (40, '65f023632bc46470c104b76f', 40, 'base', true, 190.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (41, '65f023632bc46470c104b76f', 41, 'SKU-GU-SH-HBL41', 6, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (41, '65f023632bc46470c104b76f', 41, 'base', true, 3800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (42, '65f023632bc46470c104b76f', 42, 'SKU-CH-AC-EAR42', 4, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (42, '65f023632bc46470c104b76f', 42, 'base', true, 2800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (43, '65f023632bc46470c104b76f', 43, 'SKU-NK-BG-DUF43', 10, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (43, '65f023632bc46470c104b76f', 43, 'base', true, 350.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (44, '65f023632bc46470c104b76f', 44, 'SKU-ZR-CL-BLS44', 13, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (44, '65f023632bc46470c104b76f', 44, 'base', true, 260.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (45, '65f023632bc46470c104b76f', 45, 'SKU-AD-SH-SLD45', 28, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (45, '65f023632bc46470c104b76f', 45, 'base', true, 150.00)
on conflict (product_price_id) do nothing;

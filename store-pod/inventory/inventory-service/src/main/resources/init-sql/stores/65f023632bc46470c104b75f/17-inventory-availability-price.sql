-- Demo stock and price, one row of each per catalog product (matched by sku / product_id).
-- Only the columns the inventory service reads; the rest of the table stays at its defaults.

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (136, '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136', 100, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (136, '65f023632bc46470c104b75f', 136, 'base', true, 999.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (137, '65f023632bc46470c104b75f', 137, 'ELEC-SKU-137', 100, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (137, '65f023632bc46470c104b75f', 137, 'base', true, 1299.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (138, '65f023632bc46470c104b75f', 138, 'ELEC-SKU-138', 100, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (138, '65f023632bc46470c104b75f', 138, 'base', true, 1599.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (139, '65f023632bc46470c104b75f', 139, 'ELEC-SKU-139', 100, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (139, '65f023632bc46470c104b75f', 139, 'base', true, 399.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (140, '65f023632bc46470c104b75f', 140, 'ELEC-SKU-140', 50, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (140, '65f023632bc46470c104b75f', 140, 'base', true, 1499.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (141, '65f023632bc46470c104b75f', 141, 'ELEC-SKU-141', 100, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (141, '65f023632bc46470c104b75f', 141, 'base', true, 1099.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (142, '65f023632bc46470c104b75f', 142, 'ELEC-SKU-142', 60, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (142, '65f023632bc46470c104b75f', 142, 'base', true, 1199.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (143, '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143', 80, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (143, '65f023632bc46470c104b75f', 143, 'base', true, 1399.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (144, '65f023632bc46470c104b75f', 144, 'ELEC-SKU-144', 150, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (144, '65f023632bc46470c104b75f', 144, 'base', true, 249.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (145, '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145', 120, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (145, '65f023632bc46470c104b75f', 145, 'base', true, 229.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (146, '65f023632bc46470c104b75f', 146, 'ELEC-SKU-146', 70, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (146, '65f023632bc46470c104b75f', 146, 'base', true, 1299.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (147, '65f023632bc46470c104b75f', 147, 'ELEC-SKU-147', 150, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (147, '65f023632bc46470c104b75f', 147, 'base', true, 449.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (148, '65f023632bc46470c104b75f', 148, 'ELEC-SKU-148', 130, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (148, '65f023632bc46470c104b75f', 148, 'base', true, 199.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (149, '65f023632bc46470c104b75f', 149, 'ELEC-SKU-149', 90, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (149, '65f023632bc46470c104b75f', 149, 'base', true, 799.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (150, '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150', 75, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (150, '65f023632bc46470c104b75f', 150, 'base', true, 1699.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (151, '65f023632bc46470c104b75f', 151, 'ELEC-SKU-151', 85, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (151, '65f023632bc46470c104b75f', 151, 'base', true, 999.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (152, '65f023632bc46470c104b75f', 152, 'ELEC-SKU-152', 200, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (152, '65f023632bc46470c104b75f', 152, 'base', true, 179.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (153, '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153', 40, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (153, '65f023632bc46470c104b75f', 153, 'base', true, 1499.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (154, '65f023632bc46470c104b75f', 154, 'ELEC-SKU-154', 90, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (154, '65f023632bc46470c104b75f', 154, 'base', true, 299.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (155, '65f023632bc46470c104b75f', 155, 'ELEC-SKU-155', 65, true, 1, 3)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (155, '65f023632bc46470c104b75f', 155, 'base', true, 999.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (156, '65f023632bc46470c104b75f', 156, 'ELEC-SKU-156', 55, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (156, '65f023632bc46470c104b75f', 156, 'base', true, 1899.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (157, '65f023632bc46470c104b75f', 157, 'ELEC-SKU-157', 60, true, 1, 3)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (157, '65f023632bc46470c104b75f', 157, 'base', true, 1499.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (158, '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158', 45, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (158, '65f023632bc46470c104b75f', 158, 'base', true, 1599.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (159, '65f023632bc46470c104b75f', 159, 'ELEC-SKU-159', 70, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (159, '65f023632bc46470c104b75f', 159, 'base', true, 1449.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (160, '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160', 80, true, 1, 3)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (160, '65f023632bc46470c104b75f', 160, 'base', true, 999.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (161, '65f023632bc46470c104b75f', 161, 'ELEC-SKU-161', 110, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (161, '65f023632bc46470c104b75f', 161, 'base', true, 149.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (162, '65f023632bc46470c104b75f', 162, 'ELEC-SKU-162', 75, true, 1, 3)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (162, '65f023632bc46470c104b75f', 162, 'base', true, 899.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (163, '65f023632bc46470c104b75f', 163, 'ELEC-SKU-163', 95, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (163, '65f023632bc46470c104b75f', 163, 'base', true, 849.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (164, '65f023632bc46470c104b75f', 164, 'ELEC-SKU-164', 100, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (164, '65f023632bc46470c104b75f', 164, 'base', true, 349.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (165, '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165', 140, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (165, '65f023632bc46470c104b75f', 165, 'base', true, 599.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (166, '65f023632bc46470c104b75f', 166, 'ELEC-SKU-166', 50, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (166, '65f023632bc46470c104b75f', 166, 'base', true, 1499.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (167, '65f023632bc46470c104b75f', 167, 'ELEC-SKU-167', 80, true, 1, 4)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (167, '65f023632bc46470c104b75f', 167, 'base', true, 649.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (168, '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168', 160, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (168, '65f023632bc46470c104b75f', 168, 'base', true, 299.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (169, '65f023632bc46470c104b75f', 169, 'ELEC-SKU-169', 180, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (169, '65f023632bc46470c104b75f', 169, 'base', true, 399.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (170, '65f023632bc46470c104b75f', 170, 'ELEC-SKU-170', 110, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (170, '65f023632bc46470c104b75f', 170, 'base', true, 799.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (171, '65f023632bc46470c104b75f', 171, 'ELEC-SKU-171', 100, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (171, '65f023632bc46470c104b75f', 171, 'base', true, 499.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (172, '65f023632bc46470c104b75f', 172, 'ELEC-SKU-172', 80, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (172, '65f023632bc46470c104b75f', 172, 'base', true, 1799.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (173, '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173', 70, true, 1, 3)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (173, '65f023632bc46470c104b75f', 173, 'base', true, 1299.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (174, '65f023632bc46470c104b75f', 174, 'ELEC-SKU-174', 120, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (174, '65f023632bc46470c104b75f', 174, 'base', true, 599.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (175, '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175', 40, true, 1, 2)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (175, '65f023632bc46470c104b75f', 175, 'base', true, 1899.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (176, '65f023632bc46470c104b75f', 176, 'ELEC-SKU-176', 85, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (176, '65f023632bc46470c104b75f', 176, 'base', true, 1499.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (177, '65f023632bc46470c104b75f', 177, 'ELEC-SKU-177', 190, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (177, '65f023632bc46470c104b75f', 177, 'base', true, 299.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (178, '65f023632bc46470c104b75f', 178, 'ELEC-SKU-178', 140, true, 1, 10)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (178, '65f023632bc46470c104b75f', 178, 'base', true, 199.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (179, '65f023632bc46470c104b75f', 179, 'ELEC-SKU-179', 70, true, 1, 5)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (179, '65f023632bc46470c104b75f', 179, 'base', true, 1599.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (180, '65f023632bc46470c104b75f', 180, 'ELEC-SKU-180', 90, true, 1, 3)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (180, '65f023632bc46470c104b75f', 180, 'base', true, 799.99)
on conflict (product_price_id) do nothing;

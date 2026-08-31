-- Stock and price for the electronics store's combination variants.
--
-- One row per combination sku beyond the default variant, whose row the base seed already
-- carries. Prices differ per combination so a matrix shows real per-variant pricing, and a
-- few combinations are deliberately out of stock — that is the greyed-chip case on the PDP.

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (530, '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-128GB-SILVER', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (530, '65f023632bc46470c104b75f', 530, 'base', true, 1039.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (531, '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-256GB-GRAPHITE', 10, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (531, '65f023632bc46470c104b75f', 531, 'base', true, 1119.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (532, '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-256GB-SILVER', 8, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (532, '65f023632bc46470c104b75f', 532, 'base', true, 1159.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (533, '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-512GB-GRAPHITE', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (533, '65f023632bc46470c104b75f', 533, 'base', true, 1299.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (534, '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-512GB-SILVER', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (534, '65f023632bc46470c104b75f', 534, 'base', true, 1339.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (535, '65f023632bc46470c104b75f', 137, 'ELEC-SKU-137-256GB', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (535, '65f023632bc46470c104b75f', 535, 'base', true, 1419.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (536, '65f023632bc46470c104b75f', 137, 'ELEC-SKU-137-512GB', 10, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (536, '65f023632bc46470c104b75f', 536, 'base', true, 1599.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (537, '65f023632bc46470c104b75f', 138, 'ELEC-SKU-138-SILVER', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (537, '65f023632bc46470c104b75f', 537, 'base', true, 1639.00)
on conflict (product_price_id) do nothing;

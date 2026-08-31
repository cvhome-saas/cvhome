-- Stock and price for the beauty store's combination variants.
--
-- One row per combination sku beyond the default variant, whose row the base seed already
-- carries. Prices differ per combination so a matrix shows real per-variant pricing, and a
-- few combinations are deliberately out of stock — that is the greyed-chip case on the PDP.

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (510, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-FAIR-50ML', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (510, '65f020632bc46470c104b76f', 510, 'base', true, 28.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (511, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-MEDIUM-30ML', 10, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (511, '65f020632bc46470c104b76f', 511, 'base', true, 21.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (512, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-MEDIUM-50ML', 8, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (512, '65f020632bc46470c104b76f', 512, 'base', true, 29.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (513, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-DEEP-30ML', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (513, '65f020632bc46470c104b76f', 513, 'base', true, 22.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (514, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-DEEP-50ML', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (514, '65f020632bc46470c104b76f', 514, 'base', true, 30.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (515, '65f020632bc46470c104b76f', 47, 'SKU-GUER-SKIN-ARWOIL47-MEDIUM', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (515, '65f020632bc46470c104b76f', 515, 'base', true, 22.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (516, '65f020632bc46470c104b76f', 47, 'SKU-GUER-SKIN-ARWOIL47-DEEP', 10, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (516, '65f020632bc46470c104b76f', 516, 'base', true, 23.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (517, '65f020632bc46470c104b76f', 48, 'SKU-SHIS-SKIN-ULTIMUNE48-50ML', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (517, '65f020632bc46470c104b76f', 517, 'base', true, 31.00)
on conflict (product_price_id) do nothing;

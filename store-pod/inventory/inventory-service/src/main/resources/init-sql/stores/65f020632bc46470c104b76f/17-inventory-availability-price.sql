-- Demo stock and price, one row of each per catalog product (matched by sku / product_id).
-- Only the columns the inventory service reads; the rest of the table stays at its defaults.

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (46, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (46, '65f020632bc46470c104b76f', 46, 'base', true, 20.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (47, '65f020632bc46470c104b76f', 47, 'SKU-GUER-SKIN-ARWOIL47', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (47, '65f020632bc46470c104b76f', 47, 'base', true, 21.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (48, '65f020632bc46470c104b76f', 48, 'SKU-SHIS-SKIN-ULTIMUNE48', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (48, '65f020632bc46470c104b76f', 48, 'base', true, 23.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (49, '65f020632bc46470c104b76f', 49, 'SKU-NARS-MAKE-RCCONC49', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (49, '65f020632bc46470c104b76f', 49, 'base', true, 24.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (50, '65f020632bc46470c104b76f', 50, 'SKU-LRP-SKIN-ANTHUV50', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (50, '65f020632bc46470c104b76f', 50, 'base', true, 26.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (51, '65f020632bc46470c104b76f', 51, 'SKU-KERA-HAIR-GENSHMP51', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (51, '65f020632bc46470c104b76f', 51, 'base', true, 27.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (52, '65f020632bc46470c104b76f', 52, 'SKU-YSL-FRAG-BLACKOPIUM52', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (52, '65f020632bc46470c104b76f', 52, 'base', true, 29.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (53, '65f020632bc46470c104b76f', 53, 'SKU-GUER-MAKE-TERRACOTTA53', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (53, '65f020632bc46470c104b76f', 53, 'base', true, 30.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (54, '65f020632bc46470c104b76f', 54, 'SKU-SHIS-SKIN-BENEFCR54', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (54, '65f020632bc46470c104b76f', 54, 'base', true, 32.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (55, '65f020632bc46470c104b76f', 55, 'SKU-NARS-MAKE-ORGBLUSH55', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (55, '65f020632bc46470c104b76f', 55, 'base', true, 33.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (56, '65f020632bc46470c104b76f', 56, 'SKU-LRP-SKIN-CICAB5-56', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (56, '65f020632bc46470c104b76f', 56, 'base', true, 35.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (57, '65f020632bc46470c104b76f', 57, 'SKU-KERA-HAIR-ELIXIR57', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (57, '65f020632bc46470c104b76f', 57, 'base', true, 36.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (58, '65f020632bc46470c104b76f', 58, 'SKU-YSL-FRAG-LIBREEDP58', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (58, '65f020632bc46470c104b76f', 58, 'base', true, 38.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (59, '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (59, '65f020632bc46470c104b76f', 59, 'base', true, 39.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (60, '65f020632bc46470c104b76f', 60, 'SKU-SHIS-MAKE-SYNCSKIN60', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (60, '65f020632bc46470c104b76f', 60, 'base', true, 41.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (61, '65f020632bc46470c104b76f', 61, 'SKU-NARS-MAKE-LAGUNABRZ61', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (61, '65f020632bc46470c104b76f', 61, 'base', true, 42.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (62, '65f020632bc46470c104b76f', 62, 'SKU-LRP-SKIN-HYALUB5-62', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (62, '65f020632bc46470c104b76f', 62, 'base', true, 44.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (63, '65f020632bc46470c104b76f', 63, 'SKU-KERA-HAIR-BACICAFLASH63', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (63, '65f020632bc46470c104b76f', 63, 'base', true, 45.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (64, '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (64, '65f020632bc46470c104b76f', 64, 'base', true, 47.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (65, '65f020632bc46470c104b76f', 65, 'SKU-GUER-FRAG-MONGUERLAIN65', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (65, '65f020632bc46470c104b76f', 65, 'base', true, 48.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (66, '65f020632bc46470c104b76f', 66, 'SKU-SHIS-SKIN-VPUFCR66', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (66, '65f020632bc46470c104b76f', 66, 'base', true, 50.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (67, '65f020632bc46470c104b76f', 67, 'SKU-NARS-MAKE-SHEERGLOW67', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (67, '65f020632bc46470c104b76f', 67, 'base', true, 51.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (68, '65f020632bc46470c104b76f', 68, 'SKU-LRP-SKIN-EFFADUO68', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (68, '65f020632bc46470c104b76f', 68, 'base', true, 53.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (69, '65f020632bc46470c104b76f', 69, 'SKU-KERA-HAIR-CHRONOHUILE69', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (69, '65f020632bc46470c104b76f', 69, 'base', true, 54.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (70, '65f020632bc46470c104b76f', 70, 'SKU-YSL-FRAG-MONPARIS70', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (70, '65f020632bc46470c104b76f', 70, 'base', true, 56.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (71, '65f020632bc46470c104b76f', 71, 'SKU-GUER-SKIN-OICREAM71', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (71, '65f020632bc46470c104b76f', 71, 'base', true, 57.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (72, '65f020632bc46470c104b76f', 72, 'SKU-SHIS-MAKE-MINBLUSH72', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (72, '65f020632bc46470c104b76f', 72, 'base', true, 59.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (73, '65f020632bc46470c104b76f', 73, 'SKU-NARS-MAKE-CLIMAXMASC73', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (73, '65f020632bc46470c104b76f', 73, 'base', true, 60.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (74, '65f020632bc46470c104b76f', 74, 'SKU-LRP-SKIN-TOLSENSCR74', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (74, '65f020632bc46470c104b76f', 74, 'base', true, 62.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (75, '65f020632bc46470c104b76f', 75, 'SKU-KERA-HAIR-CIMENTHERM75', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (75, '65f020632bc46470c104b76f', 75, 'base', true, 63.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (76, '65f020632bc46470c104b76f', 76, 'SKU-YSL-SKIN-PSNIGHT76', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (76, '65f020632bc46470c104b76f', 76, 'base', true, 65.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (77, '65f020632bc46470c104b76f', 77, 'SKU-GUER-FRAG-AAMANDBAS77', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (77, '65f020632bc46470c104b76f', 77, 'base', true, 66.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (78, '65f020632bc46470c104b76f', 78, 'SKU-SHIS-HAIR-TSUBAKIMASK78', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (78, '65f020632bc46470c104b76f', 78, 'base', true, 68.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (79, '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (79, '65f020632bc46470c104b76f', 79, 'base', true, 69.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (80, '65f020632bc46470c104b76f', 80, 'SKU-LRP-SKIN-LIPIAPM80', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (80, '65f020632bc46470c104b76f', 80, 'base', true, 71.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (81, '65f020632bc46470c104b76f', 81, 'SKU-KERA-HAIR-NUTRI8HSERUM81', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (81, '65f020632bc46470c104b76f', 81, 'base', true, 72.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (82, '65f020632bc46470c104b76f', 82, 'SKU-YSL-MAKE-ALLHOURSFND82', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (82, '65f020632bc46470c104b76f', 82, 'base', true, 74.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (83, '65f020632bc46470c104b76f', 83, 'SKU-GUER-SKIN-SUPERAQUA83', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (83, '65f020632bc46470c104b76f', 83, 'base', true, 75.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (84, '65f020632bc46470c104b76f', 84, 'SKU-SHIS-FRAG-GINZAEDP84', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (84, '65f020632bc46470c104b76f', 84, 'base', true, 77.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (85, '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (85, '65f020632bc46470c104b76f', 85, 'base', true, 78.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (86, '65f020632bc46470c104b76f', 86, 'SKU-LRP-SKIN-VITC10-86', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (86, '65f020632bc46470c104b76f', 86, 'base', true, 80.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (87, '65f020632bc46470c104b76f', 87, 'SKU-KERA-HAIR-DISCFLUID87', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (87, '65f020632bc46470c104b76f', 87, 'base', true, 81.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (88, '65f020632bc46470c104b76f', 88, 'SKU-YSL-FRAG-YEDP88', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (88, '65f020632bc46470c104b76f', 88, 'base', true, 83.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (89, '65f020632bc46470c104b76f', 89, 'SKU-GUER-MAKE-LESSENTIEL89', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (89, '65f020632bc46470c104b76f', 89, 'base', true, 84.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (90, '65f020632bc46470c104b76f', 90, 'SKU-SHIS-SKIN-WASOCLEAN90', 9, true, 1, 1)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (90, '65f020632bc46470c104b76f', 90, 'base', true, 86.00)
on conflict (product_price_id) do nothing;

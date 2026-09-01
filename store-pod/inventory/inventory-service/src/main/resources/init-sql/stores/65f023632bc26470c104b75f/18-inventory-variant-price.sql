-- Stock and price for the cars store's combination variants.
--
-- One row per combination sku beyond the default variant, whose row the base seed already
-- carries. Prices differ per combination so a matrix shows real per-variant pricing, and a
-- few combinations are deliberately out of stock — that is the greyed-chip case on the PDP.

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (520, '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-WHITE-PREMIUM', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (520, '65f023632bc26470c104b75f', 520, 'base', true, 31500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (521, '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-BLACK-STANDARD', 10, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (521, '65f023632bc26470c104b75f', 521, 'base', true, 25400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (522, '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-BLACK-PREMIUM', 8, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (522, '65f023632bc26470c104b75f', 522, 'base', true, 31900.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (523, '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-SILVER-STANDARD', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (523, '65f023632bc26470c104b75f', 523, 'base', true, 25250.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (524, '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-SILVER-PREMIUM', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (524, '65f023632bc26470c104b75f', 524, 'base', true, 31750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (525, '65f023632bc26470c104b75f', 92, 'CAR-SKU-92-BLACK', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (525, '65f023632bc26470c104b75f', 525, 'base', true, 65400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (526, '65f023632bc26470c104b75f', 92, 'CAR-SKU-92-SILVER', 10, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (526, '65f023632bc26470c104b75f', 526, 'base', true, 65250.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (527, '65f023632bc26470c104b75f', 93, 'CAR-SKU-93-PREMIUM', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (527, '65f023632bc26470c104b75f', 527, 'base', true, 111500.00)
on conflict (product_price_id) do nothing;

-- ---------------------------------------------------------------------------------------------------------
-- Generated bulk: stock and price for the combination skus of 18-catalog-options-variants.sql.
--
-- One row per combination beyond the default variant, whose row the base seed already carries. Prices step
-- up with the combination so a matrix shows real per-variant pricing, and the quantity cycle leaves a few
-- combinations at zero — the greyed-chip case on the PDP.
--
-- Regenerated by extra/scripts/generate-demo-variants.py; edit that rather than these lines.
-- ---------------------------------------------------------------------------------------------------------

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3001, '65f023632bc26470c104b75f', 94, 'CAR-SKU-94-BLACK', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3001, '65f023632bc26470c104b75f', 3001, 'base', true, 23100.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3002, '65f023632bc26470c104b75f', 94, 'CAR-SKU-94-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3002, '65f023632bc26470c104b75f', 3002, 'base', true, 24200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3003, '65f023632bc26470c104b75f', 95, 'CAR-SKU-95-WHITE-PREMIUM', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3003, '65f023632bc26470c104b75f', 3003, 'base', true, 29400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3004, '65f023632bc26470c104b75f', 95, 'CAR-SKU-95-BLACK-STANDARD', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3004, '65f023632bc26470c104b75f', 3004, 'base', true, 30800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3005, '65f023632bc26470c104b75f', 95, 'CAR-SKU-95-BLACK-PREMIUM', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3005, '65f023632bc26470c104b75f', 3005, 'base', true, 32200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3006, '65f023632bc26470c104b75f', 97, 'CAR-SKU-97-PREMIUM', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3006, '65f023632bc26470c104b75f', 3006, 'base', true, 31500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3007, '65f023632bc26470c104b75f', 97, 'CAR-SKU-97-SPORT', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3007, '65f023632bc26470c104b75f', 3007, 'base', true, 33000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3008, '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-WHITE-PREMIUM', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3008, '65f023632bc26470c104b75f', 3008, 'base', true, 36750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3009, '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-BLACK-STANDARD', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3009, '65f023632bc26470c104b75f', 3009, 'base', true, 38500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3010, '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-BLACK-PREMIUM', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3010, '65f023632bc26470c104b75f', 3010, 'base', true, 40250.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3011, '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-SILVER-STANDARD', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3011, '65f023632bc26470c104b75f', 3011, 'base', true, 42000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3012, '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-SILVER-PREMIUM', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3012, '65f023632bc26470c104b75f', 3012, 'base', true, 43750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3013, '65f023632bc26470c104b75f', 99, 'CAR-SKU-99-BLACK', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3013, '65f023632bc26470c104b75f', 3013, 'base', true, 50400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3014, '65f023632bc26470c104b75f', 99, 'CAR-SKU-99-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3014, '65f023632bc26470c104b75f', 3014, 'base', true, 52800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3015, '65f023632bc26470c104b75f', 99, 'CAR-SKU-99-BLUE', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3015, '65f023632bc26470c104b75f', 3015, 'base', true, 55200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3016, '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-WHITE-PREMIUM', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3016, '65f023632bc26470c104b75f', 3016, 'base', true, 23100.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3017, '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-WHITE-SPORT', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3017, '65f023632bc26470c104b75f', 3017, 'base', true, 24200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3018, '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-BLACK-STANDARD', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3018, '65f023632bc26470c104b75f', 3018, 'base', true, 25300.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3019, '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-BLACK-PREMIUM', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3019, '65f023632bc26470c104b75f', 3019, 'base', true, 26400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3020, '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-BLACK-SPORT', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3020, '65f023632bc26470c104b75f', 3020, 'base', true, 27500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3021, '65f023632bc26470c104b75f', 102, 'CAR-SKU-102-BLACK', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3021, '65f023632bc26470c104b75f', 3021, 'base', true, 39900.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3022, '65f023632bc26470c104b75f', 102, 'CAR-SKU-102-SILVER', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3022, '65f023632bc26470c104b75f', 3022, 'base', true, 41800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3023, '65f023632bc26470c104b75f', 103, 'CAR-SKU-103-WHITE-PREMIUM', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3023, '65f023632bc26470c104b75f', 3023, 'base', true, 22050.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3024, '65f023632bc26470c104b75f', 103, 'CAR-SKU-103-BLACK-STANDARD', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3024, '65f023632bc26470c104b75f', 3024, 'base', true, 23100.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3025, '65f023632bc26470c104b75f', 103, 'CAR-SKU-103-BLACK-PREMIUM', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3025, '65f023632bc26470c104b75f', 3025, 'base', true, 24150.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3026, '65f023632bc26470c104b75f', 104, 'CAR-SKU-104-PREMIUM', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3026, '65f023632bc26470c104b75f', 3026, 'base', true, 60900.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3027, '65f023632bc26470c104b75f', 104, 'CAR-SKU-104-SPORT', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3027, '65f023632bc26470c104b75f', 3027, 'base', true, 63800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3028, '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-WHITE-PREMIUM', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3028, '65f023632bc26470c104b75f', 3028, 'base', true, 63000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3029, '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-BLACK-STANDARD', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3029, '65f023632bc26470c104b75f', 3029, 'base', true, 66000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3030, '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-BLACK-PREMIUM', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3030, '65f023632bc26470c104b75f', 3030, 'base', true, 69000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3031, '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-SILVER-STANDARD', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3031, '65f023632bc26470c104b75f', 3031, 'base', true, 72000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3032, '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-SILVER-PREMIUM', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3032, '65f023632bc26470c104b75f', 3032, 'base', true, 75000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3033, '65f023632bc26470c104b75f', 107, 'CAR-SKU-107-BLACK', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3033, '65f023632bc26470c104b75f', 3033, 'base', true, 47250.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3034, '65f023632bc26470c104b75f', 107, 'CAR-SKU-107-SILVER', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3034, '65f023632bc26470c104b75f', 3034, 'base', true, 49500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3035, '65f023632bc26470c104b75f', 107, 'CAR-SKU-107-BLUE', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3035, '65f023632bc26470c104b75f', 3035, 'base', true, 51750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3036, '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-WHITE-PREMIUM', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3036, '65f023632bc26470c104b75f', 3036, 'base', true, 44100.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3037, '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-WHITE-SPORT', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3037, '65f023632bc26470c104b75f', 3037, 'base', true, 46200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3038, '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-BLACK-STANDARD', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3038, '65f023632bc26470c104b75f', 3038, 'base', true, 48300.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3039, '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-BLACK-PREMIUM', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3039, '65f023632bc26470c104b75f', 3039, 'base', true, 50400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3040, '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-BLACK-SPORT', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3040, '65f023632bc26470c104b75f', 3040, 'base', true, 52500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3041, '65f023632bc26470c104b75f', 109, 'CAR-SKU-109-BLACK', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3041, '65f023632bc26470c104b75f', 3041, 'base', true, 42000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3042, '65f023632bc26470c104b75f', 109, 'CAR-SKU-109-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3042, '65f023632bc26470c104b75f', 3042, 'base', true, 44000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3043, '65f023632bc26470c104b75f', 110, 'CAR-SKU-110-WHITE-PREMIUM', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3043, '65f023632bc26470c104b75f', 3043, 'base', true, 44100.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3044, '65f023632bc26470c104b75f', 110, 'CAR-SKU-110-BLACK-STANDARD', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3044, '65f023632bc26470c104b75f', 3044, 'base', true, 46200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3045, '65f023632bc26470c104b75f', 110, 'CAR-SKU-110-BLACK-PREMIUM', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3045, '65f023632bc26470c104b75f', 3045, 'base', true, 48300.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3046, '65f023632bc26470c104b75f', 112, 'CAR-SKU-112-PREMIUM', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3046, '65f023632bc26470c104b75f', 3046, 'base', true, 36750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3047, '65f023632bc26470c104b75f', 112, 'CAR-SKU-112-SPORT', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3047, '65f023632bc26470c104b75f', 3047, 'base', true, 38500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3048, '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-WHITE-PREMIUM', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3048, '65f023632bc26470c104b75f', 3048, 'base', true, 45150.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3049, '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-BLACK-STANDARD', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3049, '65f023632bc26470c104b75f', 3049, 'base', true, 47300.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3050, '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-BLACK-PREMIUM', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3050, '65f023632bc26470c104b75f', 3050, 'base', true, 49450.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3051, '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-SILVER-STANDARD', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3051, '65f023632bc26470c104b75f', 3051, 'base', true, 51600.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3052, '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-SILVER-PREMIUM', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3052, '65f023632bc26470c104b75f', 3052, 'base', true, 53750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3053, '65f023632bc26470c104b75f', 114, 'CAR-SKU-114-BLACK', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3053, '65f023632bc26470c104b75f', 3053, 'base', true, 50400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3054, '65f023632bc26470c104b75f', 114, 'CAR-SKU-114-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3054, '65f023632bc26470c104b75f', 3054, 'base', true, 52800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3055, '65f023632bc26470c104b75f', 114, 'CAR-SKU-114-BLUE', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3055, '65f023632bc26470c104b75f', 3055, 'base', true, 55200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3056, '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-WHITE-PREMIUM', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3056, '65f023632bc26470c104b75f', 3056, 'base', true, 39900.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3057, '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-WHITE-SPORT', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3057, '65f023632bc26470c104b75f', 3057, 'base', true, 41800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3058, '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-BLACK-STANDARD', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3058, '65f023632bc26470c104b75f', 3058, 'base', true, 43700.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3059, '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-BLACK-PREMIUM', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3059, '65f023632bc26470c104b75f', 3059, 'base', true, 45600.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3060, '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-BLACK-SPORT', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3060, '65f023632bc26470c104b75f', 3060, 'base', true, 47500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3061, '65f023632bc26470c104b75f', 117, 'CAR-SKU-117-BLACK', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3061, '65f023632bc26470c104b75f', 3061, 'base', true, 120750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3062, '65f023632bc26470c104b75f', 117, 'CAR-SKU-117-SILVER', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3062, '65f023632bc26470c104b75f', 3062, 'base', true, 126500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3063, '65f023632bc26470c104b75f', 118, 'CAR-SKU-118-WHITE-PREMIUM', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3063, '65f023632bc26470c104b75f', 3063, 'base', true, 31500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3064, '65f023632bc26470c104b75f', 118, 'CAR-SKU-118-BLACK-STANDARD', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3064, '65f023632bc26470c104b75f', 3064, 'base', true, 33000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3065, '65f023632bc26470c104b75f', 118, 'CAR-SKU-118-BLACK-PREMIUM', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3065, '65f023632bc26470c104b75f', 3065, 'base', true, 34500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3066, '65f023632bc26470c104b75f', 119, 'CAR-SKU-119-PREMIUM', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3066, '65f023632bc26470c104b75f', 3066, 'base', true, 42000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3067, '65f023632bc26470c104b75f', 119, 'CAR-SKU-119-SPORT', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3067, '65f023632bc26470c104b75f', 3067, 'base', true, 44000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3068, '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-WHITE-PREMIUM', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3068, '65f023632bc26470c104b75f', 3068, 'base', true, 50400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3069, '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-BLACK-STANDARD', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3069, '65f023632bc26470c104b75f', 3069, 'base', true, 52800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3070, '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-BLACK-PREMIUM', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3070, '65f023632bc26470c104b75f', 3070, 'base', true, 55200.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3071, '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-SILVER-STANDARD', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3071, '65f023632bc26470c104b75f', 3071, 'base', true, 57600.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3072, '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-SILVER-PREMIUM', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3072, '65f023632bc26470c104b75f', 3072, 'base', true, 60000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3073, '65f023632bc26470c104b75f', 122, 'CAR-SKU-122-BLACK', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3073, '65f023632bc26470c104b75f', 3073, 'base', true, 42000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3074, '65f023632bc26470c104b75f', 122, 'CAR-SKU-122-SILVER', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3074, '65f023632bc26470c104b75f', 3074, 'base', true, 44000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3075, '65f023632bc26470c104b75f', 122, 'CAR-SKU-122-BLUE', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3075, '65f023632bc26470c104b75f', 3075, 'base', true, 46000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3076, '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-WHITE-PREMIUM', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3076, '65f023632bc26470c104b75f', 3076, 'base', true, 57750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3077, '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-WHITE-SPORT', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3077, '65f023632bc26470c104b75f', 3077, 'base', true, 60500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3078, '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-BLACK-STANDARD', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3078, '65f023632bc26470c104b75f', 3078, 'base', true, 63250.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3079, '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-BLACK-PREMIUM', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3079, '65f023632bc26470c104b75f', 3079, 'base', true, 66000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3080, '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-BLACK-SPORT', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3080, '65f023632bc26470c104b75f', 3080, 'base', true, 68750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3081, '65f023632bc26470c104b75f', 124, 'CAR-SKU-124-BLACK', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3081, '65f023632bc26470c104b75f', 3081, 'base', true, 15750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3082, '65f023632bc26470c104b75f', 124, 'CAR-SKU-124-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3082, '65f023632bc26470c104b75f', 3082, 'base', true, 16500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3083, '65f023632bc26470c104b75f', 125, 'CAR-SKU-125-WHITE-PREMIUM', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3083, '65f023632bc26470c104b75f', 3083, 'base', true, 28350.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3084, '65f023632bc26470c104b75f', 125, 'CAR-SKU-125-BLACK-STANDARD', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3084, '65f023632bc26470c104b75f', 3084, 'base', true, 29700.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3085, '65f023632bc26470c104b75f', 125, 'CAR-SKU-125-BLACK-PREMIUM', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3085, '65f023632bc26470c104b75f', 3085, 'base', true, 31050.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3086, '65f023632bc26470c104b75f', 127, 'CAR-SKU-127-PREMIUM', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3086, '65f023632bc26470c104b75f', 3086, 'base', true, 45150.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3087, '65f023632bc26470c104b75f', 127, 'CAR-SKU-127-SPORT', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3087, '65f023632bc26470c104b75f', 3087, 'base', true, 47300.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3088, '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-WHITE-PREMIUM', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3088, '65f023632bc26470c104b75f', 3088, 'base', true, 78750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3089, '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-BLACK-STANDARD', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3089, '65f023632bc26470c104b75f', 3089, 'base', true, 82500.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3090, '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-BLACK-PREMIUM', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3090, '65f023632bc26470c104b75f', 3090, 'base', true, 86250.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3091, '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-SILVER-STANDARD', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3091, '65f023632bc26470c104b75f', 3091, 'base', true, 90000.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3092, '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-SILVER-PREMIUM', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3092, '65f023632bc26470c104b75f', 3092, 'base', true, 93750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3093, '65f023632bc26470c104b75f', 129, 'CAR-SKU-129-BLACK', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3093, '65f023632bc26470c104b75f', 3093, 'base', true, 37800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3094, '65f023632bc26470c104b75f', 129, 'CAR-SKU-129-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3094, '65f023632bc26470c104b75f', 3094, 'base', true, 39600.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3095, '65f023632bc26470c104b75f', 129, 'CAR-SKU-129-BLUE', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3095, '65f023632bc26470c104b75f', 3095, 'base', true, 41400.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3096, '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-WHITE-PREMIUM', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3096, '65f023632bc26470c104b75f', 3096, 'base', true, 40950.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3097, '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-WHITE-SPORT', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3097, '65f023632bc26470c104b75f', 3097, 'base', true, 42900.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3098, '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-BLACK-STANDARD', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3098, '65f023632bc26470c104b75f', 3098, 'base', true, 44850.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3099, '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-BLACK-PREMIUM', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3099, '65f023632bc26470c104b75f', 3099, 'base', true, 46800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3100, '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-BLACK-SPORT', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3100, '65f023632bc26470c104b75f', 3100, 'base', true, 48750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3101, '65f023632bc26470c104b75f', 132, 'CAR-SKU-132-BLACK', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3101, '65f023632bc26470c104b75f', 3101, 'base', true, 18900.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3102, '65f023632bc26470c104b75f', 132, 'CAR-SKU-132-SILVER', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3102, '65f023632bc26470c104b75f', 3102, 'base', true, 19800.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3103, '65f023632bc26470c104b75f', 133, 'CAR-SKU-133-WHITE-PREMIUM', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3103, '65f023632bc26470c104b75f', 3103, 'base', true, 43050.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3104, '65f023632bc26470c104b75f', 133, 'CAR-SKU-133-BLACK-STANDARD', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3104, '65f023632bc26470c104b75f', 3104, 'base', true, 45100.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3105, '65f023632bc26470c104b75f', 133, 'CAR-SKU-133-BLACK-PREMIUM', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3105, '65f023632bc26470c104b75f', 3105, 'base', true, 47150.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3106, '65f023632bc26470c104b75f', 134, 'CAR-SKU-134-PREMIUM', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3106, '65f023632bc26470c104b75f', 3106, 'base', true, 78750.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (3107, '65f023632bc26470c104b75f', 134, 'CAR-SKU-134-SPORT', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (3107, '65f023632bc26470c104b75f', 3107, 'base', true, 82500.00)
on conflict (product_price_id) do nothing;

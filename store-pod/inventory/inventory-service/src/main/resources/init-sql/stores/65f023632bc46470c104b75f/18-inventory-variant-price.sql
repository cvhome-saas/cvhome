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
VALUES (4001, '65f023632bc46470c104b75f', 139, 'ELEC-SKU-139-256GB', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4001, '65f023632bc46470c104b75f', 4001, 'base', true, 419.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4002, '65f023632bc46470c104b75f', 139, 'ELEC-SKU-139-512GB', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4002, '65f023632bc46470c104b75f', 4002, 'base', true, 439.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4003, '65f023632bc46470c104b75f', 140, 'ELEC-SKU-140-128GB-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4003, '65f023632bc46470c104b75f', 4003, 'base', true, 1574.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4004, '65f023632bc46470c104b75f', 140, 'ELEC-SKU-140-256GB-GRAPHITE', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4004, '65f023632bc46470c104b75f', 4004, 'base', true, 1649.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4005, '65f023632bc46470c104b75f', 140, 'ELEC-SKU-140-256GB-SILVER', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4005, '65f023632bc46470c104b75f', 4005, 'base', true, 1724.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4006, '65f023632bc46470c104b75f', 142, 'ELEC-SKU-142-SILVER', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4006, '65f023632bc46470c104b75f', 4006, 'base', true, 1259.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4007, '65f023632bc46470c104b75f', 142, 'ELEC-SKU-142-MIDNIGHT', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4007, '65f023632bc46470c104b75f', 4007, 'base', true, 1319.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4008, '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-128GB-SILVER', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4008, '65f023632bc46470c104b75f', 4008, 'base', true, 1469.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4009, '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-256GB-GRAPHITE', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4009, '65f023632bc46470c104b75f', 4009, 'base', true, 1539.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4010, '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-256GB-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4010, '65f023632bc46470c104b75f', 4010, 'base', true, 1609.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4011, '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-512GB-GRAPHITE', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4011, '65f023632bc46470c104b75f', 4011, 'base', true, 1679.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4012, '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-512GB-SILVER', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4012, '65f023632bc46470c104b75f', 4012, 'base', true, 1749.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4013, '65f023632bc46470c104b75f', 144, 'ELEC-SKU-144-256GB', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4013, '65f023632bc46470c104b75f', 4013, 'base', true, 261.45)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4014, '65f023632bc46470c104b75f', 144, 'ELEC-SKU-144-512GB', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4014, '65f023632bc46470c104b75f', 4014, 'base', true, 273.90)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4015, '65f023632bc46470c104b75f', 144, 'ELEC-SKU-144-1TB', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4015, '65f023632bc46470c104b75f', 4015, 'base', true, 286.35)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4016, '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-128GB-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4016, '65f023632bc46470c104b75f', 4016, 'base', true, 241.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4017, '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-128GB-MIDNIGHT', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4017, '65f023632bc46470c104b75f', 4017, 'base', true, 252.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4018, '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-256GB-GRAPHITE', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4018, '65f023632bc46470c104b75f', 4018, 'base', true, 264.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4019, '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-256GB-SILVER', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4019, '65f023632bc46470c104b75f', 4019, 'base', true, 275.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4020, '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-256GB-MIDNIGHT', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4020, '65f023632bc46470c104b75f', 4020, 'base', true, 287.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4021, '65f023632bc46470c104b75f', 147, 'ELEC-SKU-147-256GB', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4021, '65f023632bc46470c104b75f', 4021, 'base', true, 472.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4022, '65f023632bc46470c104b75f', 147, 'ELEC-SKU-147-512GB', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4022, '65f023632bc46470c104b75f', 4022, 'base', true, 494.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4023, '65f023632bc46470c104b75f', 148, 'ELEC-SKU-148-128GB-SILVER', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4023, '65f023632bc46470c104b75f', 4023, 'base', true, 209.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4024, '65f023632bc46470c104b75f', 148, 'ELEC-SKU-148-256GB-GRAPHITE', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4024, '65f023632bc46470c104b75f', 4024, 'base', true, 219.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4025, '65f023632bc46470c104b75f', 148, 'ELEC-SKU-148-256GB-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4025, '65f023632bc46470c104b75f', 4025, 'base', true, 229.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4026, '65f023632bc46470c104b75f', 149, 'ELEC-SKU-149-SILVER', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4026, '65f023632bc46470c104b75f', 4026, 'base', true, 839.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4027, '65f023632bc46470c104b75f', 149, 'ELEC-SKU-149-MIDNIGHT', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4027, '65f023632bc46470c104b75f', 4027, 'base', true, 879.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4028, '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-128GB-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4028, '65f023632bc46470c104b75f', 4028, 'base', true, 1784.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4029, '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-256GB-GRAPHITE', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4029, '65f023632bc46470c104b75f', 4029, 'base', true, 1869.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4030, '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-256GB-SILVER', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4030, '65f023632bc46470c104b75f', 4030, 'base', true, 1954.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4031, '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-512GB-GRAPHITE', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4031, '65f023632bc46470c104b75f', 4031, 'base', true, 2039.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4032, '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-512GB-SILVER', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4032, '65f023632bc46470c104b75f', 4032, 'base', true, 2124.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4033, '65f023632bc46470c104b75f', 152, 'ELEC-SKU-152-256GB', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4033, '65f023632bc46470c104b75f', 4033, 'base', true, 187.95)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4034, '65f023632bc46470c104b75f', 152, 'ELEC-SKU-152-512GB', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4034, '65f023632bc46470c104b75f', 4034, 'base', true, 196.90)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4035, '65f023632bc46470c104b75f', 152, 'ELEC-SKU-152-1TB', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4035, '65f023632bc46470c104b75f', 4035, 'base', true, 205.85)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4036, '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-128GB-SILVER', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4036, '65f023632bc46470c104b75f', 4036, 'base', true, 1574.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4037, '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-128GB-MIDNIGHT', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4037, '65f023632bc46470c104b75f', 4037, 'base', true, 1649.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4038, '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-256GB-GRAPHITE', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4038, '65f023632bc46470c104b75f', 4038, 'base', true, 1724.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4039, '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-256GB-SILVER', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4039, '65f023632bc46470c104b75f', 4039, 'base', true, 1799.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4040, '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-256GB-MIDNIGHT', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4040, '65f023632bc46470c104b75f', 4040, 'base', true, 1874.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4041, '65f023632bc46470c104b75f', 154, 'ELEC-SKU-154-256GB', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4041, '65f023632bc46470c104b75f', 4041, 'base', true, 314.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4042, '65f023632bc46470c104b75f', 154, 'ELEC-SKU-154-512GB', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4042, '65f023632bc46470c104b75f', 4042, 'base', true, 329.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4043, '65f023632bc46470c104b75f', 155, 'ELEC-SKU-155-128GB-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4043, '65f023632bc46470c104b75f', 4043, 'base', true, 1049.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4044, '65f023632bc46470c104b75f', 155, 'ELEC-SKU-155-256GB-GRAPHITE', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4044, '65f023632bc46470c104b75f', 4044, 'base', true, 1099.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4045, '65f023632bc46470c104b75f', 155, 'ELEC-SKU-155-256GB-SILVER', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4045, '65f023632bc46470c104b75f', 4045, 'base', true, 1149.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4046, '65f023632bc46470c104b75f', 157, 'ELEC-SKU-157-SILVER', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4046, '65f023632bc46470c104b75f', 4046, 'base', true, 1574.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4047, '65f023632bc46470c104b75f', 157, 'ELEC-SKU-157-MIDNIGHT', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4047, '65f023632bc46470c104b75f', 4047, 'base', true, 1649.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4048, '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-128GB-SILVER', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4048, '65f023632bc46470c104b75f', 4048, 'base', true, 1678.95)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4049, '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-256GB-GRAPHITE', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4049, '65f023632bc46470c104b75f', 4049, 'base', true, 1758.90)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4050, '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-256GB-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4050, '65f023632bc46470c104b75f', 4050, 'base', true, 1838.85)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4051, '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-512GB-GRAPHITE', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4051, '65f023632bc46470c104b75f', 4051, 'base', true, 1918.80)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4052, '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-512GB-SILVER', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4052, '65f023632bc46470c104b75f', 4052, 'base', true, 1998.75)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4053, '65f023632bc46470c104b75f', 159, 'ELEC-SKU-159-256GB', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4053, '65f023632bc46470c104b75f', 4053, 'base', true, 1522.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4054, '65f023632bc46470c104b75f', 159, 'ELEC-SKU-159-512GB', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4054, '65f023632bc46470c104b75f', 4054, 'base', true, 1594.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4055, '65f023632bc46470c104b75f', 159, 'ELEC-SKU-159-1TB', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4055, '65f023632bc46470c104b75f', 4055, 'base', true, 1667.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4056, '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-128GB-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4056, '65f023632bc46470c104b75f', 4056, 'base', true, 1049.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4057, '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-128GB-MIDNIGHT', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4057, '65f023632bc46470c104b75f', 4057, 'base', true, 1099.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4058, '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-256GB-GRAPHITE', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4058, '65f023632bc46470c104b75f', 4058, 'base', true, 1149.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4059, '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-256GB-SILVER', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4059, '65f023632bc46470c104b75f', 4059, 'base', true, 1199.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4060, '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-256GB-MIDNIGHT', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4060, '65f023632bc46470c104b75f', 4060, 'base', true, 1249.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4061, '65f023632bc46470c104b75f', 162, 'ELEC-SKU-162-256GB', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4061, '65f023632bc46470c104b75f', 4061, 'base', true, 944.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4062, '65f023632bc46470c104b75f', 162, 'ELEC-SKU-162-512GB', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4062, '65f023632bc46470c104b75f', 4062, 'base', true, 989.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4063, '65f023632bc46470c104b75f', 163, 'ELEC-SKU-163-128GB-SILVER', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4063, '65f023632bc46470c104b75f', 4063, 'base', true, 892.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4064, '65f023632bc46470c104b75f', 163, 'ELEC-SKU-163-256GB-GRAPHITE', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4064, '65f023632bc46470c104b75f', 4064, 'base', true, 934.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4065, '65f023632bc46470c104b75f', 163, 'ELEC-SKU-163-256GB-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4065, '65f023632bc46470c104b75f', 4065, 'base', true, 977.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4066, '65f023632bc46470c104b75f', 164, 'ELEC-SKU-164-SILVER', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4066, '65f023632bc46470c104b75f', 4066, 'base', true, 367.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4067, '65f023632bc46470c104b75f', 164, 'ELEC-SKU-164-MIDNIGHT', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4067, '65f023632bc46470c104b75f', 4067, 'base', true, 384.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4068, '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-128GB-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4068, '65f023632bc46470c104b75f', 4068, 'base', true, 629.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4069, '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-256GB-GRAPHITE', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4069, '65f023632bc46470c104b75f', 4069, 'base', true, 659.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4070, '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-256GB-SILVER', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4070, '65f023632bc46470c104b75f', 4070, 'base', true, 689.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4071, '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-512GB-GRAPHITE', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4071, '65f023632bc46470c104b75f', 4071, 'base', true, 719.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4072, '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-512GB-SILVER', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4072, '65f023632bc46470c104b75f', 4072, 'base', true, 749.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4073, '65f023632bc46470c104b75f', 167, 'ELEC-SKU-167-256GB', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4073, '65f023632bc46470c104b75f', 4073, 'base', true, 682.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4074, '65f023632bc46470c104b75f', 167, 'ELEC-SKU-167-512GB', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4074, '65f023632bc46470c104b75f', 4074, 'base', true, 714.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4075, '65f023632bc46470c104b75f', 167, 'ELEC-SKU-167-1TB', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4075, '65f023632bc46470c104b75f', 4075, 'base', true, 747.49)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4076, '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-128GB-SILVER', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4076, '65f023632bc46470c104b75f', 4076, 'base', true, 314.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4077, '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-128GB-MIDNIGHT', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4077, '65f023632bc46470c104b75f', 4077, 'base', true, 329.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4078, '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-256GB-GRAPHITE', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4078, '65f023632bc46470c104b75f', 4078, 'base', true, 344.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4079, '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-256GB-SILVER', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4079, '65f023632bc46470c104b75f', 4079, 'base', true, 359.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4080, '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-256GB-MIDNIGHT', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4080, '65f023632bc46470c104b75f', 4080, 'base', true, 374.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4081, '65f023632bc46470c104b75f', 169, 'ELEC-SKU-169-256GB', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4081, '65f023632bc46470c104b75f', 4081, 'base', true, 418.95)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4082, '65f023632bc46470c104b75f', 169, 'ELEC-SKU-169-512GB', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4082, '65f023632bc46470c104b75f', 4082, 'base', true, 438.90)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4083, '65f023632bc46470c104b75f', 170, 'ELEC-SKU-170-128GB-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4083, '65f023632bc46470c104b75f', 4083, 'base', true, 839.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4084, '65f023632bc46470c104b75f', 170, 'ELEC-SKU-170-256GB-GRAPHITE', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4084, '65f023632bc46470c104b75f', 4084, 'base', true, 879.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4085, '65f023632bc46470c104b75f', 170, 'ELEC-SKU-170-256GB-SILVER', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4085, '65f023632bc46470c104b75f', 4085, 'base', true, 919.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4086, '65f023632bc46470c104b75f', 172, 'ELEC-SKU-172-SILVER', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4086, '65f023632bc46470c104b75f', 4086, 'base', true, 1889.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4087, '65f023632bc46470c104b75f', 172, 'ELEC-SKU-172-MIDNIGHT', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4087, '65f023632bc46470c104b75f', 4087, 'base', true, 1979.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4088, '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-128GB-SILVER', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4088, '65f023632bc46470c104b75f', 4088, 'base', true, 1364.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4089, '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-256GB-GRAPHITE', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4089, '65f023632bc46470c104b75f', 4089, 'base', true, 1429.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4090, '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-256GB-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4090, '65f023632bc46470c104b75f', 4090, 'base', true, 1494.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4091, '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-512GB-GRAPHITE', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4091, '65f023632bc46470c104b75f', 4091, 'base', true, 1559.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4092, '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-512GB-SILVER', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4092, '65f023632bc46470c104b75f', 4092, 'base', true, 1624.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4093, '65f023632bc46470c104b75f', 174, 'ELEC-SKU-174-256GB', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4093, '65f023632bc46470c104b75f', 4093, 'base', true, 628.95)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4094, '65f023632bc46470c104b75f', 174, 'ELEC-SKU-174-512GB', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4094, '65f023632bc46470c104b75f', 4094, 'base', true, 658.90)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4095, '65f023632bc46470c104b75f', 174, 'ELEC-SKU-174-1TB', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4095, '65f023632bc46470c104b75f', 4095, 'base', true, 688.85)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4096, '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-128GB-SILVER', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4096, '65f023632bc46470c104b75f', 4096, 'base', true, 1994.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4097, '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-128GB-MIDNIGHT', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4097, '65f023632bc46470c104b75f', 4097, 'base', true, 2089.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4098, '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-256GB-GRAPHITE', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4098, '65f023632bc46470c104b75f', 4098, 'base', true, 2184.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4099, '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-256GB-SILVER', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4099, '65f023632bc46470c104b75f', 4099, 'base', true, 2279.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4100, '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-256GB-MIDNIGHT', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4100, '65f023632bc46470c104b75f', 4100, 'base', true, 2374.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4101, '65f023632bc46470c104b75f', 177, 'ELEC-SKU-177-256GB', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4101, '65f023632bc46470c104b75f', 4101, 'base', true, 314.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4102, '65f023632bc46470c104b75f', 177, 'ELEC-SKU-177-512GB', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4102, '65f023632bc46470c104b75f', 4102, 'base', true, 329.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4103, '65f023632bc46470c104b75f', 178, 'ELEC-SKU-178-128GB-SILVER', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4103, '65f023632bc46470c104b75f', 4103, 'base', true, 209.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4104, '65f023632bc46470c104b75f', 178, 'ELEC-SKU-178-256GB-GRAPHITE', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4104, '65f023632bc46470c104b75f', 4104, 'base', true, 219.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4105, '65f023632bc46470c104b75f', 178, 'ELEC-SKU-178-256GB-SILVER', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4105, '65f023632bc46470c104b75f', 4105, 'base', true, 229.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4106, '65f023632bc46470c104b75f', 179, 'ELEC-SKU-179-SILVER', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4106, '65f023632bc46470c104b75f', 4106, 'base', true, 1679.99)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (4107, '65f023632bc46470c104b75f', 179, 'ELEC-SKU-179-MIDNIGHT', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (4107, '65f023632bc46470c104b75f', 4107, 'base', true, 1759.99)
on conflict (product_price_id) do nothing;

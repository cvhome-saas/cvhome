-- Demo option vocabulary and multi-variant products for the fashion store.
--
-- Two options (color, size). Product 1 (Nike Running Shoes) varies by size only; product 2 (Zara dress) by
-- color x size, deliberately missing the red/L combination so the storefront's "impossible combination"
-- greying is visible in QA. The products' original skus stay on their default variants, so the existing
-- inventory seed rows keep pricing them; the extra combination skus get inventory rows in the inventory
-- service's store seed.

-- ------------------------------------------------------------------------------------------ option vocabulary
INSERT INTO catalog.product_option (product_option_id, date_created, date_modified, code, sort_order, store_merchant_id)
VALUES (1, NOW(), NOW(), 'color', 0, '65f023632bc46470c104b76f')
on conflict (product_option_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (1, NOW(), NOW(), 'Color', 'en', 1) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (2, NOW(), NOW(), 'اللون', 'ar', 1) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option (product_option_id, date_created, date_modified, code, sort_order, store_merchant_id)
VALUES (2, NOW(), NOW(), 'size', 1, '65f023632bc46470c104b76f')
on conflict (product_option_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (3, NOW(), NOW(), 'Size', 'en', 2) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (4, NOW(), NOW(), 'المقاس', 'ar', 2) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (1, NOW(), NOW(), 'red', 0, 1) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (1, NOW(), NOW(), 'Red', 'en', 1) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (2, NOW(), NOW(), 'أحمر', 'ar', 1) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (2, NOW(), NOW(), 'blue', 1, 1) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (3, NOW(), NOW(), 'Blue', 'en', 2) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (4, NOW(), NOW(), 'أزرق', 'ar', 2) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (3, NOW(), NOW(), 'm', 0, 2) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (5, NOW(), NOW(), 'M', 'en', 3) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (6, NOW(), NOW(), 'وسط', 'ar', 3) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (4, NOW(), NOW(), 'l', 1, 2) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (7, NOW(), NOW(), 'L', 'en', 4) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (8, NOW(), NOW(), 'كبير', 'ar', 4) on conflict (description_id) do nothing;

-- ---------------------------------------------------------------- product 1: Nike Running Shoes, size only
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (1, 2, 0) on conflict (product_id, product_option_id) do nothing;

-- the default-variant seed created variant 1 with the product's original sku; promote it to the M combination
UPDATE catalog.product_variant SET option_signature = '3' WHERE product_variant_id = 1;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (501, NOW(), NOW(), '65f023632bc46470c104b76f', 1, 'SKU-NK-RUN-001-L', 1, false, '4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (501, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;

-- ------------------------------------------------------------------- product 2: Zara dress, color x size
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (2, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (2, 2, 1) on conflict (product_id, product_option_id) do nothing;

-- variant 2 keeps the original sku as the red/M default combination
UPDATE catalog.product_variant SET option_signature = '1-3' WHERE product_variant_id = 2;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (502, NOW(), NOW(), '65f023632bc46470c104b76f', 2, 'SKU-ZR-CL-DRS02-BL-M', 1, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (502, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (502, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (503, NOW(), NOW(), '65f023632bc46470c104b76f', 2, 'SKU-ZR-CL-DRS02-BL-L', 2, false, '2-4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (503, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (503, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;

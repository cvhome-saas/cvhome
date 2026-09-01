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

-- ---------------------------------------------------------------------------------------------------------
-- Generated bulk: the rest of this store's catalogue sells by variants too.
--
-- The curated products above stay exactly as they are (QA and the integration tests pin their skus and
-- their deliberately-missing combinations). Everything below gives 36 of the store's 45 products
-- (80%) at least two variants, leaving 9 deliberately optionless as the control case. Matrix shapes
-- rotate — one axis, two axes, two to six combinations — so listings, facets and the console matrix all
-- meet realistic shapes, and the stores carry enough rows to be worth measuring.
--
-- Regenerated by extra/scripts/generate-demo-variants.py; edit that rather than these lines.
-- ---------------------------------------------------------------------------------------------------------

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (20, NOW(), NOW(), 'black', 2, 1) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (39, NOW(), NOW(), 'Black', 'en', 20) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (40, NOW(), NOW(), 'أسود', 'ar', 20) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (21, NOW(), NOW(), 'white', 3, 1) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (41, NOW(), NOW(), 'White', 'en', 21) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (42, NOW(), NOW(), 'أبيض', 'ar', 21) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (22, NOW(), NOW(), 'green', 4, 1) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (43, NOW(), NOW(), 'Green', 'en', 22) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (44, NOW(), NOW(), 'أخضر', 'ar', 22) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (23, NOW(), NOW(), 's', 0, 2) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (45, NOW(), NOW(), 'S', 'en', 23) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (46, NOW(), NOW(), 'صغير', 'ar', 23) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (24, NOW(), NOW(), 'xl', 3, 2) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (47, NOW(), NOW(), 'XL', 'en', 24) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (48, NOW(), NOW(), 'كبير جداً', 'ar', 24) on conflict (description_id) do nothing;

UPDATE catalog.product_option_value SET sort_order = 1 WHERE product_option_value_id = 3;
UPDATE catalog.product_option_value SET sort_order = 2 WHERE product_option_value_id = 4;

-- product 5 (SKU-GU-BG-MAR05): color(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (5, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 5;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (5, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1001, NOW(), NOW(), '65f023632bc46470c104b76f', 5, 'SKU-GU-BG-MAR05-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1001, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;

-- product 6 (SKU-CH-AC-SUN06): color(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (6, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 6;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (6, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1002, NOW(), NOW(), '65f023632bc46470c104b76f', 6, 'SKU-CH-AC-SUN06-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1002, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1003, NOW(), NOW(), '65f023632bc46470c104b76f', 6, 'SKU-CH-AC-SUN06-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1003, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;

-- product 7 (SKU-NK-CL-KHD07): size(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (7, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 7;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (7, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1004, NOW(), NOW(), '65f023632bc46470c104b76f', 7, 'SKU-NK-CL-KHD07-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1004, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1005, NOW(), NOW(), '65f023632bc46470c104b76f', 7, 'SKU-NK-CL-KHD07-L', 2, false, '4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1005, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;

-- product 8 (SKU-ZR-SH-SNK08): color(2) x size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (8, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (8, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 8;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (8, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (8, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1006, NOW(), NOW(), '65f023632bc46470c104b76f', 8, 'SKU-ZR-SH-SNK08-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1006, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1006, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1007, NOW(), NOW(), '65f023632bc46470c104b76f', 8, 'SKU-ZR-SH-SNK08-BLUE-S', 2, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1007, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1007, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1008, NOW(), NOW(), '65f023632bc46470c104b76f', 8, 'SKU-ZR-SH-SNK08-BLUE-M', 3, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1008, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1008, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

-- product 9 (SKU-AD-BG-BPK09): color(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (9, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 9;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (9, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1009, NOW(), NOW(), '65f023632bc46470c104b76f', 9, 'SKU-AD-BG-BPK09-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1009, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1010, NOW(), NOW(), '65f023632bc46470c104b76f', 9, 'SKU-AD-BG-BPK09-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1010, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1011, NOW(), NOW(), '65f023632bc46470c104b76f', 9, 'SKU-AD-BG-BPK09-WHITE', 3, false, '21')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1011, 1, 21) on conflict (product_variant_id, product_option_id) do nothing;

-- product 10 (SKU-HM-AC-BLT10): color(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (10, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 10;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (10, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1012, NOW(), NOW(), '65f023632bc46470c104b76f', 10, 'SKU-HM-AC-BLT10-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1012, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1013, NOW(), NOW(), '65f023632bc46470c104b76f', 10, 'SKU-HM-AC-BLT10-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1013, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;

-- product 12 (SKU-CH-AC-CRD12): color(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (12, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 12;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (12, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1014, NOW(), NOW(), '65f023632bc46470c104b76f', 12, 'SKU-CH-AC-CRD12-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1014, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;

-- product 13 (SKU-NK-CL-LEG13): size(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (13, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 13;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (13, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1015, NOW(), NOW(), '65f023632bc46470c104b76f', 13, 'SKU-NK-CL-LEG13-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1015, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1016, NOW(), NOW(), '65f023632bc46470c104b76f', 13, 'SKU-NK-CL-LEG13-L', 2, false, '4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1016, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1017, NOW(), NOW(), '65f023632bc46470c104b76f', 13, 'SKU-NK-CL-LEG13-XL', 3, false, '24')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1017, 2, 24) on conflict (product_variant_id, product_option_id) do nothing;

-- product 14 (SKU-ZR-CL-POL14): color(3) x size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (14, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (14, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 14;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (14, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (14, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1018, NOW(), NOW(), '65f023632bc46470c104b76f', 14, 'SKU-ZR-CL-POL14-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1018, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1018, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1019, NOW(), NOW(), '65f023632bc46470c104b76f', 14, 'SKU-ZR-CL-POL14-BLUE-S', 2, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1019, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1019, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1020, NOW(), NOW(), '65f023632bc46470c104b76f', 14, 'SKU-ZR-CL-POL14-BLUE-M', 3, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1020, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1020, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1021, NOW(), NOW(), '65f023632bc46470c104b76f', 14, 'SKU-ZR-CL-POL14-BLACK-S', 4, false, '20-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1021, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1021, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1022, NOW(), NOW(), '65f023632bc46470c104b76f', 14, 'SKU-ZR-CL-POL14-BLACK-M', 5, false, '3-20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1022, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1022, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

-- product 15 (SKU-AD-SH-SND15): color(2) x size(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (15, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (15, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 15;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (15, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (15, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1023, NOW(), NOW(), '65f023632bc46470c104b76f', 15, 'SKU-AD-SH-SND15-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1023, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1023, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1024, NOW(), NOW(), '65f023632bc46470c104b76f', 15, 'SKU-AD-SH-SND15-RED-L', 2, false, '1-4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1024, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1024, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1025, NOW(), NOW(), '65f023632bc46470c104b76f', 15, 'SKU-AD-SH-SND15-BLUE-S', 3, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1025, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1025, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1026, NOW(), NOW(), '65f023632bc46470c104b76f', 15, 'SKU-AD-SH-SND15-BLUE-M', 4, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1026, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1026, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1027, NOW(), NOW(), '65f023632bc46470c104b76f', 15, 'SKU-AD-SH-SND15-BLUE-L', 5, false, '2-4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1027, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1027, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;

-- product 17 (SKU-GU-AC-WAL17): color(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (17, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 17;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (17, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1028, NOW(), NOW(), '65f023632bc46470c104b76f', 17, 'SKU-GU-AC-WAL17-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1028, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1029, NOW(), NOW(), '65f023632bc46470c104b76f', 17, 'SKU-GU-AC-WAL17-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1029, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;

-- product 18 (SKU-CH-AC-BRH18): color(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (18, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 18;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (18, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1030, NOW(), NOW(), '65f023632bc46470c104b76f', 18, 'SKU-CH-AC-BRH18-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1030, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1031, NOW(), NOW(), '65f023632bc46470c104b76f', 18, 'SKU-CH-AC-BRH18-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1031, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1032, NOW(), NOW(), '65f023632bc46470c104b76f', 18, 'SKU-CH-AC-BRH18-WHITE', 3, false, '21')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1032, 1, 21) on conflict (product_variant_id, product_option_id) do nothing;

-- product 19 (SKU-NK-CL-BBS19): size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (19, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 19;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (19, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1033, NOW(), NOW(), '65f023632bc46470c104b76f', 19, 'SKU-NK-CL-BBS19-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1033, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

-- product 20 (SKU-ZR-SH-SND20): size(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (20, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 20;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (20, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1034, NOW(), NOW(), '65f023632bc46470c104b76f', 20, 'SKU-ZR-SH-SND20-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1034, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1035, NOW(), NOW(), '65f023632bc46470c104b76f', 20, 'SKU-ZR-SH-SND20-L', 2, false, '4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1035, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;

-- product 22 (SKU-HM-CL-KTP22): color(2) x size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (22, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (22, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 22;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (22, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (22, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1036, NOW(), NOW(), '65f023632bc46470c104b76f', 22, 'SKU-HM-CL-KTP22-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1036, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1036, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1037, NOW(), NOW(), '65f023632bc46470c104b76f', 22, 'SKU-HM-CL-KTP22-BLUE-S', 2, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1037, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1037, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1038, NOW(), NOW(), '65f023632bc46470c104b76f', 22, 'SKU-HM-CL-KTP22-BLUE-M', 3, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1038, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1038, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

-- product 23 (SKU-GU-SH-SNK23): size(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (23, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 23;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (23, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1039, NOW(), NOW(), '65f023632bc46470c104b76f', 23, 'SKU-GU-SH-SNK23-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1039, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1040, NOW(), NOW(), '65f023632bc46470c104b76f', 23, 'SKU-GU-SH-SNK23-L', 2, false, '4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1040, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1041, NOW(), NOW(), '65f023632bc46470c104b76f', 23, 'SKU-GU-SH-SNK23-XL', 3, false, '24')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1041, 2, 24) on conflict (product_variant_id, product_option_id) do nothing;

-- product 24 (SKU-CH-SH-BAL24): color(3) x size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (24, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (24, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 24;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (24, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (24, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1042, NOW(), NOW(), '65f023632bc46470c104b76f', 24, 'SKU-CH-SH-BAL24-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1042, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1042, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1043, NOW(), NOW(), '65f023632bc46470c104b76f', 24, 'SKU-CH-SH-BAL24-BLUE-S', 2, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1043, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1043, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1044, NOW(), NOW(), '65f023632bc46470c104b76f', 24, 'SKU-CH-SH-BAL24-BLUE-M', 3, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1044, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1044, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1045, NOW(), NOW(), '65f023632bc46470c104b76f', 24, 'SKU-CH-SH-BAL24-BLACK-S', 4, false, '20-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1045, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1045, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1046, NOW(), NOW(), '65f023632bc46470c104b76f', 24, 'SKU-CH-SH-BAL24-BLACK-M', 5, false, '3-20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1046, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1046, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

-- product 25 (SKU-NK-CL-TNK25): color(2) x size(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (25, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (25, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 25;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (25, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (25, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1047, NOW(), NOW(), '65f023632bc46470c104b76f', 25, 'SKU-NK-CL-TNK25-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1047, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1047, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1048, NOW(), NOW(), '65f023632bc46470c104b76f', 25, 'SKU-NK-CL-TNK25-RED-L', 2, false, '1-4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1048, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1048, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1049, NOW(), NOW(), '65f023632bc46470c104b76f', 25, 'SKU-NK-CL-TNK25-BLUE-S', 3, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1049, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1049, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1050, NOW(), NOW(), '65f023632bc46470c104b76f', 25, 'SKU-NK-CL-TNK25-BLUE-M', 4, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1050, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1050, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1051, NOW(), NOW(), '65f023632bc46470c104b76f', 25, 'SKU-NK-CL-TNK25-BLUE-L', 5, false, '2-4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1051, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1051, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;

-- product 27 (SKU-AD-CL-KTS27): size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (27, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 27;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (27, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1052, NOW(), NOW(), '65f023632bc46470c104b76f', 27, 'SKU-AD-CL-KTS27-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1052, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

-- product 28 (SKU-HM-AC-SCF28): color(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (28, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 28;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (28, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1053, NOW(), NOW(), '65f023632bc46470c104b76f', 28, 'SKU-HM-AC-SCF28-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1053, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1054, NOW(), NOW(), '65f023632bc46470c104b76f', 28, 'SKU-HM-AC-SCF28-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1054, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;

-- product 29 (SKU-GU-BG-BBG29): color(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (29, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 29;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (29, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1055, NOW(), NOW(), '65f023632bc46470c104b76f', 29, 'SKU-GU-BG-BBG29-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1055, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;

-- product 30 (SKU-CH-SH-SNK30): size(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (30, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 30;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (30, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1056, NOW(), NOW(), '65f023632bc46470c104b76f', 30, 'SKU-CH-SH-SNK30-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1056, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1057, NOW(), NOW(), '65f023632bc46470c104b76f', 30, 'SKU-CH-SH-SNK30-L', 2, false, '4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1057, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;

-- product 32 (SKU-ZR-CL-JNS32): color(2) x size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (32, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (32, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 32;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (32, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (32, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1058, NOW(), NOW(), '65f023632bc46470c104b76f', 32, 'SKU-ZR-CL-JNS32-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1058, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1058, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1059, NOW(), NOW(), '65f023632bc46470c104b76f', 32, 'SKU-ZR-CL-JNS32-BLUE-S', 2, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1059, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1059, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1060, NOW(), NOW(), '65f023632bc46470c104b76f', 32, 'SKU-ZR-CL-JNS32-BLUE-M', 3, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1060, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1060, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

-- product 33 (SKU-AD-AC-CAP33): color(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (33, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 33;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (33, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1061, NOW(), NOW(), '65f023632bc46470c104b76f', 33, 'SKU-AD-AC-CAP33-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1061, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1062, NOW(), NOW(), '65f023632bc46470c104b76f', 33, 'SKU-AD-AC-CAP33-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1062, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;

-- product 34 (SKU-HM-CL-KRJ34): size(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (34, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 34;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (34, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1063, NOW(), NOW(), '65f023632bc46470c104b76f', 34, 'SKU-HM-CL-KRJ34-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1063, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1064, NOW(), NOW(), '65f023632bc46470c104b76f', 34, 'SKU-HM-CL-KRJ34-L', 2, false, '4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1064, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1065, NOW(), NOW(), '65f023632bc46470c104b76f', 34, 'SKU-HM-CL-KRJ34-XL', 3, false, '24')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1065, 2, 24) on conflict (product_variant_id, product_option_id) do nothing;

-- product 35 (SKU-GU-AC-SCF35): color(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (35, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 35;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (35, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1066, NOW(), NOW(), '65f023632bc46470c104b76f', 35, 'SKU-GU-AC-SCF35-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1066, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1067, NOW(), NOW(), '65f023632bc46470c104b76f', 35, 'SKU-GU-AC-SCF35-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1067, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1068, NOW(), NOW(), '65f023632bc46470c104b76f', 35, 'SKU-GU-AC-SCF35-WHITE', 3, false, '21')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1068, 1, 21) on conflict (product_variant_id, product_option_id) do nothing;

-- product 37 (SKU-NK-CL-WRS37): color(3) x size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (37, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (37, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 37;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (37, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (37, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1069, NOW(), NOW(), '65f023632bc46470c104b76f', 37, 'SKU-NK-CL-WRS37-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1069, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1069, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1070, NOW(), NOW(), '65f023632bc46470c104b76f', 37, 'SKU-NK-CL-WRS37-BLUE-S', 2, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1070, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1070, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1071, NOW(), NOW(), '65f023632bc46470c104b76f', 37, 'SKU-NK-CL-WRS37-BLUE-M', 3, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1071, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1071, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1072, NOW(), NOW(), '65f023632bc46470c104b76f', 37, 'SKU-NK-CL-WRS37-BLACK-S', 4, false, '20-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1072, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1072, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1073, NOW(), NOW(), '65f023632bc46470c104b76f', 37, 'SKU-NK-CL-WRS37-BLACK-M', 5, false, '3-20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1073, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1073, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

-- product 38 (SKU-ZR-CL-KSW38): color(2) x size(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (38, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (38, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 38;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (38, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (38, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1074, NOW(), NOW(), '65f023632bc46470c104b76f', 38, 'SKU-ZR-CL-KSW38-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1074, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1074, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1075, NOW(), NOW(), '65f023632bc46470c104b76f', 38, 'SKU-ZR-CL-KSW38-RED-L', 2, false, '1-4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1075, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1075, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1076, NOW(), NOW(), '65f023632bc46470c104b76f', 38, 'SKU-ZR-CL-KSW38-BLUE-S', 3, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1076, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1076, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1077, NOW(), NOW(), '65f023632bc46470c104b76f', 38, 'SKU-ZR-CL-KSW38-BLUE-M', 4, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1077, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1077, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1078, NOW(), NOW(), '65f023632bc46470c104b76f', 38, 'SKU-ZR-CL-KSW38-BLUE-L', 5, false, '2-4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1078, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1078, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;

-- product 39 (SKU-AD-AC-SCK39): color(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (39, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 39;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (39, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1079, NOW(), NOW(), '65f023632bc46470c104b76f', 39, 'SKU-AD-AC-SCK39-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1079, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1080, NOW(), NOW(), '65f023632bc46470c104b76f', 39, 'SKU-AD-AC-SCK39-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1080, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;

-- product 40 (SKU-HM-CL-SWM40): size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (40, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 40;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (40, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1081, NOW(), NOW(), '65f023632bc46470c104b76f', 40, 'SKU-HM-CL-SWM40-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1081, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

-- product 42 (SKU-CH-AC-EAR42): color(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (42, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 42;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (42, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1082, NOW(), NOW(), '65f023632bc46470c104b76f', 42, 'SKU-CH-AC-EAR42-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1082, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;

-- product 43 (SKU-NK-BG-DUF43): color(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (43, 1, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1' WHERE product_variant_id = 43;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (43, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1083, NOW(), NOW(), '65f023632bc46470c104b76f', 43, 'SKU-NK-BG-DUF43-BLUE', 1, false, '2')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1083, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1084, NOW(), NOW(), '65f023632bc46470c104b76f', 43, 'SKU-NK-BG-DUF43-BLACK', 2, false, '20')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1084, 1, 20) on conflict (product_variant_id, product_option_id) do nothing;

-- product 44 (SKU-ZR-CL-BLS44): size(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (44, 2, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '23' WHERE product_variant_id = 44;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (44, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1085, NOW(), NOW(), '65f023632bc46470c104b76f', 44, 'SKU-ZR-CL-BLS44-M', 1, false, '3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1085, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1086, NOW(), NOW(), '65f023632bc46470c104b76f', 44, 'SKU-ZR-CL-BLS44-L', 2, false, '4')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1086, 2, 4) on conflict (product_variant_id, product_option_id) do nothing;

-- product 45 (SKU-AD-SH-SLD45): color(2) x size(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (45, 1, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (45, 2, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '1-23' WHERE product_variant_id = 45;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (45, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (45, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1087, NOW(), NOW(), '65f023632bc46470c104b76f', 45, 'SKU-AD-SH-SLD45-RED-M', 1, false, '1-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1087, 1, 1) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1087, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1088, NOW(), NOW(), '65f023632bc46470c104b76f', 45, 'SKU-AD-SH-SLD45-BLUE-S', 2, false, '2-23')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1088, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1088, 2, 23) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (1089, NOW(), NOW(), '65f023632bc46470c104b76f', 45, 'SKU-AD-SH-SLD45-BLUE-M', 3, false, '2-3')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1089, 1, 2) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (1089, 2, 3) on conflict (product_variant_id, product_option_id) do nothing;

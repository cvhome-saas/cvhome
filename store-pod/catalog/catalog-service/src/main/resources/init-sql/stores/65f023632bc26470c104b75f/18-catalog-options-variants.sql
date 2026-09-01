-- Demo option vocabulary and multi-variant products for the cars store.
--
-- Car store: paint and trim. The showroom model carries both axes, one model is paint-only
-- and one trim-only, so every matrix shape appears here too.
--
-- Each product's original sku stays on its default variant, which is promoted to the first
-- combination, so the existing inventory seed keeps pricing it; the extra combination skus get
-- their own rows in the inventory service's store seed.

-- ------------------------------------------------------------------------------------------ option vocabulary
INSERT INTO catalog.product_option (product_option_id, date_created, date_modified, code, sort_order, store_merchant_id)
VALUES (5, NOW(), NOW(), 'paint', 0, '65f023632bc26470c104b75f')
on conflict (product_option_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (13, NOW(), NOW(), 'اللون الخارجي', 'ar', 5) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (14, NOW(), NOW(), 'Peinture', 'fr', 5) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (10, NOW(), NOW(), 'white', 0, 5)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (19, NOW(), NOW(), 'أبيض', 'ar', 10) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (20, NOW(), NOW(), 'Blanc', 'fr', 10) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (11, NOW(), NOW(), 'black', 1, 5)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (21, NOW(), NOW(), 'أسود', 'ar', 11) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (22, NOW(), NOW(), 'Noir', 'fr', 11) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (12, NOW(), NOW(), 'silver', 2, 5)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (23, NOW(), NOW(), 'فضي', 'ar', 12) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (24, NOW(), NOW(), 'Argent', 'fr', 12) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option (product_option_id, date_created, date_modified, code, sort_order, store_merchant_id)
VALUES (6, NOW(), NOW(), 'trim', 1, '65f023632bc26470c104b75f')
on conflict (product_option_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (15, NOW(), NOW(), 'فئة التجهيز', 'ar', 6) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (16, NOW(), NOW(), 'Finition', 'fr', 6) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (13, NOW(), NOW(), 'standard', 0, 6)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (25, NOW(), NOW(), 'قياسي', 'ar', 13) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (26, NOW(), NOW(), 'Standard', 'fr', 13) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (14, NOW(), NOW(), 'premium', 1, 6)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (27, NOW(), NOW(), 'فاخر', 'ar', 14) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (28, NOW(), NOW(), 'Premium', 'fr', 14) on conflict (description_id) do nothing;

-- -------------------- product 91: paint x trim, the full six
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (91, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (91, 6, 1) on conflict (product_id, product_option_id) do nothing;

-- the default variant keeps the original sku as the WHITE-STANDARD combination
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 91;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (91, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (91, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (520, NOW(), NOW(), '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (520, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (520, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (521, NOW(), NOW(), '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (521, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (521, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (522, NOW(), NOW(), '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (522, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (522, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (523, NOW(), NOW(), '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-SILVER-STANDARD', 4, false, '12-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (523, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (523, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (524, NOW(), NOW(), '65f023632bc26470c104b75f', 91, 'CAR-SKU-91-SILVER-PREMIUM', 5, false, '12-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (524, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (524, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- -------------------- product 92: paint only
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (92, 5, 0) on conflict (product_id, product_option_id) do nothing;

-- the default variant keeps the original sku as the WHITE combination
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 92;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (92, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (525, NOW(), NOW(), '65f023632bc26470c104b75f', 92, 'CAR-SKU-92-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (525, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (526, NOW(), NOW(), '65f023632bc26470c104b75f', 92, 'CAR-SKU-92-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (526, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;

-- -------------------- product 93: trim only
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (93, 6, 0) on conflict (product_id, product_option_id) do nothing;

-- the default variant keeps the original sku as the STANDARD combination
UPDATE catalog.product_variant SET option_signature = '13' WHERE product_variant_id = 93;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (93, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (527, NOW(), NOW(), '65f023632bc26470c104b75f', 93, 'CAR-SKU-93-PREMIUM', 1, false, '14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (527, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

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
VALUES (29, NOW(), NOW(), 'blue', 3, 5) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (57, NOW(), NOW(), 'أزرق', 'ar', 29) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (58, NOW(), NOW(), 'Bleu', 'fr', 29) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (30, NOW(), NOW(), 'red', 4, 5) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (59, NOW(), NOW(), 'أحمر', 'ar', 30) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (60, NOW(), NOW(), 'Rouge', 'fr', 30) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (31, NOW(), NOW(), 'sport', 2, 6) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (61, NOW(), NOW(), 'رياضي', 'ar', 31) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (62, NOW(), NOW(), 'Sport', 'fr', 31) on conflict (description_id) do nothing;

-- product 94 (CAR-SKU-94): paint(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (94, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 94;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (94, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3001, NOW(), NOW(), '65f023632bc26470c104b75f', 94, 'CAR-SKU-94-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3001, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3002, NOW(), NOW(), '65f023632bc26470c104b75f', 94, 'CAR-SKU-94-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3002, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;

-- product 95 (CAR-SKU-95): paint(2) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (95, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (95, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 95;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (95, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (95, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3003, NOW(), NOW(), '65f023632bc26470c104b75f', 95, 'CAR-SKU-95-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3003, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3003, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3004, NOW(), NOW(), '65f023632bc26470c104b75f', 95, 'CAR-SKU-95-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3004, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3004, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3005, NOW(), NOW(), '65f023632bc26470c104b75f', 95, 'CAR-SKU-95-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3005, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3005, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 97 (CAR-SKU-97): trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (97, 6, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '13' WHERE product_variant_id = 97;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (97, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3006, NOW(), NOW(), '65f023632bc26470c104b75f', 97, 'CAR-SKU-97-PREMIUM', 1, false, '14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3006, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3007, NOW(), NOW(), '65f023632bc26470c104b75f', 97, 'CAR-SKU-97-SPORT', 2, false, '31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3007, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 98 (CAR-SKU-98): paint(3) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (98, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (98, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 98;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (98, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (98, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3008, NOW(), NOW(), '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3008, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3008, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3009, NOW(), NOW(), '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3009, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3009, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3010, NOW(), NOW(), '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3010, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3010, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3011, NOW(), NOW(), '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-SILVER-STANDARD', 4, false, '12-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3011, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3011, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3012, NOW(), NOW(), '65f023632bc26470c104b75f', 98, 'CAR-SKU-98-SILVER-PREMIUM', 5, false, '12-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3012, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3012, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 99 (CAR-SKU-99): paint(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (99, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 99;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (99, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3013, NOW(), NOW(), '65f023632bc26470c104b75f', 99, 'CAR-SKU-99-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3013, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3014, NOW(), NOW(), '65f023632bc26470c104b75f', 99, 'CAR-SKU-99-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3014, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3015, NOW(), NOW(), '65f023632bc26470c104b75f', 99, 'CAR-SKU-99-BLUE', 3, false, '29')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3015, 5, 29) on conflict (product_variant_id, product_option_id) do nothing;

-- product 100 (CAR-SKU-100): paint(2) x trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (100, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (100, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 100;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (100, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (100, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3016, NOW(), NOW(), '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3016, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3016, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3017, NOW(), NOW(), '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-WHITE-SPORT', 2, false, '10-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3017, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3017, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3018, NOW(), NOW(), '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-BLACK-STANDARD', 3, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3018, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3018, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3019, NOW(), NOW(), '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-BLACK-PREMIUM', 4, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3019, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3019, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3020, NOW(), NOW(), '65f023632bc26470c104b75f', 100, 'CAR-SKU-100-BLACK-SPORT', 5, false, '11-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3020, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3020, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 102 (CAR-SKU-102): paint(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (102, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 102;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (102, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3021, NOW(), NOW(), '65f023632bc26470c104b75f', 102, 'CAR-SKU-102-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3021, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3022, NOW(), NOW(), '65f023632bc26470c104b75f', 102, 'CAR-SKU-102-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3022, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;

-- product 103 (CAR-SKU-103): paint(2) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (103, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (103, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 103;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (103, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (103, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3023, NOW(), NOW(), '65f023632bc26470c104b75f', 103, 'CAR-SKU-103-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3023, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3023, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3024, NOW(), NOW(), '65f023632bc26470c104b75f', 103, 'CAR-SKU-103-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3024, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3024, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3025, NOW(), NOW(), '65f023632bc26470c104b75f', 103, 'CAR-SKU-103-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3025, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3025, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 104 (CAR-SKU-104): trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (104, 6, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '13' WHERE product_variant_id = 104;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (104, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3026, NOW(), NOW(), '65f023632bc26470c104b75f', 104, 'CAR-SKU-104-PREMIUM', 1, false, '14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3026, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3027, NOW(), NOW(), '65f023632bc26470c104b75f', 104, 'CAR-SKU-104-SPORT', 2, false, '31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3027, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 105 (CAR-SKU-105): paint(3) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (105, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (105, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 105;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (105, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (105, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3028, NOW(), NOW(), '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3028, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3028, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3029, NOW(), NOW(), '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3029, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3029, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3030, NOW(), NOW(), '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3030, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3030, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3031, NOW(), NOW(), '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-SILVER-STANDARD', 4, false, '12-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3031, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3031, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3032, NOW(), NOW(), '65f023632bc26470c104b75f', 105, 'CAR-SKU-105-SILVER-PREMIUM', 5, false, '12-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3032, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3032, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 107 (CAR-SKU-107): paint(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (107, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 107;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (107, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3033, NOW(), NOW(), '65f023632bc26470c104b75f', 107, 'CAR-SKU-107-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3033, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3034, NOW(), NOW(), '65f023632bc26470c104b75f', 107, 'CAR-SKU-107-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3034, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3035, NOW(), NOW(), '65f023632bc26470c104b75f', 107, 'CAR-SKU-107-BLUE', 3, false, '29')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3035, 5, 29) on conflict (product_variant_id, product_option_id) do nothing;

-- product 108 (CAR-SKU-108): paint(2) x trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (108, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (108, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 108;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (108, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (108, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3036, NOW(), NOW(), '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3036, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3036, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3037, NOW(), NOW(), '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-WHITE-SPORT', 2, false, '10-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3037, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3037, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3038, NOW(), NOW(), '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-BLACK-STANDARD', 3, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3038, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3038, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3039, NOW(), NOW(), '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-BLACK-PREMIUM', 4, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3039, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3039, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3040, NOW(), NOW(), '65f023632bc26470c104b75f', 108, 'CAR-SKU-108-BLACK-SPORT', 5, false, '11-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3040, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3040, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 109 (CAR-SKU-109): paint(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (109, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 109;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (109, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3041, NOW(), NOW(), '65f023632bc26470c104b75f', 109, 'CAR-SKU-109-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3041, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3042, NOW(), NOW(), '65f023632bc26470c104b75f', 109, 'CAR-SKU-109-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3042, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;

-- product 110 (CAR-SKU-110): paint(2) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (110, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (110, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 110;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (110, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (110, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3043, NOW(), NOW(), '65f023632bc26470c104b75f', 110, 'CAR-SKU-110-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3043, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3043, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3044, NOW(), NOW(), '65f023632bc26470c104b75f', 110, 'CAR-SKU-110-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3044, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3044, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3045, NOW(), NOW(), '65f023632bc26470c104b75f', 110, 'CAR-SKU-110-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3045, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3045, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 112 (CAR-SKU-112): trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (112, 6, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '13' WHERE product_variant_id = 112;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (112, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3046, NOW(), NOW(), '65f023632bc26470c104b75f', 112, 'CAR-SKU-112-PREMIUM', 1, false, '14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3046, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3047, NOW(), NOW(), '65f023632bc26470c104b75f', 112, 'CAR-SKU-112-SPORT', 2, false, '31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3047, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 113 (CAR-SKU-113): paint(3) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (113, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (113, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 113;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (113, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (113, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3048, NOW(), NOW(), '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3048, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3048, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3049, NOW(), NOW(), '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3049, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3049, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3050, NOW(), NOW(), '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3050, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3050, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3051, NOW(), NOW(), '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-SILVER-STANDARD', 4, false, '12-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3051, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3051, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3052, NOW(), NOW(), '65f023632bc26470c104b75f', 113, 'CAR-SKU-113-SILVER-PREMIUM', 5, false, '12-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3052, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3052, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 114 (CAR-SKU-114): paint(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (114, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 114;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (114, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3053, NOW(), NOW(), '65f023632bc26470c104b75f', 114, 'CAR-SKU-114-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3053, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3054, NOW(), NOW(), '65f023632bc26470c104b75f', 114, 'CAR-SKU-114-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3054, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3055, NOW(), NOW(), '65f023632bc26470c104b75f', 114, 'CAR-SKU-114-BLUE', 3, false, '29')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3055, 5, 29) on conflict (product_variant_id, product_option_id) do nothing;

-- product 115 (CAR-SKU-115): paint(2) x trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (115, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (115, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 115;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (115, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (115, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3056, NOW(), NOW(), '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3056, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3056, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3057, NOW(), NOW(), '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-WHITE-SPORT', 2, false, '10-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3057, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3057, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3058, NOW(), NOW(), '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-BLACK-STANDARD', 3, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3058, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3058, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3059, NOW(), NOW(), '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-BLACK-PREMIUM', 4, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3059, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3059, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3060, NOW(), NOW(), '65f023632bc26470c104b75f', 115, 'CAR-SKU-115-BLACK-SPORT', 5, false, '11-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3060, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3060, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 117 (CAR-SKU-117): paint(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (117, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 117;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (117, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3061, NOW(), NOW(), '65f023632bc26470c104b75f', 117, 'CAR-SKU-117-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3061, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3062, NOW(), NOW(), '65f023632bc26470c104b75f', 117, 'CAR-SKU-117-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3062, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;

-- product 118 (CAR-SKU-118): paint(2) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (118, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (118, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 118;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (118, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (118, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3063, NOW(), NOW(), '65f023632bc26470c104b75f', 118, 'CAR-SKU-118-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3063, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3063, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3064, NOW(), NOW(), '65f023632bc26470c104b75f', 118, 'CAR-SKU-118-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3064, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3064, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3065, NOW(), NOW(), '65f023632bc26470c104b75f', 118, 'CAR-SKU-118-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3065, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3065, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 119 (CAR-SKU-119): trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (119, 6, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '13' WHERE product_variant_id = 119;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (119, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3066, NOW(), NOW(), '65f023632bc26470c104b75f', 119, 'CAR-SKU-119-PREMIUM', 1, false, '14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3066, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3067, NOW(), NOW(), '65f023632bc26470c104b75f', 119, 'CAR-SKU-119-SPORT', 2, false, '31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3067, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 120 (CAR-SKU-120): paint(3) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (120, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (120, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 120;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (120, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (120, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3068, NOW(), NOW(), '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3068, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3068, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3069, NOW(), NOW(), '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3069, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3069, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3070, NOW(), NOW(), '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3070, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3070, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3071, NOW(), NOW(), '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-SILVER-STANDARD', 4, false, '12-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3071, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3071, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3072, NOW(), NOW(), '65f023632bc26470c104b75f', 120, 'CAR-SKU-120-SILVER-PREMIUM', 5, false, '12-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3072, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3072, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 122 (CAR-SKU-122): paint(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (122, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 122;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (122, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3073, NOW(), NOW(), '65f023632bc26470c104b75f', 122, 'CAR-SKU-122-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3073, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3074, NOW(), NOW(), '65f023632bc26470c104b75f', 122, 'CAR-SKU-122-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3074, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3075, NOW(), NOW(), '65f023632bc26470c104b75f', 122, 'CAR-SKU-122-BLUE', 3, false, '29')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3075, 5, 29) on conflict (product_variant_id, product_option_id) do nothing;

-- product 123 (CAR-SKU-123): paint(2) x trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (123, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (123, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 123;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (123, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (123, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3076, NOW(), NOW(), '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3076, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3076, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3077, NOW(), NOW(), '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-WHITE-SPORT', 2, false, '10-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3077, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3077, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3078, NOW(), NOW(), '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-BLACK-STANDARD', 3, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3078, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3078, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3079, NOW(), NOW(), '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-BLACK-PREMIUM', 4, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3079, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3079, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3080, NOW(), NOW(), '65f023632bc26470c104b75f', 123, 'CAR-SKU-123-BLACK-SPORT', 5, false, '11-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3080, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3080, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 124 (CAR-SKU-124): paint(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (124, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 124;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (124, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3081, NOW(), NOW(), '65f023632bc26470c104b75f', 124, 'CAR-SKU-124-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3081, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3082, NOW(), NOW(), '65f023632bc26470c104b75f', 124, 'CAR-SKU-124-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3082, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;

-- product 125 (CAR-SKU-125): paint(2) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (125, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (125, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 125;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (125, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (125, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3083, NOW(), NOW(), '65f023632bc26470c104b75f', 125, 'CAR-SKU-125-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3083, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3083, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3084, NOW(), NOW(), '65f023632bc26470c104b75f', 125, 'CAR-SKU-125-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3084, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3084, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3085, NOW(), NOW(), '65f023632bc26470c104b75f', 125, 'CAR-SKU-125-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3085, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3085, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 127 (CAR-SKU-127): trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (127, 6, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '13' WHERE product_variant_id = 127;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (127, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3086, NOW(), NOW(), '65f023632bc26470c104b75f', 127, 'CAR-SKU-127-PREMIUM', 1, false, '14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3086, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3087, NOW(), NOW(), '65f023632bc26470c104b75f', 127, 'CAR-SKU-127-SPORT', 2, false, '31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3087, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 128 (CAR-SKU-128): paint(3) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (128, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (128, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 128;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (128, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (128, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3088, NOW(), NOW(), '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3088, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3088, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3089, NOW(), NOW(), '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3089, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3089, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3090, NOW(), NOW(), '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3090, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3090, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3091, NOW(), NOW(), '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-SILVER-STANDARD', 4, false, '12-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3091, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3091, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3092, NOW(), NOW(), '65f023632bc26470c104b75f', 128, 'CAR-SKU-128-SILVER-PREMIUM', 5, false, '12-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3092, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3092, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 129 (CAR-SKU-129): paint(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (129, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 129;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (129, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3093, NOW(), NOW(), '65f023632bc26470c104b75f', 129, 'CAR-SKU-129-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3093, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3094, NOW(), NOW(), '65f023632bc26470c104b75f', 129, 'CAR-SKU-129-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3094, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3095, NOW(), NOW(), '65f023632bc26470c104b75f', 129, 'CAR-SKU-129-BLUE', 3, false, '29')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3095, 5, 29) on conflict (product_variant_id, product_option_id) do nothing;

-- product 130 (CAR-SKU-130): paint(2) x trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (130, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (130, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 130;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (130, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (130, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3096, NOW(), NOW(), '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3096, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3096, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3097, NOW(), NOW(), '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-WHITE-SPORT', 2, false, '10-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3097, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3097, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3098, NOW(), NOW(), '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-BLACK-STANDARD', 3, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3098, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3098, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3099, NOW(), NOW(), '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-BLACK-PREMIUM', 4, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3099, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3099, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3100, NOW(), NOW(), '65f023632bc26470c104b75f', 130, 'CAR-SKU-130-BLACK-SPORT', 5, false, '11-31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3100, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3100, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

-- product 132 (CAR-SKU-132): paint(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (132, 5, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10' WHERE product_variant_id = 132;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (132, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3101, NOW(), NOW(), '65f023632bc26470c104b75f', 132, 'CAR-SKU-132-BLACK', 1, false, '11')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3101, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3102, NOW(), NOW(), '65f023632bc26470c104b75f', 132, 'CAR-SKU-132-SILVER', 2, false, '12')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3102, 5, 12) on conflict (product_variant_id, product_option_id) do nothing;

-- product 133 (CAR-SKU-133): paint(2) x trim(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (133, 5, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (133, 6, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '10-13' WHERE product_variant_id = 133;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (133, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (133, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3103, NOW(), NOW(), '65f023632bc26470c104b75f', 133, 'CAR-SKU-133-WHITE-PREMIUM', 1, false, '10-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3103, 5, 10) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3103, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3104, NOW(), NOW(), '65f023632bc26470c104b75f', 133, 'CAR-SKU-133-BLACK-STANDARD', 2, false, '11-13')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3104, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3104, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3105, NOW(), NOW(), '65f023632bc26470c104b75f', 133, 'CAR-SKU-133-BLACK-PREMIUM', 3, false, '11-14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3105, 5, 11) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3105, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;

-- product 134 (CAR-SKU-134): trim(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (134, 6, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '13' WHERE product_variant_id = 134;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (134, 6, 13) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3106, NOW(), NOW(), '65f023632bc26470c104b75f', 134, 'CAR-SKU-134-PREMIUM', 1, false, '14')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3106, 6, 14) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (3107, NOW(), NOW(), '65f023632bc26470c104b75f', 134, 'CAR-SKU-134-SPORT', 2, false, '31')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (3107, 6, 31) on conflict (product_variant_id, product_option_id) do nothing;

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

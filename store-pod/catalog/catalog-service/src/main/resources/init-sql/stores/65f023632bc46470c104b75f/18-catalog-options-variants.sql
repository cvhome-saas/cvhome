-- Demo option vocabulary and multi-variant products for the electronics store.
--
-- Electronics store: storage and finish — the axes a phone or a laptop actually sells by.
--
-- Each product's original sku stays on its default variant, which is promoted to the first
-- combination, so the existing inventory seed keeps pricing it; the extra combination skus get
-- their own rows in the inventory service's store seed.

-- ------------------------------------------------------------------------------------------ option vocabulary
INSERT INTO catalog.product_option (product_option_id, date_created, date_modified, code, sort_order, store_merchant_id)
VALUES (7, NOW(), NOW(), 'storage', 0, '65f023632bc46470c104b75f')
on conflict (product_option_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (17, NOW(), NOW(), 'Storage', 'en', 7) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (18, NOW(), NOW(), 'Stockage', 'fr', 7) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (15, NOW(), NOW(), '128gb', 0, 7)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (29, NOW(), NOW(), '128 GB', 'en', 15) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (30, NOW(), NOW(), '128 Go', 'fr', 15) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (16, NOW(), NOW(), '256gb', 1, 7)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (31, NOW(), NOW(), '256 GB', 'en', 16) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (32, NOW(), NOW(), '256 Go', 'fr', 16) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (17, NOW(), NOW(), '512gb', 2, 7)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (33, NOW(), NOW(), '512 GB', 'en', 17) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (34, NOW(), NOW(), '512 Go', 'fr', 17) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option (product_option_id, date_created, date_modified, code, sort_order, store_merchant_id)
VALUES (8, NOW(), NOW(), 'finish', 1, '65f023632bc46470c104b75f')
on conflict (product_option_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (19, NOW(), NOW(), 'Finish', 'en', 8) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (20, NOW(), NOW(), 'Finition', 'fr', 8) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (18, NOW(), NOW(), 'graphite', 0, 8)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (35, NOW(), NOW(), 'Graphite', 'en', 18) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (36, NOW(), NOW(), 'Graphite', 'fr', 18) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (19, NOW(), NOW(), 'silver', 1, 8)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (37, NOW(), NOW(), 'Silver', 'en', 19) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (38, NOW(), NOW(), 'Argent', 'fr', 19) on conflict (description_id) do nothing;

-- -------------------- product 136: storage x finish, the full six
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (136, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (136, 8, 1) on conflict (product_id, product_option_id) do nothing;

-- the default variant keeps the original sku as the 128GB-GRAPHITE combination
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 136;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (136, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (136, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (530, NOW(), NOW(), '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (530, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (530, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (531, NOW(), NOW(), '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (531, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (531, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (532, NOW(), NOW(), '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (532, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (532, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (533, NOW(), NOW(), '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-512GB-GRAPHITE', 4, false, '17-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (533, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (533, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (534, NOW(), NOW(), '65f023632bc46470c104b75f', 136, 'ELEC-SKU-136-512GB-SILVER', 5, false, '17-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (534, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (534, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- -------------------- product 137: storage only
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (137, 7, 0) on conflict (product_id, product_option_id) do nothing;

-- the default variant keeps the original sku as the 128GB combination
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 137;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (137, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (535, NOW(), NOW(), '65f023632bc46470c104b75f', 137, 'ELEC-SKU-137-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (535, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (536, NOW(), NOW(), '65f023632bc46470c104b75f', 137, 'ELEC-SKU-137-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (536, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;

-- -------------------- product 138: finish only
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (138, 8, 0) on conflict (product_id, product_option_id) do nothing;

-- the default variant keeps the original sku as the GRAPHITE combination
UPDATE catalog.product_variant SET option_signature = '18' WHERE product_variant_id = 138;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (138, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (537, NOW(), NOW(), '65f023632bc46470c104b75f', 138, 'ELEC-SKU-138-SILVER', 1, false, '19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (537, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

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
VALUES (32, NOW(), NOW(), '1tb', 3, 7) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (63, NOW(), NOW(), '1 TB', 'en', 32) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (64, NOW(), NOW(), '1 To', 'fr', 32) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (33, NOW(), NOW(), 'midnight', 2, 8) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (65, NOW(), NOW(), 'Midnight', 'en', 33) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (66, NOW(), NOW(), 'Minuit', 'fr', 33) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (34, NOW(), NOW(), 'gold', 3, 8) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (67, NOW(), NOW(), 'Gold', 'en', 34) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (68, NOW(), NOW(), 'Or', 'fr', 34) on conflict (description_id) do nothing;

-- product 139 (ELEC-SKU-139): storage(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (139, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 139;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (139, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4001, NOW(), NOW(), '65f023632bc46470c104b75f', 139, 'ELEC-SKU-139-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4001, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4002, NOW(), NOW(), '65f023632bc46470c104b75f', 139, 'ELEC-SKU-139-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4002, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;

-- product 140 (ELEC-SKU-140): storage(2) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (140, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (140, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 140;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (140, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (140, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4003, NOW(), NOW(), '65f023632bc46470c104b75f', 140, 'ELEC-SKU-140-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4003, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4003, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4004, NOW(), NOW(), '65f023632bc46470c104b75f', 140, 'ELEC-SKU-140-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4004, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4004, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4005, NOW(), NOW(), '65f023632bc46470c104b75f', 140, 'ELEC-SKU-140-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4005, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4005, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 142 (ELEC-SKU-142): finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (142, 8, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '18' WHERE product_variant_id = 142;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (142, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4006, NOW(), NOW(), '65f023632bc46470c104b75f', 142, 'ELEC-SKU-142-SILVER', 1, false, '19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4006, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4007, NOW(), NOW(), '65f023632bc46470c104b75f', 142, 'ELEC-SKU-142-MIDNIGHT', 2, false, '33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4007, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 143 (ELEC-SKU-143): storage(3) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (143, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (143, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 143;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (143, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (143, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4008, NOW(), NOW(), '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4008, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4008, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4009, NOW(), NOW(), '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4009, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4009, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4010, NOW(), NOW(), '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4010, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4010, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4011, NOW(), NOW(), '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-512GB-GRAPHITE', 4, false, '17-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4011, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4011, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4012, NOW(), NOW(), '65f023632bc46470c104b75f', 143, 'ELEC-SKU-143-512GB-SILVER', 5, false, '17-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4012, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4012, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 144 (ELEC-SKU-144): storage(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (144, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 144;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (144, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4013, NOW(), NOW(), '65f023632bc46470c104b75f', 144, 'ELEC-SKU-144-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4013, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4014, NOW(), NOW(), '65f023632bc46470c104b75f', 144, 'ELEC-SKU-144-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4014, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4015, NOW(), NOW(), '65f023632bc46470c104b75f', 144, 'ELEC-SKU-144-1TB', 3, false, '32')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4015, 7, 32) on conflict (product_variant_id, product_option_id) do nothing;

-- product 145 (ELEC-SKU-145): storage(2) x finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (145, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (145, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 145;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (145, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (145, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4016, NOW(), NOW(), '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4016, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4016, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4017, NOW(), NOW(), '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-128GB-MIDNIGHT', 2, false, '15-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4017, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4017, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4018, NOW(), NOW(), '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-256GB-GRAPHITE', 3, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4018, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4018, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4019, NOW(), NOW(), '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-256GB-SILVER', 4, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4019, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4019, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4020, NOW(), NOW(), '65f023632bc46470c104b75f', 145, 'ELEC-SKU-145-256GB-MIDNIGHT', 5, false, '16-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4020, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4020, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 147 (ELEC-SKU-147): storage(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (147, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 147;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (147, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4021, NOW(), NOW(), '65f023632bc46470c104b75f', 147, 'ELEC-SKU-147-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4021, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4022, NOW(), NOW(), '65f023632bc46470c104b75f', 147, 'ELEC-SKU-147-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4022, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;

-- product 148 (ELEC-SKU-148): storage(2) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (148, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (148, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 148;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (148, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (148, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4023, NOW(), NOW(), '65f023632bc46470c104b75f', 148, 'ELEC-SKU-148-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4023, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4023, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4024, NOW(), NOW(), '65f023632bc46470c104b75f', 148, 'ELEC-SKU-148-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4024, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4024, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4025, NOW(), NOW(), '65f023632bc46470c104b75f', 148, 'ELEC-SKU-148-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4025, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4025, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 149 (ELEC-SKU-149): finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (149, 8, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '18' WHERE product_variant_id = 149;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (149, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4026, NOW(), NOW(), '65f023632bc46470c104b75f', 149, 'ELEC-SKU-149-SILVER', 1, false, '19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4026, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4027, NOW(), NOW(), '65f023632bc46470c104b75f', 149, 'ELEC-SKU-149-MIDNIGHT', 2, false, '33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4027, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 150 (ELEC-SKU-150): storage(3) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (150, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (150, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 150;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (150, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (150, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4028, NOW(), NOW(), '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4028, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4028, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4029, NOW(), NOW(), '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4029, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4029, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4030, NOW(), NOW(), '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4030, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4030, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4031, NOW(), NOW(), '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-512GB-GRAPHITE', 4, false, '17-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4031, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4031, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4032, NOW(), NOW(), '65f023632bc46470c104b75f', 150, 'ELEC-SKU-150-512GB-SILVER', 5, false, '17-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4032, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4032, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 152 (ELEC-SKU-152): storage(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (152, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 152;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (152, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4033, NOW(), NOW(), '65f023632bc46470c104b75f', 152, 'ELEC-SKU-152-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4033, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4034, NOW(), NOW(), '65f023632bc46470c104b75f', 152, 'ELEC-SKU-152-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4034, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4035, NOW(), NOW(), '65f023632bc46470c104b75f', 152, 'ELEC-SKU-152-1TB', 3, false, '32')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4035, 7, 32) on conflict (product_variant_id, product_option_id) do nothing;

-- product 153 (ELEC-SKU-153): storage(2) x finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (153, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (153, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 153;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (153, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (153, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4036, NOW(), NOW(), '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4036, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4036, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4037, NOW(), NOW(), '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-128GB-MIDNIGHT', 2, false, '15-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4037, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4037, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4038, NOW(), NOW(), '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-256GB-GRAPHITE', 3, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4038, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4038, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4039, NOW(), NOW(), '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-256GB-SILVER', 4, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4039, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4039, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4040, NOW(), NOW(), '65f023632bc46470c104b75f', 153, 'ELEC-SKU-153-256GB-MIDNIGHT', 5, false, '16-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4040, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4040, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 154 (ELEC-SKU-154): storage(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (154, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 154;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (154, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4041, NOW(), NOW(), '65f023632bc46470c104b75f', 154, 'ELEC-SKU-154-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4041, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4042, NOW(), NOW(), '65f023632bc46470c104b75f', 154, 'ELEC-SKU-154-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4042, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;

-- product 155 (ELEC-SKU-155): storage(2) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (155, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (155, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 155;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (155, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (155, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4043, NOW(), NOW(), '65f023632bc46470c104b75f', 155, 'ELEC-SKU-155-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4043, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4043, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4044, NOW(), NOW(), '65f023632bc46470c104b75f', 155, 'ELEC-SKU-155-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4044, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4044, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4045, NOW(), NOW(), '65f023632bc46470c104b75f', 155, 'ELEC-SKU-155-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4045, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4045, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 157 (ELEC-SKU-157): finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (157, 8, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '18' WHERE product_variant_id = 157;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (157, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4046, NOW(), NOW(), '65f023632bc46470c104b75f', 157, 'ELEC-SKU-157-SILVER', 1, false, '19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4046, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4047, NOW(), NOW(), '65f023632bc46470c104b75f', 157, 'ELEC-SKU-157-MIDNIGHT', 2, false, '33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4047, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 158 (ELEC-SKU-158): storage(3) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (158, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (158, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 158;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (158, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (158, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4048, NOW(), NOW(), '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4048, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4048, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4049, NOW(), NOW(), '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4049, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4049, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4050, NOW(), NOW(), '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4050, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4050, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4051, NOW(), NOW(), '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-512GB-GRAPHITE', 4, false, '17-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4051, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4051, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4052, NOW(), NOW(), '65f023632bc46470c104b75f', 158, 'ELEC-SKU-158-512GB-SILVER', 5, false, '17-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4052, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4052, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 159 (ELEC-SKU-159): storage(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (159, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 159;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (159, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4053, NOW(), NOW(), '65f023632bc46470c104b75f', 159, 'ELEC-SKU-159-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4053, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4054, NOW(), NOW(), '65f023632bc46470c104b75f', 159, 'ELEC-SKU-159-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4054, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4055, NOW(), NOW(), '65f023632bc46470c104b75f', 159, 'ELEC-SKU-159-1TB', 3, false, '32')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4055, 7, 32) on conflict (product_variant_id, product_option_id) do nothing;

-- product 160 (ELEC-SKU-160): storage(2) x finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (160, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (160, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 160;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (160, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (160, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4056, NOW(), NOW(), '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4056, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4056, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4057, NOW(), NOW(), '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-128GB-MIDNIGHT', 2, false, '15-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4057, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4057, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4058, NOW(), NOW(), '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-256GB-GRAPHITE', 3, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4058, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4058, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4059, NOW(), NOW(), '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-256GB-SILVER', 4, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4059, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4059, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4060, NOW(), NOW(), '65f023632bc46470c104b75f', 160, 'ELEC-SKU-160-256GB-MIDNIGHT', 5, false, '16-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4060, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4060, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 162 (ELEC-SKU-162): storage(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (162, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 162;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (162, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4061, NOW(), NOW(), '65f023632bc46470c104b75f', 162, 'ELEC-SKU-162-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4061, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4062, NOW(), NOW(), '65f023632bc46470c104b75f', 162, 'ELEC-SKU-162-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4062, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;

-- product 163 (ELEC-SKU-163): storage(2) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (163, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (163, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 163;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (163, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (163, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4063, NOW(), NOW(), '65f023632bc46470c104b75f', 163, 'ELEC-SKU-163-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4063, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4063, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4064, NOW(), NOW(), '65f023632bc46470c104b75f', 163, 'ELEC-SKU-163-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4064, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4064, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4065, NOW(), NOW(), '65f023632bc46470c104b75f', 163, 'ELEC-SKU-163-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4065, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4065, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 164 (ELEC-SKU-164): finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (164, 8, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '18' WHERE product_variant_id = 164;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (164, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4066, NOW(), NOW(), '65f023632bc46470c104b75f', 164, 'ELEC-SKU-164-SILVER', 1, false, '19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4066, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4067, NOW(), NOW(), '65f023632bc46470c104b75f', 164, 'ELEC-SKU-164-MIDNIGHT', 2, false, '33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4067, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 165 (ELEC-SKU-165): storage(3) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (165, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (165, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 165;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (165, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (165, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4068, NOW(), NOW(), '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4068, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4068, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4069, NOW(), NOW(), '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4069, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4069, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4070, NOW(), NOW(), '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4070, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4070, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4071, NOW(), NOW(), '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-512GB-GRAPHITE', 4, false, '17-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4071, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4071, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4072, NOW(), NOW(), '65f023632bc46470c104b75f', 165, 'ELEC-SKU-165-512GB-SILVER', 5, false, '17-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4072, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4072, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 167 (ELEC-SKU-167): storage(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (167, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 167;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (167, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4073, NOW(), NOW(), '65f023632bc46470c104b75f', 167, 'ELEC-SKU-167-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4073, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4074, NOW(), NOW(), '65f023632bc46470c104b75f', 167, 'ELEC-SKU-167-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4074, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4075, NOW(), NOW(), '65f023632bc46470c104b75f', 167, 'ELEC-SKU-167-1TB', 3, false, '32')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4075, 7, 32) on conflict (product_variant_id, product_option_id) do nothing;

-- product 168 (ELEC-SKU-168): storage(2) x finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (168, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (168, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 168;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (168, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (168, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4076, NOW(), NOW(), '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4076, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4076, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4077, NOW(), NOW(), '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-128GB-MIDNIGHT', 2, false, '15-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4077, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4077, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4078, NOW(), NOW(), '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-256GB-GRAPHITE', 3, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4078, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4078, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4079, NOW(), NOW(), '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-256GB-SILVER', 4, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4079, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4079, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4080, NOW(), NOW(), '65f023632bc46470c104b75f', 168, 'ELEC-SKU-168-256GB-MIDNIGHT', 5, false, '16-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4080, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4080, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 169 (ELEC-SKU-169): storage(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (169, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 169;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (169, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4081, NOW(), NOW(), '65f023632bc46470c104b75f', 169, 'ELEC-SKU-169-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4081, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4082, NOW(), NOW(), '65f023632bc46470c104b75f', 169, 'ELEC-SKU-169-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4082, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;

-- product 170 (ELEC-SKU-170): storage(2) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (170, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (170, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 170;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (170, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (170, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4083, NOW(), NOW(), '65f023632bc46470c104b75f', 170, 'ELEC-SKU-170-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4083, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4083, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4084, NOW(), NOW(), '65f023632bc46470c104b75f', 170, 'ELEC-SKU-170-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4084, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4084, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4085, NOW(), NOW(), '65f023632bc46470c104b75f', 170, 'ELEC-SKU-170-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4085, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4085, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 172 (ELEC-SKU-172): finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (172, 8, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '18' WHERE product_variant_id = 172;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (172, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4086, NOW(), NOW(), '65f023632bc46470c104b75f', 172, 'ELEC-SKU-172-SILVER', 1, false, '19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4086, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4087, NOW(), NOW(), '65f023632bc46470c104b75f', 172, 'ELEC-SKU-172-MIDNIGHT', 2, false, '33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4087, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 173 (ELEC-SKU-173): storage(3) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (173, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (173, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 173;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (173, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (173, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4088, NOW(), NOW(), '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4088, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4088, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4089, NOW(), NOW(), '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4089, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4089, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4090, NOW(), NOW(), '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4090, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4090, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4091, NOW(), NOW(), '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-512GB-GRAPHITE', 4, false, '17-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4091, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4091, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4092, NOW(), NOW(), '65f023632bc46470c104b75f', 173, 'ELEC-SKU-173-512GB-SILVER', 5, false, '17-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4092, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4092, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 174 (ELEC-SKU-174): storage(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (174, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 174;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (174, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4093, NOW(), NOW(), '65f023632bc46470c104b75f', 174, 'ELEC-SKU-174-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4093, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4094, NOW(), NOW(), '65f023632bc46470c104b75f', 174, 'ELEC-SKU-174-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4094, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4095, NOW(), NOW(), '65f023632bc46470c104b75f', 174, 'ELEC-SKU-174-1TB', 3, false, '32')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4095, 7, 32) on conflict (product_variant_id, product_option_id) do nothing;

-- product 175 (ELEC-SKU-175): storage(2) x finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (175, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (175, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 175;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (175, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (175, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4096, NOW(), NOW(), '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4096, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4096, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4097, NOW(), NOW(), '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-128GB-MIDNIGHT', 2, false, '15-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4097, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4097, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4098, NOW(), NOW(), '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-256GB-GRAPHITE', 3, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4098, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4098, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4099, NOW(), NOW(), '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-256GB-SILVER', 4, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4099, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4099, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4100, NOW(), NOW(), '65f023632bc46470c104b75f', 175, 'ELEC-SKU-175-256GB-MIDNIGHT', 5, false, '16-33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4100, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4100, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

-- product 177 (ELEC-SKU-177): storage(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (177, 7, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15' WHERE product_variant_id = 177;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (177, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4101, NOW(), NOW(), '65f023632bc46470c104b75f', 177, 'ELEC-SKU-177-256GB', 1, false, '16')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4101, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4102, NOW(), NOW(), '65f023632bc46470c104b75f', 177, 'ELEC-SKU-177-512GB', 2, false, '17')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4102, 7, 17) on conflict (product_variant_id, product_option_id) do nothing;

-- product 178 (ELEC-SKU-178): storage(2) x finish(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (178, 7, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (178, 8, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '15-18' WHERE product_variant_id = 178;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (178, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (178, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4103, NOW(), NOW(), '65f023632bc46470c104b75f', 178, 'ELEC-SKU-178-128GB-SILVER', 1, false, '15-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4103, 7, 15) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4103, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4104, NOW(), NOW(), '65f023632bc46470c104b75f', 178, 'ELEC-SKU-178-256GB-GRAPHITE', 2, false, '16-18')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4104, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4104, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4105, NOW(), NOW(), '65f023632bc46470c104b75f', 178, 'ELEC-SKU-178-256GB-SILVER', 3, false, '16-19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4105, 7, 16) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4105, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;

-- product 179 (ELEC-SKU-179): finish(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (179, 8, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '18' WHERE product_variant_id = 179;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (179, 8, 18) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4106, NOW(), NOW(), '65f023632bc46470c104b75f', 179, 'ELEC-SKU-179-SILVER', 1, false, '19')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4106, 8, 19) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (4107, NOW(), NOW(), '65f023632bc46470c104b75f', 179, 'ELEC-SKU-179-MIDNIGHT', 2, false, '33')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (4107, 8, 33) on conflict (product_variant_id, product_option_id) do nothing;

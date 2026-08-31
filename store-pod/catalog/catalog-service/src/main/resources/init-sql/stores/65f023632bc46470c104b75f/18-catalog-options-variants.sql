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

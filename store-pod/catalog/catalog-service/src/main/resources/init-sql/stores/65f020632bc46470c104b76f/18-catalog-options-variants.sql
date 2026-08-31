-- Demo option vocabulary and multi-variant products for the beauty store.
--
-- Beauty store: shade and volume. Foundation sells in three shades and two bottle sizes,
-- a serum in shades only, a cream in sizes only — three shapes of matrix in one store.
--
-- Each product's original sku stays on its default variant, which is promoted to the first
-- combination, so the existing inventory seed keeps pricing it; the extra combination skus get
-- their own rows in the inventory service's store seed.

-- ------------------------------------------------------------------------------------------ option vocabulary
INSERT INTO catalog.product_option (product_option_id, date_created, date_modified, code, sort_order, store_merchant_id)
VALUES (3, NOW(), NOW(), 'shade', 0, '65f020632bc46470c104b76f')
on conflict (product_option_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (9, NOW(), NOW(), 'Shade', 'en', 3) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (10, NOW(), NOW(), 'Teinte', 'fr', 3) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (5, NOW(), NOW(), 'fair', 0, 3)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (9, NOW(), NOW(), 'Fair', 'en', 5) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (10, NOW(), NOW(), 'Clair', 'fr', 5) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (6, NOW(), NOW(), 'medium', 1, 3)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (11, NOW(), NOW(), 'Medium', 'en', 6) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (12, NOW(), NOW(), 'Moyen', 'fr', 6) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (7, NOW(), NOW(), 'deep', 2, 3)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (13, NOW(), NOW(), 'Deep', 'en', 7) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (14, NOW(), NOW(), 'Foncé', 'fr', 7) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option (product_option_id, date_created, date_modified, code, sort_order, store_merchant_id)
VALUES (4, NOW(), NOW(), 'volume', 1, '65f020632bc46470c104b76f')
on conflict (product_option_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (11, NOW(), NOW(), 'Volume', 'en', 4) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_description (description_id, date_created, date_modified, name, language_code, product_option_id)
VALUES (12, NOW(), NOW(), 'Contenance', 'fr', 4) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (8, NOW(), NOW(), '30ml', 0, 4)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (15, NOW(), NOW(), '30 ml', 'en', 8) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (16, NOW(), NOW(), '30 ml', 'fr', 8) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (9, NOW(), NOW(), '50ml', 1, 4)
on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (17, NOW(), NOW(), '50 ml', 'en', 9) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (18, NOW(), NOW(), '50 ml', 'fr', 9) on conflict (description_id) do nothing;

-- -------------------- product 46: YSL foundation — shade x volume, the full six
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (46, 3, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (46, 4, 1) on conflict (product_id, product_option_id) do nothing;

-- the default variant keeps the original sku as the FAIR-30ML combination
UPDATE catalog.product_variant SET option_signature = '5-8' WHERE product_variant_id = 46;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (46, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (46, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (510, NOW(), NOW(), '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-FAIR-50ML', 1, false, '5-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (510, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (510, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (511, NOW(), NOW(), '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-MEDIUM-30ML', 2, false, '6-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (511, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (511, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (512, NOW(), NOW(), '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-MEDIUM-50ML', 3, false, '6-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (512, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (512, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (513, NOW(), NOW(), '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-DEEP-30ML', 4, false, '7-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (513, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (513, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (514, NOW(), NOW(), '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-DEEP-50ML', 5, false, '7-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (514, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (514, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- -------------------- product 47: Guerlain oil — shade only
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (47, 3, 0) on conflict (product_id, product_option_id) do nothing;

-- the default variant keeps the original sku as the FAIR combination
UPDATE catalog.product_variant SET option_signature = '5' WHERE product_variant_id = 47;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (47, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (515, NOW(), NOW(), '65f020632bc46470c104b76f', 47, 'SKU-GUER-SKIN-ARWOIL47-MEDIUM', 1, false, '6')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (515, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (516, NOW(), NOW(), '65f020632bc46470c104b76f', 47, 'SKU-GUER-SKIN-ARWOIL47-DEEP', 2, false, '7')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (516, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;

-- -------------------- product 48: Shiseido serum — volume only
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (48, 4, 0) on conflict (product_id, product_option_id) do nothing;

-- the default variant keeps the original sku as the 30ML combination
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 48;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (48, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;

INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (517, NOW(), NOW(), '65f020632bc46470c104b76f', 48, 'SKU-SHIS-SKIN-ULTIMUNE48-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (517, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

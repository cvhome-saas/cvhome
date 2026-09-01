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
VALUES (25, NOW(), NOW(), 'rose', 3, 3) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (49, NOW(), NOW(), 'Rose', 'en', 25) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (50, NOW(), NOW(), 'Rosé', 'fr', 25) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (26, NOW(), NOW(), 'sand', 4, 3) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (51, NOW(), NOW(), 'Sand', 'en', 26) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (52, NOW(), NOW(), 'Sable', 'fr', 26) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (27, NOW(), NOW(), '100ml', 2, 4) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (53, NOW(), NOW(), '100 ml', 'en', 27) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (54, NOW(), NOW(), '100 ml', 'fr', 27) on conflict (description_id) do nothing;

INSERT INTO catalog.product_option_value (product_option_value_id, date_created, date_modified, code, sort_order, product_option_id)
VALUES (28, NOW(), NOW(), '200ml', 3, 4) on conflict (product_option_value_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (55, NOW(), NOW(), '200 ml', 'en', 28) on conflict (description_id) do nothing;
INSERT INTO catalog.product_option_value_description (description_id, date_created, date_modified, name, language_code, product_option_value_id)
VALUES (56, NOW(), NOW(), '200 ml', 'fr', 28) on conflict (description_id) do nothing;

-- product 49 (SKU-NARS-MAKE-RCCONC49): shade(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (49, 3, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5' WHERE product_variant_id = 49;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (49, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2001, NOW(), NOW(), '65f020632bc46470c104b76f', 49, 'SKU-NARS-MAKE-RCCONC49-MEDIUM', 1, false, '6')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2001, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2002, NOW(), NOW(), '65f020632bc46470c104b76f', 49, 'SKU-NARS-MAKE-RCCONC49-DEEP', 2, false, '7')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2002, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;

-- product 50 (SKU-LRP-SKIN-ANTHUV50): volume(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (50, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 50;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (50, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2003, NOW(), NOW(), '65f020632bc46470c104b76f', 50, 'SKU-LRP-SKIN-ANTHUV50-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2003, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- product 52 (SKU-YSL-FRAG-BLACKOPIUM52): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (52, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 52;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (52, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2004, NOW(), NOW(), '65f020632bc46470c104b76f', 52, 'SKU-YSL-FRAG-BLACKOPIUM52-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2004, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2005, NOW(), NOW(), '65f020632bc46470c104b76f', 52, 'SKU-YSL-FRAG-BLACKOPIUM52-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2005, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 53 (SKU-GUER-MAKE-TERRACOTTA53): shade(2) x volume(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (53, 3, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (53, 4, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5-8' WHERE product_variant_id = 53;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (53, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (53, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2006, NOW(), NOW(), '65f020632bc46470c104b76f', 53, 'SKU-GUER-MAKE-TERRACOTTA53-FAIR-50ML', 1, false, '5-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2006, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2006, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2007, NOW(), NOW(), '65f020632bc46470c104b76f', 53, 'SKU-GUER-MAKE-TERRACOTTA53-MEDIUM-30ML', 2, false, '6-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2007, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2007, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2008, NOW(), NOW(), '65f020632bc46470c104b76f', 53, 'SKU-GUER-MAKE-TERRACOTTA53-MEDIUM-50ML', 3, false, '6-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2008, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2008, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- product 54 (SKU-SHIS-SKIN-BENEFCR54): volume(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (54, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 54;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (54, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2009, NOW(), NOW(), '65f020632bc46470c104b76f', 54, 'SKU-SHIS-SKIN-BENEFCR54-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2009, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2010, NOW(), NOW(), '65f020632bc46470c104b76f', 54, 'SKU-SHIS-SKIN-BENEFCR54-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2010, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2011, NOW(), NOW(), '65f020632bc46470c104b76f', 54, 'SKU-SHIS-SKIN-BENEFCR54-200ML', 3, false, '28')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2011, 4, 28) on conflict (product_variant_id, product_option_id) do nothing;

-- product 55 (SKU-NARS-MAKE-ORGBLUSH55): shade(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (55, 3, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5' WHERE product_variant_id = 55;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (55, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2012, NOW(), NOW(), '65f020632bc46470c104b76f', 55, 'SKU-NARS-MAKE-ORGBLUSH55-MEDIUM', 1, false, '6')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2012, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2013, NOW(), NOW(), '65f020632bc46470c104b76f', 55, 'SKU-NARS-MAKE-ORGBLUSH55-DEEP', 2, false, '7')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2013, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2014, NOW(), NOW(), '65f020632bc46470c104b76f', 55, 'SKU-NARS-MAKE-ORGBLUSH55-ROSE', 3, false, '25')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2014, 3, 25) on conflict (product_variant_id, product_option_id) do nothing;

-- product 57 (SKU-KERA-HAIR-ELIXIR57): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (57, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 57;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (57, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2015, NOW(), NOW(), '65f020632bc46470c104b76f', 57, 'SKU-KERA-HAIR-ELIXIR57-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2015, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2016, NOW(), NOW(), '65f020632bc46470c104b76f', 57, 'SKU-KERA-HAIR-ELIXIR57-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2016, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 58 (SKU-YSL-FRAG-LIBREEDP58): volume(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (58, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 58;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (58, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2017, NOW(), NOW(), '65f020632bc46470c104b76f', 58, 'SKU-YSL-FRAG-LIBREEDP58-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2017, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- product 59 (SKU-GUER-MAKE-METEORITES59): shade(3) x volume(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (59, 3, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (59, 4, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5-8' WHERE product_variant_id = 59;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (59, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (59, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2018, NOW(), NOW(), '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-FAIR-50ML', 1, false, '5-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2018, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2018, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2019, NOW(), NOW(), '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-MEDIUM-30ML', 2, false, '6-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2019, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2019, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2020, NOW(), NOW(), '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-MEDIUM-50ML', 3, false, '6-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2020, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2020, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2021, NOW(), NOW(), '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-DEEP-30ML', 4, false, '7-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2021, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2021, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2022, NOW(), NOW(), '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-DEEP-50ML', 5, false, '7-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2022, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2022, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- product 60 (SKU-SHIS-MAKE-SYNCSKIN60): shade(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (60, 3, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5' WHERE product_variant_id = 60;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (60, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2023, NOW(), NOW(), '65f020632bc46470c104b76f', 60, 'SKU-SHIS-MAKE-SYNCSKIN60-MEDIUM', 1, false, '6')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2023, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;

-- product 62 (SKU-LRP-SKIN-HYALUB5-62): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (62, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 62;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (62, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2024, NOW(), NOW(), '65f020632bc46470c104b76f', 62, 'SKU-LRP-SKIN-HYALUB5-62-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2024, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2025, NOW(), NOW(), '65f020632bc46470c104b76f', 62, 'SKU-LRP-SKIN-HYALUB5-62-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2025, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 63 (SKU-KERA-HAIR-BACICAFLASH63): volume(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (63, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 63;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (63, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2026, NOW(), NOW(), '65f020632bc46470c104b76f', 63, 'SKU-KERA-HAIR-BACICAFLASH63-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2026, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2027, NOW(), NOW(), '65f020632bc46470c104b76f', 63, 'SKU-KERA-HAIR-BACICAFLASH63-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2027, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2028, NOW(), NOW(), '65f020632bc46470c104b76f', 63, 'SKU-KERA-HAIR-BACICAFLASH63-200ML', 3, false, '28')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2028, 4, 28) on conflict (product_variant_id, product_option_id) do nothing;

-- product 64 (SKU-YSL-MAKE-RPCLIP64): shade(2) x volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (64, 3, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (64, 4, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5-8' WHERE product_variant_id = 64;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (64, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (64, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2029, NOW(), NOW(), '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-FAIR-50ML', 1, false, '5-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2029, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2029, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2030, NOW(), NOW(), '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-FAIR-100ML', 2, false, '5-27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2030, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2030, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2031, NOW(), NOW(), '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-MEDIUM-30ML', 3, false, '6-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2031, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2031, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2032, NOW(), NOW(), '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-MEDIUM-50ML', 4, false, '6-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2032, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2032, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2033, NOW(), NOW(), '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-MEDIUM-100ML', 5, false, '6-27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2033, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2033, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 65 (SKU-GUER-FRAG-MONGUERLAIN65): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (65, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 65;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (65, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2034, NOW(), NOW(), '65f020632bc46470c104b76f', 65, 'SKU-GUER-FRAG-MONGUERLAIN65-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2034, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2035, NOW(), NOW(), '65f020632bc46470c104b76f', 65, 'SKU-GUER-FRAG-MONGUERLAIN65-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2035, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 67 (SKU-NARS-MAKE-SHEERGLOW67): shade(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (67, 3, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5' WHERE product_variant_id = 67;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (67, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2036, NOW(), NOW(), '65f020632bc46470c104b76f', 67, 'SKU-NARS-MAKE-SHEERGLOW67-MEDIUM', 1, false, '6')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2036, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2037, NOW(), NOW(), '65f020632bc46470c104b76f', 67, 'SKU-NARS-MAKE-SHEERGLOW67-DEEP', 2, false, '7')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2037, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;

-- product 68 (SKU-LRP-SKIN-EFFADUO68): volume(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (68, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 68;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (68, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2038, NOW(), NOW(), '65f020632bc46470c104b76f', 68, 'SKU-LRP-SKIN-EFFADUO68-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2038, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- product 69 (SKU-KERA-HAIR-CHRONOHUILE69): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (69, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 69;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (69, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2039, NOW(), NOW(), '65f020632bc46470c104b76f', 69, 'SKU-KERA-HAIR-CHRONOHUILE69-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2039, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2040, NOW(), NOW(), '65f020632bc46470c104b76f', 69, 'SKU-KERA-HAIR-CHRONOHUILE69-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2040, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 70 (SKU-YSL-FRAG-MONPARIS70): volume(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (70, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 70;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (70, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2041, NOW(), NOW(), '65f020632bc46470c104b76f', 70, 'SKU-YSL-FRAG-MONPARIS70-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2041, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2042, NOW(), NOW(), '65f020632bc46470c104b76f', 70, 'SKU-YSL-FRAG-MONPARIS70-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2042, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2043, NOW(), NOW(), '65f020632bc46470c104b76f', 70, 'SKU-YSL-FRAG-MONPARIS70-200ML', 3, false, '28')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2043, 4, 28) on conflict (product_variant_id, product_option_id) do nothing;

-- product 72 (SKU-SHIS-MAKE-MINBLUSH72): shade(2) x volume(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (72, 3, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (72, 4, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5-8' WHERE product_variant_id = 72;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (72, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (72, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2044, NOW(), NOW(), '65f020632bc46470c104b76f', 72, 'SKU-SHIS-MAKE-MINBLUSH72-FAIR-50ML', 1, false, '5-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2044, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2044, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2045, NOW(), NOW(), '65f020632bc46470c104b76f', 72, 'SKU-SHIS-MAKE-MINBLUSH72-MEDIUM-30ML', 2, false, '6-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2045, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2045, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2046, NOW(), NOW(), '65f020632bc46470c104b76f', 72, 'SKU-SHIS-MAKE-MINBLUSH72-MEDIUM-50ML', 3, false, '6-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2046, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2046, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- product 73 (SKU-NARS-MAKE-CLIMAXMASC73): shade(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (73, 3, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5' WHERE product_variant_id = 73;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (73, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2047, NOW(), NOW(), '65f020632bc46470c104b76f', 73, 'SKU-NARS-MAKE-CLIMAXMASC73-MEDIUM', 1, false, '6')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2047, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2048, NOW(), NOW(), '65f020632bc46470c104b76f', 73, 'SKU-NARS-MAKE-CLIMAXMASC73-DEEP', 2, false, '7')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2048, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2049, NOW(), NOW(), '65f020632bc46470c104b76f', 73, 'SKU-NARS-MAKE-CLIMAXMASC73-ROSE', 3, false, '25')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2049, 3, 25) on conflict (product_variant_id, product_option_id) do nothing;

-- product 74 (SKU-LRP-SKIN-TOLSENSCR74): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (74, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 74;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (74, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2050, NOW(), NOW(), '65f020632bc46470c104b76f', 74, 'SKU-LRP-SKIN-TOLSENSCR74-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2050, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2051, NOW(), NOW(), '65f020632bc46470c104b76f', 74, 'SKU-LRP-SKIN-TOLSENSCR74-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2051, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 75 (SKU-KERA-HAIR-CIMENTHERM75): volume(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (75, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 75;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (75, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2052, NOW(), NOW(), '65f020632bc46470c104b76f', 75, 'SKU-KERA-HAIR-CIMENTHERM75-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2052, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- product 77 (SKU-GUER-FRAG-AAMANDBAS77): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (77, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 77;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (77, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2053, NOW(), NOW(), '65f020632bc46470c104b76f', 77, 'SKU-GUER-FRAG-AAMANDBAS77-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2053, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2054, NOW(), NOW(), '65f020632bc46470c104b76f', 77, 'SKU-GUER-FRAG-AAMANDBAS77-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2054, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 78 (SKU-SHIS-HAIR-TSUBAKIMASK78): volume(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (78, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 78;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (78, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2055, NOW(), NOW(), '65f020632bc46470c104b76f', 78, 'SKU-SHIS-HAIR-TSUBAKIMASK78-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2055, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2056, NOW(), NOW(), '65f020632bc46470c104b76f', 78, 'SKU-SHIS-HAIR-TSUBAKIMASK78-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2056, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2057, NOW(), NOW(), '65f020632bc46470c104b76f', 78, 'SKU-SHIS-HAIR-TSUBAKIMASK78-200ML', 3, false, '28')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2057, 4, 28) on conflict (product_variant_id, product_option_id) do nothing;

-- product 79 (SKU-NARS-MAKE-AFTERGLOWLB79): shade(3) x volume(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (79, 3, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (79, 4, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5-8' WHERE product_variant_id = 79;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (79, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (79, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2058, NOW(), NOW(), '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-FAIR-50ML', 1, false, '5-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2058, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2058, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2059, NOW(), NOW(), '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-MEDIUM-30ML', 2, false, '6-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2059, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2059, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2060, NOW(), NOW(), '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-MEDIUM-50ML', 3, false, '6-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2060, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2060, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2061, NOW(), NOW(), '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-DEEP-30ML', 4, false, '7-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2061, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2061, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2062, NOW(), NOW(), '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-DEEP-50ML', 5, false, '7-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2062, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2062, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- product 80 (SKU-LRP-SKIN-LIPIAPM80): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (80, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 80;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (80, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2063, NOW(), NOW(), '65f020632bc46470c104b76f', 80, 'SKU-LRP-SKIN-LIPIAPM80-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2063, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2064, NOW(), NOW(), '65f020632bc46470c104b76f', 80, 'SKU-LRP-SKIN-LIPIAPM80-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2064, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 82 (SKU-YSL-MAKE-ALLHOURSFND82): shade(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (82, 3, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5' WHERE product_variant_id = 82;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (82, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2065, NOW(), NOW(), '65f020632bc46470c104b76f', 82, 'SKU-YSL-MAKE-ALLHOURSFND82-MEDIUM', 1, false, '6')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2065, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;

-- product 83 (SKU-GUER-SKIN-SUPERAQUA83): volume(2)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (83, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 83;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (83, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2066, NOW(), NOW(), '65f020632bc46470c104b76f', 83, 'SKU-GUER-SKIN-SUPERAQUA83-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2066, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;

-- product 84 (SKU-SHIS-FRAG-GINZAEDP84): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (84, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 84;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (84, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2067, NOW(), NOW(), '65f020632bc46470c104b76f', 84, 'SKU-SHIS-FRAG-GINZAEDP84-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2067, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2068, NOW(), NOW(), '65f020632bc46470c104b76f', 84, 'SKU-SHIS-FRAG-GINZAEDP84-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2068, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 85 (SKU-NARS-MAKE-LRSPWD85): shade(2) x volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (85, 3, 0) on conflict (product_id, product_option_id) do nothing;
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (85, 4, 1) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5-8' WHERE product_variant_id = 85;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (85, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (85, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2069, NOW(), NOW(), '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-FAIR-50ML', 1, false, '5-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2069, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2069, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2070, NOW(), NOW(), '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-FAIR-100ML', 2, false, '5-27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2070, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2070, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2071, NOW(), NOW(), '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-MEDIUM-30ML', 3, false, '6-8')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2071, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2071, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2072, NOW(), NOW(), '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-MEDIUM-50ML', 4, false, '6-9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2072, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2072, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2073, NOW(), NOW(), '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-MEDIUM-100ML', 5, false, '6-27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2073, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2073, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 87 (SKU-KERA-HAIR-DISCFLUID87): volume(4)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (87, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 87;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (87, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2074, NOW(), NOW(), '65f020632bc46470c104b76f', 87, 'SKU-KERA-HAIR-DISCFLUID87-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2074, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2075, NOW(), NOW(), '65f020632bc46470c104b76f', 87, 'SKU-KERA-HAIR-DISCFLUID87-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2075, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2076, NOW(), NOW(), '65f020632bc46470c104b76f', 87, 'SKU-KERA-HAIR-DISCFLUID87-200ML', 3, false, '28')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2076, 4, 28) on conflict (product_variant_id, product_option_id) do nothing;

-- product 88 (SKU-YSL-FRAG-YEDP88): volume(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (88, 4, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '8' WHERE product_variant_id = 88;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (88, 4, 8) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2077, NOW(), NOW(), '65f020632bc46470c104b76f', 88, 'SKU-YSL-FRAG-YEDP88-50ML', 1, false, '9')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2077, 4, 9) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2078, NOW(), NOW(), '65f020632bc46470c104b76f', 88, 'SKU-YSL-FRAG-YEDP88-100ML', 2, false, '27')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2078, 4, 27) on conflict (product_variant_id, product_option_id) do nothing;

-- product 89 (SKU-GUER-MAKE-LESSENTIEL89): shade(3)
INSERT INTO catalog.product_option_assignment (product_id, product_option_id, sort_order)
VALUES (89, 3, 0) on conflict (product_id, product_option_id) do nothing;
UPDATE catalog.product_variant SET option_signature = '5' WHERE product_variant_id = 89;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (89, 3, 5) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2079, NOW(), NOW(), '65f020632bc46470c104b76f', 89, 'SKU-GUER-MAKE-LESSENTIEL89-MEDIUM', 1, false, '6')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2079, 3, 6) on conflict (product_variant_id, product_option_id) do nothing;
INSERT INTO catalog.product_variant (product_variant_id, date_created, date_modified, store_merchant_id, product_id, sku, sort_order, default_variant, option_signature)
VALUES (2080, NOW(), NOW(), '65f020632bc46470c104b76f', 89, 'SKU-GUER-MAKE-LESSENTIEL89-DEEP', 2, false, '7')
on conflict (product_variant_id) do nothing;
INSERT INTO catalog.product_variant_option_value (product_variant_id, product_option_id, product_option_value_id)
VALUES (2080, 3, 7) on conflict (product_variant_id, product_option_id) do nothing;

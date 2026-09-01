-- Stock and price for the beauty store's combination variants.
--
-- One row per combination sku beyond the default variant, whose row the base seed already
-- carries. Prices differ per combination so a matrix shows real per-variant pricing, and a
-- few combinations are deliberately out of stock — that is the greyed-chip case on the PDP.

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (510, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-FAIR-50ML', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (510, '65f020632bc46470c104b76f', 510, 'base', true, 28.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (511, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-MEDIUM-30ML', 10, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (511, '65f020632bc46470c104b76f', 511, 'base', true, 21.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (512, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-MEDIUM-50ML', 8, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (512, '65f020632bc46470c104b76f', 512, 'base', true, 29.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (513, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-DEEP-30ML', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (513, '65f020632bc46470c104b76f', 513, 'base', true, 22.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (514, '65f020632bc46470c104b76f', 46, 'SKU-YSL-MAKE-TEIPEN46-DEEP-50ML', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (514, '65f020632bc46470c104b76f', 514, 'base', true, 30.00)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (515, '65f020632bc46470c104b76f', 47, 'SKU-GUER-SKIN-ARWOIL47-MEDIUM', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (515, '65f020632bc46470c104b76f', 515, 'base', true, 22.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (516, '65f020632bc46470c104b76f', 47, 'SKU-GUER-SKIN-ARWOIL47-DEEP', 10, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (516, '65f020632bc46470c104b76f', 516, 'base', true, 23.50)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (517, '65f020632bc46470c104b76f', 48, 'SKU-SHIS-SKIN-ULTIMUNE48-50ML', 12, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (517, '65f020632bc46470c104b76f', 517, 'base', true, 31.00)
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
VALUES (2001, '65f020632bc46470c104b76f', 49, 'SKU-NARS-MAKE-RCCONC49-MEDIUM', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2001, '65f020632bc46470c104b76f', 2001, 'base', true, 25.73)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2002, '65f020632bc46470c104b76f', 49, 'SKU-NARS-MAKE-RCCONC49-DEEP', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2002, '65f020632bc46470c104b76f', 2002, 'base', true, 26.95)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2003, '65f020632bc46470c104b76f', 50, 'SKU-LRP-SKIN-ANTHUV50-50ML', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2003, '65f020632bc46470c104b76f', 2003, 'base', true, 27.30)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2004, '65f020632bc46470c104b76f', 52, 'SKU-YSL-FRAG-BLACKOPIUM52-50ML', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2004, '65f020632bc46470c104b76f', 2004, 'base', true, 30.45)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2005, '65f020632bc46470c104b76f', 52, 'SKU-YSL-FRAG-BLACKOPIUM52-100ML', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2005, '65f020632bc46470c104b76f', 2005, 'base', true, 31.90)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2006, '65f020632bc46470c104b76f', 53, 'SKU-GUER-MAKE-TERRACOTTA53-FAIR-50ML', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2006, '65f020632bc46470c104b76f', 2006, 'base', true, 32.03)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2007, '65f020632bc46470c104b76f', 53, 'SKU-GUER-MAKE-TERRACOTTA53-MEDIUM-30ML', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2007, '65f020632bc46470c104b76f', 2007, 'base', true, 33.55)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2008, '65f020632bc46470c104b76f', 53, 'SKU-GUER-MAKE-TERRACOTTA53-MEDIUM-50ML', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2008, '65f020632bc46470c104b76f', 2008, 'base', true, 35.08)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2009, '65f020632bc46470c104b76f', 54, 'SKU-SHIS-SKIN-BENEFCR54-50ML', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2009, '65f020632bc46470c104b76f', 2009, 'base', true, 33.60)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2010, '65f020632bc46470c104b76f', 54, 'SKU-SHIS-SKIN-BENEFCR54-100ML', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2010, '65f020632bc46470c104b76f', 2010, 'base', true, 35.20)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2011, '65f020632bc46470c104b76f', 54, 'SKU-SHIS-SKIN-BENEFCR54-200ML', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2011, '65f020632bc46470c104b76f', 2011, 'base', true, 36.80)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2012, '65f020632bc46470c104b76f', 55, 'SKU-NARS-MAKE-ORGBLUSH55-MEDIUM', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2012, '65f020632bc46470c104b76f', 2012, 'base', true, 35.18)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2013, '65f020632bc46470c104b76f', 55, 'SKU-NARS-MAKE-ORGBLUSH55-DEEP', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2013, '65f020632bc46470c104b76f', 2013, 'base', true, 36.85)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2014, '65f020632bc46470c104b76f', 55, 'SKU-NARS-MAKE-ORGBLUSH55-ROSE', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2014, '65f020632bc46470c104b76f', 2014, 'base', true, 38.53)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2015, '65f020632bc46470c104b76f', 57, 'SKU-KERA-HAIR-ELIXIR57-50ML', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2015, '65f020632bc46470c104b76f', 2015, 'base', true, 38.33)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2016, '65f020632bc46470c104b76f', 57, 'SKU-KERA-HAIR-ELIXIR57-100ML', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2016, '65f020632bc46470c104b76f', 2016, 'base', true, 40.15)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2017, '65f020632bc46470c104b76f', 58, 'SKU-YSL-FRAG-LIBREEDP58-50ML', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2017, '65f020632bc46470c104b76f', 2017, 'base', true, 39.90)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2018, '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-FAIR-50ML', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2018, '65f020632bc46470c104b76f', 2018, 'base', true, 41.48)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2019, '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-MEDIUM-30ML', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2019, '65f020632bc46470c104b76f', 2019, 'base', true, 43.45)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2020, '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-MEDIUM-50ML', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2020, '65f020632bc46470c104b76f', 2020, 'base', true, 45.43)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2021, '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-DEEP-30ML', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2021, '65f020632bc46470c104b76f', 2021, 'base', true, 47.40)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2022, '65f020632bc46470c104b76f', 59, 'SKU-GUER-MAKE-METEORITES59-DEEP-50ML', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2022, '65f020632bc46470c104b76f', 2022, 'base', true, 49.38)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2023, '65f020632bc46470c104b76f', 60, 'SKU-SHIS-MAKE-SYNCSKIN60-MEDIUM', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2023, '65f020632bc46470c104b76f', 2023, 'base', true, 43.05)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2024, '65f020632bc46470c104b76f', 62, 'SKU-LRP-SKIN-HYALUB5-62-50ML', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2024, '65f020632bc46470c104b76f', 2024, 'base', true, 46.20)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2025, '65f020632bc46470c104b76f', 62, 'SKU-LRP-SKIN-HYALUB5-62-100ML', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2025, '65f020632bc46470c104b76f', 2025, 'base', true, 48.40)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2026, '65f020632bc46470c104b76f', 63, 'SKU-KERA-HAIR-BACICAFLASH63-50ML', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2026, '65f020632bc46470c104b76f', 2026, 'base', true, 47.78)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2027, '65f020632bc46470c104b76f', 63, 'SKU-KERA-HAIR-BACICAFLASH63-100ML', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2027, '65f020632bc46470c104b76f', 2027, 'base', true, 50.05)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2028, '65f020632bc46470c104b76f', 63, 'SKU-KERA-HAIR-BACICAFLASH63-200ML', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2028, '65f020632bc46470c104b76f', 2028, 'base', true, 52.33)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2029, '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-FAIR-50ML', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2029, '65f020632bc46470c104b76f', 2029, 'base', true, 49.35)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2030, '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-FAIR-100ML', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2030, '65f020632bc46470c104b76f', 2030, 'base', true, 51.70)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2031, '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-MEDIUM-30ML', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2031, '65f020632bc46470c104b76f', 2031, 'base', true, 54.05)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2032, '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-MEDIUM-50ML', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2032, '65f020632bc46470c104b76f', 2032, 'base', true, 56.40)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2033, '65f020632bc46470c104b76f', 64, 'SKU-YSL-MAKE-RPCLIP64-MEDIUM-100ML', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2033, '65f020632bc46470c104b76f', 2033, 'base', true, 58.75)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2034, '65f020632bc46470c104b76f', 65, 'SKU-GUER-FRAG-MONGUERLAIN65-50ML', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2034, '65f020632bc46470c104b76f', 2034, 'base', true, 50.93)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2035, '65f020632bc46470c104b76f', 65, 'SKU-GUER-FRAG-MONGUERLAIN65-100ML', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2035, '65f020632bc46470c104b76f', 2035, 'base', true, 53.35)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2036, '65f020632bc46470c104b76f', 67, 'SKU-NARS-MAKE-SHEERGLOW67-MEDIUM', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2036, '65f020632bc46470c104b76f', 2036, 'base', true, 54.08)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2037, '65f020632bc46470c104b76f', 67, 'SKU-NARS-MAKE-SHEERGLOW67-DEEP', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2037, '65f020632bc46470c104b76f', 2037, 'base', true, 56.65)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2038, '65f020632bc46470c104b76f', 68, 'SKU-LRP-SKIN-EFFADUO68-50ML', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2038, '65f020632bc46470c104b76f', 2038, 'base', true, 55.65)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2039, '65f020632bc46470c104b76f', 69, 'SKU-KERA-HAIR-CHRONOHUILE69-50ML', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2039, '65f020632bc46470c104b76f', 2039, 'base', true, 57.23)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2040, '65f020632bc46470c104b76f', 69, 'SKU-KERA-HAIR-CHRONOHUILE69-100ML', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2040, '65f020632bc46470c104b76f', 2040, 'base', true, 59.95)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2041, '65f020632bc46470c104b76f', 70, 'SKU-YSL-FRAG-MONPARIS70-50ML', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2041, '65f020632bc46470c104b76f', 2041, 'base', true, 58.80)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2042, '65f020632bc46470c104b76f', 70, 'SKU-YSL-FRAG-MONPARIS70-100ML', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2042, '65f020632bc46470c104b76f', 2042, 'base', true, 61.60)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2043, '65f020632bc46470c104b76f', 70, 'SKU-YSL-FRAG-MONPARIS70-200ML', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2043, '65f020632bc46470c104b76f', 2043, 'base', true, 64.40)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2044, '65f020632bc46470c104b76f', 72, 'SKU-SHIS-MAKE-MINBLUSH72-FAIR-50ML', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2044, '65f020632bc46470c104b76f', 2044, 'base', true, 61.95)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2045, '65f020632bc46470c104b76f', 72, 'SKU-SHIS-MAKE-MINBLUSH72-MEDIUM-30ML', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2045, '65f020632bc46470c104b76f', 2045, 'base', true, 64.90)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2046, '65f020632bc46470c104b76f', 72, 'SKU-SHIS-MAKE-MINBLUSH72-MEDIUM-50ML', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2046, '65f020632bc46470c104b76f', 2046, 'base', true, 67.85)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2047, '65f020632bc46470c104b76f', 73, 'SKU-NARS-MAKE-CLIMAXMASC73-MEDIUM', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2047, '65f020632bc46470c104b76f', 2047, 'base', true, 63.53)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2048, '65f020632bc46470c104b76f', 73, 'SKU-NARS-MAKE-CLIMAXMASC73-DEEP', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2048, '65f020632bc46470c104b76f', 2048, 'base', true, 66.55)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2049, '65f020632bc46470c104b76f', 73, 'SKU-NARS-MAKE-CLIMAXMASC73-ROSE', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2049, '65f020632bc46470c104b76f', 2049, 'base', true, 69.58)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2050, '65f020632bc46470c104b76f', 74, 'SKU-LRP-SKIN-TOLSENSCR74-50ML', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2050, '65f020632bc46470c104b76f', 2050, 'base', true, 65.10)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2051, '65f020632bc46470c104b76f', 74, 'SKU-LRP-SKIN-TOLSENSCR74-100ML', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2051, '65f020632bc46470c104b76f', 2051, 'base', true, 68.20)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2052, '65f020632bc46470c104b76f', 75, 'SKU-KERA-HAIR-CIMENTHERM75-50ML', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2052, '65f020632bc46470c104b76f', 2052, 'base', true, 66.68)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2053, '65f020632bc46470c104b76f', 77, 'SKU-GUER-FRAG-AAMANDBAS77-50ML', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2053, '65f020632bc46470c104b76f', 2053, 'base', true, 69.83)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2054, '65f020632bc46470c104b76f', 77, 'SKU-GUER-FRAG-AAMANDBAS77-100ML', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2054, '65f020632bc46470c104b76f', 2054, 'base', true, 73.15)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2055, '65f020632bc46470c104b76f', 78, 'SKU-SHIS-HAIR-TSUBAKIMASK78-50ML', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2055, '65f020632bc46470c104b76f', 2055, 'base', true, 71.40)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2056, '65f020632bc46470c104b76f', 78, 'SKU-SHIS-HAIR-TSUBAKIMASK78-100ML', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2056, '65f020632bc46470c104b76f', 2056, 'base', true, 74.80)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2057, '65f020632bc46470c104b76f', 78, 'SKU-SHIS-HAIR-TSUBAKIMASK78-200ML', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2057, '65f020632bc46470c104b76f', 2057, 'base', true, 78.20)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2058, '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-FAIR-50ML', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2058, '65f020632bc46470c104b76f', 2058, 'base', true, 72.98)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2059, '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-MEDIUM-30ML', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2059, '65f020632bc46470c104b76f', 2059, 'base', true, 76.45)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2060, '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-MEDIUM-50ML', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2060, '65f020632bc46470c104b76f', 2060, 'base', true, 79.93)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2061, '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-DEEP-30ML', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2061, '65f020632bc46470c104b76f', 2061, 'base', true, 83.40)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2062, '65f020632bc46470c104b76f', 79, 'SKU-NARS-MAKE-AFTERGLOWLB79-DEEP-50ML', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2062, '65f020632bc46470c104b76f', 2062, 'base', true, 86.88)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2063, '65f020632bc46470c104b76f', 80, 'SKU-LRP-SKIN-LIPIAPM80-50ML', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2063, '65f020632bc46470c104b76f', 2063, 'base', true, 74.55)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2064, '65f020632bc46470c104b76f', 80, 'SKU-LRP-SKIN-LIPIAPM80-100ML', 6, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2064, '65f020632bc46470c104b76f', 2064, 'base', true, 78.10)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2065, '65f020632bc46470c104b76f', 82, 'SKU-YSL-MAKE-ALLHOURSFND82-MEDIUM', 0, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2065, '65f020632bc46470c104b76f', 2065, 'base', true, 77.70)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2066, '65f020632bc46470c104b76f', 83, 'SKU-GUER-SKIN-SUPERAQUA83-50ML', 11, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2066, '65f020632bc46470c104b76f', 2066, 'base', true, 79.28)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2067, '65f020632bc46470c104b76f', 84, 'SKU-SHIS-FRAG-GINZAEDP84-50ML', 7, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2067, '65f020632bc46470c104b76f', 2067, 'base', true, 80.85)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2068, '65f020632bc46470c104b76f', 84, 'SKU-SHIS-FRAG-GINZAEDP84-100ML', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2068, '65f020632bc46470c104b76f', 2068, 'base', true, 84.70)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2069, '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-FAIR-50ML', 3, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2069, '65f020632bc46470c104b76f', 2069, 'base', true, 82.43)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2070, '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-FAIR-100ML', 22, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2070, '65f020632bc46470c104b76f', 2070, 'base', true, 86.35)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2071, '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-MEDIUM-30ML', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2071, '65f020632bc46470c104b76f', 2071, 'base', true, 90.28)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2072, '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-MEDIUM-50ML', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2072, '65f020632bc46470c104b76f', 2072, 'base', true, 94.20)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2073, '65f020632bc46470c104b76f', 85, 'SKU-NARS-MAKE-LRSPWD85-MEDIUM-100ML', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2073, '65f020632bc46470c104b76f', 2073, 'base', true, 98.13)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2074, '65f020632bc46470c104b76f', 87, 'SKU-KERA-HAIR-DISCFLUID87-50ML', 5, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2074, '65f020632bc46470c104b76f', 2074, 'base', true, 85.58)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2075, '65f020632bc46470c104b76f', 87, 'SKU-KERA-HAIR-DISCFLUID87-100ML', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2075, '65f020632bc46470c104b76f', 2075, 'base', true, 89.65)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2076, '65f020632bc46470c104b76f', 87, 'SKU-KERA-HAIR-DISCFLUID87-200ML', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2076, '65f020632bc46470c104b76f', 2076, 'base', true, 93.73)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2077, '65f020632bc46470c104b76f', 88, 'SKU-YSL-FRAG-YEDP88-50ML', 18, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2077, '65f020632bc46470c104b76f', 2077, 'base', true, 87.15)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2078, '65f020632bc46470c104b76f', 88, 'SKU-YSL-FRAG-YEDP88-100ML', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2078, '65f020632bc46470c104b76f', 2078, 'base', true, 91.30)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2079, '65f020632bc46470c104b76f', 89, 'SKU-GUER-MAKE-LESSENTIEL89-MEDIUM', 14, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2079, '65f020632bc46470c104b76f', 2079, 'base', true, 88.73)
on conflict (product_price_id) do nothing;

INSERT INTO inventory.product_availability (product_avail_id, store_merchant_id, product_id, sku, quantity, available, quantity_ord_min, quantity_ord_max)
VALUES (2080, '65f020632bc46470c104b76f', 89, 'SKU-GUER-MAKE-LESSENTIEL89-DEEP', 9, true, 1, 0)
on conflict (product_avail_id) do nothing;
INSERT INTO inventory.product_price (product_price_id, store_merchant_id, product_avail_id, product_price_code, default_price, product_price_amount)
VALUES (2080, '65f020632bc46470c104b76f', 2080, 'base', true, 92.95)
on conflict (product_price_id) do nothing;

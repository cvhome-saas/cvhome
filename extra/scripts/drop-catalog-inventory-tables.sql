-- Manual, post-verification cleanup after the catalog/inventory split.
--
-- Run this ONLY after confirming the inventory service serves the copied data correctly
-- (row counts match, no null skus in inventory.product_availability, checkout reserve/commit/release works).
-- Until then the old catalog tables stay as the migration's source of truth.
--
--   psql "$DATABASE_URL" -f extra/scripts/drop-catalog-inventory-tables.sql

begin;

drop table if exists catalog.product_reservation_line;
drop table if exists catalog.product_reservation;
drop table if exists catalog.product_price_description;
drop table if exists catalog.product_price;
drop table if exists catalog.product_availability;

-- The Shopizer-era variant machinery, dead since the catalog/inventory split and not revived by the
-- variant rework — that built a different model (see below).
--
-- !! catalog.product_variant is NOT dropped here, and must never be. !!
--
-- The rework reused the name: `catalog.product_variant` is now the LIVE table holding every product's
-- sellable sku, and dropping it destroys the whole catalogue's variants. The tables below are the old
-- ones the new model does not use. If you are looking at a database that still carries the pre-split
-- `product_variant` (an id/sku/variation shape rather than one with `option_signature`), it predates
-- the rework — recreate the database rather than dropping into a live name.
drop table if exists catalog.product_var_image_description;
drop table if exists catalog.product_var_image;
drop table if exists catalog.product_variant_group;
drop table if exists catalog.product_variation;

commit;

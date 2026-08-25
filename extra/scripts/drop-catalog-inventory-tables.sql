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

-- variant tables, deprecated with the single-product model (DDL kept in store-pod/catalog-deprecated)
drop table if exists catalog.product_var_image_description;
drop table if exists catalog.product_var_image;
drop table if exists catalog.product_variant;
drop table if exists catalog.product_variant_group;
drop table if exists catalog.product_variation;

commit;

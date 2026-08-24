# catalog-deprecated

Parked code from the catalog/inventory split (2026-08). **Not registered in `settings.gradle` — nothing here
compiles.** Same pattern as `store-pod/content-deprecated`: reference only.

## What is here

- **Variant / variation machinery** the single-product UIs never used: `ProductVariant`, `ProductVariantGroup`,
  `ProductVariantImage(+Description)`, `ProductVariation` entities, their repositories, services, facades, mappers,
  the v2 `ProductVariantApi` / `ProductVariantGroupApi` / `ProductVariationApi` controllers, the variant DTOs from
  `catalog-commons`, and the variant-specific exceptions.
- **`deprecated-ddl.sql`** — the `catalog.*` DDL removed from catalog's `schema.sql`: the variant tables plus the
  pre-split `product_availability` / `product_price(+description)` / `product_reservation(+line)` tables, whose data
  now lives in the `inventory` schema (see `store-pod/inventory`, `init-sql/migrate-from-catalog.sql`).

## Where the live functionality went

- Availability/stock, pricing and reservations: **`store-pod/inventory`** (port 8126, schema `inventory`), keyed by
  sku. `product_availability.product_variant` survives there as a dormant column for when variants return.
- Product catalog: stays in **`store-pod/catalog`**, now free of price/quantity in its responses.

## Reintroducing variants

1. Re-register variant modules/code (start from this dir, but expect to rewrite against the split): variant entities
   go back to catalog, while per-variant stock/price rows belong to inventory's `product_availability`
   (`product_variant` column + variant skus).
2. The old catalog tables still exist in deployed databases until the manual drop script
   (`extra/scripts/drop-catalog-inventory-tables.sql`) has been run — after that, only `deprecated-ddl.sql`
   documents their shape.
3. The `CATALOG.PRODUCT_VARIANT.*` / `CATALOG.PRODUCT_VARIATION.*` error codes were pruned from `CatalogErrors`
   with the code; the exception classes here still name them, so re-add the constants when re-registering.

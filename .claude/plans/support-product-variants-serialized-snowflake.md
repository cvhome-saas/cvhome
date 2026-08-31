# Product Variants — catalog, inventory, checkout, console-ui, landing-ui

## Context

The platform today models only **simple products**: one product = one SKU, with price and stock held by the
inventory service under that SKU. Merchants need **variants** — a product (shirt) sold in combinations of
options (Color × Size), where every variant has its own SKU, its own price, and its own stock. Options are
store-wide reusable definitions; **each product declares which options it varies by** (Shopify/Medusa style).

Nothing is in production yet, so schemas can be designed freely (only the seeded demo data must keep
working). The old Shopizer model in `store-pod/catalog-deprecated` is a reference for what NOT to repeat: it
conflated priced add-ons / reified option pairs / variant SKUs, and hard-capped variants at two axes.

Exploration established a favorable starting position:

- **Checkout is already SKU-addressed end to end** — cart lines, reservation entries and order lines all key
  on a SKU string; a variant modeled as its own SKU flows through cart → reserve → order unchanged.
- **Inventory already supports N SKUs per product** — rows keyed `(store, sku)`, `productId` informational;
  console and storefront already bulk-fetch by SKU list.
- **landing-ui already ships a complete (dead) variant UI**: types, `use-product-purchase` selection machine,
  option chips in all 12 themes' `BuyBox.tsx`, `hasVariants` card behavior, facet rail, i18n in 5 locales.
  Its types mirror the deprecated DTO shape and must be reshaped.
- **console-ui has no variant UI**, documented as a deliberate gap (`models/catalog.ts` L22-26, `lessons.md`
  ~L1363/~L1470 — which drafts the expected product-type attribute contract).

### Decisions taken with the user

1. **Option vocabulary is store-wide and reusable** (Color defined once, translated once, shared value ids —
   which is what keeps id-based faceting possible). **Options attach per PRODUCT, not per product type**
   (Shopify/Medusa style — revised 2026-08-31, superseding the earlier per-type decision): any product can
   declare the options it varies by; **product types are untouched by this feature**.
2. **No per-variant images in v1** — product gallery shared; schema keeps a dormant nullable
   `product_variant_id` column on `product_image` so the later addition is additive.
3. **Option-value faceting/filtering in the storefront listing IS in v1 scope.**
4. **Exactly one default variant per product**, DB-enforced — it supplies the card/list price and the
   PDP preselection.
5. **One `available` flag, owned by inventory** — the catalog variant row carries no availability switch;
   whether a sku is sellable is decided by its inventory row alone (PDP chips grey from enrichment).
6. **Option-value swatches / display types are a later phase** — additive columns when they come, nothing now.

### Core design decisions

- **N-axis model**: `product_option` (store-scoped vocabulary) → `product_option_value` (scoped to its
  option) → `product_option_assignment` (product↔option join, ordered — the axes THIS product varies by) →
  `product_variant` (product child, own sku unique per store) → `product_variant_option_value` (one value
  per option per variant). Every assigned option is variant-defining; non-variant product attributes are a
  separate future feature, not smuggled in here.
- **Duplicate-combination guard**: canonical `option_signature` on `product_variant` — sorted option-value
  ids joined with `-` (e.g. `"1203-1517"`), `unique (product_id, option_signature)`. Computed in the mapper;
  debuggable in psql; DB-enforced under concurrency. Not used for filtering (the join table is).
- **Filter semantics**: OR within an option, AND across options, **anchored to a single variant** — "Red AND
  L" means one variant is both, not "owns a Red variant and owns an L variant".
- **Option-edit safety**: deleting a store option or one of its values is blocked with 409
  (`ProductOptionInUseException`) while any product assignment or variant references it.
- **Variants are a dedicated whole-set-replace API** (`PUT /private/product/{id}/variants`), not embedded in
  `PersistableProductDefinition` — same pattern as the gallery PUT; keeps the wizard incremental. The payload
  carries the axes AND the combinations (`{options: [optionCode…], variants: […]}`), written atomically:
  assignments and variants can never desync, and a product with assignments always has ≥1 combination
  variant **by construction** — no separate publishability check needed.
- **Guardrails**: max 4 assigned options per product, max 100 variants per product, enforced server-side
  (422, `VariantLimitExceededException`) — protects the matrix UI, the facet queries and the PDP
  availability call; trivially relaxable later.
- **Uniform variant model (Shopify-style)**: the product is a **pure definition** (copy, type, brand,
  categories, images, flags); **every product owns at least one variant**, and sku/price/stock always live at
  the variant level. A product with no variant-defining options owns exactly one **default variant** (empty
  option set, `option_signature = 'DEFAULT'`) carrying the sku the merchant typed. One read path, no
  conditional joins, no dual logic: reading a product is always product + its variants, and the sellable unit
  is always a variant sku.
- **`catalog.product.sku` is dropped** — `unique (store_merchant_id, sku)` lives on `product_variant` alone.
  `PersistableProductDefinition.sku` stays on the console form and maps to the default variant's sku at
  create time, so the merchant UX for a simple product is unchanged.
- **Read model — two altitudes, variants load only where they're used**:
  - *Listing/card altitude*: `ReadableMinimalProduct.sku` = the **default variant's sku** and
    `variantCount` (int). That's all a card gets — filled by ONE batched aggregate query over the page of
    ids (`product_id → default sku, count(*)`), never by loading the variant rows. No `hasVariants` flag is
    stored anywhere; UIs derive "has options" from `variantCount > 1`.
  - *PDP altitude*: `ReadableProduct` carries full `options[]` + `variants[]` (≥1), hydrated with one
    IN-query for that product only.
  - A nullable `variant` selection block on `ReadableMinimalProduct` carries the chosen option labels when a
    read is addressed by a combination sku (powers cart/order lines).
- **Bulk everywhere**: catalog gains `GET /detailed-products?skus=`, inventory gains
  `POST /api/v1/availability/query` (JSON body — for a PDP whose variant matrix exceeds what a GET query
  string comfortably carries) and `PUT /private/inventory/bulk` (a 20-variant matrix save ≠ 20 gateway
  round-trips);
  checkout's `ProductDetailsComposer` becomes batch (today it is called once per cart line at 5 call sites).
- **Rewrite over patch** — where this feature touches code that only exists to work around the old model,
  rewrite it to the target architecture instead of layering on top:
  - *Checkout*: the Shopizer-era populator chain (`OrderProductPopulator`, `ReadableOrderProductPopulator`,
    per-line composer calls) is **rewritten** into the mapper style the rewritten catalog uses — one
    `OrderLineMapper`/`CartLineMapper` fed a prefetched `Map<sku, ProductDetails>` from a single batch
    composer call per cart/order. No populator keeps its own fetch.
  - *Console*: the silent best-effort save legs (`catchError → false` on category/inventory writes) are
    replaced by an explicit save orchestration — catalog first, then inventory bulk; a failed leg surfaces a
    retryable error state, never a silently half-saved product.
  - *Landing-ui*: `enrichProducts` today mutates `Product` in place to reconstruct pre-split embedded fields.
    Formalize the merge: a typed `PricedProduct`/pricing fields section in `libs/types` written by one
    documented enrichment function — same seam, explicit contract, no ad-hoc mutation scattering.

### Product lifecycle — one invariant, no branches

Invariant: **every product owns ≥1 variant; a product with no variant-defining options owns exactly one
default variant** (`option_signature = 'DEFAULT'`, zero `product_variant_option_value` rows — the
`unique (product_id, option_signature)` constraint therefore also guarantees at most one default variant).

- **Create (simple product)**: `POST /private/product` creates the product row **and** its default variant in
  the same transaction (sku taken from the payload); the console then upserts one inventory row for that sku
  — the merchant sees exactly today's flow.
- **Read**: always product + variants. The listing's batched variants query and the PDP hydration return ≥1
  row for every product; catalog has no `hasVariants` branch and no conditional join anywhere.
- **Add real variants**: in the product form, pick the options this product varies by (from the store
  vocabulary), generate the matrix → `PUT /private/product/{id}/variants` **replaces axes + variant set
  atomically** (the default variant is retired; the console seeds the first combination from the default
  variant's sku/price/qty so stock can carry over), then bulk inventory upsert writes one price/stock row
  per combination sku. Exactly one variant is flagged default (DB-enforced) — the service falls back to the
  lowest sort order when the payload flags none.
- **Remove all combinations**: `PUT` with empty options + variants restores a single default variant — the
  product sells by one sku again.
- **Storefront**: a single-variant product resolves its variant immediately (nothing to select); a
  multi-option product requires a selection — both flow through the same `use-product-purchase` path and the
  same add-to-cart-by-sku call. Inventory/checkout see only `(store, sku)` either way.

### Known debt fixed along the way

- `inventory.product_availability`: drop dormant `product_variant`/`region`/`region_variant` columns + legacy
  unique constraint; add the missing `unique (store_merchant_id, sku)`; `sku` NOT NULL.
- `OrderProductPopulator.java:45` writes placeholder `"Product {sku}"` as the order line name — fix; widen
  `order_product.product_name` to varchar(255).
- Checkout dead schema: drop `shopping_cart_item.product_variant`, orphan `shopping_cart_attr_item` table,
  never-written `order_product_attribute` table + entity (replaced by a proper snapshot table).

---

## 1. DB schema

**No migrations — final shape only.** Nothing is in production: every change below is made by **editing the
`CREATE TABLE` statements in `init-sql/schema.sql`** (the source of truth) to the final shape. Existing
dev/QA databases are dropped and recreated (`lcl` postgres volume reset) and reseeded — no `ALTER`-style
migration blocks are added for this feature (that idempotent-alter convention exists for live schemas; we
deliberately don't need it here). This also makes the one-off catalog→inventory migration obsolete: **delete
`inventory-service/init-sql/migrate-from-catalog.sql` and the `CatalogDataMigration` ApplicationRunner** —
fresh databases are initialized directly in the final shape.

### catalog — `store-pod/catalog/catalog-service/src/main/resources/init-sql/schema.sql`

```sql
create table if not exists catalog.product_option (
    product_option_id bigint       not null primary key,
    date_created      timestamp(6), date_modified timestamp(6), updt_id varchar(60),
    code              varchar(100) not null,
    sort_order        integer,
    store_merchant_id varchar(50)  not null,
    constraint uk_product_option_code unique (store_merchant_id, code)
);
create table if not exists catalog.product_option_description (
    description_id    bigint       not null primary key,
    date_created      timestamp(6), date_modified timestamp(6), updt_id varchar(60),
    name              varchar(120) not null,
    language_code     varchar(6)   not null,
    product_option_id bigint       not null references catalog.product_option,
    constraint uk_product_option_desc unique (product_option_id, language_code)
);
create table if not exists catalog.product_option_value (
    product_option_value_id bigint       not null primary key,
    date_created timestamp(6), date_modified timestamp(6), updt_id varchar(60),
    code                    varchar(100) not null,
    sort_order              integer,
    product_option_id       bigint       not null references catalog.product_option,
    constraint uk_product_option_value_code unique (product_option_id, code)
);
create table if not exists catalog.product_option_value_description (
    description_id          bigint       not null primary key,
    date_created timestamp(6), date_modified timestamp(6), updt_id varchar(60),
    name                    varchar(120) not null,
    language_code           varchar(6)   not null,
    product_option_value_id bigint       not null references catalog.product_option_value,
    constraint uk_product_option_value_desc unique (product_option_value_id, language_code)
);
create table if not exists catalog.product_option_assignment (
    product_id        bigint  not null references catalog.product,
    product_option_id bigint  not null references catalog.product_option,
    sort_order        integer not null,
    primary key (product_id, product_option_id)
);
create index if not exists product_option_assignment_option_idx
    on catalog.product_option_assignment (product_option_id);    -- delete-guard scan

create table if not exists catalog.product_variant (
    product_variant_id bigint       not null primary key,
    date_created timestamp(6), date_modified timestamp(6), updt_id varchar(60),
    store_merchant_id  varchar(50)  not null,
    product_id         bigint       not null references catalog.product,
    sku                varchar(255) not null,
    sort_order         integer,
    default_variant    boolean      not null default false,
    option_signature   varchar(255) not null,   -- 'DEFAULT' for the default variant (no options)
    constraint uk_product_variant_sku unique (store_merchant_id, sku),
    constraint uk_product_variant_signature unique (product_id, option_signature)
);
create index if not exists product_variant_product_idx on catalog.product_variant (product_id);
-- exactly one default variant per product (the card/list price, the PDP preselection)
create unique index if not exists uk_product_variant_default
    on catalog.product_variant (product_id) where default_variant;
-- NOTE: no `available` column — sellability is inventory's flag, one flag only (decision 5)

-- catalog.product: EDIT the existing create table — remove the sku column and its
-- unique constraint UK8y3h56fhn50m59svlocxwqnn0. The product is a pure definition now.

create table if not exists catalog.product_variant_option_value (
    product_variant_id      bigint not null references catalog.product_variant,
    product_option_id       bigint not null references catalog.product_option,
    product_option_value_id bigint not null references catalog.product_option_value,
    primary key (product_variant_id, product_option_id)          -- one value per option
);
create index if not exists pvov_value_variant_idx
    on catalog.product_variant_option_value (product_option_value_id, product_variant_id);

-- catalog.product_image: EDIT the create table — add nullable product_variant_id bigint (dormant, v2)
```

**Search document**: extend the `catalog.product_search_source` view — lateral
`string_agg(v.sku, ' ')` over `catalog.product_variant` **replaces** `p.sku` in the weight-B `concat_ws`
(alongside `p.ref_sku`); with the uniform model, this covers the default variant's sku and every combination
sku with one expression. Variant writes re-save the parent `Product` with `searchIndexStale()` so the
existing outbox → `refresh_product_search_index` path reindexes. After the view change, one
`POST /api/v2/private/products/search-index/rebuild` per store (document in the .http file).

**Filter query shape** (from `ProductSpecifications`, keeping the EXISTS-not-join discipline of
`inCategories` — one row per product, no `distinct`, relevance ordering stays legal):

```sql
where exists (select 1 from catalog.product_variant v
              where v.product_id = p.product_id
                and exists (select 1 from catalog.product_variant_option_value x
                            where x.product_variant_id = v.product_variant_id
                              and x.product_option_value_id in (:colorValueIds))
                and exists (... :sizeValueIds ...))
```
`product_variant_product_idx` serves the outer correlated scan; the PK `(variant_id, option_id)` serves the
inner probes; `pvov_value_variant_idx` lets the planner drive from a selective value instead.

**Facet counts**: `ProductFacetRepository` pattern — one grouped criteria query
(`join variants → optionValues`, group by value id, `countDistinct(product.id)`); labels resolved afterwards
by loading counted value ids + descriptions in one query.

### inventory — `store-pod/inventory/inventory-service/src/main/resources/init-sql/schema.sql`

EDIT the `product_availability` create table to its final shape:
- remove `product_variant`, `region`, `region_variant` and the legacy unique constraint
  `UK3cq0pcvlrorbgahh1r1o6fao5`;
- `sku varchar(255) not null`;
- `constraint uk_prd_avail_store_sku unique (store_merchant_id, sku)` (delete the now-redundant
  `prd_avail_store_sku_idx` index statement).

(Also update the `Inventory` entity javadoc; `migrate-from-catalog.sql` + `CatalogDataMigration` are deleted
per the no-migration stance above.) The dormant `weight`/`height`/`length`/`width` columns are **kept** —
per-variant shipping specs
(an XL hoodie ships heavier than an S, Shopify models this per variant) become a later flip-on with no
migration. A variant is simply one more `product_availability` row keyed by the variant sku — `getBySkus`,
reservations (`lockBySku`) and `SkuInventory` need **no** functional change. The inventory `available`
flag is THE availability switch for a sku (catalog keeps none).

### checkout — `store-pod/checkout/checkout-service/src/main/resources/init-sql/schema.sql`

EDIT the existing create tables: `shopping_cart_item` loses the `product_variant` column; the
`shopping_cart_attr_item` and `order_product_attribute` create statements are **deleted**;
`order_product.product_name` becomes `varchar(255)`. One new table:

```sql
create table if not exists checkout.order_product_option (      -- label snapshot at placement
    order_product_option_id bigint       not null primary key,
    order_product_id        bigint       not null references checkout.order_product,
    option_code             varchar(100) not null,
    option_name             varchar(120) not null,
    value_code              varchar(100) not null,
    value_name              varchar(120) not null,
    sort_order              integer
);
create index if not exists order_product_option_line_idx
    on checkout.order_product_option (order_product_id);
```

---

## 2. Catalog module

Follow the rewritten catalog pattern exactly: `services/<domain>/{XService, XServiceImpl, XMapper}` (static
mapper unless collaborators needed), store always part of the lookup, typed `XNotFoundException.of(id, store)`,
description merge by language map so ids survive, `Pages.toReadable`. Reference: `services/type/`.

**Entities** (`catalog-core/.../entity/`): `ProductOption` (+`ProductOptionDescription`), `ProductOptionValue`
(+`ProductOptionValueDescription`) — clone the `ProductType`/`ProductTypeDescription` shape; `ProductOption`
has `@OneToMany(cascade=ALL, orphanRemoval=true) Set<ProductOptionValue> values`. `ProductOptionAssignment`
— `@EmbeddedId(productId, optionId)` join entity with `sortOrder` (**`ProductType` is untouched**).
`ProductVariant` — embedded `StoreMerchantId`, lazy `@ManyToOne Product`, sku, sortOrder,
defaultVariant, optionSignature, `Set<ProductVariantOptionValue>` (cascade ALL) — no availability field.
`Product` **loses its `sku` field** and gains `@OneToMany(mappedBy="product", cascade=REMOVE)
Set<ProductVariant> variants` plus a `Set<ProductOptionAssignment> optionAssignments` (both managed by the
variant service, like `images`).

**Repositories**: `ProductOptionRepository` (fetch-join listing, `findByStoreAndCode`, reference-check
exists queries); `ProductVariantRepository` (`findByProductIdInHydrated` fetch-joining
`optionValues.optionValue`, `findByStoreAndSku`, `findVariantSummaryByProductIdIn` aggregate projection —
`(product_id, default variant sku, count(*))` for the listing, no variant rows loaded);
`ProductSpecifications.hasOptionValues(Map<Long, List<Long>> valuesByOption)`, and `skuLike` re-targeted to
an EXISTS over `product_variant.sku` (the product no longer has one);
`ProductFacetRepository.countByOptionValue(spec)`.

**Services**:
- `services/option/` — CRUD; delete guarded by reference checks → 409.
- `services/variant/` — `replaceAll(store, productId, PersistableVariantSet)` where the payload is
  `{options: [optionCode…], variants: [PersistableProductVariant…]}`, applied atomically: validate the axes
  (options exist in the store, ≤4) and every variant against them (exactly one value per assigned option,
  values belong to their option, ≤100 variants, non-empty when axes are declared), compute
  `optionSignature`, diff by id (existing ids keep rows/audit), guarantee exactly one `defaultVariant`
  (fall back to lowest sort order), re-save parent with `searchIndexStale()`. Empty options + variants
  restores the single default variant (invariant ≥1 per product; its sku taken from the request or kept
  from the retiring first variant). Plus `list(store, productId, language)`.
- `services/product/ProductServiceImpl` — create: persists the product **and its default variant** in one
  transaction (sku from `PersistableProductDefinition.sku`); listing: after `findAllHydrated`, one
  `findVariantSummaryByProductIdIn` fills each product's `sku` (default variant) + `variantCount`;
  `ProductFilter`/`ProductSearchCriteria` gain `optionValueIds` (grouped by owning option via one lookup,
  then `hasOptionValues`). PDP: hydrate `options[]` + full `variants[]` with one IN-query. `getBySku`
  resolves **only** through `ProductVariantRepository.findByStoreAndSku` — every sellable sku is a variant
  sku — and returns the owning product, with the `variant` selection block filled (localized option/value
  labels) when the sku is a combination variant.
- `ProductSearchServiceImpl` — option-value facet buckets via `countByOptionValue` + label load.
  **Search results are product-level**: the index stays one row per product (variant skus are folded into
  the document, weight B), so a name or sku query returns ONE hit per product — the default-variant card —
  never one row per variant; the user narrows to a combination on the PDP. A query that matched a
  combination variant's sku still resolves to the parent product; `ReadableProductSuggestion` gains an
  optional `matchedVariantSku` so suggest can deep-link the PDP with that variant preselected (`?sku=`).

**DTOs** (`catalog-commons/.../model/`):
- `model/option/`: `PersistableProductOption {code, sortOrder, descriptions[], values[]}`,
  `ReadableProductOption`, `ReadableProductOptionValue`.
- `model/type/`: **unchanged** — product types carry nothing new.
- `model/product/`: `ReadableMinimalProduct.sku` stays on the wire (the default/resolved variant's sku), plus
  `variantCount` (int) and a nullable `ReadableVariantSelection variant {sku, optionValues: [{optionId,
  optionCode, optionName, valueId, valueCode, valueName, sortOrder}]}`. Variant rows themselves never ship
  at listing altitude. `ReadableProduct` + `options[]` (this product's assigned options in order, carrying
  only values actually used by its variants — no dead chips) and full `variants[]`
  (`{id, sku, sortOrder, defaultVariant, optionValueIds}` — no availability field; sellability comes from
  inventory). `PersistableVariantSet {options: [code…], variants: [PersistableProductVariant…]}`;
  `PersistableProductVariant` (sku same `@Pattern` as before) / `ReadableProductVariantDefinition` (adds
  resolved labels for the console matrix). `ReadableSearchFacets` + `options: [{optionId, code, name,
  sortOrder, values: [{valueId, code, name, count}]}]`.
- `errors/`: `ProductOptionNotFoundException`, `DuplicateProductOptionException`,
  `ProductOptionInUseException`, `ProductVariantNotFoundException`, `DuplicateVariantSkuException`,
  `DuplicateVariantCombinationException`, `VariantOptionsInvalidException`,
  `VariantLimitExceededException` (422 — >4 options or >100 variants) + codes in `CatalogErrors`.

**APIs** (`catalog-service/.../api/`), all private ones `hasPermission(...,'STORE-POD.CATALOG.*')`:
- New `api/v1/ProductOptionApi`: `GET /api/v1/private/product/options` (paged), `GET/POST/PUT/DELETE
  /api/v1/private/product/option[/{id}]` (DELETE → 409 when in use).
- New `api/v2/ProductVariantApi`: `GET /api/v2/private/product/{id}/variants`,
  `PUT /api/v2/private/product/{id}/variants` (body `PersistableVariantSet` — axes + combinations, atomic).
- Changed (additive): `ProductApiV2` listing/search/PDP/definition responses grow as above.
  **`ProductTypeApi` is untouched.**
- `ExternalProductApi` + `catalog-external-api/ExternalProductService`: new
  `GET /api/v1/detailed-products?skus=a,b,c → List<ReadableMinimalProduct>` (missing skus absent, mirroring
  inventory); `getDetailedProduct` resolves variant skus too.
- `.http`: new `catalog-service/http/product-option-api.http`, `product-variant-api.http`; blocks in
  `product-api.http` (bulk read, optionValueIds examples).

**Wire contract — the three shapes that matter** (catalog carries no price/stock anywhere; clients merge it
from the inventory availability call):

```jsonc
// PDP — GET /api/v2/product/name/aurora-shirt  (ReadableProduct, excerpt)
{
  "id": 120,
  "sku": "SHIRT-RED-M",          // the default variant's sku
  "variantCount": 4,
  "description": { "name": "Aurora Shirt" },
  "options": [                    // this product's axes, ordered; only values its variants use
    { "id": 7, "code": "color", "name": "Color", "sortOrder": 0,
      "optionValues": [ { "id": 71, "code": "red",  "name": "Red",  "sortOrder": 0 },
                        { "id": 72, "code": "blue", "name": "Blue", "sortOrder": 1 } ] },
    { "id": 8, "code": "size", "name": "Size", "sortOrder": 1,
      "optionValues": [ { "id": 81, "code": "m", "name": "M" }, { "id": 82, "code": "l", "name": "L" } ] }
  ],
  "variants": [
    { "id": 501, "sku": "SHIRT-RED-M",  "defaultVariant": true,  "sortOrder": 0, "optionValueIds": [71, 81] },
    { "id": 502, "sku": "SHIRT-RED-L",  "defaultVariant": false, "sortOrder": 1, "optionValueIds": [71, 82] },
    { "id": 503, "sku": "SHIRT-BLUE-M", "defaultVariant": false, "sortOrder": 2, "optionValueIds": [72, 81] },
    { "id": 504, "sku": "SHIRT-BLUE-L", "defaultVariant": false, "sortOrder": 3, "optionValueIds": [72, 82] }
  ]
}

// Cart line / order line — the `variant` block on ReadableMinimalProduct when the read
// was addressed by a combination sku (this is what renders "Color: Red / Size: L"):
"variant": {
  "sku": "SHIRT-RED-L",
  "optionValues": [
    { "optionId": 7, "optionCode": "color", "optionName": "Color",
      "valueId": 71, "valueCode": "red", "valueName": "Red", "sortOrder": 0 },
    { "optionId": 8, "optionCode": "size", "optionName": "Size",
      "valueId": 82, "valueCode": "l", "valueName": "L", "sortOrder": 1 }
  ]
}

// Listing facets — ReadableSearchFacets.options (value ids are store-wide, so they
// round-trip straight back as ?optionValueIds=71,82):
"options": [
  { "optionId": 7, "code": "color", "name": "Color", "sortOrder": 0,
    "values": [ { "valueId": 71, "code": "red",  "name": "Red",  "count": 12 },
                { "valueId": 72, "code": "blue", "name": "Blue", "count": 8 } ] }
]
```

---

## 3. Inventory + checkout

**Inventory** (`inventory-service`): schema cleanup (§1). `InventoryApi` adds
`PUT /api/v1/private/inventory/bulk` (`List<PersistableSkuInventory>` = `PersistableInventory` + `sku`,
≤200 items, same `@PreAuthorize`) → `List<SkuInventory>`. `ExternalInventoryApi` adds
`POST /api/v1/availability/query` (public, `{skus: []}` body) delegating to the same service path as the GET.
`.http` blocks for both; new `extra/requests/landing-ui/inventory.http` and an inventory file under
`extra/requests/console-ui/org-admin/` (none exists today).

**Checkout** (`checkout-core`/`-service`):
- **Rewrite, not patch**: `ProductDetailsComposer` becomes batch-only —
  `Map<String, ProductDetails> getDetailedProducts(store, skus, language)`, one bulk catalog call + one bulk
  inventory call per cart/order. The Shopizer-era populator chain is retired: `OrderProductPopulator` /
  `ReadableOrderProductPopulator` are **replaced** by `OrderLineMapper` / cart-line mapping in
  `ReadableShoppingCartMapper` fed the prefetched map — no mapper or populator keeps its own per-line fetch.
  Call sites reworked: `ShoppingCartFacadeImpl`, `ShoppingCartServiceImpl.getPopulatedShoppingCart`,
  `ReadableShoppingCartMapper`, order placement, `ReadableOrderProductMapper`.
  `CachedExternalProductService` becomes bulk-aware (per-sku entries, fetch misses only).
- Order lines: write the real localized product name (fixing the `"Product {sku}"` placeholder, fallback to
  sku only when the catalog read failed); write `OrderProductOption` snapshot rows (new entity
  `entity/order/orderproduct/OrderProductOption`, `@OneToMany` on `OrderProduct`) from
  `ReadableMinimalProduct.variant.optionValues`.
- `ReadableOrderProduct` exposes the snapshot through the existing `ReadableOrderProductAttribute` list
  (`attributeName` = option name, `attributeValue` = value name) — console/storefront order views need no new DTO.
- `ReadableShoppingCartItem` needs **no change**: it extends `ReadableMinimalProduct` and
  `ReadableShoppingCartMapper.copyProductProperties` (`BeanUtils.copyProperties`,
  `ReadableShoppingCartMapper.java:151`) carries the new `variant` block onto cart lines automatically.
- Remove dead code: `ShoppingCartItem.variant` field, `OrderProductAttribute` entity.
- **Enforce `quantityOrderMinimum`/`quantityOrderMaximum` server-side** at add-to-cart/modify (the values
  arrive on every `SkuInventory` and are enforced nowhere today; the storefront hook only clamps
  client-side). Rejection uses the existing `ProductNotPurchasableException` family with a typed reason.

**Reservations under variants — verdict: works as-is, two hardenings.** The reservation flow is entirely
`(store, sku)`-addressed: `reserve` locks each `product_availability` row with `lockBySku`
(PESSIMISTIC_WRITE), decrements, and `commit`/`release`/expiry flip status or restore quantity per row.
Variant skus are just independent rows, so **no refactor is needed** — two variants of the same product
reserve/release independently and correctly. Hardenings to include:
1. **Deterministic lock order** — `ReservationServiceImpl.take()` loops the reservation entries; two
   concurrent orders holding overlapping sku sets in different orders can deadlock. Sort entries by sku
   before locking (one-line, cheap insurance now that multi-line variant carts are the norm).
2. **Retired-sku cleanup** — replacing a variant set retires skus whose inventory rows would orphan. Add
   `DELETE /api/v1/private/inventory/{sku}` and have the console's variant save delete rows for removed skus
   (same best-effort→explicit orchestration as the save). `ProductReservationLine.inventory` is already
   nullable, so an in-flight reservation of a deleted sku still commits/releases safely (release simply has
   no row to restock — acceptable for v1, log it).

**The critical chain, aligned end to end** (pricing → availability → inventory → reservation → checkout —
every hop keys on `(store, sku)` and nothing else):

| Concern | Single source of truth | Who reads it, when |
|---|---|---|
| Price | `inventory.product_price` per sku; `finalPrice` resolved server-side into `SkuPrice` | Card: default sku via bulk availability. PDP: all the product's variant skus. Cart: re-fetched server-side on add, on every load, and again at placement — the client can never send a price. Order: snapshotted into `order_product_price` rows at placement. |
| Availability | `inventory.product_availability.available` + `quantity` → `canBePurchased` (ONE flag; catalog has none) | PDP chip greys from enrichment; checkout gates add-to-cart on `canBePurchased` and enforces qty min/max; card badges from the default sku. |
| Stock | `inventory.product_availability.quantity` per sku | Decremented only inside `reserve` under `lockBySku` (PESSIMISTIC_WRITE, entries sorted by sku); restored by `release`/expiry; `commit` is a status flip. Two variants of one product are fully independent rows. |
| Reservation | `product_reservation(store, ref=orderId)` + lines by sku | Placement: reserve → payment → commit (PAID) / release (FAILED) / expiry job → release + notify checkout. Unchanged by variants. |
| Order truth | `order_product` snapshot: sku, real localized name, price rows, `order_product_option` label rows | Orders survive later catalog edits/deletes — labels are copied at placement, never re-joined. |

---

## 4. Console-ui (`store-core/console-ui`)

Per `ARCHITECTURE.md` tiers (features → shared → api → models), signals + `rxResource`, only `app-*`
controls, transloco keys in `src/locale/en.json` + `ar.json` (lint fails on unused, throws on missing).

1. **Options management** — new tab in `features/catalogue/`: `components/options-tab/` (list + editor panel
   like the brand tab; value management; per-language names). New `api/catalog/product-option.service.ts`;
   extend `catalogue-form.service.ts` + `catalogue.facade.ts`. The type tab is **untouched** (its
   `catalogue.types.noAttributes` notice stays accurate — types carry no option config in this design); mark
   the lessons.md variant-gap entries CLOSED (append-only convention) pointing at the per-product model.
2. **Product-form variants step** — new `features/product-form/components/variants-step/`, available to
   every product (Shopify-style "this product has options, like size or color"): pick the options this
   product varies by from the store vocabulary (`app-autocomplete`, ordered), then **auto-generate the
   cartesian product** with per-row remove + "add combination" escape hatch (server caps at 4 options /
   100 variants). Matrix columns: option values (read-only), sku (auto-suggested
   `<productSku>-<valueCodes>`, editable), price, quantity, available (writes the inventory flag), default
   (radio — exactly one). Save = `PUT /private/product/{id}/variants` (axes + combinations, new
   `api/catalog/product-variant.service.ts`) + `PUT inventory/bulk` (extend
   `api/inventory/inventory.service.ts`). `pricing-step` stays for single-variant products — it
   transparently edits the default variant's sku/price/qty (same UX as today).
   **Publish gating**: structural rules ("a product with options has combinations", "exactly one default")
   are guaranteed by the atomic PUT — no publishability endpoint needed. The readiness checklist
   (`ReadinessItem` in `ProductFormFacade`) gains one rule: every variant sku has its inventory row (price
   set) — the multi-variant generalization of today's "pricing" readiness item. The publish button stays
   disabled with the unmet item named, per the checklist's existing pattern.
3. **Products list** (`features/products/`) — one sku per row, as today: price/stock cells show the default
   variant's values (`bySkus` of the page's default skus), multi-variant rows add an "N variants" tag and
   disable inline edit (edit in the form, where the full matrix lives). No variant fan-out on the list.
4. **Order detail** — render line `attributes` (option/value labels) under the product name.

---

## 5. Landing-ui (`store-pod/landing-ui`)

1. **Types reshape** — `libs/types/src/product-groups.ts` L125-209: replace dead shapes, keeping the names
   themes read: `ProductOption {id, code, name, sortOrder, variant, optionValues[]}`,
   `ProductOptionValue {id, code, name, sortOrder}`, `ProductVariant {id, sku, defaultVariant,
   sortOrder, optionValueIds}` + enrichment-attached `price/quantity/canBePurchased` (availability comes
   ONLY from the inventory merge — one flag). Delete
   `ProductVariation`, the two-slot fields, `ProductVariantInventory`, `ProductAttribute*`. `Product` always
   carries `variants[]` (≥1); `hasVariants` stays a **derived** presenter predicate
   (`options.length > 0`, as `product-presenter.ts` already computes it) — nothing stored.
2. **Hook** — `libs/hooks/src/use-product-purchase.ts`: rebuild matching/default-selection/`isValueAvailable`
   over `optionValueIds` (one `valueId → optionId` index from `options[]`). **Public return API unchanged**
   → the 12 `BuyBox.tsx` files stay untouched; if any theme destructures `variation`/`variationValue`
   directly, that's a mechanical 12-file fix — flag during implementation.
3. **Card contract — one sku per card, no variant loading.** A listing card contains: image, name, the
   **default variant's price** (badges — sale/low-stock — from that same sku), and when `variantCount > 1`
   an "options available" hint; quick-add is replaced by a view-details link (existing card branch). Pricing
   comes from the same one-sku-per-product bulk availability call as today (`enrichProducts` over the page's
   default skus — a page of 24 products stays 24 skus, GET is fine). We deliberately do **not** show
   "from"/min–max prices on cards — that would require loading every variant of every card; the merchant
   controls the card price by choosing the default variant.
4. **PDP — variants are selectable and routable.** `enrichProduct` fetches availability for **all** of that
   one product's variant skus (bounded; POST body form above ~40) and attaches price/qty per variant.
   `use-product-purchase` syncs the selection to the URL (`?sku=<variantSku>` via `router.replace`, no
   reload): landing on a PDP with `?sku=` preselects that variant (price, stock, gallery follow), so variant
   links are shareable and each combination is directly addressable. Selecting values swaps the displayed
   price/stock to the resolved variant; unresolved combinations grey out as the hook already does.
5. **Enrichment rewrite** — `libs/services/src/inventory-service.ts` stops ad-hoc mutating `Product`:
   the merged pricing fields become a typed, documented pricing section on the product types
   (`PricedProduct`), written by the one enrichment function — same seam, explicit contract.
6. **Search UX** — results and suggestions render standard product cards (default variant price), one card
   per product no matter how many variants matched; a suggestion carrying `matchedVariantSku` links to the
   PDP with `?sku=` so a shopper who typed a variant sku lands preselected. No per-variant rows in search
   results.
7. **Facets** — wire `getFacets` (currently hardcoded `variants: []` in
   `libs/services/src/product-category.ts`) to the new `ReadableSearchFacets.options`; `optionValueIds` is
   already sent by `product-category.ts` and toggled by `use-product-listing.ts` — only the response mapping
   is new.

---

## 6. Demo seed data

Under `catalog-service/src/main/resources/init-sql/stores/<storeId>/` (all seeded stores):
- **Edit the product seed files to the final shape**: remove the `sku` column from `product` inserts and
  give every seeded product a default variant row carrying that sku (e.g. a `19-catalog-default-variants.sql`
  per store, before the option seeds). Databases are recreated, so seeds are the only "migration".
- `20-catalog-option.sql` (options + values + descriptions in the store's languages),
  `21-catalog-option-assignment.sql` (assign options to 2–3 existing products),
  `22-catalog-product-variant.sql` (those products × 4–6 combination variants, signatures precomputed,
  one flagged default each, replacing their default variants).
- Bump `catalog.sm_sequencer` rows above seeded ids (existing convention).
- Inventory: one `product_availability` + `product_price` row per variant sku with differing prices (so
  ranges and facets are visible in QA).

---

## 7. Tests

Per `references/testing.md`: units in `src/test` (`*Test`), integration in `src/integrationTest`
(`*IntegrationTest`, `@ServiceIntegrationTest`); every store-scoped endpoint owes a tenant-isolation case and
a 403 case.

- **catalog-core**: `ProductOptionMapperTest`, `ProductVariantMapperTest` (signature canonicalization,
  description-merge keeps ids), `ProductVariantServiceImplTest` (validation matrix: missing/foreign value,
  duplicate combination, duplicate sku, diff keeps ids, empty set restores the default variant, exactly one
  default enforced with sort-order fallback, >4 options / >100 variants → `VariantLimitExceededException`),
  `ProductServiceImplTest` addition: product create persists the default variant in the same transaction.
- **catalog-service integration**: `ProductOptionApiIntegrationTest` (incl. delete blocked 409 while
  assigned/used), `ProductVariantApiIntegrationTest` (+ tenant isolation, 403, atomic axes+set replace);
  `ProductApiV2` additions — `optionValueIds` AND/OR semantics incl. the Red/S + Blue/L negative case,
  facet counts agree with the page, PDP returns options+variants, variant sku findable via search after
  rebuild; `ExternalProductApiIntegrationTest` — bulk endpoint, variant-sku resolution with localized labels.
- **inventory**: bulk upsert (create+update mix, isolation, 403), `(store, sku)` unique violation, POST
  availability parity with GET.
- **checkout**: `ProductDetailsComposerImplTest` (one catalog + one inventory call for N skus; missing sku →
  notStocked), `OrderLineMapperTest` (real name, option snapshot rows, >64-char name), qty min/max
  enforcement at add-to-cart, placement integration test asserting `order_product_option` rows and cart
  `variant` passthrough.
- **console-ui / landing-ui**: facade + variants-step specs; hook selection/matching specs; `npm run build`
  + lint (i18n keys) as gates.

---

## 8. Order of work (each phase independently mergeable)

1. **Inventory cleanup + bulk upsert + POST availability** — fully independent. (Checkout composer batching
   + order-name fix can ride along.)
2. **Catalog: option vocabulary** — schema, entities, `ProductOptionApi`. Additive; types untouched.
3. **Catalog: variants** — entities (assignments + variants), `ProductVariantApi`, read-model hydration,
   search-doc view + rebuild, `optionValueIds` filtering + facets, external bulk + variant-sku resolution,
   .http, seeds.
4. **Checkout: order option snapshot** + qty min/max enforcement + dead column/table drops (needs phase 3's
   `variant` block).
5. **Console-ui**: options tab → variants step + inventory matrix → list default-price + variant tag → order
   line option labels.
6. **Landing-ui**: types reshape, hook, enrichment, facets.
7. **Seed/QA polish across stores.**

## Verification

Reset the local databases first (drop the `lcl` postgres volume — schemas initialize fresh in the final
shape, no migrations), then `lcl start -d` (profiles `lcl,test-stores`), rebuild search index per store, then:
- **API QA** via the new/updated `.http` files (`extra/requests/console-ui/org-admin`, service `http/` dirs).
- **Console (org1-admin/admin on gateway.com:8000)**: create option (Color, values, translations) → create a
  product → in the variants step pick Color+Size → generate matrix → set prices/stock (bulk) → list shows
  the default variant's price + "N variants" tag → console search finds a variant sku; publish button stays
  disabled until every variant sku has a price (readiness item names the gap); exceeding 4 options or 100
  variants via the API → 422; exactly one default enforced.
- **Storefront (user/revo on org1-store1.spg-507f1f77.gateway.com)**: search by name returns one card per
  product (default variant price, never one row per variant), and searching a variant sku suggests the
  product deep-linked with `?sku=`; facet rail shows option values with
  counts and filters correctly; card shows the default variant's price with an options hint + view-details;
  PDP chips select a variant with per-variant price/stock, grey impossible combos, and the URL updates to
  `?sku=<variantSku>` (opening that URL preselects the variant); add to cart shows "Color: Red / Size: L" on
  the line;
  full purchase → order confirmation and console order detail show the real name + option labels; reservation
  decrements the variant's stock only.
- **Tenant isolation**: repeat as the second demo store — no cross-store options/variants visible; no token → 403.
- **Performance evidence**: logs/traces show exactly two s2s calls per cart load (one catalog bulk, one
  inventory bulk); listing page issues one availability call for all card skus.

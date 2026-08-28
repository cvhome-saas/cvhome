# Split `catalog` into lean `catalog` + tiny `inventory` service

## Context

The catalog service today bundles product catalog, availability/stock, pricing, variants, and reservations. The console-ui and landing-ui support only a **single product with no variants**, so the variant/availability machinery is largely dead code, yet it deeply entangles the product read/write paths (fetch-joins, cascade-ALL availability, implicit price creation). We split it:

- **catalog** — pure product catalog: products, categories, images, attributes/options, types, groups, manufacturers, relationships. No price/quantity in its responses.
- **inventory** (new, port **8126**, schema `inventory`) — availability/stock, pricing, reservations. Tables moved **as-is** (variant/region columns stay dormant for the future variant feature).
- **store-pod/catalog-deprecated** — variant/variation code + DDL parked, unregistered from the build (same pattern as `content-deprecated`), for later reintroduction.
- **UIs call inventory directly** (user decision): catalog responses become pure catalog; console-ui and landing-ui make separate inventory calls and merge client-side; checkout composes from both services.
- **A data migration is required** — a deployed environment holds real availability/price rows.

Verified facts shaping the plan:
- Only s2s consumer of catalog is **checkout**: `getDetailedProduct(sku)` (bundles product+price+availability) + reserve/commit/release.
- console-ui never calls `ProductInventoryApi`/`ProductPriceApi`; it uses the embedded `inventory {price, quantity}` on the v2 product and an inline `PATCH` (`LightPersistableProduct`). No variant screens.
- landing-ui reads embedded price/qty and calls two variation endpoints (`GET /v2/category/{id}/variations`, `POST /v2/product/{id}/variation`) that will be removed.
- `product_availability.sku` is **null** in real data (`getBySku` joins through `product` today) — migration must backfill it; sku is safe as cross-service key (`product` has unique `(store_merchant_id, sku)`).
- `product_price` has **no `store_merchant_id`** — must be added + backfilled via the availability join.
- `CustomPermissionEvaluator` hard-codes domain strings; grants are membership-derived, not seeded — adding `STORE-POD.INVENTORY.*` is 2 constants + 2 switch cases, no role-seed changes.
- `catalog-core`'s `checkout-external-api` dependency exists only for `ProductReservationCleanupService` → moves to inventory-core.

## Phase 0 — Scaffold `inventory` (boots empty, nothing calls it)

`store-pod/inventory/` with the standard 4 modules (mirror `store-pod/payment`, the smallest pod service): `inventory-commons`, `inventory-core`, `inventory-external-api`, `inventory-service`. `spring.application.name=inventory`, port 8126, schema `inventory`.

Registration checklist (one commit):
- `settings.gradle` — 4 `store-pod:inventory:*` includes
- `store-commons/autoconfigure/src/main/resources/common-config.yml` — `inventory:` block after `payment` (port 8126, gateway-service-name spg)
- `lcl-config.yml` — simple discovery instance `http://localhost:8126`
- `fargate-config.yml` — `loadbalancer.eager-load.clients` + `ecs.discovery.service-ports`
- `extra/scripts/run-lcl.sh` — services array row (copy catalog's shape)
- `extra/scripts/configure-domain.sh` + `docker-compose-lcl.yml` — host alias
- `store-pod/spg/Caddyfile` — `handle_path /inventory* { ... reverse_proxy http://inventory.{$NAMESPACE}:8126 }` right after the `/catalog*` block, before the landing-ui catch-all
- `store-commons/autoconfigure/.../CustomPermissionEvaluator.java` — add `STORE-POD.INVENTORY.*` → `hasManageAccessOnStore` and `STORE-POD.INVENTORY.RESERVE` → `isSameStorePod` (keep `CATALOG_RESERVE` until catalog's reservation API is deleted, then remove)

## Phase 1 — Inventory DDL + one-off migration + seeds

`inventory-service/src/main/resources/init-sql/schema.sql` (CREATE IF NOT EXISTS):
- `inventory.sm_sequencer`
- `inventory.product_availability` — same columns, **FKs severed**: `product_id` plain nullable bigint (informational), `product_variant` plain nullable bigint (dormant); index `(store_merchant_id, sku)`
- `inventory.product_price` — same columns **+ `store_merchant_id varchar(50)`**; keeps internal FK to availability
- `inventory.product_price_description`, `product_reservation`, `product_reservation_line` — as-is, FKs retargeted inside schema

`init-sql/migrate-from-catalog.sql`, run after schema.sql (same Postgres per pod; follow catalog's init-sql ordering mechanism). Idempotent (`WHERE NOT EXISTS`), guarded by `IF EXISTS catalog.product_availability`:
1. Copy availability with **sku backfill**: `COALESCE(a.sku, p.sku)` joining `catalog.product p ON p.product_id = a.product_id`
2. Copy price with **tenancy backfill**: `store_merchant_id` from the joined availability row
3. Copy price descriptions + reservation tables verbatim
4. Initialize `inventory.sm_sequencer` above `max(id)` of each copied table (**collision risk otherwise**)

Do **not** drop catalog's old tables now; catalog's schema.sql just stops creating them. A manual `extra/scripts/drop-catalog-inventory-tables.sql` runs post-verification.

Seeds: move each store's `17-catalog-product-availability-price.sql` into inventory-service test-stores seeds, retargeted to `inventory.*`, with explicit `sku` (from the matching `14-catalog-product.sql`) and `store_merchant_id` on price rows.

## Phase 2 — Move code into inventory (`com.asrevo.cvhome.inventory...`)

From catalog-core → **inventory-core**:
- Entities: `ProductAvailability` (replace `@ManyToOne Product` / variant refs with plain `Long productId` + `String sku`), `ProductReservation(+Line/Status)`, `ProductPrice(+Description)` (+ `storeMerchantId`)
- Repos: `ProductAvailabilityRepository` — rewrite `getBySku` to `where p.sku=?1 and p.storeMerchantId=?2` (keep pessimistic lock), `PageableProductAvailabilityRepository`, `ProductPriceRepository`
- Services: `ProductAvailabilityService(Impl)`, `ProductReservationService(Impl)`, `ProductReservationCleanupService` (+ `checkout-external-api` dep moves here), `PricingService(Impl)`, `ProductPriceUtils`, `ProductPriceService(Impl)`
- Facades/mappers/populators: `ProductInventoryFacade`, `ProductPriceFacade`, availability/inventory/price mappers, `ReadableProductPricePopulator`, `ReadableFinalPricePopulator`. Where a mapper reached into `Product`, use the carried sku/productId. `merchant-external-api` dep moves if PricingService needs store currency (check).

From catalog-commons → **inventory-commons**: `model/product/inventory/*`, price DTOs, exceptions (`InsufficientInventoryException`, `EmptyReservationException`, `InventoryNotFoundException`, `ProductPriceNotConvertibleException`, reservation exceptions).

**inventory-service** controllers (paths preserved — clients change only prefix `/catalog/…` → `/inventory/…`):
- `ProductInventoryApi` (v1 private inventory CRUD; add sku-addressed upsert `/api/v1/private/inventory/{sku}` for the console)
- `ProductPriceApi` (v1 private price CRUD)
- `ExternalProductReservationApi` (`/api/v1/private/reserve|commit|release/{ref}`, `STORE-POD.INVENTORY.RESERVE`)
- **New** bulk read: `GET /api/v1/availability?skus=a,b,c` → `List<SkuInventory>` (public/storefront-readable, mirroring catalog's public read auth — **verify cua-token access**) + same under `/private` for console
- All `@PreAuthorize` → `STORE-POD.INVENTORY.*`
- `.http` files: `inventory-service/http/` for all of the above via `{{SELLER_UI_URL}}/spg/inventory/…`

**inventory-external-api**:
```java
@HttpExchange("/api/v1")
interface ExternalInventoryService {
  @GetExchange("/availability")
  List<SkuInventory> getBySkus(StoreMerchantId store, @RequestParam List<String> skus, LanguageCode lang);
}
// SkuInventory { sku, available, canBePurchased, quantity, qtyOrdMin/Max, FinalPriceCalc price }
@HttpExchange("/api/v1/private")
interface ExternalProductReservationService { /* moved verbatim */ }
```
Plus `InventoryApiErrors` (moves `ProductReservationRejectedException` semantics from `CatalogApiErrors`).

## Phase 3 — Slim catalog (only after consumers switched — see Ordering)

- `Product.java` — delete `availabilities` cascade-ALL collection. `product.available` **stays in catalog** as merchandising visibility; stock-level available/canBePurchased/quantity owned by inventory.
- `ProductRepositoryImpl`/`ProductRepository` — remove all availability/price fetch-joins + the `@EntityGraph`; drop variant-sku branch in the sku lookup. **Risk: some joins are INNER — products lacking availability rows become visible in listings; audit seed/prod data and compare listing counts.**
- Mappers: delete `ReadableProductMapper.populateVariants/populateAvailability/populatePrice`; drop pricing from `ReadableProductPopulator`; drop `ReadableInventory` from `ReadableProductDefinitionMapper`; delete `PersistableProductDefinitionMapper.applyAvailabilityAndPrice` and `PersistableProductMapper.applyInventory`; remove `quantity`/`price`/`inventory` from persistable/readable product DTOs (console ships in lockstep).
- `ProductServiceImpl.getDetailedProduct` → minimal product only; `ExternalProductApi /detailed-product` returns slimmed `ProductDetails` (product data only).
- Delete moved APIs (`ProductInventoryApi`, `ProductPriceApi`, `ExternalProductReservationApi`) + availability/price/reservation/pricing code from catalog; drop `checkout-external-api` from `catalog-core/build.gradle`; remove moved + variant tables from catalog schema.sql.

## Phase 4 — `store-pod/catalog-deprecated`

Unregistered dir (like `content-deprecated`): variant/variation entities, services, facades, mappers, repos, v2 `ProductVariantApi`/`ProductVariantGroupApi`/`ProductVariationApi`, variant DTOs/exceptions, and `deprecated-ddl.sql` with the removed variant-table DDL. Short README on the reintroduction path.

## Phase 5 — Consumers

**checkout** (only s2s consumer):
- build.gradle: add `inventory-external-api` (keep slimmed `catalog-external-api`); `checkout-commons` picks up `inventory-commons` if DTOs referenced
- `ClientsConfig.java`: `INVENTORY_SERVICE_NAME="inventory"`; reservation bean from inventory's interface + `InventoryApiErrors`; new `ExternalInventoryService` bean (optionally cached like `CachedExternalProductService`)
- `OrderInventoryOrchestratorImpl` — import swap only
- The 6 `getDetailedProduct` call sites (`ShoppingCartFacadeImpl`, `ShoppingCartServiceImpl`, `ReadableShoppingCartMapper`, `ReadableOrderProductMapper`, `OrderProductPopulator`, `ReadableOrderProductPopulator`): introduce one `ProductDetailsComposer` in checkout-core that calls catalog + inventory and rebuilds the existing `ProductDetails` shape — call sites swap one dependency, downstream mappers untouched.

**console-ui** (`store-core/console-ui`):
- New `src/app/api/inventory/inventory.service.ts` (`/spg/inventory/api/v1/...`)
- Product form (`product-form.api.service.ts`, `pricing-step/`): save = POST/PUT product to catalog, then upsert inventory `{sku, productId, quantity, price, available}`; on partial failure surface error and retry inventory only
- Inline edit (`LightPersistableProduct`): split into catalog PATCH (`available` visibility) + inventory PUT-by-sku (price/quantity), fired in parallel
- Product list price/qty columns: bulk `GET /inventory/.../availability?skus=…` after the catalog page loads, merge client-side
- Delete: **tolerate orphans** — best-effort inventory delete after catalog delete; no events/cleanup job now

**landing-ui**:
- `storeBaseServiceUrl("inventory", ctx)` — works generically
- Listing + PDP: bulk-fetch availability by skus after catalog fetch; merge so `use-product-purchase.ts` fields keep resolving (adapt the hook in one place)
- Remove variation calls: `product-category.ts:41` (hide variation filter panel) and `product-service.ts:56` (PDP shows inventory price directly, no recalc); verify no other imports first

## Ordering (nothing broken mid-sequence)

1. Phase 0 scaffold + registration + evaluator
2. Phase 1 DDL + migration + seeds (dual state: catalog still serves everything)
3. Phase 2 code move; verify inventory endpoints via `.http` against migrated data
4. checkout switch (reservation + composition); verify checkout flow while catalog still serves old endpoints
5. console-ui + landing-ui switches
6. Phase 3 + 4 catalog slimming & deprecation (delete old endpoints last)
7. Post-verify: manual drop script for old catalog tables

## Verification (`./extra/scripts/run-lcl.sh`, profiles lcl,test-stores)

- All services healthy incl. inventory 8126; migration counts match; **zero null skus** in `inventory.product_availability`; every price row has `store_merchant_id`
- `.http` QA: inventory/price CRUD, bulk by-skus, reserve→commit, reserve→release, insufficient stock → typed refusal, no-token → 403, cross-tenant → denied
- Console (org1-admin/admin @ gateway.com:8000): product create with price/qty (two-call flow), inline edit, delete, list shows price/qty
- Storefront (user/revo @ org1-store1…): listing prices, PDP, add-to-cart, full checkout (reserve via inventory), expired-reservation cleanup notifies checkout
- Regression: listing product count unchanged after inner-join removal; catalog responses carry no price/qty; pages that used variation endpoints render clean

## Risks

- Inner-join removal changes listing membership (hidden products may appear) — audit first
- sku backfill is load-bearing for reservations — fail loudly if any null skus remain post-migration
- `product_price.store_merchant_id` backfill misses rows with missing availability — guard/report
- Sequencer init must exceed copied max IDs
- Storefront (cua) auth on the bulk availability endpoint — get it wrong and listing/PDP lose prices
- Partial-failure UX in console's two-call save

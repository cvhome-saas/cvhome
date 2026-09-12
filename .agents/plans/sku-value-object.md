# `Sku` value object

## Context

The sku is the key three pods share — catalog owns it (`product_variant`), inventory keys stock and price by it,
checkout keys carts and orders by it — and everywhere it is a raw `String`:

| Where | Field |
|---|---|
| catalog | `ProductVariant.sku` (`catalog-core/.../entity/ProductVariant.java:91`) |
| inventory | `Inventory.sku` (`Inventory.java:68`), `ProductReservationLine.sku` (`ProductReservationLine.java:51`) |
| checkout | `CartLine.sku` (`CartLine.java:56`), `OrderLine.sku` (`OrderLine.java:55`), `Order.addLine(String sku, …)` |
| s2s contracts | `ReadableMinimalProduct.sku`, `ExternalProductService.getDetailedProduct(s)`, `SkuInventory.sku`, `AvailabilityQuery.skus`, `ExternalInventoryService.getBySkus`, `ReserveProductEntry.sku` |

No `Sku` exists in `store-commons/commons/.../domain/`, so the review rule *"a raw `String` where a
`commons/domain/` value object exists"* never had anything to fire on.

It is not only a typing gap. **The sku's format rule lives in one service.** Catalog's request DTOs carry
`@Pattern(regexp = "^[a-zA-Z0-9_-]*$")` (`PersistableProductDefinition.java:29`,
`PersistableProductVariant.java:28`); nothing else checks it:

- `InventoryApi.upsert(@PathVariable String sku, …)` and `deleteBySku` — any string.
- `PersistableSkuInventory(@NotEmpty String sku, …)` (the bulk upsert) — any non-empty string.
- `CartApi.removeLine(@PathVariable String sku)`, `PersistableCartItem.product` — any string.

So inventory will stock `"abc def"` or `"ABC "`, a sku catalog can never create and checkout will never find.

## Why the design is what it is

1. **Home: `store-commons/commons/.../domain/Sku.java`.** Three pods and two of the pod-shared libraries use it.
   `CartCode` sits in `checkout-commons` because only checkout ever sees one; the sku is the opposite case.
2. **The invariant is in the constructor: `^[A-Za-z0-9_-]{1,255}$`.** That is catalog's existing rule, bounded
   by the column: all five sku columns are `varchar(255)`. `Sku.FORMAT` is a public constant, so the request DTOs'
   `@Pattern` annotations reference it and the rule exists once.
3. **Case-preserving, never trimmed.** The unique keys (`uk_product_variant_sku (store_merchant_id, sku)`,
   `UK_CART_LINE_SKU`, inventory's `(store, sku)`) compare exactly; normalising would orphan existing rows.
4. **Strict on read as well.** The JPA converter and the JSON creator both go through the constructor, so a row or
   payload that breaks the rule fails loudly instead of being carried along. This is safe for existing data:
   - every sku pattern catalog has ever had (`^[a-zA-Z0-9_]*$`, then `^[a-zA-Z0-9_-]*$`, per `git log -G`) is a
     subset of the new rule, combined with `@NotEmpty`;
   - all 590 skus in the seed SQL conform (scanned every `INSERT … sku …` in the repo);
   - console-ui rewrites a variant sku to `[A-Za-z0-9_-]` before sending it (`product-form.facade.ts:668`), and
     load-testing generates `K6-SKU-…` / `K6-EDIT-…`.

   The one path that could have written a non-conforming row is inventory's unvalidated API. See *Deploy note*.
5. **The wire stays a bare string** — `@JsonValue` on the accessor and a delegating `@JsonCreator`, the way
   `StoreMerchantId` serializes. landing-ui, console-ui, k6 and the `.http` files see no change.
   - `toString()` returns the value, because an `@HttpExchange` client formats a `List<Sku>` query param through
     the conversion service's `toString()`.
   - Spring MVC binds `@PathVariable Sku` / `@RequestParam List<Sku>` through the `String` constructor. A bad value
     is a `MethodArgumentTypeMismatchException`, which `GlobalErrorHandler.handleExceptionInternal` already renders
     as 400 `MALFORMED_REQUEST` — no new advice.
6. **Request bodies (`Persistable*`) keep a `String`, validated with `@Pattern(regexp = Sku.FORMAT)`.** Bean
   validation answers 400 `VALIDATION_FAILED` with `fieldErrors[sku]`; a failure inside a Jackson creator can only
   be a field-less `MALFORMED_REQUEST`. No request DTO in the repo carries a value object today, so this also
   matches the neighbours. The service converts with `Sku.of(...)` after validation has passed.
7. **Search input stays a `String`.** `ProductFilter.sku`, `ProductSpecifications.skuLike` and the suggestion's
   `equalsIgnoreCase(query)` compare a partial, case-insensitive search term against skus. They are not skus.
8. **Error factories take `Sku` and put `sku.value()` in their params** (`InsufficientInventoryException.of`,
   `ProductNotPurchasableException.of`, `CartQuantityOutOfRangeException.of`, `DuplicateVariantSkuException.of`,
   `VariantOptionsInvalidException.of`). The ProblemDetail params stay strings, so the client-side decoders
   (`InventoryApiErrors`, `CheckoutErrors` …) do not change.
9. **No DDL change, no event change.** The columns stay `varchar(255)`; no outbox event carries a sku.

**Non-goals:** the unmapped Shopizer `ref_sku` column; renaming `PersistableCartItem.product` on the wire.

## Phase 1 — the type (commit 1)

- `store-commons/commons/src/main/java/com/asrevo/cvhome/commons/domain/Sku.java` — record, `FORMAT`,
  `of(String)`, `@JsonValue value()`, delegating `@JsonCreator`, `toString()`.
- `store-pod/commons/store-commons/.../store/core/converter/SkuConverter.java`, beside `CurrencyCodeConverter`.
- Tests: `SkuTest` (the accepted/rejected table, bare-string JSON both ways, a malformed JSON value rejected,
  `toString`), `SkuConverterTest` (round trip, null both ways).
- Skill `references/api-conventions.md`: add `Sku` to the value-object list.

Nothing uses the type yet.

## Phase 2 — inventory (commit 2)

- Entities `Inventory.sku`, `ProductReservationLine.sku` → `Sku` with `@Convert(converter = SkuConverter.class)`.
- `InventoryRepository` (`findBySkus`, `findBySku`, `lockBySku`), `InventoryService(Impl)`,
  `ReservationService(Impl)`, `SkuInventoryMapper`, `ProductReservation.holds`.
- Contracts: `SkuInventory.sku`, `AvailabilityQuery.skus`, `ExternalInventoryService.getBySkus(List<Sku>)`,
  `ReserveProductEntry.sku` (`store-pod/commons/store-commons`), `InsufficientInventoryException.of/notStocked`.
- Edge — **the fix**: `InventoryApi` binds `@PathVariable Sku` (PUT and DELETE), `ExternalInventoryApi` binds
  `List<Sku>`, `PersistableSkuInventory.sku` gains `@Pattern(regexp = Sku.FORMAT)`.
- Checkout's two call sites into inventory (`ProductSnapshotServiceImpl`, `OrderStepRunner`) get the smallest
  bridge that compiles; phase 4 removes it.
- Tests: fixtures follow the type; integration cases for `PUT /private/inventory/{bad}` → 400 and a bulk upsert
  with a malformed sku → 400 `VALIDATION_FAILED`.
- QA: `inventory-service/qa/inventory-qa.md`.

## Phase 3 — catalog (commit 3)

- `ProductVariant.sku` + `@Convert`; `ProductVariantRepository` (`findByStoreAndSku`, `findByStoreAndSkuIn`,
  `existsByStoreMerchantIdAndSku`); `ProductService(Impl)` (`getBySku`, `getBySkus`, `exists`,
  `renameDefaultVariant`), `ProductVariantServiceImpl`, `ProductMapper`, `ProductVariantMapper`,
  `ProductImageServiceImpl`, `ProductSearchServiceImpl`.
- Contracts: `ReadableMinimalProduct.sku`, `ReadableProductDefinition.sku`, `ReadableProductVariant.sku`,
  `ReadableVariantSelection.sku`, `ReadableProductSuggestion.sku` / `matchedVariantSku`,
  `ExternalProductService.getDetailedProduct(Sku)` / `getDetailedProducts(List<Sku>)`,
  `DuplicateVariantSkuException.of`, `VariantOptionsInvalidException.of`.
- Request DTOs: `@Pattern(regexp = Sku.FORMAT)` replaces the two literals.
- `ExternalProductApi` binds `Sku` / `List<Sku>`. Checkout's snapshot call bridged, removed in phase 4.
- QA: `catalog-service/qa/catalog-qa.md`.

## Phase 4 — checkout (commit 4)

- `CartLine.sku`, `OrderLine.sku` + `@Convert`; `Cart.line/put/remove(Sku)`, `Order.addLine(Sku, …)`.
- `CartService.removeLine(…, Sku)`, `CartServiceImpl`, `ProductSnapshot.sku`,
  `ProductSnapshotService.snapshot(…, Collection<Sku>) → Map<Sku, ProductSnapshot>`, `OrderPlacementTransaction`,
  `OrderStepRunner`, `CartMapper`, `OrderMapper`, `ReadableOrderProduct.sku`.
- Errors: `ProductNotPurchasableException.of(Sku)`, `CartQuantityOutOfRangeException.of(Sku, …)`.
- Edge: `CartApi.removeLine(@PathVariable Sku)`; `PersistableCartItem.product` gains
  `@Pattern(regexp = Sku.FORMAT)`, so a malformed sku on cart add is a 400 rather than reaching `Sku.of`.
- The bridges from phases 2–3 go away.
- QA: `checkout-service/qa/checkout-qa.md`.

## Other repos

None. The wire format is unchanged, and load-testing and e2e-testing already send conforming skus.

## Deploy note

Before this reaches an environment holding real data, each of these must return 0. A non-conforming row would fail
to load with `IllegalArgumentException` from `SkuConverter` rather than be misread:

```sql
select 'catalog.product_variant', count(*) from catalog.product_variant where sku !~ '^[A-Za-z0-9_-]{1,255}$'
union all select 'inventory.product_availability', count(*) from inventory.product_availability where sku !~ '^[A-Za-z0-9_-]{1,255}$'
union all select 'inventory.product_reservation_line', count(*) from inventory.product_reservation_line where sku !~ '^[A-Za-z0-9_-]{1,255}$'
union all select 'checkout.cart_line', count(*) from checkout.cart_line where sku !~ '^[A-Za-z0-9_-]{1,255}$'
union all select 'checkout.sales_order_line', count(*) from checkout.sales_order_line where sku !~ '^[A-Za-z0-9_-]{1,255}$';
```

## Deviations, as built

- **`Sku` implements `Comparable` (phase 2, not 1).** `ReservationServiceImpl` locks inventory rows in sku order so
  two overlapping reservations cannot deadlock; `compareTo` is the string's, so that order is unchanged.
  `SkuTest.skusSortAsTheirStringsSoInventoryKeepsItsLockOrder` pins it.
- **The phase-2 checkout bridge skips a string that is not a sku** rather than calling `Sku.of` on it. Until
  phase 4 validates `PersistableCartItem.product`, a cart line can hold whatever a shopper posted, and a strict
  bridge would turn that into a 500 on the cart. Phase 4 deletes the bridge.
- **`store-pod/commons/store-commons` got its first tests** (`SkuConverterTest`), and with them
  `testImplementation` copies of its compileOnly JPA and Jackson APIs.
- **`GET /private/product/unique?code=` keeps a `String`** (phase 3). It is the console's "is this sku taken?",
  asked while the merchant types; a string that cannot be a sku cannot be taken, so it answers `false` rather than
  400. Every other catalog sku edge binds a `Sku`.
- **Found, not fixed: console-ui's `SKU_PATTERN` allows a dot** (`product-draft-form.service.ts`,
  `/^[A-Za-z0-9._-]+$/`). The server has never accepted one, so a product sku with a dot passes the form and fails
  the save with a 400. Out of scope for a backend type change; it wants the pattern made `Sku.FORMAT`'s.
- **The phase-3 checkout bridge replaced the phase-2 one** rather than stacking on it: the snapshot filters the cart's
  strings to well-formed skus once and asks catalog and inventory with the same `List<Sku>`, which keeps
  `snapshot()` inside checkstyle's complexity limit.

## Verification

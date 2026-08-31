# Product Variants — implementation handoff

Working notes for whoever continues this branch (`feat/product-variants`, targeting `main`).
The plan of record is [`support-product-variants-serialized-snowflake.md`](./support-product-variants-serialized-snowflake.md)
in this directory — read its **Context**, **Core design decisions** and **Product lifecycle** sections first;
this file records what is *built*, what was *learned building it*, and exactly what remains.

## The model in one paragraph

Shopify-style uniform variant model. `catalog.product` is a pure definition (no `sku` column any more);
**every product owns ≥ 1 `catalog.product_variant`**, and sku/price/stock always live at the variant level.
A product with no options owns exactly one **default variant** (`option_signature = 'DEFAULT'`); a product
with options owns one variant per sold combination, written **atomically together with its axes**
(`product_option_assignment`) through `PUT /api/v2/private/product/{id}/variants`. Option vocabulary
(`product_option` / `product_option_value`) is store-wide — shared value ids are what make id-based
faceting work. Sellability is **inventory's single `available` flag** (the catalog variant row has none).
Guardrails: max 4 options / 100 variants per product. No migrations anywhere: schema.sql files are edited
to final shape; dev/QA databases are dropped and reseeded.

## Done — four commits, each phase green (unit + integration + checkstyle) before committing

| Phase | Commit | What it contains |
|---|---|---|
| 1 inventory | `644944c5c` | `product_availability` final shape (dormant variant/region columns dropped, `sku` NOT NULL, `unique (store_merchant_id, sku)`); `migrate-from-catalog.sql` + `CatalogDataMigration` deleted; `PUT /api/v1/private/inventory/bulk` (body `PersistableInventoryBatch {entries:[{sku, inventory}]}`, ≤200); `DELETE /api/v1/private/inventory/{sku}`; public `POST /api/v1/availability/query` (`{skus:[]}`); reservation entries locked in **sorted sku order**; `.http` files incl. new `extra/requests/{landing-ui,console-ui/org-admin}/inventory.http` |
| 2 catalog options | `2895cfb87` | store-wide vocabulary: `product_option(_value)(_description)` tables, entities, `services/option/`, `ProductOptionApi` (`/api/v1/private/product/option[s]`, whole-document writes — values addressed by id keep rows), errors `CATALOG.PRODUCT_OPTION.{NOT_FOUND,DUPLICATE,IN_USE}` |
| 3 catalog variants | `3201225ae` | the model itself — see below |
| 4 checkout | `4fe2df357` | batch `ProductDetailsComposer.getDetailedProducts(store, skus, lang)` (ONE catalog + ONE inventory call per cart/order — call sites: `ShoppingCartServiceImpl.getPopulatedShoppingCart`, `ReadableShoppingCartMapper.applyItems`, `OrderFacadeImpl.saveOrder` + `readableProducts(...)`); populators **deleted**, replaced by static `OrderLineMapper` (real localized name, `varchar(255)`, price row, option-label snapshot) and a batch-aware `ReadableOrderProductMapper`; new `checkout.order_product_option` snapshot table (entity `OrderProductOption`); `order_product_attribute` + `shopping_cart_attr_item` + `shopping_cart_item.product_variant` removed; qty min/max enforced at cart entry (`ProductNotPurchasableException.quantityOutOfRange`; quantity 0 passes — it is the modify path's remove signal); `CachedExternalProductService` is bulk-aware (per-sku cache entries, fetches only misses) |

### Phase 3 specifics you will build against

- **Wire shapes** (all verified by integration tests):
  - `ReadableMinimalProduct` + `variantCount: int` + nullable `variant: ReadableVariantSelection {sku, optionValues: [{optionId, optionCode, optionName, valueId, valueCode, valueName, sortOrder}]}` — the label block is filled only on sku-addressed reads of a **combination** variant (cart/order lines get it via the existing `BeanUtils.copyProperties` in `ReadableShoppingCartMapper`, untouched).
  - `ReadableProduct` (PDP + listing) + `options: ReadableProductOption[]` (assigned axes in order, each carrying **only values its variants use**) + `variants: [{id, sku, sortOrder, defaultVariant, optionValueIds}]`. Listings leave both empty — a card gets `sku` (default variant) + `variantCount` only.
  - `PersistableVariantSet {options: [optionCode…], variants: [{id?, sku, sortOrder, defaultVariant, optionValueIds}]}` — the atomic replace body. Empty options+variants restores the single default variant.
  - `ReadableProductVariantDefinition` (console matrix row) = readable variant + resolved `optionValues` labels.
  - `ProductFilter`/`ProductSearchCriteria` + `optionValueIds` (OR within an option, AND across options, anchored to ONE variant); `ReadableSearchFacets` + `options: [{optionId, code, name, sortOrder, values: [ReadableFacetBucket]}]`; `ReadableProductSuggestion` + `matchedVariantSku` (for `?sku=` deep links).
  - s2s: `ExternalProductService.getDetailedProducts(store, skus, lang)` — missing skus absent (mirrors inventory).
- `PersistableProductDefinition.sku` **stays on the wire**: create persists product + default variant in one tx; on update it renames the default variant only while the product is still single-DEFAULT-variant; `GET /api/v1/private/product/unique?code=` now checks the variant table (all skus live there).
- **Perf pattern**: `@BatchSize(100)` on `Product.variants`/`optionAssignments` — every mapper reads `product.defaultVariant()`/`variants.size()` freely and a page of 24 cards loads variants in one IN query. PDP/console matrix use `ProductVariantRepository.findByProductIdHydrated` (one fetch-join query, labels included). Do NOT add per-row queries.
- Search: `product_search_source` view folds `string_agg(variant skus)` into weight B (replaces `p.sku`); variant writes re-save the parent with `searchIndexStale()`; after schema changes run `POST /api/v2/private/products/search-index/rebuild` per store.
- Option deletes are guarded (409 `PRODUCT_OPTION.IN_USE`) by `ProductOptionRepository.isAssignedToProducts` / `isUsedByVariants`.
- Errors added: `CATALOG.PRODUCT_VARIANT.{NOT_FOUND, DUPLICATE_SKU, DUPLICATE_COMBINATION, OPTIONS_INVALID, LIMIT_EXCEEDED}`.

### Seeds (test-stores profile)

- Every seeded product's sku moved to a default-variant row: `stores/<id>/17-catalog-default-variants.sql`
  (generated; `14-catalog-product.sql` inserts no longer carry `sku`).
- Fashion store `65f023632bc46470c104b76f` additionally has `18-catalog-options-variants.sql`:
  options color(1: red=1, blue=2) / size(2: m=3, l=4); **product 1** (Nike, `SKU-NK-RUN-001`) varies by size
  (M keeps the original sku, `SKU-NK-RUN-001-L` id 501); **product 2** (Zara dress, `SKU-ZR-CL-DRS02`) varies
  by color×size with red/L deliberately missing (grey-out QA case); combination skus have inventory rows
  (ids 501–503, differing prices, `SKU-ZR-CL-DRS02-BL-L` has quantity 0).
- Integration tests use store A = `65f023632bc46470c104b76f`, product 3 (`SKU-AD-CL-TPT03`) as the
  turn-into-variants-and-back fixture.

## Hard-won gotchas (each cost a debug cycle — do not rediscover them)

1. **Hibernate flushes inserts before deletes.** Never `clear()` + re-add rows whose composite key or unique
   value may be reused: merge in place. This is why `ProductVariantServiceImpl` matches existing variants by
   **id, else by sku**, merges `ProductVariantOptionValue` per option, and merges assignments
   (`applyAssignments`) instead of clearing.
2. Spring 6.1 method-parameter validation (`@Size` on a `@RequestBody List`) raises
   `HandlerMethodValidationException`, which the shared advice maps to **500**. Wrap list bodies in a record
   (`PersistableInventoryBatch`) so standard `@Valid` body validation → 400.
3. Checkstyle gates: `MultipleStringLiterals` (≥2 occurrences — extract constants, tests included),
   `DeclarationOrder` (public constants before private fields), no string `+` concatenation (use
   `%s".formatted(...)`), import order. Run `check` per module before committing.
4. `-core` module unit tests that construct entities need
   `testImplementation libs.spring.data.commons / spring.data.jpa / jakarta.persistence.api / hibernate.core`
   (done for catalog-core and checkout-core; copy the block if you add tests to another `-core`).
5. Long git commit messages via PowerShell here-strings can mangle args — write the message to a scratch file
   and `git commit -F <file>`.
6. The cars-store seed uses timestamp literals, not `NOW()` — any seed tooling must not assume one format.
7. `lcl` local QA gap: reset the postgres volume before QA — schemas initialize in final shape, there are no
   migrations on this branch (context integration tests assert the constraints).

## Remaining work

> **2026-08-31 update:** Phases 5 and 6 are DONE (commits below). Only Phase 7 (live QA against a
> reset stack) remains. The sections beneath are kept as the record of what was asked; deltas worth
> knowing:
>
> - **Phase 5 commits:** `feat(console-ui): store option vocabulary — models, api tier, Options tab`
>   and `feat(console-ui): variants step, variant-aware list rows and order lines`. All gates green
>   (`npm run build`, `npm run lint`, `npm run test:ci` — 997 specs).
>   - The variants step lives in the wizard as a fifth step (locked until saved, like media) and
>     **saves itself** (atomic PUT → inventory bulk + retired-sku deletes, retryable
>     `variantInventoryPending` state). Retired skus are diffed against the **post-write** variant
>     list, not the request — clearing all options must not delete the restored default variant's
>     inventory row.
>   - Pricing step yields to a pointer when options are assigned; `ProductFormApi.update` takes
>     `writeInventory=false` then. Readiness swaps `price` for `variantPricing`.
>   - `ProductFormApi.load` now: catalog reads (definition, variants, option vocabulary, refs) then
>     ONE `bySkus` over default + combination skus.
>   - lessons.md: the variants console-gap entry is CLOSED; the type-attributes entry stays open by
>     design with a dated note.
> - **Phase 6 commit:** `feat(landing-ui): re-activate the variant UI against the uniform model`.
>   Gates: `build:libs`, `next build`, libs/theme tests; lint error count unchanged (539 pre-existing).
>   - **One deliberate deviation:** `ProductAttribute*` types were NOT deleted — every theme's
>     product page renders a specifications accordion from them (degrading to nothing; the wire
>     never sends attributes). Documented dead in `product-groups.ts`; delete together with those
>     blocks or revive when descriptive attributes land.
>   - `?sku=` sync uses `history.replaceState` (the listing's own pattern), not `router.replace`.
>   - Facet counts render inside the value label ("Red (12)") via each theme's `optionFacetGroups`.
>   - 13 themes touched mechanically: BuyBox (no `value.description`/`value.price`), Listing
>     (facet groups from `facets.options`), CartLineItem (`variantSelectionLabel`), OrderDetails
>     (attributes snapshot line).

### Phase 5 — console-ui (`store-core/console-ui`) — DONE (see update above)

Read `ARCHITECTURE.md` (tiers, shared controls), `CLAUDE.md` (the rules easiest to break), `lessons.md`
(~L1363 "a product type carries no attribute definitions", ~L1470 "variants, options and attributes" — mark
both CLOSED, append-only, pointing at the per-product model; the type tab itself stays untouched).

1. **Models** (`src/app/models/catalog.ts`): add `ReadableProductOption`, `ReadableProductOptionValue`,
   `PersistableProductOption(Value)`, `PersistableVariantSet`, `PersistableProductVariant`,
   `ReadableProductVariantDefinition`, `ReadableVariantOptionValue`; extend `ReadableProduct` with
   `variantCount`; extend `PersistableInventory` batch shape (`{entries: [{sku, inventory}]}`). Rewrite the
   file-header note that declares variants "deliberately not modelled".
2. **API tier**: new `api/catalog/product-option.service.ts`
   (`/spg/catalog/api/v1/private/product/option[s]`, unique probe) and
   `api/catalog/product-variant.service.ts` (`GET/PUT /spg/catalog/api/v2/private/product/{id}/variants`);
   extend `api/inventory/inventory.service.ts` with `bulkUpsert(entries)` (`PUT …/private/inventory/bulk`)
   and `deleteBySku(sku)`.
3. **Options tab** in `features/catalogue/`: `components/options-tab/` modelled on `brand-tab`
   (list + editor panel, per-language names via `copy-fields`/`locale-chips`, values managed inline —
   value rows carry ids so edits keep rows); wire into `catalogue.ts/html` tab switcher,
   `catalogue.facade.ts`, `catalogue.api.service.ts`, `catalogue-form.service.ts`. Delete guard: surface the
   409 `PRODUCT_OPTION.IN_USE` as a named toast.
4. **Variants step** in `features/product-form/` (new `components/variants-step/`), available to every saved
   product: pick axes from the store vocabulary (`app-autocomplete`, ordered), **auto-generate the cartesian
   product** with per-row remove + add-combination escape hatch; matrix columns: option values (read-only),
   sku (auto-suggest `<productSku>-<valueCodes>`, editable), price, quantity, available (writes the
   *inventory* flag), default (radio — exactly one). Save = `PUT …/variants` then `PUT inventory/bulk`, and
   delete inventory rows of removed skus via `deleteBySku` — **explicit orchestration, no
   `catchError → false` silent legs** (surface a retryable error state instead; this is a stated
   rewrite-over-patch decision). `pricing-step` stays for single-variant products (it edits the default
   variant transparently — no UX change).
5. **Products list** (`features/products/`): one sku per row (price/stock = default variant via existing
   `bySkus`); when `variantCount > 1` show an "N variants" tag and disable inline edit (edit in the form).
6. **Order detail** (`features/order-details/` or equivalent): render line `attributes`
   (option/value labels — already delivered by checkout's `ReadableOrderProduct.attributes`) under the name.
7. **Publish gating**: extend the readiness checklist (`ReadinessItem` in `product-form.facade.ts`) — every
   variant sku must have an inventory row/price; structural rules are already server-enforced by the PUT.
8. i18n: `src/locale/en.json` + `ar.json` (lint fails on unused AND on missing keys — transloco throws at
   runtime on missing). Gates: `npm run build` (AOT strict), `npm run lint`, `npm run test:ci`.

### Phase 6 — landing-ui (`store-pod/landing-ui`) — DONE (see update above)

Read `.claude/skills/project-structure/references/landing-ui.md`. The variant UI already exists in all 12
themes and is dead — this phase re-activates it against the new contract.

1. **Types** (`libs/types/src/product-groups.ts` L125–209): replace the deprecated shapes with
   `ProductOption {id, code, name, sortOrder, optionValues: [{id, code, name, sortOrder}]}` and
   `ProductVariant {id, sku, defaultVariant, sortOrder, optionValueIds}` (+ enrichment-attached
   price/quantity/canBePurchased); delete `ProductVariation`, two-slot fields, `ProductVariantInventory`,
   `ProductAttribute*`; `Product` gains `variantCount` (+`options`/`variants` on PDP). `hasVariants` stays a
   **derived** presenter predicate (`product-presenter.ts` already computes it from `options`) — keep
   `toListingProduct` stripping `variants` but keeping nothing variant-stored.
2. **Hook** (`libs/hooks/src/use-product-purchase.ts`): rebuild matching/default-selection/
   `isValueAvailable` over `optionValueIds` (build one `valueId → optionId` index from `options[]`).
   **Keep the public return API identical** so the 12 `BuyBox.tsx` files stay untouched; if any theme
   destructures `variation`/`variationValue`, that is a mechanical 12-file fix — check first.
   Add `?sku=` URL sync (`router.replace`, no reload) + preselect from the param.
3. **Enrichment** (`libs/services/src/inventory-service.ts`): listing stays one-sku-per-card (default sku;
   card shows the default variant's price, options hint from `variantCount > 1` — **never** load all
   variants on listing); PDP `enrichProduct` fetches availability for ALL the product's variant skus
   (switch to `POST /availability/query` above ~40 skus) and attaches per-variant price/qty. Formalize the
   merged pricing fields as a typed `PricedProduct` section instead of ad-hoc mutation.
4. **Facets** (`libs/services/src/product-category.ts` + `product-search-service.ts`): map the new
   `facets.options` groups (value ids round-trip as the already-sent `optionValueIds` param);
   `use-product-listing.ts` already toggles them.
5. **Search UX**: one card per product; a suggestion carrying `matchedVariantSku` links the PDP with `?sku=`.
6. Gates: `npm run build`, lint, hook unit specs if `libs/hooks` has a spec setup.

### Phase 7 — QA: RUN 2026-08-31, one real bug found and fixed

Stack reset and brought up clean (`lcl start -d`, profiles `lcl,test-stores`). There is no named
postgres volume — data lives in the container layer, so recreating the container IS the reset.

**Verified against the running stack:**

| What | Evidence |
|---|---|
| Schema initializes in final shape, no migrations | fresh DB: `catalog.product_variant` present, `catalog.product.sku` **gone**, all 5 option tables, dormant `product_image.product_variant_id`, `uk_product_variant_default` partial index, inventory's dropped columns absent + `uk_prd_avail_store_sku` + `sku NOT NULL`, `checkout.order_product_option` present and the two dead tables gone |
| Uniform-model invariant | every seeded product owns ≥1 variant; products 1 and 2 carry combinations, all others one `DEFAULT` |
| Search folds variant skus | `product_search_source` aggregates `string_agg(v.sku)` into weight B; querying `SKU-ZR-CL-DRS02-BL-L` returns **product 2 only**, one row per language |
| PDP | chips render; default preselected; Red/L greyed (no such combination) and Blue/L greyed (exists, qty 0 → `canBePurchased:false`); Blue swaps price 350→365 and the sku; URL syncs `?sku=`; a fresh load of that URL lands preselected |
| Cart line | reads **"Color: Blue / Size: M"** at the variant's own 365 — catalog → sku-addressed read → `variant` block → presenter → theme |
| Console Options tab | list with value summaries, editor, AR/EN locale chips, code locked on an existing record; **409 IN_USE delete guard** surfaces as the named toast and the option survives |
| Console Variants step | 5-step rail; axis chips; matrix rows carry the right sku/price/qty merged from inventory; exactly one default; readiness swaps `price` → **"Every variant has a price"**; per-row aria labels name their combination |
| Arabic / RTL | the whole variants step mirrors correctly; SKU and figures stay LTR |
| Products list | "2 variants" / "3 variants" badges, price = **default** variant's, and variant rows' edit action routes to the form while single-variant rows keep inline edit |

**The bug this caught** (commit `fix(landing-ui): the option wire field is values…`): the reshaped
`ProductOption` declared `optionValues`, but `ReadableProductOption` serialises `values`. Every
option was filtered out of `variantOptions()`, so a three-variant PDP rendered **no chips at all**.
tsc could not see it (optional-shaped wire data) and no theme test drives a real payload. Fixed in
the type + hook + 12 BuyBox files.

**Also fixed:** the console matrix's SKU column was 11rem, clipping every row to `SKU-ZR-CL-DRS02` —
the suffix is the only thing distinguishing rows. Now 15rem.

**Not exercised live** (covered by unit + integration tests): the variant-set save round-trip
(atomic PUT → inventory bulk → retired-sku delete), the storefront facet rail, and a full purchase
through to the order's option snapshot. The seeded stock made those non-trivial to drive without
mutating the demo data.

### Phase 7 — polish/QA (original notes)

Reset DBs (`lcl` postgres volume) → `lcl start -d` (profiles `lcl,test-stores`) → rebuild the search index
per store → walk the **Verification** section at the end of the plan file (console flow, storefront flow,
tenant isolation, and the performance evidence: exactly two s2s calls per cart load, one availability call
per listing page). Optionally extend `18-catalog-options-variants.sql`-style seeds to the other three stores.

## Build/test commands used throughout

```bash
./gradlew :store-pod:<svc>:<module>:test            # units
./gradlew :store-pod:<svc>:<svc>-service:integrationTest   # Testcontainers (Docker required)
./gradlew :store-pod:<svc>:<module>:check           # + checkstyle; run before every commit
```

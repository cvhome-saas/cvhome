# QA — product variants

The catalog/inventory split removed variants and parked the Shopizer-era code. This change brings them back as
a **different model**, and rewrites the console and storefront around it.

`catalog.product` is a pure definition — its `sku` column is gone. **Every product owns at least one
`catalog.product_variant`**, and sku, price and stock always live at the variant level: a product with no
options owns exactly one *default* variant, a product with options owns one variant per sold combination.
Option vocabulary (`product_option` / `product_option_value`) is store-wide, so value ids are shared and
id-based faceting works. Sellability is inventory's single `available` flag. Axes and combinations are written
**atomically together** (`PUT /api/v2/private/product/{id}/variants`), so they can never desync.

- **Scope** — catalog · inventory · checkout's order lines · console-ui Catalogue (Options tab), Product form
  (Variants step) and Products list · landing-ui PDP, listing facets, cart and order lines, all 13 themes
- **Change** — PR #306, branch `feat/product-variants`. Plan:
  `.claude/plans/support-product-variants-serialized-snowflake.md`; running notes:
  `.claude/plans/product-variants-handoff.md`.
- **Related** — [`qa/catalog-and-inventory.md`](catalog-and-inventory.md), whose ARC-01 and ARC-04 this change
  supersedes (the `product_option*` tables are back, and `catalog-deprecated` is deleted).

Tags: `[verified]` run end to end against the local stack · `[tests]` covered by automated tests only ·
`[not run]` reasoned about but never executed.

---

## SCHEMA — the no-migration stance

### SCH-01 — A fresh database initializes in the final shape · high · [verified]

- **Steps** — drop the stack's postgres container, `lcl start -d`, then `\d catalog.*`.
- **Expect** — `product_variant`, `product_variant_option_value`, `product_option`, `product_option_value`,
  `product_option_assignment` and the two description tables exist; **`catalog.product` has no `sku` column**;
  `product_image.product_variant_id` exists and is nullable (dormant, for per-variant images later).
- **Result** — all present, `product.sku` absent. There are no `ALTER`-style migration blocks anywhere in this
  change: `schema.sql` is edited to its final shape and databases are recreated.

### SCH-02 — The constraints that make the model safe exist · high · [verified]

- **Expect** — `uk_product_variant_sku (store_merchant_id, sku)`, `uk_product_variant_signature
  (product_id, option_signature)`, and the partial unique index `uk_product_variant_default on
  (product_id) where default_variant` — that last one is what makes "exactly one default per product" a
  database fact rather than a convention.
- **Result** — all three present.

### SCH-03 — Inventory reached its final shape too · [verified]

- **Expect** — `product_availability` has no `product_variant` / `region` / `region_variant` columns,
  `sku` is `NOT NULL`, and `uk_prd_avail_store_sku (store_merchant_id, sku)` exists.
- **Result** — confirmed. `checkout.order_product_option` exists; `order_product_attribute` and
  `shopping_cart_attr_item` are gone.

### SCH-04 — The old drop script no longer names a live table · high · [verified]

- **Why it matters** — `extra/scripts/drop-catalog-inventory-tables.sql` is documented to be run
  post-verification, and it used to `drop table catalog.product_variant`. The rework **reused that name** for
  the live table holding every product's sku, so running the script as written would have destroyed the whole
  catalogue's variants.
- **Result** — that drop is removed and the reason is written into the script beside the remaining ones, which
  are the genuinely dead Shopizer tables (`product_var_image*`, `product_variant_group`, `product_variation`).

---

## MODEL — the invariant

### MOD-01 — Every product owns at least one variant · high · [verified]

- **Steps** — after seeding, group `catalog.product_variant` by product.
- **Expect** — no product without a variant; products with no options carry exactly one row with
  `option_signature = 'DEFAULT'` and the sku the merchant typed.
- **Result** — holds across all four demo stores.

### MOD-04 — The demo stores are mostly multi-variant · high · [verified]

The seeds started with two or three showcase products per store, which measured nothing: a listing where 43
of 45 products carry one sku exercises none of the variant paths at page scale. The stores are now bulk data
as well as demo data.

- **Steps** — `extra/scripts/generate-demo-variants.py` regenerates the two seed 18 files per store; drop the
  `catalog` and `inventory` schemas, restart both services, then count.
- **Expect** — at least 75% of every store's products sell by more than one variant, with a spread of matrix
  shapes and a deliberate optionless remainder.
- **Result** — **36 of 45 (80%) per store**, 590 variants and 590 inventory rows in total (was 207), up to 6
  per product, 34 option values across the four vocabularies. Nine products per store stay optionless as the
  control case, including the two the tests pin: product 3 (`ProductVariantApiIntegrationTest` turns it
  multi-variant and back) and product 4 (`ProductApiIntegrationTest`'s no-selection cart line). The curated
  showcase products — 1, 2, 46–48, 91–93, 136–138 — are untouched, so SF-01's deliberately-missing red/L
  combination still exists.
- **Integrity, checked in SQL** — 0 variants without an inventory row · 0 `option_signature` values
  disagreeing with the variant's own option-value rows · 0 variants whose option count differs from their
  product's axis count · 0 products without a default variant · 0 duplicate signatures within a product.

### MOD-02 — A combination sku resolves to one product in search · [verified]

- **Steps** — query the search index for `SKU-ZR-CL-DRS02-BL-L`.
- **Expect** — one hit per language for the **parent** product, never one row per variant. The
  `product_search_source` view folds `string_agg(variant skus)` into weight B, replacing the old `p.sku`.
- **Result** — product 2 only, in both `en` and `ar`.

### MOD-03 — The typed refusals · [tests]

- Covered by `ProductVariantApiIntegrationTest`: a variant missing an axis → **400**
  (`PRODUCT_VARIANT.OPTIONS_INVALID`), a duplicate combination → **409**, a sku another product owns → **409**,
  an unknown option code → **404**. Note the limit guard is **400**, not the 422 the plan predicted — its
  category is `VALIDATION`.
- Option deletes are refused **409** (`PRODUCT_OPTION.IN_USE`) while a product assigns the option or a variant
  uses one of its values.

---

## SF — storefront

### SF-01 — The PDP selects a variant · high · [verified]

- **Steps** — open the seeded Zara dress (colour × size, red/L deliberately absent).
- **Expect** — chips for both axes; the default variant preselected; **Red/L greyed** because the combination
  does not exist and **Blue/L greyed** because it exists with quantity 0 (inventory says not purchasable);
  selecting Blue swaps the price 350 → 365 and the sku to `SKU-ZR-CL-DRS02-BL-M`.
- **Result** — exactly that. Repeated on the electronics store's six-combination iPhone: 512 GB + Silver reads
  $1,339.00 (999 + 300 + 40 as seeded) and "Out of stock", which is the seeded zero-stock combination.

### SF-02 — A variant is addressable and shareable · high · [verified]

- **Expect** — selecting a combination writes `?sku=<variantSku>` with `history.replaceState` (no reload, no
  server re-render), and loading that URL cold lands preselected on that variant.
- **Result** — both directions confirmed.

### SF-03b — A purchase carries the combination all the way to the order · high · [verified]

- **Steps** — signed in as the demo shopper, cart holding one combination line (Zara dress Blue/M, SAR 365)
  and one optionless line (Gucci bag) as the control; Cash on Delivery, order placed.
- **The snapshot** — `checkout.order_product_option` gained **exactly two rows, both on the dress line**:
  `color`/`Color`/`blue`/`Blue` (sort 0) and `size`/`Size`/`m`/`M` (sort 1). The bag's line has none. Codes
  *and* names are stored, which is what lets an order keep saying what was bought after an option is renamed
  or deleted. `order_product.product_name` is the real localized name — the `"Product {sku}"` placeholder the
  rework set out to fix is gone.
- **Stock** — decremented on the bought sku **only**: `SKU-ZR-CL-DRS02-BL-M` 8 → 7, while the product's
  default variant stayed at 40 and its Blue/L variant at 0. Two variants of one product really are
  independent inventory rows.
- **Both order views render it** — the console order detail shows `Color: Blue · Size: M` between the name and
  the sku; the storefront's own order view shows `Color: Blue / Size: M` under the name. The optionless line
  shows nothing on either, which is the control.

### SF-03 — The cart line names the combination · high · [verified]

- **Expect** — adding Blue/M gives a line reading **"Color: Blue / Size: M"** at the variant's own price.
- **Result** — confirmed. The labels are the placement-time snapshot, never re-joined from the catalog, so an
  order keeps saying what was bought after the option is renamed or deleted.

### SF-04 — Listing cards stay one-sku-per-product · [verified]

- **Expect** — a card shows the **default** variant's price and never loads the variant rows; `variantCount`
  is the only variant fact a listing payload carries.
- **Result** — `toListingProduct` strips `options` and `variants`; the listing enrichment is one availability
  call for the page's default skus.

### SF-05 — The facet rail filters by option value · high · [verified]

- **Steps** — the seeded Dresses category, whose two products give the rail something to count.
- **Expect** — counted groups per option, a click narrowing the listing and putting the value in the URL, and
  the AND across options anchored to a **single variant**.
- **Result** — the rail renders `FILTER BY COLOR` (Red (1), Blue (1)) and `FILTER BY SIZE` (M (1), L (1))
  beside the pre-existing manufacturer facet. Red alone narrows 2 → 1 with `?options=1`. **Red + L
  (`?options=1,4`) answers "No products" while L alone answers 1** — so the empty result is the anchoring
  and not an empty catalogue, matching the integration test exactly.
- Also confirmed on those cards: the variant product offers *view details* while the simple one offers
  quick-add, which is the card contract deriving `hasVariants` from `variantCount`.
- Still not run: a suggestion carrying `matchedVariantSku` deep-linking the PDP with `?sku=`.

---

### SF-06 — The buy box respects the merchant's per-order limits · high · [verified]

Reported from the running stack: adding 2 of the Zara dress answered 422 `Product SKU-ZR-CL-DRS02 sells
between 1 and 1 per order; 2 was asked.`, and the storefront rendered "Failed to add product to cart."
Two defects behind one symptom.

- **The buy box ignored limits the API publishes.** `quantityOrderMinimum/Maximum` are per sku and reach the
  storefront on every availability read, but `applyVariantInventory` dropped them and `useProductPurchase`
  built its stepper from stock alone — so it offered a quantity the cart was always going to refuse. The
  cart's own `requireQuantityInRange` even says "the storefront clamps client-side"; it did not.
- **Fix** — the bounds ride on `VariantPricing` and enrichment copies them; the stepper's ceiling is
  `min(stock, quantityOrderMaximum)` with `0` meaning no limit, its floor is `quantityOrderMinimum`, and
  `isOutOfStock` covers a floor no stock can reach. `maxQty` deliberately keeps meaning **units on hand** —
  the themes print it as "Only N left", and 8 in stock with a limit of 1 is not "only 1 left".
- **Steps** — open `SKU-NK-RUN-001` (25 in stock, capped at 1 per order) and a generated variant product
  (`SKU-NK-CL-KHD07`, uncapped).
- **Result** — the capped product shows "In stock", quantity pinned at 1 with **both** stepper buttons
  disabled; the uncapped one increments freely. Sizes render S · M · L in that order after the seed's
  `sort_order` fix.

### SF-07 — A refusal says what was actually refused · high · [verified]

`locales/*.json` has carried a message per error `code` since the error contract landed, and **nothing read
them**: every interactive failure notified one fixed string per action, so a quantity cap, an offline
browser and a declined card were all "Failed to add product to cart" / "Failed to place order".

- **Fix** — `useErrorMessage()` resolves code → the caller's own fallback → category → generic, interpolating
  the problem's `params`; wired into add-to-cart (both hooks), cart quantity, remove and checkout.
- **The code was wrong too.** The range refusal reused `CHECKOUT.CART.PRODUCT_NOT_PURCHASABLE`, whose
  contract says the item is not sellable at all and retrying will not help — the opposite of "buy fewer and
  it works". It now raises `CHECKOUT.CART.QUANTITY_OUT_OF_RANGE`, still 422, carrying `sku`, `quantity`,
  `minimum` and `maximum` so the message can name the numbers. Pinned by
  `ProductNotPurchasableExceptionTest`.
- **Steps** — the cart drawer's stepper is deliberately server-guarded rather than clamped (a line's bounds
  are not on the cart payload), so it is the reachable path: put the capped `SKU-NK-RUN-001` in the cart and
  press +.
- **Result** — `POST /api/v1/cart` answers `CHECKOUT.CART.QUANTITY_OUT_OF_RANGE`, and the toast reads **"You
  can order between 1 and 1 of this item — 2 isn't allowed."** Translated in all five locales; the ICU
  plural renders "at least {minimum}" when the maximum is the `0` no-limit sentinel.

### SF-08 — The demo stores can actually sell more than one of something · [verified]

The fashion and beauty seeds set `quantity_ord_max = 1` on every row, so with the limits now enforced client
side every stepper in those stores would have been inert.

- **Fix** — both stores get a spread (about half unlimited, the rest 2/3/5/10). Cars stays at 1 throughout,
  which is right for a car and keeps a whole store exercising the cap; electronics was already 2–10.
  `SKU-NK-RUN-001` keeps its cap of 1 deliberately as the fixture SF-06 and SF-07 test against.
- **Result** — fashion 22 unlimited · 1 capped at 1 · the rest 2–10; beauty 23 unlimited and no row left at 1.

---

## CON — console

### CON-01 — The Options tab · high · [verified]

- **Expect** — a fifth Catalogue tab listing the store vocabulary with each option's values summarised; the
  editor writes the whole document (values carrying their id keep their row, and therefore the store-wide value
  id every variant references); per-language names park across a locale switch.
- **Result** — confirmed. Deleting an in-use option surfaces the 409 as the named toast ("This option is still
  used by a product or one of its variants…") and the option survives.

### CON-02 — The Variants step · high · [verified]

- **Expect** — a fifth wizard step, locked until the product is saved; axes picked from the vocabulary generate
  the cartesian matrix; each row carries sku, price, quantity, available and a default toggle, with exactly one
  default; the step saves itself (atomic catalog PUT, then the inventory bulk upsert and retired-sku cleanup).
- **Result** — matrix rows carry the right per-row price and stock merged from inventory, exactly one default,
  and every control is labelled with the combination it belongs to ("Price for Red / M").
- **The save round-trip, both directions, driven from the UI on the seeded simple product 3:**
  - *Adding an axis* — picking Colour generated the two combinations, **seeding row 1 from the product's own
    sku, price and stock** (SKU-AD-CL-TPT03 / 320 / 35) and suggesting `…-BLUE` for the second. The readiness
    item swapped to "Every variant has a price", went unmet, dropped the product to 86% and blocked publish
    until the second row was priced — then returned to 100% the moment it was.
  - *Saving* — "Variants saved." Catalog got both variants with the right signatures, exactly one default and
    the assignment row; inventory got both skus priced (320/35 and 345/12). The storefront PDP picked it up
    with no further action: `variantCount: 2`, Colour chips, Red and Blue.
  - *Removing the axis* — "Remove variants — sell as one SKU again" restored a single `DEFAULT` variant
    **keeping the original sku**, cleared the assignment, and **deleted only the retired sku's inventory row
    while the surviving default kept its price and stock**. That is the specific hazard the post-write diff
    exists for (diffing against the request instead would have deleted the restored default's row), confirmed
    live rather than reasoned about.
- **Fixed during QA** — the SKU column was 11rem, which clipped every row to `SKU-ZR-CL-DRS02`; the suffix is
  the only thing distinguishing rows, so the column was showing nothing useful. Now 15rem.

### CON-03 — Publish gating · [verified]

- **Expect** — with options assigned, the readiness checklist swaps its "price" item for **"Every variant has a
  price"**, and publish stays blocked until every combination sku is priced.
- **Result** — confirmed; the pricing step defers to a pointer at the matrix so one number has one home.

### CON-04 — The products list · high · [verified]

- **Expect** — one row per product: an "N variants" badge, the **default** variant's price, and the product's
  **total** stock across its variants. Inline edit is disabled on a variant row (it writes one sku and the row
  stands for several) and routes to the form instead.
- **Result** — Nike reads 37 (25 + 12), the Zara dress 46 (38 + 8 + 0), single-variant rows unchanged.
- **Fixed during QA** — the row previously showed the **default variant's** quantity as the product's, because
  the listing payload carries only the default sku. Inventory gained a product-addressed bulk read
  (`GET /private/inventory/by-products`) and the row totals from it.

### CON-05 — Arabic / RTL · [verified]

- **Expect** — the Variants step mirrors correctly, with SKU and figures staying left-to-right.
- **Result** — confirmed, including the matrix column order and the axis chips.

### CON-06 — Responsive · [verified]

- **Expect** — the matrix is wide, so it must scroll **inside its own container** and never make the page
  scroll sideways; the axis chips, the add-combination row and the footer all wrap.
- **Result** — at a 360px panel the page does not scroll sideways, the scroller fits, the table overflows into
  its own scroll and no input is clipped. The Options tab uses the shared `.split`, which stacks below 1100px.

---

## PERF — the query shape

### PERF-01 — The listing is flat in the number of products · high · [verified]

- **Steps** — Hibernate SQL logging on, request the same listing at `count=5`, `20` and `45`, count statements.
- **Found first** — a 20-product page issued **100** statements: 20 each for images, descriptions and
  categories, plus per-entity loads for brands and types. `findAllHydrated` fetch-joins those, but the listing
  pages ids through `search(...)` and maps entities directly, so nothing batched them. **This predates the
  variant work** — the variant collections were the only ones already batched and measured 1 query for 20
  products.
- **Fix** — `@BatchSize(100)` on `Product.{categories,descriptions,images}`, on the description collections of
  `Category`/`Manufacturer`/`ProductType`, on `ProductVariant.optionValues`, and at class level on
  `Manufacturer` and `ProductType` so their lazy proxies initialise in one query.
- **Result** — 5 products → 12 statements, 20 → 15, 45 → 13. Flat; the variance is the background outbox
  poller. Roughly 9 statements belong to the request, each a bounded `IN` query.
- **Re-measured after MOD-04 tripled the variant count** — the same 45-product listing issues **11**
  statements and answers in 14–37 ms; a whole store's 137 skus priced in one availability call takes 39 ms.
  Nothing about the query shape depends on how many variants the catalogue holds.

### PERF-02 — The PDP is flat in the number of variants · high · [verified]

- **Steps** — same method, on a 3-variant product and a 6-variant one.
- **Result** — **12 statements each**. Doubling the variants adds no queries: the PDP hydrates through one
  fetch-join (`findByProductIdHydrated`) and the option values are batched. Re-measured on the expanded seed:
  9 statements for a generated 3-variant product, 15 ms.

### PERF-03 — Cart and listing service-to-service calls · [tests]

- `ProductDetailsComposer` is batch-only: one catalog call and one inventory call per cart or order, whatever
  the line count. Covered by `ProductDetailsComposerImplTest`; not re-measured on the running stack here.

---

## Known gaps

- **SF-05** (facet rail, `matchedVariantSku` deep link) is wired and type-checked but never run end to end.
- **A suggestion's `matchedVariantSku` deep link** — the only interaction in this feature never driven end to
  end. Everything it depends on is verified (suggest returns the field, the provider maps it into the href,
  and `?sku=` preselection works — SF-02), so what is untested is the wiring between them.

Everything else in this file has now been run against the stack. The chain the feature exists to serve —
catalogue option → product variants → storefront selection → cart → checkout → placement → order snapshot →
both order views, with stock moving on the right sku alone — is verified end to end.
- **`ProductAttribute*`** remains in landing-ui's types, documented as dead on the wire: every theme's product
  page renders a specifications block from it that degrades to nothing. Descriptive attributes are a stated
  future feature — delete the shape together with those blocks, or revive it when the feature lands.

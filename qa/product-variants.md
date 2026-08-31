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

### SF-03 — The cart line names the combination · high · [verified]

- **Expect** — adding Blue/M gives a line reading **"Color: Blue / Size: M"** at the variant's own price.
- **Result** — confirmed. The labels are the placement-time snapshot, never re-joined from the catalog, so an
  order keeps saying what was bought after the option is renamed or deleted.

### SF-04 — Listing cards stay one-sku-per-product · [verified]

- **Expect** — a card shows the **default** variant's price and never loads the variant rows; `variantCount`
  is the only variant fact a listing payload carries.
- **Result** — `toListingProduct` strips `options` and `variants`; the listing enrichment is one availability
  call for the page's default skus.

### SF-05 — Facet rail and search UX · [not run]

- The rail maps `facets.options` into counted checkbox groups ("Red (12)") wired to the already-sent
  `optionValueIds`; a suggestion carrying `matchedVariantSku` deep-links the PDP with `?sku=`. Wired and
  type-checked, but not exercised against the running stack.

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

### PERF-02 — The PDP is flat in the number of variants · high · [verified]

- **Steps** — same method, on a 3-variant product and a 6-variant one.
- **Result** — **12 statements each**. Doubling the variants adds no queries: the PDP hydrates through one
  fetch-join (`findByProductIdHydrated`) and the option values are batched.

### PERF-03 — Cart and listing service-to-service calls · [tests]

- `ProductDetailsComposer` is batch-only: one catalog call and one inventory call per cart or order, whatever
  the line count. Covered by `ProductDetailsComposerImplTest`; not re-measured on the running stack here.

---

## Known gaps

- **SF-05** (facet rail, `matchedVariantSku` deep link) is wired and type-checked but never run end to end.
- **A full purchase** through to the order's `order_product_option` snapshot was not run: the seeded stock made
  it awkward to drive without mutating demo data. The placement path is covered by checkout's integration tests.
- **The variant-set save round-trip** was not driven from the console UI; it is covered by
  `ProductVariantApiIntegrationTest` and the console facade specs.
- **`ProductAttribute*`** remains in landing-ui's types, documented as dead on the wire: every theme's product
  page renders a specifications block from it that degrades to nothing. Descriptive attributes are a stated
  future feature — delete the shape together with those blocks, or revive it when the feature lands.

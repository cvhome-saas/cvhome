# QA — catalog (`store-pod/catalog/catalog-service`)

Catalog owns the product definition and everything a shopper browses by: products and their copy, the category
tree, brands, product types, groups and the image gallery. It owns no prices and no stock — those are
[inventory](../../../inventory/inventory-service/qa/inventory-qa.md), and the two are composed for the shopper
by [checkout](../../../checkout/checkout-service/qa/checkout-qa.md).

> **Superseded in part (2026-08-31, the variant rework).** The ARC cases below record the catalog/inventory
> split, at which point variants and product options were removed and parked. They are **back** — a store-wide
> option vocabulary assigned per product, and a uniform variant model in which every product owns at least one
> `catalog.product_variant` and sku/price/stock live at the variant level. The ARC entries are marked where that
> changed them; everything else still holds, because the rework kept the same wire contract again.
> `store-pod/catalog-deprecated` is deleted — the Shopizer-era model it parked was a reference for what not to
> repeat, and the rework did not reuse it.

- **Scope** — `/api/v2/products`, `/api/v2/product/**`, category, manufacturer, product-type, product-group and
  product-image APIs; the console's Catalogue module as a client; the billing write gate on catalog writes
- **Runs on** — `lcl start -d --stack <name>`; read the live port from `lcl urls`. Address it through the
  gateway, never `:8122`
- **Cases** — 95 (33 verified, 1 unit only, 61 not verified)
- **Also see** — inventory (stock and price), checkout (the composed cart line),
  [content](../../../content/content-service/qa/content-qa.md) (the media library the gallery reads from),
  [billing](../../../../store-core/billing/billing-service/qa/billing-qa.md) (the plan ceiling behind PRD-14/15)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading: one is
defects that have already happened, the other is behaviour that looks wrong and is expected.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins, the seeded org/store/pod ids, gateway-vs-pod
addressing and the `psql` idiom are in
[`references/qa-testing.md`](../../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5.
Only what is specific to catalog is below.

---

### The demo catalog (org1-store1 · `65f023632bc46470c104b76f`)

Every demo store carries the same shape with different products; org1-store1 is the one the `.http` files and
this document assume.

| What | Seeded |
|---|---|
| Products | 45, each with `en` **and** `ar` copy, 5 images, a brand, a type, two categories. First three: `SKU-NK-RUN-001` (`nike-zoomx-invincible-run-3`), `SKU-ZR-CL-DRS02`, `SKU-AD-CL-TPT03` |
| Categories | 12 in a two-level tree: roots `MEN` (id 1), `WOMEN`, `KIDS`, `ACCESSORIES`; children such as `MEN_SHOES`, `WOMEN_DRESSES`, `KIDS_BOYS` |
| Brands | `ADIDAS`, `CHANEL`, `GUCCI`, `H&M`, `NIKE`, `ZARA` |
| Product types | `CLOTHING`, `SHOES`, `ACCESSORIES`, `BAGS` |
| Groups | `HOME_PAGE` (24), `RECOMMENDED` (12), `NEWLY_ADDED` (15), `FEATURED_ITEMS` (10) — the four strips the home page renders. No `RELATED_ITEM` groups are seeded |
| Inventory | one `product_availability` row + one `base` price per product (`SKU-NK-RUN-001`: 25 in stock, 750.00; note the PR's QA left it at **23** — see [00 → state](#state-the-prs-qa-left-behind)) |

org1-store2 (`65f023632bc46470c104b75f`) is the **same org, other store** — use it for "another store" cases;
org2-store1 (`65f020632bc46470c104b76f`) for "another org".

### Addressing

```
http://gateway.com:8000/spg/catalog/api/v2/...?store=<id>&pod=507f1f77bcf86cd799439011&lang=en     # seller path
http://gateway.com:8000/spg/inventory/api/v1/...?store=<id>&pod=507f1f77bcf86cd799439011           # seller path
http://org1-store1.spg-507f1f77.gateway.com/catalog/api/v2/...?store=<id>&lang=en                  # pod path
http://org1-store1.spg-507f1f77.gateway.com/inventory/api/v1/...?store=<id>                        # pod path
```

The platform gateway route predicates on `pod` as well as `store`. Runnable blocks, one file per controller,
addressed through the gateway: `store-pod/catalog/catalog-service/http/{product,category,manufacturer,
product-type,product-group,product-image}-api.http` and
`store-pod/inventory/inventory-service/http/{inventory,reservation}-api.http`. The seller session id comes from
the gitignored `http-client.private.env.json`; the reservation blocks need a token with same-pod access (a
seller session works locally because `STORE-POD.INVENTORY.RESERVE` maps to same-pod).

**Two language conventions on one API.** Storefront reads answer the `lang` asked for in `description`.
Private (console) reads answer that **and** every language in `descriptions`. A case that says "every
language" means the `descriptions` array.

---

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select product_id, sku, available, product_ship, sort_order, manufacturer_id, product_type_id
     from catalog.product where store_merchant_id='65f023632bc46470c104b76f' order by product_id;"
... "select category_id, code, parent_id, depth, lineage, sort_order, visible from catalog.category
       where store_merchant_id='65f023632bc46470c104b76f' order by lineage;"
... "select g.code, count(pp.product_id) from catalog.product_group g
       left join catalog.product_group_product pp on pp.product_group_id=g.product_group_id group by 1;"
```

Logs: `.lcl/<stack>/logs/catalog.log`. An `Unhandled failure [traceId=…]` line in it is a defect regardless of
what the screen showed.

### State an earlier QA pass left behind

The PR #282 smoke tests reserved stock on org1-store1: `SKU-NK-RUN-001` is at **23** units rather than the
seeded 25. That is inventory's row — see inventory's `## 00` for the reset — and it changes what LST-01 and the
GRP strips show as available.

---

---

## LST — The product listing (`GET /api/v2/products`)

One endpoint serves both the storefront's category page and the console's product table. It is **public**:
the console reads its own catalogue through it on purpose (see [99](#99--known-gaps)). Filters: `sku`
(substring, case-insensitive), `available`, `categoryIds`, `manufacturerId`; paging `page`/`count`;
`sort=<column>,<dir>` on direct `Product` columns only.

### LST-01 — The listing answers with the product shape the storefront renders · critical · [verified]

- **Steps** — `GET /catalog/api/v2/products?store=<org1-store1>&lang=en&count=3` on the pod path.
- **Expect** — 200, `totalElements: 45`, each row with `sku`, `description.name` in English,
  `description.friendlyUrl`, `image.imageUrl` (a `localhost:9000` MinIO path), `images[]`, `manufacturer.code`,
  `type.code`, `categories[]` (two per seeded product), `productSpecifications` with `cm`/`kg` units — and
  **no** `price`, `quantity`, `finalPrice`, `canBePurchased` or `attributes` fields at all.

### LST-02 — A single category filter widens to its subtree · critical · [verified]

- **Steps** — `?categoryIds=1` (`MEN`, a root).
- **Expect** — `totalElements: 20` — the products in `MEN_TOPS`, `MEN_BOTTOMS` and `MEN_SHOES`, none of which
  are attached to `MEN` itself. Then `?categoryIds=<MEN_SHOES id>`: only those.

### LST-03 — Two category ids are taken literally · high · [not verified]

- **Steps** — `?categoryIds=<MEN id>&categoryIds=<WOMEN id>`.
- **Expect** — the union of products *directly* in those two — which for the seeds is **0**, because only a
  single id widens to a subtree. This is the legacy rule kept verbatim; the storefront only ever sends one.

### LST-04 — The sku filter is a case-insensitive substring · high · [verified]

- **Steps** — `?sku=nk-run`.
- **Expect** — exactly `SKU-NK-RUN-001`. `?sku=NK` returns every Nike sku.

### LST-05 — Brand and availability filters · high · [not verified]

- **Steps** — `?manufacturerId=<NIKE id>`; `?available=false`.
- **Expect** — only Nike products; then only products a merchant has hidden (none seeded — hide one with
  PRD-08 first and it must appear here and disappear from `?available=true`).

### LST-06 — Sorting on a direct column works; a joined column is a 500 · high · [verified] / [not verified]

- **Steps** — `?count=2&sort=dateAvailable,desc` (verified: answers `SKU-AD-SH-SLD45, SKU-ZR-CL-BLS44`).
  Then `?sort=description.name,asc`.
- **Expect** — the second is a **500** with a traceId. Known and documented in `libs/types/listing.ts`
  (`SORT_MAP` only exposes `dateAvailable`); it is listed here so nobody widens the storefront sort menu
  without also fixing the query.

### LST-07 — Paging envelope · [verified]

- **Steps** — `?count=5&page=1`.
- **Expect** — `size: 5`, `pageNumber: 1`, `totalPages: 9`, `totalElements: 45`. `count`, not `size`, is the
  page-size parameter platform-wide.

### LST-08 — Another store's ids do not leak · critical · [verified]

- **Steps** — `?store=<org2-store1>&categoryIds=1` (category 1 belongs to org1-store1).
- **Expect** — an empty page (or org2-store1's own category 1 if ids collide across stores — check the
  `store_merchant_id` of what comes back; it must be org2-store1's).

### LST-09 — Every language a product has, but one at a time · [not verified]

- **Steps** — `?lang=ar&count=1`.
- **Expect** — `description.name` in Arabic, `description.language: "ar"`; no `descriptions` array on the
  public listing.

---

---

## PDP — The product page (`GET /api/v2/product/name/{friendlyUrl}`)



### PDP-01 — A product resolves by its slug in the shopper's language · critical · [verified]

- **Steps** — `/catalog/api/v2/product/name/nike-zoomx-invincible-run-3?store=<org1-store1>&lang=en`.
- **Expect** — 200, `sku: SKU-NK-RUN-001`, `description.friendlyUrl` echoing the slug, `type.code: SHOES`,
  `manufacturer.description.name: Nike`, `productSpecifications` with `height 12, length 30, weight 0.8,
  width 10, dimensionUnitOfMeasure cm, weightUnitOfMeasure kg`, five `images` with `image` set to the default.

### PDP-02 — The slug is per language · high · [not verified]

- **Steps** — the same slug with `lang=ar`; then the Arabic slug (read it from LST-09) with `lang=ar`.
- **Expect** — the English slug under `lang=ar` is a **404** `CATALOG.PRODUCT.NOT_FOUND`; the Arabic slug
  answers. Slugs are matched in the language asked for, which is what the storefront's per-locale URLs rely on.

### PDP-03 — A hidden product has no page · critical · [not verified]

- **Steps** — hide a product (PRD-08), then request its slug.
- **Expect** — **404**. `available=false` means the shop does not show it, and the product page is part of
  the shop. The listing with `?available=false` still returns it (LST-05) — that is the console's view.

### PDP-04 — An unknown slug is a typed 404 · high · [verified]

- **Expect** — 404 with `CATALOG.PRODUCT.NOT_FOUND` and a traceId; not a 500, not an empty 200.

> The storefront's own render of this page is **PDP-05**, now in [landing-ui](../../../landing-ui/qa/landing-ui-qa.md).

---

## PRD — Product definition (console: create, read, update, small writes)

`POST/PUT /api/v2/private/product`, `GET /api/v2/private/product/{id}`, `PATCH/DELETE
/api/v1/private/product/{id}`, `GET /api/v1/private/product/unique`, category membership. The console's
product form writes the definition here and the price/stock to inventory in a **second call** (INV-05).

### PRD-01 — Create a product with the minimum body · critical · [not verified]

- **Steps** — block one of `product-api.http` (`sku`, `visible`, `shipeable`, a box, one `en` description).
- **Expect** — **201** `{"id": n}`; `GET /private/product/{n}` shows `visible: true`, `identifier == sku`,
  `descriptions` with one entry, empty `categories`, no `type`, no `manufacturer`, `dateAvailable` set to now.
  In the DB: one `product` row and one `product_description` row; **no** inventory row — stock and price are
  a separate write (INV-05). `sm_sequencer` `PRODUCT_SEQ_NEXT_VAL` advanced.

### PRD-02 — Create with brand, type and categories by code / id · critical · [not verified]

- **Steps** — body with `"manufacturer": "NIKE"`, `"type": "SHOES"`, `"categories": [{"id": 1}, {"code":
  "MEN_SHOES"}]`.
- **Expect** — 201; the definition read shows `manufacturer.code NIKE` with **every language** in
  `manufacturer.descriptions`, `type.code SHOES`, two categories each with `descriptions`. Brand and type are
  addressed by **code**, categories by id *or* code.

### PRD-03 — An unresolvable reference is a 400 naming the reference · critical · [not verified]

- **Steps** — `"manufacturer": "no-such-brand"`; then `"type": "nope"`; then `"categories": [{"id": 999999}]`;
  then `{"code": "NOPE"}`.
- **Expect** — 400 with `CATALOG.MANUFACTURER.REFERENCE_UNRESOLVABLE`, `CATALOG.PRODUCT_TYPE.REFERENCE_
  UNRESOLVABLE`, `CATALOG.CATEGORY.REFERENCE_UNRESOLVABLE` (twice) — each carrying the offending value in
  `params`. Nothing is written (check the product count).

### PRD-04 — A reference belonging to another store is unresolvable, not forbidden · critical · [not verified]

- **Steps** — as org1-store1, `"categories": [{"id": <a category id of org2-store1>}]`.
- **Expect** — 400 `REFERENCE_UNRESOLVABLE`, **not** a 403 and not a success. Every lookup is scoped by store
  first; a foreign id simply does not exist here, and the response must not confirm that it exists elsewhere.

### PRD-05 — Validation is a 400 with fields, never a 500 · high · [not verified]

- **Steps** — `{"descriptions": []}` (no sku); `"sku": "has space"`; `"sku": "x/y"`.
- **Expect** — 400 ProblemDetail with a `fields` entry for `sku` (`@NotEmpty`, then the `^[a-zA-Z0-9_-]*$`
  pattern). A body with a description that has no `language` — record what happens; the entity column is
  NOT NULL, so today that is a **500** from the database (see [99](#99--known-gaps)).

### PRD-06 — Duplicate sku in the same store · high · [not verified]

- **Steps** — create `HTTP-DEMO-001` twice.
- **Expect** — the second is refused. The unique constraint is `(store_merchant_id, sku)`; the console asks
  `/private/product/unique?code=` first (PRD-07) and disables Save, so the raw API answer is what you are
  checking: record whether it is a typed 409 or a bare 500 — it is **expected to be a 500 today**
  ([99](#99--known-gaps)). The same sku in org1-store2 must succeed.

### PRD-07 — The sku check · high · [not verified]

- **Steps** — `GET /private/product/unique?code=SKU-NK-RUN-001`; then `?code=FREE-001`; then the same two as
  org1-store2.
- **Expect** — `{"exists": true}`, `{"exists": false}`, and `false` for both from the other store.

### PRD-08 — The inline switches (`PATCH`) touch nothing else · critical · [not verified]

- **Steps** — read the definition of product 1; `PATCH /api/v1/private/product/1` with
  `{"available": false, "productShipeable": true}`; read again.
- **Expect** — only `visible` (the catalog's name for `available`) changed; descriptions, images, categories,
  brand, type, box and sort order untouched. Both fields are Java primitives: a body that omits one sets it
  to **false** — the console always sends both.

### PRD-09 — Update replaces the editable fields and merges copy by language · critical · [not verified]

- **Steps** — `PUT /private/product/{id}` with a renamed `en` description, a new `fr` description, a changed
  box, `sortOrder: 5`, and the `ar` description **omitted**.
- **Expect** — 200 empty; the read shows `en` renamed **with the same description id as before** (merged, not
  recreated), `fr` added, `ar` **gone** (a language absent from the body is removed — the console always
  sends every language it loaded), the new box and sort order. Images are untouched: they are not part of the
  definition.

### PRD-10 — Update cannot move a product between stores · critical · [not verified]

- **Steps** — `PUT /private/product/<org1-store1 product>?store=<org1-store2>` as org1-admin (who owns both).
- **Expect** — **404** `CATALOG.PRODUCT.NOT_FOUND`: the product is looked up by store first. Nothing changed
  in either store.

### PRD-11 — Update with an empty `categories` list keeps the categories · high · [not verified]

- **Steps** — `PUT` a body with `"categories": []`.
- **Expect** — the product's categories are **unchanged**. The console manages membership through the
  add/remove endpoints (PRD-13) and never sends categories on `PUT`; an empty list is "not specified". Send
  one category and it replaces the set.

### PRD-12 — Delete removes the product, its rows and its files · critical · [not verified]

- **Steps** — upload an image to a product created by PRD-01 (IMG-02), then `DELETE /api/v1/private/product/{id}`.
- **Expect** — 200; product, description and image rows gone; the object gone from the MinIO bucket
  (`docker exec cvhome-minio-1 mc ls local/<bucket>/products/<store>/<sku>/`); the product no longer in any
  group it was a member of (`product_group_product` cleaned by the FK cascade — check). Its inventory row is
  **still there** until the console's best-effort `DELETE /inventory/.../by-product/{id}` runs (INV-08) — an
  orphan inventory row is invisible to every reader.

### PRD-13 — Category membership: add, add again, remove · high · [not verified]

- **Steps** — `POST /api/v1/private/product/{id}/category/<KIDS id>`; the same again; `DELETE` it.
- **Expect** — 201; then **409** `CATALOG.CATEGORY.ALREADY_ATTACHED`; then 200 and the category gone from the
  definition. A category id from another store: 404 `CATALOG.CATEGORY.NOT_FOUND`.

### PRD-14 — The plan ceiling on products · high · [not verified]

- **Steps** — with a store whose plan caps products (see `qa/billing-per-store-subscriptions.md` for how to
  set `MAX_PRODUCTS` low), create products up to the cap and one more.
- **Expect** — the one over is refused with billing's `EntitlementExceededException` code; **editing** an
  existing product at the cap still works (only creation counts).

### PRD-15 — A lapsed subscription blocks writes but not reads · critical · [not verified]

- **Steps** — with `SUSPENDED_STORE_ID` from the env file, `GET` the listing and `PATCH` a product.
- **Expect** — the read answers; the write is **402** `BILLING.STORE.SUSPENDED` from `StoreBillingWriteGate`
  before the controller runs. Same for every `/private` write in both services? — **No**: the gate is
  catalog's; inventory has no billing guard (see [99](#99--known-gaps)).

### PRD-16 — The removed endpoints are gone · high · [not verified]

- **Steps** — `POST /api/v1/private/product` (the v1 create), `GET /api/v1/product/{slug}`,
  `GET /api/v2/private/tiny-products`, `GET /api/v2/private/base-products`, `PATCH /api/v2/private/product/{sku}`,
  `GET /api/v1/manufacturers`, `GET /api/v1/manufacturer/{id}`, anything under `/api/v1/private/product/option`
  or `/property/set`, `GET /api/v1/auth/me`.
- **Expect** — **404** on every one. None had a caller in the console, the storefront or checkout; if
  something in your environment calls one of them, that is the finding.

---

---

## CAT — Categories

The tree is materialised: each row carries `lineage` (`/1/7/` — every ancestor id then its own) and `depth`.
"The subtree" is one `like 'prefix%'` query everywhere — the listing's category filter, the brand facet, the
subtree delete.

### CAT-01 — The storefront tree · critical · [verified]

- **Steps** — `GET /catalog/api/v1/category-hierarchy?store=…&lang=en&count=50`.
- **Expect** — `totalElements: 12`, `content` holds the **four roots** only (`MEN` 3 children, `WOMEN` 3,
  `KIDS` 2, `ACCESSORIES` 0), each child under `children` with `description.name` in English and
  `parent: {id, code}`; `lineage` and `depth` present. No `descriptions` array (public).

### CAT-02 — The console tree carries every language · critical · [verified]

- **Steps** — the same under `/private/category-hierarchy` with a session.
- **Expect** — the same tree, and every node with **both** `description` (the request's `lang`) and
  `descriptions` (`en` and `ar`). Before the rewrite this endpoint returned neither — the console fell back to
  category codes.

### CAT-03 — The flat list and the name filter · high · [verified]

- **Steps** — `/private/category?count=50`; `/category-hierarchy?name=sho`.
- **Expect** — a flat page of 12; the filtered tree holds exactly `MEN_SHOES` and `WOMEN_SHOES` — matched in
  **any** language, case-insensitively, and returned as roots of what was read because their parents did not
  match.

### CAT-04 — A tree page is a page of nodes, not of roots · [not verified]

- **Steps** — `/category-hierarchy?count=5`.
- **Expect** — five nodes total, arranged into whatever tree they form; a child whose parent fell outside the
  page appears as a root of that page. `totalElements` stays 12. (The storefront asks for 20; the seeds fit.)

### CAT-05 — One category with its subtree · high · [not verified]

- **Steps** — `GET /private/category/1`.
- **Expect** — `MEN` with every language and `children` holding its three sub-categories (also every language),
  not itself.

### CAT-06 — By slug · high · [verified]

- **Steps** — `GET /category/men?lang=en`.
- **Expect** — `code: MEN, lineage: /1/, depth: 0`. An unknown slug: 404 `CATALOG.CATEGORY.FRIENDLY_URL_NOT_FOUND`.
  The English slug under `lang=ar`: 404 (slugs are per language, as for products).

### CAT-07 — Create a root and a child · critical · [not verified]

- **Steps** — blocks one and two of `category-api.http`.
- **Expect** — 201 with the body echoed **plus its id**; in the DB the root has `lineage '/<id>/'`, `depth 0`,
  `parent_id null`; the child (parent given by **code**) has `lineage '/<root>/<child>/'`, `depth 1`,
  `parent_id <root>`. Both `en` descriptions saved with `sef_url` from `friendlyUrl`.

### CAT-08 — A parent by id, and a parent that does not exist · high · [not verified]

- **Steps** — create with `"parent": {"id": 1}`; then `{"code": "no-such-parent"}`; then `{"id": <org2's>}`.
- **Expect** — 201 under `MEN`; then 400 `CATALOG.CATEGORY.REFERENCE_UNRESOLVABLE` twice — the foreign id is
  unresolvable, never "belongs to another store".

### CAT-09 — Duplicate code · high · [not verified]

- **Steps** — create `http-demo` twice in the same store; then once in org1-store2.
- **Expect** — the console checks `/private/category/unique` first and disables Save; the raw second `POST`
  hits the `(store, code)` unique constraint — **expected to be a 500 today** ([99](#99--known-gaps)). The
  other store succeeds.

### CAT-10 — Update merges copy by language and can re-parent · critical · [not verified]

- **Steps** — `PUT /private/category/{child}` with a renamed `en` description, `featured: true`, and
  `"parent": {"code": "WOMEN"}`.
- **Expect** — 200, the body echoed; the description keeps its id; `featured` flipped; the category is now
  under `WOMEN` with `lineage` and `depth` recomputed — **and so are its own descendants'** (create a
  grandchild first and check its lineage changed with it).

### CAT-11 — Move, and move to the root · critical · [not verified]

- **Steps** — `PUT /private/category/{id}/move/1`; then `/move/-1`.
- **Expect** — lineage `/1/<id>/` depth 1 and every descendant re-based; then `/<id>/` depth 0. Moving under
  a category of another store: 404.

### CAT-12 — Moving a category under its own descendant · high · [not verified]

- **Steps** — with `A → B → C`, `PUT /private/category/<A>/move/<C>`.
- **Expect** — **record what happens.** Nothing refuses it today; the expected outcome is a cycle in
  `lineage` that the subtree queries then never terminate on. If it is accepted, that is a finding
  ([99](#99--known-gaps) lists it as known).

### CAT-13 — Visibility patch · high · [not verified]

- **Steps** — `PATCH /private/category/{id}/visible` with `{"code": "x", "visible": false}`.
- **Expect** — only `visible` changes; `code` in the body is ignored. The storefront tree still lists it
  (there is no visibility filter on the read — see [99](#99--known-gaps)).

### CAT-14 — The categories of a product · [not verified]

- **Steps** — `GET /private/category/product/1`.
- **Expect** — the two seeded categories of product 1, every language, `totalElements: 2, totalPages: 1`.

### CAT-15 — Delete removes the subtree and the products left with no category · critical · [not verified]

- **Steps** — create root `R` with child `C`; create product `P1` in `C` only and `P2` in `C` **and** `MEN_TOPS`;
  `DELETE /private/category/<R>`.
- **Expect** — 200; `R` and `C` gone (child first — no FK failure); `P1` **deleted** (it had no other
  category); `P2` still exists with `MEN_TOPS` only. Deleting `R` again: 404.

### CAT-16 — Delete is store-scoped · critical · [not verified]

- **Steps** — `DELETE /private/category/1?store=<org1-store2>` as org1-admin.
- **Expect** — 404; `MEN` of org1-store1 is intact.

---

---

## BRD — Brands (manufacturers)

### BRD-01 — The console list, with every language, and its name filter · critical · [not verified]

- **Steps** — `GET /private/manufacturers?count=20`; `?name=ni`.
- **Expect** — six brands, each with `descriptions` (`en`, `ar`) and `description` for the request language;
  the filter returns `NIKE` (matched on the name in any language, case-insensitive).

### BRD-02 — Create, read, update, delete · critical · [not verified]

- **Steps** — the `manufacturer-api.http` sequence.
- **Expect** — 201 echoing the body with its id; the read shows both languages; `PUT` answers 200 with no
  body and the description keeps its id; `DELETE` 200, then 404 `CATALOG.MANUFACTURER.NOT_FOUND`.

### BRD-03 — Deleting a brand that products use · high · [not verified]

- **Steps** — `DELETE /private/manufacturer/<NIKE id>`.
- **Expect** — **record what happens.** `product.manufacturer_id` has an FK with no cascade, so the expected
  result today is a **500** from the constraint. The console does not guard it either.

### BRD-04 — The storefront brand facet follows the category subtree · high · [verified]

- **Steps** — `GET /category/1/manufacturer?lang=en` (`MEN`).
- **Expect** — `ADIDAS, GUCCI, H&M, NIKE, ZARA` — the brands of the **visible** products anywhere under
  `MEN`, one language, ordered by code. `CHANEL` is absent because no men's product is Chanel. An unknown
  category: 404.

### BRD-05 — The facet excludes hidden products · [not verified]

- **Steps** — hide every Nike product under `MEN` (PRD-08), re-read the facet.
- **Expect** — `NIKE` gone from the facet while the brand itself still exists.

### BRD-06 — Code uniqueness and the check · [not verified]

- **Steps** — `/private/manufacturer/unique?code=NIKE` then `?code=nike`.
- **Expect** — `true` then `false`: codes are case-sensitive, the console lower-cases nothing.

---

---

## TYP — Product types

### TYP-01 — List, get, create, update, delete · critical · [not verified]

- **Steps** — the `product-type-api.http` sequence.
- **Expect** — 201 `{"id": n}` (an `Entity`, not the body); the list and the read carry every language;
  `PUT` cannot change the **code** (send another code and read it back unchanged — the code is the type's
  identity once products point at it); `DELETE` then 404.

### TYP-02 — Duplicate code is a typed conflict · high · [not verified]

- **Steps** — create `http-type` twice.
- **Expect** — **409** `CATALOG.PRODUCT_TYPE.DUPLICATE` — this one *is* checked before the insert, unlike
  categories and brands.

### TYP-03 — Deleting a type that products use · [not verified]

- As BRD-03: expected to be a constraint 500 today.

---

---

## GRP — Product groups and related items

Store-level groups (`FEATURED_ITEMS`, …) have no parent product. A product's related items are the group coded
`RELATED_ITEM` whose `parentProduct` is that product — one shape, two meanings, the endpoint tells them apart.

### GRP-01 — The home-page strips · critical · [verified]

- **Steps** — `GET /products/groups/HOME_PAGE?lang=en` and the other three codes.
- **Expect** — `code`, `active: true`, 24 / 12 / 15 / 10 `products`, each the **minimal** product shape
  (`sku`, `description`, `image`, `images`, box — no brand, type or categories) so the storefront can fetch
  price and stock by sku (SF-01). One language.

### GRP-02 — The console list is a summary · high · [not verified]

- **Steps** — `GET /private/products/groups`.
- **Expect** — four rows with `code`, `active`, every language in `descriptions`, and **empty** `products` —
  the list does not carry members; `/private/products/groups/{code}` does, with every language.

### GRP-03 — Save is an upsert on code · critical · [not verified]

- **Steps** — `POST /private/products/groups` with `HTTP_STRIP`, two `productIds` and one description; `POST`
  it again with three ids and no descriptions.
- **Expect** — 201 both times, the second **updating** the same row (same id in the echo): members replaced by
  the three, descriptions **cleared** (a body that omits them clears them — the console re-posts whole).

### GRP-04 — Unknown member or parent · high · [not verified]

- **Steps** — `"productIds": [999999]`; `"parentProductId": 999999`; a product id of org2-store1.
- **Expect** — 404 `CATALOG.PRODUCT.NOT_FOUND` on all three; nothing written.

### GRP-05 — Add and remove a member; idempotent add · high · [not verified]

- **Steps** — `POST …/groups/HTTP_STRIP/product/3` twice; `DELETE` it; `DELETE` again.
- **Expect** — 201 both times with the member present **once**; 204; 204 again (removing a non-member is not
  an error). A non-existent group: 404 `CATALOG.PRODUCT_GROUP.NOT_FOUND`.

### GRP-06 — Delete a group; the products survive · high · [not verified]

- **Steps** — `DELETE /private/products/groups/HTTP_STRIP`; re-read one of its former members.
- **Expect** — 204; then 404 for the group; the product is untouched.

### GRP-07 — Related items from nothing · critical · [not verified]

- **Steps** — `GET /products/1/relationship` (verified: **404** on the seeds — no `RELATED_ITEM` group exists);
  `POST /private/products/1/relationship/2`; `GET` again; `POST …/relationship/3`; `DELETE …/relationship/2`.
- **Expect** — the first `POST` creates product 1's `RELATED_ITEM` group and answers 201; the `GET` then shows
  `parentProduct` = product 1 and `products` = [2]; then [2, 3]; then [3]. The storefront's "you may also
  like" strip degrades to nothing on the 404 — that is by design (SF-03).

### GRP-08 — Related items are per product, and one direction · [not verified]

- **Steps** — after GRP-07, `GET /products/2/relationship`.
- **Expect** — 404: relating 2 to 1 did not relate 1 to 2.

### GRP-09 — A second product's related group · high · [not verified]

- **Steps** — `POST /private/products/2/relationship/3` after GRP-07.
- **Expect** — **record what happens.** `product_group` has a unique constraint on `(store, code)` and every
  product's related group is coded `RELATED_ITEM`, so the second product's group is expected to fail on the
  constraint — a **500**. This is inherited and listed in [99](#99--known-gaps); it needs a DDL change.

---

---

## IMG — Product images

Files live on the store's bucket (MinIO locally, S3 on Fargate) under `products/<store>/<sku>/`; the row
points at the file name and the read builds the url from the CDN properties.

### IMG-01 — The public list, in display order · high · [verified]

- **Steps** — `GET /product/1/images`.
- **Expect** — five images, `order 0..4`, exactly one `defaultImage: true` (order 0), each `imageUrl` a
  MinIO path for the product's sku. Unknown product: 404 `CATALOG.PRODUCT.NOT_FOUND`.

### IMG-02 — Upload one file · critical · [not verified]

- **Steps** — the upload block in `product-image-api.http` (multipart, field `file`, `order=0`) against a
  product with no images (PRD-01's).
- **Expect** — 201; the list shows it with `defaultImage: true` (the first image of a product becomes the
  default); the object exists in the bucket; the storefront listing row for that product now has an `image`.

### IMG-03 — Upload several; explicit default · high · [not verified]

- **Steps** — upload two files in one request with `order=5`; then one more with `defaultImage=true`.
- **Expect** — orders `5, 6`, neither default (one already exists); the third becomes default **and the old
  default keeps its flag** — see [99](#99--known-gaps): two defaults, the read picks the first it finds.

### IMG-04 — Reorder · high · [not verified]

- **Steps** — `PATCH /private/product/{id}/image/{imageId}?order=2`.
- **Expect** — the list re-sorts; an image id from another product of the same store: 404
  `CATALOG.PRODUCT_IMAGE.NOT_FOUND` (the lookup is by store **and** product **and** id).

### IMG-05 — Delete removes row and file · high · [not verified]

- **Steps** — `DELETE /private/product/{id}/image/{imageId}`; check the bucket.
- **Expect** — 200, row gone, object gone. Deleting the default: the next read picks the lowest-order image as
  `image` on the product (`defaultImage()` falls back), but no row is re-flagged.

### IMG-06 — A file the bucket refuses · high · [not verified]

- **Steps** — stop MinIO (`docker stop cvhome-minio-1`), upload.
- **Expect** — a typed **`CATALOG.PRODUCT_IMAGE.NOT_PERSISTED`** naming the sku, and **no** row written.
  Restart MinIO afterwards.

### IMG-07 — The MinIO trap · [verified]

The seeded image rows reference objects that a fresh MinIO does not have (MinIO runs without a volume). The
rows and urls are correct; the storefront shows a placeholder. Upload something before judging a rendering
case. Regenerate: `qa/lcl-lifecycle.md`.

---

---

## GAL — The gallery reads the media library

_From `qa/content-owns-appearance-and-media.md` §CAT, renumbered `CAT-0N` → `GAL-0N` because catalog's own
`CAT` prefix is the category tree._

Product images are no longer files catalog uploads and owns. Catalog stores a `media_asset_id` per gallery row
and content owns the asset, the bytes and the usage index. Catalog has no upload endpoint left.

### GAL-01 — The seeded gallery reads · critical · [verified]

- **Steps** — `GET /catalog/api/v1/product/1/images?store=…`.
- **Expect** — images in sort order, each with a `mediaAssetId` and a cached `imageUrl`. No `imageName`.

### GAL-02 — Attaching from the library · critical · [verified]

- **Steps** — open a saved product's **Media** step; **Choose from media library**; pick two images.
- **Expect** — both appear; the first becomes the default on an empty gallery.
- **Found** — the step did not render at all: `productForm.media.pick` was in neither locale, and Transloco is
  configured strict, so the throw took the whole panel down. `npm run lint` catches this and had not been run.
  Then the tiles were captioned with the gallery row's database id ("901") because a picked asset was attached
  with `altText: null` unless it had a title. Both fixed; a picked asset now carries its title or filename.

### GAL-03 — Changing the default image · critical · [verified]

- **Steps** — move another image to the front of the gallery.
- **Expect** — it becomes the storefront thumbnail and the previous one is no longer default. This was
  impossible before: `PATCH ?order=` only renumbered.
- **Found** — the endpoint gap was closed by `PUT …/product/{id}/images` but the console had not followed. The
  facade kept `isDefault` on whichever image already held it, so a reorder carried the badge off position 1
  while the panel above went on saying the first image is the thumbnail. Fixed: reordering re-designates, and
  moving an image to the front *is* how the thumbnail changes. The step's hint said the opposite and now says
  that. **There is deliberately no separate "make default" control** — that would be a second way to express
  one write.

### GAL-04 — Reordering · [verified]

- **Steps** — move an image up.
- **Expect** — the whole gallery is renumbered in one `PUT`, with no two images sharing a position.
- **Result** — holds, and survives a reload.

### GAL-05 — An asset from another store is refused · critical · [verified]

- **Steps** — `POST /private/product/1/images` with a `mediaAssetId` belonging to another store.
- **Expect** — **400 `CATALOG.PRODUCT_IMAGE.ASSET_UNKNOWN`**, and nothing written. Reported as 400 rather than
  404 so probing tells the caller nothing about whether the asset exists.

### GAL-06 — Detaching leaves the asset alone · [verified]

- **Steps** — remove an image from a product; open the media library.
- **Expect** — the asset is still there. Another product may be showing the same photo.

### GAL-07 — Deleting a product releases its assets · [unit only]

- **Steps** — delete a product that held images; check `content.media_usage`.
- **Expect** — no rows for that product. The assets themselves remain.

### GAL-08 — Catalog no longer uploads · [verified]

- **Steps** — `POST /spg/catalog/api/v1/private/product/1/image` (singular, multipart).
- **Expect** — **404**. There is no upload endpoint in catalog any more.

---

---

## ENF — The plan ceiling and the write gate

_From `qa/billing-per-store-subscriptions.md` §ENF. The edge half of enforcement (ENF-01, ENF-04) is the gateway's, in [gateway-qa.md](../../../../store-core/gateway/gateway-service/qa/gateway-qa.md); ENF-03 (a lapsed store's storefront keeps selling) is landing-ui's._

A store that has not paid cannot be *changed*. It can still be read, and its shopfront still sells — both
deliberate.

### ENF-02 — Reading a lapsed store still works · critical · [verified]

A seller has to see what they are being asked to pay for. This was wrong once and is worth re-checking.

- **Steps** — on the same lapsed store, browse products, orders and settings.
- **Expect** — everything lists normally. Only changes are refused.

### ENF-05 — The product ceiling refuses the one that would exceed it · high · [not verified]

> **Expect this to be permissive today.** The catalog guard shipped only partly wired — see
> [Known gaps](#99--known-gaps). If the limit is not enforced, that is the known state, not a new bug. Record
> what you observe either way.

- **Setup** — a store on Free, which allows 25 products.
- **Steps** — create products up to 25, then attempt one more.
- **Expect** — *if wired:* the 26th is refused with a message naming the limit and the current count, and
  existing products stay editable. *If not:* it succeeds — log it against the known gap.

### ENF-06 — Unlimited means unlimited · [not verified]

Pro does not cap products. An absent limit must never behave as a limit of zero.

- **Expect** — on Pro, product creation is never refused for a ceiling, however many exist.

---

---

## SEC — Permissions and tenant isolation

_These five cases sweep catalog **and** inventory together; they are kept whole here rather than split in half. Inventory's own gate cases are INV-04 and RES-08 in [inventory-qa.md](../../../inventory/inventory-service/qa/inventory-qa.md)._

### SEC-01 — Every private catalog and inventory endpoint refuses without a session · critical · [verified] (sample)

- **Steps** — one `GET`, one `POST` under `/private` per controller, no cookie.
- **Expect** — 401 on all. The security chain matches `/api/*/private/**`; every private mapping here sits
  exactly one segment below `/api/v1` or `/api/v2`, so the chain covers them (unlike merchant's router — see
  that file).

### SEC-02 — `STORE-POD.CATALOG.*` and `STORE-POD.INVENTORY.*` need manage access · critical · [not verified]

- **Steps** — as the moderator, one write per controller.
- **Expect** — 403 `COMMON.ACCESS_DENIED` with a traceId; nothing changed.

### SEC-03 — Another org cannot read private data or write · critical · [not verified]

- **Steps** — as `org2`'s admin, `GET /private/product/1?store=<org1-store1>` and a `PUT`.
- **Expect** — 403 both. The public reads with `store=<org1-store1>` answer — they are public for shoppers.

### SEC-04 — Nothing crosses stores through an id in the path or body · critical · [not verified]

Every lookup in both services is `(store, id)` or `(store, sku)`. The cases that prove it: PRD-04, PRD-10,
CAT-08, CAT-16, GRP-04, IMG-04, INV-04, LST-08. Run them as a set.

### SEC-05 — Nothing sensitive in the logs · [not verified]

- **Steps** — grep both logs after a session of writes.
- **Expect** — sku, ids, refs and traceIds only; no session cookie, no bearer token, no body dumps.

---

---

## ARC — What the rewrite left behind

### ARC-01 — The live schema creates no attribute tables · high · [superseded by the variant rework]

- **Steps** — on a fresh database, `\dt catalog.*`.
- **Was** — 14 tables, and no `product_option*` among them.
- **Now** — the rework adds seven: `product_option`, `product_option_description`, `product_option_value`,
  `product_option_value_description`, `product_option_assignment`, `product_variant`,
  `product_variant_option_value` — and **drops `catalog.product.sku`**, because the sku moved to the variant.
  Still absent, and still correct to assert: `product_attribute`, `product_opt_set*`, `product_digital`,
  `product_image_description`. The `product_option*` tables here are the NEW vocabulary, unrelated in shape to
  the Shopizer ones this entry was written about.

### ARC-03 — Nothing in the repo calls a removed type or endpoint · [verified]

- **Steps** — `./gradlew build -x test -x check`; `npm run build` in console-ui and landing-ui.
- **Expect** — clean. checkout compiles against the new `ReadableMinimalProduct` and `SkuInventory`.

### ARC-04 — `catalog-deprecated` is deleted · [superseded by the variant rework]

- **Was** — the directory existed, unregistered from `settings.gradle`, parking the Shopizer-era variant code
  against the day variants returned.
- **Now** — variants returned as a different model, so the parked code had no reader and is removed. Nothing
  in `settings.gradle` referenced it, so the build is unchanged.
- **Watch instead** — `extra/scripts/drop-catalog-inventory-tables.sql` no longer drops
  `catalog.product_variant`: the rework reused that name for a live table, and dropping it would take every
  product's sku with it. The script's remaining drops are the genuinely dead Shopizer tables.

---

> ARC-02 (the inventory schema holds no price descriptions) is in inventory-qa.md.

---

## SID — One store id, end to end

_From `qa/unify-store-id-value-objects.md` §EDGE, §T and §REG, reformatted from that file's bold run-in cases
into the case shape used everywhere else. The text of each case is unchanged._

A store had two identifier types: `ManagerStoreId` in store-core, `StoreMerchantId` in the pods. They are now
one type and it serializes as a **bare string**. Catalog is where the merged id is easiest to drive, so the
edge cases live here even though the resolver is `store-commons`.

### SID-01 — A malformed store parameter is a 400, not a 500 or a 403 · critical · [verified]

_Was E1._ Previously a bad `?store=` travelled inwards and blew up as an `IllegalArgumentException` deep inside
the permission evaluator (a 500). It is now refused at the argument resolver.

- **Steps** —

  ```bash
  curl -i 'http://spg-507f1f77.gateway.com/catalog/api/v1/category/anything?store=abc&lang=en'
  ```

- **Expect** — **400** with `"code":"COMMON.CONVERSION_FAILED"`, `"detail":"abc is not a valid store id."`,
  `"params":{"store":"abc"}` and a `traceId`. No stack trace, no root-cause text in `detail`.

### SID-02 — A missing store parameter is a 400 · [verified]

_Was E2._

- **Steps** —

  ```bash
  curl -i 'http://spg-507f1f77.gateway.com/catalog/api/v1/category/anything?lang=en'
  ```

- **Expect** — **400**, `"code":"COMMON.MISSING_PARAMETER"`, `"params":{"parameter":"store"}`.

### SID-03 — A valid store id still resolves past the resolver · [verified]

_Was E3._ Same URL with `?store=65f023632bc46470c104b76f&lang=en` returns **404** (no such category) — a 400
here would mean the validation is rejecting good ids.

### SID-04 — The same public endpoint, two stores, two different catalogs · critical · [verified]

_Was T1._

- **Steps** —

  ```bash
  B=http://spg-507f1f77.gateway.com/catalog/api/v1
  curl -s "$B/category-hierarchy?count=20&page=0&store=65f023632bc46470c104b76f"   # Men, Women, Kids, Accessories
  curl -s "$B/category-hierarchy?count=20&page=0&store=65f023632bc46470c104b75f"   # Computers, Mobile Phones, Audio, …
  ```

- **Expect** — no overlap between the two. Any would mean store scoping was lost.

### SID-05 — A principal without the permission token gets 403 · high · [not verified]

_Was T3._ Call a `/private/**` catalog endpoint with a session for a store you do not own. **403**, not 200 and
not 500.

### SID-06 — A write into a brand-new store uses its id correctly · critical · [verified]

_Was R5, and the strongest single case in the file it came from: a fresh id minted by store-core, written
through a pod's JPA `@Embedded` column, read back, and tenant-scoped._

- **Setup** — provision a store through the console (see
  [tenancy-qa.md](../../../../store-core/tenancy/tenancy-service/qa/tenancy-qa.md) SID-03) and select it.
- **Steps** — `POST /spg/catalog/api/v1/private/category`, then read `category-hierarchy` for the new store and
  for org1-store1.
- **Expect** — **201**, and the row persisted as
  `catalog.category.store_merchant_id = <the new id>`. Reading back `category-hierarchy?store=<new id>` returns
  exactly `[QA-CAT-STOREID]` while `category-hierarchy?store=65f023632bc46470c104b76f` still returns
  `[MEN, WOMEN, KIDS, ACCESSORIES]` — no leakage in either direction. The console agrees: switch to the new
  store and Category → List of category shows one row.
- **Expect a 402 first.** An org gets **one trial grant**, so the *second* and later stores in an org are
  created with a `PENDING` subscription, which billing reports as blocked — the write is refused
  `402 BILLING.STORE.SUSPENDED`. That is correct behaviour, not a store-id bug. Give the store an active
  subscription (or use the org's first store) before expecting a write to land; the gateway takes up to a
  minute to notice.

---

## REG — Regression watchlist

Every row was a real defect found while building or verifying the catalog rewrite (PR #282).

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Every catalog read 500'd on first boot of the rewrite** | Seed rows have `null` in `visible`, `sort_order`, `prd_type_add_to_cart`; the entities had primitive fields. Hibernate refused to set them. | LST-01, CAT-01, TYP-01 — any read of seeded data |
| **Category tree and brand list 500'd with no filter** | `?2 is null or lower(d.name) like …` — Postgres typed the null parameter as `bytea`. Split into two queries. | CAT-01, CAT-03, BRD-01 |
| **Every request 500'd with "Cannot change HTTP Accept-Language header"** | The locale resolver bean looked unused and was removed; the shared request context writes the locale. | Any request at all — SEC-01's sample |
| **The console's category tree showed codes instead of names** | The private hierarchy passed `nonLanguage`, so neither `description` nor `descriptions` was set. | CAT-02 |
| **The old catalog listing hid products with no availability row** | Inner joins to `product_availability` in the fetch queries; the count was right only because every seed had one. | LST-01 (`totalElements` 45 with a product created by PRD-01 → 46) |
| **The store id column went blank in the console** | A template reached into the id (`{{ value.id }}`) after it became a bare string. A grep could not have caught it. | SID-06, and console-ui's SW cases |

---

## VAR — the uniform variant model

Added by the variant rework (PR #306). `catalog.product` is a pure definition — its `sku` column is gone.
**Every product owns at least one `catalog.product_variant`**, and sku, price and stock live at the variant
level. Option vocabulary (`product_option` / `product_option_value`) is store-wide, so value ids are shared and
id-based faceting works. Axes and combinations are written **atomically together**
(`PUT /api/v2/private/product/{id}/variants`), so they can never desync.

The console side of this is
[console-ui](../../../../store-core/console-ui/qa/console-ui-qa.md); the shopper side is
[landing-ui](../../../landing-ui/qa/landing-ui-qa.md); the order snapshot is
[checkout](../../../checkout/checkout-service/qa/checkout-qa.md).

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

### SCH-04 — The old drop script no longer names a live table · high · [verified]

- **Why it matters** — `extra/scripts/drop-catalog-inventory-tables.sql` is documented to be run
  post-verification, and it used to `drop table catalog.product_variant`. The rework **reused that name** for
  the live table holding every product's sku, so running the script as written would have destroyed the whole
  catalogue's variants.
- **Result** — that drop is removed and the reason is written into the script beside the remaining ones, which
  are the genuinely dead Shopizer tables (`product_var_image*`, `product_variant_group`, `product_variation`).

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

**Backend — a reachable 500 and an unguarded FK**

- *Promoting an earlier row to default violated `uk_product_variant_default`.* It is a partial unique **index**
  and Postgres never defers those, so the two `UPDATE`s in one dirty-checking pass could hit "set true" while
  the old default was still true. Cleared and flushed before the new one is set. Promoting a *later* row
  happened to succeed, which is exactly what CON-02 did — the direction that failed was never run.
- *Editing an option could delete a value a variant still sells by*, answering a raw FK 500 where deleting the
  option answers a named 409. `update` now diffs the dropped values and raises `PRODUCT_OPTION.IN_USE`.
- The per-order quantity refusal moved to its own `CartQuantityOutOfRangeException`: sharing
  `ProductNotPurchasableException` meant a caller wanting to retry smaller could not branch on the type.
- `findByProductIdHydrated` is store-scoped, and an explicit `"optionValueIds": null` is a 400 rather than a 500.

---

## 99 — Known gaps

- **`GET /api/v1/detailed-products?skus=` takes an uncapped sku list.** Its siblings cap (`AvailabilityQuery`
  at 500, the inventory batch at 200) and it is on the anonymous path, so it should too — but the obvious
  `@Validated` + `@Size` does not work here: `ExternalProductApi` implements `ExternalProductService`, so the
  annotation makes Spring proxy the controller through the interface and every request to it 500s (caught by
  `ProductApiIntegrationTest`, reverted). `InventoryApi.getByProducts` implements nothing and does carry the
  cap. A fix for the catalog side needs an explicit in-method guard rather than bean validation.

Behaviour that is expected today. Please don't spend time raising these — but do shout if you see something
*beyond* what is described.

**The console reads its product table through a public endpoint.** `/api/v2/products` carries no
`@PreAuthorize`; the private lists that used to exist returned rows with no name, and were removed. The data
is the storefront's own public catalogue, scoped by `store`. Documented in console-ui `lessons.md`.

**Duplicate codes are a 500, not a 409, for products, categories and brands.** Only product types check
before inserting. The console runs the `/unique` check and disables Save, so the raw API answer is only
reachable by hand. Worth a typed conflict; not done in this PR.

**Deleting a brand or a type that products reference is a constraint 500.** No cascade, no guard, no console
warning.

**Moving a category under its own descendant is accepted.** Nothing detects the cycle. The console's tree
control does not offer it, so it is API-only.

**Category visibility is recorded and not enforced on reads.** The storefront tree lists hidden categories;
the listing filters products by *product* availability only. The storefront ignores `visible` today.

**A second product's related-items group cannot be created.** `product_group` is unique on `(store, code)`
and every product's related group is coded `RELATED_ITEM`. The first product to get related items claims the
code. Inherited; needs the constraint widened to `(store, code, parent_product_id)`.

**A product can have two default images.** Uploading with `defaultImage=true` flags the new one without
clearing the old; reads pick the first flagged. Deleting the default re-flags nothing.

**A product's default image cannot be changed after upload** from the console (`lessons.md`) — the API can,
through the flag above, with the caveat above.

**A description with no language is a 500.** `language_code` is NOT NULL and nothing validates it above the
database. The console always sends it.

**A merchant's related-items and groups are single-store.** There is no cross-store or org-level group.

**Sorting the listing by anything but a direct `Product` column is a 500.** `SORT_MAP` in landing-ui exposes
only `dateAvailable`.

**The legacy `split-merchant-content-services.md` ROUTE cases are gone, not lost.** They asserted the old
`/spg/merchant/api/v1/content/**` alias still answered; content's MIG-03 asserts it is removed, and the later
assertion wins.

---

Raise anything unexpected against the catalog PR. Include the store id, the sku or category id, the time, and
the matching `Unhandled failure [traceId=…]` block from `.lcl/<stack>/logs/catalog.log`. For a console defect
attach the browser console and the failing request: a 401 is a missing session, a 403 is a permission problem,
a 404 through `/spg/**` is usually a missing `pod` parameter, and a 400 with a `*.REFERENCE_UNRESOLVABLE` code
names the exact value that did not resolve in this store.

---

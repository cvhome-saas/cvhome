# QA — catalog and inventory (products, categories, brands, groups, images · stock, price, reservations)

`store-pod/catalog` is what a store *sells*: products with their copy per language, images, brand, type and
categories, the category tree, and the merchandising groups the home page is built from. `store-pod/inventory`
is what a store *has and charges*: stock, price and the reservations checkout takes while an order is paid for.
They were one service until PR #282 split them; the seam between them is the **sku**, and nothing else — the
inventory schema has no foreign key into catalog, and a catalog response never carries a price or a quantity.

Both services were then rewritten from their consumers inward (same PR): one shape per domain, entities that
map only the columns the single-product model uses, no facade/populator layers. Every endpoint the console, the
storefront and checkout call kept its path and JSON shape; endpoints nothing called are gone; variants and
product options/attributes are parked in `store-pod/catalog-deprecated`. That is what makes this file
necessary: the wire contract is unchanged and the code behind every line of it is new.

- **Scope** — catalog · inventory · checkout's composition of the two · console-ui Products, Product form and
  Catalogue pages · landing-ui listing, product page, category page and home strips
- **Change** — PR #282, branch `refactor/split-inventory-from-catalog` (three commits: the split, the inventory
  rewrite, the catalog rewrite). Plan: `.claude/plans/i-want-to-split-reflective-muffin.md`.
- **Cases** — 119 (37 verified, 8 covered by tests only, 74 never run end to end)
- **Related** — [`qa/merchant-store-service.md`](merchant-store-service.md) for the store record both services
  read (units of measure, default language); [`qa/run-lcl-lifecycle.md`](run-lcl-lifecycle.md) for the stack.

Each case is tagged:

- **[verified]** — driven end to end against a running stack and passed, during the PR's own QA.
- **[unit only]** — covered by a named automated test; nobody drove it through the stack.
- **[not verified]** — never run end to end. Every merchant *write* through the console is in this state:
  the PR's QA had no console session, so the console write paths were exercised only by their Karma specs
  and the `.http` files.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading. Several
things that look like defects are decisions — a product with no inventory row is *not stocked*, not an error;
a private catalog read returns two description fields; the console reads its product table through a public
endpoint. Read section 99 before filing anything.

---

## 00 — Before you start

```bash
sudo ./extra/scripts/configure-domain.sh        # once per machine
./extra/scripts/run-lcl.sh start -d             # never `restart <one-service>`: it tears the whole stack down
```

**Sign-in.** Console `http://gateway.com:8000` — `org1-admin` / `admin` (org owner), `org1-store1-admin` /
`admin`, `org1-store1-moderator` / `admin` (read-only). Storefront `http://org1-store1.spg-507f1f77.gateway.com`
(`user` / `revo` for a signed-in shopper; most cases here are anonymous).

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

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select product_id, sku, available, product_ship, sort_order, manufacturer_id, product_type_id
     from catalog.product where store_merchant_id='65f023632bc46470c104b76f' order by product_id;"
... "select category_id, code, parent_id, depth, lineage, sort_order, visible from catalog.category
       where store_merchant_id='65f023632bc46470c104b76f' order by lineage;"
... "select g.code, count(pp.product_id) from catalog.product_group g
       left join catalog.product_group_product pp on pp.product_group_id=g.product_group_id group by 1;"
... "select product_avail_id, sku, quantity, available, quantity_ord_min, quantity_ord_max
       from inventory.product_availability where store_merchant_id='65f023632bc46470c104b76f' order by 1;"
... "select p.product_price_id, a.sku, p.product_price_amount, p.product_price_special_amount,
            p.product_price_special_st_date, p.product_price_special_end_date, p.default_price
       from inventory.product_price p join inventory.product_availability a using (product_avail_id);"
... "select ref, status, expire_at from inventory.product_reservation order by id desc limit 10;"
... "select * from inventory.sm_sequencer;"
```

Logs: `build/lcl-logs/catalog.log`, `build/lcl-logs/inventory.log`, `build/lcl-logs/checkout.log`. An
`Unhandled failure [traceId=…]` line in any of them is a defect regardless of what the screen showed.

### State the PR's QA left behind

The PR's smoke tests reserved stock on org1-store1: `SKU-NK-RUN-001` is at **23** units (a reservation under
ref `smoke-1` was committed; `smoke-2` was refused), price still 750.00 with no special. The upsert with a
special was refused (403 — a service token cannot manage stock, INV-04), so no price was changed. Reset before
the INV cases if you want the seeded number:

```sql
update inventory.product_availability set quantity=25 where sku='SKU-NK-RUN-001';
```

or `docker compose down -v` + a fresh `run-lcl.sh start -d`, which reseeds everything.

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

### PDP-05 — The storefront page renders it, with the price from inventory · critical · [verified]

- **Steps** — open `http://org1-store1.spg-507f1f77.gateway.com/en/product/nike-zoomx-invincible-run-3`.
- **Expect** — 200, the product name, `SAR 750.00` (or whatever INV state you left — the price is *not* in
  the catalog payload, landing-ui fetches it from inventory by sku and formats it), quantity and an enabled
  add-to-cart. With inventory stopped the page still renders, without a price and with add-to-cart disabled.

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
case. Regenerate: `qa/run-lcl-lifecycle.md`.

---

## INV — Stock and price (inventory)

`GET /inventory/api/v1/availability?skus=a,b,c` is the **only read** — public, store-scoped, bulk. A sku with
no record is **absent** from the answer, not present with zeros. `PUT /private/inventory/{sku}` is the
**only write**: quantity, available, order min/max and one price with an optional special amount and window.

### INV-01 — Bulk read · critical · [verified]

- **Steps** — `?skus=SKU-NK-RUN-001,SKU-NOPE&store=<org1-store1>`.
- **Expect** — one element: `sku, productId, available, canBePurchased, quantity, quantityOrderMinimum,
  quantityOrderMaximum, price{originalPrice, finalPrice, discounted, discountPercent, specialAmount,
  specialStartDate, specialEndDate}`. Raw numbers — no currency strings; every caller formats. `SKU-NOPE`
  absent.

### INV-02 — `canBePurchased` is available **and** in stock · critical · [unit only]

`SkuInventoryMapperTest.notPurchasableWithoutStockOrWhenUnavailable`.

- **Steps** — set `available: false` on a sku (INV-05), read; set `quantity: 0`, read.
- **Expect** — `canBePurchased: false` in both; `available` reports the flag alone.

### INV-03 — Discount windows · critical · [unit only]

`SkuInventoryMapperTest` (7 cases: inside window, start-only, end-only, open-ended, start today, end today,
default price over other rows).

- **Steps** — through INV-05 set `specialAmount 608` with no dates; then `specialEndDate` yesterday; then
  `specialStartDate` tomorrow; then start today.
- **Expect** — `discounted: true, finalPrice 608, discountPercent 20` (on 760); then not discounted; then not;
  then discounted. Start is inclusive, end exclusive; a window with only a start is honoured (the legacy rule
  ignored it — a deliberate change).

### INV-04 — Cross-tenant and no-token · critical · [verified]

- **Steps** — the bulk read with `store=<org2-store1>` and org1's skus; `PUT /private/inventory/…` with no
  token.
- **Expect** — `[]`; **401**. Then `PUT` with the pod's **s2s token** (client-credentials against uaa,
  scope `store_pod`): **403** `COMMON.ACCESS_DENIED` (verified) — a service principal can reserve, not manage.

### INV-05 — The upsert creates, then updates, one row · critical · [not verified] (400/401 paths verified)

- **Steps** — `PUT /private/inventory/HTTP-DEMO-001` with `{"productId": <PRD-01 id>, "quantity": 12,
  "available": true, "price": {"amount": 25, "specialAmount": 19.9, "specialEndDate": "2099-12-31"}}`; read the
  DB; `PUT` again with `quantity 3` and `price.amount 30`, no special.
- **Expect** — 200 with the resolved `SkuInventory` (`finalPrice 19.9, discounted true, discountPercent 20`);
  one `product_availability` row with `sku`, `product_id`, `store_merchant_id`, and one `product_price` row
  (`base`, default, `store_merchant_id` set). The second call **updates both rows in place** (same ids) and
  clears the special. `quantityOrderMinimum/Maximum` omitted: kept (1 / 0 on create).

### INV-06 — Validation · high · [verified]

- **Steps** — `{"quantity": -1, "available": true}` (no price); `{"quantity": 1, "available": true, "price":
  {"amount": -5}}`.
- **Expect** — 400 with `fields` on `quantity` / `price`, then on `price.amount`.

### INV-07 — A legacy sku with several price rows · [unit only]

`SkuInventoryMapperTest.defaultPriceWinsOverOtherLegacyPriceRows`. Insert a second, non-default price row for
a sku by SQL; the read answers the default one; an upsert **edits the default one** and leaves the other.

### INV-08 — Delete by product is best-effort orphan cleanup · high · [not verified]

- **Steps** — `DELETE /private/inventory/by-product/<PRD-01 id>`; again.
- **Expect** — 200 both times (a product with no rows is a no-op). A row referenced by a reservation line
  (RES-01 first): **record what happens** — the FK from `product_reservation_line` is expected to make it a
  **500**; the console swallows it and the row stays, which is harmless but should be known.

### INV-09 — Boot migration from the old catalog tables · critical · [verified]

`CatalogDataMigration` runs `migrate-from-catalog.sql` on **every** start; idempotent; no-op once the catalog
tables are gone.

- **Steps** — on a database that still has `catalog.product_availability` / `product_price` /
  `product_reservation*` rows (a pre-split dump, or the simulation the PR ran), start inventory; read the log
  and `inventory.sm_sequencer`.
- **Expect** — every availability row copied **with `sku` backfilled** from `catalog.product` (the column was
  NULL pre-split and it is now the reservation key); every price row copied with `store_merchant_id` from its
  availability; reservations and lines copied; every sequencer **≥ the max copied id**; log line
  `Catalog-to-inventory data migration completed; all availability rows carry a sku`. A second start changes
  nothing. Any row left with a NULL sku logs an **ERROR** naming the count — that is a finding.

### INV-10 — Dropping the old tables is manual · [not verified]

- **Steps** — after INV-09 is verified on the target, run `extra/scripts/drop-catalog-inventory-tables.sql`.
- **Expect** — catalog boots without them; nothing in either service references them. Do not run it before
  INV-09 has been checked on that database.

---

## RES — Reservations (checkout ↔ inventory)

Every call is keyed by the order ref and idempotent. Expiry is 45 minutes (`reservation.expiry.minutes`), the
sweep runs every 60 s (`reservation.cleanup.interval`) and tells checkout.

### RES-01 — Reserve takes stock; a retry does not take it twice · critical · [verified]

- **Steps** — `POST /private/reserve/qa-1` with `{"entries": [{"sku": "SKU-NK-RUN-001", "reserveQty": 2}]}`;
  read INV-01; `POST` the same again.
- **Expect** — `{"status": true, "reservationId": n, "expireAt": <now+45m>}`; quantity down by 2; the retry
  answers the **same** reservation and the quantity is **still** down by only 2.

### RES-02 — Not enough stock, and a sku that is not stocked · critical · [verified]

- **Steps** — `reserveQty: 999`; then `"sku": "SKU-NOPE"`.
- **Expect** — **422** `INVENTORY.RESERVATION.INSUFFICIENT_INVENTORY` with `params {sku, requested,
  available}` (available `0` for the unstocked sku); **nothing was taken** for any line of that request
  (the transaction rolls back — send two entries, the second short, and check the first's quantity).

### RES-03 — No lines · high · [verified]

- **Expect** — `{"entries": []}` → **400** `INVENTORY.RESERVATION.EMPTY`.

### RES-04 — Commit, then release after commit · critical · [verified]

- **Steps** — `POST /private/commit/qa-1`; `POST /private/release/qa-1`.
- **Expect** — `status: true` (row `COMPLETED`); then `status: false` — a committed reservation is not given
  back. Commit again: `true` (idempotent). Commit an unknown ref: `status: false`, no exception.

### RES-05 — Release gives the stock back · critical · [not verified]

- **Steps** — reserve `qa-2` for 3; release; release again; read the quantity.
- **Expect** — `true` (row `ROLLBACK`, quantity restored); `true` again; the quantity restored **once**.

### RES-06 — Expiry sweeps a held reservation and tells checkout · critical · [not verified]

- **Steps** — set `reservation.expiry.minutes=1` for inventory (`application-lcl.yml` or `-D`), reserve
  `qa-3`, wait two minutes, read the quantity and `inventory.log` / `checkout.log`.
- **Expect** — quantity restored, row `ROLLBACK`, inventory logs `Released expired reservation qa-3`, checkout
  received `handleReservationExpired` (checkout's log; the order — if one exists under that ref — moves to
  its expired state). Commit after expiry: `status: false` with a warning logged. If checkout is down the
  release still happens and inventory logs the failed notification once.

### RES-07 — Two orders racing for the last unit · high · [not verified]

- **Steps** — set a sku to quantity 1; fire two reserves for 1 with different refs concurrently (two `curl &`).
- **Expect** — exactly one `status: true`, one 422. The reserve path reads the row with a pessimistic write
  lock.

### RES-08 — Reservation calls are same-pod only · critical · [verified] / [not verified]

- **Steps** — no token (verified 401); a seller session from **another pod's** store (only possible with a
  second pod locally — see `qa/tenancy-and-pod-registry-split.md`).
- **Expect** — 401; 403.

---

## CHK — Checkout composes the two services

`ProductDetailsComposer` (checkout-core) calls catalog's `detailed-product` and inventory's bulk read for one
sku and hands the cart and order code a `(product, inventory)` pair.

### CHK-01 — Add to cart shows catalog copy and inventory price · critical · [verified]

- **Steps** — `POST /checkout/api/v1/cart?store=…&lang=en` with `{"product": "SKU-NK-RUN-001", "quantity": 1}`.
- **Expect** — the cart line with the product's name and image from catalog and `displayTotal` `SAR750.00`
  from inventory (or 608 while the special is on — the cart charges `finalPrice`).

### CHK-02 — A sku with no inventory cannot be added · critical · [not verified]

- **Steps** — add PRD-01's sku (catalog only, no inventory row).
- **Expect** — refused with checkout's `ProductNotPurchasableException` code; the same for `available: false`
  or `quantity: 0` in inventory. The composer treats a missing sku as *not stocked* — never a 500.

### CHK-03 — A cart line whose product lost its price is dropped · high · [not verified]

- **Steps** — add a product, then delete its inventory rows by SQL, then read the cart.
- **Expect** — the cart is rebuilt without that line (checkout marks the item obsolete). Before the rewrite
  this was a NullPointerException.

### CHK-04 — Catalog down · critical · [not verified]

- **Steps** — stop catalog, add to cart.
- **Expect** — a typed remote failure from checkout, not a 500 with a stack trace; the storefront's cart
  page reports it. Inventory down: the same, with `INVENTORY.*` codes.

### CHK-05 — Placing an order reserves, then commits · critical · [not verified]

- **Steps** — a full storefront checkout (`qa/billing-per-store-subscriptions.md` has the payment set-up).
- **Expect** — `inventory.product_reservation` gains a `TEMPORARY_RESERVED` row under the order ref on
  placement and it becomes `COMPLETED` on payment; the sku's quantity drops by the ordered amount and stays
  down. A failed payment: `ROLLBACK` and the quantity back.

### CHK-06 — The order records the price it sold at · high · [not verified]

- **Steps** — after CHK-05 with a special price active, read `checkout.order_product_price`.
- **Expect** — one row per line: `product_price_code base`, `default_price true`, `product_price` = the final
  price, and the special amount/dates copied when the sale was discounted. Changing the price afterwards does
  not change the order.

---

## UI — The console

Products (`/products`), the product form (`/products/new`, `/products/{id}`) and Catalogue (categories,
brands, types, groups). Specs: `features/products/**`, `features/product-form/**`, `features/catalogue/**`,
`api/catalog/*.spec.ts`, `api/inventory` — 962 Karma cases pass, which is what every [unit only] below means.

### UI-01 — The product table merges price and stock from inventory · critical · [verified]

- **Steps** — open Products as org1-admin.
- **Expect** — rows with name, categories, brand, image, **price and quantity** (one bulk inventory call for
  the page's skus — check the network tab: `GET /spg/inventory/api/v1/availability?skus=…`). A product with
  no inventory row shows `0` and no price, not an error.

### UI-02 — Inline edit writes two services · critical · [unit only] (`products.api.service.spec.ts`)

- **Steps** — change price and quantity of a row inline, toggle availability, save.
- **Expect** — a `PATCH /spg/catalog/api/v1/private/product/{id}` with **both** switches and a
  `PUT /spg/inventory/api/v1/private/inventory/{sku}` with `{productId, quantity, available, price: {amount}}`;
  the row re-renders from the reload. An empty price sends `amount: 0`, not nothing.

### UI-03 — The product form loads the definition and the stock · critical · [unit only]

- **Steps** — open a seeded product.
- **Expect** — the Pricing step shows the inventory price (`originalPrice`, falling back to `finalPrice`) and
  quantity; "can be purchased" reflects the inventory record's `available`; the definition steps show every
  language, the brand and type by code, the categories ticked.

### UI-04 — Create from the form is two writes, and a partial failure is honest · critical · [unit only]

- **Steps** — create a product with a price; then repeat with inventory stopped.
- **Expect** — `POST /spg/catalog/api/v2/private/product` then `PUT /spg/inventory/.../{sku}`; on the second
  run the definition lands, the inventory write fails, and the form **says so** (`inventoryApplied: false`)
  rather than pretending — the product exists with no stock, which UI-01 then shows as `0`.

### UI-05 — Update merges categories by diff · high · [unit only]

- **Steps** — tick one category, untick another, save.
- **Expect** — one `POST .../category/{added}` and one `DELETE .../category/{removed}`, run **sequentially**
  (`concat`, not `forkJoin`), never a `PUT` with `categories` in the body.

### UI-06 — Delete cleans inventory best-effort · high · [unit only]

- **Steps** — delete a product from the table.
- **Expect** — `DELETE /spg/catalog/api/v1/private/product/{id}` then
  `DELETE /spg/inventory/api/v1/private/inventory/by-product/{id}`; the second failing does not fail the
  delete.

### UI-07 — The category tree page · critical · [not verified]

- **Steps** — open Catalogue → categories; create a root, a child under it; rename; move; hide; delete.
- **Expect** — each maps to the CAT endpoints (create/`PUT`/`move`/`visible`/`DELETE`); the tree re-renders
  with names in the console's language from `descriptions`; the uniqueness check runs on the code field.

### UI-08 — Brands, types and groups pages · high · [not verified]

- **Steps** — create / edit / delete one of each; add and remove a group member.
- **Expect** — the BRD / TYP / GRP endpoints, in the shapes those sections describe; the brand page shows no
  logo and no publish flag (by design — `lessons.md`).

### UI-09 — Related products picker · high · [not verified]

- **Steps** — on a product, search the picker by **sku** (not name), add two related products, remove one.
- **Expect** — the picker searches `GET /api/v2/products?sku=` (the only text filter the catalog has); adds
  and removes hit `/private/products/{id}/relationship/{productId}`.

### UI-10 — The moderator can read and cannot write · critical · [not verified]

- **Steps** — as `org1-store1-moderator`, open every page above and try one write on each.
- **Expect** — pages render (the reads are public or read-permitted); every write is a **403** surfaced as a
  disabled control or an error, never a silent no-op.

### UI-11 — Arabic, right to left · high · [not verified]

- **Steps** — switch the console to `ar`, open Products, the form, Catalogue.
- **Expect** — product and category names in Arabic (from `descriptions`, matched on `ar`), layout mirrored,
  the price column still showing the store currency correctly formatted.

---

## SF — The storefront

### SF-01 — Home strips carry prices from inventory · critical · [verified]

- **Steps** — open `/en` (or `/ar` — org1-store1's default is Arabic).
- **Expect** — 200; the four strips render products with names, images and prices. The price is fetched in
  bulk from inventory for each strip's skus (`InventoryService.enrichProducts`); a strip whose group 404s is
  simply absent.

### SF-02 — Category page: listing, facets, sort · critical · [verified] (page) / [not verified] (facets, sort)

- **Steps** — `/en/category/men`; filter by brand; sort newest.
- **Expect** — 200 with the subtree's products and prices; the brand facet is BRD-04's list; sort sends
  `sort=dateAvailable,desc`; the "variants" facet group is **never** rendered (always empty since the split).

### SF-03 — Product page without related items, without inventory · high · [verified] / [not verified]

- **Steps** — a product with no `RELATED_ITEM` group (all seeded ones); then stop inventory and reload.
- **Expect** — the page renders without the related strip (verified); without inventory it renders with no
  price and add-to-cart disabled — the product itself **must not** fail because a strip did.

### SF-04 — Unknown slugs · [verified]

- **Steps** — `/en/product/does-not-exist`.
- **Expect** — the catalog answers 404; the Next dev server currently renders a **500** for it (a pre-existing
  stream error in the dev server, `build/lcl-logs/landing-ui.log`, `controller[kState].transformAlgorithm is
  not a function`). Not a catalog defect; listed so it is not filed as one.

---

## SEC — Permissions and tenant isolation

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

## ARC — What the rewrite left behind

### ARC-01 — The live schema creates no attribute tables · high · [verified]

- **Steps** — on a fresh database, `\dt catalog.*`.
- **Expect** — 14 tables: sequencer, category(+description), manufacturer(+description), product_type
  (+description), product, product_category, product_description, product_image, product_group(+description),
  product_group_product. No `product_option*`, `product_attribute`, `product_opt_set*`, `product_digital`,
  `product_image_description`. On an **existing** database those tables still exist (`create if not exists`
  never drops) — that is expected; `catalog-deprecated/deprecated-ddl.sql` documents them.

### ARC-02 — The inventory schema holds no price descriptions · [verified]

- **Expect** — `\dt inventory.*`: sequencer, product_availability, product_price, product_reservation,
  product_reservation_line. Seeds create no description rows; the migration copies none.

### ARC-03 — Nothing in the repo calls a removed type or endpoint · [verified]

- **Steps** — `./gradlew build -x test -x check`; `npm run build` in console-ui and landing-ui.
- **Expect** — clean. checkout compiles against the new `ReadableMinimalProduct` and `SkuInventory`.

### ARC-04 — `catalog-deprecated` is not built · [verified]

- **Expect** — no `settings.gradle` entry; the directory compiles nothing. Its README describes the
  reintroduction path and the error codes that were pruned with it.

---

## REG — Regression watchlist

Every row was a real defect found while building or verifying this PR.

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Upsert 400'd from the console** | `PersistableInventory.sku` was `@NotEmpty`, validated before the controller could copy it from the path. | INV-05 |
| **Every catalog read 500'd on first boot of the rewrite** | Seed rows have `null` in `visible`, `sort_order`, `prd_type_add_to_cart`; the entities had primitive fields. Hibernate refused to set them. | LST-01, CAT-01, TYP-01 — any read of seeded data |
| **Category tree and brand list 500'd with no filter** | `?2 is null or lower(d.name) like …` — Postgres typed the null parameter as `bytea`. Split into two queries. | CAT-01, CAT-03, BRD-01 |
| **Every request 500'd with "Cannot change HTTP Accept-Language header"** | The locale resolver bean looked unused and was removed; the shared request context writes the locale. | Any request at all — SEC-01's sample |
| **The console's category tree showed codes instead of names** | The private hierarchy passed `nonLanguage`, so neither `description` nor `descriptions` was set. | CAT-02 |
| **A cart line with no price NPE'd** | The composer handed a null `price` to the cart populator. | CHK-03 |
| **The old catalog listing hid products with no availability row** | Inner joins to `product_availability` in the fetch queries; the count was right only because every seed had one. | LST-01 (`totalElements` 45 with a product created by PRD-01 → 46) |
| **`restart inventory` took the whole stack down** | `run-lcl.sh restart <svc>` stops it, the supervisor sees a service exit and stops everything. | `qa/run-lcl-lifecycle.md`; use `stop` + `start -d` |
| **Generated seed inserts had 7 values for 6 columns** | A regex added `sku` to wrapped column lists inconsistently. | ARC-01 — inventory boots and `select count(*) from inventory.product_availability` = 180 |

---

## 99 — Known gaps

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

**Inventory has no billing write gate.** `StoreBillingWriteGate` is catalog's; a store with a lapsed
subscription can still change stock and prices through the inventory API. The console reaches inventory only
after a catalog write that the gate refuses, so it is not reachable from the screen.

**A merchant's related-items and groups are single-store.** There is no cross-store or org-level group.

**An inventory row survives its product.** The console deletes it best-effort; by API, nothing does. Orphans
are invisible to every reader (the bulk read is by sku and the listing never joins inventory) and cost a row.

**`quantityOrderMaximum` is `0` for "no limit" and the seeds set it to `1`.** Nothing enforces either value
today — checkout does not read the limits. Recorded so a future enforcement does not surprise anyone.

**Sorting the listing by anything but a direct `Product` column is a 500.** `SORT_MAP` in landing-ui exposes
only `dateAvailable`.

**The Next dev server 500s on unknown slugs** instead of rendering a 404 page (SF-04). Dev-only.

---

Raise anything unexpected against PR #282. Include the store id, the sku or category id, the time, and the
matching `Unhandled failure [traceId=…]` block from `build/lcl-logs/catalog.log`, `inventory.log` or
`checkout.log`. For a console defect attach the browser console and the failing request: a 401 is a missing
session, a 403 is a permission problem, a 404 through `/spg/**` is usually a missing `pod` parameter, and a
400 with a `*.REFERENCE_UNRESOLVABLE` code names the exact value that did not resolve in this store.

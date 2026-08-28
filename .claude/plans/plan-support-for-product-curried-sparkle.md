# Product search: catalog full-text backend + storefront search (basic theme)

## Context

Every storefront theme ships a header `SearchBox`, and none of them can search products. The reason is
written into the code as a standing TODO — `libs/types/src/search.ts`:

> *"Search is an abstraction because the catalog has NO text-search endpoint today … Themes render search UI
> only according to `SearchCapabilities`, so nothing promises a feature the platform cannot deliver."*

So `capabilities.text` is hard-coded `false` everywhere, the box returns category/CMS-page suggestions only,
and pressing Enter does nothing because no results route exists. `PRODUCT.md` lists "no text search endpoint"
as a product constraint.

The abstraction was built for exactly this change. The work is to give the catalog a real multilingual
full-text search with filters and facets, then flip `text: true` and add the results page the box can submit to.

**Decisions taken with the user:**

| Decision | Choice |
|---|---|
| Theme coverage | `pages.Search` **optional** in the contract; a designed page in **`basic` only**. The other 11 themes run on a shell fallback and get their own plan. |
| Index maintenance | **Domain events through the existing outbox**, not database triggers |
| Price / in-stock filter & sort | **Deferred** — inventory owns price by sku; phase 1 filters on catalog-owned facets only |
| Arabic | **Custom immutable normalisation** (tashkeel stripping, alef/teh-marbuta folding) |
| API surface | **Full search + facet counts**, plus a separate lightweight suggest endpoint |
| Workflow | A **new git worktree on a new branch** (`feat/catalog-product-search`) |

## Why Postgres full-text, and where it stops

Recommended for this stage. `pg_trgm`, `unaccent`, `btree_gin` and snowball configs for `arabic`, `english`,
`french`, `spanish`, `russian` (the five storefront locales) are all present in the `postgres:15-alpine` image
the stack already runs — no new infrastructure, no separate search cluster.

Two premises were probed against the running local Postgres before writing this plan, both confirmed:

- A `tsvector` **generated column** whose text-search config is chosen per row by an `IMMUTABLE` function is
  accepted by PG 15. `أَحْذِيَة رِجَالِي` indexes to `'احذ':1A 'رجال':2A`, and a query for the unvocalised,
  teh-marbuta-folded `احذيه` matches it — the stock `arabic` stemmer alone does **not** do this.
- `unaccent()` is `STABLE`, so it cannot be used in a generated column directly; the documented `IMMUTABLE`
  wrapper pinned to `'public.unaccent'::regdictionary` works, and makes `veritable` match `Véritable`.

**Honest limits, so the ceiling is a decision and not a surprise:**

- No built-in typo tolerance. Mitigated by a `pg_trgm` similarity fallback, not solved.
- No synonyms/stopword tuning per merchant without shipping PG dictionary files.
- No semantic/vector search (`pgvector` is not available in this image).
- Facet counts need a second aggregate pass over the same predicate.
- Relevance is static `ts_rank_cd`; there is no click/conversion signal feeding it.
- Practical ceiling roughly single-digit millions of product-language rows per pod. Past that, or when
  semantic search is wanted, the intended exit is to swap the `ProductSearchService` implementation for
  OpenSearch — which is why that interface exists in this plan.

---

## Part 0 — Worktree

All work happens off `main` in an isolated worktree, so the current branch
(`feat/demo-store-content-seeds`, which has a wide dirty tree) is untouched:

```
branch:   feat/catalog-product-search
worktree: a fresh checkout (EnterWorktree), not the primary working copy
```

The catalog service and landing-ui both change, and they live in the same repo, so one worktree covers both.

---

## Part 1 — Catalog backend

### 1.1 Schema (`catalog-service/src/main/resources/init-sql/schema.sql`)

Appended in the file's existing idempotent style (`create … if not exists`, `alter … add column if not exists`).
There is no Flyway here; `spring.sql.init` runs this script on every boot.

**Extensions** — `create extension if not exists unaccent / pg_trgm / btree_gin`.
On RDS all three are in the `rds_superuser` allowlist. If the production role cannot create them, these three
lines must be run once by a DBA and the script then no-ops.

**Two immutable functions in the `catalog` schema:**

- `catalog.search_config(varchar) → regconfig` — maps `language_code` to a snowball config
  (`ar→arabic, en→english, fr→french, es→spanish, ru→russian`, else `simple`).
- `catalog.search_normalize(text) → text` — lowercases, strips Arabic tashkeel (`U+064B–U+0652`) and tatweel,
  folds `أ إ آ → ا`, `ة → ه`, `ى → ي`, then applies the immutable `unaccent` wrapper for Latin scripts.

This function is the single normalisation implementation. **Java must never re-implement it** — the query side
calls `catalog.search_normalize(:q)` in SQL, which is the only way to guarantee index and query agree.

**A dedicated index table:**

```sql
create table if not exists catalog.product_search_index (
    product_id        bigint      not null,
    language_code     varchar(6)  not null,
    store_merchant_id varchar(50) not null,   -- denormalised: lets one index answer store+lang+text
    name_normalized   text,
    search_document   tsvector,
    indexed_at        timestamp(6) not null default now(),
    primary key (product_id, language_code)
);
```

`search_document` is built with weights so name beats body:
`A` = product name, `B` = title + meta_keywords + sku + ref_sku + brand name, `C` = highlight, `D` = description.

Why a separate table rather than columns on `product_description`:

1. A generated column can only reference its own row, so it could not include `product.sku` or the brand name.
2. `store_merchant_id` has to sit beside the vector for the composite GIN index below.
3. Nothing in JPA maps this table, so the reindex path owns it end to end with no risk of Hibernate
   round-tripping a `tsvector`.

**One SQL function does the actual document build:**

```sql
catalog.refresh_product_search_index(p_product_id bigint) -- upsert all languages of one product
catalog.rebuild_product_search_index(p_store varchar)     -- whole-store rebuild / backfill
```

Keeping the document shape in SQL means normalisation, weighting and config selection live in exactly one
place. Java only decides *when* to call it — see 1.2.

**Indexes:**

```sql
create index if not exists product_search_doc_idx  on catalog.product_search_index
       using gin (store_merchant_id, language_code, search_document);   -- needs btree_gin
create index if not exists product_search_trgm_idx on catalog.product_search_index
       using gin (store_merchant_id, name_normalized gin_trgm_ops);
```

The composite GIN is the point of the whole design: without `store_merchant_id` in the index, a search for
"shirt" scans every matching row **across all tenants in the pod** and only then filters — cost grows with
tenant count rather than with the store's own catalogue.

Also add two btree indexes that are missing today and sit directly on the facet path:
`product(store_merchant_id)` and `product_category(category_id)`.

Plus the **outbox tables** (`outbox_record`, `outbox_instance`, `outbox_partition` and their indexes) in the
`catalog` schema — copy the block from `payment-service/src/main/resources/init-sql/schema.sql:54-120`, which
already does this for the same library, since both services set
`namastack.outbox.jpa.schema-initialization.enabled: false`.

> `create index` in `schema.sql` cannot be `concurrently`. Fine at current data volumes; for a large
> production table, build these out-of-band first and the `if not exists` will then no-op at boot.

### 1.2 Keeping the index fresh — domain events over the outbox

The platform already has this pattern and catalog can join it with **no base-class change**:
`Product extends SalesManagerEntity` → `org.springframework.data.domain.AbstractAggregateRoot`, so
`registerEvent(...)` is available on the aggregate today. `payment-service` is the closest precedent —
same pod, same JPA flavour, same library.

**Wiring** (mirroring `payment-service`):

- `catalog-service/build.gradle` → `implementation libs.namastack.outbox.starter.jpa`
- `catalog-commons/build.gradle` → `api libs.namastack.outbox.api`
- `catalog-service/src/main/resources/application.yml` → the `namastack.outbox` block copied from
  `payment-service` (multicaster on, 2s fixed polling, batch 10, `schema-name: ${spring.application.name}`,
  schema-initialization off).

**Events** in `catalog-commons/model/product/event/` (mirroring `payment-commons/model/payment/event/`),
implementing the shared `com.asrevo.cvhome.commons.event.Event`:

| Event | Registered when | Handler does |
|---|---|---|
| `ProductSearchIndexStaleEvent(productId, storeId)` | product created or updated — copy, sku, brand, type, categories or images | `refresh_product_search_index(productId)` |
| `ProductSearchIndexPurgedEvent(productId, storeId)` | product deleted | delete its rows |
| `BrandRenamedEvent(manufacturerId, storeId)` | `manufacturer_description.name` changes | reindex that brand's products in batches |

Partition key is the **product**, not the store:

```java
@OutboxEvent(key = "#this.productId()")
```

Two rapid edits to one product must apply in order; two different products must not queue behind each other.
Keying on the store would serialise reindexing for an entire merchant. `BrandRenamedEvent` keys on the
manufacturer for the same reason.

**Registration** is on the aggregate, following `Transaction.success()` in payment — a named method that
mutates and registers, never a setter:

```java
// Product
public Product searchIndexStale() {
    this.registerEvent(ProductSearchIndexStaleEvent.from(this.id, this.store));
    return this;
}
```

Called from the existing mutation paths in `ProductServiceImpl.create/update/delete` and the image/category
services. Because `ProductDescription` is cascaded from `Product`, copy edits are already covered by
registering on the product.

**Handler** — `catalog-service/.../service/CatalogSearchOutboxHandler.java`, `@OutboxHandler` methods
delegating to `ProductSearchIndexer`, which is a thin call onto the SQL function:

```java
@Modifying
@Query(value = "select catalog.refresh_product_search_index(:productId)", nativeQuery = true)
void refresh(@Param("productId") Long productId);
```

**The consequence to be honest about: the index is now eventually consistent.** The event row commits in the
same transaction as the product change, then the poller picks it up within ~2s. A merchant who saves a
product will not find it by search for a couple of seconds. That is the trade for durability, retries,
ordered per-product processing, batched brand renames, and no PL/pgSQL trigger web — and it is why the
integration tests below drive the handler directly rather than asserting immediately after a write.

A one-time populate at the end of `schema.sql` (`insert … select … on conflict do nothing`) backfills
existing rows, and `POST /api/v2/private/products/search-index/rebuild` re-runs a store after any change to
the document shape.

### 1.3 Java — query shape

The hard constraint: a `tsvector` must never appear in a select list, or Hibernate has to materialise it per
row. The design that satisfies that **and** keeps clean JPA Specifications is to leave the query root as
`Product` and express the text match as a correlated `EXISTS`:

```java
// ProductSpecifications.matchesText(...)
Subquery<Integer> sq = query.subquery(Integer.class);
Root<ProductSearchIndex> idx = sq.from(ProductSearchIndex.class);
sq.select(cb.literal(1)).where(cb.and(
        cb.equal(idx.get("productId"), root.get(ID)),
        cb.equal(idx.get("store"), store),
        cb.equal(idx.get("languageCode"), language),
        cb.isTrue(cb.function("fts_match", Boolean.class, idx.get("searchDocument"), tsQuery))));
return cb.exists(sq);
```

The subquery selects a literal, so the vector is never fetched, the composite GIN index is fully usable, and
because the root is still `Product` **every existing filter predicate, `Page<Product>`, and `ProductMapper`
keep working unchanged**.

Relevance ordering is a correlated scalar subquery over `ts_rank_cd`, applied via `query.orderBy(...)` only
when the caller's `Pageable` is unsorted, and guarded with `if (query.getResultType() != Long.class)` so
Spring Data's count query is untouched.

### 1.4 Java — files

**`catalog-commons`** (`model/product/`) — wire DTOs only, no Spring:
- `ProductSearchCriteria` — `q`, `categoryIds`, `manufacturerIds`, `productTypeIds`, `available`, `sort`,
  `facets` (bool). Plain setters, bound from the query string like the existing `ProductFilter`.
- `ProductSearchSort` enum — `RELEVANCE, NEWEST, OLDEST, NAME, SORT_ORDER`.
- `ReadableProductSearchResult` extends `ReadableEntityList<ReadableProduct>`, adds `facets` and `didYouMean`.
- `ReadableSearchFacets` / `ReadableFacetBucket` (`id`, `name`, `count`, `selected`).
- `ReadableProductSuggestion` — `id`, `name`, `friendlyUrl`, `sku`, `imageUrl`, `brand`.
- `model/product/event/` — the three events above.

**`catalog-core`:**
- `entity/ProductSearchIndex.java` — `@Entity @Immutable`, `@IdClass`, maps `search_document` through a
  `TsVectorType`. Never selected, only referenced in predicates.
- `config/PostgresFtsFunctionContributor.java` — Hibernate 6 `FunctionContributor` registering `fts_match`
  (`?1 @@ ?2`), `ts_rank_cd`, `websearch_to_tsquery`, `similarity`, `catalog.search_normalize`.
- `repositories/ProductSpecifications.java` — **new, and the direct answer to "clean filter via
  Specification"**. Static `Specification<Product>` factories: `inStore`, `available`, `skuLike`,
  `inCategories`, `byManufacturers`, `byTypes`, `matchesText`, `similarTo`, composed with
  `Specification.allOf(...)`.
  The inline lambda now sitting in `ProductRepository.search` is **refactored to call these**, so the console
  listing, the category page and search share one filter vocabulary instead of two.
- `repositories/ProductSearchIndexRepository.java` — the refresh/purge/rebuild calls.
- `repositories/ProductFacetRepository.java` — one grouped query per dimension (category, brand, type) over
  the same `Specification`, returning `(id, count)`; names come from the already-loaded descriptions.
- `services/product/ProductSearchService` + `ProductSearchServiceImpl` — **the seam an OpenSearch
  implementation would later replace.** Owns: build tsquery, page products, run facets, run the fallbacks.
- `services/product/ProductSearchIndexer.java` — invoked by the outbox handler.

**`catalog-service`** — three endpoints on `ProductApiV2` (public, since the convention is that only
`/private/` paths authenticate) plus the outbox handler:

```
GET  /api/v2/products/search ?store&lang&q&categoryIds&manufacturerIds&productTypeIds
                             &available&facets&page&count&sort   → ReadableProductSearchResult
GET  /api/v2/products/suggest?store&lang&q&limit(≤10)            → List<ReadableProductSuggestion>
POST /api/v2/private/products/search-index/rebuild               → existing CATALOG permission
```

Pagination params are `page` / `count`, not `page` / `size` — `ServletWebConfig` renames them globally.

### 1.5 Fallbacks and query handling

- Query text goes to Postgres raw; `websearch_to_tsquery(config, catalog.search_normalize(:q))` parses it,
  which gives shoppers quoted phrases and `-exclusion` for free and cannot throw on malformed input the way
  `to_tsquery` does.
- **Prefix matching** for suggest: last token gets `:*` appended so "run" matches "running".
- **Typo fallback**: if the tsquery page has zero total hits, retry once using the trigram index
  (`similarity(name_normalized, …) > 0.3`, ordered by similarity) and return the best name as `didYouMean`.
- **Language fallback**: if the requested language yields zero hits and it is not the store's default
  language, retry once against the default. Costs an extra query only on the already-empty path.
- Blank `q` is valid — it degrades to a plain filtered listing, which is what the results page needs when a
  shopper clears the term but keeps their filters.

### 1.6 Latency work

- Facets are one extra grouped query, skipped entirely when `facets=false`.
- `ProductMapper.specification()` calls `merchantStoreService.getStore()` **per product**; a 24-row search
  page multiplies that by 24. Hoist the lookup to once per page.
- The listing path does not fetch-join descriptions/images, so mapping N+1s. Hydrate the search page's
  products with an `@EntityGraph` covering descriptions, images, manufacturer and type.
- Suggest is the hottest and most repetitive path: `@Cacheable` on the existing Caffeine/`@EnableCaching`
  setup keyed `store:lang:q`, short TTL, plus `Cache-Control: public, max-age=30` on the response.
- Suggest uses `LIMIT`, never a `Page` — no count query on the keystroke path.

### 1.7 Backend tests

`catalog-service/src/integrationTest/java/.../api/v2/ProductSearchApiIntegrationTest.java`, using the existing
`@StorageIntegrationTest` + Testcontainers + `CatalogApiSupport` pattern so `schema.sql` runs for real. To
avoid racing the 2s poller, tests seed through the API and then invoke `ProductSearchIndexer` directly —
the same approach as `payment-service`'s `OutboxHandlersTest`.

Cases: relevance ordering, Arabic normalisation (`احذيه` finds `أحذية`), accent-insensitive French, sku match,
each filter, facet counts matching the filtered page, pagination, typo fallback and `didYouMean`, blank-`q`
listing, and **tenant isolation via `STORE_A`/`STORE_B`** — a term present in both stores must return only
the caller's.

Seed data needs `ar` product descriptions in at least one store; the existing seeds under
`init-sql/stores/<id>/14-catalog-product.sql` are `en`/`fr`.

Unit tests: `ProductSpecifications` composition, `ProductSearchServiceImpl` fallback branching, and an
aggregate test asserting `Product.searchIndexStale()` registers exactly one event — mirroring
`TransactionTest` / `StoreSubscriptionEntityTest`.

---

## Part 2 — Storefront

### 2.1 Shared libs

- `libs/types/src/search.ts` — keep `SearchCapabilities` (its comment gets rewritten); add
  `ProductSearchPage`, `SearchFacets`, `FacetBucket`, `ProductSuggestion`.
- `libs/types/src/listing.ts` — add `q` and the facet-id arrays to `ListingQuery`, `parseListingQuery` and
  `listingQueryToSearchString`. These are pure and already shared with the category page.
- `libs/services/src/product-search-service.ts` — `search()` and `suggest()`, built exactly like
  `product-category.ts` (`storeBaseServiceUrl('catalog', ctx)` + `?store&lang`), then
  `InventoryService.enrichProducts(...)` so results carry price and stock like every other product surface.
  `SORT_MAP` in `product-category.ts` currently maps `relevance → undefined`; that becomes a real value.
- **Delete the 12 duplicated `themes/*/src/sections/search-provider.client.ts`.** Each is a near-verbatim copy
  hard-coding `{text: false, suggestions: true}`, which means flipping the shell's capability flag would never
  reach the browser. Replace with one shared provider that merges product hits from the new endpoint with the
  existing category/page suggestions.
- `storefront/src/shell/search/product-search-provider.ts` + flip the default in `getSearchProvider()`.
  `LayoutData.search` then carries `{text: true, suggestions: true}` and every theme's existing
  `capabilities.text` branch lights up on its own.
- `libs/hooks/src/use-product-search.ts` — `useProductListing` is hard-wired to `category.id`. Extract its
  shared paging / filter / URL-sync / retry core into `useListingSource` and have **both** hooks wrap it,
  rather than forking a second copy of that logic.

### 2.2 Route and contract

- `storefront/src/app/(storefront)/[locale]/search/page.tsx` — searchParams-only page; `blog/page.tsx` is the
  closest existing template. No Suspense wrapper, matching the deliberate comment on the category route.
- `storefront/src/shell/loaders/search.ts` — `loadSearch(query)`, mirroring `loadCategory`, `cache()`d,
  returning `SearchData { query, initial, facets, didYouMean }`.
- `libs/theme/src/contract.ts` — `Search?: ComponentType<PageProps<SearchData>>` **optional**, and
  `PageSkeletonKind` gains an optional `'search'` that falls back to the `category` skeleton.
  `define-theme.ts` keeps `Search` out of the required `PAGES` array, so **the build stays green for all 12
  themes** while only `basic` implements it.
- `storefront/src/shell/theme/default-search-page.tsx` — the fallback, composed from the theme's own
  `EmptyState` and product grid so an unadopted theme still looks like itself.
- `ThemeLayoutConfig.search` gains `'page'` alongside `'header' | 'overlay' | 'hidden'`.
- `/search` is `noindex, follow` in `robots.ts`, and stays out of `sitemap.ts`.

### 2.3 Themes

**All 12 themes** get the small `SearchBox` change: form submit → `/search?q=…` via the locale-aware
`useRouter` from `@store-front/i18n/navigation`, product hits with thumbnails in the dropdown, and a
"See all results" footer row. This is a shared edit repeated per theme file, and it is what makes the
fallback page reachable everywhere.

**Only `basic`** gets a designed `pages/Search.tsx` — results grid, filter rail, active-filter chips, sort
control, did-you-mean line, empty state — plus `states/skeletons/SearchSkeleton.tsx`. The other 11 themes
render the shell fallback and are covered by a follow-up plan.

`libs/ui` gains `popover.tsx` and `command.tsx` — an accessible combobox is currently hand-rolled in each
theme, and the guide's rule is that a primitive that cannot be themed gets added to `libs/ui`, never forked.

RTL: logical utilities only (`ps/pe/ms/me/start/end`) — eslint already warns on physical ones under `themes/**`.

### 2.4 i18n and docs

- New `PAGE.SEARCH.*` keys in **all five** `locales/{en,ar,es,fr,ru}.json` (results count, did-you-mean,
  filter labels, sort labels, clear-filters). `STATES.EMPTY_SEARCH_TITLE/BODY` and `COMPONENTS.SEARCH.*`
  already exist and are reused; `SUGGESTIONS_ONLY` and `NOT_AVAILABLE` become dead and are removed.
- Update `PRODUCT.md` (drop "no text search endpoint" from its constraints and from principle 2), the header
  comment in `libs/types/src/search.ts`, and
  `../skills/project-structure/references/new-landing-ui-template.md` (contract checklist and folder
  layout gain the optional Search page).

---

## Suggested order

1. Worktree + branch.
2. Schema: extensions, functions, index table, GIN/trgm/btree indexes, outbox tables, backfill.
3. Outbox wiring + the three events + registration on the aggregate + handler + indexer, with the aggregate
   and handler unit tests.
4. `ProductSpecifications` refactor of the existing listing — no behaviour change, green tests.
5. `ProductSearchService` + the two public endpoints + facets + fallbacks + integration tests.
6. Latency pass (mapper `getStore()` hoist, `@EntityGraph`, suggest cache).
7. Shared libs, provider de-duplication, capability flip.
8. `/search` route, loader, optional contract entry, shell fallback.
9. `basic`'s Search page and skeleton; SearchBox submit across all 12 themes.
10. i18n keys and docs.

Steps 2–6 are shippable on their own — the storefront keeps its current behaviour until step 7 flips the flag.

## Verification

**Backend**

```bash
./gradlew :store-pod:catalog:catalog-service:integrationTest
./gradlew :store-pod:catalog:catalog-core:test
```

Then against the running stack:

```bash
curl "http://catalog.gateway.com/api/v2/products/search?store=<oid>&lang=ar&q=احذيه&facets=true&count=24"
curl "http://catalog.gateway.com/api/v2/products/suggest?store=<oid>&lang=en&q=run&limit=8"
```

Confirm the index is used, not scanned:

```sql
explain (analyze, buffers)
select 1 from catalog.product_search_index
 where store_merchant_id = '…' and language_code = 'en'
   and search_document @@ websearch_to_tsquery('english', catalog.search_normalize('running shoes'));
-- expect: Bitmap Index Scan on product_search_doc_idx
```

Confirm the event pipeline: edit a product name in the console, then

```sql
select status, record_key, created_at, completed_at from catalog.outbox_record order by created_at desc limit 5;
select indexed_at from catalog.product_search_index where product_id = <id>;
```

— the record should reach completed within ~2s and `indexed_at` should move; then the new term must hit.
Also confirm a failing handler retries rather than silently dropping.

**Storefront**

```bash
npm run lint && npm run typecheck && npm run build
npm test --workspace=libs/theme
```

Then in the browser via the dev override
`http://org1-store1.spg-507f1f77.gateway.com/en?theme=basic`:

- type in the header box → product suggestions with thumbnails appear
- Enter → `/search?q=…` renders `basic`'s designed results page
- apply a category and a brand filter → chips, counts and URL all update; reload restores state
- `/ar?theme=basic` → RTL correct, Arabic query returns hits, no physical-direction regressions
- a nonsense query → `EmptyState` with `kind="search"`, plus did-you-mean when trigram finds one
- `?theme=starter` (or any other) → the shell fallback page renders correctly and is not broken

## Out of scope (named follow-ups)

- **A designed Search page for the other 11 themes** — its own plan, copying `basic`.
- **Price range filter and price sort** — needs an inventory endpoint returning the sku set for a price range
  so catalog can intersect before paging. Post-filtering on the storefront would give ragged pages and lying
  totals, so it is not a shortcut worth taking.
- In-stock-only filter (same dependency).
- Per-merchant synonyms, search analytics / zero-result reporting, click-signal relevance tuning.
- OpenSearch migration — `ProductSearchService` is the seam it would enter through.

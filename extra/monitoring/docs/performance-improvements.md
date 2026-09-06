# Performance: what the first load tests found, and what was done about it

The issues the first measured runs surfaced, in enough detail to plan work later. Each entry says what was seen,
how it was measured, where to look at it, what was found to be the cause, and — where a fix has landed — what
changed and how it was verified. Issues without a code fix say what still has to be measured.

The runs (2026-09-06, local stack: one JVM per service on one machine, PostgreSQL 15, Hikari pool 5 per service,
storefront on `next dev`): smoke, `storefront-browse` 30 VUs, `shopper-guest-checkout` 40/min, `mixed-production-mix`,
`storefront-breakpoint` ramp to 150 iterations/s. Numbers and test ids: `../../../load-testing/docs/baseline.md` (the
`load-testing` repo). Dashboards named below are the provisioned Grafana ones; their queries are in
[dashboards.md](dashboards.md), the KPIs in [kpis.md](kpis.md).

## Summary

| # | issue | severity | where | status |
|---|---|---|---|---|
| 1 | Storefront pages take 4.5–5 s at p95; the APIs behind them answer in milliseconds | high for the user, but measured on the dev server | landing-ui | open: measure on a built storefront first |
| 2 | catalog ran ~21 SQL statements per request; the product page loaded option values and their labels row by row | high, grows with the catalogue | catalog | **fixed**: batch fetching, 8 statements per product page |
| 3 | 132 k `SELECT cvhome` statements — a third of catalog's SQL hidden behind one span name | medium | catalog / collector | **fixed**: the collector names them from the statement |
| 4 | checkout ran ~11 statements per request; every insert cost two sequencer round-trips | medium | checkout, every pod | **fixed**: fifty ids per fetch |
| 5 | Admin traffic in the production mix failed 2 % with 403s | medium, a test-fixture problem | load-testing sessions | **fixed** in `load-testing`: sessions picked by role |
| 6 | `catalog → merchant` was the slowest service-to-service edge (p95 0.79 s) | medium | uaa (the s2s token endpoint) | **fixed**: one bcrypt per token request, 0.53 s → 0.3 s |
| 7 | `landing-ui → spg` showed 4–6 % failed calls | low | catalog public reads | **fixed**: absent strips answer an empty group, not 404 |
| 8 | No knee found: the API tier's capacity is unknown above 552 req/s | measurement gap | load-testing | open |
| 9 | 409s on category attach under concurrent catalogue edits | expected behaviour | catalog | no change |
| 10 | Login and redirect paths are the slowest routes on uaa and the gateway | low | uaa, gateway | partly: the `UNKNOWN` route on uaa is the token endpoint, see 6 |

## 1. Storefront pages: 4.5–5 s at p95

- **Seen.** `page:home` 4.99 s, `page:product` 4.72 s, `page:category` 4.66 s at p95 in the browse run (threshold
  3 s); the same ~4.4 s in the production mix. Server-side renders in landing-ui: `GET /[locale]` 4.73 s,
  `GET /[locale]/product/[url]` 4.73 s, `GET /[locale]/category/[url]` 4.70 s, `GET /[locale]/search` 4.51 s, while
  `GET /[locale]/checkout` is 1.24 s.
- **What the APIs did meanwhile.** catalog product 14 ms, search 27 ms, products-by-category 16 ms, inventory
  availability 4 ms, content layout under 50 ms — all at p95, all under their thresholds by 20× or more. spg's own
  p95 (2.6 s) is landing-ui's time seen from the gateway.
- **Where.** Edge → *Page render p95* and *Upstream fetch p95 by call*; Load test vs app → *k6 p95 by endpoint*
  against *App p95 by service*; a trace from Traces → *Slow requests* shows the render span with short fetch
  children underneath.
- **Known.** The stack runs the storefront with `next dev` (Turbopack, no build), which compiles on demand and does
  no caching. The k6 suite documents the same ("SSR and browser numbers are dev-server bound"). The time is inside
  the render, not in the upstream fetches (0.2 s p95 on the `landing-ui → spg` edge).
- **Still to measure.** The same runs against a built storefront (`next build` + `next start`, or the container
  image). Only then do these numbers say anything about the platform. If a built storefront is still above 3 s,
  the render span's children (`resolve page components`, `render route`, `generateMetadata`) in Tempo say which
  part; the upstream fan-out per page (site, layout, faq, category-hierarchy, products, groups, availability —
  seven to nine calls, each in both languages in the traces seen) is the next suspect.

## 2. catalog: ~21 SQL statements per request (N+1 on the product page) — fixed

- **Seen.** `cvhome:sql_per_request:ratio5m` for catalog was 21.3 during the browse run (checkout 11, every other
  service about 7). Over the 3 h of runs catalog served 28,560 `GET /api/v2/product/name/{friendlyUrl}` requests
  and ran:

  | statement family | count | per product request |
  |---|---|---|
  | `SELECT cvhome` (joins the instrumentation could not name — see issue 3) | 132,024 | 4.6 |
  | `SELECT catalog.product_option_value_description` | 61,903 | 2.2 |
  | `SELECT catalog.product_variant` | 35,921 | 1.3 |
  | `SELECT catalog.category_description` | 32,806 | 1.1 |
  | `SELECT catalog.product_option_value` | 31,468 | 1.1 |
  | `SELECT catalog.product_option_assignment` | 28,575 | 1.0 |
  | `SELECT catalog.product_description`, `product_image`, `product`, `product_type`, `manufacturer` | 2,300–4,100 each | ≈ 0.1 |

- **Cause.** A product page trace (Traces → *N+1 suspects*) showed the shape: one query for the product with its
  copy, images, brand and type; one for its categories; one for the variants; then, for each assigned option,
  one query for its values, and for each value one query for its labels
  (`ProductOption.values`, `ProductOption.descriptions`, `ProductOptionValue.descriptions` were the only
  collections on the page without Hibernate's `@BatchSize`). A product with one option and five values cost five
  label queries; the count grows with options × values.
- **Fix.** `@BatchSize(size = 100)` on those three collections (`catalog-core` entities `ProductOption` and
  `ProductOptionValue`), the same device the product's own collections already used.
- **Verified.** The same product page after the change: 8 statements — product, categories, category labels,
  variant count, variants (one hydrated query), option assignments, option values, value labels — whatever the
  number of values. Database & SQL → *Statements per request* is the KPI to watch; expect catalog well under
  10 under the browse mix.
- **Left.** The category read still costs two statements (categories, then their labels), the variant count one;
  they are one query each, not per row, and were left alone.

## 3. 132 k `SELECT cvhome` statements on catalog — fixed in the collector

- **Seen.** The most frequent JDBC span on catalog was `SELECT cvhome` — a SELECT whose table the instrumentation
  could not name (the span name falls back to the database name), 4.6 per product request; also 579 on checkout
  in the same period.
- **Cause.** Read from `db.statement` on a sample (Tempo: `{resource.service.name="catalog" && name="SELECT
  cvhome"}`): every one is a Hibernate join or subquery — the product fetched with its descriptions, images,
  brand and type; the category with its parent; the variants with their option values; checkout's order with
  its lines; the outbox poller's `not exists (…)` query. The OpenTelemetry JDBC instrumentation sets
  `db.sql.table` only for single-table statements, so multi-table statements lost their name and their
  `db_sql_table` dimension, and a third of catalog's SQL sat behind one row on the SQL panels.
- **Fix.** `extra/monitoring/logging-otel-collector-config.yml`, processor `transform/span_names`: for a JDBC span
  without `db.sql.table`, the first table after `FROM` / `INTO` / `UPDATE` in `db.statement` becomes
  `db.sql.table`, and a span still named `<operation> <database>` is renamed `<operation> <table>`. No
  application change; the span metrics' `db_sql_table` dimension and the per-table recording rules see every
  statement.
- **Verified.** After the collector restart, catalog's span metrics show no `SELECT cvhome`; the joins appear as
  `SELECT catalog.product`, `SELECT catalog.product_category`, `SELECT catalog.outbox_record`, with
  `db_sql_table` set. Porting note: on another provider the same rule is a span processor keyed on the statement
  text; the regex is in the collector file.

## 4. checkout: ~11 statements per request, two sequencer round-trips per insert — fixed

- **Seen.** 11.2 statements per request during the checkout and mix runs. Per order (579 orders in 3 h):
  `UPDATE checkout.sm_sequencer` and `SELECT checkout.sm_sequencer` 2,154 each (3.7 per order), `customer_account`
  1.7, `sales_order_total` 1.4, `sales_order` 1.0, `sales_order_event` 1.0, `cart_line` 0.8.
- **Cause.** Every pod entity takes its id from a `@TableGenerator` on the `sm_sequencer` table with
  `allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE`, and that constant was **0**: Hibernate's
  "no optimizer", one `SELECT` plus one `UPDATE` on the sequencer row per id, on a second connection from the
  pool, inside the request. An order with its lines, totals and event paid seven extra statements, and every
  order in flight serialised on the same rows. The same applied to every insert in catalog, payment and the
  other JPA pods.
- **Fix.** The constant is 50 (`store-pod/commons/store-commons/.../SchemaConstant.java`), and
  `common-config.yml` sets Hibernate's `id.optimizer.pooled.preferred: pooled-lo`. The optimizer choice is
  load-bearing: Hibernate's default `pooled` reads the sequencer row as the *last* id of a block and handed out
  negative ids against the seeded rows (the catalog integration tests caught it); `pooled-lo` reads it as the
  *first* id of the next block, which is what the rows already hold from the one-id-per-fetch days and what the
  seeds write, so existing databases migrate without a script. Ids stay unique and increasing; a restart skips
  what its JVM had left of the block, so they are no longer consecutive.
- **Verified.** On the live database (154 orders, last id 1154, sequencer row 1154): two smoke orders got 1155
  and 1156, the rows advanced by fifty, and the two orders together cost eight sequencer fetches — one per table
  touched, for the next fifty inserts of each. Product-group creation in the catalog integration tests passes
  against the seeded ids.

## 5. Admin traffic fails 2 % with 403s in the production mix — fixed in `load-testing`

- **Seen.** `http_req_failed{layer:admin}` 2 % (threshold 2 %) in `production-mix-load`: 403 on
  `tenancy:store-unique` (19), `tenancy:store`, `tenancy:store-info`, `billing:entitlement`, `merchant:store-private`
  (4 each). Server side: `tenancy /api/v1/store-manager/private/store/unique` 403 ×13, `merchant
  /api/v1/private/store` 403 ×4, `billing /api/v1/entitlement/private/snapshot` 403 ×3 and 404 ×21. All from the
  advice (`AccessDeniedException`), so the tokens were valid and the permission check refused them.
- **Cause.** The session pool logs in four seeded accounts — org admin, store admin, super admin, store moderator —
  and the `store-reads` journey ("the org admin's store screens": org-level tenancy and billing reads) took
  whichever session the VU number pointed at. A store admin or moderator is refused on those endpoints by design,
  so a quarter to a half of the admin reads were 403 before they started. The 404s on the entitlement snapshot
  are the fixture store having no subscription yet.
- **Fix.** `k6/lib/core/session.js` gained `sessionWithRole(sessions, vuId, roles)`; `admin/store-reads.js`,
  `mixed/production-mix.js` and `smoke.js` ask for an `ORG_ADMIN` session for that journey (falling back to the
  pool when none logged in, as on a target without that account).
- **Verified.** `admin/store-reads` at the smoke profile after the change: 12 admin requests, 0 failed, and no
  403 on tenancy, billing or merchant in the ten minutes around it (Auth → *401 / 403 by service*). The admin
  threshold now measures the platform.

## 6. `catalog → merchant` is the slowest service-to-service edge — fixed at the token endpoint

- **Seen.** p95 0.79 s on the `catalog → merchant` edge (client side) during the checkout run, versus 0.05 s on
  `catalog → content` and `catalog → billing`. The server side of merchant is fast (its p95 is under 50 ms).
- **Cause.** A trace of one slow call: the client span starts, nothing happens for ~640 ms, then merchant
  receives the request and answers in 58 ms. The gap is the OAuth2 client-credentials exchange with uaa that the
  service-to-service client performs when its token has expired (every 15 min per service; the token client is
  not traced, so it shows as a gap). uaa's `POST /oauth2/token` took 0.51–0.65 s, with two ~250 ms pauses after
  two reads of the registered client: uaa's grace-aware client authentication
  (`GraceAwareClientSecretAuthenticationProvider` in `sso-core`) verified the live secret's bcrypt hash once to
  choose a registry view, then Spring's provider verified the same hash again. Two bcrypt rounds per token
  request, plus the same client read twice. This is also the `UNKNOWN` route on uaa in issue 10.
- **Fix.** The provider lets Spring's provider try the live registration first and consults the retired-secret
  history only after an `invalid_client` refusal. One hash per request on the common path; the grace window
  behaves as before (unit test asserts one encoder call and no history read for the live secret).
- **Verified.** uaa restarted: `POST /oauth2/token` 0.26–0.32 s (was 0.51–0.65 s) on both the accepted and the
  refused path. The remaining cost is one bcrypt at strength 10 on this machine. If the edge still matters, the
  levers left are the s2s token lifetime (900 s in the uaa seed, `settings.token.access-token-time-to-live`) and
  tracing the token client so the exchange stops showing as a gap.

## 7. `landing-ui → spg` shows 4–6 % failed calls — fixed

- **Seen.** `cvhome:s2s_failed:ratio_rate5m{client="landing-ui",server="spg"}` 4.2 % during the checkout run and
  5.6 % in the mix; over 3 h, 760 of 19,059 calls flagged failed, all on landing-ui's un-normalised client spans
  named `GET`.
- **Cause.** Their `url.full` (Traces → `{resource.service.name="landing-ui" && span:kind=client &&
  status=error}`): every one a 404 from `catalog /api/v1/products/groups/FEATURED_ITEMS` (the home page's
  strips) or `catalog /api/v1/products/{id}/relationship` (the product page's "you may also like"). The
  storefront treats both as optional and renders without them, but catalog answered 404 when the store had no
  such group, and a 4xx marks the client span as an error, so every home and product render of a store without
  those groups counted as a failed edge call.
- **Fix.** The storefront reads (`GET /products/groups/{code}` and `GET /products/{id}/relationship`) answer an
  empty, inactive strip (`active: false`, `products: []`) when the store has none; the console's private reads
  still 404 (`ProductGroupService.storefront(...)`, `related(...)`). Unit and integration tests updated; QA case
  GRP-07 in `store-pod/catalog/catalog-service/qa/catalog-qa.md` corrected.
- **Verified.** Both reads answer 200 with the empty strip on the live stack for a store without the groups; the
  storefront renders as before. Expect the edge's failed ratio to sit at 0 under the browse mix; anything left is
  a real failure worth a look.

## 8. The knee was not found

- **Seen.** The breakpoint ramp reached its ceiling (150 iterations/s = 300 k6 req/s = 552 req/s on the
  application) with every threshold intact: product p95 9 ms, availability 4 ms, pool 20 %, threads under 1 %,
  system CPU 29 %, 0 failed, 0 dropped iterations.
- **Known.** The API tier has more than five times the headroom of that ramp on this machine. The capacity number
  the SLOs should be set against is still unknown, and so is the resource that gives out first.
- **Still to measure.** `make storefront-breakpoint PROFILE=breakpoint MAX_RPS=600 RAMP=6m` with pre-allocated VUs
  raised accordingly (watch *Dropped iterations*: above 0 the generator, not the app, is the limit), then the same
  for `shopper/cart` and `shopper/inventory-contention`. Read the knee on Bottlenecks → *Traffic vs p95* and the
  saturation strip on Load test vs app.

## 9. 409s on category attach under concurrent edits

- **Seen.** 20 × 409 on `POST /api/v1/private/product/{productId}/category/{categoryId}` (catalog) in the mix, from
  the `catalog-management` scenario's concurrent edits; also 21 × 409 at the gateway (`uri=UNKNOWN`, the relay of the
  same), 1 on tenancy signup, 1 on uaa users.
- **Known.** Optimistic locking doing its job under deliberately concurrent edits; the suite expects them. The rate
  is worth watching only if it rises without a concurrency change.
- **Where.** Service RED (catalog) → *409* stat and *Failed routes*.

## 10. Login and redirect paths on uaa and the gateway

- **Seen.** uaa p95: `UNKNOWN` 0.59 s, `REDIRECTION` 0.38 s (the authorization-code hops); gateway `REDIRECTION`
  0.53 s. Every API route on both is under 50 ms.
- **Known.** The `UNKNOWN` route on uaa is the token endpoint (Spring Authorization Server serves it from a
  filter, so Micrometer has no route for it); its cost and fix are issue 6. The two-hop sign-in (gateway → uaa →
  gateway) is expected to be the slowest thing on these services; 0.5 s is within the 2.5 s login SLO. The
  `platform/gateway-login` script was not run today.
- **Still to measure.** `make platform-gateway-login PROFILE=load` while watching Auth → *Token endpoint p95* and
  *Gateway sign-in and session* (the in-memory session count is the capacity signal there).

## Not seen, worth stating

- No 5xx in any run. No database connection waited (pending 0), no timeouts. No GC pressure (pause share 0,
  heap-after-GC flat). No request-thread saturation (busy under 1 %). Outbox backlog 0, no FAILED records.

## What is still open

1. Re-run browse and the mix against a **built** storefront; without that, issue 1 is a measurement of the dev server.
2. Run the breakpoint ramp to at least 600 req/s (issue 8) so the SLOs have a capacity number behind them.
3. Re-run `mixed-production-mix` at `PROFILE=load` to record the new baseline: statements per request on catalog
   and checkout, the `catalog → merchant` edge p95, the `landing-ui → spg` failed ratio, and the admin threshold —
   all expected to move with the fixes above.

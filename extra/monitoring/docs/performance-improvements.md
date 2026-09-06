# Performance: what the first load tests found

The issues the first measured runs surfaced, in enough detail to plan fixes later. Each entry says what was seen,
how it was measured, where to look at it, what is known about the cause, and what still has to be measured before
deciding. No fixes are proposed here on purpose; that is the next step.

The runs (2026-09-06, local stack: one JVM per service on one machine, PostgreSQL 15, Hikari pool 5 per service,
storefront on `next dev`): smoke, `storefront-browse` 30 VUs, `shopper-guest-checkout` 40/min, `mixed-production-mix`,
`storefront-breakpoint` ramp to 150 iterations/s. Numbers and test ids: `../../../load-testing/docs/baseline.md` (the
`load-testing` repo). Dashboards named below are the provisioned Grafana ones; their queries are in
[dashboards.md](dashboards.md), the KPIs in [kpis.md](kpis.md).

## Summary

| # | issue | severity | where |
|---|---|---|---|
| 1 | Storefront pages take 4.5–5 s at p95; the APIs behind them answer in milliseconds | high for the user, but measured on the dev server | landing-ui |
| 2 | catalog runs ~21 SQL statements per request; the product page loads options and variants row by row | high, grows with the catalogue | catalog |
| 3 | 132 k untyped `SELECT` statements in 3 h on catalog — 4.6 per request, source unknown | medium, needs identification | catalog / JDBC |
| 4 | checkout runs ~11 statements per request; every order costs two sequencer round-trips | medium | checkout |
| 5 | Admin traffic in the production mix fails 2 % with 403s | medium, likely a test-fixture problem | load-testing sessions, tenancy/billing/merchant |
| 6 | `catalog → merchant` is the slowest service-to-service edge (p95 0.79 s) | medium | catalog, merchant |
| 7 | `landing-ui → spg` shows 4–6 % failed calls during runs | low until understood | landing-ui |
| 8 | No knee found: the API tier's capacity is unknown above 552 req/s | measurement gap | load-testing |
| 9 | 409s on category attach under concurrent catalogue edits | expected behaviour, keep an eye on the rate | catalog |
| 10 | Login and redirect paths are the slowest routes on uaa and the gateway | low | uaa, gateway |

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

## 2. catalog: ~21 SQL statements per request (N+1 on the product page)

- **Seen.** `cvhome:sql_per_request:ratio5m` for catalog was 21.3 during the browse run (checkout 11, every other
  service about 7). Over the 3 h of runs catalog served 28,560 `GET /api/v2/product/name/{friendlyUrl}` requests
  and ran:

  | statement family | count | per product request |
  |---|---|---|
  | `SELECT cvhome` (no table parsed — see issue 3) | 132,024 | 4.6 |
  | `SELECT catalog.product_option_value_description` | 61,903 | 2.2 |
  | `SELECT catalog.product_variant` | 35,921 | 1.3 |
  | `SELECT catalog.category_description` | 32,806 | 1.1 |
  | `SELECT catalog.product_option_value` | 31,468 | 1.1 |
  | `SELECT catalog.product_option_assignment` | 28,575 | 1.0 |
  | `SELECT catalog.product_description`, `product_image`, `product`, `product_type`, `manufacturer` | 2,300–4,100 each | ≈ 0.1 |

  The product entity itself is read once per ~12 requests (cached), but its options, option values and their
  descriptions, and variants are read per request, per row: the classic one-query-per-child pattern.
- **Where.** Database & SQL → *Statements per request* (catalog), *Statements / s by operation and table*, *Top
  statements*; *N+1 suspects* lists traces with more than 20 statements — open one and the repeated
  `SELECT catalog.product_option_value_description` spans are visible under the product request.
- **Why it matters.** It did not hurt today (each statement is 2–5 ms, the pool peaked at 40 %), but the count
  scales with options × values × languages per product and with the pool it shares; it is the first thing that
  will saturate the database pool when the catalogue or the traffic grows, and it is invisible in latency until
  it does.
- **Still to measure.** One trace per route (`product/name`, `products/search`, `products/groups`, `category-hierarchy`)
  with the statement list, to attribute the counts to the populators; and the same ratio with `spring.jpa.show-sql`
  or the JDBC spans' `db.statement` to see the exact queries and their parameters (are descriptions fetched per
  language, per value?).

## 3. 132 k untyped `SELECT` statements on catalog (4.6 per request)

- **Seen.** The most frequent JDBC span on catalog is `SELECT cvhome` — a SELECT whose table the instrumentation
  could not name (the span name falls back to the database name). 4.6 per product request; also 579 on checkout
  in the same period (one per order).
- **Known.** Candidates: sequence reads (`select nextval(...)`), `select 1`-style connection checks (Hikari uses
  `isValid()`, which produces no statement, unless `connection-test-query` is set), count queries over subqueries,
  or Hibernate's `select ... from (subquery)` for pagination. None of these is confirmed.
- **Where.** Database & SQL → *Top statements* → the row's *Traces with this statement* link → the span's
  `db.statement` attribute is the SQL text.
- **Still to measure.** Read `db.statement` on a sample of these spans (Tempo: `{resource.service.name="catalog" &&
  name="SELECT cvhome"}`) and group by text. If they are sequence or count queries, they belong to issue 2; if they
  are connection checks, they are configuration.

## 4. checkout: ~11 statements per request, two sequencer round-trips per order

- **Seen.** 11.2 statements per request during the checkout and mix runs. Per order (579 orders in 3 h):
  `UPDATE checkout.sm_sequencer` and `SELECT checkout.sm_sequencer` 2,154 each (3.7 per order), `customer_account`
  1.7, `sales_order_total` 1.4, `sales_order` 1.0, `sales_order_event` 1.0, `cart_line` 0.8.
- **Known.** Order numbers come from a table-based sequencer (`sm_sequencer`) read and updated in the order's
  transaction, several times per order. Under contention this row is a serialisation point: every order waits for
  the previous one's update. It did not show today (checkout p95 74 ms at 40 orders/min), but it is the kind of
  thing the `inventory-contention` script is built to find.
- **Where.** Database & SQL with `service=checkout` → *Top statements*; Traces → a checkout trace shows the
  sequencer statements in order.
- **Still to measure.** `shopper/inventory-contention` and `shopper/guest-checkout` at RATE=200+ while watching
  *Time a connection is held* on checkout and `pg_stat_activity` for lock waits on `sm_sequencer`.

## 5. Admin traffic fails 2 % with 403s in the production mix

- **Seen.** `http_req_failed{layer:admin}` 2 % (threshold 2 %) in `production-mix-load`: 403 on
  `tenancy:store-unique` (19), `tenancy:store`, `tenancy:store-info`, `billing:entitlement`, `merchant:store-private`
  (4 each). Server side: `tenancy /api/v1/store-manager/private/store/unique` 403 ×13, `merchant
  /api/v1/private/store` 403 ×4, `billing /api/v1/entitlement/private/snapshot` 403 ×3 and 404 ×21. All from the
  advice (`AccessDeniedException`), so the tokens were valid and the permission check refused them.
- **Known.** The mix's admin scenarios use a pool of seller sessions and the `k6-local` fixture store; the 403s say a
  session addressed a store its principal has no permission on (a session of org1 against the k6 org's store, or
  the reverse). The 404s on the entitlement snapshot are the fixture store having no subscription yet. Neither is a
  platform defect, but the admin threshold cannot be trusted until the sessions are fixed, and a real permission
  regression would hide behind it.
- **Where.** Auth → *401 / 403 by service*, *Rejections by reason* (`AccessDeniedException`, `source=advice`);
  Service RED for tenancy → *403* stat and *Failed routes*.
- **Still to measure.** Which session (which principal) produced each 403 — the `SESSION_POOL` size versus the
  stores addressed in `k6/config/mix.js`; then re-run with a single principal that owns the fixture store.

## 6. `catalog → merchant` is the slowest service-to-service edge

- **Seen.** p95 0.79 s on the `catalog → merchant` edge (client side) during the checkout run, versus 0.05 s on
  `catalog → content` and `catalog → billing`. The server side of merchant is fast (its p95 is under 50 ms).
- **Known.** catalog reads the merchant store through `CachedExternalMerchantStoreService` (the `STORE` cache,
  now metered: 28 hits, 4 misses in the first minutes after a restart). The edge latency is far above merchant's
  own time, so the time is between them: the s2s token exchange (client credentials with uaa), discovery, or the
  first call after a cache miss including the OAuth2 round-trip.
- **Where.** Service-to-Service → *Edges* table and *p95 by edge*; a trace from the row link shows the client
  span with the token request beside it (uaa `/oauth2/token`).
- **Still to measure.** Whether the slow calls coincide with cache misses (Bottlenecks → *Cache hit ratio*) and with
  uaa token requests in the same trace; the p95 on this edge over a longer, steadier run.

## 7. `landing-ui → spg` shows 4–6 % failed calls

- **Seen.** `cvhome:s2s_failed:ratio_rate5m{client="landing-ui",server="spg"}` 4.2 % during the checkout run and
  5.6 % in the mix; over 3 h, 760 of 19,059 calls flagged failed. landing-ui's own client spans in error are all
  named `GET` (the un-normalised, non-fetch client spans, 759) — not the `fetch GET /catalog/...` API calls, which
  show no errors.
- **Known.** The failing spans are the storefront's generic outgoing HTTP calls, not its API fetches. Candidates:
  image and asset requests to MinIO through spg that 404 (the suite notes "product photos 404 locally"), or requests
  k6 aborted on page close. This is not an API failure and the SLI excludes it, but it dirties the service graph.
- **Where.** Edge → *Upstream fetch errors / s*; Traces → `{resource.service.name="landing-ui" && span:kind=client
  && status=error}` → the span's `url.full` and `http.response.status_code`.
- **Still to measure.** The `url.full` and status of a sample of those spans. If they are asset 404s, the
  Caddyfile or the seed should serve them; if they are aborts, they should be filtered from the graph.

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
- **Known.** The two-hop sign-in (gateway → uaa → gateway) is expected to be the slowest thing on these services;
  0.5 s is within the 2.5 s login SLO. The `platform/gateway-login` script was not run today.
- **Still to measure.** `make platform-gateway-login PROFILE=load` while watching Auth → *Token endpoint p95* and
  *Gateway sign-in and session* (the in-memory session count is the capacity signal there).

## Not seen, worth stating

- No 5xx in any run. No database connection waited (pending 0), no timeouts. No GC pressure (pause share 0,
  heap-after-GC flat). No request-thread saturation (busy under 1 %). Outbox backlog 0, no FAILED records.

## Before planning fixes

1. Re-run browse and the mix against a **built** storefront; without that, issue 1 is a measurement of the dev server.
2. Attribute issue 2 and issue 3 to exact queries (one trace per route, `db.statement` of the untyped selects).
3. Fix the mix's admin sessions (issue 5) so the admin threshold measures the platform.
4. Run the breakpoint ramp to at least 600 req/s (issue 8) so the SLOs have a capacity number behind them.

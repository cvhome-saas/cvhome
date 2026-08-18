# console-ui go-live — migration framework, and Module 3

## Context

`store-core/console-ui` is meant to replace `store-core/seller-ui` as the production seller
console. The new app already exists and renders: Angular 20 standalone + SSR, Tailwind v4 with a
three-theme token layer, Transloco (en/ar, RTL), its own `core/` tier, ~30 shared UI components,
and eight feature areas (`marketing`, `auth`, `first-run`, `dashboard`, `orders`,
`create-store`, `store-management`, `not-found`). It is served on port 8011 and already routed by
the gateway — `GatewayRouteLocatorImpl` sends `Host: console-ui.gateway.com` to `lb://console-ui`,
`configure-domain.sh` adds the hosts entry, and `run-lcl.sh:50` starts it. Nothing is missing
from the infrastructure.

**What is missing is the data.** Every data-bearing call in console-ui is a fixture. Each feature
owns a `*.api.service.ts` that returns `of(FIXTURE).pipe(delay(...))` over `src/app/mocks/*.fixture.ts`
(~1300 lines of fake data), `core/store-context/selected-store.service.ts` holds three hardcoded
"Acme" stores with an invented pod id, and `core/store-context/first-run-mock.ts` fakes the
zero-store account behind `?firstRun=1`. The app is unauthenticated — `canAccessSecuredPages`
exists in `core/auth/` but `app.routes.ts` references it nowhere.

That mock seam was deliberate (`.agents/plans/for-those-three-pages-fuzzy-parrot.md` records the
decision: "Typed models + API interfaces + facades, mock-backed. No live HTTP yet"), and it is
exactly the seam this migration consumes. Going live means replacing those `*.api.service.ts`
bodies with real HTTP, module by module.

The endpoint knowledge for that already exists and does not need rediscovering.
`.agents/plans/seller-core-shared-lib.md` extracted every HTTP service, DTO, mapper and validator
out of seller-ui into a library at `seller-ui/projects/seller-core`, split across eleven
ng-packagr entry points (`seller-core/catalog`, `/orders`, `/stores`, `/subscriptions`,
`/signup`, `/content`, `/customers`, `/payments`, `/orgs`, `/analytics`). Every path, DTO shape
and quirk of the backend is written down there, with doc comments naming the Java source. That
library is the **reference to port from**, not a dependency to link against.

The outcome: console-ui reaches functional parity with seller-ui, driven by real APIs, migrated
one module at a time — each module independently planned, implemented, tested against both UIs
side by side, and committed in its own phases. Design blocks with no backend behind them get a
`TODO` in code and an entry in `lessons.md`, never invented data. When the last module lands,
`seller-ui` and `seller-core` are deleted together and console-ui owns its own API tier outright.

## Decisions (settled with the user)

| Question | Decision |
|---|---|
| How console-ui gets its API layer | **Port `seller-core`'s services and models into console-ui, per module.** No tsconfig path mapping, no `dist/seller-core` build step, no cross-project dependency. seller-ui is left completely untouched. |
| i18n | console-ui's own **Transloco** throughout. seller-core's two `@ngx-translate/core` call sites are rewritten during the port, not bridged by a token. No `TRANSLATION_PORT`, no adapter, and `@ngx-translate/core` never enters console-ui's `package.json`. |
| console-ui's `core/` tier | **Authoritative.** Its `errors/`, `http/crud.service.ts`, `table/`, `auth/`, `platform/` are the ones that run. Ported services inject *these*, never a second copy. |
| Strictness | Ported code is hardened to console-ui's `strict: true` as part of the port. This is a feature, not a tax — see below. |
| Module order | Marketing/auth, then console shell and store context (both done), then the dashboard. |

### Why the copy is cheaper than the link, here

Three facts made linking expensive and the copy nearly free:

1. **console-ui already has the whole infrastructure tier.** `core/errors/*` (RFC-7807 parser,
   `ApiError`, `apiErrorInterceptor`, `ApiErrorService`, `form-error.utils`),
   `core/http/crud.service.ts`, `core/http/request-context.ts`, `core/table/table.types.ts`,
   `core/platform/browser-storage.ts` are already present, already wired in `app.config.ts`, and
   already Transloco-native. Only the **domain** services and models are missing, and those are
   thin — a service is typically 5–15 one-line `crudService.get/post(...)` methods over a
   hand-written DTO file.
2. **The i18n stacks genuinely differ.** seller-core is built on `@ngx-translate/core` v17
   (`ApiErrorService`, `ConfigService`); console-ui is on `@jsverse/transloco` v8 with a different
   locale set (en/ar vs en/fr/ar/es/ru). Linking meant shipping a port token and, for anything
   depending on the primary entry point, keeping ngx-translate resolvable in console-ui. Copying
   deletes that problem instead of managing it.
3. **The strictness gap.** `seller-ui/tsconfig.json` sets `strictNullChecks: false` and
   `noImplicitAny: false`, and `projects/seller-core/tsconfig.lib.json` inherits them.
   Linking against the built `.d.ts` would have imported signatures that *overstate*
   non-nullability — `SelectedStoreService.getStore(id)` is typed `ManagerStore` but genuinely
   returns `undefined`, the exact latent bug `.agents/plans/seller-core-shared-lib.md` already
   flagged and deliberately deferred. Porting under `strict: true` forces each of those to be
   confronted at the moment the code is read, in a diff small enough to review.

The cost the copy accepts, stated plainly: until seller-ui is retired, the two apps hold two
copies of each domain service, and a backend contract change must be applied twice. That window
is the migration itself, and each module closes a slice of it.

---

## The migration framework

Everything in this section is the framework, and it governs every module. **Module 1 is done and
shipped** (see below); **Module 2 is the plan in this document.** Later modules are named, not
designed — each gets its own planning phase when requested, per the requirements doc's constraint
against one large plan.

### Per-module lifecycle

Each module runs three phases, **one commit each**:

1. **Planning** — a plan file in `.agents/plans/console-ui-<module>.md`. Commit: `plan(console-ui): <module>`
2. **Implementation** — real APIs, TODOs for gaps, `lessons.md` entries. Commit: `feat(console-ui): <module>`
3. **Testing** — Chrome, old vs new side by side, fixes. Commit: `fix(console-ui): <module> after QA`

`lessons.md` updates ride in the implementation commit unless a module's gaps are large enough to
stand alone.

### What a module plan must cover

Fixed template, so each plan is comparable to the last:

1. **seller-ui functionality** — routes, components, workflows (start from `.agents/plans/seller-ui-feature-inventory.md`, which already maps the whole app section by section).
2. **API surface to port** — which `seller-core` entry point, which services, which endpoints, which DTOs.
3. **console design** — which `console-template/*.dc.html` page(s), which blocks.
4. **Mapping table** — old capability → new location, one row each. Anything with no row is a deliberate removal and must say so.
5. **New components** — what `shared/ui/` is missing.
6. **Backend gaps** — design blocks with no API. TODO + `lessons.md` entry.
7. **Testing** — the specific comparisons to run in the two tabs.
8. **Scope + commits.**

### Porting API code from seller-core — the standing convention

This is the rule every module follows, so it is written once here.

**Where ported code lands.** A new top-level tier `src/app/api/`, mirroring seller-core's entry
points one directory per bounded context:

```
src/app/api/
  billing/     subscription.service.ts        <- from seller-core/subscriptions
  signup/      sign-up.service.ts             <- from seller-core/signup
  stores/      manager-store.service.ts, pod.service.ts, dns-check.service.ts
  catalog/     product|category|brand|type|product-group services
  orders/  content/  customers/  payments/  orgs/  analytics/
```

Add a `@api/*` path alias in `tsconfig.json` next to the existing ones. Wire DTOs go in the
existing `src/app/models/` (already documented as "wire DTOs, one file per bounded context") —
`models/billing.ts`, `models/signup.ts`, and so on. Dependency direction becomes
`features → layouts → shared → api → core → models`; extend the eslint rule that enforces it.

**How a feature reaches it.** Unchanged from console-ui's existing architecture: the facade calls
the feature's `*.api.service.ts`, which now delegates to `@api/*` and does the view-shaping
(mapping wire DTOs to `@models/*` view models). The `*.api.service.ts` files are the seam this
whole migration turns over; keeping them means facades, components and specs do not move.

**Port checklist**, applied per file:

- Rewrite `import {CrudService} from 'seller-core'` → `import {CrudService} from '@core/http/crud.service'`;
  same for `PageT`/`SpringPage`/`PageRequest` → `@core/table/table.types`, `ApiError` →
  `@core/errors/api-error`, `BrowserStorage` → `@core/platform/browser-storage`.
- Rewrite any `TranslateService` usage to `TranslocoService` (`.instant(k, p)` →
  `.translate(k, p)`). This affects `ConfigService` when the store-settings module ports it; the
  error stack needs nothing, since console-ui's `ApiErrorService` is already Transloco-native.
- Harden to `strict: true`. Do not paper over with `!` or `as`. Where seller-core says
  `getStore(id): ManagerStore` and can return `undefined`, the ported signature says
  `ManagerStore | undefined` and the caller narrows. Every such correction is a real bug found —
  note it in the module's plan under a **Deviations** heading.
- Keep the doc comments. They name the Java DTO each interface mirrors and record why several
  decisions were made; they are the most valuable thing in the library and cost nothing to carry.
- Add a provenance line at the top of each ported file:
  `/** Ported from seller-ui/projects/seller-core/<path>. */`
  so the two copies can be diffed while both exist, and so the line can be deleted wholesale when
  seller-ui is retired.
- Bring the spec across too when one exists, adapted to console-ui's `@testing/` harness.
- Port **only what the module needs.** Do not pre-port the whole entry point.

**seller-ui is not modified by any module** except the final retirement one.

### Reading the template

`console-template/*.dc.html` are **Claude Design Canvas files, not portable HTML** — a React
runtime (`support.js`) with `<sc-if>` / `<sc-for>` / `{{ }}` and a trailing
`class Component extends DCLogic`. Read them for structure, blocks and copy; never lift markup.
They also have **no toast, modal, drawer, confirm, loading, skeleton, or error state anywhere**,
and **no responsive layout** (fixed 1440–1760px, `Sign In` is `min-width:1100px`). Those are
console-ui's to design — the shared components for them mostly exist already
(`busy-overlay`, `toast`, `notice-bar`, `page-header`).

Where the template and seller-ui disagree, seller-ui defines *what the feature does* and the
template defines *how it looks*. A capability seller-ui has and the template omits is still in
scope unless the module plan explicitly records the removal.

### lessons.md

New file `store-core/console-ui/lessons.md`. Append-only, newest module last. One entry per gap:

```markdown
## <Module> — <capability>

- **Screen:** console-ui route + the `console-template` page it comes from
- **What the UI needs:** the interaction, in one or two sentences
- **What is missing:** the endpoint or service that does not exist
- **Why it is required:** what the seller cannot do without it
- **Expected contract:** method, path, request/response shape as far as it can be determined
- **Placeholder:** the `TODO(lessons.md):` marker left in code
```

Precedent for the depth worth reaching:
`console-template/Content Management Service - Backend Requirements.md`, a full spec for a
service that does not exist yet. Anything that large graduates out of `lessons.md` into its own
requirements doc, with `lessons.md` linking to it.

### TODO convention

Every unbacked block gets, at the call site:

```ts
// TODO(lessons.md): <capability> — no backend endpoint. See lessons.md "<Module> — <capability>".
```

and in the UI either omits the block or renders it disabled with an honest label. **Never a
fixture standing in for a real answer.** `orders.ts` already sets the precedent for unimplemented
actions — `ToastService.info('… is not available yet.')`.

### Migration order (named only — not planned here)

`1` marketing + auth → `2` console shell & store context → `3` dashboard → `4` orders →
`5` store management → `6` catalogue → `7` payments → `8` content → `9` users & profile →
`10` customers → `11` subscription & usage → `12` org & pod management (platform admin) →
`13` retire seller-ui.

There is no Module 0. With the copy approach nothing needs wiring up front — console-ui's
`CrudService`, interceptor, error stack and config token are already provided in `app.config.ts`
and merely unused. The `@api/*` alias, the `src/app/api/` directory and `lessons.md` are created
by Module 1 as its first files.

Rationale for the front of the queue: `2` unblocks every console route (nothing can be scoped to
a store until the real store list loads, replacing the hardcoded `STORES` array and
`FirstRunMock`), and `3`–`5` are the modules whose console-ui shells are already built against
fixtures, so they are pure api-service swaps.

---
## Module 1 — Marketing / landing + auth — **done**

Shipped in three commits: `plan(console-ui)…`, `feat(console-ui): marketing and auth on real APIs`,
`fix(console-ui): marketing and auth after QA against the live stack`.

What it established, which Module 2 builds on:

- `src/app/api/` exists as the ported HTTP tier, with the `@api/*` alias and the eslint rule that
  keeps `core/`, `shared/` and `models/` out of it.
- The port-by-copy convention above is proven: `models/billing.ts`, `models/signup.ts`,
  `api/billing/subscription.service.ts`, `api/signup/sign-up.service.ts`.
- `lessons.md` exists with twelve entries.
- QA against the live stack found four defects, three of them in claims the UI was making that the
  backend did not support. **That is the pattern to expect**: the expensive findings in this
  migration are not wiring errors, they are places where the design asserts something no service
  can answer.

---
## Module 2 — Console shell and store context — **done**

Shipped in three commits. What it established, which Module 3 depends on:

- **The console is authenticated.** `canAccessSecuredPages` gates every `ConsoleShell` route.
- **`?store=&pod=` is real.** `SelectedStoreService` reads `POST /store-manager/list`; a `consoleContext`
  guard resolves it before any console route activates, because the request context reads the list
  synchronously. `FirstRunMock` and the hardcoded stores are gone. **Every endpoint Module 3 calls is
  store-scoped, and this is what scopes it.**
- `api/tenancy/`, `api/pod-registry/` joined `api/billing/` and `api/signup/` in the ported tier.
- QA against the live stack found that `user-account/current` 500s for every caller, that `AuthUser` had
  been mistyped since it was written, and two regressions of my own. `lessons.md` reached 22 entries.

---

## Module 3 — Dashboard

### Why this one is mostly a subtraction

The console's dashboard mockup is the most data-hungry screen in the app: four KPI tiles, a
three-row attention queue, a new-vs-returning donut, an order-status chart and a top-products list.
Behind it there are **three endpoints**, all on checkout, all returning the same three-column shape.

That mismatch — not the wiring — is the work. seller-ui's client dashboard shows exactly three charts
because three is what exists; the new design asks for eleven figures. This module ships the three, adds
the two more that turn out to be derivable, and is explicit on screen about the rest.

### seller-ui today

`/pages` renders one of two dashboards by role. The merchant one
(`src/app/pages/home/`) has a from-date/to-date range driving three ECharts panels: order status
breakdown, customer countries, top selling products. All three come from
`seller-core/analytics`'s `StatisticApiService`.

Worth copying: `orders-statistic.component.ts:30` derives its series from the response
(`[...new Set(data.entries.map(it => it.name))]`) rather than hardcoding a status list. That is the
right instinct and this module keeps it — see the status-label note below.

### API surface to port

All three statistics take `POST` with a `StatisticRange {fromDate, toDate}` body and answer
`{entries: [{date, name, value}]}` (`commons/domain/StatisticEntry`). Store scoping rides on the query
string, which Module 2 made real.

| From | To | Endpoint | What it actually returns |
|---|---|---|---|
| `seller-core/analytics/.../statistic.api.service.ts` | `api/analytics/statistic.service.ts` | `POST /spg/checkout/api/v2/private/order-statistic` | `(day, orderStatus, count)` grouped by both |
| same | same | `POST /spg/checkout/api/v2/private/customer-statistic` | `(null, billing.country, count)` — **orders by country**, despite the name |
| same | same | `POST /spg/checkout/api/v2/private/product-statistic` | `(null, sku, count)` — **order lines per SKU**, not units or money |
| `seller-core/payments/.../payment.service.ts` | `api/payment/payment.service.ts` | `GET /spg/payment/api/v1/private/payment/transactions` | paged `ReadableList`; only `totalElements` is read here |

**Port only the read half of `PaymentService`.** Approve and reject belong to the payments module.

**Do not port the three platform-admin statistics** (`store-statistic`, `org-statistic`,
`subscription-statistic` on tenancy). They feed seller-ui's *admin* dashboard, which is Module 12.

### What each panel gets

| Panel | Backing | Verdict |
|---|---|---|
| Order status breakdown | `order-statistic`, summed over the range per status | **real** |
| Orders by customer country (donut) | `customer-statistic` | **real** — retitled, see below |
| Top selling products | `product-statistic` | **real** — SKU and order count, see below |
| KPI: Orders | sum of `order-statistic` | **derived** |
| KPI: Orders delta | a second `order-statistic` over the preceding window of equal length | **derived** |
| KPI: Pending payments | `transactions?status=WAITING_VERIFICATION&count=1` → `totalElements` | **derived** |
| Attention: payment approvals | the same count | **derived** |
| Attention: awaiting fulfilment | `order-statistic`, statuses before SHIPPED | **derived**, retitled |
| KPI: Revenue | — | **none** → rendered unavailable |
| KPI: Low stock items | — | **none** → rendered unavailable |
| Attention: low stock products | — | **none** → row removed |
| New vs returning customers | — | **none** → panel repurposed |

### Decisions (settled with the user)

| Question | Decision |
|---|---|
| Revenue and Low stock tiles | **Keep all four tiles**; render these two with an em dash and a "Not available yet" flag on a muted tone. The gap is visible on screen, not only in `lessons.md`. `KpiCard` already has the `flag` input for exactly this — no component change. |
| New-vs-returning donut | **Retitled** to "Orders by customer country" and sliced by the countries the endpoint returns, which is what seller-ui shows and what the query computes. |
| Top products | **SKU, counted in orders.** `ACME-HDPH-01 · 48 orders`, not `Wireless Headphones · 482 sales`. The fixture's "sales" implied units or money and it is neither. No catalog lookup. |

### Three things the port has to get right

**The dates are fixtures and must go.** `dashboard.facade.ts` hardcodes
`DEFAULT_RANGE = {from: new Date(2026, 6, 5), to: new Date(2026, 7, 4)}` and a `HEADING_DATE` of
`new Date(2026, 7, 4)`. Against real data these are simply wrong. The default becomes the last 30 days
ending today, and the heading's date becomes today.

**Status labels cannot be translated blindly.** The real `OrderStatus` enum is ten values — `CREATED,
PENDING_PAYMENT, CONFIRMED, PROCESSING, SHIPPED, DELIVERING, DELIVERED, COMPLETED, CANCELLED,
RETURNED` — and neither UI's five-value list (`ORDERED/PROCESSED/DELIVERED/REFUNDED/CANCELED`) matches
it. Follow seller-ui and derive the series from the response, then label each through a known-key map
with a humanized fallback: Transloco's `StrictMissingHandler` **throws** on an unknown key, so
`translate('dashboard.orderStatus.' + name)` on an unexpected status would crash the page. Same shape
as the entitlement labels in Module 1. Tones come from a stable map so a status keeps its colour
between renders, cycling the categorical palette for anything unmapped.

**The payment count must not be able to blank the dashboard.** `loadSnapshot` fans out five requests;
the three statistics are required, but a payments outage should cost the two payment figures and
nothing else. `catchError(() => of(null))` on that one leg, and the tile reports unavailable rather
than zero — reporting zero pending approvals when the service is down is the worst possible answer.

### Implementation

- **Port** into `src/app/api/analytics/statistic.service.ts` and `src/app/api/payment/payment.service.ts`,
  with `src/app/models/statistics.ts` and `src/app/models/payment.ts`, following the standing checklist.
  `StatisticsParams` loses its `store` field: the request context supplies it now.
- **`DashboardApi.loadSnapshot(range)`** becomes the assembly point — `forkJoin` of the five calls,
  reducing entries into the existing `DashboardSnapshot`. This keeps the facade, the page and every
  widget untouched, which is the whole point of the `*.api.service.ts` seam.
- **`models/dashboard.ts`** gains what the shapes now need: `Kpi.value` becomes `string | null`
  (null renders the em dash), `OrderStatus.labelKey` becomes a resolved `label`, and `Product` becomes
  `{sku, orders}`. `CustomerSplitSegment.labelKey` becomes `label` — a country code is data, not copy.
- **`dashboard.fixture.ts`** is deleted outright; nothing on this page is authored any more.
- Locale: add `dashboard.orderStatus.*` for the ten real statuses, `dashboard.kpi.unavailable`, the
  retitled donut and products headings, and remove the keys for the five invented statuses.

### Backend gaps → `lessons.md`

1. **No revenue anywhere.** Every statistic is a `count(...)`; nothing sums `order.total`. The single
   most prominent figure in the design has no source. Expected: a `revenue-statistic` returning
   `(day, currency, sum)` — note it must be per currency, since a store can take more than one.
2. **No stock levels.** `ProductCriteria` has no quantity or threshold field, so "low stock" cannot be
   asked for. Expected: a threshold filter on the product query, or a count endpoint.
3. **No new-vs-returning split.** Nothing records a customer's first order date.
4. **`customer-statistic` is misnamed** — it groups *orders* by billing country and counts orders, not
   customers. A store with one loyal customer in Germany reads the same as one with forty.
5. **`product-statistic` returns no product name** and counts order lines rather than units — a ten-unit
   order counts once. Expected: the product's name and localized title, plus `sum(quantity)`.
6. **No "stale order" signal.** The design's "past 24 hours without a status update" needs the last
   history timestamp; the statistic has only the purchase date, so the row is retitled to what can be
   computed.
7. **Counting requires fetching.** `totalElements` is only reachable by asking for a page of rows. Cheap
   at `count=1`, but a real counts endpoint would serve the attention queue and the sidebar badges
   (already logged in Module 2) at once.

### Testing

Both UIs, signed in as the same org admin, `ORG1-STORE1` open in each.

- The order-status chart shows the same statuses and totals as seller-ui's ECharts panel over the same
  from/to dates. This is the one direct numeric comparison available.
- Same for customer countries and top products — noting seller-ui prints the raw SKU too.
- Switch stores in the rail: every figure refetches and changes. Confirm each request carries the new
  `?store=`, which is what Module 2 built.
- Change the date range: five requests go out, and the previous-period call spans the correctly
  offset window.
- A store with no orders in range: the charts say so rather than rendering empty axes.
- Stop the payment pod (or point the status at a nonexistent value): the three charts still render and
  only the payment figures report unavailable.
- Revenue and Low stock read "Not available yet" in both locales and all three themes.

### Commits

1. `plan(console-ui): dashboard` — this document.
2. `feat(console-ui): dashboard on real statistics`.
3. `fix(console-ui): dashboard after QA`.

---

## Critical files

**seller-ui: not modified.** Read-only reference.

**New in console-ui:** `src/app/api/analytics/statistic.service.ts`,
`src/app/api/payment/payment.service.ts`, `src/app/models/statistics.ts`, `src/app/models/payment.ts`.

**Changed in console-ui:** `src/app/features/dashboard/services/dashboard.api.service.ts` (the
assembly point — most of the module's code), `.../facades/dashboard.facade.ts` (real dates, resolved
labels), `src/app/models/dashboard.ts`, `src/app/features/dashboard/dashboard.html` (retitled panels,
one attention row fewer), `src/locale/{en,ar}.json`, `lessons.md`.

**Deleted:** `src/app/mocks/dashboard.fixture.ts`.

**Reused, already present:** `src/app/api/tenancy/selected-store.service.ts` and
`selected-store-request-context.ts` (store scoping), `src/app/core/http/crud.service.ts`,
`src/app/shared/ui/{kpi-card,kpi-grid,action-list,donut-chart,bar-chart,ranked-list,date-range-picker}`
— every widget this page needs already exists and none of them changes.

## Verification

1. `cd store-core/console-ui && npm run build && npm run lint && npm test`;
   `git -C store-core/seller-ui status --porcelain` empty.
2. `grep -rn "dashboard.fixture" src` → no hits.
3. `grep -rn "new Date(2026" src` → no hits; the dashboard's dates are real.
4. The two-tab comparison above, driven through Chrome.
5. Network tab: five requests per range change, each carrying `?store=` and `?pod=`.
6. Every `TODO(lessons.md):` marker in the diff has a matching heading in `lessons.md`.

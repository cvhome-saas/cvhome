# console-ui go-live — migration framework, and Module 5

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
| Module order | Marketing/auth, console shell and store context, dashboard (all done), then orders. |

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
## Module 3 — Dashboard — **done**

Shipped in three commits. What it established, which Module 4 reuses:

- `api/analytics/statistic.service.ts` and `api/payment/payment.service.ts` are ported. **Module 4's KPI
  row reuses `orderStatistic` directly.**
- The pattern for a figure with no source: an em dash under a "Not available yet" flag, on a muted
  tone, rather than a zero or a hidden tile.
- The pattern for humanizing the server's `OrderStatus` enum instead of translating it — Transloco is
  configured to **throw** on a missing key, so a status the console has not seen would take the page
  down. Module 4 needs the same treatment.
- **A live blocker:** all three merchant statistics 500 for every caller
  (`java.util.Date is not assignable to java.time.Instant`). Logged in `lessons.md`, not fixed, at the
  user's direction. It matters here — see the KPI note below.

---

## Module 4 — Orders — **done**

Shipped across twelve commits (`feat(console-ui): order list on real orders` through
`docs(console-ui): make every lessons.md reference resolve`). `orders.fixture.ts` is gone and
`lessons.md` reached 45 entries. What it established, which Module 5 reuses:

- **The known-set-then-fallback i18n pattern, as reusable services.** `shared/i18n/status-label.ts`,
  `total-label.ts` and `money.ts` translate a server enum only when it is in a known set and humanize
  it otherwise, so a value the console has not seen cannot take the page down under Transloco's
  strict-missing handler. Module 5's provider and payment-type enums get the same treatment.
- **Locale-correct data, not just locale-correct words.** `TranslocoLocaleService` for money, numbers
  and dates — `Intl.NumberFormat(undefined, …)` resolves to the *browser's* locale and `DatePipe` is
  pinned to `LOCALE_ID`, so both were wrong in Arabic. And `unicode-bidi: plaintext` for Latin data
  (addresses, phone numbers, SKUs) inside an RTL page.
- **Route params are validated before they reach a facade.** `/orders/abc` used to reach the server
  as `orders/NaN` and come back a 500 that read as "load failed".
- The full-page review after shipping found a genuinely app-wide bug — `crypto.randomUUID` does not
  exist on insecure origins, so *every* toast in the app silently failed over plain HTTP.

### Scope

**The list and the detail screen together.** console-ui has only `/orders` today; the detail screen is
net-new and is the largest single page in the migration. `console-template/Order Details.dc.html` is the
design reference and carries most of the layout already.

### seller-ui today

`/pages/orders/order-list` — filters on customer name, email, phone and status, server-side paging,
columns Id / name / email / phone / date / status / total / Details.
`/pages/orders/order-details/:id` — the richest screen in the old app.

Its facade (`order-details.facade.ts`) calls six things. **Three of them do not exist:**
`updateOrder` → `PATCH /private/orders/{id}/customer`, `refundOrder` → `POST …/refund`,
`captureOrder` → `POST …/capture`. No such mappings exist anywhere in checkout. So seller-ui's
editable address panels and its Refund and Capture buttons have always 404'd. Its transactions dialog
is empty for the same reason, and seller-core says so in a comment: *"No backend endpoint populates
transactionListData anywhere."*

### API surface to port

`ReadableOrderList extends ReadableList<ReadableOrder>` — the list returns **whole orders**, so items,
totals, billing and customer are all present without a second call.

| From | To | Endpoint |
|---|---|---|
| `seller-core/orders/.../orders.service.ts` | `api/orders/orders.service.ts` | `GET /spg/checkout/api/v1/private/orders` — `name`, `id`, `status`, `phone`, `email`, plus `page`/`count` |
| same | same | `GET …/private/orders/{id}` |
| same | same | `GET …/private/orders/{id}/history`, `POST` the same path |
| same | same | `GET …/api/v1/country`, `GET …/api/v1/zones?code=` |
| `seller-core/orders/.../models/order.model.ts` | `models/checkout.ts` | `ReadableOrder`, `ReadableOrderProduct`, `ReadableBilling`, `ReadableDelivery`, `OrderTotal`, `ReadableOrderStatusHistory`, `PersistableOrderStatusHistory` |

**Do not port `updateOrder`, `refundOrder` or `captureOrder`** — the endpoints do not exist. Record
under **Deviations**, as with `ManagerStoreService.create()` in Module 2.

`count` is the page-size parameter platform-wide (`ServletWebConfig` sets
`setSizeParameterName("count")`), which is why `PageRequest` in `@core/table` uses it.

### Decisions (settled with the user)

| Question | Decision |
|---|---|
| Scope | **List and details together**, following `console-template`. |
| Invented list columns | **Dropped**, with `lessons.md` entries: the channel column and its filter (`ReadableOrder` has no channel), the card-brand line (`Visa •••• 4242` lives in the payment service, not on the order), and the "unfulfilled for 6h" badge (nothing records when a status last changed — the same gap the dashboard hit). |
| KPI counts | **One `order-statistic` call**, reusing Module 3's ported service, rather than several `?status=X&count=1` list calls. |

**Consequence of the KPI decision, and how it is handled.** `order-statistic` is the endpoint that
currently 500s. So that leg is made **optional** — `catchError(() => of(null))`, exactly as the
dashboard treats its payment count. The table, filters and paging come from `GET /private/orders`,
which works today, and the KPI row reports unavailable until checkout is fixed. Without this the
entire orders page would be dead on arrival because of a bug in a different endpoint.

**Average order value** joins Revenue and Low stock as permanently unavailable: it needs a revenue sum,
and nothing on the platform provides one.

### The status vocabulary, again

`OrderStatus` is ten values — `CREATED, PENDING_PAYMENT, CONFIRMED, PROCESSING, SHIPPED, DELIVERING,
DELIVERED, COMPLETED, CANCELLED, RETURNED`. console-ui's `models/orders.ts` currently declares five
invented ones (`Ordered | Processed | Delivered | Refunded | Canceled`) with tone maps and translation
keys for each. All of that is replaced by the real enum, mirrored in `models/checkout.ts` the way
`ProvisioningState` and `PaymentStatus` already are, with:

- labels **humanized, not translated** — the Module 3 rule;
- a stable tone map, reusing the one written for the dashboard so a status is the same colour in both;
- the tab strip populated from the real ten rather than the mockup's five. Ten tabs is more than the
  mockup shows and the strip scrolls horizontally; that is a deliberate deviation from the template,
  because grouping them would mean inventing groups the API cannot filter by (`status` takes one value).

### What the detail screen gets

`console-template/Order Details.dc.html` designs roughly twenty blocks. Six have data.

| Block | Backing |
|---|---|
| Header, status, placed date | **real** |
| Items table, unit price, line totals | **real** — `ReadableOrder.products` |
| Totals: subtotal, shipping, tax, grand total | **real** — `totals[]`, `total`, `tax`, `shipping` |
| Billing and delivery addresses | **real**, read-only — see below |
| Customer name, email, phone | **real** |
| Status history timeline | **real** — `GET …/history` |
| Add a status change with a comment | **real** — `POST …/history` |
| Flags: `customerAgreed`, `confirmedAddress`, `paymentStatus`, `reservationStatus` | **real** |
| Invoice document | **client-rendered** — see below |
| Refund, Capture | no endpoint → removed |
| Editing the addresses | no endpoint → **read-only** |
| Transactions list | no endpoint that links a transaction to an order → removed |
| Cancel order, Duplicate order, Create shipment | no endpoint → removed |
| Print picking list, Print packing slip | no endpoint → removed |
| Internal notes, attachments | no service → removed |
| Tracking, promised-by, ships-from, shipping method | not on the order → removed |
| Gateway fee | payment service, unlinked → removed |
| Customer lifetime spend, returns, "business account" | no customer analytics → removed |
| Address verification | nothing verifies addresses → removed |
| Email invoice to customer | no mail service → removed |

**The invoice is worth keeping and is genuinely buildable.** Every figure on it — line items, totals,
addresses, dates, order reference — is already in `ReadableOrder`. console-ui has
`core/export/pdf-export.service.ts` and a shared `ExportButton` (both live, used by the dashboard), so
Download and Print work with no backend at all. Only *emailing* it needs a service.

`GET /country` and `GET /zones?code=` are ported even though the addresses are read-only: the codes on
an order are ISO strings, and these are what turn `DE` into `Germany`.

### Implementation

- **Port** into `src/app/api/orders/orders.service.ts` with `src/app/models/checkout.ts`, following the
  standing checklist. Wire DTOs go in `models/checkout.ts`; `models/orders.ts` stays the page's view
  models, as `models/dashboard.ts` and `models/statistics.ts` are split today.
- **`orders.api.service.ts`** becomes the list's assembly point: `forkJoin` of the paged list and the
  optional `order-statistic`, mapping `ReadableOrder` → `OrderRow`. Filters go to the server —
  `status`, and the free-text search mapped to `name`/`email`/`phone`.
- **`models/orders.ts`** loses `OrderChannel`, `ORDER_CHANNEL_ICON`, `ORDER_CHANNEL_LABEL_KEY`,
  `ChannelFilter`, the five-value `OrderStatus` and its label keys, `paymentMeta` and `unfulfilledFor`.
- **New feature `features/order-details/`** — component, facade, `order-details.api.service.ts`,
  following the shape of `features/store-management` (a page with sections and a facade).
  Route `orders/:id` under `ConsoleShell`, guarded by `canAccessSecuredPages` and `consoleContext` +
  `requiresStore`, with `RenderMode.Client` in `app.routes.server.ts`.
- **`mocks/orders.fixture.ts` is deleted** — 549 lines, the largest fixture in the app.
- Locale: real status labels are not translated, so those keys go; add the detail screen's copy, the
  invoice's, and `orders.kpi.unavailable`.

### Backend gaps → `lessons.md`

1. **No refund and no capture.** seller-core calls `POST …/orders/{id}/refund` and `…/capture`; neither
   is mapped in checkout. Both buttons have always 404'd in seller-ui.
2. **Order addresses cannot be edited.** `PATCH …/orders/{id}/customer` does not exist either, so
   seller-ui's editable billing and delivery panels have never saved.
3. **No link from an order to its payment transactions.** The payment service keys on its own refs; the
   order carries a `paymentStatus` string and nothing else. seller-core's own comment records that the
   transactions dialog is never populated.
4. **No order channel.** Web/phone/marketplace is not recorded anywhere.
5. **No payment method on the order** — card brand and last four live in the payment service.
6. **No fulfilment or shipping model at all**: no shipment, no tracking number, no carrier, no
   promised-by date, no ships-from. The template designs all of them.
7. **No internal notes.** Distinct from status-history comments, which are customer-visible.
8. **No cancel and no duplicate.** Cancelling is only expressible as a status-history entry, which is a
   different thing from a cancellation that releases stock and refunds.
9. **No customer analytics** — lifetime spend, return rate, account type.
10. **No invoice service.** Rendering and printing are client-side, but numbering, storage and emailing
    are not.

### Testing

Both UIs, same org admin, `ORG1-STORE1` open in each.

- The list shows the same orders as seller-ui's `order-list` — same ids, customers, statuses, totals,
  same total count.
- Filter by each status: both narrow identically. Search by customer name, email and phone.
- Paging: `count` is the page-size parameter — confirm page 2 differs and the total is stable.
- Open an order in both: items, quantities, unit prices, subtotal, shipping, tax and grand total match
  line for line. This is the module's strongest comparison, and unlike Module 3 it does not depend on
  the broken statistic endpoint.
- Add a status change with a comment in console-ui; confirm it appears in seller-ui's history after a
  reload, and that the order's status moved.
- The KPI row reports unavailable while `order-statistic` 500s, and the table still works — the point
  of making that leg optional.
- Download and print the invoice; check totals and addresses against the order.
- An order with no delivery address, and one with a status the console has not seen, both render.
- Arabic and all three themes; 1440 / 900 / 420.

### Commits

1. `plan(console-ui): orders` — this document.
2. `feat(console-ui): order list on real orders`.
3. `feat(console-ui): order details`.
4. `fix(console-ui): orders after QA`.

Four rather than three: the list and the detail screen are independently reviewable, and the detail
screen is large enough that folding it into one commit would make the diff unreadable.

---

### Critical files (Module 4)

**seller-ui: not modified.** Read-only reference.

**New in console-ui:** `src/app/api/orders/orders.service.ts`, `src/app/models/checkout.ts`,
`src/app/features/order-details/{order-details.ts,.html,.css,facades/,services/}`.

**Changed in console-ui:** `src/app/features/orders/services/orders.api.service.ts` (the list's
assembly point), `.../facades/orders.facade.ts`, `.../orders.html` (columns and tabs),
`src/app/models/orders.ts`, `src/app/app.routes.ts`, `src/app/app.routes.server.ts`,
`src/locale/{en,ar}.json`, `lessons.md`.

**Deleted:** `src/app/mocks/orders.fixture.ts`.

**Reused, already present:** `src/app/api/analytics/statistic.service.ts` (the KPI counts),
`src/app/core/table/table.types.ts` (`PageT`, `PageRequest`, `count` not `size`),
`src/app/core/export/pdf-export.service.ts` and `shared/ui/export-button` (the invoice),
`shared/ui/{data-table,pagination,tab-switcher,page-header,panel,badge,busy-overlay,kpi-grid}`.

### Verification (Module 4)

1. `cd store-core/console-ui && npm run build && npm run lint && npm test`;
   `git -C store-core/seller-ui status --porcelain` empty, and `store-pod` untouched.
2. `grep -rn "orders.fixture" src` → no hits.
3. `grep -rn "'Ordered'\|paymentMeta\|unfulfilledFor\|OrderChannel" src` → no hits.
4. The two-tab comparison above, driven through Chrome — in particular the line-by-line totals check on
   one order, which is this module's strongest evidence.
5. Network tab: the list request carries `?store=`, `page`, `count` and the active filter.
6. Every `TODO(lessons.md):` marker in the diff has a matching heading in `lessons.md`.

---

## Module 5 — Store management

### Context

console-ui's store-management feature is fully built and entirely fake. `mocks/store-settings.fixture.ts`
feeds one `StoreSettings` document through `store-settings.api.service.ts`, which merges patches in
memory and answers after a simulated delay. Eight sections — branding, home page, domain, social links,
slider images, details, social login, payment configuration — render, validate and "save" without a
single HTTP call. This module makes all eight real.

Unlike Module 4, the shell does not need building: the page, the sub-nav, the eight section components,
the form service and its validators all exist and stay. What changes is the layer beneath them.

**The finding that shapes this module.** The design and the backend disagree far more than they did for
Orders, and in both directions:

- **Six of the twelve designed store-detail fields exist nowhere on the platform** — legal entity name,
  store slug, category, timezone, tax/VAT number, short description — as do the Published and
  Maintenance-mode toggles. Verified by grepping every `.java` file in `store-pod` and `store-core`:
  `legalName`, `taxNumber`, `maintenanceMode`, `timezone` and `shortDescription` return zero hits, and
  `slug` exists only for products, categories and content, never for a store.
- **Nine fields the store record really carries are not on the screen** — theme, colour theme,
  in-business-since, weight and dimension units, cache, require-login-for-checkout, supported languages
  and country (`MerchantStoreDetails` + `ReadableMerchantStore`).
- **The home page section has never worked, in either UI.** All three endpoints seller-core calls —
  `GET /private/content/any/{code}`, `PUT /private/content/{code}`, `POST /private/content` — do not
  exist. `ContentApi` maps `/private/content/pages`, `/private/content/boxes`, `/private/content/page`
  and `/private/content/box`, and nothing else. seller-ui's landing-page screen has always 404'd, the
  same class of finding as Orders' Refund and Capture buttons.
- **The API hands the browser live secrets in cleartext.** `PaymentConfigurationMapper` and
  `SocialLoginConfigMapper` both `decrypt()` before serialising, so `GET` on a payment configuration
  returns the Stripe secret key and webhook secret in plaintext, and `GET` on a social-login
  configuration returns the OAuth app secret. Encryption at rest buys nothing on these two endpoints.

### seller-ui today

`src/app/pages/store-management/`, one route per section, nav driven by `seller-core/stores`'
`sideMenuLinks`. Store creation and the stores list are **out of scope** — Module 2 already ported
`ManagerStoreService.list/create/nameExists`, and console-ui's `create-store` feature consumes them.

| Section | seller-ui route | seller-core facade |
|---|---|---|
| Branding | `store-branding/:code` | `store-branding-logo.facade.ts`, `store-branding-banner.facade.ts` |
| Details | `store/:code` (`StoreFormComponent`) | `store-form/facades/store-form.facade.ts` |
| Home page | `store-landing/:code` | `store-landing-page.facade.ts` — **404s today** |
| Domain | `store-domain/:code` | `store-domain.facade.ts`, `store-domain.validator.ts` |
| Social links | `store-social-links/:code` | `store-social-links.facade.ts` |
| Slider images | `store-slider-images/:code` | `store-slider-images.facade.ts` |
| Social login | `store-social-login/:code` | `store-social-login.facade.ts` |
| Payments | `store-payment-configuration/:code` | `store-payment-configuration.facade.ts` |

Seven of the eight work. The console-ui fixture's `notSupported` toasts on branding upload, slider
add/reorder/delete and delete-store are **not** backend gaps — they are places the fixture is more
restrictive than reality, and they get wired to real endpoints here.

### API surface to port

All from `seller-core/stores/src/lib/services/store.service.ts` and `dns-check.service.ts`, split by
concern into `src/app/api/stores/` — plus one new `api/content/` for the home page:

| New file | Endpoints |
|---|---|
| `api/stores/store-detail.service.ts` | `PUT /spg/merchant/api/v1/private/store?store=`, `DELETE …/private/store?store=`, `GET /tenancy/api/v1/store-manager/public/themes`, `GET …/public/color-themes` |
| `api/stores/store-branding.service.ts` | `POST`/`DELETE …/private/store/marketing/logo?store=`, `POST`/`DELETE …/private/store/marketing/banner?store=` (multipart) |
| `api/stores/store-domain.service.ts` | `GET /tenancy/api/v1/saas/public/saas-properties`, `GET /spg/merchant/api/v1/router/private/allocates?store=`, `GET /tenancy/api/v1/router/store-pod-by-store-id?store=`, `POST`/`DELETE …/router/private/allocate|remove?domain=`, plus `DnsCheckService`'s client-side DoH CNAME lookup |
| `api/stores/store-social-links.service.ts` | `GET /tenancy/api/v1/store-manager/public/social-links-providers`, `PUT /spg/merchant/api/v1/private/store/social-links?store=` |
| `api/stores/store-slider.service.ts` | `POST …/private/store/marketing/add-slider-image?store=`, `PUT …/private/store/marketing/slider-images?store=` |
| `api/stores/store-social-login.service.ts` | `GET /spg/cua/api/v1/private/social-login-config/supported-social-providers`, `GET`/`POST …/social-login-config?store=` |
| `api/stores/store-payment-config.service.ts` | `GET /spg/payment/api/v1/private/payment-configuration/supported-payment-types`, `GET`/`POST …/payment-configuration?store=`, `PUT`/`DELETE …/payment-configuration/{paymentType}?store=` |
| `api/content/content-box.service.ts` | `GET /spg/content/api/v1/private/content/boxes/{code}?store=`, `GET …/private/content/box/{code}/exists?store=`, `POST …/private/content/box?store=`, `PUT …/private/content/box/{id}?store=` |

`GET …/store-manager/private/store/{store}` is **already ported** as `ManagerStoreService.getStoreDetail`
(Module 2) — reuse it, do not re-port. `updateSocialNetworks` (`POST /v1/private/store/{store}/marketing`)
has no caller anywhere in seller-ui; **not ported**, dead code.

Wire DTOs go in `models/stores.ts` (new) and `models/content.ts` (new): `PersistableMerchantStore`,
`Address`, `Logo`, `Banner`, `SocialLink`, `SliderImage`, `ManagerStoreDomain`, `SaasProperties`,
`ReadableSocialLoginConfig`/`PersistableSocialLoginConfig`,
`ReadablePaymentConfiguration`/`PersistablePaymentConfiguration`, `ReadableContentBox`/
`PersistableContentBox`/`ContentDescription`. Drop seller-core's `LandingPageContent` entirely — it was
typed from what the frontend happened to send, against endpoints that do not exist.

**Two corrections to make while porting.** `PersistableMerchantStore` carries an
`[key: string]: unknown` index signature that defeats `strict` — remove it and type the fields.
`POST /social-login-config` takes a **`List<PersistableSocialLoginConfig>`**, an array, not a single
object; seller-core's signature obscures this.

### Decisions (settled with the user)

| Question | Decision |
|---|---|
| The six unbacked detail fields | **Keep them, all of them, marked.** Add the nine real fields the store record carries *and* keep the six designed-but-unbacked ones, each disabled with an honest label, a `TODO(lessons.md):` marker and a `lessons.md` entry. The section ends up the fullest picture of a store the console can draw, and every dead control says why it is dead. |
| Secrets returned in cleartext | **Reveal toggle.** The console shows the real `apiKey`, `secretKey`, `webhookSecret` and `appSecret` behind a click-to-reveal, masked by default, because that is genuinely what the API returns and an operator needs to verify a key. The `SecretHint` shape (`endsWith`/`lastRotated`) is dropped — `lastRotated` has no source anywhere. A `lessons.md` entry records that these endpoints should stop decrypting before serialising. |
| The home page section | **Built, on content boxes.** `ContentBox` is a fragment with a `code` and per-language `descriptions[]`; `ContentPage` adds `linkToMenu`, which means nothing for home-page copy. `ContentDescription` carries `name`, `description`, `metaDescription` and `keyWords` — a 1:1 fit for the four fields the design shows — and `GET …/box/{code}/exists` gives a real create-vs-update pre-flight instead of seller-ui's guess. |
| Storefront Builder | **Not built.** `console-template/Storefront Builder.dc.html` designs a drag-and-drop canvas of typed, reorderable page sections. A content box is a flat per-language document; nothing on the platform stores a section list. `lessons.md` entry, and the plain "Store home page" block from `Store Management.dc.html` is what gets built. |
| Scope | **One module, five commits**, grouped by how the sections cluster rather than by pod. |

### What stays unbacked → `lessons.md`

1. **No legal entity name, slug, category, timezone, tax number or short description on a store.** Six
   designed fields, zero backing. Disabled with a reason.
2. **No store publish state and no maintenance mode.** `Store visibility` in the design is two toggles
   with nothing behind them; provisioning state is not the same thing.
3. **No page builder.** A content box is flat; `Storefront Builder.dc.html` needs typed, ordered,
   reorderable sections.
4. **No storefront preview.** Nothing renders a storefront from draft content before publish. The
   design's "Preview storefront" button has no target.
5. **Payment and social-login GETs return secrets in cleartext.** Both mappers decrypt before
   serialising. Recorded as a backend issue with the mapper lines quoted, since the console cannot fix
   it and the reveal toggle makes it visible rather than hidden.
6. **A legacy plaintext credential row reads back as `null`.** Both mappers only set the field when the
   stored value is in encrypted form, so a row written before encryption silently reads empty — which
   the console cannot distinguish from "not configured".
7. **DNS verification is a browser-side check, not a platform one.** `DnsCheckService` queries Google's
   DoH endpoint directly; nothing server-side ever confirms the CNAME. Carried over as-is, because it is
   what seller-ui does, but it is not a guarantee.
8. **Slider images have no scheduling, no link target and no file metadata.** `ReadableSliderImage` is
   `{priority, name, url}`; the design's `LIVE`/`SCHEDULED` tag, click-through link and
   `1600×640 · 248 KB · JPG` line have no source.

### Implementation

- **Port** `api/stores/*` and `api/content/content-box.service.ts` per the table, following the standing
  checklist: rewrite imports to `@core/*`, `TranslateService` → `TranslocoService`, harden to `strict`,
  keep the doc comments, add the provenance line, bring specs across.
- **`models/store-settings.ts`** is reshaped to what the DTOs actually hold: `StoreDetails` gains the
  nine real fields and keeps the six unbacked ones behind an `unbacked` marker the section reads;
  `address` expands from one string to the five `Address` fields; `SecretHint` is deleted; `SliderSlide`
  loses `meta`, `link` and `state`; `StoreDomain` keeps its derived `status` and `record`, which come
  from the DoH check and `saasProperties` respectively.
- **`store-settings.api.service.ts`** becomes the assembly point: each section's read and save call the
  matching ported service and map wire DTO ↔ view model. `loadSettings()` is a `forkJoin` across the
  section GETs and the three supported-provider lists, each optional leg wrapped in `catchError` so one
  dead pod cannot blank the whole page — the pattern Module 3 set for `order-statistic`.
- **`store-settings.facade.ts`**: `saveSection(key, patch)` dispatches to the real service instead of
  merging in memory; `verifyDomain` calls the real DoH check.
- Delete the `notSupported` paths for branding upload/remove, slider add/reorder/delete and delete-store,
  and wire them to their endpoints. **Delete-store gets a typed confirmation dialog** — it is the one
  irreversible action on the page and currently a bare button.
- Provider and payment-type enums route through the `StatusLabel`-style known-set guard from Module 4,
  so a new `SocialProvider` or `PaymentType` from the server cannot throw under Transloco strict.
- `mocks/store-settings.fixture.ts` is deleted.

### New components

- **`shared/ui/secret-field`** — masked value, reveal toggle, copy button. Used by both the social-login
  and payment sections, four fields in total.
- **`shared/ui/confirm-dialog`** — a typed confirmation for delete-store. Check first whether the toast
  and busy-overlay work in `shared/ui/` already covers this; the template has no modal anywhere, so this
  is console-ui's to design either way.

### Testing

Both UIs, same org admin, `ORG1-STORE1` open in each.

- Each section reads the same values seller-ui shows, field for field.
- Each section writes: change one field, save, reload, confirm it persisted — and confirm seller-ui shows
  the change after a reload there too.
- Branding: upload a logo and a banner, remove each. These were `notSupported` toasts; confirm they now
  round-trip.
- Slider: add an image, reorder, delete; confirm the 8-slot capacity still holds.
- Domain: allocate a subdomain, run the DNS check on a custom domain, remove it.
- Home page: save copy in English, switch to Arabic, save different copy, reload — confirm both
  `descriptions[]` entries persist against the box's `code`, and that the first save created the box
  (`POST`) while the second updated it (`PUT` by numeric id).
- Secrets: confirm masked by default, revealed on click, and that a gateway with no credentials stored
  reads as "not configured" rather than blank.
- **Delete store: only against a throwaway store created for the test, never `ORG1-STORE1`.**
- The six unbacked detail fields render disabled with their reason, and do not submit.
- Arabic and all three themes; 1440 / 900 / 420.

### Commits

1. `plan(console-ui): store management` — this document.
2. `feat(console-ui): store details and branding on real APIs`.
3. `feat(console-ui): store domain, social links and slider images on real APIs`.
4. `feat(console-ui): store home page on content boxes`.
5. `feat(console-ui): social login and payment configuration on real APIs`.
6. `fix(console-ui): store management after QA`.

### Critical files

**seller-ui: not modified.** Read-only reference. **store-pod: not modified** — the secret-in-cleartext
finding is logged, not fixed, per the standing convention.

**New:** `src/app/api/stores/{store-detail,store-branding,store-domain,store-social-links,store-slider,store-social-login,store-payment-config}.service.ts`,
`src/app/api/content/content-box.service.ts`, `src/app/models/{stores,content}.ts`,
`src/app/shared/ui/secret-field/`, possibly `src/app/shared/ui/confirm-dialog/`.

**Changed:** `src/app/features/store-management/services/store-settings.api.service.ts` (the assembly
point), `.../services/store-settings-form.service.ts`, `.../facades/store-settings.facade.ts`,
`.../components/{details,branding,slider,social-login,payments}-section/`,
`src/app/models/store-settings.ts`, `src/locale/{en,ar}.json`, `lessons.md`.

**Deleted:** `src/app/mocks/store-settings.fixture.ts`.

**Reused:** `api/tenancy/manager-store.service.ts`'s `getStoreDetail`, `shared/i18n/status-label.ts`'s
known-set pattern, `core/http/crud.service.ts`, `core/errors/*`.

### Verification

1. `cd store-core/console-ui && npm run build && npm run lint && npm test`;
   `git -C store-core/seller-ui status --porcelain` and `git -C store-pod status --porcelain` both empty.
2. `grep -rn "store-settings.fixture" src` → no hits.
3. `grep -rn "SecretHint\|lastRotated" src` → no hits.
4. `grep -rn "notSupported" src/app/features/store-management` → only the genuinely unbacked actions
   (storefront preview) remain.
5. The two-tab comparison above, driven through Chrome — one field changed and reloaded per section.
6. Network tab: every save carries `?store=`, and the home-page save is a `POST …/content/box` the first
   time and a `PUT …/content/box/{id}` the second.
7. Every `TODO(lessons.md):` marker in the diff has a matching heading in `lessons.md`.

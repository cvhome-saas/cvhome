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
`5` store management → `6` catalogue → `7` payments → `8` users & profile →
`9` customers → `10` subscription & usage → `11` org & pod management (platform admin) →`12` content →
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

## Module 5 — Store management — **done**

Shipped across five commits (`feat(console-ui): store details and branding on real APIs` through
`feat(console-ui): social login and payment configuration on real APIs`).
`store-settings.fixture.ts` is gone and `lessons.md` reached 61 entries. What it established, which
Module 6 reuses:

- **The `optional*` `catchError` helpers as a named pattern.** `store-settings.api.service.ts`'s
  `optional`, `optionalList` and `optionalOne` wrap every leg of a wide `forkJoin` except the one
  that *is* the page — "a select that falls back to showing only the current value is still a
  working page, whereas a failed `forkJoin` is a blank one." Module 6's `loadCatalogue()` is built
  the same way, with the category hierarchy as the unwrapped leg.
- **Writes reload rather than echo.** The endpoints answer `void`, so `saveSection` re-reads; the
  page shows what the server normalised, not the operator's own input.
- **Three shared components Module 6 depends on:** `shared/ui/image-picker` (product images),
  `shared/ui/confirm-dialog` (every destructive action in the catalogue) and
  `shared/ui/secret-field`.
- The Module 3 dashboard-statistics outage was confirmed resolved against the live stack during
  this module's QA, and logged as the one *resolved* entry in `lessons.md`.

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

---

## Between Module 6 and Module 7 — the alignment pass — **done**

Shipped in nine commits (`chore(console-ui): the dead layer…` through
`docs(console-ui): the base architecture`). Not a module: no feature gained a capability and no
endpoint was newly called. Six modules had each been built against their own plan, and the shared
layer had grown by accretion — a control added when a module needed it, never retrofitted to the
modules before it.

**What it changed, which every later module now depends on:**

- **`store-core/console-ui/ARCHITECTURE.md` exists**, and it is the contract a module plan cites
  rather than restating. The tiers, the shape of a feature, the facade naming, the loading/empty/
  error trio, the shared control catalogue, the form rules, the page contract, the i18n and styling
  rules, and the QA checklist are all there.
- **A module plan's "New components" section must first check §4 of that document.** Five copies of
  the field vocabulary existed because each module wrote its own; the catalogue is what stops a
  sixth.
- **There are no raw `<input>`, `<select>`, `<textarea>` or checkbox elements left in a feature.**
  `app-form-field` with `app-text-field`, `app-textarea`, `app-select`, `app-number-field` and
  `app-checkbox` is how a form is built now.
- **`snapshot()`, `optionalOne`/`optionalList`, `uniqueAsync` and `formDirty`** replace the
  copy-pasted versions in seven facades, four api services and three form services.
- **`models/` is a leaf, one feature may not import another, and every branch declares its render
  mode** — all four enforced by eslint or a spec rather than by convention.
- **`npm run lint` now runs stylelint, a lessons.md citation check and an unused-key check** beside
  eslint. The citation check found two references that had already rotted.
- **The api tier went from 1 spec to 22.** 696 specs in total, up from 539.

Module 7 starts from a documented base rather than from Module 6's habits.

---

## Module 6 — Product catalogue — **done**

### Context

Module 6 is the catalogue — **products, categories, brands (manufacturers), product types and
product groups**. It is the largest merchant-facing area in seller-ui and the one console-ui has
never had at all: there is no `features/catalogue`, no `@api/catalog`, no `@models/catalog`. The
Commerce group in the nav rail (`src/app/mocks/console.fixture.ts`) carries an `inventory` item
with **no `route`**, which is this app's marker for "not built"; this module is what gives it one.

Unlike Modules 3–5 there is no fixture-backed shell to swap underneath. The feature and its API
tier are built together, which makes this the largest module of the migration so far — hence six
commits rather than three.

**Prerequisite:** Module 5 must land first. `shared/ui/image-picker/` arrives with it, and the
product Media step depends on it.

**The finding that shapes this module.** The catalog pod is the *richest* backend on the platform
and the design asks it for things it has never modelled. Verified by reading every controller under
`store-pod/catalog/catalog-service/src/main/java/com/asrevo/cvhome/catalog/api/`:

- **Everything `Inventory.dc.html` is actually about does not exist.** Zero hits across the pod for
  reorder, stockCount, warehouse, location, purchase order, stock movement, valuation or cost.
  `InventoryEntity` carries `region`/`regionVariant` as free text — not a location model, with no
  endpoint of its own. There is no cost field anywhere, only a sale `price`.
- **The reverse is also true, at scale.** `ProductVariantApi`, `ProductVariationApi`,
  `ProductVariantGroupApi`, `ProductAttributeOptionApi`, `ProductPropertySetApi`,
  `ProductInventoryApi` and `ProductPriceApi` are all fully mapped and have **no seller-core client
  and no UI**. seller-ui's menu has the Options group commented out.
- **Two seller-core calls have never worked.** `ProductService.addProductToCategory()` builds
  `.../category/${categoryId}}` — a literal trailing brace, so the URL never matches its mapping.
  `ProductImageService.createImage()` posts to `/private/product/{id}/images`; the pod only maps
  `/private/product/{id}/image`, singular. Same class of finding as Orders' Refund and Capture.
- **`ProductApi` maps `PATCH /api/v1/private/product/{id}` twice**, the two differing only by
  `produces` — a latent ambiguity, noted under Deviations.

### seller-ui today

`src/app/pages/catalogue`, mounted at `/pages/catalogue`, guarded by `CanAccessCatalogue`. Five
child areas. **There is no inventory page, no stock screen and no warehouse screen anywhere in
seller-ui** — `.agents/plans/seller-ui-feature-inventory.md` §10 uses "inventory" as a synonym for
"catalogue".

| Section | seller-ui route | seller-core service |
|---|---|---|
| Products | `products/products-list`, `products/create-product`, `products/product/:code` | `products/services/product.service.ts` |
| Product images | `product/:code/images` | `products/services/product-image.service.ts` |
| Related products | `product/:code/related` | `products/product-related/services/product-relationship.service.ts` |
| Categories | `categories/categories-list`, `create-category`, `category/:id` | `categories/services/category.service.ts` |
| Category hierarchy | `categories/categories-hierarchy` | same — `PUT …/category/{child}/move/{parent}` |
| Brands | `brands/brands-list`, `create-brand`, `brand/:id` | `brands/services/brand.service.ts`, `manufacture.service.ts` |
| Product groups | `products-groups/*` | `products-groups/services/product-groups.service.ts` |
| Product types | `types/types-list`, `create-type`, `type/:id` | `types/services/types.service.ts` |

The products list filters on `sku` and `available` (`categoryIds`/`manufacturerId` are wired in the
facade but never exposed in the template), pages server-side, and inline-edits quantity and price by
**double-click** plus an availability checkbox, all three via `PATCH /private/product/{id}`. The
product form is two columns (Definition + per-language SEO descriptions) with real router sub-tabs
for Images, Category and Related; `PRODUCT_FORM_TABS` also declares a `discount` tab with no route
and no component. The category hierarchy is a drag-and-drop `ngcx-tree` — the feature inventory
calls it "one of the few genuinely interactive screens; keep it, upgrade it."

### API surface to port

All from `seller-ui/projects/seller-core/catalog/src/lib`, into a new `src/app/api/catalog/`. Every
path is prefixed `/spg/catalog`. `?store=` is stamped by `CrudService` via `REQUEST_CONTEXT` —
**callers never pass it**.

| New file | Endpoints |
|---|---|
| `api/catalog/product.service.ts` | `GET /api/v2/private/base-products`, `GET /api/v2/private/tiny-products`, `GET`/`PUT /api/v2/private/product/{id}`, `POST /api/v2/private/product`, `PATCH /api/v1/private/product/{id}`, `DELETE /api/v1/private/product/{id}`, `GET /api/v1/private/product/unique?code=`, `POST`/`DELETE /api/v1/private/product/{productId}/category/{categoryId}` |
| `api/catalog/product-image.service.ts` | `POST /api/v1/private/product/{id}/image` (multipart, `?order=&defaultImage=`), `DELETE …/image/{imageId}`, `PATCH …/image/{imageId}?order=`, `GET /api/v1/product/{id}/images` |
| `api/catalog/product-relationship.service.ts` | `GET /api/v1/products/{id}/relationship`, `POST`/`DELETE /api/v1/private/products/{id}/relationship/{relatedId}` |
| `api/catalog/category.service.ts` | `GET`/`POST /api/v1/private/category`, `GET`/`PUT`/`DELETE …/category/{id}`, `PATCH …/category/{id}/visible`, `PUT …/category/{child}/move/{parent}`, `GET …/category/unique?code=`, `GET /api/v1/private/category-hierarchy`, `GET …/category/product/{productId}` |
| `api/catalog/manufacturer.service.ts` | `GET /api/v1/private/manufacturers`, `GET`/`PUT`/`DELETE …/manufacturer/{id}`, `POST …/manufacturer`, `GET …/manufacturer/unique?code=` |
| `api/catalog/product-type.service.ts` | `GET /api/v1/private/product/types`, `GET`/`PUT`/`DELETE …/product/type/{id}`, `POST …/product/type`, `GET …/product/type/unique?code=` |
| `api/catalog/product-group.service.ts` | `GET`/`POST /api/v1/private/products/groups`, `GET`/`DELETE …/groups/{code}`, `GET …/groups/unique?code=`, `POST`/`DELETE …/groups/{code}/product/{productId}` |

Wire DTOs → **`models/catalog.ts`** (new): `ReadableProduct`, `ReadableProductDefinition`,
`PersistableProductDefinition`, `LightPersistableProduct`, `ProductDescription`,
`ProductSpecification`, `ReadableImage`, `ReadableCategory`, `PersistableCategory`, `CategoryRef`,
`CategoryDescription`, `ReadableManufacturer`, `PersistableManufacturer`, `ReadableProductType`,
`PersistableProductType`, `ReadableProductGroup`, `PersistableProductGroup`, `ProductGroupItem`,
`EntityExists`, and `ProductQuery extends PageRequest` (`sku`, `productName`, `available`,
`categoryIds`, `manufacturerId`).

View models → **`models/products.ts`** (list rows, the form's view model, the step definitions, the
readiness checklist) and **`models/taxonomy.ts`** (`CategoryNode`, `BrandCard`, `TypeCard`,
`GroupRow`, the `/catalogue` tab set) — the `checkout.ts` / `orders.ts` split Module 4 established.

**Not ported, deliberately.** `ProductVariantApi`, `ProductVariationApi`, `ProductVariantGroupApi`,
`ProductAttributeOptionApi`, `ProductPropertySetApi`, `ProductInventoryApi`, `ProductPriceApi`,
`ExternalProductApi`, `ExternalProductReservationApi`. All mapped on the backend; none has a
seller-core client or a UI today, and the decision below is one product with no variants. This is a
**console gap, not a backend gap** — it belongs here, not in `lessons.md`. Also not ported:
`PATCH …/product/{id}?order=` (product ordering, commented out of seller-ui's own menu) and
`ProductImageService.createImage()` (no such mapping).

**Deviations.** Each is a real bug found by porting under `strict: true`, per the standing rule.

1. **`addProductToCategory` has never worked** — literal trailing brace. Fixed in the port; it is
   what makes the Organize step's category diffing possible.
2. **`createImage` targets a mapping that does not exist.** The port uses `POST …/{id}/image`,
   multipart, with `?order=` and `?defaultImage=`, and drops the dead method.
3. **`ProductApi` maps `PATCH …/product/{id}` twice.** The console calls the
   `LightPersistableProduct` form and does not port the `?order=` form.
4. **`ReadableProductDefinition.inventory` is a single flat object**, `{sku, price: string,
   quantity}` — not a list, and `price` reads back as a **string** while
   `PersistableProductDefinition.price` writes a **number**. Both typed honestly; the api service
   converts in one place.
5. **seller-core's catalog models are optional on every field.** Hardened: `id`, `sku`, `code` and
   `descriptions` are required where the server always sends them, and the mappers narrow rather
   than assert.
6. **`PersistableProductDefinition.categories` and `.properties` are `unknown[]`** — typed as
   `CategoryRef[]`, and dropped, respectively.

**Two further deviations, found during implementation** and not anticipated when this section was
written. Both are in `lessons.md`.

7. **"Move a category to the top level" is an undocumented `-1`.** `PUT …/category/{child}/move/{parent}`
   is the only re-parenting endpoint, and `PUT …/category/{id}` cannot clear a parent it is not
   given. `CategoryFacadeImpl.move` special-cases `parent == -1` into `addChild(null, category)` —
   the only way to detach a child. seller-ui never found it, because its tree only nests. Named as
   `ROOT_PARENT` in the port rather than left as a magic number.
8. **The private product list is stripped, so the list reads the public one.**
   `GET /api/v2/private/base-products` answers `description: null`, `categories: []`,
   `manufacturer: null` and `image: null` on every row — this section's claim that it "carries the
   description, the categories and the brand" is wrong, verified against the running stack. The
   console reads `GET /api/v2/products`, which runs the identical query through the full mapper and
   does not hide unpublished products. It is public, which is the cost.
9. **`ProductCriteria.productName` is bound and never read.** `ProductRepository`'s predicate builder
   covers `sku`, `manufacturerId`, `categoryIds` and `available` only. The name filter this section
   lists as "real" narrows nothing while appearing to, so the console offers three filters, not four,
   and its text box searches SKUs.
10. **Two DTOs answer `null` where their siblings answer `[]`.** `ReadableManufacturer.descriptions`
    and `ReadableProductType.descriptions` carry no Java initialiser. Typed nullable; their mappers
    narrow.
11. **A product's default image cannot be changed after upload.** `?defaultImage=` on the upload is
   the only place the flag can be set; `PATCH …/image/{imageId}` sets `sortOrder` and nothing else.
   `buildContentImages` also sets the flag without clearing it on the existing default, so passing
   `true` on a product that already has one leaves two. This invalidates one line of the Testing
   section below — "set the second as default" is not a thing the platform can do. The Media step
   marks which image is the thumbnail and does not offer to move it; reordering is real.

### Decisions (settled with the user)

| Question | Decision |
|---|---|
| Page shape | **Two pages, as the templates draw them.** A new `Catalogue` nav item → `/catalogue` (tabs: Categories, Product types, Brands, Groups), and the existing disabled `Inventory` item gets `route: '/products'` (list + form). |
| Inventory behaviour | **Not built.** No locations, reorder points, purchase orders, stock movements, valuation, CSV or bulk actions. Only the stock-levels *table* survives, as the product list. |
| Product shape | **One product, no variants.** `PersistableProductDefinition` with a flat `price` and `quantity`, exactly as seller-ui writes it — multi-variant does not work from the backend. |
| Product groups | **Built**, as a fourth tab on `/catalogue`. In seller-ui and in the backend, in no template. |
| Product form | **The wizard layout, backed steps only.** Four steps plus the readiness and translations panels. Unbacked fields are removed with a `lessons.md` entry each, *not* disabled — Module 5's "keep them, marked" call does not scale to a form with this many dead controls. |
| The products KPI row | **Removed entirely**, not reported unavailable — see below. |

### What gets built, block by block

**`/catalogue`** — from `console-template/Catalog (standalone).html`. `page-header` + a four-tab
`tab-switcher`; each tab is a list or tree on the left and a detail editor on the right.

| Block | Backing |
|---|---|
| Category tree, expand/collapse, depth | **real** — `GET /private/category-hierarchy` |
| Per-row product count | **real** — `ReadableCategory.productCount` |
| Drag to re-parent; drag to reorder as sibling | **real** — `PUT …/category/{child}/move/{parent}`; `sortOrder` on `PUT …/category/{id}` |
| "In nav" eye toggle | **real** — `PATCH …/category/{id}/visible` |
| Add child, add top-level, delete | **real** |
| Detail: locale chips, name, slug, description, SEO title, meta description, visible | **real** — `descriptions[]` carries `name`, `friendlyUrl`, `title`, `description`, `metaDescription`, `keyWords`, `highlights` |
| Category **banner image** | no field on `ReadableCategory` → **removed** |
| Product type list, name, code, description | **real** |
| Product type **attribute list** (name, kind, required, variant-defining) | nothing links a type to an attribute set → **removed**. The largest deviation on this page. |
| "Used by categories" chips on a type | no such relation → **removed** |
| Brand cards, name, slug, description, order | **real** |
| Brand **logo upload**, **"publish brand page"** toggle | `ReadableManufacturer` is `{id, code, order, descriptions}` → **removed** |
| "View storefront" | no preview target → **removed**, cross-referencing Module 5's entry |
| Groups tab (no template) | **real** — list, code, active toggle, per-language name, member picker |

**`/products`** — from `console-template/Inventory.dc.html`.

| Block | Backing |
|---|---|
| Header, store context, Add product | **real** |
| Table: name + SKU, category, price, quantity, available, actions | **real** — `GET /api/v2/private/base-products` |
| Tabs All / Available / Unavailable | **real** — `available` on `ProductCriteria`. Replaces In stock / Low / Out of stock / Overstock, none of which the platform can compute. |
| Filters: SKU, name, category, brand | **real** — `sku`, `productName`, `categoryIds`, `manufacturerId`. Backed and unused in seller-ui; the console exposes them. |
| Paging | **real** — `count`, not `size` |
| Inline edit of price, quantity, availability | **real** — `PATCH /api/v1/private/product/{id}` |
| Delete a product | **real**, behind `confirm-dialog` |
| KPI row, location cards, on-hand/reserved split, stock bars, "reorder at N", Value column, row checkboxes and bulk bar, "All locations", Import CSV, Stock count, Incoming purchase orders, Recent stock movements | **all removed** — nothing on the platform answers any of them |

**Why the KPI row goes rather than reporting unavailable.** Module 3's pattern — an em dash under a
"Not available yet" flag — was for a row where *some* tiles were real. Here all four are unbacked:
Stock on hand and Inventory value need a sum the platform never computes, Low stock needs a reorder
point that does not exist, and Out of stock needs a `quantity = 0` filter `ProductCriteria` does not
offer. A row of four em dashes is not an honest page, it is a decoration. The one real figure — the
total SKU count — already appears in the page header's context line and in the pagination footer.

**`/products/new` and `/products/:id`** — from `console-template/Add Product.dc.html`: the stepper
layout and both right-hand panels, over four backed steps.

| Step | Fields |
|---|---|
| **1 Essentials** | SKU (required, alphanumeric, uniqueness-checked live via `GET …/product/unique?code=`), visible, `dateAvailable`, `sortOrder`; per-language `name`, `friendlyUrl`, `title`, `highlights`, `description`, `metaDescription`, `keyWords` |
| **2 Media** | Images: upload, remove, reorder, set default |
| **3 Pricing & stock** | `price`, `quantity`, `canBePurchased`, `shipeable`, `virtual`; `productSpecifications` — weight, height, width, length + `weightUnitOfMeasure`, `dimensionUnitOfMeasure` |
| **4 Organize** | Categories (multi-select over the tree), brand (required), product type, related products (edit only) |

Removed, each with a `lessons.md` entry: barcode/GTIN, compare-at price, unit cost and its derived
margin, bulk pricing tiers, tax class, per-product currency, per-location opening quantity and
reorder point, collections, tags, supplier, and the SKU-generated-from-category hint. Backorder goes
too — `productQuantityOrderMin/Max` are per-order purchase limits, not a backorder flag, and reusing
them would be a fixture standing in for a real answer.

**The right column is real and worth keeping.** The readiness checklist is computed from the form's
own required fields, and the translations panel from `descriptions[]` — the console can see exactly
which locales a product is missing, which the feature inventory flags as invisible in seller-ui
(§16). Both are client-side and cost no endpoint.

**Media and Related need a saved product.** Images post to `/private/product/{id}/image` and
relationships to `/products/{id}/relationship/{relatedId}`, both needing an id. So on
`/products/new` step 2 and the related-products block are disabled with an honest label, and **Save
draft** (a `POST` with `visible: false`) creates the product and routes to `/products/:id` where
every step is live. Stated in the step rail, not discovered by clicking a dead control.

**Categories are applied by diffing.** Organize compares the selected set against what
`GET /api/v2/private/product/{id}` returned and issues `POST`/`DELETE
…/product/{productId}/category/{categoryId}` for the difference — possible only because the
stray-brace bug is fixed.

### Mapping table — old capability → new location

| seller-ui | console-ui |
|---|---|
| `catalogue/products/products-list` | `/products` |
| SKU + availability filters | same, plus category and brand filters (backed, previously unexposed) |
| Double-click inline edit of quantity/price | An explicit inline-edit affordance on the row — the feature inventory calls double-click "undiscoverable — only a `title` tooltip" |
| Availability checkbox in the row | same, via `toggle` |
| `products/create-product`, `products/product/:code` | `/products/new`, `/products/:id` — one wizard, not a form plus router sub-tabs |
| Product sub-tab: Images | Step 2, Media |
| Product sub-tab: Category | Step 4, Organize |
| Product sub-tab: Related | Step 4, Organize (edit only) |
| Product sub-tab: `discount` | **Deliberate removal** — declared in `PRODUCT_FORM_TABS` with no route and no component |
| `categories/categories-list` + `create-category` + `category/:id` | `/catalogue`, Categories tab — the list and the editor are one screen |
| `categories/categories-hierarchy` | Same screen. The tree *is* the list |
| `brands/*` | `/catalogue`, Brands tab |
| `types/*` | `/catalogue`, Product types tab |
| `products-groups/*` | `/catalogue`, Groups tab |
| Commented-out Options group, `product-ordering` | **Deliberate removal** — no routes exist in seller-ui either |

### New components

- **`shared/ui/tree`** — the category tree: expand/collapse, depth indent, drag to nest, drag to
  reorder as a sibling. Nothing in `shared/ui/` does hierarchy. Drag-and-drop alone is
  inaccessible, so every move is also reachable from the row's `menu` ("Move into…", "Move up",
  "Move down"), and the whole tree is keyboard-navigable.
- **`shared/ui/stepper`** — the wizard rail. `tab-switcher` is a `tablist` and cannot express
  completion, linear progress or a disabled-until-saved step; the ARIA contract differs.
- **`shared/ui/autocomplete`** — remote-search picker over `GET …/tiny-products`, for group members
  and related products. `tag-input` is chips-you-type, not a search.

**Reused, already present:** `data-table` + `table-row`, `pagination` (zero-based on the wire,
one-based on screen), `tab-switcher`, `page-header`, `panel`, `badge`, `busy-overlay`,
`confirm-dialog`, **`image-picker`** (product images — the Module 5 component, hence the
prerequisite), `form-field`, `toggle`, `progress-track` (the readiness percentage), `notice-bar`,
`toast`, `shared/i18n/money`, `shared/i18n/status-label` (`InventoryStatus` is **already named
there** — the availability badge reuses it).

### What stays unbacked → `lessons.md`

Appended after the existing 61, newest last, in the file's established heading and bullet format.
Each pairs with a `TODO(lessons.md):` marker at its call site.

1. **No multi-location inventory.** `InventoryEntity.region`/`regionVariant` are free text with no
   location entity, repository or endpoint.
2. **No reorder point and no low-stock threshold.** `productQuantityOrderMin`/`Max` are per-order
   purchase limits, not replenishment.
3. **No purchase orders and no supplier.** Zero hits pod-wide.
4. **No stock-movement ledger.** Quantity is a mutable column; the only movement surface is the
   checkout reservation trio, which is service-to-service.
5. **No product cost, and so no inventory valuation.** Only a sale `price` exists — which also
   removes the unit cost and its derived margin.
6. **No CSV import or export** anywhere, front or back.
7. **No bulk operations** — bulk price change, bulk visibility, bulk category assignment.
8. **No product tags and no collections.** Product groups are a different concept: a named,
   code-addressed membership set, not free-form labels.
9. **No barcode / GTIN on a product.**
10. **No compare-at price and no quantity-break tiers.** `ProductPriceApi` allows several prices per
    inventory record but carries neither semantic.
11. **No tax class and no per-product currency.**
12. **A product type carries no attribute definitions.** `ProductAttributeOptionApi` and
    `ProductPropertySetApi` exist, but nothing links a *type* to the attributes a product of that
    type must carry. The whole right-hand panel of the template's Product types tab rests on this.
13. **A category has no banner image**, and **a brand has no logo and no publish flag.**
14. **No SKU generation.** The design says "generated from category"; nothing derives one, and
    `GET …/product/unique?code=` only answers whether one is taken.
15. **`PATCH /api/v1/private/product/{id}` is mapped twice**, differing only by `produces`.

Cross-referenced rather than duplicated: the storefront-preview gap (Module 5), and
`product-statistic` answering with `date=null` (Dashboard).

### Implementation

Two features, each following `features/store-management`'s four layers — page → facade → feature api
service → `@api/*`.

- **`features/catalogue/`** — `catalogue.ts`/`.html`/`.css`,
  `components/{category-tab,type-tab,brand-tab,group-tab}/`, `facades/catalogue.facade.ts`,
  `services/catalogue.api.service.ts`. The facade uses `rxResource` keyed on
  `shell.currentStoreId()` with a `linkedSignal` last-good snapshot, forms held in the facade
  because Save lives in the header — the `StoreSettingsFacade` contract exactly.
  `loadCatalogue()` is a `forkJoin` in which **the category hierarchy is the unwrapped leg** (it is
  the page); every other leg goes through the existing `optionalList`/`optionalOne` `catchError`
  helpers, each with an inline comment naming why *that* leg may fail. Writes reload rather than
  echo the operator's input, since the endpoints answer `void`.
- **`features/products/`** — the list: `products.ts`, `facades/products.facade.ts`,
  `services/products.api.service.ts` mapping `ReadableProduct` → `ProductRow` and translating
  filters to `ProductCriteria`.
- **`features/product-form/`** — the wizard: `product-form.ts`, `facades/product-form.facade.ts`,
  `services/product-form.api.service.ts`, `services/product-form.service.ts` (the reactive form,
  following `store-settings-form.service.ts`), `components/{essentials,media,pricing,organize}-step/`.
- **Routing** — `app.routes.ts`, under `ConsoleShell` with `canAccessSecuredPages, consoleContext,
  requiresStore` and `data: {titleKey, breadcrumbKey}`: `catalogue`, `products`, `products/new`,
  `products/:id`. Both trees are `RenderMode.Client` in `app.routes.server.ts` —
  `SelectedStoreRequestContext.params()` throws during SSR, the same reason `store-management/**`
  is client-rendered. Route params are validated before reaching a facade, per Module 4's
  `/orders/abc` finding.
- **Nav** — `console.fixture.ts`: add `{labelKey: 'shell.nav.item.catalogue', icon: 'sitemap',
  route: '/catalogue'}` to the Commerce group, and give the existing `inventory` item
  `route: '/products'`.
- **i18n** — names come from the server's `descriptions[]`, matched against
  `transloco.activeLang()` with a first-entry fallback, so there is no known-set problem for them.
  The two enum-ish values that do need the Module 4 guard are `weightUnitOfMeasure` and
  `dimensionUnitOfMeasure`. Locale chips render `LocaleService`'s available languages — en and ar,
  not seller-ui's five. Prices go through `shared/i18n/money`, never bare `Intl`; SKUs and slugs get
  `unicode-bidi: plaintext` inside the RTL page.
- **No fixture.** `src/app/mocks/` gains nothing and no seam for one, per the Module 5 precedent.

### Testing

Both UIs, same org admin, `ORG1-STORE1` open in each.

- The product list shows the same products as seller-ui's `products-list` — same ids, SKUs,
  quantities, prices, availability, same total count.
- Filter by SKU and by availability: both narrow identically. Then exercise the two filters
  seller-ui never exposed — category and brand. **There is no name filter** — see Deviation 9.
- Paging: `count` is the page-size parameter; page 2 differs and the total is stable.
- Inline-edit a price, a quantity and availability; reload seller-ui and confirm all three moved.
- Create a product end to end: SKU uniqueness reports a taken code live, Save draft creates it and
  routes to `/products/:id`, then upload two images and reorder them, and confirm the order in
  seller-ui's Images tab. **Not** "set the second as default" — see Deviation 8: no endpoint
  re-designates a default image, so the console marks the thumbnail and does not offer to move it.
- Assign and unassign categories in Organize; confirm in seller-ui's Category tab. **This is the
  module's sharpest test** — the endpoint has never worked from seller-ui.
- Attach and remove a related product; confirm in seller-ui's Related tab.
- Category tree: nest a category by drag, reorder a sibling, do the same two moves by keyboard
  through the row menu, toggle visibility, and confirm the hierarchy in seller-ui's tree screen.
- Create and delete a brand, a product type and a product group; add and remove a group member.
- A product with no images, one with no Arabic description, and a category with no children all
  render — and the translations panel names the missing locale rather than showing blank.
- Delete a product, a category and a brand, each behind the confirm dialog, **against throwaway
  records only**.
- Arabic and all three themes; 1440 / 900 / 420.

### Commits

1. `plan(console-ui): product catalogue` — this section.
2. `feat(console-ui): the category tree on real categories`.
3. `feat(console-ui): brands, product types and product groups`.
4. `feat(console-ui): product list on real products`.
5. `feat(console-ui): the product form`.
6. `fix(console-ui): catalogue after QA`.

Six rather than three: the API tier and both features are net-new, and folding the tree, the three
taxonomy tabs, the list and the wizard into one diff would make it unreadable. `lessons.md` entries
ride with the commit that creates their `TODO` marker.

### Critical files

**seller-ui: not modified. store-pod: not modified** — every backend finding above is logged, not
fixed, per the standing convention.

**New:**
`src/app/api/catalog/{product,product-image,product-relationship,category,manufacturer,product-type,product-group}.service.ts`,
`src/app/models/{catalog,products,taxonomy}.ts`,
`src/app/features/catalogue/{catalogue.ts,.html,.css,components/,facades/,services/}`,
`src/app/features/products/{products.ts,.html,.css,facades/,services/}`,
`src/app/features/product-form/{product-form.ts,.html,.css,components/,facades/,services/}`,
`src/app/shared/ui/{tree,stepper,autocomplete}/`.

**Changed:** `src/app/app.routes.ts`, `src/app/app.routes.server.ts`,
`src/app/mocks/console.fixture.ts` (the two nav routes), `src/locale/{en,ar}.json`,
`eslint.config.js` (`ignoreAttributes` for any new component-internal enum input), `lessons.md`.

**Reused, already present:** `core/http/crud.service.ts` (`?store=` stamping),
`core/table/table.types.ts` (`PageT`, `PageRequest`, `count` not `size`), `core/errors/*`
(`applyToForm`, `clearServerErrorsOnChange`), `shared/i18n/{money,status-label}.ts`,
`shared/ui/image-picker/`, `shared/ui/confirm-dialog/`,
`testing/{transloco-testing,console-api.fake}.ts`.

### Verification

1. `cd store-core/console-ui && npm run build && npm run lint && npm test`;
   `git -C store-core/seller-ui status --porcelain` and `git -C store-pod status --porcelain` both
   empty.
2. `grep -rn "categoryId}}" src` → no hits. The stray-brace bug does not survive the port.
3. `grep -rn "/private/product/.*/images" src` → no hits. The dead plural mapping is not ported.
4. `grep -rn "reorder\|purchaseOrder\|stockMovement\|onHand\|inventoryValue" src` → no hits. No
   inventory concept leaked in from the template.
5. `grep -rn "variant\|optionValue\|propertySet" src/app/api` → no hits. The no-variants decision
   holds at the API tier.
6. `grep -rn "TODO(lessons.md)" src` — every marker in the diff has a matching heading in
   `lessons.md`, and every new heading has a marker.
7. The two-tab comparison above, driven through Chrome — in particular the category-assignment
   round-trip, this module's strongest evidence, because the endpoint has never worked from the old
   console.
8. Network tab: the list request carries `?store=`, `page`, `count` and the active filter; the
   product `POST` is v2 and the inline edit is a v1 `PATCH`; the image upload is multipart with
   `?order=` and `?defaultImage=`.
9. Specs: `products.api.service.spec.ts` and `catalogue.api.service.spec.ts` following
   `orders.api.service.spec.ts` (a fake that filters and pages the way the server would), plus page
   specs for both features asserting against rendered English copy.

---

## Module 7 — Payments — **done**

### Context

The named order puts payments next, and the console's nav rail already carries a routeless
`shell.nav.item.payments` — this app's marker for "not built". This module gives it a route.

The seller has no way, in the new console, to see the money behind an order or to confirm a manual
bank transfer. seller-ui has exactly one payments screen (`/pages/payment/payment-list`) and
`.agents/plans/seller-ui-feature-inventory.md` §14 calls its approve/reject flow *"a genuinely
important operator task that is currently buried in a table row"* (gap #10). That task is the reason
this module exists.

**The finding that shapes it.** The payment backend is the *smallest* on the platform and the design
asks it for more than any other template. Verified by reading every controller under
`store-pod/payment/payment-service/.../api/` and grepping the whole repo:

- **The entire seller-facing HTTP surface is three endpoints**: a filtered paged transaction list,
  `approve`, and `reject`. Plus gateway-credential CRUD, which Module 5 already ported and ships.
- **There is no aggregate endpoint of any kind.** `grep -i statistic` across `store-pod/payment`
  returns zero hits. No counts, no sums, no group-by, no series.
- **`payout`, `settlement`, `chargeback` and `gatewayFee` have zero backend hits repo-wide.** Every
  occurrence of `payout` (16) and `chargeback` (1) is inside `console-template/*.dc.html`. The
  template's Payouts panel, dispute tile, gateway-fee column and settlement summary are drawn from
  nothing at all.
- **`PaymentStatus.WAITING_VERIFICATION` is never set.** `ManualTransferredProcessor.initiate`
  returns `PENDING`. The constant appears in exactly two Java files: its own declaration and one
  mapping line. **This makes the dashboard tile shipped in Module 3 count 0 forever** — it filters on
  `WAITING_VERIFICATION`.
- **A transaction carries no customer and no order.** `ReadableTransaction` is
  `{id, internalRef, requestRef, amount, currency, paymentType, status, transactionDate,
  transactionNo}`. The link to an order is `requestRef`, which
  `checkout/.../OrderPlacementFacadeImpl.java:148` writes as `modelOrder.getId().toString()` — a
  convention nothing types or enforces. `payment.transaction` even has a dead `order_id bigint`
  column in `schema.sql` with no entity mapping.

So this is the module where the template and the platform diverge hardest. The page that gets built
is honest and small: **a transaction ledger, filterable and paged, with a manual-transfer approval
queue at the front of it.**

### Decisions (settled with the user)

| Question | Decision |
|---|---|
| KPI row | **Count-only tiles.** Four honest integers — Awaiting approval, Captured, Failed, Refunded — each a one-row fetch read off `totalElements`. Relabelled as counts, never amounts. Money sums get a `lessons.md` entry. |
| The order link | **Both directions.** The ledger's Order column links to `/orders/{requestRef}`, **and** the existing order-details page gains a Payments panel listing that order's transactions via `?requestRef={id}`. Closes the standing gap from both sides at zero backend cost. |
| The approval queue | **Fix the console, log the backend gap.** Key the queue on `status=PENDING` + `paymentType=MANUAL_TRANSFER`, correct the Module 3 dashboard tile to match, and record that the backend should set `WAITING_VERIFICATION`. |
| Gateway config | **Stays in store-management.** `/payments` is the ledger only; its header action routes to `/store-management/payments`, exactly as the template's primary action does. |

### seller-ui today

`/pages/payment/payment-list` — the only screen. Guarded by nothing but `canAccessSecuredPages`;
access control is menu-visibility only (`pages-menu.ts:8-10`, orgAdmin / storeAdmin /
storeModerator). The backend `@PreAuthorize(… 'STORE-POD.PAYMENT.*')` is the real gate.

Filters: status, payment type, `requestRef`, `internalRef`, date from, date to, with Search and
Reset. Columns: internalRef, requestRef, amount + currency, payment type, **raw ISO transaction date
with no pipe**, status, transactionNo, actions. Approve opens a dialog asking for the external
transaction number; **Reject fires on one click with no confirmation at all** — an asymmetry worth
fixing. Filters are not URL-persisted, and `onFilterChange` does not reset the page offset.

Also payment-shaped, and out of scope: store payment configuration (Module 5, shipped), and
order-details' Refund / Capture buttons and Transactions dialog — all three already recorded in
`lessons.md` as endpoints that have never existed.

### API surface to port

Into the **existing** `src/app/api/payment/` — Module 3 ported the read half and its header comment
says so: *"`approveTransaction`, `rejectTransaction` and the supported-type lookups belong to the
payments module."*

| File | Added |
|---|---|
| `api/payment/payment.service.ts` | `approve(internalRef, {transactionNo})` → `POST /spg/payment/api/v1/private/payment/transaction/{internalRef}/approve`; `reject(internalRef)` → `POST …/{internalRef}/reject`; `countAwaitingApproval(range)` replacing the queue half of `countByStatus` |
| `api/payment/payment-configuration.service.ts` | `supportedStatuses()` → `GET …/private/payment-configuration/supported-payment-statuses` — placed here, beside the existing `supportedTypes()`, because they share a base path |

Already ported and reused unchanged: `transactions(query)`, `countByStatus(status)`,
`supportedTypes()`.

Wire DTOs stay in **`models/payment.ts`**; view models go in a new **`models/transactions.ts`**
(`TransactionRow`, `PaymentTab`, `PAYMENT_TABS`, `PaymentKpiSource`) — the `checkout.ts` /
`orders.ts` split Module 4 established.

**Deviations.** Each is a real bug found by porting under `strict: true`, per the standing rule.

1. **`PaymentTransaction` has no `id`.** The backend returns `Long id`; seller-core's model drops it,
   leaving rows with no stable key. Added as `readonly id: number` and used as the track-by.
2. **`getSupportedPaymentTypes` is declared twice in seller-core** — in both the `payments` and
   `stores` entry points, hitting the same URL. Ported once.
3. **Cleared filters are sent as `''`.** `CrudService.getParams` only drops `undefined`, so
   `?status=&paymentType=` goes on the wire and works only because Spring happens to bind `''` to
   `null` for enums and `Instant`s. The port omits the key instead.
4. **`getTransactions` passes `store` in its params and `CrudService` then overwrites it** from
   `REQUEST_CONTEXT`. Not ported — callers never pass `store`.
5. **`transactionNo` is `string` in seller-core and is null until approval.** Already
   `string | null` in console-ui; confirmed rather than changed.
6. **`paymentType` stays a wire `string`, not a union.** A new gateway must not throw under
   Transloco's strict-missing handler — it goes through the Module 4 known-set guard, reusing
   `isPaymentType` / `PAYMENT_TYPE_LABEL_KEY` already in `models/store-settings.ts`.
7. **`count`, not `size`, is correct here — verified.** `store-commons/autoconfigure`'s
   `ServletWebConfig:36-38` registers a `PageableHandlerMethodArgumentResolver` with
   `setSizeParameterName("count")`, and `payment-service/build.gradle:66` depends on that module, so
   the existing port is right. It is nonetheless the **first thing to confirm in the network tab**,
   because `PrivatePaymentApi.list` takes a bare `Pageable` and nothing in the payment service itself
   names the parameter.

### What gets built, block by block

`console-template/Payments.dc.html` designs roughly fifteen blocks. **Five have data.**

| Block | Backing |
|---|---|
| Page header, store context, date range | **real** |
| Transactions table: ref, order, method, status, amount, date | **real** — `GET …/payment/transactions` |
| Tabs by status; gateway filter; ref search; paging | **real** — `TransactionSearchFilter` + `page`/`count` |
| Approve / Reject on an actionable row | **real** — the two POSTs |
| KPI row, as **counts** | **real, derived** — four one-row fetches reading `totalElements` |
| KPI row as **amounts** (Captured $, Pending $, Refunded $) | no sum anywhere → **counts instead** |
| "Volume by day" 14-bar chart | no series endpoint → **removed** |
| Gateways panel with amounts and share % | no group-by, no sums → **removed** |
| Settlement summary: gross, fees, refunds, net payout | no fee field, no payout → **removed** |
| Payouts panel (4 rows, schedule link) | zero backend hits repo-wide → **removed** |
| Disputes tile, Disputed tab | zero backend hits → **removed** |
| Customer column (avatar, name, email) | not on a transaction → **removed** |
| Method meta (`•••• 4242`, card brand) | not on a transaction → **removed** |
| Per-row `fee $36.20` | no fee field anywhere → **removed** |
| Refunds tab as an *action* | `REFUNDED` is a status nothing can set from an API → **status filter only** |
| "Approve all" bulk bar | each approval needs its own external `transactionNo` → **removed** |
| Export CSV | **PDF instead**, via the existing `app-export-button`, as Orders resolved it |
| Sidebar badge count (7) | already removed platform-wide — `lessons.md` "Shell — no sidebar badge counts" |

**Tabs.** `Awaiting approval` first, then `All`, then the **real ten** `PaymentStatus` values —
horizontally scrolling, exactly as Module 4's order strip does, rather than the mockup's invented
six. "Awaiting approval" is a compound the server can express: `status=PENDING` **and**
`paymentType=MANUAL_TRANSFER` are separate filter fields and are ANDed.

**Filters.** A gateway `app-select` from `supported-payment-types`; an `app-date-range-picker`
widened to `T00:00:00Z` / `T23:59:59Z` the way `orders.api.service.ts` already does; and one
`app-search-box` routed to a single server field by shape — all digits means `requestRef` (an order
id), anything else means `internalRef` (a UUID). Sending both would AND to nothing. Tab, filter and
page state live in the URL, per the page contract.

**Columns.** Transaction (`internalRef`, `latin mono`, with `app-copy-field`; `transactionNo`
beneath when set) · Order (`#{requestRef}` linking to `/orders/{requestRef}`) · Method · Status
(`app-badge`, tone from the shared payment tone map so a status is the same colour as on the
dashboard) · Amount (`@shared/i18n/money`) · Date (`TranslocoLocaleService`) · Actions.

**The KPI row keys on store + date range only, not on tab, search or page** — so switching tabs does
not refire four count requests. It is a second `snapshot()`, and each of the four legs is
`catchError`-optional; the table is the unwrapped leg that *is* the page.

**Approve and reject.** Buttons appear on the four statuses seller-ui treats as actionable
(`PENDING, PROCESSING, WAITING_VERIFICATION, AUTHORIZED`). Approve needs a required, non-blank
external `transactionNo`; check whether `app-confirm-dialog` can host a projected required field
before building anything (`ARCHITECTURE.md` §4 is the catalogue, and a sixth copy of the field
vocabulary is what that rule exists to stop). If it cannot, add a feature-local
`components/approve-dialog/` — not a shared component, since only this page approves. **Reject gets
a confirm dialog**, closing seller-ui's one-click asymmetry.

### Mapping table — old capability → new location

| seller-ui | console-ui |
|---|---|
| `/pages/payment/payment-list` | `/payments` |
| Status + payment-type selects | Status becomes the tab strip; payment type stays a select |
| `requestRef` and `internalRef` text filters | One search box routed by shape |
| Date from / date to inputs | `app-date-range-picker`, shared with Orders and the dashboard |
| Search / Reset Filter buttons | Filters apply live and are in the URL; `app-empty-state` offers "clear filters" |
| Raw ISO date column | `TranslocoLocaleService`, locale-correct in Arabic |
| `{{amount}} {{currency.code}}` | `@shared/i18n/money` |
| Approve dialog (free-text transaction no.) | Same, with the field required and validated |
| Reject, one click, no confirmation | Reject behind `app-confirm-dialog` |
| `transactionNo` column | Folded under the transaction ref, where it belongs |
| — (seller-ui has no such link) | **New:** Order column links to `/orders/{requestRef}` |
| — | **New:** a Payments panel on `/orders/:id` |

Nothing from seller-ui's payments screen is dropped.

### New components

**None expected.** Everything the honest page needs is in `ARCHITECTURE.md` §4 already:
`page-header`, `kpi-grid` + `kpi-card`, `panel`, `tab-switcher`, `select`, `search-box`,
`date-range-picker`, `data-table` + `table-row`, `badge`, `pagination`, `action-list` (the overflow
menu), `confirm-dialog`, `copy-field`, `notice-bar`, `empty-state`, `busy-overlay`, `load-error`,
`export-button`. The one possible addition is the feature-local approve dialog above, and only if
`confirm-dialog` cannot project a field.

The two shared components the mockup would have needed — `charts/bar-chart` and `ranked-list` —
already exist and are **not** used here, because the volume series and the gateway split have no
data behind them.

### What stays unbacked → `lessons.md`

Appended after the existing 99, newest last, in the file's established heading and bullet format.
Each pairs with a `TODO(lessons.md):` marker at its call site.

1. **No payment aggregates of any kind.** No counts, no sums, no group-by, no series. Kills the money
   KPIs, the volume-by-day chart and the gateway split. Expected: `GET …/transactions/summary` →
   totals by status and by payment type over a range.
2. **No payouts and no settlement.** Zero hits repo-wide outside the mockup.
3. **No disputes and no chargebacks.** Same.
4. **No gateway fee, and so no net amount.** No fee field or column anywhere in payment or checkout.
5. **No refund operation.** `PaymentStatus.REFUNDED` and `TransactionType.REFUND` exist as enum
   constants; nothing sets them, and `TransactionType` is dead code referenced by no entity or DTO.
6. **No capture and no void.** Cross-references the existing "Orders — no refund and no capture".
7. **A transaction carries no customer.** Kills the Customer column and the card-brand meta line.
8. **`WAITING_VERIFICATION` is never set.** `ManualTransferredProcessor` returns `PENDING`, so the
   status that names the approval queue is unreachable. The console filters on
   `PENDING` + `MANUAL_TRANSFER` instead.
9. **`reject` tells checkout nothing.** `approve` fires `PaymentPaidEvent`; `reject` fires no event,
   so a rejected payment leaves its order where it was.
10. **`approve` and `reject` are unguarded and not idempotent.** No state check, so approving an
    already-`PAID` transaction re-fires the event. The console guards in the UI; the server does not.
11. **The order link is a convention, not a contract.** `requestRef` holds the order id only because
    `OrderPlacementFacadeImpl:148` writes it there, and a dead `order_id bigint` column sits unmapped
    in `payment/init-sql/schema.sql`. Updates the standing "Orders — no link from an order to its
    payment transactions" entry to record that the console now traverses it *by convention*.
12. **No transaction detail endpoint.** `GET …/transactions/{internalRef}` does not exist — the row
    is all there is. `getTransaction` also throws a bare `IllegalArgumentException` for an unknown
    ref, so an internal lookup 500s rather than 404s.
13. **`PAYPAL` is offered but has no processor.** `supported-payment-types` lists it; only
    `StripeProcessor`, `CODProcessor` and `ManualTransferredProcessor` exist, so selecting it fails at
    initiate time.
14. **The transaction schema's status CHECK constraint is stale** — it allows `PAY_LATER` and omits
    `AUTHORIZED` and `REFUNDED`, so persisting either of those two statuses would violate it.
15. **No CSV export**, front or back. The header exports PDF.

Cross-referenced rather than duplicated: "Dashboard — counting requires fetching" (now paid five
times on this page), "Store management — payment and social-login reads return secrets in cleartext",
"Shell — no sidebar badge counts".

Not a `lessons.md` entry, because it is a **console** gap rather than a backend one: the server
honours `sort=` and default-sorts `transactionDate DESC`, but `app-data-table` has no column-sort
affordance. Recorded here as a deliberate omission.

### Implementation

Four layers, per `ARCHITECTURE.md` §2 — page → facade → feature api service → `@api/*`.

- **`features/payments/`** — `payments.ts` / `.html` / `.css`, `facades/payments.facade.ts`,
  `services/payments.api.service.ts`, and the approve dialog if one is needed. The facade is
  **provided by the page**, never `providedIn: 'root'`. `isLoading` / `error` / `isEmpty` / `reload`
  come from `snapshot()`; `busy` is the in-flight approve or reject; the facade raises the toasts.
  Two `snapshot()`s: the table (keyed on store + tab + filters + page) and the KPI counts (keyed on
  store + range only).
- **`services/payments.api.service.ts`** is the assembly point, following `orders.api.service.ts`
  closely: a `forkJoin` of the paged list and the four optional counts, mapping `PaymentTransaction`
  → `TransactionRow`, and translating the page's filter into `TransactionQuery`. Writes reload
  rather than echo — both endpoints answer `void`.
- **Routing** — `app.routes.ts`: `payments` under `ConsoleShell` with
  `[canAccessSecuredPages, consoleContext, requiresStore]` and `data: {titleKey, breadcrumbKey}`.
  **`app.routes.server.ts` gains a `payments/**` entry with `RenderMode.Client`** — mandatory, or
  `app.routes.spec.ts` fails; `SelectedStoreRequestContext` throws during SSR. Any route param is
  validated before it reaches a facade.
- **Nav** — `layouts/console-shell/console-navigation.ts`: give the existing routeless
  `shell.nav.item.payments` `route: '/payments'`, with the same one-line comment Module 6 left on
  `inventory`.
- **Order details** — `features/order-details/services/order-details.api.service.ts` gains one
  `optionalList` leg calling `transactions({requestRef: String(orderId), page: 0, count: 20})`, and
  `order-details.html` a Payments panel: ref, method, status, amount, date. Read-only — approving
  belongs on the payments page. The existing `TODO(lessons.md)` about the unlinked transactions is
  rewritten to say the link is now traversed by convention.
- **Dashboard correction** — `features/dashboard/services/dashboard.api.service.ts` swaps
  `countByStatus(AWAITING_VERIFICATION)` for `countAwaitingApproval()`, and
  `dashboard.api.service.spec.ts` follows. `AWAITING_VERIFICATION` keeps its place in
  `models/payment.ts` with a doc comment recording *why* it is not what the queue filters on.
- **i18n** — a new `payments.*` root in `src/locale/{en,ar}.json` at exact key parity. Statuses are
  **not** newly translated: every `PaymentStatus` value is already under the shared `status.*`
  namespace and reachable through `@shared/i18n/status-label`'s known-set guard. Payment types go
  through the same guard via `models/store-settings.ts`'s existing `PAYMENT_TYPE_LABEL_KEY`. Money
  through `@shared/i18n/money`, dates and counts through `TranslocoLocaleService` — never bare
  `Intl`. Refs, UUIDs and order numbers get `latin` (`unicode-bidi: plaintext`) inside the RTL page.
- **No fixture.** `src/app/mocks/` no longer exists and nothing reintroduces it.

### Testing

Both UIs, same org admin, `ORG1-STORE1` open in each.

**Generating test data first**, since a fresh store has no transactions and the approval queue is the
point of the page:

1. In store-management → Payments, enable **MANUAL_TRANSFER** for `ORG1-STORE1`.
2. Place orders on the live storefront at `http://org1-store1.spg-507f1f77.gateway.com/ar`, choosing
   manual transfer — each writes a `PENDING` transaction whose `requestRef` is the new order id.
   Place at least one Stripe/COD order too, so the gateway filter has more than one value.

Then:

- The ledger shows the same transactions as seller-ui's `payment-list` — same refs, amounts, types,
  statuses, same total count.
- Filter by each status, by gateway, by date range, and search by both an order id and an internal
  ref: both UIs narrow identically.
- **Paging is the decisive check** — set the page size and confirm the server honours `count` (page 2
  differs, the total is stable, and the row count matches the requested size, not 20). See
  Deviation 7.
- The Order column links to `/orders/{requestRef}` and lands on the right order.
- The order-details Payments panel lists that order's transactions, and an order with none renders
  the empty state rather than a blank panel.
- **Approve a manual transfer** with an external transaction number; confirm it reads `PAID` in
  seller-ui after a reload, and that the order moved. Then **reject** another and confirm the
  transaction reads `REJECTED` while the order *does not* move — that is gap 9, observed rather than
  assumed.
- The KPI counts match the tab strip's own filtered totals, and switching tabs does not refire them.
- The awaiting-approval count is non-zero after step 2 — the thing the Module 3 tile could never show.
- The dashboard's "payment approvals waiting" figure now agrees with the payments page.
- A transaction with a null `transactionNo`, one with a status the console has not seen, and a store
  with no transactions at all all render.
- Export the ledger to PDF; check the figures against the table.
- Arabic and all three themes; 1440 / 900 / 420.

### Commits

1. `plan(console-ui): payments` — this section.
2. `feat(console-ui): the transaction ledger on real payments` — the read half, the route, the nav
   item, the KPI counts, the order link.
3. `feat(console-ui): approve and reject a manual transfer` — the two write paths, their dialogs, the
   order-details panel, and the dashboard queue correction.
4. `fix(console-ui): payments after QA`.

Four rather than three: the reads and the writes are independently reviewable, and the write half
reaches outside the new feature into the dashboard and order details.

### Critical files (Module 7)

**seller-ui: not modified. store-pod: not modified** — every backend finding above is logged, not
fixed, per the standing convention. That includes the `WAITING_VERIFICATION` gap, the stale CHECK
constraint and the dead `order_id` column.

**New:** `src/app/features/payments/{payments.ts,.html,.css,facades/,services/}`,
`src/app/models/transactions.ts`, and a feature-local `components/approve-dialog/` only if
`app-confirm-dialog` cannot host a required field.

**Changed:** `src/app/api/payment/payment.service.ts` (approve, reject, the queue count),
`src/app/api/payment/payment-configuration.service.ts` (`supportedStatuses`),
`src/app/models/payment.ts` (`id`, the queue doc comment),
`src/app/features/dashboard/services/dashboard.api.service.ts` (+ spec),
`src/app/features/order-details/services/order-details.api.service.ts` and `order-details.html`
(the Payments panel), `src/app/app.routes.ts`, `src/app/app.routes.server.ts`,
`src/app/layouts/console-shell/console-navigation.ts`, `src/locale/{en,ar}.json`, `lessons.md`.

**Deleted:** nothing. There is no payments fixture — `src/app/mocks/` was removed in the alignment
pass.

**Reused, already present:** `core/http/crud.service.ts` (`?store=` stamping),
`core/http/{optionalOne,optionalList}`, `core/table/table.types.ts` (`PageT`, `PageRequest`,
`count`), `shared/state`'s `snapshot()`, `shared/i18n/{money,status-label}.ts`,
`models/store-settings.ts`'s `isPaymentType` / `PAYMENT_TYPE_LABEL_KEY`,
`core/export/pdf-export.service.ts` + `shared/ui/export-button`, and the §4 control catalogue.
`features/orders/` is the closest sibling — page, facade, api service and template all follow it.

### Verification (Module 7)

1. `cd store-core/console-ui && npm run build && npm run lint && npm run test:ci`;
   `git -C store-core/seller-ui status --porcelain` and `git -C store-pod status --porcelain` both
   empty.
2. `grep -rn "payout\|settlement\|chargeback\|dispute\|gatewayFee" src` → no hits. None of the
   template's invented finance vocabulary leaked in.
3. `grep -rn "WAITING_VERIFICATION" src` → only `models/payment.ts`, where the doc comment explains
   why the queue does not filter on it.
4. `grep -rn "TODO(lessons.md)" src` — every marker in the diff has a matching heading in
   `lessons.md`, and every new heading has a marker. `npm run lint:lessons` enforces this.
5. `src/app/app.routes.spec.ts` passes — proof the `payments/**` render-mode entry exists.
6. Network tab: the list request carries `?store=`, `page`, `count` and the active filter; **the
   response honours `count`**; the approve `POST` is `…/payment/transaction/{internalRef}/approve`
   with a `{transactionNo}` body and the ref is the **UUID, not the numeric id**; no request fires
   twice per page load, and switching tabs fires one request, not five.
7. Specs: `payments.api.service.spec.ts` following `orders.api.service.spec.ts` (a fake that filters
   and pages the way the server would), a `payments.spec.ts` page spec asserting against rendered
   English copy, and `HttpTestingController` specs for the two new api methods asserting URL, verb,
   params and body.
8. The two-tab comparison above, driven through Chrome — in particular the approve round-trip, this
   module's strongest evidence, because it is the one operator task the old console buried.

---

## Module 8 — Users & profile — **done**

### Context

The console has no way to add a colleague. `shell.nav.item.userManagement` is routeless — this
app's marker for "not built" — and the toolbar's profile menu ends in two `<button>` elements with
no click handler and no route (`console-toolbar.ts:142-143`). Every seller who needs a second
operator has to go back to seller-ui.

Behind that gap sits the **largest working backend surface any module has found so far, and the
only one no frontend has ever called at all**:

- `UserAccountApi` (`tenancy`, `api/v1/user-account`) — `list`, `find-one`, `assignable-roles`,
  `create`, `update`, `reset`, `delete`, `enable`, `disable`. seller-ui uses most of it.
- `OrgMemberApi` (`tenancy`, `api/v1/org-member`) — members, and a complete invitation flow:
  `invite`, `resend`, `revoke`, `accept`, tested (`InvitationServiceTest`), audited
  (`tenancy_audit`), QA-verified (`qa/tenancy-and-pod-registry-split.md`, LIF-04), and **called by
  nothing**. seller-ui has no invitations at all.

So this module is not a fixture swap and not a parity port. It is the first module where the
console *overtakes* seller-ui, and the shape it takes is decided by four defects that reading the
backend turned up.

### The four findings that shape this module

1. **`STORE-CORE.USERS.RESET_PASSWORD` is mapped nowhere.** It appears in exactly one file in the
   repo — the `@PreAuthorize` on `UserAccountApi.resetPassword` itself.
   `CustomPermissionEvaluator.hasStoreCorePermission` wires the other six `USERS.*` tokens and
   falls through billing and pod-registry to `default -> false`, so **`POST /user-account/reset`
   is 403 for every caller, super admin included.** seller-ui never noticed because its
   change-password screen points at `PATCH /v1/private/user/{id}/password`, a URL that has never
   existed.
2. **The JWT `sub` is the username, not a user id.** `JwtCustomizerConfig` adds `roles` and spreads
   `users.metadata` (`org`, `store`) into the **access** token, and adds no id claim; Spring
   Authorization Server sets `sub` from `principal.getName()`. Meanwhile every read-by-id path ends
   at `AdminUserClient.getUser(id)` → `GET {uaa}/api/v1/admin/users/{uuid}`, and uaa exposes no
   get-by-username. **The console cannot look itself up.** This is also why fixing
   `user-account/current`'s `Principal`/`Jwt` binding would not be enough on its own — it would
   turn a 500 into a 404.
3. **The user list is store-scoped, so an org admin is invisible in it.**
   `ManagedUserAccountServiceImpl.list` filters uaa on `{org, store}` and `validateUserAccess`
   rejects any user whose metadata `store` differs from the requested one. `org1-admin` carries
   `{"org": …}` and no store (`uaa/init-sql/data-test-stores.sql`), so the org's own admin appears
   in no store's list and cannot be read by `find-one` under any store — including by themselves.
4. **`assignable-roles` offers `SUPER_ADMIN` to an org admin.**
   `UserAccountServiceImpl.getAssignableRoles` filters out `USER` and `ORG_ADMIN` only, and the
   seeded set is `{SUPER_ADMIN, USER, ORG_ADMIN, STORE_ADMIN, STORE_MODERATOR}`. A role picker fed
   straight from that endpoint offers platform superuser.

### Decisions (settled with the user)

| Question | Decision |
|---|---|
| Scope | **Both pages.** `features/users/` → `/users` (fills the routeless nav item) and `features/profile/` → `/profile` (fills the dead toolbar button). |
| Invitations | **Built in full**, including the accept route. The console gains a capability seller-ui never had, at no backend cost. Nothing emails the invite — the API returns the token once, so the console shows a copyable link. |
| The `RESET_PASSWORD` permission gap | **Fixed, not just logged** — a deliberate departure from the standing "logged, not fixed" convention, in `store-commons/autoconfigure` (not `store-pod`, not `seller-ui`). One `case`, one unit test, its own commit. |
| The signed-in user's identity | **Username only.** `/profile` does not call `find-one`. Finding 2 makes that not merely conservative but correct: with `sub` a username, the lookup 404s; and for an org admin finding 3 would 403 it anyway. |

**A consequence of the second and third decisions together, stated plainly.** The permission fix
makes `reset` work, but it cannot make it work *on `/profile`*: a self-service change-password needs
the caller's own user id, and finding 2 says the console does not have one. What the fix buys is the
**admin reset on `/users`** — where the row carries a real `ReadableUser.id` — which is exactly the
operation seller-ui's change-password screen was trying and failing to perform. `/profile` gets a
`lessons.md` entry instead of a password field.

### seller-ui today

`src/app/pages/user-management/`, guarded only by `canAccessSecuredPages`; access control is
menu-visibility (`pages-menu.ts:24` — `isOrgAdmin || isStoreAdmin`) plus the backend `@PreAuthorize`.
`.agents/plans/seller-ui-feature-inventory.md` §9 maps it.

| Screen | Route | Works? |
|---|---|---|
| Users list | `users` | yes — `userName`, `emailAddress`, `active`, actions. **No filters at all.** |
| Create user | `create-user/:store` | yes |
| User details | `user/:id` | yes |
| Change password | `change-password/:id` | **no** — see below |
| My profile | `profile` | **no** — see below |

Six defects in it that the port does not carry across:

- **Change password targets an endpoint that does not exist** (`PATCH /v1/private/user/{id}/password`;
  the source comment already admits "No matching controller was found"), and it passes
  `getUserId()`, which reads `localStorage['userId']` — a key **nothing in the codebase ever
  writes**. So it sends `null` as the subject and ignores its own `:id` route param.
- **My profile is not read-only.** `user-profile.component.html` writes `action="'VIEW'"` as a
  string attribute rather than a binding, so `action` is the literal `"'VIEW'"` and every
  `=== 'VIEW'` check fails: the form and its role checkboxes are editable on the profile screen.
- `change-password-form.service.ts` passes `{validator: …}` where Angular reads `{validators: …}`,
  so its `notSame` error never fires and the template's `hasError('notSame')` is dead.
- `UserManagementComponent.ngDoCheck()` reads `window.location.hash` on every change-detection tick
  to compute a side menu the template never renders.
- Untranslated English literals: the delete dialog's `'Are you sure!'` / `'Do you really want to
  remove this entity?'`, and `Repeat Password *`.
- The inline status checkbox mutates `rowData.active` in place rather than reloading.

### API surface to port

A new `src/app/api/tenancy/user-account.service.ts` and `src/app/api/tenancy/org-member.service.ts`,
joining the existing `api/tenancy/`. `?store=` is stamped by `CrudService` via `REQUEST_CONTEXT` —
**callers never pass it**, which is one of seller-core's bugs not carried over.

| New file | Endpoints |
|---|---|
| `api/tenancy/user-account.service.ts` | `GET /tenancy/api/v1/user-account/list` (`page`, `count`) · `GET …/find-one?userId=` · `GET …/assignable-roles` · `POST …/create` · `PUT …/update` · `POST …/reset?userId=` · `DELETE …/delete?userId=` · `POST …/enable?userId=` · `POST …/disable?userId=` |
| `api/tenancy/org-member.service.ts` | `GET /tenancy/api/v1/org-member/list` · `DELETE …/org-member?userId=` · `GET …/invitations` · `POST …/invitations?email=&role=` · `POST …/invitations/resend?email=&role=` · `POST …/invitations/revoke?invitationId=` · `POST …/invitations/accept?token=` |

**Not ported:** `user-account/current` (finding 2 — a 500 today and a 404 after the obvious fix),
and `UserService.updatePassword` (the URL does not exist).

Wire DTOs → **`models/users.ts`** (new): `ReadableUser`, `PersistableUser`, `UserPassword`,
`OrgMember`, `Invitation`, `CreatedInvitation`, `InvitationStatus`. View models →
**`models/team.ts`** (new): `TeamRow`, `InvitationRow`, `UsersTab`, `USERS_TABS`, `RoleName`,
`ROLE_LABEL_KEY`. The `checkout.ts`/`orders.ts` split Module 4 established.

**`src/app/models/user.ts` is deleted.** It is a stub from the Module 1 port that nothing imports,
and its `groups?` field describes a concept the platform does not have.

### Deviations

Each is a real defect found by porting under `strict: true`, per the standing rule.

1. **`lastAccess` and `loginTime` are dead fields.** They are on `ReadableUser` and set by no
   mapper; uaa has no `last_login` column. Not ported — which removes the template's "Last active".
2. **`defaultLanguage` is dead the same way.** On `UserEntity`, no uaa column, never read or
   written. Not ported.
3. **`UserPassword.password` is ignored.** `UserAccountServiceImpl.changePassword` sends only
   `changePassword` to uaa's admin reset; there is no current-password verification anywhere in the
   platform. The console therefore does **not** ask for a current password — a field nothing checks
   is a fixture standing in for a real answer.
4. **`repeatPassword` is never checked server-side.** Confirmed client-side only, as
   `lessons.md` "Auth — public signup validates nothing" already records for the sibling path.
   `@shared/validators/passwords-match` is reused rather than a fourth copy of `checkPasswords`.
5. **`createUser` is two calls, not one.** `client.createUser(...)` then
   `client.resetPassword(...)`. If the second fails the first has already committed, leaving a user
   with no password and no way to set one until `reset` works. Named in `lessons.md`.
6. **`enable`/`disable` post an `undefined` body** in seller-core, and pass `store` explicitly where
   `CrudService` then overwrites it. The port sends neither.
7. **seller-core's user models are optional on every field.** Hardened: `id`, `userName`,
   `emailAddress`, `active` and `roles` are required where the server always sends them; `org` and
   `store` are `string | null`, because a user with no store is exactly finding 3.
8. **`count`, not `size` — verified.** `tenancy-service/build.gradle:63` depends on
   `store-commons:autoconfigure`, whose `ServletWebConfig:38` registers
   `setSizeParameterName("count")`, and `ManagedUserAccountServiceImpl.list` forwards
   `pageable.getPageSize()` to uaa intact. seller-ui sends `count` while rendering `limit: 10` and
   its author assumed it was dropped; it is not. Still the **first thing to confirm in the network
   tab**, as Module 7 did.

### What gets built, block by block

### `/users` — from `console-template/User Management.dc.html`

The template merges staff and customers into one table. **The customer half is Module 9** and is out
of scope; `shell.nav.item.customers` stays routeless. What is built is the team half.

`app-page-header` + a two-tab `app-tab-switcher` (Team · Invitations), tab and page state in the
URL per the page contract, master–detail as the template draws it: the list on the left, a detail
rail on the right (`.split` from `@shared/styles/field.css`), the selected user carried as `?user=`.

| Block | Backing |
|---|---|
| Team table: user (name + email), username, roles, status, actions | **real** — `GET …/user-account/list` |
| Paging | **real** — `page`, `count` |
| Enable / disable a user | **real** — the two POSTs |
| Delete a user, behind `app-confirm-dialog` | **real** |
| Create and edit a user in the detail rail | **real** — `POST …/create`, `PUT …/update` |
| Reset a user's password | **real once the permission `case` lands** — `POST …/reset?userId=` |
| Role picker | **real** — `GET …/assignable-roles`, minus `SUPER_ADMIN` (finding 4) |
| Invitations tab: email, role, status, expires, created by | **real** — `GET …/org-member/invitations` |
| Invite, resend, revoke | **real** — the three POSTs |
| The one-time invitation link | **real** — `CreatedInvitationDto.token`, shown once in a dialog with `app-copy-field` |
| KPI tiles: team members, pending invitations | **real, derived** — `totalElements`, and a count of `PENDING` |
| KPI tiles: customers, orders placed | Module 9 / no revenue → **removed** |
| KPI tile: suspended | **real** — but only as a count of `active: false` on the current page, so **removed** rather than reported from a partial page |
| Search box ("Name or email") | uaa filters on metadata equality only → **removed** |
| Export CSV | no endpoint → **PDF instead**, via the existing `app-export-button`, as Orders and Payments resolved it |
| "Last active" column | dead field → **removed** (Deviation 1) |
| Avatar photo, phone, addresses, lifetime value, "148 actions", recent order activity | no source → **removed** |
| "Store access — All 3 stores" | a user carries **one** store in metadata → the rail shows that one store |

**Two things the rail says that the template does not.** A user whose `store` is null is an
org-level account the store-scoped list cannot show (finding 3) — the tab carries an
`app-notice-bar` saying the list is scoped to the open store. And the roles column is rendered
through the Module 4 known-set guard (`@shared/i18n/status-label`'s pattern, mirrored as
`ROLE_LABEL_KEY` in `models/team.ts`), so a role name the console has not seen humanizes instead of
taking the page down under Transloco's strict-missing handler.

### `/profile` — from `console-template/Account Profile.dc.html`

Three of the design's six tabs have a subject the console can name; two of those have no data.
What is built is small and true.

| Panel | Backing |
|---|---|
| Account: username, roles | **real** — `/api/v1/auth/me`, the only identity there is |
| A notice naming why the rest is blank, citing `lessons.md` | — |
| Preferences: interface language, theme | **real and client-side** — the existing `LocaleService` and theme service, today reachable only from the toolbar |
| Link to Billing | **real** — routes to the existing `/subscription` |
| Full name, job title, email, phone, timezone, date format, bio, avatar | no column, no endpoint → **removed** |
| Notifications (4 toggles) | no service → **removed**, cross-referencing "Shell — no notifications service" |
| Password "Change" | no user id for a self-reset (finding 2) → **removed**, with the reset living on `/users` |
| Two-factor, active sessions | zero backend hits → **removed** |
| Organization tab | `org-manager/find-one` is super-admin only → **removed**, cross-referencing "Shell — an org admin cannot read its own organization" |
| Subscriptions, usage, payment methods, invoices | already shipped at `/subscription` → **linked, not duplicated** |

The toolbar's `Profile` button routes to `/profile`. **The `Settings` button beside it is removed** —
there is no console-wide settings page and never will be, since settings are per-store and live at
`/store-management`. A button that goes nowhere is the same promise a bell with no feed was.

### `/accept-invitation` — no template

`?token=`, under `AuthShell` rather than `ConsoleShell`, guarded by `canAccessSecuredPages` **only**:
an invitee is authenticated but not yet a member of the org, so `consoleContext` and `requiresStore`
would both refuse them — which is precisely why `OrgMemberApi.accept` carries no `@PreAuthorize`
either. On success it routes to `/dashboard`; on `INVITATION.NOT_USABLE` it says the link has been
used or has expired, and offers to ask the inviter for a new one.

### Mapping table — old capability → new location

| seller-ui | console-ui |
|---|---|
| `user-management/users` | `/users`, Team tab |
| `user-management/create-user/:store` | The detail rail in create mode |
| `user-management/user/:id` | The detail rail, `?user={id}` |
| Inline `active` checkbox that mutates the row | `app-toggle`, reloading rather than echoing |
| Delete with an untranslated English dialog | `app-confirm-dialog`, translated |
| `user-management/change-password/:id` | The rail's **Reset password** action — the operation that screen was trying to perform |
| `user-management/profile` (editable by accident) | `/profile`, genuinely read-only |
| `nb-select` sub-navigation | `app-tab-switcher`, per the feature inventory's "replace with real tabs" |
| Role checkboxes fed straight from `assignable-roles` | Same, minus `SUPER_ADMIN` |
| — (seller-ui has none) | **New:** the Invitations tab and the whole invite flow |
| — | **New:** `/accept-invitation` |

Nothing seller-ui does is dropped except its two screens that have never worked.

### New components

**None expected.** `ARCHITECTURE.md` §4 already has everything: `page-header`, `panel`,
`tab-switcher`, `data-table` + `table-row`, `pagination`, `kpi-grid` + `kpi-card`, `badge`,
`form-field` with `text-field` / `select` / `checkbox` / `toggle`, `copy-field` (the one-time
token), `confirm-dialog`, `action-list`, `notice-bar`, `empty-state`, `busy-overlay`, `load-error`,
`export-button`. Check §4 before adding anything — the alignment pass exists to stop a sixth copy of
the field vocabulary.

The one possible addition is a feature-local `components/invite-dialog/`, and only if
`app-confirm-dialog` cannot host the projected email field and role select. Module 7 asked the same
question of its approve dialog and answered it in the feature, not in `shared/`.

### The one backend change

`store-commons/autoconfigure/.../CustomPermissionEvaluator.java`, one line beside its six siblings:

```java
case "STORE-CORE.USERS.RESET_PASSWORD" -> checker.hasAccessOnStoreUsersResetPassword(authentication, (StoreMerchantId) targetId);
```

with `hasAccessOnStoreUsersResetPassword` delegating to the existing private
`hasMaintainAccessOnUsers` — org admin or store admin, matching create/update/delete/enable/disable,
and deliberately **not** the moderator, who has read access only. A unit test asserts the token
resolves for both admins and refuses a moderator, so the next unmapped token fails a test rather
than a QA pass. `./gradlew checkstyleMain` runs at `maxWarnings = 0`.

**seller-ui, store-pod and every other service are untouched.**

### What stays unbacked → `lessons.md`

Appended after the existing 111, newest last, in the file's established format. Each pairs with a
`TODO(lessons.md):` marker at its call site; `npm run lint:lessons` enforces the citation.

1. **The JWT carries no user id, so the console cannot look itself up.** `sub` is the username;
   `JwtCustomizerConfig` adds `roles` and metadata and no id; every read-by-id ends at uaa's
   by-UUID lookup, and uaa exposes no get-by-username. Expected: add a `uid` claim, or give uaa a
   `GET /api/v1/admin/users/by-username`. **This is the root cause of the next two entries**, and it
   supersedes the standing "user-account/current is broken" entry, which is updated to say that
   fixing the `Principal`/`Jwt` binding turns a 500 into a 404 rather than into an answer.
2. **The gateway's OAuth client requests `openid` only.** `data-common.sql:64-77` — `scopes =
   'openid'`, no `profile`, no `email`. This is the precise, one-row cause of the already-logged
   "uaa's ID token carries no profile claims". Recorded with the row quoted, since it is the
   cheapest fix on this list.
3. **No self-service password change.** Only an admin reset exists, and it verifies no current
   password (`UserPassword.password` is read by nothing).
4. **`STORE-CORE.USERS.RESET_PASSWORD` was mapped nowhere.** Logged as the one **fixed** entry
   alongside the resolved dashboard-statistics outage, with the commit named — the file records what
   the backend could not do, and it should record when that stops being true.
5. **The user list is store-scoped, so an org admin is in no list.** Finding 3, with the
   `validateUserAccess` lines quoted. Expected: an org-scoped list, or treat a null `store` as
   "every store in the org".
6. **`assignable-roles` offers `SUPER_ADMIN` to an org admin.** Finding 4. The console filters it
   out client-side, which is defence in depth and not a fix.
7. **`StoreRoleAccessChecker.isOrgAdmin` does not verify the store belongs to the org** — it carries
   its own `@TODO`. Any org admin passes the permission check for any store id, with the real check
   done downstream in `validateUserAccess`. Defence in depth held, but the layer is doing less than
   it appears to.
8. **Creating a user is two calls and is not atomic** (Deviation 5).
9. **No user search of any kind.** uaa's list filters on metadata equality only; there is no
   name, email or username query. Kills the template's search box.
10. **A user has no last-login, no avatar, no phone, no job title, no timezone, no date format and
    no bio.** Ten designed fields, one table of eight columns behind them.
11. **No two-factor authentication and no session listing.** Zero hits repo-wide for
    `twoFactor`, `mfa`, `otp`; every `session` hit is HTTP-session config, a Stripe
    `CheckoutSession`, or a logout handler.
12. **No notification preferences.** Cross-references "Shell — no notifications service": the feed
    does not exist, so neither does the choice of what lands in it.
13. **No per-user activity log.** `tenancy_audit` records store and org lifecycle, keyed by actor,
    but nothing reads it per user and it does not cover catalogue or order actions. Kills "148
    actions" and "Recent order activity".
14. **No CSV export**, front or back. The header exports PDF.
15. **Nothing emails an invitation.** Stated by the platform itself in `CreatedInvitationDto`'s
    javadoc. Recorded not as a defect but as the constraint that shapes the invite dialog: the
    console must show the token once, because nothing else ever can.
16. **A `groups` concept is referenced and does not exist.**
    `extra/requests/store-ui/org-admin/users.http:16` calls `GET /user-account/groups`; there is no
    such endpoint, no groups table, and no role→permission mapping anywhere. A role is a bare name.

Cross-referenced rather than duplicated: "Shell — no user-preferences endpoint", "Shell — an org
admin cannot read its own organization", "Shell — no notifications service", "Auth — no password
reset, and no email verification", "Billing — entitlements are ceilings with no usage behind them"
(the design's "Team seats 6/15 — 2 invitations pending" meter).

### Implementation

Four layers per `ARCHITECTURE.md` §2 — page → facade → feature api service → `@api/*`.

- **`features/users/`** — `users.ts`/`.html`/`.css`, `components/user-detail/`,
  `components/invite-dialog/` (only if needed), `facades/users.facade.ts`,
  `services/users.api.service.ts`, `services/user-form.service.ts`. The facade is **provided by the
  page**. Two `snapshot()`s on deliberately different keys, as Module 7 established: the table
  (store + tab + page) and the KPI counts (store only), so switching tabs does not refire them.
  `isLoading`/`error`/`isEmpty`/`reload` come from `snapshot()`; `busy` is the in-flight write; the
  facade raises the toasts. Writes reload rather than echo — `enable`, `disable`, `delete` and
  `reset` all answer `void`.
- **`services/users.api.service.ts`** is the assembly point, following `payments.api.service.ts`
  closely: a `forkJoin` of the paged list and the optional legs, with **the user list as the
  unwrapped leg that is the page** and `assignable-roles` and the invitation list through the
  existing `optionalList`/`optionalOne` helpers, each with a comment naming why that leg may fail.
- **`features/profile/`** — `profile.ts`/`.html`/`.css`, `facades/profile.facade.ts`. No api
  service and no `@api/*` call: everything on the page comes from `AuthService`, `LocaleService` and
  the theme service. This is the first feature with no HTTP, and the facade says so.
- **`features/accept-invitation/`** — the token page, with `facades/accept-invitation.facade.ts`
  calling `OrgMemberService.accept`.
- **Client-side gating.** `AuthService.getRoles()` exists and **nothing calls it**. `/users` is the
  first page that should: the write actions render only for `isOrgAdmin || isStoreAdmin`, mirroring
  `hasMaintainAccessOnUsers`, so a moderator sees a readable list rather than buttons that 403.
  A small `@shared/` helper, not a copy per call site.
- **Routing** — `app.routes.ts`, under `ConsoleShell` with
  `[canAccessSecuredPages, consoleContext, requiresStore]` and `data: {titleKey, breadcrumbKey}`:
  `users`. `profile` takes the same guards **minus `requiresStore`** — a personal page is not
  store-scoped. `accept-invitation` sits under `AuthShell` with `canAccessSecuredPages` only.
  **`app.routes.server.ts` gains `users/**`, `profile/**` and `accept-invitation` as
  `RenderMode.Client`** — mandatory, or `app.routes.spec.ts` fails.
- **Nav** — `console-navigation.ts`: give the routeless `shell.nav.item.userManagement`
  `route: '/users'`, with the same one-line comment Modules 6 and 7 left on `inventory` and
  `payments`.
- **Toolbar** — `console-toolbar.ts`: `Profile` becomes a `routerLink="/profile"`; `Settings` is
  removed, and its `shell.profile.settings` key with it (`npm run lint:i18n` fails on an unreachable
  key, so this is not optional).
- **i18n** — a new `users.*` root and a `profile.*` root in `src/locale/{en,ar}.json` at exact key
  parity. Role names and `InvitationStatus` go through the known-set guard. Dates and counts through
  `TranslocoLocaleService`, never bare `Intl` or `DatePipe`. Emails, usernames and invitation tokens
  get `latin` (`unicode-bidi: plaintext`) inside the RTL page.
- **No fixture.** `src/app/mocks/` was removed in the alignment pass and nothing reintroduces it.

### Testing

Both UIs, same org admin, `ORG1-STORE1` open in each. The seeded accounts
(`uaa/init-sql/data-test-stores.sql`) give a moderator and a second store to test scoping with.

- The team list shows the same users as seller-ui's `users-list` — same usernames, emails, active
  flags, same total count.
- **Paging is the decisive check** — set the page size and confirm the server honours `count`
  (page 2 differs, the total is stable, the row count matches the requested size, not 20).
  See Deviation 8; seller-ui gets this wrong on screen.
- Create a user; confirm it appears in seller-ui's list after a reload, and that it can sign in.
- Edit a user's name and roles; disable and re-enable; delete — each round-tripped against
  seller-ui, and delete only against a throwaway account.
- **Reset a user's password and sign in as them.** This is the module's strongest evidence: it is
  the operation seller-ui's change-password screen has never been able to perform, and it works only
  because of the permission commit.
- **Invite `newbie@example.com`, copy the link, revoke it, invite again, resend, then accept in a
  private window as a second account.** Confirm: the token appears exactly once; `GET /invitations`
  never contains it; a duplicate invite is a 409 rendered as a field error, not a toast; a second
  accept says the link is used rather than failing silently; and the accepted user then appears in
  `org-member/list`. `qa/tenancy-and-pod-registry-split.md` LIF-04 is the reference run.
- Sign in as `org1-store1-moderator`: the list renders, and no write action does. Then confirm the
  backend agrees by calling `create` directly — it must be 403, not 200.
- Switch to `ORG1-STORE2` and confirm the list changes, and that `org1-admin` appears in neither —
  the notice-bar is what explains it (finding 3).
- `/profile` shows the username and roles, the language and theme controls actually change the app,
  and the Billing link lands on `/subscription`. The toolbar's Profile button opens it and no
  Settings button remains.
- A user with no roles, one with a role the console has not seen, an expired invitation, and a store
  with one user all render.
- Export the team list to PDF; check the rows against the table.
- Arabic and all three themes; 1440 / 900 / 420.

### Commits

1. `plan(console-ui): users and profile` — this section.
2. `fix(commons): the permission token that locked every password reset` — the
   `CustomPermissionEvaluator` case, `PermissionAccessChecker` method and its test. Its own commit
   because it is the one backend change in the module and should be revertable alone.
3. `feat(console-ui): the team list on real user accounts` — `api/tenancy/user-account.service.ts`,
   `models/{users,team}.ts`, `features/users/` Team tab, the route, the nav item, the KPI tiles, the
   role gate.
4. `feat(console-ui): invitations` — `api/tenancy/org-member.service.ts`, the Invitations tab, the
   invite dialog and its one-time link, `features/accept-invitation/`.
5. `feat(console-ui): the account page` — `features/profile/`, the toolbar wiring, the removed
   Settings button.
6. `fix(console-ui): users and profile after QA`.

Six rather than three: the backend fix, the two halves of `/users` and the profile page are each
independently reviewable, and folding the invitation flow into the team list would make the diff
unreadable.

### Critical files

**seller-ui: not modified. store-pod: not modified.** `store-commons/autoconfigure` **is** modified,
once, by decision — see "The one backend change".

**New:** `src/app/api/tenancy/{user-account,org-member}.service.ts` (+ specs),
`src/app/models/{users,team}.ts`,
`src/app/features/users/{users.ts,.html,.css,components/,facades/,services/}`,
`src/app/features/profile/{profile.ts,.html,.css,facades/}`,
`src/app/features/accept-invitation/{…}`,
`store-commons/autoconfigure/.../CustomPermissionEvaluatorTest` additions.

**Changed:** `src/app/app.routes.ts`, `src/app/app.routes.server.ts`,
`src/app/layouts/console-shell/console-navigation.ts`,
`src/app/layouts/console-shell/components/console-toolbar/console-toolbar.ts`,
`src/locale/{en,ar}.json`, `lessons.md`,
`store-commons/autoconfigure/.../CustomPermissionEvaluator.java`,
`store-commons/autoconfigure/.../PermissionAccessChecker.java`.

**Deleted:** `src/app/models/user.ts` — an unused stub describing a `groups` concept that does not
exist.

**Reused, already present:** `core/http/crud.service.ts` (`?store=` stamping),
`core/http/{optionalOne,optionalList}`, `core/table/table.types.ts` (`PageT`, `PageRequest`,
`count`), `core/auth/auth.service.ts` (`getRoles()`, used for the first time),
`shared/state`'s `snapshot()`, `shared/validators/passwords-match`,
`shared/forms`' `applyToForm` + `clearServerErrorsOnChange` and `formDirty`,
`shared/i18n/status-label`'s known-set pattern,
`core/export/pdf-export.service.ts` + `shared/ui/export-button`, and the §4 control catalogue.
`features/payments/` is the closest sibling — page, facade, api service and template all follow it.

### Verification

1. `cd store-core/console-ui && npm run build && npm run lint && npm run test:ci`;
   `./gradlew :store-commons:autoconfigure:test checkstyleMain`;
   `git -C store-core/seller-ui status --porcelain` and `git -C store-pod status --porcelain` both
   empty.
2. `grep -rn "user-account/current\|private/user/.*password\|localStorage.*userId" src` → no hits.
   None of seller-ui's three dead paths survives the port.
3. `grep -rn "lastAccess\|loginTime\|defaultLanguage" src` → no hits. No dead field ported.
4. `grep -rn "avatar\|twoFactor\|jobTitle\|timezone\|notificationPreference" src/app/features/profile` →
   no hits. None of the template's unbacked vocabulary leaked in.
5. `grep -rn "SUPER_ADMIN" src/app/features/users` → only the filter that removes it from the picker.
6. `grep -rn "TODO(lessons.md)" src` — every marker has a matching heading and every new heading a
   marker. `npm run lint:lessons` enforces it.
7. `src/app/app.routes.spec.ts` passes — proof the three new render-mode entries exist.
8. Network tab: every private call carries `?store=`; **the list response honours `count`**; the
   reset `POST` is `…/user-account/reset?store=&userId={uuid}` with a `{changePassword}` body and
   the id is the **UUID from the row, not the username**; `GET …/org-member/invitations` never
   contains a token; no request fires twice per page load.
9. Specs: `users.api.service.spec.ts` following `payments.api.service.spec.ts` (a fake that pages
   the way the server would), `HttpTestingController` specs for both new api services asserting URL,
   verb, params and body, and page specs for `/users` and `/profile` asserting against rendered
   English copy.
10. The two-tab comparison above, driven through Chrome — in particular the password reset and the
    invitation round-trip, this module's strongest evidence, because neither is something the old
    console can do at all.

## Module 12 — Content — **done**

Planned and built under `.agents/plans/console-ui-content.md`: a new `store-pod/content` service
(the old one kept as `store-pod/content-deprecated`), the console-ui Content module at `/content`
(pages, posts, banners, FAQ, media, menus, policies) and the storefront adoption in landing-ui
(site document, blog, help, policies, banners, sitemap). Phase 7 of that plan — retiring the
legacy compat API, `store-pod/content-deprecated` and the `Page/Box` types — rides with Module 13.

## Modules 9–11 and 13 — **done**

Shipped without headings of their own in this document: Module 9 (customers), Module 10 (billing,
as the `/subscription` page), Module 11 (the platform console: dashboard, organizations, pods,
plans, platform users and billing). Module 13 — retire seller-ui — was planned as
`.agents/plans/console-ui-retire-seller-ui.md` and landed 2026-08-24: `store-core/seller-ui` is
deleted (last commit tagged `seller-ui-final`), the gateway apex serves console-ui, and content
Phase 7 (the legacy compat surface) went with it. The migration this document tracked is complete.

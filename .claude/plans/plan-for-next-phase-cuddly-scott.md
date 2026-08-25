# Module 7 — Payments

## Context

Six modules of the seller-ui → console-ui migration have shipped, plus the alignment pass that gave
the app `ARCHITECTURE.md`. The named order in
`.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md` puts **payments** next, and
the console's nav rail already carries a routeless `shell.nav.item.payments` — this app's marker for
"not built". This module gives it a route.

The seller has no way, in the new console, to see the money behind an order or to confirm a manual
bank transfer. seller-ui has exactly one payments screen (`/pages/payment/payment-list`) and the
feature inventory calls its approve/reject flow *"a genuinely important operator task that is
currently buried in a table row"* (§14, gap #10). That task is the reason this module exists.

**The finding that shapes it.** The payment backend is the *smallest* on the platform and the design
asks it for more than any other template. Verified by reading every controller under
`store-pod/payment/payment-service/.../api/` and grepping the whole repo:

- **The entire seller-facing HTTP surface is three endpoints**: a filtered paged transaction list,
  `approve`, and `reject`. Plus gateway-credential CRUD, which Module 5 already ported and ships.
- **There is no aggregate endpoint of any kind.** `grep -i statistic` across `store-pod/payment`
  returns zero hits. No counts, no sums, no group-by, no series.
- **`payout`, `settlement`, `chargeback`, `gatewayFee` have zero backend hits repo-wide.** Every
  occurrence of `payout` (16) and `chargeback` (1) is inside `console-template/*.dc.html`. The
  template's Payouts panel, dispute tile, gateway-fee column and settlement summary are drawn from
  nothing at all.
- **`PaymentStatus.WAITING_VERIFICATION` is never set.** `ManualTransferredProcessor.initiate`
  returns `PENDING`. The enum constant appears in exactly two Java files: its own declaration and one
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

## Decisions (settled with the user)

| Question | Decision |
|---|---|
| KPI row | **Count-only tiles.** Four honest integers — Awaiting approval, Captured, Failed, Refunded — each a one-row fetch read off `totalElements`. Relabelled as counts, never amounts. Money sums get a `lessons.md` entry. |
| The order link | **Both directions.** The ledger's Order column links to `/orders/{requestRef}`, **and** the existing order-details page gains a Payments panel listing that order's transactions via `?requestRef={id}`. Closes the standing gap from both sides at zero backend cost. |
| The approval queue | **Fix the console, log the backend gap.** Key the queue on `status=PENDING` + `paymentType=MANUAL_TRANSFER`, correct the Module 3 dashboard tile to match, and record that the backend should set `WAITING_VERIFICATION`. |
| Gateway config | **Stays in store-management.** `/payments` is the ledger only; its header action routes to `/store-management/payments`, exactly as the template's primary action does. |

Standing rules unchanged: **seller-ui and store-pod are not modified**; no fixture stands in for a
real answer; every unbacked block carries `TODO(lessons.md)`; `lessons.md` is append-only.

---

## 1. seller-ui today

`/pages/payment/payment-list` — the only screen. Guarded by nothing but
`canAccessSecuredPages`; access control is menu-visibility only (`pages-menu.ts:8-10`,
orgAdmin / storeAdmin / storeModerator). The backend `@PreAuthorize(… 'STORE-POD.PAYMENT.*')` is the
real gate.

Filters: status, payment type, `requestRef`, `internalRef`, date from, date to, with Search and
Reset. Columns: internalRef, requestRef, amount + currency, payment type, **raw ISO transaction date
with no pipe**, status, transactionNo, actions. Approve opens a dialog asking for the external
transaction number; **Reject fires on one click with no confirmation at all** — an asymmetry worth
fixing. Filters are not URL-persisted, and `onFilterChange` does not reset the page offset.

Also payment-shaped, and out of scope: store payment configuration (Module 5, shipped), and
order-details' Refund / Capture buttons and Transactions dialog — all three already recorded in
`lessons.md` as endpoints that have never existed.

## 2. API surface to port

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

### Deviations

Each is a real bug found by porting under `strict: true`, per the standing rule.

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

## 3. What gets built, block by block

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
external `transactionNo`; **check whether `app-confirm-dialog` can host a projected required field
before building anything** (ARCHITECTURE.md §4 is the catalogue, and a sixth copy of the field
vocabulary is what that rule exists to stop). If it cannot, add a feature-local
`components/approve-dialog/` — not a shared component, since only this page approves. **Reject gets
a confirm dialog**, closing seller-ui's one-click asymmetry.

## 4. Mapping table — old capability → new location

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

## 5. New components

**None expected.** Everything the honest page needs is in ARCHITECTURE.md §4 already:
`page-header`, `kpi-grid` + `kpi-card`, `panel`, `tab-switcher`, `select`, `search-box`,
`date-range-picker`, `data-table` + `table-row`, `badge`, `pagination`, `action-list` (the ⋯
overflow), `confirm-dialog`, `copy-field`, `notice-bar`, `empty-state`, `busy-overlay`,
`load-error`, `export-button`. The one possible addition is the feature-local approve dialog above,
and only if `confirm-dialog` cannot project a field.

The two shared components the mockup would have needed — `charts/bar-chart` and `ranked-list` —
already exist and are **not** used here, because the volume series and the gateway split have no
data behind them.

## 6. What stays unbacked → `lessons.md`

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
affordance. Noted here as a deliberate omission.

## 7. Implementation

Four layers, per ARCHITECTURE.md §2 — page → facade → feature api service → `@api/*`.

- **`features/payments/`** — `payments.ts` / `.html` / `.css`, `facades/payments.facade.ts`,
  `services/payments.api.service.ts`, and the approve dialog if one is needed. The facade is
  **provided by the page**, never `providedIn: 'root'`. `isLoading` / `error` / `isEmpty` / `reload`
  come from `snapshot()`; `busy` is the in-flight approve or reject; the facade raises the toasts.
  Two `snapshot()`s: the table (keyed on store + tab + filters + page) and the KPI counts (keyed on
  store + range only).
- **`services/payments.api.service.ts`** is the assembly point, following
  `orders.api.service.ts` closely: `forkJoin` of the paged list and the four optional counts,
  mapping `PaymentTransaction` → `TransactionRow`, and translating the page's filter into
  `TransactionQuery`. Writes reload rather than echo — both endpoints answer `void`.
- **Routing** — `app.routes.ts`: `payments` under `ConsoleShell` with
  `[canAccessSecuredPages, consoleContext, requiresStore]` and
  `data: {titleKey, breadcrumbKey}`. **`app.routes.server.ts` gains `payments/**` →
  `RenderMode.Client`** — mandatory, or `app.routes.spec.ts` fails; `SelectedStoreRequestContext`
  throws during SSR. Any route param is validated before it reaches a facade.
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
  through `@shared/i18n/money`, dates and counts through `TranslocoLocaleService` — never bare `Intl`.
  Refs, UUIDs and order numbers get `latin` (`unicode-bidi: plaintext`) inside the RTL page.
- **No fixture.** `src/app/mocks/` no longer exists and nothing reintroduces it.

## 8. Testing

Both UIs, same org admin, `ORG1-STORE1` open in each.

**Generating test data first**, since a fresh store has no transactions and the approval queue is the
point of the page:

1. In store-management → Payments, enable **MANUAL_TRANSFER** for `ORG1-STORE1`.
2. Place orders on the live storefront at `http://org1-store1.spg-507f1f77.gateway.com/ar`, choosing
   manual transfer — each one writes a `PENDING` transaction whose `requestRef` is the new order id.
   Place at least one Stripe/COD order too, so the gateway filter has more than one value.

Then:

- The ledger shows the same transactions as seller-ui's `payment-list` — same refs, amounts,
  types, statuses, same total count.
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
  transaction reads `REJECTED` while the order *does not* move — that is finding 9, observed rather
  than assumed.
- The KPI counts match the tab strip's own filtered totals, and switching tabs does not refire them.
- Awaiting-approval count is non-zero after step 2 — the thing the Module 3 tile could never show.
- The dashboard's "payment approvals waiting" figure now agrees with the payments page.
- A transaction with a null `transactionNo`, one with a status the console has not seen, and a store
  with no transactions at all all render.
- Export the ledger to PDF; check the figures against the table.
- Arabic and all three themes; 1440 / 900 / 420.

## 9. Commits

1. `plan(console-ui): payments` — the Module 7 section appended to
   `.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md`.
2. `feat(console-ui): the transaction ledger on real payments` — the read half, the route, the nav
   item, the KPI counts, the order link.
3. `feat(console-ui): approve and reject a manual transfer` — the two write paths, their dialogs, the
   order-details panel, and the dashboard queue correction.
4. `fix(console-ui): payments after QA`.

Four rather than three: the reads and the writes are independently reviewable, and the write half
reaches outside the new feature into the dashboard and order details.

---

## Critical files

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
`src/app/features/order-details/services/order-details.api.service.ts` and
`order-details.html` (the Payments panel), `src/app/app.routes.ts`,
`src/app/app.routes.server.ts`, `src/app/layouts/console-shell/console-navigation.ts`,
`src/locale/{en,ar}.json`, `lessons.md`.

**Deleted:** nothing. There is no payments fixture — `src/app/mocks/` was removed in the alignment
pass.

**Reused, already present:** `core/http/crud.service.ts` (`?store=` stamping),
`core/http/{optionalOne,optionalList}`, `core/table/table.types.ts` (`PageT`, `PageRequest`, `count`),
`shared/state`'s `snapshot()`, `shared/i18n/{money,status-label}.ts`,
`models/store-settings.ts`'s `isPaymentType` / `PAYMENT_TYPE_LABEL_KEY`,
`core/export/pdf-export.service.ts` + `shared/ui/export-button`, and the §4 control catalogue.
`features/orders/` is the closest sibling — page, facade, api service and template all follow it.

## Verification

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
   response honours `count`**; the approve POST is
   `…/payment/transaction/{internalRef}/approve` with a `{transactionNo}` body and the ref is the
   **UUID, not the numeric id**; no request fires twice per page load, and switching tabs fires one
   request, not five.
7. Specs: `payments.api.service.spec.ts` following `orders.api.service.spec.ts` (a fake that filters
   and pages the way the server would), a `payments.spec.ts` page spec asserting against rendered
   English copy, and `HttpTestingController` specs for the two new api methods asserting URL, verb,
   params and body.
8. The two-tab comparison above, driven through Chrome — in particular the approve round-trip, this
   module's strongest evidence, because it is the one operator task the old console buried.

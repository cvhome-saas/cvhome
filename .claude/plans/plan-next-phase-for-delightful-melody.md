# Module 9 — Customers

## Context

Eight modules of the seller-ui → console-ui migration have shipped. The named order in
`.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md:210` puts **customers** next,
and `console-navigation.ts:28` still carries a routeless `shell.nav.item.customers` — this app's
marker for "not built". Module 8 deferred the customer half of the User Management design here
explicitly (`models/team.ts:16-17`). This module gives it a route.

It inherits a debt, too. `order-details.ts:184`'s `viewProfile()` is a button that raises
`"Customer profiles are not available yet."` and does nothing else, because until now there was no
screen to send anyone to.

**What reading the backend turned up.** seller-ui's customers page is, in the feature inventory's own
words, *"the thinnest real feature in the app"* (§12): one read-only table, four columns, no filters,
no detail. The reason is not that the data is thin — it is that **the capability exists one layer
down and no controller ever wired it up**:

- `CustomerRepository.findByStoreMerchantId` (`customer-core/.../repositories/customer/CustomerRepository.java:38-66`)
  implements filtering on `name`, `firstName`, `lastName`, `email` and `country`.
  **`CustomerApi.list` binds none of them** — it constructs a bare `CustomerCriteria` and sets only
  the pageable (`CustomerApi.java:50-52`). Dead capability, reachable by nothing.
- Inside that repository, the `name` branch is **broken independently**: `cb.or(...)`'s result is
  computed and thrown away, never added to `predicates` (`CustomerRepository.java:47-50`). So even
  once bound, a name filter would silently match everything.
- The same pattern again in orders: `OrderCriteria.customerId` is honoured by the repository
  (`OrderRepository.java:85-87`) and **exposed as no request param** (`OrderApi.java:194-198`).
  "The orders of customer X" is therefore only askable by fuzzy email today.
- **`firstName`/`lastName` on the DTO come from the billing address, not the customer.**
  `ReadableCustomerPopulator.applyBilling` sets `target.setFirstName(billing.getFirstName())`; the
  customer's own name columns are never mapped. A customer with no billing address has no name.
- **There is no `GET /private/customers/{id}`.** `CustomerFacade.getCustomerById` exists and is
  exposed by nothing. The only detail endpoint, `/private/customer/info`, is gated
  `STORE-POD.CUSTOMER.*`, which `CustomPermissionEvaluator` resolves to
  `isCustomerInSameStore` — a **shopper** token. A seller JWT is rejected by it.
- No per-customer aggregate of any kind exists. `customer-statistic` counts orders grouped by
  billing country, which `lessons.md:421` already records.
- `customer_group`, `CustomerReview` and `CustomerGender` have DDL and entities and **zero
  references outside their own packages**. Legacy leftovers, not features.

So this is the module where the useful thing to do is not to port a screen but to **turn on
capability that is already written and unreachable**, and then build the page the design draws on top
of it.

## Decisions (settled with the user)

| Question | Decision |
|---|---|
| The unbound filters | **Bind them, in checkout, in their own commit.** The same deliberate departure from "store-pod is not modified" that Module 8 made for the `RESET_PASSWORD` permission `case`. Without it the page has no search over thousands of people and no deep link, and `viewProfile()` stays a dead toast. |
| Page shape | **Master–detail, as `User Management.dc.html` draws it** — the list on the left, a detail rail on the right carrying the addresses and that customer's orders. |
| Blank names | **Shown blank, as seller-ui does.** No invented email fallback in the name slot; the email is on its own line in the same cell, so a nameless row still identifies itself. The populator defect is recorded, not worked around. |
| Lifetime money figures | **Em dash, per the standing decision** in `lessons.md:562` "Orders — no customer analytics". Summing the orders on screen would be a different number under the same label. |

Standing rules unchanged: **seller-ui is not modified**; no fixture stands in for a real answer;
every unbacked block carries `TODO(lessons.md)`; `lessons.md` is append-only and its headings are
load-bearing strings (`lint:lessons` fails on a citation that names no heading).

---

## 1. seller-ui today

`src/app/pages/customer/` — one route (`list`), one component, one facade, one service, and an empty
`.scss`. Guarded by nothing but the parent `canAccessSecuredPages`; access control is
menu-visibility only (`pages-menu.ts:20-22`, `isOrgAdmin || isStoreAdmin`). The backend
`@PreAuthorize(… 'STORE-POD.CHECKOUT.*')` is the real gate.

Columns: `Id`, `firstName`, `lastName`, `emailAddress`. No filters, no search, no sorting, no
actions, no dialogs, no forms, no detail view. Spinner while loading; ngx-datatable's default "No
data to display" for empty; a toast on error. Paging is server-side and correct.

Nothing else in seller-ui is customer-owned. Customer data surfaces only inside orders and the
dashboard's country donut.

**Not carried across:** the `CUSTOMERS.CREATE_CUSTOMER` / `UPDATE_CUSTOMER` / `GROUPS` / `DETAILS`
i18n keys, which are orphans referenced by no template — leftovers from a screen that was never
built.

## 2. The backend commit

Three changes, all of the same kind: **wire up a capability the layer below already implements.** No
new endpoint, no new query, no schema change.

| File | Change |
|---|---|
| `checkout-service/.../api/order/v1/customer/CustomerApi.java` | `list` gains `@RequestParam(required=false)` for `name`, `firstName`, `lastName`, `email`, `country` and sets them on the `CustomerCriteria` it already builds. |
| `customer-core/.../repositories/customer/CustomerRepository.java` | Repair the dropped predicate at `:47-50` — `predicates.add(cb.or(...))` — and extend the `or` to cover `emailAddress`, so `name` is the single "name or email" query the design's one search box needs. The other four stay exact-field filters. |
| `checkout-service/.../api/order/v1/order/OrderApi.java` | `list` gains `@RequestParam(required=false) Long customerId` → `orderCriteria.setCustomerId(customerId)`. The repository already honours it (`OrderRepository.java:85-87`). |

Why `name` must span email: the criteria predicates are AND-ed, so one box sending both `name=` and
`email=` would match nothing. Making `name` the OR is the repair the broken line was reaching for.

Tests beside the existing ones in checkout: a filtered list narrows, an unfiltered one does not, and
`customerId` on the order list returns only that customer's orders.

## 3. Console API surface

New `src/app/api/customers/customers.service.ts` — the directory `…:124` of the migration doc
already reserves.

| Method | Call |
|---|---|
| `list(query: CustomerQuery)` | `GET /spg/checkout/api/v1/private/customers` — `page`, `count`, plus the newly bound `name`, `email`. `store` is stamped by `CrudService` via `REQUEST_CONTEXT`; **callers never pass it**, which is one of seller-core's bugs not carried over. |

`count`, not `size` — verified, not assumed: `checkout-service/build.gradle:69` depends on
`store-commons:autoconfigure`, whose `ServletWebConfig:36-39` registers `setSizeParameterName("count")`
platform-wide. Still the first thing to confirm in the network tab, as Modules 7 and 8 did.

`api/orders/orders.service.ts`'s `OrderQuery` gains `customerId?: number`. No new service — the rail
reads the orders list that is already ported.

**Wire DTOs are reused, not redefined.** `ReadableCustomer` and `CustomerAddress` are already in
`@models/checkout` (`:102-115`, `:72`) from Module 4. New view models go in **`models/customers.ts`**:
`CustomerRow`, `CustomerDetail`, `CustomerOrderRow` — the `checkout.ts`/`orders.ts` split Module 4
established.

## 4. What gets built, block by block

### `/customers` — from `console-template/User Management.dc.html` (the customer half)

There is no `Customers.dc.html`. The design lives inside the User Management artboard, which merges
staff and buyers into one page; Module 8 built the staff half and left this one. Same anatomy:
`app-page-header`, master–detail over `.split` from `@shared/styles/field.css` — **pulled in via the
feature's own `styleUrl`, not globally**, which is the bug Module 8's QA found (`users.ts:88-93`).

Selection and search both live in the URL, per the page contract: `?customer=` for the open rail and
`?q=` for the search term.

| Block | Backing |
|---|---|
| Table: id, customer (name over email), location (city, country) | **real** — every field is on `ReadableCustomer` |
| Paging | **real** — `page`, `count`, `totalElements` |
| "Name or email" search box, debounced | **real once the backend commit lands** — `name=` |
| Detail rail: monogram, name, email, id, username | **real** — from the selected row; there is no get-by-id to call |
| Rail facts: company, phone, country | **real** — from `billing` |
| Addresses card: billing and delivery | **real** — both are inline on the DTO |
| Orders card: the customer's recent orders, each linking to `/orders/{id}` | **real** — `GET …/private/orders?customerId={id}&count=5` |
| "Orders placed" count | **real and exact** — the order query's own `totalElements` |
| "View all orders" → `/orders?customerId={id}` | **real** |
| Lifetime value, returns, return rate | no aggregate → **em dash** under the existing "not available" note, per `lessons.md:562` |
| KPI row (customers, +38 this month, orders placed, suspended) | only the first has a source, and it is the list's own `totalElements` → **no KPI row**; the count goes in the page header's `context` and the pagination footer |
| Status badge (Active / Invited / Suspended) | a customer record has no enabled flag anywhere → **removed** |
| "Buys from" (which stores) | the list is already store-scoped → **removed** |
| Multiple addresses, VAT/TRN tags | two fixed slots, no tax-id field → **removed** |
| Email action, Edit details, Export CSV | no mail service, no customer write endpoint, no CSV producer → **removed**; export is the existing `app-export-button` (PDF from the DOM), as Orders, Payments and Users each resolved it |
| Sorting | no whitelist, `Criteria.orderBy` unused for customers → **removed** |
| Create / edit / delete a customer | no write endpoint exists → **removed** |

Two things the page says that the design does not. A customer with no billing address renders a
**blank name** — faithful to seller-ui, and the reason is named in the rail rather than papered over.
And the search matches on the billing name, so such a customer is findable **by email only**; the
search box's hint says so.

### `order-details` — the dead button becomes real

`viewProfile()` (`order-details.ts:184`) stops raising a toast and navigates to
`/customers?q={customer.emailAddress}`. The facade auto-opens the rail when a filtered page returns
exactly one row, so the operator lands on the customer, not on a filtered list of one. Its
`TODO(lessons.md)` marker and the `orderDetails.profileNotAvailable` key are removed;
`customerStats`' separate marker stays, because lifetime figures still have no source.

### Files

```
store-core/console-ui/src/app/
  api/customers/customers.service.ts            (+ .spec.ts, HttpTestingController on URL/verb/params)
  api/orders/orders.service.ts                  OrderQuery gains customerId
  models/customers.ts                           CustomerRow, CustomerDetail, CustomerOrderRow
  features/customers/
    customers.ts / customers.html / customers.css / customers.spec.ts
    facades/customers.facade.ts                 provided by the page, never providedIn:'root'
    services/customers.api.service.ts           (+ .spec.ts) DTO → view model, forkJoin + optionalOne
  features/order-details/order-details.ts|html  viewProfile becomes a navigation
  layouts/console-shell/console-navigation.ts   the customers item gains route: '/customers'
  app.routes.ts                                 + app.routes.server.ts ('customers/**' — the spec fails without it)
  locale/en.json, locale/ar.json                a new `customers` block, both files
store-core/console-ui/lessons.md                appended entries (§5)
```

The facade follows `users.facade.ts` exactly: `snapshot()` for the list gated on
`shell.currentStoreId()`, `pageIndex` as a `linkedSignal` sourced on the store so a switch resets to
page 0, a second `snapshot()` for the rail's orders gated on the selected id, `isLoading`/`error`/
`isEmpty`/`reload` names unchanged, toasts raised in the facade and not the component.

No permission gate. The page is read-only and `STORE-POD.CHECKOUT.*` is the same authority the
already-shipped orders page runs under — `ConsolePermissions` gains nothing.

## 5. lessons.md entries

Appended, in this order, each with a `TODO(lessons.md)` marker at its call site:

1. **Customers — no customer detail endpoint.** `CustomerFacade.getCustomerById` exists and is
   exposed by nothing; `/private/customer/info` is shopper-token-scoped. Expected contract:
   `GET …/private/customers/{id}`. Meanwhile the rail renders from the loaded row, which is why a
   deep link goes through the search term and not through an id.
2. **Customers — a customer's name comes from the billing address.**
   `ReadableCustomerPopulator.applyBilling` overwrites `firstName`/`lastName` from billing and never
   maps the customer's own columns. Expected contract: map them, falling back to billing. Two visible
   consequences: blank names, and a customer findable by email only.
3. **Customers — no lifetime or per-customer aggregate.** Cross-references the existing
   "Orders — no customer analytics" contract (`GET …/customers/{id}/summary`) rather than restating
   it; that heading must not be renamed.
4. **Customers — no created-at on the DTO.** The `audit` column exists (`schema.sql:85-86`) and the
   populator never maps it, so the console cannot show "customer since" or the design's
   "+38 this month".
5. **Customers — no groups, tags, notes or reviews.** `customer_group` DDL, `CustomerReview` entities
   and repository, and `CustomerGender` all exist with zero references outside their own packages.
   Named so the next person does not mistake them for working features.
6. **Customers — no write endpoint and no mail service.** Create, edit, delete and the design's
   "Email" action.
7. **Checkout — dead filter capability, now wired.** A short record of what the backend commit turned
   on and why, so the departure from "store-pod is not modified" is on the record alongside Module 8's.

## 6. Verification

Both apps against the same backend, as the requirements prescribe — seller-ui.gateway.com:8000 and
console-ui.gateway.com:8000 in two tabs, signed in as the org admin.

1. `npm run lint` (which runs `lint:i18n` and `lint:lessons`), `npm run test`, `npm run build` in
   `store-core/console-ui`. Checkout's tests for the backend commit.
2. **Parity.** The console list and seller-ui's `/pages/customer/list` show the same customers, same
   ids, same order, same total. Page 2 on both.
3. **The `count` param.** Network tab: the request carries `page` and `count`, and changing the page
   size changes the number of rows returned. This is the check that has caught something in two
   previous modules.
4. **Search.** A term matching a first name, a last name and an email each narrow the list, and the
   result count agrees with what the equivalent order-list filter finds. A term matching nothing
   gives the empty state, not an error. Confirm on the wire that `name=` is sent and honoured — this
   is the branch that was dead before the backend commit.
5. **The rail.** Open a customer with orders: the order count matches what `/orders?customerId={id}`
   returns, and each order link lands on the right order. Open a customer with none: the orders card
   shows its empty state, not a spinner.
6. **A customer with no billing address** — blank name, email still visible, rail's addresses card
   honest about having nothing.
7. **The round trip.** From an order's customer panel, "View profile" opens `/customers` with the
   rail already open on that customer. Back, and the order is unchanged.
8. **Store switching** resets to page 0 and clears the rail; the list reloads scoped to the new store.
9. **Reload with `?customer=` and `?q=` set** restores the same view.
10. Arabic: `/customers` in RTL, no untranslated keys, dates and counts through `TranslocoLocaleService`.
11. A store admin and a store moderator both reach the page (it is `STORE-POD.CHECKOUT.*`, the same
    gate `/orders` already runs under).

## 7. Commits

Per the requirements' one-commit-per-phase rule, plus the backend change on its own as Module 8 set
the precedent:

1. `plan(console-ui): customers` — this file.
2. `fix(checkout): bind the customer list filters and the order list's customerId` — §2, with tests.
3. `feat(console-ui): customers on real APIs` — §3, §4, §5.
4. `fix(console-ui): customers after QA` — whatever §6 turns up.

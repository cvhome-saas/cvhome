# Billing, as the platform sees it

## Context

A super admin has **no visibility into money at all**. Not a revenue figure, not a plan count, not
an invoice, not a single line of the subscription audit trail. `/platform` is two counts of signups,
and `/platform/plans` is a price list — the console's own doc comment calls it "the only platform-wide
thing billing will answer", and that is accurate.

**The data is all there.** Reading `billing/billing-service/src/main/resources/schema.sql` end to end:

| Asked for | Already recorded in |
|---|---|
| money | `subscription_invoice` — `amount_due`, `amount_paid`, `currency`, `status`, `paid_at` |
| plan statistics | `store_subscription.plan_id` + `status`, priced by `plan_price.unit_amount` |
| subscription audit | `subscription_audit` — 16 event types, from/to status, from/to plan, `source`, `actor`, `occurred_at`, append-only |
| invoices | `subscription_invoice`, with Stripe's own hosted + PDF URLs |

What is missing is the **read path**, and it is missing in two different ways:

1. **No aggregate exists.** Nothing in billing sums money or counts subscriptions. `grep` for
   `subscription-statistic` across the repository returns nothing — and seller-ui's admin dashboard
   has been calling that 404 since it was written.
2. **No audience exists.** Every human-facing billing endpoint is guarded by
   `hasPermission(#store,'StoreMerchantId','STORE-CORE.BILLING.READ'|'.MANAGE')`, and
   `PermissionAccessChecker` resolves both to checkers with **no super-admin branch**. A platform
   operator is refused for every store on the platform.

`subscription_audit` is the sharpest case: `SubscriptionAuditService` is a **write-only interface**,
`SubscriptionAuditRepository` has exactly one method, and nothing anywhere calls it. Every plan
change and payment failure since the service was written is sitting in that table, unread.

Module 11 already wrote the contracts for four of these gaps in `lessons.md` —
*"Platform — no subscription statistics"*, *"Platform — no revenue or GMV figure anywhere"*,
*"Platform — a store's subscription cannot be read by an operator"*, *"Platform — no view of stores
billing has cut off"*. **This plan implements them**, plus the audit read and the invoice ledger,
which were not recorded because the screens for them did not exist yet.

Outcome wanted: an operator can answer "what is this platform earning", "who is on what", "why did
this store's plan change", "did this merchant actually pay" — and can act on a subscription when the
answer is that something is wrong.

### Settled with the user

1. **See everything; act on subscriptions.** Change plan, cancel (scheduled *or* immediate — the
   super-admin-only branch `SubscriptionApi.cancel` already has and the console has never called),
   resume. **No new money operations** — no comp, no credit note, no trial extension. Those need
   real Stripe calls and new audit event types; they are recorded as a gap instead.
2. **Billing tokens only.** The super-admin branch goes on `hasAccessOnBillingRead` and
   `hasAccessOnBillingManage` — **not** on the shared `hasReadAccessOnStore` /
   `hasMaintainAccessOnStore`. A super admin gains billing on any store and nothing else; the
   merchant screens hidden in the previous change stay 403.
3. **A new `/platform/billing` section, and enrich what exists.**

---

## Backend — billing

### Security, in three lines and one annotation

`store-commons/autoconfigure/.../s2s/services/PermissionAccessChecker.java`:

```java
public boolean hasAccessOnBillingRead(Authentication a, StoreMerchantId store) {
    return storeRoleAccessChecker.isSuperAdmin(a) || hasReadAccessOnStore(a, store);
}
public boolean hasAccessOnBillingManage(Authentication a, StoreMerchantId store) {
    return storeRoleAccessChecker.isSuperAdmin(a) || hasMaintainAccessOnStore(a, store);
}
```

The private `hasReadAccessOnStore` / `hasMaintainAccessOnStore` are **not** touched, so nothing but
billing widens. `isSuperAdmin` already exists and `hasAccessOnPodRead` already uses it — this is the
pattern billing simply never adopted. Nothing else is needed on the query side: both
`SubscriptionApi.tenantScopeOf` and `InvoiceApi`'s twin already answer `null` for a super admin, so
the org predicate drops away on its own. That code has been unreachable since it was written.

**`hasAccessOnBillingQuotaCheck` and `ExternalEntitlementApi` are not touched at all.** The
`lessons.md` contract proposed widening that checker so a human could call `blocked-stores`. It did
not notice that the same token gates `ExternalStoreQuotaApi.private/provision`, which **creates a
subscription** and claims the org's one trial through `org_trial_grant` — a primary key, so a trial
consumed for a mistyped store id can never be reclaimed. That is a one-way door reached through what
reads like a widening for a report.

It is also unnecessary. `blockedStores` answers `List<StoreMerchantId>` and nothing else — no why, no
since-when — and it is on the gateway's once-a-minute hot path. **The subscription register below
answers the human question strictly better**: `blockedOnly = true` returns the same three statuses
`EntitlementServiceImpl.BLOCKED` uses, and every row carries the reason (`status`), the date
(`graceUntil`, `trialEnd`, `suspendedAt`, `canceledAt`), the org and the plan. One call, paged, and
gated on `ROLE_SUPER_ADMIN` rather than on a token that also opens a write. The `lessons.md` entry
gets corrected to say so.

`SecurityConfig` needs no change either: it permits `/api/v1/*/public/**` only, and `/api/v2/**`
falls through to `anyRequest().authenticated()`. The gateway already routes `/billing/**`.

### Two defects found while reading, which the Activity screen would otherwise expose

**Every `ChangeSource.API` audit row is written with `actor = null`.** `SubscriptionServiceImpl`
calls `auditService.record(before, saved, …, ChangeSource.API, null)` at all five human-driven call
sites (lines 140, 149, 174, 260, 282); only the `JOB` rows name anyone, via `JOB_ACTOR`. So the
table records *that* a person changed a plan and never *which* person — precisely the column the
Activity tab exists to show, and precisely the question a billing dispute turns on.

**This gets worse the moment the guard above lands**: a super admin will be able to cancel any
subscription on the platform, and the trail will not say who. So it is fixed in the same change, by
threading the principal rather than reaching for `SecurityContextHolder` inside the audit writer —
three files, explicit, and testable without a security context: `SubscriptionService` gains a
`String actor` on `changePlan` / `cancel` / `resume`, the impl passes it to the three `record(…)`
calls in place of `null`, and `SubscriptionApi` takes a `java.security.Principal` (Spring MVC
resolves it natively — no commons change) and passes `principal.getName()`. `cancel` reaches five
parameters, well under checkstyle's ceiling of nine. Historic rows stay null and the console renders
that honestly rather than guessing.

**And `from_plan_id` is never written either.** `SubscriptionAuditEntity.of` takes `fromPlan`, and
both call sites in `SubscriptionAuditServiceImpl` pass a literal `null` — the column exists, the
`CHECK` constraints cover it, and nothing fills it. So a `PLAN_UPGRADED` row says which plan the
store landed on and not which one it left, which is half of the sentence the screen is for. This one
*does* need the interface widened: `record` takes the before-plan alongside the before-status
(around seven call sites, all inside billing). Without it the Activity tab's most valuable event
type renders "→ PRO" with nothing on the left.

There is a third, subtler one in the same area: for `PLAN_DOWNGRADE_SCHEDULED` the entity's `planId`
has not moved yet, so `to_plan_id` records the plan the store is **leaving** and the scheduled target
appears nowhere. Recorded and left as-is — fixing it means the writer reading
`pendingPlanPriceId`, which is a change to what the event *means*, not to a null column.

(`detail` is unpopulated too — `SubscriptionAuditEntity.withDetail` has **no callers anywhere**. It
is free text and nothing depends on it, so it is left alone and the column simply is not drawn.)

---

### `api/v2/BillingStatisticApi.java` — new, `@RequestMapping("/api/v2")`

Modelled on tenancy's `OrgStatisticApi` exactly: `POST`, a `StatisticRange` body, a `StatisticList`
back, `@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")` on every method. `StatisticEntry` is
`(date, name, value)` and `name` is the grouping key, which is how `orderStatistic` already carries
an order status.

**`POST /private/revenue-statistic`** — money actually collected, per day and per currency.

```sql
select date(paid_at) as date, currency as name, sum(amount_paid) as value
from billing.subscription_invoice
where status = 'PAID' and paid_at between :from and :to
group by date(paid_at), currency
```

`paid_at`, not `issued_at`, and the reason is specific to this table: `SubscriptionInvoiceEntity
.settled(...)` writes `status`, `amount_paid` and `paid_at` and **does not touch `issued_at`**. Key
the sum on `issued_at` and a *past* day's bar moves when a late payment lands — an operator reloads
last month and the chart has changed under them. Keyed on `paid_at`, a day's figure changes only
when money actually moved on that day. `UNCOLLECTIBLE` and `VOID`
are excluded by the same predicate. **Minor units, and the currency is in the grouping key** — never
converted and never summed across currencies, because nothing on this platform holds an exchange
rate and a mixed total is a wrong number rather than a missing one.

**`POST /private/subscription-statistic`** — subscriptions *started* per day, by plan code, reading
`subscription_audit` rather than `store_subscription.created_date`:

```sql
select cast(date(s.occurred_at) as varchar) as date,
       coalesce(p.code, 'UNKNOWN')          as name,
       count(*)                             as value
from (select distinct on (a.store_id) a.store_id, a.occurred_at, a.to_plan_id
      from billing.subscription_audit a
      where a.event_type in ('TRIAL_STARTED', 'ACTIVATED')
      order by a.store_id, a.occurred_at) s
left join billing.plan p on p.id = s.to_plan_id
where s.occurred_at >= :from and s.occurred_at < :to
group by date(s.occurred_at), coalesce(p.code, 'UNKNOWN')
order by 1, 2
```

**`distinct on (store_id)`, because `ACTIVATED` fires more than once.** A suspended store that pays
and comes back activates again, and counting raw rows would book a returning customer as a new one.
The range filter sits **outside** the sub-select on purpose — pushing it in would pick the first row
*in the window* rather than the first ever, which is the same bug with extra steps. `coalesce` to
`UNKNOWN` rather than dropping the row: a dangling `to_plan_id` after a catalogue change should show
up as a visible bar, not as a total that quietly shrank.

`created_date` would count **every store that entered billing**, including the ones that never paid
— a `store_subscription` row is written by provisioning the moment a store is created. That is
already `store-statistic`'s answer. "Started" means started *paying or trialling*, and the audit
table is the only thing that records when that happened. Both tables begin at the same moment in
history, so the audit source costs nothing in coverage. Grouped by plan code so the console can
stack the series — the tenancy counters' `name` is null and these are the first that is not.

**`GET /private/plan-statistic`** — the commercial reading of the catalogue. Two queries, one
response, and a purpose-built DTO rather than `StatisticList`, because it carries counts *and* money
in two dimensions and `StatisticEntry.value` is a single `Number`.

```sql
-- who is on what, right now
select p.code, s.status, count(*) from billing.store_subscription s
  join billing.plan p on p.id = s.plan_id group by p.code, s.status

-- annualised run rate, per plan per currency
select p.code, pp.currency,
       sum(case pp.billing_interval when 'YEAR' then pp.unit_amount else pp.unit_amount * 12 end)
from billing.store_subscription s
  join billing.plan p on p.id = s.plan_id
  join billing.plan_price pp on pp.id = s.plan_price_id
where s.status = 'ACTIVE' group by p.code, pp.currency
```

**Annualised, not monthly** — dividing a yearly price by twelve truncates on every row (a €1199/yr
plan contributes €99.91, and four hundred of them lose €33 to the floor); multiplying a monthly one
by twelve is exact in `bigint`. The single division happens once, in Java, on the aggregate, and
both figures ship so the console never has to divide either.

**`status` stays in the group key** rather than filtering to `ACTIVE` — `TRIALING` is reported
separately rather than mixed in. Whether an operator wants a committed or an optimistic run rate is
their call, not the API's, and an endpoint that silently picks one is the classic way to overstate a
book. The `left join` in the first query is what keeps plan-less `PENDING` stores visible; an inner
join would hide exactly the rows an operator is looking for.

New records in `billing-commons/.../dto/admin/`: `PlanStatisticReport(counts, recurringValue)`,
`PlanSubscriptionCount(planCode, planDisplayName, tier, status, subscriptions)` — `planCode` nullable
for the plan-less — and `PlanRecurringValue(planCode, status, subscriptions, Money monthly,
Money annual)`.

**`GET /private/billing-health`** — two counts nothing has ever read: `processed_stripe_event` with
`outcome = 'FAILED'` in the last 24 hours, and `stripe_request` with `completed_at is null` older
than a few minutes (a mutating Stripe call that was recorded and never finished). One query each,
and together they are the only "billing is broken right now" signal the platform has. Cheap enough
that omitting it and writing a `lessons.md` entry instead would be the more expensive choice.

### `api/v1/PlatformBillingApi.java` — new, `@RequestMapping("/api/v1/platform")`

Every method `@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")`. `POST` with a query body wherever there
is more than one optional filter, matching `store-manager/list` and the `org-manager/list` added in
the previous change; `page` and **`count`** as query parameters, because
`store-commons:autoconfigure`'s `ServletWebConfig` renames Spring's `size` platform-wide.

| Endpoint | Body | Returns |
|---|---|---|
| `POST subscriptions` | `ListSubscriptionQuery(org, status, planCode, term, boolean blockedOnly)` | `Page<PlatformSubscriptionView>` |
| `POST invoices` | `ListInvoiceQuery(ManagerOrgId org, StoreMerchantId store, InvoiceStatus status, Instant from, Instant to)` | `Page<PlatformInvoiceView>` |
| `POST invoices/totals` | the same body | `List<InvoiceTotal(CurrencyCode, Money paid, Money due)>` |
| `POST audit` | `ListAuditQuery(StoreMerchantId store, ManagerOrgId org, AuditEventType eventType, ChangeSource source, Instant from, Instant to)` | `Page<SubscriptionAuditView>` |

Totals are a **second call on the same body**, not a field on the page: the ledger's rows can render
while the sums are still being computed, and a page response that is sometimes a page and sometimes
a page-plus-envelope is the kind of shape the console's `SpringPage` type exists to avoid.

`blockedOnly` is a named boolean rather than a status list because "blocked" is *three* statuses
(`PENDING`, `SUSPENDED`, `CANCELED` — `EntitlementServiceImpl.BLOCKED`) and a nullable list cannot be
written with the `cast(:x as varchar) is null` idiom. It is also the thing an operator actually asks
for, so naming it beats making the console spell it out.

Every optional filter is written the way tenancy writes them —
`cast(:x as varchar) is null or col = :x`, and `ilike '%' || :term || '%'` for the substring search
over the store id. Spring Data JDBC has **no `countQuery`**, so each list is a `findVisible` plus a
`countVisible` assembled by hand into a `PageImpl`, exactly as `ManagerOrgRepository.findVisible` +
`InternalOrgServiceImpl.findAll` do.

The audit list orders by **`occurred_at desc, id desc`**: `occurred_at` is `Instant.now()` at write
time, two rows written in one transaction can share it, and the `bigserial` is what makes the order
deterministic across pages.

`from_plan_id` / `to_plan_id` are ids and the screen needs codes — resolved by **two left joins in
the query**, not in the service. `PlanCatalogService` is an uncached per-id read, and a page of fifty
rows would be a hundred round trips to fill two columns; `billing.plan` is a handful of rows and gets
hash-joined for nothing.

`SubscriptionAuditService.search` is annotated **`@Transactional(readOnly = true)`, not
`Propagation.MANDATORY`** like its two siblings on that interface. The writers are called from inside
the transaction they describe; this one is called from a controller with no ambient transaction, and
copying the propagation would throw `IllegalTransactionStateException` on every request.

### New and changed files

**New in `billing-commons/.../commons/dto/`:** `PlanStatisticView`, `PlatformSubscriptionView`,
`PlatformInvoiceView`, `SubscriptionAuditView`, `InvoiceTotal`, `BillingHealthView`, and the three
query records above. `PlatformSubscriptionView` is *not* `SubscriptionView`, and the reason is sharper than
"different fields": **`PlanCatalogServiceImpl` has no caching**, so `SubscriptionMappers.toView`
does two to four uncached primary-key reads *per row* to fill the plan, the price and the
entitlement map. A fifty-row page would be roughly two hundred round trips to render a table that
wants none of it. The admin row is flat and comes out of one joined query. It also carries
`graceUntil` (a `PAST_DUE` row means nothing without it), `suspendedAt` / `canceledAt` (the
"blocked since" the register replaces `blockedStores` with) and `providerLinked`.

**New in `billing-service/`:** `api/v2/BillingStatisticApi`, `api/v1/PlatformBillingApi`,
`service/PlatformBillingService` + `impl/`, `mappers/PlatformBillingMappers`.

`InvoiceServiceImpl.toView` is private and both services now need it, so it is extracted to a
`mappers/InvoiceMappers` `@Component` beside the existing `SubscriptionMappers`.

**Changed:** `SubscriptionAuditRepository` (four new queries — the paged read, its count, and the two
statistics), `SubscriptionInvoiceRepository` (the paged ledger, its count, the totals, the revenue
statistic), `StoreSubscriptionRepository` (the paged register, its count, the two plan aggregates),
`SubscriptionAuditService` + impl (the actor and from-plan fixes above, plus a read method — the
interface is write-only today), `ExternalEntitlementApi` (two annotations), `PermissionAccessChecker`
(two lines), and `schema.sql`.

**Indexes**, all into `schema.sql` — the DDL is hand-written and reviewed, so a new index is part of
the change, not a migration afterthought. `subscription_invoice` has only `(store_id, issued_at)`
and `subscription_audit` only `(store_id, occurred_at)`, so every org filter and every date sweep in
the tables above is currently a sequential scan:

```sql
create index if not exists idx_subscription_invoice_org_issued
    on billing.subscription_invoice (org_id, issued_at desc);
create index if not exists idx_subscription_invoice_paid
    on billing.subscription_invoice (paid_at) where status = 'PAID';
create index if not exists idx_subscription_audit_org
    on billing.subscription_audit (org_id, occurred_at desc);
create index if not exists idx_subscription_audit_occurred
    on billing.subscription_audit (occurred_at desc, id desc);
create index if not exists idx_store_subscription_plan_status
    on billing.store_subscription (plan_id, status);
```

### Tests — the first real ones billing has

`billing-service/src/test` contains **only** `contextLoads` and a Testcontainers config. New Mockito
unit tests tagged `@Tag("unit-test")`, following tenancy's `StoreTenantScopingTest`:

- **`PlatformBillingScopingTest`** — each filter reaches the query and a null one drops the
  predicate; the register, ledger and audit each page by hand into a correct `PageImpl`.
- **`SubscriptionAuditActorTest`** — an `API` row records the authenticated principal, a webhook row
  still says `stripe`, a job row still says the job name, and `from_plan_id` is now written.
- **`RevenueStatisticTest`** — only `PAID` counts, it keys on `paid_at`, and two currencies come back
  as two entries rather than one sum.

And in `store-commons/autoconfigure`, the test the whole authorization decision rests on: a super
admin **passes** `hasAccessOnBillingRead` and `hasAccessOnBillingManage` and still **fails**
`hasReadAccessOnStore` — that is what keeps the merchant screens 403 for them.

---

## Console

### New api tier

`src/app/api/billing/platform-billing.service.ts` (+ spec) — the platform's half of billing, kept
apart from the store-scoped `subscription.service.ts` because the two have different audiences and
different guards. Specs assert path, verb, the wrapped `{id}` bodies and **`count`, not Spring's
`size`** — the mistake `org.service.spec.ts` exists to catch.

`@api/analytics/statistic.service.ts` gains `subscriptionStatistic` and `revenueStatistic` beside
`orgStatistic` / `storeStatistic`, on a new `BILLING_STATISTIC_BASE`. Its class comment currently
explains that `subscription-statistic` was never ported *because it does not exist* — that paragraph
gets rewritten rather than deleted, since the reason it now exists is worth keeping.

### Models

`src/app/models/platform-billing.ts` — the row and view types, tone maps, and known-value sets
(`INVOICE_STATUSES`, `AUDIT_EVENT_TYPES`, `AUDIT_SOURCES`) for the strict missing-key guard. Reuses
`Money`, `SubscriptionStatus`, `InvoiceStatus` from `@models/billing` rather than restating them.

`@shared/i18n/platform-label.ts` gains `invoiceStatus`, `auditEvent` and `auditSource`, each through
the existing `lookup` guard with a **template-literal key builder** — a key composed from a
parameter has no static head and `npm run lint:i18n` would report the whole namespace dead.

### `/platform/billing` — a new tabbed section

Route `platform/billing/:section` with a `pathMatch: 'full'` redirect from `platform/billing`,
copying `organizations/:id/:section` exactly: the tab is a route segment, so it is linkable and
survives a reload. Four sections:

| Tab | What it is |
|---|---|
| **Overview** | The money screen. Revenue KPIs per currency, a revenue trend, plan mix (donut), annualised run rate per plan (bar), the stores billing has cut off, and the two health counts. |
| **Subscriptions** | Every subscription on the platform. Server-side search and filters (org, status, plan). Row opens the store's billing detail. |
| **Invoices** | The ledger. Filter by org, store, status, date range; totals for the current filter; Stripe's hosted invoice and PDF as row links. |
| **Activity** | `subscription_audit`, newest first, filterable by event type, source, org and date range. The one screen that answers "who moved this store onto the cheaper plan, and when". |

`features/platform-billing/` follows the standard anatomy — page + `facades/` +
`services/platform-billing.api.service.ts`, facade **provided by the page**, `snapshot()` for every
load, `app-load-error` / `app-busy-overlay` / `app-empty-state` throughout. Filters are
`app-search-box` + `app-select` + `app-date-range-picker`, all reflected in the URL and restored
from it, as organizations and the fleet now do.

Every currency is a **separate figure**, never summed — nothing on the platform holds an exchange
rate, and a mixed-currency total is a wrong number rather than a missing one.

Nav: a `Billing` entry in the platform group in `console-navigation.ts`, between `Accounts` and
`Plans`, on `icon: 'dollar'` — `creditCard` is already `Plans` and two identical glyphs in one rail
is worse than either choice. `receipt`, `percent` and `chartLine` are also in the set and are the
candidates for the tabs' own headers.

### Store billing detail

`features/platform-billing/components/store-billing-panel/` — one store's subscription, its
invoices and its audit trail, plus the three levers. Reached from a subscription row, from the
invoice ledger, and from an organization's Stores tab (a new **Billing** action on the row, which is
where the `lessons.md` entry says the question actually gets asked).

The levers reuse the existing endpoints through the widened guard: change plan (an
`app-select` of the catalogue the plans screen already loads), cancel — with `app-confirm-dialog`
distinguishing **at period end** from **immediately**, since only an operator can do the second —
and resume. Every act writes a `subscription_audit` row naming the operator, and the panel's Activity
list reloads to show it.

**The levers are gated on `providerLinked`, not on status.** `SubscriptionServiceImpl.cancel` and
`resume` both call `requireProviderSubscription`, so a trial we granted ourselves — which has no
Stripe subscription behind it — cannot be cancelled or resumed at all. `SubscriptionView` already
carries the flag; the merchant billing page already reads it. Offering a button that is certain to
throw is the failure this avoids.

### Enriching what exists

- **`/platform` dashboard.** The third chart slot that has been documented as absent since Module 11
  gets its series, and the KPI row gets money. `platform-dashboard.api.service.ts`'s `forkJoin`
  gains two legs; the two new ones are wrapped in `optionalOne` — the page is still the tenant
  counts, and a billing outage should not blank the org and store charts. The `notice-bar` saying
  the page has no money on it is removed. Its `toDailySeries` / `toRange` helpers are reused
  unchanged; `toDailySeries` currently sums `name` away, so it grows a by-name variant for the
  stacked plan series.
- **Organization detail.** The Stores tab's Billing column becomes a link into the store billing
  panel, and the org gains a **Billing** tab: its subscriptions, its invoices, its spend.
- **`/platform/plans`.** Each plan row gains a live **subscribers** count and its recurring value,
  from the new plan statistics — turning a price list into a commercial reading.

### i18n

**The event-type filter lists thirteen values, not sixteen.** `RESUMED`, `INVOICE_RECORDED` and
`QUOTA_REFUSED` are in the enum and in the `CHECK` constraint and **are never written by anything** —
grep the writers. Offering three options that always return nothing reads as a broken filter. The
translations exist for all sixteen (a row could appear later); only the dropdown is narrowed, with a
comment saying why.

A `platform.billing.*` namespace in `en.json` and `ar.json` at exact parity, plus
`platform.invoiceStatus.*`, `platform.auditEvent.*` (16 values) and `platform.auditSource.*` beside
the existing `platform.orgStatus.*`. Money through `Money.account()` — the ISO-code form, because
`symbol` renders `US$` and strands Latin script in an RTL line. Dates and counts through
`TranslocoLocaleService`. Never `CurrencyPipe` or `DatePipe`.

### `lessons.md`

Four entries are **answered** and get rewritten to say what shipped rather than what is missing:
*"Platform — no subscription statistics"*, *"Platform — no revenue or GMV figure anywhere"*,
*"Platform — a store's subscription cannot be read by an operator"*, *"Platform — no view of stores
billing has cut off"*. New entries for what this plan deliberately leaves open: no comp/credit/trial
extension, no per-currency conversion, and whatever the API design below cannot answer.

---

## Verification

0. **Commit the five previous turns' work first** — Module 11, the merchant-nav split, the billing
   column and plans screen, the pod store list, and the search/dialog change are all sitting
   uncommitted on `feat/mirror-console-ui`. They are separable and reviewable; this change should not
   land on top of them in one undifferentiated diff.
1. `./gradlew :store-core:billing:billing-service:test checkstyleMain checkstyleTest`, and the same
   for `store-commons:autoconfigure` — the permission checker change lives there and is depended on
   by every service.
2. `npm run build`, `npm run lint`, `npm run test:ci` in `store-core/console-ui`.
3. `bash extra/scripts/run-lcl.sh restart billing console-ui`.
4. As the signed-in super admin, in the browser: each of the four tabs loads with real rows; the
   filters narrow server-side and restore from the URL; the dashboard's third chart and money KPIs
   are populated; a plan change from the store panel appears in Activity naming the operator.
5. Confirm an **org admin is unchanged** — their own store's billing page still works, and they still
   cannot read another org's. This is the regression the guard change risks.
6. Both new screens in Arabic/RTL.

The two statistic queries use `date()`, which resolves in the **database session's timezone**. The
service runs UTC, so a payment at 23:50 local lands on the previous day for an operator elsewhere.
Documented in the javadoc rather than fixed: doing it properly means an `AT TIME ZONE` parameter on
both queries, which is its own change.

## Not in scope

Comp/credit/trial-extension levers; currency conversion; reading `tenancy_audit` and `pod_audit`,
which have the same "written and never read" shape and are their own change.

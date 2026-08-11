# Billing (subscription) microservice

## Context

The SaaS needs per-store subscriptions: an org gets one 14-day trial, each store carries its own plan
(an org can hold two PRO stores and one BASIC), billed monthly through Stripe, with renewal, upgrade,
downgrade, disable-renew, cancel, suspend-on-non-payment, invoice history, webhooks, idempotency,
subscription events and an audit trail.

**What exists today.** `store-core/control-plane` already runs an *org-level* Stripe subscription context —
`subscription-commons` / `-events` / `-external-api` modules, `subscription.subscription` (PK = org id, one
row per org), SUBSCRIPTION-mode checkout, webhook routing, and `DeActivateNonRenewedSubscriptionsJob`. It is
one row per org, not per store; it has no upgrade/downgrade, no invoice history, no idempotency, no audit,
and its plans are the hardcoded `store-commons` `SubscriptionPlan` enum. Deactivation only flips a column:
`SubscriptionDeActivateEvent` has no `@Ou ftboxHandler`, `SubscriptionPlan.exceeded()` has zero callers, and
`manager_store` has no status at all — nothing is enforced anywhere.

**Decisions taken** (do not re-litigate):

1. **Greenfield alongside.** New modules; control-plane's subscription code is left untouched and retired
   last (phase 8).
2. **Service name `billing`.** `spring.application.name: subscription` is a hard blocker —
   `common-config.yml:105` sets `spring.datasource.hikari.schema: ${spring.application.name}` and every
   service shares the `cvhome` database, so the app would get `search_path=subscription`, the schema
   control-plane's tables already occupy. `billing` also avoids duplicating the `subscription-commons`
   Gradle leaf name.
3. **Per-store subscription; the 14-day trial is granted once per org.** The org's first store starts
   `TRIALING`; stores #2+ start `PENDING` and must pay. Enforced by a primary key, not an `if`.
4. **DB-driven plan catalog** (`plan` / `plan_price` / `plan_entitlement`), seeded from config and synced to
   Stripe. This is what makes the service generic. The `SubscriptionPlan` enum is not imported.
5. **All four enforcement layers**, entitlement API first.
6. **Suspended stores keep their public storefront up.** Suspension blocks the seller console; shoppers keep
   browsing and ordering so the merchant can pay.
7. **Subscription rows are provisioned from the outbox**, not a synchronous call.

Intended outcome: a deployable `billing` service on port 8021 behind `store-core-gateway` at `/billing/**`,
owning plans, subscriptions, Stripe, invoices and entitlements, with control-plane, the gateway and the pods
consuming it through `billing-external-api`.

---

## 1. Modules

New tree `store-core/billing/`, package root `com.asrevo.cvhome.billing.*`. Follows the standard split;
copy `store-core/control-plane`'s module shapes (each library module is `build.gradle` + `lombok.config` +
sources).

```
store-core/billing/
├── billing-commons/        value objects, DTOs, the ErrorCode enum + condition-named exceptions
├── billing-events/         @OutboxEvent records + commands (depends on billing-commons + libs.namastack.outbox.api)
├── billing-external-api/   @HttpExchange client contracts + caller-side error catalog + entitlement cache
└── billing-service/        Spring Boot app (JDBC, Stripe, webhooks, jobs, schema.sql), http/*.http
```

`billing-service/build.gradle` is `control-plane-service/build.gradle` verbatim with
`createImageName("store-core/billing", …)` and the project deps swapped to `billing-events` +
`billing-external-api`. It keeps `libs.spring.boot.starter.data.jdbc`, `libs.namastack.outbox.starter.jdbc`,
`libs.stripe.java`, `libs.gson`, `libs.caffeine`, `libs.mapstruct`. **No dependency on any
`store-core:control-plane:*` module** — billing does not read control-plane's schema and does not import
`SubscriptionPlan`.

`billing-external-api` serves both servlet callers (control-plane, pods) and the reactive gateway, so it
takes `compileOnly libs.spring.web` **and** `compileOnly libs.spring.webflux`, with the `Mono` methods on a
separate `ReactiveExternalEntitlementService` interface — a servlet caller must never see reactor types on
its proxy (`buildClient(...)` with caller-side types on the server interface is a review reject).

Store-core layer ⇒ **Spring Data JDBC**, not JPA: `@Table(schema=…)` from
`org.springframework.data.relational.core.mapping`, hand-written `src/main/resources/schema.sql`, no
`hibernate.orm` plugin. s2s client `store-core@service.store-core.internal`, scope `store_core`.

---

## 2. Persistence — `billing-service/src/main/resources/schema.sql`

Two schemas: `billing` (business) and `billing_outbox`. Every enum column carries a `CHECK` constraint —
`schema.sql` is the source of truth for DDL and a new enum value means a DDL edit.

| Table | Key | Purpose |
|---|---|---|
| `billing.plan` | `id` | code, display name, `tier` (ordering: upgrade iff target tier > current), `stripe_product_id` |
| `billing.plan_price` | `id` | plan + currency + `billing_interval` (MONTH/YEAR) + `unit_amount` minor units + `trial_days` + `stripe_price_id`; unique on `(plan_id, currency, billing_interval)` |
| `billing.plan_entitlement` | `(plan_id, entitlement_key)` | `limit_value` or `flag_value`; both null = unlimited (shape enforced by a CHECK) |
| `billing.store_subscription` | `id` = `ManagerStoreId` | `org_id`, `status`, plan + price, Stripe customer/subscription/schedule ids, `current_period_start/end`, `trial_end`, `cancel_at_period_end`, `pending_plan_price_id` + `pending_effective_at` (scheduled downgrade), `grace_until` |
| `billing.org_trial_grant` | `org_id` | the once-per-org trial token — the PK *is* the enforcement |
| `billing.subscription_invoice` | Stripe invoice id | status, amounts, period, `hosted_invoice_url`, `invoice_pdf_url` |
| `billing.subscription_audit` | `bigserial` | append-only: from/to status, from/to plan, `source` (API/WEBHOOK/JOB/SYSTEM), `actor`, `stripe_event_id`, `detail` |
| `billing.processed_stripe_event` | Stripe `event.id` | inbound idempotency; `outcome` in SCHEDULED/IGNORED/FAILED |
| `billing.stripe_request` | `idempotency_key` | outbound idempotency; records intent, not outcome |
| `billing_outbox.outbox_record` / `_instance` / `_partition` | — | copy the three tables + nine indexes from `control-plane-service/src/main/resources/schema.sql:66-129`, replacing the `control.` prefix |

Indexes that matter: `store_subscription (org_id)`, `(stripe_customer_id)`, `(status, current_period_end)`,
`(status, grace_until)`, a partial index on `pending_effective_at where pending_plan_price_id is not null`,
and `subscription_invoice (store_id, issued_at desc)`.

`data.sql` carries no catalog rows — catalog rows hold Stripe ids that a static SQL file cannot know (§5.4).

**`JdbcConfig extends AbstractJdbcConfiguration`** (copy `control-plane-service/.../config/JdbcConfig.java`):
every VO used as a column needs a `Converter` pair — `ManagerStoreId`, `ManagerOrgId`, `PlanId`,
`PlanPriceId`, the five Stripe id types, `CurrencyCode`, `EntitlementKey`. A missing one is a runtime
`ConverterNotFoundException` at first query, not a compile error; this is the likeliest phase-1 startup
failure.

---

## 3. Domain and state machine

| Status | Meaning | Store operable? |
|---|---|---|
| `PENDING` | Row exists, no plan, never paid — store #2+ of an org that used its trial | no |
| `TRIALING` | The org's one 14-day trial, on its first store | yes |
| `ACTIVE` | Paid, period open | yes |
| `PAST_DUE` | Renewal failed; `grace_until = now + P7D` (configurable) | yes (grace) |
| `SUSPENDED` | Grace expired, or trial expired unpaid | no |
| `CANCELED` | Terminal; re-subscribing returns the row to `PENDING` on a new Stripe subscription | no |

Legal transitions live in one static table on `SubscriptionStatus` in `billing-commons`, with
`canTransitionTo` and `operable()`:

```
PENDING   → TRIALING, ACTIVE, CANCELED
TRIALING  → ACTIVE, SUSPENDED, CANCELED
ACTIVE    → PAST_DUE, SUSPENDED, CANCELED
PAST_DUE  → ACTIVE, SUSPENDED, CANCELED
SUSPENDED → ACTIVE, CANCELED
CANCELED  → PENDING
```

A transition to the **same** status is deliberately legal, so a redelivered webhook is a no-op rather than a
failure — the backstop behind `processed_stripe_event`. Anything else throws
`IllegalSubscriptionTransitionException` (an `OperationNotAllowedException`).

`StoreSubscriptionEntity extends BaseEntity<StoreSubscriptionEntity, ManagerStoreId>` has **no setters**.
Each change is a named method that validates the transition, mutates, `registerEvent(...)` and returns
`this` — the shape of `ManagerStoreEntity` and payment's `Transaction`: `pending`, `trialing`, `activate`,
`renew`, `markPastDue`, `suspend`, `upgradeTo`, `scheduleDowngradeTo`, `applyPendingChange`,
`scheduleCancel`, `revokeScheduledCancel`, `cancelNow`.

Audit rows are written explicitly by `SubscriptionAuditService.record(...)` from the service layer, inside
the same `@Transactional` as the save. There is no auditing framework available here — store-pod's
`AuditSection`/`AuditListener` is JPA-only. Never write an audit row from a controller.

### Plan-change semantics

- **Upgrade** (`target.tier > current.tier`) — immediate and prorated:
  `ProrationBehavior.ALWAYS_INVOICE` + `PaymentBehavior.ERROR_IF_INCOMPLETE`, so a declined card surfaces as
  a Stripe `CardException` → 422, never a silent half-upgrade. Entitlements widen in the same request; the
  following `customer.subscription.updated` webhook is a no-op.
- **Downgrade** (`target.tier < current.tier`) — deferred to period end, never prorated. A Stripe
  `SubscriptionSchedule` (phase 1 = current price to `current_period_end`, phase 2 = target) is the primary
  path; locally `pending_plan_price_id` + `pending_effective_at` are set, and `ApplyPendingPlanChangesJob` is
  the safety net for a dropped webhook. Both converge on `applyPendingChange`, which is idempotent.
- **Monthly → yearly** is an upgrade; yearly → monthly is a downgrade.
- **Disable renewal** = `setCancelAtPeriodEnd(true)`; status stays `ACTIVE`. **Resume** reverses it, legal
  only while the flag is set and the period has not ended.
- **Immediate cancel** is `ROLE_SUPER_ADMIN` only; self-serve offers cancel-at-period-end.
- A downgrade whose target entitlements are already exceeded by current usage is refused with
  `DowngradeNotAllowedException` (422) naming the offending keys — checked at schedule time and again at
  apply time. At apply time a violation **cannot** refuse (Stripe already flipped): it downgrades, emits
  `SubscriptionPlanChangedEvent`, and layer (d) enforces the new ceiling on writes while leaving existing
  data readable. Put that in the javadoc; it is the kind of thing that gets "fixed" into data deletion.

### Webhook → transition

| Stripe event | Transition |
|---|---|
| `checkout.session.completed` | bind ids from `client_reference_id` = storeId (no transition) |
| `customer.subscription.created` | `PENDING → TRIALING` if `trial_end` present |
| `invoice.payment_succeeded` | `PENDING/TRIALING/PAST_DUE/SUSPENDED → ACTIVE`, or `ACTIVE → ACTIVE` (renew) |
| `invoice.payment_failed` | `ACTIVE → PAST_DUE`, set `grace_until` |
| `customer.subscription.updated` | apply upgrade / pending downgrade; map status |
| `customer.subscription.deleted` | `* → CANCELED` |
| `customer.subscription.paused` | `* → SUSPENDED` |
| `trial_will_end`, `invoice.created/finalized`, `payment_intent.*`, `charge.*` | recorded `IGNORED` |

Never throw on an unknown type — Stripe retries a non-2xx for days.

---

## 4. API surface and permissions

Core convention, not the pod one: `@OrgStorePrincipalInfo UserOrgStoreIdentity identity` plus an explicit
`@RequestParam ManagerStoreId store`, and
`@PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.BILLING.<ACTION>')")`.

**`SubscriptionApi` — `api/v1/subscription`**

| Endpoint | Token | Returns |
|---|---|---|
| `GET current?store=` | `BILLING.READ` | status, plan, price, `currentPeriodEnd` (= next renewal), `trialEnd`, `cancelAtPeriodEnd`, `pendingPlanChange`, entitlements |
| `POST checkout?store=` `{planPriceId}` | `BILLING.MANAGE` | `{ url }` as 200 JSON — **not** a 302 like control-plane's, so the SPA can open it and `.http` can assert on it |
| `POST upgrade?store=` `{planPriceId}` | `BILLING.MANAGE` | `SubscriptionView` (immediate) |
| `POST downgrade?store=` `{planPriceId}` | `BILLING.MANAGE` | `SubscriptionView` with `pendingPlanChange` |
| `POST cancel?store=` `{immediate}` | `BILLING.MANAGE` | `SubscriptionView` (`immediate:true` additionally requires super-admin) |
| `POST resume?store=` | `BILLING.MANAGE` | `SubscriptionView` |

**`PlanCatalogApi` — `api/v1/plan`**: `GET public/plans?currency=` (permitAll via the `/api/v1/*/public/**`
rule in `SecurityConfig`), `GET plans/comparison?store=` (`BILLING.READ`).

**`InvoiceApi` — `api/v1/invoice`**: `GET list?store=` paged (`BILLING.READ`).

**s2s** — `ExternalEntitlementApi` (`api/v1/private/entitlement`): `GET snapshot?store=`,
`POST snapshot/batch`, `GET blocked-stores` (`BILLING.ENTITLEMENT-READ`). `ExternalStoreQuotaApi`
(`api/v1/private/quota`): `POST store-create` → `StoreQuotaDecision`, `POST provision`
(`BILLING.QUOTA-CHECK`).

**`StripeWebhookApi` — `api/v1/stripe-webhook`**: `POST public/events`, public by path, authenticated by
signature only.

### The two mandatory permission edits

Both files, or every call 403s silently (deny by default):

- `store-commons/autoconfigure/.../s2s/config/internal/CustomPermissionEvaluator.java` — four new `case`s in
  `hasStoreCorePermission` (core tokens are inline strings there): `STORE-CORE.BILLING.READ`, `.MANAGE`,
  `.ENTITLEMENT-READ`, `.QUOTA-CHECK`.
- `store-commons/autoconfigure/.../s2s/services/PermissionAccessChecker.java` — the four matching methods.
  `READ` → read access on store; `MANAGE` → org admin (paying is an org-level act; widen the existing
  private `hasMaintainAccessOnStore` rather than duplicating the check); `ENTITLEMENT-READ` → store-core or
  store-pod scope, or read access; `QUOTA-CHECK` → store-core scope only.

**Constraint:** `CustomPermissionEvaluator` builds `PermissionAccessChecker` with `new` and has no Spring
context, so it **cannot** call billing. Subscription status is never a permission token — layers (c) and (d)
are separate guards.

Every endpoint ships a `.http` block in `billing-service/http/<api-class>.http`, addressed through the
gateway (`{{SELLER_UI_URL}}/billing/api/v1/...`), covering the happy path **and** the failure the endpoint
exists to produce: cross-store 403, 422 downgrade refusal, 404 for a store with no subscription.

---

## 5. Stripe

### 5.1 Config

Reuse the existing `StripeProperties` (`store-commons/autoconfigure/.../s2s/model/StripeProperties.java`,
`@ConfigurationProperties("com.asrevo.cvhome.stripe")`, `key` + `webhookSigningKey`). Add nothing.

`StripeConfig` exposes a `RequestOptions` factory taking an idempotency key. **Never set the static
`Stripe.apiKey`** — control-plane does; payment does not. Per-call `RequestOptions` is the rule.

### 5.2 Customer and checkout

One Stripe customer **per org** (shared payment method and portal), many subscriptions under it — one per
store. `findOrCreate` resolves the org's existing `stripe_customer_id` from any `store_subscription` row for
that org (indexed), **not** `Customer.search` — search is eventually consistent and duplicates customers
under concurrent store creation. Idempotency key `"cust:" + orgId`.

Checkout is `Mode.SUBSCRIPTION`, `setClientReferenceId(storeId)` as the join key back to our row, plus
`storeId`/`orgId`/`planPriceId` in `subscription_data.metadata`. Success/cancel URLs come from the existing
`RedirectionUrlBuilder` + `ServiceDomainProperties.getService("seller-ui")` idiom — never hand-built.
Idempotency key `"checkout:<storeId>:<planPriceId>:<minuteBucket>"`.

### 5.3 Webhook + idempotency

The controller verifies the signature and returns **400** on failure (so Stripe stops retrying a payload
that will never verify) and lets everything else 500 (so Stripe *does* retry). `WebhookIngestService.ingest`
is tiny and transactional:

1. `existsById(event.id)` → log duplicate, return;
2. insert the `processed_stripe_event` row — **the PK insert is the race guard**: a concurrent redelivery
   fails the constraint, rolls back, Stripe retries into the now-present row and short-circuits;
3. unhandled type → mark `IGNORED`, return;
4. otherwise `outbox.schedule(StripeWebhookReceivedEvent, storeKey)` — partition key = store id, so one
   store's events stay ordered.

All *work* happens in the `@OutboxHandler` after commit — the same shape as payment's `WebhookOutboxHandler`.
`storeKeyOf` reads `metadata.storeId`, or `client_reference_id`, or resolves via `stripe_subscription_id`;
unresolvable → `IGNORED` + 200, not an error.

### 5.4 The error split (mandatory)

Copy `store-pod/payment/payment-core/.../processor/StripeProcessor.java` — **not** control-plane's version.
Every Stripe call ends with exactly this pair, never one `catch`:

- `catch (CardException)` → `SubscriptionChangeRejectedException`, `ErrorCategory.UNPROCESSABLE` (422). An
  answer: the card was refused, retrying unchanged won't help.
- `catch (StripeException)` → `BillingProviderUnavailableException`, `ErrorCategory.REMOTE_SERVICE` (502).
  No decision: the change may or may not have landed.

Both extend `ExternalProviderException`, so Stripe's code rides as `providerCode` and never becomes ours.
Copy `statusOf(StripeException)` (null status = 0 = no response). **On a 502 the service must not write a
local plan change** — leave the row alone and let the webhook or the reconcile job settle it.

### 5.5 Catalog → Stripe sync

`plan-catalog.yml` (imported via `spring.config.import`) declares plans, prices and entitlements. A seeder
upserts them into the DB by `code` / `(plan, currency, interval)`, then enqueues
`SyncPlanCatalogToStripeCommand` on the outbox so the Stripe half is durably retried on exactly one
instance. Idempotency keys `"product:<code>"` and `"price:<planCode>:<currency>:<interval>:<amount>"`.
**Prices are immutable in Stripe**: changing an amount creates a *new* `plan_price` row and deactivates the
old one; existing subscribers stay on the old price until they change plans. Document that in the yaml
header.

---

## 6. Enforcement

**(a) Entitlement API — the foundation.** `EntitlementService.snapshot(storeId)` loads the subscription plus
its plan's entitlement rows in one store-scoped query and returns
`{ status, operable, planCode, currentPeriodEnd, Map<EntitlementKey, EntitlementValue> }`, cached in
Caffeine for 30s and evicted whenever the aggregate is saved. Every transition publishes an event from
`billing-events`, keyed by store id so one store's transitions stay ordered. Note there is **no cross-service
broker** between store-core and store-pod in this repo: these events drive billing's own reactions; pods
read the snapshot synchronously with a cache, so the cache TTL is the real freshness knob.

**(b) Quota gate on store creation** — in `StoreManagerServiceImpl.createStore`, *before*
`podSelection.next(...)`: a synchronous `checkStoreCreate(orgId)` through `ExternalStoreQuotaService`, built
in control-plane's `ClientsConfig` with `restClientBuilder.buildClient("billing", …, BillingApiErrors.CATALOG)`.
Control-plane gains `implementation project(':store-core:billing:billing-external-api')` — the allowed
direction. **Fail closed**: if billing is unreachable, store creation fails; an unbilled store is worse than
a retryable 502.

Because each store pays for itself, the quota is not a store-count cap — it refuses only on abuse guards
(`max-pending-stores`, default 3) and returns `trialAvailable`.

Provisioning the subscription row is **not** this call. It happens in a `@OutboxHandler` on the existing
`StoreCreatedEvent`, so it inherits the outbox's durable retry and no window exists where a store has no
subscription.

**(c) Store status + gateway gate.** No new column on `manager_store`: the projection lives in billing and
`snapshot(...).operable()` *is* the store status; `ManagerStoreDto` gains a `billingStatus` field populated
on read. One owner, no dual write.

The gate mirrors `PodClient` exactly (`store-core/gateway/.../client/PodClient.java`): a component polling
`GET blocked-stores` on `@Scheduled(fixedRateString = "${cvhome.gateway.billing-refresh-rate:PT1M}")` into a
volatile `Set`, plus a `GlobalFilter` ordered before routing that answers **402 Payment Required** on
`/spg/**` when the `store` query param is blocked. Built in the gateway's existing `ClientsConfig` with
`webClientBuilder.buildClient("billing", ReactiveExternalEntitlementService.class, RemoteErrorCatalog.none())`.

**Fail open at the gateway, fail closed at store creation** — deliberately opposite. A billing outage must
not take every storefront offline; it must stop new unbilled stores. This needs a javadoc paragraph in both
places or a later PR will "fix" the inconsistency.

Per decision 6, public storefront traffic (host-routed through Caddy, never through this filter) stays up.

**(d) Pod-side enforcement.** Each enforcing pod adds `billing-external-api` and builds
`ExternalEntitlementService` + a Caffeine-backed `StoreEntitlements` guard in its `ClientsConfig`. Two
points: a `HandlerInterceptor` registered by that service's own `WebMvcConfigurer` refusing non-`GET`
requests to `/api/*/private/**` with 402 when `!operable()` (reads stay open so a seller can see their data
and pay); and explicit quota calls where a count is known — `MAX_PRODUCTS` in catalog's product-create,
`MAX_ACCOUNTS` in merchant's user-create, `MAX_ORDERS_MONTH` in checkout's order placement — each throwing
`EntitlementExceededException` (422) with the key, limit and current value in `params`. Do **not** put this
in `ServletWebConfig` in autoconfigure; that belongs to the platform and must not learn about billing.
`StoreEntitlements` degrades **open** on a billing outage and logs at WARN.

### The org-trial-once rule

Entirely inside one transaction in the provisioning handler, enforced by a primary key:

```java
if (subscriptionRepository.existsById(store)) { return existing; }        // idempotent re-provision
try {
    trialGrantRepository.insert(OrgTrialGrantEntity.grant(org, store, trialEnd));   // PK = org_id
    entity = StoreSubscriptionEntity.trialing(store, org, trialPrice.plan(), trialPrice, trialEnd);
} catch (DbActionExecutionException | DuplicateKeyException e) {
    entity = StoreSubscriptionEntity.pending(store, org);                 // org already used its one trial
}
```

Two concurrent first-store creations: exactly one wins the insert, the other lands `PENDING`. No
read-then-write race, no lock. Use a real `INSERT` (`JdbcAggregateTemplate.insert` or a `@Modifying @Query`),
not `save`, which could become an `UPDATE`.

`ExpireTrialsJob` (`@Scheduled(cron = "0 */10 * * * *")`) selects `TRIALING and trial_end < now()` and fires
`ExpireTrialCommand` per row onto the outbox keyed by store id — the multi-instance-safety idiom from
`DeActivateNonRenewedSubscriptionsJob`, since there is no Shedlock in this repo. Same pattern for
`SuspendUnpaidSubscriptionsJob` and `ApplyPendingPlanChangesJob`.

---

## 7. Registration (all mandatory — miss one and it is unreachable)

1. **`settings.gradle`** — the four `'store-core:billing:billing-*'` entries.
2. **`store-commons/autoconfigure/src/main/resources/common-config.yml`** — a `billing` block under
   `com.asrevo.cvhome.services`, key **== `spring.application.name`** (`server.port` resolves through it),
   `port: 8021` (free: 8000/8001/8010/8020 taken in the core band), `domain: ${…app.domain}`,
   `namespace: store-core.cvhome.lcl`, `gateway-service-name: store-core-gateway`.
3. **`lcl-config.yml`** — a simple-discovery instance at `http://localhost:8021`.
4. **`fargate-config.yml`** — `"billing"` in `loadbalancer.eager-load.clients` **and** `"billing": 8021` in
   `ecs.discovery.service-ports`. Missing this works locally and is dead on AWS.
5. **`store-core/gateway/.../config/GatewayRouteLocatorImpl.java`** — *two* edits: the route
   (`path("/billing/**")`, `stripPrefix(1).tokenRelay().preserveHostHeader()`, `uri("lb://billing")`) **and**
   `"billing"` in the `backendServices` array. That array is negated to build the seller-ui catch-all, so
   without the second edit every `/billing/**` call returns seller-ui's shell HTML.
6. **`extra/scripts/run-lcl.sh`** — `"billing|:store-core:billing:billing-service|8021"` after control-plane,
   before gateway.
7. **`configure-domain.sh`** — nothing; billing is reached by path, not its own hostname.
8. **`application.yml`** — `name: billing`, imports `common-config.yml` + `plan-catalog.yml`, the store-core
   s2s registration, outbox `jdbc.schema-name: billing_outbox` with `schema-initialization.enabled: false`,
   and `com.asrevo.cvhome.billing.{trial-period: P14D, past-due-grace: P7D, quota.max-pending-stores: 3}`.
   `-lcl` imports `lcl-config.yml` + `store-core-lcl-config.yml`; `-fargate` imports `fargate-config.yml`.
   No `spring.sql.init.schema-locations` — `classpath:schema.sql` is the default and `mode: always` already
   comes from `common-config.yml`.

Never hand-roll a `@ControllerAdvice`, argument resolver, JWT decoder or permission evaluator —
`store-commons:autoconfigure` supplies them, and a second advice is a review reject.

---

## 8. Phases

1. **The service exists and answers.** Modules + all four registrations + the two gateway edits +
   `run-lcl.sh` row + `BillingApplication`/`SecurityConfig`/`ClientsConfig`/`JdbcConfig`/`SchedulingConfig` +
   full `schema.sql` + `plan-catalog.yml` + plan-catalog domain/repos/seeder (DB only, Stripe sync off) +
   `PlanCatalogApi` + the `billing-commons` error types + `plan-catalog-api.http`. No Stripe, no
   subscriptions. This must be **one** PR: a half-registered service cannot be exercised at all.
   *Gate:* `GET gateway.com:8000/billing/api/v1/plan/public/plans` returns the seeded catalog.
2. **Subscription lifecycle, no money.** `StoreSubscriptionEntity` + state machine + `OrgTrialGrantEntity` +
   `SubscriptionService` + `ExternalStoreQuotaApi` + `SubscriptionApi.current` + audit writes +
   `billing-events` + outbox wiring + `ExpireTrialsJob`; layer (b) wired into `StoreManagerServiceImpl`, and
   the provisioning `@OutboxHandler` on `StoreCreatedEvent`.
   *Gate:* store #1 `TRIALING`, store #2 `PENDING`, verified under concurrent creation.
3. **Stripe.** `StripeConfig` + the three gateways + checkout + webhook + `processed_stripe_event` +
   `stripe_request` + `WebhookIngestService` + handlers + invoices + `InvoiceApi` + catalog sync on. The
   error split lands here and is the review focus.
   *Gate:* `stripe listen` → test card → `PENDING → ACTIVE` + invoice row; replaying the event changes
   nothing.
4. **Plan changes.** upgrade / downgrade (`SubscriptionSchedule` + pending change + job) / cancel / resume.
   Ship downgrade *without* the usage pre-check; it needs phase 6's counters.
5. **Layers (a) + (c).** `ExternalEntitlementApi` + gateway poller + 402 filter + `billingStatus` on
   `ManagerStoreDto`.
6. **Layer (d).** `StoreEntitlements` + write-gate interceptor + quota calls, **one PR per pod service** so
   each is QA'd against a real store. Backfill the downgrade usage pre-check.
7. **seller-ui.** Rewire `store-core/seller-ui/src/app/pages/subscription-and-usage/` from control-plane's
   org endpoints to `/billing/**` per-store endpoints. Separate design.
8. **Retire control-plane's org-level subscription** — delete `controlplane/subscription/**`, the
   `subscription` schema and the three modules, after migrating each org's plan onto its stores. Last.

---

## 9. Verification

**Every PR:**

```bash
./gradlew checkstyleMain checkstyleTest                          # warnings = errors; a TODO fails the build
./gradlew build -x test -x check
./gradlew :store-core:billing:billing-service:test               # Docker running (Testcontainers)
./gradlew :store-core:control-plane:control-plane-service:build  # phase 2 touches it
./gradlew :store-core:gateway:gateway-service:build              # phases 1 and 5 touch it
```

Plus the error-handling grep gate over `store-core/billing` — zero hits for `throws BaseException` or any
category base, and no `catch (BaseException)` + `switch (category())`.

**Stack:** `./extra/scripts/run-lcl.sh --list` shows `billing`; check `lsof -i :8000` first; background the
run and stop it with **SIGTERM**, never SIGINT.

**Stripe locally:**

```bash
stripe listen --forward-to http://gateway.com:8000/billing/api/v1/stripe-webhook/public/events
#   → whsec_...  goes into com.asrevo.cvhome.stripe.webhook-signing-key for the lcl run
stripe trigger checkout.session.completed | invoice.payment_succeeded | invoice.payment_failed
stripe events resend evt_...      # replay: row untouched, log says "duplicate"
```

End to end: `POST /billing/api/v1/subscription/checkout?store=…` → open the url → `4242 4242 4242 4242` →
store flips `ACTIVE`. Decline `4000 0000 0000 0341` → `PAST_DUE`. Upgrade decline `4000 0000 0000 0002` must
return **422**, not 502, with the local plan unchanged.

**Tenant isolation and the permission gate — mandatory, not the happy path:** org 1's admin session against
`store={{STORE_ID_2}}` must be **403** on `current`, `invoice/list`, `upgrade`, `cancel`; repeat every read
as the second store's own admin and confirm each sees only its own rows. A `ROLE_STORE_MODERATOR` must get
200 on `READ` and 403 on `MANAGE` — test both directions, since a missing `case` in
`CustomPermissionEvaluator` shows up as a silent 403 on the read path too.

**Tests:** `SubscriptionStatusTransitionTest` (full legal/illegal matrix, unit),
`StripeGatewayErrorSplitTest` (mocked `CardException` → 422, `ApiConnectionException` → 502 with the row
untouched, unit), `OrgTrialGrantConcurrencyTest` (two threads, one grant), `WebhookIdempotencyTest` (same
`event.id` twice, one state change), `SchemaConstraintTest` (every enum column rejects an invalid literal —
this keeps the CHECK constraints honest as the enums grow). The last three are `@Tag("integration-test")`
with Testcontainers Postgres.

---

## 10. Open risks

1. **`SubscriptionSchedule` for downgrades** is the highest-complexity piece and the most likely to behave
   differently from the docs. Phase 4 fallback if it fights back: `cancel_at_period_end = true` plus an
   auto-created checkout for the new plan at period end — uglier for the customer, but it uses only
   mechanisms proven in phase 3.
2. **The fail-open/fail-closed asymmetry** (§6) will read as an inconsistency to a reviewer. Javadoc it in
   both places.
3. **One Stripe customer per org** is right for a shared payment method and portal, but one dunning failure
   can put several stores into `PAST_DUE` at once. Notification copy must name *which* stores.
4. **`processed_stripe_event`, `subscription_audit` and `outbox_record` grow unbounded**
   (`delete-completed-records` is false repo-wide). Needs a retention job before production; not a phase-1
   blocker.
5. **`billing.stripe_request` records intent, not outcome.** A crash between insert and call leaves
   `completed_at` null — that is exactly what makes the retry safe under the same key. Do not "clean up"
   those rows.
6. **The catalog is not reproducible from `plan-catalog.yml` alone across environments**, because Stripe ids
   are per-account and written back. Inherent; do not try to pin them in the yaml.
7. **Re-check that port 8021 is still free** in `common-config.yml` at implementation time.

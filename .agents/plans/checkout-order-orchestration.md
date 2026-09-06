# Checkout rewrite — robust order orchestration

> **Rebased on `origin/main` @ `4dbfe9c67`** (product variants #306, QA-per-service #312). Every finding below
> was re-verified against that commit, not the older local tree. No local rebase is needed: per AGENTS.md the
> work is cut fresh — `git worktree add .claude/worktrees/refactor-checkout-order-saga -b
> refactor/checkout-order-saga origin/main`.

## Context

`store-pod/checkout` orchestrates the two things in this system that cost real money: inventory reservation and
payment. It is also the least defended service in the repo — Shopizer-derived code with an exploitable
authentication bypass, three write paths that violate their own DB constraints, no optimistic locking, no
durable record of an in-flight order, and a reconciliation story that amounts to a log line saying "manual
intervention required".

The repo already anticipates this. `build.gradle:18` says checkout stays at zero coverage "until their legacy
code is refactored", and `pod-commons` is "queued behind the checkout refactor".

**Goal:** rebuild checkout's internals into a durable, idempotent, tenant-safe order orchestrator, keeping the
existing schema and the public API contract so the storefront and console keep working.

**Decisions taken (fixed):**

1. One PR series — security fixes are folded in, not shipped separately, but they land in **PR 1**.
2. **Durable saga + guarded state machine + reconciler.** Synchronous happy path, so storefront UX is unchanged.
3. **Keep the schema and the public API.** Strangler rewrite, additive DDL only, no data migration, no v3 API.
4. Cart stays in the checkout pod, cleanly separated behind its own service boundary.

### What changed on main, and what it means for this plan

| Change | Effect |
|---|---|
| **QA convention (#312)**: scripts moved to `<service>/qa/<module>-qa.md`, one per **service**, never per plan | Cases get **appended to the existing `store-pod/checkout/checkout-service/qa/checkout-qa.md`** (246 lines, sections `00 / CHK / AGR / ORD / SEC / STA / VAR / 99`). Structure to copy is now `store-core/billing/billing-service/qa/billing-qa.md`. Its `ORD-02` (failed payment releases the reservation), `ORD-03` (reservation expiry tells checkout) and `SEC-01` (every private endpoint refuses without a session) are all tagged `[not verified]` — those are exactly the paths proven broken below. |
| **Product variants (#306)** rewrote part of checkout's mapper layer | `OrderProductPopulator` and `ReadableOrderProductPopulator` are **deleted**; `OrderLineMapper` + `ReadableOrderProductMapper` replace them. `OrderProductAttribute` → `OrderProductOption` (+ `order_product_option` table). Only **two** populators remain (`PersistableOrderApiPopulator`, `ReadableOrderPopulator`). The repo has already set the populator→mapper direction — the restructure PR shrinks accordingly. |
| Variants also added the **first real unit tests** in `checkout-core` | `OrderLineMapperTest`, `ProductDetailsComposerImplTest`, `CartRefusalErrorsTest`. The coverage ratchet starts from a non-zero base. |
| Skills now live at `.agents/skills/project-structure/` | Both paths exist; AGENTS.md references the `.agents` one. |

**Unchanged, and therefore still the whole problem:** `OrderPlacementFacadeImpl` has **no diff** against the
new main. `OrderFacadeImpl.saveOrder`'s idempotency check is untouched. Both critical defects below are live on
`4dbfe9c67`.

---

## What is broken

Every row re-verified against `origin/main@4dbfe9c67`.

### Security

| # | Defect | Evidence |
|---|---|---|
| **S1** | **Auth bypass — any shopper account can mark any order PAID.** `ExternalOrderApi` has **zero** `@PreAuthorize` (verified: `grep -c` = 0 on the new main). `SecurityConfig` gates `/api/*/private/**` on `.authenticated()` only; checkout trusts **cua shopper tokens**; `spg` proxies `/checkout*` wholesale. `POST /checkout/api/v1/private/order/{id}/payment-status?status=PAID&store=<any>` → order CONFIRMED, inventory COMMITTED, nothing charged. The `store` param is attacker-controlled and unvalidated. | `ExternalOrderApi.java:47,84`; `SecurityConfig.java:16`; `store-pod-lcl-config.yml:17-21`; `spg/Caddyfile:83-91` |
| S2 | The coarse filter chain (`anyRequest().permitAll()`, method-level `@PreAuthorize` as the real gate) is the **identical house pattern in all five pod services** — not a checkout defect, and not to be changed here. What it means is that a missing `@PreAuthorize` **fails open, silently**, which is how S1 happened. `ExternalOrderApi` is the only pod `External*Api` without one. The fix is mechanical enforcement, not a divergent filter chain. | `SecurityConfig.java:16`, identical in inventory/catalog/content/merchant |
| S3 | **IDOR on order status.** `orderStatus(orderId)` checks only "is a shopper of this store", never ownership. If the store does not require login it is unauthenticated entirely. | `OrderApi.java:154-165` |
| S4 | **Two** store-unscoped writes on the callback path, not one: `OrderServiceImpl.updateOrderStatus` does `orderRepository.findById(orderId)` with **no store predicate** (verified), so even with S1 fixed the callback could write cross-tenant by id alone. `OrderRepository.findExpiredOrders` is likewise unscoped. | `OrderServiceImpl.java:156`; `OrderRepository.java:166-167` |
| S5 | No s2s permission token exists for checkout at all. Inventory gates all three of its s2s methods with `STORE-POD.INVENTORY.RESERVE` → `isSameStorePod`. | `CustomPermissionEvaluator.java:20-38`; `ExternalProductReservationApi.java:42,52,60` |

### Correctness

| # | Defect | Evidence |
|---|---|---|
| **C1** | **Three write paths violate the `payment_status` CHECK constraint.** It still allows only `PENDING, PAID, FAILED, AUTHORIZED, REFUNDED` (verified on the new main, `schema.sql:264-277`), but the code writes `EXPIRED` and `CANCELLED`. On any freshly-provisioned environment — every lcl stack, every new pod/region — **order expiry never works and cancelling an order from the console throws.** | constraint `schema.sql:264-277`; writers `OrderTimeoutService.java:35-41`, `ExternalOrderApi.java:90`, `OrderFacadeImpl.java:397-400` |
| C2 | Root cause: `align-checkout-inventory-payment-flow.md:41-42` added `PROCESSING/EXPIRED/WAITING_VERIFICATION/REJECTED` to the enum and never updated the DDL — the "new enum value not added to the CHECK constraint" violation, shipped. `inventory_status` likewise omits `AVAILABLE`. | that plan doc |
| **C3** | **Expiry is broken three independent ways.** (a) the constraint above; (b) `@Transactional` wraps the whole batch while making remote HTTP calls, and `catch (Exception e)` inside the loop swallows the failure while the transaction is already rollback-only — so the batch fails silently at commit; (c) **`orders.date_purchased` is `date` while the field is `Instant`, and it is written** (`setDatePurchased(Instant.now())`), so every purchase timestamp is truncated to midnight and a 30-minute cutoff is day-granular. | `OrderTimeoutService.java:29-62`; `schema.sql:207`; `PersistableOrderApiPopulator.java:46` |
| **C4** | **COD and manual-transfer orders are auto-cancelled 45 minutes after placement.** Inventory's `ReservationExpiryJob` releases *every* `TEMPORARY_RESERVED` reservation past `expireAt` regardless of payment type and calls `handleReservationExpired` → checkout cancels. COD is documented as "no timeout" and manual transfer as "24–48 h". | `ReservationServiceImpl.java:39` (`reservation.expiry.minutes:45`); `ReservationExpiryJob.java:32-47` |
| C5 | **No optimistic locking anywhere in checkout** — no `@Version`, no version column, no `@Lock`. The payment webhook races the synchronous placement path on the same row; last writer wins. | grep: zero hits across `checkout-core` |
| C6 | **Retry re-drives external calls.** `saveOrder` correctly returns the existing order for a resubmitted cart, but `placeOrder` then re-reserves and re-initiates payment on it anyway. | `OrderFacadeImpl.java:111-117` + `OrderPlacementFacadeImpl.java:58-79` |
| **C7** | **No durable record of intent.** `placeOrder` is a synchronous in-method sequence (correctly non-transactional — it does remote I/O), but a crash between reserve and initiate is recoverable only by the timeout job, which is broken three ways. The PAID-but-commit-failed branch writes `PENDING_PAYMENT/RESERVED/PAID` and logs "Manual intervention required" — no retry, no queue, no alert, no audit trail. | `OrderPlacementFacadeImpl.java:51-127`, esp. `:90-94` |
| C8 | **Three orthogonal status enums with no state machine**, set independently from three call sites; illegal combinations are reachable and written. The documented spec disagrees with the enum (`checkout-flow.md` uses an order status `PENDING` that does not exist; the enum's `SHIPPED`/`RETURNED` appear in no rule). | `checkout-flow.md:701-742` vs `OrderStatus.java:6-7` |
| C9 | `ShoppingCartItem.itemPrice`/`subTotal` are **`@Transient`** — populated in one service call and read in a *different* one (`OrderServiceImpl.calculateOrder:113`). That only works because `spring.jpa.open-in-view` defaults to `true` and the same managed instance survives the request. Turn OSIV off and order totals NPE. | `ShoppingCartItem.java:67-73` |
| C10 | `payment.rejectPayment` sets `REJECTED` and registers **no event**, so checkout is never told a manual-transfer proof was rejected. | `TransactionServiceImpl.java:133-139` |

### Conventions and coverage

- **`checkout-service/http/` still does not exist.** The four stale root files (`missed/products/requests/store.http`) are still there and still point at catalog ports; the skill lists them as "known-stale, do not copy".
- Coverage floor `[unit: 0.0, integration: 0.0, merged: 0.0]` (`build.gradle:42`), now with three real `-core` tests to build from.
- `CheckoutArchitectureTest:18-25` disables `API_GOES_THROUGH_SERVICES` (v2 statistic APIs inject repositories), plus a `controllersLiveIn` deviation for `controller.v1.auth`.
- **ArchUnit blind spot:** `CvhomeArchitectureRules.sub(domain, "services")` matches `..checkout.services..` only. Everything in `service/` (singular) — all facades, mappers and the remaining populators, i.e. the bulk of the order logic — is **outside every layering rule today**. Renaming is not cosmetic.

---

## Target architecture

### The state machine — one authoritative state, **derived**

`OrderState` is computed from the existing triple plus `payment_type`. **No new state column and no backfill**
— which is what "additive DDL only, no migration" demands, and it has a second payoff: the wedged triple the
current code writes (`PENDING_PAYMENT / RESERVED / PAID`, the "manual intervention" branch) becomes a *named,
queryable, reconciler-owned* state on day one.

*Rejected:* adding `orders.order_state` and backfilling — a data migration that creates two sources of truth
which drift the moment anything writes one without the other.

New in `checkout-commons` (`model/order/state/`): `OrderState`, `StatusTriple`.

```java
public enum OrderState {
    DRAFT, STOCK_RESERVED, STOCK_REFUSED,
    AWAITING_PAYMENT, AWAITING_VERIFICATION, PAYMENT_REJECTED,
    PAYMENT_RECEIVED, AWAITING_REFUND,
    CONFIRMED, PROCESSING, SHIPPED, DELIVERING, DELIVERED, COMPLETED,
    RETURNED, REFUNDED, PAYMENT_FAILED, EXPIRED, CANCELLED;
}
public record StatusTriple(OrderStatus order, InventoryStatus inventory, PaymentStatus payment) { }
```

`BASE` is a static `EnumMap<OrderState, StatusTriple>` projecting onto the three legacy columns, e.g.
`AWAITING_PAYMENT → (PENDING_PAYMENT, RESERVED, PENDING)`, `CONFIRMED → (CONFIRMED, COMMITTED, PAID)`,
`EXPIRED → (CANCELLED, RELEASED, EXPIRED)`, `AWAITING_REFUND → (PENDING_PAYMENT, RESERVATION_FAILED, PAID)`.
**COD is the one variation** and is a function of the payment type already on the row, so the mapping stays a
bijection *per payment type*:

```java
private static final Set<OrderState> COLLECT_ON_DELIVERY = EnumSet.of(CONFIRMED, PROCESSING, SHIPPED, DELIVERING);

public StatusTriple project(PaymentType type) {
    StatusTriple base = BASE.get(this);
    return type == PaymentType.COD && COLLECT_ON_DELIVERY.contains(this)
            ? base.withPayment(PaymentStatus.PENDING) : base;
}
public static Optional<OrderState> parse(StatusTriple triple, PaymentType type) { ... }   // INDEX, built statically
```

A unit test asserts the map is **total** (every state projects) and **injective** (no two states share a triple
for the same payment kind) — that is what keeps `parse` honest as states are added.

`canTransitionTo` encodes the legal edges; staying put is legal everywhere (payment redelivers webhooks and the
reconciler re-runs). `STOCK_REFUSED, PAYMENT_FAILED, EXPIRED, CANCELLED, REFUNDED, RETURNED` are terminal. Two
edges worth calling out:

- **`STOCK_RESERVED → CONFIRMED` is the COD lane** — commit at placement. COD has no payment that can fail, and
  it is the only fix for C4 that needs no inventory change. `payment_status` stays `PENDING` through fulfilment
  and flips to `PAID` at `DELIVERED`, which is what the money is actually doing. *This is the one deliberate
  behaviour change in the series* — see Risks.
- **`CANCELLED` is illegal from any committed state.** Post-commit the compensating path is `RETURNED`. Today
  `OrderFacadeImpl.createOrderStatus` lets a merchant set `CANCELLED` at any point and only releases stock if it
  happens to be `RESERVED` — a silent committed-stock leak. The guard refuses it with a typed 409.

**The guard lives on the aggregate**, mirroring `StoreSubscriptionEntity` (billing) and `ContentStatus`
(content): one named method per move, idempotent on the current state.

```java
// Order.java — @Version added; the three status setters removed entirely (not just narrowed)
public Order recordPayment() throws IllegalOrderTransitionException { return moveTo(PAYMENT_RECEIVED, "payment received"); }
public Order confirm()       throws IllegalOrderTransitionException { return moveTo(CONFIRMED, "stock committed"); }
// … reserveStock, refuseStock, awaitPayment, awaitVerification, rejectProof, advanceFulfilment,
//    failPayment, expire, cancel, awaitRefund, markReturned

private Order moveTo(OrderState target, String comment) throws IllegalOrderTransitionException {
    OrderState current = currentState().orElseThrow(() -> IllegalOrderTransitionException.unparseable(id, triple()));
    if (current == target) return this;                        // redelivered webhook: no second history row
    if (!current.canTransitionTo(target)) throw IllegalOrderTransitionException.of(id, current, target);
    return apply(target, comment);
}

/** The reconciler's escape hatch: payment.status(ref) is the authority on money, so it may overrule a row the
 *  old code wedged into a combination OrderState does not name. Audited like any other move. */
public Order reconcileTo(OrderState target, String reason) { return apply(target, "reconciled: " + reason); }
```

`apply` writes all three columns together and appends exactly one history row per `order_status` change.
`OrderService.updateOrderStatus` (both overloads) and `OrderFacade.updateOrderStatus` (both) are **deleted** —
that is what closes S4's unscoped `findById`.

`@Version` on `Order`; contention is real (payment's outbox handler calls `updatePaymentStatus` while placement
still holds the row), so `OrderStateService` retries on `ObjectOptimisticLockingFailureException` up to 3 times
and then leaves the saga row due — the move is idempotent, so a retry that finds it already applied is a no-op.

### The durable saga — dedicated table, **not** the outbox

Reasoning to state in the PR body:

1. **The reconciler pulls; the outbox pushes.** The primitive is `GET /payments/{ref}/status` — "ask again in
   30 s until the shopper comes back from Stripe". `namastack-outbox` has no self-rescheduling record; polling
   would append a row per poll.
2. **`max-retries: 3` is a fatal mismatch.** With 2s→4s→8s backoff, a 15-second gateway blip burns the whole
   budget and marks every in-flight order `FAILED` **permanently**, with no self-healing. Money compensation
   needs an unbounded backed-off budget and a named give-up state a human sees.
3. `stop-on-first-failure: true` and `delete-completed-records: false` (append-only) are wrong shapes here;
   checkout is the pod's highest-volume writer.
4. **Cost:** outbox = 3 tables + starter + poller + heartbeat rows for a fit it does not have. Saga = 2 tables
   we need anyway — the attempt log **is** the audit trail the current code lacks.
5. **Precedent:** the repo's pattern for "scheduled job that pulls and compensates" is `ReservationExpiryJob`
   and `OrderTimeoutService`. The outbox's precedent is "an aggregate recorded something; tell a peer once" —
   which is exactly what payment already does *to* checkout, and stays that way.

Checkout gains no outbox in this series. `SalesManagerEntity extends AbstractAggregateRoot`, so if checkout
later needs to *emit* (`OrderConfirmedEvent` → notifications) the hook is already there.

Two additive tables in `checkout-service/src/main/resources/init-sql/schema.sql` — `checkout.order_saga`
(`order_id`, `store_merchant_id`, `idempotency_key`, `saga_state`, `payment_type`, `reserve_expires_at`,
`payment_deadline`, `next_attempt_at`, `attempt_count`, `last_error_code`, `version`; unique
`(store_merchant_id, order_id)` and `(store_merchant_id, idempotency_key)`; index on
`(saga_state, next_attempt_at)`) and `checkout.order_saga_step` (`step`, `attempt_no`, `outcome`, `remote_ref`,
`error_code`, `started_at`, `finished_at`; unique `(order_saga_id, step, attempt_no)`). Both with `CHECK`
constraints on their enum columns, and two `sm_sequencer` seed rows in `data-common.sql` — `ddl-auto: update`
must not be the thing that creates these.

**Idempotency keys.** Placement keys on `shoppingCartCode` (already unique as `orders.cart_code`). Inventory
and payment both key on `ref = order.getId().toString()`, and both already enforce it —
`product_reservation (store, ref)` and `transaction (request_ref, store_merchant_id)`. So the order id is
*already* a correct idempotency key; the fix for C6 is simply to **stop re-driving steps that already
succeeded**:

```java
Order order = orderService.findOrCreateForCart(cart, customer, store, language);   // tx 1
OrderSaga saga = sagaService.openOrJoin(order, cart.cartCode());                   // tx 2
if (saga.settled()) return OrderPlacementResult.of(order);   // resubmit — no external call whatsoever
return driver.drive(saga, order, successUrl, cancelUrl);     // synchronous happy path, UX unchanged
```

**Write-ahead invariant:** *a step row with `outcome = STARTED` is committed before the call leaves the
process; the outcome is written in a separate transaction after it returns.* A crash therefore always leaves a
`STARTED` row — the durable record of intent that C7 lacks. `beginStep` is
`@Transactional(propagation = REQUIRES_NEW)`; remote I/O happens outside every transaction. `REFUSED` (422,
decided) and `UNDECIDED` (502, nothing decided) never share a `catch`, per `error-handling.md`.

**The reconciler** (`@Scheduled`, claims due rows with `select … for update skip locked` — no ShedLock exists
in this repo, and `SKIP LOCKED` is both dependency-free and safely parallel; inventory already uses
`PESSIMISTIC_WRITE`) treats **`payment.status(ref)` as truth**. That call exists today and checkout never makes
it; turning it on is the single most valuable change in the series. Backoff
`min(30s · 2^attempts, 15m)`; past ~12 attempts → `ABANDONED` + `log.error`, order left visible in a
non-terminal state rather than guessed at.

**Recovery matrix:**

| Crash point | Row left behind | Action | Truth |
|---|---|---|---|
| Before reserve | saga `OPENED` | drive from the top | — |
| **Reserve call left, no outcome written** | `RESERVE / STARTED` | `release(ref)` (idempotent), then `expire()` | inventory |
| Reserve refused (422) | `RESERVE / REFUSED` | nothing to unwind → `STOCK_REFUSED` | — |
| Reserve undecided (502) | `RESERVE / UNDECIDED` | `release(ref)` then `expire()` | inventory |
| **Initiate left, no outcome written** | `INITIATE_PAYMENT / STARTED` | **`payment.status(ref)`** — `PAID`→commit→`CONFIRMED`; `PENDING/PROCESSING`→re-check until deadline; `FAILED/CANCELLED`→release→`PAYMENT_FAILED`; *no transaction*→release→`CANCELLED` | **payment** |
| Initiate undecided (502) | `INITIATE_PAYMENT / UNDECIDED` | `payment.status(ref)` — **never assume failure**. Today this branch throws out of `placeOrder` with the reservation held | payment |
| **PAID but commit failed** | `PAYMENT_RECEIVED`, `COMMIT / STARTED` | retry `commit(ref)`; `status=false` (reservation gone) → `AWAITING_REFUND` + `log.error` | inventory |
| Crash during release | `RELEASE / STARTED` | `release(ref)` again → intended terminal state | inventory |
| Webhook mid-placement | order already moved | `moveTo` no-ops; `@Version` resolves the write race | — |
| **Triple `parse` does not recognise** (legacy row) | `currentState()` empty | `payment.status(ref)` → `reconcileTo(...)`, reason in history | payment | 
| Payment deadline passed | `AWAITING_PAYMENT` / `AWAITING_VERIFICATION` | **ask `payment.status(ref)` first**, only then release + `expire()` | payment |

That last row is the point of the whole design: it is what stops the system cancelling an order the gateway
says is paid. `payment_deadline` is per payment type (`stripe: 30m`, `paypal: 30m`, `manual-transfer: 48h`,
COD: none — already `CONFIRMED`), replacing `OrderTimeoutService`'s hard-coded durations.

### Module layout

```
store-pod/checkout/
├── checkout-commons/        + model/order/state/{OrderState, StatusTriple}, errors/IllegalOrderTransitionException
├── checkout-cart/           NEW module — the cart bounded context
├── checkout-external-api/   two-interface split + CheckoutApiErrors (PR 7)
├── checkout-core/           order aggregate, saga, reconciler, services
└── checkout-service/        api/, config/, schema.sql, http/, qa/
```

A real Gradle module for the cart follows existing precedent — `store-pod/commons/customer/customer-core` and
`reference/reference-core` are separate modules sharing the `checkout` persistence unit and package root. It
gives a compile-enforced boundary; the ArchUnit rule gives a second one. `checkout-cart` exports exactly one
thing to the order side:

```java
public record CartSnapshot(Long cartId, String cartCode, StoreMerchantId store, List<CartLine> lines) { }
public record CartLine(String sku, int quantity, String name, BigDecimal unitPrice, BigDecimal lineTotal) { }
```

Pricing becomes **eager**, which is what removes C9's `@Transient`/OSIV dependency and lets
`spring.jpa.open-in-view: false` be set safely.

`checkout-core` collapses to the catalog/inventory shape — `entity/`, `repositories/`, `services/<domain>/` —
with `service/` (singular) gone, which also puts ~15 previously-unenforced classes under the ArchUnit layering
rules. `OrderInventoryOrchestrator` becomes `InventoryGateway`: a thin typed wrapper that does **not** write
status (the aggregate does). `customer-core` and `reference-core` keep their layout — different domain,
explicitly deferred in `build.gradle:39-40`.

### `checkout-external-api` and the permission token

Do the two-interface split in PR 7, once the callbacks have typed failures worth mapping: `IOrderCallbackService`
(server vocabulary, implemented by the controller) + `ExternalOrderService` (`@HttpExchange`, caller vocabulary,
declaring `CheckoutOrderRejectedException` — decided, do not retry — and `CheckoutApiUnavailableException` —
undecided, retry) + `CheckoutApiErrors.CHECKOUT`. This removes the `UncheckedBaseException` smuggling that
`ExternalOrderApi:30-40` currently documents as a workaround, and lets payment's outbox stop burning its retry
budget on a permanent rejection.

New token **`STORE-POD.CHECKOUT.ORDER-CALLBACK`** → `isSameStorePod`. Named for the capability, not the caller
(payment and inventory both use it). Deliberately not `STORE-POD.CHECKOUT.*`, which resolves to
`hasManageAccessOnStore` and would demand a store-admin principal. Both callers already register the same s2s
client (`store-pod-…@service.store-pod.internal`, scope `store_pod`), which is exactly what `isSameStorePod`
accepts — so the gate will not break the pod.

**Note:** `@PreAuthorize` binds by *parameter name*, so `store` must be renamed to `merchantStore` in both
`ExternalOrderService` and `ExternalOrderApi` for `#merchantStore` to resolve. Argument resolution is by type,
so the rename is safe for the generated proxy.

---

## PRs

Each builds, passes `./gradlew check`, and leaves the system working. Coverage floors in `build.gradle:42` are
ratcheted every PR — run `./gradlew perServiceCoverage`, set to achieved rounded down, never lower.

**PR 1 — `fix(checkout): gate the service-to-service order callbacks and fix the status CHECK constraints`**
S1, S5, C1, C2, and the `date_purchased` half of C3. New token in `CustomPermissionEvaluator` +
`PermissionAccessChecker`; `@PreAuthorize` on both `ExternalOrderApi` methods; param rename. `schema.sql` gains
idempotent `drop constraint if exists` / `add constraint` for both status checks plus
`alter column date_purchased type timestamp(6)` — `spring.sql.init.mode: always` means `schema.sql` runs every
boot, so `ALTER` is the right vehicle (a `CREATE TABLE IF NOT EXISTS` edit alone would never reach an existing
table). `OrderTimeoutService` loses its batch `@Transactional` and gets per-order `REQUIRES_NEW` + ERROR logging
— honest until PR 4 deletes it. Tests: shopper token → 403, staff token → 403, pod s2s token → 200, cross-store
→ denied, and expiry actually writing `EXPIRED` against real Postgres. **`[0.10, 0.15, 0.20]`**

**PR 2 — `fix(checkout): bind the order-status lookup to the shopper who placed it`**
S3. Additive `orders.confirmation_token` + partial unique index; generated at creation; `orderStatus` authorises
on token match **or** resolved-customer ownership, else 404 (not 403 — a 403 confirms the id exists, and
`CheckoutErrors.ORDER_NOT_FOUND`'s javadoc already says why). Legacy rows (null token) fall back to ownership —
no migration. Checkout builds the success/cancel URLs, so the token reaches the browser with no contract change.
Frontend: **two files** (`libs/hooks/src/use-order-status.ts`, `libs/services/src/order-service.ts`); the 13
themes already delegate the fetch to the hook. **`[0.15, 0.22, 0.28]`**

**PR 3 — `refactor(checkout): one guarded order state, projected onto the legacy columns`**
C5, C8, S4, and C4. `OrderState`/`StatusTriple`, the guarded aggregate, `@Version`, `OrderStateService` with
conflict retry. `updateOrderStatus` deleted everywhere (closing the unscoped `findById`); `findExpiredOrders`
gains a store predicate. COD commits at placement. **`[0.40, 0.30, 0.50]`**

**PR 4 — `feat(checkout): a durable placement saga with a payment-truth reconciler`**
C6, C7, C10, and the rest of C3. The two saga tables, `services/placement/*`, `OrderPlacementFacade` deleted,
`OrderTimeoutService` deleted and its sweeps folded into `OrderReconciler`. Folded-in payment fix:
`Transaction.rejected()` registers `PaymentRejectedEvent` + handler arm, without which `PAYMENT_REJECTED` is
unreachable and manual-transfer rejections silently expire. **`[0.58, 0.48, 0.70]`**

**PR 5 — `refactor(checkout): the cart as its own bounded module`**
C9. New `checkout-cart` module, `CartSnapshot`/`CartLine`, `@Transient` fields deleted,
`spring.jpa.open-in-view: false`, `CART_INTERNALS_ARE_PRIVATE` ArchUnit rule. **`[0.66, 0.56, 0.78]`**

**PR 6 — `refactor(checkout): drop the remaining facade and populator layers`**
Smaller than originally scoped — #306 already deleted the two order-product populators and set the direction.
`service/` → `services/` (which is what actually puts the order logic under the layering rules),
`PersistableOrderApiPopulator`/`ReadableOrderPopulator` → mappers, `OrderStatisticService` so the v2 APIs stop
injecting repositories, `AuthController` moved into `api/v1/auth`. Both ArchUnit deviations deleted;
`ONLY_THE_AGGREGATE_SETS_STATUS` added. **`[0.75, 0.62, 0.82]`**

**PR 7 — `refactor(checkout): split the callback contract and give it an error catalog`**
The two-interface split, `CheckoutApiErrors.CHECKOUT`, both `ClientsConfig` call sites off
`RemoteErrorCatalog.none()`, and narrowed `catch` arms in `PaymentOutboxHandler` and `ReservationExpiryJob`.
**`[0.78, 0.65, 0.84]`**

**PR 8 — `docs(checkout): runnable requests and the QA script`**
Create `checkout-service/http/` — one file per `*Api` class, gateway-addressed
(`{{SELLER_UI_URL}}/spg/checkout/…?store={{STORE_ID}}&lang={{LANG}}`), each with a non-2xx block — and delete
the four stale root `.http` files. Append to the **existing** `checkout-service/qa/checkout-qa.md`. Update
`checkout-flow.md` / `order-placement-flow.md` / `align-checkout-inventory-payment-flow.md` to the one
`OrderState` table. **`[0.80, 0.70, 0.85]`** — the target.

---

## Verification

```bash
./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest
./gradlew build -x test -x check
./gradlew :store-pod:checkout:checkout-core:test :store-pod:checkout:checkout-service:test
./gradlew :store-pod:checkout:checkout-service:integrationTest    # Docker running
./gradlew perServiceCoverage
```

**Unit (`-core`/`-commons`/`-cart`)** — `OrderStateTest` (totality + injectivity of the projection, the whole
transition table as a `@ParameterizedTest`, COD's payment lane, terminals); `OrderTest` (guard, idempotence on
the current state, exactly one history row per change, `cancellingAfterCommitIsRefused`, `reconcileTo`);
`OrderSagaDriverTest` (**step written before the call**; refusal and undecided never collapse);
`OrderReconcilerTest` (one test per matrix row, `MutableClock`), including
`paymentSaysPaidSoAnExpiredLookingOrderIsConfirmedNotCancelled()`; `OrderTotalCalculatorTest`; `CartPricingServiceTest`.

**Crash simulation without killing JVMs.** A crash *is* "a `STARTED` step with no outcome and `next_attempt_at`
in the past". Every recovery test seeds exactly that row shape and asserts one reconcile pass — deterministic,
fast, and the same fault a real crash produces.

**Integration (`checkout-service/src/integrationTest`)**, collaborators stubbed at the `External*Service` bean
so 422-vs-502 is scriptable: `ExternalOrderApiIntegrationTest` (the S1 bypass as an explicit regression);
`OrderApiIntegrationTest` (placement per payment type, the IDOR set); `OrderPlacementIntegrationTest`
(`resubmittingTheSameCartMakesNoSecondReservation()` asserting **zero** further stub calls, redelivered webhook
commits once); `OrderReconcilerIntegrationTest`; `OrderLifecycleIntegrationTest` (409 on an illegal console
transition). Every store-scoped class owes its **tenant-isolation** and **403 permission-gate** cases, per
`testing.md`.

**End to end** — a user-visible change is not done until exercised (AGENTS.md):

```bash
git worktree add .claude/worktrees/refactor-checkout-order-saga -b refactor/checkout-order-saga origin/main
cd .claude/worktrees/refactor-checkout-order-saga && lcl start -d --stack ckout
lcl urls --stack ckout          # read live ports; never assume
```

Then per `checkout-qa.md`: place COD, Stripe (complete **and** abandon) and manual-transfer orders through the
storefront; `lcl stop checkout` mid-placement, restart, wait one reconcile interval, confirm convergence; call
the s2s endpoint with a shopper token and confirm 403; repeat as a second store. New cases append to the
existing `SEC` and `ORD` sections plus new `SAG` / `IDM` / `EXP` sections, and **`ORD-02`, `ORD-03` and
`SEC-01` move from `[not verified]` to `[verified]`** — they are precisely the flows this series repairs. Note
`lcl.yml:221-226` does not list inventory/payment/merchant in checkout's `depends-on`, so start order matters.

---

## Risks, sign-offs, and non-goals

**Sign off before PR 3:** COD commit-at-placement is a **behaviour change** — stock leaves availability at order
time rather than at delivery. It is the only fix for C4 that needs no inventory change. The alternative is a
per-payment-type reservation TTL on `reserve` (cross-pod, its own PR).

**Other risks.** Post-commit `CANCELLED` becoming a 409 will surface for merchants who cancel confirmed orders
today (they currently get a silent stock leak) — console copy change, no code change. `open-in-view: false`
(PR 5) is the change most likely to surface something in QA; land it inside PR 5 so blame is unambiguous and
exercise the console order-details page before merging. `alter column date_purchased` rewrites the table — fine
at current scale, note it in the PR body. `AWAITING_REFUND` has no automated exit by design; if it proves
common, the follow-up is an inventory `recommit(ref)`.

**Payment-pod defects found while mapping the flow:**

| Defect | Verdict |
|---|---|
| `rejectPayment` registers no event (C10) | **Fold into PR 4** — `PAYMENT_REJECTED` is unreachable without it. |
| **`toStripeUnitAmount` is `amount.longValue() * 100` — truncates cents** (`StripeProcessor.java:81-83`) | **File separately and ship before PR 1.** Every fractional order is undercharged: €19.99 bills as €19.00. One line (`movePointRight(2).setScale(0, HALF_UP).longValueExact()`) plus a test, payment-owned, unrelated to this refactor. |
| No Stripe idempotency key on `Session.create` | File separately — a retried initiate can create two sessions. Payment's `(request_ref, store)` unique mitigates but does not eliminate. |
| No webhook dedup in payment (billing has `WebhookIngestService.claim(...)`; payment has nothing) | File separately. The state-machine guard makes a redelivered webhook harmless downstream, so this is no longer urgent — which is why it should be its own PR, not smuggled in. |
| Inventory's uniform 45-minute reservation TTL (C4's root cause) | File separately — needs a per-payment-type `expireAt` on `reserve`. Manual transfer stays half-broken (48 h deadline vs 45 min reservation) until then; the reconciler's payment-truth check at least stops it cancelling a *paid* order. Record under `99 — Known gaps`. |
| `WebhookEvent` keyed on `storeId` + `stop-on-first-failure` — one bad webhook blocks a store's queue | File separately. |
| Inventory's `reserve` locks SKUs in `Set` iteration order — concurrent multi-SKU orders can deadlock | File separately. |

**Non-goals.** Shipping/tax/handling/discount totals stay absent (`OrderTotalType` has the values; nothing
computes them; it needs tax rules and shipping rates and is a plan of its own — `OrderTotalCalculator` gets a
javadoc saying so). No v3 API, no response-shape change. No data migration, no dropped or renamed column. No
outbox and no `checkout-events` module in checkout. Multi-currency/FX. `AUTHORIZED`/`REFUNDED`/`PAYPAL` stay
unimplemented — the state machine simply refuses transitions into them. Manual-transfer proof-upload UI.
`customer-core`/`reference-core` keep their facade layout.

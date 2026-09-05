# Checkout rewrite — durable orders, event ledger, repo-shaped modules

Plan file to copy to `.agents/plans/checkout-rewrite.md` in the worktree (`.claude/worktrees/refactor-checkout-rewrite`,
branch `refactor/checkout-rewrite`, stack `checkout-rw`).

## Context

`store-pod/checkout` is the one pod service that never got the 2026 treatment catalog/inventory/content got. Audit findings
(current code, `store-pod/checkout/`):

- **Orders get lost.** `OrderPlacementFacadeImpl.placeOrder` does save → remote reserve → status write → remote payment →
  status write with no recovery; a crash between steps leaves an order nothing reconciles; the `default:` branch leaves
  orders in `CREATED` forever and the expiry sweep only looks at `PENDING_PAYMENT`.
- **The DDL rejects what the code writes.** `orders_payment_status_check` allows 5 of the 10 `PaymentStatus` values;
  `EXPIRED` and `CANCELLED` fail at flush, so expiry and cancel are dead paths.
- **No guards.** No `@Version`, no transition validation, idempotency only by `cart_code` regardless of order state,
  customer lookup by `cua_external_id` not store-scoped and without an index.
- **Security.** `ExternalOrderApi` (payment/inventory callback) has no `@PreAuthorize`: any JWT can flip an order to PAID.
- **Architecture.** Facade + populator + mapper sprawl; controllers inject repositories (ArchUnit rule disabled); customer
  and reference entities live in `store-pod/commons/{customer,reference}` under the checkout package; `checkout-core` ↔
  `inventory-core`/`payment-core` module cycle; S3/MapStruct wired and unused; no `http/`, ~0 tests, coverage floor 0.

Decisions taken with the user (2026-09-05): fresh start (no data migration); placement stays synchronous and returns
`redirectUrl`, made durable step by step with a recovery job; customers absorbed into checkout-core, `customer-core` and
`reference-core` retired, `/country` from the JDK; callback contract with payment/inventory may change, payment must emit
an event on manual-transfer rejection. Old service → `store-pod/checkout-deprecated/`.

Style templates: `store-pod/catalog` (flat `entity/`, `repositories/`, `services/<domain>/{XService, XServiceImpl, XMapper}`)
and `store-pod/inventory` (tiny). Aggregate idiom: `store-pod/payment/payment-core/.../entity/payment/Transaction.java`
(transitions as methods). Error idiom: `payment-commons/.../errors/`. Test idiom: `store-commons/test-support`.

## Contracts that stay byte-compatible (external consumers)

Storefront (`store-pod/landing-ui/libs/services/src/{cart,customer,order}-service.ts`, types `libs/types/src/*.ts`):

| Endpoint | Shape read |
|---|---|
| `POST /api/v1/cart` `{product, quantity}` → `Cart`; `PUT /api/v1/cart/{code}` (absolute qty); `GET /api/v1/cart/{code}`; `DELETE /api/v1/cart/{code}/product/{sku}` → 204 | `Cart{id, code, subtotal, displaySubTotal, total, displayTotal, quantity, products[], totals[], order, ...}`; products read `description`, `sku`, `quantity`, `finalPrice`, `displaySubTotal`, `available`, `image`, `variant` |
| `POST /api/v1/cart/{code}/checkout` `{paymentType, customer{emailAddress, billing, delivery}}` (shopper JWT) | `Order` incl. `redirectUrl`, `orderStatus` — storefront navigates to `redirectUrl` if present |
| `GET /api/v1/order/{orderId}/status` | `{orderId, orderStatus, paymentStatus, redirectUrl}` (12 themes read these) |
| `GET /api/v1/private/customer/info`, `/customer/orders?page&count`, `/customer/{id}/order`, `/customer/{id}/order/history` (`STORE-POD.CUSTOMER.*`) | `Customer`, `OrderPage` (`ReadableList` envelope), `Order`, `OrderHistoryItem[]{id, orderId, comments, date, orderStatus}` |
| `GET /api/v1/country` | `Country[]{id, code, supported, name, zones}` |

Console (`store-core/console-ui/src/app/api/{orders,customers,analytics}/*.service.ts`, models `src/app/models/checkout.ts`):
`GET /private/orders?page&count&name&id&status&phone&email&customerId`, `GET /private/orders/{id}`,
`GET|POST /private/orders/{id}/history` (`{orderStatus, comments}` — the console's only write),
`GET /private/customers?page&count&name&firstName&lastName&email&country`,
`POST /api/v2/private/{order,customer,product}-statistic` (`StatisticRange` → `StatisticList`). All `STORE-POD.CHECKOUT.*`.
Page param is `count`.

Deliberately dropped (no or single in-repo consumer): `/zones`, `/api/v1/auth/{current,me}`, `ReferencesApi`.
Two console renames (`redirectUri`→`redirectUrl`, `reservationStatus`→`inventoryStatus`) instead of emitting both.

## 1. Modules and layout

Same Gradle coordinates as today so the moved directory is implicitly unregistered (the `content-deprecated` pattern):

```
store-pod/checkout/
├── checkout-commons/      api: store-pod:commons:store-commons, customer-commons, reference-commons
│   └── com/asrevo/cvhome/checkout/
│       ├── domain/        OrderRef (UUID, newRef()/of()), CartCode (UUID), ShopperId (cua `sub`)
│       ├── errors/        CheckoutErrors enum + CartNotFoundException(404), CartEmptyException(422),
│       │                  CartAlreadyConvertedException(409), CartQuantityOutOfRangeException(422),
│       │                  ProductNotPurchasableException(422), OrderNotFoundException(404),
│       │                  IllegalOrderTransitionException(409), OrderLoginRequiredException(401),
│       │                  ForeignStoreTokenException(403), PriceNotFormattableException(500)
│       ├── model/cart/    PersistableCartItem, ReadableCart, ReadableCartItem (extends ReadableMinimalProduct), ReadableCartTotal
│       ├── model/order/   PlaceOrderRequest, ReadableOrder, ReadableOrderList, ReadableOrderConfirmation, ReadableOrderStatus,
│       │                  ReadableOrderLine, ReadableOrderLineAttribute, ReadableOrderTotal, ReadableOrderStatusHistory,
│       │                  PersistableOrderStatusHistory, OrderFilter, PendingAction, OrderEventType, OrderEventSource,
│       │                  OrderEventOutcome, CartStatus
│       └── model/signal/  PaymentSignal(status, transactionRef), ReservationExpiredSignal(reservationRef),
│                          SignalOutcome(outcome, orderStatus, paymentStatus)
├── checkout-external-api/ api checkout-commons; compileOnly spring-web
│   └── .../checkout/api/errors/{CheckoutApiErrors, CheckoutApiException, CheckoutApiUnavailableException}
│       .../checkout/services/order/{IOrderSignalService, ExternalOrderSignalService}
├── checkout-core/         api store-commons(pod), checkout-commons; implementation merchant/catalog/inventory/payment -external-api
│   └── .../checkout/
│       ├── config/CheckoutProperties
│       ├── entity/        Cart, CartLine, Customer, Order, OrderLine, OrderLineOption, OrderTotal, OrderStatusHistory,
│       │                  OrderEvent, AddressSnapshot(@Embeddable), converter/{OrderRefConverter, CartCodeConverter}
│       ├── repositories/  CartRepository, CustomerRepository, OrderRepository, OrderSpecifications, OrderStatusHistoryRepository
│       └── services/
│           ├── cart/      CartService, CartServiceImpl, CartMapper
│           ├── catalog/   ProductSnapshotService(+Impl), ProductSnapshot record  (catalog + inventory merge)
│           ├── customer/  CustomerService(+Impl), CustomerMapper
│           ├── money/     MoneyFormatter
│           ├── order/     OrderPlacementService(+Impl), OrderStepRunner, OrderSignalService(+Impl), OrderService(+Impl),
│           │              OrderStatisticsService(+Impl), OrderMapper, RedirectUrls record
│           ├── jobs/      OrderRecoveryJob, OrderExpiryJob
│           ├── reference/ CountryService(+Impl)
│           └── store/     StoreSettings (currency, requireLogin, locale from cached ReadableMerchantStore)
└── checkout-service/      boot app (deps like payment-service minus stripe; no S3, no MapStruct, no springdoc config)
    ├── http/              cart-api.http, checkout-api.http, order-api.http, external-order-signal-api.http,
    │                      customer-api.http, customer-admin-api.http, country-api.http, statistic-api.http
    ├── qa/checkout-qa.md
    └── src/main/java/.../checkout/
        ├── CheckoutApplication
        ├── api/v1/cart/CartApi   api/v1/order/{CheckoutApi, OrderApi, ExternalOrderSignalApi}
        ├── api/v1/customer/{CustomerApi, CustomerAdminApi}   api/v1/reference/CountryApi   api/v2/statistic/StatisticApi
        └── config/  SecurityConfig, ClientsConfig (+CachedExternalMerchantStoreService), CacheConfig, ClockConfig
                     (@EnableScheduling + Clock, copy store-pod/content/content-service/.../config/ClockConfig.java),
                     CheckoutWebConfig, ShopperArgumentResolver, @CurrentShopper
    └── src/main/resources/ application.yml (+ -lcl, -fargate, -test-stores), init-sql/{drop-legacy.sql, schema.sql,
                            data-common.sql, data-test-stores.sql}
```

No outbox in checkout: it produces no cross-service event today (payment/inventory are called synchronously, callbacks are
inbound). Add `namastack` the day the first `registerEvent` is needed.

Retired: `store-pod/commons/customer/customer-core`, `store-pod/commons/reference/reference-core` (only checkout used them;
removed from `settings.gradle`). `customer-commons`/`reference-commons` DTO modules stay (catalog, content, merchant use them).

## 2. Data model — `init-sql/schema.sql`, schema `checkout`

Legacy handling: `init-sql/drop-legacy.sql` listed first in `spring.sql.init.schema-locations`, one
`drop table if exists checkout.<every legacy table> cascade` (orders, order_*, shopping_cart*, customer*, file_history, optin,
country*, zone*, geozone*, language, currency). New tables use names that collide with nothing, so the drop is a no-op after
the first boot and `CREATE TABLE IF NOT EXISTS` never inherits stale columns. `sm_sequencer` kept (ids via `@TableGenerator`
like inventory/payment). Numeric `Long` ids stay on the wire; the opaque `order_ref` UUID is the only id given to
inventory/payment.

Entities extend `SalesManagerEntity`, `@EntityListeners(AuditListener)`, `@Embedded AuditSection`, `StoreMerchantId` embedded
(`STORE_MERCHANT_ID varchar(50)`). Tables:

| Table | Key columns |
|---|---|
| `customer_account` | `cua_external_id`, `email`, names, `billing_*`/`delivery_*` (AddressSnapshot), **unique (store_merchant_id, cua_external_id)**, index (store, email) |
| `cart` | `cart_code` unique, `status` CHECK `ACTIVE, CONVERTED`, `order_id`, `cua_external_id`, `language_code` |
| `cart_line` | `sku`, `quantity > 0`, unique (cart_id, sku) |
| `sales_order` | `version` (`@Version`), `order_ref` unique, `cart_code` + unique (store, cart_code), `customer_id` FK, `customer_email`, `language_code`, `currency_code`, `payment_type` CHECK (4), `order_status` CHECK (all 10 `OrderStatus`), `payment_status` CHECK (all 10 `PaymentStatus`), `inventory_status` CHECK (all 6), `pending_action` CHECK `NONE, RESERVE, INITIATE_PAYMENT, COMMIT, RELEASE`, `pending_action_attempts`, `pending_action_updated_at`, `needs_attention` bool, `attention_reason`, `reservation_expire_at`, `payment_transaction_ref`, `redirect_url varchar(2048)`, `success_url`, `cancel_url`, `expires_at`, `date_purchased`, `subtotal`, `total` numeric(19,4), `billing_*`, `delivery_*` |
| `sales_order_line` | `sku`, `product_id`, `product_name`, `unit_price`, `quantity`, `line_total`, `image_url`, `sort_order` |
| `sales_order_line_option` | `option_name`, `value_name`, `sort_order` (variant label snapshot) |
| `sales_order_total` | `code` CHECK `SUBTOTAL, SHIPPING, TAX, TOTAL`, `module`, `title`, `value`, `sort_order` |
| `sales_order_history` | `status` CHECK (10), `comments`, `actor`, `date_added` |
| `sales_order_event` | **append-only ledger**: `event_type`, `source` CHECK `PLACEMENT, PAYMENT, INVENTORY, CONSOLE, JOB, SYSTEM`, `source_ref`, `outcome` CHECK `APPLIED, DUPLICATE, IGNORED`, `*_status_after`, `pending_action_after`, `payload`, `reason`, `occurred_at`; **unique (order_id, source, source_ref) where source_ref is not null** |

Indexes: `(store, date_purchased desc)`, `(store, customer_id)`, `(store, order_status)`, partial
`(pending_action_updated_at) where pending_action <> 'NONE'`, partial `(expires_at) where expires_at is not null`,
partial `(store) where needs_attention`, `(order_id, occurred_at)` on events.

Dedup key convention: payment signal → (`PAYMENT`, `"<transactionRef>:<status>"`); reservation expired → (`INVENTORY`,
`"<reservationRef>:EXPIRED"`); placement/job/console rows have `source_ref = null`.

An integration test inserts every CHECK-listed enum value via `JdbcTemplate` so the enum/DDL drift that killed the old
service cannot recur silently.

## 3. State machine — methods on `Order` (checkout-core `entity/Order.java`)

Every method validates preconditions, mutates the three statuses + `pendingAction` (+ `pendingActionUpdatedAt = now`,
attempts reset), appends one `OrderEvent`, and where marked (H) an `OrderStatusHistory`. Illegal →
`IllegalOrderTransitionException` (409). The aggregate takes `Instant now`; no clock inside.

```java
static Order place(PlacementDraft d, Instant now)                          // CREATED / PENDING / NOT_REQUESTED, pending RESERVE (H)
void reserved(Long reservationId, Instant reservationExpireAt, Instant expiresAt, Instant now) // RESERVED, pending INITIATE_PAYMENT
void reservationRefused(String sku, Instant now)                           // RESERVATION_FAILED, CANCELLED, payment CANCELLED, NONE (H)
void paymentInitiated(PaymentInitiateResult r, Instant now)                // table below
SignalOutcome applyPaymentSignal(PaymentStatus s, String txRef, Instant now)     // dedup + dispatch, never throws
SignalOutcome applyReservationExpired(String reservationRef, Instant now)
void committed(Instant now)  void commitRefused(Instant now)  void released(Instant now)
void expired(Instant now)                                                  // payment EXPIRED, CANCELLED, pending RELEASE (H)
void cancel(String comment, String actor, Instant now)  void fulfil(OrderStatus next, String comment, String actor, Instant now) // console (H)
void recoveryAttempted(Instant now)  void recoveryGaveUp(Instant now)     // attempts++ / needs_attention
```

Terminal: order `COMPLETED, CANCELLED, RETURNED`; inventory `COMMITTED, RELEASED, RESERVATION_FAILED`; payment
`PAID (→ REFUNDED only), FAILED, EXPIRED, CANCELLED, REJECTED, REFUNDED`.

`paymentInitiated` (from pending `INITIATE_PAYMENT`):

| `PaymentInitiateStatus` | payment | order | pending |
|---|---|---|---|
| `PENDING`, STRIPE/PAYPAL/MANUAL_TRANSFER | PENDING | PENDING_PAYMENT (H) | NONE; `redirectUrl` stored; `expiresAt` from `reserved()` |
| `PENDING`, COD | PENDING | CONFIRMED (H) | COMMIT |
| `PAID` | PAID | CONFIRMED (H) | COMMIT; `expiresAt = null` |
| `FAILED` | FAILED | CANCELLED (H) | RELEASE |

**COD decision:** confirm on placement and commit inventory immediately. Inventory decrements stock at `reserve`; the
reservation is only a timer, and the only thing that timer can do to a COD order is cancel a valid one. Cash collection is
recorded by the console: `fulfil(DELIVERED)` on a COD order also sets payment `PAID`. MANUAL_TRANSFER stays reserved (the
merchant may reject the proof).

`applyPaymentSignal` — first dedup on (`PAYMENT`, `txRef:status`) → `DUPLICATE`, no change. Then:

| status | from PENDING_PAYMENT / CREATED | from CONFIRMED+ | from CANCELLED |
|---|---|---|---|
| PAID | PAID, CONFIRMED (H), pending COMMIT if RESERVED, `expiresAt=null` | IGNORED | PAID recorded, order stays CANCELLED, `needs_attention("paid after cancellation — refund required")` (H) |
| FAILED / CANCELLED / REJECTED / EXPIRED | payment ← status, CANCELLED (H), pending RELEASE if RESERVED | IGNORED | IGNORED |
| PROCESSING / AUTHORIZED | payment ← status; `expiresAt = now + processing-grace` | IGNORED | IGNORED |
| WAITING_VERIFICATION | payment ← status; `expiresAt = null` (never auto-expire while a merchant verifies) | IGNORED | IGNORED |
| REFUNDED | IGNORED | REFUNDED; order → RETURNED if DELIVERED/COMPLETED else CANCELLED (H); RELEASE if still RESERVED else `needs_attention("refunded after stock committed")` | IGNORED |
| PENDING | IGNORED | IGNORED | IGNORED |

`applyReservationExpired`: dedup; from PENDING_PAYMENT/CREATED → expired with inventory `RELEASED`, pending NONE (inventory
already released); from CONFIRMED with pending COMMIT → inventory RELEASED + `needs_attention("stock released before commit")`;
else IGNORED.

Console: `fulfil` allows `CONFIRMED→PROCESSING→SHIPPED→DELIVERING→DELIVERED→COMPLETED`, `DELIVERED|COMPLETED→RETURNED`;
`cancel` from `CREATED|PENDING_PAYMENT|CONFIRMED|PROCESSING` (pending RELEASE if RESERVED; if PAID → keep PAID +
`needs_attention("cancelled after payment — refund required")`). Anything else → 409.

## 4. Durable placement, recovery, expiry

### `OrderPlacementService.place(store, lang, cartCode, PlaceOrderRequest, ShopperId nullable, RedirectUrls)`

1. **tx1** `createOrder`: load cart by (store, code). `CONVERTED` + open order → resume at step 2 (idempotent resubmit);
   `CONVERTED` + terminal order → `CartAlreadyConvertedException` 409. Login rule from cached
   `ReadableMerchantStore.requireLoginForOrderPlacement` → `OrderLoginRequiredException` 401; token realm ≠ store →
   `ForeignStoreTokenException` 403. `CustomerService.getOrCreate(store, shopper, request.customer())` (guest checkout:
   key `guest:<lowercased email>` so repeat guests collapse into one row). `ProductSnapshotService.snapshot(store, lang, skus)`
   = `ExternalProductService.getDetailedProducts` + `ExternalInventoryService.queryBySkus` merged; missing in either →
   `ProductNotPurchasableException`; qty outside `[min, max]` → `CartQuantityOutOfRangeException`. `Order.place(...)` with
   lines, totals (SUBTOTAL, TOTAL), success/cancel URLs with `?orderId=` appended. Cart → `CONVERTED` + `orderId`. Commit.
2. **`OrderStepRunner.runUntilSettled(orderId, maxSteps)`** — remote call *outside* any tx, apply in its own
   `@Transactional` under `@Version`:
   - `RESERVE`: `reserve(store, orderRef, ProductReservationList(entries, expireAt))` → `reserved(...)` with
     `expiresAt = min(result.expireAt, now + checkout.placement.expiry.<type>)`, null for COD.
     `ProductReservationRejectedException` → tx `reservationRefused(sku)` + cart back to `ACTIVE`, rethrow (422).
     `InventoryApiUnavailableException` → leave pending, rethrow (502): the order is durable, a resubmit or the recovery job
     resumes it.
   - `INITIATE_PAYMENT`: `initiatePayment(store, PaymentRequest{ref=orderRef, amount=total, currency, paymentType,
     expireAt (COD: now+30d), successUrl, cancelUrl})` → `paymentInitiated(result)`. `PaymentGatewayRejectedException` →
     pending RELEASE, run it, rethrow 422. `PaymentApiUnavailableException` → leave pending, rethrow 502. Payment is
     idempotent by (store, ref), so a retry returns the stored result.
   - `COMMIT`: `commit(store, ref)` → `status=true` → `committed()`, `false` → `commitRefused()`.
     `RELEASE`: `release(store, ref)` → `released()`.
   - `ObjectOptimisticLockingFailureException` = another replica/job applied the same idempotent step → re-read, continue.
3. Return `ReadableOrderConfirmation` (`redirectUrl` only when payment gave one).

Storefront behaviour is unchanged: `redirectUrl` → navigate; else success dialog unless `CANCELLED`; a 502 shows the retry
message with the cart still readable (see §5) and a resubmit resumes.

### Reservation TTL (one small inventory change)

Inventory hardcodes `${reservation.expiry.minutes:45}` and its `ReservationExpiryJob` releases every stale reservation —
which is what auto-cancels a 48h manual transfer. Add an optional `Instant expireAt` to the shared record
`store-pod/commons/store-commons/.../store/core/model/catalog/ProductReservationList` (keep the 1-arg constructor);
`inventory-core/.../services/ReservationServiceImpl.reserve` honours it, capped by `reservation.expiry.max-hours` (default 72).

### `CheckoutProperties` (`@ConfigurationProperties("checkout")`)

```yaml
checkout:
  placement:
    expiry: { stripe: 30m, paypal: 30m, manual-transfer: 48h, processing-grace: 24h }
  recovery: { interval: 30s, stale-after: 60s, batch-size: 50, max-attempts: 10 }
  expiry:   { interval: 60s, batch-size: 50 }
```

### `OrderRecoveryJob` (`services/jobs/`)

`@Scheduled(fixedDelayString="${checkout.recovery.interval:30s}")`, store-agnostic query
`pending_action <> 'NONE' and pending_action_updated_at < :staleBefore order by pending_action_updated_at limit :batch`.
Per id: small tx `recoveryAttempted()` (the claim: bumps `attempts` and the timestamp; a second replica loses on `@Version`
and skips) → `stepRunner.runUntilSettled(id, 1)`. `attempts >= max-attempts` → `recoveryGaveUp()` (flag, stop retrying).
One loop covers: crash after tx1, crash after reserve, PAID-but-commit-unavailable, cancel-but-release-unavailable.
Multi-replica safety = `@Version` on the claim + idempotent remotes by `order_ref`; no lock library, no remote I/O under a
DB lock.

### `OrderExpiryJob`

`@Scheduled(fixedDelayString="${checkout.expiry.interval:60s}")`, query `order_status='PENDING_PAYMENT' and expires_at < now`.
STRIPE/PAYPAL: call `ExternalPaymentGatewayService.status(store, orderRef)` once — `PAID` → `applyPaymentSignal(PAID,
gatewayRef)` (a late webhook never gets cancelled); `PaymentApiUnavailableException` → skip this pass. Otherwise tx
`expired(now)` → pending RELEASE → runner. MANUAL_TRANSFER: no status call.

## 5. Inbound signals — `checkout-external-api`

```java
// IOrderSignalService (server vocabulary, implemented by ExternalOrderSignalApi)
SignalOutcome signalPayment(StoreMerchantId store, @PathVariable("orderRef") String orderRef, @RequestBody PaymentSignal signal) throws OrderNotFoundException;
SignalOutcome signalReservationExpired(StoreMerchantId store, @PathVariable("orderRef") String orderRef, @RequestBody ReservationExpiredSignal signal) throws OrderNotFoundException;

// ExternalOrderSignalService — @HttpExchange("/api/v1/private/orders"), caller vocabulary
@PostExchange("/{orderRef}/signals/payment")              ... throws CheckoutApiUnavailableException;
@PostExchange("/{orderRef}/signals/reservation-expired")  ... throws CheckoutApiUnavailableException;
```

`CheckoutApiErrors.CATALOG = RemoteErrorCatalog.builder().unreachable(CheckoutApiUnavailableException::from).build()`.
Controller `@PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.CHECKOUT.SIGNAL')")` on both. Illegal-in-this-
state is an `IGNORED` event and 200 (a 4xx would make payment's outbox retry a decision that never changes). Unknown
`(store, orderRef)` → 404: refs are UUIDs checkout minted, so an unknown one is a bug and should surface in payment's
`outbox_record.status='FAILED'`, the documented debugging path. Reflection contract test asserts client paths == controller
paths (mirrors inventory's `ReservationClientContractIntegrationTest`).

Permission: `store-commons/autoconfigure/.../s2s/config/internal/CustomPermissionEvaluator.java` — constant
`CHECKOUT_SIGNAL = "STORE-POD.CHECKOUT.SIGNAL"` in the `isSameStorePod` branch next to `INVENTORY_RESERVE`
(`PermissionAccessChecker.isSameStorePod` already exists); add to the `@ValueSource` in `CustomPermissionEvaluatorDispatchTest`.

Callers:
- `payment-service/.../service/PaymentOutboxHandler` → `signalPayment(store, event.requestRef(), new PaymentSignal(status,
  event.internalRef()))`; new `handlePaymentRejectedEvent`. `payment-service/.../config/ClientsConfig` builds
  `ExternalOrderSignalService` with `CheckoutApiErrors.CATALOG`.
- `payment-commons/.../model/payment/event/payment/PaymentRejectedEvent` (copy of `PaymentPaidEvent`);
  `Transaction.rejected()` registers it; `TransactionServiceImpl.rejectPayment` calls it instead of setting the status.
- `inventory-core/.../services/ReservationExpiryJob` → `signalReservationExpired(store, ref, new ReservationExpiredSignal(ref))`,
  keep catch-and-log. `inventory-service/.../config/ClientsConfig` likewise.
- Tests to update: `payment-service/src/integrationTest/.../config/ExternalClientsTestConfiguration`,
  `payment-service/src/test/.../service/OutboxHandlersTest` (+rejected case), `inventory-service/src/integrationTest/.../config/
  ExternalClientsTestConfiguration`, `inventory-core/src/test/.../services/ReservationExpiryJobTest`, `ReservationServiceImplTest`.

## 6. Cart

`CartService`: `create(store, lang, item, shopper)`, `upsert(store, lang, code, item)` (absolute qty; 0 removes), `get`,
`removeLine`. Priced on every read from `ProductSnapshotService` (no product cache: price/stock must be live). Lines whose sku
vanished from catalog or inventory are pruned on read; `canBePurchased=false` lines stay with `available:false`.
Min/max enforced on write. A `CONVERTED` cart: `GET` returns it read-only with `order: <id>` while the order is open;
`PUT`/`DELETE` → 409; once the order is terminal every verb → 404 (the storefront's `CartManager` already clears
localStorage on 404 and starts a new cart). `MoneyFormatter` (`NumberFormat.getCurrencyInstance` for the store currency +
request locale) feeds the `display*` strings for cart and orders.

## 7. Customers

`Customer` in checkout-core; `CustomerService`: `getOrCreate(store, shopper, PersistableCustomer)` (upserts email/names/
addresses), `find`, `info` (404 typed, not an empty 200), `list(store, CustomerFilter, Pageable)`. DTOs reused from
`customer-commons` (`ReadableCustomer`, `PersistableCustomer`, `CustomerAddress`). Country codes validated against
`Locale.getISOCountries()` → `UnsupportedCountryCodeException` (customer-commons).

`@CurrentShopper` + `ShopperArgumentResolver` live in `checkout-service/config/` (only checkout reads a shopper today;
promote to autoconfigure when a second service needs it): returns `ShopperId(sub)` for a `JwtAuthenticationToken`, else null.
Registered by `CheckoutWebConfig implements WebMvcConfigurer`. Tenant gate stays `STORE-POD.CUSTOMER.*` (realm == store).

Shopper endpoints (`CustomerApi`): `/private/customer/info`, `/private/customer/orders`, `/private/customer/{id}/order`,
`/private/customer/{id}/order/history` — scoped by (store, shopper); another shopper's order → 404.
Console (`CustomerAdminApi`, `STORE-POD.CHECKOUT.*`): `GET /private/customers` with the five filters.

## 8. Read APIs, statistics, country

- `CheckoutApi`: `POST /cart/{code}/checkout`, `GET /order/{orderId}/status` (owned by the shopper when the store requires
  login; anonymous status read otherwise).
- `OrderApi` (`STORE-POD.CHECKOUT.*`): list via `OrderSpecifications` (store always applied) with `name, id, status, phone,
  email, customerId`; detail; history GET; history POST → `OrderService.transition(store, id, status, comments, actor)` →
  `fulfil`/`cancel`, then the runner inline (release) with the job as fallback.
- `OrderMapper`: `ReadableOrder{id, orderRef, orderStatus, paymentStatus, inventoryStatus, paymentType, currency,
  datePurchased, total, totals[], products[]{sku, productName, orderedQuantity, price (formatted), subTotal (formatted),
  image, attributes[]}, customer, billing, delivery, redirectUrl, needsAttention, attentionReason}`;
  `ReadableOrderConfirmation` = storefront `Order`; `ReadableOrderStatus{orderId, orderStatus, paymentStatus, redirectUrl}`.
- `StatisticApi` (`/api/v2/private/{order,customer,product}-statistic`) → `OrderStatisticsService` (no repository in
  controllers): orders per day per status; distinct customers per billing country; units per sku. Shapes unchanged.
- `CountryApi` `GET /country` → `CountryService`: `Locale.getISOCountries()` → `ReadableCountry{id = 1-based index, code,
  supported=true, name = Locale.of("", code).getDisplayCountry(locale), zones=[]}`, sorted by name, memoised per language.

## 9. Cross-repo edits

| File | Change |
|---|---|
| `settings.gradle` | remove `store-pod:commons:reference:reference-core`, `store-pod:commons:customer:customer-core` |
| root `build.gradle` `ext.domainCoverageMinimum` | `checkout: [unit: 0.80, integration: 0.50, merged: 0.85]` in P0; ratchet to measured in P2 |
| `CustomPermissionEvaluator` + `CustomPermissionEvaluatorDispatchTest` | `STORE-POD.CHECKOUT.SIGNAL` |
| `store-commons/test-support/.../security/Tokens.java` | `shopper(String store, String sub)`: claims `sub`, `roles:["ROLE_CUSTOMER"]`, `realm: store`, `exp` (what `StoreRoleAccessChecker.isStoreCustomer` reads; the test decoder attaches no `REALM_` authority so `isForeignRealm` is false) |
| `ProductReservationList` (pod store-commons), inventory `ReservationServiceImpl`, `application.yml` (`reservation.expiry.max-hours`) | optional `expireAt` |
| payment: `PaymentRejectedEvent`, `Transaction.rejected()`, `TransactionServiceImpl.rejectPayment`, `PaymentOutboxHandler`, `ClientsConfig`, tests | §5 |
| inventory: `ReservationExpiryJob`, `ClientsConfig`, tests | §5 |
| `store-core/console-ui/src/app/models/checkout.ts` + usages; `api/orders/orders.service.ts` | `redirectUri`→`redirectUrl`, `reservationStatus`→`inventoryStatus`; delete `zones()`/`ReadableZone`; `npm run build && npm run lint` |
| `store-pod/landing-ui` | none expected; verify with the stack |
| `store-pod/checkout-deprecated/` | `git mv store-pod/checkout/*` keeping layout minus `checkout-service/{missed,products,requests,store}.http`, `compose.yml`, `*/HELP.md`, `*/bin`; `README.md` (what it was, why, old class → new class table, "unregistered, do not depend on", removal condition for `drop-legacy.sql`); `docs/` ← `git mv store-pod/{checkout-flow,order-placement-flow,align-checkout-inventory-payment-flow}.md` |
| `.claude/skills/project-structure/SKILL.md`, `references/store-pod.md`, `references/testing.md`, `references/events-outbox.md`, `references/error-handling.md` | checkout row/section rewritten; `checkout-deprecated` noted; remove the two checkout ArchUnit deviations and the `LEGACY.*` count; `PaymentRejectedEvent` listed |
| `checkout-service/http/*.http` (8 files) | one block per endpoint + 403/404/409/422 negatives; seller via `{{SELLER_UI_URL}}/spg/checkout/...` + gateway session cookie; shopper/public via `{{SPG_URL}}/checkout/...?store={{STORE_ID}}&lang={{LANG}}`; add `SHOPPER_TOKEN`, `S2S_TOKEN` to `http-client.private.env.json.example` |
| `checkout-service/qa/checkout-qa.md` | rewritten on `store-core/billing/billing-service/qa/billing-qa.md` structure: `00` prerequisites + `psql` idioms on `sales_order`/`sales_order_event`; sections `CART`, `CUS`, `PLC`, `SIG`, `JOB`, `ORD`, `STA`, `SEC`, `REG`, `99` |
| `lcl.yml`, `Caddyfile`, `common-config.yml`, `lcl-config.yml`, `fargate-config.yml`, `docker-compose-lcl.yml` | unchanged (same name/port/schema) |

## 10. Tests

- **checkout-commons**: `CheckoutErrorsTest`, `OrderRefTest`, `CartCodeTest`.
- **checkout-core** (bulk): `OrderTransitionTest` (`@ParameterizedTest` tables for `paymentInitiated` × types,
  `applyPaymentSignal` full matrix of §3, `applyReservationExpired`, `fulfil`/`cancel` legal/illegal, dedup → second call
  `DUPLICATE` with statuses unchanged, exactly one event per applied transition, history only where (H), COD delivered →
  PAID, terminal states only IGNORE); `OrderPlacementServiceImplTest` (every branch of §4 with Mockito clients);
  `OrderStepRunnerTest` (each action, optimistic-lock swallow); `OrderSignalServiceImplTest`; `OrderRecoveryJobTest` and
  `OrderExpiryJobTest` with `MutableClock`; `CartServiceImplTest`; `ProductSnapshotServiceImplTest`; `CustomerServiceImplTest`;
  `OrderMapperTest` (exact field names, formatted strings); `MoneyFormatterTest`; `CountryServiceImplTest`;
  `OrderStatisticsServiceImplTest`.
- **checkout-external-api**: `CheckoutApiErrorsTest`.
- **checkout-service/src/test**: `CheckoutArchitectureTest` with all five `CvhomeArchitectureRules` and no deviation;
  `ShopperArgumentResolverTest`; `ExternalOrderSignalServiceContractTest`.
- **checkout-service/src/integrationTest** (`@ServiceIntegrationTest`, `@Import(ExternalClientsTestConfiguration,
  TestClockConfiguration)` with `@Primary` mocks of the five external clients): `CheckoutContextIntegrationTest` (every CHECK
  enum value insertable); `CartApiIntegrationTest` (+ cross-store 404); `CheckoutApiIntegrationTest` (placement per payment
  type asserting `sales_order`, lines, totals, history, events, cart CONVERTED; inventory down → 502 → resubmit resumes;
  reservation refused → 422 + cart ACTIVE; login required → 401); `ExternalOrderSignalApiIntegrationTest` (s2s PAID →
  CONFIRMED + commit; duplicate → `DUPLICATE`; out-of-order → `IGNORED`; shopper/staff token → 403; unknown ref → 404;
  STORE_2 s2s → 404); `OrderApiIntegrationTest` (filters, JSON field names, history 409, STORE_2 isolation, no token 403);
  `CustomerApiIntegrationTest` (`Tokens.shopper`; other shopper's order 404; staff 403; admin list isolation);
  `StatisticApiIntegrationTest`; `CountryApiIntegrationTest`; `OrderJobsIntegrationTest` (clock advance → expiry;
  recovery finishes pending COMMIT once the mock recovers).

## 11. Phasing — three PRs on one worktree, first two stacked

Once `store-pod/checkout` moves, payment and inventory must compile against the new `checkout-external-api`, and order
placement is offline until orders land. So P0 and P1 are stacked PRs merged back-to-back; P2 follows.

**P0 — `refactor(checkout): new skeleton, cart, customers, signal contract; retire legacy service`**
deprecate + README + docs move; `settings.gradle`; four modules; DDL + `drop-legacy.sql`; Cart*, Customer*,
ProductSnapshotService, MoneyFormatter, CountryService; `CartApi`, `CustomerApi.info`, `CustomerAdminApi`, `CountryApi`;
resolver; signal contract + controller (404 until orders exist) + `STORE-POD.CHECKOUT.SIGNAL`; payment/inventory callers and
tests switched; `PaymentRejectedEvent`; `ProductReservationList.expireAt`; `Tokens.shopper`; ArchUnit; coverage floors;
http files for the four APIs; QA skeleton (`00`, `CART`, `CUS`, `SEC`).
Gates: `./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest`, `build -x test -x check`, `test`,
`integrationTest` (checkout, payment, inventory, autoconfigure), `lcl start -d --stack checkout-rw`; browser on
`org1-store1.spg-507f1f77.gateway.com`: add/update/remove cart lines, `/country`; console Customers page at
`gateway.com:8000`; second store isolation via `.http`.

**P1 — `feat(checkout): durable order placement with event ledger, recovery and expiry`**
`Order` aggregate + children, placement/runner/signal/order/statistics services, both jobs, `CheckoutProperties`,
`CheckoutApi`, `OrderApi`, customer order endpoints, `StatisticApi`; console model renames; all order tests; http files;
QA `PLC`, `SIG`, `JOB`, `ORD`, `STA`.
Gates: as P0 plus browser end to end per payment type (COD → success dialog + CONFIRMED; MANUAL_TRANSFER → pending, console
approve/reject in payment's screen → CONFIRMED/CANCELLED; STRIPE test card → CONFIRMED, `sales_order_event` shows the PAYMENT
row); duplicate signal via `.http` → `DUPLICATE`; `lcl stop inventory --stack checkout-rw` → checkout 502 → resubmit resumes
(or recovery job does); console list/detail/history transitions incl. a 409; second store sees nothing; every private
endpoint 403 without a session.

**P2 — `chore(checkout): coverage ratchet, docs, QA verification`**
floors ratcheted to measured (`./gradlew perServiceCoverage`); skill/reference edits; QA tags flipped to `[verified]`;
`REG`/`99`; optional console `needs_attention` filter.
Gates: full `./gradlew check`; console-ui `npm run build && npm run lint`; re-run QA `SEC` and `SIG`.

## 12. Risks / open points

- Placement is offline on `main` between P0 and P1 merges — merge them the same day.
- Guest customers keyed `guest:<email>`: a typo creates a new row. Acceptable; documented in QA.
- `GET /order/{orderId}/status` on stores without login stays an anonymous read by numeric id; full fix needs the storefront
  to pass `orderRef` — deferred, noted in QA `99`.
- Paid-after-cancel / refunds have no automation (payment has no refund API); `needs_attention` + ledger make them visible.
- `ProductReservationList` shape change touches a shared record; check inventory JSON fixtures.
- Statistics semantics change (distinct customers, units per sku); update the two doc comments in
  `console-ui/src/app/api/analytics/statistic.service.ts`.
- Payment `initiate` idempotency by (store, ref) is relied on for recovery; add one payment-service integration assertion if
  none exists.

## Outcome (2026-09-05)

Shipped as **one PR** rather than the three stacked ones above: once the old modules moved, payment and inventory had
to compile against the new signal contract, and placement was offline until the order aggregate landed — splitting
would have left `main` without order placement between merges for no reviewer benefit. Everything in P0–P2 is in
the PR: coverage measured at unit 97.6 % / integration 86.2 % / merged 99.4 % and the floors ratcheted to
`[0.97, 0.86, 0.99]`; the QA file records what was driven through the stack (`lcl start -d --stack checkout-rw`)
and what still needs a signed-in console or shopper session.

Two design corrections found by the integration suite and folded in: only an `APPLIED` ledger row carries the dedup
key (a `DUPLICATE` row keeps the key in its payload, or the unique index would have refused the second row), and
`(store, cart_code)` is an index, not a unique constraint, because a cart handed back after a refused reservation
must be able to become a second order — the cart's own `@Version` is what stops two concurrent checkouts of one cart.

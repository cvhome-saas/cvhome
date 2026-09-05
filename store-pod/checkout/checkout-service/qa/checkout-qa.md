# QA — checkout (`store-pod/checkout/checkout-service`)

Checkout owns the shopper's **cart**, turns it into an **order**, and keeps that order tracked to a final state:
every status change is a method on one aggregate, every inbound signal from payment or inventory lands in an
append-only ledger, and two jobs finish what a request could not. It also owns the store's **customers** and the
console's order statistics.

- **Scope** — `CartApi` (public), `CheckoutApi` (placement + the payment-return status read), `OrderApi` /
  `CustomerAdminApi` / `StatisticApi` (console), `CustomerApi` (shopper), `ExternalOrderSignalApi` (payment and
  inventory calling in), `CountryApi`, the recovery and expiry jobs
- **Runs on** — `lcl start -d --stack <name>`; read the live ports from `lcl urls`. Address it through the pod
  gateway (`http://spg-507f1f77.gateway.com/checkout/…`) or the platform gateway (`gateway.com:8000/spg/checkout/…`),
  never `:8123`
- **Cases** — 48 (29 verified end to end or in part, 10 unit only, 9 not verified — the console screens, which need a seller login the automated QA run could not perform)
- **Also see** — [payment](../../payment/payment-service/qa/payment-qa.md) (the transactions and the approve /
  reject that drive the signals), [inventory](../../inventory/inventory-service/qa/inventory-qa.md) (the
  reservation that placement takes and expiry releases), [landing-ui](../../landing-ui/qa/landing-ui-qa.md) (the
  storefront screens), [console-ui](../../../store-core/console-ui/qa/console-ui-qa.md) (the orders and customers
  screens)
- **Runnable requests** — `../http/*.http`, one file per `*Api` class; the shopper and s2s bearers they need are
  described in `checkout-api.http` and `external-order-signal-api.http`

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — the branch is covered by the named unit or integration test, but nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins, the seeded ids and the `psql` idiom are in
[`references/qa-testing.md`](../../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5.
Everything below is specific to checkout.

**Nothing is seeded.** `init-sql/data-test-stores.sql` primes the id generators only; every cart, customer and
order in these cases is made by the case itself. That is deliberate — a placement that works from an empty schema
is the thing being proved.

**The first boot drops the old schema.** `init-sql/drop-legacy.sql` removes the pre-rewrite `orders`,
`shopping_cart`, `customer`, `country` … tables once; on a database that ran the old service you will see them go.
It is a no-op afterwards.

**Store 1 requires a signed-in shopper**; sign in on the storefront as `user` / `revo`. For the `.http` files copy the
access token from the storefront's `sessionStorage` (`auth-tokens`) into `SHOPPER_TOKEN`.

### Looking at the truth underneath

```bash
# the order and what it still owes
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select order_id, order_ref, order_status, payment_status, inventory_status, pending_action,
          pending_action_attempts, needs_attention, attention_reason, expires_at
     from checkout.sales_order order by order_id desc limit 10;"

# the ledger — every transition and every signal, including the ones that changed nothing
... "select event_type, source, source_ref, outcome, order_status_after, payment_status_after,
            inventory_status_after, pending_action_after, reason, occurred_at
       from checkout.sales_order_event where order_id = <id> order by occurred_at, event_id;"

# the trail the console and the shopper see
... "select status, comments, actor, date_added from checkout.sales_order_history where order_id = <id> order by date_added;"

# the cart's fate
... "select cart_code, status, order_id from checkout.cart order by cart_id desc limit 5;"
```

Logs: `.lcl/<stack>/logs/checkout.log`. The two jobs log at INFO when they act (`Order N was paid after all`,
`recovery attempt failed, will retry`) and at ERROR when recovery gives up.

Timings that matter for QA, all in `application.yml` under `checkout.*` and overridable per stack through
`SPRING_APPLICATION_JSON`: card orders expire after **30 min**, manual transfers after **48 h**, COD never; the
recovery job runs every **30 s** over actions untouched for **60 s**; the expiry job every **60 s**.

---

## CART — The cart

Public and keyed by the code the browser holds (`localStorage` `seller-ui-cart-data`). Prices are never stored:
every read re-prices from inventory, and a line the catalog or inventory no longer knows is dropped on read.

### CART-01 — Add, change, remove, read · critical · [verified via API — browser drawer not verified]

- **Steps** — on the storefront add a product, open the cart drawer, raise the quantity, add a second product,
  remove the first.
- **Expect** — the drawer follows every step; `subtotal`/`total` and the `displaySubTotal` strings agree with the
  product prices; `checkout.cart_line` holds sku + quantity only.

### CART-02 — A quantity outside the sku's bounds is refused with the bounds · high · [verified]

- **Steps** — `SKU-NK-RUN-001` is capped at 1 per order; ask for 2 (`cart-api.http`, "above the maximum").
- **Expect** — `422 CHECKOUT.CART.QUANTITY_OUT_OF_RANGE` with `params.minimum` / `params.maximum`; the storefront
  renders the message from the params, not a generic failure.

### CART-03 — An unknown or unpurchasable sku · high · [verified]

- **Expect** — `422 CHECKOUT.CART.PRODUCT_NOT_PURCHASABLE`, `params.sku` naming it.

### CART-04 — Another store cannot read the cart · critical · [verified]

- **Steps** — `GET /cart/{code}?store=<store 2>`.
- **Expect** — `404 CHECKOUT.CART.NOT_FOUND`. Never the cart.

### CART-05 — A converted cart is read-only while its order is open, and gone once it closes · high · [verified]

`CartServiceImplTest.aConvertedCartWithAnOpenOrderIsReadOnly`, `…WhoseOrderClosedIsSpent`;
`CheckoutApiIntegrationTest.aClosedOrdersCartIsSpent`.

- **Expect** — `GET` answers the cart with `order: <id>` while the order is open; `PUT`/`DELETE` answer
  `409 CHECKOUT.CART.ALREADY_CONVERTED`; once the order is CANCELLED or COMPLETED every verb answers 404 and the
  storefront starts a new cart on the next add.

---

## CUS — Customers

One row per (store, cua account), created on the first order and refreshed from every later checkout body. A guest
checkout keys the row on the lowercased email (`guest:<email>`), so a repeat guest is one customer.

### CUS-01 — The first order creates the customer, the second refreshes it · high · [verified for a guest — signed-in refresh not verified]

- **Steps** — place two orders as `user`, changing the billing city between them; open **Customers** in the console.
- **Expect** — one row, the newer city, `cua_external_id` = the shopper's `sub` (an account id, not `user`).

### CUS-02 — The shopper's profile and order list are theirs alone · critical · [verified in the browser for the own path — the foreign-id 404 is unit only]

- **Steps** — as `user`, open the storefront account page; then call `GET /private/customer/{id}/order` with
  another shopper's order id (`customer-api.http`).
- **Expect** — the page lists only this shopper's orders; the foreign id answers **404**, never 403. A shopper who
  has never ordered gets an **empty profile (200)**, not a 404 — the account page renders it as dashes.

### CUS-03 — A seller cannot use the shopper endpoints and a shopper cannot use the console's · critical · [not verified]

- **Expect** — `/private/customer/info` with the console session → 403; `/private/customers` with a shopper
  token → 403.

### CUS-04 — The console's customer list is one store's · critical · [not verified]

- **Steps** — switch the console to store 2.
- **Expect** — store 1's customers are absent; the filters (`email`, `country`, `name`) narrow within the store.

### CUS-05 — An unknown country code is refused before anything is written · [unit only]

`CustomerServiceImplTest.anUnknownCountryIsRefusedBeforeAnythingIsWritten`.

- **Expect** — `400 CUSTOMER.COUNTRY.UNSUPPORTED`, no customer row, no order.

---

## PLC — Placement

The order row is committed **before** the first remote call. Then, one step at a time and each in its own
transaction: reserve stock → initiate payment → (COD or already paid) commit stock. A refusal closes the order; an
outage leaves the step owed, and both a resubmit of the same cart and the recovery job pick it up. COD confirms at
placement and commits at once — the reservation timer could only ever cancel a valid order.

### PLC-01 — Cash on delivery · critical · [verified]

- **Steps** — cart → checkout → COD.
- **Expect** — success dialog; `sales_order` is `CONFIRMED / PENDING / COMMITTED / NONE`; ledger reads
  `PLACED, RESERVED, PAYMENT_INITIATED, COMMITTED`; the cart is `CONVERTED`; inventory's stock dropped; a
  `payment.transaction` exists for the order ref.

### PLC-02 — Manual bank transfer · critical · [verified via API]

- **Expect** — success dialog, no redirect; `PENDING_PAYMENT / PENDING / RESERVED / NONE`, `expires_at` 48 h out;
  the transaction waits in the console's Payments screen for approval (PAY-01 in payment's QA).

### PLC-03 — Card (Stripe) · critical · [verified up to the Stripe redirect — the paid return not verified]

- **Setup** — the store's Stripe configuration enabled, the `stripe-org1-store1-webhook` listener running.
- **Steps** — checkout with STRIPE; pay with `4242 4242 4242 4242` on the Stripe page.
- **Expect** — the storefront navigates to `redirectUrl`; the order sits at `PENDING_PAYMENT` with `expires_at`
  30 min out until the webhook lands; then `CONFIRMED / PAID / COMMITTED` and a `PAYMENT_SIGNAL` row whose
  `source_ref` is `<transaction internal ref>:PAID`; the return page shows the paid state.

### PLC-04 — A refused reservation cancels the order and hands the cart back · critical · [unit only]

- **Setup** — a sku with 1 in stock; two shoppers with it in their carts.
- **Steps** — both check out.
- **Expect** — the second gets `422 INVENTORY.RESERVATION.INSUFFICIENT_INVENTORY`; their order row is
  `CANCELLED / CANCELLED / RESERVATION_FAILED`; their cart is `ACTIVE` again and can be edited and re-ordered.

### PLC-05 — Inventory down: the request fails before any row, and nothing is left behind · critical · [verified]

Placement prices the lines from inventory before it writes the order, so an inventory outage is a 502 with no order
row at all — there is nothing to resume or recover, and the cart stays `ACTIVE`. The durable path is PLC-06.

- **Steps** — `lcl stop inventory --stack <name>`; check out.
- **Expect** — `502 COMMON.REMOTE_UNAVAILABLE`; no `sales_order` row for the cart; the cart still `ACTIVE`; after
  `lcl start inventory` the same cart checks out normally.
- **Seen** — 502 in ~1 s; cart untouched; the retry placed a fresh order.

### PLC-06 — Payment down: the order row and the reservation survive; a resubmit resumes, recovery finishes · critical · [verified]

- **Steps** — two carts; `lcl stop payment --stack <name>`; check both out; wait 100 s; `lcl start payment`;
  resubmit the first cart; wait ~95 s for the second.
- **Expect** — both checkouts answer `502 COMMON.REMOTE_UNAVAILABLE` (`remoteService: payment`); both orders sit at
  `CREATED / PENDING / RESERVED / INITIATE_PAYMENT` with the stock held; while payment is down the recovery job counts
  an attempt (`pending_action_attempts = 1`, a `RECOVERY_RETRIED` row) and changes nothing else; the resubmit answers
  **201 with the same order id** (one row per cart); the untouched order is finished by the job — `PENDING_PAYMENT /
  RESERVED / NONE` for a transfer, `CONFIRMED / COMMITTED` for COD — and its ledger reads `PLACED, RESERVED,
  RECOVERY_RETRIED ×n, PAYMENT_INITIATED`; a `payment.transaction` exists for each.
- **Seen** — exactly that: orders 1006/1007, stock 149 → 147 and held throughout, two `RECOVERY_RETRIED` rows before
  `PAYMENT_INITIATED`.

### PLC-07 — A refused payment cancels and releases in the same request · critical · [unit only]

`OrderStepRunnerTest.aRefusedPaymentCancelsReleasesTheStockAndIsRethrownAfterwards`;
`CheckoutApiIntegrationTest.aRefusedPaymentCancelsTheOrderAndReleasesTheStock`.

- **Expect** — `422 PAYMENT.INITIATE.REJECTED`; `CANCELLED / FAILED / RELEASED / NONE`; inventory's stock back.
  A declined test card at Stripe is a webhook, not an initiate refusal — see SIG-02 for that path.

### PLC-08 — A guest may order where the store allows it, and is refused where it does not · high · [verified]

- **Steps** — store 1 (requires login): checkout signed out → `401 CHECKOUT.ORDER.LOGIN_REQUIRED`. A store with
  `requireLoginForOrderPlacement` off: checkout signed out → 201, customer row `guest:<email>`.

### PLC-09 — The return page reads the real status, whichever URL it landed on · high · [verified for COD — the card return not verified]

- **Steps** — after a card payment, open `/{lang}/checkout/cancel?orderId=<id>` by hand; after a COD order open
  `/{lang}/checkout/success?orderId=<id>` by hand.
- **Expect** — the page still shows the order as paid: it calls `GET /order/{id}/status` and trusts that, not the
  URL. The redirect link is offered only while `PENDING_PAYMENT`.

### PLC-10 — The status read is owned by the shopper · critical · [verified for the store-2 read — owner scoping not verified]

- **Steps** — read another shopper's `/order/{id}/status` with your token; read it from store 2.
- **Expect** — 404 both times.

### PLC-11 — The order snapshot survives catalog edits · high · [verified via API — the console detail not verified]

- **Steps** — place an order for a variant product; rename the product and its option value in the console; reopen
  the order.
- **Expect** — `sales_order_line.product_name` and `sales_order_line_option` still say what was bought; both the
  console detail and the storefront order page render the old labels.

### PLC-12 — Money is rendered in the store currency · [verified]

- **Expect** — `price`, `subTotal`, `total.total`, `grandTotal` carry the store's currency symbol and the request
  locale's formatting; the numbers behind them are on `sales_order_line.unit_price` / `line_total`.

---

## SIG — Signals from payment and inventory

`POST /private/orders/{ref}/signals/payment` and `…/reservation-expired`, `STORE-POD.CHECKOUT.SIGNAL` (the pod's
own service principal). **A signal is never a 4xx for a state the order cannot use**: it answers `APPLIED`,
`DUPLICATE` (same transaction ref + status seen before) or `IGNORED` (with the reason), because payment's outbox
would otherwise retry a decision that will not change. A ref this store never issued is the one 404.

### SIG-01 — Payment approved in the console confirms the order · critical · [not verified]

- **Steps** — PLC-02, then **Payments → Approve** in the console.
- **Expect** — within a few seconds the order is `CONFIRMED / PAID / COMMITTED`; the ledger has a `PAYMENT_SIGNAL`
  `APPLIED` row keyed `<internal ref>:PAID`, then `COMMITTED`.

### SIG-02 — Payment rejected in the console cancels the order and releases the stock · critical · [verified via the signal API — the console reject button not verified]

Before the rewrite a rejection told nobody; the order waited forever. `PaymentRejectedEvent` is new.

- **Steps** — PLC-02, then **Payments → Reject**.
- **Expect** — `CANCELLED / REJECTED / RELEASED`; inventory's quantity back; the shopper's order page shows it
  cancelled.

### SIG-03 — The same signal twice is a recorded no-op · critical · [verified]

- **Steps** — `external-order-signal-api.http`, "the same signal again".
- **Expect** — `outcome: DUPLICATE`, statuses unchanged, one more ledger row (`outcome = DUPLICATE`, the key in
  `payload`).

### SIG-04 — A late failure after a payment is ignored, not applied · critical · [verified]

- **Expect** — `outcome: IGNORED`, `reason: already paid`; the order stays `CONFIRMED / PAID`.

### SIG-05 — Paid after cancellation is recorded and flagged for a refund · critical · [unit only]

`OrderTransitionTest.paidAfterCancellationIsRecordedAndFlaggedForARefund`.

- **Expect** — payment `PAID`, order stays `CANCELLED`, `needs_attention = true`, reason names the refund; a
  `PAYMENT_AFTER_CLOSE` ledger row. The console detail shows `needsAttention` / `attentionReason`.

### SIG-06 — Only the pod's service principal may signal · critical · [verified]

- **Expect** — a shopper token → 403; the console session → 403; no token → 401; the order untouched.

### SIG-07 — An expired reservation cancels an unpaid order · high · [verified via the signal API]

- **Steps** — place a card order and do not pay; wait for inventory's expiry (`reservation.expiry.minutes`, or
  post the signal by hand).
- **Expect** — `CANCELLED / EXPIRED / RELEASED`; a second delivery is `DUPLICATE`.

### SIG-08 — An expiry arriving after payment but before the commit flags the order · high · [unit only]

`OrderTransitionTest.expiringAPaidOrderBeforeItsCommitFlagsIt`.

- **Expect** — inventory `RELEASED`, `needs_attention` with "re-reserve manually"; the order stays `CONFIRMED / PAID`.

### SIG-09 — PROCESSING / WAITING_VERIFICATION keep the order waiting · [unit only]

`OrderTransitionTest.aPaymentInFlightIsRecordedAndTheOrderKeepsWaiting`, `…waitingForVerificationDropsTheExpiryEntirely`.

- **Expect** — `PROCESSING` extends `expires_at` by 24 h; `WAITING_VERIFICATION` clears it, so a merchant looking
  at a transfer proof is never expired under.

### SIG-10 — A ref this store never issued is 404, and the outbox says so · high · [verified]

- **Expect** — `404 CHECKOUT.ORDER.NOT_FOUND`; on payment's side the outbox record ends `FAILED` with the problem
  detail in `failure_reason` — the documented place to find a lost signal.

---

## JOB — Recovery and expiry

Both run on every replica with no lock. The claim is the order's `@Version`: two replicas picking the same order
both try, the second loses and skips, and the remotes are idempotent by ref anyway.

### JOB-01 — Recovery finishes an order stuck on an outage · critical · [verified]

- **Steps** — PLC-05 without the resubmit.
- **Expect** — after `stale-after` (60 s) the ledger gains `RECOVERY_RETRIED` rows, one per attempt, then the
  missing step; `pending_action_attempts` counts them.

### JOB-02 — Recovery gives up and flags instead of retrying forever · high · [unit only]

`OrderRecoveryJobTest.afterMaxAttemptsTheOrderIsFlaggedAndNoStepRuns`.

- **Expect** — after `max-attempts` (10) `needs_attention` is set with the attempt count, `pending_action` is kept
  so a person can see what was owed, and the job stops touching the order.

### JOB-03 — Expiry cancels an unpaid card order · critical · [unit only]

- **Steps** — card order, do not pay; wait past 30 min (or lower `checkout.placement.expiry.stripe` for the stack).
- **Expect** — `CANCELLED / EXPIRED / RELEASED`, `EXPIRED` then `RELEASED` in the ledger, stock back.

### JOB-04 — A late payment rescues the order instead of expiring it · critical · [unit only]

`OrderJobsIntegrationTest.expiryClosesAnUnpaidCardOrderButSparesOneThatPaymentSaysWasPaid`.

- **Expect** — before cancelling a card order the job asks payment once; `PAID` → the order is confirmed and
  committed with the gateway ref as the signal key; payment unreachable → nothing is decided this pass.

### JOB-05 — COD and verified transfers never expire · high · [verified for COD]

- **Expect** — a COD order has `expires_at` null from placement; a transfer answered `WAITING_VERIFICATION` has it
  cleared. Neither is ever selected by the job.

---

## ORD — The console's orders

### ORD-01 — List, filters, newest first · critical · [not verified]

- **Expect** — `?name`, `?id`, `?status`, `?phone`, `?email`, `?customerId` narrow the list; the list rows carry no
  lines or customer (`products: null`), the detail does; `inventoryStatus` and `redirectUrl` are the field names
  (the console model was renamed from `reservationStatus` / `redirectUri`).

### ORD-02 — Moving an order forward, and an illegal step · critical · [not verified]

- **Steps** — CONFIRMED → PROCESSING → SHIPPED → DELIVERING → DELIVERED; then try DELIVERED again, or COMPLETED
  from PROCESSING.
- **Expect** — each legal step answers 201 with the history entry and the actor; an illegal one answers
  `409 CHECKOUT.ORDER.ILLEGAL_TRANSITION` with `params.from` / `params.to`, and the order does not move. On a COD
  order `DELIVERED` also sets payment `PAID`.

### ORD-03 — Cancelling from the console · high · [not verified]

- **Expect** — a `PENDING_PAYMENT` or `CONFIRMED` order goes `CANCELLED`; a still-reserved one owes `RELEASE`, which
  runs inline and puts the stock back; a paid one keeps `PAID` and is flagged for a refund; a `SHIPPED` one is
  refused with 409.

### ORD-04 — Every private endpoint refuses without the seller token · critical · [verified for the no-token case]

- **Expect** — no session → 401; a shopper token → 403; a store **moderator** → 403 (checkout grants
  `STORE-POD.CHECKOUT.*` to admins only).

### ORD-05 — Another store's seller sees nothing · critical · [not verified]

- **Expect** — list empty for a store 1 id, detail / history / transition → 404, and store 1's admin asking for
  store 2 → 403.

### ORD-06 — Flagged orders are visible · high · [not verified]

- **Expect** — `needsAttention` / `attentionReason` on the detail; `select … where needs_attention` finds them.
  There is no console filter yet (99).

---

## STA — Statistics

### STA-01 — Three charts, one store · high · [not verified]

- **Expect** — `order-statistic` = orders per day per status; `customer-statistic` = **distinct customers** per
  billing country (it used to count orders); `product-statistic` = **units** per sku (it used to count orders). A
  second store's charts do not include the first's.

### STA-02 — Statistics need the seller token · high · [verified for the no-token case]

- **Expect** — shopper → 403, none → 401, another store's admin → 403.

---

## REF — Countries

### REF-01 — The list is public, ISO-complete and localised · [verified]

- **Expect** — ~250 entries, `Germany` in `lang=en`, `Allemagne` in `lang=fr`, `zones` always empty (states are
  free text now); both address forms still load it.

---

## SEC — Secrets and logs

### SEC-01 — Nothing sensitive in the logs · high · [verified]

- **Expect** — `grep -i "authorization\|bearer" .lcl/<stack>/logs/checkout.log` finds nothing; shopper emails
  appear only in the request path, never in an error message.

### SEC-02 — Every CHECK constraint accepts every enum value · critical · [unit only]

`CheckoutContextIntegrationTest.everyEnumValueIsAcceptedByItsCheckConstraint`. This is the test the old
service needed: its DDL rejected `EXPIRED` and `CANCELLED`, so expiry and cancel failed at flush.

---

## REG — Regression watchlist

Defects that actually happened in checkout — most in the service this one replaced. Each row is a re-test.

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Expiry and cancel failed at flush** | Orders never expired; cancel 500ed with a CHECK violation. | SEC-02; JOB-03; SIG-02. |
| **An order lost between two remote calls** | Reserved stock, no payment, order stuck `CREATED` forever. | PLC-05, PLC-06, JOB-01 — stop a service mid-placement and watch the order finish anyway. |
| **A refused payment left the stock held** | Cancelled order, inventory still decremented. | PLC-07: `inventory_status = RELEASED` and the quantity back. |
| **A rejected transfer told nobody** | Payment `REJECTED`, order `PENDING_PAYMENT` forever. | SIG-02. |
| **Any JWT could mark an order paid** | The callback had no permission gate. | SIG-06. |
| **A redelivered webhook applied twice** | The second delivery wrote a second ledger row under the same key and 409ed. | SIG-03 — `DUPLICATE`, not an error. |
| **A cart handed back after a refusal could not order again** | A unique (store, cart) constraint refused the second order. | PLC-04 — the reopened cart places a new order. |
| **Customers collided across stores** | The same cua `sub` in two stores found one row. | CUS-04 — two stores, one shopper account, two rows. |
| **The account page 404ed before the first order** | `CUSTOMER.NOT_FOUND [404]` on My Account for a signed-in shopper with no order yet. | CUS-02 — empty profile, 200. |
| **Totals rendered without labels** | Two bare amounts under the items — `sales_order_total.title` was empty. | PLC-01 — the order page reads `Subtotal` / `Total`. |
| **First name blank on the account page** | Every storefront theme read `customer.firstNames`; the API has always sent `firstName`. | CUS-02 — the profile tab shows the first name. |
| **Order ids walked by URL** | Another shopper's status read answered 403 (confirming the id) or worse, the order. | PLC-10, CUS-02 — 404, never 403. |

---

## 99 — Known gaps

Behaviour that is expected today.

**No refund automation.** Payment has no refund API, so "paid after cancellation" and "cancelled after payment" end
as `needs_attention` orders with a reason, and someone refunds by hand at the provider. The ledger shows exactly
what happened; nothing is lost, nothing is automatic.

**No console filter for flagged orders.** `needsAttention` is on the detail and in the database; a list filter is a
console change waiting on a product decision about the screen.

**The status read is anonymous for stores that allow guest checkout.** A store that requires login gets the
owner-scoped 404; a store that does not lets anyone who knows a numeric order id read `{orderStatus,
paymentStatus}` — the storefront never passes the opaque `orderRef`, and changing that is a landing-ui change.

**No shipping, no tax.** Totals are `SUBTOTAL` and `TOTAL`, always equal; the codes `SHIPPING` and `TAX` are
reserved in the CHECK constraint for when they exist.

**Stripe's declined test card is a webhook, not an initiate refusal.** PLC-07's 422 path is reached from the API
only through a provider that refuses at initiate; with Stripe Checkout a declined card arrives as a `FAILED`
signal after the redirect (SIG path). Both end the same way.

**Guests collapse on email.** A guest who mistypes their email is a new customer row. Chosen over one row per
order so the console's customer list stays meaningful for stores that allow guest checkout.

---

Raise anything unexpected against the checkout rewrite PR with the order id, the `sales_order_event` rows for it
and the matching lines from `.lcl/<stack>/logs/checkout.log` — the ledger is written to answer "what happened to
this order" without a debugger.

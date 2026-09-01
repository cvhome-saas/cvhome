# QA — payment (`store-pod/payment/payment-service`)

Payment takes a shopper's money: it holds each store's provider configuration (encrypted), initiates a payment
with the provider, reports its status back to checkout, and receives the provider's webhooks. It is the
platform's **reference implementation for error handling** — the service where *refused* and *no answer* are
kept apart on purpose — so most of this document is about failure, not the happy path.

- **Scope** — `/api/v1/private/payment-configuration/**`, `/api/v1/private/payment/**`, the s2s
  `/api/v1/private/payments/initiate` and `/{requestRef}/status`, the public supported-types read, and
  `/api/v1/public/webhook/{storeId}/{paymentType}`
- **Runs on** — `lcl start -d --stack <name>`; read the live port from `lcl urls`. Address it through an edge,
  never `:8125`
- **Cases** — 23 (0 verified, 2 unit only, 21 not verified)
- **Also see** — [checkout](../../../checkout/checkout-service/qa/checkout-qa.md) (the caller that places the
  order), [merchant](../../../merchant/merchant-service/qa/merchant-qa.md) (the store record it caches),
  [billing](../../../../store-core/billing/billing-service/qa/billing-qa.md) (**a different thing entirely** —
  billing charges the *merchant* for the platform; payment charges the *shopper* for the goods)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

**This service had no QA document until now.** Every case below was written from `PaymentConfigurationController`,
`PrivatePaymentApi`, `ExternalPaymentGatewayApi`, `PublicPaymentWebhookApi` and the typed exceptions in
`payment-commons/errors`. Nothing here has been driven end to end, so the tags are honest rather than
encouraging: this is where the bugs are.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins, the seeded org/store/pod ids, gateway-vs-pod
addressing and the `psql` idiom are in
[`references/qa-testing.md`](../../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5. Only
what is specific to payment is below.

**A store needs a payment configuration before anything works.** The demo seed does not create one, so start by
adding a provider through the console (Store management → Payments) or
`POST /api/v1/private/payment-configuration`.

> **Provider credentials are encrypted at rest** by `secret-crypto`. A configuration row's secret columns must
> never be readable as plaintext — SEC-04 is that assertion, and it is the one case in this file that is a
> hard requirement rather than a quality bar.

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select store_merchant_id, payment_type, active from payment.payment_configuration;"

# the ciphertext columns — read them, and confirm they are not readable
... "select * from payment.payment_configuration limit 1;"

... "select internal_ref, request_ref, store_merchant_id, payment_type, status, amount, created_date
       from payment.payment_transaction order by created_date desc limit 20;"
```

Logs: `.lcl/<stack>/logs/payment.log`.

---

## CFG — The store's provider configuration

`PaymentConfigurationController` is `/api/v1/private/payment-configuration`, every method gated on
`STORE-POD.PAYMENT.*`. The public read (`/api/v1/public/payment-configuration/{storeId}/supported-payment-types`)
is what the storefront asks before rendering the payment step.

### CFG-01 — Create, read, update and delete a configuration · critical · [not verified]

- **Steps** — `POST` a provider configuration for org1-store1, `GET` the list, `PUT /{paymentType}` to change
  it, `GET /supported-payment-types`, then `DELETE /{paymentType}`.
- **Expect** — each answers 2xx and the list reflects the change. After the delete the type disappears from
  both the private list and the public supported-types read.

### CFG-02 — The public supported-types read needs no session · high · [not verified]

- **Steps** — `GET /api/v1/public/payment-configuration/{storeId}/supported-payment-types` with no token.
- **Expect** — **200** with the active types only. This is the storefront's call, so it must be open — but it
  must also carry **nothing** but the type names: no keys, no merchant ids, no endpoints.

### CFG-03 — A configuration for a type that does not exist · high · [not verified]

- **Steps** — `PUT` and `DELETE` an unknown `paymentType`.
- **Expect** — a typed **404** (`PaymentConfigurationNotFoundException`), not a 500 and not a silent 200.

### CFG-04 — Deactivating a provider removes it from the storefront · high · [not verified]

- **Steps** — set a configuration inactive, then re-read the public supported types.
- **Expect** — the type is gone from the public read while the row survives, so the credentials do not have to
  be re-entered to turn it back on.

---

## PAY — Initiating and settling a payment

`ExternalPaymentGatewayApi` is the s2s surface checkout calls: `POST /api/v1/private/payments/initiate` and
`GET /api/v1/private/payments/{requestRef}/status`.

### PAY-01 — A payment initiates and reports its status · critical · [not verified]

- **Setup** — a configured store and a service token (uaa-qa.md AUT-04).
- **Steps** — `initiate` with an amount and a request ref, then poll `/{requestRef}/status`.
- **Expect** — initiate returns the provider's redirect or intent and a transaction row appears in
  `payment.payment_transaction`; the status read reflects the provider's state and does **not** invent one.

### PAY-02 — The same request ref initiated twice does not charge twice · critical · [not verified]

- **Steps** — call `initiate` twice with the same `requestRef`.
- **Expect** — one transaction, one charge. A double charge is the worst defect this service can have; if the
  second call creates a second row, stop and report it before running anything else.

### PAY-03 — A payment the seller approves by hand · [not verified]

- **Steps** — `POST /api/v1/private/payment/transaction/{internalRef}/approve` as a seller with
  `STORE-POD.PAYMENT.*`.
- **Expect** — the transaction moves state, the change is recorded with the actor, and checkout can see it.

### PAY-04 — The transaction list is store-scoped · critical · [not verified]

- **Steps** — `GET /api/v1/private/payment/transactions` as org1-store1, then with `?store=` set to
  org2-store1 while holding org1's session.
- **Expect** — the first lists only org1-store1's transactions; the second is refused. Another store's payment
  history is the most sensitive read in the pod.

---

## ERR — Refused is not the same as no answer

This is the section the service exists to get right, and the rule
`references/error-handling.md` states plainly: inside a provider call, *refused* (422) and *no answer* (502)
**never share a `catch`**. Collapsing them cancels orders that were charged.

### ERR-01 — A provider that refuses the payment answers 422 · critical · [unit only]

- **Covered by** — `PaymentExceptionsTest` and `StripeProcessorTest`.
- **Steps** — configure a provider and initiate a payment the provider declines (a declined test card).
- **Expect** — `PaymentInitiateRejectedException` → **422**, with a code naming *our* condition. The provider's
  own code or status must **not** be re-emitted as ours.

### ERR-02 — A provider that never answers is 502, not 422 · critical · [unit only]

- **Covered by** — `PaymentExceptionsTest`, `StripeProcessorTest`.
- **Steps** — point the configuration at an unreachable endpoint, or stop the provider stub, and initiate.
- **Expect** — `PaymentProviderUnavailableException` → **502**. The order must remain payable: a transport
  failure is not a refusal, and treating it as one cancels an order that may have been charged.

### ERR-03 — The two are distinguishable from the response alone · critical · [not verified]

- **Steps** — run ERR-01 and ERR-02 and compare the bodies.
- **Expect** — different codes, different statuses, and **no root-cause text in `detail`**. Both carry a
  `traceId`; the cause is in the log, joined by that id.

### ERR-04 — Checkout does not turn a rejection into a success · critical · [not verified]

- **Why it matters** — the recurring violation this rule exists for is a `catch` that swallows a typed failure
  and returns a success shape. A 200 on a rejection kills the typed path for every caller downstream.
- **Steps** — run ERR-01 through a real storefront checkout.
- **Expect** — the shopper sees a refusal, the order is not marked paid, and checkout's response is not 200.
- **Also touches** — [checkout-qa.md](../../../checkout/checkout-service/qa/checkout-qa.md) PAY-01.

---

## WHK — Webhooks

`PublicPaymentWebhookApi` is `POST /api/v1/public/webhook/{storeId}/{paymentType}` — open by necessity, which
makes the signature check the only thing standing between a stranger and a paid order.

### WHK-01 — A forged webhook is rejected · critical · [not verified]

- **Steps** — post a well-formed body with a wrong or missing signature.
- **Expect** — refused via `InvalidWebhookSignatureException`, nothing written, and the attempt logged. A
  webhook that changes a transaction without a valid signature is a **critical** finding: it lets anyone mark
  any order paid.

### WHK-02 — The same event twice does nothing twice · critical · [not verified]

- **Steps** — replay a valid webhook.
- **Expect** — the second is accepted and ignored. Providers retry by design, so a non-idempotent handler
  double-settles.

### WHK-03 — An unreadable or unexpected payload is refused cleanly · high · [not verified]

- **Steps** — post malformed JSON, then a valid envelope carrying an object type the handler does not model.
- **Expect** — `UnreadableWebhookPayloadException` and `UnexpectedWebhookObjectException` respectively — a
  typed 4xx, not a 500, and not a silent 200 that loses the event.

### WHK-04 — A webhook for another store's transaction is ignored · critical · [not verified]

- **Steps** — post a valid webhook to `{storeId}` A carrying a transaction belonging to store B.
- **Expect** — ignored quietly, nothing written. The `storeId` in the path is not trusted on its own.

---

## SEC — Permissions and secrets

### SEC-01 — Every private endpoint refuses without a session · critical · [not verified]

- **Steps** — call each `/api/v1/private/**` path with no token.
- **Expect** — **401** on all of them. Only the public supported-types read and the webhook are open.

### SEC-02 — `STORE-POD.PAYMENT.*` is required, and a moderator does not have it · critical · [not verified]

- **Steps** — as `org1-store1-moderator`, attempt each configuration write and the approve.
- **Expect** — **403**. A new permission token with no `case` in `CustomPermissionEvaluator` denies by
  default — so a 403 here may mean the token is missing from the evaluator rather than that the gate works.
  Check the evaluator before concluding either way.

### SEC-03 — Another org cannot read or write this store's payments · critical · [not verified]

- **Steps** — repeat CFG-01 and PAY-04 with an `org2-admin` session against an org1 store.
- **Expect** — refused throughout.
- **Known platform gap** — `StoreRoleAccessChecker.isOrgAdmin` still returns true for **any** store once the
  caller holds `ROLE_ORG_ADMIN`, so this may pass at the query layer and fail at the permission layer. See
  [tenancy-qa.md](../../../../store-core/tenancy/tenancy-service/qa/tenancy-qa.md) 99 — it is the largest open
  item on the platform and payment is one of the services it affects.

### SEC-04 — Provider credentials are encrypted at rest · critical · [not verified]

- **Steps** — `select *` the configuration row and read the secret columns; then grep
  `.lcl/<stack>/logs/payment.log` for the plaintext secret you entered.
- **Expect** — ciphertext in the column, and **nothing** in the log. A plaintext credential column or a secret
  in a log line is a hard failure, not a quality issue.

### SEC-05 — Nothing sensitive in the logs · high · [not verified]

- **Steps** — run a full initiate → webhook → status cycle and read the log.
- **Expect** — no card data, no provider secret, no full request body from the provider.

---

## DEP — What depends on payment

### DEP-01 — A store edit reaches the other services · high · [not verified]

- **Steps** — change the store's currency (or default language); then load a product page, a cart and an order
  in the console.
- **Expect** — the new value appears in prices and formatting **eventually**. If it does not appear at all,
  find out whether the `STORE` cache has any eviction — a permanently stale store record across four services
  is worth knowing about either way.

- _From `qa/merchant-store-service.md` §DEP — payment is one of the four services that cache the store record
  behind `@Cacheable("STORE")`. The other three assertions are in merchant-qa.md._

### DEP-02 — Checkout degrades rather than 500s when payment is down · critical · [not verified]

- **Steps** — `lcl stop payment`, then attempt a checkout.
- **Expect** — the shopper is told payment is unavailable; checkout answers a typed 502 rather than a 500 or a
  cancelled order. Restart and confirm it recovers without a manual step.

---

## 99 — Known gaps

**Payment has no `http/` directory.** Every endpoint should ship a runnable block in
`store-pod/payment/payment-service/http/<api-class>.http`; none exists. That is the single cheapest thing that
would move most of this file off `[not verified]`, and it is a review-policy violation on the next PR that
touches an endpoint here.

**Nothing in this file has been driven end to end.** The `[unit only]` tags on ERR-01 and ERR-02 are real —
`PaymentExceptionsTest` and `StripeProcessorTest` exist and pass — but the stack path is unproven.

**The console's payment section is listed under Store management** and belongs to this service; it is
described in [console-ui-qa.md](../../../../store-core/console-ui/qa/console-ui-qa.md) §MER only because it shares
the page.

**`isOrgAdmin` ignores the store it is handed** — see SEC-03.

---

Raise anything unexpected against the payment PR. Include the store id, the `internalRef` or `requestRef`, the
time, and the matching `traceId` block from `.lcl/<stack>/logs/payment.log`. **Never paste a provider secret,
a card number or a webhook body into the report.**

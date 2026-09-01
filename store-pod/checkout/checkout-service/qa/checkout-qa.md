# QA — checkout (`store-pod/checkout/checkout-service`)

Checkout is where a shopper's intent becomes an order: the cart, the customer, the order and its status
history, and the statistics the console reports on. It owns no product copy and no stock — it **composes**
[catalog](../../../catalog/catalog-service/qa/catalog-qa.md) and
[inventory](../../../inventory/inventory-service/qa/inventory-qa.md) for every cart line, and holds stock through
inventory's reservations while the shopper pays.

- **Scope** — `ShoppingCartApi`, `OrderApi` / `CustomerOrderApi` / `ExternalOrderApi`, `OrderStatusHistoryApi`,
  `CustomerApi`, `ReferencesApi` and the v2 statistics APIs; the composition of catalog + inventory; the
  reserve → commit → release cycle
- **Runs on** — `lcl start -d --stack <name>`; read the live port from `lcl urls`. Address it through an edge,
  never `:8123`
- **Cases** — 19 (2 verified, 0 unit only, 17 not verified)
- **Also see** — catalog and inventory (the two services it composes),
  [payment](../../../payment/payment-service/qa/payment-qa.md) (which takes the money),
  [landing-ui](../../../landing-ui/qa/landing-ui-qa.md) (the shopper's screens),
  [content](../../../content/content-service/qa/content-qa.md) (the TERMS policy the agreement reads)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins, the seeded org/store/pod ids, gateway-vs-pod
addressing and the `psql` idiom are in
[`references/qa-testing.md`](../../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5. Only
what is specific to checkout is below.

Shop as `user` / `revo` at `http://org1-store1.spg-507f1f77.gateway.com` — the storefront account is scoped per
store and **only authenticates through the store host**.

> **A stale cart survives a container restart and then fails every add-to-cart** with
> `CHECKOUT.CART.NOT_FOUND`. Clear the `cart` keys in `localStorage` — this is the single most common
> false alarm on this service.

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select id, store_merchant_id, customer_id, status, total, created_date
     from checkout.orders order by created_date desc limit 10;"
... "select * from checkout.order_product where order_id=<id>;"
... "select ref, status, expire_at from inventory.product_reservation order by id desc limit 10;"
```

Logs: `.lcl/<stack>/logs/checkout.log`. An `Unhandled failure [traceId=…]` line is a defect regardless of what
the screen showed.

---

## CHK — Composing catalog and inventory

_From `qa/catalog-and-inventory.md` §CHK._

`ProductDetailsComposer` (checkout-core) calls catalog's `detailed-product` and inventory's bulk read for one
sku and hands the cart and order code a `(product, inventory)` pair.

### CHK-01 — Add to cart shows catalog copy and inventory price · critical · [verified]

- **Steps** — `POST /checkout/api/v1/cart?store=…&lang=en` with `{"product": "SKU-NK-RUN-001", "quantity": 1}`.
- **Expect** — the cart line with the product's name and image from catalog and `displayTotal` `SAR750.00`
  from inventory (or 608 while the special is on — the cart charges `finalPrice`).

### CHK-02 — A sku with no inventory cannot be added · critical · [not verified]

- **Steps** — add PRD-01's sku (catalog only, no inventory row).
- **Expect** — refused with checkout's `ProductNotPurchasableException` code; the same for `available: false`
  or `quantity: 0` in inventory. The composer treats a missing sku as *not stocked* — never a 500.

### CHK-03 — A cart line whose product lost its price is dropped · high · [not verified]

- **Steps** — add a product, then delete its inventory rows by SQL, then read the cart.
- **Expect** — the cart is rebuilt without that line (checkout marks the item obsolete). Before the rewrite
  this was a NullPointerException.

### CHK-04 — Catalog down · critical · [not verified]

- **Steps** — stop catalog, add to cart.
- **Expect** — a typed remote failure from checkout, not a 500 with a stack trace; the storefront's cart
  page reports it. Inventory down: the same, with `INVENTORY.*` codes.

### CHK-05 — Placing an order reserves, then commits · critical · [not verified]

- **Steps** — a full storefront checkout (`qa/billing-per-store-subscriptions.md` has the payment set-up).
- **Expect** — `inventory.product_reservation` gains a `TEMPORARY_RESERVED` row under the order ref on
  placement and it becomes `COMPLETED` on payment; the sku's quantity drops by the ordered amount and stays
  down. A failed payment: `ROLLBACK` and the quantity back.

### CHK-06 — The order records the price it sold at · high · [not verified]

- **Steps** — after CHK-05 with a special price active, read `checkout.order_product_price`.
- **Expect** — one row per line: `product_price_code base`, `default_price true`, `product_price` = the final
  price, and the special amount/dates copied when the sale was discounted. Changing the price afterwards does
  not change the order.

---

---

## AGR — The checkout agreement

### AGR-01 — The checkout agreement comes from the live TERMS policy · [verified]

- **Steps** — edit the TERMS policy's text, publish a new version, open checkout.
- **Expect** — the new text. No `agreement` box is consulted.

- _Was SF-02 in `qa/content-owns-appearance-and-media.md`. The storefront's half is
  [landing-ui-qa.md](../../../landing-ui/qa/landing-ui-qa.md) LUI-04, and the policy itself is
  [content-qa.md](../../../content/content-service/qa/content-qa.md) POL-01. All three broke together once: the
  demo stores seeded a legacy `agreement` box but no TERMS policy, and the agreement now reads only
  `GET /storefront/policies/TERMS`._

---

## ORD — Orders

### ORD-01 — Placing an order reserves stock, then commits it · critical · [not verified]

- **Setup** — a store with stock, a signed-in shopper, a configured payment provider.
- **Steps** — place an order through the storefront and watch `inventory.product_reservation`.
- **Expect** — a reservation appears before payment and is **committed** after it succeeds; stock drops once,
  not twice. This is CHK-05 from the other side, and the case both services own together.

### ORD-02 — A failed payment releases the reservation · critical · [not verified]

- **Steps** — place an order with a card the provider declines.
- **Expect** — the order is not marked paid, the reservation is **released**, and the stock returns. A
  reservation left held after a refusal silently removes sellable stock — see inventory RES-05.

### ORD-03 — An abandoned order's reservation expires and checkout is told · critical · [not verified]

- **Steps** — start an order, stop before paying, wait past the reservation window.
- **Expect** — inventory's sweep expires the hold and calls `handleReservationExpired`; the order moves to a
  terminal state rather than sitting payable forever. Inventory's half is RES-06.

### ORD-04 — The order records the price it sold at · high · [not verified]

- **Steps** — place an order, then change the product's price in inventory, then re-read the order.
- **Expect** — the order still shows the price at the time of sale. An order that re-reads today's price is a
  bookkeeping defect, not a display bug. (CHK-06 asserts the same from the composition side.)

### ORD-05 — Order status history records every transition with its actor · [not verified]

- **Steps** — move an order through a few states and read `OrderStatusHistoryApi`.
- **Expect** — one row per transition, each with who or what caused it.

### ORD-06 — The customer's own order list shows only their orders · critical · [not verified]

- **Steps** — as a signed-in shopper, `CustomerOrderApi` list; then as a second shopper in the same store.
- **Expect** — each sees only their own. A shopper seeing another shopper's order is a customer-data breach,
  not a scoping bug.

---

## SEC — Permissions and tenant isolation

Every private endpoint carries
`@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')")` — the customer
endpoints use `STORE-POD.CUSTOMER.*`.

### SEC-01 — Every private endpoint refuses without a session · critical · [not verified]

- **Steps** — call a sample of `/private/**` order, customer and statistics paths with no token.
- **Expect** — **401** on each.

### SEC-02 — A moderator can read orders and cannot change them · critical · [not verified]

- **Steps** — as `org1-store1-moderator`, list orders, then attempt a status change and a customer edit.
- **Expect** — reads pass, writes are **403**.

### SEC-03 — Another store's orders are not reachable · critical · [not verified]

- **Steps** — repeat the order list with `?store=` set to org1-store2 and to org2-store1 while holding an
  org1-store1 session.
- **Expect** — refused, or an empty page — never another store's rows. Check the order **ids** too: nothing may
  cross stores through an id in a path or body.

### SEC-04 — Nothing sensitive in the logs · high · [not verified]

- **Steps** — run a full cart → order → payment cycle and read the log.
- **Expect** — no card data, no customer address dumped at INFO, no provider secret.

---

## STA — Statistics

The v2 statistics APIs (`OrderStatisticApi`, `CustomerStatisticApi`, `ProductStatisticApi`) back the console's
dashboard.

### STA-01 — Each statistic answers, scoped to the store · high · [not verified]

- **Steps** — call all three for org1-store1 with some seeded orders, then for a store with none.
- **Expect** — real numbers for the first, zeroes rather than a 500 for the second, and no row from another
  store in either.

### STA-02 — A statistics query is store-scoped in SQL, not just in the response · critical · [not verified]

- **Why it matters** — an aggregate that forgets its `where store_merchant_id = ?` returns a number that looks
  plausible and is the whole platform's. Check the query, not only the answer: totals for org1-store1 plus
  org1-store2 must not equal what either reports alone.

---

## 99 — Known gaps

**Checkout has no `http/` directory.** Ten API classes and not one runnable block. That is the cheapest way to
move most of this file off `[not verified]`, and it is a review-policy violation for the next PR that touches
an endpoint here.

**`quantityOrderMaximum` is not enforced.** Inventory records a per-sku minimum and maximum and checkout does
not read either — see inventory 99.

**Inventory has no billing write gate**, so a lapsed store's stock still moves through a checkout even though
the seller cannot edit the catalog. Deliberate: a suspended store keeps selling.

**A cart line whose product lost its price is dropped** rather than shown as unavailable (CHK-03). Silently
losing a line is friendlier than a 500 and worse than saying so.

**`isOrgAdmin` ignores the store it is handed** — see
[tenancy-qa.md](../../../../store-core/tenancy/tenancy-service/qa/tenancy-qa.md) 99. Checkout is one of the pod
services that gap affects, so SEC-03 may pass at the query layer and fail at the permission layer.

---

Raise anything unexpected against the checkout PR. Include the store id, the order id or cart ref, the time,
and the matching `Unhandled failure [traceId=…]` block from `.lcl/<stack>/logs/checkout.log`.

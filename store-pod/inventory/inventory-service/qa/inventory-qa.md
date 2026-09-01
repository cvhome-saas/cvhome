# QA — inventory (`store-pod/inventory/inventory-service`)

Inventory owns stock and price, keyed by sku, and the reservations that hold stock while an order is being
paid for. It owns no product copy — that is
[catalog](../../../catalog/catalog-service/qa/catalog-qa.md) — and the two are composed for the shopper by
[checkout](../../../checkout/checkout-service/qa/checkout-qa.md).

- **Scope** — the bulk read, the stock/price upsert, the boot migration from the old catalog tables, and the
  reserve / commit / release / expire cycle
- **Runs on** — `lcl start -d --stack <name>`; read the live port from `lcl urls`. Address it through the
  gateway, never `:8126`
- **Cases** — 19 (10 verified, 3 unit only, 6 not verified)
- **Also see** — catalog (SEC-01…05 sweep both services), checkout (the caller of every reservation),
  [billing](../../../../store-core/billing/billing-service/qa/billing-qa.md) (inventory has **no** write gate —
  see 99)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins, the seeded org/store/pod ids, gateway-vs-pod
addressing and the `psql` idiom are in
[`references/qa-testing.md`](../../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5.
Only what is specific to inventory is below.

### Addressing

```
http://gateway.com:8000/spg/inventory/api/v1/...?store=<id>&pod=507f1f77bcf86cd799439011      # seller path
http://org1-store1.spg-507f1f77.gateway.com/inventory/api/v1/...?store=<id>                   # pod path
```

The platform gateway route predicates on `pod` as well as `store` — a 404 through `/spg/**` is usually a
missing `pod`. Runnable blocks:
`store-pod/inventory/inventory-service/http/{inventory,reservation}-api.http`. The reservation blocks need a
token with same-pod access; a seller session works locally because `STORE-POD.INVENTORY.RESERVE` maps to
same-pod.

### The seeded stock

One `product_availability` row and one `base` price per product, 180 rows across the demo stores.
`SKU-NK-RUN-001` on org1-store1 is 25 in stock at 750.00.

### State an earlier QA pass left behind

The PR #282 smoke tests left `SKU-NK-RUN-001` at **23** units (a reservation under ref `smoke-1` was committed;
`smoke-2` was refused). The price is unchanged: the upsert with a special was refused 403, because a service
token cannot manage stock (INV-04). Reset before the INV cases if you want the seeded number:

```sql
update inventory.product_availability set quantity=25 where sku='SKU-NK-RUN-001';
```

or `docker compose down -v` and a fresh `lcl start -d`, which reseeds everything.

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select product_avail_id, sku, quantity, available, quantity_ord_min, quantity_ord_max
     from inventory.product_availability where store_merchant_id='65f023632bc46470c104b76f' order by 1;"
... "select p.product_price_id, a.sku, p.product_price_amount, p.product_price_special_amount,
            p.product_price_special_st_date, p.product_price_special_end_date, p.default_price
       from inventory.product_price p join inventory.product_availability a using (product_avail_id);"
... "select ref, status, expire_at from inventory.product_reservation order by id desc limit 10;"
... "select * from inventory.sm_sequencer;"
```

Logs: `.lcl/<stack>/logs/inventory.log`.

---

## INV — Stock and price (inventory)

`GET /inventory/api/v1/availability?skus=a,b,c` is the **only read** — public, store-scoped, bulk. A sku with
no record is **absent** from the answer, not present with zeros. `PUT /private/inventory/{sku}` is the
**only write**: quantity, available, order min/max and one price with an optional special amount and window.

### INV-01 — Bulk read · critical · [verified]

- **Steps** — `?skus=SKU-NK-RUN-001,SKU-NOPE&store=<org1-store1>`.
- **Expect** — one element: `sku, productId, available, canBePurchased, quantity, quantityOrderMinimum,
  quantityOrderMaximum, price{originalPrice, finalPrice, discounted, discountPercent, specialAmount,
  specialStartDate, specialEndDate}`. Raw numbers — no currency strings; every caller formats. `SKU-NOPE`
  absent.

### INV-02 — `canBePurchased` is available **and** in stock · critical · [unit only]

`SkuInventoryMapperTest.notPurchasableWithoutStockOrWhenUnavailable`.

- **Steps** — set `available: false` on a sku (INV-05), read; set `quantity: 0`, read.
- **Expect** — `canBePurchased: false` in both; `available` reports the flag alone.

### INV-03 — Discount windows · critical · [unit only]

`SkuInventoryMapperTest` (7 cases: inside window, start-only, end-only, open-ended, start today, end today,
default price over other rows).

- **Steps** — through INV-05 set `specialAmount 608` with no dates; then `specialEndDate` yesterday; then
  `specialStartDate` tomorrow; then start today.
- **Expect** — `discounted: true, finalPrice 608, discountPercent 20` (on 760); then not discounted; then not;
  then discounted. Start is inclusive, end exclusive; a window with only a start is honoured (the legacy rule
  ignored it — a deliberate change).

### INV-04 — Cross-tenant and no-token · critical · [verified]

- **Steps** — the bulk read with `store=<org2-store1>` and org1's skus; `PUT /private/inventory/…` with no
  token.
- **Expect** — `[]`; **401**. Then `PUT` with the pod's **s2s token** (client-credentials against uaa,
  scope `store_pod`): **403** `COMMON.ACCESS_DENIED` (verified) — a service principal can reserve, not manage.

### INV-05 — The upsert creates, then updates, one row · critical · [not verified] (400/401 paths verified)

- **Steps** — `PUT /private/inventory/HTTP-DEMO-001` with `{"productId": <PRD-01 id>, "quantity": 12,
  "available": true, "price": {"amount": 25, "specialAmount": 19.9, "specialEndDate": "2099-12-31"}}`; read the
  DB; `PUT` again with `quantity 3` and `price.amount 30`, no special.
- **Expect** — 200 with the resolved `SkuInventory` (`finalPrice 19.9, discounted true, discountPercent 20`);
  one `product_availability` row with `sku`, `product_id`, `store_merchant_id`, and one `product_price` row
  (`base`, default, `store_merchant_id` set). The second call **updates both rows in place** (same ids) and
  clears the special. `quantityOrderMinimum/Maximum` omitted: kept (1 / 0 on create).

### INV-06 — Validation · high · [verified]

- **Steps** — `{"quantity": -1, "available": true}` (no price); `{"quantity": 1, "available": true, "price":
  {"amount": -5}}`.
- **Expect** — 400 with `fields` on `quantity` / `price`, then on `price.amount`.

### INV-07 — A legacy sku with several price rows · [unit only]

`SkuInventoryMapperTest.defaultPriceWinsOverOtherLegacyPriceRows`. Insert a second, non-default price row for
a sku by SQL; the read answers the default one; an upsert **edits the default one** and leaves the other.

### INV-08 — Delete by product is best-effort orphan cleanup · high · [not verified]

- **Steps** — `DELETE /private/inventory/by-product/<PRD-01 id>`; again.
- **Expect** — 200 both times (a product with no rows is a no-op). A row referenced by a reservation line
  (RES-01 first): **record what happens** — the FK from `product_reservation_line` is expected to make it a
  **500**; the console swallows it and the row stays, which is harmless but should be known.

### INV-09 — Boot migration from the old catalog tables · critical · [verified]

`CatalogDataMigration` runs `migrate-from-catalog.sql` on **every** start; idempotent; no-op once the catalog
tables are gone.

- **Steps** — on a database that still has `catalog.product_availability` / `product_price` /
  `product_reservation*` rows (a pre-split dump, or the simulation the PR ran), start inventory; read the log
  and `inventory.sm_sequencer`.
- **Expect** — every availability row copied **with `sku` backfilled** from `catalog.product` (the column was
  NULL pre-split and it is now the reservation key); every price row copied with `store_merchant_id` from its
  availability; reservations and lines copied; every sequencer **≥ the max copied id**; log line
  `Catalog-to-inventory data migration completed; all availability rows carry a sku`. A second start changes
  nothing. Any row left with a NULL sku logs an **ERROR** naming the count — that is a finding.

### INV-10 — Dropping the old tables is manual · [not verified]

- **Steps** — after INV-09 is verified on the target, run `extra/scripts/drop-catalog-inventory-tables.sql`.
- **Expect** — catalog boots without them; nothing in either service references them. Do not run it before
  INV-09 has been checked on that database.

---

---

## RES — Reservations (checkout ↔ inventory)

Every call is keyed by the order ref and idempotent. Expiry is 45 minutes (`reservation.expiry.minutes`), the
sweep runs every 60 s (`reservation.cleanup.interval`) and tells checkout.

### RES-01 — Reserve takes stock; a retry does not take it twice · critical · [verified]

- **Steps** — `POST /private/reserve/qa-1` with `{"entries": [{"sku": "SKU-NK-RUN-001", "reserveQty": 2}]}`;
  read INV-01; `POST` the same again.
- **Expect** — `{"status": true, "reservationId": n, "expireAt": <now+45m>}`; quantity down by 2; the retry
  answers the **same** reservation and the quantity is **still** down by only 2.

### RES-02 — Not enough stock, and a sku that is not stocked · critical · [verified]

- **Steps** — `reserveQty: 999`; then `"sku": "SKU-NOPE"`.
- **Expect** — **422** `INVENTORY.RESERVATION.INSUFFICIENT_INVENTORY` with `params {sku, requested,
  available}` (available `0` for the unstocked sku); **nothing was taken** for any line of that request
  (the transaction rolls back — send two entries, the second short, and check the first's quantity).

### RES-03 — No lines · high · [verified]

- **Expect** — `{"entries": []}` → **400** `INVENTORY.RESERVATION.EMPTY`.

### RES-04 — Commit, then release after commit · critical · [verified]

- **Steps** — `POST /private/commit/qa-1`; `POST /private/release/qa-1`.
- **Expect** — `status: true` (row `COMPLETED`); then `status: false` — a committed reservation is not given
  back. Commit again: `true` (idempotent). Commit an unknown ref: `status: false`, no exception.

### RES-05 — Release gives the stock back · critical · [not verified]

- **Steps** — reserve `qa-2` for 3; release; release again; read the quantity.
- **Expect** — `true` (row `ROLLBACK`, quantity restored); `true` again; the quantity restored **once**.

### RES-06 — Expiry sweeps a held reservation and tells checkout · critical · [not verified]

- **Steps** — set `reservation.expiry.minutes=1` for inventory (`application-lcl.yml` or `-D`), reserve
  `qa-3`, wait two minutes, read the quantity and `inventory.log` / `checkout.log`.
- **Expect** — quantity restored, row `ROLLBACK`, inventory logs `Released expired reservation qa-3`, checkout
  received `handleReservationExpired` (checkout's log; the order — if one exists under that ref — moves to
  its expired state). Commit after expiry: `status: false` with a warning logged. If checkout is down the
  release still happens and inventory logs the failed notification once.

### RES-07 — Two orders racing for the last unit · high · [not verified]

- **Steps** — set a sku to quantity 1; fire two reserves for 1 with different refs concurrently (two `curl &`).
- **Expect** — exactly one `status: true`, one 422. The reserve path reads the row with a pessimistic write
  lock.

### RES-08 — Reservation calls are same-pod only · critical · [verified] / [not verified]

- **Steps** — no token (verified 401); a seller session from **another pod's** store (only possible with a
  second pod locally — see `qa/tenancy-and-pod-registry-split.md`).
- **Expect** — 401; 403.

---

---

## SEC — Permissions and tenant isolation

The five cases that sweep every private catalog **and** inventory endpoint (no session, the
`STORE-POD.INVENTORY.*` tokens, another org, ids in the path or body, and secrets in the log) are kept whole in
[catalog-qa.md §SEC](../../../catalog/catalog-service/qa/catalog-qa.md#sec--permissions-and-tenant-isolation)
rather than split in half. Inventory's own gate cases are **INV-04** (cross-tenant and no-token) and **RES-08**
(reservation calls are same-pod only), above.

---

## ARC — What the rewrite left behind

### ARC-02 — The inventory schema holds no price descriptions · [verified]

- **Expect** — `\dt inventory.*`: sequencer, product_availability, product_price, product_reservation,
  product_reservation_line. Seeds create no description rows; the migration copies none.

> ARC-01, ARC-03 and ARC-04 are catalog's, in catalog-qa.md.

---

## REG — Regression watchlist

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Upsert 400'd from the console** | `PersistableInventory.sku` was `@NotEmpty`, validated before the controller could copy it from the path. | INV-05 |
| **Generated seed inserts had 7 values for 6 columns** | A regex added `sku` to wrapped column lists inconsistently. | INV-09 — inventory boots and `select count(*) from inventory.product_availability` = 180 |
| **`restart inventory` took the whole stack down** | Under the old `run-lcl.sh` supervisor a restart read as a service exit and brought everything down. `lcl restart <svc>` replaces one service and leaves the rest up. | [`qa/lcl-qa.md`](../../../../qa/lcl-qa.md) case 06 |

---

## 99 — Known gaps

**Inventory has no billing write gate.** `StoreBillingWriteGate` is catalog's; a store with a lapsed
subscription can still change stock and prices through the inventory API. The console reaches inventory only
after a catalog write that the gate refuses, so it is not reachable from the screen.

**An inventory row survives its product.** The console deletes it best-effort; by API, nothing does. Orphans
are invisible to every reader (the bulk read is by sku and the listing never joins inventory) and cost a row.

**`quantityOrderMaximum` is `0` for "no limit".** Both bounds are enforced now, at both ends: the cart refuses
an out-of-range quantity with `CHECKOUT.CART.QUANTITY_OUT_OF_RANGE`, and the storefront's buy box clamps its
stepper to them so the refusal is not reachable by clicking (see `store-pod/landing-ui/qa/landing-ui-qa.md`).
The seeds no longer cap every fashion and beauty row at 1.

**Dropping the old catalog price/availability tables is manual** (INV-10). The boot migration copies; it never
drops.

---

Raise anything unexpected against the catalog/inventory PR. Include the store id, the sku, the time, and the
matching `Unhandled failure [traceId=…]` block from `.lcl/<stack>/logs/inventory.log`.

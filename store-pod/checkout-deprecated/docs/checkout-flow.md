# Online Store System - Payment & Inventory Flow Documentation

## Table of Contents
- [System Modules](#system-modules)
- [Inventory Status Lifecycle](#inventory-status-lifecycle)
- [Payment Status Lifecycle](#payment-status-lifecycle)
- [Order Status Lifecycle](#order-status-lifecycle)
- [Flow 1: COD (Cash on Delivery)](#flow-1-cod-cash-on-delivery)
- [Flow 2: Online Payment (Stripe)](#flow-2-online-payment-stripe)
- [Flow 3: Manual Transfer](#flow-3-manual-transfer)
- [Comparison Table](#comparison-table)
- [Edge Cases & Error Handling](#edge-cases--error-handling)

---

## System Modules

```
┌─────────────────────────────────────────────────────────┐
│                    ONLINE STORE SYSTEM                   │
├─────────────────┬─────────────────┬─────────────────────┤
│   INVENTORY     │     ORDER       │      PAYMENT        │
├─────────────────┼─────────────────┼─────────────────────┤
│ - Product Stock │ - Order Details │ - Payment Records   │
│ - Reservations  │ - Order Status  │ - Transaction Info  │
│ - Status Track  │ - Line Items    │ - Gateway Response  │
│ - Quantity Mgmt │ - Totals        │ - Verification Data │
└─────────────────┴─────────────────┴─────────────────────┘
```

---

## Inventory Status Lifecycle

```
                    ┌──────────────────┐
                    │    AVAILABLE     │
                    │  (Ready to sell) │
                    └────────┬─────────┘
                             │
              User initiates order (ANY payment method)
                             │
                             ▼
                    ┌──────────────────┐
                    │    RESERVED      │◄─────────────────────┐
                    │  (Temp hold for  │                      │
                    │   order, 15-30   │    Admin rejects     │
                    │   min timeout)   │    payment proof     │
                    └────────┬─────────┘                      │
                             │                               │
          ┌──────────────────┼──────────────────┐            │
          │                  │                  │            │
    Payment      Payment Failed/Expired    Admin Approves    │
    Confirmed    or Order Cancelled        (Manual Only)     │
          │                  │                  │            │
          ▼                  ▼                  │            │
   ┌──────────────┐   ┌──────────────┐         │            │
   │  COMMITTED   │   │   RELEASED   │─────────┘            │
   │ (Permanently │   │  (Back to    │                      │
   │  deducted)   │   │  AVAILABLE)  │──────────────────────┘
   └──────────────┘   └──────────────┘
```

### Inventory Status Definitions

| Status | Description | Stock Deducted? | Can Be Sold? |
|--------|-------------|-----------------|--------------|
| `AVAILABLE` | Normal state, ready for purchase | ❌ No | ✅ Yes |
| `RESERVED` | Temporarily held for an order | ❌ No | ❌ No |
| `COMMITTED` | Permanently removed from available stock | ✅ Yes | ❌ No |
| `RELEASED` | Was reserved, now back to available | ❌ No | ✅ Yes |

### Reservation Timeout
```
┌─────────────────────────────────────────────────────────┐
│              RESERVATION TIMEOUT RULES                  │
├─────────────────────┬───────────────────────────────────┤
│ Payment Method      │ Timeout Duration                  │
├─────────────────────┼───────────────────────────────────┤
│ COD                 │ No timeout (waits for delivery)   │
│ Stripe              │ 30 minutes                        │
│ Manual Transfer     │ 24-48 hours (configurable)        │
└─────────────────────┴───────────────────────────────────┘
```

---

## Payment Status Lifecycle

```
                              ┌──────────────┐
                              │    PENDING   │
                              │ (Initiated,  │
                              │  not paid)   │
                              └──────┬───────┘
                                     │
         ┌───────────────────────────┼───────────────────────────┐
         │                           │                           │
    User pays via              User submits               Timeout/
    Stripe                      proof (Manual)            Cancelled
         │                           │                           │
         ▼                           ▼                           ▼
  ┌──────────────┐          ┌──────────────────┐         ┌──────────────┐
  │  PROCESSING  │          │WAITING_VERIFICATION│        │   EXPIRED    │
  │(Stripe proc- │          │(Admin needs to    │         │  CANCELLED   │
  │ essing)      │          │ verify transfer)  │         │   FAILED     │
  └──────┬───────┘          └────────┬─────────┘         └──────────────┘
         │                           │
    ┌────┴────┐              ┌───────┴───────┐
    │         │              │               │
  Success   Failed       Approved        Rejected
    │         │              │               │
    ▼         ▼              ▼               ▼
┌────────┐ ┌────────┐  ┌────────┐    ┌────────────┐
│  PAID  │ │ FAILED │  │  PAID  │    │  REJECTED  │
└────────┘ └────────┘  └────────┘    └─────┬──────┘
                                           │
                                    User can resubmit
                                           │
                                           ▼
                                   ┌──────────────────┐
                                   │WAITING_VERIFICATION│
                                   └──────────────────┘
```

### Payment Status Definitions

| Status | Description | Applicable Methods |
|--------|-------------|-------------------|
| `PENDING` | Payment initiated, awaiting action | All |
| `PROCESSING` | Stripe is processing the payment | Stripe |
| `PAID` | Payment successfully completed | All |
| `FAILED` | Payment attempt failed | Stripe |
| `EXPIRED` | Reservation timeout, no payment received | Stripe, Manual |
| `CANCELLED` | User or system cancelled payment | All |
| `WAITING_VERIFICATION` | Awaiting admin verification | Manual |
| `REJECTED` | Admin rejected payment proof | Manual |
| `REFUNDED` | Payment refunded after completion | All |

---

## Order Status Lifecycle

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        ORDER STATUS FLOW                                │
└─────────────────────────────────────────────────────────────────────────┘

COD Flow:
┌──────────┐    ┌───────────┐    ┌────────────┐    ┌───────────┐    ┌───────────┐
│ PENDING  │───►│ CONFIRMED │───►│ PROCESSING │───►│ DELIVERING│───►│ COMPLETED │
└──────────┘    └───────────┘    └────────────┘    └───────────┘    └───────────┘
     │
     └──► CANCELLED

Stripe/Manual Flow (Before Payment):
┌────────────────┐         ┌───────────┐
│PENDING_PAYMENT │────────►│ CANCELLED │
└────────────────┘         └───────────┘
        │
   Payment Success
        │
        ▼
┌───────────┐    ┌────────────┐    ┌───────────┐    ┌───────────┐
│ CONFIRMED │───►│ PROCESSING │───►│ DELIVERING│───►│ COMPLETED │
└───────────┘    └────────────┘    └───────────┘    └───────────┘
```

### Order Status Definitions

| Status | Description | Inventory State | Payment Expected |
|--------|-------------|-----------------|------------------|
| `PENDING_PAYMENT` | Order created, waiting for payment | RESERVED | Before payment |
| `PENDING` | Order confirmed (COD), waiting for delivery | RESERVED | After delivery |
| `CONFIRMED` | Payment received, preparing order | COMMITTED | Paid |
| `PROCESSING` | Order being prepared/packed | COMMITTED | Paid |
| `DELIVERING` | Order out for delivery | COMMITTED | Paid |
| `COMPLETED` | Order delivered and closed | COMMITTED | Paid/Collected |
| `CANCELLED` | Order cancelled | RELEASED | None |

---

## Flow 1: COD (Cash on Delivery)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           COD PAYMENT FLOW                                   │
└─────────────────────────────────────────────────────────────────────────────┘

USER ACTION                  SYSTEM ACTION                 STATUS CHANGES
─────────────                ─────────────                 ────────────────

1. Add to Cart
   │
   ▼
2. Checkout (Select COD)
   │                         • Create Order
   │                         • Reserve Inventory
   │                         • Create Payment Record
   │
   ▼                         Order:    PENDING
                             Inventory: RESERVED
                             Payment:  PENDING

3. [Wait for Delivery]
   │
   │                         ⚠️ NO TIMEOUT for COD
   │                         (Inventory stays RESERVED
   │                          until delivery/cancel)
   │
   ▼
4. Admin Marks Delivered
   │                         • Update Order Status
   │                         • Update Payment Status
   │                         • Commit Inventory
   │
   ▼                         Order:    COMPLETED
                             Inventory: COMMITTED
                             Payment:  PAID (collected)


═══════════════════════════════════════════════════════════════════════════════
                           CANCELLATION PATH (COD)
═══════════════════════════════════════════════════════════════════════════════

User/Admin Cancels
   │
   ▼                         • Update Order Status
                             • Update Payment Status
                             • Release Inventory

                             Order:    CANCELLED
                             Inventory: RELEASED → AVAILABLE
                             Payment:  CANCELLED
```

### COD Summary Table

| Stage | Order Status | Payment Status | Inventory Status |
|-------|-------------|----------------|------------------|
| After Checkout | `PENDING` | `PENDING` | `RESERVED` |
| During Delivery | `PENDING` | `PENDING` | `RESERVED` |
| After Delivery | `COMPLETED` | `PAID` | `COMMITTED` |
| If Cancelled | `CANCELLED` | `CANCELLED` | `RELEASED` |

**Key Points for COD:**
- Inventory is reserved immediately but NOT committed
- No payment timeout (waits for delivery)
- Payment marked as `PAID` only AFTER delivery (cash collected)
- Inventory committed ONLY after successful delivery

---

## Flow 2: Online Payment (Stripe)

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         STRIPE PAYMENT FLOW                                  │
└─────────────────────────────────────────────────────────────────────────────┘

USER ACTION                  SYSTEM ACTION                 STATUS CHANGES
─────────────                ─────────────                 ────────────────

1. Add to Cart
   │
   ▼
2. Checkout (Select Stripe)
   │                         • Create Order
   │                         • Reserve Inventory ⏱️
   │                         • Create Payment Record
   │                         • Start 30-min timer
   │
   ▼                         Order:    PENDING_PAYMENT
                             Inventory: RESERVED
                             Payment:  PENDING

3. Redirect to Stripe
   │                         • Generate Stripe Session
   │                         • Store Session ID
   │
   ▼                         Payment:  PROCESSING

4a. USER PAYS SUCCESSFULLY ──────────────────────────────────────────┐
   │                                                                  │
   │                         • Verify with Stripe API                 │
   │                         • Update Payment Status                  │
   │                         • Commit Inventory                       │
   │                         • Update Order Status                    │
   │                                                                  │
   ▼                         Order:    CONFIRMED                      │
                             Inventory: COMMITTED                     │
                             Payment:  PAID                           │
                                                                      │
5. Admin Processes Order                                           │
   │                                                                  │
   ▼                                                                  │
6. Admin Marks Delivered                                             │
   │                                                                  │
   ▼                         Order:    COMPLETED                      │
                             Payment:  PAID (unchanged)               │
                                                                      │
═════════════════════════════════════════════════════════════════════│═══
                           FAILURE PATHS (STRIPE)                    │
═════════════════════════════════════════════════════════════════════│═══

4b. PAYMENT FAILED ─────────────────────────────────────────────────┐│
   │                                                                ││
   │                         • Update Payment Status                 ││
   │                         • Release Inventory                     ││
   │                         • Update Order Status                   ││
   │                                                                ││
   ▼                         Order:    CANCELLED                     ││
                             Inventory: RELEASED → AVAILABLE         ││
                             Payment:  FAILED                        ││
   │                                                                ││
   └──────── User can retry (new order) ◄───────────────────────────┘│
                                                                     │
4c. USER ABANDONS / TIMEOUT (30 min) ◄──────────────────────────────┘
   │
   │                         • Cron job detects timeout
   │                         • Update Payment Status
   │                         • Release Inventory
   │                         • Update Order Status
   │
   ▼                         Order:    CANCELLED
                             Inventory: RELEASED → AVAILABLE
                             Payment:  EXPIRED
```

### Stripe Webhook Flow

```
┌─────────────┐                    ┌─────────────┐
│   STRIPE    │─── Webhook ───────►│   SYSTEM    │
│             │                    │             │
│ Events:     │                    │ Actions:    │
│ • payment_  │                    │ • Verify    │
│   intent.   │                    │ • Update    │
│   succeeded │                    │   Status    │
│ • payment_  │                    │ • Commit    │
│   intent.   │                    │   Inventory │
│   failed    │                    │             │
└─────────────┘                    └─────────────┘
```

### Stripe Summary Table

| Stage | Order Status | Payment Status | Inventory Status | Timeout |
|-------|-------------|----------------|------------------|---------|
| After Checkout | `PENDING_PAYMENT` | `PENDING` | `RESERVED` | - |
| Redirected to Stripe | `PENDING_PAYMENT` | `PROCESSING` | `RESERVED` | 30 min |
| Payment Success | `CONFIRMED` | `PAID` | `COMMITTED` | - |
| After Delivery | `COMPLETED` | `PAID` | `COMMITTED` | - |
| Payment Failed | `CANCELLED` | `FAILED` | `RELEASED` | - |
| Timeout/Abandoned | `CANCELLED` | `EXPIRED` | `RELEASED` | 30 min |

**Key Points for Stripe:**
- Inventory reserved for **30 minutes** only
- If not paid within timeout → auto-cancel → release inventory
- Inventory committed **immediately** after successful payment
- Payment status changes to `PAID` before delivery
- Webhook ensures reliable status updates

---

## Flow 3: Manual Transfer

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        MANUAL TRANSFER FLOW                                  │
└─────────────────────────────────────────────────────────────────────────────┘

USER ACTION                  SYSTEM ACTION                 STATUS CHANGES
─────────────                ─────────────                 ────────────────

1. Add to Cart
   │
   ▼
2. Checkout (Select Manual Transfer)
   │                         • Create Order
   │                         • Reserve Inventory ⏱️
   │                         • Create Payment Record
   │                         • Start 24-48hr timer
   │                         • Show bank details to user
   │
   ▼                         Order:    PENDING_PAYMENT
                             Inventory: RESERVED
                             Payment:  PENDING

3. User Transfers Money
   │                         (External: Bank/App)
   │
   ▼
4. User Submits Proof
   │                         • Store transaction number
   │                         • Store proof image/reference
   │                         • Update Payment Status
   │                         • Notify Admin
   │
   ▼                         Order:    PENDING_PAYMENT
                             Inventory: RESERVED (still)
                             Payment:  WAITING_VERIFICATION

═══════════════════════════════════════════════════════════════════════════════
                        ADMIN VERIFICATION (Manual Transfer)
═══════════════════════════════════════════════════════════════════════════════

5. Admin Opens Admin Panel
   │
   ▼
6. Admin Sees Pending Verifications
   │
   │    ┌─────────────────────────────────────────────────────────────┐
   │    │  ADMIN DASHBOARD - PENDING VERIFICATIONS                    │
   │    │  ┌───────────┬──────────────┬───────────────┬────────────┐  │
   │    │  │ Order #   │ Transaction #│ Amount        │ Submitted  │  │
   │    │  ├───────────┼──────────────┼───────────────┼────────────┤  │
   │    │  │ ORD-001   │ TXN-ABC123  │ $150.00       │ 2 hrs ago  │  │
   │    │  │ ORD-002   │ TXN-XYZ789  │ $75.50        │ 5 hrs ago  │  │
   │    │  └───────────┴──────────────┴───────────────┴────────────┘  │
   │    └─────────────────────────────────────────────────────────────┘
   │
   ▼
7a. ADMIN APPROVES ─────────────────────────────────────────────────┐
   │                                                                 │
   │                         • Verify transaction                    │
   │                         • Update Payment Status                 │
   │                         • Commit Inventory                       │
   │                         • Update Order Status                    │
   │                         • Notify User                            │
   │                                                                 │
   ▼                         Order:    CONFIRMED                      │
                             Inventory: COMMITTED                     │
                             Payment:  PAID                           │
                                                                      │
8. Admin Processes & Delivers                                        │
   │                                                                  │
   ▼                         Order:    COMPLETED                      │
                             Payment:  PAID (unchanged)               │
                                                                      │
═════════════════════════════════════════════════════════════════════│═══
                           REJECTION PATH (Manual)                   │
═════════════════════════════════════════════════════════════════════│═══

7b. ADMIN REJECTS ──────────────────────────────────────────────────┐│
   │  (Wrong amount, no matching transaction, etc.)                 ││
   │                                                                ││
   │                         • Update Payment Status                 ││
   │                         • Keep Inventory RESERVED               ││
   │                         • Notify User with reason               ││
   │                         • Reset timer (optional)                ││
   │                                                                ││
   ▼                         Order:    PENDING_PAYMENT               ││
                             Inventory: RESERVED (still)             ││
                             Payment:  REJECTED                      ││
   │                                                                ││
   ▼                                                                ││
8. User Can Resubmit Proof ◄───────────────────────────────────────┘│
   │                         Payment:  WAITING_VERIFICATION          │
   │                         (Back to step 4)                        │
   │                                                                ││
   └──► (Goes back to Admin Verification)                           │
                                                                     │
═════════════════════════════════════════════════════════════════════│═══
                           TIMEOUT PATH (Manual)                     │
═════════════════════════════════════════════════════════════════════│═══

User never submits / Resubmission timeout ◄─────────────────────────┘
   │
   │                         • Cron job detects timeout (24-48 hrs)
   │                         • Update Payment Status
   │                         • Release Inventory
   │                         • Update Order Status
   │                         • Notify User
   │
   ▼                         Order:    CANCELLED
                             Inventory: RELEASED → AVAILABLE
                             Payment:  EXPIRED
```

### Admin Verification Interface Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    ADMIN VERIFICATION FLOW                       │
└─────────────────────────────────────────────────────────────────┘

  ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
  │  View List   │────►│  View Detail │────►│  Take Action │
  │  of Pending  │     │  - Order Info│     │              │
  │  Verifications│     │  - Txn Number│     │  ┌────────┐  │
  └──────────────┘     │  - Proof     │     │  │APPROVE│  │
                       │  - Amount    │     │  └───┬────┘  │
                       │  - Time      │     │      │       │
                       └──────────────┘     │  ┌───┴────┐  │
                                            │  │REJECT │  │
                                            │  └───┬────┘  │
                                            └──────┼───────┘
                                                   │
                              ┌────────────────────┴────────────────────┐
                              │                                         │
                              ▼                                         ▼
                    ┌─────────────────┐                     ┌─────────────────┐
                    │ Fill Reason     │                     │ Add Note        │
                    │ (Required)      │                     │ (Optional)      │
                    └────────┬────────┘                     └────────┬────────┘
                             │                                       │
                             ▼                                       ▼
                    ┌─────────────────┐                     ┌─────────────────┐
                    │ Payment: REJECTED│                    │ Payment: PAID   │
                    │ Inventory: RESERVED│                   │ Inventory: COMMITTED│
                    │ Order: PENDING_  │                     │ Order: CONFIRMED│
                    │   PAYMENT        │                     │                 │
                    └─────────────────┘                     └─────────────────┘
```

### Manual Transfer Summary Table

| Stage | Order Status | Payment Status | Inventory Status | Timeout |
|-------|-------------|----------------|------------------|---------|
| After Checkout | `PENDING_PAYMENT` | `PENDING` | `RESERVED` | 24-48 hrs |
| Proof Submitted | `PENDING_PAYMENT` | `WAITING_VERIFICATION` | `RESERVED` | Timer continues |
| Admin Approved | `CONFIRMED` | `PAID` | `COMMITTED` | - |
| Admin Rejected | `PENDING_PAYMENT` | `REJECTED` | `RESERVED` | Timer resets |
| After Delivery | `COMPLETED` | `PAID` | `COMMITTED` | - |
| Timeout | `CANCELLED` | `EXPIRED` | `RELEASED` | 24-48 hrs |

**Key Points for Manual Transfer:**
- Inventory reserved for **24-48 hours** (configurable)
- Payment status `WAITING_VERIFICATION` when proof submitted
- Admin can **reject** and user can **resubmit** (inventory stays reserved)
- Inventory committed **only after admin approval**
- Timer may reset on rejection (configurable)

---

## Comparison Table

### Complete Status Comparison Across All Payment Methods

| Stage | COD | Stripe | Manual Transfer |
|-------|-----|--------|-----------------|
| **After Checkout** | | | |
| Order Status | `PENDING` | `PENDING_PAYMENT` | `PENDING_PAYMENT` |
| Payment Status | `PENDING` | `PENDING` | `PENDING` |
| Inventory Status | `RESERVED` | `RESERVED` | `RESERVED` |
| **After Payment/Confirmation** | | | |
| Order Status | `PENDING`* | `CONFIRMED` | `CONFIRMED` |
| Payment Status | `PENDING`* | `PAID` | `PAID` |
| Inventory Status | `RESERVED`* | `COMMITTED` | `COMMITTED` |
| **After Delivery** | | | |
| Order Status | `COMPLETED` | `COMPLETED` | `COMPLETED` |
| Payment Status | `PAID` | `PAID` | `PAID` |
| Inventory Status | `COMMITTED` | `COMMITTED` | `COMMITTED` |
| **If Failed/Cancelled** | | | |
| Order Status | `CANCELLED` | `CANCELLED` | `CANCELLED` |
| Payment Status | `CANCELLED` | `FAILED`/`EXPIRED` | `EXPIRED`/`REJECTED` |
| Inventory Status | `RELEASED` | `RELEASED` | `RELEASED` |
| **Reservation Timeout** | None | 30 min | 24-48 hrs |

*\*COD: Payment confirmed and inventory committed only after delivery*

---

## Edge Cases & Error Handling

### 1. Concurrent Orders (Race Condition)

```
┌─────────────────────────────────────────────────────────────────┐
│              CONCURRENT ORDER HANDLING                          │
└─────────────────────────────────────────────────────────────────┘

Product X: 5 items available

User A: Adds 3 to cart
User B: Adds 4 to cart

Scenario 1: User A checks out first
├─ User A: 3 reserved (AVAILABLE: 2)
├─ User B: Checks out → ERROR "Insufficient stock"
└─ Resolution: User B notified, cart updated

Scenario 2: Both check out simultaneously
├─ Database uses optimistic locking / row-level locks
├─ First successful reservation wins
├─ Second gets "Insufficient stock" error
└─ Resolution: Failed user notified, can retry
```

### 2. Payment Received But Webhook Delayed (Stripe)

```
┌─────────────────────────────────────────────────────────────────┐
│              STRIPE WEBHOOK DELAY HANDLING                       │
└─────────────────────────────────────────────────────────────────┘

User pays → Stripe processes → Webhook delayed

Solutions:
├─ 1. Poll Stripe API for payment status (backup)
├─ 2. Show "Verifying payment..." to user
├─ 3. Send email confirmation once webhook received
└─ 4. Cron job to reconcile pending payments
```

### 3. Admin Rejects Multiple Times (Manual)

```
┌─────────────────────────────────────────────────────────────────┐
│              MULTIPLE REJECTION HANDLING                         │
└─────────────────────────────────────────────────────────────────┘

Options (configurable):
├─ Option A: Allow unlimited resubmissions (within timeout)
├─ Option B: Max 3 rejections → Auto-cancel
└─ Option C: Each rejection extends timeout by X hours

Recommended: Option A with clear communication to user
```

### 4. Partial Stock Availability

```
┌─────────────────────────────────────────────────────────────────┐
│              PARTIAL STOCK SCENARIOS                             │
└─────────────────────────────────────────────────────────────────┘

Order: Product A (3) + Product B (2)
Stock: Product A (2) + Product B (5)

Options:
├─ Option A: Reject entire order (simpler)
├─ Option B: Allow partial order (complex)
└─ Recommended: Option A - "Some items out of stock, please modify cart"
```

### 5. Refund Flow (After Completion)

```
┌─────────────────────────────────────────────────────────────────┐
│              REFUND FLOW (All Methods)                           │
└─────────────────────────────────────────────────────────────────┘

1. Admin initiates refund
2. Payment Status: PAID → REFUNDED
3. Inventory Status: COMMITTED → AVAILABLE (restored)
4. Order Status: COMPLETED → REFUNDED

Note: COD refund requires physical cash collection process
      (outside system scope)
```

---

## Database Entity Relationships

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│    INVENTORY    │       │      ORDER      │       │     PAYMENT     │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ id              │       │ id              │       │ id              │
│ product_id      │       │ order_number    │       │ order_id (FK)   │
│ quantity        │◄──────│                 │──────►│ method          │
│ status          │       │ status          │       │ status          │
│ reserved_at     │       │ total_amount    │       │ amount          │
│ reserved_by_    │       │ payment_method  │       │ transaction_no  │
│   order_id (FK) │       │ created_at      │       │ gateway_ref     │
│ committed_at    │       │ updated_at      │       │ proof_image     │
│ expires_at      │       │                 │       │ verified_by     │
│ updated_at      │       │                 │       │ verified_at     │
└─────────────────┘       └─────────────────┘       │ rejection_reason│
                                                    │ created_at      │
                                                    │ updated_at      │
                                                    └─────────────────┘
```

---

## Cron Jobs Required

```
┌─────────────────────────────────────────────────────────────────┐
│                    SCHEDULED JOBS                                │
├───────────────────┬─────────────┬───────────────────────────────┤
│ Job Name          │ Frequency   │ Description                   │
├───────────────────┼─────────────┼───────────────────────────────┤
│ check_stripe_     │ Every 5 min │ Cancel expired Stripe orders  │
│ expiration        │             │ Release inventory             │
├───────────────────┼─────────────┼───────────────────────────────┤
│ check_manual_     │ Every 15 min│ Cancel expired manual transfer│
│ expiration        │             │ orders, release inventory     │
├───────────────────┼─────────────┼───────────────────────────────┤
│ reconcile_stripe  │ Every hour  │ Sync pending payments with    │
│ _payments         │             │ Stripe API (fallback)         │
└───────────────────┴─────────────┴───────────────────────────────┘
```

---

## Quick Reference: Status Transitions

### Order Status Transitions
```
PENDING_PAYMENT ──► CONFIRMED    (payment successful/verified)
PENDING_PAYMENT ──► CANCELLED    (expired/failed/cancelled)
PENDING ──────────► CONFIRMED    (COD: payment received after delivery - rare)
PENDING ──────────► PROCESSING   (COD: being prepared)
PENDING ──────────► CANCELLED    (cancelled before delivery)
CONFIRMED ────────► PROCESSING   (order being prepared)
PROCESSING ───────► DELIVERING   (out for delivery)
DELIVERING ───────► COMPLETED    (delivered successfully)
DELIVERING ───────► CANCELLED    (delivery failed, rare)
```

### Payment Status Transitions
```
PENDING ──────────► PROCESSING           (Stripe: redirected)
PENDING ──────────► WAITING_VERIFICATION (Manual: proof submitted)
PENDING ──────────► PAID                 (COD: after delivery)
PENDING ──────────► EXPIRED              (timeout)
PENDING ──────────► CANCELLED            (user cancelled)
PROCESSING ───────► PAID                 (Stripe: success)
PROCESSING ───────► FAILED               (Stripe: declined)
WAITING_VERIFICATION ► PAID              (Manual: admin approved)
WAITING_VERIFICATION ► REJECTED          (Manual: admin rejected)
REJECTED ────────► WAITING_VERIFICATION  (Manual: user resubmitted)
PAID ─────────────► REFUNDED             (refund initiated)
```

### Inventory Status Transitions
```
AVAILABLE ────────► RESERVED     (order created)
RESERVED ────────► COMMITTED    (payment confirmed)
RESERVED ────────► RELEASED     (payment failed/expired/cancelled)
RELEASED ────────► AVAILABLE    (automatic, same as release)
COMMITTED ───────► AVAILABLE    (only via refund)
```

---

## Summary Decision Matrix

| Question | COD | Stripe | Manual |
|----------|-----|--------|--------|
| When is inventory reserved? | Checkout | Checkout | Checkout |
| When is inventory committed? | After delivery | After payment | After admin approval |
| When does payment become PAID? | After delivery | After payment | After admin approval |
| Is there a timeout? | ❌ No | ✅ 30 min | ✅ 24-48 hrs |
| Who confirms payment? | Delivery person | Automatic (Stripe) | Admin |
| Can payment be rejected? | No | No | Yes |
| Can user retry payment? | N/A | New order | Resubmit proof |

---

*Documentation Version: 1.0*  
*Modules Covered: Inventory, Order, Payment*  
*Shipping Module: Not Included*
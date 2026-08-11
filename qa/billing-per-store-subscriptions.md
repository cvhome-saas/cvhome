# QA — per-store subscriptions on Stripe

Billing moved from one subscription per **org** to one per **store**, on a database-driven plan catalog, with
Stripe doing the charging. This is what to try in order to believe it works — and, just as usefully, the things
that were already broken once and could break again.

- **Scope** — billing · control-plane · gateway · catalog · seller-ui
- **Change** — PR #270, branch `feat/billing-subscription-service`, plan `.claude/plans/billing-subscription-service.md`
- **Cases** — 54
- **Stripe** — test mode only. Never point this at a live key.

Each case is tagged:

- **[verified]** — run during the build and passed.
- **[not verified]** — never run end to end by anyone. These are where a tester is most likely to find
  something, and they are called out rather than buried.

Roughly half the cases are in each bucket. Sections [REG](#reg--regression-watchlist) and
[99](#99--known-gaps) are the highest-value reading: one is defects that have already happened, the other is
behaviour that looks wrong but is expected.

---

## 00 — Before you start

Most cases need the stack, a Stripe test account and a webhook listener. **Without the listener, payments
appear to do nothing** — the money moves at Stripe and nothing reaches us.

```bash
sudo ./extra/scripts/configure-domain.sh        # once per machine

# terminal 1 — prints whsec_...; billing needs it
stripe listen --forward-to http://gateway.com:8000/billing/api/v1/stripe-webhook/public/events

# terminal 2 — stop with SIGTERM, never SIGINT on a backgrounded run
STRIPE_WEBHOOK_SECRET=whsec_... ./extra/scripts/run-lcl.sh
```

**Sign-in.** Seller console `http://gateway.com:8000` — `org1-admin` / `admin`. The console works on one store
at a time; use the store switcher in the header, because every billing answer on the page belongs to the store
selected there.

### Cards worth knowing

| Number | Behaviour | Use it for |
|---|---|---|
| `4242 4242 4242 4242` | Succeeds | The happy path |
| `4000 0000 0000 0341` | Attaches, then fails on charge | Renewal failure, PAST_DUE |
| `4000 0000 0000 0002` | Declined outright | Upgrade refusal — must be 422, not 502 |
| `4000 0025 0000 3155` | Requires 3-D Secure | Authentication interrupting a change |

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select id, org_id, status, plan_id, current_period_end, cancel_at_period_end
     from billing.store_subscription order by updated_date desc limit 10;"

# the audit trail — every state change, and what drove it
... "select event_type, from_status, to_status, source, occurred_at
       from billing.subscription_audit where store_id='<store>' order by occurred_at;"

# events we accepted from Stripe, and what became of them
... "select event_type, outcome, received_at
       from billing.processed_stripe_event order by received_at desc limit 20;"
```

---

## TRL — Trial & provisioning

An org gets one 14-day trial, ever. It goes to whichever store it creates first; every later store starts
unpaid. A store gets its subscription from the outbox shortly after creation, not during the request.

### TRL-01 — A brand-new org's first store gets the trial · critical · [verified]

- **Setup** — sign up a fresh org that has never had a store.
- **Steps** — create a store; wait a few seconds; open **Subscription And Usage → Subscription**.
- **Expect** — status **Trial**, trial end roughly 14 days out. The row shows `TRIALING`, and
  `billing.org_trial_grant` has one row for that org.

### TRL-02 — The same org's second store does not get a trial · critical · [verified]

- **Steps** — with the same org, create a second store and switch the console to it.
- **Expect** — **Not subscribed**, no plan, a prompt to choose one. `org_trial_grant` still has exactly **one**
  row for the org.

### TRL-03 — Two stores created at the same instant still yield one trial · critical · [verified]

The rule is a database primary key rather than a check-then-write, so this is the case that proves it.

- **Steps** — for a fresh org, fire several store creations simultaneously (two browser tabs submitting
  together works; a small script is better).
- **Expect** — exactly one store `TRIALING`, the rest `PENDING`, one `org_trial_grant` row. Never two trials,
  and never an error page for the loser.

### TRL-04 — A store appears in billing even when the pod is down · high · [verified]

Billing and pod provisioning are independent handlers on the same event; neither should block the other.

- **Steps** — stop the `merchant` service, then create a store.
- **Expect** — the store still gets a billing subscription. `control.outbox_record` holds two rows for the one
  `StoreCreatedEvent` — billing's `COMPLETED`, the pod's still pending.

### TRL-05 — Store creation is refused when billing is unreachable · high · [not verified]

Deliberately the opposite of every other billing call: a store nobody is billed for is worse than an error you
can retry.

- **Steps** — stop `billing`, try to create a store.
- **Expect** — creation **fails**, no store row left behind. Restart billing, retry, it works.

### TRL-06 — Stockpiling unpaid stores is refused · [not verified]

- **Steps** — for one org, create unpaid stores until you have four.
- **Expect** — the fourth is refused, citing too many unpaid stores. Limit is
  `com.asrevo.cvhome.billing.quota.max-pending-stores`, default 3. Paying for one should free a slot.

---

## PAY — Checkout & payment

### PAY-01 — An unpaid store can buy a plan · critical · [verified]

- **Steps** — on a **Not subscribed** store press **Subscribe** on Basic; pay with `4242 4242 4242 4242`;
  return and reload.
- **Expect** — **Active** on Basic, renewal a month out. An invoice appears, marked Paid, with a working PDF
  link. Audit shows `ACTIVATED` from `WEBHOOK`.

### PAY-02 — Abandoning the payment page changes nothing · high · [not verified]

Being redirected is not paying. The store may only become active when Stripe says money moved.

- **Steps** — press Subscribe, reach Stripe's page, close the tab without paying, return to the console.
- **Expect** — still **Not subscribed**. No invoice, no plan.

### PAY-03 — A declined card leaves the store unpaid and says so · high · [not verified]

- **Steps** — subscribe using `4000 0000 0000 0002`.
- **Expect** — Stripe refuses on its own page; the store stays **Not subscribed**; retrying with a good card
  succeeds.

### PAY-04 — One org, several stores, one card · [not verified]

Billing details belong to the org; each store pays separately underneath them.

- **Steps** — pay for two stores of the same org, then look at Customers in the Stripe dashboard.
- **Expect** — **one** customer for the org with two subscriptions under it, not two customers. Each
  subscription's metadata names its own store id.

### PAY-05 — Yearly pricing is bought, and billed, yearly · [not verified]

- **Steps** — flip the Month/Year toggle, subscribe to Basic at the yearly price.
- **Expect** — charged the yearly amount, renewal a year out — not a month.

---

## PLN — Plan changes

Moving up is charged now and applies now. Moving down waits until the end of the period already paid for. The
direction is decided by the server from the catalog, never by the screen.

### PLN-01 — Upgrade applies immediately and charges the difference · critical · [verified]

- **Setup** — a store Active on Basic monthly.
- **Steps** — choose Pro and confirm.
- **Expect** — plan reads **Pro** straight away, nothing pending. A new invoice for the prorated difference,
  already Paid. Stripe's subscription is on the Pro price.

### PLN-02 — Downgrade is deferred — the customer keeps what they paid for · critical · [verified]

- **Setup** — a store Active on Pro.
- **Steps** — choose Basic and confirm.
- **Expect** — plan **still reads Pro**, with a note that Basic starts on the renewal date. No charge, no
  refund. Stripe holds a schedule with two phases: Pro until period end, then Basic.

### PLN-03 — A declined upgrade is refused cleanly, not half-applied · critical · [not verified]

The single most valuable payment case: a refusal and an unknown outcome must not look the same.

- **Setup** — an Active store whose card is `4000 0000 0000 0002`.
- **Steps** — attempt an upgrade to Pro.
- **Expect** — an error the seller can act on — **422**, wording about the card, not a gateway failure. The plan
  is **unchanged** in the console *and* in Stripe. Nothing partially moved.

### PLN-04 — A scheduled downgrade actually happens at the boundary · high · [not verified]

The long-horizon case. Best done by moving a Stripe test clock rather than waiting a month.

- **Steps** — schedule a downgrade (PLN-02), then advance the test clock past the period end.
- **Expect** — the plan becomes Basic, the pending note disappears, audit shows `PLAN_DOWNGRADE_APPLIED`. It
  should land exactly once even though both a webhook and a safety-net job watch for it.

### PLN-05 — Monthly → yearly is treated as an upgrade · [not verified]

- **Expect** — applies immediately with a proration charge, not deferred.

### PLN-06 — Choosing the plan you already have does nothing · [not verified]

- **Expect** — the current plan's button is disabled and reads **Current plan**. No charge, no audit row, no
  Stripe call.

### PLN-07 — Changing plan on a store that never paid · high · [not verified]

There is nothing at Stripe to change, so this must route to checkout rather than error.

- **Steps** — on a **Not subscribed** store, pick any plan.
- **Expect** — you reach the payment page, not an error.

---

## LIF — Cancel, resume, lapse

### LIF-01 — Stopping renewal keeps the store working · critical · [verified]

- **Steps** — on an Active store, press **Stop renewal**.
- **Expect** — status **stays Active** (this is not a cancellation) with a note that access continues to period
  end. The store is still editable. Stripe shows `cancel_at_period_end = true`.

### LIF-02 — Resume puts it back · high · [verified]

- **Expect** — the warning goes, the renewal date returns, Stripe agrees (`cancel_at_period_end = false`).
  Audit shows `CANCEL_REVOKED`.

### LIF-03 — Stopping renewal also calls off a pending downgrade · high · [verified]

A downgrade lands at the period boundary — exactly when the subscription would now end — so it cannot happen.

- **Steps** — schedule a downgrade, then press Stop renewal.
- **Expect** — the pending-change note is gone, and Stripe no longer holds a schedule for the subscription.

### LIF-04 — Resuming something that was never stopped · [verified]

- **Expect** — a clear refusal (422) and — the point — **nothing changes at Stripe**. Check Stripe's event log:
  the failed attempt should have sent nothing.

### LIF-05 — A failed renewal moves to past due, not straight to off · critical · [not verified]

A merchant who cannot trade cannot earn the money to pay you. The grace window is the whole point.

- **Setup** — an Active store whose card is `4000 0000 0000 0341`.
- **Steps** — advance a Stripe test clock past the renewal so the invoice fails.
- **Expect** — **Payment failed**, with a date by which it must be fixed. The store is **still fully usable** —
  products editable, orders taken. An unpaid invoice appears in history.

### LIF-06 — Paying a failed invoice restores the store · critical · [not verified]

- **Steps** — from LIF-05, pay the outstanding invoice in Stripe.
- **Expect** — back to **Active**, fresh renewal date, invoice reads Paid. No manual nudge needed.

### LIF-07 — An expired trial suspends the store · high · [not verified]

The job runs every ten minutes, so allow for that rather than expecting it on the stroke of the hour.

- **Steps** — set a trialling store's `trial_end` to the past in the database; wait for `ExpireTrialsJob`.
- **Expect** — **Suspended**, audit row `SUSPENDED` with source `JOB`. Writes refused, reads still work.

### LIF-08 — A suspended store can buy its way back · high · [not verified]

- **Expect** — subscribing returns it to Active, and writing works again within about a minute. The guards cache
  briefly; allow for that before calling it a bug.

---

## HK — Webhooks

Stripe repeats itself, delivers out of order, and retries anything not answered. All three are normal, and all
three have to be harmless.

### HK-01 — The same event twice does nothing twice · critical · [verified]

- **Steps** — take a real event id from a payment, then `stripe events resend evt_...`.
- **Expect** — the log says the delivery was already accepted. The period end does **not** move, no second
  invoice row, no extra audit row.

### HK-02 — A forged webhook is rejected · critical · [verified]

The endpoint is public; the signature is the only thing that makes a payload trustworthy.

- **Steps** — POST a hand-written "payment succeeded" body with no signature, then with a nonsense one.
- **Expect** — **400** both times, nothing changes. A 500 would be wrong — Stripe would retry rubbish for days.

### HK-03 — Events about other people's objects are ignored quietly · high · [verified]

- **Steps** — `stripe trigger customer.subscription.created`, which creates objects unrelated to any store.
- **Expect** — recorded as `IGNORED`, answered 200. No store changes, no retries in the listener.

### HK-04 — An invoice arriving before its subscription is not lost · critical · [verified]

Stripe does not promise ordering. This was a real defect: the invoice was dropped from history for good.

- **Steps** — watch the logs during a first payment. If an invoice event arrives before the subscription is
  linked, billing should *refuse* that delivery rather than shrug it off. Resend it once the store is Active.
- **Expect** — the refusal is visible (Stripe will retry), and after the resend the invoice is in history. It
  must never end up permanently missing.

### HK-05 — Events that arrive while billing is down are not lost · high · [not verified]

- **Steps** — stop billing, pay for a store, start billing, then resend the events or let Stripe's retries run.
- **Expect** — the store ends up Active with its invoice, with no manual repair.

---

## ENF — Enforcement

A store that has not paid cannot be *changed*. It can still be read, and its shopfront still sells — both
deliberate.

### ENF-01 — A lapsed store is refused at the edge · critical · [verified]

- **Setup** — a store Not subscribed or Suspended. Allow a minute; the edge refreshes on a timer.
- **Steps** — in the console for that store, create or edit a product.
- **Expect** — **402 Payment Required** with a message about the subscription — not a permissions error, not a
  404.

### ENF-02 — Reading a lapsed store still works · critical · [verified]

A seller has to see what they are being asked to pay for. This was wrong once and is worth re-checking.

- **Steps** — on the same lapsed store, browse products, orders and settings.
- **Expect** — everything lists normally. Only changes are refused.

### ENF-03 — The shopfront of a lapsed store keeps selling · critical · [not verified]

- **Steps** — open the storefront of a suspended store (`http://org1-store1.spg-507f1f77.gateway.com`) and place
  an order.
- **Expect** — browsing and checkout work. Shoppers are never punished for the merchant's billing.

### ENF-04 — Nothing is blocked while billing is down · high · [verified]

- **Steps** — with everything working, stop billing; then use a paying store normally — list stores, edit a
  product.
- **Expect** — work continues. The store list renders with billing standing shown as unknown rather than as an
  error. An outage must not stop a paying merchant trading.

### ENF-05 — The product ceiling refuses the one that would exceed it · high · [not verified]

> **Expect this to be permissive today.** The catalog guard shipped only partly wired — see
> [Known gaps](#99--known-gaps). If the limit is not enforced, that is the known state, not a new bug. Record
> what you observe either way.

- **Setup** — a store on Free, which allows 25 products.
- **Steps** — create products up to 25, then attempt one more.
- **Expect** — *if wired:* the 26th is refused with a message naming the limit and the current count, and
  existing products stay editable. *If not:* it succeeds — log it against the known gap.

### ENF-06 — Unlimited means unlimited · [not verified]

Pro does not cap products. An absent limit must never behave as a limit of zero.

- **Expect** — on Pro, product creation is never refused for a ceiling, however many exist.

---

## SEC — Isolation & permissions

### SEC-01 — One org cannot see another org's billing · critical · [verified]

The most important case here. Use a store belonging to a *different org* that genuinely has a subscription —
pointing at a store with no data proves nothing.

- **Steps** — signed in as org 1's admin, request org 2's store directly:
  `/billing/api/v1/subscription/current?store=<org2-store>`, then `/billing/api/v1/invoice/list`.
- **Expect** — no plan, no amounts, no invoices — **404**, deliberately indistinguishable from a store that does
  not exist. Anything returning the other org's data is a stop-ship defect.

### SEC-02 — A moderator can look but not spend · critical · [not verified]

Reading and paying are separate rights: spending is the org's money, not the store operator's.

- **Steps** — sign in as a store moderator (not an org admin); open the subscription page; try to change plan,
  stop renewal and resume.
- **Expect** — the page and invoices readable; every money action refused with **403**. Test both halves — a
  missing permission entry fails silently in the "can read" direction too.

### SEC-03 — Platform-wide endpoints are closed to sellers · high · [verified]

- **Steps** — as a signed-in seller, call `/billing/api/v1/entitlement/private/blocked-stores` and
  `/billing/api/v1/quota/private/store-create`.
- **Expect** — **403** for both; these span every org on the platform.

### SEC-04 — The price list is public; everything else is not · [verified]

- **Steps** — signed out, open `/billing/api/v1/plan/public/plans`, then
  `/billing/api/v1/subscription/current?store=…`.
- **Expect** — plans return 200 (a price list has to be readable by people who have not signed up); the
  subscription call is refused.

### SEC-05 — Nothing sensitive is in the logs · high · [not verified]

- **Steps** — after exercising payments, search `build/lcl-logs/billing.log` for `sk_test`, `sk_live`, `whsec_`.
- **Expect** — no matches. The startup line should say a key was decrypted, never what it is.

---

## UI — Seller console

### UI-01 — The page follows the store switcher · high · [verified]

Subscriptions belong to stores now. Switching store must change what the page says.

- **Steps** — with one store paying and another not, switch between them.
- **Expect** — status, plan, renewal date and invoices all change with the store. No stale figures from the
  previous one.

### UI-02 — Every status reads correctly · high · [not verified]

Walk one store through each state and check the wording and colour each time.

| State | The page should say |
|---|---|
| Trial | Trial, with the date it ends |
| Active | Active, with the next renewal date |
| Renewal off | Still Active, plus "will not renew" and the date access ends |
| Downgrade pending | The *old* plan, plus the new one and when it starts |
| Payment failed | Payment failed, with the date it must be fixed by |
| Suspended / not subscribed | A prompt to choose a plan |

### UI-03 — Plan cards compare like for like · [verified]

A more expensive plan showing *fewer* lines than a cheaper one was a real bug here.

- **Expect** — every card lists the same features in the same order, so values line up across columns. Pro shows
  **∞** for products and orders rather than omitting those rows.

### UI-04 — The Month/Year toggle is honest · [verified]

- **Expect** — yearly shows yearly prices. Free, sold monthly only, disappears from the yearly view rather than
  showing a made-up figure.

### UI-05 — Invoices link to Stripe's own documents · [verified]

- **Expect** — each paid invoice has a working PDF link and an amount matching what was charged. A store that
  has never paid shows no invoice section — not an error.

### UI-06 — All five languages, and Arabic right-to-left · high · [not verified]

> **Known to be unchecked.** The Arabic strings are in, but the layout was never reviewed right-to-left. Treat
> this as a real hunt.

- **Steps** — switch through en, ar, es, fr, ru on the subscription page. In Arabic, check the plan cards,
  status chips, alerts and the invoice table.
- **Expect** — no raw keys such as `SUBSCRIPTION.RENEWS_ON` on screen. In Arabic the layout mirrors properly —
  values, chips and the table read right-to-left rather than sitting on the wrong edge.

### UI-07 — The public price list still works · [not verified]

This page moved to a different backend in this change and was only checked by build, never on screen.

- **Steps** — signed out (a private window is easiest), open the public site and find the pricing section.
- **Expect** — plans and prices appear, matching the console exactly, with the free plan shown separately.
  Monthly and yearly both work.

---

## MIG — Migration from the old model

Existing installations carry one subscription per org. The migration fans each org's plan out to every store it
owns, and must run **before** the release that deletes the old code.

> **Order matters.** Deploy the migration first, verify, then deploy the code. The old rows are the only record
> of who was paying, and the code that reads them disappears in this release. Dropping the old tables is a
> separate, deliberate step that is commented out on purpose.

### MIG-01 — Every store of a paying org comes out with a subscription · critical · [verified]

- **Steps** — on a copy of real data, note the org subscriptions and store count per org; run part 1 of
  `extra/migrations/2026-08-10-retire-org-subscriptions.sql`; run the verification queries at the foot of that
  file.
- **Expect** — the "stores with no billing row" query returns **zero**. An org with three stores now has three
  subscriptions.

### MIG-02 — A free plan lands as a trial, not as active · critical · [verified]

This was wrong on the first attempt. Migrated as Active, those stores would sit active forever — no job examines
them, so they would never renew and never be asked to pay.

- **Expect** — old FREE + active rows become `TRIALING` with the original end date preserved as the trial end.
  Old inactive rows become `SUSPENDED`.

### MIG-03 — Nobody is moved to a cheaper plan · high · [not verified]

- **Expect** — LIMITED and BASIC both land on Basic; PERFORMANCE lands on Pro. Spot-check the most expensive
  customers by hand — a migration must never quietly reduce what someone is paying for.

### MIG-04 — Running it twice is safe · high · [verified]

- **Expect** — the second run inserts nothing and changes nothing. Anything billing already knew is untouched.

### MIG-05 — The old endpoints are gone and nothing still calls them · high · [verified]

- **Steps** — call `/control-plane/api/v1/subscription-plan/public/tables` and
  `/control-plane/api/v1/subscription/subscription-plan-details`; then click through the console — org
  management, store management, the public pricing page — watching the browser's network tab for 404s.
- **Expect** — the endpoints are gone, and **no screen calls them**. The second half matters more than the
  first.

---

## REG — Regression watchlist

Every item here was a real defect during this work, found by running the thing rather than reading it. They are
the highest-value re-tests because each has already proven it can happen — and several were invisible from the
screen.

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Every payment event silently dropped** | Payments succeeded at Stripe; the store never became Active. No error anywhere. | Pay for a store and confirm it reaches Active *on its own*, without a resend. Watch the log for "no handler for event type". |
| **Renewal date wrong** | A monthly plan claimed it renewed today. | After any payment, check the renewal date is a full period away — not today's date. |
| **Paid invoice lost forever** | An invoice arriving before its subscription was linked was marked handled and never seen again. | HK-04. Count invoices against Stripe's dashboard after a first payment. |
| **Change applied at Stripe but not locally** | A downgrade left a schedule in Stripe that our records knew nothing about, after an error page. | After *any* failed plan action, compare the console, the database and Stripe. All three must agree. |
| **"Will not renew" stuck on** | After resuming, a late webhook re-applied the cancellation and it never cleared. | Stop renewal, resume, wait a minute, reload. It must still show as renewing. |
| **Cancel refused outright** | Stopping renewal failed with a gateway error while a downgrade was pending. | LIF-03 — schedule a downgrade first, then stop renewal. |
| **Failed action still changed Stripe** | Resuming something that was not stopped errored, but had already altered Stripe. | LIF-04, then check Stripe's event log shows nothing for that attempt. |
| **Reads blocked for lapsed stores** | A seller who stopped paying could not see the catalog they were being asked to pay for. | ENF-02. |
| **Store list 500s** | A multi-store lookup failed and took the console's main screen with it. | Open store management with several stores, and again with billing stopped. Must render either way. |
| **Best plan looked worst** | Pro showed fewer feature lines than Basic, because unlimited items were omitted. | UI-03. |
| **Free stores active forever** | Migrated trials that no job would ever examine. | MIG-02. |
| **Values too long for their column** | Checkout failed with a database error on long provider identifiers. | Exercise checkout, upgrade, downgrade and cancel at least once each — each writes a different shape. |

---

## 99 — Known gaps

Behaviour that is expected today. Please do not spend time raising these — but do shout if you see something
*beyond* what is described.

**Pod-side limits are only partly wired.** The catalog guard is in place but its call to billing was never
confirmed end to end, so plan ceilings are expected to be permissive. Merchant and checkout have no guard at
all. If ENF-05 lets you past the limit, that is this gap.

**A restarted gateway blocks nothing until billing answers.** The edge works from a list refreshed on a timer.
If the gateway starts while billing is down, it holds an empty list and lets everything through until billing
returns. This follows from choosing to fail open.

**A downgrade is not checked against current usage.** Dropping from Pro to Free is allowed even with 400
products. The lower ceiling then applies to new writes; nothing is deleted, and existing data stays readable.
Losing customer data to make a plan fit would be the worse bug.

**Org management still offers a plan picker.** It lists real plans but no longer applies anywhere, because a
plan belongs to a store now. It needs a product decision rather than a fix.

**No automated tests exist for any of this.** None of the suites in the plan were written, so this document is
currently the whole safety net. Everything marked **[not verified]** has never been run by anyone.

---

Raise anything unexpected against PR #270. When reporting, include the store id, the time, and the matching
lines from `build/lcl-logs/billing.log` — most of these paths are asynchronous, so the log is usually the only
place the real cause appears.

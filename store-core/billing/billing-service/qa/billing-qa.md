# QA — billing (`store-core/billing/billing-service`)

Billing owns one subscription **per store** on a database-driven plan catalog, with Stripe doing the charging:
the 14-day trial an org gets once, plan changes, renewals and lapses, the webhooks that carry all of it, and
the blocked-store list the gateway enforces at the edge.

- **Scope** — the subscription lifecycle, the plan catalog and its entitlements, the Stripe webhook receiver,
  the platform-wide admin endpoints, and the migration from the old per-org model
- **Runs on** — `lcl start -d --stack <name>`; read the live port from `lcl urls`. Address it through the
  gateway, never `:8021`
- **Cases** — 40 (22 verified, 0 unit only, 18 not verified)
- **Also see** — [gateway](../../../gateway/gateway-service/qa/gateway-qa.md) (the edge that refuses a lapsed
  store), [catalog](../../../../store-pod/catalog/catalog-service/qa/catalog-qa.md) (the plan ceiling and the
  write gate), [tenancy](../../../tenancy/tenancy-service/qa/tenancy-qa.md) (store creation, which billing
  gates), [console-ui](../../../console-ui/qa/console-ui-qa.md) (the Subscription screen)
- **Stripe** — test mode only. Never point this at a live key.

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[not verified]** — never run end to end by anyone. These are where a tester is most likely to find
  something, and they are called out rather than buried.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading: one is
defects that have already happened, the other is behaviour that looks wrong and is expected.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins, the seeded org/store/pod ids and the `psql`
idiom are in
[`references/qa-testing.md`](../../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5.
Everything below is specific to billing, and **without the webhook listener payments appear to do nothing** —
the money moves at Stripe and nothing reaches us.

```bash
stripe login                                    # once per machine

lcl start -d                                    # `stripe-billing-webhook` is one of the services it starts
lcl logs stripe-billing-webhook -n 5            # its ready line prints this run's whsec_...

# billing verifies against that secret, so hand it over and restart just billing
COM_ASREVO_CVHOME_STRIPE_WEBHOOK_SIGNING_KEY=whsec_... lcl restart billing
```

The listener follows the assigned port map, so it forwards to the right gateway on a shifted stack too. Store
payment events are separate services, one per seeded store — `stripe-org1-store1-webhook` …
`stripe-org2-store2-webhook`.

The console works on one store at a time; use the store switcher in the header, because every billing answer on
the page belongs to the store selected there.

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

Logs: `.lcl/<stack>/logs/billing.log`.

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

### TRL-06 — Stockpiling unpaid stores is refused · [not verified]

- **Steps** — for one org, create unpaid stores until you have four.
- **Expect** — the fourth is refused, citing too many unpaid stores. Limit is
  `com.asrevo.cvhome.billing.quota.max-pending-stores`, default 3. Paying for one should free a slot.

---

> TRL-05 (store creation is refused when billing is unreachable) asserts how **tenancy** behaves and moved to [tenancy-qa.md](../../../tenancy/tenancy-service/qa/tenancy-qa.md).

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

- **Steps** — after exercising payments, search `.lcl/default/logs/billing.log` for `sk_test`, `sk_live`, `whsec_`.
- **Expect** — no matches. The startup line should say a key was decrypted, never what it is.

---

---

## ENF — Where enforcement is asserted

Billing decides who is blocked; it does not do the blocking. The six ENF cases went to the services that
actually refuse the request:

| Case | Now in |
|---|---|
| ENF-01 — a lapsed store is refused at the edge | [gateway-qa.md](../../../gateway/gateway-service/qa/gateway-qa.md) |
| ENF-04 — nothing is blocked while billing is down | gateway-qa.md |
| ENF-02 — reading a lapsed store still works | [catalog-qa.md](../../../../store-pod/catalog/catalog-service/qa/catalog-qa.md) |
| ENF-05 — the product ceiling refuses the one that would exceed it | catalog-qa.md |
| ENF-06 — unlimited means unlimited | catalog-qa.md |
| ENF-03 — the shopfront of a lapsed store keeps selling | [landing-ui-qa.md](../../../../store-pod/landing-ui/qa/landing-ui-qa.md) |

---

## SID — The merged store id, on billing's side

_From `qa/unify-store-id-value-objects.md` §CNV, reformatted into the case shape used everywhere else._

### SID-01 — Billing reads `store_subscription`, whose `@Id` **is** the store id · [verified]

_Was C2._

- **Steps** — provision a store, then `GET /billing/api/v1/subscription/current?store=<new id>`.
- **Expect** — **200**, a TRIALING row, and the clearest single illustration of the wire format:

  ```json
  {"store":"6a7c775e2479528beff8a4c2","status":"TRIALING",
   "planPriceId":{"id":"6a7c754dcd4d53952a3244f1"}}
  ```

  The store id is a bare string; `planPriceId` is still an object. Both are intended.

> The gateway ↔ billing boundary — where the shape change actually crosses a network hop — is asserted from the
> consumer's side, in gateway-qa.md SID-01 and SID-02.

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

> MIG-05 (the old endpoints are gone and nothing still calls them) is checked from the console's network tab and moved to [console-ui-qa.md](../../../console-ui/qa/console-ui-qa.md).

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

Raise anything unexpected against the billing PR. When reporting, include the store id, the time, and the matching
lines from `.lcl/<stack>/logs/billing.log` — most of these paths are asynchronous, so the log is usually the only
place the real cause appears.

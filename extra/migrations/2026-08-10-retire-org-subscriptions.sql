-- Retires control-plane's org-level subscriptions in favour of billing's per-store ones.
--
-- Run this ONCE, against each environment, BEFORE deploying the release that deletes control-plane's subscription
-- code. Ordering matters: the old rows are the only record of who was paying for what, and the code that reads them
-- goes away in that release.
--
-- The two halves are deliberately separate. Part 1 copies data and is safe to re-run — it inserts only what is
-- missing. Part 2 destroys the old tables and is commented out, because a schema drop cannot be undone and should be
-- a decision someone makes deliberately, after confirming part 1 landed.
--
-- WHAT CHANGES SHAPE
--
-- Old: one subscription per org, in subscription.subscription, keyed by the org id, carrying a SubscriptionPlan enum
--      (FREE / LIMITED / BASIC / PERFORMANCE) and a status of ACTIVE or NOT_ACTIVE.
-- New: one subscription per store, in billing.store_subscription, pointing at a row in the billing.plan catalog.
--
-- So an org's single plan fans out to every store it owns. That is a real change in meaning, not a rename: an org on
-- one plan with three stores becomes three subscriptions. Nobody is charged for it here — these rows carry no Stripe
-- subscription, so they renew nothing until the store goes through checkout.

-- ---------------------------------------------------------------------------------------------------------------
-- Part 1 — copy each org's plan onto its stores. Safe to re-run.
-- ---------------------------------------------------------------------------------------------------------------

insert into billing.store_subscription (id, org_id, status, plan_id, plan_price_id, current_period_start,
                                        current_period_end, trial_end, cancel_at_period_end, suspended_at,
                                        created_date, updated_date, version)
select s.id,
       o.id,
       -- A live FREE plan becomes TRIALING, not ACTIVE, and the distinction is load-bearing: the old model expressed
       -- "free until this date" as an ACTIVE FREE plan with an end date, which is exactly what TRIALING means now.
       -- Migrating it as ACTIVE would leave a store no job ever looks at — ExpireTrialsJob only considers TRIALING —
       -- so it would stay active forever, never renewing and never being asked to pay.
       case
           when old.subscription_status <> 'ACTIVE' then 'SUSPENDED'
           when old.subscription_plan = 'FREE' then 'TRIALING'
           else 'ACTIVE'
           end,
       p.id,
       pp.id,
       old.last_renewed_date,
       old.end_date,
       -- The old model had no separate trial: a FREE plan with an end date *was* the trial. Preserved as one so the
       -- store keeps the time it was given rather than being cut off at migration.
       case when old.subscription_plan = 'FREE' and old.subscription_status = 'ACTIVE' then old.end_date end,
       false,
       old.de_activated_date,
       old.created_date,
       now(),
       0
from subscription.subscription old
         join manager.manager_org o on o.id = old.id
         join manager.manager_store s on s.org_id = o.id
    -- The old enum maps onto catalog codes. LIMITED and BASIC both land on BASIC, and PERFORMANCE on PRO: nobody is
    -- moved down, so a customer never loses something they were paying for because of a migration.
         join billing.plan p on p.code = case old.subscription_plan
                                             when 'FREE' then 'FREE'
                                             when 'LIMITED' then 'BASIC'
                                             when 'BASIC' then 'BASIC'
                                             when 'PERFORMANCE' then 'PRO'
    end
    -- Matched on the org's old recurring interval so a yearly subscriber stays yearly. USD because the old model had
    -- no currency of its own; check this against your catalog before running anywhere that sells in another.
         join billing.plan_price pp on pp.plan_id = p.id
    and pp.currency = 'USD'
    and pp.billing_interval = old.recurring_plan
    and pp.active
-- Stores billing already knows are left exactly as they are. Billing is the authority now, and anything it has is
-- newer than what is being migrated.
where not exists (select 1 from billing.store_subscription b where b.id = s.id);

-- Check before going further. Every store of an org that had a subscription should now have one, and no store should
-- have been given a plan the catalog does not sell.
--
--   select count(*) from subscription.subscription;
--   select status, count(*) from billing.store_subscription group by status;
--   select s.id from manager.manager_store s
--     join subscription.subscription old on old.id = s.org_id
--     left join billing.store_subscription b on b.id = s.id
--    where b.id is null;   -- expect zero rows

-- ---------------------------------------------------------------------------------------------------------------
-- Part 2 — drop the old tables. Irreversible. Uncomment and run only once part 1 is confirmed.
-- ---------------------------------------------------------------------------------------------------------------

-- drop table if exists subscription.subscription_price_plan;
-- drop table if exists subscription.subscription;
-- drop schema if exists subscription;

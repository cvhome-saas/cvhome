-- Fixtures for the API integration tests, seeded only under the `test-stores` profile.
--
-- Every row here is PENDING and names no plan, and that is forced rather than chosen: `PlanCatalogSeeder` writes the
-- catalog from `plan-catalog.yml` during start-up, *after* `spring.sql.init` has run, with plan and price ids it
-- mints itself. A fixture that named a plan id would either violate the foreign key or go stale the moment the
-- catalog file changed. The tests that need a paying store therefore arrange it through the repositories, using the
-- real ids the seeder produced.
--
-- Each test class owns its own stores, so one class cancelling a subscription cannot make another class's assertions
-- depend on execution order. Two orgs, so every cross-tenant case has a real neighbour to be refused by rather than
-- an empty one it passes against vacuously.

-- ---------------------------------------------------------------- org A: the tenant under test

-- SubscriptionApiIntegrationTest: read, checkout, plan change.
INSERT INTO billing.store_subscription(id, org_id, status, cancel_at_period_end, created_date, updated_date, version)
VALUES ('b1110000000000000000aa01', '32a034a43cd77581d105c87a', 'PENDING', false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- SubscriptionApiIntegrationTest: cancel and resume, which are terminal enough to need a store of their own.
INSERT INTO billing.store_subscription(id, org_id, status, cancel_at_period_end, created_date, updated_date, version)
VALUES ('b1110000000000000000aa02', '32a034a43cd77581d105c87a', 'PENDING', false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- InvoiceApiIntegrationTest.
INSERT INTO billing.store_subscription(id, org_id, status, cancel_at_period_end, created_date, updated_date, version)
VALUES ('b1110000000000000000aa03', '32a034a43cd77581d105c87a', 'PENDING', false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- StripeWebhookApiIntegrationTest: driven entirely through the public endpoint with signed payloads.
INSERT INTO billing.store_subscription(id, org_id, status, cancel_at_period_end, created_date, updated_date, version)
VALUES ('b1110000000000000000aa04', '32a034a43cd77581d105c87a', 'PENDING', false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- PlatformBillingApiIntegrationTest and BillingStatisticApiIntegrationTest read across every tenant; this store is
-- theirs to mutate.
INSERT INTO billing.store_subscription(id, org_id, status, cancel_at_period_end, created_date, updated_date, version)
VALUES ('b1110000000000000000aa05', '32a034a43cd77581d105c87a', 'PENDING', false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- ExternalEntitlementApiIntegrationTest.
INSERT INTO billing.store_subscription(id, org_id, status, cancel_at_period_end, created_date, updated_date, version)
VALUES ('b1110000000000000000aa06', '32a034a43cd77581d105c87a', 'PENDING', false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- ---------------------------------------------------------------- org B: the neighbour that must stay invisible

-- The store every cross-tenant case aims at. It exists, it has a subscription, and org A must not be able to see it
-- — an isolation test against a store that simply is not there proves nothing.
INSERT INTO billing.store_subscription(id, org_id, status, cancel_at_period_end, created_date, updated_date, version)
VALUES ('b2220000000000000000bb01', '42a034a43cd77581d105c87b', 'PENDING', false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- The neighbour's second store, so a listing that leaked across orgs would show more than one row rather than none.
INSERT INTO billing.store_subscription(id, org_id, status, cancel_at_period_end, created_date, updated_date, version)
VALUES ('b2220000000000000000bb02', '42a034a43cd77581d105c87b', 'PENDING', false, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 0)
ON CONFLICT DO NOTHING;

-- The neighbour org has already spent its trial, so a quota check against it answers differently from org A's.
INSERT INTO billing.org_trial_grant(org_id, store_id, granted_at, trial_end, version)
VALUES ('42a034a43cd77581d105c87b', 'b2220000000000000000bb01', CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP + INTERVAL '14 days', 0)
ON CONFLICT DO NOTHING;

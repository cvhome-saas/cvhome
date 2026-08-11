# QA — renaming `control-plane` to `tenancy` (phase 1)

Phase 1 of `.claude/plans/tenancy-and-pod-registry-split.md`. The service formerly called `control-plane` is
now `tenancy`: Java packages and modules, gateway route and s2s client name, all three config slices, the two
Postgres schemas, and 22 seller-ui call sites hoisted into one exported base constant per lib.

Port is unchanged (8020). Table names are unchanged (`tenancy.manager_org`, `tenancy.manager_store`) and the
`Manager*` type names survive deliberately — see the plan's §0a. `pod-external-api` still uses the old
`controlplane.pod.api` package; it moves wholesale to `pod-registry` in a later phase, so renaming it here
would be churn.

**The two ways this phase fails are worth stating up front, because neither looks like what it is:**

1. A missed `backendServices` entry in `GatewayRouteLocatorImpl` makes `/tenancy/**` return **seller-ui's
   shell HTML** with a 200. It reads as a frontend bug and costs an afternoon.
2. Skipping the migration is **silent**: `schema.sql` runs on every boot, so tenancy happily creates *empty*
   `tenancy.*` tables beside the still-populated `manager.*` ones. Nothing errors; the console just shows no
   orgs and no stores.

## Setup

Order matters — this is the deploy order, and the migration is not safe against a running old instance.

```bash
# 1. stack down (SIGTERM, never SIGINT)
pkill -TERM -f "bash ./extra/scripts/run-lcl.sh"
# 2. migrate, with the service stopped
docker exec -i cvhome-postgres-1 psql -U postgres -d cvhome -v ON_ERROR_STOP=1 \
  < extra/migrations/2026-08-11-rename-control-plane-to-tenancy.sql
# 3. start the new release
./extra/scripts/run-lcl.sh
```

> **Local caveat, or you will waste time:** the compose teardown discards the Postgres data, so a normal local
> run starts from an empty database and the migration is a no-op there — there is no `manager` schema to
> rename. To exercise the migration for real, recreate the pre-rename state first:
> ```bash
> git show origin/develop:store-core/control-plane/control-plane-service/src/main/resources/schema.sql \
>   | docker exec -i cvhome-postgres-1 psql -U postgres -d cvhome -q
> git show origin/develop:store-core/control-plane/control-plane-service/src/main/resources/data.sql \
>   | docker exec -i cvhome-postgres-1 psql -U postgres -d cvhome -q
> ```
> That is how case 1 below was actually run.

Sign in at `http://gateway.com:8000/` as `org1-admin` / `admin`, and as `org2-admin` / `admin` for isolation.

## Cases

### 1. The migration preserves everything — **run, passed**

With the stack down and the pre-rename state in place (2 orgs, 4 stores, 1 pod), plus a hand-inserted
`PENDING` row in `control.outbox_record`, apply the migration.

**Expect:** `\dn` lists `tenancy` and `tenancy_outbox`, and **no** `manager` or `control`. `org` is still
there and untouched — it holds `org.pod`, which moves services in a later phase. Row counts unchanged:
2 orgs, 4 stores, 1 pod, and the pending outbox row still present.

**Result:** exactly that. Both statements are instantaneous catalog-only DDL.

### 2. Nothing recreates the old schemas on boot — **run, passed**

Start the stack against the migrated database.

**Expect:** `run-lcl.sh --list` shows **`tenancy`** on 8020 (not `control-plane`), and after boot `\dn` still
shows no `manager` / `control` schema. The store list must show the **migrated** four stores, not a fresh
seed — if you see the seed data in a `tenancy` schema *and* a populated `manager` schema, the migration was
skipped and you are looking at the silent failure above.

**Result:** passed. `tenancy up on :8020`; the four migrated stores are the ones served.

### 3. `/tenancy/**` returns JSON, not seller-ui's HTML — **run, passed**

The `backendServices` check. Hit any tenancy path through the gateway.

**Expect:** a JSON body. Any HTML shell means the route was added without adding `"tenancy"` to
`backendServices`, so seller-ui's negated catch-all swallowed it.

**Result:** passed — JSON in every case, including error bodies, which come back as RFC-7807 `ProblemDetail`.

### 4. The console works end to end — **run, passed**

As `org1-admin`: store list, user list, store creation.

**Expect:** stores list renders with populated **ID** and **Pod Id** columns; user list renders; creating a
store succeeds and the new row reaches `SUCCESSFULLY_PROVISIONING`.

**Result:** passed. Store creation drove the full asynchronous path on the renamed schema — two outbox records
written to `tenancy_outbox`, both `COMPLETED`: billing provisioning (`TRIALING`) and pod provisioning
(`Successfully created new Store ... in Pod ...`). This is the strongest single piece of evidence in this
phase: it exercises the renamed packages, the renamed outbox schema, the s2s client name and the pod call in
one go.

### 5. Tenant isolation, both directions — **run, passed**

**Expect:** `org1-admin` sees only org1's stores; `org2-admin` sees only `ORG2-STORE1` and `ORG2-STORE2` and
none of org1's. Both directions, because a missing `case` in `CustomPermissionEvaluator` shows up as a silent
403 on the read path too.

**Result:** passed both ways. User list is likewise scoped — `org1-admin` sees only `ORG1-STORE1`'s two users
out of the ten seeded.

### 6. Cross-tenant fetch by id — **attempted, INCONCLUSIVE (not a regression)**

Requesting org1's store id while authenticated as org2.

**Expected** a clean 403. **Got** a 500 from `RequestCacheAwareLocaleInterceptor`
(`UnsupportedOperationException: Cannot change HTTP Accept-Language header`) — the request dies in a shared
interceptor in `store-commons:autoconfigure` **before reaching the controller**, so it says nothing about
authorization either way. No store data was returned.

Two separate things to know:

- **This is a pre-existing bug unrelated to the rename:** any browser-address-bar GET carrying `lang=` against
  any service 500s the same way. Reproducing it properly needs an `.http` request or curl with a session
  cookie rather than a browser navigation, which is how this case should be re-run.
- **The authorization hole it was meant to probe is real and already known.** The plan's §3 records that
  `StoreRoleAccessChecker.isOrgAdmin` ignores its `requestedStoreId` and returns `true` for any store once the
  caller is an org admin, so `hasPermission(#store,'ManagerStoreId',…)` is decorative for org admins today.
  **Phase 3 fixes it and is where this case gets proven** — do not read this phase's pass as evidence that
  cross-tenant access by id is blocked.

### 7. Outbox continuity across the rename — **partially run, see below**

`record_type` and `handler_id` are stored as **fully-qualified Java class names**
(`com.asrevo.cvhome.tenancy.events.store.StoreCreatedEvent`, and the handler FQN plus method signature). This
rename changes those packages, so a row written by the old release names classes that no longer exist. The
migration therefore rewrites them alongside the schema rename.

**What is established:** the FQNs above were read directly from `outbox_record` on a running stack; a `PENDING`
row survives `ALTER SCHEMA` intact; the rewrite produces values byte-identical to what the new release writes;
and freshly generated events process normally after the rename (case 4).

**What is NOT established, honestly:** that an un-rewritten old-FQN row actually fails. Hand-inserted probe
rows are **not picked up by the poller at all** — a control row with untouched, valid FQNs sat `PENDING`
exactly like the old-FQN one — so that experiment cannot tell the two apart. The rewrite is a cheap precaution
justified by inspection, not a fix for an observed failure.

**How to actually prove it**, if someone wants certainty before a production deploy: on the old release,
create a store while the target pod is unreachable so provisioning genuinely fails and the record stays
`PENDING`; then migrate, deploy, and watch whether it completes. The belt-and-braces alternative is in the
migration header — drain the outbox to zero `PENDING` before deploying, and the question never arises.

## Not covered

- **Super-admin screens (orgs, pods) were not exercised** — `org1-admin` has no such menu, and they need the
  `super-admin` account. `pod.service.ts` and `org.service.ts` were both repointed to the new base constants,
  so they are worth a pass before merge.
- **Signup** (`/tenancy/api/v1/user-account/public/create`) was not exercised.
- **The storefront path was not re-checked**, deliberately: `/spg/**` routing comes from `PodClient`, which
  this phase does not touch beyond one log message. Phase 2 rebuilds it and re-proves it.
- `fargate-config.yml` changed in both required places (`eager-load.clients` and `ecs.discovery.service-ports`)
  but **cannot be verified locally** — a miss there works locally and 503s only on AWS.

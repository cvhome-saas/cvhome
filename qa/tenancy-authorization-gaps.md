# QA — tenancy authorization gaps

Phase 3 of `.claude/plans/tenancy-and-pod-registry-split.md`.

## What changed and why you are testing it

An audit of tenancy found **41 endpoint mappings against 21 `@PreAuthorize` annotations**, two of them commented
out. Several endpoints took a store or org id straight from the caller with no gate at all.

Worse, the gate that *is* applied does not hold. `StoreRoleAccessChecker.isOrgAdmin`
(`store-commons/autoconfigure`) ignores the `requestedStoreId` it is handed and returns `true` for any store on
the platform once the caller holds `ROLE_ORG_ADMIN`. Every `hasPermission(#store,'ManagerStoreId',…)` in this
service therefore passes for a store belonging to somebody else.

This PR closes the hole **where tenancy owns the data**: `manager_store.org_id` is tenancy's, so the store reads
are scoped by the caller's org in the query, and the missing annotations are added. Two things follow from that:

- **A foreign store answers 404, not 403.** A 403 confirms the id exists, which lets anyone enumerate the
  platform's stores by probing ids and reading the status code. This is a deliberate deviation from the plan's
  gate wording ("403 on org 2's stores") and should not be "fixed" back.
- **`isOrgAdmin` itself is NOT fixed here.** It is a shared-library change affecting every pod service and gets
  its own PR. Until then **the pods remain permissive** — an org admin can still reach another org's catalog,
  orders and payments. That is the single most important open item after this change.

The third fix is a fail-open in the listing query: it scoped rows only when the caller matched
`isOrgAdminOrAnyStoreAdmin()`, so a principal carrying an org claim but *none* of those roles fell through and
received every store on the platform. Scoping is now driven by whether the identity carries an org at all.

## Setup

> **Do not run this QA under `run-lcl.sh`.** It tears the whole stack down when any one child exits, so you
> cannot restart a single service. This bit twice during this run. Start them independently:

```bash
sudo ./extra/scripts/configure-domain.sh                     # once, ever
docker compose -f docker-compose-lcl.yml up -d               # infra

./gradlew :store-core:uaa:bootRun                     --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:tenancy:tenancy-service:bootRun --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:gateway:gateway-service:bootRun --args='--spring.profiles.active=lcl,test-stores'
```

`test-stores` is required — it seeds the orgs, stores and logins below.

Log in at `http://gateway.com:8000/oauth2/authorization/uaa` as **`org1-admin` / `admin`**. Going straight to an
`/api/**` URL returns 401 without redirecting to login, so visit that path first. Seeded ids:

| | org | store 1 | store 2 |
|---|---|---|---|
| **ORG1** | `21f023932bc66470c104b76f` | `65f023632bc46470c104b76f` | `65f023632bc46470c104b75f` |
| **ORG2** | `352023632b046970c104b76f` | `65f020632bc46470c104b76f` | `65f023632bc26470c104b75f` |

---

## Case 1 — own store is readable

`GET /tenancy/api/v1/store-manager/store-info?store=65f023632bc46470c104b76f`

**Expect:** 200, `name: "ORG1-STORE1"`.

## Case 2 — another org's store is refused (the main event)

`GET /tenancy/api/v1/store-manager/store-info?store=65f020632bc46470c104b76f`

**Expect:** **404** `CONTROL_PLANE.STORE.NOT_FOUND`, detail naming the bare id. **Before this change it
returned 200 with ORG2-STORE1's full record.** A 403 here is a regression, not an improvement — see above.

## Case 3 — the store→pod lookup is refused cross-tenant

`GET /tenancy/api/v1/router/store-pod-by-store-id?store=65f020632bc46470c104b76f`

**Expect:** 404. This endpoint had **no annotation at all**, so any authenticated principal could map any store
id to its pod endpoint.

## Case 4 — the listing is confined to the caller's org

`GET /tenancy/api/v1/store-manager/private/store?size=50`

**Expect:** exactly **2** stores, both ORG1, out of the 4 in the database. This endpoint's `@PreAuthorize` was
commented out.

## Case 5 — super-admin-only endpoints refuse an org admin

As `org1-admin`:

- `GET /tenancy/api/v1/org-manager/stores?id=352023632b046970c104b76f` → **403**. It was the only method in that
  super-admin controller with no annotation, and it takes an arbitrary org id.
- `GET /tenancy/api/v1/pod/{id}` → 403 (only unannotated read on `PodController`).
- `POST /tenancy/api/v2/private/org-statistic` and `/store-statistic` → 403. Platform-wide business metrics
  across every tenant.

## Case 6 — nothing is open to an anonymous caller

Without a session, every path above returns **401**.

## Case 7 — a moderator can read but not manage

Sign in as a `ROLE_STORE_MODERATOR` for ORG1-STORE1: `store-info` → 200, store create → 403. Tests both
directions, since a missing `case` in `CustomPermissionEvaluator` shows up as a silent 403 on the read path too.

**Status: NOT RUN** — no moderator login is seeded in `test-stores`; only `org1-admin` / `org2-admin` exist.
Creating one is worth doing before the `isOrgAdmin` PR, which is where that distinction actually starts to bite.

---

## Results

Run 2026-08-11, branch `fix/tenancy-authorization-gaps`, logged in as `org1-admin` through the gateway against
`uaa + tenancy + gateway` started independently.

| Case | Result | Evidence |
|---|---|---|
| 1 — own store | **PASS** | 200, `ORG1-STORE1` |
| 2 — foreign store | **PASS** | 404 `CONTROL_PLANE.STORE.NOT_FOUND`, `detail: "No store is visible with id 65f020632bc46470c104b76f."` |
| 3 — router cross-tenant | **PASS** | 404, same code |
| 4 — listing scoped | **PASS** | `totalElements: 2`, both ORG1, with 4 stores seeded |
| 5 — super-admin only | **PARTIAL PASS** | `org-manager/stores` → 403 `COMMON.ACCESS_DENIED` confirmed as org1-admin. The pod read and the two POST statistics endpoints were **verified unauthenticated (401) and by annotation only**, not executed as an authenticated org admin |
| 6 — anonymous | **PASS** | 401 on all five paths probed |
| 7 — moderator | **NOT RUN** | no moderator seeded |

A first run showed `detail: "No store is visible with id ManagerStoreId[id=65f0…]."` — the value object's record
`toString()` leaking into user-facing text. Fixed, tenancy restarted, re-verified as the table above records.

Automated coverage:
`./gradlew :store-core:tenancy:tenancy-service:test --tests '*StoreTenantScopingTest*'` — **7 tests, 0
failures**, covering the fail-open scoping branch, the foreign-store refusal, the missing-store 404 and the
platform-wide (super-admin / `store_core`) bypass. `checkstyleMain`/`checkstyleTest`, the module `build` and a
full `./gradlew build -x test -x check` are all clean.

## Still open after this PR

1. **`StoreRoleAccessChecker.isOrgAdmin` is unchanged**, so every pod service (catalog, checkout, payment, cua,
   merchant, content) still lets an org admin manage any store on the platform. Next PR.
2. **Case 7 has no seeded moderator.**
3. `StoreManagerServiceImpl.findAll` still wraps its per-row pod call in `catch (Exception) { return null; }`,
   which will now swallow `StoreNotFoundException` too. Rows are already org-scoped before that call, so it is
   not an isolation hole — but it is why a store silently vanishes from the list when its pod is down. Phase 9.

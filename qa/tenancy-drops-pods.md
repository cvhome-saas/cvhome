# QA — tenancy stops owning pods

Phase 7 of `.claude/plans/tenancy-and-pod-registry-split.md`. The split finishes here: there is one pod
registry again, and it is pod-registry.

## What changed and why you are testing it

Since phase 5 the gateway has routed from pod-registry, but tenancy still had its own `org.pod` table and its
own `/tenancy/api/v1/pod` API. Two registries, one authoritative — an operator editing pods through tenancy
was changing a table nothing routed from. That ends here:

- The six `tenancy/org/**` classes are deleted (`PodEntity`, `PodRepository`, `PodService{,Impl}`,
  `PodController`, `PodDatabaseInitializer`), along with tenancy's `PodNotFoundException` and the now-dead
  `TenancyErrors.POD_NOT_FOUND`.
- `org.pod` and the `org` schema are gone from `schema.sql`, and the pod row from `data.sql`.
- `RouterController` — which still has to answer *which pod hosts this store* — resolves through a new
  **`CachingPodDirectory`** instead of a local table. Tenancy keeps the store→pod binding
  (`manager_store.pod_id`); the registry owns what that pod is.
- seller-ui's `POD_API_BASE` moves to `/pod-registry/api/v1/pod`. One line, because phase 1 hoisted it.

Two things worth knowing before you test:

**The directory fails open**, unlike the placement client added in phase 6 which fails closed. That asymmetry
is deliberate and lives in the same service: placement decides where a store *will* live and must refuse
rather than guess, while this only answers where one *already* lives — a stale endpoint beats a 502 on the
screen a seller uses to reach their own store.

**A paged `GET /api/v1/pod` was added to `PodApi`.** Phase 4 only implemented `list` and dropped the paged
root read the old `PodController` had, which seller-ui's pod table binds to. That was a latent regression this
phase would otherwise have shipped.

## Migration — read this before deploying

`extra/migrations/2026-08-12-move-pods-to-pod-registry.sql`.

Phase 4 deliberately seeded pod-registry from `ServiceDomainProperties` rather than a `data.sql` copy. That
gets you every pod's id, name and endpoint — but **not `org_id`**, the private-pod assignment, which only ever
existed in the database, and not any pod an operator created through the old API.

Skipping the migration fails silently in the worst possible way: a formerly-private pod comes back `PUBLIC`
with no owner, and placement starts putting other organizations' stores on dedicated infrastructure — exactly
the bug phase 6 exists to remove. Part 1 (copy) is idempotent; part 2 (`DROP SCHEMA org CASCADE`) is commented
out and destructive, to be run only after the verification query.

## Setup

```bash
docker compose -f docker-compose-lcl.yml up -d
# uaa, billing, pod-registry, tenancy, gateway — independently, not under run-lcl.sh
```

Log in at `http://gateway.com:8000/oauth2/authorization/uaa` as `org1-admin` / `admin`.

---

## Case 1 — tenancy no longer mentions pods

```bash
grep -rn "org\.pod\|tenancy\.org" store-core/tenancy | grep -v /build/
```

**Expect:** nothing. Also confirm the `org` schema is not created: after a fresh start,
`select nspname from pg_namespace` should show `tenancy` and `pod_registry` but no `org`.

## Case 2 — the router still answers, now via the registry

`GET /tenancy/api/v1/router/store-pod-by-store-id?store=65f023632bc46470c104b76f`

**Expect:** 200 with `pod-507f1f77` and its endpoint. Tenancy read `manager_store.pod_id` and resolved the
rest from pod-registry.

## Case 3 — the router still refuses another org's store

Same endpoint with `store=65f020632bc46470c104b76f` (ORG2's) → **404**. Phase 3's query-level scoping is
untouched by the move.

## Case 4 — the pod screen's paged read works

`GET /pod-registry/api/v1/pod?size=10` → 200 with a page. As `org1-admin` the content is **empty**: org1 owns
no private pods and the seeded pod is shared. A super admin sees it.

## Case 5 — the old tenancy pod API is gone

`GET /tenancy/api/v1/pod/list` → **404**.

## Case 6 — a registry outage does not take the router down

1. Stop pod-registry.
2. Wait **>60s**, past `POD_DIRECTORY_TTL`, so a refresh is actually attempted and fails.
3. Re-run case 2.

**Expect:** still **200**, and exactly one `Could not refresh the pod directory; keeping N known pod(s)` WARN
in tenancy's log. Both halves matter — a 200 without the warning would only prove the cache had not expired
yet, which is not the same as degrading.

## Case 7 — seller-ui builds and points at the new service

```bash
cd store-core/seller-ui && npm run build
grep -rn "tenancy/api/v1/pod" projects/ src/   # expect nothing
```

## Case 8 — the super-admin pod screens in the browser

Create, rename and delete a pod through the console as `super-admin`.

**Status: NOT RUN.** Exercised at the API level (cases 4 and 5, plus phase 4's `pod-api.http`), not through
the Angular screens. Worth doing before merge, because `getPod(id)` now reaches an endpoint that returns
`PodView` and is **super-admin only** where tenancy's equivalent also admitted org admins — an org admin
opening a pod detail page will now get a 403 rather than a body. That is intentional (lifecycle, capacity and
health are operator data) but it is a user-visible change nobody has looked at.

---

## Results

Run 2026-08-12, branch `feat/tenancy-drops-pods`, against `uaa + billing + pod-registry + tenancy + gateway`.

| Case | Result | Evidence |
|---|---|---|
| 1 — no pod references | **PASS** | grep clean; schemas are `tenancy`, `pod_registry` — no `org` |
| 2 — router via registry | **PASS** | 200, `pod-507f1f77`, `http://spg-507f1f77.gateway.com` |
| 3 — cross-tenant refused | **PASS** | 404 |
| 4 — paged pod read | **PASS** | 200, correctly empty for org1-admin |
| 5 — old API gone | **PASS** | 404 |
| 6 — fail open | **PASS** | registry stopped, TTL expired, router **200** with exactly 1 refresh-failure WARN |
| 7 — seller-ui | **PASS** | build output produced; no `tenancy/api/v1/pod` left |
| 8 — pod screens in browser | **NOT RUN** | see above — includes a real behaviour change for org admins |

Automated: `CachingPodDirectoryTest` — **6 tests, 0 failures** (seed answers during an outage, a fetch replaces
the seed, a later outage keeps last-known-good rather than falling back to the seed, TTL honoured, unknown pod
is empty, null id short-circuits). Full `build -x test -x check`, module builds and checkstyle clean.

The locale-interceptor defect recorded in `pod-placement-cutover.md` did **not** fire during this run — GET and
the login flow both worked. It is intermittent, which is worth knowing when reproducing it.

## Still open after this PR

1. **Phase 8** — health, capacity, drain and audit. `capacity_stores` is still 0 for every pod, so placement's
   capacity ceiling and least-loaded tie-break are exercised only by unit tests.
2. **Case 8**, above.
3. **`PodApi.delete` still orphans stores.** There is no foreign key from `manager_store.pod_id` and no check
   for placed stores — deleting a populated pod silently strands every store on it. Drain in phase 8 is the
   safe operation; until then it is a sharp tool, and only a super admin holds it.

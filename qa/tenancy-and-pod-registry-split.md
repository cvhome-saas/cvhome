# QA — splitting control-plane into tenancy and pod-registry

`control-plane` became two services: **tenancy** (orgs, stores, users, signup, provisioning) on :8020, and
**pod-registry** (which pods exist, who owns them, what state they are in) on :8022. Along the way the audit's
defects were fixed — a platform-wide storefront outage risk at the gateway, a cross-tenant pod leak, and a set
of robustness holes in provisioning. This is what to try in order to believe it works, and the things that were
already broken once and could break again.

- **Scope** — tenancy · pod-registry · gateway · merchant · seller-ui · store-commons/autoconfigure
- **Change** — PR #271, branch `refactor/tenancy-conventions`, plan `.claude/plans/tenancy-and-pod-registry-split.md`
- **Cases** — 75
- **Migrations** — two, in order, one destructive step held back. See [MIG](#mig--migration).

Each case is tagged:

- **[verified]** — run against a running stack during the build and passed.
- **[unit only]** — the branch is covered by a named test, but nobody has driven it through the stack. The test
  is named so you can judge whether that is good enough.
- **[not verified]** — never run end to end by anyone. These are where a tester is most likely to find
  something, and they are called out rather than buried.

**62 verified, 4 unit only, 9 not verified.** The thirteen that are not [verified] are worth reading first.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading: one is
defects that have already happened, the other is behaviour that looks wrong but is expected. **The single
largest known gap is [`isOrgAdmin`](#99--known-gaps) — the pods are still permissive across tenants.**

---

## 00 — Before you start

### Do not run this QA under `run-lcl.sh`

`run-lcl.sh` supervises its children and **tears the whole stack down when any one of them exits**. Most cases
here stop a single service, so under the script the gateway dies with it and the case "fails" for a reason that
has nothing to do with the code. This cost time twice during the build. Start the services independently:

```bash
sudo ./extra/scripts/configure-domain.sh              # once per machine
docker compose -f docker-compose-lcl.yml up -d        # infra, including the spg Caddy edge

# each in its own shell, so they can be stopped individually
./gradlew :store-core:uaa:bootRun                                --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:billing:billing-service:bootRun            --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:pod-registry:pod-registry-service:bootRun  --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:tenancy:tenancy-service:bootRun            --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:gateway:gateway-service:bootRun            --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-pod:merchant:merchant-service:bootRun           --args='--spring.profiles.active=lcl,test-stores'
```

Both profiles are needed: `lcl` is the environment slice, `test-stores` seeds the orgs, stores and logins.
Stop a service with **SIGTERM** (`kill <pid>`), never `kill -9` and never Ctrl-C on a backgrounded run.

> When killing one service by port, use `lsof -ti :8020 -sTCP:LISTEN`. Plain `lsof -ti :8020` also returns the
> **gateway's** pid, because it holds a client connection — killing that list kills the gateway too.

### Signing in

`http://gateway.com:8000/oauth2/authorization/uaa` — `super-admin` / `admin`, `org1-admin` / `admin`,
`org2-admin` / `admin`. Go to that path first: hitting an `/api/**` URL directly returns 401 without
redirecting to a login page.

> The gateway holds sessions **in memory**. Restarting it logs you out, and the symptom is a 401 where you
> expected a 403. The login form also does not submit from a synthetic click in some tools — use
> `document.querySelector('form').requestSubmit()`.

### Seeded ids

| | org | store 1 | store 2 |
|---|---|---|---|
| **ORG1** | `21f023932bc66470c104b76f` | `65f023632bc46470c104b76f` | `65f023632bc46470c104b75f` |
| **ORG2** | `352023632b046970c104b76f` | `65f020632bc46470c104b76f` | `65f023632bc26470c104b75f` |

The shared pod is `507f1f77bcf86cd799439011`, seeded from `store-core-lcl-config.yml` by `PodSeedInitializer`.
Only that one exists, so several cases need a second pod, or a private one:

```sql
insert into pod_registry.pod (id,name,endpoint,endpoint_type,org_id,visibility,lifecycle_state,capacity_stores,version)
values ('907f1f77bcf86cd799439099','pod-org2-private','http://spg-org2.gateway.com','EXTERNAL',
        '352023632b046970c104b76f','PRIVATE','ACTIVE',0,1);

insert into pod_registry.pod (id,name,endpoint,endpoint_type,org_id,visibility,lifecycle_state,capacity_stores,version)
values ('807f1f77bcf86cd799439088','pod-second','http://spg-507f1f77.gateway.com','EXTERNAL',
        null,'PUBLIC','ACTIVE',0,1);
```

### A service-to-service token

Placement and capacity are s2s endpoints — they need client credentials, **not** a session:

```bash
TOK=$(curl -s -u 'store-core@service.store-core.internal:<the shared lcl secret>' \
  -d 'grant_type=client_credentials&scope=store_core' \
  http://uaa.gateway.com:8001/oauth2/token | jq -r .access_token)
```

### Billing's quota will stop you after a few stores

Every test store is provisioned unpaid and `max-pending-stores` is 3, after which creation answers 422
`BILLING.QUOTA.STORE_EXCEEDED`. Between runs:

```sql
delete from billing.store_subscription where id in (select id from tenancy.manager_store where name like 'TEST%');
delete from pod_registry.pod_store_placement;
delete from tenancy.manager_store where name like 'TEST%';
```

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select id, name, org_id, pod_id, status, provisioning_state from tenancy.manager_store;"

# the registry's operational columns
... "select id, name, visibility, lifecycle_state, capacity_stores, last_health_status from pod_registry.pod;"

# who changed what, in both services
... "select entity_type, entity_id, action, from_state, to_state, actor, source, recorded_at
       from tenancy.tenancy_audit order by recorded_at desc limit 20;"
... "select pod_id, action, from_state, to_state, actor, source from pod_registry.pod_audit order by recorded_at desc;"
```

---

## RNM — Rename and cleanup

Phases 0–1. `control-plane` is now `tenancy`: packages, modules, gateway route, s2s client name, all three
config slices, both Postgres schemas, and 22 seller-ui call sites hoisted into one base constant per lib. The
port is unchanged; table names (`tenancy.manager_org`, `manager_store`) and the `Manager*` type names survive
deliberately.

**Two ways this fails, neither of which looks like what it is:**

1. A missed `backendServices` entry in `GatewayRouteLocatorImpl` makes `/tenancy/**` return **seller-ui's shell
   HTML** with a 200. It reads as a frontend bug and costs an afternoon.
2. Skipping the migration is **silent**. `schema.sql` runs on every boot, so tenancy cheerfully creates *empty*
   `tenancy.*` tables beside the still-populated `manager.*` ones. Nothing errors; the console just shows no
   orgs and no stores.

### RNM-01 — Ids still resolve after the converter de-dupe · critical · [verified]

`JdbcConfig` registered `Identifier -> String` and `String -> ManagerOrgId` twice, verbatim. Removing the
duplicate is safe only if Spring Data JDBC was not relying on registration order.

- **Steps** — as `org1-admin`, open Store management → Stores.
- **Expect** — two rows with **populated ID and Pod Id columns**. Those columns are the converted value objects;
  blank ids, an empty table or a 500 means the removal was not safe. The header's store selector reads
  `ORG1-STORE1`, from the same call.

### RNM-02 — The migration preserves everything · critical · [verified]

- **Setup** — stack **down**; the pre-rename state in place. Locally the compose teardown discards the database,
  so recreate it first, which is how this was actually run:
  ```bash
  git show origin/develop:store-core/control-plane/control-plane-service/src/main/resources/schema.sql \
    | docker exec -i cvhome-postgres-1 psql -U postgres -d cvhome -q
  ```
- **Steps** — apply `extra/migrations/2026-08-11-rename-control-plane-to-tenancy.sql`.
- **Expect** — `\dn` lists `tenancy` and `tenancy_outbox` and **no** `manager` or `control`. `org` is still there
  and untouched — it holds `org.pod`, which moves in phase 7. Row counts unchanged: 2 orgs, 4 stores, 1 pod, and
  a pending outbox row still pending.
- **Seen** — exactly that; both statements are instantaneous catalog-only DDL.

### RNM-03 — Nothing recreates the old schemas on boot · critical · [verified]

- **Steps** — start the stack against the migrated database.
- **Expect** — `run-lcl.sh --list` shows **tenancy** on 8020, and `\dn` still shows no `manager` / `control`.
  The store list serves the **migrated** four stores. Seed data in `tenancy` *plus* a populated `manager` schema
  means the migration was skipped — that is the silent failure above.

### RNM-04 — `/tenancy/**` returns JSON, not seller-ui's HTML · critical · [verified]

- **Expect** — a JSON body from any tenancy path through the gateway, error bodies included (RFC-7807
  `ProblemDetail`). Any HTML shell means the route was added without adding `"tenancy"` to `backendServices`.

### RNM-05 — The console works end to end · critical · [verified]

- **Steps** — as `org1-admin`: store list, user list, create a store.
- **Expect** — all three work and the new store reaches `SUCCESSFULLY_PROVISIONING`.
- **Seen** — the strongest single piece of evidence in the rename: two outbox records written to
  `tenancy_outbox`, both `COMPLETED` (billing → `TRIALING`, pod → created), exercising the renamed packages, the
  renamed outbox schema, the s2s client name and the pod call at once.

### RNM-06 — Outbox continuity across the rename · high · [not verified]

`record_type` and `handler_id` hold **fully-qualified Java class names**, so a row written by the old release
names classes that no longer exist. The migration rewrites them.

- **Expect** — a `PENDING` row written before the rename completes after it.
- **What is established** — the FQNs were read from a running `outbox_record`; a `PENDING` row survives
  `ALTER SCHEMA` intact; the rewrite produces values byte-identical to what the new release writes; and fresh
  events process normally (RNM-05).
- **What is NOT** — that an *un-rewritten* row actually fails. Hand-inserted probe rows are not picked up by the
  poller at all — a control row with valid FQNs sat `PENDING` exactly like the old-FQN one — so that experiment
  cannot tell them apart. The rewrite is a precaution justified by inspection, not a fix for an observed
  failure. To prove it: on the old release, create a store with the pod unreachable so the record stays
  `PENDING`, then migrate and deploy. Or simply drain the outbox first, per [MIG-05](#mig--migration).

---

## SEC — Isolation and permissions

Phase 3. The audit found **41 endpoint mappings against 21 `@PreAuthorize` annotations**, two of them commented
out, and several endpoints taking a store or org id straight from the caller with no gate at all.

**A foreign store answers 404, not 403.** A 403 confirms the id exists, which turns id-probing into store
enumeration. This is a deliberate deviation from the plan's wording and should not be "fixed" back.

**`isOrgAdmin` itself is not fixed here** — see [99](#99--known-gaps). Tenancy closes the hole at the query
layer, because `manager_store.org_id` is tenancy's own data.

### SEC-01 — Your own store is readable · [verified]

`GET /tenancy/api/v1/store-manager/store-info?store=65f023632bc46470c104b76f` → 200, `ORG1-STORE1`.

### SEC-02 — Another org's store is refused · critical · [verified]

The main event.

- **Steps** — as `org1-admin`, request ORG2's store: `…/store-info?store=65f020632bc46470c104b76f`.
- **Expect** — **404** `CONTROL_PLANE.STORE.NOT_FOUND`, detail naming the bare id. **Before this change it
  returned 200 with ORG2-STORE1's full record.** A 403 here is a regression, not an improvement.

### SEC-03 — The store→pod lookup is refused cross-tenant · critical · [verified]

- **Steps** — `GET /tenancy/api/v1/router/store-pod-by-store-id?store=<ORG2 store>`.
- **Expect** — 404. This endpoint had **no annotation at all**, so any authenticated principal could map any
  store id to its pod endpoint.

### SEC-04 — The listing is confined to the caller's org · critical · [verified]

- **Steps** — `GET /tenancy/api/v1/store-manager/private/store?size=50`.
- **Expect** — exactly **2** stores, both ORG1, out of the 4 seeded. This endpoint's `@PreAuthorize` was
  commented out, and its scoping additionally failed open: it filtered only when the caller matched
  `isOrgAdminOrAnyStoreAdmin()`, so a principal carrying an org claim but neither role received **every store on
  the platform**. Scoping is now driven by whether the identity carries an org at all.

### SEC-05 — Super-admin-only endpoints refuse an org admin · high · [verified]

As `org1-admin`, each must be **403**:

| Request | Note |
|---|---|
| `GET /tenancy/api/v1/org-manager/stores?id=<ORG2>` | the only unannotated method in a super-admin controller, and it takes an arbitrary org id |
| `POST /tenancy/api/v2/private/org-statistic` and `/store-statistic` | platform-wide business metrics across every tenant |

- **Seen** — `org-manager/stores` → 403 `COMMON.ACCESS_DENIED` as an authenticated org admin. **The two
  statistics endpoints were verified unauthenticated (401) and by annotation only**, not executed as an org
  admin. Worth completing.

### SEC-06 — Nothing is open to an anonymous caller · [verified]

Without a session every path above returns **401**. Five probed.

### SEC-07 — A moderator can read but not manage · high · [not verified]

Reading and managing are separate rights, and a missing `case` in `CustomPermissionEvaluator` fails silently in
the *read* direction too — so test both halves.

- **Steps** — as a `ROLE_STORE_MODERATOR` for ORG1-STORE1: `store-info`, then store create.
- **Expect** — 200 then 403.
- **Blocked by** — no moderator login is seeded in `test-stores`; only `org1-admin` / `org2-admin` exist.
  Creating one is worth doing before the `isOrgAdmin` PR, which is where the distinction starts to bite.

---

## GWR — Gateway pod routes

Phases 2 and 5. `PodClient` builds one gateway route per pod so `/spg/**?store=…&pod=…` reaches the pod hosting
that store.

**Before:** `getRouteDefinitions()` — which `CachingRouteLocator` calls *while rebuilding its route table* — made
a live HTTP call and returned `Mono.empty()` on any error, while `refreshRoutes()` published a
`RefreshRoutesEvent` unconditionally every minute. A control-plane restart therefore rebuilt the table from an
empty result: **every tenant storefront 404s within one refresh period.** A full multi-tenant outage caused by an
unrelated service restarting.

**After:** routes are fetched on a schedule into `lastKnownGood`; lookup never does I/O. A failed refresh logs
and leaves the table intact. `RefreshRoutesEvent` is published only when the route set actually changes.
`@PostConstruct` seeds from `ServiceDomainProperties.pods()`. `PodRoutesHealthIndicator` exposes staleness,
because a gateway serving indefinitely stale routes would otherwise look perfectly healthy.

**The distinction that matters:** an *empty response* is a real answer and IS applied — a pod really was removed.
A *failure to get an answer* is not. [GWR-04](#gwr-04--an-empty-answer-is-still-an-answer--critical--unit-only)
separates them, and collapsing the two is how someone reintroduces this bug.

Phase 5 then repointed the source from tenancy to pod-registry — which is the whole point of the split. Tenancy
is the busiest service in store-core and the most often redeployed; pod-registry holds one table that changes
when infrastructure changes, which is to say almost never.

### GWR-01 — Routes are present when the source is up · [verified]

```bash
curl -s localhost:8000/actuator/gateway/routes | jq -r '.[].route_id' | grep '^pod-'
```

**Expect** — at least `pod-507f1f77`.

### GWR-02 — The route table survives an outage of the route source · critical · [verified]

The regression that motivated all of this.

- **Steps** — confirm GWR-01; SIGTERM the route source; wait **>70s** (one `route-refresh-rate` period, `PT1M`)
  so at least one refresh has failed; re-run GWR-01.
- **Expect** — `pod-507f1f77` is **still listed**, and the gateway logs the failure at ERROR each period. The
  outage must be *visible*, just not *fatal*.
- **Seen** — after **134s** with tenancy SIGTERMed and two failed refreshes logged, the route was still serving.
  Pre-fix the table emptied at the 60s mark. Repeated against pod-registry after the cutover.

### GWR-03 — Staleness is observable, and being briefly stale is not "down" · high · [verified]

```bash
curl -s localhost:8000/actuator/health | jq '.components.podRoutes'
```

- **Expect** — with the source down: `status: "UP"`, `secondsSinceLastRefresh` climbing, `routes: 1`. Staleness
  under `route-staleness-threshold` (`PT10M`) is by design. Past it → `status: "DOWN"` with `routes` still 1:
  the gateway keeps serving while *reporting* that it is flying blind. Both halves matter — serving is the
  availability win, reporting is what stops the outage being silent.
- **Seen** — UP with `secondsSinceLastRefresh` climbing 48 → 134. The **>10 min → DOWN** variant was not waited
  out; the DOWN path itself is proven by GWR-06.
- If `.components.podRoutes` is absent, set `management.endpoint.health.show-details=always`.

### GWR-04 — An empty answer is still an answer · critical · [unit only]

- **Steps** — with the stack up, `delete from pod_registry.pod where id = '507f1f77bcf86cd799439011';` and wait
  >70s.
- **Expect** — `pod-507f1f77` **disappears**. A successful empty response is applied. Restart pod-registry to
  re-seed and confirm the route returns.
- **Covered by** — `PodRouteResilienceTest.emptyResponseIsAppliedButOnlyWhenItIsTheRealAnswer`.

### GWR-05 — Recovery · [verified]

- **Expect** — after restarting the source, `secondsSinceLastRefresh` resets to near zero and health returns to
  UP. `Pod routes changed; publishing refresh` appears **only if the set actually changed** — a steady state
  should be quiet.
- **Seen** — reset 134 → 50, health UP, route intact. And the notable one: that publish line was logged **zero**
  times across the whole run, because the pod set never changed. Pre-fix this published every 60s, making
  `CachingRouteLocator` discard and rebuild its entire table each time for nothing.

### GWR-06 — Cold start with the registry down · high · [verified]

- **Steps** — stop both; start **only** the gateway.
- **Expect** — `pod-507f1f77` present anyway, seeded from config, and health **DOWN** with
  `reason: "no successful pod route refresh yet"`. Note this makes `/actuator/health` return **503** for the
  first minute after start-up, which is expected.
- **Expected to fail, by design** — a pod created *since* the config was written is missing until the first
  successful refresh. The config seed is a mitigation, not a cure; see [99](#99--known-gaps).

### GWR-07 — The refresh succeeds with tenancy not running at all · critical · [verified]

The cutover proof.

- **Setup** — start uaa + pod-registry + gateway, deliberately **without tenancy**.
- **Expect** — after one refresh period, health `UP`, `routes: 1` — while port 8020 has nothing listening on it.
- **Seen** — `secondsSinceLastRefresh: 52`, port 8020 empty.

### GWR-08 — The route table names the registry · [verified]

**Expect** — `pod-507f1f77 -> http://spg-507f1f77.gateway.com:80`, plus the static backend routes including
`lb://pod-registry`. The `lb://tenancy` route is still listed — tenancy still serves its own API, it is simply
no longer the route *source*.

### GWR-09 — Tenant traffic actually routes · critical · [verified]

```bash
curl -s -o /dev/null -w '%{http_code}\n' \
  "http://gateway.com:8000/spg/merchant/api/v1/router/private/allocates?store=<ORG1 store>&pod=507f1f77bcf86cd799439011"
curl -s -o /dev/null -w '%{http_code}\n' \
  "http://gateway.com:8000/spg/merchant/api/v1/router/private/allocates?store=<ORG1 store>&pod=000000000000000000000000"
```

**Expect 502 then 404**, and the contrast is the point. **502** means the route matched and the gateway
forwarded to the pod's edge (with no merchant behind it in a cut-down stack) — routing succeeded. **404** means
no route matched, because the `Query=pod,<podId>` predicate did not, which is right for a pod that does not
exist. A 200 would need merchant running; the discriminating pair is stronger evidence than a 200 would be.

### GWR-10 — The old module is gone · [verified]

```bash
grep -rn "pod-external-api\|controlplane.pod.api" --include='*.gradle' --include='*.java' . | grep -v /build/
```

**Expect** — nothing.

---

## PDR — The pod registry service

Phases 4 and 7. A new store-core service on **:8022** owning the pod registry, and then tenancy giving up its
copy. Three audit defects were fixed in the move rather than carried across: `listPublicPods()` delegating to
`findAll`, `GET /pod/{id}` having no `@PreAuthorize` and answering 200 with a null body, and duplicate names
raising an untyped 500.

**Tenancy keeps the store→pod binding** (`manager_store.pod_id`); the registry owns what that pod *is*.
`RouterController` resolves through a new `CachingPodDirectory`, which **fails open** — unlike the placement
client, which fails closed. That asymmetry is deliberate: placement decides where a store *will* live and must
refuse rather than guess; the directory only answers where one *already* lives, and a stale endpoint beats a 502
on the screen a seller uses to reach their own store.

### PDR-01 — The service is registered · [verified]

`./extra/scripts/run-lcl.sh --list` → a `pod-registry … :8022` row.

### PDR-02 — The gateway route is not swallowed by seller-ui · critical · [verified]

```bash
curl -s -o /dev/null -w '%{http_code} %{content_type}\n' http://gateway.com:8000/pod-registry/api/v1/pod/list
```

**Expect** — `401` with **no** content type. HTML means `"pod-registry"` reached the route but not
`backendServices`; the array is negated to build seller-ui's catch-all, so a missing entry serves the console's
shell and reads like a frontend bug.

### PDR-03 — The seed is idempotent · [verified]

- **Expect** — the local pod, seeded from `store-core-lcl-config.yml` (not a `data.sql`): the log shows
  `Reconciling 1 configured pod(s)` then `Seeded pod pod-507f1f77`. Restart and expect **no second insert**; on a
  multi-instance run the loser logs `Another instance is seeding the pod registry; skipping`.

### PDR-04 — The registry's own view · [verified]

`GET /pod-registry/api/v1/pod/507f1f77bcf86cd799439011` as super-admin → `visibility: "PUBLIC"`,
`lifecycleState: "ACTIVE"`, `capacityStores`, health — the operational columns `list` deliberately omits,
because the gateway has no use for them.

### PDR-05 — An unknown pod is a typed 404 · [verified]

`GET /pod-registry/api/v1/pod/000000000000000000000000` → 404 `POD_REGISTRY.POD.NOT_FOUND`. Previously 200 with
a null body.

### PDR-06 — The permission split · critical · [verified]

As **org1-admin**:

| Request | Expect |
|---|---|
| `GET /pod/list` | **200**, and an **empty** array |
| `GET /pod/{id}` | **403** |
| `POST /pod` | **403** |

The empty list is the point: an org admin is admitted to read but sees only pods it owns. A public pod appearing
here means the row filtering in `PodApi.listPods` has regressed.

### PDR-07 — Duplicate pod names · [unit only]

`POST /pod` twice with one name → 200 then **409** `POD_REGISTRY.POD.NAME_TAKEN`.
**Covered by** `PodServiceTest`, both the pre-check and the lost-race paths, and by `pod-api.http`.

### PDR-08 — Tenancy no longer mentions pods · [verified]

```bash
grep -rn "org\.pod\|tenancy\.org" store-core/tenancy | grep -v /build/
```

**Expect** — nothing, and after a fresh start `pg_namespace` shows `tenancy` and `pod_registry` but no `org`.

### PDR-09 — The router still answers, now via the registry · critical · [verified]

`GET /tenancy/api/v1/router/store-pod-by-store-id?store=<ORG1 store>` → 200 with `pod-507f1f77` and its
endpoint. Tenancy read `manager_store.pod_id` and resolved the rest from pod-registry.

### PDR-10 — The router still refuses another org's store · critical · [verified]

Same endpoint with ORG2's store → **404**. Phase 3's query-level scoping is untouched by the move.

### PDR-11 — The pod screen's paged read works · high · [verified]

`GET /pod-registry/api/v1/pod?size=10` → 200 with a page; **empty** for `org1-admin`, populated for a super
admin. Phase 4 implemented only `list` and **dropped the paged root read** that seller-ui's pod table binds to —
a latent regression caught here.

### PDR-12 — The old tenancy pod API is gone · [verified]

`GET /tenancy/api/v1/pod/list` → **404**.

### PDR-13 — A registry outage does not take the router down · critical · [verified]

- **Steps** — stop pod-registry; wait **>60s**, past `POD_DIRECTORY_TTL`, so a refresh is actually attempted and
  fails; re-run PDR-09.
- **Expect** — still **200**, and exactly one `Could not refresh the pod directory; keeping N known pod(s)` WARN
  in tenancy's log. Both halves matter: a 200 *without* the warning only proves the cache had not expired, which
  is not the same as degrading.

### PDR-14 — seller-ui builds and points at the new service · [verified]

```bash
cd store-core/seller-ui && npm run build
grep -rn "tenancy/api/v1/pod" projects/ src/     # expect nothing
```

`POD_API_BASE` moved to `/pod-registry/api/v1/pod` — one line, because phase 1 hoisted it.

### PDR-15 — The super-admin pod screens in the browser · high · [not verified]

- **Steps** — create, rename and delete a pod through the console as `super-admin`.
- **Why it matters** — `getPod(id)` now reaches an endpoint that is **super-admin only**, where tenancy's
  equivalent also admitted org admins. An org admin opening a pod detail page now gets a 403 rather than a body.
  That is intentional — lifecycle, capacity and health are operator data — but it is a user-visible change
  nobody has looked at. Exercised at the API level only (PDR-11, PDR-12, `pod-api.http`).

---

## PLC — Placement

Phase 6. **This is where the cross-tenant placement bug dies.**

Tenancy used to choose a pod itself, in `PodSelectionImpl`. When an organization had no private pod it asked for
"public" pods through `listPublicPods()`, which delegated to `findAll` and returned **every** pod — so a store
could be placed onto **another organization's private pod**. Dedicated infrastructure, silently shared.

Placement now lives in pod-registry behind `POST /api/v1/pod/private/placement`, with ordered rules:

1. Candidates must be `ACTIVE`, healthy (or never probed) and under capacity.
2. An org with private pods is confined to **those only**. If none is eligible it is **refused** — never moved
   onto shared infrastructure.
3. Otherwise, shared pods by a real predicate (`visibility = 'PUBLIC' and org_id is null`).
4. Ties break to least-loaded *by fraction of capacity*, not at random.
5. Nothing eligible → `NoEligiblePodException` (422), replacing an `IllegalArgumentException: bound must be
   positive` from `random.nextInt(0)` — a 500 with no code.

Tenancy calls the registry and **fails closed**, matching how it already treats billing being unreachable.

### PLC-01 — An org with no private pod gets a shared pod · [verified]

`POST /api/v1/pod/private/placement` `{"org":"<ORG1>"}` → 200, the shared pod, `dedicated: false`,
`"least-loaded shared pod"`.

### PLC-02 — An org with a private pod always lands on it · [verified]

`{"org":"<ORG2>"}` → 200, `907f1f77…`, `dedicated: true`.

### PLC-03 — Another org's private pod is unreachable · critical · [verified]

The bug itself.

- **Steps** — `{"org":"<ORG1>"}` while ORG2's private pod exists. Then the sharper version: ask for it by name,
  `{"org":"<ORG1>","preferredPodId":"907f1f77bcf86cd799439099"}`.
- **Expect** — the **shared** pod both times. A preference is honoured only from within the candidate set, never
  as a way around it.

### PLC-04 — A dedicated org whose pods are all ineligible is refused, not relocated · critical · [verified]

- **Setup** — `update pod_registry.pod set lifecycle_state='DRAINING' where id='907f1f77bcf86cd799439099';`
- **Steps** — `{"org":"<ORG2>"}`.
- **Expect** — **422** `POD_REGISTRY.PLACEMENT.NO_ELIGIBLE_POD`, **even though an eligible shared pod exists**.
  This is the case the old code got wrong, and a 200 here is a regression, not a convenience.

### PLC-05 — Registry down: store creation fails closed with no orphan row · critical · [verified]

- **Steps** — stop pod-registry, then create a store through tenancy.
- **Expect** — a 502 naming **pod-registry**, and `select count(*) from tenancy.manager_store` unchanged. A store
  row without a confirmed pod is not recoverable by retrying, because the store is already there.

### PLC-06 — The happy path end to end · critical · [verified]

- **Steps** — create a store through tenancy with everything running.
- **Expect** — 200 and a row whose `pod_id` is the pod placement chose.
- **Seen** — blocked at the time by the locale-interceptor defect (fixed in [RBS-01](#rbs--robustness)); finally
  run during phase 11, reaching `SUCCESSFULLY_PROVISIONING`. See [CNV-04](#cnv-04--creating-a-store-with-the-typed-request--critical--verified)
  for what a complete payload has to contain.

---

## OPS — Pod health, capacity and drain

Phase 8. What makes placement's rules real: until now `capacity_stores` was always 0 and `last_health_status`
always null, so the capacity ceiling, the least-loaded tie-break and the health filter existed only in unit
tests.

**A drained pod takes no new stores but keeps serving its tenants and keeps its gateway route.** That is the
entire difference between retiring a pod and breaking it, and it is why drain exists next to `DELETE`, which
strands every store on the pod and has no undo.

**The health probe is a reachability probe, not a deep health check.** Any HTTP answer — including a 502 —
counts as GREEN, because it proves the pod's edge is up and serving; only a connection failure or timeout is
RED. That is the honest limit of what can be asked without a dedicated health endpoint behind the pod gateway,
and it is the signal placement needs: can this host take traffic at all.

**Health and lifecycle gate placement, never routing.** A RED or DRAINING pod keeps its `/spg/**` route, because
its tenants already live there and withdrawing it turns "degraded" into "entirely offline".

**Capacity is counted from a committed event, not from the placement decision.** Placement is a question asked
while the caller can still abandon the creation; reserving capacity there would leak it on every abandoned
attempt.

> **On "events":** there is **no message broker in this repo.** The `spring.cloud.stream` block in
> `common-config.yml` has no binder behind it, nothing declares an `events` consumer, and docker-compose has no
> broker. The outbox is a local JDBC table polled in-process, and the repo's idiom for reaching another service
> is *outbox handler → HTTP call*. Durable delivery comes from the outbox's retries — which is precisely why the
> receiving endpoint must be idempotent.

### OPS-01 — The probe runs and records · [verified]

Start pod-registry and wait ~60s; the first sweep is one period after start, not immediate.
**Expect** — `pod.last_health_status` set and a row in `pod_health_check`.

### OPS-02 — Drain moves placement elsewhere · critical · [verified]

With two ACTIVE pods, place once and note the pod; drain it; place again → the **other** pod.

### OPS-03 — Drain and resume, with an audit trail · [verified]

- **Steps** — as super-admin: `/resume`, `/drain`, `/drain` again.
- **Expect** — 200, 200, 200 and three `pod_audit` rows: `DRAINING→ACTIVE`, `ACTIVE→DRAINING`, and a
  `DRAINING→DRAINING` marked `no-op:`. The repeat is deliberately not an error — draining twice is not worth
  failing a request over, and the row recording that someone asked is the useful part when reconstructing an
  incident.

### OPS-04 — Drain is super-admin only · high · [verified]

Call `/drain` with the **s2s** token → **403**. A service principal may ask where to place a store; it may not
retire infrastructure.

### OPS-05 — Capacity is idempotent under redelivery · critical · [verified]

`POST /api/v1/pod/private/placement-recorded` three times with the same store and pod → 200 each time,
`capacity_stores` = **1**, exactly one `pod_store_placement` row. This is what the outbox produces routinely, not
an edge case.

### OPS-06 — An unreachable pod goes RED · [verified]

Point a pod's endpoint at a dead port, wait one sweep → `last_health_status = RED`, detail naming the failure. A
live pod stays GREEN **even if it answers 502**.

### OPS-07 — A RED or DRAINING pod keeps its route · critical · [verified]

With one pod DRAINING and one RED: **both** pod routes still present in the gateway's table, and placement →
**422** `NO_ELIGIBLE_POD`. Excluded from placement, still routed — both halves at once.

### OPS-08 — Capacity through the real outbox path · high · [verified]

Create a store through tenancy and confirm `capacity_stores` rises without anyone calling `placement-recorded`
by hand. The only case that proves the handler is actually wired to the event. Blocked at the time by the locale
interceptor; **closed by [RBS-05](#rbs-05--capacity-is-counted-through-the-real-outbox-path--critical--verified)**.

---

## RBS — Robustness

Phase 9. Six audit defects, plus the locale interceptor that had to be fixed first because it blocked every
write endpoint the rest of the phase needed.

- **Provisioning is idempotent.** It is an outbox handler, so it runs again — after a timeout, a restart, any
  non-permanent failure — and the pod's create is not idempotent on the pod's side. It now checks its own
  recorded state first.
- **Refused and unreachable are no longer the same thing.** Both used to mark `FAILED_PROVISIONING` and rethrow.
  A pod that never answered decided nothing, so that case is now left mid-flight and rethrown for the outbox to
  retry; a pod that *answered* with an error is recorded failed and swallowed so the outbox stops. This required
  declaring the failure types on `MerchantStorePodClient.create` — without them both arrive wrapped in the
  unchecked carrier and cannot be told apart.
- **Stores no longer vanish from the console.** The per-row pod call was wrapped in
  `catch (Exception e) { return null; }` followed by a filter, so a slow pod did not degrade the screen, it
  *removed rows from it* — silently, with nothing logged. A merchant seeing a store missing concludes it was
  deleted.
- **Signup cannot orphan an organization.** The org row was committed before uaa was asked for anything, so a
  duplicate email left an org with no user and no way in.
- **Store names are actually unique.** `checkNameExists` is a read-then-write that two concurrent creates both
  pass. A unique constraint now decides.
- **Two dead events deleted.** `OrgCreatedEvent` was `@OutboxEvent` with **zero consumers**, so every signup
  wrote an outbox row nothing would ever handle.

### RBS-01 — The locale interceptor no longer 500s a whole service · critical · [verified]

`RequestCacheAwareLocaleInterceptor` called `setLocale` on the resolver and caught only
`IllegalArgumentException`. Pod services declare a `SessionLocaleResolver`, so it worked there; store-core
services get Spring Boot's default `AcceptHeaderLocaleResolver`, whose `setLocale` throws
`UnsupportedOperationException` **by contract**. Registered platform-wide, that turned a whole service's web
layer into 500s the moment anything supplied a locale — a `?lang=` parameter, or a request Spring Security
cached across a login. It looked intermittent because it only fires when a locale is actually found.

- **Steps** — `GET` and `POST` any tenancy endpoint **with `?lang=en`**.
- **Expect** — normal responses. Before, both were 500 `COMMON.INTERNAL_ERROR`.

### RBS-02 — A duplicate store name is a typed 409 · [verified]

Create a store, then another with the same name → 200, then **409** `CONTROL_PLANE.STORE.NAME_TAKEN` with the
name in `detail` and `params` — not the generic `COMMON.DATA_INTEGRITY_VIOLATION`, and not a 500.

### RBS-03 — Concurrent creates with the same name · critical · [verified]

Fire five simultaneous creates with one name → exactly **one 200 and four 409s**, all `NAME_TAKEN`, and exactly
one row in `manager_store`.

### RBS-04 — A store whose pod cannot be reached still appears in the list · critical · [verified]

`GET /store-manager/private/store` with stores whose pod detail cannot be fetched → `totalElements` equals the
number of rows returned. Under the old code the count and the rows disagreed, and that gap *was* the bug.

### RBS-05 — Capacity is counted through the real outbox path · critical · [verified]

Create a store, wait a few seconds → `pod_registry.pod.capacity_stores` rises and a `pod_store_placement` row
appears, unaided. **This also closes [OPS-08](#ops-08--capacity-through-the-real-outbox-path--high--verified).**

### RBS-06 — A replayed provisioning event does not create the store twice · critical · [unit only]

Forcing a real outbox redelivery needs a mid-flight kill or direct manipulation of `outbox_record`; the guard is
a state check with no timing component, so the test exercises the same branch.
**Covered by** `StoreProvisioningServiceTest.replayDoesNotDuplicate`.

### RBS-07 — Signup cannot orphan an organization · high · [not verified]

Sign up with an email that already exists in uaa → an error, and **no new row** in `tenancy.manager_org`. The
transaction is the whole change and it is one annotation, but the case was not exercised against the stack.

---

## LIF — Store and organization lifecycle

Phase 10. Tenancy could create a store and nothing else: no way to close one, no way to name an organization or
close it, no members beyond the single administrator signup creates, and no record of who changed what.

**Store status is separate from provisioning state**, deliberately. `ProvisioningState` is the machine's answer —
did the pod create succeed. `StoreStatus` is the operator's. Folding them together would mean you could not
suspend a store that was still building.

**Delete is soft.** Billing holds a subscription against the store id and pod-registry holds a placement;
removing the row would orphan both and erase the history of a store that existed.

**Suspension blocks entering a store, not reading its record.** `requireOperable` guards the router lookup and
the pod-detail fetch — the calls the console makes to *work in* a store — while the record stays readable so the
console can show why it is closed. Suspending an organization suspends its stores **without writing to any of
them**: the org owns its status and `requireOperable` reads both, because a fan-out write drifts the moment one
update fails.

**Invitations carry a bearer token and are handled accordingly.** *There is no mail sender anywhere in this
platform* — verified, not assumed — so nothing can email the invitee. Creating an invitation returns a one-time
token and stores only its SHA-256 hash. Resending **rotates** the token: "resend" usually means the first link
went astray, and a link that went astray should stop working. Accept is authenticated but carries no permission
token, which looks like an omission and is not — the invitee is not yet a member, so no org-scoped check could
pass; the token *is* the authorization. Unknown, spent, revoked and expired all return one error, so the
endpoint cannot be used to probe which tokens existed.

> The reaper's defaults are deliberately slow (`reap-rate` PT5M, `stuck-after` PT15M) because a pod that is
> merely slow is not stuck. To see it act, restart tenancy with
> `--com.asrevo.cvhome.tenancy.provisioning.reap-rate=PT10S --com.asrevo.cvhome.tenancy.provisioning.stuck-after=PT1S`.

### LIF-01 — Suspend and resume a store · [verified]

As super-admin: `POST /store-manager/private/store/suspend?store=…&reason=…`, then `/resume` → 200 each, with
`status` moving `ACTIVE → SUSPENDED → ACTIVE`.

### LIF-02 — A suspended store blocks the console but stays readable · critical · [verified]

| Request | Expect |
|---|---|
| `GET /router/store-pod-by-store-id?store=<suspended>` | **422** `CONTROL_PLANE.STORE.NOT_OPERABLE` |
| `GET /router/store-pod-by-store-id?store=<active>` | 200 |
| `GET /store-manager/store-info?store=<suspended>` | **200** — the record is still readable |

The third row is the point: blocking the record too would leave the console unable to explain the suspension.

### LIF-03 — Lifecycle rules · high · [unit only]

- `DELETED` is terminal: resuming a deleted store → **422** `ILLEGAL_TRANSITION`.
- Suspending an already-suspended store → 200, nothing changes, **and an audit row is still written**.
- A soft-deleted store disappears from `GET /store-manager/private/store`.

**Covered by** `StoreLifecycleServiceTest` (6 tests).

### LIF-04 — The invitation flow · critical · [verified]

As org1-admin, against `/org-member`:

1. `POST /invitations?email=Newbie@Example.COM&role=STORE_ADMIN` → 200, a token, email normalised to lowercase.
2. The same address again → **409** `INVITATION.ALREADY_EXISTS`.
3. `GET /invitations` → the token **must not** appear anywhere in the response.
4. `POST /invitations/accept?token=…` → 200, `ACCEPTED`.
5. Accept again → **422** `INVITATION.NOT_USABLE`.
6. `GET /list` → the accepted user is a member.

- **Seen** — token 43 chars, email normalised, duplicate 409, list does not leak the token, second accept 422.

### LIF-05 — The reaper · high · [verified]

Set a store to `IN_PROGRESS_PROVISIONING`, restart tenancy with the fast settings above, wait.
**Expect** — it returns to `NOT_STARTED_PROVISIONING`, eligible for the ordinary provisioning path again, with a
`source = JOB` audit row. The reaper resets rather than calling the pod itself, so the idempotent provisioning
path from phase 9 stays the only place that knows how to talk to a pod.

### LIF-06 — Everything is audited · [verified]

`select * from tenancy.tenancy_audit order by recorded_at` → a row per mutation with the previous and new state,
the actor, and `API` or `JOB`.
- **Seen** — STORE STATUS ×2, INVITATION CREATE, MEMBER JOIN, STORE REPROVISION.

### LIF-07 — Suspending an organization suspends its stores · high · [not verified]

The org path is unit-covered and shares its implementation with the store path, but was not exercised through
the API. Worth doing precisely because it is the one that does **not** write to the store rows.

---

## CNV — Conventions, the typed request, and runnable requests

Phase 11. `*Controller` → `*Api` for all seven classes (no URL changes). `CreateStoreRequest` replaces
`Map<Object, Object>`, which was threaded through six signatures, serialized into the outbox, and read with
`request.get("name").toString()` — an NPE for anyone omitting a name. Signup moved to `api/v1/signup`, off the
base path it shared with `UserAccountApi`: two controllers on one base path is legal and misleading, when
everything on the other one needs a session and a store-scoped permission while signup is the one endpoint
anyone on the internet may call.

**Only two fields of the create request are typed**, deliberately. Tenancy needs the name (it owns the row and
the uniqueness constraint) and the preferred pod (it asks the registry for placement). Everything else — the
whole of merchant's store model — is collected by `@JsonAnySetter` and forwarded untouched. Duplicating
merchant's model here would mean two definitions of a store to keep in step forever, and the flat wire shape
means the console's create form did not have to change.

### CNV-01 — Every endpoint has a runnable request, none aimed at a service port · [verified]

```bash
grep -hE "^(GET|POST|PUT|DELETE) " store-core/tenancy/tenancy-service/http/*.http | grep -vc SELLER_UI_URL
```

**Expect** — `0`, and every `*Api` class appearing in some `http/` file. **Seen** — 40 requests, 11 classes.

### CNV-02 — seller-ui builds · [verified]

`cd store-core/seller-ui && npm run build`.

### CNV-03 — The signup URL moved · [verified]

| Request | Expect |
|---|---|
| `POST /tenancy/api/v1/signup/public/create` | reaches the endpoint (not 404) |
| `POST /tenancy/api/v1/user-account/public/create` | **404** — the collision is gone |

### CNV-04 — Creating a store with the typed request · critical · [verified]

- **Steps** — `POST /store-manager/private/store` with a body the console would send.
- **Expect** — 200, `status: "ACTIVE"`, and — with a **complete** payload — `SUCCESSFULLY_PROVISIONING` a few
  seconds later, plus the pod's `capacity_stores` rising.
- **A complete payload matters.** Merchant requires `theme` ∈ {BASIS, FOOD, …}, `colorTheme` ∈ {OCEAN, SKY, …},
  `defaultLanguage`, and an address:

  | Payload | Outcome |
  |---|---|
  | `colorTheme: BLUE` | refused — not a `ColorTheme` |
  | `theme: DEFAULT` | refused — not a `Theme` |
  | valid enums, no language | refused — merchant NPEs on `defaultLanguage` |
  | complete | **`SUCCESSFULLY_PROVISIONING`** |

  The three refusals are themselves a result worth having: each was classified as *refused* rather than
  *unreachable*, recorded once, and not retried forever — which is exactly what phase 9's error split was for.

---

## MIG — Migration

Two migrations, in this order, and **the order is not optional**.

### MIG-01 — Rename with the service stopped · critical · [verified]

`extra/migrations/2026-08-11-rename-control-plane-to-tenancy.sql`, applied with tenancy **down** — it is not
safe against a running old instance. See [RNM-02](#rnm-02--the-migration-preserves-everything--critical--verified)
and [RNM-03](#rnm-03--nothing-recreates-the-old-schemas-on-boot--critical--verified) for what to check after.

### MIG-02 — Move pods to the registry · critical · [not verified]

`extra/migrations/2026-08-12-move-pods-to-pod-registry.sql`, part 1.

Phase 4 seeded pod-registry from `ServiceDomainProperties` rather than copying `data.sql`. That gets you every
pod's id, name and endpoint — but **not `org_id`**, the private-pod assignment, which only ever existed in the
database, and not any pod an operator created through the old API.

- **Expect** — after part 1, every pod that had an owner still has one, and every PRIVATE pod is still PRIVATE.
- **Skipping this fails silently in the worst possible way:** a formerly-private pod comes back `PUBLIC` with no
  owner, and placement starts putting other organizations' stores on dedicated infrastructure — exactly the bug
  [PLC](#plc--placement) exists to remove.

### MIG-03 — Running it twice is safe · high · [not verified]

Part 1 is `INSERT … ON CONFLICT DO NOTHING` plus an UPDATE restoring `org_id`/`visibility`.
**Expect** — the second run inserts nothing and changes nothing.

### MIG-04 — The destructive step is held back · high · [not verified]

Part 2 is `DROP SCHEMA org CASCADE`, **commented out on purpose**. Run it only after the verification query at
the foot of the file, and only once part 1 is confirmed.

### MIG-05 — Drain the outbox before deploying · critical · [not verified]

`StoreCreatedEvent` now carries `CreateStoreRequest` where it carried a `Map`. **Outbox records written by the
old release will not deserialize into the new shape.** Drain the outbox to zero `PENDING` before deploying, or
accept that in-flight store creations fail and are re-driven by the reaper. Draining also makes
[RNM-06](#rnm-06--outbox-continuity-across-the-rename--high--not-verified) moot.

---

## REG — Regression watchlist

Every item here was a real defect during this work, found by running the thing rather than reading it. Each has
already proven it can happen, and several were invisible from the screen.

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Every storefront 404s when an unrelated service restarts** | The gateway rebuilt its route table from a failed fetch. A full multi-tenant outage. | GWR-02. Stop the route source, wait past one refresh period, confirm routes survive. |
| **A store placed on another org's private pod** | Dedicated infrastructure silently shared, with nothing in any log. | PLC-03 and PLC-04. The refusal in PLC-04 is the half people "fix" back. |
| **Another org's store returned in full** | 200 with the complete record, from a guarded-looking endpoint. | SEC-02. And check it is **404**, not 403. |
| **Every store on the platform returned** | A principal with an org claim but neither expected role fell through the scoping branch. | SEC-04, with a caller that is not an org admin. |
| **The wrong service blamed during an incident** | A pod-registry timeout reported as *"The billing service could not be reached"* — because `RestClientBuilder` handed every client the **same mutable builder**, interceptors accumulated, and the earliest one wraps the call. | Stop one dependency of a service that builds several clients; confirm the error names **that** dependency. |
| **A whole service's web layer 500s** | Any request carrying `?lang=` died in a shared interceptor before reaching a controller. Looked intermittent. | RBS-01. Add `?lang=en` to any store-core request. |
| **Stores vanished from the console** | A slow pod removed rows from the list instead of degrading them; nothing logged. A merchant concludes the store was deleted. | RBS-04. Compare `totalElements` against the rows actually returned. |
| **A typed error replaced by a generic one** | `DataAccessException` attached as a cause: Spring's resolver walks the cause chain, `DataIntegrityErrorHandler` claims it, and the specific code is discarded — while the catch runs correctly. | RBS-02. Assert on the **code**, not just the status. |
| **Every audited change failed** | `String.valueOf(valueObject)` yields `ManagerStoreId[id=…]`, 40-odd chars, overflowing `tenancy_audit.entity_id varchar(24)` — the insert failed and took the change with it, surfacing as a 409 on suspend. | LIF-01 and LIF-06 together. Any value object rendered into a column or a message. |
| **A leaky error message** | `detail: "No store is visible with id ManagerStoreId[id=65f0…]."` | SEC-02. Read the `detail`, not just the code. |
| **A paged endpoint silently dropped** | Phase 4 implemented `list` and not the paged root read seller-ui's pod table binds to. | PDR-11. |
| **Queries left pointing at the renamed schema** | Two statistics queries still said `manager.` — both screens would fail on first open. The completeness grep searched for `control-plane`, not the schema name. | Open both statistics screens after any rename. |
| **The service would not start** | A second `OpenAPI` bean made springdoc ambiguous; `@Modifying` on a `select pg_advisory_xact_lock` made Postgres answer *"A result was returned when none was expected"*. | Start the service. Neither is visible from a build. |

---

## 99 — Known gaps

Behaviour that is expected today. Please do not spend time raising these — but do shout if you see something
*beyond* what is described.

**`isOrgAdmin` still ignores the store it is handed.** `StoreRoleAccessChecker.isOrgAdmin` in
`store-commons/autoconfigure` returns `true` for any store on the platform once the caller holds
`ROLE_ORG_ADMIN`, so **every pod service — catalog, checkout, payment, cua, merchant, content — still lets an
org admin manage any store on the platform.** Tenancy closed this at the query layer ([SEC](#sec--isolation-and-permissions)),
which is why those cases pass; the pods have not. This is the largest open item on the whole plan and has its
own PR. `PermissionAccessChecker.hasReadAccessOnStore` never checking `isSuperAdmin` — so a super admin gets 403
on `store-info` — belongs with it.

**No seller-ui screens for the lifecycle features.** Store suspend / archive / delete, org profile, members and
invitations all have endpoints and none have screens. **Invitations most of all**, since the token is displayed
exactly once and the console is supposed to be what shows the link.

**A cold gateway is blind until its first successful refresh.** It serves config-seeded routes, so a pod created
since the config was written is missing for up to a refresh period. The seed is a mitigation, not a cure
(GWR-06).

**`AMBER` pod health is never produced.** The enum and its CHECK constraint carry it and placement already
excludes it, but nothing sets it. It needs a real per-pod health endpoint to mean anything.

**The health probe is single-threaded and sequential.** With a 3s timeout and a handful of pods that is fine;
with fifty unreachable pods a sweep takes two and a half minutes.

**No capacity reconcile job.** With outbox retries and an idempotent receiver the only drift sources are a
permanently-failed handler and store deletion, which does not exist yet. It should land with deletion, and it
needs a `tenancy-external-api` module that does not exist.

**`PodApi.delete` still orphans stores.** There is no foreign key from `manager_store.pod_id` and no check for
placed stores, so deleting a populated pod strands every store on it. Drain (OPS) is the safe operation; delete
is a sharp tool, and only a super admin holds it.

**Members are not reconciled with uaa.** Removing a member here does not remove their uaa user, and a user
deleted in uaa leaves a membership row behind.

**Audit and health tables grow unbounded** — `tenancy_audit`, `pod_audit`, `pod_health_check` and the outbox
tables. Retention is a platform-wide job nobody has written.

**`WebClientsUtils`' clone fix has no regression test**, because `store-commons/autoconfigure` has no test
source set at all. Given how silently that one failed, it is worth its own small PR.

**`PersistableMerchantStorePopulator.applyLanguages` NPEs on a missing `defaultLanguage`**, so a merchant-side
validation problem surfaces as a 500 rather than a 400 naming the field. In `merchant-core`, unrelated to this
work, and the reason an incomplete create payload is harder to diagnose than it should be (CNV-04).

**The `Manager*` type names survive the rename by design.** Anyone "finishing" it must sweep every
`hasPermission(…,'ManagerStoreId',…)` string too, or it 403s silently.

---

## Automated coverage

Not a substitute for the cases above, but it is what backs the **[unit only]** tags.

| Suite | Tests |
|---|---|
| `PodPlacementServiceTest` | 10 — all five rules, plus both halves of the cross-tenant fix |
| `InvitationServiceTest` | 9 |
| `StoreTenantScopingTest` | 7 |
| `CachingPodDirectoryTest` | 6 |
| `StoreLifecycleServiceTest` | 6 |
| `PodLifecycleServiceTest` / `PodServiceTest` / `StoreProvisioningServiceTest` | 5 each |
| `PodRouteResilienceTest` (gateway) | 5 |
| `PodCapacityServiceTest` | 3 |

**56 tests across tenancy and pod-registry, 0 failures**, plus the gateway's 5. `./gradlew build -x test -x check`,
the touched modules' `build`, the root `checkstyleMain checkstyleTest` and `npm run build` in seller-ui are all
clean. `./gradlew test` for the whole repo has **not** been run.

---

Raise anything unexpected against PR #271. Include the store or pod id, the time, and the matching lines from
`build/lcl-logs/tenancy.log` or `pod-registry.log` — most of these paths are asynchronous, so the log is usually
the only place the real cause appears.

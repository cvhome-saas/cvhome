# Splitting `control-plane` into `tenancy` + `pod-registry`

> **Naming note.** This plan renames the service `control-plane` → `tenancy` in phase 1 (§0). Everywhere below,
> **`tenancy` means the service today called `control-plane`**. File paths and line numbers are cited against the
> **current, pre-rename tree** (`store-core/control-plane/control-plane-service/...`) so they stay resolvable while
> the audit findings are being verified.

## Context

`.claude/plans/billing-subscription-service.md` shipped in one merge (6e49ddd5c): billing now owns plans,
subscriptions, Stripe, invoices and entitlements, and control-plane's org-level subscription code is gone.
What is left in `store-core/control-plane` is the original service — orgs, stores, provisioning, pods, managed
users and signup — and it did not get the same treatment. It is now the least conventional service in
`store-core`: **57 Java files against billing's 74**, and billing is the newer, smaller-scoped one.

An audit found the gap is not cosmetic:

- **A platform-wide storefront outage waiting to happen.** `gateway/.../client/PodClient.java:62-76` performs
  a live HTTP call to control-plane *inside* `getRouteDefinitions()`, and `onErrorResume` returns
  `Mono.empty()` (`:55-58`) — zero pod routes. `refreshRoutes()` (`:49-52`) publishes `RefreshRoutesEvent`
  unconditionally every minute, so `CachingRouteLocator` replaces its table with that empty result. **Any
  control-plane restart or outage kills all `/spg/**` tenant traffic within 60s.**
- **Cross-tenant holes.** `OrgManagerController.java:75` `findAllStores(@RequestParam ManagerOrgId id, …)` has
  no `@PreAuthorize` — the only method in that controller without one — so any authenticated principal can
  enumerate any org's stores. `PodServiceImpl.listPublicPods():39-41` delegates to `listAllPods`, so an org
  with no private pod is placed on the **full inventory, including another org's private pod**.
  `RouterController.java:26` and `PodController.java:51` are likewise unguarded. Overall: **41 endpoint
  mappings, 21 `@PreAuthorize`**, two of them commented out (`StoreManagerController.java:70`,
  `UserAccountController.java:61`).
- **The store→org guard is a no-op.** `store-commons/autoconfigure/.../StoreRoleAccessChecker.java:49-60`
  ignores its `requestedStoreId` and returns `true` for any store once the caller holds `ROLE_ORG_ADMIN`
  (`@SuppressWarnings("java:S1172")` + an `@TODO` that evades the checkstyle `TodoComment` rule). Every
  `hasPermission(#store,'ManagerStoreId',…)` in this service is therefore decorative for org admins.
- **Robustness.** Two dead events (`OrgCreatedEvent`, `StoreProvisionedEvent` — registered, never consumed;
  the latter is not even `@OutboxEvent`); a **non-idempotent** `@OutboxHandler`
  (`ManagerStoreCreatedEventImpl` → `StoreProvisioningService` re-issues pod `create` on every retry);
  `StoreManagerServiceImpl.java:102` `catch (Exception) { return null; }` making stores silently vanish from
  the console when a pod is slow; `StoreProvisioningService.java:33` collapsing pod-rejected and
  pod-unreachable into one `catch`, both marked `FAILED_PROVISIONING`; an orphan org when uaa rejects the
  user mid-signup (`SignupServiceImpl.java:29-38`, no `@Transactional`, no outbox); no unique constraint on
  store name; **no CHECK constraints and no indexes at all** in `schema.sql`; stores stuck in
  `IN_PROGRESS_PROVISIONING` forever with no reaper.
- **Conventions.** 8 `*Controller` classes vs the repo's `*Api`; `Map<Object,Object>` as the store-creation
  payload threaded through 6 signatures **and serialized into the outbox**; `Object`/`PageImpl<Object>`
  returns; two controllers colliding on `api/v1/user-account`; **no `http/` directory** (5 stale root-level
  `.http` files hitting `localhost:8020` and `localhost:8083` directly, two calling endpoints that no longer
  exist); **zero real tests** (3 scaffolding files); an **empty `manager-external-api` module** still in
  `settings.gradle`; hardcoded dependency versions at `control-plane-service/build.gradle:78-79`.
- **The name is wrong, and the split makes it wronger.** See §0.

Intended outcome: a `pod-registry` service on **8022** owning pod identity, health, capacity and placement;
a gateway that survives a registry outage; and `tenancy` — the renamed, restructured control-plane —
following the conventions billing already does, with the store and org lifecycles it never had.

**Decisions taken** (settled with the user; do not re-litigate):

1. **Split into two deployables** — `tenancy` (:8020, renamed from `control-plane`) and `pod-registry` (:8022).
2. **Breaking API changes are allowed**, and seller-ui is updated in the same PRs (~22 hardcoded
   `/control-plane/api/v1/...` paths across 5 Angular libs).
3. All four feature areas are in scope: store lifecycle, org profile + members, audit trail + real event
   consumers, pod health + capacity.
4. **`control-plane` is renamed to `tenancy`**, early (phase 1), including its Postgres schemas — §0.

### One refinement to the split

`RouterController` (`store-pod-by-store-id`) **stays in tenancy**. It reads `manager_store.pod_id`, which
is store-owned data; moving it would force pod-registry to read tenancy's store table. pod-registry owns
the *pod*, tenancy owns the *store→pod binding*. Since the endpoint returns a full `Pod` (not a `PodId`),
tenancy resolves the endpoint through a `CachingPodDirectory` seeded from `ServiceDomainProperties` —
see §2.

---

## 0. The rename: `control-plane` → `tenancy`

**"Control plane" already means something else here.** In cloud vocabulary the control plane is the whole
management layer — which in this repo is *all* of `store-core`: uaa, gateway, billing, seller-ui and this
service together. Naming one member of that set `control-plane` is a category error, and it tells a reader
nothing about what the service owns. What it actually owns is the tenants: orgs, stores, store→pod bindings,
managed users, signup, and (after phases 10–11) suspend/archive/members/invitations. That is *tenancy*.

Three reasons the moment is now:

- Once pods leave for `pod-registry` (phase 7), the residue is purely tenant lifecycle — name and contents
  finally agree.
- The tree is internally inconsistent: modules are `manager-commons`, `manager-events`,
  `manager-external-api`, `pod-external-api` under a `control-plane/` tree, while billing is `billing-*` under
  `billing/` and the planned `pod-registry-*` under `pod-registry/`. The rename fixes the module prefix in the
  same sweep.
- `.claude/skills/project-structure/references/multi-tenancy.md` already calls this concept tenancy. The code
  is the thing that drifted.

**The shape:** tree `store-core/tenancy/`, package root `com.asrevo.cvhome.tenancy.*`, modules
`tenancy-commons` / `tenancy-events` / `tenancy-external-api` / `tenancy-service`,
`spring.application.name: tenancy`, gateway path `/tenancy/**` → `lb://tenancy`, **port unchanged at 8020**.

### Why this is cheap — what the audit established

| Concern | Finding | Consequence |
|---|---|---|
| DB coupling to the service name | Tables live in **`manager`, `org`, `control`** — none named `control_plane`. All three entities pin `@Table(schema=…)` explicitly (`PodEntity:20`, `ManagerOrgEntity:19`, `ManagerStoreEntity:22`) | The schema rename is a *choice*, not a consequence |
| `spring.datasource.hikari.schema` | `common-config.yml:112` = `${spring.application.name}` | Becomes `tenancy` — exactly the DDL schema name, so it lines up for free |
| Spring Session JDBC | **Not used** by this service (no `session` key in its `application.yml`, no dependency) | No session table to migrate, no seller logouts on deploy |
| s2s identity | `client-id: store-core@service.store-core.internal`, `scope: store_core` — layer-named, shared with billing | **No uaa client registration change** |
| Permission tokens | All `STORE-CORE.*` in `CustomPermissionEvaluator` — none `CONTROL-PLANE.*` | **No `CustomPermissionEvaluator` change** |
| Inbound s2s callers | Only `gateway/config/ClientsConfig.java:16` `buildClient("control-plane", …)`. No pod service calls it | One string |
| The hyphen trap | `tenancy` has no hyphen, so `spring.application.name` and the schema name are the same token | Avoids the `pod-registry`/`pod_registry` split §5 warns about |

**Blast radius: ~435 raw occurrences, but only ~110 are code or config.** The rest are 15 documentation files
and the Angular build cache.

### 0a. Java (67 files across 4 modules)

Use **IntelliJ's rename-package / rename-module refactor, not `sed`** — a blind substitution hits
`manager_store`, `StoreManagerController`, `org-manager` and the `store-manager` API paths, none of which
change.

- Package `com.asrevo.cvhome.controlplane.**` → `com.asrevo.cvhome.tenancy.**`. Sub-packages collapse:
  `controlplane.manager.commons.dto` → `tenancy.commons.dto`; `controlplane.manager.commons.event.store` →
  `tenancy.events.store`.
- **Leave `pod-external-api` alone in this PR.** `controlplane.pod.api.ExternalPodClient` moves out entirely
  in phase 5 to `pod-registry-external-api`; renaming a module that is deleted four phases later is churn.
- Classes: `ControlPlaneApplication` → `TenancyApplication`, plus `TestControlPlaneApplication`,
  `ControlPlaneApplicationTests`, and `errors/ControlPlaneErrors` → `TenancyErrors`.
- Module directories via `git mv` so history follows: `manager-commons` → `tenancy-commons`,
  `manager-events` → `tenancy-events`, `control-plane-service` → `tenancy-service`.
- `manager-external-api` (0 java files) is **deleted** in phase 0, not renamed.

**Explicitly out of scope:** `ManagerOrgEntity`, `ManagerStoreEntity`, `ManagerOrgDto`, `ManagerStoreDto`,
`ManagerOrgId`, `ManagerStoreId`. The last two are commons value objects
(`store-commons/commons/.../domain/`) named in the `@PreAuthorize` expression **strings** of every service in
the repo — `hasPermission(#store,'ManagerStoreId',…)` is matched by literal name in
`CustomPermissionEvaluator`, so renaming them is a repo-wide, silently-403-ing change with a blast radius
larger than this entire rename. `Manager*` → `Tenant*` is a separate, later, optional PR. State this in the PR
body so a reviewer does not read the leftover inconsistency as an oversight.

### 0b. Config and wiring — miss one and the service is unreachable

1. `settings.gradle:68-72` — five module paths → `store-core:tenancy:tenancy-*`.
2. `common-config.yml:30-36` — the key **and** its `name:` → `tenancy`; port, namespace and
   `gateway-service-name` unchanged.
3. `lcl-config.yml:17-22` — key, `instance-id`, `service-name`, `instance`.
4. `fargate-config.yml:10` (`loadbalancer.eager-load.clients`) **and** `:30` (`ecs.discovery.service-ports`).
   One without the other works locally and 503s only on AWS.
5. `gateway/config/GatewayRouteLocatorImpl.java` — **two** edits: `:41,43` the route and `lb://` URI, **and**
   `:23` `backendServices`. That array is negated to build seller-ui's catch-all; changing the route without
   the array means every `/tenancy/**` call returns seller-ui's shell HTML — it reads as a frontend bug.
6. `gateway/config/ClientsConfig.java:16` — `buildClient("control-plane", …)` → `"tenancy"`.
   `gateway/build.gradle:43` and the `PodClient.java:19` import stay on `pod-external-api` until phase 5.
7. `extra/scripts/run-lcl.sh:29` — the row, including the gradle path.
8. `tenancy-service/src/main/resources/application.yml` — `spring.application.name: tenancy`,
   `namastack.outbox.jdbc.schema-name: tenancy_outbox`.

### 0c. Database — `ALTER SCHEMA`, not a copy

Both renames are **catalog-only DDL: instantaneous, and they preserve every row including in-flight outbox
records.** No drain, no dual write, no data copy.

```sql
-- extra/migrations/2026-08-11-rename-control-plane-to-tenancy.sql
ALTER SCHEMA manager RENAME TO tenancy;
ALTER SCHEMA control  RENAME TO tenancy_outbox;
```

Follow `extra/migrations/2026-08-10-retire-org-subscriptions.sql`'s conventions: a header explaining what
changes shape, destructive parts kept separate. Nothing here is destructive, but the **ordering is** — run it
with the service **stopped**, between the old release and the new one. A running instance holds the old schema
names in its pinned `@Table` annotations and its Hikari `search_path`.

- `schema.sql` — `create schema if not exists manager` → `tenancy`, `... control` → `tenancy_outbox`, and
  every `manager.` table qualifier.
- `@Table(schema = "tenancy")` on `ManagerOrgEntity` and `ManagerStoreEntity`. Keep pinning it explicitly even
  though it now matches Hikari's default — repo convention, and it survives a future rename.
- **Table names do not change**: `tenancy.manager_org`, `tenancy.manager_store`. Same reasoning as §0a.
- **`org` schema untouched.** It holds only `org.pod`, deleted outright in phase 7 when pods move to
  `pod_registry`. Renaming it now means migrating it twice, and phase 7's `to_regclass('org.pod')` backfill
  guard (§8.2) keeps working unchanged.

### 0d. seller-ui — 22 call sites across 5 libs

Mechanical `/control-plane/api/` → `/tenancy/api/` in `projects/seller-core/`:

| File | Sites |
|---|---|
| `src/lib/auth/user.service.ts` | 8 |
| `stores/src/lib/services/store.service.ts` | 8 |
| `stores/src/lib/services/pod.service.ts` | 6 — move again to `/pod-registry/` in phase 7 |
| `orgs/src/lib/services/org.service.ts` | 6 |
| `analytics/src/lib/services/statistic.api.service.ts` | 3 |
| `src/lib/store/store.service.ts` | 1 — already a `STORE_MANAGER_BASE_URL` constant |
| `signup/src/lib/service/sign-up.service.ts` | 1 |

**While here, hoist the base path into one exported constant per lib** instead of repeating the literal. That
is what makes phase 11's convention work — and any future rename — a one-line change instead of a 22-line
one, and it is why `pod.service.ts` moving again in phase 7 is not a problem. Also fix the six doc comments
naming control-plane: `stores/.../store-service.model.ts:6,36`, `stores/.../store.ts:3`, `orgs/.../org.ts:1`,
`src/lib/models/commons.ts:15`, `src/lib/table/table.types.ts:20`,
`src/app/public/sections/pricing/mappers/pricing.mapper.ts:25`.

No gateway alias is needed — seller-ui is served by the same gateway and deploys with it, and no external
consumer calls these paths. If an out-of-repo client turns up, add `/control-plane/**` as a second route to
`lb://tenancy` for one release train, then delete it.

### 0e. Documentation

`.claude/skills/project-structure/` is the rulebook: if it still says `control-plane`, the next change gets
built wrong. Update, with occurrence counts — `SKILL.md` (13), `references/database-schemas.md` (10),
`events-outbox.md` (10), `store-core.md` (9), `new-service.md` (9), `multi-tenancy.md` (8), `uaa-client.md`
(4), `configuration.md` (3), `service-discovery.md` (3), `gateways-and-local-domains.md` (2), and one each in
`api-conventions.md`, `authentication.md`, `http-request-files.md`, `shared-libraries.md`. Plus `CLAUDE.md:136`.

Fold in §8.8's doc-drift item while in these files: `store-core.md:15-17,66-67` still documents the deleted
`subscription-*` modules.

**Leave `.claude/plans/*.md` and `qa/*.md` history alone** — they record what was true when written. This file
is the only plan rewritten.

---

## 1. Modules

New tree `store-core/pod-registry/`, package root `com.asrevo.cvhome.podregistry.*`. Copy
`store-core/billing/billing-service/build.gradle` and prune (drop `stripe.java`, `gson`, `secret-crypto`; keep
`caffeine`, `mapstruct`, `namastack.outbox.starter.jdbc`, `spring.boot.starter.data.jdbc`). Store-core layer ⇒
**Spring Data JDBC**, hand-written `schema.sql`, s2s client `store-core@service.store-core.internal`, scope
`store_core`.

```
store-core/pod-registry/
├── pod-registry-commons/       PodView, PlacementRequest/Decision, ErrorCode enum + condition-named exceptions
├── pod-registry-events/        @OutboxEvent records (pod lifecycle, placement)
├── pod-registry-external-api/  ExternalPodClient (reactive, for gateway) + ExternalPodPlacementService
│                               (servlet, for tenancy) + CachingPodDirectory + RemoteErrorCatalog
└── pod-registry-service/       the app, schema.sql, http/*.http
```

`pod-registry-external-api` serves both the **reactive** gateway and **servlet** tenancy, so it takes
`compileOnly libs.spring.web` **and** `compileOnly libs.spring.webflux`, with the `Mono` methods on a separate
interface — reactor types on a servlet caller's proxy are a review reject. This is exactly billing's
`ReactiveExternalEntitlementService` split; copy it.

**Moves** (git-mv + repackage, no behaviour change beyond the fixes called out). Source paths are shown
post-rename, i.e. `tenancy-service/.../tenancy/`:

| From | To |
|---|---|
| `org/entity/PodEntity` · `org/repository/PodRepository` | `podregistry/domain/` · `podregistry/repository/` |
| `org/service/PodService{,Impl}` | `podregistry/service/` |
| `org/controller/PodController` | `podregistry/api/v1/PodApi` (rename to the `*Api` convention) |
| `org/config/PodDatabaseInitializer` | `podregistry/config/` (+ advisory lock, §5) |
| `errors/PodNotFoundException` | `pod-registry-commons/.../errors/` |
| `manager/service/PodSelection{,Impl}` | `podregistry/service/PodPlacementService` (rewritten, §5) |
| module `store-core/control-plane/pod-external-api` | `pod-registry-external-api` |

**Deleted:** `store-core/control-plane/manager-external-api` (no `src/` at all) + its `settings.gradle` line —
in phase 0, before the rename.

---

## 2. Boundary and cross-service calls

**pod-registry owns:** pod identity, endpoint + `EndpointType`, name uniqueness, private-org assignment,
lifecycle state, drain, health, capacity, **placement decisions**, pod audit. All of `/api/v1/pod/**`.

**tenancy keeps:** org, store, user, signup, store lifecycle, `manager_store.pod_id`, `RouterController`,
`StorePodClientFactory`, `StoreProvisioningService`.

| Direction | Call | Failure mode | Why |
|---|---|---|---|
| gateway → pod-registry | `GET /api/v1/pod/list` every `PT1M` | **fail open** — last-known-good; cold start seeded from `ServiceDomainProperties.pods()` | Losing this kills *all* tenant traffic. Stale routes beat no routes; a pod endpoint changes ~never. |
| tenancy → pod-registry | `POST /api/v1/pod/private/placement` on store create | **fail closed** — typed exception, store not created | Mirrors billing's quota gate. A store on a drained/dead pod is unrecoverable; a refused create is retryable. |
| tenancy → pod-registry | `GET /api/v1/pod/{id}` (RouterController, store detail) | **fail open** via `CachingPodDirectory` + config seed | Read-only decoration, same shape as `InternalStoreServiceImpl.withBillingStatus`. |
| pod-registry → tenancy | `GET /api/v1/private/pod-store-counts` — **scheduled reconcile only, never in a request path** | fail open, keep last counts | Breaks the cycle in §8.3. |
| pod-registry → each pod | health probe via `restClientBuilder.buildClient(pod, "merchant", …)` | fail open — record RED, exclude from **placement only** | Health must never affect routing; those tenants already live there. |
| tenancy ⇢ pod-registry | `StoreCreatedEvent` / `StoreArchivedEvent` / `StoreDeletedEvent` via outbox | idempotent handlers | Capacity counters without a synchronous edge. |

**Not a cross-service call:** `StorePodClientFactory` resolves from `ServiceDomainProperties.getPodByPodId`
(config, verified) — store provisioning needs no runtime hop. Leave it alone.

---

## 3. The gateway fix (highest impact, ships early and alone)

Restructure `PodClient` to **fetch-then-publish**, so route lookup is never I/O and an error can never shrink
the table:

```java
private final AtomicReference<List<RouteDefinition>> lastKnownGood =
        new AtomicReference<>(List.of());   // @PostConstruct: seed from serviceDomainProperties.pods()

@Scheduled(fixedRateString = "${cvhome.gateway.route-refresh-rate:PT1M}")
public void refreshRoutes() {
    externalPodClient.listPods().map(this::toRouteDefinitions)
        .subscribe(fresh -> { if (!fresh.equals(lastKnownGood.getAndSet(fresh)))
                                  publisher.publishEvent(new RefreshRoutesEvent(this)); },
                   e -> log.error("Pod route refresh failed; keeping {} known routes",
                                  lastKnownGood.get().size(), e));
}

@Override public Flux<RouteDefinition> getRouteDefinitions() { return Flux.fromIterable(lastKnownGood.get()); }
```

Four properties: an error never shrinks the table; lookup is allocation-only; `RefreshRoutesEvent` fires only
on real change (today it churns the cache every minute); and the per-call `new ServiceUrlBuilder(...)` at
`PodClient.java:63` moves to the refresh path. Add a `PodRoutesHealthIndicator` exposing
last-successful-refresh age so a silently-stale gateway is visible in actuator.

Cold start with the registry down is the residual hole; the config seed is the mitigation, not a cure.

**This phase is not gated by the rename.** It is the one genuine production risk in the plan. If it needs to
ship this week, cut it from `develop` first and rebase phase 1 over it — the two touch `PodClient.java` only
at an import line and a log message.

---

## 4. Registration for `pod-registry` (all mandatory — miss one and it is unreachable)

1. `settings.gradle` — the four `'store-core:pod-registry:pod-registry-*'` entries.
2. `store-commons/autoconfigure/src/main/resources/common-config.yml` — a `pod-registry` block under
   `com.asrevo.cvhome.services`, key **== `spring.application.name`**, `port: 8022` (**verified free**: 8000,
   8001, 8010, 8020, 8021 taken), `namespace: store-core.cvhome.lcl`,
   `gateway-service-name: store-core-gateway`.
3. `lcl-config.yml` — a simple-discovery instance at `http://localhost:8022`.
4. `fargate-config.yml` — `"pod-registry"` in `loadbalancer.eager-load.clients` **and** `"pod-registry": 8022`
   in `ecs.discovery.service-ports`. One without the other works locally and 503s only on AWS.
5. `gateway/.../config/GatewayRouteLocatorImpl.java` — **two** edits: the route (`path("/pod-registry/**")`,
   `stripPrefix(1).tokenRelay().preserveHostHeader()`, `uri("lb://pod-registry")`) **and** `"pod-registry"` in
   `backendServices` (line 23, post-rename `{"tenancy","billing","uaa","spg"}`). That array is negated to
   build the seller-ui catch-all — without the second edit every call returns seller-ui's shell HTML.
6. `extra/scripts/run-lcl.sh` — a row after `billing`, before `gateway`.
7. `application.yml` + `-lcl` + `-fargate`, outbox `jdbc.schema-name: pod_registry_outbox`,
   `schema-initialization.enabled: false`.
8. Permission tokens `STORE-CORE.POD.READ` / `.MANAGE` / `.PLACEMENT` need a `case` in
   `CustomPermissionEvaluator` **and** a method on `PermissionAccessChecker`, or they 403 silently (deny by
   default). `.PLACEMENT` is like `BILLING.QUOTA-CHECK`: asked about an org before a store exists, so it
   checks the `SCOPE_STORE_CORE` authority and ignores the target.

Never hand-roll a `@ControllerAdvice`, argument resolver or permission evaluator — `store-commons:autoconfigure`
supplies them.

---

## 5. Persistence, placement, health

**Same database, different schema.** All services share `cvhome`; `common-config.yml:112` sets
`spring.datasource.hikari.schema: ${spring.application.name}`. So the move is `org.pod` → `pod_registry.pod`
inside one Postgres — a plain `INSERT … SELECT`, no dump/restore, no dual write.

> **The hyphen trap:** `spring.application.name` is `pod-registry`, so Hikari's default schema is
> `"pod-registry"`. Name the DDL schema `pod_registry` and pin `@Table(schema = "pod_registry")` explicitly on
> every entity. Same for the outbox schema. (`tenancy` has no hyphen and so does not have this problem.)

`pod_registry.pod` = the moved columns plus `lifecycle_state` (PROVISIONING|ACTIVE|DRAINING|DECOMMISSIONED),
`visibility` (PUBLIC|PRIVATE), `region`, `capacity_max_stores`, `capacity_stores`, `last_health_status`,
`last_health_at` — **every enum column with a CHECK constraint**, plus `check (org_id is null or visibility =
'PRIVATE')`. Also `pod_registry.pod_health_check` (pruned), `pod_registry.pod_store_placement(store_id pk,
pod_id)` and `pod_registry.pod_audit` (shape: `billing.subscription_audit`).

**Do not extend `com.asrevo.cvhome.commons.domain.Pod`** — it is on every service's classpath and is the
gateway's and `StorePodClientFactory`'s wire type. Add `PodView` in `pod-registry-commons`.

`capacity_stores` is maintained from tenancy's store events, backed by `pod_store_placement` so a
redelivered `StoreCreatedEvent` is a genuine no-op (`capacity_stores + 1` is not idempotent). A scheduled
reconcile corrects drift.

### `PodPlacementService` — replacing `PodSelectionImpl` and its cross-tenant bug

Ordered rules:

1. Eligible = `lifecycle_state = 'ACTIVE'` AND not draining AND health ∈ {GREEN, recently-probed-unknown} AND
   (`capacity_max_stores IS NULL OR capacity_stores < capacity_max_stores`).
2. Org has private pods → choose **among those only**, honouring `preferredPodId` if eligible. If the org has
   private pods but none is eligible → **refuse** with `NoEligiblePodException` (422). **Never fall back to
   public** — silently moving a private-pod tenant onto shared infrastructure is the same class of bug being
   fixed.
3. Otherwise `org_id IS NULL AND visibility = 'PUBLIC'` (a real predicate, not `listAllPods`).
4. Tie-break **least-loaded**, not `random`. Today `private static final Random rnd` is shared and
   unsynchronised.
5. Empty candidate set → `NoEligiblePodException`. Today `random(emptyList)` throws
   `IllegalArgumentException: bound must be positive` — a 500 on store create.

Health gates **placement only, never routing**: a RED pod keeps its `/spg/**` route, because its tenants are
already there and removing it breaks working storefronts to fix nothing. Drain (`DRAINING`) is the same —
excluded from placement, still routed.

### Fixed during the move, not after

`PodApi.find(@PathVariable PodId id)` and `RouterController.getStorePodByStoreId` both currently have **no**
`@PreAuthorize`. A verbatim move carries the hole across.

---

## 6. Phases

Each phase is independently shippable and QA-able. **All of them share one QA document** —
`qa/tenancy-and-pod-registry-split.md`, named after this plan, appended to as each phase landed. That is the
convention (CLAUDE.md, and `references/qa-testing.md` §7 in the `project-structure` skill); the per-phase files
this plan originally produced were folded into it.

### Status — phases 0–6 are done and collected on one branch

**Branch: `feat/tenancy-pod-registry-split`.** Phase 0 is already in `develop`; phases 1–6 were each developed
on their own branch and are merged into this one, which is what goes to review as a single PR.

| Phase | Commit | QA section |
|---|---|---|
| 0 — cleanup | `2215afcd2` (already in `develop`) | RNM-01 |
| 1 — rename to `tenancy` | `4a7ed20f6` | RNM |
| 2 — gateway resilience | `f63847fbd` | GWR-01 … GWR-06 |
| 3 — tenancy authorization gaps | `bc13e48e5` | SEC |
| 4 — pod-registry exists | `e85ecb60f` | PDR-01 … PDR-07 |
| 5 — gateway cutover | `f415062b5` | GWR-07 … GWR-10 |
| 6 — placement cutover | `112ccbb03` | PLC |

Verified on the merged branch: full `build -x test -x check` clean, every touched module's `build` +
checkstyle clean, **29 tests green** (`PodPlacementServiceTest` 10, `StoreTenantScopingTest` 7,
`PodRouteResilienceTest` 5, `PodServiceTest` 5, plus 2 context loads).

**Phases 7–11 are also done**, each on its own branch cut from the one before:
`feat/tenancy-drops-pods` (7), `feat/pod-health-capacity-drain` (8), `fix/tenancy-robustness` (9),
`feat/tenancy-store-org-lifecycle` (10), `refactor/tenancy-conventions` (11). Their QA sections are
PDR-08 … PDR-15 (7), OPS (8), RBS (9), LIF (10) and CNV (11).

**The plan is complete.** What remains is listed under "99 — Known gaps" in
`qa/tenancy-and-pod-registry-split.md`; the largest
by far is `StoreRoleAccessChecker.isOrgAdmin`, which is still unfixed and still lets an org admin manage any
store on the platform through the pods.

> **Do not continue phases 7–11 on this branch.** It is closed at phase 6 and is under review as one PR.
> Each later phase cuts a fresh branch — from `develop` once this merges, or from this branch meanwhile,
> and rebased after. The per-phase branches (`refactor/rename-…`, `fix/gateway-…`, `fix/tenancy-…`,
> `feat/pod-registry-service`, `feat/gateway-pod-registry-cutover`, `feat/pod-placement-cutover`) are fully
> contained in it and can be deleted once it lands.

### Two defects found while building, not predicted by the audit

Both are recorded here because they outlive the phases that found them:

1. **`WebClientsUtils` built every typed client from a shared, mutable builder** (found in phase 6, fixed
   there). `baseUrl(...)` mutates in place and interceptors accumulate, so a service's second and later
   clients carried earlier clients' interceptors — and the earliest-registered one wraps the call. A
   pod-registry outage reported *"The billing service could not be reached"* with `remoteService: billing`
   while billing was healthy. Fixed by cloning per client. **It has no regression test**:
   `store-commons/autoconfigure` has no test source set at all, and adding one belongs in its own PR.
2. **`RequestCacheAwareLocaleInterceptor` can 500 every request to a service.** It calls `setLocale` on an
   `AcceptHeaderLocaleResolver`, which throws `UnsupportedOperationException`. Seen blocking *all* tenancy
   requests, `GET` included, during phase 6 QA — but not on every run of the session, so the trigger is not
   yet understood. It fires in `preHandle`, before any controller. **Unowned by any phase here; it needs its
   own fix** and it blocks browser QA of tenancy's write endpoints until then.

✅ = shipped · phases 0–6 on `feat/tenancy-pod-registry-split`, 7–11 each on their own branch

| # | Content | Gate |
|---|---|---|
| **0** ✅ | Delete the empty `manager-external-api` (folder + `settings.gradle`); move `build.gradle:78-79` versions into `libs.versions.toml`; delete dead `manager/utils/ErrorCodes.java`; de-dupe `JdbcConfig` converter block | `./gradlew build -x test -x check` clean |
| **1** ✅ | **The rename `control-plane` → `tenancy`** (§0): Java packages + modules, config + gateway wiring, `ALTER SCHEMA` migration, 22 seller-ui paths hoisted to constants, 15 doc files | The §7 completeness grep is empty; `/tenancy/api/v1/...` returns JSON not seller-ui HTML; a mid-flight outbox record survives the schema rename |
| **2** ✅ | **Gateway resilience only**, still against tenancy (§3) | Stack up; **SIGTERM** tenancy; `/spg/**?store=&pod=` still routes for >5 min; restart → refresh |
| **3** ✅ | **Security fixes in place**, before any move: `@PreAuthorize` on `OrgManagerController:75`, `RouterController:26`, `PodController:51`, both statistic APIs; restore the two commented-out guards; **fix `StoreRoleAccessChecker.isOrgAdmin`** to actually resolve store→org | Org 1 admin gets **403** on org 2's stores; a `ROLE_STORE_MODERATOR` gets 200 on READ, 403 on MANAGE |
| **4** ✅ | pod-registry exists and is registered: 4 modules + the full §4 checklist + `schema.sql` + config-seeded initializer with advisory lock + `PodApi` + `http/pod-api.http`. tenancy untouched | `run-lcl.sh --list` shows it; `GET gateway.com:8000/pod-registry/api/v1/pod/list` returns the seeded pod; non-admin gets 403 |
| **5** ✅ | Gateway cutover: `ExternalPodClient` → `pod-registry-external-api`; delete `pod-external-api`; gateway `build.gradle:43` + `ClientsConfig.java:16` repointed | Stop tenancy entirely; storefronts still serve (phase 2's cache makes this a swap, not a risk) |
| **6** ✅ | Placement cutover: `PodPlacementService` + `/api/v1/pod/private/placement`; tenancy's `PodSelectionImpl` → `ExternalPodPlacementService`, fail closed. **Cross-tenant bug fixed here** | Org with a private pod always lands there; that pod DRAINING → 422, **not** another org's pod; registry down → create fails retryably with no orphan `manager_store` row |
| **7** ✅ | tenancy stops owning pods: delete the 6 `org/**` classes, drop the `org` schema + `data.sql` insert, `RouterController` → `CachingPodDirectory`, seller-ui `pod.service.ts` base constant → `/pod-registry/...` | `grep -rn "org\.pod\|tenancy\.org" store-core/tenancy` empty; `npm run build`; super-admin pod screens exercised in the browser |
| **8** ✅ | Pod health / capacity / drain / audit (§5): new columns, poller, capacity from events + reconcile, drain endpoints | Drain a pod → next store lands elsewhere; kill a pod → RED, existing storefronts unaffected; replay `StoreCreatedEvent` → `capacity_stores` unchanged |
| **9** ✅ | **tenancy robustness** (independent track, unblocked by 4–7): make `ManagerStoreCreatedEventImpl` idempotent; split pod-rejected from pod-unreachable in `StoreProvisioningService:33`; kill `catch (Exception) → null` at `StoreManagerServiceImpl:102`; route signup through the outbox so uaa failure cannot orphan an org; unique constraint on store name; CHECK constraints + indexes in `schema.sql`; consumers for `OrgCreatedEvent` / `StoreProvisionedEvent` (+ `@OutboxEvent` on the latter) | Concurrent same-name creates → one wins; replayed provisioning issues one pod create; a down pod degrades the store list instead of hiding rows |
| **10** ✅ | **Store + org lifecycle**: suspend/resume/archive/delete, the `IN_PROGRESS_PROVISIONING` reaper, org name/status/owner, members + invitations, audit trail | Stuck store is reaped and re-provisioned; suspended store blocks the console; every mutation lands in the audit table |
| **11** ✅ | **Conventions**: `*Controller` → `*Api`, typed `CreateStoreRequest` replacing `Map<Object,Object>` (including in `StoreCreatedEvent`), drop `Object` returns, split the `api/v1/user-account` collision, real `http/` directory through the gateway, delete the 5 stale root `.http` files, seller-ui base constants updated | `npm run build`; every endpoint has a `.http` block hitting `{{SELLER_UI_URL}}`, none hitting `localhost:8020` |

Phase 1 is a single sweeping PR and should not overlap anything else — merge it before opening the parallel
tracks. After it, phases 9–11 are tenancy-only and can run in parallel with 4–8 by a second person.

---

## 7. Verification

**Every PR:**

```bash
./gradlew checkstyleMain checkstyleTest                              # warnings = errors; a TODO fails the build
./gradlew build -x test -x check
./gradlew :store-core:pod-registry:pod-registry-service:test         # Docker running (Testcontainers)
./gradlew :store-core:tenancy:tenancy-service:build
./gradlew :store-core:gateway:gateway-service:build                  # phases 2, 4, 5
cd store-core/seller-ui && npm run build                             # phases 1, 7, 11
```

Plus the error-handling grep gate over both trees: zero hits for `throws BaseException`, any category base, or
`catch (BaseException)` + `switch (category())`.

**Phase 1 completeness gate** — must return hits only in `.claude/plans/` history and
`store-core/seller-ui/.angular/` cache:

```bash
grep -rIn --exclude-dir=node_modules --exclude-dir=.angular --exclude-dir=.git --exclude-dir=build \
  -e 'control-plane' -e 'control_plane' -e 'controlplane' -e 'ControlPlane' .
```

**Stack:** check `lsof -i :8000` first, background `./extra/scripts/run-lcl.sh`, stop with **SIGTERM**, never
SIGINT.

**Phase 1 end-to-end, in this order:**

1. Apply `2026-08-11-rename-control-plane-to-tenancy.sql` with the stack **down**, then start it. Confirm
   `run-lcl.sh --list` shows `tenancy` on 8020.
2. `GET gateway.com:8000/tenancy/api/v1/store-manager/private/store?store=…&lang=en` returns the store list —
   **not** seller-ui's shell HTML. HTML means the `backendServices` edit (§0b.5) was missed.
3. Browser: store list and detail, org list, user management, analytics, signup, super-admin pod screens.
4. **Outbox continuity** — the real risk of the schema rename. Before stopping the stack, create a store so a
   record is mid-flight in `control.outbox_record`; after the rename and restart, confirm it is processed from
   `tenancy_outbox.outbox_record` and the store reaches `PROVISIONED`. A lost outbox record is a store that
   never finishes provisioning, and nothing logs an error.

**Tenant isolation and the permission gate — mandatory, not the happy path.** Org 1's admin session must be
**403** against org 2's stores on `org-manager/stores`, `store-manager/private/store/{id}`, `router`, and pod
reads; repeat every read as org 2's own admin and confirm each sees only its own rows. Test **both**
directions — a missing `case` in `CustomPermissionEvaluator` shows up as a silent 403 on the read path too.
Re-prove this after phase 1, since every path moved.

**Tests** (there are none today beyond scaffolding; payment is the reference):
`PodPlacementRulesTest` (private-only, no public fallback, drain, capacity, empty set — unit),
`PodRouteResilienceTest` (simulated fetch failure leaves the definition count unchanged — `reactor-test`),
`StoreProvisioningIdempotencyTest` (replayed `StoreCreatedEvent` → one pod create),
`SignupOrphanOrgTest` (uaa conflict leaves no org),
`SchemaConstraintTest` (every enum column rejects an invalid literal). The last three are
`@Tag("integration-test")` with Testcontainers Postgres.

---

## 8. Open risks

1. **Cold start with pod-registry down** still yields only the config-seeded routes. The
   `ServiceDomainProperties.pods()` seed is a mitigation, not a cure. Assert it with a test.
2. **`spring.sql.init.mode: always`** re-runs `data.sql` on every boot in every environment. The `org.pod`
   backfill must be `ON CONFLICT (id) DO NOTHING` inside a `to_regclass('org.pod') IS NOT NULL` guard (it must
   not resurrect a pod an operator deleted, and must not fail to parse once the table is gone), and must be
   **deleted in phase 7**. It reads another service's schema — declare it in the PR as a one-release-train
   exception, or move it to an operator script under `extra/scripts/` carrying only `org_id`.
3. **The tenancy ⇄ pod-registry cycle.** Placement wants store counts, which tenancy owns. Making
   that synchronous inside placement creates a request cycle that deadlocks both thread pools under load.
   Counts stay event-derived with a scheduled reconcile — this is the single most tempting shortcut here.
4. **`PodDatabaseInitializer` has no multi-instance guard** and does `findByName` → `save`. Add
   `pg_advisory_xact_lock` in phase 4, before anyone scales the service.
5. **The local pod id `507f1f77bcf86cd799439011`** is hardcoded in `configure-domain.sh`,
   `store-pod-lcl-config.yml`, tenancy's `data.sql` and the demo store rows. A regenerated seed id breaks
   every local storefront and the failure presents as DNS.
6. **`StoreRoleAccessChecker.isOrgAdmin` (phase 3) is in `store-commons/autoconfigure`** — shared by every
   service. Tightening it will surface 403s wherever code currently relies on the permissive behaviour. Sweep
   all callers and QA each service's store screens, not just tenancy's.
7. **The `backendServices` shadow.** Adding a route without the array entry returns seller-ui's shell HTML —
   it reads as a frontend bug and costs an afternoon. This bites twice: phase 1 (`/tenancy/**`) and phase 4
   (`/pod-registry/**`).
8. **The rename's migration ordering.** `ALTER SCHEMA` is instant and lossless, but only if the service is
   stopped across it. Deploying the new jar before the migration, or running the migration against a live old
   instance, produces `relation "manager.manager_store" does not exist` on one side or the other. Write the
   stop → migrate → deploy order into the migration file's header, not just the PR body.
9. **`Manager*` type names survive the rename** (`ManagerStoreId` et al., §0a). Deliberate: they are commons
   value objects matched by literal name in `@PreAuthorize` strings repo-wide. Anyone "finishing the rename"
   later must sweep every `hasPermission(...,'ManagerStoreId',...)` expression, or it 403s silently.
10. **Re-check that port 8022 is still free** in `common-config.yml` at implementation time.

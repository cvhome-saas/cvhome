# QA — pod-registry (`store-core/pod-registry/pod-registry-service`)

A pod is a physical deployment of the pod services in a region. The registry owns the pods themselves — their
endpoints, visibility, lifecycle state, health and capacity — and decides which pod a new store is placed on.
Tenancy asks it; the gateway builds its route table from it.

- **Scope** — the pod CRUD and its permission split, placement, the health probe, capacity accounting, drain
  and resume, and the migration that moved pods out of tenancy
- **Runs on** — `lcl start -d --stack <name>`; read the live port from `lcl urls`. Address it through the
  gateway, never `:8022`
- **Cases** — 25 (21 verified, 1 unit only, 3 not verified)
- **Also see** — [tenancy](../../../tenancy/tenancy-service/qa/tenancy-qa.md) (the caller, and the router that
  degrades when this service is down), [gateway](../../../gateway/gateway-service/qa/gateway-qa.md) (the route
  table this feeds)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins, the seeded org/store/pod ids, the
service-to-service token and the `psql` idiom are in
[`references/qa-testing.md`](../../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5.
Only what is specific to the registry is below.

### A second pod, and a private one

The shared pod `507f1f77bcf86cd799439011` is seeded from `store-core-lcl-config.yml` by `PodSeedInitializer`.
Only that one exists, so the placement and drain cases need more:

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

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select id, name, visibility, lifecycle_state, capacity_stores, last_health_status from pod_registry.pod;"

... "select store_id, pod_id from pod_registry.pod_store_placement;"
... "select pod_id, action, from_state, to_state, actor, source from pod_registry.pod_audit order by recorded_at desc;"
```

Logs: `.lcl/<stack>/logs/pod-registry.log`.

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

`lcl status` → a `pod-registry … :8022` row.

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

### PDR-11 — The pod screen's paged read works · high · [verified]

`GET /pod-registry/api/v1/pod?size=10` → 200 with a page; **empty** for `org1-admin`, populated for a super
admin. Phase 4 implemented only `list` and **dropped the paged root read** that seller-ui's pod table binds to —
a latent regression caught here.

> The rest of §PDR moved to where its assertion lives: PDR-02 (the gateway route is not swallowed) to
> [gateway-qa.md](../../../gateway/gateway-service/qa/gateway-qa.md); PDR-08, PDR-09, PDR-10, PDR-12 and PDR-13
> to [tenancy-qa.md](../../../tenancy/tenancy-service/qa/tenancy-qa.md) (they assert how *tenancy's* router
> behaves); PDR-14 and PDR-15 to
> [console-ui-qa.md](../../../console-ui/qa/console-ui-qa.md).

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

---

## SID — The merged store id, on the registry's side

_From `qa/unify-store-id-value-objects.md` §CNV, reformatted into the case shape used everywhere else._

### SID-01 — A placement row is written for a new store · [verified — write path only]

_Was C3._ A `String → StoreMerchantId` reading converter is needed per module; a missing one is not a compile
error, it throws `ConverterNotFoundException` the first time that column is read.

- **Steps** — provision a store and read `pod_registry.pod_store_placement`.
- **Expect** — the row exists with the store id in `store_id`, and no `ConverterNotFoundException` anywhere.
- **Be precise about what this converter fixes.** `PodStorePlacementRepository` declares
  `CrudRepository<PodStorePlacementEntity, StoreMerchantId>`, but both of its callers go through custom
  `@Query` methods taking `String` (`claim`, `recountCapacity`) — **nothing calls an inherited `CrudRepository`
  method**. So the missing reading converter was **latent, not live**: it could not have thrown for any current
  caller. The converter makes the declared id type actually usable and stops the first `findById` anyone adds
  from failing. Do not go hunting for a bug it was masking; there wasn't one in production use.

---

## MIG — Moving pods into the registry

Two migrations, in this order, and **the order is not optional**.

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

> MIG-01 (rename with the service stopped) and MIG-05 (drain the outbox before deploying) are tenancy's.

---

## REG — Regression watchlist

| What broke | How it looked | How to catch it again |
|---|---|---|
| **A store placed on another org's private pod** | Dedicated infrastructure silently shared, with nothing in any log. | PLC-03 and PLC-04. The refusal in PLC-04 is the half people "fix" back. |
| **A paged endpoint silently dropped** | An earlier phase implemented `list` and not the paged root read the console's pod table binds to. | PDR-11 |

---

## 99 — Known gaps

**`AMBER` pod health is never produced.** The enum and its CHECK constraint carry it and placement already
excludes it, but nothing sets it. It needs a real per-pod health endpoint to mean anything.

**The health probe is single-threaded and sequential.** With a 3s timeout and a handful of pods that is fine;
with fifty unreachable pods a sweep takes two and a half minutes.

**No capacity reconcile job.** With outbox retries and an idempotent receiver the only drift sources are a
permanently-failed handler and store deletion, which does not exist yet. It should land with deletion, and it
needs a `tenancy-external-api` module that does not exist.

**`PodApi.delete` still orphans stores.** There is no foreign key from `manager_store.pod_id` and no check for
placed stores, so deleting a populated pod strands every store on it. Drain (§OPS) is the safe operation;
delete is a sharp tool, and only a super admin holds it.

**`pod_audit` and `pod_health_check` grow unbounded.** Retention is a platform-wide job nobody has written.

**`pod_store_placement` is empty on a fresh local stack.** Provision a store and it gets a row; that is how
SID-01 was exercised.

---

## Automated coverage

| Suite | Tests |
|---|---|
| `PodPlacementServiceTest` | 10 — all five rules, plus both halves of the cross-tenant fix |
| `PodLifecycleServiceTest` / `PodServiceTest` | 5 each |
| `PodCapacityServiceTest` | 3 |

---

Raise anything unexpected against the pod-registry PR. Include the pod id, the time, and the matching lines
from `.lcl/<stack>/logs/pod-registry.log`.

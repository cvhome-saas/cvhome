# QA — pod health, capacity, drain and audit

Phase 8 of `.claude/plans/tenancy-and-pod-registry-split.md`. This is what makes placement's rules real: until
now `capacity_stores` was always 0 and `last_health_status` always null, so the capacity ceiling, the
least-loaded tie-break and the health filter existed only in unit tests.

## What changed and why you are testing it

- **Drain / resume** — `POST /api/v1/pod/{id}/drain` and `/resume`, super-admin only, both audited. A drained
  pod takes no new stores but **keeps serving its tenants and keeps its gateway route**. That is the entire
  difference between retiring a pod and breaking it, and it is why drain exists next to the already-present
  `DELETE`, which strands every store on the pod and has no undo.
- **Health probe** — a scheduled sweep records GREEN/RED per pod and appends to `pod_health_check`.
- **Capacity** — tenancy tells the registry, from an outbox handler, that a store landed; the registry claims
  the store and recounts the pod.
- **Audit** — every lifecycle change writes a `pod_audit` row with actor and source.

Three design points worth knowing before you read the results:

**The health probe is a reachability probe, not a deep health check.** Any HTTP answer — including a 502 —
counts as GREEN, because it proves the pod's edge is up and serving; only a connection failure or timeout is
RED. That is the honest limit of what can be asked without a dedicated health endpoint behind the pod gateway,
and it is the signal placement needs: can this host take traffic at all. It also means `AMBER` is currently
never produced. A deeper check is future work.

**Health and lifecycle gate placement, never routing.** A RED or DRAINING pod keeps its `/spg/**` route,
because its tenants already live there and withdrawing the route turns "degraded" into "entirely offline".

**Capacity is counted from a committed event, not from the placement decision.** Placement is a question asked
while the caller is still deciding and can still abandon the creation; reserving capacity there would leak it
on every abandoned attempt. Counting happens after the store row commits, via a third `@OutboxHandler` on
`StoreCreatedEvent` alongside pod provisioning and billing — the outbox writes one record per handler, so all
three retry independently.

> **On "events":** the plan says capacity is maintained "from tenancy's store events via outbox". Note what
> that means here — there is **no message broker**. The `spring.cloud.stream` block in `common-config.yml` has
> no binder behind it, nothing declares an `events` consumer, and docker-compose has no broker. The outbox is a
> local JDBC table polled in-process. The repo's actual idiom for reaching another service is *outbox handler →
> HTTP call*, which is exactly what `BillingProvisioningEventImpl` does, and what this follows. Durable
> delivery comes from the outbox's retries, which is precisely why the receiving endpoint must be idempotent.

## Setup

```bash
docker compose -f docker-compose-lcl.yml up -d
# uaa, billing, pod-registry, tenancy, gateway — independently, not under run-lcl.sh
```

The placement and capacity endpoints are service-to-service; drain/resume are super-admin. So you need both:

```bash
TOK=$(curl -s -u 'store-core@service.store-core.internal:hLwOF59NEOdMzYYrfxUbQEGVK1uTczj7' \
  -d 'grant_type=client_credentials&scope=store_core' \
  http://uaa.gateway.com:8001/oauth2/token | jq -r .access_token)
```

…and a browser session as `super-admin` / `admin`.

Only one pod is seeded, so most of these need a second:

```sql
insert into pod_registry.pod (id,name,endpoint,endpoint_type,org_id,visibility,lifecycle_state,capacity_stores,version)
values ('807f1f77bcf86cd799439088','pod-second','http://spg-507f1f77.gateway.com','EXTERNAL',null,'PUBLIC','ACTIVE',0,1);
```

---

## Case 1 — the probe runs and records

Start pod-registry and wait ~60s (the first sweep is one period after start, not immediate).

**Expect:** `pod.last_health_status` set, and a row in `pod_health_check`.

## Case 2 — drain moves placement elsewhere

With two ACTIVE pods, place once and note the pod. Drain that pod. Place again.

**Expect:** the second placement returns the **other** pod.

## Case 3 — drain and resume through the API, with an audit trail

As super-admin: `POST /pod/{id}/resume`, then `/drain`, then `/drain` again.

**Expect:** 200, 200, 200 — and three `pod_audit` rows: `DRAINING→ACTIVE`, `ACTIVE→DRAINING`, and a
`DRAINING→DRAINING` marked `no-op:`. The repeat is deliberately not an error: draining twice is not worth
failing a request over, and the row recording that someone asked is the useful part when reconstructing an
incident.

## Case 4 — drain is super-admin only

Call `/drain` with the **s2s** token → **403**. A service principal may ask where to place a store; it may not
retire infrastructure.

## Case 5 — capacity is idempotent under redelivery

`POST /api/v1/pod/private/placement-recorded` three times with the same store and pod.

**Expect:** 200 each time, `capacity_stores` = **1**, and exactly one `pod_store_placement` row. This is the
case the outbox produces routinely, not an edge case — the handler retries.

## Case 6 — an unreachable pod goes RED

Point a pod's endpoint at a dead port, wait one sweep.

**Expect:** `last_health_status = RED`, detail naming the failure. A live pod stays GREEN **even if it answers
502**, because something being broken *behind* the edge is not what this probe measures.

## Case 7 — a RED or DRAINING pod keeps its route

With one pod DRAINING and one RED, check the gateway's route table.

**Expect:** **both** pod routes still present. Then place a store → **422**
`POD_REGISTRY.PLACEMENT.NO_ELIGIBLE_POD`. Excluded from placement, still routed — both halves at once.

## Case 8 — capacity through the real outbox path

Create a store through tenancy and confirm the pod's `capacity_stores` rises without anyone calling
`placement-recorded` by hand.

**Status: NOT RUN.** The endpoint and the handler are both verified — case 5 exercises the receiving side
directly, and the handler is a near-copy of `BillingProvisioningEventImpl` — but the end-to-end path from a
real store creation was not run, because store creation through tenancy is blocked by the locale-interceptor
defect recorded in `pod-placement-cutover.md`. Worth running once that is fixed, because it is the only case
that proves the handler is actually wired to the event.

---

## Results

Run 2026-08-12, branch `feat/pod-health-capacity-drain`, against all five store-core services.

| Case | Result | Evidence |
|---|---|---|
| 1 — probe records | **PASS** | GREEN at 60s, one `pod_health_check` row |
| 2 — drain moves placement | **PASS** | drained `pod-507f1f77` → next placement returned `pod-second` |
| 3 — drain/resume + audit | **PASS** | 3× 200; audit rows `DRAINING→ACTIVE`, `ACTIVE→DRAINING`, `DRAINING→DRAINING` marked `no-op:`, all `API` / `super-admin` |
| 4 — drain is super-admin only | **PASS** | s2s token → 403 |
| 5 — capacity idempotent | **PASS** | 3 calls, all 200, `capacity_stores = 1`, one placement row |
| 6 — unreachable → RED | **PASS** | `RED` / `ResourceAccessException`; live pod `GREEN` / `answered 502 BAD_GATEWAY` |
| 7 — routed but not placeable | **PASS** | both pod routes present with one DRAINING and one RED; placement → 422 |
| 8 — capacity via a real store create | **NOT RUN** | blocked by the locale-interceptor defect |

Automated: **29 tests, 0 failures** across pod-registry — `PodPlacementServiceTest` 10, `CachingPodDirectoryTest`
6, `PodLifecycleServiceTest` 5, `PodServiceTest` 5, `PodCapacityServiceTest` 3. Full `build -x test -x check`,
module builds and checkstyle clean.

## Still open

1. **`AMBER` is never produced.** The enum and its CHECK constraint carry it, and placement already excludes
   it, but nothing sets it. It needs a real health endpoint per pod to mean anything.
2. **No reconcile job.** The plan pairs event-driven counting with a scheduled reconcile against tenancy for
   drift. Not built: with outbox retries and an idempotent receiver, the only drift sources today are a
   permanently-failed handler and store deletion, which does not exist until phase 10. It should land with
   deletion, and it needs a `tenancy-external-api` module that does not exist yet.
3. **`pod_health_check` and `pod_audit` grow unbounded**, like billing's audit tables. A retention job is
   needed before production.
4. **The probe is single-threaded and sequential.** With a 3s timeout and a handful of pods that is fine; with
   fifty unreachable pods a sweep would take two and a half minutes. Worth revisiting before the pod count
   grows.
5. **Case 8**, above — and behind it, the locale-interceptor defect that blocks tenancy's write endpoints.

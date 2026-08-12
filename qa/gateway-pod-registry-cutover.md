# QA — gateway cutover to pod-registry

Phase 5 of `.claude/plans/tenancy-and-pod-registry-split.md`.

## What changed and why you are testing it

The gateway builds one route per pod so `/spg/**?store=&pod=` reaches the pod hosting that store. It used to
get that pod list from **tenancy**. It now gets it from **pod-registry**.

That is the entire point of the split. Tenancy is the busiest service in store-core and the one most often
redeployed; pod-registry holds one table that changes when infrastructure changes, which is to say almost
never. Hanging the platform's tenant route table off the former was a standing availability risk.

Four things moved and one module died:

- `gateway-service/build.gradle` — depends on `pod-registry-external-api` instead of `pod-external-api`
- `ClientsConfig` — `buildClient("pod-registry", ReactiveExternalPodService.class, …)`
- `PodClient` — the field and the call
- `PodRouteResilienceTest` — the same swap
- `store-core/tenancy/pod-external-api` is **deleted**, along with its `settings.gradle` entry. The gateway was
  its only consumer, so nothing else had to change.

This is a swap rather than a risky cutover only because phase 2 already gave `PodClient` a last-known-good
cache. Without it, pointing the route source at a service that had never been exercised in this path would
have been a live experiment on tenant routing. **This branch therefore contains phase 2's commit as well** —
it is stacked on `fix/gateway-pod-route-resilience`, not on the rename alone.

## Setup

Deliberately start **without tenancy** — that is the whole demonstration:

```bash
sudo ./extra/scripts/configure-domain.sh                        # once, ever
docker compose -f docker-compose-lcl.yml up -d                  # infra, including the spg Caddy edge

./gradlew :store-core:uaa:bootRun                                --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:pod-registry:pod-registry-service:bootRun  --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:gateway:gateway-service:bootRun            --args='--spring.profiles.active=lcl,test-stores'
```

> Not under `run-lcl.sh` if you plan to stop a service — it tears the whole stack down when any child exits.

---

## Case 1 — a cold gateway serves config-seeded routes and says it is blind

Immediately after the gateway starts, before its first scheduled refresh:

```bash
curl -s localhost:8000/actuator/health | jq '.components.podRoutes'
```

**Expect:** `status: "DOWN"`, `reason: "no successful pod route refresh yet"`,
`servingConfigSeededRoutes: 1`. Serving and reporting-DOWN at once is the design: the routes work, but nobody
should mistake this for a healthy gateway. Note this makes `/actuator/health` return **503** for the first
minute after start-up, which is expected and not a failure.

## Case 2 — the refresh succeeds with tenancy not running at all

Wait past one `cvhome.gateway.route-refresh-rate` period (`PT1M`), then re-read health.

**Expect:** `status: "UP"` with `secondsSinceLastRefresh` under 60 and `routes: 1` — **while port 8020 has
nothing listening on it.** A successful refresh with tenancy absent is the proof that the pod list now comes
from pod-registry.

## Case 3 — the route table

```bash
curl -s localhost:8000/actuator/gateway/routes | jq -r '.[] | "\(.route_id) -> \(.uri)"'
```

**Expect:** a `pod-507f1f77 -> http://spg-507f1f77.gateway.com:80` route, plus the static backend routes
including `lb://pod-registry`. The `lb://tenancy` route is still listed — tenancy still serves its own API,
it is simply no longer the route *source*.

## Case 4 — tenant traffic actually routes

```bash
# valid pod id
curl -s -o /dev/null -w '%{http_code}\n' \
  "http://gateway.com:8000/spg/merchant/api/v1/router/private/allocates?store=65f023632bc46470c104b76f&pod=507f1f77bcf86cd799439011"

# bogus pod id — the control
curl -s -o /dev/null -w '%{http_code}\n' \
  "http://gateway.com:8000/spg/merchant/api/v1/router/private/allocates?store=65f023632bc46470c104b76f&pod=000000000000000000000000"
```

**Expect 502 then 404**, and the contrast is the point:

- **502** means the route *matched* and the gateway forwarded to the pod's Caddy edge, which has no merchant
  behind it in this cut-down stack. Routing succeeded.
- **404** means no route matched, because the `Query=pod,<podId>` predicate did not — which is exactly right
  for a pod that does not exist.

A 200 would need merchant running; the 502/404 pair is stronger evidence that matching is driven by the
registry's data than a 200 would be, because it discriminates.

## Case 5 — the old module is gone

```bash
grep -rn "pod-external-api\|controlplane.pod.api" --include='*.gradle' --include='*.java' . | grep -v /build/
```

**Expect:** nothing.

---

## Results

Run 2026-08-12, branch `feat/gateway-pod-registry-cutover` (phase 4 + phase 2 merged), against
`uaa + pod-registry + gateway` with **tenancy deliberately not started**.

| Case | Result | Evidence |
|---|---|---|
| 1 — cold start honest | **PASS** | `DOWN`, `no successful pod route refresh yet`, 1 seeded route; health 503 as expected |
| 2 — refresh without tenancy | **PASS** | `UP`, `secondsSinceLastRefresh: 52`, `routes: 1`, port 8020 empty |
| 3 — route table | **PASS** | `pod-507f1f77 -> http://spg-507f1f77.gateway.com:80`, plus `lb://pod-registry` |
| 4 — tenant traffic routes | **PASS** | valid pod **502** (reached the pod edge), bogus pod **404** (no route matched) |
| 5 — old module gone | **PASS** | no references outside build output |

`PodRouteResilienceTest` — **5 tests, 0 failures** — still passes unchanged in behaviour against the new
client, which is the useful signal: the resilience contract survived the swap. Full `./gradlew build -x test
-x check`, the gateway module `build`, and checkstyle are all clean.

## Still open after this PR

1. **Tenancy still owns `org.pod`** and still serves `/tenancy/api/v1/pod`. Two registries now exist and only
   one is authoritative for routing. Phase 7 deletes tenancy's copy; until then an operator editing pods
   through the tenancy API changes a table nothing reads for routing, which is a real chance to confuse
   somebody. Worth saying out loud in a standup, not just in a file.
2. **Placement still uses tenancy's `PodSelectionImpl`** and therefore still has the cross-tenant fallback bug
   — an org with no private pod can be placed on another org's private pod. Phase 6.
3. **Cold start with pod-registry down** still yields only config-seeded routes. Unchanged by this PR;
   inherent to the seed being a mitigation rather than a cure.

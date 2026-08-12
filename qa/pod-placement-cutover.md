# QA — placement cutover to pod-registry

Phase 6 of `.claude/plans/tenancy-and-pod-registry-split.md`. **This is where the cross-tenant placement bug
dies.**

## What changed and why you are testing it

Tenancy used to choose a pod itself, in `PodSelectionImpl`. That code had a defect worth restating so nobody
reintroduces it: when an organization had no private pod, it asked for "public" pods through
`PodServiceImpl.listPublicPods()`, which delegated to `findAll` and returned **every** pod. So a store could be
placed onto **another organization's private pod** — dedicated infrastructure, silently shared.

Placement now lives in pod-registry behind `POST /api/v1/pod/private/placement`, with ordered rules:

1. Candidates must be `ACTIVE`, healthy (or never probed) and under capacity.
2. An org with private pods is confined to **those only**. If none is eligible it is **refused** — never moved
   onto shared infrastructure.
3. Otherwise, shared pods chosen by a real predicate (`visibility = 'PUBLIC' and org_id is null`).
4. Ties break to least-loaded *by fraction of capacity*, not at random.
5. Nothing eligible → `NoEligiblePodException` (422), replacing an `IllegalArgumentException: bound must be
   positive` from `random.nextInt(0)` — a 500 with no code.

`PodSelection` / `PodSelectionImpl` are deleted. Tenancy calls the registry and **fails closed**, matching how
it already treats billing being unreachable.

## Setup

```bash
docker compose -f docker-compose-lcl.yml up -d
# uaa, billing, pod-registry, tenancy, gateway — each independently, not under run-lcl.sh
./gradlew :store-core:pod-registry:pod-registry-service:bootRun --args='--spring.profiles.active=lcl,test-stores'
# …and the rest
```

Placement is service-to-service, so it needs a client-credentials token, **not** a session:

```bash
TOK=$(curl -s -u 'store-core@service.store-core.internal:hLwOF59NEOdMzYYrfxUbQEGVK1uTczj7' \
  -d 'grant_type=client_credentials&scope=store_core' \
  http://uaa.gateway.com:8001/oauth2/token | jq -r .access_token)
```

Seeded ids — ORG1 `21f023932bc66470c104b76f`, ORG2 `352023632b046970c104b76f`, shared pod
`507f1f77bcf86cd799439011`.

To exercise the private-pod rules you need a private pod, which nothing seeds:

```sql
insert into pod_registry.pod (id,name,endpoint,endpoint_type,org_id,visibility,lifecycle_state,capacity_stores,version)
values ('907f1f77bcf86cd799439099','pod-org2-private','http://spg-org2.gateway.com','EXTERNAL',
        '352023632b046970c104b76f','PRIVATE','ACTIVE',0,1);
```

---

## Case 1 — an org with no private pod gets a shared pod

`POST /api/v1/pod/private/placement` `{"org":"<ORG1>"}` → 200, the shared pod, `dedicated: false`.

## Case 2 — an org with a private pod always lands on it

`{"org":"<ORG2>"}` → 200, `907f1f77…`, `dedicated: true`.

## Case 3 — the bug: another org's private pod is unreachable

`{"org":"<ORG1>"}` while ORG2's private pod exists → must return the **shared** pod, never `907f1f77…`.

Then the sharper version — ask for it explicitly:
`{"org":"<ORG1>","preferredPodId":"907f1f77bcf86cd799439099"}` → still the shared pod. A preference is honoured
only from the candidate set, never as a way around it.

## Case 4 — a dedicated org whose pods are all ineligible is refused, not relocated

```sql
update pod_registry.pod set lifecycle_state='DRAINING' where id='907f1f77bcf86cd799439099';
```

`{"org":"<ORG2>"}` → **422** `POD_REGISTRY.PLACEMENT.NO_ELIGIBLE_POD`, **even though an eligible shared pod
exists**. This is the case the old code got wrong, and a 200 here is a regression, not a convenience.

## Case 5 — registry down: store creation fails closed with no orphan row

Stop pod-registry, then create a store through tenancy.

**Expect** a 502 naming **pod-registry**, and `select count(*) from tenancy.manager_store` unchanged. A store
row without a confirmed pod is not recoverable by retrying, because the store is already there.

## Case 6 — the happy path end to end

Create a store through tenancy with everything running; expect 200 and a row whose `pod_id` is the pod
placement chose.

**Status: NOT RUN — blocked by an unrelated pre-existing defect.** Every request to tenancy's web layer, `GET`
included, failed with:

```
java.lang.UnsupportedOperationException: Cannot change HTTP Accept-Language header
  at AcceptHeaderLocaleResolver.setLocale(AcceptHeaderLocaleResolver.java:130)
  at RequestCacheAwareLocaleInterceptor.updateLocale(RequestCacheAwareLocaleInterceptor.java:55)
  at RequestCacheAwareLocaleInterceptor.preHandle(RequestCacheAwareLocaleInterceptor.java:44)
```

This is in `store-commons/autoconfigure` and fires in `preHandle`, before any controller or placement code. It
is not caused by this branch — nothing here touches inbound locale resolution — but it was **not** bisected
against a clean checkout, so that is an inference from the stack trace rather than a proven fact. It needs its
own fix; `RequestCacheAwareLocaleInterceptor` cannot call `setLocale` on an `AcceptHeaderLocaleResolver`.

Note the earlier registry-down run (case 5) *did* reach placement through tenancy, so the interceptor did not
fire on every run of the session — worth knowing when reproducing it.

---

## Results

Run 2026-08-12, branch `feat/pod-placement-cutover`.

| Case | Result | Evidence |
|---|---|---|
| 1 — shared placement | **PASS** | `507f1f77…`, `dedicated: false`, `"least-loaded shared pod"` |
| 2 — dedicated placement | **PASS** | `907f1f77…`, `dedicated: true`, `"least-loaded private pod"` |
| 3 — no cross-tenant leak | **PASS** | ORG1 got the shared pod both implicitly and when explicitly asking for ORG2's pod by id |
| 4 — refused, not relocated | **PASS** | 422 `POD_REGISTRY.PLACEMENT.NO_ELIGIBLE_POD` with an eligible shared pod available |
| 5 — fail closed, no orphan | **PASS** | 502, store count unchanged at 4, no row created |
| 6 — happy path via tenancy | **NOT RUN** | blocked by the locale interceptor defect above |

Automated: `PodPlacementServiceTest` — **10 tests, 0 failures**, covering all five rules plus the two halves of
the cross-tenant fix (no fallback query is even issued for a dedicated org; a preference cannot escape the
candidate set). `PodServiceTest` — 5 tests. Full `./gradlew build -x test -x check` and checkstyle clean.

## A shared-library bug found by this work

Case 5 initially returned the right behaviour with the **wrong diagnosis**: a 502 saying *"The billing service
could not be reached"* with `remoteService: "billing"`, while billing was healthy and the failing path in
`params` was `/api/v1/pod/private/placement`.

`RestClientBuilder` hands every client the **same, mutable** `RestClient.Builder`. `baseUrl(...)` mutates in
place and interceptors accumulate, so tenancy's third client carried the interceptors of the two billing
clients built before it — and the earliest-registered interceptor wraps the call, so **billing's**
`RemoteErrorCatalog` translated a pod-registry transport failure.

This affects any service building more than one typed client, and it points an operator at the wrong service
during an incident. Fixed by cloning the builder per client in `WebClientsUtils`, on both the RestClient and
WebClient paths, with a javadoc explaining why so it is not removed as redundant. Re-verified: the same
scenario now reports *"The pod registry could not be reached."* with `remoteService: "pod-registry"`.

**Follow-up, deliberately not done here:** there is no regression test for it, because
`store-commons/autoconfigure` has no test source set and no test dependencies at all. Adding test
infrastructure to a shared module inside a placement PR is the wrong place for it, but given how silently this
failed, it is worth its own small PR.

## Still open after this PR

1. **Tenancy still owns `org.pod`** and serves `/tenancy/api/v1/pod`. Two registries, one authoritative.
   Phase 7.
2. **The locale interceptor defect** blocks browser-driven QA of tenancy's write endpoints. Needs its own fix.
3. **No capacity counting yet** — `capacity_stores` stays 0, so the least-loaded tie-break and the capacity
   ceiling are exercised only by unit tests. Phase 8 maintains it from store events.

# QA — the pod-registry service

Phase 4 of `.claude/plans/tenancy-and-pod-registry-split.md`.

## What changed and why you are testing it

A new store-core service on **:8022** that owns the pod registry — which pods exist, who owns them, and what
state they are in. It exists so the gateway can get its route table from a small, rarely-changing service
instead of tenancy, which is the busiest service on the platform and the one most often restarted.

**Nothing is cut over yet.** Tenancy still owns its own `org.pod` table and still serves `/tenancy/api/v1/pod`,
and the gateway still polls tenancy. The two registries run side by side; the gateway moves in phase 5 and
tenancy stops owning pods in phase 7. So the thing to check here is that the new service *exists, is reachable
through the gateway, and is correctly guarded* — not that anything now depends on it.

Three defects from the audit are fixed in the move rather than carried across:

- `listPublicPods()` used to delegate to `findAll`, so an organization with no private pod could be placed onto
  **another organization's private pod**. The replacement is a real predicate
  (`visibility = 'PUBLIC' and org_id is null`), and the schema has a CHECK making "owned but public" unstorable.
- `GET /pod/{id}` had **no `@PreAuthorize`** and answered `200` with a `null` body for an unknown id. It is now
  guarded and returns a typed 404.
- Duplicate pod names raised `IllegalArgumentException` (a 500 with no code) or a raw `DuplicateKeyException`
  when two instances raced. Both now produce `POD_REGISTRY.POD.NAME_TAKEN` (409).

## Setup

> **Do not run this QA under `run-lcl.sh`** if you intend to restart a single service — it tears the whole
> stack down when any child exits. Start them independently:

```bash
sudo ./extra/scripts/configure-domain.sh                          # once, ever
docker compose -f docker-compose-lcl.yml up -d                    # infra

./gradlew :store-core:uaa:bootRun                                  --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:pod-registry:pod-registry-service:bootRun    --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:gateway:gateway-service:bootRun              --args='--spring.profiles.active=lcl,test-stores'
```

tenancy and billing are not needed: nothing calls pod-registry yet, and it calls nothing.

Log in at `http://gateway.com:8000/oauth2/authorization/uaa`. Both logins are local seed data:
**`super-admin` / `admin`** and **`org1-admin` / `admin`**.

> The login form does not submit from a synthetic click or Enter in this environment. If you are driving it
> from a tool rather than by hand, submit the form directly (`document.querySelector('form').requestSubmit()`).
> Also note the gateway holds sessions in memory — restarting it logs you out, and the symptom is a **401**
> where you expected a 403.

---

## Case 1 — the service is registered

```bash
./extra/scripts/run-lcl.sh --list        # expect a pod-registry row on 8022
```

**Expect:** `pod-registry   :store-core:pod-registry:pod-registry-service   :8022`.

## Case 2 — the gateway route is not swallowed by seller-ui

```bash
curl -s -o /dev/null -w '%{http_code} %{content_type}\n' \
  http://gateway.com:8000/pod-registry/api/v1/pod/list
```

**Expect:** `401` with **no** content type. HTML here means `"pod-registry"` was added to the route but not to
`backendServices` in `GatewayRouteLocatorImpl` — the array is negated to build seller-ui's catch-all, so a
missing entry returns the console's shell and reads like a frontend bug.

## Case 3 — the seed, and the phase gate

As **super-admin**: `GET /pod-registry/api/v1/pod/list`

**Expect:** 200 with the local pod `pod-507f1f77` / `507f1f77bcf86cd799439011` and its
`http://spg-507f1f77.gateway.com` endpoint — seeded from `store-core-lcl-config.yml` by `PodSeedInitializer`,
not from a `data.sql`. The service log shows `Reconciling 1 configured pod(s)` then `Seeded pod pod-507f1f77`.

Restart pod-registry and check the log again: **expect no second insert**, and on a multi-instance run expect
the loser to log `Another instance is seeding the pod registry; skipping`.

## Case 4 — the registry's own view

As **super-admin**: `GET /pod-registry/api/v1/pod/507f1f77bcf86cd799439011`

**Expect:** 200 carrying `visibility: "PUBLIC"`, `lifecycleState: "ACTIVE"`, `capacityStores: 0`, and null
health — the operational columns `list` deliberately omits, because the gateway has no use for them.

## Case 5 — an unknown pod is a typed 404

`GET /pod-registry/api/v1/pod/000000000000000000000000`

**Expect:** 404 with `code: "POD_REGISTRY.POD.NOT_FOUND"`. Previously this was 200 with a null body.

## Case 6 — the permission split

As **org1-admin**:

| Request | Expect |
|---|---|
| `GET /pod/list` | **200**, and an **empty** array — org1 owns no private pods, and the only pod is public |
| `GET /pod/{id}` | **403** |
| `POST /pod` | **403** |

The empty list is the point: an org admin is admitted to read but sees only pods it owns. If a public pod
appears here, the row filtering in `PodApi.listPods` has regressed.

## Case 7 — duplicate names

As super-admin, `POST /pod` twice with the same name.

**Expect:** 200 then **409** `POD_REGISTRY.POD.NAME_TAKEN`.

**Status: NOT RUN** — covered by `PodServiceTest` for both the pre-check and the lost-race paths, and by the
`.http` file, but not executed against the running stack.

## Case 8 — placement never crosses tenants

Give org2 a private pod, then ask where a store for org1 should go.

**Status: NOT RUN — the endpoint does not exist yet.** Placement lands in phase 6; this case is written now
because it is the reason the schema and the query are shaped the way they are. `PodServiceTest` pins the query
choice in the meantime.

---

## Results

Run 2026-08-12, branch `feat/pod-registry-service`, against `uaa + pod-registry + gateway` started
independently over docker infra.

| Case | Result | Evidence |
|---|---|---|
| 1 — registered | **PASS** | `--list` shows pod-registry on 8022 |
| 2 — route not shadowed | **PASS** | 401, empty content type, on gateway and on :8022 directly |
| 3 — seed / phase gate | **PASS** | `Seeded pod pod-507f1f77`; list returns it with its endpoint through the gateway |
| 4 — registry view | **PASS** | `PUBLIC` / `ACTIVE` / `capacityStores: 0` |
| 5 — typed 404 | **PASS** | `POD_REGISTRY.POD.NOT_FOUND`, detail carrying the bare id |
| 6 — permission split | **PASS** | org1-admin: list **200 `[]`**, find **403**, create **403** |
| 7 — duplicate name | **NOT RUN** | unit-tested both paths; not exercised on the stack |
| 8 — placement | **NOT RUN** | endpoint arrives in phase 6 |

Two failures found and fixed during this run, both only visible by actually starting the service:

1. A local `SwaggerConfig` declared a second `OpenAPI` bean, which made springdoc's `openAPIBuilder` ambiguous
   and **failed the context at start-up**. `store-commons:autoconfigure` already supplies that bean; the
   service-local config now only maps value objects to strings, as billing's does.
2. The seed lock was `@Modifying @Query("select pg_advisory_xact_lock(:key)")`, and `@Modifying` calls
   `executeUpdate`, so Postgres answered *"A result was returned when none was expected"* and the service died
   on boot. It is now `pg_try_advisory_xact_lock`, which returns a boolean — and the non-blocking variant is
   better anyway: a second instance skips a seed that is already running rather than queueing to repeat it.

Automated coverage: `./gradlew :store-core:pod-registry:pod-registry-service:test` — **5 tests, 0 failures**
(unknown pod → 404, duplicate name pre-check, lost name race, placement uses the narrow query and never
`findAll`, visibility follows ownership). `checkstyleMain` across the three new modules plus autoconfigure and
the gateway, and a full `./gradlew build -x test -x check`, are clean.

## Deviations from the plan

- **Three modules, not four.** `pod-registry-events` is not created: it would have no events in it until phase
  8, and an empty module is exactly what got `manager-external-api` deleted in phase 0. It arrives with its
  first event.
- **No `data.sql`.** The plan described seeding the local pod there. `PodSeedInitializer` already reconciles it
  from `ServiceDomainProperties`, and two seeding mechanisms that can disagree is worse than one.
- **`spring.datasource.hikari.schema` is overridden** to `pod_registry` in `application.yml`. `common-config.yml`
  sets it to `${spring.application.name}`, which for this service is the hyphenated `pod-registry` — not a
  valid schema. Every `@Table` pins the schema explicitly as well.

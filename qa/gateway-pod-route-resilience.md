# QA — gateway pod-route resilience

Phase 2 of `.claude/plans/tenancy-and-pod-registry-split.md`.

## What changed and why you are testing it

`store-core/gateway/.../client/PodClient.java` builds one gateway route per pod, so
`/spg/**?store=…&pod=…` reaches the pod hosting that store.

**Before:** `getRouteDefinitions()` — which Spring Cloud Gateway's `CachingRouteLocator` calls while it
rebuilds its route table — made a live HTTP call to tenancy and, on any error, returned `Mono.empty()`.
`refreshRoutes()` published a `RefreshRoutesEvent` unconditionally every minute. So a tenancy restart or
outage caused the gateway to rebuild its table from an empty result: **every tenant storefront 404s within
one refresh period.** That is a full multi-tenant outage triggered by an unrelated service restarting.

**After:** routes are fetched on the schedule and cached in `lastKnownGood`; lookup never does I/O. A failed
refresh logs and leaves the previous table intact. A `RefreshRoutesEvent` is published only when the route set
actually changes. `@PostConstruct` seeds the table from `ServiceDomainProperties.pods()` so a gateway that
starts while tenancy is down still routes the statically-known pods. `PodRoutesHealthIndicator` exposes
staleness, because a gateway serving indefinitely stale routes would otherwise look perfectly healthy.

The distinction that matters: **an empty response from tenancy is a real answer and IS applied** (a pod really
was removed); a *failure* to get an answer is not. Case 4 below is what separates them, and collapsing the two
is the most likely way someone reintroduces this bug.

## Setup

> **Do not run this QA under `run-lcl.sh`.** The script supervises its children and tears the whole stack
> down when any one of them exits — so SIGTERMing tenancy also kills the gateway, and Case 2 then "fails"
> for a reason that has nothing to do with the code. This was hit on the first attempt at this run.
> Start the three services **independently** instead:

```bash
sudo ./extra/scripts/configure-domain.sh                    # once, ever
docker compose -f docker-compose-lcl.yml up -d              # infra

# each in its own shell / background process, so they can be killed individually
./gradlew :store-core:uaa:bootRun                    --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:tenancy:tenancy-service:bootRun --args='--spring.profiles.active=lcl,test-stores'
./gradlew :store-core:gateway:gateway-service:bootRun --args='--spring.profiles.active=lcl,test-stores'
```

Both profiles are required: `lcl` is the environment slice, `test-stores` seeds the demo orgs and users.

Wait for gateway on :8000 and tenancy on :8020. Everything below is unauthenticated actuator, so no login is
needed.

The local pod is `507f1f77bcf86cd799439011`, seeded by tenancy's `data.sql` and present in
`store-core-lcl-config.yml`.

---

## Case 1 — routes are present when tenancy is up

**Status: PASSED** (see Results).

```bash
curl -s localhost:8000/actuator/gateway/routes | jq -r '.[].route_id' | grep '^pod-'
```

**Expect:** at least `pod-507f1f77`.

## Case 2 — the route table survives a tenancy outage

This is the regression that motivated the change.

1. Confirm Case 1 passes.
2. Stop tenancy with **SIGTERM** (`kill <pid>`, not `kill -9`, not Ctrl-C).
3. Wait **>70s** — longer than one `cvhome.gateway.route-refresh-rate` (`PT1M`) period, so at least one
   refresh has failed. Watch for `Pod route refresh failed; keeping 1 known route(s)` in the gateway log.
4. Re-run the Case 1 command.

**Expect:** `pod-507f1f77` is **still listed**. On the old code it disappeared here.

**Also expect:** the gateway log shows the failure at ERROR each period — the outage must be *visible*, just
not *fatal*.

## Case 3 — staleness is observable, and being briefly stale is not "down"

With tenancy still stopped from Case 2:

```bash
curl -s localhost:8000/actuator/health | jq '.components.podRoutes'
```

**Expect:** `status: "UP"` with `secondsSinceLastRefresh` climbing and `routes: 1`. Staleness under
`cvhome.gateway.route-staleness-threshold` (`PT10M`) is by design and stays UP.

**Then:** leave tenancy down >10 min and re-check → `status: "DOWN"`, `routes` still 1. The gateway keeps
serving while *reporting* that it is flying blind. Both halves matter: serving is the availability win,
reporting is what stops the outage being silent.

> If `.components.podRoutes` is absent, health detail exposure is off for this profile; use
> `management.endpoint.health.show-details=always`.

## Case 4 — an empty answer is still an answer

Distinguishes "tenancy says there are no pods" from "tenancy did not answer". **Only case here that needs a
running tenancy and a DB write.**

1. With the stack up, delete the pod row: `delete from org.pod where id = '507f1f77bcf86cd799439011';`
   against the `cvhome` database.
2. Wait >70s.

**Expect:** `pod-507f1f77` **disappears**. A successful empty response is applied.

3. Restore it (restart tenancy — `PodDatabaseInitializer` re-seeds from config on `ApplicationReadyEvent`)
   and confirm the route returns within a refresh period.

**Status: NOT RUN** — requires a DB mutation; the unit test
`PodRouteResilienceTest.emptyResponseIsAppliedButOnlyWhenItIsTheRealAnswer` covers the same branch.

## Case 5 — recovery

1. Restart tenancy.
2. Wait >70s.

**Expect:** `secondsSinceLastRefresh` resets to near zero, health returns to UP, routes still correct. The
gateway log shows `Pod routes changed; publishing refresh` **only if** the set actually changed — a steady
state should be quiet, not logging a publish every minute.

## Case 6 — cold start with tenancy down (the known residual gap)

1. Stop **both** gateway and tenancy.
2. Start **only** gateway.

**Expect:** `pod-507f1f77` is present anyway — seeded from config by `@PostConstruct` — and health is **DOWN**
with `reason: "no successful pod route refresh yet"`.

**Expected to fail, by design:** a pod created *since* the config was written is missing until the first
successful refresh. The config seed is a mitigation, not a cure. Do not file this as a bug; it is documented
in `PodClient.seedFromConfiguration`'s javadoc and in the plan's open risks.

---

## Results

Run 2026-08-11, branch `refactor/rename-control-plane-to-tenancy`, `uaa + tenancy + gateway` started as three
independent processes against docker infra. All executed against a real running stack.

| Case | Result | Evidence |
|---|---|---|
| 1 — routes present | **PASS** | `pod-507f1f77` in `/actuator/gateway/routes`; health UP, `routes: 1` |
| 2 — survives outage | **PASS** | tenancy SIGTERMed; after **134s** (2 failed refreshes logged) `pod-507f1f77` still routing. This is the regression: pre-fix the table emptied at the 60s mark |
| 3 — staleness observable | **PARTIAL PASS** | UP with `secondsSinceLastRefresh` climbing 48 → 134 while down. The **>10 min → DOWN** variant was not waited out; the DOWN path itself is proven by case 6 |
| 4 — empty answer applied | **NOT RUN** | needs a DB mutation; branch covered by unit test `emptyResponseIsAppliedButOnlyWhenItIsTheRealAnswer` |
| 5 — recovery | **PASS** | tenancy restarted; `secondsSinceLastRefresh` reset 134 → 50, health UP, route intact |
| 6 — cold start | **PASS** | gateway started alone: `Seeded 1 pod route(s) from configuration`, route served, health **DOWN** with `reason: "no successful pod route refresh yet"` |

Notable observation from case 5: `Pod routes changed; publishing refresh` was logged **zero** times across the
whole run. The pod set never actually changed, so no `RefreshRoutesEvent` was ever published — the
publish-only-on-change fix working exactly as intended. Pre-fix this published every 60s, making
`CachingRouteLocator` discard and rebuild its entire table each time for nothing.

Automated coverage, all green:
`./gradlew :store-core:gateway:gateway-service:test --tests '*PodRouteResilienceTest*'` — **5 tests, 0
failures**. It asserts the core invariant directly: a failed refresh leaves the route count unchanged and
publishes no event, and route lookup never calls the registry.
`checkstyleMain`/`checkstyleTest` and `:store-core:gateway:gateway-service:build` both clean.

Automated coverage, all green:
`./gradlew :store-core:gateway:gateway-service:test --tests '*PodRouteResilienceTest*'` — 5 tests, 0 failures.
That test asserts the core invariant directly: a failed refresh leaves the route count unchanged and publishes
no event, and route lookup never calls the registry.

# QA — gateway (`store-core/gateway/gateway-service`)

The platform gateway is the single front door for the seller tier: it routes `/tenancy/**`, `/billing/**`,
`/pod-registry/**`, `/uaa/**` and `/spg/**` to their services over `lb://`, falls through to console-ui for
everything else, holds the seller's session, relays the token inward, and refuses writes for a store whose
subscription has lapsed.

- **Scope** — the static route table and the dynamic pod routes, the console catch-all, token relay and host
  preservation, the session, and `StoreBillingGuardFilter`
- **Runs on** — `lcl start -d --stack <name>`; it *is* `http://gateway.com:8000` (read the live port from
  `lcl urls`)
- **Cases** — 23 (16 verified, 1 unit only, 6 not verified)
- **Also see** — [pod-registry](../../../pod-registry/pod-registry-service/qa/pod-registry-qa.md) (the source
  of the pod routes), [tenancy](../../../tenancy/tenancy-service/qa/tenancy-qa.md) (the router it used to read),
  [billing](../../../billing/billing-service/qa/billing-qa.md) (the blocked-store list),
  [spg](../../../../store-pod/spg/qa/spg-qa.md) (the pod's own edge, a different gateway)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins and the seeded ids are in
[`references/qa-testing.md`](../../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5.
Only what is specific to the gateway is below.

Sign in at `http://gateway.com:8000/oauth2/authorization/uaa` — hitting an `/api/**` URL directly returns 401
without redirecting to a login page.

> **The gateway holds sessions in memory.** Restarting it logs you out, and the symptom is a 401 where you
> expected a 403. That is SES-01, not a defect. The login form also does not submit from a synthetic click in
> some tools — use `document.querySelector('form').requestSubmit()`.

Many cases here stop a *dependency* and watch the gateway behave. Use `lcl stop <service>` /
`lcl restart <service>`; never kill a supervised PID.

### Looking at the truth underneath

```bash
curl -s http://gateway.com:8000/actuator/gateway/routes | jq '.[].route_id'   # the live route table
curl -s http://gateway.com:8000/actuator/health | jq '.components.podRoutes'  # staleness, from PodRoutesHealthIndicator

lcl logs store-core-gateway --grep 'Blocked store set changed|Could not refresh'
```

Logs: `.lcl/<stack>/logs/store-core-gateway.log`.

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

---

## RTE — The static route table

Five prefixes belong to backends — `tenancy`, `billing`, `pod-registry`, `uaa`, `spg` — and
`GatewayRouteLocatorImpl` **negates** that array to build console-ui's catch-all. A service missing from it is
not merely unrouted: its calls are answered with the console's shell HTML, which is why the two cases below
exist and why both were real defects.

### RNM-04 — `/tenancy/**` returns JSON, not seller-ui's HTML · critical · [verified]

- **Expect** — a JSON body from any tenancy path through the gateway, error bodies included (RFC-7807
  `ProblemDetail`). Any HTML shell means the route was added without adding `"tenancy"` to `backendServices`.

### PDR-02 — The gateway route is not swallowed by seller-ui · critical · [verified]

```bash
curl -s -o /dev/null -w '%{http_code} %{content_type}\n' http://gateway.com:8000/pod-registry/api/v1/pod/list
```

**Expect** — `401` with **no** content type. HTML means `"pod-registry"` reached the route but not
`backendServices`; the array is negated to build seller-ui's catch-all, so a missing entry serves the console's
shell and reads like a frontend bug.

### RTE-01 — Every backend prefix resolves through `lb://` · critical · [not verified]

- **Steps** — `curl -s http://gateway.com:8000/actuator/gateway/routes | jq -r '.[].uri'`, then request one
  path under each of `/tenancy/`, `/billing/`, `/pod-registry/`, `/uaa/`, `/spg/`.
- **Expect** — five `lb://` URIs plus the console route. Each request reaches its service (a 401 or a typed
  4xx is a reach; the console's HTML is not). A `503 no instances available for X` means that service is
  missing from `lcl-config.yml`, not that the route is wrong.

### RTE-02 — The console catch-all answers only on the console's own hosts · high · [not verified]

- **Steps** — request `/` and an unknown path such as `/anything` on `gateway.com:8000`, on
  `console-ui.gateway.com:8000`, and on a host that is neither.
- **Expect** — the first two return the console's shell HTML; the third matches no route at all. The catch-all
  is `host(gateway.com, www.gateway.com, console-ui.gateway.com)` **and** the negation of the five prefixes.

### RTE-03 — The forwarded `Host` is the real one, not what discovery resolved · high · [not verified]

- **Why it matters** — the console renders server-side, so the `Host` it receives is what its SSR pass treats
  as the request's own origin. Without `preserveHostHeader` that becomes `localhost:8011` locally and the
  task's **private IP** on Fargate, putting an internal address where the public one belongs.
- **Steps** — request the console through the gateway and check the `Host` the console logged; repeat for one
  `/tenancy/**` call.
- **Expect** — `gateway.com:8000` in both, never `localhost:8011` or an IP.

### RTE-04 — The seller's token is relayed inward, not re-minted · critical · [not verified]

- **Steps** — with a console session, call `/tenancy/api/v1/store-manager/list` through the gateway and read
  tenancy's log for the principal it saw.
- **Expect** — tenancy sees the **operator's** token (`tokenRelay()`), not the gateway's `s2s` client
  credentials. `/uaa/**` relays it unchanged too — uaa's own `AppSecurityConfig` gates `/api/v1/admin/**` on
  `SCOPE_super_admin`, and that guard, not the gateway's, is what keeps the admin API safe.

### SES-01 — The session is in memory and does not survive a restart · [not verified]

- **Steps** — sign in, `lcl restart store-core-gateway`, then repeat any `/api/**` call.
- **Expect** — **401**, and the console bounces to the login page. Expected, not a defect: a 401 where you
  expected a 403 almost always means this happened.

### SES-02 — Logout ends the session at the gateway · high · [not verified]

- **Steps** — sign in, then `POST /logout`; retry a private call.
- **Expect** — 401 on the retry, and the browser lands back on uaa's login rather than in a redirect loop.

---

## ENF — The edge gate for a lapsed store

_From `qa/billing-per-store-subscriptions.md` §ENF — the two cases whose assertion is the gateway's._

`StoreBillingGuardFilter` turns away seller **writes** for a store whose subscription has lapsed. It applies
only to changes, and only to pod traffic addressed by the `store` query parameter — the console's path through
the gateway. Reads pass, matching the pods' own gate: a seller who has stopped paying can still see the catalog
they are being asked to pay for. A shopper reaches a storefront **by host**, through the pod's own edge, and
never crosses this filter, so a suspended store keeps selling. The answer is `402 Payment Required` — a 403
would send the seller to their permissions and a 404 would suggest the store is gone.

### ENF-01 — A lapsed store is refused at the edge · critical · [verified]

- **Setup** — a store Not subscribed or Suspended. Allow a minute; the edge refreshes on a timer.
- **Steps** — in the console for that store, create or edit a product.
- **Expect** — **402 Payment Required** with a message about the subscription — not a permissions error, not a
  404.

### ENF-04 — Nothing is blocked while billing is down · high · [verified]

- **Steps** — with everything working, stop billing; then use a paying store normally — list stores, edit a
  product.
- **Expect** — work continues. The store list renders with billing standing shown as unknown rather than as an
  error. An outage must not stop a paying merchant trading.

---

## SID — The merged store id, at the one boundary that crosses a network hop

_From `qa/unify-store-id-value-objects.md` §WIRE and §CNV, reformatted into the case shape used everywhere
else. Billing's `blockedStores()` now returns `["65f0…"]` instead of `[{"id":"65f0…"}]`, and the gateway is the
consumer that parses it._

### SID-01 — The blocked-store refresh parses the new shape · critical · [verified]

_Was W2._

- **Steps** — on a running stack, let `StoreBillingStatusClient.refresh()` run at least once, then grep
  `.lcl/<stack>/logs/store-core-gateway.log` for `Could not refresh blocked stores`.
- **Expect** — the scheduled refresh completes with no error; that line must **not** appear.

### SID-02 — A non-empty blocked list is not just parsed but honoured · critical · [verified]

_Was W3, and the case SID-01 could not reach: the payload whose JSON shape changed carrying real values end to
end._

- **Steps** — create stores whose subscription is `PENDING`; watch the gateway log; attempt a catalog write to
  one; then activate a subscription and retry.
- **Expect** — the set grows (`Blocked store set changed: 0 -> 1 -> 2 -> 3`), the write is refused
  `402 BILLING.STORE.SUSPENDED`, activating shrinks it (`3 -> 2`) within the refresh interval, and the same
  write then returns 201.

### SID-03 — Nothing regressed at startup, across every service · [verified]

_Was C4._

- **Steps** — after a full `lcl start -d`:

  ```bash
  grep -i 'ConverterNotFound\|Cannot deserialize\|MismatchedInput' .lcl/<stack>/logs/*.log
  ```

- **Expect** — empty, across all services.

---

## REG — Regression watchlist

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Every storefront 404s when an unrelated service restarts** | The gateway rebuilt its route table from a failed fetch. A full multi-tenant outage. | GWR-02. Stop the route source, wait past one refresh period, confirm routes survive. |
| **A backend's calls answered with the console's HTML** | The service was missing from `backendServices`, so the negated catch-all claimed its prefix. Looks like a JSON parse error in the caller, not a routing bug. | RNM-04, PDR-02, RTE-01 |
| **A private IP forwarded as the origin** | `preserveHostHeader` missing on the console route; harmless locally, wrong on Fargate where `lb://` goes through Cloud Map. | RTE-03 |

---

## 99 — Known gaps

**A cold gateway is blind until its first successful refresh.** It serves config-seeded routes, so a pod
created since the config was written is missing for up to a refresh period. The seed is a mitigation, not a
cure (GWR-06).

**A restarted gateway blocks nothing until billing answers.** The edge works from a list refreshed on a timer.
If the gateway starts while billing is down, it holds an empty list and lets everything through until billing
returns. This follows from choosing to fail open (ENF-04).

**Sessions are in memory.** There is no shared session store, so a gateway restart logs every seller out and
two gateway instances would not share sessions (SES-01).

---

Raise anything unexpected against the gateway PR. Include the path, the time, and the matching lines from
`.lcl/<stack>/logs/store-core-gateway.log` — plus `curl -s .../actuator/gateway/routes` output, which is the
whole answer for most routing reports.

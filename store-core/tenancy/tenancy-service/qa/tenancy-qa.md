# QA — tenancy (`store-core/tenancy/tenancy-service`)

Tenancy is the control plane: organizations, stores, their lifecycle, the members and invitations that get
people into them, and the store→pod router the gateway and the pods depend on. Everything a merchant owns
starts as a row here and is provisioned outward over the outbox.

- **Scope** — the store and org APIs, signup and invitations, suspend/archive/delete, the store→pod router,
  the outbox that provisions a store into its pod, and the user-account endpoints the console drives
- **Runs on** — `lcl start -d --stack <name>`; read the live port from `lcl urls`. Address it through the
  gateway, never `:8020`
- **Cases** — 47 (33 verified, 3 unit only, 11 not verified)
- **Also see** — [pod-registry](../../../pod-registry/pod-registry-service/qa/pod-registry-qa.md) (placement,
  capacity and pod health), [gateway](../../../gateway/gateway-service/qa/gateway-qa.md) (the route table this
  feeds), [billing](../../../billing/billing-service/qa/billing-qa.md) (which gates store creation),
  [merchant](../../../../store-pod/merchant/merchant-service/qa/merchant-qa.md) (the pod side of a created store)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

Sections [REG](#reg--regression-watchlist) and [99](#99--known-gaps) are the highest-value reading: one is
defects that have already happened, the other is behaviour that looks wrong and is expected.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins, the seeded org/store/pod ids, the
service-to-service token, gateway-vs-pod addressing and the `psql` idiom are in
[`references/qa-testing.md`](../../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5.
Only what is specific to tenancy is below.

### Run this QA under `lcl`

`lcl` can stop and restart one supervised service while the rest of the stack stays up. Many cases here stop
one service, so use `lcl stop <service>` and `lcl restart <service>`; never kill a supervised PID directly.
`lcl why <service>` shows the exact command and failure evidence when a service does not recover.

> The gateway holds sessions **in memory**. Restarting it logs you out, and the symptom is a 401 where you
> expected a 403. The login form also does not submit from a synthetic click in some tools — use
> `document.querySelector('form').requestSubmit()`. Go to
> `http://gateway.com:8000/oauth2/authorization/uaa` first: hitting an `/api/**` URL directly returns 401
> without redirecting to a login page.

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

# who changed what
... "select entity_type, entity_id, action, from_state, to_state, actor, source, recorded_at
       from tenancy.tenancy_audit order by recorded_at desc limit 20;"

# the events that provision a store outward
... "select id, aggregate_id, event_type, status, failure_reason from outbox_record order by created_at desc limit 20;"
```

Logs: `.lcl/<stack>/logs/tenancy.log`.

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
- **Expect** — `lcl status` shows **tenancy** up on 8020, and `\dn` still shows no `manager` / `control`.
  The store list serves the **migrated** four stores. Seed data in `tenancy` *plus* a populated `manager` schema
  means the migration was skipped — that is the silent failure above.

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

> RNM-04 (`/tenancy/**` returns JSON, not seller-ui's HTML) is a routing assertion and moved to [gateway-qa.md](../../../gateway/gateway-service/qa/gateway-qa.md).

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

> **RBS-07 also touches uaa** — it is the signup ↔ uaa transactional boundary. It is kept here because signup
> is tenancy's endpoint; [uaa-qa.md](../../../uaa/qa/uaa-qa.md) cross-references it.

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

---

## PDR — What tenancy kept when the pod registry split off

_From `qa/tenancy-and-pod-registry-split.md` §PDR — the cases whose assertion is about **tenancy**: that it no
longer owns pods, that its router still answers through the registry, and that a registry outage does not take
the router down. The registry's own cases are in
[pod-registry-qa.md](../../../pod-registry/pod-registry-service/qa/pod-registry-qa.md)._

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

### PDR-12 — The old tenancy pod API is gone · [verified]

`GET /tenancy/api/v1/pod/list` → **404**.

### PDR-13 — A registry outage does not take the router down · critical · [verified]

- **Steps** — stop pod-registry; wait **>60s**, past `POD_DIRECTORY_TTL`, so a refresh is actually attempted and
  fails; re-run PDR-09.
- **Expect** — still **200**, and exactly one `Could not refresh the pod directory; keeping N known pod(s)` WARN
  in tenancy's log. Both halves matter: a 200 *without* the warning only proves the cache had not expired, which
  is not the same as degrading.

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

> CNV-02 (seller-ui builds) is gone: seller-ui no longer exists. The console's equivalent is in [console-ui-qa.md](../../../console-ui/qa/console-ui-qa.md).

---

## CRT — Creating a store, from the caller's side

_From `qa/merchant-store-service.md` §CRT. Creation is tenancy's endpoint: it re-validates merchant's required
fields **before** the outbox so the merchant can be told what is wrong, and it records a pod that refuses
differently from a pod that never answers. Merchant's own side is
[merchant-qa.md](../../../../store-pod/merchant/merchant-service/qa/merchant-qa.md) CRT-03 and CRT-05._

A store is created by the control plane, not by the merchant directly: tenancy writes its own row, then calls
`POST /private/store` on the pod **through the outbox**. Creation is therefore asynchronous, and the merchant
sees "provisioning" then "ready" or "failed".

Because there is no later moment at which the caller can be told what was wrong, **tenancy re-validates
merchant's required fields up front** — deliberately duplicating part of merchant's model. The required set is
name, email, phone, theme, colorTheme, currency, defaultLanguage, supportedLanguages, and address.{country,
city, postalCode}.

### CRT-01 — Creating a store from the console works end to end · critical · [not verified]

- **Steps** — create a store through `/create-store`; watch `merchant.log` and `control.outbox_record`.
- **Expect** — the row appears in `merchant.merchant_store` within seconds, with its `SUB_DOMAIN` allocated
  from the store name, and the storefront answers on its hostname once `/etc/hosts` has the entry.

### CRT-02 — An incomplete body is refused synchronously, with fields · critical · [not verified]

The whole point of the duplicated validation. It has already been wrong once, in a way that produced a real
FAILED store row.

- **Steps** — post a create body missing `city`, then missing `postalCode`, then missing `phone`.
- **Expect** — **400 with field errors** from tenancy, before any store row exists. A 200 followed minutes
  later by a failed provisioning — or a **500** reading `COMMON.INTERNAL_ERROR` — is the regression.

### CRT-04 — A pod that refuses and a pod that never answers are recorded differently · high · [not verified]

The client names these failures separately because the caller acts on the difference: a refusal is a verdict
and is recorded as failed provisioning; a timeout decided nothing and must be left for the outbox to retry.

- **Steps** — (a) stop `merchant`, create a store; (b) make merchant refuse (post a body it will reject).
- **Expect** — (a) the outbox row stays pending and retries, and the store completes when merchant returns;
  (b) the store is recorded as failed with the pod's own code. A timeout recorded as a rejection is the
  regression this contract exists to prevent.

---

## TRL — The billing gate on store creation

_From `qa/billing-per-store-subscriptions.md` §TRL — the one case whose assertion is tenancy's: creation must
fail closed. The trial and quota rules themselves are
[billing-qa.md](../../../billing/billing-service/qa/billing-qa.md)._

### TRL-05 — Store creation is refused when billing is unreachable · high · [not verified]

Deliberately the opposite of every other billing call: a store nobody is billed for is worse than an error you
can retry.

- **Steps** — stop `billing`, try to create a store.
- **Expect** — creation **fails**, no store row left behind. Restart billing, retry, it works.

---

## PERM — The user-account endpoints and the permission evaluator

_From `qa/console-ui-users-and-profile.md` §PERM. The screen is the console's; the endpoint
(`UserAccountApi.resetPassword`, `ManagedUserAccountServiceImpl.list`) is tenancy's, and the fix was in
`store-commons/autoconfigure`'s `CustomPermissionEvaluator`. PERM-02 (the new password actually signs in) is
[uaa-qa.md](../../../uaa/qa/uaa-qa.md)._

The one backend change in this module. `UserAccountApi.resetPassword` declared
`STORE-CORE.USERS.RESET_PASSWORD`, and that token was matched by no `case` in `CustomPermissionEvaluator` —
it fell through every switch to `default -> false`. **The endpoint was 403 for every caller, including a
super admin, from the day it was written.** Nothing reported it because an unmapped token is
indistinguishable from a refused one, and no frontend called the endpoint: seller-ui's change-password
screen points at `PATCH /v1/private/user/{id}/password`, which is mapped nowhere either.

### PERM-01 — Setting a password works at all · critical · [verified]

As `org1-admin`, `/users` → choose `Store1 Moderator` → **Set password** → `Passw0rdQA` twice → confirm.

**Expect** — a success toast, and `POST …/user-account/reset` **200** in the network tab.

- **Seen** — 403 before the fix and 200 after, in the same tab, with the only variable being a restart of
  tenancy onto the rebuilt `store-commons`. That pair is the evidence for this whole section.
- **Watch for** — the request must carry `userId=` as uaa's **UUID**, not the username, and `?store=`.

### PERM-03 — A moderator may not set a password · critical · [not verified]

The token resolves to `hasMaintainAccessOnUsers` — org admin or store admin — deliberately **not** the read
audience. As `org1-store1-moderator`, call `POST …/user-account/reset?store=…&userId=…` directly.

**Expect** — **403.** The console does not offer the button to a moderator ([U-09](#u-09)); this proves the
server refuses it too, which is the half that matters.

### PERM-04 — The regression guard · high · [verified]

```bash
./gradlew :store-commons:autoconfigure:test
```

**Expect** — 17 tests pass. Delete the `STORE-CORE.USERS.RESET_PASSWORD` case from
`CustomPermissionEvaluator` and exactly two fail. These are the first tests in `store-commons`.

---

---

## SID — The merged store id, on tenancy's side

_From `qa/unify-store-id-value-objects.md` §CNV, §WIRE and §REG, reformatted from that file's bold run-in cases
into the case shape used everywhere else. `ManagerStoreId` and `StoreMerchantId` are now one type that
serializes as a bare string._

### SID-01 — Tenancy reads stores through the merged converter · [verified]

_Was C1._ Spring Data JDBC needs a `String → StoreMerchantId` reading converter per module; a missing one is
not a compile error, it throws `ConverterNotFoundException` the first time that column is read.

- **Steps** — `POST /tenancy/api/v1/store-manager/list`.
- **Expect** — **200** with the store rows, and `grep -i ConverterNotFound .lcl/<stack>/logs/tenancy.log` empty.

### SID-02 — The JSON contract, including the two older shapes · [unit only]

_Was W1._ The store id now serializes as `"65f0…"`. Its deserializer still accepts the two older object shapes,
because outbox rows and stored event payloads written by the previous release hold them.
`StoreMerchantIdJsonTest` (tenancy-service, 7 cases) serializes to a bare string; reads a bare string,
`{"id":…}`, `{"storeMerchantId":…}` and an explicit null; round-trips a `StoreCreatedEvent`; and reads a
`StoreCreatedEvent` payload stored in the **old** shape.

```bash
./gradlew :store-core:tenancy:tenancy-service:test --tests '*StoreMerchantIdJsonTest*'
```

### SID-03 — Store provisioning end to end, with one freshly minted id · critical · [verified]

_Was R3, the only caller of `StoreMerchantId.newId()`._

- **Steps** — create a store via `POST /tenancy/api/v1/store-manager/private/store`.
- **Expect** — it reaches **`SUCCESSFULLY_PROVISIONING`**, and the one freshly minted id comes back as a
  **bare string** (with `orgId`/`podId` still objects) and lands in all four schemas at once:

  | where | column | type |
  |---|---|---|
  | `tenancy.manager_store` | `id` | `varchar(24)` |
  | `pod_registry.pod_store_placement` | `store_id` | `varchar(24)` |
  | `billing.store_subscription` | `id` | `varchar(24)` |
  | `merchant.merchant_store` | `store_merchant_id` | `varchar(50)` |

  One value, two column widths, no conversion anywhere — which is the whole point of the merge.
- **The payload matters, and the errors are not store-id related.** Merchant rejects a hand-built body until
  `address` is an **object**, the contact fields are `email`/`phone` (not `storeEmailAddress`/`storePhone`), and
  `theme` **and** `colorTheme` are present (both not-null in the DDL; `Theme.BASIS` / `ColorTheme.LIGHT` work).
  Easier to use the Create Store form, which fills all of it.
- **Leftovers** — a QA pass leaves `QA-*` stores in `tenancy.manager_store`, each with placement and billing
  rows. They are inert (the outbox has no pending or failed records); delete them for a clean slate.

### SID-04 — An outbox row written by the previous release is consumed by this one · high · [not verified]

_Was W4._ The unit test covers the deserializer; nothing has exercised a real row. Before deploying, either
drain the outbox or: on the previous release, create a store so `StoreCreatedEvent` is written with the old
shape; stop the stack before the handler runs; switch to this release and restart; confirm the event is handled
rather than landing in `select * from outbox_record where status='FAILED'`.

---

## MIG — Migration

Two migrations, in this order, and **the order is not optional**.

### MIG-01 — Rename with the service stopped · critical · [verified]

`extra/migrations/2026-08-11-rename-control-plane-to-tenancy.sql`, applied with tenancy **down** — it is not
safe against a running old instance. See [RNM-02](#rnm-02--the-migration-preserves-everything--critical--verified)
and [RNM-03](#rnm-03--nothing-recreates-the-old-schemas-on-boot--critical--verified) for what to check after.

### MIG-05 — Drain the outbox before deploying · critical · [not verified]

`StoreCreatedEvent` now carries `CreateStoreRequest` where it carried a `Map`. **Outbox records written by the
old release will not deserialize into the new shape.** Drain the outbox to zero `PENDING` before deploying, or
accept that in-flight store creations fail and are re-driven by the reaper. Draining also makes
[RNM-06](#rnm-06--outbox-continuity-across-the-rename--high--not-verified) moot.

---

> MIG-02, MIG-03 and MIG-04 move pods into the registry — they are [pod-registry-qa.md](../../../pod-registry/pod-registry-service/qa/pod-registry-qa.md).

---

## REG — Regression watchlist

Every item here was a real defect, found by running the thing rather than reading it.

| What broke | How it looked | How to catch it again |
|---|---|---|
| **Another org's store returned in full** | 200 with the complete record, from a guarded-looking endpoint. | SEC-02. And check it is **404**, not 403. |
| **Every store on the platform returned** | A principal with an org claim but neither expected role fell through the scoping branch. | SEC-04, with a caller that is not an org admin. |
| **A leaky error message** | `detail: "No store is visible with id ManagerStoreId[id=65f0…]."` | SEC-02. Read the `detail`, not just the code. |
| **A whole service's web layer 500s** | Any request carrying `?lang=` died in a shared interceptor before reaching a controller. Looked intermittent. | RBS-01. Add `?lang=en` to any store-core request. |
| **Stores vanished from the console** | A slow pod removed rows from the list instead of degrading them; nothing logged. A merchant concludes the store was deleted. | RBS-04. Compare `totalElements` against the rows actually returned. |
| **A typed error replaced by a generic one** | `DataAccessException` attached as a cause: Spring's resolver walks the cause chain, `DataIntegrityErrorHandler` claims it, and the specific code is discarded — while the catch runs correctly. | RBS-02. Assert on the **code**, not just the status. |
| **Every audited change failed** | `String.valueOf(valueObject)` yields `ManagerStoreId[id=…]`, 40-odd chars, overflowing `tenancy_audit.entity_id varchar(24)` — the insert failed and took the change with it, surfacing as a 409 on suspend. | LIF-01 and LIF-06 together. Any value object rendered into a column or a message. |
| **The wrong service blamed during an incident** | A pod-registry timeout reported as *"The billing service could not be reached"* — because `RestClientBuilder` handed every client the **same mutable builder**, interceptors accumulated, and the earliest one wraps the call. | Stop one dependency of a service that builds several clients; confirm the error names **that** dependency. |
| **Queries left pointing at the renamed schema** | Two statistics queries still said `manager.` — both screens would fail on first open. The completeness grep searched for `control-plane`, not the schema name. | Open both statistics screens after any rename. |
| **The service would not start** | A second `OpenAPI` bean made springdoc ambiguous; `@Modifying` on a `select pg_advisory_xact_lock` made Postgres answer *"A result was returned when none was expected"*. | Start the service. Neither is visible from a build. |
| **A created store failed provisioning with no reason** | Tenancy accepted a four-field create body and forwarded the rest untyped; the pod refused it off the outbox on `@NotNull`s and NOT NULL columns. The merchant saw "provisioning", then "failed", and never learned which field was wrong. | CRT-02 |

---

## 99 — Known gaps

**`isOrgAdmin` still ignores the store it is handed.** `StoreRoleAccessChecker.isOrgAdmin` in
`store-commons/autoconfigure` returns `true` for any store on the platform once the caller holds
`ROLE_ORG_ADMIN`, so **every pod service — catalog, checkout, payment, cua, merchant, content — still lets an
org admin manage any store on the platform.** Tenancy closed this at the query layer (§SEC), which is why those
cases pass; the pods have not. This is the largest open item on the whole plan and has its own PR.
`PermissionAccessChecker.hasReadAccessOnStore` never checking `isSuperAdmin` — so a super admin gets 403 on
`store-info` — belongs with it.

**No console screens for the lifecycle features.** Store suspend / archive / delete, org profile, members and
invitations all have endpoints and none have screens. **Invitations most of all**, since the token is displayed
exactly once and the console is supposed to be what shows the link.

**Members are not reconciled with uaa.** Removing a member here does not remove their uaa user, and a user
deleted in uaa leaves a membership row behind.

**Audit and outbox tables grow unbounded** — `tenancy_audit` and the outbox tables. Retention is a
platform-wide job nobody has written.

**`WebClientsUtils`' clone fix has no regression test**, because `store-commons/autoconfigure` has no test
source set at all. Given how silently that one failed, it is worth its own small PR.

**The `Manager*` type names survive the rename by design.** Anyone "finishing" it must sweep every
`hasPermission(…,'ManagerStoreId',…)` string too, or it 403s silently.

**`orgId`, `podId`, plan/price/invoice ids still serialize as `{id: "…"}`.** Only the *store* id became a bare
string. The asymmetry is deliberate.

**`ManagerOrgId` is untouched**, including its real quirk: its `String` constructor yields a null inner
`ObjectId` for malformed input while every sibling throws. `SecurityUtils` relies on that leniency for a
missing `org` claim.

**`OrgInvitationRepository` types its id as the store id**, which looks wrong for an invitation. Pre-existing
and behaviour-preserved; flagged, not fixed.

---

## Automated coverage

Not a substitute for the cases above, but it is what backs the **[unit only]** tags.

| Suite | Tests |
|---|---|
| `InvitationServiceTest` | 9 |
| `StoreTenantScopingTest` | 7 |
| `CachingPodDirectoryTest` | 6 |
| `StoreLifecycleServiceTest` | 6 |
| `StoreProvisioningServiceTest` | 5 |
| `StoreMerchantIdJsonTest` | 7 |

---

Raise anything unexpected against the tenancy PR. Include the store or org id, the time, and the matching lines
from `.lcl/<stack>/logs/tenancy.log` — most of these paths are asynchronous, so the log is usually the only
place the real cause appears.

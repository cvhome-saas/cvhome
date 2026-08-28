# Module 11 — Org & pod management (platform admin)

To be appended as `## Module 11` to
`.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md`, following the per-module
template that document fixes. Module 8 (users & profile) shipped in five commits and has an
unfinished QA pass in the working tree; `lessons.md` stands at 124 entries.

**Queue note.** The named order puts this at `11`, after `9` customers and `10` subscription & usage.
It is being planned now at the user's request; the two skipped modules keep their numbers and their
place in the queue. Nothing here depends on them.

## Context

Everything the console has built so far is one merchant's reading of one store. This module builds
the other product: **the platform console** — the screens that run the SaaS rather than a shop.
`.agents/plans/seller-ui-feature-inventory.md` §1 names the problem exactly: *"two products are
wearing one skin"*, a platform admin console (orgs, pods, platform statistics) and a merchant
console, sharing one sidebar with hidden branches.

Four facts, each verified by reading the code, decide the shape of this module.

### 1. There is no design. At all.

`console-template` has **zero occurrences of "pod"** in any of its 20 `.dc.html` files, and
"Organization" appears only as a nav-group label in the rail. `Admin Dashboard.dc.html` — the
closest-sounding file — is the *merchant* home Module 3 already built: revenue, orders, pending
payments, low stock.

Every module so far had the template to argue with. This one does not. The design source is the
console's own vocabulary as Modules 4–8 settled it: `page-header`, `panel`, `data-table`,
`search-box`, `tab-switcher`, `badge`, `confirm-dialog`, `kpi-card`, `charts`, and Module 8's
dialog. **No new visual language is invented here.** Where a platform screen needs a shape the
console already has, it uses that shape and nothing else.

### 2. The largest never-called backend surface yet, and it is bigger than Module 8's.

- `OrgManagerApi` (`tenancy`, `api/v1/org-manager`) — `find-all`, `find-one`, `create`,
  `change-password`, `stores`, and a full lifecycle: `rename`, `suspend`, `resume`, `close`.
  **seller-ui calls the first five and none of the lifecycle four.**
- `PodApi` (`pod-registry`, `api/v1/pod`) — `list`, paged `findAllPods`, `find` (the rich
  `PodView`), `create`, `update`, `drain`, `resume`, `delete`. seller-ui calls five of eight and
  knows nothing of `drain`, `resume` or `PodView`.
- `AdminUserController` (`uaa`, `api/v1/admin/users`) — platform-wide user CRUD: paged list with
  metadata filters, find, exists, create, update, enable, disable, delete, reset-password, assign
  and remove roles. **Called by no frontend anywhere in the repo.** seller-ui's user management goes
  through tenancy's store-scoped `UserAccountApi` instead.
- `OrgStatisticApi` / `StoreStatisticApi` (`tenancy`, `api/v2/private`) — org and store growth per
  day, super-admin only. Module 3 explicitly declined to port them: *"They feed the platform admin
  dashboard, not this one, and are not ported"* (`statistic.service.ts`).

### 3. A super admin cannot use this console today, and the way it fails is silent.

`requiresStore` holds any account with no store on `/getting-started`, and every console route
except `/profile` and `/accept-invitation` carries it. A super admin is not store-less, though —
`InternalStoreServiceImpl.findAll` reads a null org claim as *platform-wide* and returns **every
store on the platform**, then `visiblePage` clamps an unpaged request to `DEFAULT_PAGE_SIZE`. So
a super admin signing into console-ui today gets the rail's store switcher filled with the first
page of every tenant's stores, silently truncated, and works "inside" whichever one sorts first.
That is not a platform console; it is a merchant console pointed at a stranger's shop.

### 4. Four defects, and one of them is three defects stacked.

1. **Org password change has never worked, in three layers at once.**
   `OrgManagerApi.changePassword` calls `userAccountService.changePassword(id.toString(), …)` —
   passing the **org id** where uaa wants a **user id**. uaa's `AdminUserController.resetPassword`
   declares `@PathVariable UUID id`, and `ManagerOrgId` is a 24-char ObjectId, so the request cannot
   even bind. seller-ui then sends `{password}` while `UserPassword` reads `getChangePassword()`, so
   the password would be null if it arrived. And `ManagerOrgDto.ownerUserId` — the field that would
   carry the right id — **is written by nothing**: `ManagerOrgEntity.createOrgFromUser` sets id,
   createdDate, email and status, and no other code path assigns an owner. The column is null for
   every organization on the platform.
2. **`/uaa/**` is routed nowhere.** `GatewayRouteLocatorImpl` declares routes for `tenancy`,
   `billing` and `pod-registry`, and lists `"uaa"` in `backendServices` — the array that is
   *negated* to build the UI catch-all. So `/uaa/**` is excluded from the console's catch-all and
   given no backend route: it matches nothing and 404s. uaa's own `AppSecurityConfig` already
   accepts `ROLE_SUPER_ADMIN` on `/api/v1/admin/**` and is a JWT resource server, so the admin API
   is reachable-in-principle and unreachable-in-fact.
3. **`subscription-statistic` does not exist.** `grep -rn "subscription-statistic" --include="*.java"`
   returns nothing. seller-ui's admin dashboard has called it since it was written; that chart has
   always been a 404.
4. **`InternalOrgServiceImpl.findOne` ends in `.orElseThrow()`** — a bare `NoSuchElementException`,
   so an unknown org id is a 500 where `OrgNotFoundException` (which exists, and which `rename` and
   the lifecycle methods throw) would give a 404.

### 5. Impersonation does not exist, and the gateway is why it is hard.

`grep -ril "impersonat|act_as|actAs|on-behalf|token-exchange"` across `store-core`, `store-commons`
and `store-pod` returns **zero files**. There is no partial implementation to finish.

It is also not a screen. The console never holds a token: the gateway is an `oauth2Login` client
with a session cookie and a `tokenRelay()` filter on every backend route, so "act as this user"
means *swapping the authorized client held in the gateway's session*, not minting something the
browser can carry. That is an authorization-server and gateway change with an audit obligation
attached, and it graduates out of `lessons.md` into its own requirements document — the precedent
being `console-template/Content Management Service - Backend Requirements.md`.

## Decisions (settled with the user)

- **`/platform/*` inside the existing console shell**, not a separate shell. A `Platform` nav group
  rendered only for super admins; new routes carry `canAccessSecuredPages` + `consoleContext` and a
  new `platformOnly` guard, and deliberately **not** `requiresStore`. The inventory's advice to
  split the two products visually is honoured by the group and the guard, not by a second layout.
- **Scope is the full platform console**: organizations, pods, the platform dashboard, platform user
  management, and audit — plus impersonation, whose backend does not exist and which therefore ships
  in this module as a specification, not a screen.
- **The org-password defect is fixed here**, not logged. Module 8 set that precedent with
  `STORE-CORE.USERS.RESET_PASSWORD`. The gateway's missing `/uaa/**` route rides with it, because
  platform user management cannot exist without it.

## seller-ui today

**Organization management** (`/pages/org-management`, platform only) —
`ORG_MANAGEMENT_ROUTES`: `org-list`, `create-org`, `org/:id`, `org/:id/change-password`,
`org/:id/stores`. The three per-org screens are joined by `ORG_SIDEMENU_LINKS`, the same
select-as-subnavigation the inventory calls the worst pattern in the app. The list is
`ngx-datatable` over id / created date / contact email / an edit button. Create asks firstName,
lastName, emailAddress, password (`PWD_PATTERN`, 6–12 with upper, lower, digit) and a
`subscriptionPlan` the server ignores — `CreateOrgRequest` is `(PersistableUser user)` and has no
plan field. Update binds `email` (disabled) and that same phantom `subscriptionPlan`, and PUTs to
`org-manager/update`, **a path no controller maps**. Change password posts the wrong field to the
wrong id. Org stores is a plain table of the org's stores.

**Pod management** (`/pages/pod-management`, superAdmin only) — `list`, `create-pod`, `pod/:id`.
Table of id / name / endpoint / edit, with a delete on the facade. The form is name (pattern
`validators.podName`), endpoint URL + `EndpointType`, and a free-text `orgId`. Nothing in it knows
about lifecycle, visibility, region, capacity or health, because seller-core's `Pod` interface is
the routing record and stops at `{id, name, shortenPodId, endpoint, orgId}`.

**Admin home** (`/pages`, role-switched) — three ECharts panels driven by one date range:
subscriptions (404), new org joiners, new stores created.

**Platform user management** — none. seller-ui's user management is tenancy's store-scoped list; the
uaa admin API has never had a caller.

**Audit** — none. Neither `tenancy_audit` nor `pod_audit` is read by anything.

**Impersonation** — none.

## API surface to port

New api-tier files, per the standing convention (`src/app/api/<context>/`, `@api/*` alias, provenance
line, doc comments kept, `strict: true`, port only what the module needs):

| File | From | Endpoints |
|---|---|---|
| `api/tenancy/org.service.ts` | `seller-core/orgs/…/org.service.ts` | `find-all`, `find-one`, `create`, `change-password`, `stores`, **+ `rename`, `suspend`, `resume`, `close`** (new, no seller-ui precedent) |
| `api/pod-registry/pod.service.ts` | extend the existing port | **+ paged `findAllPods`, `find` (`PodView`), `create`, `update`, `drain`, `resume`, `delete`** |
| `api/uaa/admin-user.service.ts` | none — new | `GET/POST /uaa/api/v1/admin/users`, `/{id}`, `/exists`, `/{id}/enable`, `/{id}/disable`, `/{id}/reset-password`, `/{id}/roles`, `/{id}/roles/remove`, `DELETE /{id}` |
| `api/analytics/statistic.service.ts` | extend the existing port | **+ `orgStatistic`, `storeStatistic`** on `/tenancy/api/v2/private` |

Wire DTOs go in `@models/`: `models/tenancy.ts` gains `ManagerOrgDto` (`id`, `email`, `createdDate`,
`name`, `status: OrgStatus`, `ownerUserId`), `models/pod.ts` gains `PodView`, `PodVisibility`,
`PodLifecycleState`, `PodHealthStatus`, and a new `models/uaa.ts` carries `UserDto`
(`id`, `username`, `email`, `firstName`, `lastName`, `enabled`, `roles`, `metadata`).

## Deviations

Corrections the port makes, each a real bug in the copy being replaced:

1. **`Org` was missing three fields.** seller-core's interface is `{id, email, createdDate}`;
   `ManagerOrgDto` has carried `name`, `status` and `ownerUserId` since the lifecycle work. The
   ported model is the whole DTO — which is what makes a status column and lifecycle actions
   possible at all.
2. **`updateOrg` is deleted, not ported.** `PUT org-manager/update` maps to nothing. The operation
   that exists is `POST rename?id=&name=`.
3. **`getSubscriptionPlans` is deleted, not ported.** Its own doc comment says a plan belongs to a
   store now, so the create-org and update-org screens were choosing something applied nowhere.
4. **`changeOrgPassword` sends `changePassword`, not `password`** — `UserPassword.getChangePassword()`
   is what the server reads.
5. **`Pod.orgId` is `IdentityId | null`.** A public pod has no owner; the seller-core type says it
   always does.
6. **`models/pod.ts`'s standing comment is wrong and gets corrected.** It says *"region, latency and
   data residency — `PodEntity` carries none of them"*. `PodEntity` carries `region`,
   `capacityMaxStores`, `capacityStores`, `lastHealthStatus` and `lastHealthAt`, and `PodView`
   returns all five. What is missing is a *merchant-readable* endpoint, which is the separate
   lessons entry that comment was pointing at.

## What gets built, block by block

### `/platform` — the platform dashboard

`page-header` with a `date-range-picker`, then a `kpi-grid` of what the two statistic endpoints can
actually total over the range, then two `charts` panels: **new organizations per day**
(`org-statistic`) and **new stores per day** (`store-statistic`). The subscriptions panel of
seller-ui's admin home is **not built** — its endpoint does not exist. Its absence is a lessons
entry, not an empty card.

### `/platform/organizations` — the tenant registry

`data-table` over `find-all`: name (falling back to the contact email when null, which is every org
created so far), contact email, status `badge`, created date, store count — **no store count**, see
gaps; row click opens the detail. A `search-box` is **not** rendered: `find-all` takes a `Pageable`
and nothing else, and a box that filters one page of rows is a lie. Paging is server-side, as
everywhere else in this app.

`Create organization` opens Module 8's dialog with first name, last name, email, password and
confirm. The password rules come from the console's own policy helper — seller-ui's `PWD_PATTERN`
(6–12 chars) is carried over verbatim and its provenance noted, since the platform still has no
published policy (`lessons.md`, "Users — no password policy anywhere").

### `/platform/organizations/:id` — one tenant

A `page-header` naming the org, with the lifecycle actions as the header's action row, and
`section-nav` tabs beneath — the pattern store-management uses, replacing seller-ui's select-as-nav:

- **Overview** — identity (id with `copy-field`, name, contact email, created, status), rename
  inline, and the lifecycle actions: **Suspend** (a `confirm-dialog` that says plainly that every
  store the org owns goes offline, with an optional reason posted as `reason`), **Resume**, and
  **Close** (a confirm that names it as terminal). Illegal transitions come back as
  `IllegalLifecycleTransitionException`; the console renders the server's refusal rather than
  predicting it.
- **Stores** — `data-table` over `org-manager/stores`, paged, read-only: name, status, provisioning
  state, pod, created. No row action — a store is administered by its own org, and nothing on the
  platform side may edit one.
- **Users** — `data-table` over `GET /uaa/api/v1/admin/users?metadata[org]=<id>`. This is the
  answer to Module 8's open gap *"the user list is store-scoped, so an org admin is in no list"*:
  uaa's metadata filter is org-scoped by construction. Enable / disable / reset password / assign
  roles from here, all through the uaa admin API.
- **Activity** — the org's `tenancy_audit` trail. **No endpoint reads that table**, so this tab
  renders the console's `empty-state` with an honest label and a `TODO(lessons.md)`. It is built as
  a tab now because the audit rows exist and the endpoint is a small, well-specified addition.

**Owner password reset** lives on Overview, and is the action the backend change below unblocks.

### `/platform/pods` — the fleet

`data-table` over the paged `GET /pod-registry/api/v1/pod`: name, endpoint + type, owner (org id, or
"Public"), and a shortened id with `copy-field`. **Lifecycle, health and capacity are not columns** —
the paged endpoint returns the routing `Pod`, and `PodView` is per-id only; a column that costs one
request per row is not a column. They appear in the detail panel, which fetches `GET /pod/{id}`.

### `/platform/pods/:id` — one pod

`panel`s: **state** (lifecycle `badge`, health `badge` with `lastHealthAt`, capacity as
`progress-track` — `capacityStores / capacityMaxStores`, "unlimited" when the ceiling is null),
**routing** (endpoint, type, visibility, region), and **actions**:

- **Drain** — stops new placements, keeps the route and the tenants. The confirm says exactly that.
- **Resume** — returns it to rotation.
- **Delete** — behind a typed confirmation, because `PodApi.delete`'s own doc comment says it checks
  nothing and *"orphans every store on it"*. The dialog says so, and says drain is the safe one.

**Edit** (name, endpoint, endpoint type) is a form; **visibility, region, capacity and owner are
rendered disabled** in the console's established "not settable" treatment, because `PodApi.update`
reads only `name` and `endpoint` off the body and ignores the rest. `PROVISIONING → ACTIVE` and
`DECOMMISSIONED` have no endpoint either, so the lifecycle actions are drain and resume and nothing
else. Each of the four is a `TODO(lessons.md)`.

**Create pod** is the same form plus an owning-org picker (which sets `orgId`, from which the server
derives `PRIVATE` visibility) — the one place visibility is settable, once, at creation.

### `/platform/users` — every account on the platform

`data-table` over `GET /uaa/api/v1/admin/users`, paged: username, email, name, roles, enabled, and
the org and store read out of `metadata`. Filters are `metadata[...]` only — **there is no text
search over username or email** in `AdminService.getUsers`, so the console offers an org filter
(fed by the org list) and no search box, and logs the gap. Row actions: enable, disable, reset
password, assign/remove roles, delete — with `SuperAdminImmutableException` surfaced as the server's
own refusal when the target is the platform's own super admin.

**Impersonate** appears here as a disabled row action with an honest label and a `TODO`, pointing at
the requirements document below. It is not hidden: the point of writing the requirement is that the
screen it belongs to already exists.

### Shell changes

- `console-navigation.ts` gains a `shell.nav.group.platform` section — Platform home, Organizations,
  Pods, Users — with a `superAdminOnly` marker on the group.
- `console-sidebar.ts` filters that group through `ConsolePermissions.canAdministerPlatform()`
  (new; mirrors `hasAnyRole('ROLE_SUPER_ADMIN')`, the guard every one of these endpoints carries).
- A new `platformOnly` route guard, redirecting a non-super-admin to `/dashboard` rather than
  rendering a page that will 403 row by row.
- `first-run.guard.ts`: a super admin is never held on `/getting-started`. Today they are not held
  there for the wrong reason — they are handed a page of other tenants' stores. The guard learns to
  let a platform operator through to `/platform` instead, and the store switcher is hidden for an
  account whose store list is the platform's rather than its own. The truncation itself is a
  lessons entry.

## Mapping table — old capability → new location

| seller-ui | console-ui |
|---|---|
| `/pages/org-management/org-list` | `/platform/organizations` |
| `/pages/org-management/create-org` | `/platform/organizations`, create dialog |
| `/pages/org-management/org/:id` (update) | `/platform/organizations/:id` → Overview (rename; the phantom `subscriptionPlan` and the unmapped PUT are dropped) |
| `/pages/org-management/org/:id/change-password` | `/platform/organizations/:id` → Overview, owner password reset |
| `/pages/org-management/org/:id/stores` | `/platform/organizations/:id` → Stores |
| `ORG_SIDEMENU_LINKS` select-as-navigation | `section-nav` tabs |
| `/pages/pod-management/list` | `/platform/pods` |
| `/pages/pod-management/create-pod` | `/platform/pods/new` |
| `/pages/pod-management/pod/:id` | `/platform/pods/:id` |
| pod delete (unconfirmed, on the list) | `/platform/pods/:id`, typed confirmation |
| admin home — new org joiners chart | `/platform`, organizations chart |
| admin home — new stores chart | `/platform`, stores chart |
| admin home — subscriptions chart | **removed** — the endpoint exists in no Java file |
| — | `/platform/organizations/:id` — suspend / resume / close (new; no seller-ui equivalent) |
| — | `/platform/organizations/:id` → Users, and `/platform/users` (new; uaa's admin API has never had a caller) |
| — | `/platform/pods/:id` — drain / resume, health, capacity (new) |

## New components

The console's shared library covers this module. Expected additions, all small:

- `shared/ui/progress-track` already exists (subscription usage) — pod capacity reuses it.
- One new shared piece: **`app-lifecycle-badge`**, if and only if org status, pod lifecycle and pod
  health cannot be expressed by `app-badge` + a tone. Check `badge` first; three new badge tones is
  the cheaper answer and the likely one.
- Module 8's dialog moves to `shared/ui/` if it has not already, since create-org, create-pod and
  the user actions all want it. (It is `features/users/components/user-dialog/` in the working tree
  and uncommitted — see Implementation, step 0.)

No new visual language, per the Context.

## The backend changes

Three, all small, all in the implementation commit, each with a test:

1. **`SignupServiceImpl.createOrgUser` records the owner.** `userAccountService.createUser` returns
   the created `ReadableUser`; its id is written to `manager_org.owner_user_id` before the method
   returns. Plus a one-time backfill for existing rows, resolving each org's owner by
   `metadata[org]` through the uaa admin API — the same filter the Users tab uses.
2. **`OrgManagerApi.changePassword` resolves the owner.** It looks up `ManagerOrgDto.ownerUserId`
   and passes *that* to `userAccountService.changePassword`, throwing `OrgNotFoundException` when
   the org is unknown and a typed error when it has no recorded owner. While there:
   `InternalOrgServiceImpl.findOne`'s `.orElseThrow()` becomes `OrgNotFoundException` so an unknown
   id is a 404.
3. **The gateway routes `/uaa/**`.** One entry in `GatewayRouteLocatorImpl`, the same shape as
   `tenancy`: `stripPrefix(1).tokenRelay().preserveHostHeader()` → `lb://uaa`. uaa's
   `AppSecurityConfig` already gates `/api/v1/admin/**` on `SCOPE_super_admin` or
   `ROLE_SUPER_ADMIN` and is already a JWT resource server, so nothing else moves. Worth stating in
   the commit message: this exposes uaa's admin API to the browser tier for the first time, and its
   guard — not the gateway's — is what keeps it safe.

## What stays unbacked → `lessons.md`

Fourteen entries, each in the fixed format:

1. **Platform — no subscription statistics.** `subscription-statistic` exists in no Java file;
   seller-ui's chart has always 404'd. Contract: `POST /billing/api/v2/private/subscription-statistic`
   taking `StatisticRange`, answering `StatisticList` grouped by day and plan.
2. **Platform — no revenue or GMV figure anywhere.** The dashboard's KPI row can total organizations
   and stores and nothing else; payments has no aggregate endpoint of any kind (Module 7's finding
   stands).
3. **Organizations — the list cannot be searched, filtered or sorted.** `find-all` takes a
   `Pageable`. Contract: a `ListOrgQuery` body mirroring `ListManagerStoreQuery` — name, email,
   status.
4. **Organizations — no store count, user count or plan on the row.** Each is a separate paged call
   today, so the list shows none of them.
5. **Organizations — no audit read.** `tenancy_audit` records ORG / STORE / MEMBER / INVITATION with
   actor, from/to state, source and detail, and nothing exposes it. Contract:
   `GET /tenancy/api/v1/org-manager/{id}/audit` (and a store equivalent), paged, super-admin only.
6. **Organizations — an org cannot be named at creation.** `ManagerOrgEntity.createOrgFromUser`
   sets no name; `rename` is the only way one is ever set.
7. **Organizations — another org's members cannot be listed from tenancy.** `OrgMemberApi` is
   scoped to the caller's own org, so a platform operator reads users through uaa's metadata filter
   instead — which works, and means the console shows uaa's view of a member rather than tenancy's.
8. **Pods — the paged list returns the routing record, not the view.** Lifecycle, visibility,
   region, capacity and health are on `PodView` and reachable only per id. Contract: `Page<PodView>`
   from `GET /pod-registry/api/v1/pod`.
9. **Pods — visibility, region, capacity and owner cannot be edited.** `PodServiceImpl.update`
   reads only `name` and `endpoint` off the body.
10. **Pods — two lifecycle states are unreachable.** `PodLifecycleService` exposes `drain` and
    `resume`; `PROVISIONING → ACTIVE` and `DECOMMISSIONED` have no endpoint, so a newly registered
    pod cannot be marked ready and a retired one cannot be marked retired.
11. **Pods — health history and audit are written and never read.** `pod_health_check` and
    `pod_audit` both have retention and indexes; no endpoint returns either.
12. **Pods — no way to see which stores are on a pod.** `pod_store_placement` is the table that
    would answer it, and drain is the operation that makes the question urgent.
13. **Platform users — no text search.** `AdminService.getUsers` filters on `metadata[...]` only.
14. **Shell — a super admin's store rail is the whole platform, truncated.**
    `InternalStoreServiceImpl.findAll` reads a null org as platform-wide and `visiblePage` clamps an
    unpaged request to `DEFAULT_PAGE_SIZE`. Contract: either a platform-wide store screen with real
    paging, or an explicit refusal for an identity with no org.

And one that graduates out of `lessons.md` into its own document:

**`.agents/requirments/user-impersonation.md`** — support and super admins acting as a merchant to
reproduce what they are reporting. The document must cover: RFC 8693 token exchange in uaa
(`urn:ietf:params:oauth:grant-type:token-exchange`), restricted to `ROLE_SUPER_ADMIN` and
`ROLE_SUPPORT`; an `act` claim naming the real actor so every downstream `@PreAuthorize` and every
audit row can tell an impersonated call from a genuine one; **the gateway problem** — the console
holds a session cookie, not a token, so impersonation means swapping the authorized client in the
gateway's session store and giving that swap its own short TTL and its own end-impersonation
endpoint; an audit row per issuance and per end, in `tenancy_audit` or a new `uaa_audit`; a
non-dismissible banner in the console shell for the whole duration; and the hard exclusions —
impersonation must never mint a refresh token, never survive a browser restart, and never be
available for another platform admin's account. `lessons.md` links to it; the console ships the
disabled action and the `TODO`.

## Implementation

0. **Land Module 8 first.** The working tree carries uncommitted changes to `users`, `profile`,
   `models/team.ts`, both locale files, and an untracked `features/users/components/user-dialog/`.
   This module reuses that dialog. Nothing here starts until that is committed and green.
1. **Api tier + models**, with specs: `org.service.ts`, the `pod.service.ts` extension,
   `admin-user.service.ts`, the statistic extension, and the model additions. Deviations applied.
2. **The three backend changes**, with tests, and the uaa route verified end to end through the
   gateway before any screen binds to it.
3. **Shell**: nav group, `canAdministerPlatform`, `platformOnly`, the first-run correction, the
   store-switcher suppression.
4. **`/platform/organizations`** — list, create dialog, detail with its four tabs, lifecycle actions.
5. **`/platform/pods`** — list, detail, create/edit form, drain/resume/delete.
6. **`/platform/users`** — list, row actions, the disabled impersonate action.
7. **`/platform`** — the dashboard, last, because it is the smallest and the most dependent.
8. **`lessons.md`** (14 entries) and `.agents/requirments/user-impersonation.md`.
9. **i18n**: every string keyed in `en.json` and `ar.json`, exact parity, no hardcoded template
   text — the standing gate.

## Testing

Two tabs, `seller-ui.gateway.com:8000` and `console-ui.gateway.com:8000`, signed in as the
super admin from `store-core/uaa/src/main/resources/init-sql/data-common.sql`:

- **The trap.** Sign into console-ui as super admin *before* the shell change and confirm the
  store-switcher-full-of-strangers behaviour; after, confirm the operator lands on `/platform` and
  the switcher is gone.
- **A non-super-admin sees no Platform group**, and `/platform/organizations` typed by hand
  redirects rather than 403s row by row. Check as org admin, store admin and store moderator.
- **Org list parity** — same rows, same order, same count as seller-ui's `org-list`.
- **Create an org** in the console; confirm it appears in seller-ui's list, and that
  `manager_org.owner_user_id` is now non-null (the backend change).
- **Reset that org owner's password**, then sign in as them. This is the assertion that the
  three-layer defect is actually fixed; seller-ui has no working comparison to make.
- **Suspend an org**, then in a third tab confirm one of its stores refuses to load
  (`requireOperable` reads the org's status), then resume and confirm it recovers. seller-ui cannot
  do this at all.
- **Pod list parity** with seller-ui's; then `GET /pod/{id}` in the detail and check lifecycle,
  health and capacity against `pod_registry.pod` directly.
- **Drain a pod**, then create a store and confirm placement lands elsewhere; resume and confirm it
  becomes a candidate again. Check `pod_audit` gained rows with the right actor.
- **Delete confirmation** — open it, read it, cancel it. Do not delete a populated pod.
- **Platform users** — list, filter by an org's id, disable and re-enable a test account, and
  confirm `SuperAdminImmutableException` surfaces as a readable refusal when the target is the
  platform super admin.
- **Dashboard** — same series as seller-ui's admin home for org joiners and stores over the same
  range; confirm the subscriptions panel is absent rather than empty.
- **Arabic/RTL** on every new screen, and the locale-parity check.

## Commits

Larger than a usual module, so it splits — each still one phase, in the house convention:

1. `plan(console-ui): org and pod management` — this document, appended as Module 11.
2. `feat(console-ui): the platform area, and the organizations behind it` — api tier, shell, orgs.
3. `fix(tenancy,gateway): the org owner nobody recorded, and a uaa API nobody could reach` — the
   three backend changes, with tests. Separate because it is server-side and must be reviewable on
   its own.
4. `feat(console-ui): the pod fleet` — pods, list through drain.
5. `feat(console-ui): every account on the platform` — `/platform/users`.
6. `feat(console-ui): the platform's own numbers` — the dashboard, plus `lessons.md` and the
   impersonation requirements doc.
7. `fix(console-ui): org and pod management after QA`.

## Critical files (Module 11)

**Read before starting:** `OrgManagerApi.java`, `PodApi.java`, `PodServiceImpl.java`,
`PodLifecycleService.java`, `AdminUserController.java`, `AdminService.java`,
`AppSecurityConfig.java`, `GatewayRouteLocatorImpl.java`, `InternalStoreServiceImpl.java`,
`SignupServiceImpl.java`, `ManagerOrgEntity.java`, both `schema.sql` files.

**Written:** `api/tenancy/org.service.ts`, `api/uaa/admin-user.service.ts`,
`api/pod-registry/pod.service.ts`, `models/uaa.ts`, `features/platform/**`,
`layouts/console-shell/console-navigation.ts`, `layouts/console-shell/guards/first-run.guard.ts`,
`shared/auth/console-permissions.ts`, `lessons.md`,
`.agents/requirments/user-impersonation.md`.

## Verification (Module 11)

Lint clean, `tsc` clean, all specs green, en/ar at exact key parity, zero Tailwind palette classes,
zero hardcoded template strings, zero bare `TODO`s (every one carries `(lessons.md)`), and the
gateway route exercised from the browser rather than from a `.http` file.

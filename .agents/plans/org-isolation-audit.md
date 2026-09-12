# Org isolation — who can mint a staff identity, and what a pod accepts

This file is both the audit report and the plan: the findings table is the report, each phase is one commit on
`fix/org-isolation`, and the *status* column says what landed. It extends `authorization-audit.md`, which covered
*which gate an endpoint carries* and never reached *who may create the principal that passes it*. QA cases live in
each owning service's `qa/<service>-qa.md`, never here.

The question asked: a pod assigned to org X accepts org X's tokens only; org Y can neither reach it nor create stores
or manage anything of X's. **As built, it does not hold.** Placement is right; the mint is not, and the pods trust
what the mint produces.

## Context — how a pod decides, as built

There is no org header. A pod service is a resource server that believes the uaa JWT's `roles`, `org` and `store`
claims. uaa copies `org` and `store` from free-form user metadata (`JwtCustomizerConfig` javadoc) that it never
validates, and tenancy is what writes that metadata for store staff. So a pod's isolation is exactly as strong as the
rules on who may create which user with which `org`, `store` and roles.

| Principal | What the pod compares today | Where |
|---|---|---|
| Org admin | pod's org (if configured) + the store's owner via merchant (`ownsTheStore`) | `StoreRoleAccessChecker.java:138-152`, `:74-94` |
| Store admin / moderator | pod's org (if configured) + `store` claim equals `?store=` — **no owner check** | `StoreRoleAccessChecker.java:154-200` |
| `store_core` service | nothing — reads and provisions any store on any pod, by design | `PermissionAccessChecker.java:135`, `:59-73` |
| Pod service | `resource` claim equals this pod's name | `StoreRoleAccessChecker.java:266-290` |
| Shopper | its own pod's cua issuer, `realm` claim equals `?store=` | `StoreRoleAccessChecker.java:232-252` |

"If configured" never happens: `com.asrevo.cvhome.pod-info.pod` carries no `org-id` locally
(`store-pod-lcl-config.yml:43-50`, `lcl.yml:107-111`) or in AWS (cvhome-platform `modules/store-pod/main.tf:75-79`;
`var.pod` has no org, `variables.tf:9-18`), so `isPodAllowOrg` (`StoreRoleAccessChecker.java:216-230`) returns true on
every running pod and the pod-org branch of `hasAccessOnStoreCreate` never runs. The registry does know a pod's org —
but only if a super admin created the pod through `POST /api/v1/pod` with one: the seed never carries an org
(cvhome-platform `modules/store-core/main.tf:102-105`), `PodEntity.newEntity` is the only writer of `org_id`, and
`PodServiceImpl.update` ignores it (`PodServiceImpl.java:104-117`).

What holds, verified: tenancy takes the org for a new store from the token, never the body
(`StoreManagerApi.java:113-120`); the registry confines an org with private pods to them, never hands a shared-pool
org someone's private pod, and a preferred pod cannot escape the candidate set (`PodPlacementService.java:51-101`,
`PodRepository.java:70-73`); org lifecycle writes are super-admin only (`OrgManagerApi`); members and invitations are
scoped by the token's org (`OrgMemberApi`); a foreign store is a 404 on tenancy's store reads, archive and delete
(`InternalStoreServiceImpl.java:218-227`); an org admin of Y is refused on X's store on any pod.

## Findings, severity-ranked

Severity as in `authorization-audit.md`. Public signup hands out `ORG_ADMIN` (`SignUpApi.java:45-49`,
`SignupServiceImpl.java:33,114`), so "an org admin" below means "anyone with an email address".

| Id | Sev | Where | What an attacker can do | Fix | Phase | Status |
|---|---|---|---|---|---|---|
| O1 | **C** | tenancy `ManagedUserAccountServiceImpl.java:83-101`; uaa-client-impl `UserAccountServiceImpl.java:117-155`; sso-core `AdminService.java:229-234` | An org admin — or a store admin, for its own store — creates or updates a user with `roles: ["SUPPORT"]`. Tenancy forwards the body's roles; uaa refuses only `SUPER_ADMIN`. `SUPPORT` reads every org, store and uaa user (`OrgManagerApi.java:84-95,185-189`, `AdminUserController.java:83`) and impersonates any non-platform account with the target's full roles (`ImpersonationExchangeProvider.java:176-184`; read-only mode was removed, `user-impersonation.md:22-37`). Sign up → mint support → impersonate org X's owner → control of org X | Platform roles grantable only by a person (uaa); tenancy grants from a per-caller allow-list | P1, P2 | open |
| O2 | **C** | tenancy `application.yml:24` (`DELEGATED`), `ManagedUserAccountServiceImpl.java:86-87`; `StoreRoleAccessChecker.java:154-200` | Org Y's admin calls `user-account/create?store=<X's store>`. The gate admits any org admin under `DELEGATED`; the service stamps `org=Y, store=<X's store>` without asking who owns it. The new user's token passes every pod check for a store admin, which compares only the `store` claim: full manage on X's catalog, orders, payment settings, customers. Store ids are public (the storefront's PKCE `client_id` is the store id) | Tenancy resolves the store through the org-scoped lookup before any uaa call; pods check store staff against the store's owner | P2, P4 | open |
| O3 | **H** | as O1; `ManagedUserAccountServiceImpl.java:167-180` | Same passthrough: a store admin mints an `ORG_ADMIN` in its own org. Writes check the target's org and store but not its rank, so a store admin can reset the password of, disable or delete any account stamped with its store, whatever roles it holds | Per-caller allow-list for grants; a write needs every role the target holds to be one the caller could grant | P2 | open |
| O4 | **H** | cvhome-platform `modules/store-pod/main.tf:75-79`; `store-pod-lcl-config.yml:43-50`; `PodSeedInitializer.java:85-99` | A pod never learns it is dedicated, so it accepts every org's tokens; only placement keeps other orgs' stores off it. The registry and the pod read the org from different places, and nothing reports when they disagree | One declaration feeds both; the pod binds `pod-info.pod.org-id`; the seed reports a disagreement | P5, platform | open |
| O5 | **M** | tenancy `UserAccountApi.java:74-78`; `UserAccountServiceImpl.java:271-277` | `assignable-roles` answers uaa's table minus `USER` and `ORG_ADMIN` — `SUPER_ADMIN` and `SUPPORT` included — to anyone authenticated. Known since `store-core/console-ui/lessons.md:2293`; the console intersects with `OFFERABLE_ROLES` (`store-core/console-ui/src/app/models/team.ts:35`) as defence in depth | Answer the caller's own allow-list | P2 | open |
| O6 | **L** | `PodApi.java:84-86`, `InternalStoreServiceImpl.java:157-159`; `SecurityUtils.java:154-163` | A staff token with a role but no usable `org` claim parses to an org with a null id, and both sites read that as platform-wide: every pod, every store on the platform. Only reachable if uaa issues such a token (a super admin creating staff without metadata) | Platform-wide by role, never by a missing org | P3 | open |

**Not findings — recorded so nobody re-opens them.** `SUPPORT` being platform-wide is its design (`user-impersonation.md`);
the fix is who may become support, not what support may do. The shared `store_core` client reading and provisioning
any store on any pod is already on the by-design register in `authorization-audit.md`.

## Decisions to confirm before Phase 2

1. **`ORG_ADMIN` is not grantable through `user-account/*`.** That endpoint is store-scoped and stamps a store on
   every account it makes; an org co-owner is an org-level identity. Nothing in the console offers it today
   (`OFFERABLE_ROLES` = `STORE_MODERATOR`, `STORE_ADMIN`). A real co-owner flow belongs to `OrgMemberApi` and is out
   of scope here.
2. **The grantable set is `STORE_ADMIN`, `STORE_MODERATOR`, `STORE_RETAIL`** for both org admins and store admins. A
   store admin granting `STORE_ADMIN` on its own store makes a peer, not a superior.
3. **Making an existing shared pod dedicated is out of scope.** It is only safe for a pod that hosts no other org's
   stores; P5 reports the disagreement rather than resolving it.

## Why the design is what it is

- **Fix the mint and the check, not one of them.** The mint (tenancy, uaa) is the root cause: every service believes
  a token uaa issued. The pod check is the second layer, because uaa metadata stays writable by any `super_admin`
  caller and O2 users may already exist.
- **Tenancy stays `DELEGATED`.** Its answer to a foreign store is 404, not 403 — a 403 confirms the id exists. So the
  ownership check goes into the service, through `InternalStoreService.findStore(identity, store)`, which already
  answers a foreign store with `StoreNotFoundException` (`InternalStoreServiceImpl.java:218-227`). Reuse, no new query.
- **The allow-list lives in tenancy, keyed by the caller's role.** Tenancy knows who is asking; uaa sees one
  `admin-sdk` client for every caller and cannot. The console's `OFFERABLE_ROLES` stays as defence in depth.
- **A write needs the caller to outrank the target.** "Every role the target holds is one the caller could grant."
  Without it the grant rule is bypassed by editing an account that already holds more.
- **uaa: platform roles are granted by a person.** `SUPER_ADMIN` stays never assignable through the API (uaa-qa
  SEC-15). `SUPPORT` becomes assignable only when the caller is a signed-in user — the `ADMIN` gate
  (`AdminUserController.java:77`) makes that a super admin — and never a client-credentials caller.
  `AuditActorResolver.current()` already says `USER` or `CLIENT`. Every grant path (create, update, assign, invite via
  `InvitationService.invite` → `createAccount`) funnels through `AdminService.assignableRole`, so there is one place to
  change. `PLATFORM_ROLES` moves from `ImpersonationExchangeProvider.java:98` to `UaaConstants` and both use it.
- **Pods check store staff against the owner, the way they already check org admins.** `ownsTheStore` runs after the
  `store`-claim match in `isStoreAdmin` and `isStoreModerator`. Every pod service already has the lookup: merchant
  through its own `ExternalMerchantStoreApi`, content, catalog, inventory, checkout, payment and cua through an
  `ExternalMerchantStoreService` bean, which `MerchantStoreOrgOwnerAutoConfiguration` turns into the retriever. Answers
  are cached 30 minutes per store (`MerchantStoreOrgOwner.java:34`). Tenancy and billing are `DELEGATED` and
  unchanged. The cost: with merchant unreachable and the cache cold, store staff are refused, as org admins already
  are. A check that cannot be made has not passed.
- **A dedicated pod learns its org from the same declaration the registry is seeded from.** `pod-info.pod.org-id`
  already binds to `ManagerOrgId` through its String constructor, as `pod.id` binds to `PodId`. No converter, no code
  on the pod. A pod asking the registry at runtime was rejected: it would make every pod depend on store-core to
  authorize a request. A pod's org is fixed at creation (`PodServiceImpl.update` ignores it, correctly — moving a pod
  strands its stores), so a configuration that disagrees with the stored row is an operator error to report, not to
  apply.
- **Platform-wide is a role, not an absence.** `UserOrgStoreIdentity.isPlatformWide()` — super admin or
  `SCOPE_STORE_CORE` — replaces two copies of `org() == null || org().id() == null`.

## Phase 1 — uaa: platform roles are granted by a person (commit 1)

`store-commons/sso/sso-core`: `UaaConstants.PLATFORM_ROLES = {SUPER_ADMIN, SUPPORT}`, used by
`ImpersonationExchangeProvider` (replacing its own set) and by `AdminService`. `assignableRole(name)` keeps refusing
`SUPER_ADMIN` always, and refuses `SUPPORT` when `AuditActorResolver.current()` is a `CLIENT`, with the existing
`RoleNotAssignableException` (403 `UAA.ROLE.NOT_ASSIGNABLE`). `getAssignableRoles()` leaves out every platform role
for a `CLIENT` caller.

- Tests: `AdminServiceTest` — client grants `SUPPORT` on create, on update and on `assignRoles` → refused, no row
  written; a signed-in super admin grants `SUPPORT` → granted; invite with `SUPPORT` from a client → refused;
  `getAssignableRoles` by caller type. The admin-user API integration suite: `admin-sdk` token + `SUPPORT` → 403.
- `.http`: a refusal block in `store-core/uaa/http/admin-user-api.http`.
- QA: `uaa-qa.md` — SEC-15 gains the client-grants-`SUPPORT` line; a new SEC-18 "Platform roles are granted by a
  person".
- Gates: `:store-commons:sso:sso-core:test`, `:store-core:uaa:integrationTest`, checkstyle.

## Phase 2 — tenancy: user writes stay inside the caller's store and rank (commit 2)

`ManagedUserAccountServiceImpl` gains `InternalStoreService`. Every method — `list`, `findOne`, `createUser`,
`updateUser`, `resetPassword`, `deleteUser`, `enableUser`, `disableUser` — first calls
`findStore(identity, store)`; a foreign store is the existing `StoreNotFoundException` (404) and nothing reaches uaa.
Then:

- **Grant rule** (`createUser`, `updateUser`): every requested role must be in `grantable(identity)` — `STORE_ADMIN`,
  `STORE_MODERATOR`, `STORE_RETAIL` for an org admin or a store admin, nothing for anyone else. Otherwise a new
  `RoleNotGrantableException` (`TenancyErrors.USER_ROLE_NOT_GRANTABLE`, `CONTROL_PLANE.USER.ROLE_NOT_GRANTABLE`,
  `FORBIDDEN`), shaped like `ForeignOrgUserAccessException`.
- **Rank rule** (`updateUser`, `resetPassword`, `deleteUser`, `enableUser`, `disableUser`): after
  `validateUserAccess`, every role the target holds must be in `grantable(identity)`; otherwise the same exception.
- `assignableRoles()` answers `grantable(identity)` intersected with uaa's roles, and stays on the handler walk's
  `AUTHENTICATED_ONLY` list (`authorization-audit.md`, P3 deviations): the answer is scoped by the caller, so a token
  would be decoration. It takes `@OrgStorePrincipalInfo`.

Tests: the audit's `OrgUserEscalationTest` cases fold into `ManagedUserAccountServiceImplTest` (foreign store 404
before uaa; `SUPPORT`/`ORG_ADMIN` refused on create and update; a store admin cannot reset an `ORG_ADMIN` stamped
with its store; the peer grant is allowed); the new file is then deleted. `UserAccountApiIntegrationTest`: org 2 admin
`create?store=<org 1 store>` → 404; `SUPPORT` → 403 `CONTROL_PLANE.USER.ROLE_NOT_GRANTABLE`; own store
`STORE_MODERATOR` → 200; `assignable-roles` for an org admin → exactly the three store roles.

- `.http`: refusal blocks in `store-core/tenancy/tenancy-service/http/user-account-api.http`.
- QA: `tenancy-qa.md` §PERM — PERM-05 "A user cannot be created on another org's store" (critical), PERM-06 "No
  caller grants `SUPPORT` or `ORG_ADMIN`" (critical), PERM-07 "A store admin cannot act on an account that outranks
  it" (high), PERM-08 "`assignable-roles` answers what this caller may grant" (high).
- Gates: `:store-core:tenancy:tenancy-service:test integrationTest`, checkstyle. The console needs no change: it
  offers a subset of the new answer.

## Phase 3 — platform-wide is a role, not a missing org (commit 3)

`UserOrgStoreIdentity.isPlatformWide()` in `store-commons/commons`. `PodApi` (`listPods`, `findAllPods`) and
`InternalStoreServiceImpl.findAll` use it; an identity that is neither platform-wide nor carries an org id gets an
explicit empty answer — never a query with a null org parameter.

- Tests: identity unit test; `PodApi` and `InternalStoreServiceImplTest` — a store admin token with no `org` → empty;
  super admin and `store_core` → everything.
- QA: `tenancy-qa.md` SEC-04 and `pod-registry-qa.md` PDR-06 gain the no-org line, `[unit only]` — `test-stores`
  seeds no such account.
- Gates: `:store-commons:commons:test`, `:store-core:pod-registry:pod-registry-service:test`, tenancy `:test`.

## Phase 4 — pods: store staff belong to the store's owner (commit 4)

`StoreRoleAccessChecker.isStoreAdmin` and `isStoreModerator` end with `ownsTheStore(identity.org(), requestedStoreId)`,
exactly as `isOrgAdmin` does. `DELEGATED` services are unaffected by construction.

The blast radius is the tests, not the code. Fourteen integration-test files in merchant, content, catalog, inventory,
checkout and payment sign store-staff tokens, and only catalog and checkout stub the owner lookup:

- `store-commons/test-support` `Tokens.staff(role, store)` derives the org from the store — `STORE_1`/`STORE_2` →
  `ORG_1`, `STORE_3`/`STORE_4` → `ORG_2`, the ownership `data-test-stores.sql` seeds in uaa — instead of always
  `ORG_1` (`Tokens.java:85-87`).
- A `StoreOwners` test configuration in test-support: a `StoreOrgOwnerRetriever` over the same map, which makes
  `MerchantStoreOrgOwnerAutoConfiguration` back off (`@ConditionalOnMissingBean`). Content, inventory and payment
  import it; catalog's and checkout's `ExternalClientsTestConfiguration` stubs answer the owner by store. Merchant
  answers from its own rows, so its two suites need their stores seeded with the owning org — check before assuming.
- Unit: the audit's `OrgPodIsolationTest` cases fold into `StoreRoleAccessCheckerTest.StoreStaff` and
  `PermissionAccessCheckerTest` (another org's store admin and moderator refused on a shared pod; the owning org's
  staff admitted; a store with an unknown owner refuses staff), and the file is deleted. Fixtures there that pair a
  store with an org that does not own it are corrected.
- Integration: catalog and merchant each gain "a store admin whose `org` claim does not own the store → 403".
- QA: `catalog-qa.md` SEC-03 and `merchant-qa.md` SEC-02 gain the minted-token case — the token tenancy could mint
  before P2 — cross-referencing `tenancy-qa.md` PERM-05.
- Gates: `:store-commons:autoconfigure:test`, `:store-commons:test-support:test`, then `integrationTest` for merchant,
  content, catalog, inventory, checkout, payment and cua with Docker.

## Phase 5 — a dedicated pod knows its org (commit 5)

- `store-pod-lcl-config.yml`: document `pod-info.pod.org-id` beside its siblings — unset means shared, which lcl stays
  (one pod, both demo orgs).
- A binding test in autoconfigure: `com.asrevo.cvhome.pod-info.pod.org-id` and `com.asrevo.cvhome.pods[0].org-id`
  bind to `ManagerOrgId`, in both the property spelling and the environment spelling the platform will use
  (`COM_ASREVO_CVHOME_POD-INFO_POD_ORG-ID`, `COM_ASREVO_CVHOME_PODS[0]_ORG-ID`).
- `PodSeedInitializer.reconcile`: when a configured pod's org differs from the stored row's, log an error naming both
  and keep the stored value. `newEntity` already takes the configured org for a new row.
- Integration (catalog): with `pod-info.pod.org-id = ORG_1`, org 2's store admin on org 2's own store → 403, org 1's →
  200, a `store_core` principal → 200, a shopper's public read unaffected.
- QA: `catalog-qa.md` SEC-06 "A dedicated pod refuses every other org's staff" (critical); `pod-registry-qa.md`
  PLC-07 "Configuration and registry disagree on a pod's org". Locally: a `--stack` whose pod services get
  `COM_ASREVO_CVHOME_POD-INFO_POD_ORG-ID` for org 1 — org 2's logins then fail on org 2's stores, which is the point.
- Gates: autoconfigure `:test`, pod-registry `:test`, catalog `integrationTest`.

## Phase 6 — docs and the clean-up runbook (commit 6)

- `.agents/skills/project-structure/references/authentication.md` principal table: store staff are checked against
  the store's owner; who may grant which role. `multi-tenancy.md`: how a pod learns it is dedicated, and the isolation
  table. Check the `.claude/skills/project-structure` copy (authorization-audit A16, deferred).
- `store-core/console-ui/lessons.md:2293` closed; the `store-core/console-ui/src/app/models/team.ts:25-35` comment
  says the server now answers per caller and the intersection stays as defence in depth; `uaa-qa.md` ACC-02
  rewritten to match.
- `authorization-audit.md` gains one line pointing at this register.

## Other repos (handed to the orchestrator)

- **cvhome-platform** — the pods declaration gains an optional `org_id`, fed from the one declaration to both the pod
  (`COM_ASREVO_CVHOME_POD-INFO_POD_ORG-ID`, spelled like `modules/store-pod/main.tf:75-79`) and store-core
  (`COM_ASREVO_CVHOME_PODS[n]_ORG-ID`, like `modules/store-core/main.tf:102-105`). Unset for every current pod, so it
  changes nothing until a pod is declared dedicated. The property already binds on today's images, so the order is
  free; ship after P5 so a disagreement is reported.
- **load-testing** — reads `user-account/list` for its own stores (`k6/lib/clients/tenancy.js:75`); unaffected by P2.
- **lcl, e2e-testing, image repos** — nothing.

## Clean-up after the release (operational)

O1 and O2 may already have been used. Once P1, P2 and P4 are deployed:

1. `SUPPORT` granted by a client: `uaa.audit_events` where `event_type in ('user.created', 'user.role.assigned')`,
   `actor_type = 'CLIENT'` and `after_json` names `SUPPORT`. Disable each account, then read its
   `user.impersonation.started` rows for whom it acted as.
2. `ORG_ADMIN` holders whose metadata has a `store` key — signup never writes one, only the store-scoped endpoint does.
3. Store staff whose `store` is owned by another org: uaa users' `(metadata->>'org', metadata->>'store')` against
   tenancy `manager_store (id, org_id)`. After P4 these accounts are refused everywhere; disable them anyway.

## Deviations, as built

Filled in while implementing.

## Verification

- Audit (2026-09-12), before any fix, each case asserting the secure behaviour so a red case is a verified hole:
  `OrgPodIsolationTest` (autoconfigure) 5 green, 2 red — another org's store admin and moderator admitted on a shared
  pod; `OrgUserEscalationTest` (tenancy) 2 green, 5 red — user created on another org's store, `SUPPORT` granted on
  create and on update, `ORG_ADMIN` minted by a store admin; `PodPlacementServiceTest` 10 green. The uaa half of O1
  (roles forwarded, claims copied from metadata, impersonation rules) is verified by reading the code, not against a
  running stack.
- Per phase: the gates named in it, then `extra/scripts/verify-before-push.sh` for the receipt. End to end on this
  worktree's own `lcl --stack org-isolation` before the PR: O1 (a store admin tries to mint `SUPPORT` → 403) and O2
  (org 2 admin tries `create?store=<org 1 store>` → 404; a pre-P2 minted token → 403 on catalog).

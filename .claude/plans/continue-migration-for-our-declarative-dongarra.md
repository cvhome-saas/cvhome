# Module 8 — Users & profile

To be appended as `## Module 8` to
`.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md`, following the per-module
template that document fixes. Module 7 (payments) shipped in five commits; `lessons.md` stands at
111 entries.

## Context

The console has no way to add a colleague. `shell.nav.item.userManagement` is routeless — this
app's marker for "not built" — and the toolbar's profile menu ends in two `<button>` elements with
no click handler and no route (`console-toolbar.ts:142-143`). Every seller who needs a second
operator has to go back to seller-ui.

Behind that gap sits the **largest working backend surface any module has found so far, and the
only one no frontend has ever called at all**:

- `UserAccountApi` (`tenancy`, `api/v1/user-account`) — `list`, `find-one`, `assignable-roles`,
  `create`, `update`, `reset`, `delete`, `enable`, `disable`. seller-ui uses most of it.
- `OrgMemberApi` (`tenancy`, `api/v1/org-member`) — members, and a complete invitation flow:
  `invite`, `resend`, `revoke`, `accept`, tested (`InvitationServiceTest`), audited
  (`tenancy_audit`), QA-verified (`qa/tenancy-and-pod-registry-split.md`, LIF-04), and **called by
  nothing**. seller-ui has no invitations at all.

So this module is not a fixture swap and not a parity port. It is the first module where the
console *overtakes* seller-ui, and the shape it takes is decided by four defects that reading the
backend turned up.

### The four findings that shape this module

1. **`STORE-CORE.USERS.RESET_PASSWORD` is mapped nowhere.** It appears in exactly one file in the
   repo — the `@PreAuthorize` on `UserAccountApi.resetPassword` itself.
   `CustomPermissionEvaluator.hasStoreCorePermission` wires the other six `USERS.*` tokens and
   falls through billing and pod-registry to `default -> false`, so **`POST /user-account/reset`
   is 403 for every caller, super admin included.** seller-ui never noticed because its
   change-password screen points at `PATCH /v1/private/user/{id}/password`, a URL that has never
   existed.
2. **The JWT `sub` is the username, not a user id.** `JwtCustomizerConfig` adds `roles` and spreads
   `users.metadata` (`org`, `store`) into the **access** token, and adds no id claim; Spring
   Authorization Server sets `sub` from `principal.getName()`. Meanwhile every read-by-id path ends
   at `AdminUserClient.getUser(id)` → `GET {uaa}/api/v1/admin/users/{uuid}`, and uaa exposes no
   get-by-username. **The console cannot look itself up.** This is also why fixing
   `user-account/current`'s `Principal`/`Jwt` binding would not be enough on its own — it would
   turn a 500 into a 404.
3. **The user list is store-scoped, so an org admin is invisible in it.**
   `ManagedUserAccountServiceImpl.list` filters uaa on `{org, store}` and `validateUserAccess`
   rejects any user whose metadata `store` differs from the requested one. `org1-admin` carries
   `{"org": …}` and no store (`uaa/init-sql/data-test-stores.sql`), so the org's own admin appears
   in no store's list and cannot be read by `find-one` under any store — including by themselves.
4. **`assignable-roles` offers `SUPER_ADMIN` to an org admin.**
   `UserAccountServiceImpl.getAssignableRoles` filters out `USER` and `ORG_ADMIN` only, and the
   seeded set is `{SUPER_ADMIN, USER, ORG_ADMIN, STORE_ADMIN, STORE_MODERATOR}`. A role picker fed
   straight from that endpoint offers platform superuser.

## Decisions (settled with the user)

| Question | Decision |
|---|---|
| Scope | **Both pages.** `features/users/` → `/users` (fills the routeless nav item) and `features/profile/` → `/profile` (fills the dead toolbar button). |
| Invitations | **Built in full**, including the accept route. The console gains a capability seller-ui never had, at no backend cost. Nothing emails the invite — the API returns the token once, so the console shows a copyable link. |
| The `RESET_PASSWORD` permission gap | **Fixed, not just logged** — a deliberate departure from the standing "logged, not fixed" convention, in `store-commons/autoconfigure` (not `store-pod`, not `seller-ui`). One `case`, one unit test, its own commit. |
| The signed-in user's identity | **Username only.** `/profile` does not call `find-one`. Finding 2 makes that not merely conservative but correct: with `sub` a username, the lookup 404s; and for an org admin finding 3 would 403 it anyway. |

**A consequence of the second and third decisions together, stated plainly.** The permission fix
makes `reset` work, but it cannot make it work *on `/profile`*: a self-service change-password needs
the caller's own user id, and finding 2 says the console does not have one. What the fix buys is the
**admin reset on `/users`** — where the row carries a real `ReadableUser.id` — which is exactly the
operation seller-ui's change-password screen was trying and failing to perform. `/profile` gets a
`lessons.md` entry instead of a password field.

## seller-ui today

`src/app/pages/user-management/`, guarded only by `canAccessSecuredPages`; access control is
menu-visibility (`pages-menu.ts:24` — `isOrgAdmin || isStoreAdmin`) plus the backend `@PreAuthorize`.
`.agents/plans/seller-ui-feature-inventory.md` §9 maps it.

| Screen | Route | Works? |
|---|---|---|
| Users list | `users` | yes — `userName`, `emailAddress`, `active`, actions. **No filters at all.** |
| Create user | `create-user/:store` | yes |
| User details | `user/:id` | yes |
| Change password | `change-password/:id` | **no** — see below |
| My profile | `profile` | **no** — see below |

Six defects in it that the port does not carry across:

- **Change password targets an endpoint that does not exist** (`PATCH /v1/private/user/{id}/password`;
  the source comment already admits "No matching controller was found"), and it passes
  `getUserId()`, which reads `localStorage['userId']` — a key **nothing in the codebase ever
  writes**. So it sends `null` as the subject and ignores its own `:id` route param.
- **My profile is not read-only.** `user-profile.component.html` writes `action="'VIEW'"` as a
  string attribute rather than a binding, so `action` is the literal `"'VIEW'"` and every
  `=== 'VIEW'` check fails: the form and its role checkboxes are editable on the profile screen.
- `change-password-form.service.ts` passes `{validator: …}` where Angular reads `{validators: …}`,
  so its `notSame` error never fires and the template's `hasError('notSame')` is dead.
- `UserManagementComponent.ngDoCheck()` reads `window.location.hash` on every change-detection tick
  to compute a side menu the template never renders.
- Untranslated English literals: the delete dialog's `'Are you sure!'` / `'Do you really want to
  remove this entity?'`, and `Repeat Password *`.
- The inline status checkbox mutates `rowData.active` in place rather than reloading.

## API surface to port

A new `src/app/api/tenancy/user-account.service.ts` and `src/app/api/tenancy/org-member.service.ts`,
joining the existing `api/tenancy/`. `?store=` is stamped by `CrudService` via `REQUEST_CONTEXT` —
**callers never pass it**, which is one of seller-core's bugs not carried over.

| New file | Endpoints |
|---|---|
| `api/tenancy/user-account.service.ts` | `GET /tenancy/api/v1/user-account/list` (`page`, `count`) · `GET …/find-one?userId=` · `GET …/assignable-roles` · `POST …/create` · `PUT …/update` · `POST …/reset?userId=` · `DELETE …/delete?userId=` · `POST …/enable?userId=` · `POST …/disable?userId=` |
| `api/tenancy/org-member.service.ts` | `GET /tenancy/api/v1/org-member/list` · `DELETE …/org-member?userId=` · `GET …/invitations` · `POST …/invitations?email=&role=` · `POST …/invitations/resend?email=&role=` · `POST …/invitations/revoke?invitationId=` · `POST …/invitations/accept?token=` |

**Not ported:** `user-account/current` (finding 2 — a 500 today and a 404 after the obvious fix),
and `UserService.updatePassword` (the URL does not exist).

Wire DTOs → **`models/users.ts`** (new): `ReadableUser`, `PersistableUser`, `UserPassword`,
`OrgMember`, `Invitation`, `CreatedInvitation`, `InvitationStatus`. View models →
**`models/team.ts`** (new): `TeamRow`, `InvitationRow`, `UsersTab`, `USERS_TABS`, `RoleName`,
`ROLE_LABEL_KEY`. The `checkout.ts`/`orders.ts` split Module 4 established.

**`src/app/models/user.ts` is deleted.** It is a stub from the Module 1 port that nothing imports,
and its `groups?` field describes a concept the platform does not have.

### Deviations

Each is a real defect found by porting under `strict: true`, per the standing rule.

1. **`lastAccess` and `loginTime` are dead fields.** They are on `ReadableUser` and set by no
   mapper; uaa has no `last_login` column. Not ported — which removes the template's "Last active".
2. **`defaultLanguage` is dead the same way.** On `UserEntity`, no uaa column, never read or
   written. Not ported.
3. **`UserPassword.password` is ignored.** `UserAccountServiceImpl.changePassword` sends only
   `changePassword` to uaa's admin reset; there is no current-password verification anywhere in the
   platform. The console therefore does **not** ask for a current password — a field nothing checks
   is a fixture standing in for a real answer.
4. **`repeatPassword` is never checked server-side.** Confirmed client-side only, as
   `lessons.md` "Auth — public signup validates nothing" already records for the sibling path.
   `@shared/validators/passwords-match` is reused rather than a fourth copy of `checkPasswords`.
5. **`createUser` is two calls, not one.** `client.createUser(...)` then
   `client.resetPassword(...)`. If the second fails the first has already committed, leaving a user
   with no password and no way to set one until `reset` works. Named in `lessons.md`.
6. **`enable`/`disable` post an `undefined` body** in seller-core, and pass `store` explicitly where
   `CrudService` then overwrites it. The port sends neither.
7. **seller-core's user models are optional on every field.** Hardened: `id`, `userName`,
   `emailAddress`, `active` and `roles` are required where the server always sends them; `org` and
   `store` are `string | null`, because a user with no store is exactly finding 3.
8. **`count`, not `size` — verified.** `tenancy-service/build.gradle:63` depends on
   `store-commons:autoconfigure`, whose `ServletWebConfig:38` registers
   `setSizeParameterName("count")`, and `ManagedUserAccountServiceImpl.list` forwards
   `pageable.getPageSize()` to uaa intact. seller-ui sends `count` while rendering `limit: 10` and
   its author assumed it was dropped; it is not. Still the **first thing to confirm in the network
   tab**, as Module 7 did.

## What gets built, block by block

### `/users` — from `console-template/User Management.dc.html`

The template merges staff and customers into one table. **The customer half is Module 9** and is out
of scope; `shell.nav.item.customers` stays routeless. What is built is the team half.

`app-page-header` + a two-tab `app-tab-switcher` (Team · Invitations), tab and page state in the
URL per the page contract, master–detail as the template draws it: the list on the left, a detail
rail on the right (`.split` from `@shared/styles/field.css`), the selected user carried as `?user=`.

| Block | Backing |
|---|---|
| Team table: user (name + email), username, roles, status, actions | **real** — `GET …/user-account/list` |
| Paging | **real** — `page`, `count` |
| Enable / disable a user | **real** — the two POSTs |
| Delete a user, behind `app-confirm-dialog` | **real** |
| Create and edit a user in the detail rail | **real** — `POST …/create`, `PUT …/update` |
| Reset a user's password | **real once the permission `case` lands** — `POST …/reset?userId=` |
| Role picker | **real** — `GET …/assignable-roles`, minus `SUPER_ADMIN` (finding 4) |
| Invitations tab: email, role, status, expires, created by | **real** — `GET …/org-member/invitations` |
| Invite, resend, revoke | **real** — the three POSTs |
| The one-time invitation link | **real** — `CreatedInvitationDto.token`, shown once in a dialog with `app-copy-field` |
| KPI tiles: team members, pending invitations | **real, derived** — `totalElements`, and a count of `PENDING` |
| KPI tiles: customers, orders placed | Module 9 / no revenue → **removed** |
| KPI tile: suspended | **real** — but only as a count of `active: false` on the current page, so **removed** rather than reported from a partial page |
| Search box ("Name or email") | uaa filters on metadata equality only → **removed** |
| Export CSV | no endpoint → **PDF instead**, via the existing `app-export-button`, as Orders and Payments resolved it |
| "Last active" column | dead field → **removed** (Deviation 1) |
| Avatar photo, phone, addresses, lifetime value, "148 actions", recent order activity | no source → **removed** |
| "Store access — All 3 stores" | a user carries **one** store in metadata → the rail shows that one store |

**Two things the rail says that the template does not.** A user whose `store` is null is an
org-level account the store-scoped list cannot show (finding 3) — the tab carries an
`app-notice-bar` saying the list is scoped to the open store. And the roles column is rendered
through the Module 4 known-set guard (`@shared/i18n/status-label`'s pattern, mirrored as
`ROLE_LABEL_KEY` in `models/team.ts`), so a role name the console has not seen humanizes instead of
taking the page down under Transloco's strict-missing handler.

### `/profile` — from `console-template/Account Profile.dc.html`

Three of the design's six tabs have a subject the console can name; two of those have no data.
What is built is small and true.

| Panel | Backing |
|---|---|
| Account: username, roles | **real** — `/api/v1/auth/me`, the only identity there is |
| A notice naming why the rest is blank, citing `lessons.md` | — |
| Preferences: interface language, theme | **real and client-side** — the existing `LocaleService` and theme service, today reachable only from the toolbar |
| Link to Billing | **real** — routes to the existing `/subscription` |
| Full name, job title, email, phone, timezone, date format, bio, avatar | no column, no endpoint → **removed** |
| Notifications (4 toggles) | no service → **removed**, cross-referencing "Shell — no notifications service" |
| Password "Change" | no user id for a self-reset (finding 2) → **removed**, with the reset living on `/users` |
| Two-factor, active sessions | zero backend hits → **removed** |
| Organization tab | `org-manager/find-one` is super-admin only → **removed**, cross-referencing "Shell — an org admin cannot read its own organization" |
| Subscriptions, usage, payment methods, invoices | already shipped at `/subscription` → **linked, not duplicated** |

The toolbar's `Profile` button routes to `/profile`. **The `Settings` button beside it is removed** —
there is no console-wide settings page and never will be, since settings are per-store and live at
`/store-management`. A button that goes nowhere is the same promise a bell with no feed was.

### `/accept-invitation` — no template

`?token=`, under `AuthShell` rather than `ConsoleShell`, guarded by `canAccessSecuredPages` **only**:
an invitee is authenticated but not yet a member of the org, so `consoleContext` and `requiresStore`
would both refuse them — which is precisely why `OrgMemberApi.accept` carries no `@PreAuthorize`
either. On success it routes to `/dashboard`; on `INVITATION.NOT_USABLE` it says the link has been
used or has expired, and offers to ask the inviter for a new one.

## Mapping table — old capability → new location

| seller-ui | console-ui |
|---|---|
| `user-management/users` | `/users`, Team tab |
| `user-management/create-user/:store` | The detail rail in create mode |
| `user-management/user/:id` | The detail rail, `?user={id}` |
| Inline `active` checkbox that mutates the row | `app-toggle`, reloading rather than echoing |
| Delete with an untranslated English dialog | `app-confirm-dialog`, translated |
| `user-management/change-password/:id` | The rail's **Reset password** action — the operation that screen was trying to perform |
| `user-management/profile` (editable by accident) | `/profile`, genuinely read-only |
| `nb-select` sub-navigation | `app-tab-switcher`, per the feature inventory's "replace with real tabs" |
| Role checkboxes fed straight from `assignable-roles` | Same, minus `SUPER_ADMIN` |
| — (seller-ui has none) | **New:** the Invitations tab and the whole invite flow |
| — | **New:** `/accept-invitation` |

Nothing seller-ui does is dropped except its two screens that have never worked.

## New components

**None expected.** `ARCHITECTURE.md` §4 already has everything: `page-header`, `panel`,
`tab-switcher`, `data-table` + `table-row`, `pagination`, `kpi-grid` + `kpi-card`, `badge`,
`form-field` with `text-field` / `select` / `checkbox` / `toggle`, `copy-field` (the one-time
token), `confirm-dialog`, `action-list`, `notice-bar`, `empty-state`, `busy-overlay`, `load-error`,
`export-button`. Check §4 before adding anything — the alignment pass exists to stop a sixth copy of
the field vocabulary.

The one possible addition is a feature-local `components/invite-dialog/`, and only if
`app-confirm-dialog` cannot host the projected email field and role select. Module 7 asked the same
question of its approve dialog and answered it in the feature, not in `shared/`.

## The one backend change

`store-commons/autoconfigure/.../CustomPermissionEvaluator.java`, one line beside its six siblings:

```java
case "STORE-CORE.USERS.RESET_PASSWORD" -> checker.hasAccessOnStoreUsersResetPassword(authentication, (StoreMerchantId) targetId);
```

with `hasAccessOnStoreUsersResetPassword` delegating to the existing private
`hasMaintainAccessOnUsers` — org admin or store admin, matching create/update/delete/enable/disable,
and deliberately **not** the moderator, who has read access only. A unit test asserts the token
resolves for both admins and refuses a moderator, so the next unmapped token fails a test rather
than a QA pass. `./gradlew checkstyleMain` runs at `maxWarnings = 0`.

**seller-ui, store-pod and every other service are untouched.**

## What stays unbacked → `lessons.md`

Appended after the existing 111, newest last, in the file's established format. Each pairs with a
`TODO(lessons.md):` marker at its call site; `npm run lint:lessons` enforces the citation.

1. **The JWT carries no user id, so the console cannot look itself up.** `sub` is the username;
   `JwtCustomizerConfig` adds `roles` and metadata and no id; every read-by-id ends at uaa's
   by-UUID lookup, and uaa exposes no get-by-username. Expected: add a `uid` claim, or give uaa a
   `GET /api/v1/admin/users/by-username`. **This is the root cause of the next two entries**, and it
   supersedes the standing "user-account/current is broken" entry, which is updated to say that
   fixing the `Principal`/`Jwt` binding turns a 500 into a 404 rather than into an answer.
2. **The gateway's OAuth client requests `openid` only.** `data-common.sql:64-77` — `scopes =
   'openid'`, no `profile`, no `email`. This is the precise, one-row cause of the already-logged
   "uaa's ID token carries no profile claims". Recorded with the row quoted, since it is the
   cheapest fix on this list.
3. **No self-service password change.** Only an admin reset exists, and it verifies no current
   password (`UserPassword.password` is read by nothing).
4. **`STORE-CORE.USERS.RESET_PASSWORD` was mapped nowhere.** Logged as the one **fixed** entry
   alongside the resolved dashboard-statistics outage, with the commit named — the file records what
   the backend could not do, and it should record when that stops being true.
5. **The user list is store-scoped, so an org admin is in no list.** Finding 3, with the
   `validateUserAccess` lines quoted. Expected: an org-scoped list, or treat a null `store` as
   "every store in the org".
6. **`assignable-roles` offers `SUPER_ADMIN` to an org admin.** Finding 4. The console filters it
   out client-side, which is defence in depth and not a fix.
7. **`StoreRoleAccessChecker.isOrgAdmin` does not verify the store belongs to the org** — it carries
   its own `@TODO`. Any org admin passes the permission check for any store id, with the real check
   done downstream in `validateUserAccess`. Defence in depth held, but the layer is doing less than
   it appears to.
8. **Creating a user is two calls and is not atomic** (Deviation 5).
9. **No user search of any kind.** uaa's list filters on metadata equality only; there is no
   name, email or username query. Kills the template's search box.
10. **A user has no last-login, no avatar, no phone, no job title, no timezone, no date format and
    no bio.** Ten designed fields, one table of eight columns behind them.
11. **No two-factor authentication and no session listing.** Zero hits repo-wide for
    `twoFactor`, `mfa`, `otp`; every `session` hit is HTTP-session config, a Stripe
    `CheckoutSession`, or a logout handler.
12. **No notification preferences.** Cross-references "Shell — no notifications service": the feed
    does not exist, so neither does the choice of what lands in it.
13. **No per-user activity log.** `tenancy_audit` records store and org lifecycle, keyed by actor,
    but nothing reads it per user and it does not cover catalogue or order actions. Kills "148
    actions" and "Recent order activity".
14. **No CSV export**, front or back. The header exports PDF.
15. **Nothing emails an invitation.** Stated by the platform itself in `CreatedInvitationDto`'s
    javadoc. Recorded not as a defect but as the constraint that shapes the invite dialog: the
    console must show the token once, because nothing else ever can.
16. **A `groups` concept is referenced and does not exist.**
    `extra/requests/store-ui/org-admin/users.http:16` calls `GET /user-account/groups`; there is no
    such endpoint, no groups table, and no role→permission mapping anywhere. A role is a bare name.

Cross-referenced rather than duplicated: "Shell — no user-preferences endpoint", "Shell — an org
admin cannot read its own organization", "Shell — no notifications service", "Auth — no password
reset, and no email verification", "Billing — entitlements are ceilings with no usage behind them"
(the design's "Team seats 6/15 — 2 invitations pending" meter).

## Implementation

Four layers per `ARCHITECTURE.md` §2 — page → facade → feature api service → `@api/*`.

- **`features/users/`** — `users.ts`/`.html`/`.css`, `components/user-detail/`,
  `components/invite-dialog/` (only if needed), `facades/users.facade.ts`,
  `services/users.api.service.ts`, `services/user-form.service.ts`. The facade is **provided by the
  page**. Two `snapshot()`s on deliberately different keys, as Module 7 established: the table
  (store + tab + page) and the KPI counts (store only), so switching tabs does not refire them.
  `isLoading`/`error`/`isEmpty`/`reload` come from `snapshot()`; `busy` is the in-flight write; the
  facade raises the toasts. Writes reload rather than echo — `enable`, `disable`, `delete` and
  `reset` all answer `void`.
- **`services/users.api.service.ts`** is the assembly point, following `payments.api.service.ts`
  closely: a `forkJoin` of the paged list and the optional legs, with **the user list as the
  unwrapped leg that is the page** and `assignable-roles` and the invitation list through the
  existing `optionalList`/`optionalOne` helpers, each with a comment naming why that leg may fail.
- **`features/profile/`** — `profile.ts`/`.html`/`.css`, `facades/profile.facade.ts`. No api
  service and no `@api/*` call: everything on the page comes from `AuthService`, `LocaleService` and
  the theme service. This is the first feature with no HTTP, and the facade says so.
- **`features/accept-invitation/`** — the token page, with `facades/accept-invitation.facade.ts`
  calling `OrgMemberService.accept`.
- **Client-side gating.** `AuthService.getRoles()` exists and **nothing calls it**. `/users` is the
  first page that should: the write actions render only for `isOrgAdmin || isStoreAdmin`, mirroring
  `hasMaintainAccessOnUsers`, so a moderator sees a readable list rather than buttons that 403.
  A small `@shared/` helper, not a copy per call site.
- **Routing** — `app.routes.ts`, under `ConsoleShell` with
  `[canAccessSecuredPages, consoleContext, requiresStore]` and `data: {titleKey, breadcrumbKey}`:
  `users`. `profile` takes the same guards **minus `requiresStore`** — a personal page is not
  store-scoped. `accept-invitation` sits under `AuthShell` with `canAccessSecuredPages` only.
  **`app.routes.server.ts` gains `users/**`, `profile/**` and `accept-invitation` as
  `RenderMode.Client`** — mandatory, or `app.routes.spec.ts` fails.
- **Nav** — `console-navigation.ts`: give the routeless `shell.nav.item.userManagement`
  `route: '/users'`, with the same one-line comment Modules 6 and 7 left on `inventory` and
  `payments`.
- **Toolbar** — `console-toolbar.ts`: `Profile` becomes a `routerLink="/profile"`; `Settings` is
  removed, and its `shell.profile.settings` key with it (`npm run lint:i18n` fails on an unreachable
  key, so this is not optional).
- **i18n** — a new `users.*` root and a `profile.*` root in `src/locale/{en,ar}.json` at exact key
  parity. Role names and `InvitationStatus` go through the known-set guard. Dates and counts through
  `TranslocoLocaleService`, never bare `Intl` or `DatePipe`. Emails, usernames and invitation tokens
  get `latin` (`unicode-bidi: plaintext`) inside the RTL page.
- **No fixture.** `src/app/mocks/` was removed in the alignment pass and nothing reintroduces it.

## Testing

Both UIs, same org admin, `ORG1-STORE1` open in each. The seeded accounts
(`uaa/init-sql/data-test-stores.sql`) give a moderator and a second store to test scoping with.

- The team list shows the same users as seller-ui's `users-list` — same usernames, emails, active
  flags, same total count.
- **Paging is the decisive check** — set the page size and confirm the server honours `count`
  (page 2 differs, the total is stable, the row count matches the requested size, not 20).
  See Deviation 8; seller-ui gets this wrong on screen.
- Create a user; confirm it appears in seller-ui's list after a reload, and that it can sign in.
- Edit a user's name and roles; disable and re-enable; delete — each round-tripped against
  seller-ui, and delete only against a throwaway account.
- **Reset a user's password and sign in as them.** This is the module's strongest evidence: it is
  the operation seller-ui's change-password screen has never been able to perform, and it works only
  because of the permission commit.
- **Invite `newbie@example.com`, copy the link, revoke it, invite again, resend, then accept in a
  private window as a second account.** Confirm: the token appears exactly once; `GET /invitations`
  never contains it; a duplicate invite is a 409 rendered as a field error, not a toast; a second
  accept says the link is used rather than failing silently; and the accepted user then appears in
  `org-member/list`. `qa/tenancy-and-pod-registry-split.md` LIF-04 is the reference run.
- Sign in as `org1-store1-moderator`: the list renders, and no write action does. Then confirm the
  backend agrees by calling `create` directly — it must be 403, not 200.
- Switch to `ORG1-STORE2` and confirm the list changes, and that `org1-admin` appears in neither —
  the notice-bar is what explains it (finding 3).
- `/profile` shows the username and roles, the language and theme controls actually change the app,
  and the Billing link lands on `/subscription`. The toolbar's Profile button opens it and no
  Settings button remains.
- A user with no roles, one with a role the console has not seen, an expired invitation, and a store
  with one user all render.
- Export the team list to PDF; check the rows against the table.
- Arabic and all three themes; 1440 / 900 / 420.

## Commits

1. `plan(console-ui): users and profile` — this section.
2. `fix(commons): the permission token that locked every password reset` — the
   `CustomPermissionEvaluator` case, `PermissionAccessChecker` method and its test. Its own commit
   because it is the one backend change in the module and should be revertable alone.
3. `feat(console-ui): the team list on real user accounts` — `api/tenancy/user-account.service.ts`,
   `models/{users,team}.ts`, `features/users/` Team tab, the route, the nav item, the KPI tiles, the
   role gate.
4. `feat(console-ui): invitations` — `api/tenancy/org-member.service.ts`, the Invitations tab, the
   invite dialog and its one-time link, `features/accept-invitation/`.
5. `feat(console-ui): the account page` — `features/profile/`, the toolbar wiring, the removed
   Settings button.
6. `fix(console-ui): users and profile after QA`.

Six rather than three: the backend fix, the two halves of `/users` and the profile page are each
independently reviewable, and folding the invitation flow into the team list would make the diff
unreadable.

## Critical files

**seller-ui: not modified. store-pod: not modified.** `store-commons/autoconfigure` **is** modified,
once, by decision — see "The one backend change".

**New:** `src/app/api/tenancy/{user-account,org-member}.service.ts` (+ specs),
`src/app/models/{users,team}.ts`,
`src/app/features/users/{users.ts,.html,.css,components/,facades/,services/}`,
`src/app/features/profile/{profile.ts,.html,.css,facades/}`,
`src/app/features/accept-invitation/{…}`,
`store-commons/autoconfigure/.../CustomPermissionEvaluatorTest` additions.

**Changed:** `src/app/app.routes.ts`, `src/app/app.routes.server.ts`,
`src/app/layouts/console-shell/console-navigation.ts`,
`src/app/layouts/console-shell/components/console-toolbar/console-toolbar.ts`,
`src/locale/{en,ar}.json`, `lessons.md`,
`store-commons/autoconfigure/.../CustomPermissionEvaluator.java`,
`store-commons/autoconfigure/.../PermissionAccessChecker.java`.

**Deleted:** `src/app/models/user.ts` — an unused stub describing a `groups` concept that does not
exist.

**Reused, already present:** `core/http/crud.service.ts` (`?store=` stamping),
`core/http/{optionalOne,optionalList}`, `core/table/table.types.ts` (`PageT`, `PageRequest`,
`count`), `core/auth/auth.service.ts` (`getRoles()`, used for the first time),
`shared/state`'s `snapshot()`, `shared/validators/passwords-match`,
`shared/forms`' `applyToForm` + `clearServerErrorsOnChange` and `formDirty`,
`shared/i18n/status-label`'s known-set pattern,
`core/export/pdf-export.service.ts` + `shared/ui/export-button`, and the §4 control catalogue.
`features/payments/` is the closest sibling — page, facade, api service and template all follow it.

## Verification

1. `cd store-core/console-ui && npm run build && npm run lint && npm run test:ci`;
   `./gradlew :store-commons:autoconfigure:test checkstyleMain`;
   `git -C store-core/seller-ui status --porcelain` and `git -C store-pod status --porcelain` both
   empty.
2. `grep -rn "user-account/current\|private/user/.*password\|localStorage.*userId" src` → no hits.
   None of seller-ui's three dead paths survives the port.
3. `grep -rn "lastAccess\|loginTime\|defaultLanguage" src` → no hits. No dead field ported.
4. `grep -rn "avatar\|twoFactor\|jobTitle\|timezone\|notificationPreference" src/app/features/profile` →
   no hits. None of the template's unbacked vocabulary leaked in.
5. `grep -rn "SUPER_ADMIN" src/app/features/users` → only the filter that removes it from the picker.
6. `grep -rn "TODO(lessons.md)" src` — every marker has a matching heading and every new heading a
   marker. `npm run lint:lessons` enforces it.
7. `src/app/app.routes.spec.ts` passes — proof the three new render-mode entries exist.
8. Network tab: every private call carries `?store=`; **the list response honours `count`**; the
   reset `POST` is `…/user-account/reset?store=&userId={uuid}` with a `{changePassword}` body and
   the id is the **UUID from the row, not the username**; `GET …/org-member/invitations` never
   contains a token; no request fires twice per page load.
9. Specs: `users.api.service.spec.ts` following `payments.api.service.spec.ts` (a fake that pages
   the way the server would), `HttpTestingController` specs for both new api services asserting URL,
   verb, params and body, and page specs for `/users` and `/profile` asserting against rendered
   English copy.
10. The two-tab comparison above, driven through Chrome — in particular the password reset and the
    invitation round-trip, this module's strongest evidence, because neither is something the old
    console can do at all.

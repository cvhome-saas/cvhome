# lessons — uaa-fe

Every capability the SSO design shows that uaa cannot do, and what this console does instead.
**Append-only.** Same format as [console-ui's](../../../../console-ui/lessons.md): what the screen
shows, what is missing underneath, the decision, and the contract that would close it.

The design lives in `store-core/uaa/sso/` (design mocks, deliberately not committed — the repo removed
`console-template`'s for the same reason). It draws a nine-screen SSO console; uaa has four screens'
worth of backend. This file is the difference.

The rule these all follow: **a fixture standing in for a real answer is worse than an absence.** A
number in a navigation rail is read as fact; a column with no source is read as "this account has no
MFA" rather than "we cannot tell".

---

## Shell — no sidebar badge counts

- **Screen:** the rail, which draws a count against Users.
- **What is missing:** any count endpoint. The number in the mock was invented.
- **Decision:** dropped. console-ui reached the same conclusion against the same design element —
  see its lessons.md, "Shell — no sidebar badge counts".
- **Expected contract:** `GET /api/v1/admin/counts` → `{users: n, roles: n, clients: n}`, one call
  rather than three, since it exists only to paint a rail.

## Shell — no realm switcher

- **Screen:** the rail's footer, which switches between `production` and other realms.
- **What is missing:** the concept. uaa is one authorization server with one user store; there is no
  realm to select.
- **Decision:** removed entirely rather than shown disabled — the other four unbuilt sections name a
  place that will exist, and this one names an idea the product has not adopted.
- **Expected contract:** none until multi-realm is a product decision.

## Shell — no notifications

- **Screen:** the topbar's bell, with a count badge.
- **What is missing:** any event stream or notification store.
- **Decision:** removed. Same reasoning as the rail badge.
- **Expected contract:** `GET /api/v1/admin/notifications?unread=true`.

## Shell — four sections have no backend

- **Screen:** Dashboard, Audit log, Identity providers and Settings in the rail.
- **What is missing:** all of it — no controller, no table, no data.
- **Decision:** rendered, but **disabled**: not links, `aria-disabled`, and titled "Not built yet".
  The rail is a map of the product, and hiding them would make it a map of this sprint. Each is a
  place an operator will eventually go.
- **Expected contract:** four separate features, not one.
- **Partly closed by feat/uaa-sso (phase 2):** Settings is built (`/settings`, `GET/PUT
  /api/v1/admin/settings`). Dashboard, Audit log and Identity providers stay disabled until their
  phases.
- **Closed by feat/uaa-sso (phase 7), for Identity providers:** the rail entry is a real route now. Dashboard and
  Audit log are still disabled; they arrive with the audit query API.
- **Closed by feat/uaa-sso (phase 8):** Dashboard and Audit log are routes now, over `GET /api/v1/admin/dashboard` and `GET /api/v1/admin/audit`. With Identity providers (phase 7) that is every disabled row gone; what the design still draws and this console does not have is the realm switcher and the notification bell, both of which need a store that does not exist.

## Users — no MFA state

- **Screen:** the users table's MFA column, and the detail pane's Security section.
- **What is missing:** uaa models no second factor. `UserDto` has no field, and
  `AppSecurityConfig` has no MFA filter.
- **Decision:** column and section removed. A blank MFA column reads as "no second factor", which is
  a security claim this console cannot make.
- **Expected contract:** `UserDto.mfaEnabled: boolean`, plus an endpoint to reset an enrolment.

## Users — no last sign-in

- **Screen:** the users table's "Last sign-in" column.
- **What is missing:** uaa records no authentication timestamp on the user.
- **Decision:** column removed.
- **Expected contract:** `UserDto.lastSignInAt: string | null`, ISO-8601.

## Users — no session list

- **Screen:** the detail pane's "Active sessions" list and "Sign out everywhere".
- **What is missing:** sessions live in Spring Session's JDBC store, which no admin endpoint exposes.
- **Decision:** removed. Offering "sign out everywhere" with nothing behind it is worse than not
  offering it — an operator would believe a compromised session had been revoked.
- **Expected contract:** `GET /api/v1/admin/users/{id}/sessions` and
  `DELETE /api/v1/admin/users/{id}/sessions`.

## Users — email and username cannot be changed here

- **Screen:** the edit dialog, where the design draws both as editable fields.
- **What is missing:** `UpdateUserRequest` carries neither — only `firstName`, `lastName`, `enabled`,
  `roles` and `metadata`. `CreateUserRequest` carries both, so the create dialog *does* edit them;
  they become read-only the moment the account exists.
- **Decision:** both are rendered **read-only when editing**, in a labelled block that says so.
  Editable-looking fields that silently fail to save are the worse failure.
- **Expected contract:** `UpdateUserRequest.email`, and a rename flow that decides what happens to a
  JWT `sub` — the username *is* the identity, so this is not a field change.
- **Closed by feat/uaa-sso (phase 4), for email:** `UpdateUserRequest.email` exists and the dialog
  edits it; a changed address is marked unverified, with a badge and a "Mark verified" action beside it.
  The username stays read-only, on purpose — it is what a JWT `sub` carries.

## Users — metadata is merged, never replaced

- **Screen:** the Metadata rows in the edit dialog, where each row has a remove button.
- **What is missing:** removal. `AdminService.updateUser` applies the bag with
  `u.getMetadata().putAll(req.metadata())` — a merge. A key already stored cannot be unset through
  this API at any value, including by omitting it.
- **Decision:** rows that came back from the server have their **remove button disabled**, with the
  reason as its title, and the section says so beneath. A remove that silently does nothing is worse
  than no remove: `org` and `store` are written by tenancy and read by other services, so an
  operator who believed a removal had worked would be wrong about which store an account belongs to.
  Editing a stored key and adding a new one both work and are offered.
- **Expected contract:** either `PUT /users/{id}/metadata` replacing the map wholesale, or a
  documented null-value convention meaning "unset".
- **Closed by feat/uaa-sso (phase 1):** the null-value convention. A key sent with `null` in
  `UpdateUserRequest.metadata` is removed. The pane still disables the remove button on stored keys
  until it learns to send that.
- **Closed by feat/uaa-sso (phase 4):** the pane sends `key: null` for every stored key whose row was
  removed, so the remove button is live again and `lockedKeys` is no longer passed.

## Users — creating an account is two calls, and there are no invites

- **Screen:** the design's "Invite user" and "Import CSV" buttons.
- **What is missing:** invitations as a concept, and CSV import. There is no token, no email and no
  acceptance step anywhere in uaa. `CreateUserRequest` is
  `(username, email, firstName, lastName, roles, metadata)` and carries **no password**, while
  `AdminService.createUser` leaves `passwordHash` null and `enabled` at its entity default of
  `true` — so a freshly created account exists, is enabled, and cannot sign in.
- **Decision:** **create is built**, as the two calls it really is: `POST /users`, then the
  set-password dialog opens on the new account without being asked for. Presenting it as one step
  and leaving the second to be discovered is how an operator ends up with an account nobody can use.
  Invite and CSV import are not built.
- **Expected contract:** for invites, `POST /api/v1/admin/invitations` returning a one-time token,
  which is roughly what tenancy already does for store members.
- **Closed by feat/uaa-sso (phase 4), for invites:** `POST /api/v1/admin/users/invitations` creates a
  pending account and answers a one-time link once; the console shows it in the kit's
  `app-one-time-link-dialog`, lists invitations with resend/revoke, and `/accept-invitation` takes the
  password. Admin-issued reset links (`POST /users/{id}/password-reset-links`, `/reset-password`) use the
  same mechanism. Create-then-set-password stays as the fast path. CSV import is still not built.

## Roles — a role is a name

- **Screen:** the roles table's Scope, Users, Perms and Type columns, and the edit dialog's
  Description, Inherits from, Permissions and Assigned users.
- **What is missing:** all of it. uaa's `Role` entity is `{id, name}`. Authorities *are* the role
  names; what they grant is decided by `@PreAuthorize` in each service, not by data.
- **Decision:** the page is a list of names with create, rename and delete. Everything else the
  design draws would be inventing a permission model in the UI that no service reads.
- **Expected contract:** a real model — `Role.description`, `Role.permissions[]`, `Role.parentId` —
  and services that consult it. That is a platform decision, not a screen.
- **Closed by feat/uaa-sso (phase 2):** `RoleDto` carries description, scope, systemRole, the parent,
  own and effective permissions and the holder count; the catalogue is `GET /roles/permissions`; the
  token carries `permissions`. Services still authorise on the role name — the second half of the
  contract is theirs to adopt.

## Clients — the list carries three fields

- **Screen:** the clients table's Type, Protocol, Last token and Status columns.
- **What is missing:** `ClientSummary` is `{id, clientId, clientName}`. Everything else needs the
  detail endpoint, so a five-column table would be N+1 fetches to paint a list.
- **Decision:** two columns; the rest appear in the detail pane, which fetches one client.
- **Expected contract:** widen `ClientSummary`, or `GET /clients?expand=true`.
- **Closed by feat/uaa-sso (phase 5):** `ClientSummary` carries the derived type, `enabled`, the grant
  types, the secret's expiry and the last token, and the list takes `q`, `enabled` and `type`. The
  table draws all five columns; the enable switch is a row action.

## Clients — no token metrics

- **Screen:** the "Tokens issued · 24h", "Failed authorizations · 24h" and "Secrets expiring · 30d"
  tiles.
- **What is missing:** uaa keeps no issuance counters and no secret expiry.
- **Decision:** removed. The one tile with a source — registered clients — is the pagination total,
  which the list already states.
- **Expected contract:** counters on the authorization server, and `clientSecretExpiresAt` on the
  registration.
- **Partly closed by feat/uaa-sso (phase 5):** `clientSecretExpiresAt` is set on every registration and
  rotation, so the *Secrets expiring · 30d* tile is live from `GET /clients/stats`. The 24h token and
  failure counters wait for phase 8's audit hooks; `lastTokenIssuedAt` exists on the row and is null
  until then.

## Sign-in — one step, password only

- **Screen:** the mock's provider buttons, passkey prompt, MFA step, "keep me signed in for 30 days",
  forgot-password and create-account links.
- **What is missing:** all of them. uaa's `AppSecurityConfig` declares `formLogin` and nothing else —
  no `oauth2Login`, no WebAuthn, no remember-me, no registration, no reset.
- **Decision:** the split layout was taken; the capability was not. The form stays a native POST to
  `/login` with `name=` attributes, because the redirect Spring Security answers with is what
  resumes the OAuth2 authorization flow.
- **Expected contract:** each is its own feature. Social login already exists in **cua** (shoppers)
  and is the closest precedent for uaa gaining it.
- **Partly closed by feat/uaa-sso (phase 3–4):** remember-me when the realm enables it, lockout and
  attempts-left states, and a "Forgot password?" that explains resets are issued by an administrator
  (there is no self-service reset, deliberately). Providers and MFA remain open.
- **Closed by feat/uaa-sso (phase 7), for providers:** the page is identity-first — provider buttons, an email step
  that asks uaa which realm the address belongs to, then the password with the identity shown above it. A brokered
  login that matches an existing account confirms with that password once. Passkeys and MFA remain open.

## Clients — a custom setting's value is a string

- **Screen:** the Custom settings rows on the client form, in both `clientSettings` and
  `tokenSettings`.
- **What is missing:** the value's type. Spring's `ClientSettings` and `TokenSettings` are
  `Map<String, Object>`, and nothing documents which keys exist or what they hold.
- **Decision:** values are edited and sent **as strings**. A member that arrives as a number or an
  object is shown as its JSON so it is at least legible rather than `[object Object]`, and saving it
  back sends that text. None of uaa's own settings use a non-string value; inventing a type picker
  for a bag nobody documents would cost more than it is worth and would still be guessing.
- **Expected contract:** a documented list of the settings uaa reads, with their types — at which
  point these become real fields rather than an open map.

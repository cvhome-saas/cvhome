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

- **Screen:** the detail pane, where the design draws both as editable fields.
- **What is missing:** `UpdateUserRequest` carries neither — only `firstName`, `lastName`, `enabled`,
  `roles` and `metadata`.
- **Decision:** both are rendered **read-only**, in a labelled block that says so. Editable-looking
  fields that silently fail to save are the worse failure.
- **Expected contract:** `UpdateUserRequest.email`, and a rename flow that decides what happens to a
  JWT `sub` — the username *is* the identity, so this is not a field change.

## Users — no invite, no CSV import

- **Screen:** the "Invite user" and "Import CSV" buttons.
- **What is missing:** invitations as a concept. `AdminUserService.create` exists and takes a
  password, so it is a *create*, not an invite; there is no token, no email, no acceptance.
- **Decision:** neither built. Create is a feature this alignment did not add — see the PR's scope.
- **Expected contract:** for invites, `POST /api/v1/admin/invitations` returning a one-time token,
  which is roughly what tenancy already does for store members.

## Roles — a role is a name

- **Screen:** the roles table's Scope, Users, Perms and Type columns, and the detail pane's
  Description, Inherits from, Permissions and Assigned users.
- **What is missing:** all of it. uaa's `Role` entity is `{id, name}`. Authorities *are* the role
  names; what they grant is decided by `@PreAuthorize` in each service, not by data.
- **Decision:** the page is a list of names with create, rename and delete. Everything else the
  design draws would be inventing a permission model in the UI that no service reads.
- **Expected contract:** a real model — `Role.description`, `Role.permissions[]`, `Role.parentId` —
  and services that consult it. That is a platform decision, not a screen.

## Clients — the list carries three fields

- **Screen:** the clients table's Type, Protocol, Last token and Status columns.
- **What is missing:** `ClientSummary` is `{id, clientId, clientName}`. Everything else needs the
  detail endpoint, so a five-column table would be N+1 fetches to paint a list.
- **Decision:** two columns; the rest appear in the detail pane, which fetches one client.
- **Expected contract:** widen `ClientSummary`, or `GET /clients?expand=true`.

## Clients — no token metrics

- **Screen:** the "Tokens issued · 24h", "Failed authorizations · 24h" and "Secrets expiring · 30d"
  tiles.
- **What is missing:** uaa keeps no issuance counters and no secret expiry.
- **Decision:** removed. The one tile with a source — registered clients — is the pagination total,
  which the list already states.
- **Expected contract:** counters on the authorization server, and `clientSecretExpiresAt` on the
  registration.

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

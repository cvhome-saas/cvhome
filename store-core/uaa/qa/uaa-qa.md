# QA — uaa (`store-core/uaa`)

uaa is the platform's authorization server for **staff**: it authenticates the seller console and every
operator, issues the tokens the gateway relays inward, mints client-credentials tokens for
service-to-service calls, and owns the user, role and client records behind all of it. Shoppers authenticate
somewhere else entirely — that is [cua](../../../store-pod/cua/qa/cua-qa.md).

- **Scope** — the authorization-code flow behind the console, the client-credentials grant, `/userinfo`, the
  `/api/v1/admin/**` user / role / client API, the seeded clients and accounts the `test-stores` profile
  provides, and **uaa's own console** (`src/main/resources/uaa-fe`), which is served from uaa's `static/` and
  is where the platform's sign-in page lives
- **Runs on** — `lcl start -d --stack <name>`; uaa is `http://uaa.gateway.com:8001` and is the **first**
  service the stack brings up, because it issues the tokens. Read the live port from `lcl urls`
- **Cases** — 115 (89 verified, 11 unit only, 15 not verified)
- **Also see** — [gateway](../../gateway/gateway-service/qa/gateway-qa.md) (which relays the token and holds
  the session), [tenancy](../../tenancy/tenancy-service/qa/tenancy-qa.md) (which owns the *store-scoped*
  accounts and calls uaa to create them),
  [console-ui](../../console-ui/qa/console-ui-qa.md) (the screens)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

Most of this file is **[not verified]**: uaa has had no QA document of its own until now, and the cases below
were written from `AppSecurityConfig`, the admin controllers and the seed. That is exactly where the bugs will
be.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins and the seeded ids are in
[`references/qa-testing.md`](../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5. Only
what is specific to uaa is below.

Sign in through the console at `http://gateway.com:8000/oauth2/authorization/uaa` — that is the
authorization-code flow. uaa's own login page is `http://uaa.gateway.com:8001`.

**Seeded accounts** (`store-core/uaa/src/main/resources/init-sql/data-test-stores.sql`), password `admin`:
`super-admin`, `org1-admin`, `org1-store1-admin`, `org1-store1-moderator`, `org1-store2-admin`, `org2-admin`.
Only `super-admin` can reach `/api/v1/admin/**`.

**Seeded clients** — `web-app` (the console's authorization-code client, PKCE required) and
`store-core@service.store-core.internal` (client credentials, scope `store_core`). On a shifted stack the
`web-app` redirect URIs are rewritten by an `after-up` hook; `lcl events` records `uaa.redirects.patched`.

**Secrets come from the environment.** `application.yml` reads every client secret and the super admin's password
from `UAA_*` variables with no default; the `lcl` and `test-stores` slices supply the local values (the shared
`hLwOF…` secret, password `admin`) and turn the boot-time seed writers on (`com.asrevo.cvhome.uaa.seed.apply-on-boot`).
Anywhere else the writers are off, so a password an operator changed survives a restart — and a missing variable
fails the start rather than running on a committed secret.

**CSRF is on.** Every session-authenticated write to uaa needs the `XSRF-TOKEN` cookie's value back in
`X-XSRF-TOKEN` (Angular does that on its own) and the sign-in form carries it as `_csrf`. A bearer-token call is
exempt, and so are the protocol endpoints (`/oauth2/token`, `/oauth2/introspect`, `/oauth2/revoke`).

**Token lifetimes** — access 15 min, refresh 12 h, authorization code 5 min. A day-long access token is what the
seed used to hand out.

```bash
# a service-to-service token (the shared lcl secret is UAA_STORE_CORE_SECRET in application-lcl.yml)
TOK=$(curl -s -u 'store-core@service.store-core.internal:<the shared lcl secret>' \
  -d 'grant_type=client_credentials&scope=store_core' \
  http://uaa.gateway.com:8001/oauth2/token | jq -r .access_token)

# what is in it
echo "$TOK" | cut -d. -f2 | base64 -d 2>/dev/null | jq .
```

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select id, username, enabled from uaa.users order by username;"
... "select * from uaa.roles;"
... "select * from uaa.user_roles;"
... "select id, client_id, client_authentication_methods, authorization_grant_types, redirect_uris, scopes
       from uaa.oauth2_registered_client;"
... "select principal_name, authorization_grant_type, access_token_expires_at from uaa.oauth2_authorization;"
```

Tokens are rows now (`uaa.oauth2_authorization`): a token endpoint call adds one, a restart keeps them, a
revocation invalidates one in place. An empty table after a login is a regression — it means the in-memory
authorization service is back.

Logs: `.lcl/<stack>/logs/uaa.log`. **If a login fails, the stack almost certainly came up without
`test-stores`.**

---

## AUT — Signing in and getting a token

The console never sees a password: it redirects to uaa, uaa authenticates, and the gateway holds the resulting
session and relays the token inward. Everything in this section is that path.

### AUT-01 — The authorization-code flow signs an operator in · critical · [verified]

- **Steps** — open `http://gateway.com:8000/oauth2/authorization/uaa`, sign in as `org1-admin` / `admin`.
- **Expect** — a redirect back to the console, which renders with the org's stores in the switcher. Going
  straight to an `/api/**` URL instead returns **401 without** redirecting to a login page — that is by design,
  not a broken redirect.
- **Seen** — this is the path every other QA document in the repo starts with; it is verified continuously by
  everything else being runnable.

### AUT-02 — The login form submits · critical · [verified]

- **Steps** — open `/login`, type `super-admin` / `admin`, press Enter.
- **Expect** — a 302 to `/`, then the console's Users page. A failed attempt comes back to `/login?error`.
- **Why this is the case that breaks** — the page is Angular but the form is **not**: `AppSecurityConfig`
  declares `formLogin(loginPage("/login"))`, so the browser must post `username`/`password` as a real form and
  Spring Security answers with the redirect that resumes the OAuth2 flow. A browser only submits inputs that
  carry a `name`, which is why `app-text-field` has a `name` input at all. Posting through `HttpClient`
  instead would authenticate and then strand the flow.
- **And the token** — the form also posts `_csrf`, read from the `XSRF-TOKEN` cookie the page response set.
  Without it the post comes back as `/login?error=expired` (SEC-05); a page that says "expired" on a fresh
  load means the cookie was never planted, which is `CsrfCookieFilter`'s job.
- **Note** — the form does not submit from a synthetic click in some automation tools. Use
  `document.querySelector('form').requestSubmit()`, or type into the password field and press Enter. A tool
  that appears to hang on the login page is almost always this, not uaa.

### AUT-03 — A wrong password is refused, and says nothing useful · high · [not verified]

- **Steps** — sign in with a valid username and a wrong password, then with a username that does not exist.
- **Expect** — both are refused the same way, with no hint about which half was wrong, and no stack trace or
  root-cause text on the page.

### AUT-04 — The client-credentials grant issues a service token · critical · [not verified]

- **Steps** — the `curl` in §00, then decode the token.
- **Expect** — **200** with an `access_token` carrying `scope: store_core` and no user claims. Pod services
  accept it for their s2s endpoints (see
  [pod-registry-qa.md](../../pod-registry/pod-registry-service/qa/pod-registry-qa.md) §PLC and
  [inventory-qa.md](../../../store-pod/inventory/inventory-service/qa/inventory-qa.md) INV-04).

### AUT-05 — A service token cannot do a person's job · critical · [not verified]

- **Why it matters** — inventory INV-04 records the inverse observation: a service token was **refused** a
  stock upsert, because managing stock is a person's permission. The two together are what the scope split is
  for.
- **Steps** — call a `/private/**` seller endpoint with the s2s token from AUT-04.
- **Expect** — **403**, not 200.

### AUT-06 — The token carries the claims the pods scope on · critical · [not verified]

- **Steps** — decode an operator token obtained through the console.
- **Expect** — an `org` claim, a `store` claim where the account has one, `uid` (the account's UUID — `sub` stays
  the username, which every service keys on), `roles`, and **nothing else from metadata**: the customizer copies only
  `org` and `store`, so a metadata key named `roles` or `scope` cannot shadow the real claim (SEC-09). A missing
  `org` claim is tolerated by `ManagerOrgId`'s lenient `String` constructor (see
  [tenancy-qa.md](../../tenancy/tenancy-service/qa/tenancy-qa.md) 99) — `SecurityUtils` relies on it, so do not
  "fix" it.
- **Seen** — `LoginFlowIntegrationTest` walks form login → `/oauth2/authorize` with PKCE → code → token for
  `org1-store1-admin` and asserts exactly these claims, plus `email`/`given_name` on the ID token.

### AUT-07 — `/userinfo` answers for a signed-in operator · [not verified]

- **Steps** — call `/userinfo` with an operator token.
- **Expect** — 200 with the subject and the ID-token profile claims, and **not** a password hash, a client secret
  or an internal id the console has no business seeing. This is the authorization server's own endpoint: the
  hand-written `UserInfoController` that shadowed it was unreachable and is gone.

### AUT-08 — On a shifted stack the redirect URIs still point at this stack · high · [verified]

- **Steps** — start a second stack (`lcl start -d --stack xxx`) and sign in through **its** gateway port.
- **Expect** — the login redirect targets `uaa.gateway.com:<uaa-b>` with
  `redirect_uri=http://gateway.com:<gw-b>/…`, not the default stack's ports. `lcl events` shows
  `uaa.redirects.patched`. Cross-referenced from [`qa/lcl-qa.md`](../../../qa/lcl-qa.md) case 09.

---

## ADM — The admin API

`/api/v1/admin/**` is gated twice: at the filter chain on `SCOPE_super_admin` or `ROLE_SUPER_ADMIN`, and again
with `@PreAuthorize` on **every** method. The gateway relays the operator's token unchanged and adds nothing —
uaa's own guard is what keeps this safe, which is why the negative cases matter more than the positive ones.

### ADM-01 — A super admin can list and read users · [verified]

- **Steps** — as `super-admin`, `GET /uaa/api/v1/admin/users` through the gateway, then one by id, then
  `/exists`. `http/admin-user-api.http` runs all of it.
- **Expect** — 200 on each; the list carries no password material.
- **Mind the prefix.** `/uaa/...` is the **gateway's** forward route. On uaa itself the same endpoints are at
  `/api/v1/admin/...`, and asking for `/uaa/...` there does not 404: `StaticController` forwards any dotless
  path to index.html, so it answers **200 with the SPA's HTML**. That is why `UiKitConfig.uaaBasePath` exists,
  and why a mis-prefixed call shows up as `CLIENT.HTTP_200` rather than as a 404.

### ADM-02 — An org admin is refused every admin endpoint · critical · [unit only]

- **Steps** — as `org1-admin`, repeat ADM-01, then attempt `POST /users`, `PUT /users/{id}`,
  `DELETE /users/{id}`, and the same across `/roles` and `/clients`.
- **Expect** — **403** as `application/problem+json` (`COMMON.ACCESS_DENIED`) on every one. This is the case that
  proves the double gate; an org admin reaching any of it is a platform-wide escalation.
- **Seen** — `AdminUserApiIntegrationTest` proves the half a service token can reach: a `store_core` token gets
  403, `admin-sdk` gets 200, anonymous 401. The org-admin session half is still to be driven through the console.

### ADM-03 — Anonymous is refused · critical · [unit only]

- **Steps** — call any `/api/v1/admin/**` path with no token.
- **Expect** — **401** as `application/problem+json`, never a redirect to `/login`. `/.well-known/**`, the login
  page, `/actuator/health`, `/actuator/info` and the swagger paths are deliberately open; nothing else is — the
  rest of the actuator needs a platform principal and is not even mapped (SEC-01).

### ADM-04 — Enable and disable actually take effect · high · [not verified]

- **Steps** — disable a seeded account, try to sign in as it, re-enable it, sign in again.
- **Expect** — the sign-in fails while disabled and succeeds after. `uaa.users.enabled` follows.
- **Restore it afterwards** — the seed only runs on a clean database.

### ADM-05 — Roles created here appear to the console · high · [verified]

- **Steps** — add a role through `/api/v1/admin/roles`, then open the console's role picker.
- **Expect** — the console renders the unknown role rather than crashing or dropping it — see ACC-03 below,
  which is the console's half of the same assertion.

### ADM-06 — A client's secret is never read back · critical · [verified]

- **Steps** — `GET /uaa/api/v1/admin/clients` as super-admin, then one by id.
- **Expect** — neither carries the secret in readable form. A secret in the response, or in `uaa.log`, is a
  finding. Registration does not echo one either, and `reset-secret` answers `void` — so the only moment a
  secret is readable is the moment it is set, which is what the console's notice says.

### ADM-07 — Reading one client does not exhaust the stack · critical · [verified]

- **Steps** — `GET /api/v1/admin/clients/{id}` for any registered client.
- **Expect** — 200 with the full `ClientDetails`, including `clientAuthenticationMethods`.
- **What this is guarding.** `ClientAuthMethod.from` called *itself* instead of `valueOf`, so every parse
  recursed until the stack ran out and the endpoint answered 500 carrying `StackOverflowError`. Nothing
  noticed for as long as the controller had no caller: the list endpoint answers `ClientSummary` and never
  parses an auth method, so only reading or saving one client reached it. Fixed, with
  `ClientAuthMethodTest` — uaa's first unit test — holding it.

### ADM-08 — Search, status filters and counts · high · [verified]

- **Steps** — `GET /users?q=store2%20mod`, `?status=PENDING`, `?role=ORG_ADMIN`, `?metadata[org]=<id>`, then
  `GET /users/counts`. `http/admin-user-api.http` has each.
- **Expect** — `q` is a case-insensitive contains over username, email **and the full name** ("Store2 Mod" finds
  `org1-store2-moderator` and `org2-store2-moderator`); the filters AND together; `counts` answers
  `{total, active, pending, locked, disabled}` and `active + pending + locked + disabled == total` on the seed.
- **Mind the NULLs.** `locked_until` is null on every never-locked row, so a filter written as
  `NOT (locked_permanently OR locked_until > now)` matched nothing; `UserSpecifications.hasStatus` is null-safe on
  purpose. `UserSearchIntegrationTest` holds it.
- **Gate** — a `store_core` token gets 403 on `/counts`, `/invitations` and `POST /invitations`; anonymous 401.

---

## ACC — Accounts, as the console drives them

_From `qa/console-ui-users-and-profile.md` — the cases whose assertion is **uaa's**, not the console's. The
screens are [console-ui-qa.md](../../console-ui/qa/console-ui-qa.md). Was PERM-02 / U-10 / U-11 / P-02 / P-03;
renumbered to ACC-01…05 because a prefix must be unique within the file and those were the console's._

### ACC-01 — The new password actually signs in · critical · [not verified]

After console-ui's PERM-01, sign out and sign in as `org1-store1-moderator` / `Passw0rdQA`.

**Expect** — the console opens. Set it back to `admin` afterwards, or note that you changed it.

### ACC-02 — The role picker never offers platform superuser · critical · [unit only]

Open the create form.

**Expect** — **Store administrator** and **Store moderator**, and nothing else.
`GET …/assignable-roles` really does return `SUPER_ADMIN` to an org admin — it filters uaa's role table by
removing only `USER` and `ORG_ADMIN`. The console intersects rather than filters one name, so a role added to
uaa later cannot appear unreviewed either. **This is defence in depth, not a fix**: `lessons.md`, "Users —
assignable-roles offers SUPER_ADMIN to an org admin".

### ACC-03 — A role the console has never seen · high · [unit only]

Add a role to `uaa.roles` and grant it to a user.

**Expect** — the row humanizes it (`REGIONAL_BUYER` → `Regional Buyer`) rather than the page going blank.
Transloco throws on a missing key and a role is a database row, not an enum.

---

### ACC-04 — What it shows, and what it says instead · high · [not verified]

**Expect** — the username, the roles, and a notice explaining that the console can see nothing else.
**No name, email, avatar, phone, job title, timezone, date format or bio** — none has a column anywhere, and
the account record is unreachable twice over (`lessons.md`, "Users — the JWT carries no user id"). Empty
fields would read as "you have not filled these in"; the notice is the honest version.

### ACC-05 — No password control · high · [unit only]

**Expect** — nothing about passwords on `/profile`. A self-service change needs the caller's own user id and
the JWT carries the username instead, so the action lives on `/users` where a row has a real id.

> **Signup transactionality lives in tenancy.** RBS-07 ("signup cannot orphan an organization") asserts the
> boundary between tenancy's signup and uaa's account creation; it is kept on tenancy's side because signup is
> tenancy's endpoint — [tenancy-qa.md](../../tenancy/tenancy-service/qa/tenancy-qa.md) §RBS.

---

## CON — uaa's own console

`src/main/resources/uaa-fe` was rebuilt on **`@cvhome-saas/ui-kit`**, the library extracted from console-ui:
same Angular 20 standalone stack, same four-theme token layer, same controls. Nebular, jQuery, Bootstrap,
ngx-toastr and module-federation are gone, and with them 158 files.

Reach it on uaa's own origin — `http://uaa.gateway.com:8001` in a default stack, and read the live port from
`lcl urls`. It is **not** behind the gateway; `/uaa/**` is how the *seller* console reaches uaa's API, not how
this app is served.

`StaticController` forwards any dotless path that is not `/api/` or `/oauth2/` to index.html, so a deep link
to `/clients` reaches the router rather than 404ing.

### CON-01 — The shell renders and knows who is signed in · [verified]

- **Steps** — sign in (AUT-02), then look at the bar.
- **Expect** — brand, three sections with icons, the signed-in username, a language toggle, Sign out. The
  active section is highlighted.
- **Why the username matters.** uaa's `/api/v1/auth/me` returns `getAuthentication().getPrincipal()` — the
  principal *unwrapped* — while the gateway's endpoint of the same path returns a whole
  `OAuth2AuthenticationToken` with the principal nested under `principal`. The kit's `AuthService` handles
  both. When it handled only the gateway's, sign-in appeared to fail: the POST authenticated and the guard
  then read a shape it did not recognise and bounced straight back to `/login`. A blank username here means
  that regression is back.

### CON-02 — Users lists every account, and the table is the console's own · critical · [verified]

- **Steps** — open **Users**.
- **Expect** — every seeded account with avatar monogram, roles, organization and an ENABLED badge; a notice
  saying uaa offers no text search; paging by `count`.
- **This is the point of the extraction.** The table is `app-user-admin-table` from
  `@cvhome-saas/ui-kit/uaa` — the same component console-ui renders at `/platform/users`, not a copy. If it
  drifts visually between the two consoles, something has been forked that should not have been.

### CON-03 — Roles lists, creates, renames and deletes · high · [verified]

- **Steps** — open **Roles**; the five seeded roles are listed. Create one, rename it, delete it.
- **Expect** — each round-trips and the list re-reads. Delete asks you to type the role name first.
- **Note** — the amber notice is not decoration: renaming a role does **not** re-issue anyone's token, so a
  principal already signed in keeps the old authority until their next sign-in.

### CON-04 — Clients lists, and the editor is driven by the server's own options · high · [verified]

- **Steps** — open **Clients**; the four seeded registrations are listed. Press **Edit** on one.
- **Expect** — the form fills from `GET /clients/{id}`, and each list field's hint shows what uaa actually
  accepts — grant types, auth methods, scopes — read from `GET /clients/options`, which is built from the
  server's own enums. A form driven by it cannot offer a grant type uaa would reject.
- **The options leg is allowed to fail.** It is wrapped in `optionalOne()`: if it 500s the form still edits
  the values the client already has, where a failed `forkJoin` would render a blank page.

### CON-05 — Arabic mirrors the whole app · high · [verified]

- **Steps** — press **العربية** in the bar.
- **Expect** — copy switches and the layout mirrors: the nav moves to the right, labels right-align, the
  notice and the form mirror with them. `LocaleService` sets `dir="rtl"` on the document.
- **What must NOT mirror** — a client id, a grant type, a scope, a role name. Those fields are `latin mono`
  and stay left-to-right, because `client_secret_basic` reversed is not a translation.

### CON-06 — A non-super-admin gets nothing · critical · [verified]

- **Steps** — sign in as `org1-admin` and open each of the three sections.
- **Seen** — the shell renders (they *are* authenticated: `/auth/me` answers with `ROLE_ORG_ADMIN`), and all
  three lists fail. `GET /api/v1/admin/{users,roles,clients}` each answer **403**, which is ADM-02 verified
  from the console's side.
- **The refusal is rendered, but as a code.** The strip reads `CLIENT.HTTP_403 [403]` where the string
  "You do not have permission to do that." already exists in the kit's dictionary. The filter chain's 403 now
  **does** carry a problem+json body (`COMMON.ACCESS_DENIED`, written by `ProblemAccessDeniedHandler` — see
  SEC-11), so the parser no longer has to synthesise a code; what remains is `app-load-error` being bound to
  `failure.message`, which is developer text. See console-ui-qa.md KIT-04b; it is pre-existing and affects
  both consoles. **[not verified]** since the body change.

### CON-09 — The tab title is translated on a cold load · [verified]

- **Steps** — open `/login` in a fresh tab and read the browser tab.
- **Expect** — "Sign in", not `route.signIn`.
- **What this guards.** A `*transloco` directive triggers its own load, so the *screen* looks right without
  any initializer. Anything translating imperatively does not: `TranslatedTitleStrategy` runs on the first
  navigation and, with no dictionary in place, `translate()` returns the key. The app now preloads the
  active language in `provideAppInitializer`, the way console-ui does. The facades' toast messages are the
  same shape and would have followed.

### CON-07 — Write paths round-trip · high · [verified]

- **Seen, from Roles** — create `ROLE_QA_TEMP` (list re-read to 6, form closed), rename it to
  `ROLE_QA_RENAMED` **keeping the same UUID** so it is an update rather than a create, then delete it.
  Toast on each — "…was saved.", "…was deleted." — and the list re-read from the server every time. The
  delete's **Delete button stays disabled until the role name is typed**.
- **Seen, from Users** — disable `org1-store2-moderator`: toast "…can no longer sign in.", server
  `enabled: false`, and the row's own action flipped to **Enable**, which is the re-read proving itself.
  Re-enabled afterwards.
- **Seed restored** — 5 roles, no `ROLE_QA*` left, no account left disabled. Verified against the server,
  not the screen.
- **Still [not verified]** — reset password, grant/revoke a role, and the whole client register → rotate →
  delete path.
- **Restore whatever you change** — the seed only runs on a clean database.

### CON-08 — Rebuilding the kit under a running dev server · [verified]

- **Steps** — with `ng serve` running, rebuild `store-commons/ui-kit`.
- **Expect** — this is the one that will waste your afternoon. The `file:` dependency is a symlink into the
  kit's `dist`, and rebuilding it replaces that directory underneath the watcher; the dev server can latch a
  resolution failure and show `TS2307: Cannot find module '@cvhome-saas/ui-kit'` over a page that is otherwise
  fine. Restart the app (`lcl restart console-ui`), do not go hunting for a broken import.
- **A new kit export is a second trap.** Vite pre-bundles the kit into `.angular/cache/<version>/<app>/vite/deps/`
  and a restart alone keeps that bundle: the page throws `does not provide an export named 'OneTimeLinkDialog'`
  and renders nothing. `rm -rf store-core/console-ui/.angular/cache`, then restart. Seen when the one-time-link
  dialog moved into the kit.

---

## DSN — the SSO design alignment

The console was aligned to the mocks in `store-core/uaa/sso/` — a grouped rail, a topbar, the Light
theme, and a **list + detail pane** on all three screens replacing the inline form and the dialogs.
What the design draws that uaa cannot do was removed, not faked;
[`uaa-fe/lessons.md`](../src/main/resources/uaa-fe/lessons.md) is the record, and `npm run lint`
checks every citation to it still names a real heading.

### DSN-01 — The rail is a map of the product, not of this sprint · high · [verified]

- **Steps** — sign in and read the rail.
- **Expect** — four groups (Overview, Identity, Applications, System) and seven rows. Users, Roles
  and Clients are links; **Dashboard, Audit log, Identity providers and Settings are `<span>`s** with
  `aria-disabled="true"` and the title "Not built yet".
- **What must NOT be there** — a count badge on any row, and the realm switcher. Both were in the
  mock and neither has a source. A number in a navigation rail is read as fact.

### DSN-02 — The rail collapses, then becomes a drawer · [verified]

- **Steps** — collapse the rail with its toggle; then narrow the window below 900px.
- **Seen** — collapsed to an icon strip at full width; below 900px the toggle appears, the rail goes
  `position: fixed` and translates off-canvas, the scrim appears and closing it slides the rail back.
  The split panes stack below 1100px.
- **Structural, not fluid** — the type does not shrink at any width.

### DSN-03 — A row opens the right container · critical · [verified]

- **Steps** — click a row on each of the three screens.
- **Expect** — **Roles** and **Users** open a modal; **Clients** navigates to `/clients/{id}`.
- **Why the split.** A role is one field and a client is five sections, two URI arrays and two open
  maps. Both prior designs agree: the pre-kit app routed the client form, and the mocks draw a whole
  *SSO New Client* page for it. A modal there would be a form nobody can read; a route for a role's
  single field is ceremony.
- **Also check** — the Users row's action **menu** does not also open the row behind it. The table
  reads the click target rather than stopping propagation on a wrapper, so this is a real regression
  risk if that check is removed.

### DSN-04 — Two dialogs never stack · high · [verified]

- **Steps** — open a role, press Delete. Open a user, press Set password, then Delete account.
- **Expect** — the form closes as the second dialog opens. Never two in the top layer at once.
- **Why** — a modal over a modal leaves a form the operator can see and cannot reach, and Escape
  then closes the wrong one. Cancelling returns to the list, which is the accepted cost.
- **Still dialogs, deliberately** — setting a password and deleting. Both are genuinely modal
  moments, and delete keeps its typed confirmation.

### DSN-05 — Saving a user does not disable them · critical · [verified]

- **Steps** — select a user, change a name, Save. Then check `enabled` on the server.
- **Expect** — unchanged, along with `roles` and `email`.
- **Why this is a case.** The pane sends `UpdateUserRequest` with names only. uaa's update is
  genuinely partial, so the omitted fields are preserved — verified against the server rather than
  assumed. If it ever stops being partial, saving a profile silently locks the account out.

### DSN-06 — Saving a client keeps the settings it does not edit · critical · [verified]

- **Steps** — edit a client's access-token lifetime and save; compare `tokenSettings` before/after.
- **Seen** — `accessTokenTimeToLive` changed, and `refreshTokenTimeToLive`, `accessTokenFormat`,
  `reuseRefreshTokens`, `clientSettings` and `scopes` were all carried through untouched.
- **Note for whoever runs this** — `accessTokenFormat` is an **object** (`{value: 'self-contained'}`),
  not a string. Comparing it with `===` compares references and always reports a false difference;
  compare the serialised form.

### DSN-07 — Identifiers stay readable under Arabic · high · [verified]

- **Steps** — switch to العربية on Clients and Roles.
- **Expect** — the layout mirrors (rail on the right, panes swapped), and client ids, role names and
  UUIDs still read correctly.
- **How** — those cells are `unicode-bidi: plaintext`, the CSS spelling of `dir="auto"`: the cell
  aligns with the document while the string resolves from its own first strong character, so
  `store-core@service.store-core.internal` does not have its punctuation reordered.

### DSN-08 — The lessons file cannot rot · [verified]

- **Steps** — `npm run lint` in uaa-fe.
- **Expect** — `lessons.md: N citation(s) checked against 13 headings — all resolve.` Rename a
  heading without fixing its citation and the build fails.


### DSN-09 — The kit's own buttons are styled · critical · [verified]

- **Steps** — in uaa-fe open **Clients → a client → New secret**, and **Users → a user → Set
  password**. Look at the dialog's footer.
- **Expect** — a filled emerald *Set secret* and a neutral *Cancel*, not two runs of bare text.
- **What this is really testing.** `set-password-dialog`, `roles-dialog`, `load-error`,
  `empty-state`, `file-drop-zone`, `tree`, `action-menu` and `user-admin-table` all render
  `.primary-action`, `.secondary-action`, `.icon-action`, `.danger-action` or `.popover`, and every
  one of those declarations used to live in **console-ui's** `styles.css`. uaa-fe imported the token
  layer and not that, so the kit's own components rendered unstyled here and nothing errored. The
  layer is now `@cvhome-saas/ui-kit/theme/css/controls.css` and both consoles import it. **Delete
  that import from either app's `styles.css` and this case is how you find out.**
- **Cheap probe** — in the console:
  `getComputedStyle(Object.assign(document.body.appendChild(document.createElement('i')),{className:'primary-action'})).minHeight`
  must not be `0px`.

### DSN-10 — The user row's actions are a menu · high · [verified]

- **Steps** — Users, open a row's **⋮**.
- **Expect** — five named entries; *Delete account* in the danger tone; *Signing in as another
  account is not built yet* listed and **disabled**. The column has a header reading **Actions**.
- **Before** — five bare glyphs in a 10rem column (eye, padlock, shield, arrow, bin), told apart
  only by hovering for a tooltip, one of them permanently inert with no visible reason.
- **Note** — this component is console-ui's too, on Platform users and Organization → Users. Check
  one of those as well; the change is deliberate there.

### DSN-11 — A duration is a number and a unit · high · [verified]

- **Steps** — Clients → a client → **Token settings**.
- **Expect** — four compact `amount + unit` pairs, two-up. `PT24H` reads as **1 · days**;
  `PT5M` as **5 · minutes**.
- **Round-trip** — set Access token to `90 minutes`, save, reload. The Summary shows `PT1H30M` and
  the field still reads **90 · minutes** — the largest unit that divides evenly, so nothing is lost.
- **Empty is not zero** — clear the amount and save: the field sends `null` ("uaa's default"), never
  `PT0S` ("expires immediately"). These are different answers and the control must not conflate them.
- **Was** — a text box asking an operator to type `PT30M`, and before that four number boxes to add up.

### DSN-12 — Redirect URIs are rows, and one of them warns · high · [verified]

- **Steps** — Clients → a client → **Redirects**. Add a row, type `console.example.com/cb`, blur.
- **Expect** — a field error (*Enter an absolute URI, including its scheme*). Fix it to
  `https://…` and the row's glyph turns into an emerald check.
- **The amber case** — `http://console-ui.gateway.com:10000/...` shows an **amber warning**, and
  `http://localhost:10000/...` does not. Plain HTTP to a remote host is legal and uaa accepts it;
  it also hands the authorization code to anything on the network path. It is flagged where it is
  typed because it is never looked at again.
- **Blank rows** — click Add and save without typing: the empty row is dropped, not sent.

### DSN-13 — The checklist and the form agree · critical · [verified]

- **Setup** — Clients → **Register client**.
- **Steps** — tick `authorization_code` with no redirect URI; set the only auth method to `none`
  with PKCE off; tick `private_key_jwt` with no JWK Set URL; tick `tls_client_auth` with no
  Subject DN.
- **Expect** — each turns its **Before you save** line grey, and Save refuses with
  *Some settings still need attention*.
- **Why it matters.** These are OAuth rules uaa enforces somewhere less visible. The panel is not
  decoration: every line is backed by the same root-level validator, so the panel cannot read ready
  while the form refuses, or the reverse.

### DSN-14 — A client round-trips whole · critical · [verified]

- **Steps** — on `web-app`, change something in **every** section — a scope, a redirect URI, a TTL,
  PKCE, the ID-token algorithm, a custom setting — then Save and reload.
- **Expect** — all of it read back. Then `GET /api/v1/admin/clients/{id}` and confirm
  `tokenSettings.accessTokenFormat` is `{"value":"self-contained"}`, an **object**.
- **The trap** — `OAuth2TokenFormat` serialises as `{value}`, not the bare string the field name
  suggests. Sending a string is accepted and read back as an object, so the *next* save carries
  `[object Object]` into the registry. Nothing fails at the time.

### DSN-15 — Metadata merges, and the UI does not pretend otherwise · critical · [verified]

- **Steps** — Users → a user with `org`/`store` metadata. Try to remove a stored row; add a new
  key/value; save; reopen.
- **Expect** — the stored rows' remove buttons are **disabled**, titled *Stored keys cannot be
  removed through uaa's update*. The added key persists. Editing a stored value persists.
- **Why** — `AdminService.updateUser` does `metadata.putAll(...)`. A key already stored cannot be
  unset through this API at any value, including by omitting it. An operator who believed a removal
  worked would be wrong about which store an account belongs to.
- **Superseded by INV-06 (phase 4).** The remove button is live again: a removed row goes out as `key: null`,
  which uaa now treats as "unset". Verified on the `team` key of a test account.

### DSN-16 — Creating a user is two calls · high · [verified]

- **Steps** — Users → **Create user**, fill username and email, Save.
- **Expect** — the account is created **and the password dialog opens by itself**. Set a password,
  then sign in as that account (DSN-16 is not complete without this).
- **Why the second half is not optional.** `createUser` never sets a password hash and `enabled`
  defaults to `true`, so skipping it leaves an account that exists, looks enabled, and cannot sign
  in. Presenting create as one step is how that happens.
- **[not verified]** — signing in as the created account. Everything up to and including
  set-password is verified.
- **The alternative is INV-01.** *Invite user* creates the account pending and hands the person a one-time link
  to choose their own password; the create-then-set-password path stays as the fast one.

### DSN-17 — Every table paginates · [verified]

- **Steps** — Users, Roles, Clients.
- **Expect** — each shows `Showing 1–N of N …` beneath its table. All three are the kit's
  `app-pagination` over a `SpringPage`, and uaa's three list endpoints all page.
- **Note** — none of the three has a search box, and Users says so in a notice: uaa's admin list
  matches metadata equality and offers no text query at all.


---

## SEC — Hardening

_The security review that started the uaa work found seventeen defects; each row below is one of them, stated as
the request that used to succeed and now must not. Every case has a matching integration test where one is
possible; the tag says whether it has also been driven against a stack._

### SEC-01 — The actuator is closed · critical · [verified]

- **Steps** — anonymous `GET /actuator/env`, `/actuator/heapdump`, `/actuator/loggers`, `/actuator`; then the same
  with a `store_core` token; then `/actuator/health` and `/actuator/info` anonymously.
- **Expect** — 401, 401, 401, 401; **404** with the token (the endpoints are not mapped at all — uaa narrows
  `management.endpoints.web.exposure.include` to `health,info,prometheus`); 200 and 200.
- **Why it matters** — a heap dump of an authorization server contains its signing key, and all of it was public.
- **Seen** — `ActuatorExposureIntegrationTest`. On the first stack pass the live uaa still listed every endpoint:
  it had started before the exposure change. Restart uaa after touching `application.yml`.

### SEC-02 — The super admin's password is not reset on every boot · critical · [not verified]

- **Steps** — with `seed.apply-on-boot` off (any profile but `lcl`/`test-stores`), change the super admin's
  password in the database, restart uaa, sign in with the new password.
- **Expect** — the new password holds. With the flag on (a local stack) the configured one is written back, which
  is the point of a local stack. `AdminUserDatabaseInitializer` and `OAuth2ClientDatabaseInitializer` are both
  behind the flag.

### SEC-03 — No committed secret · high · [verified]

- **Steps** — `grep -rn hLwOF store-core/uaa/src/main/resources/application.yml`; start uaa without
  `UAA_ADMIN_SDK_SECRET` set and without the `lcl` profile.
- **Expect** — no match; uaa fails to start with an unresolved placeholder rather than booting on a default.
- **Seen** — the grep is empty; the defaults live in `application-lcl.yml` and `application-test-stores.yml`, and
  `common-config.yml` / tenancy's `application.yml` read the same `UAA_*` names with the local value as fallback.

### SEC-04 — The super admin's password cannot be set through the API · critical · [verified]

- **Steps** — as `super-admin` (or with an `admin-sdk` token), `PUT /api/v1/admin/users/65d8419c-…/reset-password`.
- **Expect** — **403 `UAA.USER.SUPER_ADMIN_IMMUTABLE`**. Every other mutator already refused that account; this one
  did not, so any `super_admin` token could take the platform owner over. The guard is now keyed on the account's
  **id**, not its email, so renaming the account does not lift it.
- **Seen** — `AdminServiceTest`, and the block in `http/admin-user-api.http`.

### SEC-05 — CSRF is enforced · critical · [verified]

- **Steps** — `POST /login` without `_csrf`; a session-authenticated `POST /api/v1/admin/roles` without the
  `X-XSRF-TOKEN` header; the same with it.
- **Expect** — `302 /login?error=expired`; **403 problem+json**; 200.
- **Seen** — `CsrfLoginIntegrationTest`. The first version of the 403 came back as the **SPA's index page with a
  200**: the default handler used `sendError`, the container dispatched to `/error`, and `StaticController`'s
  catch-all served `index.html` for it. Both are fixed — the handler writes the body itself and the router
  excludes `/error` — and REG has the row.

### SEC-06 — Lockout, rate limiting, password policy, audit · [verified]

- Built in phases 2 and 3: see `LCK`, `RL`, `PWD` and `SES` below; the audit query screen is phase 8 (`AUD`).

### SEC-07 — Short tokens and PKCE · high · [verified]

- **Steps** — decode an operator token and a service token; start the console login without `code_challenge`.
- **Expect** — `exp - iat` ≤ 900 s; the seed's refresh token is 12 h and its authorization code 5 min;
  `/oauth2/authorize` for `web-app` without a challenge is refused with `invalid_request`.
- **Seen** — `LoginFlowIntegrationTest` (both). The gateway canary (AUT-01) is what proves the real client still
  sends PKCE.

### SEC-08 — Signing keys · [verified]

- Phase 6 of the plan (`KEY`). For now: a key that fails to parse is **logged at ERROR** and left out of the JWK
  set, where it used to disappear silently.
- **Closed by phase 6** — see KEY-01…04: encrypted at rest, rotated with a retire window, scheduled, unreadable keys
  replaced.
### SEC-09 — The issuer is pinned · critical · [verified]

- **Steps** — `GET /.well-known/openid-configuration` on `localhost:<port>`, not on `uaa.gateway.com`.
- **Expect** — `issuer` is `http://uaa.gateway.com:<port>` from the service registry, whatever host the request
  used. Left unpinned, a proxy's `X-Forwarded-Host` decided the issuer of every token.
- **Seen** — `IssuerPinningIntegrationTest`; on the live stack `http://uaa.gateway.com:10001`.

### SEC-10 — Metadata cannot forge a claim · critical · [verified]

- **Steps** — as super-admin, `PUT /users/{id}` with `{"metadata": {"roles": ["SUPER_ADMIN"], "scope": "super_admin"}}`,
  then obtain a token for that user.
- **Expect** — the token's `roles` are the user's real roles and there is no `scope` from metadata. Only `org` and
  `store` cross over.
- **Seen** — `JwtCustomizerConfigTest.metadataCannotOverrideTheRolesClaim`.

### SEC-11 — A refusal is a problem, not a page · high · [verified]

- **Steps** — any 401 or 403 from the filter chain on `/api/**`.
- **Expect** — `application/problem+json` with `COMMON.UNAUTHENTICATED` / `COMMON.ACCESS_DENIED`, no HTML, no
  redirect. `/api/v1/auth/me` is the cheapest probe (`http/auth-api.http`).
- **Seen** — `MeEndpointIntegrationTest`, `AdminUserApiIntegrationTest`.

### SEC-12 — `PUT /clients/{id}` writes the path's client · high · [verified]

- **Steps** — `PUT /api/v1/admin/clients/A` with a body whose `id` is B.
- **Expect** — A is updated, B untouched, response `id` is A. It used to write B.
- **Seen** — `AdminClientServiceTest`; the block in `http/admin-client-api.http`.

### SEC-13 — `/api/v1/auth/me` is a DTO · high · [verified]

- **Steps** — call it with a session, then with an `admin-sdk` token.
- **Expect** — `{uid, username, email, firstName, lastName, roles, authorities[{authority}], authenticatedVia}`;
  never the raw token or a `UserDetails`. The service token gets **403 `UAA.AUTH.NOT_A_USER_PRINCIPAL`**.
- **Seen** — `MeEndpointIntegrationTest`.

### SEC-14 — An account without a password fails cleanly · high · [verified]

- **Steps** — create a user without `password`, try to sign in as it.
- **Expect** — refused like a wrong password, not a 500 out of a null hash. Create **with** a password and the
  account signs in at once — creation is one call now, not create-then-reset.
- **Seen** — `AdminServiceTest` (create with/without); the sign-in half **[not verified]**.

### SEC-15 — Role grants are exact · high · [verified]

- **Steps** — grant `ROLE_STORE_ADMIN` (the authority spelling), then `SUPER_ADMIN`.
- **Expect** — **404 `UAA.ROLE.NOT_FOUND`** and **403 `UAA.ROLE.NOT_ASSIGNABLE`**. Both used to answer 200 having
  granted nothing.
- **Seen** — `AdminServiceTest`; the blocks in `http/admin-user-api.http`.

### SEC-16 — Tokens are rows · critical · [verified]

- **Steps** — mint a service token; `select count(*) from uaa.oauth2_authorization`; `POST /oauth2/revoke`;
  `POST /oauth2/introspect`.
- **Expect** — the count grows by one; revocation keeps the row and introspection answers `active: false`.
  Before the `JdbcOAuth2AuthorizationService` bean the table was always empty and revocation had nothing to act on.
- **Seen** — `JdbcAuthorizationPersistenceIntegrationTest`.

### SEC-17 — Metadata keys can be unset · [verified]

- **Steps** — `PUT /users/{id}` with `{"metadata": {"store": null}}`.
- **Expect** — `store` is gone from the response and from `uaa.users.metadata`. Merge is still the rule for every
  non-null value.
- **Seen** — `AdminServiceTest`; the block in `http/admin-user-api.http`. DSN-15's "stored keys cannot be removed"
  is therefore stale on the console side until the pane learns the null convention.

---

## ROL — Roles and permissions

_Phase 2 of the uaa SSO plan. A role is no longer only a name: it carries a description, a scope, an optional
parent it inherits from, and a set of keys from the permission catalogue in `store-commons:commons`. The token
now carries `permissions` (the effective set over the user's roles) beside `roles`; **services still authorise on
the role name** — the claim is issued so they can start reading it without a token-format change._

### ROL-01 — The catalogue is the enum · [verified]

- **Steps** — `GET /uaa/api/v1/admin/roles/permissions`.
- **Expect** — every value of `Permission` with its group and description, and nothing else. Grant a key that is not
  in it and the role write is **400 `UAA.PERMISSION.UNKNOWN`** naming the key (ROL-04).
- **Seen** — `AdminRoleApiIntegrationTest`.

### ROL-02 — Inheritance is transitive and cycle-free · high · [verified]

- **Steps** — create `REGIONAL_BUYER` inheriting from `ORG_ADMIN` with `users:read`; read it back; set `ORG_ADMIN`'s
  parent to `REGIONAL_BUYER`.
- **Expect** — `effectivePermissions` holds `users:read` plus everything `ORG_ADMIN` grants; the second write is
  **422 `UAA.ROLE.INHERITANCE_CYCLE`**. Clearing the parent (`clearInheritsFrom: true`) drops the inherited keys.
- **Seen** — `RoleServiceTest`, `AdminRoleApiIntegrationTest`.

### ROL-03 — A system role keeps its name and cannot be deleted · critical · [verified]

- **Steps** — `PUT /roles/{ORG_ADMIN}` with `{"name":"OWNER"}`; `DELETE /roles/{ORG_ADMIN}`; then
  `PUT` with only `description` and `permissions`.
- **Expect** — **403 `UAA.ROLE.SYSTEM_IMMUTABLE`** twice; the third write is 200. Renaming `STORE_ADMIN` would not
  rename the `hasPermission` checks that read it.
- **Seen** — `AdminRoleApiIntegrationTest`; the blocks in `http/admin-role-api.http`.

### ROL-04 — Names are the claim, so they are normalised and validated · [verified]

- **Steps** — create `" staff "`; create `store-admin`; create `STAFF` twice.
- **Expect** — `STAFF`; **400 `UAA.ROLE.NAME_INVALID`** with a field error on `name`; **409 `UAA.ROLE.NAME_TAKEN`**.
- **Seen** — `RoleServiceTest`.

### ROL-05 — A held role cannot be deleted · high · [unit only]

- **Steps** — delete a custom role after granting it to an account.
- **Expect** — **409 `UAA.ROLE.IN_USE`** with the holder count. Remove it from the accounts first. Before this, a
  delete silently stripped every holder.
- **Seen** — `RoleServiceTest.aHeldRoleCannotBeDeleted`.

### ROL-06 — The token carries `permissions` · critical · [verified]

- **Steps** — obtain a token for `org1-store1-admin` (AUT-06); call `/api/v1/auth/me` as `org1-admin`.
- **Expect** — `permissions` lists the effective keys (`users:read`, `users:write` for a store admin); `/me` carries
  `permissions` and `PERM_<key>` authorities beside `ROLE_*`. Nothing authorises on them yet.
- **Seen** — `LoginFlowIntegrationTest`, `MeEndpointIntegrationTest`.

### ROL-07 — Every write leaves an audit row · high · [verified]

- **Steps** — create, change permissions on, and delete a custom role; then
  `select event_type, actor_name, target_name, before_json, after_json from uaa.audit_events order by id desc limit 5`.
- **Expect** — `role.created`, `role.permissions.updated` (with the `permissions` before/after) and `role.deleted`,
  each naming the actor (`admin-sdk` for a client token, the username for a session) and the role.
- **Seen** — `AdminRoleApiIntegrationTest` counts the rows.

### ROL-08 — The Roles screen · high · [verified]

- **Steps** — open **Roles**; switch System / Custom; search; open `ORG_ADMIN`; open a custom role; create one with
  a parent and a few permissions; switch to العربية.
- **Expect** — five columns (Role with description, Scope, Users, Perms = effective count, Type); a system role's
  name and scope are disabled with the notice; the matrix ticks by group with *Select all / Clear* and the count line
  reads `N direct · M effective`; the parent select lists every other role; the delete button is absent on a
  system role. Identifiers stay left-to-right under Arabic.
- **Seen** — created `REGIONAL_BUYER` with two keys (row read `REALM 0 2 CUSTOM`), set its parent to `ORG_ADMIN`
  (row read `4`, the union), deleted it with the typed confirmation; `role.created` and `role.updated` rows with
  actor `super-admin` and ip; العربية mirrored the page (`dir=rtl`, «الأدوار») with the role names still
  left-to-right.

---

## SET — Realm settings

_Phase 2. One row, `uaa.settings`, read and written whole with a version. Lockout, password policy, session and
token defaults, key rotation and audit retention **are stored here from this phase and enforced from the phases
that build each mechanism** (LCK, PWD, SES, KEY) — until then a change is a change of record only._

### SET-01 — The document round-trips and audits · high · [verified]

- **Steps** — `GET /uaa/api/v1/admin/settings`, change `lockout.threshold`, `PUT` it back with its `version`.
- **Expect** — 200 with `version + 1` and `updatedBy` = the caller; a `settings.updated` audit row whose diff holds
  only `lockout`.
- **Seen** — `AdminSettingsApiIntegrationTest`; `http/admin-settings-api.http`.

### SET-02 — A stale version is refused · high · [verified]

- **Steps** — `PUT` the document you read *before* SET-01's save.
- **Expect** — **409 `UAA.SETTINGS.CONFLICT`**; nothing written. The screen asks for a reload rather than
  overwriting someone else's change.

### SET-03 — Ranges are checked on the way in · critical · [verified]

- **Steps** — `PUT` with `lockout.threshold: 0`, then with `password.minLength: 4`, then with
  `sessions.maxSeconds` shorter than `idleSeconds`.
- **Expect** — **400 `UAA.SETTINGS.INVALID`** with the field named, every time. A threshold of zero would lock
  every account on its first attempt, and the column would store it.
- **Seen** — `SettingsServiceTest` (each rule), `AdminSettingsApiIntegrationTest` (the first).

### SET-04 — The Settings screen · high · [verified]

- **Steps** — open **Settings** (the rail row is a link now); change a number; watch the header; Discard; change
  again and Save; reload.
- **Expect** — sections General / Authentication / Sessions & tokens / Signing keys, with *Email* disabled and
  titled as not built; the header reads *Saved* until a field changes, then *Save changes* + *Discard*; durations
  are edited in minutes / hours / days and read back the same; *Allow self-registration* is disabled with its
  note; the last-saved stamp names you.
- **Seen** — *Saved* → edit the lockout threshold → *Discard* + *Save changes* → *Saved*, toast, the stamp
  `Last saved by super-admin at …`, and the `settings.updated` audit row carrying only the `lockout` diff. The
  first build of the page threw `Cannot read properties of null (reading '_rawValidators')`: the audit-retention
  field sat inside the keys form group in the template. REG has the row.

---

## LCK — Lockout

_Phase 3. Failed password sign-ins are counted per account; at the realm's threshold (`settings.lockout.threshold`,
default 5) the account is locked for `durationSeconds` (default 15 min), and after `permanentAfter` lockouts (default
5) it stays locked until an administrator unlocks it. Locked and disabled accounts fail **before** the password is
compared, so a locked account learns nothing from a guess and never counts another attempt._

### LCK-01 — Five wrong passwords lock, the right one is then refused, unlock restores · critical · [verified]

- **Steps** — sign in as `org2-store2-moderator` with a wrong password five times, then with `admin`; as
  super-admin read the account and `POST …/unlock`; sign in again.
- **Expect** — the first four wrong attempts return to `/login?error&attemptsLeft=N` counting down to 1; the fifth
  — the one that locks — already lands on `/login?error=locked` (never "0 attempts left"); the right password then
  lands on `/login?error=locked` too; the admin API shows `status: LOCKED`; after the unlock the sign-in succeeds.
  Audit: five `user.login.failed` (reason `BAD_CREDENTIALS`), one `user.locked`, one `user.unlocked`, one `user.login`.
- **Seen** — `LockoutIntegrationTest`, `LockoutServiceTest`.

### LCK-02 — A disabled account is told so · high · [verified]

- **Steps** — disable an account, sign in as it.
- **Expect** — `/login?error=disabled`. The page says the account is disabled and to contact an administrator; a
  wrong password on a disabled account says the same, not "attempts left".
- **Seen** — `AdminSessionsIntegrationTest`.

### LCK-03 — The sign-in page explains the state · high · [verified]

- **Steps** — drive LCK-01 in the browser, then unlock the account from the Users screen (row menu → *Unlock
  account*).
- **Expect** — "N attempt(s) left before a 15-minute lock" under the wrong-password message (the minutes come from
  the realm's public login settings, not a literal); "This account is locked …" when it is; "signed out" after a
  logout; the page never says which half of the credentials was wrong. On the Users screen the row's badge reads
  **LOCKED**, the dialog's *Security* section shows the failed-attempt count, and *Unlock account* returns the badge
  to ACTIVE, zeroes the counters and writes `user.unlocked` with the administrator as the actor.
- **Seen** — EN and AR, stack `uaa-sso`, 2026-09-02. **Beware** — a failed sign-in attempt submitted from a browser
  that already holds an administrator session ends that session (Spring clears the security context on any
  authentication failure). That is the framework's default and is left as is; use a second browser profile for the
  wrong-password half.

---

## RL — Rate limiting

_Phase 3. A fixed window per address on the endpoints that take a secret: `/login` (10 a minute), `/oauth2/token`
(60) and `/api/v1/public/**` (20). Refused before Spring Security runs, so a refused attempt costs no hash and locks
nothing; POSTs only, so loading a page never counts. In memory, per instance — a brake on guessing, not accounting._

### RL-01 — A burst of sign-ins is a 429 with a problem body · critical · [verified]

- **Steps** — POST `/login` eleven times from one address within a minute (any username, even one that does not
  exist).
- **Expect** — ten 302s, then **429** `application/problem+json` `UAA.AUTH.RATE_LIMITED` with `Retry-After: 60` and
  a `traceId`; `GET /login` still answers 200. Audit: one `request.rate_limited` row per refusal.
- **Seen** — `RateLimitIntegrationTest` (limit lowered to 3 for its context), `RateLimiterTest`.

### RL-02 — The token endpoint is limited too · high · [not verified]

- **Steps** — 61 `client_credentials` calls from one address in a minute.
- **Expect** — the 61st is 429. Services that legitimately mint that often share an address only behind a proxy
  that forwards the client's, which `forward-headers-strategy: NATIVE` honours.

---

## PWD — Password policy

_Phase 3. Every password set through the API — admin reset, self-service change, and later the invitation and
reset-link flows — goes through one funnel: the realm's rules (length, character classes, not the username or the
email's local part), the account's recent hashes, and, when enabled, the Have I Been Pwned range check. Seeds and
boot initializers write hashes directly and are the deliberate exception; that is why `admin` still signs in._

### PWD-01 — Every broken rule is reported at once · high · [verified]

- **Steps** — reset a password to `short`.
- **Expect** — **400 `UAA.PASSWORD.POLICY_VIOLATION`** with one field error on `password` per rule: `minLength`,
  `upper`, `digit` (the defaults require 12 characters, upper, lower, digit).
- **Seen** — `PasswordPolicyValidatorTest`, `AccountApiIntegrationTest`.

### PWD-02 — A recent password cannot come back · high · [unit only]

- **Steps** — change a password to A, then B, then A again.
- **Expect** — **422 `UAA.PASSWORD.REUSED`** naming how many are remembered (`settings.password.historyCount`).
- **Seen** — `PasswordServiceTest`.

### PWD-03 — Expiry forces a change · [unit only]

- **Steps** — set `password.expiryDays` to 1, backdate `uaa.users.password_changed_at` by two days, sign in.
- **Expect** — `/login?error=expired-password`; the account is told to ask for a reset.
- **Seen** — `PasswordServiceTest.expiryFollowsTheChangeDate` for the rule; the sign-in path is not driven.

### PWD-04 — The breach check is a check, not a decision · [not verified]

- **Steps** — enable `password.rejectBreached`, set `password123456` (in every breach corpus), then set a strong one
  with the network cut.
- **Expect** — the first is **422 `UAA.PASSWORD.COMPROMISED`**; the second is accepted with a WARN in the log — an
  outage at the corpus must not block a reset.

### PWD-05 — New hashes are bcrypt 12 · [verified]

- **Steps** — reset a password, `select password_hash from uaa.users where …`.
- **Expect** — `{bcrypt}$2a$12$…`; the seeded `$2a$10$` hashes still verify.

---

## SES — Sessions and revocation

_Phase 3. Sessions are Spring Session rows indexed by principal, stamped at sign-in with ip, user agent, how the
sign-in happened and when. Ending a session deletes its row. Disabling, deleting or resetting an account, and a
self-service password change, end its sessions **and** its OAuth2 authorizations (`uaa.oauth2_authorization`),
which makes its refresh tokens unusable at once; a self-contained access token lives out its fifteen minutes._

### SES-01 — Two sessions, end one · high · [verified]

- **Steps** — sign in twice (two browsers) as `org2-store1-moderator`; as super-admin `GET /users/{id}/sessions`;
  `DELETE` one of them.
- **Expect** — two rows with ip, `via: PASSWORD` and timestamps; after the delete one browser is signed out
  (`/api/v1/auth/me` 401) and the other is not. A session id that is not the account's is **404**, never a hint.
- **Seen** — `AdminSessionsIntegrationTest`.

### SES-02 — Disabling signs the account out everywhere · critical · [verified]

- **Steps** — with the sessions of SES-01 alive, `POST /users/{id}/disable`.
- **Expect** — both browsers get 401; a fresh sign-in says `disabled`; `uaa.oauth2_authorization` holds no row for
  the account. Re-enable afterwards.
- **Seen** — `AdminSessionsIntegrationTest`, `AdminServiceTest`.

### SES-03 — Changing my password keeps only my session · critical · [verified]

- **Steps** — sign in twice as `org1-store2-admin`; from one, `PUT /api/v1/account/password` with the current and a
  new password.
- **Expect** — that session stays, the other is gone, the old password no longer signs in, the new one does; a wrong
  current password is **400 `UAA.PASSWORD.CURRENT_MISMATCH`** and changes nothing.
- **Seen** — `AccountApiIntegrationTest`. **Restore** — this leaves the account on the new password until the
  database is reset.

### SES-04 — A service client has no account · [verified]

- **Steps** — `GET /api/v1/account/sessions` with a `client_credentials` token.
- **Expect** — **403 `UAA.AUTH.NOT_A_USER_PRINCIPAL`**.

### SES-06 — The account page · high · [verified]

- **Steps** — from the who-chip open `/account` as `super-admin`, in English and Arabic.
- **Expect** — the breadcrumb reads *My account*; *Change password* asks for the current password, a new one and its
  repeat, states the realm's rules in the hint and warns that the change signs every other session out; *Sessions*
  lists this device with its ip, `PASSWORD`, last-active time and user agent, marked *this device*. Arabic mirrors
  the layout; the user agent and ip stay left-to-right.
- **Seen** — the screen; the password change itself is SES-03 (`AccountApiIntegrationTest`).

### SES-05 — Idle and absolute timeouts, remember-me, single session · [not verified]

- **Steps** — set `sessions.idleSeconds` to 60 and wait; set `sessions.maxSeconds` to 120 and keep clicking; turn
  remember-me on and sign in with the box ticked, then delete the `SESSION` cookie; turn *one session per user* on
  and sign in twice.
- **Expect** — signed out after a minute idle; signed out after two minutes however active; still signed in from
  the remember-me cookie (and not when the setting is off); the first session ends when the second begins.
  Mechanisms: `LoginSuccessHandler`, `SessionMaxAgeFilter`, `SettingsAwareRememberMeServices`.

---

## INV — Invitations and one-time links

An invitation creates the account **pending** — no password hash, `activated_at` null — and issues a 256-bit
token whose SHA-256 is the only thing stored (`uaa.invitations.token_hash`). The plaintext link is answered
**once** by the admin call and, in the same transaction, registered on the `User` aggregate as an
`InvitationIssuedEvent` that the namastack outbox delivers to `LoggingLinkDeliveryHandler`. Nothing emails it: the
console shows it in `app-one-time-link-dialog` and the operator carries it. A password-reset link is the same
mechanism for an account that already has a password (`uaa.password_reset_tokens`, one hour). The public pages
are `/accept-invitation?token=` and `/reset-password?token=`, served by uaa itself, anonymous, rate-limited.

### INV-01 — Invite, accept, sign in · critical · [verified]

- **Steps** — uaa console → Users → **Invite user**: email `ada.qa@mail.com`, name, tick `USER`, *Create
  invitation*. Copy the link from the dialog. In a private window open it; type `alllowercase12` twice and submit;
  then `Welcome-Passw0rd-2026` twice. Follow *Go to sign in* and sign in as `ada.qa@mail.com`.
- **Expect** — the tiles go to *Pending invitations 1*, the row shows `PENDING`, the Invitations tab lists it
  *Pending* with *Resend* / *Revoke*. The accept page greets by first name, shows the account and expiry, lists
  the realm's rules. The weak password is refused by the server with **"Add an upper-case letter."** bound to the
  field (`UAA.PASSWORD.POLICY_VIOLATION`, `params.rule=upper`). The strong one answers *Your account is ready*;
  signing in works; the row is `ACTIVE`, `email_verified` true, the invitation *Accepted*.
- **Truth underneath** — `select status from uaa.invitations` → `ACCEPTED`; `select status, count(*) from
  uaa.outbox_record group by status` → `COMPLETED`; `uaa.log` carries `One-time invitation link issued for …`
  and, because `com.asrevo.cvhome.uaa.links.log-links=true` under `lcl`, the link itself. Audit rows
  `user.created`, `invitation.created`, `invitation.accepted`, `user.activated`.
- **Also** — `InvitationFlowIntegrationTest`.

### INV-02 — A link works once · critical · [verified]

- **Steps** — reload the accepted link from INV-01; open `/accept-invitation` with no token; open it with a made-up
  token; call `GET /api/v1/public/password-resets/nope`.
- **Expect** — *This link cannot be used* with *Go to sign in* for the first three (one message for expired,
  revoked and spent: an anonymous visitor can act on none of them differently); the API answers **404**
  `application/problem+json` (`UAA.INVITATION.NOT_USABLE` / `UAA.PASSWORD.RESET_TOKEN_NOT_USABLE`). Never a 401
  and never a login redirect — the public chain is stateless.

### INV-03 — Resend rotates, revoke withdraws · high · [verified]

- **Steps** — invite a second address; Invitations tab → *Resend* on it (a new link dialog), then *Revoke* and
  confirm. Then `POST /users/{id}/invitations/resend` on the account from INV-01.
- **Expect** — after resend the **old** link answers 404 and the new one previews; only one `PENDING` row per
  user exists (`uq_invitations_one_pending`), the previous is `REVOKED` with detail *superseded by a resend*.
  After revoke the row is *Revoked*, the tile drops, the pending account remains (delete it from its row, or
  re-invite). Resend on an activated account answers **422** `UAA.USER.NOT_PENDING`.
- **Also** — the invitation status tabs (*Pending / Accepted / Revoked / Expired / All*) each re-query the server.
- **[verified]** for the API half and the *Accepted* / *Pending* tabs; the console's *Resend* and *Revoke* buttons
  were driven once each on an earlier build and are **[not verified]** on this one.

### INV-04 — Inviting a taken address is a 409 with a field · high · [verified]

- **Steps** — invite `org1-admin@mail.com`.
- **Expect** — **409** `UAA.USER.EMAIL_TAKEN` with `fieldErrors[0].field == "email"`; the dialog marks the email
  field. The username defaults to the email, so the email check fires before the username one.
- **Also** — `InvitationFlowIntegrationTest`.

### INV-05 — An admin-issued reset link · critical · [verified]

- **Steps** — Users → open `org1-store1-moderator` → **Issue reset link**, leave *Also sign them out everywhere*
  off, *Issue link*. Open the link in a private window; type two different passwords; then
  `Reset-Passw0rd-2026` twice. Sign in with it. Repeat with the toggle **on** while the account has a live
  session.
- **Expect** — the dialog is the same show-once one. The page says *Choose a new password*; the mismatch is caught
  locally (*The two passwords do not match.*) without a request; the strong password answers *Password saved* and
  the old password stops working. With the toggle on, `GET /users/{id}/sessions` is empty after issuing and every
  `oauth2_authorization` row for the user is gone (`SES-*` mechanisms). A live link becomes unusable once a second
  one is issued, and the link is single-use. Audit `user.password.reset_link.issued`, then `user.password.reset`
  with reason `RESET_LINK`.
- **Also** — `PasswordResetLinkIntegrationTest` (leaves `org1-store1-moderator` on `Reset-Passw0rd-2026`
  inside the test database only).
- **[verified]** without the revoke toggle; **[unit only]** with it.

### INV-06 — Metadata rows can be removed · high · [verified]

- **Steps** — open a user, *Add* a metadata row `team = qa`, Save; reopen, *Remove* it, Save; reopen.
- **Expect** — the key is present after the first save and **absent** after the second. The pane sends
  `{"team": null}` for a stored key whose row was removed (SEC-17 is the API half).

### INV-07 — "Forgot password?" explains, it does not promise · [verified]

- **Steps** — `/login` → *Forgot your password?*.
- **Expect** — a note that there is no self-service reset and an administrator issues a one-time link. No form,
  no request. Arabic reads the same.

---

## VER — Email verification and editing

### VER-01 — Changing the email un-verifies it · high · [verified]

- **Steps** — open the account from INV-01 (verified by accepting): change the email to `ada.qa2@mail.com`,
  Save, reopen.
- **Expect** — the list shows the new address; the dialog's badge is **Email unverified** with a *Mark verified*
  action beside it. Saving without touching the address sends no `email` at all (the request is diffed), so a
  plain name edit does not un-verify. Audit `user.email.changed`. `EMAIL_TAKEN` (409) when the address belongs to
  another account.

### VER-02 — An operator can vouch for an address · [verified]

- **Steps** — *Mark verified* on the account from VER-01.
- **Expect** — the badge flips to **Email verified** in place; `POST /users/{id}/email/verify` answers the DTO;
  the super admin's own row is refused (`SuperAdminImmutableException`).

### VER-03 — The username is not a field · [verified]

- **Steps** — open any account.
- **Expect** — the username is read-only with *The username is the identity tokens carry; it cannot be changed.*
  It is what a JWT `sub` holds; renaming it is not an edit, and this console does not offer one.

---

## CLI — Registered clients

A registration is Spring's `oauth2_registered_client` row plus uaa's `client_extension` (enabled, description, last
token) and `client_secret_history` (the grace window). The **type** — `PUBLIC` (auth method `none` alone), `MACHINE`
(`client_credentials` only) or `CONFIDENTIAL` — is derived on every read, never stored. A secret is answered exactly
twice, at registration and at rotation, and can never be read back; the realm's *Sessions & tokens* settings decide
how long a new secret lives (`clientSecretValidityDays`) and how long the one it replaced keeps working
(`clientSecretGraceHours`). `http/admin-client-api.http` runs every call below.

### CLI-01 — Registration answers the secret once · critical · [verified]

- **Steps** — uaa console → Clients → *Register client*: id `qa-machine`, name, description, `client_secret_basic`
  + `client_credentials`, scope `store_core`, Save. Copy the secret from the dialog; *Done* lands on the client's page.
  Mint a token with it (`POST /oauth2/token`, basic auth, `grant_type=client_credentials&scope=store_core`).
- **Expect** — **201** `{client, clientSecret}`; the page shows type *Machine*, *Registered* now, the secret card with
  *Expires* a year out; `GET /clients/{id}` and the list never carry `"clientSecret"`; the token endpoint answers 200.
  A client registered with `none` alone gets `clientSecret: null` and no secret card.
- **Also** — `AdminClientApiIntegrationTest`.

### CLI-02 — The list carries type and status, and filters on the server · high · [verified]

- **Steps** — Clients: the four tiles; segments *All / Enabled / Disabled / Machine / Confidential / Public*; search
  `store`; `GET /clients?q=store&enabled=true&type=MACHINE`; `GET /clients/stats`.
- **Expect** — every row shows client + id, a type badge, the grant types, when the secret expires (amber inside
  thirty days, red past it, *none (PKCE)* for a public client) and an enable switch. The segments carry counts from
  `/stats`; `active + …` add up. The seed answers `total 4, enabled 4, machine 3, confidential 1`.

### CLI-03 — Rotation keeps the old secret alive for the grace window · critical · [verified]

- **Steps** — on `qa-machine`: *Rotate secret*, confirm; copy the new secret. Mint a token with the **old** secret,
  then with the new one. Then *Revoke it now* on the *previous secret still works until …* line, confirm, and mint
  with the old secret again.
- **Expect** — the dialog says both dates: the new secret's expiry and *the secret it replaces keeps working until*
  (now + `clientSecretGraceHours`). Old → 200, new → 200, wrong → 401. After the revocation old → **401**
  `invalid_client`, new → 200; a second revocation answers **404** `UAA.CLIENT.NO_PREVIOUS_SECRET`. Audit
  `client.secret.rotated` twice, the second with detail *previous secret revoked early*.
- **Mechanism** — `GraceAwareClientSecretAuthenticationProvider` hands Spring's own provider a one-client view carrying
  the retired hash; nothing about Spring's checks is re-implemented. `ClientSecretRotationIntegrationTest`.
- **Watch for** — `bcrypt` strength upgrades: Spring re-encodes and *saves* a hash whose strength is below the
  encoder's. On the grace view that save is a no-op on purpose (it would write the old hash back onto the
  registration).

### CLI-04 — Disable revokes and refuses; enable restores · critical · [verified]

- **Steps** — with a live token for `qa-machine`, switch it off in the list (or the Status card). Mint again. Read the
  client. Switch it on. Mint again.
- **Expect** — the toast says the tokens were revoked; `oauth2_authorization` has no rows for the client; the token
  endpoint answers **401** `invalid_client`; `GET /clients/{id}` still answers 200 (an operator can read and re-enable
  it: `findById` is not filtered, only `findByClientId` is); after enabling the token endpoint answers 200. Audit
  `client.disabled` with the revoked count, then `client.enabled`.
- **Also** — `ClientDisableIntegrationTest`, `EnabledAwareRegisteredClientRepositoryTest`.

### CLI-05 — Registration rules answer typed problems · high · [verified]

- **Steps** — register with client id `admin-sdk`; with redirect `http://evil.example/cb`; with access-token lifetime
  `PT48H` while the realm's ceiling is 3600 s; with a wildcard redirect.
- **Expect** — **409** `UAA.CLIENT.ID_TAKEN` with `fieldErrors[0].field == clientId`; **400**
  `UAA.CLIENT.INVALID_REDIRECT_URI` with `params.reason` `PLAIN_HTTP` (`WILDCARD`, `FRAGMENT`, `NOT_ABSOLUTE` for the
  others); **400** `UAA.CLIENT.TOKEN_TTL_EXCEEDS_POLICY` on `tokenSettings.accessTokenTimeToLive`. Under `lcl`,
  `gateway.com` and `*.gateway.com` are allowed over plain http (`com.asrevo.cvhome.uaa.clients.plain-http-hosts`);
  `localhost` always is; a native `com.example.app:/cb` passes.
- **Also** — `RedirectUriRulesTest`, `AdminClientServiceTest`.

### CLI-06 — The realm's ceiling clamps every token · high · [unit only]

- **Steps** — Settings → *Sessions & tokens* → max access-token lifetime 60 s, Save. Mint a token for `admin-sdk`
  (whose own setting is 900 s). Decode `exp - iat`.
- **Expect** — 60. Lowering the ceiling applies at the next token for every client, not only the ones re-saved.
  Mechanism: `JwtCustomizerConfig.clampLifetime`.

### CLI-07 — Rotate every secret · critical · [not verified]

- **Steps** — Settings → *Danger zone* → *Rotate all secrets*, type `ROTATE`, confirm. Copy every secret from the
  dialog into `application-lcl.yml`'s `UAA_*_SECRET` values (or the stack's env) and restart the services.
- **Expect** — one new secret per machine and confidential client, shown once; every service on the stack keeps
  working for `clientSecretGraceHours`, then fails with `invalid_client` unless reconfigured. `POST /rotate-all` as a
  `store_core` token is **403**.
- **Why not verified** — it invalidates the shared local secret for every service on the stack after the grace
  window; run it on a throw-away stack.

### CLI-08 — Delete revokes first · [verified]

- **Steps** — delete `qa-machine` from its page, typing the id.
- **Expect** — its authorizations are gone before the row is; `GET /clients/{id}` → 404. `reset-secret` (the SDK's
  alias) still works and sets a chosen secret with no grace window; on a public client it answers **422**
  `UAA.CLIENT.NOT_CONFIDENTIAL`.

---

## KEY — Signing keys

One key signs (`ACTIVE`); a rotated-out key keeps verifying (`RETIRING`, public half only in the JWKS) until
`settings.keys.retireDays` have passed, then leaves (`RETIRED`). The public JWK is stored plain; the private JWK is a
secret-crypto envelope (`private_jwk_enc` starts with `ENC:`) and never leaves the row unencrypted. Every token carries
the active `kid` in its header, which is how the encoder picks one key while two share the algorithm and how a resource
server knows to refetch the JWKS. `http/admin-key-api.http` runs every call below.

### KEY-01 — Rotation keeps in-flight tokens alive · critical · [verified]

- **Steps** — mint a token, note its header `kid`. Settings → *Signing keys* → *Rotate now*, confirm (or
  `POST /uaa/api/v1/admin/keys/rotate`). Call an admin endpoint with the old token; mint a new one; call with it.
  `GET /oauth2/jwks`; `GET /keys`; `GET /keys/status`.
- **Expect** — the old token still answers 200 (its key is *Retiring*, *leaves the JWKS* in `retireDays`); the new
  token carries the new `kid` and answers 200; the JWKS lists both keys with no `d`, `p` or `q` member; the table shows
  *Active* and *Retiring* with the dates; the status line names the active key, when it started signing and the next
  automatic rotation. Audit `key.rotated` with *replaces <kid>, which verifies until <date>*.
- **Truth underneath** — `select status, left(private_jwk_enc, 4) from uaa.signing_keys` → every row `ENC:`.
- **Also** — `KeyRotationIntegrationTest` (with the test clock: eight days later `retireDue()` retires the old key and
  the old token answers **401**; the JWKS is back to one key), `KeyRotationServiceTest`, `SigningKeyMaterialMapperTest`.

### KEY-02 — Scheduled rotation and retirement · [unit only]

- **Steps** — set *Rotate automatically every* to 1 day, wait past it (or advance the test clock) for the hourly tick.
- **Expect** — `KeyRotationScheduler.tick()` retires what is past its window and rotates when the active key is older
  than the interval; 0 means manual only, and the status line says so. Mechanism: `KeyRotationService.rotateIfDue`.

### KEY-03 — An unreadable key is not an outage · high · [unit only]

- **Steps** — change the crypto provider's key underneath a stored signing key (locally: edit
  `com.asrevo.cvhome.crypto.local.key` in `application-lcl.yml` and restart).
- **Expect** — the active key's private half cannot be opened: it is moved to *Retiring* (its public half still verifies
  what it signed), a fresh key is generated and activated, the log says so, and the status line shows *1 stored key
  cannot be read back*. No token request fails. `KeyRotationServiceTest.anUnreadableActiveKeyIsReplacedNotFatal`.
- **Why the lcl slice pins a static crypto key** — the shared `com.asrevo.cvhome.crypto` config is `LOCAL` with no
  key, which falls back to a *random key per boot*: without `key-provider-type: STATIC` every restart would do exactly
  this to every stored key. A deployment must give the provider a stable key (KMS, or `CVHOME_CRYPTO_KEY`).

### KEY-04 — The gate holds, and nothing answers key material · critical · [verified]

- **Steps** — `POST /keys/rotate` as a `store_core` token; `GET /keys` anonymous; read `GET /keys` and `GET /keys/status`
  as super admin.
- **Expect** — **403**, **401**; the admin reads carry `kid`, algorithm, status and dates only — never a JWK, never
  `ENC:`.

---

## SID — The `"*"` wildcard

_From `qa/unify-store-id-value-objects.md` §EDGE, reformatted into the case shape used everywhere else._

### SID-01 — The `"*"` wildcard still means "every store" · [not verified]

_Was E4._ A super admin or `store_core` service principal carries `store = "*"`, which is deliberately **not**
a valid store id.

- **Steps** — sign in as `super-admin` and list stores.
- **Expect** — all four come back. If this regressed, validation leaked into `StoreMerchantId`'s constructor.
- **Why it was never run** — it needs a super-admin session, and the pass that wrote it had only `org1-admin`.

---

## MIG — Resetting a database

This project is not in production: **there are no migrations.** `schema.sql` is rewritten in place (the
`oauth2_authorization` table changed shape for Spring Authorization Server 7 and the seed's token lifetimes and PKCE
flag changed), so a database created before this branch is reset, not migrated:

```bash
lcl stop --hard --stack <name>          # drops the compose volumes, postgres included
lcl start -d --stack <name>
```

or, keeping the other schemas, `drop schema uaa cascade;` and restart uaa. The seed writers then recreate the
clients and accounts. Nothing in uaa carries an `alter table`.

---

## REG — Regression watchlist

| What broke | How it looked | Caught by |
|---|---|---|
| `ClientAuthMethod.from` recursed into itself | `GET /clients/{id}` 500 with `StackOverflowError` | ADM-07, `ClientAuthMethodTest` |
| A filter-chain 403 dispatched to `/error`, which the SPA router served as `index.html` | a forged POST and a `store_core` call to the admin API both answered **200 with the console's HTML** | SEC-05, SEC-11, `CsrfLoginIntegrationTest`, `AdminUserApiIntegrationTest` |
| uaa's `management.endpoints.web.exposure.include` override lost to the imported `common-config.yml` | every actuator endpoint mapped on the live stack after the "fix" | SEC-01, `ActuatorExposureIntegrationTest` |
| `admin-sdk` seeded with `refresh_token` and consent | a machine client with a grant it can never use | seed review |
| The `roles` JWT claim was a `TreeSet`; the JDBC authorization store serialises claim values with type info the gateway's UserInfo parser refuses | gateway login died with `Could not resolve type id 'java.util.TreeSet'` | AUT-01, `LoginFlowIntegrationTest` |
| A settings field placed inside the wrong `formGroupName` | the Settings page threw at render and showed skeletons for ever | SET-04 |
| A `GET /api/…` was saved as the request to resume after login | signing in landed on raw JSON at `/api/v1/auth/me?continue` | AUT-02 (the request cache excludes `/api/**`) |
| The realm's remember-me key sat one YAML level too high in the local slices | uaa refused to start: `Could not resolve placeholder 'UAA_REMEMBER_ME_KEY'` | boot |
| The attempt that crossed the lockout threshold was reported as `attemptsLeft=0` | the sign-in page said "0 attempt(s) left before a 15-minute lock" on an account that was already locked | LCK-01, LCK-03, `LoginFailureHandlerTest` |
| `/account` had no rail row, so the breadcrumb fell back to *Users* | "cvhome identity › Users" over the account page | SES-06 |
| A password-policy field error carried only the rule key as its message | the accept page said **"upper"** under the password | INV-01 (`params.rule`, translated by the kit's `errors.code.UAA_PASSWORD_POLICY_VIOLATION`) |
| The accept page's mismatch check was a `computed` over a reactive form | two different passwords submitted with no message, then the server's 400 | INV-05 |
| The Users tiles and tabs translated once and never again | switching to Arabic left *ACCOUNTS* / *All 12* in English | CON-05 (the `computed`s read `activeLang()` first) |
| The Users panel rendered its empty state under a 403 | a `USER`-role session saw *No accounts* beneath the error bar | CON-06 |
| The grace-aware client-secret provider was a bean | Spring adopted the lone `AuthenticationProvider` bean as the global manager's provider: every form login failed and lockout stopped counting | AUT-02, LCK-01 (the provider is built inside the SAS chain, never a bean) |
| Spring's provider saves an upgraded hash through the registry it was given | `UnsupportedOperationException` from the grace view's read-only `save` on the first token with a `{bcrypt}` strength-10 seed | CLI-03 (`SingleClientRepository.save` is a no-op) |

---

## 99 — Known gaps

**Members are not reconciled with tenancy.** A user deleted in uaa leaves a membership row behind in tenancy,
and removing a member in tenancy does not remove their uaa user.

**The shared local secret is in the `lcl` slices in plain text.** It is local seed configuration and never leaves
the machine, but it means any local service can mint a `store_core` token. `application.yml` itself has no default
any more; `fargate` must set every `UAA_*` variable.

**`/logout` accepts GET.** The kit's `AuthService.logout()` navigates rather than posts, so the logout matcher takes
any method. A GET logout is a link that signs you out; it should become a POST once the kit posts.

**Lessons closed by this branch.** `lessons.md` entries "Roles — a role is a name", "Users — metadata is merged,
never replaced", "Users — creating an account is two calls, and there are no invites" and the email half of
"Users — email and username cannot be changed here" now describe the old system; each carries its *Closed by*
line. Still open: realm switcher, notifications, MFA, CSV import, SAML, and providers on the sign-in page.

**The outbox row holds the plaintext link until it completes.** `uaa.outbox_record.payload` carries
`InvitationIssuedEvent` / `PasswordResetLinkIssuedEvent` with the link in clear until the handler runs (seconds,
under `lcl`). A stuck outbox would keep live links readable to anyone with the database. Encrypting the payload
or carrying only the token hash plus a fetch is a follow-up for the delivery service.

**The invitee's shell is empty.** A `USER`-role account that signs in to uaa's console reaches the shell and gets a
403 bar on every admin screen (CON-06); `/account` is the only page it can use. A landing on `/account` for
non-admins is a follow-up.

**The signing keys' crypto key is the platform's LOCAL provider.** `common-config.yml` sets
`com.asrevo.cvhome.crypto.type: LOCAL` with no key, which resolves ENV → FILE → *random per boot*. uaa's `lcl` and
`test-stores` slices pin a static key so a restart can read the stored private halves (KEY-03); `fargate` must supply
one. cua's social-login secrets sit on the same provider and have the same exposure.

**Delivery is a log line.** `LoggingLinkDeliveryHandler` is the only consumer of the link events; the SMS /
WhatsApp / email service that subscribes to `uaa-events` does not exist yet, so the operator carries every link.

**~~uaa has no `http/` directory.~~** Closed: `http/admin-user-api.http`, `http/admin-role-api.http` and
`http/admin-client-api.http` now cover all three admin controllers, each including the 403-as-org-admin case.
The loose `req.http` at the module root is gone (it held plaintext credentials).

**`PermissionAccessChecker.hasReadAccessOnStore` never checks `isSuperAdmin`,** so a super admin gets 403 on
`store-info`. Tracked with the `isOrgAdmin` gap in tenancy-qa.md 99.

---

Raise anything unexpected against the uaa PR. Include the username, the client id, the time, and the matching
lines from `.lcl/<stack>/logs/uaa.log` — and never paste a token or a secret into the report.

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
- **Cases** — 64 (47 verified, 5 unit only, 12 not verified)
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

### DSN-16 — Creating a user is two calls · high · [verified]

- **Steps** — Users → **Create user**, fill username and email, Save.
- **Expect** — the account is created **and the password dialog opens by itself**. Set a password,
  then sign in as that account (DSN-16 is not complete without this).
- **Why the second half is not optional.** `createUser` never sets a password hash and `enabled`
  defaults to `true`, so skipping it leaves an account that exists, looks enabled, and cannot sign
  in. Presenting create as one step is how that happens.
- **[not verified]** — signing in as the created account. Everything up to and including
  set-password is verified.

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

### SEC-06 — Lockout, rate limiting, password policy, audit · [not verified]

- Phases 2 and 3 of the plan; their sections (`LCK`, `RL`, `PWD`, `AUD`) arrive with them.

### SEC-07 — Short tokens and PKCE · high · [verified]

- **Steps** — decode an operator token and a service token; start the console login without `code_challenge`.
- **Expect** — `exp - iat` ≤ 900 s; the seed's refresh token is 12 h and its authorization code 5 min;
  `/oauth2/authorize` for `web-app` without a challenge is refused with `invalid_request`.
- **Seen** — `LoginFlowIntegrationTest` (both). The gateway canary (AUT-01) is what proves the real client still
  sends PKCE.

### SEC-08 — Signing keys · [not verified]

- Phase 6 of the plan (`KEY`). For now: a key that fails to parse is **logged at ERROR** and left out of the JWK
  set, where it used to disappear silently.

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

---

## 99 — Known gaps

**Members are not reconciled with tenancy.** A user deleted in uaa leaves a membership row behind in tenancy,
and removing a member in tenancy does not remove their uaa user.

**The shared local secret is in the `lcl` slices in plain text.** It is local seed configuration and never leaves
the machine, but it means any local service can mint a `store_core` token. `application.yml` itself has no default
any more; `fargate` must set every `UAA_*` variable.

**`/logout` accepts GET.** The kit's `AuthService.logout()` navigates rather than posts, so the logout matcher takes
any method. A GET logout is a link that signs you out; it should become a POST once the kit posts.

**`DSN-15` is stale.** uaa can unset a metadata key now (send it with a `null` value); the console still disables
the remove button on stored keys.

**~~uaa has no `http/` directory.~~** Closed: `http/admin-user-api.http`, `http/admin-role-api.http` and
`http/admin-client-api.http` now cover all three admin controllers, each including the 403-as-org-admin case.
The loose `req.http` at the module root is gone (it held plaintext credentials).

**`PermissionAccessChecker.hasReadAccessOnStore` never checks `isSuperAdmin`,** so a super admin gets 403 on
`store-info`. Tracked with the `isOrgAdmin` gap in tenancy-qa.md 99.

---

Raise anything unexpected against the uaa PR. Include the username, the client id, the time, and the matching
lines from `.lcl/<stack>/logs/uaa.log` — and never paste a token or a secret into the report.

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
- **Cases** — 29 (13 verified, 3 unit only, 13 not verified)
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

**Seeded clients** — `web-app` (the console's authorization-code client) and
`store-core@service.store-core.internal` (client credentials, scope `store_core`). On a shifted stack the
`web-app` redirect URIs are rewritten by an `after-up` hook; `lcl events` records `uaa.redirects.patched`.

```bash
# a service-to-service token
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
```

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
- **Expect** — an `org` claim and the role authorities the permission evaluator reads. A missing `org` claim is
  tolerated by `ManagerOrgId`'s lenient `String` constructor (see
  [tenancy-qa.md](../../tenancy/tenancy-service/qa/tenancy-qa.md) 99) — `SecurityUtils` relies on it, so do not
  "fix" it.

### AUT-07 — `/userinfo` answers for a signed-in operator · [not verified]

- **Steps** — call `/userinfo` with an operator token.
- **Expect** — 200 with the subject, and **not** a password hash, a client secret or an internal id the console
  has no business seeing.

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

### ADM-02 — An org admin is refused every admin endpoint · critical · [not verified]

- **Steps** — as `org1-admin`, repeat ADM-01, then attempt `POST /users`, `PUT /users/{id}`,
  `DELETE /users/{id}`, and the same across `/roles` and `/clients`.
- **Expect** — **403** on every one. This is the case that proves the double gate; an org admin reaching any of
  it is a platform-wide escalation.

### ADM-03 — Anonymous is refused · critical · [not verified]

- **Steps** — call any `/api/v1/admin/**` path with no token.
- **Expect** — **401**. `/.well-known/**`, the actuator endpoints, the login page and the swagger paths are
  deliberately open; nothing else is.

### ADM-04 — Enable and disable actually take effect · high · [not verified]

- **Steps** — disable a seeded account, try to sign in as it, re-enable it, sign in again.
- **Expect** — the sign-in fails while disabled and succeeds after. `uaa.users.enabled` follows.
- **Restore it afterwards** — the seed only runs on a clean database.

### ADM-05 — Roles created here appear to the console · high · [verified]

- **Steps** — add a role through `/api/v1/admin/roles`, then open the console's role picker.
- **Expect** — the console renders the unknown role rather than crashing or dropping it — see U-11 below,
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
screens are [console-ui-qa.md](../../console-ui/qa/console-ui-qa.md)._

### PERM-02 — The new password actually signs in · critical · [not verified]

After PERM-01, sign out and sign in as `org1-store1-moderator` / `Passw0rdQA`.

**Expect** — the console opens. Set it back to `admin` afterwards, or note that you changed it.

### U-10 — The role picker never offers platform superuser · critical · [unit only]

Open the create form.

**Expect** — **Store administrator** and **Store moderator**, and nothing else.
`GET …/assignable-roles` really does return `SUPER_ADMIN` to an org admin — it filters uaa's role table by
removing only `USER` and `ORG_ADMIN`. The console intersects rather than filters one name, so a role added to
uaa later cannot appear unreviewed either. **This is defence in depth, not a fix**: `lessons.md`, "Users —
assignable-roles offers SUPER_ADMIN to an org admin".

### U-11 — A role the console has never seen · high · [unit only]

Add a role to `uaa.roles` and grant it to a user.

**Expect** — the row humanizes it (`REGIONAL_BUYER` → `Regional Buyer`) rather than the page going blank.
Transloco throws on a missing key and a role is a database row, not an enum.

---

### P-02 — What it shows, and what it says instead · high · [not verified]

**Expect** — the username, the roles, and a notice explaining that the console can see nothing else.
**No name, email, avatar, phone, job title, timezone, date format or bio** — none has a column anywhere, and
the account record is unreachable twice over (`lessons.md`, "Users — the JWT carries no user id"). Empty
fields would read as "you have not filled these in"; the notice is the honest version.

### P-03 — No password control · high · [unit only]

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

### CON-06 — A non-super-admin gets nothing · critical · [not verified]

- **Steps** — sign in as `org1-admin` and open each of the three sections.
- **Expect** — the shell renders (they are authenticated) and every list fails with a permission error the
  toast reports. The double gate in ADM-02 is what enforces it; this case is that the console *renders* the
  refusal rather than an empty table that looks like there is nothing to see.

### CON-07 — Write paths round-trip · high · [not verified]

- **Steps** — from Users: disable an account, re-enable it, reset its password, grant and revoke a role.
  From Clients: register one, rotate its secret, delete it.
- **Expect** — a toast per action and the list re-reads from the server rather than echoing what you typed.
  Most of these endpoints answer `void`, which is why the page re-fetches.
- **Restore whatever you change** — the seed only runs on a clean database.

### CON-08 — Rebuilding the kit under a running dev server · [verified]

- **Steps** — with `ng serve` running, rebuild `store-commons/ui-kit`.
- **Expect** — this is the one that will waste your afternoon. The `file:` dependency is a symlink into the
  kit's `dist`, and rebuilding it replaces that directory underneath the watcher; the dev server can latch a
  resolution failure and show `TS2307: Cannot find module '@cvhome-saas/ui-kit'` over a page that is otherwise
  fine. Restart the app (`lcl restart console-ui`), do not go hunting for a broken import.

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

## 99 — Known gaps

**Members are not reconciled with tenancy.** A user deleted in uaa leaves a membership row behind in tenancy,
and removing a member in tenancy does not remove their uaa user.

**The client secret is in `application.yml` in plain text for `lcl`.** It is local seed configuration and never
leaves the machine, but it means any local service can mint a `store_core` token. Do not copy that pattern to
`fargate`.

**~~uaa has no `http/` directory.~~** Closed: `http/admin-user-api.http`, `http/admin-role-api.http` and
`http/admin-client-api.http` now cover all three admin controllers, each including the 403-as-org-admin case.
A loose `req.http` remains at the module root and should be folded in or deleted.

**`PermissionAccessChecker.hasReadAccessOnStore` never checks `isSuperAdmin`,** so a super admin gets 403 on
`store-info`. Tracked with the `isOrgAdmin` gap in tenancy-qa.md 99.

---

Raise anything unexpected against the uaa PR. Include the username, the client id, the time, and the matching
lines from `.lcl/<stack>/logs/uaa.log` — and never paste a token or a secret into the report.

# QA — cua (`store-pod/cua`)

cua is the **shopper's** authorization server, one per pod, serving every store on it. Unlike
[uaa](../../../store-core/uaa/qa/uaa-qa.md) — which has a fixed set of staff clients — cua registers its
clients **dynamically, per store**: `DynamicRegisteredClientRepository` derives a store's client and its
`redirect_uri` from the request it is answering, which is why so much of this file is about hostnames and
ports rather than about tokens.

- **Scope** — the shopper sign-in hand-off (cua is **headless**: it redirects to the storefront's themed login
  page and processes the posted form), JSON registration, the per-store dynamic client registration, social
  login configuration (`SocialLoginConfigApi`), and the `/cua` path-prefix handling the edge depends on
- **Runs on** — `lcl start -d --stack <name>`; reached only through the pod edge at
  `http://<store>.spg-507f1f77.gateway.com/cua/**`, never on `:8124` directly
- **Cases** — 22 (9 verified, 4 unit only, 9 not verified)
- **Also see** — [spg](../../spg/qa/spg-qa.md) (which keeps the `/cua` prefix and sets `X-Forwarded-Port` for
  exactly this service), [merchant](../../merchant/merchant-service/qa/merchant-qa.md) (the store record cua
  caches), [landing-ui](../../landing-ui/qa/landing-ui-qa.md) (the storefront that starts the login),
  [checkout](../../checkout/checkout-service/qa/checkout-qa.md) (which needs the shopper identity)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

**cua had one case in the whole `qa/` tree before this document** — SID-01 below. Everything else was written
from `DynamicRegisteredClientRepository`, `AuthorizationServerConfig`, `PathPrefixFilter`, `StorefrontUrls`,
`PublicRegistrationController` and `SocialLoginConfigController`. The hand-off, registration and social-list
cases are covered by `LoginHandoffIntegrationTest`, `PublicRegistrationControllerIntegrationTest` and
`PublicSocialLoginControllerIntegrationTest`; `http/*.http` carries the runnable blocks.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins and the seeded ids are in
[`references/qa-testing.md`](../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5. Only
what is specific to cua is below.

**The shopper account is `user` / `revo`**, and it is scoped per store (`cua.users.client_id`). It
authenticates **only through the store host** — posting to `localhost:8124/login` directly always fails, and
that is not a bug. Every case below therefore starts at
`http://org1-store1.spg-507f1f77.gateway.com`.

### Looking at the truth underneath

```bash
docker exec cvhome-postgres-1 psql -U postgres -d cvhome -c \
  "select * from cua.users;"
... "select * from cua.social_login_config;"
```

Logs: `.lcl/<stack>/logs/cua.log`.

---

## LGN — Shopper sign-in

### LGN-01 — A shopper signs in through the store host · critical · [verified]

- **Steps** — from `http://org1-store1.spg-507f1f77.gateway.com`, click the account icon. Watch the network
  tab: `GET /cua/oauth2/authorize?…&lang=en` → `302 /en/login?auth=1` (the storefront's page, themed), fill in
  `user` / `revo`, submit.
- **Expect** — the form posts to `POST /cua/login` (form-encoded, with `client_id` and `lang` hidden inputs) →
  `302 /cua/oauth2/authorize?…` → `302 /en/callback?code=…` → back on the storefront, signed in, with a shopper
  token the storefront and checkout accept. cua never renders HTML at any step.

### LGN-02 — Signing in on `localhost:8124` fails, by design · [not verified]

- **Steps** — post the same credentials to cua's own port.
- **Expect** — refused. The client is derived from the **host**, so there is no client to authenticate
  against. Record it as expected rather than raising it.

### LGN-03 — A shopper of one store cannot sign in to another · critical · [verified]

> Also covered from the token side by RLM-03 and RLM-04.

- **Steps** — sign in on org1-store1, then present that session on `org2-store1.spg-507f1f77.gateway.com`.
- **Expect** — not signed in. Two stores on one pod share the service and must not share shoppers; this is the
  single most important case in this file.

### LGN-04 — Registration creates a shopper for **this** store only · critical · [verified]

- **Steps** — register a new shopper on org1-store1 (`/en/register` in the browser, or the first block of
  `http/public-registration-controller.http`), then sign in with it on org1-store2.
- **Expect** — it works on the first and not the second (`…/login?auth=1&error=invalid`), and the row carries
  the store it was created for. Covered by `LoginHandoffIntegrationTest.aShopperOfOneStoreIsNobodyOnAnother`.

### LGN-05 — A duplicate registration is refused cleanly · high · [verified]

- **Steps** — the 409 blocks of `http/public-registration-controller.http`: the seeded username, the seeded
  email, then the same username on `STORE_ID_2`.
- **Expect** — `409 CUA.REGISTRATION.USERNAME_TAKEN` / `EMAIL_TAKEN` with a problem body, never a 500; the
  cross-store one is `201`, because a shopper is per store. Covered by
  `PublicRegistrationControllerIntegrationTest`.

### LGN-06 — The hand-off carries the store host and its port · critical · [verified]

- **Steps** — on a shifted stack (`lcl start -d --stack xxx`), start a login and read the `Location` of the
  `302` from `/cua/oauth2/authorize`.
- **Expect** — `http://org1-store1.spg-507f1f77.gateway.com:<spg-port>/en/login?auth=1` — the store's host
  **with** the port, the shopper's locale as the path prefix, never `/cua/login`. Same origin rule as CLI-02.
  Walked on `lcl start -d --stack shifted` (offset +2000): `302 http://org1-store1.spg-507f1f77.gateway.com:2080/en/login?auth=1`,
  the storefront page on `:2080` carrying the form and `_csrf`, and the login POST resuming
  `…:2080/cua/oauth2/authorize?…`.

### LGN-07 — A wrong password comes back to the form, and the form still works · high · [verified]

- **Steps** — on the storefront's login page submit `user` / `wrong`, then `user` / `revo`.
- **Expect** — `302 /en/login?auth=1&error=invalid`, the page shows the translated message under the theme's
  styling, and the second submit completes the flow: the saved authorize request survived the failure.
  Covered by `LoginHandoffIntegrationTest`.

### LGN-08 — Social buttons only appear while cua is waiting · high · [verified]

- **Steps** — open `/en/login?auth=1` after starting a login (buttons present for the store's enabled
  providers), then open `/en/login` directly (no buttons: the page starts the flow instead).
- **Expect** — each button navigates to `/cua/oauth2/authorization/<store>.<provider>` and cua redirects to the
  provider. A provider failure lands on `…/login?auth=1&error=social`. Locally the seeded providers carry demo
  app ids, so the provider itself refuses — that is expected. Walked with curl through spg: with a saved
  request, `/cua/oauth2/authorization/<store>.google` → `302 https://accounts.google.com/o/oauth2/v2/auth?…`,
  and the provider's refusal (`/cua/login/oauth2/code/<store>.google?error=access_denied&state=…`) →
  `302 …/en/login?auth=1&error=social`.

### LGN-09 — A stale `/cua/login` link is sent to the storefront · [unit only]

- **Steps** — open `http://org1-store1.spg-507f1f77.gateway.com/cua/login` directly.
- **Expect** — `302 /en/login` (no marker → the storefront starts a login); mid-flow it is
  `302 /en/login?auth=1`. Covered by `LoginHandoffIntegrationTest.theOldLoginPageUrlRedirectsToTheStorefront`.

---

> **Before any of these:** cua runs the shared server from `store-commons/sso/sso-core`, and `bootRun` puts that
> module's jar on its classpath. If you rebuilt `sso-core` while the stack was up, restart **both** deployments —
> `lcl restart uaa cua` — before trusting anything below. The symptom otherwise is a `ClassNotFoundException` for
> a class that plainly exists, because the jar was replaced under a running JVM.

## RLM — Realms: one per store

cua is the multi-realm deployment of the SSO server in `store-commons/sso/sso-core`; uaa is the same code with a
single realm. A realm here **is** a store, and these are the cases that prove the boundary holds. All were run
against a live stack on 2026-09-03.

### RLM-01 — The host decides which store a request belongs to · critical · [verified]

- **Steps** — the pod edge resolves the storefront host to a store and sets `Store-Id`; `spg`'s `/cua*` route does
  the same lookup the storefront route does. Sign in normally at `org1-store1.spg-507f1f77.gateway.com`.
- **Expect** — the realm is the store the host resolved to, not the `client_id` on the form. cua used to take the
  tenant straight out of that form field, which let the request name its own tenant.

### RLM-02 — A request that names another store is refused · critical · [verified]

- **Steps** — `POST /cua/api/v1/public/registration?store=<store-2-id>` at **store 1's** host. Same again against
  `POST /cua/oauth2/token`.
- **Expect** — `403` `UAA.REALM.CROSS_STORE_REQUEST`, as `application/problem+json`, naming both stores. Not a
  500: the refusal comes from a filter, which throws outside the `@ControllerAdvice`, so `RealmFilter` renders the
  problem body itself. It answered a bare 500 the first time this was run.

### RLM-03 — One username is a different person in each store · critical · [verified]

- **Steps** — register `qa-shopper-1` on store 1, then the same username **and** email on store 2.
- **Expect** — both `201`. A third attempt on store 1 is `409 UAA.USER.USERNAME_TAKEN`. Uniqueness is
  `(realm_id, username)` and `(realm_id, email)`, never global.

### RLM-04 — A shopper token is accepted at its own store and refused at another · critical · [verified]

- **Steps** — sign in on store 1, take the access token, and call
  `GET /checkout/api/v1/private/customer/orders?store=…` first with store 1, then with store 2, then with no token.
- **Expect** — `200`, then `403`, then `401`. This is the whole tenant boundary in three requests:
  `StoreRoleAccessChecker.isStoreCustomer` matches the token's store claim against the `?store=` of the request.

### RLM-05 — The token says which store, which role, and who · critical · [verified]

- **Steps** — decode the access token from RLM-04.
- **Expect** — `iss` is the pod's one issuer (`…/cua`), never the store host; `realm` carries the store id;
  `roles` is `["CUSTOMER"]`; `sub` is the account **UUID**, not the username. There is no `clientId` claim: it
  was emitted alongside `realm` while the readers moved over, and it held the same value only because a store had
  exactly one client.
- **Why each matters** — without `roles` the pods' `ROLE_CUSTOMER` ceiling admits nothing and every shopper call
  to checkout is refused. Without the store claim the store check fails even with the role. And `sub` must not be
  the username: all four demo shoppers are called `user`, and checkout joins its customer records on `sub`, so
  two stores' shoppers would merge. All three were wrong the first time this was run.

### RLM-06 — A rejected registration leaves no account behind · high · [verified]

- **Steps** — register with a password the realm's policy refuses (`secret1`), then look for the row.
- **Expect** — `400 UAA.PASSWORD.POLICY_VIOLATION` and **no** `cua.users` row. The account is saved before the
  password so password history has a row to point at, and every refusal is a checked exception — which Spring
  does not roll back for by default. It left an enabled, password-less account holding the username against the
  person trying to claim it.

### RLM-07 — Shoppers get the realm's password policy · high · [verified]

- **Steps** — register with `secret1`, then with `Str0ng-Passphrase!`.
- **Expect** — refused naming the rules that failed (`minLength`, `upper`), then `201`. Registration goes through
  the same `PasswordService` as every other password in the server; cua used to encode it directly, so shoppers
  had no policy, no history and no breach check.

### RLM-08 — Social sign-in uses the store's own provider row · critical · [verified]

- **Steps** — `GET /cua/oauth2/authorization/google` at store 1's host, then at store 2's. Same for `github`.
- **Expect** — `302` to the provider with that store's client id, PKCE, and
  `redirect_uri=…/cua/login/oauth2/code/google` **on the host the request came from**. The registration id is the
  bare alias: it is unique within the realm, and the realm comes from the host, so the callback URL a merchant
  registers with Google is still per-store because it is built on their own domain.
- **Known trap** — a `500` here with `UAA.IDP.CONFIG_INVALID` means the provider row has no endpoints. A stored
  provider is self-contained: `IdentityProviderMapper` resolves the preset's defaults into the row when one is
  created through the API, and nothing fills them in on read. A row seeded without them cannot be built.

### RLM-09 — Brokered sign-in links to an account that already exists · critical · [verified]

- **Setup** — a store with a real Google and GitHub application configured (the seeded demo credentials cannot
  complete a round trip).
- **Steps** — sign in with GitHub, then sign in again with Google using the same address.
- **Expect** — the first creates the account (`user.created … provisioned by provider github`) and links the
  identity; the second links a second identity to the *same* account and signs in. Both audit rows are in
  `cua.audit_events`.
- **Why LINK and not CONFIRM** — `CONFIRM` asks for the account's password once before linking, and cua has
  nowhere to ask: it renders no HTML and the storefront has no confirmation step, so the outcome reaches the
  shopper as a bare "we couldn't sign you in with that provider" with no way forward. `LINK` joins silently, and
  only where the provider vouches for the email — `trust_email_verified` is what gates that.

> **Known gap.** `LINK` still falls back to a confirmation when the provider does *not* vouch for the email, and
> cua cannot complete that either — the shopper sees `?error=social` with no explanation. A storefront
> confirmation step, and an error token distinct from `social`, is outstanding work.

### RLM-10 — A shopper sees their own sessions, and only their own · critical · [verified]

- **Setup** — register the same username on store 1 and store 2 (RLM-03 leaves you with exactly this), and sign
  in as each, in two browsers or two private windows.
- **Steps** — `GET /cua/api/v1/account/sessions?store=<own store>` as each shopper. Then ask as the *other*
  store: `GET /cua/api/v1/account/sessions?store=<other store>` with the first shopper's cookie.
- **Expect** — one session each, its own. The cross-store ask is `403`, not that store's sessions. Then
  `DELETE /cua/api/v1/account/sessions` as shopper 1 and confirm shopper 2 is still signed in.
- **Why** — one `cua.spring_session` table holds every store's sessions and one index answers "which are this
  account's". That index used to hold the username, which is unique only within a realm, so two shoppers called
  `user` shared it: each listed the other's address, browser and start time, and could end them. The principal
  name is the account id now. Pinned by `LoginHandoffIntegrationTest.sameNamedShoppersOfTwoStoresDoNotShareSessions`,
  which sees two sessions where one belongs when the old keying is put back. Run against a live stack on
  2026-09-03 with two `qa-mia` accounts: one session each, and `403` for the cross-store ask.

### RLM-11 — A password change signs out one store's shopper, not the other's · high · [verified]

- **Setup** — as RLM-10.
- **Steps** — change shopper 1's password. Check shopper 2's session and access token still work.
- **Expect** — shopper 2 is untouched: its session still answers, and only store 1's row has the later
  `password_changed_at`. Revocation reads `oauth2_authorization` by principal name, which is the same index the
  sessions use and was keyed the same wrong way.

### RLM-12 — A store cannot set a policy past the platform's limits · high · [unit only]

> Exercised on uaa's admin settings API instead — see `store-core/uaa/qa/uaa-qa.md` **SSO-05**, which is the
> same code path. cua has no merchant-facing settings endpoint yet; it arrives with the console screens.

- **Steps** — as a merchant, `PUT` the realm's settings with `lockout.threshold` of `1000000`, then with a
  refresh-token TTL of ten years, then with `auditRetentionDays` of `1`.
- **Expect** — `400 UAA.SETTINGS.INVALID` each time, naming the field. A value inside the ceiling
  (`lockout.threshold` of 20) is accepted.
- **Why** — the pod is shared. A store that turns lockout off by setting a threshold nobody reaches, or mints a
  token that outlives the store, is weakening a deployment other merchants' shoppers sign in to. The ceilings are
  deployment configuration: no API returns them, and the merchant sees only that the value was refused.

### RLM-13 — One store cannot spend another store's login budget · medium · [unit only]

- **Steps** — from one address, post wrong passwords at store 1 until `429`. Immediately try a *correct* sign-in
  at store 2 from the same address.
- **Expect** — store 2 still signs in. Keep spraying across stores and the address is refused on its own after
  `spread` times the limit (5× by default).
- **Why** — one deployment serves every store on the pod, so counting an address once let a burst aimed at one
  store lock every other store's shoppers out. Pinned by `RateLimitFilterTest`.

### RLM-14 — Audit retention actually runs · medium · [not verified]

- **Steps** — set a realm's `auditRetentionDays` to its floor, insert an older `cua.audit_events` row for that
  realm, and run the nightly job (`0 17 3 * * *`) or invoke `AuditRetentionJob.trim()`.
- **Expect** — the row is gone, and rows of *other* realms are untouched.
- **Why** — audit rows are `@TenantId` rows and the job ran in no realm at all, so the delete was filtered to the
  sentinel realm and matched nothing: retention a merchant configured did nothing, quietly, while the table grew.
  It sweeps realm by realm now, each in its own transaction.

### RLM-15 — cua reads and writes cua's tables, never uaa's · critical · [verified]

- **Setup** — a signed-in shopper holding an access token, so `cua.oauth2_authorization` has a row.
- **Steps** — count `uaa.oauth2_authorization` and `cua.oauth2_authorization`. Change the shopper's password,
  which revokes every authorization the account holds. Count both again.
- **Expect** — cua's row is gone and **uaa's count is unchanged**. Same for `audit_events`: cua's dashboard
  counts cua's logins.
- **Why** — the two deployments share one database and are separated by schema. The raw SQL extracted from uaa
  still said `uaa.oauth2_authorization`, so cua's revocation read — and deleted from — uaa's table, and cua's
  dashboard reported uaa's numbers. The connection's own schema decides now
  (`spring.datasource.hikari.schema`), and `SsoSqlSchemaTest` fails the build if a qualifier comes back.
  Verified 2026-09-03: uaa stayed at 10 rows while cua went 1 → 0.

### RLM-19 — A platform operator reads a pod's SSO dashboard, one store at a time · high · [verified]

- **Setup** — a uaa token with `scope=super_admin` (`admin-sdk` client credentials), and a second uaa token
  with `scope=store_pod`.
- **Steps** — `GET {cua}/api/v1/admin/dashboard?store={STORE_1}` with no token, then with the super-admin
  token, then for `STORE_2`, then with no `?store=`, then with the `store_pod` token.
- **Expect** — 401 · 200 · 200 with **different** `users.total` · 200 with `users.total = 0` · 403.
  Verified 2026-09-03: 401, then `users.total` 2 for STORE_1 and 1 for STORE_2, 0 with no store, 403 for
  `store_pod`.
- **Why** — the admin endpoints used to be `denyAll` in cua. They are now claimed by the staff chain, which
  authenticates a uaa token, so a shopper principal cannot reach them whatever it presents, and each endpoint
  keeps its own `@PreAuthorize`. The realm comes from `?store=`; naming no store yields `NO_REALM` and
  therefore nothing, which is the safe direction to fail.
- **Expected to fail** — `activeSessions`, `tokensIssued`, `topClients` and `recentFailures` are **the same
  whatever store is named**, including with no store at all. See gap 99-RLM-19 below: those come from raw SQL
  that carries no realm predicate. Only `users` is realm-scoped today.

### RLM-16 — A provider endpoint pointing inside the network is refused · critical · [verified]

- **Steps** — as a merchant, save an identity provider whose issuer is `https://169.254.169.254/`, then
  `https://127.0.0.1/`, then `https://10.0.0.5/`, then `http://accounts.google.com/` (plain HTTP), then
  `https://accounts.google.com@127.0.0.1/`.
- **Expect** — `400 UAA.IDP.ENDPOINT_REFUSED` every time, naming the field. The message says only that the
  endpoint is not allowed: a merchant who can tell "private address" from "bad scheme" has been handed a port
  scanner, because the difference in the answer *is* the scan result.
- **Then** — press `test` more than 30 times in an hour on one store and expect
  `429 UAA.IDP.TEST_THROTTLED`; a second store's budget is untouched.
- **Why** — these URLs are merchant-entered and this server fetches every one of them: on save, on test, and on
  every sign-in through the provider. Unbounded that is a request forger inside the pod's network, with cloud
  metadata one hop away. Pinned by `EgressGuardTest`.
- **Expected to fail locally** — the `lcl` slice sets `allow-private-addresses: true`, because the demo providers
  answer on localhost. Flip that slice back to the defaults, restart cua, and it bites.
- **Verified 2026-09-03** exactly that way: all four refused with `UAA.IDP.ENDPOINT_REFUSED` naming `issuerUri`
  and one identical message, and a real provider (`https://accounts.google.com`) was accepted in the same
  breath — `201`, with `hasClientSecret: true` and no `clientSecret` anywhere in the response.

### RLM-17 — A signed-in session is refused in another store, not ended · high · [unit only]

- **Setup** — a shopper signed in on store 1.
- **Steps** — call `GET /cua/api/v1/account/sessions?store=<store 2>` with that session cookie. Then call it
  again with `?store=<store 1>`.
- **Expect** — `403 UAA.REALM.CROSS_STORE_REQUEST`, and then `200`: the session survives the refusal.
- **Why** — a session that any request can destroy by naming another store in a query parameter is a
  forced-logout button for anyone who can make a browser follow a link. Refusing costs the caller the request and
  the owner nothing.

### RLM-18 — An org admin reaches another org's store on the same pod · critical · [verified] · **known gap**

- **Steps** — signed in as `org1-admin`, call
  `GET /spg/cua/api/v1/private/shoppers?store=<a store of org 2>&pod=…` through the seller gateway.
- **Expect today** — `200`, and **it is that other org's shopper**. Verified 2026-09-03: store 1 answered the
  account ending `cdd4` and org 2's store answered `cdd1`, which are different rows in different realms.
- **What is actually wrong** — not the tenancy. The realm switched correctly and `@TenantId` returned exactly the
  rows of the realm asked for; every isolation case here still holds. What fails is the authorization question
  *may this operator administer this store*: `StoreRoleAccessChecker.isOrgAdmin` answers yes for any store on a
  pod the org is allowed on, and says so — `// @TODO find better way to know if requested store created by this
  org admin`. `StoreOrgOwnerRetriever` is the seam left for the fix: an interface with no implementation and no
  callers.
- **Not this feature's** — the same call against `customer`'s pre-existing
  `/spg/customer/api/v1/private/customers` answers `200` for the same foreign store. It is platform-wide, it
  predates the SSO work, and closing it means giving every pod service a store→org lookup — its own change, with
  its own QA. Recorded here because this is where it was found.

## CLI — The dynamic client, and the port that must survive

### CLI-01 — The `redirect_uri` is derived from the request's real host · critical · [not verified]

- **Steps** — start a login from the storefront and read the authorization request.
- **Expect** — `redirect_uri=http://org1-store1.spg-507f1f77.gateway.com/en/callback` — the store's own host
  with the locale prefix, not the pod's, not `localhost`. The login hand-off (LGN-06) is built by the same
  `StorefrontUrls.origin` rule, so the two can never disagree about host or port.

### CLI-02 — On a shifted stack the port survives · critical · [not verified]

- **Why it exists** — Caddy sends `X-Forwarded-Proto` and `X-Forwarded-Host` but never `X-Forwarded-Port`, and
  Tomcat's `RemoteIpValve` strips the port out of `X-Forwarded-Host` and falls back to 80/443. Without spg's
  `request_header X-Forwarded-Port`, `DynamicRegisteredClientRepository` builds
  `redirect_uri` with no port and the redirect does not match.
- **Steps** — on a second stack (`lcl start -d --stack xxx`), start a shopper login.
- **Expect** — `redirect_uri=http://org1-store1.spg-507f1f77.gateway.com:<spg-b>/callback`, **with** the port.
  On the default stack the port is 80 and the defect is invisible, so this must be run on a shifted stack.
- **Cross-reference** — [spg-qa.md](../../spg/qa/spg-qa.md) HDR-01 and
  [`qa/lcl-qa.md`](../../../qa/lcl-qa.md) case 09. LGN-06 exercises the same header on the login hand-off and was run on a
  shifted stack, so the port is known to survive.

### CLI-03 — The `/cua` prefix reaches the service intact · critical · [not verified]

- **Steps** — compare what cua logs as its request path with what catalog logs.
- **Expect** — cua sees `/cua/...` (the edge uses `handle`, not `handle_path`) plus
  `X-Forwarded-Prefix: /cua`; `PathPrefixFilter` accounts for it when building absolute URLs. Catalog, by
  contrast, sees its path with the prefix stripped.

### CLI-04 — An unknown store gets no client · critical · [verified]

- **Steps** — start an authorize flow with a `client_id` that is not a store this pod serves.
- **Expect** — no client at all, so the request is a plain "no such client". The old repository
  answered every `client_id` with a freshly built client, and only the user lookup failing later
  stopped an unknown store — defence by accident. A row in `cua.realms` is what makes a store real
  now, written on the first request the edge vouches for.

### LGN-10 — The form is CSRF-protected, and a stale one is not a dead end · high · [verified]

- **Steps** — read the `Set-Cookie` headers on the hand-off `302` (`SESSION` and `XSRF-TOKEN; Path=/`), then post
  the form once with the `_csrf` hidden input the storefront rendered and once without it.
- **Expect** — with it the flow resumes; without it `302 …/login?auth=1&error=expired` with a fresh `XSRF-TOKEN`,
  and the re-rendered form submits fine. Walked with curl through spg (the hand-off's `Set-Cookie: XSRF-TOKEN=…;
  Path=/`, the storefront page's `name="_csrf"` hidden input carrying the same value, both POSTs); also
  `LoginHandoffIntegrationTest.aFormWithoutTheCsrfTokenIsSentBackAsExpired`.

### LGN-11 — `prompt=login` asks a signed-in session for the password again · critical · [verified]

- **Steps** — sign in as `user`, then register a new shopper on `/en/register` in the same browser.
- **Expect** — the sign-in form appears (not a silent callback), and after entering the new credentials the
  storefront is signed in as the new shopper. Walked with curl through spg: a session that had just signed in
  as `user` and started a second `prompt=login` authorize (new `code_challenge`) was sent to `/en/login?auth=1`,
  not to the callback. Also `LoginHandoffIntegrationTest.aSignedInSessionIsPromptedAgainWhenTheStorefrontAsksForLogin`; without
  `prompt=login` the same session is single sign-on (`withoutPromptLoginASignedInSessionGetsACodeStraightAway`).

### CLI-05 — The session cookie is what carries the saved request across the hand-off · high · [verified]

- **Steps** — start a login and read the `Set-Cookie` on the `302` from `/cua/oauth2/authorize`, then the one on
  the `302` from `POST /cua/login`.
- **Expect** — `SESSION=…; Path=/cua/; HttpOnly` (the path is the `X-Forwarded-Prefix` context path; in the
  integration test, with no prefix, it is `/`). The storefront's form POST is same-origin so the cookie rides
  along; signing in rotates the id, and the browser follows the new one. No `SameSite` attribute is set —
  browsers default to `Lax`, which allows exactly this top-level POST.

---

## SOC — Social login configuration

### SOC-01 — A store's social providers round-trip · high · [not verified]

- **Steps** — configure a provider through `SocialLoginConfigController`, read it back, change it, remove it.
- **Expect** — each answers 2xx and the storefront's login page offers exactly the configured providers.

### SOC-02 — Provider credentials are encrypted at rest · critical · [not verified]

- **Steps** — `select *` the `cua.social_login_config` row and read the secret columns; grep the log for the
  secret you entered.
- **Expect** — ciphertext in the column, nothing in the log. A plaintext credential column is a hard failure.

### SOC-04 — The public provider list is per store and carries no secret · critical · [unit only]

- **Steps** — `http/public-social-login-controller.http`: `GET /cua/api/v1/public/social-logins?store=` for
  `STORE_ID` and `STORE_ID_2`.
- **Expect** — `[{providerId, name, registrationId}]` for that store only; the body never contains `appId`,
  `appSecret` or an `ENC:` envelope. Covered by `PublicSocialLoginControllerIntegrationTest`.

### SOC-03 — One store's providers are not another's · critical · [not verified]

- **Steps** — configure a provider on org1-store1 and read the public login options for org1-store2.
- **Expect** — org1-store2 offers nothing configured for org1-store1.

---

## SID — The composite registration id

_From `qa/unify-store-id-value-objects.md` §REG, reformatted into the case shape used everywhere else. It is
the only cua case that existed before this document._

### SID-01 — Storefront login still parses a store id out of the registration id · [not verified]

_Was R4._ `SocialLoginConfigId` parses a store id out of a composite OAuth2 registration id; that parsing is
unchanged, but it now constructs the merged `StoreMerchantId` type.

- **Steps** — log in at `http://org1-store1.spg-507f1f77.gateway.com` as `user` / `revo`.
- **Expect** — the login completes. Note it only works **through the store host** (a known local constraint,
  not a bug).

---

## 99 — Known gaps

**99-RLM-19 — the dashboard's numbers are pod-wide, not per store.** `DashboardService` counts from raw SQL,
and `@TenantId` does not reach raw SQL. Three of its queries carry no realm predicate:

| Query | Table | Fixable today? |
|---|---|---|
| sign-ins, tokens issued, top clients, recent failures | `audit_events` | **yes** — the table has `realm_id`, the SQL simply does not use it |
| `activeSessions` | `spring_session` | no — Spring Session's table has no realm column |
| client counts | `oauth2_registered_client` | no — the table has no realm column; only `client_extension` does |

On uaa this was never wrong: one realm, so unfiltered and filtered are the same set, which is why it survived
the extraction unnoticed. On cua a realm is a store, so a viewer of any one store's dashboard sees pod-wide
session and token counts, and `recentFailures` returns **audit rows belonging to other stores**. That last one
is the sharp edge — it is row data, not just a count.

Scope the `audit_events` queries by the current realm identifier before this endpoint is shown to anyone who
should not see the whole pod. The other two need a schema change first.

**Only the public endpoints have `http/` blocks** (`public-registration-controller.http`,
`public-social-login-controller.http`). The OAuth2 endpoints and the form-login POST are browser flows, walked
by `LoginHandoffIntegrationTest` rather than a request file.

**cua's only prior coverage in the whole `qa/` tree was one case** (SID-01), reached incidentally by a
store-id refactor. Nothing has ever exercised registration, social login or the dynamic client on purpose.

**`PromptLoginFilter` consumes its marker on the next `prompt=login` authorize, whoever sends it.** A shopper
who is logged out by the filter, abandons the form, signs in through some other path and then starts a new
`prompt=login` flow passes through that one time without being asked again. One-time and same-session only.

**The storefront login only works through the store host.** Documented in
[`references/qa-testing.md`](../../../.claude/skills/project-structure/references/qa-testing.md) §2 and §6 as
an expected local constraint.

**`isOrgAdmin` ignores the store it is handed** — see
[tenancy-qa.md](../../../store-core/tenancy/tenancy-service/qa/tenancy-qa.md) 99. cua is one of the pod
services that gap affects.

**The console's social-login section is under Store management** and belongs to this service; it is described
in [console-ui-qa.md](../../../store-core/console-ui/qa/console-ui-qa.md) §MER only because it shares the page.

---

Raise anything unexpected against the cua PR. Include the store host **with its port**, the registration id,
the time, and the matching lines from `.lcl/<stack>/logs/cua.log`. Never paste a token or a provider secret
into the report.

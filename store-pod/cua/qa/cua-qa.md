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
- **Cases** — 22 (5 verified, 6 unit only, 11 not verified)
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

### LGN-03 — A shopper of one store cannot sign in to another · critical · [not verified]

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

### LGN-06 — The hand-off carries the store host and its port · critical · [not verified]

- **Steps** — on a shifted stack (`lcl start -d --stack xxx`), start a login and read the `Location` of the
  `302` from `/cua/oauth2/authorize`.
- **Expect** — `http://org1-store1.spg-507f1f77.gateway.com:<spg-port>/en/login?auth=1` — the store's host
  **with** the port, the shopper's locale as the path prefix, never `/cua/login`. Same origin rule as CLI-02.

### LGN-07 — A wrong password comes back to the form, and the form still works · high · [verified]

- **Steps** — on the storefront's login page submit `user` / `wrong`, then `user` / `revo`.
- **Expect** — `302 /en/login?auth=1&error=invalid`, the page shows the translated message under the theme's
  styling, and the second submit completes the flow: the saved authorize request survived the failure.
  Covered by `LoginHandoffIntegrationTest`.

### LGN-08 — Social buttons only appear while cua is waiting · high · [verified] (rendering) / [not verified] (provider round-trip)

- **Steps** — open `/en/login?auth=1` after starting a login (buttons present for the store's enabled
  providers), then open `/en/login` directly (no buttons: the page starts the flow instead).
- **Expect** — each button navigates to `/cua/oauth2/authorization/<store>.<provider>` and cua redirects to the
  provider. A provider failure lands on `…/login?auth=1&error=social`. Locally the seeded providers carry demo
  app ids, so the provider itself refuses — that is expected.

### LGN-09 — A stale `/cua/login` link is sent to the storefront · [unit only]

- **Steps** — open `http://org1-store1.spg-507f1f77.gateway.com/cua/login` directly.
- **Expect** — `302 /en/login` (no marker → the storefront starts a login); mid-flow it is
  `302 /en/login?auth=1`. Covered by `LoginHandoffIntegrationTest.theOldLoginPageUrlRedirectsToTheStorefront`.

---

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
  [`qa/lcl-qa.md`](../../../qa/lcl-qa.md) case 09, which records the same observation as still not verified.

### CLI-03 — The `/cua` prefix reaches the service intact · critical · [not verified]

- **Steps** — compare what cua logs as its request path with what catalog logs.
- **Expect** — cua sees `/cua/...` (the edge uses `handle`, not `handle_path`) plus
  `X-Forwarded-Prefix: /cua`; `PathPrefixFilter` accounts for it when building absolute URLs. Catalog, by
  contrast, sees its path with the prefix stripped.

### CLI-04 — An unknown store host gets no client · critical · [not verified]

- **Steps** — send an authorization request with a `Host` no store owns.
- **Expect** — refused. It must **not** fall back to another store's client, and it must not 500.

### LGN-10 — The form is CSRF-protected, and a stale one is not a dead end · high · [unit only]

- **Steps** — read the `Set-Cookie` headers on the hand-off `302` (`SESSION` and `XSRF-TOKEN; Path=/`), then post
  the form once with the `_csrf` hidden input the storefront rendered and once without it.
- **Expect** — with it the flow resumes; without it `302 …/login?auth=1&error=expired` with a fresh `XSRF-TOKEN`,
  and the re-rendered form submits fine. Covered by `LoginHandoffIntegrationTest.aFormWithoutTheCsrfTokenIsSentBackAsExpired`.

### LGN-11 — `prompt=login` asks a signed-in session for the password again · critical · [unit only]

- **Steps** — sign in as `user`, then register a new shopper on `/en/register` in the same browser.
- **Expect** — the sign-in form appears (not a silent callback), and after entering the new credentials the
  storefront is signed in as the new shopper. Covered by
  `LoginHandoffIntegrationTest.aSignedInSessionIsPromptedAgainWhenTheStorefrontAsksForLogin`; without
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

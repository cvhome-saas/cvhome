# Super-admin impersonation — acting as a merchant inside the console

## Context

A platform operator cannot see what a merchant sees. `PermissionAccessChecker` deliberately gives
`ROLE_SUPER_ADMIN` a branch in **billing and pod** checks only — `hasReadAccessOnStore` and
`hasManageAccessOnStore` have none, on purpose (`PermissionAccessChecker.java:159`: "only billing widens;
the merchant screens a platform operator has no business in stay 403"). A support request that says "my
orders page is empty" cannot be reproduced. The console renders the capability as a **disabled menu row**
labelled *"Signing in as another account is not built yet"*
(`store-commons/ui-kit/uaa/src/lib/user-admin-table/user-admin-table.ts:164`).

The feature is already specified: **`.agents/requirments/user-impersonation.md`** is the binding
document, linked from `store-core/console-ui/lessons.md:2834`. This plan implements it, plus three
decisions taken with the user:

1. **Two entry points** — by user (the existing row action) *and* by store ("open this store's
   dashboard" from the platform store/org screens).
2. **`ROLE_SUPPORT` becomes real.** It exists only in the frontend (`ui-kit/.../auth/roles.ts`) and in
   the design mockups (`store-core/uaa/sso/SSO Roles.dc.html:249` — a `support` role holding
   `users:impersonate`); the backend `Roles` enum lacks it.
3. **Read-only vs read-write is a per-session choice** — see *The RO/RW model*.

Outcome: an operator picks a merchant account and a store, states a reason, picks a mode, and lands on
that merchant's dashboard with the merchant's rail, data and permissions — under a non-dismissible
banner, with a 15-minute ceiling, every action audited against the real operator.

**Assumption to confirm at review:** `ROLE_SUPPORT` impersonates **read-only only**; `ROLE_SUPER_ADMIN`
picks either mode. One predicate in the provider to relax.

---

## Why the design is what it is

**The console never holds a token.** The gateway is the OAuth2 client (`gateway/config/SecurityConfig.java`);
the browser holds only `STORE-CORE-GATEWAY-JSESSIONID`, and `GatewayRouteLocatorImpl` puts `.tokenRelay()`
on every backend route. "Act as" is therefore **a server-side swap inside the gateway session**.

**`sub` becomes the merchant, `act` names the operator.** Every `@PreAuthorize`, `hasPermission(...)`,
`SecurityUtils.getOrgStoreIdentity` and all 27 permission tokens keep working unchanged, because the
token carries the merchant's `org` / `store` / `roles` exactly as `JwtCustomizerConfig` emits them today.
**No new permission token, no new checker method.**

## The RO/RW model

Read-only lives in the **roles claim**, not in an HTTP-method filter — `POST store-manager/list` and
`POST org-manager/list` are reads, and `STORE-POD.CATALOG.*` guards GETs too, so neither method nor
token separates reading from writing. `ROLE_STORE_MODERATOR` already *is* the read-only store role:
`hasReadAccessOnStore` accepts it, `hasManageAccessOnStore` / `hasMaintainAccessOnUsers` refuse it, and
it sits in `StoreManagerApi.STORE_VIEWER_ROLES` so the store list still answers (scoped by
`InternalStoreService.findAll` to that one store — exactly right).

| Mode | `roles` minted | `org` / `store` | `permissions` |
|---|---|---|---|
| `write` | the target's own, verbatim — never wider | the target's metadata | the target's |
| `read` | `["STORE_MODERATOR"]` | target's `org`; the **chosen store** | `STORE_MODERATOR`'s effective set |

Both modes carry `act` and `act_mode`. `act_mode` feeds the audit trail and banner copy; **the roles
claim is the enforcement.** Read mode is refused for a target holding none of
`ORG_ADMIN` / `STORE_ADMIN` / `STORE_MODERATOR` — otherwise "read" would *widen* a `STORE_RETAIL` account.

Read mode is store-scoped, so an impersonation always resolves to a **concrete store** — which is what
makes the store-centric entry point natural.

---

## Phase 1 — uaa: the token-exchange grant  (PR 1)

Paths under `store-commons/sso/sso-core/` unless noted. Registration is uaa-shell-only on purpose:
**the grant must never reach cua**.

**Vocabulary**
- `commons/domain/Roles.java` → `ROLE_SUPPORT`. `UaaJwtGrantedAuthoritiesConverter` maps any `roles`
  entry to `ROLE_<name>`, so no converter change; `getOrgStoreIdentity`'s `else` branch gives support an
  empty identity, which is correct.
- `commons/domain/Permission.java` → `USERS_IMPERSONATE("users:impersonate", IDENTITY)`; `RoleService`
  validates grants against this enum.
- `sso/service/OAuthGrantType.java` → `TOKEN_EXCHANGE("urn:ietf:params:oauth:grant-type:token-exchange")`;
  `from(String)` throws on anything outside the enum.

**The client** — `console-impersonation`, confidential, `client_secret_basic`, grant `token-exchange`
only, scope `openid`, access TTL 900s, **no refresh_token grant**. Not `web-app`: that is the browser's
PKCE client and the exchange must be callable only by the gateway.
- `store-core/uaa/src/main/resources/init-sql/data-common.sql` — seed the client; seed a `SUPPORT` role
  (`users:read`, `users:impersonate`, `audit:read`, per the mockup); grant `users:impersonate` to
  `SUPER_ADMIN`.
- `store-core/uaa/.../application.yml` — `oauth2.clients.console-impersonation.secret:
  ${UAA_IMPERSONATION_SECRET}` so `OAuth2ClientDatabaseInitializer` rewrites it under `lcl`.
- `data-test-stores.sql` — a `support` demo account for QA.

**The grant** — new `sso/token/ImpersonationExchange{Converter,Provider,AuthenticationToken}.java`,
registered by a new `store-core/uaa/.../config/ImpersonationGrantCustomizer.java` implementing the
existing seam `AuthorizationServerHttpCustomizer` (applied at `AuthorizationServerConfig:84`):
`tokenEndpoint(t -> t.accessTokenRequestConverter(ours).authenticationProvider(ours))`.
`OAuth2TokenEndpointConfigurer` **prepends** custom converters to its defaults, so ours is consulted
before Spring's built-in `OAuth2TokenExchangeAuthenticationConverter`, and the built-in provider never
`supports()` our token class. Lock that in with a unit test — if a Spring upgrade reorders the list, the
built-in converter would claim the grant and reject it as malformed.

Request shape (`requested_subject` is Keycloak's name for the same extension):

```
POST /oauth2/token                     Authorization: Basic <console-impersonation>
grant_type           = urn:ietf:params:oauth:grant-type:token-exchange
subject_token        = <the operator's access token>
subject_token_type   = urn:ietf:params:oauth:token-type:access_token
requested_token_type = urn:ietf:params:oauth:token-type:access_token
requested_subject    = <target uaa user id>
impersonation_store  = <StoreMerchantId>
impersonation_mode   = read | write
reason               = <free text, required>
```

Provider, in order — every refusal is an `OAuth2AuthenticationException` **and** a
`user.impersonation.denied` audit row naming which rule fired:

1. Resolve `subject_token` via the JDBC `OAuth2AuthorizationService`: live, unexpired, issued here.
   Resolve the operator by the `uid` claim, as `sso/security/CurrentUserResolver.java` does.
2. **No chaining** — subject token already carries `act`.
3. **Operator** lacks `users:impersonate`.
4. **Target** holds `SUPER_ADMIN` or `SUPPORT` (`UaaConstants.SUPER_ADMIN_ROLE`;
   `AdminService.getNonSuperAdmin` is the precedent).
5. **`write` for a `SUPPORT` operator.**
6. **Store the target cannot act in** — must equal the target's `store` metadata, or belong to the
   target's `org` for an org admin.
7. **`read` for a target with no store-level read role** (the widening rule above).
8. Build the `OAuth2Authorization` under `AuthorizationGrantType.TOKEN_EXCHANGE`, principal =
   `User.withUsername(target.getId().toString())` — mirroring `JpaUserDetailsService`, because
   `JwtCustomizerConfig.addUserClaims` resolves by `principals.account(principal.getName())`. Stash
   `ImpersonationContext(operator, target, store, mode, reason)` as an authorization attribute.
9. Mint **an access token only.** TTL = `min(900s, subject token's remaining life, realm ceiling)`;
   `clampLifetime` already applies the ceiling.

**Claims** — `sso/config/JwtCustomizerConfig.java`. The impersonation branch reads the context off
`context.getAuthorization()` and **runs after `addUserClaims`, as the one thing allowed to follow
`roles`** — that method writes `roles` last so nothing can shadow it; the comment there moves to say
the impersonation branch is the deliberate exception, and a test pins the order.

```
act      = {"sub": "<operator username>", "uid": "<operator id>"}   // RFC 8693 §4.1, identity only
act_mode = "read" | "write"
roles / store / permissions   overridden per the table above (read mode only)
```

**Tests** — `sso-core/src/test`: the seven refusals, the claim shape, the converter order.
`store-core/uaa/src/integrationTest`: a real exchange against `/oauth2/token`.
**`.http`** — `store-core/uaa/http/impersonation-api.http` at `{{SELLER_UI_URL}}/uaa/oauth2/token`.

---

## Phase 2 — gateway: the session swap  (PR 2)

`store-core/gateway/gateway-service/src/main/java/com/asrevo/cvhome/gateway/`.

- **`controller/ImpersonationController.java`** (new)
  - `POST /api/v1/impersonation` `{userId, storeId, mode, reason}` → `200 {actingAs, storeId, mode, expiresAt}`
  - `DELETE /api/v1/impersonation` → `204`
  - `@PreAuthorize("isAuthenticated()")`; the real gate is uaa's refusal list — the only place that sees
    both principals. `reason` is `@NotBlank`. Platform-scoped like `AuthController` beside it, so no
    `StoreMerchantId`/`LanguageCode`. Same CSRF posture as every other gateway POST.
- **`config/ImpersonationService.java`** (new)
  1. Ask the `ReactiveOAuth2AuthorizedClientManager` for the operator's client **first** — that refreshes a
     near-expiry token. Without this the exchange inherits whatever seconds the 15-minute `web-app` token
     has left, and an impersonation could be born with a minute to live.
  2. Exchange with a second `spring.security.oauth2.client.registration` (`console-impersonation`) in
     the gateway's `application.yml`, beside `s2s`.
  3. Stash the original `SecurityContext` **and** `OAuth2AuthorizedClient` under one `WebSession`
     attribute together with `expiresAt` (= the exchanged token's `exp`), the target, mode and reason.
  4. Save the exchanged token as the authorized client for registration `uaa`, so `tokenRelay()` attaches it.
  5. **Replace the session's `Authentication`** with an `OAuth2AuthenticationToken` whose authorities come
     from `SecurityConfig.extractAuthority` over the exchanged token. Otherwise the console keeps the
     operator's rail while its API calls act as the merchant — a mismatch no banner fixes.
  6. `DELETE` and expiry both **restore** the stashed pair and call uaa's `/oauth2/revoke` on the exchanged
     token (best-effort on expiry) — that call is what writes the audit "ended" row (Phase 3).
- **`config/ImpersonationExpiryFilter.java`** (new `WebFilter`, before the security chain) — restores the
  moment `expiresAt` passes; **never** falls through to the merchant. Its own clock, checked every
  request. Also needed because the exchanged client has no refresh token: past `exp`, `tokenRelay()`
  would forward a dead token and every call would 401.
- **`controller/LogoutController.java`** — logout during an impersonation restores the original context
  **first**. `UaaLogoutSuccessHandler` builds the end-session URL from the OIDC principal's ID token;
  the swapped `Authentication` has none, so an un-restored logout would end the gateway session and
  leave uaa's alive — the exact bug `authentication.md` warns about.
- **`controller/AuthController.java`** — `me()` returns an explicit `MeView` record instead of
  Jackson-serialising `OAuth2AuthenticationToken`: `principal {claims{sub}, preferredUsername, name,
  givenName, familyName, email}`, `authorities[]`, and `impersonation {actingAs, storeId, mode,
  expiresAt, reason} | null`. The first two mirror what `ui-kit`'s `AuthService` reads today, and
  stating them explicitly is what lets the swapped principal (a `DefaultOAuth2User`, whose attributes
  do not serialise as `claims`) answer the same shape.

Sessions are in-JVM (`config/GatewaySessionMetrics.java`), which satisfies "survives nothing" — a
gateway restart ends every impersonation by design.

**Tests** — `src/test`: expiry filter, controller, the logout-restore path. Extend
`src/integrationTest/.../AuthApiIntegrationTest.java` for the `impersonation` block.
**`.http`** — `store-core/gateway/gateway-service/http/impersonation-api.http`.

---

## Phase 3 — audit  (PR 3)

**uaa owns the rows** — it alone sees both principals, and `sso/audit/` already gives actor, target,
ip, user-agent, trace-id, CSV and retention.

- `audit/AuditEventType.java` → `USER_IMPERSONATION_STARTED / _ENDED / _DENIED`
  (`user.impersonation.*`, `SECURITY`). No new `AuditTargetType`, so `schema-template.sql`'s check
  constraints — shared with cua — stay untouched.
- Started/denied: written by the provider; actor = operator, target = `USER` + merchant, `detail` =
  reason, `reasonCode` = the mode, or the refusal that fired.
- Ended: `sso/security/ProtocolAuditListener.java` already writes `TOKEN_REVOKED` on `/oauth2/revoke`;
  extend it to emit `USER_IMPERSONATION_ENDED` when the revoked authorization's grant is token-exchange.
  Both gateway end paths revoke, so a silent expiry leaves a row too. (`TokenRevocationService`'s caveat
  stands: a self-contained token outlives revocation until `exp` — the session swap and the TTL are the
  real end; revoke is the audit trigger.)

**Rows written *during* an impersonation must name the real actor** — services read
`authentication.getName()`, now the merchant.
- `SecurityUtils.actorOf(Authentication)` in `store-commons/autoconfigure/.../s2s/utils/` →
  `"merchant"` or `"merchant (via operator)"` when `act` is present; fits the `varchar(100)` actor column.
- Use it in tenancy's `TenancyAuditService.record(...)` and `OrgManagerApi.actorOf`, and in the pods'
  `PodAuditEntity.of(...)`.

**`ROLE_SUPPORT`'s way in.** Support holds `users:impersonate` and nothing else, so it cannot reach the
screens it needs to *find* a target. Widen exactly these reads: `OrgManagerApi` `list` / `find-one` /
`stores` → `hasAnyRole('ROLE_SUPER_ADMIN','ROLE_SUPPORT')`; uaa's `AdminUserController` list/read via a
method-level `@PreAuthorize` (the class-wide guard is `SCOPE_super_admin or ROLE_SUPER_ADMIN`). Every
write stays super-admin. uaa's own SPA guard `canAdministerRealm` is **not** widened.

---

## Phase 4 — console  (PR 4)

Not worth starting before Phase 3 exists.

**Context reset is a full reload** — `location.assign('/dashboard')` on start, `/platform/users` on
end. Identity, the store list, `SelectedStoreService` and ~20 facades keyed on
`shell.currentStoreId()` all change at once; the app has no global-invalidation precedent and a reload
is honest. This also means **`ConsoleShellFacade.isPlatformOperator` can stay a construction-time
boolean and `AuthService` needs no cache invalidation** — both are re-read on load.

**ui-kit** (`store-commons/ui-kit/`, shared with uaa's console):
- `uaa/src/lib/user-admin-table/user-admin-table.ts` — `impersonate` is enabled when the host allows it
  and emits an intent; keeps the disabled-with-reason form otherwise. Update `user-admin-table.spec.ts:67`.
- `src/lib/auth/auth.service.ts` — `AuthenticationResponse` / `AuthUser` gain `impersonation`.
- `i18n/.../{en,ar}.json` — `shared.userAdmin.action.impersonate` replaces `…impersonateUnavailable`,
  in **both** locales.

**console-ui** (`store-core/console-ui/`):
- `src/app/api/tenancy/impersonation.service.ts` + `HttpTestingController` spec.
- `layouts/console-shell/components/impersonation-banner/` — the shape of `plan-banner/`,
  **non-dismissible**, naming merchant, store, mode and time remaining, carrying the only control that
  ends it. Slot above `<app-plan-banner>` in `console-shell.ts`; `--banner-h` in `console-shell.css`
  becomes the **sum** of banners shown (flat `49px` today; `.plan-banner` hardcodes its own height).
- A start dialog: **reason (required)**, **mode (read-only default; write hidden for support)**, store.
- **Entry A — by user:** `features/platform-users/` and `organization-detail/`'s Users tab. Store
  choices: the target's `store`, or the org's stores for an org admin (`PlatformUserRow` carries both).
- **Entry B — by store:** "Open dashboard" on the platform store list and the org detail Stores tab.
  Candidates = accounts with `metadata[store]=id` ∪ the org's admins (`metadata[org]`), via the existing
  uaa admin list filter (`AdminUserController.extractMetadataFilters`). Posts the chosen `userId` —
  **the gateway contract stays one endpoint**; B is a picker.
- `shared/auth/console-permissions.ts` — `canImpersonate()`, and the `isSupport` branch its comment at
  line 50 reserves. `platformOnly` / `merchantOnly` need no change: post-reload `authorities` are the
  exchanged token's, so merchant routes open and platform routes bounce, which is right.

---

## Files to touch — the short list

| Area | Path |
|---|---|
| Grant | `store-commons/sso/sso-core/.../sso/token/ImpersonationExchange*.java` (new), `sso/service/OAuthGrantType.java` |
| Claims | `store-commons/sso/sso-core/.../sso/config/JwtCustomizerConfig.java` |
| Registration | `store-core/uaa/.../config/ImpersonationGrantCustomizer.java` (new) |
| Vocabulary | `store-commons/commons/.../domain/{Roles,Permission}.java` |
| Seeds | `store-core/uaa/src/main/resources/init-sql/{data-common,data-test-stores}.sql`, `application.yml` |
| Audit | `sso/audit/AuditEventType.java`, `sso/security/ProtocolAuditListener.java` |
| Actor | `autoconfigure/.../s2s/utils/SecurityUtils.java`, tenancy `TenancyAuditService`, `OrgManagerApi`, pods' `PodAuditEntity` |
| Gateway | `controller/{ImpersonationController,AuthController,LogoutController}.java`, `config/{ImpersonationService,ImpersonationExpiryFilter}.java`, `application.yml` |
| ui-kit | `uaa/.../user-admin-table.ts`, `src/lib/auth/auth.service.ts`, `i18n/.../{en,ar}.json` |
| console | `layouts/console-shell/**`, `features/{platform-users,organization-detail,organizations}/**`, `api/tenancy/impersonation.service.ts`, `shared/auth/console-permissions.ts`, `src/locale/{en,ar}.json` |

No new Gradle module, no new service, no new permission token, no DDL change.

---

## Verification

**Worktree first.** `git fetch origin && git worktree add .claude/worktrees/feat-impersonation -b
feat/impersonation origin/main`; everything inside it, own stack `lcl start -d --stack impersonation`,
ports from `lcl urls`.

**Gates:**
```bash
./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest
./gradlew build -x test -x check
./gradlew test
./gradlew integrationTest                                   # Docker — security + SQL touched
cd store-commons/ui-kit && npm run build
cd store-core/console-ui && npm run build && npm run lint   # lint carries the token / logical-property / i18n checks
```

**End to end, through the gateway, in the browser:**
1. `super-admin` → `/platform/users` → impersonate `org1-store1-admin`, **read-only**, with a reason.
   Banner names merchant + store + mode; merchant rail; orders/catalogue readable; every save 403s.
2. Same in **read-write**: the save succeeds; `uaa.audit_events` has the `started` row with the reason;
   `tenancy.tenancy_audit.actor` on the change reads `org1-store1-admin (via super-admin)`.
3. **Tenant isolation:** while impersonating, `?store=<ORG2-STORE1>` → 403, not an empty 200.
4. **The gate, each leaving a `denied` row:** `org1-admin` posts → refused; `support` asks `write` →
   refused; target `super-admin` → refused; subject token already carrying `act` → refused; read mode
   on a retail-only target → refused.
5. **Expiry:** past `expiresAt` the next request is the operator, never the merchant; banner gone after
   reload; an `ended` row exists.
6. **End:** banner control → `DELETE` → `/platform/users` as the operator, store list reloaded, `ended` row.
7. **Logout mid-impersonation** ends both sessions — the next navigation is the sign-in page, not the
   operator's dashboard.
8. Entry B: org detail → Stores → "Open dashboard" → same banner, store preselected.
9. **i18n / RTL** in `en` and `ar`; console clean; 1440 / 900 / 420; Forest / Midnight / Daylight.

**QA documents** — append to the per-service files, never a new one: `store-core/uaa/qa/uaa-qa.md`
(grant + refusals), `store-core/gateway/gateway-service/qa/gateway-qa.md` (swap, TTL, logout, `auth/me`),
`store-core/console-ui/qa/console-ui-qa.md` (banner, both entries — and **fix the case at line 1211**
asserting the disabled placeholder), `store-core/tenancy/tenancy-service/qa/tenancy-qa.md` (the `via`
actor). Tag `[verified]` / `[unit only]` / `[not verified]` honestly.

**Docs** — rewrite `console-ui/lessons.md`'s `## Platform — no impersonation` heading rather than delete
it: `scripts/check-lessons-citations.mjs` fails on a citation to a vanished heading, and
`console-permissions.ts:52` cites it. Update `.agents/requirments/user-impersonation.md` §2 ("What
exists today: Nothing") once shipped.

# uaa — from a basic auth server to the platform's SSO

One PR, ten phases, each
built + tested + QA'd against a running stack before the next, `store-core/uaa/qa/uaa-qa.md` updated per phase.

## Context

`store-core/uaa` is the identity server for the whole SaaS: it authenticates every operator through the gateway,
mints every service-to-service token, and owns users, roles and OAuth2 clients. It is also the least developed
service in the repo: 44 classes, one unit test, no lockout, no rate limiting, no password policy, no audit, no
session control, no key rotation, no brokered login. The SSO design in `store-core/uaa/sso/` (nine screens) is
the product vision; `uaa-fe/lessons.md` records the fifteen places the console had to back away from it because
nothing existed underneath.

The audit of the code (this session) found real defects on top of the missing features:

| # | Finding | Where |
|---|---|---|
| S1 | Every actuator endpoint is public and everything is exposed (heapdump, env, loggers, sessions) | `AppSecurityConfig:29-30` + `common-config.yml:229-231` |
| S2 | Super-admin password is reset to `admin` on every boot, every profile | `application.yml:24`, `AdminUserDatabaseInitializer` |
| S3 | All four client secrets are one plaintext literal, re-applied on every boot; `admin-sdk` carries `super_admin` | `application.yml:29-35`, `common-config.yml:173` |
| S4 | `resetPassword` bypasses the super-admin guard, so any `super_admin` token can take over the super admin | `AdminService:143-149` |
| S5 | CSRF disabled on the app chain (SAS ignores its own endpoints anyway) | `AppSecurityConfig:45` |
| S6 | No lockout, no rate limit, no password policy, no audit | whole module |
| S7 | 24 h access, refresh **and authorization code** TTLs; PKCE off on the browser client | `data-common.sql:30-31,75-76` |
| S8 | Signing keys stored plaintext (`jwk_json oid`), never rotated, parse errors swallowed | `SigningKey`, `KeyPairService` |
| S9 | Issuer not pinned; derived from `Host`/`X-Forwarded-Host` (cua refuses to boot in that state) | `AuthorizationServerConfig:49` |
| S10 | User metadata copied verbatim into JWT claims **after** `roles`; a metadata key `roles`/`scope`/`aud` overrides the real claim | `JwtCustomizerConfig:74` |
| S11 | `PUT /clients/{id}` ignores the path id and writes whichever id is in the body | `AdminClientController:49-52` |
| S12 | `/api/v1/auth/me` returns the raw principal (a full `Jwt` for bearer callers) | `AuthController:14-17` |
| S13 | Users can exist with a null password hash and `enabled=true`; login then 500s | `AdminService.createUser`, `JpaUserDetailsService:38` |
| S14 | Super-admin guard keyed on the literal `super-admin@mail.com`; roles have no guard at all | `AdminService:176`, `RoleService` |
| S15 | Role grant silently skips unknown names and answers 200 | `AdminService:101-103` |
| S16 | **No `OAuth2AuthorizationService` bean** → SAS uses its in-memory one: refresh tokens die on restart and nothing can be revoked; the `oauth2_authorization` DDL uses `bytea` where SAS 7 on Postgres needs `text`, and lacks the `user_code_*`/`device_code_*` columns | `AuthorizationServerConfig`, `schema.sql:20-53` |
| S17 | Dead code and drift: `UserInfoController` unreachable, `consent.html` unregistered, `/api/v1/me` permitAll for nothing, `http/admin-user-api.http` calls paths/fields that do not exist, `req.http` holds plaintext credentials | — |

Verified versions: Spring Boot 4.0.1, Spring Security 7.0.2 (SAS merged into it), Spring Session JDBC 4.0.1,
caffeine 3.2.3 in the catalogue, `secret-crypto-autoconfigure` already used by cua. Nothing for TOTP/WebAuthn/
rate limiting/CSV exists in the catalogue and none is added: the design below uses Spring Security built-ins,
caffeine, and small in-repo code.

**Scope decisions (confirmed with the user):**

- Identity providers: **OIDC + OAuth2 brokering** (Google, Microsoft, Apple, GitHub, generic OIDC by discovery,
  generic OAuth2 by manual endpoints). **No SAML** — the UI shows it as not built.
- **No MFA in this PR.**
- **No mail sender.** Invitations and admin-issued password resets produce a hashed one-time token; the link is
  returned once to the caller **and** handed to a `LinkDeliveryPort` whose default implementation logs it. A later
  service/lib (SMS, WhatsApp, other APIs) implements the port.
- **Rich roles**: description, scope, system flag, inherits-from, a permissions catalogue; effective permissions
  emitted as a `permissions` claim beside `roles`. Services keep authorising on roles.
- **Single realm.** No realm switcher. "Settings" is the realm-wide policy of the one realm.
- **Not production yet: drop and recreate.** `schema.sql` is rewritten as a clean `create table` set (no
  `alter … add column if not exists`, no legacy columns, no data migrations, no seed-patching `update`s). A
  developer resets with `drop schema uaa cascade` (or `lcl stop --hard`) and restarts. The QA `MIG` section says
  exactly that and nothing else.
- **Shared UI goes into `@cvhome-saas/ui-kit`** (we own it): any component built for these screens that is not
  uaa-specific — segmented control, chip matrix, pair list (moved out of uaa-fe), reorderable list, KPI delta tile,
  bar chart, fact grid, diff table, code panel, OTP-style digit boxes are not needed — lands in `ui-kit/ui` with a
  spec and is consumed by uaa-fe (and console-ui when it wants it).
- **Events go through the namastack outbox** (`references/events-outbox.md`): a new `store-core/uaa/uaa-events`
  library module (the `tenancy-events` shape) holds the contracts; uaa publishes from aggregate roots and
  consumes with idempotent `@OutboxHandler`s. Used for link delivery (invitation / reset link issued) and user
  lifecycle (created / disabled / deleted) so the future SMS/WhatsApp service and tenancy's membership
  reconciliation subscribe instead of being called.

## Ground rules for the implementation

- Worktree first: `git fetch origin && git worktree add .claude/worktrees/feat-uaa-sso -b feat/uaa-sso origin/main`;
  stack `lcl start -d --stack uaa-sso` from inside it; ports from `lcl urls --stack uaa-sso`.
- Backend stays in `store-core/uaa` (BE+FE shape); new packages `audit/`, `settings/`, `security/`, `password/`,
  `ratelimit/`, `session/`, `token/`, `invitation/`, `client/`, `keys/`, `idp/`, `web/account/`, `web/pub/`.
- Errors: condition-named classes, constants in `UaaErrors` (`store-commons/uaa-client`), copy the shape of
  `SuperAdminImmutableException`; bodies only via `ProblemDetailFactory`. `ErrorCategory` gains
  `TOO_MANY_REQUESTS(429)` (additive, `store-commons/errors`).
- DDL in `init-sql/schema.sql` first, as clean `create table if not exists` statements (enums as `varchar` +
  `CHECK`); seed changes in `data-common.sql`. Outbox tables come from namastack's own DDL in the `uaa` schema. Secrets (IdP client secrets, private JWKs) encrypted
  with `SecretCryptoProvider` in the mapper layer (copy cua's `SocialLoginConfigMapper`); add
  `:store-commons:secret-crypto:secret-crypto-autoconfigure` to uaa's `build.gradle`.
- Every new/changed endpoint gets a block in `store-core/uaa/http/<api-class>.http` addressed
  `{{SELLER_UI_URL}}/uaa/...` through the gateway; each file ends with the 403-as-org-admin case; public
  endpoints get their own `public-api.http`.
- Tests: `src/test` `*Test` (no Spring); `src/integrationTest` `*IntegrationTest` on **`@DatabaseIntegrationTest`**
  (test-support's javadoc: authorization servers must not use `@ServiceIntegrationTest`, whose
  `ServletTestSecurityConfiguration` would replace uaa's `JwtDecoder`). Tokens come from uaa itself
  (`client_credentials` on `admin-sdk`; a form-login session via `java.net.http.HttpClient` + `CookieManager`, the
  shape of cua's `LoginHandoffIntegrationTest`). `MutableClock`/`TestClockConfiguration` work once uaa has a
  `Clock` bean (copy `store-pod/content/.../ClockConfig.java`).
- Frontend: uaa-fe on `@cvhome-saas/ui-kit` (`data-table`, `kpi-card`/`kpi-grid`, `section-nav`, `form-dialog`,
  `toggle`, `duration-field`, `secret-field`, `copy-field`, `search-box`, `tab-switcher`, `notice-bar`,
  `pagination`, `tag-input`, `select`, `stepper`, `ranked-list`, `progress-track`). New uaa API services go in
  `store-commons/ui-kit/uaa` (the `AdminUserService` pattern) so console-ui can reuse them; screens stay in
  uaa-fe. i18n keys in **both** `en.json` and `ar.json`; RTL checked. Angular `withXsrfConfiguration` is **not**
  configured anywhere today — phase 1 adds it in ui-kit's `provideUiKit`.
- `lessons.md` in uaa-fe is append-only: every entry closed by this PR gets a "Closed by feat/uaa-sso" line; `npm
  run lint` must still resolve every citation.
- Keep `store-core/uaa/sso/` untracked (design mocks are deliberately not committed).

## Phase map

| Phase | Theme | Depends on |
|---|---|---|
| 0 | Worktree, baseline tests, drift clean-up | — |
| 1 | Hardening + foundations: JDBC token store, issuer, CSRF, actuator, claims, guards, TTL/PKCE seeds | 0 |
| 2 | Settings singleton, rich roles + permissions claim, user columns, audit core | 1 |
| 3 | Lockout, password policy, rate limiting, sessions + token revocation, self-service account | 2 |
| 4 | User lifecycle: search/status/counts, invitations, reset links, `LinkDeliveryPort`, verification | 3 |
| 5 | Clients: enable/disable, type, secret expiry, rotation with grace, wider summary | 2 |
| 6 | Signing keys: encrypted at rest, rotate now, scheduled rotation, retiring window | 2 |
| 7 | Identity providers (OIDC/OAuth2) + identity-first sign-in page | 3, 4 |
| 8 | Protocol audit hooks, audit query API + CSV, dashboard, rail badges | 2 (5, 6, 7 for full tiles) |
| 9 | Closure: `uaa-client` contract, lessons.md, docs, full regression, PR | all |

5, 6 and 7 are independent of each other after 3/4.

---

## Phase 0 — worktree, baseline, drift clean-up

- Cut the worktree, start the stack, run the three `http/` files and record what fails (the four drifts in
  `http/admin-user-api.http`: `username-exists` vs `/exists`, `POST` vs `PUT reset-password`, `password`/`enabled`
  fields that do not exist, `ROLE_` prefixed names). Fix the file to match the controller.
- Delete `web/oidc/UserInfoController`, `templates/consent.html`, the `permitAll` for `/api/v1/me` and
  `/v3/api-docs/**` (springdoc is at `/api-docs`; permit it only under `lcl`), and `store-core/uaa/req.http`.
- Add `UaaArchitectureTest` (ArchUnit, copy `PodRegistryArchitectureTest`), `config/ClockConfig` (`Clock` bean +
  `@EnableScheduling`), and `AdminUserApiIntegrationTest` on `@DatabaseIntegrationTest`: list users 200 with an
  `admin-sdk` token, 403 with a `store_core` token, 401 anonymous (turns ADM-02/03 to [verified]).
- QA file: add `## REG — Regression watchlist` and `## MIG` (billing's structure); renumber the `ACC` section's
  `PERM-02/U-10/U-11/P-02/P-03` to `ACC-01…05` with a "renumbered because" note.

## Phase 1 — hardening and foundations

**DDL (`schema.sql`)** — make SAS's token store real (S16): rewrite `uaa.oauth2_authorization` to SAS 7's
Postgres shape (every `blob` column as `text`, `timestamptz`, plus the `user_code_*` and `device_code_*` columns,
indexes on `principal_name` and `registered_client_id`). Drop-and-recreate; no `alter`.

**Seeds (`data-common.sql`)** — S7: `web-app` `require-proof-key: true`, access 900 s, refresh 43200 s, auth code
300 s; s2s clients access 900 s, auth code 300 s, drop `refresh_token` from `admin-sdk`.

**Outbox wiring:** `store-core/uaa/uaa-events` module (`settings.gradle`, `namastack-outbox-api`), uaa gets the
`namastack-outbox` starter like tenancy; `User`, `Role`, `IdentityProvider` extend `AbstractAggregateRoot` where
they publish. Event contracts (records): `UserCreatedEvent`, `UserDisabledEvent`, `UserDeletedEvent`,
`InvitationIssuedEvent`, `PasswordResetLinkIssuedEvent`, `ClientSecretRotatedEvent`, `SigningKeyRotatedEvent`,
each `@OutboxEvent(key = <aggregate id>)`.

**Config** — smallest blast radius:
- Issuer (S9): `AuthorizationServerSettings.issuer(UrlNormalize.normalizeUri(schema://domain:port))` from
  `ServiceDomainProperties.getService("uaa")`; `IllegalStateException` if missing (cua's pattern). No
  `common-config.yml` change: `issuers.uaa.uris` already lists both forms and `IssuerRegistry` matches normalized.
  Do **not** uncomment `provider.uaa.issuer-uri` (couples gateway start order).
- Actuator (S1): keep the global `exposure: '*'`; override in uaa's `application.yml` to
  `health,info,prometheus,metrics`; chain: `EndpointRequest.to("health","info","prometheus")` permitAll,
  `toAnyEndpoint()` → `hasAnyAuthority('SCOPE_store_core','ROLE_SUPER_ADMIN')`. lcl's health check stays open.
- Secrets (S2/S3): `com.asrevo.cvhome.admin.password: ${UAA_ADMIN_PASSWORD:admin}` and every client secret
  `${UAA_<CLIENT>_SECRET:<lcl default>}` with defaults only in `application-lcl.yml`; `AdminUserDatabaseInitializer`
  and `OAuth2ClientDatabaseInitializer` run only when `com.asrevo.cvhome.uaa.seed.apply-on-boot=true` (true in
  `lcl`, absent → false). `application-fargate.yml` documents the required env vars.
- `@Bean OAuth2AuthorizationService` → `JdbcOAuth2AuthorizationService`; `@Bean OAuth2AuthorizationConsentService`
  → `JdbcOAuth2AuthorizationConsentService`. Principal stays `UsernamePasswordAuthenticationToken` over
  `org.springframework.security.core.userdetails.User` (SAS's Jackson allow-list) — this constrains phase 7.
- CSRF (S5) on the app chain: `CookieCsrfTokenRepository.withHttpOnlyFalse()` + `CsrfTokenRequestAttributeHandler`
  + `security/CsrfCookieFilter` (touches the token so `XSRF-TOKEN` is written on every response, index.html
  included), an access-denied handler that re-plants the cookie and redirects `/login?error=expired`. SAS already
  ignores its own endpoints. A `/api/v1/public/**` chain (`@Order(1)`, stateless, CSRF off, 401 entry point) is
  added now for phases 4 and 7. `.logout()` accepting GET or POST on `/logout` → `/login?logout` (the SPA's Sign
  out navigates; POST-only is a follow-up). `/api/**` anonymous → 401 problem, not a login redirect
  (`defaultAuthenticationEntryPointFor`).
- **Frontend in the same phase or login breaks:** `sign-in.html` adds `<input type="hidden" name="_csrf">` from the
  `XSRF-TOKEN` cookie; ui-kit's `provideUiKit` adds `withXsrfConfiguration({cookieName:'XSRF-TOKEN',
  headerName:'X-XSRF-TOKEN'})`; the shell's Sign out points at `/logout`.
- Claims (S10): allow-list `org`, `store`; add `uid`; `roles` written last; custom client settings only under a
  `cvhome.` prefix plus the explicit `resource` (pods read it). ID tokens get `email`, `email_verified`,
  `given_name`, `family_name`, `name`, `uid`.
- `AuthController.me` (S12) → `MeResponse(uid, username, email, firstName, lastName, emailVerified, roles,
  permissions, authorities[{authority}], authenticatedVia SESSION|JWT)`; a client-credentials caller → 403
  `UAA.AUTH.NOT_A_USER_PRINCIPAL`. ui-kit's `AuthService` keeps working (it reads `username` + `authorities`).
- Guards: `resetPassword` → `getNonSuperAdmin` (S4); super admin identified by `UaaConstants.SUPER_ADMIN_ID`
  (S14); unknown role on grant → `RoleNotFoundException`, `SUPER_ADMIN` → `RoleNotAssignableException` (S15);
  `AdminClientController.update(id, req)` → `adminClientService.update(id, req)` (S11); `RoleService.findBy` →
  `RoleNotFoundException`; `UpdateUserRequest.metadata` null value = remove key (documented; SDK untouched);
  `JpaUserDetailsService` maps a null hash to a never-matching placeholder hash (S13, status PENDING in phase 2).
- `KeyPairService`: log parse failures (S8 finished in phase 6).

**Errors:** `ROLE_NOT_FOUND` (NOT_FOUND), `ROLE_NOT_ASSIGNABLE` (FORBIDDEN), `NOT_A_USER_PRINCIPAL` (FORBIDDEN).

**Tests:** unit `JwtCustomizerConfigTest`, `AdminServiceTest`, `AdminClientServiceTest`, `MeResponseMapperTest`;
integration `IssuerPinningIntegrationTest` (discovery `issuer` == pinned value on a `127.0.0.1` request),
`CsrfLoginIntegrationTest` (no `_csrf` → `/login?error=expired`; admin POST without header 403, with 200),
`JdbcAuthorizationPersistenceIntegrationTest` (token → row; `/oauth2/revoke` → gone; introspect inactive),
`ActuatorExposureIntegrationTest`, `MeEndpointIntegrationTest`, `LoginFlowIntegrationTest` (form login →
`/oauth2/authorize` with PKCE → code → token carries `roles`, `uid`, `org`, no stray metadata).

**Verify in the stack:** gateway login as `org1-admin` still works (PKCE on `web-app`: Spring's OAuth2 client
does not send `code_challenge` for confidential clients by default — if the login fails, add
`OAuth2AuthorizationRequestCustomizers.withPkce()` to the gateway's `CapturingServerOAuth2AuthorizationRequestResolver`
and flag the cross-service change in the PR; fallback: keep `require-proof-key=false` on `web-app` and enforce
PKCE only for PUBLIC clients).

**QA:** `## SEC — Hardening` SEC-01…12 (one per S-row, exact request + expected status); `## MIG` = "drop
schema uaa, restart".

## Phase 2 — settings, rich roles, user columns, audit core

**DDL:** `uaa.settings` singleton (`id smallint pk check (id=1)`; password `min_length 12`, `require_upper|lower|
digit|special`, `history_count 5`, `expiry_days 0`, `hibp_check false`; lockout `threshold 5`,
`duration_seconds 900`, `permanent_after 5`; `session_idle_seconds 1800`, `session_max_seconds 43200`,
`remember_me_enabled false`, `remember_me_seconds`; `max_access_token_ttl_seconds 3600`,
`default_access_token_ttl_seconds 900`, `default_refresh_token_ttl_seconds 43200`; `client_secret_validity_days
365`, `client_secret_grace_hours 24`; `key_rotation_days 90`, `key_retire_days 7`; `self_registration_enabled
false`; `audit_retention_days 365`; `updated_at`, `updated_by`, `version`), seeded with one row.
`uaa.roles` + `description`, `scope` CHECK (`REALM|ORGANIZATION|CLIENT`), `system_role`, `inherits_from_id`,
timestamps; `uaa.role_permissions(role_id, permission)`. `uaa.users` + `email_verified`, `activated_at`,
`failed_login_attempts`, `lockout_count`, `locked_until`, `locked_permanently`, `password_changed_at`,
`last_sign_in_at`, `last_sign_in_client_id`, `last_sign_in_ip`, `last_sign_in_via`; indexes on `lower(email)`,
`lower(username)`. `uaa.password_history`. `uaa.audit_events` (`id bigserial`, `occurred_at`, `event_type`,
`outcome` CHECK (`SUCCESS|FAILURE`), `reason_code`, `actor_type` CHECK (`USER|CLIENT|SYSTEM|ANONYMOUS`),
`actor_id`, `actor_name`, `target_type` CHECK (`USER|ROLE|CLIENT|IDP|SETTINGS|KEY|SESSION|INVITATION|TOKEN`),
`target_id`, `target_name`, `client_id`, `ip`, `user_agent`, `before_json jsonb`, `after_json jsonb`, `detail`,
`trace_id`; indexes on time, type+time, actor+time, target+time, client+time). Seeds: the five roles get
`system_role=true`, scopes (`SUPER_ADMIN`/`USER` REALM, `ORG_ADMIN`/`STORE_*` ORGANIZATION), `SUPER_ADMIN` gets
every permission; also seed `STORE_RETAIL` (in `Roles.java`, never seeded).

**Code:**
- `settings/Settings` entity (`@Version`), `SettingsService.current()` (caffeine 30 s) / `update()` (bounds →
  `SettingsInvalidException`, stale version → `SettingsConflictException`, audit `settings.updated` with diff).
- `domain/Permission` enum (`users:read`, `users:write`, `users:invite`, `users:sessions`, `users:unlock`,
  `roles:read`, `roles:write`, `clients:read`, `clients:write`, `clients:secrets`, `idps:read`, `idps:write`,
  `settings:read`, `settings:write`, `audit:read`, `keys:read`, `keys:rotate`, `dashboard:read`) — grouped for the
  UI; `GET /api/v1/admin/permissions`.
- `Role` gains the fields + `@ElementCollection` permissions; `RoleService`: name `^[A-Z][A-Z0-9_]{1,79}$`,
  `DuplicateRoleNameException`, system role name immutable / undeletable (`SystemRoleImmutableException`),
  in-use delete → `RoleInUseException`, inherits cycle → `RoleInheritanceCycleException`,
  `effectivePermissions()`, `PUT /roles/{id}/permissions` audits `role.permissions.updated`. `RoleDto` gains
  `description, scope, systemRole, inheritsFrom, permissions, effectivePermissions, userCount`.
- `User.status()` derived: `!enabled → DISABLED`, locked → `LOCKED`, `activatedAt==null && hash==null → PENDING`,
  else `ACTIVE`. `UserDto` gains `status, emailVerified, lastSignInAt, lastSignInClientId, lastSignInVia,
  lockedUntil, createdAt`.
- `audit/AuditEventType` (the mock vocabulary + lifecycle events), `AuditEventEntity`, `AuditService.record()`
  (joins the tx) / `recordDetached()` (REQUIRES_NEW, for listeners), `AuditActorResolver` (Jwt with `uid` → USER,
  without → CLIENT, session → USER, none → ANONYMOUS), ip/UA from the request (`forward-headers-strategy: NATIVE`
  makes `getRemoteAddr()` right behind the gateway), `traceId` from the tracer, `AuditDiff.of(before, after)` on
  DTO snapshots (never entities, never hashes/secrets). Every admin mutation from here on records. `AuditRetentionJob`
  nightly.
- `JwtCustomizerConfig` adds `permissions` (effective, sorted). `JpaUserDetailsService` adds `PERM_<key>`
  authorities to session principals so the SPA can hide menu items from `/me.permissions`. The admin gate stays
  `SCOPE_super_admin or ROLE_SUPER_ADMIN` in this PR.
- Endpoints: `GET/PUT /api/v1/admin/settings`; roles CRUD + `PUT /{id}/permissions`; `GET /permissions`.

**Frontend:** Roles screen per the mock (System/Custom filters, search, columns Role+description / Scope / Users /
Perms / Type; detail pane with lock notice, slug-normalised name, description, scope segmented control, inherits
chips, permissions matrix with per-group select-all, counts line, assigned-users count linking to Users). Settings
screen (rail row enabled): General (display name, support email, default locale, self-registration shown disabled
with a note, require verification), Authentication (password policy, brute-force), Sessions & tokens; Signing keys
and Danger zone arrive in phases 6/3; Email section rendered "not built" with the delivery-port note. Dirty-state
Save/Discard on the kit's `form-dirty` helper; optimistic `version`.

**Errors:** `ROLE_NAME_TAKEN` (CONFLICT), `SYSTEM_ROLE_IMMUTABLE` (FORBIDDEN), `ROLE_IN_USE` (CONFLICT),
`ROLE_INHERITANCE_CYCLE` (UNPROCESSABLE), `PERMISSION_UNKNOWN` (VALIDATION), `SETTINGS_INVALID` (VALIDATION),
`SETTINGS_CONFLICT` (CONFLICT).

**Tests:** unit `RoleServiceTest`, `SettingsServiceTest`, `AuditDiffTest`, `AuditActorResolverTest`,
`UserStatusTest` (`MutableClock` matrix), `PermissionTest`; integration `AdminRoleApiIntegrationTest`,
`AdminSettingsApiIntegrationTest` (stale version 409; audit row), `PermissionsClaimIntegrationTest`.

**QA:** `## ROL`, `## SET` sections; AUT-06 extended to the `permissions` claim.

## Phase 3 — lockout, password policy, rate limiting, sessions, self-service

- **Lockout** via `UserDetails` flags checked by `DaoAuthenticationProvider`'s pre-check (locked accounts never
  count another attempt, no timing leak): `accountNonLocked = !(lockedPermanently || lockedUntil > now)`,
  `credentialsNonExpired` from `password_changed_at` + `expiry_days`. `security/AccountLockoutListener` on
  `AuthenticationFailureBadCredentialsEvent` (only `UsernamePasswordAuthenticationToken`): increment; at threshold
  lock for duration, `lockout_count++`, permanent when `lockout_count >= permanent_after`; audit
  `user.login.failed` (reason `BAD_CREDENTIALS|LOCKED|DISABLED|PASSWORD_EXPIRED|RATE_LIMITED`) and `user.locked`.
  `AuthenticationSuccessEvent` → reset counters, `last_sign_in_at/ip/via=PASSWORD`, audit `user.login`.
  `POST /users/{id}/unlock` (idempotent, audit `user.unlocked`). `security/LoginFailureHandler` →
  `/login?error=locked|disabled|expired|invalid&attemptsLeft=N`; the SPA renders "N attempts left before an
  M-minute lock". Explicit `DaoAuthenticationProvider` bean (so a checker can be attached later).
- **Password policy:** `password/PasswordPolicyValidator` (length, classes, not username/email local part;
  fieldErrors per rule), `PasswordHistoryService` (last N via `encoder.matches`), `CompromisedPasswordGate` over
  Spring Security's `HaveIBeenPwnedRestApiPasswordChecker` (only when `hibp_check`; transport failure logs and
  passes — it is a check, not a decision; javadoc says so). One funnel `PasswordService.setPassword(user, raw,
  reason)` used by admin reset, self-service, invitation accept, reset-link accept. Bcrypt strength 12 for new
  hashes.
- **Rate limiting:** `ratelimit/RateLimiter` (caffeine `expireAfterWrite(window)`, key `ip|rule`, fixed window),
  `RateLimitFilter` (`FilterRegistrationBean`, POST on `/login`, `/oauth2/token`, `/api/v1/public/*`; defaults
  10/60/20 per minute from `com.asrevo.cvhome.uaa.rate-limit.*`), 429 `application/problem+json` from
  `ProblemDetailFactory` + `Retry-After`. Needs `ErrorCategory.TOO_MANY_REQUESTS` and `UaaErrors.RATE_LIMITED`.
- **Sessions:** `session/SessionAdminService` over `FindByIndexNameSessionRepository` (`findByPrincipalName`,
  revoke one/all, `activeCount()` from `uaa.spring_session`); `SessionMetadataSuccessHandler` stores ip/UA/via/
  createdAt attributes and sets the idle timeout from settings; `SessionMaxAgeFilter` enforces `session_max`;
  `SettingsAwareRememberMeServices` (token-based, key from `com.asrevo.cvhome.uaa.remember-me.key`, no-op when
  the setting is off; the sign-in form gains the checkbox when on).
- **Token revocation (S16 closed):** `token/TokenRevocationService.revokeAllForUser(username)` (JDBC id scan on
  `oauth2_authorization` → `authorizationService.remove`), `revokeAllForClient(id)`; audit `token.revoked`. Wired
  into disable/delete/admin reset/permanent lock, and self-service password change (revokes *other* sessions).
- **Self-service** `/api/v1/account/**` (`isAuthenticated()`, user principal only): `GET me`, `PUT password`
  (`CurrentPasswordMismatchException` → 400), `GET sessions`, `DELETE sessions/{id}`, `DELETE sessions`. Admin:
  `GET/DELETE /users/{id}/sessions[/{sid}]`, `POST /admin/sessions/revoke-all` (danger zone).
- **Frontend:** Users: Status column + Locked filter + Unlock action; detail pane Security section (password
  changed N days ago, failed attempts) and Active sessions list with per-session revoke + Sign out everywhere;
  `/account` page in uaa-fe (profile, change password, my sessions); console-ui `/profile` gains Change password +
  sessions via `/uaa/api/v1/account/**` (closes P-03); Settings → Danger zone "Revoke all sessions"; sign-in error
  states.

**Errors:** `PASSWORD_POLICY_VIOLATION` (VALIDATION), `PASSWORD_REUSED`, `PASSWORD_COMPROMISED` (UNPROCESSABLE),
`CURRENT_PASSWORD_MISMATCH` (VALIDATION), `SESSION_NOT_FOUND` (NOT_FOUND), `RATE_LIMITED` (TOO_MANY_REQUESTS).

**Tests:** unit `AccountLockoutListenerTest`, `JpaUserDetailsServiceTest`, `PasswordPolicyValidatorTest`,
`PasswordHistoryServiceTest`, `CompromisedPasswordGateTest`, `RateLimiterTest` (caffeine `Ticker`),
`RateLimitFilterTest`, `SettingsAwareRememberMeServicesTest`, `TokenRevocationServiceTest`; integration
`LockoutIntegrationTest` (5 wrong → locked even with the right password; clock past duration → ok; audit rows),
`RateLimitIntegrationTest` (limit 3 → 4th 429 with `traceId`), `SelfServicePasswordIntegrationTest`,
`SessionAdminIntegrationTest` (two logins → two sessions with ip/UA; revoke one → its cookie dead),
`PasswordExpiryIntegrationTest`, `DisableRevokesIntegrationTest` (disable → sessions and authorizations gone).

**QA:** `## LCK`, `## RL`, `## PWD`, `## SES` sections.

## Phase 4 — user lifecycle: search, invitations, reset links, delivery port, verification

**DDL:** `uaa.invitations` (`user_id`, `email`, `token_hash unique`, `status` CHECK (`PENDING|ACCEPTED|REVOKED|
EXPIRED`), `expires_at`, `created_by`, `accepted_at`; one PENDING per user), `uaa.password_reset_tokens`
(`token_hash`, `expires_at`, `used_at`, `revoked_at`).

**Code:**
- `invitation/OneTimeTokens` (256-bit random, SHA-256 base64 — lifted from tenancy's `InvitationService`).
  **Delivery is an outbox event, not a call:** issuing a link registers `InvitationIssuedEvent` /
  `PasswordResetLinkIssuedEvent(userId, recipientEmail, recipientName, link, expiresAt, locale)` on the `User`
  aggregate (committed with the row, delivered at-least-once). The default consumer is
  `delivery/LoggingLinkDeliveryHandler` (`@OutboxHandler`, idempotent by event id): logs kind/recipient/expiry,
  and the link itself only when `com.asrevo.cvhome.uaa.links.log-links=true` (set in `application-lcl.yml`).
  The future SMS/WhatsApp service subscribes to the same events from `uaa-events`; the javadoc says so. The
  admin response still carries the link once. `LinkBuilder` = pinned issuer + `/accept-invitation?token=` /
  `/reset-password?token=`. `UserCreated/Disabled/DeletedEvent` are published here too; a tenancy consumer that
  reconciles `org_member` (known gap 99) is a follow-up, the contract ships now.
- `InvitationService.invite(req, actor)` creates the user PENDING (no hash, `activated_at` null), the token row,
  audits `user.created` + `invitation.created`, returns `{user, invitation, link, expiresAt}` once and calls the
  port; `resend`, `revoke`; public `accept(token, password)` → `PasswordService`, `activated_at`,
  `email_verified=true`, audit `invitation.accepted`. Validity `P7D`. `PasswordResetService.createLink(userId,
  revokeSessions, actor)` (super-admin guard; `PT1H`) / `acceptLink(token, newPassword)` (revokes sessions+tokens).
- `AdminService.getUsers(UserSearch(q ILIKE over username/email/names, status, role, metadata), pageable)`,
  `counts()` (`{total, active, pending, locked, disabled}`), `verifyEmail(id)`; `UpdateUserRequest.email`
  editable (resets `email_verified`, audited); `CreateUserRequest.password` optional (uses `PasswordService`);
  `PUT /{id}/reset-password` kept (the SDK calls it) but guarded, policy-checked, session-revoking.
- Endpoints: admin `GET /users?q=&status=&role=&metadata[k]=`, `GET /users/counts`, `POST /users/invitations`,
  `POST /users/{id}/invitations/resend`, `DELETE /users/{id}/invitations`, `POST /users/{id}/password-reset-links`,
  `POST /users/{id}/email/verify`; public `GET /api/v1/public/invitations/{token}` (email, username, expiry, the
  password rules), `POST …/accept {password}`, `GET /api/v1/public/password-resets/{token}`, `POST …/accept`.
- **Frontend:** Users screen per the mock: tiles (total/active, pending invites, locked), filters All/Active/
  Pending/Locked/Disabled, search, Invite user dialog → one-time link dialog (copy console-ui's
  `invitation-link-dialog`), Invitations tab (resend/revoke), detail pane with editable email + Verified/Unverified
  badge, "Issue reset link", metadata rows with working remove; public pages `/accept-invitation`,
  `/reset-password` (permitAll routes, password-strength from the kit's `forms`). "Forgot password?" on the sign-in
  page opens an explanation that resets are admin-issued. CSV import stays not built.

**Errors:** `INVITATION_NOT_USABLE` (NOT_FOUND, one code for missing/expired/spent), `INVITATION_ALREADY_PENDING`
(CONFLICT), `USER_NOT_PENDING` (UNPROCESSABLE), `RESET_TOKEN_NOT_USABLE` (NOT_FOUND), `USERNAME_TAKEN`,
`EMAIL_TAKEN` (CONFLICT; mapped to `UaaConflictException` in `UaaApiErrors`).

**Tests:** unit `InvitationServiceTest` (event registered; token never in the log), `PasswordResetServiceTest`,
`OneTimeTokensTest`, `UserSearchSpecificationTest`, `LoggingLinkDeliveryHandlerTest` (idempotent on redelivery);
integration also asserts the `outbox_record` row is `COMPLETED`; integration
`InvitationFlowIntegrationTest` (invite → weak password 400 with fieldErrors → strong → login works → second
accept 404), `PasswordResetLinkIntegrationTest`, `UserSearchIntegrationTest`, `PublicApiGateIntegrationTest`.

**QA:** `## INV`, `## VER`; ADM extended with search/status/counts.

## Phase 5 — clients

**DDL:** `uaa.client_extension(registered_client_id pk fk, enabled, description, disabled_at, disabled_by,
last_token_issued_at, timestamps)` seeded for the four clients; `uaa.client_secret_history(id, registered_client_id,
secret_hash, created_at, expires_at, revoked_at)`.

**Design (constraints found in SAS 7.0.2):**
- `ClientSecretAuthenticationProvider` is `final` and its PKCE verifier package-private → grace is
  `security/GraceAwareClientSecretAuthenticationProvider`: matches the presented secret against the primary hash,
  else a live `client_secret_history` row; builds a one-client `RegisteredClient` view carrying the matching hash
  and delegates to a fresh SAS provider over that view (PKCE and expiry handling stay SAS's). Registered through
  `clientAuthentication(c -> c.authenticationProviders(list -> replace the SAS one))`; fallback if that consumer
  is not on 7.0.2: `authenticationProvider(grace)` prepends.
- Enable/disable = `security/EnabledAwareRegisteredClientRepository` (`@Primary`) filtering **`findByClientId`
  only** — `JdbcOAuth2AuthorizationService`'s row mapper calls `findById` and throws on null. Disable also revokes
  the client's authorizations; SAS answers `invalid_client`.
- Type derived (not stored): only `none` → PUBLIC; grants ⊆ {client_credentials} → MACHINE; else CONFIDENTIAL.
- `rotateSecret(id)` → new random secret, `client_secret_expires_at = now + validity_days` (null when 0), old
  hash into history valid `grace_hours`, audit `client.secret.rotated`, returned once as `RotatedSecret`;
  `DELETE /{id}/previous-secret` ends grace early. Create returns the generated secret once
  (`CreatedClientResponse`, exists unused). `reset-secret` kept as an alias without grace. TTL above
  `max_access_token_ttl` → `ClientTokenTtlExceedsPolicyException`; `JwtCustomizerConfig` clamps `exp`.
- Redirect URI validation: plain `http` only for `localhost`/`*.gateway.com` under `lcl`; wildcards rejected
  (`InvalidRedirectUriException`).
- `ClientSummary` widened: `type, enabled, secretExpiresAt, lastTokenIssuedAt, grantTypes`; `GET /clients?q=&enabled=&type=`;
  `GET /clients/stats`; `GET /clients/options` gains `@PreAuthorize` and the scope catalogue.
- Endpoints: `POST` (201 + secret), `PUT /{id}`, `DELETE /{id}` (revokes), `POST /{id}/enable|disable`,
  `POST /{id}/rotate-secret`, `DELETE /{id}/previous-secret`, `POST /clients/rotate-all` (danger zone; incident
  response — invalidates every s2s secret after the grace window, documented).

**Frontend:** Clients list per the mock (tiles, filters All/Enabled/Disabled/Machine, search, five columns,
enable toggle); client page gains Secret card (masked, "shown once", Rotate, expiry line, revoke previous), Type,
Status; Settings → Danger zone "Rotate every client secret".

**Errors:** `CLIENT_ID_TAKEN` (CONFLICT), `CLIENT_DISABLED`, `CLIENT_NOT_CONFIDENTIAL` (UNPROCESSABLE),
`CLIENT_TOKEN_TTL_EXCEEDS_POLICY`, `INVALID_REDIRECT_URI` (VALIDATION), `CLIENT_NO_PREVIOUS_SECRET` (NOT_FOUND),
`CLIENT_ID_MISMATCH` (UNPROCESSABLE).

**Tests:** unit `GraceAwareClientSecretAuthenticationProviderTest`, `EnabledAwareRegisteredClientRepositoryTest`,
`ClientClientDetailsMapperTest` (type), `AdminClientServiceTest`, `RedirectUriRulesTest`; integration
`ClientSecretRotationIntegrationTest` (old works in grace, new works, revoke previous → old `invalid_client`),
`ClientDisableIntegrationTest`, `AdminClientApiIntegrationTest` (create returns the secret once; reads never do).

**QA:** `## CLI`.

## Phase 6 — signing keys

**DDL:** `signing_keys` recreated as `id, kid unique, algorithm, status CHECK (ACTIVE|RETIRING|RETIRED),
public_jwk_json text, private_jwk_enc text (secret-crypto envelope), created_at, activated_at, retire_after,
retired_at`; the `jwk_json oid` column and `@Lob` are gone.

**Code:** `keys/SigningKeyMaterialMapper` (public JWK plain, private encrypted; decrypt failure →
`SigningKeyUnusableException`, key excluded from signing, logged, JWKS never 500s);
`KeyRotationService.rotate(actor)` (pessimistic lock, new ACTIVE, old → RETIRING with `retire_after = now +
retire_days`, cache invalidated, audit `key.rotated`), `retireDue()` (→ RETIRED, audit `key.retired`),
`KeyRotationScheduler` hourly (rotate when `activated_at + rotation_days < now` and `rotation_days > 0`);
`JwksConfig` over `keys/JwkSetCache` (ACTIVE with private part first, RETIRING public-only; `NimbusJwtEncoder`
selects `privateOnly`, the JWKS endpoint serves both). Other services refetch JWKS on an unknown `kid`.
Endpoints: `GET /api/v1/admin/keys`, `POST /keys/rotate`, `GET /keys/status`.

**Frontend:** Settings → Signing keys table (kid, algorithm, created, status), Rotate now, rotation interval,
retire window, default algorithm (RS256 selectable; ES256 listed disabled).

**Errors:** `SIGNING_KEY_UNUSABLE`, `NO_ACTIVE_SIGNING_KEY` (INTERNAL).

**Tests:** unit `SigningKeyMaterialMapperTest`, `KeyRotationServiceTest` (`MutableClock`), `JwkSetCacheTest`;
integration `KeyRotationIntegrationTest` (token A verifies during the retiring window, not after; DB column
shows `ENC:`), `JwksEndpointIntegrationTest` (never exposes `d`/`p`/`q`).

**QA:** `## KEY`.

## Phase 7 — identity providers and the identity-first sign-in

**Dependency:** `libs.spring.boot.starter.oauth2.client` on uaa (the dormant `s2s` registration becomes live;
harmless — the dynamic repository delegates to the properties-built one, cua's `DynamicClientsConfig` shape).

**DDL:** `uaa.identity_providers` (`alias unique ^[a-z0-9-]{2,50}$` = Spring `registrationId`, `display_name`,
`type` CHECK (`OIDC|OAUTH2`), `preset` CHECK (`GOOGLE|MICROSOFT|APPLE|GITHUB|GENERIC_OIDC|GENERIC_OAUTH2`),
`enabled`, `hide_on_login`, `sort_order`, `client_id_enc`, `client_secret_enc`, `issuer_uri`, the four manual
endpoints, `jwk_set_uri`, `scopes`, `user_name_attribute`, `client_auth_method`, `email_domains`,
`account_linking` CHECK (`LINK|CONFIRM|REJECT`), `jit_provisioning`, `default_roles`, `trust_email_verified`,
`attribute_mapping jsonb`, timestamps); `uaa.user_identities(user_id, provider_id, subject, email, linked_at,
last_login_at)` unique `(provider_id, subject)`.

**Code (copy cua's shapes):**
- `idp/IdentityProviderMapper` (encrypt client id + secret; readable carries `hasClientSecret` only),
  `IdentityProviderService` (CRUD, `test(id)` → discovery/token reachability → `IdpDiscoveryFailedException
  extends ExternalProviderException`, `visibleForLogin()`, `discoverByEmail()` longest-suffix over
  `email_domains`, audits with secret-redacted diffs), `IdpPreset` enum (Google/Microsoft tenant/GitHub with
  `/user/emails` fallback/Apple `form_post` + ES256 client-secret JWT from the stored `.p8` — **Apple ships
  scaffolded and [not verified]**, it needs a developer account/generic OIDC discovery cached 1 h/generic OAuth2).
- `idp/DynamicClientRegistrationRepository` (properties first, then DB by alias, caffeine 10 min, evicted on
  save/delete; redirect `{baseUrl}/login/oauth2/code/{alias}` on uaa's own origin), `LoginHintAuthorizationRequestResolver`.
- `security/BrokeredOAuth2UserService` / `BrokeredOidcUserService` → `IdentityBrokerService.resolve(alias,
  attributes)`: known identity → login (roles from `default_roles` idempotent, mapping refresh, audit
  `user.login via=IDP:<alias>`); else local user by email per `account_linking` (`LINK` needs `email_verified`
  when `trust_email_verified`, else behaves as `CONFIRM`; `CONFIRM` stashes a `PendingLink` in the session and
  fails with `link_required` → SPA asks the password once → `POST /api/v1/public/idp/link-confirm`; `REJECT`);
  else `jit_provisioning` → create user (`username = email`, so `sub` stays stable) or fail `unknown_user`;
  locked/disabled local user → fail. `BrokeredLoginSuccessHandler` swaps the context to a
  `UsernamePasswordAuthenticationToken` over the local `UserDetails` (SAS's Jackson allow-list and the customizer
  see the standard principal), stores session metadata, then `SavedRequestAwareAuthenticationSuccessHandler`
  resumes the saved `/oauth2/authorize` — the SAS chain gets the **same `RequestCache` bean explicitly** so the
  handoff is certain. `oauth2Login` on the app chain with `loginPage("/login")`, `csrf.ignoringRequestMatchers
  ("/login/oauth2/code/*")` for Apple's form_post, permit `/oauth2/authorization/**`, `/login/oauth2/**`;
  `IdpLoginFailureHandler` → `/login?error=link_required|idp_rejected|idp_unknown|locked|idp` + audit.
- Public: `GET /api/v1/public/idps` (enabled, not hidden, ordered), `POST /api/v1/public/idps/discover {email}`
  (hidden providers included — hiding is about the button), `GET /api/v1/public/login/settings` (display name,
  remember-me on/off), `GET /api/v1/public/login/context` (client name from the saved authorization request).
- Admin `/api/v1/admin/identity-providers`: CRUD, `enable|disable`, `test`, `PUT /order`, `GET /presets`; users
  `GET/DELETE /users/{id}/identities[/{iid}]`; self `GET/DELETE /account/identities[/{iid}]` (refuse unlinking the
  last credential of a password-less user → `LastCredentialException`).

**Frontend:** Identity providers screen per the mock (type-chooser modal, SAML card disabled "not built",
drag-to-reorder with CDK, live sign-in preview, detail pane tabs Connection / Mapping / Behaviour, Test, redirect
URI with copy). Sign-in page becomes identity-first: step 1 email + provider buttons + home-realm discovery; step
2 identity chip, password with show/hide, attempts-left, remember-me when enabled, "Forgot password?"; link-confirm
step for `CONFIRM`; step 3 done. The password step stays a native POST with `_csrf`. Client context card from
`/login/context`.

**Errors:** `IDP_NOT_FOUND`, `IDENTITY_NOT_FOUND` (NOT_FOUND), `IDP_ALIAS_TAKEN`, `IDENTITY_ALREADY_LINKED`
(CONFLICT), `IDP_CONFIG_INVALID`, `LINK_CONFIRMATION_INVALID` (VALIDATION), `IDP_DISCOVERY_FAILED` (502, carries
`provider`), `LAST_CREDENTIAL` (UNPROCESSABLE).

**Tests:** unit `IdentityProviderMapperTest`, `IdpPresetTest`, `IdentityBrokerServiceTest` (every branch),
`EmailDomainDiscoveryTest`, `DynamicClientRegistrationRepositoryTest`, `AppleClientSecretFactoryTest`;
integration `BrokeredLoginIntegrationTest` with an in-test stub OIDC issuer (`@TestConfiguration` controller for
discovery/authorize/token/userinfo/jwks on the same port; no WireMock in the catalogue): JIT creates a user and
resumes `/oauth2/authorize` → token has `roles`/`permissions`; second login reuses the identity; CONFIRM path;
REJECT; disabled provider 404; discovery by email; admin gate; `test` against a dead port → 502.

**QA:** `## IDP` (a real Google/GitHub app is [not verified] until someone configures one; the stub path is
[unit only]); `## SIN` for the new sign-in flow incl. AR/RTL.

## Phase 8 — protocol audit hooks, audit API, dashboard, rail

- **Hooks:** `@EventListener(AuthenticationSuccessEvent)` on `OAuth2AccessTokenAuthenticationToken` → `token.issued`
  (client, grant, principal, scopes, ttl; updates `users.last_sign_in_client_id` on code grants and
  `client_extension.last_token_issued_at`) — the event fires after the authorization is saved; fallback if the
  SAS chain's publisher turns out null: record from the `OAuth2TokenCustomizer` for `ACCESS_TOKEN`.
  `OAuth2TokenRevocationAuthenticationToken` → `token.revoked`; `LogoutSuccessEvent` → `user.logout`. A
  `DefaultAuthenticationEventPublisher` bean with `setAdditionalExceptionMappings` for
  `OAuth2AuthorizationCodeRequestAuthenticationException` and `OAuth2AuthenticationException` → two in-repo
  failure events (two-arg constructors) → `client.redirect_uri.mismatch` (description names `redirect_uri`) and
  `client.auth.failed`.
- **Query API:** `GET /api/v1/admin/audit` (filters: types, actor, target, client, outcome, from/to, q; sort
  `occurred_at desc`; max page 200), `GET /{id}`, `GET /types`, `GET /export` (`text/csv`, streaming 1 000 rows a
  page, hard cap 100 000 → `AuditExportTooLargeException`; `audit/CsvWriter` RFC-4180, ~30 lines, no library).
- **Dashboard:** `GET /api/v1/admin/dashboard?range=24h|7d|30d` → sign-in series (hour/day buckets via
  `date_trunc`), active sessions, tokens issued, top clients, user counts, locked users, secrets expiring 30 d,
  key status, last failures, security-posture checks computed from data (key age vs rotation interval, users
  without password, clients without PKCE / with expiring secret, HIBP on/off, lockout spike >5 in 24 h with the
  top `/24`); caffeine 30 s per range. `GET /api/v1/admin/counts` → `{users, roles, clients, idps}` for the rail.
- **Frontend:** Audit log screen (tiles, category segments with counts, outcome/range chips, search, table, detail
  pane fact grid / reason / changes diff / raw JSON + copy, "All by this actor" / "All from this IP" pivots, Load
  older, Export CSV as a link, Live = 10 s polling); Dashboard per the mock with the kit's `kpi-grid`,
  `ranked-list`, `progress-track` and an inline SVG bar chart; Users table gains "Last sign-in" + client; Clients
  gain last-token and 24 h counts; rail: Users badge, Dashboard/Audit/Providers/Settings become links;
  notifications bell stays out (no store).

**Errors:** `AUDIT_EVENT_NOT_FOUND` (NOT_FOUND), `AUDIT_EXPORT_TOO_LARGE` (PAYLOAD_TOO_LARGE), `AUDIT_QUERY_INVALID`
(VALIDATION).

**Tests:** unit `CsvWriterTest`, `AuditSpecificationsTest`, `OAuth2FailureEventMappingTest`, `DashboardBucketingTest`;
integration `TokenIssuedAuditIntegrationTest`, `RedirectUriMismatchAuditIntegrationTest`
(`redirect_uri=http://evil` → 400 + row naming `web-app`), `ClientAuthFailureAuditIntegrationTest`,
`LogoutAuditIntegrationTest`, `AuditApiIntegrationTest` (filters, paging, gate, CSV, 413), `DashboardApiIntegrationTest`.

**QA:** `## AUD`, `## DSH`.

## Phase 9 — closure

- `uaa-client` contract: `UserAccountService` gains `invite(...)`, `createResetLink(userId)`, `search(q, status,
  role, page, size)`, `counts()`; `ReadableUser` gains `status`, `emailVerified`, `lastSignInAt` and drops the dead
  `lastAccess/loginTime` and the shadowed `userName/active` duplicates; `UaaApiErrors` maps the new conflict codes;
  `uaa-client-impl` follows (`createUser` becomes one call now that create takes a password). tenancy's
  `ManagedUserAccountServiceImpl`/`SignupServiceImpl` touched only for the DTO change; `references/uaa-client.md`
  updated (and its stale "sole consumer" and `assignable-roles` lines corrected).
- `lessons.md`: "Closed by feat/uaa-sso" lines on every closed entry; remaining open: realm switcher,
  notifications, MFA, CSV import, SAML. `npm run lint` passes. `uaa-fe/README.md` rewritten (port 4300, `npm run
  kit` first). `references/store-core.md`, `authentication.md`, `frontends.md` updated (Nebular/module-federation
  notes are stale).
- Full regression: `./gradlew check` on `:store-core:uaa`, `:store-commons:uaa-client`,
  `:store-commons:uaa-client-impl`, `:store-commons:errors`, `:store-commons:ui-kit`,
  `:store-core:tenancy:tenancy-service`, `:store-core:console-ui`; `integrationTest` with Docker; both consoles
  built; every case in `uaa-qa.md` run and tagged honestly; console-ui and tenancy QA files get cross-references
  (account page, DTO change). `/go` from the worktree; PR body per template keeping all eight checklist rows;
  labels `type/feature` + `warn/config` (new env vars, TTL/PKCE change, actuator override).

---

## Verification gate (every phase)

```bash
./gradlew :store-core:uaa:checkstyleMain :store-core:uaa:checkstyleTest :store-core:uaa:checkstyleIntegrationTest
./gradlew :store-core:uaa:build -x test -x check
./gradlew :store-core:uaa:test
./gradlew :store-core:uaa:integrationTest          # Docker
cd store-core/uaa/src/main/resources/uaa-fe && npm run build && npm run lint
lcl restart uaa --stack uaa-sso && lcl logs uaa --errors --stack uaa-sso
```

Against the running stack (ports from `lcl urls --stack uaa-sso`):
- run the phase's `http/*.http` blocks (super admin 200, org admin 403, anonymous 401, public endpoints 200);
- drive the phase's screens in Chrome (`claude-in-chrome`), EN and AR, console clean, no failed requests;
- **canary:** sign in through the gateway as `org1-admin` and load the console (AUT-01) after every phase — the
  gateway login is what breaks first on CSRF, PKCE, issuer, TTL and principal-type changes;
- mint an s2s token (AUT-04) and call a pod endpoint to prove resource servers still accept uaa tokens;
- update `store-core/uaa/qa/uaa-qa.md`: new cases tagged from what was actually run, `REG` rows for every bug
  found on the way, `99` updated.

## Files most touched

- `store-core/uaa/src/main/java/com/asrevo/cvhome/uaa/config/{AppSecurityConfig, AuthorizationServerConfig, JwtCustomizerConfig, JwksConfig}.java`
- `store-core/uaa/src/main/java/com/asrevo/cvhome/uaa/{audit, settings, security, password, ratelimit, session, token, invitation, client, keys, idp, web/account, web/pub}/…` (new)
- `store-core/uaa/src/main/java/com/asrevo/cvhome/uaa/service/{AdminService, AdminClientService, RoleService, KeyPairService}.java`
- `store-core/uaa/src/main/resources/init-sql/{schema.sql, data-common.sql}`, `application*.yml`, `build.gradle`
- `store-commons/uaa-client/.../{UaaErrors, UaaApiErrors, UserAccountService, ReadableUser, PersistableUser}.java`, `uaa-client-impl`
- `store-commons/errors/.../ErrorCategory.java` (429)
- `store-commons/ui-kit/{src/lib/config (xsrf), ui/src/lib/{segmented-control, chip-matrix, pair-list, reorderable-list, kpi-delta, bar-chart, fact-grid, diff-table, code-panel} (new, with specs), uaa/src/lib/*.service.ts (new admin services)}`
- `store-core/uaa/uaa-events/` (new module: event records), `settings.gradle`
- `store-core/uaa/src/main/resources/uaa-fe/src/app/{features/*, layouts/admin-shell, app.routes.ts}`, `src/locale/{en,ar}.json`, `lessons.md`, `README.md`
- `store-core/uaa/http/*.http` (+ `public-api.http`, `account-api.http`, `admin-{settings,audit,keys,identity-provider}-api.http`), `store-core/uaa/qa/uaa-qa.md`
- `.claude/skills/project-structure/references/{store-core, authentication, uaa-client, frontends}.md`
- possibly `store-core/gateway/.../CapturingServerOAuth2AuthorizationRequestResolver.java` (PKCE, see phase 1)

## Precedents to copy

- CSRF cookie + `oauth2Login` chains: `store-pod/cua/.../config/AppSecurityConfig.java`
- Dynamic client registrations: `store-pod/cua/.../config/DynamicClientRegistrationRepository.java`
- Secret encryption in the mapper: `store-pod/cua/.../web/mapper/SocialLoginConfigMapper.java`
- Hashed one-time tokens, no mail: `store-core/tenancy/.../manager/service/InvitationService.java`
- Audit in the same transaction: `store-core/tenancy/.../manager/service/TenancyAuditService.java`
- Browser-walk integration test: `store-pod/cua/src/integrationTest/.../LoginHandoffIntegrationTest.java`
- Pinned issuer: `store-pod/cua/.../config/AuthorizationServerConfig.java:87-95`
- Outbox events and handlers: `store-core/tenancy/tenancy-events` + `@OutboxHandler`s in `tenancy-service`,
  `references/events-outbox.md`

## Uncertainties, each with its fallback

1. `OAuth2ClientAuthenticationConfigurer.authenticationProviders(Consumer<List>)` on 7.0.2 → prepend with
   `authenticationProvider(...)`.
2. `AuthenticationSuccessEvent` for SAS tokens on the SAS chain → record from the token customizer.
3. Gateway sending PKCE once `web-app` requires it → `withPkce()` in the gateway resolver, else keep
   `require-proof-key=false` for the confidential `web-app`.
4. Broker callback resuming the saved `/oauth2/authorize` → both chains share the explicit `RequestCache` bean;
   verified by `BrokeredLoginIntegrationTest` before the IdP UI is built.
5. Apple preset → scaffolded, [not verified].
6. Logout is GET-or-POST until ui-kit posts — noted in `99`.

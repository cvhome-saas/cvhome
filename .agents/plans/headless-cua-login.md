# Headless cua: storefront-owned shopper login and registration

## Context

`store-pod/cua` is the shopper OAuth2 authorization server, one per pod, serving many stores. Its login and
registration pages are Thymeleaf templates inside cua (`templates/login.html`, `register.html`, ~140 lines of
inline CSS each, six `messages*.properties` bundles). `store-pod/landing-ui` renders every other storefront page
through per-store themes, so the login page is the one screen a merchant cannot theme. A half-built bridge
exists (cua links `../css/login.css`, which landing-ui serves from `theme.loginCss`); no theme implements it and
it is CSS-only. We are **not** finishing that bridge.

**Decision (confirmed by the user): headless cua.** cua keeps everything that is an authorization server:
`/oauth2/*`, JDBC session, form-login *processing* at `POST /login`, social login
(`/oauth2/authorization/{storeId}.{provider}`), `/connect/logout`, the store-scoped `users` table,
`JpaUserDetailsService`. cua stops rendering HTML. landing-ui owns the login and register pages as themed pages,
the way it owns `Customer`. The PKCE flow, token handling and `MultiIssuerJwtDecoder` trust are untouched.

What makes this cheap: cua and landing-ui are same-origin per store host (spg routes `/cua*` to cua, everything
else to landing-ui, custom domains included), and `client_id` already *is* the `StoreMerchantId`.

**Non-goals, decided:** no password reset, no rate limiting on registration, no designed Login/Register pages
for the eleven non-starter themes (the token-only fallbacks inherit each theme's look).

## Verified facts the design rests on

- Spring Boot 4.0.1 / Spring Security 7 / SAS 2.x. `prompt=login` makes SAS drop the current auth and invoke
  the configured `AuthenticationEntryPoint`, so swapping the entry point keeps that behaviour.
- `RequestCacheAwareLocaleInterceptor` (autoconfigure) is an MVC interceptor: it never runs for
  `/oauth2/authorize` or the entry point. The new helper must read `lang` itself: request param → SavedRequest
  param → default `en`, validated with `LanguageCode.isLanguage()`.
- Session cookie: Spring Session defaults, name `SESSION`, `SameSite=Lax`, path = context path → `/cua/`
  (`PathPrefixFilter` is `HIGHEST_PRECEDENCE`, before `SessionRepositoryFilter`). A top-level form POST from
  `/en/login` to `/cua/login` is same-origin and carries it.
- `X-Forwarded-Port` is set site-wide by the Caddy `(routes)` snippet, so `request.getServerPort()` is right on
  a shifted stack. `handle /cua*` keeps the prefix; `/cua/api/v1/public/...` needs no Caddyfile change.
- `Store-Id` does not reach cua. Public endpoints take `?store=` via the `StoreMerchantId` resolver, and public
  controllers in the repo carry no `@PreAuthorize` (see `PublicPaymentConfigurationController`).
- The merchant and content clients in cua (`ClientsConfig`, `CachedExternalMerchantStoreService`,
  `StoreLogoResolver`, `ExternalClientsTestConfiguration`) are used only by the two page controllers.
- `AuthService.login()` (landing-ui) unconditionally overwrites `postLoginRedirect`; registration needs a
  `returnTo` override.
- cua's `src/test` is empty; `req.http` is documented stale; QA files are `store-pod/cua/qa/cua-qa.md` and
  `store-pod/landing-ui/qa/landing-ui-qa.md`. Seeded shopper `user`/`revo` in
  `init-sql/stores/65f023632bc46470c104b76f/01-store.sql`.

## Contract

**Hand-off (cua → storefront):** `{scheme}://{host}[:port]/{lang}/login?auth=1[&error=invalid|social]`.
`auth=1` means "cua holds a SavedRequest for `/cua/oauth2/authorize`, render the form".

**cua does no translation.** Every user-facing string (labels, errors, button text) is rendered by landing-ui
from `locales/*.json` in the shopper's language; the six `messages*.properties` bundles and `LocalConfig`'s
locale resolver are deleted. `lang` survives in cua only as a URL path segment, because storefront routes are
locale-prefixed (`/en/login`, `/ar/login`), the same way `redirect_uri` is already `/{lang}/callback`. Error
states cross the boundary as a machine token (`error=invalid|social`), never as text.
Considered and rejected: redirecting to an unprefixed `{origin}/login?auth=1` and letting landing-ui pick the
locale. `libs/i18n/src/routing.ts` has `localeDetection: false`, so next-intl would send every shopper to the
default locale; only the bare `/` path gets the store-aware pick in `storefront/src/proxy.ts`. Echoing the
`lang` landing-ui already sends is deterministic and needs no new routing rule.

**Login POST (storefront → cua):** full-page `application/x-www-form-urlencoded` `POST /cua/login` with
`username`, `password`, `client_id` (= store id, what `JpaUserDetailsService` reads), `lang`.
- success with SavedRequest → 302 `/cua/oauth2/authorize?…` → 302 `{origin}/{lang}/callback?code=` (unchanged)
- success without SavedRequest → 302 `{origin}/{lang}/login` (no marker; storefront restarts authorize)
- failure → 302 `{origin}/{lang}/login?auth=1&error=invalid`; social failure → `…&error=social`

**Registration:** `POST /cua/api/v1/public/registration?store={id}&lang={lang}`, JSON
`{username, email, password, firstName?, lastName?}` → `201` no body. `400 COMMON.VALIDATION_FAILED` with
`fieldErrors` (existing advice), `409 CUA.REGISTRATION.USERNAME_TAKEN` / `EMAIL_TAKEN` (existing exceptions).
No auth-flow context needed.

**Social discovery:** `GET /cua/api/v1/public/social-logins?store={id}` →
`[{providerId, name, registrationId: "{storeId}.{provider}"}]`, never `appId`/`appSecret`. Buttons are plain
anchors to `/cua/oauth2/authorization/{registrationId}`, rendered only in the `auth=1` state (they need the
SavedRequest).

Both public endpoints live on a new stateless filter chain so they never touch session or RequestCache.

## Sequences

```
Happy:   HeaderActions → AuthService.login() → GET /cua/oauth2/authorize?client_id=S&lang=en&prompt=login…
         → cua saves request, StorefrontLoginEntryPoint → 302 O/en/login?auth=1
         → storefront SSR: social-logins?store=S → theme.pages.Login ?? DefaultLoginPage (form action=/cua/login)
         → POST /cua/login (username,password,client_id=S,lang=en) [SESSION cookie, same-origin]
         → success handler → 302 saved /cua/oauth2/authorize → 302 O/en/callback?code=… → token exchange (as today)
Failure: POST /cua/login bad password → 302 O/en/login?auth=1&error=invalid (SavedRequest survives, re-submit works)
Social:  O/en/login?auth=1 → GET /cua/oauth2/authorization/S.google → provider → /cua/login/oauth2/code/S.google
         → CustomOidcUserService (unchanged) → success handler → saved authorize → callback
Register: O/en/register → useRegisterForm.submit() → POST /cua/api/v1/public/registration?store=S
         → 201 → AuthService.login(ctx, {returnTo: `/${locale}`}) → Happy path
```

No loop is possible: cua only ever redirects to the storefront; the storefront calls cua only on the no-marker
`/login` path (`LoginRedirect`, unchanged) or on an explicit click; the marker page never auto-redirects.
`/error` stays `permitAll`.

## Worktree

`git worktree add .claude/worktrees/feat-headless-cua-login -b feat/headless-cua-login origin/main`; its own
`lcl start -d --stack cua-login`.

## PR 1: additive, safe to deploy alone

### cua backend (`store-pod/cua/src/main/java/com/asrevo/cvhome/cua/`)

New:
- `security/StorefrontUrls` (final, static): `origin(HttpServletRequest)` factored out of
  `DynamicRegisteredClientRepository.extractHost` (scheme/serverName/port rule, no contextPath);
  `language(request, RequestCache)` → `LanguageCode`; `loginPage(request, cache, boolean pending, String error)`;
  `home(request, cache)`. `DynamicRegisteredClientRepository` calls `origin(...)`; the redirect URI must stay
  byte-identical to `{origin}/{lang}/callback`.
- `security/StorefrontLoginEntryPoint implements AuthenticationEntryPoint` (needs `RequestCache`).
- `security/StorefrontLoginFailureHandler implements AuthenticationFailureHandler` (ctor: `invalid` | `social`).
- `security/StorefrontLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler`, overriding
  `determineTargetUrl` → `StorefrontUrls.loginPage(req, cache, false, null)`.
- `web/PublicRegistrationController` (`@RestController`, `POST /api/v1/public/registration`,
  `@Valid @RequestBody RegistrationRequest`, `StoreMerchantId merchantStore`, `LanguageCode language`,
  `throws DuplicateUsernameException, DuplicateEmailException`, returns 201).
- `web/PublicSocialLoginController` (`GET /api/v1/public/social-logins`, `StoreMerchantId merchantStore`).
- `web/dto/ReadableSocialLogin(providerId, name, registrationId)` built from `SocialLoginConfigId.toRegistrationId()`
  and `SocialProvider`; `SocialLoginConfigService.enabledLogins(StoreMerchantId)` over the existing repository query.

Changed:
- `web/dto/RegistrationRequest`: drop `clientId`; `service/UserService.registerUser(StoreMerchantId, RegistrationRequest)`.
  `RegistrationController` passes `new StoreMerchantId(clientId)` until PR 2 deletes it.
- `config/AppSecurityConfig`: add `@Order(1) publicApiSecurity` (`securityMatcher("/api/v1/public/**")`,
  `permitAll`, csrf off, `STATELESS`); register the success/failure handlers on `formLogin` and `oauth2Login`
  (keep `loginPage("/login")`: it suppresses Spring's generated page and fixes the processing URL).
- `store-pod/cua/http/public-registration-controller.http`, `http/public-social-login-controller.http`:
  `{{LANDING_UI_URL}}/cua/api/v1/public/...?store={{STORE_ID}}&lang={{LANG}}`; blocks for 201, 409, 400, cross-store
  same username → 201 (isolation), social list for two stores. Delete `req.http`.

Tests:
- `src/test`: `security/StorefrontUrlsTest`, `StorefrontLoginEntryPointTest`, `StorefrontLoginFailureHandlerTest`,
  `StorefrontLoginSuccessHandlerTest` (Mock servlet request/response + `HttpSessionRequestCache`),
  `service/UserServiceTest` (both duplicates typed, password encoded, store stamped).
- `src/integrationTest`: `web/PublicRegistrationControllerIntegrationTest` (201 / 409×2 / 400 fieldErrors /
  cross-store 201), `web/PublicSocialLoginControllerIntegrationTest` (empty, enabled, isolation, no secret in body).

### landing-ui (`store-pod/landing-ui/`)

- `libs/types/src/social-login.ts`, `registration.ts`, exported from `index.ts`.
- `libs/services/src/auth-service.ts`: `login(ctx, opts?: {returnTo?})`, `register(ctx, body)` (must-fail),
  `socialLogins(ctx)`, `loginAction(ctx)` = `${storeBaseServiceUrl('cua', ctx)}/login`.
- `libs/hooks/src/use-register-form.ts`: values, `submitting`, `fieldErrors` from `ApiError.fieldErrors`, `error`
  via `useErrorMessage`, `submit()` → register → `login(ctx, {returnTo})`.
- `libs/theme/src/contract.ts`: `LoginError`, `LoginData {action, clientId, lang, error?, socialLogins}`,
  `RegisterData`, `ThemePages.Login?` and `Register?` **optional**, same policy as `Search`; comment in
  `define-theme.ts`; a `define-theme.test.ts` case proving a theme without them still validates.
- Shell fallbacks: `storefront/src/shell/theme/default-login-page.tsx` (client component, `@store-front/ui`
  `Input/Label/Button/Card`, plain `<form method="post" action={data.action}>` with hidden `client_id` and `lang`,
  error banner from `data.error`, social anchors, link to `/register`) and `default-register-page.tsx`.
- `storefront/src/app/(storefront)/[locale]/login/page.tsx`: no `auth` param → `<LoginRedirect/>` (today's
  behaviour, keeps deep links and `Secured` working); `auth=1` → `loadPageContext()`,
  `orUndefined(AuthService.socialLogins(ctx))`, render `theme.pages.Login ?? DefaultLoginPage`. `noindex`.
- New `[locale]/register/page.tsx`: always renders `theme.pages.Register ?? DefaultRegisterPage`; `noindex`;
  confirm `sitemap.ts` does not enumerate it.
- `themes/starter/src/pages/Login.tsx`, `Register.tsx`, `sections/LoginForm.tsx`, `RegisterForm.tsx`, registered
  in `themes/starter/src/index.ts`. **Starter only**; the other 11 themes get the token-only fallback.
  Update `scripts/new-theme.mjs` text and `references/new-landing-ui-template.md` (optional pages: Search, Login, Register).
- i18n in **all five** `locales/*.json`: `PAGE.LOGIN.*`, `PAGE.REGISTER.*` ported from cua's `messages_*.properties`,
  plus `ERRORS.CODE.CUA_REGISTRATION_USERNAME_TAKEN` / `EMAIL_TAKEN`.
- Themes' `HeaderActions` unchanged (still call `login()`).

## PR 2: switch the hand-off, delete the old UI

- `config/AuthorizationServerConfig`: `new LoginUrlAuthenticationEntryPoint("/login")` →
  injected `StorefrontLoginEntryPoint`.
- `config/AppSecurityConfig`: `.exceptionHandling(ex -> ex.authenticationEntryPoint(storefrontLoginEntryPoint))`;
  drop `/register` from `permitAll`; keep `/error` permitAll and update its comment. Optional 3-line
  `GET /login` → storefront redirect for stale bookmarks.
- Delete: `templates/*.html`, six `messages*.properties`, `web/LoginController`, `web/RegistrationController`,
  `web/StoreLogoResolver`, `config/ClientsConfig`, `config/CachedExternalMerchantStoreService`, `config/LocalConfig`,
  `integrationTest/.../ExternalClientsTestConfiguration`, `web/AuthPagesIntegrationTest`.
  `build.gradle`: remove `spring.boot.starter.thymeleaf`, `thymeleaf.extras.springsecurity6`,
  `spring.boot.starter.thymeleaf.test`, `merchant-external-api`, `content-external-api` (uaa still uses the
  thymeleaf catalog entries; leave `libs.versions.toml`).
- Replace the integration test with `web/LoginHandoffIntegrationTest` (`@DatabaseIntegrationTest`, no external
  stubs): authorize → `302 http://localhost:{port}/en/login?auth=1` plus `SESSION` cookie; bad password →
  `?auth=1&error=invalid`; `user`/`revo`/`client_id=65f023632bc46470c104b76f` → 302 saved authorize → follow →
  `302 …/en/callback?code=`; POST without session → `…/en/login`; wrong `client_id` (other store) → `error=invalid`
  (tenant isolation).
- landing-ui: delete `storefront/src/app/css/login.css/route.ts`, `shell/auth/login-default-css.ts`,
  `ThemeDefinition.loginCss`.
- Docs: `lcl.yml` ~line 141 comment; `.claude/skills/project-structure/SKILL.md` (cua row → `BE`, controllers;
  frontend pattern 3 → uaa only); `references/authentication.md`, `frontends.md`, `landing-ui.md`,
  `http-request-files.md` (drop `req.http` from the stale list); `cua-qa.md` §99 and LGN-01/04/05, CLI-01.

**Deploy order:** PR 2 must not reach a pod before PR 1's landing-ui is deployed there, or shoppers land on a
redirect-only `/login` that restarts authorize in a loop.

## QA

`store-pod/cua/qa/cua-qa.md`: LGN-06 hand-off 302 carries the port on a shifted stack; LGN-07 wrong password →
`?auth=1&error=invalid`, re-submit works; LGN-08 social buttons absent without marker, provider redirect works with
it; LGN-09 registration 201/409/400 via `.http`, then login; SOC-04 public social-logins per store (isolation, no
secrets); CLI-05 `Set-Cookie: SESSION; Path=/cua/; SameSite=Lax`.

`store-pod/landing-ui/qa/landing-ui-qa.md`, new `AUTH` section: themed login on `starter` vs fallback on another
theme (`?theme=`); deep link `/en/customer` still redirects and returns; register → auto login → home; error banner
per `error=`; RTL `/ar/login?auth=1`; all five locales; custom-domain host in the hand-off URL; second store cannot
log in with the first store's user (403/`error=invalid`).

## Verification gates

- `./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest`, `./gradlew build -x test -x check`,
  `./gradlew :store-pod:cua:test`, `./gradlew :store-pod:cua:integrationTest` (Docker).
- landing-ui: `npm run typecheck`, `npm run lint`, `npm run build` from `store-pod/landing-ui`, `npm test --workspace=libs/theme`.
- End to end on `lcl start -d --stack cua-login`: `http://org1-store1.spg-507f1f77.gateway.com/en` → Login →
  themed page → `user`/`revo` → back on the storefront signed in; repeat with wrong password, with a second store,
  with `?theme=` on a fallback theme, and the register flow. Confirm the `SESSION` cookie in DevTools.

## Risks

- Cookie path `/cua/` derives from `PathPrefixFilter`'s wrapper; if a filter reorder moved it to `/` nothing
  breaks. `Secure` follows `X-Forwarded-Proto`.
- Origin derivation is the one already trusted for `redirect_uri`; open-redirect exposure is unchanged.
- `prompt=login` plus the no-SavedRequest fallback means a second password entry in the expired-session edge
  case only. Follow-up: reconsider `prompt=login` now that `/connect/logout` ends the cua session anyway.
- `lang` path injection guarded by `LanguageCode.isLanguage()`.

## Phase 3 (same PR): the two gaps QA surfaced

- **`prompt=login` enforced.** Spring Authorization Server answered it from a live session, so a shopper signed in
  as A who registered B was handed to the callback as A. `security/PromptLoginFilter`, in the authorization-server
  chain after `SecurityContextHolderFilter`, logs a signed-in `prompt=login` authorize out (session invalidated)
  and marks the fresh session; the resumed request passes once. Without `prompt=login` a live session stays
  single sign-on.
- **CSRF on the form, without JavaScript.** `CookieCsrfTokenRepository` (cookie path `/`, readable) with the plain
  `CsrfTokenRequestAttributeHandler`; `StorefrontLoginEntryPoint` and `GET /login` plant the `XSRF-TOKEN` cookie
  on the redirect; the storefront page reads the cookie server-side (`cookies()`) and renders `_csrf` as a hidden
  input; `StorefrontCsrfDeniedHandler` sends a stale form back as `error=expired` with a fresh cookie.

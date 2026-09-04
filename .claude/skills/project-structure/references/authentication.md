# Authentication — two authorization servers

cvhome runs **two separate OAuth2 authorization servers**, because it has two populations of users that must
never share an identity realm.

| | `uaa` (`store-core/uaa`) | `cua` (`store-pod/cua`) |
|---|---|---|
| Layer | Platform (`store-core`) | Tenant pod (`store-pod`) |
| Port | 8001 | 8124 |
| Who it authenticates | Platform staff, org owners, merchants/sellers | **Storefront shoppers** |
| Reached via | `store-core-gateway` (:8000) | `spg` at `/cua` |
| Front end | Embedded Angular SPA (`uaa-fe`) — the identity-first sign-in page and the admin console | **None** — headless. The storefront (`landing-ui`) renders login and registration as theme pages; cua redirects to them and processes the posted form |
| Self-registration | No — admin-provisioned or **invited**: the account is created pending and its owner sets a password through a one-time link | **Yes** — `POST /api/v1/public/registration` (JSON, `?store=`), social login |
| External providers | OIDC and OAuth 2.0 brokering, per-realm, with linking policy and just-in-time provisioning | Social login per store |
| Serves tokens to | console-ui, tenancy, gateway, all `-service` s2s clients | landing-ui storefront sessions |
| Deployment | One shared instance for the whole SaaS | One per pod |

Both use `spring-boot-starter-oauth2-authorization-server` with JDBC sessions, so they look nearly identical in
code — the difference is *which realm they own*, not how they're built.

**Practical consequence:** when you see an auth bug, first establish whether the actor is a seller/admin
(→ `uaa`) or a shopper (→ `cua`). They have separate user tables, separate clients, separate issuers.

## "Realm" means two different things — keep them apart

The word is overloaded in this repo, and the two senses sit one layer apart:

| Term | Where | Means | Values |
|---|---|---|---|
| **Issuer realm** | `store-commons/autoconfigure/.../s2s/jwt/IssuerRealm.java` | *Which authorization server minted this token.* What a resource server's trust list is written against, and what the `grants` ceiling and the `REALM_*` authority key on. | `uaa`, `cua` |
| **Realm** | `store-commons/sso/sso-commons/.../RealmId.java` | *Which user pool a principal belongs to, inside one server.* What `users`, `roles`, `settings`, `identity_providers` and `audit_events` are scoped by. | `platform` (uaa) · one per store (cua) |

They are independent: every realm lives inside exactly one issuer realm. uaa has one realm and always will —
its staff and service accounts are a single pool, and there is no realm selector in its API or console. cua has
one realm per store, which is what makes the same email address two different shoppers in two different stores.

If you are reading `IssuerRegistry`, `MultiIssuerJwtDecoder` or `RealmAwareJwtGrantedAuthoritiesConverter`, the
realm is the *server*. If you are reading anything under `sso-core`, it is the *user pool*.

## Pod services accept tokens from both

A pod service is an OAuth2 **resource server** that must honour a seller token *and* a shopper token *and* an
internal service token. That's what `MultiIssuerJwtDecoder` is for. The accepted issuers are configured in
`store-commons/autoconfigure/src/main/resources/store-pod-lcl-config.yml` (and its `-fargate` twin):

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuers:
            uaa:                                   # staff and service principals
              uris:
                - ${...services.uaa.schema}://${...services.uaa.domain}
                - ${...services.uaa.schema}://${...services.uaa.domain}:${...services.uaa.port}
              jwk-set-uri: ${...services.uaa.schema}://${...services.uaa.domain}:${...services.uaa.port}/oauth2/jwks
            cua:                                   # shoppers
              uris:
                - ${...services.spg.schema}://${...services.spg.domain}/cua
                - ${...services.spg.schema}://${...services.spg.domain}:${...services.spg.port}/cua
              jwk-set-uri: ${...services.spg.schema}://${...services.spg.domain}:${...services.spg.port}/cua/oauth2/jwks
              grants:
                - ROLE_CUSTOMER
                - SCOPE_OPENID
```

Issuers are keyed by **realm**, not listed flat, because a realm is not a URL: the same server answers on
several equivalent forms (with or without a default port, a shifted port under a named stack, a path prefix).
Every `uris` entry is matched **normalized** — `UrlNormalize` drops `:80` under http and `:443` under https —
so an operator-entered pod endpoint that happens to carry an explicit default port still matches. Listing both
port forms is belt and braces rather than load-bearing.

`grants` is the realm's authority ceiling, applied after claim parsing. It is what makes a cua token
structurally incapable of granting staff authority: both authorization servers write their roles into the same
`roles` claim, so without it a shopper token claiming `ORG_ADMIN` would have been granted it. The staff realm
declares no `grants` because its clients carry arbitrary scopes. Every principal also gains a `REALM_<name>`
authority, which `StoreRoleAccessChecker` uses to refuse a staff check for a shopper principal and vice versa.

`jwk-set-uri` is preferred over OIDC discovery: discovery costs a blocking network call on the first
authenticated request, its failure is not cached, and `withIssuerLocation` asserts the discovered `issuer`
string equals the location requested — too literal for a realm reachable at several equivalent URIs. Discovery
remains the fallback when a realm declares no `jwk-set-uri`.

The `cua` issuer is the **spg-fronted** URL with the `/cua` prefix — which is exactly why `spg`'s Caddyfile uses
`handle /cua*` (prefix preserved, `X-Forwarded-Prefix: /cua`) rather than `handle_path` like every other route.
Strip that prefix and OAuth2 issuer validation breaks. cua pins that issuer from `pod-info.pod.endpoint.endpoint`
and **refuses to start without it**: unpinned, Spring Authorization Server derives the issuer from the request
host, which is the shopper's storefront host — a per-store subdomain or an arbitrary `CUSTOM_DOMAIN`, a set no
trust list can enumerate.

Supporting classes in `store-commons:autoconfigure` (`com.asrevo.cvhome.s2s.*`):

- `jwt/MultiIssuerJwtDecoder`, `jwt/MultiIssuerReactiveJwtDecoder` — issuer-aware decoding (servlet + reactive;
  the gateway is WebFlux, everything else is MVC)
- `jwt/IssuerRealm`, `jwt/IssuerRegistry` — the realms and the trust decision (normalized issuer matching; an
  untrusted, unparseable or issuer-less token fails as `BadJwtException`, which Spring maps to 401 — a bare
  `JwtException` becomes an `AuthenticationServiceException` and escapes the filter chain as a 500)
- `jwt/IssuerRealmProperties`, `config/internal/IssuerRealmsCondition`, `IssuerRegistryConfiguration`,
  `MultiIssuerJwtDecoderConfiguration`, `MultiIssuerReactiveJwtDecoderConfiguration` — bind and activate the
  realms above
- `jwt/RealmIssuerValidator` — post-decode `iss` check against the realm's URIs, compared normalized
- `jwt/UaaJwtGrantedAuthoritiesConverter` — claims → Spring authorities
- `jwt/RealmAwareJwtGrantedAuthoritiesConverter` — the same, capped at the issuing realm's `grants`, registered
  once for every service by `config/internal/JwtAuthenticationConverterConfiguration`
- `services/{PermissionAccessChecker, StoreRoleAccessChecker, StoreSecurityService, StoreOrgOwnerRetriever}`,
  `config/internal/{ServletPermissionConfig, CustomPermissionEvaluator}` — tenant-aware authorization. This is
  what backs `@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")` on
  controllers; the permission-string catalogue and how roles vs. scopes are resolved is in
  `api-conventions.md`.
- `utils/SecurityUtils`, `utils/RedirectionUrlBuilder`

## Service-to-service authentication

Every service registers a `client_credentials` client named `s2s` against `uaa` in its own `application.yml`.
Two scopes exist, one per layer:

`store-core/gateway/gateway-service/application.yml`:
```yaml
spring.security.oauth2.client.registration.s2s:
  provider: uaa
  client-id: store-core@service.store-core.internal
  authorization-grant-type: client_credentials
  scope: store_core
```

`store-pod/catalog/catalog-service/application.yml`:
```yaml
spring.security.oauth2.client.registration.s2s:
  provider: uaa
  client-id: store-pod-507f1f77@service.store-pod.internal
  authorization-grant-type: client_credentials
  scope: store_pod
```

The pod client id embeds the **pod id** (`store-pod-507f1f77@…`), so tokens are attributable to a specific pod.
This `s2s` registration is what authenticates the declarative HTTP clients described in
`service-to-service.md`.

Note: `uaa` itself issues these, and it is also the identity source for the `uaa-client` /
`uaa-client-impl` admin SDK (`AdminUserClient`, `AdminClientClient`, `OAuth2TokenManager`) that services use to
manage users and clients programmatically.

> The client secrets checked into `application.yml` are local-development values, overridden per environment.

## Browser login flows

**Seller/admin (store-core):** the browser hits `store-core-gateway`, which is an OAuth2 **client**. It runs the
authorization-code flow against `uaa`, holds the session, and forwards authenticated requests onward. That is
why `console-ui`'s `environment.ts` sets `loginUrl: '/oauth2/authorization/uaa'` and `apiUrl: ''` — the Angular
app holds no tokens and makes same-origin relative calls. Relevant gateway classes: `SecurityConfig`,
`CapturingServerOAuth2AuthorizationRequestResolver`, `RedirectingServerAuthenticationSuccessHandler`,
`AuthController`, `LogoutController`, plus `security/UaaLogoutSuccessHandler` and
`security/SecurityContextServerLogoutHandler` in `autoconfigure`.

**The whole visible flow happens on the console's own origin, and the console renders the form.** uaa is
reached through the gateway's `/uaa` forward rather than at its own address:

```
gateway.com:8000/oauth2/authorization/uaa   the gateway starts the flow
gateway.com:8000/uaa/oauth2/authorize       uaa saves the request, plants XSRF-TOKEN, redirects
gateway.com:8000/sign-in?auth=1             console-ui renders the form
gateway.com:8000/uaa/login       (POST)     uaa authenticates, resumes the saved request
gateway.com:8000/login/oauth2/code/uaa      the gateway takes the code
```

Three things make that work, and each fails quietly if it is undone:

- **The `/uaa` route does not strip its prefix.** uaa reads `X-Forwarded-Prefix` in `PathPrefixFilter` and
  reports it as its context path; Spring requires the context path to be the literal start of the request path,
  so stripping and then announcing it is rejected as `Invalid contextPath '/uaa'` and dispatched to `/error` —
  a 500 on every browser hop. spg forwards cua's path intact for exactly the same reason.
- **The session cookie is `Path=/uaa` on the console's origin.** It is what carries the saved authorize request
  across the form POST, and it is why the flow must not go cross-origin. `XSRF-TOKEN` stays at `Path=/` so the
  console can read it and echo it as `_csrf`; uaa's `CsrfTokenRequestAttributeHandler` is the plain one, not the
  XOR one, precisely so a page uaa never rendered can fill in a token equal to the cookie.
- **Logout ends two sessions.** The gateway's, and uaa's — and uaa's now lives on this origin, so
  `UaaLogoutSuccessHandler` rebuilds the end-session URL as `/uaa/connect/logout` here
  (`com.asrevo.cvhome.gateway.uaa-path-prefix`). Sent to uaa's own host it carries no cookie, ends nothing, and
  the next navigation signs straight back in.

**uaa keeps its own door.** Reached at `uaa.gateway.com:8001` the context path is empty, `ConsoleUrls.isHandoff`
is false, and uaa serves its own embedded SPA — sign-in page included. That is how a platform administrator
signs in to administer uaa itself, and it is deliberately the one thing the hand-off must never swallow. The
switch is the context path and nothing a caller can put in a query string.

`LinkAccept` in `@cvhome-saas/ui-kit/uaa` serves the invitation and password-reset pages for **both** consoles;
`com.asrevo.cvhome.uaa.links.base-url` decides which origin the emailed link points at.

**Shopper (store-pod):** the storefront (`landing-ui`) is a PKCE public client whose `client_id` is the store
id. `AuthService.login()` sends the browser to `/cua/oauth2/authorize?…&lang=<locale>`; cua saves that request
and its `StorefrontLoginEntryPoint` redirects to `{origin}/{lang}/login?auth=1` — same origin, because spg
fronts both. landing-ui renders `theme.pages.Login` (or the shell fallback) as a plain HTML form that posts
`username`, `password`, `client_id`, `lang` to `/cua/login`; on success `StorefrontLoginSuccessHandler` resumes
the saved authorize request, which redirects to `/{lang}/callback?code=`, where the storefront exchanges the
code. A failure is `…/login?auth=1&error=invalid|social` — a token, never text: cua has no strings, the
storefront translates. Without the `auth=1` marker `/login` just starts the flow, which is what deep links and
`shell/auth/secured.tsx` rely on. Registration is `POST /cua/api/v1/public/registration` from
`theme.pages.Register` (`useRegisterForm`), then the same login flow. The helpers live in
`store-pod/cua/.../security/StorefrontUrls.java`. The form is CSRF-protected without JavaScript: the hand-off
redirect plants an `XSRF-TOKEN` cookie (path `/`), the storefront page reads it server-side and echoes it as a
hidden `_csrf` input, and a stale form comes back as `error=expired`. `prompt=login` is enforced by
`PromptLoginFilter` — a live cua session is logged out and sent to the form, once — so registering while another
shopper's session is alive signs in as the new account.

Each `-service` also has its own small `controller/v1/auth/AuthController` for exposing the current principal to
its own clients.

## Managing users (as opposed to authenticating them)

Creating, listing, enabling, inviting or role-assigning a **staff/seller** account means calling `uaa`'s admin
API, and that goes through the `store-commons:uaa-client` / `uaa-client-impl` SDK (`UserAccountService` →
`AdminUserClient` → `/api/v1/admin/users`), authenticated by its own `admin-sdk` `client_credentials` token with
scope `super_admin` — not the `s2s` registration. The contract covers the whole lifecycle, including
`search(...)`, `counts()`, `invite(...)` and `createResetLink(...)`; a one-time link is answered once and must
never be logged. Because that scope is platform-wide, the caller is responsible
for tenant scoping via the `org`/`store` user metadata. Full guide: `uaa-client.md`.

## Related

- Per-environment issuer/port values: `configuration.md`
- How tokens ride along on inter-service calls: `service-to-service.md`
- Why `spg` treats `/cua` specially: `store-pod.md`

# Authentication — two authorization servers

cvhome runs **two separate OAuth2 authorization servers**, because it has two populations of users that must
never share an identity realm.

| | `uaa` (`store-core/uaa`) | `cua` (`store-pod/cua`) |
|---|---|---|
| Layer | Platform (`store-core`) | Tenant pod (`store-pod`) |
| Port | 8001 | 8124 |
| Who it authenticates | Platform staff, org owners, merchants/sellers | **Storefront shoppers** |
| Reached via | `store-core-gateway` (:8000) | `spg` at `/cua` |
| Front end | Thymeleaf login + embedded Angular admin SPA (`uaa-fe`) | Thymeleaf login/registration/social-login pages |
| Self-registration | No — admin-provisioned (`AdminUserController`) | **Yes** — `RegistrationController`, social login |
| Serves tokens to | seller-ui, control-plane, gateway, all `-service` s2s clients | landing-ui storefront sessions |
| Deployment | One shared instance for the whole SaaS | One per pod |

Both use `spring-boot-starter-oauth2-authorization-server` with JDBC sessions, so they look nearly identical in
code — the difference is *which realm they own*, not how they're built.

**Practical consequence:** when you see an auth bug, first establish whether the actor is a seller/admin
(→ `uaa`) or a shopper (→ `cua`). They have separate user tables, separate clients, separate issuers.

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
          issuer-uri-set:
            - ${...services.uaa.schema}://${...services.uaa.domain}
            - ${...services.uaa.schema}://${...services.uaa.domain}:${...services.uaa.port}
            - ${...services.spg.schema}://${...services.spg.domain}/cua
            - ${...services.spg.schema}://${...services.spg.domain}:${...services.spg.port}/cua
```

Four entries = two issuers × (with port / without port), because the same service is reachable both ways
depending on environment. The `cua` issuer is the **spg-fronted** URL with the `/cua` prefix — which is exactly
why `spg`'s Caddyfile uses `handle /cua*` (prefix preserved, `X-Forwarded-Prefix: /cua`) rather than
`handle_path` like every other route. Strip that prefix and OAuth2 issuer validation breaks.

Supporting classes in `store-commons:autoconfigure` (`com.asrevo.cvhome.s2s.*`):

- `jwt/MultiIssuerJwtDecoder`, `jwt/MultiIssuerReactiveJwtDecoder` — issuer-aware decoding (servlet + reactive;
  the gateway is WebFlux, everything else is MVC)
- `jwt/IssuerUriSetConfigrationProperties`, `config/internal/IssuerUriSetCondition`,
  `IssuerUriSetJwtDecoderConfiguration`, `IssuerUriSetReactiveJwtDecoderConfiguration` — bind and activate the
  set above
- `jwt/UaaJwtGrantedAuthoritiesConverter` — claims → Spring authorities
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
why `seller-ui`'s `environment.ts` sets `LOGIN_URL: "/oauth2/authorization/uaa"` and `apiUrl: ''` — the Angular
app holds no tokens and makes same-origin relative calls. Relevant gateway classes: `SecurityConfig`,
`CapturingServerOAuth2AuthorizationRequestResolver`, `RedirectingServerAuthenticationSuccessHandler`,
`AuthController`, `LogoutController`, plus `security/UaaLogoutSuccessHandler` and
`security/SecurityContextServerLogoutHandler` in `autoconfigure`.

**Shopper (store-pod):** the storefront (`landing-ui`) sends the shopper to `cua` through `spg` at `/cua`, using
`@store-front/services`' `AuthService`; the template's `callback/` route completes the flow and
`Common/Secured.tsx` + `useUser` guard `/customer/**` pages.

Each `-service` also has its own small `controller/v1/auth/AuthController` for exposing the current principal to
its own clients.

## Managing users (as opposed to authenticating them)

Creating, listing, enabling or role-assigning a **staff/seller** account means calling `uaa`'s admin API, and
that goes through the `store-commons:uaa-client` / `uaa-client-impl` SDK (`UserAccountService` →
`AdminUserClient` → `/api/v1/admin/users`), authenticated by its own `admin-sdk` `client_credentials` token with
scope `super_admin` — not the `s2s` registration. Because that scope is platform-wide, the caller is responsible
for tenant scoping via the `org`/`store` user metadata. Full guide: `uaa-client.md`.

## Related

- Per-environment issuer/port values: `configuration.md`
- How tokens ride along on inter-service calls: `service-to-service.md`
- Why `spg` treats `/cua` specially: `store-pod.md`

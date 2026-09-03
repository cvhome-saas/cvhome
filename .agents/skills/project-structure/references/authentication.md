# Authentication — two authorization servers

cvhome runs **two separate OAuth2 authorization servers**, because it has two populations of users that must
never share an identity realm.

| | `uaa` (`store-core/uaa`) | `cua` (`store-pod/cua`) |
|---|---|---|
| Layer | Platform (`store-core`) | Tenant pod (`store-pod`) |
| Port | 8001 | 8124 |
| Who it authenticates | Platform staff, org owners, merchants/sellers | **Storefront shoppers** |
| Reached via | `store-core-gateway` (:8000) | `spg` at `/cua` |
| Front end | Embedded Angular SPA (`uaa-fe`, on `@cvhome-saas/ui-kit`): sign-in, users, roles, clients — plus a Thymeleaf consent page | **None — headless.** The storefront renders login and registration as theme pages and hands off to `/oauth2/authorize` |
| User pools | One, fixed | **One per store** (see *Realms* below) |
| Self-registration | No — admin-provisioned (`AdminUserController`) | **Yes** — `RegistrationController`, social login |
| Serves tokens to | console-ui, tenancy, gateway, all `-service` s2s clients | landing-ui storefront sessions |
| Deployment | One shared instance for the whole SaaS | One per pod |

**They are the same program.** Both are thin shells over `store-commons/sso/sso-core`
(`shared-libraries.md`), which holds every entity, service and security component; a shell supplies only its
application class, its issuer pin, its realm resolver and its edge. They were separate codebases once, and the
hardening that landed in uaa never reached the server that authenticates shoppers — lockout, rate limiting,
audit, key rotation, a persistent token store. Anything added now lands in both by construction.

**Practical consequence:** when you see an auth bug, first establish whether the actor is a seller/admin
(→ `uaa`) or a shopper (→ `cua`). They have separate user tables, separate clients, separate issuers.

## Two things are called realm

They are unrelated, and mistaking one for the other is how this gets misread:

| | Answers | Named by | Where |
|---|---|---|---|
| **Issuer realm** | *Which server minted this token, and may I trust it?* | `uaa`, `cua` | `s2s/jwt/IssuerRealm`, `store-pod-*-config.yml` |
| **User realm** | *Whose users is this token about?* | a store id, or `platform` | `commons/domain/RealmId`, `sso/realm/*` |

A shopper token is issued by the **cua issuer realm** and belongs to the **user realm** of one store. Read the
rest of this file with that split in mind: everything above this line is about issuers, everything below about
user pools.

## Realms — one user pool per store

uaa serves one realm forever (`platform`). cua serves **one realm per store on its pod**, which is what makes
the four things cua exists for possible:

- Each store has its own users. The same email in two stores is two accounts, with their own passwords,
  their own lockout state and their own sessions.
- Each store configures its own identity providers, with its own Google or GitHub credentials.
- The endpoints are reached same-origin on the shopper's own store domain…
- …while the **issuer stays pinned to one value per pod**, so a resource server's trust list is bounded by pod
  count rather than store count. A per-store issuer would be an unbounded set no trust list could enumerate.

`RealmMode` picks which: `SINGLE` for uaa, `MULTI` for cua, stated explicitly in each shell's `application.yml`
because a deployment that forgot to say and defaulted to `SINGLE` would put every store's shoppers in one pool.

**Isolation is the ORM's job, not the query author's.** Every `sso-core` entity carries Hibernate's `@TenantId`,
and `SsoTenantIdentifierResolver` supplies the realm — so every query is filtered and every insert populated
without a `where realm_id = ?` being written by hand. Unique constraints are realm-scoped
(`(realm_id, lower(username))`), which is what lets one shopper exist in two stores.

**Where the realm comes from.** The pod edge is authoritative: Caddy's `domain_lookup` resolves the storefront
host to a store and injects `Store-Id`, and cua's realm filter reads that. A `client_id` form field or a
`?store=` parameter is checked *against* it, never trusted as the source — letting the client name its own
tenant is what the header replaced.

**A signed-in session belongs to one realm.** It is stamped at sign-in and checked on every request
(`SessionRealmFilter`); a mismatch refuses the request and leaves the session standing, because a session anybody
can destroy by naming another store in a query parameter is a forced-logout button. Anonymous sessions — the one
`/oauth2/authorize` creates to hold the saved request — carry no stamp and are not checked. This is the second
lock: the first is that the cookie is host-scoped, which is why a `Domain` on the shared parent
(`.spg-<pod>.gateway.com`) must never be set.

**Background work has no request, so it has no realm.** `RealmContext.runIn` is how a scheduled job enters one,
and jobs that sweep every realm (audit retention) iterate `RealmRegistry.all()`. Outside a realm the resolver
answers a sentinel that matches nothing, so a mistake reads no rows rather than every realm's.

### What is the realm's, and what is the platform's

A merchant edits their own realm's policy — password rules, lockout, session and token lifetimes, audit
retention — and the pod underneath is shared, so `SsoPlatformCeilings` bounds what they may set. It is
configuration, returned by no API and written by no endpoint; a merchant sees only that a value was refused.

The signing key is **one per deployment**, which on cua means one per pod. Its rotation interval comes from the
platform realm's settings, never from whichever realm a background thread happened to be in. State this plainly
in merchant-facing material: "your own SSO" does not mean cryptographic isolation, and a merchant who genuinely
requires it needs a dedicated pod.

**Merchant-supplied endpoints are fetched by this server**, so they are guarded rather than trusted. Every URL on
an identity provider — issuer, authorization, token, userinfo, JWKS — is checked before it is stored and again
before the `test` action fetches one: HTTPS only, no credentials in the URL, and no name that resolves inside the
server's own network (loopback, RFC1918, link-local, unique-local, carrier-grade NAT, and the cloud metadata
address). A name that does not resolve is refused rather than allowed, because an address that cannot be checked
has not been. `test` is rationed per realm, since an unlimited one is a port scanner with a progress bar.

The gap worth naming: between the check and the socket, the name is resolved again by the HTTP client, and
nothing pins those two answers together. Every static case is closed; what remains needs an attacker who runs a
DNS server and wins a race. Closing it means owning the connection factory.

`allow-private-addresses` is the one way to lose this, and it is off by default — set only by the `lcl` slices and
by the integration test whose stub provider answers on localhost. `EgressGuardTest` holds the defaults to being
the strict ones.

Rate limiting counts an attempt twice — once against the realm it was aimed at, once against the address it
came from (at `spread` times the limit) — so one store cannot spend another's budget and spraying a thousand
stores does not evade the brake.

### Claims a realm-scoped token carries

| Claim | Value |
|---|---|
| `sub` | the account id |
| `uid` | the same account id — a user token always carries it, a `client_credentials` token never does |
| `realm` | the user realm: the store id |
| `username` / `preferred_username` | the human name, which is unique only within the realm |

`StoreRoleAccessChecker.isStoreCustomer` and checkout's shopper gate both read `realm`. They read `clientId`
once, which held the same value only because a store had exactly one client — a coincidence that would have
stopped being true the first time a store got a second one.

**The principal name is the account id, not the username.** Spring Session's `PRINCIPAL_NAME` index and
`oauth2_authorization.principal_name` are both looked up by it, and one deployment holds every realm's rows: two
shoppers called `user` in two stores shared a principal name, so each could list and end the other's sessions.
`PrincipalNames` is what turns it back into a username for audit rows and lockout counters.

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

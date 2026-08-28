# Fix: uaa admin API answers 403 to the tenancy S2S client (`COMMON.REMOTE_UNAVAILABLE`, remoteStatus 403)

## Context

`GET /tenancy/api/v1/user-account/list` fails with `COMMON.REMOTE_UNAVAILABLE` because uaa answers **403** to
`/api/v1/admin/users`. The suspicion was an expired token held by tenancy. It is not: the token manager
(`store-commons/uaa-client-impl/.../OAuth2TokenManager.java`) refreshes correctly (client_credentials, `scope=super_admin`,
re-fetch 60 s before `expires_in`), and an invalid/expired JWT would produce **401**, not 403. 403 means uaa authenticated
the token but found no matching authority.

The actual cause is commit `047336a8f` (today, "fix(gateway,uaa): route /uaa/** and read uaa's own roles claim"):
`store-core/uaa/.../config/AppSecurityConfig.java` now registers `UaaJwtGrantedAuthoritiesConverter`
(`store-commons/autoconfigure/.../s2s/jwt/UaaJwtGrantedAuthoritiesConverter.java`). That converter **upper-cases** every
scope → the S2S token yields `SCOPE_SUPER_ADMIN`, while uaa still checks the lowercase `SCOPE_super_admin`:

- `AppSecurityConfig.java:44` — `.hasAnyAuthority("SCOPE_super_admin", "ROLE_SUPER_ADMIN")`
- `AdminUserController`, `AdminRoleController`, `AdminClientController` — every method:
  `@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")`

So since uaa was rebuilt with that commit, **every** client-credentials call from tenancy (and any other SDK user) is 403.
Restarting tenancy will not help; it "worked before" only while uaa was still running the pre-commit build (default
converter kept scopes lowercase).

## Fix (uaa only)

Align uaa's authority checks with the platform-wide uppercase convention (cf. `Roles.SCOPE_STORE_CORE`,
`SecurityUtils.hasRole`), keeping `UaaJwtGrantedAuthoritiesConverter` untouched (other services depend on its uppercasing).

1. `store-core/uaa/src/main/java/com/asrevo/cvhome/uaa/config/AppSecurityConfig.java`
   - line 44: `hasAnyAuthority("SCOPE_SUPER_ADMIN", "ROLE_SUPER_ADMIN")`
   - update the javadoc that mentions `SCOPE_super_admin`.
2. `store-core/uaa/src/main/java/com/asrevo/cvhome/uaa/web/admin/AdminUserController.java`,
   `AdminRoleController.java`, `AdminClientController.java`
   - replace every `hasAuthority('SCOPE_super_admin')` with `hasAuthority('SCOPE_SUPER_ADMIN')` (mechanical, ~25 sites).
   - Optionally introduce one constant, e.g. `AdminAuthz.SUPER_ADMIN = "hasAuthority('SCOPE_SUPER_ADMIN') or hasRole('SUPER_ADMIN')"`
     and `@PreAuthorize(AdminAuthz.SUPER_ADMIN)`, so this cannot drift again.
3. `store-core/gateway/gateway-service/.../GatewayRouteLocatorImpl.java:57` — comment only, fix `SCOPE_super_admin` → `SCOPE_SUPER_ADMIN`.
4. (Optional hardening, `OAuth2TokenManager`) nothing required; the token flow is correct.

No change to `uaa-client-impl`, tenancy, or the data SQL (`scope super_admin` minted lowercase is fine — the converter normalises).

## Verification

1. Rebuild & restart uaa (`store-core/uaa`), no need to restart tenancy.
2. In the logged-in console, hit
   `http://console-ui.gateway.com:8000/tenancy/api/v1/user-account/list?page=0&count=20&store=65f023632bc46470c104b76f&pod=507f1f77bcf86cd799439011`
   → 200 with a page of users.
3. Direct check: `POST http://<uaa>/oauth2/token` (client_credentials, admin-sdk client, `scope=super_admin`) then
   `GET /api/v1/admin/users` with the bearer → 200 (was 403).
4. Super-admin *user* token via gateway `/uaa/api/v1/admin/users` still 200 (ROLE_SUPER_ADMIN path from the same commit still works).
5. Non-admin user token → still 403.

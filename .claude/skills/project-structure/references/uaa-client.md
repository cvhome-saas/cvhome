# `uaa-client` / `uaa-client-impl` — the UAA admin SDK

How a service **manages users in `uaa`** (create, read, list, enable/disable, roles, passwords) instead of
talking to the authorization server's admin REST API by hand. Note the scope: this is the *management* path.
Authenticating a user, validating a token, or reading the current principal is a different mechanism entirely —
`authentication.md`.

## Two modules, deliberately split

| Module | Contains | Depends on |
|---|---|---|
| `store-commons:uaa-client` | **Contract only**: `UserAccountService` + the `domain/user` DTOs (`PersistableUser`, `ReadableUser`, `ReadableUserList`, `UserPassword`, `UserEntity`) | `store-commons:commons` (compileOnly) |
| `store-commons:uaa-client-impl` | **Implementation**: `UserAccountServiceImpl`, the raw SDK (`AdminUserClient`, `AdminClientClient`, `AbstractAdminClient`, `OAuth2TokenManager`), `sdk/dto/*`, `ApiException` | `uaa-client` |

Same interface/impl split as `-external-api` vs `-service` elsewhere: business code compiles against
`UserAccountService`, and only one `@Configuration` ever touches the concrete client. Today
`control-plane-service` is the sole consumer:

```gradle
implementation project(":store-commons:uaa-client")
implementation project(":store-commons:uaa-client-impl")
```

## Layers

```
your service code
      ↓  UserAccountService            ← uaa-client       (domain DTOs: Persistable/ReadableUser)
      ↓  UserAccountServiceImpl        ← uaa-client-impl  (maps DTO ↔ SDK dto, handles org/store metadata)
      ↓  AdminUserClient               ← uaa-client-impl  (java.net.http, /api/v1/admin/users)
      ↓  OAuth2TokenManager            ← client_credentials token, cached
   uaa :8001  AdminUserController
```

**Note it is plain `java.net.http.HttpClient`, not `@HttpExchange` + `RestClientBuilder`.** This SDK predates /
sits beside the s2s mechanism: it manages its own token instead of using the `s2s` registration, and it targets
`uaa` by absolute URL rather than `lb://uaa`. Don't take it as the template for a *new* cross-service call —
use `service-to-service.md` for that.

## Wiring (the only place the impl is named)

`control-plane-service`'s `UaaClientConfig`:

```java
@Configuration
@EnableConfigurationProperties(UaaClientConfig.UaaClientConfigProperties.class)
public class UaaClientConfig {
    @Bean
    public UserAccountService userAccountService(UaaClientConfigProperties p) {
        return new UserAccountServiceImpl(new AdminUserClient(p.baseUrl(), p.clientId(), p.clientSecret()));
    }

    @ConfigurationProperties(prefix = "com.asrevo.cvhome.uaa.client")
    public record UaaClientConfigProperties(String baseUrl, String clientId, String clientSecret) { }
}
```

with, in `application.yml`:

```yaml
com.asrevo.cvhome.uaa.client:
  client-id: admin-sdk
  client-secret: ...
  base-url: ${com.asrevo.cvhome.services.uaa.schema}://${com.asrevo.cvhome.services.uaa.domain}:${com.asrevo.cvhome.services.uaa.port}
```

The base URL is **interpolated from `common-config.yml`**, so it follows a port change automatically
(`configuration.md`). `admin-sdk` is a distinct OAuth2 client from the service's `s2s` registration.

### Authentication

`OAuth2TokenManager` fetches `grant_type=client_credentials&scope=super_admin` from `{baseUrl}/oauth2/token`,
caches the token and refreshes it 60s before expiry, `synchronized` around the refresh. It posts credentials in
the form body and **falls back to HTTP Basic** if that is rejected. `AbstractAdminClient` then attaches
`Authorization: Bearer …` to every call and throws `ApiException` on any status ≥ 400.

That `super_admin` scope matters: every endpoint on uaa's `AdminUserController` is guarded by

```java
@PreAuthorize("hasAuthority('SCOPE_super_admin') or hasRole('SUPER_ADMIN')")
```

so **this SDK carries platform-wide privileges**. Tenant scoping is *not* enforced by uaa — the caller must do
it (see "Tenant scoping" below).

## The domain-facing API — `UserAccountService`

```java
ReadableUser     createUser(PersistableUser user);
ReadableUser     updateUser(PersistableUser user);
ReadableUser     findOne(String userId);
ReadableUser     current(String id);
ReadableUserList list(Map<String,String> filters, Integer pageNumber, Integer pageSize);
void             deleteUser(String userId);
void             enableUser(String userId);
void             disableUser(String userId);
void             changePassword(String userId, UserPassword request);
Set<String>      getAssignableRoles();
```

`PersistableUser` in, `ReadableUser` out — the same `Readable*`/`Persistable*` convention as the pods. Both
carry `org`, `store`, `userName`, `active`, `roles`.

### Get a user

```java
ReadableUser user = userAccountService.findOne(userId);   // → GET /api/v1/admin/users/{id}
```

### Create a user

```java
PersistableUser user = new PersistableUser();
user.setEmailAddress("owner@example.com");
user.setUserName("owner@example.com");
user.setFirstName("Ada"); user.setLastName("Lovelace");
user.setPassword("…");
user.setRoles(Set.of("STORE_ADMIN"));
user.setOrg(orgId.id().toString());        // ← tenant metadata, see below
user.setStore(storeId.id().toString());
user.setActive(true);

ReadableUser created = userAccountService.createUser(user);
```

`UserAccountServiceImpl.createUser` is **two HTTP calls**: `POST /api/v1/admin/users` then
`PUT /api/v1/admin/users/{id}/reset-password` — uaa's create endpoint does not accept a password. So a failure
between them leaves a user with no password; treat creation as non-atomic.

### Tenant scoping is the caller's job

`org` and `store` are pushed into uaa's free-form **user metadata** (`ORG_KEY`/`STORE_KEY`) on write and read
back out on the way in, and `list(...)` filters on them (`?metadata[org]=…&metadata[store]=…`). uaa itself has
no notion of orgs or pods.

`ManagedUserAccountServiceImpl` in control-plane is the reference for doing this correctly: it stamps
`org`/`store` from the caller's `UserOrgStoreIdentity` on every create **and** update ("ensure the user is not
moved to another org/store via update"), and calls `validateUserAccess(...)` after fetching before any
mutation. `getAssignableRoles()` likewise strips the reserved `USER` and `ORG_ADMIN` roles.

**Never expose `UserAccountService` straight from a controller** — go through a service that applies the
tenant check first, or one org's admin can read and modify another org's users.

## `AdminUserClient` — the raw surface

Base path `{baseUrl}/api/v1/admin/users`. Use it directly only for what `UserAccountService` does not cover
(role assignment, username checks).

| Method | Call |
|---|---|
| `listUsers(metadataFilters, pageRequest)` | `GET /` with `metadata[k]=v`, `page`, `size` |
| `getUser(id)` / `createUser(req)` / `updateUser(id, req)` / `deleteUser(id)` | `GET` / `POST` / `PUT` / `DELETE /{id}` |
| `usernameExist(username)` | `GET /exists?username=` |
| `enableUser(id)` / `disableUser(id)` | `POST /{id}/enable` / `/disable` |
| `resetPassword(id, newPassword)` | `PUT /{id}/reset-password` |
| `assignRoles(id, roles)` / `removeRoles(id, roles)` | `POST /{id}/roles` / `/roles/remove` |
| `getAssignableRoles()` | `GET /assignable-roles` |

`AdminClientClient` is the sibling for **OAuth2 client registrations** — `listClients`, `getClient`,
`createClient`, `updateClient`, `deleteClient`, `resetSecret`, `getOptions`, over `ClientDetails` /
`ClientSummary` / `ClientDetailsSettings` / `ClientDetailsTokens` / `OAuthGrantType` / `ClientAuthMethod` /
`OAuth2TokenFormat`. No `UserAccountService`-style facade exists over it; construct it the same way
(`new AdminClientClient(baseUrl, clientId, clientSecret)`).

## Using it from another service — checklist

1. `implementation project(":store-commons:uaa-client")` + `":store-commons:uaa-client-impl"`.
2. Copy the `UaaClientConfig` bean and the `com.asrevo.cvhome.uaa.client` properties block, keeping `base-url`
   interpolated from `common-config.yml`.
3. Inject `UserAccountService` — never `AdminUserClient` — into a service that applies the tenant check.
4. Handle `ApiException` (any status ≥ 400) at the boundary; it is unchecked and carries status + body.

Ask first whether you need it at all: a pod service dealing with *shoppers* wants `cua`, not `uaa`
(`authentication.md`), and reading the current caller's identity needs no SDK — that comes from the JWT via
`UserOrgStoreIdentity` (`api-conventions.md`).

## Related

- `authentication.md` — the two authorization servers, `s2s` clients, token validation
- `api-conventions.md` — `UserOrgStoreIdentity`, `Readable*`/`Persistable*`
- `service-to-service.md` — the *preferred* mechanism for new cross-service calls
- `store-core.md` — uaa itself and `AdminUserController`

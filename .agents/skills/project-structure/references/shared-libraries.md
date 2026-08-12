# `store-commons/` — platform-wide shared libraries

Pure libraries, nothing deployable. Grouping folder; the leaves are the Gradle modules.

```
store-commons/
├── commons/                     domain primitives
├── autoconfigure/               Spring auto-config: security, JWT, web clients, shared YAML config
├── uaa-client/                  UAA admin SDK (interfaces + DTOs)
├── uaa-client-impl/             its implementation
├── secret-crypto/               pluggable secret encryption
│   ├── secret-crypto-core/          SPI + local providers
│   ├── secret-crypto-local/
│   ├── secret-crypto-aws/           AWS KMS
│   ├── secret-crypto-caffeine/      caching decorator
│   └── secret-crypto-autoconfigure/ Spring wiring
└── ecs-commons/                 AWS ECS/Fargate runtime helpers
    ├── ecs-service-discoveryclient/
    └── fargate-task-info/
```

## `store-commons:commons`

Domain primitives shared by every layer. `java-library-conventions` plugin, `api mongodb.bson`.

`com.asrevo.cvhome.commons.domain` holds ~40 **value objects** used pervasively in place of raw `String`/`Long`:
`StoreMerchantId`, `ManagerOrgId`, `PodId`, `LanguageCode`, `CurrencyCode`, `CountryIsoCode`,
`ZoneCode`, `Email`, `Domain`, `Pod`, `PodEndpoint`, `Theme`, `ColorTheme`, `Roles`, `UserOrgStoreIdentity`,
`SubscriptionPlan*`, plus `BaseEntity`/`Identifier`/`Entity`. This is a load-bearing convention, not a style
choice — argument resolvers and the permission evaluator dispatch on these types. See `api-conventions.md`.

## `store-commons:autoconfigure`

Spring Boot auto-configuration — security and cross-cutting infrastructure that every service inherits by
merely depending on this module.

- **Multi-tenant JWT:** `MultiIssuerJwtDecoder`, `MultiIssuerReactiveJwtDecoder` — a request's token may come
  from `uaa` *or* `cua` *or* a tenant-specific issuer, so decoding is issuer-aware. Servlet and reactive
  variants exist because the gateway is WebFlux and the rest are MVC.
- **Authorization:** `ServletPermissionConfig`, `PermissionAccessChecker`, `SecurityUtils`,
  `StoreSecurityService`.
- **Misc:** `CvhomeSharedConfig`, `WebClientBuilder`, `SwaggerConfig`, `RedirectionUrlBuilder`.
- **Shared YAML config** in its resources (`common-config.yml`, `lcl-config.yml`, `fargate-config.yml`, …) —
  see `build-system.md`. This is why the module is on the classpath of services that need no Java class from it.

## `store-commons:uaa-client` / `uaa-client-impl`

A typed SDK for UAA's admin API, so services can manage users/clients without hand-rolling HTTP:
`AbstractAdminClient`, `AdminUserClient`, `AdminClientClient`, `OAuth2TokenManager`, `ApiException`, and DTOs
(`ClientDetailsSettings`, `ClientSummary`, `UpdateUserRequest`, `ResetUserPasswordRequest`, `OAuthGrantType`,
`OAuth2TokenFormat`, `PageResponse`). Split interface/impl so consumers can depend on the contract alone:
`uaa-client` holds `UserAccountService` + `Persistable*`/`ReadableUser`, `uaa-client-impl` the HTTP client.

Full usage — wiring, the `admin-sdk` token, creating/fetching a user, and why the caller owns tenant scoping —
in `uaa-client.md`.

## `store-commons:secret-crypto:*`

Pluggable encryption for stored secrets (payment gateway keys, social-login credentials). SPI in
`secret-crypto-core`: `SecretCryptoProvider`, `SecretCryptoProviderRegistry`, `EncryptedValue`.

Implementations: `LocalAesCryptoProvider` with swappable key providers (`StaticKeyProvider`,
`FileSystemKeyProvider`, `EnvironmentVariableKeyProvider`, `RandomKeyProvider`, `CustomCallbackKeyProvider`);
`AwsKmsCryptoProvider` for KMS; `CachingSecretCryptoProvider` (Caffeine) as a decorator.

Selected by config — `common-config.yml` has `com.asrevo.cvhome.crypto.type: LOCAL` locally, KMS in AWS.
Consumers depend on `secret-crypto-autoconfigure` (e.g. `store-pod/cua`, `store-pod/payment`).

How it is actually used — the mapper-level encrypt/decrypt pattern that keeps tenant API keys opaque in the
database — is in `secrets-encryption.md`.

## `store-commons:ecs-commons:*`

AWS runtime helpers, depended on by every `-service`:

- `ecs-service-discoveryclient` — `EcsDiscoveryClient` / `EcsReactiveDiscoveryClient`, `CloudMapServiceInstance`,
  `EcsDiscoveryProperties`, `ConditionalOnEcsDiscoveryEnabled`. Implements Spring Cloud's `DiscoveryClient` over
  **AWS Cloud Map**, which is how services resolve one another in Fargate. Also vendors a patched copy of
  Spring's `org.springframework.cloud.client.DefaultServiceInstance` (adds a `getScheme()` override).
- `fargate-task-info` — reads the ECS task metadata endpoint (`Container`, `Network`,
  `EphemeralStorageMetrics`); surfaced as a bean + health indicator by `autoconfigure`'s `EcsInfoConfig`.

Both are inert outside AWS (guarded by conditionals), so they stay on the classpath locally. The local
counterpart and the whole `lb://` resolution story: `service-discovery.md`.

---

# ⚠️ The `store-commons` naming collision

**Two different modules are named `store-commons`.** Always disambiguate by Gradle path, never by folder name.

| | Root | Pod-scoped |
|---|---|---|
| Path | `:store-commons:commons` (and siblings) | `:store-pod:commons:store-commons` |
| Folder | `store-commons/` | `store-pod/commons/store-commons/` |
| Scope | Platform-wide: security, JWT, AWS, crypto, primitives | Pod business domain shared across pods |
| Consumers | `uaa`, `gateway-service`, `tenancy/*`, `cua`, and every `-service` (for `autoconfigure`) | Nearly every `store-pod` module: catalog, checkout, merchant, payment, customer, reference, cua, `store-cms-commons` |
| Example usage | `implementation project(':store-commons:commons')` | `api project(':store-pod:commons:store-commons')` |

**Rule of thumb:** cross-cutting infrastructure (auth, secrets, discovery, primitives) → root `store-commons`.
Pod business domain reused across pods → `store-pod/commons/store-commons`.

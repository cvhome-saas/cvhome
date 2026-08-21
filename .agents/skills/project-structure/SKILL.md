---
name: project-structure
description: Map of the cvhome monorepo - every service and what it does, whether it is backend / frontend / mixed, its port, and where its code lives. Covers store-commons (shared libs), store-core (platform services - uaa, gateway, tenancy, seller-ui), store-pod (business pods - merchant, content, catalog, checkout, payment, cua, spg, landing-ui), the multi-tenancy model (orgs, stores, and pods as physical per-region deployments, store provisioning, pod routing), the -commons/-core/-external-api/-service module pattern, API conventions (every endpoint takes StoreMerchantId and LanguageCode, heavy use of value objects, @PreAuthorize hasPermission authorization), encryption of tenant secrets at rest via secret-crypto, the two OAuth2 authorization servers (uaa for staff, cua for shoppers), shared configuration in store-commons/autoconfigure, database schema per service (Spring Data JDBC vs JPA, schema.sql / init-sql DDL), how every service is reachable both on its own port and as a path behind its gateway (store-core-gateway and the pod's spg/Caddy), the local docker-compose-lcl setup and the configure-domain.sh /etc/hosts script, how to run the whole stack locally with run-lcl.sh and how QA is done here (demo logins, browser-driven QA, .http API QA, tenant-isolation and permission checks, logs and traces, known local gaps), service-to-service calls via @HttpExchange -external-api clients, service discovery unified behind lb:// (Spring SimpleDiscoveryClient locally, the ecs-service-discoveryclient module over AWS Cloud Map on Fargate), managing uaa users through the uaa-client / uaa-client-impl admin SDK, domain events and the namastack transactional outbox, the landing-ui Next.js template system, and the Gradle version catalog. Includes the full step-by-step guide for creating a new landing-ui storefront template/theme, and for creating a whole new service - backend like catalog or tenancy, UI like seller-ui, or one deployable serving both like uaa - covering module layout, registering it in settings.gradle and the common/lcl/fargate config files, run-lcl.sh, gateway/Caddy routing and permissions. Trigger when navigating the repo, adding or scaffolding a new service or module, deciding where new code belongs, tracing a dependency or request path, writing or securing an API endpoint, adding a table or column or writing DDL, storing a secret or API key, working on tenancy/pods/store provisioning or where a store's data physically lives, calling another service, creating or looking up a user account, working out what URL to hit a service on or why a request is not reaching it, adding a service to discovery or debugging instance resolution, setting up or fixing local dev domains, running the app locally or QA-ing/verifying a change end to end or reproducing a UI bug in a browser, publishing a domain event, changing a port or config, adding a dependency version, creating or designing a storefront template or theme, or asking "where is X" / "what does this module do".
metadata:
  version: '3.2'
---

# cvhome monorepo

Multi-tenant e-commerce SaaS (evolved from Shopizer). One root Gradle build (`settings.gradle`, **no** root
`build.gradle`) drives everything — Java services *and* the npm/Angular/Next.js frontends. See `README.md` for
tech stack and build/run commands.

**Java package root:** `com.asrevo.cvhome.*` · **Gradle group:** `com.asrevo.cvhome`

`settings.gradle`'s `include(...)` list is the source of truth for what is a real Gradle module versus a plain
grouping folder. Check it there first.

## The three top-level trees

| Tree | Contains | Deployed? |
|---|---|---|
| `store-commons/` | Platform-wide shared **libraries** — no runnable app | No, libs only |
| `store-core/` | **Control-plane / platform** services: identity, gateway, tenant management, admin UI. One shared instance for the whole SaaS. | Yes |
| `store-pod/` | **Business pods** — the per-tenant "store" runtime: merchant, content, catalog, checkout, payment, customer auth, storefront, edge proxy. Deployed as an isolated pod, **many times over**. | Yes, once per pod |

Plus `build-logic/` (Gradle convention plugins, a composite build) and `gradle/libs.versions.toml` (version
catalog). See `references/build-system.md`.

## Service catalog — what each one is and does

**Category legend:** `BE` = pure backend (Spring Boot, REST/JSON) · `FE` = pure frontend (npm app) ·
`BE+FE` = one deployable that serves both a Java backend and its own UI · `INFRA` = proxy/edge.

### store-core — platform layer

| Service | Category | Port | Purpose |
|---|---|---|---|
| `store-core/uaa` | **BE+FE** | 8001 | OAuth2 **Authorization Server** + OIDC provider for staff/admin identity. Issues tokens for all other services. Serves an **embedded Angular admin SPA** (`uaa-fe`) from its own `static/` folder, plus Thymeleaf login pages. Controllers: `AuthController`, `AdminUserController`, `AdminClientController`, `AdminRoleController`, `UserInfoController`. |
| `store-core/gateway/gateway-service` | **BE** | 8000 | Spring Cloud **Gateway** (WebFlux, reactive) for the platform layer. Terminates the browser OAuth2 login session, exchanges it for tokens, and proxies to `tenancy` / `seller-ui`. Key classes: `GatewayRouteLocatorImpl`, `SecurityConfig`, `RedirectingServerAuthenticationSuccessHandler`, `PodClient`. |
| `store-core/tenancy/tenancy-service` | **BE** | 8020 | The **SaaS control plane**: organizations, store provisioning, subscription plans, Stripe billing, usage statistics. Controllers: `PodController`, `StoreManagerController`, `OrgManagerController`, `SubscriptionController`, `StripeWebhookController`, `SignUpController`, `StoreStatisticApi`. |
| `store-core/seller-ui` | **FE** | 8010 | Angular 20 (SSR) **seller/admin console** — the UI merchants and platform admins use. Feature areas under `src/app/pages/`: catalogue, orders, customer, payment, store-management, org-management, pod-management, subscription-and-usage, user-management, content. Logs in via `/oauth2/authorization/uaa`. |

`tenancy` is backed by sibling library modules (`tenancy-commons`, `tenancy-events`, `pod-external-api`) — see
`references/store-core.md`.

### store-pod — per-tenant business layer

| Service | Category | Port | Purpose |
|---|---|---|---|
| `store-pod/spg` | **INFRA** | 80 | "SaaS Pod Gateway" — a **Caddy** reverse proxy (not Java). Terminates TLS with **on-demand certificates** for custom tenant domains, resolves the domain → store via `domain_lookup`, adds tracing headers, and path-routes `/merchant*`, `/content*`, `/catalog*`, `/checkout*`, `/cua*`, `/payment*` to the pod services; everything else falls through to `landing-ui`. Config: `Caddyfile`. |
| `store-pod/merchant` | **BE** | 8120 | Store entity, settings, branding, domains, and routing. APIs: `MerchantStoreApi`, `ExternalMerchantStoreApi`. |
| `store-pod/content` | **BE** | 8121 | CMS pages, boxes, files, and images. API: `ContentApi`. |
| `store-pod/catalog` | **BE** | 8122 | **Products & categories**: product CRUD, inventory, pricing, images, attributes/options, product types, groups, manufacturers, relationships, reservations. APIs: `ProductApi`, `CategoryApi`, `ProductInventoryApi`, `ProductPriceApi`, `ExternalProductApi`, `ExternalProductReservationApi`. |
| `store-pod/checkout` | **BE** | 8123 | **Cart, orders, customers**: shopping cart, order lifecycle + status history, customer records, order/product/customer statistics. APIs: `ShoppingCartApi`, `OrderApi`, `CustomerOrderApi`, `OrderStatusHistoryApi`, `CustomerApi`, `OrderStatisticApi`. |
| `store-pod/payment` | **BE** | 8125 | **Payments**: gateway configuration per store, payment execution, provider webhooks. APIs: `PaymentConfigurationController`, `PrivatePaymentApi`, `PublicPaymentConfigurationController`, `PublicPaymentWebhookApi`, `ExternalPaymentGatewayApi`. Uses Stripe. |
| `store-pod/cua` | **BE+FE** | 8124 | "Customer User Account" — a **second OAuth2 Authorization Server**, this one for *storefront shoppers* (separate identity realm from `uaa`, which is for staff). Self-registration, social login, Thymeleaf-rendered login/registration pages. Controllers: `LoginController`, `RegistrationController`, `SocialLoginConfigController`, `UserInfoController`. Standalone module (no commons/core split). |
| `store-pod/landing-ui` | **FE** | 8110 | The customer-facing **storefront**. ONE Next.js 16 / React 19 app (`storefront/`) plus **theme packages** (`themes/<id>/`) inside an npm-workspaces monorepo; business logic in `libs/` (`types`, `services`, `hooks`), shared primitives in `libs/ui`, the theme contract in `libs/theme`. The theme is resolved per request from the `Theme` header spg injects; merchant colours come from the `Color-Theme` preset through a contrast-guarded bridge. Old one-app-per-theme templates are parked in `templates-deprecated/`. See `references/landing-ui.md`. |

## Every service sits behind a gateway

A service's port is its *internal* address; from outside its namespace it is only reachable as a **path on its
gateway**. `common-config.yml` records which edge fronts each service (`gateway-service-name`), and
`ServiceUrlBuilder` chooses between the two forms at runtime.

```
merchant, same endpoint, two addresses:
  http://merchant.gateway.com:8120/api/v1/store/...          direct — inside the pod namespace
  http://spg-507f1f77.gateway.com/merchant/api/v1/store/...  via spg — from anywhere else (prefix stripped)
```

Two edges: **`store-core-gateway`** (:8000, `gateway.com`) fronts `tenancy` (`/tenancy/**`),
`seller-ui`, and every pod under `/spg/**?store=&pod=`; **`spg`** (:80, the pod's Caddy) fronts the pod
services under `/merchant*`, `/content*`, `/catalog*`, `/checkout*`, `/payment*`, `/cua*`, with everything else falling
through to `landing-ui`. A seller request therefore crosses *both*:
`gateway.com:8000/spg/catalog/...` → `spg` → `catalog:8122`.

**Locally** only infra runs in Docker (`docker-compose-lcl.yml` — postgres, `spg`, monitoring); the Java
services run on the host and `spg`'s `extra_hosts` map service names back to it. Since everything is addressed
by hostname, run **`sudo ./extra/scripts/configure-domain.sh`** once to add the `/etc/hosts` entries —
platform (`gateway.com`, `uaa.`, `seller-ui.`), pod (`spg-507f1f77.gateway.com`, `merchant.`, …) and the demo
tenant storefronts (`org1-store1.spg-507f1f77.gateway.com`, …). Adding a store or pod locally means adding a
hosts entry there too.

Details, full routing tables and which address to use when: `references/gateways-and-local-domains.md`.

## Multi-tenancy in one paragraph

**A store is a logical tenant; a pod is a physical deployment.** `store-pod/` is not deployed once — it is
deployed many times, each instance (a **pod**) being a complete isolated stack (spg + merchant + catalog +
checkout + payment + cua + landing-ui) with **its own database**, hosting many stores. Control-plane's
`ManagerStoreEntity.podId` column is the whole routing table: it records which physical pod hosts each store.
Because a pod's `PodEndpoint` can be `INTERNAL` (same cluster) or `EXTERNAL` (a URL anywhere), **assigning a
store to a pod is what decides which region its data physically lives in.** A pod may be dedicated to one org or
shared by many.

Two runtime paths reach a store: sellers go through `store-core-gateway`, whose `PodClient` rebuilds its route
table from tenancy every minute and token-relays into the right pod; shoppers hit a custom domain on that
pod's own Caddy (`spg`), which asks the pod's `merchant-service` to map domain → store and injects
`Store-Id` / `Theme` headers.

Full model — provisioning flow, isolation table, who may manage pods: `references/multi-tenancy.md`.

## Two authorization servers

Deliberate, and the thing most likely to confuse: **`uaa` (:8001, store-core) authenticates staff, org owners
and merchants; `cua` (:8124, store-pod) authenticates storefront shoppers.** Separate realms, separate user
tables, separate issuers — same underlying Spring tech, so they look alike in code.

Pod services are resource servers that accept tokens from **both**, via `MultiIssuerJwtDecoder` and the
`issuer-uri-set` list in `store-pod-lcl-config.yml`. The `cua` issuer is spg-fronted *with* the `/cua` prefix,
which is why the Caddyfile preserves that prefix for `cua` alone. Services authenticate to each other with a
`client_credentials` `s2s` client against `uaa` (scope `store_core` or `store_pod`).

Details: `references/authentication.md`.

## Shared configuration

Config is **not** duplicated per service. Shared YAML ships inside `store-commons:autoconfigure`'s resources and
each service imports slices from the classpath:

```
common-config.yml   service registry: name, domain, port, namespace, gateway — for EVERY service
lcl-config.yml  / fargate-config.yml               environment slice
store-core-lcl-config.yml                          layer slice (core)
store-pod-lcl-config.yml / store-pod-fargate-...   layer slice (pod)
```

Composition rule: **`common-config` (always) + one environment slice + one layer slice.** A service's own
`application.yml` sets only `spring.application.name`, its `s2s` client, its schema, and its own settings.

**Change a port, host, or namespace in `common-config.yml`** — never hardcode one in a service. Profiles are
`lcl`, `fargate`, and `test-stores`. Details: `references/configuration.md`.

## API conventions — apply these to every new endpoint

**Almost every API takes `StoreMerchantId` and `LanguageCode`**, unannotated — custom argument resolvers supply
them from the `store` and `lang` query params. `store` is **mandatory** (the resolver throws if absent); `lang`
defaults to `en`. Tenant context is then passed explicitly down through facades and services, so every query is
tenant-scoped by construction.

```java
@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
public Entity create(@Valid @RequestBody PersistableProduct product,
                     StoreMerchantId merchantStore, LanguageCode language) {  }
```

**Authorization is declarative**, never an inline role check: `hasPermission(target, type, 'LAYER.DOMAIN.ACTION')`
dispatches through `CustomPermissionEvaluator` → `PermissionAccessChecker`, which is pod-aware and denies by
default.

**Value objects are used everywhere** instead of raw `String`/`Long` — ~40 records in
`store-commons/commons/.../domain/` (`StoreMerchantId`, `LanguageCode`, `PodId`, `Email`, `Domain`,
`CurrencyCode`, …). They carry behaviour (`LanguageCode.isAllLanguage()`, `PodId.shorten()`), let the resolvers
and permission evaluator dispatch on type, and persist via `AttributeConverter`s. Don't introduce a raw `String`
id or code — check `commons/domain/` first.

**Every endpoint ships a runnable request.** Adding or changing one means adding or changing its block in
`<service>/http/<api-class>.http` — IntelliJ HTTP Client format, one file per `*Api` class, addressed through
the gateway (`{{SELLER_UI_URL}}/spg/catalog/…`) rather than the service's own port. Env vars come from the
repo-root `http-client.env.json`; session ids from the gitignored `http-client.private.env.json`.

Details: `references/api-conventions.md`, and `references/http-request-files.md` for the request files.

## Persistence — schema per service, two stacks

Every service owns a **Postgres schema** and ships its own hand-written DDL; there is no shared database and no
cross-service foreign key. Two persistence stacks coexist:

- **Spring Data JDBC** — `tenancy-service`. Entities use
  `org.springframework.data.relational.core.mapping.@Table(schema = "tenancy", …)`, extend `BaseEntity`, and it
  owns three schemas (`tenancy`, `org`, `tenancy_outbox`) mirroring its bounded contexts. DDL at
  `src/main/resources/schema.sql`.
- **Spring Data JPA / Hibernate** — the pod services. Entities use `jakarta.persistence`, extend
  `SalesManagerEntity`, and get their schema from `hibernate.default_schema: ${spring.application.name}`. DDL at
  `src/main/resources/init-sql/schema.sql` plus `data-common.sql`.

`spring.sql.init.mode: always` runs the SQL on every startup (everything is `CREATE TABLE IF NOT EXISTS`), and
`ddl-auto: update` is only a safety net — **`schema.sql` is the source of truth.** Enums are `varchar` +
`CHECK` constraints, so adding an enum value needs a DDL change too.

Details: `references/database-schemas.md`.

## Secrets are encrypted at rest

Tenant-supplied credentials (payment API keys, social-login app secrets, webhook secrets) are **encrypted in the
mapper layer**, so the database only ever holds an opaque `ENC:<version>:<keyId>:<algorithm>:<iv>:<ciphertext>`
envelope — nobody can read them straight from the table. Encrypt in `toEntity`, decrypt in `toDTO`, guard with
`EncryptedValue.isEncrypted(...)`. See `PaymentConfigurationMapper` and `SocialLoginConfigMapper`.

Details: `references/secrets-encryption.md`.

## Talking between services

Two sanctioned mechanisms — pick by whether the caller needs an answer now:

- **Synchronous:** a `@HttpExchange` interface in the provider's `-external-api` module. The provider's
  `External*Api` controller *implements* that same interface; the consumer builds a proxy with
  `RestClientBuilder.buildClient("catalog", Iface.class, errorCatalog)`. Never depend on another pod's `-core` or
  `-service`. → `references/service-to-service.md`, and `references/error-handling.md` for the error contract.
- **Asynchronous:** a domain event registered on an aggregate root (`registerEvent(...)` via
  `AbstractAggregateRoot`) and delivered by the **namastack transactional outbox** to an `@OutboxHandler`.
  Used in `tenancy-service` and `payment-service`. Put event types in their own **`-events` module**
  (like `subscription-events`) — consumers must know an event's structure, so the contract has to be
  dependable without pulling in the producer. Delivery is **at-least-once, so handlers must be idempotent**,
  and `@OutboxEvent(key = …)` picks the ordering key. → `references/events-outbox.md`

**Managing users in `uaa`** is the one exception to both: `store-commons:uaa-client` (contract:
`UserAccountService`, `PersistableUser`/`ReadableUser`) + `uaa-client-impl` (`AdminUserClient` over plain
`java.net.http`, its own `admin-sdk` `client_credentials` token with scope `super_admin`). Inject
`UserAccountService`, never the raw client, and stamp/verify `org` + `store` yourself — uaa stores them as
free-form user metadata and enforces no tenancy. → `references/uaa-client.md`

## Service discovery — one `lb://`, two implementations

Callers never write a URL: `ServiceUrlBuilder` turns a logical name into `lb://<service>`, a `@LoadBalanced`
builder hands it to Spring Cloud LoadBalancer, and a `DiscoveryClient` resolves it. **Only that client differs
per environment**, which is what makes local and Fargate behave identically:

| Profile | Config | Servlet | Reactive (gateway) |
|---|---|---|---|
| `lcl` | `lcl-config.yml` → `spring.cloud.discovery.client.simple.instances` (static `http://localhost:<port>` list) | Spring's `SimpleDiscoveryClient` | Spring's `SimpleReactiveDiscoveryClient` |
| `fargate` | `fargate-config.yml` → `spring.cloud.ecs.discovery.*` (namespace, `service-ports`) | `EcsDiscoveryClient` | `EcsReactiveDiscoveryClient` |

The AWS pair is this repo's own `store-commons/ecs-commons/ecs-service-discoveryclient`, resolving instances
through **AWS Cloud Map** `DiscoverInstances` (host = the task's `AWS_INSTANCE_IPV4`, port from the instance
attribute → `service-ports` → `default-port`). It is on every `-service`'s classpath but only activates when
`spring.cloud.ecs.discovery.enabled` *and* a `namespace` are set — neither is set locally, so the simple client
stays in charge. **A new service needs an entry in all three files**: `common-config.yml`, `lcl-config.yml`, and
`fargate-config.yml`. Details: `references/service-discovery.md`.

## The pod module pattern: `-commons` / `-core` / `-external-api` / `-service`

Business pods (`merchant`, `catalog`, `checkout`, `payment`) are split into up to four Gradle modules named
`<domain>-<suffix>`:

| Suffix | Role | Depends on |
|---|---|---|
| `-commons` | Domain model: JPA entities + `Readable*`/`Persistable*` DTOs. Leaf module. | pod `store-commons`, `reference-commons` |
| `-core` | Business logic: repositories, services, facades, populators. | its own `-commons` |
| `-external-api` | Thin **client contract** so *other* services can call this pod over HTTP without pulling in `-core`. | its own `-commons` only |
| `-service` | The deployable Spring Boot app: `*Api` controllers, `SecurityConfig`, `*Application` main class. | `-core` + `-external-api` |

A pod may host more than one sub-domain in one service (e.g. `merchant-service` serves both `merchant-*` and
`content-*` modules). Details and per-pod module lists: `references/store-pod.md`.

## Adding a whole new service

Decide the **shape** first — backend only (`catalog`, `tenancy`), frontend only (`seller-ui`,
`landing-ui`), or one deployable serving both (`uaa`, `cua`) — then the **tree** (`store-core/` = one shared
platform instance, `store-pod/` = deployed once per pod). Those two choices fix the module layout, the config
slices, the s2s client and the fronting gateway.

Whatever the shape, the service does not exist until it is registered in **four** places —
`settings.gradle`, `common-config.yml` (block key **must** equal `spring.application.name`), `lcl-config.yml`,
`fargate-config.yml` — plus a row in `run-lcl.sh` and a route on its edge. For a pod service that is a
`store-pod/spg/Caddyfile` block; **for any `store-core/` service — backend, UI or both — it is
`GatewayRouteLocatorImpl`, and the name must go in its `backendServices` array as well as getting a
`.route(...)`**, because that array is negated to build seller-ui's catch-all, which otherwise swallows the
path. Miss one of these and you get "no instances available", a gateway 503, or seller-ui's HTML instead of
your API.

**Full procedure, per-shape skeletons and a checklist: `references/new-service.md`.**

## QA — proving a change works end to end

Unit tests prove a unit; **QA proves the feature works through the path a user takes** — browser → gateway →
uaa → pod service → database. Bring the whole stack up with **`./extra/scripts/run-lcl.sh`** (infra + every
Java service + both frontends, profiles `lcl,test-stores`), sign in as the seeded `org1-admin` / `admin` on
`http://gateway.com:8000/` or as `user` / `revo` on `http://org1-store1.spg-507f1f77.gateway.com`, and drive
it in the browser or through the endpoint's `.http` blocks. Two things QA must show beyond the happy path:
**tenant isolation** (repeat as a second store — it must not see the first store's data) and the
**permission gate** (no token → 403, not an empty 200).

Use `./extra/scripts/run-lcl.sh start` to bring the stack up, `stop` to tear down the recorded run,
`stop <service...>` to stop only selected services, `restart` to replace the stack, `restart <service...>` to
restart only selected services, `logs [service...]` to tail service logs, and `pid [service...]` to inspect the
recorded processes. A no-argument run still means `start`.
Full `start`, `stop`, and `restart` reset compose volumes, logs, and runtime pid files. Selected service
start/stop/restart is scoped to the named service and keeps infra/data in place. Use `start -d` or full
`restart -d` to return the terminal after the requested service ports open; selected `start -d <service>` asks
the recorded supervisor to start that service without stopping the rest of the stack.

**Procedure, flags, logins, browser tooling, log/trace locations, known local gaps and the checklist:
`references/qa-testing.md`.**

## Frontend patterns — three distinct ones

1. **`-ui` suffix = Gradle-wrapped npm app.** `seller-ui` (Angular) and `landing-ui` (Next.js) both apply the
   `ui-conventions` plugin, so Gradle `build` → `npm run build` and Gradle `bootRun` → `npm run dev`, and both
   get container images the same way. Framework differs; the build contract does not.
2. **Embedded Angular in Spring Boot** (`uaa`): `uaa-fe` lives at `store-core/uaa/src/main/resources/uaa-fe`,
   is **not** a Gradle module, is built by the `node` plugin, and its `dist` is copied into
   `src/main/resources/static` before `processResources` — so Spring Boot serves the SPA from its own port.
3. **Server-rendered Thymeleaf** (`uaa` login pages, `cua`): classic server-side templates, no SPA.

See `references/frontends.md`.

## Where to look

| I need to… | Go to |
|---|---|
| Find a business capability | `store-pod/<domain>/` — or `store-core/` if it's platform-level (auth, billing, tenants) |
| Find a REST endpoint | the `<domain>-service` module, in `**/api/**` or `**/controller/**` |
| Write a new endpoint | take `StoreMerchantId merchantStore` + `LanguageCode language`, add `@PreAuthorize("hasPermission(...)")`, add its block to `<service>/http/<api-class>.http` |
| Run an endpoint by hand | `<service>/http/<api-class>.http` — or write it there if it is missing |
| QA a change / reproduce a UI bug / drive the app in a browser | `references/qa-testing.md` — start with `./extra/scripts/run-lcl.sh` |
| Bring the local stack up or shut it down | `references/qa-testing.md` §1 (`run-lcl.sh start/stop/restart/logs/pid`) |
| Log in locally (seller or storefront) | `references/qa-testing.md` §2 — the `test-stores` demo accounts |
| Add a new permission | a `case` in `CustomPermissionEvaluator` + a method on `PermissionAccessChecker` |
| Store an API key / secret | encrypt in the mapper via `SecretCryptoProvider` — never a plaintext column |
| Add a table or column | the service's `schema.sql` / `init-sql/schema.sql`, not just the entity |
| Debug "the event never arrived" | query `outbox_record` for `status='FAILED'`, read `failure_reason` |
| Pass an id or code around | use the value object from `store-commons/commons/.../domain/`, not a `String` |
| Change business logic | `<domain>-core` (services/facades), not `-service` |
| Add/change an entity or DTO | `<domain>-commons` |
| Call another pod from a service | that pod's `-external-api` module + a bean in your `ClientsConfig` |
| React to something without blocking | a domain event in an `-events` module + `@OutboxHandler` |
| Understand how a store maps to physical infrastructure | `ManagerStoreEntity.podId` → `org.pod` → `PodEndpoint` |
| Find shared auth/JWT/security code | `store-commons:autoconfigure`, package `com.asrevo.cvhome.s2s.*` |
| Create / fetch / list a user in `uaa` | inject `UserAccountService` (`uaa-client`), stamp `org` + `store` |
| Make a new service resolvable by `lb://` | `common-config.yml` + `lcl-config.yml` instances + `fargate-config.yml` `service-ports` |
| Debug "no instances available for X" | that service's entry in `lcl-config.yml` (local) or its Cloud Map registration (AWS) |
| Debug a login problem | first decide: seller/admin → `uaa`, shopper → `cua` |
| Change a service port or host | `store-commons/autoconfigure/src/main/resources/common-config.yml` |
| Curl a pod service by hand | `http://spg-507f1f77.gateway.com/<service>/...`, not the raw port |
| Fix "host not found" running locally | `sudo ./extra/scripts/configure-domain.sh` (`/etc/hosts` entries) |
| Change how a request is routed to a service | `store-pod/spg/Caddyfile` (pod) or `GatewayRouteLocatorImpl`/`PodClient` (platform) |
| Bump a dependency version | `gradle/libs.versions.toml` — never hardcode versions in a `build.gradle` |
| Add a storefront theme | `store-pod/landing-ui/themes/` — `npm run new-theme <id>`, then follow `references/new-landing-ui-template.md` |
| Create a whole new service (backend, UI, or both) | `references/new-service.md` — pick the shape first, then register it in all four config files |
| Know if a folder is a build unit | `settings.gradle` |

## Reference files

**Architecture**
- `references/multi-tenancy.md` — orgs / stores / pods, provisioning, pod routing, regional placement, isolation.

**Structure**
- `references/new-service.md` — **step-by-step procedure + checklist for creating a new service**: the three
  shapes (backend like `catalog`/`tenancy`, UI like `seller-ui`, both-in-one like `uaa`), module layout
  per shape, the four registration files, routing, and what `store-commons:autoconfigure` already gives you.
- `references/store-core.md` — platform services in depth: uaa, gateway, tenancy and its library modules.
- `references/store-pod.md` — the 4-module pod pattern with evidence, per-pod breakdown, spg routing, pod-shared libs.
- `references/shared-libraries.md` — `store-commons` submodules, the `store-commons` naming collision.

**Cross-cutting mechanics**
- `references/api-conventions.md` — `StoreMerchantId`/`LanguageCode` on every API, value objects, `hasPermission`.
- `references/http-request-files.md` — the `.http` request file every endpoint ships: where it lives, the shared
  env files, gateway-form addressing, response handlers, and which existing `.http` files are stale.
- `references/database-schemas.md` — schema per service, Spring Data JDBC vs JPA, DDL locations, outbox tables.
- `references/secrets-encryption.md` — encrypting tenant credentials at rest via `secret-crypto`.
- `references/authentication.md` — the two authorization servers, multi-issuer JWT, s2s clients, login flows.
- `references/configuration.md` — the shared config slices, the composition rule, the service registry.
- `references/gateways-and-local-domains.md` — port vs gateway-path addressing, both edges' routing tables, the local Docker/`/etc/hosts` setup.
- `references/service-to-service.md` — `@HttpExchange` contracts, `RestClientBuilder`, URL/namespace resolution.
- `references/error-handling.md` — the `BaseException` hierarchy and its rules, the ProblemDetail wire format, the shared advice, and typed service-to-service errors.
- `references/service-discovery.md` — `lb://` resolution, simple discovery locally vs `ecs-service-discoveryclient` / Cloud Map on Fargate.
- `references/uaa-client.md` — the UAA admin SDK: creating/reading users in `uaa`, tenant metadata, wiring.
- `references/events-outbox.md` — aggregate roots, `@OutboxEvent`/`@OutboxHandler`, when to use events vs. calls.

**Running & QA**
- `references/qa-testing.md` — **how QA is done here**: `run-lcl.sh` and its flags, the seeded demo logins and
  entry points, browser-driven QA with the Chrome tooling, API QA through the `.http` files, logs/traces/outbox
  as evidence, the known local gaps (no MinIO → broken images), the QA checklist, and where `unitTest` /
  `integrationTest` fit.

**Frontend & build**
- `references/frontends.md` — seller-ui, the embedded `uaa-fe` build flow, `ui-conventions`.
- `references/landing-ui.md` — landing-ui workspace layout and the template/theme system.
- `references/new-landing-ui-template.md` — **step-by-step procedure + checklist for adding a storefront theme.**
- `references/build-system.md` — version catalog, convention plugins, build commands.

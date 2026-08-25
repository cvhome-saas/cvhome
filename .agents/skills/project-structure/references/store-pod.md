# `store-pod/` — the per-tenant business layer

A "pod" is an isolated **deployment** of everything below, serving a group of tenant stores — the whole tree is
deployed once per pod, each with its own database. Namespace in config: `store-pod-<id>.cvhome.lcl`, fronted by
`spg` (Caddy). Which stores live in which pod, and how pods are provisioned and routed to, is in
`multi-tenancy.md`. Standing up a *new* pod service (modules, config registration, Caddy route) is
`new-service.md`.

```
store-pod/
├── spg/                          INFRA :80    Caddy edge proxy (Caddyfile, compose.yml)
├── landing-ui/                   FE    :8110  Next.js storefront + template system
├── cua/                          BE+FE :8124  shopper OAuth2 auth server (standalone)
├── merchant/                     BE    :8120  store, branding, domains, routing
├── content/                      BE    :8121  CMS pages, boxes, files, images
├── catalog/                      BE    :8122  products & categories
├── checkout/                     BE    :8123  cart, orders, customers
├── payment/                      BE    :8125  payment gateways & webhooks
└── commons/                                   pod-shared libraries (grouping folder)
    ├── store-commons/                         pod-scoped shared domain
    ├── store-modules/store-cms-commons/       CMS primitives
    ├── reference/{reference-commons, reference-core}    countries, zones, currencies, languages
    └── customer/{customer-commons, customer-core}       shared customer domain
```

## The 4-module pattern in detail

The same shape holds for `merchant`, `catalog`, `checkout`, and `payment`.

### `-commons` — domain model (leaf)

JPA-annotated entities and read/write DTOs. Applies `java-library`; JPA/Hibernate/Lombok are `compileOnly` so
the module stays dependency-light. Depends only on `store-pod:commons:store-commons` and `reference-commons`.

Evidence: `ReadableMerchantStore` / `PersistableMerchantStore`, catalog's product DTOs, and checkout's order DTOs.

The `Readable*` / `Persistable*` DTO pair is a repo-wide convention: `Readable*` goes out to clients,
`Persistable*` comes in from them, and the entity is neither.

### `-core` — business logic

Repositories, services, facades, and populators that map entity ↔ DTO. Depends on its own `-commons` plus
`store-cms-commons`.

Evidence: `MerchantRepository`, `MerchantStoreService`/`Impl`, `StoreFacade`,
`ReadableMerchantStorePopulator`, and `PersistableMerchantStorePopulator`. Content has the equivalent classes in
its own `content-core` module.

**So: `-commons` = the data model, `-core` = the logic that operates on it.**

### `-external-api` — remote client contract

A thin library other services depend on to call this pod over HTTP, *without* dragging in `-core` or a database
connection. Depends only on its own `-commons` + `spring-web`.

Evidence: `MerchantStorePodClient`, `ExternalMerchantStoreService`. Real consumer: `store-pod/cua`'s
`build.gradle` has `implementation project(':store-pod:merchant:merchant-external-api')`.

These are `@HttpExchange` interfaces, and the mirror image lives inside `-service`: an `External*Api` controller
(e.g. `ExternalMerchantStoreApi`, `ExternalProductApi`, `ExternalOrderApi`, `ExternalPaymentGatewayApi`)
**implements the same interface**, so the route and the client contract cannot drift. `Public*` prefixes mark
unauthenticated endpoints (e.g. `PublicPaymentWebhookApi`).

Full mechanics — proxy construction, service-name → URL resolution, adding a new call — in
`service-to-service.md`.

### `-service` — the deployable Spring Boot app

Applies the `spring-boot` plugin, has the `*Application` main class, `*Api` controllers, config
(`SecurityConfig`, `S3Config`, `SwaggerConfig`), exception handlers. Declares `-core` + `-external-api`
directly (so `-commons` arrives transitively), plus cross-cutting infra: `fargate-task-info`,
`ecs-service-discoveryclient`, `store-commons:autoconfigure`.

Evidence: `MerchantApplication`, `MerchantStoreApi`, `ExternalMerchantStoreApi`, `AuthController`,
`RouterController`.

## Per-pod breakdown

### `merchant` and `content` — independent services

`merchant` owns store, branding, domain, and routing data. Its modules are `merchant-commons`, `merchant-core`,
`merchant-external-api`, and `merchant-service`.

`content` owns CMS pages, boxes, files, and images. Its modules are `content-commons`, `content-core`, and
`content-service`; it has no external-api module. SPG routes legacy content paths under `/merchant` to
`content-service` indefinitely.

### `catalog` — products & categories

APIs under `api/v1/`: `ProductApi`, `CategoryApi`, `ProductInventoryApi`, `ProductPriceApi`, `ProductImageApi`,
`ProductTypeApi`, `ProductGroupApi`, `ProductAttributeOptionApi`, `ProductPropertySetApi`,
`ProductRelationshipApi`, `ProductManufacturerApi`, `ExternalProductApi`, `ExternalProductReservationApi`.

### `checkout` — cart, orders, customers

APIs under `api/order/v1/` and `v2/`: `ShoppingCartApi`, `OrderApi`, `CustomerOrderApi`, `ExternalOrderApi`,
`OrderStatusHistoryApi`, `CustomerApi`, `ReferencesApi`, and `v2/statistic/` (`OrderStatisticApi`,
`ProductStatisticApi`, `CustomerStatisticApi`). Note the mixed API versioning — statistics are `v2`.

### `payment` — gateways & webhooks

`PaymentConfigurationController` (per-store gateway setup), `PrivatePaymentApi`,
`PublicPaymentConfigurationController`, `PublicPaymentWebhookApi` (provider callbacks),
`ExternalPaymentGatewayApi`. Stripe is the integrated provider.

This is the one pod using the **transactional outbox**: the `Transaction` aggregate registers
`PaymentPaidEvent` / `PaymentFailedEvent` / `PaymentCanceledEvent`, and `PaymentOutboxHandler` /
`WebhookOutboxHandler` push the resulting status to checkout asynchronously. See `events-outbox.md`.

## Pods that break the pattern

- **`cua`** (:8124) — a single standalone Gradle module, no commons/core split. It's an OAuth2 authorization
  server for storefront shoppers, with Thymeleaf-rendered login/registration UI. Controllers: `LoginController`,
  `RegistrationController`, `SocialLoginConfigController`, `AuthController`, `oidc/UserInfoController`. Notably
  it depends on `secret-crypto-autoconfigure` (for encrypted social-login credentials) and
  `merchant-external-api` (to resolve which store a shopper belongs to).
- **`landing-ui`** (:8110) — npm/Next.js, see `landing-ui.md`.
- **`spg`** (:80) — Caddy config, see below.

## `spg` — the tenant edge proxy

Caddy, not Java. Responsibilities, from the `Caddyfile`:

1. **On-demand TLS** — issues certificates per custom tenant domain at request time via
   `on_demand_tls { ask $ASK_TLS_URL }`, with certs stored in S3. That `ask` URL is this pod's own
   `merchant-service` (`/api/v1/router/public/ask-for-tls`), so a pod only mints certs for its own tenants.
2. **Domain → store resolution** — `domain_lookup { lookup_url ... cache_ttl 5m }` on the fall-through route
   calls `merchant-service`'s `/api/v1/router/public/lookup-by-domain`, which returns `Store-Id`, `Theme`,
   `Color-Theme` and language headers that get injected into the request. This is how the single `landing-ui`
   deployment knows which tenant it is rendering. See `multi-tenancy.md`.
3. **Path routing** to pod services, with an OpenTelemetry span and `X-Trace-Id`/`X-Span-Id` response headers
   per route:

   | Path | → | Notes |
   |---|---|---|
   | `/merchant*` | `merchant:8120` | `handle_path` (prefix stripped; legacy content subpaths are routed to content) |
   | `/content*` | `content:8121` | `handle_path` |
   | `/catalog*` | `catalog:8122` | `handle_path` |
   | `/checkout*` | `checkout:8123` | `handle_path` |
   | `/payment*` | `payment:8125` | `handle_path` |
   | `/cua*` | `cua:8124` | `handle` — prefix **kept**, sends `X-Forwarded-Prefix: /cua` |
   | everything else | `landing-ui:8110` | after `domain_lookup` |

   `cua` is the exception: it needs the `/cua` prefix preserved because OAuth2 redirect URIs must match.

**Every pod service therefore has two addresses** — its port (`merchant:8120`, inside the namespace) and its
spg path (`spg-507f1f77.gateway.com/merchant`, from anywhere else). Prefer the gateway path when calling by
hand; `gateways-and-local-domains.md` has both edges' routing tables and the local `/etc/hosts` setup.

## Pod-shared libraries (`store-pod/commons/`)

| Module | Role |
|---|---|
| `store-commons` (`:store-pod:commons:store-commons`) | Pod-scoped shared domain — consumed by essentially every pod module. **Not** the root `store-commons/`; see `shared-libraries.md`. |
| `store-modules/store-cms-commons` | CMS primitives shared by content/catalog (`api project(':store-pod:commons:store-commons')`). |
| `reference/{reference-commons, reference-core}` | Reference data: countries, zones, currencies, languages. |
| `customer/{customer-commons, customer-core}` | Customer domain shared between checkout and cua. |

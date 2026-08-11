# `store-core/` — the platform / control-plane layer

One shared deployment for the whole SaaS (not per-tenant). Namespace in config: `store-core.cvhome.lcl`,
fronted by `store-core-gateway`.

```
store-core/
├── uaa/                              BE+FE  :8001  auth server + embedded Angular admin SPA
├── gateway/gateway-service/          BE     :8000  Spring Cloud Gateway (reactive)
├── control-plane/                            grouping folder
│   ├── control-plane-service/        BE     :8020  the deployable app
│   ├── manager-commons/              lib           org/store DTOs
│   ├── manager-events/               lib           org/store domain events
│   └── pod-external-api/             lib           client for talking to a tenant pod
└── seller-ui/                        FE     :8010  Angular 20 SSR admin console
```

## `uaa` — identity for staff/admins

The OAuth2 **Authorization Server** (`spring-boot-starter-oauth2-authorization-server`) and OIDC provider that
every other service trusts. JDBC-backed sessions, Thymeleaf login pages, and an embedded Angular admin SPA.

- Main class: `com.asrevo.cvhome.uaa.UaaApplication`
- Web layer: `web/AuthController`, `web/StaticController`, `web/oidc/UserInfoController`,
  `web/admin/{AdminUserController, AdminClientController, AdminRoleController}`
- Embedded frontend: `src/main/resources/uaa-fe` (Angular 20, Nebular, module-federation) — see
  `frontends.md` for the exact build wiring.
- Depends on `store-commons:commons`, `store-commons:autoconfigure`, and both `ecs-commons` modules.

**Contrast with `cua`** (`store-pod/cua`, :8124): same technology, different realm. `uaa` authenticates
platform staff and merchants; `cua` authenticates storefront shoppers.

## `gateway/gateway-service` — platform edge

Reactive Spring Cloud Gateway (`spring-cloud-starter-gateway-server-webflux`). It is the browser-facing entry
point for the admin/seller experience: it runs the OAuth2 **client** login flow against `uaa`, holds the
session, and forwards authenticated requests to `control-plane` and `seller-ui`.

- Main class: `com.asrevo.cvhome.gateway.StoreCoreGatewayApplication`
- `config/GatewayRouteLocatorImpl` — programmatic route definitions
- `config/SecurityConfig`, `config/CapturingServerOAuth2AuthorizationRequestResolver`,
  `config/RedirectingServerAuthenticationSuccessHandler` — login/redirect handling
- `controller/AuthController`, `controller/LogoutController`
- `client/PodClient` — implements `RouteDefinitionRepository`: polls control-plane for the pod list every minute
  and **generates gateway routes per pod at runtime** (`multi-tenancy.md`)
- Also registers a `client_credentials` service-to-service client
  (`store-core@service.store-core.internal`, scope `store_core`) for machine calls to `uaa`.

Note the naming: this is the **platform** gateway. The **tenant** edge is `store-pod/spg` (Caddy) — different
technology, different layer.

## `control-plane` — the SaaS brain

`control-plane-service` is the deployable app; the surrounding modules are libraries it (and other services)
consume.

**Controllers** (`com.asrevo.cvhome.controlplane.*`):
- `org/controller/PodController` — pod/tenant infrastructure
- `manager/controller/` — `StoreManagerController`, `SignUpController`, `UserAccountController`,
  `SaasController`, `RouterController`, `admin/OrgManagerController`
- `manager/controller/statistic/` — `StoreStatisticApi`, `OrgStatisticApi`
- `subscription/controller/` — `SubscriptionController`, `SubscriptionPlanController`,
  `SubscriptionPlanDetailsController`, `StripeWebhookController`

**Library modules and what distinguishes them:**

| Module | Contents |
|---|---|
| `manager-commons` | `ManagerOrgDto`, `ManagerStoreDto`, `ListManagerStoreQuery`, `ProvisioningState`. Depends on `store-commons:commons` (+ `dnsjava` for custom-domain checks). |
| `manager-events` | `OrgCreatedEvent`, `StoreCreatedEvent`, `StoreProvisionedEvent`. Uses `namastack-outbox-api` — events go out via a **transactional outbox**. |
| `pod-external-api` | `ExternalPodClient` — how store-core talks to a tenant pod. |

The `-events` suffix is unique to control-plane: it marks **messaging/event contract** modules, published
through the `io.namastack:namastack-outbox` starter.

## `seller-ui` — Angular 20 admin console

Standalone-component Angular app with SSR (`app.config.server.ts`, `app.routes.server.ts`), Nebular +
ng-bootstrap. Authenticates by redirecting to `/oauth2/authorization/uaa` (see
`src/environments/environment.ts`), so it relies on the gateway's session rather than holding tokens itself.

Feature areas under `src/app/pages/`: `catalogue`, `orders`, `customer`, `payment`, `content`,
`store-management`, `org-management`, `pod-management`, `subscription-and-usage`, `user-management`, plus
`shared`, `theme`, `facades`, `common`.

Build/run detail in `frontends.md`.

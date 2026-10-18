# `store-core/` — the platform / tenancy layer

One shared deployment for the whole SaaS (not per-tenant). Namespace in config: `store-core.cvhome.lcl`,
fronted by `store-core-gateway`.

```
store-core/
├── uaa/                              BE+FE  :8001  auth server + embedded Angular admin SPA
├── gateway/gateway-service/          BE     :8000  Spring Cloud Gateway (reactive)
├── tenancy/                          grouping folder
│   ├── tenancy-service/              BE     :8020  the deployable app — orgs, stores, members, signup, store → pod binding
│   ├── tenancy-commons/              lib           org/store DTOs
│   └── tenancy-events/               lib           org/store domain events
├── billing/                          grouping folder
│   ├── billing-service/              BE     :8021  plans, per-store subscriptions, Stripe, invoices, entitlements
│   ├── billing-commons/              lib           ids, enums, DTOs, error catalog
│   ├── billing-events/               lib           subscription domain events + commands
│   └── billing-external-api/         lib           entitlement / quota clients for tenancy, the gateway and the pods
├── pod-registry/                     grouping folder
│   ├── pod-registry-service/         BE     :8022  the pod catalog: identity, endpoint, health, capacity, placement
│   ├── pod-registry-commons/         lib           `PodView`, placement DTOs, lifecycle/health enums
│   └── pod-registry-external-api/    lib           `ExternalPodService` / `ReactiveExternalPodService`, placement client
└── console-ui/                        FE     :8011  Angular 20 SSR admin console
```

## `uaa` — identity for staff/admins

The OAuth2 **Authorization Server** (Spring Security's authorization server, merged into `spring-security` as of
7.0) and OIDC provider that every other service trusts. JDBC-backed sessions **and a JDBC authorization store**, so
refresh tokens survive a restart and can be revoked; the signing keys are rotated on a schedule and their private
halves are encrypted at rest with `secret-crypto`.

- Main class: `com.asrevo.cvhome.uaa.UaaApplication`
- Web layer: `web/{AuthController, LinkConfirmController, StaticController}`,
  `web/admin/{AdminUserController, AdminRoleController, AdminClientController, AdminSettingsController,
  AdminKeyController, AdminIdentityProviderController, AdminAuditController, AdminDashboardController,
  AdminSessionController}`, `web/account/AccountController`, `web/pub/*` for the endpoints a signed-out browser
  needs (invitation and reset-link acceptance, the sign-in page's providers and context)
- Behind them: `audit/`, `settings/`, `security/`, `password/`, `ratelimit/`, `session/`, `token/`, `invitation/`,
  `client/`, `keys/`, `idp/`, `dashboard/`
- **Brokered login**: uaa is an OAuth2 *client* as well as a server. `idp/` registers external providers (OIDC and
  OAuth 2.0 — Google, Microsoft, GitHub, Apple scaffolded, plus generic) whose client secrets are encrypted, and
  `security/Brokered*` turns an external identity into a local account by linking, confirming with a password, or
  provisioning. SAML is not built.
- Embedded frontend: `src/main/resources/uaa-fe` (Angular 20 on `@cvhome-saas/ui-kit`, no Nebular, no
  module federation) — see `frontends.md` for the build wiring, and `uaa-fe/lessons.md` for what the console
  deliberately does not draw.
- Depends on `store-commons:commons`, `store-commons:autoconfigure`, `store-commons:ui-kit` (frontend),
  `secret-crypto-autoconfigure`, and both `ecs-commons` modules.

**Contrast with `cua`** (`store-pod/cua`, :8124): same technology, different realm. `uaa` authenticates
platform staff and merchants; `cua` authenticates storefront shoppers.

## `gateway/gateway-service` — platform edge

Reactive Spring Cloud Gateway (`spring-cloud-starter-gateway-server-webflux`). It is the browser-facing entry
point for the admin/seller experience: it runs the OAuth2 **client** login flow against `uaa`, holds the
session, and forwards authenticated requests to `tenancy`, `billing`, `pod-registry`, `uaa` and `console-ui`
(`/tenancy/**`, `/billing/**`, `/pod-registry/**`, `/uaa/**`; everything else is the console's catch-all).

- Main class: `com.asrevo.cvhome.gateway.StoreCoreGatewayApplication`
- `config/GatewayRouteLocatorImpl` — programmatic route definitions
- `config/SecurityConfig`, `config/CapturingServerOAuth2AuthorizationRequestResolver`,
  `config/RedirectingServerAuthenticationSuccessHandler` — login/redirect handling
- `controller/AuthController`, `controller/LogoutController`
- `client/PodClient` — implements `RouteDefinitionRepository`: polls **pod-registry** for the pod list every
  minute (`ReactiveExternalPodService.listPods()`), seeded from configuration and keeping the last-known-good set
  on failure, and **generates gateway routes per pod at runtime** (`multi-tenancy.md`)
- Also registers a `client_credentials` service-to-service client
  (`store-core@service.store-core.internal`, scope `store_core`) for machine calls to `uaa`.

Note the naming: this is the **platform** gateway. The **tenant** edge is `store-pod/spg` (Caddy) — different
technology, different layer.

## `tenancy` — the SaaS brain

`tenancy-service` is the deployable app; the surrounding modules are libraries it (and other services)
consume.

Tenancy owns the **tenants**: orgs, stores, org members and invitations, signup, store lifecycle and the
store → pod binding (`manager_store.pod_id`). It no longer owns pods — those moved to `pod-registry` in 2026-08
(`.agents/plans/tenancy-and-pod-registry-split.md`) — and no longer owns subscriptions, which moved to `billing`.
Schema `tenancy`: `manager_org`, `manager_store`, `org_member`, `org_invitation`, `tenancy_audit` (Spring Data
JDBC, `schema.sql`), plus `tenancy_outbox`.

**Controllers** (`com.asrevo.cvhome.tenancy.*`, all `*Api` classes):
- `manager/controller/` — `StoreManagerApi`, `StoreLifecycleApi`, `SignUpApi`, `UserAccountApi`, `OrgMemberApi`,
  `SaasApi`, `RouterApi` (`store-pod-by-store-id` — reads `manager_store.pod_id`, which is why it stays here),
  `admin/OrgManagerApi`
- `manager/controller/statistic/` — `StoreStatisticApi`, `OrgStatisticApi`
- `controller/AuthApi`

It calls out through `pod-registry-external-api` (placement, pod lookup) and `billing-external-api` (store quota
before a store is created, entitlements); `StorePodClientFactory` resolves a pod through `CachingPodDirectory`
(pod-registry's client, seeded from `ServiceDomainProperties.pods()`) and talks to the pod's `merchant-service`
directly (`multi-tenancy.md`).

**Library modules and what distinguishes them:**

| Module | Contents |
|---|---|
| `tenancy-commons` | `ManagerOrgDto`, `ManagerStoreDto`, `OrgMemberDto`, `InvitationDto`, `CreateStoreRequest`, `ListManagerStoreQuery`, `ListOrgQuery`, `ProvisioningState`, `OrgStatus`, `StoreStatus`, `PodStoreCount`. Depends on `store-commons:commons`. |
| `tenancy-events` | `StoreEvent`, `StoreCreatedEvent`. Uses `namastack-outbox-api` — events go out via a **transactional outbox**. |

The `-events` suffix marks **messaging/event contract** modules, published through the `io.namastack:namastack-outbox`
starter; `tenancy` and `billing` both have one.

## `billing` — plans, subscriptions and entitlements (port 8021)

`billing-commons` / `billing-events` / `billing-external-api` / `billing-service`, package root
`com.asrevo.cvhome.billing`, Spring Data JDBC, schema `billing` (+ `billing_outbox`). Plan:
`.agents/plans/billing-subscription-service.md`.

Owns the money side of the SaaS: a DB-driven plan catalog (`plan`, `plan_price`, `plan_entitlement`, seeded from
`plan-catalog.yml` by `PlanCatalogSeeder` and pushed to Stripe by `PlanCatalogPublisher`), **one subscription per store** (`store_subscription`), one trial per org
(`org_trial_grant`), Stripe checkout / webhooks / idempotency (`processed_stripe_event`, `stripe_request`), invoice
history (`subscription_invoice`) and an audit trail (`subscription_audit`). Nothing about a store's plan lives in
tenancy any more.

- APIs (`api/v1`, `api/v2`): `PlanCatalogApi` (`/plan`), `SubscriptionApi` (`/subscription`, `STORE-CORE.BILLING.READ`
  / `.MANAGE`), `InvoiceApi` (`/invoice`), `PlatformBillingApi` (`/platform`, platform admin), `StripeWebhookApi`
  (`/stripe-webhook`), `BillingStatisticApi` (`/api/v2`).
- **s2s** — `ExternalEntitlementApi` (`/api/v1/entitlement`, `STORE-CORE.BILLING.ENTITLEMENT-READ`) and
  `ExternalStoreQuotaApi` (`/api/v1/quota`, `STORE-CORE.BILLING.QUOTA-CHECK`). `billing-external-api` wraps them as
  `ExternalEntitlementService` (+ a `ReactiveExternalEntitlementService` for the gateway) and
  `ExternalStoreQuotaService`, with a caller-side error catalog (`BillingApiErrors`, `StoreQuotaRefusedException`)
  and the `StoreEntitlements` guard. Consumers: `tenancy` (store quota, entitlements) and `gateway`.
- `billing-events`: `SubscriptionActivated|Renewed|PlanChanged|PastDue|Suspended|Canceled|TrialStartedEvent`,
  `InvoiceRecordedEvent`, plus the outbox-driven commands (`ExpireTrialCommand`, `ApplyPendingPlanChangeCommand`,
  `SuspendUnpaidSubscriptionCommand`).

Reachable as `/billing/**` on `store-core-gateway`.

## `pod-registry` — the pod catalog (port 8022)

`pod-registry-commons` / `pod-registry-external-api` / `pod-registry-service`, package root
`com.asrevo.cvhome.podregistry`, Spring Data JDBC, schema `pod_registry` (`pod`, `pod_store_placement`,
`pod_health_check`, `pod_audit`). Plan: `.agents/plans/tenancy-and-pod-registry-split.md`; migration out of tenancy's
old `org.pod`: `extra/migrations/2026-08-12-move-pods-to-pod-registry.sql`.

Owns the **pod**: identity, endpoint and `EndpointType`, name uniqueness, private-org assignment (`orgId` →
`PodVisibility`), lifecycle state (drain / resume), health probing (`PodHealthProbe`), capacity
(`PodCapacityService`) and **placement decisions** (`PodPlacementService`). `PodSeedInitializer` creates every pod
named in `ServiceDomainProperties` at start-up, so a fresh environment routes before anyone has touched the API.
Health gates placement only, never routing: an unhealthy pod keeps its gateway route because its tenants are still
there.

- APIs (`api/v1/pod`): `PodApi` — `GET list` (`STORE-CORE.POD.READ`; an org admin sees the shared pool plus their
  own private pods), `GET|PUT|DELETE {id}`, `POST {id}/drain`, `POST {id}/resume` (`STORE-CORE.POD.MANAGE`).
- **s2s** — `PodPlacementApi` (`api/v1/pod/private/placement`, `placement-recorded`, `placements-recorded`,
  `STORE-CORE.POD.PLACEMENT`): tenancy asks where a new store should go and reports back what it wrote.
- `pod-registry-external-api`: `ExternalPodService` / `ReactiveExternalPodService` (`listPods()`, wrapped by
  `CachingPodDirectory`) and `ExternalPodPlacementService`, with `PodRegistryApiErrors` /
  `PodPlacementRefusedException`. Consumers: `gateway` (`PodClient` builds the `/spg/**` routes from `listPods()`)
  and `tenancy` (placement, pod lookup).

Reachable as `/pod-registry/**` on `store-core-gateway`.

## `console-ui` — Angular 20 admin console

Standalone-component Angular app with SSR (`app.config.server.ts`, `app.routes.server.ts`), Tailwind v4
over a three-theme token layer, Transloco (en/ar, RTL). Authenticates by redirecting to
`/oauth2/authorization/uaa` (see `src/environments/environment.ts`), so it relies on the gateway's session
rather than holding tokens itself.

Feature areas under `src/app/features/` (dashboard, orders, catalogue, products, payments, customers,
users, profile, store-management, content, billing, plus the platform-admin set), tiered as
`features → layouts → shared → api → core → models` — the module's own `ARCHITECTURE.md` is the contract,
and `lessons.md` records the backend gaps.

Build/run detail in `frontends.md`.

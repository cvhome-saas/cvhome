# Authorization audit — findings register and remediation plan

This file is both the audit report and the plan: the findings table is the report, each phase is one PR, and
the *status* column is updated as phases land. QA cases live in each owning service's `qa/<service>-qa.md`, never
here.

## Context — the model as built

One evaluator: `store-commons/autoconfigure/src/main/java/com/asrevo/cvhome/s2s/config/internal/CustomPermissionEvaluator.java`.
The 3-arg `hasPermission` overload always denies (L79-82). The 4-arg overload ignores `targetType` and dispatches on
the permission string; an unknown token falls to `default -> false`. It delegates to
`s2s/services/PermissionAccessChecker.java` (the audience per token) which delegates to
`s2s/services/StoreRoleAccessChecker.java` (claims vs the requested store, realm and pod). Method security is
servlet-only (`ServletPermissionConfig`); the WebFlux gateway has none. **There is no `@PermissionAccessChecker`
annotation and no authorization aspect** — `PermissionAccessChecker` is a plain class reached only through
`@PreAuthorize("hasPermission(#store,'StoreMerchantId','LAYER.DOMAIN.ACTION')")`.

| Principal | Issuer / claims | Recognised by |
|---|---|---|
| Super admin | uaa, `ROLE_SUPER_ADMIN` | `isSuperAdmin`; wildcard store in `SecurityUtils.getOrgStoreIdentity` |
| Org admin | uaa, `ROLE_ORG_ADMIN`, `org` claim | `isOrgAdmin` → `isPodAllowOrg` → `ownsTheStore` (`StoreOrgOwnerRetriever`, or `DELEGATED`: tenancy and billing check for themselves) |
| Store admin / moderator | uaa, role + `org` + `store` claims | `store` claim must equal `?store=` (L169, L193) |
| Shopper | cua, `ROLE_CUSTOMER`, `realm` claim = store id | `isStoreCustomer`; authorities capped per realm by `RealmAwareJwtGrantedAuthoritiesConverter` |
| Pod service | uaa client credentials `store-pod-<id>@service.store-pod.internal`, `scope=store_pod`, `resource` = pod name | `isScopeStorePod`: `resource` must equal `pod.name()` (L268-273) |
| Store-core service | one shared `store-core` client, `scope=store_core` | `isScopeStoreCore` — reads any store |

Filter chains: pod services (`<svc>/config/SecurityConfig.java`) = `/api/*/private/**` authenticated, **anything
else permitAll**; store-core services = `/api/v1/*/public/**` + `/actuator/**` + swagger permitAll, anything else
authenticated; gateway `anyExchange().permitAll()`, CSRF disabled; cua permits every actuator endpoint
(`CuaSecurityConfig.java:59`); uaa narrows to health/info/prometheus. So on a pod, security is opt-in by path
spelling plus the annotation — the structural fact behind most findings below.

Gate tests: only checkout's `CheckoutApisTest.privateEndpointsAreGatedForTheirAudience` walks every handler.
`PaymentApisTest.privateEndpoints()` filters by method name and never saw `ExternalPaymentGatewayApi`.
`CvhomeArchitectureRules` (test-support) has five packaging rules, none about gates.

## Findings, severity-ranked

Severity: **C** critical (anonymous → platform control or secrets), **H** (wrong principal → destructive or
cross-tenant), **M** (wrong principal → non-public read, or abuse vector), **L** (hygiene / defence in depth).

| Id | Sev | Service | Where | What an attacker can do | Fix | Phase | Status |
|---|---|---|---|---|---|---|---|
| A1 | **C** (verify live) | all | `common-config.yml:291-299` exposure `*`, `gateway.access: unrestricted`; gateway `SecurityConfig.java:55` permitAll; store-core chains permit `/actuator/**` | Anonymous `/actuator/heapdump`, `/env`, `/configprops`, `/actuator/gateway/routes/**` through the public gateway and via `/tenancy/actuator/...` | Default `health,info,prometheus`; `when-authorized`; drop gateway unrestricted | P7, P8 | open |
| A2 | **H** | payment | `ExternalPaymentGatewayApi.java:40,58` no `@PreAuthorize` | Any bearer from either issuer (a self-registered shopper of any store on the pod) initiates a provider checkout under any store's keys with a chosen `successUrl`, and reads any store's payment status by ref | Token `STORE-POD.PAYMENT.INITIATE` → `isSameStorePod`; only checkout calls it | P1, P2 | P1 done; P2 done; P1 done |
| A3 | **H** | checkout | `CheckoutApi.java:84-89`, `OrderServiceImpl.java:98` | Guest enumerates `GET /api/v1/order/{n}/status?store=` by integer id: order status, payment status, live provider redirect URL of somebody else's open order | Guest must present `?ref=<OrderRef>`; the redirect URL carries it beside `orderId` | P4 | P4 done |
| A4 | **H** | tenancy | `StoreLifecycleApi.java:40,62-63,74-75` | Store admin, moderator, or any holder of the shared store-core client secret archives or deletes the store: the read token `STORE-CORE.STORE-FIND-ONE` gates a destructive op. Cross-org is not possible (tenancy is `DELEGATED`, foreign store → 404) | Wire `STORE-CORE.STORE-DELETE` → `hasAccessOnStoreDelete` (org admin or super admin) | P1, P3 | P1 done; P1, P3 done |
| A5 | **M** | payment | `PublicPaymentWebhookApi.java:32-42` | Anonymous POST writes an outbox row for any store id; the signature is checked later in `PaymentGatewayService.handleWebhook` and the row discarded — DB flooding, no 4xx to the sender | Verify the signature and the enabled configuration before `outbox.schedule` | P5 | open |
| A6 | **M** | all | no gate test in billing, tenancy, pod-registry, content, merchant, gateway | The class of bug A2 recurs silently | ArchUnit rule: every handler gated or in an explicit anonymous allow-list; stale entries fail | P6, P6b | P6 done (rule + checkout); P6b done (every service; payment and tenancy red on A2/A10/A17 until P2, P3 land) |
| A7 | **M** | cua | `CuaSecurityConfig.java:59` | As A1 on the shopper auth server (heapdump holds signing keys) | health/info/prometheus public, the rest authenticated, as uaa does | P9 | open |
| A8 | **M** | store-commons | `StoreRoleAccessChecker.java:146-148,164-166,188-190` | An `ORG_ADMIN`/staff token without a usable `org` claim on an org-private pod NPEs while logging the refusal → 500 with a stack trace instead of 403 | Null-safe log | P1 | done |
| A9 | **L** | billing | `PermissionAccessChecker.hasAccessOnBillingEntitlementRead` uses `hasScopeStorePod` without the pod match | Any pod's service reads any store's entitlement snapshot (plan ceilings, no tenant data) | Accepted; documented in the javadoc, pinned by test | P1, P10 | documented |
| A10 | **L** | payment, tenancy | `payment/controller/v1/auth/AuthController.java`, `tenancy/.../AuthApi.java:41` | Echo the entire JWT to its holder; payment's is outside `/private/`; no consumer | Delete payment's; tenancy keeps `current` behind `isAuthenticated()` | P2, P3 | P2 done (payment); tenancy in P3; P3 done (tenancy); P2 open |
| A11 | **L** | gateway | `ImpersonationController.java`, CSRF disabled, no `SameSite` on the session cookie | Cross-site POST/DELETE `/api/v1/impersonation` with the operator's cookie; mitigated by the JSON preflight | `same-site: lax`; a gate test on `operator()` | P8 | open |
| A12 | **L** | merchant | `MerchantStoreApi.java:98` guard reads `#store.org` from the body; `hasAccessOnStoreCreate` skips the org check on a shared pod | Only `store_core` principals pass; the body org is what tenancy decided | By design; registered | — | accepted |
| A13 | **L** | tenancy | `StoreManagerApi.java:127-129` | Any org admin probes platform-wide store-name existence | Names are globally unique and this is the create pre-flight | — | accepted |
| A14 | **L** | merchant | `ExternalMerchantStoreApi.java:33`, `MerchantStoreApi.java:52` | Anonymous store record including `audit` (creator) and domains; the storefront needs it | By design; consider trimming `audit` | P10 | accepted |
| A15 | **L** | pod-registry | `PodApi.java:110-165` null target | Super-admin-only tokens; correct | — | accepted |
| A16 | **L** | docs | `InternalStoreServiceImpl.java` ~L204, `SubscriptionApi.java:212-217` say `isOrgAdmin` ignores the store; `.claude/skills/project-structure` is a tracked, divergent copy of `.agents/skills/project-structure` | Misleads the next change | Rewrite comments; replace the copy with a symlink | P3, P10, P11 | P3 done (tenancy comment); P10, P11 open |
| A17 | **L** | payment | `PaymentConfigurationController.java:59-66` `GET /private/payment-configuration/supported-payment-types`, `/supported-payment-statuses`, no `@PreAuthorize` | Any bearer from either issuer (a self-registered shopper of any store on the pod) reads the two enum lists — no tenant data, but a `/private/` handler with no token is the shape A2 had, and the P6b rule refuses to allow-list it | `@PreAuthorize` with `STORE-POD.PAYMENT.*` on both, or move them beside `PublicPaymentConfigurationController` under `/public/` | P2 (or a follow-up) | found in P6b, open |

## By-design register (not findings)

- **Storefront anonymous reads on pod services** — merchant `GET /api/v1/store`, `/store/{code}`, `/store/languages`;
  inventory `GET /availability`, `POST /availability/query`; catalog `/detailed-product(s)` and the product,
  category, search reads; content `/api/v1/storefront/**`; checkout `CartApi` and `POST /cart/{code}/checkout`
  (the service answers `CHECKOUT.ORDER.LOGIN_REQUIRED` itself when the store demands a session); payment
  `PublicPaymentConfigurationController`. The Next.js storefront calls them with no token. Moving any under
  `/private/` breaks the guest storefront. They are the P6 allow-lists.
- **How to add an anonymous endpoint** — write the handler with no `@PreAuthorize`, outside `/private/` on a pod
  (under `/public/` on store-core), and add `SimpleClassName#methodName` to the `ANONYMOUS` set of that service's
  `<Service>ArchitectureTest` with a one-line reason in the javadoc beside it. `handlersAreGatedOrDeclaredAnonymous`
  fails the build until the entry exists, refuses a `/private/` entry (a token is never optional there) and an entry
  on a handler that already has a gate; `anonymousAllowListIsLive` fails when the handler is deleted or renamed. The
  allow-list is this register, per service, machine-checked — an entry nobody can justify in one line is a finding.
- **Guest carts** — the cart code is a UUID; possession is the credential.
- **Scope-only s2s tokens** with a null or org target (`STORE-CORE.BILLING.QUOTA-CHECK`, `STORE-CORE.POD.*`,
  `STORE-POD.MERCHANT.STORE-CREATE`): no store exists yet; only a service principal passes.
- **The shared `store_core` client** — one credential for every store-core service; `SCOPE_STORE_CORE` reads any
  store. Per-service clients are a platform design item (*Other repos*).
- **Scheduled jobs and `@OutboxHandler`s** run with no SecurityContext; they call services with an explicit
  `StoreMerchantId`, never controllers.
- **cua `/api/v1/auth/me` permitAll** — the storefront asks "who am I" before any session exists; the response is a
  `MeResponse`, not the token.
- **No JWT `aud` validation** — both issuers are ours; issuer pinning, the realm authority cap and the `resource`
  claim do the audience's job. Revisit when a third-party client is issued.

## Why the design is what it is

- Every fix is a token, never an inline role check — AGENTS.md rejects inline checks on sight. A2 and A4 start in
  store-commons (P1), then the service PRs wire the token.
- Gates that already exist are wired, not reinvented: `hasAccessOnStoreDelete` (unit-tested, unwired) and the
  `isSameStorePod` pattern behind `RESERVE`/`SIGNAL`.
- Guest order status reuses `OrderRef`, the UUID payments already key on; the redirect gains one query parameter.
- The ArchUnit rule is allow-list-driven: every handler is either gated or explicitly listed as anonymous, and a
  stale entry fails the build. The allow-lists are the by-design register, machine-checked.
- Actuator is fixed at the shared default so thirteen services change with one line; the env placeholder stays as
  the debugging lever.

## Phase 1 — store-commons: tokens, guard, dispatch (PR 1)

`CustomPermissionEvaluator`: `STORE-POD.PAYMENT.INITIATE` joins the pod set and the `isSameStorePod` case;
`STORE-CORE.STORE-DELETE` reaches `hasAccessOnStoreDelete`. `PermissionAccessChecker.hasAccessOnStoreDelete`
admits the super admin as `hasAccessOnBillingManage` does. `StoreRoleAccessChecker` renders the org null-safely in
the three pod refusals (A8). Entitlement read javadoc records A9. Tests extended: `CustomPermissionEvaluatorDispatchTest`,
`PermissionAccessCheckerTest.CreatingDeletingAndManagingAStore`, `StoreRoleAccessCheckerTest.OrgAdmins`.

## Phase 2 — payment: gate initiate/status, delete the token echo (PR 2, after PR 1)

`@PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.INITIATE')")` on both handlers of
`ExternalPaymentGatewayApi`; delete `payment/controller/v1/auth/AuthController.java`; `PaymentApisTest` walks every
controller; integration cases shopper 403, foreign pod 403, own pod 200; create `payment-service/http/` with
`external-payment-gateway-api.http` and `public-payment-webhook-api.http`; `payment-qa.md` SEC cases.

## Phase 3 — tenancy: archive/delete take the delete token (PR 3, after PR 1)

`StoreLifecycleApi.OWNER` → `STORE-CORE.STORE-DELETE`; `AuthApi.me` deleted, `current` behind `isAuthenticated()`;
the `InternalStoreServiceImpl` comment rewritten around `DELEGATED`; `TenancyApisTest` gains a handler walk until
P6b; integration: store admin 403, own org admin 200, foreign org 404, super admin 200; `.http` 403 block;
`tenancy-qa.md`.

## Phase 4 — checkout + landing-ui: guest order status needs the reference (PR 4)

One PR across checkout and landing-ui on purpose: either half alone breaks the payment-return page.
`RedirectUrls` appends `ref`; `OrderService.status(store, id, shopper, ref)`: shopper path unchanged, guest
requires the ref via `findByStoreMerchantIdAndIdAndOrderRef`, else 404; `CheckoutApi.status` takes
`@RequestParam(required = false) String ref`. landing-ui `libs/services/src/order-service.ts`,
`libs/hooks/src/use-order-status.ts`, `themes/*/src/pages/CheckoutResult.tsx` read `ref`. Tests in
`OrderServiceImplTest`, `CheckoutApiIntegrationTest`, `RedirectUrlsTest`; `.http`; QA in checkout and landing-ui.

## Phase 5 — payment: verify the webhook before it touches the outbox (PR 5)

`PaymentProcessor.authenticate(...)` split from parsing; `PaymentGatewayService.authenticateWebhook` → 404 (no
enabled configuration) or 400 (`InvalidWebhookSignatureException`); `PublicPaymentWebhookApi` calls it before
`outbox.schedule`. Tests: the negative twin of the outbox test, `StripeProcessorTest`, integration asserting no
outbox row; `.http`; `payment-qa.md` WHK.

## Phase 6 — test-support: the gate rule, adopted by checkout (PR 6)

`CvhomeArchitectureRules.handlersAreGatedOrDeclaredAnonymous(domain, policy, allowList)` and
`anonymousAllowListIsLive(domain, allowList)`; adopted in `CheckoutArchitectureTest`; the hand walk in
`CheckoutApisTest` reduced to token assertions; the rule unit-tested against fixture controllers.

As built: `HandlerPolicy { POD, CORE }` in test-support encodes the two mirror-image filter chains. A handler passes
when the method or its class carries `@PreAuthorize`, or its `SimpleClassName#methodName` is allow-listed; `POD` fails
a listed `/private/` handler, `CORE` fails a listed handler outside `/public/` unless it is in the optional fourth
argument `authenticatedOnly` (a bare session is the gate — tenancy's `AuthApi#current` shape; `POD` rejects a
non-empty set). Paths come from Spring's merged `@RequestMapping` (class prefix × method path) so `@GetMapping` & co.
resolve; the gate is read from ArchUnit's model by name, so test-support gains no security dependency.

## Phase 6b — adopt the rule everywhere (PR 7, test-only)

`*ArchitectureTest` in merchant, inventory, catalog, content, payment, cua (new), billing, tenancy, pod-registry,
sso-core, each with its allow-list from the register. Anything flagged that is not in the register is a new
finding, added to the table first.

As built: ten `*ArchitectureTest`s bind both rules — `POD` in merchant (5 entries), inventory (2), catalog (12),
content (12), payment (2), cua (1, new class, the shell's own `..cua.web..` controllers only; the SSO server's are
sso-core's); `CORE` in billing (2 anonymous / 0 authenticated-only), tenancy (5 / 3), pod-registry (0 / 0, kept so
the next handler cannot add an anonymous surface silently), sso-core (9 / 2). uaa has no controllers and its
`UaaShellArchitectureTest` already forbids them; the gateway is WebFlux with no method security and stays out.
Every entry is in the by-design register or public by its `/public/` path with a one-line reason beside it.

Not allow-listed, so the gate rule is red in two modules until the fix branches land: payment
`ExternalPaymentGatewayApi#initiatePayment`/`#status` (A2, `/private/`, gated on `fix/payment-initiate-gate`),
payment `AuthController#current`/`#me` (A10, deleted there), tenancy `AuthApi#me` (A10, deleted on
`fix/tenancy-store-delete-gate`) and `AuthApi#current` (A10, `isAuthenticated()` there — deliberately not in
`AUTHENTICATED_ONLY`, so that branch turns tenancy green with no edit), and the two enum reads of
`PaymentConfigurationController` (A17, new). Once P3 and P6b are both in, the interim handler walk in
`TenancyApisTest` is superseded by `TenancyArchitectureTest`; the same goes for `PaymentApisTest`'s walk on the P2
branch (its `UNGATED_PRIVATE` set is what A17 names).

## Phase 7 — store-commons: actuator default (PR 8)

`common-config.yml`: `include: ${MANAGEMENT_ENDPOINTS_EXPOSURE:health,info,prometheus}`, `show-details:
when-authorized`, delete `gateway.access: unrestricted`; confirm `lcl.yml` and the ECS health checks read status
only; test in autoconfigure; `qa/lcl-qa.md` stack case; record the A1 live result.

## Phase 8 — gateway: SameSite, actuator, impersonation gate test (PR 9)

`server.reactive.session.cookie.same-site: lax`; `/actuator/**` requires `SUPER_ADMIN` ahead of permitAll;
`ImpersonationControllerGateTest`; `.http` 401 block; `gateway-qa.md`.

## Phase 9 — cua: actuator (PR 10)

`EndpointRequest.to("health","info","prometheus")` permitAll, the rest authenticated; integration env 401, health
200, anonymous `/auth/me` carries no PII; `cua-qa.md`.

## Phase 10 — billing: register and stale comment (PR 11)

`SubscriptionApi` comment rewritten around `DELEGATED`; `PermissionAccessCheckerTest.Billing` names the cross-pod
entitlement read as accepted; `ExternalEntitlementApi` javadoc; `billing-qa.md` unit-only case.

## Phase 11 — docs: the model, the skill copy (PR 12)

"Authorization model" section in `.agents/skills/project-structure/references/authentication.md`, linked from
`api-conventions.md` §3 (adding an anonymous endpoint = adding it to the allow-list); fold anything unique in
`.claude/skills/project-structure/` into `.agents/` and replace the directory with a symlink; update the status
column here.

## Other repos (handed to the orchestrator)

- **cvhome-platform** — leave `MANAGEMENT_ENDPOINTS_EXPOSURE` unset in prod after P7; the literal s2s client secret
  committed in gateway, billing, pod-registry, catalog and payment `application.yml` becomes an env placeholder in
  a small follow-up here, rotation is platform-side; per-service `store_core` clients as a design item.
- **caddy-domainlookup / saas-gateway** — strip inbound `Store-Id` headers at the edge.
- **load-testing** — guest `order/{id}/status` calls need `ref` after P4.

## Deviations, as built

- P1: A8 is fixed by rendering the org null-safely in the log line rather than by an early return, because the
  refusal itself was already correct — only the log crashed.
- P4: one PR spans checkout and landing-ui deliberately — either half alone breaks the payment-return page (a
  storefront that does not send `ref` gets 404 for every guest order; a service that does not append `ref` gives
  the storefront nothing to send). `RedirectUrls.withOrderId(id)` became `withOrder(id, ref)` rather than a second
  method, so a caller cannot build a return URL without the ref. The guest path never touches
  `findByStoreMerchantIdAndId`: a blank or missing ref is a 404 before any query, so the id is never confirmed
  real. The QA "known gap" that documented the anonymous read is replaced by the possession rule (the ref is the
  credential, like a cart code) and two `[not verified]` cases — PLC-13 / SEC-03 in checkout, LUI-06 in
  landing-ui — because no guest-checkout store with Stripe was driven live in this phase.

- P6: the rule reads the effective path through `AnnotatedElementUtils` on the reflected method rather than from
  ArchUnit's annotation model, because `@GetMapping` is a meta-annotation whose `path` alias only Spring merges;
  spring-web was already a `compileOnly` of test-support, so no dependency is added for consumers, and test-support's
  own `src/test` (which existed, one test) gains `spring-web` + `spring-boot-starter-security` for the fixture
  controllers only. A gated handler that is also allow-listed fails, beyond the plan's wording, so the list can
  never claim a handler is anonymous when it is not. The `.http`/QA files are untouched: no endpoint changed.
- P6b: the rule's path join was a plain concatenation, and the services write `@RequestMapping("api/v1/signup")`
  over `@PostMapping("public/create")` — read as `api/v1/signuppublic/create`, no `/public/` segment, so every
  store-core anonymous entry failed the `CORE` check and a `/private/` segment could hide the same way on a pod.
  `effectivePaths` now combines with `AntPathMatcher.combine`, as Spring does (spring-core, already on the path),
  with a `SlashlessApi` fixture and one case per policy. That is the one change outside a `*ArchitectureTest`.
  sso-core is scored as `CORE` although its chain is its own: `publicApiSecurity` permits `/api/v1/public/**` and
  each shell authenticates the rest, which is the store-core shape — except two handlers a shell opens by an
  explicit matcher outside `/public/` (`/api/v1/auth/me` on cua, `/api/v1/auth/link-confirm` on uaa). The rule has
  no tier for "open by matcher"; they sit in `AUTHENTICATED_ONLY` (the no-token tier) with the matcher named in
  the comment, rather than widening the rule for two entries. Two modules are left red on purpose (see Phase 6b):
  an entry for a `/private/` handler is refused by the rule and an entry for A10 is one nobody can justify in a
  line, and the plan does not ask for a green build ahead of the fix.
- P2: `PaymentApiTestSupport.s2s()` minted its token with `resource=payment`, which the ungated gateway never compared
  to anything, and no payment integration class named the pod at all (`pod-info.pod.name` is unset under
  `test-stores`, so `isScopeStorePod` had a null pod). The fixture now mints `resource=pod-507f1f77` and every class
  importing `ExternalClientsTestConfiguration` carries `@TestPropertySource(POD_PROPERTY)`, as checkout's do — the
  gate is not loosened. The two
  `supported-*` enum reads on `PaymentConfigurationController` stay ungated (authenticated by the chain, nothing
  tenant-scoped behind them) as the existing test already asserted; the walk carries them as an explicit allow-list.
  `AuthApiIntegrationTest`, the one consumer of `AuthController`, is deleted with it. The webhook `.http` is written
  against today's behaviour (200 to everything, the signature checked on the outbox); P5 adds its 4xx blocks.
  `http-client.private.env.json.example` gains `WEBHOOK_SECRET` and `STRIPE_SIGNATURE`. No `Tokens` helper was added:
  `s2s(scope, resource)` and `shopper(store, sub)` already mint the foreign-pod and shopper principals.
- P3: the handler walk in `TenancyApisTest` carries an explicit `AUTHENTICATED_ONLY` list (`OrgMemberApi.accept`,
  `UserAccountApi.current`, `UserAccountApi.assignableRoles`) rather than gating those three: the filter chain
  already authenticates every non-public path and there is no store or org to check, so a token would be
  decoration. The list is asserted both ways (an entry that gains a gate fails), which is the P6 shape in
  miniature. `AuthApi.current` does get `isAuthenticated()` as planned, since it is the identity endpoint.
- P3: the super-admin 200 case needs a store the operator can close without stealing one from another test, so
  `data-test-stores.sql` gains `11111111111111111111bb05` on data.sql's second organization.
- P3: `tenancy-qa.md` §SEC and §99 still said `isOrgAdmin` ignores the store — the same stale claim as A16 — and
  are rewritten around `DELEGATED` in the same PR.

## Verification

Per phase: `./gradlew checkstyleMain checkstyleTest checkstyleIntegrationTest`, `build -x test -x check`, the
module `:test`, `integrationTest` with Docker for P2–P5, P8, P9, then `extra/scripts/verify-before-push.sh` for the
receipt. P4 also builds and lints landing-ui.

- P1 (2026-09-09): `:store-commons:autoconfigure:test` and checkstyle — recorded below once run;
  `verify-before-push.sh` before the push.
- P4 (2026-09-09): `:store-pod:checkout:checkout-core:test` 188/0 failed, `:checkout-service:test` 24/0,
  `:checkout-service:integrationTest` 77/0 (Docker), checkstyle main/test/integrationTest clean on both modules;
  landing-ui `npm run lint` 0 errors (4 pre-existing warnings in `banner-image.tsx` / fashion `PosterImage.tsx`,
  untouched), `npm run build` clean across libs → storefront, `npm run test` clean. Not driven through a live
  stack; `verify-before-push.sh` before the push.
- P6 (2026-09-09): `:store-commons:test-support:build` (compile, checkstyle main/test, verifyTestNaming, 15 tests /
  0 failed — 14 new against the fixture controllers), `:store-pod:checkout:checkout-service:test` 26/0 (the two new
  ArchUnit cases included), checkstyle main/test and verifyTestNaming clean on checkout-service. Negative probe by
  hand: removing `CheckoutApi#status` from the allow-list fails with "has no @PreAuthorize: add one, or list
  ... in the anonymous allow-list of CheckoutArchitectureTest"; a `CheckoutApi#gone` entry fails the live-list rule.
  Test-only change, nothing to drive through a stack; `verify-before-push.sh` before the push.
- P6b (2026-09-09): `:store-commons:test-support:test` 17/0 (2 new, the slashless join), checkstyle main/test clean;
  `:test` + `:checkstyleTest` per module — merchant 18/0, inventory 27/0, catalog 68/0, content 65/0, cua 78/0,
  billing 334/0, pod-registry 70/0, sso-core 687/0; payment 29/1 and tenancy 200/1, the one failure in each being
  `HANDLERS_ARE_GATED_OR_ANONYMOUS` on exactly the A2/A10/A17 handlers named in Phase 6b, expected to clear when
  `fix/payment-initiate-gate` (plus A17) and `fix/tenancy-store-delete-gate` merge. Run first with every allow-list
  empty: 63 ungated handlers surfaced, 55 of them in the register; the 8 left over are the findings above.
  Test-only change (plus the rule's join), nothing to drive through a stack; `verify-before-push.sh` before the push.
- P1 (2026-09-09): `:store-commons:autoconfigure:test` 257 tests green, checkstyle main and test clean;
  `extra/scripts/verify-before-push.sh` before the push.
- P2 (2026-09-09): `:store-pod:payment:payment-service:checkstyleMain checkstyleTest checkstyleIntegrationTest`
  clean; `:store-pod:payment:payment-service:test` and `:store-pod:checkout:checkout-service:test` green (checkout
  mocks the client, unchanged); `:store-pod:payment:payment-service:integrationTest` with Docker green, including
  the three new gate cases. Not driven through a stack: SEC-06 and SEC-08 in `payment-qa.md` are `[not verified]`.
- P3 (2026-09-09): `:store-core:tenancy:tenancy-service` checkstyle main, test and integrationTest clean; `:test`
  211 tests green (`TenancyApisTest` 24, 13 of them the handler walk); `:integrationTest` 125 tests green
  (`StoreLifecycleApiIntegrationTest` 12, `AuthApiIntegrationTest` 4) with Docker. The store-admin 403 is
  `[not verified]` through the stack: `test-stores` seeds no store-level login (tenancy-qa SEC-07, LIF-08).

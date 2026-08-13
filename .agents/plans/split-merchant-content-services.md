# Split Merchant and Content Services

## Summary

Extract CMS content from `merchant-service` into an independent pod service named `content` on port `8121`.
Preserve existing API payloads, permissions, and tenant scoping. Make `/spg/content/**` canonical while
supporting legacy `/spg/merchant/**` content paths indefinitely through SPG routing.

## Implementation Changes

### Service and module boundary

- Move `content-commons` and `content-core` from `store-pod/merchant` to `store-pod/content`.
- Rename Java packages from `com.asrevo.cvhome.merchant.content.*` to `com.asrevo.cvhome.content.*`.
- Add `content-service` following the pod backend pattern, including:
  - `ContentApplication`, security, Swagger, locale, S3/MinIO, static-content asset, and CDN path configuration.
  - `application.yml` plus `lcl`, `fargate`, and `test-stores` profiles.
  - Image name `store-pod/content`.
  - PostgreSQL and MinIO integration-test configuration.
- Do not add `content-external-api`; no Java service-to-service content contract currently exists.
- Remove `ContentApi` and the `content-core` dependency from `merchant-service`. Retain merchant storage support
  needed for branding and store assets.
- Update `settings.gradle` to register the relocated modules and new service.

### Persistence and seed data

- Give `content-service` its own `content` schema containing `sm_sequencer`, `content`, and
  `content_description`.
- Move content-specific sequence entries and all test-store content inserts into the content service resources,
  changing schema qualifiers from `merchant` to `content`.
- Remove content tables and content seed records from merchant resources; retain merchant-store, language,
  domain, branding, and merchant sequence data.
- Keep `store_merchant_id` as the tenant key without a cross-service foreign key.
- Do not provide an existing-data migration. Document that existing local/deployed databases must be recreated
  before this version is started.

### Discovery, routing, and runtime

- Register `content` at port `8121` in `common-config.yml`, local discovery, Fargate eager-load/service-port
  configuration, `run-lcl.sh`, `configure-domain.sh`, and Docker SPG `extra_hosts`.
- Add canonical SPG routing: `/content*` to `content:8121`, stripping the service prefix.
- Within the existing `/merchant*` handler, route these legacy paths to content after stripping `/merchant`:
  - `/api/v1/content/**`
  - `/api/v1/private/content/**`
  - `/api/v1/private/files`
- Route all other `/merchant/**` traffic to merchant as today. Give legacy content requests content-specific
  tracing while retaining the normal trace response headers.
- Keep the compatibility aliases indefinitely; do not add sunset headers.
- Update the authoritative `project-structure` skill and its service, configuration, gateway, discovery,
  database, and pod references to show two services.

### Clients and API contract

- Keep `ContentApi`'s internal `/api/v1` mappings, request/response models, error codes,
  `StoreMerchantId`/`LanguageCode` arguments, and `STORE-POD.CONTENT.*` permission unchanged.
- Change Angular seller-core content URLs and model comments from `/spg/merchant` and merchant modules to
  `/spg/content` and content modules.
- Change the Next.js shared content service from `storeBaseServiceUrl("merchant", ...)` to `"content"` without
  changing its existing fail/degrade behavior.
- Update stale store-client content ownership URLs to `/spg/content` without expanding or repairing those legacy
  endpoint contracts.
- Add `content-service/http/content-api.http` with runnable gateway requests for every public/private CRUD,
  file, and image endpoint, plus representative legacy-alias checks.

## Test and Rollout Plan

- Add content integration tests proving:
  - The application starts with PostgreSQL and MinIO.
  - Content DDL initializes only the `content` schema.
  - Public reads work, authorized mutations work, missing content returns the typed error, a second store cannot
    access the first store's content, and an unauthorized mutation returns 403.
- Keep and run the merchant context test to prove it starts without content modules or beans.
- Add `qa/split-merchant-content-services.md`, initially tagging cases accurately and updating them after
  execution. Cover canonical and legacy routes, seller CMS CRUD, storefront page/box rendering, tenant
  isolation, permission denial, and the documented local media/MinIO gap.
- Run:
  - `./gradlew checkstyleMain checkstyleTest`
  - `./gradlew build -x test -x check`
  - Merchant and content service tests, then `./gradlew test`
  - `npm run build` in `store-core/seller-ui`
  - Root `npm run build` in `store-pod/landing-ui`
- Exercise the running stack through both gateways. Verify `/spg/content/**` and legacy
  `/spg/merchant/**` return equivalent content, while merchant store/router endpoints remain on merchant.
- Deployment order: publish and register the content ECS/Cloud Map service first, deploy updated SPG routing
  second, then deploy merchant and both frontends. Existing databases must be recreated before startup.

## Assumptions

- Port `8121` is reserved for content.
- Fresh databases are acceptable; production content preservation is explicitly out of scope.
- Legacy compatibility is guaranteed only through SPG, not by calling merchant's direct port.
- Permission tokens and wire formats remain backward compatible.

---

# Follow-up: Modernize Merchant After the Content Split

## Summary

Bring merchant in line with the current `project-structure` conventions now that content is independent. Preserve
existing HTTP routes and JSON shapes while modernizing module boundaries, tenant handling, permissions, typed service
contracts, persistence, and tests.

## Implementation Changes

### Module ownership and legacy cleanup

- Keep API models and local error types in `merchant-commons`; rename the misleading non-JPA
  `MerchantStoreEntity` DTO base.
- Keep JPA entities, repositories, mappings, and store/branding/domain logic in `merchant-core`. Move routing, TLS
  lookup, and domain-header business logic out of `merchant-service`; the service module retains controllers and
  infrastructure adapters only.
- Retain `store-cms-commons` solely for merchant branding assets. Replace `TempConfig` with merchant-specific asset
  configuration and remove unused product-file beans.
- Prune unused load-balancer, REST client, OAuth client, cache, MapStruct, validation, and product-image dependencies
  after verifying references.
- Remove generated `HELP.md` files, obsolete local compose/native-image files, unused merchant auth/locale/Swagger
  copies already supplied centrally, and stale root-level `.http` files targeting old ports or catalog/checkout
  endpoints.
- Correct `project-structure` references so merchant is no longer the stale worked example containing content classes,
  and document the actual module ownership consistently.

### APIs, tenancy, and authorization

- Keep all current merchant URLs and response JSON compatible. Make `/api/v1/store?store=&lang=` the canonical public
  read and retain `/api/v1/store/{code}` as a compatibility alias that rejects a path/query store mismatch with a
  named error.
- Replace raw store, organization, and language identifiers in merchant DTOs and Java contracts with
  `StoreMerchantId`, `ManagerOrgId`, and `LanguageCode`, using Jackson serialization annotations to preserve the
  existing flat string JSON.
- Make every update use the resolver-provided `StoreMerchantId`; never select the tenant from the request body.
  Preserve immutable store id, organization, and domain fields while applying updates.
- Replace the inline `or hasAnyAuthority('SCOPE_STORE_CORE')` with a registered `STORE-POD.MERCHANT.READ` permission
  handled by `CustomPermissionEvaluator` and `PermissionAccessChecker`. Keep mutations on
  `STORE-POD.MERCHANT.*`.
- Change store-creation permission targeting from raw `String` organization ids to `ManagerOrgId`. Public store and
  SPG router endpoints remain intentionally unauthenticated; every private endpoint remains declaratively
  permission-gated.
- Return named merchant errors for missing stores, mismatched tenant context, duplicate domain allocation, unreadable
  uploads, and protected default-store deletion. Do not introduce another advice or hand-built problem response.
- Make branding uploads update database metadata only after storage succeeds, preserve existing metadata on upload
  failure, and close streams deterministically.

### Typed external contract and persistence

- Replace `Map<Object,Object>`, `Map<String,Object>`, and raw store strings in `MerchantStorePodClient` with typed
  provisioning/read models from merchant commons. Adapt tenancy's existing flat create JSON into the typed pod request
  without changing the seller-facing payload.
- Follow the external-API error pattern: separate server-side and caller-side interfaces where checked failures differ,
  publish `MerchantApiErrors.CATALOG`, and define caller-side not-found, conflict, and unavailable exceptions.
- Pass the merchant error catalog to every `buildClient(...)` call and update tenancy, catalog, checkout, payment, and
  CUA callers to handle or propagate the declared conditions.
- Make pod store creation idempotent for outbox retries: the same store id and organization returns success; an
  existing id owned by a different organization returns the typed conflict.
- Remove the unused `sm_sequencer` table and legacy reference/catalog/checkout sequence seed rows. Keep only
  merchant-owned tables and demo-store data.
- Add DDL constraints for globally unique routed domains, per-store collection uniqueness, and the persisted `Theme`,
  `ColorTheme`, and `DomainType` enum values. Do not introduce cross-service foreign keys.

### Requests, documentation, and QA

- Add `http/merchant-store-api.http`, `http/external-merchant-store-api.http`, and
  `http/router-controller.http`, covering every endpoint through the gateway, canonical and compatibility reads,
  typed failures, permission denial, and second-store isolation.
- Append merchant-modernization cases to `qa/split-merchant-content-services.md`; do not create another QA document.
- Update landing-ui to use the canonical query-scoped store read while retaining the legacy server alias for
  compatibility.
- Update this plan's summary, rollout notes, and verification record as the modernization phase lands.

## Test Plan

- Add unit tests for DTO serialization compatibility, mapping, immutable tenant fields, path/query mismatch, missing
  stores, domain allocation, upload failure ordering, and idempotent provisioning.
- Expand merchant integration tests with PostgreSQL and MinIO: schema ownership, constraints, public reads, authorized
  mutations, store-core reads/creation, 403 denial, typed problem details, and cross-store isolation.
- Add external-client translation tests and update tenancy provisioning tests for success, retry after timeout,
  conflicting ownership, unavailable pod, and typed remote failures.
- Run merchant, tenancy, catalog, checkout, payment, and CUA affected tests; then:
  - `./gradlew checkstyleMain checkstyleTest`
  - `./gradlew build -x test -x check`
  - `./gradlew test`
- Build landing-ui from its workspace root.
- Exercise the live stack through both gateways: storefront store loading, seller store settings and branding,
  domain/TLS routing, repeated provisioning, second-store isolation, and permission denial. Record every case as
  `[verified]`, `[unit only]`, or `[not verified]` in the existing QA document.

## Assumptions and Compatibility

- Existing HTTP paths and JSON shapes are compatibility contracts and will not be removed or renamed.
- Internal Java interfaces may change because every in-repo consumer will be migrated together.
- The original split's fresh-database requirement remains; no migration for pre-split merchant/content data is added.
- Merchant continues to own branding assets and domain routing; CMS pages, boxes, files, and images remain owned by
  content.
- Baseline merchant tests and checkstyle were clean on 2026-08-13. The observed OTLP shutdown warning was
  environmental and did not fail the build.

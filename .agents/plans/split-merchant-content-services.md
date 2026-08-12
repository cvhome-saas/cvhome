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

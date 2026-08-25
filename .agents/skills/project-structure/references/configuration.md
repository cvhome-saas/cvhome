# Shared configuration

Configuration is **not** duplicated per service. The shared YAML ships inside the
`store-commons:autoconfigure` jar, and every service imports the slices it needs from the classpath.

```
store-commons/autoconfigure/src/main/resources/
├── common-config.yml            ← the service registry; imported by EVERY service, always
├── lcl-config.yml               ← local env: datasource + SimpleDiscoveryClient localhost URIs
├── fargate-config.yml           ← AWS env: datasource + ECS/Cloud Map discovery + eager LB clients
├── store-core-lcl-config.yml    ← store-core layer only, local
├── store-pod-lcl-config.yml     ← store-pod layer only, local
└── store-pod-fargate-config.yml ← store-pod layer only, AWS
```

Note the asymmetry: there is no `store-core-fargate-config.yml` — store-core needs no AWS-specific layer
overrides beyond `fargate-config.yml`.

## How a service composes them

Each service keeps a thin `application.yml` plus one file per profile, each importing classpath slices.

`store-pod/catalog/catalog-service/src/main/resources/`:

```yaml
# application.yml — always
spring:
  application:
    name: catalog                      # ← the key that ties into common-config's registry
  config:
    import: "classpath:common-config.yml"
  security: { oauth2: { client: { registration: { s2s: ... } } } }
  sql:
    init:
      schema-locations: classpath:init-sql/schema.sql
      data-locations: classpath:init-sql/data-common.sql
  jpa:
    properties:
      hibernate:
        default_schema: ${spring.application.name}     # schema-per-service
reservation:
  expiry: { minutes: 45 }              # service-specific settings live here

# application-lcl.yml
spring.config.import:
  - "classpath:lcl-config.yml"
  - "classpath:store-pod-lcl-config.yml"

# application-fargate.yml
spring.config.import:
  - "classpath:fargate-config.yml"
  - "classpath:store-pod-fargate-config.yml"
```

A store-core service is identical but swaps the layer slice:

```yaml
# store-core/tenancy/tenancy-service/application-lcl.yml
spring.config.import:
  - "classpath:lcl-config.yml"
  - "classpath:store-core-lcl-config.yml"
```

**So the composition rule is: `common-config` (always) + one environment slice + one layer slice.**

| | local | AWS |
|---|---|---|
| store-core service | `lcl-config` + `store-core-lcl-config` | `fargate-config` |
| store-pod service | `lcl-config` + `store-pod-lcl-config` | `fargate-config` + `store-pod-fargate-config` |

Profiles: **`lcl`** (local dev), **`fargate`** (AWS), and **`test-stores`** — an extra per-service
`application-test-stores.yml` that seeds demo store data.

## `common-config.yml` — the service registry

The single source of truth for **every service's name, domain, port, namespace, and fronting gateway**:

```yaml
com.asrevo.cvhome:
  app:
    domain: gateway.com
    sub: [www, console-ui, uaa]
  pod:
    domain: spg-507f1f77.gateway.com
  services:
    uaa:        { name: uaa,        domain: uaa.${...app.domain}, port: 8001,
                  namespace: store-core.cvhome.lcl,          gateway-service-name: store-core-gateway }
    store-core-gateway: { ..., port: 8000, namespace: store-core.cvhome.lcl, gateway-service-name: store-core-gateway }
    tenancy:      { ..., port: 8020, namespace: store-core.cvhome.lcl, gateway-service-name: store-core-gateway }
    console-ui:          { ..., port: 8011, namespace: store-core.cvhome.lcl, gateway-service-name: store-core-gateway }
    spg:        { ..., port: 80,   namespace: store-pod-507f1f77.cvhome.lcl, gateway-service-name: spg }
    merchant:   { ..., port: 8120, namespace: store-pod-507f1f77.cvhome.lcl, gateway-service-name: spg }
    content:    { ..., port: 8121, namespace: store-pod-507f1f77.cvhome.lcl, gateway-service-name: spg }
    catalog:    { ..., port: 8122, ... }
    checkout:   { ..., port: 8123, ... }
    cua:        { ..., port: 8124, ... }
    payment:    { ..., port: 8125, ... }
    landing-ui: { ..., port: 8110, ... }
  crypto:
    type: LOCAL
```

Two fields do real work at runtime, not just documentation:

- **`namespace`** — `store-core.cvhome.lcl` vs `store-pod-<id>.cvhome.lcl`. `ServiceUrlBuilder` compares the
  caller's namespace against the target's to decide between a direct `lb://<service>` call and a
  gateway-routed one. See `service-to-service.md`.
- **`gateway-service-name`** — which edge fronts that service (`store-core-gateway` or `spg`) when it is
  reached from outside its namespace. This is what makes `merchant` reachable as
  `spg-507f1f77.gateway.com/merchant` in addition to `merchant.gateway.com:8120` —
  see `gateways-and-local-domains.md`.

`crypto.type: LOCAL` selects the `secret-crypto` provider (KMS in AWS).

**To change a port, host, or namespace, edit `common-config.yml`.** Never hardcode a port in a service.

## `lcl-config.yml` — local environment

- Datasource: `jdbc:postgresql://localhost:5432/cvhome`, `postgres`/`password`
- `spring.cloud.discovery.client.simple.instances` — every service registered with a hardcoded
  `http://localhost:<port>` URI, consumed by Spring's `SimpleDiscoveryClient` /
  `SimpleReactiveDiscoveryClient`. This is what makes `lb://catalog` resolve without any discovery
  infrastructure locally. **A new service needs an entry here as well as in `common-config.yml`** —
  see `service-discovery.md`.

## `fargate-config.yml` — AWS environment

- Datasource built from `${spring.datasource.host/port/database}` (injected per task)
- `spring.cloud.loadbalancer.eager-load.clients` — pre-warms LB state for all services
- `spring.cloud.ecs.discovery` — namespace + namespace-id for **AWS Cloud Map**, `enabled: true`,
  `default-port: 8080`, and a `service-ports` map repeating the port per service. Consumed by
  `ecs-service-discoveryclient` (`EcsDiscoveryClient`). Both `enabled` and `namespace` must be present for
  those beans to exist — their absence in `lcl-config.yml` is what keeps the module dormant locally.

Same `lb://` URLs as local; only the discovery client behind them changes (`service-discovery.md`).

## `store-pod-lcl-config.yml` / `store-pod-fargate-config.yml` — the pod layer

Both set the **multi-issuer JWT list** — the reason pod services accept tokens from both authorization servers:

```yaml
spring.security.oauth2.resourceserver.jwt.issuer-uri-set:
  - ${...services.uaa.schema}://${...services.uaa.domain}                       # seller/admin
  - ${...services.uaa.schema}://${...services.uaa.domain}:${...services.uaa.port}
  - ${...services.spg.schema}://${...services.spg.domain}/cua                   # shopper
  - ${...services.spg.schema}://${...services.spg.domain}:${...services.spg.port}/cua
```

Note these interpolate from `common-config.yml`, so fixing a port there fixes issuer validation too. Details in
`authentication.md`.

`store-pod-lcl-config.yml` additionally configures local **MinIO** as the CDN/storage provider
(`cdn.storage.provider: MINIO`, `s3-url: http://localhost:9000`, `minioadmin`/`minioadmin`) and the local
`pod-info.pod` identity (id `507f1f77bcf86cd799439011`, `pod-507f1f77`, endpoint
`http://spg-507f1f77.gateway.com`, type `EXTERNAL`).

## `store-core-lcl-config.yml` — the core layer

Declares the list of **pods store-core knows about** — locally a single entry with the same id/endpoint that
`store-pod-lcl-config.yml` claims for itself:

```yaml
com.asrevo.cvhome.pods:
  - id: 507f1f77bcf86cd799439011
    name: pod-507f1f77
    endpoint: { endpoint: http://spg-507f1f77.gateway.com, type: EXTERNAL }
    domain: spg-507f1f77.gateway.com
```

`endpoint.type` (`INTERNAL` / `EXTERNAL`) is what `ServiceUrlBuilder.getServiceUrl(Pod)` switches on when
store-core calls into a pod. In production these come from the control plane, not config.

## Typed properties

`common-config.yml` binds to `@ConfigurationProperties` records in
`store-commons/autoconfigure/.../s2s/model/`: `ServiceDomainProperties` (the registry),
`AppProperties`, `PodProperties`, `PodInfoProperties`, `CdnProperties`, `CdnStorageProperties`,
`OAuth2ClientProperties`, `AdminUserProperties`, `StripeProperties`, `StoreProductImageProperties`,
`TestStoreProperties`. Inject these rather than `@Value`.

## Database schemas

`common-config.yml` sets `spring.sql.init.mode: always`, `spring.datasource.hikari.schema:
${spring.application.name}` and `spring.jpa.hibernate.ddl-auto: update` for everyone. Each service then owns a
Postgres schema and ships its own DDL — `init-sql/schema.sql` + `init-sql/data-common.sql` for pod services,
plain `schema.sql` for tenancy (which owns three schemas: `tenancy`, `org`, `tenancy_outbox`).

Full detail — the JDBC vs JPA split, conventions in the DDL, and the outbox tables: `database-schemas.md`.

## Where to change what

| Change | File |
|---|---|
| A service's port, domain, namespace, or fronting gateway | `common-config.yml` |
| Local DB credentials / localhost service URIs | `lcl-config.yml` |
| AWS Cloud Map namespace, per-service ports in ECS | `fargate-config.yml` |
| Which token issuers pod services trust | `store-pod-{lcl,fargate}-config.yml` |
| Local object storage (MinIO) or pod identity | `store-pod-lcl-config.yml` |
| Which pods store-core can reach locally | `store-core-lcl-config.yml` |
| One service's own settings (timeouts, business rules, outbox) | that service's `application.yml` |

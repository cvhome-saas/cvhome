# Creating a new service

Three shapes exist in this repo, and the first decision is which one you are building:

| Shape | Example | What you create |
|---|---|---|
| **BE** — backend only | `catalog`, `payment`, `tenancy` | Gradle Java modules + a Spring Boot `-service` |
| **FE** — frontend only | `seller-ui`, `landing-ui` | One Gradle module wrapping an npm app (`ui-conventions`) |
| **BE+FE** — one deployable serving both | `uaa`, `cua` | A Java module whose npm app lives *inside* its resources, **not** a Gradle module |

The second decision is which tree: `store-core/` (one shared platform instance — identity, tenants, billing,
admin UI) or `store-pod/` (deployed once per pod, per-tenant business runtime). That choice decides the layer
config slice, the fronting gateway, and the s2s client id — get it right before writing anything.

Everything below is derived from what the existing services actually do. Copy the nearest neighbour
(`payment` for a pod BE, `tenancy` for a core BE, `seller-ui` for an FE, `uaa` for BE+FE) rather than
inventing a layout.

---

## Common to every shape: register the service

Nothing resolves until the service exists in the registry. **All four of these, or `lb://` fails and the
gateway 503s.**

1. **`settings.gradle`** — add every new module to `include(...)`. It is the source of truth for what is a
   Gradle module; a folder not listed there is just a folder.

2. **`common-config.yml`** (`store-commons/autoconfigure/src/main/resources/`) — a block under
   `com.asrevo.cvhome.services`. The key **must equal `spring.application.name`**, because `common-config`
   resolves the server port as `${com.asrevo.cvhome.services.${spring.application.name}.port}`:

   ```yaml
   reporting:                                          # == spring.application.name
     name: reporting
     domain: ${com.asrevo.cvhome.pod.domain}           # core services use ${...app.domain}
     port: 8126                                        # unused port, see the table below
     schema: http
     namespace: store-pod-507f1f77.cvhome.lcl          # store-core.cvhome.lcl for core
     gateway-service-name: spg                         # store-core-gateway for core
   ```

   Ports in use: 80 spg · 8000 gateway · 8001 uaa · 8010 seller-ui · 8020 tenancy · 8110 landing-ui ·
   8120 merchant · 8122 catalog · 8123 checkout · 8124 cua · 8125 payment. Pick the next free one in the
   right band (`81xx` pod, `80xx` core) and **never hardcode it anywhere else.**

3. **`lcl-config.yml`** — a `spring.cloud.discovery.client.simple.instances.<name>` entry pointing at
   `http://localhost:<port>` with `instance-id` and the `service-name`/`instance` metadata, exactly like its
   neighbours. This is what `SimpleDiscoveryClient` resolves locally.

4. **`fargate-config.yml`** — add the name to `spring.cloud.loadbalancer.eager-load.clients` **and** the port
   to `spring.cloud.ecs.discovery.service-ports`. Missing here = resolvable locally, dead on AWS.

Then make it reachable and runnable:

- **`extra/scripts/run-lcl.sh`** — add a row to `JAVA_SERVICES` (`name|:gradle:module:path|port`) or
  `NODE_SERVICES` (`name|dir|npm-script|port|prebuild-scripts`). Order in `JAVA_SERVICES` is startup order;
  `uaa` must stay first. Without a row the service simply never starts locally.
- **`extra/scripts/configure-domain.sh`** — a `127.0.0.1 <name>.gateway.com` line if the service gets its own
  local hostname; users must re-run it with `sudo`.
- **Routing** — a `store-pod/` service gets a block in `store-pod/spg/Caddyfile`; **a `store-core/` service of
  any shape — backend, UI, or both — must be added to `GatewayRouteLocatorImpl`** (see below). Nothing outside
  its own namespace can reach a core service until that file changes.

---

## BE: a backend service

### Pod backend (the `merchant`/`catalog`/`payment` shape)

Create the module split described in `store-pod.md` — `<domain>-commons`, `<domain>-core`,
`<domain>-external-api` (only if other services must call you), `<domain>-service`:

```
store-pod/reporting/
├── reporting-commons/build.gradle       alias(libs.plugins.java.library.conventions)
├── reporting-core/build.gradle          alias(libs.plugins.java.library.conventions)
├── reporting-external-api/build.gradle  alias(libs.plugins.java.library.conventions)
└── reporting-service/build.gradle       java.application.conventions + spring.boot
                                         + spring.dependency.management (+ hibernate.orm for JPA)
```

Library modules pull dependencies `compileOnly` (`spring.data.jpa`, `jakarta.validation.api`, `lombok`,
`hibernate.core`) and `api project(':store-pod:commons:store-commons')`. **Never** depend on another pod's
`-core`/`-service` — only its `-external-api`.

`-service/build.gradle` (copy `payment-service`'s verbatim and prune):

```groovy
plugins {
    alias(libs.plugins.java.application.conventions)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.hibernate.orm)
}
group = 'com.asrevo.cvhome'
springBoot { buildInfo() }
bootBuildImage {
    environment["BP_JVM_JLINK_ENABLED"] = "true"
    environment["BP_JVM_JLINK_ARGS"] = "..."                       // copy from a neighbour
    imageName = createImageName("store-pod/reporting", project.version)
    tags = createImageTags("store-pod/reporting", project.version)
    docker { publishRegistry { url = System.getenv("REGISTRY"); ... } }
}
dependencies {
    implementation project(':store-commons:autoconfigure')          // the whole platform: security,
    implementation project(':store-commons:ecs-commons:fargate-task-info')       // resolvers, permission
    implementation project(':store-commons:ecs-commons:ecs-service-discoveryclient')  // evaluator, errors
    implementation project(':store-pod:reporting:reporting-core')
    ...
}
dependencyManagement { imports { mavenBom "org.springframework.cloud:spring-cloud-dependencies:2025.1.0"
                                 mavenBom "io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.27.0" } }
```

All versions come from `libs.versions.toml` as `libs.*` — never a literal version. CI images need no
per-service list: `./gradlew bootBuildImage` walks every module.

**Resources** — four files, mirroring `payment-service`:

```yaml
# application.yml
spring:
  application: { name: reporting }
  config: { import: "classpath:common-config.yml" }
  security: { oauth2: { client: { registration: { s2s: {
      provider: uaa, client-id: store-pod-507f1f77@service.store-pod.internal,
      client-secret: ..., authorization-grant-type: client_credentials, scope: store_pod } } } } }
  sql: { init: { schema-locations: classpath:init-sql/schema.sql,
                 data-locations: classpath:init-sql/data-common.sql } }
  jpa: { properties: { hibernate: { default_schema: ${spring.application.name} } } }

# application-lcl.yml       import lcl-config.yml + store-pod-lcl-config.yml
# application-fargate.yml   import fargate-config.yml + store-pod-fargate-config.yml
# application-test-stores.yml   data-locations += data-test-stores.sql, stores/*/*.sql
```

The **s2s client is per layer, not per service** — reuse `store-pod-507f1f77@service.store-pod.internal`
(scope `store_pod`) or `store-core@service.store-core.internal` (scope `store_core`). No new uaa client
registration is needed for a backend.

Ship `init-sql/schema.sql` (+ `data-common.sql`) yourself: schema-per-service, name it after the app, no
cross-service foreign keys. `schema.sql` is the source of truth — `ddl-auto: update` is only a net.

**Java** — the minimum is three classes, in `com.asrevo.cvhome.<domain>`:

- `<Domain>Application` — `@SpringBootApplication`, `final`, private constructor, `main` annotated
  `@lombok.Generated` (checkstyle-friendly; copy `PaymentApplication`).
- `config/SecurityConfig` — the resource-server chain: `/api/*/private/**` authenticated, everything else
  `permitAll`, `oauth2ResourceServer(jwt)`, csrf disabled, plus a `JwtAuthenticationConverter` using
  `UaaJwtGrantedAuthoritiesConverter`. Copy `payment`'s.
- `config/ClientsConfig` — one `@Bean` per remote, built with
  `restClientBuilder.buildClient("<service>", Iface.class, <ApiErrors|RemoteErrorCatalog.none()>)`.

You do **not** write a `ControllerAdvice`, argument resolvers, a JWT decoder, Swagger config, or a permission
evaluator — `store-commons:autoconfigure` (`CvhomeSharedConfig`) imports all of it, including the single
`@ControllerAdvice` error handlers. Adding a second advice is a review reject.

**Routing** — a pod service is only reachable from outside through `spg`:

- `store-pod/spg/Caddyfile` — a `handle_path /reporting*` block with a `tracing` span, the trace headers, and
  `reverse_proxy http://reporting.{$NAMESPACE}:8126`. Add it **before** the catch-all `route { ... landing-ui }`
  block, which swallows anything unmatched. Use `handle` (not `handle_path`) plus
  `header_up X-Forwarded-Prefix` only if the app must keep its prefix — `cua` is the sole case, because it is
  an issuer.
- `docker-compose-lcl.yml` — add `reporting.gateway.com:host-gateway` to `spg`'s `extra_hosts`, since Java
  services run on the host while Caddy runs in the container.

Nothing changes in the platform gateway: it already forwards `/spg/**?store=&pod=` to the whole pod.

### Core backend (the `tenancy` shape)

Same skeleton, different slices: `store-core/<name>/<name>-service` (with sibling `-commons`/`-events`/
`-external-api` libs as needed), `application-lcl.yml` imports `lcl-config.yml` + **`store-core-lcl-config.yml`**,
`application-fargate.yml` imports `fargate-config.yml` alone (there is no `store-core-fargate-config.yml`),
s2s client `store-core@service.store-core.internal` scope `store_core`, `namespace: store-core.cvhome.lcl`,
`gateway-service-name: store-core-gateway`.

Persistence in core is **Spring Data JDBC**, not JPA: `spring.boot.starter.data.jdbc`,
`org.springframework.data.relational...@Table(schema = "...")`, DDL at `src/main/resources/schema.sql`
(+ `data.sql`), no `hibernate.orm` plugin, no `default_schema` property.

**Routing — mandatory for every `store-core/` service.** See the section below; skipping it is the usual cause
of "the service is up, `lb://` resolves, but the browser gets seller-ui's HTML back".

### Routing a store-core service: `GatewayRouteLocatorImpl`

`store-core/gateway/gateway-service/src/main/java/com/asrevo/cvhome/gateway/config/GatewayRouteLocatorImpl.java`
is the platform edge's whole route table, and it is hand-written — there is no discovery locator
(`spring.cloud.gateway.server.webflux.discovery.locator.enabled: false`). Today:

```java
private static final String[] backendServices = {"tenancy", "uaa", "spg"};

private static final String[] backendServicesPattern = Arrays.stream(backendServices)
        .map(it -> String.format("/%s/**", it))
        .toArray(String[]::new);
...
return routeLocatorBuilder.routes()
        .route(r -> r.path("/tenancy/**")
                .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
                .uri("lb://tenancy"))
        .route(r -> r.path(backendServicesPattern)          // ← everything NOT a backend path
                .negate()
                .and()
                .host(storeCoreGatewayDomain, "www." + ..., "seller-ui." + ...)
                .uri("lb://seller-ui"))                     // ← falls through to the UI
        .build().getRoutes();
```

**Two edits, and the second is the one people forget:**

1. Add the route itself, in the same shape as `tenancy` — the filter chain matters:
   `stripPrefix(1)` removes the `/reporting` prefix so the service sees its own paths, `tokenRelay()` swaps
   the browser's gateway session for a bearer token (a service that skips it gets 401s on every authenticated
   call), `preserveHostHeader()` keeps tenant/host resolution working downstream.

   ```java
   .route(r -> r.path("/reporting/**")
           .filters(f -> f.stripPrefix(1).tokenRelay().preserveHostHeader())
           .uri("lb://reporting"))
   ```

2. **Add `"reporting"` to the `backendServices` array.** That array is turned into `/<name>/**` patterns and
   `negate()`d to build the seller-ui catch-all, so any path *not* listed there is claimed by seller-ui —
   which is declared last but matches first for your prefix. A route added in step 1 without step 2 is
   shadowed: requests reach seller-ui, which returns its shell HTML, and the failure looks like a frontend
   bug rather than a routing one.

A **UI** service in `store-core` is routed by host, not path: add its hostname to the `.host(...)` list on the
seller-ui route (it is served by whichever `lb://` that route names), or give it its own
`.route(...).host("reporting-ui." + storeCoreGatewayDomain).uri("lb://reporting-ui")` **before** the catch-all.
Either way the hostname also needs `configure-domain.sh` and `com.asrevo.cvhome.app.sub`.

Pod services need none of this — the gateway already forwards `/spg/**?store=&pod=` to the whole pod via
`PodClient`, which rebuilds its routes from tenancy every minute.

### Permissions

If the service introduces a new permission token, both halves are mandatory or every call silently 403s
(the evaluator denies by default):

1. a constant + `case` in `CustomPermissionEvaluator` (`store-commons/autoconfigure`,
   `com.asrevo.cvhome.s2s.config.internal`) — `STORE-POD.<DOMAIN>.*` goes in the pod switch,
   `STORE-CORE.<DOMAIN>.<ACTION>` in the core switch;
2. the matching method on `PermissionAccessChecker` (`com.asrevo.cvhome.s2s.services`).

Endpoints then follow `api-conventions.md`: `StoreMerchantId merchantStore` + `LanguageCode language`,
`@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.REPORTING.*')")`.

---

## FE: a frontend-only service

The Gradle side is deliberately tiny — the module is a wrapper, all real config is npm's:

```groovy
// store-core/reporting-ui/build.gradle
plugins {
    alias(libs.plugins.ui.conventions)
    alias(libs.plugins.node)
}
bootBuildImage { imageGroup = "store-core" }        // "store-pod" under store-pod/
```

`ui-conventions` wires `gradle build` → `npm run build`, `gradle bootRun` → `npm run dev`, pins Node/npm
(downloaded, so no local install needed), and pulls in `docker-conventions`, whose `bootBuildImage` runs a
plain `docker build .`. So the module **must contain a `Dockerfile`** — seller-ui's is four lines: copy
`dist/`, `CMD node server/server.mjs`, `EXPOSE 8010`.

Requirements the plugin imposes on `package.json`: a `build` script, and a `dev` script if you want
`gradle bootRun` (seller-ui only has `start`, which is why `run-lcl.sh` names the script per row —
`seller-ui|store-core/seller-ui|start|8010|`).

Register it in the four config files exactly like a backend — a UI service is discovered via `lb://` too
(`GatewayRouteLocatorImpl` routes to `lb://seller-ui`), so it needs the `common-config` block, the
`lcl-config` instance, and the `fargate-config` entries.

Browser-facing extras:

- **`com.asrevo.cvhome.app.sub`** in `common-config.yml` — add the subdomain. `AppProperties.getUrls()`
  expands `sub × handlers` into the redirect-uri/allow-list that `OAuth2ClientDatabaseInitializer` seeds into
  uaa's `web-app` client. A UI that logs in through the gateway and is not listed there gets
  `invalid_redirect_uri`.
- **`configure-domain.sh`** — `127.0.0.1 reporting-ui.gateway.com`.
- **Gateway route** — a UI under `store-core/` still goes in `GatewayRouteLocatorImpl`: extend the host list on
  the seller-ui catch-all, or add an explicit host route to `lb://reporting-ui`; if you route it by path
  instead, `backendServices` needs the name too. Details and the shadowing trap: *Routing a store-core
  service* above.
- **Don't hold tokens.** Follow seller-ui: `environment.ts` sets `apiUrl: ''` and
  `LOGIN_URL: '/oauth2/authorization/uaa'`, and the app makes same-origin relative calls — the gateway owns
  the session and relays the token.

For a storefront *theme* rather than a new UI service, stop here and use `new-landing-ui-template.md`.

---

## BE+FE: one deployable serving both

`uaa` and `cua` are single Java modules that also serve UI. Two sub-shapes:

**Thymeleaf** (`cua`, uaa's login pages): add `spring.boot.starter.thymeleaf` +
`thymeleaf.extras.springsecurity6`, put templates in `src/main/resources/templates/`. Nothing else — no npm,
no extra Gradle wiring.

**Embedded SPA** (`uaa`): the Angular app lives at `src/main/resources/<name>-fe`, is **not** in
`settings.gradle`, and has no `build.gradle` of its own. The host module adds `alias(libs.plugins.node)` and:

```groovy
node { version = '23.8.0'; download = true; nodeProjectDir = file('src/main/resources/uaa-fe') }

tasks.register('copyAngularApp', Copy) {
    dependsOn('npm_run_build')
    from("${projectDir}/src/main/resources/uaa-fe/dist/uaa-fe/browser")
    into "${project.projectDir}/src/main/resources/static"
}
tasks.named('processResources') { dependsOn(tasks.named('copyAngularApp')); exclude 'uaa-fe/**' }
tasks.named('clean')            { delete "${project.projectDir}/src/main/resources/static" }
```

`static/` is generated output — never hand-edit, never commit it. Iterate with `ng serve` inside the `-fe`
folder. The service registers once, on one port: the SPA is same-origin with its API, so there is no CORS
config and no second `common-config` entry.

Pick this shape only when the UI is inseparable from the backend (login/consent screens, an admin console for
that service's own data). Anything a user navigates to as a product surface belongs in its own `-ui` module.

---

## Checklist

```
[ ] settings.gradle: every new module listed, each applying a build-logic convention plugin
[ ] common-config.yml: services.<name> block, key == spring.application.name, free port
[ ] lcl-config.yml: simple discovery instance
[ ] fargate-config.yml: eager-load client + service-ports entry
[ ] run-lcl.sh: JAVA_SERVICES or NODE_SERVICES row (right startup position)
[ ] Routing, pod: spg Caddyfile block placed BEFORE the landing-ui catch-all,
    + the hostname in spg's extra_hosts in docker-compose-lcl.yml
[ ] Routing, core (BE, FE or both): GatewayRouteLocatorImpl route with
    stripPrefix(1).tokenRelay().preserveHostHeader() AND the name in backendServices
    (omit it and seller-ui's catch-all swallows the path)
[ ] configure-domain.sh: new local hostname, if any
[ ] BE: application.yml + -lcl + -fargate (+ -test-stores), layer-correct s2s client and slices
[ ] BE: schema.sql / init-sql/schema.sql written by hand, schema named after the app
[ ] BE: Application + SecurityConfig + ClientsConfig only — no hand-rolled advice/resolvers
[ ] New permission token: CustomPermissionEvaluator case + PermissionAccessChecker method
[ ] FE: Dockerfile present, package.json build (and dev) scripts, imageGroup set
[ ] FE: app.sub entry if it is browser-facing behind uaa login
[ ] Versions via libs.versions.toml only
[ ] ./gradlew checkstyleMain checkstyleTest && ./gradlew build -x test -x check clean
[ ] ./extra/scripts/run-lcl.sh start --list shows it; a real run starts it
```

# Service discovery: one `lb://` abstraction, two implementations

Nothing in business code knows an address. Callers pass a **logical service name**, `ServiceUrlBuilder` turns it
into an `lb://` URL, and a Spring Cloud `DiscoveryClient` resolves that name to real instances. **Only the
discovery client changes between local and AWS** — the URL, the code and the config keys above it are identical.

```
RestClientBuilder.buildClient("catalog", Iface.class)
        ↓  ServiceUrlBuilder            (service-to-service.md)
   "lb://catalog"
        ↓  @LoadBalanced RestClient.Builder → Spring Cloud LoadBalancer
        ↓  DiscoveryClient.getInstances("catalog")
   lcl     → SimpleDiscoveryClient          ← static list in lcl-config.yml
   fargate → EcsDiscoveryClient             ← AWS Cloud Map, ecs-service-discoveryclient
```

The `lb://` scheme only works on a `@LoadBalanced` builder. `WebClientServicesConfig` declares both kinds:
`microServiceRestClientBuilder` / `microServiceWebClientBuilder` are `@LoadBalanced` (used by
`buildClient(String, …)`), while `defaultRestClientBuilder` / `defaultWebClientBuilder` are plain, for absolute
URLs — which is why `buildClient(Pod, …)` uses the plain one: an `EXTERNAL` pod endpoint is a real URL, not a
service name. Both variants carry the `s2s` OAuth2 interceptor (`authentication.md`).

## Local — Spring's simple discovery client

`lcl-config.yml` *is* the registry. No Eureka, no Consul, no sidecar:

```yaml
spring.cloud.discovery.client.simple.instances:
  uaa:
    - uri: "http://localhost:8001"
      instance-id: uaa-1
      metadata: { service-name: uaa, instance: uaa-1 }
  catalog:
    - uri: "http://localhost:8122"
      ...
```

Every service is listed with its hardcoded localhost URI (`uaa` 8001, `store-core-gateway` 8000, `seller-ui`
8010, `control-plane` 8020, `landing-ui` 8110, `merchant` 8120, `catalog` 8122, `checkout` 8123, `cua` 8124,
`payment` 8125). Spring Cloud Commons auto-configures the matching client from those properties:

| App type | Bean | Where |
|---|---|---|
| Servlet (all MVC services) | `SimpleDiscoveryClient` | `SimpleDiscoveryClientAutoConfiguration` |
| Reactive (`gateway-service`, WebFlux) | `SimpleReactiveDiscoveryClient` | `SimpleReactiveDiscoveryClientAutoConfiguration` |

Both read the same `simple.instances` map — that is why the gateway needs no special local configuration.
Instances are `DefaultServiceInstance`s populated through `setUri(...)`, which derives host, port and `secure`
from the URI.

**Adding a service means adding an entry here**, next to its `common-config.yml` registration — otherwise
`lb://<name>` has nothing to resolve. Since the list is static, the URI must match the port in
`common-config.yml`.

### The patched `DefaultServiceInstance`

`ecs-service-discoveryclient` ships
`src/main/java/org/springframework/cloud/client/DefaultServiceInstance.java` — a copy of the Spring class in
**Spring's own package**, so it shadows the one in `spring-cloud-commons` on the classpath. Diffed against
5.0.0, the single delta is an added override:

```java
@Override
public String getScheme() {
    return this.getUri().getScheme();
}
```

Upstream inherits `ServiceInstance.getScheme()`, which returns `null`; the patched version reports the scheme
carried by the configured `uri`. Treat this file as a vendored patch — do not "clean it up", and re-check it
when bumping `spring-cloud-commons`.

## AWS — `ecs-service-discoveryclient` over Cloud Map

`store-commons/ecs-commons/ecs-service-discoveryclient` is a small home-grown Spring Cloud discovery client
backed by **AWS Cloud Map** (`software.amazon.awssdk.services.servicediscovery`). It is a dependency of every
`-service` module but stays inert unless enabled.

Activated by `fargate-config.yml`:

```yaml
spring.cloud.ecs.discovery:
  namespace: "store-pod-507f1f77.cvhome.lcl"     # the Cloud Map namespace this task lives in
  namespace-id: "ns-je7qri6wn7fbsrpn"
  enabled: true
  default-port: 8080
  service-ports: { uaa: 8001, store-core-gateway: 8000, seller-ui: 8010, control-plane: 8020,
                   landing-ui: 8110, merchant: 8120, catalog: 8122, checkout: 8123, cua: 8124, payment: 8125 }
```

`fargate-config.yml` also sets `spring.cloud.loadbalancer.eager-load.clients` for every service, so LB state is
warmed at startup instead of on the first (latency-sensitive) call.

### Module contents

| Class | Role |
|---|---|
| `EcsDiscoveryProperties` | binds `spring.cloud.ecs.discovery.*` — `namespace`, `namespaceId`, `enabled`, `defaultPort`, `servicePorts`, `includeServices` |
| `EcsDiscoveryClient` | blocking `DiscoveryClient` (servlet apps) |
| `EcsReactiveDiscoveryClient` | `ReactiveDiscoveryClient` (the WebFlux gateway) |
| `CloudMapServiceInstance` | adapts a Cloud Map `HttpInstanceSummary` to `ServiceInstance` |
| `EcsConfig` | the auto-configuration, split servlet/reactive |
| `ConditionalOnEcsDiscoveryEnabled` | `@ConditionalOnProperty("spring.cloud.ecs.discovery.enabled")` |

Registered as a Boot auto-configuration (`AutoConfiguration.imports`), with `EcsDiscoveryProperties` also
listed in `spring.factories` as a bootstrap configuration.

`EcsConfig` mirrors the servlet/reactive split of the simple client exactly:

```java
@ConditionalOnEcsDiscoveryEnabled
@ConditionalOnProperty(prefix = "spring.cloud.ecs.discovery", name = "namespace")
@ConditionalOnWebApplication(type = REACTIVE)   // …and a SERVLET twin
static class EcsReactiveDiscoveryClientAutoConfiguration {
    @Bean ServiceDiscoveryAsyncClient awsServiceDiscoveryAsync() { return ServiceDiscoveryAsyncClient.create(); }
    @Bean EcsReactiveDiscoveryClient reactiveDiscoveryClient(...) { ... }
    @Bean ReactiveDiscoveryClientHealthIndicator ...                 // actuator health, if present
}
```

**Two conditions must both hold** (`enabled: true` *and* a `namespace`), which is precisely why the module is
harmless locally: `lcl-config.yml` sets neither, so no bean is created and the simple client stays in charge.

### How lookup works

`getInstances(serviceId)` issues a Cloud Map `DiscoverInstances(namespaceName, serviceName)` and maps each
returned instance:

```java
host  = attributes["AWS_INSTANCE_IPV4"]                       // the task's ENI address
port  = attributes["AWS_INSTANCE_PORT"]                       // else servicePorts[serviceId]
                                                              // else defaultPort (8080)
secure = attributes["SECURE"] == "true"
```

So `service-ports` is a **fallback for Cloud Map registrations that don't publish a port**, not the primary
source — and each Fargate task's private IP is what the load balancer ends up calling. Scaling a service to N
tasks yields N instances with no config change; this is the "dynamic" half that the static local list fakes.

`getServices()` lists Cloud Map services filtered by `NAMESPACE_ID` (when `namespaceId` is set) and appends
`includeServices` — an escape hatch for names that must be resolvable but aren't registered in Cloud Map.

### Namespace-qualified service ids

`EcsDiscoveryClient` splits `serviceId` on the **first dot**: everything before it is the Cloud Map service
name, everything after is the namespace, falling back to the configured `namespace`. That is exactly the shape
`ServiceUrlBuilder` emits for a cross-namespace call:

```
lb://spg.store-pod-507f1f77.cvhome.lcl/merchant
     └┬┘ └──────────┬────────────────┘
   service       namespace           → DiscoverInstances(namespace, "spg")
```

so a store-core task can resolve a pod's edge in the pod's own namespace. Two caveats:

- `EcsReactiveDiscoveryClient` does **not** do this splitting — it always queries `properties.getNamespace()`.
- Locally the simple instance list has no `spg` entry and no namespace-qualified keys, so this form does not
  resolve. It doesn't need to: every name-based `buildClient(...)` in the repo today is same-namespace
  (pod → pod, gateway → control-plane), and store-core reaches a pod through the `buildClient(Pod, …)` overload,
  which uses the pod's `EXTERNAL` endpoint URL (`http://spg-507f1f77.gateway.com`) on the non-load-balanced
  builder. See `multi-tenancy.md`.

## `fargate-task-info` — the sibling module

Not discovery, but the same "AWS-only, inert elsewhere" shape: `EcsTaskFetcher.fetch()` reads the ECS task
metadata endpoint (`$ECS_CONTAINER_METADATA_URI_V4/task`) into `EcsTask`/`Container`/`Network`/`Limits`.
`autoconfigure`'s `EcsInfoConfig` exposes it as a bean plus an `EcsTaskHealthIndicator`, guarded by
`@ConditionalOnProperty(name = "AWS_EXECUTION_ENV", havingValue = "AWS_ECS_FARGATE")` — so the task identity
shows up in actuator health inside Fargate and nowhere else. `autoconfigure` depends on it `compileOnly`.

## Adding a new service to discovery — checklist

1. Register it in `common-config.yml` (name, domain, port, namespace, `gateway-service-name`).
2. Add a `spring.cloud.discovery.client.simple.instances.<name>` entry in `lcl-config.yml` with the same port.
3. Add `<name>: <port>` to `spring.cloud.ecs.discovery.service-ports` and to
   `spring.cloud.loadbalancer.eager-load.clients` in `fargate-config.yml`.
4. Add the route to its gateway (`gateways-and-local-domains.md`) and a `/etc/hosts` entry for local dev.
5. In the `-service` build: `implementation project(':store-commons:ecs-commons:ecs-service-discoveryclient')`
   (plus `fargate-task-info`), as every other service does.

## Related

- `service-to-service.md` — `ServiceUrlBuilder`, `RestClientBuilder`, `@HttpExchange` contracts
- `configuration.md` — `common-config.yml` registry, the `lcl` / `fargate` slices
- `gateways-and-local-domains.md` — the gateway paths that `lb://<gateway>.<ns>/<service>` maps onto
- `shared-libraries.md` — where `ecs-commons` sits in `store-commons`

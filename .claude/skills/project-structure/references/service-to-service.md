# Service-to-service calls

Services never hand-write HTTP calls to each other. The pattern is **declarative HTTP interfaces** (Spring 6
`@HttpExchange`) defined once in an `-external-api` module and used from both sides.

## The three pieces

### 1. The contract lives in `<domain>-external-api`

A plain interface annotated with `@HttpExchange`. No implementation, no Spring context, no `-core` dependency —
just the coordinates of the endpoint and its DTOs (which come from `<domain>-commons`).

`store-pod/catalog/catalog-external-api/.../ExternalProductReservationService.java`:

```java
@HttpExchange("/api/v1/private")
public interface ExternalProductReservationService {

    @PostExchange("/reserve/{ref}")
    ProductReservationReserveResult reserve(StoreMerchantId store, @PathVariable("ref") String ref,
                                            @RequestBody ProductReservationList productReservation);

    @PostExchange("/commit/{ref}")
    ProductReservationCommitResult commit(StoreMerchantId store, @PathVariable("ref") String ref);

    @PostExchange("/release/{ref}")
    ProductReservationReleaseResult release(StoreMerchantId store, @PathVariable("ref") String ref);
}
```

Note `StoreMerchantId store` carries **no annotation**. It is serialized by a custom argument resolver (below) —
tenant context travels on every call automatically.

### 2. The provider implements it as its controller

`catalog-service` makes its REST controller **implement the same interface**, so the server route and the client
contract cannot drift:

```java
@RestController
public class ExternalProductReservationApi implements ExternalProductReservationService { ... }
```

Same idea across the repo: `ExternalProductApi implements ExternalProductService`,
`ExternalMerchantStoreApi implements ExternalMerchantStoreService`. The `/private` path prefix and the
`External*` naming mark these as service-to-service endpoints, distinct from the public/customer APIs.

### 3. The consumer builds a proxy from it

The caller depends on `catalog-external-api` and declares beans in its own `ClientsConfig`.
`store-pod/checkout/checkout-service/.../config/ClientsConfig.java`:

```java
@Configuration
public class ClientsConfig {
    private static final String CATALOG_SERVICE_NAME = "catalog";

    @Bean
    public ExternalProductReservationService externalProductReservationService(RestClientBuilder b) {
        return b.buildClient(CATALOG_SERVICE_NAME, ExternalProductReservationService.class);
    }

    @Bean
    public ExternalProductService externalProductService(RestClientBuilder b) {
        // decorated with a caching layer
        return new CachedExternalProductService(b.buildClient(CATALOG_SERVICE_NAME, ExternalProductService.class));
    }
}
```

Business code then injects the interface like any local bean —
`checkout-core`'s `OrderInventoryOrchestratorImpl` takes an `ExternalProductReservationService` in its
constructor and calls `reserve(...)` / `commit(...)` / `release(...)` without knowing HTTP is involved.

**Caching is a decorator, not a concern of the contract:** `CachedExternalProductService` and
`CachedExternalMerchantStoreService` wrap the generated proxy. Add caching there, never in the interface.

## How the URL is resolved — `RestClientBuilder` + `ServiceUrlBuilder`

Both live in `store-commons:autoconfigure` (`com.asrevo.cvhome.s2s.config.internal`). You pass a **logical
service name** (`"catalog"`), never a URL.

`ServiceUrlBuilder` looks up both the target and the caller in `ServiceDomainProperties` (backed by
`common-config.yml`) and compares **namespaces**:

- **Same namespace** (e.g. checkout → catalog, both in `store-pod-<id>.cvhome.lcl`) → direct load-balanced call:
  `lb://catalog`
- **Different namespace** (e.g. store-core → a pod service) → route through that service's declared
  `gatewayServiceName`: `lb://spg.store-pod-<id>.cvhome.lcl/catalog`

The `lb://` scheme means Spring Cloud LoadBalancer resolves instances via the discovery client — `SimpleDiscoveryClient`
with hardcoded localhost URIs in `lcl`, and `EcsDiscoveryClient` (AWS Cloud Map) in `fargate`. Same code, different
environment. How both are wired, and what to add when introducing a service: `service-discovery.md`.

Note the two builder flavours in `WebClientServicesConfig`: `buildClient(String, …)` uses the `@LoadBalanced`
`microService*` builder (so it can speak `lb://`), while `buildClient(Pod, …)` uses the plain `default*` builder,
because an `EXTERNAL` pod endpoint is an absolute URL. Both carry the `s2s` OAuth2 interceptor.

There is a second overload, `buildClient(Pod pod, String serviceName, Class<T>)`, for calls into a **specific
tenant pod** where the target is data, not config. `ServiceUrlBuilder.getServiceUrl(Pod)` switches on
`pod.endpoint().type()`: `INTERNAL` → `lb://spg.<endpoint>`, `EXTERNAL` → the raw endpoint URL — which is what
lets a pod live in another region or account. `StorePodClientFactory` uses it to build (and cache per `PodId`) a
`MerchantStorePodClient` when provisioning a store into a chosen pod. See `multi-tenancy.md`.

## Proxy construction — `WebClientsUtils`

`WebClientsUtils.build(...)` turns the interface into a proxy via `HttpServiceProxyFactory`, over a
`RestClientAdapter` (blocking, MVC services) or `WebClientAdapter` (reactive, the gateway). It registers custom
argument resolvers that serialize domain types transparently:

`LanguageCodeSerializeParamArgumentResolver`, `StoreMerchantIdSerializeParamArgumentResolver`,
`StoreSerializeParamArgumentResolver`, `OrgSerializeParamArgumentResolver`,
`DomainSerializeParamArgumentResolver`, and `PageableSerializeParamArgumentResolver` (registered only if
`org.springframework.data.domain.Pageable` is on the classpath).

**That is why `StoreMerchantId store` needs no annotation** — the resolver turns it into the right header/param
on the way out, and matching argument resolvers on the server (`ServletStoreMerchantIdArgumentResolver`,
`ServletLanguageCodeArgumentResolver`, `ServletOrgStorePrincipalInfoArgumentResolver` in `ServletWebConfig`)
reconstitute it on the way in. It is the same convention in both directions — see `api-conventions.md`.

## Authentication on the wire

The call carries a `client_credentials` token — each service registers an `s2s` OAuth2 client against `uaa`
in its `application.yml` (see `authentication.md`).

## Adding a new cross-service call — checklist

1. Add the method to the provider's `<domain>-external-api` interface (DTOs must live in `<domain>-commons`).
2. Implement it on the provider's `External*Api` controller (it already `implements` the interface, so the
   compiler tells you).
3. In the consumer: `implementation project(':store-pod:<domain>:<domain>-external-api')`.
4. Add a `@Bean` in the consumer's `ClientsConfig` via `restClientBuilder.buildClient(SERVICE_NAME, Iface.class)`.
5. Inject the interface where you need it.

**Never** depend on another pod's `-core` or `-service`. `-external-api` (which drags in only `-commons` and
`spring-web`) is the only sanctioned coupling between pods.

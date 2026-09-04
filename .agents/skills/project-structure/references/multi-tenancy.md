# Multi-tenancy: orgs, stores, and pods

The central idea: **a store is a logical tenant; a pod is a physical deployment.** The control plane owns the
mapping between them, and everything else — routing, TLS, data isolation, regional placement — follows from it.

## Three levels of tenancy

| Level | Identifier | Lives in | Meaning |
|---|---|---|---|
| **Organization** | `ManagerOrgId` | tenancy (`tenancy` schema) | The customer account that signs up and pays. Owns stores. |
| **Store** | `StoreMerchantId` | tenancy + the pod's own DB | One storefront. The unit a shopper actually visits. |
| **Pod** | `PodId` | tenancy (`org.pod` table) | A **physical deployment** of the whole `store-pod` stack. Hosts many stores. |

A fourth unit exists inside the identity layer only: a **realm** (`RealmId`) is one user pool. On a pod's `cua`
there is one per store, so the same email in two stores is two shoppers; on `uaa` there is exactly one,
`platform`. It is the store id by value, and it is what Hibernate's `@TenantId` filters every identity query by.
See `authentication.md` — and note that "realm" also names the *issuing server* elsewhere in that file, which is
a different question entirely.

**One store id everywhere.** `StoreMerchantId` (a `String`) is the store's identifier in tenancy, billing,
pod-registry, the gateway and every pod alike. It used to be two types — store-core carried an `ObjectId`
wrapper called `ManagerStoreId` and the pods a `String` wrapper — with the permission evaluator translating
between them on every request; they held the same value, so they were merged. The value is still ObjectId hex
(`StoreMerchantId.newId()`, called only by tenancy), stored `varchar(24)` in store-core and `varchar(50)` in
the pods. It serializes as a bare string, and its deserializer still accepts the two older object shapes so
outbox rows written before the merge remain readable.

Control plane and pod still divide the work the same way: tenancy tracks *that a store exists and where*, the
pod owns the store's actual data. They now just agree on what to call it.

`PodId.shorten()` takes the first 8 chars of the ObjectId — that short form is what you see everywhere in
infrastructure naming: namespace `store-pod-507f1f77.cvhome.lcl`, domain `spg-507f1f77.gateway.com`, gateway
route id `pod-507f1f77`.

## What a pod physically is

One pod = one complete, isolated deployment of the tenant layer: `spg` (Caddy edge), `merchant`, `catalog`,
`checkout`, `payment`, `cua`, `landing-ui` — with **its own Postgres** and its own Cloud Map namespace. Nothing
in a pod is shared with another pod.

```java
public record Pod(PodId id, String name, PodEndpoint endpoint, ManagerOrgId orgId, String domain) { ... }
public record PodEndpoint(String endpoint, EndpointType type) { }   // EndpointType: INTERNAL | EXTERNAL
```

Two fields carry the architecture:

- **`orgId`** — a pod may be **dedicated to one organization** (enterprise tenant, isolated infrastructure) or
  **shared** (`listPublicPods()` — the default multi-tenant pool). This is the SaaS "shared or isolated
  infrastructure" promise from the README, expressed as one nullable field.
- **`endpoint.type`** — `INTERNAL` means the pod sits in the same cluster and is reached through service
  discovery; `EXTERNAL` means it is reached over a public URL. `ServiceUrlBuilder` switches on exactly this:

  ```java
  public String getServiceUrl(Pod pod) {
      return switch (pod.endpoint().type()) {
          case INTERNAL -> LB_PREFIX + "spg." + pod.endpoint().endpoint();   // lb://spg.<namespace>
          case EXTERNAL -> pod.endpoint().endpoint();                        // https://pod.eu.example.com
          case null     -> pod.endpoint().endpoint();
      };
  }
  ```

**This is how a store lands in a specific region.** A pod deployed in eu-central-1 and one in us-east-1 are just
two rows in `org.pod` with different endpoints. Assign a store to the EU pod and its data physically lives in
the EU — no code path changes, because every caller resolves the pod through `ServiceUrlBuilder`. An
`EXTERNAL`-typed pod can live in an entirely different account, region, or even another cloud, and store-core
still reaches it uniformly.

## Where the store → pod binding lives

`ManagerStoreEntity` (tenancy, `tenancy.manager_store`) is the authoritative record:

```java
@Table(schema = "tenancy", name = "manager_store")
public class ManagerStoreEntity extends BaseEntity<ManagerStoreEntity, StoreMerchantId> {
    private String name;
    private ManagerOrgId orgId;                    // which customer owns it
    private PodId podId;                           // ← WHICH PHYSICAL POD HOSTS IT
    private ProvisioningState provisioningState;
    private Instant createdDate;
}
```

That one `podId` column is the whole multi-tenant routing table. Everything downstream — which gateway route
matches, which database holds the products, which region serves the shopper — is derived from it.

## Provisioning: how a store gets into a pod

`ManagerStoreEntity` is an aggregate root, so the lifecycle is expressed as state transitions that register
domain events (see `events-outbox.md`):

```java
public static ManagerStoreEntity createStore(Map<Object,Object> request, ManagerOrgId orgId, PodId podId) {
    ...
    entity.provisioningState = ProvisioningState.NOT_STARTED_PROVISIONING;
    entity.registerEvent(StoreCreatedEvent.from(entity.getId(), orgId, podId, request));
    return entity;
}
public ManagerStoreEntity startProvisioning()    { ... IN_PROGRESS_PROVISIONING; registerEvent(StoreProvisionedEvent...); }
public ManagerStoreEntity completeProvisioning() { ... SUCCESSFULLY_PROVISIONING; registerEvent(...); }
public ManagerStoreEntity failProvisioning()     { ... FAILED_PROVISIONING;      registerEvent(...); }
```

The flow:

1. An org admin creates a store and picks (or is assigned) a pod. The row is written with
   `NOT_STARTED_PROVISIONING`, and `StoreCreatedEvent` goes into the outbox **in the same transaction**.
2. The outbox delivers it to `ManagerStoreCreatedEventImpl`, which calls `StoreProvisioningService.provisioning(...)`.
3. `StoreProvisioningService` marks `IN_PROGRESS`, then calls into the target pod:

   ```java
   podClientFactory.getMerchantStorePodClient(pod).create(newRequest);
   internalStoreService.completeProvisioning(store);
   ```

   On exception it marks `FAILED_PROVISIONING` and rethrows, so the outbox retries.
4. `StorePodClientFactory` builds (and caches per `PodId`) a `MerchantStorePodClient` aimed at that specific pod:

   ```java
   Pod pod = serviceDomainProperties.getPodByPodId(podId).orElseThrow(...);
   return restClientBuilder.buildClient(pod, "merchant", MerchantStorePodClient.class);
   ```

   Note the **pod-aware overload** of `buildClient` — the same declarative-client machinery as any other
   service call (`service-to-service.md`), but targeted at a pod resolved at runtime rather than a static
   service name.
5. The pod's `merchant-service` creates the real store record in **its own** database. From then on, that store's
   products, orders and customers never leave the pod.

Provisioning is deliberately asynchronous: it crosses a network boundary into possibly another region, and it
must survive the pod being briefly unavailable. Doing it inline in the HTTP request that created the store would
make store creation fail whenever a pod hiccups.

## Two runtime paths to a store

### Seller/admin path — through `store-core-gateway`

The platform gateway doesn't know pods at compile time; it **discovers them and rewrites its own routing table**.
`PodClient` implements `RouteDefinitionRepository`:

```java
@Scheduled(fixedRateString = "${cvhome.gateway.route-refresh-rate:PT1M}")
public void refreshRoutes() { publisher.publishEvent(new RefreshRoutesEvent(this)); }

@Override
public Flux<RouteDefinition> getRouteDefinitions() {
    return getPods().map(pod -> {
        RouteDefinition rd = new RouteDefinition();
        rd.setId("pod-" + pod.shortenPodId());
        rd.setUri(URI.create(serviceUrlBuilder.getServiceUrl(pod)));   // INTERNAL vs EXTERNAL resolved here
        var predicates = new ArrayList<>(commonPredicates);            // Path=/spg/**, Query=store
        predicates.add(new PredicateDefinition("Query=pod," + pod.id().id()));
        rd.setPredicates(predicates);
        rd.setFilters(commonFilters);                                  // StripPrefix=1, TokenRelay
        return rd;
    });
}
```

- Pods come from tenancy via `ExternalPodClient.listPods()` (`GET /api/v1/pod/list`), with
  `onErrorResume` so a tenancy outage degrades to the existing routes instead of dropping them.
- A request to `/spg/**?store=<id>&pod=<podId>` matches the route for that pod. `StripPrefix=1` removes `/spg`,
  and **`TokenRelay` forwards the seller's OAuth2 token** into the pod — which the pod accepts because `uaa` is
  one of its configured realms (`authentication.md`).
- Routes refresh every minute, so **a newly created pod becomes reachable without redeploying the gateway.**

So console-ui edits a product in any pod through one origin, and pod selection is just a query parameter.

### Shopper path — through the pod's own `spg`

A shopper never touches store-core. They hit a custom domain that resolves to the pod's Caddy (`spg`), which
asks the pod's **own** `merchant-service` who that domain belongs to. `merchant-service` exposes both hooks at
`/api/v1/router/public/`:

- **`ask-for-tls`** ← Caddy's `on_demand_tls { ask }`. `AskTlsService` issues a certificate only if the domain is
  the pod's own domain or `merchantRepository.findByDomain(...)` finds a store **in this pod** that owns it.
  That check is pod-local, so one pod can never mint certificates for another pod's tenants.
- **`lookup-by-domain`** ← Caddy's `domain_lookup`. `LookupDomainHeadersService` maps the domain to a store and
  returns the headers Caddy injects into the request:

  | Header | Source |
  |---|---|
  | `Store-Id` | the store's `StoreMerchantId` |
  | `Theme` | which `landing-ui` template to render (`landing-ui.md`) |
  | `Color-Theme` | runtime palette |
  | `Default-Language`, `Supported-Languages` | i18n |

  Results are cached in Caddy for `DOMAIN_LOOKUP_TTL` (5m default).

**This is the tenant-resolution seam.** `landing-ui` is one deployment serving every store in the pod; it is
told which tenant it is serving purely by these headers. Domains are allocated per store through the same
controller's `private/allocate` / `private/remove` endpoints.

## Who may manage pods

`PodController` (`/api/v1/pod`) is deliberately restrictive:

```java
@GetMapping                                     // list
@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN') or hasAuthority('SCOPE_STORE_CORE')")
public Page<Pod> findAllPods(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, Pageable pageable) {
    return identity.isSuperAdmin() ? podService.listAllPods(pageable)
                                   : podService.listAllPods(identity.org(), pageable);
}

@PostMapping @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")   public Pod create(@RequestBody Pod pod)
@PutMapping @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")    public Pod update(...)
@DeleteMapping @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')") public void delete(...)
```

- **Creating/updating/deleting a pod is platform-operator only** (`ROLE_SUPER_ADMIN`) — a pod is infrastructure,
  not a self-service resource.
- **Listing is tenant-scoped**: a super admin sees every pod; an org admin sees only pods belonging to their org
  (their dedicated ones). Same endpoint, filtered by `UserOrgStoreIdentity`.
- **`SCOPE_STORE_CORE`** is the `client_credentials` scope — that authority is what lets the gateway's
  `PodClient` poll `/api/v1/pod/list` as a machine rather than a user.

`RouterController.getStorePodByStoreId(store)` resolves a store to its pod for internal callers.

## Isolation summary

| Concern | Isolation |
|---|---|
| Store data (products, orders, customers) | Per **pod** database; never crosses pods |
| Shopper identity | Per **store** — one `cua` per pod, one realm per store inside it |
| Shopper sessions, tokens, lockout, identity providers | Per store, by `@TenantId` on every row |
| Shopper signing key | Per **pod** — shared by every store on it, and not a merchant's to rotate |
| Seller/admin identity | **Shared** — one `uaa` in store-core for the whole platform |
| Billing, subscriptions, org/store registry | **Shared** — tenancy |
| TLS certificates | Per pod (S3-backed Caddy storage, pod-local `ask` check) |
| Physical region | Per pod, via `PodEndpoint` |

The rule of thumb when adding a feature: **if it's about a shopper or a store's own data, it belongs in a pod.
If it's about accounts, plans, provisioning, or which pod hosts what, it belongs in the control plane.**

## Related

- Pod-aware client construction and `lb://` resolution — `service-to-service.md`
- `StoreCreatedEvent` / `StoreProvisionedEvent` and the outbox — `events-outbox.md`
- The `spg` Caddyfile routing table — `store-pod.md`
- Local pod identity config (`pod-info.pod`, `com.asrevo.cvhome.pods`) — `configuration.md`
- Why pods accept both `uaa` and `cua` tokens, and what a realm is — `authentication.md`
- The authorization server both deployments are built from — `shared-libraries.md`

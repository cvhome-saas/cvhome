# Caching — `store-commons:cache`

One library caches for every service: a service declares its **regions** once, keys every cached read by its
**store** first, reads through Spring's `@Cacheable` on the library's `CacheManager`, and lets a committed write
drop its store's entries. What a region holds, how long, how much and on which provider is configuration.

## The pieces

| Piece | Where | What |
|---|---|---|
| `CacheRegion` | `com.asrevo.cvhome.cache` | one named cache: `regionName()` (`catalog.product`), value type, default ttl and size, scope `STORE` or `GLOBAL` |
| `<Service>Regions` | `<domain>-core/.../reads/` | the service's enum of regions, exposed as its one `CacheRegions` bean |
| `CacheKey` | `com.asrevo.cvhome.cache` | store, language, typed parts; `of`, `sku`, `product`, `variant`, `slug`, `query`, `global`; `render()` is `store\|lang\|part…` |
| `StoreScopedKeyGenerator` | bean `storeScopedKeys` | builds the key of a `@Cacheable` method from its typed parameters; refuses a missing store, a raw `Long`, a shopper |
| `RegionCache` / `CacheRegistry` | `com.asrevo.cvhome.cache` | the programmatic surface: single and bulk (`getAll`) reads, `evictStore`, per region |
| `CacheProvider` | `…cache.provider` | where entries live; `caffeine` here, another provider is its own module and a bean |
| `EvictionRules` | `<domain>-service/.../config/CacheConfig` | which regions a committed write to which entity drops |
| `StoreScoped` | `commons/domain` | what an entity implements to say its store |
| `AfterCommitEviction` | bean | for bulk JPQL and native writes Hibernate reports no entity for |
| meters | Micrometer | `cache_gets_total{cache,result}`, `cache_puts_total`, `cache_evictions_total`, `cache_size` per region |
| `CacheEvent` | `…cache.event` | the sealed family an aggregate raises through the outbox: `ProductChanged`, `VariantChanged`, `CategoryChanged`, `ManufacturerChanged`, `StockChanged`, `PriceChanged`, `StoreChanged`, `ContentChanged` |
| `CacheEventOutboxHandler` | bean, where the outbox is | drains the events: `CacheEventApplier` drops what `EvictionRules.onEvent(...)` names, then `CacheEventTransport` is told |
| `CacheEventTransport` | port, bean | where an event goes after this task: `LoggingCacheEventTransport` unless the service declares its own |

## Rules

- **Every cached read is a `*Reads` method** keyed by `StoreMerchantId` first (a `GLOBAL` region is the one
  exception: a reference list the same for every tenant, keyed by the sentinel store `*`). The architecture rules
  (`cachedReadsAreStoreScoped`, `noCacheInternalsOutsideTheCacheModule`) fail the build otherwise.
- **Never cached:** anything that depends on who asks (`ShopperId`, `CustomerId`, a principal: carts, orders,
  customers), a preview request, a reservation, a payment intent or webhook, a per-user `/private/` read.
- **A key part is typed:** a `KeyPart` (`Sku`, `ProductId`, `CategoryId`, `ManufacturerId`, `VariantId`,
  `QueryHash`), a short string (a slug, a code), an integer, an enum, a `Pageable`. A raw `Long`, a mutable criteria
  object or a shopper is refused at the call. A criteria object is hashed: `QueryHash.of(criteria.normalised())`.
- **A cached value is read-only.** The DTOs are shared between every reader on the task; a caller that mutates one
  corrupts the entry.
- **Eviction is per store and O(1):** a write bumps the store's version in the rule's regions; entries written
  under the old version are never seen again and die of age or size. `cache_evictions_total` counts the provider's
  own evictions, not store bumps. An entity of the package with no rule is logged once at start-up and evicts
  nothing; a ruled entity that is not `StoreScoped` stops the service.
- **Another task lags by the region's ttl** after a write until a transport carries the aggregate's `CacheEvent` to
  it; with the logging transport, the ttl is the bound. A consumer holding a copy of another service's data lags the
  same way until it maps that service's event (`rules.onEvent(StoreChanged.class).evict(...)`).
- **Names** are `<service>.<read>`, lowercase, a hyphen inside a read (`catalog.cart-line`): the `cache` label on
  every meter and the key of a configuration override.

## Configuration

```yaml
com.asrevo.cvhome.cache:
  default-provider: caffeine          # common-config.yml
  regions:                            # a service's application.yml, by service then by read
    catalog:
      listing: { ttl: 60s, max-size: 5000 }
      suggest: { enabled: false }      # every read misses, the meters keep counting
      product: { provider: redis }     # once a provider named redis is a bean; otherwise the service refuses to start
```

Code defaults come from the enum; YAML wins. The registry reads it at run time, never as a bean condition
(`verifyNoConfigurationSwitchedBeans`).

## Adding a cached read, in five steps

1. Add the constant to `<Service>Regions` (name, value type, default ttl and size, scope).
2. Add the method to `<Area>Reads` in `-core`: `@Cacheable(cacheNames = Names.X, keyGenerator = StoreScopedKeyGenerator.BEAN)`,
   parameters typed (`StoreMerchantId`, `LanguageCode`, a `KeyPart`, a slug…). A bulk read (many skus at once)
   uses `registry.region(Regions.X, Type.class).getAll(keys, loader)` instead.
3. Add the entities whose writes stale it to `EvictionRules` in the service's `CacheConfig`; a new entity implements
   `StoreScoped` (a description or an option value answers through its owner).
4. Override ttl or size in the service's `application.yml` only if the enum's default does not fit.
5. `<Area>ReadsIntegrationTest` with `SqlStatements.during(...)`: the second read costs no statement; a write in
   store A makes A's next read pay and leaves B's warm. A QA case in `<service>/qa/<svc>-qa.md`.

## Cache events

A change a shopper sees is also a typed event, registered on the aggregate in the transaction that changed it and
written to the outbox with it: `Product.domainEvents()` adds `ProductChanged` to every save, `Manufacturer.renamed()`
raises `ManufacturerChanged`, `MerchantStore.changed()` raises `StoreChanged`, `Inventory.stockChanged()` and
`priceChanged()` raise `StockChanged` and `PriceChanged` (an upsert, a reservation taken or released), and
`Content.changed()` raises `ContentChanged` once per save. Every event names its store and partitions on it, carries
typed ids (`ProductId`, `Sku`) and survives the outbox's JSON as such; `VariantChanged` and `CategoryChanged` exist
for the reads that will key by them and are raised by nobody yet.

The flow, on the one task the outbox hands the event to:

```
aggregate.registerEvent(new StockChanged(store, sku))   →  outbox row, same transaction
→ CacheEventOutboxHandler.onStockChanged                →  CacheEventApplier.apply: evictStore(store, rules.regionsForEvent(StockChanged))
                                                        →  CacheEventTransport.publish(event)
```

The applier is the same eviction a commit does, so on the task that took the write it is free; its worth is on
every other task, once a transport exists. `EvictionRules` map events with the same builder as entities:
`.onEvent(StockChanged.class, PriceChanged.class).evict(SKU)`; a service with no tables of its own starts from
`EvictionRules.onEvents()`. An event no rule names drops nothing and is logged at DEBUG.

**The transport is a port, and choosing one is a bean, never a switch.** `LoggingCacheEventTransport` is the default:
the event is applied here and written to the log at DEBUG, every other task keeps its ttl. The options this leaves
open, each a `CacheEventTransport` bean in its own module:

| Transport | What it does | When |
|---|---|---|
| logging (now) | applies locally, logs | until a region's ttl is too long to wait out |
| HTTP fan-out | `POST /internal/cache-events` (s2s scope, body polymorphic by `eventType`) to every replica of every consuming service through the discovery client; the receiver calls the applier | few replicas, no broker |
| broker | RabbitMQ or Redis pub-sub; every replica subscribes and calls the applier | many replicas or many consumers |
| none | a region on a shared provider (Redis): the applier updates the one copy and `publish` is a no-op | once `cache-redis` exists |

A consumer maps a foreign event exactly as its own: `rules.onEvent(StoreChanged.class).evict(MerchantClientRegions.STORE)`
in the service that holds the copy. Which services raise what, and who will map it:

| Event | Raised by | Mapped locally | Consumers to map it (once a transport carries it) |
|---|---|---|---|
| `ProductChanged` | catalog, every product save | `catalog.product`, listing, search, suggest, related, group, cart-line, detailed-product | checkout (cart line) |
| `ManufacturerChanged` | catalog, a rename | `catalog.brands`, listing, search, product, suggest | — |
| `StoreChanged` | merchant, every store save | `merchant.store`, store-by-language, languages | every service's `merchant.store-client` |
| `StockChanged`, `PriceChanged` | inventory, an upsert or a reservation | `inventory.sku` | catalog (cart-line, detailed-product), checkout |
| `ContentChanged` | content, an edit or a status change | `content.page`, post, posts, post-categories, banners, faq, site, sitemap, menu | — |

Merchant, inventory and content gained the outbox for this (the JPA starter, the `namastack.outbox` block, the three
tables in their `schema.sql`, the `cvhome_outbox_*` gauges); `events-outbox.md` has the outbox itself.

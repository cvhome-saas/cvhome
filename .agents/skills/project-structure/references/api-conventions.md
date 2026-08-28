# API conventions: tenant context, value objects, permissions

Three conventions hold across essentially every endpoint in the repo. Follow them by default; deviating is the
thing that needs justifying.

## 1. Almost every API takes `StoreMerchantId` and `LanguageCode`

This is a multi-tenant, multi-language platform, so **which store** and **which language** are part of nearly
every request. The standard controller signature:

```java
@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
public Entity create(@Valid @RequestBody PersistableProduct product,
                     StoreMerchantId merchantStore,
                     LanguageCode language) {
    Long id = productCommonFacade.saveProduct(merchantStore, product, language);
    ...
}
```

Note both parameters carry **no annotation** — no `@RequestParam`, no `@PathVariable`. They are resolved
automatically by custom `HandlerMethodArgumentResolver`s registered in `ServletWebConfig`
(`store-commons:autoconfigure`):

| Resolver | Triggers on | Reads | Behaviour |
|---|---|---|---|
| `ServletStoreMerchantIdArgumentResolver` | parameter type `StoreMerchantId` | query param **`store`** | **Throws `IllegalArgumentException` if missing** — tenant context is mandatory |
| `ServletLanguageCodeArgumentResolver` | parameter type `LanguageCode` | query param **`lang`** | Falls back to `LanguageCode.defaultLanguage()` (`en`); `lang=_all` resolves to `null` meaning "all languages" |

```java
// ServletStoreMerchantIdArgumentResolver
String storeCode = Optional.ofNullable(webRequest.getParameter("store"))
        .filter(it -> !it.isEmpty())
        .orElseThrow(() -> new IllegalArgumentException("Missing required parameter 'store'"));
return new StoreMerchantId(storeCode);
```

**Why this matters:** tenant scoping is not something a developer can forget to add. Declaring the parameter is
enough; the framework supplies it, and a request without `?store=` fails before any business code runs. There is
no "current tenant" thread-local to leak across requests — the store travels explicitly down through facade and
service calls (`saveProduct(merchantStore, product, language)`), so every query is tenant-scoped by
construction.

Matching resolvers exist for other context types: `ServletOrgStorePrincipalInfoArgumentResolver`
(`@OrgStorePrincipalInfo UserOrgStoreIdentity` — the caller's org/store/roles),
`DomainSerializeParamArgumentResolver`, `OrgSerializeParamArgumentResolver`, and the reactive equivalents.

The **same resolvers run on the client side** when building declarative HTTP clients — `WebClientsUtils`
registers `StoreMerchantIdSerializeParamArgumentResolver`, `LanguageCodeSerializeParamArgumentResolver`, etc. on
the `HttpServiceProxyFactory`. That is why an `-external-api` interface can declare a bare `StoreMerchantId
store` parameter and the tenant context is serialized onto the outgoing request automatically
(`service-to-service.md`). One convention, both directions.

For Swagger, the implicit parameters are documented explicitly:

```java
@Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
@Parameter(name = "lang",  schema = @Schema(name = "lang",  type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
```

**When writing a new endpoint:** add `StoreMerchantId merchantStore` unless the endpoint is genuinely
tenant-independent (platform admin, health, `public/ask-for-tls`), and add `LanguageCode language` whenever the
response contains translatable content.

## 2. Value objects, used heavily

`store-commons/commons/.../domain/` is a deliberate collection of **typed value objects instead of raw
`String`/`Long`**. There are ~40 of them:

- **Identifiers:** `StoreMerchantId`, `ManagerOrgId`, `PodId`, `IdentityId`, `Identifier`
- **Codes:** `LanguageCode`, `CurrencyCode`, `CountryIsoCode`, `ZoneCode`
- **Web/domain concepts:** `Email`, `Domain`, `DomainType`, `ManagerStoreDomain`, `SocialLink`, `SocialProvider`
- **Infrastructure:** `Pod`, `PodEndpoint`, `EndpointType`, `ServiceDomain`, `StorageProviderType`
- **Presentation/config:** `Theme`, `ColorTheme`, `SliderImage`
- **Subscription:** `SubscriptionPlan`, `SubscriptionPlanFeature`, `SubscriptionPlanLimitKey`, …
- **Security:** `Roles`, `Groups`, `UserOrgStoreIdentity`

They are Java `record`s, and they are not just wrappers — they carry behaviour and invariants:

```java
public record StoreMerchantId(String storeMerchantId) implements Comparable<StoreMerchantId>, Serializable {
    public String getId() { return this.storeMerchantId; }
}

public record LanguageCode(String code) implements Serializable, Comparable<LanguageCode> {
    public static LanguageCode defaultLanguage() { return new LanguageCode("en"); }
    public static LanguageCode nonLanguage()     { return new LanguageCode("_non"); }
    public static LanguageCode allLanguage()     { return new LanguageCode("_all"); }

    @JsonIgnore
    public boolean isLanguage() {   // rejects null, blank, <2 or >3 chars, and the _non/_all sentinels
        ...
    }
}
```

`LanguageCode` is the clearest illustration of the payoff: the "all languages" and "no language" sentinels
(`_all`, `_non`) are **encoded in the type** with `isAllLanguage()` / `isNonLanguage()` helpers, instead of
magic strings scattered through the codebase. `PodId.shorten()` is another — the 8-char short form used in
namespaces and route ids lives on the type that owns it.

What this buys, concretely:

- **The argument resolvers and permission evaluator can dispatch on type.** `supportsParameter(...)` is just
  `parameter.getParameterType().equals(StoreMerchantId.class)`. With `String` parameters none of this machinery
  could exist.
- **You cannot transpose arguments.** `saveProduct(merchantStore, product, language)` won't compile with the
  store and language swapped.
- **Serialization is centralized** — `PodId` uses `@JsonSerialize(using = ToStringSerializer.class)` over a
  Mongo `ObjectId` in one place, not at every call site. `StoreMerchantId` goes further: `@JsonValue` makes it
  a bare string on the wire, and its own `@JsonDeserialize` reader also accepts the two object shapes it had
  before store-core's `ManagerStoreId` was merged into it, so stored outbox payloads stay readable.

**Persistence:** JPA `AttributeConverter`s in `store-pod/commons/store-commons`
(`com.asrevo.cvhome.store.core.converter`) map them to plain columns —
`LanguageCodeConverter`, `CurrencyCodeConverter`, `CountryIsoCodeConverter`, `ZoneCodeConverter`,
`LocaleConverter`:

```java
@Converter
public class LanguageCodeConverter implements AttributeConverter<LanguageCode, String> {
    public String convertToDatabaseColumn(LanguageCode c) { return c == null ? null : c.code(); }
    public LanguageCode convertToEntityAttribute(String s) { return new LanguageCode(s); }
}
```

**Rule: don't introduce a raw `String` id or code.** Look in `commons/domain/` first — the value object almost
certainly exists. If you genuinely need a new one, add it there as a record (plus a converter if it is
persisted) rather than passing a `String` around.

## 3. Authorization via `@PreAuthorize("hasPermission(...)")`

Endpoints don't check roles inline. They declare a **permission string** and let a central evaluator decide:

```java
@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
```

The three arguments are `(targetId, targetType, permission)` — the store being acted on, its type, and the
capability required.

`CustomPermissionEvaluator` (in `store-commons:autoconfigure`) switches on the permission string and delegates
to `PermissionAccessChecker`:

```java
case "STORE-POD.MERCHANT.*", "STORE-POD.CONTENT.*", "STORE-POD.CATALOG.*", "STORE-POD.CHECKOUT.*",
     "STORE-POD.CUA.*", "STORE-POD.PAYMENT.*"
     -> checker.hasManageAccessOnStore(authentication, (StoreMerchantId) targetId, this.pod);

case "STORE-POD.CUSTOMER.*"          -> checker.isCustomerInSameStore(...);
case "STORE-POD.CATALOG.RESERVE"     -> checker.isSameStorePod(...);          // service-to-service only
case "STORE-POD.MERCHANT.STORE-CREATE" -> checker.hasAccessOnStoreCreate(authentication, (String) targetId, this.pod);

case "STORE-CORE.STORE-FIND-ONE"     -> checker.hasAccessOnStoreFindOne(...);
case "STORE-CORE.USERS.LIST"         -> checker.hasAccessOnStoreUsersList(...);
case "STORE-CORE.USERS.CREATE"       -> checker.hasAccessOnStoreUsersCreate(...);
...
default -> false;                     // unknown permission = denied
```

Note the conventions:

- **Naming is `LAYER.DOMAIN.ACTION`** — `STORE-POD.CATALOG.*` (any management action on catalog),
  `STORE-CORE.USERS.CREATE`. The `.*` suffix means "manage access to this domain" rather than a fine-grained
  action.
- **`default -> false`** — a typo'd or unregistered permission denies rather than allows.
- **The target is a `StoreMerchantId` on both sides.** The evaluator used to convert the pod-side id into a
  tenancy-side `ManagerStoreId` here; the two types have been merged, so it now just casts
  (`multi-tenancy.md`).
- **It is pod-aware.** `CustomPermissionEvaluator` injects the current `Pod` from `PodInfoProperties`, and the
  checks compare the caller's store/org against *this* pod — so a token valid in one pod can't manage a store in
  another.

`PermissionAccessChecker` resolves each permission through `StoreRoleAccessChecker` against the caller's roles
(`ROLE_SUPER_ADMIN`, `ROLE_ORG_ADMIN`, `ROLE_STORE_ADMIN`, `ROLE_STORE_MODERATOR`, `ROLE_STORE_RETAIL`,
`ROLE_CUSTOMER`) **and** OAuth2 scopes (`SCOPE_STORE_CORE`, `SCOPE_STORE_POD`). That scope path is how
service-to-service calls authorize — e.g. `STORE-POD.CATALOG.RESERVE` (checkout reserving stock in catalog)
requires `isScopeStorePod`, so it can only be invoked by another service in the same pod, never by a browser.

**Adding an endpoint:**

1. Pick the permission string for its layer/domain; reuse an existing one where possible.
2. If it needs a genuinely new capability, add a `case` to `CustomPermissionEvaluator` and a method on
   `PermissionAccessChecker` — never inline a role check in the controller.
3. Point `#merchantStore` at the `StoreMerchantId` parameter so the check is tenant-scoped.

## Related

- Why the same argument resolvers run client-side — `service-to-service.md`
- Roles, scopes, and the two token issuers — `authentication.md`
- The single `StoreMerchantId` and pod-scoped checks — `multi-tenancy.md`
- Encrypting secret fields on the way into the DB — `secrets-encryption.md`

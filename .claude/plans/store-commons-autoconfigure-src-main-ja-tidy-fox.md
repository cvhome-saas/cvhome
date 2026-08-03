> **SUPERSEDED — history only.** Merged into `error-handling.md`, which is the plan of record. Parts of this
> file describe designs that have since been replaced; do not implement from it.

# Simplify service-to-service error wiring — explicit catalogs, one handler

## Context

`.claude/plans/claude-plans-help-me-set-plan-curried-f-cached-grove.md` delivered typed s2s errors, but paid for them with a discovery mechanism that costs more than it earns.

To answer one question — *which error contract applies to this client?* — the codebase currently carries: a `META-INF/services` file per `-external-api` module, a `ServiceLoader` scan, a lazily-initialised `RemoteErrorRegistry` holder with duplicate-claim warnings and an `EMPTY` fallback, and an `apis()` method on every catalog that exists **solely** to let the registry key by interface. That machinery is justified when registration must be implicit — a plugin classpath, an open set of providers.

This is not that. There are **11 client construction sites** in the whole repo, all in five `ClientsConfig` classes plus `StorePodClientFactory`, and exactly **one** has an error contract today. The plan's own note admits the design was forced: catalogs are keyed by interface because the URI is unreliable, which is a workaround for the fact that nobody was passing the catalog in the first place — even though the call site knows both the interface and the contract.

The cost is not just lines. Registration is invisible: a typo in the services file, or a missing one, degrades silently to `UnmappedRemoteFailureException` with nothing at compile time to catch it. And the wiring is untestable except through the classpath — `store-commons/autoconfigure` needs its own test-scoped services file and a `probe/` fixture package purely to exercise a mechanism that a constructor argument would make trivial.

**Outcome:** the catalog is passed to `WebClientsUtils.build(...)`. `RemoteErrorRegistry`, both services files, and `apis()` are deleted. Error handling for a client is consolidated into one named class, `S2sErrorHandler`, instead of being spread across an interceptor lambda, a status-handler class, and a proxy helper.

## Decisions taken

| | |
|---|---|
| Catalog shape | `RemoteErrorCatalog` stops being an interface and becomes a **final value class with a fluent builder**, held as a `public static final` constant on the `-external-api` module. Nothing to implement; maps `ErrorCode` directly rather than `.code()` strings |
| Passing | A **single 3-arg signature**, third argument **nullable**. No 2-arg overload: every call site states its error contract or explicitly states it has none |
| Discovery | Deleted outright. `ServiceLoader`, `RemoteErrorRegistry`, `apis()`, both `META-INF/services` files |
| Handler | One `S2sErrorHandler` per client owns both failure paths (no response, error response) and the typed-unwrapping rule. `ProblemDetailErrorHandler` folds into it |
| `RemoteProblemTranslator` | **Unchanged in behaviour** — it is the pure decoder and has 9 passing tests. Only `RemoteErrorRegistry.find(catalog, code)` becomes `catalog.find(code)` |

---

## Design

### 1. `RemoteErrorCatalog` becomes a value — `store-commons/errors/.../errors/remote/RemoteErrorCatalog.java`

Replaces the interface. Absorbs `RemoteErrorRegistry.find(...)` as an instance method, which is the last thing the registry was for.

```java
public final class RemoteErrorCatalog {

    private final Map<String, RemoteExceptionFactory> mappings;
    private final RemoteExceptionFactory transportFailure;   // nullable

    public static Builder builder();
    public static RemoteErrorCatalog none();                 // empty; what null normalises to

    public Optional<RemoteExceptionFactory> find(String code);
    public RemoteExceptionFactory transportFailure();

    public static final class Builder {
        public Builder map(ErrorCode code, RemoteExceptionFactory factory);
        public Builder unreachable(RemoteExceptionFactory factory);
        public RemoteErrorCatalog build();
    }
}
```

`map(ErrorCode, ...)` rather than `map(String, ...)` keeps the property the old catalog got right — renaming a code in `PaymentErrors` cannot silently orphan a mapping.

`RemoteExceptionFactory` and `RemoteErrorContext` are unchanged. The factory's `RemoteServiceException` return type stays the guard rail that keeps a remote failure from being mistaken for a locally raised one — and, since the provider/service split, from being confused with an `ExternalProviderException`.

**Delete** `store-commons/errors/.../errors/remote/RemoteErrorRegistry.java` (~90 lines, including the `System.Logger` workaround that existed only because the registry needed to warn about duplicate `apis()` claims).

### 2. The handler — new `store-commons/autoconfigure/.../s2s/error/S2sErrorHandler.java`

The "service-to-service error handler" as a single named thing. Today its three parts live in three places: a `requestInterceptor` lambda inside `WebClientsUtils.build`, the `ProblemDetailErrorHandler` class, and `declaredOrCarrier` at the bottom of `WebClientsUtils`.

```java
public final class S2sErrorHandler {

    private final RemoteErrorCatalog catalog;

    /** The one null check: a client with no declared error contract gets the empty catalog. */
    public S2sErrorHandler(RemoteErrorCatalog catalog) {
        this.catalog = catalog == null ? RemoteErrorCatalog.none() : catalog;
    }

    /** Both failure paths on a blocking client: a call that produced no response, and one that produced an error. */
    public void apply(RestClient.Builder builder);

    /** Reactive: translation only — the failure travels inside a Mono, where no proxy can rethrow it as declared. */
    public void apply(WebClient.Builder builder);

    /** Delivers the carried cause as the method's declared type, or leaves it in the carrier. */
    public static Throwable declaredOrCarrier(Method method, Throwable thrown);
}
```

Both `apply` overloads keep the exact behaviour that is there now — the interceptor catching `IOException | ResourceAccessException` → `RemoteProblemTranslator.unreachable`, and `defaultStatusHandler(HttpStatusCode::isError, ...)` → `RemoteProblemTranslator.translate`. Nothing about translation changes; this is a move, not a rewrite.

**Delete** `store-commons/autoconfigure/.../s2s/error/ProblemDetailErrorHandler.java` — its body becomes the status-handler lambda inside `apply(RestClient.Builder)`. Its javadoc explaining *why* the exception travels inside `UncheckedBaseException` moves onto `S2sErrorHandler`.

### 3. `WebClientsUtils` — the signature the user asked for

`store-commons/autoconfigure/.../s2s/utils/WebClientsUtils.java`. Both overloads take the catalog as a nullable fourth argument and shrink to delegation:

```java
public static <T> T build(RestClient.Builder builder, String url, Class<T> type, RemoteErrorCatalog errors) {
    RestClient.Builder configured = builder.baseUrl(url);
    new S2sErrorHandler(errors).apply(configured);
    return buildClient(type, RestClientAdapter.create(configured.build()));
}
```

`buildClient(...)` (the argument-resolver assembly) and `withTypedErrors(...)` are unchanged, except that `withTypedErrors` now calls `S2sErrorHandler.declaredOrCarrier`.

### 4. Threading through the two façades

`store-commons/autoconfigure/.../s2s/config/internal/RestClientBuilder.java` and `WebClientBuilder.java` — these are what `ClientsConfig` actually calls. Each `buildClient` gains the nullable catalog parameter:

```java
public <T> T buildClient(String serviceName, Class<T> type, RemoteErrorCatalog errors);
public <T> T buildClient(Pod pod, String serviceName, Class<T> type, RemoteErrorCatalog errors);   // RestClientBuilder only
```

No 2-arg overload, per the decision above.

### 5. The payment contract becomes a constant

New `store-pod/payment/payment-external-api/.../payment/api/errors/PaymentApiErrors.java`:

```java
public final class PaymentApiErrors {

    public static final RemoteErrorCatalog CATALOG = RemoteErrorCatalog.builder()
            .map(PaymentErrors.INITIATE_REJECTED, PaymentGatewayRejectedException::from)
            .map(PaymentErrors.INITIATE_FAILED,   PaymentApiUnavailableException::from)
            .unreachable(PaymentApiUnavailableException::from)
            .build();

    private PaymentApiErrors() {
    }
}
```

Carry over the existing `PaymentErrorCatalog` javadoc — it explains the load-bearing point that these rebuild **caller-side** types, and that neither appears in `IPaymentGatewayService`'s `throws` clauses, which is why `RestPaymentGatewayClient` opens the carrier.

**Delete** `PaymentErrorCatalog.java` and `payment-external-api/src/main/resources/META-INF/services/com.asrevo.cvhome.errors.remote.RemoteErrorCatalog`.

### 6. Call sites — 11 total

| File | Change |
|---|---|
| `store-pod/checkout/checkout-service/.../config/ClientsConfig.java` | `externalPaymentGatewayService` passes `PaymentApiErrors.CATALOG`; its other 3 beans pass `null` |
| `store-pod/catalog/catalog-service/.../config/ClientsConfig.java` | 2 beans → `null` |
| `store-pod/payment/payment-service/.../config/ClientsConfig.java` | 2 beans → `null` |
| `store-pod/cua/.../config/ClientsConfig.java` | 1 bean → `null` |
| `store-core/gateway/gateway-service/.../config/ClientsConfig.java` | 1 bean → `null` (the sole `WebClientBuilder` site) |
| `store-core/control-plane/control-plane-service/.../manager/service/StorePodClientFactory.java` | 1 site → `null` (the sole `Pod` overload site) |

checkout-service already declares `api project(':store-pod:payment:payment-external-api')`, so `PaymentApiErrors` is visible with no Gradle change.

---

## Consequence to be aware of

Typed payment errors stop being automatic. Today, any service with `payment-external-api` on its classpath gets them via `ServiceLoader`; afterwards a service gets them only by passing `PaymentApiErrors.CATALOG`. **Nothing regresses now** — checkout is the only builder of a payment client — but the next pod that calls payment must pass the catalog, and if it forgets, failures degrade to `UnmappedRemoteFailureException` exactly as an unmapped code does today.

That is the intended trade: visible wiring at the cost of automatic wiring. The nullable third argument is what keeps it honest — an author cannot construct a client without seeing the parameter and deciding.

---

## Files

**Created** — `store-commons/autoconfigure/.../s2s/error/S2sErrorHandler.java`, `store-pod/payment/payment-external-api/.../api/errors/PaymentApiErrors.java`

**Deleted** — `store-commons/errors/.../remote/RemoteErrorRegistry.java`, `store-commons/autoconfigure/.../s2s/error/ProblemDetailErrorHandler.java`, `store-pod/payment/.../api/errors/PaymentErrorCatalog.java`, `store-commons/autoconfigure/src/test/java/.../s2s/error/probe/ProbeErrorCatalog.java`, and both `META-INF/services/com.asrevo.cvhome.errors.remote.RemoteErrorCatalog` files (payment `src/main/resources`, autoconfigure `src/test/resources`)

**Modified** — `RemoteErrorCatalog.java` (interface → value + builder), `RemoteProblemTranslator.java` (`catalog.find(code)`), `WebClientsUtils.java`, `RestClientBuilder.java`, `WebClientBuilder.java`, the five `ClientsConfig` classes, `StorePodClientFactory.java`

---

## Verification

**Existing tests carry the weight — all 13 must stay green with only wiring edits:**

- `store-commons/autoconfigure/src/test/java/.../s2s/error/RemoteProblemTranslatorTest.java` (9 cases). Edits are mechanical: `NO_CATALOG` becomes `RemoteErrorCatalog.none()` instead of `RemoteErrorRegistry.forApi(Object.class)`, and the nested `TestCatalog` class becomes a builder-built constant. **If any assertion needs changing, the refactor has changed behaviour and is wrong** — translation is supposed to be untouched.
- `store-commons/autoconfigure/src/test/java/.../s2s/error/TypedRemoteErrorRoundTripTest.java` (4 cases, `MockRestServiceServer`). This is the one that proves the whole chain: `@HttpExchange` → handler → carrier → declared type. It currently depends on `ServiceLoader` finding `ProbeErrorCatalog` on the test classpath; afterwards it passes the probe catalog straight to `WebClientsUtils.build(...)`. The test gets *more* direct, and `src/test/resources/META-INF/services/...` disappears.

**New test** — one case in `TypedRemoteErrorRoundTripTest` asserting a `null` catalog yields `UnmappedRemoteFailureException` rather than an NPE. That is the nullable argument's contract and nothing else pins it.

**Build** — `./gradlew :store-commons:errors:build :store-commons:autoconfigure:build :store-pod:payment:payment-external-api:build`, then `./gradlew build` (checkstyle runs as part of it and is currently clean).

**Grep gate** — after the change, `grep -rn "RemoteErrorRegistry\|META-INF/services/com.asrevo.cvhome.errors" --include='*.java' --include='*.gradle' .` and a search for the two services files must both come back empty. Stale references would compile fine while silently doing nothing, which is the failure mode this plan exists to remove.

**Local end-to-end** (`docker-compose-lcl`), unchanged from the parent plan's acceptance table — the point is that these still behave identically:

| Check | Expected |
|---|---|
| Place an order with payment-service stopped | checkout returns 502, inventory reservation released |
| Place an order with a declined test card | checkout returns the rejection typed as `PaymentGatewayRejectedException`, order fails cleanly rather than being held |

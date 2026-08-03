> **SUPERSEDED — history only.** Merged into `error-handling.md`, which is the plan of record. Parts of this
> file describe designs that have since been replaced; do not implement from it.

# Typed service-to-service errors — the `-external-api` module as a client SDK

Extends `.claude/plans/help-me-set-plan-curried-fountain.md`. That plan gave *locally raised* failures condition-named types and compiler-checked signatures. This one closes the same gap across an HTTP hop.

## Context

`IPaymentGatewayService` declares no exceptions at all:

```java
@PostExchange("/payments/initiate")
PaymentInitiateResult initiatePayment(StoreMerchantId store, @RequestBody PaymentRequest paymentRequest);
```

A caller reading that signature learns nothing can go wrong. Compare a real client SDK: Stripe decodes its error body into `CardException`, `RateLimitException`, `InvalidRequestException`, `ApiConnectionException` — all under `StripeException` — so a caller branches on *type*. We do the opposite: everything collapses into one `RemoteServiceException`, and it does not even arrive as itself.

Four verified defects behind that:

1. **The typed exception cannot reach the caller.** `ProblemDetailErrorHandler` implements `RestClient.ResponseSpec.ErrorHandler`, whose `handle` may only throw `IOException` and unchecked exceptions. `RemoteServiceException` is checked, so it is wrapped in `UncheckedBaseException` — and nothing unwraps it at the proxy boundary. `catch (PaymentGatewayRejectedException e)` can never match; only the shared advice, at the very edge, ever opens the carrier.
2. **Nothing distinguishes remote conditions.** `RemoteProblemTranslator.codeFor` returns exactly two codes — `REMOTE_TIMEOUT` for 504/408, `REMOTE_CALL_FAILED` for everything else. "Order not found", "gateway declined the card" and "payment-service is on fire" are one type carrying one of two codes.
3. **Transport failures are not translated at all.** `defaultStatusHandler` only fires when a response exists. Connection refused, DNS failure and read timeout surface as raw `ResourceAccessException`. `RemoteProblemTranslator.unreachable(...)` exists and is called **only from its own test** — dead code in production.
4. **`remoteCode` never reaches the wire.** The translator captures it on the exception; `ProblemDetailFactory` does not know about `RemoteServiceException`, and `GlobalErrorHandler.render` remaps only the *status*. So checkout re-emits `COMMON.REMOTE_CALL_FAILED`, and payment's code dies at the first hop — despite `RemoteServiceException`'s javadoc claiming the opposite.

Consequences today: `OrderPlacementFacadeImpl:122` calls `initiatePayment` with **no try/catch**, after the inventory reservation is taken — a gateway failure propagates out of `placeOrder` and the reservation is never released. `OrderInventoryOrchestratorImpl` wraps `reserve` in `catch (Exception _) → status(false)`, so "out of stock" and "catalog is down" are the same outcome. The `WebClient` path (`ExternalPodClient`) has no error handler whatsoever.

**Outcome:** an `-external-api` module is the client SDK for its service. It ships a named exception family, the wire error is decoded back into it, and the interface declares it — so a caller branches on type with the compiler checking the branches, exactly as it now does for local failures.

## Decisions taken

| | |
|---|---|
| Client-side types | A **separate family per `-external-api` module**, rooted at that API's own abstract base extending `RemoteServiceException`. Keeps "who failed" honest: `PaymentInitiateRejectedException` (payment-commons) means *Stripe* refused; `PaymentGatewayRejectedException` (payment-external-api) means *payment-service* refused |
| Discovery | **`ServiceLoader`** — a `META-INF/services` entry per `-external-api` module. Putting the jar on the classpath is the registration; no per-service Spring wiring to forget |
| Re-emission | The **remote code passes through**: checkout's body carries `code: PAYMENT.INITIATE.FAILED` plus `remoteService`/`remoteStatus`. Fixes defect 4 and makes the plan's existing claim true |
| Reconstructed types | Must extend `RemoteServiceException` — enforced by the factory's return type, so a remote failure can never be mistaken for a locally raised one |
| Reactive path | Blocking (`RestClient`) gets typed unwrapping. `WebClient` gets translation but not unwrapping — see Out of scope |
| Interface roles | The `@HttpExchange` interface is **implemented by the server's controller**, so its `throws` clause carries the **server's** vocabulary. A separate hand-written **client wrapper** carries the caller's. One signature cannot be honest about both |
| What the catalog rebuilds | **The server's own exception class** — pure deserialisation. The wrapper makes the caller-facing judgement, in readable Java, with the original kept as the cause |
| `RemoteServiceException` | **Abstract**, like every other base. Its three concrete conditions are named: `UnmappedRemoteFailureException`, `RemoteServiceUnavailableException`, `RemoteServiceTimeoutException` |

---

## Design

### 1. Registry SPI — `store-commons:errors`

Plain Java, no Spring; the module is already `api` from both commons roots, so every module sees it. New package `com.asrevo.cvhome.errors.remote`:

```java
public interface RemoteErrorCatalog {
    Set<Class<?>> apis();                             // the @HttpExchange interfaces this catalog speaks for
    Map<String, RemoteExceptionFactory> mappings();   // "PAYMENT.INITIATE.FAILED" -> factory
    default RemoteExceptionFactory transportFailure() { return null; }
}

@FunctionalInterface
public interface RemoteExceptionFactory {
    RemoteServiceException create(RemoteErrorContext context);   // return type is the guard rail
}

public record RemoteErrorContext(String code, String detail, Map<String, Object> params,
        List<FieldError> fieldErrors, String service, int status, String traceId, Throwable cause) { }
```

**Implementation note — catalogs are keyed by client interface, not globally by code.** The plan first assumed one global `code → factory` map. The URIs made that wrong: an in-namespace call goes to `lb://payment`, a cross-namespace one to the gateway host with the service as a path segment, and load-balancer resolution rewrites the host before any error handler sees it — so there is no reliable service identifier at translation time to scope a code by. `apis()` fixes it: `WebClientsUtils` resolves the catalog once from the interface it is building a client for, so `PAYMENT.INITIATE.FAILED` only ever rebuilds for the payment client, and two services may use the same code string without colliding. `transportFailure()` is on the catalog for the same reason — a refused connection carries no code to look up.

`RemoteErrorRegistry` — a lazily-initialised holder over `ServiceLoader.load(RemoteErrorCatalog.class)`, exposing `forApi(Class<?>)` and `find(catalog, code)`. Static, so `RemoteProblemTranslator` stays static and `WebClientsUtils` stays free of bean plumbing. Uses `System.Logger`, not SLF4J: `store-commons:errors` has no logging dependency and giving it one would put a transitive library on every module in the repo.

Unregistered codes fall back to `UnmappedRemoteFailureException` — every existing caller keeps working, and a module opts in by shipping a catalog. The name is the point: encountering one in a log is the signal that a code deserves an entry in that API's catalog.

**Follow-up, after review of `PaymentProcessor`.** `parseWebhook` declared three condition-named types under the abstract `ValidationException`, while `initiate`'s `PaymentInitiateRejectedException` hung off `RemoteServiceException`, which was still concrete by exemption — so the generic type the rules forbid everywhere else was throwable from anywhere, and `RemoteProblemTranslator` was in fact throwing it. The base is now abstract and the translator builds named types instead:

| Condition | Type | Code |
|---|---|---|
| Remote answered with a code this codebase has no type for | `UnmappedRemoteFailureException` | `COMMON.REMOTE_CALL_FAILED` (or `REMOTE_TIMEOUT` for a 504/408 *response*) |
| No response at all — refused, DNS, no route | `RemoteServiceUnavailableException` | `COMMON.REMOTE_UNAVAILABLE` |
| No response in time | `RemoteServiceTimeoutException` | `COMMON.REMOTE_TIMEOUT` |

The last two are separate types because the remedies differ, and because a timeout means the request probably *did* arrive — treating it as a failure can contradict work the remote completed. A remote that answers 504 is the first case, not the third: it answered, its own downstream timed out.

### 2. The payment client SDK — `payment-external-api`

New package `com.asrevo.cvhome.payment.api.errors`:

```
abstract PaymentApiException extends RemoteServiceException      ← catch-all for "the payment API failed"
 ├── PaymentGatewayRejectedException     PAYMENT.INITIATE.FAILED
 ├── PaymentConfigurationMissingException PAYMENT.CONFIGURATION.MISSING
 ├── PaymentTypeUnsupportedException     PAYMENT.PROCESSOR.UNSUPPORTED
 └── PaymentApiUnavailableException      transport failure / opaque 5xx
```

Each with a `from(RemoteErrorContext)` factory, built through `RemoteServiceException.of(code, Factory)` so `remoteService`/`remoteCode`/`remoteStatus` survive. `PaymentErrorCatalog implements RemoteErrorCatalog` maps the codes, registered via
`payment-external-api/src/main/resources/META-INF/services/com.asrevo.cvhome.errors.remote.RemoteErrorCatalog`.

No Gradle change: `payment-external-api` already declares `api project(':store-pod:payment:payment-commons')`, and `checkout-core` declares `api project(':store-pod:payment:payment-external-api')`, so checkout compiles against these types today.

Note the codes `PAYMENT.CONFIGURATION.MISSING` and `PAYMENT.PROCESSOR.UNSUPPORTED` exist in `PaymentErrors` but are never thrown — `PaymentGatewayService` logs a warning and returns `PaymentInitiateResult.failed()`. Making them real server-side throws is **Step 2c**, tracked below; the catalog entries are harmless until then.

### 3. Delivering the type through the proxy — `WebClientsUtils`

The missing link. After `HttpServiceProxyFactory` creates the client, wrap it in a JDK dynamic proxy that opens the carrier when — and only when — the method declares the type:

```java
private static <T> T withTypedErrors(Class<T> type, T target) {
    return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
            (proxy, method, args) -> {
                try {
                    return method.invoke(target, args);
                } catch (InvocationTargetException e) {
                    throw rethrow(method, e.getCause());
                }
            });
}

private static Throwable rethrow(Method method, Throwable thrown) {
    if (thrown instanceof UncheckedBaseException carrier) {
        for (Class<?> declared : method.getExceptionTypes()) {
            if (declared.isInstance(carrier.getCause())) {
                return carrier.getCause();          // caller catches the named type
            }
        }
    }
    return thrown;                                   // undeclared: carrier flows to the advice
}
```

Applied once in `buildClient(...)`, so all eight `@HttpExchange` clients inherit it. An undeclared type still renders correctly at the edge, so this is additive — nothing breaks for a client that declares nothing.

### 4. Transport failures — a `ClientHttpRequestInterceptor`

Added in the `RestClient` overload of `WebClientsUtils.build(...)`, where the request URI is available:

```java
.requestInterceptor((request, body, execution) -> {
    try {
        return execution.execute(request, body);
    } catch (IOException | ResourceAccessException e) {
        throw new UncheckedBaseException(RemoteProblemTranslator.unreachable(request.getURI(), e));
    }
})
```

`unreachable(...)` stops being dead code. Extend it to consult the registry so an unreachable payment service yields `PaymentApiUnavailableException`, and to classify read-timeout as `REMOTE_TIMEOUT` rather than `REMOTE_UNAVAILABLE`.

### 4b. Two roles, two interfaces — the wrapper

**A defect found after the first cut shipped.** `ExternalPaymentGatewayApi implements ExternalPaymentGatewayService`: the same interface is the controller's contract *and* the generated client proxy. Declaring the caller-side exceptions on it was wrong in three ways — the controller inherited a signature listing four exceptions it can never throw (Java allows narrowing, so it compiled silently); the server was blocked from ever declaring a failure of its own, because Java forbids widening; and the `server throws X → code → catalog rebuilds Y` chain had three links nothing kept in sync.

The split:

```java
// payment-external-api — the wire contract, implemented by the controller. Server vocabulary.
public interface ExternalPaymentGatewayService {
    PaymentInitiateResult initiatePayment(...) throws PaymentInitiateRejectedException;
}

// payment-external-api — what callers depend on. Caller vocabulary.
public interface PaymentGatewayClient {
    PaymentInitiateResult initiatePayment(...)
            throws PaymentGatewayRejectedException, PaymentApiUnavailableException;
}

public class RestPaymentGatewayClient implements PaymentGatewayClient {
    public PaymentInitiateResult initiatePayment(...) throws ... {
        try {
            return delegate.initiatePayment(store, request);
        } catch (PaymentInitiateRejectedException e) {
            throw PaymentGatewayRejectedException.wrapping(e);   // original kept as the cause
        } catch (UncheckedBaseException e) {
            throw unreachable(e);                                 // undecided, never a rejection
        }
    }
}
```

The catalog now maps `PAYMENT.INITIATE.FAILED` → `PaymentInitiateRejectedException::from` — **the class the server threw**. Because the wire interface declares that type, `WebClientsUtils` narrows the carrier into it, and the wrapper receives the original rather than a carrier. Division of labour: *catalog = deserialisation, wrapper = policy.* The one thing with no server counterpart is `PaymentApiUnavailableException`, since a service that never answered never threw anything.

`remoteService()` stays truthful on both sides: `stripe` on the server's exception (read back from the `provider` param, not the connection), `payment` on the caller's.

**Consequence: the server had to start propagating.** `PaymentGatewayService.initiatePayment` was catching `PaymentInitiateRejectedException` and returning `failed()`, so the endpoint answered HTTP 200 with no reason — the whole typed path was unreachable in production. It now rethrows after logging. Checkout's behaviour is unchanged: `OrderPlacementFacadeImpl` turns the rejection back into a failed result, which is the branch that releases the reservation. Only the *reason* is new, and it is what the client SDK rebuilds from.

The two client types with no server counterpart yet (`PaymentConfigurationMissingException`, `PaymentTypeUnsupportedException`) were **deleted**: no configuration and no processor are still `failed()` results, so nothing could ever throw them. They come back in Step 2c, in pairs, when the server actually throws.

### 5. Declaring failures on the interface

```java
@HttpExchange("/api/v1/private")
public interface ExternalPaymentGatewayService {

    /**
     * @throws PaymentGatewayRejectedException  payment-service refused the request
     * @throws PaymentApiUnavailableException   payment-service could not be reached
     */
    @PostExchange("/payments/initiate")
    PaymentInitiateResult initiatePayment(StoreMerchantId store, @RequestBody PaymentRequest request)
            throws PaymentGatewayRejectedException, PaymentApiUnavailableException;
}
```

This is what forces `OrderPlacementFacadeImpl:122` to handle the case it silently ignores today — release the reservation and fail the order cleanly, distinguishing "declined" from "unreachable".

### 6. Preserving the code across the hop — `RemoteProblemTranslator` + `ProblemDetailFactory`

- Translator parses `params` and `fieldErrors` from the remote body as well as `code`/`detail` (today only the latter two), so the reconstructed exception carries the remote's structured context.
- `ProblemDetailFactory` gains a `remote(...)` variant: `code` = `remoteCode()` when present, plus `remoteService` and `remoteStatus` extension properties. `GlobalErrorHandler.render` calls it for `RemoteServiceException` instead of only overriding the status.

Without this, a two-hop failure loses its identity and the registry has nothing to key on at the second hop.

---

## Sequencing

Slots into the parent plan as **Step 2b**, after payment (Step 2) and before store-core (Step 3): payment is the only pod with typed errors, and checkout is its caller.

| Sub-step | Work |
|---|---|
| **2b.1** | SPI + `RemoteErrorRegistry` in `store-commons:errors`; translator parses full payload; `ProblemDetailFactory.remote(...)`; `GlobalErrorHandler` uses it. No behaviour change for unregistered codes. |
| **2b.2** | `WebClientsUtils`: unwrapping proxy + transport interceptor. Still no behaviour change — nothing declares a type yet. |
| **2b.3** | `payment-external-api` exception family + `PaymentErrorCatalog` + `META-INF/services`; `IPaymentGatewayService` declares its failures. |
| **2b.4** | `OrderPlacementFacadeImpl` handles them: release the reservation on rejection, distinguish unavailable, keep the order in a recoverable state. This is the sub-step that pays for the other three. |

**All four sub-steps are implemented; `./gradlew build` is green.** How 2b.4 landed: the three definitive refusals (rejected / configuration missing / type unsupported) are caught in `doOrderPaymentInitiate` and returned as `PaymentInitiateResult.failed()`, which flows into the existing FAILED branch that releases the reservation and cancels the order. `PaymentApiUnavailableException` is deliberately *not* caught — nothing was decided, so cancelling would be a guess that could contradict a payment that did start. It propagates through `OrderPlacementFacade.placeOrder` and `OrderApi`, both of which now declare it, leaving the order reserved and pending for reconciliation while the client gets a 502. Adding it to the interface broke `OrderApi` at compile time, which is precisely the forcing function this design exists for.

Each later migration step then adds one catalog for the module's own `-external-api`. Highest value: **Step 6**, where `OrderInventoryOrchestratorImpl` can finally tell `CATALOG.INVENTORY.INSUFFICIENT` from "catalog is down" instead of returning `status(false)` for both.

**Pattern for the remaining seven clients** (one per later migration step): wire interface keeps the server's vocabulary; a `<Service>Client` + `Rest<Service>Client` pair in the same `-external-api` module carries the caller's; the catalog maps codes to the server's classes; the `-service` module's `ClientsConfig` exposes the wrapper as the bean, never the raw proxy.

**Adjacent finding, cheap to fix here:** `ExternalPaymentGatewayService.status` declares `@PathVariable("ref")` while the server's `ExternalPaymentGatewayApi.status` declares `@PathVariable("requestRef")`. The method has no caller in the repo, so the mismatch has never fired.

---

## Files

**Created**
- `store-commons/errors/.../errors/remote/`: `RemoteErrorCatalog.java`, `RemoteExceptionFactory.java`, `RemoteErrorContext.java`, `RemoteErrorRegistry.java`
- `store-pod/payment/payment-external-api/.../payment/api/errors/`: `PaymentApiException.java`, `PaymentGatewayRejectedException.java`, `PaymentConfigurationMissingException.java`, `PaymentTypeUnsupportedException.java`, `PaymentApiUnavailableException.java`, `PaymentErrorCatalog.java`
- `store-pod/payment/payment-external-api/src/main/resources/META-INF/services/com.asrevo.cvhome.errors.remote.RemoteErrorCatalog`

**Modified**
- `store-commons/autoconfigure/.../s2s/utils/WebClientsUtils.java` — unwrapping proxy, transport interceptor
- `store-commons/autoconfigure/.../s2s/error/RemoteProblemTranslator.java` — registry lookup, full payload parse, timeout classification
- `store-commons/autoconfigure/.../errors/web/ProblemDetailFactory.java` — `remote(...)`
- `store-commons/autoconfigure/.../errors/web/GlobalErrorHandler.java` — `render` uses it
- `store-pod/payment/payment-external-api/.../ExternalPaymentGatewayService.java` — declares its failures
- `store-pod/checkout/checkout-core/.../service/facade/checkout/OrderPlacementFacadeImpl.java` — handles them

---

## Verification

**Unit.** Extend `RemoteProblemTranslatorTest` (5 existing cases, pure-unit, no HTTP): a registered code yields the named type with `remoteCode`/`remoteStatus`/params intact; an unregistered code still yields plain `RemoteServiceException`; an unparseable body still degrades. Add a registry test asserting the payment catalog is discovered via `ServiceLoader` from the classpath.

**Round-trip — the gap that matters most.** *(Implemented: `TypedRemoteErrorRoundTripTest`, four cases, all passing.)* There is no test today that exercises `@HttpExchange` → error handler → carrier → caller. `MockRestServiceServer` is available transitively via `spring-boot-starter-test` in `store-commons/autoconfigure`; MockWebServer is not in the repo. Bind it to a `RestClient.Builder`, build a probe `@HttpExchange` interface through `WebClientsUtils.build(...)`, and assert:
- a 502 with `{"code":"PAYMENT.INITIATE.FAILED"}` → the call throws `PaymentGatewayRejectedException`, catchable by name;
- an undeclared code → `UncheckedBaseException` still escapes (advice renders it);
- connection refused → `PaymentApiUnavailableException`;
- a method declaring nothing behaves exactly as before.

**Advice.** Extend `GlobalErrorHandlerTest` / `ProbeApi`: a `RemoteServiceException` with `remoteCode` set renders `code` = the remote code, `remoteService`/`remoteStatus` present, status 502 for a remote 5xx and pass-through for a remote 4xx.

**End-to-end, local (`docker-compose-lcl`).** Add to the parent plan's acceptance table:

| After | Check | Today |
|---|---|---|
| 2b | Place an order with payment-service **stopped** → checkout returns 502 `PAYMENT.INITIATE.FAILED` (or `COMMON.REMOTE_UNAVAILABLE`), and the inventory reservation is **released** | exception escapes `placeOrder`; reservation leaks |
| 2b | Place an order with Stripe keys invalid → 502 carrying `remoteService: payment` and payment's own code, not `COMMON.REMOTE_CALL_FAILED` | remote code dropped at the first hop |

`./gradlew :store-commons:errors:build :store-commons:autoconfigure:build :store-pod:payment:payment-external-api:build :store-pod:checkout:checkout-service:build`, then `./gradlew build`.

Two test-only Gradle additions were needed in `store-commons/autoconfigure`: `testImplementation project(':store-commons:commons')` (the custom argument resolvers reference its domain types, which are `compileOnly` there, so a client proxy cannot be invoked at test time without them) and `testImplementation libs.spring.webflux` (`WebClientsUtils` touches both client stacks). Neither changes what production consumers get.

---

## Out of scope

- **Typed unwrapping on the `WebClient`/reactive path.** 2b.1 gives that path a `defaultStatusHandler` so it stops surfacing raw `WebClientResponseException`, but the error travels inside a `Mono`, where a dynamic proxy cannot rethrow it as a declared checked type. Only `ExternalPodClient.listPods` is reactive, and `PodClient` already handles it with `onErrorResume`. A reactive caller uses `.onErrorMap`.
- **Retry/circuit-breaking.** The registry makes "retryable vs permanent" expressible as a type; acting on it is separate work.
- **The other six clients' catalogs** — one per later migration step, as their codes are defined.
- **Making `PAYMENT.CONFIGURATION.MISSING` / `PAYMENT.PROCESSOR.UNSUPPORTED` real server-side throws** (Step 2c): it changes `initiatePayment`'s contract with checkout from a `failed()` result to a propagated error.

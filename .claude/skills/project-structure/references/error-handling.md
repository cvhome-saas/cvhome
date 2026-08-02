# Error handling

Every failure in cvhome carries a stable machine-readable code, a category that decides the HTTP status,
structured params, and field-level errors where they apply — emitted in one format across all 8 services, with
internal detail confined to logs and joined to the response by `traceId`.

This is the distilled result of three refactors (`.claude/plans/help-me-set-plan-curried-fountain.md`,
`claude-plans-help-me-set-plan-curried-f-cached-grove.md`, `store-commons-autoconfigure-src-main-ja-tidy-fox.md`).
Each corrected the one before it. The corrections are the most useful part of this document — see
**Lessons learned the hard way**.

## Migration status

**The wire format is universal; the typing is not yet.** A legacy bridge in `store-pod/commons/store-commons`
(`ServiceException`, `GenericRuntimeException`, `ServiceRuntimeException` → `LegacyErrors`) makes every
un-migrated throw site render in the correct format with the right status, under a `LEGACY.*` code. So a client
never sees the old shapes, but a `LEGACY.*` code means that endpoint has not been migrated and its signature still
says nothing.

Grepping `LEGACY.` measures the remaining work. As of this writing ~125 legacy throw sites remain, concentrated in
`catalog-core` (69), `content-core` (13), `checkout-core` (13) and `store-cms-commons` (12). Payment, the
`store-commons` roots and the s2s layer are fully migrated — payment is the reference implementation to copy.
The bridge and its four classes are deleted in the final step; a repo-wide grep for the deleted types is the
completion gate.

## Where the code lives

| | |
|---|---|
| `store-commons/errors` | The whole type system. **Plain Java, zero dependencies** — no Spring, no logging, no Jackson at runtime. It is `api` from both commons roots, so every module sees it |
| `store-commons/autoconfigure` | `errors/web/` — the advice, `ProblemDetailFactory`, autoconfiguration. `s2s/error/` — `S2sErrorHandler`, `RemoteProblemTranslator` |
| `<domain>-commons/.../errors/` | Per-context `ErrorCode` enum + the condition-named exceptions that service throws |
| `<domain>-external-api/.../api/errors/` | The client SDK's caller-side types + its `RemoteErrorCatalog` constant |

Keeping `store-commons:errors` dependency-free is load-bearing: it is why an `-external-api` module can declare an
error contract without depending on `autoconfigure`, and why adding a logging framework there was rejected in
favour of `System.Logger` when the (now-deleted) registry needed to warn.

## The wire format

Extended RFC-7807 `ProblemDetail`. One shape, all services, built only by `ProblemDetailFactory`.

```json
{
  "type": "https://errors.asrevo.com/catalog/product/not-found",
  "title": "CATALOG.PRODUCT.NOT_FOUND",
  "status": 404,
  "detail": "Product [42] does not exist in store [7]",
  "code": "CATALOG.PRODUCT.NOT_FOUND",
  "category": "NOT_FOUND",
  "params": { "productId": 42, "storeId": 7 },
  "fieldErrors": [ { "field": "sku", "code": "...", "message": "..." } ],
  "traceId": "3f9a1c8e"
}
```

Plus, depending on **who** failed: `remoteService` / `remoteStatus` for a peer cvhome service, or
`provider` / `providerCode` / `providerStatus` for a third party. Never both — they mark different boundaries.

`detail` never carries root-cause text in production. `ErrorHandlingProperties.includeDebugDetail` (default
`false`, on in `lcl`) restores it locally. `traceId` is the Micrometer trace id when present, else a short UUID,
and is logged with the stack trace so a user-reported id leads to the server log line.

## The hierarchy

```
ErrorCodeAware (interface)          errorCode(); params(); fieldErrors()

abstract BaseException extends Exception implements ErrorCodeAware     ← checked root
 ├── abstract ValidationException              400, carries fieldErrors
 ├── abstract ResourceNotFoundException        404
 ├── abstract DuplicateResourceException       409
 ├── abstract OperationNotAllowedException
 ├── abstract AccessDeniedStoreException       403
 ├── abstract ConversionException              400
 ├── abstract StoreIOException                 500, wraps java.io.IOException
 ├── abstract RemoteServiceException           another cvhome service failed
 │    ├── UnmappedRemoteFailureException       it answered with a code we have no type for
 │    ├── RemoteServiceUnavailableException    no response at all — refused, DNS, no route
 │    └── RemoteServiceTimeoutException        no response in time — the request may still have succeeded
 └── abstract ExternalProviderException        a third party failed — Stripe, PayPal, a carrier

UncheckedBaseException extends RuntimeException implements ErrorCodeAware
    // the sole unchecked type; a carrier for lambda and Spring-callback boundaries. Never thrown to signal a failure.
```

Checked everywhere, deliberately: the signature is where a caller learns what can go wrong, and a new failure mode
then breaks the build at every site that has to decide what it means. Where Java gets in the way — a `Function`
inside `stream().map(...)` cannot throw checked — wrap with `Unchecked`; the advice unwraps the carrier
transparently, so one that escapes still renders correctly instead of becoming a 500.

`ErrorCategory` holds the status as a plain `int` (no Spring in this module): `VALIDATION`/`MALFORMED`/`CONVERSION`
400, `UNAUTHENTICATED` 401, `FORBIDDEN` 403, `NOT_FOUND` 404, `CONFLICT` 409, `PAYLOAD_TOO_LARGE` 413,
`UNPROCESSABLE` 422, `STORAGE`/`INTERNAL` 500, `REMOTE_SERVICE` 502, `TIMEOUT` 504.

## The rules

These also live in `BaseException`'s javadoc, so they are read where they are needed.

1. **Never throw a generic type** — not `BaseException`, not a category base. Throw a class whose *name* states
   the condition. The bases are abstract, so this is a compile error, not a review comment.
2. **Never declare a generic type.** `throws BaseException` tells a caller only "something may fail", which it
   already knew.
3. **The condition names the class; the category names the parent.**
   `NonPositivePriceException extends ValidationException` is a 400 because validation failures are — nothing at
   the throw site restates the status.
4. **Attaching a better `ErrorCode` to a generic exception is not a migration.** It improves the response body
   while leaving the signature — the part a caller reads — saying nothing.
5. **One class per condition, with a static factory that names its inputs.**
   `PriceNotParseableException.of(amount, cause)` beats a builder chain repeated at every site: the params a
   support engineer will search on are guaranteed rather than remembered.
6. **Catch narrowly.** Catch the named types, or a category base when the handling genuinely is per-category.
   Catching `BaseException` to switch on `category()` re-creates at runtime the distinction the type system was
   making for free.
7. **Name the condition, not the category** — `DuplicateSkuException`, not `CatalogDuplicateException`.

The shape, once per condition:

```java
public class ProductNotFoundException extends ResourceNotFoundException {
    protected ProductNotFoundException(ErrorPayload payload, Throwable cause) { super(payload, cause); }

    public static ProductNotFoundException of(Long productId, StoreMerchantId storeId) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_NOT_FOUND, ProductNotFoundException::new)
                .param("productId", productId).param("storeId", storeId).build();
    }
}
```

## Who failed — the three-way distinction

This is the axis the type system exists to keep straight, and the one that cost the most to get wrong.

| Failure | Base | Code on the wire | Status |
|---|---|---|---|
| This service | a local category base | ours | from our category |
| Another cvhome service | `RemoteServiceException` | **the remote's**, re-emitted | remote 4xx passes through; remote 5xx → 502 |
| A third party (Stripe, PayPal) | `ExternalProviderException` | **ours**; theirs rides along as `providerCode` | from our category, never theirs |

Re-emission is sound for a peer because it speaks our problem-detail contract: its `code` is from a catalogue we
publish, and its status means what our statuses mean. A third party shares neither. `GlobalErrorHandler.renderBody`
picks between the three; `ProblemDetailFactory.remote(...)` and `.external(...)` are the two special shapes.

Within a provider call, the second distinction is **whether anything was decided**:

```java
} catch (CardException e) {                       // an answer: the card was refused, retrying won't help
    throw PaymentInitiateRejectedException.of(...);   // PAYMENT.INITIATE.REJECTED, 422
} catch (StripeException e) {                     // no decision: no connection, rate limit, bad API key
    throw PaymentProviderUnavailableException.of(...); // PAYMENT.INITIATE.FAILED, 502
}
```

A caller may unwind an order on a rejection. On an undecided failure it must **hold and reconcile** — the payment
may have started. Collapsing the two is how orders get cancelled after being charged.

## Service-to-service: the `-external-api` module is a client SDK

Stripe's SDK decodes its error body into `CardException`, `RateLimitException`, `ApiConnectionException` so a
caller branches on type. Ours does the same.

**Two roles, two interfaces.** The `@HttpExchange` interface is implemented by the *server's controller*, so its
`throws` clause carries the **server's** vocabulary. A hand-written wrapper beside it carries the **caller's**.
One signature cannot be honest about both.

```java
// the wire contract — implemented by ExternalPaymentGatewayApi. Server vocabulary.
public interface ExternalPaymentGatewayService {
    PaymentInitiateResult initiatePayment(...)
            throws PaymentInitiateRejectedException, PaymentProviderUnavailableException;
}

// what callers depend on. Caller vocabulary.
public interface PaymentGatewayClient {
    PaymentInitiateResult initiatePayment(...)
            throws PaymentGatewayRejectedException, PaymentApiUnavailableException;
}
```

`ClientsConfig` exposes the **wrapper** as the bean, never the raw proxy.

**The error contract is a constant, passed explicitly.**

```java
// payment-external-api
public static final RemoteErrorCatalog CATALOG = RemoteErrorCatalog.builder()
        .map(PaymentErrors.INITIATE_REJECTED, PaymentGatewayRejectedException::from)
        .map(PaymentErrors.INITIATE_FAILED,   PaymentApiUnavailableException::from)
        .unreachable(PaymentApiUnavailableException::from)
        .build();

// checkout ClientsConfig — third argument is nullable; null means "this API names none of its failures"
restClientBuilder.buildClient(PAYMENT_SERVICE_NAME, ExternalPaymentGatewayService.class, PaymentApiErrors.CATALOG);
```

Mappings are keyed by `ErrorCode`, not by string, so renaming a code cannot silently orphan an entry. Unmapped
codes fall back to `UnmappedRemoteFailureException`, which still carries the remote's code and status — the name is
the point: seeing one in a log is the signal that a code deserves an entry.

**How the type reaches the caller.** `S2sErrorHandler` owns both failure paths — an error response
(`defaultStatusHandler`) and no response at all (`requestInterceptor`, which is the only place a refused connection
or read timeout can be caught). The translated exception is checked, but Spring's hooks may only throw
`IOException` and unchecked exceptions, so it travels inside `UncheckedBaseException`. `WebClientsUtils` wraps the
generated proxy in a JDK dynamic proxy whose rule is `S2sErrorHandler.declaredOrCarrier`: **the invoked method's
declared exception types are the authority.** A declared cause is rethrown as itself; anything undeclared stays in
the carrier and the shared advice renders it at the edge. A client that declares nothing behaves exactly as it did
before typed errors existed.

The reactive `WebClient` path gets translation but **not** unwrapping — the failure travels inside a `Mono`, where
no proxy can rethrow it as a declared checked type. A reactive caller uses `onErrorMap`.

## Lessons learned the hard way

Each of these was shipped wrong first. They are the reason the design looks the way it does.

**Abstract bases are the enforcement, not the documentation.** Rules 1–7 were review comments that got skipped
under deadline. Making every category base `abstract` turned rule 1 into a compile error. Rule 2 still compiles,
which is why it needs a grep gate (below).

**One exemption rots the whole scheme.** `RemoteServiceException` was left concrete on the argument that a remote
can fail in ways we have no type for. That single exemption left a generic type throwable from anywhere — and
`RemoteProblemTranslator` was in fact throwing it. "We have no name for this" is itself a nameable condition:
`UnmappedRemoteFailureException`. There are now no exemptions.

**A better code on a legacy exception is not a migration.** Both Step 1 and Step 2 shipped this on the first pass.
It produces a plausible-looking diff, an improved response body, and a signature that still says nothing. A
`ServiceException(ErrorCode, String)` constructor was added as a migration shortcut and then deliberately removed.

**Mis-scoped advice is silent.** All four `RestErrorHandler` copies declared
`@ControllerAdvice({"com.asrevo.cvhome.store.controller"})` while every real API lives in
`com.asrevo.cvhome.<service>.api.v1.*` — so error handling was dead code in four services. There is now one
`@ControllerAdvice` with **no** basePackages, plus `AdviceScopeTest`, because this bug is invisible and would
otherwise return.

**Code that exists is not code that runs.** `RemoteProblemTranslator.unreachable(...)` was called only from its own
test — transport failures escaped as raw `ResourceAccessException` in production. Separately,
`PaymentGatewayService` was catching its own rejection and returning `failed()`, so the endpoint answered HTTP 200
and the entire typed path was unreachable. Both looked complete and tested.

**One type for two boundaries costs two bugs.** `RemoteServiceException` briefly served both peer-service and
third-party failures. Because a provider's code was re-emitted as ours, a Stripe decline went out as
`code: "card_declined"`, the caller's catalog matched nothing, and a definitive refusal reached checkout as
"undecided" — orders held forever that were never going to be paid. And because a provider's 4xx passed through,
Stripe answering 401 for *our* bad API key told the *shopper* they were unauthenticated. Splitting
`ExternalProviderException` out fixed both at once.

**Implicit registration must earn its keep.** Catalogs were discovered by `ServiceLoader` from a `META-INF/services`
file per module, which forced an `apis()` method onto every catalog (to key by interface, because the URI is
rewritten by load-balancer resolution before any handler sees it) and a ~90-line registry to hold it. For **11 call
sites, one of which had a contract**, that bought nothing: a missing or misspelt services file degraded silently.
Passing the catalog to `buildClient(...)` deleted the registry, both services files, and `apis()`. Choose implicit
discovery only for a genuinely open set of providers.

**A shared signature narrows outward.** Removing a generic type from `PriceUtils.getAmount` broke both its callers,
which had been catching `ServiceException` and rethrowing a generic runtime type — discarding the distinction the
parser had just made. A two-line collateral change downstream is the normal price, and it is cheapest early.

**Stale javadoc survives every compiler.** After renaming `PaymentErrorCatalog` → `PaymentApiErrors`, seven doc
comments still named the deleted class, two of them also describing behaviour that had been reversed a session
earlier. Prose compiles fine while actively misleading. Grep for deleted type names, not just for code references.

## Checklists

**Adding a failure mode to a service**

1. Add the constant to that context's `ErrorCode` enum (`PaymentErrors`, `CatalogErrors`, …) with the category that
   fixes its status.
2. Add one exception class in the same `-commons` module, extending the category base that matches *who failed*,
   with a static factory naming its inputs.
3. Declare it on the method that throws it, by name. Fix the compile errors that follow — that is the design
   working.
4. If callers over HTTP need to branch on it, add a caller-side type and a `.map(...)` entry to that
   `-external-api` module's catalog constant.

**Adding a cross-service call** — see `service-to-service.md` for the transport side. For errors: decide whether the
called API names any failures. If not, pass `null` as the third argument to `buildClient`. If it does, pass its
`*ApiErrors.CATALOG` constant, and remember the caller-side types are deliberately absent from the wire interface's
`throws` clause, so the wrapper opens the carrier.

## Verification patterns

- **Grep gate for rule 2** — throwing a base is a compile error, but *declaring* one still compiles, so this is
  what catches a step that swapped one generic type for another:

  ```
  grep -rn "throws \(BaseException\|ValidationException\|ResourceNotFoundException\|RemoteServiceException\|ExternalProviderException\|DuplicateResourceException\|OperationNotAllowedException\|AccessDeniedStoreException\|StoreIOException\)\b" <module>
  ```

  Run it **per migrated module**, not repo-wide, and know the two legitimate hits: `Unchecked.rethrow` genuinely
  declares `throws BaseException` (that is its job — restoring the checked type at the enclosing method), and
  `BaseException`'s own javadoc quotes the rule. `ConversionException` is deliberately **omitted** from the
  pattern: the deprecated `com.asrevo.cvhome.store.core.exception.ConversionException` is still declared across
  the populator layer in the modules that have not migrated yet, so including it drowns the signal. Add it back
  when checking a module that has migrated.
- **Grep gate after a rename or deletion** — search the old type name across `*.java` *including comments*.
- **Wire-format tests** — `GlobalErrorHandlerTest` (10 cases) pins `code`/`category`/`status`/`traceId`, that
  `detail` leaks no root cause, and the three who-failed shapes. `ProbeApi` is the fixture that lets a probe
  exception be thrown through a real controller.
- **Round-trip test** — `TypedRemoteErrorRoundTripTest` (5 cases, `MockRestServiceServer`) is the only thing
  covering `@HttpExchange` → handler → carrier → declared type. It includes the case that a method declaring
  nothing is unaffected, and that a `null` catalog degrades rather than NPEs.
- **Pure-refactor discipline** — `RemoteProblemTranslatorTest` (9 cases) is the decoder's behaviour pin. When a
  change is meant to be a move rather than a rewrite, *no assertion in it may change*; if one does, the refactor
  changed behaviour.
- `./gradlew build` runs checkstyle repo-wide and must stay clean.

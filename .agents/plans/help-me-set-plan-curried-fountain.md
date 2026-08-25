> **SUPERSEDED — history only.** Merged into `error-handling.md`, which is the plan of record. Parts of this
> file describe designs that have since been replaced; do not implement from it.

# Error Handling Refactor — typed, coded, project-wide

## Context

Errors in cvhome collapse into one generic shape by the time they reach a UI. Four compounding causes, all verified in the current tree:

1. **One exception for every failure mode.** `ServiceException` (checked, 45 throws) and `GenericRuntimeException` (unchecked, root of ~230 throws) are used identically for "entity not found", "price calculation failed", "S3 upload failed", "remote API call failed", and "validation failed". Nothing on the exception distinguishes them — `ServiceException`'s `messageCode` field is written at 7 sites and **read nowhere** (`getMessageCode` has zero call sites); its `int exceptionType` only ever holds `500` or `120`.
2. **The advice that was supposed to translate them is mis-scoped.** All four copies of `RestErrorHandler` declare `@ControllerAdvice({"com.asrevo.cvhome.store.controller"})`, but every real API lives in `com.asrevo.cvhome.<service>.api.v1.*`. The advice does not apply to the controllers it was written for.
3. **When it does apply, it NPEs.** `handleServiceException(Exception)` calls `Objects.requireNonNull(exception.getCause())`, and the `ServiceRuntimeException` handler dereferences `getCause()` unguarded. The overwhelming majority of throws (all 92 `ResourceNotFoundException` sites, all 86 `ServiceRuntimeException` sites) are constructed from a message with no cause → NPE inside the handler → the client gets a bare 500.
4. **Three incompatible wire formats.** Pods emit `ErrorEntity {errorCode, message}`; uaa/cua emit RFC-7807 `ProblemDetail` (no `message` field at all); control-plane emits Spring's default `{timestamp,status,error,path}` from an **empty** `@ControllerAdvice`. No frontend type models any of them.

Consequences visible today: a remote 400 from catalog surfaces to the browser as a local **500** carrying the remote service's root-cause stack text (no `@HttpExchange` client translates errors, and nothing in the repo catches `HttpClientErrorException`); `@Valid` failures never produce field-level errors because no `MethodArgumentNotValidException` handler exists anywhere; and internal root-cause strings are concatenated into the client-facing `message` by `createErrorEntity`.

**Outcome:** every failure carries a stable machine-readable code, a category that determines HTTP status, structured params, and field-level errors where applicable — emitted in one format across all 8 services, with internal detail confined to logs and correlated by `traceId`.

## Decisions taken

| | |
|---|---|
| Wire format | Extended RFC-7807 `ProblemDetail` (+ `code`, `category`, `params`, `fieldErrors[]`, `traceId`) |
| Migration | Full rewrite of all throw sites, sequenced **one module per step, smallest module first** |
| Exception style | **Checked everywhere** — business errors included — under a checked root, with a single documented unchecked carrier for lambda boundaries |
| Exception granularity | **A condition-named exception per failure mode**, declared by name in the signature. The category bases (`ValidationException`, `ResourceNotFoundException`, …) supply the HTTP status and are **abstract**, so throwing or declaring a generic type does not compile |
| Root type | `BaseException` — abstract, checked. Named for what it is (the base of every application failure), not for stores; the unchecked carrier is `UncheckedBaseException` |
| Scope | **Backend only.** Frontend consumption (seller-ui interceptor, typed errors, field binding) is deliberately out of scope and planned separately |
| Handler bugs | Fix all four: advice scope + NPEs, root-cause leakage, s2s translation, bean-validation handler |

**Flagged, proceeding as decided:** checked-everywhere costs the most in catalog's mapper/populator layer, where ~200 throw sites sit inside `stream().map(...)`. `java.util.function.Function` cannot throw checked exceptions, so those call sites need either a try/catch or a wrapper. Step 0 ships `Unchecked` helpers so this stays a one-token change per lambda rather than a five-line try/catch; the advice unwraps the carrier transparently. Because catalog is now migrated last, Step 4 (`customer-core`'s populators) is the earliest step that exercises `Unchecked` in anger — treat it as the ergonomics checkpoint, and revisit the checked-everywhere decision there rather than after 191 sites are already converted.

---

## Target design

### New module `store-commons:errors`

Plain Java, no Spring dependency, so both trees can depend on it. Registered in `../../settings.gradle` alongside `store-commons:commons`, then exposed as `api project(':store-commons:errors')` from **both** `../../store-commons/commons/build.gradle` and `../../store-pod/commons/store-commons/build.gradle` — those two are the roots that every other module already pulls in.

**Error code SPI** — per-module enums implement it; no central god-enum.

```java
public interface ErrorCode {
    String code();               // "CATALOG.PRODUCT.NOT_FOUND"
    ErrorCategory category();    // drives HTTP status
    default String titleKey() { return code(); }
}

public enum ErrorCategory {
    VALIDATION(400), UNAUTHORIZED(401), FORBIDDEN(403), NOT_FOUND(404),
    CONFLICT(409), PAYLOAD_TOO_LARGE(413), UNPROCESSABLE(422),
    CONVERSION(400), REMOTE_SERVICE(502), TIMEOUT(504), STORAGE(500), INTERNAL(500);
}
```

One enum per bounded context: `CommonErrors`, `CatalogErrors`, `CheckoutErrors`, `MerchantErrors`, `ContentErrors`, `PaymentErrors`, `CustomerErrors`, `UaaErrors`, `ControlPlaneErrors`. Codes are namespaced `<CONTEXT>.<RESOURCE>.<CONDITION>` — this is the vocabulary the UI will later key translations off, so it is the contract, not an implementation detail.

**Hierarchy** — checked root, one unchecked carrier:

```
ErrorCodeAware (interface)
    ErrorCode errorCode();  Map<String,Object> params();  List<FieldError> fieldErrors();

abstract BaseException extends Exception implements ErrorCodeAware     ← checked root
 ├── abstract ResourceNotFoundException      (params: resourceType, id, storeId)
 ├── abstract ValidationException            (carries fieldErrors)
 ├── abstract DuplicateResourceException
 ├── abstract OperationNotAllowedException
 ├── abstract AccessDeniedStoreException
 ├── abstract ConversionException            (keeps the name 30 existing sites use)
 ├── abstract StoreIOException               (S3/file; wraps java.io.IOException as cause)
 └── abstract RemoteServiceException         (s2s; carries remote code + remote status)
      ├── UnmappedRemoteFailureException     the remote answered with a code we have no type for
      ├── RemoteServiceUnavailableException  no response at all — refused, DNS, no route
      └── RemoteServiceTimeoutException      no response in time — the request may still have succeeded

UncheckedBaseException extends RuntimeException implements ErrorCodeAware
    // sole unchecked type. Carrier for lambda/stream and Spring-callback boundaries.
```

**Every base is `abstract`, with no exceptions, and that is the enforcement mechanism.** The rules below are otherwise review comments that get skipped under deadline; as abstract classes, `throw new ValidationException(...)` does not compile. The root is named `BaseException` rather than `StoreException` because it is the base of *all* application failures, not of store-scoped ones — `StoreIOException` and `AccessDeniedStoreException` keep "Store" because they genuinely are about a store's assets and a store's data.

`RemoteServiceException` was briefly exempted and concrete, on the argument that a remote service fails in ways this codebase has no type for and `RemoteProblemTranslator` has to represent whatever came back. That was wrong twice over: "we have no name for this" is itself a nameable condition, and leaving one base concrete left a generic type throwable from anywhere — which is the single thing these rules exist to prevent. Its three concrete conditions are now named, and they are the only things the translator builds. Locally raised remote failures get their own name through `RemoteServiceException.of(code, Factory)`, as `PaymentInitiateRejectedException` does.

`FieldError` is a record `(String field, String code, String message, Map<String,Object> params)`.

### Condition-named exceptions per bounded context

The classes above are **category bases**: each one exists to fix an HTTP status and a rendering shape. Nothing throws them and no signature declares them. Each bounded context defines its own exceptions, one per failure mode, in its own `-commons` module next to its `ErrorCode` enum, and a method declares exactly the ones it can produce:

```java
public interface PaymentProcessor {
    PaymentInitiateResult initiate(...) throws PaymentInitiateRejectedException;

    WebhookResult parseWebhook(...) throws InvalidWebhookSignatureException,
                                           UnreadableWebhookPayloadException,
                                           UnexpectedWebhookObjectException;
}
```

Why this over `throws BaseException` with a distinguishing code: the signature is where a caller learns what can go wrong, and a code only helps at runtime. `PaymentGatewayService` has to discard an unauthentic webhook but retry a provider outage — with named types that is a multi-catch the compiler checks, and adding a retryable failure mode to a processor *breaks the build* at the place that has to decide. With one generic type it is an `if` on a category that silently mis-handles anything new.

Each class carries a static factory naming its inputs, so the params are enforced rather than remembered:

```java
throw UnexpectedWebhookObjectException.of(STRIPE, event.getId(), event.getType(), Session.class, e);
```

Two extension points in `store-commons:errors` exist for this: `ErrorBuilder`'s constructor is public (`new ErrorBuilder<>(CODE, MyException::new)`), and `RemoteServiceException.of(code, Factory)` builds a named subclass while keeping `remoteCode`/`remoteStatus`.

Cost: roughly 60–100 classes repo-wide, concentrated in catalog. They are ~15 lines each and mechanical.

Since the bases are abstract, the builder is only ever used *inside* a named subclass, never at a call site:
```java
new ErrorBuilder<>(CatalogErrors.PRODUCT_NOT_FOUND, ProductNotFoundException::new)
        .param("productId", id).param("storeId", storeId).build();
```

### The rules

These live in `BaseException`'s javadoc as well, so they are read where they are needed rather than in a plan file nobody opens twice.

1. **Never throw a generic type** — not `BaseException`, not a category base. Throw a class whose *name* states the condition. The bases are abstract, so this is a compile error, not a review comment.
2. **Never declare a generic type.** `throws BaseException` tells a caller only "something may fail", which it already knew. Declare the exact exceptions a method produces; a newly added one then breaks the build at every site that has to decide what it means.
3. **The condition names the class; the category names the parent.** `NonPositivePriceException extends ValidationException` is a 400 because validation failures are — nothing at the throw site restates the status.
4. **Attaching a better `ErrorCode` to a generic exception is not a migration.** It improves the body and leaves the signature saying nothing. (Both Step 1 and Step 2 shipped this on their first pass; see Step 1.)
5. **One class per condition, with a static factory that names its inputs** — `PriceNotParseableException.of(amount, cause)`. The params a support engineer will search on are then guaranteed rather than remembered.
6. **Catch narrowly.** Catch the named types, or a category base when the handling genuinely is per-category. Catching `BaseException` to switch on `category()` re-creates at runtime the distinction the type system was making for free.
7. **Name the condition, not the category** — `DuplicateSkuException`, not `CatalogDuplicateException`.

The only place these are relaxed is the deprecated bridge (`ServiceException` and friends), which is generic by definition and dies in Step 8. There is no other exemption — the last one, `RemoteServiceException`, was removed once its asymmetry showed up in review: `PaymentProcessor.parseWebhook` declared three named types under an abstract base while `initiate`'s hung off a concrete one.

**Lambda ergonomics** (`Unchecked` utility, same module) — required by the checked-everywhere decision:
```java
list.stream().map(Unchecked.fn(mapper::map)).toList();   // wraps BaseException → UncheckedBaseException
```
and at the enclosing service method, `Unchecked.rethrow(() -> ...)` restores the checked type. The advice also unwraps `UncheckedBaseException` directly, so an unwrapped carrier that escapes still produces the correct response rather than a 500.

### Global advice — `store-commons:autoconfigure`

**Why here:** `../../store-commons/autoconfigure` is already a dependency of uaa, gateway, control-plane-service, and all five pod services (verified), and already auto-configures via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` → `CvhomeSharedConfig`. Adding the handler there gives all 8 services one implementation with zero per-service wiring, replacing 4 duplicated `RestErrorHandler`s, 4 duplicated `FileUploadExceptionAdvice`s, 2 duplicated `GeneralExceptionHandler`s, and 1 empty stub.

`GlobalErrorHandler extends ResponseEntityExceptionHandler`, annotated `@ControllerAdvice` **with no basePackages** — this alone fixes the mis-scoping that currently disables error handling in all four pod services. Registered from a new `ErrorHandlingAutoConfiguration` added to the `.imports` file, guarded `@ConditionalOnWebApplication` + `@ConditionalOnMissingBean` so a service can still override.

Handles:

| Exception | Result |
|---|---|
| `BaseException` / `UncheckedBaseException` (any subclass) | status from `ErrorCategory`, full `code`/`params`/`fieldErrors` |
| `MethodArgumentNotValidException` (override) | 400 + `fieldErrors[]` from `BindingResult` — **new capability** |
| `ConstraintViolationException` | 400 + `fieldErrors[]` from violation paths |
| `MaxUploadSizeExceededException` | 413 (replaces the 4 `FileUploadExceptionAdvice` copies) |
| `AccessDeniedException` / `AuthenticationException` | 403 / 401 |
| `HttpClientErrorException` etc. leaking through | 502 via `RemoteServiceException` mapping |
| `Exception` (fallback) | 500, **generic** detail + `traceId`; full stack to logs only |

The fallback is where root-cause leakage stops: `createErrorEntity`'s `message + ", " + rootCause.getMessage()` concatenation is not carried forward. `traceId` comes from Micrometer `Tracer` when on the classpath, otherwise a generated UUID, and is logged with the stack trace so support can join a user-reported id to the server log.

A `ProblemDetailFactory` builds the body; `ErrorHandlingProperties` exposes `includeDebugDetail` (default `false`, enabled in the local `docker-compose-lcl` profile) so developers keep root-cause text locally without shipping it to production.

### Service-to-service translation

`store-commons/autoconfigure/.../s2s/utils/WebClientsUtils.java` currently builds proxies bare — no `defaultStatusHandler`, no `ResponseErrorHandler` (verified; and no code anywhere in the repo catches `RestClientResponseException`). Add a `ProblemDetailErrorHandler` applied in `WebClientsUtils.build(...)` for both the `RestClient` and `WebClient` paths, so all 9 `@HttpExchange` clients (`ExternalProductService`, `ExternalOrderService`, `IPaymentGatewayService`, `ExternalPodClient`, …) inherit it without touching the interfaces.

It decodes a remote ProblemDetail body into `RemoteServiceException`, preserving the **remote** `code`, status and params. The advice re-emits with the original code intact and status mapped: remote 4xx passes through (a remote 404 stays a 404), remote 5xx becomes 502. This is what turns today's "remote 400 → local 500 with remote stack text" into an actionable error.

---

## Migration sequence — one module per step, smallest first

Ordered by throw-site count ascending, so the pattern is proven on modules where a mistake is cheap to unwind before touching the 191-site one. Actual counts from the current tree:

| Step | Module | Throw sites |
|---|---|---|
| 1 | `../../store-pod/commons/store-commons` | 4 |
| 2 | `payment` (core + service) | ~7 |
| 3 | `store-core` (uaa, cua, control-plane) | ~10 |
| 4 | `../../store-pod/commons` leaves (customer, reference, store-modules) | ~21 |
| 5 | `merchant` + `content` | 29 |
| 6 | `checkout` | 38 |
| 7 | `catalog` | 216 |

Each step compiles and ships green on its own, and deletes the legacy types it replaces within its own module. The shared legacy classes die in Step 8.

**Step 0 — Foundation.** New `store-commons:errors` module (SPI, the abstract hierarchy rooted at `BaseException`, `CommonErrors`, `Unchecked`, `FieldError`); `GlobalErrorHandler` + `ProblemDetailFactory` + `ErrorHandlingAutoConfiguration` in `store-commons:autoconfigure`; `ProblemDetailErrorHandler` wired into `WebClientsUtils`. Temporary `@ExceptionHandler`s for `GenericRuntimeException` and `ServiceException` translate legacy exceptions into the new format so **every later step is behaviour-preserving from the client's perspective**. No throw site changes. Fixes bugs 2, 3 and 4 repo-wide on day one.

**Step 1 — `../../store-pod/commons/store-commons` (4 sites).** The pilot: smallest possible surface, and it is the module every pod depends on, so it validates that the new module reaches everywhere via the `api` wiring. Two distinct pieces of work, and conflating them is the trap this step exists to expose:

- **The bridge, for the ~275 sites this step does *not* touch.** The deprecated hierarchy becomes `ErrorCodeAware` (`GenericRuntimeException`) and a `BaseException` subclass (`ServiceException`), each reporting a `LegacyErrors.*` code. That is what makes every un-migrated throw render with the right status before it is rewritten. The `LEGACY.` prefix is deliberate — grepping it measures how much of the codebase is still on the old path, and a client seeing one knows the endpoint is not migrated. Delete the 4 identical `RestErrorHandler` copies and the 4 identical `FileUploadExceptionAdvice` copies; keep `ErrorEntity` until Step 8.
- **The migration, for this module's own 4 sites.** `PriceUtils` throws `PriceNotParseableException extends ConversionException` and `NonPositivePriceException extends ValidationException` (the latter carrying a `price` field error), and `getAmount` declares both. Not `ServiceException` with a better code.

**The rule this step establishes, learned the hard way in payment:** *attaching an `ErrorCode` to a legacy exception is not a migration.* It improves the response body while leaving the signature — the thing a caller actually reads — saying nothing. The first cut of this step gave `ServiceException` a `(ErrorCode, String)` constructor and called it "preferred while migrating"; that constructor is **removed**, because a shortcut that produces a plausible-looking diff is worse than no shortcut. The bridge keeps only its legacy constructors, all mapping to `LegacyErrors`.

Cost of the rule at this step: narrowing a shared utility's signature breaks its callers. `PriceUtils.getAmount` has two, both in `ProductCommonFacadeImpl`, both of which caught `ServiceException` and rethrew a generic `ServiceRuntimeException` — losing the distinction the parser had just made. They become a multi-catch of the two named types carried out through `UncheckedBaseException`, so the precise code survives until Step 7b lets the enclosing method declare it. Expect this: a two-line collateral change in a downstream module is the normal price of removing a generic type from a shared signature, and it is cheapest now.

**Gate: do not start Step 2 until the end-to-end checks below pass against a running pod** — this is the step that proves the wire format, the advice registration and the status mapping before anything else moves.

**Step 2 — `payment` (~7 sites).** Replace `FailedPaymentInitiate`, `InvalidWebhookPayload`, and `InvalidPaymentReferenceId` (declared, never thrown — delete) with `PaymentErrors` plus four named exceptions in `payment-commons`: `InvalidWebhookSignatureException`, `UnreadableWebhookPayloadException`, `UnexpectedWebhookObjectException` (all `extends ValidationException`) and `PaymentInitiateRejectedException` (`extends RemoteServiceException`, keeping Stripe's code and status). `PaymentProcessor` declares them per operation. These currently have **no `@ExceptionHandler` at all**, so this is a small module that is also a genuine behaviour fix for `StripeProcessor` — a good second step because it shows the value early at low cost, and it is where the naming convention gets set for the remaining steps.

**Step 2b — typed service-to-service errors.** Planned and implemented separately in `claude-plans-help-me-set-plan-curried-f-cached-grove.md`: an `-external-api` module becomes its service's client SDK, publishing a named exception family and a `RemoteErrorCatalog` that turns a wire `code` back into it, with `WebClientsUtils` delivering the type through the `@HttpExchange` proxy. Payment is the pilot; each later step adds its own catalog and client wrapper. The `@HttpExchange` interface is implemented by the server's own controller, so its `throws` clause states the *server's* exceptions; a hand-written wrapper beside it restates them in the caller's vocabulary. This is also where the remote `code` finally survives re-emission, making this plan's long-standing claim about `RemoteServiceException` true.

**Step 3 — `store-core` (~10 sites): uaa, cua, control-plane.** Delete the byte-identical duplicate pairs (`ForbiddenOperationException` / `ResourceNotExistException` exist twice — `store-core/uaa/.../exception/` and `store-pod/cua/.../exception/`) in favour of the shared types. Retire both `GeneralExceptionHandler` copies; their `ResourceNotExistException` → **400** titled "User Not Found" mismatch is corrected to 404 here. Convert control-plane's 3 `ResponseStatusException` throws in `ManagedUserAccountServiceImpl` and delete its empty `ExceptionHandlerAdvice`. Doing this early collapses the three wire formats into one before the bulk migration begins.

**Step 4 — `../../store-pod/commons` leaves (~21 sites).** `customer-core` (`CustomerPopulator` country/zone codes), `reference-*`, and `store-modules/store-cms-commons` — the S3 layer. `S3StaticContentAssetsManagerImpl` (4 sites) is the model conversion: `ServiceException(e)` → named types under the abstract `StoreIOException` (`AssetUploadFailedException`, `AssetNotFoundException`, …), each with the S3 key in `params`. **This is the `Unchecked` ergonomics checkpoint** (see the flag in Decisions): the populators are the first lambda-heavy code to convert, and it is the last cheap moment to revisit checked-everywhere.

**Step 5 — `merchant` + `content` (29 sites).** `ContentFacadeImpl`'s 4 `ConstraintException` sites; `MerchantStoreApi`'s `RestApiException(ioe)` → a named type under `StoreIOException`.

**Step 6 — `checkout` (38 sites)**, `checkout-core` + `checkout-service`. Includes `EXCEPTION_INVENTORY_MISMATCH` (the one meaningful `exceptionType`) → `CheckoutErrors.INVENTORY_MISMATCH`, which the storefront needs to distinguish from a generic failure. Checkout calls catalog over `@HttpExchange`, so this is where the s2s translation gets its first real exercise — while catalog is still on the legacy path, proving the Step 0 adapters preserve codes across the boundary.

**Step 7 — `catalog` (216 sites).** Last and largest, split into three sub-steps. Ordered by dependency direction rather than size, since `catalog-service` depends on `catalog-core`:
- **7a — `catalog-core` mappers & pricing** (~120): `PersistableInventoryMapper`, `PersistableProductVariantMapper`, `ReadableProductVariantMapper`, `PersistableProductAttributeMapper`, `ReadableMinimalProductMapper`, `services/pricing/ProductPriceUtils`. Introduce `CatalogErrors`.
- **7b — `catalog-core` facades** (~71): `CategoryFacadeImpl`, `ProductCommonFacadeImpl`, `ProductVariantFacadeImpl`, `ProductVariationFacadeImpl`, `ProductOptionSetFacadeImpl`, `ProductTypeFacadeImpl`. Duplicate-code checks become named types under `DuplicateResourceException` (`DuplicateSkuException`, `DuplicateCategoryCodeException`, …). This sub-step produces the bulk of the new exception classes; batch them by resource so review stays tractable.
- **7c — `catalog-service` API** (25): cross-store guards in `ProductApi` (4 sites) and `ProductImageApi` become `ForeignStoreProductAccessException extends AccessDeniedStoreException` — they are currently `UnauthorizedException` → 401, semantically wrong since the caller *is* authenticated.

**Step 8 — Cleanup.** Delete `GenericRuntimeException`, `ServiceException`, `ServiceRuntimeException`, `ErrorEntity`, `uaa-client-impl`'s `ApiException`, and the legacy adapter handlers from Step 0. A repo-wide grep for the deleted types is the completion gate.

---

## Files that matter

**Created:** `store-commons/errors/**` (new module + `../../settings.gradle` entry); in `../../store-commons/autoconfigure/src/main/java/com/asrevo/cvhome`: `error/GlobalErrorHandler.java`, `error/ProblemDetailFactory.java`, `error/ErrorHandlingAutoConfiguration.java`, `s2s/error/ProblemDetailErrorHandler.java`. Then, per step, an `errors/` package in the touched `-commons` module holding that context's `ErrorCode` enum and its condition-named exceptions — `store-pod/commons/store-commons/.../store/errors/` (Step 1: `StoreErrors`, `LegacyErrors`, `PriceNotParseableException`, `NonPositivePriceException`), `payment-commons/.../payment/errors/` (Step 2), and so on.

**Modified:** `../../store-commons/autoconfigure/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`; `store-commons/autoconfigure/.../s2s/utils/WebClientsUtils.java` (both `build` overloads); `../../store-commons/commons/build.gradle` + `../../store-pod/commons/store-commons/build.gradle` (expose the new module as `api`).

**Deleted:** 4× `RestErrorHandler`, 4× `FileUploadExceptionAdvice` (under `store-pod/*/​*-service/.../controller/exception/`), 2× `GeneralExceptionHandler` (uaa, cua), `control-plane-service/.../ExceptionHandlerAdvice.java`, and the legacy exception classes in Step 9.

**Pattern-repeated across ~300 sites** — the shape is identical everywhere, so it is not enumerated per file:
```java
// before
throw new ResourceNotFoundException("Product with id [%s] not found for store [%s]".formatted(id, storeId));
// after — a named type per condition; the params live in its factory, not at the call site
throw ProductNotFoundException.of(id, storeId);
```
with, once per condition, in `catalog-commons`:
```java
public class ProductNotFoundException extends ResourceNotFoundException {
    protected ProductNotFoundException(ErrorPayload payload, Throwable cause) { super(payload, cause); }

    public static ProductNotFoundException of(Long productId, StoreMerchantId storeId) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_NOT_FOUND, ProductNotFoundException::new)
                .param("productId", productId).param("storeId", storeId).build();
    }
}
```
Representative files: `catalog-core/.../service/mapper/inventory/PersistableInventoryMapper.java`, `catalog-core/.../service/facade/category/CategoryFacadeImpl.java`, `store-modules/store-cms-commons/.../cms/s3/S3StaticContentAssetsManagerImpl.java`, `checkout-core/.../services/pricing/ProductPriceUtils.java`.

---

## Verification

**Per step:** `./gradlew :<module>:build` for the touched module, then `./gradlew build` before the step is considered done. After each of Steps 1–7, two greps must return zero for that module:

- `grep -rn "throw new \(ServiceException\|GenericRuntimeException\|ServiceRuntimeException\)" <module>` — no legacy type left.
- `grep -rn "throws \(BaseException\|ValidationException\|ResourceNotFoundException\|ConversionException\|RemoteServiceException\|DuplicateResourceException\|OperationNotAllowedException\|AccessDeniedStoreException\|StoreIOException\)\b" <module>` — no category base declared in a signature. Throwing one is already a compile error since the bases are abstract; *declaring* one still compiles, so this grep is what catches it. A hit means the step swapped one generic type for another — the failure mode Steps 1 and 2 both hit on the first pass.

**Step 1 gate (the point of going smallest-first).** Before any other module moves, stand up one pod locally and confirm on real traffic that the foundation works: a legacy-thrown error now returns the extended ProblemDetail with the right status (not a 500), `code`/`category`/`traceId` are populated, the `traceId` in the response appears in the service log, and no root-cause text leaks into `detail`. If the contract needs adjusting, it is adjusted here — with 4 converted sites behind it, not 300.

**Contract tests (Step 0, run every step after):** `@WebMvcTest` slice per category asserting the JSON body — `code`, `category`, `status`, `params` present; `detail` free of stack-trace text when `includeDebugDetail=false`; `traceId` present. This is the regression net that keeps the wire format stable while 300 throw sites move.

**Advice-scope regression test:** a test controller in `com.asrevo.cvhome.catalog.api.v1` that throws — asserting a 404 body, not a 500. This is the specific bug (#2) that made the current handler dead code; without a test it will silently return.

**s2s test:** MockWebServer returning a ProblemDetail 404 to an `@HttpExchange` client; assert the caller re-emits 404 with the **remote** code, not a 500.

**End-to-end, local:** bring up `docker-compose-lcl` and exercise through the real gateway paths. Each check is tied to the step that makes it pass, so every step has a concrete acceptance signal:

| After step | Check | Today |
|---|---|---|
| 1 | `GET` a non-existent product → 404, not 500; `traceId` in body matches the log line | 500, NPE in the advice |
| 1 | `POST` with a missing required field → 400 with populated `fieldErrors[]` | impossible — no handler exists |
| 1 | Oversized image upload → 413 from the shared advice, after the 4 duplicate advices are deleted | 413 from a per-service copy |
| 1 | Product price `"abc"` → 400 `STORE.PRICE.NOT_PARSEABLE`; price `"-5"` → 400 `STORE.PRICE.NOT_POSITIVE` with a `price` field error | both "Invalid product price format" |
| 1 | Any un-migrated endpoint → a `LEGACY.*` code with the right status, never a bare 500 | 500, NPE in the advice |
| 2 | Stripe webhook with an invalid payload → 400 `PAYMENT.WEBHOOK.INVALID`, not 500 | 500, no handler at all |
| 3 | Unknown uaa user → 404, not a 400 titled "User Not Found"; control-plane returns the same shape as the pods | 3 different formats |
| 4 | Upload with S3 unreachable → 500 `STORAGE` with `traceId`; unsupported country code → 400 `CONVERSION` | generic 500 |
| 6 | Checkout whose catalog reservation is rejected → remote code preserved | 500 carrying catalog's stack text |
| 7 | Duplicate SKU → 409 `DUPLICATE`; another store's product → 403 with no internal detail | generic failure; 401 |

Since the frontend is out of scope, verify 1–6 with `curl`/HTTP client against the gateway and assert on the raw JSON.

## Out of scope (follow-up)

seller-ui `HttpErrorInterceptor` + typed `ApiError` model, `code` → i18n key resolution against the new namespaced vocabulary (the `ERROR` namespace currently holds 4 keys, and 167 call sites pass `ERROR.SYSTEM_ERROR`), backend `fieldErrors` bound to reactive-form controls, and landing-ui's `handleResponse` in `libs/services/src/http-utils.ts`, which discards non-2xx bodies entirely and returns `undefined`.

# Error handling — the plan of record

**Supersedes and merges three plans, which are kept only as history:**
`help-me-set-plan-curried-fountain.md` (the original refactor), `claude-plans-help-me-set-plan-curried-f-cached-grove.md`
(typed service-to-service errors, Step 2b), `store-commons-autoconfigure-src-main-ja-tidy-fox.md` (explicit error
catalogs). Where they disagree with each other, this file is right: each corrected the one before it, and two of the
three describe designs that have since been replaced. Nothing below is aspirational unless it sits under
**Remaining work**.

The distilled version of the *design* also lives in the `project-structure` skill
(`.claude/skills/project-structure/references/error-handling.md` and `service-to-service.md`), which is what gets read
during ordinary work. This file is the *migration*: what is done, what is left, in what order, and how each step is
verified.

---

## Goal

Every failure carries a stable machine-readable code, a category that fixes the HTTP status, structured params, and
field-level errors where they apply — emitted in one format across all 8 services, with internal detail confined to
logs and joined to the response by `traceId`.

The four defects that started this, all now fixed repo-wide by Step 0:

1. One exception for every failure mode (`ServiceException`, `GenericRuntimeException`), so nothing distinguished
   "not found" from "S3 upload failed".
2. The advice was mis-scoped — all four `RestErrorHandler` copies declared
   `@ControllerAdvice({"com.asrevo.cvhome.store.controller"})` while every API lives in
   `com.asrevo.cvhome.<service>.api.v1.*`. Error handling was dead code in four services.
3. When it did apply it NPE'd on `Objects.requireNonNull(exception.getCause())`, so a message-only throw became a
   bare 500.
4. Three incompatible wire formats (`ErrorEntity`, `ProblemDetail`, Spring's default).

---

## Status

### Done — every step. The migration is complete.

| | |
|---|---|
| **Step 0 — foundation** | `store-commons:errors` (SPI, abstract hierarchy, `Unchecked`, `FieldError`), `GlobalErrorHandler` + `ProblemDetailFactory` + `ErrorHandlingAutoConfiguration` in `store-commons:autoconfigure`, s2s error handling in `WebClientsUtils`. One `@ControllerAdvice` with **no** basePackages — defects 2, 3, 4 fixed everywhere on day one |
| **Step 1 — `store-pod/commons/store-commons`** | The legacy bridge (`ServiceException`/`GenericRuntimeException`/`ServiceRuntimeException` → `LegacyErrors`) so un-migrated throws still render correctly under a `LEGACY.*` code; the module's own 4 sites migrated (`PriceNotParseableException`, `NonPositivePriceException`) |
| **Step 2 — `payment`** | `PaymentErrors` + four named exceptions in `payment-commons`; `PaymentProcessor` declares them per operation. Payment is the reference implementation to copy |
| **Step 2b — typed s2s errors** | `-external-api` as client SDK: caller-side exception family, `RemoteErrorCatalog`, transport-failure translation, typed unwrapping through the proxy, remote code surviving re-emission. `OrderPlacementFacadeImpl` branches on the types |
| **Refactor A — explicit catalogs** | `ServiceLoader` discovery, `RemoteErrorRegistry`, `apis()` and both `META-INF/services` files deleted. The catalog is passed to `buildClient(...)`; `S2sErrorHandler` is the one named class owning both failure paths |
| **Refactor B — two interfaces, no wrapper** | The hand-written `PaymentGatewayClient`/`RestPaymentGatewayClient` pair is gone. The caller-side types are declared on the `@HttpExchange` interface itself, and `declaredOrCarrier` delivers them narrowed |
| **Step 3 — `store-core`** | uaa, cua and control-plane migrated; the last two per-service advices and the empty stub deleted; `DataIntegrityErrorHandler` added so the 409 they used to provide is now every service's. Details below |
| **Step 4 — `store-pod/commons` leaves** | `store-cms-commons` (all 12 sites) and `customer-core` migrated; `CmsErrors` + 7 exceptions, `CustomerErrors` + 2; the populator SPI widened so a migrated populator can name its own conditions. `reference-*` had nothing to migrate. Details below |
| **Step 5 — `merchant` + `content`** | `ContentErrors` + 5 exceptions, `MerchantErrors` + 4; every facade, service and controller signature in both modules migrated. The 7 sites left blocked on the generic root dissolved in Step 8. Details below |
| **Step 6 — `checkout` + the catalog reservation contract** | `CheckoutErrors` + 10 exceptions; every facade, service, populator and controller in `checkout-core`/`checkout-service` migrated. `catalog-external-api` split into the `IProductReservationService`/`ExternalProductReservationService` pair with `CatalogApiErrors.CATALOG`, so out-of-stock and catalog-is-down stopped being the same outcome. Three `ConversionRuntimeException` sites in the order mappers were **missed** — the step's grep pattern omitted that type — and were finished in Step 7 once the `Mapper` SPI could carry a checked failure. Details below |
| **Step 7 — `catalog`** | The largest step: **205 legacy throw sites across 45 files, down to zero** (the last 2 went with the root in Step 8). `CatalogErrors` grown to 45 codes, 58 exception classes in `catalog-commons`. The `Mapper` SPI widened exactly as `DataPopulator` was in Step 4, which is what let 22 unchecked `ConversionRuntimeException` sites become named types. Details below |
| **Step 8 — cleanup** | The `SalesManagerEntityService` root retired and the whole legacy bridge deleted. Details below |

`./gradlew build`, `./gradlew checkstyleMain checkstyleTest` and `./gradlew test` are all green.

### Step 8 as built — retiring the root, then deleting the bridge

Two moves, in this order, because the second is impossible before the first.

**The root.** `SalesManagerEntityService.update/create/delete` declared `throws ServiceException`; every entity service
in the repo overrides them and Java forbids an override from widening a `throws` clause, so no typed failure could
travel through a persistence call. Dropping it from the interface *and* `SalesManagerEntityServiceImpl` cleared
~90 files of `throws ServiceException` residue in one pass and dissolved the 9 remaining rewrap sites — the catch
blocks that turned a store failure back into an untyped exception simply had nothing left to catch. What replaced
them:

- **A persistence failure is now an unchecked `DataAccessException`**, rendered by the shared advice as a 500 with a
  `traceId`. That is what it always was; the wrapper only hid it. `CustomerFacadeImpl` no longer swallows it into an
  empty `Optional`, and `StoreFacadeImpl.delete` no longer flattens a constraint violation and an outage into one
  message — `DataIntegrityErrorHandler` gives the first a 409.
- **`ProductServiceImpl.saveProduct`** still owes its caller a named condition, so it catches `DataAccessException`
  and re-emits `ProductNotPersistedException`.
- **`ProductServiceImpl.delete`** is the one place a typed failure still has to cross a boundary that cannot declare
  it: it overrides the root `delete(E)`, and deleting a product deletes its images, which can raise
  `AssetDeleteFailedException`. It travels in `UncheckedBaseException` — the sanctioned carrier — so the advice
  unwraps it and the asset's own code and status still reach the client. Nothing is flattened.
- **`getCustomerById`** became typed (`CustomerNotFoundException`), which forced the two order-reading paths to say
  what they mean: an order outlives the customer row it names, so `OrderFacadeImpl.attachCustomer` catches that one
  condition and still returns the order — the same outcome the old `null` check produced, now narrow and deliberate.

**The bridge.** Deleted outright: `ServiceException`, `ServiceRuntimeException`, `GenericRuntimeException`,
`ConstraintException`, `ConversionRuntimeException`, `RestApiException`, `UnauthorizedException`, the legacy
`OperationNotAllowedException`, `ResourceNotFoundException`, `ErrorEntity`, the deprecated
`store.core.exception.ConversionException`, and `LegacyErrors` — the whole `store.controller.exception` and
`store.core.exception` packages. The three surviving users of the legacy `ResourceNotFoundException` were migrated
first: `CustomerNotFoundException.byId`, a new `PaymentConfigurationNotFoundException`
(`PAYMENT.CONFIGURATION.NOT_FOUND`, distinct from the 422 `CONFIGURATION_MISSING`), and a new `PodNotFoundException`
(`CONTROL_PLANE.POD.NOT_FOUND`). `uaa-client-impl`'s `ApiException` was already gone.

No `@ExceptionHandler` needed removing: the bridge was never in the advice: it was the payload each legacy class built
over `LegacyErrors`, so deleting the classes deleted it.

**Gates, all zero repo-wide:** the legacy-throw grep, the generic-`throws` grep (only the documented `Mapper` /
`DataPopulator` SPI declarations and `Unchecked`'s own remain), `LEGACY.` as a live code, and dangling `{@link}`
references to a deleted type. Historical javadoc that *names* a deleted type in `{@code}` — "Replaces a
{@code ServiceException} which reported {@code LEGACY.SERVICE_ERROR}" — is kept deliberately: it is the record of what
each condition-named class was built to fix, and it links to nothing.

### Open defects — both fixed

- **`ExternalPaymentGatewayApi.status` was unreachable.** The client resolved
  `/api/v1/private/payments/{ref}/status`; the controller mapped `/api/v1/payments/{requestRef}/status` — no
  `/private`. Nothing calls `status` yet, which is why it never fired. The `@GetMapping` now carries the segment, with
  a comment saying why the two halves have to be checked by hand. (The older plan noted only the `ref`/`requestRef`
  param-name mismatch, which is harmless; the missing path segment was not.)
- **Stale javadoc in three files**, all describing the deleted wrapper design, all rewritten:
  `ExternalPaymentGatewayService`, `PaymentApiErrors`, `ClientsConfig.externalPaymentGatewayService`.

---

## The design as it now stands

### Where the code lives

| | |
|---|---|
| `store-commons/errors` | The whole type system. **Plain Java, zero dependencies** — no Spring, no logging, no Jackson at runtime. `api` from both commons roots, so every module sees it |
| `store-commons/autoconfigure` | `errors/web/` — the advice, `ProblemDetailFactory`, autoconfiguration. `s2s/error/` — `S2sErrorHandler`, `RemoteProblemTranslator` |
| `<domain>-commons/.../errors/` | Per-context `ErrorCode` enum + the condition-named exceptions that service throws |
| `<domain>-external-api/.../api/errors/` | The client SDK's caller-side types + its `RemoteErrorCatalog` constant |
| `<domain>-external-api/.../services/` | The HTTP interfaces — one, or the split pair (below) |

Keeping `store-commons:errors` dependency-free is load-bearing: it is why an `-external-api` module can declare an
error contract without depending on `autoconfigure`, and why `System.Logger` was used instead of adding SLF4J when
the (now-deleted) registry needed to warn.

### The wire format

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

Plus, depending on **who** failed: `remoteService`/`remoteStatus` for a peer cvhome service, or
`provider`/`providerCode`/`providerStatus` for a third party. Never both — they mark different boundaries.

`detail` never carries root-cause text in production; `ErrorHandlingProperties.includeDebugDetail` (default `false`,
on in `lcl`) restores it locally. `traceId` is the Micrometer trace id when present, else a short UUID, logged with
the stack trace so a user-reported id leads to the server log line.

### The hierarchy

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

### The rules

Also in `BaseException`'s javadoc, so they are read where they are needed.

1. **Never throw a generic type** — not `BaseException`, not a category base. Throw a class whose *name* states the
   condition. The bases are abstract, so this is a compile error, not a review comment.
2. **Never declare a generic type.** `throws BaseException` tells a caller only "something may fail", which it
   already knew.
3. **The condition names the class; the category names the parent.** `NonPositivePriceException extends
   ValidationException` is a 400 because validation failures are — nothing at the throw site restates the status.
4. **Attaching a better `ErrorCode` to a generic exception is not a migration.** It improves the response body while
   leaving the signature — the part a caller reads — saying nothing.
5. **One class per condition, with a static factory that names its inputs.** `PriceNotParseableException.of(amount,
   cause)` beats a builder chain repeated at every site: the params a support engineer will search on are guaranteed
   rather than remembered.
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

The only relaxation is the legacy bridge, which is generic by definition and dies in Step 8.

### Who failed — the three-way distinction

The axis the type system exists to keep straight, and the one that cost the most to get wrong.

| Failure | Base | Code on the wire | Status |
|---|---|---|---|
| This service | a local category base | ours | from our category |
| Another cvhome service | `RemoteServiceException` | **the remote's**, re-emitted | remote 4xx passes through; remote 5xx → 502 |
| A third party (Stripe, PayPal) | `ExternalProviderException` | **ours**; theirs rides along as `providerCode` | from our category, never theirs |

Re-emission is sound for a peer because it speaks our problem-detail contract; a third party shares neither.

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

### Service-to-service: two interfaces, and the catalog is passed explicitly

An API that names no failures keeps **one** `@HttpExchange` interface, implemented by the controller and proxied by
the caller — that is still most of them. As soon as it names failures, the two sides need different `throws`
clauses, because the server's exceptions describe its conversation with Stripe and the caller's describe
"payment-service refused us". One signature cannot be honest about both, so the module declares the operations
twice:

```java
// IPaymentGatewayService — server vocabulary. Plain interface, no @HttpExchange.
// ExternalPaymentGatewayApi (the controller) implements it; the routes are @PostMapping on the controller.
public interface IPaymentGatewayService {
    PaymentInitiateResult initiatePayment(StoreMerchantId store, @RequestBody PaymentRequest req)
            throws PaymentInitiateRejectedException, PaymentProviderUnavailableException;
}

// ExternalPaymentGatewayService — caller vocabulary. Carries @HttpExchange, is what buildClient proxies,
// and is implemented by nothing.
@HttpExchange("/api/v1/private")
public interface ExternalPaymentGatewayService {
    @PostExchange("/payments/initiate")
    PaymentInitiateResult initiatePayment(StoreMerchantId store, @RequestBody PaymentRequest req)
            throws PaymentGatewayRejectedException, PaymentApiUnavailableException;
}
```

**Why the caller-side types go on the client interface and nowhere else:** `S2sErrorHandler.declaredOrCarrier` reads
the invoked proxy method's declared exception types and rethrows a matching cause as itself. Declaring them there is
what makes `PaymentGatewayRejectedException` arrive catchable in `OrderPlacementFacadeImpl` as ordinary Java. This
replaces the `PaymentGatewayClient`/`RestPaymentGatewayClient` wrapper pair the earlier plan specified — see
*Corrections*.

The halves need not line up one-for-one: `PaymentApiUnavailableException` has **no** server counterpart, because a
service that could not be reached never threw anything.

The contract is a constant, passed at construction:

```java
// payment-external-api
public static final RemoteErrorCatalog CATALOG = RemoteErrorCatalog.builder()
        .map(PaymentErrors.INITIATE_REJECTED, PaymentGatewayRejectedException::from)
        .map(PaymentErrors.INITIATE_FAILED,   PaymentApiUnavailableException::from)
        .unreachable(PaymentApiUnavailableException::from)
        .build();

// checkout ClientsConfig — the client interface, never IPaymentGatewayService
restClientBuilder.buildClient(PAYMENT_SERVICE_NAME, ExternalPaymentGatewayService.class, PaymentApiErrors.CATALOG);

// an API that names no failures says so
restClientBuilder.buildClient(CATALOG_SERVICE_NAME, ExternalProductService.class, RemoteErrorCatalog.none());
```

Mappings are keyed by `ErrorCode`, not by string, so renaming a code cannot silently orphan an entry. Unmapped codes
fall back to `UnmappedRemoteFailureException`, which still carries the remote's code and status — seeing one in a log
is the signal that a code deserves an entry.

`S2sErrorHandler` owns both failure paths: an error response (`defaultStatusHandler`) and no response at all
(`requestInterceptor`, the only place a refused connection or read timeout can be caught). The translated exception
is checked, but Spring's hooks may only throw `IOException` and unchecked exceptions, so it travels inside
`UncheckedBaseException` and `declaredOrCarrier` opens it. The reactive `WebClient` path gets translation but **not**
unwrapping — the failure travels inside a `Mono`, where no proxy can rethrow it as a declared checked type; a
reactive caller uses `onErrorMap`.

---

## Step 3 as built — `store-core`: uaa, cua, control-plane

Kept here rather than folded away, because the later steps repeat its shape.

**New vocabularies.** `UaaErrors` (`store-core/uaa/.../uaa/errors/`), `CuaErrors` (`store-pod/cua/.../cua/errors/`),
`ControlPlaneErrors` (`control-plane-service/.../controlplane/errors/`) — the last one in the service module because
`control-plane-service` does not depend on `manager-commons`; its manager code lives in the service itself.

**Eight condition-named exceptions.** `UserNotFoundException`, `ClientNotFoundException`,
`SuperAdminImmutableException` · `DuplicateUsernameException`, `DuplicateEmailException` ·
`ManagedUserNotFoundException`, `ForeignOrgUserAccessException`, `ForeignStoreUserAccessException`.

**Deleted.** Both `GeneralExceptionHandler` copies, control-plane's empty `ExceptionHandlerAdvice`, and both
duplicated exception pairs (`ForbiddenOperationException`, `ResourceNotExistException` × uaa and cua — cua's copies
had no throw site at all and existed only for its handler to reference).

**Added to the shared advice: `DataIntegrityErrorHandler`** + `CommonErrors.DATA_INTEGRITY_VIOLATION` (409). Deleting
the two `GeneralExceptionHandler`s would otherwise have dropped their `DataIntegrityViolationException` → 409 "in use
by another entity" to a 500. Registered `@ConditionalOnClass` like the security and constraint advices, so a service
without `spring-tx` still loads the rest; `spring-tx` added to the version catalog and as `compileOnly` on
`autoconfigure`. **Every service gains this** — a foreign-key violation is no longer a 500 anywhere.

**Three behaviour changes, all deliberate:**

| | Before | After |
|---|---|---|
| Unknown uaa user | **400** titled "User Not Found" | 404 `UAA.USER.NOT_FOUND` |
| `resetSecret` for a missing client | HTTP **200**, nothing rotated — the `if (client != null)` had no `else` | 404 `UAA.CLIENT.NOT_FOUND` |
| cua registration with a taken **email** | form error shown under the **username** control | shown under `email` |

The last two were found by the migration, not looked for: one type per condition is what made the missing branch and
the mis-bound field visible.

**The forcing function worked as designed.** Making `ManagedUserAccountService`'s tenancy guard throw three named
types broke `UserAccountController` at seven call sites — every endpoint that can 404 or 403 now says so in its
signature. `ManagedUserNotFoundException` moved into `findOne(String)` because that is the only frame that still has
the id to report; the guard now handles only the two access conditions.

`orElseThrow` needed no `Unchecked` wrapper: it is generic over the thrown type, so a checked exception passes
straight through. The real `Unchecked` checkpoint is still Step 4's populators.

---

## Step 4 as built — `store-pod/commons` leaves

**New vocabularies.** `CmsErrors` (`store-cms-commons/.../modules/cms/errors/`) with seven codes, and `CustomerErrors`
(`customer-commons/.../customer/errors/`) with two. `reference-*` turned out to have no legacy throw site at all, so
the step's real content was the two modules.

**Nine condition-named exceptions.** `AssetNotFoundException` (404) · `AssetReadFailedException`,
`AssetUploadFailedException`, `AssetDeleteFailedException`, `AssetListFailedException`,
`ImageSizeMisconfiguredException` (all `StoreIOException`, 500) · `ImageUnreadableException` (`ValidationException`,
400) · `UnsupportedCountryCodeException`, `UnsupportedZoneCodeException` (both `ConversionException`, 400).

**The CMS interfaces carry the vocabulary, not the implementations.** `FileGet`/`FilePut`/`FileRemove`,
`ImageGet`/`ImageRemove`, `ProductImageGet`/`ProductImagePut`/`ProductImageRemove` all lost `throws ServiceException`
for per-operation types. That is what makes the split visible to a caller: `getFile` says it can 404 *or* fail to
read, and nothing else in the repo can now conflate the two.

**The `Unchecked` checkpoint answered: it was not needed once.** The friction was never `stream().map(...)` — it was
`Optional.or(Supplier)` in `CustomerFacadeImpl.getOrCreateCustomer`, whose supplier cannot throw checked. Replacing it
with a plain `isPresent()` short-circuit kept the same laziness and left the two conversion failures on the signature,
where a caller reads them. **Prefer restructuring the lambda away over reaching for `Unchecked`**; on this evidence
checked-everywhere costs far less in populator code than the decision anticipated, so Step 7's 76 sites need no
rethink.

**The populator SPI had to widen, and that is the one deliberate category-base declaration.**
`DataPopulator`/`AbstractDataPopulator` now declare `com.asrevo.cvhome.errors.ConversionException` instead of the
deprecated `store.core.exception.ConversionException`, and the deprecated class was changed to extend that base rather
than `BaseException` directly. Together those two one-line changes let a migrated populator throw a condition-named
type through the same signature that the 27 un-migrated ones still satisfy — otherwise every populator in the repo
would have had to move in one commit. A generic SPI over three type parameters has no condition to name; every
implementation narrows, so the base never reaches a call site.

**Cost of that widening: four narrowing overrides.** The inherited two-argument `populate` picks up the wide base, so
`ReadableProductPricePopulator`, `ReadableContentPagePopulator`, `ReadableContentBoxPopulator` and
`PersistableMerchantStorePopulator` each re-declare it narrowly and re-dispatch to their own four-argument override.
Five lines each; they disappear with the deprecated type in Step 8.

**Five behaviour changes, all deliberate:**

| | Before | After |
|---|---|---|
| `getFile` for a deleted S3 object | 500, identical to an unreachable bucket | 404 `CMS.ASSET.NOT_FOUND` |
| Upload of a non-image file | 500 — a caller's bad file reported as our outage | 400 `CMS.IMAGE.UNREADABLE` |
| Non-numeric `PRODUCT_IMAGE_HEIGHT_SIZE` | raw `NumberFormatException` → unexplained 500 | 500 `CMS.IMAGE.SIZE_MISCONFIGURED`, naming the setting |
| Unsupported country code at checkout | swallowed into `Optional.empty()`, reported as "unable to create customer" | 400 `CUSTOMER.COUNTRY.UNSUPPORTED` with the code in `params` |
| NPE inside `ReadableCustomerPopulator` | 400 `LEGACY.CONVERSION` — our bug blamed on the caller's input | 500, as an unhandled fault should be |

The fourth was the forcing function working exactly as in Step 3: making `CustomerPopulator` throw two named types
broke `CustomerFacade`, then `OrderApi`, so the checkout endpoint now declares that it can reject a bad country or
zone code. The fifth came from deleting a blanket `catch (Exception) -> ConversionException` that had no real
conversion failure behind it.

**One place a typed failure is deliberately flattened again.** `ProductServiceImpl.delete` overrides
`SalesManagerEntityService.delete`, whose `throws` clause is `ServiceException`, and Java forbids an override from
widening — so `AssetDeleteFailedException` is caught and wrapped there, with a comment. It is the only such site, and
it goes when catalog is migrated in Step 7.

---

## Step 5 as built — `merchant` + `content`

**New vocabularies.** `ContentErrors` (`content-commons/.../content/errors/`) with five codes, `MerchantErrors`
(`merchant-commons/.../merchant/errors/`) with four. Both in the `-commons` module, so a consumer that has to catch
them can see them.

**Nine condition-named exceptions.** `ContentNotFoundException`, `ContentFileNotFoundException` (404) ·
`DuplicateContentCodeException` (409) · `ContentFileUnreadableException` (500) · `InvalidFolderPathException` (400) ·
`MerchantStoreNotFoundException` (404) · `DuplicateMerchantStoreException` (409) ·
`DefaultStoreNotRemovableException` (422) · `UploadedFileUnreadableException` (500).

**The catch that made the check pointless.** `saveContentPage` and `saveContentBox` each threw a
`ConstraintException` for a duplicate code *inside* a `try` whose `catch (Exception)` immediately re-wrapped it as a
generic runtime failure. The 409 never left the facade, so the duplicate check might as well not have been written —
and seller-ui could not have offered "that code is taken" even if it wanted to. Four methods had this shape; all four
now let the typed failure out and catch only the persistence exception they actually mean to handle.

**Seven behaviour changes, all deliberate:**

| | Before | After |
|---|---|---|
| Duplicate content page/box code | generic 400, check swallowed | 409 `CONTENT.CODE.DUPLICATE` |
| Update a content page that does not exist | **409** `ConstraintException` | 404 `CONTENT.NOT_FOUND` |
| Rename a file that does not exist | 500 | 404 `CONTENT.FILE.NOT_FOUND` |
| Invalid folder path | 500 | 400 `CONTENT.FOLDER.PATH_INVALID`, with a `path` field error |
| Store id already taken | 400 "MerhantStore ... already exists" | 409 `MERCHANT.STORE.DUPLICATE` |
| Deleting the default store | 400 | 422 `MERCHANT.STORE.DEFAULT_NOT_REMOVABLE` |
| Unreadable upload | 400 (`RestApiException` reports `LEGACY.BAD_REQUEST`) | 500 `MERCHANT.UPLOAD.UNREADABLE` / `CONTENT.FILE.UNREADABLE`, naming the file |

The last one is a status going *up*, not down: a broken transfer on our side had been reported to the caller as their
mistake.

**Two more blanket catches deleted, same finding as Step 4.** `PersistableMerchantStorePopulator.applyLanguages` and
`StoreFacadeImpl.convertMerchantStoreToReadableMerchantStore` both wrapped `catch (Exception)` around pure
null-check-and-setter code, so an NPE in our own mapping surfaced as a 400 blaming the caller's input. Both are gone;
`PersistableMerchantStorePopulator` now declares no failure at all. **Three modules in, this is the single most common
defect the migration finds** — a blanket catch over code that cannot fail in the way the wrapper claims.

**Three `@SneakyThrows` removed** from `ContentFacadeImpl`'s getters, which existed only to hide the populator's
declared `ConversionException` from the signature. The exception is now named on the method, so the annotation had
nothing left to hide.

**Two dependency scopes corrected.** `content-core` and `merchant-core` had `store-cms-commons` as `implementation`
while naming its exception types on their own public signatures — a consumer could call the method but not catch what
it declared. Both are `api` now.

---

## Step 6 as built — `checkout`, and the catalog reservation contract

**New vocabularies.** `CheckoutErrors` (`checkout-commons/.../checkout/errors/`) with ten codes, and — earlier than
Step 7 planned — `CatalogErrors` (`catalog-commons/.../catalog/errors/`) with the two the reservation API needs. The
catalog enum starts here because checkout cannot tell a refusal from an outage without codes on the wire to tell them
apart; Step 7 extends it rather than introducing it.

**Twelve condition-named exceptions.** Checkout: `ShoppingCartNotFoundException`, `OrderNotFoundException` (404) ·
`ProductNotPurchasableException`, `OrderCustomerUnresolvedException` (422) · `OrderLoginRequiredException` (401) ·
`ForeignStoreTokenException` (403) · `OrderProductPriceMissingException`, `OrderProductNotConvertibleException`,
`OrderNotConvertibleException`, `PriceNotFormattableException` (400). Catalog: `InsufficientInventoryException` (422) ·
`EmptyReservationException` (400). Plus `CustomerNotFoundException` (404) in `customer-commons`, for the one legacy
site `CustomerOrderApi` still held.

**One addition to the shared hierarchy: `AuthenticationRequiredException`.** `ErrorCategory` had `UNAUTHENTICATED`
(401) from the start but no base class to hang a condition on, so the first genuine 401 in the codebase had nowhere
to go. It sits beside `AccessDeniedStoreException`, and the pair is the distinction a storefront acts on: 401 means
"log in and try again", 403 means "logging in again will not help".

### The reservation contract — the collapse this step existed to undo

Reserving stock had **three** layers each turning a decision into a boolean, and the composition of them meant a
shopper was told an item was out of stock whenever catalog restarted:

1. `ProductReservationServiceImpl.reserve` caught its own `ServiceException` and returned `status(false)` — so
   `EXCEPTION_INVENTORY_MISMATCH`, the one legacy `exceptionType` that carried meaning, died one line after it was
   thrown.
2. The API returned that boolean, so nothing about *why* crossed the wire.
3. `OrderInventoryOrchestratorImpl.reserveProduct` wrapped the call in `catch (Exception _) -> status(false)`, making
   "out of stock" and "catalog is unreachable" the same value on the caller's side.

Now: the service throws `InsufficientInventoryException` naming the sku, the requested quantity and what was actually
available; `CatalogApiErrors.CATALOG` maps that code to `ProductReservationRejectedException` on the caller's side and
anything unreachable to `CatalogApiUnavailableException`; and `OrderPlacementFacadeImpl` branches on the types. A
rejection cancels the order — nothing was reserved, so there is nothing to release. An unavailable catalog propagates,
leaving the order `CREATED`/`NOT_REQUESTED` and recoverable, because cancelling it would both mislead the shopper and
abandon stock catalog may in fact be holding.

**The API is now a pair**, following payment: `IProductReservationService` (plain, server vocabulary, implemented by
`ExternalProductReservationApi`) and `ExternalProductReservationService` (`@HttpExchange`, caller vocabulary,
implemented by nothing). `ExternalProductReservationApi`'s `@SneakyThrows` went with it — it existed only to hide the
`throws` clause the split now makes honest.

**`ExternalOrderApi` uses the `UncheckedBaseException` carrier, deliberately.** `ExternalOrderService` is a *single*
interface — checkout's controller implements it and payment and catalog proxy it — so naming
`CatalogApiUnavailableException` in its `throws` clause would put catalog's vocabulary into a signature payment reads,
when payment called checkout, not catalog. The carrier is the designed escape hatch for a signature that cannot be
widened: the advice unwraps it, callers get a 502 naming catalog as the remote, and payment's outbox retries. Splitting
this API too would have bought nothing — no caller can act on the difference.

### Everything else in checkout

**Blanket catches deleted, not re-coded.** `saveOrder`, both `getReadableOrder`s, `getReadableOrderList`,
`createOrderStatus`, `addToCart`, `modifyCart`, `getByCode` and `ReadableShoppingCartMapper.merge` each wrapped their
whole body in `catch (Exception)` and rethrew one legacy type — which meant a `ResourceNotFoundException` thrown three
lines earlier came back out as a 500. Each is gone; the failures that were being swallowed are now declared.

**`throws Exception` eliminated from `ShoppingCartFacade`** — five methods, replaced by the conditions each actually
has. That interface was the worst signature in the module: strictly less informative than `throws BaseException`, which
the rules already forbid.

**A raw `response.sendError(404)`** in `ShoppingCartApi.getByCode` bypassed the advice entirely and returned an HTML
error page where every other 404 returns a problem document. It is `ShoppingCartNotFoundException` now, and
`HttpServletResponse` left the signature with it.

**One `IllegalStateException`, on purpose.** `BeanUtils.copyProperties` declares two reflective checked exceptions
meaning our own DTOs cannot be introspected. That is a bug in this code, not a condition a caller can act on, so it
gets no named type and falls to the advice's internal-error fallback — a 500 with a `traceId`. It used to become
`ConversionRuntimeException`: `LEGACY.CONVERSION`, and a 400 blaming the caller.

**Residual `ServiceException`, same root as everywhere else.** 33 references remain in checkout, every one of them
reaching `SalesManagerEntityService`'s `create`/`update`/`delete`. They are declared, not caught-and-rewrapped, and
they disappear with that root in Step 8. Two vestigial declarations that did *not* come from it were simply dropped:
`ShoppingCartService.populateShoppingCartItem` and `OrderService.calculateOrderTotal`, neither of which could throw.

---

## Step 7 as built — `catalog`

**205 legacy throw sites across 45 files, down to 2.** Four times the size the plan estimated (it said 74, counted
with a pattern that omitted `ConversionRuntimeException` and `OperationNotAllowedException`). The two survivors are
`CategoryServiceImpl` and `ProductServiceImpl`, both reaching `SalesManagerEntityService`.

**`CatalogErrors` grew to 45 codes and 58 exception classes** in `catalog-commons/.../catalog/errors/`, grouped by
resource rather than by category — that is how they get looked up, and the `ErrorCategory` on each still says what the
status will be.

### The `Mapper` SPI had to widen, for the same reason `DataPopulator` did

`Mapper.convert`/`merge` declared nothing, which is precisely why every mapper in the repo ended in
`catch (Exception) -> ConversionRuntimeException`: an unchecked legacy type was the only thing that fitted through the
signature. Both now declare `com.asrevo.cvhome.errors.ConversionException`, the second and last deliberate
category-base declaration.

**Cost of the widening: one site.** `ManufacturerFacadeImpl` held its mapper as `Mapper<Manufacturer,
ReadableManufacturer>` — the interface type, through which javac sees only the base — and is now typed as the concrete
`ReadableManufacturerMapper` so the narrowing is visible. Every other caller already sat inside a `try`.

### What the SPI's ceiling forced, and what it revealed

A mapper can only declare `ConversionException` subtypes, which ruled out reporting a 404 or a 403 from inside one.
Rather than widen further, that constraint was taken as a design question and answered: **a dangling reference inside a
submitted payload is not the same condition as a failed lookup by path id.**

| | Thrown from | Type | Status |
|---|---|---|---|
| `GET /product/{id}` finds nothing | facade | `ProductNotFoundException` | 404 |
| A product body names a category that does not resolve | mapper | `CategoryReferenceUnresolvableException` | 400 |

Nine `*ReferenceUnresolvableException` types carry the second case, each naming the id it could not resolve. The
endpoint's own target exists; it is a field inside the body that names nothing, and a 400 about the payload is the
honest answer. Three conditions were re-categorised to fit the same reading — `PRODUCT_VARIANT_PARENT_MISSING`,
`PRODUCT_VARIANT_SKU_CONFLICT` and the category parent-store check, all of which surface inside a mapper.

**The cross-store checks fold into not-found, deliberately.** An option, option value or category belonging to another
store now answers 404 rather than naming the mismatch: a 403 would confirm the row exists, which is exactly what
someone walking ids wants to learn. `ForeignStoreProductAccessException` (403) is kept for the *product* guards in
`ProductApi` and `ProductImageApi`, where the caller already holds the product id from a legitimate path.

### The behaviour changes worth naming

| | Before | After |
|---|---|---|
| `GET` a product type that does not exist | 500 — the `ResourceNotFoundException` was thrown *inside* a `try` whose `catch (Exception)` re-emitted it | 404 `CATALOG.PRODUCT_TYPE.NOT_FOUND` |
| Another store's product or category | 401 `UnauthorizedException` — a login loop for an authenticated caller | 403 `CATALOG.PRODUCT.FOREIGN_STORE` |
| A product with no priced inventory | 500 `LEGACY.SERVICE_ERROR` | 422 `CATALOG.PRICING.NO_APPLICABLE_INVENTORY` |
| Duplicate product type / option set / variation code | 400 | 409, so a client can offer "try another code" |
| A category already on the product | 400 | 409 `CATALOG.CATEGORY.ALREADY_ATTACHED` |
| `POST` an inventory with no `productId` param | `RestApiException`, 500 | 400 `CATALOG.PRODUCT.ID_PARAMETER_MISSING` |
| A price id that does not exist | `ServiceRuntimeException`, 500 | 404 `CATALOG.PRODUCT_PRICE.NOT_FOUND` |

The first row is the pattern that recurred most: **nine `try` blocks whose `catch (Exception)` swallowed a
`ResourceNotFoundException` thrown three lines above it.** The 404 could never reach a caller. Deleting the catch is
what fixed them; no new code was needed.

### Repository wrappers deleted rather than migrated

`CategoryServiceImpl.getByCode` and `getListByLineage`, and three lookups in `ProductServiceImpl`, wrapped bare Spring
Data calls in `try { … } catch (Exception e) { throw new ServiceException(e); }`. A repository signals infrastructure
failure with an unchecked `DataAccessException`, which the shared advice already renders as a 500 with a `traceId`, so
the wrapper only added a checked type to every caller's signature and told them nothing. They are gone, and
`ProductServiceImpl.getBySku` now separates "no such sku" (404, naming it) from "the query failed" (500) — the
ServiceException had made those one thing.

**Eleven `stream().map(...)` pipelines became plain loops**, and one `RuntimeException`-wrapping lambda in
`ReadableProductGroupPopulator` went with them. Step 4's guidance held up: *prefer restructuring the lambda away over
reaching for `Unchecked`* — it was not needed once in this step either. Six `@SneakyThrows` were deleted; every one was
hiding a base the signature can now name.

**One place the base could not be narrowed at the throw site.** `ProductFacadeV2Impl.listProducts` takes the mapper as
a *parameter*, because its two public callers pass different implementations, so javac sees only `ConversionException`.
It is narrowed there, once, to `ProductNotConvertibleException` — true of every path into the method — with a comment
saying why.

### On the tooling

The last ~120 signature updates in `catalog-service` were applied by two throwaway scripts: one that reads javac's
`unreported exception X` errors and adds `X` to the enclosing method's `throws`, one that adds the missing import.
Both are purely mechanical — every semantic decision (which condition, which status, which category) was made by hand
at the throw site, and the scripts only carry an already-proven declaration outward. They are not committed.

---

## Remaining work, step by step

None. Steps 7 and 8 are described above under **Status**; the gates below are the ones that were run to close them.

---

## Verification

**Per step:** `./gradlew :<module>:build`, then `./gradlew build` (checkstyle runs with it and must stay clean).
Then two greps must return zero **for that module**:

```
grep -rn "throw new \(ServiceException\|GenericRuntimeException\|ServiceRuntimeException\|ConversionRuntimeException\|ResourceNotFoundException\|UnauthorizedException\|ConstraintException\|RestApiException\|OperationNotAllowedException\)" <module>

grep -rn "throws \(BaseException\|ValidationException\|ResourceNotFoundException\|RemoteServiceException\|ExternalProviderException\|DuplicateResourceException\|OperationNotAllowedException\|AccessDeniedStoreException\|StoreIOException\)\b" <module>
```

The second is the gate for rule 2: throwing a base is already a compile error, but *declaring* one still compiles, so
this is what catches a step that swapped one generic type for another — the failure mode Steps 1 and 2 both hit on
their first pass. Two legitimate hits exist: `Unchecked.rethrow` genuinely declares `throws BaseException` (that is
its job), and `BaseException`'s own javadoc quotes the rule.

**Widen the first pattern before running it.** Step 6's version omitted `ConversionRuntimeException` and Step 7's
omitted `OperationNotAllowedException`; both omissions hid real sites — three in checkout, and enough in catalog to
make the estimate off by 3×. The pattern above is the corrected one. There are two further legitimate `throws`-base
hits now: `DataPopulator` and `Mapper`, the two SPIs that declare `ConversionException` on purpose — plus
`AbstractDataPopulator`, which implements the first. `ConversionException` is now **in** the second pattern (it was
omitted while the deprecated `store.core.exception.ConversionException` still existed; that class was deleted in
Step 8), and the type names in the first pattern now match nothing at all, since every class it lists is gone.

**Two further gates, run once at the end of Step 8** and both zero: `grep -rn "LEGACY\."` over all sources, which must
find no live code — only historical javadoc — and a grep for `{@link}` references to a deleted type, which would be a
dangling javadoc link. `{@code}` mentions of a deleted type in a "Replaces …" note are deliberate and stay.

**The standing test suite — MISSING. Rebuild it before Step 4.**

Every one of the tests the three merged plans describe as the regression net is **absent from the working tree**,
verified 2026-08-03: `GlobalErrorHandlerTest`, `AdviceScopeTest`, `TypedRemoteErrorRoundTripTest`,
`RemoteProblemTranslatorTest`, and the `ProbeApi` / `probe` fixtures. `store-commons/autoconfigure/src/test` does not
exist and `store-pod/commons/store-commons/src/test` is empty; they were untracked, never committed, and are now
gone. Repo-wide there are **8 test classes, all `*ApplicationTests` context loads**.

So "`./gradlew test` is green" currently means eight Spring contexts start — nothing about the wire format, the
advice's scope, or the typed s2s round trip. Steps 4–7 are ~229 sites of the bulk migration; running them against no
regression net is how the wire format drifts silently, which is precisely what these tests were written to prevent.

What has to come back, in priority order:

1. `RemoteProblemTranslatorTest` (9 cases) — the decoder's behaviour pin. When a change is meant to be a move rather
   than a rewrite, **no assertion in it may change**; if one does, the refactor changed behaviour.
2. `TypedRemoteErrorRoundTripTest` (5 cases, `MockRestServiceServer`) — the only thing covering `@HttpExchange` →
   handler → carrier → declared type, including that a method declaring nothing is unaffected and that a `null`
   catalog degrades rather than NPEs. Needs updating for the two-interface split: build the client from
   `External*Service`, not the server half.
3. `GlobalErrorHandlerTest` (10 cases) + `ProbeApi` — pins `code`/`category`/`status`/`traceId`, that `detail` leaks
   no root cause, and the three who-failed shapes.
4. `AdviceScopeTest` — exists because defect 2 was invisible and would otherwise return.
5. New: one case for `DataIntegrityErrorHandler` (409, not 500), added in Step 3 with no test at all.

Two test-only Gradle additions are needed again in `store-commons/autoconfigure`:
`testImplementation project(':store-commons:commons')` (the argument resolvers reference its domain types, which are
`compileOnly` there) and `testImplementation libs.spring.webflux`.

**Local end-to-end** (`docker-compose-lcl`, through the real gateway paths, asserting on raw JSON — the frontend is
out of scope). Rows 1–2b are regression checks now; 3–7 are each step's acceptance signal:

| After step | Check | Before this work |
|---|---|---|
| 1 ✅ | `GET` a non-existent product → 404, not 500; `traceId` in body matches the log line | 500, NPE in the advice |
| 1 ✅ | `POST` with a missing required field → 400 with populated `fieldErrors[]` | impossible — no handler existed |
| 1 ✅ | Oversized image upload → 413 from the shared advice | 413 from a per-service copy |
| 1 ✅ | Price `"abc"` → 400 `STORE.PRICE.NOT_PARSEABLE`; `"-5"` → 400 `STORE.PRICE.NOT_POSITIVE` with a `price` field error | both "Invalid product price format" |
| 1 ✅ | Any un-migrated endpoint → a `LEGACY.*` code with the right status, never a bare 500 | 500, NPE in the advice |
| 2 ✅ | Stripe webhook with an invalid payload → 400 `PAYMENT.WEBHOOK.INVALID` | 500, no handler at all |
| 2b ✅ | Order placed with payment-service **stopped** → 502, inventory reservation **released**, order left recoverable | exception escaped `placeOrder`; reservation leaked |
| 2b ✅ | Order placed with a declined test card → typed `PaymentGatewayRejectedException`, order fails cleanly; body carries `remoteService: payment` and payment's own code | `COMMON.REMOTE_CALL_FAILED`, remote code dropped at the first hop |
| 3 ✅ | Unknown uaa user → 404 `UAA.USER.NOT_FOUND`, not a 400 titled "User Not Found"; control-plane returns the same shape as the pods | 3 different formats |
| 3 ✅ | `POST /api/v1/admin/clients/{unknown}/reset-secret` → 404, not a 200 that rotated nothing | silent success |
| 3 ✅ | Register a shopper with a taken email → the form highlights **email**; delete a row still referenced → 409, not 500 | error under username; 500 |
| 4 | Upload with S3 unreachable → 500 `CMS.ASSET.UPLOAD_FAILED` with `traceId`; `GET` a deleted image → 404 `CMS.ASSET.NOT_FOUND`; checkout with an unsupported country code → 400 `CUSTOMER.COUNTRY.UNSUPPORTED` carrying the code in `params` | generic 500; 500; silently "unable to create customer" |
| 5 | `POST` a content page whose code exists → 409 `CONTENT.CODE.DUPLICATE`; `PUT` a page id that does not → 404, not 409; create a store whose id is taken → 409 `MERCHANT.STORE.DUPLICATE`; delete the default store → 422 | one generic 400 for all four |
| 6 | Checkout with a sku short of stock → order `CANCELLED`, body carries `CATALOG.RESERVATION.INSUFFICIENT_INVENTORY`, `remoteService: catalog` and the `sku`/`requested`/`available` params | `status(false)`, order cancelled with no reason |
| 6 | Checkout with catalog-service **stopped** → 502, order left `CREATED`/`NOT_REQUESTED` and recoverable — **not** cancelled as out of stock | `status(false)`, order cancelled and the shopper told the item was unavailable |
| 6 | Store with `requireLoginForOrderPlacement` — no token → 401 `CHECKOUT.ORDER.LOGIN_REQUIRED`; a token for another store → 403 `CHECKOUT.ORDER.CLIENT_MISMATCH` | both 400, with "HTTP 401" in the message text |
| 6 | `GET /api/v1/cart/{unknown}` → 404 problem document | HTML error page from a raw `sendError` |
| 7 | `GET` a product type that does not exist → 404 `CATALOG.PRODUCT_TYPE.NOT_FOUND` | 500 — the 404 was thrown inside a `try` that swallowed it |
| 7 | Another store's product → 403 `CATALOG.PRODUCT.FOREIGN_STORE`, no internal detail; another store's *option* → 404, which does not confirm the row exists | 401 for both |
| 7 | Duplicate product type / option set / variation code → 409 with the code in `params` | 400 |
| 7 | `POST` a product whose body names an unknown category → 400 `CATALOG.CATEGORY.REFERENCE_UNRESOLVABLE` naming the id, **not** a 404 | one generic 400 for every bad reference |
| 7 | Price a product with no inventory → 422 `CATALOG.PRICING.NO_APPLICABLE_INVENTORY` | 500 |

---

## Corrections — why the design looks like this

Each of these shipped wrong first, in one of the three merged plans. They are the most useful thing carried forward.

**Abstract bases are the enforcement, not the documentation.** Rules 1–7 were review comments that got skipped under
deadline. Making every category base `abstract` turned rule 1 into a compile error. Rule 2 still compiles, which is
why it needs the grep gate.

**One exemption rots the whole scheme.** `RemoteServiceException` was left concrete on the argument that a remote can
fail in ways we have no type for — and `RemoteProblemTranslator` was in fact throwing it, so the generic type the
rules forbid everywhere else was throwable from anywhere. "We have no name for this" is itself a nameable condition:
`UnmappedRemoteFailureException`. There are now no exemptions.

**A better code on a legacy exception is not a migration.** Both Step 1 and Step 2 shipped this on the first pass. It
produces a plausible-looking diff, an improved response body, and a signature that still says nothing. A
`ServiceException(ErrorCode, String)` constructor was added as a migration shortcut and then deliberately removed.

**Mis-scoped advice is silent.** Defect 2 above. One `@ControllerAdvice` with no basePackages, plus `AdviceScopeTest`.

**Code that exists is not code that runs.** `RemoteProblemTranslator.unreachable(...)` was called only from its own
test — transport failures escaped as raw `ResourceAccessException` in production. Separately, `PaymentGatewayService`
was catching its own rejection and returning `failed()`, so the endpoint answered HTTP 200 and the entire typed path
was unreachable. Both looked complete and tested.

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

**One signature cannot carry two vocabularies.** `ExternalPaymentGatewayApi implements
ExternalPaymentGatewayService` made one interface both the controller's contract and the client proxy. Declaring the
caller-side exceptions on it gave the controller a signature listing failures it can never throw (Java allows
narrowing, so it compiled silently) and blocked the server from ever declaring a new failure of its own (Java forbids
widening). Hence the split pair.

**A second interface is cheaper than a wrapper class.** The caller-side vocabulary first arrived as a hand-written
`PaymentGatewayClient`/`RestPaymentGatewayClient` wrapping the generated proxy, whose whole body was
catch-the-carrier-and-rethrow — code that existed only to restate what a `throws` clause already says, and one more
thing to keep in step every method. Declaring the caller's types on the `@HttpExchange` interface makes
`declaredOrCarrier` do it, because that is the method the proxy is invoked through. Both classes are deleted. The
cost of the split is that the two interfaces' paths are no longer checked against each other by the compiler — which
is exactly how `status` drifted (see *Open defects*), so check them by eye when adding a method.

**A blanket catch is where the migration keeps finding real defects.** Every module so far has had at least one
`catch (Exception) -> <legacy type>` wrapped around code that cannot fail the way the wrapper claims — an NPE in a
mapper reported as a 400, a duplicate check swallowed by the catch two lines below it, an object-store failure and a
missing file rendered identically. The wrapper always looks defensive and is always the thing hiding the bug. **When
migrating a method, read what its catch can actually receive before choosing a type for it**; more than once the
answer has been "nothing checked, delete the catch".

**A generic SPI is the one honest place for a category base — and it must widen before the leaves can migrate.**
Step 4 could not give `CustomerPopulator` condition-named types until `DataPopulator` stopped declaring the
*deprecated* `ConversionException`, because a new type under the shared base was not a subtype of it. The fix was two
one-line changes — widen the SPI, and make the deprecated class extend the shared base rather than `BaseException` —
after which migrated and un-migrated populators coexist. Attempting the leaf migration first, without touching the
SPI, would have forced all 27 populator files into one commit. When a migration stalls on "this new type does not fit
the old signature", look up at the SPI before compromising the type.

**A shared signature narrows outward.** Removing a generic type from `PriceUtils.getAmount` broke both its callers,
which had been catching `ServiceException` and rethrowing a generic runtime type — discarding the distinction the
parser had just made. A two-line collateral change downstream is the normal price, and it is cheapest early.

**Stale javadoc survives every compiler.** After renaming `PaymentErrorCatalog` → `PaymentApiErrors`, seven doc
comments still named the deleted class, two of them describing behaviour reversed a session earlier. The wrapper
deletion has just done the same thing again — three files still describe it. Prose compiles fine while actively
misleading. Grep for deleted type names, not just for code references.

---

## Out of scope

- **Frontend consumption**, deliberately and throughout: seller-ui's `HttpErrorInterceptor` + a typed `ApiError`
  model, `code` → i18n key resolution against the new namespaced vocabulary (the `ERROR` namespace holds 4 keys
  today, and 167 call sites pass `ERROR.SYSTEM_ERROR`), `fieldErrors` bound to reactive-form controls, and
  landing-ui's `handleResponse` in `libs/services/src/http-utils.ts`, which discards non-2xx bodies entirely and
  returns `undefined`. Plan separately once Step 7 lands and the vocabulary is stable.
- **Typed unwrapping on the reactive path** — impossible through a proxy; `onErrorMap` is the answer.
- **Retry / circuit-breaking.** The types make "retryable vs permanent" expressible; acting on it is separate work.
- **Step 2c** — making `PAYMENT.CONFIGURATION.MISSING` and `PAYMENT.PROCESSOR.UNSUPPORTED` real server-side throws.
  They are `PaymentInitiateResult.failed()` results today, which is why the two matching caller-side types were
  deleted rather than left unreachable. Doing it changes `initiatePayment`'s contract with checkout from a failed
  result to a propagated error, so it is its own decision.

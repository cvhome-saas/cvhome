# Typed errors for the uaa admin SDK — the non-Spring client

Extends `.claude/plans/error-handling.md`. That plan's Step 2b made an `-external-api` module a client SDK that
decodes wire errors back into named types. `uaa-client` is the same thing for a client that has no Spring at all, and
this is what it takes to give it the same contract.

## Context

`store-commons:uaa-client-impl` talks to uaa over plain `java.net.http`, and it throws away everything the server
says:

```java
protected void verifyResponse(HttpResponse<String> response) {
    if (response.statusCode() >= 400) {
        throw new ApiException(String.format("API call failed with status %d: %s", statusCode, body));
    }
}
```

`ApiException` is an untyped `RuntimeException` carrying a **string** — the exact shape the whole error refactor
exists to remove. Three consequences, all live today:

1. **Step 3's work is invisible.** uaa now answers `404 UAA.USER.NOT_FOUND`, `409 COMMON.DATA_INTEGRITY_VIOLATION`,
   `403 UAA.USER.SUPER_ADMIN_IMMUTABLE` with a full problem body. The SDK concatenates all of it into a message and
   the caller cannot branch on any of it.
2. **`ManagedUserNotFoundException` can never fire.** `ManagedUserAccountServiceImpl.validateUserAccess` (and
   `findOne`) null-check the `ReadableUser`, but `UserAccountServiceImpl.findOne` calls
   `toReadableUser(client.getUser(id))` — on a 404 the SDK throws `ApiException` long before a null could be
   returned, so control-plane's 404 path is dead code and a missing user surfaces as a **500**.
3. **`UserAccountService` says nothing can go wrong**, so every caller — `ManagedUserAccountServiceImpl`,
   `SignupServiceImpl`, `OrgManagerController` — is written as if uaa always succeeds.

**Outcome:** uaa's problem body is decoded into named exceptions the caller can catch, using the same
`RemoteErrorCatalog` / `RemoteExceptionFactory` contract as payment, and without adding Spring to a plain-Java SDK.

## Two decisions taken

| | |
|---|---|
| **Shared decoder, Map-based** | The catalog lookup and problem decoding move into `store-commons:errors` as `RemoteFailures`, working on a plain `Map<String,Object>`. Each transport does its own JSON→Map with the Jackson it already has. Preserves the errors module's zero-dependency property; no new Gradle module |
| **One interface, typed through** | `UserAccountService` declares the uaa types; control-plane catches them and restates them in its own vocabulary |

**Why one interface and not payment's two.** The split in `payment-external-api` exists because a single
`@HttpExchange` interface was implemented by *both* the server's controller and the generated client proxy, so its
`throws` clause had to be two vocabularies at once. `UserAccountService` has no such collision — nothing on the server
implements it; uaa's controllers are `AdminUserController`/`AdminClientController` with their own signatures. So it
takes the caller-side types directly and needs no second interface. **Do not copy the payment split here.**

---

## Design

### 1. `RemoteFailures` — the shared, dependency-free core

New `store-commons/errors/.../errors/remote/RemoteFailures.java`. Everything `RemoteProblemTranslator` does *except*
parsing JSON, which is the only part that needs a library:

```java
public final class RemoteFailures {

    /** Problem document as a plain map → the context a RemoteExceptionFactory needs. */
    public static RemoteErrorContext contextOf(Map<String, Object> problem, String service, String path,
            int status, Throwable cause);

    /** find(code) → factory, else UnmappedRemoteFailureException with a status-derived code. */
    public static RemoteServiceException resolve(RemoteErrorCatalog catalog, RemoteErrorContext context);

    /** No response at all: the catalog's transport factory, else the timeout/unavailable pair. */
    public static RemoteServiceException unreachable(RemoteErrorCatalog catalog, String service, String path,
            Throwable cause);
}
```

All of it is `Map`, `List`, `String` and `java.net` — `SocketTimeoutException` / `HttpTimeoutException` for the
timeout check are JDK types, so the zero-dependency rule holds and `-external-api` modules can still declare a
contract without pulling in `autoconfigure`.

Moved in verbatim from `RemoteProblemTranslator`: the `code`/`detail`/`traceId`/`params`/`fieldErrors` extraction,
the `service`/`path`/`remoteStatus` param defaults, `codeFor(status)` (504/408 → `REMOTE_TIMEOUT`, else
`REMOTE_CALL_FAILED`), `isTimeout`, and the `fallback(...)` builder.

### 2. `RemoteProblemTranslator` becomes a Jackson adapter

`store-commons/autoconfigure/.../s2s/error/RemoteProblemTranslator.java` keeps its Spring-facing signature
(`HttpStatusCode`) and its two public methods, but its body collapses to:

```java
public static RemoteServiceException translate(RemoteErrorCatalog catalog, URI uri, HttpStatusCode status,
        String body) {
    return RemoteFailures.resolve(catalog,
            RemoteFailures.contextOf(readProblem(body, uri), serviceNameOf(uri), uri.getPath(), status.value(), null));
}
```

`readProblem` is `MAPPER.readValue(body, Map.class)` inside the existing try/catch that tolerates an HTML error page
or an empty body. **`readTree` and the four `JsonNode` helpers — `text`, `scalar`, `readParams`, `readFieldErrors` —
are deleted**, roughly 90 lines. Behaviour is unchanged; this is a move.

### 3. `uaa-client` becomes the client SDK

**Move `UaaErrors`** from `store-core/uaa/.../uaa/errors/` to `store-commons/uaa-client/.../uaa/errors/`, and add
`implementation project(':store-commons:uaa-client')` to `store-core/uaa/build.gradle`. The catalog keys on
`ErrorCode` constants rather than strings so a rename cannot orphan a mapping, which means server and SDK must share
the enum — exactly as `PaymentErrors` is shared from `payment-commons`.

**Built with one change:** uaa's three condition types (`UserNotFoundException`, `ClientNotFoundException`,
`SuperAdminImmutableException`) moved with the enum rather than staying behind. Leaving them would have split the
`com.asrevo.cvhome.uaa.errors` package across two jars for no gain; moving them makes `uaa-client` the exact analogue
of `payment-commons` — the shared module holding a context's vocabulary *and* its condition types — with the
caller-side family beside it in `uaa.api.errors`, as `payment-external-api` has it. The package name is unchanged, so
no import in `store-core/uaa` needed editing.

**New caller-side family**, `store-commons/uaa-client/.../uaa/api/errors/`:

```
abstract UaaApiException extends RemoteServiceException      remoteService = "uaa"
 ├── UaaUserNotFoundException        UAA.USER.NOT_FOUND
 ├── UaaClientNotFoundException      UAA.CLIENT.NOT_FOUND
 ├── UaaOperationForbiddenException  UAA.USER.SUPER_ADMIN_IMMUTABLE
 ├── UaaConflictException            COMMON.DATA_INTEGRITY_VIOLATION   (duplicate username/email)
 └── UaaApiUnavailableException      transport failure, token failure, and any unmapped code
```

Each with a `from(RemoteErrorContext)` factory built through `RemoteServiceException.of(code, Factory)`, copying
`PaymentGatewayRejectedException` / `PaymentApiUnavailableException`. `UaaApiErrors.CATALOG` maps the four codes and
sets `.unreachable(UaaApiUnavailableException::from)`.

`uaa-client/build.gradle` gains `api project(':store-commons:errors')` — these types appear in `UserAccountService`'s
signature, so consumers need them at runtime, and the current `compileOnly project(":store-commons:commons")` does
not expose them.

### 4. The SDK throws them

`AbstractAdminClient` gets a static `RemoteErrorCatalog` (the constant above) and:

```java
protected void verifyResponse(HttpResponse<String> response) throws UaaApiException {
    if (response.statusCode() < 400) {
        return;
    }
    URI uri = response.uri();
    throw RemoteFailures.resolve(CATALOG, RemoteFailures.contextOf(
            readProblem(response.body()), "uaa", uri.getPath(), response.statusCode(), null));
}
```

with `readProblem` being this class's own `objectMapper.readValue(body, Map.class)` in a try/catch — the same ~10
lines as the Spring side, and the only duplication the Map-based split costs.

`resolve` returns `RemoteServiceException`; the cast to `UaaApiException` is safe because every factory in the
catalog produces one, but the fallback does not — so `verifyResponse` wraps anything that is not a `UaaApiException`
(an unmapped code arriving as `UnmappedRemoteFailureException`) into `UaaApiUnavailableException.wrapping(cause)`,
mirroring the payment SDK's method of the same name.

The `IOException` / `InterruptedException` catches in `sendAndParse`, `sendAndParsePage` and `sendAndVerify` change
from `UncheckedIOException` / `IllegalStateException` to `RemoteFailures.unreachable(CATALOG, "uaa", path, e)` — this
is the transport path that has never been represented at all. **`OAuth2TokenManager.refreshToken`** does the same:
a failed `client_credentials` exchange becomes `UaaApiUnavailableException`, because nothing was decided about the
request the caller wanted to make.

**Per-method `throws` — the plan was wrong here, and the compiler said so.** The intent was for each SDK method to
name only what its endpoint can answer. It does not type-check: every method routes through one `send(...)`, which
resolves against the catalog and can therefore produce any type in the family, so a method declaring a subset cannot
call it. The precision was aspirational — at the transport, any uaa endpoint really can answer any code.

What was built instead, which is both honest and simpler:

| Layer | Declares | Why |
|---|---|---|
| `AdminUserClient`, `AdminClientClient` | `UaaApiException` throughout | the transport genuinely cannot narrow |
| `UserAccountService` + `Impl` | the specific types per operation | this layer knows what the call was *for* |

The impl does the narrowing in a `try`/`catch` per method: rethrow the types this operation can mean, and fold
everything else into `UaaApiUnavailableException` via a private `undecided(...)`. Two helpers keep it short —
`mutate(...)` for enable/disable/delete, which share a failure shape, and a generic
`restatingNotFound(userId, call)` in control-plane whose `<E extends Throwable>` passes each call's *other* failure
(a conflict on update, the super-admin guard on delete) straight through without widening any signature.

That division is the payment SDK's "catalog = deserialisation, wrapper = policy" split, arrived at from the other
direction: the judgement the compiler cannot make, made once, in the one place that has the context to make it.

`AdminClientClient` has **no caller anywhere in the repo** today — it is covered for consistency, and because
`resetSecret`'s 404 is one of the two bugs Step 3 just fixed on the server side.

### 5. `UserAccountService` declares them; control-plane restates them

`UserAccountService` (uaa-client) takes the same `throws` clauses, mapped through `UserAccountServiceImpl` unchanged
— the impl is pure delegation, so it only needs the declarations.

`ManagedUserAccountServiceImpl` is where the vocabulary changes from "uaa said no" to control-plane's own:

```java
@Override
public ReadableUser findOne(String id) throws ManagedUserNotFoundException, UaaApiUnavailableException {
    try {
        return userAccountService.findOne(id);
    } catch (UaaUserNotFoundException e) {
        throw ManagedUserNotFoundException.of(id, e);   // new cause-carrying factory
    }
}
```

This is what finally makes `ManagedUserNotFoundException` reachable. The now-dead `if (user == null)` check in
`validateUserAccess` is **deleted** — `findOne` is the only way a user is obtained and it can no longer return null.

`UaaApiUnavailableException` is deliberately **not** caught: uaa being unreachable is a 502, not a 404, and the
shared advice already renders a `RemoteServiceException` correctly. It propagates through `ManagedUserAccountService`,
`UserAccountController`, `SignupServiceImpl` and `OrgManagerController.changePassword`, each of which declares it —
the same forcing function that Step 3 applied, and the compiler will list the sites.

`ManagedUserNotFoundException` gains `of(String userId, Throwable cause)`; the existing single-arg factory stays for
the local case.

### 6. Delete `ApiException`

`store-commons/uaa-client-impl/.../uaa/exception/ApiException.java` goes now rather than in Step 8 — nothing outside
the SDK references it, and leaving an untyped escape hatch in a module that has just been typed is how it comes back.

---

## Files

**Created** — `store-commons/errors/.../remote/RemoteFailures.java`;
`store-commons/uaa-client/.../uaa/api/errors/`: `UaaApiException`, `UaaUserNotFoundException`,
`UaaClientNotFoundException`, `UaaOperationForbiddenException`, `UaaConflictException`,
`UaaApiUnavailableException`, `UaaApiErrors`.

**Moved** — `UaaErrors` from `store-core/uaa/.../uaa/errors/` to `store-commons/uaa-client/.../uaa/errors/`.

**Deleted** — `uaa-client-impl/.../uaa/exception/ApiException.java`; ~90 lines of `JsonNode` helpers in
`RemoteProblemTranslator`; the dead null-check in `ManagedUserAccountServiceImpl.validateUserAccess`.

**Modified** — `RemoteProblemTranslator`; `AbstractAdminClient`, `OAuth2TokenManager`, `AdminUserClient`,
`AdminClientClient`, `UserAccountServiceImpl`; `UserAccountService`; `ManagedUserAccountService` + `Impl`,
`UserAccountController`, `SignupServiceImpl`, `OrgManagerController`, `ManagedUserNotFoundException`;
`uaa-client/build.gradle` (`api project(':store-commons:errors')`), `store-core/uaa/build.gradle`
(`implementation project(':store-commons:uaa-client')`).

---

## Verification

**Characterize before refactoring.** The nine `RemoteProblemTranslatorTest` cases that pinned this decoder no longer
exist (see `error-handling.md` → the suite is missing). Since step 1 moves working, untested code, write
`RemoteFailuresTest` **first**, against the current behaviour, then refactor until it still passes:

- a full problem body → `code`, `detail`, `traceId`, `params`, `fieldErrors` all present on the context;
- `params` gains `service`, `path`, `remoteStatus` when the body omits them, and does **not** overwrite them when it
  supplies them;
- a mapped code → the catalog's type; an unmapped code → `UnmappedRemoteFailureException` still carrying the remote's
  code and status;
- a non-JSON body (HTML error page) and an empty body → degrade, never throw;
- 504 and 408 → `REMOTE_TIMEOUT`; everything else → `REMOTE_CALL_FAILED`;
- `unreachable` with a `SocketTimeoutException` cause → `RemoteServiceTimeoutException`, otherwise
  `RemoteServiceUnavailableException`; with a catalog that sets `.unreachable(...)` → that type instead.

**New — `UaaApiErrorsTest`** (plain unit, no HTTP): feed each of uaa's four codes through
`RemoteFailures.resolve(UaaApiErrors.CATALOG, …)` and assert the named type, `remoteService() == "uaa"`,
`remoteCode()` and `remoteStatus()`.

**SDK round trip** — `AbstractAdminClient` takes an injectable `HttpClient` (constructor overload, defaulting to
today's `HttpClient.newBuilder().build()`) so a stub can return a canned uaa problem body. Assert that
`getUser` on a 404 `UAA.USER.NOT_FOUND` throws `UaaUserNotFoundException`, and that a connection failure throws
`UaaApiUnavailableException`. This is the equivalent of `TypedRemoteErrorRoundTripTest` for the non-Spring path.

**Build** — `./gradlew :store-commons:errors:build :store-commons:uaa-client:build
:store-commons:uaa-client-impl:build :store-commons:autoconfigure:build :store-core:uaa:build
:store-core:control-plane:control-plane-service:build`, then `./gradlew build` (checkstyle included, currently
clean).

**Grep gates** — `grep -rn "ApiException" store-commons/uaa-client-impl` empty; `grep -rn "JsonNode"
store-commons/autoconfigure/.../s2s/error/` empty.

**Local end-to-end** (`docker-compose-lcl`, control-plane + uaa):

| Check | Today |
|---|---|
| `GET /api/v1/user-account/find-one?userId=<unknown>` → **404** `CONTROL_PLANE.USER.NOT_FOUND` | 500 — `ApiException` from the SDK; the 404 branch is unreachable |
| Create an org user whose username already exists → **409**, not 500 | 500 with uaa's body inside a message string |
| Same call with **uaa stopped** → **502** `COMMON.REMOTE_UNAVAILABLE`, `remoteService: uaa` | 500 `UncheckedIOException` |
| A user from another org → 403 `CONTROL_PLANE.USER.FOREIGN_ORG`, unchanged | unchanged |

## Out of scope

- The rest of the missing test suite (`GlobalErrorHandlerTest`, `AdviceScopeTest`, `TypedRemoteErrorRoundTripTest`)
  — tracked in `error-handling.md`; only the two decoder tests above are required here, because this change moves
  that specific code.
- Steps 4–8 of the parent plan.
- `uaa-client-impl`'s `PageResponse`/DTO shapes and the Jackson-3-vs-`com.fasterxml` annotation mix in
  `OAuth2TokenManager.TokenResponse` — noticed, unrelated, working.

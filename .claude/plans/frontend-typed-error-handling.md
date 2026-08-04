# Frontend typed error handling — seller-ui & landing-ui

## Context

The backend finished a three-refactor migration to a single typed error contract: every one of the 8
services emits the same extended RFC-7807 body, built only by `ProblemDetailFactory`, carrying a stable
machine-readable `code`, an `ErrorCategory` that fixed the HTTP status, structured `params`,
`fieldErrors`, and a `traceId` that joins the response to the server log line.

**Both frontends throw all of it away.**

- **seller-ui** never reads the response body. 240 call sites across 66 files do
  `errorService.error('ERROR.SYSTEM_ERROR', err)` — a 400 validation failure, a 409 duplicate SKU, a 403
  and a 500 all render the identical toast "System Error". There is no HTTP interceptor, no `ErrorHandler`,
  no client type for the error body, and `setErrors` appears zero times, so backend `fieldErrors` never
  reach a form control.
- **landing-ui** is worse: `handleResponse` (`libs/services/src/http-utils.ts`) discards the status *and*
  the body on any non-2xx and returns `undefined`. Because it resolves rather than rejects, every
  downstream `.catch(...)` is dead code for backend failures. Consequences reach the money path: on a
  checkout 4xx/5xx the shopper's cart is cleared from `localStorage` and they see "Payment failed"; a
  checkout-service outage renders a genuinely paid order as "We couldn't find this order." No template has
  an `error.tsx`, and a merchant-pod outage renders a blank page (`return null` from the layout).

**Outcome:** one typed error model per frontend, fed from the real `ProblemDetail`, with a translated
message resolved from the backend's own `code`, field errors bound to the controls that caused them, a
`traceId` a shopper or seller can quote to support, and the checkout money-path bugs the current
swallowing hides.

**Decisions taken** (asked and confirmed):
- i18n chain `ERRORS.CODE.<code>` → `ERRORS.CATEGORY.<category>` → `ERRORS.GENERIC`. All 13 categories
  translated in all 5 locales (the guaranteed floor); code-level keys hand-written only for the errors
  each audience actually meets. No stub translations for the 126 codes.
- Full migration — every call site moves, and the old generic-toast path is deleted, not left beside the new one.
- The landing-ui checkout behaviour bugs are in scope; they are direct consequences of the swallowing.

---

# Section 1 — The backend contract (what the frontends must consume)

Read `.claude/skills/project-structure/references/error-handling.md` before implementing. The parts that
bind the frontend work:

**Wire format** — one shape, all services, built only by
`store-commons/autoconfigure/.../errors/web/ProblemDetailFactory.java`:

```json
{ "type": "https://errors.asrevo.com/catalog/product/not-found",
  "title": "CATALOG.PRODUCT.NOT_FOUND", "status": 404,
  "detail": "Product [42] does not exist in store [7]",
  "code": "CATALOG.PRODUCT.NOT_FOUND", "category": "NOT_FOUND",
  "params": { "productId": 42, "storeId": 7 },
  "fieldErrors": [ { "field": "sku", "code": "...", "message": "...", "params": {} } ],
  "traceId": "3f9a1c8e" }
```

Plus, mutually exclusively: `remoteService`/`remoteStatus` (a peer cvhome service failed) **or**
`provider`/`providerCode`/`providerStatus` (a third party — Stripe — failed).

**Rules the frontend must respect:**

1. **`detail` is developer text and must never be rendered.** `ErrorHandlingProperties.includeDebugDetail`
   defaults to `false`, so in production it is usually absent; when present it may carry internal
   specifics. It is for `console.error` only.
2. **`code` is the translation key.** `title` duplicates it. It is already dotted and stable.
3. **`category` is the guaranteed fallback.** Every code has one, from
   `store-commons/errors/.../ErrorCategory.java`: `VALIDATION`/`MALFORMED`/`CONVERSION` 400,
   `UNAUTHENTICATED` 401, `FORBIDDEN` 403, `NOT_FOUND` 404, `CONFLICT` 409, `PAYLOAD_TOO_LARGE` 413,
   `UNPROCESSABLE` 422, `STORAGE`/`INTERNAL` 500, `REMOTE_SERVICE` 502, `TIMEOUT` 504.
4. **422 vs 502 is a decided-vs-undecided distinction, not a severity one.** In `payment`,
   `PAYMENT.INITIATE.REJECTED` (422) means the card was refused — retrying will not help, unwind the order.
   `PAYMENT.INITIATE.FAILED` (502) means *no answer* — the payment may have started; hold and reconcile,
   never tell the shopper it failed. Collapsing these is exactly how orders get cancelled after being charged.
5. **`fieldErrors[].field` arrives in two shapes.** `GlobalErrorHandler` emits bean paths
   (`endpoint.endpoint`, `items[0].sku`); `ConstraintViolationErrorHandler` emits
   `jakarta.validation` property paths prefixed with the method name (`createPod.pod.name`). Both need
   normalizing to a form path before `form.get()`.
6. **126 distinct codes exist** across 15 `*Errors.java` enums (`CatalogErrors` alone has 57). ~125
   `LEGACY.*` throw sites remain un-migrated — they render in the correct format under a `LEGACY.*` code,
   so the category fallback is what covers them until the backend finishes. This is why the fallback chain
   is load-bearing and not a nicety.

Enumerate the universe with:
`grep -rhoE '"[A-Z][A-Z0-9_]*\.[A-Z0-9_.]+"' --include='*Errors.java' . | sort -u`

---

# Section 2 — seller-ui (Angular 20 SSR)

## New files

All under a new `src/app/core/` — it must serve both `src/app/pages/**` and `src/app/public/**`, and
`pages/shared` cannot be imported from `public` without inverting the dependency.

| File | Responsibility |
|---|---|
| `core/errors/problem-detail.model.ts` | `ProblemDetail`, `ProblemFieldError`, `ErrorCategory` — exact mirror of the wire types |
| `core/errors/api-error.ts` | `ApiError extends Error` + `isApiError()` guard; getters `isValidation`/`isAuth`/`isForbidden`/`isServerSide` |
| `core/errors/problem-detail.parser.ts` | Pure `toApiError(raw, url)`. No DI, heavily unit-tested |
| `core/errors/api-error.interceptor.ts` | `HttpInterceptorFn`: normalize, notify `SessionService` on 401, rethrow `ApiError` |
| `core/errors/api-error.service.ts` | The i18n resolution chain + `notify()` / `applyToForm()` |
| `core/errors/session.service.ts` | Login redirect with a one-shot latch, SSR-safe |
| `core/errors/form-error.utils.ts` | `applyFieldErrors()`, `clearServerErrorsOnChange()` |
| `core/errors/global-error-handler.ts` | `ErrorHandler` for whatever escapes |
| `core/notifications/notification.service.ts` | The single `NbToastrService` wrapper |
| `pages/shared/components/field-error/` | `FieldErrorComponent` + `ControlStatusDirective` (presentational, so not in `core`) |

## Normalization belongs in an interceptor, not `CrudService`

`CrudService` (`src/app/pages/shared/services/crud.service.ts`) is the main wrapper but **not** the only
one: `pages/shared/services/store.service.ts` and `pages/store-management/services/dns-check.service.ts`
inject `HttpClient` directly and would silently keep the old behaviour. `CrudService.request()` also
returns `Observable<HttpEvent<unknown>>` for upload progress, which a blanket `catchError` complicates.
Only an interceptor sees the `HttpRequest`, which is what makes `HttpContextToken` opt-outs expressible
(`dns-check` treats a 404 as a legitimate answer; `TranslateHttpLoader` fetches local JSON). `CrudService`
stays what it is — a URL and store/pod param builder.

```ts
export const apiErrorInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.context.get(SKIP_ERROR_NORMALIZATION)) return next(req);
  const session = inject(SessionService);
  return next(req).pipe(catchError((raw: unknown) => {
    const error = toApiError(raw, req.urlWithParams);
    if (error.isAuth) session.onUnauthenticated(error);
    return throwError(() => error);
  }));
};
```

Registered at `src/app/app.config.ts:32`:
`provideHttpClient(withFetch(), withInterceptors([apiErrorInterceptor]))`.

**The interceptor must not toast.** Presentation (toast vs inline vs form errors) is a call-site decision;
only auth is global.

## What a facade receives

The observable **errors with an `ApiError` instance** — never an `HttpErrorResponse`. Rethrowing rather
than mapping to a result object keeps every existing `subscribe({error})` / `catchError` shape intact, so
the 240 call sites migrate mechanically instead of being restructured.

```ts
// mechanical case
error: (e: ApiError) => this.apiErrors.notify(e)

// branching case
.pipe(catchError((e: ApiError) => {
  if (e.code === 'CATALOG.PRODUCT_VARIANT.SKU_CONFLICT') { this.skuTaken.set(true); return EMPTY; }
  if (e.isValidation) { this.apiErrors.applyToForm(e, this.formService.form); return EMPTY; }
  this.apiErrors.notify(e);
  return EMPTY;
}))
```

## Parser branches (`toApiError`) — in order

1. Already an `ApiError` → return as-is (idempotent; `CrudService.request()` streams get re-piped).
2. `status === 0` → `NETWORK` / `CLIENT.NETWORK_UNAVAILABLE`.
3. `error` is an object with string `code` **and** string `category` → the real thing. Drop `detail` from
   the user-facing payload. Normalize `fieldErrors[].field` via an exported `normalizeFieldPath()`:
   `items[0].sku` → `items.0.sku`; strip the leading method segment from constraint-violation paths
   (`createPod.pod.name` → `pod.name`). This is the only fiddly part — unit-test it directly.
4. Body is a `string` → `JSON.parse`, re-enter (3).
5. `error instanceof ProgressEvent` → `NETWORK`.
6. Any other `HttpErrorResponse` (Caddy HTML 502, blob body) → synthesize from status via a
   `statusToCategory` map mirroring `ErrorCategory.java`; code `CLIENT.HTTP_<status>`.
7. Otherwise → `UNKNOWN` / `CLIENT.UNEXPECTED`.

`ApiError.message` is set to a diagnostic string (`${code} [${status}] traceId=…`), never user copy, so a
stray `{{err.message}}` in a template leaks nothing that looks translated.

## fieldErrors → reactive form controls

```ts
export function applyFieldErrors(form: AbstractControl, fieldErrors: readonly ProblemFieldError[],
                                 opts?: FieldErrorOptions): ProblemFieldError[]  // returns UNMATCHED
```

Two invariants that make this survive real use:

- **The error key is always `server`**, its value the whole `ProblemFieldError` — one branch for the
  display component, and `code` + `params` ride along for translation.
- **Server errors self-clear**, or the form stays permanently invalid. `clearServerErrorsOnChange(form,
  destroyRef)` subscribes once to `valueChanges`, strips any `server` key and calls
  `updateValueAndValidity({emitEvent: false})`, deleting the whole `errors` object when `server` was the
  only key.

`ApiErrorService.applyToForm()` composes the two and toasts whatever came back unmatched (an object-level
`@AssertTrue` has no control to attach to).

**`FieldErrorComponent`** replaces the ~15 hand-rolled `<p class="caption status-danger">` blocks.
Standalone, `OnPush`, signal inputs. `AbstractControl` is not signal-reactive, so bridge `control.events`
(the `TouchedChangeEvent` matters — every existing template gates on `dirty || touched`) through
`toSignal`. Server errors win over client validators: they are the authoritative statement about the value.

```html
<input formControlName="name" fullWidth id="name" nbInput ngxControlStatus>
<ngx-field-error [control]="facade.formService.name"
                 [messages]="{required: 'POD_FORM.NAME_REQUIRED', pattern: 'POD_FORM.NAME_PATTERN'}"/>
```

Add `ERRORS.FIELD.{required,pattern,email,minlength,maxlength,min,max}` generics so `[messages]` is
optional for the common cases. Pilot on `pod-form.component.html` — it has `required`, `pattern`, and a
nested `endpoint.endpoint` path that exercises path normalization — then convert the rest.

## One toast stack: Nebular

Evidence, not preference. `app.config.ts` provides `NbToastrModule.forRoot()`; **nothing** provides
`provideToastr()`/`ToastrModule.forRoot()` for ngx-toastr, and `ngx-toastr/toastr.css` is imported in no
stylesheet — so `PublicNotificationService` throws `NullInjectorError` on `/signup` today. 240 call sites
already route through Nebular, which also inherits the app theme and RTL handling (Arabic ships).

- Delete `pages/shared/services/error.service.ts` and `public/shared/services/public-notification.service.ts`.
- Add `core/notifications/notification.service.ts`, the only `NbToastrService` wrapper:
  `success/info/warning(key, params?)` take **i18n keys** (matching the 60 existing `.success('X.Y')` calls
  verbatim); `danger(message, opts?)` takes an **already-resolved** string, because `ApiErrorService` owns
  the resolution chain and nothing else should build error copy.
- Drop `ngx-toastr` from `package.json`.

**Required sub-step:** `src/app/public/public.component.ts` has no `nb-layout`, so Nebular's overlay
container has no host and public-area toasts silently do nothing. Wrap the public shell in
`<nb-layout><nb-layout-column>`. Verify visually on `/signup` before calling this done — if it disturbs
the public CSS, fall back to an inline banner in the public forms and no toasts there.

`SignUpFormFacade` currently discards the error and shows hardcoded English ("Failed to Register!",
"Please fill all required fields"). The backend emits `CUA.REGISTRATION.EMAIL_TAKEN` /
`USERNAME_TAKEN` — route it through `applyToForm`.

## Auth: 401 redirect, 403 does not

`SessionService.onUnauthenticated()` owns the redirect, shared by the guard and the interceptor, with a
`redirecting` latch — a dashboard fires several parallel requests, and a session expiring mid-flight
yields N simultaneous 401s. Guarded by `isPlatformBrowser`; SSR must never touch `window`.

**403 is not a redirect.** A seller lacking a permission (`COMMON.ACCESS_DENIED`) must see
`ERRORS.CATEGORY.FORBIDDEN`, not be bounced to a login they are already past.

**Fix `pages/shared/services/auth-guard.service.ts:16`** — today *any* error from `getAuthUser()`
(a 500, a network blip) sends the user to `LOGIN_URL`, which loops forever against a broken backend:

```ts
catchError((error: ApiError) => {
  if (error.isAuth) { session.onUnauthenticated(error); return of(false); }
  return of(router.createUrlTree(['/pages/miscellaneous/500'], {queryParams: {traceId: error.traceId}}));
})
```
Returning a `UrlTree` rather than `false` avoids the blank-screen-on-`false` failure mode. Check whether a
miscellaneous 500 route already exists before inventing one; the smaller version is
`of(false)` + `apiErrors.notify(error)`.

## i18n

Codes contain dots and ngx-translate treats dots as nesting, so **flatten**: `codeToKey()` does
`code.replace(/\./g, '_')` → `ERRORS.CODE.CATALOG_PRODUCT_NOT_FOUND`. One exported helper, used by both
`translate()` and any tooling. Flat keys diff cleanly across five files; a key missing from `ar.json` is
visible by eye.

```json
"ERRORS": {
  "CODE": {
    "CATALOG_PRODUCT_NOT_FOUND": "Product {{productId}} no longer exists.",
    "CATALOG_PRODUCT_VARIANT_SKU_CONFLICT": "SKU {{sku}} is already used by another variant.",
    "CATALOG_RESERVATION_INSUFFICIENT_INVENTORY": "Only {{available}} of {{sku}} left in stock."
  },
  "CATEGORY": { "NOT_FOUND": "We couldn't find what you asked for." },
  "GENERIC": "Something went wrong. Please try again.",
  "TRACE": "Reference: {{traceId}}"
}
```

`params` is a flat map and ngx-translate's default interpolation is `{{name}}`, so it passes straight
through. Missing-key detection: `instant(key)` returns the key when unresolved —

```ts
const value = this.translate.instant(key, params);
return value === key ? null : value;
```

`ERRORS.GENERIC` is guaranteed in all five locales, so the chain always terminates.

**Curated seller-facing set (~40):** `CATALOG.PRODUCT*` / `CATEGORY` / `PRODUCT_TYPE` / `PRODUCT_GROUP`
`NOT_FOUND|DUPLICATE|NOT_EDITABLE|SKU_CONFLICT`, `CATALOG.RESERVATION.INSUFFICIENT_INVENTORY`,
`CONTENT.*`, `CMS.ASSET.*`, `MERCHANT.STORE.{DUPLICATE,NOT_FOUND,DEFAULT_NOT_REMOVABLE}`,
`PAYMENT.{CONFIGURATION.MISSING,INITIATE.FAILED,INITIATE.REJECTED,PROCESSOR.UNSUPPORTED}`,
`CONTROL_PLANE.{POD,USER}.*`, `UAA.USER.*`, `CUA.REGISTRATION.*`, `CUSTOMER.*`, and all 21 `COMMON.*`.

Files: `public/assets/i18n/{en,ar,es,fr,ru}.json`. **AR is RTL** — check toast layout, not just strings.

## traceId

1. Appended to the toast as a second line **only when `error.isServerSide`** — a seller does not need a
   reference number for "SKU already taken".
2. Query param on the error route from the guard.
3. Always `console.error`'d by `GlobalErrorHandler` with `{code, category, status, traceId, url,
   remoteService, provider, providerCode}`. This is the only place `detail` may be logged.
   `PAYMENT.INITIATE.REJECTED` carrying `providerCode: card_declined` is precisely what support asks about.

## Migration recipe — 240 calls, 66 files

| Before | After |
|---|---|
| `errorService.error('ERROR.SYSTEM_ERROR', err)` (177×) | `apiErrors.notify(err)` |
| `errorService.success('X.Y')` (60×) | `notify.success('X.Y')` |
| `errorService.warning('X.Y')` (1×) | `notify.warning('X.Y')` |
| `error:` handler in a **form submit** | `error: (e: ApiError) => this.apiErrors.applyToForm(e, this.formService.form)` |
| `errorService.handleError(err)` | delete — `GlobalErrorHandler` covers it |

Representative: `src/app/pages/payment/facades/payment-list.facade.ts`

```diff
-  private readonly errorService = inject(ErrorService);
+  private readonly apiErrors = inject(ApiErrorService);
+  private readonly notify = inject(NotificationService);
-          this.errorService.success('TRANSACTION.APPROVE_SUCCESS');
+          this.notify.success('TRANSACTION.APPROVE_SUCCESS');
-        error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
+        error: (e: ApiError) => this.apiErrors.notify(e)
```

Two files need judgement, not sed:
- `pages/shared/services/storage.service.ts` — deprecated 3-arg `subscribe(next, error)` and, per its own
  comment, dead code. Migrate or delete.
- `pages/content/pages/services/add-page.form.service.ts` — a *form service* injecting `ErrorService`; the
  natural first host for `applyToForm` + `clearServerErrorsOnChange`.

Also fix the two swallowers found during exploration:
- `pages/shared/services/store.service.ts:36` — `catchError(() => of(defaultPageOnError))` fabricates a
  fake store list on a backend outage.
- `pages/store-management/services/dns-check.service.ts:83` — returns `of(false)`, making a service
  failure indistinguishable from a genuine negative DNS result. This one keeps its opt-out via
  `SKIP_ERROR_NORMALIZATION` but must return a tri-state, not a boolean.

Enforce completion with an ESLint `no-restricted-imports` ban on `error.service` /
`public-notification.service`, added in the commit that deletes them.

## Steps (each leaves the app building)

1. Model + parser + `problem-detail.parser.spec.ts` covering all 7 branches and `normalizeFieldPath()`.
   Nothing wired; zero runtime risk.
2. i18n: `ERRORS` block in all five locales + a spec asserting the five files have identical key sets
   under `ERRORS`. That spec is the only thing that will keep them in sync.
3. `NotificationService`, `ApiErrorService`, `SessionService`. Delete nothing yet.
4. Interceptor + `app.config.ts` registration + `GlobalErrorHandler`. From here every observable errors
   with `ApiError`; old call sites still compile and still show the generic toast, so steps 4–8 are safe.
5. Auth: guard fix, 401 dedup, `nb-layout` wrap for the public shell.
6. Forms: `form-error.utils.ts`, `FieldErrorComponent`, `ControlStatusDirective`; pilot `pod-form`, then
   the other ~14 templates.
7. Migrate call sites by feature area, one commit each (~8): catalogue → content → orders/payment →
   store-management → user/org/pod → shared + public.
8. Remove: delete both old services, drop `ngx-toastr`, add the ESLint ban, delete the orphaned `ERROR.*`
   keys from all five locales.

---

# Section 3 — landing-ui (Next.js 16 / React 19)

Shopper-facing, so the priorities differ: fewer distinguishable errors, but the ones that exist are on the
money path, and a blank page is a lost sale.

## The choke point

`libs/services/src/http-utils.ts` is the single function every one of the ~33 backend calls funnels
through, and it is where everything is lost:

```ts
export function handleResponse<T>(it: Response): T | undefined {
    if (it.ok) return it.json() as unknown as T;
    console.error(`Request failed: ${it.status} ...`);
    return undefined;                       // status and body both gone
}
```

Note the declared type is a lie — the OK path returns a `Promise<T>` typed as `T`. Because it *resolves*
rather than rejects, **every `.catch(...)` downstream is dead code for backend failures**; they fire only
on DNS/CORS/JSON-parse errors. That is why the checkout `onError` branch has never run in production.

## New files

| File | Responsibility |
|---|---|
| `libs/types/src/api-error.ts` | `ProblemDetail`, `ProblemFieldError`, `ApiError` class, `isApiError()`. Exported from `libs/types/src/index.ts` |
| `libs/services/src/http-utils.ts` (rewrite) | `handleResponse` becomes async and **throws** `ApiError` |
| `libs/services/src/error-messages.ts` | `errorMessageKey(error)` — the `CODE → CATEGORY → GENERIC` chain as next-intl keys |
| `libs/hooks/src/use-api-error.ts` | Hook: `{ toastError, messageFor }`, wrapping `nextjs-toast-notify` with the RTL-aware options currently duplicated 7× |
| `templates/*/src/app/[locale]/error.tsx` | Route error boundary (× 4 templates) |
| `templates/*/src/app/[locale]/global-error.tsx` | Root boundary (× 4) |
| `templates/*/src/app/[locale]/not-found.tsx` | 404 (× 4) |
| `templates/*/src/components/ui/alert.tsx` | shadcn Alert primitive — currently absent from all four templates |
| `templates/*/src/shared/ErrorState.tsx` | Shared inline error panel used by the boundaries and CheckoutResult |

## The rewrite

```ts
export async function handleResponse<T>(res: Response, url?: string): Promise<T> {
  if (res.ok) return res.status === 204 ? (undefined as T) : await res.json() as T;
  throw await toApiError(res, url);   // parses application/problem+json; falls back to status mapping
}
```

`toApiError` mirrors the seller-ui parser: read the body as text, `JSON.parse`, accept it as a
`ProblemDetail` only when `code` and `category` are both strings, otherwise synthesize from status.
**Drop `detail`** — same rule as seller-ui.

Because this now throws, every one of the ~33 call sites in `cart-service` (7), `customer-service` (5),
`content-service` (4), `product-service` (4), `auth-service` (3), `category-service` (3),
`product-category` (3), `store-service` (2), `order-service` (2) changes behaviour. That is the point, but
it means each needs a decision: **does this call have a sensible degraded rendering, or must it fail the
page?** Split accordingly:

- **Must fail loudly** (checkout, order status, cart mutations, auth) — let it throw; the caller handles it.
- **Degrades gracefully** (a content box that fails to load, a recommendations strip) — wrap at the call
  site in a small `orUndefined(promise)` helper so the *choice* is explicit and greppable, rather than
  being the invisible default it is today.

## SSR: no more blank pages

`templates/*/src/app/[locale]/layout.tsx` currently does:

```ts
const store = await StoreService.getStore(storeContext);
if (!store) return null;      // merchant-pod outage → no <html>, no status, blank screen
```

Replace with a thrown `ApiError` propagating to `error.tsx`. Add per template:

- **`global-error.tsx`** — must render its own `<html>`/`<body>`; it is the only boundary that catches a
  root-layout failure. Static copy, no next-intl (the i18n provider may itself be what failed).
- **`[locale]/error.tsx`** — `'use client'`, receives `{error, reset}`, renders `ErrorState` with the
  translated message from `errorMessageKey(error)` plus a retry button wired to `reset()`.
- **`[locale]/not-found.tsx`** — a real 404 for a missing product/category, replacing the Express
  `404.html` fallback that only fires on a missing `store-id` header.

Next.js strips error details in production server components, so `error.digest` is what survives to the
client. Log the full `ApiError` (including `traceId`) server-side and surface **`traceId` in the UI** —
it is the one string a shopper can give support.

## Checkout — the money path

Three fixes, all consequences of the swallowing.

**(a) `libs/services/src/cart-manager.ts:108-117` clears the cart before validating the response:**

```ts
CartService.checkout(...).then((order) => {
    this.setCartData(undefined);   // ← runs even when the backend refused
    if (onSuccess) onSuccess(order);
})
```

Clear only on a confirmed order. Also: when `this.cart` is undefined, neither callback fires and the
submit button silently does nothing — call `onError` with a client-side `CART.EMPTY`.

**(b) `libs/hooks/src/use-checkout-form.ts:127-146` collapses every failure into one toast.** With typed
errors, branch on what the backend actually said:

| Error | Shopper sees |
|---|---|
| `CATALOG.RESERVATION.INSUFFICIENT_INVENTORY` (422) | "Only N left of {{sku}}" — keep the cart, return to cart page |
| `PAYMENT.INITIATE.REJECTED` (422) | "Your card was declined" — **decided**; keep cart, let them retry with another method |
| `PAYMENT.INITIATE.FAILED` (502) / `TIMEOUT` (504) | **Undecided — never say "failed".** "We're confirming your payment" + the order reference; do not clear the cart, do not offer an immediate retry that could double-charge |
| `category === 'UNAUTHENTICATED'` | Re-auth, once — see (d) |
| `VALIDATION` + `fieldErrors` | Bind to the react-hook-form controls via `setError(field, …)` |
| anything else | `ERRORS.CATEGORY.<category>` + traceId |

Rule (4) from Section 1 is the whole reason this table exists: 422 and 502 from payment are *not*
different severities of the same thing.

**(c) `CheckoutResult.tsx` cannot tell an outage from a missing order.** Today
`orderStatus === undefined` renders `RESULT_NOT_FOUND`, so a checkout-service blip tells a shopper who
just paid that their order does not exist. `useOrderStatus` (`libs/hooks/src/use-order-status.ts`) already
captures an `error` state that no template renders. Distinguish three states: 404 → genuinely not found;
any other `ApiError` → "We can't reach the order service — your payment went through, reference {{traceId}}"
with a retry; `undefined` while loading → pending. Add a bounded retry (3 attempts, backoff) for 5xx only.

**(d)** `libs/hooks/src/use-user.ts` does `catch { setUser(null) }`, so a `cua` outage looks like "logged
out" and `use-checkout-form.ts:99-105` opens `LoginRequiredDialog` in a loop. Distinguish 401 (really
logged out) from 502/network (cua unreachable → show an error, do not open the dialog). Also make
`login/login-client.tsx` read the `?error=true` that `callback-client.tsx` already sets and nothing consumes.

## Toasts

`nextjs-toast-notify` is imperative and needs no provider, which is why it works today with no toaster in
any layout. Keep it. The 5-line options object duplicated across 7 sites moves into `use-api-error.ts`,
reusing the existing `toastDirection(locale)` from `libs/services/src/direction-utils.ts` for RTL.

Toast only for *actions*. Page-load failures use `ErrorState` / `error.tsx`, not a 3-second toast that
vanishes before it is read.

## i18n

Locales are shared by all four templates: `store-pod/landing-ui/locales/{en,ar,es,fr,ru}.json`, loaded by
`templates/*/src/i18n/request.ts`. Add a top-level `ERRORS` namespace alongside the existing `PAGE`,
`COMPONENTS`, etc. Same flattened key shape as seller-ui.

`getMessageFallback` currently renders missing keys literally as `-*KEY*-` in the UI — with the category
floor present in all five locales that is unreachable for errors, but keep the `ERRORS.GENERIC` terminator
anyway.

**Curated shopper-facing set (~15)** — this is deliberately small; shoppers do not benefit from
`CATALOG.PRODUCT.NOT_CONVERTIBLE`:

`CATALOG.RESERVATION.INSUFFICIENT_INVENTORY`, `CATALOG.PRODUCT.NOT_FOUND`,
`CHECKOUT.CART.{NOT_FOUND,EMPTY,EXPIRED}`, `CHECKOUT.ORDER.{NOT_FOUND,ALREADY_PLACED}`,
`PAYMENT.INITIATE.{REJECTED,FAILED}`, `PAYMENT.CONFIGURATION.MISSING`,
`CUA.{REGISTRATION.EMAIL_TAKEN,LOGIN.INVALID_CREDENTIALS}`, `CUSTOMER.*`,
`COMMON.{UNAUTHENTICATED,VALIDATION_FAILED,REMOTE_UNAVAILABLE}`.

Migrate the existing ad-hoc keys (`PAGE.CHECKOUT.PAYMENT_FAILED`, `FAILED_TO_PLACE_ORDER`,
`RESULT_FAILED_TITLE/_MESSAGE`, `RESULT_NOT_FOUND`, `COMPONENTS.PRODUCT.FAILED_TO_ADD_PRODUCT_TO_CART`,
`FAILED_TO_REMOVED_PRODUCT`) into `ERRORS.*` and delete the originals from all five files — they exist in
all five today, so this is a clean move. **AR is RTL.**

## Steps

1. `libs/types/src/api-error.ts` + export it; add the `ERRORS` namespace to all five locale files.
2. Rewrite `http-utils.ts` to throw; add `orUndefined()`. **Nothing else compiles-and-behaves until step 3**,
   so 2 and 3 land together.
3. Sweep all ~33 `handleResponse` call sites, classifying each as must-fail vs degrades-gracefully.
4. `error.tsx` / `global-error.tsx` / `not-found.tsx` + `alert.tsx` + `ErrorState` in **all four**
   templates; remove the `return null` from each `layout.tsx`.
5. `use-api-error.ts`; migrate the 7 duplicated toast option blocks.
6. Checkout fixes (a)–(d) — the highest-value step, and the one to test hardest.
7. Delete the migrated ad-hoc error keys from the five locale files.

**Build order matters:** `libs/*` and `templates/*` changes must go through the root `npm run build`
chain (libs → templates → app). Building a template alone against stale `libs` types will silently use
the old `handleResponse` signature.

---

# Verification

## seller-ui

```bash
cd store-core/seller-ui
npx tsc -p tsconfig.json --noEmit        # strictTemplates is on
npm run lint
npm test -- --watch=false --browsers=ChromeHeadless
npm run build                             # SSR build; catches window/document use in new code

# migration completeness — all must print 0
grep -rn "errorService\." src --include="*.ts" | wc -l
grep -rn "ERROR.SYSTEM_ERROR" src public/assets/i18n | wc -l
grep -rn "ngx-toastr" src package.json | wc -l
grep -rn "caption status-danger" src --include="*.html" | wc -l

# locale parity under ERRORS
node -e "const f=['en','ar','es','fr','ru'].map(l=>require('./public/assets/i18n/'+l+'.json').ERRORS); \
 const k=o=>Object.entries(o).flatMap(([a,b])=>typeof b==='object'?Object.keys(b).map(c=>a+'.'+c):[a]).sort(); \
 const b=k(f[0]); f.forEach((x,i)=>console.log(i, JSON.stringify(k(x))===JSON.stringify(b)))"
```

## landing-ui

```bash
cd store-pod/landing-ui
npm run build                             # MUST be the root chain: libs -> templates -> app
grep -rn "handleResponse" libs templates --include="*.ts*" | wc -l   # all awaited / try-caught
node -e "..."                             # same locale-parity check over locales/*.json ERRORS
```

## Repo gates

```bash
./gradlew checkstyleMain checkstyleTest    # unchanged, but CI runs it
./gradlew build -x test -x check
```

## End-to-end (nothing above catches these)

Run the stack locally (`docker compose -f docker-compose-lcl.yml up -d`, then `bootRun` the pod services
with `--spring.profiles.active=lcl`; `lcl` sets `includeDebugDetail=true`, so confirm `detail` still never
reaches the UI).

**seller-ui:** mid-session 401 → exactly one redirect, not N (open a dashboard, expire the cookie in
devtools, click around) · 403 → toast, no redirect · backend stopped → `ERRORS.CATEGORY.NETWORK`, no login
loop · POST a duplicate SKU → the specific message, not "System Error" · a 400 with `fieldErrors` → the
messages land on the right controls and clear on typing · `/signup` → the toast actually appears (the
`nb-layout` fix) · switch to `ar` → RTL toast, correct interpolation.

**landing-ui:** stop `merchant-service` → the error boundary renders, not a blank page · stop
`checkout-service` mid-checkout → cart is **still there**, message is "confirming", not "failed" ·
force `PAYMENT.INITIATE.REJECTED` (Stripe test card `4000000000000002`) → "card declined", cart kept,
retry offered · force a 502 from payment → undecided copy, no retry button, reference shown · stop
`checkout-service` then load `/checkout/success?orderId=…` → "can't reach the order service", **not**
"we couldn't find this order" · add an out-of-stock quantity → the inventory message with the real count ·
`ar` locale → RTL toast position via `toastDirection`.

# Extract `seller-core` — a shared Angular library out of seller-ui

## Context

`../../store-core/seller-ui` is a single-project Angular 20 CLI workspace: ~360 `.ts` files under `src/`, no
barrels, no path aliases, every import a deep relative path (`../../../core/errors/api-error`). Domain
logic — HTTP services, DTOs, the error stack, validators, table state — sits interleaved with Nebular
components and facades in `src/app/pages/**`. Nothing is reusable outside this app, and the boundary
between "view logic" and "domain logic" is enforced only by convention.

The goal: move everything that is **not** UI logic into a real Angular library at
`projects/seller-core`, so a future Angular app can depend on it, and so the domain tier has a
compiler-enforced boundary (it cannot import back into `src/app`).

**Decisions locked with the user:**
- Location: `projects/` inside the existing seller-ui workspace. No Gradle or `../../settings.gradle` changes.
- Consumers: future Angular apps. The lib must be self-contained and ship as a real package.
- Scope: core tier **and** all feature domain services + models. Components, facades, templates,
  routes, and `NotificationService` stay in the app.

## What moves, what stays

**Moves** — services with HTTP calls, models/DTOs/enums, mappers, pure utils, validators, the error
stack, guards, interceptors, table types & state.

**Stays** — every `*.component.ts`, every `facades/*` (73 of them; 20+ import `@nebular/theme`),
`core/notifications/notification.service.ts` (Nebular toastr; `eslint.config.js` keys a rule exemption
to its exact path), `home/models/echarts.model.ts` (`NbEchartsTheme` is a Nebular view-model),
`pod-management/services/pod-form.service.ts` (`FormBuilder` → `FormGroup` = UI tier),
`public/routing/external-logout-link.component.ts`, and `core/errors/i18n-errors.spec.ts` (asserts
parity across `public/assets/i18n/{en,fr,ar,es,ru}.json`, and the assets stay).

## Key design decisions

| Question | Decision | Why |
|---|---|---|
| Strictness | Lib `tsconfig` **inherits the app's loose flags** (`strictNullChecks:false`, `noImplicitAny:false`); override only `declaration:true` | Tightening would surface real latent bugs (`SelectedStoreService.getStore()` returns `undefined` into `CrudService.getParams()`'s `let store: ManagerStore`) *during* a ~400-import mechanical refactor. Two risky changes at once is unreviewable. Flipping `strictNullChecks` on in `tsconfig.lib.json` alone is a clean follow-up PR — and is precisely the payoff of having a small lib tsconfig. |
| `environment.ts` | `InjectionToken<SellerCoreConfig>` + `provideSellerCore(config, ...features)` | A library cannot import the app's environment. Only 4 files import it today, 3 of which move. |
| `CrudService` ↔ `SelectedStoreService` | Split behind a `REQUEST_CONTEXT` token with `SelectedStoreService` as the **default** provider — but in a later step, after the move is green | `CrudService` secretly appends `store`/`pod` to *every* request; a future app without a "selected store" cannot use it at all. ~25 lines, behaviour-identical for seller-ui. Doing it after the move keeps the move diff purely mechanical. |
| Unguarded `localStorage` | `BrowserStorage` injectable, `isPlatformBrowser`-guarded | In `selected-store.service.ts`, `user.service.ts`, and `selected-language.service.ts`'s **constructor**. Survives today only because `app.routes.server.ts` marks those routes `RenderMode.Client`. A shipped lib can't carry that landmine. Deliberate hardening — flag it in the PR's *Deviations*. |
| `api-error.service.ts` | **Moves**, behind a `NOTIFICATION_PORT` token; app provides `useExisting: NotificationService` | It *is* the error stack — the `ERRORS.CODE.x → ERRORS.CATEGORY.x → ERRORS.GENERIC` fallback chain is the valuable part. The Nebular coupling is one call. `@ngx-translate/core` becomes a peer dep regardless (`ConfigService` needs it). |
| Barrels | **Secondary entry points**, not one flat barrel | Three name collisions force it: `StoreService` (shared vs store-management), `SubscriptionService` (public vs subscription-and-usage), `types.ts` twice. Also gives real tree-shaking granularity. |
| Build wiring | `paths` → lib **source** during migration; flip to `dist/seller-core` + `ng build seller-core && ng build seller-ui` as the committed final state | Consumers are future apps, so the shipped artifact must be a real package — but source paths remove a ~30s rebuild from each of ~12 checkpoints. |

**Rename first:** `pages/shared/services/store.service.ts`'s class `StoreService` → **`ManagerStoreService`**
(it already returns `ManagerStore`). IDE rename in Step 3, before anything imports it via a barrel.

## Entry-point layout

```
projects/seller-core/
  ng-package.json  package.json  tsconfig.lib.json  tsconfig.lib.prod.json  tsconfig.spec.json
  src/public-api.ts                 -> 'seller-core'
    lib/config/    seller-core.config.ts, provide-seller-core.ts
    lib/http/      crud.service.ts, request-context.ts
    lib/auth/      auth.service.ts, auth-guard.service.ts, user.service.ts, roles.ts
    lib/errors/    api-error*, problem-detail*, session.service, form-error.utils,
                   global-error-handler, api-error.service, notification.port.ts
    lib/models/    commons.ts, entity.model.ts, user.ts, subscription.model.ts, Language.ts
    lib/table/     table.types.ts, table-events.ts, table-state.service.ts
    lib/platform/  browser-storage.ts
    lib/util/      slugifying.ts, validators.ts
    lib/store/     store.service.ts (ManagerStoreService), selected-store.service.ts,
                   selected-language.service.ts, config.service.ts
  catalog/ orders/ content/ customers/ stores/ orgs/ payments/ subscriptions/ analytics/ signup/
      each: ng-package.json + src/public-api.ts   -> 'seller-core/<name>'
```

Resulting call sites:
```ts
import {CrudService, ApiError, ManagerStore, TableStateService} from 'seller-core';
import {ProductService, ReadableProduct} from 'seller-core/catalog';
import {StoreService, DnsCheckService} from 'seller-core/stores';
import {SubscriptionService} from 'seller-core/subscriptions';   // vs 'seller-core/signup'
```

## Config sketches

**`projects/seller-core/ng-package.json`**
```jsonc
{ "$schema": "../../node_modules/ng-packagr/ng-package.schema.json",
  "dest": "../../dist/seller-core",
  "lib": { "entryFile": "src/public-api.ts" },
  "allowedNonPeerDependencies": ["moment"] }
```
Each secondary entry point: `{ "lib": { "entryFile": "src/public-api.ts" } }` — nothing else, `dest` inherited.

**`projects/seller-core/package.json`**
```jsonc
{ "name": "seller-core", "version": "0.0.1", "sideEffects": false,
  "peerDependencies": { "@angular/common": "^20.3.0", "@angular/core": "^20.3.0",
    "@angular/forms": "^20.3.0", "@angular/router": "^20.3.0",
    "@ngx-translate/core": "^17.0.0", "rxjs": "^7.8.0" },
  "dependencies": { "moment": "^2.30.1", "tslib": "^2.3.0" } }
```

**`projects/seller-core/tsconfig.lib.json`** — inherits the loose root flags deliberately:
```jsonc
{ "extends": "../../tsconfig.json",
  "compilerOptions": { "outDir": "../../out-tsc/lib", "declaration": true,
                       "declarationMap": true, "inlineSources": true, "types": [] },
  "exclude": ["**/*.spec.ts"], "include": ["**/*.ts"] }
```
`tsconfig.lib.prod.json` adds `angularCompilerOptions.compilationMode: "partial"`.
`tsconfig.spec.json` adds `"types": ["jasmine"]`, `"include": ["**/*.spec.ts","**/*.d.ts"]`.
Note `types: []` is load-bearing: it keeps the app's `@types/node` out of the lib, so any lib file
touching `process`/`Buffer` fails loudly.

**`angular.json`** — new project alongside `seller-ui`:
```jsonc
"seller-core": {
  "projectType": "library", "root": "projects/seller-core",
  "sourceRoot": "projects/seller-core/src", "prefix": "sc",
  "architect": {
    "build": { "builder": "@angular-devkit/build-angular:ng-packagr",
      "options": { "project": "projects/seller-core/ng-package.json" },
      "configurations": {
        "production":  { "tsConfig": "projects/seller-core/tsconfig.lib.prod.json" },
        "development": { "tsConfig": "projects/seller-core/tsconfig.lib.json" } },
      "defaultConfiguration": "production" },
    "test": { "builder": "@angular-devkit/build-angular:karma",
      "options": { "tsConfig": "projects/seller-core/tsconfig.spec.json",
                   "polyfills": ["zone.js", "zone.js/testing"] } } } }
```
Extend the **existing** `seller-ui` lint target rather than adding a second:
`"lintFilePatterns": ["src/**/*.ts","src/**/*.html","projects/**/*.ts","projects/**/*.html"]`.
`eslint.config.js`'s `notification.service.ts` path exemption is unaffected — that file stays put.

**Root `tsconfig.json`** — add `"baseUrl": "./"` and:
```jsonc
"paths": { "seller-core": ["dist/seller-core"], "seller-core/*": ["dist/seller-core/*"] }
```
*During Steps 3–8 only*, point at source: `["projects/seller-core/src/public-api.ts"]` /
`["projects/seller-core/*/src/public-api.ts"]`. Flip in Step 10.

**`package.json`** — add devDependency `ng-packagr@^20.0.0` (**not currently installed**; the only
lockfile hits are unresolved optional-peer declarations, so `npm i -D` genuinely fetches it), and:
```jsonc
"build": "ng build seller-core && ng build seller-ui",
"build:lib": "ng build seller-core",
"watch:lib": "ng build seller-core --watch --configuration development",
"start": "ng build seller-core && ng serve",
"test": "ng test seller-core --watch=false && ng test seller-ui --watch=false",
"dev": "ng serve"
```
The Gradle side needs **zero** changes — `com.asrevo.ui-conventions.gradle` only ever calls
`npm run build`. (Separately: that plugin registers `bootRun → npm_run_dev` but no `dev` script
exists, so `./gradlew :store-core:seller-ui:bootRun` is broken today. The `"dev"` line above is a
one-line drive-by fix.)

## The library's public API

`lib/config/seller-core.config.ts`
```ts
export type StoreMode = 'MARKETPLACE' | 'BTB' | 'STANDARD';
export interface SellerCoreConfig {
  apiUrl: string; loginUrl: string; logoutUrl: string;
  mode: StoreMode; defaultStore: string;
  languages: { default: string; available: readonly string[] };
}
export const SELLER_CORE_CONFIG = new InjectionToken<SellerCoreConfig>('SELLER_CORE_CONFIG');
```

`lib/errors/notification.port.ts`
```ts
export interface NotificationPort { danger(m: string): void; success(m: string): void; info(m: string): void; }
export const NOTIFICATION_PORT = new InjectionToken<NotificationPort>('NOTIFICATION_PORT');
```

`lib/config/provide-seller-core.ts` — Angular's feature-provider idiom:
```ts
export function provideSellerCore(config: SellerCoreConfig,
                                  ...features: SellerCoreFeature[]): EnvironmentProviders {
  return makeEnvironmentProviders([
    { provide: SELLER_CORE_CONFIG, useValue: config },
    ...features.flatMap(f => f.providers),
  ]);
}
export function withNotifications(impl: Type<NotificationPort>): SellerCoreFeature { … }
export function withRequestContext(impl: Type<RequestContextProvider>): SellerCoreFeature { … }
```

App side, `src/app/app.config.ts`:
```ts
provideSellerCore({
  apiUrl: environment.apiUrl, loginUrl: environment.LOGIN_URL, logoutUrl: environment.LOGOUT_URL,
  mode: environment.mode as StoreMode, defaultStore: environment.defaultStore,
  languages: { default: environment.client.language.default,
               available: environment.client.language.array },
}, withNotifications(NotificationService)),
```
`environment.shippingApi` and `production` are unused by the moved set — leave them out.
`CrudService` swaps `url = environment.apiUrl` for `inject(SELLER_CORE_CONFIG).apiUrl`; likewise
`SessionService` and `ConfigService`.

## Steps

Branch from an up-to-date `develop`. **Checkpoint C = `npm run build && npm run lint && npm test`.**
Commit at every checkpoint.

**Step 0 — baseline.** Run Checkpoint C on untouched `HEAD` and record it. You must know which lint
warnings and test failures are pre-existing before you can read the ones you cause.

**Step 1 — scaffold an empty library.** `npm i -D ng-packagr@^20.0.0`, then **hand-author** the files
above — do *not* run `ng generate library`; its schematic rewrites `angular.json`, `tsconfig.json` and
`package.json` in ways you then have to undo, and scaffolds a component/module you don't want. Point
`paths` at source. Add `provideSellerCore({...})` to `app.config.ts` with no features yet.
`ng build seller-core` must succeed on a near-empty barrel. → Checkpoint C.

**Step 2 — write the mover.** A Node script in the scratchpad taking a `{oldPath → newPath}` table:
`git mv` each file (preserves history — reviewers need it), then walk every `.ts` under `src/` and
`projects/` and rewrite import specifiers. **Resolve specifiers to absolute paths via
`path.resolve(dirname(file), spec)` — never match on basename**; `store.service`, `subscription.service`
and `types.ts` each exist twice. Both-moved-into-same-entry-point → new relative path; otherwise → the
entry point specifier. Merge duplicate `from 'seller-core'` lines. Handle `export … from`,
`import type`, and dynamic `import()` in route files. Dry-run and eyeball the diff first.

**Step 3 — core tier A: leaves.** `shared/models/*`, `shared/utils/slugifying.ts`,
`shared/validation/validators.ts`, `shared/table/*`. Do the `StoreService → ManagerStoreService` rename
here, before Step 5 moves it. ~40 importers. → Checkpoint C.

**Step 4 — core tier B: errors + platform.** All of `core/errors/` except `i18n-errors.spec.ts`. Add
`NOTIFICATION_PORT`; `ApiErrorService` swaps `inject(NotificationService)` for it; app adds
`withNotifications(NotificationService)`. `SessionService.LOGIN_URL` → `config.loginUrl`. Move the
co-located specs (`api-error.interceptor`, `api-error.service`, `form-error.utils`,
`problem-detail.parser`) and confirm `ng test seller-core` picks them up. The `ERRORS.*` i18n keys now
live in app assets while the code lives in the lib — document that contract in the lib README; the
parity spec stays in the app and still guards it. → Checkpoint C.

**Step 5 — core tier C: services + storage hardening.** `crud`, `auth`, `user`, `config`, `store`,
`selected-store`, `selected-language`, `auth-guard`. Add `BrowserStorage`, route all three
`localStorage` users through it, and move `SelectedLanguageService`'s constructor body into a lazy
`ensureInit()`. Highest-risk step — `CrudService` is on every request path. → Checkpoint C.

**Steps 6–8 — feature slices, one entry point per commit,** Checkpoint C after each, in dependency
order: `stores` (most depended-on) → `catalog` → `orders` (adds `moment`; expect an ng-packagr CJS
warning, and confirm the app build's `allowedCommonJsDependencies` still covers it) → `customers`,
`content`, `payments`, `orgs`, `subscriptions`, `analytics`, `signup`.
Cross-entry-point imports are legal (`seller-core/orders` → `seller-core/catalog`); a **cycle** between
two secondary entry points is a hard ng-packagr error — hoist the shared type into the primary barrel.

**Step 9 — decouple `CrudService`.** `RequestContextProvider { params(explicitStore?): Record<string,string> }`
+ `REQUEST_CONTEXT` token; default implementation wraps `SelectedStoreService` and emits exactly today's
`store`/`pod` params. Add `withRequestContext(...)`. → Checkpoint C, plus a network-tab diff against a
pre-refactor recording: every request must carry identical query params.

**Step 10 — flip to a real package boundary.** `paths` → `dist/seller-core`; verify from a clean
`rm -rf dist out-tsc node_modules/.cache`. Expect IDE resolution to fail before the first lib build —
document "run `npm run build:lib` once after checkout" in the lib README. Anything that was silently
resolving through source and isn't exported from a `public-api.ts` fails here; that's the point.
→ Checkpoint C, then `./gradlew :store-core:seller-ui:clean :store-core:seller-ui:build`.

## Verification

```bash
rm -rf dist out-tsc && npm ci
npm run build          # ng build seller-core && ng build seller-ui
npm run lint
npm test               # both projects, --watch=false
./gradlew :store-core:seller-ui:clean :store-core:seller-ui:build
```

Boundary gates — each **must return nothing**:
```bash
grep -rn "src/app"                                  projects/seller-core
grep -rn "environments/environment"                 projects/seller-core
grep -rn "@nebular\|@swimlane\|ngx-echarts\|ngx-quill" projects/seller-core
```
Then: `ls dist/seller-core` shows `fesm2022/`, `index.d.ts`, and one folder per secondary entry point
each with its own `package.json`. Compare `dist/seller-ui` bundle sizes against the Step-0 baseline — a
large jump means an entry point pulled in something it shouldn't.

**End-to-end QA.** Pure refactor, so the bar is *identical* behaviour. Bring up the stack
(`configure-domain.sh` once, then `run-lcl.sh` in the background, `SIGTERM` to stop) and drive:

| Flow | Watches |
|---|---|
| Login via uaa → dashboard | `SessionService` / `loginUrl` from the token, `AuthGuard` |
| Switch store in header, reload | `SelectedStoreService` + `BrowserStorage`; **network tab: every request still carries `store=` and `pod=`** |
| Product list → paginate → sort → edit → save | `CrudService`, `TableStateService`, `seller-core/catalog` |
| Order list → order details | `order-details.mapper.ts` + `moment` through the packaged bundle |
| Switch language | `SelectedLanguageService` lazy init (constructor side-effect moved) |
| Force a 4xx (invalid product) | `ApiErrorService` → `NOTIFICATION_PORT` → `NotificationService`: toast copy and field errors **byte-identical**, not "System Error" |
| Force a 401 (clear session cookie mid-session) | one redirect, not N — the `SessionService` latch |
| SSR routes `/`, `/signup`, `/terms`, `/privacy-policy` via `npm run serve:ssr:seller-ui` | no `localStorage is not defined` from Node — the regression `BrowserStorage` prevents |

Tenant-isolation and permission checks are unchanged by this work (server-side), but repeat the product
edit as a second store to confirm the `store`/`pod` param plumbing survived Step 9.

## Follow-ups (separate PRs, deliberately out of scope)

1. Flip `strictNullChecks: true` in `tsconfig.lib.json` and fix the nullability the loose flags hide.
2. Migrate `uaa-fe` (`../../store-core/uaa/src/main/resources/uaa-fe`) onto the lib — it duplicates
   `auth.service`, `error.service`, `roles.ts` and four guards today. It uses `ngx-toastr`, which is
   exactly why `NOTIFICATION_PORT` is a port and not a Nebular-bound service.

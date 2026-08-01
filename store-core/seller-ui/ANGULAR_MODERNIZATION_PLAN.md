# Angular Modernization Audit & Migration Plan

Scope: `store-core/seller-ui/src`. This document is the platform/framework-level
counterpart to `ARCHITECTURE.md` (which already defines the target
Component/Facade/State/Service layering and is being adopted module by module).
This plan covers the **Angular version-level** modernization: standalone
components, new control-flow syntax, signal inputs/outputs, DI style, type
safety, HTTP, testing, and linting.

Audited: Angular 20.3.25, TypeScript 5.8, RxJS 7.8, Nebular UI (`@nebular/theme`),
zone-based change detection, SSR via `@angular/ssr`.

No hard deprecations block the current build — everything found is a
modernization/consistency gap, not a broken API. The codebase is **already
mid-migration** (see the payment module, in progress on the current branch),
using the correct target patterns in several places. This plan is about
finishing that migration consistently across all feature areas.

---

## 1. Audit Findings

### 1.1 Standalone vs NgModule (biggest gap)

| Metric | Count |
|---|---|
| `@NgModule` files | 40 |
| Components with `standalone: true` | 14 |
| Components with `standalone: false` (explicit) | rest of the ~112 components |
| Lazy routes via `loadChildren` (NgModule) | 19 |
| Lazy routes via `loadComponent` (standalone) | 0 |

Every feature area (`catalogue`, `content`, `customer`, `home`, `orders`,
`org-management`, `payment`, `pod-management`, `store-management`,
`subscription-and-usage`, `theme`, `user-management`, `public`) still has a
`*.module.ts` + `*-routing.module.ts` pair. `bootstrapApplication` in
`main.ts` is already standalone-based, so the app root is modern — the debt
is entirely inside feature modules.

**Facade adoption is uneven** (per `ARCHITECTURE.md`'s target pattern):
already has `facades/`: `customer`, `home`, `orders`, `payment`,
`pod-management`, `subscription-and-usage`, `user-management`,
`public/shared`. Still missing: `catalogue`, `content`, `org-management`,
`store-management`, `theme`.

### 1.2 Template control flow

- `*ngIf`: 45 files, `*ngFor`: 37 files, `@if`/`@for`: **0 files**.
- Angular provides an official schematic (`ng generate @angular/core:control-flow`)
  that mechanically converts these — low risk, high mechanical coverage.

### 1.3 Dependency injection

- `inject()`: 179 occurrences — already the dominant style.
- `constructor(...)` DI: 45 occurrences — remaining legacy, concentrated in
  older NgModule-declared components/services.
- Guards are already modern: `canAccessSecuredPages` is a `CanActivateFn`
  (functional guard) — no class-based `CanActivate` found. No class-based
  `HttpInterceptor` either, and **no interceptors registered at all** in
  `app.config.ts` — confirm this is intentional (cookie/session auth handled
  by the gateway) rather than a gap; if there is any per-service duplicated
  error/auth handling, a functional `HttpInterceptorFn` would centralize it.

### 1.4 Inputs / Outputs / Queries

- `@Input()`: 16 files, `input()` signal: 6 files.
- `@Output()`: 3 files, `output()` signal: 0 files.
- Skewed toward the old decorator style; Angular ships automated migration
  schematics for both (`signal-input-migration`, `output-migration`,
  `signal-queries-migration` for `@ViewChild`/`@ContentChild`).

### 1.5 RxJS / subscription hygiene

- `.subscribe(...)`: 196 occurrences.
- `| async` in templates: **0**.
- `takeUntilDestroyed`: 36 files (good — modern unsubscribe pattern already
  in use, no legacy `takeUntil(destroy$)` subject pattern found).
- The volume of manual `.subscribe()` with zero `async` pipe usage means a
  lot of state ends up manually assigned to component/facade fields. Where
  this is read-only display data, `toSignal()` or the `async` pipe would
  remove manual subscription bookkeeping. Where it's an intentional
  side-effecting action (dialogs, mutations, navigation — as in
  `PaymentListFacade.approve()`), manual `.subscribe()` inside a Facade is
  correct per `ARCHITECTURE.md` and should stay as-is.

### 1.6 Type safety

- `tsconfig.json` sets `"strict": true` but then **weakens it**:
  `"strictNullChecks": false`, `"noImplicitAny": false`, `"noImplicitReturns": false`.
  This largely neutralizes `strict` mode.
- 283 explicit `: any` annotations across the codebase (e.g. `row: any`,
  `event: any`, `filter(): any` seen in the payment facade/component).
- This is the single highest-risk gap: turning strictness back on will be
  disruptive, so it's sequenced last (Phase 6), after structural migration
  reduces churn.

### 1.7 HTTP layer

- `provideHttpClient(withFetch())` — modern, correct API (no deprecated
  `HttpClientModule`).
- No `HttpInterceptorFn`/interceptor chain registered — see 1.3.

### 1.8 Change detection

- `provideZoneChangeDetection({eventCoalescing: true})` — zone-based, not
  zoneless. Given the dependency on Nebular (`@nebular/theme`), which is not
  confirmed zoneless-compatible, **do not** attempt
  `provideZonelessChangeDetection()` as part of this plan — track separately
  once Nebular/zoneless compatibility is verified.

### 1.9 Testing

- Karma + Jasmine (`@angular-devkit/build-angular:karma` builder). Karma is
  community-deprecated upstream; Angular's newer builder option is
  Vitest/Web Test Runner, but Karma still fully works on v20 — not urgent.
- Only 25 `*.spec.ts` files for 112 components plus all facades/services —
  thin coverage. No e2e framework detected.

### 1.10 Linting

- `eslint` and `typescript-eslint` are devDependencies, but there is **no**
  ESLint config file (no `eslint.config.js`/`.mjs`), no `@angular-eslint`
  packages, and no `lint` script in `package.json`. Linting is effectively
  inert despite the dependencies being present — either a half-finished
  setup or dead weight.

### 1.11 Housekeeping to confirm/remove

- `@angular-architects/module-federation` is a dependency but no federation
  config file exists anywhere in the project — confirm whether it's actually
  used (e.g. from a parent workspace config) or safe to remove.
- Both `@ngx-translate` and `@angular/localize` are present — confirm both
  are actually needed (they are two different i18n systems); drop whichever
  is unused to cut bundle size and confusion.

---

## 2. Migration Plan (phased, ordered by risk/dependency)

Each phase is independently shippable. Phases 2–4 are mechanical
(codemod-driven) and can go module-by-module as separate PRs to keep blast
radius small and reviewable.

### Phase 0 — Foundations (~1 day)
1. Add a proper flat ESLint config (`eslint.config.js`) with
   `@angular-eslint` + `typescript-eslint`, add a `lint` script to
   `package.json`. Run once to get a baseline violation count — don't fix
   everything immediately, just stop the bleeding on new code.
2. Confirm/remove `@angular-architects/module-federation` if unused.
3. Confirm which of `@ngx-translate` / `@angular/localize` is the system of
   record; remove the other.

### Phase 1 — Finish the in-progress payment module (~0.5 day)
The current branch already refactored `payment` to facade + signals +
`inject()` + `takeUntilDestroyed`, but it's not finished:
1. Convert `PaymentComponent`, `PaymentListComponent`, `PaymentApproveComponent`
   to `standalone: true` with explicit `imports` (drop `SharedModule`,
   import only what's used: Nebular pieces, `CommonModule`/`NgIf`/`NgFor`
   or nothing once Phase 3 removes structural directives).
2. Convert `payment-routing.module.ts` to a plain `Routes` array using
   `loadComponent`; delete `payment.module.ts` once nothing declares
   through it.
3. Replace `*ngIf`/`*ngFor` in the 3 payment templates with `@if`/`@for`.
4. Replace `any` (`row: any`, `filter(): any`, `event: any` in
   `onPageChange`) with real types — a `PaymentRow` interface and the
   already-defined `PaymentFilterPageRequest`.

This becomes the reference implementation other modules are copied from.

### Phase 2 — Standalone conversion, module by module (~2–4 days per area)
Suggested order (lower risk/isolation first):
1. `theme` (shared shell — everything else depends on it; convert carefully,
   smoke-test the whole app after).
2. `content`, `catalogue`.
3. `org-management`, `store-management`, `customer`, `orders`,
   `pod-management`, `user-management`, `subscription-and-usage`, `home`.
4. `public` and `public/subscription` (marketing/sign-up funnel).

Per module:
- Run Angular's official codemods: `ng generate @angular/core:standalone`
  (bulk-converts components/directives/pipes to standalone), then
  `ng generate @angular/core:prune-ng-modules` to remove now-empty modules.
- Manually rewire `*-routing.module.ts` into plain `Routes` arrays with
  `loadComponent`; delete `*.module.ts`/`*-routing.module.ts` once unused.
- For modules without a `facades/` directory yet (`catalogue`, `content`,
  `org-management`, `store-management`, `theme`), extract one per
  `ARCHITECTURE.md` section 10's naming convention while doing the
  standalone conversion — the two changes touch the same files, so bundling
  them avoids a second pass.

### Phase 3 — Control-flow syntax (~1 day, repo-wide)
- Run `ng generate @angular/core:control-flow` across the whole repo in one
  pass (this is safe to do repo-wide, independent of the standalone
  conversion's progress).
- Manually review the diff for tricky cases: `*ngFor` with `trackBy`,
  `*ngIf; else` templates, and `<ng-container>` wrapping — the schematic
  handles most of these but edge cases are worth a human look.

### Phase 4 — Signal inputs/outputs/queries (~1–2 days, repo-wide)
- Run `ng generate @angular/core:signal-input-migration`,
  `...:output-migration`, and `...:signal-queries-migration`.
- Manually verify any `@Input()` + `@Output()` pair used as a two-way
  binding — these should become a single `model()` instead of two separate
  signals.

### Phase 5 — RxJS/subscription hygiene (ongoing, opportunistic)
- For read-only display data currently manually subscribed into a field,
  prefer `toSignal()` or the `async` pipe.
- Leave side-effecting `.subscribe()` calls inside Facades untouched — that
  is the correct, already-established pattern (`ARCHITECTURE.md` section 5).

### Phase 6 — Type-safety hardening (~3–5 days, do last)
- Flip `strictNullChecks: true` and `noImplicitAny: true` in
  `tsconfig.json` once structural migration has stabilized.
- Fix errors module by module — this will surface real bugs hiding behind
  the 283 `any` usages, so budget the most review time here.

### Phase 7 — Testing (ongoing)
- Prioritize specs for facades/services first (highest logic density,
  cheapest to test in isolation) to raise coverage before touching
  component/template tests.
- Karma→Vitest builder migration is optional, not urgent — revisit once
  structural work is done.

### Phase 8 — Zoneless (future, not scoped here)
- Do not attempt `provideZonelessChangeDetection()` until Nebular
  compatibility with zoneless change detection is explicitly verified.

---

## 3. Rough Sequencing

| Phase | Effort | Blocking? |
|---|---|---|
| 0 — Foundations | ~1 day | No, but unblocks lint feedback for everything after |
| 1 — Finish payment module | ~0.5 day | No |
| 2 — Standalone, per module | ~2–4 days × 13 areas | Do incrementally per PR |
| 3 — Control flow | ~1 day | Independent, can run anytime |
| 4 — Signal inputs/outputs | ~1–2 days | Best after Phase 2 per module |
| 5 — RxJS hygiene | Ongoing | No |
| 6 — Strict typing | ~3–5 days | Do last — most disruptive |
| 7 — Testing | Ongoing | No |
| 8 — Zoneless | Not scoped | Future, needs Nebular verification |

No code changes have been made as part of producing this document.

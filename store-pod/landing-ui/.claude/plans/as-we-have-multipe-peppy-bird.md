# Extract `@cvhome/ui-kit` — a stack-neutral Angular UI library

## Context

`store-core/console-ui/src/app/shared/` has grown into a genuine design system: 53 components,
~15.7k LOC, hand-built with **no third-party UI kit and no `@angular/cdk` import at all** — its only
runtime dependencies are `@angular/*` and `rxjs`, with `echarts` confined to the five files in
`ui/charts/`. A new Angular app is planned,
and `uaa-fe` / `cua` are slated for rewrites later. Today that library is trapped inside one
application, so the new app would start by copying it — which is exactly what happened once before.

**The precedent that makes this worth doing now.** Commit `d9e5111d6` ported seller-ui's HTTP tier
into console-ui *by copy rather than by dependency*, and said why: console-ui keeps `strict: true`
and Transloco, and could not link against a library built with `strictNullChecks: false` and
ngx-translate. That mismatch is the whole problem. A library that hard-depends on Transloco repeats
it; a library with a **translation port** does not — which is why "fully stack-neutral" is the right
call here rather than gold-plating.

**Outcome.** `@cvhome/ui-kit` — an Angular 20 library with no opinion about the consumer's i18n
stack, no hardcoded palette, a documented token contract, and a real public API. Consumed locally by
console-ui via an npm workspace, and *publishable-ready* (ng-packagr) so shipping it outside the
monorepo later is a config change, not a re-architecture.

**Out of scope:** rewriting `uaa-fe` or `cua`. They are future consumers; this work only removes the
barrier that would block them.

## What the evidence says

Measured, not assumed:

| Fact | Value |
|---|---|
| Source size | 136 non-spec files (~15.7k LOC) + 28 specs |
| Components | 53 selectors + 1 directive (`img[appImageBroken]`) |
| Call sites to rewrite | 742 `@shared/*` import occurrences across 129 files; ~956 `<app-*>` template usages |
| Transloco API actually used | **two members**: `translate(key, params)` (49×) and `activeLang()` (16×), plus 11 `*transloco="let t"` blocks |
| CSS token contract | 83 distinct `var(--…)`, plus global `.popover`, `.sr-only`, `.{primary,secondary,ghost,danger,icon}-action` |
| Coupling out of `shared/` | ~30 import sites across 9 `@core/*` and 10 `@models/*` modules |
| Already clean | no `@api/*`, `@features/*`, `@layouts/*`, `@env/*`, no `HttpClient`, no hardcoded URLs |
| Runtime deps | `@angular/*` + `rxjs` only; `echarts` isolated to `ui/charts/` (5 files); no `@angular/cdk` |

Two findings materially de-risk the ambitious option:

- **The i18n port is small.** Only `translate` + `activeLang` are used. The abstraction is a
  two-method interface, not a re-architecture of 25 files.
- **The theme is already consumer-supplied.** `src/app/core/theme/theme.provider.ts` reads tokens off
  the live document via `getComputedStyle`, with a `providedIn:'root'` fallback factory. The
  stylesheet is already the single source of truth and the library never hardcodes a palette. It
  moves almost verbatim.

## Repo constraints the plan respects

- **No repo-root `package.json`, no `.npmrc`, no registry, no publish pipeline.** Local consumption
  only for now. `store-pod/landing-ui` (`workspaces: ["storefront","libs/*","themes/*"]`) is the
  working in-repo precedent to mirror.
- **Gradle orchestrates.** `settings.gradle` includes `store-core:console-ui` and
  `store-pod:landing-ui`. `build-logic/.../com.asrevo.ui-conventions.gradle` pins Node 23.8.0, wires
  `build.dependsOn('npm_run_build')`, and **deletes `package-lock.json` on `clean`**.
- **Lockfiles untracked** (`**/package-lock.json` in root `.gitignore`) — nothing is pinned today.
- **CI runs no JS at all.** All six `.github/workflows` are Gradle + Checkstyle + JVM tests. `ng test`,
  eslint and stylelint have never run in CI.
- **Selector prefix.** `eslint.config.js` pins `app-`; a library must not squat the consumer's prefix.

---

## Design

### 1. Layout and wiring

Create an npm workspace root at **`store-core/ui-kit/`**, mirroring landing-ui's proven shape:

```
store-core/ui-kit/
  package.json            workspaces: ["lib", "sandbox"]
  angular.json            two projects: ui-kit (library), sandbox (application)
  tsconfig.json           strict: true, strictTemplates — must match console-ui or linking fails
  build.gradle            ui-conventions plugin, so Gradle builds it
  lib/
    package.json          name @cvhome/ui-kit, ng-packagr config, peerDeps only
    ng-package.json
    src/…                 the extracted code
    styles/               base.css, tokens.contract.css, theme-default.css
    i18n/                 en.json, ar.json  (the 21 generic namespaces)
  sandbox/                a throwaway consumer proving stack-neutrality (see §9)
```

**Why a separate workspace rather than a `projects/` folder inside console-ui:** console-ui's
`angular.json` is an SSR *application* with `newProjectRoot: "projects"` unused. Putting a library
inside it would make the library's lifecycle hostage to the app's, and the future Angular app would
depend on console-ui — the exact inversion we are trying to remove.

**Local consumption:** add `store-core/ui-kit` to `settings.gradle`, and have console-ui depend on
the built output by relative `file:` path. Because `ui-conventions.gradle` deletes `package-lock.json`
on `clean` and lockfiles are gitignored, the dependency must be resolvable from a cold clone —
a `file:../ui-kit/dist/ui-kit` path plus a Gradle `dependsOn` ordering does that without a registry.

> **Deferrable:** `ng-packagr` + `lib/package.json` are configured now but nothing publishes. That
> costs ~an hour and is what keeps "reuse outside, maybe later" a config change.

### 2. Public API surface

**Secondary entry points**, not one barrel — a single barrel drags all 53 components plus echarts
into every consumer's initial bundle (console-ui's budget is 1MB). Note that `jspdf` and
`modern-screenshot` are *not* in `shared/` at all today — they live in `@core`'s `PdfExportService`,
which is why inverting it (§5) keeps them out of the library entirely rather than merely isolating
them.

```
@cvhome/ui-kit            core primitives: icon, badge, panel, page-header, notice-bar, …
@cvhome/ui-kit/forms      form-field, text-field, select, checkbox, toggle, validators, form helpers
@cvhome/ui-kit/data       data-table, pagination, tree, kpi-*, ranked-list
@cvhome/ui-kit/overlays   confirm-dialog, image-preview, video-dialog, toast
@cvhome/ui-kit/charts     echarts-backed — isolates the echarts peer dep
@cvhome/ui-kit/export     export-button — isolates jspdf + modern-screenshot
@cvhome/ui-kit/i18n       the translation port + the shipped en/ar copy
@cvhome/ui-kit/transloco  the Transloco adapter (console-ui's one-liner)
@cvhome/ui-kit/testing    spec harnesses, replacing @testing/transloco-testing
```

**Selector prefix `ck-`** (`<ck-select>`, `[ckImageBroken]`). The 53-name list is exhaustive and
unambiguous, so the rename is deterministic.

**Call-site rewrite is one codemod**, run once, doing both jobs together: rewrite the 742 imports to
the new entry points, and rename the 53 selectors across ~956 template usages — matching only the
known list so feature-owned `app-*` components are untouched. Script it under
`store-core/ui-kit/scripts/`, commit the script, and verify with `npm run build` (AOT catches every
missed selector as an unknown element).

### 3. The translation port

The measured surface is two members, so:

```ts
// @cvhome/ui-kit/i18n
export abstract class UiKitTranslator {
  /** Synchronous — templates and computed() call this directly. */
  abstract translate(key: string, params?: Record<string, unknown>): string;
  /** Read inside computed() so every label re-resolves on a language change. */
  abstract readonly activeLang: Signal<string>;
}
```

- Ships a **structural directive** `*ckT="let t"` so the 11 `*transloco` blocks become library-owned
  and no template imports Transloco.
- **Transloco adapter** in `@cvhome/ui-kit/transloco`: `provideUiKitTransloco()` — console-ui adds one
  line. An ngx-translate adapter is ~20 lines and is what makes the uaa/cua rewrites viable later.
- **Default fallback implementation** ships the library's own `en`/`ar` for the 21 generic namespaces,
  so a consumer with no i18n stack at all still renders real text. This is what makes it genuinely
  stack-neutral rather than "Transloco with extra steps".
- **Missing-key behaviour is the consumer's, not the library's.** Transloco is configured to *throw*;
  the port must not. The adapter resolves through the host stack first and falls back to the shipped
  dictionary, so a consumer that has not copied a key gets English rather than a blank page.

**Lint interaction:** `scripts/i18n-unused.mjs` scans `src/app` against `src/locale/en.json`. The 21
generic namespaces must be deleted from console-ui's `en.json`/`ar.json` **in the same commit** that
moves the components, or `lint:i18n` reports them dead. The 5 console-specific ones
(`storeStatus`, `provisioningState`, `subscriptionStatus`, `allowance`, `entitlement`) stay.

### 4. The token contract

You asked for consumer-supplied tokens; components read 83 of them, so "supplied" must mean
*contracted*, not *assumed*. Three files:

- **`styles/tokens.contract.css`** — every one of the 83 tokens declared with a documented fallback.
  This is the spec, and it is what a consumer is checked against.
- **`styles/base.css`** — the global vocabulary components depend on but cannot scope: `.popover`,
  `.sr-only`, `.{primary,secondary,ghost,danger,icon}-action`, the `rise` keyframe, the
  `@custom-variant rtl`, and the `prefers-reduced-motion` block. Imported by the consumer, so it stays
  overridable by ordinary cascade order.
- **`styles/theme-default.css`** — one working palette, so the sandbox and any new app render on day
  one. Consumers may ignore it entirely.

`[data-theme]` selection stays the consumer's business — console-ui keeps Forest/Midnight/Daylight in
`src/styles/`, unchanged. **Verification:** a script in the library resolves every contract token
against a booted consumer and fails on any that computes empty. That converts an 83-token implicit
dependency into a check.

> **Watch:** `theme-forest.css` also claims bare `:root` and must stay first in console-ui's import
> order. Moving files near it risks a silent theme regression — keep `src/styles.css` import order
> byte-identical.

### 5. Port inversions and moves

| Item | Decision | Why |
|---|---|---|
| `theme.provider.ts` (`THEME`, `provideTheme`) | **Move into lib** | Already reads tokens from the document; zero console knowledge. Modernize deprecated `APP_INITIALIZER` → `provideAppInitializer`. |
| `clipboard.ts` (`copyText`) | **Move** | Pure, dependency-free, carries hard-won insecure-origin + modal-dialog fixes. |
| `calendar.ts` (~12 fns, 158 LOC) | **Move** | Pure, no deps. |
| `notification.port.ts` | **Move** | 12 lines, already a port. |
| `form-error.utils.ts` + `ProblemFieldError` | **Move** | RFC 7807 is a wire standard, not console domain. |
| `Tone`, `IconName`, `KpiDatum` | **Move** | Presentational vocabulary; `ARCHITECTURE.md` already records that these belong to the UI tier. |
| `PHONE_MIN_DIGITS`, `PHONE_PATTERN` | **Inline into lib** | Two constants. |
| `ReferenceOption` | **Inline** | Two-field interface, type-only. |
| `PdfExportService` (415 LOC, jspdf + modern-screenshot) | **Invert to a token** | Moving it drags two heavy deps into the library. `export-button` injects `PDF_EXPORT_PORT`; console-ui provides the concrete service. Keeps `@cvhome/ui-kit/export` optional. |

### 6. What stays in console-ui

Domain-bound, stays as an app-level `src/app/shared/`: `i18n/` (money, status-label, role-label,
total-label, platform-label — all import console wire enums), `billing/`, `auth/console-permissions.ts`
(injects `AuthService`, encodes uaa roles), `ui/user-admin-table/` (imports `PlatformUserRow`,
reaches `platform.users.*`), and `styles/product-picker.css`.

`styles/field.css` is a judgement call: it is imported by *features* via relative path
(`'../../../shared/styles/field.css'`) and defines page layout (`.page-body`, `.split`), not
component styling. **Keep it in console-ui** — it is app chrome, and moving it breaks ~dozens of
relative `styleUrls` for no reuse benefit.

`set-password-dialog` and `roles-dialog` are dependency-free but uaa-shaped in their semantics. Ship
them in the library — the future app and the uaa rewrite are exactly their audience.

### 7. Testing and CI

- 28 specs move with their components. The 11 importing `@testing/transloco-testing` switch to
  `@cvhome/ui-kit/testing`, which provides a **stub translator** returning the key — simpler than the
  Transloco testing module and stack-neutral by construction.
- **CI gap is real and must be closed as part of this.** Add one `.github/workflows` Node job running,
  for the library and console-ui: `npm ci` → `npm run build` → `npm run lint` → `npm run test:ci`.
  Without it the library ships unverified, and the 742-site codemod is exactly the change you do not
  want landing unchecked. This is a genuine prerequisite, not scope creep.

### 8. Sequencing

Console-ui stays green (`npm run build` **and** `npm run lint` **and** `npm run test:ci`) at every step.

1. **Scaffold** `store-core/ui-kit` — workspace, ng-packagr, `build.gradle`, `settings.gradle` entry.
   Nothing consumes it. Add the CI Node job here, so every later step is checked.
2. **Move the leaf dependencies** into the lib: theme provider, clipboard, calendar, notification
   port, form-error utils, the three UI types, phone constants. Console-ui re-exports them from their
   old `@core/*` paths as thin shims — **zero call sites change yet.**
3. **Build the i18n port** + Transloco adapter + shipped dictionaries + `*ckT`. Console-ui adopts
   `provideUiKitTransloco()` while still using its own keys. Still zero component moves.
4. **Move the generic components** in entry-point batches (core → forms → data → overlays → charts →
   export), running the build after each batch.
5. **The codemod** — imports + selectors, one commit. **This is the riskiest step.** De-risk it by:
   running it only against the exhaustive 53-name list; relying on AOT (`npm run build`) to catch
   every unknown element; and keeping it a single mechanical commit with no behaviour change, so it
   reviews as a diff of one shape and reverts cleanly.
6. **Delete the moved i18n keys** from console-ui's locale files, same commit as step 5's namespace cut.
7. **Drop the shims** from step 2 and delete the emptied `@core/*` files.
8. **Sandbox app** proving stack-neutrality.

### 9. Verification

- `npm run build` in `store-core/ui-kit` produces `dist/ui-kit` with all nine entry points.
- Console-ui: `npm run build` (AOT — catches every missed selector and template type error),
  `npm run lint` (eslint + stylelint + `lint:i18n` + `lint:i18n-missing` + `lint:lessons`),
  `npm run test:ci`. All three must pass; the i18n gates are the ones that will catch a half-done
  namespace move.
- `./gradlew :store-core:console-ui:build` from a **clean** tree, proving the `file:` dependency
  resolves after `clean` deleted the lockfile.
- **Token contract check** — the script from §4 reports zero empty tokens against console-ui.
- **The console-ui QA matrix, unchanged:** every page in **EN and AR**, in **Forest, Midnight and
  Daylight**, at **1440 / 900 / 420**. This migration touches every control on every page, so the
  full matrix is the real gate — a themed `<ck-select>` that lost its token is invisible to the
  compiler. Drive it against the local stack with `lcl restart console-ui` (never `kill`).
- **Stack-neutrality proof:** the `sandbox` app boots with **no Transloco at all** — the default
  translator plus its own hand-written tokens — and renders a page of controls. If that works, the
  uaa and cua rewrites have a library they can actually adopt.

## Honest assessment

The library half of this is well-founded: the code is already clean (no HTTP, no env, no upward
imports), the ports are small, and the theme is already externalized. The two things that make it a
*large* change rather than a moving job are the 742-site codemod and the 83-token contract — both
mechanical, both checkable.

What I would flag: **there is exactly one consumer today.** The value depends entirely on the planned
Angular app actually arriving. If it slips, the safe fallback is to stop after step 4 — the code is
extracted, ported and green, but console-ui still imports it by path alias. That leaves the boundary
hardened and the codemod deferred, with nothing painted into a corner.

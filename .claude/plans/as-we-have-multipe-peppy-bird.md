# Make console-ui's `shared/` extraction-ready, and scaffold `@cvhome/ui-kit`

## Context

`store-core/console-ui/src/app/shared/` has grown into a genuine design system: 53 components,
~15.7k LOC, hand-built with **no third-party UI kit and no `@angular/cdk` import at all** — its only
runtime dependencies are `@angular/*` and `rxjs`, with `echarts` confined to the five files in
`ui/charts/`. A new Angular app is planned, and `uaa` and `cua` will be rewritten onto Angular later.
Today that design system is trapped inside one application, so the next app would start by copying it.

**The precedent that makes this worth doing.** Commit `d9e5111d6` ported seller-ui's HTTP tier into
console-ui *by copy rather than by dependency*, and said why: console-ui keeps `strict: true` and
Transloco, and could not link against a library built with `strictNullChecks: false` and
ngx-translate. That mismatch is the whole problem. A library that hard-depends on Transloco repeats
it; a library with a **translation port** does not.

**Scope decision.** There is exactly one Angular consumer today. So this plan deliberately stops
short of moving code: it **hardens the boundary in place** and **scaffolds the library**, proving the
packaging works without paying for a 742-site codemod that only earns its keep when the second app
actually starts.

**Outcome of this scope:**
- `src/app/shared/` has zero imports leaving it, a stack-neutral translation port, and an explicit
  83-token CSS contract — i.e. it is *extraction-ready*.
- `store-commons/ui-kit` exists as a real, building, publishable-ready Angular library project with
  ng-packagr and Gradle wiring — but empty of components.
- console-ui still imports everything through its existing `@shared/*` alias. **No call site, no
  selector and no template changes.**

**Explicitly out of scope:** moving the components into the library, the import/selector codemod,
and rewriting `uaa` or `cua`. Those are the follow-up, unblocked by this work.

**Not applicable:** `store-pod/landing-ui` is Next.js/React on shadcn and will stay that way. It is
not a consumer, now or later.

## What the evidence says

Measured, not assumed:

| Fact | Value |
|---|---|
| Source size | 136 non-spec files (~15.7k LOC) + 28 specs |
| Components | 53 selectors + 1 directive (`img[appImageBroken]`) |
| Transloco API actually used | **two members**: `translate(key, params)` (49×) and `activeLang()` (16×), plus 11 `*transloco="let t"` blocks |
| CSS token contract | 83 distinct `var(--…)`, plus global `.popover`, `.sr-only`, `.{primary,secondary,ghost,danger,icon}-action` |
| Coupling out of `shared/` | ~30 import sites across 9 `@core/*` and 10 `@models/*` modules |
| Already clean | no `@api/*`, `@features/*`, `@layouts/*`, `@env/*`, no `HttpClient`, no hardcoded URLs |
| Deferred (not in this scope) | 742 `@shared/*` import occurrences across 129 files; ~956 `<app-*>` template usages |

Two findings make the stack-neutral goal cheap rather than ambitious:

- **The i18n port is small.** Only `translate` + `activeLang` are used. A two-method interface covers it.
- **The theme is already consumer-supplied.** `src/app/core/theme/theme.provider.ts` reads tokens off
  the live document via `getComputedStyle`, with a `providedIn:'root'` fallback factory. The
  stylesheet is already the single source of truth; the library never hardcodes a palette.

## Repo constraints this respects

- **No repo-root `package.json`, no `.npmrc`, no registry, no publish pipeline.**
- **Gradle orchestrates.** `settings.gradle` lists every module explicitly;
  `build-logic/.../com.asrevo.ui-conventions.gradle` pins Node 23.8.0, wires
  `build.dependsOn('npm_run_build')`, and **deletes `package-lock.json` on `clean`**.
- **Lockfiles untracked** (`**/package-lock.json` in root `.gitignore`).
- **CI runs no JavaScript at all.** All six `.github/workflows` are Gradle + Checkstyle + JVM tests.
- **Custom lint gates** that must stay green: `lint:i18n` (`scripts/i18n-unused.mjs`, which scans
  `src/app` against `src/locale/en.json`), `lint:i18n-missing`, `lint:lessons`, `lint:css`.

---

## Part A — Scaffold `store-commons/ui-kit`

`store-commons` is the monorepo's declared home for shared libraries and sits above both `store-core`
and `store-pod`, so `console-ui` and `uaa` (store-core) and `cua` (store-pod) can all consume it later
without an upward dependency. It is Java-only today; this is the first frontend library there.

```
store-commons/ui-kit/
  package.json       npm workspace root; workspaces: ["lib"]
  angular.json       one project: ui-kit (projectType: library)
  tsconfig.json      strict: true + strictTemplates — MUST match console-ui or linking fails later
  build.gradle       ui-conventions plugin
  lib/
    package.json     name @cvhome/ui-kit, peerDependencies only
    ng-package.json  ng-packagr entry point config
    src/public-api.ts
```

- Add `'store-commons:ui-kit'` to `settings.gradle` alongside the existing `store-commons:*` entries.
- **Entry points are declared now, empty now** — secondary entry points must exist before code lands,
  because retrofitting them later is what forces a second codemod:
  `@cvhome/ui-kit` (core), `/forms`, `/data`, `/overlays`, `/charts`, `/export`, `/i18n`,
  `/transloco`, `/testing`.
- **Selector prefix `ck-`** is fixed in the library's eslint config now, so nothing lands under `app-`.
- `ng-packagr` is configured and `npm run build` produces `dist/ui-kit`. Nothing publishes; that stays
  a config change for later.

**Why scaffold rather than defer entirely:** the packaging questions (strict-mode parity, entry-point
layout, Gradle + Node ordering, the `clean` deleting lockfiles) are the ones that are expensive to
discover late. Answering them against an empty library costs little and de-risks the real move.

## Part B — Close the boundary in `shared/`

Move these into `src/app/shared/` (they are already dependency-free), and leave thin re-export shims
at their old `@core/*` paths so **no existing call site changes**:

| Item | Move | Why |
|---|---|---|
| `core/theme/theme.provider.ts` (`THEME`, `provideTheme`) | → `shared/theme/` | Reads tokens from the document; zero console knowledge. Modernize deprecated `APP_INITIALIZER` → `provideAppInitializer`. |
| `core/platform/clipboard.ts` | → `shared/platform/` | Pure; carries hard-won insecure-origin + modal-dialog fixes. |
| `core/i18n/calendar.ts` (~12 fns, 158 LOC) | → `shared/i18n/` | Pure, no deps. |
| `core/errors/notification.port.ts` | → `shared/ports/` | 12 lines, already a port. |
| `core/errors/form-error.utils.ts` + `ProblemFieldError` | → `shared/forms/` | RFC 7807 is a wire standard, not console domain. |
| `Tone`, `IconName`, `KpiDatum` from `@models/ui` | → `shared/ui/` | Presentational vocabulary. `ARCHITECTURE.md` already records that these belong to the UI tier. |
| `PHONE_MIN_DIGITS`, `PHONE_PATTERN` | inline into `shared/validators/phone-number.ts` | Two constants. |
| `ReferenceOption` | inline into `locale-switcher` | Two-field, type-only. |

**One inversion rather than a move:** `PdfExportService` (415 LOC, pulls `jspdf` +
`modern-screenshot`). `export-button` gets a `PDF_EXPORT_PORT` token; console-ui provides the concrete
service. This keeps two heavy dependencies out of the library permanently — note they are *not* in
`shared/` today at all, so inverting preserves that rather than merely isolating it.

**Enforce it.** Extend the existing `no-restricted-imports` block in `eslint.config.js` (which already
bans `@api/*`, `@features/*`, `@layouts/*` from `shared/`) to also ban `@core/*` and `@models/*`.
That is the gate that keeps the boundary closed after this work — without it, it reopens in a month.

**What stays behind**, as console-owned app code — these genuinely import console wire enums:
`shared/i18n/{money,status-label,role-label,total-label,platform-label}.ts`, `shared/billing/`,
`shared/auth/console-permissions.ts` (injects `AuthService`, encodes uaa roles),
`shared/ui/user-admin-table/` (imports `PlatformUserRow`, reaches `platform.users.*`), and
`shared/styles/product-picker.css`. Relocate them to a sibling `src/app/console/` tier so the eslint
ban above can apply cleanly to what remains.

`shared/styles/field.css` **stays in console-ui**: it defines page layout (`.page-body`, `.split`),
is imported by *features* via relative `styleUrls`, and is app chrome rather than component styling.

## Part C — The translation port

The measured surface is two members, so:

```ts
// shared/i18n/translator.ts   (later: @cvhome/ui-kit/i18n)
export abstract class UiKitTranslator {
  /** Synchronous — templates and computed() call this directly. */
  abstract translate(key: string, params?: Record<string, unknown>): string;
  /** Read inside computed() so labels re-resolve on a language change. */
  abstract readonly activeLang: Signal<string>;
}
```

- A structural directive `*ckT="let t"` replaces the 11 `*transloco="let t"` blocks, so no shared
  template imports Transloco.
- **Transloco adapter** — `provideUiKitTransloco()`; console-ui adds one line to `app.config.ts` and
  behaves exactly as before. An ngx-translate adapter is ~20 lines and is precisely what makes the
  later uaa/cua rewrites viable.
- **Default fallback translator** ships the 21 generic `shared.*` namespaces as bundled `en`/`ar`, so
  a consumer with no i18n stack still renders real text. This is what makes it genuinely
  stack-neutral rather than "Transloco with extra steps".
- **Missing-key behaviour is the consumer's.** Transloco is configured to *throw*; the port must not.
  The adapter resolves through the host stack first, then falls back to the bundled dictionary.

**Deliberately deferred:** console-ui's `src/locale/{en,ar}.json` keep their `shared.*` keys for now.
Deleting them belongs with the component move, because `scripts/i18n-unused.mjs` scans `src/app`
against `en.json` — cutting keys while the components still live in `src/app` would fail `lint:i18n`.

## Part D — The token contract

Components read 83 CSS custom properties that are currently an undocumented, unchecked dependency.
Write the contract now, while the code is still in one place:

- **`ui-kit/lib/styles/tokens.contract.css`** — all 83 tokens declared with documented fallbacks. The spec.
- **`ui-kit/lib/styles/base.css`** — the global vocabulary components need but cannot scope:
  `.popover`, `.sr-only`, `.{primary,secondary,ghost,danger,icon}-action`, the `rise` keyframe, the
  `@custom-variant rtl`, and the `prefers-reduced-motion` block. Consumer-imported, so ordinary
  cascade order keeps it overridable.
- **`ui-kit/lib/styles/theme-default.css`** — one working palette so a new app renders on day one.

`[data-theme]` selection stays the consumer's business; console-ui keeps Forest/Midnight/Daylight in
`src/styles/` untouched.

**Verification script** — resolve every contract token against a booted console-ui and fail on any
that computes empty. This converts an implicit 83-token dependency into a check, and it is the single
highest-value artifact in this scope: it is what will make the eventual component move safe.

> **Watch:** `theme-forest.css` also claims bare `:root` and must stay first in `src/styles.css`'s
> import order. Keep that order byte-identical.

## Part E — CI

CI runs no JavaScript today, so none of console-ui's build, lint or test gates are enforced on any PR.
Add one `.github/workflows` Node job running, for `store-commons/ui-kit` and `store-core/console-ui`:
`npm ci` → `npm run build` → `npm run lint` → `npm run test:ci`.

This is a genuine prerequisite rather than scope creep: everything in Parts B–D is a refactor of code
that nothing in CI currently checks, and it is the gate the deferred codemod will depend on entirely.

## Sequencing

Console-ui stays green (`npm run build` **and** `npm run lint` **and** `npm run test:ci`) at each step.

1. **CI Node job** (Part E) — first, so every later step is actually checked.
2. **Scaffold `store-commons/ui-kit`** (Part A) — builds, empty, nothing consumes it.
3. **Move leaf dependencies down with shims** (Part B) — no call site changes.
4. **Relocate console-specific code** to `src/app/console/`, then **tighten the eslint block** to ban
   `@core/*` and `@models/*` from `shared/`. This step *proves* the boundary is closed.
5. **Translation port + Transloco adapter + `*ckT`** (Part C); console-ui adopts
   `provideUiKitTransloco()` keeping its own keys.
6. **`PDF_EXPORT_PORT` inversion** for `export-button`.
7. **Token contract + verification script** (Part D).
8. **Delete the step-3 shims** and the emptied `@core/*` files.

## Verification

- `npm run build` in `store-commons/ui-kit` produces `dist/ui-kit` with all nine entry points declared.
- console-ui: `npm run build` (AOT — the only place template type errors surface), `npm run lint`
  (eslint + stylelint + `lint:i18n` + `lint:i18n-missing` + `lint:lessons`), `npm run test:ci`.
- `./gradlew :store-core:console-ui:build` and `:store-commons:ui-kit:build` from a **clean** tree,
  proving Node/Gradle ordering survives `clean` deleting the lockfiles.
- **Boundary check:** `grep -rE "from '@(core|models|api|features|layouts)/" src/app/shared` returns
  nothing, and eslint fails if it ever does again.
- **Token contract check:** the Part D script reports zero empty tokens against console-ui.
- **QA matrix.** Parts C and D touch every label and every themed surface, so run the console-ui
  standard: every page in **EN and AR**, in **Forest, Midnight and Daylight**, at **1440 / 900 / 420**.
  A label that silently fell back, or a token that resolved empty, is invisible to the compiler.
  Drive it against the local stack with `lcl restart console-ui` (never `kill`).

## What this leaves for later

The component move and the codemod — 742 imports across 129 files plus ~956 `<app-*>` usages renamed
to `ck-*`. After this scope that becomes a mechanical, fully-gated change: the boundary is closed and
eslint-enforced, the i18n and PDF ports exist, the token contract is checkable, and CI runs the build.
Do it when the new Angular app is actually starting — that is the point where it pays for itself.

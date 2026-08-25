# First-run mode: force the admin to create their first store

## Context

Every page in `console-ui` is store-scoped — `SelectedStoreRequestContext.params()` stamps
`?store=&pod=` onto every request, and the dashboard, orders and store-management pages are all
readings of one store. An admin signing in for the first time has none, so today they land on
`/dashboard` and see fabricated KPIs, a nav rail of sections that cannot work, and a store switcher
with an empty list and no explanation.

`store-core/console-template/First Run with Nav.dc.html` is the designed answer: a getting-started
page that is the *only* thing a store-less admin can reach, with the navigation rail visibly
disabled and one live action — create a store. This plan implements that mockup as a real mode, not
a static page: the 0-store condition drives a route guard, the guard drives the rail's disabled
state, and finishing create-store ends the mode.

Decisions already settled with the user:

| | |
|---|---|
| Location | Its own route `/getting-started` + `features/first-run/`, not a branch inside dashboard |
| 0-store test | `StoreDirectory.stores.length === 0` from the existing `ConsoleApi`, plus a mock toggle |
| Trial gate | **Kept** — Create store stays disabled until the 14-day trial is started, per the mockup |
| Content | All sections built, fixture-backed through a facade, exactly like `dashboard` |

The mockup's hardcoded light palette (`#eff3f8`, `#fff`, `#0f172a`, `#10b981`) is **not** ported.
Per `DESIGN.md`'s Token Rule a literal hex in a stylesheet is a defect, and the app has three themes
(Forest/Midnight/Daylight). Every value comes from the semantic tokens — `--card`, `--border`,
`--muted`, `--primary`, `--track`, `--chart-N-wash`, `--radius-*`, `--text-*`.

---

## 1. Make an empty store directory representable

Three places assume at least one store exists and must be loosened first.

- **`src/app/models/console.ts`** — `StoreDirectory.defaultStoreId` and `currentStoreId` become
  `string | null`. They are non-nullable `string` today, which alone blocks an empty directory.
- **`src/app/layouts/console-shell/services/console.api.service.ts`** — `loadStores()` ends with
  `?? this.stores[0].id`, which throws on an empty list. Guard it (`?? null`).
- **`src/app/core/store-context/selected-store.service.ts`** — `currentSelectedStore()` falls back to
  `STORES[0].id`; same guard. Note this file holds a **second, disjoint** store list from
  `CONSOLE_STORES` (kept in sync by id only, per the comment in `mocks/console.fixture.ts`). Both
  have to be empty together or the request context keeps stamping a phantom store.

**The mock toggle.** Add `src/app/core/store-context/first-run-mock.ts`: a tiny root service reading
a `?firstRun=1` query param once and persisting it to `BrowserStorage` under
`cvhome.console.mock.firstRun` (same key style as `cvhome.console.store`). `SelectedStoreService`
and `ConsoleApi` both start from an empty list when it is set. It lives in `core/` so both the core
service and the layout service can use it without a layering violation — `ConsoleApi` may import
`@mocks/*`, but `SelectedStoreService` may not.

**`ConsoleShellFacade`** (`layouts/console-shell/facades/console-shell.facade.ts`) already owns
`stores()` and `storesLoading()`; add the one derived fact everything else reads:

```ts
readonly firstRun = computed(() => !this.storesLoading() && this.stores().length === 0);
```

## 2. The guards

New `src/app/core/store-context/first-run.guard.ts`, two `CanActivateFn`s. Copy the
`router.createUrlTree(...)` idiom from the existing (currently unwired) `canAccessSecuredPages` in
`core/auth/auth-guard.service.ts` — these will be the first guards actually wired into the route
table. Each injects `ConsoleApi` and maps `loadStores()` to a decision, so the answer is settled
before the shell renders.

- `requiresStore` — 0 stores ⇒ `createUrlTree(['/getting-started'])`. Applied to `dashboard`,
  `orders`, and `store-management/:section`.
- `firstRunOnly` — ≥1 store ⇒ `createUrlTree(['/dashboard'])`. Applied to `getting-started`.

**`store-management/create` deliberately gets neither** — it is the only exit from first run.

## 3. The `first-run` feature

New `src/app/features/first-run/` following the `dashboard` layering exactly
(`first-run.ts` / `.html` / `.css` / `.spec.ts`, `facades/first-run.facade.ts`,
`services/first-run.api.service.ts`), plus `src/app/models/first-run.ts` and
`src/app/mocks/first-run.fixture.ts`.

Model — key-based like every other model here, so copy stays in the dictionaries:

```ts
export interface SetupStep   { id; labelKey; metaKey; }
export interface GuideVideo  { id; titleKey; durationKey; sectionKey; }
export interface NextUpCard  { id; titleKey; copyKey; icon: IconName; }
export interface PlanLimit   { id; labelKey; used; cap; pct; noteKey; noteParams?; }
export interface FirstRunSnapshot { steps; guides; nextUp; limits; trialDays; feature{titleKey,copyKey,durationKey}; }
```

`FirstRunApi.loadSnapshot()` returns `of(FIRST_RUN_*).pipe(delay(latency))` — the same shape as
`services/dashboard.api.service.ts`.

The template follows the invariant console page skeleton every other page uses: an outer
`<ng-container *transloco="let t">`, `app-page-header`, the `.load-error` block with a retry, then
`app-busy-overlay` wrapping `@if (isEmpty()) { .first-load } @else { … }`, with
`:host {display: contents}` and `.first-load {block-size: 60vh}` in the CSS. Control flow is
`@if`/`@for (… ; track …)`/`@let` only; translation happens in the facade (each derived signal reads
`transloco.activeLang()` first) so models carry `labelKey`, never display text.

`FirstRunFacade` holds the trial state: a `trialStarted` signal persisted via `BrowserStorage`
(`cvhome.console.trialStarted`) so a reload does not silently un-start it, and `startTrial()`.
Derived: `trialPending`, `heroTitle/heroCopy/heroMeta` keys, `createDisabled`, `progressPct`, and
`steps` with the first marked active and the rest `locked`.

**Section → existing component mapping.** Two of the mockup's hardest pieces already exist,
tokenised and animated, in `features/create-store/` — port them rather than rebuilding:

| Mockup section | Built from |
|---|---|
| Amber/green trial bar | `app-notice-bar` (`icon="clock"` tone amber → `icon="sparkles"` tone green), Start-trial button projected as content |
| Page title | `app-page-header` |
| Hero card | Page CSS `.hero` + `app-badge` for the "Step 1 of 4" pill |
| Setup progress (% + bar + numbered steps + locks) | **Copy `.progress-card` / `.progress-head` / `.progress-pct` / `.task-list` / `.task-dot` from `create-store.{html,css}`** — already `app-progress-track` + `li.done`/`li.active`; swap the dot glyph for `lock` on locked steps |
| Video card | Page CSS `.feature-video`; chrome borrowed from `.done-banner` in `create-store.css` |
| More short guides | `app-action-list` (icon tile → label/detail → `chevronRight`, emits `itemSelect`) |
| What comes after | Three cards using the locked idiom: `.region-card.locked {cursor:not-allowed; opacity:.6}` + `app-badge` + `lock` icon |
| Your plan (4 meters) | **Copy `.limit-card` / `.limit-track` / `.limit-note` from `create-store.{html,css}`** |
| Book a call strip | Page CSS `.help`, `messageCircle` icon |

`app-progress-track` fills with **`currentColor`**, so each meter sets its own (`.limit-track {color: var(--chart-4-foreground)}`).

Buttons follow the console convention, **not** `app-button` (the 48px marketing pill, unused in any
feature): local `.primary-action` / `.secondary-action` classes, already duplicated verbatim across
`create-store.css:28-75`, `orders.css` and `store-management.css`. They carry the `:disabled`
treatment the trial gate needs.

**One new icon.** `play` must be added to `src/app/shared/ui/icon/icon-paths.ts` — it is the only one
of the mockup's 18 glyphs missing from the 89-name registry.

Entrance animation: declare page-local keyframes (`section-in`, matching `panel-in` in
`dashboard.css` and `kpi-rise`), which is the house norm — the global `rise`/`fade` in `styles.css`
are not what feature pages use.

**Lint constraint:** `eslint.config.js` runs `@angular-eslint/template/i18n` with `checkText: true`
and `checkAttributes: true`, so no literal string may appear in the template. If a new enum-ish
string input is introduced, add its name to that rule's `ignoreAttributes` allowlist (which already
lists `tone, icon, shape, fileName, panelId, data-label`).

## 4. Disable the rail

- **`console-sidebar.ts`** — the template already splits routed items (`<a routerLink>`) from
  routeless ones (inert `<button>`); extend the condition to `@if (item.route && !shell.firstRun())`
  and set `[attr.aria-disabled]` on the button branch. That reuses the existing "leads nowhere"
  mechanism rather than inventing a second one.
- **`console-sidebar.css`** — add the disabled visual (none exists today):
  `.nav-item[aria-disabled='true'] { color: var(--foreground-quiet); pointer-events: none; }`, and
  dim the group labels while `firstRun()`. Follow the "disabled item with a reason" idiom from
  `components/settings-nav/settings-nav.ts` — a `[title]` explaining *why*, not a dead link.
- **`store-switcher.ts`** — add the empty state the mockup shows (a dashed "No store yet" row) when
  `!shell.storesLoading() && !shell.stores().length`; today you get a bare empty `<ul>`. The
  `Create store` link stays enabled — it is the exit.
- **`console-shell.ts`** — the mockup's trial bar occupies the slot `app-plan-banner` sits in, so
  render the banner only when `shell.bannerVisible() && !shell.firstRun()`; the page owns the notice
  during first run.

## 5. Close the loop: creating the store must end first-run

Today `CreateStoreFacade.start()` only runs a fake `interval(420ms)` provisioning timer — it never
registers the store anywhere, so without this step the admin would create a store and still be
bounced back to `/getting-started`.

- `ConsoleApi.addStore(name)` — push onto its mutable `stores`, set it as current + default.
- `SelectedStoreService` gains the matching entry so `?store=&pod=` resolves (the two lists again).
- `CreateStoreFacade` calls both on completion, then `ConsoleShellFacade.selectStore(newId)`.
- `create-store.html` opens with `<a routerLink="/store-management">Cancel</a>` — a page the admin
  cannot reach in first run. Hide it while `firstRun()`.
- `CreateStoreFacade.storesUsed` is the static `CONSOLE_STORES.length`; read it from `ConsoleApi` so
  the limit card says 0 of 1 during first run.

## 6. i18n, routing, SSR

- **`src/locale/en.json` and `ar.json`** — add a `firstRun.*` namespace, plus `route.firstRun.title`,
  `shell.breadcrumb.firstRun` and `shell.store.noStoreYet`. `StrictMissingHandler` **throws in dev
  mode** and there is no English fallback (`useFallbackTranslation: false`), so a key absent from
  `ar.json` breaks the Arabic page — write both files, always. The two dictionaries are currently
  exactly in sync at 687 leaf keys; verify with `npm run i18n:missing`.
- **`app.routes.ts`** — a new top-level `getting-started` path mounting `ConsoleShell` with a
  `FirstRun` child and `data: {titleKey, breadcrumbKey}` (every console page must supply both), plus
  the `canActivate` wiring from §2.
- **`app.routes.server.ts`** — `{path: 'getting-started', renderMode: RenderMode.Client}`, matching
  the other store-scoped routes.

---

## Critical files

- New: `features/first-run/**`, `models/first-run.ts`, `mocks/first-run.fixture.ts`,
  `core/store-context/first-run.guard.ts`, `core/store-context/first-run-mock.ts`
- Changed: `models/console.ts`, `layouts/console-shell/services/console.api.service.ts`,
  `layouts/console-shell/facades/console-shell.facade.ts`, `layouts/console-shell/console-shell.ts`,
  `layouts/console-shell/components/{console-sidebar,store-switcher}/*`,
  `core/store-context/selected-store.service.ts`, `features/create-store/**`,
  `shared/ui/icon/icon-paths.ts`, `app.routes.ts`, `app.routes.server.ts`, `locale/{en,ar}.json`

## Verification

1. `npm run build` (warning-free; watch the `anyComponentStyle` budget), `npm run lint`,
   `npm run i18n:missing`.
2. `npm test` — new `first-run.spec.ts` (renders every section; trial gate disables/enables Create
   store; no console chrome in the page), a guard spec (0 stores redirects `/dashboard` →
   `/getting-started`; ≥1 store redirects the other way), and an assertion in `console-shell.spec.ts`
   that the rail is disabled during first run. Use `@testing/transloco-testing` and the
   `fakeAsync` + `tick(500)` pattern from `store-switcher.spec.ts` (250 ms fake latency).
3. `npm start`, then walk the flow in the browser:
   - `http://localhost:4200/getting-started?firstRun=1` — page renders; rail items are visibly
     disabled and not clickable; store switcher shows "No store yet".
   - `/dashboard`, `/orders`, `/store-management/branding` all bounce to `/getting-started`.
   - Create store is disabled until **Start 14-day trial**; the notice flips amber → green.
   - `/store-management/create` is reachable, has no Cancel link, and on completion the rail
     re-enables, the switcher shows the new store, and `/getting-started` now bounces to
     `/dashboard`.
4. Screenshot-compare against `store-core/console-template/First Run with Nav.dc.html` at 1440px and
   420px, and re-check in all three themes — Daylight is where a stray hardcoded colour shows up.
5. Keyboard-only pass: tab through the rail (disabled items must not receive focus) and the hero
   actions.

# console-ui: from three flat pages to a real project

## Context

`../../store-core/console-ui` is the next-generation admin console. It currently holds three page
components — `marketing`, `auth`, `dashboard` — built directly from the mockups in
`../../store-core/console-template`. They render correctly but there is no project underneath them:

- **No architecture.** No `core/`, `shared/`, `features/`, `layouts/`, no path aliases, no services,
  guards, interceptors, models or environments. All data is hardcoded in component classes.
- **A global stylesheet doing page work.** `src/styles.scss` is 184 lines, ~80% of it marketing-page
  CSS plus bare-element rules (`h2`, `form`, `input`, `textarea`) that auth and dashboard actively
  fight with overrides. This already caused two real bugs this week (the console search box inherited
  the dark marketing input styling; hover states lost specificity ties).
- **Tailwind installed but unused.** v4 runs and ships Preflight, but zero utilities are intentional
  and `DESIGN.md`'s complete two-world token set exists nowhere in CSS.
- **`dashboard.scss` is 509 lines / 22,293 B** — already past the 22 kB `anyComponentStyle` budget
  warning, 1.7 kB from the hard error.
- **A language menu that cannot translate anything**, no forms validation, no error handling, three
  different icon approaches, and one scaffold spec as the entire test suite.

The outcome: a console that new feature pages (orders, inventory, users, content…) can be dropped
into by following a proven pattern, with the three existing pages refactored onto that pattern as the
reference implementation.

## Decisions (settled with the user)

| Area | Decision |
|---|---|
| Component library | **Angular CDK + our own components.** No PrimeNG/Material/Nebular. |
| Charts | **No library** — hand-written SVG components (the donut and bars already are). |
| i18n | **Angular built-in `$localize`**, locales `en` (source), `ar`, `fr`, `de`. |
| Data layer | **Typed models + API interfaces + facades, mock-backed.** No live HTTP yet. |
| Styling | **Tailwind v4 CSS-first**, `DESIGN.md` tokens via `@theme`. |
| Scope | Architecture + refactor the three existing pages. No new feature pages. |
| Sign-in | **Honest OAuth handoff** — drop the decorative credential form and dead social buttons. |
| Deferred | Zoneless change detection; Karma → Vitest. Both are isolated follow-ups. |

Conventions come from `../../store-core/seller-ui/ARCHITECTURE.md` (the authoritative Angular doc in this
monorepo): components are dumb renderers; layers are `.component.ts` / `.facade.ts` /
`.api.service.ts` / `.state.ts` / `.form.service.ts` / `.mapper.ts` / `.validators.ts`; components
never inject `HttpClient`/`FormBuilder`/`Router`, never `.subscribe()`, never extend base classes;
signals + plain services, no NgRx.

## Reuse, don't reinvent

`../../store-core/seller-ui/projects/seller-core/src/lib` already models the exact backend contracts, with
specs. Copy these into `core/` rather than rewriting:

- `errors/problem-detail.model.ts`, `problem-detail.parser.ts`, `api-error.ts`,
  `api-error.interceptor.ts`, `global-error-handler.ts` — RFC-7807 with the `{code, category, params,
  fieldErrors[], traceId}` extensions. Rule preserved: **`detail` is never rendered.**
- `errors/form-error.utils.ts` — binds server `fieldErrors[]` onto controls, including stripping the
  jakarta method prefix (`createPod.pod.name` → `pod.name`). Copy verbatim; do not reinvent.
- `table/table.types.ts` (`PageRequest {page, count}`, `PageT`, `SpringPage`, `EMPTY_PAGE`) and
  `table/table-state.service.ts` — the backend uses `count`, not `size`, and returns two different
  page envelopes (pod `ReadableList` vs store-core Spring `Page`).
- `http/crud.service.ts`, `http/request-context.ts` (`?store=&pod=`; **throws during SSR** by design),
  `platform/browser-storage.ts`, `auth/` (`GET /api/v1/auth/me` answers **200 with an empty body**
  when signed out, not 401).

## Target structure

```
src/
  styles.css                      ← renamed from .scss; tokens + base + keyframes only
  environments/environment{,.development}.ts
  app/
    core/                         ← singletons. Imports models/ only.
      config/  http/  errors/  auth/  i18n/  store-context/  layout/  table/  testing/
    shared/                       ← dumb UI. Imports core/ types only.
      ui/  directives/  pipes/  validators/
    layouts/
      marketing-shell/  auth-shell/  console-shell/
        console-shell/components/{console-sidebar,console-toolbar,store-switcher,plan-banner}/
    features/
      marketing/  auth/  dashboard/  not-found/
        each: components/  facades/  services/  state/  constants/
    models/                       ← wire DTOs, one file per bounded context
    mocks/                        ← fixtures + provide-mock-api.ts; never imported by a component
```

Path aliases declared **once** in `tsconfig.json` (children `extend` it, so no duplication):
`@core/* @shared/* @layouts/* @features/* @models/* @mocks/* @env/*`.
Dependency direction, lint-enforced: `features → layouts → shared → core → models`.

## Design tokens (Tailwind v4)

Rename `src/styles.scss` → `src/styles.css`. `@use 'tailwindcss'` currently works only because Sass
passes it through; v4's `@theme` / `@theme inline` / `@custom-variant` are at-rules Sass can mangle.

Two layers, and the distinction is load-bearing:

- **Primitives in `@theme`** — one literal per `DESIGN.md` name (`--color-forest-canvas`,
  `--color-emerald-primary`, `--color-slate-*`, `--radius-*`, the 14 `--text-*` roles).
- **Semantics in `@theme inline`** — `--color-canvas: var(--surface-canvas)`. `inline` makes Tailwind
  emit `var(--surface-canvas)` *in the utility*, so it resolves at the element and a `[data-world]`
  ancestor can rebind it. A plain `@theme` computes once on `:root` and would silently not work.

```css
@custom-variant ops (&:where([data-world='operations'], [data-world='operations'] *));
@custom-variant rtl (&:where([dir='rtl'], [dir='rtl'] *));

[data-world='forest']     { --surface-canvas: var(--color-forest-canvas);  --ink: #fff; … }
[data-world='operations'] { --surface-canvas: var(--color-ops-canvas);     --ink: var(--color-slate-950); … }
```

The layout root carries the attribute; nothing else does. Every shared component then writes
`bg-accent text-accent-on` and is emerald-on-forest in marketing and emerald-on-white in the console
with **zero conditional code**. That is the payoff.

What legitimately stays as component CSS: the seven `@keyframes` (moved to `styles.css`), the donut's
`rotate(-90deg)` + `stroke-dashoffset`, the bar chart's `transform-origin` + stagger delays, the
progress track's grow origin, and the console shell's `--banner-h` coupling. Everything else —
`.card .pill .badge .kpi .panel .toolbar .sidebar .popover`, all three `@media` blocks — becomes
utilities. The five tone classes become a `Badge` component with a `tone` input and a `TONE_CLASSES`
record, not 5×2 CSS rules. The 22 kB budget problem disappears structurally.

## Shared UI inventory

All signal `input()`/`output()`, `OnPush`, `host: { class: … }`, injecting nothing except the two
CDK-backed ones. Build in dependency order:

`Icon` → `Button`, `Badge`, `Card`, `Avatar`, `Skeleton`, `EmptyState` → `FormField`, `FieldError`,
`TextField`, `PasswordField`, `Checkbox` → `Tabs`, `ProgressTrack`, `StatCard`, `DonutChart`,
`BarChart` → `Menu`, `MenuItem`, `Select` (CDK `Overlay` + `cdkTrapFocus` + `LiveAnnouncer`) →
`DataTable`, `Paginator` (emits `{page, count}`, never `size`) → `DatePicker`, `DateRangeField` (CDK
Overlay + our calendar) → `Toast`/`ToastHost`.

`StatCard` is the exemplar to write first and review as the pattern for the rest.

A chart library becomes necessary only for a time axis with tick collision, tooltips tracking a
nearest point, zoom/brush, multi-series with a shared scale, or >200 points. None of that is in these
pages; a library would be 150–600 kB to draw two shapes.

## Icons — one inline-SVG registry

Replace both current approaches (primeicons font in the dashboard, hand-inlined SVG paths in
marketing/auth) with a typed `ICON_PATHS` registry rendered by `<app-icon>`, and **drop the
`primeicons` dependency**. Rationale: the font ships five formats for ~250 glyphs when we use ~43,
it is render-blocking with an icon-shaped FOUT on the hero, glyphs cannot be tree-shaken, and
`<i class="pi pi-x">` gives the a11y layer nothing. A sprite file is also wrong here because
`<use href>` resolves against the document base URL, which is `/ar/` under localized builds.

`<app-icon [name] [size] [flip] [label] />` — `flip` auto-mirrors directional icons in RTL so we
don't scatter `rtl:` classes; omitting `label` renders `aria-hidden`.

## Fonts

Self-host Inter via `@fontsource-variable/inter` (plus a Noto fallback for Arabic) and delete the
Google Fonts `<link>` from `index.html`. Removes a render-blocking third-party origin, works offline
in dev, and avoids a CSP exception.

## i18n and RTL

Verified against the installed `@angular/build` and `@angular/ssr`:

- `--localize` with `outputMode: server` produces **one** server entry (`server/server.mjs`, our
  `src/server.ts`, not localized) plus a per-locale app bundle (`server/ar/main.server.mjs`) and a
  per-locale browser dir (`browser/ar/`). **The Dockerfile does not change.**
- `AngularAppEngine` resolves the locale from the **first URL path segment** and lazily imports the
  right bundle. `src/server.ts` needs no locale routing — only a `Content-Language` +
  `Vary: Accept-Language` header pass. Hand-rolling detection produces double redirects.
- English gets `subPath: ''`, so `/`, `/sign-in`, `/dashboard` keep working and any unknown segment
  falls back to English.

Three real limitations, stated plainly:

1. **Runtime language switching is impossible.** Each locale is a separately-inlined bundle. The
   dashboard's `language` signal + `selectLanguage()` is architecturally dead. It becomes a
   `LocaleService.urlFor(code)` **`<a href>`** — a full document load to the same page in the other
   bundle.
2. **`ng serve` is single-locale** (force-disabled in the dev-server source). Add `dev:ar` etc. as
   separate configurations; RTL work means restarting the dev server.
3. **Translations must be literals.** Error messages become an extractable `$localize` dispatch table
   keyed by ProblemDetail `code` then `category`, every key with an explicit `@@id`.

Per `../../AGENTS.md`, a key must exist in **every** locale — enforce with `"i18nMissingTranslation":
"error"` on the production build so a missing `ar` entry fails the build.

RTL: `dir`/`lang` set on `<html>` from an app initializer (so SSR emits it with no flash), and a hard
review rule of **logical properties only** — `ps-*/pe-*`, `ms-*/me-*`, `start-*/end-*`,
`text-start/end`, `rounded-s-*/e-*`. The existing SCSS gets this wrong in ~15 places
(`margin-left: auto`, `right: .55rem` on the password toggle, …), all of which become `ms-auto` /
`end-2`.

## Forms, validation, animation, routing

- **Forms**: typed reactive forms behind a `.form.service.ts` per feature (components never inject
  `FormBuilder`). Shared `validators.ts` patterns, `FormField` + `FieldError`, and server
  `fieldErrors[]` bound through the copied `form-error.utils.ts`. Covers the marketing contact form
  and the sign-up form.
- **Animations**: native `animate.enter` / `animate.leave` + CSS keyframes. No `@angular/animations`.
  Durations from `.impeccable/design.json` (state 200–250 ms, entrance 600–700 ms) and a global
  `prefers-reduced-motion` block.
- **Routing**: three parent layout routes with lazy children, a `**` not-found,
  `withComponentInputBinding()`, and a `TitleStrategy` reading `$localize`'d titles. `auth`'s
  `data: { mode }` disappears — `sign-in` and `sign-up` become two real components sharing
  `AuthShell`.
- **`app.routes.server.ts` is load-bearing**: marketing/auth move `Server` → `Prerender` (static, and
  prerendering runs once per locale); **`dashboard/**` must stay `Client`** — every store-scoped
  request needs `?store=&pod=`, `request-context.ts` throws during SSR precisely so an unscoped
  request cannot query the wrong tenant, and the SSR process does not hold the gateway session cookie.

## Sequencing

Each step ends with a green `ng build` and a working `ng serve`.

| # | Step | Risk |
|---|---|---|
| 1 | Hygiene + tooling: delete `Archive.zip`, two `.DS_Store`, empty `app.scss`; add `paths`, `environments/`, ESLint + `prettier-plugin-tailwindcss`, schematics naming | mechanical |
| 2 | Fonts + icons: self-host Inter, add `Icon` + registry, replace ~43 icons, drop `primeicons` | mechanical |
| 3 | Token layer: `styles.css` with `@theme` / `@theme inline` / `[data-world]`, keeping existing rules below it temporarily | low |
| 4 | `core/` foundation copied from seller-core + wired into `app.config.ts` | low |
| 5 | `models/` + mock API layer + fixtures derived from the current hardcoded arrays | low |
| 6 | Layouts + routing split; move header/footer/sidebar/toolbar out of pages; render-mode change | **risky** |
| 7 | Shared UI components in dependency order (`npm i @angular/cdk@^20.3` first) | low–med |
| 8 | Refactor marketing onto shell + components + utilities; delete its slice of global CSS | medium |
| 9 | Refactor auth: split sign-in/sign-up; sign-in becomes the OAuth handoff; sign-up gets real DTO + form service + `fieldErrors` binding | **risky** |
| 10 | Refactor dashboard panel by panel (KPI → attention → customers → orders → products → toolbar → sidebar), 509 lines of SCSS → utilities + 3 small chart stylesheets | **risky** |
| 11 | i18n: `@angular/localize`, project `i18n` block, mark up every string, extract, `messages.{ar,fr,de}.xlf`, locale menu → `urlFor` anchors, `dir` initializer, logical-property sweep | **risky** |

## Verification

- After every step: `npm run build` (must stay warning-free, watch the `anyComponentStyle` budget).
- Steps 6/8/9/10: run `npm start` and screenshot-compare each page against
  `store-core/console-template/*.dc.html` at 1440px and 420px, plus keyboard-only passes over the
  menus, store switcher and forms.
- Step 6 SSR check: `npm run build && node dist/console-ui/server/server.mjs`, then `curl /`
  (prerendered HTML present) and `/dashboard` (CSR shell only).
- Step 11: confirm the built tree matches the documented per-locale layout, then serve and check `/`,
  `/ar/`, `/ar/dashboard`, `/fr/sign-up`, asserting `<html dir="rtl" lang="ar">` in the Arabic body.
- `ng test` grows real coverage from step 4 (the copied seller-core specs come along).

## Explicitly not in this pass

- **The gateway route.** `GatewayRouteLocatorImpl` matches console-ui by **host only**, with no
  backend-path negation, so `/tenancy/**` and `/spg/**` on `console-ui.<domain>` resolve to the
  console's own shell instead of the APIs. This is why the data layer is mock-backed. Fixing it is a
  change in `../../store-core/gateway/gateway-service` and needs its own review. Do not work around it with
  a cross-origin base URL — that breaks the session cookie.
- New feature pages (orders, inventory, users…). The sidebar keeps rendering non-routing buttons for
  them until the features exist.
- A charting library, NgRx, `@angular/animations`, zoneless, Vitest.

## Critical files

- `../../store-core/console-ui/angular.json` — i18n block, localize configs, styles/polyfills, budgets
- `../../store-core/console-ui/src/styles.scss` → `src/styles.css` — the token layer; 184 lines to delete
- `../../store-core/console-ui/src/app/pages/dashboard/dashboard.scss` — 509 lines, the mapping target
- `../../store-core/console-ui/src/app/app.config.ts`, `src/app/app.routes.server.ts` — providers + render modes
- `store-core/seller-ui/projects/seller-core/src/lib/{errors,table,http,auth}/**` — copy sources
- `../../store-core/seller-ui/ARCHITECTURE.md` — the layering rules being adopted

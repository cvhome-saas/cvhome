# Marketing page as the single design system

## Context

`../../store-core/console-ui` currently ships **two deliberate visual worlds**, encoded in `src/styles.css:45-59` and switched by a `data-world` host attribute on the three layout shells:

- `forest` — dark `#05140f` canvas, emerald signal palette. Used by marketing (`MarketingShell`) and auth (`AuthShell`).
- `operations` — light `#eff3f8`/white slate workspace. Used by the console (`ConsoleShell` → dashboard).

The user wants the **marketing page to be the one design system**, overriding the other pages. That retires the light `operations` world: the console, dashboard, charts and shared components all move onto the forest/emerald palette.

The second problem is that there is no design system *in code* today. `styles.css` defines a `@theme` block and `--surface-*`/`--ink`/`--accent` variables, but **nothing consumes them** — every feature stylesheet hardcodes hex values (`marketing.css` 249 lines, `dashboard.css` 530 lines, `auth.css` 60 lines), and no feature template uses a single Tailwind utility class. The tokens exist as decoration.

The outcome: a **TypeScript file is the single source of truth** for the design system. A prebuild step emits its tokens as a Tailwind v4 `@theme` block so utilities are generated statically (no SSR flash), and the same object is provided through an Angular `InjectionToken` so components can read raw values in TS. Every stylesheet then consumes tokens instead of hex literals.

### Decisions taken (confirmed with the user)

1. **One dark world everywhere.** Delete the `operations` world; repaint the console dark.
2. **Codegen + runtime provider.** `theme.ts` is authoritative → prebuild script emits `theme.generated.css` → Tailwind sees every token; `THEME` InjectionToken serves TS consumers.

### Important pre-existing findings

- **There are already three competing token sources**, none of which generates any other, and all of which have drifted:
  1. `DESIGN.md:1-172` — YAML frontmatter with the most complete set: 33 colors, **14 named typography roles**, 8 radii, 7 spacing steps, 9 component recipes using `{colors.x}` reference syntax.
  2. `.impeccable/design.json` — colour ramps, elevation, motion, breakpoints, narrative rules.
  3. `src/styles.css:8-35` — a hand-copied partial subset of (1).
  **This change must collapse them to one.** `theme.ts` should be transcribed from `DESIGN.md`'s frontmatter (the richest source), not reverse-engineered from `marketing.css`. See Step 8 for what happens to the other two.
- **~90% of `src/app/shared/ui/**` is dead code.** `Button`, `Card`, `Badge`, `StatCard`, `DataTable`, `Menu`, `Avatar`, `ProgressTrack`, `BarChart`, `DonutChart`, `FormField` have **zero importers**. Only `Icon`, `DateRangePicker`, `field-error` and `ToastHost` are live. Worse, `stat-card.ts:17` and `form-field.ts:5` reference `.metric-card` / `.form-field` classes that **no stylesheet in the repo defines** — they are not merely unused, they are broken. Do not gold-plate these — see Step 6e.
- **The world-switching mechanism is fully wired and fully unused.** Zero occurrences of the `ops:` variant in any template; zero uses of the `bg-canvas`/`bg-surface`/`text-ink`/`bg-accent` aliases at `styles.css:37-43`. Every stylesheet hardcodes its world's colours directly. Removing the `operations` world therefore breaks nothing that is currently rendering.
- `@fontsource-variable/noto-sans-arabic` is imported at `styles.css:2` but **no `font-family` rule ever references it**. Arabic renders in Inter's fallback today. The theme's font stack should fix this.
- `.impeccable/design.json` encodes a **"Two-World Rule"**, and `DESIGN.md:219-225` encodes both it and a *Dark Territory Rule* ("no isolated light cards in forest"). The Two-World Rule becomes false; the Dark Territory Rule becomes **more** load-bearing, since it is exactly the constraint the dashboard repaint must satisfy.
- `.impeccable/design.json` `extensions.source` paths are stale (`src/app/pages/…`, `src/styles.scss`); the tree is `src/app/features/…` with `.css`.
- `dashboard.css` uses **native CSS nesting** (`&:hover`) with no preprocessor. Preserve that style.
- `angular.json:63-67` sets a per-component-stylesheet budget: **warn 22kB, error 24kB**. `dashboard.css` (530 lines) is the file at risk; substituting `var(--long-token-name)` for 6-char hex literals *increases* file size. Check the budget after Step 6c and raise it in `angular.json` if it trips.
- `prettier-plugin-tailwindcss` is installed (`package.json:56`) but not listed in the `prettier` block (`:12-22`), so class sorting never runs. Out of scope; mention only if touching that config anyway.

---

## Step 1 — Author the theme (`src/app/core/theme/theme.ts`)

New directory `src/app/core/theme/` (sibling of `core/config`, `core/i18n`). Reachable via the existing `@core/*` alias in `tsconfig.json:6-15` — no new alias needed.

Export one deeply-typed `as const` object. **Transcribe it from `DESIGN.md:1-172`** — that frontmatter is already the most complete token set in the repo (33 colours, 14 typography roles, 8 radii, 7 spacing steps, 9 component recipes). Fill the gaps it lacks (motion, breakpoints, elevation names, alpha surfaces) from `.impeccable/design.json` `extensions.motion` / `breakpoints` / `elevation`. Use `marketing.css` only to resolve conflicts (see the drift table below).

```ts
export const theme = {
  color: {
    forest: { ink: '#04120d', canvas: '#05140f', surface: '#071a13' },
    emerald: { primary:'#10b981', bright:'#34d399', highlight:'#59e2ab', soft:'#6ee7b7', pale:'#8cf2c6' },
    text: { primary:'#fff', body:'#e6efeb', supporting:'#b2c1ba', icon:'#b6c5be', label:'#afc0b8',
            control:'#a7b8b0', helper:'#8fa39a', muted:'#98aca3', subtle:'#91a59c', quiet:'#82978e' },
    chart: { blue:'#3b82f6', cyan:'#06b6d4', amber:'#f59e0b', red:'#ef4444', violet:'#8b5cf6' },
    state: { danger: '#fca5a5' },            // from auth.css:14 field-error
  },
  // Alpha layers the dark world is built from — currently retyped as literals in every file
  surface: { hairline:'rgb(255 255 255 / 10%)', hairlineSoft:'rgb(255 255 255 / 7%)',
             fill:'rgb(255 255 255 / 2%)', fillRaised:'rgb(255 255 255 / 6%)',
             panel:'rgb(4 18 13 / 60%)', headerVeil:'rgb(5 20 15 / 82%)',
             accentWash:'rgb(16 185 129 / 6%)', accentEdge:'rgb(52 211 153 / 42%)' },
  radius: { xs:'4px', sm:'6px', md:'8px', lg:'12px', field:'10px', channel:'14px',
            card:'16px', store:'18px', plan:'20px', pill:'999px' },
  font: { sans: "'Inter Variable', 'Noto Sans Arabic Variable', ui-sans-serif, system-ui, sans-serif" },

  // All 14 roles from DESIGN.md:40-109, each { size, weight, lineHeight, tracking }
  text: { display:{…}, headline:{…}, title:{…}, body:{…}, label:{…},
          authHeadline:{…}, bodyProminent:{…}, bodyCompact:{…}, caption:{…}, micro:{…},
          operationsTitle:{…}, operationsBody:{…}, operationsLabel:{…} },

  // DESIGN.md:119-126 — deliberately 7 steps, not a dense scale
  space: { controlXs:'0.5rem', controlSm:'0.8rem', contentSm:'1rem', contentMd:'1.5rem',
           contentLg:'3rem', sectionMobile:'5rem', sectionDesktop:'7rem' },

  shadow: { actionLift:'0 12px 30px rgb(16 185 129 / 25%)', cardLift:'0 20px 45px rgb(0 0 0 / 20%)' },
  glow: { color:'rgb(16 185 129 / 27%)', stop:'68%', blur:'22px' },
  motion: { state:'200ms', entrance:'600ms', atmosphere:'14s', livePulse:'2.4s',
            ease:{ standard:'ease', emphasis:'cubic-bezier(.2,0,0,1)' } },
  breakpoint: { compact:'600px', authStack:'800px', nav:'900px', wide:'1100px',
                drawer:'760px', rail:'1180px' },
  layout: { frame:'1100px', railWidth:'280px', railCollapsed:'76px',
            formMeasure:'26rem', formMeasureWide:'32rem' },
} as const;

export type Theme = typeof theme;
```

**Radii note:** the compact radii (4/6/8/12px, formerly named `operations-*`) are kept as `xs/sm/md/lg` — the console stays visually denser than marketing even though both are now dark. Only the *palette and surface treatment* unify; sizing does not. Drop the `operations-` prefix from the names, since there is no longer a second world to distinguish from.

**Chart colours are retained.** Both `DESIGN.md` and `.impeccable/design.json` require semantic chart colours stay redundant with text/structure; they are not a light-world artifact.

### Resolving the documented drift

`DESIGN.md`'s spec and the shipped CSS disagree in eight places. **The user's instruction is that marketing is authoritative**, so where marketing's implementation differs from the spec, marketing wins and `DESIGN.md` gets corrected in Step 8 — except where the spec value is the more disciplined one and marketing's is an unreviewed one-off.

| Token | Spec (`DESIGN.md`) | Marketing | Take |
|---|---|---|---|
| Action Lift | `0 12px 30px …/25%` | `0 12px 34px …/40%` (`marketing.css:27`) | **Spec.** `auth.css:27` is a third value (`0 10px 28px`); 40% is the outlier. One token, all three call sites converge. |
| Atmospheric Glow | `27%` + `blur(22px)` | `28%` + `blur(20px)` (`:35`) | **Spec.** `auth-story.css:6` is a fourth value (`24%`/`20px`). Difference is imperceptible; pick one and stop the bleed. |
| Card radius | `card 16px` only | `18px` stores (`:78`), `20px` plans (`:140`) | **Marketing.** Add `store 18px` / `plan 20px` tokens — these are real, deliberate distinctions the spec simply never captured. |
| Focus ring | `3px` mint, offset `4px` | global `styles.css:69` ✅ | **Spec.** Delete the `dashboard.css:21` override (`2px`, offset `2px`). |
| `label` role | 700 / `0.7rem` / `0.1em` | `auth.css:12` 600/`.75rem`; dashboard 600-700/`.6875rem`/`.07em` | **Spec for `label`**, and keep `operationsLabel` (700/`.6875rem`/`.07em`) as the separate dense role it already is. |
| Button padding | `0.8rem 1.5rem`, min-height `48px` | `button.ts:18` `min-h-12 px-6`, no vertical token | **Spec.** Fix in Step 6e. |
| Badge colours | chart tokens | `badge.ts:5-13` stock Tailwind `blue-500`… | **Spec.** Step 6e. |
| Console drawer | `<760px` | `dashboard.css:488` uses `900px` | **Flag, do not change.** This is layout behaviour, not palette, and changing it risks a regression outside this change's remit. Note it and leave it. |

## Step 2 — Bridge to CSS (`src/app/core/theme/theme.css-vars.ts`)

A pure function `toCssVars(theme): Record<string,string>` flattening the object into CSS custom-property names, mapping each section onto the **Tailwind v4 namespace** that generates the utility we want:

| Theme section | CSS namespace | Utilities gained |
|---|---|---|
| `color.*`, `surface.*` | `--color-*` | `bg-forest-canvas`, `text-text-body`, `border-surface-hairline` |
| `radius.*` | `--radius-*` | `rounded-card`, `rounded-pill` |
| `font.sans` | `--font-*` | `font-sans` |
| `text.*` | `--text-*` (+ paired `--text-*--font-weight`, `--line-height`, `--letter-spacing`) | `text-headline` applies size **and** weight/leading/tracking in one utility |
| `space.*` | `--spacing-*` | `p-content-md`, `py-section-desktop` |
| `shadow.*` | `--shadow-*` | `shadow-action-lift` |
| `motion.ease.*` | `--ease-*` | `ease-emphasis` |
| `breakpoint.*` | `--breakpoint-*` | `nav:`, `compact:` variants |

The typography row is the one that needs care: Tailwind v4 supports `--text-<name>--font-weight` / `--line-height` / `--letter-spacing` companion properties, so each of the 14 roles from `DESIGN.md:40-109` becomes a **single** utility carrying all four values. That is what makes the ladder enforceable rather than advisory.

Keys are kebab-cased from the nested path (`color.forest.canvas` → `--color-forest-canvas`), which **preserves the token names already in `styles.css:9-34`** so nothing downstream breaks.

Shared by the codegen script and the runtime provider so both emit byte-identical output.

## Step 3 — Codegen (`scripts/generate-theme-css.mjs` → `src/theme.generated.css`)

Node script, run through the existing TS via `tsx`-free approach: keep `theme.ts` and `theme.css-vars.ts` free of Angular imports so the script can load them with Node's native TS type-stripping (Node 20 in `@types/node`; if unavailable in the pinned runtime, compile them with `tsc` to a temp dir in the same script).

Emits, with a `/* GENERATED — edit theme.ts */` header:

```css
@theme { --color-forest-canvas: #05140f; … --radius-card: 16px; … }
```

`package.json` scripts:

```json
"generate:theme": "node scripts/generate-theme-css.mjs",
"verify:theme":   "node scripts/generate-theme-css.mjs --check",
"prestart": "npm run generate:theme",
"prebuild": "npm run generate:theme"
```

`--check` regenerates in memory and diffs against the file on disk, exiting non-zero if stale — **this is the guard against the two sources drifting.** Wire it into `npm run lint` so CI catches a hand-edited generated file. Commit `theme.generated.css` (SSR builds and fresh clones must not depend on the script having run).

## Step 4 — Provide the theme to Angular (`src/app/core/theme/theme.provider.ts`)

```ts
export const THEME = new InjectionToken<Theme>('THEME');
export function provideTheme(overrides?: PartialDeep<Theme>): EnvironmentProviders
```

Returns `{provide: THEME, useValue: merged}`. Because Step 3 already baked the tokens into CSS, the provider does **not** need to write to the DOM on the normal path — that is what avoids the SSR flash. It only writes custom properties (via injected `DOCUMENT`, guarded so SSR is a no-op) when `overrides` are passed, leaving the door open for per-tenant theming later without reworking anything.

Registered in `src/app/app.config.ts` alongside the existing providers (after `provideHttpClient`, before the `APP_INITIALIZER` at :31).

Components read values in TS with `inject(THEME)` — the real consumers are the charts (`bar-chart`, `donut-chart` set SVG `stroke`) and anything computing an inline style.

## Step 5 — Collapse two worlds into one (`src/styles.css`, the three shells)

- Replace the hand-written `@theme` block (`styles.css:8-35`) with `@import './theme.generated.css';` placed directly after `@import 'tailwindcss';`. Tailwind's PostCSS plugin inlines it, so `@theme` still registers.
- **Delete** the `ops` custom variant (`:5`) and the `[data-world='operations']` block (`:53-59`).
- Promote `[data-world='forest']` (`:45-51`) to `:root` — one world, no attribute needed.
- **Keep** the `rtl` custom variant (`:6`) — it is unused today but Step 7 will use it.
- Base rules (`:61-70`) switch hex literals to `var(--color-*)`; `font-family` uses `var(--font-sans)` so **Noto Sans Arabic is finally in the stack**.
- Keep the `rise/fade/drift/glow/pulse` keyframes and the `prefers-reduced-motion` block as-is.
- Remove `'data-world'` from all three shells: `layouts/marketing-shell/marketing-shell.ts:9`, `auth-shell/auth-shell.ts:9`, `console-shell/console-shell.ts:9`. They become plain `<router-outlet />` wrappers.

## Step 6 — Repaint the stylesheets onto tokens

Mechanical hex → `var(--…)` substitution, in ascending order of risk.

**a. `features/marketing/marketing.css` (249 lines) — reference, lowest risk.** Values are unchanged; only their *source* changes. Update the file's header comment (`:1-10`), which currently claims the values mirror `console-template/cvhome Marketing.dc.html` step-for-step — they now come from `theme.ts`.

**b. `features/auth/auth.css` (60 lines) + `components/auth-story.css` (34).** Already forest-dark and near-duplicates of marketing's values; pure substitution. `#fca5a5` at `:14`/`:15` becomes `--color-state-danger`.

**c. `features/dashboard/dashboard.css` (530 lines) — the real work.** Light → dark repaint:

| Currently | Becomes |
|---|---|
| `:host { background:#eff3f8; color:#334155; color-scheme:light }` (`:9-17`) | canvas / `--color-text-body` / `color-scheme: dark` |
| `.card { background:#fff; border:1px solid #e2e8f0 }` (`:27`) | `--surface-fill` + `--surface-hairline` |
| `.card-label { color:#94a3b8 }` (`:26`) | `--color-text-muted` |
| slate ramp `#0f172a/#334155/#64748b/#94a3b8` | forest text ramp `text-primary → text-quiet` |
| `.plan-banner` `#ecfdf5`/`#a7f3d0`/`#065f46` (`:31-46`) | `--surface-accent-wash` / `--surface-accent-edge` / `--color-emerald-soft` |
| `:focus-visible { outline: 2px solid #10b981; offset 2px }` (`:22`) | drop it — inherit the global `3px solid var(--color-emerald-soft)` / 4px offset |
| `::selection` `#d1fae5`/`#064e3b` (`:21`) | drop it — inherit global |
| `box-shadow: 0 1px 2px rgb(18 18 23/5%)` | flat-at-rest: hairline border only, `--shadow-card-lift` on hover |

The `.card` fill is the one place needing judgement: a light card is defined by being *lighter* than its canvas, so a straight inversion loses the panel/canvas distinction. Use `--surface-fill` (`rgb(255 255 255 / 2%)`) over the canvas plus the hairline border, matching the `.store`/`.review`/`.plan` treatment in `marketing.css:78,107,140`. Recheck contrast after (see Verification).

**d. Live shared components.** `shared/ui/charts/bar-chart.css` (track `#e2e8f0` → `--surface-hairline`), `donut-chart.css` (track `#e2e8f0`, `#0f172a`, `#64748b`), `toast/toast-host.ts:29-30` (`#0f172a`), `date-range-picker.ts` inline styles (`:107-155`: `#e2e8f0`/`#fff`/`#334155`/`#f1f5f9`/`#94a3b8`) and its Tailwind class strings (`:309,312,320`: `text-slate-300`, `hover:bg-slate-100`, `text-slate-700`, `text-white`), `not-found.ts:28`.

**e. Dead shared components — do this last, timeboxed.** `Button`, `Card`, `Badge`, `StatCard`, `DataTable`, `Menu`, `Avatar`, `ProgressTrack`, `FormField`, `BarChart`, `DonutChart` have no importers and are written for the light world (`card.ts:7` `border-slate-200 bg-white`; `menu.ts:7` and `data-table.ts:7` likewise; `badge.ts:5-13` seven `-700` light tones on stock Tailwind palette; `progress-track.ts` inline `rgb(226 232 240)`). `stat-card.ts` and `form-field.ts` are additionally **broken** — they emit `.metric-card` / `.stat-icon` / `.form-field` class names that nothing defines.

Restyle them onto the tokens rather than deleting: they are the intended primitive library, and the natural follow-up to this change is having `marketing.css` and `dashboard.css` consume them instead of re-declaring `.button`, `.card`, `.plan` locally. But **their correctness cannot be observed in the running app**, so they produce no verification signal and must not absorb the effort that Step 6c needs. While here, apply the three spec fixes from the drift table: button vertical padding, badge chart tokens, and giving `stat-card`/`form-field` actual styles.

If the user would rather not carry unused code, deleting the eleven is the cheaper and arguably more honest call. Flag it; do not decide unilaterally.

## Step 7 — RTL

Not a new requirement, but the theme touches every stylesheet, so fix it opportunistically where a line is already being edited. `dir=rtl` lands on `<html>` via `initializeLocale` (`app.config.ts:31` → `locale.service.ts:28-32`), above everything. Current CSS is largely physical: `margin-left:auto` (`marketing.css:17,22`), `right:.55rem` (`auth.css:20`), `padding-right` (`auth.css:19`), `border-right` (`auth.css:42`). Convert to logical properties (`margin-inline-start`, `inset-inline-end`, `padding-inline-end`, `border-inline-end`) as those lines are touched. Do **not** open a separate RTL sweep in this change.

## Step 8 — Collapse the token sources and update the records

The point of this step is that **`theme.ts` ends as the only place a design value is authored.**

- **`DESIGN.md`** — delete the YAML frontmatter (`:1-172`) and replace it with a pointer to `src/app/core/theme/theme.ts`, so the prose spec survives but the token duplication does not. (Alternative, if the frontmatter is consumed by tooling outside this repo: have `generate-theme-css.mjs` emit it too, and cover it with the same `--check`. Grep the wider repo for readers of `DESIGN.md` before choosing.) In the prose: rewrite `### Operational Palette` (:227), the two-world framing in `## Colors` (:192-225) and `## Do's and Don'ts` (:360-367), and correct the eight drift entries per the Step 1 table. Keep the *Emerald Signal*, *Flat-at-Rest*, and *Quiet Authority* rules — they are unaffected. Promote *Dark Territory* ("no isolated light cards in forest"), which is now the governing constraint on the console.
- **`.impeccable/design.json`** — delete `colorRamps.operationsSlate`; drop **The Two-World Rule** and the `don't` about mixing dark surfaces into the light workspace; rewrite `componentExtensions.operationsDashboard.world` and `narrative.overview`/`keyCharacteristics` for one dark world; fix the stale `extensions.source` paths (`src/app/pages/…` → `src/app/features/…`, `styles.scss` → `styles.css`); point `extensions.source` at `theme.ts` as the token origin.
- **`src/styles.css`** — after Step 5 it holds no literal design values at all; that is the check that this step worked.
- `src/index.html:11-17` — the five-block direction contract comment still reads correctly for a single dark world; verify rather than rewrite.

---

## Verification

1. `npm run generate:theme` then `npm run verify:theme` — must be clean; hand-edit `theme.generated.css` and confirm `--check` fails.
2. `npm run lint` and `npm test` — `date-range-picker.spec.ts` and `dashboard.spec.ts` are the only UI specs; confirm no selector/class assertions broke.
   Also grep the whole of `src/` for surviving hex literals: after Step 6 the only permitted matches are inside `theme.ts` and `theme.generated.css`. That grep is the real completion test for this change.
   Check the `angular.json:63-67` component-style budget did not trip on `dashboard.css` (22kB warn / 24kB error) — token names are longer than hex literals.
3. `npm run build` — SSR build must succeed; `provideTheme` must not touch `document` on the server path.
4. `npm start`, then drive with chrome-devtools MCP over `/`, `/sign-in`, `/sign-up`, `/dashboard`:
   - screenshot each at **1440 / 900 / 600px** (the `nav`, `authStack`, `compact` breakpoints);
   - confirm no white/slate residue survives on `/dashboard` — grep the computed styles for `#fff`, `#eff3f8`, `#e2e8f0`;
   - confirm the dashboard's sidebar collapse (1180px) and drawer (760px) still work after the repaint.
5. **Contrast** — the dashboard repaint is where regressions will hide. Run the chrome-devtools a11y/contrast pass on `/dashboard`; the `.card-label` muted-on-fill and `.plan-banner` pairings are the likeliest failures. Marketing's own ratios are unchanged by construction.
6. `npm run dev:ar` — check `/` and `/dashboard` in RTL, and confirm Arabic now renders in Noto Sans Arabic rather than an Inter fallback (inspect computed `font-family`).
7. Confirm one dark world end-to-end: navigating `/` → `/sign-in` → `/dashboard` shows no palette jump.

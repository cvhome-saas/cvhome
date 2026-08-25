# Make the console-ui theme Tailwind-standard

## Context

`../../store-core/console-ui` is on Tailwind v4.3.3, but its design system uses **valid Tailwind
namespaces with entirely bespoke keys**: `--color-forest-canvas`, `--radius-card`,
`--text-headline`, `--spacing-content-md`, `--breakpoint-drawer`. Tailwind happily generates
utilities from those, so the build works — but nothing external can interoperate. You cannot
drop in a shadcn/tweakcn theme, you cannot swap palettes, and every value has to be learned
rather than recognised.

Two findings shape the work:

1. **~20 of the 41 solid colours are already stock Tailwind colours under private aliases.**
   `emerald-primary` = `emerald-500`, `chart-blue` = `blue-500`, `mark-amber` = `amber-300`,
   `seat-slate` = `slate-800`, `state-danger` = `red-300`, and so on. Deleting those aliases
   is a pure win — zero visual change.
2. **Consumption is overwhelmingly `var(--token)` inside the 22 component CSS files**
   (~450 references), not Tailwind utility classes in templates (only ~39 colour utilities
   across all `.html`/`.ts`). The rename is therefore concentrated and mechanical.

Decisions taken (confirmed with you):

- **Standard names, your values.** Where a value doesn't match Tailwind's default step, the
  key stays standard and the *value* is overridden — which is exactly how a Tailwind theme
  is supposed to work. No wholesale visual redesign.
- **shadcn/tweakcn semantic layer** (`background`/`foreground`/`primary`/`muted`/`border`/
  `ring`/`card`/`popover`/`destructive`/`chart-1..5`) as the layer components reference, so
  swapping themes is swapping one `:root` block.
- **CSS becomes the source of truth.** `theme.ts` + `scripts/generate-theme-css.mjs` +
  `theme.css-vars.ts` go away; `THEME` becomes a thin runtime reader for the five TS
  consumers that need resolved values.

Intended outcome: any third-party Tailwind/shadcn theme file can replace `theme-forest.css`
and the console re-skins, while the current forest identity renders pixel-for-pixel the same
(modulo the small, enumerated collapses below).

---

## Target architecture

```
src/styles.css                  @import 'tailwindcss'; then the three below
src/styles/theme.css            @theme  — primitive ramps + scales (standard keys)
src/styles/theme-forest.css     :root   — semantic assignments (THE swappable file)
src/styles/theme-bridge.css     @theme inline + @utility role compositions
```

Delete: `src/theme.generated.css`, `src/app/core/theme/theme.css-vars.ts`,
`scripts/generate-theme-css.mjs`, and the `generate:theme` / `verify:theme` / `prestart` /
`prebuild` scripts in `package.json` (`lint` drops the `verify:theme` prefix).

**Three-layer model** — this is the part that makes themes swappable:

- *Layer 1* (`theme.css`, `@theme`): primitives. Stock Tailwind ramps come free; only
  `forest-*` and `sage-*` are authored. Plus the standard scales — `--radius-*`, `--text-*`,
  `--shadow-*`, `--breakpoint-*`, `--ease-*`, `--animate-*`, `--font-weight-*`.
- *Layer 2* (`theme-forest.css`, `:root`): semantic tokens as plain custom properties
  pointing at layer 1. **This file alone defines the identity.** A tweakcn export drops in here.
- *Layer 3* (`theme-bridge.css`, `@theme inline`): re-exports layer 2 into the `--color-*`
  namespace so `bg-background`, `text-muted-foreground`, `border-border` exist as utilities.
  `inline` is required — it makes the utility resolve through the `var()` chain at use time.

---

## Colour mapping

### Delete outright — already stock Tailwind (no visual change)

| Old token | Replacement |
|---|---|
| `emerald-primary` `emerald-bright` `emerald-soft` `emerald-mist` | `emerald-500` `-400` `-300` `-200` |
| `chart-blue/cyan/amber/red/violet` | `blue-500` `cyan-500` `amber-500` `red-500` `violet-500` |
| `mark-blue/cyan/amber/red/violet` | `blue-300` `cyan-300` `amber-300` `red-300` `violet-300` |
| `mark-emerald` | `emerald-300` |
| `seat-emerald` `seat-teal` `seat-slate` | `emerald-800` `teal-900` `slate-800` |
| `seat-ink-emerald` `seat-ink-teal` `seat-ink-slate` | `emerald-200` `emerald-300` `slate-200` |
| `state-danger` | `red-300` |
| `text-primary` | `white` |

### Author as real ramps

`--color-forest-50 … 950` — pin `950: #04120d`, `900: #05140f`, `800: #071a13`; generate the
remaining steps by OKLCH interpolation toward a pale green so the ramp is complete and a
foreign ramp can replace it wholesale.

`--color-sage-50 … 950` — the green-tinted neutral replacing the nine `text-*` greys. Pin
`100: #e6efeb`, `300: #b2c1ba`, `400: #98aca3`, `500: #8fa39a`, `600: #82978e`; generate the rest.

**Enumerated collapses** (a numeric ramp has fewer slots than nine hand-picked greys; each
shift is ≤4% lightness):

- `text-icon` #b6c5be, `text-label` #afc0b8, `text-control` #a7b8b0 → `sage-300` (#b2c1ba)
- `text-subtle` #91a59c → `sage-500` (#8fa39a)
- `emerald-highlight` #59e2ab → `emerald-300`; `emerald-pale` #8cf2c6 → `emerald-200`
  (each ≤1 call site)

### Alpha surfaces → `--alpha()`

Tailwind v4's `--alpha()` is the standard idiom; the hardcoded `rgb(255 255 255 / 10%)`
literals become derivations, so they follow the palette when it is swapped:

```css
--border:      --alpha(var(--color-white) / 10%);   /* was surface-hairline */
--input:       --alpha(var(--color-white) / 6%);    /* was surface-fill-raised */
--muted:       --alpha(var(--color-white) / 2%);    /* was surface-fill */
--accent:      --alpha(var(--color-emerald-500) / 6%);  /* was surface-accent-wash */
--ring:        var(--color-emerald-300);
```

### Semantic layer (`theme-forest.css`)

```css
:root {
  color-scheme: dark;

  --background: var(--color-forest-900);   /* was forest-canvas */
  --foreground: var(--color-sage-100);     /* was text-body */
  --card: var(--color-forest-800);         /* was forest-surface */
  --card-foreground: var(--color-white);
  --popover: var(--color-forest-800);
  --popover-foreground: var(--color-sage-100);
  --primary: var(--color-emerald-500);
  --primary-foreground: var(--color-forest-950);   /* was forest-ink */
  --secondary: --alpha(var(--color-white) / 6%);
  --muted: --alpha(var(--color-white) / 2%);
  --muted-foreground: var(--color-sage-400);       /* was text-muted */
  --accent: --alpha(var(--color-emerald-500) / 6%);
  --accent-foreground: var(--color-emerald-300);
  --destructive: var(--color-red-300);
  --border: --alpha(var(--color-white) / 10%);
  --input: --alpha(var(--color-white) / 6%);
  --ring: var(--color-emerald-300);

  --chart-1: var(--color-emerald-500);
  --chart-2: var(--color-blue-500);
  --chart-3: var(--color-cyan-500);
  --chart-4: var(--color-amber-500);
  --chart-5: var(--color-violet-500);
}
```

Tokens with no shadcn counterpart keep descriptive names in the same `:root` block and are
bridged the same way: `--track` (was `surface-track`), `--scrim`, `--header-veil`,
`--hairline-soft`, `--edge` / `--edge-strong` (was `surface-accent-edge*`).

The six `--color-surface-wash-*` become `--alpha(var(--chart-N) / 16%)` at the call site,
which collapses the repeated six-way tone blocks in `kpi-card.css`, `ranked-list.css`,
`badge.ts` etc. to a single rule driven by `--chart-N`.

---

## Scale mapping

### Radius — `--radius-*`, values preserved

`xs 4px → sm` · `sm 6px → md` · `md 8px → lg` · `field 10px → lg` (collapse, 3 sites) ·
`lg 12px → xl` · `channel 14px → 2xl` (collapse) · `card 16px → 2xl` · `store 18px → 3xl` ·
`plan 20px → 3xl` (collapse) · `pill 999px → rounded-full` (stock; delete the token).

### Type — split the ladder from the roles

Tailwind's `--text-*` carries one size + companions, so two roles that share a size but differ
in weight cannot both be a ladder step. Resolve by separating them:

**The ladder** (`--text-*`, standard keys, your values — anchored on `base: 1rem` and
`sm: 0.875rem`, which already match Tailwind exactly):

| key | value | from |
|---|---|---|
| `4xs` | 0.625rem | tiny |
| `3xs` | 0.6875rem | operations-label, label (0.7 → 0.6875, −0.2px) |
| `2xs` | 0.75rem | micro |
| `xs` | 0.8125rem | caption, operations-body |
| `sm` | 0.875rem | body-strong |
| `base` | 1rem | body, body-compact (0.9375 → 1rem, +1px) |
| `lg` | 1.0625rem | body-prominent |
| `xl` | 1.25rem | title |
| `2xl` | 1.375rem | subtitle |
| `3xl` | 1.75rem | operations-title |
| `4xl` | 1.875rem | operations-metric |
| `5xl` | 2rem | metric-small |
| `6xl` | 2.5rem | quote |
| `7xl` | 2.75rem | metric |
| `8xl` | 5.25rem | hero |

`4xs`/`3xs`/`2xs` are documented extensions — Tailwind has no slot below `xs`, and the
ecosystem convention is exactly these names. A foreign theme simply won't define them; those
call sites degrade in size only, never structurally.

**The roles** become `@utility` compositions (v4's official custom-utility API) in
`theme-bridge.css`, preserving the one-decision ergonomics the codebase relies on:

```css
@utility text-headline {
  font-size: clamp(2.4rem, 5vw, 3.5rem);
  font-weight: var(--font-weight-light);
  line-height: 1.12;
  letter-spacing: -0.035em;
}
@utility text-operations-label {
  font-size: var(--text-3xs);
  font-weight: var(--font-weight-bold);
  line-height: 1.5;
  letter-spacing: 0.07em;
}
```

The three fluid roles (`display`, `headline`, `auth-headline`) are utilities only — they are
responsive clamps, not ladder steps. Add `--font-weight-book: 350` as the one extension in
the standard weight namespace (Inter Variable; 350 vs 400 is visible). Leading and tracking
are written inline in the role utilities; the stock `--leading-*` / `--tracking-*` scales stay
untouched for ad-hoc use.

### Spacing — adopt the numeric scale

Set `--spacing: 0.25rem` (stock) and delete all seven named steps:
`control-xs 0.5 → 2` · `control-sm 0.8 → 3` (0.75rem, −0.8px) · `content-sm 1 → 4` ·
`content-md 1.5 → 6` · `content-lg 3 → 12` · `section-mobile 5 → 20` · `section-desktop 7 → 28`.
Only 2 utility call sites; the rest are `var()` in component CSS.

### Breakpoints — standard keys, your values (8 call sites)

`sm: 600px` (compact) · `md: 768px` (drawer 760 + auth-stack 800, collapsed to stock) ·
`lg: 900px` (nav) · `xl: 1100px` (wide) · `2xl: 1180px` (rail).

### Shadow, ease, blur, motion

- `card-lift → --shadow-xl`; the emerald glow becomes `--shadow-primary` (a named extension
  in the standard namespace, since it is not a member of an elevation scale).
- `--ease-standard` deleted — it was literally `ease`. `--ease-emphasis` kept as an extension.
- `blur-header 14px → backdrop-blur-md` (12px, −2px, 3 sites); delete `--blur-header`.
- `--motion-state 200ms → duration-200` / `var(--default-transition-duration)`;
  `--motion-entrance 600ms → duration-600`. The two long animation durations move into the
  standard `--animate-*` namespace beside the keyframes already in `styles.css`:
  `--animate-drift: drift 14s ease infinite`, `--animate-live-pulse: pulse 2.4s ease infinite`.
- `--glow-*` and `--layout-*` are app constants, not scale members — keep them as plain
  custom properties in `theme-forest.css`, except `layout.formMeasureWide` 32rem which is
  exactly `--container-lg`.

---

## TypeScript side

`src/app/core/theme/theme.provider.ts` keeps the `THEME` token and `provideTheme()` signature
but changes shape: instead of a nested literal, it exposes a **flat reader over the resolved
custom properties**, so TS and CSS cannot drift by construction.

```ts
// resolves through the var() chain — computed custom properties are already substituted
const read = (name: string) =>
  getComputedStyle(doc.documentElement).getPropertyValue(name).trim() || SSR_FALLBACK[name];
```

`SSR_FALLBACK` is a small literal map (the ~15 tokens the five consumers actually touch), so
server rendering and unit tests resolve without a live DOM. The charts already gate on
`afterNextRender` (`src/app/shared/ui/charts/echart.ts:60`), so they are browser-only and read
live values; verify the same for `pdf-export.service.ts:42` and `console-shell.facade.ts:29`.

Consumers to update — `theme.color.emerald.primary` → `theme('--primary')`:

- `src/app/shared/ui/tone.ts` — `toneColor`/`toneInk` collapse to `--chart-N` lookups plus an
  `--alpha()` derivation for the ink, removing the hand-written `mark-*` switch entirely.
- `src/app/shared/ui/charts/bar-chart.ts` (:91-119), `donut-chart.ts` (:81-83)
- `src/app/core/export/pdf-export.service.ts`, `console-shell.facade.ts`

---

## Migration mechanics

The rename is mostly mechanical. Drive it from a single explicit map rather than ad-hoc
sed — build a `scratchpad/token-map.json` of `old → new`, then rewrite:

1. **22 component CSS files** (`src/app/**/*.css`) — the bulk, ~450 `var()` references.
   Highest-density: `console-shell.css`, `marketing.css`, `auth.css`, `dashboard.css`,
   `kpi-card.css`, `ranked-list.css`, `date-range-picker.css`.
2. **`src/styles.css`** — replace the ad-hoc `:root` + `@theme inline` aliasing block
   (`--surface-canvas`, `--ink`, `--accent`, `--color-canvas`…) with the three imports;
   update `.popover`, `::selection`, `:focus-visible`.
3. **Templates** — ~39 colour utilities across `.html`/`.ts`, plus the two spacing utilities
   (`py-section-desktop`, `p-content-md`) and the 21 type-role classes (which keep their
   names via `@utility`, so most templates need no edit at all).
4. **`DESIGN.md`** — the generated frontmatter (lines 1-220) is no longer generated. Replace
   it with a hand-written block reflecting the standard token names; the prose below line 220
   stays, with the `### Named Rules` sections re-pointed at the new names.

Do it in this order so the app stays runnable: theme files first (both old and new tokens
resolving), then CSS, then templates, then TS, then delete the generator and old tokens last.

## Verification

- `npm run lint` — must pass with `verify:theme` removed from the script.
- `npm run build` — Tailwind errors loudly on an unknown `@theme` key or a missing `@utility`,
  so a clean build proves no token was left dangling.
- `grep -rE 'forest-(ink|canvas|surface)|--color-(text|mark|chart|seat|state)-|--radius-(field|channel|card|store|plan|pill)|--spacing-(control|content|section)|--motion-|--text-(display|headline|title|body|label|caption|micro|tiny|hero|quote|metric|subtitle|operations)' src/` → **zero hits** outside `theme-forest.css` and the `@utility` block.
- `npm test` — the existing specs (`console-shell.spec.ts`, `date-range-picker.spec.ts`,
  `export-button.spec.ts`) exercise the `THEME` consumers; they must pass with the reader's
  SSR fallback path.
- `npm start`, then walk the four surfaces with the browser tools and compare against the
  current build: **marketing** (`/`, the fluid hero/display/quote roles and the glow),
  **auth** (`/sign-in`, `/sign-up` — auth-story, form measure, the 800px stack breakpoint),
  **console shell** (rail expand/collapse at 1180px, mobile drawer at 760px, toolbar veil +
  backdrop blur, store switcher popover), **dashboard** (KPI tiles, bar + donut charts, ranked
  list, date-range picker — the six-tone wash/ink pairs and the echarts colours).
- Prove swappability: paste any tweakcn export over `theme-forest.css`, reload, confirm the
  console re-skins with no other file touched. This is the acceptance test for the whole change.

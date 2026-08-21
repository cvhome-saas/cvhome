---
name: Beauty — Industrial Quote Grammar
description: A beauty + fashion boutique run like a stockroom — ink plates, hazard stripes, quoted labels, and the merchant's primary as the one zip-tie tag.
colors:
  # This theme owns NO palette. Every role below is injected per request from the merchant's ColorTheme
  # preset through the contrast-guarded bridge (libs/theme/src/merchant-bridge.ts); the theme's only colour
  # decision is mapMerchantColors in src/index.ts. Values are the live CSS variables, never hex.
  background: "var(--background)"
  foreground: "var(--foreground)"
  primary: "var(--primary)"
  primary-foreground: "var(--primary-foreground)"
  primary-hover: "var(--primary-hover)"
  muted: "var(--muted)"
  muted-foreground: "var(--muted-foreground)"
  destructive: "var(--destructive)"
  success: "var(--success)"
  overlay-shade: "rgb(0 0 0 / 0.35)"
typography:
  display:
    fontFamily: "var(--font-beauty-display), var(--font-beauty-arabic), 'Arial Narrow', sans-serif"
    fontSize: "3rem"
    fontWeight: 700
    lineHeight: 0.9
    letterSpacing: "-0.02em"
  headline:
    fontFamily: "var(--font-beauty-display), var(--font-beauty-arabic), 'Arial Narrow', sans-serif"
    fontSize: "2.25rem"
    fontWeight: 700
    lineHeight: 0.95
    letterSpacing: "-0.02em"
  title:
    fontFamily: "var(--font-beauty-display), var(--font-beauty-arabic), 'Arial Narrow', sans-serif"
    fontSize: "1.625rem"
    fontWeight: 600
    lineHeight: 1
    letterSpacing: "-0.02em"
  body:
    fontFamily: "var(--font-beauty-mono), var(--font-beauty-arabic), ui-monospace, SFMono-Regular, Menlo, monospace"
    fontSize: "0.9375rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "0.01em"
  label:
    fontFamily: "var(--font-beauty-mono), var(--font-beauty-arabic), ui-monospace, SFMono-Regular, Menlo, monospace"
    fontSize: "0.6875rem"
    fontWeight: 400
    lineHeight: 1.15
    letterSpacing: "0.08em"
  control:
    fontFamily: "var(--font-beauty-display), var(--font-beauty-arabic), 'Arial Narrow', sans-serif"
    fontSize: "0.8125rem"
    fontWeight: 600
    lineHeight: 1
    letterSpacing: "0.08em"
rounded:
  control: "2px"
  badge: "2px"
  card: "0px"
  image: "0px"
  overlay: "0px"
spacing:
  unit: "0.25rem"
  gutter: "1rem"
  gutter-lg: "1.5rem"
  section: "2.5rem"
  section-lg: "3.5rem"
  header: "3.25rem"
  header-lg: "3.75rem"
  plate-pad: "0.75rem"
components:
  button-tag:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.control}"
    rounded: "{rounded.control}"
    padding: "0 1.35rem 0 0.875rem"
    height: "2.5rem"
  button-tag-hover:
    backgroundColor: "{colors.primary-hover}"
    textColor: "{colors.primary-foreground}"
  button-plate:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.control}"
    rounded: "{rounded.card}"
    padding: "0 1rem"
    height: "2.5rem"
  button-plate-hover:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
  input-plate:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.label}"
    rounded: "{rounded.card}"
    padding: "0 0.75rem"
    height: "2.25rem"
  chip-sale-tag:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.label}"
    rounded: "{rounded.badge}"
    padding: "0 1.35rem 0 0.5rem"
    height: "1.5rem"
  chip-out-of-stock:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
    typography: "{typography.label}"
    rounded: "{rounded.card}"
    padding: "0 0.5rem"
    height: "1.5rem"
  card-item-plate:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.card}"
    padding: "{spacing.plate-pad}"
  nav-item:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.control}"
    rounded: "{rounded.card}"
    padding: "0 0.75rem"
  nav-item-hover:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
---

# Design System: Beauty — Industrial Quote Grammar

## Overview

**Creative North Star: "Labels Stay On"**

A beauty + fashion boutique run like a stockroom. Every zone, control and fact is named literally, set in
straight quotes the way a stockroom names its bins, and printed on an ink plate: a 1px rule of the merchant's
foreground colour around a patch of the merchant's background. Structure is drawn with those rules and with
45° hazard stripes; sections are separated by lines, not air. The one thing that is not ink is the zip-tie
tag — the merchant's primary colour, a punched hole at its reading end — and it carries every primary action
(add, checkout, the cart count, the active nav band, the sale mark). The world refuses both the airy blush
beauty page and the luxury black-and-gold page; it is monochrome plus one tag.

Density is tight. Plates butt on a 1px pitch (negative 1px margins, `gap-px` grids on a foreground bed),
the header is a 52/60px awning rail of plates, and the content container runs wide (84rem, 100rem for the
grid). Type does two jobs only: a condensed heavy grotesk in caps (Oswald) for anything that names, and a
mono (JetBrains Mono) for anything that is a fact — prices, SKUs, counts, labels, body. Arabic keeps the
register with Noto Kufi Arabic. There are no shadows below the overlay layer; depth is a line. State is a mark
(tag on, stripe across, ink fill on hover), never a tint.

**Key Characteristics:**
- Monochrome from the merchant preset (background + foreground); the merchant's primary is the single accent, the tag.
- 1px ink plates, square corners (2px ease on controls so the tag hole reads), hazard stripes as structure and as "struck" state.
- Straight quotes around every label (`.q`), display in condensed caps, facts and numerals in mono with tabular, zero-padded counts (`01 / 04`).
- Hover and active are ink inversions (foreground fill, background text) or the tag; never opacity or a colour wash.
- One authored motion: the tag swings once when it changes.

## Colors

The theme owns no hex. Colour roles arrive on `<html>` per request from the merchant's ColorTheme preset via
the contrast-guarded bridge (AA enforced on every `*-foreground` pair); the theme only re-maps roles and then
uses them sparingly.

### Primary
- **The Tag** (`var(--primary)` on `var(--primary-foreground)`, hover `var(--primary-hover)`): the zip-tie tag and nothing else — add-to-cart, checkout, the cart control, the hero CTA, the sale mark, the active nav band (4px bottom bar), the selected option value, the hero "next" arrow, the announcement "NOTE" tag, caret, `accent-color` and `::selection`. It is also the focus ring (`ring` is re-mapped to primary).

### Neutral
- **Cotton Ground** (`var(--background)`): the page and every plate's fill; the punched hole in the tag.
- **Ink** (`var(--foreground)`): text, every 1px rule and plate border, hazard stripes, hover fill, the out-of-stock mark, the announcement strip. `secondary`, `accent`, `border` and `input` are all demoted to this value by `mapMerchantColors`, so nothing competes with the tag.
- **Print Grey** (`var(--muted-foreground)`): secondary facts (brand/SKU lines, notes, footer address, struck option text).
- **Window Grey** (`var(--muted)`): image-window placeholder behind product and slider images only.

### Status (marks, not tints)
- **Fault** (`var(--destructive)`): field error text and the logout item; never a surface wash.
- **In Stock** (`var(--success)`): the stock fact's text only.
- Sale is not a colour: it is the primary tag with a quoted `"SAVE 20%"` / `"SALE"` label.

### Named Rules
**The One Tag Rule.** The merchant's primary appears only as the tag (or the tag's band/ring). No primary text on the page, no primary borders, no primary backgrounds wider than a control.
**The Ink Line Rule.** Secondary, accent, border and input roles are the foreground. A border is never a lighter grey; it is the same ink as the text.
**The Mark Not Tint Rule.** State is shown by putting a tag on it, running a stripe across it (`.struck`, `.hazard-soft` over an image window), or inverting it to ink on hover. Never by an opacity or colour wash — the one exception is the out-of-stock product image at 50% under its stripe.

## Typography

**Display Font:** Oswald 500/600/700 (with Noto Kufi Arabic, then "Arial Narrow", sans-serif)
**Body Font:** JetBrains Mono 400/500/700 (with Noto Kufi Arabic, then ui-monospace, SFMono-Regular, Menlo, monospace)
**Label/Mono Font:** JetBrains Mono (same family; labels are the mono at its smallest)

**Character:** A stencilled label voice over printed utility text. Display is always uppercase and always
inside straight quotes; the mono is for anything that is a fact (price, SKU, count, date, body copy), with
`tabular-nums` and two-digit zero-padding on counts.

### Hierarchy
- **Display** (700, 3rem → 6rem at `sm`/`lg`, line-height 0.9, tracking −0.02em, uppercase, quoted): the store's name on Home only. `overflow-wrap: anywhere` so long names break on narrow screens.
- **Headline** (700, 2.25rem → 4.25rem at `sm`, line-height 0.95, tracking −0.02em, uppercase, quoted): the product name in the buy box; the error-state title (3rem, line-height 1).
- **Title** (600, 1.625rem, line-height 1, tracking −0.02em, uppercase, quoted, on a riveted plate): section headings (shelves, "Checkout" form title at 600). Card titles are the same face at 1.0625rem, 600, line-height 1.15.
- **Control** (600, 0.8125rem, line-height 1, tracking +0.08em, uppercase): nav zone names, button labels, footer column headings, drawer titles, option legends. The cart tag and hero plate CTAs go to 0.9375rem–1.0625rem.
- **Body** (mono 400, 0.9375rem, line-height 1.5, tracking +0.01em): page prose and the error cause.
- **Price** (mono 700, 0.9375rem on cards, 2.25rem in the buy box, 1.0625rem as the cart subtotal, line-height 1): always mono, never display; original price as `<del>` in print grey.
- **Label** (mono 400, 0.6875rem, line-height 1.15, tracking +0.08em, uppercase): facts bands, breadcrumbs, field labels, menu items, search input, footer rows, notes. Some facts run smaller (0.7rem / 0.65rem) — see not-canonized note.

### Named Rules
**The Straight Quotes Rule.** Anything that names a thing (store, section, product, zone, control, state title) is wrapped in `.q` — straight typographic-neutral `"…"` via `::before/::after`, bidi-isolated so RTL names quote correctly. Facts and numbers are never quoted.
**The Two Voices Rule.** Display names, mono states. A price, SKU, count or date never appears in Oswald; a heading never appears in mono.
**The Padded Count Rule.** Every count is mono, `tabular-nums`, zero-padded to two digits: `02`, `01 / 04`.

## Layout

The stockroom grid: `PageShell` centres content at `max-w-wide` (100rem) by default (`container: 'wide'`),
with `max-w-content` (84rem) and `max-w-narrow` (46rem, used by the error state and checkout-style columns)
available. Horizontal gutter is 1rem, 1.5rem from `lg`. Vertical section rhythm is 2.5rem, 3.5rem from `lg`
(`py-section`); sections are separated by an ink rule or a hazard band (3px-tall `.hazard` with 1px rules
above and below), not by extra whitespace.

Plates butt: grids use `gap-px` on a foreground bed (`border border-foreground bg-foreground`, cells
`bg-background`) so each cell reads as a plate with shared 1px walls; rails set `spaceBetween: -1` and
draw a `border-e` on each slide; adjacent controls use `-ms-px` / `-mt-px`. Product grid is 2 columns at
base, 3 at `lg`, 4 at `xl` (rails go to 5 at 1400px); product images are 1:1.

Header is a sticky awning rail, 52px (60px at `lg`): logo plate with a trailing rule · quoted nav row
separated by 1px dividers (scrolls horizontally, no scrollbar) · search plate (from `md`) · 44/48px-square
action plates (language, account) · the cart tag with 0.5rem inset. A single ink rule closes the rail. Home's
first viewport is a 9/11 two-column grid from `lg`: display name + CTAs + facts strip on the start side, the
slider plate on the end side, then a hazard band. The cart is an end-side drawer (`sm:max-w-md`), mobile nav
is fullscreen. RTL mirrors via logical properties (`ps`/`pe`/`start`/`end`); Swiper is re-keyed on `dir`.

## Elevation & Depth

No shadows. Depth is drawn: every surface sits flat on the page and is bounded by a 1px ink rule
(`--elev-sm/md/lg` are all `0 0 #0000`, and shared primitives are forced to `shadow-none`/`rounded-none`).
Layering is expressed by ink inversion (a hovered or open item fills with foreground) and by hazard bands
that mark a cut between zones. The only shadow is the overlay layer.

### Shadow Vocabulary
- **Overlay** (`box-shadow: 0 24px 48px -16px rgb(0 0 0 / 0.35)`, `shadow-overlay`): the cart drawer and the search-suggestions plate only. Dropdown menus and the nav flyout do not take it; they are plates.

### Named Rules
**The No Shadow Below The Overlay Rule.** Cards, buttons, inputs, menus and the nav flyout never cast a shadow; if something needs to read as "on top" it gets an ink rule or an ink fill.

## Shapes

Square. Plates (`.plate`: `border: 1px solid var(--foreground); background: var(--background)`) and images
have 0 radius; controls and badges take a 2px ease (`rounded-control`) so the tag's punched hole reads as a
hole, not a corner artefact. The tag silhouette is a primary plate with 1.35rem end padding and a 0.4rem
background-coloured circle punched at 0.45rem from the reading end (inset ring at 55% primary-foreground);
its `transform-origin` is the hanging corner so the swing pivots like a hung tag. Riveted plates carry a
0.3rem ink dot at the start (`.rivet`, section headings). Hazard is a 45° repeating gradient, 5px ink / 8px
gap (`.hazard`), a 14% version for soft bands and struck image windows (`.hazard-soft`), a 22% 4/7px version
for struck controls (`.struck`). Swiper arrows and bullets are re-shaped to plates and 1.25×0.3rem bars.

## Components

### Buttons
- **Shape:** square plate with a 2px ease (`rounded-control`); heights 2rem / 2.5rem / 3rem (`sm` / `md` / `lg`).
- **Tag (primary, `TagButton`):** primary fill, primary-foreground text, punched hole at the end; display 600 uppercase tracking-wide; start padding 0.625 / 0.875 / 1.25rem, end padding fixed 1.35rem for the hole. Icon (Lucide, 16px) before the label. Full-width on cards and in the drawer foot.
- **Hover / Focus:** hover → `primary-hover`; focus-visible → 2px primary outline, 2px offset; transition 110ms standard ease. `swing` restarts the 420ms `beauty-swing` keyframe once (−6° / +4° / −2°), disabled under reduced motion. Disabled: 60% opacity, not-allowed cursor.
- **Plate (secondary):** `.plate` with display 600 uppercase label; hover inverts to ink fill + background text. Used for "Back to home", secondary hero CTAs, breadcrumb links, pager arrows, social icons.
- **Quiet:** mono uppercase underlined link with 4px offset ("Continue shopping").

### Chips / Marks (`ProductBadges`)
- **Sale:** the tag at 1.5rem tall, display 0.6875rem, quoted `"SAVE 20%"`; clipped to the image window's top-start corner.
- **Out of stock:** ink-filled plate, mono 0.65rem uppercase, background text; the image window behind it gets `.hazard-soft` and 50% image opacity.
- **Low stock:** a plain plate, mono 0.65rem uppercase.

### Cards / Containers (`ProductCard` — the item plate)
- **Corner Style:** 0.
- **Background:** background; image window on `muted`, 1:1, secondary image cross-fades in on hover (180ms).
- **Shadow Strategy:** none; plates share 1px walls in grids and rails.
- **Border:** 1px foreground (dropped to `border-0` inside a rail/grid that supplies the walls).
- **Internal Padding:** 0.75rem; quoted display title (1.0625rem, 2-line clamp, hover underline in primary 2px/4px offset), facts band in mono 0.7rem uppercase print grey (brand, `SKU:` with LTR value), mono bold price with `<del>` original, a full-width small tag (add, or "View details" when the product has variants).

### Inputs / Fields
- **Style:** shared `Input`/`SelectTrigger` forced to `rounded-none border-foreground font-mono text-sm` on transparent; field label is a quoted mono 0.7rem uppercase `Label`; errors mono xs in destructive under the field with `aria-invalid` on the control.
- **Search:** a `.plate` 2.25rem tall with a 14px Lucide search icon at start, mono xs uppercase input, a 1.5rem clear square (inverts on hover); suggestions drop as a plate with the overlay shadow, rows inverting on hover/focus.
- **Option values:** radios rendered as plates butted on −1px; the selected value is the tag (square, z-10); an unavailable value is `.struck` (stripe across, print-grey text).
- **Quantity:** shared stepper with buttons forced square and ink-bordered.

### Navigation
- **Desktop:** quoted display 0.8125rem 600 uppercase zone names, full-height, divided by 1px ink rules; hover/open inverts to ink; the active zone keeps a 4px primary band at its foot. Children open as a square plate (no shadow, 18rem / 32rem two-column): a "View all in …" row in display, children in mono xs uppercase with zero-padded counts at 70% opacity.
- **Header actions:** 44/48px square plates with 16px Lucide icons, ink-inverting on hover; menus are plates with mono xs uppercase rows, focus = ink inversion; logout row in destructive.
- **Breadcrumbs:** mono xs uppercase; each ancestor a quoted plate (inverts on hover), `/` separators in print grey, the current page an ink-filled quoted plate.
- **Mobile:** fullscreen nav; search moves in from the header below `md`.

### Section Heading (signature)
A quoted display title (1.625rem 600, tracking −0.02em) on a riveted plate, a `.hazard-soft` rule running
to the end, an optional mono count plate (`02`) and action at the end. Used as `h1`/`h2`/`h3` for every
shelf and page section.

### Announcement (signature)
An ink strip across the top: a small primary "NOTE" tag at the start (from `sm`), the notice in mono xs
uppercase with underlined links, a 1.5rem close square with a 60% background border that inverts on hover.
Dismissal persists per session.

### Hero Frame (signature)
The merchant's slider in a `.plate` (4:3, 16:9 from `lg`, `muted` bed), a 2px `.hazard-soft` foot band,
and at bottom-end a `01 / 04` mono counter plate with plate prev / tag next arrows (32px squares, mirrored
in RTL). Autoplay 6s, disabled on interaction; without slides the banner shows in a 21:9 plate instead.

### States
- **Empty:** a 28×10 `.hazard-soft` swatch with an ink border, quoted display title (1.25rem), mono xs print-grey body, tag actions.
- **Error:** a narrow `.plate` with a 3px `.hazard` band at its top, quoted display headline (3rem), mono body, mono 0.7rem reference digest, Retry tag + Back-home plate.
- **Skeletons:** square `Skeleton` blocks inside the same `gap-px` plate grid (`aria-busy`).

## Do's and Don'ts

### Do:
- **Do** wrap every name in `.q` and set it in Oswald uppercase; set every fact, price, count and date in JetBrains Mono.
- **Do** build surfaces as `.plate`s (1px foreground rule on background) and butt them on a 1px pitch (`gap-px` on a foreground bed, `-ms-px` between siblings).
- **Do** reserve the merchant's primary for the tag (`TagButton`, `.tag`, the active-nav band, the selected option, the sale mark) and for the focus outline.
- **Do** show state as a mark: tag on, `.struck` / `.hazard-soft` across, ink inversion on hover (`hover:bg-foreground hover:text-background`).
- **Do** separate sections with `.hazard` bands and ink rules at the 2.5rem / 3.5rem section rhythm; keep containers wide (100rem) and gutters at 1rem / 1.5rem.
- **Do** zero-pad counts to two digits with `tabular-nums`, and write the pager as `01 / 04` in LTR.
- **Do** force shared primitives into the world: `rounded-none border-foreground shadow-none` on drawers, menus, inputs, steppers, skeletons.
- **Do** keep Lucide stroke icons at 16px (14px inside inputs) and mirror directional ones with `rtl:rotate-180`.

### Don't:
- **Don't** hard-code hex; every colour is a role variable from the merchant bridge, and `secondary`/`accent`/`border`/`input` are the foreground by policy.
- **Don't** tint state: no primary/destructive/success backgrounds as washes, no opacity fades for disabled plates beyond the tag's 60%, no coloured borders lighter than ink.
- **Don't** add shadows to anything but the cart drawer and the search-suggestions plate; cards, menus and the nav flyout stay flat.
- **Don't** round corners past the 2px control ease; images, plates, overlays and badges stay square.
- **Don't** use the primary on text, rules or wide backgrounds — the tag is ≤ a control's width and the only primary on the screen.
- **Don't** add a second motion: the tag swing (once, 420ms, emphasized ease) is the theme's only keyframe; everything else is a ≤180ms colour/opacity transition.
- **Don't** put body or price text in the display face, or headings in mono.

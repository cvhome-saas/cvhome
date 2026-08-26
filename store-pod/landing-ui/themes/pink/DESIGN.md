---
name: Pink — Tokyo Girls Issue
description: The shop as this month's Japanese girls' fashion magazine — flooded cover and section openers, ruled plates of die-cuts, every price on a notched accent flag, a printed page address on every spread, and the pen annotating what is on sale or nearly gone.
colors:
  # This theme owns ONE palette: its default (src/colors.ts, generated from THEME_DEFAULTS.pink in
  # libs/types/scripts/build-color-schemas.mjs — `npm run gen:colors --workspace=libs/types`), rendered when
  # the merchant's ColorTheme is DEFAULT or unset. A fixed preset the merchant picks replaces it whole; either
  # way every role below is injected on <html> per request through the contrast-guarded bridge
  # (libs/theme/src/merchant-bridge.ts). Unlike its sibling themes pink declares NO mapMerchantColors hook —
  # its only colour decisions of its own are the material aliases and mixes in src/tokens.css. Values are the
  # live CSS variables, never hex.
  background: "var(--background)"
  foreground: "var(--foreground)"
  card: "var(--card)"
  border: "var(--border)"
  primary: "var(--primary)"
  primary-foreground: "var(--primary-foreground)"
  secondary: "var(--secondary)"
  secondary-foreground: "var(--secondary-foreground)"
  accent: "var(--accent)"
  accent-foreground: "var(--accent-foreground)"
  muted: "var(--muted)"
  muted-foreground: "var(--muted-foreground)"
  sale: "var(--sale)"
  sale-foreground: "var(--sale-foreground)"
  success: "var(--success)"
  destructive: "var(--destructive)"
  hair-soft: "color-mix(in srgb, var(--foreground) 22%, transparent)"
  ink-dim: "color-mix(in srgb, var(--foreground) 65%, transparent)"
  tone-ink: "color-mix(in srgb, var(--foreground) 30%, transparent)"
  tone-light: "color-mix(in srgb, var(--primary-foreground) 34%, transparent)"
typography:
  display:
    fontFamily: "var(--font-pink-display), var(--font-pink-arabic), var(--font-pink-sans), sans-serif"
    fontSize: "3rem"
    fontWeight: 400
    lineHeight: 0.88
    letterSpacing: "-0.03em"
  cover-line:
    fontFamily: "var(--font-pink-display), var(--font-pink-arabic), var(--font-pink-sans), sans-serif"
    fontSize: "0.8125rem"
    fontWeight: 400
    lineHeight: 1
    letterSpacing: "0.12em"
  figure:
    fontFamily: "var(--font-pink-display), var(--font-pink-arabic), var(--font-pink-sans), sans-serif"
    fontSize: "0.8125rem"
    fontWeight: 400
    lineHeight: 1.1
    letterSpacing: "-0.01em"
    fontVariant: "tabular-nums lining-nums"
  price:
    fontFamily: "var(--font-pink-display), var(--font-pink-arabic), var(--font-pink-sans), sans-serif"
    fontSize: "1.0625rem"
    fontWeight: 400
    lineHeight: 1.1
    letterSpacing: "0"
    fontVariant: "tabular-nums lining-nums"
  flag:
    fontFamily: "var(--font-pink-display), var(--font-pink-arabic), var(--font-pink-sans), sans-serif"
    fontSize: "0.8125rem"
    fontWeight: 400
    lineHeight: 1.1
    letterSpacing: "0"
    fontVariant: "tabular-nums lining-nums"
  pagemark:
    fontFamily: "var(--font-pink-display), var(--font-pink-arabic), var(--font-pink-sans), sans-serif"
    fontSize: "0.6875rem"
    fontWeight: 400
    lineHeight: 1
    letterSpacing: "0.1em"
    fontVariant: "tabular-nums"
  label:
    fontFamily: "var(--font-pink-display), var(--font-pink-arabic), var(--font-pink-sans), sans-serif"
    fontSize: "0.6875rem"
    fontWeight: 400
    lineHeight: 1
    letterSpacing: "0.1em"
  body:
    fontFamily: "var(--font-pink-sans), var(--font-pink-arabic), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 400
    lineHeight: 1.55
    letterSpacing: "0"
  entry-name:
    fontFamily: "var(--font-pink-sans), var(--font-pink-arabic), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.8125rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "0"
  marker:
    fontFamily: "var(--font-pink-sans), var(--font-pink-arabic), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.6875rem"
    fontWeight: 800
    lineHeight: 1
    letterSpacing: "0"
rounded:
  control: "9999px"
  badge: "9999px"
  card: "0px"
  image: "0px"
  overlay: "0px"
  textarea: "1rem"
spacing:
  unit: "0.25rem"
  hairline: "1px"
  gutter: "1rem"
  gutter-lg: "2rem"
  section: "2.75rem"
  section-lg: "4rem"
  header: "3.5rem"
  header-lg: "6.5rem"
  cell-foot: "0.75rem"
  panel-pad: "1rem"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.cover-line}"
    rounded: "{rounded.control}"
    padding: "0 1rem"
    height: "2.25rem"
  button-primary-lg:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.cover-line}"
    rounded: "{rounded.control}"
    padding: "0 1.5rem"
    height: "2.5rem"
  button-outline:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.cover-line}"
    rounded: "{rounded.control}"
    padding: "0 0.75rem"
    height: "2rem"
  button-shop-now:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
    typography: "{typography.cover-line}"
    rounded: "{rounded.control}"
    padding: "0.75rem 1.5rem"
  cart-tab:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.cover-line}"
    rounded: "0px"
    padding: "0 0.75rem"
    height: "2.25rem"
  nav-entry:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.cover-line}"
    rounded: "0px"
    padding: "0.375rem 0.75rem"
    height: "2.25rem"
  nav-entry-open:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
  flag-price:
    backgroundColor: "{colors.accent}"
    textColor: "{colors.accent-foreground}"
    typography: "{typography.price}"
    rounded: "0px"
    padding: "0.3em 0.85em 0.26em 0.6em"
  flag-sale:
    backgroundColor: "{colors.sale}"
    textColor: "{colors.sale-foreground}"
    typography: "{typography.flag}"
    rounded: "0px"
    padding: "0.3em 0.85em 0.26em 0.6em"
  flag-ink:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
    typography: "{typography.flag}"
    rounded: "0px"
    padding: "0.3em 0.85em 0.26em 0.6em"
  flag-flood:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.flag}"
    rounded: "0px"
    padding: "0.3em 0.85em 0.26em 0.6em"
  pagemark:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
    typography: "{typography.pagemark}"
    rounded: "0px"
    padding: "0.3em 0.6em 0.26em"
  pagemark-open:
    backgroundColor: "transparent"
    textColor: "{colors.foreground}"
    typography: "{typography.pagemark}"
    rounded: "0px"
    padding: "0.3em 0.6em 0.26em"
  cell:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.card}"
    padding: "{spacing.cell-foot}"
  option-chip:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.entry-name}"
    rounded: "0px"
    padding: "0.5rem 0.875rem"
  option-chip-on:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
  input:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    padding: "0 0.75rem"
    height: "2.25rem"
  tab-active:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.cover-line}"
    rounded: "0px"
    height: "2.5rem"
  announcement:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.cover-line}"
    rounded: "0px"
    padding: "0.5rem 1rem"
---

# Design System: Pink — Tokyo Girls Issue

## Overview

**Creative North Star: "This Month's Issue"**

The shop is this month's issue of a Japanese girls' fashion magazine, printed for a mixed girly-lifestyle
merchant — apparel, accessories, beauty and stationery in one catalogue — and read by teenage girls and
young women. The stock is paper (`--paper`) and plum-black ink (`--ink`); whole regions flood with the
merchant's primary the way a cover floods with ink; every product is a die-cut ranged in a ruled cell with
its price printed on a notched flag in the merchant's accent; and every spread carries a printed page
address, so the reader always knows where in the issue she is. The direction was assigned by impeccable's
`new-work` roll (seed `f844e405`, candidate 3 of the grounded list) and chosen by the user over the pick card
and one competitive challenger; the build was code-led, with no comp round. Its stated anti-goals were the
soft millennial-pink boutique of rounded, shadowed cards that every pink storefront ships, its stark
monochrome opposite, and — the user's own words — anything "too loud / hard to shop".

The pink world always wins. The theme ships a committed pink default palette (generated, never
hand-written), and a merchant preset re-maps the roles without dissolving the grammar: the flood is
whatever `--primary` is that day, the flag whatever `--accent` is, and every rule below is written against
the role, not the hue. There is exactly one authored moment of motion — a flag snapping onto a die-cut when
the basket's real quantity for that sku goes up — and everything else is a 110–190ms colour or opacity
transition. Density is a magazine's: running text small and tight (0.9375rem/1.55), cover lines jumping
straight to cover scale (up to 5.5rem), five die-cuts across the widest plate.

Three devices carry the issue and nothing may dilute them: **the page-address system** (`.pagemark` tabs,
numbered contents entries, numbered section openers), **the notched accent price flag**, and **the single
basket-truth snap**.

**Key Characteristics:**
- Flooded regions, not accents: `.flood` owns the cover, category and blog openers, drawer heads, the open nav entry, the basket tab, the active account tab, the selected option, the state-block icon square and every primary button.
- Every price rides a notched pennant of flat accent (`.flag` / `.price-flag`, `clip-path` chevron, mirrored in RTL); sale is the same pennant in `--sale`, sold out the same pennant in ink.
- A printed page address everywhere: `.pagemark` ink tabs for section numbers, listing pages, order numbers and statuses; `.pagemark-open` (hairline, no field) for SKUs and order statuses in tables.
- Ruled plates: `.plate` is a 1px `--hair` grid whose 1px gaps *are* the rules, `.cell` is one entry on paper; no card radius, no card shadow, pill controls only.
- Facts the shopper needs are marker annotations in the reader's own pen (`.marker`, 2.2px round-cap strokes drawn on over 460ms) — a circled sale price, an arrow at a last-few-units count. Never decoration.
- One authored motion: `.snap` / `@keyframes pink-snap` (460ms on `--easing-emphasized`), fired by `inCart > seenInCart`, not by the click.
- Dela Gothic One names, prices and flags; M PLUS Rounded 1c explains; Cairo is the Arabic companion in both stacks, reached by fallthrough rather than by a stack swap.

## Colors

The theme hand-writes no hex. Its default palette (`src/colors.ts`, generated from the OKLCH seed
`THEME_DEFAULTS.pink`: paper-white stock, plum-black ink, hot-pink cover flood with dark text, petal wash
support, cover-line yellow statement) renders when the merchant's ColorTheme is `DEFAULT`; a picked preset
replaces it whole. Roles arrive on `<html>` per request through the contrast-guarded bridge, which chooses
every `*-foreground` white-or-near-black by measured contrast and nudges the field until the pair clears
4.5:1. Pink declares no `mapMerchantColors` hook at all — it takes the bridge's roles as they come and adds
only its own material aliases and mixes in `tokens.css`.

### Primary
- **The Flood** (`var(--primary)` on `var(--primary-foreground)`, aliased `--flood` / `--flood-ink`): colour owning a whole region — the cover, the category and blog-index openers, the announcement band, the cart / menu / filter drawer heads, the open or hovered contents entry, the basket tab in the masthead, the active account tab, the selected option value, the ADDED flag, the empty / error block's icon square, the redirect and order-success star squares, the order-history dots, the active swiper bullet and the hovered page-turn chip, plus every primary `Button`, the focus ring, `caret-color`, `accent-color`, the scrollbar thumb and `::selection`. Never a hairline, never a tint, never body text.

### Secondary
- **The Wash** (`var(--secondary)` on `var(--secondary-foreground)`, aliased `--wash` / `--wash-ink`): the petal support field — alternating home feature bands, image beds behind die-cuts and cart thumbnails, table header rows, the mobile nav's expanded children, the search panel's scope note, and the 45%-of-wash hover on a `.cell`.

### Tertiary
- **The Flag** (`var(--accent)` on `var(--accent-foreground)`, aliased `--flag` / `--flag-ink`): the notched pennant and nothing else — every price on a die-cut, in the buy box, on a cart line, on a subtotal and on an order total. It is never a field, a rule or a text colour.

### Neutral
- **Paper** (`var(--background)`, aliased `--paper`): the page, the masthead, every `.cell`, every input and the print inside an ink field.
- **Ink** (`var(--foreground)`, aliased `--ink` and `--hair`): all text, every hairline, the `.pagemark` field, the `.flag-ink`, the SHOP NOW pill, the footer colophon (`.ink-field`), the page-turn chips and the swiper bullets.
- **Soft hair** (`--hair-soft`, ink at 22%): the only quieter line in the issue — input and textarea borders, and the ruled lines of the order slip's blank stock (`.ruled-stock`).
- **Quiet ink** (`--ink-dim`, ink at 65% composited over its own ground): `.dim` — contents-row section numbers, child counts, the item count beside a section opener, the kind label in a search suggestion.
- **Screentone** (`--tone-ink` at 30% ink / `--tone-light` at 34% primary-foreground): the printed halftone dot laid over a field at 7px pitch. Only `.tone-light` is in service, on the flooded cover, announcement, category and blog openers and cart head.
- **Print grey** (`var(--muted-foreground)`): facts on paper — the manufacturer line, struck-through original prices, timestamps, addresses, helper copy. AA-guarded by the bridge against the page background only.
- **Rule** (`var(--border)`): reaches the theme through the shared primitives; the theme's own lines are drawn in `--hair` (ink) or `--hair-soft`.

### Status
- **Sale** (`var(--sale)` on `var(--sale-foreground)`, the preset's error role via the bridge): the SAVE N% / SALE flag and the marker's own ink — the ring around a discounted price, the arrow at a low-stock count.
- **In stock** (`var(--success)`): one line of cover-line text in the buy box; never a field.
- **Fault** (`var(--destructive)`): field errors and the failed-order mark through the shared primitives; never a surface.

### Named Rules
**The Flood Rule.** The merchant primary owns whole regions or nothing. It is a field (`.flood`), the ADDED flag, the focus ring, the caret and the selection — never a hairline, never a tint under text, never a second primary button in one view.
**The Flag Rule.** Every printed figure that is a price rides the notched accent pennant; the pennant's only other liveries are `.flag-sale` (`--sale`), `.flag-ink` for sold out, and `.flag-flood` for the one ADDED moment. The accent never appears as a field, a rule or a text colour.
**The Measured Quiet Rule.** Contrast is measured, not assumed. `--primary-foreground` clears the flood by only **4.57:1** on the default palette, so **nothing may be dimmed on a flooded field**: `.flood .dim` and `.ink-field .dim` reset to `color: inherit`, and the quiet register there is carried by size and weight at full colour strength. On paper and on the wash the quiet register is `--ink-dim` (**5.6:1** on paper, **5.0:1** on the wash) and never `opacity`. `muted-foreground` is guarded against the page background only (4.7:1 there, **3.6:1** on the wash) — keep it on paper. On the ink colophon, printed opacity stays at or above 0.6 (6.8:1); below roughly 0.5 it falls under the floor.

## Typography

**Display Font:** Dela Gothic One 400 — one ultra-heavy Japanese poster gothic, the weight a girls'
magazine cover line is printed at (with Cairo, then M PLUS Rounded 1c, sans-serif)
**Body Font:** M PLUS Rounded 1c 400 / 500 / 700 / 800 — the rounded gothic of Japanese stationery and
captions (Latin, Latin-ext, Cyrillic; with Cairo, ui-sans-serif, system-ui, sans-serif)
**Label/Mono Font:** none distinct; labels are the display face at 0.6875rem with +0.1em tracking, uppercase.
`--font-code` (`ui-monospace`) is declared and unused.

**Character:** Two voices and no third. Anything that names, prices or flags is Dela Gothic One — the
masthead, cover lines, section openers, page marks, flags, figures, button labels, form labels, prose
subheads and pull quotes. Anything that explains is M PLUS Rounded 1c — product names, facts, prose, table
cells, helper copy. Dela Gothic One ships one weight (400) that reads as ultra-black by design, so nothing
is faux-bolded and no display weight is ever requested.

Arabic is served without a stack swap, deliberately: neither Latin cut carries Arabic, so Arabic glyphs fall
through to Cairo per glyph while Latin inside an Arabic page keeps its own voice. Both Latin cuts set
`adjustFontFallback: false`, because next/font's Arial-based metric fallback *does* carry Arabic and would
catch those glyphs before Cairo ever sees them. Because Cairo is variable and Dela Gothic One is not,
`:lang(ar)` asks Cairo for weight 900 on the printed roles (`.display`, `.cover-line`, `.figure`,
`.pagemark`, `.flag`, headings, buttons, the price flag, prose blockquotes) and drops the Latin tracking and
uppercasing to 0 / none.

### Hierarchy
- **Display** (`.display`, 400, line-height 0.88, −0.03em): the cover title at 3rem → 4rem at `sm` → 5.5rem at `lg`; the colophon's store name at the same ramp; section-opener titles at 1.625rem → 2.25rem; page titles at 2.25–3rem; drawer titles at 1.625rem; cover-line entries at 1.25–1.625rem; the blog card title at 1.0625rem.
- **Cover line** (`.cover-line`, 400, 0.8125rem, +0.12em, uppercase, line-height 1): the tracked caps a magazine sets over a photograph — contents entries, breadcrumbs, imprint links, filter legends, table heads, tab labels, field group heads, the subtotal label, the in-stock line, result counts, the SHOP NOW pill.
- **Figure** (`.figure`, 400, tabular lining, −0.01em): every printed number that is not on a flag — contents numbers, item counts, quantities, order ids and dates, phone numbers, timestamps.
- **Price** (`.price-flag [data-slot="price-final"]`, 1.0625rem on a die-cut, 2.25rem in the buy box; the struck original beside it in the display face at 0.8125rem / 1.0625rem print grey): always on the flag, always tabular, never wrapped.
- **Flag** (`.flag`, 0.8125rem, line-height 1.1; `.flag-lg` at 1.25rem for subtotals and totals; 0.9375rem on a die-cut's sale and ADDED flags): sale, sold out, category counts, cart line totals.
- **Page mark** (`.pagemark`, 0.6875rem, +0.1em, tabular, ink field / paper print): section numbers, "page x of y", order numbers and statuses; `.pagemark-open` is the same tab drawn as a hairline outline for SKUs and table statuses.
- **Body** (M PLUS Rounded 1c 400, 0.9375rem, line-height 1.55; prose at 1.7): page copy, facts, inputs, messages.
- **Product name** (M PLUS Rounded 1c 700, 0.8125rem, line-height 1.2, 2-line clamp): the die-cut's name slot and the cart line; the buy-box name is `.display` at 2.25–3rem.
- **Label** (display face, 0.6875rem, +0.1em, uppercase): every form label; `:lang(ar)` drops both tracking and case.
- **Marker** (`.marker`, M PLUS Rounded 1c 800, 0.6875rem, sale-coloured, rotated −4°): the annotation's own hand-written voice.

### Named Rules
**The Two Voices Rule.** If it names, prices or flags, it is Dela Gothic One; if it explains, it is M PLUS Rounded 1c. There is no third face and no other weight of the display face — a price is never set in the body face, and a product name, fact or paragraph is never set in the display face.
**The Fallthrough Rule.** Arabic is reached by falling through the Latin stacks to Cairo, never by swapping the stack on `:lang(ar)`; `adjustFontFallback` stays `false` on both Latin cuts, and `:lang(ar)` adjusts weight (900), tracking (0) and case (none) only.
**The Printed Fact Rule.** Prices arrive pre-formatted from the API and are never re-formatted; counts, SKUs, order ids, dates and phone numbers carry `tabular-nums`; SKUs and phone numbers are forced `dir="ltr"`, and a SKU already beginning with "SKU" is not labelled twice.

## Layout

The issue runs at `max-w-content` (82rem) for Home, Category, Product, Checkout, Account and Blog;
`max-w-narrow` (44rem) for CMS pages, FAQ, blog posts, checkout results, and every empty / error / not-found
block; `max-w-wide` (98rem) exists and is unused. The gutter is 1rem, 2rem from `lg`; the stretch rhythm is
2.75rem, 4rem from `lg` (`--section-y`, used as `py-section` / `mt-section`).

First viewport: the merchant's announcement as a flooded, screentoned band (its copy plain and compact at
0.8125rem on a phone, tracked cover caps from `sm`) → the masthead, sticky, a heavy two-rule band: a 3.5rem
title row (4.25rem at `lg`) with the menu trigger below `lg`, the logo (2rem, 2.5rem at `lg`) or the store
name in the display face, the imprint links from `xl`, then search from `md`, the language and account
ghosts and the flooded basket tab; under it, from `lg` only, the ruled contents row of numbered sections
that wraps rather than clipping. Below `lg` the contents live in the start-side drawer instead. The token
`--header-h-lg` (6.5rem) is what sticky offsets and `scroll-mt-header-lg` anchor to.

Then the cover: one flooded, screentoned section. From `lg` its grid (`.cover-grid`) is three columns —
column one is the trim, computed as exactly the container's own margin measured off the full-bleed section,
so the type sits on the container's measure while the merchant's first slider image bleeds off the end edge
(min 30rem tall, capped at 34rem of column). The type column carries the store name at cover scale, the
issue's real cover lines — each a numbered in-page address of a feature further down, so no second contents
strip is printed — and SHOP NOW as an ink pill that lifts 2px on hover. With no slider image the cover is
type-only on the content measure and still finished.

Feature groups follow, every odd one on a wash band, each opening with a `SectionHeading`: a 2px ink rule
carrying the numbered `.pagemark`, the title in the display face and the item count at the end. The first
group prints as a plate; the rest run as rails (12px gaps, 2 / 3 / 4 / 5 slides at 0 / 640 / 1024 / 1280px,
page-turn chips hidden below `lg` because touch swipes). Plates are 2 / 3 / 4 / 5 columns (layout config)
with 4:5 portrait die-cuts.

The category page opens with a flooded header (breadcrumbs in `crumbs-flood`, the name at 3–4rem, the true
listing count on an ink flag, subsections as numbered cover lines), then the listing: a 14rem facet rail
from `lg` (a start-side drawer below), the result count and sort on one hairline, the plate between them,
and a 2px-ruled page turn with the `.pagemark` address between two outline buttons. The product page is two
columns from `lg` — the gallery (1:1 well, `contain`, 4rem thumbnails) and the buy box, with details and
specifications as an accordion under the action so the buying column reads as one panel. Checkout is 2 + 1
columns with the order slip sticky at `--header-h-lg + 1rem`. Drawers: cart from the reading end
(`sm:max-w-md`), contents and filters from the reading start (88vw, max 24rem). RTL mirrors through logical
properties, `rtl:rotate-180` on directional icons, `rtl:-scale-x-100` on the marker arrows, an explicit
`dir` handed to Swiper, and a mirrored `clip-path` on every flag.

## Elevation & Depth

Print has rules, not shadows. Depth in this world is a 1px ink hairline, a 2px ink rule where a section
starts, and flat fields stacked on paper — die-cuts, cells, plates, flags, page marks and buttons never
lift and never glow. Only things that genuinely float over the page carry a shadow: the rail's page-turn
chips (`--elev-md`), the search suggestion panel (`shadow-overlay`) and the dev toast (`--elev-lg`). Hover
is a colour change, a 45%-wash on a cell, or a 2px translate on the SHOP NOW pill — never a lift on a card.

### Shadow Vocabulary
- **Floating control** (`--elev-md`: `0 2px 4px rgb(31 17 26 / 0.10), 0 10px 24px -12px rgb(31 17 26 / 0.28)`): the rail's prev / next ink chips.
- **Lifted paper** (`--elev-lg`: `0 4px 8px rgb(31 17 26 / 0.12), 0 22px 44px -18px rgb(31 17 26 / 0.34)`): the toast.
- **Overlay** (`--elev-overlay`: `0 28px 56px -20px rgb(31 17 26 / 0.45)`): the search panel, menus and drawers through the shell's `shadow-overlay`.
- `--elev-sm` (`0 1px 2px rgb(31 17 26 / 0.10)`) is declared for the shared primitives and referenced by no theme element — not canonized. Inputs and select triggers explicitly reset `box-shadow: none`.

### Named Rules
**The Ruled-Not-Lifted Rule.** Separation is a 1px `--hair` (2px where a section or a panel begins). A shadow means the surface is floating over the page. Never a shadow on a cell, a plate, a flag, a page mark or a button; never a glow, never a coloured shadow, never a hover lift on a die-cut.
**The Layering Rule.** The world's grammar (`.display`, `.cover-line`, `.figure`, `.dim`, `.band-copy`, `.pagemark`, `.flood`, `.wash`, `.ink-field`, `.tone*`, `.hair*`, `.cell`, `.plate`, `.cover-grid`, `.flag*`, `.price-flag*`, `.marker*`, `.prose-issue`, `.snap`, `.greyed`) lives in `@layer components`, so a Tailwind utility on the same element (`text-2xl`, `p-3`, `lg:sticky`) always wins. The control and form block (`.crumbs-flood`, the nav row fix, `.ruled-stock`, `.issue-block`, button / input / label / checkbox rules, the Swiper and toast overrides) sits deliberately **outside** the layer, because it has to outrank the shared primitives' own defaults.

## Shapes

Cut square, pressed as pills. Every plane in the issue is square (`--r-card` / `--r-image` / `--r-overlay`
all 0): cells, plates, images, drawers, menus, the search panel, the toast, the swiper bullets. Every
control is a full pill (`--r-control` / `--r-badge` `9999px`, which the shared scale reads as
`--radius-sm/md`): buttons, inputs, select triggers, skeleton buttons. The one exception is the textarea, a
1rem soft rectangle rather than a pill because a pill cannot hold multiple lines — the theme's only radius
outside the two-value scale.

The signature silhouette is the flag's notch: a `clip-path` chevron cut into the end edge
(`polygon(0 0, 100% 0, calc(100% - 0.55em) 50%, 100% 100%, 0 100%)`, mirrored under `dir="rtl"`), with the
padding lopsided to match. Rules are ink at 1px, doubled to 2px at a section opener, a panel edge, a drawer
edge or a page turn; the only softer line in the issue is `--hair-soft` (ink at 22%) on form stock and on
the order slip's blank ruled paper. A plate draws its rules *with its gaps*: the container is `--hair` with
a 1px frame and 1px gaps and every child is paper.

## Components

### Buttons
- **Shape:** full pills at every size (`9999px`); heights 2rem (`sm`), 2.25rem (default), 2.5rem (`lg`), plus the shared 2rem / 2.25rem / 2.5rem icon squares — which are pills too.
- **Type:** every button label is the display face at 400 with +0.03em tracking (0 in Arabic), Lucide 16px icons inline.
- **Primary:** the flood as a field with primary-foreground print — one per view (ADD TO CART, CHECKOUT, PLACE ORDER, COMPLETE PAYMENT, BACK TO HOME). Hover is the shared 90% opacity; focus is the shared 3px ring in primary.
- **Outline / Ghost / Link:** outline (paper field, hairline, ink print) carries the pager, FILTERS, RETRY and CONTINUE SHOPPING; ghost carries the masthead's language and account controls, the announcement's dismiss and the clear-filters control; `variant="link"` carries Continue shopping under CHECKOUT.
- **Shop Now (on the cover):** the inversion — an ink pill on the flooded cover with a `StarMark` sparkle, lifting 2px on hover over 110ms.

### Flags (signature)
`.flag` is the issue's badge, chip and price tag in one: a notched pennant of flat colour in the display
face with tabular figures. **Accent** for every price (`.price-flag`, `.price-flag-lg` in the buy box);
**`--sale`** for SAVE N% / SALE; **ink** for OUT OF STOCK and for a category's product count; **flood** for
the one ADDED moment. Struck-through originals sit beside the pennant in print grey, never on it. Flags
never wrap, never round, never take a border.

### Page marks (signature)
`.pagemark` is a solid ink tab in the display face at 0.6875rem with +0.1em tracking and tabular figures —
the issue's address system. Solid ink for a section number, a listing's "page x of y", an order number and
an order status; `.pagemark-open` (transparent, hairline, ink print) for a SKU beside the buy box and for
statuses inside the orders table. `.pagemark-flood` is declared and unused — not canonized.

### Marker annotations (signature)
The reader's own pen: `.marker` sets an 800-weight caption at 0.6875rem in `--sale`, rotated −4°, beside one
of four SVG marks (`RingMark`, `ArrowMark`, `UnderMark`, `StarMark`) sharing one stroke language — 2.2px,
round caps, no fill, drawn on over 460ms with a 120ms delay via `stroke-dasharray` / `@keyframes pink-draw`.
Marks point at a fact the shop actually knows: an arrow at a low-stock count on a die-cut and in the buy
box, a ring around a discounted price. `.marker-ink` and `UnderMark` are built and unused — not canonized.
A mark with nothing to say is not drawn.

### Cards / Containers
- **Die-cut (`ProductCard` in a `.cell`):** a paper cell in a plate, hairline-ruled by its neighbours' gaps, full height: a 4:5 picture on the wash (second image cross-fades in 190ms on hover), the annotation row across its top (sale flag, sold-out flag, low-stock marker), the ADDED flag snapping onto the bottom-end corner clear of that row, then under a 1px rule the fixed slots at `p-3` — name (700, 2 lines), the price on its flag, and a full-width `sm` action that reads ADD TO CART, then "IN BASKET (n)" once the basket says so, or VIEW DETAILS when the product has variants. Hover / focus-within washes the cell to 45% wash over paper in 110ms.
- **Plate (`.plate` / `ProductGrid`):** an ink grid with a 1px frame and 1px gaps, children on paper; 2 / 3 / 4 / 5 columns. Skeletons print as blank cells on the same plate, so nothing shifts when the goods arrive.
- **Panel:** a 2px-ruled block for the order slip, the checkout result and the account tables; a 1px-ruled block with a cover-line head on a rule for addresses, order items and history.

### Inputs / Fields
- **Style:** full pills on paper with a `--hair-soft` border and no shadow; the textarea is the 1rem exception; checkboxes and radios take a full-strength ink border.
- **Focus:** the border goes to the flood and a 3px flood-at-32% ring is drawn — the theme's own rule, replacing the primitives' default ring.
- **Labels:** the display face at 0.6875rem, +0.1em, uppercase (tracking and case dropped in Arabic); errors print in destructive text under the field.
- **Search:** a pill input with a 16px icon at the start and a ghost clear at the end, 14rem wide in the masthead (from `md`) and full width in the contents drawer; suggestions drop into an 18rem square panel with a 2px hairline, `shadow-overlay` and hover rows that flood. When the platform can only suggest categories and pages, the panel says so on a washed cover-line strip.
- **Option values:** square hairline chips at `px-3.5 py-2` in 700 body; selected floods with a primary border; unavailable goes dashed, print grey and struck through, with a price delta printed in the figure voice at full strength.

### Navigation
- **Contents row (signature):** from `lg`, a ruled row of the merchant's categories, each a tracked cover line at `px-3 py-1.5` preceded by its two-digit number in `.dim`; hover and open flood the entry. Children drop into a square 2px-ruled panel — a "view all" row on a rule, then a two-column list of children with counts in the dim figure voice.
- **Contents drawer:** below `lg` the same list opens from the reading start (88vw, max 24rem) with a flooded head reading CONTENTS, the search field on a rule, HOME as 00, then numbered rows; a category with children carries an accordion trigger beside its own link, and the children sit on the wash.
- **Masthead actions:** ghost language and account icon buttons, then the basket as a flooded tab (2.25rem, bag icon, count in tabular figures) that brightens 5% on hover and opens the cart drawer.
- **Breadcrumbs:** the running head, cover-line small; on a flooded opener `.crumbs-flood` prints the whole trail at full `--flood-ink` strength and marks the current page by weight 900 and the ancestors by an underline on hover — never by dimming.
- **Account tabs:** three full-width cover-line tabs on a 2px rule; the active one floods.

### Cart Drawer (signature)
From the reading end (`sm:max-w-md`): a flooded, screentoned head with the title in the display face at
1.625rem and the item count in cover-line caps; then the lines on `.ruled-stock` — blank ruled paper at a
2.75rem pitch in `--hair-soft` that only shows through where there is no line, because each populated line
carries its own opaque paper background. A line is a 5rem hairline-framed die-cut on the wash, the name at
700, the shared quantity stepper, and the line total on its flag; the remove control is a 1.5rem hairline
square that floods on hover and carries a `before:-inset-2.5` pseudo-element so its hit area reaches 44px.
The foot sits on a 2px rule: SUB TOTAL in cover-line caps, the subtotal on the largest flag, the tax note,
a full-width `lg` CHECKOUT and Continue shopping as a link.

### Announcement (signature)
The merchant's header message as a flooded, screentoned band above the masthead: centred, links underlined,
with a ghost dismiss that hides itself whenever a drawer's scrim owns the screen (otherwise it sits a thumb
away from the drawer's own close, inert and identical). On a phone the copy is plain 700 body at 0.8125rem
with 1.35 leading; from `sm` it becomes tracked cover caps. Dismissal persists per session.

### States
- **Empty / Not found / Error (`.issue-block`):** the shared blocks printed in the issue's voice — the title forced into the display face at 1.625rem and the icon into a 4rem flooded square with a 1.75rem glyph.
- **Loading:** per-page skeletons on the same plates and the same square / pill radii (`aria-busy`); a refetching listing dims to 60%.
- **Sold out:** the ink flag over a `.greyed` picture (`grayscale(1) contrast(0.92) brightness(1.04)`, 62% opacity) and the action disabled.
- **Redirecting:** the sparkle turning in a flooded square at 2.4s, stopped under reduced motion.

### Motion
`--motion-fast` 110ms (colour, border, brightness, the SHOP NOW lift), `--motion-base` 190ms (the die-cut's
image cross-fade), `--motion-slow` 460ms (the two keyframes), on `--easing-standard`
`cubic-bezier(0.2, 0, 0, 1)` and `--easing-emphasized` `cubic-bezier(0.16, 1, 0.3, 1)`. The one authored
moment is `.snap` / `@keyframes pink-snap`: the ADDED flag arrives at 1.6× scale and −12° rotation,
overshoots to 0.95 / +2° at 58% and settles — 460ms on the emphasized easing. It is fired by the basket's
own truth (`inCart > seenInCart` in `ProductCard`), not by the click, so a failed add never animates and a
quantity that really rose always does. `prefers-reduced-motion` disables the snap and the marker's draw-on
outright.

## Do's and Don'ts

### Do:
- **Do** flood whole regions with the primary (`.flood` on the cover, section openers, drawer heads, the open contents entry, the basket tab, the active tab, the selected option, the state-block icon square) and keep exactly one primary button per view.
- **Do** print every price on the notched accent pennant (`.price-flag`, `.flag`), and use the pennant's ink and sale liveries for sold out and for SAVE N%.
- **Do** give every spread its printed address: a numbered `.pagemark` on a section opener, "page x of y" on a listing's page turn, the order number and status as page marks, the SKU as `.pagemark-open`.
- **Do** range entries on a `.plate` (2 / 3 / 4 / 5, 1px ink gaps, children on paper) with the die-cut's slots fixed — 4:5 picture, annotation row on top, name at 700 (2 lines), price flag, one full-width action.
- **Do** carry the quiet register with `--ink-dim` (`.dim`) on paper and the wash, and with size and weight alone on any flooded or ink field, where `.dim` deliberately resets to `inherit`.
- **Do** set names, prices, flags, page marks, buttons, labels, prose subheads and pull quotes in Dela Gothic One, and everything that explains in M PLUS Rounded 1c; let Arabic fall through to Cairo and ask it for weight 900 on the printed roles.
- **Do** draw a marker annotation only for a fact the shop actually knows (a discount, a last-few count, a sold-out die-cut), in the one stroke language (2.2px, round caps, drawn on over 460ms).
- **Do** keep radii to the two-value scale: 0 on every plane, `9999px` on every control (the 1rem textarea is the single exception).
- **Do** mirror for RTL with logical properties, the flag's mirrored `clip-path`, `rtl:rotate-180` on directional icons and `rtl:-scale-x-100` on the marker arrows, and hand Swiper an explicit `dir`.

### Don't:
- **Don't** hard-code hex. Every colour is a role variable from the merchant bridge or one of the theme's declared `color-mix` materials (`--hair-soft`, `--ink-dim`, `--tone-ink`, `--tone-light`); the theme deliberately keeps no `mapMerchantColors` hook.
- **Don't** dim anything on a flooded or ink field — `--primary-foreground` clears the flood by only 4.57:1, so opacity, `--ink-dim` or `muted-foreground` there falls under the floor. Don't put `muted-foreground` on the wash either (3.6:1).
- **Don't** use opacity as the quiet register anywhere it can be a colour: `.dim` is a composited token precisely so it can be measured.
- **Don't** let the accent be anything but the pennant — no accent fields, no accent rules, no accent text — and don't let the primary become a hairline, a tint or a text colour.
- **Don't** float a cell, a plate, a flag, a page mark or a button: no card shadows, no hover lift on a die-cut, no glows, no coloured shadows.
- **Don't** add a second authored motion. `pink-snap` (460ms, emphasized, basket-driven) is the issue's one moment, and the marker's `pink-draw` is its only other keyframe; everything else is a 110–190ms transition, and both keyframes stop under `prefers-reduced-motion`.
- **Don't** round a plane or square a control; don't introduce a third radius beyond the textarea's 1rem.
- **Don't** print a second contents strip — the cover lines *are* the contents, each the numbered address of a feature further down the page.
- **Don't** put category navigation in the masthead's title row: that row is the imprint (CMS pages and the merchant's non-page menu entries); categories are the ruled contents row under it, and its drawer below `lg`.
- **Don't** move the world's grammar out of `@layer components`, or move the control / form / Swiper / toast overrides into it — the first must lose to Tailwind utilities, the second must beat the shared primitives.

<!--
Recorded from the built theme after its finish review, which was not a clean first pass: it returned
`recapture` (mobile captures taken at the wrong viewport), then `fix` with eight material findings, then a
verdict scoring seven resolved and one partial, plus two regressions and two out-of-scope contrast dims. A
second fix round addressed all of those — `.ruled-stock` no longer paints behind populated cart lines
(CartLineItem carries an opaque paper background), the remove control keeps its 24px hairline square but
regains a 44px hit area via `before:-inset-2.5`, the phone announcement band moved from 11px to 13px, and
`.crumbs-flood` and the BuyBox option price dropped their sub-floor dimming on flooded grounds.

Known good at the time of writing: `npm run lint`, `npm run typecheck`, `npm run build`, the colour-bridge
tests and the impeccable design detector all pass clean.

Wiring: the theme is registered under the `pink` theme id and its own `Theme.PINK` enum value only. It is
not mapped to any legacy enum value and is not the fallback theme (the user's call). Making it
merchant-selectable needs the Java `Theme` enum value with `implemented = true`.

Divergences between the direction contract in `src/layout/Root.tsx` and the build, where the build wins:
- The contract says "every category, listing page and product carries a printed page address". Section
  openers, listings (when there is more than one page), products (SKU), FAQ groups and orders do; the
  category opener itself prints its count on an ink flag instead, with no page mark.
- The contract says "no radii except pill controls"; the textarea ships at 1rem.
- The contract's first viewport describes the ruled contents row unconditionally; it renders from `lg`
  only, with the contents drawer standing in below that.
- Built and unused, deliberately not canonized: `.tone` (the ink screentone; only `.tone-light` is in
  service), `.pagemark-flood`, `.marker-ink`, `UnderMark`, `--elev-sm`, `--width-wide`, `--font-code`.
- The screenshots in `.impeccable/review/` were taken against locally generated labelled PLACEHOLDER image
  fixtures, not merchant photography.
-->

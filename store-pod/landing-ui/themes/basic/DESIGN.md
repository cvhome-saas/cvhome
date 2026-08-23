---
name: Basic — The Catalogue Page
description: The platform's multi-purpose default — one continuous ruled catalogue of entries with big condensed prices, a thumb-index of categories, and the merchant primary as flat fields on the cover, the active tab and the one action per view.
colors:
  # This theme owns NO palette. Every role below is injected per request from the merchant's ColorTheme
  # preset through the contrast-guarded bridge (libs/theme/src/merchant-bridge.ts); the theme's only colour
  # decisions are mapMerchantColors in src/index.ts and the material mixes in src/tokens.css. Values are
  # the live CSS variables, never hex.
  background: "var(--background)"
  foreground: "var(--foreground)"
  card: "var(--card)"
  border: "var(--border)"
  primary: "var(--primary)"
  primary-foreground: "var(--primary-foreground)"
  muted: "var(--muted)"
  muted-foreground: "var(--muted-foreground)"
  sale: "var(--sale)"
  sale-foreground: "var(--sale-foreground)"
  success: "var(--success)"
  success-foreground: "var(--success-foreground)"
  destructive: "var(--destructive)"
  faint: "color-mix(in srgb, var(--foreground) 8%, transparent)"
  accent: "color-mix(in srgb, var(--background), var(--foreground) 6%)"
  secondary: "color-mix(in srgb, var(--background), var(--foreground) 10%)"
typography:
  display:
    fontFamily: "var(--font-basic-display), var(--font-basic-arabic), var(--font-basic-sans), 'Arial Narrow', sans-serif"
    fontSize: "clamp(3.5rem, 6vw, 6rem)"
    fontWeight: 800
    lineHeight: 0.9
    letterSpacing: "0.005em"
  headline:
    fontFamily: "var(--font-basic-display), var(--font-basic-arabic), var(--font-basic-sans), 'Arial Narrow', sans-serif"
    fontSize: "2.25rem"
    fontWeight: 700
    lineHeight: 1
    letterSpacing: "0.01em"
  title:
    fontFamily: "var(--font-basic-display), var(--font-basic-arabic), var(--font-basic-sans), 'Arial Narrow', sans-serif"
    fontSize: "1.5rem"
    fontWeight: 700
    lineHeight: 0.95
    letterSpacing: "0.005em"
  price:
    fontFamily: "var(--font-basic-display), var(--font-basic-arabic), var(--font-basic-sans), 'Arial Narrow', sans-serif"
    fontSize: "1.25rem"
    fontWeight: 700
    lineHeight: 1
    letterSpacing: "0"
  index:
    fontFamily: "var(--font-basic-display), var(--font-basic-arabic), var(--font-basic-sans), 'Arial Narrow', sans-serif"
    fontSize: "0.875rem"
    fontWeight: 700
    lineHeight: 1
    letterSpacing: "0.04em"
  stamp:
    fontFamily: "var(--font-basic-display), var(--font-basic-arabic), var(--font-basic-sans), 'Arial Narrow', sans-serif"
    fontSize: "0.75rem"
    fontWeight: 700
    lineHeight: 1.1
    letterSpacing: "0.05em"
  body:
    fontFamily: "var(--font-basic-sans), var(--font-basic-arabic), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "0"
  entry-name:
    fontFamily: "var(--font-basic-sans), var(--font-basic-arabic), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 500
    lineHeight: 1.375
    letterSpacing: "0"
  label:
    fontFamily: "var(--font-basic-sans), var(--font-basic-arabic), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 600
    lineHeight: 1.5
    letterSpacing: "0.025em"
  cat-no:
    fontFamily: "var(--font-basic-sans), var(--font-basic-arabic), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.75rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "0.02em"
rounded:
  control: "0.25rem"
  card: "0px"
  image: "0px"
  badge: "0.125rem"
  overlay: "0.5rem"
spacing:
  unit: "0.25rem"
  rule: "1px"
  gutter: "1rem"
  gutter-lg: "2rem"
  section: "3rem"
  section-lg: "5rem"
  header: "3.5rem"
  header-lg: "4rem"
  entry-foot: "0.625rem 0.75rem 0.75rem"
  cell-pad: "1.25rem"
  cell-pad-lg: "1.5rem"
  plate-pad: "1.25rem"
  plate-pad-lg: "2rem"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    padding: "0 1.5rem"
    height: "3rem"
  button-primary-hover:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
  button-shop-now:
    backgroundColor: "{colors.primary-foreground}"
    textColor: "{colors.primary}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    padding: "0 1.25rem"
    height: "2.75rem"
  button-outline:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    padding: "0 0.75rem"
    height: "2rem"
  chip:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    typography: "{typography.entry-name}"
    rounded: "{rounded.control}"
    padding: "0 0.75rem"
    height: "2.25rem"
  chip-hover:
    backgroundColor: "{colors.faint}"
    textColor: "{colors.foreground}"
  chip-on:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
  chip-square:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.control}"
    padding: "0"
    size: "2.25rem"
  index-tab:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.index}"
    rounded: "0px"
    padding: "0 0.875rem"
    height: "2.5rem"
  index-tab-hover:
    backgroundColor: "{colors.faint}"
    textColor: "{colors.foreground}"
  index-tab-active:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
  stamp:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
    typography: "{typography.stamp}"
    rounded: "{rounded.badge}"
    padding: "0.2rem 0.45rem"
  stamp-sale:
    backgroundColor: "{colors.sale}"
    textColor: "{colors.sale-foreground}"
    typography: "{typography.stamp}"
    rounded: "{rounded.badge}"
    padding: "0.2rem 0.45rem"
  stamp-outline:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    typography: "{typography.stamp}"
    rounded: "{rounded.badge}"
    padding: "0.2rem 0.45rem"
  plate:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    rounded: "{rounded.card}"
    padding: "{spacing.plate-pad}"
  entry:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.card}"
    padding: "{spacing.entry-foot}"
  cell:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.card}"
    padding: "{spacing.cell-pad}"
  input-search:
    backgroundColor: "{colors.muted}"
    textColor: "{colors.foreground}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    padding: "0 2.25rem"
    height: "2.5rem"
  page-stub:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    typography: "{typography.index}"
    rounded: "0px"
    padding: "0"
    size: "2.25rem"
  page-stub-active:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
  announcement:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
    typography: "{typography.entry-name}"
    rounded: "0px"
    padding: "0.375rem 1rem"
---

# Design System: Basic — The Catalogue Page

## Overview

**Creative North Star: "One Continuous Ruled Catalogue"**

The store is a mail-order catalogue, not a page of cards. Every product is an entry — photo, name,
catalogue number and a big condensed price in the same fixed slots — and entries share 1px rules instead of
floating: a grid is a ruled spread (`.ruled`), a home group is a ruled rail, the footer is four ruled cells, the
cart is ruled lines. The categories are the book's thumb-index, a strip of uppercase condensed tabs under the
masthead with the current section printed as a flat field of the merchant primary. The world refuses the
default theme's white page of rounded, shadowed cards under a full-bleed slider: here the slider is a ruled
stage capped at 40–58vh with numbered page stubs at its bottom-end, and the store's name stands on a primary
title block overlapping its bottom-start.

The materials are the preset's paper and ink (`--paper` / `--ink` alias the merchant background and
foreground) and the preset's border as the rule. Planes are flat; only floating surfaces (rail arrows, the
search list, menus, drawers) lift, with a real offset and blur. Colour is scarce and flat: the merchant primary
appears only as a plate — the cover title block, the active index tab, the active page stub, the selected
chip, the one primary action per view — never as text or a tint. State is a printed stamp in a fixed slot
(SALE, OUT OF STOCK, ONLY N LEFT, ADDED); in stock is silence on entries. Type is a single super-family: Sofia
Sans for what explains, Sofia Sans Extra Condensed 700/800 uppercase for what names and for every price, Cairo
leading both roles in Arabic. Density is compact (0.9375rem body, 1px gaps), and there is exactly one authored
motion: the price cell floods success-green from the start edge and prints ADDED when the bag takes the add.

**Key Characteristics:**
- Entries in ruled grids and rails (`.ruled`, `.rail`): cells on `--cell` separated by 1px of `--rule`; nothing floats, no card radius, no card shadow.
- The merchant primary only as `.plate` / active `.index-tab` / active page stub / `.chip` on / the one primary button per view, plus the ring, caret, accent-color and `::selection`.
- State as a `.stamp` in a fixed slot: ink stamp by default, `--sale` for SALE, outline for ONLY N LEFT; the buy box prints "In stock" in success text.
- Sofia Sans Extra Condensed 700 uppercase (`.display`, `.running-head-title`, `.index-tab`, `.stamp`) and for every price (`.price`, tabular, one line); Sofia Sans 400–600 for names, facts, prose; Cairo first in `:lang(ar)`.
- One motion, the price flash (`basic-flash`, 1.6s emphasized ease-out, held then released; a still hold under reduced motion); everything else is a 120–200ms colour / opacity transition.
- Square cells and images (0 radius), a hairline softening on controls (0.25rem), stamps at 0.125rem, overlays at 0.5rem.

## Colors

The theme owns no hex. Colour roles arrive on `<html>` per request from the merchant's ColorTheme preset via
the contrast-guarded bridge; the theme re-maps `ring` → primary and demotes `accent` / `secondary` to faint
tonal mixes of background toward foreground (6% / 10%, both with foreground text) so hover surfaces, chips and
quiet badges never introduce a second hue, and mixes one material of its own (`--faint`, foreground at 8%).

### Primary
- **The Plate** (`var(--primary)` on `var(--primary-foreground)`): a flat field and nothing else — the cover title block (`.plate`), the active index tab and the active sub-index chip, the active page stub on the cover stage, the selected option chip (`.chip[aria-checked]`), the one primary `Button` per view (ADD TO CART, CHECKOUT, PLACE ORDER), the flash-free focus ring (`ring` is primary), `caret-color`, `accent-color`, `::selection`, and the underline colour of prose links. SHOP NOW inverts it: primary-foreground field, primary text, on the plate. `--primary-hover` is not used; hover on primary is 90% opacity.

### Neutral
- **Paper** (`var(--background)`, aliased `--paper`): the page, the masthead, the index strip.
- **Cell** (`var(--card)`, aliased `--cell`): every entry and cell inside a ruled grid or rail, the image bed, chips, page stubs, the order summary and gallery frame. Equals paper on light presets; lifted 4% on dark ones by the bridge.
- **Ink** (`var(--foreground)`, aliased `--ink`): all text, the default stamp's field, the announcement band (ink band, paper text), the outline stamp's inset stroke, the scrollbar thumb at 35%.
- **Rule** (`var(--border)`, aliased `--rule`): every line — ruled-grid gaps and frames, the masthead and index-strip base, running heads, the cover stage frame, drawer headers and footers, `divide-y` lists, the pager's top rule, chip and square-button borders, the thumbnail and cart-image frames.
- **Faint** (`--faint` = foreground at 8%): the hover wash on index tabs, chips, rail arrows.
- **Accent / Secondary** (background mixed 6% / 10% toward foreground): the shell's hover surfaces (ghost buttons, menu items, the footer's social squares) and secondary buttons — a tonal step, never a hue.
- **Muted** (`var(--muted)`, at 50%): the search field's bed; the cover stage's bed while its image loads.
- **Print Grey** (`var(--muted-foreground)`): facts — catalogue numbers, counts beside running heads, `<del>` prices, city · since, section labels in the footer and facet legends, the tab count at 70%.

### Status
- **Sale** (`var(--sale)` on `var(--sale-foreground)`, the preset's `error` role via the bridge): the SALE / SAVE N% stamp only. Never a background wider than a stamp, never text.
- **Added Green** (`var(--success)` / `var(--success-foreground)`): the price-flash flood and the ADDED print on an entry; "In stock" text in the buy box. `.stamp-success` is declared but unused — not canonized.
- **Fault** (`var(--destructive)`): field errors and the logout row via the shared primitives; never a surface.

### Named Rules
**The Plate Rule.** The merchant primary is a flat field (`.plate`, active `.index-tab`, active page stub, `.chip` on, the one primary button per view) or the ring / caret / selection. No primary text, no primary rules, no primary tint, no second primary button in a view.
**The Stamp Rule.** State is printed in a fixed slot as a `.stamp`: ink for OUT OF STOCK, `--sale` for SALE, outline for ONLY N LEFT, success for ADDED (the flash). No coloured borders, washes or badges for state; in stock on an entry is silence.
**The One-Hue Rule.** Accent and secondary are tonal mixes of paper toward ink; hover is `--faint` (8%) or 90% opacity; the only chroma besides the primary is sale (stamp) and success (flash, in-stock text).

## Typography

**Display Font:** Sofia Sans Extra Condensed 700 / 800 (variable; with Cairo for Arabic, then Sofia Sans, "Arial Narrow", sans-serif)
**Body Font:** Sofia Sans 400 / 500 / 600 (variable; Latin, Latin-ext, Cyrillic; with Cairo, ui-sans-serif, system-ui, sans-serif)
**Label/Mono Font:** none distinct; labels are Sofia Sans 600 uppercase at 0.75rem; `ui-monospace` is declared as `--font-code` but unused.

**Character:** The mail-order book's bold condensed voice over plain printed explanation. Anything that
names — the store, a running head, a tab, a stamp, a drawer title, a page stub, the language code — is Sofia
Sans Extra Condensed, uppercase, 700 (the cover name at 800), with tabular lining numerals; every price is the
same face at weight 700 on one line. Anything that explains — product names, facts, prose, inputs, buttons —
is Sofia Sans. Arabic pages lead with Cairo on both roles (`:lang(ar)` swaps the stacks, letter-spacing goes to
0) and the Sofia cuts ship without the metric fallback so Arabic glyphs reach Cairo. All three faces are
variable, so nothing is faux-bolded.

### Hierarchy
- **Display** (800, 3rem → 3.75rem at `sm` → `clamp(3.5rem, 6vw, 6rem)` at `lg`, line-height 0.9, uppercase, `overflow-wrap: anywhere`): the store's name on the cover plate (`.display .display-black`), roughly 6× body at its largest. The footer's store name is `.display` at 1.875rem; the wordmark fallback in the masthead is 1.5rem → 1.875rem.
- **Headline** (700, 1.75rem → 2.25rem at `lg`, line-height 1, +0.01em, uppercase, balanced): every running head (`.running-head-title`) — home groups, category and CMS titles, related products. Prose `h2` is 1.75rem, `h3` 1.375rem in the same voice.
- **Title** (700, 1.5rem / 1.25rem, line-height 0.95, uppercase): drawer titles (cart, menu, filters) at 1.5rem; the order summary and empty / error titles at 1.25–1.5rem (`.display`, `font-display`).
- **Price** (700, tabular, nowrap, line-height 1): 1.25rem on an entry, 1rem on a cart line, 1.5rem for subtotals, 2.25rem → 3rem in the buy box; `<del>` original above it in Sofia Sans 0.75–0.875rem print grey.
- **Index** (700, 0.875rem, +0.04em, uppercase, tabular): index tabs, page stubs, the language tab, the pager's "page x of y" (`.display text-sm`); the tab's count at weight 500 / 70%.
- **Stamp** (700, 0.75rem, line-height 1.1, +0.05em, uppercase, tabular): every state.
- **Product Name** (Sofia Sans 500, 0.875rem, line-height 1.375, 2-line clamp, `dir="auto"`): the entry's name slot and the cart line; the buy-box name is Sofia Sans 600 at 1.5rem → 1.875rem, tight tracking.
- **Body** (Sofia Sans 400, 0.9375rem, line-height 1.5; prose 1.65 at ≤70ch via `.prose-basic`): page copy, facts, inputs, messages. Button labels are 600 at 0.9375–1rem.
- **Label** (Sofia Sans 600, 0.75rem, +0.025em, uppercase, print grey): footer column heads, facet legends, the SUBTOTAL label (0.875rem); option legends are 600 sentence case at 0.875rem.
- **Catalogue Number** (`.cat-no`: Sofia Sans 400, 0.75rem, +0.02em, tabular, print grey, nowrap): "No. SKU" on entries, cart lines and the buy box, with the maker before it.

### Named Rules
**The Condensed Voice Rule.** If it names or prices, it is Sofia Sans Extra Condensed 700 uppercase (Cairo in Arabic) with tabular lining numerals; product names, facts and prose are never condensed, and a price is never Sofia Sans.
**The Printed Fact Rule.** Prices come pre-formatted from the API and are never re-formatted; counts, SKUs, dates and phone numbers carry `tabular-nums`; merchant-supplied names sit in `<bdi dir="auto">` and long names break with `overflow-wrap: anywhere`.

## Layout

The catalogue runs at `max-w-content` (84rem) for Home, Category, Product, Checkout and Account
(`container: 'content'`), `max-w-narrow` (44rem) for CMS pages, empty / error / not-found; the gutter is 1rem,
2rem from `lg`. The stretch rhythm is 3rem, 5rem from `lg` (`pt-section` above every home group,
`mt-section` above the footer, `py-section` around narrow pages); inner gaps are 1.5rem (`gap-6`), 2.5–3rem
at `lg` (`lg:gap-10` / `lg:gap-12`).

First viewport: the merchant's announcement as an ink band → the masthead, sticky, 3.5rem (4rem at `lg`),
on paper with a base rule — menu square (below `lg`) · logo (2rem / 2.25rem tall, ≤11rem) or wordmark · search
(from `md`, up to 24–28rem) · language tab / account square / cart square → the index strip, 2.5rem tabs
sharing inline rules, HOME with a leading rule, CMS pages pushed to the end with their own leading rule, scrolling
horizontally with an end fade below `lg` → the cover: the stage at 4:3 / `max-h-40vh` on phones, 16:9 /
50vh at `sm`, 21:9 / 58vh at `lg`, framed by a rule, page stubs on its bottom-end; the plate below it on phones
and absolutely over its bottom-start at `lg` (`min(40rem, 48%)`); with no image at all the plate alone is the
cover at `min-h-45vh` / 52vh → the first running head and its entries.

Entries sit in a `.ruled` grid — 2 / 2 / 3 / 4 columns at base / `sm` / `lg` / `xl` (layout config), 1px
gaps — or a `.rail` (2 / 3 / 4 / 5 per view at 0 / 640 / 1024 / 1280px) when a home group has more than the
grid shows. Product images are 1:1 on `--cell`; the entry foot is `px-3 pb-3 pt-2.5` under a top rule. The
product page is two columns from `lg` (gap 1.5rem / 3rem): the gallery (1:1, `contain`, framed, 4rem / 5rem
thumbnails in a scroll row) and the buy box; the listing has a 14rem facet rail from `lg` and a start-side
drawer below, with the results count and sort on one rule and the pager on a top rule; checkout's summary is a
framed cell sticky at `header-lg + 1rem`. Drawers: cart from the reading end (`sm:max-w-md`), menu and filters
from the reading start (88vw, max 24rem). RTL mirrors via logical properties, `rtl:rotate-180` on directional
icons, a mirrored flash origin and scroll fade.

## Elevation & Depth

A printed page: planes are flat and depth is a rule, not a shadow. `--elev-sm` is transparent by definition.
Only floating surfaces lift, with a real offset and blur — the rail's arrow squares (`--elev-md`), and the
shell's menus, drawers, search list and toasts (`--elev-overlay`). Nothing lifts on hover; hover is a faint
wash or a 90% opacity.

### Shadow Vocabulary
- **Flat** (`--elev-sm`: `0 0 0 0 transparent`): every entry, cell, chip, plate and stamp at rest — the token exists so shared primitives that ask for `shadow-sm` stay flat.
- **Floating control** (`--elev-md`: `0 4px 12px -4px rgb(0 0 0 / 0.12)`): the rail's prev / next squares.
- **Overlay** (`--elev-overlay`: `0 24px 56px -18px rgb(0 0 0 / 0.35)`): the search suggestions list, menus, drawers, toasts (through the shell's `shadow-overlay`).
- `--elev-lg` (`0 10px 28px -10px rgb(0 0 0 / 0.2)`) is declared for the shell and referenced by no theme element — not canonized.

### Named Rules
**The Rule-Not-Shadow Rule.** Separation is a 1px `--rule`; a shadow means the surface is floating over the page (menus, drawers, rail arrows, toasts). Never a shadow on an entry, a cell, a button or a stamp; never a glow or a coloured shadow.
**The Layering Rule.** World classes (`.display`, `.price`, `.ruled`, `.plate`, `.index-tab`, `.stamp`, `.chip`, `.running-head`, `.cat-no`, `.flash`, `.scroll-x`, `.prose-basic`) live in `@layer components`, so a Tailwind utility on the same element (`text-xl`, `size-9`, `p-0`) always wins; only the Swiper overrides (`.cover`, `.rail`) stay unlayered because Swiper's CSS is.

## Shapes

Square cells with hairline-soft controls. Cards, images and overlays' cells are square (`--r-card` /
`--r-image` 0): entries, the cover stage, the gallery, thumbnails, cart images, the footer's cells, drawers'
content. Controls take a 0.25rem softening (`--r-control`): buttons, chips, the add / arrow squares, the
search field, inputs, the footer's social squares, rail arrows. Stamps are 0.125rem (`--r-badge`); popovers,
menus and the search list 0.5rem (`--r-overlay`). Borders are 1px of `--rule` everywhere — the only stroke
that is not the rule is the outline stamp's inset 1px of ink and the unavailable chip's dashed rule. The
ruled grid is drawn by its gap: the container is `--rule` with a 1px frame and 1px gaps, the children are
`--cell`. The index tab and the page stub are rectangles with no radius, sharing 1px inline rules; the page
stubs form a rule-framed block that sits on the stage's bottom-end corner.

## Components

### Buttons
- **Shape:** hairline-soft (0.25rem); heights 2rem (`sm`, outline pager / filters / clear), 2.25rem (default and squares), 2.5rem (`lg`), 2.75rem (SHOP NOW), 3rem (`h-12`: ADD TO CART, CHECKOUT, PLACE ORDER).
- **Primary (shared `Button`):** primary field, primary-foreground Sofia Sans 600 at 0.9375–1rem, no shadow, Lucide 16px icon at the inline start; padding `px-4` / `px-6`. One per view.
- **Shop Now (on the plate):** the inversion — primary-foreground field, primary text, 2.75rem, `px-5`, 600, an arrow-down icon; 90% opacity on hover, outline in primary-foreground on focus.
- **Hover / Focus / Disabled:** hover → 90% opacity (primary) or the accent wash (ghost / outline); focus → the shared 3px ring at primary/50 on a primary border; disabled → 50% opacity, `not-allowed` on the add square.
- **Outline (secondary):** paper field, 1px rule, foreground text — pager, FILTERS, CLEAR FILTERS, RETRY, CONTINUE SHOPPING.
- **Ghost:** masthead squares (menu, account, cart, the language tab in the index voice, the announcement close in paper on ink), the search clear.
- **Quiet:** `variant="link"` — Continue shopping under CHECKOUT in the cart.

### Chips / Stamps
- **Chip (`.chip`):** 2.25rem min height, `px-3`, 1px rule, 0.25rem, cell field, Sofia Sans 500 at 0.875rem; hover → `--faint`; on (`aria-checked`, `aria-current`, `data-on`) → the plate; `.chip-off` → dashed rule, print grey, line-through. Used for option values (`min-w-10`, price delta in 0.75rem at 75%), the category sub-index, and as a `size-9` square for the add (+) / view (→, mirrored in RTL) control on every entry.
- **Stamp (`.stamp`):** ink field, paper text, Sofia Sans Extra Condensed 700 at 0.75rem +0.05em uppercase, `0.2rem 0.45rem`, 0.125rem; stacked at `start-2 top-2` over the photo, 0.25rem apart. `.stamp-sale` in `--sale` ("SAVE 20%" when derivable, else "SALE"; hidden when out of stock); `.stamp-outline` (cell field, inset 1px ink) for "ONLY N LEFT"; OUT OF STOCK is the ink stamp and the photo goes 50% greyscale. In the buy box the stamps sit beside the price; in stock prints as success text.

### Cards / Containers
- **Entry (`ProductCard` in `.ruled` / `.rail`):** a cell on `--cell`, full height, no radius, no shadow: the 1:1 photo (second photo cross-fades in 200ms on hover), stamps at the top-start, then under a top rule the fixed slots — name (Sofia Sans 500, 0.875rem, 2 lines), `.cat-no`, and a foot row with the price (`.price` 1.25rem, `<del>` above) beside the `size-9` add square. The price cell is a `.flash` host: on add it floods success from the start edge, holds, releases over 1.6s and prints ADDED in the price voice.
- **Ruled Grid (`.ruled`):** a `--rule` box with 1px frame and gaps, children on `--cell`; columns 2 / 2 / 3 / 4, also the footer's 1 / 2 / 4 cells at `p-5` / `p-6`.
- **Rail (`.rail`):** the same rules on a Swiper row (slides on `--cell` with an end rule); arrows are 2.5rem rule-framed squares with `--elev-md`, hidden on touch and when disabled.
- **Plate (`.plate`):** primary field, primary-foreground text, `p-5` / `p-6` / `p-8`, 0 radius — the cover title block (name, facts at 85%, SHOP NOW) and the skip link.
- **Cell:** a rule-framed block on `--cell` for the order summary (display head on a base rule, `divide-y` lines, totals on a top rule), the gallery frame, error / empty blocks (`border px-6 py-14`), the facet drawer.

### Inputs / Fields
- **Search:** a 2.5rem field on `muted/50`, no shadow, 0.25rem, `ps-9 pe-9` for the 16px search icon at start and a 1.75rem clear square at end; suggestions drop as a `rounded-overlay` popover with a rule, `--elev-overlay`, `mt-1`, z-50. In the masthead from `md` (24–28rem), full width in the menu drawer.
- **Shared fields:** the shell's `Input` / `Textarea` / `Select` with the theme's 0.25rem radius and 1px `--input` (= border) stroke; focus is the 3px primary/50 ring; errors in destructive text under the field.
- **Option values:** `.chip` radios; **Sort:** a `w-40` small select trigger; **Quantity:** the shared stepper (2.25rem squares, 0.25rem); **Facets:** shared radios / checkboxes under 0.75rem uppercase legends on a base rule.

### Navigation
- **Index strip (signature):** 2.5rem `.index-tab`s, Sofia Sans Extra Condensed 700 at 0.875rem +0.04em uppercase, `px-3.5`, inline end rules, the count at 500 / 70% tabular; hover `--faint`, current page the plate; HOME with a leading rule, CMS pages pushed to the end; scrolls with a masked end fade below `lg`.
- **Sub-index:** `.chip` rows for child categories on the category page.
- **Masthead actions:** ghost squares (Lucide 16px), the language code as a `.display` tab, the cart square with its count; the cart opens as a drawer.
- **Breadcrumbs:** the shared breadcrumb in Sofia Sans 0.875rem with chevrons mirrored in RTL.
- **Mobile:** a start-side drawer (88vw, max 24rem) with a `.display` 1.5rem "Menu" title on a base rule, the search field in a ruled cell, then full-width rows with base rules and tabular counts; children expand in place.
- **Pager:** two outline `sm` buttons on a top rule with "page x of y" in the index voice between them.

### Cover (signature)
The merchant slider as a ruled stage (1px frame, `--muted` bed, 4:3 / 16:9 / 21:9 capped at 40 / 50 / 58vh)
with numbered page stubs — 2.25rem rule-framed squares in the index voice, the current one the plate — on its
bottom-end, and the `.plate` title block with the store name at `display-black` up to 6rem, the store's facts
(city · since year, tabular, 85%) and SHOP NOW, overlapping the stage's bottom-start from `lg` and stacked
under it on phones. With a banner only, the banner is the stage; with no image the plate alone is the cover.

### Running Head (signature)
`SectionHeading`: a base rule with the title in `.running-head-title` at the start, the count in 0.875rem
tabular print grey on the same baseline, an optional subtitle under it, an action at the end; `mb-5`.

### Cart Drawer (signature)
A drawer from the reading end (`sm:max-w-md`): a `.display` 1.5rem title and the items count on a base rule;
lines in a `divide-y` list (4rem rule-framed image, name at 500, `.cat-no`, the stepper, the line total in
`.price` 1rem); a footer on a top rule with SUBTOTAL in 0.875rem uppercase print grey and the subtotal in
`.price` 1.5rem, a full-width 3rem primary CHECKOUT, Continue shopping as a link.

### Announcement (signature)
The merchant's header message as an ink band above the masthead: ink field, paper text at 0.875rem centred with
underlined links, a 2rem ghost close square (paper at 15% on hover). Dismissal persists per session.

### States
- **Empty / Not found / Error:** the shared blocks inside a rule-framed cell (`px-6 py-14`) with the title forced into the display voice at 1.5rem uppercase; error digests in 0.75rem tabular; outline actions.
- **Loading:** skeleton pages on the same ruled grids (`aria-busy`); the listing dims to 60% while refetching.
- **Sold out:** ink stamp, photo 50% greyscale, the add square disabled.

## Do's and Don'ts

### Do:
- **Do** put entries in a `.ruled` grid (2 / 2 / 3 / 4, 1px `--rule` gaps, children on `--cell`) or a `.rail`; keep every entry's slots fixed — photo 1:1, name (2 lines, Sofia Sans 500), `.cat-no`, `.price` beside a `size-9` `.chip` square.
- **Do** reserve the merchant primary for flat fields: `.plate`, the active `.index-tab` / page stub, `.chip` on, and one primary `Button` per view; keep SHOP NOW as the inversion on the plate.
- **Do** print state as a `.stamp` in its slot (ink; `.stamp-sale`; `.stamp-outline`) and let the price cell `.flash` ADDED on add; leave in stock silent on entries.
- **Do** set names, heads, tabs, stamps and every price in Sofia Sans Extra Condensed 700 uppercase with tabular lining numerals (`.display`, `.running-head-title`, `.index-tab`, `.stamp`, `.price`) and explanation in Sofia Sans; wrap merchant strings in `<bdi dir="auto">`.
- **Do** separate with 1px rules (`border`, `border-t`, `divide-y`, `.running-head`) and keep planes flat; use `--elev-md` / `--elev-overlay` only on surfaces that float (rail arrows, menus, drawers, the search list).
- **Do** keep radii to the scale: 0 on cells / images / plates, 0.25rem on controls and chips, 0.125rem on stamps, 0.5rem on overlays.
- **Do** cap the cover stage (4:3 / 40vh, 16:9 / 50vh, 21:9 / 58vh) and let the plate stand alone when the merchant has no image.
- **Do** mirror for RTL with logical properties, `rtl:rotate-180` on directional icons, and trust the `:lang(ar)` font swap (Cairo first, letter-spacing 0 on the condensed voice).

### Don't:
- **Don't** hard-code hex; every colour is a role variable from the merchant bridge or a declared `color-mix` of `--foreground` / `--background` (`--faint`, the accent / secondary mixes in `mapMerchantColors`).
- **Don't** tint or colour state: no primary / sale / success backgrounds wider than a stamp or the flash, no coloured borders, no opacity fades beyond the 50% greyscale sold-out photo, the 50% disabled square and the listing's 60% while loading.
- **Don't** float an entry, cell, button or stamp: no card shadows, no hover lift, no glows; hover is `--faint`, the accent wash or 90% opacity.
- **Don't** add a second motion: `basic-flash` (1.6s, `--easing-emphasized`, start-edge flood, held then released; a still hold under `prefers-reduced-motion`) is the theme's only keyframe; everything else is a ≤200ms transition.
- **Don't** set a price, a head or a tab in Sofia Sans, or a product name / fact / prose in the condensed face; don't request display weights below 700 or set the display in sentence case.
- **Don't** put category navigation in the masthead — it is the index strip; don't give the cover a full-bleed or uncapped slider.
- **Don't** round cells or images, add radii above 0.25rem to controls, or draw a border in anything but `--rule` (the outline stamp's ink inset and the dashed `.chip-off` are the only exceptions).

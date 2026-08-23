---
name: Fashion — The Wheatpaste Wall
description: A streetwear / drops store as a fly-posted wall — a rendered ground, every product and slider image a pasted paper sheet, the merchant's primary as day-glo paper, state as rubber stamps.
colors:
  # This theme owns NO palette. Every role below is injected per request from the merchant's ColorTheme
  # preset through the contrast-guarded bridge (libs/theme/src/merchant-bridge.ts); the theme's only colour
  # decisions are mapMerchantColors in src/index.ts and the material mixes in src/tokens.css. Values are
  # the live CSS variables, never hex.
  background: "var(--background)"
  foreground: "var(--foreground)"
  primary: "var(--primary)"
  primary-foreground: "var(--primary-foreground)"
  primary-hover: "var(--primary-hover)"
  muted-foreground: "var(--muted-foreground)"
  sale: "var(--sale)"
  destructive: "var(--destructive)"
  wall: "color-mix(in srgb, var(--background) 86%, var(--foreground))"
  rule: "color-mix(in srgb, var(--foreground) 18%, transparent)"
  input-stroke: "color-mix(in srgb, var(--foreground) 45%, transparent)"
  faint-print: "color-mix(in srgb, var(--foreground) 26%, transparent)"
typography:
  display:
    fontFamily: "var(--font-fashion-display), var(--font-fashion-arabic), var(--font-fashion-sans), 'Arial Narrow', sans-serif"
    fontSize: "4.5rem"
    fontWeight: 400
    lineHeight: 0.88
    letterSpacing: "normal"
  headline:
    fontFamily: "var(--font-fashion-display), var(--font-fashion-arabic), var(--font-fashion-sans), 'Arial Narrow', sans-serif"
    fontSize: "3.25rem"
    fontWeight: 400
    lineHeight: 0.9
    letterSpacing: "normal"
  title:
    fontFamily: "var(--font-fashion-display), var(--font-fashion-arabic), var(--font-fashion-sans), 'Arial Narrow', sans-serif"
    fontSize: "1.125rem"
    fontWeight: 400
    lineHeight: 0.95
    letterSpacing: "normal"
  strip:
    fontFamily: "var(--font-fashion-display), var(--font-fashion-arabic), var(--font-fashion-sans), 'Arial Narrow', sans-serif"
    fontSize: "0.8125rem"
    fontWeight: 400
    lineHeight: 1
    letterSpacing: "0.05em"
  stamp:
    fontFamily: "var(--font-fashion-display), var(--font-fashion-arabic), var(--font-fashion-sans), 'Arial Narrow', sans-serif"
    fontSize: "0.8125rem"
    fontWeight: 400
    lineHeight: 1
    letterSpacing: "0.08em"
  body:
    fontFamily: "var(--font-fashion-sans), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "0"
  label:
    fontFamily: "var(--font-fashion-sans), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.6875rem"
    fontWeight: 400
    lineHeight: 1.15
    letterSpacing: "0.08em"
rounded:
  control: "0px"
  card: "0px"
  image: "0px"
  badge: "0px"
  overlay: "0px"
spacing:
  unit: "0.25rem"
  gutter: "1rem"
  gutter-lg: "2rem"
  section: "2.5rem"
  section-lg: "4rem"
  header: "3.5rem"
  header-lg: "4rem"
  sheet-pad: "1.25rem"
  sheet-pad-lg: "1.75rem"
  poster-foot: "0.625rem 0.75rem 0.75rem"
  grid-gap: "0.75rem 1.25rem"
  grid-gap-sm: "1rem 1.5rem"
components:
  button-glo:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.strip}"
    rounded: "{rounded.control}"
    padding: "0 1.25rem"
    height: "2.75rem"
  button-glo-hover:
    backgroundColor: "{colors.primary-hover}"
    textColor: "{colors.primary-foreground}"
  button-glo-buy:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.strip}"
    rounded: "{rounded.control}"
    padding: "0 1.5rem"
    height: "3rem"
  button-glo-square:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    rounded: "{rounded.control}"
    padding: "0"
    size: "2.25rem"
  button-strip:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.strip}"
    rounded: "{rounded.control}"
    padding: "0 0.75rem"
    height: "2.25rem"
  button-strip-hover:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
  button-strip-on:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
  cart-stub:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.strip}"
    rounded: "{rounded.control}"
    padding: "0 0.625rem"
    height: "2.25rem"
  input-pencil:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    padding: "0 0.75rem"
    height: "2.25rem"
  chip-stamp:
    backgroundColor: "transparent"
    textColor: "{colors.foreground}"
    typography: "{typography.stamp}"
    rounded: "{rounded.badge}"
    padding: "0.28em 0.5em 0.22em"
  chip-stamp-sale:
    backgroundColor: "transparent"
    textColor: "{colors.sale}"
    typography: "{typography.stamp}"
    rounded: "{rounded.badge}"
    padding: "0.28em 0.5em 0.22em"
  chip-count-stub:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
    typography: "{typography.label}"
    rounded: "{rounded.badge}"
    padding: "0 0.75rem"
    height: "2rem"
  card-sheet:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.card}"
    padding: "{spacing.sheet-pad}"
  card-product-poster:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.card}"
    padding: "{spacing.poster-foot}"
  nav-strip:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.strip}"
    rounded: "{rounded.control}"
    padding: "0 0.75rem"
    height: "2.25rem"
  nav-strip-hover:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
---

# Design System: Fashion — The Wheatpaste Wall

## Overview

**Creative North Star: "Posters Stay Up"**

A streetwear / drops store as a fly-posted wall. The ground is a material, not a colour: the merchant's
background mixed 14% toward its foreground, with a fixed fractal grain and a faint vignette from the top.
Everything that matters is a sheet of paper pasted on that wall — the store's name sheet, every slider
image as a big peeling poster, every product as a poster with its price printed on the foot, every label
and nav item as a strip. Sheets take a small deterministic tilt (never more than 1.2°) and an offset soft
shadow, so no two neighbours are parallel and the goods are the page; nothing is drawn around them. The
world refuses the black-and-white streetwear brutalism of a full-bleed lookbook hero over a four-column
grid, and it refuses carousels: each merchant image gets its own place on the wall.

Two things are not paper. The merchant's primary is the day-glo paper stock — SHOP NOW, the add square, the
cart stub, checkout, the selected option, the active tab — and nothing else on the wall is that colour.
State is a rubber stamp overprinted on the sheet (SALE / SAVE 20%, SOLD OUT, ONLY N LEFT, ADDED, order
status, the receipt verdict), rotated −7°, multiplied into the paper on light presets; never a tint, never a
background wash. Type is poster caps (Anton; Changa 800 in Arabic) for anything that names and Rubik for
anything that explains. There are no radii and no borders except pencil rules; density is tight on the wall
(posters butt at 0.75–1rem) and loose between stretches (2.5rem / 4rem of bare wall).

**Key Characteristics:**
- A rendered wall (`--wall`, grain, vignette) under paper sheets in the merchant's background colour; depth is paper on a wall (offset soft shadow), not a line.
- Deterministic tilts ≤1.2° from the `.wall` nth-child table (±0.4° in `.wall-calm`), mirrored in RTL; the peel corner on big posters.
- The merchant primary only as day-glo paper on primary actions and live states; the merchant `sale` role only on the SALE stamp and the failed verdict.
- State is a stamp (3px border, −7°, `mix-blend-mode: multiply` on light), in stock is silence; sold-out sheets bleach under their stamp.
- Poster caps (Anton 400 / Changa 800) for names, strips, stamps and labels; Rubik for facts, prices and prose; tabular numerals everywhere a count or price appears.
- Exactly two authored motions: the stamp slap (480ms) and the cart-stub bump (480ms); everything else is a ≤180ms transition.

## Colors

The theme owns no hex. Colour roles arrive on `<html>` per request from the merchant's ColorTheme preset via
the contrast-guarded bridge; the theme re-maps four roles (`ring` → primary, `accent` → background mixed 8%
toward foreground, `accentForeground` → foreground, `secondary` → foreground) and mixes its two materials
(wall, rule) from background and foreground with `color-mix`.

### Primary
- **Day-Glo Paper** (`var(--primary)` on `var(--primary-foreground)`, hover `var(--primary-hover)`): the `.glo` stock and nothing else — SHOP NOW, the add square on every poster, the buy box ADD strip, the cart stub, CHECKOUT, PLACE ORDER, the selected option value (`.strip-on`), the active customer tab, the success-dialog and receipt stamps (`.stamp-glo`, an outline only), the ADDED stamp, the selected gallery thumbnail's 3px outline, the order-history dots, link underlines on hover (3px under card titles, 2px in prose), the focus ring (`ring` is primary), `caret-color`, `accent-color`, `::selection`, the Swiper active bullet, the toast check and progress line, and the announcement tape.

### Neutral
- **Paper** (`var(--background)`, aliased `--paper`): every sheet and strip, the image bed behind posters, the footer's social squares.
- **Ink** (`var(--foreground)`, aliased `--ink`): all text, the stamp border and print, strip/menu hover inversion, count stubs and the current breadcrumb (ink strip, paper text), the footer baseboard, `secondary` by policy, the checkbox/radio border.
- **The Wall** (`--wall` = background 86% + foreground 14%, plus `--grain` and a 10% top vignette): the page ground, the header bar at 90% with `backdrop-blur-sm`, the cart / nav / filter drawers (with grain). `--wall-deep` (76/24) is declared but unused — not canonized.
- **Pencil** (`--rule` = foreground at 18%): the only border colour — accordion items, table rows, the order-history spine, summary totals (`.rule`).
- **Pencil Stroke** (foreground at 45%): the input / textarea / select box; the quantity stepper buttons at `border-foreground/40`.
- **Print Grey** (`var(--muted-foreground)`): facts — brand lines, SKU, city · year, item counts, `<del>` prices, in-stock text, table heads, notes, the search icon.
- **Faint Print** (foreground at 26%, `.typo-faint`): the product name as a watermark-weight graphic on a typographic poster when the foot already names the product.

### Status (stamps, not tints)
- **Sale Red** (`var(--sale)`, the preset's `error` role via the bridge): the SALE / SAVE N% stamp and the failed-payment / error-title stamp only. Never a background.
- **Fault** (`var(--destructive)`): field error text and the logout row; never a surface.
- In stock is silence: poster shows nothing; the buy box prints "In stock" in print-grey poster caps. `success` is not used.

### Named Rules
**The Day-Glo Rule.** The merchant primary appears only as day-glo paper on primary actions and live states (`.glo`, `.strip-on`, the ring, the ADDED/success stamp outline, link underlines on hover). No primary text, no primary rules, no primary surface wider than a control.
**The Stamp Rule.** State is an overprint, never a tint: a stamp on the sheet (SALE, SOLD OUT, ONLY N LEFT, ADDED, status), a bleached sheet under SOLD OUT, a line-through on an unavailable option. No coloured backgrounds, badges or washes for state.
**The Paper Rule.** Sheets and strips are the only containers; they are the merchant background on the wall with an offset shadow. The only borders are pencil rules at 18% and the 45% input box.

## Typography

**Display Font:** Anton 400 only (with Changa 800 for Arabic, then Rubik, "Arial Narrow", sans-serif)
**Body Font:** Rubik 400/500/700 (Latin, Latin-ext, Cyrillic, Arabic; with ui-sans-serif, system-ui, sans-serif)
**Label/Mono Font:** none distinct; labels are Rubik or Anton at their smallest; `ui-monospace` is declared as `--font-code` but unused.

**Character:** Fly-poster caps over plain printed explanation. Anything that names — the store, a group, a
product, a nav zone, a control, a state — is uppercase Anton at weight 400 (Anton has one face; Changa 800
resolves at the same request so the Arabic display is never faux-bolded). Anything that explains or counts —
prose, prices, SKUs, facts, inputs — is Rubik, with `tabular-nums` on every number.

### Hierarchy
- **Display** (400, 4.5rem → 6rem at `sm` → `clamp(3rem, 5vw, 5.25rem)` at `lg`, line-height 0.88, uppercase, `overflow-wrap: anywhere`): the store's name on the headline sheet only. The typographic poster sets the same face at `clamp(0.75rem, 13cqi, 5.5rem)`, line-height 0.86, 3–4 line clamp.
- **Headline** (400, 3.25rem → 4.5rem at `sm`, line-height 0.9, uppercase): category and CMS page titles on their sheets. The product name in the buy box is 2.5rem → 3.25rem; the footer's store name is 2.5rem → 3.25rem.
- **Page Strip Title** (400, 1.75rem → 2.5rem at `sm`, on a 3rem strip): Checkout and Account titles; the order-details title at 1.375rem on a 2.75rem strip.
- **Title** (400, 1.125rem, line-height 0.95, uppercase, 2-line clamp): product poster names; sheet headings (order items, details accordion triggers, cart subtotal, content `h2` at 1.75rem / `h3` at 1.375rem). Cart line names are 0.9375rem.
- **Section Heading** (400, 0.9375rem → 1.125rem at `sm`, on a 2.5rem / 2.75rem strip): every group / shelf label.
- **Strip** (400, 0.8125rem, line-height 1, +0.05em, uppercase): nav, buttons, option values, tabs, breadcrumbs (0.6875rem), drawer titles (0.9375rem), dialog titles (1.75rem).
- **Stamp** (400, 0.8125rem, +0.08em, uppercase, 3px border; `-lg` 1.75rem / 4px / +0.06em; `-xl` 3.25rem / 5px / +0.04em): every state.
- **Price** (Rubik 700, 0.9375rem on posters, 2.5rem in the buy box, 0.8125rem in the cart line; `<del>` original in print grey): always Rubik, never display.
- **Body** (Rubik 400, 0.9375rem, line-height 1.5; prose 1.65 at ≤70ch): page copy, error causes, messages, inputs.
- **Label** (Rubik 400, 0.6875rem, +0.08em, uppercase): brand lines, SKU, facts, item counts; field labels are the same size in Anton. Brand in the buy box / city · year run +0.2em (not canonized).

### Named Rules
**The Poster Caps Rule.** If it names, it is Anton (Changa in Arabic), uppercase, weight 400. Never request 500/600/700 of the display face, never set display in sentence case.
**The Printed Fact Rule.** Prices, counts, SKUs, dates and prose are Rubik with `tabular-nums`; the slide counter is zero-padded `01 / 05` in LTR; long names get `overflow-wrap: anywhere` and `<bdi dir="auto">`.

## Layout

The wall runs wide: `PageShell` centres at `max-w-wide` (100rem) for Home, Category and Product
(`container: 'wide'`), `max-w-content` (84rem) for Checkout / Account / Order, `max-w-narrow` (44rem) for
CMS pages, receipts, empty / error / not-found. Gutter is 1rem, 2rem from `lg`; the stretch rhythm is
2.5rem, 4rem from `lg` (`pt-section` between stretches, `h-section` of bare wall at the foot of Home);
inner page gaps are 1.5rem (2rem at `lg`).

The hero wall is a 2-column grid at base (gap 0.75rem, 1rem from `sm`) and 12 columns from `lg`: the
headline sheet spans both columns / 5, the first slider image as a 4:5 peeling poster takes 1 column / 5,
and the first product poster takes 1 column / 2 beside it (with no product the poster is 16:10 across 2 /
7; with no slide the product is 2 / `sm` 1 / 3; with neither, the banner as a 21:9 sheet). Posters stretch
to row height at `lg` (`lg:aspect-auto`). Every stretch after that is a label strip + count stub and a
`ProductGrid`: 2 / 3 / 4 / 6 columns at base / `sm` / `lg` / `xl` (`gap-x-3 gap-y-5`, `sm:gap-x-4
sm:gap-y-6`); when a stretch is led by the next slider image, that poster spans 2 columns × 2 rows at the
start. Slider images left over get their own 1 / 2 / 3 stretch at 4:3. Product images are 4:5.

The product page is 7fr / 5fr from `lg` (gap 1.5rem / 2.5rem): the gallery poster (4:5, `contain`, 4:3
with no image) with 5rem thumbnail sheets in a scrolling row, and the spec sheet sticky at
`header-lg + 1.5rem`. Checkout is 2 / 1 from `lg` with the summary sheet sticky likewise; the category
listing has a 15rem filter sheet rail from `lg` and a start-side drawer below.

Header is sticky, 56px (64px at `lg`), the wall at 90% with a blur: menu strip (below `lg`) · wordmark
strip (−1°, 2.25rem / 2.5rem, capped at 44vw / 60vw) · nav strips (from `lg`, alternating ±0.5–0.9°) ·
search strip (from `md`, 16rem) · language / account strips (2.25rem squares) · the day-glo cart stub.
Drawers: cart from the reading end (`sm:max-w-md`), nav and filters from the reading start (88vw, max 24rem),
all on the wall with grain. RTL mirrors via logical properties, `rtl:rotate-180` on directional icons,
`--tilt-sign: -1` on every sheet / strip, and a mirrored peel gradient.

## Elevation & Depth

Paper on a wall. Every sheet and strip sits on the rendered wall with an offset, soft, two-layer shadow;
depth is the paper's thickness, never a line. A lifted sheet (`.sheet-lift`, product posters and gallery
thumbnails) rises 3px and takes the large shadow on hover / focus-within (pointer devices only). A sheen
(`.sheen::after`, a 115° wash to 4.5% black from the corner) sits on the big posters and spec sheets as the
paste brush's mark. The peel corner (`.peel::before`, 2.5rem, with a 2px drop shadow) lifts at the reading-end
foot of hero, gallery, receipt and error posters; on dark presets it catches light (white mixes) instead of
shadow.

### Shadow Vocabulary
- **Flat sheet** (`--elev-sm`: `0 1px 2px rgb(0 0 0 / 0.14), 0 3px 8px -2px rgb(0 0 0 / 0.16)`): strips, `.glo`, cart lines, gallery thumbnails (`.sheet-flat`).
- **Sheet** (`--elev-md`: `0 2px 4px rgb(0 0 0 / 0.14), 0 12px 28px -10px rgb(0 0 0 / 0.32)`): every `.sheet` at rest — posters, name sheets, spec sheet, menus, dialogs.
- **Lifted** (`--elev-lg`: `0 4px 8px rgb(0 0 0 / 0.16), 0 24px 48px -16px rgb(0 0 0 / 0.38)`): `.sheet-lift` on hover; toasts.
- **Overlay** (`--elev-overlay`: `0 30px 60px -20px rgb(0 0 0 / 0.5)`): declared for the shell's overlay layer; no theme element references it directly — not canonized.

### Named Rules
**The Paper Thickness Rule.** Shadows only say "this is paper on the wall": rest = sheet, hover = lifted, pressed `.glo` = none (`translate: 0 1px`). No glows, no coloured shadows, no shadow as a border.
**The Layering Rule.** World classes (`.sheet`, `.strip`, `.glo`, `.stamp`, `.peel`, `.wall`, `.typo-poster`, `.typo-faint`) live in `@layer components`, so a Tailwind utility on the same element (`h-9`, `text-lg`, `bg-foreground`, `lg:sticky`, `[--tilt:0deg]`) always wins.

## Shapes

Cut paper. Every radius token is 0 (`--r-control/card/image/badge/overlay`) and shared primitives are
forced square (`rounded-none` on drawers, dialogs, menus, selects, inputs, steppers, skeletons, checkboxes,
toast check). The only rotated geometry is the tilt — sheets and strips rotate by `--tilt × --tilt-sign`,
set per element (`[--tilt:-0.7deg]`) or by the `.wall` table (−1.1° / 0.8° / −0.5° / 1.2° / −0.9° / 0.6°
over six children; `.wall-calm` is −0.4° / 0 / 0.4° over three) — and the stamp's −7° (−5° for stamped
titles; one-off −2° to −4° on table and order stamps via `[rotate:…]`, not canonized). Borders are pencil
rules at 18% or the 45% input box; the stamp border is 3–5px of `currentColor`. The peel is a 2.5rem
square gradient, the tape a 2px white line every 48px, the typographic poster a −45° hatch (5% foreground,
2px on 14px) under a 6% foot gradient. Images are square-cut; the product bed is 4:5.

## Components

### Buttons
- **Shape:** square (0 radius); heights 2.25rem (`h-9`, squares and strips), 2.75rem (`h-11`, SHOP NOW, retry, dialog actions), 3rem (`h-12`, ADD TO CART, CHECKOUT, PLACE ORDER, receipt actions).
- **Day-Glo (`.glo`, primary):** primary fill, primary-foreground text, flat-sheet shadow, Anton 0.8125rem–1rem uppercase +0.06em, icon gap 0.5rem (Lucide 16px, 20px in the buy box); padding `px-5` / `px-6` / `px-8`; the add square is `size-9` with a Plus (Arrow when the product has variants, mirrored in RTL).
- **Hover / Active / Disabled:** hover → `primary-hover` (100ms); active → 1px down, shadow off; disabled → 45% opacity, `not-allowed`.
- **Strip (`.strip .strip-hover`, secondary):** paper strip, 2.25rem, `px-3`, Anton 0.8125rem +0.05em uppercase, flat-sheet shadow, optional tilt; hover / open / `aria-current` invert to ink on paper. Used for nav, secondary CTAs, pager, breadcrumbs (2.25rem → 1.75rem, `px-2`, 0.6875rem), filters, Continue shopping, Reject / Cancel.
- **On (`.strip-on`):** the strip in day-glo — the selected option value, the active account tab.
- **Quiet:** underlined Rubik link, primary 2px underline on hover (Continue shopping in the drawer, terms link).
- **Focus:** 2px `outline-ring` (primary) offset 2px on nav strips and the search strip; the shared focus ring elsewhere.

### Chips / Stamps (`ProductBadges`, `.stamp`)
- **Stamp:** 3px `currentColor` border, Anton 0.8125rem +0.08em uppercase, padding `0.28em 0.5em 0.22em`, −7°, 92% opacity, `pointer-events: none`, `mix-blend-mode: multiply` on light presets. Stacked at `start-2 top-3` on a poster, gap 0.5rem.
- **Sale:** `.stamp-sale` in `--sale`, "SAVE 20%" when derivable, else "SALE"; hidden when sold out.
- **Sold out:** ink stamp; the poster's image box gets `.bleached` (saturate 0.2, contrast 0.85, brightness 1.05, 70% opacity) and the add square disables.
- **Low stock:** ink stamp "ONLY N LEFT" (−3° in the buy box, not canonized).
- **Large:** `.stamp-lg` (1.75rem, 4px) in the buy box and on the success dialog; `.stamp-xl` (3.25rem, 5px) for the receipt verdict and not-found / error titles (`.stamp-title`, `.stamp-title-xl`, `.stamp-title-sale` wrap the shared blocks' string titles).
- **Count stub:** an ink strip with paper text, 2rem tall, Rubik 0.6875rem tabular — section counts, results count, items-in-bag.

### Cards / Containers
- **Sheet (`.sheet`):** paper, ink text, sheet shadow, `rotate` by tilt; 0 radius; no border. Padding 1.25rem (`p-5`), 1.75rem from `sm` (`p-7`), 2rem for the headline sheet at `lg`; 1rem for cart / address / filter sheets, 2.5rem for CMS pages at `sm`, 3rem for the receipt at `sm`. Menus, selects and dialogs are sheets with `[--tilt:0deg]`.
- **Product Poster (`ProductCard`):** `.sheet .sheet-lift`, full height; a 4:5 image box on paper (second image cross-fades in 180ms on hover; `PosterImage` swaps a missing / 404 picture for a typographic poster with the faint name), stamps at the top-start, the name in Anton 1.125rem (2-line clamp, hover underline primary 3px / offset 4px), the brand in 0.6875rem print grey, and a foot (`px-3 pb-3 pt-2.5`) with the bold 0.9375rem price (`<del>` original above) beside a `size-9` day-glo square. ADDED stamp (`.stamp-glo .stamp-lg .stamp-slap`) centred over the image for 1.8s after the bag takes the add.
- **Slide Poster (`SlidePoster`):** `.sheet .sheen .peel` at the given ratio (4:5 in the hero, `auto` when leading a grid, 4:3 for leftovers); the image, or a typographic poster of the store name in faint print with the `01 / 05` counter as meta.
- **Headline Sheet (`HeadlineSheet`):** `.sheet .sheen .peel`, city · year in Anton 0.6875rem +0.2em print grey, the H1, SHOP NOW in day-glo (2.75rem, 0°) with up to two group strips (±0.7–0.8°), the item count · currency.
- **Typographic Poster (`.typo-poster`):** the designed no-image fallback — hatch + foot gradient, content bottom-aligned (`align="start"` in the gallery), the title at `13cqi`, meta truncated below. Intentional: merchant images 404 locally and the wall never shows a broken image or a grey box.

### Inputs / Fields
- **Style:** shared `Input` / `Textarea` / `SelectTrigger` as a pencil box — 0 radius, 45% foreground stroke, paper fill, no shadow; labels Anton 0.6875rem +0.08em uppercase (Rubik sentence case for checkbox / radio labels).
- **Focus:** primary border + `0 0 0 3px` primary at 35%.
- **Error:** Rubik 0.8125rem in `destructive` under the field, `aria-invalid` on the control.
- **Search:** a 2.25rem strip (16rem from `md`, full width in the mobile nav) with a 16px search icon at start, Rubik 0.8125rem input, a 1.75rem clear square (inverts on hover); suggestions drop as a sheet (18rem, `mt-2`, z-50) with rows inverting on hover / focus and the kind in Anton 0.6875rem at 70%.
- **Option values:** `role="radio"` strips alternating ±0.5–0.6°, `min-w-10`; selected = `.strip-on`; unavailable = print grey, line-through, 70%.
- **Sort:** the shared select trigger dressed as a 2.25rem strip (`w-40`, 0°); items Anton uppercase on a sheet.
- **Quantity:** the shared stepper with buttons forced square and `border-foreground/40`.

### Navigation
- **Desktop:** category strips (2.25rem, ±0.5–0.9° from a five-step table) that invert on hover / open; children open on a 0° sheet (16rem, 30rem two-column from `md`, `p-2`) with a "View all in …" row in Anton 0.9375rem and children in Rubik 0.8125rem with a 70% tabular count, rows inverting on hover.
- **Header actions:** 2.25rem strip squares (16px Lucide), inverting on hover; menus are 0° sheets with Anton 0.8125rem uppercase rows inverting on highlight; logout in destructive.
- **Cart stub:** `.glo h-9` with a 16px bag and a `min-w-[1.25ch]` tabular count that `.bump`s when it grows (never on hydration).
- **Breadcrumbs:** 1.75rem strips (0.6875rem) for ancestors, chevrons mirrored in RTL, the current page as an ink strip.
- **Mobile:** a start-side drawer on the wall; a "Menu" strip title (−1°), the search strip, then `.wall-calm` 2.5rem full-width strips (accordion squares for children, child strips 2.25rem at 0.8125rem).

### Section Heading (signature)
A label strip pasted at the start of a stretch: the title in Anton 0.9375rem → 1.125rem on a 2.5rem →
2.75rem strip at −0.6°, a 2rem ink count stub at +0.9° (`aria-hidden`), an optional action at the end;
`mb-5`. Used as `h1` / `h2` / `h3`.

### Announcement (signature)
Day-glo tape across the top: primary fill with the 48px tape lines, the notice in Anton 0.8125rem uppercase
centred with underlined links, a 1.75rem close square (15% primary-foreground on hover). Dismissal persists
per session.

### Cart Drawer (signature)
A stretch of wall from the reading end: a "Bag" strip title (−1°) and an ink count stub (+1°); lines as flat
sheets (`p-2.5`, 5rem thumbnail, Anton 0.9375rem name, stepper, bold tabular total, a 1.75rem remove square
inverting on hover) in a `.wall-calm` list; the total on a +0.4° sheet with the subtotal in Anton 1.125rem
and a full-width 3rem CHECKOUT in day-glo; Continue shopping as a quiet link.

### Toasts
Square paper with the lifted shadow, message in Anton 0.8125rem +0.05em uppercase, the check square and
the progress line in day-glo; no check animation.

### States
- **Empty:** a sheet (`max-w-md`, −0.5°) with the title as a −5° stamp (`.stamp-title`, 1.75rem / 4px), Rubik body, strip action.
- **Not found / Error:** a narrow `.sheet .sheen .peel` with the title as an `-xl` stamp (3.25rem / 5px; sale colour for errors), the cause in Rubik, the reference digest in 0.6875rem tabular, a day-glo Back home / Retry plus a 0° strip.
- **Receipt:** the real order status as a centred stamp — `.stamp-xl .stamp-glo` paid, plain pending, `.stamp-sale` failed — on a peeling sheet.
- **Redirecting:** a strip with a spinning 16px loader in primary.
- **Skeletons:** square `Skeleton` blocks inside sheets on the same `.wall` grids (`aria-busy`); strip skeletons are 2.5rem bars.

## Do's and Don'ts

### Do:
- **Do** build every container as a `.sheet` or `.strip` (merchant background on the wall, offset shadow, 0 radius) and put groups of them in a `.wall` (or `.wall-calm`) so the tilt table runs; override a single element with `[--tilt:…]`, never with `rotate-*`.
- **Do** reserve the merchant primary for `.glo` / `.strip-on` / the ring / the ADDED-and-success stamp outline / hover underlines; keep `.glo` at 2.25 / 2.75 / 3rem heights.
- **Do** show state as a `.stamp` (sale in `--sale`, everything else ink), bleach a sold-out sheet, and leave in-stock silent (print-grey caps in the buy box only).
- **Do** set names, strips, stamps and labels in Anton uppercase at weight 400 (Changa 800 in Arabic) and facts, prices, prose and inputs in Rubik with `tabular-nums`; wrap merchant names in `<bdi dir="auto">` with `overflow-wrap: anywhere`.
- **Do** keep the grid at 2 / 3 / 4 / 6 with `gap-x-3 gap-y-5` (`sm:gap-x-4 sm:gap-y-6`), posters at 4:5, the hero at 5 / 5 / 2 on `lg`, and stretches 2.5rem / 4rem apart on bare wall.
- **Do** let a missing or failed image fall back to `PosterImage`'s typographic poster (faint name when the foot names the product, ink when it does not); never a grey box or a broken image.
- **Do** force shared primitives into the world: `rounded-none border-0` on drawers, dialogs, menus, selects, skeletons; drawers on `bg-(--wall)` with `[background-image:var(--grain)]`; dialogs and menus as 0° sheets.
- **Do** mirror for RTL with logical properties, `rtl:rotate-180` on directional icons, and trust `--tilt-sign` / the peel's RTL gradient.

### Don't:
- **Don't** hard-code hex; every colour is a role variable from the merchant bridge or a `color-mix` of `--background` / `--foreground` declared in `tokens.css`.
- **Don't** tint state: no primary / sale / destructive / success backgrounds or badges, no coloured borders, no opacity fades beyond `.bleached` (70%), `.glo:disabled` (45%) and the listing's 60% while loading.
- **Don't** tilt past the table (≤1.2° on sheets, ±0.4° in calm walls) or stamp at anything but −7° / −5° — bigger skews turn the wall into kitsch.
- **Don't** add radii, borders or dividers: pencil `.rule` at 18% and the 45% input stroke are the only lines on the wall.
- **Don't** add a third motion: the stamp slap (`fashion-slap`, 480ms emphasized) and the cart-stub bump (`fashion-bump`, 480ms emphasized) are the theme's only keyframes, both off under `prefers-reduced-motion` (the wall's fixed background also goes static); everything else is a ≤180ms colour / translate / opacity transition.
- **Don't** put a carousel on the wall: each slider image is pasted once (hero, group lead, leftover stretch).
- **Don't** "fix" the intentional exceptions: typographic posters are the designed no-image fallback (merchant images 404 locally); in-stock is silence / muted caps; `.typo-faint` watermark names on product posters are `aria-hidden` decoration.
- **Don't** request display weights other than 400, set display in sentence case, or put a price / count in Anton.

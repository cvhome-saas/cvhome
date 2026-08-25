---
name: Grocery — Cash & Carry
description: The shop as a working warehouse floor — products stacked in molded crates with stepper quick-add, prices as printed signage on the merchant primary, categories as an aisle-board strip, state as stickers slapped on, and a basket drawer that shows the load.
colors:
  # This theme owns ONE palette: its default (src/colors.ts, generated from THEME_DEFAULTS.grocery in
  # libs/types/scripts/build-color-schemas.mjs), rendered when the merchant's ColorTheme is DEFAULT or unset.
  # A fixed preset the merchant picks replaces it whole; either way every role below is injected per request
  # through the contrast-guarded bridge (libs/theme/src/merchant-bridge.ts). The theme's only other colour
  # decisions are mapMerchantColors in src/index.ts (ring → primary, nothing else) and the material mixes in
  # src/tokens.css. Values are the live CSS variables, never hex.
  background: "var(--background)"
  foreground: "var(--foreground)"
  card: "var(--card)"
  border: "var(--border)"
  primary: "var(--primary)"
  primary-foreground: "var(--primary-foreground)"
  primary-hover: "var(--primary-hover)"
  secondary: "var(--secondary)"
  secondary-foreground: "var(--secondary-foreground)"
  muted: "var(--muted)"
  muted-foreground: "var(--muted-foreground)"
  sale: "var(--sale)"
  sale-foreground: "var(--sale-foreground)"
  success: "var(--success)"
  success-foreground: "var(--success-foreground)"
  destructive: "var(--destructive)"
  faint: "color-mix(in srgb, var(--foreground) 7%, transparent)"
  line-strong: "color-mix(in srgb, var(--foreground) 55%, var(--background))"
typography:
  display:
    fontFamily: "var(--font-grocery-display), var(--font-grocery-arabic), var(--font-grocery-sans), 'Arial Narrow', sans-serif"
    fontSize: "clamp(3.5rem, 5.5vw, 6rem)"
    fontWeight: 800
    lineHeight: 0.92
    letterSpacing: "0.01em"
  headline:
    fontFamily: "var(--font-grocery-display), var(--font-grocery-arabic), var(--font-grocery-sans), 'Arial Narrow', sans-serif"
    fontSize: "2.375rem"
    fontWeight: 800
    lineHeight: 0.92
    letterSpacing: "0.01em"
  title:
    fontFamily: "var(--font-grocery-display), var(--font-grocery-arabic), var(--font-grocery-sans), 'Arial Narrow', sans-serif"
    fontSize: "1.75rem"
    fontWeight: 800
    lineHeight: 0.92
    letterSpacing: "0.01em"
  price:
    fontFamily: "var(--font-grocery-display), var(--font-grocery-arabic), var(--font-grocery-sans), 'Arial Narrow', sans-serif"
    fontSize: "1.375rem"
    fontWeight: 800
    lineHeight: 1
    letterSpacing: "0"
  aisle:
    fontFamily: "var(--font-grocery-display), var(--font-grocery-arabic), var(--font-grocery-sans), 'Arial Narrow', sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "0.02em"
  sticker:
    fontFamily: "var(--font-grocery-display), var(--font-grocery-arabic), var(--font-grocery-sans), 'Arial Narrow', sans-serif"
    fontSize: "0.75rem"
    fontWeight: 800
    lineHeight: 1.15
    letterSpacing: "0.05em"
  body:
    fontFamily: "var(--font-grocery-sans), var(--font-grocery-arabic), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "0"
  product-name:
    fontFamily: "var(--font-grocery-sans), var(--font-grocery-arabic), ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 600
    lineHeight: 1.375
    letterSpacing: "0"
  sku:
    fontFamily: "ui-monospace, SFMono-Regular, Menlo, monospace"
    fontSize: "0.75rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "0"
rounded:
  control: "0.625rem"
  card: "1rem"
  image: "0.625rem"
  badge: "0.375rem"
  overlay: "1.25rem"
spacing:
  unit: "0.25rem"
  line: "2px"
  gutter: "1rem"
  gutter-lg: "2rem"
  section: "3rem"
  section-lg: "4.5rem"
  header: "4rem"
  header-lg: "4.5rem"
  crate-foot: "0.625rem 0.75rem 0.75rem"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    typography: "{typography.title}"
    rounded: "{rounded.control}"
    padding: "0 1.5rem"
    height: "3rem"
  button-quick-add:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    rounded: "{rounded.control}"
    padding: "0"
    size: "2.5rem"
  button-quick-add-hover:
    backgroundColor: "{colors.primary-hover}"
    textColor: "{colors.primary-foreground}"
  button-shop-now:
    backgroundColor: "{colors.primary-foreground}"
    textColor: "{colors.primary}"
    typography: "{typography.title}"
    rounded: "{rounded.control}"
    padding: "0 1.25rem"
    height: "3rem"
  aisle-tile:
    backgroundColor: "{colors.secondary}"
    textColor: "{colors.secondary-foreground}"
    typography: "{typography.aisle}"
    rounded: "{rounded.control}"
    padding: "0 1rem"
    height: "2.75rem"
  aisle-tile-active:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
  sticker:
    backgroundColor: "{colors.foreground}"
    textColor: "{colors.background}"
    typography: "{typography.sticker}"
    rounded: "{rounded.badge}"
    padding: "0.25rem 0.55rem"
  sticker-sale:
    backgroundColor: "{colors.sale}"
    textColor: "{colors.sale-foreground}"
    typography: "{typography.sticker}"
    rounded: "{rounded.badge}"
    padding: "0.25rem 0.55rem"
  sticker-success:
    backgroundColor: "{colors.success}"
    textColor: "{colors.success-foreground}"
    typography: "{typography.sticker}"
    rounded: "{rounded.badge}"
    padding: "0.25rem 0.55rem"
  sticker-outline:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    typography: "{typography.sticker}"
    rounded: "{rounded.badge}"
    padding: "0.25rem 0.55rem"
  crate:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.card}"
    padding: "0"
  board:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
    rounded: "{rounded.card}"
    padding: "1.25rem"
  option-tile:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    padding: "0.5rem 0.875rem"
  option-tile-selected:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.primary-foreground}"
  input-search:
    backgroundColor: "{colors.card}"
    textColor: "{colors.foreground}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    padding: "0 2rem"
    height: "2.75rem"
---

# Design System: Grocery — Cash & Carry

## Overview

**Creative North Star: "The Warehouse Floor"**

The store is a working cash-and-carry, not a produce app. Goods sit at quantity in molded plastic
crates — every product card is a `.crate` with a 2px hardware line, big soft corners and a lift on
hover — and everything the store says, it prints: prices as signage on a board, categories as
aisle-board tiles hanging in a strip under the counter, state as stickers slapped on at a tilt, never
a toast. The world refuses the clean white produce-app grammar every grocery storefront ships, and
the dark gourmet-deli page alike. The merchant's PRIMARY is the price-board colour (safety yellow by
default): the hero board, the active aisle tile, every primary action, the focus ring. The first
viewport is the floor entrance: announcement tape → chunky counter header → the aisle strip → the
price board answering the merchant's slider across one seam — board first in the stack on phones —
with the first rail's crates already in reach below.

The materials are the preset's concrete floor and near-black ink (`--floor` / `--ink` alias the
merchant background and foreground) with one 2px hardware line (`--line`) running through every
crate, tile and control; `--line-strong` (ink at 55%) is the line under attention. Type is two
voices: Fira Sans Extra Condensed 700/800 uppercase is the printed signage — boards, prices, running
heads, stickers, aisle tiles — and Manrope carries everything that explains; Almarai is the Arabic
companion leading both roles under `:lang(ar)`. Shopping is stepper-first: quick-add straight from
the crate, and a product already in the basket shows its stepper right there. There is one motion
clock: everything that appears lands like a stamp (`.stamp-in`, 480ms emphasized), and everything
else is a 120–220ms transition on the same easings. The basket drawer shows the load honestly — a
12-segment meter that fills one segment per item and caps, inventing no threshold.

**Key Characteristics:**
- Products in `.crate` cells: `--cell` field, 2px `--line` border, 1rem molded corners; hover/focus-within lifts 2px with `--elev-md` and `--line-strong` (translate dropped under reduced motion).
- The merchant primary as the price board: `.board` (hero), the active `.aisle-tile`, the quick-add square, the one primary `Button` per view, the count sticker, the ring / caret / accent-color / `::selection`.
- State printed as a `.sticker` slapped on (`data-tilt` −2° over photos, straight in text rows): ink for OUT OF STOCK, `--sale` for SALE / SAVE N%, outline for ONLY N LEFT, success for ADDED and IN STOCK. Never a toast.
- Fira Sans Extra Condensed 800 uppercase for signage and every price (`.signage`, `.price`, tabular lining); Manrope 400–700 for names, facts, prose; Almarai leads both roles in `:lang(ar)`; SKUs in `ui-monospace`.
- One motion moment, the stamp (`grocery-stamp`, 480ms `--easing-emphasized`: lands at 118% and −4°, settles; a plain fade under reduced motion) — on ADDED stickers, the header count, the drawer's count sticker.
- Molded-plastic radii (0.625rem controls / 1rem crates / 0.375rem stickers / 1.25rem overlays), 2px borders everywhere, real offset-and-blur elevation on lifted surfaces.

## Colors

The theme hand-writes no hex. Its default palette (`src/colors.ts`, generated from OKLCH seeds in
`THEME_DEFAULTS.grocery`: concrete floor `#EFF2F5`, near-black ink `#14181F`, safety-yellow
`#EDB417` boards with dark text, crate-blue `#B4D6F3` wash, signal-red `#B7010B` statement) renders
when the merchant's ColorTheme is `DEFAULT`; a picked preset replaces it whole. Colour roles arrive
on `<html>` per request via the contrast-guarded bridge; `mapMerchantColors` re-maps only `ring` →
primary, so the focus ring is the shelf light that always matches the boards — every other role
rides as the preset gives it. The theme mixes two materials of its own in tokens.css: `--faint`
(foreground at 7%, the rail arrows' hover wash) and `--line-strong` (foreground 55% toward
background, the attention border).

### Primary
- **The Price Board** (`var(--primary)` on `var(--primary-foreground)`): a flat printed field — the hero `.board`, the active / current `.aisle-tile`, the selected option tile, the quick-add square on every crate, the one primary `Button` per view (ADD TO CART, CHECKOUT, PLACE ORDER), the basket count sticker (header and drawer), the filled load-meter segments, the active hero pager bullet, the selected gallery thumbnail's border, prose link underlines, plus `caret-color`, `accent-color`, the ring and `::selection`. `--primary-hover` (from the preset) is the quick-add square's border and hover field; SHOP NOW inverts the board (primary-foreground field, primary text, on the board).

### Secondary
- **Crate Blue** (`var(--secondary)` on `var(--secondary-foreground)`): the aisle-board wash — the resting `.aisle-tile` field and nothing else; hover mixes it 18% toward its own foreground. The only second hue on the floor, and it belongs to wayfinding.

### Neutral
- **Concrete** (`var(--background)`, aliased `--floor`): the page, the counter header, the aisle strip, the mobile counter bar.
- **Crate** (`var(--card)`, aliased `--cell`): every crate, image bed, facet crate, option tile, search field, rail arrow, the drawer's counter foot.
- **Ink** (`var(--foreground)`, aliased `--ink`): all text, the default sticker's field, the footer's loading-dock field, the hero pager tray (at 78%), the scrollbar thumb at 35%.
- **The Hardware Line** (`var(--border)`, aliased `--line`): every 2px line — crate and tile borders, the header's and aisle strip's base, drawer edges and rules, `divide-y-2` basket lines, the crate foot's top line, gallery frames.
- **Strong Line** (`--line-strong` = foreground 55% toward background): the crate's hover/focus border, the gallery thumbnail's hover border.
- **Faint** (`--faint` = foreground at 7%): the rail arrows' hover wash.
- **Muted** (`var(--muted)`): hover fields on quiet controls (option tiles, the variants arrow square, search suggestions) and the load meter's empty segments.
- **Print Grey** (`var(--muted-foreground)`): facts — SKUs, `<del>` prices, result counts, manufacturer lines, footer meta, the page-of counter, the aisle tile's count at 75%.

### Status
- **Sale** (`var(--sale)` on `var(--sale-foreground)`, the preset's `error` role via the bridge): the SALE / SAVE N% sticker only. Never a background wider than a sticker, never text.
- **Success** (`var(--success)` / `var(--success-foreground)`): the ADDED sticker that stamps onto the crate's price tag, and the IN STOCK sticker in the buy box.
- **Fault** (`var(--destructive)`): field errors and the logout row via the shared primitives; never a surface.

### Named Rules
**The Price-Board Rule.** The merchant primary is printed signage: the `.board`, the active `.aisle-tile`, the selected option tile, the quick-add square, the count sticker, the one primary button per view, and the ring / caret / selection. No primary text, no primary tint, no second primary button in a view.
**The Sticker Rule.** State prints itself as a `.sticker` and is slapped on where it applies — tilted (`data-tilt`) over a photo, straight in a text row: ink for OUT OF STOCK, `--sale` for SALE, outline for ONLY N LEFT, success for ADDED / IN STOCK. Never a toast, never a coloured border or wash for state.
**The Honest Count Rule.** Numbers are only ever real: aisle tiles show counts only when the catalog hierarchy provides them (a parent with none of its own sums its children; a hierarchy with no counts renders plain tiles), and the load meter fills one segment per item and caps at 12 — it never invents a free-shipping threshold.

## Typography

**Display Font:** Fira Sans Extra Condensed 700 / 800 (Latin, Latin-ext, Cyrillic; with Almarai for Arabic, then Manrope, "Arial Narrow", sans-serif)
**Body Font:** Manrope 400–800 variable (Latin, Latin-ext, Cyrillic; with Almarai, ui-sans-serif, system-ui, sans-serif)
**Label/Mono Font:** `ui-monospace` (`--font-code`) for SKUs only; labels are Manrope 600–700.

**Character:** Printed warehouse signage over a plain speaking voice. Anything the store prints —
the store's name on the board, aisle boards and running heads, drawer titles, stickers, aisle tiles,
SHOP NOW and CHECKOUT, and every price — is Fira Sans Extra Condensed 800 (tiles at 700), uppercase,
tabular lining numerals. Anything that explains — product names, facts, prose, inputs — is Manrope.
Arabic editions lead with Almarai on both roles: `:lang(ar)` swaps the stacks and drops the tracking
on `.signage` / `.price` / `.sticker` / `.aisle-tile` to 0, and both Latin faces ship with
`adjustFontFallback: false` because next/font's Arial-based metric fallback carries Arabic glyphs
and would catch them before Almarai.

### Hierarchy
- **Display** (`.signage` 800, 4.25rem → 6rem at `sm` → `clamp(3.5rem, 5.5vw, 6rem)` at `lg`, line-height 0.92, +0.01em, uppercase, balanced, `overflow-wrap: anywhere`): the store's name on the price board. The footer's name is the same voice at 3.25rem → 4.25rem; the wordmark fallback in the header at 1.75rem → 2.375rem.
- **Headline** (`.signage` 800, 1.75rem → 2.375rem at `lg`): every hanging aisle board (`SectionHeading`); the buy-box product name runs 2.375rem → 3.25rem; prose `h2` 1.75rem, `h3` 1.375rem in the same voice.
- **Title** (`.signage` 800, 1.375–1.75rem): drawer titles (basket 1.75rem, filters 1.375rem), facet legends and option legends at 1.125rem, checkout / summary heads.
- **Price** (`.price` 800, tabular, nowrap, line-height 1): 1.375rem on a crate, 1.125rem on a basket line, 2.375rem for the drawer subtotal, 4.25rem in the buy box, 1.75rem on the mobile counter bar; `<del>` original above or beside it in Manrope 0.75–1rem print grey. The page-of counter borrows the voice at 0.9375rem print grey.
- **Aisle** (`.aisle-tile` 700, 0.9375rem, +0.02em, uppercase): the aisle-board tiles; the count rides in Manrope 600 at 0.75rem / 75% tabular.
- **Sticker** (800, 0.75rem, line-height 1.15, +0.05em, uppercase, tabular): every state; the header's count sticker at 0.7rem.
- **Product Name** (Manrope 600, 0.875rem, line-height 1.375, 2-line clamp, `dir="auto"`): the crate's rail tag and the basket line.
- **Body** (Manrope 400, 0.9375rem, line-height 1.5; prose 1.65 at ≤70ch via `.prose-grocery`): page copy, facts, inputs, messages; buttons and option tiles at 600.
- **SKU** (`font-mono`, 0.75rem, print grey, `dir="ltr"`): "SKU: …" on crates and the buy box.

### Named Rules
**The Signage Rule.** If the store prints it — a name, a board, a price, a sticker, a tile, a primary action's label — it is Fira Sans Extra Condensed 800 uppercase (Almarai in Arabic) with tabular lining numerals; product names, facts and prose are never condensed, and a price is never Manrope.
**The Printed Fact Rule.** Prices come pre-formatted from the API and are never re-formatted; counts, SKUs and the meter carry `tabular-nums`; merchant-supplied names sit in `<bdi dir="auto">` and the board name breaks with `overflow-wrap: anywhere`.

## Layout

The floor runs at `max-w-content` (84rem) for Home, Category, Product, Checkout and Account
(`container: 'content'`); CMS, blog and policy pages narrow to 44rem. The gutter is 1rem, 2rem from
`lg`; the stretch rhythm is `--section-y` 3rem, 4.5rem from `lg` (`pt-section` above every home
group, `mt-section` above the footer); inner gaps run 1.5rem (`gap-6`), 2.5rem at `lg` (`lg:gap-10`).

First viewport: the merchant's announcement tape → the counter: a sticky header, 4rem (4.5rem at
`lg`), on the floor with a 2px base line — menu (below `lg`) · logo (2.25rem / 2.5rem tall, ≤11rem)
or signage wordmark · the search slot (from `md`, up to 24–28rem) · language / account / basket with
its stamped count sticker → the aisle strip: 2.75rem tiles in a `py-2.5` row, HOME first, CMS pages
pushed to the end (`ms-auto`), scrolling horizontally with an end fade below `lg` → the entrance:
the price board and the merchant's slider sharing one seam on a `lg:grid-cols-[2fr_3fr]` grid —
stacked board-first on phones — the stage capped at 4:3 / 38vh, 16:9 / 46vh at `sm`, 56vh at `lg`
so the first rail's crates stay in reach; with a banner only, the banner is the stage; with no image
the board alone is the entrance at `min-h-42vh`, full width, still finished.

Crates sit in a plain grid — 2 / 3 / 4 / 5 columns at base / `sm` / `lg` / `xl` (layout config),
`gap-3` / `gap-4` — or a `.rail` (2 / 3 / 4 / 5 per view at 0 / 640 / 1024 / 1280px, 12px between)
when a home group runs longer than the grid shows; the HOME_PAGE group always grids. Product images
are 1:1 on `--cell`; the crate foot is `px-3 pb-3 pt-2.5` under a 2px top line. The product page is
two columns from `lg` (gap 1.5rem / 2.5rem): the crate-window gallery (1:1, `contain`, 4rem
thumbnails in a `.scroll-x` strip) and the rail tag; on phones a fixed counter bar (price + ADD TO
CART, `safe-area-inset-bottom`, `shadow-overlay`) keeps the action in thumb reach. The listing has a
14rem facet rail from `lg` (facets in `rounded-card` crates) and a start-side drawer below (88vw,
max 24rem); checkout and account reuse the same crates. Drawers: basket from the reading end
(`sm:max-w-md`, `border-s-2`), menu and filters from the reading start. RTL mirrors via logical
properties, `rtl:rotate-180` on directional icons, a mirrored `.scroll-x-fade`, and Swiper re-keyed
on `dir`.

## Elevation & Depth

A stocked floor under work lights: surfaces are defined by the 2px hardware line, and elevation is
earned by lifting off it. Crates rest nearly flat and physically lift on hover/focus-within (2px up,
`--elev-md`, the line going strong); rail arrows and the mobile counter bar float; overlays (search
suggestions, menus, drawers) carry the deep overlay shadow. Stickers sit a hair off the surface with
`--elev-sm`, like vinyl.

### Shadow Vocabulary
- **Sticker** (`--elev-sm`: `0 1px 2px 0 rgb(10 14 20 / 0.06)`): every `.sticker` at rest.
- **Lift** (`--elev-md`: `0 6px 16px -6px rgb(10 14 20 / 0.16)`): the crate on hover/focus-within; the rail's prev / next buttons.
- **Overlay** (`--elev-overlay`: `0 28px 60px -18px rgb(10 14 20 / 0.38)`): the search suggestions, menus, drawers, and the mobile counter bar (through the shell's `shadow-overlay`).
- `--elev-lg` (`0 12px 28px -10px rgb(10 14 20 / 0.22)`) is declared for the shared shell and referenced by no theme element — not canonized.

### Named Rules
**The Hardware-Line Rule.** Every crate, tile, control and seam is drawn with the same 2px `--line` (`--border-width: 2px` feeds the shared `border-2` scale); attention turns the line strong or primary, never a glow. The only borders that are not the line are the quick-add square's `--primary-hover` edge and the sticker-outline's inset ink.
**The Lift-Is-Earned Rule.** Only interaction and floating surfaces lift: the crate's hover (2px translate + `--elev-md`), the rail arrows, the counter bar, overlays. Nothing ships pre-lifted; no glows, no coloured shadows.
**The Layering Rule.** World classes (`.signage`, `.price`, `.board`, `.crate`, `.aisle-tile`, `.sticker`, `.stamp-in`, `.scroll-x`, `.scroll-x-fade`, `.prose-grocery`) live in `@layer components`, so a Tailwind utility on the same element (`text-2xl`, `bg-primary`, `absolute`) always wins; only the Swiper overrides (`.stage`, `.rail`) stay unlayered because Swiper's CSS is.

## Shapes

Molded plastic. Crates, boards, facet crates, the stage and gallery frames take the big soft corner
(`--r-card` 1rem); controls — buttons, tiles, the quick-add and arrow squares, inputs, rail arrows,
social squares — take 0.625rem (`--r-control`); images and thumbnails 0.625rem (`--r-image`);
stickers and the load meter's segments 0.375rem (`--r-badge`); popovers and the search list 1.25rem
(`--r-overlay`); the hero's pager tray and bullets are full pills. Every edge is the 2px hardware
line. Stickers tilt when slapped over a photo (`data-tilt` −2°, `data-tilt="alt"` +1.5°) and sit
straight in text rows; the hero pager is a small ink tray pinned to the stage's bottom-end with the
active bullet stretching to 1.5rem of primary.

## Components

### Buttons
- **Shape:** molded (0.625rem); heights 2rem (`sm`: filters, pager, clear), 2.25rem (default), 2.5rem (`lg` and the size-10 squares), 3rem (`h-12`: ADD TO CART, SHOP NOW, CHECKOUT).
- **Primary (shared `Button`):** primary field, primary-foreground text; the big actions carry `.signage` at 1.125rem (CHECKOUT, ADD TO CART with the cart icon at the inline start). One per view.
- **Quick-add square (on every crate):** `size-10`, primary field, 2px `--primary-hover` border, a `size-5` plus; hover fills `--primary-hover`; disabled 50% / `not-allowed`. Variants swap it for a card-field arrow square (`hover:bg-muted`, arrow mirrored in RTL).
- **Shop Now (on the board):** the inversion — primary-foreground field, primary text, `.signage` 1.125rem, h-12, arrow-down icon; 90% opacity on hover; focus outline in primary-foreground.
- **Hover / Focus:** colour transitions at `--motion-fast` (120ms); focus is the shared ring pinned to primary (the shelf light).
- **Outline / Ghost:** shared variants with the theme's 2px border — FILTERS, pager, RETRY, CONTINUE SHOPPING; ghost squares for menu / language / account / basket and REMOVE.

### Stickers
- **`.sticker`:** ink field, floor text, condensed 800 at 0.75rem +0.05em uppercase, `0.25rem 0.55rem`, 0.375rem, `--elev-sm`; `data-tilt` rotates −2° (alt +1.5°) when slapped over a photo. `.sticker-sale` in `--sale` ("SAVE N%" when derivable, else "SALE"; hidden when out of stock); `.sticker-outline` (cell field, inset 2px ink) for "ONLY N LEFT" and the empty basket count; `.sticker-success` for ADDED and IN STOCK. OUT OF STOCK is the ink sticker and the photo drops to 50% grayscale.
- **Placement:** stacked at `start-2 top-2` over the crate photo; beside the price in the buy box (out ▸ low ≤ 5 ▸ in stock, exactly one shows); the ADDED sticker stamps in over the crate's price tag; counts stamp on the basket button (`-end-1 -top-1`) and in the drawer header (primary field when > 0, outline at 0).

### Cards / Containers
- **Crate (`ProductCard`):** `.crate` full height, overflow hidden: the 1:1 photo (second photo cross-fades in 220ms on hover), stickers at the top-start, then under a 2px top line the rail tag — name (Manrope 600, 0.875rem, 2 lines), mono SKU, and a foot row with `<del>` + `.price` at 1.375rem beside the quick-add slot. Stepper-first: a product already in the basket shows the shared `QuantityStepper` (size `sm`) right in the crate; adding or incrementing stamps the ADDED sticker (`.sticker-success .stamp-in`, tilted, `role="status"`) over the tag. Variants send the shopper to the page instead.
- **Board (`.board`):** primary field, primary-foreground text, 1rem corners, `p-5` → `p-8` — the hero's price board (name at display scale, the store's real facts joined by " · " at 85%, SHOP NOW), bottom-justified, `min-h-15rem` beside an image / `min-h-42vh` alone.
- **Facet crate:** `rounded-card border-2 bg-card p-4` fieldsets with `.signage` 1.125rem legends — manufacturer radios, option checkboxes; CLEAR FILTERS as ghost `sm`.
- **Stage:** `rounded-card border-2 bg-card` with the Swiper pager tray at its bottom-end (ink 78% pill tray, floor bullets at 55%, the active one primary and 1.5rem wide).

### Inputs / Fields
- **Search:** a 2.75rem shared `Input` on the crate field with a 2px line, `ps-8 pe-8` for the 16px icon at start and a ghost clear at end; suggestions drop as a `rounded-overlay` 2px-lined popover with `shadow-overlay` (`w-72`, `mt-1.5`, z-50), rows hovering `bg-muted`, each hit tagged with its kind. Hidden entirely when the platform reports nothing searchable.
- **Option tiles (buy box):** `role="radio"` buttons, `min-w-11`, 2px line, 0.625rem, Manrope 600 at 0.875rem, `px-3.5 py-2`; selected = primary field + primary border; unavailable = dashed line, print grey, line-through; price deltas at 0.75rem / 80%.
- **Shared fields:** the shell's `Input` / `Select` / `Checkbox` / `RadioGroup` with the theme's 2px border and 0.625rem radius; sort is a `w-44` small select on the crate field; focus is the primary ring.
- **Quantity:** the shared `QuantityStepper` — full size in the buy box, `sm` in crates and basket lines.

### Navigation
- **Aisle strip (signature):** 2.75rem `.aisle-tile`s — crate-blue field, 2px border of `currentColor` at 22%, condensed 700 caps at 0.9375rem +0.02em, count in Manrope 600 xs at 75% — in a `py-2.5` row under the counter; hover deepens the wash 18% toward its foreground; the current aisle (`aria-current="page"`) takes the price-board colour. HOME leads, CMS pages and merchant menu extras push to the end; `.scroll-x .scroll-x-fade` below `lg`. Sub-categories get their own tile row on the category page.
- **Counter (header):** sticky, 2px base line; logo or signage wordmark; the search slot from `md`; ghost icon squares for language (dropdown), account (dropdown), basket (count sticker stamps in on every change, opens the drawer).
- **Breadcrumbs:** the shared breadcrumb, chevrons mirrored in RTL.
- **Mobile:** a start-side drawer with signage title; the aisle strip itself stays visible on phones.
- **Pager:** two outline `sm` buttons around "page x of y" in the `.price` voice at 0.9375rem print grey.

### Hero (signature)
The floor entrance: the `.board` (store name at signage scale up to 6rem, facts, SHOP NOW) answering
the merchant's slider across one 0.75rem seam on a `2fr_3fr` grid — the board leads the stack on
phones. The stage is a molded crate (`rounded-card border-2 bg-card`) capped at 38 / 46 / 56vh with
the pill pager tray at its bottom-end; autoplay at 6s only when there are multiple slides, re-keyed
on `dir`. Banner-only merchants get the banner as the stage; imageless merchants get the board
alone, full width, `min-h-42vh`.

### Section Heading (signature)
`SectionHeading`, the hanging aisle board: a 2px base line with the title in `.signage` 1.75rem →
2.375rem at the start, the count in Manrope 600 0.875rem tabular print grey on the same baseline, an
optional subtitle and an end action; `mb-5`.

### Cart Drawer (signature)
The basket, from the reading end (`sm:max-w-md`, `border-s-2`): the header on a 2px line with the
signage title and the count sticker (primary field, stamping in on every count change; outline at
zero) — then the load meter, 12 `h-1.5` rounded-badge segments in a `grid-cols-12`, one filling
primary per item, capped and honest. Lines divide with 2px rules: a `size-20` crate-framed photo,
name, `sm` stepper, REMOVE as ghost, the line total in `.price` 1.125rem. The counter foot sits on
the crate field over a 2px line: SUBTOTAL in `.signage` 1.125rem against the subtotal in `.price`
2.375rem, the tax note, a full-width signage CHECKOUT, Continue shopping as a link.

### Counter Bar (signature)
On product pages below `lg`: a fixed bottom bar on the floor with a 2px top line and the overlay
shadow, padded to `safe-area-inset-bottom` — the price in `.price` 1.75rem beside a full-width h-12
signage ADD TO CART, mirroring the buy box's state (ADDING…, OUT OF STOCK, disabled until options
are picked).

### Footer (signature)
The loading dock: an ink field (`bg-foreground text-background`) closing the floor — the store name
in `.signage` 3.25rem → 4.25rem, then the manifest over a 2px line at background/20: address and
`size-10` molded social squares (2px border at background/25), SHOP (first 8 categories),
INFORMATION, CONTACT, headed in `.signage` at background/70; the copyright line on its own 2px seam.

### States
- **Empty / Not found / Error:** the shared blocks; the basket's empty state renders inside the drawer with CONTINUE SHOPPING.
- **Loading:** skeleton pages on the same crate grid (skeleton crates with a 2px-lined foot, `aria-busy`); the listing dims to 60% while refetching.
- **Sold out:** ink sticker, photo 50% grayscale, the quick-add disabled.

## Do's and Don'ts

### Do:
- **Do** ship every product as a `.crate` with fixed slots — 1:1 photo, stickers at `start-2 top-2`, a 2px-lined foot with name (2 lines, Manrope 600), mono SKU, `<del>` + `.price` beside the quick-add slot — in the 2 / 3 / 4 / 5 grid or a `.rail`.
- **Do** keep quick-add stepper-first: the plus square for a product not yet in the basket, its `QuantityStepper` in the crate once it is, the page link only when variants force a choice; stamp ADDED as a `.sticker-success .stamp-in` over the tag.
- **Do** reserve the merchant primary for printed signage: the `.board`, the active `.aisle-tile`, the selected option tile, the quick-add square, count stickers, the one primary button per view, the ring; keep SHOP NOW as the inversion on the board.
- **Do** print state as a `.sticker` (`data-tilt` over photos, straight in rows) and let counts and stickers land with `.stamp-in` on the shared clock; leave in-stock silent on crates.
- **Do** set everything the store prints in Fira Sans Extra Condensed 800 uppercase with tabular lining numerals (`.signage`, `.price`, `.sticker`, `.aisle-tile`) and everything it says in Manrope; wrap merchant strings in `<bdi dir="auto">`.
- **Do** draw every crate, tile, control and seam with the 2px `--line` (`border-2`, `divide-y-2`) and keep radii to the molded scale: 1rem crates / boards, 0.625rem controls and images, 0.375rem stickers, 1.25rem overlays.
- **Do** cap the stage (38 / 46 / 56vh) and keep the board first in the stack on phones; let the board stand alone, full width, when the merchant has no image.
- **Do** mirror for RTL with logical properties, `rtl:rotate-180` on directional icons, the mirrored `.scroll-x-fade`, Swiper re-keyed on `dir`, and trust the `:lang(ar)` swap (Almarai leads both roles, tracking drops to 0).

### Don't:
- **Don't** hard-code hex; every colour is a role variable from the merchant bridge or one of the two declared mixes (`--faint`, `--line-strong`). `mapMerchantColors` touches only `ring`.
- **Don't** announce state anywhere but a sticker — no toasts, no banners, no coloured borders or washes; no sale / success background wider than a sticker.
- **Don't** invent numbers: no counts on aisle tiles the hierarchy didn't provide, no thresholds on the load meter, no re-formatted prices.
- **Don't** add a second motion clock: `grocery-stamp` (480ms, `--easing-emphasized`, 118% / −4° landing, a fade under `prefers-reduced-motion`) is the theme's only keyframe; everything else is a 120–220ms transition on `--easing-standard`, and the crate's hover translate drops under reduced motion.
- **Don't** set a price, a board or a sticker in Manrope, or a product name / fact / prose in the condensed face; don't print signage in sentence case.
- **Don't** put category navigation in the header — it is the aisle strip; don't give the entrance a full-bleed or uncapped slider, and don't let the stage push the first crates out of the first viewport.
- **Don't** thin the hardware line (no 1px borders), pre-lift a resting crate, or draw a border in anything but `--line` (the quick-add's `--primary-hover` edge, the aisle tile's currentColor 22%, the sticker-outline's ink inset and the footer's background/20–25 are the exceptions the build owns).

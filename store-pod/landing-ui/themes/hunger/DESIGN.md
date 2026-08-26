---
name: Hunger
description: The Letterbox Menu — the storefront as the folded takeaway menu posted through the door.
colors:
  primary: "#AD0010"
  primary-hover: "#8B000A"
  primary-focus: "#6F0006"
  secondary: "#FFD4CD"
  accent: "#00663E"
  background: "#FEFBF8"
  foreground: "#16100D"
  border: "#E2D9CF"
  neutral: "#B2A9A5"
  error: "#B7191C"
  warning: "#EE9E10"
  success: "#007533"
  info: "#0065A1"
typography:
  display:
    fontFamily: "Alumni Sans, Alexandria, Arial Narrow, sans-serif"
    fontSize: "5.25rem"
    fontWeight: 800
    lineHeight: 0.9
    letterSpacing: "0.005em"
  headline:
    fontFamily: "Alumni Sans, Alexandria, Arial Narrow, sans-serif"
    fontSize: "2.75rem"
    fontWeight: 800
    lineHeight: 0.9
    letterSpacing: "0.005em"
  title:
    fontFamily: "Alumni Sans, Alexandria, Arial Narrow, sans-serif"
    fontSize: "1.5rem"
    fontWeight: 800
    lineHeight: 0.9
    letterSpacing: "0.005em"
  dish-name:
    fontFamily: "Alumni Sans, Alexandria, Arial Narrow, sans-serif"
    fontSize: "1.0625rem"
    fontWeight: 700
    lineHeight: 1.02
    letterSpacing: "0.005em"
  body:
    fontFamily: "Geologica, Alexandria, ui-sans-serif, system-ui, sans-serif"
    fontSize: "0.9375rem"
    fontWeight: 400
    lineHeight: 1.5
    letterSpacing: "normal"
  price:
    fontFamily: "Alumni Sans, Alexandria, Arial Narrow, sans-serif"
    fontSize: "1.0625rem"
    fontWeight: 800
    lineHeight: 1
    fontFeature: "tnum 1, lnum 1"
  label:
    fontFamily: "Alumni Sans, Alexandria, Arial Narrow, sans-serif"
    fontSize: "0.6875rem"
    fontWeight: 700
    lineHeight: 1.35
    letterSpacing: "0.08em"
rounded:
  control: "0"
  card: "0"
  image: "0"
  badge: "0"
  overlay: "0"
spacing:
  unit: "0.2375rem"
  gutter: "1rem"
  gutter-lg: "2rem"
  section-y: "2.5rem"
  section-y-lg: "3.5rem"
components:
  add-button:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    rounded: "{rounded.control}"
    padding: "0 0.85rem"
    height: "2.25rem"
    typography: "{typography.label}"
  fold-tab:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.control}"
    padding: "0 0.85rem"
    height: "2.75rem"
  fold-tab-active:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
  dish-no:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    rounded: "{rounded.badge}"
    padding: "0 0.3rem"
    height: "1.65rem"
    typography: "{typography.label}"
  dish-no-ordered:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
  section-band:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    rounded: "{rounded.card}"
    padding: "0.375rem 0.75rem"
    typography: "{typography.title}"
  mark:
    backgroundColor: "transparent"
    textColor: "{colors.foreground}"
    rounded: "{rounded.badge}"
    padding: "0.1rem 0.35rem"
    typography: "{typography.label}"
  mark-sale:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
  mark-offer:
    backgroundColor: "{colors.accent}"
    textColor: "#FFFFFF"
---

# Design System: Hunger

## Overview

**Creative North Star: "The Letterbox Menu"**

The storefront is the folded takeaway menu that came through the door. Every dish is a printed line —
order number, name, dotted leader, price in one tabular column, ADD at the end. Sections are bands of the
second ink plate. State is printed in outline (SALE / SOLD OUT / ONLY N LEFT), never toasted. The world
refuses the full-bleed dark dish photo over rounded cards with a coloured add button that every food
storefront ships, and it refuses the cream-and-serif fine-dining page just as hard.

The sheet is white menu stock carrying exactly two ink plates: ink (`foreground`) and the merchant's
PRIMARY. Nothing else prints. Every radius token is `0`; hairline rules replace cards; the primary appears
only as flat fields — the masthead phone block, the section band, the active fold tab, every ADD, the
ordered dish's number box. Density is deliberately high (`--density: 0.95`, dish lines at `py-3.5`): a
menu earns trust by fitting a lot of dishes on one sheet, and the shopper is meant to read down, not to
browse a gallery.

Attention is a registration crop mark, not a browser ring. Motion is a press clock: one authored moment
(the second plate wiping across the line you just ordered) and nothing else — no entrance animations, all
content visible by default.

**Key Characteristics:**
- Two ink plates on white stock: ink and the merchant PRIMARY. No third colour, no gradients.
- Zero radius on every surface, control, image and overlay.
- Hairline rules instead of cards; the sheet is flat, only floating surfaces cast shadow.
- One price column down the whole sheet at every width — `productGrid` is 1 at every breakpoint.
- Dishes are lines, never cards; pictures are small printed thumbs beside the line, never the subject.
- Attention = crop marks; state = printed outline marks; motion = one wipe.

## Colors

Two plates on white menu stock: black ink for everything that is read, the merchant's primary for
everything that counts.

**Colour roles are not authored in `tokens.css`.** They arrive from the merchant's `ColorTheme` preset
through the shell's colour bridge (an inline style on `<html>`), which is contrast-guarded. The theme
ships only its own `DEFAULT` palette (`src/colors.ts`, wired as `tokens.defaultColors`), used when the
merchant's colour theme is `DEFAULT` or unset; any fixed preset the merchant picks replaces it whole.

`src/colors.ts` is **generated**, never hand-edited: it comes from the `THEME_DEFAULTS.hunger` OKLCH seed
in `libs/types/scripts/build-color-schemas.mjs`, regenerated with `npm run gen:colors` in `libs/types`.
Change the seed, not the file.

### Primary
- **Menu Red** (`#AD0010`): the second ink plate. Section bands, the ADD control, the active fold tab, the
  ordered dish's number box, the SALE mark, the masthead phone number, the pagination stub of the slider,
  and the crop marks. Never a gradient, never a shadow, never a tint except through `--wash`.

### Secondary
- **Blotting Pink** (`#FFD4CD`): a 20%-tint of the plate. Present in the palette for bridge completeness;
  the sheet reaches for `--wash` (8% primary in background) instead, as the hover state on dish lines and
  fold tabs.

### Tertiary
- **Spot Green** (`#00663E`): the offer/in-stock ink. Used only on `.mark-offer` — the one place the sheet
  admits a third ink, in the way a menu prints a green "V" beside a dish.

### Neutral
- **Menu Stock** (`#FEFBF8`): the page. Every surface is this; there is no second background tone.
- **Press Ink** (`#16100D`): all body copy, all rules that must read as structural (`--rule-strong`,
  `border-foreground`), the header's bottom rule (2px), the footer's top rule (2px).
- **Cut Rule** (`#E2D9CF` / `--rule`, 26% ink over stock): the hairline between dish lines, the section
  crease, the footer column rules.
- **Small Print** (`--small-print`, 62% ink): descriptions, counts, `SOLD OUT`, footer copy.
- **Leader Dot** (`--leader-dot`, 42% ink): the dotted leader and the phone-width dotted rule above the
  price row.

### Named Rules
**The Two Plates Rule.** The sheet prints in ink and the merchant primary. A third colour appears only as
`--wash` (8% primary), the spot-green offer mark, and the semantic error/warning/success roles the shell
supplies for forms. Nothing else earns an ink.

**The Flat Field Rule.** The primary is only ever a flat, fully-saturated field with `primary-foreground`
on it (`.plate`). Never a gradient, never at partial opacity as decoration, never a shadow tint.

**The Bridge Rule.** Never write `--primary`, `--background` or `--foreground` in `tokens.css`. The
merchant's preset owns colour roles; the theme owns everything else.

## Typography

**Display Font:** Alumni Sans (`--font-hunger-display`, weights 600–900) — a tall collegiate gothic.
**Body Font:** Geologica (`--font-hunger-sans`, weights 300–700).
**Arabic Font:** Alexandria (`--font-hunger-arabic`, weights 300–800), leading *both* roles in Arabic.

**Character:** Alumni Sans prints what names and what counts; Geologica sets what must be read. The scale
contrast between them is violent on purpose — a 5.25rem masthead over 0.6875rem small print is what a
printed menu actually looks like.

Arabic editions swap the stacks in `[data-theme="hunger"]:lang(ar)` so Alexandria leads `--font-body` and
`--font-heading`. **This is not a preference — it is a fix.** next/font's metric fallback for the Latin
faces carries Arabic glyphs and would otherwise catch Arabic text first, rendering it in a fallback face.
Alexandria's Latin sets the few Latin words that appear inside Arabic copy. In the same block, `.press`,
`.mark`, `.dish-no`, `.fold` and `.price` drop `text-transform: uppercase` and all tracking (Arabic has no
case and Alexandria sets wider), and `.press` relaxes to `line-height: 1.25`.

### Hierarchy
- **Display** (Alumni Sans 800, up to `--type-6xl` 5.25rem, line-height 0.9): the store name on the
  masthead and nothing else.
- **Headline** (Alumni Sans 800, `--type-4xl` 2.75rem–`--type-5xl`): the product page's dish name, the
  footer's store name.
- **Title** (Alumni Sans 800, `--type-xl`–`--type-2xl`): section bands, drawer titles, empty-state titles.
- **Dish name** (`.dish-name`, Alumni Sans 700, line-height 1.02, `text-wrap: pretty`): the menu line's
  name. Set tight so a wrapped name still reads as one line. ~30px in the board variant, ~17px in the list.
- **Body** (Geologica 400, `--type-base` 0.9375rem, line-height 1.5): descriptions, forms, CMS copy.
  Long-form is `.prose-hunger` — line-height 1.7, `max-width: 68ch`.
- **Price** (`.price`, Alumni Sans 800, tabular + lining numerals, `white-space: nowrap`).
- **Label** (`.mark` / `.fold` / breadcrumbs, Alumni Sans 700, `--type-xs` 0.6875rem, tracking 0.08em,
  uppercase).

### Named Rules
**The Printed Voice Rule.** `.press` (Alumni Sans 800, uppercase, tabular numerals, `text-wrap: balance`)
is the voice for anything that names or counts: masthead, bands, numbers, prices, marks, nav, small print.
Everything a shopper has to *read* — dish descriptions, forms, policies — is Geologica. Do not mix.

**The Tabular Numeral Rule.** Every number that can align with another number (`.price`, `.dish-no`,
`.fold > .count`, the order tally, pagination) is `font-variant-numeric: tabular-nums lining-nums`.

**The Arabic Lead Rule.** Never reorder the `:lang(ar)` font stacks to put a Latin face first. The metric
fallback will eat the Arabic glyphs.

## Layout

The sheet is one column set to a readable measure: `--width-content` 74rem is the default container
(`layoutConfig.container: 'content'`), with `--width-narrow` 40rem for checkout/redirect and
`--width-wide` 90rem available. The gutter is `--gutter` (1rem, 2rem at ≥1024px); section rhythm is
`--section-y` (2.5rem, 3.5rem at ≥1024px).

**One price column, at every width.** `layoutConfig.productGrid` is `{base: 1, sm: 1, lg: 1, xl: 1}`.
This is load-bearing, not an unfilled default: a second column breaks the single tabular price column that
is the whole point of a printed menu. `ProductGrid` still reads the config (so every listing agrees) and
adds `lg:gap-x-10` as a printed gutter, but the config must stay 1.

**Scroll pacing.** The home page's *first* product group renders `variant="board"` — one wide column,
dish names at `text-xl sm:text-2xl lg:text-3xl` (~30px), 96px thumbs, `py-5`. Every group after it is the
dense line list — names at `text-base sm:text-lg` (~17px), 56px thumbs, `py-3.5`. The sheet paces itself
instead of running one wall of lines from top to bottom.

**The dish line grid.** `grid-cols-[auto_minmax(0,1fr)]` on phones, `[auto_minmax(0,1fr)_auto]` from `sm`.
Column 1 is the number box plus the thumb; column 2 the name, description and marks; column 3 (desktop
only) the price and the action, right-aligned and row-spanning. On phones the price and action drop to
their own full-width row separated by a dotted top rule: squeezing a tabular price and an ADD plate into a
third column collapses the dish name, worst in Arabic.

**Header and fold.** Sticky header at `--header-h` 3.5rem / `--header-h-lg` 4.25rem, closed by a 2px ink
rule. Directly beneath it, the fold strip: a `.scroll-x` row of category tabs with `-ms-px` overlaps so
adjacent borders share one rule. Category navigation lives here and is deliberately **not** repeated in
the standing nav, which carries CMS pages and menu entries only.

### Named Rules
**The One Column Rule.** `productGrid` stays `1` at every breakpoint. If a surface needs more density,
shorten the line — never add a column.

**The Shared Rule Rule.** Adjacent bordered items overlap by one pixel (`-ms-px`, `-mt-px`, `-mx-px`) so
two 1px borders read as one printed rule, never as a 2px seam. Applies to fold tabs, social icons,
pagination, and BuyBox option rows.

## Elevation & Depth

**Print has no shadows.** The sheet is flat: depth comes from rules, from the ink/primary plate contrast,
and from `--wash` on hover. Only what genuinely floats above the sheet — drawers, dropdowns, the search
suggestion panel — carries a shadow. `--elev-sm` is not a shadow at all but a 1px hairline
(`0 1px 0 0 …12%`), used by `.crease` to give a section fold its paper edge.

### Shadow Vocabulary
- **Hairline / crease** (`--elev-sm`, `0 1px 0 0 color-mix(--foreground 12%)`): the fold under the header
  strip. Reads as a crease in paper, not a lift.
- **Low** (`--elev-md`, `0 2px 6px -2px …22%`) and **Raised** (`--elev-lg`, `0 6px 18px -8px …28%`):
  available to libs/ui primitives; the theme's own surfaces do not use them.
- **Overlay** (`--elev-overlay`, `0 20px 48px -16px …40%`): drawers, dropdowns, the search panel — the
  only surfaces that are truly off the sheet. Paired with a 2px ink border.

### Named Rules
**The Paper Rule.** A surface that is part of the sheet gets a rule, never a shadow. A surface that floats
over the sheet gets `--elev-overlay` *and* a 2px ink border. There is nothing in between.

## Shapes

Nothing on a printed sheet has a rounded corner: `--r-control`, `--r-card`, `--r-image`, `--r-badge` and
`--r-overlay` are all `0`. Borders carry the entire form language, in three weights:

- **Hairline** (1px `--rule` / `border-border`): between dish lines, around thumbs, footer columns, the
  dotted price-row rule on phones.
- **Ink** (1px `--rule-strong` / `border-foreground`): number boxes, fold tabs, marks, BuyBox option rows,
  the masthead slider frame, the phone block, social tiles.
- **Cut** (2px ink): the header's bottom edge, the footer's top edge, the price rule in the BuyBox, drawer
  and dropdown edges. A cut rule means "the sheet ends here".

Dashed borders mean *provisional*: the announcement strip's inner rule, an empty stretch of sheet
(`EmptyState`), an unavailable variant row. `--product-aspect` is `4/3` for the printed thumb.

**The registration crop mark** is the one non-rectangular device: eight linear-gradient corner brackets
drawn once as `--crop-marks` / `--crop-sizes` / `--crop-positions`, inset `-5px`.

### Named Rules
**The Zero Radius Rule.** No radius token, utility or inline value above `0` anywhere in this theme,
including images, avatars and overlays.

## Components

### Dish Line (`.dish`, signature component)
The whole product, printed. Hairline bottom rule, `--wash` on hover, crop marks on hover/focus-within via
`.crop`. Contents in reading order: the number box, the printed thumb (only when the merchant supplied an
image; `tabIndex={-1} aria-hidden` because the name link already goes there), the dish name with a dotted
leader running to the price, one plain-text sentence of description (`line-clamp-2`, HTML stripped),
outline marks, then price and action.

The action is one of three, never more: **ADD** (`.fold.plate`) when the dish is simply addable;
**VIEW DETAILS** (`.fold`) when it has variants and a choice is required; **SOLD OUT** (`.mark.mark-out`)
when it is not orderable.

### Dish Number (`.dish-no`)
The thing you order by: a bordered slot, min-width 2.5rem, tabular. It prints the **last two
hyphen-segments** of a real SKU so the code column stays a column, with the full SKU carried in the line's
`title`; the product page prints the SKU in full. Once the dish is in the order the box fills with the
primary and shows `×N` — so scrolling a long menu shows what you already chose.

### Fold Tab (`.fold`)
The unit control of this theme: min-height 2.75rem, ink border, uppercase Alumni Sans, `--wash` on hover.
`aria-current="page"` or `data-active="true"` lights it across its whole width in the primary. The same
class is the button, the pagination control, the empty-state action and (with `.plate`) every ADD. An
optional `> .count` child sets in Geologica at `--type-xs`, 0.7 opacity.

### Section Band (`SectionHeading`)
The running head that names a stretch of the menu, printed on the primary plate (`px-3 py-1.5`) so the eye
finds it while scrolling. An optional action sits inside the band, at its end.

### Marks (`.mark`)
Outline, square, micro-caps state, printed in the line. Only signals that change a decision: sale (with %
when derivable), sold out, last few. **In stock is silence** — a menu prints nothing beside a dish it
simply has. `.mark-sale` fills with the primary, `.mark-offer` with the spot green, `.mark-out` drops to
small print. Never a floating badge over a photo; never a toast.

### Inputs / Fields
libs/ui primitives with `border-foreground` and zero radius. Focus is a 2px primary outline at 2px offset
(see the focus rule below). The search field carries a leading icon and a clearing X; its suggestion panel
is a 2px-ink-bordered popover with `shadow-overlay`, hits highlighting to the full primary plate.

### Navigation
Standing nav is `.press` at `--type-sm`, ink at 75% opacity, the current page underlined 2px in the
primary at 4px offset. Mobile nav and cart are both drawers (`mobileNav: 'drawer'`, `cart: 'drawer'`) with
2px ink edges and a primary-plate header. The **order tally** in the header is a fold tab, not a bag icon:
the running count printed in a bordered box the way a counter clerk keeps it, lighting to the primary once
something is in the order.

### Cart Docket (`CartDrawer` / `CartLineItem`)
The order docket torn off the pad: number, dish, quantity stepper, line total in the same tabular column.
**No thumbnails** — a docket is written, not illustrated. The subtotal sits above a full-width ADD-weight
checkout plate.

### Empty / Loading / Redirect states
Empty is a dashed ruled panel with the reason set in the section-band voice — no icon-in-a-circle, nothing
on a menu is drawn in a circle. Skeletons mirror the dish line exactly (number slot, name, price column),
so the page does not change shape when it prints. Redirecting is the press still running: a `hunger-wipe`
plate looping across a `--wash` bar.

### Focus & Attention
- Anything that can carry a pseudo-element (`a, button, summary, [role=radio|tab|option], [tabindex]`)
  gets the crop marks on `:focus-visible`, with `outline: none` and **`box-shadow: none`** — libs/ui's
  Button paints its own 3px ring as a box-shadow, and it is suppressed here rather than forked there.
- Form controls (`input, textarea, select, [role=combobox]`) **keep a 2px primary outline at 2px offset**.
  They cannot carry a pseudo-element, and focus must stay visible. This exception is deliberate; do not
  "unify" it away.
- `.crop` reuses the identical `--crop-marks` variables for hover/`focus-within` on links, logos, the
  slider frame and dish lines, at `opacity 0 → 1` over `--motion-fast`.

### Motion
`--motion-fast` 90ms and `--motion-base` 150ms with `--easing-standard` carry every colour transition.
`--motion-slow` 480ms with `--easing-emphasized` `cubic-bezier(0.9, 0, 0.1, 1)` belongs to one moment.

**`.impression` — the one authored moment.** Adding a dish wipes the primary plate across that whole line
(`clip-path: inset(0 100% 0 0) → inset(0)`, then fading out; 480ms), mirrored in RTL by a swapped keyframe
(`hunger-impression-rtl`) selected on `[dir="rtl"]`. Under `prefers-reduced-motion: reduce` it degrades to
`hunger-impression-still`, a plain fade on the same clock. Afterwards the dish's number box stays filled
showing `×N`. The line's own timer clears at 520ms.

### Named Rules
**The Printed State Rule.** State is printed where the thing is — an outline mark in the line, a filled
number box, a lit tab. Never a toast, never a floating badge over a photo.

**The Crop Mark Rule.** Attention in this theme is the registration crop mark. Never introduce a browser
ring, a glow, or a coloured outline on anything that can carry `::after`.

**The One Impression Rule.** `.impression` is the only authored animation in the theme. Nothing enters,
nothing fades in on scroll, nothing bounces; all content is visible by default.

## Do's and Don'ts

### Do:
- **Do** print a product as a line: number, thumb, name, dotted leader, price, action.
- **Do** keep `productGrid` at `{base:1, sm:1, lg:1, xl:1}` and every price in one tabular column.
- **Do** use `.press` for anything that names or counts and Geologica for anything that must be read.
- **Do** reach for `.fold`, `.plate`, `.mark`, `.dish-no`, `.crease`, `.crop`, `.press` and `.price`
  before writing new CSS; the world grammar already exists.
- **Do** keep new world-grammar CSS inside `@layer components` so Tailwind utilities on the same element
  still win.
- **Do** overlap adjacent bordered items by one pixel so two borders read as one rule.
- **Do** render `.leader` as `hidden sm:block`. It only makes sense where the price shares the row; on
  phones the price/action row's dotted top rule carries that function. Do not "fix" it back to always-on.
- **Do** change colour by editing the `THEME_DEFAULTS.hunger` OKLCH seed in
  `libs/types/scripts/build-color-schemas.mjs` and running `npm run gen:colors` in `libs/types`.
- **Do** keep the masthead short — the first section band and its dish lines must be reachable without
  scrolling, and the masthead must look finished with one slider image and with none.
- **Do** put every string through `t()`; this theme adds no literal UI text and added no new locale keys.

### Don't:
- **Don't** give anything a radius, a gradient, or a shadow that belongs to the sheet.
- **Don't** turn dishes into photo cards, or make an image the subject of a line.
- **Don't** author colour roles in `tokens.css` or hand-edit `src/colors.ts`; both are owned elsewhere.
- **Don't** introduce a third ink beyond `--wash`, the spot-green offer mark, and the shell's semantic
  form roles.
- **Don't** add entrance animations, scroll reveals, carousels between sections, or a second authored
  motion moment.
- **Don't** use a toast, a floating badge, or an icon-in-a-circle for any state.
- **Don't** put a Latin face first in the `:lang(ar)` stacks, or apply `text-transform: uppercase` /
  letter-spacing to Arabic text.
- **Don't** remove the 2px primary outline on form controls; they cannot carry crop marks.
- **Don't** repeat categories in the standing nav — the fold strip is the category index.
- **Don't** reformat prices; they arrive pre-formatted from the API.
- **Don't** build UI for text search, reviews or ratings, wishlists, promotions or coupons, or table
  reservations. The platform has none of them; this theme never pretends to book a table. Search is
  suggestions-only over categories and pages, and says so in its panel.
- **Don't** edit `storefront/**`, fork `libs/ui`, or re-implement behaviour from `libs/hooks`. Restyle
  through tokens, variants and `className` only, and compose the shared hooks for cart, listing, purchase,
  search and user.

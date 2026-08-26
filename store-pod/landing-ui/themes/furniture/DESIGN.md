---
name: Furniture
description: The Home Floor Directory — a whole-home retailer rendered as its own department-store directory.
colors:
  primary: "#005A43"
  primary-hover: "#004532"
  primary-focus: "#003324"
  secondary: "#E2DDD4"
  secondary-hover: "#CBC7BD"
  accent: "#A82000"
  background: "#F9F6F2"
  foreground: "#1B1612"
  border: "#DBD5C9"
  neutral: "#B1A9A3"
  outline: "#235745"
  error: "#B7191C"
  warning: "#EE9E10"
  success: "#007533"
  info: "#0065A1"
typography:
  display:
    fontFamily: "Archivo, Golos Text, Tajawal, sans-serif"
    fontSize: "3rem"
    fontWeight: 800
    lineHeight: 0.95
    letterSpacing: "0.055em"
    fontVariation: "wdth 122"
  headline:
    fontFamily: "Archivo, Golos Text, Tajawal, sans-serif"
    fontSize: "1.75rem"
    fontWeight: 800
    lineHeight: 0.95
    letterSpacing: "0.055em"
    fontVariation: "wdth 122"
  title:
    fontFamily: "Archivo, Golos Text, Tajawal, sans-serif"
    fontSize: "1.375rem"
    fontWeight: 800
    lineHeight: 0.95
    letterSpacing: "0.055em"
    fontVariation: "wdth 122"
  floor-number:
    fontFamily: "Archivo, Golos Text, Tajawal, sans-serif"
    fontSize: "3rem"
    fontWeight: 800
    lineHeight: 0.8
    letterSpacing: "-0.01em"
    fontFeature: "tnum 1, lnum 1"
    fontVariation: "wdth 124"
  figure:
    fontFamily: "Archivo, Golos Text, Tajawal, sans-serif"
    fontSize: "0.875rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "normal"
    fontFeature: "tnum 1, lnum 1"
    fontVariation: "wdth 104"
  body:
    fontFamily: "Golos Text, Tajawal, ui-sans-serif, system-ui, sans-serif"
    fontSize: "1rem"
    fontWeight: 400
    lineHeight: 1.55
    letterSpacing: "normal"
  label:
    fontFamily: "Archivo, Golos Text, Tajawal, sans-serif"
    fontSize: "0.625rem"
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "0.09em"
    fontVariation: "wdth 116"
rounded:
  control: "0.1875rem"
  card: "0.1875rem"
  badge: "0.125rem"
  overlay: "0.25rem"
  image: "0"
spacing:
  unit: "0.25rem"
  gutter: "1.25rem"
  gutter-lg: "2.5rem"
  section: "3.5rem"
  section-lg: "5.5rem"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    height: "3rem"
    padding: "0 1.5rem"
  button-primary-hover:
    backgroundColor: "{colors.primary-hover}"
  button-outline:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "0.625rem 0.875rem"
  button-outline-hover:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
  board-action:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "0.75rem 1.25rem"
  plate-number:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    typography: "{typography.floor-number}"
    rounded: "{rounded.card}"
    size: "3.5rem"
  price-plate:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    typography: "{typography.figure}"
    rounded: "{rounded.card}"
    padding: "0.25rem 0.5rem"
  state-plate:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.label}"
    rounded: "{rounded.badge}"
    padding: "0.125rem 0.4rem"
  product-window:
    backgroundColor: "{colors.secondary}"
    rounded: "{rounded.image}"
  nav-item:
    textColor: "{colors.foreground}"
    typography: "{typography.label}"
    rounded: "{rounded.control}"
    padding: "0.5rem 0.5rem"
  nav-item-hover:
    backgroundColor: "{colors.secondary}"
  input-field:
    backgroundColor: "{colors.background}"
    textColor: "{colors.foreground}"
    typography: "{typography.body}"
    rounded: "{rounded.control}"
    height: "2.25rem"
---

# Design System: Furniture

## Overview

**Creative North Star: "The Home Floor Directory"**

A whole-home retailer is not a catalogue, it is a building with floors. This theme renders the shop as its own department-store directory: departments carry floor numbers and product counts on one enamel board, every product group is a numbered plate, and the same floor number follows a shopper from the board to the department plate to a product's floor tag to the footer colophon to the mobile drawer. Wayfinding is the whole design; the shopper always knows which floor they are standing on.

The material world is public architecture, not a showroom. The ground is flat bone plaster and the ink is warm graphite; the merchant's PRIMARY appears as whole vitreous-enamel fields — the directory board, the department plate, the numbered plate square, the price plate on every card, the one action per view — each with a lit inner edge and a real mounted shadow. Brass hairlines separate directory rows and plate-key entries. Aggregate (terrazzo) exists only in the landings between departments, never as a page-wide field. Photographs are cut square in hairline windows and never float.

It refuses the airy-white Scandi arrangement that every furniture site ships — whitespace here is architecture, not absence — and refuses the dark spotlit showroom just as hard. Density is generous but structural: wide gutters, big tabular numerals, and lettering that behaves like signage rather than branding.

**Key Characteristics:**
- Floor numbers as the single wayfinding device, from one source of truth
- Enamel fields of the merchant's primary, not thin accents
- Every figure — counts, prices, quantities, floor numbers — in one tabular slot
- State printed as a word plus a figure, never as a tint
- Square-cut photographs in hairline windows; terrazzo only between departments
- Exactly one authored motion (a figure that rolls when it changes)

## Colors

A flat bone-plaster ground carrying warm-graphite ink, with the merchant's primary committed as whole enamel fields; the statement colour is held back for the rare signal.

Colour ROLES are not owned by this theme. They arrive at runtime from the merchant's `ColorTheme` preset through the shell's colour bridge (an inline style on `<html>`); the values in the frontmatter are the theme's own generated default palette (`src/colors.ts`, seeded from `THEME_DEFAULTS.furniture` in `libs/types/scripts/build-color-schemas.mjs`). Never hand-edit `colors.ts`; edit the seed and run `npm run gen:colors`. The theme must therefore read every colour through the role tokens, and must survive a dark preset (MIDNIGHT) and a hot one (DESERT_MIRAGE) unchanged.

### Primary
- **Enamel Green** (`#005A43` in the default palette): the sign material. It is used as a whole field — the directory board, the department plate on category, the numbered plate square in a section head, the price plate on every product card, the selected gallery thumb and selected option, and the single decisive action per view. Exposed in the theme as `--enamel` / `--enamel-ink`.

### Secondary
- **Bone Wash** (`#E2DDD4`): the quiet surface. Every hover and highlight that is not a commitment — menu rows, select rows, nav triggers, sub-department chips — lands here, and it is the tint the terrazzo band is mixed from.

### Tertiary
- **Signal Orange** (`#A82000`): the platform's statement colour. In this world it is a signal, not a surface; it appears only where the bridge itself puts it and in the warm speckle of terrazzo (`--speckle-warm`). It is deliberately kept out of hover states.

### Neutral
- **Bone Plaster** (`#F9F6F2`): the page ground, flat and unpatterned. Exposed as `--plaster`.
- **Warm Graphite** (`#1B1612`): body ink, and the solid field of the utility rail above the masthead. Exposed as `--ink`.
- **Brass** (`color-mix(--foreground 28%, --accent 16%)`): the hairline between directory rows, plate-key entries, and section rules. Its faint sibling (`--brass-faint`, foreground at 14%) frames windows and closes the footer.

### Named Rules

**The Enamel Field Rule.** The primary is spent as whole fields, never as a hairline or a thin accent. If a surface carries the primary it carries all of it, with the lit inner edge (`--enamel-edge`) and a mounted shadow. There is one enamel *action* per view; the boards, plates and price plates are the field.

**The Printed State Rule.** State is printed, never tinted. Sale, low stock, sold out, "in stock" — each is a bordered plate carrying a word and, when there is one, a figure. Strip every colour from the page and every state still reads.

**The Accent-Is-Not-A-Hover Rule.** In this platform's colour bridge `accent` is the STATEMENT colour. The shared shadcn primitives use `accent` as their neutral hover surface, which would flash signal orange on every menu row. `tokens.css` re-points those hovers to the bone wash (`--secondary`), and the open/selected nav trigger to the enamel. Those rules are **deliberately unlayered** — the primitives set their colours with Tailwind utilities in `@layer utilities`, which beat any layered rule at any specificity. Moving that block into `@layer components` silently breaks it.

## Typography

**Display Font:** Archivo Variable, `wdth` axis (fallbacks: Golos Text, Tajawal, sans-serif) — the enamel sign
**Body Font:** Golos Text (fallbacks: Tajawal, ui-sans-serif, system-ui) — everything read
**Arabic:** Tajawal, on both roles

**Character:** Signage and reading kept strictly apart. Archivo is set expanded (`wdth` 116–124), heavy and tracked, the way a directory board is lettered; Golos Text sets calm, roomy running copy and every form field and table. There is no third voice — the "mono" role is Golos Text with tabular figures.

### Hierarchy
- **Display** (`.sign-lg`, 800, `wdth` 122, 1.75–3rem, LH 0.95, LS 0.055em, mixed case, `text-wrap: balance`): the building's name on the board, a department name, a page name, a product name. Mixed case on purpose — tracked caps at this size break long merchant names and cannot be set in Arabic at all.
- **Floor number** (`.floor-no`, 800, `wdth` 124, up to 4.25rem, LH 0.8, tabular lining): the biggest figure on the page. Directory rows, department plate, product floor tag, mobile drawer, and the struck-through `00` on not-found.
- **Sign / label** (`.sign`, 700, `wdth` 116, 0.5625–0.6875rem, LS 0.09em, uppercase): every caption, column heading, nav item, button, breadcrumb, field label, table head. This is the most-used role in the theme.
- **Figure** (`.figure`, 700, `wdth` 104, tabular lining, inherits size): counts, prices, quantities, SKUs, phone numbers, plate-key values.
- **Body** (400, 1rem, LH 1.55): product and page copy. Running CMS copy (`.copy`) relaxes to LH 1.7 and caps at 68ch; empty/error bodies cap at 48ch.
- **Plate head H2 inside copy** (`.copy h2`, 800, `wdth` 122, 1.125rem, uppercase, LS 0.055em): merchant-authored HTML is re-lettered as building signage rather than left as browser defaults.

### Named Rules

**The One Slot Rule.** Every figure on the site sits in the same tabular slot (`.figure`, `tnum`/`lnum`). A number never renders in the body face, and never in proportional figures — the directory column, the cart tally, the quantity stepper and the price plate all align on the same rhythm.

**The Script Lead Rule.** `:lang(ar)` leads both roles with Tajawal and `:lang(ru)` leads the sign role with Golos Text, because next/font's metric fallback for Archivo/Golos carries those glyphs and would otherwise catch them before the intended face — a Russian board would silently letter itself in Arial. Arabic additionally drops uppercase, tracking and the width axis on `.sign`, `.sign-lg`, `.floor-no`, `.figure`, `.state-plate` and copy heads: Arabic has no case, and tracking that flatters caps only breaks the joins.

**The No-Kicker Rule.** A sign is a heading, not a line above one. Facts that would become a kicker — maker, catalogue number, floor, availability — sit in the record rule *below* the product name, never as an eyebrow above it.

## Layout

A single content measure (`--width-content`, 84rem) with architectural gutters: 1.25rem below `lg`, 2.5rem above. Section rhythm is 3.5rem, opening to 5.5rem at `lg`. The narrow measure (44rem) carries error, redirect and checkout-adjacent pages.

**The entrance is two rows from `lg` up** (`--header-h-lg` 6.5rem, `heightPx.lg` 104 in `config.ts`): a thin warm-graphite utility rail carrying the store's own facts, information pages, language, account and the basket ticket; beneath it the masthead with mark, departments and search. Below `lg` the rail folds away and the masthead is a single 3.5rem row (`--header-h`) with the drawer trigger. Information pages live on the rail rather than the masthead, because long page titles ("Contact <store name>") push a department off the end of the row.

**The first viewport** is a two-column grid: the enamel directory board on the start side, the merchant's slider in a hairline window on the end side (`minmax(0,1fr) minmax(0,1.15fr)` at `lg`). The window is a grid cell that stretches to the board's height, so its width is only final after the board lays out — Swiper is therefore initialised with `observer`, `observeParents` and `resizeObserver`, or it holds a stale width and the slide track ends short of the frame. Mobile stacks the board first, the window under it at a 15rem floor.

**Product grids** follow `layoutConfig.productGrid`: 2 / 2 / 3 / 4 across base / sm / lg / xl, `gap-x-5 gap-y-10` opening to `gap-x-8 gap-y-14`. Product images are portrait 4/5 because a whole-home retailer sells objects that stand in a room. Longer groups run as a rail instead of a grid.

**Landings.** Home alternates: every second product group sits on a terrazzo band bounded by faint brass rules, so the scroll has floors between departments. The footer sits on terrazzo too, so the page closes on ground rather than trailing off.

### Named Rules

**The Landing Rule.** Terrazzo is a floor between departments, never a page-wide field. A faint dot pattern tiled across the whole page reads as a grid overlay, not as plaster; the body carries no pattern at all.

**The Honest Column Rule.** A column that has no data is removed, not filled with dashes. The directory board drops its ITEMS column entirely when no department reports a count; the plate key does not render when the merchant listed no attributes, and the product page then gives the description the full measure instead of hanging a heading over one grey line.

## Elevation & Depth

Mostly flat, with depth carried by hairlines and one real mounted shadow. Signs are mounted on a wall: `.enamel` combines a lit inner edge (`inset 0 0 0 1px` of the ink at 22%, the way vitreous enamel catches light) with a genuine offset, blurred shadow. `.enamel-flat` is the same field set *into* the page — inner edge only, no lift — and it is what the board, the department plate, the announcement strip and every drawer head use. Everything else in the theme is flat and separated by brass hairlines or by the terrazzo band.

### Shadow Vocabulary
- **Enamel edge** (`inset 0 0 0 1px color-mix(in srgb, var(--primary-foreground) 22%, transparent)`): inside every enamel field, always.
- **Mounted small** (`0 1px 2px -1px rgb(0 0 0 / 0.10)`): the bone action on the board, the hero caption plate.
- **Mounted medium** (`0 6px 16px -8px rgb(0 0 0 / 0.22)`): a mounted enamel panel, the nav floor list.
- **Mounted large** (`0 14px 34px -14px rgb(0 0 0 / 0.26)`): reserved for lifted panels.
- **Overlay** (`0 28px 64px -20px rgb(0 0 0 / 0.38)`): drawers, dialogs, search suggestions.
- **Gallery selection** (`0 0 0 2px var(--primary)`): the only ring in the theme; it marks the selected view.

### Named Rules

**The Mounted Sign Rule.** A shadow means a sign is hung on the wall — offset and blurred, never a symmetric halo and never a hard offset block. Anything not mounted is flat and bounded by a brass hairline.

## Shapes

Near-square throughout. Vitreous enamel has a shallow, constant corner: controls, cards and enamel fields all sit at 3px (`--r-control` / `--r-card`), state plates at 2px, overlays at 4px. **Photographs are cut square at radius 0** and set in `.window` — a hairline `--brass-faint` border over a bone-wash ground, overflow hidden — so an image is always framed, never floating and never rounded. Rules are 1px: brass between directory rows and plate keys, a lighter enamel-ink rule inside enamel fields (`.rule-enamel`), dotted brass for the plate-key leader lines. The only drawn geometry in the theme is `PlanPlate`, a hairline plan of a room (walls, rug, sofa, table, lamp, shelving) with a dimension line carrying the building's floor count — it fills the hero window when the merchant supplied no slider image, in place of a grey placeholder box.

## Components

### Buttons
- **Shape:** shallow 3px corner (`--r-control`); lettering is always `.sign` at 0.625–0.6875rem.
- **Primary:** solid enamel field with its ink; the single decisive action per view (add to cart at `h-12`, checkout, retry).
- **Outline / secondary:** transparent over plaster with a brass hairline; hover commits to the full enamel field (`hover:bg-primary hover:text-primary-foreground`) rather than to a tint.
- **On enamel:** an action placed on a board inverts — bone plaster field, graphite text, small mounted shadow, `hover:translate-y-px` (the only press affordance in the theme).
- **On the ink rail:** controls inherit `currentColor` and hover to `bg-current/15`, so the same three controls work on the graphite rail and on the plaster masthead.
- **Disabled:** faint brass border, muted text, no hover.

### Chips
- **Sub-department chips** (category header) and **option values** (buy box): brass-hairline outline, `.sign` lettering, 3px corner, optional count in the figure slot; hover fills with enamel. Selected option values become a borderless enamel field; unavailable combinations are dashed, muted and struck through rather than merely dimmed.

### Cards / Containers
- **Product card:** square window over 4/5 portrait; secondary image cross-fades on hover; badges stack at the start-top corner; name in body face, clamped to two lines; **price on its own small enamel plate** so colour commits across the whole grid instead of hiding on one button; a full-width brass-outlined action closes the card (add to cart, or "view details" when the product has variants).
- **Directory board:** `.enamel-flat`, padded 1.5–2.5rem, store name as display, optional facts line, the action, then a table — column heads at 0.6875rem over an enamel rule, one row per department (floor number · name · count · chevron), enamel rules between, row hover a 12% wash of the sign ink. The chevron closes every row at the measure whether or not the count column exists.
- **Section head (`SectionHeading`):** an enamel number square, the plate name, the count in the figure slot, then a brass hairline running to the end of the measure.
- **Windows:** `.window` is the universal image container — cart thumbs, gallery, hero, product cards.

### Inputs / Fields
- **Style:** shared `Input`/`Select`/`Textarea` primitives restyled only by brass hairline and 3px corner; labels are `.sign` at 0.625rem in muted ink, sitting above the field.
- **Focus:** the platform ring on the primary role (`--ring`), plus `caret-color` and `accent-color` set to the primary so native controls and the scrollbar carry the world.
- **Error:** `aria-invalid` on the control and a destructive-coloured message below; the field container carries `data-invalid`.
- **Search:** hidden entirely when nothing is searchable; when the platform offers suggestions only, the suggestion panel says so on its own ruled caption line rather than pretending to search text.

### Navigation
- **Masthead:** departments only, `.sign` at 0.625rem, hover on the bone wash. A department with children opens a ruled floor list — a plate hung under the sign (plaster ground, brass border, 3px corner, mounted-medium shadow), with a "view all" row over a brass rule and sub-departments carrying their counts in the figure slot. It splits into two columns only past five children, so a short list never opens a half-empty slab.
- **Open trigger:** the sign lights up as a full enamel field.
- **Mobile:** a start-side drawer under an enamel head reading DIRECTORY, carrying the same floor numbers, names and counts as the board, with accordions for sub-departments. Drawer open state is keyed to the pathname it was opened on, so navigation closes it without an effect.

### Signature Components

**The floor number.** `src/components/floors.ts` is the single source: a floor is the ROOT department's position in the merchant's own category order, zero-padded, and the lookup walks the whole subtree so a sub-category inherits its root's floor. Board, department plate, product floor tag, footer colophon and mobile drawer all read from it, so they cannot drift. Not-found borrows the same grammar: a directory row with a struck-through `00` and a real row beneath it pointing back at the board.

**The plate key** (`PlateKey`): the product record as a furnishing catalogue's numbered plate list — a zero-padded figure, the merchant's attribute label, a dotted brass leader running to the measure, and the value in the figure slot. It carries only what the merchant filled in, and deliberately repeats nothing the buy box printed two hundred pixels above.

**The figure slot** (`Figure`): the theme's one authored motion. A figure that changes rolls in place in its slot (`.roll-slot` / `.rolling`, 480ms on the emphasized easing), driven by derived state so the roll replays exactly once per real change and never on mount. Used for the basket tally, listing totals, cart line totals and the buy-box price.

**The lift bar** (`Redirecting`): the only other moving thing — an enamel bar travelling inside a brass-outlined track while a redirect is in flight. Both animations are disabled under `prefers-reduced-motion`, where the lift bar becomes a full static bar.

## Do's and Don'ts

### Do:
- **Do** spend the primary as whole enamel fields with the lit inner edge, and keep to one enamel *action* per view.
- **Do** put every number in the `.figure` tabular slot, and use `Figure` when the number can change while the page is open.
- **Do** print state as a `StatePlate` — a word, plus a figure when there is one.
- **Do** take floor numbers from `src/components/floors.ts` and nowhere else.
- **Do** letter signage with `.sign` / `.sign-lg` and set everything read in Golos Text.
- **Do** drop a column, a key or a whole section when the data is absent, rather than rendering dashes or an empty heading.
- **Do** give Swiper `observer` / `observeParents` / `resizeObserver` anywhere it lives inside a stretching grid cell.
- **Do** keep world grammar inside `@layer components` in `tokens.css` so Tailwind utilities on the same element still win.
- **Do** wrap merchant addresses in `<bdi>` and set SKUs and phone numbers `dir="ltr"`.

### Don't:
- **Don't** move the two unlayered blocks at the top of `tokens.css` into `@layer components`. The shadcn primitives paint with Tailwind utilities in `@layer utilities`; layering those overrides silently breaks the drawer heads and re-admits signal orange as a hover surface.
- **Don't** use `accent` as a hover or highlight surface. It is the statement colour here; the quiet hover surface is the bone wash and the committed surface is the enamel.
- **Don't** round a photograph. Images are cut square in `.window`; the 3px corner belongs to controls, cards and enamel fields.
- **Don't** tile terrazzo across the page or the body. It is a landing between departments and the footer ground.
- **Don't** autoplay the hero or add a second animation. One authored motion (the rolling figure) plus the redirect lift bar is the whole motion budget; an unpausable carousel is both a second motion and a WCAG 2.2.2 failure.
- **Don't** set tracked caps or the `wdth` axis on Arabic text, and don't let Archivo lead the stack for `ar` or `ru`.
- **Don't** put a line above a heading. Facts belong in the record rule below the name.
- **Don't** hard-code colour values. Read the role tokens; the merchant's preset replaces the whole palette at runtime, and the theme must hold up under a dark preset.
- **Don't** hand-edit `src/colors.ts` — edit `THEME_DEFAULTS.furniture` in `libs/types/scripts/build-color-schemas.mjs` and regenerate.
- **Don't** reformat a price. Prices arrive formatted from the API; the theme only places them in the figure slot.

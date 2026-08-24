# Catalogue & Inventory — the design pass

## Context

The functional QA pass landed (519 tests, request storm fixed, groups listing, name search working).
Reviewing it, the user found seven **design** problems, and asked me to go back to
`store-core/console-template/Inventory.dc.html` and `Add Product.dc.html` for the intended
vocabulary. Reading those two files changes three of the answers, because the template already
decided things I invented:

- **Filters are compact chips in the panel header, not a rail of wide inputs.** The template's
  search box is a fixed `width: 230px` with `padding: .45rem .75rem`; its category filter is a
  `.45rem .7rem` chip with a chevron, sitting in the table's header row beside the tabs. I built
  three controls at `flex: 1 1 11rem` that grow to 372px each. That is the "big input" complaint.
- **The image drop zone is a cell in the image grid, not a panel below it.** `Add Product.dc.html`
  lays images out `grid-template-columns: repeat(4, 1fr)` and makes the dashed drop target the
  **last cell**, the same size as a card. Mine is a separate 240px well under a vertical list, which
  is why it reads as being in the wrong place.
- **Number fields have no browser chrome.** The template renders a bordered box with a `$` prefix
  and the figure beside it — no spinners, no native number styling. The console uses
  `<input type="number">` in nine places.

Verified in the browser, not assumed:

- **Panel spacing was still zero on the add page.** Three of the four steps wrap their panels in a
  `<div [formGroup]>`, so my `.step-body > *` rule applied `gap: 16px` to a container holding exactly
  one child while the panels inside it stayed flush. Measured gap between *Identifiers* and *Names
  and descriptions*: **0px**. (Already fixed with an explicit `.step-stack` class before this plan.)
- **Quantity is not localised.** In Arabic the price cell reads `٧٥٠٫٠٠ ر.س.` and the quantity cell
  reads `25`. The column *header* is translated; the figure is not.
- **`app-select` only ever opens downward**, so the Dimensions unit at the bottom of the pricing step
  opens past the end of the page.

## Decisions (settled with the user)

| Question | Decision |
|---|---|
| Chosen members in the related/group pickers | **Wrapping chips with an ×, capped height.** The block scrolls past its cap so adding never grows the page. |
| Scope of the number field | **Everywhere**, including the products table's inline price and quantity edit. |

---

## 1. `shared/ui/number-field` — the numeric control

New, following `date-picker.ts` for the CVA and `.control` for its metrics.

- `ControlValueAccessor` over `model<number | null>(null)`, `NG_VALUE_ACCESSOR` + `forwardRef`,
  `formDisabled` signal ORed with a `disabled` input — the idiom used by `DatePicker`, `RichText`
  and `Select`.
- **`type="text"` with `inputmode="decimal"`, not `type="number"`.** The native control brings spin
  buttons that no other field in this console has, silently discards a value it considers malformed
  (so `control.value` becomes `null` while the operator still sees their digits), and takes the
  scroll wheel. Parsing here is explicit.
- Inputs: `prefix` (currency), `suffix` (unit), `min`, `max`, `step`, `decimals`, `ariaLabel`,
  `invalid`. `dir="ltr"` always — a figure is not prose.
- `null` is preserved and distinct from `0`: an empty price is "not priced yet", which
  `product-form.service.ts` already relies on.

**Consumers** — nine native inputs plus the table: price and quantity (`pricing-step`), weight,
length, width, height (`pricing-step`), sort order, and the inline price/quantity editors in
`products.html`. Currency becomes the price field's `prefix`, so the bespoke `SAR` adornment in
`pricing-step.html` goes.

## 2. `app-select` — open upward when there is no room

`openList()` measures the trigger against the viewport and adds a `drop-up` class when the list
would overflow the bottom; the popover then anchors `inset-block-end: 100%`. Measured after render
via `afterNextRender`, because the list has no height until it exists. Re-checked on open only —
not on scroll, which would be a listener per select for a case that cannot happen while the list is
open and focus-trapped inside it.

## 3. Products list — filters as the template draws them

- The search box becomes **fixed-width** (`width: 230px`, no `flex: 1`), with its leading icon, per
  `Inventory.dc.html:99`.
- Category and brand become **compact selects** — `.45rem .7rem`, sized to their content with a
  sensible `max-inline-size`, not stretched to a third of the table each.
- All of it moves onto the panel's header row with the availability tabs, matching the template's
  single `padding: 1.1rem 1.25rem` header, instead of the separate rail below it.
- `Clear filters` stays a trailing ghost button on that row.

## 4. Quantity localised

`products.api.service.ts` already formats price through `TranslocoLocaleService`. Quantity is
rendered raw. Route it through the same service so Arabic gets `٢٥`, and do the same for the
inline-edit display. The **editor** stays Latin — an operator types `25`, not `٢٥`.

## 5. Media step — the image grid from the template

Replace the vertical list plus separate well with `Add Product.dc.html`'s layout:

- `grid-template-columns: repeat(auto-fill, minmax(9rem, 1fr))`, each image a card: preview,
  a `MAIN` badge on the first, and hover actions for *set as main* and *remove* in the corner.
- The **drop zone is the last cell of that grid**, dashed, the same size as a card.
- Keep the existing move-up/move-down buttons for ordering — the platform has no drag reorder here
  and inventing one is out of scope — but move them onto the card.
- The image rules notice stays, as the template's info bar.

## 6. The two product pickers — related products, and group members

Both currently render a full-width autocomplete whose results push the page down, over a list that
also grows downward. One shared shape instead:

- **Results in an overlay.** `app-autocomplete`'s panel already uses `.popover`; give it
  `position: absolute` so results float over the content rather than displacing it. This fixes both
  pickers at once and is where the "page height increases" comes from.
- **The trigger is compact** — `max-inline-size: 22rem`, not the full panel width.
- **Chosen items are wrapping chips**, each with an ×: `display: flex; flex-wrap: wrap; gap: .4rem`,
  `max-block-size: 12rem; overflow-y: auto`, so a group with 24 members is a stable block rather
  than a page that doubles in height. The count goes in the section header.
- Applied in `organize-step.html` (related products) and `group-tab.html` (members), off one shared
  stylesheet so they cannot drift — the same reason `editor-card.css` is shared today.

## 7. Tests

| File | What it must prove |
|---|---|
| `shared/ui/number-field/number-field.spec.ts` | CVA round trip; `null` survives and is not `0`; no `type="number"`; a typed non-numeric is refused without wiping the control; prefix and suffix render; min/max reported through `app-field-error` |
| `shared/ui/select/select.spec.ts` | Extended: a trigger near the viewport bottom opens upward |
| `products.spec.ts` | Quantity renders in the active locale's numerals; the filter controls are not full-width |
| `product-form.spec.ts` | The media grid puts the drop zone last; the related picker's results do not sit in normal flow |
| `catalogue.spec.ts` | Group members render as removable chips and the block is height-capped |

## 8. Verification

Live stack through the gateway (`run-lcl.sh` supervises `console-ui` on 8011 — never `kill` it; use
`restart console-ui`).

1. `/products/new`: measure the gap between *Identifiers* and *Names and descriptions* — must be
   16px, not 0.
2. Pricing step at the bottom of the page: open the Dimensions unit and confirm the list opens
   **upward** and is fully visible.
3. `/products`: measure the search box (≈230px, not 372) and confirm the filters sit on the panel
   header row. Switch to Arabic and confirm the quantity cell reads `٢٥`.
4. Media step: the drop zone is the last cell of the image grid, cards are uniform, set-main and
   remove work.
5. Type in the price field: no spinners, no scroll-wheel change, the currency shows as a prefix,
   clearing it leaves `null` and the readiness item unticks.
6. Organize step and the Groups tab: results float over the content, the page height does not change
   when results appear, members are chips, and adding a 25th does not lengthen the page.
7. Forest / Midnight / Daylight, English and Arabic RTL, on every screen touched.
8. `npm run build`, `npm test`, `npx eslint .`, and `git status --porcelain` empty for
   `store-core/seller-ui` and `store-pod`.

## Critical files

**New:** `shared/ui/number-field/*` (+ spec), `features/product-form/components/product-picker.css`
(shared by the two pickers).

**Changed:** `shared/ui/select/{select.ts,select.css}` (drop-up),
`shared/ui/autocomplete/autocomplete.css` (overlay results),
`features/products/{products.html,products.css}` (filters onto the header row),
`features/products/services/products.api.service.ts` (localised quantity),
`features/product-form/components/pricing-step/*` (number fields),
`features/product-form/components/media-step/*` (image grid),
`features/product-form/components/organize-step/*` (picker),
`features/catalogue/components/group-tab/*` (picker), `src/locale/{en,ar}.json`, `lessons.md`.

**Not modified:** `store-core/seller-ui`, `store-pod`.

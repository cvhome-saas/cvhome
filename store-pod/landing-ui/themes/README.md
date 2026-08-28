# Storefront themes — direction catalog

A theme is a package under `themes/<id>/` implementing the `ThemeDefinition` contract (`libs/theme`). One
storefront app serves all of them; the merchant's `Theme` enum picks one. Colours: every theme ships **its own
default palette** (`src/colors.ts`, generated from the `THEME_DEFAULTS` seed in
`libs/types/scripts/build-color-schemas.mjs`, wired as `tokens.defaultColors`) — that is what renders when the
merchant's `ColorTheme` is `DEFAULT` or unset. Any fixed preset the merchant picks (`LIGHT`, `MIDNIGHT`, …)
replaces it whole. QA: `?theme=<id>&color=<PRESET|default>` (dev/QA cookies). How to build one:
`.agents/skills/project-structure/references/new-landing-ui-template.md`.

## Shipped

| id | status | notes |
|---|---|---|
| `starter` | reference | deliberately undesigned; the copy source for `npm run new-theme <id>`; the fallback, and what `DEFAULT` and every retired enum value resolve to |
| `beauty` | built (v1.0.0) | Industrial Quote Grammar for a beauty + fashion boutique — ink plates on a 1px grid, 45° hazard stripes, straight-quoted labels, condensed caps display (Oswald) + mono facts (JetBrains Mono), the merchant primary as the single zip-tie-tag accent; `themes/beauty/DESIGN.md` |
| `fashion` | built (v1.0.0) | The Wheatpaste Wall for streetwear / drops — a rendered wall under pasted paper posters (≤1.2° tilts, offset shadows, a peeling corner on the big ones), the merchant primary as day-glo paper on every primary action, state as rubber stamps (SALE / SOLD OUT / ONLY N LEFT / ADDED), Anton + Changa poster caps, Rubik body; typographic posters when the merchant gave no picture; `themes/fashion/DESIGN.md` |
| `grocery` | built (v1.0.0) | Cash & Carry for food / grocery consumables (the `fresh` brief below) — the shop as a warehouse floor: concrete ground, the merchant primary as safety-yellow price boards (hero board, active aisle tile, every action), crate cells with a 2px hardware line and stepper-first quick-add, an aisle-board category strip, states printed as stickers, the ADDED stamp as the one motion moment, and an honest 12-segment load meter in the basket drawer; Manrope + Fira Sans Extra Condensed (Almarai leads Arabic); selected by the `GROCERY` enum value; `themes/grocery/DESIGN.md` |
| `pink` | built (v1.0.0) | Tokyo Girls Issue for a mixed girly-lifestyle store aimed at teenage girls and young women in Japanese girls' visual culture — the shop as this month's fashion magazine: a flooded screentoned cover carrying the store name at cover scale with the product groups as numbered cover lines, ruled plates of die-cuts sharing every hairline, the merchant ACCENT as the notched price flag every figure rides, marker annotations (sale %, only N left, sold out) drawn in SVG over the goods, a solid ink colophon, and one motion moment — the ADDED flag snapping onto a cell when the basket's count actually rises; Dela Gothic One + M PLUS Rounded 1c (Cairo leads Arabic per glyph); selected by the `PINK` enum value; `themes/pink/DESIGN.md` |
| `hunger` | built (v1.0.0) | The Letterbox Menu for food / restaurants — the storefront as the folded takeaway menu posted through the door: dishes are printed lines, never cards (order code, printed thumb, name, dotted leader, price, ADD), sections are bands of the merchant primary, **one** tabular price column at every width (`productGrid` is 1/1/1/1 on purpose), state prints as outline marks, attention is a registration crop mark rather than a focus ring, and the one motion moment is the "impression" — the primary wiping across the line you just ordered, after which its number box holds ×N. Zero radius everywhere; Alumni Sans + Geologica (Alexandria leads Arabic). Tuned for a **full restaurant menu ordered into the cart** — the platform has no reservation API, so it never offers table booking; `themes/hunger/DESIGN.md` |
| `furniture` | built (v1.0.0) | The Home Floor Directory for a whole-home lifestyle retailer (the `nordic` brief below) — the shop as a department store's escalator-hall directory: a solid enamel board in the merchant primary carries the store name and one row per department (big tabular floor number, name in tracked expanded caps, the item count where the catalogue reports one, and a wayfinding mark closing every row at the measure), beside the merchant's slider in a hairline window with a ruled caption plate; product groups are numbered plates, terrazzo landings pace the scroll, the same floor number reappears on the department plate and as a product's floor tag, every figure sits in one tabular slot and rolls in place when it changes (the one motion moment), state prints as a word plus a figure and never as a tint, the product page prints the merchant's own measurements as a numbered plate key on dotted brass leaders (and says so plainly when there are none), and a drawn room plan stands in when the merchant supplied no picture; Archivo (wdth axis) + Golos Text, Tajawal leads Arabic; light and roomy on purpose, never dark; selected by the `FURNITURE` enum value; `themes/furniture/DESIGN.md` |
| `basic` | built (v1.0.0) | The Catalogue Page — the platform's multi-purpose default: one continuous ruled catalogue of entries (photo, name, catalogue number, big condensed price in fixed slots; cells share 1px rules, nothing floats), a thumb-index strip of categories under the masthead, the merchant primary as flat fields only (cover title block, active tab, the one action per view), Sofia Sans + Sofia Sans Extra Condensed (Cairo in Arabic), state as printed stamps, the price flash on add as the only motion; selected by the `BASIC` enum value; not the fallback, which stays `starter`; `themes/basic/DESIGN.md` |

## How to read the catalog

Each entry below is a **brief**: target merchant, structural thesis per surface, colour strategy, density,
and what makes it not a recolour. It is **not** a visual world. Fonts, materials, palette rendition and the
composition of the first viewport are decided when the theme is generated, by impeccable's `new-work` flow
and its direction roll (`concept-seed.mjs --scope direction`) — a catalog entry never skips the roll, and
it must not be read as "luxury = cream paper + serif", "tech = black + neon", "fashion = broadsheet
hairlines": those are the category defaults the roll exists to avoid.

Rules every theme shares: behaviour from `@store-front/hooks`; primitives from `@store-front/ui`; every
state rendered; RTL + mobile first-class; merchant colour roles respected (the theme may re-map them through
`tokens.mapMerchantColors`, e.g. demote primary to an accent, but never paint over the preset); the theme's
default palette is the palette of its chosen visual world, seeded in the generator (never hand-written hex) so
it passes the same contrast rules as the presets.

**Fonts are a shared cost, not a private one.** Next collects every theme's `next/font` CSS into the *same*
layout entry, so all twelve themes' `@font-face` rules are `<link>`ed on every storefront whatever theme is
active — a face declared in `themes/<id>/src/fonts.ts` is bytes every other theme's merchants pay for. Hence
two rules in `fonts.ts`: `preload: false` on every face (a preload would fire on all twelve storefronts), and
prefer a family Google serves as named subsets. `subsets: [...]` does **not** shrink the CSS — it only picks
what would be preloaded — so a family with CJK coverage ships ~120 numbered `unicode-range` slices per weight
whatever you ask for. `storefront/scripts/prune-font-subsets.mjs` strips the CJK-only slices after every build
as a backstop, but a Latin-native family costs nothing to begin with.

Every page in the contract is required except **Search**. A theme without `pages/Search.tsx` gets the shell's
fallback results page, which is built from tokens and so still wears the theme's type, colour and spacing;
`basic` is the reference implementation to copy when a theme wants a designed one. What is *not* optional is
the header box: it submits to `/search`, and it takes its provider from `useSearchProvider(capabilities)` so
it never claims a search the deployment cannot answer.

## Directions

| id | Target merchant · replaces enum values | Structural thesis (header · hero · card · PDP · listing · cart) | Colour strategy · density | Not a recolour because |
|---|---|---|---|---|
| `atelier` | premium / luxury — jewellery, watches, perfumery · `JEWELERY`, `WATCHES` | two-row header (utility row + centred wordmark + thin nav) · one full-bleed hero with a caption, **no carousel** · 2-col large image cards, no borders, name/price small below · gallery-led PDP (vertical thumbnails or scroll), sticky buy box, materials accordion · 2–3-col listing, "refine" as a top sheet · cart as a full page review | Restrained: merchant primary used as ink/accent only, surfaces from the preset's background · low | composition, scale and restraint carry the identity; remove colour and it is still recognisable |
| `market` | high-density marketplace — electronics, tools, parts, many SKUs · `ELECTRONICS`, `TOOLS` | compact header with a dominant search slot + category mega-menu + utility links · hero = promo-tile grid · 4–6-col cards with a spec line, rating slot, price-forward, quick add · spec-table-first PDP, sticky price/add rail, tabs · left filter rail, sticky sort bar, result counts, pagination · drawer with line totals | Committed: primary on CTAs, secondary on links, info badges · high | information architecture and grid density *are* the identity |
| `nordic` (built as `furniture`) | minimal home / lifestyle — furniture, homeware · `FURNITURE` | airy header, wide gutters · split image/text hero, one CTA · 4/5 image-first cards, no border, tonal hover · 60/40 PDP with long-form story sections (materials, dimensions) · 3-col listing, filters as chips · minimal drawer | Tonal: surfaces derived from the preset's background/foreground, primary muted · low–medium | tonal surfaces and whitespace rhythm, not chrome |
| `editorial` | bold fashion — apparel, streetwear · `FASHION` (TS-only value today) | oversized wordmark, uppercase text row nav · asymmetric hero, display type over image · lookbook grid, mixed aspect ratios, hover second image · vertical image scroll + sticky buy column · dense 4-col listing, filters in a drawer · side panel cart | Monochrome base, accent only on CTA/sale · medium | typography-as-layout and asymmetric grids |
| `fresh` (built as `grocery`) | food, grocery, beauty consumables, baby · `FOOD`, `BABY`, `BEAUTY`, `COSMETICS` | rounded header + category tile strip · short offer-card carousel · quick-add stepper cards, unit/price-per-unit slot · compact PDP, benefits/ingredients accordion, sticky bottom bar on mobile · horizontal category chips + grid · drawer with a progress slot | Full palette, saturated, large radius tokens · medium–high | rounded system, stepper-first cards, bottom sticky bar |
| `showroom` | technology / electronics brand store, sports, eyewear · `SPORTS`, `GLASSES` | dark-capable header, feature stripes · spotlight product hero · 3-col cards with feature bullets · spec-driven PDP, comparison table, sticky gallery · facet chips · drawer | Drenched / the bridge's dark path · medium | dark scheme + spec-driven PDP |
| `bazaar` | clean modern general retail — the default for `BASIS`, `MODERN`, `DEFAULT`; becomes the fallback theme once built | standard header with search, nav, account, cart · slider hero with headline + CTA (uses `sliderImages`) · 3–4-col cards with sale badge · classic 2-col PDP · filter rail + sort, mobile filter drawer · drawer | merchant palette as given · medium | the "safe default" — still structurally composed, never a Card+gradient reskin |

## Legacy mapping once themes exist

`storefront/src/shell/theme/legacy-theme-map.ts` maps every `Theme` enum value (lowercased) to a theme id.
A value whose own name is a shipped theme resolves straight through the registry; the map carries the rest.
The seven built themes above are the ones the Java enum marks `implemented`, and they are the whole of what a
merchant may select. `COSMETICS`, `GLASSES`, `JEWELLERY` and `SPORTS` have packages but are still
`new-theme` scaffolds, so they resolve and preview without being offered. Everything else — `DEFAULT`,
`BASIS`, `MODERN`, `JEWELERY`, `ELECTRONICS`, `FOOD`, `WATCHES`, `BABY`, `TOOLS` — lands on `starter`; those
stay declared so stores already set to them keep loading. As the remaining directions ship:
`basis/modern/default → bazaar`, `jewelery/watches → atelier`, `electronics/tools → market`. Set
`STOREFRONT_FALLBACK_THEME=bazaar` when it exists.

Making a theme merchant-selectable is three edits, all needed: the Java `Theme` enum value with
`implemented=true` (`store-commons/commons/.../domain/Theme.java`), that value in the `merchant_store.theme`
check constraint (`merchant-service/src/main/resources/init-sql/schema.sql`), and a `legacy-theme-map.ts`
entry if the enum name is not the theme id. Do the first only once the theme is actually designed — a
scaffold in the picker is a store a merchant can ship undesigned.

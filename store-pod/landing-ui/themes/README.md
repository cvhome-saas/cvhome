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
| `starter` | reference | deliberately undesigned; the copy source for `npm run new-theme <id>`; current fallback for every legacy enum value |
| `beauty` | built (v1.0.0) | Industrial Quote Grammar for a beauty + fashion boutique — ink plates on a 1px grid, 45° hazard stripes, straight-quoted labels, condensed caps display (Oswald) + mono facts (JetBrains Mono), the merchant primary as the single zip-tie-tag accent; `themes/beauty/DESIGN.md` |
| `fashion` | built (v1.0.0) | The Wheatpaste Wall for streetwear / drops — a rendered wall under pasted paper posters (≤1.2° tilts, offset shadows, a peeling corner on the big ones), the merchant primary as day-glo paper on every primary action, state as rubber stamps (SALE / SOLD OUT / ONLY N LEFT / ADDED), Anton + Changa poster caps, Rubik body; typographic posters when the merchant gave no picture; `themes/fashion/DESIGN.md` |
| `grocery` | built (v1.0.0) | Cash & Carry for food / grocery consumables (the `fresh` brief below) — the shop as a warehouse floor: concrete ground, the merchant primary as safety-yellow price boards (hero board, active aisle tile, every action), crate cells with a 2px hardware line and stepper-first quick-add, an aisle-board category strip, states printed as stickers, the ADDED stamp as the one motion moment, and an honest 12-segment load meter in the basket drawer; Manrope + Fira Sans Extra Condensed (Almarai leads Arabic); not yet wired to any enum value (user's call); `themes/grocery/DESIGN.md` |
| `basic` | built (v1.0.0) | The Catalogue Page — the platform's multi-purpose default: one continuous ruled catalogue of entries (photo, name, catalogue number, big condensed price in fixed slots; cells share 1px rules, nothing floats), a thumb-index strip of categories under the masthead, the merchant primary as flat fields only (cover title block, active tab, the one action per view), Sofia Sans + Sofia Sans Extra Condensed (Cairo in Arabic), state as printed stamps, the price flash on add as the only motion; not yet wired to any legacy enum value or set as the fallback (user's call); `themes/basic/DESIGN.md` |

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

## Directions

| id | Target merchant · replaces enum values | Structural thesis (header · hero · card · PDP · listing · cart) | Colour strategy · density | Not a recolour because |
|---|---|---|---|---|
| `atelier` | premium / luxury — jewellery, watches, perfumery · `JEWELERY`, `WATCHES` | two-row header (utility row + centred wordmark + thin nav) · one full-bleed hero with a caption, **no carousel** · 2-col large image cards, no borders, name/price small below · gallery-led PDP (vertical thumbnails or scroll), sticky buy box, materials accordion · 2–3-col listing, "refine" as a top sheet · cart as a full page review | Restrained: merchant primary used as ink/accent only, surfaces from the preset's background · low | composition, scale and restraint carry the identity; remove colour and it is still recognisable |
| `market` | high-density marketplace — electronics, tools, parts, many SKUs · `ELECTRONICS`, `TOOLS` | compact header with a dominant search slot + category mega-menu + utility links · hero = promo-tile grid · 4–6-col cards with a spec line, rating slot, price-forward, quick add · spec-table-first PDP, sticky price/add rail, tabs · left filter rail, sticky sort bar, result counts, pagination · drawer with line totals | Committed: primary on CTAs, secondary on links, info badges · high | information architecture and grid density *are* the identity |
| `nordic` | minimal home / lifestyle — furniture, homeware · `FURNITURE` | airy header, wide gutters · split image/text hero, one CTA · 4/5 image-first cards, no border, tonal hover · 60/40 PDP with long-form story sections (materials, dimensions) · 3-col listing, filters as chips · minimal drawer | Tonal: surfaces derived from the preset's background/foreground, primary muted · low–medium | tonal surfaces and whitespace rhythm, not chrome |
| `editorial` | bold fashion — apparel, streetwear · `FASHION` (TS-only value today) | oversized wordmark, uppercase text row nav · asymmetric hero, display type over image · lookbook grid, mixed aspect ratios, hover second image · vertical image scroll + sticky buy column · dense 4-col listing, filters in a drawer · side panel cart | Monochrome base, accent only on CTA/sale · medium | typography-as-layout and asymmetric grids |
| `fresh` (built as `grocery`) | food, grocery, beauty consumables, baby · `FOOD`, `BABY`, `BEAUTY`, `COSMETICS` | rounded header + category tile strip · short offer-card carousel · quick-add stepper cards, unit/price-per-unit slot · compact PDP, benefits/ingredients accordion, sticky bottom bar on mobile · horizontal category chips + grid · drawer with a progress slot | Full palette, saturated, large radius tokens · medium–high | rounded system, stepper-first cards, bottom sticky bar |
| `showroom` | technology / electronics brand store, sports, eyewear · `SPORTS`, `GLASSES` | dark-capable header, feature stripes · spotlight product hero · 3-col cards with feature bullets · spec-driven PDP, comparison table, sticky gallery · facet chips · drawer | Drenched / the bridge's dark path · medium | dark scheme + spec-driven PDP |
| `bazaar` | clean modern general retail — the default for `BASIS`, `MODERN`, `DEFAULT`; becomes the fallback theme once built | standard header with search, nav, account, cart · slider hero with headline + CTA (uses `sliderImages`) · 3–4-col cards with sale badge · classic 2-col PDP · filter rail + sort, mobile filter drawer · drawer | merchant palette as given · medium | the "safe default" — still structurally composed, never a Card+gradient reskin |

## Legacy mapping once themes exist

`storefront/src/shell/theme/legacy-theme-map.ts` maps every `Theme` enum value (lowercased) to a theme id.
Today everything → `starter`. As themes ship: `basis/modern/default → bazaar`, `jewelery/watches → atelier`,
`beauty/cosmetics/food/baby → fresh`, `fashion → editorial`, `furniture → nordic`,
`electronics/tools → market`, `sports/glasses → showroom`. Set `STOREFRONT_FALLBACK_THEME=bazaar` when it
exists. Making a theme merchant-selectable needs the Java `Theme` enum value with `implemented=true` (backend).

# Storefront themes — direction catalog

A theme is a package under `themes/<id>/` implementing the `ThemeDefinition` contract (`libs/theme`). One
storefront app serves all of them; the merchant's `Theme` enum picks one, the merchant's `ColorTheme` preset
colours it. How to build one: `.agents/skills/project-structure/references/new-landing-ui-template.md`.

## Shipped

| id | status | notes |
|---|---|---|
| `starter` | reference | deliberately undesigned; the copy source for `npm run new-theme <id>`; current fallback for every legacy enum value |
| `beauty` | built (v1.0.0) | Industrial Quote Grammar for a beauty + fashion boutique — ink plates on a 1px grid, 45° hazard stripes, straight-quoted labels, condensed caps display (Oswald) + mono facts (JetBrains Mono), the merchant primary as the single zip-tie-tag accent; `themes/beauty/DESIGN.md` |

## How to read the catalog

Each entry below is a **brief**: target merchant, structural thesis per surface, colour strategy, density,
and what makes it not a recolour. It is **not** a visual world. Fonts, materials, palette rendition and the
composition of the first viewport are decided when the theme is generated, by impeccable's `new-work` flow
and its direction roll (`concept-seed.mjs --scope direction`) — a catalog entry never skips the roll, and
it must not be read as "luxury = cream paper + serif", "tech = black + neon", "fashion = broadsheet
hairlines": those are the category defaults the roll exists to avoid.

Rules every theme shares: behaviour from `@store-front/hooks`; primitives from `@store-front/ui`; every
state rendered; RTL + mobile first-class; merchant colour roles respected (the theme may re-map them through
`tokens.mapMerchantColors`, e.g. demote primary to an accent, but never paint over the preset).

## Directions

| id | Target merchant · replaces enum values | Structural thesis (header · hero · card · PDP · listing · cart) | Colour strategy · density | Not a recolour because |
|---|---|---|---|---|
| `atelier` | premium / luxury — jewellery, watches, perfumery · `JEWELERY`, `WATCHES` | two-row header (utility row + centred wordmark + thin nav) · one full-bleed hero with a caption, **no carousel** · 2-col large image cards, no borders, name/price small below · gallery-led PDP (vertical thumbnails or scroll), sticky buy box, materials accordion · 2–3-col listing, "refine" as a top sheet · cart as a full page review | Restrained: merchant primary used as ink/accent only, surfaces from the preset's background · low | composition, scale and restraint carry the identity; remove colour and it is still recognisable |
| `market` | high-density marketplace — electronics, tools, parts, many SKUs · `ELECTRONICS`, `TOOLS` | compact header with a dominant search slot + category mega-menu + utility links · hero = promo-tile grid · 4–6-col cards with a spec line, rating slot, price-forward, quick add · spec-table-first PDP, sticky price/add rail, tabs · left filter rail, sticky sort bar, result counts, pagination · drawer with line totals | Committed: primary on CTAs, secondary on links, info badges · high | information architecture and grid density *are* the identity |
| `nordic` | minimal home / lifestyle — furniture, homeware · `FURNITURE` | airy header, wide gutters · split image/text hero, one CTA · 4/5 image-first cards, no border, tonal hover · 60/40 PDP with long-form story sections (materials, dimensions) · 3-col listing, filters as chips · minimal drawer | Tonal: surfaces derived from the preset's background/foreground, primary muted · low–medium | tonal surfaces and whitespace rhythm, not chrome |
| `editorial` | bold fashion — apparel, streetwear · `FASHION` (TS-only value today) | oversized wordmark, uppercase text row nav · asymmetric hero, display type over image · lookbook grid, mixed aspect ratios, hover second image · vertical image scroll + sticky buy column · dense 4-col listing, filters in a drawer · side panel cart | Monochrome base, accent only on CTA/sale · medium | typography-as-layout and asymmetric grids |
| `fresh` | food, grocery, beauty consumables, baby · `FOOD`, `BABY`, `BEAUTY`, `COSMETICS` | rounded header + category tile strip · short offer-card carousel · quick-add stepper cards, unit/price-per-unit slot · compact PDP, benefits/ingredients accordion, sticky bottom bar on mobile · horizontal category chips + grid · drawer with a progress slot | Full palette, saturated, large radius tokens · medium–high | rounded system, stepper-first cards, bottom sticky bar |
| `showroom` | technology / electronics brand store, sports, eyewear · `SPORTS`, `GLASSES` | dark-capable header, feature stripes · spotlight product hero · 3-col cards with feature bullets · spec-driven PDP, comparison table, sticky gallery · facet chips · drawer | Drenched / the bridge's dark path · medium | dark scheme + spec-driven PDP |
| `bazaar` | clean modern general retail — the default for `BASIS`, `MODERN`, `DEFAULT`; becomes the fallback theme once built | standard header with search, nav, account, cart · slider hero with headline + CTA (uses `sliderImages`) · 3–4-col cards with sale badge · classic 2-col PDP · filter rail + sort, mobile filter drawer · drawer | merchant palette as given · medium | the "safe default" — still structurally composed, never a Card+gradient reskin |

## Legacy mapping once themes exist

`storefront/src/shell/theme/legacy-theme-map.ts` maps every `Theme` enum value (lowercased) to a theme id.
Today everything → `starter`. As themes ship: `basis/modern/default → bazaar`, `jewelery/watches → atelier`,
`beauty/cosmetics/food/baby → fresh`, `fashion → editorial`, `furniture → nordic`,
`electronics/tools → market`, `sports/glasses → showroom`. Set `STOREFRONT_FALLBACK_THEME=bazaar` when it
exists. Making a theme merchant-selectable needs the Java `Theme` enum value with `implemented=true` (backend).

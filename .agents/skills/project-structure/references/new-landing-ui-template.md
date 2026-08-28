# New Storefront Theme Guide

How to add a storefront theme to `store-pod/landing-ui`. A theme is **a package, not an app**: one Next.js
app (`storefront/`) owns routes, data loading, i18n, auth and theme resolution; a theme under
`themes/<id>/` supplies design tokens, fonts, the page layout and the page compositions that satisfy the
`ThemeDefinition` contract in `libs/theme`.

> The previous generation (one full Next app per theme under `templates/`) is deprecated and parked in
> `templates-deprecated/`. Do not copy from it.

---

## 1. Architecture you are plugging into

```
storefront/                 THE Next.js app (shell) — never edited for a theme
  src/proxy.ts                Store-Id gate, / → /{lang}, next-intl routing, ?theme= / ?color= dev overrides
  src/app/(storefront)/[locale]/…   routes: loaders + metadata only; each renders theme.pages.X
  src/app/globals.css         the ONLY Tailwind entry + @theme inline token mapping
  src/app/themes.css          GENERATED: @source + tokens.css import per theme
  src/shell/theme/registry.ts GENERATED entries: static map of dynamic imports, one per theme
  src/shell/theme/legacy-theme-map.ts   Theme enum value → theme id (fallback for old values)
  src/shell/tokens/merchant-tokens.ts   ColorTheme (DEFAULT → theme's own palette, else preset) → colour-role tokens (inline style on <html>)
libs/theme                  @store-front/theme — ThemeDefinition, token schema, colour bridge, defineTheme()
libs/ui                     @store-front/ui — shadcn primitives shared ONCE + Skeleton/EmptyState/ErrorState/
                            Price/QuantityStepper/Drawer/AspectBox. Themes never fork these.
libs/i18n                   @store-front/i18n — routing, Link/useRouter, direction, useDir()
libs/{types,services,hooks} domain, API clients, behaviour hooks (useCart, useProductListing, useProductPurchase, …)
themes/<id>/                YOUR theme (package @store-front/theme-<id>)
locales/{en,ar,es,fr,ru}.json  SHARED translations — add keys to all five
```

Request flow: spg/Caddy injects `Store-Id, Theme, Color-Theme, Default-Language, Supported-Languages` →
`proxy.ts` → `getTheme()` (cookie override → `theme` header → `STOREFRONT_THEME` → legacy map → fallback)
→ registry dynamic import → `getColorThemeRequest()` (cookie override → `Color-Theme` header → store record) →
`resolveColorScheme()` (a fixed preset wins; `DEFAULT` / unset / unknown → `theme.tokens.defaultColors`) → root
layout sets `<html data-theme=<id> data-color-scheme data-color-theme=<DEFAULT|PRESET> style="--primary:…"
class="<font vars>">` → `theme.layout.Root` → page → `theme.pages.X`.

**Who owns what**

| Concern | Owner |
|---|---|
| Routes, data fetching, `notFound()`/errors, metadata, Suspense | shell |
| Colour roles (`--primary`, `--primary-foreground`, `--muted`, `--sale`, …) | the theme's default palette (`src/colors.ts`, generated from `THEME_DEFAULTS` in `libs/types/scripts/build-color-schemas.mjs`, wired as `tokens.defaultColors`) or the merchant's preset, via the bridge; theme may re-map with `tokens.mapMerchantColors` |
| Fonts, type scale, spacing/density, radius, shadows, containers, motion, header height, product aspect | **theme** (`tokens.css`) |
| Header/nav/footer/cart drawer/mobile nav structure | **theme** (`layout/`) |
| Page composition for Home, Category, Product, Content, Checkout, CheckoutResult, Customer, Order | **theme** (`pages/`) |
| Skeletons, error, not-found, empty, redirecting states | **theme** (`states/`) |
| Behaviour (cart, listing sort/pagination/facets, variants, checkout form, auth) | `libs/hooks` — reuse, never re-implement |
| Primitives (Button, Dialog, Select, …) | `libs/ui` — restyle through tokens/variants/className, or compose a wrapper |

---

## 2. Step 0 — scaffold

```bash
cd store-pod/landing-ui
npm run new-theme <id>          # kebab-case, e.g. atelier
```

The script copies `themes/starter` → `themes/<id>`, renames ids/selectors, and registers the theme in
`registry.ts`, `themes.css`, `next.config.ts` (`transpilePackages`), `legacy-theme-map.ts`,
`storefront/package.json` and the TS `Theme` enum, then runs `npm install`. Commit that as one change.

Run it with the local stack (`lcl start -d`) and open
`http://org1-store1.spg-507f1f77.gateway.com/en?theme=<id>` — spg injects the store headers; `?theme=` is a dev-only
override cookie (`STOREFRONT_THEME=<id>` also works), `?color=<PRESET|default>` likewise previews a colour theme. `http://localhost:8110` renders SSR via the `FALLBACK_STORE_ID`
fallback but browser-side API calls need spg (see `landing-ui.md`, "Local dev URLs").

---

## 3. Step 1 — design it with impeccable (mandatory for a real theme)

The storefront ships `store-pod/landing-ui/PRODUCT.md` (impeccable product truth) and each theme owns
`themes/<id>/DESIGN.md`. `context.mjs` resolves PRODUCT.md from the landing-ui root and DESIGN.md from the
theme folder when targeted.

1. `node ~/.claude/skills/impeccable/scripts/context.mjs --target themes/<id>` (cwd `store-pod/landing-ui`).
2. Pick the brief from `themes/README.md` (target merchant, structural thesis, colour strategy, density)
   — or write a new one. **The catalog entry is the brief, not the visual world.**
3. `new-work` flow: name the audience's world, list candidates, run
   `concept-seed.mjs --scope direction` — **the roll is mandatory**, a catalog entry never skips it —
   present the direction, commit the contract comment in `themes/<id>/src/layout/Root.tsx`.
4. Build page-by-page in the order a shopper travels: Home → Category → Product → Cart drawer → Checkout →
   CheckoutResult → Customer/Order → Content → states. Keep `starter`'s *behaviour* (hooks, states, RTL,
   a11y); replace its *look and structure* completely — `starter` is evidence of the contract, not a style.
5. Finish: batched desktop + mobile + `/ar/` screenshots, the finish reviewer, fix batch, verdict, then the
   documenter writes `themes/<id>/DESIGN.md` from the built theme.

---

## 4. Step 2 — what to edit (and what never)

```
themes/<id>/
├── package.json            name @store-front/theme-<id>; exports ".", "./tokens.css"
├── DESIGN.md               written at finish by the documenter
└── src/
    ├── index.ts            defineTheme({id, name, version, fonts, tokens, layout, pages, states, loginCss?})
    ├── fonts.ts            next/font (google or local) → fonts.variables; tokens.css maps --font-body onto it
    ├── tokens.css          [data-theme="<id>"] { every THEME_OWNED_TOKENS entry }  ← the theme's voice
    ├── config.ts           ThemeLayoutConfig (cart drawer/page, mobile nav kind, grid, aspect, container, search)
    ├── layout/             Root, Header, Nav, MobileNav, HeaderActions, CartDrawer, Announcement, Footer
    ├── pages/              Home, Category, Product, Content, Checkout, CheckoutResult, Customer, Order
    │                       (+ Search — the only optional page; without it the shell renders a fallback)
    ├── sections/           Hero, ProductRail, Listing, BuyBox, Gallery, SearchBox, SearchResults, CheckoutForm, …
    ├── components/         ProductCard, ProductGrid, ProductBadges, CartLineItem, Breadcrumbs, PageShell, …
    └── states/             ErrorState*, NotFound, EmptyState*, Redirecting*, skeletons/*   (* = 'use client')
```

Never edit for a theme: `storefront/**` (except what the scaffold generated), `libs/**`, `locales/*` other
than adding keys. A primitive that cannot be themed gets a new variant in `libs/ui` (benefits all themes).

### Tokens (`tokens.css`) → utilities (`globals.css` `@theme inline`)

| Token (theme sets) | Utility you get |
|---|---|
| `--font-body / --font-heading / --font-code` | `font-sans / font-display / font-mono` |
| `--type-xs…--type-6xl`, `--line-*`, `--track-*` | `text-xs…text-6xl`, `leading-*`, `tracking-*` |
| `--space-unit`, `--density`, `--section-y`, `--gutter`, `--header-h(-lg)` | spacing scale, `py-section`, `px-gutter`, `h-header`, `top-header` |
| `--r-control/card/image/badge/overlay` | `rounded-control/card/image/badge/overlay` (+ `rounded-md` = control, `rounded-lg` = card) |
| `--elev-sm/md/lg/overlay` | `shadow-sm/md/lg/overlay` |
| `--width-narrow/content/wide` | `max-w-narrow/content/wide` |
| `--motion-fast/base/slow`, `--easing-standard/emphasized` | `duration-(--motion-base)`, `ease-standard/emphasized` |
| `--product-aspect` | `aspect-product` (and `AspectBox` default) |
| merchant colour roles (bridge) | `bg-primary text-primary-foreground bg-muted text-muted-foreground bg-sale border-border ring-ring …` |

The stock Tailwind palette is removed: `bg-blue-500` does not compile. Colour is role-based or it does not
exist. `white` / `black` / `transparent` remain.

---

## 5. Step 3 — contract checklist (before you call it done)

### The Search page (the one optional page)

`ThemePages.Search` is optional, and `defineTheme()` does not require it. A theme without one gets
`storefront/src/shell/theme/default-search-page.tsx` — built only from design tokens, so it inherits the
theme's type, colour and spacing rather than looking like a different shop. That is why search worked
everywhere the day the catalog endpoint landed, instead of waiting for twelve bespoke pages.

To give a theme a designed one, copy the three files from `basic`, which is the reference implementation:

```
src/pages/Search.tsx                       heading, count, the did-you-mean line, then the results section
src/sections/SearchResults.tsx             'use client' — useProductSearch(): facets, chips, sort, grid, paging
src/states/skeletons/SearchSkeleton.tsx    optional; the shell falls back to the category skeleton
```

then register both in `src/index.ts`:

```ts
pages:  {Home, Category, Search, Product, …}
states: {PageSkeleton: {…, category: CategorySkeleton, search: SearchSkeleton}}
```

Behaviour comes from `useProductSearch` (paging, sorting, facet toggling, URL sync, retry) — a sibling of
`useProductListing`, not a replacement for it: the category page hits an endpoint that has no search term and
no multi-select facets. Never re-implement either.

What the catalog can filter on is category, brand and product type. **Price and stock are not filterable or
sortable**: they live in the inventory service keyed by sku, and are merged into results after paging. A rail
that offered a price filter would be promising something the platform cannot do.

`ThemeLayoutConfig.search` picks the header treatment: `header` (inline box), `overlay`, `page` (the box is a
link straight to `/search`) or `hidden`.

- [ ] `defineTheme()` accepts it (`npm run build` passes — it throws on a missing page/state)
- [ ] Every `THEME_OWNED_TOKENS` entry set in `tokens.css`; no hard-coded colours anywhere
- [ ] Fonts load (`<html class>` carries the next/font variable; CSS has the `@font-face`)
- [ ] Every page and every state renders: home, category (sort/page/filter, empty, error), product (variants,
      out of stock, sale), cart drawer (empty → add → qty → remove), checkout, success/cancel, customer
      (login redirect), order, content, 404 product/category/page, thrown error, store-not-found
- [ ] Search: the header box submits to `/search`, its dropdown shows product hits and a "see all" row, and
      `/search?q=…` renders — the theme's own `pages/Search.tsx` if it has one, otherwise the shell fallback.
      The box must be driven by `useSearchProvider(capabilities)`, never by a provider the theme picks itself
- [ ] RTL: `/ar/` — logical utilities only (`ps/pe/ms/me/start/end/text-start/text-end`), Swiper gets `dir`,
      drawers use `DrawerContent side="start|end"`, icons that imply direction get `rtl:rotate-180`
- [ ] Mobile nav, tablet and desktop verified; header hamburger; grids adapt per `config.productGrid`
- [ ] No literal UI text — every string through `t()`; new keys added to **all 5** locale files
- [ ] No `@/` imports in the theme (ESLint enforces); only `@store-front/*` + relative
- [ ] shadcn rules: `Dialog/Sheet` have a title, `SelectItem` inside `SelectGroup`, icons in buttons use
      `data-icon`, `gap-*` not `space-*`, `size-*` for squares, `Empty/Error/Skeleton` primitives, `Badge` not spans
- [ ] Accessibility: icon-only buttons labelled, `aria-live` on async regions, focus visible, skip link kept
- [ ] `ProductCard`: placeholder image, sale + out-of-stock badges, 2-line clamp, price via `Price`
- [ ] `loginCss` (optional) if the theme wants the cua auth pages to match

---

## 6. Step 4 — verify end-to-end

```bash
cd store-pod/landing-ui
npm run lint && npm run typecheck && npm run build     # contract, lint (incl. RTL warnings), Tailwind
npm test --workspace=libs/theme                         # colour bridge (30 presets + theme defaults × AA)
npm run gen:colors --workspace=libs/types               # after editing a THEME_DEFAULTS seed (regenerates src/colors.ts)
npm run dev   # then http://localhost:8110/en?theme=<id>&color=default, /ar?theme=<id>, ?color=MIDNIGHT

# full stack: spg injects the headers for the demo store
lcl start -d
open http://org1-store1.spg-507f1f77.gateway.com/en?theme=<id>
```
Browser QA at desktop, tablet and mobile widths, first in the theme's own palette (`?color=default`, what a
merchant on `DEFAULT` sees), then `?color=MIDNIGHT` (dark preset) and one light preset — or `Color-Theme: …`
through spg / the store's colour theme in the seller console.

---

## 7. Step 5 — making it selectable by merchants

Backend work (out of this repo's storefront scope): add the enum value to the Java `Theme` enum
(`store-commons/commons/.../Theme.java`) with `implemented = true`. Until then, point existing enum values
at the new theme in `storefront/src/shell/theme/legacy-theme-map.ts` (e.g. `jewelery: 'atelier'`). The
TS `Theme` enum is kept in step by the scaffold script.

---

## Key imports

```ts
import {defineTheme, type ThemeDefinition, type PageProps, type HomeData} from '@store-front/theme';
import {Button, Badge, Price, QuantityStepper, Drawer, DrawerContent, Skeleton, EmptyState, ErrorState} from '@store-front/ui';  // or deep: '@store-front/ui/button'
import {Link, useRouter, usePathname} from '@store-front/i18n/navigation';
import {useDir} from '@store-front/i18n/use-dir';
import {useCart, useUser, useCustomer, useCheckoutForm, useOrderStatus, useProductListing, useProductPurchase,
        useProductSearch, useSearch, useSearchProvider} from '@store-front/hooks';
import {isOnSale, isOutOfStock, discountPercent, primaryImage, productHref} from '@store-front/services/product-presenter';
import type {Product, Category, Store, StoreContext} from '@store-front/types';
```

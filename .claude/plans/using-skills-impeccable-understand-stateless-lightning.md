# Storefront Theme System — Architecture Redesign Plan

## Context

`.agents/requirments/storefront-themes.md` asks for a complete rethink of the storefront theme system in
`store-pod/landing-ui`: the four existing templates look like one template with three recolors, themes can
only control colors, and adding a theme means copying a 78-file Next.js app. The requirement explicitly
says: analyze first, propose an architecture, define shared vs theme-specific, propose distinct theme
directions, explain migration, then implement theme-by-theme.

Decisions taken with the user (this session):

| Question | Decision |
|---|---|
| Scope of this plan | **Architecture only** — how any new theme is built and what it must follow. No designed theme now; the user will ask "generate theme X" later. |
| Proof of architecture | One deliberately plain **`starter`** scaffold theme (unstyled reference implementing the whole contract) that runs end-to-end and is the copy source for future themes. |
| Runtime model | **Single Next.js app + theme packages** (`themes/<name>/`), theme resolved per request from the `theme` header. Express `app/` retired. |
| Old templates | **Not refactored.** Kept as deprecated, excluded from the workspace/build; new themes replace them. Theme enum values stay; unknown/legacy values fall back. |
| Backend / seller UI | **Untouched.** Merchant still chooses `Theme` + `ColorTheme` (30 presets × 29 colors). Theme author decides fonts/radius/density; theme maps preset colors into its token system with derived tokens + contrast guards. |
| Design process per theme | Every future theme runs the **impeccable** flow (PRODUCT.md → new-work direction roll → build → finish review → `DESIGN.md`). The shadcn skill rules govern component composition. |

---

## Phase 1 — Analysis of the current system (findings)

Measured on `store-pod/landing-ui` (Next 16.0.0, React 19.2, Tailwind **v4**, shadcn new-york/radix, next-intl 4).

### 1.1 Architecture problems

| # | Finding | Evidence |
|---|---|---|
| A1 | **68 % of every template is a byte-identical copy.** 53 of 78 files identical across all 4 (all 15 route files incl. `layout.tsx`/`page.tsx`, all 21 shadcn primitives, `i18n/*`, `lib/utils.ts`, `proxy.ts`, all config). Only `src/shared/**` (~22 files), `globals.css`, `public/css/login.css` differ. | md5 across `templates/*` |
| A2 | **N full Next.js apps multiplexed by Express.** `app/src/template-manager.ts` boots one Next instance per theme, uses process-global `process.chdir`, and passes the unvalidated `theme` header into `path.resolve(.. '../templates/' + name)`. Memory = N × Next. | `app/src/server.ts:20-54`, `template-manager.ts:34-66` |
| A3 | **`globals.css` is 786 lines, ~650 of them dead.** Hand-written `.bg-primary{@apply bg-[var(--primary)]}` shims duplicate what Tailwind v4 `@theme inline` already generates; `--primary` is defined three times (shadcn oklch `:root`, merchant hex via inline `<style>`, `@theme inline` alias). Works by cascade accident. | `templates/basis/src/app/[locale]/globals.css:11-663, 671-776` |
| A4 | **Merchant colors have no `*-foreground` pairs.** `ColorSchema` (29 keys) overrides shadcn `--primary` but never `--primary-foreground`, `--card`, `--muted`, `--input`, `--popover` → primitives get merchant backgrounds with stock foregrounds; no contrast guarantee. `--neutral` is referenced but never defined. | `libs/types/src/color-schema.ts`, `basis/.../Footer.tsx:18-68` |
| A5 | **Two of five routing headers are ignored.** spg/Caddy `domain_lookup` injects `Store-Id, Theme, Color-Theme, Default-Language, Supported-Languages`; the storefront reads only the first three via Express and fetches colors/languages again through `GET merchant/api/v1/store`. | `MerchantRoutingService.mapHeaders`, `layout.tsx:68` |
| A6 | **Layout does 5 serial round-trips** (store → categories → contents → header box, plus 2 boxes in `generateMetadata`), renders `null` on missing store (blank 200). Pages mutate the `params` prop. | `layout.tsx:38-66`, `page.tsx:15` |
| A7 | **Mixed module resolution.** `@/services/*` and `@store-front/services/*` both alias to `../../libs/services/src/*` while `@store-front/hooks` resolves through built `dist`; `components.json` aliases `@/hooks` which does not exist. | `templates/basis/tsconfig.json`, `components.json` |
| A8 | **Enum drift.** TS `Theme` has 15 values (adds `DEFAULT`, `FASHION`), Java has 13 with an `implemented` flag; nothing in landing-ui guards an unimplemented theme name. | `libs/types/src/store.ts:53-69` vs `Theme.java` |
| A9 | **Docker/build fragility.** Dockerfile hand-pins deps, omits `locales/` and template deps; `build:templates` targets a directory not a workspace; `next/react` live in template devDependencies while `npm prune --production` runs. | `Dockerfile`, root `package.json` |

### 1.2 Why the themes look the same / generic

| # | Finding | Evidence |
|---|---|---|
| D1 | `modern` is **byte-identical** to `basis` in `globals.css` and all of `src/app`; it is a className reskin of ~11 files. `beauty`'s `Header.tsx` differs from basis by 27 of 929 lines. Only `jewelery` restructures anything (two-row header, `rounded-none` system). | diff counts |
| D2 | **No template loads a font.** Zero `next/font`/`@font-face`/`@import url` hits; `jewelery` declares `'Cormorant Garamond'`/`'Jost'` that never load; `beauty`'s identity is the browser's default serif. | grep across `templates/*/src` |
| D3 | **No type scale, no spacing system, no radius system.** `--radius` is decoupled from components (beauty uses 7 different literal radius steps); shadows are random per template. | `globals.css:710`, utility counts |
| D4 | **There is no hero.** `CoverFlow` is a bare image carousel: no headline, no CTA, `h-[450px]`, `object-contain`. The above-the-fold is "whatever the merchant uploaded". | `basis/.../CoverFlow.tsx:21` |
| D5 | **Product card is a shadcn `Card` with `m-10 max-w-xs hover:scale-105 shadow-2xl`** plus `from-green-500`/`from-blue-500` gradient buttons — the anti-pattern list from the requirement, verbatim. | `basis/.../ProductItem.tsx:106,123` |
| D6 | Invisible text bugs: announcement bar and **cart-drawer Checkout button** use `bg-primary text-foreground` (≈1.1:1 contrast in basis; inherited by beauty). | `basis/.../Header.tsx:322,431` |
| D7 | **Monoliths**: `Header.tsx` 461–488 lines holding 6 components incl. the cart drawer; `CheckoutForm.tsx` 418–463 lines with hand-rolled error `<p>`s while `form.tsx` is installed and unused. | line counts |

### 1.3 Missing storefront capabilities (every theme inherits these gaps)

| Gap | State today | What the backend already offers |
|---|---|---|
| Loading states | No `Skeleton` primitive; category page first-paints the *empty* state then flips | — |
| Error / not-found | Zero error UI; missing product/category renders a blank 200 (no `notFound()`) | `http-utils.ts` already types `ApiError` |
| Empty states | Cart drawer empty in basis/beauty is a blank panel | i18n key `CART_IS_EMPTY` exists |
| Sale badge | Absent from every product card; discount only on PDP | `productPrice.discounted/originalPrice/finalPrice` |
| Out-of-stock | `IN_STOCK` badge shouted on every product (noise) | `available`, `quantity`, `canBePurchased` |
| Variants / options | **Not implemented**; `options/variants/attributes` typed `any[]`; add-to-cart sends `sku` only | `ReadableProductOption{optionValues[{price,image}]}`, `ReadableProductVariant{images,inventory[{sku,price}]}`, `POST catalog /api/v2/product/{id}/variation`, `GET /api/v2/category/{id}/variations` |
| Sort / pagination | Hook hardcodes `page=0&count=15`, no sort | `GET /api/v2/products` honours Spring `Pageable` (`page`, `count`, `sort=`) |
| Filters | Manufacturer radio only; no price, no chips, no result count | manufacturer + category; option facets via variations endpoint |
| Search | **No endpoint does text search** (`productName`/`search` criteria are ignored by `ProductRepository.findAll`; only `sku LIKE` works) | must be an honest stub / SKU-only until backend adds it |
| Cart qty edit | Static qty + Remove only | `PUT /api/v1/cart/{code}` exists |
| RTL | physical `left/right` outnumber logical 3:1; `isRtl ? 'ms-3' : 'me-3'` double-flips; mobile nav `side="left"` hardcoded; Swiper never gets `dir` | — |
| Dark mode | `.dark` never applied; 2 of 4 templates have achromatic dark blocks | — |
| Reviews | none | no API — do not fabricate |

### 1.4 What merchants control today (and must keep working)

`Theme` enum → template; `ColorTheme` enum → 1 of 30 palettes (29 hex each); `logo`, `banner` (unused), `sliderImages[]`, `socialLinks[]`, supported/default languages, CMS boxes (`meta-title`, `meta-description`, `header-message`, `agreement`) and pages. Nothing else (no fonts/radius/layout). **Backend stays untouched, so this is the merchant input the new architecture must map.**

---

## Phase 2 — Target architecture

Verified mechanics: Next 16.0.0 names the middleware file `proxy.ts` (`PROXY_FILENAME='proxy'`); Tailwind 4.2.1 supports `@source`; next-intl 4.8.3; `next/font` present. spg/Caddy `domain_lookup` injects `Store-Id, Theme, Color-Theme, Default-Language, Supported-Languages` (`MerchantRoutingService.mapHeaders`). `POST /api/v2/product/{id}/variation` **ignores the selection** and returns the base price → variant pricing must be resolved client-side from `options[].optionValues[].price` / `variants[].inventory[].price,sku`. `run-lcl.sh:65` starts landing-ui with `npm run dev` after `build:libs-*` prep; spg routes to `landing-ui:8110`.

### 2.1 Workspace layout (all under `store-pod/landing-ui/`)

```
package.json                 workspaces: ["storefront", "libs/*", "themes/*"]   (app/, templates/* removed)
PRODUCT.md                   impeccable product truth (inherited by every theme)
storefront/                  THE single Next.js app — routes, proxy, loaders, theme resolution, token bridge
libs/types, services, hooks  unchanged packages (tsc-built), extended (§2.6)
libs/ui                      NEW source pkg @store-front/ui — the 21 shadcn primitives moved once + Skeleton/EmptyState/ErrorState/Price/QuantityStepper/Drawer(dir-aware)/AspectBox
libs/i18n                    NEW source pkg @store-front/i18n — routing.ts, navigation.ts (Link/useRouter), direction.ts/useDir()
libs/theme                   NEW source pkg @store-front/theme — ThemeDefinition contract, token schema, merchant color bridge, defineTheme()
themes/README.md             theme-direction catalog (§2.8)
themes/starter/              @store-front/theme-starter — plain reference theme (§2.7)
templates-deprecated/        `git mv templates/* app/` here; out of workspaces, eslint/tsc ignore; README "superseded"
scripts/new-theme.mjs        scaffold script
locales/{en,ar,es,fr,ru}.json unchanged location
Dockerfile, docker.sh, build.gradle   updated for Next standalone output (port 8110 stays)
```

"Source pkg" = `exports: {".":"./src/index.ts","./*":"./src/*"}`, no build step, compiled by Next via `transpilePackages`. Themes **must** be source packages (`next/font`, CSS, `'use client'`). One alias scheme: storefront uses `@/*` for its own `src` + `@store-front/*`; themes/libs use only `@store-front/*` (ESLint `no-restricted-imports` forbids `@/` inside `themes/**`). Delete the `@/services/*`, `@/types/*` tsconfig path hacks.

Root scripts: `build:libs` (types→services→hooks), `dev`/`build`/`start` → `--workspace=storefront` (`next dev -p 8110 --turbopack` / `next build` / `next start -p 8110`), `lint`, `typecheck`, `new-theme`. `run-lcl.sh` and `build.gradle` keep working unchanged.

`storefront/src`:
```
proxy.ts                                  replaces app/src/server.ts + template-manager.ts (§2.4)
instrumentation.ts                        port of app/src/instrumentation.ts (OTel NodeSDK)
i18n/request.ts                           messages from ../../../locales/${locale}.json
app/globals.css                           ONE Tailwind entry: @import tailwindcss, tw-animate-css; @source "../../../libs/ui/src", "../../../themes/*/src"; @import "./themes.css"; single @theme inline (§2.3)
app/themes.css                            GENERATED by new-theme.mjs — one @import "@store-front/theme-<id>/tokens.css" per theme
app/global-error.tsx
app/(system)/layout.tsx, store-not-found/page.tsx     own root layout, no theme/i18n (no Store-Id → 404)
app/(storefront)/[locale]/layout.tsx      html/body, getTheme(), merchant tokens, fonts, providers, <theme.layout.Root>
app/(storefront)/[locale]/{error.tsx,not-found.tsx,page.tsx,category/[url],product/[url],content/[url],checkout,checkout/success,checkout/cancel,customer,customer/order/[id],login,callback}
shell/request/{headers.ts,store-context.ts}   getStoreHeaders(), getStoreContext()=cache(extractSsrContext)
shell/theme/{registry.ts,get-theme.ts,legacy-theme-map.ts,theme-client-states.tsx}
shell/tokens/merchant-tokens.ts           color-theme header → deriveColorTokens → inline style
shell/loaders/{layout,home,category,product,content,checkout,customer}.ts   cache()'d, Promise.all
shell/seo/metadata.ts
shell/search/{search-provider.ts,noop-search-provider.ts,category-nav-search-provider.ts}
```

### 2.2 Layers and dependency direction (strictly downward)

```
storefront (shell): routes · proxy · i18n · auth redirects · store ctx · theme resolution · merchant token bridge · loaders · SEO · Suspense/notFound/error plumbing · search wiring
themes/<id>:        ThemeDefinition — tokens.css · fonts · layout.Root · pages.* (full-page composition) · states.* · layout config · optional mapMerchantColors
@store-front/theme: contract, token schema, color bridge, contrast math, defineTheme
@store-front/ui:    shadcn primitives shared ONCE (CSS-var themeable) + Skeleton/EmptyState/ErrorState/Price/QuantityStepper/Drawer
@store-front/hooks: useCart · useUser · useCustomer · useCheckoutForm · useOrderStatus · useProductListing · useProductPurchase · useSearch
@store-front/services + types + i18n
```
Rules: the shell imports themes only through the registry; themes never import the shell; **themes do not fork primitives** — they restyle via tokens/variants/`className` or compose wrappers; a primitive that proves un-themable gets a new cva variant in `libs/ui` (benefits all themes).

### 2.3 Theme contract and design tokens (`libs/theme/src/contract.ts`, `tokens.ts`)

```ts
export interface ThemeDefinition {
  id: string; name: string; version: string;
  fonts: { variables: string /* next/font .variable classes for <html> */; roles: {sans: string; display?: string; mono?: string} };
  tokens: { mapMerchantColors?: (schema: ColorSchema, meta: {isDark: boolean}) => Partial<ColorRoleTokens>; minContrast?: number };
  layout: { config: ThemeLayoutConfig; Root: ComponentType<RootLayoutProps> };
  pages: { Home, Category, Product, Content, Checkout, CheckoutResult, Customer, Order };   // ComponentType<PageProps<XData>>
  states: { PageSkeleton: Record<'home'|'category'|'product'|'content'|'checkout'|'customer'|'order', ComponentType>;
            ErrorState /*client*/; NotFound; EmptyState /*client*/; Redirecting /*client*/ };
}
export interface ThemeLayoutConfig { header:{sticky:boolean; heightPx:{base:number; lg:number}}; cart:'drawer'|'page';
  mobileNav:'drawer'|'fullscreen'|'bottom-bar'; productGrid:{base:1|2; sm:number; lg:number; xl:number};
  productImageAspect:'1/1'|'3/4'|'4/5'|'4/3'; container:'narrow'|'content'|'wide'|'fluid'; search:'header'|'overlay'|'hidden' }
export interface PageContext { store: Store; storeContext: StoreContext; locale: string; dir: 'ltr'|'rtl'; theme: ThemeLayoutConfig }
export interface LayoutData { store; categories: Category[]; pages: Page[]; announcement?: Box; search: {text:boolean; suggestions:boolean} }
export interface HomeData { hero:{slides:SliderImage[]; banner?:ImageFile}; groups:{code; title; products: Product[]}[] }
export interface CategoryData { category; breadcrumbs; initial: ProductListingPage; query: ListingQuery; facets: ListingFacets }
export interface ProductData { product; breadcrumbs; related: Product[] }
export interface ContentData { page; html; breadcrumbs }   CheckoutData {requireLogin}   CheckoutResultData {outcome:'success'|'cancel'; requireLogin}   OrderData {orderId}
```
`defineTheme(def)` asserts every member exists → a missing piece fails `next build`.

**Token schema** — plain CSS vars; the only `@theme inline` lives in `storefront/src/app/globals.css` and maps them once (replaces the 650-line shim):

| Group | Vars | Owner |
|---|---|---|
| Color roles | `--background --foreground --card(-foreground) --popover(-foreground) --primary(-foreground,-hover) --secondary(-fg) --muted(-fg) --accent(-fg) --destructive(-fg) --success(-fg) --warning(-fg) --info(-fg) --sale(-fg) --border --input --ring` | merchant bridge (inline style on `<html>`); theme may re-map via `mapMerchantColors` |
| Fonts | `--font-sans --font-display --font-mono` | theme `tokens.css` → next/font vars |
| Type scale | `--text-xs…--text-6xl`, `--leading-*`, `--tracking-*` | theme |
| Spacing/density | `--space-unit`, `--density`, `--section-y`, `--gutter` | theme |
| Radius | `--radius-control --radius-card --radius-image --radius-badge --radius-overlay` | theme |
| Borders/shadows | `--border-width`, `--shadow-sm/md/lg/overlay` | theme |
| Containers | `--container-narrow/content/wide` | theme |
| Motion | `--duration-fast/base/slow`, `--ease-standard/emphasized` | theme |
| Layout | `--header-h(-lg)`, `--aspect-product` | theme |
| Breakpoints | Tailwind defaults — shared, not per theme | shared |

Each theme's `tokens.css` is scoped `[data-theme="<id>"] { … }` and statically imported via `themes.css` (≈2–4 KB/theme, no FOUC, no per-request CSS injection). `@source "../../../themes/*/src"` is mandatory (Tailwind v4 auto-detection is cwd-based) — verify a theme-only utility lands in the built CSS.

**Merchant color bridge** (`libs/theme/src/merchant-bridge.ts`, `color-math.ts`): input `ColorSchema` from `getThemeColors(Color-Theme header ?? store.colorTheme ?? RAINBOW)`. `isDark = luminance(background) < 0.3` → `data-color-scheme`. Direct mappings (`primary, secondary, accent, destructive←error, success, warning, info, border, ring, primary-hover←hoverPrimary`); derived (`card/popover` from background, `muted` = mix(bg, fg, 6 %), `input = border`, `sale = error`); every `*-foreground` chosen white/near-black by contrast then `ensureContrast(≥ minContrast, default 4.5)`; theme's `mapMerchantColors` spreads last. Output → `style` attribute on `<html>` (inline beats stylesheets deterministically). Unit-test: all 30 presets × all pairs ≥ 4.5.

### 2.4 Theme resolution in one Next app

- `proxy.ts`: no `store-id` header (and no `FALLBACK_STORE_ID`) → rewrite `/store-not-found` (404); `/` → redirect to `NEXT_LOCALE` cookie ?? `default-language` header ?? `routing.defaultLocale`, constrained by `supported-languages`; otherwise `next-intl createMiddleware(routing)`; dev/`STOREFRONT_THEME_OVERRIDE=true` only: `?theme=x` → `storefront-theme` cookie.
- `shell/theme/registry.ts`: static map of dynamic imports between `// @themes:start/end` markers — `starter: () => import('@store-front/theme-starter')`. Each theme is its own server chunk; client bundles only carry `'use client'` components actually rendered.
- `getTheme()` = `cache()`: cookie override → `theme` header → `STOREFRONT_THEME` env → `resolveThemeId` (registry hit → legacy map → `STOREFRONT_FALLBACK_THEME ?? 'starter'`). `legacy-theme-map.ts` lists **all 15 TS `Theme` enum values** explicitly.
- Root layout: `Promise.all([getTheme(), loadLayoutData()])`; `<html lang dir data-theme data-color-scheme className={theme.fonts.variables} style={merchantVars}>`; `NextIntlClientProvider` → `ThemeClientStates` (context carrying the theme's client state components so `error.tsx` can use them) → `<theme.layout.Root ctx data>{children}</theme.layout.Root>`.
- Fonts: `themes/<id>/src/fonts.ts` with module-scope `next/font/google|local`; only the resolved theme module is imported per request. Risk + fallbacks in §4.

### 2.5 Page composition ownership — full-page ownership by the theme

Shell pages = loaders + metadata only; they render `<Suspense fallback={<theme.states.PageSkeleton.x/>}><Body/></Suspense>` and `Body` calls the loader (`ApiError NOT_FOUND → notFound()`, other errors → `error.tsx`) then `<theme.pages.X ctx data/>`. `theme.layout.Root` owns skip-link, announcement, header (logo, nav from categories/pages, locale switcher, account, cart trigger), cart drawer/page, mobile nav, `<main>`, footer. `login`/`callback` islands stay shell-owned but render `theme.states.Redirecting`; `customer/*` wrapped by headless `<Secured>` (moved to libs). Shared "sections" are headless behaviours (hooks, `Price`, `QuantityStepper`, `SearchProvider`); the starter shows a section-per-file composition future themes copy and rearrange.

### 2.6 Shared-layer fixes every theme inherits (backend untouched)

| Gap | Change |
|---|---|
| Typed options/variants | `libs/types/src/product-groups.ts`: `ProductOption`, `ProductOptionValue{price?,image?}`, `ProductVariant{variation,images,inventory[{sku,price,quantity?}]}`, `ProductAttribute`, `SelectedVariantValue` replace the `any[]`s |
| Listing | `libs/types/src/listing.ts` (`ListingQuery{page,count,sort,manufacturerId?,optionValueIds?}`, `ListingFacets`); `services/product-category.ts` → `getProducts(ctx, query)` (Pageable `page/count/sort=`), `getCategoryVariants` (`GET /api/v2/category/{id}/variations`); `hooks/use-product-listing.ts` replaces `useProductCategoryFilter`: `{items,page,totalPages,total,query,setSort,setPage,setManufacturer,toggleOptionValue,facets,status,error,retry}`, takes SSR `initial`, syncs `?page&sort` to URL. Centralised `SORT_MAP` — verify sortable property names against `ProductRepository` (unsupported → 500) |
| Variants/add-to-cart | `hooks/use-product-purchase.ts` replaces `useProductDetailedAddToCart`: selection per `option.variant===true`, resolves variant → sku/price/images/stock client-side; `canAdd = allRequiredSelected && inStock`; variation endpoint called opportunistically only |
| Search | `types/search.ts` + `hooks/use-search.ts` `SearchProvider{capabilities:{text,suggestions}; search()}`; shell ships `noopSearchProvider` and `categoryNavSearchProvider` (client match over loaded categories/pages); `LayoutData.search` lets themes branch honestly (no fake text search) |
| States | `ThemeStates` contract + `libs/ui` Skeleton/EmptyState/ErrorState; hooks expose `status`, `error: ApiError`, `retry()` |
| Layout fetch | `loadLayoutData` = `Promise.all(store, categories, contents, header-message)`; `extractSsrContext`/`getStore` in React `cache()`; metadata boxes parallel |
| RTL | `libs/i18n` `useDir()`; `Drawer` auto side; Swiper gets `dir` + `key={dir}`; ESLint warning on physical `pl-/pr-/ml-/mr-/left-/right-/text-left/text-right/rounded-l/r` literals inside `themes/**` |
| Product semantics | `services/product-presenter.ts`: `isOnSale`, `isOutOfStock`, `discountLabel`, `primaryImage` (placeholder fallback), `secondaryImage` |
| Cart | `CartManager.updateQuantity(sku, qty)`; `useCart` exposes `updateQuantity,status,isEmpty,count` |
| i18n (all 5 locales) | `COMMON.*`, `ERRORS.*` (not-found per kind, store-not-found), `PAGE.CATEGORY.{SORT_*,RESULTS_COUNT,PAGINATION,FILTERS,CLEAR_FILTERS}`, `PAGE.PRODUCT.{SELECT_OPTION,OPTION_REQUIRED,SALE,SAVE_PERCENT,LOW_STOCK}`, `COMPONENTS.SEARCH.*`, `COMPONENTS.CART.{EMPTY_*,QUANTITY,…}`, `COMPONENTS.HEADER.{OPEN_CART,OPEN_MENU,ACCOUNT,LANGUAGE,ANNOUNCEMENT_DISMISS}` |

### 2.7 Starter theme — `themes/starter/` (plain, but complete)

```
package.json (@store-front/theme-starter; exports ".", "./tokens.css", "./*")   README.md (what each file proves)   DESIGN.md (placeholder)
src/index.ts        defineTheme({id:'starter', fonts, tokens:{}, layout:{config,Root}, pages, states})
src/fonts.ts        one next/font (proves font wiring)      src/tokens.css  [data-theme="starter"]{…all theme-owned tokens…}
src/config.ts       cart:'drawer', mobileNav:'drawer', grid {1,2,3,4}, aspect '1/1', container 'content'
src/layout/         Root, Header(≤120 lines), Nav, MobileNav*, LocaleSwitcher*, AccountButton*, CartButton*, CartDrawer* (qty stepper, remove, empty state, checkout CTA), Announcement, Footer
src/pages/          Home, Category, Product, Content, Checkout, CheckoutResult, Customer, Order
src/sections/       Hero, ProductRail (Swiper, dir-aware), Listing* (useProductListing: filters, sort, pagination, count), BuyBox* (useProductPurchase: variant selector, stepper, add), Gallery, RelatedRail, CheckoutForm (uses libs/ui form.tsx, ≤200 lines), OrderSummary, CustomerTabs, OrderDetails, SearchBox (branches on capabilities)
src/components/     ProductCard (placeholder, sale + out-of-stock badge, price, clamp, add), Price, Breadcrumbs, SectionHeading, PageShell
src/states/         skeletons/{home,…,order}, ErrorState*, NotFound, EmptyState*, Redirecting*        (* = 'use client')
```
`scripts/new-theme.mjs <name>`: copy starter → `themes/<name>` (fresh DESIGN.md placeholder), rename id/package/selector, insert registry loader line, `@import` in `themes.css`, `transpilePackages` entry, add enum value to `libs/types/src/store.ts` + `legacy-theme-map.ts` if absent, `npm install`, print next steps (impeccable flow, `?theme=<name>`, Java enum reminder — backend task, out of scope).

### 2.8 Theme-direction catalog — `themes/README.md`

Each entry is a **brief** (target merchant + structural thesis + color strategy + density). The visual world (faces, materials, palette rendition) is **not** pre-chosen here: at generation time impeccable's `new-work` flow runs `concept-seed.mjs --scope direction` and the roll decides; the catalog must not steer toward the category default (cream+serif luxury, black+neon tech, broadsheet editorial).

| id | Target merchant / replaces enum | Structural thesis (header · hero · card · PDP · listing · cart) | Color strategy · density |
|---|---|---|---|
| `atelier` | premium/luxury — JEWELERY, WATCHES | two-row centered header · single full-bleed hero with caption, no carousel · 2-col large image cards, no borders · gallery-led PDP, sticky buy box, materials accordion · 2–3-col listing, "refine" top sheet · cart as full page | Restrained: merchant primary as ink/accent only · low |
| `market` | high-density marketplace — ELECTRONICS, TOOLS | compact header, dominant search slot + mega-menu · promo-tile hero grid · 4–6-col cards, spec line, price-forward, quick add · spec-table-first PDP, sticky price rail · left filter rail, sticky sort bar, counts, pagination · drawer with line totals | Committed: primary on CTAs, info badges · high |
| `nordic` | minimal home/lifestyle — FURNITURE | airy header, wide gutters · split image/text hero, one CTA · 4/5 image-first cards, tonal hover · 60/40 PDP + long-form story sections · 3-col, filters as chips · minimal drawer | Tonal from merchant bg/fg, primary muted · low–medium |
| `editorial` | bold fashion — FASHION (TS-only enum value) | oversized wordmark, uppercase text nav · asymmetric hero, display type over image · lookbook grid, mixed aspect, hover second image · vertical image scroll + sticky buy column · dense 4-col, filters drawer · side panel | Monochrome base, accent only on CTA/sale · medium |
| `fresh` | food / consumables / baby — FOOD, BABY, BEAUTY, COSMETICS | rounded header + category tile strip · short offer-card carousel · quick-add stepper cards, unit-price slot · compact PDP, sticky bottom bar on mobile · horizontal chips + grid · drawer with progress slot | Full palette, saturated, large radius tokens · medium–high |
| `showroom` | tech/sports brand store — SPORTS, GLASSES | dark-capable header, feature stripes · spotlight product hero · 3-col cards with feature bullets · spec-driven PDP, comparison table, sticky gallery · facet chips · drawer | Drenched/dark path of the bridge · medium |
| `bazaar` | clean modern general retail — BASIS, MODERN, DEFAULT (fallback theme once built) | standard header with search/nav/account/cart · slider hero with headline + CTA · 3–4-col cards with sale badge · classic 2-col PDP · filter rail + sort, mobile filter drawer · drawer | merchant palette as given · medium |

### 2.9 Docs and impeccable artefacts

- Rewrite `.agents/skills/project-structure/references/new-landing-ui-template.md` → "New Storefront Theme Guide": architecture recap; `npm run new-theme <name>`; impeccable flow (`context.mjs --target themes/<name>` → PRODUCT.md inherited → `new-work` + mandatory direction roll → build page-by-page Home→Category→Product→Cart→Checkout→Customer → finish reviewer → documenter writes `themes/<name>/DESIGN.md`); what to edit vs never edit; contract checklist (every member, every state, RTL, mobile, 5-locale keys, no `@/`, no hard-coded colors, logical properties, shadcn skill rules); verification steps; backend enum note.
- Update `references/landing-ui.md` (layout, request flow: Caddy headers → proxy → getTheme → registry → layout; build/dev; conventions).
- `store-pod/landing-ui/PRODUCT.md` (impeccable schema 1): web; shoppers of SMB merchants on multi-tenant SaaS, 5 locales incl. RTL; purpose/positioning; operating context (merchant domain via spg, theme/color/language headers); capabilities (catalog groups/categories/options/variants/manufacturers, cart, checkout COD/Stripe/PayPal/manual, account/orders, CMS, slider, social) and **absences not to fabricate** (no text search, reviews, wishlist, promotions API); brand commitments (merchant logo + ColorTheme honoured); WCAG AA, RTL first-class. Inferred facts marked inferred.
- `.agents/requirments/storefront-themes.md` gains a short "Status / where the answer lives" pointer to the plan, guide and catalog (no duplication).

---

## Phase 3 — Migration & implementation order

| # | Work | Verification |
|---|---|---|
| 1 | Scaffold `storefront/` (config, proxy, i18n, `(system)` + `(storefront)` routes as thin placeholders), `libs/ui` (move primitives + new ones), `libs/i18n`, `libs/theme` (contract), root package.json/workspaces/eslint, `instrumentation.ts` | `npm i && npm run build` green; `npm run dev` → `/`→`/en`; no store-id → 404 page; `/ar` → `dir=rtl`; `?theme=x` sets cookie |
| 2 | Token bridge + contrast math + `defineTheme`; `globals.css` single `@theme inline` + `@source` + `themes.css`; registry/getTheme/legacy map; layout wiring (fonts, `data-theme`, inline vars, ThemeClientStates); loaders parallel + cached; metadata; Suspense/notFound/error; search provider stub | bridge unit tests (30 presets, all pairs ≥ 4.5, dark detection); built HTML shows `data-theme`, `style="--primary…"`, font class; theme-only utility present in built CSS; font CSS emitted from dynamically imported theme |
| 3 | Shared-layer API first (types, services, hooks of §2.6, i18n keys ×5, presenter, cart qty) — starter depends on them | `tsc` on libs; hook QA against local catalog; confirm `sort=` props and `optionValueIds` facet or hide facets |
| 4 | Starter theme (§2.7) + `scripts/new-theme.mjs` (`npm run new-theme tmp-check` then delete) | `npm run build && npm run lint`; dev `STOREFRONT_THEME=starter`; `./extra/scripts/run-lcl.sh` + spg with `Theme: BASIS` (legacy→starter) and `Color-Theme: MIDNIGHT` (dark path); browser QA desktop/tablet/mobile: home, category (sort/page/filter URL sync), product (variants, out-of-stock, sale), cart drawer (empty→add→qty→remove), checkout, success/cancel, customer (login redirect), content, 404 product, thrown error; all again under `/ar/` |
| 5 | `git mv templates app → templates-deprecated/`; Dockerfile → Next `output:'standalone'` + `outputFileTracingRoot` (copy `.next/standalone`, `.next/static`, `public`; `CMD ["storefront/server.js"]`, `PORT=8110 HOSTNAME=0.0.0.0`); `docker.sh`/`scripts.sh`; `.gitignore` | `./gradlew :store-pod:landing-ui:build`; image boots, `curl -H 'Store-Id:…' -H 'Theme: starter' :8110/en` 200 and `/fr` (locales traced); `run-lcl.sh` path unchanged |
| 6 | Docs + catalog + PRODUCT.md + starter README/DESIGN placeholder | follow the guide literally with `new-theme demo` on a scratch branch; `context.mjs --target themes/starter` from `store-pod/landing-ui` resolves PRODUCT.md (inherited) + DESIGN.md (child) |

## Phase 4 — Risks

1. **Tailwind scanning** of `themes/*/src` and `libs/ui/src` needs explicit `@source`; verify a theme-only class in built CSS.
2. **next/font in a transpiled workspace package + dynamically imported theme module** — expected OK; fallback A: `storefront/src/theme-fonts/<id>.ts` imported by the registry; fallback B: per-theme route group re-export stubs generated by the scaffold script. Google fonts need network at build → document `next/font/local`.
3. **RSC boundaries** — theme pages are server components with serialisable data; `ThemeStates` used by `error.tsx` must be `'use client'` (starter demonstrates; docs state it).
4. **Header availability** — all five Caddy headers readable in `proxy.ts` and `headers()`; locally without spg use `FALLBACK_STORE_ID` / `STOREFRONT_THEME`.
5. **Docker/locales** — standalone tracing must include `libs/*/dist` and locale JSON chunks; verify with `curl /fr` in the image.
6. **Catalog sort/facets** — raw `Pageable` `sort=` (unsupported prop → 500) and `optionValueIds` filtering need verification before exposing UI.
7. **Enum drift** — TS 15 vs Java 13 values; legacy map covers all TS values; Java enum changes remain a backend task.
8. **Lockfile churn** when workspaces change — one-time, acceptable.

## Critical files

- `store-pod/landing-ui/storefront/src/app/(storefront)/[locale]/layout.tsx` (new) — theme resolution, tokens, fonts, providers, Root
- `store-pod/landing-ui/libs/theme/src/{contract.ts,tokens.ts,merchant-bridge.ts,color-math.ts,define-theme.ts}` (new)
- `store-pod/landing-ui/storefront/src/proxy.ts` (new) — replaces `app/src/server.ts` + `template-manager.ts`
- `store-pod/landing-ui/storefront/src/app/globals.css` (new) — single `@theme inline`, `@source`, `themes.css`; replaces `templates/*/src/app/[locale]/globals.css`
- `store-pod/landing-ui/storefront/src/shell/theme/{registry.ts,get-theme.ts,legacy-theme-map.ts}` (new)
- `store-pod/landing-ui/themes/starter/src/index.ts` (new) — reference implementation
- `store-pod/landing-ui/libs/hooks/src/{use-product-listing.ts,use-product-purchase.ts,use-search.ts}` (new) and `libs/types/src/{product-groups.ts,listing.ts,search.ts}`
- Ports from: `templates/basis/src/app/[locale]/layout.tsx`, `libs/types/src/color-schema.ts`, `libs/hooks/src/use-product-category-filter.ts`
- Docs: `.agents/skills/project-structure/references/{new-landing-ui-template.md,landing-ui.md}`, `store-pod/landing-ui/themes/README.md`, `store-pod/landing-ui/PRODUCT.md`

---

## Status — implemented 2026-08-21

Everything in Phases 2–3 is in place on branch `feat/mirror-console-ui` (uncommitted):

- `store-pod/landing-ui/storefront/` — single Next 16 app: `proxy.ts` (Store-Id gate, `/`→`/{lang}`, `?theme=` dev override), routes
  (loaders + metadata only; 404-capable routes have no Suspense so `notFound()` yields a real 404 — home/checkout/customer use
  `loading.tsx` skeletons; the home one sits in a `(home)` route group on purpose), `shell/` (theme registry + resolution +
  legacy map, merchant token bridge, loaders, search providers, auth islands), `globals.css` (single `@theme inline` mapping,
  stock palette removed), `themes.css` (generated `@source` + tokens import per theme), `/css/login.css` route for cua.
- `libs/theme` (contract, token schema, colour bridge + 32 passing tests), `libs/ui` (primitives moved once + Skeleton/Empty/
  Error/Price/QuantityStepper/Drawer/AspectBox), `libs/i18n`; `libs/types|services|hooks` extended (typed options/variants,
  listing/search types, `useProductListing`, `useProductPurchase`, `useSearch`, cart qty, product-presenter).
- `themes/starter` — complete reference theme; `scripts/new-theme.mjs`; `themes/README.md` catalog; `PRODUCT.md`.
- `templates-deprecated/` holds the old templates + Express app (out of workspaces). Dockerfile → standalone output.
- Docs: `new-landing-ui-template.md`, `landing-ui.md`, SKILL.md rows (both `.agents` and `.claude` copies), requirement pointer.

Verified: `npm run build` (standalone, theme utilities + Inter font in the CSS), `lint`, `typecheck`, bridge tests; live QA
through spg (`run-lcl.sh`) on the demo store: home, category (sort/filters/20 products), product, cart drawer (add/qty/remove),
checkout (login-required dialog), `/ar` RTL, mobile viewport, real 404 for unknown category/content, error state for backend 500.

Found along the way (backend, not changed): `GET /api/v2/product/name/{unknown}` returns 500 instead of 404; local images 404
(no MinIO) — known gap. Left for later: generating the first designed theme via `npm run new-theme` + the impeccable flow.

## First designed theme — `beauty` (2026-08-21)

Built with the impeccable flow: init answers (beauty + fashion boutique, "trusted expert counter"), direction roll
(`concept-seed` key 85f13d63, user chose the dealt challenger **Industrial Quote Grammar**), contract in
`themes/beauty/src/layout/Root.tsx`, full rebuild of tokens/fonts/layout/pages/sections/states, detector run, fresh
finish-reviewer (two fix batches within budget), documenter → `themes/beauty/DESIGN.md`.
Architecture fix found on the way: the shell's `:root` token fallbacks now use `:where(:root)` (zero specificity) —
before, they silently overrode every theme token (fonts included). ESLint config moved to the landing-ui root so
`themes/**` and `libs/**` are actually linted; hook-rule fixes applied to `starter` too.

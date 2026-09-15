# `store-pod/landing-ui` — storefront and theme system

The customer-facing storefront (port **8110**). Next.js 16 / React 19, TypeScript, Tailwind v4 + shadcn/ui,
`next-intl`. An npm-workspaces monorepo wrapped by `build.gradle` (`ui-conventions`) so the root Gradle build
can build and containerize it.

## Workspace layout

```
store-pod/landing-ui/
├── package.json           name "store-front", workspaces: ["storefront", "libs/*", "themes/*"]
├── PRODUCT.md             impeccable product truth for every theme
├── storefront/            THE single Next.js app ("shell"): routes, proxy, loaders, theme resolution, token bridge
├── libs/
│   ├── types/             @store-front/types      — Store, Product (options/variants typed), listing, search, Theme enum
│   ├── services/          @store-front/services   — API clients, product-presenter, cart-manager
│   ├── hooks/             @store-front/hooks      — useCart, useUser, useCustomer, useCheckoutForm, useOrderStatus,
│   │                                                useProductListing, useProductSearch, useProductPurchase,
│   │                                                useSearch, useSearchProvider, productSearchProvider
│   ├── ui/                @store-front/ui         — shadcn primitives shared once (+ Skeleton, EmptyState, ErrorState, Price, …)
│   ├── i18n/              @store-front/i18n       — routing, Link/useRouter, direction, useDir()
│   └── theme/             @store-front/theme      — ThemeDefinition contract, token schema, colour bridge, defineTheme()
├── themes/
│   ├── README.md          theme-direction catalog (briefs for future themes)
│   └── starter/           @store-front/theme-starter — plain reference theme, copy source
├── templates-deprecated/  the old one-Next-app-per-theme generation + Express server (not built)
├── locales/               SHARED across all themes: en, ar, es, fr, ru
├── scripts/new-theme.mjs  scaffold + register a theme
└── Dockerfile, docker.sh, build.gradle
```

`types`, `services`, `hooks` are tsc-built (`dist/`); `ui`, `i18n`, `theme` and every theme are **source
packages** compiled by Next (`transpilePackages` + tsconfig paths in `storefront/tsconfig.json`).

## How theming works end to end

1. spg/Caddy `domain_lookup` injects `Store-Id`, `Theme`, `Color-Theme`, `Default-Language`,
   `Supported-Languages` request headers.
2. `storefront/src/proxy.ts`: no `Store-Id` → `/store-not-found` (404); `/` → `/{lang}`; next-intl routing; then
   the store's theme picks its route tree: `/{locale}/…` is **rewritten** (never redirected, so the browser keeps its
   URL and client navigations take the same path) to `/t/<id>/{locale}/…`. `/t/…` requested from outside is a 404.
   The id comes from `resolveThemeId()` (`src/shell/theme/theme-id.ts`, which imports no theme): dev-only
   `?theme=<id>` / its override cookie → `Theme` header → `STOREFRONT_THEME` → a registered id, else
   `legacy-theme-map.ts` (every `Theme` enum value, lowercased), else fallback (`starter`).
3. One route tree per theme, `app/(storefront)/t/<id>/[locale]/**`: 25 generated one-line files that bind the route
   factories in `src/shell/routes/` to that theme, imported statically. Next splits CSS and JS per route, so a page
   loads only its own theme's CSS and JS. Never edit a tree: change the factory, or `ROUTES` in
   `scripts/theme-routes.mjs` for a new route, then `npm run theme-routes` (`npm test` runs `--check`).
4. `registry.ts` (every theme as a dynamic import, via `getTheme()`) serves `/api/theme-manifest` only. A page that
   imported it would put every theme's client code back into its CSS and JS.
5. The root layout (`src/shell/routes/layout.tsx`, bound by each tree) fetches store + categories + pages + announcement
   in parallel, derives the merchant colour-role tokens from the `Color-Theme` preset
   (`libs/theme/src/merchant-bridge.ts`, contrast-guarded) and renders
   `<html data-theme=<id> data-color-scheme style="--primary:…" class="<next/font vars>">` →
   `theme.layout.Root` → page → `theme.pages.X`.
6. CSS: one Tailwind entry per theme, `app/theme-css/<id>.css` (generated): `@import "../globals.css"` plus `@source`
   for that theme's folder, so a page's utilities are the shared ones and its own theme's. `globals.css` is the shared
   part: tokens → utilities (`@theme inline`), fallbacks, base, `libs/ui`'s `@source`; the `(system)` layout uses it
   alone. A tree's layout imports its entry *before* the theme: Tailwind declares the cascade-layer order, and a
   theme's `tokens.css` opening `@layer components` first would rank components below base (preflight would strip the
   theme's buttons). Theme tokens are scoped to `[data-theme="<id>"]`. The stock Tailwind palette is removed — colour
   is role-based only.

Pages in the shell do loading + metadata only (`Suspense` with the theme's skeleton, `notFound()` on 404,
`error.tsx` → the theme's `ErrorState`). Composition is owned by the theme.

## Build / run

```
npm run build      # build:libs (types → services → hooks) then next build (standalone output)
npm run dev        # build:libs then next dev -p 8110 --turbopack
npm run lint | typecheck
npm test --workspace=libs/theme     # colour bridge tests
npm run new-theme <id>          # also writes the theme's route tree + Tailwind entry
npm run theme-routes            # regenerate every tree (after changing ROUTES); npm test runs --check
```
`lcl start -d` starts it (its `prepare` builds the workspace libs, then `next dev`). Docker: the
image copies `storefront/.next/standalone` (build on host/CI first — see `docker.sh`).

**Local dev URLs.** The storefront needs the store headers spg injects, so the supported dev URL is through spg:
`http://org1-store1.spg-507f1f77.gateway.com/en?theme=<id>` (stack up via `lcl start -d`). Hitting `http://localhost:8110/en`
directly works for SSR only because the proxy falls back to `FALLBACK_STORE_ID` (env, then the demo-store constant) —
but browser-side calls (cart, listing, auth) go to `/catalog`, `/checkout`, … on the same origin, which only spg routes;
set `EXTERNAL_SPG=http://spg-507f1f77.gateway.com` if you must use localhost. `?theme=<id>` sets a dev-only override
cookie (`?theme=` clears it); unknown ids resolve through the legacy map to the fallback theme. `next dev` refuses
cross-origin requests to its `/_next` resources (the HMR socket among them), so `next.config.ts` allows the subdomains of
the `INTERNAL_SPG` host (`*.spg-507f1f77.gateway.com` under lcl); any other dev host needs an `allowedDevOrigins` entry.

### The request signal (`start.mjs` only)

`start.mjs` puts the request signal in front of Next (`storefront/scripts/server/`), which `next dev` does not run (the page cache, its own PR, joins it here):

- **The request signal** (`request-scope.mjs`). A render's backend reads (`apiFetch`, server side, GET only) abort when
  its shopper disconnects, and give up after `STOREFRONT_BACKEND_TIMEOUT_MS` (3000; 0 waits); a write carries neither.

To QA them under lcl, run the production build in place of lcl's `next dev` (`qa/landing-ui-qa.md` LOAD).

## Adding a theme

**➡️ `new-landing-ui-template.md`** — scaffold, impeccable design flow, contract checklist, verification.
Direction briefs live in `themes/README.md`.

## Non-negotiable conventions in theme code

- No literal UI text — `t()` and keys in **all 5** locale files.
- No hard-coded colours (they don't compile); role tokens only. Fonts/radius/spacing through `tokens.css`.
- RTL-safe: logical utilities, `DrawerContent side="start|end"`, Swiper `dir={useDir()}`.
- No `@/` imports in themes/libs (ESLint). Primitives from `@store-front/ui`, never forked.
- Behaviour from `@store-front/hooks`; every state (loading/empty/error/not-found) rendered.
- Search UI follows `SearchCapabilities` from `LayoutData`, via `useSearchProvider(capabilities)` — a theme
  never picks a provider itself, and never assumes text search is available.

## Product search

The catalog answers two public endpoints: `/api/v2/products/search` (a ranked page plus counted category,
brand and type facets, a did-you-mean, and the language the results actually came from) and
`/api/v2/products/suggest` (autocomplete, capped and cached, no count query).

- `ProductSearchService` in `libs/services` calls them, then merges price and stock from the inventory
  service exactly as the category listing does.
- One provider, `productSearchProvider` in `libs/hooks`, backs every theme's `SearchBox`. It used to be
  copied into each theme with `{text: false}` hard-coded, which meant the shell's capability flags never
  reached the browser.
- `storefront/src/shell/search/index.ts` chooses it; `NEXT_PUBLIC_STOREFRONT_SEARCH_PROVIDER=navigation`
  falls back to categories-and-pages suggestions, `none` turns the box off.
- `/{locale}/search` renders `theme.pages.Search` when the theme has one, otherwise the shell's fallback.
  It is `noindex, follow` and excluded from the sitemap — a results page has no content of its own.
- Not filterable: price and in-stock. They live in the inventory service keyed by sku, so the catalog cannot
  filter or sort on them; they are merged in after paging.

## Shopper login and registration

cua is headless; these screens are the storefront's. `/{locale}/login` is two pages under one route: without
`?auth=1` it starts the OAuth2 flow (`shell/auth/login-redirect.tsx` — what deep links and `Secured` rely on);
with the marker cua added it renders `theme.pages.Login ?? DefaultLoginPage` with a `LoginData` of
`{action: '/cua/login', clientId, lang, error?, socialLogins}`. The form is plain HTML posting to cua, so no
client JavaScript is in the hand-off. `/{locale}/register` renders `theme.pages.Register ?? DefaultRegisterPage`,
driven by `useRegisterForm` → `AuthService.register()` (JSON, typed conflicts mapped onto the field) → `login()`.
Both pages are optional in the theme contract like `Search`, and every registered theme implements them in its own
idiom (`pages/{Login,Register}.tsx` + `sections/{LoginForm,RegisterForm}.tsx`); the token-only fallbacks in
`storefront/src/shell/theme/default-{login,register}-page.tsx` cover a theme that has not yet. Strings: `PAGE.LOGIN.*`,
`PAGE.REGISTER.*` in all five locales.

## Checkout redirect flow

After payment the gateway returns to `{domain}/{locale}/checkout/success?orderId=<id>&ref=<orderRef>` (or
`/cancel`). Both routes render `theme.pages.CheckoutResult` with `outcome`, which re-fetches the real order status
(`useOrderStatus`, sending `ref`) rather than trusting the URL. `ref` is the guest's credential for that read: the
service answers 404 for any id without it, so a page that drops it breaks every guest return.

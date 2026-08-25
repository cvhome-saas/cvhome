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
│   │                                                useProductListing, useProductPurchase, useSearch
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
2. `storefront/src/proxy.ts`: no `Store-Id` → `/store-not-found` (404); `/` → `/{lang}`; next-intl routing;
   dev-only `?theme=<id>` override cookie.
3. `getTheme()` (`src/shell/theme/get-theme.ts`): override cookie → `theme` header → `STOREFRONT_THEME` →
   registry hit, else `legacy-theme-map.ts` (every `Theme` enum value, lowercased), else fallback (`starter`).
4. `registry.ts` is a static map of dynamic imports — each theme is its own server chunk; only the rendered
   theme's client components reach the browser.
5. The root layout (`app/(storefront)/[locale]/layout.tsx`) fetches store + categories + pages + announcement
   in parallel, derives the merchant colour-role tokens from the `Color-Theme` preset
   (`libs/theme/src/merchant-bridge.ts`, contrast-guarded) and renders
   `<html data-theme=<id> data-color-scheme style="--primary:…" class="<next/font vars>">` →
   `theme.layout.Root` → page → `theme.pages.X`.
6. CSS: one Tailwind build. `globals.css` maps tokens → utilities once (`@theme inline`); `themes.css`
   (generated) holds a `@source` + `tokens.css` import per theme; theme tokens are scoped to
   `[data-theme="<id>"]`. The stock Tailwind palette is removed — colour is role-based only.

Pages in the shell do loading + metadata only (`Suspense` with the theme's skeleton, `notFound()` on 404,
`error.tsx` → the theme's `ErrorState`). Composition is owned by the theme.

## Build / run

```
npm run build      # build:libs (types → services → hooks) then next build (standalone output)
npm run dev        # build:libs then next dev -p 8110 --turbopack
npm run lint | typecheck
npm test --workspace=libs/theme     # colour bridge tests
npm run new-theme <id>
```
`./extra/scripts/run-lcl.sh` starts it as before (prep builds the libs, then `npm run dev`). Docker: the
image copies `storefront/.next/standalone` (build on host/CI first — see `docker.sh`).

**Local dev URLs.** The storefront needs the store headers spg injects, so the supported dev URL is through spg:
`http://org1-store1.spg-507f1f77.gateway.com/en?theme=<id>` (stack up via `run-lcl.sh`). Hitting `http://localhost:8110/en`
directly works for SSR only because the proxy falls back to `FALLBACK_STORE_ID` (env, then the demo-store constant) —
but browser-side calls (cart, listing, auth) go to `/catalog`, `/checkout`, … on the same origin, which only spg routes;
set `EXTERNAL_SPG=http://spg-507f1f77.gateway.com` if you must use localhost. `?theme=<id>` sets a dev-only override
cookie (`?theme=` clears it); unknown ids resolve through the legacy map to the fallback theme.

## Adding a theme

**➡️ `new-landing-ui-template.md`** — scaffold, impeccable design flow, contract checklist, verification.
Direction briefs live in `themes/README.md`.

## Non-negotiable conventions in theme code

- No literal UI text — `t()` and keys in **all 5** locale files.
- No hard-coded colours (they don't compile); role tokens only. Fonts/radius/spacing through `tokens.css`.
- RTL-safe: logical utilities, `DrawerContent side="start|end"`, Swiper `dir={useDir()}`.
- No `@/` imports in themes/libs (ESLint). Primitives from `@store-front/ui`, never forked.
- Behaviour from `@store-front/hooks`; every state (loading/empty/error/not-found) rendered.

## Checkout redirect flow

After payment the gateway returns to `{domain}/{locale}/checkout/success?code=&orderId=` (or `/cancel`).
Both routes render `theme.pages.CheckoutResult` with `outcome`, which re-fetches the real order status
(`useOrderStatus`) rather than trusting the URL.

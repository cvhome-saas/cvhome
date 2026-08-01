# `store-pod/landing-ui` — storefront and template system

The customer-facing storefront (port **8110**). Next.js 16 / React 19, TypeScript, Tailwind + shadcn/ui,
`next-intl` for i18n. It is itself an **npm workspaces monorepo**, wrapped by a `build.gradle` so the root
Gradle build can build and containerize it.

## Workspace layout

```
store-pod/landing-ui/
├── package.json           name "store-front", workspaces: ["app", "libs/*", "templates/*"]
├── app/                   the Express server that selects and serves a template
│   └── src/
│       ├── server.ts            Express entry
│       ├── template-manager.ts  picks the template folder per request
│       └── instrumentation.ts   OpenTelemetry
├── libs/                  shared business logic — imported by EVERY template
│   ├── types/             @store-front/types      — Store, Product, Category, Cart, User, StoreContext, Theme enum
│   ├── services/          @store-front/services   — server-side API clients + utils
│   └── hooks/             @store-front/hooks      — client-side React hooks
├── templates/             one full Next.js app per visual theme
│   └── basis/  modern/  beauty/  jewelery/
├── locales/               SHARED across all templates: en, ar, es, fr, ru
├── Dockerfile, docker.sh, scripts.sh
└── build.gradle           ui-conventions + node plugin
```

`app` is **not** a Next.js app — it's a thin Express server. Each `templates/<name>` is the actual Next.js app.

## How theming works end to end

1. A store admin assigns a `Theme` enum value (e.g. `BEAUTY`) to their store.
2. The Express server reads the `theme` HTTP header on each request.
3. `getTheme()` lowercases it → `"beauty"`.
4. `TemplateManager` looks for `templates/beauty/` and serves that app.

**The template folder name must be the lowercase of the `Theme` enum value.** `BEAUTY` → `templates/beauty/`.

Registration is almost entirely convention-driven: the `templates/*` workspace glob auto-discovers new folders,
`TemplateManager` discovers by folder existence, and the Dockerfile copies the whole `templates/` directory. The
**only** explicit registration is adding the value to the `Theme` enum in `libs/types/src/store.ts`.

On top of the template choice, each store injects its own **color palette at runtime**: `layout.tsx` renders a
`<style>` tag built by `toRootStyle()` from `@store-front/services`, overriding the CSS variables the template
declared in `globals.css`.

## Build order

The root `package.json` build script is strictly ordered — libs first, because templates and app consume their
compiled `dist`:

```
npm run build
  = build:libs-types → build:libs-services → build:libs-hooks → build:templates → build:app
```

Dev options:
- Single template: `cd templates/<name> && npm run dev` (Next dev on 8110, turbopack)
- Full stack via Express (theme-header routing): `npm install && npm run build && npm run dev` at
  `landing-ui/` root

## Adding a new template

**➡️ Follow `new-landing-ui-template.md` (in this same `references/` folder).** It is the authoritative
step-by-step procedure with a complete file-by-file checklist — work through it rather than improvising.

Summary of what it prescribes, so you know what you're in for:

**Copy as-is (do not modify):** `next.config.ts`, `tsconfig.json` (aliases must point at `../../libs/*`),
`postcss.config.mjs`, `eslint.config.mjs`, `components.json`, `src/proxy.ts`, all four `src/i18n/*` files,
`src/lib/utils.ts`, all shadcn primitives in `src/components/ui/` (~22 files), and **every page** under
`src/app/[locale]/` — pages are server components that fetch data and pass it down as props, so they carry no
visual logic.

**Customize:** `public/css/login.css` (brand styles for auth pages) and `src/app/[locale]/globals.css` (the
palette/font/radius CSS variables — noting the store's runtime colors will override them).

**Design from scratch — the actual work (19 files under `src/shared/`):** `Layout/{Header,Footer}`,
`SlideShow/{CoverFlow,swiper-custom.css}`, `ProductGrid/{ProductGrid,ProductSwiperGrid}`,
`ProductItem/ProductItem`, `ProductDetails/{ProductDetails,ProductDetailedActionBox,ProductDetailsImageGallery}`,
`Category/ProductCategoryFilter`, `Cart/CartProductList`, `Checkout/{CheckoutForm,CheckoutResult}`,
`Customer/{CustomerDashboard,OrderDetails}`, `Common/{Breadcrumb,SectionTitle,Secured}`.

These receive the same props as `basis`/`modern` — only JSX and Tailwind classes may differ. The props/hooks
contract must not break:

| Component | Hook |
|---|---|
| `Header` | `useCart`, `useUser` |
| `CartProductList` | `useCart` |
| `ProductCategoryFilter` | `useProductCategoryFilter` |
| `CheckoutForm` | `useCheckoutForm` |
| `CheckoutCartBox` | `useCart` |
| `CheckoutResult` | `useOrderStatus` |
| `ProductDetailedActionBox` | `useProductDetailedAddToCart` |
| `CustomerDashboard`, `OrderDetails` | `useUser`, `useCustomer` |
| `ProductGrid`, `ProductItem`, `ProductSwiperGrid`, `CoverFlow` | props only |

## Shared library imports

```typescript
import type { Store, Product, Category, Cart, User, StoreContext } from '@store-front/types'

// services — server components / pages
import { ProductService }  from '@store-front/services/product-service'
import { CategoryService } from '@store-front/services/category-service'
import { StoreService }    from '@store-front/services/store-service'
import { ContentService }  from '@store-front/services/content-service'
import { OrderService }    from '@store-front/services/order-service'
import { AuthService }     from '@store-front/services/auth-service'
import { toRootStyle }     from '@store-front/services/color-utils'
import { getDirection }    from '@store-front/services/direction-utils'

// hooks — client components only ('use client')
import { useCart, useUser, useCustomer } from '@store-front/hooks/...'
import { useCheckoutForm, useOrderStatus } from '@store-front/hooks/...'
import { useProductCategoryFilter, useProductDetailedAddToCart } from '@store-front/hooks/...'
```

## Non-negotiable conventions when writing template code

- **No hardcoded text.** Add keys to **all 5** files in `landing-ui/locales/` and use `t()` —
  `useTranslations('NS')` in client components, `getTranslations('NS')` in server components.
- **No hardcoded colors.** Use variable-backed Tailwind classes (`bg-primary`, `text-foreground`,
  `border-border`, `hover:bg-primary-hover`), never `bg-blue-500` — runtime injection would override them.
- **RTL-safe.** Logical properties only: `ps-*`/`pe-*`, `ms-*`/`me-*`, `text-start`/`text-end`,
  `rounded-s-*`/`rounded-e-*`. Test against `/ar/`.
- **Secure `/customer/**`** with `useUser` and redirect unauthenticated visitors to login.
- **Mobile-first**, Tailwind breakpoints; header needs a mobile drawer; grids 1 → 2–3 → 3–4 columns.
- **Approved libraries only:** `lucide-react` (icons), `swiper` (carousels), `nextjs-toast-notify` (toasts),
  `react-hook-form` + `yup` (forms), shadcn/Radix (primitives).
- **Product data edge cases:** `object-cover` in fixed-ratio containers, `line-clamp-2` for long names,
  `placeholder.png` fallback, "Out of Stock" badge + disabled add-to-cart at zero stock, and prices arrive
  pre-formatted from the API — display as-is.

## Checkout redirect flow

After payment the gateway returns the shopper to
`{domain}/{locale}/checkout/success?code={cartCode}&orderId={id}` (or `.../cancel?...`); those query params are
set server-side by the checkout API's `successUrl`/`cancelUrl`. Both routes render the *same* `CheckoutResult`
component, which reads `code`/`orderId` via `useSearchParams()` and fetches the real order status from the API
rather than trusting which URL the browser landed on.

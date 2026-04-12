# New Template Creation Guide

## Context

The project is a Next.js 16 e-commerce monorepo under `store-pod/landing-ui/`.
Two existing Template (`basis`, `modern`) live in `templates/` and share all business logic
via three workspace packages in `libs/`. A new template is a **full Next.js app** that reuses
the shared libs and only differs in visual components.

---

## Step-by-Step: Creating a New Template (e.g. `health`)

### Step 1 — Scaffold the App

```
store-pod/landing-ui/templates/health/
```

Copy the folder structure of an existing template (e.g. `basis`). Every file below must exist.

---

### Step 2 — Configuration Files (5 files, no logic changes needed)

| File | Action |
|------|--------|
| `package.json` | Copy from `basis/package.json`. Change `name` to `health` (or keep blank). |
| `next.config.ts` | Copy as-is — do not change. |
| `tsconfig.json` | Copy as-is — path aliases must point to `../../libs/*`. |
| `postcss.config.mjs` | Copy as-is. |
| `eslint.config.mjs` | Copy as-is. |
| `components.json` | Copy as-is (shadcn config). |
| `public/` | Copy entire directory — contains `css/login.css` (login page styles), `placeholder.png` (fallback product image), and SVG assets. |

---

### Step 3 — i18n Setup (4 files copied, locales are shared)

```
src/i18n/routing.ts      ← locales list, do not change
src/i18n/request.ts      ← imports from shared locales, do not change
src/i18n/navigation.ts   ← do not change
src/proxy.ts             ← middleware, do not change
```

**Locales are SHARED** across all templates. They live at `landing-ui/locales/` (the monorepo root), not inside your template folder. The `request.ts` file imports them via `../../../../locales/${locale}.json`.

There are **5** locale files: `en.json`, `ar.json`, `es.json`, `fr.json`, `ru.json`.

**IMPORTANT:** If you add any new literal text in your components (e.g., "Exclusive Offer", "Sign up now"), **DO NOT hardcode it**. Add the key/value pairs to `landing-ui/locales/*.json` (all 5 files) and use the `t('key')` function from `next-intl`.

---

### Step 4 — Global Styles (1 file, design-only changes)

```
src/app/[locale]/globals.css
```

- Copy from `basis`.
- Change the CSS variable default values in `:root` for your template's palette.
- You can change fonts (`@import`), spacing tokens, or border-radius variables here.
- The color variables injected at runtime from store config will **override** these defaults.

---

### Step 5 — Pages (copy as-is, no visual logic here)

All pages are **server components** — they fetch data and pass it as props. Do not change them.

```
src/app/[locale]/layout.tsx                ← copy, no changes
src/app/[locale]/page.tsx                  ← copy, no changes
src/app/[locale]/login/page.tsx            ← copy, no changes
src/app/[locale]/login/login-client.tsx    ← copy, no changes
src/app/[locale]/callback/page.tsx         ← copy, no changes
src/app/[locale]/callback/callback-client.tsx  ← copy, no changes
src/app/[locale]/product/[url]/page.tsx    ← copy, no changes
src/app/[locale]/category/[url]/page.tsx   ← copy, no changes
src/app/[locale]/checkout/page.tsx         ← copy, no changes
src/app/[locale]/content/[url]/page.tsx    ← copy, no changes
src/app/[locale]/customer/page.tsx         ← copy, no changes
src/app/[locale]/customer/order/[id]/page.tsx ← copy, no changes
src/app/[locale]/favicon.ico              ← copy or replace with your own
```

> `layout.tsx` injects the store's runtime color template via `<style>` tag using `toRootStyle()` from `@store-front/services`. Do not change this.

---

### Step 6 — shadcn UI Primitives (copy as-is or regenerate with shadcn CLI)

These are low-level Radix UI wrappers. Copy from `basis/src/components/ui/`:

```
accordion.tsx, alert-dialog.tsx, badge.tsx, breadcrumb.tsx,
button.tsx, card.tsx, checkbox.tsx, dialog.tsx, dropdown-menu.tsx,
form.tsx, input.tsx, label.tsx, navigation-menu.tsx, radio-group.tsx,
scroll-area.tsx, select.tsx, separator.tsx, sheet.tsx, textarea.tsx, 
tooltip.tsx, tabs.tsx, table.tsx
```

You can regenerate any of these via `npx shadcn add <component>` if you want a different variant.

---

### Step 7 — Shared Components (THE BRAND NEW DESIGN — 18 files)

These are the files you actually design from scratch for your new template. This is NOT a copy-paste job; it should be a **completely brand new design** for this template. They all receive the same props as basis/modern — only the JSX and Tailwind classes differ.

```
src/shared/
  Layout/
    Header.tsx                  ← navbar, logo, language, cart icon, auth button
    Footer.tsx                  ← footer links, social icons, copyright

  SlideShow/
    CoverFlow.tsx               ← homepage hero/banner carousel
    swiper-custom.css           ← custom swiper CSS overrides

  ProductGrid/
    ProductGrid.tsx             ← static product grid layout
    ProductSwiperGrid.tsx       ← swipeable product carousel

  ProductItem/
    ProductItem.tsx             ← single product card (image, name, price, add-to-cart)

  ProductDetails/
    ProductDetails.tsx          ← full product detail layout
    ProductDetailedActionBox.tsx ← quantity selector + add-to-cart button
    ProductDetailsImageGallery.tsx ← product image gallery/zoom

  Category/
    ProductCategoryFilter.tsx   ← sidebar/filter UI + product listing

  Cart/
    CartProductList.tsx         ← cart sidebar/sheet item list

  Checkout/
    CheckoutForm.tsx            ← checkout form (CheckoutForm) + cart summary sidebar (CheckoutCartBox)

  Customer/
    CustomerDashboard.tsx       ← Tabs for Customer Info, Addresses, and Orders List
    OrderDetails.tsx            ← Order products, total, status, and history timeline

  Common/
    Breadcrumb.tsx              ← page breadcrumb trail
    SectionTitle.tsx            ← section heading component
    Secured.tsx                 ← securing page filter component
```

#### Props Contract for each component (do not break these):

Each component receives typed props from the page server components. The hooks used are:

| Component | Hook / Service |
|-----------|---------------|
| `Header` | `useCart`, `useUser` |
| `CartProductList` | `useCart` |
| `ProductCategoryFilter` | `useProductCategoryFilter` |
| `CheckoutForm` | `useCheckoutForm` |
| `CheckoutCartBox` | `useCart` |
| `ProductDetailedActionBox` | `useProductDetailedAddToCart` |
| `CustomerDashboard` | `useUser`, `useCustomer` |
| `OrderDetails` | `useUser`, `useCustomer` |
| `ProductGrid` / `ProductItem` | Props only (no hooks) |
| `ProductSwiperGrid` | Props only (Swiper.js) |
| `CoverFlow` | Props only (Swiper.js) |

---

### Step 8 — Secured pages

all /customer, /customer/* should be secured access `useUser` and redirect to login if not authenticated.

### Step 9 — Utilities (1 file, copy as-is)

```
src/lib/utils.ts    ← cn() classname helper, do not change
```

---

### Step 10 — Register the Theme Enum

Add your new template name to the `Theme` enum in the shared types package:

**File:** `store-pod/landing-ui/libs/types/src/store.ts`

```typescript
export enum Theme {
    DEFAULT = 'DEFAULT',
    // ...
    HEALTH = 'HEALTH',
    BEAUTY = 'BEAUTY',   // ← add your new theme here
}
```

**How it works end-to-end:**
1. Store admin assigns a `Theme` enum value (e.g., `BEAUTY`) to a store
2. The Express server reads the `theme` HTTP header from each request
3. `getTheme()` lowercases it → `"beauty"`
4. `TemplateManager` looks for `templates/beauty/` folder and serves it

**Your template folder name MUST be the lowercase version of the Theme enum value.**
For example: `BEAUTY` → `templates/beauty/`, `HEALTH` → `templates/health/`.

**No other registration is needed.** The npm workspace glob (`templates/*` in root `package.json`) auto-discovers new folders. The Express `TemplateManager` auto-discovers templates by folder existence. The Dockerfile copies the entire `templates/` directory. No env vars are needed per template.

---

### Step 11 — Install Dependencies & Run

```bash
# Option A: Run just your template in dev mode
cd store-pod/landing-ui/templates/health
npm install
npm run dev          # starts Next.js on port 8110

# Option B: Run via the Express server (serves all templates, uses theme header routing)
cd store-pod/landing-ui
npm install          # installs all workspaces
npm run build        # builds libs → templates → app
npm run dev          # starts Express server on port 8110
```

---

## Summary Checklist

```
— Config (copy) —
[ ] package.json (change name only)
[ ] next.config.ts (copied)
[ ] tsconfig.json (copied, verify lib paths)
[ ] postcss.config.mjs (copied)
[ ] eslint.config.mjs (copied)
[ ] components.json (copied)
[ ] public/ (copy entire directory — login.css, placeholder.png, SVGs)

— i18n (copy) —
[ ] src/proxy.ts (copied)
[ ] src/i18n/routing.ts (copied)
[ ] src/i18n/request.ts (copied)
[ ] src/i18n/navigation.ts (copied)

— Styles (customize) —
[ ] src/app/[locale]/globals.css (customize palette, fonts)

— Pages (copy, no changes) —
[ ] src/app/[locale]/layout.tsx
[ ] src/app/[locale]/page.tsx
[ ] src/app/[locale]/login/page.tsx
[ ] src/app/[locale]/callback/page.tsx
[ ] src/app/[locale]/product/[url]/page.tsx
[ ] src/app/[locale]/category/[url]/page.tsx
[ ] src/app/[locale]/checkout/page.tsx
[ ] src/app/[locale]/content/[url]/page.tsx
[ ] src/app/[locale]/customer/page.tsx
[ ] src/app/[locale]/customer/order/[id]/page.tsx

— UI primitives (copy or regenerate) —
[ ] src/components/ui/* (22 files)

— Utilities (copy) —
[ ] src/lib/utils.ts

— Shared components (DESIGN from scratch) —
[ ] src/shared/Layout/Header.tsx
[ ] src/shared/Layout/Footer.tsx
[ ] src/shared/SlideShow/CoverFlow.tsx
[ ] src/shared/SlideShow/swiper-custom.css
[ ] src/shared/ProductGrid/ProductGrid.tsx
[ ] src/shared/ProductGrid/ProductSwiperGrid.tsx
[ ] src/shared/ProductItem/ProductItem.tsx
[ ] src/shared/ProductDetails/ProductDetails.tsx
[ ] src/shared/ProductDetails/ProductDetailedActionBox.tsx
[ ] src/shared/ProductDetails/ProductDetailsImageGallery.tsx
[ ] src/shared/Category/ProductCategoryFilter.tsx
[ ] src/shared/Cart/CartProductList.tsx
[ ] src/shared/Checkout/CheckoutForm.tsx
[ ] src/shared/Customer/CustomerDashboard.tsx
[ ] src/shared/Customer/OrderDetails.tsx
[ ] src/shared/Common/Breadcrumb.tsx
[ ] src/shared/Common/SectionTitle.tsx

— Shared types (one-line change) —
[ ] libs/types/src/store.ts → add new value to Theme enum

— Shared locales (add new keys if needed) —
[ ] landing-ui/locales/en.json (+ ar, es, fr, ru) → add any new literal text
```

---

## Key Shared Library Imports to Know

```typescript
// Types
import type { Store, Product, Category, Cart, User, StoreContext } from '@store-front/types'

// Services (used in server components / pages)
import { ProductService } from '@store-front/services/product-service'
import { CategoryService } from '@store-front/services/category-service'
import { StoreService } from '@store-front/services/store-service'
import { ContentService } from '@store-front/services/content-service'
import { AuthService } from '@store-front/services/auth-service'
import { toRootStyle } from '@store-front/services/color-utils'
import { getDirection } from '@store-front/services/direction-utils'

// Hooks (used in client components only — 'use client')
import { useCart } from '@store-front/hooks/use-cart'
import { useUser } from '@store-front/hooks/use-user'
import { useCustomer } from '@store-front/hooks/use-customer'
import { useCheckoutForm } from '@store-front/hooks/use-checkout-form'
import { useProductCategoryFilter } from '@store-front/hooks/use-product-category-filter'
import { useProductDetailedAddToCart } from '@store-front/hooks/use-product-detailed-add-to-cart'
```

---

## Design Guidelines

When designing the 14 shared components for your new template, follow these rules:

### RTL Support
- Use logical properties: `start`/`end` instead of `left`/`right` in Tailwind (`ps-4`, `pe-4`, `ms-auto`, `me-2`, `text-start`, `text-end`, `rounded-s-lg`, `rounded-e-lg`).
- Use `gap-*` and flexbox instead of directional margins where possible.
- Always test with Arabic locale (`/ar/`) to verify RTL layout flips correctly.

### Colors — Use CSS Variables Only
- Always use Tailwind classes that reference CSS variables: `bg-primary`, `text-foreground`, `border-border`, `bg-accent`, `text-muted-foreground`, etc.
- **Never** hardcode colors like `bg-blue-500` or `text-red-600` — the store's color theme is injected at runtime via `toRootStyle()` and will override CSS variables.
- For hover/focus states use the corresponding variables: `hover:bg-primary-hover`, `focus:ring-ring`, etc.

### Translations — No Hardcoded Text
- Use `useTranslations('NAMESPACE')` in client components and `getTranslations('NAMESPACE')` in server components.
- Add new keys to **all 5** locale files in `landing-ui/locales/` (`en.json`, `ar.json`, `es.json`, `fr.json`, `ru.json`).
- Existing translation keys cover most common text (see `en.json` for full structure: `PAGE.*`, `COMPONENTS.*`).

### Responsive Design
- Follow Tailwind's breakpoints: `sm` (640px) → `md` (768px) → `lg` (1024px) → `xl` (1280px).
- Design mobile-first. Test: mobile viewport, tablet, and desktop.
- Header must include a hamburger/drawer menu for mobile.
- Product grids should adapt columns: 1 col on mobile, 2–3 on tablet, 3–4 on desktop.

### Product Data Edge Cases
- **Varying image ratios**: Use `object-cover` with a fixed aspect ratio container.
- **Long product names**: Allow 2–3 lines with `line-clamp-2` or `truncate`.
- **Missing images**: Fallback to `placeholder.png` from the `public/` directory.
- **Zero stock**: Show "Out of Stock" badge, disable "Add to Cart" button.
- **Currencies**: Prices come pre-formatted from the API — display as-is, don't format manually.

### Libraries Available (already in dependencies)
- **Icons**: `lucide-react` — use this for all icons. Don't import other icon libraries.
- **Carousel/Slider**: `swiper` (Swiper.js) — used by CoverFlow and ProductSwiperGrid. Keep custom CSS in `swiper-custom.css`.
- **Toast/Notifications**: `nextjs-toast-notify` — for add-to-cart success, error messages, etc.
- **Form validation**: `react-hook-form` + `yup` — used by CheckoutForm via `useCheckoutForm` hook.
- **UI primitives**: shadcn/ui (Radix UI) — all available in `components/ui/`.

### Accessibility
- Use semantic HTML: `<nav>`, `<main>`, `<article>`, `<header>`, `<footer>`.
- Add `aria-label` to icon-only buttons (e.g., cart icon, close button).
- Ensure color contrast meets WCAG AA standards.


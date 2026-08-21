# starter theme

Plain, complete reference implementation of `ThemeDefinition` (`@store-front/theme`). Copy it with
`npm run new-theme <name>` and redesign; do not "improve" starter itself beyond contract fixes.

## What each file proves

| File | Proves |
|---|---|
| `src/index.ts` | `defineTheme()` contract — build fails if a page/state is missing |
| `src/fonts.ts` | `next/font` wiring from a theme package (variable class on `<html>`) |
| `src/tokens.css` | token scoping under `[data-theme="starter"]`; every `THEME_OWNED_TOKENS` entry set |
| `src/config.ts` | `ThemeLayoutConfig` consumed by the shell (`ctx.layout`) |
| `src/layout/Root.tsx` | skip link, announcement, header, `<main id="main">`, footer |
| `src/layout/Header*.tsx`, `Nav.tsx`, `MobileNav.tsx` | category tree nav (mega-menu + drawer), `start/end` logical props, `Drawer` side by `dir` |
| `src/layout/HeaderActions.tsx` | locale switcher, account (login/logout), cart button + cart drawer (qty stepper, remove, empty state, checkout CTA) |
| `src/layout/Announcement.tsx` | `header-message` box rendered + dismissable |
| `src/pages/*` | full-page composition ownership with the typed `PageProps<XData>` contracts |
| `src/sections/Hero.tsx`, `ProductRail.tsx` | Swiper with `dir`, slides from `store.sliderImages` |
| `src/sections/Listing.tsx` | `useProductListing`: sort, pagination, manufacturer/option facets, URL sync, loading/empty/error states |
| `src/sections/BuyBox.tsx` | `useProductPurchase`: option/variant selector, stock clamp, quantity stepper, add to cart |
| `src/sections/Gallery.tsx` | variant-aware image gallery, placeholder fallback |
| `src/sections/CheckoutForm.tsx` | `useCheckoutForm` with `libs/ui` form primitives, dialogs |
| `src/sections/SearchBox.tsx` | branches on `data.search` capabilities (suggestions-only today) |
| `src/components/ProductCard.tsx` | placeholder image, sale + out-of-stock badges, price pair, clamp, quick add |
| `src/states/*` | skeleton per page, error (client), not-found per kind, empty per kind, redirecting |

## Dev

```
npm run dev                         # storefront on :8110
open http://localhost:8110/en?theme=starter   # override cookie (dev only)
STOREFRONT_THEME=starter npm run dev          # env override
```

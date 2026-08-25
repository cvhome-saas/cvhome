# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

Shoppers of small and medium merchants on a multi-tenant e-commerce SaaS. They arrive on a merchant's own
domain (desktop and mobile, often from social or search), in one of five locales (en, ar, es, fr, ru — Arabic
is right-to-left), to browse a catalogue, compare a few products, and buy. Merchants (who configure the store
in the seller console) are a secondary audience: they choose a theme and a colour preset and expect their
logo, slider images, social links and CMS pages to appear.

## Product Purpose

The storefront is the customer-facing shop rendered for every merchant on the platform: home, category
listing, product detail, cart, checkout, post-payment result, customer account and orders, CMS pages. Success
is a shopper finding and buying without friction on any device and in any of the supported locales, and a
merchant recognising their brand in it. _(inferred from the codebase and the requirement brief)_

## Positioning

One platform, genuinely distinct storefront identities: a theme controls structure, typography, spacing,
imagery treatment and composition — not just colours — while every theme shares the same tested behaviour
(cart, variants, checkout, account) and honours the merchant's colour preset through a contrast-guarded
bridge. _(inferred)_

## Operating Context

- Reached through the pod gateway (spg/Caddy), which resolves the domain to a store and injects the
  `Store-Id`, `Theme`, `Color-Theme`, `Default-Language`, `Supported-Languages` headers.
- Shoppers authenticate with the `cua` authorization server (OAuth2 PKCE, redirect to `/login`, back to
  `/callback`); `cua`'s own login/registration pages link `/css/login.css` served by the storefront.
- Payment gateways redirect back to `/checkout/success` or `/checkout/cancel` with `code` and `orderId`;
  the real status is re-read from the API.
- Content (meta title/description, header announcement, agreement text, information pages) comes from the
  merchant's CMS boxes/pages.

## Capabilities and Constraints

Available data/behaviour: product groups for the home page (featured, newly added, home page, recommended);
category tree with product counts; products with images, pre-formatted prices (final/original, discounted
flag), stock, manufacturer, options/variants (per-variant SKU, images, price, stock), attributes; related
products; manufacturer and option-value facets; sort by newest/oldest (catalog `Pageable`); cart with
quantity edit; checkout with COD / Stripe / PayPal / manual transfer; customer profile, addresses, orders and
order history; five locales; merchant logo, banner, slider images, social links, address/email/phone.

Constraints (do not fabricate): **no text search endpoint** (search UI is suggestions-only over categories
and pages until the backend adds one); **no reviews/ratings API** (rating fields exist but cannot be
written or listed); **no wishlist**; **no promotions/coupon API**; prices are formatted by the API — never
reformat; images are served unoptimised; category has no image field; sort by price/name is not supported
by the catalog.

## Brand Commitments

The merchant's logo and `ColorTheme` preset must be honoured (a theme may re-map roles via
`tokens.mapMerchantColors`, never ignore the preset). Merchant-supplied imagery (slider, products) is the
primary visual material; a theme must look finished with a single image and with none.

## Evidence on Hand

Demo stores seeded by the `test-stores` profile (`org1-store1.spg-507f1f77.gateway.com`, shopper
`user`/`revo`); local stack via `lcl start -d`. No customer testimonials, case studies or
benchmarks exist — do not invent them.

## Product Principles

1. Behaviour is shared, identity is not: themes compose, they never re-implement cart/checkout/account.
2. Honest surfaces: only show what the platform can do (no fake search, reviews or promotions).
3. Mobile and RTL are first-class, not adaptations.
4. Every state is designed: loading, empty, error, not-found, out-of-stock, sale.
5. The merchant's brand wins: their colours, their images, their content — the theme frames them.

## Accessibility & Inclusion

WCAG AA contrast is enforced programmatically for every colour role (`libs/theme`); icon-only controls are
labelled; keyboard focus is visible; Arabic right-to-left layout must mirror correctly.

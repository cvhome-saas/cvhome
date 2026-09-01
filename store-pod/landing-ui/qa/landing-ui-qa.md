# QA — landing-ui (`store-pod/landing-ui`)

landing-ui is the storefront: the Next.js app a shopper actually sees. It renders what
[content](../../content/content-service/qa/content-qa.md) writes, what
[catalog](../../catalog/catalog-service/qa/catalog-qa.md) defines and
[inventory](../../inventory/inventory-service/qa/inventory-qa.md) prices, in the store's own theme, locale and
text direction, behind the pod's edge.

- **Scope** — the home page, category and product pages, the blog/help/policy routes, the storefront's use of
  the content read API, themes and templates, locale and RTL, and how the page behaves when a service behind it
  is down
- **Runs on** — `lcl start -d --stack <name>` (`npm run dev` alone is not enough — it needs the backend).
  Always reach it through the edge at `http://<store>.spg-507f1f77.gateway.com`; read the live port from
  `lcl urls`
- **Cases** — 20 (9 verified, 0 unit only, 11 not verified)
- **Also see** — [spg](../../spg/qa/spg-qa.md) (the edge in front of it), content, catalog, inventory,
  [checkout](../../checkout/checkout-service/qa/checkout-qa.md),
  [cua](../../cua/qa/cua-qa.md) (shopper login)

Each case is tagged:

- **[verified]** — run against a running stack and passed.
- **[unit only]** — covered by the named test; nobody drove it through the stack.
- **[not verified]** — never run end to end by anyone.

**Prefixes name where the case came from**, because three source documents each called their storefront section
`SF`: `SF` is the catalogue storefront, `LUI` the content storefront, `STR` the store's own identity, `PRC` the
public price list.

---

## 00 — Before you start

**Shared prerequisites** — starting the stack, the demo logins, the seeded org/store/pod ids and the four demo
storefronts are in
[`references/qa-testing.md`](../../../.claude/skills/project-structure/references/qa-testing.md) §§1–5. Only
what is specific to the storefront is below.

> **Always reach the storefront through the spg host.** Hitting landing-ui's own port directly makes
> `FALLBACK_STORE_ID` answer for every hostname, so every store looks like the same store. This is the cause of
> most "why is this the wrong store" reports.

**Build it from the workspace root**, not from `storefront/`: `npm run build` at `store-pod/landing-ui` chains
libs → templates → app, and building `app` alone compiles against stale types.

**Broken images are expected locally** — MinIO runs without a volume. Content's `## 00` has the recipe that
repopulates it; without that, every product photo 404s and that is the local stack, not a defect.

Logs: `.lcl/<stack>/logs/landing-ui.log`.

---

## SF — The catalogue storefront

_From `qa/catalog-and-inventory.md` §SF and §PDP._



### SF-01 — Home strips carry prices from inventory · critical · [verified]

- **Steps** — open `/en` (or `/ar` — org1-store1's default is Arabic).
- **Expect** — 200; the four strips render products with names, images and prices. The price is fetched in
  bulk from inventory for each strip's skus (`InventoryService.enrichProducts`); a strip whose group 404s is
  simply absent.

### SF-02 — Category page: listing, facets, sort · critical · [verified] (page) / [not verified] (facets, sort)

- **Steps** — `/en/category/men`; filter by brand; sort newest.
- **Expect** — 200 with the subtree's products and prices; the brand facet is BRD-04's list; sort sends
  `sort=dateAvailable,desc`; the "variants" facet group is **never** rendered (always empty since the split).

### SF-03 — Product page without related items, without inventory · high · [verified] / [not verified]

- **Steps** — a product with no `RELATED_ITEM` group (all seeded ones); then stop inventory and reload.
- **Expect** — the page renders without the related strip (verified); without inventory it renders with no
  price and add-to-cart disabled — the product itself **must not** fail because a strip did.

### SF-04 — Unknown slugs · [verified]

- **Steps** — `/en/product/does-not-exist`.
- **Expect** — the catalog answers 404; the Next dev server currently renders a **500** for it (a pre-existing
  stream error in the dev server, `.lcl/default/logs/landing-ui.log`, `controller[kState].transformAlgorithm is
  not a function`). Not a catalog defect; listed so it is not filed as one.

---

### SF-05 — The storefront page renders it, with the price from inventory · critical · [verified]

- **Steps** — open `http://org1-store1.spg-507f1f77.gateway.com/en/product/nike-zoomx-invincible-run-3`.
- **Expect** — 200, the product name, `SAR 750.00` (or whatever INV state you left — the price is *not* in
  the catalog payload, landing-ui fetches it from inventory by sku and formats it), quantity and an enabled
  add-to-cart. With inventory stopped the page still renders, without a price and with add-to-cart disabled.

---

- _Was PDP-05 in `qa/catalog-and-inventory.md`._

---

## LUI — The storefront itself

### LUI-01 — The home page renders content from the new API · critical · [verified]

- **Steps** — open `http://org1-store1.spg-507f1f77.gateway.com`.
- **Expect** — footer pages from the CMS, navigation from the MAIN menu, the announcement bar when a STRIP
  banner or `header-message` box is live. Broken product/logo images after a Docker restart are the known MinIO
  gap.

### LUI-02 — A content page renders with its title · critical · [verified]

- **Steps** — `/en/content/about-us`, then `/ar/content/about-us`.
- **Expect** — heading and body from the CMS, the browser tab title from the meta title, and the Arabic version
  right-to-left with the Arabic font (not Arial-substituted boxes).

### LUI-03 — Blog, help and policy routes exist and behave · high · [not verified]

- **Steps** — `/en/blog`, `/en/blog/<slug>`, `/en/help`, `/en/policies/terms`; then a slug that does not exist
  on each.
- **Expect** — content where there is content, the store's own not-found page where there is not — never an
  unhandled error page or an empty shell.

### LUI-04 — Checkout still shows the agreement · critical · [verified]

This is the one that breaks quietly: the agreement now comes only from the TERMS policy.

- **Steps** — add a product, reach checkout, look for the terms text.
- **Expect** — the LIVE TERMS text for that store, in the shopper's locale. Repeat on **all four** demo stores.

### LUI-05 — The CMS being down does not take the storefront down · high · [not verified]

The site loader degrades to an empty document on purpose; the page loader does not (a page with no content is a
404 by definition).

- **Steps** — stop `content`; open the storefront home, then `/en/content/about-us`.
- **Expect** — the home page still renders products with a plain header and no announcement; the content page
  gives the not-found page. Neither should be a stack trace.

---

---

## STR — The store's own identity

_From `qa/merchant-store-service.md` §SF, renumbered `SF-0N` → `STR-0N` because §SF above is the catalogue
storefront._



### STR-01 — The store's identity renders · critical · [not verified]

- **Steps** — open org1-store1's storefront.
- **Expect** — store name, logo, banner, slider and social links come from the merchant record. Broken images
  on a fresh stack are the MinIO gap; the *names* must still be right in the API response.

### STR-02 — Arabic default language is honoured · high · [not verified]

org1-store1's default language is `ar`.

- **Steps** — open the storefront root with no locale in the path.
- **Expect** — Arabic, right-to-left, with the Arabic font actually applied — a Latin fallback face silently
  drops Arabic glyphs, so check the rendering, not just the direction.

### STR-03 — A store with a custom domain serves the same content · [not verified]

- **Steps** — add a hosts entry for a custom domain you allocated in DOM-05, then open it.
- **Expect** — the same storefront as the subdomain, identified by the same `Store-Id` header.

---

---

## PRC — The public price list and the lapsed store

_From `qa/billing-per-store-subscriptions.md` §UI and §ENF — the two cases the shopper sees._

### PRC-01 — The public price list still works · [not verified]

This page moved to a different backend in this change and was only checked by build, never on screen.

- **Steps** — signed out (a private window is easiest), open the public site and find the pricing section.
- **Expect** — plans and prices appear, matching the console exactly, with the free plan shown separately.
  Monthly and yearly both work.

---

- _Was UI-07._

### ENF-03 — The shopfront of a lapsed store keeps selling · critical · [not verified]

- **Steps** — open the storefront of a suspended store (`http://org1-store1.spg-507f1f77.gateway.com`) and place
  an order.
- **Expect** — browsing and checkout work. Shoppers are never punished for the merchant's billing.

> ENF-03 is deliberate, and the reason `StoreBillingGuardFilter` only guards `/spg/**`: a shopper reaches the
> storefront **by host**, through the pod's edge, and never crosses that filter. A merchant who cannot trade
> cannot earn the money to settle the invoice.

---

## SID — The merged store id, end to end through the storefront

_From `qa/unify-store-id-value-objects.md` §T, reformatted into the case shape used everywhere else._

### SID-01 — The storefront renders end to end · [verified]

_Was T2._

- **Steps** — open `http://org1-store1.spg-507f1f77.gateway.com`.
- **Expect** — navigation, categories and featured products render. That is landing-ui → spg →
  catalog/merchant/content, every hop carrying the merged store id. **Broken images are expected** (no MinIO
  locally).

---

## THM — Themes, locale and direction

The storefront picks its theme from the store record and its locale from the store's supported languages. These
cases have no owner elsewhere and are written from the workspace layout (`libs/*`, `templates/*`) rather than
from a past run.

### THM-01 — Each demo store renders in its own theme · high · [not verified]

- **Steps** — open all four demo storefronts.
- **Expect** — each uses the theme its merchant record names; changing `theme` on the store and reloading
  changes the rendering (merchant UPD-06 asserts the write side).

### THM-02 — An Arabic-first store renders right-to-left · critical · [not verified]

- **Steps** — open `org2-store2` (locales `ar, fr`, Arabic first).
- **Expect** — `dir="rtl"` on the document, the navigation mirrored, prices and dates formatted for the locale,
  and no English seed strings leaking into an Arabic page.

### THM-03 — A locale the store does not support falls back · high · [not verified]

- **Steps** — request a page in a language the store has not enabled.
- **Expect** — it falls back to the store's default and says so where a translation is missing, rather than
  rendering an empty page or a raw key.

### THM-04 — The workspace builds as one · critical · [not verified]

- **Steps** — `cd store-pod/landing-ui && npm run build`.
- **Expect** — libs → templates → app all build. Building `storefront/` alone compiles against stale types and
  is the usual cause of a type error that "does not reproduce".

---

## 99 — Known gaps

**The Next dev server 500s on unknown slugs** instead of rendering a 404 page (SF-04). Dev-only; the production
build renders the 404.

**Sorting the listing by anything but a direct `Product` column is a 500.** `SORT_MAP` here exposes only
`dateAvailable`.

**Broken images everywhere locally** — MinIO has no volume, and the seeded asset URLs hard-code
`http://localhost:9000/<bucket>/…`, so on a `+1000` shifted stack they 404 even after a repopulation. Real
uploads resolve against the configured CDN base and are unaffected.

**`FALLBACK_STORE_ID` makes every host look like the same store** when landing-ui is reached directly rather
than through the edge.

**Product thumbnails are full-size originals** and `next/image` runs unoptimised, so pages are heavier than
they will be once the media service grows derivative sizes.

**A stale browser cart from before a container restart fails every add-to-cart** with
`CHECKOUT.CART.NOT_FOUND` — clear the `cart` keys in `localStorage`.

**No storefront builder.** The page `template` column and the `blocks` placeholders were removed: the console
stored a layout choice, the storefront carried it to the theme contract, and every theme rendered the same
title and prose. A builder would arrive as its own feature rather than as a dormant column.

---

Raise anything unexpected against the landing-ui PR. Include the store **host with its port**, the path, the
browser console, and `.lcl/<stack>/logs/landing-ui.log` — plus which theme and which locale were active.

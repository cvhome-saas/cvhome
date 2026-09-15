# QA — landing-ui (`store-pod/landing-ui`)

landing-ui is the storefront: the Next.js app a shopper actually sees. It renders what
[content](../../content/content-service/qa/content-qa.md) writes, what
[catalog](../../catalog/catalog-service/qa/catalog-qa.md) defines and
[inventory](../../inventory/inventory-service/qa/inventory-qa.md) prices, in the store's own theme, locale and
text direction, behind the pod's edge.

- **Scope** — the home page, category and product pages, the blog/help/policy routes, the storefront's use of
  the content read API, themes and templates, locale and RTL, the shopper's login and registration pages (cua
  is headless — the screens are here), and how the page behaves when a service behind it is down
- **Runs on** — `lcl start -d --stack <name>` (`npm run dev` alone is not enough — it needs the backend).
  Always reach it through the edge at `http://<store>.spg-507f1f77.gateway.com`; read the live port from
  `lcl urls`
- **Cases** — 51 (42 verified, 0 unit only, 17 not verified; 5 cases have split verification tags)
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

### LUI-06 — The checkout result page sends the order ref back · critical · [not verified]

The payment provider returns the shopper to `/{lang}/checkout/success|cancel?orderId=<id>&ref=<orderRef>`. Every
theme's `CheckoutResult` reads both and `useOrderStatus` → `OrderService.getOrderStatus` appends `&ref=` to
`GET /order/{id}/status`; a guest is refused (404) without it. The full case, including the API side, is
checkout's [PLC-13 / SEC-03](../../checkout/checkout-service/qa/checkout-qa.md#plc-13--the-guest-return-page-reads-the-status-with-the-ref--critical--not-verified).

- **Steps** — on a store that allows guest checkout, signed out, pay by card and land on the result page; then
  remove `ref` from the URL and reload.
- **Expect** — the status request carries `ref` and the page shows the paid state; without it the page shows the
  not-found state, not a spinner or an error page. Signed in, the page works with or without `ref`.

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

### THM-04 — The workspace builds as one · critical · [verified]

- **Steps** — `cd store-pod/landing-ui && npm run build`.
- **Expect** — libs → templates → app all build. Building `storefront/` alone compiles against stale types and
  is the usual cause of a type error that "does not reproduce".

### THM-05 — Pink's CJK fonts compile and load without Google fetch races · critical · [verified]

- **Setup** — run landing-ui through the stack, then select the pink theme with `/en?theme=pink` in development.
- **Steps** — load the page from a cold browser context; inspect the console and font requests. In DevTools,
  run `await document.fonts.load('400 16px "M PLUS Rounded 1c"', 'Магазин')` to exercise Cyrillic too.
- **Expect** — the page returns 200 with `data-theme="pink"`; body text resolves to M PLUS Rounded 1c and
  display text to Dela Gothic One; every font comes from `/_next/static/media/` with 200. There is no
  `@vercel/turbopack-next/internal/font/google/font` resolution error and no request to `fonts.gstatic.com` for
  either Japanese family.
- **Seen** — production `npm run build` passed. Browser QA loaded the Latin 400/500/700/800 M PLUS files, Dela
  Gothic One Latin 400 and a forced M PLUS Cyrillic 400 file from the storefront origin, all with 200; the
  browser console was clean.

### THM-06 — A page loads its own theme's CSS and JS, no other theme's · critical · [verified]

- **Why** — each theme has its own route tree (`app/(storefront)/t/<id>/`) and Tailwind entry
  (`app/theme-css/<id>.css`); `proxy.ts` rewrites a store's `/{locale}/…` into its theme's tree. Before, every page
  loaded all twelve themes' stylesheets and client chunks.
- **Setup** — a production build (`npm run build`) behind spg, with `STOREFRONT_THEME_OVERRIDE=true`.
- **Steps** — for each theme, open `/en?theme=<id>`, then its category, product, search (`?q=apple`), checkout,
  login and a content page. List every `<link rel="stylesheet">` and `<script src>` and attribute each file: a CSS
  file carries theme X when it has `[data-theme=X]` rules; a JS chunk carries X when it defines a module under
  `themes/X/` (the module ids are in the build's `*_client-reference-manifest.js`).
- **Expect** — 200 and `data-theme="<id>"` on every page, and no file carrying another theme. Two stylesheets: the
  theme's Tailwind entry first, then its tokens. A theme's Tailwind file has no class only another theme uses:
  `[animation-duration:2.4s]` (pink) appears in pink's file alone, `[--tilt:-0.6deg]` (fashion) in fashion's alone.
- **Seen** — 2026-09-13, local production build at 0.25 CPU / 512 MB behind the load stack's spg: 84 of 84
  page × theme pairs pass. Fashion home: 2 CSS (111 KiB) and 16 JS (1,108 KiB), none of another theme; before:
  13 CSS and 31 JS, 22 of them another theme's. The twelve Tailwind files hold exactly the 1,419 classes the
  single file held.
- **Re-run** — 2026-09-14, on Next 16.3.5: the image on the load stack in CDN mode, with `make page-budget` in
  `../load-testing`.
  - 48 of 48 page × theme pairs (home, category, product, search) pass, and no file carries another theme.
  - Fashion home: 2 CSS (112 KiB) and 15 JS (1,106 KiB).
  - The attribution still reads the build's manifests: 222 theme modules.

### THM-07 — `/t/…` cannot be addressed from outside · high · [verified]

- **Steps** — request `/t/fashion/en`, `/t/pink/en/product/apple-iphone-15-pro` and `/t` through spg.
- **Expect** — 404, empty body. The shopper's URLs stay `/{locale}/…`: the proxy rewrites, it never redirects, so
  `/t/` never shows in the address bar.
- **Seen** — 2026-09-13: 404 for all three (before: a redirect to `/en/t/…`, then a 404).
- **Re-run** — 2026-09-14, on Next 16.3.5 through spg: 404 for all three. The payload and prefetch forms of the same
  URLs are SEC-02, which found the `.rsc` form open until the matcher listed `/t/:path*`.

### THM-08 — `?theme=` switches the tree, and an empty value clears it · high · [verified]

- **Steps** — on the fashion store (org1-store2): `/en?theme=pink`, then `/en` and a product page, then
  `/en?theme=`, then `/en`. Also `?theme=bogus`, and the `Theme` header with legacy values (`MODERN`, `JEWELERY`,
  `COSMETICS`) on the storefront's own port.
- **Expect** — pink on the first three, fashion after clearing; an unknown id renders `starter`; legacy values map
  as `legacy-theme-map.ts` says.
- **Seen** — 2026-09-13: as expected, and the legacy values resolve exactly as on the build before.
- **Re-run** — 2026-09-14, on Next 16.3.5 on the load stack: pink, pink, pink, then fashion after clearing; `bogus`
  renders starter; `MODERN` and `JEWELERY` map to starter and `COSMETICS` to cosmetics, as the map says.

### THM-09 — Client navigation, back/forward and the locale switch stay in the theme's tree · critical · [verified]

- **Steps** — in a browser on the fashion store: click a category link, then a product, add it to the cart, open
  the cart and follow Checkout; go back twice and forward once; switch to French from the header's language menu.
- **Expect** — every step is a client-side navigation (no document reload), `data-theme="fashion"` throughout,
  checkout lists the item, and the language switch lands on `/fr/<same path>` with `lang="fr"`. No file loaded over
  the session carries another theme.
- **Expected to differ**
  - The language switch drops the query string (`?sku=…`); it did before this change too (`usePathname()` carries
    no search).
  - Switched on a product or category page, it lands on the not-found page. Slugs are per language, and the switch
    keeps the path. `/fr/product/<English slug>` is a 404 on a direct load too.
- **Seen** — 2026-09-13, k6 browser through spg: all steps pass; 18 CSS/JS files over the session, none of another
  theme (the build before: 44, 22 of them another theme's).
- **Re-run** — 2026-09-14, on Next 16.3.5 (the image on the load stack), in Chrome through spg, which exercises 16.3's
  client-router defaults:
  - Category, then product (preselected `?sku=`), add to cart, the cart drawer, and Checkout are client-side
    navigations. The same document runs the whole way, and `data-theme="fashion"` holds throughout.
  - Checkout lists the item.
  - Back twice and forward once stay in the same document.
  - French from checkout lands on `/fr/checkout` with `lang="fr"`.

### THM-10 — Sign-in hand-off, checkout and the system routes are unchanged · high · [verified]

- **Steps** — click the header's sign-in button; open `/en/callback?code=bogus&state=bogus`; open `/en/checkout`
  with an item in the cart; request `/store-not-found`, `/sitemap.xml` and `/robots.txt`.
- **Expect** — sign-in lands on `/en/login?auth=1` in the store's theme; the `redirect_uri` sent to cua stays
  `<origin>/<locale>/callback` (built from the origin and locale, never the rewritten path); a bogus code fails the
  exchange and returns to the login page; checkout renders the cart; the three system routes return what they
  returned before.
- **Seen** — 2026-09-13: as expected and identical to the build before (system routes differ only in build ids and
  host). A full sign-in with real credentials through cua was not run.
- **Re-run** — 2026-09-14, on Next 16.3.5 on the load stack:
  - `/store-not-found`, `/sitemap.xml` and `/robots.txt` answer 200 with the expected content, and the login page
    renders in the store's theme.
  - The real sign-in now runs as well. `make browser-shopper-auth` in `../load-testing` registered a shopper, then
    signed in with its credentials through cua's hand-off pages and landed back on the storefront. It had 0 journey
    errors, and 0 of its 80 browser requests failed.

---

## VAR — the variant model on the storefront

Added by the variant rework (PR #306): the PDP resolves an option selection to a variant and adds to cart by
its sku, the listing facets by option value id, and cart and order lines render the combination. The model is
[catalog](../../catalog/catalog-service/qa/catalog-qa.md#var--the-uniform-variant-model).

### SF-01 — The PDP selects a variant · high · [verified]

- **Steps** — open the seeded Zara dress (colour × size, red/L deliberately absent).
- **Expect** — chips for both axes; the default variant preselected; **Red/L greyed** because the combination
  does not exist and **Blue/L greyed** because it exists with quantity 0 (inventory says not purchasable);
  selecting Blue swaps the price 350 → 365 and the sku to `SKU-ZR-CL-DRS02-BL-M`.
- **Result** — exactly that. Repeated on the electronics store's six-combination iPhone: 512 GB + Silver reads
  $1,339.00 (999 + 300 + 40 as seeded) and "Out of stock", which is the seeded zero-stock combination.

### SF-02 — A variant is addressable and shareable · high · [verified]

- **Expect** — selecting a combination writes `?sku=<variantSku>` with `history.replaceState` (no reload, no
  server re-render), and loading that URL cold lands preselected on that variant.
- **Result** — both directions confirmed.

### SF-03b — A purchase carries the combination all the way to the order · high · [verified]

- **Steps** — signed in as the demo shopper, cart holding one combination line (Zara dress Blue/M, SAR 365)
  and one optionless line (Gucci bag) as the control; Cash on Delivery, order placed.
- **The snapshot** — `checkout.order_product_option` gained **exactly two rows, both on the dress line**:
  `color`/`Color`/`blue`/`Blue` (sort 0) and `size`/`Size`/`m`/`M` (sort 1). The bag's line has none. Codes
  *and* names are stored, which is what lets an order keep saying what was bought after an option is renamed
  or deleted. `order_product.product_name` is the real localized name — the `"Product {sku}"` placeholder the
  rework set out to fix is gone.
- **Stock** — decremented on the bought sku **only**: `SKU-ZR-CL-DRS02-BL-M` 8 → 7, while the product's
  default variant stayed at 40 and its Blue/L variant at 0. Two variants of one product really are
  independent inventory rows.
- **Both order views render it** — the console order detail shows `Color: Blue · Size: M` between the name and
  the sku; the storefront's own order view shows `Color: Blue / Size: M` under the name. The optionless line
  shows nothing on either, which is the control.

### SF-03 — The cart line names the combination · high · [verified]

- **Expect** — adding Blue/M gives a line reading **"Color: Blue / Size: M"** at the variant's own price.
- **Result** — confirmed. The labels are the placement-time snapshot, never re-joined from the catalog, so an
  order keeps saying what was bought after the option is renamed or deleted.

### SF-04 — Listing cards stay one-sku-per-product · [verified]

- **Expect** — a card shows the **default** variant's price and never loads the variant rows; `variantCount`
  is the only variant fact a listing payload carries.
- **Result** — `toListingProduct` strips `options` and `variants`; the listing enrichment is one availability
  call for the page's default skus.

### SF-05 — The facet rail filters by option value · high · [verified]

- **Steps** — the seeded Dresses category, whose two products give the rail something to count.
- **Expect** — counted groups per option, a click narrowing the listing and putting the value in the URL, and
  the AND across options anchored to a **single variant**.
- **Result** — the rail renders `FILTER BY COLOR` (Red (1), Blue (1)) and `FILTER BY SIZE` (M (1), L (1))
  beside the pre-existing manufacturer facet. Red alone narrows 2 → 1 with `?options=1`. **Red + L
  (`?options=1,4`) answers "No products" while L alone answers 1** — so the empty result is the anchoring
  and not an empty catalogue, matching the integration test exactly.
- Also confirmed on those cards: the variant product offers *view details* while the simple one offers
  quick-add, which is the card contract deriving `hasVariants` from `variantCount`.
- Still not run: a suggestion carrying `matchedVariantSku` deep-linking the PDP with `?sku=`.

---

### SF-06 — The buy box respects the merchant's per-order limits · high · [verified]

Reported from the running stack: adding 2 of the Zara dress answered 422 `Product SKU-ZR-CL-DRS02 sells
between 1 and 1 per order; 2 was asked.`, and the storefront rendered "Failed to add product to cart."
Two defects behind one symptom.

- **The buy box ignored limits the API publishes.** `quantityOrderMinimum/Maximum` are per sku and reach the
  storefront on every availability read, but `applyVariantInventory` dropped them and `useProductPurchase`
  built its stepper from stock alone — so it offered a quantity the cart was always going to refuse. The
  cart's own `requireQuantityInRange` even says "the storefront clamps client-side"; it did not.
- **Fix** — the bounds ride on `VariantPricing` and enrichment copies them; the stepper's ceiling is
  `min(stock, quantityOrderMaximum)` with `0` meaning no limit, its floor is `quantityOrderMinimum`, and
  `isOutOfStock` covers a floor no stock can reach. `maxQty` deliberately keeps meaning **units on hand** —
  the themes print it as "Only N left", and 8 in stock with a limit of 1 is not "only 1 left".
- **Steps** — open `SKU-NK-RUN-001` (25 in stock, capped at 1 per order) and a generated variant product
  (`SKU-NK-CL-KHD07`, uncapped).
- **Result** — the capped product shows "In stock", quantity pinned at 1 with **both** stepper buttons
  disabled; the uncapped one increments freely. Sizes render S · M · L in that order after the seed's
  `sort_order` fix.

### SF-07 — A refusal says what was actually refused · high · [verified]

`locales/*.json` has carried a message per error `code` since the error contract landed, and **nothing read
them**: every interactive failure notified one fixed string per action, so a quantity cap, an offline
browser and a declined card were all "Failed to add product to cart" / "Failed to place order".

- **Fix** — `useErrorMessage()` resolves code → the caller's own fallback → category → generic, interpolating
  the problem's `params`; wired into add-to-cart (both hooks), cart quantity, remove and checkout.
- **The code was wrong too.** The range refusal reused `CHECKOUT.CART.PRODUCT_NOT_PURCHASABLE`, whose
  contract says the item is not sellable at all and retrying will not help — the opposite of "buy fewer and
  it works". It now raises `CHECKOUT.CART.QUANTITY_OUT_OF_RANGE`, still 422, carrying `sku`, `quantity`,
  `minimum` and `maximum` so the message can name the numbers. Pinned by
  `ProductNotPurchasableExceptionTest`.
- **Steps** — the cart drawer's stepper is deliberately server-guarded rather than clamped (a line's bounds
  are not on the cart payload), so it is the reachable path: put the capped `SKU-NK-RUN-001` in the cart and
  press +.
- **Result** — `POST /api/v1/cart` answers `CHECKOUT.CART.QUANTITY_OUT_OF_RANGE`, and the toast reads **"You
  can order between 1 and 1 of this item — 2 isn't allowed."** Translated in all five locales; the ICU
  plural renders "at least {minimum}" when the maximum is the `0` no-limit sentinel.

### SF-08 — The demo stores can actually sell more than one of something · [verified]

The fashion and beauty seeds set `quantity_ord_max = 1` on every row, so with the limits now enforced client
side every stepper in those stores would have been inert.

- **Fix** — both stores get a spread (about half unlimited, the rest 2/3/5/10). Cars stays at 1 throughout,
  which is right for a car and keeps a whole store exercising the cap; electronics was already 2–10.
  `SKU-NK-RUN-001` keeps its cap of 1 deliberately as the fixture SF-06 and SF-07 test against.
- **Result** — fashion 22 unlimited · 1 capped at 1 · the rest 2–10; beauty 23 unlimited and no row left at 1.

**Landing-ui — the wrong item in the cart**

- *An unresolved combination fell back to the default variant.* `canAdd` never checked that a variant
  resolved and `sku` fell back to `product.sku`, so on the seeded dress (SF-01) picking Red then the greyed L
  showed "In stock" and Red/M's price, kept the button live, and added **Red/M**. All 12 themes rendered the
  unavailable chip as a plain clickable button. Now `unresolved` blocks the add, the badge and button say
  "Not available", and the chips carry `aria-disabled`.
- *Two `ERRORS.CODE.*` keys were dead*: they said `CATALOG_RESERVATION_*` while the service emits
  `INVENTORY.RESERVATION.*`, so the one refusal a shopper can act on ("Only 3 left of X") still fell through
  to "Failed to place order" — the very defect SF-07 exists to fix. Renamed in all five locales.
- `ReadableProductOption.name` was `null` for a language with no option description while the client types it
  non-optional, giving an empty `<legend>`, an empty `aria-label` and "Please choose ". It falls back to the
  code, like `ProductVariantMapper.label` already did.

---

## AUTH — Shopper login and registration

cua renders no pages any more. `/{locale}/login` starts the OAuth2 flow; cua sends the browser back to
`/{locale}/login?auth=1`, which renders `theme.pages.Login` (or the shell fallback); the form posts straight to
`/cua/login`. `/{locale}/register` calls cua's JSON endpoint and then starts the same flow. The server half is
[cua-qa.md](../../cua/qa/cua-qa.md) LGN-01/06/07/08.

### AUTH-01 — The login page is the theme's · critical · [verified]

- **Steps** — for every registered theme open `/en/login?auth=1&theme=<id>` and `/en/register?theme=<id>` on
  `http://org1-store1.spg-507f1f77.gateway.com` (the dev override cookie), then `?theme=` to clear it.
- **Expect** — each theme renders its own `pages/Login.tsx` / `pages/Register.tsx` in its own idiom (basic's
  `display` rule, beauty's `plate`, fashion's `sheet`, furniture's enamel `PageHead`, grocery's `signage`, hunger's
  `press plate`, pink's `hair display`, …), every form posts to `/cua/login` with `client_id`, `lang` and `_csrf`
  hidden inputs, and no `/css/login.css` request appears anywhere (the bridge is gone). Walked for all twelve
  themes through spg; the shell fallback (`default-login-page.tsx`) is now reachable only by a theme that drops
  the page.

### AUTH-02 — The whole flow, and the deep link · critical · [verified]

- **Steps** — open `/en/customer` signed out.
- **Expect** — `Redirecting…`, then the themed login page, then after `user` / `revo` the callback and finally
  `/en/customer` rendered signed in (`postLoginRedirect` survived the hand-off).

### AUTH-03 — A wrong password shows the translated message · high · [verified]

- **Steps** — submit `user` / `wrong`, then correct it. Repeat under `/ar/…`.
- **Expect** — the page reloads as `/en/login?auth=1&error=invalid` with `PAGE.LOGIN.ERROR_INVALID` in the
  banner (Arabic under `/ar/`, right-to-left, form still aligned to the start edge); the second submit succeeds.

### AUTH-04 — Registration, then straight into the store · critical · [verified]

- **Steps** — `/en/register`: submit an empty form, then the seeded `user@mail.com`, then a fresh account.
- **Expect** — the first shows the server's field errors under the fields (`FIELD_ERRORS.*`); the second puts
  `ERRORS.CODE.CUA_REGISTRATION_EMAIL_TAKEN` under the email field; the third goes through the login flow
  without re-asking for anything the shopper already typed except the password, and lands on `/en` — signed in
  as the **new** shopper even if the browser was signed in as someone else beforehand (`prompt=login`).

### AUTH-05 — Social buttons appear only while cua is waiting · high · [not verified]

- **Steps** — compare `/en/login?auth=1` reached through cua with `/en/login?auth=1` typed by hand in a fresh
  browser.
- **Expect** — both render the buttons for the store's enabled providers (the list is the store's, not the
  session's); clicking one in the fresh browser is answered by cua with a redirect to the storefront login
  without the marker, because there is no saved request to resume. Never a 500.

### AUTH-06 — All five locales, both pages · high · [verified] (en, ar in the browser; fr on org2-store1) / [not verified] (es, ru — no demo store serves them; their strings render before the THM-03 fallback redirect)

- **Steps** — open `/{en,ar,es,fr,ru}/login?auth=1` and `/{…}/register` on a store that supports the locale
  (`org1-store1` serves en/ar, `org2-store1` fr; an unsupported locale falls back per THM-03).
- **Expect** — every label, button and message translated; nothing falls back to a key or to English.

### AUTH-07 — The contract still admits a theme without the pages · [unit only]

- **Expect** — `libs/theme/test/define-theme.test.ts`: a theme with neither `Login` nor `Register` validates, a
  theme with both keeps them, and a required page is still required. `npm test --workspace=libs/theme`.

---

## PERF — What a render costs

Every storefront page is rendered per request, on a task of a quarter vCPU on dev, so the CPU one render costs is
the storefront's capacity. The measurement: the production build at `--cpus=0.25 --memory=512m` (`node:20-alpine`,
`OTEL_SDK_DISABLED=true`), its server-side calls going to dev's pod, the headers spg's `domain_lookup` adds for
org1-store2, and the container's cgroup `usage_usec` before and after 20 sequential renders of a page. Separate runs
drift by ±25 %, so a change is compared in one session against the build before it, alternating rounds and taking
the median; pages the change does not touch show the noise (about ±15 %). The method and the dev numbers that
started it are in the orchestrator plan `.agents/plans/landing-ui-render-cost.md`.

From PERF-05 on, the measurement follows dev's task as it is now:

- **Container:** 0.5 vCPU / 1 GiB, on production's base image (`gcr.io/distroless/nodejs20`, which the mirror copies).
- **Telemetry:** on, exporting over OTLP.
- **Backend:** org1-store2's API responses, recorded once from the load stack and replayed. The replay gzips a
  response when the caller asks, as spg does. So a run touches no shared backend and repeats exactly.
- **Readings:** CPU per render is read from the cgroup by a sidecar, over 20 renders of each page, in three rounds
  interleaved with the build before.

The profile behind these cases, and the ideas measured and rejected, are in the orchestrator plan
`.agents/plans/landing-ui-cpu-memory.md`.

### PERF-01 — The category page loads its data once · high · [verified]

- **Why** — `generateMetadata` and the page each called `loadCategory` with a `ListingQuery` built per caller.
  React `cache()` compares arguments by identity, so the memo missed and the whole loader (listing, facets,
  inventory merge, price formatting) ran twice per render. It now takes the query string.
- **Steps** — render `/en/category/laptops` 20 times at 0.25 CPU and compare CPU per render with the build before;
  render `/en/category/no-such-category` and `/en/category/laptops?sort=NEWEST`.
- **Expect** — category CPU per render drops; the backend still sees one call per endpoint (Next's fetch dedupe
  already collapsed the duplicate requests, so the waste was CPU only); the unknown slug is still a 404 (SF-04) and
  the title still comes from the category.
- **Seen** — 2026-09-13, against the local load stack (`../load-testing`, the seeded org1-store2): category 144.3 →
  135.8 ms median (−6 %), 8 backend calls per render before and after; against dev's pod, where every call is TLS:
  240.5 → 189.0 ms (−21 %). 404, title and sorted listing identical to the build before.

### PERF-02 — A page carries its stylesheets as links, not inlined · high · [verified]

- **Why** — `experimental.inlineCss` put the CSS of all twelve themes into every page twice, as `<style>` and
  again as strings in the RSC payload (the theme registry imports every theme into the layout's entry): about
  580 KiB of an 864 KiB home page, rebuilt on every request.
- **Steps** — render `/en`; read the HTML; fetch each `<link rel="stylesheet">`; compare CPU per render with the
  build before.
- **Expect** — no `<style>` element and no CSS strings in the RSC payload; the page links its stylesheets (13 on
  the home page at the time; 2 since each theme has its own route tree, THM-06), each answering 200 from `/_next/static/chunks/` (or the CDN prefix when the S3 sync is on); the
  page looks the same in the browser; CPU per render drops on every page.
- **Expected to differ** — a first visit waits on the stylesheet links; Lighthouse flags them as render-blocking,
  which is what inlining was switched on for. A repeat visit takes them from the cache.
- **Seen** — 2026-09-13, local load stack: home 864 → 265 KiB, 13 links, all 200. The linked files carry the same
  2,600 CSS rules in the same order as the inlined `<style>` did (font `url()`s are relative in a file, absolute
  inline: the same files). CPU per render against the build before: home −17 %, category −24 %, product −27 %,
  search −36 % (medians, same session). Compared as CSS, not looked at in a browser.
- **Re-run** — 2026-09-14, on Next 16.3.5: `inlineCss: false` still holds. Every page page-budget renders (12 themes ×
  4 pages) links 2 stylesheets from the CDN prefix and carries no duplicated inline CSS.

### PERF-03 — spg compresses the storefront's HTML, landing-ui does not · high · [verified]

- **Why** — Next gzipped every page in the storefront's own process (`compress` defaults to true), 10–25 % of a
  render on a quarter vCPU, while spg's Caddy, which already compresses the APIs, passed the HTML through. Next's
  gzip is off (`compress: false`); `encode zstd gzip` in [spg's Caddyfile](../../spg/Caddyfile) takes it.
- **Steps** — through spg (`http://org1-store2.spg-507f1f77.gateway.com/en`): request with
  `Accept-Encoding: gzip`, with `zstd`, and with neither; decode the gzip body. Request landing-ui's own port with
  `Accept-Encoding: gzip`. Time the first and last byte of the home page through spg.
- **Expect** — through spg: `Content-Encoding: gzip`, `zstd`, and none respectively, with `Vary: Accept-Encoding`;
  the gzip body decodes to the whole page. landing-ui's own port answers uncompressed whatever is asked. The first
  byte arrives well before the last (the page still streams).
- **Seen** — 2026-09-13, the load stack's spg (Caddyfile from `main`, the same `encode` line) with this build in
  place of the stack's landing-ui: gzip, zstd and identity as expected; 42 KB of gzip decoding to 273 KB of HTML.
  Home through spg, against the build before: first byte 0.276 → 0.099 s, last byte 0.573 → 0.387 s, 65 → 42 KB
  (Caddy's gzip beats Next's chunk-by-chunk gzip). CPU per render with gzip requested: home −21 %, category −8 %,
  product −7 %, search +2 % (noise).

### PERF-04 — A store's layout data is fetched once per store every 30 s · high · [verified]

- **Why** — the store record, the category tree and the site document behind every page's layout were fetched on
  every render. They are the same for every visitor of a store and change rarely, so Next's data cache now keeps
  them for 30 s (`publicCachedGet` in `libs/services/src/http-utils.ts`): keyed on the URL, which carries `store=`
  and `lang=`, and sent without a credential.
- **Setup** — point `INTERNAL_SPG` at a logging proxy in front of spg, so each backend call is visible.
- **Steps** — render org1-store2's `/en` twice, then its category page, within 30 s; render org1-store1's `/ar`
  twice; wait past 30 s and render org1-store2's `/en` twice more. Compare each store's page title.
- **Expect** — the first render of a store fetches the three once; further renders of that store within 30 s
  fetch none of them; another store fetches its own (`store=` its id) and shows its own name; after 30 s the next
  render serves the cached copy and refreshes it in the background (one fetch each), then none again.
- **Expected to differ** — a merchant's edit to the store record, the category tree or the site document (menus,
  footer pages, announcement, branding, social links) reaches the storefront up to 30 s later than before.
  Product, listing, search, inventory, page content and anything a shopper's session touches are not cached.
- **Seen** — 2026-09-13, local load stack behind the logging proxy: exactly as expected, per store; org1-store2
  and org1-store1 each titled with their own name through the shared cache. Backend calls per render: home 9 → 6,
  category 8 → 5, search 5 → 2. CPU per render, two runs against the build before: search −21 % and −23 %; home,
  category and product within the noise (−10 % to +14 %).
- **Re-run** — 2026-09-14, on Next 16.3.5: the data cache still holds.
  - The same 24 renders make the same backend calls as on 16.0.0: 96 outgoing HTTP spans on each.
  - The `fetch` spans are identical, name by name.

### PERF-05 — Prices come from one formatter per locale and currency · [verified]

- **Why** — `InventoryService.formatAmount` built a new `Intl.NumberFormat` for every price of every product on every
  render: 3.9 % of a home render's CPU in a V8 profile. `currencyFormatter` (`libs/services/src/currency-format.ts`)
  keeps one per `locale|currency` for the process. A code `Intl` rejects is remembered as rejected, and the map is
  bounded.
- **Steps**
  - Run `npm test` in `libs/services` (`test/currency-format.test.ts`).
  - Render home, a category, a product and a search, and compare every price with the build before.
  - Compare CPU per render with the build before.
- **Expect**
  - The same prices in the same places. A store whose currency `Intl` rejects still shows the plain amount.
  - CPU per render the same or lower on the pages with many prices.
- **Seen** — 2026-09-14:
  - The four pages carry the same 110 / 70 / 17 / 45 prices as `main`. The HTML differs from `main` only in chunk
    hashes and the build id.
  - `currency-format.test.ts` passes 4 of 4.
  - CPU per render against `main`: home 63.4 → 63.0, category 50.5 → 49.9, product 27.3 → 29.5, search 27.0 →
    24.7 ms. The mean is −0.7 %, within the noise.
  - An A/B of this function alone, patched into the same build, gave home −4.2 % and category −5.0 % on Node 20,
    and −4.1 % on Node 24. Small and safe, not a lever.

### PERF-06 — landing-ui's calls to spg come back uncompressed · high · [verified] (spg's `encode` line) / [not verified] (the full spg image)

- **Why** — Node's `fetch` sends `accept-encoding: gzip, deflate`. spg's `encode zstd gzip` (the `(routes)` snippet,
  the same line PERF-03 relies on) then gzipped every API response to landing-ui, and landing-ui inflated it again,
  on an internal hop. `apiFetch` now sends `Accept-Encoding: identity` from the server
  (`libs/services/src/http-utils.ts`). A browser's request is left as the caller built it.
- **Setup** — a Caddy with spg's `encode zstd gzip` line and a JSON access log, in front of the backend, with
  `INTERNAL_SPG` pointed at it.
- **Steps**
  - Render the four pages with the build before and with this one. Read each API call's `Accept-Encoding` and the
    `Content-Encoding` it got back.
  - Request the same Caddy as a browser would: `gzip, deflate, br, zstd`, then `gzip, deflate`, then `identity`.
  - Run `npm test` in `libs/services` (`test/http-utils.test.ts`).
- **Expect**
  - Before: `gzip, deflate` asked, and `gzip` returned. Now: `identity` asked, and nothing encoded.
  - The pages are unchanged.
  - A browser still gets zstd or gzip.
  - The tests show:
    - every server-side call carries the header;
    - a caller's own `Accept-Encoding` wins;
    - the caller's `RequestInit` is not mutated;
    - a browser's request is untouched.
- **Expected to differ** — the internal hop carries the JSON uncompressed, about 4× the bytes, inside the VPC. spg no
  longer spends CPU compressing landing-ui's calls.
- **Seen** — 2026-09-14:
  - **Before:** 24 API calls asked for `gzip, deflate` and got gzip, 60 KB on the hop.
  - **Now:** 18 asked for `identity` and got it uncompressed, 257 KB.
  - **Pages:** the same bytes; the HTML differs only in chunk hashes and the build id.
  - **A browser through the same Caddy:** zstd (51 KB → 669 B), gzip (1.2 KB), identity (51 KB).
  - **Tests:** `http-utils.test.ts` passes 5 of 5.
  - **CPU per render** against the formatter commit, with the backend gzipping as spg does: home 63.0 → 59.4,
    category 49.9 → 44.2, product 29.5 → 23.2, search 24.7 → 26.8 ms. The mean is 41.8 → 38.4 ms (−8.1 %).
- **Not verified** — the real spg image with its full route set and the Java backend. The load stack was down; the
  Caddy used carries spg's `encode` line and nothing else.

### PERF-07 — The storefront runs on Node 24 · critical · [verified] (runtime, build, the image on the mirror) / [not verified] (dev)

- **Why**
  - The image ran `gcr.io/distroless/nodejs20`, and Node 20 has been end-of-life since 2026-04-30.
  - Every UI module was built with Node 23.8.0, end-of-life since mid-2025.
  - On Node 24 a render costs about 40 % less CPU, and landing-ui is the service dev saturates first.
  - The Dockerfile now runs `public.ecr.aws/b2i4h4k9/nodejs24:latest`, which cvhome-saas/public-dkr mirrors from
    `gcr.io/distroless/nodejs24`.
  - Every build Node moves to 24.21.0: `com.asrevo.ui-conventions` (console-ui and landing-ui), ui-kit, and uaa's
    `uaa-fe`.
- **Steps**
  - Run the standalone build on `gcr.io/distroless/nodejs24:latest` (the image the mirror copies) at 0.5 vCPU /
    1 GiB, telemetry on. Render the four pages, and compare CPU per render with `main` on Node 20.
  - Run `./gradlew :store-commons:ui-kit:build :store-core:console-ui:build :store-pod:landing-ui:build
    :store-core:uaa:build -x test -x check`.
  - Once public-dkr has published, run `./gradlew :store-pod:landing-ui:bootBuildImage` and render through the
    image.
- **Expect**
  - The same pages, and no errors in the log.
  - CPU per render about 40 % lower.
  - Memory at idle about 30 MiB higher, still far inside 1 GiB.
  - Every UI module downloads and builds with Node 24.21.0.
- **Seen** — 2026-09-14:
  - The runtime reports `v24.21.0`. All four pages return 200 with the same prices and bytes as `main` (only chunk
    hashes and the build id differ), and the log has 0 errors.
  - CPU per render against `main` on Node 20, the three commits together: home 63.4 → 35.9, category 50.5 → 24.8,
    product 27.3 → 14.4, search 27.0 → 19.8 ms. The mean is 42.0 → 23.7 ms (−43.6 %).
  - Memory at idle went from 84–93 to 118–122 MiB.
  - The Gradle build is `BUILD SUCCESSFUL`, and each of the four modules downloaded `node-v24.21.0` and ran
    `npmInstall` and its build with it.
  - Earlier, same harness: a burst of 300 shoppers with 30 s of patience on one task. Node 24 served all 300, with
    p50 9 s and p95 16 s, and peaked at about 210 MiB anon. Node 20 served 291–297, with p50 16 s and p95 29 s, and
    peaked at about 390 MiB.
- **Seen, the image on the mirror** — 2026-09-14, after cvhome-saas/public-dkr#4 published:
  - The Dockerfile builds on `public.ecr.aws/b2i4h4k9/nodejs24:latest` (linux/amd64), and the image reports
    `v24.21.0`.
  - It ran in place of the load stack's landing-ui: page-budget, the k6 browser journeys and the SEC-02 probes pass
    (SEC-01).
- **Re-run on Next 16.3.5** — 2026-09-14, same harness, three rounds against 16.0.0 (SEC-01):
  - home 35.9 → 33.3, category 24.3 → 25.5, product 15.8 → 15.0, search 19.5 → 17.8 ms. The mean is 23.9 → 22.9 ms
    (−4 %, within the noise).
  - Anon memory after the renders is 179–185 → 172 MiB.
- **Not verified** — dev's x86 Fargate. Every ratio was measured on arm64.

---

## SEC — The framework's own security

The storefront runs Next 16.3.5 with React 19.2.8. The plan is `.agents/plans/next-react-security-upgrade.md`: the
advisories, the versions that fix them, and what changed for this app between 16.0 and 16.3.

### SEC-01 — The framework carries no known advisory, and the standalone build serves · critical · [verified]

- **Why**
  - 16.0.0 carried 37 advisories. They include the critical RSC flight-protocol RCE (GHSA-9qr9-h5gf-34mp), proxy
    bypasses, RSC denial of service and SSRF in rewrites.
  - A version bump also has to survive the production layout. On 16.3 the standalone build answered 500 on every
    page until `copy-instrumentation.mjs` linked Turbopack's external aliases (REG).
- **Steps**
  - From `store-pod/landing-ui`, run a fresh `npm install` (no lockfile, as CodeBuild).
  - Run `npm ls next react react-dom eslint-config-next`, then `npm audit`.
  - Run `npm run build`, then serve the build as the Dockerfile lays it out (`storefront/start.mjs`, with
    telemetry on). Render the four key pages.
  - Run `next dev` once in the same checkout (any lcl stack does), stop it, and run `npm run build` again.
- **Expect**
  - One copy each: `next@16.3.5`, `react@19.2.8`, `react-dom@19.2.8`, `eslint-config-next@16.3.5`.
  - No advisory against next, react or react-dom.
  - 200 on every page, `✅ OpenTelemetry instrumentation started` in the log, and no error.
  - The second build succeeds too. Standalone's `node_modules` holds the S3 SDK (`@aws-sdk/client-s3` and its
    dependencies), copied by `copy-instrumentation.mjs`.
- **Expected to differ**
  - In traces, the proxy's span (`middleware GET`) is now the root of its own trace; on 16.0 it was a child of the
    request's `GET`. Spans per render are the same: 562 for the same 24 renders on both.
  - `npm audit` still lists the `@opentelemetry/*` pins: 4 high, 23 moderate. The highs need the Prometheus
    exporter or the Jaeger propagator, and neither is configured. They are a separate upgrade (the plan's *Out of
    scope*).
- **Seen** — 2026-09-14:
  - The versions are as expected, each a single copy.
  - `npm audit` shows 0 advisories against the framework, and 27 in OpenTelemetry and `uuid`.
  - On `gcr.io/distroless/nodejs24` at 0.5 vCPU / 1 GiB, every page returns 200 and the log is clean.
  - The image built from the Dockerfile on the mirror's `nodejs24` ran on the load stack in CDN mode. It uploaded its
    build to MinIO and rewrote the prefix. Telemetry started, with no error.
  - A build after `next dev` failed with EISDIR while the S3 SDK was traced in by `outputFileTracingIncludes` (REG).
    Now it builds, and the image, 86.8 MB instead of 107.5, still uploads to MinIO.

### SEC-02 — A theme's tree cannot be reached in any URL form · high · [verified]

- **Why**
  - THM-07 keeps `/t/<theme>/…` private: the proxy answers 404.
  - A tree's RSC payload also answers at `<path>.rsc`. The matcher skipped every path containing a dot, and Next
    appends the payload suffixes after that pattern, so the proxy never ran for `/t/fashion/en.rsc`. The matcher now
    also lists `/t/:path*`.
  - Nothing private is exposed this way: the proxy holds no authorization, and a tree renders the same store's public
    data. What broke was the contract: `/t/pink/en.rsc` rendered a fashion store in the pink tree.
- **Steps** — through spg on org1-store2, request each of these, with and without `RSC: 1`:
  - `/t/fashion/en`, `/t`
  - `/t/fashion/en.rsc`, `/t/fashion/en/product/<slug>.rsc`
  - `/t/fashion/en.segments/_tree.segment.rsc`
  - `/t/fashion/en` with `Next-Router-Prefetch: 1`, with `Next-Router-Segment-Prefetch: /_tree`, with a forged
    `x-middleware-subrequest`, with `x-nextjs-data: 1`, and with `?_rsc=…`
  - `/%74/fashion/en`, `/t%2Ffashion%2Fen`, `//t/fashion/en`, `/t/fashion/en/`

  Also request `/en` with `RSC: 1` alone, and follow redirects throughout.
- **Expect**
  - 404 for every form of a tree URL.
  - The encoded and doubled forms redirect, then end at 404.
  - `/en` with `RSC: 1` gets a 307 to `/en?_rsc…`, then the payload. Since 16.3, an RSC request whose `_rsc` does
    not match its headers is redirected (`validateRSCRequestHeaders`); a browser always sends the matching pair.
- **Seen** — 2026-09-14:
  - Before, on `main`'s build (16.0.0): `/t/fashion/en.rsc` with `RSC: 1` answered 200 with the tree's 94 KB payload,
    and so did the product form.
  - On 16.3.5 without the matcher change, `/t/fashion/en.rsc` answered 200 with or without the header.
    `/t/pink/en.rsc` answered with a different payload, pink's client chunks, for a fashion store.
  - After, every form is 404: first against the build directly, then through spg on the load stack. `/en` with
    `RSC: 1` gets 307, then 200 `text/x-component`.

### SEC-03 — The dev server behind spg keeps hot reload · high · [verified]

- **Why** — from 16.2, `next dev` refuses cross-origin requests to its `/_next` resources. Under lcl the browser
  reaches it through spg as `<store>.spg-507f1f77.gateway.com`, so the HMR socket was refused. `next.config.ts` allows
  the subdomains of the `INTERNAL_SPG` host, which lcl sets.
- **Setup** — `next dev` from `storefront/`, with and without lcl's
  `INTERNAL_SPG=http://spg-507f1f77.gateway.com:<port>`, reached through spg (or a proxy adding spg's headers) at
  `http://org1-store2.spg-507f1f77.gateway.com…`.
- **Steps** — open `/en`; from its console open `new WebSocket('ws://<host>/_next/hmr')` (the 16.3 path; it was
  `/_next/webpack-hmr`); read the dev log. Look for new files in `storefront/`.
- **Expect**
  - With `INTERNAL_SPG`: the page renders, the socket opens, and the log has no "Blocked cross-origin request".
  - Without it: the socket errors, and the log says `Blocked cross-origin request to Next.js dev resource /_next/hmr`.
  - No `AGENTS.md` or `CLAUDE.md` appears in `storefront/` (`agentRules: false`).
- **Seen** — 2026-09-14, on 16.3.5, through a Caddy that adds spg's headers for org1-store2 in front of `next dev`:
  exactly as expected, both ways.
  - Before `agentRules: false`, the first dev start wrote both files into `storefront/`.
  - Not run through a full `lcl start`: the stand-in carries spg's headers and the WebSocket upgrade, which is all this
    case depends on.

---

## REG — Regression watchlist

| What broke | How it looked | Caught by |
|---|---|---|
| Pink's Google CJK font fan-out | Turbopack could not resolve its virtual font module. | THM-05; pink font source test |
| Next 16.3's Turbopack requires external packages by a hashed alias (`require-in-the-middle-<hash>`, a symlink in `.next/node_modules`) | Every page 500 on the standalone build (`start.mjs`): `Failed to load external module require-in-the-middle-…`. `next build`, lint, typecheck and tests were all green. | `copy-instrumentation.test.mjs`; SEC-01 |
| The `.rsc` form of a theme-tree URL skipped the proxy | `/t/fashion/en.rsc` answered 200 with the tree's payload | SEC-02 |
| Next 16.3's Turbopack matches `outputFileTracingIncludes` globs anywhere in a path, and hashes every match | After any `next dev` in the checkout, `next build` failed: `reading file …/.next-<stack>/dev/node_modules/@aws-sdk/client-s3-<hash>: Is a directory` | SEC-01 (the build after `next dev`); `copy-instrumentation.test.mjs` |

---

## LOAD — The 2026-09-14 load-test fixes

Findings 1 and 7 of *Where cvhome Breaks* (orchestrator `.agents/plans/load-bottlenecks.md`). **lcl runs landing-ui
under `next dev`, which never goes through `start.mjs`**, so the page cache and the request signal are off on a plain lcl
stack. (The page cache is its own PR; its cases LOAD-01/02/03/05/06/07 arrive with it.) To QA them: `npm run build` in `store-pod/landing-ui`, `lcl stop landing-ui --stack <name>`, then from
`storefront/` run `PORT=<landing-ui port> INTERNAL_SPG=http://spg-507f1f77.gateway.com:<spg port> node start.mjs`, and
browse through spg as usual.

### LOAD-04 — A render stops when its shopper leaves, and a slow backend read fails in 3 s · high · [verified]

- **Steps** — the production build in front of a fake spg that holds every call 5 s; a client that gives up after
  1 s, then one that waits.
- **Result** — the render's four backend calls are aborted at 929 ms when the client leaves, and no further call is
  made; for the waiting client each read is aborted at 3.0 s (`STOREFRONT_BACKEND_TIMEOUT_MS`, 0 waits). Writes have no
  budget. Covered also by `libs/services/test/http-utils.test.ts` and `scripts/server/request-scope.test.mjs`.


### LOAD-08 — A video section loads its player only when the shopper presses play · high · [not verified]

The re-run's browser failures on the home page were all the seeded YouTube embed: `load` came 13–26 s after the
document with the stack idle.

- **Steps** — open a page with a video section (org1-store2's home); watch the network panel; press play.
- **Expect** — before the press: no request to youtube-nocookie.com, one still from `i.ytimg.com` (YouTube) or the
  muted surface (Vimeo), a button labelled "Play video: <title>"; after it: the player's iframe with `autoplay=1`.
- **Result** — `libs/theme/test/models.test.ts` (the provider, id and poster); the component itself not yet driven.

---

## 99 — Known gaps

- **`libs/types`, `libs/services` and `libs/hooks` are linted by nothing.** `npm run lint` covers
  `storefront libs/ui libs/i18n libs/theme themes`, so the three tsc-built libs — including
  `use-product-purchase.ts`, which carried two of the variant rework's blockers — are checked only by `tsc`.
  That is how a `react-hooks/set-state-in-effect` **error** sat in the quantity clamp unnoticed (since fixed by
  deriving during render). Adding them to the script surfaces 54 pre-existing errors of unrelated origin, so
  the scope change belongs in its own PR.
- **A suggestion's `matchedVariantSku` deep link** — the one interaction in the variant rework never driven end
  to end. Everything it depends on is verified (suggest returns the field, the provider maps it into the href,
  and `?sku=` preselection works — SF-02), so what is untested is the wiring between them.
- **`ProductAttribute*`** remains in landing-ui's types, dead on the wire: every theme's product page renders a
  specifications block from it that degrades to nothing. Descriptive attributes are a stated future feature —
  delete the shape together with those blocks, or revive it when the feature lands.

**The Next dev server 500s on unknown slugs** instead of rendering a 404 page (SF-04). Dev-only; the production
build renders the 404.

**A cart the checkout service no longer has is never replaced.** The browser keeps the cart's code in
`localStorage` (`seller-ui-cart-data`). If checkout no longer has that cart, every add answers 404
(`PUT /checkout/api/v1/cart/<code>`), and the storefront keeps the dead code instead of starting a new cart, so
nothing can be bought. Clearing the key recovers it: the next add creates a cart (`POST … 201`). Seen 2026-09-14 on
the load stack, whose database had been recreated since the browser's last visit.

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

---

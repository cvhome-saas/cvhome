# Theme architecture

How a storefront theme is built, what it owns, and how the home-page builder renders through it.
Read this before scaffolding or extending a theme; `themes/README.md` holds the direction catalog
and the section design rules, each theme's `DESIGN.md` holds its own visual world.

## The one split that explains everything

**The shell owns behavior; a theme owns voice.** Cart, checkout, account, search, routing, data
loading, the layout document pipeline — shell (`storefront/src/`). Typography, color, surfaces,
composition, motion — theme (`themes/<id>/`). A theme composes shared behavior; it never
re-implements it. The same split repeats at every level below.

## Anatomy of a theme package

```
themes/<id>/
  DESIGN.md              the theme's visual world (written by the impeccable documenter at finish)
  src/
    index.ts             defineTheme({...}) — the single export the shell loads (server side)
    client.ts            GENERATED 'use client' barrier: one next/dynamic per client component (see below)
    client-bundle.ts     GENERATED barrel of every 'use client' component + tokens.css + ThemeFrame = ONE chunk
    ThemeFrame.tsx       'use client'; puts the next/font variables on <html> from inside the theme's chunk
    tokens.css           [data-theme="<id>"] design tokens + the theme's component vocabulary
    fonts.ts             next/font faces exported as CSS variables (imported by ThemeFrame only)
    colors.ts            GENERATED default palette — edit the seed in
                         libs/types/scripts/build-color-schemas.mjs, never this file
    config.ts            ThemeLayoutConfig (container widths, product grid, header behavior)
    layout/              Root (header/footer/announcement) — wraps every page
    pages/               Category, Product, Content, Blog*, Faq, Policy, Checkout*, Customer, Order
                         (all required; Search optional — the shell has a token-built default)
    sections/            LayoutSections.tsx (the builder registry, see below) + the theme's
                         bespoke pieces (Hero, ProductRail, BuyBox, Gallery, Listing, …)
    components/          PageShell, ProductCard, ProductGrid, SectionHeading, Breadcrumbs, …
    states/              EmptyState, error states, skeletons
```

Registration lives in the shell and is done by the scaffold: `storefront/src/shell/theme/registry.ts`
(dynamic import), `themes.css` (Tailwind source), `next.config.ts` (transpile), `legacy-theme-map.ts`,
the `Theme` enum in `libs/types`, and a palette seed. Merchant colors arrive as a `ColorTheme`
preset through the contrast-guarded bridge in `libs/theme` — a theme may re-map roles
(`tokens.mapMerchantColors`), never ignore them.

## The client barrier: why server files import from `./client`

Turbopack decides a route's browser chunk group from the *whole* server module graph, dynamic `import()`
included — the registry's per-theme `import()` splits server chunks only. Left alone, every theme's
`'use client'` components land in the one chunk group each storefront downloads (twelve themes' worth for a
store that renders one). What Turbopack does split is an `import()` issued from a client module, so:

- `client-bundle.ts` re-exports every `'use client'` component of the theme, imports `tokens.css` and
  exports `ThemeFrame` — one module, so one lazily loaded chunk per theme (JS *and* CSS *and* fonts).
- `client.ts` (`'use client'`) wraps each of them in `next/dynamic(() => import('./client-bundle')…)`.
  The `import()` literal is what the `next/dynamic` transform records, so SSR preloads the chunk and its
  CSS: no hydration waterfall, no flash of unstyled content.
- Server files (`Root`, pages, `LayoutSections`, skeletons, `index.ts`) import client components **only
  from `./client`**; client files import each other directly (they share the chunk). `index.ts` does not
  import `tokens.css` or `fonts.ts` — the shell renders `layout.Frame` (= `ThemeFrame` from `./client`)
  around `Root`, and that is what brings the theme's CSS and fonts in.

Both generated files are written by `node scripts/theme-client-barrier.mjs <id>` (idempotent; re-run it after
adding a client component) and checked by `storefront/scripts/theme-client-barrier.test.mjs`: a direct import
of a client file from a server file fails the test, because the build would not warn and every store would
silently pay for it again.

## The home page: layout document → sections → voice

The home page is not theme code. The builder (console) edits a **layout document** per store
(content service, `page_layout`): an ordered list of sections, each `{kind, variant, props, items,
text, style}`. Rendering pipeline:

```
page_layout (published or draft-by-preview-token)
  → storefront/src/shell/loaders/home.ts        resolves data per section (products, categories, faq, posts)
  → storefront/src/shell/sections/section-list  wraps each section (width/spacing/tone, visibility, builder bridge)
  → resolve-renderer                            theme registry kind.variant → theme kind → shell fallback
  → the renderer                                composed from the three layers below
```

The kind/variant/field catalogue lives in `libs/theme/src/sections/catalog.ts` and is served merged
with the active theme's registry at `GET /api/theme-manifest` — the builder's inspector is generated
from it, so a field you declare is a promise every renderer must keep. The next layer is how that
promise is kept exactly once.

## The three layers of a section renderer

**Consistency is by construction.** Eight registries (7 themes + the shell fallback) never
hand-agree on semantics; instead:

1. **Models — semantics, once** (`libs/theme/src/sections/models.ts`, unit-tested).
   Pure functions that read a section's props/text/data and apply meaning: every catalogue field
   honored, empty rules, CTA label dedupe, limits, link (`linkHref`) and media resolution,
   `embedUrl`. If the inspector offers a field, its value appears in a model — nowhere else parses
   sections.

2. **Composer + chrome — structure, once** (`libs/ui/src/sections/compose.tsx`).
   `sectionsFromChrome(chrome, overrides)` builds every *composable* kind — usp, categories, promo,
   faq, newsletter, richtext, testimonials, brands, image, video, posts — from the models and a
   theme's `SectionChrome`: ~10 voice primitives (Heading, Badge, NavToken, Band, Panel, Quote,
   MediaFigure, BrandLabel, VideoFrame, PostCard) plus class strings (form controls, panel title,
   prose, FAQ summary) and options (navLayout, quoteGridClass, proseOnPanel). Structure, headings,
   `<bdi>` isolation, empty→null (builder hint in preview) are the composer's; a chrome primitive
   that deliberately drops a model value (a typographic badge not drawing `icon`, fashion's wall
   never autoplaying) says so in a comment — silent ignoring is a bug.

3. **Overrides — identity, per theme.**
   `hero` and `products` are always bespoke (the selling composition and the product surfaces are
   the theme's identity), and a theme may add a signature piece (furniture's DirectoryBoard as
   `categories`) or an exclusive variant (beauty's `hero.editorial` — the manifest offers it only
   there; a theme switch falls back to the kind's default). Overrides still consume
   `heroModel`/`productsModel`, so their semantics cannot drift either.

The shell fallbacks are the same composer with `neutralChrome`
(`storefront/src/shell/sections/fallbacks/neutral.tsx`) — the neutral voice on the same code path,
which is why any layout renders on any theme, and why a theme can re-voice a section but never
change what it means.

```
                    catalog.ts (kinds, variants, fields, presets)
                          │ declares
                          ▼
models.ts ──semantics──► sectionsFromChrome(chrome, overrides) ──► ThemeSectionRegistry
                          ▲                        ▲
              SectionChrome (theme voice)   hero/products (theme identity, on the models)
                          │
        neutralChrome = the shell fallback voice (same call)
```

## Extending

- **A new theme**: `npm run new-theme <id>` copies starter — you inherit starter's registry
  (hero + products on the models) and the neutral composer voice for everything else. Grow it by
  writing a `SectionChrome` from your `tokens.css` vocabulary and switching the registry to
  `sectionsFromChrome(chrome, {hero, products})`; every composed kind snaps into your voice at
  once. See any designed theme's `sections/LayoutSections.tsx` for the shape.
- **A new field on a kind**: declare it in `catalog.ts`, surface it in the model, spend it in the
  composer or document which chromes ignore it. Three files, no per-theme sweep.
- **A new kind**: catalog entry + model + one composed implementation (+ a neutral chrome mapping);
  every theme gets it immediately, themes with an opinion override or extend their chrome.
- **A theme-only variant or preset**: add it to the theme's registry/preset additions — the
  manifest picks it up for that theme's stores automatically.

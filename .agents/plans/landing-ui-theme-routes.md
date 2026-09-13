# landing-ui: one route tree per theme, so a store loads only its own theme

One PR, `refactor/landing-ui-theme-routes`, one commit per phase, easiest first. Only
`store-pod/landing-ui` changes. The public URLs, spg, the CDN and the API routes stay as they are. No other repo
is touched. The write-up of the measured result goes to load-testing's `docs/monitoring/performance-improvements.md`
after dev is deployed; that is a follow-up, not a phase.

## What was found (2026-09-13, dev, after cvhome-saas/cvhome#353)

The home page of org1-store2, a FASHION store, loads files for all 12 themes:

| | Files | Compressed | Of which other themes |
| --- | --- | --- | --- |
| CSS | 13 | 54 KiB | 11 files, ~33 KiB: one `tokens.css` + `next/font` file per theme |
| JS | 31 | 517 KiB | 11 chunks of 51–69 KiB, ~163 KiB (~625 KiB of JavaScript to parse) |

In total, 22 of the 44 files and about a third of the bytes belong to themes this store never renders. The shared
Tailwind file (125 KiB raw) also contains classes from all 12 themes.

**Why.** The `[locale]` layout and every page take their theme from `getTheme()`, which resolves it through
`shell/theme/registry.ts`, a map of 12 `import()`s. The registry's comment says only the resolved theme's client
components reach the browser. They do not. The bundler cannot know at build time which theme a request will pick, so
every theme's CSS and client components are attached to the one route tree every page shares. Tailwind scans all 12
theme folders because `app/themes.css` lists them as `@source`.

## The design

Next splits CSS and JS **per route**. So each theme gets its own route tree, and a tree's layout and pages import
only that theme:

```
storefront/src/app/
  (storefront)/t/fashion/[locale]/layout.tsx        ← imports @store-front/theme-fashion only
  (storefront)/t/fashion/[locale]/(home)/page.tsx   ← export default homeRoute(fashion)
  (storefront)/t/fashion/[locale]/product/[url]/page.tsx
  …                                                 ← the same 25 files for every theme
  (system)/store-not-found/…                        ← unchanged
  api/theme-manifest/route.ts, robots.ts, sitemap.ts, global-error.tsx   ← unchanged, not per theme
```

- **The middleware picks the tree.** `proxy.ts` already has the store's theme: spg's `domain_lookup` sends it as the
  `Theme` header. After locale routing, it **rewrites** `/en/…` to `/t/<theme>/en/…`. It rewrites rather than
  redirects, so the browser keeps `/en/…` and client navigations (RSC and prefetch requests) are rewritten the same
  way.
- **The segment is `t`, not `_t`.** A folder starting with `_` is private to the App Router and never routed. The
  middleware answers 404 to any request that arrives with `/t/…` already in its path, so a theme cannot be addressed
  from outside.
- **The per-theme files are generated stubs.** The logic of each route moves into `shell/routes/*.tsx` as a factory
  that takes a theme (`homeRoute(theme)`). Each tree's files are one-line re-exports that bind their theme. A
  generator writes them for every registered theme; the files are committed and checked by `npm test`, like the
  existing `@themes` markers. Per theme that is 1 layout, 18 pages, 4 loading screens, `not-found` and `error`: 25
  files, 300 for 12 themes.
- **Theme resolution moves to a module with no imports.** The ids, `resolveThemeId` and the legacy map move into
  `shell/theme/theme-id.ts`, so the middleware never pulls a theme package into its bundle. The resolution order is
  unchanged: `?theme=` (when overrides are enabled, read from the request itself because the cookie is only set on the
  response), then the override cookie, the `Theme` header, `STOREFRONT_THEME`, and the fallback.
- **next-intl stays in front.** Its redirects pass through untouched. Otherwise its response is re-targeted to the theme
  path, carrying its headers and cookies (`NEXT_LOCALE`).
- **`next/font` keeps working.** Each theme's `fonts.ts` is imported only by its own layout. `?theme=` previews keep
  working, because the middleware rewrites to a different tree.
- **Kept as is:** `/api/theme-manifest` keeps `getTheme()` and the registry. It is server-only, so it ships nothing to
  a browser, and the matcher already skips `api`.
- **Tailwind per theme (phase 5).** `globals.css` loses its `@source` list. Its shared part (the `@theme inline` token
  mapping, fallbacks, variants, `@source` of `libs/ui`) becomes a partial. Each theme gets a generated entry that
  imports the partial and adds `@source` for its own folder, and its layout imports that entry. Every theme's file then
  holds shared utilities plus its own. Shared classes repeat across the files, but a shopper loads one.

**Why not one Next app per theme** (the release/1.0.14 `server.ts` design, several Next servers in one Express
process):

- a cold start of seconds per theme on every deploy and every new task;
- one full Next server's memory per theme loaded, in a task that has already run out of heap at 512 MB;
- 12 builds in one image, and duplicated caches;
- several Next servers in one process is not a supported Next setup.

A single app with one tree per theme gives the browser the same one-theme payload without those costs.

**Accepted costs:**

- a longer build and more server output (300 route entries), both measured in phase 3;
- 300 generated files in the tree.

## Phase 1 — this plan

## Phase 2 — each route as a factory of its theme

A refactor with no behaviour change.

- **Factories.** Every route file under `(storefront)/[locale]/` moves into `shell/routes/`, as
  `export function homeRoute(theme: ThemeDefinition)` and the like. The factory returns the component, plus
  `generateMetadata` where the route has one.
- **Theme passed, not fetched.** `loadPageContext`, `not-found`, the layout and the loading screens take the theme as
  an argument instead of calling `getTheme()`.
- **The existing tree stays.** It calls the factories with `await getTheme()`, so every store still renders exactly
  as before.

Verified: build, lint, typecheck and tests pass; the render-CPU harness shows no change; smoke passes.

## Phase 3 — the proof: fashion gets its own tree

- **Two generators:**
  - `scripts/theme-routes.mjs` writes `(storefront)/t/<id>/[locale]/**` from the route list;
  - `scripts/theme-css.mjs` stays a stub until phase 5.
- **Fashion only.** Run the generator for fashion, and have the middleware rewrite only fashion stores. Every other
  store keeps the shared tree.
- **Theme id module.** `shell/theme/theme-id.ts` is added and `get-theme.ts` uses it.

**Decision gate.** Measured on a local production build: a fashion page must load **no other theme's** CSS or JS,
checked by the same file-by-file attribution used on dev. If Next does not split it, the plan stops here and the
commit is reverted.

Also recorded:

- build time and `.next/standalone` size;
- CPU per render (the 0.25-CPU harness);
- client navigation, back/forward, locale switch, cua's `callback` hand-off, and checkout on the fashion tree.

## Phase 4 — every theme, and the shared tree goes

- **All themes generated.** The middleware rewrites every store, and the shared `(storefront)/[locale]/` tree is
  deleted.
- **Registry.** Only `api/theme-manifest` still uses `registry.ts`, and its comment says why.
- **New themes.** `scripts/new-theme.mjs` also generates the new theme's tree.
- **Staleness check.** `theme-routes.mjs --check` runs in `npm test`: a stale or missing tree fails.

Verified on a local build, with every theme reached through `?theme=`:

- each theme's home, category, product, search, checkout, login and a content page render (200);
- each loads only its own theme's CSS and JS;
- `/t/fashion/en` requested directly answers 404;
- `/store-not-found`, `/sitemap.xml` and `/robots.txt` are unchanged.

## Phase 5 — Tailwind per theme

- **Split the global entry.** `app/globals.css` becomes the shared partial. `app/themes.css` is replaced by one
  generated entry per theme (`app/theme-css/<id>.css`), which each theme's layout imports.
- **Keep generators in step.** `new-theme.mjs` writes the new theme's entry in place of the `@source` line.

Verified:

- a fashion page's utilities file holds no class that only another theme uses (`[animation-duration:2.4s]` is
  pink-only, `[--tilt:-0.6deg]` fashion-only);
- visual check of every theme's home in the browser against a screenshot from before this PR.

## Phase 6 — docs, QA and the measured result

- **Architecture docs:** `themes/ARCHITECTURE.md`, `references/landing-ui.md` and `references/new-landing-ui-template.md`
  in the project-structure skill: how a theme is loaded, and what the generators write.
- **Stale comments:** `registry.ts`, and the "same layout entry" note in `themes/fashion/src/fonts.ts`.
- **QA** in `store-pod/landing-ui/qa/landing-ui-qa.md`:
  - one theme's files per page;
  - `/t/…` returns 404;
  - `?theme=` switches tree;
  - client navigation and locale switch;
  - the cua callback, checkout, and store-not-found.

## Gates

`npm run lint`, `npm run typecheck` and `npm test` in `store-pod/landing-ui`, then
`extra/scripts/verify-before-push.sh`.

## Measurement, before and after

| What | How |
| --- | --- |
| CSS and JS per page: files, bytes, and which themes they hold | the file-by-file attribution script, on a local build and on dev after deploy |
| CPU per render | the 0.25-CPU / 512 MB harness from #353 |
| Build time, `.next/standalone` size, image size | the production build |
| Memory idle, and after every theme's routes are warm | `docker stats` on the image |
| First-visit Lighthouse on a fashion home | before and after |

## Open, decided after phase 5

**`inlineCss`.** It was turned off in #353 because inlining all 12 themes' CSS cost 28–39 % of each render's CPU.
With one theme's CSS per page, inlining may be affordable again for first paint. It gets its own measured A/B,
outside this plan.

## Deviations as built

## Verification

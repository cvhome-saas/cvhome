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

- **The design gate does not apply.** The 300 generated `page.tsx`/`layout.tsx`/`loading.tsx` stubs add no screen:
  each binds an existing screen (`theme.pages.*`, the loading and error states, all unchanged) to one theme. They
  are written only by `scripts/theme-routes.mjs`, never by hand, and `npm test` fails on a hand-edited or hand-added
  file. The shared tree's files they replace were deleted, not redesigned.
- **No `scripts/theme-css.mjs`.** The route generator also writes the Tailwind entries (phase 5): the layout stub
  that imports `app/theme-css/<id>.css` comes from the same script and the same id list, so one `--check` covers
  both. Phase 3 therefore had no CSS stub.
- **`theme-trees.ts` existed in phase 3 only.** While the shared tree still served the other themes, the proxy
  needed the list of themes that had a tree; phase 4 removed it with the shared tree.
- **The factories take the theme, not a thunk** (phase 4). The async `ThemeSource` existed only so the shared tree
  could call `getTheme()` per request.
- **Stylesheet before theme.** A tree's layout imports its CSS entry before the theme. As first written (phases 3
  and 4), the theme came first: fashion's `tokens.css` then opened `@layer components` before Tailwind declared its
  layer order, which ranked components below base, and preflight stripped the padding and backgrounds of fashion's
  chips and buttons (six themes use `@layer components`). The phase-5 screenshot comparison caught it; phases 3 and
  4 were rewritten before anything was pushed, and the generator says why the order matters.
- **The same stale "one layout entry" note** was in eleven themes' `fonts.ts`, not only fashion's (starter's copy
  seeds every new theme); all eleven are corrected. `preload: false` itself is unchanged.
- **Measured on the local load stack, not dev.** Dev's storefront DNS no longer resolves (dev is stopped), so the
  data source was `load-testing`'s compose stack (seeded org1-store2) with each build in a container at dev's
  caps (`node:20-alpine`, `--cpus=0.25 --memory=512m`) behind that stack's spg. "On dev after deploy" and the
  first-visit Lighthouse run are not done: nothing was deployed, and Lighthouse was not available locally.
- **Costs the plan did not list.** Memory: with every route of every theme warm, the process holds 130–160 MiB of
  anonymous memory against 69–90 MiB before (204 route entries loaded instead of 17, about 0.35 MiB each; no
  growth over 480 further renders). Idle it is 62 against 48 MiB, because Next 16 preloads every route entry at
  start (`experimental.preloadEntriesOnStart`, default on); with it off, idle drops to 40 MiB and the warm figure
  is unchanged. Server output: `.next/standalone` grows 164 → 246 MB, of which 89 MB is `page.js.nft.json`
  build traces that the server never reads (the pages render with them deleted). Both are follow-ups, not done here.
- **`npm test` runs `theme-routes --check`, but nothing runs landing-ui's `npm test` automatically**: neither CI
  nor `extra/scripts/verify-before-push.sh` does (the colour-schema `--check` has the same gap). Wiring it in is a
  separate change.

## Verification

All on a local production build (`npm run build`, standalone output run the way the Dockerfile runs it) at
`--cpus=0.25 --memory=512m`, behind the load stack's spg, data from its seeded org1-store2 (theme FASHION).
Attribution: every `<link rel="stylesheet">` and `<script src>` of the page, a CSS file assigned to theme X by its
`[data-theme=X]` rules and a JS chunk by the modules under `themes/X/` it defines (module ids from the build's
client-reference manifests).

**Fashion home, before (main `bede05362`) and after (this branch)**

| | Before | After |
| --- | --- | --- |
| Stylesheets | 13 files, 285 KiB (53 KiB gz) | 2 files, 111 KiB (20 KiB gz) |
| of them another theme's | 11 files, 141 KiB (29 KiB gz) | 0 |
| Scripts | 31 files, 1,739 KiB (509 KiB gz) | 16 files, 1,108 KiB (348 KiB gz) |
| of them another theme's | 11 files, 619 KiB (156 KiB gz) | 0 |
| Tailwind utilities file | 127.6 KB (all themes) | 93.1 KB (shared + fashion) |
| HTML | 267 KiB | 250 KiB |
| Browser session (7 client navigations) | 44 CSS/JS files, 22 another theme's | 18 files, 0 |
| CPU per render, 3 × 30 renders, all warm: home / category / product / search | 88.5 / 59.7 / 40.2 / 43.9 ms | 84.9 / 68.1 / 45.2 / 44.8 ms |
| CPU per render, mean of the four | 58.1 ms | 60.7 ms |
| `npm run build` | 25 s (Next compile 12.5 s), one run | 25–33 s (compile 9.7–14.8 s), four runs |
| `.next/standalone` / `.next/static` | 164 MB / 4.5 MB | 246 MB / 7.7 MB |
| Memory (cgroup anon), idle after the first render | 48 MiB | 62 MiB |
| Memory (cgroup anon), every route of every theme warm | 69–90 MiB | 130–160 MiB |

CPU per render moves within the harness's noise (rounds of the same page differ by up to ±15 %); category and
product were 5–8 ms higher in all three rounds, home 4 % lower.

**Per phase**

- Phase 2 (factories): routes, statuses, attribution and CPU identical to main; lint, typecheck, tests pass.
- Phase 3 (fashion tree) — the decision gate: fashion `/en`, a category, a product and a search page load 0 files of
  another theme (main: 22), so the plan continued. Other themes still rendered from the shared tree unchanged.
  Client navigation, back/forward, the locale switch, add to cart → checkout, the sign-in hand-off and the cua
  callback behave as on main, in a browser (k6) through spg.
- Phase 4 (every theme): for each of the 12 themes through `?theme=`, home, category, product, search, checkout,
  login and a content page answer 200, render that theme, and load no file of another theme (84 of 84). `/t/…` from
  outside is a 404; `/store-not-found`, `/sitemap.xml`, `/robots.txt` and `/api/theme-manifest` return what main
  returns. The 23-route smoke matches main's statuses and themes; legacy `Theme` header values resolve as on main.
- Phase 5 (Tailwind per theme): each theme's Tailwind file adds only classes that occur in that theme's own folder;
  `[animation-duration:2.4s]` is in pink's file alone and `[--tilt:-0.6deg]` in fashion's alone; the twelve files
  hold exactly main's 1,419 classes. Every theme's home, screenshotted full-page against main's: same height, and
  pixel-identical except inside the third-party YouTube iframe (and cosmetics' broken hero image, whose alt text
  depends on load timing; same markup).
- Gates: `npm run lint` (0 errors, the 4 warnings main already has), `npm run typecheck`, `npm test` (with
  `theme-routes --check`), then `extra/scripts/verify-before-push.sh`.

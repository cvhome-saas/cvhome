# Storefront initial payload: every store downloads all twelve themes

**Status: §1 and §2 shipped by PR `fix/theme-chunks` (2026-09-01).** Mechanism, guard and measured results:
`themes/ARCHITECTURE.md` "The client barrier", `scripts/theme-client-barrier.mjs`, and
`store-pod/landing-ui/qa/landing-ui-qa.md` THM-05 … THM-08. Neither `--webpack` nor a Next upgrade would have
fixed it (vercel/next.js#61066 is open; #76666 unmerged): the split had to come from an `import()` issued by a
client module. §3 is moot (`lucide-react` is in Next's default `optimizePackageImports`; the big shared chunk is
Radix + yup), §4 stays off (next/font preloads from the layout entry only). The three production bugs below and
the Tailwind `@source`-all-themes utilities (127 KB inlined) remain open.

**Was: urgent.** Measured against production, `https://org2-store2.spg-507f1f77.dev.asrevo.click/ar`,
2026-09-01, over the real CDN. Every number below came off the wire, not off a local build.

## Why

A storefront that renders exactly one theme downloads the client code of **all twelve**. The furniture
store above pulls 30 JavaScript files on first paint; eleven of them are themes it will never render, and
a further ~250 KB of the inlined CSS declares fonts and tokens for themes that are not active. This is on
the critical path of every first visit to every tenant store — the one page where the platform is judged.

It is also not a regression to hunt for. The architecture *intends* to split per theme
(`storefront/src/shell/theme/registry.ts:4` — "each theme becomes its own server chunk and only the
resolved theme's client components reach the browser"). That holds on the server and fails on the client,
silently, with no build warning.

## What was measured

30 JS requests fetched by Chrome, **1.56 MB decompressed / ~445 KB brotli**.

The CDN itself is healthy and is not the problem: `content-encoding: br`,
`cache-control: public, max-age=31536000, immutable`, `x-cache: Hit from cloudfront`. The problem is what
is in the chunks and how many there are.

### The twelve theme chunks

Read the head of every chunk; twelve are one-per-theme, each opening with its own
`"<theme>-announcement-dismissed"` key:

| chunk | theme | raw | brotli |
|---|---|---|---|
| `12604e4bf761092e.js` | **furniture — the only theme this store renders** | 58 KB | 14.0 KB |
| `27b88d002769a1bc.js` | beauty | 66 KB | 14.7 KB |
| `9795f9b785dac08d.js` | basic | 61 KB | 14.9 KB |
| `1ce3c7e6a0efe77f.js` | fashion | 57 KB | 14.0 KB |
| `798d945c108983d0.js` | pink | 56 KB | 13.9 KB |
| `a3e3ef5e8ddff1e3.js` | grocery | 55 KB | 13.9 KB |
| `3537f35afdd2cf4e.js` | hunger | 53 KB | 13.1 KB |
| `88939bb2e0e13ce1.js` | starter | 50 KB | 12.6 KB |
| `90fa3a320728668f.js` | glasses | 49 KB | 12.2 KB |
| `eb31fd2abfd31fb1.js` | jewellery | 49 KB | 12.2 KB |
| `f3f0b0a9dedad5f4.js` | cosmetics | 49 KB | 12.2 KB |
| `fda1c383f36f6995.js` | sports | 49 KB | 12.2 KB |
| | **total** | **651 KB** | **160 KB** |
| | **of which unused** | **594 KB** | **146 KB** |

The rendered document carries `data-theme="furniture"`. The other eleven chunks are downloaded, parsed and
evaluated to register modules nothing will ever call.

### The rest of the payload

| chunk | what it is | raw | brotli |
|---|---|---|---|
| `4b1453983e07b190.js` | lucide-react icon set + Radix primitives, one chunk | 267 KB | 83 KB |
| `80ff52631b32f8d7.js` | Next client runtime | 217 KB | 65 KB |
| `dae05618d7f6f07e.js` | Next app-router | 83 KB | 21 KB |
| `db65ffa2c66eb6e9.js` | **crypto-js** | 73 KB | 23 KB |
| `912ebfafc3ef6578.js` | react-dom | 50 KB | 14 KB |
| `a6dad97d9634a72d.js` | legacy polyfills — `noModule`, **not fetched** by modern browsers | (110 KB) | (38 KB) |
| 12 others | app + shared chunks | ~215 KB | ~65 KB |

### The document

908 KB raw, **166 KB on the wire**, `cache-control: private, no-cache, no-store` — paid in full on every
page view, cached nowhere.

- **311 KB of it is inlined CSS** (`experimental.inlineCss`, `storefront/next.config.ts:23`).
- That CSS contains **636 `@font-face` rules covering 164 distinct woff2 files**, and `--font-basic`,
  `--font-beauty`, `--font-cosmetics`, `--font-sports` … tokens for all twelve themes.
- **498 KB is the RSC flight payload**, and 422 chunk-path strings are repeated inside it — each of the 19
  client references in the tree carries the *same* 20-chunk list.

## Root cause

`themeLoaders` (`storefront/src/shell/theme/registry.ts:8`) is a static object of twelve `import()` calls.
`getTheme()` (`storefront/src/shell/theme/get-theme.ts:27`) picks one key at **runtime**, from a request
header or cookie — and it is reached from `layout.tsx:35` and from every page in
`storefront/src/app/(storefront)/[locale]/`.

Each `themes/<t>/src/index.ts` eagerly imports ~30 modules, of which **21–24 are `'use client'`**
components (Root, all page components, all states, all skeletons). Turbopack cannot know which key wins,
so client-reference collection for the route reaches all twelve themes' client components and places them
in **one client chunk group**.

The flight payload proves it — every client reference lists the identical twenty chunks:

```
19 static/chunks/12604e4bf761092e.js   (furniture)
19 static/chunks/27b88d002769a1bc.js   (beauty)
19 static/chunks/9795f9b785dac08d.js   (basic)
…all 20 chunks, 19 times each
```

`themes.css` reproduces the same leak in CSS: it `@source`s all twelve theme directories so Tailwind scans
every theme's classes into one stylesheet, which `inlineCss` then stamps into every HTML response.

## The parts that are not obvious

- **The `-service`/`-core` split has no analogue here, and that is the trap.** Server-side the design is
  right: `getTheme()` is `cache()`d, `import()`ed per theme, and only the resolved `ThemeDefinition` is
  used. Nothing about the server code is wrong. The bundle boundary is decided by Turbopack's *static*
  reachability, which does not care that the selection is a single key lookup.
- **This is already known and was mitigated in the wrong layer.**
  `themes/furniture/src/fonts.ts:14` says it outright: *"`preload: false` on every face: all themes' font
  CSS lands in the SAME layout entry, so a preload here fires on every storefront whatever theme is
  active."* Someone hit the symptom, correctly turned off font preloading, and the underlying grouping was
  left in place. Fixing the grouping makes `preload: true` correct again for the active theme's faces —
  which is a *further* LCP win, not just a byte saving.
- **It is not fixable at build time.** The theme is per-tenant, resolved from the `theme` request header
  (`get-theme.ts:23`) on a pod shared by many stores. Pruning `registry.ts` per deployment only works if a
  pod ever serves one theme, which is not the model.
- **`browserslist` is already correct** (`storefront/package.json:14`: chrome ≥ 93, safari ≥ 15.4). The
  110 KB polyfill chunk is `noModule` and no modern browser fetches it. Do not spend time here.
- **crypto-js rides in on a barrel.** `libs/services/src/index.ts:14` is `export * from './pkce-utils'`,
  and `pkce-utils.ts:1` imports crypto-js for exactly two functions —
  `CryptoJS.lib.WordArray.random` and `CryptoJS.SHA256`. Any client module importing *anything* from
  `@store-front/services` drags 73 KB of crypto library with it.
- **Adding a thirteenth theme makes every store slower.** The cost is linear in the number of themes in
  the registry and is paid by tenants who use none of them. `scripts/new-theme.mjs` currently has no way to
  signal this.

## The work, in priority order

### 1. Split the theme client boundary — the whole point of this document

**Win: −11 requests, −146 KB brotli JS, and most of the ~250 KB of inlined foreign-theme CSS.**

The server must keep choosing the theme; only the *client* chunk group has to stop being shared. Two
candidate shapes, to be prototyped against a real `npm run build` before committing:

- **(a) A client-side lazy theme boundary.** A `'use client'` shell holding a per-theme
  `next/dynamic(() => import('@store-front/theme-<t>'))` map, so Next emits a separate async chunk per
  theme and loads only the resolved one. `next/dynamic` is used **nowhere** in the repo today, so this is
  new ground. Cost: theme components move behind a client boundary — measure the SSR/streaming impact on
  LCP before accepting it, because trading server rendering for bytes can be a net loss.
- **(b) Per-theme route groups or build entries**, keeping server rendering intact. Heavier, structurally
  invasive, but preserves the current rendering model exactly.

Decide with numbers, not preference: build both, compare request count, brotli bytes, and LCP against the
current production baseline recorded above.

Whatever the shape, `themes.css` must narrow with it — `@source` only the theme(s) that can reach a given
entry — and `scripts/new-theme.mjs` has to keep generating whatever the new structure needs, since it owns
the `@themes:start`/`@themes:end` markers in both `registry.ts` and `themes.css`.

### 2. Drop crypto-js for Web Crypto

**Win: −23 KB brotli, −1 request. ~10 lines, no architectural risk. Do this first, independently.**

`libs/services/src/pkce-utils.ts` — `crypto.getRandomValues()` and
`crypto.subtle.digest('SHA-256', …)` are native in every browser in the `browserslist` target and give
byte-identical PKCE output. Base64url-encode by hand. Then drop `crypto-js` and `@types/crypto-js` from
`libs/services/package.json`.

`generateCodeChallenge` is already `async`, so the signature does not change; `generateCodeVerifier`
becomes trivially synchronous as it already is. Only caller is `libs/services/src/auth-service.ts:16-18`.

**Verification: a real shopper login through cua, end to end against a running stack.** A broken code
challenge fails at the authorization server, not at compile time — unit tests alone do not cover this.

### 3. `optimizePackageImports` for lucide-react and Radix

**Win: expect most of the 83 KB brotli icon chunk.** `storefront/next.config.ts` has no
`experimental.optimizePackageImports`. Add `lucide-react` and the Radix packages. Measure after — barrel
optimisation interacts with `transpilePackages`, so confirm on the built output rather than assuming.

### 4. Re-enable font preloading once §1 lands

`preload: false` in every `themes/*/src/fonts.ts` exists only because of the shared layout entry. Once a
store's HTML carries only its own theme's faces, preload the active theme's faces and delete the
workaround — and the 636 inlined `@font-face` rules collapse to that theme's handful.

### 5. Do not do

- Chasing the `noModule` polyfill chunk. It is never fetched.
- Touching compression or cache headers on `/storefront/_next/*`. They are already correct.

## Two production bugs found in the same session — unrelated, both urgent

1. **Every CloudFront image on this store is broken.** The logo and all four product images return **503**
   to the browser (`x-cache: Error from cloudfront`); a direct request gives **403 from AmazonS3**:

   ```
   https://d1maghyq0ore94.cloudfront.net/files/65f023632bc26470c104b75f/LOGO/logo.jpeg        → 403
   https://d1maghyq0ore94.cloudfront.net/products/…/SMALL/toyota-camry-2024-1.jpg             → 403
   ```

   `/storefront/_next/*` on the same distribution serves fine, so this is a bucket-policy or OAC gap on the
   `/files/*` and `/products/*` prefixes, not a CDN outage. The storefront preloads these as
   `<link rel="preload" as="image">`, so it is a visible, above-the-fold failure on the LCP element.

2. **Fonts bypass the CDN.** All seven woff2 files load from the store origin
   (`org2-store2.spg-…/_next/static/media/…`), not from `d1maghyq0ore94.cloudfront.net`. The
   `assetPrefix` sentinel (`next.config.ts:70`, substituted by `start.mjs`) rewrites script URLs but not
   the font URLs inside the CSS that `inlineCss` stamps into the HTML — so the `preconnect` warmed at
   `layout.tsx:63` for the crossorigin font connection is spent on a host the fonts never use.

3. Minor, but it blinds future work: CloudFront sends no `Timing-Allow-Origin`, so
   `PerformanceResourceTiming` reports `transferSize: 0` for every CDN asset. Any RUM added to the
   storefront will be unable to see asset weight at all. One response-header policy fixes it.

## Baseline to measure against

Recorded 2026-09-01 from `https://org2-store2.spg-507f1f77.dev.asrevo.click/ar` (theme: furniture, locale:
ar):

| metric | now | target after §1–§3 |
|---|---|---|
| JS requests | 30 | ≤ 18 |
| JS brotli | ~445 KB | ~270 KB |
| JS decompressed | 1.56 MB | ~0.85 MB |
| HTML on the wire | 166 KB | ~110 KB |
| inlined CSS | 311 KB | ~70 KB |
| `@font-face` rules in HTML | 636 | ~12 |

Re-measure the same way — the storefront's `qa/landing-ui-qa.md` should gain a case for it, tagged
`[verified]`, with these numbers as the reference so a regression is visible rather than inferred.

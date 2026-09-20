# The storefront cache — a layered, configurable cache in front of landing-ui's render

Branch `perf/storefront-cache`, one PR, one commit per phase, easiest first. Replaces cvhome #363
(`perf/landing-ui-page-cache`), which is closed once this PR is open. QA cases live in
`store-pod/landing-ui/qa/landing-ui-qa.md`, never here.

## Context

The 2026-09-14 spikes (load-testing `docs/baseline.md`) found landing-ui rendering every page per request: at its CPU cap
for 2m15s at 5×, home median 42 s. #363 answered with one in-process LRU of rendered HTML bolted onto `start.mjs`
(`scripts/server/page-cache.mjs`, 369 lines): one 30 s TTL for every route, bypass rules as regex literals inside the
module, three env vars, a response header and nothing else. What it lacked:

- a **policy**: the home page, a product page and a policy page do not share one freshness; nothing could be tuned
  per route, and nothing could be changed without a rebuild;
- a **store seam**: landing-ui runs 1–3 tasks on dev and 2–12 on prod (`cvhome-platform/services.yaml:173-196`),
  each warming its own copy, and the cache's storage was welded to its middleware;
- **edge headers**: every HTML response left with Next's `private, no-cache, no-store` (`send-payload.js:60` sets it
  only when nothing is set), so no intermediary can ever help;
- **metrics**: the only signal was `x-storefront-cache`.

The anonymous render's inputs are known and few: spg's `store-id`, `theme`, `color-theme`, `default-language`,
`supported-languages` headers (`storefront/src/shell/request/headers.ts:17-26`, `src/proxy.ts:38-76`), the host, the
locale in the URL, path and query; the dev override cookies (`shell/theme/override.ts:6-8`); `XSRF-TOKEN` on
`/login?auth=1` only; `NEXT_LOCALE` for the bare `/` redirect. No session cookie is read on the server; `/customer`,
`/checkout` and `/callback` are client-gated (`shell/auth/secured.tsx`). A document is therefore a function of a short
list of request parts, and a policy table can say, per route class, which parts key it and for how long it lives.

Two Next internals shape the design (16.3.5): a `Cache-Control` set before the render is kept
(`node_modules/next/dist/server/send-payload.js:60`), error and 404 renders force `private, no-store`
(`base-server.js:1312,1361,1732`); and the per-request abort signal is passed into a data-cache-filling fetch
(`patch-fetch.js:652-672`), so a departed shopper can abort a fetch other renders are locked on.

## Decisions

- **Memory only, behind an adapter.** `CacheStore` (`get/set/delete/size/bytes/clear`, async-capable) with a
  `MemoryStore`; a Redis/Valkey store is a later PR with its infrastructure. Single-flight stays per process.
- **TTL only, per route class.** No purge API; a merchant's edit shows within the class's TTL. Entries carry their
  class and store so a purge can be added without a key change.
- **Edge headers now, edge later.** `Cache-Control: public, s-maxage=<ttl>, stale-while-revalidate=<swr>` and a
  merged `Vary` per class; `private, no-store` for shopper routes and every non-200. CloudFront or Caddy in front of
  spg is cvhome-platform / saas-gateway work, noted for the orchestrator.
- **Configuration is env, defaults checked in.** Every variable has a default, so `cvhome-platform` needs no change.
  A JSON blob (`STOREFRONT_CACHE_POLICY_JSON`) and flat per-class variables merge over `cache/config.mjs`; the policy is
  validated at start and logged once.
- **The loopback revalidator stays.** A stale page is refreshed by a request to the server's own port carrying the
  store headers, so the refresh is exactly a shopper's render (proxy rewrite, next-intl, `headers()`); the marker is
  honoured from a loopback address only.
- **Cached data reads carry no request signal.** `publicCachedGet` reads keep the timeout budget only, so one shopper
  leaving does not abort the fetch every other render is waiting on.
- **The data cache is named.** `publicCachedGet(name)` reads a per-name TTL (`STOREFRONT_DATA_CACHE_<NAME>_SECONDS`)
  from `libs/services/src/cache-policy.ts`; the naming convention is the contract with `start.mjs`, no import across.

## The default policy

Matched on the path after `/{locale}`. `enabled: no` classifies only (edge headers and stats still apply).

| class | match | enabled | ttl s | swr s | key parts | edge Cache-Control | share |
|---|---|---|---|---|---|---|---|
| home | `/` | yes | 30 | 300 | host, store-id, theme, color-theme, locale, path, query | `public, s-maxage=30, stale-while-revalidate=300` | 25 % |
| category | `/category/*` | yes | 30 | 300 | same | same | 25 % |
| product | `/product/*` | yes | 20 | 120 | same | `public, s-maxage=20, stale-while-revalidate=120` | 25 % |
| search | `/search` | yes | 15 | 60 | same, query normalised | `public, s-maxage=15, stale-while-revalidate=60` | 10 % |
| content | `/content/*`, `/blog`, `/blog/*` | yes | 60 | 600 | same | `public, s-maxage=60, stale-while-revalidate=600` | 10 % |
| help | `/help`, `/policies/*` | yes | 300 | 3600 | same | `public, s-maxage=300, stale-while-revalidate=3600` | 5 % |
| seo | `/sitemap.xml`, `/robots.txt` | yes | 600 | 3600 | host, store-id, path | `public, s-maxage=600, stale-while-revalidate=3600` | 2 % |
| api-theme-manifest | `/api/theme-manifest` | no | | | | passthrough | |
| shopper | `/login`, `/register`, `/customer/*`, `/checkout/*`, `/callback` | no | | | | `private, no-store` | |
| next-internal | `/_next/*`, `/api/*`, `/store-not-found`, `/t/*`, bare `/` | no | | | | untouched | |
| default | anything else | no | | | | `private, no-store` | |

Bypassed whatever the class, with the reason recorded: a method other than GET/HEAD; `authorization`; any of `rsc`,
`next-router-state-tree`, `next-router-prefetch`, `next-router-segment-prefetch`, `next-url`, `x-middleware-prefetch`;
the `storefront-theme` / `storefront-color` cookies; `?theme=`, `?color=`, `?preview=`; a path starting `//`.
Dropped from the key: `utm_*`, `fbclid`, `gclid`, `msclkid`, `_ga`, `ref`.

Env: `STOREFRONT_CACHE_ENABLED` (true), `_MAX_MB` (64), `_MAX_ENTRY_KB` (1024), `_STORE` (memory), `_DEBUG` (false),
`_STATS_TOKEN` (unset: loopback only), `_EVICTION_LOG_THRESHOLD` (100 per minute); per class
`STOREFRONT_CACHE_<CLASS>_TTL_SECONDS`, `_SWR_SECONDS`, `_ENABLED`; `STOREFRONT_CACHE_POLICY_JSON`, deep-merged over the
defaults, flat variables winning. Key: `v1|<cls>|host=…|store=…|theme=…|color=…|locale=…|path=…|q=<sorted>` with the
class's parts only. `x-storefront-cache` keeps its states (`hit|miss|stale|stale-refresh|shared|bypass`, read by
load-testing's k6); `x-storefront-cache-class`, `-key`, `-reason` only under `_DEBUG`. `GET /_storefront/cache/stats`
answers a JSON snapshot from loopback or with the token.

## Phase 1 — Foundation kept from #363: degraded renders and released signals

`storefront/scripts/server/request-scope.mjs` (+ test), `libs/services/src/http-utils.ts` (+ test): #363's
`withRequestSignal(res, handle, keepAlive)`, `isDegraded(res)`, the `REQUEST_DEGRADED` global, `forServer → {init,
release}` with its own controller and timer, `orUndefined` marking a NETWORK failure. One change: a read with
`next.revalidate > 0` forwards the timeout only.

## Phase 2 — The named data-cache policy in `libs/services`

`libs/services/src/cache-policy.ts` (+ test): `DataCacheName`, `DATA_CACHE_DEFAULT_SECONDS`, `dataCacheSeconds(name,
env)`. `publicCachedGet(name)` replaces the constant; every anonymous GET in `store-service`, `category-service`,
`content-service`, `product-service`, `product-search-service` names its cache; previews stay uncached;
`InventoryService.storeBySite` gets the `store` TTL and a cap; `loadBlogIndex` memoises on positional arguments.

## Phase 3 — Policy, key, store: pure modules

`storefront/scripts/server/cache/{config,policy,key,store,memory-store}.mjs` (+ tests). No wiring yet.

## Phase 4 — Middleware, capture, edge headers, wiring

`cache/{capture,edge-headers,revalidate,page-cache,index}.mjs` (+ tests); `start.mjs` imports `installPageCache` only.

## Phase 5 — Observability

`cache/metrics.mjs`: OTel counters and gauges on the `storefront.cache` meter, a no-op when the API is absent, the
stats snapshot, the eviction log line.

## Phase 6 — QA and docs

`qa/landing-ui-qa.md` LOAD-01/02/03/05/06/07 and PERF-04 rewritten; `references/landing-ui.md` (both copies).

## Other repos

- `cvhome-platform`: nothing now; every variable has a default. The follow-ups (ElastiCache for a Redis store, CloudFront
  in front of spg honouring `s-maxage`, keyed on `Host`) are the orchestrator's to plan.
- `load-testing`: `x-storefront-cache` is unchanged; a follow-up may read `x-storefront-cache-class` and the stats route.

## Not in this PR

A Redis store; a purge API and its Java callers; the edge; a per-store policy header; negative caching of 404s.

## Deviations as built

(filled in per phase)

## Verification

`cd store-pod/landing-ui && npm run lint && npm run typecheck && npm test && npm run build`; a production build behind
spg on an lcl stack per the QA file's LOAD procedure, with the curl checks in each LOAD case; `scripts/verify.sh`.

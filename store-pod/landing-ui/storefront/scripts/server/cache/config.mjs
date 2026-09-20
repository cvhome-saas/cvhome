/**
 * The default policy of the page cache: what a route class is, how long its documents live, what keys them, and
 * what an edge is told. `policy.mjs` merges an environment's changes over this table and validates the result.
 *
 * A class is matched on the path after `/{locale}` (`raw: true` classes on the path as requested, since Next's own
 * files, the sitemap and the API routes carry no locale), first match in this order. `enabled: false` is a class the
 * cache classifies but never keeps: its edge header and its statistics still apply.
 *
 * `edge` says what `Cache-Control` a document of the class leaves with: `public` derives
 * `public, s-maxage=<ttl>, stale-while-revalidate=<swr>` from the class, `private` is `private, no-store`, and
 * `passthrough` leaves whatever Next or the route set. `share` bounds the class to that fraction of the cache's bytes.
 */

/** The request parts a page's key is made of, in key order (`key.mjs`). */
export const PAGE_VARY = Object.freeze(['host', 'store-id', 'theme', 'color-theme', 'locale', 'path', 'query']);

/** The request headers of an App Router client navigation or prefetch: Next answers those with an RSC payload. */
export const NEXT_RSC_HEADERS = Object.freeze(['rsc', 'next-router-state-tree', 'next-router-prefetch',
    'next-router-segment-prefetch', 'next-url', 'x-middleware-prefetch']);

/** The response header that says what the cache did; its values are read by load-testing's k6, so they are fixed. */
export const CACHE_STATE_HEADER = 'x-storefront-cache';

/** Marks the cache's own request for a fresh copy of a stale page; honoured from a loopback address only. */
export const REVALIDATE_HEADER = 'x-storefront-revalidate';

/** The dev/QA theme and colour overrides (`src/shell/theme/override.ts`): a page under one is never shared. */
export const OVERRIDE_COOKIES = Object.freeze(['storefront-theme', 'storefront-color']);

/** The query parameters that make a request one editor's or one tester's, never a shopper's. */
export const OVERRIDE_PARAMS = Object.freeze(['theme', 'color', 'preview']);

/** The environment variable that holds a whole policy as JSON, merged over this table. */
export const POLICY_JSON_VARIABLE = 'STOREFRONT_CACHE_POLICY_JSON';

export const ENV_PREFIX = 'STOREFRONT_CACHE_';

export const DEFAULT_POLICY = Object.freeze({
    enabled: true,
    store: 'memory',
    maxMb: 64,
    maxEntryKb: 1024,
    debug: false,
    statsToken: '',
    evictionLogThreshold: 100,
    /** Dropped from the key: a tracking parameter changes no byte of the document. `x*` is a prefix. */
    dropParams: ['utm_*', 'fbclid', 'gclid', 'msclkid', '_ga', 'ref'],
    classes: {
        'api-theme-manifest': {raw: true, match: ['/api/theme-manifest'], enabled: false, edge: 'passthrough'},
        'next-internal': {raw: true, match: ['/_next/*', '/api/*', '/store-not-found', '/t/*'], enabled: false,
            edge: 'passthrough'},
        seo: {raw: true, match: ['/sitemap.xml', '/robots.txt'], enabled: true, ttl: 600, swr: 3600,
            vary: ['host', 'store-id', 'path'], edge: 'public', share: 0.05},
        home: {match: ['/'], enabled: true, ttl: 30, swr: 300, vary: PAGE_VARY, edge: 'public', share: 0.2},
        category: {match: ['/category/*'], enabled: true, ttl: 30, swr: 300, vary: PAGE_VARY, edge: 'public',
            share: 0.25},
        product: {match: ['/product/*'], enabled: true, ttl: 20, swr: 120, vary: PAGE_VARY, edge: 'public',
            share: 0.25},
        search: {match: ['/search'], enabled: true, ttl: 15, swr: 60, vary: PAGE_VARY, edge: 'public', share: 0.1},
        content: {match: ['/content/*', '/blog', '/blog/*'], enabled: true, ttl: 60, swr: 600, vary: PAGE_VARY,
            edge: 'public', share: 0.1},
        help: {match: ['/help', '/policies/*'], enabled: true, ttl: 300, swr: 3600, vary: PAGE_VARY, edge: 'public',
            share: 0.05},
        shopper: {match: ['/login', '/register', '/customer/*', '/checkout/*', '/callback'], enabled: false,
            edge: 'private'},
        default: {match: [], enabled: false, edge: 'private'},
    },
});

/** The request headers a loopback refresh carries, so the render is the one a shopper of that host would get. */
export const FORWARDED_HEADERS = Object.freeze(['host', 'x-forwarded-host', 'x-forwarded-proto', 'store-id', 'theme',
    'color-theme', 'default-language', 'supported-languages', 'accept', 'accept-language']);

/** The cache's own route: a JSON snapshot of the policy, the store and the counters. */
export const STATS_PATH = '/_storefront/cache/stats';

/** The header that authorises the stats route from beyond loopback, when `statsToken` is set. */
export const STATS_TOKEN_HEADER = 'x-storefront-cache-token';

/** next-intl's locale cookie: the one Set-Cookie a kept page may carry, replayed only to a shopper whose differs. */
export const NEXT_LOCALE = 'NEXT_LOCALE';

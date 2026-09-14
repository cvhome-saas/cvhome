/**
 * Anonymous storefront pages served from memory instead of rendered again.
 *
 * Why: a render costs ~70 ms of a Fargate vCPU, so one 0.5 vCPU task tops out near 7 page views a second, and every
 * route is dynamic: in the 2026-09-14 spike 500 shoppers held landing-ui at its cap for 2m15s and the median home page
 * took 42 s. Yet the server render reads no per-shopper state: the store, its theme and languages arrive as spg's
 * headers, the locale is in the URL, and cart and customer data are fetched by the browser. So a page is a function of
 * the host, spg's store headers, the URL and Next's own Vary headers, and every shopper asking for the same one within
 * a few seconds can be sent the same bytes.
 *
 * What it will not hold (served straight from Next, `x-storefront-cache: bypass`):
 * - anything but GET and HEAD, anything with an Authorization header;
 * - a dev/QA theme or colour override (its cookies, or the ?theme= / ?color= that set them), a ?preview= draft;
 * - the routes that read the shopper's cookies or session: login, register, customer, checkout, callback;
 * - Next's own assets and API routes (`/_next/`, `/api/`).
 * And what it will not keep: anything but a 200, a response that sets a cookie other than next-intl's NEXT_LOCALE, a
 * response that varies on a header it does not key on, a body over a quarter of the budget.
 *
 * Freshness: a page is served for `ttlMs` (`hit`). For `staleMs` after that it is still served (`stale`) while the
 * first request to find it stale renders it again; past both it is rendered as if never seen (`miss`). Requests that
 * miss while the same page is already rendering wait for that render and share it (`shared`), so a spike on a cold
 * page renders it once. Memory is bounded by `maxBytes`, least recently used first out. Each task keeps its own copy.
 */

/** spg's headers that make a page the page of one store (MerchantRoutingService.mapHeaders), and where it is served. */
const STORE_HEADERS = ['host', 'x-forwarded-host', 'x-forwarded-proto', 'store-id', 'theme', 'color-theme',
    'default-language', 'supported-languages'];

/** The request headers Next varies an App Router response on: the RSC payload of a client navigation or prefetch. */
const NEXT_VARY = ['rsc', 'next-router-state-tree', 'next-router-prefetch', 'next-router-segment-prefetch', 'next-url'];

/** A response that varies on anything else is not kept: its key would not tell its variants apart. */
const KEYED = new Set(NEXT_VARY);

const OVERRIDE_COOKIES = ['storefront-theme', 'storefront-color'];

const OVERRIDE_PARAMS = ['theme', 'color', 'preview'];

const SHOPPER_ROUTES = /^\/(?:[a-z]{2}(?:-[A-Za-z]{2})?\/)?(?:t\/[^/]+\/[^/]+\/)?(?:login|register|customer|checkout|callback)(?:[/?]|$)/;

const NEXT_LOCALE = 'NEXT_LOCALE';

/** Response headers never replayed: they describe one connection, or are recomputed for the copy being sent. */
const HOP_HEADERS = new Set(['connection', 'keep-alive', 'transfer-encoding', 'date', 'content-length', 'set-cookie']);

const SHARED_WAIT_MS = 10_000;

/** The cache's settings from STOREFRONT_PAGE_CACHE_* (seconds, megabytes); a TTL of 0 turns it off. */
export function pageCacheSettings(env) {
    const seconds = (name, fallback) => {
        const value = Number(env[name]);
        return env[name] !== undefined && env[name] !== '' && Number.isFinite(value) && value >= 0 ? value : fallback;
    };
    return {
        ttlMs: seconds('STOREFRONT_PAGE_CACHE_TTL_SECONDS', 30) * 1000,
        staleMs: seconds('STOREFRONT_PAGE_CACHE_STALE_SECONDS', 300) * 1000,
        maxBytes: seconds('STOREFRONT_PAGE_CACHE_MAX_MB', 64) * 1024 * 1024,
    };
}

export function createPageCache({ttlMs, staleMs, maxBytes, now = Date.now}) {
    const entries = new Map();
    const rendering = new Map();
    let bytes = 0;

    function evict(key) {
        const entry = entries.get(key);
        if (entry) {
            bytes -= entry.body.length;
            entries.delete(key);
        }
    }

    function store(key, entry) {
        evict(key);
        entries.set(key, entry);
        bytes += entry.body.length;
        for (const oldest of entries.keys()) {
            if (bytes <= maxBytes) {
                break;
            }
            evict(oldest);
        }
    }

    function lookup(key) {
        const entry = entries.get(key);
        if (!entry) {
            return undefined;
        }
        const age = now() - entry.storedAt;
        if (age >= ttlMs + staleMs) {
            evict(key);
            return undefined;
        }
        entries.delete(key);
        entries.set(key, entry);
        return {entry, fresh: age < ttlMs};
    }

    /** Renders through {@code next}, sending the page as Next streams it and keeping a copy if it may be kept. */
    function renderAndKeep(req, res, key, next) {
        let settle;
        const shared = new Promise(resolve => {
            settle = resolve;
        });
        rendering.set(key, shared);
        const finish = entry => {
            if (rendering.get(key) === shared) {
                rendering.delete(key);
            }
            settle(entry);
        };
        const chunks = [];
        let size = 0;
        let inline = {};
        const {write, end, writeHead} = res;
        const keep = (chunk, encoding) => {
            if (chunk === undefined || chunk === null || typeof chunk === 'function') {
                return;
            }
            const buffer = typeof chunk === 'string'
                ? Buffer.from(chunk, typeof encoding === 'string' ? encoding : 'utf8')
                : Buffer.from(chunk);
            size += buffer.length;
            if (size <= maxBytes / 4) {
                chunks.push(buffer);
            }
        };
        res.writeHead = function (status, ...rest) {
            const headers = rest.find(arg => arg && typeof arg === 'object');
            if (headers) {
                inline = Array.isArray(headers) ? {} : headers;
            }
            return writeHead.call(this, status, ...rest);
        };
        res.write = function (chunk, encoding, ...rest) {
            keep(chunk, encoding);
            return write.call(this, chunk, encoding, ...rest);
        };
        res.end = function (chunk, encoding, ...rest) {
            keep(chunk, encoding);
            return end.call(this, chunk, encoding, ...rest);
        };
        res.once('finish', () => {
            const headers = {...lower(inline), ...lower(res.getHeaders())};
            const entry = keepable(res.statusCode, headers, size) ? {
                status: res.statusCode,
                headers: replayable(headers),
                locale: localeCookieOf(headers),
                body: Buffer.concat(chunks),
                storedAt: now(),
            } : undefined;
            if (entry) {
                store(key, entry);
            }
            finish(entry);
        });
        res.once('close', () => finish(undefined));
        res.setHeader('x-storefront-cache', entries.has(key) ? 'stale-refresh' : 'miss');
        return next();
    }

    function keepable(status, headers, size) {
        if (status !== 200 || size > maxBytes / 4) {
            return false;
        }
        const cookies = [].concat(headers['set-cookie'] ?? []);
        if (cookies.some(cookie => !String(cookie).startsWith(`${NEXT_LOCALE}=`))) {
            return false;
        }
        const vary = String(headers.vary ?? '').toLowerCase().split(',').map(name => name.trim()).filter(Boolean);
        return vary.every(name => KEYED.has(name));
    }

    function send(req, res, entry, state) {
        const headers = {...entry.headers, 'x-storefront-cache': state, 'content-length': entry.body.length};
        // next-intl remembers the locale in a cookie; send it as Next would have, when the shopper's differs.
        if (entry.locale && cookieOf(req, NEXT_LOCALE) !== entry.locale.value) {
            headers['set-cookie'] = entry.locale.header;
        }
        res.writeHead(entry.status, headers);
        res.end(req.method === 'HEAD' ? undefined : entry.body);
    }

    async function serve(req, res, next) {
        if (ttlMs <= 0 || !admissible(req)) {
            res.setHeader('x-storefront-cache', 'bypass');
            return next();
        }
        const key = keyOf(req);
        const found = lookup(key);
        if (found?.fresh) {
            return send(req, res, found.entry, 'hit');
        }
        if (found && rendering.has(key)) {
            return send(req, res, found.entry, 'stale');
        }
        if (!found && rendering.has(key)) {
            const entry = await Promise.race([rendering.get(key),
                new Promise(resolve => setTimeout(resolve, SHARED_WAIT_MS).unref())]);
            if (entry) {
                return send(req, res, entry, 'shared');
            }
        }
        if (req.method === 'HEAD') {
            res.setHeader('x-storefront-cache', 'bypass');
            return next();
        }
        return renderAndKeep(req, res, key, next);
    }

    return {serve, size: () => entries.size, bytes: () => bytes};
}

function admissible(req) {
    if (req.method !== 'GET' && req.method !== 'HEAD') {
        return false;
    }
    if (req.headers.authorization) {
        return false;
    }
    const url = new URL(req.url, 'http://storefront');
    if (url.pathname.startsWith('/_next/') || url.pathname.startsWith('/api/') || SHOPPER_ROUTES.test(url.pathname)) {
        return false;
    }
    if (OVERRIDE_PARAMS.some(param => url.searchParams.has(param))) {
        return false;
    }
    return !OVERRIDE_COOKIES.some(cookie => cookieOf(req, cookie) !== undefined);
}

function keyOf(req) {
    const values = [...STORE_HEADERS, ...NEXT_VARY].map(name => `${name}=${req.headers[name] ?? ''}`);
    return `${req.url}\n${values.join('\n')}`;
}

function cookieOf(req, name) {
    for (const part of String(req.headers.cookie ?? '').split(';')) {
        const at = part.indexOf('=');
        if (at > 0 && part.slice(0, at).trim() === name) {
            return part.slice(at + 1).trim();
        }
    }
    return undefined;
}

function localeCookieOf(headers) {
    const header = [].concat(headers['set-cookie'] ?? []).map(String).find(c => c.startsWith(`${NEXT_LOCALE}=`));
    return header ? {header, value: header.slice(NEXT_LOCALE.length + 1).split(';')[0]} : undefined;
}

function lower(headers) {
    return Object.fromEntries(Object.entries(headers ?? {}).map(([name, value]) => [name.toLowerCase(), value]));
}

function replayable(headers) {
    return Object.fromEntries(Object.entries(headers).filter(([name]) => !HOP_HEADERS.has(name)
        && name !== 'x-storefront-cache'));
}

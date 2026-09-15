/**
 * Anonymous storefront pages served from memory instead of rendered again.
 *
 * Why: a render costs ~70 ms of a Fargate vCPU, so one 0.5 vCPU task tops out near 7 page views a second, and every
 * route is dynamic: in the 2026-09-14 spike 500 shoppers held landing-ui at its cap for 2m15s and the median home page
 * took 42 s. Yet the server render reads no per-shopper state: the store, its theme and languages arrive as spg's
 * headers, the locale is in the URL, and cart and customer data are fetched by the browser. So a document is a function
 * of the host, spg's store headers and the URL, and every shopper asking for the same one within a few seconds can be
 * sent the same bytes.
 *
 * What it will not hold (served straight from Next, `x-storefront-cache: bypass`):
 * - anything but GET and HEAD, anything with an Authorization header;
 * - a client navigation or prefetch (Next's `rsc` request headers): its payload is keyed to that navigation's router
 *   state, so no second shopper would ever hit it, and its key alone would outweigh the page;
 * - a dev/QA theme or colour override (its cookies, or the ?theme= / ?color= that set them), a ?preview= draft;
 * - the routes that read the shopper's cookies or session: login, register, customer, checkout, callback;
 * - Next's own assets and API routes (`/_next/`, `/api/`).
 * And what it will not keep: anything but a 200, an HTML body that did not reach its `</html>` (Next cannot change the
 * status once the shell is out), a render one of whose backend reads was aborted or timed out (a page missing a
 * section, which is what libs/services' orUndefined turns a failed read into), a response that sets a cookie other
 * than next-intl's NEXT_LOCALE, a response that varies on a header it does not key on, a body over a quarter of the
 * budget.
 *
 * Freshness: a page is served for `ttlMs` (`hit`). For `staleMs` after that it is still served (`stale`) while a
 * request of the cache's own, to this same server (`revalidate`), renders it again in the background, so no shopper
 * waits on a refresh; past both it is rendered as if never seen (`miss`). Requests that miss while the same page is
 * already rendering wait for that render and share it (`shared`), so a spike on a cold page renders it once; and a
 * render that shoppers are waiting on is not stopped because the one who started it left. Memory is bounded by
 * `maxBytes`, least recently used first out. Each task keeps its own copy.
 */
import http from 'node:http';

/** spg's headers that make a page the page of one store (MerchantRoutingService.mapHeaders), and where it is served. */
const STORE_HEADERS = ['host', 'x-forwarded-host', 'x-forwarded-proto', 'store-id', 'theme', 'color-theme',
    'default-language', 'supported-languages'];

/** The request headers of an App Router client navigation or prefetch: Next answers those with an RSC payload. */
const NEXT_VARY = ['rsc', 'next-router-state-tree', 'next-router-prefetch', 'next-router-segment-prefetch', 'next-url'];

/** A response that varies on anything else is not kept: its key would not tell its variants apart. */
const KEYED = new Set(NEXT_VARY);

const OVERRIDE_COOKIES = ['storefront-theme', 'storefront-color'];

const OVERRIDE_PARAMS = ['theme', 'color', 'preview'];

const SHOPPER_ROUTES = /^\/(?:[a-z]{2}(?:-[A-Za-z]{2})?\/)?(?:t\/[^/]+\/[^/]+\/)?(?:login|register|customer|checkout|callback)(?:[/?]|$)/;

const NEXT_LOCALE = 'NEXT_LOCALE';

/** Marks the cache's own request for a fresh copy of a stale page; it renders whatever the cache holds. */
export const REVALIDATE_HEADER = 'x-storefront-revalidate';

/** Response headers never replayed: they describe one connection, or are recomputed for the copy being sent. */
const HOP_HEADERS = new Set(['connection', 'keep-alive', 'transfer-encoding', 'date', 'content-length', 'set-cookie']);

/** The cache's settings from STOREFRONT_PAGE_CACHE_* (seconds, megabytes); a TTL of 0 turns it off. */
export function pageCacheSettings(env) {
    const number = (name, fallback) => {
        const value = Number(env[name]);
        return env[name] !== undefined && env[name] !== '' && Number.isFinite(value) && value >= 0 ? value : fallback;
    };
    return {
        ttlMs: number('STOREFRONT_PAGE_CACHE_TTL_SECONDS', 30) * 1000,
        staleMs: number('STOREFRONT_PAGE_CACHE_STALE_SECONDS', 300) * 1000,
        maxBytes: number('STOREFRONT_PAGE_CACHE_MAX_MB', 64) * 1024 * 1024,
    };
}

/**
 * Refreshes a stale page by asking this same server for it again, with the store headers that key it and the
 * revalidate marker, and discarding the answer: the cache keeps the copy on the way out. `port` is read on each call,
 * since the server is listening only after start.
 */
export function loopbackRevalidator(port) {
    return req => new Promise((resolve, reject) => {
        const headers = {[REVALIDATE_HEADER]: '1'};
        for (const name of [...STORE_HEADERS, 'accept']) {
            if (req.headers[name] !== undefined) {
                headers[name] = req.headers[name];
            }
        }
        const refresh = http.request({host: '127.0.0.1', port: port(), path: req.url, method: 'GET', headers},
            response => {
                response.resume();
                response.once('end', resolve);
                response.once('error', reject);
            });
        refresh.once('error', reject);
        refresh.end();
    });
}

/**
 * @param revalidate refreshes a stale page in the background (loopbackRevalidator); without one the first request to
 *                   find a page stale renders it in line (`stale-refresh`)
 * @param degraded   whether the render behind {@code res} had a backend read aborted or timed out (request-scope.mjs)
 */
export function createPageCache({ttlMs, staleMs, maxBytes, now = Date.now, revalidate, degraded = () => false,
    log = console}) {
    const entries = new Map();
    const rendering = new Map();
    const refreshing = new Set();
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

    /**
     * Renders through {@code next}, sending the page as Next streams it and keeping a copy if it may be kept. `next`
     * is told whether shoppers other than the one who asked are waiting on this render, so their leaving is what ends
     * it, not the first one's.
     */
    function renderAndKeep(req, res, key, next) {
        let settle;
        const inflight = {waiters: new Set(), promise: new Promise(resolve => {
            settle = resolve;
        })};
        rendering.set(key, inflight);
        const finish = entry => {
            if (rendering.get(key) === inflight) {
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
                inline = headersOf(headers);
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
            // A header set with setHeader and again in writeHead: Node sends writeHead's, so the copy keeps that one.
            const headers = {...lower(res.getHeaders()), ...lower(inline)};
            const body = Buffer.concat(chunks);
            const entry = keepable(res, headers, size, body) ? {
                status: res.statusCode,
                headers: replayable(headers),
                locale: localeCookieOf(headers),
                body,
                storedAt: now(),
            } : undefined;
            if (entry) {
                store(key, entry);
            }
            finish(entry);
        });
        res.once('close', () => finish(undefined));
        res.setHeader('x-storefront-cache', entries.has(key) ? 'stale-refresh' : 'miss');
        const shared = () => [...inflight.waiters].some(waiter => !waiter.destroyed);
        return new Promise(resolve => resolve(next(shared))).catch(error => {
            // Next answers its own errors; this is for a listener that threw before it could, so that the shoppers
            // waiting on this render, and this one, are not left hanging.
            log.error(`[page-cache] rendering ${req.url} failed: ${error?.stack ?? error}`);
            finish(undefined);
            if (!res.headersSent) {
                res.statusCode = 500;
            }
            res.end();
        });
    }

    function keepable(res, headers, size, body) {
        if (res.statusCode !== 200 || size > maxBytes / 4 || degraded(res)) {
            return false;
        }
        const cookies = [].concat(headers['set-cookie'] ?? []);
        if (cookies.some(cookie => !String(cookie).startsWith(`${NEXT_LOCALE}=`))) {
            return false;
        }
        const vary = String(headers.vary ?? '').toLowerCase().split(',').map(name => name.trim()).filter(Boolean);
        if (!vary.every(name => KEYED.has(name))) {
            return false;
        }
        return !String(headers['content-type'] ?? '').startsWith('text/html') || whole(body);
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

    /** One refresh of a stale page at a time; a refresh that fails leaves the stale copy to serve out its window. */
    function refresh(req, key) {
        if (!revalidate || rendering.has(key) || refreshing.has(key)) {
            return;
        }
        refreshing.add(key);
        revalidate(req)
            .catch(error => log.warn(`[page-cache] revalidating ${req.url} failed: ${error?.message ?? error}`))
            .finally(() => refreshing.delete(key));
    }

    /** What to do with a request: send a kept copy, or render it (and whether it may fill the cache). */
    async function decide(req, res) {
        if (ttlMs <= 0 || !admissible(req)) {
            return {render: 'bypass'};
        }
        const key = keyOf(req);
        if (req.headers[REVALIDATE_HEADER] === '1') {
            return {render: 'keep', key};
        }
        const found = lookup(key);
        if (found?.fresh) {
            return {send: found.entry, state: 'hit'};
        }
        if (found) {
            refresh(req, key);
            if (rendering.has(key) || refreshing.has(key)) {
                return {send: found.entry, state: 'stale'};
            }
        } else if (rendering.has(key)) {
            const inflight = rendering.get(key);
            inflight.waiters.add(res);
            const entry = await inflight.promise;
            if (entry) {
                return {send: entry, state: 'shared'};
            }
        }
        return {render: req.method === 'HEAD' ? 'bypass' : 'keep', key};
    }

    async function serve(req, res, next) {
        let action;
        try {
            action = await decide(req, res);
        } catch (error) {
            log.error(`[page-cache] ${req.method} ${req.url}: ${error?.stack ?? error}`);
            action = {render: 'bypass'};
        }
        if (action.send) {
            return send(req, res, action.send, action.state);
        }
        if (action.render === 'keep') {
            return renderAndKeep(req, res, action.key, next);
        }
        res.setHeader('x-storefront-cache', 'bypass');
        return next(() => false);
    }

    return {serve, size: () => entries.size, bytes: () => bytes};
}

function admissible(req) {
    if (req.method !== 'GET' && req.method !== 'HEAD') {
        return false;
    }
    if (req.headers.authorization || NEXT_VARY.some(name => req.headers[name] !== undefined)) {
        return false;
    }
    const path = String(req.url ?? '').split('?')[0];
    if (path.startsWith('//') || path.startsWith('/_next/') || path.startsWith('/api/') || SHOPPER_ROUTES.test(path)) {
        return false;
    }
    const url = new URL(req.url, 'http://storefront');
    if (OVERRIDE_PARAMS.some(param => url.searchParams.has(param))) {
        return false;
    }
    return !OVERRIDE_COOKIES.some(cookie => cookieOf(req, cookie) !== undefined);
}

function keyOf(req) {
    return `${req.url}\n${STORE_HEADERS.map(name => `${name}=${req.headers[name] ?? ''}`).join('\n')}`;
}

/** An HTML document Next finished: a stream that failed after the shell was flushed stops short of this. */
function whole(body) {
    const tail = body.subarray(Math.max(0, body.length - 64)).toString('utf8').trimEnd();
    return tail.endsWith('</html>');
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

/** writeHead takes headers as an object or as the flat `[name, value, …]` list; both read the same here. */
function headersOf(headers) {
    if (!Array.isArray(headers)) {
        return headers;
    }
    const plain = {};
    for (let i = 0; i + 1 < headers.length; i += 2) {
        const key = String(headers[i]).toLowerCase();
        plain[key] = plain[key] === undefined ? headers[i + 1] : [].concat(plain[key], headers[i + 1]);
    }
    return plain;
}

function lower(headers) {
    return Object.fromEntries(Object.entries(headers ?? {}).map(([name, value]) => [name.toLowerCase(), value]));
}

function replayable(headers) {
    return Object.fromEntries(Object.entries(headers).filter(([name]) => !HOP_HEADERS.has(name)
        && name !== 'x-storefront-cache'));
}

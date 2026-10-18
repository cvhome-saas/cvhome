/**
 * Anonymous storefront pages served from a store instead of rendered again.
 *
 * Why: a render costs ~70 ms of a Fargate vCPU, so one 0.5 vCPU task tops out near 7 page views a second, and every
 * route is dynamic: in the 2026-09-14 spike 500 shoppers held landing-ui at its cap for 2m15s and the median home page
 * took 42 s. Yet the server render reads no per-shopper state: the store, its theme and languages arrive as spg's
 * headers, the locale is in the URL, and cart and customer data are fetched by the browser. So a document is a function
 * of a few request parts (`key.mjs`), and every shopper asking for the same one within its class's seconds can be
 * sent the same bytes.
 *
 * What happens to a request, and what `x-storefront-cache` says about it:
 * - `bypass`: the policy says it cannot be kept (`policy.mjs` classify: a write, a credential, a client navigation, an
 *   override, a shopper route, a disabled class); rendered by Next as if the cache were not there, its edge header
 *   still set;
 * - `hit`: a copy within its class's fresh seconds;
 * - `stale`: a copy past them but within its stale seconds, sent at once while a request of the cache's own to this
 *   same server (`revalidate.mjs`) renders it again in the background;
 * - `stale-refresh`: a stale copy exists but there is no revalidator, so this request renders it in line;
 * - `shared`: it missed while the same page was already rendering, waited, and was sent that render;
 * - `miss`: rendered, and kept if `capture.mjs` allows.
 * A render other shoppers are waiting on is not stopped because the one who started it left (`keepAlive`).
 */
import {CACHE_STATE_HEADER, NEXT_LOCALE, STATS_PATH, STATS_TOKEN_HEADER} from './config.mjs';
import {classify, cookieOf} from './policy.mjs';
import {buildKey} from './key.mjs';
import {captureRender} from './capture.mjs';
import {edgeHeadersFor, enforceEdgeHeaders, presetEdgeHeaders} from './edge-headers.mjs';
import {fromLoopback, isRevalidation} from './revalidate.mjs';

const NO_METRICS = {record: () => undefined, snapshot: () => ({})};

/** Bypass reasons that are about the page, not about what this request carries: the class's edge header applies. */
const SHARED_BYPASS = new Set(['class-disabled', 'cache-disabled', 'head']);

const PRIVATE_RULE = {enabled: false, edge: 'private, no-store', vary: []};

/**
 * @param policy     from `policy.mjs` loadPolicy
 * @param store      a CacheStore (`store.mjs`)
 * @param revalidate refreshes a stale page in the background (`revalidate.mjs`); without one the first request to
 *                   find a page stale renders it in line
 * @param degraded   whether the render behind a response had a backend read aborted or timed out (request-scope.mjs)
 * @param metrics    `record(state, cls)` per request and `snapshot()` for the stats route (`metrics.mjs`)
 */
export function createPageCache({policy, store, revalidate, degraded = () => false, metrics = NO_METRICS, log = console,
    now = Date.now}) {
    const rendering = new Map();
    const refreshing = new Set();

    async function serve(req, res, next) {
        let action;
        try {
            action = await decide(req, res);
        } catch (error) {
            log.error(`[storefront-cache] ${req.method} ${req.url}: ${error?.stack ?? error}`);
            action = {render: 'bypass', reason: 'error'};
        }
        if (action.stats) {
            return sendStats(res);
        }
        if (action.send) {
            return send(req, res, action.classified, action.send, action.state);
        }
        if (action.render === 'keep') {
            return renderAndKeep(req, res, action.classified, action.key, action.stale, next);
        }
        return renderThrough(req, res, action.classified, action.reason, action.state, next);
    }

    /** What to do with a request: send a kept copy, or render it (and whether the render may fill the cache). */
    async function decide(req, res) {
        const raw = String(req.url ?? '');
        if ((raw === STATS_PATH || raw.startsWith(`${STATS_PATH}?`)) && statsAllowed(req)) {
            return {stats: true};
        }
        const classified = classify(req, policy);
        if (classified.bypass) {
            return {render: 'bypass', classified, reason: classified.bypass};
        }
        const key = buildKey(classified, req, policy);
        if (isRevalidation(req)) {
            return {render: 'keep', classified, key, stale: true};
        }
        const entry = await store.get(key);
        if (entry && now() - entry.storedAt < entry.ttlMs) {
            return {send: entry, classified, state: 'hit'};
        }
        if (entry) {
            refresh(req, key);
            if (rendering.has(key) || refreshing.has(key)) {
                return {send: entry, classified, state: 'stale'};
            }
        } else if (rendering.has(key)) {
            const inflight = rendering.get(key);
            inflight.waiters.add(res);
            const shared = await inflight.promise;
            if (shared) {
                return {send: shared, classified, state: 'shared'};
            }
        }
        if (req.method === 'HEAD') {
            return {render: 'bypass', classified, reason: 'head', state: 'miss'};
        }
        return {render: 'keep', classified, key, stale: entry !== undefined};
    }

    /** One refresh of a stale page at a time; a refresh that fails leaves the stale copy to serve out its window. */
    function refresh(req, key) {
        if (!revalidate || rendering.has(key) || refreshing.has(key)) {
            return;
        }
        refreshing.add(key);
        revalidate(req)
            .catch(error => log.warn(`[storefront-cache] revalidating ${req.url} failed: ${error?.message ?? error}`))
            .finally(() => refreshing.delete(key));
    }

    /**
     * Renders through Next without a copy: the state header and the edge header are still set. A request bypassed
     * for what it carries (a credential, an override, a client navigation, a write) is nobody else's page, so it
     * leaves `private, no-store` whatever its class; one bypassed for its class, or because the cache is off, leaves
     * with the class's value, which is what an edge needs.
     */
    function renderThrough(req, res, classified, reason, state = 'bypass', next) {
        if (classified) {
            const rule = SHARED_BYPASS.has(reason) ? classified.rule : PRIVATE_RULE;
            presetEdgeHeaders(res, rule);
            hookHead(res, status => enforceEdgeHeaders(res, rule, status));
            debugHeaders(res, classified, undefined, reason);
            metrics.record(state, classified.cls);
        }
        res.setHeader(CACHE_STATE_HEADER, state);
        return next(() => false);
    }

    /**
     * Renders through Next, sending the page as Next streams it and keeping a copy if it may be kept. `next` is told
     * whether shoppers other than the one who asked are waiting on this render, so their leaving is what ends it.
     */
    function renderAndKeep(req, res, classified, key, stale, next) {
        const {rule, cls} = classified;
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
        const state = stale ? 'stale-refresh' : 'miss';
        presetEdgeHeaders(res, rule);
        res.setHeader(CACHE_STATE_HEADER, state);
        debugHeaders(res, classified, key);
        metrics.record(state, cls);
        captureRender(res, {maxEntryBytes: policy.maxEntryBytes, degraded, now, cls, ttlMs: rule.ttlMs,
            staleMs: rule.swrMs, onHead: status => enforceEdgeHeaders(res, rule, status)})
            .then(async entry => {
                if (entry) {
                    await store.set(key, entry);
                }
                finish(entry);
            })
            .catch(error => {
                log.error(`[storefront-cache] keeping ${req.url} failed: ${error?.stack ?? error}`);
                finish(undefined);
            });
        const shared = () => [...inflight.waiters].some(waiter => !waiter.destroyed);
        return new Promise(resolve => resolve(next(shared))).catch(error => {
            // Next answers its own errors; this is for a listener that threw before it could, so that the shoppers
            // waiting on this render, and this one, are not left hanging.
            log.error(`[storefront-cache] rendering ${req.url} failed: ${error?.stack ?? error}`);
            finish(undefined);
            if (!res.headersSent) {
                res.statusCode = 500;
            }
            res.end();
        });
    }

    function send(req, res, classified, entry, state) {
        const headers = {...entry.headers, ...edgeHeadersFor(entry, classified.rule), [CACHE_STATE_HEADER]: state,
            'content-length': entry.body.length};
        // next-intl remembers the locale in a cookie; send it as Next would have, when the shopper's differs.
        if (entry.locale && cookieOf(req, NEXT_LOCALE) !== entry.locale.value) {
            headers['set-cookie'] = entry.locale.header;
        }
        if (policy.debug) {
            headers['x-storefront-cache-class'] = classified.cls;
            headers['x-storefront-cache-key'] = buildKey(classified, req, policy);
        }
        metrics.record(state, classified.cls);
        res.writeHead(entry.status, headers);
        res.end(req.method === 'HEAD' ? undefined : entry.body);
    }

    function debugHeaders(res, classified, key, reason) {
        if (!policy.debug) {
            return;
        }
        res.setHeader('x-storefront-cache-class', classified.cls);
        if (key) {
            res.setHeader('x-storefront-cache-key', key);
        }
        if (reason) {
            res.setHeader('x-storefront-cache-reason', reason);
        }
    }

    function statsAllowed(req) {
        if (fromLoopback(req)) {
            return true;
        }
        return policy.statsToken !== '' && req.headers[STATS_TOKEN_HEADER] === policy.statsToken;
    }

    function sendStats(res) {
        const body = JSON.stringify(snapshot(), null, 2);
        res.writeHead(200, {'content-type': 'application/json; charset=utf-8', 'cache-control': 'private, no-store',
            'content-length': Buffer.byteLength(body)});
        res.end(body);
    }

    function snapshot() {
        return {
            enabled: policy.enabled,
            store: {kind: policy.store, entries: store.size(), bytes: store.bytes(), maxBytes: policy.maxBytes,
                byClass: store.byClass?.() ?? {}},
            rendering: rendering.size,
            refreshing: refreshing.size,
            ...metrics.snapshot(),
        };
    }

    return {serve, snapshot};
}

/** Runs `onHead(status)` when the head is written, for a render that is not captured. */
function hookHead(res, onHead) {
    const {writeHead} = res;
    res.writeHead = function (status, ...rest) {
        const at = rest.findIndex(arg => arg && typeof arg === 'object');
        if (at >= 0) {
            const given = rest[at];
            const entries = Array.isArray(given) ? pairs(given) : Object.entries(given);
            for (const [name, value] of entries) {
                this.setHeader(name, value);
            }
            rest.splice(at, 1);
        }
        onHead(status);
        return writeHead.call(this, status, ...rest);
    };
}

function pairs(flat) {
    const result = [];
    for (let i = 0; i + 1 < flat.length; i += 2) {
        result.push([flat[i], flat[i + 1]]);
    }
    return result;
}

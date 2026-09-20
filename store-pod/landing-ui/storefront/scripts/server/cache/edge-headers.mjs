/**
 * What a document tells the caches between it and the shopper.
 *
 * Next sets `Cache-Control: private, no-cache, no-store, max-age=0, must-revalidate` on every dynamic render unless
 * one is already set (`send-payload.js`), and every storefront route is dynamic, so nothing could ever be kept
 * outside this process. The class's `edge` value is set before the render (`preset`, which Next then keeps) and
 * enforced as the head is written (`enforce`): a 200 leaves with the class's value, anything else with
 * `private, no-store`, since an error page or a 404 must not be kept anywhere. A class whose `edge` is null
 * (`passthrough`) is left alone: the theme manifest route sets its own.
 *
 * `Vary` is Next's own list (its RSC headers) plus the store headers the class keys on, so a cache between spg and
 * this process keys a document the way this cache does. The host, the locale, the path and the query are the
 * URL's, which every cache keys on already.
 */

const PRIVATE = 'private, no-store';

const VARY_HEADERS = new Set(['store-id', 'theme', 'color-theme', 'default-language', 'supported-languages']);

/** Sets the class's Cache-Control before the render, so Next keeps it. */
export function presetEdgeHeaders(res, rule) {
    if (rule.edge) {
        res.setHeader('cache-control', rule.edge);
    }
}

/** Makes the head about to be written say what the class says: called from the writeHead hook, and on a replay. */
export function enforceEdgeHeaders(res, rule, status) {
    if (!rule.edge) {
        return;
    }
    res.setHeader('cache-control', status === 200 ? rule.edge : PRIVATE);
    if (status === 200 && rule.enabled) {
        res.setHeader('vary', mergeVary(res.getHeader('vary'), rule.vary.filter(part => VARY_HEADERS.has(part))));
    }
}

/** The headers a replayed copy carries, computed the same way. */
export function edgeHeadersFor(entry, rule) {
    if (!rule.edge) {
        return {};
    }
    const headers = {'cache-control': entry.status === 200 ? rule.edge : PRIVATE};
    if (entry.status === 200 && rule.enabled) {
        headers.vary = mergeVary(entry.headers.vary, rule.vary.filter(part => VARY_HEADERS.has(part)));
    }
    return headers;
}

/** Next's list and the class's, each name once, in the order first seen. */
export function mergeVary(existing, extra) {
    const seen = new Set();
    const names = [];
    for (const name of [...String(existing ?? '').split(','), ...extra]) {
        const trimmed = name.trim();
        if (trimmed && !seen.has(trimmed.toLowerCase())) {
            seen.add(trimmed.toLowerCase());
            names.push(trimmed);
        }
    }
    return names.join(', ');
}

/**
 * The key of a page: the parts of the request the class says the document is a function of, and nothing else.
 *
 * `v1|<class>|host=…|store=…|theme=…|color=…|locale=…|path=…|q=…`. The host is what the shopper typed
 * (`x-forwarded-host` from spg, else `host`); the store, theme and colour are spg's headers for that host
 * (`domain_lookup`), so two stores on one task never share a key, and a store whose theme changes gets a new one. The
 * query is normalised: parameters sorted by name then value, the tracking ones dropped, so `?b=2&a=1&utm_source=x`
 * and `?a=1&b=2` are the same page. The version prefix lets a later key format coexist in a shared store.
 */

const PARTS = {
    host: req => req.headers['x-forwarded-host'] ?? req.headers.host ?? '',
    'store-id': req => req.headers['store-id'] ?? '',
    theme: req => req.headers.theme ?? '',
    'color-theme': req => req.headers['color-theme'] ?? '',
    'default-language': req => req.headers['default-language'] ?? '',
    'supported-languages': req => req.headers['supported-languages'] ?? '',
    locale: (req, c) => c.locale ?? '',
    path: (req, c) => c.path,
    query: (req, c, policy) => normaliseQuery(c.search, policy.dropParams),
};

const SHORT = {host: 'host', 'store-id': 'store', theme: 'theme', 'color-theme': 'color', 'default-language': 'lang',
    'supported-languages': 'langs', locale: 'locale', path: 'path', query: 'q'};

export function buildKey(classified, req, policy) {
    const parts = [`v1`, classified.cls];
    for (const name of classified.rule.vary) {
        const read = PARTS[name];
        if (!read) {
            throw new Error(`[storefront-cache] class ${classified.cls} varies on ${JSON.stringify(name)}, which is not a request part`);
        }
        parts.push(`${SHORT[name]}=${String(read(req, classified, policy))}`);
    }
    return parts.join('|');
}

/** Sorted, with the dropped parameters removed; '' for none. A `drop` ending in `*` is a prefix. */
export function normaliseQuery(search, drop = []) {
    if (!search) {
        return '';
    }
    const dropped = name => drop.some(rule => rule.endsWith('*') ? name.startsWith(rule.slice(0, -1)) : name === rule);
    const pairs = [...new URLSearchParams(search)].filter(([name]) => !dropped(name));
    pairs.sort(([a, av], [b, bv]) => (a < b ? -1 : a > b ? 1 : av < bv ? -1 : av > bv ? 1 : 0));
    return new URLSearchParams(pairs).toString();
}

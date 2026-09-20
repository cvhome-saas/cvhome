/**
 * What the page cache reports: a counter per request state and class, gauges of what the store holds, a counter of
 * evictions with their reason, and one log line when evictions run away.
 *
 * The instruments are OpenTelemetry's, on the meter `storefront.cache`, exported by the SDK that
 * `instrumentation.ts` starts in this same process (`src/shell/telemetry.ts`, OTLP every 60 s). `@opentelemetry/api`
 * is loaded here by name so that a tree without it (a plain `node start.mjs` from a stripped image) still serves:
 * without the API there are no instruments, and the counters below still feed the stats route. Instruments made
 * before the SDK starts are the API's proxies, bound once it does, so nothing here waits on `register()`.
 *
 * The names: `storefront_page_cache_requests{state,class}`, `storefront_page_cache_entries{class}`,
 * `storefront_page_cache_bytes{class}`, `storefront_page_cache_evictions{class,reason}`.
 */
import {createRequire} from 'node:module';

const METER = 'storefront.cache';

const EVICTION_WINDOW_MS = 60_000;

export function createCacheMetrics({policy, store, log = console, api = loadApi(), now = Date.now} = {}) {
    const counters = {};
    const byClass = {};
    let evictions = 0;
    const instruments = api ? createInstruments(api, store) : undefined;
    const window = {startedAt: now(), count: 0, warned: false};

    store?.onEvict?.((key, entry, reason) => {
        evictions += 1;
        instruments?.evictions.add(1, {class: entry.cls, reason});
        if (reason === 'expired' || reason === 'replaced') {
            return;
        }
        const at = now();
        if (at - window.startedAt >= EVICTION_WINDOW_MS) {
            window.startedAt = at;
            window.count = 0;
            window.warned = false;
        }
        window.count += 1;
        const threshold = policy?.evictionLogThreshold ?? 0;
        if (threshold > 0 && window.count >= threshold && !window.warned) {
            window.warned = true;
            log.warn(`[storefront-cache] ${window.count} pages evicted for room in the last minute (store at `
                + `${store.bytes()} of ${policy.maxBytes} bytes): the cache is smaller than what shoppers ask for; raise `
                + `STOREFRONT_CACHE_MAX_MB or a class's share`);
        }
    });

    return {
        record(state, cls) {
            counters[state] = (counters[state] ?? 0) + 1;
            const held = byClass[cls] ?? (byClass[cls] = {});
            held[state] = (held[state] ?? 0) + 1;
            instruments?.requests.add(1, {state, class: cls});
        },
        snapshot() {
            return {counters: {...counters}, byClass: structuredClone(byClass), evictions};
        },
    };
}

function createInstruments(api, store) {
    const meter = api.metrics.getMeter(METER);
    const requests = meter.createCounter('storefront_page_cache_requests',
        {description: 'Storefront page requests by what the page cache did with them'});
    const evictions = meter.createCounter('storefront_page_cache_evictions',
        {description: 'Pages dropped from the page cache, by reason'});
    const entries = meter.createObservableGauge('storefront_page_cache_entries',
        {description: 'Pages the page cache holds'});
    const bytes = meter.createObservableGauge('storefront_page_cache_bytes', {description: 'Bytes the page cache holds',
        unit: 'By'});
    meter.addBatchObservableCallback(result => {
        const held = store?.byClass?.() ?? {};
        for (const [cls, {entries: count, bytes: size}] of Object.entries(held)) {
            result.observe(entries, count, {class: cls});
            result.observe(bytes, size, {class: cls});
        }
    }, [entries, bytes]);
    return {requests, evictions};
}

function loadApi() {
    try {
        return createRequire(import.meta.url)('@opentelemetry/api');
    } catch {
        return undefined;
    }
}

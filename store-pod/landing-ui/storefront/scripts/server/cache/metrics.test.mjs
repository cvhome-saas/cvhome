import assert from 'node:assert/strict';
import {test} from 'node:test';
import {createCacheMetrics} from './metrics.mjs';
import {MemoryStore} from './memory-store.mjs';

/** A stand-in for @opentelemetry/api: records every add and every observation. */
function fakeApi() {
    const adds = [];
    const callbacks = [];
    const instrument = name => ({name, add: (value, attributes) => adds.push([name, value, attributes])});
    return {
        adds,
        callbacks,
        observe() {
            const seen = [];
            for (const cb of callbacks) {
                cb({observe: (gauge, value, attributes) => seen.push([gauge.name, value, attributes])});
            }
            return seen;
        },
        metrics: {
            getMeter: () => ({
                createCounter: instrument,
                createObservableGauge: name => ({name}),
                addBatchObservableCallback: cb => callbacks.push(cb),
            }),
        },
    };
}

const entry = (cls, size) => ({status: 200, headers: {}, body: Buffer.alloc(size), cls, storedAt: 0, ttlMs: 1000, staleMs: 0});

test('requests are counted by state and class, for the instruments and for the snapshot alike', () => {
    const api = fakeApi();
    const metrics = createCacheMetrics({api, store: new MemoryStore({maxBytes: 100})});
    metrics.record('hit', 'home');
    metrics.record('hit', 'home');
    metrics.record('miss', 'product');
    assert.deepEqual(metrics.snapshot(), {counters: {hit: 2, miss: 1}, byClass: {home: {hit: 2}, product: {miss: 1}}, evictions: 0});
    assert.deepEqual(api.adds, [
        ['storefront_page_cache_requests', 1, {state: 'hit', class: 'home'}],
        ['storefront_page_cache_requests', 1, {state: 'hit', class: 'home'}],
        ['storefront_page_cache_requests', 1, {state: 'miss', class: 'product'}],
    ]);
});

test('the gauges observe what the store holds per class', async () => {
    const api = fakeApi();
    const store = new MemoryStore({maxBytes: 100});
    createCacheMetrics({api, store});
    await store.set('a', entry('home', 10));
    await store.set('b', entry('product', 5));
    await store.set('c', entry('product', 5));
    assert.deepEqual(api.observe().sort(), [
        ['storefront_page_cache_bytes', 10, {class: 'home'}],
        ['storefront_page_cache_bytes', 10, {class: 'product'}],
        ['storefront_page_cache_entries', 1, {class: 'home'}],
        ['storefront_page_cache_entries', 2, {class: 'product'}],
    ].sort());
});

test('evictions are counted with their reason, and one warning a minute says the cache is too small', async () => {
    const api = fakeApi();
    let clock = 0;
    const warnings = [];
    const store = new MemoryStore({maxBytes: 20, now: () => clock});
    const policy = {evictionLogThreshold: 2, maxBytes: 20};
    const metrics = createCacheMetrics({api, store, policy, log: {warn: message => warnings.push(message)}, now: () => clock});
    await store.set('a', entry('home', 10));
    await store.set('b', entry('home', 10));
    await store.set('c', entry('home', 10));
    await store.set('d', entry('home', 10));
    await store.set('e', entry('home', 10));
    assert.equal(metrics.snapshot().evictions, 3);
    assert.deepEqual(api.adds.filter(([name]) => name === 'storefront_page_cache_evictions').map(([, , attributes]) => attributes.reason),
        ['bytes', 'bytes', 'bytes']);
    assert.equal(warnings.length, 1, 'one warning for the window');
    assert.match(warnings[0], /evicted for room/);
    clock += 61_000;
    await store.set('f', entry('home', 10));
    await store.set('g', entry('home', 10));
    assert.equal(warnings.length, 2, 'a new window warns again');
});

test('without the OpenTelemetry API the counters and the snapshot still work', () => {
    const metrics = createCacheMetrics({api: undefined, store: new MemoryStore({maxBytes: 100})});
    metrics.record('bypass', 'shopper');
    assert.equal(metrics.snapshot().counters.bypass, 1);
});

test('the real API is found from here, and its instruments accept a value before any SDK starts', () => {
    const store = new MemoryStore({maxBytes: 100});
    const metrics = createCacheMetrics({store});
    metrics.record('hit', 'home');
    assert.equal(metrics.snapshot().counters.hit, 1);
});

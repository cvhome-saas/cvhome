import assert from 'node:assert/strict';
import http from 'node:http';
import {after, before, beforeEach, test} from 'node:test';
import {createPageCache} from './page-cache.mjs';
import {loadPolicy} from './policy.mjs';
import {createStore} from './store.mjs';
import {loopbackRevalidator} from './revalidate.mjs';
import {createCacheMetrics} from './metrics.mjs';
import {REVALIDATE_HEADER, STATS_PATH} from './config.mjs';
import {REVALIDATE_TOKEN} from './revalidate.mjs';

/** A stand-in for Next: counts renders, answers what the test asks for, with Next's Cache-Control and Vary. */
let renders = 0;
let answer = {};
let clock = 0;
let cache;
let server;
let base;
let keepAlives = [];
const degradedResponses = new WeakSet();

const STORE_1 = {'store-id': 'store-1', theme: 'starter', 'color-theme': 'LIGHT', 'default-language': 'en',
    'supported-languages': 'en,ar'};
const HTML = 'text/html; charset=utf-8';
const NEXT_VARY = 'RSC, Next-Router-State-Tree, Next-Router-Prefetch, Next-Router-Segment-Prefetch';
const NEXT_PRIVATE = 'private, no-cache, no-store, max-age=0, must-revalidate';

before(async () => {
    server = http.createServer((req, res) => cache.serve(req, res, async keepAlive => {
        renders += 1;
        keepAlives.push(keepAlive);
        const {status = 200, headers = {}, body = `<html>page ${req.url} for ${req.headers['store-id']}</html>`,
            delayMs = 0, degraded = false, inline, truncated = false} = answer;
        if (delayMs) {
            await new Promise(resolve => setTimeout(resolve, delayMs));
        }
        if (degraded) {
            degradedResponses.add(res);
        }
        res.statusCode = status;
        res.setHeader('content-type', HTML);
        res.setHeader('vary', NEXT_VARY);
        // What Next does: its own Cache-Control only when none is set yet.
        if (!res.getHeader('cache-control')) {
            res.setHeader('cache-control', NEXT_PRIVATE);
        }
        for (const [name, value] of Object.entries(headers)) {
            res.setHeader(name, value);
        }
        if (inline) {
            res.writeHead(status, inline);
        }
        res.write(body.slice(0, 5));
        if (truncated) {
            res.destroy();
            return;
        }
        res.end(body.slice(5));
    }));
    await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
    base = `http://127.0.0.1:${server.address().port}`;
});

after(() => server.close());

function build(env = {}, {revalidator = true} = {}) {
    const policy = loadPolicy({STOREFRONT_CACHE_MAX_MB: '1', ...env});
    const store = createStore(policy, {now: () => clock});
    cache = createPageCache({policy, store, now: () => clock, metrics: createCacheMetrics({policy, store}),
        revalidate: revalidator ? loopbackRevalidator(() => server.address().port) : undefined,
        degraded: res => degradedResponses.has(res), log: {warn: () => undefined, error: () => undefined, info: () => undefined}});
    return {policy, store};
}

beforeEach(() => {
    renders = 0;
    answer = {};
    keepAlives = [];
    clock = 1_000_000;
    build();
});

async function get(path, headers = STORE_1, method = 'GET') {
    const res = await fetch(`${base}${path}`, {method, headers});
    return {status: res.status, cache: res.headers.get('x-storefront-cache'), body: await res.text(), res,
        cc: res.headers.get('cache-control'), vary: res.headers.get('vary')};
}

const settled = () => new Promise(resolve => setTimeout(resolve, 30));

test('a second request for the same page of the same store is sent the same bytes without a render', async () => {
    const first = await get('/en');
    const second = await get('/en');
    assert.equal(first.cache, 'miss');
    assert.equal(second.cache, 'hit');
    assert.equal(second.body, first.body);
    assert.equal(second.res.headers.get('content-type'), HTML);
    assert.equal(renders, 1);
});

test('hit and miss leave with the same edge headers: the class\'s Cache-Control, Next\'s Vary plus the store headers', async () => {
    const first = await get('/en');
    const second = await get('/en');
    for (const r of [first, second]) {
        assert.equal(r.cc, 'public, s-maxage=30, stale-while-revalidate=300');
        assert.equal(r.vary, `${NEXT_VARY}, store-id, theme, color-theme`);
    }
    assert.equal((await get('/en/product/shoe')).cc, 'public, s-maxage=20, stale-while-revalidate=120');
});

test('another store, host, locale or URL is another page; a tracking parameter is not', async () => {
    await get('/en');
    await get('/en', {...STORE_1, 'store-id': 'store-2'});
    await get('/en', {...STORE_1, 'x-forwarded-host': 'other.example'});
    await get('/de');
    await get('/en/category/shoes');
    assert.equal(renders, 5);
    assert.equal((await get('/en?utm_source=mail')).cache, 'hit');
    assert.equal((await get('/en', {...STORE_1, 'store-id': 'store-2'})).body, '<html>page /en for store-2</html>');
});

test('a bypassed request is rendered every time, says why under debug, and its edge header follows its class', async () => {
    build({STOREFRONT_CACHE_DEBUG: 'true'});
    const login = await get('/en/login');
    assert.equal(login.cache, 'bypass');
    assert.equal(login.res.headers.get('x-storefront-cache-reason'), 'class-disabled');
    assert.equal(login.res.headers.get('x-storefront-cache-class'), 'shopper');
    assert.equal(login.cc, 'private, no-store');
    assert.equal((await get('/en/login')).cache, 'bypass');
    const override = await get('/en?theme=basic');
    assert.equal(override.res.headers.get('x-storefront-cache-reason'), 'override-param');
    assert.equal(override.cc, 'private, no-store', 'an override is one tester\'s page, whatever its class says');
    assert.equal((await get('/en', {...STORE_1, rsc: '1'})).res.headers.get('x-storefront-cache-reason'), 'rsc');
    assert.equal((await get('/en', {...STORE_1, cookie: 'storefront-theme=basic'})).cache, 'bypass');
    assert.equal((await get('/en', {...STORE_1, authorization: 'Bearer x'})).cache, 'bypass');
    assert.equal((await get('/en', STORE_1, 'POST')).cache, 'bypass');
    const manifest = await get('/api/theme-manifest');
    assert.equal(manifest.cache, 'bypass');
    assert.equal(manifest.cc, NEXT_PRIVATE, 'passthrough leaves what the route set');
    assert.equal(renders, 8);
    const hit = await get('/en');
    assert.equal((await get('/en')).res.headers.get('x-storefront-cache-key'), hit.res.headers.get('x-storefront-cache-key'));
});

test('without debug no class, key or reason header leaves', async () => {
    const r = await get('/en/login');
    assert.equal(r.res.headers.get('x-storefront-cache-reason'), null);
    assert.equal((await get('/en')).res.headers.get('x-storefront-cache-key'), null);
});

test('a non-200, a page missing a section, a truncated page and a page setting a cookie are never kept', async () => {
    answer = {status: 404};
    const missing = await get('/en/product/nope');
    assert.equal(missing.status, 404);
    assert.equal(missing.cc, 'private, no-store', 'an error page is not for any cache');
    answer = {};
    assert.equal((await get('/en/product/nope')).cache, 'miss');
    answer = {degraded: true};
    await get('/en/product/a');
    answer = {};
    assert.equal((await get('/en/product/a')).cache, 'miss');
    answer = {truncated: true};
    await get('/en/product/b').catch(() => undefined);
    answer = {};
    assert.equal((await get('/en/product/b')).cache, 'miss');
    answer = {headers: {'set-cookie': 'session=1'}};
    await get('/en/product/c');
    answer = {};
    assert.equal((await get('/en/product/c')).cache, 'miss');
    answer = {inline: ['Set-Cookie', 'NEXT_LOCALE=en; Path=/']};
    await get('/en/product/d');
    answer = {};
    assert.equal((await get('/en/product/d')).cache, 'hit', 'the locale cookie is the one cookie a page may set');
});

test('the locale cookie a kept page set is replayed only to a shopper whose differs', async () => {
    answer = {inline: ['Set-Cookie', 'NEXT_LOCALE=en; Path=/']};
    await get('/en');
    const other = await get('/en');
    assert.equal(other.res.headers.get('set-cookie'), 'NEXT_LOCALE=en; Path=/');
    const same = await get('/en', {...STORE_1, cookie: 'NEXT_LOCALE=en'});
    assert.equal(same.res.headers.get('set-cookie'), null);
});

test('a stale page is served at once and refreshed once by the cache\'s own request', async () => {
    await get('/en');
    clock += 31_000;
    const first = await get('/en');
    const second = await get('/en');
    assert.equal(first.cache, 'stale');
    assert.equal(second.cache, 'stale');
    await settled();
    assert.equal(renders, 2, 'one refresh for two stale requests');
    assert.equal((await get('/en')).cache, 'hit');
});

test('without a revalidator the first request to find a page stale renders it in line', async () => {
    build({}, {revalidator: false});
    await get('/en');
    clock += 31_000;
    assert.equal((await get('/en')).cache, 'stale-refresh');
    assert.equal((await get('/en')).cache, 'hit');
});

test('past its stale seconds a page is rendered as if never seen', async () => {
    await get('/en');
    clock += 331_000;
    assert.equal((await get('/en')).cache, 'miss');
});

test('shoppers who arrive while a page is rendering share that render, which their waiting keeps alive', async () => {
    answer = {delayMs: 80};
    const [a, b, c] = await Promise.all([get('/en'), get('/en'), get('/en')]);
    assert.equal(renders, 1);
    assert.deepEqual([a.cache, b.cache, c.cache].sort(), ['miss', 'shared', 'shared']);
    assert.equal(b.body, a.body);
    assert.equal(keepAlives.length, 1);
});

test('a HEAD is answered from a copy without a body, and never fills the cache', async () => {
    const head = await get('/en', STORE_1, 'HEAD');
    assert.equal(head.cache, 'miss');
    assert.equal((await get('/en', STORE_1, 'HEAD')).cache, 'miss');
    await get('/en');
    const served = await get('/en', STORE_1, 'HEAD');
    assert.equal(served.cache, 'hit');
    assert.equal(served.body, '');
    assert.equal(served.res.headers.get('content-length'), String(Buffer.byteLength('<html>page /en for store-1</html>')));
});

test('the revalidate marker is this process\'s secret; the stats route answers on loopback, not through a proxy', async () => {
    await get('/en');
    clock += 31_000;
    const forged = await get('/en', {...STORE_1, [REVALIDATE_HEADER]: '1'});
    assert.equal(forged.cache, 'stale', 'a guessed marker is an ordinary request');
    await settled();
    clock += 31_000;
    const refreshed = await get('/en', {...STORE_1, [REVALIDATE_HEADER]: REVALIDATE_TOKEN});
    assert.equal(refreshed.cache, 'stale-refresh');
    assert.equal((await get('/en')).cache, 'hit');
    const proxied = await get(STATS_PATH, {'x-forwarded-for': '10.0.0.9'});
    assert.equal(proxied.cache, 'bypass', 'through spg the route is not the cache\'s: Next answers it');
    const stats = await get(STATS_PATH);
    assert.equal(stats.status, 200);
    const body = JSON.parse(stats.body);
    assert.equal(body.store.entries, 1);
    assert.equal(body.counters.hit, 1);
    assert.equal(body.byClass.home.miss, 1);
    build({STOREFRONT_CACHE_STATS_TOKEN: 's3cret'});
    assert.equal((await get(STATS_PATH, {'x-forwarded-for': '10.0.0.9', 'x-storefront-cache-token': 's3cret'})).status, 200);
});

test('a disabled cache bypasses everything but still sets the edge headers', async () => {
    build({STOREFRONT_CACHE_ENABLED: 'false'});
    assert.equal((await get('/en')).cache, 'bypass');
    assert.equal((await get('/en')).cache, 'bypass');
    assert.equal((await get('/en')).cc, 'public, s-maxage=30, stale-while-revalidate=300');
    assert.equal(renders, 3);
});

test('the environment tunes a class: five seconds for a product page', async () => {
    build({STOREFRONT_CACHE_PRODUCT_TTL_SECONDS: '5'});
    await get('/en/product/shoe');
    clock += 6_000;
    assert.equal((await get('/en/product/shoe')).cache, 'stale');
    assert.equal((await get('/en/product/shoe')).cc, 'public, s-maxage=5, stale-while-revalidate=120');
});

test('a body over the entry limit is sent but not kept', async () => {
    build({STOREFRONT_CACHE_MAX_ENTRY_KB: '1'});
    answer = {body: `<html>${'x'.repeat(2048)}</html>`};
    await get('/en');
    assert.equal((await get('/en')).cache, 'miss');
});

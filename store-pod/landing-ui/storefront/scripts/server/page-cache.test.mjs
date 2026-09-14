import {strict as assert} from 'node:assert';
import http from 'node:http';
import {after, before, beforeEach, test} from 'node:test';
import {createPageCache, pageCacheSettings} from './page-cache.mjs';

/** A stand-in for Next: counts renders, answers what the test asks for, with Next's Vary. */
let renders = 0;
let answer = {};
let clock = 0;
let cache;
let server;
let base;

const STORE_1 = {'store-id': 'store-1', theme: 'starter', 'default-language': 'en', 'supported-languages': 'en,ar'};

before(async () => {
    server = http.createServer((req, res) => cache.serve(req, res, async () => {
        renders += 1;
        const {status = 200, headers = {}, body = `page ${req.url} for ${req.headers['store-id']}`, delayMs = 0} = answer;
        if (delayMs) {
            await new Promise(resolve => setTimeout(resolve, delayMs));
        }
        res.statusCode = status;
        res.setHeader('content-type', 'text/html; charset=utf-8');
        res.setHeader('vary', 'rsc, next-router-state-tree, next-router-prefetch, next-router-segment-prefetch');
        for (const [name, value] of Object.entries(headers)) {
            res.setHeader(name, value);
        }
        res.write(body.slice(0, 5));
        res.end(body.slice(5));
    }));
    await new Promise(resolve => server.listen(0, '127.0.0.1', resolve));
    base = `http://127.0.0.1:${server.address().port}`;
});

after(() => server.close());

beforeEach(() => {
    renders = 0;
    answer = {};
    clock = 1_000_000;
    cache = createPageCache({ttlMs: 30_000, staleMs: 300_000, maxBytes: 1024 * 1024, now: () => clock});
});

async function get(path, headers = STORE_1, method = 'GET') {
    const res = await fetch(`${base}${path}`, {method, headers});
    return {status: res.status, cache: res.headers.get('x-storefront-cache'), body: await res.text(), res};
}

test('a second request for the same page of the same store is sent the same bytes without a render', async () => {
    const first = await get('/en');
    const second = await get('/en');
    assert.equal(first.cache, 'miss');
    assert.equal(second.cache, 'hit');
    assert.equal(second.body, first.body);
    assert.equal(second.res.headers.get('content-type'), 'text/html; charset=utf-8');
    assert.equal(renders, 1);
});

test('another store, host, URL or RSC variant is another page', async () => {
    await get('/en');
    await get('/en', {...STORE_1, 'store-id': 'store-2'});
    // fetch will not send a Host of its own; spg's X-Forwarded-Host names the storefront's domain the same way.
    await get('/en', {...STORE_1, 'x-forwarded-host': 'other.example'});
    await get('/en/category/shoes');
    await get('/en', {...STORE_1, rsc: '1'});
    assert.equal(renders, 5);
    assert.equal((await get('/en', {...STORE_1, 'store-id': 'store-2'})).body, 'page /en for store-2');
});

test('requests that could carry a shopper, a draft or an override are never served from the cache', async () => {
    const cases = [
        ['/en', {...STORE_1, authorization: 'Bearer t'}],
        ['/en', {...STORE_1, cookie: 'storefront-theme=noir'}],
        ['/en?theme=noir', STORE_1],
        ['/en/content/about?preview=token', STORE_1],
        ['/en/login', STORE_1],
        ['/en/customer/order/7', STORE_1],
        ['/en/checkout', STORE_1],
        ['/api/theme-manifest', STORE_1],
        ['/_next/static/chunk.js', STORE_1],
    ];
    for (const [path, headers] of cases) {
        assert.equal((await get(path, headers)).cache, 'bypass', path);
        assert.equal((await get(path, headers)).cache, 'bypass', path);
    }
    assert.equal(renders, cases.length * 2);
});

test('other cookies do not keep a shopper from the cached page', async () => {
    await get('/en');
    assert.equal((await get('/en', {...STORE_1, cookie: '_ga=1; NEXT_LOCALE=en'})).cache, 'hit');
});

test('a response that sets a cookie of its own, or is not a 200, is not kept', async () => {
    answer = {headers: {'set-cookie': 'session=abc; Path=/'}};
    await get('/en/a');
    assert.equal((await get('/en/a')).cache, 'miss');
    answer = {status: 404};
    await get('/en/b');
    assert.equal((await get('/en/b')).cache, 'miss');
    answer = {headers: {vary: 'accept-language'}};
    await get('/en/c');
    assert.equal((await get('/en/c')).cache, 'miss');
    assert.equal(renders, 6);
});

test("next-intl's NEXT_LOCALE cookie is kept, and sent only to a shopper whose cookie differs", async () => {
    answer = {headers: {'set-cookie': 'NEXT_LOCALE=en; Path=/; SameSite=lax'}};
    await get('/en');
    const without = await get('/en');
    const same = await get('/en', {...STORE_1, cookie: 'NEXT_LOCALE=en'});
    const other = await get('/en', {...STORE_1, cookie: 'NEXT_LOCALE=ar'});
    assert.equal(without.cache, 'hit');
    assert.equal(without.res.headers.get('set-cookie'), 'NEXT_LOCALE=en; Path=/; SameSite=lax');
    assert.equal(same.res.headers.get('set-cookie'), null);
    assert.equal(other.res.headers.get('set-cookie'), 'NEXT_LOCALE=en; Path=/; SameSite=lax');
    assert.equal(renders, 1);
});

test('past its TTL a page is served stale while one request renders it again; past the stale window it is a miss', async () => {
    await get('/en');
    clock += 31_000;
    answer = {body: 'page two', delayMs: 100};
    const [refreshing, meanwhile] = await Promise.all([get('/en'), new Promise(resolve => setTimeout(resolve, 20))
        .then(() => get('/en'))]);
    assert.equal(refreshing.cache, 'stale-refresh');
    assert.equal(refreshing.body, 'page two');
    assert.equal(meanwhile.cache, 'stale');
    assert.notEqual(meanwhile.body, 'page two');
    assert.equal((await get('/en')).body, 'page two');
    clock += 400_000;
    assert.equal((await get('/en')).cache, 'miss');
    assert.equal(renders, 3);
});

test('shoppers who arrive while a page is first rendering share that one render', async () => {
    answer = {delayMs: 150};
    const results = await Promise.all(Array.from({length: 20}, () => get('/en/spike')));
    assert.equal(renders, 1);
    assert.equal(results.filter(r => r.cache === 'shared').length, 19);
    assert.ok(results.every(r => r.body === results[0].body));
});

test('HEAD is answered from a kept page, and never fills one', async () => {
    assert.equal((await get('/en/h', STORE_1, 'HEAD')).cache, 'bypass');
    await get('/en/h');
    const head = await get('/en/h', STORE_1, 'HEAD');
    assert.equal(head.cache, 'hit');
    assert.equal(head.body, '');
    assert.equal(renders, 2);
});

test('memory stays inside the budget, least recently used out first', async () => {
    cache = createPageCache({ttlMs: 30_000, staleMs: 0, maxBytes: 1000, now: () => clock});
    answer = {body: 'x'.repeat(200)};
    for (const page of ['a', 'b', 'c', 'd']) {
        await get(`/en/${page}`);
    }
    await get('/en/a');
    await get('/en/e');
    await get('/en/f');
    assert.ok(cache.bytes() <= 1000);
    assert.equal((await get('/en/a')).cache, 'hit');
    assert.equal((await get('/en/b')).cache, 'miss');
});

test('STOREFRONT_PAGE_CACHE_TTL_SECONDS=0 turns the cache off', async () => {
    const settings = pageCacheSettings({STOREFRONT_PAGE_CACHE_TTL_SECONDS: '0'});
    cache = createPageCache({...settings, now: () => clock});
    await get('/en');
    assert.equal((await get('/en')).cache, 'bypass');
    assert.equal(renders, 2);
    assert.deepEqual(pageCacheSettings({}), {ttlMs: 30_000, staleMs: 300_000, maxBytes: 64 * 1024 * 1024});
});

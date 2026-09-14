import {strict as assert} from 'node:assert';
import http from 'node:http';
import {after, before, beforeEach, test} from 'node:test';
import {createPageCache, loopbackRevalidator, pageCacheSettings, REVALIDATE_HEADER} from './page-cache.mjs';

/** A stand-in for Next: counts renders, answers what the test asks for, with Next's Vary. */
let renders = 0;
let answer = {};
let clock = 0;
let cache;
let server;
let base;
/** What each render was told about shoppers waiting on it, and the responses of renders marked degraded. */
let keepAlives = [];
const degradedResponses = new WeakSet();

const STORE_1 = {'store-id': 'store-1', theme: 'starter', 'default-language': 'en', 'supported-languages': 'en,ar'};

const HTML = 'text/html; charset=utf-8';

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
        res.setHeader('vary', 'rsc, next-router-state-tree, next-router-prefetch, next-router-segment-prefetch');
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

beforeEach(() => {
    renders = 0;
    answer = {};
    keepAlives = [];
    clock = 1_000_000;
    cache = createPageCache({ttlMs: 30_000, staleMs: 300_000, maxBytes: 1024 * 1024, now: () => clock,
        revalidate: loopbackRevalidator(() => server.address().port), degraded: res => degradedResponses.has(res),
        log: {warn: () => undefined, error: () => undefined}});
});

async function get(path, headers = STORE_1, method = 'GET') {
    const res = await fetch(`${base}${path}`, {method, headers});
    return {status: res.status, cache: res.headers.get('x-storefront-cache'), body: await res.text(), res};
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

test('another store, host or URL is another page', async () => {
    await get('/en');
    await get('/en', {...STORE_1, 'store-id': 'store-2'});
    // fetch will not send a Host of its own; spg's X-Forwarded-Host names the storefront's domain the same way.
    await get('/en', {...STORE_1, 'x-forwarded-host': 'other.example'});
    await get('/en/category/shoes');
    assert.equal(renders, 4);
    assert.equal((await get('/en', {...STORE_1, 'store-id': 'store-2'})).body, '<html>page /en for store-2</html>');
});

test('requests that could carry a shopper, a draft, an override or a client navigation are never served from the cache',
    async () => {
        const cases = [
            ['/en', {...STORE_1, authorization: 'Bearer t'}],
            ['/en', {...STORE_1, cookie: 'storefront-theme=noir'}],
            ['/en', {...STORE_1, rsc: '1', 'next-router-state-tree': '%5B%22%22%2C%7B%7D%5D'}],
            ['/en', {...STORE_1, 'next-router-prefetch': '1'}],
            ['/en?theme=noir', STORE_1],
            ['/en/content/about?preview=token', STORE_1],
            ['/en/login', STORE_1],
            ['/en/customer/order/7', STORE_1],
            ['/en/checkout', STORE_1],
            ['/api/theme-manifest', STORE_1],
            ['/_next/static/chunk.js', STORE_1],
            ['//_next/static/chunk.js', STORE_1],
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
    // writeHead's flat array form sets the cookie as surely as setHeader does
    answer = {inline: ['Set-Cookie', 'session=abc; Path=/']};
    await get('/en/d');
    assert.equal((await get('/en/d')).cache, 'miss');
    assert.equal(renders, 8);
});

test('a render with a backend read that gave up, or a document that stopped short, is not kept', async () => {
    answer = {degraded: true};
    await get('/en/thin');
    assert.equal((await get('/en/thin')).cache, 'miss');
    answer = {body: '<html>the shell, then the stream failed'};
    await get('/en/short');
    assert.equal((await get('/en/short')).cache, 'miss');
    answer = {truncated: true};
    await get('/en/cut').catch(() => undefined);
    answer = {};
    assert.equal((await get('/en/cut')).cache, 'miss');
    assert.equal(cache.size(), 1);
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

test('past its TTL a page is served stale at once while the cache refreshes it by itself; past the stale window it is a miss',
    async () => {
        await get('/en');
        clock += 31_000;
        answer = {body: '<html>page two</html>', delayMs: 100};
        const stale = await get('/en');
        assert.equal(stale.cache, 'stale');
        assert.equal(stale.body, '<html>page /en for store-1</html>');
        assert.equal((await get('/en')).cache, 'stale', 'one refresh at a time');
        await new Promise(resolve => setTimeout(resolve, 200));
        const refreshed = await get('/en');
        assert.equal(refreshed.cache, 'hit');
        assert.equal(refreshed.body, '<html>page two</html>');
        assert.equal(renders, 2);
        clock += 400_000;
        assert.equal((await get('/en')).cache, 'miss');
        assert.equal(renders, 3);
    });

test('a refresh that fails leaves the stale copy to serve out its window, and is tried again on the next request',
    async () => {
        await get('/en/fragile');
        clock += 31_000;
        answer = {status: 500};
        assert.equal((await get('/en/fragile')).cache, 'stale');
        await settled();
        assert.equal((await get('/en/fragile')).cache, 'stale');
        await settled();
        assert.equal(renders, 3);
        answer = {};
        assert.equal((await get('/en/fragile')).cache, 'stale');
        await settled();
        assert.equal((await get('/en/fragile')).cache, 'hit');
    });

test('a listener that throws before answering ends the response instead of leaving it open', async () => {
    const throwing = createPageCache({ttlMs: 30_000, staleMs: 0, maxBytes: 1024, now: () => clock,
        log: {warn: () => undefined, error: () => undefined}});
    const broken = http.createServer((req, res) => throwing.serve(req, res, () => {
        throw new Error('boom');
    }));
    await new Promise(resolve => broken.listen(0, '127.0.0.1', resolve));
    try {
        const res = await fetch(`http://127.0.0.1:${broken.address().port}/en`, {headers: STORE_1});
        assert.equal(res.status, 500);
    } finally {
        broken.close();
    }
});

test('without a revalidator the first request to find a page stale renders it in line', async () => {
    cache = createPageCache({ttlMs: 30_000, staleMs: 300_000, maxBytes: 1024 * 1024, now: () => clock});
    await get('/en');
    clock += 31_000;
    answer = {body: '<html>page two</html>', delayMs: 100};
    const [refreshing, meanwhile] = await Promise.all([get('/en'), new Promise(resolve => setTimeout(resolve, 20))
        .then(() => get('/en'))]);
    assert.equal(refreshing.cache, 'stale-refresh');
    assert.equal(refreshing.body, '<html>page two</html>');
    assert.equal(meanwhile.cache, 'stale');
    assert.equal((await get('/en')).cache, 'hit');
    assert.equal(renders, 2);
});

test("the cache's own refresh request renders whatever the cache holds, and never loops", async () => {
    await get('/en/r');
    const refresh = await get('/en/r', {...STORE_1, [REVALIDATE_HEADER]: '1'});
    assert.equal(refresh.cache, 'stale-refresh');
    assert.equal(renders, 2);
    assert.equal((await get('/en/r')).cache, 'hit');
});

test('shoppers who arrive while a page is first rendering share that one render, which they keep alive', async () => {
    answer = {delayMs: 150};
    const results = await Promise.all(Array.from({length: 20}, () => get('/en/spike')));
    assert.equal(renders, 1);
    assert.equal(results.filter(r => r.cache === 'shared').length, 19);
    assert.ok(results.every(r => r.body === results[0].body));
    assert.equal(typeof keepAlives[0], 'function');
});

test('a render nobody shares is not kept alive for anyone; one that is shared is, while a waiter is still there',
    async () => {
        await get('/en/alone');
        assert.equal(keepAlives[0](), false);

        answer = {delayMs: 150};
        const first = get('/en/together');
        await new Promise(resolve => setTimeout(resolve, 30));
        const second = get('/en/together');
        await new Promise(resolve => setTimeout(resolve, 30));
        assert.equal(keepAlives[1](), true, 'a second shopper is waiting on the first render');
        await Promise.all([first, second]);
        assert.equal(keepAlives[1](), false, 'nobody is waiting once it was sent');
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
    answer = {body: `<html>${'x'.repeat(200)}</html>`};
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

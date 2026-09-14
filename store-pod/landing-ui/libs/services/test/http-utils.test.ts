import {strict as assert} from 'node:assert';
import {afterEach, beforeEach, test} from 'node:test';
import {apiFetch, get, orUndefined, post, publicCachedGet, publicGet} from '../src/http-utils';

type Seen = { url: string; init?: RequestInit };
let seen: Seen[] = [];
const realFetch = globalThis.fetch;

beforeEach(() => {
    seen = [];
    globalThis.fetch = (async (url: string, init?: RequestInit) => {
        seen.push({url, init});
        return new Response('{"ok":true}', {status: 200, headers: {'Content-Type': 'application/json'}});
    }) as typeof fetch;
});

afterEach(() => {
    globalThis.fetch = realFetch;
    delete (globalThis as {window?: unknown}).window;
});

const headersOf = (s: Seen) => new Headers(s.init?.headers);

test('a server-side call asks spg for identity encoding', async () => {
    assert.deepEqual(await apiFetch('http://spg/catalog/api/v1/x', publicGet()), {ok: true});
    assert.equal(headersOf(seen[0]).get('Accept-Encoding'), 'identity');
});

test('the rest of the request is untouched: method, body, headers, Next cache options', async () => {
    await apiFetch('http://spg/a', post({a: 1}));
    const sent = seen[0].init!;
    assert.equal(sent.method, 'POST');
    assert.equal(sent.body, '{"a":1}');
    assert.equal(headersOf(seen[0]).get('Content-Type'), 'application/json');

    await apiFetch('http://spg/b', publicCachedGet(30));
    assert.deepEqual((seen[1].init as {next?: unknown}).next, {revalidate: 30});
    assert.equal(headersOf(seen[1]).get('Accept-Encoding'), 'identity');
});

test('a call without init still gets the header', async () => {
    await apiFetch('http://spg/c');
    assert.equal(headersOf(seen[0]).get('Accept-Encoding'), 'identity');
});

test("the caller's init is not mutated, and a caller's own Accept-Encoding wins", async () => {
    const shared = get();
    await apiFetch('http://spg/d', shared);
    assert.deepEqual(shared.headers, {});

    await apiFetch('http://spg/e', {method: 'GET', headers: {'Accept-Encoding': 'gzip'}});
    assert.equal(headersOf(seen[1]).get('Accept-Encoding'), 'gzip');
});

test('in a browser the request goes out as the caller built it', async () => {
    (globalThis as {window?: unknown}).window = {};
    const init = publicGet();
    await apiFetch('http://spg/f', init);
    assert.equal(seen[0].init, init);
    assert.equal(headersOf(seen[0]).get('Accept-Encoding'), null);
});

const REQUEST_SIGNAL = Symbol.for('cvhome.storefront.requestSignal');

test('a server-side read carries a time budget, and a write does not', async () => {
    await apiFetch('http://spg/g', publicGet());
    await apiFetch('http://spg/h', post({a: 1}));
    assert.ok(seen[0].init?.signal, 'a read has no signal');
    assert.equal(seen[0].init?.signal?.aborted, false);
    assert.equal(seen[1].init?.signal, undefined);
});

test('a read gives up once its budget is spent', async () => {
    process.env.STOREFRONT_BACKEND_TIMEOUT_MS = '20';
    try {
        globalThis.fetch = ((_url: string, init?: RequestInit) => new Promise((_resolve, reject) => {
            init?.signal?.addEventListener('abort', () => reject(init.signal?.reason));
        })) as typeof fetch;
        await assert.rejects(apiFetch('http://spg/slow', publicGet()), (error: {category?: string}) =>
            error.category === 'NETWORK');
    } finally {
        delete process.env.STOREFRONT_BACKEND_TIMEOUT_MS;
    }
});

test("the shopper's request signal ends a server-side read, and is not attached to a write", async () => {
    const shopper = new AbortController();
    (globalThis as Record<symbol, unknown>)[REQUEST_SIGNAL] = () => shopper.signal;
    try {
        await apiFetch('http://spg/i', post({a: 1}));
        assert.equal(seen[0].init?.signal, undefined, 'a write is not given up on because the tab closed');
        globalThis.fetch = ((_url: string, init?: RequestInit) => new Promise((_resolve, reject) => {
            init?.signal?.addEventListener('abort', () => reject(init.signal?.reason));
        })) as typeof fetch;
        const read = apiFetch('http://spg/j', publicGet());
        shopper.abort(new Error('the client closed the connection'));
        await assert.rejects(read, (error: {category?: string}) => error.category === 'NETWORK');
    } finally {
        delete (globalThis as Record<symbol, unknown>)[REQUEST_SIGNAL];
    }
});

test('a read that gave up marks the render degraded through orUndefined; an answer, even a 404, does not', async () => {
    const REQUEST_DEGRADED = Symbol.for('cvhome.storefront.requestDegraded');
    let marks = 0;
    (globalThis as Record<symbol, unknown>)[REQUEST_DEGRADED] = () => {
        marks += 1;
    };
    try {
        globalThis.fetch = (() => Promise.reject(new TypeError('fetch failed'))) as typeof fetch;
        assert.equal(await orUndefined(apiFetch('http://spg/down', publicGet())), undefined);
        assert.equal(marks, 1);
        globalThis.fetch = (() => Promise.resolve(new Response('', {status: 404}))) as typeof fetch;
        assert.equal(await orUndefined(apiFetch('http://spg/missing', publicGet())), undefined);
        assert.equal(marks, 1, 'a 404 is an answer: the page is whole without that section');
    } finally {
        delete (globalThis as Record<symbol, unknown>)[REQUEST_DEGRADED];
    }
});

test('STOREFRONT_BACKEND_TIMEOUT_MS=0 turns the budget off', async () => {
    process.env.STOREFRONT_BACKEND_TIMEOUT_MS = '0';
    try {
        await apiFetch('http://spg/k', publicGet());
        assert.equal(seen[0].init?.signal, undefined);
    } finally {
        delete process.env.STOREFRONT_BACKEND_TIMEOUT_MS;
    }
});

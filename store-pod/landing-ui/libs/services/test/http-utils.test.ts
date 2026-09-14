import {strict as assert} from 'node:assert';
import {afterEach, beforeEach, test} from 'node:test';
import {apiFetch, get, post, publicCachedGet, publicGet} from '../src/http-utils';

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

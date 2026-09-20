import assert from 'node:assert/strict';
import {test} from 'node:test';
import {MemoryStore} from './memory-store.mjs';
import {createStore} from './store.mjs';
import {loadPolicy, PolicyError} from './policy.mjs';

const entry = (cls, size, at = 0, ttlMs = 1000, staleMs = 1000) =>
    ({status: 200, headers: {}, body: Buffer.alloc(size, 'x'), cls, storedAt: at, ttlMs, staleMs});

test('a store keeps what it is given, moves a read entry to the end, and drops the oldest past its bytes', async () => {
    const store = new MemoryStore({maxBytes: 30, now: () => 0});
    const evicted = [];
    store.onEvict((key, held, reason) => evicted.push([key, reason]));
    await store.set('a', entry('home', 10));
    await store.set('b', entry('home', 10));
    await store.set('c', entry('home', 10));
    assert.equal(store.size(), 3);
    assert.equal(store.bytes(), 30);
    assert.ok(await store.get('a'), 'a is now the most recent');
    await store.set('d', entry('home', 10));
    assert.deepEqual(evicted, [['b', 'bytes']]);
    assert.equal(await store.get('b'), undefined);
    assert.ok(await store.get('a'));
    assert.deepEqual(store.byClass(), {home: {entries: 3, bytes: 30}});
});

test('a class over its share loses its own oldest, not another class\'s', async () => {
    const store = new MemoryStore({maxBytes: 100, shares: {product: 0.2}, now: () => 0});
    const evicted = [];
    store.onEvict((key, held, reason) => evicted.push([key, reason]));
    await store.set('home', entry('home', 50));
    await store.set('p1', entry('product', 10));
    await store.set('p2', entry('product', 10));
    await store.set('p3', entry('product', 10));
    assert.deepEqual(evicted, [['p1', 'class-share']]);
    assert.ok(await store.get('home'));
    assert.equal(store.bytes(), 70);
});

test('an entry past its fresh and stale time is gone when read; a replaced one is counted once', async () => {
    let now = 0;
    const store = new MemoryStore({maxBytes: 100, now: () => now});
    await store.set('a', entry('home', 10, 0, 1000, 500));
    now = 1499;
    assert.ok(await store.get('a'));
    now = 1500;
    assert.equal(await store.get('a'), undefined);
    assert.equal(store.bytes(), 0);
    await store.set('b', entry('home', 10, now));
    await store.set('b', entry('home', 20, now));
    assert.equal(store.size(), 1);
    assert.equal(store.bytes(), 20);
    await store.delete('b');
    assert.equal(store.size(), 0);
});

test('an entry larger than the whole store does not stay', async () => {
    const store = new MemoryStore({maxBytes: 10, now: () => 0});
    await store.set('big', entry('home', 11));
    assert.equal(store.size(), 0);
});

test('createStore builds the memory store with the enabled classes\' shares, and refuses an unknown store', () => {
    const store = createStore(loadPolicy({}));
    assert.ok(store instanceof MemoryStore);
    assert.throws(() => createStore(loadPolicy({STOREFRONT_CACHE_STORE: 'redis'})), PolicyError);
});

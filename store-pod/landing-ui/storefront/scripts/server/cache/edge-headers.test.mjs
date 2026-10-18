import assert from 'node:assert/strict';
import {test} from 'node:test';
import {edgeHeadersFor, enforceEdgeHeaders, mergeVary, presetEdgeHeaders} from './edge-headers.mjs';
import {loadPolicy} from './policy.mjs';

function fakeRes() {
    const headers = new Map();
    return {
        setHeader: (name, value) => headers.set(name.toLowerCase(), value),
        getHeader: name => headers.get(name.toLowerCase()),
        headers,
    };
}

test('a kept class presets its value, enforces it on a 200 and private on anything else', () => {
    const rule = loadPolicy({}).classes.get('home');
    const res = fakeRes();
    presetEdgeHeaders(res, rule);
    assert.equal(res.getHeader('cache-control'), 'public, s-maxage=30, stale-while-revalidate=300');
    res.setHeader('cache-control', 'private, no-cache');
    res.setHeader('vary', 'RSC');
    enforceEdgeHeaders(res, rule, 200);
    assert.equal(res.getHeader('cache-control'), 'public, s-maxage=30, stale-while-revalidate=300');
    assert.equal(res.getHeader('vary'), 'RSC, store-id, theme, color-theme');
    enforceEdgeHeaders(res, rule, 500);
    assert.equal(res.getHeader('cache-control'), 'private, no-store');
});

test('a passthrough class touches nothing; a private class says private on every status', () => {
    const policy = loadPolicy({});
    const res = fakeRes();
    presetEdgeHeaders(res, policy.classes.get('next-internal'));
    enforceEdgeHeaders(res, policy.classes.get('next-internal'), 200);
    assert.equal(res.getHeader('cache-control'), undefined);
    enforceEdgeHeaders(res, policy.classes.get('shopper'), 200);
    assert.equal(res.getHeader('cache-control'), 'private, no-store');
    assert.equal(res.getHeader('vary'), undefined);
});

test('a replayed copy computes the same headers from its entry', () => {
    const rule = loadPolicy({}).classes.get('product');
    assert.deepEqual(edgeHeadersFor({status: 200, headers: {vary: 'RSC'}}, rule),
        {'cache-control': 'public, s-maxage=20, stale-while-revalidate=120', vary: 'RSC, store-id, theme, color-theme'});
});

test('mergeVary keeps each name once, in the order first seen', () => {
    assert.equal(mergeVary('RSC, Theme', ['store-id', 'theme']), 'RSC, Theme, store-id');
    assert.equal(mergeVary(undefined, ['store-id']), 'store-id');
    assert.equal(mergeVary(['a', 'b'], []), 'a, b');
});

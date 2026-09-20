import assert from 'node:assert/strict';
import {test} from 'node:test';
import {buildKey, normaliseQuery} from './key.mjs';
import {classify, loadPolicy} from './policy.mjs';

const headers = {host: 'landing-ui:8110', 'x-forwarded-host': 'shop.example', 'store-id': 's1', theme: 'basic',
    'color-theme': 'DARK', 'default-language': 'en', 'supported-languages': 'en,de'};

test('the key is the class, then the parts its rule names, in order', () => {
    const policy = loadPolicy({});
    const classified = classify({method: 'GET', url: '/en/product/red-shoe?b=2&a=1&utm_source=x', headers}, policy);
    assert.equal(buildKey(classified, classified && {headers}, policy),
        'v1|product|host=shop.example|store=s1|theme=basic|color=DARK|locale=en|path=/product/red-shoe|q=a=1&b=2');
});

test('a class varies on what it names and nothing else; a missing header is an empty part', () => {
    const policy = loadPolicy({});
    const classified = classify({method: 'GET', url: '/sitemap.xml', headers: {host: 'shop.example'}}, policy);
    assert.equal(buildKey(classified, {headers: {host: 'shop.example'}}, policy), 'v1|seo|host=shop.example|store=|path=/sitemap.xml');
});

test('the same page with its parameters in another order, or with a tracking parameter, has the same key', () => {
    const policy = loadPolicy({});
    const key = url => {
        const classified = classify({method: 'GET', url, headers}, policy);
        return buildKey(classified, {headers}, policy);
    };
    assert.equal(key('/en/search?q=shoe&page=2'), key('/en/search?page=2&q=shoe&fbclid=1&utm_campaign=c'));
    assert.notEqual(key('/en/search?q=shoe&page=2'), key('/en/search?q=shoe&page=3'));
    assert.notEqual(key('/en/search?q=shoe'), key('/de/search?q=shoe'));
});

test('normaliseQuery sorts by name then value, drops by name or prefix, and answers empty for nothing', () => {
    assert.equal(normaliseQuery(''), '');
    assert.equal(normaliseQuery('utm_source=x', ['utm_*']), '');
    assert.equal(normaliseQuery('b=2&a=2&a=1', []), 'a=1&a=2&b=2');
    assert.equal(normaliseQuery('q=a%20b&ref=x', ['ref']), 'q=a+b');
});

test('a rule that varies on an unknown part fails loudly', () => {
    const policy = loadPolicy({STOREFRONT_CACHE_POLICY_JSON: '{"classes":{"home":{"vary":["cookie"]}}}'});
    const classified = classify({method: 'GET', url: '/en', headers}, policy);
    assert.throws(() => buildKey(classified, {headers}, policy), /not a request part/);
});

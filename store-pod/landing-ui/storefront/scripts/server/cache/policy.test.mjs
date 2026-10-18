import assert from 'node:assert/strict';
import {test} from 'node:test';
import {DEFAULT_POLICY} from './config.mjs';
import {classify, describePolicy, loadPolicy, PolicyError} from './policy.mjs';

const request = (url, {method = 'GET', headers = {}} = {}) => ({method, url, headers: {host: 'shop.example', ...headers}});

test('the defaults load, every class compiles, and the description is one line', () => {
    const policy = loadPolicy({});
    assert.equal(policy.enabled, true);
    assert.equal(policy.store, 'memory');
    assert.equal(policy.maxBytes, 64 * 1024 * 1024);
    assert.equal(policy.maxEntryBytes, 1024 * 1024);
    assert.deepEqual([...policy.classes.keys()], Object.keys(DEFAULT_POLICY.classes));
    assert.equal([...policy.classes.keys()][0], 'api-theme-manifest', 'the manifest is matched before /api/*');
    assert.equal(policy.classes.get('home').ttlMs, 30_000);
    assert.equal(policy.classes.get('home').edge, 'public, s-maxage=30, stale-while-revalidate=300');
    assert.equal(policy.classes.get('shopper').edge, 'private, no-store');
    assert.equal(policy.classes.get('next-internal').edge, null);
    const described = describePolicy(policy);
    assert.equal(described.includes('\n'), false);
    assert.equal(JSON.parse(described).classes.product.ttl, 20);
});

test('every class row classifies its own paths, after the locale', () => {
    const policy = loadPolicy({});
    const cases = [
        ['/en', 'home'], ['/en/', 'home'], ['/de-DE', 'home'],
        ['/en/category/shoes', 'category'], ['/en/category', 'category'],
        ['/en/product/red-shoe?utm_source=x', 'product'],
        ['/en/search?q=shoe', 'search'],
        ['/en/content/about', 'content'], ['/en/blog', 'content'], ['/en/blog/first-post', 'content'],
        ['/en/help', 'help'], ['/en/policies/privacy', 'help'],
        ['/sitemap.xml', 'seo'], ['/robots.txt', 'seo'],
        ['/api/theme-manifest', 'api-theme-manifest'],
        ['/en/login', 'shopper'], ['/en/customer/orders/1', 'shopper'], ['/en/checkout', 'shopper'],
        ['/_next/static/chunk.js', 'next-internal'], ['/api/health', 'next-internal'], ['/t/basic/en', 'next-internal'],
        ['/', 'next-internal'], ['/store-not-found', 'next-internal'],
        ['/en/something-else', 'default'], ['/favicon.ico', 'next-internal'],
    ];
    for (const [url, cls] of cases) {
        assert.equal(classify(request(url), policy).cls, cls, url);
    }
    const product = classify(request('/en/product/red-shoe?b=2&a=1'), policy);
    assert.equal(product.locale, 'en');
    assert.equal(product.path, '/product/red-shoe');
    assert.equal(product.search, 'b=2&a=1');
    assert.equal(product.bypass, undefined);
});

test('a request that could carry a shopper, a draft or an override is bypassed, with its reason', () => {
    const policy = loadPolicy({});
    const reasons = [
        [request('/en', {method: 'POST'}), 'method'],
        [request('/en', {headers: {authorization: 'Bearer x'}}), 'authorization'],
        [request('/en', {headers: {rsc: '1'}}), 'rsc'],
        [request('/en', {headers: {'next-router-prefetch': '1'}}), 'rsc'],
        [request('/en', {headers: {'x-middleware-prefetch': '1'}}), 'rsc'],
        [request('/en', {headers: {cookie: 'NEXT_LOCALE=en; storefront-theme=basic'}}), 'override-cookie'],
        [request('/en?theme=basic'), 'override-param'],
        [request('/en/content/about?preview=tok'), 'override-param'],
        [request('//evil.example/en'), 'path'],
        [request('/en/login'), 'class-disabled'],
        [request('/en/something-else'), 'class-disabled'],
    ];
    for (const [req, reason] of reasons) {
        assert.equal(classify(req, policy).bypass, reason, `${req.method} ${req.url}`);
    }
    assert.equal(classify(request('/en', {headers: {cookie: 'NEXT_LOCALE=en'}}), policy).bypass, undefined);
    assert.equal(classify(request('/en', {method: 'HEAD'}), policy).bypass, undefined);
    assert.equal(classify(request('/en'), loadPolicy({STOREFRONT_CACHE_ENABLED: 'false'})).bypass, 'cache-disabled');
});

test('the JSON policy merges over the defaults, and the flat variables win over the JSON', () => {
    const policy = loadPolicy({
        STOREFRONT_CACHE_POLICY_JSON: JSON.stringify({maxMb: 8, dropParams: ['x'], classes: {product: {ttl: 5, swr: 10}, help: {enabled: false}}}),
        STOREFRONT_CACHE_PRODUCT_TTL_SECONDS: '7',
        STOREFRONT_CACHE_SEARCH_ENABLED: 'false',
        STOREFRONT_CACHE_API_THEME_MANIFEST_ENABLED: 'false',
        STOREFRONT_CACHE_DEBUG: 'true',
        STOREFRONT_CACHE_MAX_ENTRY_KB: '256',
    });
    assert.equal(policy.maxBytes, 8 * 1024 * 1024);
    assert.equal(policy.maxEntryBytes, 256 * 1024);
    assert.deepEqual(policy.dropParams, ['x']);
    assert.equal(policy.debug, true);
    const product = policy.classes.get('product');
    assert.equal(product.ttlMs, 7_000);
    assert.equal(product.swrMs, 10_000);
    assert.equal(product.edge, 'public, s-maxage=7, stale-while-revalidate=10');
    assert.equal(policy.classes.get('help').enabled, false);
    assert.equal(policy.classes.get('help').edge, 'private, no-store');
    assert.equal(policy.classes.get('search').enabled, false);
    assert.equal(policy.classes.get('category').ttlMs, 30_000, 'untouched classes keep their defaults');
});

test('a class whose ttl is 0 is off; an explicit edge value is kept as written', () => {
    const policy = loadPolicy({STOREFRONT_CACHE_HOME_TTL_SECONDS: '0',
        STOREFRONT_CACHE_POLICY_JSON: JSON.stringify({classes: {help: {edge: 'public, max-age=60'}}})});
    assert.equal(policy.classes.get('home').enabled, false);
    assert.equal(classify(request('/en'), policy).bypass, 'class-disabled');
    assert.equal(policy.classes.get('help').edge, 'public, max-age=60');
});

test('a policy the cache cannot run with is refused at start', () => {
    const refused = [
        {STOREFRONT_CACHE_POLICY_JSON: '{'},
        {STOREFRONT_CACHE_POLICY_JSON: '[]'},
        {STOREFRONT_CACHE_POLICY_JSON: '{"classes":{"basket":{"ttl":1}}}'},
        {STOREFRONT_CACHE_POLICY_JSON: '{"speed":"fast"}'},
        {STOREFRONT_CACHE_POLICY_JSON: '{"classes":{"home":{"share":0.9}}}'},
        {STOREFRONT_CACHE_PRODUCT_TTL_SECONDS: '-1'},
        {STOREFRONT_CACHE_PRODUCT_TTL_SECONDS: '1.5'},
        {STOREFRONT_CACHE_PRODUCT_TTL_SECONDS: 'soon'},
        {STOREFRONT_CACHE_ENABLED: 'yes'},
        {STOREFRONT_CACHE_MAX_MB: '0'},
        {STOREFRONT_CACHE_POLICY_JSON: '{"classes":{"home":{"edge":""}}}'},
    ];
    for (const env of refused) {
        assert.throws(() => loadPolicy(env), PolicyError, JSON.stringify(env));
    }
});

test('an override layer sits between the JSON and the flat variables', () => {
    const policy = loadPolicy({STOREFRONT_CACHE_POLICY_JSON: '{"classes":{"home":{"ttl":1}}}', STOREFRONT_CACHE_HOME_SWR_SECONDS: '9'},
        {overrides: [{classes: {home: {ttl: 2, swr: 3}}}]});
    assert.equal(policy.classes.get('home').ttlMs, 2_000);
    assert.equal(policy.classes.get('home').swrMs, 9_000);
});

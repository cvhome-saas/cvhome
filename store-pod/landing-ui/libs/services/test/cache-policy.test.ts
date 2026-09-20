import {strict as assert} from 'node:assert';
import {test} from 'node:test';
import {DATA_CACHE_DEFAULT_SECONDS, dataCacheSeconds, dataCacheVariable} from '../src/cache-policy';

test('every read has a default, and the default answers when the environment says nothing', () => {
    for (const name of Object.keys(DATA_CACHE_DEFAULT_SECONDS) as (keyof typeof DATA_CACHE_DEFAULT_SECONDS)[]) {
        assert.equal(dataCacheSeconds(name, {}), DATA_CACHE_DEFAULT_SECONDS[name]);
    }
});

test('the variable is the upper-cased name, a camel hump becoming an underscore', () => {
    assert.equal(dataCacheVariable('store'), 'STOREFRONT_DATA_CACHE_STORE_SECONDS');
    assert.equal(dataCacheVariable('productGroup'), 'STOREFRONT_DATA_CACHE_PRODUCT_GROUP_SECONDS');
});

test('the environment overrides a read, and zero turns it off', () => {
    assert.equal(dataCacheSeconds('product', {STOREFRONT_DATA_CACHE_PRODUCT_SECONDS: '5'}), 5);
    assert.equal(dataCacheSeconds('product', {STOREFRONT_DATA_CACHE_PRODUCT_SECONDS: '0'}), 0);
});

test('a value that is not a whole number of seconds is ignored', () => {
    for (const value of ['', ' ', 'soon', '-1', '1.5']) {
        assert.equal(dataCacheSeconds('listing', {STOREFRONT_DATA_CACHE_LISTING_SECONDS: value}),
            DATA_CACHE_DEFAULT_SECONDS.listing, JSON.stringify(value));
    }
});

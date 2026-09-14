import {strict as assert} from 'node:assert';
import {test} from 'node:test';
import {currencyFormatter} from '../src/currency-format';

test('formats exactly as a formatter built for the call would', () => {
    for (const [locale, currency, amount] of [['en', 'USD', 999], ['fr', 'EUR', 1234.5], ['ar', 'EGP', 19.99]] as const) {
        const expected = new Intl.NumberFormat(locale, {style: 'currency', currency}).format(amount);
        assert.equal(currencyFormatter(locale, currency)?.format(amount), expected);
    }
});

test('one formatter per locale and currency, reused across calls', () => {
    const usd = currencyFormatter('en', 'USD');
    assert.ok(usd);
    assert.equal(currencyFormatter('en', 'USD'), usd);
    assert.notEqual(currencyFormatter('en', 'EUR'), usd);
    assert.notEqual(currencyFormatter('fr', 'USD'), usd);
});

test('a currency code Intl rejects gives undefined, every time', () => {
    assert.equal(currencyFormatter('en', 'DOLLAR'), undefined);
    assert.equal(currencyFormatter('en', 'DOLLAR'), undefined);
    assert.equal(currencyFormatter('en', ''), undefined);
});

test('the cache is bounded: past its size it starts over and still formats', () => {
    const before = currencyFormatter('en', 'USD');
    // 3-letter codes are well-formed for Intl whether or not they name a real currency
    for (let i = 0; i < 300; i++) {
        const code = String.fromCharCode(65 + (i % 26), 65 + Math.floor(i / 26) % 26, 81);
        currencyFormatter('en', code);
    }
    const after = currencyFormatter('en', 'USD');
    assert.notEqual(after, before);
    assert.equal(after?.format(5), before?.format(5));
});

import assert from 'node:assert/strict';
import test from 'node:test';
import {isUnsupportedCjkShard, parseUnicodeRange} from './font-subsets.mjs';

test('parses fixed and wildcard Unicode ranges', () => {
    assert.deepEqual(parseUnicodeRange('U+0600-06FF, U+1F??'), [[0x600, 0x6ff], [0x1f00, 0x1fff]]);
});

test('keeps the script subsets used by storefront locales', () => {
    assert.equal(isUnsupportedCjkShard('@font-face{unicode-range:U+??,U+0131,U+0152-0153}'), false);
    assert.equal(isUnsupportedCjkShard('@font-face{unicode-range:U+0400-052F,U+2DE0-2DFF}'), false);
    assert.equal(isUnsupportedCjkShard('@font-face{unicode-range:U+0600-06FF,U+FB50-FDFF}'), false);
});

test('drops CJK shards even when they contain stray punctuation or one Latin glyph', () => {
    assert.equal(isUnsupportedCjkShard('@font-face{unicode-range:U+004E,U+3000,U+4E00-4EFF}'), true);
    assert.equal(isUnsupportedCjkShard('@font-face{unicode-range:U+2227,U+26A0,U+4E00-4EFF}'), true);
});

test('keeps metric fallbacks and unparseable declarations', () => {
    assert.equal(isUnsupportedCjkShard('@font-face{font-family:Example Fallback}'), false);
    assert.equal(isUnsupportedCjkShard('@font-face{unicode-range:invalid}'), false);
});

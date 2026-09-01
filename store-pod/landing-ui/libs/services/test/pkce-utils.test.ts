import {test} from 'node:test';
import assert from 'node:assert/strict';
import {base64url, generateCodeChallenge, generateCodeVerifier} from '../src/pkce-utils';

test('S256 challenge matches the RFC 7636 appendix B vector', async () => {
    const challenge = await generateCodeChallenge('dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk');
    assert.equal(challenge, 'E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM');
});

test('verifier is 43 unreserved characters and unique per call', () => {
    const a = generateCodeVerifier();
    const b = generateCodeVerifier();
    assert.match(a, /^[A-Za-z0-9\-_]{43}$/);
    assert.notEqual(a, b);
});

test('base64url drops padding and uses the url-safe alphabet', () => {
    assert.equal(base64url(new Uint8Array([0xfb, 0xff])), '-_8');
    assert.equal(base64url(new Uint8Array([])), '');
});

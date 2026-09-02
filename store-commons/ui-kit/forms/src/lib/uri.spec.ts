import {FormControl} from '@angular/forms';

import {isInsecureUri, uriValidator} from './uri';

describe('uriValidator', () => {
  const check = (value: string) => new FormControl(value, uriValidator()).errors;

  it('accepts https', () => {
    expect(check('https://console.example.com/callback')).toBeNull();
  });

  it('accepts a custom scheme, which a mobile redirect URI is', () => {
    expect(check('com.example.app://oauth/callback')).toBeNull();
  });

  it('rejects a bare host with no scheme', () => {
    expect(check('console.example.com/callback')).toEqual({url: true});
  });

  it('rejects a scheme with nothing after it', () => {
    expect(check('https://')).toEqual({url: true});
  });

  it('leaves an empty control to Validators.required', () => {
    expect(check('')).toBeNull();
    expect(check('   ')).toBeNull();
  });
});

describe('isInsecureUri', () => {
  it('flags plain http to a remote host', () => {
    expect(isInsecureUri('http://console.example.com/callback')).toBe(true);
  });

  it('does not flag loopback, where there is no path to listen on', () => {
    expect(isInsecureUri('http://localhost:4200/callback')).toBe(false);
    expect(isInsecureUri('http://127.0.0.1:8080/cb')).toBe(false);
    expect(isInsecureUri('http://[::1]:8080/cb')).toBe(false);
  });

  it('does not flag https, a custom scheme, or nothing', () => {
    expect(isInsecureUri('https://example.com')).toBe(false);
    expect(isInsecureUri('com.example.app://cb')).toBe(false);
    expect(isInsecureUri(null)).toBe(false);
  });

  /* `http://localhost.attacker.example` is not loopback, and the prefix check alone would say it is. */
  it('flags a host that merely starts with localhost', () => {
    expect(isInsecureUri('http://localhost.attacker.example/cb')).toBe(true);
  });
});

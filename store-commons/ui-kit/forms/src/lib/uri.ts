import type {AbstractControl, ValidationErrors, ValidatorFn} from '@angular/forms';

/**
 * An absolute URI with a scheme — `https://…`, and also `com.example.app://callback`.
 *
 * Deliberately wider than "a web address". An OAuth redirect URI is frequently a custom scheme
 * registered by a mobile app, and a validator that only accepted `http`/`https` would refuse the
 * one case that most needs typing correctly. The scheme grammar is RFC 3986's.
 */
export const ABSOLUTE_URI_PATTERN = /^[a-z][a-z0-9+.-]*:\/\/[^\s]+$/i;

/** Loopback, in the two spellings a redirect URI uses. */
const LOOPBACK = /^http:\/\/(localhost|127\.0\.0\.1|\[::1\])(:\d+)?(\/|$)/i;

/**
 * An absolute URI, or `{url: true}`.
 *
 * Reuses the `url` message the shared map already carries rather than introducing a key: a call
 * site with something more specific to say passes `fallback` on its `app-field-error`, which takes
 * precedence.
 *
 * An empty control is left alone, as every Angular validator except `required` does — a field that
 * is both optional and format-checked is the common case, and combining the two here would make
 * `Validators.required` unavoidable.
 */
export function uriValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = typeof control.value === 'string' ? control.value.trim() : '';
    if (!value) {
      return null;
    }
    return ABSOLUTE_URI_PATTERN.test(value) ? null : {url: true};
  };
}

/**
 * Whether a URI sends credentials over plain HTTP to somewhere that is not this machine.
 *
 * Not a validation failure: `http://` is legal in a redirect URI and uaa will accept it. It is
 * worth flagging on sight all the same, because an authorization code delivered over plain HTTP to
 * a remote host is readable by anything on the path, and a redirect URI is exactly where that
 * mistake gets made and then never looked at again.
 */
export function isInsecureUri(value: string | null | undefined): boolean {
  const uri = value?.trim() ?? '';
  return /^http:\/\//i.test(uri) && !LOOPBACK.test(uri);
}

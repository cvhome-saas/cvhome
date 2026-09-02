import {Signal, computed} from '@angular/core';

/**
 * A route parameter that has to be a positive integer, or `null`.
 *
 * **Validated before it reaches a facade**, which is the rule Module 4 arrived at the hard way:
 * `/orders/abc` reached the server as `orders/NaN`, came back a 500, and read to the operator as
 * "the order failed to load" rather than "there is no such order". A reference the console cannot
 * even parse is answered without a request.
 *
 * Order details and the product form each wrote this, differently — one as a `computed` feeding an
 * effect, the other as an imperative check inside one — so a third page would have written a third.
 * The `computed` form is the one kept: it is readable from a template, which is what lets the page
 * render "no such order" instead of an empty state.
 */
export function positiveIntParam(raw: Signal<string | undefined>): Signal<number | null> {
  return computed(() => {
    const value = raw();
    if (value === undefined) {
      return null;
    }
    const parsed = Number(value);
    return Number.isInteger(parsed) && parsed > 0 ? parsed : null;
  });
}

/**
 * A route parameter that has to be one of a known set — a settings section, a catalogue tab.
 *
 * Falls back to the first member rather than to `null`: unlike an id, an unrecognised section is
 * not a missing record, it is a URL the operator typed or a link that has gone stale, and the
 * sensible answer is the first tab rather than an error page. The caller redirects if it wants the
 * URL corrected.
 */
export function enumParam<T extends string>(
  raw: Signal<string | undefined>,
  allowed: readonly T[],
): Signal<T> {
  return computed(() => {
    const value = raw();
    return allowed.includes(value as T) ? (value as T) : allowed[0];
  });
}

package com.asrevo.cvhome.billing.api.v1;

/**
 * How a subscription should end.
 *
 * @param immediate end it now, throwing away the remainder of the paid period, rather than letting it run out.
 *                  Administrators only — self-serve cancellation is always at period end.
 */
public record CancelRequest(boolean immediate) {
}

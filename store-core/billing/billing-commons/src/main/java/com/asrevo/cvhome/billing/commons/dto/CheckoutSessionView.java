package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;

/**
 * Where to send the customer to pay.
 *
 * <p>
 * Returned as a body rather than as a redirect on purpose: a single-page console needs to open the URL itself, and a
 * 302 out of an XHR is not something a browser lets it follow usefully. It also makes the endpoint assertable from a
 * {@code .http} file.
 * </p>
 *
 * @param url the provider-hosted checkout page
 */
public record CheckoutSessionView(String url) implements Serializable {
}

package com.asrevo.cvhome.billing.api.v1;

import jakarta.validation.constraints.NotNull;

import com.asrevo.cvhome.billing.commons.PlanPriceId;

/**
 * What a customer wants to buy.
 *
 * <p>
 * A price rather than a plan: the plan says what they get, the price says what they pay and how often, and only the
 * price answers both. Where Stripe returns them afterwards is deliberately not a field — a caller-supplied return URL
 * would be an open redirect out of a payment flow.
 * </p>
 *
 * @param planPriceId the price to subscribe at
 */
public record CheckoutRequest(@NotNull PlanPriceId planPriceId) {
}

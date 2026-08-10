package com.asrevo.cvhome.billing.api.v1;

import jakarta.validation.constraints.NotNull;

import com.asrevo.cvhome.billing.commons.PlanPriceId;

/**
 * The plan a store wants to move to.
 *
 * <p>
 * There is no "upgrade" or "downgrade" flag, deliberately. The direction follows from the catalog — tier first, price
 * as the tie-break — so a client cannot ask for an immediate move to a cheaper plan and take away a month the
 * customer already paid for.
 * </p>
 *
 * @param planPriceId the price to move to
 */
public record PlanChangeRequest(@NotNull PlanPriceId planPriceId) {
}

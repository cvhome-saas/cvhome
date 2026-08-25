package com.asrevo.cvhome.billing.repository.projection;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.commons.domain.CurrencyCode;

/**
 * One plan's annualised run rate in one currency, for one lifecycle state.
 *
 * <p>
 * Annualised in SQL and divided in Java: multiplying a monthly price by twelve is exact in {@code bigint}, whereas
 * dividing a yearly one by twelve truncates on every row.
 * </p>
 */
public record PlanRecurringValueRow(String planCode, SubscriptionStatus status, CurrencyCode currency,
                                    long subscriptions, Long annual) {
}

package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;

/**
 * How many stores sit on one plan in one state.
 *
 * <p>
 * {@code planCode} is <strong>nullable</strong>, and that is the point of the left join behind it: a {@code PENDING}
 * store has no plan at all, and an inner join would hide exactly the rows an operator opened the screen to find.
 * </p>
 */
public record PlanSubscriptionCount(String planCode, String planDisplayName, Integer tier, SubscriptionStatus status,
                                    long subscriptions) implements Serializable {
}

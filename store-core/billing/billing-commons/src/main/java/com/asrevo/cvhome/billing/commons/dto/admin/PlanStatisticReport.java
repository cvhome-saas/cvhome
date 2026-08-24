package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;
import java.util.List;

/**
 * The commercial reading of the plan catalogue: who is on what, and what that is worth.
 *
 * <p>
 * Its own record rather than a {@code StatisticList}, because it carries counts <em>and</em> money in two
 * dimensions and {@code StatisticEntry.value} is a single {@code Number}.
 * </p>
 */
public record PlanStatisticReport(List<PlanSubscriptionCount> counts, List<PlanRecurringValue> recurringValue)
        implements Serializable {
}

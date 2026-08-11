package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;
import java.time.Instant;

import com.asrevo.cvhome.billing.commons.PlanPriceId;

/**
 * A plan change that has been agreed but has not taken effect yet — always a downgrade, which is deferred to the end
 * of the period the customer has already paid for.
 *
 * @param planPriceId the price that will apply
 * @param planCode    its plan's handle, so a client need not resolve the catalog to render this
 * @param effectiveAt when it takes over
 */
public record PendingPlanChangeView(PlanPriceId planPriceId, String planCode, Instant effectiveAt)
        implements Serializable {
}

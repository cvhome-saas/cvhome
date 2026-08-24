package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;

/**
 * Whether billing itself is working — the only "this is broken right now" signal the platform has.
 *
 * <p>
 * Both counts come from tables nothing has ever read. They are not a status: a non-zero figure is a prompt to look
 * at the logs, not a diagnosis.
 * </p>
 *
 * @param failedEvents     inbound Stripe events recorded {@code FAILED} in the last day
 * @param stalledRequests  mutating Stripe calls recorded and never completed — the intent row was written and the
 *                         call did not come back, which is the shape a crash mid-call leaves behind
 * @param staleAfterMinutes how old a request has to be to count as stalled, so the console can say what it counted
 */
public record BillingHealthView(long failedEvents, long stalledRequests, int staleAfterMinutes)
        implements Serializable {
}

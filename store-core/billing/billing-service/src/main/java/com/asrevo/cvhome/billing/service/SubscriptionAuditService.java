package com.asrevo.cvhome.billing.service;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;

/**
 * Writes the subscription audit trail.
 *
 * <p>
 * Called from the service layer inside the same transaction as the save it describes, so a state change and its
 * record either both land or neither does. Never called from a controller: the controller does not know which
 * transition actually happened, only which one was asked for.
 * </p>
 */
public interface SubscriptionAuditService {

    /**
     * Records a change, capturing where the subscription came from and where it went.
     *
     * @param before      the state before the change — pass {@code null} for a row being created
     * @param after       the subscription after the change
     * @param eventType   what happened
     * @param source      who drove it
     * @param actor       the principal, the provider, or the job name
     */
    void record(SubscriptionStatus before, StoreSubscriptionEntity after, AuditEventType eventType,
                ChangeSource source, String actor);

    /**
     * As {@link #record}, additionally naming the provider event that caused it — which is what lets a support
     * question be answered from the provider's dashboard and ours at the same time.
     */
    void recordFromWebhook(SubscriptionStatus before, StoreSubscriptionEntity after, AuditEventType eventType,
                           StripeEventId eventId);

}

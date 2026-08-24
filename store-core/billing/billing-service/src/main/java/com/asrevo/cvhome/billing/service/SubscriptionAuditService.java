package com.asrevo.cvhome.billing.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.commons.AuditEventType;
import com.asrevo.cvhome.billing.commons.ChangeSource;
import com.asrevo.cvhome.billing.commons.PlanId;
import com.asrevo.cvhome.billing.commons.StripeEventId;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.commons.dto.admin.ListAuditQuery;
import com.asrevo.cvhome.billing.commons.dto.admin.SubscriptionAuditView;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;

/**
 * The subscription audit trail: written by the service layer, read by the platform console.
 *
 * <p>
 * The writers are called from inside the same transaction as the save they describe, so a state change and its
 * record either both land or neither does. Never called from a controller: the controller does not know which
 * transition actually happened, only which one was asked for.
 * </p>
 *
 * <p>
 * <strong>This interface was write-only until the platform console needed it.</strong> Every plan change and every
 * payment failure since the service was written sat in {@code billing.subscription_audit} with no endpoint able to
 * read it — {@link #search} is the read that makes "who moved this store onto the cheaper plan, and when" answerable
 * from a browser rather than from psql.
 * </p>
 */
public interface SubscriptionAuditService {

    /**
     * Records a change, capturing where the subscription came from and where it went.
     *
     * <p>
     * {@code fromPlan} is passed rather than derived because the entity mutates in place: by the time this is
     * called, {@code after.getPlanId()} is already the <em>new</em> plan. It was a literal {@code null} at both
     * write sites until the platform's Activity screen needed it, so every {@code PLAN_UPGRADED} row on the
     * platform records the plan the store landed on and not the one it left — half of the sentence the row exists
     * to make.
     * </p>
     *
     * @param before      the state before the change — pass {@code null} for a row being created
     * @param fromPlan    the plan in force before the change, or {@code null} where there was none
     * @param after       the subscription after the change
     * @param eventType   what happened
     * @param source      who drove it
     * @param actor       the principal, the provider, or the job name
     */
    void record(SubscriptionStatus before, PlanId fromPlan, StoreSubscriptionEntity after, AuditEventType eventType,
                ChangeSource source, String actor);

    /**
     * As {@link #record}, additionally naming the provider event that caused it — which is what lets a support
     * question be answered from the provider's dashboard and ours at the same time.
     */
    void recordFromWebhook(SubscriptionStatus before, PlanId fromPlan, StoreSubscriptionEntity after,
                           AuditEventType eventType, StripeEventId eventId);

    /**
     * One page of the trail, newest first, narrowed by the query.
     *
     * <p>
     * <strong>{@code readOnly = true}, not {@code Propagation.MANDATORY} like the two writers above.</strong> They
     * are called from inside the transaction they describe; this one is called from a controller with no ambient
     * transaction, and copying their propagation would throw {@code IllegalTransactionStateException} on every
     * request.
     * </p>
     */
    Page<SubscriptionAuditView> search(ListAuditQuery query, Pageable pageable);

}

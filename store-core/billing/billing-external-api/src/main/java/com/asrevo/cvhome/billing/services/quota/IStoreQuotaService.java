package com.asrevo.cvhome.billing.services.quota;

import org.springframework.web.bind.annotation.RequestBody;

import com.asrevo.cvhome.billing.commons.dto.ProvisionSubscriptionRequest;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaDecision;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaRequest;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.PlanNotFoundException;

/**
 * Billing's store-provisioning contract, in billing's own vocabulary.
 *
 * <p>
 * Implemented by billing's controller, which is why the {@code throws} clauses name server-side exceptions. Callers
 * depend on {@link ExternalStoreQuotaService} instead, whose clauses are the caller's truth.
 * </p>
 */
public interface IStoreQuotaService {

    /**
     * Whether this org may create another store, and what the store would start as.
     *
     * <p>
     * Answers rather than throws when the org may not: a refusal is a decision the caller renders, and the decision
     * carries the reason and whether a trial is still available.
     * </p>
     */
    StoreQuotaDecision checkStoreCreate(@RequestBody StoreQuotaRequest request);

    /**
     * Gives a freshly created store its subscription — a trial if the org still has one to spend, otherwise unpaid.
     *
     * @throws PlanNotFoundException the catalog has no plan to start a trial on
     */
    SubscriptionView provision(@RequestBody ProvisionSubscriptionRequest request) throws PlanNotFoundException;

}

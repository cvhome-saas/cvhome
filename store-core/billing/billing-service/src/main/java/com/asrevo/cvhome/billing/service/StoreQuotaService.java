package com.asrevo.cvhome.billing.service;

import com.asrevo.cvhome.billing.commons.dto.StoreQuotaDecision;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.PlanNotFoundException;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The gate in front of store creation, and the provisioning that follows it.
 */
public interface StoreQuotaService {

    /**
     * Whether an org may create another store, and what that store would start as.
     */
    StoreQuotaDecision checkStoreCreate(ManagerOrgId org);

    /**
     * Gives a freshly created store its subscription — the org's one trial if it still has it, otherwise unpaid.
     *
     * <p>
     * Idempotent: a store that already has a subscription gets that one back. It has to be, because this is driven
     * from an outbox handler that retries.
     * </p>
     *
     * @throws PlanNotFoundException the catalog has nothing to start a trial on
     */
    SubscriptionView provision(ManagerOrgId org, StoreMerchantId store) throws PlanNotFoundException;

}

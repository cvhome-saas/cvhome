package com.asrevo.cvhome.billing.services.quota;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.billing.api.errors.BillingApiUnavailableException;
import com.asrevo.cvhome.billing.api.errors.StoreQuotaRefusedException;
import com.asrevo.cvhome.billing.commons.dto.ProvisionSubscriptionRequest;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaDecision;
import com.asrevo.cvhome.billing.commons.dto.StoreQuotaRequest;
import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;

/**
 * What a caller of billing's store-provisioning API depends on.
 *
 * <p>
 * Nothing implements this: {@code RestClientBuilder.buildClient(...)} generates the proxy from it, and because the
 * error handler narrows a carrier only into types the invoked method declares, naming the caller-side exceptions here
 * is what makes them arrive as themselves rather than wrapped.
 * </p>
 *
 * <p>
 * The paths below are not checked against the controller's mappings by any compiler. Keep them in step by eye.
 * </p>
 */
@HttpExchange("/api/v1/quota/private")
public interface ExternalStoreQuotaService {

    /**
     * Whether this org may create another store.
     *
     * @throws BillingApiUnavailableException billing could not be reached. Store creation treats this as a refusal:
     *                                        a store nobody is billed for is worse than a retryable failure.
     */
    @PostExchange("/store-create")
    StoreQuotaDecision checkStoreCreate(@RequestBody StoreQuotaRequest request)
            throws BillingApiUnavailableException;

    /**
     * Gives a freshly created store its subscription. Idempotent — safe to retry, which the outbox will.
     *
     * @throws StoreQuotaRefusedException     billing declined to provision the store
     * @throws BillingApiUnavailableException billing could not be reached, so nothing was decided
     */
    @PostExchange("/provision")
    SubscriptionView provision(@RequestBody ProvisionSubscriptionRequest request)
            throws StoreQuotaRefusedException, BillingApiUnavailableException;

}

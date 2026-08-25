package com.asrevo.cvhome.billing.service;

import java.util.List;

import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The read model every enforcement layer asks: what may this store do?
 *
 * <p>
 * Deliberately the only thing the other services depend on. There is no message broker between store-core and
 * store-pod, so a pod cannot be told when a subscription changes — it asks, briefly caches the answer, and carries on
 * if billing is unreachable. That makes the cache lifetime the real freshness knob for the whole platform.
 * </p>
 */
public interface EntitlementService {

    /**
     * @throws SubscriptionNotFoundException billing has never seen this store
     */
    EntitlementSnapshot snapshot(StoreMerchantId store) throws SubscriptionNotFoundException;

    /**
     * Several stores in one call, omitting any billing does not know.
     */
    List<EntitlementSnapshot> snapshots(List<StoreMerchantId> stores);

    /**
     * Every store that may not be worked in right now.
     */
    List<StoreMerchantId> blockedStores();

}

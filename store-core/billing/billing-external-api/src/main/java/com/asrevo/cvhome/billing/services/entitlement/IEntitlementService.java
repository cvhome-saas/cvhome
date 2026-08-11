package com.asrevo.cvhome.billing.services.entitlement;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;

import com.asrevo.cvhome.billing.commons.dto.EntitlementSnapshot;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

/**
 * What a store is allowed to do, in billing's own vocabulary.
 *
 * <p>
 * Implemented by billing's controller, so the {@code throws} clauses name server-side exceptions. Callers depend on
 * {@link ExternalEntitlementService} — or {@link ReactiveExternalEntitlementService} from the gateway — instead.
 * </p>
 */
public interface IEntitlementService {

    /**
     * One store's standing and ceilings.
     *
     * @throws SubscriptionNotFoundException billing has never seen this store
     */
    EntitlementSnapshot snapshot(ManagerStoreId store) throws SubscriptionNotFoundException;

    /**
     * Several stores at once, skipping any billing does not know.
     *
     * <p>
     * Exists so a caller rendering a list of stores makes one call rather than one per row. Absent stores are omitted
     * rather than erroring: a list of stores is not wrong because one of them has not been provisioned yet.
     * </p>
     */
    List<EntitlementSnapshot> snapshots(@RequestBody List<ManagerStoreId> stores);

    /**
     * Every store that must not be worked in — suspended, cancelled, or never paid for.
     *
     * <p>
     * Ids only, and the whole set at once, because the gateway holds this in memory and consults it on every request.
     * Asking billing per request would put a billing outage in the path of all seller traffic.
     * </p>
     */
    List<ManagerStoreId> blockedStores();

}

package com.asrevo.cvhome.billing.events;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.event.Event;

/**
 * Something that happened to one store's subscription.
 *
 * <p>
 * Every one carries the store, and every one is scheduled on the outbox under the store id as its partition key, so a
 * single store's transitions are handled in the order they happened. Ordering across stores is not promised and is
 * not needed.
 * </p>
 *
 * <p>
 * These do not cross the service boundary — there is no broker between store-core and store-pod. They drive billing's
 * own reactions: notifications, the entitlement cache, the audit trail. A pod that needs to know a store's standing
 * asks for its entitlement snapshot instead.
 * </p>
 */
public interface SubscriptionEvent extends Event {

    ManagerStoreId store();

}

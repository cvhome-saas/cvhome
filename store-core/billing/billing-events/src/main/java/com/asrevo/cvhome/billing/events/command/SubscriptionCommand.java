package com.asrevo.cvhome.billing.events.command;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.event.Event;

/**
 * Work a scheduled job wants done to one store's subscription.
 *
 * <p>
 * The jobs never do the work themselves. They run on every instance, find the due rows and write a command here,
 * keyed by store id — the outbox partitions on that key, so exactly one instance ends up acting on each store. That
 * is what stands in for a distributed lock, which this codebase does not have.
 * </p>
 */
public interface SubscriptionCommand extends Event {

    ManagerStoreId store();

}

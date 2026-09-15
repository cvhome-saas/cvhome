package com.asrevo.cvhome.cache.event;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.event.Event;

/**
 * A change a shopper can see, told to every cache that shows it: the one event family the cache library knows.
 *
 * <p>
 * An aggregate registers one of these in the transaction that changed it ({@code Product.domainEvents()},
 * {@code MerchantStore.changed()}, {@code Inventory.stockChanged()}); the outbox writes it with the change and
 * hands it to {@link CacheEventOutboxHandler} once committed. Every event names the store it belongs to, because
 * every cached read is keyed by store and dropping a store's entries is the one thing a consumer does with it. The
 * family is sealed so that a handler, a transport and a rules builder can each cover it completely.
 * </p>
 *
 * <p>
 * Partitioned by store: two changes of one store are applied in the order they happened, and one merchant's burst
 * never queues behind another's. Eviction is idempotent, so a redelivery costs one cache miss and nothing else.
 * </p>
 */
public sealed interface CacheEvent extends Event
        permits ProductChanged, VariantChanged, CategoryChanged, ManufacturerChanged, StockChanged, PriceChanged,
        StoreChanged, ContentChanged {

    /** The store whose cached reads the change stales. */
    StoreMerchantId store();

    /** The outbox partitions on a string; every event of one store shares one. Not a component, so not in the JSON. */
    default String partitionKey() {
        return store().getId();
    }

    @Override
    default String eventType() {
        return getClass().getSimpleName();
    }
}

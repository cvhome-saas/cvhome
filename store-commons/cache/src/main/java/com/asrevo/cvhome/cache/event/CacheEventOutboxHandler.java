package com.asrevo.cvhome.cache.event;

import io.namastack.outbox.annotation.OutboxHandler;

/**
 * Drains the cache events of this service: applies each to this task's regions, then hands it to the transport.
 *
 * <p>
 * One method per event type because the outbox binds a handler to the concrete class it declares; every method
 * does the same two things in the same order. Idempotent, as every outbox handler must be: applying twice evicts an
 * entry that is already gone, and a transport is asked to tolerate a repeat.
 * </p>
 */
public final class CacheEventOutboxHandler {

    private final CacheEventApplier applier;

    private final CacheEventTransport transport;

    public CacheEventOutboxHandler(CacheEventApplier applier, CacheEventTransport transport) {
        this.applier = applier;
        this.transport = transport;
    }

    @OutboxHandler
    public void onProductChanged(ProductChanged event) {
        handle(event);
    }

    @OutboxHandler
    public void onVariantChanged(VariantChanged event) {
        handle(event);
    }

    @OutboxHandler
    public void onCategoryChanged(CategoryChanged event) {
        handle(event);
    }

    @OutboxHandler
    public void onManufacturerChanged(ManufacturerChanged event) {
        handle(event);
    }

    @OutboxHandler
    public void onStockChanged(StockChanged event) {
        handle(event);
    }

    @OutboxHandler
    public void onPriceChanged(PriceChanged event) {
        handle(event);
    }

    @OutboxHandler
    public void onStoreChanged(StoreChanged event) {
        handle(event);
    }

    @OutboxHandler
    public void onContentChanged(ContentChanged event) {
        handle(event);
    }

    /** Local first: the task that drained the event never serves the stale entry while the transport is slow. */
    void handle(CacheEvent event) {
        applier.apply(event);
        transport.publish(event);
    }
}

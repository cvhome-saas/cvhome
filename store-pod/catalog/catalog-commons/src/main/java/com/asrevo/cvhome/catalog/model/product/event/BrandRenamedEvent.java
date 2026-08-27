package com.asrevo.cvhome.catalog.model.product.event;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A brand's name changed in some language. The brand name is part of every one of its products' search documents,
 * so all of them need rebuilding.
 *
 * <p>
 * Keyed by the manufacturer, so a rename is one ordered stream of work instead of a burst of per-product events —
 * the handler walks the brand's products in batches rather than the outbox holding one record per product.
 * </p>
 */
@OutboxEvent(key = "#this.partitionKey()")
public record BrandRenamedEvent(Long manufacturerId, String storeId, Map<String, String> data) implements Event {

    public static BrandRenamedEvent from(Long manufacturerId, String storeId) {
        return new BrandRenamedEvent(manufacturerId, storeId, Map.of());
    }

    /**
     * The outbox partitions on a string and the id is a number, so the key is spelled out here — the annotation
     * cannot convert one.
     */
    public String partitionKey() {
        return String.valueOf(manufacturerId);
    }

    @Override
    public String eventType() {
        return BrandRenamedEvent.class.getSimpleName();
    }

    @Override
    public Map<String, String> data() {
        return Map.of("manufacturerId", String.valueOf(manufacturerId), "storeId", storeId);
    }
}

package com.asrevo.cvhome.catalog.model.product.event;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A product changed in a way the search index has to be told about — its copy, sku, brand or categories.
 *
 * <p>
 * Partitioned by the product rather than by the store. Two edits to the same product have to be applied in the
 * order they happened, or the index ends up holding the older one; two different products have no such
 * relationship, and keying on the store would queue a whole merchant's catalogue behind a single slow refresh.
 * </p>
 */
@OutboxEvent(key = "#this.partitionKey()")
public record ProductSearchIndexStaleEvent(Long productId, String storeId, Map<String, String> data)
        implements Event {

    public static ProductSearchIndexStaleEvent from(Long productId, String storeId) {
        return new ProductSearchIndexStaleEvent(productId, storeId, Map.of());
    }

    /**
     * The outbox partitions on a string and the id is a number, so the key is spelled out here — the annotation
     * cannot convert one.
     */
    public String partitionKey() {
        return String.valueOf(productId);
    }

    @Override
    public String eventType() {
        return ProductSearchIndexStaleEvent.class.getSimpleName();
    }

    @Override
    public Map<String, String> data() {
        return Map.of("productId", String.valueOf(productId), "storeId", storeId);
    }
}

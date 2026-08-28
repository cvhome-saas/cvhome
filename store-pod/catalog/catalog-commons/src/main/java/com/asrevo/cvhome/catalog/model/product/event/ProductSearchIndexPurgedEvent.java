package com.asrevo.cvhome.catalog.model.product.event;

import java.util.Map;

import com.asrevo.cvhome.commons.event.Event;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A product is gone and its index rows have to go with it.
 *
 * <p>
 * Shares the product partition key with {@link ProductSearchIndexStaleEvent} on purpose: a delete that overtook a
 * pending refresh would leave the index holding a product the catalogue no longer has.
 * </p>
 */
@OutboxEvent(key = "#this.partitionKey()")
public record ProductSearchIndexPurgedEvent(Long productId, String storeId, Map<String, String> data)
        implements Event {

    public static ProductSearchIndexPurgedEvent from(Long productId, String storeId) {
        return new ProductSearchIndexPurgedEvent(productId, storeId, Map.of());
    }

    /**
     * The same key {@link ProductSearchIndexStaleEvent} uses, so the two stay in order relative to each other.
     */
    public String partitionKey() {
        return String.valueOf(productId);
    }

    @Override
    public String eventType() {
        return ProductSearchIndexPurgedEvent.class.getSimpleName();
    }

    @Override
    public Map<String, String> data() {
        return Map.of("productId", String.valueOf(productId), "storeId", storeId);
    }
}

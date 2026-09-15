package com.asrevo.cvhome.cache.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ProductId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/** A product, one of its descriptions, images, options or variants was written: every read that shows it is stale. */
@OutboxEvent(key = "#this.partitionKey()")
public record ProductChanged(StoreMerchantId store, ProductId product) implements CacheEvent {

    public ProductChanged {
        if (store == null || product == null) {
            throw new IllegalArgumentException("ProductChanged names a store and a product");
        }
    }

    @Override
    public Map<String, String> data() {
        return Map.of("store", store.getId(), "productId", product.cacheKeyPart());
    }
}

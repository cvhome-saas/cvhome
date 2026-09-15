package com.asrevo.cvhome.cache.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/** A sku's price changed: every cart line and product page that shows the price is stale. */
@OutboxEvent(key = "#this.partitionKey()")
public record PriceChanged(StoreMerchantId store, Sku sku) implements CacheEvent {

    public PriceChanged {
        if (store == null || sku == null) {
            throw new IllegalArgumentException("PriceChanged names a store and a sku");
        }
    }

    @Override
    public Map<String, String> data() {
        return Map.of("store", store.getId(), "sku", sku.cacheKeyPart());
    }
}

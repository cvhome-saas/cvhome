package com.asrevo.cvhome.cache.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/** A sku's quantity or availability changed, by a merchant's upsert or a reservation taken or released. */
@OutboxEvent(key = "#this.partitionKey()")
public record StockChanged(StoreMerchantId store, Sku sku) implements CacheEvent {

    public StockChanged {
        if (store == null || sku == null) {
            throw new IllegalArgumentException("StockChanged names a store and a sku");
        }
    }

    @Override
    public Map<String, String> data() {
        return Map.of("store", store.getId(), "sku", sku.cacheKeyPart());
    }
}

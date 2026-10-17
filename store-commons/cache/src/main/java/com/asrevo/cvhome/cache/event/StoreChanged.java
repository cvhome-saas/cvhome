package com.asrevo.cvhome.cache.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * The store record was saved: its name, languages, currency, domains or settings. Every service holds a copy of it
 * through the merchant client, so this is the event a consumer maps first.
 */
@OutboxEvent(key = "#this.partitionKey()")
public record StoreChanged(StoreMerchantId store) implements CacheEvent {

    public StoreChanged {
        if (store == null) {
            throw new IllegalArgumentException("StoreChanged names a store");
        }
    }

    @Override
    public Map<String, String> data() {
        return Map.of("store", store.getId());
    }
}

package com.asrevo.cvhome.cache.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManufacturerId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/** A brand or one of its descriptions was written: the brand lists and the products that show it are stale. */
@OutboxEvent(key = "#this.partitionKey()")
public record ManufacturerChanged(StoreMerchantId store, ManufacturerId manufacturer) implements CacheEvent {

    public ManufacturerChanged {
        if (store == null || manufacturer == null) {
            throw new IllegalArgumentException("ManufacturerChanged names a store and a manufacturer");
        }
    }

    @Override
    public Map<String, String> data() {
        return Map.of("store", store.getId(), "manufacturerId", manufacturer.cacheKeyPart());
    }
}

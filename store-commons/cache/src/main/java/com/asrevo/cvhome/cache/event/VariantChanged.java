package com.asrevo.cvhome.cache.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.VariantId;

import io.namastack.outbox.annotation.OutboxEvent;

/** A variant of a product was written; reserved for the reads that cache one variant rather than its product. */
@OutboxEvent(key = "#this.partitionKey()")
public record VariantChanged(StoreMerchantId store, VariantId variant) implements CacheEvent {

    public VariantChanged {
        if (store == null || variant == null) {
            throw new IllegalArgumentException("VariantChanged names a store and a variant");
        }
    }

    @Override
    public Map<String, String> data() {
        return Map.of("store", store.getId(), "variantId", variant.cacheKeyPart());
    }
}

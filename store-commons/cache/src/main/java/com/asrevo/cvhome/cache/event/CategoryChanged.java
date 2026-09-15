package com.asrevo.cvhome.cache.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.CategoryId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/** A category or one of its descriptions was written: the navigation and the listings under it are stale. */
@OutboxEvent(key = "#this.partitionKey()")
public record CategoryChanged(StoreMerchantId store, CategoryId category) implements CacheEvent {

    public CategoryChanged {
        if (store == null || category == null) {
            throw new IllegalArgumentException("CategoryChanged names a store and a category");
        }
    }

    @Override
    public Map<String, String> data() {
        return Map.of("store", store.getId(), "categoryId", category.cacheKeyPart());
    }
}

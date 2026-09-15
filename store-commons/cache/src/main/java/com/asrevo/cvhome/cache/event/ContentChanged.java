package com.asrevo.cvhome.cache.event;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.annotation.OutboxEvent;

/**
 * A page, post, menu, banner or policy of the store was written or moved between statuses. Content is read as a
 * whole (a layout, a menu, a sitemap), so the event names the store and the kind of content, not one item.
 */
@OutboxEvent(key = "#this.partitionKey()")
public record ContentChanged(StoreMerchantId store, String kind) implements CacheEvent {

    public ContentChanged {
        if (store == null || kind == null || kind.isBlank()) {
            throw new IllegalArgumentException("ContentChanged names a store and a kind of content");
        }
    }

    @Override
    public Map<String, String> data() {
        return Map.of("store", store.getId(), "kind", kind);
    }
}

package com.asrevo.cvhome.content.events;

import java.time.Instant;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.ContentStatus;
import com.asrevo.cvhome.content.model.ContentType;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.contentId().toString()")
public record ContentLifecycleEvent(
        StoreMerchantId store,
        ContentType type,
        Long contentId,
        long version,
        ContentStatus fromStatus,
        ContentStatus toStatus,
        String actor,
        Instant occurredAt
) {
}

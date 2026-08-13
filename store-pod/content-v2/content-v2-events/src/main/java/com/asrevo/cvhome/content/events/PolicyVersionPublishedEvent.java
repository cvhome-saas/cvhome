package com.asrevo.cvhome.content.events;

import java.time.Instant;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.content.model.policy.PolicyType;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.store().storeMerchantId() + ':' + #this.policyType().name()")
public record PolicyVersionPublishedEvent(StoreMerchantId store, PolicyType policyType, Long contentId,
                                          String policyVersion, String actor, Instant occurredAt) {
}

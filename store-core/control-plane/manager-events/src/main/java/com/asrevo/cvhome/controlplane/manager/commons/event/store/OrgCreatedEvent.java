package com.asrevo.cvhome.controlplane.manager.commons.event.store;

import java.util.Map;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

import io.namastack.outbox.annotation.OutboxEvent;

@OutboxEvent(key = "#this.org().id().toString()")
public record OrgCreatedEvent(ManagerOrgId org, Map<String, String> data) implements OrgEvent {
    public static OrgCreatedEvent from(ManagerOrgId org) {
        return new OrgCreatedEvent(org, Map.of());
    }

    @Override
    public String eventType() {
        return OrgCreatedEvent.class.getSimpleName();
    }
}

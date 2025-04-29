package com.asrevo.cvhome.manager.commons.event.store;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import java.util.Map;

public record OrgCreatedEvent(ManagerOrgId org, Map<String, String> data) implements OrgEvent {
    public static OrgCreatedEvent from(ManagerOrgId org) {
        return new OrgCreatedEvent(org, Map.of());
    }

    @Override
    public String eventType() {
        return OrgCreatedEvent.class.getSimpleName();
    }
}

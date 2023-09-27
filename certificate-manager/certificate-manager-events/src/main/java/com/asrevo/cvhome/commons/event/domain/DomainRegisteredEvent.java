package com.asrevo.cvhome.commons.event.domain;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainId;
import com.asrevo.cvhome.commons.event.EventId;

import java.util.List;
import java.util.Map;

public record DomainRegisteredEvent(EventId eventId, DomainId domainId, Domain domain,
                                    Map<String, String> data) implements DomainEvent {
    private static final String AUTO_RENEW_KEY = "autoRenew";
    private static final String AUTO_ORDER_KEY = "autoOrder";

    public static DomainRegisteredEvent from(DomainId domainId, Domain domain, boolean autoRenew, boolean autoOrder) {
        return new DomainRegisteredEvent(EventId.newId(), domainId, domain, Map.of(AUTO_RENEW_KEY, String.valueOf(autoRenew), AUTO_ORDER_KEY, String.valueOf(autoOrder)));
    }

    public boolean autoRenew() {
        return Boolean.getBoolean(this.data.get(AUTO_RENEW_KEY));
    }

    public boolean autoOrder() {
        return Boolean.getBoolean(this.data.get(AUTO_ORDER_KEY));
    }

    @Override
    public String eventType() {
        return "DomainRegisteredEvent";
    }

    @Override
    public List<String> getDestinations() {
        return List.of();
    }
}

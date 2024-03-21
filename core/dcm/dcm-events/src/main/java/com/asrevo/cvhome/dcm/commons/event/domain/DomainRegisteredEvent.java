package com.asrevo.cvhome.dcm.commons.event.domain;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainId;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.event.EventId;

import java.util.Map;

public record DomainRegisteredEvent(EventId eventId, DomainId domainId, Domain domain,
                                    Map<String, String> data) implements DomainEvent {
    private static final String IDENTITY_ID_KEY = "identityId";
    private static final String AUTO_RENEW_KEY = "autoRenew";
    private static final String AUTO_ORDER_KEY = "autoOrder";

    public static DomainRegisteredEvent from(DomainId domainId, IdentityId identityId, Domain domain, boolean autoRenew, boolean autoOrder) {
        return new DomainRegisteredEvent(EventId.newId(), domainId, domain, Map.of(IDENTITY_ID_KEY, identityId.id(), AUTO_RENEW_KEY, String.valueOf(autoRenew), AUTO_ORDER_KEY, String.valueOf(autoOrder)));
    }

    public boolean autoRenew() {
        return Boolean.getBoolean(this.data.get(AUTO_RENEW_KEY));
    }

    public IdentityId identityId() {
        return new IdentityId(this.data.get(IDENTITY_ID_KEY));
    }

    public boolean autoOrder() {
        return Boolean.getBoolean(this.data.get(AUTO_ORDER_KEY));
    }

    @Override
    public String eventType() {
        return "DomainRegisteredEvent";
    }
}

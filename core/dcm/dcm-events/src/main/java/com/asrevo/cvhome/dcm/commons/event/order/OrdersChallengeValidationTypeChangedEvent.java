package com.asrevo.cvhome.dcm.commons.event.order;

import com.asrevo.cvhome.commons.event.EventId;
import com.asrevo.cvhome.dcm.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.dcm.commons.domain.OrdersId;
import com.asrevo.cvhome.commons.domain.Domain;

import java.util.Map;

public record OrdersChallengeValidationTypeChangedEvent(EventId eventId, OrdersId ordersId, String eventType,
                                                        Map<String, String> data) implements OrdersEvent {
    private final static String DOMAIN_KEY = "domain";
    private final static String OLD_TYPE_KEY = "oldType";
    private final static String NEW_TYPE_KEY = "newType";


    public static OrdersChallengeValidationTypeChangedEvent from(OrdersId ordersId, Domain domain, ChallengeValidationType oldType, ChallengeValidationType newType) {
        return new OrdersChallengeValidationTypeChangedEvent(EventId.newId(), ordersId, "OrderChallengeValidationTypeChangedEvent", Map.of(DOMAIN_KEY, domain.domain(), OLD_TYPE_KEY, oldType.name(), NEW_TYPE_KEY, newType.name()));
    }

    public Domain domain() {
        return new Domain(this.data.get(DOMAIN_KEY));
    }

    public ChallengeValidationType oldType() {
        return ChallengeValidationType.valueOf(this.data.get(OLD_TYPE_KEY));

    }

    public ChallengeValidationType newType() {
        return ChallengeValidationType.valueOf(this.data.get(NEW_TYPE_KEY));
    }

    @Override
    public String eventType() {
        return "OrderChallengeValidationTypeChangedEvent";
    }
}

package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrdersChallengeValidationTypeChangedEvent extends OrdersEvent {
    private Domain domain;
    private ChallengeValidationType oldType;
    private ChallengeValidationType newType;

    public static OrdersChallengeValidationTypeChangedEvent from(OrdersId id, Domain domain, ChallengeValidationType oldType, ChallengeValidationType newType) {
        OrdersChallengeValidationTypeChangedEvent event = new OrdersChallengeValidationTypeChangedEvent();
        event.setId(id);
        event.setDomain(domain);
        event.setOldType(oldType);
        event.setNewType(newType);
        return event;
    }

    @Override
    public String eventType() {
        return "OrderChallengeValidationTypeChangedEvent";
    }
}

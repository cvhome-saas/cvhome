package com.asrevo.cvhome.commons.event.order;

import com.asrevo.cvhome.commons.domain.ChallengeValidationType;
import com.asrevo.cvhome.commons.domain.Domain;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderChallengeValidationTypeChangedEvent extends OrderEvent {
    private Domain domain;
    private ChallengeValidationType oldType;
    private ChallengeValidationType newType;

    public static OrderChallengeValidationTypeChangedEvent from(Domain domain, ChallengeValidationType oldType, ChallengeValidationType newType) {
        OrderChallengeValidationTypeChangedEvent event = new OrderChallengeValidationTypeChangedEvent();
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

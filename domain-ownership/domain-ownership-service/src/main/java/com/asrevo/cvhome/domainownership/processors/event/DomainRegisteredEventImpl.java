package com.asrevo.cvhome.domainownership.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.domainownership.commons.event.domain.DomainRegisteredEvent;
import com.asrevo.cvhome.domainownership.service.OwnerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DomainRegisteredEventImpl implements EventImpl<DomainRegisteredEvent> {
    private OwnerService ownerService;

    @Override
    public void process(DomainRegisteredEvent event) {
        ownerService.addDomain(event.identityId(), event.domainId());
    }

    @Override
    public String type() {
        return DomainRegisteredEvent.class.getSimpleName();
    }
}

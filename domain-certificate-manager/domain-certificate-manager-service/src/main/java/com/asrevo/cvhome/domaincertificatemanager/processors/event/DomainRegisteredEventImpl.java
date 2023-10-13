package com.asrevo.cvhome.domaincertificatemanager.processors.event;

import com.asrevo.cvhome.domaincertificatemanager.commons.event.domain.DomainRegisteredEvent;
import com.asrevo.cvhome.domaincertificatemanager.service.OwnerService;
import com.asrevo.cvhome.commons.event.EventImpl;
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
        return DomainRegisteredEvent.class.getName();
    }
}

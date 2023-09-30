package com.asrevo.cvhome.domainownership.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.domainownership.commons.event.domain.DomainReferenceChangedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class DomainReferenceChangedEventImpl implements EventImpl<DomainReferenceChangedEvent> {

    @Override
    public void process(DomainReferenceChangedEvent order) {
        log.info("domain ref changed from {} to {}", order.oldReference(), order.newReference());
    }

    @Override
    public String type() {
        return DomainReferenceChangedEvent.class.getName();
    }
}

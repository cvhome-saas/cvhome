package com.asrevo.cvhome.dcm.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.dcm.commons.event.order.OrdersCertificateGeneratedEvent;
import com.asrevo.cvhome.dcm.service.DomainService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class OrdersCertificateGeneratedEventImpl implements EventImpl<OrdersCertificateGeneratedEvent> {
    private DomainService domainService;

    @Override
    public void process(OrdersCertificateGeneratedEvent event) {
        domainService.updateDomainStatus(event.domain(), event.certificateOrderStatus(), event.generatedDate());
    }

    @Override
    public String type() {
        return OrdersCertificateGeneratedEvent.class.getName();
    }
}

package com.asrevo.cvhome.domaincertificatemanager.processors.event;

import com.asrevo.cvhome.domaincertificatemanager.commons.event.order.OrdersValidatedEvent;
import com.asrevo.cvhome.domaincertificatemanager.service.DomainService;
import com.asrevo.cvhome.commons.event.EventImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class OrdersValidatedEventImpl implements EventImpl<OrdersValidatedEvent> {
    private DomainService domainService;

    @Override
    public void process(OrdersValidatedEvent event) {
        domainService.updateDomainStatus(event.domain(), event.certificateOrderStatus(), event.validatedDate());
    }

    @Override
    public String type() {
        return OrdersValidatedEvent.class.getName();
    }
}

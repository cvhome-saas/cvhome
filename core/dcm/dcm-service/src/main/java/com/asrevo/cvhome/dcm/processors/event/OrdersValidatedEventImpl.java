package com.asrevo.cvhome.dcm.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.dcm.commons.event.order.OrdersValidatedEvent;
import com.asrevo.cvhome.dcm.service.DomainService;
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

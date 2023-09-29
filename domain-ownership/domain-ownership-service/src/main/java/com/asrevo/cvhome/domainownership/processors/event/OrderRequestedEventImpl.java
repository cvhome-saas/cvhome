package com.asrevo.cvhome.domainownership.processors.event;

import com.asrevo.cvhome.certificatemanager.commons.event.order.OrdersRequestedEvent;
import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.domainownership.service.DomainService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrderRequestedEventImpl implements EventImpl<OrdersRequestedEvent> {
    private final DomainService domainService;

    @Override
    public void process(OrdersRequestedEvent order) {
        domainService.updateDomainStatus(order.domain());
    }

    @Override
    public String type() {
        return OrdersRequestedEvent.class.getName();
    }
}

package com.asrevo.cvhome.certificatemanager.processors.event;

import com.asrevo.cvhome.certificatemanager.commons.event.order.OrdersRequestedEvent;
import com.asrevo.cvhome.certificatemanager.service.DomainService;
import com.asrevo.cvhome.commons.event.EventImpl;
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

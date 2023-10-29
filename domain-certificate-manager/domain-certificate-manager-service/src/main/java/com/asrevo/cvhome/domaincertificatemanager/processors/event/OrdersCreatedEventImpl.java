package com.asrevo.cvhome.domaincertificatemanager.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.domaincertificatemanager.commons.event.order.OrdersCreatedEvent;
import org.springframework.stereotype.Service;

@Service
public class OrdersCreatedEventImpl implements EventImpl<OrdersCreatedEvent> {
    @Override
    public void process(OrdersCreatedEvent event) {

    }

    @Override
    public String type() {
        return OrdersCreatedEvent.class.getName();
    }
}

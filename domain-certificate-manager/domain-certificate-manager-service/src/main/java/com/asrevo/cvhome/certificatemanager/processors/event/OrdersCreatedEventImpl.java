package com.asrevo.cvhome.certificatemanager.processors.event;

import com.asrevo.cvhome.certificatemanager.commons.event.order.OrdersCreatedEvent;
import com.asrevo.cvhome.commons.event.EventImpl;
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

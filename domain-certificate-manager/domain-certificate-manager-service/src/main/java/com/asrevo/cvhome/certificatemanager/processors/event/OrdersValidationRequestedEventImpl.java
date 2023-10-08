package com.asrevo.cvhome.certificatemanager.processors.event;

import com.asrevo.cvhome.certificatemanager.commons.event.order.OrdersValidationRequestedEvent;
import com.asrevo.cvhome.commons.event.EventImpl;
import org.springframework.stereotype.Service;

@Service
public class OrdersValidationRequestedEventImpl implements EventImpl<OrdersValidationRequestedEvent> {
    @Override
    public void process(OrdersValidationRequestedEvent event) {

    }

    @Override
    public String type() {
        return OrdersValidationRequestedEvent.class.getName();
    }
}

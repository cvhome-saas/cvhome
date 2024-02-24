package com.asrevo.cvhome.domaincertificatemanager.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.domaincertificatemanager.commons.event.order.OrdersValidationRequestedEvent;
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

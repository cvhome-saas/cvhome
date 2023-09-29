package com.asrevo.cvhome.domainownership.processors.event;

import com.asrevo.cvhome.certificatemanager.commons.event.order.OrdersCertificateGeneratedEvent;
import com.asrevo.cvhome.commons.event.EventImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class OrdersCertificateGeneratedEventImpl implements EventImpl<OrdersCertificateGeneratedEvent> {
    @Override
    public void process(OrdersCertificateGeneratedEvent event) {

    }

    @Override
    public String type() {
        return OrdersCertificateGeneratedEvent.class.getSimpleName();
    }
}

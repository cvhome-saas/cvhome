package com.asrevo.cvhome.certificatemanager.processors.event;

import com.asrevo.cvhome.certificatemanager.commons.event.order.OrdersRequestedEvent;
import com.asrevo.cvhome.certificatemanager.service.AcmCertificateOrderService;
import com.asrevo.cvhome.commons.event.EventImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrderRequestedEventImpl implements EventImpl<OrdersRequestedEvent> {
    private final AcmCertificateOrderService acmCertificateOrderService;

    @Override
    public void process(OrdersRequestedEvent order) {
        acmCertificateOrderService.updateDomainStatus(order.domain());
    }

    @Override
    public String type() {
        return OrdersRequestedEvent.class.getName();
    }
}

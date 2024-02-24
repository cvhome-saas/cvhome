package com.asrevo.cvhome.domaincertificatemanager.processors.event;

import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.domaincertificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.domaincertificatemanager.commons.event.order.OrdersRequestedEvent;
import com.asrevo.cvhome.domaincertificatemanager.service.DomainService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrdersRequestedEventImpl implements EventImpl<OrdersRequestedEvent> {
    private final DomainService domainService;

    @Override
    public void process(OrdersRequestedEvent order) {
        domainService.updateDomainStatus(order.domain(), CertificateOrderStatus.REQUESTED, order.requestedDate());
    }

    @Override
    public String type() {
        return OrdersRequestedEvent.class.getName();
    }
}

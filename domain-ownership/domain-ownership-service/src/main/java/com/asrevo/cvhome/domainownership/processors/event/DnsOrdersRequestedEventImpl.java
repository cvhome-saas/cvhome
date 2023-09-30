package com.asrevo.cvhome.domainownership.processors.event;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.certificatemanager.commons.event.order.DnsOrdersRequestedEvent;
import com.asrevo.cvhome.commons.event.EventImpl;
import com.asrevo.cvhome.domainownership.service.DomainService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class DnsOrdersRequestedEventImpl implements EventImpl<DnsOrdersRequestedEvent> {
    private final DomainService domainService;

    @Override
    public void process(DnsOrdersRequestedEvent order) {
        Map.Entry<String, String> dns01Challenge = order.dns01Challenge();
        log.info("dns request created with " + dns01Challenge.getKey() + "                 " + dns01Challenge.getValue());
        domainService.updateDomainStatus(order.domain(), CertificateOrderStatus.REQUESTED, order.requestedDate());
    }

    @Override
    public String type() {
        return DnsOrdersRequestedEvent.class.getName();
    }
}

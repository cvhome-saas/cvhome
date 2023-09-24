package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.event.order.OrdersCreatedEvent;
import com.asrevo.cvhome.commons.event.order.OrdersRequestedEvent;
import com.asrevo.cvhome.commons.event.order.OrdersValidatedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@AllArgsConstructor
public class OrderEventHandler {
    private final AcmCertificateOrderService acmCertificateOrderService;

    public void handle(Event event) throws AcmeException, IOException {

        if (event instanceof OrdersCreatedEvent order) {
            acmCertificateOrderService.order(order.getId());
        }

        if (event instanceof OrdersRequestedEvent order) {
//            @TODO should notify order types creator that is not support dynamic
//            @TODO should notify other microservices to do reset cached files
            acmCertificateOrderService.notifyDomainThatOrderRequested(order.getDomain());
            acmCertificateOrderService.preValidation(order.getId());
        }

        if (event instanceof OrdersValidatedEvent order) {
            acmCertificateOrderService.doGeneration(order.getId());
        }

    }

}

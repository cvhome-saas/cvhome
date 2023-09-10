package com.asrevo.cvhome.certificatemanager.service;

import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.commons.utils.Defines;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.acme4j.exception.AcmeException;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.asrevo.cvhome.commons.utils.Defines.OrderEvents.DOMAIN_CERTIFICATE_ORDER_STATUS_CHANGED_EVENT;

@Component
@Slf4j
@AllArgsConstructor
public class DomainEventHandler {
    private final AcmCertificateOrderService acmCertificateOrderService;

    public void handle(Event event) throws AcmeException, IOException {
        Long id = extractId(event);
        if (DOMAIN_CERTIFICATE_ORDER_STATUS_CHANGED_EVENT.equals(event.eventType())) {
            CertificateOrderStatus certificateOrderStatus = extractStatus(event);
            if (CertificateOrderStatus.INITIATED.equals(certificateOrderStatus)) {
                acmCertificateOrderService.order(id);
            }
            if (CertificateOrderStatus.REQUESTED.equals(certificateOrderStatus)) {
//                @TODO should notify order types creator that is not support dynamic
//                @TODO should notify other microservices to do reset cached files
                acmCertificateOrderService.preValidation(id);
            }
            if (CertificateOrderStatus.VALIDATION_REQUESTED.equals(certificateOrderStatus)) {
                acmCertificateOrderService.doValidation(id);
            }
            if (CertificateOrderStatus.VALIDATED_VALID.equals(certificateOrderStatus)) {
                acmCertificateOrderService.doGeneration(id);
            }
        }
    }

    Long extractId(Event event) {
        if (event.data() != null) {
            String idStr = event.data().get(Defines.OrderEvents.DOMAIN_CERTIFICATE_ORDER_ID_KEY);
            try {
                return Long.parseLong(idStr);
            } catch (Exception e) {
                log.warn("invalid id from event " + event.data());
            }
        }
        return null;
    }

    CertificateOrderStatus extractStatus(Event event) {
        if (event.data() != null) {
            String statusStr = event.data().get(Defines.OrderEvents.DOMAIN_CERTIFICATE_ORDER_STATUS_CHANGED_KEY);
            try {
                return CertificateOrderStatus.valueOf(statusStr);
            } catch (Exception e) {
                log.warn("invalid statusStr from event " + event.data());
            }
        }
        return null;
    }
}

package com.asrevo.cvhome.certificatemanager.jobs;

import com.asrevo.cvhome.certificatemanager.domain.DomainCertificateOrder;
import com.asrevo.cvhome.certificatemanager.service.DomainCertificateOrderService;
import com.asrevo.cvhome.commons.domain.CertificateOrderStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static com.asrevo.cvhome.commons.domain.CertificateOrderStatus.INITIATED;
import static com.asrevo.cvhome.commons.domain.CertificateOrderStatus.PRE_VALIDATED_INVALID;

@Component
@AllArgsConstructor
@Slf4j
public class AcmJobs {
    private final DomainCertificateOrderService domainCertificateOrderService;
    private final int limit = 100;


    public void enqueuePreValidationInvalid() {
        Set<CertificateOrderStatus> statuses = Set.of(PRE_VALIDATED_INVALID);
        Instant from = Instant.now().minus(5, ChronoUnit.MINUTES);
        List<DomainCertificateOrder> orders = domainCertificateOrderService.findAllSinceValidation(statuses, from, limit);
        log.info("will enqueue {} orders in enqueuePreValidationInvalid", orders.size());
        orders.forEach(order -> {
            log.info("will enqueue order {}", order.getId());
            order.setCertificateOrderStatus(CertificateOrderStatus.VALIDATION_REQUESTED);
            domainCertificateOrderService.save(order);
        });
    }

    public void enqueueFailedOrder() {
        Set<CertificateOrderStatus> statuses = Set.of(INITIATED);
        Instant from = Instant.now().minus(5, ChronoUnit.MINUTES);
        List<DomainCertificateOrder> orders = domainCertificateOrderService.findAllSinceCreation(statuses, from, limit);
        log.info("will enqueue {} orders in enqueueFailedOrder", orders.size());
        orders.forEach(order -> {
            log.info("will enqueue order {}", order.getId());
            order.setCertificateOrderStatus(INITIATED);
            domainCertificateOrderService.save(order);
        });
    }
}

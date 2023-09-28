package com.asrevo.cvhome.certificatemanager.jobs;

import com.asrevo.cvhome.certificatemanager.commons.domain.CertificateOrderStatus;
import com.asrevo.cvhome.certificatemanager.entity.OrdersEntity;
import com.asrevo.cvhome.certificatemanager.service.OrdersService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static com.asrevo.cvhome.certificatemanager.commons.domain.CertificateOrderStatus.INITIATED;
import static com.asrevo.cvhome.certificatemanager.commons.domain.CertificateOrderStatus.PRE_VALIDATED_INVALID;

@Component
@AllArgsConstructor
@Slf4j
public class AcmJobs {
    private final OrdersService ordersService;
    private final int limit = 100;


    public void enqueuePreValidationInvalid() {
        Set<CertificateOrderStatus> statuses = Set.of(PRE_VALIDATED_INVALID);
        Instant from = Instant.now().minus(5, ChronoUnit.MINUTES);
        List<OrdersEntity> orders = ordersService.findAllSinceValidation(statuses, from, limit);
        log.info("will enqueue {} orders in enqueuePreValidationInvalid", orders.size());
        orders.forEach(order -> {
            log.info("will enqueue order {}", order.getId());
            order.setCertificateOrderStatus(CertificateOrderStatus.VALIDATION_REQUESTED);
            ordersService.save(order);
        });
    }

    public void enqueueFailedOrder() {
        Set<CertificateOrderStatus> statuses = Set.of(INITIATED);
        Instant from = Instant.now().minus(5, ChronoUnit.MINUTES);
        List<OrdersEntity> orders = ordersService.findAllSinceCreation(statuses, from, limit);
        log.info("will enqueue {} orders in enqueueFailedOrder", orders.size());
        orders.forEach(order -> {
            log.info("will enqueue order {}", order.getId());
            order.setCertificateOrderStatus(INITIATED);
            ordersService.save(order);
        });
    }
}

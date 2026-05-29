package com.asrevo.cvhome.catalog.services.product;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservation;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservationStatusEnum;
import com.asrevo.cvhome.catalog.repositories.product.availability.ProductReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ProductUnReservationJob {

    private final ProductReservationRepository reservationRepository;

    @Scheduled(fixedDelayString = "${reservation.cleanup.interval:60000}")
    @Transactional
    public void unReserveExpiredItems() {
        log.info("Checking for expired reservations...");

        List<ProductReservation> expired = reservationRepository
                .findAllByStatusAndExpireAtBefore(ProductReservationStatusEnum.RESERVED, Instant.now());

        for (ProductReservation res : expired) {
            log.info("Expiring reservation ID: {} for SKU: {}", res.getId(), res.getSku());

            // Restore quantity
            var availability = res.getProductAvailability();
            availability.setProductQuantity(availability.getProductQuantity() + res.getQuantity());

            // Update status to EXPIRED
            res.setStatus(ProductReservationStatusEnum.EXPIRED);
            reservationRepository.save(res);
        }
    }
}

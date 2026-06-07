package com.asrevo.cvhome.catalog.services.product.availability;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservation;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservationStatus;
import com.asrevo.cvhome.catalog.repositories.product.availability.ProductReservationRepository;
import com.asrevo.cvhome.catalog.services.product.ProductReservationService;
import com.asrevo.cvhome.store.core.exception.ServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReservationCleanupService {

    private final ProductReservationRepository productReservationRepository;
    private final ProductReservationService productReservationService;

    @Scheduled(fixedRateString = "${reservation.cleanup.interval:60000}")
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Starting cleanup of expired product reservations.");
        Instant now = Instant.now();
        List<ProductReservation> expiredReservations = productReservationRepository.findExpiredReservations(
                ProductReservationStatus.TEMPORARY_RESERVED, now);

        if (expiredReservations.isEmpty()) {
            log.info("No expired product reservations found.");
            return;
        }

        log.info("Found {} expired product reservations.", expiredReservations.size());

        for (ProductReservation reservation : expiredReservations) {
            try {
                productReservationService.expire(reservation.getStoreMerchantId(), reservation.getOrderId());
            } catch (ServiceException e) {
                log.error("Error while expiring reservation: {}", e.getMessage());
            }
        }
        log.info("Finished cleanup of expired product reservations.");
    }
}

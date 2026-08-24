package com.asrevo.cvhome.inventory.services.reservation;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.inventory.entity.ProductReservation;
import com.asrevo.cvhome.inventory.entity.ProductReservationStatus;
import com.asrevo.cvhome.inventory.repositories.ProductReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductReservationCleanupService {

    private final ProductReservationRepository productReservationRepository;
    private final ProductReservationService productReservationService;
    private final ExternalOrderService externalOrderService;

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
            var result = productReservationService.release(reservation.getStoreMerchantId(), reservation.getRef());
            if (result.status()) {
                log.info("Successfully expired reservation for ref {}. Notifying checkout service.",
                        reservation.getRef());
                try {
                    externalOrderService.handleReservationExpired(reservation.getStoreMerchantId(),
                            reservation.getRef());
                } catch (Exception e) {
                    log.error("Failed to notify checkout service about expired reservation for ref {}",
                            reservation.getRef(), e);
                }
            } else {
                log.error("Error while expiring reservation for ref {}", reservation.getRef());
            }
        }
        log.info("Finished cleanup of expired product reservations.");
    }
}

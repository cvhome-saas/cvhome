package com.asrevo.cvhome.inventory.services;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.api.errors.CheckoutApiUnavailableException;
import com.asrevo.cvhome.checkout.model.signal.ReservationExpiredSignal;
import com.asrevo.cvhome.checkout.services.order.ExternalOrderSignalService;
import com.asrevo.cvhome.inventory.entity.ProductReservation;
import com.asrevo.cvhome.inventory.entity.ProductReservationStatus;
import com.asrevo.cvhome.inventory.repositories.ProductReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Gives back the stock of reservations nobody committed in time, and tells checkout so it can fail the order.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationExpiryJob {

    private final ProductReservationRepository reservationRepository;

    private final ReservationService reservationService;

    private final ExternalOrderSignalService orderSignals;

    @Scheduled(fixedRateString = "${reservation.cleanup.interval:60000}")
    @Transactional
    public void releaseExpired() {
        List<ProductReservation> expired = reservationRepository.findByStatusAndExpireAtBeforeOrderById(
                ProductReservationStatus.TEMPORARY_RESERVED, Instant.now());
        for (ProductReservation reservation : expired) {
            reservationService.release(reservation.getStoreMerchantId(), reservation.getRef());
            log.info("Released expired reservation {}", reservation.getRef());
            try {
                orderSignals.signalReservationExpired(reservation.getStoreMerchantId(), reservation.getRef(),
                        new ReservationExpiredSignal(reservation.getRef()));
            } catch (RuntimeException | CheckoutApiUnavailableException e) {
                // The stock is back either way; checkout's own expiry handling catches up on the next pass.
                log.error("Could not notify checkout of expired reservation {}", reservation.getRef(), e);
            }
        }
    }
}

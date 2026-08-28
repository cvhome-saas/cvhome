package com.asrevo.cvhome.inventory.services;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductReservation;
import com.asrevo.cvhome.inventory.entity.ProductReservationStatus;
import com.asrevo.cvhome.inventory.repositories.ProductReservationRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The expiry pass gives stock back first and tells checkout second; a checkout that cannot be reached must not stop
 * the remaining reservations from being released.
 */
@ExtendWith(MockitoExtension.class)
class ReservationExpiryJobTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String REF_1 = "order-1";

    private static final String REF_2 = "order-2";

    @Mock
    private ProductReservationRepository reservationRepository;

    @Mock
    private ReservationService reservationService;

    @Mock
    private ExternalOrderService externalOrderService;

    @InjectMocks
    private ReservationExpiryJob job;

    @Test
    void nothingExpiredMeansNothingReleased() {
        when(reservationRepository.findByStatusAndExpireAtBeforeOrderById(
                eq(ProductReservationStatus.TEMPORARY_RESERVED), any(Instant.class))).thenReturn(List.of());

        job.releaseExpired();

        verify(reservationService, never()).release(any(), any());
        verify(externalOrderService, never()).handleReservationExpired(any(), any());
    }

    @Test
    void releasesEveryExpiredReservationEvenWhenCheckoutCannotBeToldAboutOne() {
        when(reservationRepository.findByStatusAndExpireAtBeforeOrderById(
                eq(ProductReservationStatus.TEMPORARY_RESERVED), any(Instant.class)))
                .thenReturn(List.of(new ProductReservation(STORE, REF_1), new ProductReservation(STORE, REF_2)));
        doThrow(new IllegalStateException("checkout down"))
                .when(externalOrderService).handleReservationExpired(STORE, REF_1);

        job.releaseExpired();

        verify(reservationService).release(STORE, REF_1);
        verify(reservationService).release(STORE, REF_2);
        verify(externalOrderService).handleReservationExpired(STORE, REF_2);
    }
}

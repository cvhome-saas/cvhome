package com.asrevo.cvhome.inventory.services;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.Inventory;
import com.asrevo.cvhome.inventory.entity.ProductReservation;
import com.asrevo.cvhome.inventory.entity.ProductReservationStatus;
import com.asrevo.cvhome.inventory.errors.EmptyReservationException;
import com.asrevo.cvhome.inventory.errors.InsufficientInventoryException;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReleaseResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.inventory.repositories.InventoryRepository;
import com.asrevo.cvhome.inventory.repositories.ProductReservationRepository;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The reservation state machine: stock leaves on reserve, comes back on release, and a retry of the same ref never
 * takes the stock twice.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    private static final String REF = "order-1";

    private static final String SKU_A = "SKU-A";

    private static final String SKU_B = "SKU-B";

    private static final int EXPIRY_MINUTES = 30;

    @Mock
    private ProductReservationRepository reservationRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    private ReservationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReservationServiceImpl(reservationRepository, inventoryRepository, EXPIRY_MINUTES);
    }

    private static Inventory stocked(String sku, int quantity) {
        Inventory inventory = new Inventory(STORE, sku);
        inventory.setId(1L);
        inventory.setQuantity(quantity);
        return inventory;
    }

    private static ProductReservationList entries(ReserveProductEntry... entries) {
        return new ProductReservationList(Set.of(entries));
    }

    private static ProductReservation held(ProductReservationStatus status, Instant expireAt) {
        ProductReservation reservation = new ProductReservation(STORE, REF);
        reservation.setId(7L);
        reservation.setStatus(status);
        reservation.setExpireAt(expireAt);
        return reservation;
    }

    @Nested
    class Reserve {

        @Test
        void emptyReservationIsRejectedBeforeAnyStockIsTouched() {
            assertThatThrownBy(() -> service.reserve(STORE, REF, new ProductReservationList(Set.of())))
                    .isInstanceOf(EmptyReservationException.class);
            assertThatThrownBy(() -> service.reserve(STORE, REF, new ProductReservationList(null)))
                    .isInstanceOf(EmptyReservationException.class);
            verify(inventoryRepository, never()).lockBySku(any(), any());
        }

        @Test
        void takesStockAndHoldsItUntilExpiry() throws Exception {
            Inventory a = stocked(SKU_A, 10);
            Inventory b = stocked(SKU_B, 3);
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.empty());
            when(inventoryRepository.lockBySku(STORE, SKU_A)).thenReturn(Optional.of(a));
            when(inventoryRepository.lockBySku(STORE, SKU_B)).thenReturn(Optional.of(b));
            when(reservationRepository.save(any())).thenAnswer(invocation -> {
                ProductReservation saved = invocation.getArgument(0);
                saved.setId(42L);
                return saved;
            });
            Instant before = Instant.now();

            ProductReservationReserveResult result = service.reserve(STORE, REF,
                    entries(new ReserveProductEntry(SKU_A, 4), new ReserveProductEntry(SKU_B, 3)));

            assertThat(result.status()).isTrue();
            assertThat(result.reservationId()).isEqualTo(42L);
            assertThat(result.expireAt()).isAfterOrEqualTo(before.plus(Duration.ofMinutes(EXPIRY_MINUTES)));
            assertThat(a.getQuantity()).isEqualTo(6);
            assertThat(b.getQuantity()).isZero();
            ArgumentCaptor<ProductReservation> captor = ArgumentCaptor.forClass(ProductReservation.class);
            verify(reservationRepository).save(captor.capture());
            ProductReservation saved = captor.getValue();
            assertThat(saved.getStatus()).isEqualTo(ProductReservationStatus.TEMPORARY_RESERVED);
            assertThat(saved.getLines()).hasSize(2);
            assertThat(saved.holds(SKU_A)).isTrue();
            assertThat(saved.holds(SKU_B)).isTrue();
        }

        @Test
        void retryOfTheSameRefDoesNotTakeTheStockTwice() throws Exception {
            Inventory a = stocked(SKU_A, 10);
            ProductReservation existing = held(ProductReservationStatus.TEMPORARY_RESERVED, Instant.now());
            existing.addLine(a, 4);
            a.setQuantity(6);
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.of(existing));
            when(reservationRepository.save(existing)).thenReturn(existing);

            ProductReservationReserveResult result = service.reserve(STORE, REF,
                    entries(new ReserveProductEntry(SKU_A, 4)));

            assertThat(result.status()).isTrue();
            assertThat(result.reservationId()).isEqualTo(7L);
            assertThat(a.getQuantity()).isEqualTo(6);
            assertThat(existing.getLines()).hasSize(1);
            verify(inventoryRepository, never()).lockBySku(any(), any());
        }

        @Test
        void retryAddsOnlyTheSkusNotAlreadyHeld() throws Exception {
            Inventory a = stocked(SKU_A, 6);
            Inventory b = stocked(SKU_B, 3);
            ProductReservation existing = held(ProductReservationStatus.TEMPORARY_RESERVED, Instant.now());
            existing.addLine(a, 4);
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.of(existing));
            when(inventoryRepository.lockBySku(STORE, SKU_B)).thenReturn(Optional.of(b));
            when(reservationRepository.save(existing)).thenReturn(existing);

            service.reserve(STORE, REF, entries(new ReserveProductEntry(SKU_A, 4), new ReserveProductEntry(SKU_B, 1)));

            assertThat(a.getQuantity()).isEqualTo(6);
            assertThat(b.getQuantity()).isEqualTo(2);
            assertThat(existing.getLines()).hasSize(2);
        }

        @Test
        void skuWithNoInventoryRowIsReportedAsNotStocked() {
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.empty());
            when(inventoryRepository.lockBySku(STORE, SKU_A)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.reserve(STORE, REF, entries(new ReserveProductEntry(SKU_A, 2))))
                    .isInstanceOf(InsufficientInventoryException.class)
                    .satisfies(e -> {
                        InsufficientInventoryException failure = (InsufficientInventoryException) e;
                        assertThat(failure.payload().params()).containsEntry("sku", SKU_A)
                                .containsEntry("requested", 2).containsEntry("available", 0);
                    });
            verify(reservationRepository, never()).save(any());
        }

        @Test
        void shortStockIsRefusedAndNothingIsSaved() {
            Inventory a = stocked(SKU_A, 1);
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.empty());
            when(inventoryRepository.lockBySku(STORE, SKU_A)).thenReturn(Optional.of(a));

            assertThatThrownBy(() -> service.reserve(STORE, REF, entries(new ReserveProductEntry(SKU_A, 2))))
                    .isInstanceOf(InsufficientInventoryException.class)
                    .hasMessageContaining(SKU_A);
            assertThat(a.getQuantity()).isEqualTo(1);
            verify(reservationRepository, never()).save(any());
        }
    }

    @Nested
    class Commit {

        @Test
        void unknownRefIsNotCommitted() {
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.empty());

            ProductReservationCommitResult result = service.commit(STORE, REF);

            assertThat(result.status()).isFalse();
            assertThat(result.reservationId()).isNull();
            assertThat(result.expireAt()).isNull();
        }

        @Test
        void heldReservationBecomesCompleted() {
            ProductReservation reservation = held(ProductReservationStatus.TEMPORARY_RESERVED,
                    Instant.now().plus(Duration.ofMinutes(5)));
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.of(reservation));

            ProductReservationCommitResult result = service.commit(STORE, REF);

            assertThat(result.status()).isTrue();
            assertThat(result.reservationId()).isEqualTo(7L);
            assertThat(result.expireAt()).isEqualTo(reservation.getExpireAt());
            assertThat(reservation.getStatus()).isEqualTo(ProductReservationStatus.COMPLETED);
        }

        @Test
        void expiredReservationIsNotCommitted() {
            ProductReservation reservation = held(ProductReservationStatus.TEMPORARY_RESERVED,
                    Instant.now().minus(Duration.ofMinutes(5)));
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.of(reservation));

            ProductReservationCommitResult result = service.commit(STORE, REF);

            assertThat(result.status()).isFalse();
            assertThat(result.reservationId()).isEqualTo(7L);
            assertThat(reservation.getStatus()).isEqualTo(ProductReservationStatus.TEMPORARY_RESERVED);
        }

        @Test
        void commitIsIdempotentAndReleasedCannotBeCommitted() {
            ProductReservation completed = held(ProductReservationStatus.COMPLETED, Instant.now());
            ProductReservation released = held(ProductReservationStatus.ROLLBACK, Instant.now());
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF))
                    .thenReturn(Optional.of(completed), Optional.of(released));

            assertThat(service.commit(STORE, REF).status()).isTrue();
            assertThat(service.commit(STORE, REF).status()).isFalse();
        }
    }

    @Nested
    class Release {

        @Test
        void unknownRefIsNotReleased() {
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.empty());

            ProductReservationReleaseResult result = service.release(STORE, REF);

            assertThat(result.status()).isFalse();
            assertThat(result.reservationId()).isNull();
        }

        @Test
        void heldStockGoesBackToTheInventoryRows() {
            Inventory a = stocked(SKU_A, 6);
            ProductReservation reservation = held(ProductReservationStatus.TEMPORARY_RESERVED, Instant.now());
            reservation.addLine(a, 4);
            reservation.addLine(stocked(SKU_B, 0), 1);
            reservation.getLines().get(1).setInventory(null);
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF)).thenReturn(Optional.of(reservation));

            ProductReservationReleaseResult result = service.release(STORE, REF);

            assertThat(result.status()).isTrue();
            assertThat(result.reservationId()).isEqualTo(7L);
            assertThat(a.getQuantity()).isEqualTo(10);
            assertThat(reservation.getStatus()).isEqualTo(ProductReservationStatus.ROLLBACK);
        }

        @Test
        void releaseIsIdempotentAndCommittedCannotBeReleased() {
            ProductReservation released = held(ProductReservationStatus.ROLLBACK, Instant.now());
            ProductReservation completed = held(ProductReservationStatus.COMPLETED, Instant.now());
            when(reservationRepository.findByStoreMerchantIdAndRef(STORE, REF))
                    .thenReturn(Optional.of(released), Optional.of(completed));

            assertThat(service.release(STORE, REF).status()).isTrue();
            assertThat(service.release(STORE, REF).status()).isFalse();
        }
    }
}

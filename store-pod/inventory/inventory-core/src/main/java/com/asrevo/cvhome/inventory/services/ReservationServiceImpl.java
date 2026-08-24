package com.asrevo.cvhome.inventory.services;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.Inventory;
import com.asrevo.cvhome.inventory.entity.ProductReservation;
import com.asrevo.cvhome.inventory.entity.ProductReservationLine;
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

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ProductReservationRepository reservationRepository;

    private final InventoryRepository inventoryRepository;

    private final Duration expiry;

    public ReservationServiceImpl(ProductReservationRepository reservationRepository,
                                  InventoryRepository inventoryRepository,
                                  @Value("${reservation.expiry.minutes:45}") int expiryMinutes) {
        this.reservationRepository = reservationRepository;
        this.inventoryRepository = inventoryRepository;
        this.expiry = Duration.ofMinutes(expiryMinutes);
    }

    @Override
    @Transactional
    public ProductReservationReserveResult reserve(StoreMerchantId store, String ref, ProductReservationList request)
            throws InsufficientInventoryException, EmptyReservationException {
        if (request.entries() == null || request.entries().isEmpty()) {
            throw EmptyReservationException.of(ref);
        }
        ProductReservation reservation = reservationRepository.findByStoreMerchantIdAndRef(store, ref)
                .orElseGet(() -> new ProductReservation(store, ref));
        reservation.setStatus(ProductReservationStatus.TEMPORARY_RESERVED);
        reservation.setExpireAt(Instant.now().plus(expiry));

        for (ReserveProductEntry entry : request.entries()) {
            if (reservation.holds(entry.sku())) {
                continue; // a retry of the same ref must not take the stock twice
            }
            reservation.addLine(take(store, entry.sku(), entry.reserveQty()), entry.reserveQty());
        }
        ProductReservation saved = reservationRepository.save(reservation);
        return new ProductReservationReserveResult(true, saved.getId(), saved.getExpireAt());
    }

    private Inventory take(StoreMerchantId store, String sku, int quantity) throws InsufficientInventoryException {
        Inventory inventory = inventoryRepository.lockBySku(store, sku)
                .orElseThrow(() -> InsufficientInventoryException.notStocked(sku, quantity));
        if (inventory.getQuantity() < quantity) {
            throw InsufficientInventoryException.of(sku, quantity, inventory.getQuantity());
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        return inventory;
    }

    @Override
    @Transactional
    public ProductReservationCommitResult commit(StoreMerchantId store, String ref) {
        ProductReservation reservation = reservationRepository.findByStoreMerchantIdAndRef(store, ref).orElse(null);
        if (reservation == null) {
            return new ProductReservationCommitResult(false, null, null);
        }
        boolean committed = switch (reservation.getStatus()) {
            case COMPLETED -> true;
            case ROLLBACK -> false;
            case TEMPORARY_RESERVED -> {
                if (reservation.isExpired(Instant.now())) {
                    log.warn("Reservation {} expired at {}; not committed", ref, reservation.getExpireAt());
                    yield false;
                }
                reservation.setStatus(ProductReservationStatus.COMPLETED);
                yield true;
            }
        };
        return new ProductReservationCommitResult(committed, reservation.getId(), reservation.getExpireAt());
    }

    @Override
    @Transactional
    public ProductReservationReleaseResult release(StoreMerchantId store, String ref) {
        ProductReservation reservation = reservationRepository.findByStoreMerchantIdAndRef(store, ref).orElse(null);
        if (reservation == null) {
            return new ProductReservationReleaseResult(false, null, null);
        }
        boolean released = switch (reservation.getStatus()) {
            case ROLLBACK -> true;
            case COMPLETED -> false;
            case TEMPORARY_RESERVED -> {
                for (ProductReservationLine line : reservation.getLines()) {
                    Inventory inventory = line.getInventory();
                    if (inventory != null) {
                        inventory.setQuantity(inventory.getQuantity() + line.getQuantity());
                    }
                }
                reservation.setStatus(ProductReservationStatus.ROLLBACK);
                yield true;
            }
        };
        return new ProductReservationReleaseResult(released, reservation.getId(), reservation.getExpireAt());
    }
}

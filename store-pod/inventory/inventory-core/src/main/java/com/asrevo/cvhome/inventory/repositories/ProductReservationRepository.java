package com.asrevo.cvhome.inventory.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductReservation;
import com.asrevo.cvhome.inventory.entity.ProductReservationStatus;

public interface ProductReservationRepository extends JpaRepository<ProductReservation, Long> {

    Optional<ProductReservation> findByStoreMerchantIdAndRef(StoreMerchantId store, String ref);

    /**
     * Held reservations past their expiry, locked so a concurrent commit and the expiry job cannot both act on one.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ProductReservation> findByStatusAndExpireAtBeforeOrderById(ProductReservationStatus status,
                                                                     Instant expireAt);
}

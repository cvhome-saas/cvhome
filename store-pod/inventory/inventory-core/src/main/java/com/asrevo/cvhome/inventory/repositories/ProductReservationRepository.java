package com.asrevo.cvhome.inventory.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.entity.ProductReservation;
import com.asrevo.cvhome.inventory.entity.ProductReservationStatus;

public interface ProductReservationRepository extends JpaRepository<ProductReservation, Long> {

    Optional<ProductReservation> findByRef(String ref, StoreMerchantId storeMerchantId);

    List<ProductReservation> findAllByRef(String ref, StoreMerchantId storeMerchantId);

    @Query(value = "SELECT pr FROM ProductReservation pr WHERE pr.status = :status AND pr.expireAt < :expireAt ORDER BY pr.id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ProductReservation> findExpiredReservations(@Param("status") ProductReservationStatus status,
                                                     @Param("expireAt") Instant expireAt);
}

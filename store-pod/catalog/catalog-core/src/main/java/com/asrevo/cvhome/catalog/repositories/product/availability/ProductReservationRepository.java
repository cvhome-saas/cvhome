package com.asrevo.cvhome.catalog.repositories.product.availability;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservation;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservationStatus;

public interface ProductReservationRepository extends JpaRepository<ProductReservation, Long> {

    Optional<ProductReservation> findByRef(String ref);

    List<ProductReservation> findAllByRef(String ref);

    @Query(value = "SELECT pr FROM ProductReservation pr WHERE pr.status = :status AND pr.expireAt < :expireAt ORDER BY pr.id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<ProductReservation> findExpiredReservations(@Param("status") ProductReservationStatus status,
                                                     @Param("expireAt") Instant expireAt);
}
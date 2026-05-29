package com.asrevo.cvhome.catalog.repositories.product.availability;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservation;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservationStatusEnum;

public interface ProductReservationRepository extends JpaRepository<ProductReservation, Long> {
    List<ProductReservation> findAllByStatusAndExpireAtBefore(ProductReservationStatusEnum status, Instant now);

    List<ProductReservation> findAllByOrderIdAndStatus(Long orderId, ProductReservationStatusEnum status);
}

package com.asrevo.cvhome.catalog.repositories.product.availability;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservation;

public interface ProductReservationRepository extends JpaRepository<ProductReservation, Long> {

    Optional<ProductReservation> findByOrderIdAndSku(Long orderId, String sku);

    List<ProductReservation> findAllByOrderId(Long orderId);
}

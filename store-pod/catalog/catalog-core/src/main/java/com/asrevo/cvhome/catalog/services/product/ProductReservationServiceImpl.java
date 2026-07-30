package com.asrevo.cvhome.catalog.services.product;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservation;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservationLine;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservationStatus;
import com.asrevo.cvhome.catalog.model.product.ProductReservationCommitResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReleaseResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReserveResult;
import com.asrevo.cvhome.catalog.repositories.product.availability.ProductAvailabilityRepository;
import com.asrevo.cvhome.catalog.repositories.product.availability.ProductReservationRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductReservationServiceImpl implements ProductReservationService {

    private final ProductReservationRepository productReservationRepository;
    private final ProductAvailabilityRepository productAvailabilityRepository;

    @Value("${reservation.expiry.minutes:45}")
    private int reservationExpiryMinutes;

    @Transactional
    @Override
    public ProductReservationReserveResult reserve(StoreMerchantId store, String ref, ProductReservationList productReservation) {
        return getProductReservationResult(store, ref, productReservation, ProductReservationStatus.TEMPORARY_RESERVED);
    }

    private ProductReservationReserveResult getProductReservationResult(StoreMerchantId store, String ref,
                                                                        ProductReservationList productReservation,
                                                                        ProductReservationStatus status) {
        try {
            ProductReservation reservation = doReserveWithStatus(store, ref, productReservation, status);
            return ProductReservationReserveResult.builder().status(true).reservationId(reservation.getId())
                    .expireAt(reservation.getExpireAt())
                    .build();
        } catch (ServiceException _) {
            return ProductReservationReserveResult.builder().status(false).build();
        }
    }

    private ProductReservation doReserveWithStatus(StoreMerchantId store, String ref, ProductReservationList productReservation,
                                                   ProductReservationStatus status) throws ServiceException {
        if (Objects.isNull(productReservation.entries()) || productReservation.entries().isEmpty()) {
            throw new ServiceException("No entries to reserve");
        }

        ProductReservation reservation = productReservationRepository.findByRef(ref, store)
                .orElseGet(() -> {
                    ProductReservation res = new ProductReservation();
                    res.setRef(ref);
                    res.setStoreMerchantId(store);
                    res.setStatus(status);
                    res.setExpireAt(Instant.now().plus(Duration.ofMinutes(reservationExpiryMinutes)));
                    return res;
                });

        // Update status and expiry for the existing or new reservation
        reservation.setStatus(status);
        reservation.setExpireAt(Instant.now().plus(Duration.ofMinutes(reservationExpiryMinutes)));

        for (ReserveProductEntry entry : productReservation.entries()) {
            // Idempotency check: if line already exists for this SKU, skip
            boolean exists = reservation.getLines().stream()
                    .anyMatch(l -> Objects.equals(l.getSku(), entry.sku()));

            if (exists) {
                log.info("Active reservation line for ref {} and sku {} already exists. Skipping.", ref, entry.sku());
                continue;
            }

            List<ProductAvailability> availabilities = productAvailabilityRepository.getBySku(entry.sku(), store);
            if (availabilities.isEmpty()) {
                throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
            }

            // Pick the first availability for the store
            ProductAvailability availability = availabilities.getFirst();

            if (availability.getProductQuantity() < entry.reserveQty()) {
                throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
            }

            // Deduct quantity
            availability.setProductQuantity(availability.getProductQuantity() - entry.reserveQty());

            // Create Reservation Line
            ProductReservationLine line = new ProductReservationLine();
            line.setSku(entry.sku());
            line.setQuantity(entry.reserveQty());
            line.setProductAvailability(availability);
            line.setProductReservation(reservation);

            reservation.getLines().add(line);

            productAvailabilityRepository.save(availability);
        }
        return productReservationRepository.save(reservation);
    }

    @Transactional
    @Override
    public ProductReservationCommitResult commit(StoreMerchantId store, String ref) {
        try {
            List<ProductReservation> reservations =
                    productReservationRepository.findAllByRef(ref, store);
            ProductReservation committedRes = null;
            for (ProductReservation res : reservations) {
                if (Objects.equals(res.getStoreMerchantId(), store)) {
                    if (res.getStatus() == ProductReservationStatus.COMPLETED) {
                        log.info("Reservation for ref {} already committed", ref);
                        return ProductReservationCommitResult.builder().status(true).reservationId(res.getId())
                                .expireAt(res.getExpireAt()).build();
                    }
                    if (res.getStatus() == ProductReservationStatus.TEMPORARY_RESERVED) {
                        if (res.getExpireAt().isBefore(Instant.now())) {
                            log.error("Cannot commit reservation for ref {} because it has expired at {}", ref, res.getExpireAt());
                            // Optional: auto-release here or leave for cleanup service
                            return ProductReservationCommitResult.builder().status(false).reservationId(res.getId())
                                    .expireAt(res.getExpireAt()).build();
                        }

                        res.setStatus(ProductReservationStatus.COMPLETED);
                        committedRes = productReservationRepository.save(res);
                        log.info("Committed reservation for ref {}", ref);
                    }
                }
            }
            if (committedRes != null) {
                return ProductReservationCommitResult.builder().status(true).reservationId(committedRes.getId())
                        .expireAt(committedRes.getExpireAt()).build();
            }
            return ProductReservationCommitResult.builder().status(false).build();
        } catch (Exception e) {
            log.error("Error committing reservation for ref {}", ref, e);
            return ProductReservationCommitResult.builder().status(false).build();
        }
    }

    @Transactional
    @Override
    public ProductReservationReleaseResult release(StoreMerchantId store, String ref) {
        try {
            List<ProductReservation> reservations =
                    productReservationRepository.findAllByRef(ref, store);
            ProductReservation releasedRes = null;
            for (ProductReservation res : reservations) {
                if (Objects.equals(res.getStoreMerchantId(), store)) {
                    if (res.getStatus() == ProductReservationStatus.ROLLBACK) {
                        log.info("Reservation for ref {} already released", ref);
                        return ProductReservationReleaseResult.builder().status(true).reservationId(res.getId())
                                .expireAt(res.getExpireAt()).build();
                    }
                    if (res.getStatus() == ProductReservationStatus.TEMPORARY_RESERVED) {
                        for (ProductReservationLine line : res.getLines()) {
                            var availability = line.getProductAvailability();
                            if (availability != null) {
                                availability.setProductQuantity(availability.getProductQuantity() + line.getQuantity());
                                productAvailabilityRepository.save(availability);
                            }
                        }

                        // Mark as rollback
                        res.setStatus(ProductReservationStatus.ROLLBACK);
                        releasedRes = productReservationRepository.save(res);
                        log.info("Released reservation for ref {}. Status was {}", ref, res.getStatus());
                    }
                }
            }
            if (releasedRes != null) {
                return ProductReservationReleaseResult.builder().status(true).reservationId(releasedRes.getId())
                        .expireAt(releasedRes.getExpireAt()).build();
            }
            return ProductReservationReleaseResult.builder().status(false).build();
        } catch (Exception e) {
            log.error("Error releasing reservation for ref {}", ref, e);
            return ProductReservationReleaseResult.builder().status(false).build();
        }
    }

}
package com.asrevo.cvhome.catalog.services.product;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservation;
import com.asrevo.cvhome.catalog.entity.product.availability.ProductReservationStatus;
import com.asrevo.cvhome.catalog.model.product.ProductReservationResult;
import com.asrevo.cvhome.catalog.repositories.product.ProductRepository;
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
    private final ProductService productService;
    private final ProductRepository productRepository;

    @Value("${reservation.expiry.minutes:15}")
    private int reservationExpiryMinutes;

    @Transactional
    @Override
    public ProductReservationResult reserve(StoreMerchantId store, Long orderId, ProductReservationList productReservation)
            throws ServiceException {
        doReserveWithStatus(store, orderId, productReservation, ProductReservationStatus.TEMPORARY_RESERVED);
        return new ProductReservationResult(true);
    }

    @Transactional
    @Override
    public ProductReservationResult autoCommit(StoreMerchantId store, Long orderId, ProductReservationList productReservation)
            throws ServiceException {
        doReserveWithStatus(store, orderId, productReservation, ProductReservationStatus.COMPLETED);
        return new ProductReservationResult(true);
    }

    private void doReserveWithStatus(StoreMerchantId store, Long orderId, ProductReservationList productReservation,
                                     ProductReservationStatus status) throws ServiceException {
        if (Objects.isNull(productReservation.entries()) || productReservation.entries().isEmpty()) {
            throw new ServiceException("No entries to reserve");
        }

        for (ReserveProductEntry entry : productReservation.entries()) {
            // Idempotency check: if reservation already exists for this order and SKU, skip
            Optional<ProductReservation> existingReservation = productReservationRepository.findByOrderIdAndSku(orderId, entry.sku());
            if (existingReservation.isPresent()) {
                log.info("Reservation for order {} and sku {} already exists. Skipping.", orderId, entry.sku());
                continue;
            }

            Product product = productRepository
                    .getByProductIdFetchAvailabilities(productService.findProductIdByCode(entry.sku(), store), store);

            for (ProductAvailability availability : product.getAvailabilities()) {
                if (availability.getProductQuantity() < entry.reserveQty()) {
                    throw new ServiceException(ServiceException.EXCEPTION_INVENTORY_MISMATCH);
                }

                // Deduct quantity
                availability.setProductQuantity(availability.getProductQuantity() - entry.reserveQty());

                // Create Reservation
                ProductReservation reservation = new ProductReservation();
                reservation.setSku(entry.sku());
                reservation.setQuantity(entry.reserveQty());
                reservation.setOrderId(orderId);
                reservation.setExpireAt(Instant.now().plus(Duration.ofMinutes(reservationExpiryMinutes)));
                reservation.setStoreMerchantId(store);
                reservation.setProductAvailability(availability);
                reservation.setStatus(status);

                productReservationRepository.save(reservation);
            }
            productRepository.save(product);
        }
    }

    @Transactional
    @Override
    public void commit(StoreMerchantId store, Long orderId) {
        List<ProductReservation> reservations =
                productReservationRepository.findAllByOrderId(orderId);
        for (ProductReservation res : reservations) {
            if (res.getStatus() == ProductReservationStatus.TEMPORARY_RESERVED) {
                res.setStatus(ProductReservationStatus.COMPLETED);
                productReservationRepository.save(res);
                log.info("Committed reservation for order {} and sku {}", orderId, res.getSku());
            }
        }
    }

    @Transactional
    @Override
    public void release(StoreMerchantId store, Long orderId) {
        List<ProductReservation> reservations =
                productReservationRepository.findAllByOrderId(orderId);
        for (ProductReservation res : reservations) {
            // Restore quantity for TEMPORARY_RESERVED status
            if (res.getStatus() == ProductReservationStatus.TEMPORARY_RESERVED) {
                var availability = res.getProductAvailability();
                availability.setProductQuantity(availability.getProductQuantity() + res.getQuantity());

                // Mark as rollback
                res.setStatus(ProductReservationStatus.ROLLBACK);
                productReservationRepository.save(res);
                log.info("Released reservation for order {} and sku {}. Status was {}", orderId, res.getSku(), res.getStatus());
            }
        }
    }

    @Transactional
    @Override
    public void expire(StoreMerchantId store, Long orderId) {
        List<ProductReservation> reservations =
                productReservationRepository.findAllByOrderId(orderId);
        for (ProductReservation res : reservations) {
            // Restore quantity for TEMPORARY_RESERVED status
            if (res.getStatus() == ProductReservationStatus.TEMPORARY_RESERVED) {
                var availability = res.getProductAvailability();
                availability.setProductQuantity(availability.getProductQuantity() + res.getQuantity());

                // Mark as rollback
                res.setStatus(ProductReservationStatus.EXPIRED);
                productReservationRepository.save(res);
                log.info("Released reservation for order {} and sku {}. Status was {}", orderId, res.getSku(), res.getStatus());
            }
        }
    }

}
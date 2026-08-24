package com.asrevo.cvhome.checkout.service.facade.order;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReleaseResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.inventory.services.ExternalProductReservationService;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;

import lombok.extern.slf4j.Slf4j;

@Service("orderInventoryOrchestrator")
@Slf4j
public class OrderInventoryOrchestratorImpl implements OrderInventoryOrchestrator {

    private final OrderService orderService;
    private final ExternalProductReservationService externalProductReservationService;

    public OrderInventoryOrchestratorImpl(OrderService orderService,
                                          ExternalProductReservationService externalProductReservationService) {
        this.orderService = orderService;
        this.externalProductReservationService = externalProductReservationService;
    }

    /**
     * Passes both catalog failures straight through.
     *
     * <p>
     * This method used to be {@code catch (Exception _) -> status(false)}, which made "the shopper cannot have this"
     * and "catalog is restarting" indistinguishable one line after they arrived — so a deploy told shoppers their
     * basket was out of stock, and a reservation catalog had actually taken was abandoned. There is nothing useful to
     * do with either failure here; the placement flow is the only layer that knows what to do about it.
     * </p>
     */
    @Override
    public ProductReservationReserveResult reserveProduct(StoreMerchantId store, Order order)
            throws ProductReservationRejectedException, InventoryApiUnavailableException {
        return externalProductReservationService.reserve(store, order.getId().toString(), toProductReservationList(order));
    }

    private ProductReservationList toProductReservationList(Order modelOrder) {
        return modelOrder.getOrderProducts()
                .stream()
                .map(it -> new ReserveProductEntry(it.getSku(), it.getProductQuantity()))
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ProductReservationList::new));
    }

    @Override
    public void updateOrderStatusWithReservationCommit(Long orderId, StoreMerchantId store, OrderStatus successOrder,
                                                       PaymentStatus successPay) throws InventoryApiUnavailableException {
        ProductReservationCommitResult result = externalProductReservationService.commit(store, orderId.toString());
        if (result.status()) {
            orderService.updateOrderStatus(orderId, successOrder, InventoryStatus.COMMITTED, successPay);
        } else {
            log.error("Failed to commit reservation for order {} at catalog service", orderId);
            // We keep the status as is (likely RESERVED) to allow retry
        }
    }

    /**
     * Marks the reservation failed on either outcome, but only after catalog has been asked.
     *
     * <p>
     * An unreachable catalog still ends in {@code RESERVATION_FAILED} — unlike commit, there is no state worth
     * preserving, and catalog's expiry job frees anything still held — but it is rethrown so the caller learns the
     * release was never confirmed.
     * </p>
     */
    @Override
    public void updateOrderStatusWithReservationRelease(Long orderId, StoreMerchantId store, OrderStatus successOrder,
                                                        PaymentStatus successPay) throws InventoryApiUnavailableException {
        ProductReservationReleaseResult result;
        try {
            result = externalProductReservationService.release(store, orderId.toString());
        } catch (InventoryApiUnavailableException e) {
            log.error("Catalog unreachable while releasing the reservation for order {}", orderId, e);
            orderService.updateOrderStatus(orderId, successOrder, InventoryStatus.RESERVATION_FAILED, successPay);
            throw e;
        }

        if (result.status()) {
            orderService.updateOrderStatus(orderId, successOrder, InventoryStatus.RELEASED, successPay);
        } else {
            log.error("Failed to release reservation for order {} at catalog service", orderId);
            orderService.updateOrderStatus(orderId, successOrder, InventoryStatus.RESERVATION_FAILED, successPay);
        }
    }
}

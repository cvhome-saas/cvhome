package com.asrevo.cvhome.checkout.service.facade.order;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

/**
 * Checkout's half of the reservation conversation with catalog.
 *
 * <p>
 * Every method here keeps the two catalog failures apart, because they call for opposite handling: a refusal is an
 * answer and the order can be resolved on it, while an unreachable catalog decides nothing and the order has to be
 * left recoverable. They used to share one {@code catch (Exception)}.
 * </p>
 */
public interface OrderInventoryOrchestrator {

    /**
     * Takes stock for an order.
     *
     * @throws ProductReservationRejectedException the stock is not there; nothing was reserved
     * @throws InventoryApiUnavailableException      catalog never answered; the stock may or may not be held
     */
    ProductReservationReserveResult reserveProduct(StoreMerchantId store, Order order)
            throws ProductReservationRejectedException, InventoryApiUnavailableException;

    /**
     * Commits the reservation and moves the order on. A commit catalog declines leaves the order's status untouched so
     * it can be retried.
     *
     * @throws InventoryApiUnavailableException catalog never answered; the order is left as it was, for retry
     */
    void updateOrderStatusWithReservationCommit(Long orderId, StoreMerchantId store, OrderStatus successOrder,
                                                PaymentStatus successPay) throws InventoryApiUnavailableException;

    /**
     * Releases the reservation and moves the order on.
     *
     * @throws InventoryApiUnavailableException catalog never answered; the order is marked {@code RESERVATION_FAILED},
     *                                        since any stock still held will be freed by catalog's expiry job
     */
    void updateOrderStatusWithReservationRelease(Long orderId, StoreMerchantId store, OrderStatus successOrder,
                                                 PaymentStatus successPay) throws InventoryApiUnavailableException;
}

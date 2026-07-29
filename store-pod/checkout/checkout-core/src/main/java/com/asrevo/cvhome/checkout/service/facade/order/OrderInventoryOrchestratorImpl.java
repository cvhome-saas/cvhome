package com.asrevo.cvhome.checkout.service.facade.order;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.model.product.ProductReservationCommitResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReleaseResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReserveResult;
import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
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

    @Override
    public ProductReservationReserveResult reserveProduct(StoreMerchantId store, Order order) {
        try {
            return externalProductReservationService.reserve(store, order.getId().toString(), toProductReservationList(order));
        } catch (Exception _) {
            return ProductReservationReserveResult.builder().status(false).build();
        }
    }

    private ProductReservationList toProductReservationList(Order modelOrder) {
        return modelOrder.getOrderProducts()
                .stream()
                .map(it -> new ReserveProductEntry(it.getSku(), it.getProductQuantity()))
                .collect(Collectors.collectingAndThen(Collectors.toSet(), ProductReservationList::new));
    }

    @Override
    public void updateOrderStatusWithReservationCommit(Long orderId, StoreMerchantId store, OrderStatus successOrder,
                                                       PaymentStatus successPay) {
        try {
            ProductReservationCommitResult result = externalProductReservationService.commit(store, orderId.toString());
            if (result.status()) {
                orderService.updateOrderStatus(orderId, successOrder, InventoryStatus.COMMITTED, successPay);
            } else {
                log.error("Failed to commit reservation for order {} at catalog service", orderId);
                // We keep the status as is (likely RESERVED) to allow retry
            }
        } catch (Exception e) {
            log.error("Error calling external reservation commit for order {}", orderId, e);
            throw new ServiceRuntimeException("Error during reservation commit", e);
        }
    }

    @Override
    public void updateOrderStatusWithReservationRelease(Long orderId, StoreMerchantId store, OrderStatus successOrder,
                                                        PaymentStatus successPay) {
        try {
            ProductReservationReleaseResult result = externalProductReservationService.release(store, orderId.toString());
            if (result.status()) {
                orderService.updateOrderStatus(orderId, successOrder, InventoryStatus.RELEASED, successPay);
            } else {
                log.error("Failed to release reservation for order {} at catalog service", orderId);
                orderService.updateOrderStatus(orderId, successOrder, InventoryStatus.RESERVATION_FAILED, successPay);
            }
        } catch (Exception e) {
            log.error("Error calling external reservation release for order {}", orderId, e);
            orderService.updateOrderStatus(orderId, successOrder, InventoryStatus.RESERVATION_FAILED, successPay);
            throw new ServiceRuntimeException("Error during reservation release", e);
        }
    }
}

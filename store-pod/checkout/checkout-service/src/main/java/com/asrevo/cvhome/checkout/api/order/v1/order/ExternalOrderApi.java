package com.asrevo.cvhome.checkout.api.order.v1.order;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.checkout.service.facade.order.OrderInventoryOrchestrator;
import com.asrevo.cvhome.checkout.services.order.ExternalOrderService;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "External Order API", description = "API for external services to interact with orders")
@Slf4j
@AllArgsConstructor
public class ExternalOrderApi implements ExternalOrderService {

    private final OrderFacade orderFacade;
    private final OrderService orderService;
    private final OrderInventoryOrchestrator orderInventoryOrchestrator;

    @Override
    @PostMapping("/private/order/{orderRef}/payment-status")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.CHECKOUT.*')")
    public void updatePaymentStatus(StoreMerchantId store, @PathVariable String orderRef,
                                   @RequestParam PaymentStatus status) {
        log.info("Updating payment status for order {} to {} for store {}", orderRef, status, store);
        Long orderId = Long.parseLong(orderRef);

        Order order = orderService.getOrder(orderId, store);
        if (order == null) {
            log.warn("Order {} not found for store {}", orderId, store);
            return;
        }

        if (order.getPaymentStatus() == status) {
            log.info("Order {} already has payment status {}. Skipping.", orderId, status);
            return;
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            log.warn("Order {} is in terminal state {}. Cannot update payment status to {}.", orderId, order.getStatus(), status);
            return;
        }

        if (status == PaymentStatus.PAID) {
            orderInventoryOrchestrator.updateOrderStatusWithReservationCommit(orderId, store, OrderStatus.CONFIRMED, status);
        } else if (status == PaymentStatus.FAILED || status == PaymentStatus.EXPIRED || status == PaymentStatus.CANCELLED) {
            orderInventoryOrchestrator.updateOrderStatusWithReservationRelease(orderId, store, OrderStatus.CANCELLED, status);
        } else {
            orderFacade.updateOrderStatus(orderId, null, null, status);
        }
    }

    @Override
    @PostMapping("/private/order/{orderRef}/reservation-expired")
    @PreAuthorize("hasPermission(#store,'StoreMerchantId','STORE-POD.CHECKOUT.*')")
    public void handleReservationExpired(StoreMerchantId store, @PathVariable String orderRef) {
        log.info("Handling reservation expired for order {} for store {}", orderRef, store);
        Long orderId = Long.parseLong(orderRef);
        orderInventoryOrchestrator.updateOrderStatusWithReservationRelease(orderId, store, OrderStatus.CANCELLED, PaymentStatus.EXPIRED);
    }
}

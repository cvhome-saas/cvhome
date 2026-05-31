package com.asrevo.cvhome.checkout.service.job;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.services.product.ExternalPaymentGatewayService;
import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.catalog.services.product.model.PaymentResponse;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentJobQueryService {

    private final OrderFacade orderFacade;
    private final ExternalPaymentGatewayService externalPaymentGatewayService;
    private final ExternalProductReservationService externalProductReservationService;

    public PaymentJobQueryService(OrderFacade orderFacade,
                                  ExternalPaymentGatewayService externalPaymentGatewayService,
                                  ExternalProductReservationService externalProductReservationService) {
        this.orderFacade = orderFacade;
        this.externalPaymentGatewayService = externalPaymentGatewayService;
        this.externalProductReservationService = externalProductReservationService;
    }

    @Scheduled(fixedRate = 60000)
    public void processPendingOnlinePayments() {
        log.info("Starting job to process pending online payments.");

        // Assuming OrderFacade has a method to find orders pending online payment
        // This method needs to be implemented in OrderFacade
        List<Order> pendingOrders = orderFacade.findPendingOnlinePaymentOrders();

        if (pendingOrders.isEmpty()) {
            log.info("No pending online payment orders found.");
            return;
        }

        for (Order order : pendingOrders) {
            try {

                log.debug("Checking payment status for order {} with orderId {}", order.getId(), order.getId());
                PaymentResponse response = externalPaymentGatewayService.status(order.getId());

                switch (response.status()) {
                    case SUCCESS:
                        log.info("Payment for order {} is SUCCESS. Committing inventory.", order.getId());
                        // Commit inventory
                        commit(order.getStoreMerchantId(), order.getId());
                        // Update order status to PAID
                        orderFacade.updateOrderStatus(order.getId(), OrderStatus.PAID);
                        log.info("Order {} successfully processed and inventory committed.", order.getId());
                        break;
                    case FAILED:
                        log.warn("Payment for order {} is FAILED. Releasing inventory if reserved.", order.getId());
                        // Release inventory (if it was reserved earlier during redirect)
                        release(order.getStoreMerchantId(), order.getId());
                        // Update order status to FAILED
                        orderFacade.updateOrderStatus(order.getId(), OrderStatus.FAILED);
                        log.warn("Order {} marked as FAILED and inventory released.", order.getId());
                        break;
                    case PENDING, REDIRECT_REQUIRED:
                        log.debug("Payment for order {} is still {}. Will recheck later.", order.getId(), response.status());
                        break;
                    default:
                        log.warn("Unknown payment status {} for order {}. Skipping.", response.status(), order.getId());
                        break;
                }
            } catch (Exception e) {
                log.error("Error processing pending payment for order {}: {}", order.getId(), e.getMessage(), e);
            }
        }
        log.info("Finished job to process pending online payments.");
    }

    private void release(StoreMerchantId store, Long orderId) {
        try {
            log.debug("Un-reserving inventory for order {}", orderId);
            externalProductReservationService.release(store, orderId);
        } catch (Exception e) {
            log.error("Error while un-reserving inventory for order {}", orderId, e);
        }
    }

    private void commit(StoreMerchantId store, Long orderId) {
        try {
            log.debug("Commit inventory reservation for order {}", orderId);
            externalProductReservationService.commit(store, orderId);
        } catch (Exception e) {
            log.error("Error while commit inventory for order {}", orderId, e);
        }
    }
}

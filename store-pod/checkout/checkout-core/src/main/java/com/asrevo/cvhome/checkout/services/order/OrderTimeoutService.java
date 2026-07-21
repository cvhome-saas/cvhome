package com.asrevo.cvhome.checkout.services.order;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.repositories.order.OrderRepository;
import com.asrevo.cvhome.checkout.service.facade.order.OrderInventoryOrchestrator;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderTimeoutService {

    private final OrderRepository orderRepository;
    private final OrderInventoryOrchestrator orderInventoryOrchestrator;

    @Scheduled(fixedRateString = "${order.timeout.check.interval:300000}") // Every 5 minutes
    @Transactional
    public void cancelExpiredOrders() {
        log.info("Starting cleanup of expired orders.");

        // For Stripe: 30 minutes
        processExpiredOrders(PaymentType.STRIPE, Duration.ofMinutes(30), PaymentStatus.EXPIRED);

        // For PayPal: 30 minutes
        processExpiredOrders(PaymentType.PAYPAL, Duration.ofMinutes(30), PaymentStatus.EXPIRED);

        // For Manual Transfer: 48 hours
        processExpiredOrders(PaymentType.MANUAL_TRANSFER, Duration.ofHours(48), PaymentStatus.EXPIRED);
        
        log.info("Finished cleanup of expired orders.");
    }

    private void processExpiredOrders(PaymentType type, Duration duration, PaymentStatus status) {
        Instant cutoff = Instant.now().minus(duration);
        List<Order> expiredOrders = orderRepository.findExpiredOrders(type, cutoff);
        
        if (!expiredOrders.isEmpty()) {
            log.info("Found {} expired orders for payment type {}", expiredOrders.size(), type);
            for (Order order : expiredOrders) {
                log.info("Expiring order {} for store {}", order.getId(), order.getStoreMerchantId());
                try {
                    orderInventoryOrchestrator.updateOrderStatusWithReservationRelease(
                        order.getId(), order.getStoreMerchantId(), OrderStatus.CANCELLED, status);
                } catch (Exception e) {
                    log.error("Failed to expire order {} due to timeout", order.getId(), e);
                }
            }
        }
    }
}

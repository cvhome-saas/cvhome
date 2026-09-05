package com.asrevo.cvhome.checkout.services.jobs;

import java.time.Clock;
import java.util.List;
import java.util.function.Consumer;

import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.asrevo.cvhome.checkout.config.CheckoutProperties;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.checkout.services.order.OrderStepRunner;
import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Closes orders nobody paid for within their window. Before cancelling a card payment it asks payment once — a
 * webhook that is merely late must never cancel an order that was charged.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryJob {

    private static final String JOB = "expiry-job";

    private final OrderRepository orders;

    private final OrderStepRunner steps;

    private final ExternalPaymentGatewayService payments;

    private final CheckoutProperties properties;

    private final TransactionTemplate transactions;

    private final Clock clock;

    @Scheduled(fixedDelayString = "${checkout.expiry.interval:60s}")
    public void expire() {
        List<Long> due = orders.findExpiredIds(clock.instant(), PageRequest.of(0, properties.getExpiry().getBatchSize()));
        for (Long orderId : due) {
            expireOne(orderId);
        }
    }

    /**
     * @return whether the order was closed by this call
     */
    public boolean expireOne(Long orderId) {
        Order order = orders.findById(orderId).orElse(null);
        if (order == null || !order.isAwaitingPayment()) {
            return false;
        }
        if (isRedirectPayment(order.getPaymentType())) {
            PaymentResponse late = askPayment(order);
            if (late == null) {
                return false; // payment unreachable — decide nothing this pass
            }
            if (late.status() == PaymentStatus.PAID) {
                log.info("Order {} was paid after all; applying instead of expiring", orderId);
                String txRef = late.gatewayRef() == null ? JOB : late.gatewayRef();
                apply(orderId, o -> o.applyPaymentSignal(PaymentStatus.PAID, txRef, clock.instant()));
                finish(orderId);
                return false;
            }
        }
        if (!apply(orderId, o -> o.expired(clock.instant()))) {
            return false;
        }
        finish(orderId);
        return true;
    }

    private PaymentResponse askPayment(Order order) {
        try {
            PaymentResponse response = payments.status(order.getStoreMerchantId(), order.getOrderRef().value());
            return response == null || response.status() == null ? PaymentResponse.pending() : response;
        } catch (PaymentApiUnavailableException e) {
            log.warn("Order {}: payment unreachable while expiring, skipping this pass", order.getId());
            return null;
        }
    }

    private boolean apply(Long orderId, Consumer<Order> change) {
        try {
            return Boolean.TRUE.equals(transactions.execute(status -> {
                Order fresh = orders.findById(orderId).orElse(null);
                if (fresh == null || !fresh.isAwaitingPayment()) {
                    return false;
                }
                change.accept(fresh);
                orders.saveAndFlush(fresh);
                return true;
            }));
        } catch (ObjectOptimisticLockingFailureException | IllegalStateException e) {
            log.debug("Order {} changed under the expiry job: {}", orderId, e.getMessage());
            return false;
        }
    }

    private void finish(Long orderId) {
        try {
            steps.runUntilSettled(orderId, 1);
        } catch (BaseException e) {
            log.warn("Order {}: step after expiry left to recovery: {}", orderId, e.getMessage());
        }
    }

    private static boolean isRedirectPayment(PaymentType type) {
        return type == PaymentType.STRIPE || type == PaymentType.PAYPAL;
    }
}

package com.asrevo.cvhome.checkout.services.order;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import com.asrevo.cvhome.checkout.config.CheckoutProperties;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.model.order.PendingAction;
import com.asrevo.cvhome.checkout.repositories.CartRepository;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.inventory.services.ExternalProductReservationService;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.payment.api.errors.PaymentGatewayRejectedException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductEntry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Drives an order's {@link PendingAction}s: one remote call at a time, outside any transaction, then the result
 * applied to the aggregate in its own transaction under {@code @Version}. Placement, the signal API, the console and
 * the recovery job all go through here, so a step is executed the same way whoever asks for it.
 *
 * <p>
 * Both remotes are idempotent by the order ref, so a crash between the call and the apply is harmless: the next run
 * repeats the call and gets the same answer. Losing the optimistic lock means another replica applied the same step
 * first — logged, never an error.
 * </p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderStepRunner {

    private final OrderRepository orders;

    private final CartRepository carts;

    private final ExternalProductReservationService reservations;

    private final ExternalPaymentGatewayService payments;

    private final CheckoutProperties properties;

    private final Clock clock;

    private final TransactionTemplate transactions;

    /**
     * Runs pending actions until the order owes nothing or {@code maxSteps} were taken. Definitive refusals close the
     * order first and are then rethrown; "no answer" failures leave the pending action for the recovery job.
     */
    public Order runUntilSettled(Long orderId, int maxSteps) throws ProductReservationRejectedException,
            InventoryApiUnavailableException, PaymentGatewayRejectedException, PaymentApiUnavailableException {
        Order order = load(orderId);
        PaymentGatewayRejectedException paymentRefused = null;
        for (int step = 0; step < maxSteps && order.hasPendingAction(); step++) {
            try {
                order = switch (order.getPendingAction()) {
                    case RESERVE -> reserve(order);
                    case INITIATE_PAYMENT -> initiatePayment(order);
                    case COMMIT -> commit(order);
                    case RELEASE -> release(order);
                    case NONE -> order;
                };
            } catch (PaymentGatewayRejectedException e) {
                // The order is already CANCELLED with a RELEASE pending; keep going so the stock goes back now.
                paymentRefused = e;
            }
        }
        if (paymentRefused != null) {
            throw paymentRefused;
        }
        return order;
    }

    private Order reserve(Order order) throws ProductReservationRejectedException, InventoryApiUnavailableException {
        Instant now = clock.instant();
        Duration window = properties.paymentWindow(order.getPaymentType());
        Instant holdUntil = window == null ? null : now.plus(window);
        Set<ReserveProductEntry> entries = order.getLines().stream()
                .map(line -> new ReserveProductEntry(line.getSku(), line.getQuantity()))
                .collect(Collectors.toSet());
        try {
            ProductReservationReserveResult result = reservations.reserve(order.getStoreMerchantId(),
                    order.getOrderRef().value(), new ProductReservationList(entries, holdUntil));
            // COD never expires on our side; inventory's hold is only a safety net for the commit that follows.
            Instant expiresAt = window == null ? null : earliest(holdUntil, result.expireAt());
            return apply(order.getId(), o -> o.reserved(result.reservationId(), result.expireAt(), expiresAt, now));
        } catch (ProductReservationRejectedException e) {
            log.warn("Reservation refused for order {}: {}", order.getId(), e.getMessage());
            apply(order.getId(), o -> {
                o.reservationRefused(e.getMessage(), now);
                carts.findByStoreMerchantIdAndCode(o.getStoreMerchantId(), o.getCartCode())
                        .ifPresent(cart -> cart.reopen());
            });
            throw e;
        }
    }

    private Order initiatePayment(Order order) throws PaymentGatewayRejectedException, PaymentApiUnavailableException {
        Instant now = clock.instant();
        Instant expireAt = order.getExpiresAt();
        if (expireAt == null) {
            Duration window = properties.paymentWindow(order.getPaymentType());
            expireAt = now.plus(window == null ? properties.getPlacement().getCodPaymentExpiry() : window);
        }
        RedirectUrls urls = new RedirectUrls(order.getSuccessUrl(), order.getCancelUrl()).withOrderId(order.getId());
        PaymentRequest request = PaymentRequest.builder()
                .ref(order.getOrderRef().value())
                .amount(order.getTotal())
                .currency(order.getCurrency())
                .paymentType(order.getPaymentType())
                .expireAt(expireAt)
                .successUrl(urls.success())
                .cancelUrl(urls.cancel())
                .build();
        try {
            PaymentInitiateResult result = payments.initiatePayment(order.getStoreMerchantId(), request);
            return apply(order.getId(), o -> {
                switch (result.status()) {
                    case PAID -> o.paymentPaidAtInitiate(result.gatewayRef(), now);
                    case FAILED -> o.paymentFailedAtInitiate(result.gatewayRef(), "payment could not be started", now);
                    case PENDING -> o.paymentPending(result.gatewayRef(), result.redirectUrl(), now);
                    default -> throw new IllegalStateException(String.format("unknown initiate status %s",
                            result.status()));
                }
            });
        } catch (PaymentGatewayRejectedException e) {
            log.warn("Payment refused for order {}: {}", order.getId(), e.getMessage());
            apply(order.getId(), o -> o.paymentFailedAtInitiate(null, e.getMessage(), now));
            throw e;
        }
    }

    private Order commit(Order order) throws InventoryApiUnavailableException {
        Instant now = clock.instant();
        ProductReservationCommitResult result = reservations.commit(order.getStoreMerchantId(),
                order.getOrderRef().value());
        return apply(order.getId(), o -> {
            if (result.status()) {
                o.committed(now);
            } else {
                o.commitRefused(now);
            }
        });
    }

    private Order release(Order order) throws InventoryApiUnavailableException {
        Instant now = clock.instant();
        reservations.release(order.getStoreMerchantId(), order.getOrderRef().value());
        // false means inventory had already let the stock go (expired or never reserved); either way it is not held.
        return apply(order.getId(), o -> o.released(now));
    }

    private Order apply(Long orderId, Consumer<Order> change) {
        try {
            return transactions.execute(status -> {
                Order fresh = load(orderId);
                change.accept(fresh);
                return orders.saveAndFlush(fresh);
            });
        } catch (ObjectOptimisticLockingFailureException | IllegalStateException e) {
            // Another replica or job applied this step between our read and our write. Their result stands.
            log.info("Order {} was advanced concurrently ({}); re-reading", orderId, e.getMessage());
            return load(orderId);
        }
    }

    private Order load(Long orderId) {
        return orders.findWithLinesById(orderId)
                .orElseThrow(() -> new IllegalStateException(String.format("order %d vanished", orderId)));
    }

    private static Instant earliest(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        return b == null || a.isBefore(b) ? a : b;
    }
}

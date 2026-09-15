package com.asrevo.cvhome.checkout.services.order;

import java.time.Clock;
import java.time.Instant;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.asrevo.cvhome.checkout.config.CheckoutProperties;
import com.asrevo.cvhome.checkout.domain.OrderRef;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.signal.PaymentSignal;
import com.asrevo.cvhome.checkout.model.signal.ReservationExpiredSignal;
import com.asrevo.cvhome.checkout.model.signal.SignalOutcome;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.BaseException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSignalServiceImpl implements OrderSignalService {

    private final OrderRepository orders;

    private final OrderStepRunner steps;

    private final CheckoutProperties properties;

    private final Clock clock;

    private final TransactionTemplate transactions;

    @Override
    public SignalOutcome paymentSignal(StoreMerchantId store, String orderRef, PaymentSignal signal)
            throws OrderNotFoundException {
        Applied applied = applyPayment(store, orderRef, signal);
        finishPendingAction(applied.orderId());
        return applied.outcome();
    }

    @Override
    public SignalOutcome reservationExpired(StoreMerchantId store, String orderRef, ReservationExpiredSignal signal)
            throws OrderNotFoundException {
        Applied applied = applyExpiry(store, orderRef, signal);
        finishPendingAction(applied.orderId());
        return applied.outcome();
    }

    private Applied applyPayment(StoreMerchantId store, String orderRef, PaymentSignal signal)
            throws OrderNotFoundException {
        return apply(store, orderRef, order -> {
            Instant now = clock.instant();
            SignalOutcome outcome = order.applyPaymentSignal(signal.status(), signal.transactionRef(), now);
            if (order.isPaymentInFlight()) {
                order.extendExpiry(now.plus(properties.getPlacement().getProcessingGrace()), now);
            }
            return outcome;
        });
    }

    private Applied applyExpiry(StoreMerchantId store, String orderRef, ReservationExpiredSignal signal)
            throws OrderNotFoundException {
        return apply(store, orderRef, order -> order.applyReservationExpired(signal.reservationRef(), clock.instant()));
    }

    /**
     * Loads the order, applies the signal and saves it in one transaction, which ends before the pending action runs.
     *
     * <p>
     * These were {@code @Transactional} methods called from this class, which a Spring proxy never sees: they ran
     * with no transaction at all, and only open-in-view's request-long session let the order's events load. The
     * load, the duplicate check and the write are one unit, so they are one transaction here.
     * </p>
     */
    private Applied apply(StoreMerchantId store, String orderRef, Function<Order, SignalOutcome> signal)
            throws OrderNotFoundException {
        Applied applied = transactions.execute(status -> orders
                .findByStoreMerchantIdAndOrderRef(store, OrderRef.of(orderRef))
                .map(order -> {
                    SignalOutcome outcome = signal.apply(order);
                    orders.saveAndFlush(order);
                    return new Applied(order.getId(), outcome);
                })
                .orElse(null));
        if (applied == null) {
            throw OrderNotFoundException.ofRef(orderRef, store.getId());
        }
        return applied;
    }

    /**
     * A signal can leave a COMMIT or RELEASE behind. Best effort now; the recovery job owns it if this fails.
     */
    private void finishPendingAction(Long orderId) {
        try {
            steps.runUntilSettled(orderId, 1);
        } catch (BaseException e) {
            log.warn("Order {}: pending action after signal left to recovery: {}", orderId, e.getMessage());
        }
    }

    record Applied(Long orderId, SignalOutcome outcome) {
    }
}

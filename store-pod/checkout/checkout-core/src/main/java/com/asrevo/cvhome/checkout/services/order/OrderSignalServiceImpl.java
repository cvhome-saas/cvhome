package com.asrevo.cvhome.checkout.services.order;

import java.time.Clock;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional(rollbackFor = Exception.class)
    Applied applyPayment(StoreMerchantId store, String orderRef, PaymentSignal signal) throws OrderNotFoundException {
        Order order = load(store, orderRef);
        Instant now = clock.instant();
        SignalOutcome outcome = order.applyPaymentSignal(signal.status(), signal.transactionRef(), now);
        if (order.isPaymentInFlight()) {
            order.extendExpiry(now.plus(properties.getPlacement().getProcessingGrace()), now);
        }
        orders.saveAndFlush(order);
        return new Applied(order.getId(), outcome);
    }

    @Transactional(rollbackFor = Exception.class)
    Applied applyExpiry(StoreMerchantId store, String orderRef, ReservationExpiredSignal signal)
            throws OrderNotFoundException {
        Order order = load(store, orderRef);
        SignalOutcome outcome = order.applyReservationExpired(signal.reservationRef(), clock.instant());
        orders.saveAndFlush(order);
        return new Applied(order.getId(), outcome);
    }

    private Order load(StoreMerchantId store, String orderRef) throws OrderNotFoundException {
        return orders.findByStoreMerchantIdAndOrderRef(store, OrderRef.of(orderRef))
                .orElseThrow(() -> OrderNotFoundException.ofRef(orderRef, store.getId()));
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

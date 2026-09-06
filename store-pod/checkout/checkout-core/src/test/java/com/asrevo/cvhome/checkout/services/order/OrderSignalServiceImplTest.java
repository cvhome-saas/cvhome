package com.asrevo.cvhome.checkout.services.order;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.checkout.config.CheckoutProperties;
import com.asrevo.cvhome.checkout.domain.OrderRef;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.order.OrderEventOutcome;
import com.asrevo.cvhome.checkout.model.order.PendingAction;
import com.asrevo.cvhome.checkout.model.signal.PaymentSignal;
import com.asrevo.cvhome.checkout.model.signal.ReservationExpiredSignal;
import com.asrevo.cvhome.checkout.model.signal.SignalOutcome;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A signal is applied to the aggregate, saved, and whatever step it left behind is attempted once — a failed attempt is
 * the recovery job's problem, never the caller's.
 */
@ExtendWith(MockitoExtension.class)
class OrderSignalServiceImplTest {

    private static final String TX_1 = "tx-1";

    private static final String REF = "11111111-1111-1111-1111-111111111111";

    @Mock
    private OrderRepository orders;

    @Mock
    private OrderStepRunner steps;

    private final CheckoutProperties properties = new CheckoutProperties();

    private OrderSignalServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OrderSignalServiceImpl(orders, steps, properties, Clock.fixed(Orders.T2, ZoneOffset.UTC));
    }

    private Order given(Order order) {
        when(orders.findByStoreMerchantIdAndOrderRef(Orders.STORE, OrderRef.of(REF))).thenReturn(Optional.of(order));
        return order;
    }

    @Test
    void aPaidSignalConfirmsSavesAndRunsTheCommit() throws Exception {
        Order order = given(Orders.awaitingPayment(PaymentType.STRIPE));

        SignalOutcome outcome = service.paymentSignal(Orders.STORE, REF, new PaymentSignal(PaymentStatus.PAID, TX_1));

        assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
        assertThat(outcome.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getPendingAction()).isEqualTo(PendingAction.COMMIT);
        verify(orders).saveAndFlush(order);
        verify(steps).runUntilSettled(100L, 1);
    }

    @Test
    void aProcessingSignalExtendsTheExpiryByTheGrace() throws Exception {
        Order order = given(Orders.awaitingPayment(PaymentType.STRIPE));

        service.paymentSignal(Orders.STORE, REF, new PaymentSignal(PaymentStatus.PROCESSING, TX_1));

        assertThat(order.getExpiresAt()).isEqualTo(Orders.T2.plus(properties.getPlacement().getProcessingGrace()));
    }

    @Test
    void aDuplicateIsAnsweredWithoutTouchingAnything() throws Exception {
        Order order = given(Orders.paid(PaymentType.STRIPE));

        SignalOutcome outcome = service.paymentSignal(Orders.STORE, REF, new PaymentSignal(PaymentStatus.PAID, TX_1));

        assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.DUPLICATE);
        assertThat(order.getEvents().getLast().getOutcome()).isEqualTo(OrderEventOutcome.DUPLICATE);
        verify(orders).saveAndFlush(order);
    }

    @Test
    void anExpiredReservationCancelsTheUnpaidOrder() throws Exception {
        Order order = given(Orders.awaitingPayment(PaymentType.MANUAL_TRANSFER));

        SignalOutcome outcome = service.reservationExpired(Orders.STORE, REF, new ReservationExpiredSignal(REF));

        assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(steps).runUntilSettled(100L, 1);
    }

    @Test
    void aStepThatCannotRunNowIsLeftToRecovery() throws Exception {
        given(Orders.awaitingPayment(PaymentType.STRIPE));
        when(steps.runUntilSettled(100L, 1)).thenThrow(InventoryApiUnavailableException.from(new RemoteErrorContext(
                null, null, java.util.Map.of(), java.util.List.of(), "inventory", 0, null, new RuntimeException())));

        SignalOutcome outcome = service.paymentSignal(Orders.STORE, REF, new PaymentSignal(PaymentStatus.PAID, TX_1));

        assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
    }

    @Test
    void anUnknownRefIs404AndNothingRuns() throws Exception {
        when(orders.findByStoreMerchantIdAndOrderRef(any(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.paymentSignal(Orders.STORE, REF, new PaymentSignal(PaymentStatus.PAID, "x")))
                .isInstanceOf(OrderNotFoundException.class);
        assertThatThrownBy(() -> service.reservationExpired(Orders.STORE, REF, new ReservationExpiredSignal(REF)))
                .isInstanceOf(OrderNotFoundException.class);
        verify(steps, never()).runUntilSettled(any(), org.mockito.ArgumentMatchers.anyInt());
    }
}

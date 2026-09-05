package com.asrevo.cvhome.checkout.services.jobs;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.asrevo.cvhome.checkout.config.CheckoutProperties;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.checkout.model.order.PendingAction;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.checkout.services.order.OrderStepRunner;
import com.asrevo.cvhome.checkout.services.order.TestTransactions;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.payment.model.payment.PaymentResponse;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unpaid orders past their window are closed — after asking payment once for a card order, so a late webhook rescues
 * the order instead of losing it.
 */
@ExtendWith(MockitoExtension.class)
class OrderExpiryJobTest {

    private static final String REF = "11111111-1111-1111-1111-111111111111";

    @Mock
    private OrderRepository orders;

    @Mock
    private OrderStepRunner steps;

    @Mock
    private ExternalPaymentGatewayService payments;

    private final CheckoutProperties properties = new CheckoutProperties();

    private OrderExpiryJob job;

    @BeforeEach
    void setUp() {
        job = new OrderExpiryJob(orders, steps, payments, properties, TestTransactions.inline(),
                Clock.fixed(Orders.T2, ZoneOffset.UTC));
    }

    private static RemoteErrorContext outage() {
        return new RemoteErrorContext(null, null, java.util.Map.of(), java.util.List.of(), "payment", 0, null,
                new RuntimeException());
    }

    @Test
    void anOverdueManualTransferIsExpiredWithoutAskingPaymentAndTheStockReleased() throws Exception {
        Order order = Orders.awaitingPayment(PaymentType.MANUAL_TRANSFER);
        when(orders.findExpiredIds(eq(Orders.T2), any(Pageable.class))).thenReturn(List.of(100L));
        when(orders.findById(100L)).thenReturn(Optional.of(order));

        job.expire();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.EXPIRED);
        assertThat(order.getPendingAction()).isEqualTo(PendingAction.RELEASE);
        verify(payments, never()).status(any(), any());
        verify(steps).runUntilSettled(100L, 1);
    }

    @Test
    void anOverdueCardOrderPaymentSaysUnpaidIsExpired() throws Exception {
        Order order = Orders.awaitingPayment(PaymentType.STRIPE);
        when(orders.findById(100L)).thenReturn(Optional.of(order));
        when(payments.status(Orders.STORE, REF)).thenReturn(PaymentResponse.pending());

        assertThat(job.expireOne(100L)).isTrue();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void aLatePaymentRescuesTheOrderInsteadOfExpiringIt() throws Exception {
        Order order = Orders.awaitingPayment(PaymentType.STRIPE);
        when(orders.findById(100L)).thenReturn(Optional.of(order));
        when(payments.status(Orders.STORE, REF))
                .thenReturn(PaymentResponse.builder().gatewayRef("tx-9").status(PaymentStatus.PAID).build());

        assertThat(job.expireOne(100L)).isFalse();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getPendingAction()).isEqualTo(PendingAction.COMMIT);
        verify(steps).runUntilSettled(100L, 1);
    }

    @Test
    void anUnreachablePaymentDecidesNothingThisPass() throws Exception {
        Order order = Orders.awaitingPayment(PaymentType.PAYPAL);
        when(orders.findById(100L)).thenReturn(Optional.of(order));
        when(payments.status(Orders.STORE, REF)).thenThrow(PaymentApiUnavailableException.from(outage()));

        assertThat(job.expireOne(100L)).isFalse();

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        verify(steps, never()).runUntilSettled(any(), anyInt());
    }

    @Test
    void aNullOrEmptyPaymentAnswerCountsAsUnpaid() throws Exception {
        Order order = Orders.awaitingPayment(PaymentType.STRIPE);
        when(orders.findById(100L)).thenReturn(Optional.of(order));
        when(payments.status(Orders.STORE, REF)).thenReturn(null);

        assertThat(job.expireOne(100L)).isTrue();
    }

    @Test
    void anOrderNoLongerWaitingIsSkipped() throws Exception {
        when(orders.findById(1L)).thenReturn(Optional.of(Orders.paid(PaymentType.STRIPE)));
        when(orders.findById(2L)).thenReturn(Optional.empty());

        assertThat(job.expireOne(1L)).isFalse();
        assertThat(job.expireOne(2L)).isFalse();
        verify(steps, never()).runUntilSettled(any(), anyInt());
    }

    @Test
    void anOrderChangedUnderTheJobIsNotExpired() throws Exception {
        Order order = Orders.awaitingPayment(PaymentType.MANUAL_TRANSFER);
        when(orders.findById(100L)).thenReturn(Optional.of(order));
        when(orders.saveAndFlush(order)).thenThrow(new ObjectOptimisticLockingFailureException(Order.class, 100L));

        assertThat(job.expireOne(100L)).isFalse();
        verify(steps, never()).runUntilSettled(any(), anyInt());
    }

    @Test
    void aReleaseThatCannotRunNowIsLeftToRecovery() throws Exception {
        Order order = Orders.awaitingPayment(PaymentType.MANUAL_TRANSFER);
        when(orders.findById(100L)).thenReturn(Optional.of(order));
        when(steps.runUntilSettled(100L, 1)).thenThrow(InventoryApiUnavailableException.from(outage()));

        assertThat(job.expireOne(100L)).isTrue();
        assertThat(order.getPendingAction()).isEqualTo(PendingAction.RELEASE);
    }
}

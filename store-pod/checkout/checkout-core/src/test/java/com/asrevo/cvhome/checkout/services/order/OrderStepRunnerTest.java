package com.asrevo.cvhome.checkout.services.order;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.asrevo.cvhome.checkout.config.CheckoutProperties;
import com.asrevo.cvhome.checkout.entity.Cart;
import com.asrevo.cvhome.checkout.entity.Order;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.checkout.model.cart.CartStatus;
import com.asrevo.cvhome.checkout.model.order.PendingAction;
import com.asrevo.cvhome.checkout.repositories.CartRepository;
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReleaseResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.inventory.services.ExternalProductReservationService;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.payment.api.errors.PaymentGatewayRejectedException;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateResult;
import com.asrevo.cvhome.payment.model.payment.PaymentInitiateStatus;
import com.asrevo.cvhome.payment.model.payment.PaymentRequest;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * One remote call per pending action, applied to a freshly loaded order; refusals close the order and are rethrown,
 * outages leave the action for the recovery job.
 */
@ExtendWith(MockitoExtension.class)
class OrderStepRunnerTest {

    private static final String ORDERID_100 = "?orderId=100";

    private static final String JOIN = "%s%s";

    private static final String HTTPS_PAY = "https://pay";

    private static final String REMOTE = "remote";

    private static final String TX_9 = "tx-9";

    private static final Instant NOW = Orders.T2;

    private static final String REF = "11111111-1111-1111-1111-111111111111";

    @Mock
    private OrderRepository orders;

    @Mock
    private CartRepository carts;

    @Mock
    private ExternalProductReservationService reservations;

    @Mock
    private ExternalPaymentGatewayService payments;

    private final CheckoutProperties properties = new CheckoutProperties();

    private OrderStepRunner runner;

    private Order order;

    @BeforeEach
    void setUp() {
        runner = new OrderStepRunner(orders, carts, reservations, payments, properties, Clock.fixed(NOW, ZoneOffset.UTC),
                TestTransactions.inline());
        lenient().when(orders.saveAndFlush(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private void given(Order state) {
        order = state;
        when(orders.findWithLinesById(100L)).thenReturn(Optional.of(order));
    }

    private static RemoteErrorContext refusal(String code) {
        return new RemoteErrorContext(code, "refused", java.util.Map.of(), java.util.List.of(), REMOTE, 422, null, null);
    }

    private static RemoteErrorContext outage() {
        return new RemoteErrorContext(null, null, java.util.Map.of(), java.util.List.of(), REMOTE, 0, null,
                new RuntimeException("connection refused"));
    }

    @Test
    void aStripePlacementReservesThenInitiatesAndStopsWaitingForTheShopper() throws Exception {
        given(Orders.placed(PaymentType.STRIPE));
        when(reservations.reserve(eq(Orders.STORE), eq(REF), any()))
                .thenReturn(new ProductReservationReserveResult(true, 9L, NOW.plusSeconds(7200)));
        when(payments.initiatePayment(eq(Orders.STORE), any()))
                .thenReturn(new PaymentInitiateResult(PaymentInitiateStatus.PENDING, HTTPS_PAY, "ext", TX_9));

        Order result = runner.runUntilSettled(100L, 3);

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(result.getPendingAction()).isEqualTo(PendingAction.NONE);
        assertThat(result.getRedirectUrl()).isEqualTo(HTTPS_PAY);
        assertThat(result.getPaymentTransactionRef()).isEqualTo(TX_9);
        assertThat(result.getExpiresAt()).as("the shorter of our window and inventory's hold")
                .isEqualTo(NOW.plus(properties.getPlacement().getStripe()));

        ArgumentCaptor<ProductReservationList> lines = ArgumentCaptor.forClass(ProductReservationList.class);
        verify(reservations).reserve(eq(Orders.STORE), eq(REF), lines.capture());
        assertThat(lines.getValue().entries()).singleElement()
                .satisfies(e -> assertThat(e.reserveQty()).isEqualTo(2));
        assertThat(lines.getValue().expireAt()).isEqualTo(NOW.plus(properties.getPlacement().getStripe()));

        ArgumentCaptor<PaymentRequest> request = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(payments).initiatePayment(eq(Orders.STORE), request.capture());
        assertThat(request.getValue().ref()).isEqualTo(REF);
        assertThat(request.getValue().amount()).isEqualByComparingTo("20.00");
        assertThat(request.getValue().successUrl()).isEqualTo(String.format(JOIN, Orders.SUCCESS_URL, ORDERID_100));
        assertThat(request.getValue().cancelUrl()).isEqualTo(String.format(JOIN, Orders.CANCEL_URL, ORDERID_100));
        verify(reservations, never()).commit(any(), any());
    }

    @Test
    void aCodPlacementReservesInitiatesAndCommitsInOneRun() throws Exception {
        given(Orders.placed(PaymentType.COD));
        when(reservations.reserve(eq(Orders.STORE), eq(REF), any()))
                .thenReturn(new ProductReservationReserveResult(true, 9L, NOW.plusSeconds(2700)));
        when(payments.initiatePayment(eq(Orders.STORE), any()))
                .thenReturn(new PaymentInitiateResult(PaymentInitiateStatus.PENDING, null, null, "tx-cod"));
        when(reservations.commit(Orders.STORE, REF)).thenReturn(new ProductReservationCommitResult(true, 9L, null));

        Order result = runner.runUntilSettled(100L, 3);

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(result.getInventoryStatus()).isEqualTo(InventoryStatus.COMMITTED);
        assertThat(result.getPendingAction()).isEqualTo(PendingAction.NONE);
        assertThat(result.getExpiresAt()).isNull();
        ArgumentCaptor<ProductReservationList> lines = ArgumentCaptor.forClass(ProductReservationList.class);
        verify(reservations).reserve(eq(Orders.STORE), eq(REF), lines.capture());
        assertThat(lines.getValue().expireAt()).as("COD asks inventory for its default hold").isNull();
        ArgumentCaptor<PaymentRequest> request = ArgumentCaptor.forClass(PaymentRequest.class);
        verify(payments).initiatePayment(eq(Orders.STORE), request.capture());
        assertThat(request.getValue().expireAt())
                .isEqualTo(NOW.plus(properties.getPlacement().getCodPaymentExpiry()));
    }

    @Test
    void maxStepsBoundsTheRun() throws Exception {
        given(Orders.placed(PaymentType.STRIPE));
        when(reservations.reserve(eq(Orders.STORE), eq(REF), any()))
                .thenReturn(new ProductReservationReserveResult(true, 9L, NOW.plusSeconds(7200)));

        Order result = runner.runUntilSettled(100L, 1);

        assertThat(result.getPendingAction()).isEqualTo(PendingAction.INITIATE_PAYMENT);
        verify(payments, never()).initiatePayment(any(), any());
    }

    @Test
    void aPaidAtInitiateAnswerConfirmsAndCommits() throws Exception {
        given(Orders.reserved(PaymentType.STRIPE));
        when(payments.initiatePayment(eq(Orders.STORE), any()))
                .thenReturn(new PaymentInitiateResult(PaymentInitiateStatus.PAID, null, null, TX_9));
        when(reservations.commit(Orders.STORE, REF)).thenReturn(new ProductReservationCommitResult(true, 9L, null));

        Order result = runner.runUntilSettled(100L, 3);

        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(result.getInventoryStatus()).isEqualTo(InventoryStatus.COMMITTED);
    }

    @Test
    void aFailedInitiateAnswerCancelsAndReleases() throws Exception {
        given(Orders.reserved(PaymentType.STRIPE));
        when(payments.initiatePayment(eq(Orders.STORE), any())).thenReturn(PaymentInitiateResult.failed(TX_9));
        when(reservations.release(Orders.STORE, REF)).thenReturn(new ProductReservationReleaseResult(true, 9L, null));

        Order result = runner.runUntilSettled(100L, 3);

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(result.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getInventoryStatus()).isEqualTo(InventoryStatus.RELEASED);
    }

    @Test
    void aRefusedReservationCancelsTheOrderReopensTheCartAndIsRethrown() throws Exception {
        given(Orders.placed(PaymentType.STRIPE));
        Cart cart = new Cart(Orders.STORE, order.getCartCode(), null);
        cart.convertedInto(100L);
        when(carts.findByStoreMerchantIdAndCode(Orders.STORE, order.getCartCode())).thenReturn(Optional.of(cart));
        when(reservations.reserve(eq(Orders.STORE), eq(REF), any()))
                .thenThrow(ProductReservationRejectedException.from(refusal("INVENTORY.RESERVATION.INSUFFICIENT_INVENTORY")));

        assertThatThrownBy(() -> runner.runUntilSettled(100L, 3))
                .isInstanceOf(ProductReservationRejectedException.class);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getInventoryStatus()).isEqualTo(InventoryStatus.RESERVATION_FAILED);
        assertThat(cart.getStatus()).isEqualTo(CartStatus.ACTIVE);
        assertThat(cart.getOrderId()).isNull();
        verify(payments, never()).initiatePayment(any(), any());
    }

    @Test
    void anUnreachableInventoryLeavesTheReservationOwedAndIsRethrown() throws Exception {
        given(Orders.placed(PaymentType.STRIPE));
        when(reservations.reserve(eq(Orders.STORE), eq(REF), any()))
                .thenThrow(InventoryApiUnavailableException.from(outage()));

        assertThatThrownBy(() -> runner.runUntilSettled(100L, 3))
                .isInstanceOf(InventoryApiUnavailableException.class);

        assertThat(order.getPendingAction()).isEqualTo(PendingAction.RESERVE);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
        verify(orders, never()).saveAndFlush(any());
    }

    @Test
    void aRefusedPaymentCancelsReleasesTheStockAndIsRethrownAfterwards() throws Exception {
        given(Orders.reserved(PaymentType.STRIPE));
        when(payments.initiatePayment(eq(Orders.STORE), any()))
                .thenThrow(PaymentGatewayRejectedException.from(refusal("PAYMENT.INITIATE.REJECTED")));
        when(reservations.release(Orders.STORE, REF)).thenReturn(new ProductReservationReleaseResult(true, 9L, null));

        assertThatThrownBy(() -> runner.runUntilSettled(100L, 3)).isInstanceOf(PaymentGatewayRejectedException.class);

        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(order.getInventoryStatus()).as("released in the same run").isEqualTo(InventoryStatus.RELEASED);
        assertThat(order.getPendingAction()).isEqualTo(PendingAction.NONE);
    }

    @Test
    void anUnreachablePaymentLeavesTheInitiateOwedAndIsRethrown() throws Exception {
        given(Orders.reserved(PaymentType.STRIPE));
        when(payments.initiatePayment(eq(Orders.STORE), any())).thenThrow(PaymentApiUnavailableException.from(outage()));

        assertThatThrownBy(() -> runner.runUntilSettled(100L, 3)).isInstanceOf(PaymentApiUnavailableException.class);

        assertThat(order.getPendingAction()).isEqualTo(PendingAction.INITIATE_PAYMENT);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void aCommitRefusedByInventoryFlagsTheOrder() throws Exception {
        Order paid = Orders.awaitingPayment(PaymentType.STRIPE);
        paid.applyPaymentSignal(PaymentStatus.PAID, Orders.TX, Orders.T1);
        given(paid);
        when(reservations.commit(Orders.STORE, REF)).thenReturn(new ProductReservationCommitResult(false, null, null));

        Order result = runner.runUntilSettled(100L, 1);

        assertThat(result.getInventoryStatus()).isEqualTo(InventoryStatus.RESERVATION_FAILED);
        assertThat(result.isNeedsAttention()).isTrue();
        assertThat(result.getPendingAction()).isEqualTo(PendingAction.NONE);
    }

    @Test
    void aReleaseAnsweredFalseStillCountsAsReleased() throws Exception {
        Order cancelled = Orders.awaitingPayment(PaymentType.STRIPE);
        cancelled.applyPaymentSignal(PaymentStatus.FAILED, Orders.TX, Orders.T1);
        given(cancelled);
        when(reservations.release(Orders.STORE, REF)).thenReturn(new ProductReservationReleaseResult(false, null, null));

        Order result = runner.runUntilSettled(100L, 1);

        assertThat(result.getInventoryStatus()).isEqualTo(InventoryStatus.RELEASED);
        assertThat(result.getPendingAction()).isEqualTo(PendingAction.NONE);
    }

    @Test
    void anOrderOwingNothingIsReturnedUntouched() throws Exception {
        given(Orders.paid(PaymentType.STRIPE));

        Order result = runner.runUntilSettled(100L, 3);

        assertThat(result).isSameAs(order);
        verify(orders, never()).saveAndFlush(any());
    }

    @Test
    void losingTheOptimisticLockMeansSomeoneElseAppliedTheStepAndTheFreshOrderIsReturned() throws Exception {
        given(Orders.placed(PaymentType.STRIPE));
        when(reservations.reserve(eq(Orders.STORE), eq(REF), any()))
                .thenReturn(new ProductReservationReserveResult(true, 9L, NOW.plusSeconds(7200)));
        when(orders.saveAndFlush(any(Order.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException(Order.class, 100L));

        Order result = runner.runUntilSettled(100L, 1);

        assertThat(result).isNotNull();
        verify(orders, org.mockito.Mockito.atLeast(2)).findWithLinesById(100L);
    }

    @Test
    void aStepAlreadyAppliedByAnotherReplicaIsSkippedNotFailed() throws Exception {
        // The order says RESERVE, but by the time we apply, someone moved it on: reserved() throws IllegalState.
        Order fresh = Orders.reserved(PaymentType.STRIPE);
        Order stale = Orders.placed(PaymentType.STRIPE);
        when(orders.findWithLinesById(100L)).thenReturn(Optional.of(stale), Optional.of(fresh), Optional.of(fresh));
        when(reservations.reserve(eq(Orders.STORE), eq(REF), any()))
                .thenReturn(new ProductReservationReserveResult(true, 9L, NOW.plusSeconds(7200)));

        Order result = runner.runUntilSettled(100L, 1);

        assertThat(result.getPendingAction()).isEqualTo(PendingAction.INITIATE_PAYMENT);
    }

    @Test
    void aVanishedOrderIsAProgrammingError() {
        when(orders.findWithLinesById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> runner.runUntilSettled(404L, 1)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("vanished");
    }

}

package com.asrevo.cvhome.checkout.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import com.asrevo.cvhome.checkout.errors.IllegalOrderTransitionException;
import com.asrevo.cvhome.checkout.model.order.OrderEventOutcome;
import com.asrevo.cvhome.checkout.model.order.OrderEventSource;
import com.asrevo.cvhome.checkout.model.order.OrderEventType;
import com.asrevo.cvhome.checkout.model.order.PendingAction;
import com.asrevo.cvhome.checkout.model.signal.SignalOutcome;
import com.asrevo.cvhome.store.core.entity.common.InventoryStatus;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static com.asrevo.cvhome.checkout.entity.Orders.T0;
import static com.asrevo.cvhome.checkout.entity.Orders.T1;
import static com.asrevo.cvhome.checkout.entity.Orders.T2;
import static com.asrevo.cvhome.checkout.entity.Orders.TX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The state machine, transition by transition: which states each method accepts, what the three statuses and the
 * pending action become, and that every application appends exactly one ledger row.
 */
class OrderTransitionTest {

    private static final String THIRTY = "30.00";

    private static final String REDIRECT = "https://pay";

    private static final String DECLINED = "declined";

    private static final String PAID_REF = "tx-1:PAID";

    private static final String LATE_TX = "tx-late";

    private static final String REFUND = "refund";

    private static final String STAFF = "staff";

    private static final String RES_REF = "res-1";

    private static final String NOTE = "note";

    private static final String STAFF_AT_STORE = "staff@store";

    private static final String S = "s";

    private static void assertState(Order order, OrderStatus status, PaymentStatus payment, InventoryStatus inventory,
                                    PendingAction pending) {
        assertThat(order.getOrderStatus()).isEqualTo(status);
        assertThat(order.getPaymentStatus()).isEqualTo(payment);
        assertThat(order.getInventoryStatus()).isEqualTo(inventory);
        assertThat(order.getPendingAction()).isEqualTo(pending);
    }

    private static OrderEvent lastEvent(Order order) {
        return order.getEvents().getLast();
    }

    @Nested
    class Placement {

        @Test
        void placingOpensTheOrderOwingAReservationWithOneEventAndOneHistoryRow() {
            Order order = Orders.placed(PaymentType.STRIPE);

            assertState(order, OrderStatus.CREATED, PaymentStatus.PENDING, InventoryStatus.NOT_REQUESTED,
                    PendingAction.RESERVE);
            assertThat(order.getDatePurchased()).isEqualTo(T0);
            assertThat(order.getCustomerId()).isEqualTo(7L);
            assertThat(order.getCustomerEmail()).isEqualTo("shopper@example.com");
            assertThat(order.getEvents()).singleElement().satisfies(event -> {
                assertThat(event.getEventType()).isEqualTo(OrderEventType.PLACED);
                assertThat(event.getSource()).isEqualTo(OrderEventSource.PLACEMENT);
                assertThat(event.getOutcome()).isEqualTo(OrderEventOutcome.APPLIED);
                assertThat(event.getPendingActionAfter()).isEqualTo(PendingAction.RESERVE);
            });
            assertThat(order.getHistory()).singleElement().extracting(OrderStatusHistory::getStatus)
                    .isEqualTo(OrderStatus.CREATED);
        }

        @Test
        void totalsAreTheSumOfTheLinesAsSubtotalAndTotal() {
            Order order = Orders.placed(PaymentType.STRIPE);
            order.addLine("SKU-2", 2L, "Second", new BigDecimal("2.50"), 4, "img");
            order.computeTotals();

            assertThat(order.getSubtotal()).isEqualByComparingTo(THIRTY);
            assertThat(order.getTotal()).isEqualByComparingTo(THIRTY);
            assertThat(order.getTotals()).extracting(OrderTotal::getCode).containsExactly("SUBTOTAL", "TOTAL");
            assertThat(order.getLines()).hasSize(2);
            assertThat(order.getLines().getFirst().getLineTotal()).isEqualByComparingTo("20.00");
            assertThat(order.getLines().getFirst().getOptions()).singleElement()
                    .satisfies(option -> assertThat(option.getValueName()).isEqualTo("L"));
            assertThat(order.getLines().get(1).getSortOrder()).isEqualTo(1);
        }

        @Test
        void reservingMovesOnToPaymentAndKeepsBothExpiries() {
            Order order = Orders.placed(PaymentType.STRIPE);

            order.reserved(5L, T0.plusSeconds(3600), T0.plusSeconds(1800), T1);

            assertState(order, OrderStatus.CREATED, PaymentStatus.PENDING, InventoryStatus.RESERVED,
                    PendingAction.INITIATE_PAYMENT);
            assertThat(order.getReservationExpireAt()).isEqualTo(T0.plusSeconds(3600));
            assertThat(order.getExpiresAt()).isEqualTo(T0.plusSeconds(1800));
            assertThat(order.getPendingActionUpdatedAt()).isEqualTo(T1);
            assertThat(lastEvent(order).getEventType()).isEqualTo(OrderEventType.RESERVED);
            assertThat(lastEvent(order).getReason()).isEqualTo("reservation 5");
        }

        @Test
        void aRefusedReservationCancelsTheOrderWithNothingOwed() {
            Order order = Orders.placed(PaymentType.STRIPE);

            order.reservationRefused("no stock", T1);

            assertState(order, OrderStatus.CANCELLED, PaymentStatus.CANCELLED, InventoryStatus.RESERVATION_FAILED,
                    PendingAction.NONE);
            assertThat(order.isClosed()).isTrue();
            assertThat(order.getHistory()).extracting(OrderStatusHistory::getStatus)
                    .containsExactly(OrderStatus.CREATED, OrderStatus.CANCELLED);
        }

        @Test
        void stepsAreRefusedOutOfOrder() {
            Order order = Orders.placed(PaymentType.STRIPE);

            assertThatThrownBy(() -> order.paymentPending(TX, null, T1)).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("PAYMENT_INITIATED");
            assertThatThrownBy(() -> order.committed(T1)).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> order.released(T1)).isInstanceOf(IllegalStateException.class);
            order.reserved(5L, T1, T1, T1);
            assertThatThrownBy(() -> order.reserved(5L, T1, T1, T1)).isInstanceOf(IllegalStateException.class);
            assertThatThrownBy(() -> order.reservationRefused(REDIRECT, T1)).isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    class PaymentInitiated {

        @ParameterizedTest
        @EnumSource(value = PaymentType.class, names = {"STRIPE", "PAYPAL", "MANUAL_TRANSFER"})
        void pendingLeavesTheOrderWaitingForPaymentOwingNothing(PaymentType type) {
            Order order = Orders.reserved(type);

            order.paymentPending(TX, REDIRECT, T1);

            assertState(order, OrderStatus.PENDING_PAYMENT, PaymentStatus.PENDING, InventoryStatus.RESERVED,
                    PendingAction.NONE);
            assertThat(order.getRedirectUrl()).isEqualTo(REDIRECT);
            assertThat(order.getPaymentTransactionRef()).isEqualTo(TX);
            assertThat(order.getExpiresAt()).isNotNull();
            assertThat(order.getHistory().getLast().getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        }

        @Test
        void codConfirmsAtOnceAndOwesTheCommit() {
            Order order = Orders.reserved(PaymentType.COD);

            order.paymentPending(TX, null, T1);

            assertState(order, OrderStatus.CONFIRMED, PaymentStatus.PENDING, InventoryStatus.RESERVED,
                    PendingAction.COMMIT);
            assertThat(order.getExpiresAt()).isNull();
        }

        @Test
        void paidAtInitiateConfirmsAndOwesTheCommit() {
            Order order = Orders.reserved(PaymentType.STRIPE);

            order.paymentPaidAtInitiate(TX, T1);

            assertState(order, OrderStatus.CONFIRMED, PaymentStatus.PAID, InventoryStatus.RESERVED,
                    PendingAction.COMMIT);
            assertThat(order.getExpiresAt()).isNull();
        }

        @Test
        void failedAtInitiateCancelsAndOwesTheRelease() {
            Order order = Orders.reserved(PaymentType.STRIPE);

            order.paymentFailedAtInitiate(TX, DECLINED, T1);

            assertState(order, OrderStatus.CANCELLED, PaymentStatus.FAILED, InventoryStatus.RESERVED,
                    PendingAction.RELEASE);
            assertThat(lastEvent(order).getEventType()).isEqualTo(OrderEventType.PAYMENT_INITIATE_REJECTED);
            assertThat(lastEvent(order).getReason()).isEqualTo(DECLINED);
        }

        @Test
        void commitAndReleaseFinishTheirPendingAction() {
            Order paid = Orders.reserved(PaymentType.STRIPE);
            paid.paymentPaidAtInitiate(TX, T1);
            paid.committed(T2);
            assertState(paid, OrderStatus.CONFIRMED, PaymentStatus.PAID, InventoryStatus.COMMITTED, PendingAction.NONE);

            Order refused = Orders.reserved(PaymentType.STRIPE);
            refused.paymentPaidAtInitiate(TX, T1);
            refused.commitRefused(T2);
            assertState(refused, OrderStatus.CONFIRMED, PaymentStatus.PAID, InventoryStatus.RESERVATION_FAILED,
                    PendingAction.NONE);
            assertThat(refused.isNeedsAttention()).isTrue();
            assertThat(refused.getAttentionReason()).contains("committed");

            Order failed = Orders.reserved(PaymentType.STRIPE);
            failed.paymentFailedAtInitiate(TX, DECLINED, T1);
            failed.released(T2);
            assertState(failed, OrderStatus.CANCELLED, PaymentStatus.FAILED, InventoryStatus.RELEASED,
                    PendingAction.NONE);
        }
    }

    @Nested
    class PaymentSignals {

        @Test
        void paidWhileAwaitingPaymentConfirmsAndOwesTheCommit() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyPaymentSignal(PaymentStatus.PAID, TX, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
            assertThat(outcome.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
            assertState(order, OrderStatus.CONFIRMED, PaymentStatus.PAID, InventoryStatus.RESERVED,
                    PendingAction.COMMIT);
            assertThat(order.getExpiresAt()).isNull();
            assertThat(lastEvent(order).getSourceRef()).isEqualTo(PAID_REF);
        }

        @Test
        void theSameSignalTwiceIsADuplicateThatChangesNothingAndLeavesTwoRows() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);
            order.applyPaymentSignal(PaymentStatus.PAID, TX, T2);
            int events = order.getEvents().size();
            int history = order.getHistory().size();

            SignalOutcome outcome = order.applyPaymentSignal(PaymentStatus.PAID, TX, T2.plusSeconds(1));

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.DUPLICATE);
            assertThat(order.getEvents()).hasSize(events + 1);
            assertThat(order.getHistory()).hasSize(history);
            assertState(order, OrderStatus.CONFIRMED, PaymentStatus.PAID, InventoryStatus.RESERVED,
                    PendingAction.COMMIT);
        }

        @ParameterizedTest
        @EnumSource(value = PaymentStatus.class, names = {"FAILED", "CANCELLED", "REJECTED", "EXPIRED"})
        void aLostPaymentCancelsAndOwesTheRelease(PaymentStatus lost) {
            Order order = Orders.awaitingPayment(PaymentType.MANUAL_TRANSFER);

            SignalOutcome outcome = order.applyPaymentSignal(lost, TX, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
            assertState(order, OrderStatus.CANCELLED, lost, InventoryStatus.RESERVED, PendingAction.RELEASE);
            assertThat(order.getHistory().getLast().getComments()).contains(lost.name().toLowerCase());
        }

        @ParameterizedTest
        @EnumSource(value = PaymentStatus.class, names = {"FAILED", "CANCELLED", "REJECTED", "EXPIRED", "PROCESSING",
            "AUTHORIZED", "WAITING_VERIFICATION", "PENDING"})
        void anythingButPaidIsIgnoredOnceTheOrderIsPaid(PaymentStatus status) {
            Order order = Orders.paid(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyPaymentSignal(status, LATE_TX, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.IGNORED);
            assertState(order, OrderStatus.CONFIRMED, PaymentStatus.PAID, InventoryStatus.COMMITTED, PendingAction.NONE);
        }

        @Test
        void paidAgainIsIgnoredAsAlreadyPaid() {
            Order order = Orders.paid(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyPaymentSignal(PaymentStatus.PAID, "tx-other", T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.IGNORED);
            assertThat(lastEvent(order).getReason()).isEqualTo("already paid");
        }

        @Test
        void paidAfterCancellationIsRecordedAndFlaggedForARefund() {
            Order order = Orders.cancelled(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyPaymentSignal(PaymentStatus.PAID, LATE_TX, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
            assertState(order, OrderStatus.CANCELLED, PaymentStatus.PAID, InventoryStatus.RELEASED, PendingAction.NONE);
            assertThat(order.isNeedsAttention()).isTrue();
            assertThat(order.getAttentionReason()).contains(REFUND);
            assertThat(order.getEvents()).extracting(OrderEvent::getEventType)
                    .contains(OrderEventType.PAYMENT_AFTER_CLOSE);
        }

        @ParameterizedTest
        @EnumSource(value = PaymentStatus.class, names = {"FAILED", "CANCELLED", "REJECTED", "EXPIRED", "PROCESSING",
            "WAITING_VERIFICATION", "REFUNDED", "PENDING"})
        void aClosedOrderIgnoresEverythingButAPayment(PaymentStatus status) {
            Order order = Orders.cancelled(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyPaymentSignal(status, LATE_TX, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.IGNORED);
            assertState(order, OrderStatus.CANCELLED, PaymentStatus.FAILED, InventoryStatus.RELEASED,
                    PendingAction.NONE);
        }

        @ParameterizedTest
        @EnumSource(value = PaymentStatus.class, names = {"PROCESSING", "AUTHORIZED"})
        void aPaymentInFlightIsRecordedAndTheOrderKeepsWaiting(PaymentStatus status) {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyPaymentSignal(status, TX, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
            assertState(order, OrderStatus.PENDING_PAYMENT, status, InventoryStatus.RESERVED, PendingAction.NONE);
            assertThat(order.isPaymentInFlight()).isTrue();
        }

        @Test
        void waitingForVerificationDropsTheExpiryEntirely() {
            Order order = Orders.awaitingPayment(PaymentType.MANUAL_TRANSFER);
            assertThat(order.getExpiresAt()).isNotNull();

            order.applyPaymentSignal(PaymentStatus.WAITING_VERIFICATION, TX, T2);

            assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.WAITING_VERIFICATION);
            assertThat(order.getExpiresAt()).isNull();
            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        }

        @Test
        void pendingTeachesNothing() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyPaymentSignal(PaymentStatus.PENDING, TX, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.IGNORED);
        }

        @Test
        void aRefundOfAPaidOrderCancelsItAndFlagsTheCommittedStock() {
            Order order = Orders.paid(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyPaymentSignal(PaymentStatus.REFUNDED, TX, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
            assertState(order, OrderStatus.CANCELLED, PaymentStatus.REFUNDED, InventoryStatus.COMMITTED,
                    PendingAction.NONE);
            assertThat(order.isNeedsAttention()).isTrue();
        }

        @Test
        void aRefundAfterDeliveryIsAReturn() throws IllegalOrderTransitionException {
            Order order = Orders.paid(PaymentType.STRIPE);
            order.fulfil(OrderStatus.PROCESSING, null, STAFF, T2);
            order.fulfil(OrderStatus.SHIPPED, null, STAFF, T2);
            order.fulfil(OrderStatus.DELIVERING, null, STAFF, T2);
            order.fulfil(OrderStatus.DELIVERED, null, STAFF, T2);

            order.applyPaymentSignal(PaymentStatus.REFUNDED, TX, T2);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.RETURNED);
            assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.REFUNDED);
        }

        @Test
        void aRefundOfAPaidOrderStillReservedOwesTheRelease() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);
            order.applyPaymentSignal(PaymentStatus.PAID, TX, T1);

            order.applyPaymentSignal(PaymentStatus.REFUNDED, TX, T2);

            assertState(order, OrderStatus.CANCELLED, PaymentStatus.REFUNDED, InventoryStatus.RESERVED,
                    PendingAction.RELEASE);
        }

        @Test
        void aRefundOfAnUnpaidOrderIsIgnored() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyPaymentSignal(PaymentStatus.REFUNDED, TX, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.IGNORED);
            assertThat(lastEvent(order).getReason()).isEqualTo("not paid");
        }
    }

    @Nested
    class ReservationExpired {

        @Test
        void expiringAnUnpaidOrderCancelsItWithTheStockAlreadyReleased() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);

            SignalOutcome outcome = order.applyReservationExpired(RES_REF, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
            assertState(order, OrderStatus.CANCELLED, PaymentStatus.EXPIRED, InventoryStatus.RELEASED,
                    PendingAction.NONE);
            assertThat(order.getExpiresAt()).isNull();
        }

        @Test
        void theSameExpiryTwiceIsADuplicate() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);
            order.applyReservationExpired(RES_REF, T2);

            assertThat(order.applyReservationExpired(RES_REF, T2).outcome()).isEqualTo(OrderEventOutcome.DUPLICATE);
        }

        @Test
        void expiringAPaidOrderBeforeItsCommitFlagsIt() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);
            order.applyPaymentSignal(PaymentStatus.PAID, TX, T1);

            SignalOutcome outcome = order.applyReservationExpired(RES_REF, T2);

            assertThat(outcome.outcome()).isEqualTo(OrderEventOutcome.APPLIED);
            assertState(order, OrderStatus.CONFIRMED, PaymentStatus.PAID, InventoryStatus.RELEASED, PendingAction.NONE);
            assertThat(order.isNeedsAttention()).isTrue();
        }

        @Test
        void expiryIsIgnoredWhenNothingIsReservedOrTheOrderIsClosed() {
            assertThat(Orders.paid(PaymentType.STRIPE).applyReservationExpired(RES_REF, T2).outcome())
                    .isEqualTo(OrderEventOutcome.IGNORED);
            assertThat(Orders.placed(PaymentType.STRIPE).applyReservationExpired(RES_REF, T2).outcome())
                    .isEqualTo(OrderEventOutcome.IGNORED);

            Order shipped = Orders.awaitingPayment(PaymentType.STRIPE);
            shipped.applyPaymentSignal(PaymentStatus.PAID, TX, T1);
            shipped.committed(T1);
            shipped.setInventoryStatus(InventoryStatus.RESERVED); // a paid, committed order whose stock we pretend is held
            shipped.setPendingAction(PendingAction.NONE);
            assertThat(shipped.applyReservationExpired(RES_REF, T2).outcome()).isEqualTo(OrderEventOutcome.IGNORED);
            assertThat(lastEvent(shipped).getReason()).isEqualTo("order closed");
        }
    }

    @Nested
    class Jobs {

        @Test
        void expiringClosesTheOrderAndOwesTheRelease() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);

            order.expired(T2);

            assertState(order, OrderStatus.CANCELLED, PaymentStatus.EXPIRED, InventoryStatus.RESERVED,
                    PendingAction.RELEASE);
            assertThat(lastEvent(order).getSource()).isEqualTo(OrderEventSource.JOB);
        }

        @Test
        void expiringRefusesAnOrderThatIsNotWaitingForPayment() {
            Order order = Orders.paid(PaymentType.STRIPE);

            assertThatThrownBy(() -> order.expired(T2)).isInstanceOf(IllegalStateException.class);
        }

        @Test
        void extendingTheExpiryOnlyEverMovesItLater() {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);
            java.time.Instant original = order.getExpiresAt();

            order.extendExpiry(original.minusSeconds(60), T2);
            assertThat(order.getExpiresAt()).isEqualTo(original);

            order.extendExpiry(original.plusSeconds(60), T2);
            assertThat(order.getExpiresAt()).isEqualTo(original.plusSeconds(60));

            Order paid = Orders.paid(PaymentType.STRIPE);
            paid.extendExpiry(T2.plusSeconds(60), T2);
            assertThat(paid.getExpiresAt()).isNull();
        }

        @Test
        void recoveryCountsAttemptsAndEventuallyGivesUp() {
            Order order = Orders.placed(PaymentType.STRIPE);

            order.recoveryAttempted(T1);
            order.recoveryAttempted(T2);

            assertThat(order.getPendingActionAttempts()).isEqualTo(2);
            assertThat(order.getPendingActionUpdatedAt()).isEqualTo(T2);
            assertThat(order.getPendingAction()).isEqualTo(PendingAction.RESERVE);
            assertThat(order.isNeedsAttention()).isFalse();

            order.recoveryGaveUp(T2);

            assertThat(order.isNeedsAttention()).isTrue();
            assertThat(order.getAttentionReason()).contains("RESERVE").contains("2 attempts");
            assertThat(order.getPendingAction()).as("kept, so a person can see what was owed")
                    .isEqualTo(PendingAction.RESERVE);
        }
    }

    @Nested
    class Console {

        static Stream<Arguments> legalSteps() {
            return Stream.of(
                    Arguments.of(List.of(OrderStatus.PROCESSING)),
                    Arguments.of(List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED)),
                    Arguments.of(List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERING)),
                    Arguments.of(List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERING,
                            OrderStatus.DELIVERED)),
                    Arguments.of(List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERING,
                            OrderStatus.DELIVERED, OrderStatus.COMPLETED)),
                    Arguments.of(List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERING,
                            OrderStatus.DELIVERED, OrderStatus.RETURNED)),
                    Arguments.of(List.of(OrderStatus.PROCESSING, OrderStatus.SHIPPED, OrderStatus.DELIVERING,
                            OrderStatus.DELIVERED, OrderStatus.COMPLETED, OrderStatus.RETURNED)));
        }

        @ParameterizedTest
        @MethodSource("legalSteps")
        void fulfilmentWalksTheLegalPath(List<OrderStatus> path) throws IllegalOrderTransitionException {
            Order order = Orders.paid(PaymentType.STRIPE);
            int events = order.getEvents().size();

            for (OrderStatus next : path) {
                order.fulfil(next, NOTE, STAFF_AT_STORE, T2);
            }

            assertThat(order.getOrderStatus()).isEqualTo(path.getLast());
            assertThat(order.getEvents()).hasSize(events + path.size());
            assertThat(order.getHistory().getLast().getActor()).isEqualTo(STAFF_AT_STORE);
            assertThat(order.getHistory().getLast().getComments()).isEqualTo(NOTE);
        }

        static Stream<Arguments> illegalSteps() {
            return Stream.of(
                    Arguments.of(Orders.paid(PaymentType.STRIPE), OrderStatus.SHIPPED),
                    Arguments.of(Orders.paid(PaymentType.STRIPE), OrderStatus.DELIVERED),
                    Arguments.of(Orders.paid(PaymentType.STRIPE), OrderStatus.COMPLETED),
                    Arguments.of(Orders.paid(PaymentType.STRIPE), OrderStatus.RETURNED),
                    Arguments.of(Orders.paid(PaymentType.STRIPE), OrderStatus.CONFIRMED),
                    Arguments.of(Orders.paid(PaymentType.STRIPE), OrderStatus.CREATED),
                    Arguments.of(Orders.awaitingPayment(PaymentType.STRIPE), OrderStatus.PROCESSING),
                    Arguments.of(Orders.cancelled(PaymentType.STRIPE), OrderStatus.PROCESSING),
                    Arguments.of(Orders.cancelled(PaymentType.STRIPE), OrderStatus.CANCELLED),
                    Arguments.of(Orders.placed(PaymentType.STRIPE), OrderStatus.PROCESSING));
        }

        @ParameterizedTest
        @MethodSource("illegalSteps")
        void anIllegalStepIsRefusedWithoutTouchingTheOrder(Order order, OrderStatus next) {
            OrderStatus before = order.getOrderStatus();
            int events = order.getEvents().size();

            assertThatThrownBy(() -> order.fulfil(next, null, STAFF, T2))
                    .isInstanceOf(IllegalOrderTransitionException.class);

            assertThat(order.getOrderStatus()).isEqualTo(before);
            assertThat(order.getEvents()).hasSize(events);
        }

        @Test
        void deliveringACodOrderCollectsTheCash() throws IllegalOrderTransitionException {
            Order order = Orders.reserved(PaymentType.COD);
            order.paymentPending(TX, null, T1);
            order.committed(T1);
            order.fulfil(OrderStatus.PROCESSING, null, S, T2);
            order.fulfil(OrderStatus.SHIPPED, null, S, T2);
            order.fulfil(OrderStatus.DELIVERING, null, S, T2);
            assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);

            order.fulfil(OrderStatus.DELIVERED, null, S, T2);

            assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.PAID);
        }

        @Test
        void cancellingAnUnpaidOrderOwesTheReleaseAndMarksThePaymentCancelled()
                throws IllegalOrderTransitionException {
            Order order = Orders.awaitingPayment(PaymentType.MANUAL_TRANSFER);

            order.fulfil(OrderStatus.CANCELLED, "customer asked", STAFF, T2);

            assertState(order, OrderStatus.CANCELLED, PaymentStatus.CANCELLED, InventoryStatus.RESERVED,
                    PendingAction.RELEASE);
            assertThat(order.isNeedsAttention()).isFalse();
            assertThat(lastEvent(order).getEventType()).isEqualTo(OrderEventType.CANCELLED);
        }

        @Test
        void cancellingAPaidOrderKeepsThePaymentAndFlagsARefund() throws IllegalOrderTransitionException {
            Order order = Orders.paid(PaymentType.STRIPE);
            order.fulfil(OrderStatus.PROCESSING, null, S, T2);

            order.cancel("damaged", STAFF, T2);

            assertState(order, OrderStatus.CANCELLED, PaymentStatus.PAID, InventoryStatus.COMMITTED, PendingAction.NONE);
            assertThat(order.isNeedsAttention()).isTrue();
            assertThat(order.getAttentionReason()).contains(REFUND);
        }

        @Test
        void cancellingIsRefusedOnceShippedOrClosed() throws IllegalOrderTransitionException {
            Order shipped = Orders.paid(PaymentType.STRIPE);
            shipped.fulfil(OrderStatus.PROCESSING, null, S, T2);
            shipped.fulfil(OrderStatus.SHIPPED, null, S, T2);
            assertThatThrownBy(() -> shipped.cancel(null, S, T2)).isInstanceOf(IllegalOrderTransitionException.class);

            Order closed = Orders.cancelled(PaymentType.STRIPE);
            assertThatThrownBy(() -> closed.cancel(null, S, T2)).isInstanceOf(IllegalOrderTransitionException.class);
        }

        @Test
        void cancellingAFailedPaymentOrderKeepsTheFailedStatus() throws IllegalOrderTransitionException {
            Order order = Orders.awaitingPayment(PaymentType.STRIPE);
            order.applyPaymentSignal(PaymentStatus.PROCESSING, TX, T1);
            order.setPaymentStatus(PaymentStatus.FAILED);

            order.cancel(null, S, T2);

            assertThat(order.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        }
    }

    @Test
    void everyAppliedEventSnapshotsTheStateAfterIt() {
        Order order = Orders.paid(PaymentType.STRIPE);

        for (OrderEvent event : order.getEvents()) {
            assertThat(event.getOrderStatusAfter()).isNotNull();
            assertThat(event.getPaymentStatusAfter()).isNotNull();
            assertThat(event.getInventoryStatusAfter()).isNotNull();
            assertThat(event.getPendingActionAfter()).isNotNull();
            assertThat(event.getOccurredAt()).isNotNull();
            assertThat(event.getOrder()).isSameAs(order);
        }
        assertThat(order.getEvents().getLast().getInventoryStatusAfter()).isEqualTo(InventoryStatus.COMMITTED);
        assertThat(order.hasPendingAction()).isFalse();
        assertThat(order.isAwaitingPayment()).isFalse();
    }

    @Test
    void anEventMatchesOnlyItsOwnSourceAndRef() {
        Order order = Orders.awaitingPayment(PaymentType.STRIPE);
        order.applyPaymentSignal(PaymentStatus.PAID, TX, T2);
        OrderEvent event = order.getEvents().getLast();

        assertThat(event.matches(OrderEventSource.PAYMENT, PAID_REF)).isTrue();
        order.applyPaymentSignal(PaymentStatus.PAID, TX, T2);
        OrderEvent duplicate = order.getEvents().getLast();
        assertThat(duplicate.getSourceRef()).as("only the applied row carries the dedup key").isNull();
        assertThat(duplicate.getPayload()).isEqualTo(PAID_REF);
        assertThat(event.matches(OrderEventSource.INVENTORY, PAID_REF)).isFalse();
        assertThat(event.matches(OrderEventSource.PAYMENT, "tx-1:FAILED")).isFalse();
        assertThat(event.matches(OrderEventSource.PAYMENT, null)).isFalse();
        assertThat(new OrderEvent().matches(OrderEventSource.PAYMENT, "x")).isFalse();
    }
}

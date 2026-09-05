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
import com.asrevo.cvhome.checkout.repositories.OrderRepository;
import com.asrevo.cvhome.checkout.services.order.OrderStepRunner;
import com.asrevo.cvhome.checkout.services.order.TestTransactions;
import com.asrevo.cvhome.errors.remote.RemoteErrorContext;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The recovery loop: stale pending actions are claimed, attempted once, and after too many attempts flagged for a
 * person instead of retried forever.
 */
@ExtendWith(MockitoExtension.class)
class OrderRecoveryJobTest {

    @Mock
    private OrderRepository orders;

    @Mock
    private OrderStepRunner steps;

    private final CheckoutProperties properties = new CheckoutProperties();

    private OrderRecoveryJob job;

    @BeforeEach
    void setUp() {
        job = new OrderRecoveryJob(orders, steps, properties, TestTransactions.inline(),
                Clock.fixed(Orders.T2, ZoneOffset.UTC));
    }

    @Test
    void staleOrdersAreClaimedAndReDrivenOneStepEach() throws Exception {
        Order order = Orders.placed(PaymentType.STRIPE);
        when(orders.findStalePendingActionIds(eq(Orders.T2.minus(properties.getRecovery().getStaleAfter())),
                any(Pageable.class))).thenReturn(List.of(100L));
        when(orders.findById(100L)).thenReturn(Optional.of(order));

        job.recover();

        assertThat(order.getPendingActionAttempts()).isEqualTo(1);
        assertThat(order.getPendingActionUpdatedAt()).isEqualTo(Orders.T2);
        verify(orders).saveAndFlush(order);
        verify(steps).runUntilSettled(100L, 1);
    }

    @Test
    void aFailedAttemptIsLoggedAndLeftForTheNextPass() throws Exception {
        Order order = Orders.placed(PaymentType.STRIPE);
        when(orders.findById(100L)).thenReturn(Optional.of(order));
        when(steps.runUntilSettled(100L, 1)).thenThrow(InventoryApiUnavailableException.from(new RemoteErrorContext(
                null, null, java.util.Map.of(), java.util.List.of(), "inventory", 0, null, new RuntimeException())));

        assertThat(job.recoverOne(100L)).isTrue();

        assertThat(order.getPendingActionAttempts()).isEqualTo(1);
        assertThat(order.isNeedsAttention()).isFalse();
    }

    @Test
    void afterMaxAttemptsTheOrderIsFlaggedAndNoStepRuns() throws Exception {
        Order order = Orders.placed(PaymentType.STRIPE);
        for (int i = 0; i < properties.getRecovery().getMaxAttempts(); i++) {
            order.recoveryAttempted(Orders.T1);
        }
        when(orders.findById(100L)).thenReturn(Optional.of(order));

        assertThat(job.recoverOne(100L)).isFalse();

        assertThat(order.isNeedsAttention()).isTrue();
        verify(orders).saveAndFlush(order);
        verify(steps, never()).runUntilSettled(any(), anyInt());
    }

    @Test
    void anOrderThatOwesNothingOrIsFlaggedOrIsGoneIsSkipped() throws Exception {
        when(orders.findById(1L)).thenReturn(Optional.of(Orders.paid(PaymentType.STRIPE)));
        Order flagged = Orders.placed(PaymentType.STRIPE);
        flagged.recoveryGaveUp(Orders.T1);
        when(orders.findById(2L)).thenReturn(Optional.of(flagged));
        when(orders.findById(3L)).thenReturn(Optional.empty());

        assertThat(job.recoverOne(1L)).isFalse();
        assertThat(job.recoverOne(2L)).isFalse();
        assertThat(job.recoverOne(3L)).isFalse();
        verify(steps, never()).runUntilSettled(any(), anyInt());
    }

    @Test
    void losingTheClaimToAnotherReplicaSkipsTheOrder() throws Exception {
        when(orders.findById(100L)).thenReturn(Optional.of(Orders.placed(PaymentType.STRIPE)));
        when(orders.saveAndFlush(any(Order.class))).thenThrow(new ObjectOptimisticLockingFailureException(Order.class, 100L));

        assertThat(job.recoverOne(100L)).isFalse();
        verify(steps, never()).runUntilSettled(any(), anyInt());
    }
}

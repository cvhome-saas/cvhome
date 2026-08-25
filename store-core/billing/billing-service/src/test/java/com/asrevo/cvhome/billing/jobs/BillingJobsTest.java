package com.asrevo.cvhome.billing.jobs;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.domain.StoreSubscriptionEntity;
import com.asrevo.cvhome.billing.events.command.ApplyPendingPlanChangeCommand;
import com.asrevo.cvhome.billing.events.command.ExpireTrialCommand;
import com.asrevo.cvhome.billing.events.command.SuspendUnpaidSubscriptionCommand;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.namastack.outbox.Outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The three scheduled sweeps.
 *
 * <p>
 * All three only query and enqueue. Every instance runs them and may see the same due rows, but the outbox
 * partitions on the store id, so exactly one instance ends up doing the work — which is what stands in for the
 * distributed lock this codebase does not have. The partition key is therefore not incidental, and each case checks
 * it.
 * </p>
 */
class BillingJobsTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final ManagerOrgId ORG = new ManagerOrgId("32a034a43cd77581d105c87a");

    private StoreSubscriptionRepository subscriptions;

    private Outbox outbox;

    @BeforeEach
    void setUp() {
        subscriptions = mock(StoreSubscriptionRepository.class);
        outbox = mock(Outbox.class);
    }

    private static StoreSubscriptionEntity due() {
        return StoreSubscriptionEntity.pending(STORE, ORG);
    }

    private String partitionKey() {
        ArgumentCaptor<String> key = ArgumentCaptor.forClass(String.class);
        verify(outbox).schedule(any(), key.capture());
        return key.getValue();
    }

    @Test
    @DisplayName("an expired trial is enqueued on its store's partition")
    void expireTrials() {
        when(subscriptions.findAllByStatusAndTrialEndBefore(eqTrialing(), any(Instant.class)))
                .thenReturn(List.of(due()));

        new ExpireTrialsJob(subscriptions, outbox).execute();

        ArgumentCaptor<ExpireTrialCommand> command = ArgumentCaptor.forClass(ExpireTrialCommand.class);
        verify(outbox).schedule(command.capture(), anyString());
        assertThat(command.getValue().store()).isEqualTo(STORE);
        assertThat(partitionKey()).isEqualTo(STORE.getId().toString());
    }

    @Test
    @DisplayName("nothing due enqueues nothing")
    void expireTrialsWithNothingDue() {
        when(subscriptions.findAllByStatusAndTrialEndBefore(eqTrialing(), any(Instant.class)))
                .thenReturn(List.of());

        new ExpireTrialsJob(subscriptions, outbox).execute();

        verify(outbox, never()).schedule(any(), anyString());
    }

    @Test
    @DisplayName("a closed grace window is enqueued on its store's partition")
    void suspendUnpaid() {
        when(subscriptions.findAllByStatusAndGraceUntilBefore(eqPastDue(), any(Instant.class)))
                .thenReturn(List.of(due()));

        new SuspendUnpaidSubscriptionsJob(subscriptions, outbox).execute();

        ArgumentCaptor<SuspendUnpaidSubscriptionCommand> command =
                ArgumentCaptor.forClass(SuspendUnpaidSubscriptionCommand.class);
        verify(outbox).schedule(command.capture(), anyString());
        assertThat(command.getValue().store()).isEqualTo(STORE);
        assertThat(partitionKey()).isEqualTo(STORE.getId().toString());
    }

    @Test
    @DisplayName("no store past its grace window enqueues nothing")
    void suspendUnpaidWithNothingDue() {
        when(subscriptions.findAllByStatusAndGraceUntilBefore(eqPastDue(), any(Instant.class)))
                .thenReturn(List.of());

        new SuspendUnpaidSubscriptionsJob(subscriptions, outbox).execute();

        verify(outbox, never()).schedule(any(), anyString());
    }

    @Test
    @DisplayName("a deferred plan change whose date has passed is enqueued")
    void applyPendingChanges() {
        when(subscriptions.findAllByPendingEffectiveAtBefore(any(Instant.class))).thenReturn(List.of(due()));

        new ApplyPendingPlanChangesJob(subscriptions, outbox).execute();

        ArgumentCaptor<ApplyPendingPlanChangeCommand> command =
                ArgumentCaptor.forClass(ApplyPendingPlanChangeCommand.class);
        verify(outbox).schedule(command.capture(), anyString());
        assertThat(command.getValue().store()).isEqualTo(STORE);
    }

    @Test
    @DisplayName("nothing overdue enqueues nothing — the provider's webhook normally got there first")
    void applyPendingChangesWithNothingDue() {
        when(subscriptions.findAllByPendingEffectiveAtBefore(any(Instant.class))).thenReturn(List.of());

        new ApplyPendingPlanChangesJob(subscriptions, outbox).execute();

        // This job is a safety net for a webhook that never came, not the mechanism.
        verify(outbox, never()).schedule(any(), anyString());
    }

    private static SubscriptionStatus eqTrialing() {
        return org.mockito.ArgumentMatchers.eq(SubscriptionStatus.TRIALING);
    }

    private static SubscriptionStatus eqPastDue() {
        return org.mockito.ArgumentMatchers.eq(SubscriptionStatus.PAST_DUE);
    }

}

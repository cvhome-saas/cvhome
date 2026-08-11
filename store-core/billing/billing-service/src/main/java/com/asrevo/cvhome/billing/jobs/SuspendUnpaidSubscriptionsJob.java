package com.asrevo.cvhome.billing.jobs;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.events.command.SuspendUnpaidSubscriptionCommand;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;

import io.namastack.outbox.Outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Suspends stores whose grace window after a failed renewal has closed.
 *
 * <p>
 * Same shape as {@link ExpireTrialsJob}: query, enqueue, let the outbox's partitioning decide who acts.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SuspendUnpaidSubscriptionsJob {

    private final StoreSubscriptionRepository subscriptionRepository;

    private final Outbox outbox;

    @Scheduled(cron = "0 */10 * * * *")
    void execute() {
        var due = subscriptionRepository.findAllByStatusAndGraceUntilBefore(SubscriptionStatus.PAST_DUE,
                Instant.now());
        if (due.isEmpty()) {
            return;
        }
        log.info("Suspending {} subscriptions past their grace window", due.size());
        due.forEach(subscription -> {
            var command = SuspendUnpaidSubscriptionCommand.from(subscription.getId());
            outbox.schedule(command, command.store().getId().toString());
        });
    }

}

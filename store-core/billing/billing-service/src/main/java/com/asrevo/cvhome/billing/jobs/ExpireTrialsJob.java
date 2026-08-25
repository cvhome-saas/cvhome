package com.asrevo.cvhome.billing.jobs;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.billing.events.command.ExpireTrialCommand;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;

import io.namastack.outbox.Outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Suspends trials that ran out without a payment.
 *
 * <p>
 * The job itself only queries and enqueues. Every instance runs it and may see the same due rows, but the outbox
 * partitions on the store id, so exactly one instance ends up doing the work — which is what stands in for the
 * distributed lock this codebase does not have.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExpireTrialsJob {

    private final StoreSubscriptionRepository subscriptionRepository;

    private final Outbox outbox;

    @Scheduled(cron = "0 */10 * * * *")
    void execute() {
        var due = subscriptionRepository.findAllByStatusAndTrialEndBefore(SubscriptionStatus.TRIALING, Instant.now());
        if (due.isEmpty()) {
            return;
        }
        log.info("Expiring {} trials", due.size());
        due.forEach(subscription -> {
            var command = ExpireTrialCommand.from(subscription.getId());
            outbox.schedule(command, command.store().getId().toString());
        });
    }

}

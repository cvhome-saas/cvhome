package com.asrevo.cvhome.billing.jobs;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.billing.events.command.ApplyPendingPlanChangeCommand;
import com.asrevo.cvhome.billing.repository.StoreSubscriptionRepository;

import io.namastack.outbox.Outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies deferred plan changes whose date has passed.
 *
 * <p>
 * A safety net, not the mechanism. The provider's own schedule flips the price and reports it as
 * {@code customer.subscription.updated}, which is what normally applies the change — this only catches the case where
 * that webhook never arrived, which would otherwise leave a customer paying the cheaper price while still holding the
 * more expensive plan's entitlements.
 * </p>
 *
 * <p>
 * Both paths converge on the same idempotent aggregate method, so whichever gets there second does nothing.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ApplyPendingPlanChangesJob {

    private final StoreSubscriptionRepository subscriptionRepository;

    private final Outbox outbox;

    @Scheduled(cron = "0 */10 * * * *")
    void execute() {
        var due = subscriptionRepository.findAllByPendingEffectiveAtBefore(Instant.now());
        if (due.isEmpty()) {
            return;
        }
        log.info("Applying {} deferred plan changes the provider has not reported", due.size());
        due.forEach(subscription -> {
            var command = ApplyPendingPlanChangeCommand.from(subscription.getId());
            outbox.schedule(command, command.store().getId().toString());
        });
    }

}

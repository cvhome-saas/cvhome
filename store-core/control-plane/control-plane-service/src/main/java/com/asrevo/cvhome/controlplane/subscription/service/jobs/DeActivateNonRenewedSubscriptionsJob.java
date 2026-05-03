package com.asrevo.cvhome.controlplane.subscription.service.jobs;

import java.time.Instant;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.commons.event.EventProcessor;
import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionStatus;
import com.asrevo.cvhome.controlplane.subscription.commons.command.DeActivateNonRenewedSubscriptionCommand;
import com.asrevo.cvhome.controlplane.subscription.domain.SubscriptionEntity;
import com.asrevo.cvhome.controlplane.subscription.repository.SubscriptionRepository;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class DeActivateNonRenewedSubscriptionsJob {

    private final SubscriptionRepository subscriptionRepository;

    private final EventProcessor eventProcessor;

    private final ObservationRegistry observationRegistry;

    @Scheduled(cron = "0 */5 * * * *")
    void execute() {
        Observation.createNotStarted("cvhome.controlplane.deactivate-non-renewed-subscriptions", observationRegistry)
                .observe(() -> {
                    log.info("Running Deactivating non-renewed subscriptions Job at {}", Instant.now());
                    List<SubscriptionEntity> subscriptions = subscriptionRepository
                            .findAllByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, Instant.now());
                    subscriptions.forEach(subscription -> {
                        log.info("Firing Locking non-renewed subscription {}", subscription.getId());
                        eventProcessor.process(DeActivateNonRenewedSubscriptionCommand.from(subscription.getId()));
                    });
                });
    }

}

package com.asrevo.cvhome.controlplane.subscription.service.jobs;

import com.asrevo.cvhome.commons.event.EventProcessor;
import com.asrevo.cvhome.subscription.commons.SubscriptionStatus;
import com.asrevo.cvhome.subscription.commons.command.DeActivateNonRenewedSubscriptionCommand;
import com.asrevo.cvhome.controlplane.subscription.domain.SubscriptionEntity;
import com.asrevo.cvhome.controlplane.subscription.repository.SubscriptionRepository;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class DeActivateNonRenewedSubscriptionsJob {

	private final SubscriptionRepository subscriptionRepository;

	private final EventProcessor eventProcessor;

	@Scheduled(cron = "0 */5 * * * *")
	void execute() {
		log.info("Running Deactivating non-renewed subscriptions Job at {}", Instant.now());
		List<SubscriptionEntity> subscriptions = subscriptionRepository
			.findAllByStatusAndEndDateBefore(SubscriptionStatus.ACTIVE, Instant.now());
		subscriptions.forEach(subscription -> {
			log.info("Firing Locking non-renewed subscription {}", subscription.getId());
			eventProcessor.process(DeActivateNonRenewedSubscriptionCommand.from(subscription.getId()));
		});
	}

}

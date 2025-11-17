package com.asrevo.cvhome.subscription.service.impl;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.subscription.commons.RecurringPlan;
import com.asrevo.cvhome.subscription.commons.SubscriptionPlanDetails;
import com.asrevo.cvhome.subscription.domain.SubscriptionEntity;
import com.asrevo.cvhome.subscription.repository.SubscriptionRepository;
import com.asrevo.cvhome.subscription.service.SubscriptionService;
import java.time.Instant;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

	private final SubscriptionRepository subscriptionRepository;

	private final SubscriptionMapper subscriptionMapper;

	@Transactional
	@Override
	public void createInitialSubscription(ManagerOrgId orgId) {
		SubscriptionEntity subscription = SubscriptionEntity.createInitialSubscription(orgId);
		subscriptionRepository.save(subscription);
	}

	@Override
	public Optional<SubscriptionPlanDetails> subscriptionPlanDetails(ManagerOrgId org) {
		return subscriptionRepository.findById(org).map(subscriptionMapper::toSubscriptionPlanDetails);
	}

	@Transactional
	@Override
	public void deActivateSubscription(ManagerOrgId org) {
		subscriptionRepository.findById(org).map(it -> subscriptionRepository.save(it.deActivate()));
	}

	@Transactional
	@Override
	public void renew(ManagerOrgId org, SubscriptionPlan subscriptionPlan, Instant startDate, Instant endDate,
			RecurringPlan recurringPlan) {
		subscriptionRepository.findById(org)
			.map(it -> subscriptionRepository.save(it.renew(subscriptionPlan, startDate, endDate, recurringPlan)));
	}

}

package com.asrevo.cvhome.controlplane.subscription.service;

import java.time.Instant;
import java.util.Optional;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.controlplane.subscription.commons.RecurringPlan;
import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionPlanDetails;

public interface SubscriptionService {

    void createInitialSubscription(ManagerOrgId orgId);

    Optional<SubscriptionPlanDetails> subscriptionPlanDetails(ManagerOrgId org);

    void deActivateSubscription(ManagerOrgId org);

    void renew(ManagerOrgId org, SubscriptionPlan subscriptionPlan, Instant startDate, Instant endDate,
               RecurringPlan recurringPlan);

}

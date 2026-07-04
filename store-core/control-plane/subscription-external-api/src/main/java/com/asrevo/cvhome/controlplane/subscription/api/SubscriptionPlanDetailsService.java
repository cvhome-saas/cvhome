package com.asrevo.cvhome.controlplane.subscription.api;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;

@HttpExchange("/api/v1/subscription")
public interface SubscriptionPlanDetailsService {

    @GetExchange("subscription-plan-details")
    Object subscriptionPlanDetails(@RequestParam("org-id") ManagerOrgId orgId);

}

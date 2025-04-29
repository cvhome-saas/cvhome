package com.asrevo.cvhome.subscription.api;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

@HttpExchange("/api/v1/subscription")
public interface SubscriptionPlanDetailsService {
    @GetExchange("subscription-plan-details")
    Mono<Object> subscriptionPlanDetails(@RequestParam("org-id") ManagerOrgId orgId);
}

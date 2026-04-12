package com.asrevo.cvhome.controlplane.subscription.controller;

import com.asrevo.cvhome.commons.domain.SubscriptionPlan;
import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionPlanTables;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionPlanTablesService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/subscription-plan/public")
@AllArgsConstructor
@Slf4j
public class SubscriptionPlanController {

	private final SubscriptionPlanTablesService subscriptionPlanTablesService;

	@GetMapping("list")

	public SubscriptionPlan[] list() {
		return SubscriptionPlan.values();
	}

	@GetMapping("tables")
	public SubscriptionPlanTables tables() {
		return subscriptionPlanTablesService.tables();
	}

}

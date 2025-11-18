package com.asrevo.cvhome.controlplane.subscription.controller;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.controlplane.subscription.commons.SubscriptionPlanDetails;
import com.asrevo.cvhome.controlplane.subscription.service.SubscriptionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/subscription")
@Slf4j
@AllArgsConstructor
public class SubscriptionPlanDetailsController {

	private final SubscriptionService subscriptionService;

	@GetMapping("subscription-plan-details")
	@ConditionalOnApiStatus
	public ResponseEntity<SubscriptionPlanDetails> currentSubscriptionPlanDetails(
			@OrgStorePrincipalInfo UserOrgStoreIdentity identity) {
		return subscriptionService.subscriptionPlanDetails(identity.org())
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

	@GetMapping(value = "subscription-plan-details", params = "org-id")
	@ConditionalOnApiStatus
	public ResponseEntity<SubscriptionPlanDetails> subscriptionPlanDetails(@RequestParam("org-id") ManagerOrgId orgId) {
		return subscriptionService.subscriptionPlanDetails(orgId)
			.map(ResponseEntity::ok)
			.orElseGet(() -> ResponseEntity.notFound().build());
	}

}

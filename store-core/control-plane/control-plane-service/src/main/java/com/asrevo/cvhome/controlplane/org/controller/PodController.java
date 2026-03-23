package com.asrevo.cvhome.controlplane.org.controller;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import java.util.List;

import com.asrevo.cvhome.controlplane.org.service.PodService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/pod")
@AllArgsConstructor
@Slf4j
public class PodController {

	private final PodService podService;

	@GetMapping("list")
	@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_ORG_ADMIN')")
	@ConditionalOnApiStatus
	public Mono<List<Pod>> findAllPods(@OrgStorePrincipalInfo UserOrgStoreIdentity identity) {
		if (identity.isSuperAdmin()) {
			return Mono.just(podService.listAllPods());
		}
		else {
			return Mono.just(podService.listAllPods(identity.org()));
		}
	}

}

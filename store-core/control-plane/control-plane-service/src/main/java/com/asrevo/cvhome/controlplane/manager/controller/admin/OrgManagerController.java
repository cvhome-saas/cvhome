package com.asrevo.cvhome.controlplane.manager.controller.admin;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.service.UserAccountService;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerOrgDto;
import com.asrevo.cvhome.controlplane.manager.commons.dto.ManagerStoreDto;
import com.asrevo.cvhome.controlplane.manager.dto.CreateOrgRequest;
import com.asrevo.cvhome.controlplane.manager.service.InternalOrgService;
import com.asrevo.cvhome.controlplane.manager.service.InternalStoreService;
import com.asrevo.cvhome.controlplane.manager.service.SignupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/org-manager")
@AllArgsConstructor
@Slf4j
public class OrgManagerController {

	private final InternalOrgService internalOrgService;

	private final SignupService signupService;

	private final UserAccountService userAccountService;

	private final InternalStoreService internalStoreService;

	@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
	@GetMapping("find-all")
	@ConditionalOnApiStatus
	public Mono<Page<ManagerOrgDto>> findAllOrg(Pageable pageable) {
		log.info("findAllOrg {}", pageable);
		return Mono.just(internalOrgService.findAll(pageable));
	}

	@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
	@GetMapping("find-one")
	@ConditionalOnApiStatus
	public Mono<ManagerOrgDto> findOne(@RequestParam ManagerOrgId id) {
		return Mono.just(internalOrgService.findOne(id));
	}

	@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
	@PostMapping("create")
	@ConditionalOnApiStatus
	public Mono<ReadableUser> create(@RequestBody CreateOrgRequest request) {
		return Mono.just(signupService.createOrgUser(request));
	}

	@PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN')")
	@PostMapping("change-password")
	@ConditionalOnApiStatus
	public Mono<Void> changePassword(@RequestParam ManagerOrgId id, @RequestBody UserPassword request) {
		userAccountService.changePassword(id.toString(), request);
		return Mono.empty();
	}

	@GetMapping("stores")
	@ConditionalOnApiStatus
	public Mono<Page<ManagerStoreDto>> findAllStores(@RequestParam ManagerOrgId id, Pageable pageable) {
		return Mono.just(internalStoreService.findAll(id, pageable));
	}

}

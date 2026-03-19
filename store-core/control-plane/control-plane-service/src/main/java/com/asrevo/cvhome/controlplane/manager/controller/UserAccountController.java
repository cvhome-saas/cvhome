package com.asrevo.cvhome.controlplane.manager.controller;

import com.asrevo.cvhome.commons.annotation.ApiUsage;
import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.Groups;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.controlplane.manager.service.ManagedUserAccountService;
import java.security.Principal;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/user-account")
@Slf4j
@AllArgsConstructor
public class UserAccountController {

	private final ManagedUserAccountService managedUserAccountService;

	@GetMapping("current")
	@ConditionalOnApiStatus
	public Mono<ReadableUser> current(@AuthenticationPrincipal Principal principal) {
		return Mono.just(managedUserAccountService.findOne(principal.getName()));
	}

	@GetMapping("list")
	@PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.LIST')")
	@ConditionalOnApiStatus
	public Mono<ReadableUserList> list(@AuthenticationPrincipal Principal principal,
			@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store,
			Pageable pageable) {
		return managedUserAccountService.list(identity, store, pageable);
	}

	@GetMapping("find-one")
	// @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.LIST')")
	@ConditionalOnApiStatus
	public Mono<ReadableUser> findOne(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
			@RequestParam ManagerStoreId store, @RequestParam String userId) {
		return managedUserAccountService.findOne(identity, store, userId);
	}

	@GetMapping("assignable-roles")
	@ConditionalOnApiStatus
	public Mono<Set<String>> assignableRoles() {
		return managedUserAccountService.getAssignableRoles();
	}

	@PostMapping("create")
	@PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.CREATE')")
	@ConditionalOnApiStatus
	public Mono<ReadableUser> create(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
			@RequestParam ManagerStoreId store, @RequestBody PersistableUser user) {
		return managedUserAccountService.createUser(identity, store, user);
	}

	@PutMapping("update")
	@PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.UPDATE')")
	@ConditionalOnApiStatus
	public Mono<ReadableUser> update(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
			@RequestParam ManagerStoreId store, @RequestBody PersistableUser user) {
		return managedUserAccountService.updateUser(identity, store, user);
		//
		// {"firstName":"12313","lastName":"55555","userName":"org1-store1-moderator","emailAddress":"sfds@dfsf.vv","password":"","repeatPassword":"","active":true,"groups":[{"name":"STORE_MODERATOR"}],"id":"3dea29fd-f6b2-48b1-8231-f4b5f1c68715"}
	}

	@PostMapping("reset")
	@PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.RESET_PASSWORD')")
	@ConditionalOnApiStatus(usage = ApiUsage.NOT_USED)
	public Mono<Void> resetPassword(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
			@RequestParam ManagerStoreId store, @RequestParam String userId,
			@RequestBody UserPassword passwordRequestDto) {
		return managedUserAccountService.resetPassword(identity, store, userId, passwordRequestDto);
	}

	@DeleteMapping("delete")
	@PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.DELETE')")
	@ConditionalOnApiStatus
	public Mono<Void> delete(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store,
			@RequestParam String userId) {
		return managedUserAccountService.deleteUser(identity, store, userId);
	}

	@PostMapping("enable")
	@PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.ENABLE')")
	@ConditionalOnApiStatus
	public Mono<Void> enable(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store,
			@RequestParam String userId) {
		return managedUserAccountService.enableUser(identity, store, userId);
	}

	@PostMapping("disable")
	@PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE.USERS.DISABLE')")
	@ConditionalOnApiStatus
	public Mono<Void> disable(@OrgStorePrincipalInfo UserOrgStoreIdentity identity, @RequestParam ManagerStoreId store,
			@RequestParam String userId) {
		return managedUserAccountService.disableUser(identity, store, userId);
	}

}

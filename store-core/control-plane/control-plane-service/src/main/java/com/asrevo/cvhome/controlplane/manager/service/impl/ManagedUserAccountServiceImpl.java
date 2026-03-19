package com.asrevo.cvhome.controlplane.manager.service.impl;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import com.asrevo.cvhome.uaa.service.UserAccountService;
import com.asrevo.cvhome.controlplane.manager.service.ManagedUserAccountService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@Service
@AllArgsConstructor
@Slf4j
public class ManagedUserAccountServiceImpl implements ManagedUserAccountService {

	private final UserAccountService userAccountService;

	@Override
	public ReadableUser findOne(String id) {
		return userAccountService.findOne(id);
	}

	@Override
	public Mono<ReadableUserList> list(UserOrgStoreIdentity identity, ManagerStoreId store, Pageable pageable) {
		return Mono.fromCallable(() -> userAccountService.list(
				Map.of("org", identity.org().id().toString(), "store", store.id().toString()), pageable.getPageNumber(),
				pageable.getPageSize()));
	}

	@Override
	public Mono<ReadableUser> findOne(UserOrgStoreIdentity identity, ManagerStoreId store, String userId) {
		return Mono.fromCallable(() -> {
			ReadableUser user = findOne(userId);
			validateUserAccess(identity, store, user);
			return user;
		});
	}

	@Override
	public Mono<ReadableUser> createUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user) {
		return Mono.fromCallable(() -> {
			user.setActive(true);
			user.setOrg(identity.org().id().toString());
			user.setStore(store.id().toString());
			return userAccountService.createUser(user);
		});
	}

	@Override
	public Mono<ReadableUser> updateUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user) {
		return Mono.fromCallable(() -> {
			ReadableUser existingUser = findOne(user.getId());
			validateUserAccess(identity, store, existingUser);
			// Ensure the user is not moved to another org/store via update
			user.setOrg(identity.org().id().toString());
			user.setStore(store.id().toString());
			return userAccountService.updateUser(user);
		});
	}

	@Override
	public Mono<Void> resetPassword(UserOrgStoreIdentity identity, ManagerStoreId store, String userId,
			UserPassword passwordRequestDto) {
		return Mono.fromRunnable(() -> {
			ReadableUser user = findOne(userId);
			validateUserAccess(identity, store, user);
			userAccountService.changePassword(userId, passwordRequestDto);
		});
	}

	@Override
	public Mono<Void> deleteUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId) {
		return Mono.fromRunnable(() -> {
			ReadableUser user = findOne(userId);
			validateUserAccess(identity, store, user);
			userAccountService.deleteUser(userId);
		});
	}

	@Override
	public Mono<Void> enableUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId) {
		return Mono.fromRunnable(() -> {
			ReadableUser user = findOne(userId);
			validateUserAccess(identity, store, user);
			userAccountService.enableUser(userId);
		});
	}

	@Override
	public Mono<Void> disableUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId) {
		return Mono.fromRunnable(() -> {
			ReadableUser user = findOne(userId);
			validateUserAccess(identity, store, user);
			userAccountService.disableUser(userId);
		});
	}

	@Override
	public Mono<Set<String>> getAssignableRoles() {
		return Mono.fromCallable(userAccountService::getAssignableRoles);
	}

	private void validateUserAccess(UserOrgStoreIdentity identity, ManagerStoreId store, ReadableUser user) {
		if (user == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
		}
		if (!identity.org().id().toString().equals(user.getOrg())) {
			log.error("Access denied: User {} belongs to org {}, but admin belongs to org {}", user.getId(),
					user.getOrg(), identity.org());
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"Access denied: User belongs to another organization");
		}
		if (!store.id().toString().equals(user.getStore())) {
			log.error("Access denied: User {} belongs to store {}, but request is for store {}", user.getId(),
					user.getStore(), store);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: User belongs to another store");
		}
	}

}

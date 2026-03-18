package com.asrevo.cvhome.controlplane.manager.service;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface ManagedUserAccountService {

	ReadableUser findOne(String id);

	Mono<ReadableUserList> list(UserOrgStoreIdentity identity, ManagerStoreId store, Pageable pageable);

	Mono<ReadableUser> findOne(UserOrgStoreIdentity identity, ManagerStoreId store, String userId);

	Mono<ReadableUser> createUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user);

	Mono<ReadableUser> updateUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user);

	Mono<Void> resetPassword(UserOrgStoreIdentity identity, ManagerStoreId store, String userId,
			UserPassword passwordRequestDto);

	Mono<Void> deleteUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId);

	Mono<Void> enableUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId);

	Mono<Void> disableUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId);

}

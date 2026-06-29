package com.asrevo.cvhome.controlplane.manager.service;

import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.uaa.domain.user.PersistableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUser;
import com.asrevo.cvhome.uaa.domain.user.ReadableUserList;
import com.asrevo.cvhome.uaa.domain.user.UserPassword;

public interface ManagedUserAccountService {

    ReadableUser findOne(String id);

    ReadableUserList list(UserOrgStoreIdentity identity, ManagerStoreId store, Pageable pageable);

    ReadableUser findOne(UserOrgStoreIdentity identity, ManagerStoreId store, String userId);

    ReadableUser createUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user);

    ReadableUser updateUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user);

    void resetPassword(UserOrgStoreIdentity identity, ManagerStoreId store, String userId,
                       UserPassword passwordRequestDto);

    void deleteUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId);

    void enableUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId);

    void disableUser(UserOrgStoreIdentity identity, ManagerStoreId store, String userId);

    Set<String> getAssignableRoles();

}

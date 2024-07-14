package com.asrevo.cvhome.keycloak.service;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.keycloak.domain.user.PersistableUser;
import com.asrevo.cvhome.keycloak.domain.user.ReadableUser;
import com.asrevo.cvhome.keycloak.domain.user.ReadableUserList;
import com.asrevo.cvhome.keycloak.domain.user.UserPassword;

import java.security.Principal;


public interface UserAccountService {
    ReadableUser createOrgUser(PersistableUser user);

    ReadableUser current(String id);

    ReadableUserList list(Principal principal, UserOrgStoreIdentity identity, ManagerStoreId store);

    ReadableUser createManagedUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser create);

    ReadableUser updateManagedUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user);

    void resetPassword(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, UserPassword passwordRequestDto, String userId,boolean temporary);

    boolean usernameExist(String username);

    void deleteUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId);

    void enableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId);

    void disableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId);

    ReadableUser findOne(UserOrgStoreIdentity identity, String userId);

}

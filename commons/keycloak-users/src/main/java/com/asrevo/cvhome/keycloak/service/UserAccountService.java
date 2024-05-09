package com.asrevo.cvhome.keycloak.service;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.keycloak.domain.user.*;


public interface UserAccountService {
    ReadableUserList list(ListUsersQuery listUsers);

    ReadableUser createUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser create);

    void resetPassword(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, UserPassword passwordRequestDto, String userId);

    boolean usernameExist(String username);

    void deleteUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId);

    void enableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId);

    void disableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId);

    ReadableUser findOne(UserOrgStoreIdentity identity, String userId);
}

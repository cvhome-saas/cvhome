package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.manager.commons.dto.CreateUserRequestDto;
import com.asrevo.cvhome.manager.commons.dto.KeyCloakUserDto;
import com.asrevo.cvhome.manager.commons.dto.ListUsersQuery;
import com.asrevo.cvhome.manager.commons.dto.RestPasswordRequestDto;

import java.util.List;

public interface UserAccountService {

    List<KeyCloakUserDto> list(ListUsersQuery listUsers);

    KeyCloakUserDto createUser(IdentityId identityId, ManagerStoreId managerStoreId, CreateUserRequestDto createUserRequestDto);

    void resetPassword(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, RestPasswordRequestDto passwordRequestDto, String userId);

    boolean usernameExist(String username);

    void deleteUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId);

    void enableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId);

    void disableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId);

}

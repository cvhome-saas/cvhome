package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreInfo;
import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.commons.dto.CreateUserRequestDto;
import com.asrevo.cvhome.manager.commons.dto.KeyCloakUserDto;
import com.asrevo.cvhome.manager.commons.dto.ListUsersQuery;
import com.asrevo.cvhome.manager.commons.dto.RestPasswordRequestDto;

import java.util.List;

public interface UserAccountService {

    List<KeyCloakUserDto> list(ListUsersQuery listUsers);

    void createUser(IdentityId identityId, ManagerStoreId managerStoreId, CreateUserRequestDto createUserRequestDto);

    void resetPassword(UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId, RestPasswordRequestDto passwordRequestDto, String userId);

    boolean usernameExist(String username);

    void deleteUser(UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId, String userId);

    void enableUser(UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId, String userId);

    void disableUser(UserOrgStoreInfo userOrgStoreInfo, ManagerStoreId storeId, String userId);

}

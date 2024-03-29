package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.service.AccessEvaluator;
import com.asrevo.cvhome.manager.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccessEvaluatorImpl implements AccessEvaluator {
    private final SecurityService securityRoleCheckService;

    @Override
    public boolean hasAccessOnStoreUsersList(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasReadAccessOnStore(authentication, requestedStoreId);
    }

    @Override
    public boolean hasAccessOnStoreUsersCreate(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    @Override
    public boolean hasAccessOnStoreUsersDelete(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    @Override
    public boolean hasAccessOnStoreUsersEnable(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }


    @Override
    public boolean hasAccessOnStoreUsersDisable(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    @Override
    public boolean hasAccessOnStoreFindOne(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasReadAccessOnStore(authentication, requestedStoreId);
    }

    private boolean hasReadAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (securityRoleCheckService.isOrgAdmin(authentication, requestedStoreId)) {
            return true;
        } else if (securityRoleCheckService.isStoreAdmin(authentication, requestedStoreId)) {
            return true;
        } else if (securityRoleCheckService.isStoreModerator(authentication, requestedStoreId)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean hasMaintainAccessOnUsers(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (securityRoleCheckService.isOrgAdmin(authentication, requestedStoreId)) {
            return true;
        } else if (securityRoleCheckService.isStoreAdmin(authentication, requestedStoreId)) {
            return true;
        } else {
            return false;
        }
    }
}

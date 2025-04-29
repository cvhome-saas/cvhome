package com.asrevo.cvhome.manager.service;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.s2s.services.AccessEvaluator;
import lombok.AllArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@AllArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {
    private final AccessEvaluator rolesEvaluator;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        String action = (String) permission;
        return switch (action) {
            // store
            case "STORE.FIND-ONE" ->
                    rolesEvaluator.hasAccessOnStoreFindOne(authentication, ((ManagerStoreId) targetId));
            case "STORE.FIND-ONE-DETAILED" ->
                    rolesEvaluator.hasAccessOnStoreFindOne(authentication, ((ManagerStoreId) targetId));
            // users
            case "STORE.USERS.LIST" ->
                    rolesEvaluator.hasAccessOnStoreUsersList(authentication, ((ManagerStoreId) targetId));
            case "STORE.USERS.CREATE" ->
                    rolesEvaluator.hasAccessOnStoreUsersCreate(authentication, ((ManagerStoreId) targetId));
            case "STORE.USERS.UPDATE" ->
                    rolesEvaluator.hasAccessOnStoreUsersUpdate(authentication, ((ManagerStoreId) targetId));
            case "STORE.USERS.DELETE" ->
                    rolesEvaluator.hasAccessOnStoreUsersDelete(authentication, ((ManagerStoreId) targetId));
            case "STORE.USERS.ENABLE" ->
                    rolesEvaluator.hasAccessOnStoreUsersEnable(authentication, ((ManagerStoreId) targetId));
            case "STORE.USERS.DISABLE" ->
                    rolesEvaluator.hasAccessOnStoreUsersDisable(authentication, ((ManagerStoreId) targetId));
            // domains
            case "STORE.DOMAIN.LIST" ->
                    rolesEvaluator.hasAccessOnStoreDomainList(authentication, ((ManagerStoreId) targetId));
            case "STORE.DOMAIN.CREATE" ->
                    rolesEvaluator.hasAccessOnStoreDomainCreate(authentication, ((ManagerStoreId) targetId));
            case "STORE.DOMAIN.DELETE" ->
                    rolesEvaluator.hasAccessOnStoreDomainDelete(authentication, ((ManagerStoreId) targetId));
            default -> false;
        };
    }

}
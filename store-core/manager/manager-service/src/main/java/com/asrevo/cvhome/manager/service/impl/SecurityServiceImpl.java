package com.asrevo.cvhome.manager.service.impl;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.UserOrgStoreInfo;
import com.asrevo.cvhome.manager.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.manager.service.InternalStoreService;
import com.asrevo.cvhome.manager.service.SecurityService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SecurityServiceImpl implements SecurityService {
    private final InternalStoreService internalStoreService;

    @Override
    public boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (!hasOrgAdminRole(authentication)) {
            return false;
        }
        String orgName = authentication.getName();
        return internalStoreService.getStoreOwner(requestedStoreId).getId().toString().equals(orgName);
    }


    @Override
    public boolean isStoreAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (!hasStoreAdminRole(authentication)) {
            return false;
        }
        UserOrgStoreInfo info = getOrgStoreInfo(authentication);
        if (!requestedStoreId.getId().toString().equals(info.store())) {
            return false;
        }
        return internalStoreService.getStoreOwner(requestedStoreId).equals(info.org());
    }

    @Override
    public boolean isStoreModerator(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (!hasStoreModeratorRole(authentication)) {
            return false;
        }
        UserOrgStoreInfo info = getOrgStoreInfo(authentication);
        if (!requestedStoreId.getId().toString().equals(info.store())) {
            return false;
        }
        return internalStoreService.getStoreOwner(requestedStoreId).equals(info.org());
    }

    private static boolean hasOrgAdminRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_ORG_ADMIN);
    }

    private static boolean hasStoreAdminRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_STORE_ADMIN);
    }

    private static boolean hasStoreModeratorRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_STORE_MODERATOR);
    }

    private static boolean hasRole(Authentication authentication, Roles role) {
        return authentication.getAuthorities().stream().anyMatch(it -> it.getAuthority().contains(role.name()));
    }

    public static UserOrgStoreInfo getOrgStoreInfo(Authentication authentication) {
        List<Roles> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).map(Roles::parse).toList();
        if (hasOrgAdminRole(authentication)) {
            return new UserOrgStoreInfo(IdentityId.of(authentication.getName()), "*", roles);
        } else {
            Map<String, Object> claims = ((Jwt) authentication.getPrincipal()).getClaims();
            String adminOrg = ((String) claims.get("org"));
            String adminStore = ((String) claims.get("store"));
            return new UserOrgStoreInfo(IdentityId.of(adminOrg), adminStore, roles);
        }
    }
}

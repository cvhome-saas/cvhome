package com.asrevo.cvhome.s2s.services;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.UserOrgStoreInfo;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;

import java.util.function.Function;

import static com.asrevo.cvhome.s2s.utils.SecurityUtils.getOrgStoreInfo;

@AllArgsConstructor
public class StoreSecurityServiceImpl implements StoreSecurityService {
    private final Function<ManagerStoreId, IdentityId> getOwnerForStore;

    private static boolean hasSuperAdminRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_SUPER_ADMIN);
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

    @Override
    public boolean isSuperAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasSuperAdminRole(authentication);
    }

    @Override
    public boolean isOrgAdmin(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (!hasOrgAdminRole(authentication)) {
            return false;
        }
        String orgName = authentication.getName();
        return getOwnerForStore.apply((requestedStoreId)).getId().toString().equals(orgName);
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
        return getOwnerForStore.apply((requestedStoreId)).equals(info.org());
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
        return getOwnerForStore.apply((requestedStoreId)).equals(info.org());
    }


}

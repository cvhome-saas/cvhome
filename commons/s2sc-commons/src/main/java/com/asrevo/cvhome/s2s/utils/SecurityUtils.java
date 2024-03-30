package com.asrevo.cvhome.s2s.utils;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.UserOrgStoreInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SecurityUtils {
    private static boolean hasOrgAdminRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_ORG_ADMIN);
    }
    private static boolean hasRole(Authentication authentication, Roles role) {
        return authentication.getAuthorities().stream().anyMatch(it -> it.getAuthority().contains(role.name()));
    }
    public static UserOrgStoreInfo getOrgStoreInfo(Authentication authentication) {
        List<Roles> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).map(Roles::parse).filter(Objects::nonNull).toList();
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

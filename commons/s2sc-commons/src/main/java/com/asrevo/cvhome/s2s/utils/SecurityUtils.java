package com.asrevo.cvhome.s2s.utils;

import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityUtils {
    private static boolean hasSuperAdminRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_SUPER_ADMIN);
    }

    private static boolean hasMicroServiceRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_MICROSERVICE);
    }

    private static boolean hasOrgAdminRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_ORG_ADMIN);
    }

    private static boolean hasRole(Authentication authentication, Roles role) {
        return authentication.getAuthorities().stream().anyMatch(it -> it.getAuthority().contains(role.name()));
    }

    public static UserOrgStoreIdentity getOrgStoreIdentity(Authentication authentication) {
        Set<Roles> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(Roles::parse)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (hasSuperAdminRole(authentication)) {
            return new UserOrgStoreIdentity(IdentityId.of("*"), "*", roles);
        } else if (hasMicroServiceRole(authentication)) {
            return new UserOrgStoreIdentity(IdentityId.of("*"), "*", roles);
        } else if (hasOrgAdminRole(authentication)) {
            return new UserOrgStoreIdentity(IdentityId.of(authentication.getName()), "*", roles);
        } else {
            Map<String, Object> claims = ((Jwt) authentication.getPrincipal()).getClaims();
            String adminOrg = ((String) claims.get("org"));
            String adminStore = ((String) claims.get("store"));
            return new UserOrgStoreIdentity(IdentityId.of(adminOrg), adminStore, roles);
        }
    }

}

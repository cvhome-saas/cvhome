package com.asrevo.cvhome.s2s.utils;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;

public class SecurityUtils {

    public static final String CLAIMS_ORG_KEY = "org";

    public static final String CLAIMS_STORE_KEY = "store";

    private static final String WILD_CARD_STORE_ACCESS = "*";

    private SecurityUtils() {
    }

    public static boolean hasSuperAdminRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_SUPER_ADMIN);
    }

    public static boolean hasOrgAdminRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_ORG_ADMIN);
    }

    public static boolean hasStoreAdminRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_STORE_ADMIN);
    }

    public static boolean hasStoreModeratorRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_STORE_MODERATOR);
    }

    public static boolean hasStoreCustomerRole(Authentication authentication) {
        return hasRole(authentication, Roles.ROLE_CUSTOMER);
    }

    public static boolean hasScopeStoreCore(Authentication authentication) {
        return hasRole(authentication, Roles.SCOPE_STORE_CORE);
    }

    public static boolean hasScopeStorePod(Authentication authentication) {
        return hasRole(authentication, Roles.SCOPE_STORE_POD);
    }

    public static boolean hasRole(Authentication authentication, Roles role) {
        return getRoles(authentication).contains(role.name());
    }

    public static Set<String> getRoles(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    public static UserOrgStoreIdentity getOrgStoreIdentity(Authentication authentication) {
        Set<Roles> roles = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .map(Roles::parse)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (hasSuperAdminRole(authentication)) {
            return new UserOrgStoreIdentity(null, WILD_CARD_STORE_ACCESS, roles);
        } else if (hasScopeStoreCore(authentication)) {
            return new UserOrgStoreIdentity(null, WILD_CARD_STORE_ACCESS, roles);
        } else if (hasOrgAdminRole(authentication)) {
            Map<String, Object> claims = ((Jwt) authentication.getPrincipal()).getClaims();
            String adminOrg = ((String) claims.get(CLAIMS_ORG_KEY));
            return new UserOrgStoreIdentity(new ManagerOrgId(adminOrg), WILD_CARD_STORE_ACCESS, roles);
        } else {
            Map<String, Object> claims = ((Jwt) authentication.getPrincipal()).getClaims();
            String adminOrg = ((String) claims.get(CLAIMS_ORG_KEY));
            String adminStore = ((String) claims.get(CLAIMS_STORE_KEY));
            return new UserOrgStoreIdentity(new ManagerOrgId(adminOrg), adminStore, roles);
        }
    }

}

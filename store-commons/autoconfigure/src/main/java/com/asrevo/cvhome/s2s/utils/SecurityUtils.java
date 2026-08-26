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
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.s2s.jwt.RealmAwareJwtGrantedAuthoritiesConverter;

public final class SecurityUtils {

    public static final String CLAIMS_ORG_KEY = "org";

    public static final String CLAIMS_STORE_KEY = "store";

    /** The realm that mints staff and service tokens. */
    public static final String REALM_UAA = "uaa";

    /** The realm that mints shopper tokens. */
    public static final String REALM_CUA = "cua";

    /**
     * Stands for "every store", for a principal that is not confined to one. Deliberately not a valid store id — a
     * sentinel that can never collide with a real one — and deliberately kept here rather than on
     * {@link StoreMerchantId}, which is a tenant identifier and has no business knowing about authorization.
     */
    private static final StoreMerchantId WILD_CARD_STORE_ACCESS = new StoreMerchantId("*");

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

    /**
     * Whether this principal is known to come from an identity server <em>other</em> than {@code realm}.
     *
     * <p>
     * Deliberately not "is from realm X". A principal carries a {@code REALM_} authority only where realms are
     * configured, so asking the positive question would refuse every token under the legacy flat trust list and
     * under Boot's single-issuer support. Asking the negative one refuses a shopper token presented for a staff
     * check where the realm is known, and stays out of the way where it is not.
     * </p>
     */
    public static boolean isForeignRealm(Authentication authentication, String realm) {
        String expected = RealmAwareJwtGrantedAuthoritiesConverter.REALM_AUTHORITY_PREFIX + realm;
        Set<String> roles = getRoles(authentication);
        return roles.stream().anyMatch(it -> it.startsWith(RealmAwareJwtGrantedAuthoritiesConverter
                .REALM_AUTHORITY_PREFIX)) && !roles.contains(expected);
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
            String adminOrg = (String) claims.get(CLAIMS_ORG_KEY);
            return new UserOrgStoreIdentity(new ManagerOrgId(adminOrg), WILD_CARD_STORE_ACCESS, roles);
        } else {
            Map<String, Object> claims = ((Jwt) authentication.getPrincipal()).getClaims();
            String adminOrg = (String) claims.get(CLAIMS_ORG_KEY);
            String adminStore = (String) claims.get(CLAIMS_STORE_KEY);
            return new UserOrgStoreIdentity(new ManagerOrgId(adminOrg),
                    adminStore == null ? null : new StoreMerchantId(adminStore), roles);
        }
    }

}

package com.asrevo.cvhome.s2s.services;

import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.s2s.utils.SecurityUtils;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.s2s.utils.SecurityUtils.getOrgStoreIdentity;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.getRoles;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.hasOrgAdminRole;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.hasScopeStoreCore;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.hasScopeStorePod;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.hasStoreAdminRole;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.hasStoreCustomerRole;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.hasStoreModeratorRole;
import static com.asrevo.cvhome.s2s.utils.SecurityUtils.hasSuperAdminRole;

@Slf4j
public class StoreRoleAccessChecker {

    /**
     * Whether this principal came from the wrong identity server for the check being made.
     *
     * <p>
     * The role checks below are claim-based, and both authorization servers write their roles into the same
     * {@code roles} claim — so a shopper token and a staff token were told apart only by what they happened to
     * claim. The realm cap in the authorities converter is the primary guard; this is the second one, and it
     * makes the intent legible at the point of the check rather than implicit in configuration.
     * </p>
     */
    private static boolean wrongRealm(Authentication authentication, String realm) {
        if (SecurityUtils.isForeignRealm(authentication, realm)) {
            log.warn("Principal {} was issued by a realm other than '{}'; refusing the check.",
                    authentication.getName(), realm);
            return true;
        }
        return false;
    }

    private static String getResource(JwtAuthenticationToken authentication) {
        return authentication.getTokenAttributes().getOrDefault("resource", "").toString();
    }

    public boolean isSuperAdmin(Authentication authentication) {
        if (wrongRealm(authentication, SecurityUtils.REALM_UAA)) {
            return false;
        }
        return hasSuperAdminRole(authentication);
    }

    public boolean isOrgAdmin(Authentication authentication, StoreMerchantId requestedStoreId) {
        return isOrgAdmin(authentication, requestedStoreId, null);
    }

    public boolean isStoreAdmin(Authentication authentication, StoreMerchantId requestedStoreId) {
        return isStoreAdmin(authentication, requestedStoreId, null);
    }

    public boolean isStoreModerator(Authentication authentication, StoreMerchantId requestedStoreId) {
        return isStoreModerator(authentication, requestedStoreId, null);
    }

    @SuppressWarnings("java:S1172")
    public boolean isOrgAdmin(Authentication authentication, StoreMerchantId requestedStoreId, Pod pod) {
        if (wrongRealm(authentication, SecurityUtils.REALM_UAA)) {
            return false;
        }
        if (!hasOrgAdminRole(authentication)) {
            return false;
        }
        UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
        if (!isPodAllowOrg(pod, identity.org())) {
            log.debug("User {} does not have org admin role with roles {} on pod {} not allowed for org {}",
                    authentication.getName(), getRoles(authentication), pod.name(), identity.org().id().toString());
            return false;
        }
        // @TODO find better way to know if requested store created by this org admin
        return true;
    }

    public boolean isStoreAdmin(Authentication authentication, StoreMerchantId requestedStoreId, Pod pod) {
        if (wrongRealm(authentication, SecurityUtils.REALM_UAA)) {
            return false;
        }
        if (!hasStoreAdminRole(authentication)) {
            log.debug("User {} does not have store admin role with roles {}", authentication.getName(),
                    getRoles(authentication));
            return false;
        }
        UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
        if (!isPodAllowOrg(pod, identity.org())) {
            log.debug("User {} does not have store admin role with roles {} on pod {} not allowed for org {}",
                    authentication.getName(), getRoles(authentication), pod.name(), identity.org().id().toString());
            return false;
        }
        if (!requestedStoreId.equals(identity.store())) {
            log.debug(
                    "User {} does not have store admin role with roles {} because request store {} not match identity store {}",
                    authentication.getName(), getRoles(authentication), requestedStoreId, identity.store());
            return false;
        }
        return true;
    }

    public boolean isStoreModerator(Authentication authentication, StoreMerchantId requestedStoreId, Pod pod) {
        if (wrongRealm(authentication, SecurityUtils.REALM_UAA)) {
            return false;
        }
        if (!hasStoreModeratorRole(authentication)) {
            log.debug("User {} does not have store moderator role with roles {}", authentication.getName(),
                    getRoles(authentication));
            return false;
        }
        UserOrgStoreIdentity identity = getOrgStoreIdentity(authentication);
        if (!isPodAllowOrg(pod, identity.org())) {
            log.debug("User {} does not have store moderator role with roles {} on pod {} not allowed for org {}",
                    authentication.getName(), getRoles(authentication), pod.name(), identity.org().id().toString());
            return false;
        }
        if (!requestedStoreId.equals(identity.store())) {
            log.debug(
                    "User {} does not have store moderator role with roles {} because request store {} not match identity store {}",
                    authentication.getName(), getRoles(authentication), requestedStoreId, identity.store());
            return false;
        }
        return true;
    }

    private boolean isPodAllowOrg(Pod pod, ManagerOrgId orgId) {
        if (Objects.isNull(pod)) {
            return true;
        }
        if (Objects.isNull(pod.orgId())) {
            return true;
        }
        if (pod.orgId().equals(orgId)) {
            return true;
        } else {
            log.debug("Pod {} does not allow org {}", pod.name(), orgId);
            return false;
        }

    }

    public boolean isStoreCustomer(Authentication authentication, StoreMerchantId requestedStoreId) {
        if (wrongRealm(authentication, SecurityUtils.REALM_CUA)) {
            return false;
        }
        if (!hasStoreCustomerRole(authentication)) {
            log.debug("User {} does not have store customer role with roles {}", authentication.getName(),
                    getRoles(authentication));
            return false;
        }
        if (!(authentication instanceof JwtAuthenticationToken auth)) {
            return false;
        }
        String storeId = getStoreId(auth);
        if (!storeId.equals(requestedStoreId.getId())) {
            log.debug(
                    "User {} does not have store customer role with roles {} because requested store {} not match identity store {}",
                    authentication.getName(), getRoles(authentication), requestedStoreId, storeId);
            return false;
        }
        return true;
    }

    public boolean isScopeStoreCore(Authentication authentication) {
        if (wrongRealm(authentication, SecurityUtils.REALM_UAA)) {
            return false;
        }
        if (!hasScopeStoreCore(authentication)) {
            log.debug("User {} does not have store core scope with roles {}", authentication.getName(),
                    getRoles(authentication));
            return false;
        }
        return true;
    }

    public boolean isScopeStorePod(Authentication authentication, Pod pod) {
        if (wrongRealm(authentication, SecurityUtils.REALM_UAA)) {
            return false;
        }
        if (Objects.isNull(pod)) {
            log.debug("pod is null, cannot check internal scope");
            return false;
        }
        if (!hasScopeStorePod(authentication)) {
            log.debug("User {} does not have internal scope with roles {} pod {}", authentication.getName(),
                    getRoles(authentication), pod);
            return false;
        }
        if (!(authentication instanceof JwtAuthenticationToken auth)) {
            return false;
        }
        String resource = getResource(auth);
        if (!resource.equals(pod.name())) {
            log.debug("User {} does not have internal scope with roles {} on resource {} not matched pod name {}",
                    authentication.getName(), getRoles(authentication), resource, pod.name());
            return false;
        }

        return true;
    }

    /**
     * The store a shopper token belongs to.
     *
     * <p>
     * The {@code realm} claim, which is what the authorization server calls the user pool a token was minted
     * against. It used to be read from {@code clientId}, which happened to hold the same value because a store had
     * exactly one client — a coincidence of the old shape, and one that would have quietly stopped being true the
     * first time a store was given a second client (a mobile app on the same shopper pool). The name now says what
     * the value means.
     * </p>
     */
    private String getStoreId(JwtAuthenticationToken authentication) {
        return authentication.getTokenAttributes().getOrDefault(SecurityUtils.USER_REALM_CLAIM, "").toString();
    }

}

package com.asrevo.cvhome.s2s.services;

import java.util.Objects;

import org.springframework.security.core.Authentication;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.s2s.utils.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class PermissionAccessChecker {

    private final StoreRoleAccessChecker storeRoleAccessChecker = new StoreRoleAccessChecker();

    public boolean hasAccessOnStoreUsersList(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasReadAccessOnStore(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersCreate(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersUpdate(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersDelete(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersEnable(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersDisable(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreFindOne(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasReadAccessOnStore(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreCreate(Authentication authentication, String org, Pod pod) {
        if (!storeRoleAccessChecker.isScopeStoreCore(authentication)) {
            log.debug("User {} does not have store scope with roles {}", authentication.getName(),
                    SecurityUtils.getRoles(authentication));
            return false;
        }
        if (Objects.nonNull(pod) && Objects.nonNull(pod.orgId())) {
            log.debug("will check Org match pod org");
            if (!pod.orgId().id().toString().equals(org)) {
                log.debug("Org {} does not match pod org {}", org, pod.orgId().id());
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("java:S1172")
    public boolean isSameStorePod(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod) {
        if (storeRoleAccessChecker.isScopeStorePod(authentication, pod)) {
            return true;
        }
        log.info("User {} does not have store pod scope with roles {}", authentication.getName(),
                SecurityUtils.getRoles(authentication));
        return false;
    }

    public boolean hasManageAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod) {
        if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId, pod)) {
            return true;
        } else if (storeRoleAccessChecker.isStoreAdmin(authentication, requestedStoreId, pod)) {
            return true;
        }

        log.info("User {} does not have manage access on store {}", authentication.getName(), requestedStoreId);
        return false;
    }

    public boolean hasAccessOnStoreDelete(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnStore(authentication, requestedStoreId);
    }

    private boolean hasReadAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId)) {
            return true;
        } else if (storeRoleAccessChecker.isStoreAdmin(authentication, requestedStoreId)) {
            return true;
        } else if (storeRoleAccessChecker.isStoreModerator(authentication, requestedStoreId)) {
            return true;
        } else if (storeRoleAccessChecker.isScopeStoreCore(authentication)) {
            return true;
        } else {
            log.debug("User {} does not have read access on store {} on roles {}", authentication.getName(),
                    requestedStoreId, SecurityUtils.getRoles(authentication));
            return false;
        }
    }

    @SuppressWarnings("java:S1144")
    private boolean hasReadAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId, Pod pod) {
        if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId, pod)) {
            return true;
        } else if (storeRoleAccessChecker.isStoreAdmin(authentication, requestedStoreId, pod)) {
            return true;
        } else if (storeRoleAccessChecker.isStoreModerator(authentication, requestedStoreId, pod)) {
            return true;
        } else if (storeRoleAccessChecker.isScopeStoreCore(authentication)) {
            return true;
        } else if (isSameStorePod(authentication, requestedStoreId, pod)) {
            return true;
        } else {
            log.debug("User {} does not have read access on store {} on roles {} on pod {}", authentication.getName(),
                    requestedStoreId, SecurityUtils.getRoles(authentication), pod);
            return false;
        }
    }

    private boolean hasMaintainAccessOnStore(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId)) {
            return true;
        }
        log.debug("User {} does not have maintain access on store {} on roles {}", authentication.getName(),
                requestedStoreId, SecurityUtils.getRoles(authentication));
        return false;
    }

    private boolean hasMaintainAccessOnUsers(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId)) {
            return true;
        } else if (storeRoleAccessChecker.isStoreAdmin(authentication, requestedStoreId)) {
            return true;
        } else {
            log.debug("User {} does not have maintain access on users on store {} on roles {}",
                    authentication.getName(), requestedStoreId, SecurityUtils.getRoles(authentication));
            return false;
        }
    }

    /**
     * Reading a store's own billing — what it is on, when it renews, its invoices. Same audience as any other read of
     * the store, so a moderator can see the plan they are working under without being able to change what it costs.
     */
    public boolean hasAccessOnBillingRead(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasReadAccessOnStore(authentication, requestedStoreId);
    }

    /**
     * Buying, upgrading, downgrading or cancelling. Restricted to the org admin: spending money is an org-level act,
     * and a store admin is not the person who owns the card.
     */
    public boolean hasAccessOnBillingManage(Authentication authentication, ManagerStoreId requestedStoreId) {
        return hasMaintainAccessOnStore(authentication, requestedStoreId);
    }

    /**
     * Reading a store's entitlement snapshot. Wider than {@link #hasAccessOnBillingRead}: the pods enforce those
     * ceilings, so a store-pod principal has to be able to ask, not only a human.
     */
    public boolean hasAccessOnBillingEntitlementRead(Authentication authentication, ManagerStoreId requestedStoreId) {
        // The scope is checked directly rather than through isScopeStorePod, which additionally requires the caller's
        // resource to match a pod — and returns false outright when handed a null one. Billing is a store-core
        // service and has no pod, so routing through it denied every pod that asked, which is every service that
        // enforces these ceilings.
        if (storeRoleAccessChecker.isScopeStoreCore(authentication) || SecurityUtils.hasScopeStorePod(authentication)) {
            return true;
        }
        return hasReadAccessOnStore(authentication, requestedStoreId);
    }

    /**
     * Asking billing whether an org may create another store, and provisioning the subscription that follows. A
     * service-to-service call only — no human ever asks this directly, which is why there is no store to check.
     */
    public boolean hasAccessOnBillingQuotaCheck(Authentication authentication) {
        if (storeRoleAccessChecker.isScopeStoreCore(authentication)) {
            return true;
        }
        log.debug("User {} does not have store-core scope for a billing quota check with roles {}",
                authentication.getName(), SecurityUtils.getRoles(authentication));
        return false;
    }

    public boolean isCustomerInSameStore(Authentication authentication, ManagerStoreId requestedStoreId) {
        if (!storeRoleAccessChecker.isStoreCustomer(authentication, requestedStoreId)) {
            log.debug("Customer {} does not have maintain access on customer on store {} on roles {}",
                    authentication.getName(), requestedStoreId, SecurityUtils.getRoles(authentication));
            return false;
        }
        return true;
    }

}

package com.asrevo.cvhome.s2s.services;

import java.util.Objects;

import org.springframework.security.core.Authentication;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.utils.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class PermissionAccessChecker {

    private final StoreRoleAccessChecker storeRoleAccessChecker = new StoreRoleAccessChecker();

    public boolean hasAccessOnStoreUsersList(Authentication authentication, StoreMerchantId requestedStoreId) {
        return hasReadAccessOnStore(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersCreate(Authentication authentication, StoreMerchantId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersUpdate(Authentication authentication, StoreMerchantId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersDelete(Authentication authentication, StoreMerchantId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersEnable(Authentication authentication, StoreMerchantId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreUsersDisable(Authentication authentication, StoreMerchantId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    /**
     * Setting another user's password. Deliberately the maintain audience rather than the read one: a moderator can
     * see who has access to a store without being able to take it over.
     */
    public boolean hasAccessOnStoreUsersResetPassword(Authentication authentication, StoreMerchantId requestedStoreId) {
        return hasMaintainAccessOnUsers(authentication, requestedStoreId);
    }

    public boolean hasAccessOnStoreFindOne(Authentication authentication, StoreMerchantId requestedStoreId) {
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
    public boolean isSameStorePod(Authentication authentication, StoreMerchantId requestedStoreId, Pod pod) {
        if (storeRoleAccessChecker.isScopeStorePod(authentication, pod)) {
            return true;
        }
        log.info("User {} does not have store pod scope with roles {}", authentication.getName(),
                SecurityUtils.getRoles(authentication));
        return false;
    }

    public boolean hasManageAccessOnStore(Authentication authentication, StoreMerchantId requestedStoreId, Pod pod) {
        if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId, pod)) {
            return true;
        } else if (storeRoleAccessChecker.isStoreAdmin(authentication, requestedStoreId, pod)) {
            return true;
        }

        log.info("User {} does not have manage access on store {}", authentication.getName(), requestedStoreId);
        return false;
    }

    public boolean hasAccessOnStoreDelete(Authentication authentication, StoreMerchantId requestedStoreId) {
        return hasMaintainAccessOnStore(authentication, requestedStoreId);
    }

    private boolean hasReadAccessOnStore(Authentication authentication, StoreMerchantId requestedStoreId) {
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

    public boolean hasReadAccessOnStore(Authentication authentication, StoreMerchantId requestedStoreId, Pod pod) {
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

    private boolean hasMaintainAccessOnStore(Authentication authentication, StoreMerchantId requestedStoreId) {
        if (storeRoleAccessChecker.isOrgAdmin(authentication, requestedStoreId)) {
            return true;
        }
        log.debug("User {} does not have maintain access on store {} on roles {}", authentication.getName(),
                requestedStoreId, SecurityUtils.getRoles(authentication));
        return false;
    }

    private boolean hasMaintainAccessOnUsers(Authentication authentication, StoreMerchantId requestedStoreId) {
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
     *
     * <p>
     * <strong>Plus the platform operator.</strong> "This merchant says they paid" is the support question a platform
     * console exists to answer, and billing refused a super admin for every store on the platform because
     * {@link #hasReadAccessOnStore} has no super-admin branch — it admits an org admin, a store admin, a store
     * moderator and a store-core service principal, and stops. The branch goes here rather than in that method so
     * that <em>only</em> billing widens: the merchant screens a platform operator has no business in stay 403,
     * which is what the console's {@code platformOnly} guard already assumes. Same shape as
     * {@link #hasAccessOnPodRead}, which billing simply never adopted.
     * </p>
     */
    public boolean hasAccessOnBillingRead(Authentication authentication, StoreMerchantId requestedStoreId) {
        return storeRoleAccessChecker.isSuperAdmin(authentication) || hasReadAccessOnStore(authentication,
                requestedStoreId);
    }

    /**
     * Buying, upgrading, downgrading or cancelling. Restricted to the org admin: spending money is an org-level act,
     * and a store admin is not the person who owns the card.
     *
     * <p>
     * And to the platform operator, who is the only caller {@code SubscriptionService.cancel} will accept an
     * immediate cancellation from — a branch that has existed since it was written and that no client could reach.
     * Every act through this token writes a {@code subscription_audit} row naming the principal, which is the reason
     * the actor is threaded through {@code SubscriptionApi} rather than left null.
     * </p>
     */
    public boolean hasAccessOnBillingManage(Authentication authentication, StoreMerchantId requestedStoreId) {
        return storeRoleAccessChecker.isSuperAdmin(authentication) || hasMaintainAccessOnStore(authentication,
                requestedStoreId);
    }

    /**
     * Reading a store's entitlement snapshot. Wider than {@link #hasAccessOnBillingRead}: the pods enforce those
     * ceilings, so a store-pod principal has to be able to ask, not only a human.
     */
    public boolean hasAccessOnBillingEntitlementRead(Authentication authentication, StoreMerchantId requestedStoreId) {
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

    /**
     * Reading the pod registry. Open to super admins, org admins and store-core service principals — the gateway is
     * a service principal and rebuilds its route table from the pod list every minute, so excluding it would take
     * tenant routing down. An org admin is admitted here but the registry still returns only its own private pods.
     */
    public boolean hasAccessOnPodRead(Authentication authentication) {
        if (storeRoleAccessChecker.isSuperAdmin(authentication)
                || storeRoleAccessChecker.isScopeStoreCore(authentication)
                || SecurityUtils.hasOrgAdminRole(authentication)) {
            return true;
        }
        log.debug("User {} may not read the pod registry with roles {}", authentication.getName(),
                SecurityUtils.getRoles(authentication));
        return false;
    }

    /**
     * Creating, updating, draining or deleting a pod. Super admin only: a pod is platform infrastructure, and
     * deleting one orphans every store placed on it.
     */
    public boolean hasAccessOnPodManage(Authentication authentication) {
        if (storeRoleAccessChecker.isSuperAdmin(authentication)) {
            return true;
        }
        log.debug("User {} may not manage pods with roles {}", authentication.getName(),
                SecurityUtils.getRoles(authentication));
        return false;
    }

    /**
     * Asking the registry where a new store should be placed. A service-to-service call only — tenancy asks on
     * behalf of an org before the store exists, so there is nothing to scope against and no human ever asks it
     * directly. Same shape as {@link #hasAccessOnBillingQuotaCheck}.
     */
    public boolean hasAccessOnPodPlacement(Authentication authentication) {
        if (storeRoleAccessChecker.isScopeStoreCore(authentication)) {
            return true;
        }
        log.debug("User {} does not have store-core scope for a pod placement with roles {}",
                authentication.getName(), SecurityUtils.getRoles(authentication));
        return false;
    }

    public boolean isCustomerInSameStore(Authentication authentication, StoreMerchantId requestedStoreId) {
        if (!storeRoleAccessChecker.isStoreCustomer(authentication, requestedStoreId)) {
            log.debug("Customer {} does not have maintain access on customer on store {} on roles {}",
                    authentication.getName(), requestedStoreId, SecurityUtils.getRoles(authentication));
            return false;
        }
        return true;
    }

}

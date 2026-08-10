package com.asrevo.cvhome.billing.api.v1;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.dto.SubscriptionView;
import com.asrevo.cvhome.billing.commons.errors.SubscriptionNotFoundException;
import com.asrevo.cvhome.billing.service.SubscriptionService;
import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;

import lombok.RequiredArgsConstructor;

/**
 * A store's own subscription.
 *
 * <p>
 * Store-core convention, not the pod one: the store arrives as an explicit {@code store} parameter typed
 * {@link ManagerStoreId}, and every method is gated on it. The permission evaluator denies by default, so a token
 * with no {@code case} behind it 403s silently — which is why {@code STORE-CORE.BILLING.*} exists in both
 * {@code CustomPermissionEvaluator} and {@code PermissionAccessChecker}.
 * </p>
 *
 * <p>
 * Reading and managing are separate tokens on purpose. A store moderator should be able to see the plan they work
 * under; spending the org's money is a different act, and belongs to whoever owns the card.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionApi {

    private final SubscriptionService subscriptionService;

    /**
     * What the store is on, when it renews, and anything already scheduled to change.
     *
     * @throws SubscriptionNotFoundException billing has never seen this store, or it is not this caller's to see
     */
    @GetMapping("current")
    @PreAuthorize("hasPermission(#store,'ManagerStoreId','STORE-CORE.BILLING.READ')")
    public SubscriptionView current(@OrgStorePrincipalInfo UserOrgStoreIdentity identity,
                                    @RequestParam("store") ManagerStoreId store)
            throws SubscriptionNotFoundException {
        return subscriptionService.current(store, tenantScopeOf(identity));
    }

    /**
     * The org a read must be confined to, or {@code null} for a caller entitled to span orgs.
     *
     * <p>
     * Belt and braces on purpose. {@code @PreAuthorize} is the first gate, but the shared
     * {@code StoreRoleAccessChecker} cannot currently tell which org a store belongs to — it carries a {@code TODO}
     * saying so and returns true for any store once the caller holds the org-admin role. Billing does know, because
     * every subscription row records its org, so it narrows the query rather than relying on that check alone. Remove
     * this and one org's admin can read another org's spend.
     * </p>
     */
    private ManagerOrgId tenantScopeOf(UserOrgStoreIdentity identity) {
        return identity.isSuperAdmin() ? null : identity.org();
    }

}

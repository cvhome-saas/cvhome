package com.asrevo.cvhome.tenancy.commons.dto;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;

/**
 * A store as the seller console sees it.
 *
 * <p>
 * {@code billingStatus} is read from billing rather than stored here — billing owns it, and a copy in this table
 * would be a second source of truth to keep in step. It is {@code null} when billing could not be reached, which
 * callers must render as "unknown" rather than as a problem: a billing outage is not a reason to tell a merchant
 * their store has lapsed.
 * </p>
 *
 * @param provisioningState how far the store got in being built, which is unrelated to whether it is paid for
 * @param status            whether it may be used at all — an operator's lever, unlike provisioningState
 * @param billingStatus     the standing of its subscription, or {@code null} if unknown
 */
public record ManagerStoreDto(ManagerStoreId id, String name, ManagerOrgId orgId, PodId podId,
                              ProvisioningState provisioningState, StoreStatus status,
                              SubscriptionStatus billingStatus) {

    /**
     * The same store with its billing standing filled in.
     *
     * <p>
     * A static factory rather than a fluent {@code withX}/{@code billedAs} instance method: MapStruct reads any
     * single-argument method returning this type as a setter for a target property, and warns about one it cannot
     * map.
     * </p>
     */
    public static ManagerStoreDto billed(ManagerStoreDto store, SubscriptionStatus status) {
        return new ManagerStoreDto(store.id(), store.name(), store.orgId(), store.podId(),
                store.provisioningState(), store.status(), status);
    }

}

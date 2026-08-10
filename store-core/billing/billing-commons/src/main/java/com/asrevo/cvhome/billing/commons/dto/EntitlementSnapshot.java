package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

import com.asrevo.cvhome.billing.commons.EntitlementKey;
import com.asrevo.cvhome.billing.commons.EntitlementValue;
import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;

/**
 * Everything another service needs to decide whether a store may do something, in one call.
 *
 * <p>
 * Deliberately a snapshot rather than a query API: callers cache it for a short TTL and answer locally, because a
 * per-request hop to billing would put a billing outage in the path of every write in the platform.
 * </p>
 *
 * @param store             the store this describes
 * @param status            its subscription state
 * @param operable          whether the store may be worked in — the question most callers actually have
 * @param planCode          the plan handle, or null when the store has never had a plan
 * @param currentPeriodEnd  when the paid period ends, i.e. the next renewal date
 * @param entitlements      the ceilings and capabilities the plan grants
 */
public record EntitlementSnapshot(ManagerStoreId store, SubscriptionStatus status, boolean operable, String planCode,
                                  Instant currentPeriodEnd, Map<EntitlementKey, EntitlementValue> entitlements)
        implements Serializable {

    /**
     * The snapshot a caller falls back to when billing cannot be reached.
     *
     * <p>
     * Deliberately permissive: an outage in billing must not stop a paying merchant from taking orders. The opposite
     * choice is made at store creation, where being unable to check means refusing — see the quota client.
     * </p>
     */
    public static EntitlementSnapshot degradedOpen(ManagerStoreId store) {
        return new EntitlementSnapshot(store, SubscriptionStatus.ACTIVE, true, null, null, Map.of());
    }

    /**
     * What {@code key} is worth here, treating an absent key as unlimited rather than as zero.
     */
    public EntitlementValue entitlement(EntitlementKey key) {
        return entitlements.getOrDefault(key, EntitlementValue.unlimited(key));
    }

}

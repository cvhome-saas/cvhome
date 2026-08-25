package com.asrevo.cvhome.billing.commons.dto.admin;

import java.io.Serializable;

import com.asrevo.cvhome.billing.commons.SubscriptionStatus;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;

/**
 * What narrows the platform's subscription register. Every field is optional; all of them absent is the whole
 * platform.
 *
 * @param org         one organization's subscriptions only
 * @param status      one lifecycle state only
 * @param planCode    one plan only, by its stable handle rather than its id
 * @param term        a case-insensitive substring of the store id
 * @param blockedOnly only the stores no enforcement layer lets through
 */
public record ListSubscriptionQuery(ManagerOrgId org, SubscriptionStatus status, String planCode, String term,
                                    Boolean blockedOnly) implements Serializable {

    /**
     * Boxed with a default rather than a bare {@code boolean}.
     *
     * <p>
     * Every other field of this filter is optional and absent means "do not narrow on it". A primitive here made
     * {@code blockedOnly} the one exception: Jackson refuses to map an absent or null value onto a primitive, so a
     * body that simply left the flag out — {@code {}}, or {@code {"status":"ACTIVE"}} — was rejected as
     * unreadable with a 400 that named nothing. The console happens to send it on every call, which is why nobody
     * noticed, but {@code PlatformBillingServiceImpl} already builds a filter with it false when the whole body is
     * absent, so an omitted flag was always meant to mean the same thing.
     * </p>
     */
    public ListSubscriptionQuery {
        blockedOnly = blockedOnly != null && blockedOnly;
    }
}

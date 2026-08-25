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
                                    boolean blockedOnly) implements Serializable {
}

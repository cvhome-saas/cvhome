package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Asks billing to give a freshly created store a subscription.
 *
 * <p>
 * Idempotent: a store that already has one gets its existing subscription back rather than a second. It has to be —
 * this arrives from an outbox handler, which retries.
 * </p>
 *
 * @param org   the org that owns the store
 * @param store the store to provision
 */
public record ProvisionSubscriptionRequest(ManagerOrgId org, StoreMerchantId store) implements Serializable {
}

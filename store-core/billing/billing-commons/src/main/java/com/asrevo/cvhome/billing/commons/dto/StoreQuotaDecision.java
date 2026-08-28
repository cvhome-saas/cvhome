package com.asrevo.cvhome.billing.commons.dto;

import java.io.Serializable;

/**
 * Whether an org may create another store, and what it would get.
 *
 * @param allowed           whether to proceed
 * @param reason            why not, when refused
 * @param trialAvailable    whether the org still has its one trial to spend, which decides whether the new store
 *                          starts in a trial or unpaid
 * @param pendingStoreCount how many of the org's stores have never been paid for
 */
public record StoreQuotaDecision(boolean allowed, String reason, boolean trialAvailable, int pendingStoreCount)
        implements Serializable {

    public static StoreQuotaDecision allow(boolean trialAvailable, int pendingStoreCount) {
        return new StoreQuotaDecision(true, null, trialAvailable, pendingStoreCount);
    }

    public static StoreQuotaDecision refuse(String reason, boolean trialAvailable, int pendingStoreCount) {
        return new StoreQuotaDecision(false, reason, trialAvailable, pendingStoreCount);
    }

}

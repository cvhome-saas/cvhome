package com.asrevo.cvhome.gateway.impersonation;

import com.asrevo.cvhome.gateway.errors.ImpersonationInvalidException;

/**
 * What an operator asks for: who to act as, in which store, how, and why.
 *
 * @param userId  the target's uaa account id
 * @param storeId the store to act in — always concrete, because read mode is store-scoped
 * @param mode    {@code read} or {@code write}; uaa decides whether this operator may have the second
 * @param reason  free text, required: it is what the audit row says this was for
 */
public record StartImpersonation(String userId, String storeId, String mode, String reason) {

    /** Validation by hand: the gateway carries no bean-validation starter, and four fields do not earn one. */
    public StartImpersonation validated() throws ImpersonationInvalidException {
        require(userId, "userId");
        require(storeId, "storeId");
        require(mode, "mode");
        require(reason, "reason");
        return new StartImpersonation(userId.trim(), storeId.trim(), mode.trim(), reason.trim());
    }

    private static void require(String value, String field) throws ImpersonationInvalidException {
        if (value == null || value.isBlank()) {
            throw ImpersonationInvalidException.missing(field);
        }
    }

}

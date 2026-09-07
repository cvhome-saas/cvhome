package com.asrevo.cvhome.gateway.impersonation;

import com.asrevo.cvhome.gateway.errors.ImpersonationInvalidException;

/**
 * What an operator asks for: who to act as, and why. The session then is that account — its own org, stores and
 * roles, whatever they are; an org admin with no store yet is still an account worth seeing the console as.
 *
 * @param userId the target's uaa account id
 * @param reason free text, required: it is what the audit row says this was for
 */
public record StartImpersonation(String userId, String reason) {

    /** Validation by hand: the gateway carries no bean-validation starter, and two fields do not earn one. */
    public StartImpersonation validated() throws ImpersonationInvalidException {
        require(userId, "userId");
        require(reason, "reason");
        return new StartImpersonation(userId.trim(), reason.trim());
    }

    private static void require(String value, String field) throws ImpersonationInvalidException {
        if (value == null || value.isBlank()) {
            throw ImpersonationInvalidException.missing(field);
        }
    }

}

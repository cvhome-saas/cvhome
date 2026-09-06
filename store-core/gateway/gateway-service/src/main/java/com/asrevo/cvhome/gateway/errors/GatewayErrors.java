package com.asrevo.cvhome.gateway.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes for the gateway's own endpoints — the session it holds, and what an operator may do with it.
 *
 * <p>
 * Routing failures are not in this enum: a backend that cannot be reached is the proxy's 503, not ours. These
 * describe the two things the gateway decides for itself: whether there is a signed-in session, and impersonation.
 * </p>
 */
public enum GatewayErrors implements ErrorCode {

    /** The endpoint needs a signed-in console session and the request carried none. */
    SESSION_REQUIRED("GATEWAY.SESSION.REQUIRED", ErrorCategory.UNAUTHENTICATED),

    /** The request body is missing a field the impersonation needs, or a field is malformed. */
    IMPERSONATION_INVALID("GATEWAY.IMPERSONATION.INVALID", ErrorCategory.VALIDATION),

    /** uaa refused the exchange — the operator may not, the target may not be, or the mode is not theirs to pick. */
    IMPERSONATION_REFUSED("GATEWAY.IMPERSONATION.REFUSED", ErrorCategory.FORBIDDEN),

    /** This session is already acting as somebody; end that first. */
    IMPERSONATION_ALREADY_ACTIVE("GATEWAY.IMPERSONATION.ALREADY_ACTIVE", ErrorCategory.CONFLICT),

    /** The named store is not one the target acts in, or is not operable. */
    IMPERSONATION_STORE_NOT_TARGETS("GATEWAY.IMPERSONATION.STORE_NOT_TARGETS", ErrorCategory.UNPROCESSABLE),

    /** uaa did not answer the exchange, so nothing was decided. */
    IMPERSONATION_UNAVAILABLE("GATEWAY.IMPERSONATION.UNAVAILABLE", ErrorCategory.REMOTE_SERVICE);

    private final String code;

    private final ErrorCategory category;

    GatewayErrors(String code, ErrorCategory category) {
        this.code = code;
        this.category = category;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

}

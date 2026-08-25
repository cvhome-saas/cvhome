package com.asrevo.cvhome.cua.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes for the cua context — the authorization server that authenticates storefront shoppers.
 *
 * <p>
 * Uniqueness here is scoped to a client, never global: two stores may each have a shopper called {@code jane}, because
 * a cua account belongs to the store that issued it. Both codes therefore carry the {@code clientId} alongside the
 * value that collided.
 * </p>
 */
public enum CuaErrors implements ErrorCode {

    /** A shopper account already exists with this username for this client. */
    USERNAME_TAKEN("CUA.REGISTRATION.USERNAME_TAKEN", ErrorCategory.CONFLICT),

    /** A shopper account already exists with this email address for this client. */
    EMAIL_TAKEN("CUA.REGISTRATION.EMAIL_TAKEN", ErrorCategory.CONFLICT);

    private final String code;

    private final ErrorCategory category;

    CuaErrors(String code, ErrorCategory category) {
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

package com.asrevo.cvhome.uaa.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes for the uaa context — the authorization server that authenticates staff, org owners and merchants.
 *
 * <p>
 * Codes here describe failures of uaa's <em>administration</em> API. Authentication and token failures are not in this
 * enum: those are Spring Security's to raise and OAuth2's to render, and they reach a client as the protocol's own
 * error responses rather than as this vocabulary.
 * </p>
 *
 * <p>
 * Kept apart from the storefront realm's codes on purpose: {@code cua} is a separate authorization server with its own
 * user table, so a "user not found" there is a different fact about a different population.
 * </p>
 */
public enum UaaErrors implements ErrorCode {

    /** No user exists with the given id. */
    USER_NOT_FOUND("UAA.USER.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /** No OAuth2 registered client exists with the given id. */
    CLIENT_NOT_FOUND("UAA.CLIENT.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * The target of the mutation is the built-in super administrator.
     *
     * <p>
     * {@code FORBIDDEN} rather than {@code UNPROCESSABLE}: the account is a fact about who may be changed, not about
     * the state of the data, and no caller — however privileged — is permitted to disable or delete the account that
     * grants privileges.
     * </p>
     */
    SUPER_ADMIN_IMMUTABLE("UAA.USER.SUPER_ADMIN_IMMUTABLE", ErrorCategory.FORBIDDEN);

    private final String code;

    private final ErrorCategory category;

    UaaErrors(String code, ErrorCategory category) {
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

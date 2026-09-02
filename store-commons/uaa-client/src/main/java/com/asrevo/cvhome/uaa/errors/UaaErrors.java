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

    /** Another registration already uses that {@code client_id}. */
    CLIENT_ID_TAKEN("UAA.CLIENT.ID_TAKEN", ErrorCategory.CONFLICT),

    /** The operation needs a client that holds a secret; a public client has none to rotate. */
    CLIENT_NOT_CONFIDENTIAL("UAA.CLIENT.NOT_CONFIDENTIAL", ErrorCategory.UNPROCESSABLE),

    /** The requested access-token lifetime is longer than the realm allows. */
    CLIENT_TOKEN_TTL_EXCEEDS_POLICY("UAA.CLIENT.TOKEN_TTL_EXCEEDS_POLICY", ErrorCategory.VALIDATION),

    /** A redirect URI is not absolute, carries a fragment or a wildcard, or is plain HTTP on a host that is not local. */
    INVALID_REDIRECT_URI("UAA.CLIENT.INVALID_REDIRECT_URI", ErrorCategory.VALIDATION),

    /** No previous secret is still inside its grace window, so there is nothing to revoke. */
    CLIENT_NO_PREVIOUS_SECRET("UAA.CLIENT.NO_PREVIOUS_SECRET", ErrorCategory.NOT_FOUND),

    /**
     * The target of the mutation is the built-in super administrator.
     *
     * <p>
     * {@code FORBIDDEN} rather than {@code UNPROCESSABLE}: the account is a fact about who may be changed, not about
     * the state of the data, and no caller — however privileged — is permitted to disable or delete the account that
     * grants privileges.
     * </p>
     */
    SUPER_ADMIN_IMMUTABLE("UAA.USER.SUPER_ADMIN_IMMUTABLE", ErrorCategory.FORBIDDEN),

    /** No role exists with the given id or name. */
    ROLE_NOT_FOUND("UAA.ROLE.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * The role exists but may not be granted through the admin API — today only {@code SUPER_ADMIN}, which is held by
     * exactly one seeded account and is what grants every other privilege.
     */
    ROLE_NOT_ASSIGNABLE("UAA.ROLE.NOT_ASSIGNABLE", ErrorCategory.FORBIDDEN),

    /**
     * The caller authenticated as an OAuth2 client, not a person, and asked for something only a person has — a
     * profile, a session list, a password. A {@code client_credentials} token has no user behind it.
     */
    NOT_A_USER_PRINCIPAL("UAA.AUTH.NOT_A_USER_PRINCIPAL", ErrorCategory.FORBIDDEN),

    /** A role name that is not upper-case letters, digits and underscores. */
    ROLE_NAME_INVALID("UAA.ROLE.NAME_INVALID", ErrorCategory.VALIDATION),

    /** A role with that name already exists. */
    ROLE_NAME_TAKEN("UAA.ROLE.NAME_TAKEN", ErrorCategory.CONFLICT),

    /** The role is seeded by the platform: its name and scope are fixed and it cannot be deleted. */
    SYSTEM_ROLE_IMMUTABLE("UAA.ROLE.SYSTEM_IMMUTABLE", ErrorCategory.FORBIDDEN),

    /** The role is still held by at least one account, so deleting it would silently change their authority. */
    ROLE_IN_USE("UAA.ROLE.IN_USE", ErrorCategory.CONFLICT),

    /** Setting that parent would make the role inherit from itself, directly or through a chain. */
    ROLE_INHERITANCE_CYCLE("UAA.ROLE.INHERITANCE_CYCLE", ErrorCategory.UNPROCESSABLE),

    /** A permission key that is not in the catalogue. */
    PERMISSION_UNKNOWN("UAA.PERMISSION.UNKNOWN", ErrorCategory.VALIDATION),

    /** A settings value outside the range the server accepts. */
    SETTINGS_INVALID("UAA.SETTINGS.INVALID", ErrorCategory.VALIDATION),

    /** The settings were changed by someone else since the caller read them. */
    SETTINGS_CONFLICT("UAA.SETTINGS.CONFLICT", ErrorCategory.CONFLICT),

    /** The new password breaks the realm's policy; the field errors name each rule it breaks. */
    PASSWORD_POLICY_VIOLATION("UAA.PASSWORD.POLICY_VIOLATION", ErrorCategory.VALIDATION),

    /** The new password is one of the account's recent ones. */
    PASSWORD_REUSED("UAA.PASSWORD.REUSED", ErrorCategory.UNPROCESSABLE),

    /** The new password appears in a known breach corpus. */
    PASSWORD_COMPROMISED("UAA.PASSWORD.COMPROMISED", ErrorCategory.UNPROCESSABLE),

    /** A self-service password change named a current password that does not match. */
    CURRENT_PASSWORD_MISMATCH("UAA.PASSWORD.CURRENT_MISMATCH", ErrorCategory.VALIDATION),

    /** No session with that id belongs to the account. */
    SESSION_NOT_FOUND("UAA.SESSION.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /** Too many attempts from one address in the window. */
    RATE_LIMITED("UAA.AUTH.RATE_LIMITED", ErrorCategory.TOO_MANY_REQUESTS),

    /**
     * No usable invitation for that token — missing, expired, revoked or already accepted, one code for all four so
     * the public endpoint cannot be used to tell which tokens ever existed.
     */
    INVITATION_NOT_USABLE("UAA.INVITATION.NOT_USABLE", ErrorCategory.NOT_FOUND),

    /** The account already has a live invitation; resend rotates it instead. */
    INVITATION_ALREADY_PENDING("UAA.INVITATION.ALREADY_PENDING", ErrorCategory.CONFLICT),

    /** An invitation action on an account that is not pending — it already has a password. */
    USER_NOT_PENDING("UAA.USER.NOT_PENDING", ErrorCategory.UNPROCESSABLE),

    /** No usable reset link for that token; the same single code as {@link #INVITATION_NOT_USABLE}, for the same reason. */
    RESET_TOKEN_NOT_USABLE("UAA.PASSWORD.RESET_TOKEN_NOT_USABLE", ErrorCategory.NOT_FOUND),

    /** Another account already signs in with that username. */
    USERNAME_TAKEN("UAA.USER.USERNAME_TAKEN", ErrorCategory.CONFLICT),

    /** Another account already carries that email. */
    EMAIL_TAKEN("UAA.USER.EMAIL_TAKEN", ErrorCategory.CONFLICT);

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

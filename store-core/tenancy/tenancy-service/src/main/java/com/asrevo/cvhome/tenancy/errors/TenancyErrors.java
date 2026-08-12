package com.asrevo.cvhome.tenancy.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes for the tenancy context — organizations, stores, provisioning and the users that administer them.
 *
 * <p>
 * The tenancy codes below exist because tenancy is where tenancy is <em>enforced</em>: uaa stores {@code org}
 * and {@code store} as free-form user metadata and checks neither, so every guard that keeps one organization out of
 * another's data lives on this side of the call.
 * </p>
 */
public enum TenancyErrors implements ErrorCode {

    /** uaa has no user with the requested id, or returned nothing for it. */
    MANAGED_USER_NOT_FOUND("CONTROL_PLANE.USER.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /** The requested user belongs to a different organization than the caller administers. */
    USER_FOREIGN_ORG("CONTROL_PLANE.USER.FOREIGN_ORG", ErrorCategory.FORBIDDEN),

    /** The requested user belongs to a different store than the request targeted. */
    USER_FOREIGN_STORE("CONTROL_PLANE.USER.FOREIGN_STORE", ErrorCategory.FORBIDDEN),

    /**
     * No store with the requested id is visible to the caller — either it does not exist, or it belongs to another
     * organization. Deliberately one code for both: see {@link StoreNotFoundException}.
     */
    STORE_NOT_FOUND("CONTROL_PLANE.STORE.NOT_FOUND", ErrorCategory.NOT_FOUND);

    private final String code;

    private final ErrorCategory category;

    TenancyErrors(String code, ErrorCategory category) {
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

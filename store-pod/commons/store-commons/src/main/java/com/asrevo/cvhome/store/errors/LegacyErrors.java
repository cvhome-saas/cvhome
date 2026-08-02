package com.asrevo.cvhome.store.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Codes for failures that have not yet been given one of their own.
 *
 * <p>
 * Every exception in the deprecated hierarchy reports one of these, which is what lets ~275 un-migrated throw sites
 * render in the shared format — with the right status — before any of them are touched. The {@code LEGACY.} prefix is
 * deliberate: a client seeing one of these knows the endpoint has not been migrated yet, and a search for the prefix
 * shows exactly how much of the codebase is still on the old path.
 * </p>
 *
 * <p>
 * This enum disappears with the last legacy exception.
 * </p>
 */
public enum LegacyErrors implements ErrorCode {

    /**
     * Un-migrated failure with no more specific meaning; previously a bare 500.
     */
    SERVICE_ERROR("LEGACY.SERVICE_ERROR", ErrorCategory.INTERNAL),

    /**
     * Un-migrated bad request; what the old advice mapped to 400.
     */
    BAD_REQUEST("LEGACY.BAD_REQUEST", ErrorCategory.VALIDATION),

    /**
     * Un-migrated missing resource.
     */
    NOT_FOUND("LEGACY.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * Un-migrated authorization failure.
     */
    UNAUTHORIZED("LEGACY.UNAUTHORIZED", ErrorCategory.UNAUTHENTICATED),

    /**
     * Un-migrated uniqueness or referential constraint.
     */
    CONSTRAINT("LEGACY.CONSTRAINT", ErrorCategory.CONFLICT),

    /**
     * Un-migrated business rule refusing an operation.
     */
    OPERATION_NOT_ALLOWED("LEGACY.OPERATION_NOT_ALLOWED", ErrorCategory.UNPROCESSABLE),

    /**
     * Un-migrated parse or conversion failure.
     */
    CONVERSION("LEGACY.CONVERSION", ErrorCategory.CONVERSION);

    private final String code;

    private final ErrorCategory category;

    LegacyErrors(String code, ErrorCategory category) {
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

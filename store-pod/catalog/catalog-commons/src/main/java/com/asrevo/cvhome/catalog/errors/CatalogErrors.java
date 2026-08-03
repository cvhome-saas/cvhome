package com.asrevo.cvhome.catalog.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the catalog context.
 *
 * <p>
 * Introduced for the reservation API, whose codes checkout has to be able to tell apart on the wire; the rest of
 * catalog's conditions join it as their throw sites are migrated.
 * </p>
 */
public enum CatalogErrors implements ErrorCode {

    /**
     * The stock on hand does not cover what was asked for, or the sku has no availability record at all.
     *
     * <p>
     * A decision about the data, not a malformed request, hence 422 — and the one legacy {@code exceptionType}
     * ({@code ServiceException.EXCEPTION_INVENTORY_MISMATCH}) that carried real meaning.
     * </p>
     */
    RESERVATION_INSUFFICIENT_INVENTORY("CATALOG.RESERVATION.INSUFFICIENT_INVENTORY", ErrorCategory.UNPROCESSABLE),

    /**
     * A reservation was requested with no lines on it — the caller's bug, so 400.
     */
    RESERVATION_EMPTY("CATALOG.RESERVATION.EMPTY", ErrorCategory.VALIDATION);

    private final String code;

    private final ErrorCategory category;

    CatalogErrors(String code, ErrorCategory category) {
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

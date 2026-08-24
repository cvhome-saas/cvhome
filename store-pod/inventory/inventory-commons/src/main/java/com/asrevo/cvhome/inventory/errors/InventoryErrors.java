package com.asrevo.cvhome.inventory.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the inventory service. The {@link ErrorCategory} on each decides the HTTP status.
 */
public enum InventoryErrors implements ErrorCode {

    /**
     * The stock on hand does not cover what was asked for, or the sku is not stocked at all. A decision about the
     * data, not a malformed request, hence 422.
     */
    RESERVATION_INSUFFICIENT_INVENTORY("INVENTORY.RESERVATION.INSUFFICIENT_INVENTORY", ErrorCategory.UNPROCESSABLE),

    /**
     * A reservation was requested with no lines on it — the caller's bug, so 400.
     */
    RESERVATION_EMPTY("INVENTORY.RESERVATION.EMPTY", ErrorCategory.VALIDATION);

    private final String code;

    private final ErrorCategory category;

    InventoryErrors(String code, ErrorCategory category) {
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

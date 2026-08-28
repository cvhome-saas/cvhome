package com.asrevo.cvhome.customer.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the customer context.
 */
public enum CustomerErrors implements ErrorCode {

    /**
     * The submitted country ISO code is not one this store's reference data knows.
     */
    UNSUPPORTED_COUNTRY_CODE("CUSTOMER.COUNTRY.UNSUPPORTED", ErrorCategory.CONVERSION),

    /**
     * The submitted zone (state or province) code is not one this store's reference data knows.
     */
    UNSUPPORTED_ZONE_CODE("CUSTOMER.ZONE.UNSUPPORTED", ErrorCategory.CONVERSION),

    /**
     * No customer in this store matches the identity on the request.
     */
    CUSTOMER_NOT_FOUND("CUSTOMER.NOT_FOUND", ErrorCategory.NOT_FOUND);

    private final String code;

    private final ErrorCategory category;

    CustomerErrors(String code, ErrorCategory category) {
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

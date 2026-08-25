package com.asrevo.cvhome.store.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the shared pod module — the failures raised by utilities every pod service uses.
 */
public enum StoreErrors implements ErrorCode {

    /**
     * A monetary amount was not in a form the parser accepts.
     */
    PRICE_NOT_PARSEABLE("STORE.PRICE.NOT_PARSEABLE", ErrorCategory.CONVERSION),

    /**
     * A monetary amount parsed but was negative where only a positive value makes sense.
     */
    PRICE_NOT_POSITIVE("STORE.PRICE.NOT_POSITIVE", ErrorCategory.VALIDATION);

    private final String code;

    private final ErrorCategory category;

    StoreErrors(String code, ErrorCategory category) {
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

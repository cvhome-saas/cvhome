package com.asrevo.cvhome.merchant.errors;

import com.asrevo.cvhome.errors.ErrorCategory;
import com.asrevo.cvhome.errors.ErrorCode;

/**
 * Error codes owned by the merchant-store context.
 */
public enum MerchantErrors implements ErrorCode {

    /**
     * No merchant store exists for that id.
     */
    STORE_NOT_FOUND("MERCHANT.STORE.NOT_FOUND", ErrorCategory.NOT_FOUND),

    /**
     * A merchant store already exists with that id.
     */
    DUPLICATE_STORE("MERCHANT.STORE.DUPLICATE", ErrorCategory.CONFLICT),

    /**
     * The path and tenant query parameter identify different stores.
     */
    STORE_CONTEXT_MISMATCH("MERCHANT.STORE.CONTEXT_MISMATCH", ErrorCategory.MALFORMED),

    /**
     * The platform's default store cannot be deleted — a rule about the data, not about the caller.
     */
    DEFAULT_STORE_NOT_REMOVABLE("MERCHANT.STORE.DEFAULT_NOT_REMOVABLE", ErrorCategory.UNPROCESSABLE);

    private final String code;

    private final ErrorCategory category;

    MerchantErrors(String code, ErrorCategory category) {
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

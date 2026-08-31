package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A product option with that code already exists in this store.
 */
public class DuplicateProductOptionException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String CODE = "code";

    protected DuplicateProductOptionException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateProductOptionException of(Object code, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_DUPLICATE, DuplicateProductOptionException::new)
                .detail("Product option %s already exists in store %s.", code, store)
                .param(CODE, code)
                .param("store", store)
                .build();
    }

    /**
     * The same value code appears more than once inside one option write.
     */
    public static DuplicateProductOptionException duplicateValue(Object optionCode) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_DUPLICATE, DuplicateProductOptionException::new)
                .detail("Option %s carries the same value code more than once.", optionCode)
                .param(CODE, optionCode)
                .build();
    }
}

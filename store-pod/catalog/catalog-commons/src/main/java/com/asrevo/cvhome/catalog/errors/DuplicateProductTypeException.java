package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A product type with that code already exists in this store.
 */
public class DuplicateProductTypeException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateProductTypeException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateProductTypeException of(Object code, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_TYPE_DUPLICATE, DuplicateProductTypeException::new)
                .detail("Product type %s already exists in store %s.", code, store)
                .param("code", code)
                .param("store", store)
                .build();
    }

}

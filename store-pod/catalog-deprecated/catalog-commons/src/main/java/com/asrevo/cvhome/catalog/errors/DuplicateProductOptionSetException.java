package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A product option set with that code already exists in this store.
 */
public class DuplicateProductOptionSetException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateProductOptionSetException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateProductOptionSetException of(Object code, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_SET_DUPLICATE, DuplicateProductOptionSetException::new)
                .detail("Product option set %s already exists in store %s.", code, store)
                .param("code", code)
                .param("store", store)
                .build();
    }

}

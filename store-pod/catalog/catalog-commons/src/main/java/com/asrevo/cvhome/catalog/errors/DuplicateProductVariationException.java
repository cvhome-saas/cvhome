package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A product variation with that code already exists in this store.
 */
public class DuplicateProductVariationException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateProductVariationException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateProductVariationException of(Object code, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIATION_DUPLICATE, DuplicateProductVariationException::new)
                .detail("Product variation %s already exists in store %s.", code, store)
                .param("code", code)
                .param("store", store)
                .build();
    }

}

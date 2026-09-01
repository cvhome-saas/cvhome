package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product option with that id or code exists in this store.
 */
public class ProductOptionNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductOptionNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductOptionNotFoundException of(Object option, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_NOT_FOUND, ProductOptionNotFoundException::new)
                .detail("No product option %s in store %s.", option, store)
                .param("option", option)
                .param("store", store)
                .build();
    }
}

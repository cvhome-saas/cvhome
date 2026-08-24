package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product option value with that id exists in this store.
 */
public class ProductOptionValueNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductOptionValueNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductOptionValueNotFoundException of(Object optionValueId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_VALUE_NOT_FOUND, ProductOptionValueNotFoundException::new)
                .detail("No product option value %s in store %s.", optionValueId, store)
                .param("optionValueId", optionValueId)
                .param("store", store)
                .build();
    }
}

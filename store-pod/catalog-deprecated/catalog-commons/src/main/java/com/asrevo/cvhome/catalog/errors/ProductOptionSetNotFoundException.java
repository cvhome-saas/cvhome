package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product option set with that id exists in this store.
 */
public class ProductOptionSetNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductOptionSetNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductOptionSetNotFoundException of(Object optionSetId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_SET_NOT_FOUND, ProductOptionSetNotFoundException::new)
                .detail("No product option set %s in store %s.", optionSetId, store)
                .param("optionSetId", optionSetId)
                .param("store", store)
                .build();
    }
}

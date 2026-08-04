package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product type with that id or code exists in this store.
 */
public class ProductTypeNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductTypeNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductTypeNotFoundException of(Object productType, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_TYPE_NOT_FOUND, ProductTypeNotFoundException::new)
                .detail("No product type %s in store %s.", productType, store)
                .param("productType", productType)
                .param("store", store)
                .build();
    }
}

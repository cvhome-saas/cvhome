package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product with that id or sku exists in this store.
 */
public class ProductNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductNotFoundException of(Object productId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_NOT_FOUND, ProductNotFoundException::new)
                .detail("No product %s in store %s.", productId, store)
                .param("productId", productId)
                .param("store", store)
                .build();
    }
}

package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product variation with that id exists in this store.
 */
public class ProductVariationNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductVariationNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductVariationNotFoundException of(Object variationId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIATION_NOT_FOUND, ProductVariationNotFoundException::new)
                .detail("No product variation %s in store %s.", variationId, store)
                .param("variationId", variationId)
                .param("store", store)
                .build();
    }
}

package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product variant with that id exists in this store.
 */
public class ProductVariantNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductVariantNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductVariantNotFoundException of(Object variantId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_NOT_FOUND, ProductVariantNotFoundException::new)
                .detail("No product variant %s in store %s.", variantId, store)
                .param("variantId", variantId)
                .param("store", store)
                .build();
    }
}

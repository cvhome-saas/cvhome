package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product variant group with that id exists in this store.
 */
public class ProductVariantGroupNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductVariantGroupNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductVariantGroupNotFoundException of(Object groupId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_GROUP_NOT_FOUND, ProductVariantGroupNotFoundException::new)
                .detail("No product variant group %s in store %s.", groupId, store)
                .param("groupId", groupId)
                .param("store", store)
                .build();
    }
}

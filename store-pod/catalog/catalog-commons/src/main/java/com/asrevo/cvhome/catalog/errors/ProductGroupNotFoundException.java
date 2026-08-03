package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product group with that name exists in this store.
 */
public class ProductGroupNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductGroupNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductGroupNotFoundException of(Object groupName, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_GROUP_NOT_FOUND, ProductGroupNotFoundException::new)
                .detail("No product group %s in store %s.", groupName, store)
                .param("groupName", groupName)
                .param("store", store)
                .build();
    }
}

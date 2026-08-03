package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No product image with that id exists for the product.
 */
public class ProductImageNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductImageNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductImageNotFoundException of(Object imageId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_IMAGE_NOT_FOUND, ProductImageNotFoundException::new)
                .detail("No product image %s in store %s.", imageId, store)
                .param("imageId", imageId)
                .param("store", store)
                .build();
    }
}

package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Writing or removing a product image failed.
 */
public class ProductImageNotPersistedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductImageNotPersistedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductImageNotPersistedException of(Object productRef, Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_IMAGE_NOT_PERSISTED, ProductImageNotPersistedException::new)
                .detail("An image for product %s could not be stored.", productRef)
                .param("productRef", productRef)
                .cause(cause)
                .build();
    }

}

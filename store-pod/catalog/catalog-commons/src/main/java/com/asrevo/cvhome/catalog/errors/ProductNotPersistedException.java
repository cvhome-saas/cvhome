package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Persisting a product, or a change to one, failed.
 */
public class ProductNotPersistedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductNotPersistedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductNotPersistedException of(Object productId, Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_NOT_PERSISTED, ProductNotPersistedException::new)
                .detail("Product %s could not be saved.", productId)
                .param("productId", productId)
                .cause(cause)
                .build();
    }

}

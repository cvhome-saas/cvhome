package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload references a product that does not resolve in this store.
 *
 * <p>
 * A 400 about the body, not a 404 about the resource: the endpoint's own target exists, and it is a field inside the
 * payload that names nothing.
 * </p>
 */
public class ProductReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductReferenceUnresolvableException of(Object productId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_REFERENCE_UNRESOLVABLE, ProductReferenceUnresolvableException::new)
                .detail("No product %s in store %s.", productId, store)
                .param("productId", productId)
                .param("store", store)
                .build();
    }
}

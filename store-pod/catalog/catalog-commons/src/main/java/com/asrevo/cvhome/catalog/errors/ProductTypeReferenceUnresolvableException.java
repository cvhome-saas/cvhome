package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload references a product type that does not resolve in this store.
 *
 * <p>
 * A 400 about the body, not a 404 about the resource: the endpoint's own target exists, and it is a field inside the
 * payload that names nothing.
 * </p>
 */
public class ProductTypeReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductTypeReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductTypeReferenceUnresolvableException of(Object productType, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_TYPE_REFERENCE_UNRESOLVABLE, ProductTypeReferenceUnresolvableException::new)
                .detail("No product type %s in store %s.", productType, store)
                .param("productType", productType)
                .param("store", store)
                .build();
    }
}

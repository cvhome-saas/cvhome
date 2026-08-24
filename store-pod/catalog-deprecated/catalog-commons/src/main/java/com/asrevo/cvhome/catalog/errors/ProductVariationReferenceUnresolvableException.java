package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload references a product variation that does not resolve in this store.
 *
 * <p>
 * A 400 about the body, not a 404 about the resource: the endpoint's own target exists, and it is a field inside the
 * payload that names nothing.
 * </p>
 */
public class ProductVariationReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductVariationReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductVariationReferenceUnresolvableException of(Object variationId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIATION_REFERENCE_UNRESOLVABLE,
                        ProductVariationReferenceUnresolvableException::new)
                .detail("No product variation %s in store %s.", variationId, store)
                .param("variationId", variationId)
                .param("store", store)
                .build();
    }
}

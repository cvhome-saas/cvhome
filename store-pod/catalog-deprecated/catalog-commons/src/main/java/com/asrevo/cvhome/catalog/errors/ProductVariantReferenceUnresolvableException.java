package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload references a product variant that does not resolve in this store.
 *
 * <p>
 * A 400 about the body, not a 404 about the resource: the endpoint's own target exists, and it is a field inside the
 * payload that names nothing.
 * </p>
 */
public class ProductVariantReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductVariantReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductVariantReferenceUnresolvableException of(Object variantId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_REFERENCE_UNRESOLVABLE, ProductVariantReferenceUnresolvableException::new)
                .detail("No product variant %s in store %s.", variantId, store)
                .param("variantId", variantId)
                .param("store", store)
                .build();
    }
}

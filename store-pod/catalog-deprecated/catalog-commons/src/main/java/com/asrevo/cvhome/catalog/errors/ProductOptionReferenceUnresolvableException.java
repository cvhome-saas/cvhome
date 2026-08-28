package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload references a product option that does not resolve in this store.
 *
 * <p>
 * A 400 about the body, not a 404 about the resource: the endpoint's own target exists, and it is a field inside the
 * payload that names nothing.
 * </p>
 */
public class ProductOptionReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductOptionReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductOptionReferenceUnresolvableException of(Object optionId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_REFERENCE_UNRESOLVABLE, ProductOptionReferenceUnresolvableException::new)
                .detail("No product option %s in store %s.", optionId, store)
                .param("optionId", optionId)
                .param("store", store)
                .build();
    }
}

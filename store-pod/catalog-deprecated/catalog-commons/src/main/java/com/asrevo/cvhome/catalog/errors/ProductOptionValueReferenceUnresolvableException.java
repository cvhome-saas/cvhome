package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload references a product option value that does not resolve in this store.
 *
 * <p>
 * A 400 about the body, not a 404 about the resource: the endpoint's own target exists, and it is a field inside the
 * payload that names nothing.
 * </p>
 */
public class ProductOptionValueReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductOptionValueReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductOptionValueReferenceUnresolvableException of(Object optionValueId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_VALUE_REFERENCE_UNRESOLVABLE,
                        ProductOptionValueReferenceUnresolvableException::new)
                .detail("No product option value %s in store %s.", optionValueId, store)
                .param("optionValueId", optionValueId)
                .param("store", store)
                .build();
    }
}

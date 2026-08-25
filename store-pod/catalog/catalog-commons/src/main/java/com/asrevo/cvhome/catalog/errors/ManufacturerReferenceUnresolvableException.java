package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload references a manufacturer that does not resolve in this store.
 *
 * <p>
 * A 400 about the body, not a 404 about the resource: the endpoint's own target exists, and it is a field inside the
 * payload that names nothing.
 * </p>
 */
public class ManufacturerReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ManufacturerReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ManufacturerReferenceUnresolvableException of(Object manufacturerId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.MANUFACTURER_REFERENCE_UNRESOLVABLE, ManufacturerReferenceUnresolvableException::new)
                .detail("No manufacturer %s in store %s.", manufacturerId, store)
                .param("manufacturerId", manufacturerId)
                .param("store", store)
                .build();
    }
}

package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Persisting a manufacturer, or reading one back, failed.
 */
public class ManufacturerNotPersistedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ManufacturerNotPersistedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ManufacturerNotPersistedException of(Object manufacturerRef, Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.MANUFACTURER_NOT_PERSISTED, ManufacturerNotPersistedException::new)
                .detail("Manufacturer %s could not be saved or read.", manufacturerRef)
                .param("manufacturerRef", manufacturerRef)
                .cause(cause)
                .build();
    }

}

package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No manufacturer with that id exists in this store.
 */
public class ManufacturerNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ManufacturerNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ManufacturerNotFoundException of(Object manufacturerId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.MANUFACTURER_NOT_FOUND, ManufacturerNotFoundException::new)
                .detail("No manufacturer %s in store %s.", manufacturerId, store)
                .param("manufacturerId", manufacturerId)
                .param("store", store)
                .build();
    }
}

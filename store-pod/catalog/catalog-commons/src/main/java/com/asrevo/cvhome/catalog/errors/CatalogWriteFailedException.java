package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Persisting catalog data failed.
 *
 * <p>
 * The last resort of the catalog services: a write the database refused for a reason this code did not check first. A
 * service that knows which rule was broken should throw the condition-named type instead.
 * </p>
 */
public class CatalogWriteFailedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CatalogWriteFailedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CatalogWriteFailedException of(Object what, Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.CATALOG_WRITE_FAILED, CatalogWriteFailedException::new)
                .detail("%s could not be saved.", what)
                .param("resource", what)
                .cause(cause)
                .build();
    }

}

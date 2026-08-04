package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Reading catalog data back out of the database failed.
 */
public class CatalogReadFailedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CatalogReadFailedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CatalogReadFailedException of(Object what, Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.CATALOG_READ_FAILED, CatalogReadFailedException::new)
                .detail("%s could not be read.", what)
                .param("resource", what)
                .cause(cause)
                .build();
    }

}

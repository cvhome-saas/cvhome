package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Reading categories back out of the database failed.
 */
public class CategoryNotReadableException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CategoryNotReadableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CategoryNotReadableException of(Object categoryRef, Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_NOT_READABLE, CategoryNotReadableException::new)
                .detail("Category %s could not be read.", categoryRef)
                .param("categoryRef", categoryRef)
                .cause(cause)
                .build();
    }

}

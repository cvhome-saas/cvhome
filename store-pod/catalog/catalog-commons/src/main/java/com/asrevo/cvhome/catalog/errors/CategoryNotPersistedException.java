package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Persisting a category, or a change to one, failed.
 */
public class CategoryNotPersistedException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CategoryNotPersistedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CategoryNotPersistedException of(Object categoryId, Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_NOT_PERSISTED, CategoryNotPersistedException::new)
                .detail("Category %s could not be saved.", categoryId)
                .param("categoryId", categoryId)
                .cause(cause)
                .build();
    }

}

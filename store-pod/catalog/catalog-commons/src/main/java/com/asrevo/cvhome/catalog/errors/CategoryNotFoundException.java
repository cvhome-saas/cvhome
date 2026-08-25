package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No category with that id or code exists in this store.
 */
public class CategoryNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CategoryNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CategoryNotFoundException of(Object category, Object store) {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_NOT_FOUND, CategoryNotFoundException::new)
                .detail("No category %s in store %s.", category, store)
                .param("category", category)
                .param("store", store)
                .build();
    }
}

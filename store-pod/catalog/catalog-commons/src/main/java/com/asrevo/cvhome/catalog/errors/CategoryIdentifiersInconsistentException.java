package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * A category's id and code identify two different categories, so the reference is self-contradictory.
 */
public class CategoryIdentifiersInconsistentException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CategoryIdentifiersInconsistentException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CategoryIdentifiersInconsistentException of(Object store) {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_IDENTIFIERS_INCONSISTENT, CategoryIdentifiersInconsistentException::new)
                .detail("The category id and code submitted for store %s identify different categories.", store)
                .param("store", store)
                .build();
    }

}

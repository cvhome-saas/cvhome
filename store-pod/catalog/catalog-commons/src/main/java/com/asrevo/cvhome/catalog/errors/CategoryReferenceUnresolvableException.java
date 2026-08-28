package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload references a category that does not resolve in this store, or names one by neither id nor code.
 *
 * <p>
 * A 400 about the body, not a 404 about the resource: the endpoint's own target exists, and it is a field inside the
 * payload that names nothing.
 * </p>
 */
public class CategoryReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CategoryReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CategoryReferenceUnresolvableException of(Object category, Object store) {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_REFERENCE_UNRESOLVABLE,
                        CategoryReferenceUnresolvableException::new)
                .detail("No category %s in store %s.", category, store)
                .param("category", category)
                .param("store", store)
                .build();
    }

    /**
     * The reference carries neither an id nor a code, so there is nothing to look up.
     */
    public static CategoryReferenceUnresolvableException incomplete() {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_REFERENCE_UNRESOLVABLE,
                        CategoryReferenceUnresolvableException::new)
                .detail("A category reference needs at least an id or a code.")
                .build();
    }

}

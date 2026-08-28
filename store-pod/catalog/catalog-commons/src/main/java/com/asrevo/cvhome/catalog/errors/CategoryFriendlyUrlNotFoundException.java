package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * A category was addressed by a friendly URL that matches nothing in this store.
 */
public class CategoryFriendlyUrlNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CategoryFriendlyUrlNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CategoryFriendlyUrlNotFoundException of(Object friendlyUrl, Object store) {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_FRIENDLY_URL_NOT_FOUND, CategoryFriendlyUrlNotFoundException::new)
                .detail("No category at %s in store %s.", friendlyUrl, store)
                .param("friendlyUrl", friendlyUrl)
                .param("store", store)
                .build();
    }

}

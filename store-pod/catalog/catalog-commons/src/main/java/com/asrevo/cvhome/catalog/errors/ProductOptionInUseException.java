package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The option (or one of its values) is still referenced by a product's assignments or variants, so it cannot be
 * deleted — a 409, the caller must detach it everywhere first.
 */
public class ProductOptionInUseException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductOptionInUseException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductOptionInUseException of(Object option, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_IN_USE, ProductOptionInUseException::new)
                .detail("Product option %s is still used by products in store %s.", option, store)
                .param("option", option)
                .param("store", store)
                .build();
    }
}

package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * Two variants of the same product describe the same option-value combination.
 */
public class DuplicateVariantCombinationException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateVariantCombinationException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateVariantCombinationException of(Object signature, Object productId) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_DUPLICATE_COMBINATION,
                DuplicateVariantCombinationException::new)
                .detail("Product %s already holds the combination %s.", productId, signature)
                .param("signature", signature)
                .param("productId", productId)
                .build();
    }
}

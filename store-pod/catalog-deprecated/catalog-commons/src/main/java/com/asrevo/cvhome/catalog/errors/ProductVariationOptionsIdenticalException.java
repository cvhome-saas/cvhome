package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * A variation's option and option value name the same thing, and must differ.
 */
public class ProductVariationOptionsIdenticalException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductVariationOptionsIdenticalException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductVariationOptionsIdenticalException of(Object option) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIATION_OPTIONS_IDENTICAL,
                        ProductVariationOptionsIdenticalException::new)
                .detail("A variation's option and option value must differ; both are %s.", option)
                .param("option", option)
                .build();
    }

}

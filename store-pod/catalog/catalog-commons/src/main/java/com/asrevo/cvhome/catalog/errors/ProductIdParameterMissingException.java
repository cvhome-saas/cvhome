package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A required request parameter naming the product was absent.
 */
public class ProductIdParameterMissingException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductIdParameterMissingException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductIdParameterMissingException of() {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_ID_PARAMETER_MISSING, ProductIdParameterMissingException::new)
                .detail("The productId request parameter is required.")
                .build();
    }

}

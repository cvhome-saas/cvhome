package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A product could not be converted between its persisted and its API form.
 */
public class ProductNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductNotConvertibleException of(Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_NOT_CONVERTIBLE, ProductNotConvertibleException::new)
                .detail("A product could not be converted.")
                .cause(cause)
                .build();
    }
}

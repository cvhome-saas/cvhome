package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A product option could not be converted.
 */
public class ProductOptionNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductOptionNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductOptionNotConvertibleException of(Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_OPTION_NOT_CONVERTIBLE, ProductOptionNotConvertibleException::new)
                .detail("A product option could not be converted.")
                .cause(cause)
                .build();
    }
}

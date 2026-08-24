package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A product attribute could not be converted.
 */
public class ProductAttributeNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductAttributeNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductAttributeNotConvertibleException of(Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_ATTRIBUTE_NOT_CONVERTIBLE, ProductAttributeNotConvertibleException::new)
                .detail("A product attribute could not be converted.")
                .cause(cause)
                .build();
    }
}

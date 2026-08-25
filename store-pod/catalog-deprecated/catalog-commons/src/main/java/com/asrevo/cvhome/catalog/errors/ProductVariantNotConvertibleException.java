package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A product variant could not be converted.
 */
public class ProductVariantNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductVariantNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductVariantNotConvertibleException of(Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_NOT_CONVERTIBLE, ProductVariantNotConvertibleException::new)
                .detail("A product variant could not be converted.")
                .cause(cause)
                .build();
    }
}

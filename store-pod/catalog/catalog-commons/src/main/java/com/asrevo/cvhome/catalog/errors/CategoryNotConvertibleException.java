package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A category could not be converted.
 */
public class CategoryNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CategoryNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CategoryNotConvertibleException of(Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_NOT_CONVERTIBLE, CategoryNotConvertibleException::new)
                .detail("A category could not be converted.")
                .cause(cause)
                .build();
    }
}

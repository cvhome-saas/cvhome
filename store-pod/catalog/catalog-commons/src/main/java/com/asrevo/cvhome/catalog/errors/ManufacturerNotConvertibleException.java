package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A manufacturer could not be converted.
 */
public class ManufacturerNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ManufacturerNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ManufacturerNotConvertibleException of(Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.MANUFACTURER_NOT_CONVERTIBLE, ManufacturerNotConvertibleException::new)
                .detail("A manufacturer could not be converted.")
                .cause(cause)
                .build();
    }
}

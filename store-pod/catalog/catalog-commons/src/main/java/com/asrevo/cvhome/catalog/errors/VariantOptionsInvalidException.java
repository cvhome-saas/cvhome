package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * The variant set does not fit its declared axes: a variant misses an option, carries a value of an undeclared
 * or foreign option, carries two values of one option — or combinations were sent with no axes at all.
 */
public class VariantOptionsInvalidException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected VariantOptionsInvalidException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static VariantOptionsInvalidException of(Object sku, String reason) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_OPTIONS_INVALID, VariantOptionsInvalidException::new)
                .detail("Variant %s does not fit the declared options: %s", sku, reason)
                .param("sku", sku)
                .param("reason", reason)
                .build();
    }
}

package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A variant has no parent product to render against.
 *
 * <p>
 * Reached from inside a mapper, so it is a {@link ConversionException}: it is what a read of a variant row whose
 * product link is null looks like from there. The 400 is a compromise the SPI forces — the underlying fault is usually
 * ours, and the {@code traceId} is what leads to it.
 * </p>
 */
public class ProductVariantParentMissingException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductVariantParentMissingException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductVariantParentMissingException of(Object sku) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_PARENT_MISSING,
                        ProductVariantParentMissingException::new)
                .detail("The variants submitted do not include the parent product %s.", sku)
                .param("sku", sku)
                .build();
    }

}

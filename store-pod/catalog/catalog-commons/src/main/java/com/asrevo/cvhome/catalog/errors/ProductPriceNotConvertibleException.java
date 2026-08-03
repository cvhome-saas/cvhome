package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A price could not be converted, or its final amount could not be calculated.
 */
public class ProductPriceNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductPriceNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductPriceNotConvertibleException of(Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_PRICE_NOT_CONVERTIBLE, ProductPriceNotConvertibleException::new)
                .detail("A product price could not be converted.")
                .cause(cause)
                .build();
    }

    /**
     * The amount is present but could not be rendered or parsed — a date or a currency amount in a shape the store's
     * locale does not accept.
     */
    public static ProductPriceNotConvertibleException of(Object value, Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_PRICE_NOT_CONVERTIBLE, ProductPriceNotConvertibleException::new)
                .detail("Value %s could not be converted for this store.", value)
                .param("value", value)
                .cause(cause)
                .build();
    }

}

package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * Reading a product back out of the database failed.
 *
 * <p>
 * Distinct from {@link ProductNotFoundException}: the row may well exist. This says the query itself did not complete,
 * which the legacy {@code ServiceException} wrapper could not express.
 * </p>
 */
public class ProductNotReadableException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductNotReadableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductNotReadableException of(Object productRef, Throwable cause) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_NOT_READABLE, ProductNotReadableException::new)
                .detail("Product %s could not be read.", productRef)
                .param("productRef", productRef)
                .cause(cause)
                .build();
    }

}

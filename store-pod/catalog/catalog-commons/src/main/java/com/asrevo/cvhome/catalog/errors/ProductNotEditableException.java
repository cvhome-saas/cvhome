package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The product cannot be edited — it is not this store's to change.
 */
public class ProductNotEditableException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductNotEditableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductNotEditableException of(Object productId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_NOT_EDITABLE, ProductNotEditableException::new)
                .detail("Product %s cannot be edited by store %s.", productId, store)
                .param("productId", productId)
                .param("store", store)
                .build();
    }

}

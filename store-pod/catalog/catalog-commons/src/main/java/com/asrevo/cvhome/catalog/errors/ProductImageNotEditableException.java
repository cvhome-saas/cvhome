package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The product image cannot be edited or deleted — it belongs to another store's product.
 */
public class ProductImageNotEditableException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductImageNotEditableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductImageNotEditableException of(Object imageId, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_IMAGE_NOT_EDITABLE, ProductImageNotEditableException::new)
                .detail("Product image %s cannot be modified by store %s.", imageId, store)
                .param("imageId", imageId)
                .param("store", store)
                .build();
    }

}

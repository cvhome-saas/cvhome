package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The category is already attached to that product.
 *
 * <p>
 * A 409 rather than the 400 the legacy {@code OperationNotAllowedException} produced: the request is well-formed and
 * the state it asks for already holds, which is a different thing for a client to handle than a bad request.
 * </p>
 */
public class CategoryAlreadyAttachedException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CategoryAlreadyAttachedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CategoryAlreadyAttachedException of(Object categoryId, Object productId) {
        return new ErrorBuilder<>(CatalogErrors.CATEGORY_ALREADY_ATTACHED, CategoryAlreadyAttachedException::new)
                .detail("Category %s is already attached to product %s.", categoryId, productId)
                .param("categoryId", categoryId)
                .param("productId", productId)
                .build();
    }

}

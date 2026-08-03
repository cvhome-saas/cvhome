package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The product has no inventory a price can be calculated from.
 *
 * <p>
 * A statement about the product's configuration, not about the request, so 422 rather than the 500 the legacy
 * {@code ServiceException.EXCEPTION_ERROR} produced: a merchant who has not yet priced a product was being shown a
 * server fault.
 * </p>
 */
public class NoApplicableInventoryException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected NoApplicableInventoryException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static NoApplicableInventoryException of(Object sku) {
        return new ErrorBuilder<>(CatalogErrors.PRICING_NO_APPLICABLE_INVENTORY, NoApplicableInventoryException::new)
                .detail("Product %s has no inventory to calculate a price from.", sku)
                .param("sku", sku)
                .build();
    }

}

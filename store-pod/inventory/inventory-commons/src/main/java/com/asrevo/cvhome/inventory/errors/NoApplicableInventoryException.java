package com.asrevo.cvhome.inventory.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The sku has no inventory a price can be calculated from.
 *
 * <p>
 * A statement about the store's configuration, not about the request, so 422: a merchant who has not yet priced a
 * product should not be shown a server fault.
 * </p>
 */
public class NoApplicableInventoryException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected NoApplicableInventoryException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static NoApplicableInventoryException of(Object sku) {
        return new ErrorBuilder<>(InventoryErrors.PRICING_NO_APPLICABLE_INVENTORY, NoApplicableInventoryException::new)
                .detail("Sku %s has no inventory to calculate a price from.", sku)
                .param("sku", sku)
                .build();
    }

}

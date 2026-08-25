package com.asrevo.cvhome.inventory.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * A reservation line asked for more of a sku than the store has, or for a sku with no availability at all.
 *
 * <p>
 * A <em>decision</em>, as against the service being unreachable, so a caller may cancel the order on it and name the
 * sku that blocked it.
 * </p>
 */
public class InsufficientInventoryException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InsufficientInventoryException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InsufficientInventoryException of(String sku, int requested, int available) {
        return new ErrorBuilder<>(InventoryErrors.RESERVATION_INSUFFICIENT_INVENTORY,
                InsufficientInventoryException::new)
                .detail("Only %d of sku %s available, %d requested.", available, sku, requested)
                .param("sku", sku)
                .param("requested", requested)
                .param("available", available)
                .build();
    }

    /**
     * No availability row exists for the sku in this store — nothing is stocked, which the caller cannot distinguish
     * from a zero quantity and does not need to.
     */
    public static InsufficientInventoryException notStocked(String sku, int requested) {
        return of(sku, requested, 0);
    }

}

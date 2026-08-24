package com.asrevo.cvhome.inventory.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload references an inventory record that does not resolve in this store.
 *
 * <p>
 * A 400 about the body, not a 404 about the resource: the endpoint's own target exists, and it is a field inside the
 * payload that names nothing.
 * </p>
 */
public class InventoryReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InventoryReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InventoryReferenceUnresolvableException of(Object inventoryId, Object store) {
        return new ErrorBuilder<>(InventoryErrors.INVENTORY_REFERENCE_UNRESOLVABLE,
                InventoryReferenceUnresolvableException::new)
                .detail("No inventory record %s in store %s.", inventoryId, store)
                .param("inventoryId", inventoryId)
                .param("store", store)
                .build();
    }
}

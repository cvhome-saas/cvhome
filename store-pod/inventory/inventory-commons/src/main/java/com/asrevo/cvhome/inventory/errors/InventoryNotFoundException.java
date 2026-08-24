package com.asrevo.cvhome.inventory.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No inventory (product availability) with that id exists in this store.
 */
public class InventoryNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InventoryNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InventoryNotFoundException of(Object inventoryId, Object store) {
        return new ErrorBuilder<>(InventoryErrors.INVENTORY_NOT_FOUND, InventoryNotFoundException::new)
                .detail("No inventory record %s in store %s.", inventoryId, store)
                .param("inventoryId", inventoryId)
                .param("store", store)
                .build();
    }
}

package com.asrevo.cvhome.inventory.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * An inventory record could not be converted between its persisted and its API form.
 */
public class InventoryNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected InventoryNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static InventoryNotConvertibleException of(Throwable cause) {
        return new ErrorBuilder<>(InventoryErrors.INVENTORY_NOT_CONVERTIBLE, InventoryNotConvertibleException::new)
                .detail("Inventory record could not be converted.")
                .cause(cause)
                .build();
    }

}

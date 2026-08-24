package com.asrevo.cvhome.inventory.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted payload names a sku that has no inventory record in this store.
 *
 * <p>
 * The inventory service does not know products — the sku string is its only cross-service key — so this is the
 * counterpart of catalog's {@code ProductReferenceUnresolvableException} in inventory's vocabulary.
 * </p>
 */
public class SkuReferenceUnresolvableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SkuReferenceUnresolvableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static SkuReferenceUnresolvableException of(Object sku, Object store) {
        return new ErrorBuilder<>(InventoryErrors.SKU_REFERENCE_UNRESOLVABLE, SkuReferenceUnresolvableException::new)
                .detail("No inventory for sku %s in store %s.", sku, store)
                .param("sku", sku)
                .param("store", store)
                .build();
    }
}

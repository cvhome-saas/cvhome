package com.asrevo.cvhome.inventory.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No price with that id exists for the sku in this store.
 */
public class ProductPriceNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductPriceNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductPriceNotFoundException of(Object priceId, Object store) {
        return new ErrorBuilder<>(InventoryErrors.PRODUCT_PRICE_NOT_FOUND, ProductPriceNotFoundException::new)
                .detail("No price %s in store %s.", priceId, store)
                .param("priceId", priceId)
                .param("store", store)
                .build();
    }

}

package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The sku is already taken in this store — by another product's variant, or twice inside one write.
 */
public class DuplicateVariantSkuException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateVariantSkuException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateVariantSkuException of(Object sku, Object store) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_DUPLICATE_SKU, DuplicateVariantSkuException::new)
                .detail("Sku %s is already taken in store %s.", sku, store)
                .param("sku", sku)
                .param("store", store)
                .build();
    }
}

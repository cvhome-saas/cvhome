package com.asrevo.cvhome.catalog.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A product variant's sku must differ from its parent product's, and does not.
 */
public class ProductVariantSkuConflictException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductVariantSkuConflictException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductVariantSkuConflictException of(Object variantSku, Object productSku) {
        return new ErrorBuilder<>(CatalogErrors.PRODUCT_VARIANT_SKU_CONFLICT, ProductVariantSkuConflictException::new)
                .detail("Variant sku %s must differ from the product's own sku %s.", variantSku, productSku)
                .param("variantSku", variantSku)
                .param("productSku", productSku)
                .build();
    }

}

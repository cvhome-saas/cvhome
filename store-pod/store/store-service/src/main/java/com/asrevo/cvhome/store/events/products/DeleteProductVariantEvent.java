package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;

public class DeleteProductVariantEvent extends ProductEvent {

    private static final long serialVersionUID = 1L;
    private ProductVariant variant;

    public DeleteProductVariantEvent(Object source, ProductVariant variant, Product product) {
        super(source, product);
        this.variant = variant;
    }

    public ProductVariant getVariant() {
        return variant;
    }

}

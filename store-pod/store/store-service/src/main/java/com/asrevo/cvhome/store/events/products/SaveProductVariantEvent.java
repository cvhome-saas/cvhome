package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.variant.ProductVariant;
import lombok.Getter;

@Getter
public class SaveProductVariantEvent extends ProductEvent {

    private static final long serialVersionUID = 1L;
    private ProductVariant variant;

    public SaveProductVariantEvent(Object source, ProductVariant variant, Product product) {
        super(source, product);
        this.variant = variant;
    }


}

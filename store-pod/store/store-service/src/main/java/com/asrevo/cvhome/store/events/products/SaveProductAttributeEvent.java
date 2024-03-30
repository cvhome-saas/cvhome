package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;

public class SaveProductAttributeEvent extends ProductEvent {


    private static final long serialVersionUID = 1L;
    private ProductAttribute productAttribute;

    public SaveProductAttributeEvent(Object source, ProductAttribute productAttribute, Product product) {
        super(source, product);
        this.productAttribute = productAttribute;
    }

    public ProductAttribute getProductAttribute() {
        return productAttribute;
    }

}

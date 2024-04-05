package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import lombok.Getter;

import java.io.Serial;

@Getter
public class DeleteProductAttributeEvent extends ProductEvent {


    @Serial
    private static final long serialVersionUID = 1L;
    private final ProductAttribute productAttribute;

    public DeleteProductAttributeEvent(Object source, ProductAttribute productAttribute, Product product) {
        super(source, product);
        this.productAttribute = productAttribute;
    }

}

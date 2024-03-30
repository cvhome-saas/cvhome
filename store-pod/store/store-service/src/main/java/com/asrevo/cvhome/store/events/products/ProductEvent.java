package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import org.springframework.context.ApplicationEvent;

public abstract class ProductEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    private Product product;

    public ProductEvent(Object source, Product product) {
        super(source);
        this.product = product;
    }


    public Product getProduct() {
        return product;
    }

}

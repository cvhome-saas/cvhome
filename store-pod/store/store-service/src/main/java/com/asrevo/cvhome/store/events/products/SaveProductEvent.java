package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;

public class SaveProductEvent extends ProductEvent {

    private static final long serialVersionUID = 1L;

    public SaveProductEvent(Object source, Product product) {
        super(source, product);
    }


}

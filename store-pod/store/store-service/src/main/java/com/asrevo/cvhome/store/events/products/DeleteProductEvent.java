package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import java.io.Serial;

public class DeleteProductEvent extends ProductEvent {

    @Serial private static final long serialVersionUID = 1L;

    public DeleteProductEvent(Object source, Product product) {
        super(source, product);
    }
}

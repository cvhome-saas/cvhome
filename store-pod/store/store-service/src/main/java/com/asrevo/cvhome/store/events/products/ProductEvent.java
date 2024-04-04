package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public abstract class ProductEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;
    private final Product product;

    public ProductEvent(Object source, Product product) {
        super(source);
        this.product = product;
    }


}

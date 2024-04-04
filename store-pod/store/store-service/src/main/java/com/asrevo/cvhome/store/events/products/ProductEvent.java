package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;

@Getter
public abstract class ProductEvent extends ApplicationEvent {

    @Serial
    private static final long serialVersionUID = 1L;
    private final Product product;

    public ProductEvent(Object source, Product product) {
        super(source);
        this.product = product;
    }


}

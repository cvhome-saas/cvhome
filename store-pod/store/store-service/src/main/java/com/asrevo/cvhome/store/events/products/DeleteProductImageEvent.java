package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import java.io.Serial;
import lombok.Getter;

@Getter
public class DeleteProductImageEvent extends ProductEvent {

    @Serial private static final long serialVersionUID = 1L;
    private final ProductImage productImage;

    public DeleteProductImageEvent(Object source, ProductImage productImage, Product product) {
        super(source, product);
        this.productImage = productImage;
    }
}

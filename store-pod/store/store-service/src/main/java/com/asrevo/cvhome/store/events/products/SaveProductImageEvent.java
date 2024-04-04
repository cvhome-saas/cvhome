package com.asrevo.cvhome.store.events.products;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import lombok.Getter;

@Getter
public class SaveProductImageEvent extends ProductEvent {


    private static final long serialVersionUID = 1L;
    private ProductImage productImage;

    public SaveProductImageEvent(Object source, ProductImage productImage, Product product) {
        super(source, product);
        this.productImage = productImage;

    }


}

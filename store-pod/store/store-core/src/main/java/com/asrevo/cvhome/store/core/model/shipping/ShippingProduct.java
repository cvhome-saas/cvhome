package com.asrevo.cvhome.store.core.model.shipping;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ShippingProduct {

    private int quantity = 1;
    private Product product;
    private FinalPrice finalPrice;

    public ShippingProduct(Product product) {
        this.product = product;
    }
}

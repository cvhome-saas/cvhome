package com.asrevo.cvhome.store.core.entity.catalog.product.inventory;

import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductInventory implements Serializable {

    @Serial private static final long serialVersionUID = 1L;

    private String sku;
    private long quantity;
    private FinalPrice price;
}

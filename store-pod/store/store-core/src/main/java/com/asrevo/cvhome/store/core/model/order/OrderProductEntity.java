package com.asrevo.cvhome.store.core.model.order;

import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class OrderProductEntity extends OrderProduct implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int orderedQuantity;
    private ReadableProduct product;


}

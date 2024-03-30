package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValue;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ProductOptionValueEntity extends ProductOptionValue implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int order;


}

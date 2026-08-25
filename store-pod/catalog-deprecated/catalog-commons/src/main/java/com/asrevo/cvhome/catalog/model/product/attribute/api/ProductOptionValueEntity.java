package com.asrevo.cvhome.catalog.model.product.attribute.api;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionValue;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductOptionValueEntity extends ProductOptionValue implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private int order;

}

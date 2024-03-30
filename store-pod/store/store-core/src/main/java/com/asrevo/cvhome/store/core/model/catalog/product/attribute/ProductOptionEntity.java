package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class ProductOptionEntity extends ProductPropertyOption implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int order;

    private String type;

}

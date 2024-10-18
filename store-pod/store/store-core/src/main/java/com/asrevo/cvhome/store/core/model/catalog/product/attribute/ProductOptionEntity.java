package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductOptionEntity extends ProductPropertyOption implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private int order;

    private String type;
}

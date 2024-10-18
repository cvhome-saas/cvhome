package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValue;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ProductOptionValueEntity extends ProductOptionValue implements Serializable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    private int order;
}

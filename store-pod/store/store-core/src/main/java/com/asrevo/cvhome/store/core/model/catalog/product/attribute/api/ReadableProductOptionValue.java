package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValueDescription;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableProductOptionValue extends ProductOptionValueEntity {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private String price;
    private ProductOptionValueDescription description;

}

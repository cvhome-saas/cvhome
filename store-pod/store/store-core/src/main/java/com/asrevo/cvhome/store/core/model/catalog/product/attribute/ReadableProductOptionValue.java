package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;

@Setter
@Getter
public class ReadableProductOptionValue extends ProductOptionValue {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private String price;
    private String image;
    private String description;


}

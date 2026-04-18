package com.asrevo.cvhome.catalog.model.product.attribute;

import java.io.Serial;

import lombok.Getter;
import lombok.Setter;

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

package com.asrevo.cvhome.catalog.model.product.attribute;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductPropertyValue extends ProductOptionValue {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductOptionValueDescription> values = new ArrayList<>();

}

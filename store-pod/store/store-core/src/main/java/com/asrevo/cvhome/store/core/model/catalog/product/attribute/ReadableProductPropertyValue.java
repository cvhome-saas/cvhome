package com.asrevo.cvhome.store.core.model.catalog.product.attribute;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductPropertyValue extends ProductOptionValue {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductOptionValueDescription> values = new ArrayList<ProductOptionValueDescription>();


}

package com.asrevo.cvhome.store.core.model.catalog.product.attribute.api;

import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ProductOptionValueDescription;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductOptionValueFull extends ReadableProductOptionValue {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private List<ProductOptionValueDescription> descriptions = new ArrayList<ProductOptionValueDescription>();

}

package com.asrevo.cvhome.store.core.model.catalog.product;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ReadableProductPriceFull extends ReadableProductPrice {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private List<ProductPriceDescription> descriptions = new ArrayList<ProductPriceDescription>();


}

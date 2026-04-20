package com.asrevo.cvhome.catalog.model.product.attribute.api;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import com.asrevo.cvhome.catalog.model.product.attribute.ProductOptionDescription;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductOptionFull extends ReadableProductOptionEntity {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<ProductOptionDescription> descriptions = new ArrayList<>();

}

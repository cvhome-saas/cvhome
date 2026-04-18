package com.asrevo.cvhome.catalog.model.product;

import java.io.Serial;

import com.asrevo.cvhome.catalog.model.product.group.ProductGroup;
import com.asrevo.cvhome.commons.domain.ReadableList;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadableProductList extends ReadableList<ReadableProduct> {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private ProductGroup productGroup;

}
